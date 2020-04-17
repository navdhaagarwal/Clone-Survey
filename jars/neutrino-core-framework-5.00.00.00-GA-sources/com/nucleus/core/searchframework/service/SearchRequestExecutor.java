package com.nucleus.core.searchframework.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.searchframework.entity.ConstantSearchAttribute;
import com.nucleus.core.searchframework.entity.ObjectGraphSearchAttribute;
import com.nucleus.core.searchframework.entity.SearchAttribute;
import com.nucleus.core.searchframework.entity.SearchAttributeExpression;
import com.nucleus.core.searchframework.entity.SearchRequest;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.QueryExecutor;

public class SearchRequestExecutor<T> implements QueryExecutor<T> {

    private final SearchRequest   searchRequest;
    protected Map<String, Object> queryHints = new LinkedHashMap<String, Object>();

    public SearchRequestExecutor(SearchRequest searchRequest) {
        this.searchRequest = searchRequest;
    }

    @Override
    /**
     * Method to create Criteria Query from SearchRequest Object.
     * This method build the SELECT Clause,WHERE Clause,JOINS, Aliases
     * and get the result by making and executing the query. 
     */
    public List executeQuery(EntityManager em, Integer startIndex, Integer pageSize) {
        try {

            // Get class name of FROM table
            Class className = Class.forName(searchRequest.getSearchOn());
            List<String> selectFieldList = searchRequest.getFieldList();
            List<String> groupByColumnList = searchRequest.getGroupByList();

            List<Selection<?>> multiSelectList = new ArrayList<Selection<?>>();
            List<Expression<?>> groupByColumnsList = new ArrayList<Expression<?>>();
            Map<String, Join> joinsMap = new LinkedHashMap<String, Join>();

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
            Root<T> fromClause = criteriaQuery.from(className);

            // WHERE CLAUSE
            if (searchRequest.getWhereClause().getSearchAttributeExpression() != null) {
                Predicate wherePredicate = (Predicate) buildCriteriaForSAExpression(searchRequest.getWhereClause()
                        .getSearchAttributeExpression(), em, criteriaBuilder, criteriaQuery, fromClause, joinsMap, className);
                criteriaQuery.where(wherePredicate);
            }

            // SELECT Clause
            if (selectFieldList != null && selectFieldList.size() > 0) {
                for (String str : selectFieldList) {
                    multiSelectList.add(getPathFromOgnl(str, em, fromClause, className, joinsMap));
                }
                criteriaQuery.multiselect(multiSelectList);
            } else {
                criteriaQuery.multiselect(fromClause);
            }

            // Group By Clause
            if (groupByColumnList != null && groupByColumnList.size() > 0) {
                for (String str : groupByColumnList) {
                    groupByColumnsList.add((Expression) getPathFromOgnl(str, em, fromClause, className, joinsMap));
                }
                criteriaQuery.groupBy(groupByColumnsList);
            }

            // add query hints if any
            TypedQuery<Object[]> query = em.createQuery(criteriaQuery);
            addAllHintsIntoQuery(query);

            // RESULT
            List<Object[]> data = query.getResultList();
            List<Map<String, Object>> finalList = new ArrayList<Map<String, Object>>();
            if (selectFieldList != null && selectFieldList.size() > 0) {
                // If only 1 select column is there
                if (selectFieldList.size() == 1) {
                    for (int p = 0 ; p < data.size() ; p++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put(selectFieldList.get(0), data.get(p));
                        finalList.add(map);
                    }
                } else {
                    // For Multiple select columns
                    for (int p = 0 ; p < data.size() ; p++) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        List selectedFieldData = new ArrayList();
                        for (Object obj : data.get(p)) {
                            selectedFieldData.add(obj);
                        }
                        for (int e = 0 ; e < selectFieldList.size() ; e++) {
                            map.put(selectFieldList.get(e), selectedFieldData.get(e));
                        }
                        finalList.add(map);
                    }
                }
                return finalList;
            } else {
                return data;
            }
        } catch (ClassNotFoundException cnfException) {
            throw new SearchException("Cannot find the class " + searchRequest.getSearchOn(), cnfException);
        }
    }

    /**
     * Make Path For Select Column Names
     * from there ognl
     * 
     * @param columnOGNL
     * @param em
     * @param fromClause
     * @param className
     * @return
     */
    private Selection getPathFromOgnl(String columnOGNL, EntityManager em, Root<T> fromClause, Class className,
            Map<String, Join> joinsMap) {

        Metamodel metamodel = em.getMetamodel();
        ManagedType<?> managedType = metamodel.managedType(className);
        Attribute<?, ?> attribute;
        String[] tokens = columnOGNL.split("\\.");
        Join join = null;
        Path path = null;
        String finalStr = "";
        String alias = "";
        for (int i = 0 ; i < tokens.length ; i++) {
            String token = tokens[i];
            attribute = managedType.getAttribute(token);
            if (isEmbeddableAttributeOrBasic(attribute)) {
                if (join == null) {
                    path = fromClause.get(token);
                    if (i < tokens.length - 1) {
                        path = path.get(tokens[tokens.length - 1]);
                        i++;
                    }
                    return path;
                }
                path = join.get(token);
                if (i < tokens.length - 1) {
                    path = path.get(tokens[tokens.length - 1]);
                    i++;
                }
                return path;
            } else {
                // For First Ognl,Load from FROM Table
                if (i == 0) {
                    alias = "joined_" + token;
                    finalStr = token;
                    if (!joinsMap.containsKey(alias)) {
                        join = fromClause.join(token, JoinType.LEFT);
                        join.alias(alias);
                        joinsMap.put(alias, join);
                    }
                    join = joinsMap.get(alias);
                    // join = fromClause.join(token);
                } else { // For rest Of the OGNL
                    alias = alias + "_" + token;
                    finalStr = finalStr + "." + token;
                    if (!joinsMap.containsKey(alias)) {
                        join = join.join(token, JoinType.LEFT);
                        join.alias(alias);
                        joinsMap.put(alias, join);
                    } else {
                        join = joinsMap.get(alias);
                    }
                    // join = join.join(token);
                }
                // Load Class Of Each OGNL Based on its type, to find the next ognl in that class
                if (attribute.isCollection()) {
                    managedType = metamodel.managedType((((PluralAttribute) attribute).getElementType()).getJavaType());
                } else {
                    managedType = metamodel.managedType(attribute.getJavaType());
                }
            }
        }
        return null;
    }

    @Override
    public Long executeTotalRowsQuery(EntityManager em) {
        return null;
    }

    /**
     * Build left expression and right expression to
     * evaluate in predicate
     * @param searchAttributeExpression
     * @param emf
     * @param criteriaBuilder
     * @param criteriaQuery
     * @param fromClause
     * @param joinsMap
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    private Expression buildCriteriaForSAExpression(SearchAttributeExpression searchAttributeExpression, EntityManager emf,
            CriteriaBuilder criteriaBuilder, CriteriaQuery<Object[]> criteriaQuery, Root<T> fromClause,
            Map<String, Join> joinsMap, Class className) throws ClassNotFoundException {

        if (isLeafNode(searchAttributeExpression)) {
            return getSearchAttributeValue(criteriaBuilder, criteriaQuery, searchAttributeExpression.getSearchAttribute(),
                    emf, fromClause, joinsMap, className);
        }
        Object leftExpression = buildCriteriaForSAExpression(
                (SearchAttributeExpression) searchAttributeExpression.getLeftExpression(), emf, criteriaBuilder,
                criteriaQuery, fromClause, joinsMap, className);
        Object rightExpression = buildCriteriaForSAExpression(
                (SearchAttributeExpression) searchAttributeExpression.getRightExpression(), emf, criteriaBuilder,
                criteriaQuery, fromClause, joinsMap, className);
        return getPredicate(searchAttributeExpression.getOperator(), leftExpression, rightExpression, criteriaBuilder,
                fromClause, emf);

    }

    /**
     * Check type of expression of left and right expression
     * and reurn its value
     * @param criteriaBuilder
     * @param searchAttribute
     * @param emf
     * @param fromClause
     * @param joinsMap
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    private Expression getSearchAttributeValue(CriteriaBuilder criteriaBuilder, CriteriaQuery<Object[]> criteriaQuery,
            SearchAttribute searchAttribute, EntityManager emf, Root<T> fromClause, Map<String, Join> joinsMap,
            Class className) throws ClassNotFoundException {

        if (searchAttribute instanceof ConstantSearchAttribute) {
            return criteriaBuilder.literal(((ConstantSearchAttribute) searchAttribute).getLiteralValue());
        }
        if (searchAttribute instanceof ObjectGraphSearchAttribute) {
            return getPath(((ObjectGraphSearchAttribute) searchAttribute).getObjectGraph(), emf, fromClause, joinsMap,
                    className, criteriaQuery);
        }
        /*if (searchAttribute instanceof QuerySearchAttribute) {
            // return getSubQuery((QuerySearchAttribute) searchAttribute, criteriaBuilder, criteriaQuery, emf);
        }*/
        return null;
    }

    /**
     * Create path and join with the ognl recieved
     * i.e traverse to the column in which data is to be searched
     * making joins from base table
     * @param obj
     * @param emf
     * @param fromClause
     * @param joinsMap
     * @param className
     * @return
     */
    public Path getPath(String obj, EntityManager emf, Root<T> fromClause, Map<String, Join> joinsMap, Class className,
            CriteriaQuery<Object[]> criteriaQuery) {
        String finalStr = "";
        Metamodel metamodel = emf.getMetamodel();
        ManagedType<?> managedType = metamodel.managedType(className);
        Attribute<?, ?> attribute;
        String[] tokens = obj.split("\\.");
        Join join = null;
        Path path = null;
        String alias = "";
        if (tokens == null || tokens.length == 0) {
            return null;
        }
        for (int i = 0 ; i < tokens.length ; i++) {
            String token = tokens[i];
            attribute = managedType.getAttribute(token);
            // For Last OGNL
            if (isEmbeddableAttributeOrBasic(attribute)) {
                if (join == null) {
                    path = fromClause.get(token);
                    if (i < tokens.length - 1) {
                        path = path.get(tokens[tokens.length - 1]);
                        i++;
                    }
                    return path;
                }
                path = join.get(token);
                if (i < tokens.length - 1) {
                    path = path.get(tokens[tokens.length - 1]);
                    i++;
                }
                return path;
            } else {
                // For First Ognl,Load from FROM Table
                if (i == 0) {
                    alias = "joined_" + token;
                    finalStr = token;
                    if (!joinsMap.containsKey(alias)) {
                        join = fromClause.join(token, JoinType.LEFT);
                        join.alias(alias);
                        joinsMap.put(alias, join);
                    }
                    join = joinsMap.get(alias);
                } else { // For rest Of the OGNL
                    alias = alias + "_" + token;
                    finalStr = finalStr + "." + token;
                    if (!joinsMap.containsKey(alias)) {
                        join = join.join(token, JoinType.LEFT);
                        join.alias(alias);
                        joinsMap.put(alias, join);
                    } else {
                        join = joinsMap.get(alias);
                    }
                }
                // Load Class Of Each OGNL Based on its type, to find the next ognl in that class
                if (attribute.isCollection()) {
                    managedType = metamodel.managedType((((PluralAttribute) attribute).getElementType()).getJavaType());
                } else {
                    managedType = metamodel.managedType(attribute.getJavaType());
                }
            }
        }
        return null;
    }

    /**
     * Create predicate with left,right expressions and operator
     * @param operator
     * @param leftExp
     * @param rightExp
     * @param criteriaBuilder
     * @param fromClause
     * @param emf
     * @return
     */
    @SuppressWarnings("unchecked")
    private Predicate getPredicate(String operator, Object leftExp, Object rightExp, CriteriaBuilder criteriaBuilder,
            Root<T> fromClause, EntityManager emf) {

        if (operator.equalsIgnoreCase("AND")) {
            return criteriaBuilder.and((Predicate) leftExp, (Predicate) rightExp);
        } else if (operator.equalsIgnoreCase("OR")) {
            return criteriaBuilder.or((Predicate) leftExp, (Predicate) rightExp);
        } else if (operator.equals("=")) {
            return criteriaBuilder.equal(criteriaBuilder.upper((Expression) leftExp), criteriaBuilder.upper((Expression)rightExp) );
        } else if (operator.equals("!=")) {
            return criteriaBuilder.notEqual((Expression) leftExp, (Expression) rightExp);
        } else if (operator.equals(">")) {
            return criteriaBuilder.greaterThan((Expression) leftExp, (Expression) rightExp);
        } else if (operator.equals(">=")) {
            return criteriaBuilder.greaterThanOrEqualTo((Expression) leftExp, (Expression) rightExp);
        } else if (operator.equals("<")) {
            return criteriaBuilder.lessThan((Expression) leftExp, (Expression) rightExp);
        } else if (operator.equals("<=")) {
            return criteriaBuilder.lessThanOrEqualTo((Expression) leftExp, (Expression) rightExp);
        } else if (operator.equals("like")) {
            return criteriaBuilder.like(criteriaBuilder.upper((Expression) leftExp), criteriaBuilder.upper((Expression) rightExp));
        } else if (operator.equals("in")) {
            return criteriaBuilder.in((Expression) leftExp).value((Expression) rightExp);
        } else if (operator.equals("isNull")) {
            return criteriaBuilder.isNull((Expression) leftExp);
        }
        return null;
    }

    /**
     * Check For Leaf Node
     * @param exp
     * @return
     */
    protected boolean isLeafNode(SearchAttributeExpression exp) {
        if (exp.getLeftExpression() == null && exp.getRightExpression() == null)
            return true;
        else
            return false;
    }

    /**
     * Check whether the entity is basic/embeddable
     * @param attribute
     * @return
     */
    private boolean isEmbeddableAttributeOrBasic(Attribute<?, ?> attribute) {
        boolean embeddableAttribte = false;
        Attribute.PersistentAttributeType attributeType = attribute.getPersistentAttributeType();
        if (attributeType.equals(Attribute.PersistentAttributeType.EMBEDDED)
                || attributeType.equals(Attribute.PersistentAttributeType.BASIC)) {
            embeddableAttribte = true;
        }
        return embeddableAttribte;
    }

    public SearchRequestExecutor<T> addQueryHint(String queryHintName, Object queryHintValue) {
        NeutrinoValidator.isTrue(StringUtils.isNotBlank(queryHintName), "Query Hint name cannot be blank");
        NeutrinoValidator.notNull(queryHintValue, "Query Hint value cannot be null");
        queryHints.put(queryHintName.intern(), queryHintValue);
        return this;
    }

    protected void addAllHintsIntoQuery(Query query) {
        for (Entry<String, Object> parameter : queryHints.entrySet()) {
            query.setHint(parameter.getKey(), parameter.getValue());
        }
    }

}
