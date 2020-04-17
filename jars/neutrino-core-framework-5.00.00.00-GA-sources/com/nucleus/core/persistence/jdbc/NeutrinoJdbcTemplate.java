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

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.Nullable;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public interface NeutrinoJdbcTemplate {

    public abstract Map<String, Object> queryForMap(String sqlQueryName) throws DataAccessException;

    public abstract <T> T queryForObject(String sqlQueryName, Class<T> requiredType) throws DataAccessException;

    public abstract <T> List<T> queryForList(String sqlQueryName, Class<T> elementType) throws DataAccessException;

    public abstract List<Map<String, Object>> queryForList(String sqlQueryName) throws DataAccessException;

    public abstract Map<String, Object> queryForMap(String sqlQueryName, Object[] args, int[] argTypes)
            throws DataAccessException;

    public abstract Map<String, Object> queryForMap(String sqlQueryName, Object... args) throws DataAccessException;

    public abstract <T> List<T> queryForList(String sqlQueryName, Object[] args, int[] argTypes, Class<T> elementType)
            throws DataAccessException;

    public abstract <T> List<T> queryForList(String sqlQueryName, Class<T> elementType, Object... args)
            throws DataAccessException;

    public abstract List<Map<String, Object>> queryForList(String sqlQueryName, Object[] args, int[] argTypes)
            throws DataAccessException;

    public abstract List<Map<String, Object>> queryForList(String sqlQueryName, Object... args) throws DataAccessException;

    // NamedParameterJDBCTemplate
    public abstract <T> T queryForObject(String sqlQueryName, Map<String, ?> namedParamMap, Class<T> requiredType)
            throws DataAccessException;

    public abstract Map<String, Object> queryForMap(String sqlQueryName, Map<String, ?> namedParamMap)
            throws DataAccessException;

    public abstract <T> List<T> queryForList(String sqlQueryName, Map<String, ?> namedParamMap, Class<T> elementType)
            throws DataAccessException;

    public abstract List<Map<String, Object>> queryForList(String sqlQueryName, Map<String, ?> namedParamMap)
            throws DataAccessException;

    // special for in clause
    public abstract <T> List<T> queryForListWithSingleInClause(String sqlQueryName, String inParamName, List<?> values,
            Class<T> elementType) throws DataAccessException;

    public abstract List<Map<String, Object>> queryForListWithSingleInClause(String sqlQueryName, String inParamName,
            List<?> values) throws DataAccessException;

    public abstract List<Map<String, Object>> queryForListWithSingleInClause(String sqlQueryName, String inParamName,
            List<?> values, Map<String, ?> namedParamMap) throws DataAccessException;
    
	/**
	 * @see JdbcOperations#update(String)
	 */
	int update(String sql) throws DataAccessException;
    
    /**
     * @see JdbcOperations#update(PreparedStatementCreator)
	 */
	int update(PreparedStatementCreator psc) throws DataAccessException;

	/**
	 * @see JdbcOperations#update(PreparedStatementCreator, KeyHolder)
	 */
	int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder) throws DataAccessException;

	/**
	 * @see JdbcOperations#update(String, PreparedStatementSetter)
	 */
	int update(String sql, @Nullable PreparedStatementSetter pss) throws DataAccessException;

	/**
	 * @see JdbcOperations#update(String, Object[], int[])
	 */
	int update(String sql, Object[] args, int[] argTypes) throws DataAccessException;

	/**
	 * @see JdbcOperations#update(String, Object...)
	 */
	int update(String sql, @Nullable Object... args) throws DataAccessException;
	
	/**
	 * @see NamedParameterJdbcOperations#update(String, SqlParameterSource)
	 */
	int update(String sql, SqlParameterSource paramSource) throws DataAccessException;

	/**
	 * @see NamedParameterJdbcOperations#update(String, Map)
	 */
	int update(String sql, Map<String, ?> paramMap) throws DataAccessException;

	/**
	 * @see NamedParameterJdbcOperations#update(String, SqlParameterSource, KeyHolder)
	 */
	int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder)
			throws DataAccessException;

	/**
	 * @see NamedParameterJdbcOperations#update(String, SqlParameterSource, KeyHolder, String[])
	 */
	int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder, String[] keyColumnNames)
			throws DataAccessException;


}
