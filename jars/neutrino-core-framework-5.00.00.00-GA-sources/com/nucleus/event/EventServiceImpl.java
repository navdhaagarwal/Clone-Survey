/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.notification.service.NotificationService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoSessionInformation;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.template.TemplateService;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import net.bull.javamelody.MonitoredWithSpring;
/**
 * @author Nucleus Software Exports Limited
 * Service to perform event related operations.
 */
@Named("eventService")
public class EventServiceImpl extends BaseServiceImpl implements EventService {

    Configuration                      cfg                                         = new Configuration();
    StringTemplateLoader               stringTemplateLoader                        = new StringTemplateLoader();
    
    private static final String        ERROR_MSG                  				   = "userUri can not be null";		
    private static final String        EVENTS_BY_OWNER_ENTITY_URI                  = "Generic.getEventsByOwnerEntityUri";
    private static final String        EVENTS_BY_TYPE_AND_ASSOCIATED_USER_URI      = "Generic.getEventsByTypeAndAssociatedUserUri";
    private static final String        EVENTS_BY_WATCHER_USER_URI                  = "Generic.getEventsByWatcherUserUri";
    private static final String        EVENTS_BY_USER_URI                          = "Generic.getEventsByUserUri";
    private static final String        GET_USER_EVENTS_BEFORE_DATE                 = "Generic.getUserEventsBeforeDate";
    private static final String        EVENTS_BY_USER_URI_WITH_INCLUDE             = "Generic.getEventsByUserUriWithEventsToInclude";
    private static final String        ASSOCIATED_EVENTS_BY_USER_URI_WITH_EXCLUDE  = "Generic.getAssociatedEventsByUserUriWithExclude";
    private static final String        ASSOCIATED_EVENTS_BY_USER_URI               = "Generic.getAssociatedEventsByUserUri";
    private static final String        EVENTS_BY_USER_URI_WITH_INCLUDE_AFTER_EVENT = "Generic.getEventsByUserUriWithIncludeAfterGivenEvent";
    private static final String        ENTITY_NAME 								   = "ENTITY_NAME";
    // to get only login logout events
    private static final List<Integer> LOGIN_LOGOUT_EVENT_TYPES_LIST               = Collections
                                                                                           .unmodifiableList(Arrays
                                                                                                   .asList(EventTypes.USER_SECURITY_TRAIL_LOGIN_FAIL,
                                                                                                           EventTypes.USER_SECURITY_TRAIL_LOGIN_SUCCESS,
                                                                                                           EventTypes.USER_SECURITY_TRAIL_LOGOUT));

    // to get only successful login events
    private static final List<Integer> LOGIN_SUCCESS_EVENT_TYPES_LIST              = Collections
                                                                                           .unmodifiableList(Arrays
                                                                                                   .asList(EventTypes.USER_SECURITY_TRAIL_LOGIN_SUCCESS));

    //21 code stands for USER_SECURITY_TRAIL_LOGOUT event type.
    private static final int LOG_OUT_TYPE_ADMIN_EVENT_TYPE = 211;				//21.1
    private static final int LOG_OUT_TYPE_INACTIVITY_EVENT_TYPE = 212;			//21.2
    private static final int LOG_OUT_TYPE_DIFF_DEVICE_BROWSER_EVENT_TYPE = 213;	//21.3
    private static final int LOG_OUT_TYPE_SESSION_TIME_OUT_EVENT_TYPE = 214;		//21.4

    private static final List<Integer> LOGOUT_EVENT_TYPE_LIST                       = Collections
                                                                                            .unmodifiableList(Arrays
                                                                                                    .asList(EventTypes.USER_SECURITY_TRAIL_LOGOUT));

    
    @Inject
    @Named("frameworkMessageSource")
    protected MessageSource            messageSource;

    @Inject
    @Named("templateService")
    protected TemplateService          templateService;

    @Inject
    @Named("userService")
    protected UserService              userService;
    
