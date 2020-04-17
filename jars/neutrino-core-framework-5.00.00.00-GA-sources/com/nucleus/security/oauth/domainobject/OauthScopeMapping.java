package com.nucleus.security.oauth.domainobject;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class OauthScopeMapping extends BaseMasterEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne
	private OauthScope oauthScope;

	public OauthScope getOauthScope() {
		return oauthScope;
	}

	public void setOauthScope(OauthScope oauthScope) {
		this.oauthScope = oauthScope;
	}
	 protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		 OauthScopeMapping oauthScopeMapping = (OauthScopeMapping) baseEntity;
	        super.populate(oauthScopeMapping, cloneOptions);
	        oauthScopeMapping.setOauthScope(oauthScope);
	 }
	  @Override
	    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		  OauthScopeMapping oauthScopeMapping = (OauthScopeMapping) baseEntity;
	        super.populateFrom(oauthScopeMapping, cloneOptions);
	        this.setOauthScope(oauthScopeMapping.getOauthScope());
	  }
}
