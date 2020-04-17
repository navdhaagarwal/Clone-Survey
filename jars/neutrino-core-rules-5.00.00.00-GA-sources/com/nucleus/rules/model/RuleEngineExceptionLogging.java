package com.nucleus.rules.model;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@DynamicInsert
@DynamicUpdate
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "RULE_ENG_ERR_LOG")
@Synonym(grant = "ALL")
public class RuleEngineExceptionLogging extends BaseEntity {

    private Long typeId;
    private String typeCode;
    @Lob
    private String exception;

    private String InvokerUri;

    private String invokeTime;

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getInvokerUri() {
        return InvokerUri;
    }

    public void setInvokerUri(String invokerUri) {
        InvokerUri = invokerUri;
    }

    public String getInvokeTime() {
        return invokeTime;
    }

    public void setInvokeTime(String invokeTime) {
        this.invokeTime = invokeTime;
    }
}
