package com.nucleus.broadcast.populator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;

import com.nucleus.broadcast.entity.BroadcastMessage;
import com.nucleus.broadcast.service.BroadcastMessageService;
import com.nucleus.broadcast.vo.BroadcastMessageVO;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

/**
 * @author shivendra.kumar
 *
 */
@Named("broadcastMessageCachePopulator")
public class BroadcastMessageCachePopulator extends FWCachePopulator {

	public static final String MESSAGE_MAP = "MessageMap";
	public static final String START_DATE_MAP = "StartDateMap";
	public static final String END_DATE_MAP = "EndDateMap";


	@Inject
	@Named("broadcastMessageService")
	private BroadcastMessageService broadcastMessageService;
	
	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : BroadcastMessageCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		buildInternal();
		return get(key);
	}

	@Override
	public void build(Long tenantId) {
		buildInternal();
	}

	private void buildInternal() {
		TreeMap<Long, Set<String>> endDateMap = new TreeMap<Long, Set<String>>();
		TreeMap<Long, Set<String>> startDateMap = new TreeMap<Long, Set<String>>();
		Map<String, BroadcastMessageVO> messageMap = new ConcurrentHashMap<String, BroadcastMessageVO>();
		List<BroadcastMessage> fetchBroadcastMessageFromDB = broadcastMessageService.fetchBroadcastMessageFromDB();
		if (fetchBroadcastMessageFromDB != null && CollectionUtils.isNotEmpty(fetchBroadcastMessageFromDB)) {
			fetchBroadcastMessageFromDB.stream()
					.forEach(entry -> saveBroadcastMessage(entry, endDateMap, startDateMap, messageMap));
		}
		putMessageDataInCache(endDateMap, startDateMap, messageMap);
		put(ProductInformationLoader.getProductName(),calculateAbsoluteEndDateForAllMessages());
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.INSERT) && ValidatorUtils.notNull(object)) {
			saveBroadcastMessage((BroadcastMessage) object);
		}

	}

	private void saveBroadcastMessage(BroadcastMessage message, TreeMap<Long, Set<String>> endDateMap,
			TreeMap<Long, Set<String>> startDateMap, Map<String, BroadcastMessageVO> messageMap) {
		BroadcastMessageVO broadcastMessageVO = new BroadcastMessageVO(message);
		messageMap.put(broadcastMessageVO.getMessageCode(), broadcastMessageVO);
		Long startDate = broadcastMessageService.calculateStartDate(broadcastMessageVO);
		Set<String> startDateSet = startDateMap.get(startDate);
		if (startDateSet == null) {
			startDateSet = new HashSet<>();
		}
		startDateSet.add(broadcastMessageVO.getMessageCode());
		startDateMap.put(startDate, startDateSet);

		Long endDate = broadcastMessageService.calculateEndDate(broadcastMessageVO);
		Set<String> endDateSet = endDateMap.get(endDate);
		if (endDateSet == null) {
			endDateSet = new HashSet<>();
		}
		endDateSet.add(broadcastMessageVO.getMessageCode());
		endDateMap.put(endDate, endDateSet);
	}

	private Long calculateAbsoluteEndDateForAllMessages() {
		Map<String, BroadcastMessageVO> messageMap = (Map<String, BroadcastMessageVO>) get(MESSAGE_MAP);
		if(messageMap==null || messageMap.isEmpty())
		{
		return null;
		}
		Long max=-1L;
		for(Map.Entry<String, BroadcastMessageVO> entry : messageMap.entrySet())
		{

			if ((entry.getValue().getModuleId().equals(-1L)
					|| genericParameterService.findById(entry.getValue().getModuleId(), SourceProduct.class).getCode()
							.equals(ProductInformationLoader.getProductName())) && (entry.getValue().getEndDate().getMillis() > max)) {
				max = entry.getValue().getEndDate().getMillis();
			}
			
		}
		
		if(max==-1L)
		{
			max=null;
		}
		return	max;
	}
	

	@SuppressWarnings("unchecked")
	private synchronized void saveBroadcastMessage(BroadcastMessage message) {
		TreeMap<Long, Set<String>> endDateMap = (TreeMap<Long, Set<String>>) get(END_DATE_MAP);
		TreeMap<Long, Set<String>> startDateMap = (TreeMap<Long, Set<String>>) get(START_DATE_MAP);
		Map<String, BroadcastMessageVO> messageMap = (Map<String, BroadcastMessageVO>) get(MESSAGE_MAP);
		deleteOldMessages(endDateMap, startDateMap, messageMap);
		removeOldDataForMsgCode(endDateMap, startDateMap, messageMap, message.getMessageCode());
		saveBroadcastMessage(message, endDateMap, startDateMap, messageMap);
		putMessageDataInCache(endDateMap, startDateMap, messageMap);
		Long endDateTimestamp = calculateAbsoluteEndDateForAllMessages();
		if(get(ProductInformationLoader.getProductName())==null) { 
			put(ProductInformationLoader.getProductName(),endDateTimestamp);
			return;
		}
		else if(((Long)get(ProductInformationLoader.getProductName())<endDateTimestamp))
		{
			put(ProductInformationLoader.getProductName(),endDateTimestamp);
		}
		
	}

	private void deleteOldMessages(TreeMap<Long, Set<String>> endDateMap, TreeMap<Long, Set<String>> startDateMap,
			Map<String, BroadcastMessageVO> messageMap) {

		if (endDateMap != null && startDateMap != null && messageMap != null) {
			Map<Long, Set<String>> expiredMap = new HashMap<>();

			endDateMap.subMap(Long.MIN_VALUE, true, System.currentTimeMillis() / 1000L, false).entrySet().stream()
					.forEach(entry -> expiredMap.put(entry.getKey(), entry.getValue()));

			if (!expiredMap.isEmpty()) {
				expiredMap.entrySet().stream().forEach(entry -> endDateMap.remove(entry.getKey()));
			}

			Set<String> expiredSet = new HashSet<String>(
					expiredMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()));

			Set<Long> startDateExpiredKeysSet = new HashSet<>();

			if (!expiredSet.isEmpty()) {

				expiredSet.stream().forEach(setEntry -> {
					messageMap.remove(setEntry);
					startDateMap.entrySet().stream().forEach(mapEntry -> {
						if (mapEntry.getValue().contains(setEntry)) {
							startDateExpiredKeysSet.add(mapEntry.getKey());
						}
					});
				});

				startDateExpiredKeysSet.stream().forEach(entry -> startDateMap.remove(entry));

			}
		}

	}

	private void removeOldDataForMsgCode(TreeMap<Long, Set<String>> endDateMap, TreeMap<Long, Set<String>> startDateMap,
			Map<String, BroadcastMessageVO> messageMap, String msgCode) {
		if (messageMap != null && messageMap.get(msgCode) != null) {
			BroadcastMessageVO messageVO = messageMap.get(msgCode);
			Long startDate = broadcastMessageService.calculateStartDate(messageVO);
			Long endDate = broadcastMessageService.calculateEndDate(messageVO);
			if (endDate != null) {
				if (endDateMap.get(endDate).size() == 1) {
					endDateMap.remove(endDate);
				} else {
					endDateMap.get(endDate).remove(msgCode);
				}
			}
			if (startDate != null) {
				if (startDateMap.get(startDate).size() == 1) {
					startDateMap.remove(startDate);
				} else {
					startDateMap.get(startDate).remove(msgCode);
				}
			}
		}

	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.BROADCAST_MESSAGE_CACHE;
	}

	private void putMessageDataInCache(TreeMap<Long, Set<String>> endDateMap, TreeMap<Long, Set<String>> startDateMap,
			Map<String, BroadcastMessageVO> messageMap) {
		put(MESSAGE_MAP, messageMap);
		put(START_DATE_MAP, startDateMap);
		put(END_DATE_MAP, endDateMap);
	}

}
