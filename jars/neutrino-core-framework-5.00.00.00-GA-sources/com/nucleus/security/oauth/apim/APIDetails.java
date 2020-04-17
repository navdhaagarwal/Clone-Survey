package com.nucleus.security.oauth.apim;


import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.authority.Authority;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Table(name="api_details")
@Synonym(grant="ALL")
	
public class APIDetails extends BaseMasterEntity {
	private String apiUri;
	private String apiCode;

	private String version;
	private String description;
	
	
	@OneToOne
	private Authority authority;
	
	public Authority getAuthority() {
		return authority;
	}

	public void setAuthority(Authority authority) {
		this.authority = authority;
	}

	@ManyToMany
	@JoinColumn(name = "id")
	private Set<ThrottlingPolicy> policies;
	private static final long serialVersionUID = 924139382774066635L;

	
	public Set<ThrottlingPolicy> getPolicies() {
		return policies;
	}

	public void setPolicies(Set<ThrottlingPolicy> policies) {
		this.policies = policies;
	}

	public String getApiUri() {
		return apiUri;
	}

	public void setApiUri(String apiUri) {
		this.apiUri = apiUri;
	}

	public String getApiCode() {
		return apiCode;
	}

	public void setApiCode(String apiCode) {
		this.apiCode = apiCode;
	}
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
