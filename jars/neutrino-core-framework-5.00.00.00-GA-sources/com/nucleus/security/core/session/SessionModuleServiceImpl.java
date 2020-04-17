/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.security.core.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.service.EventExecutionService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.notification.Notification;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.SystemEntity;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEvent;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

/**
 * @author Nucleus Software Exports Limited
 * Service to perform event related operations.
 */
@Named("sessionModuleService")
public class SessionModuleServiceImpl extends BaseServiceImpl implements SessionModuleService {

    public static final String LOW_PRIORITY_USER = "LOW_PRIORITY_USER_";
    public static final int DEFAULT_DELAY = 60;
    public static final String SUCCESS = "success";
    public static final String LOGOUT = "LOGOUT";

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @Inject
    Environment environment;

    @Inject
    private EventService eventService;

    @Inject
    @Named("userService")
    private UserService userService;

    @Inject
    @Named("sessionRegistry")
    private NeutrinoSessionRegistry sessionRegistry;

    @Inject
    @Named("eventExecutionService")
    private EventExecutionService eventExecutionService;

    ScheduledExecutorService scheduledExecutorService;

    Boolean executorActive = false;


    @Override
    public Integer getActiveSessionCountForModule(String module) {
        String countQuery = "select count (distinct s.sessionId) from SessionModuleMapping s where module = :moduleName and ( markedForLogout !=:markedForLogout or markedForLogout is null )";
        JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor<>(countQuery);
        jpaQueryExecutor.addParameter("moduleName", module);
        jpaQueryExecutor.addParameter("markedForLogout", "YES");
        List result = entityDao.executeQuery(jpaQueryExecutor);
        if (CollectionUtils.isNotEmpty(result)) {
            return NumberUtils.toInt(result.get(0).toString(), 0);
        }
        return 0;
    }

