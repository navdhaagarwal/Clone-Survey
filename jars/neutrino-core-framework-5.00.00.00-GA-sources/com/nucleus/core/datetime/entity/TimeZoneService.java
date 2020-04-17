package com.nucleus.core.datetime.entity;
/**
 * 
 * Interface for TimeZoneServiceImpl
 * 
 * To persist the database time zone
 * 
 * @since GA 2.5
 * @author prateek.chachra
 *
 */
public interface TimeZoneService {

	
	public void persist(TimeZoneDetails timeZoneDetails);
	
	public  TimeZoneDetails getExistingTimeZone();

}
