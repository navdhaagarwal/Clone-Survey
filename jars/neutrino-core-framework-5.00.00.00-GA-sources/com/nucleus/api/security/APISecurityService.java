package com.nucleus.api.security;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface APISecurityService {

	List<String> putSecurityKeysInCache(HttpSession session);
	
	void deleteProxyKeyFromCache(String proxySecurityKey);

	String getCookieIdBasedOnProxyId(String proxyId);

	Boolean checkCsrfToken(String cookieId, String csrfToken);
}
