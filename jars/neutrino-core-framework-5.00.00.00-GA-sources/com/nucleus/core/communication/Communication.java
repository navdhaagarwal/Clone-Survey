/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.communication;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.BaseEntity;
import com.nucleus.notificationMaster.NotificationMaster;

/**
 * The Class CommunicationType.
 *
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant="ALL")
public abstract class Communication extends BaseEntity {

    private static final long        serialVersionUID = -160669145199175114L;

    private String                   addedBy;

    private String                   contactedBy;

    private String                   ownerEntityUri;
    private String contactedTo;
    
    private String partyTypeContacted;
    

    /* Communication Mode. 1=Phone, 2=Email, 3=In Person */
    private Integer                  communicationMode;

    @Transient
    private String                   communicationModeDescription;

    @Transient
    private String                   ownerEntityId;

    @Transient
    private String                   ownerEntityPhotoUrl;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime                 contactTime;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "communication_fk")
    private List<CommunicationTrail> communicationTrails;

    @ManyToOne(fetch = FetchType.LAZY)
    private NotificationMaster       notificationMaster;


    /********   for story CAS-52671 */
    @Column(name="event")
    private String stage;

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    /**
     * Gets the added by.
     *
     * @return the addedBy
     */
    public String getAddedBy() {
        return addedBy;
    }

    /**
     * Sets the added by.
     *
     * @param addedBy the addedBy to set
     */
    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    /**
     * Gets the contacted by.
     *
     * @return the contactedBy
     */
    public String getContactedBy() {
        return contactedBy;
    }

    /**
     * Sets the contacted by.
     *
     * @param contactedBy the contactedBy to set
     */
    public void setContactedBy(String contactedBy) {
        this.contactedBy = contactedBy;
    }

    /**
     * Gets the owner entity uri.
     *
     * @return the ownerEntityUri
     */
    public String getOwnerEntityUri() {
        return ownerEntityUri;
    }

    /**
     * Sets the owner entity uri.
     *
     * @param ownerEntityUri the ownerEntityUri to set
     */
    public void setOwnerEntityUri(String ownerEntityUri) {
        this.ownerEntityUri = ownerEntityUri;
    }

    /**
     * Gets the communication mode.
     *
     * @return the communicationMode
     */
    public Integer getCommunicationMode() {
        return communicationMode;
    }

    /**
     * Sets the communication mode.
     *
     * @param communicationMode the communicationMode to set
     */
    public void setCommunicationMode(Integer communicationMode) {
        this.communicationMode = communicationMode;
    }

    /**
     * Gets the contact time.
     *
     * @return the contactTime
     */
    public DateTime getContactTime() {
        return contactTime;
    }

    /**
     * Sets the contact time.
     *
     * @param contactTime the contactTime to set
     */
    public void setContactTime(DateTime contactTime) {
        this.contactTime = contactTime;
    }

    /**
     * Gets the communication mode description.
     *
     * @return the communicationModeDescription
     */
    public String getCommunicationModeDescription() {
        NeutrinoValidator.notNull(communicationMode, "Can't call this method before setting communicationMode");
        switch (communicationMode) {
            case 1:
                return "Phone";
            case 2:
                return "Email";
            case 3:
                return "In Person";
            default:
                return "";
        }

    }

    /**
     * Sets the communication mode description.
     *
     * @param communicationModeDescription the communicationModeDescription to set
     */

    public void setCommunicationModeDescription(String communicationModeDescription) {
        this.communicationModeDescription = communicationModeDescription;
    }

    /**
     * Gets the communication trails.
     *
     * @return the communicationTrails
     */
    public List<CommunicationTrail> getCommunicationTrails() {
        return communicationTrails;
    }

    /**
     * Adds the communication trail.
     *
     * @param communicationTrail the communication trail
     */
    public void addCommunicationTrail(CommunicationTrail communicationTrail) {
        if (communicationTrails == null) {
            communicationTrails = new ArrayList<CommunicationTrail>();
        }
        communicationTrails.add(communicationTrail);
    }

    public String getOwnerEntityId() {
        return ownerEntityId;
    }

    public void setOwnerEntityId(String ownerEntityId) {
        this.ownerEntityId = ownerEntityId;
    }

    public String getOwnerEntityPhotoUrl() {
        return ownerEntityPhotoUrl;
    }

    public void setOwnerEntityPhotoUrl(String ownerEntityPhotoUrl) {
        this.ownerEntityPhotoUrl = ownerEntityPhotoUrl;
    }

    /**
     * @return the notificationMaster
     */
    public NotificationMaster getNotificationMaster() {
        return notificationMaster;
    }

    /**
     * @param notificationMaster the notificationMaster to set
     */
    public void setNotificationMaster(NotificationMaster notificationMaster) {
        this.notificationMaster = notificationMaster;
    }

    public String getContactedTo() {
        return contactedTo;
    }

    public void setContactedTo(String contactedTo) {
        this.contactedTo = contactedTo;
    }

    public String getPartyTypeContacted() {
        return partyTypeContacted;
    }

    public void setPartyTypeContacted(String partyTypeContacted) {
        this.partyTypeContacted = partyTypeContacted;
    }

    public void setCommunicationTrails(List<CommunicationTrail> communicationTrails) {
        this.communicationTrails = communicationTrails;
    }

}
