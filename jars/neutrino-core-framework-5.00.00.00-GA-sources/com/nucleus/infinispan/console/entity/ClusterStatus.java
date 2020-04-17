package com.nucleus.infinispan.console.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class ClusterStatus implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String clusterName;
	
	private List<NodeAddress> members;
	
	private String coordinatorAddress;
	
	private String nodeAddress;
	
	private String physicalAddresses;
	
	private String localAddress;
		
	private List<String> availableCacheNames;
	
	private List<String> degradedCacheNames;
	
	public List<String> getAvailableCacheNames() {
		return availableCacheNames;
	}

	public void setAvailableCacheNames(List<String> availableCacheNames) {
		this.availableCacheNames = availableCacheNames;
	}

	public List<String> getDegradedCacheNames() {
		return degradedCacheNames;
	}

	public void setDegradedCacheNames(List<String> degradedCacheNames) {
		this.degradedCacheNames = degradedCacheNames;
	}

	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}

	public String getPhysicalAddresses() {
		return physicalAddresses;
	}

	public void setPhysicalAddresses(String physicalAddresses) {
		this.physicalAddresses = physicalAddresses;
	}

	public String getNodeAddress() {
		return nodeAddress;
	}

	public void setNodeAddress(String nodeAddress) {
		this.nodeAddress = nodeAddress;
	}

	public String getCoordinatorAddress() {
		return coordinatorAddress;
	}

	public void setCoordinatorAddress(String coordinatorAddress) {
		this.coordinatorAddress = coordinatorAddress;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public List<NodeAddress> getMembers() {
		return members;
	}

	public void setMembers(List<NodeAddress> members) {
		this.members = members;
	}

	

}
