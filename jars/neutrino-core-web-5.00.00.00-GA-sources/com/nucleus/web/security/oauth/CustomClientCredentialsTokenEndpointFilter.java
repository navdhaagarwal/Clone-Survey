package com.nucleus.web.security.oauth;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.nucleus.core.misc.util.PasswordEncryptorUtil;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.oauth.TrustedSourceInfo;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.security.oauth.service.TrustedSourceService;
import com.nucleus.security.oauth.util.TrustedSourceHelper;
import com.nucleus.web.security.AesUtil;

public class CustomClientCredentialsTokenEndpointFilter extends ClientCredentialsTokenEndpointFilter{
	@Inject
	@Named("clientDetails")
	private TrustedSourceService trustedSourceService;
	@Inject
	@Named("trustedSourceHelper")
	private  TrustedSourceHelper trustedSourceHelper;
	@Autowired
	OauthConcurrentSessionControlStrategy oauthConcurrentSessionControlStrategy;
	public TrustedSourceService getTrustedSourceService() {
		return trustedSourceService;
	}
	public void setTrustedSourceService(
			TrustedSourceService trustedSourceService) {
		this.trustedSourceService = trustedSourceService;
	}
	public OauthConcurrentSessionControlStrategy getOauthConcurrentSessionControlStrategy() {
		return oauthConcurrentSessionControlStrategy;
	}
	public void setOauthConcurrentSessionControlStrategy(
			OauthConcurrentSessionControlStrategy oauthConcurrentSessionControlStrategy) {
		this.oauthConcurrentSessionControlStrategy = oauthConcurrentSessionControlStrategy;
	}
	private boolean allowOnlyPost = false;
	public boolean isAllowOnlyPost() {
		return allowOnlyPost;
	}
	public void setAllowOnlyPost(boolean allowOnlyPost) {
		this.allowOnlyPost = allowOnlyPost;
	}
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
	
		
		if (allowOnlyPost && !"POST".equalsIgnoreCase(request.getMethod())) {
			throw new HttpRequestMethodNotSupportedException(request.getMethod(), new String[] { "POST" });
		}

		// If the request is already authenticated we can assume that this
		// filter is not needed
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			return authentication;
		}

		String clientId = request.getParameter("client_id");
		if (clientId == null) {
			throw new BadCredentialsException("No client credentials presented");
		}
		clientId = clientId.trim();

		if(TrustedSourceHelper.isInternalModule(clientId) && !trustedSourceHelper.isLicensedModule(clientId)){
			throw new BadCredentialsException("Module " + clientId + " is not licensed. Please activate license.");
		}
		String inputClientSecret = request.getParameter("client_secret");
		String inputClientSecretSH6=null;
		
        TrustedSourceInfo trustedSourceVO=(TrustedSourceInfo) trustedSourceService.loadClientByClientId(clientId);
        String passPhrase=trustedSourceVO.getPassPhrase();
        passPhrase=AesUtil.Decrypt(passPhrase,OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE);
        inputClientSecret=AesUtil.Decrypt(inputClientSecret,passPhrase );
        		//hash the plain secret
		String hashKey=trustedSourceVO.getHashKey();


		try {
			if (ValidatorUtils.notNull(hashKey)) {
				inputClientSecretSH6 = PasswordEncryptorUtil.encryptPassword(inputClientSecret, hashKey);
			}
		} catch (NoSuchAlgorithmException e) {
			BaseLoggers.exceptionLogger.error(e.getMessage());
			throw ExceptionBuilder.getInstance(SystemException.class, "hash.algorithm.not.available", e.getMessage()).setOriginalException(e).build();
		}

		if (inputClientSecretSH6 == null) {
			inputClientSecretSH6 = "";
		}


		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(clientId,
				inputClientSecretSH6);
		try{
			authentication= this.getAuthenticationManager().authenticate(authRequest);
		}
		catch (BadCredentialsException e) {
			try {
				inputClientSecret = PasswordEncryptorUtil.encryptPasswordMD5(inputClientSecret, hashKey);
			} catch (NoSuchAlgorithmException e1) {
				BaseLoggers.exceptionLogger.error(e1.getMessage());
				throw ExceptionBuilder.getInstance(SystemException.class, "hash.algorithm.not.available", e1.getMessage()).setOriginalException(e1).build();
			}
			UsernamePasswordAuthenticationToken authRequestForMD5 = new UsernamePasswordAuthenticationToken(clientId,
					inputClientSecret);
			try{
				authentication= this.getAuthenticationManager().authenticate(authRequestForMD5);
			} catch(Exception ex) {
				BaseLoggers.exceptionLogger.error(ex.getMessage(), ex);
			}
		}
		return authentication;
	}

}
