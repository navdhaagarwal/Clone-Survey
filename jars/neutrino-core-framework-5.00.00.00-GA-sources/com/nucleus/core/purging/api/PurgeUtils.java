/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - ï¿½ 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.purging.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.nucleus.core.persistence.jdbc.PersistenceUtils;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import static com.nucleus.core.purging.api.PurgeConstants.ERROR_EXECUTING_PREPARED_STATEMENT_BATCH;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public class PurgeUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger("com.nucleus.cas.purgeservice");

	private static final int IN_CLAUSE_BATCH_SIZE_LIMIT = 900;

	private static final int BULK_UPDATE_BATCH_SIZE_LIMIT = 900;

	private static final int[] AUTO_FILL_RANGES = { 0, 10, 50, 100, 200, 500, 900 };
	private static final int LOOP_END = AUTO_FILL_RANGES.length - 1;
	
	private PurgeUtils() {

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> List<T> queryForListWithSingleInClause(String sqlString, String inParamName, List<?> paramValues,
			Class<T> elementType, NamedParameterJdbcTemplate jdbcTemplate2) {

		List<T> completeResultList = new ArrayList<>();
		if (paramValues == null) {
			return completeResultList;
		}

		// first of all remove nulls
		List<?> values = ListUtils.removeAll(paramValues, Collections.singletonList(null));

		if (values != null && !values.isEmpty()) {
			completeResultList = executeQueryForList(completeResultList, values, sqlString, inParamName, elementType,
					jdbcTemplate2);

		}
		return completeResultList;
	}

	private static <T> List<T> executeQueryForList(List<T> completeResultList, List<?> values, String sqlString,
			String inParamName, Class<T> elementType, NamedParameterJdbcTemplate jdbcTemplate2) {
		int fromIndex = 0;
		int toIndex = values.size() > IN_CLAUSE_BATCH_SIZE_LIMIT ? IN_CLAUSE_BATCH_SIZE_LIMIT : values.size();
		while (toIndex <= values.size()) {
			List<?> idSubList = new ArrayList(values.subList(fromIndex, toIndex));
			PersistenceUtils.resizeListWithAutoFill(idSubList);
			if (!idSubList.isEmpty()) {
				MapSqlParameterSource parameters = new MapSqlParameterSource();
				parameters.addValue(inParamName, idSubList);
				List<T> resultForBatch = jdbcTemplate2.queryForList(sqlString, parameters, elementType);
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
		return completeResultList;
	}

	private static List<Map<String, Object>> executeQueryForList(List<Map<String, Object>> completeResultList,
			List<?> values, String sqlString, String inParamName, NamedParameterJdbcTemplate jdbcTemplate2) {
		int fromIndex = 0;
		int toIndex = values.size() > IN_CLAUSE_BATCH_SIZE_LIMIT ? IN_CLAUSE_BATCH_SIZE_LIMIT : values.size();
		while (toIndex <= values.size()) {
			List<?> idSubList = new ArrayList(values.subList(fromIndex, toIndex));
			PersistenceUtils.resizeListWithAutoFill(idSubList);
			if (!idSubList.isEmpty()) {
				MapSqlParameterSource parameters = new MapSqlParameterSource();
				parameters.addValue(inParamName, idSubList);
				List<Map<String, Object>> resultForBatch = jdbcTemplate2.queryForList(sqlString, parameters);
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

		return completeResultList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Map<String, Object>> queryForListWithSingleInClause(String sqlString, String inParamName,
			List<?> paramValues, NamedParameterJdbcTemplate jdbcTemplate2) {

		List<Map<String, Object>> completeResultList = new ArrayList<>();
		if (paramValues == null) {
			return completeResultList;
		}

		// first of all remove nulls
		List<?> values = ListUtils.removeAll(paramValues, Collections.singletonList(null));

		if (values != null && !values.isEmpty()) {
			completeResultList = executeQueryForList(completeResultList, values, sqlString, inParamName, jdbcTemplate2);
		}
		return completeResultList;
	}

	private static int[] prepareAndExecuteBatch(String sqlString, List<?> idList, Connection conn, int batchSize) {

		if (idList.isEmpty()) {
			return new int[] { 0 };
		}

		PreparedStatement ps = null;

		int listSize = idList.size();
		try {
			ps = conn.prepareStatement(sqlString);
			int i = 0; // to iterate the list from 0,1,2...listSize-1
			int j = 1; // to set jdbc param 1,2,3.....batchSize
			while (i < listSize) {
				setParameter(ps, j, idList.get(i));
				// if j is equal to batch size, we have filled the current batch
				if (j == batchSize) {
					ps.addBatch();
					j = 1;
				} else {
					j++;
				}
				i++;
			}
			// when above loop exits - there are no more items in idList
			// j is the next position of the parameter in prepared statement
			if (j != 1) {
				for (; j <= batchSize; j++) {
					// set last item from list
					setParameter(ps, j, idList.get(idList.size() - 1));
				}
				ps.addBatch();
			}

			// execute batch
			return ps.executeBatch();
		} catch (Exception e) {
			LOGGER.error(ERROR_EXECUTING_PREPARED_STATEMENT_BATCH, e);
			throw ExceptionBuilder
					.getInstance(SystemException.class, ERROR_EXECUTING_PREPARED_STATEMENT_BATCH,
							ERROR_EXECUTING_PREPARED_STATEMENT_BATCH)
					.setMessage(CoreUtility.prepareMessage(ERROR_EXECUTING_PREPARED_STATEMENT_BATCH))
					.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
		} finally {
			// close the prepared statement
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				// nothing
				LOGGER.error("Error in closing prepared statement", e);
			}
		}
	}

	public static int[] batchUpdate(String sqlString, List<?> idList, Connection conn) {

		// first of all remove nulls
		List<?> updatedIdList = ListUtils.removeAll(idList, Collections.singletonList(null));
		// decide batch size
		int batchSize = batchSize(updatedIdList.size());

		StrBuilder parameterBuilder = new StrBuilder();
		for (int i = 1; i <= batchSize; i++) {
			parameterBuilder.appendSeparator(',').append('?');
		}
		String formattedSqlString = String.format(sqlString, parameterBuilder.toString());
		return prepareAndExecuteBatch(formattedSqlString, updatedIdList, conn, batchSize);
	}

	public static int[] batchArchive(PurgeParameterHolder holder) {

		// first of all remove nulls
        List<?> idList = ListUtils.removeAll(holder.getIdList(), Collections.singletonList(null));		// decide batch size
		int batchSize = batchSize(idList.size());

		StrBuilder parameterBuilder = new StrBuilder();
		for (int i = 1; i <= batchSize; i++) {
			parameterBuilder.appendSeparator(',').append('?');
		}
		String sqlString = String.format(holder.getSqlString(), holder.getArchiveTableName(), holder.getTableName(), holder.getIdColumnName(),
                parameterBuilder.toString());

		return prepareAndExecuteBatch(sqlString, idList, holder.getConn(), batchSize);

	}

    public static int[] batchDelete(PurgeParameterHolder holder) {
		// first of all remove nulls
        List<?> idList = ListUtils.removeAll(holder.getIdList(), Collections.singletonList(null));		// decide batch size
		int batchSize = batchSize(idList.size());

		StrBuilder parameterBuilder = new StrBuilder();
		for (int i = 1; i <= batchSize; i++) {
			parameterBuilder.appendSeparator(',').append('?');
		}
		String sqlString = String.format(holder.getSqlString(), holder.getTableName(), holder.getIdColumnName(), parameterBuilder.toString());
        return prepareAndExecuteBatch(sqlString, idList, holder.getConn(), batchSize);

	}

	private static void setParameter(PreparedStatement ps, int pos, Object object) throws SQLException {

		Class<?> parameterType = object.getClass();
		if (String.class.isAssignableFrom(parameterType)) {
			ps.setString(pos, (String) object);
		} else if (Long.class.isAssignableFrom(parameterType)) {
			ps.setLong(pos, (long) object);
		} else if (Integer.class.isAssignableFrom(parameterType)) {
			ps.setInt(pos, (int) object);
		} else if (Number.class.isAssignableFrom(parameterType)) {
			ps.setLong(pos, ((Number) object).longValue());
		}

		// can set more types...
	}

	public static Logger purgeLogger() {

		return LOGGER;
	}

	private static int batchSize(int size) {
		for (int i = 0; i < LOOP_END; i++) {
			if (AUTO_FILL_RANGES[i] < size && size <= AUTO_FILL_RANGES[i + 1]) {
				return AUTO_FILL_RANGES[i + 1];
			}
		}
		return size > BULK_UPDATE_BATCH_SIZE_LIMIT ? BULK_UPDATE_BATCH_SIZE_LIMIT : size;
	}

	

}
