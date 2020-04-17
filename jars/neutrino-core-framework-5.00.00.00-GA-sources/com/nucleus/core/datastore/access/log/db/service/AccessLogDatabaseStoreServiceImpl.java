package com.nucleus.core.datastore.access.log.db.service;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.accesslog.entity.AccessLog;
import com.nucleus.persistence.sequence.DatabaseSequenceGenerator;

@Named("accessLogDatabaseStoreService")
public class AccessLogDatabaseStoreServiceImpl implements AccessLogDatabaseStoreService {

	private static final String ACCESS_LOG_SEQUENCE = "access_log_seq";
	private static final String ACCESS_LOG_TABLE = "access_log";
	
	@Value("${aggregator.message.group.size}")
	private String batchSize;
	
	@Value("${accessLog.nosql.store.enabled:false}")
	private String isNoSqlStoreEnabled;
	
	@Autowired
	@Qualifier("neutrinoSequenceGenerator")
	DatabaseSequenceGenerator databaseSequenceGenerator;	
	
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void saveAccessLog(List<AccessLog> accessLogList) throws DataAccessException, Exception {

		String sql = "INSERT INTO  " + ACCESS_LOG_TABLE
				+ " (ID,SESSION_ID,USER_NAME,URI,URI_FRAGMENT,QUERY_STRING,QUERY_STRING_FRAGMENT,REMOTEHOST,REQUEST_DATE_TIME,SERVER_IP,MODULE,METHOD,STATUS_CODE,WEB_URI_REPOSITORY)"
				+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		jdbcTemplate.batchUpdate(sql, accessLogList, getBatchSize(),
				new ParameterizedPreparedStatementSetter<AccessLog>() {
					@Override
					public void setValues(PreparedStatement ps, AccessLog accessLog) throws SQLException {
						setParametersForAccessLog(ps, accessLog);
					}
				});
	}

	protected void setParametersForAccessLog(PreparedStatement ps, AccessLog accessLog) throws SQLException {
		ps.setLong(1, databaseSequenceGenerator.getNextValue(ACCESS_LOG_SEQUENCE));
		ps.setString(2, accessLog.getSessionId());
		ps.setString(3, accessLog.getUserName());
		ps.setString(4, accessLog.getUri());
		ps.setString(5, accessLog.getUriFragment());
		ps.setString(6, accessLog.getQueryString());
		ps.setString(7, accessLog.getQueryStringFragment());
		ps.setString(8, accessLog.getRemotehost());
		ps.setDate(9, Date.valueOf(accessLog.getRequestDateTime().toLocalDate()));
		ps.setString(10, accessLog.getServerIp());
		ps.setString(11, accessLog.getModule());
		ps.setString(12, accessLog.getMethod());
		ps.setInt(13, accessLog.getStatusCode());
		ps.setString(14, accessLog.getWebUriRepository());	
	}

	private int getBatchSize() {
		return Integer.parseInt(batchSize);
	}
	
	@PostConstruct
	private void initializeJdbcTemplate(){
		if (!Boolean.parseBoolean(isNoSqlStoreEnabled)) {
			jdbcTemplate = NeutrinoSpringAppContextUtil.getBeanByName("accessLogJdbcTemplate",JdbcTemplate.class);
		}
	}
}
