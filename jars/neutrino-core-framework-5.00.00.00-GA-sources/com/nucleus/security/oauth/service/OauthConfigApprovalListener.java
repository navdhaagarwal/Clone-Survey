package com.nucleus.security.oauth.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.oauth.config.OauthConfig;
import com.nucleus.persistence.EntityDao;

@Named("oauthConfigApprovalListener")
public class OauthConfigApprovalListener extends GenericEventListener{
	
	@Inject
	@Named("oauthTokenDetailsPopulator")
	private FWCachePopulator oauthTokenDetailsPopulator;
	
	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	@Inject
	@Named("oauthauthenticationService")
	private RESTfulAuthenticationServiceImpl oauthService;

	@Override
	public boolean canHandleEvent(Event event) { 

        if (event instanceof MakerCheckerEvent
                && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED||event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED||event.getEventType() == EventTypes.MAKER_CHECKER_DELETION_APPROVED)) {
        	MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
            EntityId oauthConfigEntityId = makerCheckerEvent.getOwnerEntityId();
            if (oauthConfigEntityId.getEntityClass() != null
                    && oauthConfigEntityId.getEntityClass().equals(OauthConfig.class)) {
                return true;
            }
        }
        return false;
    }

	@Override
	public void handleEvent(Event event) {
		Long id = ((MakerCheckerEvent) event).getOwnerEntityId().getLocalId();
		OauthConfig oauthConfig = entityDao.find(OauthConfig.class, id);
		oauthTokenDetailsPopulator.update(Action.DELETE, oauthConfig.getClientId(),null);
	}

}
