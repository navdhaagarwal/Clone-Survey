package com.nucleus.security.core.session;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.pubsub.PubSubListener;
import com.nucleus.pubsub.PubSubService;
import org.redisson.api.BatchOptions;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

public class RedisSessionStore extends SessionStore {

    public static final String SESSION_VO_CACHE = "SESSION_VO_CACHE";

    private RedissonClient redisson;
    private BatchOptions batchOptions;


    public RedisSessionStore(RedissonClient redisson, BatchOptions batchOptions, String serverNodeId, PubSubService pubSubService, PubSubListener attributeTopicListener) {
        super(serverNodeId, pubSubService, attributeTopicListener);
        this.redisson = redisson;
        this.batchOptions = batchOptions;
    }

    @Override
    public NeutrinoMapSession getFromStore(Object sessionId) {
        RMap rmap = redisson.getMap("VO_" + sessionId);
        NeutrinoMapSessionVO sessionVO = (NeutrinoMapSessionVO) rmap.get("VO");
        if (sessionVO != null && !ignoreSet.contains(sessionId)) {
            NeutrinoMapSession session = new NeutrinoMapSession((String) sessionId);
            session.setCreationTime(sessionVO.getCreationTime());
            session.setLastAccessedTime(sessionVO.getLastAccessedTime());
            session.setOriginalId(sessionVO.getOriginalId());
            session.setMaxInactiveInterval(sessionVO.getMaxInactiveInterval());
            redisson.getMap((String) sessionId).entrySet().stream().forEach(entry -> {
                session.setAttribute((String) entry.getKey(), entry.getValue());
            });
            session.setFromRemoteStore(true);
            rmap.putAsync(serverNodeId, true);
            registerAttributeListenerId((String) sessionId, pubSubService.subscribeToTopic("TOPIC_" + session.getId(), attributeTopicListener));
            return session;
        }
        return null;
    }

    @Override
    protected boolean isSessionExpiredInRemoteStore(String sessionId) {
        boolean isExpired = false;
        try {
            NeutrinoMapSessionVO sessionVO = (NeutrinoMapSessionVO) redisson.getMap("VO_" + sessionId).get("VO");
            isExpired = sessionVO != null ? sessionVO.isExpired() : true;
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error(e.getMessage(), e);
        }
        return isExpired;
    }

}
