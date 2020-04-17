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
public class AuthorizedGrantTypeMapping extends BaseMasterEntity{

		private static final long serialVersionUID = 1L;
		
		@ManyToOne
		private AuthorizedGrantType authorizedGrantType;

		public AuthorizedGrantType getAuthorizedGrantType() {
			return authorizedGrantType;
		}

		public void setAuthorizedGrantType(AuthorizedGrantType authorizedGrantType) {
			this.authorizedGrantType = authorizedGrantType;
		}
		 protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
			 AuthorizedGrantTypeMapping authorizedGrantTypeMapping = (AuthorizedGrantTypeMapping) baseEntity;
		        super.populate(authorizedGrantTypeMapping, cloneOptions);
		        authorizedGrantTypeMapping.setAuthorizedGrantType(authorizedGrantType);
		 }
		  @Override
		    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
			  AuthorizedGrantTypeMapping authorizedGrantTypeMapping = (AuthorizedGrantTypeMapping) baseEntity;
		        super.populateFrom(authorizedGrantTypeMapping, cloneOptions);
		        this.setAuthorizedGrantType(authorizedGrantTypeMapping.getAuthorizedGrantType());
		  }
}
