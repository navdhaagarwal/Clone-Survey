package com.nucleus.infinispan.console.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.partitionhandling.AvailabilityMode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.infinispan.console.entity.ClusterCommunicationToken;
import com.nucleus.infinispan.console.entity.ClusterNodeAddresses;
import com.nucleus.infinispan.console.entity.ClusterStatus;
import com.nucleus.infinispan.console.entity.NodeAddress;
import com.nucleus.infinispan.console.event.ClusterEventHistoryLoggerListner;
import com.nucleus.infinispan.console.event.ClusterEventVo;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;

/**
 * 
 * @author gajendra.jatav
 *
 */
@Named("clusterConsoleService")
public class ClusterConsoleServiceImpl implements ClusterConsoleService {

	public static final String MARK_CACHE_ACTIVE_API = "cluster/clusterConsole/markCacheActiveRemotely";

	public static final String EVICT_HIBERNATE_CACHE_API = "cluster/clusterConsole/evictHibernateCacheRemotely";

	private static final String CLUSTER_STATUS_API = "cluster/clusterConsole/getClusterStatusRemotely";

	private static final String CLUSTER_EVENT_LIST_API = "cluster/clusterConsole/getClusterEventsListRemotely";

	
	
	public static final String COMM_TOKEN = "communicationToken";

	
	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;

	@Inject
	@Named("neutrinoRestTemplate")
	private RestTemplate restTemplate;

	@Inject
	@Named("clusterConsoleHelper")
	private ClusterConsoleHelper clusterConsoleHelper;

	@Override
	@Transactional
	public void persistNodeAddressList(List<String> nodeAddress) {
		ClusterNodeAddresses clusterNodeAddresses = fetchNodeAddressesEntity();
		clusterNodeAddresses.setNodeAddresses(nodeAddress);
		if (clusterNodeAddresses.getId() == null) {
			entityDao.persist(clusterNodeAddresses);
		} else {
			entityDao.update(clusterNodeAddresses);
		}
	}

	@Override
	@Transactional
	public ClusterNodeAddresses fetchNodeAddressesEntity() {
		List<ClusterNodeAddresses> addresses = entityDao.findAll(ClusterNodeAddresses.class);
		if (!ValidatorUtils.hasElements(addresses)) {
			return new ClusterNodeAddresses();
		}
		return addresses.get(0);
	}

	@Override
	public ClusterStatus fetchRemoteNodeClusterStatus(String nodeAddress) {
		try {
			return executeOnRemote(nodeAddress,CLUSTER_STATUS_API,ClusterStatus.class);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("Error white fetching ", e);
			return null;
		}
	}

	private String appendCommToken() {
		return "?" + COMM_TOKEN + "=" + getCommunicationToken(true);
	}

	@Override
	@Transactional
	public String getCommunicationToken(boolean generateToken) {
		if(generateToken){
			return genarateOrUpdateToken();
		}
		List<ClusterCommunicationToken> clusterCommunicationTokens = entityDao.findAll(ClusterCommunicationToken.class);
		if (ValidatorUtils.hasElements(clusterCommunicationTokens) && clusterCommunicationTokens.get(0).getIsValidToken()) {
			return clusterConsoleHelper.markInvalidOnRead(clusterCommunicationTokens.get(0).getId());
		}else{
			BaseLoggers.flowLogger.error("Either token not found or was inactive ");
			return null;
		}
	}

	private String genarateOrUpdateToken() {
		List<ClusterCommunicationToken> clusterCommunicationTokens = entityDao.findAll(ClusterCommunicationToken.class);
		if (!ValidatorUtils.hasElements(clusterCommunicationTokens) ) {
			return clusterConsoleHelper.createToken();
		}else{
			return clusterConsoleHelper.updateToken(clusterCommunicationTokens.get(0).getId());
		}
	}

	private String getAddress(String nodeAddress, String clusterStatusApi) {
		if (nodeAddress.charAt(nodeAddress.length() - 1) == '/') {
			return nodeAddress + clusterStatusApi;
		}
		return nodeAddress + "/" + clusterStatusApi;
	}

	@Override
	public boolean validToken(String communicationToken) {
		String existingToken=getCommunicationToken(false);
		if(existingToken==null || communicationToken==null){
			return false;
		}
		return communicationToken.equals(existingToken);
	}

	@Override
	public void evictHibernateCache() {

        Session session = (Session) entityDao.getEntityManager().getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        sessionFactory.getCache().evictAllRegions();
	}

	@Override
	public void evictHibernateCache(String nodeAddress) {
		executeOnRemote(nodeAddress,EVICT_HIBERNATE_CACHE_API,String.class);
	}

	private <T> T executeOnRemote(String nodeAddress, String api,Class<T> type) {
		try {
			ResponseEntity<T> responseEntity = restTemplate
					.getForEntity(getAddress(nodeAddress, api) + appendCommToken(), type);
			return responseEntity.getBody();
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("Error white calling {} ",api, e);
			return null;
		}
	}
	
	private <T> List<T> executeOnRemoteGetList(String nodeAddress, String api,Class<T> typeClass) {
		try {
			ResponseEntity<List<T>> responseEntity = restTemplate
					.exchange(getAddress(nodeAddress, api) + appendCommToken(),HttpMethod.GET,null, new ParameterizedTypeReference<List<T>>() {
					});
			return responseEntity.getBody();
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("Error white calling {} ",api, e);
			return null;
		}
	}
	