    @Inject
    @Named("makerCheckerHelper")
    protected MakerCheckerHelper              makerCheckerHelper;

    @Inject
    @Named("notificationService")
    private	NotificationService		   notificationService;
    
    @Inject
    @Named("coreUtility")
	private CoreUtility coreUtility;
    
    @Override
    public void createEventEntry(Event genericEvent) {
        entityDao.persist(genericEvent);
    }

    @Override
    public List<Event> getAllEventsByOwnerEntityUri(String ownerEntityUri) {

        NeutrinoValidator.notNull(ownerEntityUri, "ownerEntityUri can not be null");
        NamedQueryExecutor<Event> eventQueryExecutor = new NamedQueryExecutor<Event>(EVENTS_BY_OWNER_ENTITY_URI)
                .addParameter("ownerEntityUri", ownerEntityUri);
        return entityDao.executeQuery(eventQueryExecutor);

    }

    @Override
    public void copyExistingEventsToChangedEntity(String fromOwnerEntityUri, String toOwnerEntityUri) {
        List<Event> events = getAllEventsByOwnerEntityUri(fromOwnerEntityUri);
        if (!CollectionUtils.isEmpty(events)) {
            for (Event genericEvent : events) {
                Event event = new GenericEvent((GenericEvent) genericEvent);
                event.setStandardEventPropertiesUsingEntity((BaseEntity) entityDao.get(EntityId.fromUri(toOwnerEntityUri)));
                createEventEntry(event);
            }
        }
    }

    @Override
    public List<Event> getAllEventsByWatcherUseruri(String watcherUserUri) {

        NeutrinoValidator.notNull(watcherUserUri, "watcherUserUri can not be null");
        NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(EVENTS_BY_WATCHER_USER_URI)
                .addParameter("watcherUserUri", watcherUserUri);
        return entityDao.executeQuery(genericEventExecutor);

    }

    @Override
    public List<Event> getLimitedEventsByWatcherUseruri(String watcherUserUri, Integer numberOfEventsToFetch) {

        return getPaginatedEventsByWatcherUseruri(watcherUserUri, 0, numberOfEventsToFetch);

    }

    @Override
    public List<Event> getPaginatedEventsByWatcherUseruri(String watcherUserUri, Integer startIndex, Integer pageSize) {

        NeutrinoValidator.notNull(watcherUserUri, "watcherUserUri can not be null");
        NeutrinoValidator.notNull(startIndex, "startIndex can not be null");
        NeutrinoValidator.notNull(pageSize, "pageSize can not be null");

        NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(EVENTS_BY_WATCHER_USER_URI)
                .addParameter("watcherUserUri", watcherUserUri);

        return entityDao.executeQuery(genericEventExecutor, startIndex, pageSize);

    }

    /**
     * Method used to fetch all events where user is directly associated with
     * event(byAssociatedUserUri). User may or may not be a watcher for event's
     * owner entity . (we also filter events based on event type )
     *
     * @see com.nucleus.event.EventService#getPaginatedEventsByTypeAndAssociatedUseruri(java.util.List,
     *      java.lang.String, java.lang.Integer, java.lang.Integer)
     */
    @Override
    public List<Event> getPaginatedEventsByTypeAndAssociatedUseruri(List<Integer> eventTypeList, String associatedUserUri,
            Integer startIndex, Integer pageSize) {

        NeutrinoValidator.notNull(eventTypeList, "eventTypeList can not be null");
        NeutrinoValidator.notNull(associatedUserUri, "associatedUserUri can not be null");

        NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(
                EVENTS_BY_TYPE_AND_ASSOCIATED_USER_URI).addParameter("eventTypeList", eventTypeList).addParameter(
                "associatedUserUri", associatedUserUri);

        if (startIndex != null && pageSize != null) {
            return entityDao.executeQuery(genericEventExecutor, startIndex, pageSize);
        } else
            return entityDao.executeQuery(genericEventExecutor);

    }

