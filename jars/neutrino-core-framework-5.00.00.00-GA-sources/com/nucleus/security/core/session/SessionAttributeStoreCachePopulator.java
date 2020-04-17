package com.nucleus.security.core.session;

import com.nucleus.core.exceptions.OperationNotSupportedException;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

public class SessionAttributeStoreCachePopulator extends FWCachePopulator {

    public final static String KEY_SEPARATOR = "-";
    private final static String ASTERISK = "*";
    private final static String KEYSET_REGEX = KEY_SEPARATOR + "*";

    @Override
    public void init() {
        BaseLoggers.flowLogger.debug("Init Called : SessionAttributeStoreCachePopulator");
    }

    @Override
    protected boolean fallbackRequired() {
        return false;
    }

    @Override
    public Object fallback(Object key) {
        throw new OperationNotSupportedException();
    }

    @Override
    public void build(Long tenantId) {
        BaseLoggers.flowLogger.debug("Build Called : SessionAttributeStoreCachePopulator : Doing Nothing");
    }

    @Override
    public void update(NeutrinoCachePopulator.Action action, Object object) {
        if (action.equals(NeutrinoCachePopulator.Action.DELETE) && ValidatorUtils.notNull(object)) {
            remove(object);
        }
    }

    @Override
    public void update(NeutrinoCachePopulator.Action action, Object key, Object value) {
        if (action.equals(Action.INSERT)) {
            put(key, value);
        }
    }

    void handleSessionDestroyed(String sessionId) {
        keySet(ASTERISK + sessionId + KEYSET_REGEX).forEach(this::remove);
    }

    @Override
    public String getNeutrinoCacheName() {
        return FWCacheConstants.SESSION_ATTRIBUTE_STORE_CACHE;
    }

}