	@Override
	public void markCacheActive(DefaultCacheManager cacheManager) {
		updateModeToAvaibale(cacheManager);
	}

	@Override
	public void markCacheActive(DefaultCacheManager cacheManager, String nodeAddress) {
		executeOnRemote(nodeAddress, MARK_CACHE_ACTIVE_API, String.class);
		
	}

	private void updateModeToAvaibale(DefaultCacheManager cacheManager) {
		ClusterStatus clusterStatus = getClusterStatus(cacheManager);
		clusterStatus.getDegradedCacheNames().forEach((degradedCache) -> {
			Cache cache = cacheManager.getCache(degradedCache);
			cache.getAdvancedCache().setAvailability(AvailabilityMode.AVAILABLE);
		});
	}
	
	
	public ClusterStatus getClusterStatus(DefaultCacheManager cacheManager) {
		ClusterStatus clusterStatus = new ClusterStatus();

		clusterStatus.setClusterName(cacheManager.getClusterName());

		List<NodeAddress> members = new ArrayList<>();
		Map<String, NodeAddress> membersMap=new HashMap<>();
		cacheManager.executor().timeout(10L, TimeUnit.SECONDS)
		.submitConsumer(localManager -> 
			 localManager.getTransport().getPhysicalAddresses()
		, (address, value, throwable) -> {
			if (throwable != null) {
				BaseLoggers.flowLogger.error("Could not get physical address on address {} exception {}",address,throwable);
			}
			if(value!=null && !value.isEmpty()){
				membersMap.put(address.toString(), new NodeAddress(address.toString(), value.toString()));
			}
		}).join();
		
		if (ValidatorUtils.hasElements(cacheManager.getMembers())) {
			cacheManager.getMembers().forEach(member -> {
				if(!membersMap.containsKey(member.toString())){
					members.add(new NodeAddress(member.toString(), "Could not resolve"));
				}else{
					members.add(membersMap.get(member.toString()));
				}
			});
		}

		clusterStatus.setMembers(members);


		clusterStatus.setCoordinatorAddress(cacheManager.getCoordinator().toString());

		clusterStatus.setNodeAddress(cacheManager.getNodeAddress());

		clusterStatus.setPhysicalAddresses(cacheManager.getPhysicalAddresses());

		clusterStatus.setLocalAddress(cacheManager.getAddress().toString());

		populateCacheInfo(cacheManager.getCacheNames(), cacheManager, clusterStatus);

		
		return clusterStatus;

	}
	
	
	private void populateCacheInfo(Set<String> cacheNames, DefaultCacheManager cacheManager,
			ClusterStatus clusterStatus) {
		List<String> availableCacheList = new ArrayList<>();
		List<String> degradedCacheList = new ArrayList<>();

		if (cacheNames != null && !cacheNames.isEmpty()) {
			cacheNames.forEach((cacheName) -> {
				Cache cache = cacheManager.getCache(cacheName);
				if (cache.getAdvancedCache().getAvailability() == AvailabilityMode.AVAILABLE) {
					availableCacheList.add(cacheName);
				} else {
					degradedCacheList.add(cacheName);
				}

			});
		}

		clusterStatus.setAvailableCacheNames(availableCacheList);
		clusterStatus.setDegradedCacheNames(degradedCacheList);

	}

	@Override
	@Transactional
	public void addNodeAddress(String nodeAddress) {
		ClusterNodeAddresses clusterNodeAddresses=fetchNodeAddressesEntity();
		if(clusterNodeAddresses.getNodeAddresses()==null){
			clusterNodeAddresses.setNodeAddresses(new ArrayList<>());
		}
		for(String existingAddress:clusterNodeAddresses.getNodeAddresses()){
			if(existingAddress.equals(nodeAddress)){
				return;
			}
		}
		clusterNodeAddresses.getNodeAddresses().add(nodeAddress);
		if(clusterNodeAddresses.getId()==null){
			entityDao.persist(clusterNodeAddresses);
		}else{
			entityDao.update(clusterNodeAddresses);
		}
		
	}

	@Override
	@Transactional
	public void deleteNodeAddress(String nodeAddress) {

		ClusterNodeAddresses clusterNodeAddresses=fetchNodeAddressesEntity();
		if(clusterNodeAddresses==null || clusterNodeAddresses.getNodeAddresses()==null){
			return;
		}
		if(clusterNodeAddresses.getNodeAddresses().contains(nodeAddress)){
			clusterNodeAddresses.getNodeAddresses().remove(nodeAddress);
		}
		if(clusterNodeAddresses.getId()==null){
			entityDao.persist(clusterNodeAddresses);
		}else{
			entityDao.update(clusterNodeAddresses);
		}
	}

	@Override
	public List<ClusterEventVo> fetchRemoteNodeClusterEventsList(String nodeAddress) {

		try {
			return executeOnRemoteGetList(nodeAddress,CLUSTER_EVENT_LIST_API,ClusterEventVo.class);
		} catch (Exception e) {
			BaseLoggers.flowLogger.error("Error white fetching ", e);
			return null;
		}
		
	}

	@Override
	public List<ClusterEventVo> getClusterEventsList() {
		return ClusterEventHistoryLoggerListner.getClusterEventsList();
	}


}