    @Override
    public List<Event> getLimtedSecurityEventsByAssociatedUseruri(String associatedUserUri, Integer pageSize) {

        NeutrinoValidator.notNull(pageSize, "pageSize can not be null");
        return getPaginatedEventsByTypeAndAssociatedUseruri(LOGIN_LOGOUT_EVENT_TYPES_LIST, associatedUserUri, 0, pageSize);

    }

    @Override
    public List<Event> getAllSecurityEventsByAssociatedUseruri(String associatedUserUri) {

        return getPaginatedEventsByTypeAndAssociatedUseruri(LOGIN_LOGOUT_EVENT_TYPES_LIST, associatedUserUri, null, null);

    }

    @Override
    @MonitoredWithSpring(name = "ESI_FETCH_LAST_SUCCESS_LOGIN_BY_USER")
    public Event getLastSuccessLoginEventByAssociatedUseruri(String associatedUserUri) {

        List<Event> eventList = getPaginatedEventsByTypeAndAssociatedUseruri(LOGIN_SUCCESS_EVENT_TYPES_LIST,
                associatedUserUri, 0, 1);

        if(eventList!=null && !eventList.isEmpty()){
        	if(coreUtility.isSsoEnabled() && eventList.size()>=2){
         	   return eventList.get(1);
            }else{
            	return eventList.get(0);
            }
        }
        return null;
    }

    @Override
	public List<Event> getPaginatedEventsForUser(String userUri, Integer startIndex, Integer pageSize) {

		NeutrinoValidator.notNull(userUri, ERROR_MSG);
		NeutrinoValidator.notNull(startIndex, "startIndex can not be null");
		NeutrinoValidator.notNull(pageSize, "pageSize can not be null");

		List<Integer> applicableEvents = notificationService.getApplicableEvents();
		if (applicableEvents.isEmpty()) {
			// Failure prevention by putting '0' as no event type have a status of '0'.
			applicableEvents.add(0);
		}
		NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(EVENTS_BY_USER_URI_WITH_INCLUDE)
				.addParameter("associatedUserUri", userUri).addParameter("eventsToInclude", applicableEvents);

		return entityDao.executeQuery(genericEventExecutor, startIndex, pageSize);

	}

    @Override
    public List<Event> getPaginatedLoginLogoutAndUserEvents(String userUri, Integer startIndex, Integer pageSize, String eventType) {

        NeutrinoValidator.notNull(userUri, ERROR_MSG);
        NeutrinoValidator.notNull(startIndex, "startIndex can not be null");
        NeutrinoValidator.notNull(pageSize, "pageSize can not be null");
        List<Integer> applicableEvents = notificationService.getUserEvents(eventType);
        if (applicableEvents.isEmpty()) {
            // Failure prevention by putting '0' as no event type have a status of '0'.
            applicableEvents.add(0);
        }
        NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(EVENTS_BY_USER_URI_WITH_INCLUDE)
                .addParameter("associatedUserUri", userUri).addParameter("eventsToInclude", applicableEvents);
        return entityDao.executeQuery(genericEventExecutor, startIndex, pageSize);
    }

    @Override
    public String getEventTypeStringRepresentation(Event event, Locale locale, FormatType formatType) {

        return getEventTypeStringRepresentation(event, locale, formatType, null);

    }

