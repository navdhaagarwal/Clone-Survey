package com.nucleus.makerchecker;

import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.nucleus.entity.BaseEntity;
import com.nucleus.user.User;

// @Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class FieldChange extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    private User              makerReference;

    private String            fieldName;
    private String            oldValue;
    private String            newValue;

    public FieldChange(String fieldName, String oldValue, String newValue) {
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setMakerReference(User changeInitiator) {
        this.makerReference = changeInitiator;
    }

    public User getMakerReference() {
        return makerReference;
    }

}