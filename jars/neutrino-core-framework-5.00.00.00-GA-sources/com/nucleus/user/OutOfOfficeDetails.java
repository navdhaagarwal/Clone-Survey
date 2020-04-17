package com.nucleus.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Embeddable
public class OutOfOfficeDetails implements Serializable {

    private static final long  serialVersionUID = 8361659406199474785L;
    public static final String USER             = "user";
    public static final String TEAM_LEAD        = "team_lead";
    public static final String POOL             = "pool";

    private boolean            outOfOffice      = false;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime           fromDate;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime           toDate;
    private Long               delegatedToUserId;
    private String             assignedTo ;

    public OutOfOfficeDetails(boolean outOfOffice, DateTime fromDate, DateTime toDate, Long delegatedToUserId,
            String assignedTo) {
        super();
        this.outOfOffice = outOfOffice;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.delegatedToUserId = delegatedToUserId;
        this.assignedTo = assignedTo;
    }

    public OutOfOfficeDetails() {
        super();
    }

    public boolean isOutOfOffice() {
        return outOfOffice;
    }

    public void setOutOfOffice(boolean outOfOffice) {
        this.outOfOffice = outOfOffice;
    }

    public DateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(DateTime fromDate) {
        this.fromDate = fromDate;
    }

    public DateTime getToDate() {
        return toDate;
    }

    public void setToDate(DateTime toDate) {
        this.toDate = toDate;
    }

    public Long getDelegatedToUserId() {
        return delegatedToUserId;
    }

    public void setDelegatedToUserId(Long delegatedToUserId) {
        this.delegatedToUserId = delegatedToUserId;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

}
