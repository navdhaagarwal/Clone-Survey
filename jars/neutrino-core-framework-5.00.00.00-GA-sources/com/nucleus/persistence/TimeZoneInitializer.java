package com.nucleus.persistence;

import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author prateek.chachra
 *
 */
public class TimeZoneInitializer implements InitializingBean {

	@Value(value = "#{'${jadira.usertype.javaTimeZone}'}")
	private String jadiraJvmTZ;

	@Value(value = "#{'${jadira.usertype.databaseTimeZone}'}")
	private String jadiraJdbcTZ;

	@Value(value = "#{'${hibernate.jdbc.time_zone}'}")
	private String hibernateJdbcTZ;

	
	
	private static final String JADIRA_JVM_PROPERTY = "jadira.usertype.javaZone";
	private static final String JADIRA_JDBC_PROPERTY = "jadira.usertype.databaseZone";
	private static final String HIBERNATE_JDBC_PROPERTY = "hibernate.jdbc.time_zone";
	

	private Properties jpaProperties;

	@Override
	public void afterPropertiesSet() throws Exception {
		String applyTimeZoneConfig = (String) this.jpaProperties.get("timezone.config.applicable");
		if ("true".equals(applyTimeZoneConfig)) {

			jpaProperties.put(JADIRA_JVM_PROPERTY, jadiraJvmTZ);
			jpaProperties.put(JADIRA_JDBC_PROPERTY, jadiraJdbcTZ);
			jpaProperties.put(HIBERNATE_JDBC_PROPERTY, hibernateJdbcTZ);

		}

	}

	public Properties getJpaProperties() {
		return jpaProperties;
	}

	public void setJpaProperties(Properties jpaProperties) {
		this.jpaProperties = jpaProperties;
	}

}
