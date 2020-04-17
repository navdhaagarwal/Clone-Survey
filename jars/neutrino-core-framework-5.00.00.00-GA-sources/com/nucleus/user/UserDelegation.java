package com.nucleus.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class UserDelegation extends BaseEntity {

    private static final long serialVersionUID = -7069790432323197706L;

    @ManyToOne(fetch = FetchType.LAZY)
    private User              delegationFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    private User              delegatedTo;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          delegationStartDate;

    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          delegationEndDate;

    private String            reason;

    private String            additionalComment;

    public User getDelegationFrom() {
        return delegationFrom;
    }

    public void setDelegationFrom(User delegationFrom) {
        this.delegationFrom = delegationFrom;
    }

    public User getDelegatedTo() {
        return delegatedTo;
    }

    public void setDelegatedTo(User delegatedTo) {
        this.delegatedTo = delegatedTo;
    }

    public DateTime getDelegationStartDate() {
        return delegationStartDate;
    }

    public void setDelegationStartDate(DateTime delegationStartDate) {
        this.delegationStartDate = delegationStartDate;
    }

    public DateTime getDelegationEndDate() {
        return delegationEndDate;
    }

    public void setDelegationEndDate(DateTime delegationEndDate) {
        this.delegationEndDate = delegationEndDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdditionalComment() {
        return additionalComment;
    }

    public void setAdditionalComment(String additionalComment) {
        this.additionalComment = additionalComment;
    }

}