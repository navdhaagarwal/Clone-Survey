package com.nucleus.security.oauth.domainobject;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Cacheable
public class AuthorizedGrantType extends BaseMasterEntity{
	  
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String grantType;

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType){
		this.grantType = grantType;
	}
}
