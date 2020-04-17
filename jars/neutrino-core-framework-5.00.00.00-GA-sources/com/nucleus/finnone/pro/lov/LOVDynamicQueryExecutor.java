package com.nucleus.finnone.pro.lov;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.QueryExecutor;
import com.nucleus.entity.Entity;
import com.nucleus.logging.BaseLoggers;

public class LOVDynamicQueryExecutor<T> implements QueryExecutor<T>
{
  public static final Integer IN_OPERATOR = Integer.valueOf(3);
  public static final Integer LIKE_OPERATOR = Integer.valueOf(2);
  public static final Integer EQUALS_OPERATOR = Integer.valueOf(1);
  public static final Integer BETWEEN_OPERATOR = Integer.valueOf(4);
  public static final Integer NOT_IN_OPERATOR = Integer.valueOf(5);
  public static final Integer LESS_THAN_OPERATOR = Integer.valueOf(6);
  public static final Integer LESS_THAN_EQUAL_TO_OPERATOR = Integer.valueOf(7);
  public static final Integer GREATER_THAN_OPERATOR = Integer.valueOf(8);
  public static final Integer GREATER_THAN_EQUAL_TO_OPERATOR = Integer.valueOf(9);
  private Class<? extends Entity> queryClazz;
  private List<LOVKeyOptrValueHolder> andCustomClauses;
  private List<LOVKeyOptrValueHolder> orCustomClauses;
  private List<LOVKeyOptrValueHolder> andOrCustomClauses;
  private List<String> orderByCustomClauses;
  private final Map<String, String> selectedProperties;
  protected Map<String, Object> queryHints;

  public LOVDynamicQueryExecutor(Class<? extends Entity> queryClass)
  {
    this(queryClass, null);
  }

  public LOVDynamicQueryExecutor(Class<? extends Entity> queryClass, String entityAlias)
  {
    this.selectedProperties = new LinkedHashMap();
    this.queryHints = new LinkedHashMap();

    Validate.notNull(queryClass, "Class for execution of query cannot be null", new Object[0]);
    this.queryClazz = queryClass;
  }

  public LOVDynamicQueryExecutor<?> addQueryColumns(String[] columnNames) {

    int len = columnNames.length; 
    for (int i$ = 0; i$ < len; ++i$) {
      String columnName = columnNames[i$];
      this.selectedProperties.put(columnName, columnName);
    }
    return this;
  }

  public LOVDynamicQueryExecutor<?> addColumn(String ognl, String alias) {
    this.selectedProperties.put(ognl, alias);
    return this;
  }

  public LOVDynamicQueryExecutor<?> addAndClause(String fieldName, Integer operator, Object o) {
    if (this.andCustomClauses == null)
      this.andCustomClauses = new LinkedList();

    HashMap operatorValueMap = new HashMap();
    operatorValueMap.put(operator, o);
    this.andCustomClauses.add(new LOVKeyOptrValueHolder(fieldName.intern(), operator, o));
    return this;
  }

  public LOVDynamicQueryExecutor<?> addAndOrClause(String fieldName, Integer operator, Object o) {
    if (this.andOrCustomClauses == null)
      this.andOrCustomClauses = new LinkedList();

    HashMap operatorValueMap = new HashMap();
    operatorValueMap.put(operator, o);
    this.andOrCustomClauses.add(new LOVKeyOptrValueHolder(fieldName.intern(), operator, o));
    return this;
  }
  
  public LOVDynamicQueryExecutor<?> addOrderByClause(String fieldName) {
	    if (this.orderByCustomClauses == null)
	      this.orderByCustomClauses = new LinkedList<String>();

	    this.orderByCustomClauses.add(fieldName);
	    return this;
  }

  public LOVDynamicQueryExecutor<?> addOrClause(String fieldName, Integer operator, Object o) {
    if (this.orCustomClauses == null)
      this.orCustomClauses = new LinkedList();

    HashMap operatorValueMap = new HashMap();
    operatorValueMap.put(operator, o);
    Object prevValue = Boolean.valueOf(this.orCustomClauses.add(new LOVKeyOptrValueHolder(fieldName.intern(), operator, o)));
    if (prevValue != null) {
      BaseLoggers.persistenceLogger.debug("The query coantains more than one reference of " + fieldName + " in where clause");
    }

    return this;
  }

  public LOVDynamicQueryExecutor<T> addQueryHint(String queryHintName, Object queryHintValue) {
    NeutrinoValidator.isTrue(StringUtils.isNotBlank(queryHintName), "Query Hint name cannot be blank");
    NeutrinoValidator.notNull(queryHintValue, "Query Hint value cannot be null");
    this.queryHints.put(queryHintName.intern(), queryHintValue);
    return this;
  }

