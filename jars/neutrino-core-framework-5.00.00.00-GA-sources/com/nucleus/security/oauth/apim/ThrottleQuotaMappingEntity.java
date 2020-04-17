package com.nucleus.security.oauth.apim;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class ThrottleQuotaMappingEntity extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 162724724652347L;

	private String apiCode;

	private String clientId;

	@ManyToOne
	private ThrottlingPolicy policy;
	
	private Integer quotaRemaining;
	
	


	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime startedDate;
	

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime activeSinceDate;
	
	
	public String getApiCode() {
		return apiCode;
	}

	public void setApiCode(String apiCode) {
		this.apiCode = apiCode;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

    public Integer getQuotaRemaining() {
		return quotaRemaining;
	}

	public void setQuotaRemaining(Integer quotaRemaining) {
		this.quotaRemaining = quotaRemaining;
	}
}
