package com.nucleus.security.oauth.service;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MultiValueMap;

import com.nucleus.core.misc.util.ExceptionUtility;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.oauth.config.OauthConfig;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.security.oauth.PasswordEncodingUtil;
import com.nucleus.security.oauth.businessobject.RestfulTokenBusinessObject;
import com.nucleus.security.oauth.businessobject.RevokeTokenDTO;
import com.nucleus.security.oauth.constants.RESTfulSecurityConstants;
import com.nucleus.security.oauth.domainobject.OauthTokenDetails;
import com.nucleus.security.oauth.util.TokenUtility;
import com.nucleus.security.oauth.vo.OauthTokenDetailsVo;

@Named("oauthauthenticationService")
@Configuration
public class RESTfulAuthenticationServiceImpl implements RESTfulAuthenticationService {

	@Value("${INTG_BASE_URL}/oauth/token")
	private String tokenUrl;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("tokenBusinessObject")
	private RestfulTokenBusinessObject tokenBusinessObject;

	@Inject
	@Named("tokenUtil")
	private TokenUtility tokenUtil;

	@Inject
	@Named("oauthTokenDetailsPopulator")
	private FWCachePopulator oauthTokenDetailsPopulator;
	
	@Inject
	@Named("passwordEncodingUtil")
	private PasswordEncodingUtil passwordEncoder;

	@Override
	public String getSecurityToken(String clientId) {

		OauthTokenDetailsVo tokenDetailsFromRedis = (OauthTokenDetailsVo) oauthTokenDetailsPopulator.get(clientId);

		if (tokenDetailsFromRedis != null) {
			boolean isExpired = tokenUtil.isExpiredToken(tokenDetailsFromRedis);
			if (!isExpired) {
				return tokenDetailsFromRedis.getToken();
			}
		}

		tokenDetailsFromRedis = fetchOauthTokenDetails(clientId);
		oauthTokenDetailsPopulator.update(Action.UPDATE, clientId, tokenDetailsFromRedis);
		return tokenDetailsFromRedis.getToken();

	}
	
	@Override
	public OauthTokenDetailsVo getSecurityToken(String clientId, String userName, String password) {
		
		OauthTokenDetailsVo tokenDetailsFromRedis = (OauthTokenDetailsVo) oauthTokenDetailsPopulator.get(clientId);

		if (tokenDetailsFromRedis != null) {
			boolean isExpired = tokenUtil.isExpiredToken(tokenDetailsFromRedis);
			if (!isExpired) {
				return tokenDetailsFromRedis;
			}
		}

		tokenDetailsFromRedis = fetchOauthTokenDetails(clientId, userName, password);
		oauthTokenDetailsPopulator.update(Action.UPDATE, clientId, tokenDetailsFromRedis);
		return tokenDetailsFromRedis;

	}
	
	@Override
	public OauthTokenDetailsVo getSecurityToken(String clientId, String refreshToken) {
		OauthTokenDetailsVo newTokenDetails = fetchOauthTokenDetails(clientId, refreshToken);
		oauthTokenDetailsPopulator.update(Action.UPDATE, clientId, newTokenDetails);
		return newTokenDetails;
	}

	@Override
	public Map<String, List<String>> getLoggedInUsersTrustedSourceDetails(String url, String clientId) {
		Map<String, List<String>> modules = null;
		String token = getSecurityToken(clientId);
		try {
			modules = tokenBusinessObject.getLoggedInUsersTrustedSourceDetails(url, token, clientId);
		} catch (SystemException se) {
			BaseLoggers.exceptionLogger
					.error(RESTfulSecurityConstants.GET_LOGGED_IN_USERS_TRUSTED_SOURCE_ERROR_MSG + clientId, se);

		}
		return modules;
	}

