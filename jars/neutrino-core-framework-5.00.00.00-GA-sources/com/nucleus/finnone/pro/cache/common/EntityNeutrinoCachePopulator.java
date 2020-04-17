package com.nucleus.finnone.pro.cache.common;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.inject.Named;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.cache.annotation.CompositePredicate;
import com.nucleus.finnone.pro.cache.annotation.CustomCache;
import com.nucleus.finnone.pro.cache.annotation.CustomCache.AndOr;
import com.nucleus.finnone.pro.cache.annotation.Predicate;
import com.nucleus.finnone.pro.cache.annotation.Predicate.Operator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

@Named("entityNeutrinoCachePopulator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityNeutrinoCachePopulator extends NeutrinoCachePopulator {

	private static final String FALLBACK_QUERY_ID = "SELECT e.id ";
	private static final String BUILD_QUERY_A = "SELECT new Map(e.";
	private static final String BUILD_QUERY_B = " as ";
	private static final String ENTITY = "ENTITY";
	private static final String BUILD_QUERY_OBJECT = ", e as " + ENTITY + ") ";
	private static final String FROM = " FROM ";
	private static final String ALIAS = " e ";
	private static final String WHERE_CLAUSE = " WHERE ";
	private static final String FALLBACK_QUERY_KEY = " = :KEY ";
	private static final String AND_CLAUSE = " AND ";
	private static final String OR_CLAUSE = " OR ";
	private static final String OPENING_BRACKET = " ( ";
	private static final String CLOSING_BRACKET = " ) ";
	private static final String ALIAS_DOT = " e.";
	private static final String EMPTY_STRING = "";
	private static final String KEY = "KEY";
	private static final String SPACE = " ";
	private static final String COMMA = " , ";
	private static Map<Operator, String> operatorToStringMap = new EnumMap<>(Operator.class);

	static {
		operatorToStringMap.put(Operator.IS_NULL, Predicate.IS_NULL);
		operatorToStringMap.put(Operator.IS_NOT_NULL, Predicate.IS_NOT_NULL);
		operatorToStringMap.put(Operator.EQUAL, Predicate.EQUAL);
		operatorToStringMap.put(Operator.NOT_EQUAL, Predicate.NOT_EQUAL);
		operatorToStringMap.put(Operator.IN, Predicate.IN);
		operatorToStringMap.put(Operator.NOT_IN, Predicate.NOT_IN);
		operatorToStringMap.put(Operator.AND, Predicate.AND);
		operatorToStringMap.put(Operator.OR, Predicate.OR);
		operatorToStringMap.put(Operator.LESS_THAN, Predicate.LESS_THAN);
		operatorToStringMap.put(Operator.LESS_THAN_EQUAL, Predicate.LESS_THAN_EQUAL);
		operatorToStringMap.put(Operator.GREATER_THAN, Predicate.GREATER_THAN);
		operatorToStringMap.put(Operator.GREATER_THAN_EQUAL, Predicate.GREATER_THAN_EQUAL);
	}

	@SuppressWarnings("rawtypes")
	private Class entityClass;
	private String cacheName;
	private String fieldName;
	private String regionName;
	private String groupName;
	private String fallBackQuery;
	private String buildQuery;
	private String andOrClause;

	@SuppressWarnings("rawtypes")
	protected Class getEntityClass() {
		return entityClass;
	}

	public String getFieldName() {
		return fieldName;
	}

	@SuppressWarnings("rawtypes")
	public void initConfig(Class entityClass, Field field, CustomCache customCache) {
		fieldName = field.getName();
		cacheName = customCache.name();
		regionName = customCache.regionName();
		groupName = customCache.groupName();
		this.entityClass = entityClass;
		StringBuilder fallbackStringBuilder = new StringBuilder();
		StringBuilder buildStringBuilder = new StringBuilder();
		buildStringBuilder.append(BUILD_QUERY_A).append(fieldName).append(BUILD_QUERY_B).append(KEY)
				.append(BUILD_QUERY_OBJECT).append(FROM).append(getEntityClass().getSimpleName()).append(ALIAS);

		initConfigInternal(customCache, fallbackStringBuilder);
		String clauseString = getQueryClause(customCache);

		fallbackStringBuilder.append(FROM).append(getEntityClass().getSimpleName()).append(ALIAS).append(WHERE_CLAUSE)
				.append(clauseString);
		if (!clauseString.equals(EMPTY_STRING)) {
			fallbackStringBuilder.append(AND_CLAUSE);
			buildStringBuilder.append(WHERE_CLAUSE).append(clauseString);
		}
		fallbackStringBuilder.append(ALIAS_DOT).append(fieldName).append(FALLBACK_QUERY_KEY);

		this.fallBackQuery = fallbackStringBuilder.toString();
		this.buildQuery = buildStringBuilder.toString();

		BaseLoggers.flowLogger.debug("CachePopulator Initalized for Field : " + fieldName);
		BaseLoggers.flowLogger.debug("Build Query : " + buildQuery);
		BaseLoggers.flowLogger.debug("FallBack Query : " + fallBackQuery);
	}

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : EntityNeutrinoCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(KEY, key);
		if (isIdBasedCacheableEntity()) {
			return getDao().getEntityIdByQuery(fallBackQuery, parameterMap);
		}
		return getDao().getEntityByQuery(fallBackQuery, parameterMap);
	}

	@Override
	public void build(Long tenantId) {
		buildInternal();
	}

	private <T extends BaseEntity> void buildInternal() {
		List<Map<Object, T>> entities = getDao().getEntityListByQuery(buildQuery);
		if (isIdBasedCacheableEntity()) {
			for (Map<Object, T> map : entities) {
				put(map.get(KEY), map.get(ENTITY).getId());
			}
			return;
		}

		for (Map<Object, T> map : entities) {
			put(map.get(KEY), map.get(ENTITY));
		}

	}

	private String getQueryClause(CustomCache customCache) {
		StringBuilder str = new StringBuilder().append(getPredicateClause(customCache.predicates(), andOrClause));

		CompositePredicate[] compositePredicates = customCache.compositePredicates();
		if (compositePredicates != null && compositePredicates.length > 0) {
			StringJoiner clause = new StringJoiner(andOrClause);
			Arrays.stream(compositePredicates)
					.forEach(compositePredicate -> clause.add(OPENING_BRACKET
							+ getPredicateClause(compositePredicate.predicates(), compositePredicate.operator().name())
							+ CLOSING_BRACKET));
			str.append(andOrClause).append(clause.toString());
		}
		return str.toString();
	}

	private String getPredicateClause(Predicate[] predicates, String andOr) {
		if (predicates != null && predicates.length > 0) {
			StringJoiner clause = new StringJoiner(andOr);
			Arrays.stream(predicates)
					.forEach(predicate -> clause.add(OPENING_BRACKET + ALIAS_DOT + predicate.field() + SPACE
							+ getOperatorAsString(predicate.operator()) + SPACE + getPredicateValues(predicate)
							+ CLOSING_BRACKET));
			return clause.toString();
		}
		return EMPTY_STRING;
	}

	private String getPredicateValues(Predicate predicate) {
		if (predicate.value() == null || predicate.value().length == 0) {
			return EMPTY_STRING;
		}

		StringJoiner str = new StringJoiner(COMMA);
		Arrays.stream(predicate.value()).forEach(value -> str.add(value));
		if (predicate.operator().equals(Operator.IN)) {
			return OPENING_BRACKET + str.toString() + CLOSING_BRACKET;
		}
		return str.toString();
	}

	private void initConfigInternal(CustomCache customCache, StringBuilder fallbackStringBuilder) {

		if (customCache.type().equals(CustomCache.Type.OBJECT)) {
			fallbackStringBuilder.append(FROM);
		} else {
			setIsIdBasedCacheableEntity(Boolean.TRUE);
			fallbackStringBuilder.append(FALLBACK_QUERY_ID);
		}

		if (customCache.andOr().equals(AndOr.AND)) {
			andOrClause = AND_CLAUSE;
		} else {
			andOrClause = OR_CLAUSE;
		}

	}

	private String getOperatorAsString(Operator operator) {
		return operatorToStringMap.get(operator);
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.DELETE)) {
			remove(object);
		} else if (action.equals(Action.UPDATE)) {
			Object value = this.fallback(object);
			put(object, value);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return cacheName;
	}

	@Override
	public String getCacheRegionName() {
		return regionName;
	}

	@Override
	public String getCacheGroupName() {
		if(groupName.equals(FWCacheConstants.EMPTY_VALUE)) {
			return super.getCacheGroupName();
		}
		return groupName;
	}

}
