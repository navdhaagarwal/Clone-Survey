/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.dao.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.nucleus.core.common.EntityUtil;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.Entity;
import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 * This Class is used for search queries having nested OGNL expressions in the where clause. 
 * Currently only support for limited query type : TODO add additional where clause. 
 */
public class CriteriaQueryExecutor<T> implements QueryExecutor<T> {

    public static final Integer       IN_OPERATOR                    = 3;
    public static final Integer       LIKE_OPERATOR                  = 2;
    public static final Integer       LIKE_OPERATOR_WITH_ESCAPE      = 10;
    public static final Integer       EQUALS_OPERATOR                = 1;
    public static final Integer       NOT_EQUALS_OPERATOR            = 11;
    public static final Integer       BETWEEN_OPERATOR               = 4;
    public static final Integer       NOT_IN_OPERATOR                = 5;
    public static final Integer       LESS_THAN_OPERATOR             = 6;
    public static final Integer       LESS_THAN_EQUAL_TO_OPERATOR    = 7;
    public static final Integer       GREATER_THAN_OPERATOR          = 8;
    public static final Integer       GREATER_THAN_EQUAL_TO_OPERATOR = 9;

    private Class<? extends Entity>   queryClazz;
    private List<KeyOptrValueHolder>  andCustomClauses;
    private List<KeyOptrValueHolder>  orCustomClauses;
    private List<KeyOptrValueHolder>  andOrCustomClauses;
    private final Map<String, String> selectedProperties             = new LinkedHashMap<String, String>();
    protected Map<String, Object>     queryHints                     = new LinkedHashMap<String, Object>();

    public CriteriaQueryExecutor(Class<? extends Entity> queryClass) {
        this(queryClass, null);
    }

    public CriteriaQueryExecutor(Class<? extends Entity> queryClass, String entityAlias) {
        Validate.notNull(queryClass, "Class for execution of query cannot be null");
        queryClazz = queryClass;
    }

    public CriteriaQueryExecutor<?> addQueryColumns(String... columnNames) {
        for (String columnName : columnNames) {
            selectedProperties.put(columnName, columnName);
        }
        return this;
    }

    public CriteriaQueryExecutor<?> addColumn(String ognl, String alias) {
        selectedProperties.put(ognl, alias);
        return this;
    }

    public CriteriaQueryExecutor<?> addAndClause(String fieldName, Integer operator, Object o) {
        if (andCustomClauses == null) {
            andCustomClauses = new LinkedList<KeyOptrValueHolder>();
        }
        HashMap<Integer, Object> operatorValueMap = new HashMap<Integer, Object>();
        operatorValueMap.put(operator, o);
        andCustomClauses.add(new KeyOptrValueHolder(fieldName.intern(), operator, o));
        return this;
    }

    public CriteriaQueryExecutor<?> addAndOrClause(String fieldName, Integer operator, Object o) {
        if (andOrCustomClauses == null) {
            andOrCustomClauses = new LinkedList<KeyOptrValueHolder>();
        }
        HashMap<Integer, Object> operatorValueMap = new HashMap<Integer, Object>();
        operatorValueMap.put(operator, o);
        andOrCustomClauses.add(new KeyOptrValueHolder(fieldName.intern(), operator, o));
        return this;
    }

    public CriteriaQueryExecutor<?> addOrClause(String fieldName, Integer operator, Object o) {
        if (orCustomClauses == null) {
            orCustomClauses = new LinkedList<KeyOptrValueHolder>();
        }
        HashMap<Integer, Object> operatorValueMap = new HashMap<Integer, Object>();
        operatorValueMap.put(operator, o);
        Object prevValue = orCustomClauses.add(new KeyOptrValueHolder(fieldName.intern(), operator, o));
        if (prevValue != null) {
            BaseLoggers.persistenceLogger.debug("The query coantains more than one reference of " + fieldName
                    + " in where clause");
        }
        return this;
    }

