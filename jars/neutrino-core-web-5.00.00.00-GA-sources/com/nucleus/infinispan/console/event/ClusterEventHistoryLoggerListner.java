package com.nucleus.infinispan.console.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.MergeEvent;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;

/**
 * 
 * @author gajendra.jatav
 *
 */
@Listener(sync=false)
public class ClusterEventHistoryLoggerListner {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private static List<ClusterEventVo> clusterEventsList=new ArrayList<>();
	
	@ViewChanged
	public void onCacheStarted(ViewChangedEvent viewChangedEvent){
		logger.error("ViewChangedEvent:: New members: "+viewChangedEvent.getNewMembers());
		logger.error("ViewChangedEvent:: Old members: "+viewChangedEvent.getOldMembers());
		clusterEventsList.add(new ClusterEventVo(viewChangedEvent));
	}

	
	@Merged
	public void onMerged(MergeEvent mergedEvent){
		logger.error("MergeEvent:: New members: "+mergedEvent.getNewMembers());
		logger.error("MergeEvent:: Old members: "+mergedEvent.getOldMembers());
		logger.error("MergeEvent:: SubgroupsMerged members: "+mergedEvent.getSubgroupsMerged());
		clusterEventsList.add(new ClusterEventVo(mergedEvent));
	}


	public static List<ClusterEventVo> getClusterEventsList() {
		return clusterEventsList;
	}


}
