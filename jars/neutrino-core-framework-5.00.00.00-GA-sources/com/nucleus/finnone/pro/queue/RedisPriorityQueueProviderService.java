package com.nucleus.finnone.pro.queue;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
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

public class RedisPriorityQueueProviderService implements QueueProviderService {

	public static final String QUEUED = "QUEUED";
	public static final String PROCESSING = "PROCESSSING";
	public static final String PROCESSED = "PROCESSED";
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILED = "FAILED";
	public static final String PRIORITY_QUEUE_TOPIC = "PriorityQueueTopic";
	public static final String PRIORITY_QUEUE_SUCCESS_LOGGING_STREAM = "PriorityQueueSuccessLoggingStream";
	public static final String PRIORITY_QUEUE_FAILED_LOGGING_STREAM = "PriorityQueueFailedLoggingStream";
	public static final String PRIORITY_QUEUE = "PriorityQueue";
	public static final String FAILED_ATTEMPT_COUNTER = "FailedAttemptCounter";
	public static final String PRIORITY_QUEUE_STATE_MAP = "PriorirtyQueueStateMap";
	public static final String PRIORITY_QUEUE_SCHEDULER_LOCK = "PriorityQueueSchedulerLock";
	public static final String DASH_DELIMITER="-";

	/**
	 * Flag for enabling success logging of all messages.
	 */
	@Value("${priority.queue.success.logging:false}")
	private boolean successLoggingFlag;

	private RedissonClient redissonClient;

	public RedisPriorityQueueProviderService(RedissonClient client) {
		this.redissonClient = client;
		this.moveFailedMessagesBackToQueue();
	}

	/**
	 * Peek element from queue without popping it
	 * 
	 * @param queueName
	 * @return
	 */
	public PriorityQueueElement peek(String queueName) {
		RScoredSortedSet<PriorityQueueElement> queue = redissonClient
				.getScoredSortedSet(this.generatePriorityQueueName(queueName));
		return queue.last();
	}

	public boolean isEmpty(String queueName) {
		RScoredSortedSet<PriorityQueueElement> queue = redissonClient
				.getScoredSortedSet(this.generatePriorityQueueName(queueName));
		return queue.isEmpty();
	}

	public int size(String queueName) {
		RScoredSortedSet<PriorityQueueElement> queue = redissonClient
				.getScoredSortedSet(this.generatePriorityQueueName(queueName));
		return queue.size();
	}

	/**
	 * Polls message with highest priority from the Priority queue. Special
	 * mechanism using pub-sub to guarantee message delivery
	 * 
	 * @param queueName
	 * @return
	 */
	public PriorityQueueElement poll(String queueName) {
		
		RScoredSortedSet<PriorityQueueElement> queue = redissonClient
				.getScoredSortedSet(generatePriorityQueueName(queueName));
		PriorityQueueElement value = queue.last();
		if (value == null) {
			return value;
		}
		RLock lock = this.redissonClient
				.getLock(generatePriorityQueueName(queueName) + FWCacheConstants.KEY_DELIMITER + value.getMessageId());
		boolean isAcquired = lock.tryLock();

		if (!isAcquired) {
			return this.poll(queueName);
		}

		if (!value.equals(queue.last())) {
			lock.unlock();
			return this.poll(queueName);
		}

		if (!value.isMessageGuarantee()) {
			return this.pollUnguaranteed(queue,lock,queueName);
		}

		return this.pollGuaranteed(queue,lock,queueName,value);
	
	}

	private PriorityQueueElement pollGuaranteed(RScoredSortedSet<PriorityQueueElement> queue, RLock lock,
			String queueName, PriorityQueueElement element) {
		this.addMessageToProcessingMap(element, queueName);
		this.redissonClient.getTopic(generateTopicName(element.getMessageId())).addListener(new StatusListener() {
			public void onUnsubscribe(String channel) {
				// do nothing
			}

			public void onSubscribe(String channel) {
				// do nothing
			}
		});
		element = queue.pollLast();
		lock.unlock();
		return element;
	
	}

	
	private PriorityQueueElement pollUnguaranteed(RScoredSortedSet<PriorityQueueElement> queue, RLock lock,
			String queueName) {
		PriorityQueueElement element = queue.pollLast();
		if (successLoggingFlag) {
			redissonClient.getStream(generateSuccessLoggingStreamName()).add(element.getMessageId(), SUCCESS);
			}
		lock.unlock();
		return element;
	}
	
	private void addMessageToProcessingMap(PriorityQueueElement element, String queueName) {
		RMap<String, QueueElement> map = redissonClient.getMap(generateMapName(queueName));
		map.put(element.getMessageId(), element);
	}


