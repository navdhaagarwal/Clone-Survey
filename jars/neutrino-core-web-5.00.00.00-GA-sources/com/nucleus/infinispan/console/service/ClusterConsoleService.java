package com.nucleus.infinispan.console.service;

import java.util.List;

import org.infinispan.manager.DefaultCacheManager;

import com.nucleus.infinispan.console.entity.ClusterNodeAddresses;
import com.nucleus.infinispan.console.entity.ClusterStatus;
import com.nucleus.infinispan.console.event.ClusterEventVo;

/**
 * 
 * @author gajendra.jatav
 *
 */
public interface ClusterConsoleService {
	

	public void persistNodeAddressList(List<String> nodeAddress);

	ClusterNodeAddresses fetchNodeAddressesEntity();

	public ClusterStatus fetchRemoteNodeClusterStatus(String nodeAddress);

	String getCommunicationToken(boolean generateToken);

	public boolean validToken(String communicationToken);

	public void evictHibernateCache();

	public void evictHibernateCache(String nodeAddress);

	public void markCacheActive(DefaultCacheManager cacheManager);

	public void markCacheActive(DefaultCacheManager cacheManager,String nodeAddress);

	public ClusterStatus getClusterStatus(DefaultCacheManager cacheManager);

	public void addNodeAddress(String nodeAddress);

	public void deleteNodeAddress(String nodeAddress);

	public List<ClusterEventVo> fetchRemoteNodeClusterEventsList(String nodeAddress);

	public List<ClusterEventVo> getClusterEventsList();

}
