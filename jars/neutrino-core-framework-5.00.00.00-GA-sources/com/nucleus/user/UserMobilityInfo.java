/**
 * 
 */
package com.nucleus.user;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * to persist mobile enabled options for user
 *
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy=InheritanceType.JOINED)
@Synonym(grant="ALL")
@Cacheable
public class UserMobilityInfo extends BaseEntity {


	private static final long serialVersionUID = -3178030984903418308L;

	@Column
	@EmbedInAuditAsValue
	private Boolean isMobileEnabled;
	
	@Column
	@EmbedInAuditAsValue
	private Boolean isChallengeEnabled;
	
	@Column(scale = 8)
	@EmbedInAuditAsValue
	private Integer challenge;

	@Column
	@EmbedInAuditAsValue
	private Boolean isDeviceAuthEnabled;
	
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL/*, orphanRemoval = true*/)
	@JoinColumn(name = "user_mobility_info_fk")
	@EmbedInAuditAsValueObject
	private List<UserDeviceMapping> registeredDeviceList ;

	@Transient
	private String auditTrailIdentifier;

	public Boolean getIsMobileEnabled() {
		return isMobileEnabled;
	}



	public void setIsMobileEnabled(Boolean isMobileEnabled) {
		this.isMobileEnabled = isMobileEnabled;
	}



	public Boolean getIsChallengeEnabled() {
		return isChallengeEnabled;
	}



	public void setIsChallengeEnabled(Boolean isChallengeEnabled) {
		this.isChallengeEnabled = isChallengeEnabled;
	}



	public Integer getChallenge() {
		return challenge;
	}



	public void setChallenge(Integer challenge) {
		this.challenge = challenge;
	}


	@PrePersist
	@PreUpdate
	public void createDefaults() {
		if (isMobileEnabled == null) {
			isMobileEnabled = false;

		}
		if (isDeviceAuthEnabled == null) {
			isDeviceAuthEnabled = false;

		}

		if (isChallengeEnabled == null) {
			isChallengeEnabled = false;
		}
	}
	
	public UserMobilityInfo(){
		
	}
	public UserMobilityInfo(UserMobilityInfo userMobilityInfo){
		isMobileEnabled=userMobilityInfo.getIsMobileEnabled();
		isChallengeEnabled=userMobilityInfo.getIsChallengeEnabled();
		challenge=userMobilityInfo.getChallenge();
		isDeviceAuthEnabled = userMobilityInfo.getIsDeviceAuthEnabled();
		List<UserDeviceMapping> userMobInfo = userMobilityInfo.getRegisteredDeviceList();

		if(userMobInfo.size()>0){
			registeredDeviceList = new ArrayList<>();
			for(UserDeviceMapping userDevMap : userMobInfo){
				registeredDeviceList.add(userDevMap);
			}
		}
	}



	

	public List<UserDeviceMapping> getRegisteredDeviceList() {
		if(registeredDeviceList==null || registeredDeviceList.isEmpty())
			registeredDeviceList = new ArrayList<>();
		return registeredDeviceList;
	}



	public void setRegisteredDeviceList(List<UserDeviceMapping> registeredDeviceList) {
		this.registeredDeviceList = registeredDeviceList;
	}



	public Boolean getIsDeviceAuthEnabled() {
		if(isDeviceAuthEnabled==null)
			isDeviceAuthEnabled=false;
		
		return isDeviceAuthEnabled;
	}



	public void setIsDeviceAuthEnabled(Boolean isDeviceAuthEnabled) {
		this.isDeviceAuthEnabled = isDeviceAuthEnabled;
	}

	public void setAuditTrailIdentifier(String auditTrailIdentifier) {
		this.auditTrailIdentifier = auditTrailIdentifier;
	}

	public String getAuditTrailIdentifier() {
		return auditTrailIdentifier;
	}
}
