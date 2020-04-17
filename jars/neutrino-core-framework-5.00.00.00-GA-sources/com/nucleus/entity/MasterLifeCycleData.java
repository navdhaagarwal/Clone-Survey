/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Embeddable
public class MasterLifeCycleData implements Serializable {

    private static final long serialVersionUID = 1466632368490599285L;

    private String            reviewedByUri;
    private Integer           approvalStatus   = ApprovalStatus.APPROVED;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          reviewedTimeStamp;

    @Column(name = "IP4_ADDRESS")
    private String ip4Address;

    public String getIp4Address() {
        return ip4Address;
    }

    public void setIp4Address(String ip4Address) {
        this.ip4Address = ip4Address;
    }

    public EntityId getReviewedByEntityId() {
        return EntityId.fromUri(reviewedByUri);
    }

    public void setReviewedByEntityId(EntityId reviewedByEntityId) {
        this.reviewedByUri = reviewedByEntityId.getUri();
    }

    /**
     * @return the reviewedByUri
     */
    public String getReviewedByUri() {
        return reviewedByUri;
    }

    /**
     * @return the approvalStatus
     */
    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    /**
     * @param reviewedByUri
     *            the reviewedByUri to set
     */
    public void setReviewedByUri(String reviewedByUri) {
        this.reviewedByUri = reviewedByUri;
    }

    /**
     * @param approvalStatus
     *            the approvalStatus to set
     */
    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    /**
     * @return the reviewedTimeStamp
     */
    public DateTime getReviewedTimeStamp() {
        return reviewedTimeStamp;
    }

    /**
     * @param reviewedTimeStamp
     *            the reviewedTimeStamp to set
     */
    public void setReviewedTimeStamp(DateTime reviewedTimeStamp) {
        this.reviewedTimeStamp = reviewedTimeStamp;
    }

}
