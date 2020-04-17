package com.nucleus.user.ipaddress;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

import javax.persistence.*;

import com.nucleus.user.AccessType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="IP_ADDRESS_MST",indexes={@Index(name="RAIM_PERF_45_4346",columnList="REASON_ACT_INACT_MAP")})
@Synonym(grant="SELECT")
public class IpAddress extends BaseMasterEntity {

	private String ipAddress;

	@ManyToOne(fetch= FetchType.LAZY)
	private AccessType accessType;

	@OneToOne(cascade = CascadeType.ALL)
	private ReasonsActiveInactiveMapping reasonActInactMap;

	public String getIpAddress(){
		return ipAddress;
	}
	
	public void setIpAddress(String ipAddress){
		this.ipAddress = ipAddress;
	}

	public AccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(AccessType accessType) {
		this.accessType = accessType;
	}

	public ReasonsActiveInactiveMapping getReasonActInactMap() {
		return reasonActInactMap;
	}

	public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
		this.reasonActInactMap = reasonActInactMap;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		IpAddress ipaddress = (IpAddress) baseEntity;
		super.populate(ipaddress, cloneOptions);
		
		ipaddress.setIpAddress(ipAddress);
		ipaddress.setAccessType(accessType);
		if (reasonActInactMap != null) {
			ipaddress.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
		}
	
	}
	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		IpAddress ipaddress = (IpAddress) baseEntity;
		super.populateFrom(ipaddress, cloneOptions);
		
		this.setIpAddress(ipaddress.getIpAddress());
		this.setAccessType(ipaddress.getAccessType());
		if (ipaddress.getReasonActInactMap() != null) {
			this.setReasonActInactMap((ReasonsActiveInactiveMapping) ipaddress.getReasonActInactMap().cloneYourself(cloneOptions));
		}

	}

	@Override
	public String getDisplayName() {
		return ipAddress;
	}


}

