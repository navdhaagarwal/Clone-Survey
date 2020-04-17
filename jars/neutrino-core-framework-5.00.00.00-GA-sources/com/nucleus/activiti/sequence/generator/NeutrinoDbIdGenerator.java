package com.nucleus.activiti.sequence.generator;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.activiti.engine.impl.cfg.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.persistence.sequence.DatabaseSequenceGenerator;

public class NeutrinoDbIdGenerator implements IdGenerator {

	private static final String SEQUENCE_NAME = "activiti_sequence_generator";

	@Autowired
	@Qualifier("neutrinoSequenceGenerator")
	DatabaseSequenceGenerator databaseSequenceGenerator;

	
	private DataSource dataSource;

	private Resource[] resources;

	private boolean ignoreAllFailures;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Resource[] getResources() {
		return resources;
	}

	public void setResources(Resource[] resources) {
		this.resources = resources;
	}

	public void setIgnoreAllFailures(boolean ignoreAllFailures) {
		this.ignoreAllFailures = ignoreAllFailures;
	}

	@SuppressWarnings("unused")
	public String getNextId() {
		Long nextValue = getNextSequenceValueBySequenceName(SEQUENCE_NAME);

		if (nextValue == null) {
			throw new SystemException("No sequence generator found for activiti tables.");
		}

		return Long.toString(nextValue.longValue());

	}

	private long getNextSequenceValueBySequenceName(String sequenceName) {
		return databaseSequenceGenerator.getNextValue(sequenceName);
	}

	@PostConstruct
	public void executeActitiviSequenceScript() {
		DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setDataSource(dataSource);
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setContinueOnError(ignoreAllFailures);
		populator.setIgnoreFailedDrops(ignoreAllFailures ? true : false);
		populator.setScripts(resources);
		initializer.setDatabasePopulator(populator);
		initializer.afterPropertiesSet();
	}

}