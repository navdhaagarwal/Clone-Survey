package com.nucleus.core.ws.auth;

import org.apache.http.auth.NTCredentials;

import com.nucleus.authenticationToken.AuthenticationTokenConstants;
import com.nucleus.web.security.AesUtil;

public class CustomNTLMCredentials extends  NTCredentials{

	static String preProcessPassWord(String password) {
		String decryptedPwd=null;
		if(!password.isEmpty()){
			decryptedPwd=AesUtil.decrypt(password,AuthenticationTokenConstants.NTLM_ENCRYPT_DECRYPT_PASS_PHRASE,Boolean.TRUE);
		}		
		return decryptedPwd;
    }
	
	 public CustomNTLMCredentials(String userName, String password, String host,
	            String domain) {		 
		 super(userName,preProcessPassWord(password),host,domain);
	    }
}
