package com.nucleus.security.oauth.domainobject;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.security.oauth.apim.APIDetails;
import com.nucleus.user.IPAddressRange;
import com.nucleus.user.User;
@Entity
@DynamicUpdate
@DynamicInsert
@NamedQueries({

@NamedQuery(name = "getTrustedSource",

query = "select oauthClientDetails from OauthClientDetails oauthClientDetails where oauthClientDetails.clientId = :clientId and (oauthClientDetails.masterLifeCycleData.approvalStatus in (0,3,4,6)) and oauthClientDetails.activeFlag = true", hints = {
		@QueryHint(name = "org.hibernate.cacheable", value = "true"),
		@QueryHint(name = "org.hibernate.cacheRegion", value = "getTrustedSource") })

})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant = "ALL")
public class OauthClientDetails extends BaseMasterEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String SHARED_OAUTH_ENCYPTION_PASS_PHRASE = "01220039";
	private String mailId;
	private String encryptedSecret;
	
	@Override
	 public String getDisplayName() {
	        return clientId;
	    }

	private static String ENTITY_DISPLAY_NAME="Trusted Source";

    @Override
	public String getEntityDisplayName() {
		return ENTITY_DISPLAY_NAME;
	}
	/**
	 * Changes added for API Management
	 * 
	 * 
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "client_iprange", joinColumns = @JoinColumn(name = "client_id"))
	private List<IPAddressRange> ipAddresses;

	private Boolean isClientSecretChangeRequired;
	
	private Boolean isInternal=Boolean.FALSE;

	@ManyToMany
	@JoinTable(name="client_apis_mapping")
	private List<APIDetails> mappedAPIs;
	
	@ManyToMany
	@JoinTable(name="client_trusted_users_mapping")
	private List<User> trustedUsers;

	private Integer namedUserCount;
	private Integer concurrentUserCount;
	

	private String hashKey;
	private String clientId;
	private String clientSecret;
	private String passPhrase;
	private Integer accessTokenValiditySeconds;
	private Integer refreshTokenValiditySeconds;
	@Transient
	private long[] authorizedGrantTypeIds;

	@Transient
	private long[] oAuthScopeIds;

	
	/*
	@Transient
	private long[] mappedApiIds;
	*/

	@Transient
	private Map<Long, String> mappedApis;

	@Transient
	private long[] trustedUserIds;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "client_id")
	private List<AuthorizedGrantTypeMapping> authorizedGrantTypeMappings = null;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "client_id")
	private List<OauthScopeMapping> oauthScopeList = null;
	
	 @Type(type="encryptedString")
	private String idpClientId;
	
	 @Type(type="encryptedString")
	private String idpClientSecret;
	
	//Used in case of authorization code grant type
	 @Type(type="encryptedString")
	private String redirectUri;

	public List<OauthScopeMapping> getOauthScopeList() {
		return oauthScopeList;
	}

	public void setOauthScopeList(List<OauthScopeMapping> oauthScopeList) {
		this.oauthScopeList = oauthScopeList;
	}

	public Set<String> getScopeList() {

		Set<String> scopeSet = new HashSet<String>();
		for (OauthScopeMapping scope : oauthScopeList)
			if (scope.getOauthScope() != null) {
				scopeSet.add(scope.getOauthScope().getScope());
			}
		return scopeSet;

	}

	public String getHashKey() {
		return hashKey;
	}

	public boolean isScoped() {
		return false;
	}

	public long[] getOAuthScopeIds() {
		return oAuthScopeIds;
	}


	public long[] getTrustedUserIds() {
		return trustedUserIds;
	}

	public void setTrustedUserIds(long[] trustedUserIds) {
		this.trustedUserIds = trustedUserIds;
	}
	public Integer getNamedUserCount() {
		return namedUserCount;
	}

	public void setNamedUserCount(Integer namedUserCount) {
		this.namedUserCount = namedUserCount;
	}

	public Integer getConcurrentUserCount() {
		return concurrentUserCount;
	}

	public void setConcurrentUserCount(Integer concurrentUserCount) {
		this.concurrentUserCount = concurrentUserCount;
	}
	public void setOAuthScopeIds(long[] oAuthScopeIds) {
		this.oAuthScopeIds = oAuthScopeIds;
	}
	public Set<String> getAuthorizedGrantTypes() {
		Set<String> authorizedGrantTypesSet = new HashSet<String>();
		for (AuthorizedGrantTypeMapping authorizedGrantTypeMapping : authorizedGrantTypeMappings)
			if (authorizedGrantTypeMapping.getAuthorizedGrantType() != null) {
				authorizedGrantTypesSet.add(authorizedGrantTypeMapping
						.getAuthorizedGrantType().getGrantType());
			}
		return authorizedGrantTypesSet;
	}

	public Integer getAccessTokenValiditySeconds() {
		return accessTokenValiditySeconds;
	}

	public Map<String, Object> getAdditionalInformation() {
		return new LinkedHashMap<String, Object>();
	}

	public void setHashKey(String hashKey) {
		this.hashKey = hashKey;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
		this.accessTokenValiditySeconds = accessTokenValiditySeconds;
	}

	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValiditySeconds;
	}

	public void setRefreshTokenValiditySeconds(
			Integer refreshTokenValiditySeconds) {
		this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
	}

	public void setAuthorizedGrantTypes(
			List<AuthorizedGrantTypeMapping> authorizedGrantTypes) {
		this.authorizedGrantTypeMappings = authorizedGrantTypes;
	}

	public List<AuthorizedGrantTypeMapping> getAuthorizedGrantTypeMappings() {
		return authorizedGrantTypeMappings;
	}

	public void setAuthorizedGrantTypeMappings(
			List<AuthorizedGrantTypeMapping> authorizedGrantTypeMappings) {
		this.authorizedGrantTypeMappings = authorizedGrantTypeMappings;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getPassPhrase() {
		return passPhrase;
	}

	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}

	public String getMailId() {
		return mailId;
	}

	public String getEncryptedSecret() {
		return encryptedSecret;
	}

	public void setEncryptedSecret(String encryptedSecret) {
		this.encryptedSecret = encryptedSecret;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}



	public long[] getAuthorizedGrantTypeIds() {
		return authorizedGrantTypeIds;
	}

	public void setAuthorizedGrantTypeIds(long[] authorizedGrantTypeIds) {
		this.authorizedGrantTypeIds = authorizedGrantTypeIds;
	}
	
	public Map<Long, String> getMappedApis() {
		return mappedApis;
	}

	public void setMappedApis(Map<Long, String> mappedApis) {
		this.mappedApis = mappedApis;
	}

	
	public Boolean getIsInternal() {
		
		return isInternal==null?Boolean.FALSE:isInternal;
	}

	public void setIsInternal(Boolean isInternal) {
		this.isInternal = isInternal;
	}

	public Boolean getIsClientSecretChangeRequired() {
		return isClientSecretChangeRequired;
	}

	public void setIsClientSecretChangeRequired(Boolean isClientSecretChangeRequired) {
		this.isClientSecretChangeRequired = isClientSecretChangeRequired;
	}
	public List<APIDetails> getMappedAPIs() {
		return mappedAPIs;
	}

	public void setMappedAPIs(List<APIDetails> mappedAPIs) {
		this.mappedAPIs = mappedAPIs;
	}
	public List<IPAddressRange> getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(List<IPAddressRange> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}

	public List<User> getTrustedUsers() {
		return trustedUsers;
	}

	public void setTrustedUsers(List<User> trustedUsers) {
		this.trustedUsers = trustedUsers;
	}
	public String getIdpClientId() {
		return idpClientId;
	}

	public void setIdpClientId(String idpClientId) {
		this.idpClientId = idpClientId;
	}

	public String getIdpClientSecret() {
		return idpClientSecret;
	}

	public void setIdpClientSecret(String idpClientSecret) {
		this.idpClientSecret = idpClientSecret;
	}
	
	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		OauthClientDetails trustedSource = (OauthClientDetails) baseEntity;
		super.populate(trustedSource, cloneOptions);
		trustedSource.setClientId(clientId);
		trustedSource.setClientSecret(clientSecret);
		trustedSource.setEncryptedSecret(encryptedSecret);
		trustedSource
				.setAccessTokenValiditySeconds(accessTokenValiditySeconds);

		trustedSource.setAuthorizedGrantTypeIds(authorizedGrantTypeIds);
		trustedSource.setPassPhrase(passPhrase);
		trustedSource
				.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);

		trustedSource.setMailId(mailId);
		
		
		trustedSource.setIpAddresses(ipAddresses);
		trustedSource.setMappedAPIs(mappedAPIs);
		trustedSource.setTrustedUsers(trustedUsers);
		trustedSource.setIsInternal(isInternal);
		
		trustedSource.setIsClientSecretChangeRequired(isClientSecretChangeRequired);
		trustedSource.setHashKey(hashKey);
		trustedSource.setNamedUserCount(namedUserCount);
		trustedSource.setConcurrentUserCount(concurrentUserCount);
		if (notNull(authorizedGrantTypeMappings)
				&& !authorizedGrantTypeMappings.isEmpty()) {
			List<AuthorizedGrantTypeMapping> cloneAuthorizedGrantTypeMapping = new ArrayList<AuthorizedGrantTypeMapping>();
			for (AuthorizedGrantTypeMapping authorizedGrantTypeMapping : authorizedGrantTypeMappings) {
				cloneAuthorizedGrantTypeMapping
						.add((AuthorizedGrantTypeMapping) authorizedGrantTypeMapping
								.cloneYourself(cloneOptions));
			}
			trustedSource
					.setAuthorizedGrantTypeMappings(cloneAuthorizedGrantTypeMapping);
		}
		if (notNull(oauthScopeList)  && !oauthScopeList.isEmpty()) {
			List<OauthScopeMapping> cloneOauthScopeList = new ArrayList<OauthScopeMapping>();
			for (OauthScopeMapping oauthScope : oauthScopeList) {
				cloneOauthScopeList.add((OauthScopeMapping) oauthScope
						.cloneYourself(cloneOptions));
			}
			trustedSource.setOauthScopeList(cloneOauthScopeList);
		}
	}

	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		OauthClientDetails trustedSource = (OauthClientDetails) baseEntity;
		super.populateFrom(trustedSource, cloneOptions);
		this.setClientId(trustedSource.getClientId());
		this.setClientSecret(trustedSource.getClientSecret());
		this.setEncryptedSecret(trustedSource.getEncryptedSecret());
		this.setAccessTokenValiditySeconds(trustedSource
				.getAccessTokenValiditySeconds());
		this.setHashKey(trustedSource.getHashKey());
		this.setPassPhrase(trustedSource.getPassPhrase());
		this.setAuthorizedGrantTypeIds(trustedSource
				.getAuthorizedGrantTypeIds());
		this.setRefreshTokenValiditySeconds(trustedSource
				.getRefreshTokenValiditySeconds());
        this.setConcurrentUserCount(trustedSource.getConcurrentUserCount());
        this.setIpAddresses(trustedSource.getIpAddresses());
        this.setTrustedUsers(trustedSource.getTrustedUsers());
        this.setIsInternal(trustedSource.getIsInternal());
        this.setMappedAPIs(trustedSource.getMappedAPIs());
        this.setIsClientSecretChangeRequired(trustedSource.getIsClientSecretChangeRequired());
        this.setNamedUserCount(trustedSource.getNamedUserCount());
		this.setMailId(trustedSource.getMailId());
		this.getAuthorizedGrantTypeMappings().clear();
		if (notNull(trustedSource.getAuthorizedGrantTypeMappings())
				&& trustedSource.getAuthorizedGrantTypeMappings().size() > 0) {
			
			for (AuthorizedGrantTypeMapping authorizedGrantTypeMapping : trustedSource
					.getAuthorizedGrantTypeMappings()) {
				this.getAuthorizedGrantTypeMappings()
						.add(authorizedGrantTypeMapping != null ? (AuthorizedGrantTypeMapping) authorizedGrantTypeMapping
								.cloneYourself(cloneOptions) : null);
			}
		}
		this.getOauthScopeList().clear();
		if (notNull(trustedSource.getOauthScopeList() )) {
			
			for (OauthScopeMapping OauthScopeMapping : trustedSource
					.getOauthScopeList()) {
				this.getOauthScopeList()
						.add(OauthScopeMapping != null ? (OauthScopeMapping) OauthScopeMapping
								.cloneYourself(cloneOptions) : null);
			}
		}

	}

	
	
}


