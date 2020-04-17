package com.nucleus.security.oauth.populator;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.security.oauth.service.RESTfulAuthenticationServiceImpl;


@Named("oauthTokenDetailsPopulator")
public class OauthTokenDetailsPopulator extends  FWCachePopulator{
	
	@Inject
	@Named("oauthauthenticationService")
	private RESTfulAuthenticationServiceImpl restfulAuthenticationRedisImpl;

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.OAUTH_TOKEN_DETAILS_CACHE;
	}

	@Override
	public void init() {
		// no initialization required
	}

	@Override
	public Object fallback(Object key) {
		return restfulAuthenticationRedisImpl.fetchOauthTokenDetails((String)key);
		
	}

	@Override
	public void build(Long tenantId) {
		// build not required for this cache
	}

	
	@Override
	public void update(Action action, Object key, Object value) {
		if(action.equals(Action.UPDATE) || action.equals(Action.INSERT)) {
		put(key,value);
		}
		else if(action.equals(Action.DELETE)) {
			remove(key);
		}
		
		
	}

	@Override
	public void update(Action action, Object object) {
		Map map  = (Map<Object, Object>) object;
		Map.Entry entrySet = entrySet().iterator().next();
		update(action,entrySet.getKey(),entrySet.getValue());
		
	}

}
