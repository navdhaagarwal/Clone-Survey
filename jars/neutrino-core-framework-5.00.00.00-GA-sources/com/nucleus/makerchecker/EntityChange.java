package com.nucleus.makerchecker;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.nucleus.entity.BaseEntity;
import com.nucleus.user.User;

// @Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class EntityChange extends BaseEntity {

    @Transient
    private static final long serialVersionUID = -1790223186104541484L;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EntityChange> childEntityChanges;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FieldChange>  fieldChanges;

    private String            clazzName;

    private Long              referredEntityId;

    private int               changeType;

    private User              makerReference;

    public Set<EntityChange> getChildEntityChanges() {
        return childEntityChanges;
    }

    public void addChildEntityChange(EntityChange childEntityChange) {
        if (this.childEntityChanges == null) {
            this.childEntityChanges = new LinkedHashSet<EntityChange>();
        }
        childEntityChanges.add(childEntityChange);
    }

    public String getClazzName() {
        return clazzName;
    }

    public Long getReferredEntityId() {
        return referredEntityId;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public void setReferredEntityId(Long referredEntityId) {
        this.referredEntityId = referredEntityId;
    }

    public int getChangeType() {
        return changeType;
    }

    public Set<FieldChange> getFieldChanges() {
        return fieldChanges;
    }

    public void addFieldChange(FieldChange fieldChange) {
        if (this.fieldChanges == null) {
            this.fieldChanges = new LinkedHashSet<FieldChange>();
        }
        this.fieldChanges.add(fieldChange);
    }

    public User getMakerReference() {
        return makerReference;
    }

    public void setMakerReference(User makerReference) {
        this.makerReference = makerReference;
    }

}