	@Override
	public String revokeTokenByUsers(String url, String clientId, List<String> usernames) {
		String result = "";
		String token = getSecurityToken(clientId);
		try {
			RevokeTokenDTO revokeTokendto = new RevokeTokenDTO();
			revokeTokendto.setClientID(clientId);
			revokeTokendto.setUsernameList(usernames);
			revokeTokendto.setAccessToken(token);

			result = tokenBusinessObject.revokeTokenByUsers(url, token, revokeTokendto);
		} catch (SystemException se) {
			BaseLoggers.exceptionLogger.error(RESTfulSecurityConstants.REVOKE_TOKENS_BY_USER_ERROR_MSG + clientId, se);
		}
		return result;
	}

	public OauthTokenDetailsVo fetchOauthTokenDetails(String clientId) {
		OauthConfig oauthConfig = getOauthConfigByClientId(clientId);
		String clientSecret = oauthConfig.getClientSecret();
		MultiValueMap<String, String> requestParam = null;

		requestParam = tokenBusinessObject.prepareRequestParamForAnonymousGrant(clientId, clientSecret);
		OauthTokenDetails tokenDetails = getAccessToken(requestParam, clientId, null);
		return new OauthTokenDetailsVo(tokenDetails);
	}
	
	public OauthTokenDetailsVo fetchOauthTokenDetails(String clientId, String userName, String password) {
		OauthConfig oauthConfig = getOauthConfigByClientId(clientId);
		String clientSecret = oauthConfig.getClientSecret();
		String passPhrase = oauthConfig.getPassword();
		String encryptedPassword = encryptPasswordWithPassphrase(password, passPhrase);

		MultiValueMap<String, String> requestParam = null;

		requestParam = tokenBusinessObject.prepareRequestParamForPassGrant(userName, clientId, clientSecret,
				encryptedPassword);
		OauthTokenDetails tokenDetails = getAccessToken(requestParam, clientId, userName);
		return new OauthTokenDetailsVo(tokenDetails);
	}

	private String encryptPasswordWithPassphrase(String password, String passPhrase) {
		return passwordEncoder.encryptPassword(password, passPhrase);
	}

	public OauthTokenDetailsVo fetchOauthTokenDetails(String clientId, String refreshToken) {
		OauthConfig oauthConfig = getOauthConfigByClientId(clientId);
		String clientSecret = oauthConfig.getClientSecret();
		MultiValueMap<String, String> requestParam = null;

		requestParam = tokenBusinessObject.prepareRequestParamForRefreshTokenGrant(refreshToken, clientId,
				clientSecret);
		OauthTokenDetails tokenDetails = getAccessToken(requestParam, clientId, null);
		return new OauthTokenDetailsVo(tokenDetails);
	}

	private OauthConfig getOauthConfigByClientId(String clientId) {
		NamedQueryExecutor<OauthConfig> executor = new NamedQueryExecutor<>("getActiveOauthConfigByClientId");
		executor.addParameter("clientId", clientId).addParameter("approvalStatusList",
				ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
		executor.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, true);
		OauthConfig existingOauthConfig = null;
		try {
			existingOauthConfig = entityDao.executeQueryForSingleValue(executor);
		} catch (SystemException se) {
			BaseLoggers.exceptionLogger.error("More than one OauthConfig property is configured for the clientId: {}",
					clientId);
			throw new SystemException("More than one OauthConfig property is configured for the clientId: " + clientId,
					se);
		}
		if (existingOauthConfig == null) {
			BaseLoggers.exceptionLogger.error("No OauthConfig property is configured for the clientId: {}", clientId);
			throw new SystemException(RESTfulSecurityConstants.CLIENT_ID_NOT_CONFIGURED_MSG + clientId);
		}
		return existingOauthConfig;
	}
	
	private OauthTokenDetails getAccessToken(MultiValueMap<String, String> requestParams, String clientId,
			String username) {

		try {
			return tokenBusinessObject.getAccessToken(tokenUrl, requestParams, clientId, username);
		} catch (SystemException se) {
			BaseLoggers.exceptionLogger.error(RESTfulSecurityConstants.GET_ACCESS_TOKEN_ERROR_MSG + clientId);
			ExceptionUtility.rethrowSystemException(se);
		}

		return null;
	}

}