	/**
	 * Adds a new message in the priority queue
	 * 
	 * @param queueName
	 * @param messageObject
	 * @return
	 */
	@Override
	public String offer(String queueName, Object message) {
		PriorityQueueElement messageObject = (PriorityQueueElement) message;
		String msgId = String.valueOf(System.currentTimeMillis()) + DASH_DELIMITER + UUID.randomUUID();
		messageObject.setMessageId(msgId);
		redissonClient.getScoredSortedSet(generatePriorityQueueName(queueName)).add(messageObject.getPriority(),
				messageObject);
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
		RMap<String,PriorityQueueElement> map = redissonClient.getMap(generateMapName(queueName));
		PriorityQueueElement element = map.get(messageId);
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
		return msgId + FWCacheConstants.KEY_DELIMITER + PRIORITY_QUEUE_TOPIC;
	}

	private String generateSuccessLoggingStreamName() {
		return PRIORITY_QUEUE_SUCCESS_LOGGING_STREAM;
	}

	private String generateFailedLoggingStreamName() {
		return PRIORITY_QUEUE_FAILED_LOGGING_STREAM;
	}

	private String generatePriorityQueueName(String queueName) {
		return queueName + FWCacheConstants.KEY_DELIMITER + PRIORITY_QUEUE;
	}

	private String generateFailedAttemptCounterName(String id) {
		return id + FWCacheConstants.KEY_DELIMITER + FAILED_ATTEMPT_COUNTER;
	}

	private String getUniqueQueueName(String redisQueueName) {
		String[] array = redisQueueName.split(FWCacheConstants.REGEX_DELIMITER);
		return array[0];
	}

	private String generateMapName(String queueName) {
		return queueName + FWCacheConstants.KEY_DELIMITER + PRIORITY_QUEUE_STATE_MAP;
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
	
	public PriorityQueueElement fetchFailedMessage(String msgId) {
		RStream<String, PriorityQueueElement> stream =  redissonClient.getStream(generateFailedLoggingStreamName());
		Collection<Map<String, PriorityQueueElement>> list = stream.range(StreamMessageId.MIN, StreamMessageId.MAX)
				.values();
		for(Map<String, PriorityQueueElement> map:list) {
			Entry<String, PriorityQueueElement> entry = map.entrySet().iterator().next();
			if(entry.getKey().equals(msgId)) {
				return entry.getValue();
			}
		}
		return null;
		
	}

	/**
	 * Moves failed messages back to priority queue for processing once it detects
	 * application which was processing the message has crashed. After 3 failed
	 * attempts of processing it is finally moved in a FAILED Redis stream.
	 */
	private void moveFailedMessagesBackToQueue() {
		Flux.interval(Duration.ofMinutes(30)).flatMap(l -> {
			RLock lock = redissonClient.getLock(PRIORITY_QUEUE_SCHEDULER_LOCK);
			if (lock.tryLock()) {
				redissonClient.getKeys()
						.getKeysByPattern("*" + FWCacheConstants.KEY_DELIMITER + PRIORITY_QUEUE_STATE_MAP + "*")
						.forEach(mapName -> {
							RMap<String, PriorityQueueElement> map = redissonClient.getMap(mapName);
							map.entrySet().forEach(entry -> {
								String msgId = entry.getKey();
								PriorityQueueElement element = entry.getValue();
								if (redissonClient.getTopic(this.generateTopicName(msgId.toString()))
										.countListeners() == 0) {
									long failedAttempts = redissonClient
											.getAtomicLong(this.generateFailedAttemptCounterName(msgId)).get();
									if (failedAttempts < 3L) {
										redissonClient.getAtomicLong(this.generateFailedAttemptCounterName(msgId))
												.incrementAndGet();
										redissonClient
												.getScoredSortedSet(this
														.generatePriorityQueueName(this.getUniqueQueueName(mapName)))
												.add(element.getPriority(), element);
										redissonClient.getMap(mapName).remove(msgId);
									} else {
										RStream<PriorityQueueElement, String> dlq = redissonClient
												.getStream(this.generateFailedLoggingStreamName());
										this.checkAndEmptyFailureStream(dlq);
										dlq.add(element, FAILED);
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

	private void checkAndEmptyFailureStream(RStream<PriorityQueueElement, String> dlq) {
		if(getMemorySizeInMB(dlq.sizeInMemory())>100) {
			dlq.unlink();
		}
		
	}

	private long getMemorySizeInMB(long sizeInMemory) {
		
		return sizeInMemory/(1024*1024);
	}

}
