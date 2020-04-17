package com.nucleus.broadcast.service;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.nucleus.broadcast.entity.BroadcastMessage;
import com.nucleus.broadcast.vo.BroadcastMessageVO;

import reactor.core.publisher.FluxSink;

/**
 * @author shivendra.kumar
 *
 */
@Service
public interface BroadcastMessageService {

	Map<String, BroadcastMessageVO> publishBroadcastMessage();

	List<BroadcastMessage> fetchBroadcastMessageFromDB();

	void updateBroadcastMessageCache(Map<String, Object> dataMap);

	void updateLastExecTime(BroadcastMessageVO msg);

	long toSeconds(DateTime dateTime);

	long calculateStartDate(BroadcastMessageVO message);

	long calculateEndDate(BroadcastMessageVO message);

	String toJson(BroadcastMessageVO message);

	void removeFluxForLoggedOutUser(String userUUID);

	void broadcastMessage();

	BroadcastMessage getMessageById(Long id);

	void deleteBroadcastMessage(BroadcastMessage broadcastMessage);

	Boolean isMessageAvailableForModule();



}
