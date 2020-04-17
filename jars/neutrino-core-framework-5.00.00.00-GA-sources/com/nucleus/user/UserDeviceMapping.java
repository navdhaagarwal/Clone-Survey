package com.nucleus.user;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(indexes={@Index(name="user_device_mapping_fk_index",columnList="user_mobility_info_fk")})
@Cacheable
public class UserDeviceMapping extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6736442016748769023L;

	@EmbedInAuditAsValue
	private String deviceId;
	
	@ManyToOne
	@EmbedInAuditAsReference
	private DeviceIdentifierType deviceType;

	@EmbedInAuditAsValue
	private String deviceStatus;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}


	public String getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(String deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	public DeviceIdentifierType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceIdentifierType deviceType) {
		this.deviceType = deviceType;
	}
}
