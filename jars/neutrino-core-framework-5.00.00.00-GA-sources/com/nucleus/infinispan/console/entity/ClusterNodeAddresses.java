package com.nucleus.infinispan.console.entity;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OrderColumn;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * 
 * @author gajendra.jatav
 *
 */
@Entity
@Synonym(grant="ALL")
public class ClusterNodeAddresses extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ElementCollection
	@OrderColumn
	private List<String> nodeAddresses;

	public List<String> getNodeAddresses() {
		return nodeAddresses;
	}

	public void setNodeAddresses(List<String> nodeAddresses) {
		this.nodeAddresses = nodeAddresses;
	}
	
	
}