    public CriteriaQueryExecutor<T> addQueryHint(String queryHintName, Object queryHintValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(queryHintName), "Query Hint name cannot be blank");
        NeutrinoValidator.notNull(queryHintValue, "Query Hint value cannot be null");
        queryHints.put(queryHintName.intern(), queryHintValue);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> executeQuery(EntityManager em, Integer startIndex, Integer pageSize) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery((Class<T>) queryClazz);
        criteriaQuery.distinct(true);
        Root<T> fromClause = criteriaQuery.from((Class<T>) queryClazz);
        criteriaQuery.select(fromClause);

        Predicate andCombinedPredicate = null;
        if (andCustomClauses != null) {
            Predicate[] andPredicatesArray = createPredicateArray(em, criteriaBuilder, fromClause, andCustomClauses);
            andCombinedPredicate = criteriaBuilder.and(andPredicatesArray);
        }
        Predicate orCombinedPredicate = null;
        if (orCustomClauses != null) {
            Predicate[] orPredicatesArray = createPredicateArray(em, criteriaBuilder, fromClause, orCustomClauses);
            orCombinedPredicate = criteriaBuilder.or(orPredicatesArray);
        }

        /*
         * And + Or Grouped Custom Clause
         * e.g. where col1 = 'xyz' and (col2 = 'aa' or col3 = 'bb')  <------Grouped Condition
         */
        Predicate andOrCombinedFirstPredicate = null;
        Predicate andOrCombinedSecondPredicate = null;
        if (andOrCustomClauses != null) {
            Predicate[] orPredicatesArray = createPredicateArray(em, criteriaBuilder, fromClause, andOrCustomClauses);
            andOrCombinedFirstPredicate = criteriaBuilder.and(orPredicatesArray[0]);
            andOrCombinedSecondPredicate = criteriaBuilder.or(orPredicatesArray[1]);
        }

        /*  if (andCombinedPredicate != null && orCombinedPredicate != null && andOrCombinedFirstPredicate != null
                  && andOrCombinedSecondPredicate != null) {
              criteriaQuery.where(andCombinedPredicate, orCombinedPredicate,
                      criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
          } else if (andCombinedPredicate != null && orCombinedPredicate != null) {
              criteriaQuery.where(andCombinedPredicate, orCombinedPredicate);
          } else if (andCombinedPredicate == null) {
              criteriaQuery.where(orCombinedPredicate);
          } else if (orCombinedPredicate == null) {
              criteriaQuery.where(andCombinedPredicate);
          }*/

