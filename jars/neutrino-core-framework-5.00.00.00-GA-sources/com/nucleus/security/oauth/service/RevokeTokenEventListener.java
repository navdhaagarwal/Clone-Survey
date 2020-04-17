package com.nucleus.security.oauth.service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.pubsub.PubSubService;

@Named("revokeTokenEventListener")
public class RevokeTokenEventListener {
	
	@Inject
	@Named("pubSubService")
	private PubSubService pubSubService;
	
	@Inject
	@Named("oauthTokenDetailsPopulator")
	private FWCachePopulator oauthTokenDetailsPopulator;
	
	@PostConstruct
	public void init() {
		pubSubService.subscribeToTopic("API_REVOKE_TOKEN_TOPIC", clientId->
			oauthTokenDetailsPopulator.update(Action.DELETE, clientId,null));
	}

}
