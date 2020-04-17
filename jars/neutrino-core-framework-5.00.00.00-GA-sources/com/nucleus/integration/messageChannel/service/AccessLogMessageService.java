package com.nucleus.integration.messageChannel.service;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.messaging.Message;

import com.nucleus.core.accesslog.entity.AccessLog;

public interface AccessLogMessageService {

	public void persistAccessLog(Message<List<AccessLog>> message) throws DataAccessException, Exception;

}
