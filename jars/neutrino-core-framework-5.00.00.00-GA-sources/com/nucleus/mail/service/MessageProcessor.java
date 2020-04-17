package com.nucleus.mail.service;

import javax.mail.Message;

@FunctionalInterface
public interface MessageProcessor<T> {
	
	public T processMessage(Message message);

}
