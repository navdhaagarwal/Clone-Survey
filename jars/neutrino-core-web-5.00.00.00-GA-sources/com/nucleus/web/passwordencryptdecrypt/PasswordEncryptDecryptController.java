package com.nucleus.web.passwordencryptdecrypt;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.authenticationToken.AuthenticationTokenConstants;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.web.security.AesUtil;

@Controller
@RequestMapping(value = "/encryptPassword")
public class PasswordEncryptDecryptController {
  
  @PreAuthorize("hasAuthority('ENCRYPT_PWD')")
  @RequestMapping("/loadEncryptPasswordUI")
  public String loadEncryptEnteredPassword(ModelMap map) {
    return "passwordEncryptDecrypt";
  }
  
  @PreAuthorize("hasAuthority('ENCRYPT_PWD')")
  @RequestMapping(value = "/encryptUserPassword")
  @ResponseBody
  public Map<String, String> encryptUserPassword(String password, HttpServletRequest request) {
    String encryptedPassword = null;
    Map<String, String> map = new HashMap<String, String>();
    if (!password.isEmpty()) {
      encryptedPassword = AesUtil.encrypt(password, AuthenticationTokenConstants.NTLM_ENCRYPT_DECRYPT_PASS_PHRASE);
    }
    map.put("encryptedPassword", encryptedPassword);
    return map;
  }
  
  @PreAuthorize("hasAuthority('ENCRYPT_PWD')")
  @RequestMapping(value = "/encryptOAuthPassPhrase")
  @ResponseBody
  public Map<String, String> encryptPassPhrase(String passwordPhrase, HttpServletRequest request) {
    String encryptedPassPhrase = null;
    Map<String, String> map = new HashMap<String, String>();
    if (!passwordPhrase.isEmpty()) {
      encryptedPassPhrase = AesUtil.encrypt(passwordPhrase, OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE);
    }
    map.put("encryptedPassPhrase", encryptedPassPhrase);
    return map;
  }
  
}
