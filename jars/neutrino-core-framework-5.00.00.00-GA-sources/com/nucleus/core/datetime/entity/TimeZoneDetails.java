package com.nucleus.core.datetime.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;


/**
 * 
 * Entity for time zone details
 * 
 * @since GA 2.5
 * @author prateek.chachra
 *
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class TimeZoneDetails extends BaseEntity{ // NOSONAR

	private static final long serialVersionUID = 3334137488473418529L;
	
	private String databaseZone;
		
	
	
	public String getDatabaseZone() {
		return databaseZone;
	}
	public void setDatabaseZone(String databaseZone) {
		this.databaseZone = databaseZone;
	}
}
