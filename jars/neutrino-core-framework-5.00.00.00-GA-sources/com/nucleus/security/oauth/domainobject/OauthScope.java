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
public class OauthScope extends BaseMasterEntity{
private static final long serialVersionUID = 1L;
String scope;

public String getScope() {
	return scope;
}

public void setScope(String scope) {
	this.scope = scope;
}
}
