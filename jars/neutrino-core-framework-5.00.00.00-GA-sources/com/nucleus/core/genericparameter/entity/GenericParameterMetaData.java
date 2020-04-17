package com.nucleus.core.genericparameter.entity;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Cacheable
@Table(indexes={@Index(name="dType_index",columnList="dType")})
public class GenericParameterMetaData extends BaseEntity {

    private static final long     serialVersionUID = 474379179789030804L;

    private String dType;

    private String purpose;

    private Integer dTypeActionFlag;

    private String sourceType;

    public Integer getdTypeActionFlag() {
        return dTypeActionFlag;
    }

    public void setdTypeActionFlag(Integer dTypeActionFlag) {
        this.dTypeActionFlag = dTypeActionFlag;
    }

    public String getdType() {
        return dType;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
