package com.nucleus.sso.utils;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.entity.SystemEntity;
import com.nucleus.security.core.session.AESEncryptionWithStaticKey;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

@Named("ssoConfigUtility")
public class SsoConfigUtility {
	
	@Inject
	@Named("userService")
	private  UserService userService;
	
	@Inject
    @Named("configurationService")
    private ConfigurationService        configurationService;
	
    @Value(value = "#{'${core.web.config.SSO.request.encryption.key}'}")
	private  String ssoEncryptionKey;
	
	/**
	 * 
	 * 
	 * @throws SystemException
	 * @param encryptedUsername
	 * @return
	 */
	public String decryptuserName(String encryptedUsername) {
		try {
			return AESEncryptionWithStaticKey.decrypt(encryptedUsername, ssoEncryptionKey.getBytes(Charset.forName("UTF-8")));
		} catch (Exception e) {
			throw new SystemException("Error decrypting request to get username.");
		}
	}
	
	public  boolean checkIfUserExists(String username){
		return userService.getUserFromUsername(username) != null;
	}
	
	public  boolean hasAuthority(String username, String authority){
		UserInfo userInfo = userService.getUserFromUsername(username);
		return userInfo.hasAuthority(authority);
	}
	public  Integer getMaxOTPResendCount(){
		
	int maxOTPResendCount=3;
		  ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(
			        SystemEntity.getSystemEntityId(), "config.max.resend.attempts");
	 	   if(configVo!=null){
	 		 
	 		  maxOTPResendCount=Integer.valueOf(configVo.getPropertyValue());
	 	   }
		 return maxOTPResendCount;
	}

}
