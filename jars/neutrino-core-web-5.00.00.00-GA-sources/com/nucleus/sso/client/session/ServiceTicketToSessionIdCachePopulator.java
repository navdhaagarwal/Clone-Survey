package com.nucleus.sso.client.session;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

@Named("serviceTicketToSessionIdCachePopulator")
public class ServiceTicketToSessionIdCachePopulator extends FWCachePopulator {

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : SessionIdToServiceTicketCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		return null;
	}

	@Override
	public void build(Long tenantId) {
		BaseLoggers.flowLogger.debug("Build Called : SessionIdToServiceTicketCachePopulator");
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.DELETE) && !StringUtils.isEmpty((String) object) && containsKey(object)) {
			remove(object);
			BaseLoggers.flowLogger.debug("Found session for mapping. Mapping(ServiceTicket) Removed.");
		} else if (action.equals(Action.INSERT)) {
			putAll(object);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.SERVICE_TICKET_TO_SESSION_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.MISCELLANEOUS_CACHE_GROUP;
	}
	
	@Override
	protected boolean fallbackRequired() {
		return false;
	}

}
