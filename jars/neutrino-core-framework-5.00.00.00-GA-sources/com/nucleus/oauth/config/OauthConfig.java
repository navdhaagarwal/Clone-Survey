package com.nucleus.oauth.config;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="SELECT,REFERENCES")
@NamedQueries({ @NamedQuery(name = "getActiveOauthConfigByClientId",
			query = "SELECT oauthConfig FROM OauthConfig oauthConfig WHERE lower(oauthConfig.clientId) =lower(:clientId) AND "
					+ " oauthConfig.activeFlag = true AND oauthConfig.masterLifeCycleData.approvalStatus IN (:approvalStatusList)  AND (oauthConfig.entityLifeCycleData.snapshotRecord IS NULL "
					+ " OR oauthConfig.entityLifeCycleData.snapshotRecord = false)")})
public class OauthConfig extends BaseMasterEntity {

	private static final long serialVersionUID = 75823623523956323L;
	
	/*private static final String ALGORITHM = "PBEWithMD5AndTripleDES";
	
	private static final String KEY_OBTENTION_ITERATIONS = "1000";*/
	
	private String clientId;
	
	/*@Type(type="com.nucleus.jasypt.hibernate5.type.NeutrinoEncryptedStringType", parameters={
			@Parameter(name="algorithm", value=ALGORITHM),
			@Parameter(name="password", value="469638737725"),
			@Parameter(name="keyObtentionIterations", value=KEY_OBTENTION_ITERATIONS)
	})*/
	private String clientSecret;
	
	private boolean anonymous = true;
	
	private String username;
	
	/*@Type(type="com.nucleus.jasypt.hibernate5.type.NeutrinoEncryptedStringType", parameters={
			@Parameter(name="algorithm", value="PBEWithMD5AndTripleDES"),
			@Parameter(name="password", value="469638737725"),
			@Parameter(name="keyObtentionIterations", value="500")
	})*/
	private String password;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	
	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		return clientId.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OauthConfig) {
			OauthConfig oauthConfig = (OauthConfig) obj;
			return oauthConfig.clientId.equals(this.clientId);
		}
		return false;
	}
	
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        OauthConfig oauthConfig = (OauthConfig) baseEntity;
        super.populate(oauthConfig, cloneOptions);
        oauthConfig.setClientId(this.clientId);
        oauthConfig.setClientSecret(this.clientSecret);
        oauthConfig.setAnonymous(this.anonymous);
        oauthConfig.setUsername(this.username);
        oauthConfig.setPassword(this.password);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        OauthConfig oauthConfig = (OauthConfig) baseEntity;
        super.populateFrom(oauthConfig, cloneOptions);
        this.setClientId(oauthConfig.getClientId());
        this.setClientSecret(oauthConfig.getClientSecret());
        this.setAnonymous(oauthConfig.isAnonymous());
        this.setUsername(oauthConfig.getUsername());
        this.setPassword(oauthConfig.getPassword());
    }

    public String getLogInfo() {
        return  "Master Object for OauthConfig received to be saved. Client ID:  ------------> " + clientId;
    }
    
    @Override
    public String getDisplayName() {
        return clientId;
    }

}
