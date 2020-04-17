package com.nucleus.pubsub.redis;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.pubsub.PubSubListener;
import com.nucleus.pubsub.PubSubService;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PubSubServiceRedisImpl implements PubSubService {


    private RedissonClient redissonClient;
    private Map<String, RTopic> topicMap = new ConcurrentHashMap<>();
    private Object lock = new Object();

    public PubSubServiceRedisImpl(RedissonClient client) {
        this.redissonClient = client;
    }

    @PostConstruct
    public void init() {
        schedule();
    }


    /* (non-Javadoc)
     * @see com.nucleus.pubsub.PubSubService#publishOnTopic(java.lang.Object, java.lang.String)
     * Publishes a particular topic in Redis. Message and Topic is taken as argumnet.
     */
    @Override
    public void publishOnTopic(Object message, String topicName) {
        getRTopicFromMap(topicName).publish(message);
    }


    /**
     * Subscribes to a particular topic in Redis. org.redisson.api.listener.MessageListener should be passed to this function
     *
     * @param topicName
     * @param listener
     * @return
     */
    @Override
    public int subscribeToTopic(String topicName, PubSubListener listener) {
        MessageListener redisMessageListener = (channel, msg) -> listener.onMessage(msg);
        return getRTopicFromMap(topicName).addListener(Object.class, redisMessageListener);
    }

    @Override
    public void unSubscribeFromTopic(String topicName, int listenerId) {
        getRTopicFromMap(topicName).removeListener(listenerId);
    }


    private RTopic getRTopicFromMap(String topicName) {
        RTopic topic = topicMap.get(topicName);
        if (topic == null) {
            synchronized (lock) {
                if (topicMap.get(topicName) == null) {
                    topic = redissonClient.getTopic(topicName);
                    topicMap.put(topicName, topic);
                }
            }
        }
        return topic;
    }

    private void schedule() {
        Flux.interval(Duration.ofMinutes(15)).flatMap(count -> {
            cleanUpEmptyTopicsFromTopicMap();
            return Flux.empty();
        }).onErrorResume(error -> {
            schedule();
            return Flux.empty();
        }).subscribe();
    }

    private void cleanUpEmptyTopicsFromTopicMap() {
        topicMap.entrySet().stream().forEach(entry -> {
            if (entry.getValue().countSubscribers() < 1) {
                topicMap.remove(entry.getKey());
            }
        });
    }

}
