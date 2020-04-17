package com.nucleus.security.oauth.dao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.nucleus.common.util.NeutrinoSerializationUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.BaseDaoImpl;
import com.nucleus.persistence.EntityDao;
import com.nucleus.security.oauth.domainobject.OauthAccessToken;
import com.nucleus.security.oauth.domainobject.OauthRefreshToken;

public class CustomOauthTokenStoreDAO extends BaseDaoImpl<BaseEntity>
	implements TokenStore {
	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

	@Override
	@Transactional(readOnly=true)
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		OAuth2Authentication oAuth2Authentication = null;

		oAuth2Authentication = readAuthentication(token.getValue());

		return oAuth2Authentication;

	}

	@Override
	@Transactional(readOnly=true)
	public OAuth2Authentication readAuthentication(String token) {

		OauthAccessToken oauthAccessToken = findTokensByTokenId(token);
		OAuth2Authentication oauth2Authentication = null;
		try {
			if(oauthAccessToken!=null)
			{
			oauth2Authentication = NeutrinoSerializationUtils.deserialize(IOUtils
					.toByteArray(oauthAccessToken.getAuthentication()
							.getBinaryStream()));
			}
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();
		} catch (SQLException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

		}

		return oauth2Authentication;
	}

	@Override
	@Transactional
	public void storeAccessToken(OAuth2AccessToken token,
			OAuth2Authentication authentication) {

		Session session = (Session) getEntityManager().getDelegate();

		String refreshToken = null;
		if (token.getRefreshToken() != null) {
			refreshToken = token.getRefreshToken().getValue();
		}

		if (readAccessToken( token.getValue()) != null) {
			removeAccessToken(token.getValue());
		}
		OauthAccessToken oAuthAccessToken = new OauthAccessToken();
		oAuthAccessToken.setTokenId(extractTokenKey(token.getValue()));
		oAuthAccessToken.setToken(session.getLobHelper().createBlob(
				SerializationUtils.serialize(token)));
		oAuthAccessToken.setAuthenticationId(authenticationKeyGenerator
				.extractKey(authentication));
		oAuthAccessToken.setAuthentication(session.getLobHelper().createBlob(
				SerializationUtils.serialize(authentication)));
		oAuthAccessToken.setRefreshToken(extractTokenKey(refreshToken));
		oAuthAccessToken.setUserName(authentication.isClientOnly() ? null: authentication.getName());
		if(AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getUserAuthentication().getClass())){
			oAuthAccessToken.setAnonymous(Boolean.TRUE);
			
		}
		entityDao.saveOrUpdate(oAuthAccessToken);

	}

	@Override
	@Transactional(readOnly=true)
	public OAuth2AccessToken readAccessToken(String tokenValue) {

		OauthAccessToken oauthAccessToken = findTokensByTokenId(tokenValue);

		OAuth2AccessToken oAuth2AccessToken = null;
		try {
			if(oauthAccessToken!=null){
			oAuth2AccessToken = NeutrinoSerializationUtils
					.deserialize(IOUtils.toByteArray(oauthAccessToken
							.getToken().getBinaryStream()));
			}
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

		} catch (SQLException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

		}
		return oAuth2AccessToken;
	}

	@Override
	@Transactional
	public void removeAccessToken(OAuth2AccessToken token) {
		removeAccessToken(token.getValue());

	}

	private void removeAccessToken(String token) {

		OauthAccessToken oauthAccessToken = findTokensByTokenId(token);
		if(oauthAccessToken!=null)
		entityDao.delete(oauthAccessToken);
	}
	
	@Transactional
	public void removeAccessTokensByUserName(String username) {

		List<OauthAccessToken> oauthAccessTokenList = findOauthTokensListByUserName(username);
		if(oauthAccessTokenList != null ){
			for(OauthAccessToken oauthAccessToken:oauthAccessTokenList){
				entityDao.delete(oauthAccessToken);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<OauthAccessToken> findOauthTokensListByUserName(String username) {
		Query query = getEntityManager().createNamedQuery("getOauthAccessTokenDetailsByUserName");
		query.setParameter("userName", username);
		
		return query.getResultList();
	}

	@Override
	@Transactional
	public void storeRefreshToken(OAuth2RefreshToken refreshToken,
			OAuth2Authentication authentication) {
		Session session = (Session) getEntityManager().getDelegate();
		OauthRefreshToken oauthRefreshToken = new OauthRefreshToken();
		oauthRefreshToken.setAuthentication(session.getLobHelper().createBlob(
				SerializationUtils.serialize(authentication)));
		oauthRefreshToken.setRefreshTokenId(extractTokenKey(refreshToken
				.getValue()));
		oauthRefreshToken.setRefreshToken(session.getLobHelper().createBlob(
				SerializationUtils.serialize(refreshToken)));
		entityDao.saveOrUpdate(oauthRefreshToken);

	}

	@Override
	@Transactional(readOnly=true)
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		OauthRefreshToken oauthRefreshToken = findRefreshTokensByTokenId(tokenValue);

		OAuth2RefreshToken oAuth2RefreshToken = null;
		try {
			if(oauthRefreshToken!=null)
			{
			oAuth2RefreshToken = NeutrinoSerializationUtils.deserialize(IOUtils
					.toByteArray(oauthRefreshToken.getRefreshToken()
							.getBinaryStream()));
			}
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

		} catch (SQLException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

		}
		return oAuth2RefreshToken;

	}

	@Override
	@Transactional(readOnly=true)
	public OAuth2Authentication readAuthenticationForRefreshToken(
			OAuth2RefreshToken token) {
		OauthRefreshToken oauthRefreshToken = findRefreshTokensByTokenId(token
				.getValue());

		OAuth2Authentication oauth2Authentication = null;
		try {
			if(oauthRefreshToken!=null)
			{
			oauth2Authentication = NeutrinoSerializationUtils.deserialize(IOUtils
					.toByteArray(oauthRefreshToken.getAuthentication()
							.getBinaryStream()));
			}
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

		} catch (SQLException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

		}

		return oauth2Authentication;
	}

	@Override
	@Transactional
	public void removeRefreshToken(OAuth2RefreshToken token) {
		if(token!=null)
		{
		OauthRefreshToken oauthRefreshToken = findRefreshTokensByTokenId(token.getValue());
		if(oauthRefreshToken!=null)
		{
		entityDao.delete(oauthRefreshToken);
		}
		}
	}

	@Override
	@Transactional(readOnly=true)
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		OAuth2AccessToken oAuth2AccessToken = null;
		OauthAccessToken oauthAccessToken = null;

		String key = authenticationKeyGenerator.extractKey(authentication);

		oauthAccessToken = findTokensByAuthId(key);

		try {
			if(oauthAccessToken!=null)
			{
			oAuth2AccessToken = NeutrinoSerializationUtils
					.deserialize(IOUtils.toByteArray(oauthAccessToken
							.getToken().getBinaryStream()));
			}
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

		} catch (SQLException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

		}
		if (oAuth2AccessToken != null
				&& !key.equals(authenticationKeyGenerator
						.extractKey(readAuthentication(oAuth2AccessToken
								.getValue())))) {
			removeAccessToken(oAuth2AccessToken.getValue());
			// Keep the store consistent (maybe the same user is represented by
			// this authentication but the details have
			// changed)
			storeAccessToken(oAuth2AccessToken, authentication);
		}
		return oAuth2AccessToken;
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(
			String clientId, String userName) {
		List<OAuth2AccessToken> oAuth2AccessTokenList = new LinkedList<OAuth2AccessToken>();
		List<OauthAccessToken> accessTokenList = null;
		Query query = getEntityManager().createNamedQuery(
				"getOauthAccessTokenDetailsByUserNameAndClientID");

		query.setParameter("clientId", clientId);
		query.setParameter("UserName", userName);
		accessTokenList = query.getResultList();
		for (OauthAccessToken accessToken : accessTokenList) {
			try {
				oAuth2AccessTokenList
						.add((OAuth2AccessToken) NeutrinoSerializationUtils
								.deserialize(IOUtils.toByteArray(accessToken
										.getToken().getBinaryStream())));
			} catch (IOException e) {
				BaseLoggers.exceptionLogger.debug(e.getMessage());
				throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

			} catch (SQLException e) {
				BaseLoggers.exceptionLogger.debug(e.getMessage());
				throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();
			}
		}
		return oAuth2AccessTokenList;
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		List<OAuth2AccessToken> oAuth2AccessTokenList = new LinkedList<OAuth2AccessToken>();
		List<OauthAccessToken> accessTokenList = null;
		Query query = getEntityManager().createNamedQuery(
				"getOauthAccessTokenDetailsByClientID");

		query.setParameter("clientId", clientId);
		accessTokenList = query.getResultList();
		for (OauthAccessToken accessToken : accessTokenList) {
			try {
				oAuth2AccessTokenList
						.add((OAuth2AccessToken) NeutrinoSerializationUtils
								.deserialize(IOUtils.toByteArray(accessToken
										.getToken().getBinaryStream())));
			} catch (IOException e) {
				BaseLoggers.exceptionLogger.debug(e.getMessage());
				throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();

			} catch (SQLException e) {
				BaseLoggers.exceptionLogger.debug(e.getMessage());
				throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();
			}
		}
		return oAuth2AccessTokenList;

	}
	@Transactional(readOnly=true)
	public List<OAuth2AccessToken> findTokensByUserName(String username)
			 {
		List<OAuth2AccessToken> oAuth2AccessTokenList = new LinkedList<OAuth2AccessToken>();
		List<OauthAccessToken> accessTokenList = null;
		Query query = getEntityManager().createNamedQuery(
				"getOauthAccessTokenDetailsByUserName");

		query.setParameter("userName", username);
		accessTokenList = query.getResultList();
		try {
			for (OauthAccessToken accessToken : accessTokenList) {
			
					oAuth2AccessTokenList.add((OAuth2AccessToken) NeutrinoSerializationUtils
							.deserialize(IOUtils.toByteArray(accessToken.getToken()
									.getBinaryStream())));
				
			}
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.error(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();
		} catch (SQLException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();
		}
		return oAuth2AccessTokenList;
	}
	@Transactional(readOnly=true)
	public List<OAuth2AccessToken> findTokensByUserNameWithClient(String username,String clientId)
			 {
		List<OAuth2AccessToken> oAuth2AccessTokenList = new LinkedList<OAuth2AccessToken>();
		List<OauthAccessToken> accessTokenList = null;
		Query query = getEntityManager().createNamedQuery(
				"getOauthAccessTokenDetailsByUserNameWithDiffClient");

		query.setParameter("userName", username);
		query.setParameter("clientId", clientId);
		accessTokenList = query.getResultList();
		try {
			for (OauthAccessToken accessToken : accessTokenList) {
				
					oAuth2AccessTokenList.add((OAuth2AccessToken) NeutrinoSerializationUtils
							.deserialize(IOUtils.toByteArray(accessToken.getToken()
									.getBinaryStream())));
				
			}
		} catch (IOException e) {
			BaseLoggers.exceptionLogger.error(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();
		} catch (SQLException e) {
			BaseLoggers.exceptionLogger.debug(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class,"read.authentication.failed",e.getMessage()).setOriginalException(e).build();
		}
	
		return oAuth2AccessTokenList;
	}
	@Transactional(readOnly=true)
	public int findActiveTokensForExternalClients(String clientId)
			 {
		int count  = 0;
		List<OauthAccessToken> accessTokenList = null;
		Query query = getEntityManager().createNamedQuery(
				"getAllAnonymousOauthAccessTokenDetails");
		query.setParameter("clientId", clientId);
		accessTokenList = query.getResultList();
	
			for (OauthAccessToken accessToken : accessTokenList) {
				
				count++;
			}
		
	
		return count;
	}
	
	@Transactional(readOnly=true)
	public int findActiveTokensCountByUserNameWithOtherClients(String username,String clientId)
			 {
		int count=0;
		List<OAuth2AccessToken> oAuth2AccessTokenList=	findTokensByUserNameWithClient(username,clientId);
		for(OAuth2AccessToken oAuth2AccessToken:oAuth2AccessTokenList)
		{
			if(!oAuth2AccessToken.isExpired())
			{
				count++;
			}
		}
		return count;
	}
	@Transactional(readOnly=true)
	public int findActiveTokensCountByUserName(String username) {
		int count=0;
		List<OAuth2AccessToken> oAuth2AccessTokenList=	findTokensByUserName(username);
		for(OAuth2AccessToken oAuth2AccessToken:oAuth2AccessTokenList)
		{
			if(!oAuth2AccessToken.isExpired())
			{
				count++;
			}
		}
		return count;
	}
	
	@Transactional(readOnly=true)
	private OauthAccessToken findTokensByTokenId(String tokenID) {
		OauthAccessToken accessToken = null;
		Query query = getEntityManager().createNamedQuery(
				"getOauthAccessTokenDetailsByTokenID");

		query.setParameter("tokenId",extractTokenKey( tokenID));
		List accessTokenList= query.getResultList();
		if(!CollectionUtils.isEmpty(accessTokenList))
			accessToken = (OauthAccessToken) accessTokenList.get(0);
		return accessToken;
	}

	private OauthAccessToken findTokensByAuthId(String authID) {
		OauthAccessToken accessToken = null;
		Query query = getEntityManager().createNamedQuery(
				"getOauthAccessTokenDetailsByAuthID");

		query.setParameter("authenticationId", authID);
		List accessTokenList= query.getResultList();
		if(! CollectionUtils.isEmpty(accessTokenList))
			accessToken = (OauthAccessToken) accessTokenList.get(0);
		return accessToken;
	}
	public OauthAccessToken findTokensByRefreshToken(String refreshToken) {
		OauthAccessToken accessToken = null;
		Query query = getEntityManager().createNamedQuery(
				"getOauthAccessTokenDetailsByrefreshToken");

		query.setParameter("refreshToken",extractTokenKey(refreshToken));
		List accessTokenList= query.getResultList();
		if(!  CollectionUtils.isEmpty(accessTokenList))
			accessToken = (OauthAccessToken) accessTokenList.get(0);
		return accessToken;
	}
	private OauthRefreshToken findRefreshTokensByTokenId(String tokenID) {
		OauthRefreshToken oauthRefreshToken = null;
		Query query = getEntityManager().createNamedQuery(
				"getOauthRefreshTokenDetailsByTokenID");

		
		query.setParameter("refreshTokenId",extractTokenKey(tokenID));
		if(!  query.getResultList().isEmpty())
			oauthRefreshToken = (OauthRefreshToken) query.getResultList().get(0);
		return oauthRefreshToken;
	}

	protected String extractTokenKey(String value) {
		if (value == null) {
			return null;
		}
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(
					"SHA-256 algorithm not available.  Fatal (should be in the JDK).");
		}

		try {
			byte[] bytes = digest.digest(value.getBytes("UTF-8"));
			return String.format("%064x", new BigInteger(1, bytes));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(
					"UTF-8 encoding not available.  Fatal (should be in the JDK).");
		}
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(
			OAuth2RefreshToken refreshToken) {
		if(refreshToken!=null)
		{
		removeAccessTokenUsingRefreshToken(refreshToken.getValue());
		}
	}
	private void removeAccessTokenUsingRefreshToken(String refreshToken) {
		OauthAccessToken oauthAccessToken = findTokensByRefreshToken(refreshToken);
		if(oauthAccessToken!=null)
		entityDao.delete(oauthAccessToken);
		
		
	}

}
