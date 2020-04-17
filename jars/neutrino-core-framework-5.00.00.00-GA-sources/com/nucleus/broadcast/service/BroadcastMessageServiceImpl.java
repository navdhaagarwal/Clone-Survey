package com.nucleus.broadcast.service;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.nucleus.broadcast.entity.BroadcastMessage;
import com.nucleus.broadcast.populator.BroadcastMessageCachePopulator;
import com.nucleus.broadcast.vo.BroadcastMessageVO;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.json.util.JsonUtils;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator.Action;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.hijri.util.DateUtility;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.serversentevents.ServerSentEventsService;

import reactor.core.publisher.Flux;

/**
 * @author shivendra.kumar
 *
 */
@Named("broadcastMessageService")
public class BroadcastMessageServiceImpl implements BroadcastMessageService {

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;
	
	@Inject
	@Named("serverSentEventsService")
	private ServerSentEventsService serverSentEventsService;

	@Inject
	@Named("broadcastMessageCachePopulator")
	private BroadcastMessageCachePopulator broadcastMessageCachePopulator;

	private static final String DUMMY_MESSAGE = "DUMMY_MESSAGE";

	public static final String BROADCAST_MESSAGE_OBJECT = "BROADCAST_MESSAGE_OBJECT";

	private Map<String, Long> lastnotifiedTime = new ConcurrentHashMap<String, Long>();
	
	@PostConstruct
	private void init() {
		this.broadcastMessage();
	}
	

