package com.nucleus.security.oauth.apim;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.genericparameter.entity.TimeUnit;
import com.nucleus.entity.BaseEntity;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;



/**
 * 
 * 
 * 
 * @author NSEL
 *
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class ThrottlingPolicy extends BaseEntity{
	
	private static final long serialVersionUID = 924139382774062634L;


	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private TimeUnit timeUnit;
	
	
	
	/**
	 * Allowed Quota for the API (cannot be greater than the global value for 
	 * client specific values
	 * 
	 * 
	 */
	private Integer allowedQuota;
	
	
	
	/**
	 * Global means for the specific API this Throttling Policy is set globally 
	 * and comes from license instead of being set for just one Trusted Source.
	 * 
	 */
	private Boolean isGlobal;
	
	
	/**
	 * If the above property (isGlobal) comes out to be true, the mapped
	 * Trusted source is null, since it is set for an API for all 
	 * trusted sources, but if the above property is false the throttling
	 * policy must be mapped to a source.
	 * 
	 */
	@ManyToOne
	private OauthClientDetails mappedTrustedSource;
	
	
	public Boolean getIsGlobal() {
		return isGlobal;
	}
	public void setIsGlobal(Boolean isGlobal) {
		this.isGlobal = isGlobal;
	}
	public OauthClientDetails getMappedTrustedSource() {
		return mappedTrustedSource;
	}
	public void setMappedTrustedSource(OauthClientDetails mappedTrustedSource) {
		this.mappedTrustedSource = mappedTrustedSource;
	}
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}
	public Integer getAllowedQuota() {
		return allowedQuota;
	}
	public void setAllowedQuota(Integer allowedQuota) {
		this.allowedQuota = allowedQuota;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
