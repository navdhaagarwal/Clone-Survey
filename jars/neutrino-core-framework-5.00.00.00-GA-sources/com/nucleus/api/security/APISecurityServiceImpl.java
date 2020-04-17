package com.nucleus.api.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;

@Named("apiSecurityService")
public class APISecurityServiceImpl implements APISecurityService{

	@Inject
	@Named("apiSecurityKeyCachePopulator")
	private NeutrinoCachePopulator apiSecurityKeyCachePopulator;

	@Value("${core.security.supplementary.base.path}")
	private String allowedOrigins;
	
	private static final String CSRF_TOKEN_FOR_SESSION_ATTR_NAME = "CSRF_TOKEN_FOR_SESSION_ATTR_NAME"; 
	
	@Override
	public List<String> putSecurityKeysInCache(HttpSession session) {
		
		List<String> proxyKeyList = new ArrayList<>();
		
		if(StringUtils.isEmpty(allowedOrigins)) {
			return proxyKeyList;
		}
		
		List<String> allowedOriginList = Arrays.asList(allowedOrigins.split(","));
		
		Map<String, Object> cacheMap = new HashMap<>();
		List<String> cookieIdList = new ArrayList<>();
		String csrfToken = (String) session.getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME);

		for(int i=0; i<allowedOriginList.size(); i++) {
			String proxyKey = CoreUtility.getUniqueId();
			String cookieId = CoreUtility.getUniqueId();
			
			cacheMap.put(proxyKey, cookieId);
			cacheMap.put(cookieId, csrfToken);
			
			cookieIdList.add(cookieId);
			
			String proxyUrl = allowedOriginList.get(i).concat("/app/externalService/getProxyCookie?proxyKey=").concat(proxyKey);
			proxyKeyList.add(proxyUrl);
		}
		
		cacheMap.put(session.getId(), cookieIdList);
		apiSecurityKeyCachePopulator.update(Action.INSERT, cacheMap);
		return proxyKeyList;
	}

	@Override
	public void deleteProxyKeyFromCache(String proxySecurityKey) {
		apiSecurityKeyCachePopulator.update(Action.DELETE, proxySecurityKey);
	}

	@Override
	public String getCookieIdBasedOnProxyId(String proxyId) {
		return (String) apiSecurityKeyCachePopulator.get(proxyId);
	}

	@Override
	public Boolean checkCsrfToken(String cookieId, String csrfToken) {
		String actualCsrfToken = (String) apiSecurityKeyCachePopulator.get(cookieId);
		return csrfToken.equals(actualCsrfToken);
	}


}
