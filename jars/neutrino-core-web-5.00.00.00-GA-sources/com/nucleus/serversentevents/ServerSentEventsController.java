package com.nucleus.serversentevents;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.common.controller.NonTransactionalBaseController;

import reactor.core.publisher.Flux;

/**
 * @author shivendra.kumar
 *
 */
@RestController
public class ServerSentEventsController extends NonTransactionalBaseController{
	
	@Inject
	@Named("serverSentEventsService")
	private ServerSentEventsService serverSentEventsService;
	
	

	@GetMapping(path = "/registerNewClient", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<String>> registerNewClient() {
		return createFlux(getUserDetails().getUuid());
	}
	
	


	/** Opens a new connection for each logged in user
	 * @param userId
	 * @return
	 */
	private Flux<ServerSentEvent<String>> createFlux(String userId) {
		NeutrinoValidator.notNull(userId, "User id found null while connecting to notification stream");
	
		String fluxId = UUID.randomUUID().toString();
		if (serverSentEventsService.getUserFluxMappingMap().get(userId) == null) {
			serverSentEventsService.getUserFluxMappingMap().put(userId, new ConcurrentHashMap<>());
		}
		return Flux.create((emitter) -> {
			serverSentEventsService.getUserFluxMappingMap().get(userId).put(fluxId, emitter);
			emitter.onCancel(() -> {
				BaseLoggers.flowLogger.trace("Detected connection break for FluxId: {} ", fluxId);
				if (serverSentEventsService.getUserFluxMappingMap().get(userId) == null) {

					return;
				}
				if (serverSentEventsService.getUserFluxMappingMap().get(userId).containsKey(fluxId)) {

					serverSentEventsService.getUserFluxMappingMap().get(userId).remove(fluxId);
				}
				if (serverSentEventsService.getUserFluxMappingMap().get(userId).isEmpty()) {

					serverSentEventsService.getUserFluxMappingMap().remove(userId);
				}
			});

		});

	}
	
	
	
	
	


}
