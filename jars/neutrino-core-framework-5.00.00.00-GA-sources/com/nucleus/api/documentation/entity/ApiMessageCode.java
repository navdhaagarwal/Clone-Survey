package com.nucleus.api.documentation.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "API_MESSAGE_CODES")
@Synonym(grant = "ALL")
@NamedQueries(value = {
		@NamedQuery(name = "getApiMessageCodesByModuleCode", query = "select amc from ApiMessageCode amc where moduleCode =:moduleCode") })
public class ApiMessageCode extends BaseEntity {

	private static final long serialVersionUID = 6913605393160024989L;

	@Column(name = "MESSAGE_CODE")
	private String messageCode;

	@Column(name = "API_CODE")
	private String apiCode;

	@Column(name = "MODULE_CODE")
	private String moduleCode;

	public String getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}

	public String getApiCode() {
		return apiCode;
	}

	public void setApiCode(String apiCode) {
		this.apiCode = apiCode;
	}

	public String getModuleCode() {
		return moduleCode;
	}

	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

}