    @Override
    public String getEventTypeStringRepresentation(Event event, Locale locale, FormatType formatType,
            Map<String, String> otherContextProperties) {
        NeutrinoValidator.notNull(event, "event can not be null");
        NeutrinoValidator.notNull(formatType, "formatType can not be null");
        
        
        int eventType = event.getEventType();

        Map<String, String> consolidatedContextMap = event.getPersistentPropertyMap();
              
        if (ValidatorUtils.notNull(consolidatedContextMap.get(ENTITY_NAME))) {
        consolidatedContextMap.put(ENTITY_NAME, makerCheckerHelper.getEntityDescription(consolidatedContextMap.get(ENTITY_NAME)));
        }
        String eventActionTakenByUsername = event.getAssociatedUserUri() != null ? userService.getUserNameByUserUri(event
                .getAssociatedUserUri()) : "system";
        consolidatedContextMap.put("ASSOCIATED_USER", eventActionTakenByUsername);
        if (event.getEventTimestamp() != null) {
            consolidatedContextMap.put("EVENT_TIMESTAMP",
                    DateUtils.getFormattedDate(event.getEventTimestamp(), getUserPreferredDateTimeFormat()));
        }
        if (otherContextProperties != null) {
            consolidatedContextMap.putAll(otherContextProperties);
        }
        
        if(consolidatedContextMap.get(UserSecurityTrailEvent.ENTITY_USER_LOGOUT_TYPE) != null && StringUtils.isNotEmpty(consolidatedContextMap.get(UserSecurityTrailEvent.ENTITY_USER_LOGOUT_TYPE) )){
        	eventType = updateEventTypeValueForLogoutHistoryMessageTemplate(consolidatedContextMap.get(UserSecurityTrailEvent.ENTITY_USER_LOGOUT_TYPE),eventType,consolidatedContextMap);
        } 

        String eventStringRepresentation = "";
        try {
            eventStringRepresentation = templateService.getResolvedStringFromResourceBundle(
                    formatType.getKey(eventType), locale, consolidatedContextMap);
        } catch (Exception e) {
            throw new SystemException("Exception while loading event type string representation for event: " + event, e);
        }
        return eventStringRepresentation;

    }

   /**
     * It uses include query which might misguide.
     * But all the exclude events list already removed from applicable events
     * using <code>Collection.removeAll(Collection<?> c)</code>.
     * 
     */
  

    private int updateEventTypeValueForLogoutHistoryMessageTemplate(String logOutType,int defaultEventType,Map<String, String> consolidatedContextMap) {    	
    	if(logOutType.equals(NeutrinoSessionInformation.LOGOUT_TYPE_BY_ADMIN)){
    		defaultEventType = LOG_OUT_TYPE_ADMIN_EVENT_TYPE;    		
    		Long  userId = Long.valueOf(consolidatedContextMap.get(UserSecurityTrailEvent.ENTITY_USER_LOGOUT_BY));
    		String userName = userService.getUserNameByUserId(userId);
    		consolidatedContextMap.put(UserSecurityTrailEvent.ENTITY_USER_LOGOUT_BY, userName);
    	}else if(logOutType.equals(NeutrinoSessionInformation.LOGOUT_TYPE_BY_INACTIVITY)){
    		defaultEventType = LOG_OUT_TYPE_INACTIVITY_EVENT_TYPE;
    	}else if(logOutType.equals(NeutrinoSessionInformation.LOGOUT_TYPE_ON_DIFF_DEVICE_BROWSER)){
    		defaultEventType = LOG_OUT_TYPE_DIFF_DEVICE_BROWSER_EVENT_TYPE;
    	}else if(logOutType.equals(NeutrinoSessionInformation.LOGOUT_TYPE_ON_SESSION_TIME_OUT)){
    		defaultEventType = LOG_OUT_TYPE_SESSION_TIME_OUT_EVENT_TYPE;
    	}
    	
    	return defaultEventType;
    }

	@Override
    @MonitoredWithSpring(name = "ESI_FETCH_PAGI_EVNT_FOR_USER_WITH_EXCLUDE")
    public List<Event> getPaginatedEventsForUserWithExclude(String userUri, Integer startIndex, Integer pageSize,
            List<Integer> excludeEventTypeList) {

        NeutrinoValidator.notNull(userUri, ERROR_MSG);
        NeutrinoValidator.notNull(startIndex, "startIndex can not be null");
        NeutrinoValidator.notNull(pageSize, "pageSize can not be null");
        List<Integer> applicableEvents = notificationService.getApplicableEvents();
        applicableEvents.removeAll(excludeEventTypeList);						//Remove all the events that are to be excluded.
        if (applicableEvents.isEmpty()) {
        	//Failure prevention by putting '0' as no event type have a status of '0'.
        	applicableEvents.add(0);
        }
        NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(EVENTS_BY_USER_URI_WITH_INCLUDE)
                .addParameter("associatedUserUri", userUri)
                .addParameter("eventsToInclude", applicableEvents);
        return entityDao.executeQuery(genericEventExecutor, startIndex, pageSize);

    }

