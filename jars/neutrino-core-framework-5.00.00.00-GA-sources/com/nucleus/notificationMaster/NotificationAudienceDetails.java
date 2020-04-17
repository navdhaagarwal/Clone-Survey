package com.nucleus.notificationMaster;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class NotificationAudienceDetails extends BaseMasterEntity {

	@Transient
	private static final long                serialVersionUID  = 123569L;
	@Transient
	private String[] ids;
	
	
	private String type;
	@Column(length=4000)
	private String audienceDetails;
	
	public String[] getIds() {
		return ids;
	}
	public void setIds(String[] ids) {
		this.ids = ids;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAudienceDetails() {
		return audienceDetails;
	}
	public void setAudienceDetails(String audienceDetails) {
		this.audienceDetails = audienceDetails;
	}
	
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {

        NotificationAudienceDetails notificationAudienceDetails = (NotificationAudienceDetails) baseEntity;
        super.populate(notificationAudienceDetails, cloneOptions);
        
        notificationAudienceDetails.setAudienceDetails(audienceDetails);
        notificationAudienceDetails.setType(type);
        
    }
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {

    	NotificationAudienceDetails notificationAudienceDetails = (NotificationAudienceDetails) baseEntity;
        super.populateFrom(notificationAudienceDetails, cloneOptions);
        this.setAudienceDetails(notificationAudienceDetails.getAudienceDetails());
        this.setType(notificationAudienceDetails.getType());
    }

   
	
}
