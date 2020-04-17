package com.nucleus.infinispan.console.entity;

import java.io.Serializable;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class NodeAddress implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String physicalAddress;
	
	private String logicalAddress;
	
	public NodeAddress(){
		
	}
	
	public NodeAddress(String logicalAddress,String physicalAddress){
		this.logicalAddress=logicalAddress;
		this.physicalAddress=physicalAddress;
	}

	public String getPhysicalAddress() {
		return physicalAddress;
	}

	public void setPhysicalAddress(String physicalAddress) {
		this.physicalAddress = physicalAddress;
	}

	public String getLogicalAddress() {
		return logicalAddress;
	}

	public void setLogicalAddress(String logicalAddress) {
		this.logicalAddress = logicalAddress;
	}
	
	

	
}
