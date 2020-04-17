package com.nucleus.security.core.session;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.pubsub.PubSubListener;
import com.nucleus.pubsub.PubSubService;
import com.nucleus.pubsub.redis.PubSubServiceRedisImpl;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.BatchOptions;
import org.redisson.api.RedissonClient;
import org.redisson.codec.FstCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.connection.balancer.LoadBalancer;
import org.redisson.connection.balancer.RoundRobinLoadBalancer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.session.SessionRepository;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NeutrinoMapSessionRepository implements SessionRepository<NeutrinoMapSession>, ApplicationEventPublisherAware {


    private PubSubService pubSubService;

    @Value("${session.failover.enabled:false}")
    private boolean isSessionFailoverEnabled;

    @Value("${session.redis.server.idle.connection.timeout:30000}")
    private Integer redisServerIdleConnectionTimeout;

    @Value("${session.redis.server.ping.interval:0}")
    private Integer redisServerPingInterval;

    @Value("${session.redis.server.ping.timeout:5000}")
    private Integer redisServerPingTimeout;

    @Value("${session.redis.server.connect.timeout:30000}")
    private Integer redisServerConnectTimeout;

    @Value("${session.redis.server.timeout:10000}")
    private Integer redisServerTimeout;

    @Value("${session.redis.server.retry.attempts:3}")
    private Integer redisServerRetryAttempts;

    @Value("${session.redis.server.reconnection.timeout:10000}")
    private Integer redisServerReconnectionTimeout;

    @Value("${session.redis.server.failed.attempts:5}")
    private Integer redisServerFailedAttempts;

    @Value("${session.redis.server.database.id}")
    private Integer redisServerDatabaseId;

    @Value("${session.redis.server.password}")
    private String redisServerPasswordKey;

    @Value("${session.redis.server.address}")
    private String redisSingleServerAddress;

    @Value("${session.redis.server.client.name:redis}")
    private String redisSingleServerClientName;

    @Value("${session.redis.server.connection.min.idle.size:20}")
    private Integer redisSingleServerConnectionMinIdleSize;

    @Value("${session.redis.server.connection.pool.size:500}")
    private Integer redisSingleServerConnectionPoolSize;

    @Value("${session.redis.server.subscription.connection.pool.size:500}")
    private Integer redisServerSubscriptionConnectionPoolSize;

    @Value("${session.redis.server.subscription.connection.min.idle.size:20}")
    private Integer redisServerSubscriptionConnectionMinIdleSize;

    @Value("${session.redis.server.subscription.per.connection:200}")
    private Integer redisServerSubscriptionPerConnection;

    @Value("${session.redis.sentinel.client.master.name:master}")
    private String redisSentinelServerClientName;

    @Value("${session.redis.sentinel.slave.connection.min.idle.size:20}")
    private Integer redisSentinelSlaveConnectionMinIdleSize;

    @Value("${session.redis.sentinel.slave.connection.pool.size:500}")
    private Integer redisSentinelSlaveConnectionPoolSize;

    @Value("${session.redis.sentinel.master.connection.min.idle.size:20}")
    private Integer redisSentinelMasterConnectionMinIdleSize;

    @Value("${session.redis.sentinel.master.connection.pool.size:500}")
    private Integer redisSentinelMasterConnectionPoolSize;

    @Value("${session.redis.sentinel.master.name:master}")
    private String redisSentinelServerMasterName;

    @Value("${session.redis.sentinel.addressA}")
    private String redisSentinelServerAddressA;

    @Value("${session.redis.sentinel.addressB}")
    private String redisSentinelServerAddressB;

    @Value("${session.redis.sentinel.addressC}")
    private String redisSentinelServerAddressC;

    @Value("${session.redis.sentinel.addressD}")
    private String redisSentinelServerAddressD;

    @Value("${session.redis.server.batch.response.timeout:30}")
    private Integer redisServerBatchResponseTimeout;

    @Value("${session.redis.server.batch.retry.attempts:5}")
    private Integer redisServerBatchRetryAttempts;

    @Value("${session.redis.server.batch.retry.interval:10}")
    private Integer redisServerBatchRetryInterval;

    private SessionStore sessions;

    private RedissonClient redisson;
    private BatchOptions batchOptions;
    private ApplicationEventPublisher applicationEventPublisher;
    private Integer defaultMaxInactiveInterval;
    private String serverNodeId;
    private Boolean isRedisEnabled = Boolean.FALSE;
    private PubSubListener attributeTopicListener;


    public NeutrinoMapSessionRepository() {
        super();
    }

    public RedissonClient getRedisson() {
        return redisson;
    }

    public BatchOptions getBatchOptions() {
        return batchOptions;
    }

    public String getServerNodeId() {
        return serverNodeId;
    }

    public Boolean getRedisEnabled() {
        return isRedisEnabled;
    }

    protected void cleanupExpiredSessions() {
        this.sessions.entrySet().stream().forEach(entry -> {
            NeutrinoMapSession session = entry.getValue();
            if (session.isExpired()) {
                if (this.sessions.isSessionExpiredInRemoteStore(entry.getKey())) {
                    deleteSession(session);
                } else {
                    this.sessions.remove(session.getId());
                    pubSubService.unSubscribeFromTopic("TOPIC_" + session.getId(), this.sessions.getAndDeRegisterAttributeListenerId(session.getId()));
                }

            }
        });

    }


    public void syncSessionAttribute(String sessionId, String attributeKey) {
        if (isSessionFailoverEnabled) {
            NeutrinoMapSession session = findById(sessionId);
            session.setAttribute(attributeKey, session.getAttribute(attributeKey), true);
        }
    }

    @PostConstruct
    public void init() {
        CoreUtility coreUtility = NeutrinoSpringAppContextUtil.getBeanByName("coreUtility", CoreUtility.class);
        if (coreUtility == null) {
            throw new SystemException("Bean Not Found for CORE UTILITY");
        }
        serverNodeId = coreUtility.getServerNodeId();

        attributeTopicListener = message -> {
            SessionAttributeTopicVO vo = (SessionAttributeTopicVO) message;
            if (vo.getServerNodeId() != serverNodeId && isSessionExistInLocalStore(vo.getId())) {
                if (vo.getAttributeAction() == SessionAttributeActionEnum.ADD) {
                    setAttributesFromPubSub(vo);
                } else if (vo.getAttributeAction() == SessionAttributeActionEnum.REMOVE) {
                    removeAttributeFromSessionFromPubSub(vo);
                }
            }
        };

        String cacheMode = coreUtility.getCacheMode();
        if (isSessionFailoverEnabled && StringUtils.isNotEmpty(cacheMode)) {
            if (cacheMode.equals(CoreUtility.CACHE_MODE_REDIS)) {
                initRedissonSingleServerClient();
            } else if (cacheMode.equals(CoreUtility.CACHE_MODE_SENTINEL)) {
                initRedissonSentinelClient();
            }
        }

        if (redisson != null) {
            pubSubService = new PubSubServiceRedisImpl(redisson);
            ((PubSubServiceRedisImpl) pubSubService).init();
            sessions = new RedisSessionStore(redisson, batchOptions, serverNodeId, pubSubService, attributeTopicListener);
            isRedisEnabled = Boolean.TRUE;
        } else {
            pubSubService = NeutrinoSpringAppContextUtil.getBeanByName("pubSubService", PubSubService.class);
            if (pubSubService == null) {
                
                throw new com.nucleus.finnone.pro.base.exception.SystemException("PubSubService bean is mandatory. Bean not found.");
            }
            sessions = new SessionStore(serverNodeId, pubSubService, attributeTopicListener);
        }

        pubSubService.subscribeToTopic(NeutrinoSpringSessionListener.SESSION_DESTROYED_TOPIC, msg -> deleteFromLocalById((String) msg));
        pubSubService.subscribeToTopic(NeutrinoSpringSessionListener.SESSION_DESTROYED_COMPLETED_TOPIC, msg -> this.sessions.removeFromIgnoreSet((String) msg));

        BaseLoggers.flowLogger.info("NeutrinoMapSessionRepository INITIALIZED");
    }

    private boolean isSessionExistInLocalStore(String sessionId) {
        return this.sessions.getFromLocal(sessionId) != null;
    }

    private void setAttributesFromPubSub(SessionAttributeTopicVO vo) {
        this.sessions.getFromLocal(vo.getId()).setAttributesFromPubSub((Map<String, Object>) vo.getAttributeValue());
    }

    private void removeAttributeFromSessionFromPubSub(SessionAttributeTopicVO vo) {
        this.sessions.getFromLocal(vo.getId()).removeAttributeFromPubSub(vo.getAttributeKey());
    }

    public void setDefaultMaxInactiveInterval(int defaultMaxInactiveInterval) {
        this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
    }

    public Integer getDefaultMaxInactiveInterval() {
        return this.defaultMaxInactiveInterval;
    }

    @Override
    public void save(NeutrinoMapSession session) {
        if (!session.getId().equals(session.getOriginalId())) {
            deleteById(session.getOriginalId());
            session.setOriginalId(session.getId());
        }
        if (this.sessions.containsKeyInLocal(session.getId())) {
            publishEvent(new SessionStateUpdatedEvent(this, session));
        }
    }

    @Override
    public NeutrinoMapSession findById(String id) {
        NeutrinoMapSession session = this.sessions.get(id);
        if (session == null) {
            return null;
        }
        if (session.isExpired()) {
            if (this.sessions.isSessionExpiredInRemoteStore(id)) {
                deleteSession(session);
                return null;
            } else {
                this.sessions.remove(session.getId());
                pubSubService.unSubscribeFromTopic("TOPIC_" + session.getId(), this.sessions.getAndDeRegisterAttributeListenerId(session.getId()));
                return findById(id);
            }
        }
        return session;
    }

    @Override
    public void deleteById(String id) {
        NeutrinoMapSession session = findById(id);
        deleteSession(session);
    }

    private void deleteSession(NeutrinoMapSession session) {
        deleteSessionFromLocal(session);
        publishEvent(new SessionDestroyedEvent(this, session));
        publishEvent(new org.springframework.session.events.SessionDestroyedEvent(this, session));
        this.sessions.removeFromIgnoreSet(session.getId());
    }

    private void deleteFromLocalById(String id) {
        NeutrinoMapSession session = sessions.getFromLocal(id);
        if (session != null) {
            deleteSessionFromLocal(session);
        }
    }

    private void deleteSessionFromLocal(NeutrinoMapSession session) {
        String sessionId = session.getId();
        this.sessions.putInIgnoreSet(sessionId);
        this.sessions.remove(sessionId);
        pubSubService.unSubscribeFromTopic("TOPIC_" + sessionId, this.sessions.getAndDeRegisterAttributeListenerId(sessionId));
    }

    @Override
    public NeutrinoMapSession createSession() {
        NeutrinoMapSession session = new NeutrinoMapSession();
        if (this.defaultMaxInactiveInterval != null) {
            session.setMaxInactiveInterval(Duration.ofSeconds(this.defaultMaxInactiveInterval));
        }
        String sessionId = session.getId();
        this.sessions.put(sessionId, session);
        publishEvent(new SessionCreatedEvent(this, session));
        redisson.getMap("VO_" + sessionId).putAsync(serverNodeId, true);
        publishEvent(new org.springframework.session.events.SessionCreatedEvent(this, session));
        this.sessions.registerAttributeListenerId(sessionId, pubSubService.subscribeToTopic("TOPIC_" + sessionId, attributeTopicListener));
        return session;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        NeutrinoMapSession.initialize(applicationEventPublisher);
    }

    private void publishEvent(ApplicationEvent event) {
        try {
            applicationEventPublisher.publishEvent(event);
        } catch (Exception e) {
            BaseLoggers.flowLogger.error(e.getMessage(), e);
        }
    }


    private void initRedissonSentinelClient() {
        Config redissonConfig = new Config();
        FstCodec codec = new FstCodec();
        LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

        redissonConfig.setCodec(codec);
        redissonConfig.useSentinelServers()
                .setIdleConnectionTimeout(redisServerIdleConnectionTimeout)
                .setPingConnectionInterval(redisServerPingInterval)
                .setPingTimeout(redisServerPingTimeout)
                .setConnectTimeout(redisServerConnectTimeout)
                .setTimeout(redisServerTimeout)
                .setRetryAttempts(redisServerRetryAttempts)
                .setReconnectionTimeout(redisServerReconnectionTimeout)
                .setFailedAttempts(redisServerFailedAttempts)
                .setClientName(redisSentinelServerClientName).setLoadBalancer(loadBalancer)
                .setSlaveConnectionMinimumIdleSize(redisSentinelSlaveConnectionMinIdleSize)
                .setSlaveConnectionPoolSize(redisSentinelSlaveConnectionPoolSize)
                .setMasterConnectionMinimumIdleSize(redisSentinelMasterConnectionMinIdleSize)
                .setMasterConnectionPoolSize(redisSentinelMasterConnectionPoolSize)
                .setSubscriptionConnectionMinimumIdleSize(redisServerSubscriptionConnectionMinIdleSize)
                .setSubscriptionConnectionPoolSize(redisServerSubscriptionConnectionPoolSize)
                .setSubscriptionsPerConnection(redisServerSubscriptionPerConnection)
                .setMasterName(redisSentinelServerMasterName).setReadMode(ReadMode.SLAVE)
                .setPassword(redisServerPasswordKey).addSentinelAddress(getSentinelAddress());

        redisson = Redisson.create(redissonConfig);
        batchOptions = getDefaultBatchOptions();
    }


    private void initRedissonSingleServerClient() {

        Config redissonConfig = new Config();
        FstCodec codec = new FstCodec();
        redissonConfig.setCodec(codec);
        redissonConfig.useSingleServer()
                .setIdleConnectionTimeout(redisServerIdleConnectionTimeout)
                .setPingConnectionInterval(redisServerPingInterval)
                .setPingTimeout(redisServerPingTimeout)
                .setConnectTimeout(redisServerConnectTimeout)
                .setTimeout(redisServerTimeout)
                .setRetryAttempts(redisServerRetryAttempts)
                .setReconnectionTimeout(redisServerReconnectionTimeout)
                .setFailedAttempts(redisServerFailedAttempts)
                .setClientName(redisSingleServerClientName)
                .setAddress(redisSingleServerAddress)
                .setConnectionMinimumIdleSize(redisSingleServerConnectionMinIdleSize)
                .setSubscriptionConnectionMinimumIdleSize(redisServerSubscriptionConnectionMinIdleSize)
                .setSubscriptionConnectionPoolSize(redisServerSubscriptionConnectionPoolSize)
                .setSubscriptionsPerConnection(redisServerSubscriptionPerConnection)
                .setConnectionPoolSize(redisSingleServerConnectionPoolSize)
                .setDatabase(redisServerDatabaseId)
                .setPassword(redisServerPasswordKey);

        redisson = Redisson.create(redissonConfig);
        batchOptions = getDefaultBatchOptions();

    }

    private String[] getSentinelAddress() {
        Set<String> strSet = new HashSet<>();
        strSet = addToStringSet(redisSentinelServerAddressA, strSet);
        strSet = addToStringSet(redisSentinelServerAddressB, strSet);
        strSet = addToStringSet(redisSentinelServerAddressC, strSet);
        strSet = addToStringSet(redisSentinelServerAddressD, strSet);

        int index = -1;
        String[] ret = new String[strSet.size()];

        for (String str : strSet) {
            index++;
            ret[index] = str;
        }
        return ret;
    }

    private Set<String> addToStringSet(String temp, Set<String> strSet) {
        if (temp != null && temp.length() > 0 && !temp.startsWith("${")) {
            strSet.add(temp);
        }
        return strSet;
    }

    private BatchOptions getDefaultBatchOptions() {
        return BatchOptions.defaults()
                .responseTimeout(redisServerBatchResponseTimeout, TimeUnit.SECONDS)
                .retryAttempts(redisServerBatchRetryAttempts)
                .retryInterval(redisServerBatchRetryInterval, TimeUnit.SECONDS)
                .executionMode(BatchOptions.ExecutionMode.IN_MEMORY_ATOMIC);
    }
}
