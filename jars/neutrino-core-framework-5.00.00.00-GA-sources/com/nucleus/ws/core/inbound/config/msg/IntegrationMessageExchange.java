/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.ws.core.inbound.config.msg;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class IntegrationMessageExchange extends BaseEntity {

    private static final long serialVersionUID = -2338764717294187621L;

    private String            serviceId;
    // 1=in-bound,2=out-bound
    private Integer           serviceType;
    private String            transactionId;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime          requestTimestamp;

    private String            associatedSystemUserId;
    private String            deliveryDescription;

    private Boolean           success;
    private String            errorDetail;

    // 1=soap xml message,2=JMS,3=JSON
    private Integer           messageType;
    private String            requestMessageStoreId;
    private String            responseMessageStoreId;
    private String            faultMessageStoreId;

    @Transient
    private byte[]            requestMessage;
    @Transient
    private byte[]            responseMessage;
    @Transient
    private byte[]            faultMessage;

    private String            sourceIp;
    private String            targetIp;

    // applicable to out bound message only
    private Boolean           outResendable;
    private Integer           outRedeliveries;
    private String            loggedInUsername;
    private String            associatedEntityUri;
    private String            associatedSubEntityUri;

    // for random message search
    private String            serachTag1;
    private String            serachTag2;

    public String getSerachTag1() {
        return serachTag1;
    }

    public void setSerachTag1(String serachTag1) {
        this.serachTag1 = serachTag1;
    }

    public String getSerachTag2() {
        return serachTag2;
    }

    public void setSerachTag2(String serachTag2) {
        this.serachTag2 = serachTag2;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public DateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(DateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getAssociatedEntityUri() {
        return associatedEntityUri;
    }

    public void setAssociatedEntityUri(String associatedEntityUri) {
        this.associatedEntityUri = associatedEntityUri;
    }

    public String getAssociatedSubEntityUri() {
        return associatedSubEntityUri;
    }

    public void setAssociatedSubEntityUri(String associatedSubEntityUri) {
        this.associatedSubEntityUri = associatedSubEntityUri;
    }

    public String getDeliveryDescription() {
        return deliveryDescription;
    }

    public void setDeliveryDescription(String deliveryDescription) {
        this.deliveryDescription = deliveryDescription;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public String getRequestMessageStoreId() {
        return requestMessageStoreId;
    }

    public void setRequestMessageStoreId(String requestMessageStoreId) {
        this.requestMessageStoreId = requestMessageStoreId;
    }

    public String getResponseMessageStoreId() {
        return responseMessageStoreId;
    }

    public void setResponseMessageStoreId(String responseMessageStoreId) {
        this.responseMessageStoreId = responseMessageStoreId;
    }

    public String getFaultMessageStoreId() {
        return faultMessageStoreId;
    }

    public void setFaultMessageStoreId(String faultMessageStoreId) {
        this.faultMessageStoreId = faultMessageStoreId;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public Boolean getOutResendable() {
        return outResendable;
    }

    public void setOutResendable(Boolean outResendable) {
        this.outResendable = outResendable;
    }

    public Integer getOutRedeliveries() {
        return outRedeliveries;
    }

    public void setOutRedeliveries(Integer outRedeliveries) {
        this.outRedeliveries = outRedeliveries;
    }

    public String getAssociatedSystemUserId() {
        return associatedSystemUserId;
    }

    public void setAssociatedSystemUserId(String associatedSystemUserId) {
        this.associatedSystemUserId = associatedSystemUserId;
    }

    public byte[] getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(byte[] requestMessage) {
        this.requestMessage = requestMessage;
    }

    public byte[] getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(byte[] responseMessage) {
        this.responseMessage = responseMessage;
    }

    public byte[] getFaultMessage() {
        return faultMessage;
    }

    public void setFaultMessage(byte[] faultMessage) {
        this.faultMessage = faultMessage;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

}
