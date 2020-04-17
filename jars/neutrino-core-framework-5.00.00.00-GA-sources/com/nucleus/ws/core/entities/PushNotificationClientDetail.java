package com.nucleus.ws.core.entities;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")


@NamedQueries({
	  @NamedQuery(name="findNotificationClientByNotificationClientId",
           
  			query="SELECT dtl FROM PushNotificationClientDetail dtl where dtl.notificationClientId=:notificationClientId and dtl.activeFlag=true"),
	  @NamedQuery(name="findNotificationClientByListOfNotificationClientId",
      
		query="SELECT dtl FROM PushNotificationClientDetail dtl where dtl.activeFlag=true and dtl.notificationClientId IN :notificationClientIds"),
	  
	  @NamedQuery(name="findNotificationClientByListOfUserIds",
      
		query="SELECT dtl FROM PushNotificationClientDetail dtl where dtl.activeFlag=true and dtl.userId IN :userIds"),

		@NamedQuery(name="findNotificationClientByListOfUserIdsAndTrustedSourceModules",

				query="SELECT dtl FROM PushNotificationClientDetail dtl where dtl.activeFlag=true and dtl.userId IN :userIds and dtl.trustedSourceId IN :trustedSourceIds")
}) 
public class PushNotificationClientDetail extends BaseMasterEntity{
	
	private static final long serialVersionUID = 1L;
	private Long userId;
	private String  username;
	
	private String notificationClientId;
	
	
	private String imeiNumber;
	private String deviceType;
	private String trustedSourceId;

    private String operatingSystem;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime inactivationDate;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getNotificationClientId() {
		return notificationClientId;
	}

	public void setNotificationClientId(String notificationClientId) {
		this.notificationClientId = notificationClientId;
	}

	public String getImeiNumber() {
		return imeiNumber;
	}

	public void setImeiNumber(String imeiNumber) {
		this.imeiNumber = imeiNumber;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getTrustedSourceId() {
		return trustedSourceId;
	}

	public void setTrustedSourceId(String trustedSourceId) {
		this.trustedSourceId = trustedSourceId;
	}

	

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	
	public DateTime getInactivationDate() {
		return inactivationDate;
	}

	public void setInactivationDate(DateTime inactivationDate) {
		this.inactivationDate = inactivationDate;
	}

	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}



	

}
