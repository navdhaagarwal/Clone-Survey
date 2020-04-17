package com.nucleus.pubsub;

import java.util.EventListener;

import org.springframework.stereotype.Service;

@Service
public interface PubSubService {

	/**Publishes specific message to specific topic. Returns number of subscribers receiving the message
	 * @param message
	 * @param topicName
	 * @return
	 */
	void publishOnTopic(Object message, String topicName);

	/**Subscribes to a particular topic in Redis. Returns number of subscriptions in that topic
	 * @param topicName
	 * @param listener
	 * @return
	 */
	int subscribeToTopic(String topicName, PubSubListener listener);

	void unSubscribeFromTopic(String topicName, int listenerId);

}
