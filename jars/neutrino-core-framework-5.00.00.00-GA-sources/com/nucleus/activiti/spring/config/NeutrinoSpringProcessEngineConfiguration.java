package com.nucleus.activiti.spring.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.activiti.sequence.generator.NeutrinoDbIdGenerator;
import com.nucleus.finnone.pro.base.exception.SystemException;

public class NeutrinoSpringProcessEngineConfiguration extends SpringProcessEngineConfiguration {

	private static final String TRUE = "true";
	private static final String FALSE = "false";

	@Inject
	@Named("neutrinoDbIdGenerator")
	NeutrinoDbIdGenerator dbIdGenerator;

	@Value("${activiti.use.default.idGenerator}")
	private String isDefaultIdGenerator;

	@PostConstruct
	public void initializeIDGeneratorInstance() {
		if (!getDefaultIdGeneratorFlag()) {
			setIdGenerator(dbIdGenerator);
		}
	}

	public boolean getDefaultIdGeneratorFlag() {
		if ("${activiti.use.default.idGenerator}".equalsIgnoreCase(isDefaultIdGenerator)) {
			return true;
		} else if (!TRUE.equalsIgnoreCase(isDefaultIdGenerator) && !FALSE.equalsIgnoreCase(isDefaultIdGenerator)) {
			throw new SystemException("Flag \'activiti.use.default.idGenerator\' value should be  either \'true\' or \'false\' but provided value is "+isDefaultIdGenerator);
		}

		return Boolean.parseBoolean(isDefaultIdGenerator);
	}

}