        if (null != andCombinedPredicate && null != orCombinedPredicate && null != andOrCombinedFirstPredicate
                && null != andOrCombinedSecondPredicate) {
            criteriaQuery.where(andCombinedPredicate, orCombinedPredicate,
                    criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
        } else if (null == andCombinedPredicate && null != orCombinedPredicate && null != andOrCombinedFirstPredicate
                && null != andOrCombinedSecondPredicate) {
            criteriaQuery.where(orCombinedPredicate,
                    criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
        } else if (null == orCombinedPredicate && null != andCombinedPredicate && null != andOrCombinedFirstPredicate
                && null != andOrCombinedSecondPredicate) {
            criteriaQuery.where(andCombinedPredicate,
                    criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
        } else if (null != andCombinedPredicate && null != orCombinedPredicate && null == andOrCombinedFirstPredicate
                && null == andOrCombinedSecondPredicate) {
            criteriaQuery.where(andCombinedPredicate, orCombinedPredicate);
        } else if (null != andCombinedPredicate && null == orCombinedPredicate && null == andOrCombinedFirstPredicate
                && null == andOrCombinedSecondPredicate) {
            criteriaQuery.where(andCombinedPredicate);
        } else if (null == andCombinedPredicate && null != orCombinedPredicate && null == andOrCombinedFirstPredicate
                && null == andOrCombinedSecondPredicate) {
            criteriaQuery.where(orCombinedPredicate);
        } else if (null == andCombinedPredicate && null == orCombinedPredicate && null != andOrCombinedFirstPredicate
                && null != andOrCombinedSecondPredicate) {
            criteriaQuery.where(criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
        }
        String sortableField = EntityUtil.getSortableField(queryClazz);
        if (sortableField != null && !("".equals(sortableField))) {
            criteriaQuery.orderBy(criteriaBuilder.asc(fromClause.get(sortableField)));
        }

        TypedQuery<T> query = em.createQuery(criteriaQuery);
        if (startIndex != null && pageSize != null) {
            query.setFirstResult(startIndex);
            query.setMaxResults(pageSize);
        }
        addAllHintsIntoQuery(query);
        return query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public List<Long> executeQueriesForIdsOnly(EntityManager em, Integer startIndex, Integer pageSize) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        criteriaQuery.distinct(true);
        Root<T> fromClause = criteriaQuery.from((Class<T>) queryClazz);
        criteriaQuery.select(fromClause.<Long>get("id"));

        Predicate andCombinedPredicate = null;
        if (andCustomClauses != null) {
            Predicate[] andPredicatesArray = createPredicateArray(em, criteriaBuilder, fromClause, andCustomClauses);
            andCombinedPredicate = criteriaBuilder.and(andPredicatesArray);
        }
        Predicate orCombinedPredicate = null;
        if (orCustomClauses != null) {
            Predicate[] orPredicatesArray = createPredicateArray(em, criteriaBuilder, fromClause, orCustomClauses);
            orCombinedPredicate = criteriaBuilder.or(orPredicatesArray);
        }

        /*
         * And + Or Grouped Custom Clause
         * e.g. where col1 = 'xyz' and (col2 = 'aa' or col3 = 'bb')  <------Grouped Condition
         */
        Predicate andOrCombinedFirstPredicate = null;
        Predicate andOrCombinedSecondPredicate = null;
        if (andOrCustomClauses != null) {
            Predicate[] orPredicatesArray = createPredicateArray(em, criteriaBuilder, fromClause, andOrCustomClauses);
            andOrCombinedFirstPredicate = criteriaBuilder.and(orPredicatesArray[0]);
            andOrCombinedSecondPredicate = criteriaBuilder.or(orPredicatesArray[1]);
        }

        /*  if (andCombinedPredicate != null && orCombinedPredicate != null && andOrCombinedFirstPredicate != null
                  && andOrCombinedSecondPredicate != null) {
              criteriaQuery.where(andCombinedPredicate, orCombinedPredicate,
                      criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
          } else if (andCombinedPredicate != null && orCombinedPredicate != null) {
              criteriaQuery.where(andCombinedPredicate, orCombinedPredicate);
          } else if (andCombinedPredicate == null) {
              criteriaQuery.where(orCombinedPredicate);
          } else if (orCombinedPredicate == null) {
              criteriaQuery.where(andCombinedPredicate);
          }*/

        if (null != andCombinedPredicate && null != orCombinedPredicate && null != andOrCombinedFirstPredicate
                && null != andOrCombinedSecondPredicate) {
            criteriaQuery.where(andCombinedPredicate, orCombinedPredicate,
                    criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
        } else if (null == andCombinedPredicate && null != orCombinedPredicate && null != andOrCombinedFirstPredicate
                && null != andOrCombinedSecondPredicate) {
            criteriaQuery.where(orCombinedPredicate,
                    criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
        } else if (null == orCombinedPredicate && null != andCombinedPredicate && null != andOrCombinedFirstPredicate
                && null != andOrCombinedSecondPredicate) {
            criteriaQuery.where(andCombinedPredicate,
                    criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
        } else if (null != andCombinedPredicate && null != orCombinedPredicate && null == andOrCombinedFirstPredicate
                && null == andOrCombinedSecondPredicate) {
            criteriaQuery.where(andCombinedPredicate, orCombinedPredicate);
        } else if (null != andCombinedPredicate && null == orCombinedPredicate && null == andOrCombinedFirstPredicate
                && null == andOrCombinedSecondPredicate) {
            criteriaQuery.where(andCombinedPredicate);
        } else if (null == andCombinedPredicate && null != orCombinedPredicate && null == andOrCombinedFirstPredicate
                && null == andOrCombinedSecondPredicate) {
            criteriaQuery.where(orCombinedPredicate);
        } else if (null == andCombinedPredicate && null == orCombinedPredicate && null != andOrCombinedFirstPredicate
                && null != andOrCombinedSecondPredicate) {
            criteriaQuery.where(criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
        }
        String sortableField = EntityUtil.getSortableField(queryClazz);
        if (sortableField != null && !("".equals(sortableField))) {
            criteriaQuery.orderBy(criteriaBuilder.asc(fromClause.get(sortableField)));
        }

        TypedQuery<Long> query = em.createQuery(criteriaQuery);
        if (startIndex != null && pageSize != null) {
            query.setFirstResult(startIndex);
            query.setMaxResults(pageSize);
        }
        addAllHintsIntoQuery(query);
        return query.getResultList();
    }

    @Override
    public Long executeTotalRowsQuery(EntityManager em) {
        return null;
    }

    private Predicate[] createPredicateArray(EntityManager em, CriteriaBuilder criteriaBuilder, Root<T> fromClause,
            List<KeyOptrValueHolder> customClauses) {

        Metamodel metamodel = em.getMetamodel();
     

        List<Predicate> predicateList = new ArrayList<Predicate>();
        for (KeyOptrValueHolder andcl : customClauses) {
        	ManagedType<?> managedType = metamodel.managedType(queryClazz);
            StringTokenizer tokenizer = new StringTokenizer(andcl.key, ".");
            Predicate valuePredicate = null;
            Join ipath = null;
            Path path = null;
            int count = 0;
            int maxtokenCount = tokenizer.countTokens() - 1;
            if (maxtokenCount > 0) {
                while (tokenizer.hasMoreElements()) {
                    String token = tokenizer.nextToken();
                    if (count == 0) {
                        Attribute<?, ?> attribute = managedType.getAttribute(token);
                        if (!(isEmbeddableAttributeOrBasic(attribute))) {
                            ipath = fromClause.join(token, JoinType.LEFT);
                        } else {
                            // create complete token.
                            String completetoken = token;
                            path = fromClause.get(completetoken);
                            while (tokenizer.hasMoreTokens()) {
                                path = path.get(tokenizer.nextToken());
                            }
                        }
                    }
                    if (count > 0 && count < maxtokenCount) {
                    	  managedType = metamodel.managedType(ipath.getJavaType());
                    	  Attribute<?, ?> attribute = managedType.getAttribute(token);
                        if (!(isEmbeddableAttributeOrBasic(attribute))) {
                            ipath = ipath.join(token, JoinType.LEFT);
                        }else{
                        	// create complete token.
                            String completetoken = token;
                            path =ipath.get(completetoken);
                            while (tokenizer.hasMoreTokens()) {
                                path = path.get(tokenizer.nextToken());
                            }
                        }
                    } else if (count == maxtokenCount) {
                        path = ipath.get(token);
                    }
                    count++;
                }
            } else {                // no join required
                String token = tokenizer.nextToken();
                path = fromClause.get(token);
            }
            Integer operator = andcl.operator;
            Object toBeSearchedObject = andcl.value;
            if (operator.equals(LIKE_OPERATOR)) {
                valuePredicate = criteriaBuilder.like(criteriaBuilder.upper(path), "%"
                        + toBeSearchedObject.toString().toUpperCase() + "%");

                predicateList.add(valuePredicate);
            } else if (operator.equals(LIKE_OPERATOR_WITH_ESCAPE)) {
                valuePredicate = criteriaBuilder.like(criteriaBuilder.upper(path), "%"
                        + toBeSearchedObject.toString().toUpperCase() + "%", '!');

                predicateList.add(valuePredicate);
            } else if (operator.equals(EQUALS_OPERATOR)) {
                if (toBeSearchedObject != null) {
                    valuePredicate = criteriaBuilder.equal(path, toBeSearchedObject);
                } else {
                    valuePredicate = criteriaBuilder.isNull(path);
                }
                predicateList.add(valuePredicate);
            } else if (operator.equals(NOT_EQUALS_OPERATOR)) {
                if (toBeSearchedObject != null) {
                    valuePredicate = criteriaBuilder.equal(path, toBeSearchedObject).not();
                } else {
                    valuePredicate = criteriaBuilder.isNull(path).not();
                }
                predicateList.add(valuePredicate);
            } else if (operator.equals(IN_OPERATOR)) {
                In<Object> in = criteriaBuilder.in(path);
                Collection<Object> oList = (Collection<Object>) toBeSearchedObject;
                for (Object o : oList) {
                    in.value(o);
                }
                predicateList.add(in);
            } else if (operator.equals(NOT_IN_OPERATOR)) {
                Collection<Object> oList = (Collection<Object>) toBeSearchedObject;
                predicateList.add(criteriaBuilder.not(path.in(oList)));
            } else if (operator.equals(BETWEEN_OPERATOR)) {
                List<Object> oList = (List<Object>) toBeSearchedObject;
                if (oList.size() != 2) {
                    throw new SystemException("The size of the collection while using between operator must be two.");
                }
                valuePredicate = criteriaBuilder.between(path, (Comparable) oList.get(0), (Comparable) oList.get(1));
                predicateList.add(valuePredicate);
            } else if (operator.equals(GREATER_THAN_OPERATOR)) {
                if (toBeSearchedObject != null) {
                    Comparable comparable = (Comparable) toBeSearchedObject; // ain't a genius ?
                    valuePredicate = criteriaBuilder.greaterThan(path, comparable);
                } else {
                    valuePredicate = criteriaBuilder.isNull(path);
                }
                predicateList.add(valuePredicate);
            } else if (operator.equals(GREATER_THAN_EQUAL_TO_OPERATOR)) {
                Comparable comparable = (Comparable) toBeSearchedObject;
                if (toBeSearchedObject != null) {
                    valuePredicate = criteriaBuilder.greaterThanOrEqualTo(path, comparable);
                } else {
                    valuePredicate = criteriaBuilder.isNull(path);
                }
                predicateList.add(valuePredicate);
            } else if (operator.equals(LESS_THAN_OPERATOR)) {
                Comparable comparable = (Comparable) toBeSearchedObject;
                if (toBeSearchedObject != null) {
                    valuePredicate = criteriaBuilder.lessThan(path, comparable);
                } else {
                    valuePredicate = criteriaBuilder.isNull(path);
                }
                predicateList.add(valuePredicate);
            } else if (operator.equals(LESS_THAN_EQUAL_TO_OPERATOR)) {
                Comparable comparable = (Comparable) toBeSearchedObject;
                if (toBeSearchedObject != null) {
                    valuePredicate = criteriaBuilder.lessThanOrEqualTo(path, comparable);
                } else {
                    valuePredicate = criteriaBuilder.isNull(path);
                }
                predicateList.add(valuePredicate);
            }
        }
        Predicate[] predicateArray = new Predicate[predicateList.size()];
        predicateArray = predicateList.toArray(predicateArray);
        return predicateArray;
    }

    private boolean isEmbeddableAttributeOrBasic(Attribute<?, ?> attribute) {
        boolean embeddableAttribte = false;
        Attribute.PersistentAttributeType attributeType = attribute.getPersistentAttributeType();
        if (attributeType.equals(Attribute.PersistentAttributeType.EMBEDDED)
                || attributeType.equals(Attribute.PersistentAttributeType.BASIC)) {
            embeddableAttribte = true;
        }
        return embeddableAttribte;
    }

    protected void addAllHintsIntoQuery(Query query) {
        for (Entry<String, Object> parameter : queryHints.entrySet()) {
            query.setHint(parameter.getKey(), parameter.getValue());
        }
    }
}