    @Override
    public List<Event> getAssociatedPaginatedEventsForUserWithExclude(String userUri, Integer startIndex, Integer pageSize,
            List<Integer> excludeEventTypeList) {

        NeutrinoValidator.notNull(userUri, ERROR_MSG);
        NeutrinoValidator.notNull(startIndex, "startIndex can not be null");
        NeutrinoValidator.notNull(pageSize, "pageSize can not be null");

        NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(
                ASSOCIATED_EVENTS_BY_USER_URI_WITH_EXCLUDE).addParameter("associatedUserUri", userUri).addParameter(
                "excludeEventTypeList", excludeEventTypeList);

        return entityDao.executeQuery(genericEventExecutor, startIndex, pageSize);

    }

    @Override
    public List<Event> getAssociatedPaginatedEventsForUser(String userUri, Integer startIndex, Integer pageSize) {

        NeutrinoValidator.notNull(userUri, ERROR_MSG);
        NeutrinoValidator.notNull(startIndex, "startIndex can not be null");
        NeutrinoValidator.notNull(pageSize, "pageSize can not be null");

        NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(ASSOCIATED_EVENTS_BY_USER_URI)
                .addParameter("associatedUserUri", userUri);

        return entityDao.executeQuery(genericEventExecutor, startIndex, pageSize);

    }

    @Override
    @MonitoredWithSpring(name = "ESI_FETCH_PAGI_EVNT_FOR_USER")
    public List<Event> getPaginatedEventsForUserWithExcludeAfterEvent(String userUri, Integer startIndex, Integer pageSize,
			List<Integer> excludeEventTypeList, long lastEventId) {

		NeutrinoValidator.notNull(userUri, ERROR_MSG);
		NeutrinoValidator.notNull(startIndex, "startIndex can not be null");
		NeutrinoValidator.notNull(pageSize, "pageSize can not be null");
		NeutrinoValidator.notNull(lastEventId, "latestEventId can not be null");

		List<Integer> applicableEvents = notificationService.getApplicableEvents();
		applicableEvents.removeAll(excludeEventTypeList); // Remove all the events that are to be excluded.
		if (applicableEvents.isEmpty()) {
			// Failure prevention by putting '0' as no event type have a status of '0'.
			applicableEvents.add(0);
		}

		NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(
				EVENTS_BY_USER_URI_WITH_INCLUDE_AFTER_EVENT).addParameter("associatedUserUri", userUri)
						.addParameter("eventsToInclude", applicableEvents).addParameter("lastEventId", lastEventId);

		return entityDao.executeQuery(genericEventExecutor, startIndex, pageSize);
	}

    @Override
    public void deleteUserEventsBeforeDate(String userUri, DateTime date) {

        BaseLoggers.flowLogger.debug("Deleting user events for userURI:" + userUri + " before date " + date.toString());

        // fetch all events before specified date
        NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(GET_USER_EVENTS_BEFORE_DATE)
                .addParameter("associatedUserUri", userUri).addParameter("beforeDate", date);

        // execute query
        List<Event> deletionList = entityDao.executeQuery(genericEventExecutor);

        // delete events from the database
        for (Event e : deletionList) {
            BaseLoggers.flowLogger.debug("Deleting entry with id = " + e.getId());
            entityDao.delete(e);
        }

    }

    @Override
    public List<Event> getEventsForUser(String userUri) {

        NeutrinoValidator.notNull(userUri, ERROR_MSG);

        NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(EVENTS_BY_USER_URI).addParameter(
                "associatedUserUri", userUri);

        return entityDao.executeQuery(genericEventExecutor);

    }
    
