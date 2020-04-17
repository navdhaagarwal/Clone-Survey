package com.nucleus.security.core.session;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.pubsub.PubSubService;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NeutrinoSpringSessionListener implements ApplicationListener<AbstractSessionEvent> {

    public static final String SESSION_DESTROYED_TOPIC = "SESSION_DESTROYED_TOPIC";
    public static final String SESSION_DESTROYED_COMPLETED_TOPIC = "SESSION_DESTROYED_COMPLETED_TOPIC";

    @Value("${session.failover.redis.sync.interval:10}")
    private Integer redisSyncIntervalInSeconds;

    //The following private member 'allowedSessionAttributesProperty' to be removed post SESSION ATTRIBUTE code clean up across modules
    //Also the property from framework-config.properties is to be removed from all modules
    @Value("${session.failover.allowed.session.attributes:SPRING_SECURITY_CONTEXT}")
    private String allowedSessionAttributesProperty;

    @Autowired
    private PubSubService pubSubService;

    private NeutrinoMapSessionRepository sessionRepository;
    private SessionAttributeStoreCachePopulator sessionAttributeStoreCachePopulator;

    private Map<String, Map<String, Object>> sessionAttributeUpdates = new ConcurrentHashMap<>();
    private Map<String, Set<String>> sessionAttributeRemoves = new ConcurrentHashMap<>();
    private Set<String> sessionsToDestroy = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Map<String, NeutrinoMapSessionVO> sessionVOs = new ConcurrentHashMap<>();

    //The following private member to be removed post SESSION ATTRIBUTE code clean up across modules
    private Set<String> allowedSessionAttributes = new HashSet<>();
    private RedissonClient redisson;
    private BatchOptions batchOptions;
    private String serverNodeId;
    private Integer defaultMaxInactiveInterval;

    public NeutrinoSpringSessionListener(NeutrinoMapSessionRepository sessionRepository, SessionAttributeStoreCachePopulator sessionAttributeStoreCachePopulator) {
        super();
        this.sessionRepository = sessionRepository;
        this.sessionAttributeStoreCachePopulator = sessionAttributeStoreCachePopulator;

    }

    public NeutrinoSpringSessionListener(NeutrinoMapSessionRepository sessionRepository) {
        super();
        this.sessionRepository = sessionRepository;
    }

    @PostConstruct
    private void init() {
        if (this.sessionRepository.getRedisEnabled()) {
            //The following line to be removed post SESSION ATTRIBUTE code clean up across modules
            StringBuilder str = new StringBuilder("com.nucleus.web.csrf.CSRFTokenManager.tokenval,CSRF_TOKEN_FOR_SESSION_ATTR_NAME,PASS_PHRASE,alignment,preferredTheme,javamelody.country,javamelody.remoteAddr,javamelody.userAgent,Id,SPRING_SECURITY_LAST_EXCEPTION,sessionUsernameParameter,sessionPasswordParameter,initialConversationalId,SPRING_SECURITY_CONTEXT,amountFormatWithoutPrecision,currencyMap,baseCurrency,curr_precision,user_date_format,user_profile,user_tenant,org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE,lmstooltip,amt_format,groupingSeparator,decimalSeparator,number_format,last_login,display_date_format,isLMSMenu,default_cif,business_date_joda,businessDate,businessDateUserFormat,businessDateUtil,user_branches,neutrino_user_id,lmsleftbar,max_custom_fields,locale,specialCharacterArray,specialCharacterMaskArray,proxy_message,sessionUser,userPreferences,preferences,javamelody.remoteUser,javax.servlet.jsp.jstl.fmt.locale.session,javax.servlet.jsp.jstl.fmt.request.charset,singleUserSessionExceededFlag,casClient,org.springframework.web.servlet.i18n.SessionLocaleResolver.TIME_ZONE,licenseAlertShowBeforeExpiry,licenseAlertShowBeforeGrace,licenseAlertOnThresholdNamedUserConsumption,licenseAlertOnMaxNamedUserConsumption");
            allowedSessionAttributes = new HashSet<>(Arrays.asList(str.toString().split(",")));
            allowedSessionAttributes.addAll(Arrays.asList(allowedSessionAttributesProperty.split(",")));
            redisson = sessionRepository.getRedisson();
            batchOptions = sessionRepository.getBatchOptions();
            serverNodeId = sessionRepository.getServerNodeId();
            defaultMaxInactiveInterval = sessionRepository.getDefaultMaxInactiveInterval();
            schedule();
            BaseLoggers.flowLogger.info("NeutrinoSpringSessionListener INITIALIZED");
        }
    }

    @Override
    public void onApplicationEvent(AbstractSessionEvent event) {
        if (!this.sessionRepository.getRedisEnabled()) {
            return;
        }

        if (event instanceof SessionStateUpdatedEvent) {
            sessionStateUpdated((SessionStateUpdatedEvent) event);
        } else if (event instanceof SessionAttributeAddedEvent) {
            attributeAdded((SessionAttributeAddedEvent) event);
        } else if (event instanceof SessionAttributeReplacedEvent) {
            attributeReplaced((SessionAttributeReplacedEvent) event);
        } else if (event instanceof SessionAttributeRemovedEvent) {
            attributeRemoved((SessionAttributeRemovedEvent) event);
        } else if (event instanceof SessionDestroyedEvent) {
            sessionDestroyed((SessionDestroyedEvent) event);
        } else if (event instanceof SessionCreatedEvent) {
            sessionCreated((SessionCreatedEvent) event);
        }
    }

    protected void attributeAdded(SessionAttributeAddedEvent event) {
        checkAndUpdateRemoteStore(event.getSessionId(), event.getName(), event.getValue(), null, false);
    }

    protected void attributeReplaced(SessionAttributeReplacedEvent event) {
        checkAndUpdateRemoteStore(event.getSessionId(), event.getName(), event.getValue(), event.getOldValue(), event.isIgnoreEqualsCheck());
    }

    private void checkAndUpdateRemoteStore(String sessionId, String attributeName, Object newValue, Object oldValue, Boolean ignoreEqualsCheck) {
        if (isAllowedSessionAttribute(attributeName) && (!newValue.equals(oldValue) || ignoreEqualsCheck)) {
            Map<String, Object> map = sessionAttributeUpdates.get(sessionId);
            Set<String> set = sessionAttributeRemoves.get(sessionId);
            if (set != null) {
                set.remove(attributeName);
            }
            if (map == null) {
                map = new ConcurrentHashMap<>();
                sessionAttributeUpdates.put(sessionId, map);
            }
            map.put(attributeName, newValue);
        }
    }

    protected void attributeRemoved(SessionAttributeRemovedEvent event) {
        String attributeName = event.getName();
        if (isAllowedSessionAttribute(attributeName)) {
            String sessionId = event.getSession().getId();
            Map<String, Object> map = sessionAttributeUpdates.get(sessionId);
            Set<String> set = sessionAttributeRemoves.get(sessionId);
            if (map != null) {
                map.remove(attributeName);
            }
            if (set == null) {
                set = Collections.newSetFromMap(new ConcurrentHashMap<>());
                sessionAttributeRemoves.put(sessionId, set);
            }
            set.add(attributeName);
        }
    }


    protected void sessionCreated(SessionCreatedEvent se) {
        prepareNeutrinoMapSessionVO(se.getSession());
    }

    protected void sessionStateUpdated(SessionStateUpdatedEvent se) {
        prepareNeutrinoMapSessionVO(se.getSession());
    }

    private void prepareNeutrinoMapSessionVO(NeutrinoMapSession session) {
        NeutrinoMapSessionVO sessionVO = new NeutrinoMapSessionVO(session, serverNodeId);
        sessionVOs.put(sessionVO.getId(), sessionVO);
    }


    protected void sessionDestroyed(SessionDestroyedEvent se) {
        String sessionId = se.getSession().getId();
        sessionsToDestroy.add(sessionId);
        sessionVOs.remove(sessionId);
        sessionAttributeUpdates.remove(sessionId);
        sessionAttributeRemoves.remove(sessionId);
        //In case of Report Architecture - check for size of the following map and publish on an Rtopic if size is greater than 2
        //Size of more than 2 means, SESSION has been created locally in more than 1 NODEs
        RMap rmap = redisson.getMap("VO_" + sessionId);
        boolean publishOrNot = rmap.size() > 2;
        if (publishOrNot) {
            pubSubService.publishOnTopic(sessionId, SESSION_DESTROYED_TOPIC);
        }
        rmap.unlink();
        if (publishOrNot) {
            pubSubService.publishOnTopic(sessionId, SESSION_DESTROYED_COMPLETED_TOPIC);
        }
    }

    private void schedule() {
        Flux.interval(Duration.ofSeconds(redisSyncIntervalInSeconds)).flatMap(count -> {
            pushTheDataToStore();
            return Flux.empty();
        }).onErrorResume(error -> {
            schedule();
            return Flux.empty();
        }).subscribe();
    }

    //The following private method 'isAllowedSessionAttribute' and it's usage
    // is to be removed post SESSION ATTRIBUTE code clean up across modules
    private boolean isAllowedSessionAttribute(String attributeName) {
        /*return allowedSessionAttributes.contains(attributeName);*/
        if (allowedSessionAttributes.contains(attributeName)) {
            return true;
        } else {
            BaseLoggers.flowLogger.info("Not Allowed Attribute : " + attributeName);
            return false;
        }
    }

    private void pushTheDataToStore() {

        this.sessionRepository.cleanupExpiredSessions();

        Map<String, Map<String, Object>> attributesToPush = sessionAttributeUpdates;
        Set<String> sessionToDestroy = sessionsToDestroy;
        Map<String, Set<String>> attributesToRemove = sessionAttributeRemoves;
        Map<String, NeutrinoMapSessionVO> newSessionsToPush = sessionVOs;

        sessionAttributeUpdates = new ConcurrentHashMap<>();
        sessionsToDestroy = Collections.newSetFromMap(new ConcurrentHashMap<>());
        sessionAttributeRemoves = new ConcurrentHashMap<>();
        sessionVOs = new ConcurrentHashMap<>();

        RBatch batch = redisson.createBatch(batchOptions);

        attributesToRemove.entrySet().stream().forEach(entry -> {
            String sessionId = entry.getKey();
            RMapAsync attributesRemoveCache = batch.getMap(sessionId);
            SessionAttributeTopicVO vo = new SessionAttributeTopicVO(sessionId, serverNodeId, SessionAttributeActionEnum.REMOVE, null, null);
            String topicID = "TOPIC_" + sessionId;
            entry.getValue().stream().forEach(attr -> {
                vo.setAttributeKey(attr);
                attributesRemoveCache.removeAsync(attr);
                pubSubService.publishOnTopic(vo, topicID);
            });

            attributesRemoveCache.expireAsync(defaultMaxInactiveInterval, TimeUnit.SECONDS);
        });

        attributesToPush.entrySet().stream().forEach(entry -> {
                    String sessionId = entry.getKey();
                    RMapAsync rmap = batch.getMap(entry.getKey());
                    SessionAttributeTopicVO vo = new SessionAttributeTopicVO(sessionId, serverNodeId, SessionAttributeActionEnum.ADD, null, entry.getValue());
                    rmap.putAllAsync(entry.getValue());
                    pubSubService.publishOnTopic(vo, "TOPIC_" + sessionId);
                    rmap.expireAsync(defaultMaxInactiveInterval, TimeUnit.SECONDS);
                }
        );

        newSessionsToPush.entrySet().stream().forEach(entry -> {
            RMapAsync rmap = batch.getMap("VO_" + entry.getKey());
            rmap.putAsync("VO", entry.getValue());
            rmap.expireAsync(defaultMaxInactiveInterval, TimeUnit.SECONDS);
        });

        sessionToDestroy.stream().forEach(sessionId -> {
            batch.getMap(sessionId).unlinkAsync();
            batch.getMap("VO_" + sessionId).unlinkAsync();
//            sessionAttributeStoreCachePopulator.handleSessionDestroyed(sessionId);
        });


        batch.execute();

    }


}
