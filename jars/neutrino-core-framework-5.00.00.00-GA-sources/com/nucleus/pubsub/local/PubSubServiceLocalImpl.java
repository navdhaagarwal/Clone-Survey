package com.nucleus.pubsub.local;

import com.nucleus.pubsub.PubSubListener;
import com.nucleus.pubsub.PubSubService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;



public class PubSubServiceLocalImpl implements PubSubService{

	private AtomicInteger listenerId = new AtomicInteger(0);
	private Map<String,Map<Integer,PubSubListener>> listenerMap = new ConcurrentHashMap<>();
	private Object lock = new Object();

	@Override
	public void publishOnTopic(Object message, String topicName) {
		getListenerMapForTopic(topicName).values().forEach(listener -> listener.onMessage(message));
	}

	@Override
	public int subscribeToTopic(String topicName, PubSubListener listener) {
		int id = listenerId.incrementAndGet();
		getListenerMapForTopic(topicName).put(id, listener);
		return id;
	}

	@Override
	public void unSubscribeFromTopic(String topicName, int listenerId) {
		getListenerMapForTopic(topicName).remove(listenerId);
		removeFromListenerMapIfRequired(topicName);
	}

	private Map<Integer, PubSubListener> getListenerMapForTopic(String topicName) {
		Map<Integer, PubSubListener> map = listenerMap.get(topicName);
		if (map == null) {
			synchronized (lock) {
				if (listenerMap.get(topicName) == null) {
					map = new ConcurrentHashMap<>();
					listenerMap.put(topicName, map);
				}
			}
		}
		return map;
	}

	private void removeFromListenerMapIfRequired(String topicName) {
		if (listenerMap.get(topicName).isEmpty()) {
			synchronized (lock) {
				if (listenerMap.get(topicName).isEmpty()) {
					listenerMap.remove(topicName);
				}
			}
		}
	}

}
