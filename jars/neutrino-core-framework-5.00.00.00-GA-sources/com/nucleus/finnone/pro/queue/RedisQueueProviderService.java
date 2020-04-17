package com.nucleus.finnone.pro.queue;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RQueue;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.listener.StatusListener;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;

import reactor.core.publisher.Flux;

/**
 * @author shivendra.kumar
 *
 */
public class RedisQueueProviderService implements QueueProviderService {

	public static final String QUEUED = "QUEUED";
	public static final String PROCESSING = "PROCESSSING";
	public static final String PROCESSED = "PROCESSED";
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILED = "FAILED";
	public static final String QUEUE_TOPIC = "QueueTopic";
	public static final String QUEUE_SUCCESS_LOGGING_STREAM = "QueueSuccessLoggingStream";
	public static final String QUEUE_FAILED_LOGGING_STREAM = "QueueFailedLoggingStream";
	public static final String QUEUE = "Queue";
	public static final String FAILED_ATTEMPT_COUNTER = "FailedAttemptCounter";
	public static final String QUEUE_STATE_MAP = "QueueStateMap";
	public static final String QUEUE_SCHEDULER_LOCK = "QueueSchedulerLock";
	public static final String DASH_DELIMITER="-";

	/**
	 * Flag for enabling success logging of all messages.
	 */
	@Value("${queue.success.logging:false}")
	private boolean successLoggingFlag;

	private RedissonClient redissonClient;

	public RedisQueueProviderService(RedissonClient client) {
		this.redissonClient = client;
		this.moveFailedMessagesBackToQueue();
	}

	/**
	 * Peek element from queue without popping it
	 * 
	 * @param queueName
	 * @return
	 */
	public QueueElement peek(String queueName) {
		RQueue<QueueElement> queue = redissonClient.getQueue(this.generateQueueName(queueName));
		return queue.peek();
	}

	public boolean isEmpty(String queueName) {
		RQueue<QueueElement> queue = redissonClient.getQueue(this.generateQueueName(queueName));
		return queue.isEmpty();
	}

	public int size(String queueName) {
		RQueue<QueueElement> queue = redissonClient.getQueue(this.generateQueueName(queueName));
		return queue.size();
	}

	/**
	 * Polls message from the queue. Special mechanism using pub-sub to guarantee
	 * message delivery
	 * 
	 * @param queueName
	 * @return
	 */
	public QueueElement poll(String queueName) {
		RQueue<QueueElement> queue = redissonClient.getQueue(this.generateQueueName(queueName));
		QueueElement value = queue.peek();
		if (value == null) {
			return value;
		}
		RLock lock = this.redissonClient
				.getLock(generateQueueName(queueName) + FWCacheConstants.KEY_DELIMITER + value.getMessageId());
		boolean isAcquired = lock.tryLock();

		if (!isAcquired) {
			return this.poll(queueName);
		}

		if (!value.equals(queue.peek())) {
			lock.unlock();
			return this.poll(queueName);
		}

		if (!value.isMessageGuarantee()) {
			return this.pollUnguaranteed(queue,lock,queueName);
		}

		return this.pollGuaranteed(queue,lock,queueName,value);
	}

	private void addMessageToProcessingMap(QueueElement element, String queueName) {
		RMap<String, QueueElement> map = redissonClient.getMap(generateMapName(queueName));
		map.put(element.getMessageId(), element);
	}
	

	/**
	 * Adds a new message in the queue and return the generated correlation id.
	 * 
	 * @param queueName
	 * @param messageObject
	 * @return
	 */
	@Override
	public String offer(String queueName, Object message) {
		QueueElement messageObject = (QueueElement) message;
		String msgId = String.valueOf(System.currentTimeMillis()) + DASH_DELIMITER + UUID.randomUUID();
		messageObject.setMessageId(msgId);
		redissonClient.getQueue(this.generateQueueName(queueName)).add(messageObject);
		return msgId;
	}

	/**
	 * Must be called in case of message guarantee for acknowledgement
	 * 
	 * @param queueName
	 * @param messageId
	 * @return 
	 */
	public boolean onSuccess(String queueName, String messageId) {
		RMap<String,QueueElement> map = redissonClient.getMap(generateMapName(queueName));
		QueueElement element = map.get(messageId);
		if(element!=null) {
			if (successLoggingFlag) {
				redissonClient.getStream(generateSuccessLoggingStreamName()).add(messageId, SUCCESS);
			}
			map.remove(messageId);
			redissonClient.getTopic(generateTopicName(messageId)).removeAllListeners();
			redissonClient.getAtomicLong(this.generateFailedAttemptCounterName(messageId))
			.unlink();
			return true;
		}
		return false;
		
	}

	private String generateTopicName(String msgId) {
		return msgId + FWCacheConstants.KEY_DELIMITER + QUEUE_TOPIC;
	}

	private String generateSuccessLoggingStreamName() {
		return QUEUE_SUCCESS_LOGGING_STREAM;
	}

	private String generateFailedLoggingStreamName() {
		return QUEUE_FAILED_LOGGING_STREAM;
	}

	private String generateQueueName(String queueName) {
		return queueName + FWCacheConstants.KEY_DELIMITER + QUEUE;
	}

