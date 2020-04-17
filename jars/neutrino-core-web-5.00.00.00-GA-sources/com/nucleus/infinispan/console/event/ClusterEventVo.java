package com.nucleus.infinispan.console.event;

import java.io.Serializable;

import org.infinispan.notifications.cachemanagerlistener.event.MergeEvent;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.joda.time.DateTime;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class ClusterEventVo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DateTime time;
	
	private String eventName;
	
	private String newMembers;
	
	private String oldMembers;
	
	private String subgroupsMerged;
	
	
	public ClusterEventVo(ViewChangedEvent viewChangedEvent){
		this.eventName="ViewChangedEvent";
		initFields(viewChangedEvent);
	}

	public ClusterEventVo(MergeEvent mergeEvent){
		this.eventName="MergeEvent";
		initFields(mergeEvent);
		if(mergeEvent.getSubgroupsMerged()!=null){
			subgroupsMerged=mergeEvent.getSubgroupsMerged().toString();
		}
	}

	
	private void initFields(ViewChangedEvent viewChangedEvent) {
		time=DateTime.now();
		if(viewChangedEvent.getNewMembers()!=null){
			this.newMembers=viewChangedEvent.getNewMembers().toString();
		}
		if(viewChangedEvent.getOldMembers()!=null){
			this.oldMembers=viewChangedEvent.getOldMembers().toString();
		}
	}

	public DateTime getTime() {
		return time;
	}

	public void setTime(DateTime time) {
		this.time = time;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getNewMembers() {
		return newMembers;
	}

	public void setNewMembers(String newMembers) {
		this.newMembers = newMembers;
	}

	public String getOldMembers() {
		return oldMembers;
	}

	public void setOldMembers(String oldMembers) {
		this.oldMembers = oldMembers;
	}

	public String getSubgroupsMerged() {
		return subgroupsMerged;
	}

	public void setSubgroupsMerged(String subgroupsMerged) {
		this.subgroupsMerged = subgroupsMerged;
	}

}