    @Override
    public boolean isAllowLoginForConcurrencyMode(UserInfo userInfo) {
        String module = ProductInformationLoader.getProductName();
        Boolean isConcurrencySwitchEnabled = isConcurrencySwichingEnabled();
        if (!isConcurrencySwitchEnabled) {
            return true;
        }
        if(bypassIfSessionforUserPresent(userInfo,module)){
            return true;
        }
        Map<String, Integer> moduleMap = getConfigurationForCurrentModule(module);
        Integer loggedInUserCount = getActiveSessionCountForModule(module);
        Integer highConcurrencyStartCount = moduleMap.get("highConcurrencyStartCount");
        Integer maxThresholdCount = moduleMap.get("maxThresholdCount");
        if (loggedInUserCount >= maxThresholdCount) {
            return loginIfHighPriorityUser(userInfo, module);
        } else if (executorActive) {
            deActivateExecutor();
        }

        if (loggedInUserCount >= highConcurrencyStartCount) {
            if (userInfo.getLowPriorityUserModulesSet().contains(StringUtils.capitalize(module))) {
                logUserSecurityEvent( userInfo,  module);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }

    }

    private void logUserSecurityEvent(UserInfo userInfo, String module) {
        try {
            UserSecurityTrailEvent userSecurityTrailEvent = new UserSecurityTrailEvent(EventTypes.USER_SECURITY_TRAIL_LOGIN_FAIL);
            if (userInfo != null) {
                userSecurityTrailEvent.setUsername(userInfo.getUsername());
                userSecurityTrailEvent.setAssociatedUserUri(userInfo.getUserEntityId().getUri());
            }
            userSecurityTrailEvent.setModuleNameForEvent(module);
            List allLoggedInUserForModule = getAllLoggedInUserForModule(module);
            StringJoiner allUsers = new StringJoiner(",");
            for (Object loggedInUser : allLoggedInUserForModule) {
                allUsers.add(loggedInUser.toString());
            }
            userSecurityTrailEvent.setLoggedInUsersForHcMode(allUsers.toString());
            eventService.createEventEntry(userSecurityTrailEvent);
        }catch (Exception e){
            return;
        }
    }


    @Override
    public boolean checkForLoginFeasibility(UserInfo userInfo, String module) {
        if(module==null || StringUtils.isEmpty(module)){
            module = ProductInformationLoader.getProductName();
        }
        Boolean isConcurrencySwitchEnabled = isConcurrencySwichingEnabled();
        if (!isConcurrencySwitchEnabled) {
            return true;
        }
        if(bypassIfSessionforUserPresent(userInfo,module)){
            return true;
        }
        Map<String, Integer> moduleMap = getConfigurationForCurrentModule(module);
        Integer loggedInUserCount = getActiveSessionCountForModule(module);
        Integer highConcurrencyStartCount = moduleMap.get("highConcurrencyStartCount");
        Integer maxThresholdCount = moduleMap.get("maxThresholdCount");
        if (loggedInUserCount >= maxThresholdCount) {
            return checkLoginFeasibilityIfMaxThreshold(userInfo, module);
        }
        if (loggedInUserCount >= highConcurrencyStartCount) {
            if (userInfo.getLowPriorityUserModulesSet().contains(StringUtils.capitalize(module))) {
                logUserSecurityEvent( userInfo,  module);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }

    }

    private boolean bypassIfSessionforUserPresent(UserInfo userInfo, String module) {
        if(StringUtils.isEmpty(module)){
            module = ProductInformationLoader.getProductName();
        }
        if(userInfo == null || userInfo.getId()==null){
            return false;
        }
        String getQuery = "select count(s) from SessionModuleMapping s where module = :moduleName and userId = :userId and ( markedForLogout !=:markedForLogout or markedForLogout is null ) ORDER BY s.entityLifeCycleData.creationTimeStamp";
        JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor<>(getQuery);
        jpaQueryExecutor.addParameter("moduleName", module);
        jpaQueryExecutor.addParameter("userId", userInfo.getId());
        jpaQueryExecutor.addParameter("markedForLogout", "YES");
        List result = entityDao.executeQuery(jpaQueryExecutor, 0, 1);
        if (CollectionUtils.isNotEmpty(result)) {
            int count  = NumberUtils.toInt(result.get(0).toString(), -1);
            if(count>0){
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isAllowLoginForConcurrencyMode(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof UserInfo) {
            return isAllowLoginForConcurrencyMode((UserInfo) principal);
        } else {
            return true;
        }
    }


    @Override
    public void createSessionModuleMapping(AuthenticationSuccessEvent authenticationSuccessEvent) {
        if (!isConcurrencySwichingEnabled() || authenticationSuccessEvent==null || authenticationSuccessEvent.getSource()==null) {
            return;
        }
        String module = ProductInformationLoader.getProductName();
        AbstractAuthenticationToken abstractAuthenticationToken = (AbstractAuthenticationToken) authenticationSuccessEvent.getSource();
        if(abstractAuthenticationToken==null || abstractAuthenticationToken.getDetails()==null){
            return;
        }
        String sessionId = null;
        try{
            sessionId = ((WebAuthenticationDetails) abstractAuthenticationToken.getDetails()).getSessionId();
            if(sessionId==null || StringUtils.isEmpty(sessionId)){
                return;
            }
        }catch (Exception e){
        	BaseLoggers.exceptionLogger.error("Session Id is not found",e);
            return;
        }
        if (isSsoEnabled() && (authenticationSuccessEvent.getSource() instanceof UsernamePasswordAuthenticationToken)) {
            module = "SSO";
        }
        createSessionModuleMapping(sessionId, module);
    }


    @Override
    public void createSessionModuleMapping(String sessionId) {
        Boolean isConcurrencySwitchEnabled = isConcurrencySwichingEnabled();
        if (!isConcurrencySwitchEnabled) {
            return;
        }
        createSessionModuleMapping(sessionId, ProductInformationLoader.getProductName());
    }

    @Override
    public void deleteSessionModuleMapping(String sessionId) {
        Boolean isConcurrencySwitchEnabled = isConcurrencySwichingEnabled();
        if (!isConcurrencySwitchEnabled) {
            return;
        }
        delete(sessionId);
    }

    @Override
    public void deleteAllSessionModuleMapping(String module) {
        Boolean isConcurrencySwitchEnabled = isConcurrencySwichingEnabled();
        if (!isConcurrencySwitchEnabled ) {
            return;
        }
        if(getActiveSessionCountForModule(module) != null && getActiveSessionCountForModule(module)>0) {
            String deleteQueryString = "delete from SessionModuleMapping smm where smm.module = :module";
            Query query = entityDao.getEntityManager().createQuery(deleteQueryString);
            query.setParameter("module", module);
            try {
                query.executeUpdate();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    @Override
    public Boolean isConcurrencySwichingEnabled() {
        ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "config.concurrency.highlow.mode.enable");
        if (configurationVO == null || configurationVO.getPropertyValue() == null) {
            return false;
        }
        String highConcurrencyPropertyValue = configurationVO.getPropertyValue();
        if (BooleanUtils.toBooleanObject(highConcurrencyPropertyValue) != null) {
            return BooleanUtils.toBooleanObject(highConcurrencyPropertyValue);
        } else {
            return false;
        }
    }


    private void createSessionModuleMapping(String sessionId, String module) {
        Boolean isConcurrencySwitchEnabled = isConcurrencySwichingEnabled();
        if (!isConcurrencySwitchEnabled ) {
            return;
        }
        SessionModuleMapping sessionModuleMapping = new SessionModuleMapping();
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
        if (sessionInformation != null) {
            UserInfo userInfo = (UserInfo) sessionInformation.getPrincipal();
            sessionModuleMapping.setUserId(userInfo.getId());
            if (userInfo.getLowPriorityUserModulesSet().contains(StringUtils.capitalize(module))) {
                sessionModuleMapping.setPriority("LOW");
            } else {
                sessionModuleMapping.setPriority("HIGH");
            }
        }
        sessionModuleMapping.setModule(module);
        sessionModuleMapping.setSessionId(sessionId);
        save(sessionModuleMapping);
    }

    private HashMap getConfigurationForCurrentModule(String module) {
        String hcPropKey = module + ".config.concurrency.high.activation.count";
        String maxThPropKey = module + ".config.concurrency.max.threshold.count";
        ConfigurationVO highConcurrencyStartCountVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), hcPropKey);
        ConfigurationVO maxThresholdCountVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), maxThPropKey);

        HashMap map = new HashMap();
        map.put("highConcurrencyStartCount", NumberUtils.toInt(highConcurrencyStartCountVO.getPropertyValue()));
        map.put("maxThresholdCount", NumberUtils.toInt(maxThresholdCountVO.getPropertyValue()));
        return map;
    }


    private boolean checkLoginFeasibilityIfMaxThreshold(UserInfo userInfo, String module) {
        if (userInfo.getLowPriorityUserModulesSet().contains(StringUtils.capitalize(module))) {
            logUserSecurityEvent( userInfo,  module);
            return false;
        } else {
            SessionInformation sessionInformation = getOldestLowPrioritySessionIfPresent(false,module);
            if (sessionInformation != null) {
                return true;
            }
            logUserSecurityEvent( userInfo,  module);
            return false;
        }
    }

    private boolean loginIfHighPriorityUser(UserInfo userInfo, String module) {
        if (!executorActive) {
            activateExecutor();
        }
        if (userInfo.getLowPriorityUserModulesSet().contains(StringUtils.capitalize(module))) {
            logUserSecurityEvent( userInfo,  module);
            return false;
        } else {
            SessionInformation sessionInformation = getOldestLowPrioritySessionIfPresent(true,module);
            if (sessionInformation != null) {
                notifyUserForLogout(sessionInformation);
                markSessionForLogout(sessionInformation);
                return true;
            }
            logUserSecurityEvent( userInfo,  module);
            return false;
        }
    }

    private void notifyUserForLogout(SessionInformation sessionInformation) {
        UserInfo userInfo = (UserInfo) sessionInformation.getPrincipal();
        exeutePreNotificationEvent(userInfo);
        String userId = userInfo.getId().toString();
        GenericEvent event = new GenericEvent(EventTypes.CONCURRENCY_LOGOUT_EVENT);
        event.addPersistentProperty("Module_Name", ProductInformationLoader.getProductName());
        event.addPersistentProperty("delay", String.valueOf(getDelay()));
        event.addNonWatcherToNotify(userId);
        event.addPersistentProperty(GenericEvent.SUCCESS_FLAG, SUCCESS);
        GenericEvent eventObj = entityDao.saveOrUpdate(event);
        Notification notification = new Notification();
        String userUri = userService.getUserById(Long.parseLong(userId)).getUserEntityId().getUri();
        notification.setNotificationUserUri(userUri);
        notification.setNotificationType(LOGOUT);
        notification.setSeen(false);
        notification.setGenericEvent(eventObj);
        notification.setEventType(eventObj.getEventType());
        entityDao.saveOrUpdate(notification);
    }

    private void exeutePreNotificationEvent(UserInfo userInfo) {
        String eventCode = EventCode.HIGH_CONCURRENCY_LOGOUT_EVENT;
        Map<Object, Object> contextMap = new HashMap<>();
        contextMap.put("userId", userInfo.getUsername());
        contextMap.put("lowPriorityUserModule", userInfo.getLowPriorityUserModulesSet());
        contextMap.put("module", ProductInformationLoader.getProductName());
        contextMap.put("isSsoActive", isSsoEnabled());
        eventExecutionService.fireEventExecution(eventCode, contextMap, null);
    }


    private void markSessionForLogout(SessionInformation sessionInformation) {
        Integer delay = getDelay();
        try {
            scheduledExecutorService.schedule((Callable) () -> {
                        if (!sessionInformation.isExpired()) {
                            sessionRegistry.expireSessionBySessionId(sessionInformation.getSessionId());
                        }
                        return true;
                    },
                    delay,
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            BaseLoggers.flowLogger.error("Error while trying to kill session : " + sessionInformation.getSessionId());
        }

    }

    private SessionInformation getOldestLowPrioritySessionIfPresent(Boolean markForLogout,String module) {
        if(StringUtils.isEmpty(module)){
            module = ProductInformationLoader.getProductName();
        }
        String getQuery = "select s from SessionModuleMapping s where module = :moduleName and priority = :priority and ( markedForLogout !=:markedForLogout or markedForLogout is null ) ORDER BY s.entityLifeCycleData.creationTimeStamp ";
        JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor<>(getQuery);
        jpaQueryExecutor.addParameter("moduleName", module);
        jpaQueryExecutor.addParameter("priority", "LOW");
        jpaQueryExecutor.addParameter("markedForLogout", "YES");
        List result = entityDao.executeQuery(jpaQueryExecutor, 0, 1);
        if (CollectionUtils.isNotEmpty(result)) {
            SessionModuleMapping sessionModuleMapping = (SessionModuleMapping) result.get(0);
            if(markForLogout) {
                sessionModuleMapping.setMarkedForLogout("YES");
                entityDao.update(sessionModuleMapping);
            }
            SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionModuleMapping.getSessionId());
            if (sessionInformation != null) {
                return sessionInformation;
            } else {
                return null;
            }
        }
        return null;
    }

    public Boolean isSsoEnabled() {
        String[] activeProfiles = environment.getActiveProfiles();
        Boolean ssoEnabled = false;
        if ((activeProfiles != null) && (activeProfiles.length > 0) && (Arrays.asList(activeProfiles).contains("sso"))) {
            ssoEnabled = true;
        }

        String[] defaultProfiles = environment.getDefaultProfiles();
        if ((defaultProfiles != null) && (defaultProfiles.length > 0) && (Arrays.asList(defaultProfiles).contains("sso"))) {
            ssoEnabled = true;
        }
        return ssoEnabled;
    }

    private Integer getDelay() {
        ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "config.concurrency.logout.delay");
        if (configurationVO == null || configurationVO.getPropertyValue() == null) {
            return DEFAULT_DELAY;
        }
        String highConcurrencyDelayValue = configurationVO.getPropertyValue();
        if (NumberUtils.isNumber(highConcurrencyDelayValue)) {
            return NumberUtils.toInt(highConcurrencyDelayValue, DEFAULT_DELAY);
        } else {
            return DEFAULT_DELAY;
        }
    }

    private void activateExecutor() {
        if (scheduledExecutorService == null || scheduledExecutorService.isShutdown() || scheduledExecutorService.isTerminated()) {
            scheduledExecutorService = Executors.newScheduledThreadPool(2);
        }
        executorActive = true;
    }

    private void deActivateExecutor() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
        executorActive = false;
    }


    private void save(SessionModuleMapping sessionModuleMapping) {
        entityDao.persist(sessionModuleMapping);
    }


    private void delete(String sessionId) {
        String deleteQueryString = "delete from SessionModuleMapping smm where smm.sessionId = :sessionId ";
        Query query = entityDao.getEntityManager().createQuery(deleteQueryString);
        query.setParameter("sessionId", sessionId);
        query.executeUpdate();
    }

    private List<String> getAllLoggedInUserForModule(String module) {
        if(StringUtils.isEmpty(module)){
            module = ProductInformationLoader.getProductName();
        }
        String getQuery = "select s.userId from SessionModuleMapping s where module = :moduleName and ( markedForLogout !=:markedForLogout or markedForLogout is null )";
        JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor<>(getQuery);
        jpaQueryExecutor.addParameter("moduleName", module);
        jpaQueryExecutor.addParameter("markedForLogout", "YES");
        List result = entityDao.executeQuery(jpaQueryExecutor);
        if (CollectionUtils.isNotEmpty(result)) {
            return result;
        }
        return new ArrayList<>();
    }


}
