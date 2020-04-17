package com.nucleus.event;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;

import com.nucleus.security.core.session.NeutrinoSessionInformation;

public interface EventService {

    public void createEventEntry(Event genericEvent);

    /**
     * Method used to fetch all events where given entity is owner.(here we
     * fetch events for entity.User and events type is not considered here)
     *
     * @param ownerEntityUri
     * @return
     */
    public List<Event> getAllEventsByOwnerEntityUri(String ownerEntityUri);

    /**
     * Method used to fetch all events of all entities watched by this user.
     * User may or may not be directly associated with
     * event(byAssociatedUserUri). (we also filter events based on event type )
     *
     * @param watcherUserUri
     * @return
     */
    public List<Event> getAllEventsByWatcherUseruri(String watcherUserUri);

    /**
     *
     * @param watcherUserUri
     * @param numberOfEventsToFetch
     * @return
     */
    public List<Event> getLimitedEventsByWatcherUseruri(String watcherUserUri, Integer numberOfEventsToFetch);

    /**
     *
     * @param watcherUserUri
     * @param startIndex
     * @param pageSize
     * @return
     */
    public List<Event> getPaginatedEventsByWatcherUseruri(String watcherUserUri, Integer startIndex, Integer pageSize);

    /**
     * Method used to fetch limited number of  security type events for this user.
     * @param associatedUserUri
     * @param pageSize
     * @return
     */
    public List<Event> getLimtedSecurityEventsByAssociatedUseruri(String associatedUserUri, Integer pageSize);

    /**
     * Method used to fetch all security type events for this user.
     * @param associatedUserUri
     * @return
     */
    public List<Event> getAllSecurityEventsByAssociatedUseruri(String associatedUserUri);

    /**
     * To get last successful login event for this user.
     * @param associatedUserUri
     * @return
     */
    public Event getLastSuccessLoginEventByAssociatedUseruri(String associatedUserUri);

    /**
     * Method used to fetch all events where user is either directly associated
     * with event(byAssociatedUserUri) Or is a watcher for event's owner entity.
     *
     * @param userUri
     * @param startIndex
     * @param pageSize
     * @return
     */
    public List<Event> getPaginatedEventsForUser(String userUri, Integer startIndex, Integer pageSize);

    /**
     *
     * @param event
     * @param locale
     * @param formatType
     * @return  A localized string representation for given event.
     */
    public String getEventTypeStringRepresentation(Event event, Locale locale, FormatType formatType);

    /**
     * @param event
     * @param locale
     * @param formatType
     * @param contextProperties
     * @return
     */
    public String getEventTypeStringRepresentation(Event event, Locale locale, FormatType formatType,
            Map<String, String> contextProperties);

    /**
     * Method used to fetch all events where user is either directly associated
     * with event(byAssociatedUserUri) Or is a watcher for event's owner entity,but it excludes all those events matching event type in
     * excludeEventTypeList
     *
     * @param userUri
     * @param startIndex
     * @param pageSize
     * @param excludeEventTypeList
     * @return
     */
    public List<Event> getPaginatedEventsForUserWithExclude(String userUri, Integer startIndex, Integer pageSize,
            List<Integer> excludeEventTypeList);

    public List<Event> getAssociatedPaginatedEventsForUserWithExclude(String userUri, Integer startIndex, Integer pageSize,
            List<Integer> excludeEventTypeList);

    public List<Event> getAssociatedPaginatedEventsForUser(String userUri, Integer startIndex, Integer pageSize);

    public List<Event> getPaginatedEventsForUserWithExcludeAfterEvent(String userUri, Integer startIndex, Integer pageSize,
            List<Integer> excludeEventTypeList, long latestEventId);

    public void copyExistingEventsToChangedEntity(String fromOwnerEntityUri, String toOwnerEntityUri);

    List<Event> getPaginatedEventsByTypeAndAssociatedUseruri(List<Integer> eventTypeList, String associatedUserUri,
            Integer startIndex, Integer pageSize);
    
    /**
     * Hard Deletes events before specified date for a particular user
     * @param userUri user's uri whose event needs to be deleted
     * @param date date prior which events need to be hard deleted
     */
    public void deleteUserEventsBeforeDate(String userUri,DateTime date);
        
    /**
     * Method used to fetch all events where user is either directly associated
     * with event(byAssociatedUserUri) Or is a watcher for event's owner entity,but it excludes all those events matching event type in
     * excludeEventTypeList
     *
     * @param userUri user's uri whose event list is requested
     * @return list of events
     */
    public List<Event> getEventsForUser(String userUri);

    /**
     * This method will give the last event created time.
     * 
     * @param ownerEntityUri
     * @param eventType
     * @param dataKey
     * @param dataValue
     * @return
     */

    public DateTime getLastEventCreatedTime(String ownerEntityUri, int eventType, String dataKey, String dataValue);

	void createUserSecurityTrailEventEntry(NeutrinoSessionInformation neutrinoSessionInformation, Long userId,
			int eventType);

    public Event getLastLogoutEventByAssociatedUseruri(String associatedUserUri);

    public List<Event> getEventsForUserExcludingLoginLogout(String userUri);
    List<Event> getPaginatedLoginLogoutAndUserEvents(String userUri, Integer startIndex, Integer pageSize, String eventType);
}