	private String generateFailedAttemptCounterName(String id) {
		return id + FWCacheConstants.KEY_DELIMITER + FAILED_ATTEMPT_COUNTER;
	}

	private String getUniqueQueueName(String redisQueueName) {
		String[] array = redisQueueName.split(FWCacheConstants.REGEX_DELIMITER);
		return array[0];
	}

	private String generateMapName(String queueName) {
		return queueName + FWCacheConstants.KEY_DELIMITER + QUEUE_STATE_MAP;
	}
	
	public Set<String> fetchAllSuccessfulMessageIds() {
		RStream<String, String> successStream = redissonClient.getStream(generateSuccessLoggingStreamName());
		return successStream.range(StreamMessageId.MIN, StreamMessageId.MAX).values().stream()
				.map(localmap -> localmap.keySet().iterator().next()).collect(Collectors.toSet());
	}
	
	public Set<Entry<String, String>> fetchAllFailedMessage() {
		RStream<String,String> failureStream = redissonClient.getStream(generateFailedLoggingStreamName());
		return failureStream.range(StreamMessageId.MIN, StreamMessageId.MAX).values().stream()
				.map(localmap -> localmap.entrySet().iterator().next()).collect(Collectors.toSet());
	}
	
	public QueueElement fetchFailedMessage(String msgId) {
		RStream<String, QueueElement> stream =  redissonClient.getStream(generateFailedLoggingStreamName());
		Collection<Map<String, QueueElement>> list = stream.range(StreamMessageId.MIN, StreamMessageId.MAX)
				.values();
		for(Map<String, QueueElement> map:list) {
			Entry<String, QueueElement> entry = map.entrySet().iterator().next();
			if(entry.getKey().equals(msgId)) {
				return entry.getValue();
			}
		}
		return null;
		
	}
	
	private QueueElement pollUnguaranteed(RQueue<QueueElement> queue,RLock lock,String queueName) {
		QueueElement element = queue.poll();
		if (successLoggingFlag) {
			redissonClient.getStream(generateSuccessLoggingStreamName()).add(element.getMessageId(), SUCCESS);
			}
		lock.unlock();
		return element;
	}
	
	
	private QueueElement pollGuaranteed(RQueue<QueueElement> queue,RLock lock,String queueName,QueueElement element) {
		this.addMessageToProcessingMap(element, queueName);
		this.redissonClient.getTopic(generateTopicName(element.getMessageId())).addListener(new StatusListener() {
			public void onUnsubscribe(String channel) {
				// do nothing
			}

			public void onSubscribe(String channel) {
				// do nothing
			}
		});
		element = queue.poll();
		lock.unlock();
		return element;
	}
	
	

	/**
	 * Moves failed messages back to queue for processing once it detects
	 * application which was processing the message has crashed. After 3 failed
	 * attempts of processing it is finally moved in a FAILED Redis stream.
	 */
	private void moveFailedMessagesBackToQueue() {
		Flux.interval(Duration.ofMinutes(30)).flatMap(l -> {
			RLock lock = redissonClient.getLock(QUEUE_SCHEDULER_LOCK);
			if (lock.tryLock()) {
				redissonClient.getKeys().getKeysByPattern("*" + FWCacheConstants.KEY_DELIMITER + QUEUE_STATE_MAP + "*")
						.forEach(mapName -> {
							RMap<String, QueueElement> map = redissonClient.getMap(mapName);
							map.entrySet().forEach(entry -> {
								String msgId = entry.getKey();
								QueueElement element = entry.getValue();
								if (redissonClient.getTopic(this.generateTopicName(msgId)).countListeners() == 0) {
									long failedAttempts = redissonClient
											.getAtomicLong(this.generateFailedAttemptCounterName(msgId)).get();
									if (failedAttempts < 3L) {
										redissonClient.getAtomicLong(this.generateFailedAttemptCounterName(msgId))
												.incrementAndGet();
										redissonClient
												.getQueue(this.generateQueueName(this.getUniqueQueueName(mapName)))
												.add(element);
										redissonClient.getMap(mapName).remove(msgId);
									} else {
										RStream<String, QueueElement> dlq = redissonClient
												.getStream(this.generateFailedLoggingStreamName());
										this.checkAndEmptyFailureStream(dlq);
										dlq.add(element.getMessageId(), element);
										redissonClient.getMap(mapName).remove(msgId);
										redissonClient.getAtomicLong(this.generateFailedAttemptCounterName(msgId))
												.unlink();
									}
								}
							});

						});
				if(successLoggingFlag) {
					this.checkAndEmptySuccessLog();
					}
				lock.unlock();
			}
			return Flux.empty();
		}).onErrorResume(error -> {
			this.moveFailedMessagesBackToQueue();
			return Flux.empty();
		}).subscribe();
	}
	
	private void checkAndEmptySuccessLog() {
		RStream<String,String> successStream = redissonClient.getStream(generateSuccessLoggingStreamName());
		if(getMemorySizeInMB(successStream.sizeInMemory())>100) {
			successStream.unlink();
		}
	}

	private void checkAndEmptyFailureStream(RStream<String, QueueElement> dlq) {
		if(getMemorySizeInMB(dlq.sizeInMemory())>100) {
			dlq.unlink();
		}
		
	}

	private long getMemorySizeInMB(long sizeInMemory) {
		return sizeInMemory/(1024*1024);
	}

}
