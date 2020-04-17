package com.nucleus.security.core.session;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.pubsub.PubSubListener;
import com.nucleus.pubsub.PubSubService;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

public class InfinispanSessionStore extends SessionStore {

    private Cache<Object, Object> sessionCache;
    private EmbeddedCacheManager manager;

    public InfinispanSessionStore(String sessionCacheConfigFileName, String serverNodeId, PubSubService pubSubService, PubSubListener attributeTopicListener) {
        super(serverNodeId, pubSubService, attributeTopicListener);
        try {
            this.manager = new DefaultCacheManager(sessionCacheConfigFileName);
        } catch (IOException e) {
            BaseLoggers.securityLogger.error("Error occured in initializing cache manager for " + SESSION_CACHE_NAME);
            BaseLoggers.securityLogger.error(e.getMessage());
        }
        this.sessionCache = manager.getCache(SESSION_CACHE_NAME);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public NeutrinoMapSession getFromStore(Object arg0) {
        return (NeutrinoMapSession) this.sessionCache.get(arg0);
    }


}
