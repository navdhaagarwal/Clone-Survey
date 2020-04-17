package com.nucleus.serversentevents;

import java.util.Map;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import reactor.core.publisher.FluxSink;

/**
 * @author shivendra.kumar
 *
 */
@Service
public interface ServerSentEventsService {
	
	Map<String, Map<String, FluxSink<ServerSentEvent<String>>>> getUserFluxMappingMap();
	
	void setUserFluxMappingMap(Map<String, Map<String, FluxSink<ServerSentEvent<String>>>> userFluxMappingMap);

	/**Sends event to a particular logged in user
	 * @param obj
	 * @param event
	 * @param userName
	 * @return
	 */
	Boolean sendEventToParticularUser(Object obj,String event,String userName);

	/**Sends event to a all logged in users
	 * @param obj
	 * @param event
	 * @return
	 */
	Boolean sendEventToAllUsers(Object obj, String event);

}
