package com.nucleus.finnone.pro.queue;

public interface QueueProviderService {
	
	Object peek(String queueName);
	
	boolean isEmpty(String queueName);
	
	int size(String queueName);
	
	Object poll(String queueName);

	String offer(String queueName, Object message);

}