	/**
	 * Fetches messages to display from cache and filters them on the basis of
	 * current date and module.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, BroadcastMessageVO> fetchMessagesToDisplay() {

		TreeMap<Long, Set<String>> endDateMap = (TreeMap<Long, Set<String>>) broadcastMessageCachePopulator
				.get(BroadcastMessageCachePopulator.END_DATE_MAP);
		TreeMap<Long, Set<String>> startDateMap = (TreeMap<Long, Set<String>>) broadcastMessageCachePopulator
				.get(BroadcastMessageCachePopulator.START_DATE_MAP);
		Map<String, BroadcastMessageVO> messageMap = (Map<String, BroadcastMessageVO>) broadcastMessageCachePopulator
				.get(BroadcastMessageCachePopulator.MESSAGE_MAP);
		Set<String> intersect = new TreeSet<String>(
				startDateMap.subMap(Long.MIN_VALUE, true, System.currentTimeMillis() / 1000L, true).values().stream()
						.flatMap(Collection::stream).collect(Collectors.toSet()));
		intersect.retainAll(endDateMap.subMap(System.currentTimeMillis() / 1000L, true, Long.MAX_VALUE, true).values()
				.stream().flatMap(Collection::stream).collect(Collectors.toSet()));

		if (ValidatorUtils.notNull(messageMap) && !messageMap.isEmpty()) {

			Map<String, BroadcastMessageVO> localMap = new HashMap<String, BroadcastMessageVO>();

			for (String messageCode:intersect) {
				
				BroadcastMessageVO messageVo = messageMap.get(messageCode);
					if (messageVo.getModuleId().equals(-1L)
							|| genericParameterService.findById(messageVo.getModuleId(), SourceProduct.class)
									.getCode().equals(ProductInformationLoader.getProductName())) {
						localMap.put(messageCode, messageVo);
					}
				}
			

			return localMap;
		}

		return new HashMap<String, BroadcastMessageVO>();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * Applies final filter on messages to be displayed. Filter messages based on
	 * their start and end time for current date and their frquency
	 * 
	 * @see com.nucleus.broadcast.BroadcastMessageService#publishBroadcastMessage()
	 */
	@Override
	public Map<String, BroadcastMessageVO> publishBroadcastMessage() {

		Date date = new Date();
		String dateStr = DateUtility.formatDateAsString(date, "dd/MM/yyyy");
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");

		Map<String, BroadcastMessageVO> localMap = new HashMap<>();

		for (Map.Entry<String, BroadcastMessageVO> entry : fetchMessagesToDisplay().entrySet()) {

			if (DateTime.now().getMillis() > formatter.parseDateTime(dateStr + " " + entry.getValue().getStartTime())
					.getMillis()) {
				if (lastnotifiedTime.get(entry.getValue().getMessageCode()) == null || (System.currentTimeMillis()
						- lastnotifiedTime.get(entry.getValue().getMessageCode())) >= (entry.getValue().getFrequency()
								* 60 * 1000L)) {
					if (DateTime.now().getMillis() < formatter
							.parseDateTime(dateStr + " " + entry.getValue().getEndTime()).getMillis()) {
						localMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}

		return localMap;

	}

	/* (non-Javadoc)
	 * Updates last display time of messages.
	 * @see com.nucleus.broadcast.BroadcastMessageService#updateLastExecTime(com.nucleus.broadcast.BroadcastMessageVO)
	 */
	public void updateLastExecTime(BroadcastMessageVO msg) {
		lastnotifiedTime.put(msg.getMessageCode(), System.currentTimeMillis());
	}

	/* (non-Javadoc)
	 * Fetches messages to be displayed from the Database.
	 * @see com.nucleus.broadcast.BroadcastMessageService#fetchBroadcastMessageFromDB()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<BroadcastMessage> fetchBroadcastMessageFromDB() {
		Query query = entityDao.getEntityManager().createNamedQuery("getAllBroadcastMessages");
		query.setParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
		query.setParameter("activeFlag", true);
		return query.getResultList();
	}

	/* (non-Javadoc)
	 * Updates updated entity in corresponding cache
	 * @see com.nucleus.broadcast.BroadcastMessageService#updateBroadcastMessageCache(java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateBroadcastMessageCache(Map<String, Object> dataMap) {
		BroadcastMessage broadcastMessage = (BroadcastMessage) dataMap.get(BROADCAST_MESSAGE_OBJECT);
		broadcastMessageCachePopulator.update(
				(Map<String, ImpactedCache>) dataMap.get(FWCacheConstants.IMPACTED_CACHE_MAP), Action.INSERT,
				broadcastMessage);
	}

	/* (non-Javadoc)
	 * Calculates absolute start date. (i.e - Start date + start time)
	 * @see com.nucleus.broadcast.BroadcastMessageService#calculateStartDate(com.nucleus.broadcast.BroadcastMessageVO)
	 */
	@Override
	public long calculateStartDate(BroadcastMessageVO message) {
		String[] time = message.getStartTime().split(":");
		return this.toSeconds(new DateTime(message.getStartDate().getYear(), message.getStartDate().getMonthOfYear(),
				message.getStartDate().getDayOfMonth(), Integer.parseInt(time[0]), Integer.parseInt(time[1])));

	}

	/* (non-Javadoc)
	 * Calculates absolute end date. (i.e - End date + end time)
	 * @see com.nucleus.broadcast.BroadcastMessageService#calculateEndDate(com.nucleus.broadcast.BroadcastMessageVO)
	 */
	@Override
	public long calculateEndDate(BroadcastMessageVO message) {
		String[] time = message.getEndTime().split(":");
		return this.toSeconds(new DateTime(message.getEndDate().getYear(), message.getEndDate().getMonthOfYear(),
				message.getEndDate().getDayOfMonth(), Integer.parseInt(time[0]), Integer.parseInt(time[1])));

	}

	@Override
	public long toSeconds(DateTime dateTime) {
		return dateTime.getMillis() / 1000L;

	}

	@Override
	public String toJson(BroadcastMessageVO message) {
		return JsonUtils.serializeWithoutLazyInitialization(message);
	}


	@Override
	public void removeFluxForLoggedOutUser(String userUuid) {

		serverSentEventsService.getUserFluxMappingMap().remove(userUuid);
	}

	/**
	 * Emitts messages to all connected and logged in clients.
	 * @param msg
	 */
	private void emittMessage(BroadcastMessageVO msg) {
		
		serverSentEventsService.sendEventToAllUsers(msg, "BROADCAST_MESSAGE");
		updateLastExecTime(msg);
	}

	/**
	 * Emitts dummy messages to all connected and logged in clients to remove the connection sinks which are no longer required.
	 * @param msg
	 */
	private void emittDummyMessage(String msg) {
		serverSentEventsService.sendEventToAllUsers(msg, "BROADCAST_MESSAGE");
	}

	/* (non-Javadoc)
	 * Fetches and emitts messages at a fixed interval.
	 * @see com.nucleus.broadcast.BroadcastMessageService#broadcastMessage()
	 */
	@Override
	public void broadcastMessage() {
		Flux.interval(Duration.ofMinutes(1)).flatMap(time -> {
			this.publishBroadcastMessage().values().stream()
					.sorted((o1, o2) -> o1.getPriority().compareTo(o2.getPriority()))
					.forEach(entry -> emittMessage(entry));
			emittDummyMessage(publishDummyMessage());
			return Flux.empty();
		}).onErrorResume(error -> {
			broadcastMessage();
			return Flux.empty();
		}).subscribe();
		

	}
	
	


	private String publishDummyMessage() {

		return DUMMY_MESSAGE;
	}

	@Override
	public BroadcastMessage getMessageById(Long id) {
		if (id != null) {
			EntityId entityId = new EntityId(BroadcastMessage.class, id);
			return entityDao.get(entityId);
		} else {
			return null;
		}
	}

	@Override
	public void deleteBroadcastMessage(BroadcastMessage broadcastMessage) {
		if (broadcastMessage != null) {
			broadcastMessage.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
			entityDao.saveOrUpdate(broadcastMessage);
		}

	}
	
	@Override
	public Boolean isMessageAvailableForModule() {
		Long endDateTimestamp = (Long) broadcastMessageCachePopulator.get(ProductInformationLoader.getProductName());
		if(endDateTimestamp!=null) {
			return true;
		}
		return false;
	}

}
