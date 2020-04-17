package com.nucleus.infinispan.console.security;

import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Seconds;

/**
 * 
 * @author gajendra.jatav
 *
 */
@Named("devOpsUserManager")
public class DevOpsUserManager {
	
	private DevOpsUser devOpsUser;
	
	private DateTime creationTime;

	public DevOpsUser getDevOpsUser() {
		DateTime currentTime=DateTime.now();
		if(Seconds.secondsBetween(creationTime, currentTime).isGreaterThan(Hours.ONE.toStandardSeconds())){
			devOpsUser=null;
		}
		return devOpsUser;
	}

	public void setDevOpsUser(DevOpsUser devOpsUser) {
		this.devOpsUser = devOpsUser;
		this.creationTime=DateTime.now();
	}

	public DateTime getCreationTime() {
		return creationTime;
	}
	
}
