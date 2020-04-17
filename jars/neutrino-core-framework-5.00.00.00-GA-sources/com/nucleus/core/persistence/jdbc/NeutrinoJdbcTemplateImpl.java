/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.persistence.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.apache.commons.collections4.ListUtils;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named("neutrinoJdbcTemplate")
public class NeutrinoJdbcTemplateImpl implements NeutrinoJdbcTemplate, InitializingBean {

    private static final int               IN_CLAUSE_BATCH_SIZE_LIMIT = 900;

    @Autowired
    protected DataSource                   dataSource;

    @Inject
    @Named("neutrinoJdbcNamedQueryResolver")
    private NeutrinoJdbcNamedQueryResolver jdbcNamedQueryResolver;

    @Value("${hibernate.dialect}")
    private String                         hibernateDialectClassName;

    private Class<? extends Dialect>       dialectClass;

    private NamedParameterJdbcOperations     jdbcTemplate2;

    private JdbcOperations                 jdbcTemplate;

    @Override
    public Map<String, Object> queryForMap(String sqlQueryName) throws DataAccessException {

        return jdbcTemplate.queryForMap(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass));
    }

    @Override
    public <T> T queryForObject(String sqlQueryName, Class<T> requiredType) throws DataAccessException {
        return jdbcTemplate.queryForObject(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass),
                requiredType);
    }

    @Override
    public <T> List<T> queryForList(String sqlQueryName, Class<T> elementType) throws DataAccessException {
        return jdbcTemplate.queryForList(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), elementType);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sqlQueryName) throws DataAccessException {
        return jdbcTemplate.queryForList(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass));
    }

    @Override
    public Map<String, Object> queryForMap(String sqlQueryName, Object[] args, int[] argTypes) throws DataAccessException {
        return jdbcTemplate
                .queryForMap(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), args, argTypes);
    }

    @Override
    public Map<String, Object> queryForMap(String sqlQueryName, Object... args) throws DataAccessException {
        return jdbcTemplate.queryForMap(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), args);
    }

    @Override
    public <T> List<T> queryForList(String sqlQueryName, Object[] args, int[] argTypes, Class<T> elementType)
            throws DataAccessException {
        return jdbcTemplate.queryForList(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), args,
                argTypes, elementType);
    }

    @Override
    public <T> List<T> queryForList(String sqlQueryName, Class<T> elementType, Object... args) throws DataAccessException {
        return jdbcTemplate.queryForList(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), elementType,
                args);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sqlQueryName, Object[] args, int[] argTypes)
            throws DataAccessException {
        return jdbcTemplate.queryForList(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), args,
                argTypes);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sqlQueryName, Object... args) throws DataAccessException {
        return jdbcTemplate.queryForList(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), args);
    }

    // NamedParameterJDBCTemplate
    @Override
    public <T> T queryForObject(String sqlQueryName, Map<String, ?> namedParamMap, Class<T> requiredType)
            throws DataAccessException {
        return jdbcTemplate2.queryForObject(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass),
                new MapSqlParameterSource(namedParamMap), requiredType);
    }

    @Override
    public Map<String, Object> queryForMap(String sqlQueryName, Map<String, ?> namedParamMap) throws DataAccessException {
        return jdbcTemplate2.queryForMap(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass),
                new MapSqlParameterSource(namedParamMap));
    }

    @Override
    public <T> List<T> queryForList(String sqlQueryName, Map<String, ?> namedParamMap, Class<T> elementType)
            throws DataAccessException {
        return jdbcTemplate2.queryForList(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass),
                new MapSqlParameterSource(namedParamMap), elementType);
    }

    @Override
    public List<Map<String, Object>> queryForList(String sqlQueryName, Map<String, ?> namedParamMap)
            throws DataAccessException {
        return jdbcTemplate2.queryForList(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass),
                new MapSqlParameterSource(namedParamMap));
    }

    public SqlRowSet queryForRowSet(String sqlQueryName, Map<String, ?> namedParamMap) throws DataAccessException {
        return jdbcTemplate2.queryForRowSet(jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass),
                new MapSqlParameterSource(namedParamMap));
    }

    // special for in clause
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> List<T> queryForListWithSingleInClause(String sqlQueryName, String inParamName, List<?> values,
            Class<T> elementType) throws DataAccessException {

        List<T> completeResultList = new ArrayList<T>();
        if (values == null) {
            return completeResultList;
        }

        // first of all remove nulls
        values = ListUtils.removeAll(values, Collections.singletonList(null));

        if (values != null && !values.isEmpty()) {

            int fromIndex = 0;
            int toIndex = values.size() > IN_CLAUSE_BATCH_SIZE_LIMIT ? IN_CLAUSE_BATCH_SIZE_LIMIT : values.size();
            while (toIndex <= values.size()) {
                List<?> idSubList = new ArrayList(values.subList(fromIndex, toIndex));
                PersistenceUtils.resizeListWithAutoFill(idSubList);
                if (!idSubList.isEmpty()) {
                    MapSqlParameterSource parameters = new MapSqlParameterSource();
                    parameters.addValue(inParamName, idSubList);
                    List<T> resultForBatch = jdbcTemplate2.queryForList(
                            jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), parameters, elementType);
                    if (resultForBatch != null) {
                        completeResultList.addAll(resultForBatch);
                    }
                    fromIndex = toIndex;
                    int difference = values.size() - toIndex;
                    if (difference <= 0) {
                        break;
                    }
                    int batchSize = difference > IN_CLAUSE_BATCH_SIZE_LIMIT ? IN_CLAUSE_BATCH_SIZE_LIMIT : difference;
                    toIndex = toIndex + batchSize;
                }
            }
        }
        return completeResultList;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<Map<String, Object>> queryForListWithSingleInClause(String sqlQueryName, String inParamName, List<?> values)
            throws DataAccessException {
        List<Map<String, Object>> completeResultList = new ArrayList<Map<String, Object>>();
        if (values == null) {
            return completeResultList;
        }

        // first of all remove nulls
        values = ListUtils.removeAll(values, Collections.singletonList(null));

        if (values != null && !values.isEmpty()) {

            int fromIndex = 0;
            int toIndex = values.size() > IN_CLAUSE_BATCH_SIZE_LIMIT ? IN_CLAUSE_BATCH_SIZE_LIMIT : values.size();
            while (toIndex <= values.size()) {
                List<?> idSubList = new ArrayList(values.subList(fromIndex, toIndex));
                PersistenceUtils.resizeListWithAutoFill(idSubList);
                if (!idSubList.isEmpty()) {
                    MapSqlParameterSource parameters = new MapSqlParameterSource();
                    parameters.addValue(inParamName, idSubList);
                    List<Map<String, Object>> resultForBatch = jdbcTemplate2.queryForList(
                            jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), parameters);
                    if (resultForBatch != null) {
                        completeResultList.addAll(resultForBatch);
                    }
                    fromIndex = toIndex;
                    int difference = values.size() - toIndex;
                    if (difference <= 0) {
                        break;
                    }
                    int batchSize = difference > IN_CLAUSE_BATCH_SIZE_LIMIT ? IN_CLAUSE_BATCH_SIZE_LIMIT : difference;
                    toIndex = toIndex + batchSize;
                }
            }
        }
        return completeResultList;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate2 = new NamedParameterJdbcTemplate(dataSource);
        jdbcTemplate = jdbcTemplate2.getJdbcOperations();
        dialectClass = (Class<? extends Dialect>) Class.forName(hibernateDialectClassName);
    }

    public void setHibernateDialectClassName(String hibernateDialectClassName) {
        this.hibernateDialectClassName = hibernateDialectClassName;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<Map<String, Object>> queryForListWithSingleInClause(String sqlQueryName, String inParamName, List<?> values,
            Map<String, ?> namedParamMap) throws DataAccessException {
        List<Map<String, Object>> completeResultList = new ArrayList<Map<String, Object>>();
        if (values == null) {
            return completeResultList;
        }

        // first of all remove nulls
        values = ListUtils.removeAll(values, Collections.singletonList(null));

        if (values != null && !values.isEmpty()) {

            int fromIndex = 0;
            int toIndex = values.size() > IN_CLAUSE_BATCH_SIZE_LIMIT ? IN_CLAUSE_BATCH_SIZE_LIMIT : values.size();
            while (toIndex <= values.size()) {
                List<?> idSubList = new ArrayList(values.subList(fromIndex, toIndex));
                PersistenceUtils.resizeListWithAutoFill(idSubList);
                if (!idSubList.isEmpty()) {
                    MapSqlParameterSource parameters = new MapSqlParameterSource();
                    parameters.addValue(inParamName, idSubList);
                    parameters.addValues(namedParamMap);
                    List<Map<String, Object>> resultForBatch = jdbcTemplate2.queryForList(
                            jdbcNamedQueryResolver.resolveNamedQuery(sqlQueryName, dialectClass), parameters);
                    if (resultForBatch != null) {
                        completeResultList.addAll(resultForBatch);
                    }
                    fromIndex = toIndex;
                    int difference = values.size() - toIndex;
                    if (difference <= 0) {
                        break;
                    }
                    int batchSize = difference > IN_CLAUSE_BATCH_SIZE_LIMIT ? IN_CLAUSE_BATCH_SIZE_LIMIT : difference;
                    toIndex = toIndex + batchSize;
                }
            }
        }
        return completeResultList;
    }

	@Override
	public int update(String sql) throws DataAccessException {
		return jdbcTemplate.update(sql);
	}

	@Override
	public int update(PreparedStatementCreator psc) throws DataAccessException {
		return jdbcTemplate.update(psc);
	}

	@Override
	public int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder) throws DataAccessException {
		return jdbcTemplate.update(psc, generatedKeyHolder);
	}

	@Override
	public int update(String sql, PreparedStatementSetter pss) throws DataAccessException {
		return jdbcTemplate.update(sql, pss);
	}

	@Override
	public int update(String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return jdbcTemplate.update(sql, args, argTypes);
	}

	@Override
	public int update(String sql, @Nullable Object... args) throws DataAccessException {
		return jdbcTemplate.update(sql, args);
	}

	@Override
	public int update(String sql, SqlParameterSource paramSource) throws DataAccessException {
		return jdbcTemplate2.update(sql, paramSource);
	}

	@Override
	public int update(String sql, Map<String, ?> paramMap) throws DataAccessException {
		return jdbcTemplate2.update(sql, paramMap);
	}

	@Override
	public int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder) throws DataAccessException {
		return jdbcTemplate2.update(sql, paramSource, generatedKeyHolder);
	}

	@Override
	public int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder, String[] keyColumnNames) throws DataAccessException {
		return jdbcTemplate2.update(sql, paramSource, generatedKeyHolder, keyColumnNames);
	}
}
