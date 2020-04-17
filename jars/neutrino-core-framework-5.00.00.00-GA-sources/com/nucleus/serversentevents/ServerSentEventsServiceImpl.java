package com.nucleus.serversentevents;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.http.codec.ServerSentEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserService;

import reactor.core.publisher.FluxSink;

/**
 * @author shivendra.kumar
 *
 */
@Named("serverSentEventsService")
public class ServerSentEventsServiceImpl implements ServerSentEventsService{
	
	@Inject
	@Named(value = "userService")
	private UserService userService;
	
	
	/**
	 * Stores Flux Sink of each user. This sink can be used to send specific message to specific user.
	 */
	private Map<String, Map<String, FluxSink<ServerSentEvent<String>>>> userFluxMappingMap = new ConcurrentHashMap<>();

	@Override
	public Map<String, Map<String, FluxSink<ServerSentEvent<String>>>> getUserFluxMappingMap() {
		return userFluxMappingMap;
	}

	@Override
	public void setUserFluxMappingMap(Map<String, Map<String, FluxSink<ServerSentEvent<String>>>> userFluxMappingMap) {
		this.userFluxMappingMap = userFluxMappingMap;
	}
	
	
	/**Convert Objects to ServerSentEvent and takes message and type of event.
	 * @param obj
	 * @param event
	 * @return7
	 */
	private ServerSentEvent<String> objectToSSE(Object obj, String event) {

		ObjectWriter ow = new ObjectMapper().writer();
		String data;
		try {
			data = ow.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			BaseLoggers.flowLogger.error("Exception while parsing Json from object : {}",e.getMessage());
			return null;
		}
		return ServerSentEvent.<String>builder().event(event).data(data).build();	
		}
	
	@Override
	public Boolean sendEventToParticularUser(Object obj,String event,String userName) {
		String userUuid = userService.getUserFromUsername(userName).getUuid();
		Map<String,FluxSink<ServerSentEvent<String>>> userSinkMap =  userFluxMappingMap.get(userUuid);
		if(null!=userSinkMap && !userSinkMap.isEmpty()) {
			userSinkMap.values().stream().forEach(sink -> sink.next(objectToSSE(obj, event)));
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	@Override
	public Boolean sendEventToAllUsers(Object obj,String event) {
		if(null!=userFluxMappingMap && !userFluxMappingMap.isEmpty()) {
			userFluxMappingMap.values().parallelStream()
			.forEach(entry -> entry.values().stream().forEach(emitter -> emitter.next(objectToSSE(obj, event))));
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
		
	}
	
	
}