    @Override
	public List<Event> getEventsForUserExcludingLoginLogout(String userUri) {

		NeutrinoValidator.notNull(userUri, ERROR_MSG);

		List<Integer> applicableEvents = notificationService.getApplicableEvents();
		applicableEvents.removeAll(LOGIN_LOGOUT_EVENT_TYPES_LIST); // Remove all the events that are to be excluded.
		if (applicableEvents.isEmpty()) {
			// Failure prevention by putting '0' as no event type have a status of '0'.
			applicableEvents.add(0);
		}
		NamedQueryExecutor<Event> genericEventExecutor = new NamedQueryExecutor<Event>(EVENTS_BY_USER_URI_WITH_INCLUDE)
				.addParameter("associatedUserUri", userUri).addParameter("eventsToInclude", applicableEvents);

		return entityDao.executeQuery(genericEventExecutor);

	}

    @Override
    public DateTime getLastEventCreatedTime(String ownerEntityUri, int eventType, String dataKey, String dataValue) {

        NeutrinoValidator.notNull(ownerEntityUri, "OwnerUri can not be null.");
        NeutrinoValidator.notNull(dataKey, "DataKey can not be null.");
        NeutrinoValidator.notNull(dataValue, "DataValue can not be null.");

        NamedQueryExecutor<Map<String,Object>> eventQueryExecutor = new NamedQueryExecutor<Map<String,Object>>("Generic.getLastEventCreatedTime")
                .addParameter("ownerEntityUri", ownerEntityUri).addParameter("eventType", eventType)
                .addParameter("dataKey", dataKey);
        List<Map<String,Object>> eventDataMapList = entityDao.executeQuery(eventQueryExecutor);
        if(CollectionUtils.isNotEmpty(eventDataMapList)) {
            Set<DateTime> dateTimeSet = eventDataMapList.stream().filter(elem -> elem.get("eventDataValue").equals(dataValue)).map(elem ->  (DateTime)elem.get("eventCreationTimeStamp")).collect(Collectors.toSet());
            return CollectionUtils.isNotEmpty(dateTimeSet) ? Collections.max(dateTimeSet) : null;
        }
        return null;
    }

    @Override
	public void createUserSecurityTrailEventEntry(NeutrinoSessionInformation neutrinoSessionInformation, Long userId, int eventType) {
		UserSecurityTrailEvent userSecurityTrailEvent = new UserSecurityTrailEvent(eventType);
		userSecurityTrailEvent.setRemoteIpAddress(neutrinoSessionInformation.getForceLogOutIP());
		userSecurityTrailEvent.setSessionId(neutrinoSessionInformation.getSessionId());
		userSecurityTrailEvent.setLogOutType(neutrinoSessionInformation.getLogOutType());
		if(userId!=null){
			userSecurityTrailEvent.setLogOutBy(userId.toString());
		}
		userSecurityTrailEvent.setForceLogOutIP(neutrinoSessionInformation.getForceLogOutIP());
		if(neutrinoSessionInformation.getPrincipal()!=null && UserInfo.class.isAssignableFrom(neutrinoSessionInformation.getPrincipal().getClass())){
			UserInfo userInfo = (UserInfo) neutrinoSessionInformation.getPrincipal();
		    userSecurityTrailEvent.setUsername(userInfo.getUsername());
		    userSecurityTrailEvent.setAssociatedUserUri(userInfo.getUserEntityId().getUri());
		    userSecurityTrailEvent.setModuleNameForEvent(ProductInformationLoader.getProductName()); 
		    createEventEntry(userSecurityTrailEvent);
		}
	}

    @Override
    public Event getLastLogoutEventByAssociatedUseruri(String associatedUserUri) {

        List<Event> eventList = getPaginatedEventsByTypeAndAssociatedUseruri(LOGOUT_EVENT_TYPE_LIST,
                associatedUserUri, 0, 1);

        return eventList != null && !eventList.isEmpty() ? eventList.get(0) : null;
    }
}