  @Override
  public List<T> executeQuery(EntityManager em, Integer startIndex, Integer pageSize)
  {
    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
    CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(this.queryClazz);
    Root fromClause = criteriaQuery.from(this.queryClazz);
    criteriaQuery.select(fromClause);

    Predicate andCombinedPredicate = null;
    if (this.andCustomClauses != null) {
      Predicate[] andPredicatesArray = createPredicateArray(em, criteriaBuilder, fromClause, this.andCustomClauses);
      andCombinedPredicate = criteriaBuilder.and(andPredicatesArray);
    }
    Predicate orCombinedPredicate = null;
    if (this.orCustomClauses != null) {
      Predicate[] orPredicatesArray = createPredicateArray(em, criteriaBuilder, fromClause, this.orCustomClauses);
      orCombinedPredicate = criteriaBuilder.or(orPredicatesArray);
    }

    Predicate andOrCombinedFirstPredicate = null;
    Predicate andOrCombinedSecondPredicate = null;
    if (this.andOrCustomClauses != null) {
      Predicate[] orPredicatesArray = createPredicateArray(em, criteriaBuilder, fromClause, this.andOrCustomClauses);
      andOrCombinedFirstPredicate = criteriaBuilder.and(new Predicate[] { orPredicatesArray[0] });
      andOrCombinedSecondPredicate = criteriaBuilder.or(new Predicate[] { orPredicatesArray[1] });
    }

    if ((null != andCombinedPredicate) && (null != orCombinedPredicate) && (null != andOrCombinedFirstPredicate) && (null != andOrCombinedSecondPredicate))
    {
      criteriaQuery.where(new Predicate[] { andCombinedPredicate, orCombinedPredicate, criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate) });
    }
    else if ((null == andCombinedPredicate) && (null != orCombinedPredicate) && (null != andOrCombinedFirstPredicate) && (null != andOrCombinedSecondPredicate))
    {
      criteriaQuery.where(new Predicate[] { orCombinedPredicate, criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate) });
    }
    else if ((null == orCombinedPredicate) && (null != andCombinedPredicate) && (null != andOrCombinedFirstPredicate) && (null != andOrCombinedSecondPredicate))
    {
      criteriaQuery.where(new Predicate[] { andCombinedPredicate, criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate) });
    }
    else if ((null != andCombinedPredicate) && (null != orCombinedPredicate) && (null == andOrCombinedFirstPredicate) && (null == andOrCombinedSecondPredicate))
    {
      criteriaQuery.where(new Predicate[] { andCombinedPredicate, orCombinedPredicate });
    } else if ((null != andCombinedPredicate) && (null == orCombinedPredicate) && (null == andOrCombinedFirstPredicate) && (null == andOrCombinedSecondPredicate))
    {
      criteriaQuery.where(andCombinedPredicate);
    } else if ((null == andCombinedPredicate) && (null != orCombinedPredicate) && (null == andOrCombinedFirstPredicate) && (null == andOrCombinedSecondPredicate))
    {
      criteriaQuery.where(orCombinedPredicate);
    } else if ((null == andCombinedPredicate) && (null == orCombinedPredicate) && (null != andOrCombinedFirstPredicate) && (null != andOrCombinedSecondPredicate))
    {
      criteriaQuery.where(criteriaBuilder.or(andOrCombinedFirstPredicate, andOrCombinedSecondPredicate));
    }
   
    List<Order> orderList = new ArrayList<Order>();
    if(hasElements(this.orderByCustomClauses)){
    	for(String element: this.orderByCustomClauses){
        	orderList.add(criteriaBuilder.asc(fromClause.get(element)));
        }
        criteriaQuery.orderBy(orderList);
    }
    
    TypedQuery query = em.createQuery(criteriaQuery);
    query.setFirstResult(startIndex);
    query.setMaxResults(pageSize);
    addAllHintsIntoQuery(query);
    return query.getResultList();
  }

  @Override
  public Long executeTotalRowsQuery(EntityManager em)
  {
    return null;
  }

  private Predicate[] createPredicateArray(EntityManager em, CriteriaBuilder criteriaBuilder, Root<T> fromClause, List<LOVKeyOptrValueHolder> customClauses)
  {
    Metamodel metamodel = em.getMetamodel();
    ManagedType managedType = metamodel.managedType(this.queryClazz);

    List predicateList = new ArrayList();
    Iterator itr = customClauses.iterator();
    while(itr.hasNext()) 
    { 
    	LOVKeyOptrValueHolder andcl = (LOVKeyOptrValueHolder)itr.next();
      StringTokenizer tokenizer = new StringTokenizer(andcl.key, ".");
      Predicate valuePredicate;
      Join ipath = null;
      Path path = null;
      int count = 0;
      int maxtokenCount = tokenizer.countTokens() - 1;
      if (maxtokenCount > 0) 
      {
          Attribute attribute;
          while(tokenizer.hasMoreElements())
          { 
          	String token = tokenizer.nextToken();
	            if (count == 0) 
	            {
	              attribute = managedType.getAttribute(token);
	              if (!(isEmbeddableAttributeOrBasic(attribute))) 
	              {
	                ipath = fromClause.join(token, JoinType.LEFT);
	              }
	              else 
	              {
	                String completetoken = token;
	                path = fromClause.get(completetoken);
	                while (tokenizer.hasMoreTokens())
	                  path = path.get(tokenizer.nextToken());
	              }
	            }
	
	            if ((count > 0) && (count < maxtokenCount)) 
	            {
	              attribute = ipath.getAttribute();
	              if (!(isEmbeddableAttributeOrBasic(attribute)))
	                ipath = ipath.join(token, JoinType.LEFT);
	            }
	            else if (count == maxtokenCount) 
	            {
	              path = ipath.get(token);
	            }
	            ++count;
          }
      }
      else
      {
    	   String token = tokenizer.nextToken();
    	   path = fromClause.get(token);
      }

      Integer operator = andcl.operator;
      Object toBeSearchedObject = andcl.value;
      if (operator.equals(LIKE_OPERATOR)) {
    	  String upperCaseString="";
    	  if(toBeSearchedObject!=null){
    		  upperCaseString=String.valueOf(toBeSearchedObject).toUpperCase();
    	  }
    	  
        valuePredicate = criteriaBuilder.like(criteriaBuilder.upper(path), "%" + upperCaseString + "%");
        predicateList.add(valuePredicate);
      } else if (operator.equals(EQUALS_OPERATOR)) {
        if (toBeSearchedObject != null)
          valuePredicate = criteriaBuilder.equal(path, toBeSearchedObject);
        else
          valuePredicate = criteriaBuilder.isNull(path);

        predicateList.add(valuePredicate);
      } else if (operator.equals(IN_OPERATOR)) {
        CriteriaBuilder.In in = criteriaBuilder.in(path);
        Collection oList = (Collection)toBeSearchedObject;
        for (Iterator i$ = oList.iterator(); i$.hasNext(); ) {
          Object o = i$.next();
          in.value(o);
        }
        predicateList.add(in);
      } else if (operator.equals(NOT_IN_OPERATOR)) {
        Collection oList = (Collection)toBeSearchedObject;
        predicateList.add(criteriaBuilder.not(path.in(oList)));
      } else if (operator.equals(BETWEEN_OPERATOR)) {
        List oList = (List)toBeSearchedObject;
        if (oList.size() != 2)
          throw new SystemException("The size of the collection while using between operator must be two.");

        valuePredicate = criteriaBuilder.between(path, (Comparable)oList.get(0), (Comparable)oList.get(1));
        predicateList.add(valuePredicate); } else {
        Comparable comparable;
        if (operator.equals(GREATER_THAN_OPERATOR)) {
          if (toBeSearchedObject != null) {
            comparable = (Comparable)toBeSearchedObject;
            valuePredicate = criteriaBuilder.greaterThan(path, comparable);
          } else {
            valuePredicate = criteriaBuilder.isNull(path);
          }
          predicateList.add(valuePredicate);
        } else if (operator.equals(GREATER_THAN_EQUAL_TO_OPERATOR)) {
          comparable = (Comparable)toBeSearchedObject;
          if (toBeSearchedObject != null)
            valuePredicate = criteriaBuilder.greaterThanOrEqualTo(path, comparable);
          else
            valuePredicate = criteriaBuilder.isNull(path);

          predicateList.add(valuePredicate);
        } else if (operator.equals(LESS_THAN_OPERATOR)) {
          comparable = (Comparable)toBeSearchedObject;
          if (toBeSearchedObject != null)
            valuePredicate = criteriaBuilder.lessThan(path, comparable);
          else
            valuePredicate = criteriaBuilder.isNull(path);

          predicateList.add(valuePredicate);
        } else if (operator.equals(LESS_THAN_EQUAL_TO_OPERATOR)) {
          comparable = (Comparable)toBeSearchedObject;
          if (toBeSearchedObject != null)
            valuePredicate = criteriaBuilder.lessThanOrEqualTo(path, comparable);
          else
            valuePredicate = criteriaBuilder.isNull(path);

          predicateList.add(valuePredicate); }
      }
    }
    Predicate[] predicateArray = new Predicate[predicateList.size()];
    predicateArray = (Predicate[])predicateList.toArray(predicateArray);
    return predicateArray;
  }

  private boolean isEmbeddableAttributeOrBasic(Attribute<?, ?> attribute) {
    boolean embeddableAttribte = false;
    Attribute.PersistentAttributeType attributeType = attribute.getPersistentAttributeType();
    if ((attributeType.equals(Attribute.PersistentAttributeType.EMBEDDED)) || (attributeType.equals(Attribute.PersistentAttributeType.BASIC)))
    {
      embeddableAttribte = true;
    }
    return embeddableAttribte;
  }

  protected void addAllHintsIntoQuery(Query query) {
    for (Iterator i$ = this.queryHints.entrySet().iterator(); i$.hasNext(); ) { 
      Map.Entry parameter = (Map.Entry)i$.next();
      query.setHint((String)parameter.getKey(), parameter.getValue());
    }
  }
}