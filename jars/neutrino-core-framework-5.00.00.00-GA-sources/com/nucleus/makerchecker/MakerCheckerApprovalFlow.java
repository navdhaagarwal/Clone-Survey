package com.nucleus.makerchecker;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.approval.ApprovalFlow;
import com.nucleus.entity.EntityId;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class MakerCheckerApprovalFlow extends ApprovalFlow {

    @Transient
    private static final long         serialVersionUID = -8657526404702243156L;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_flow_fk")
    @OrderBy("timestamp")
    private Set<UnapprovedEntityData> changeTrail;

    private String                    changedEntityUri;

    public Set<UnapprovedEntityData> getChangeTrail() {
        return changeTrail;
    }

    public void setChangeTrail(Set<UnapprovedEntityData> changeTrail) {
        this.changeTrail = changeTrail;
    }

    public String getChangedEntityUri() {
        return changedEntityUri;
    }

    public void setChangedEntityUri(EntityId changedEntityId) {
        this.changedEntityUri = changedEntityId.getUri();
    }

    public boolean addChangeTrail(UnapprovedEntityData unapprovedEntityData) {
        if (null == changeTrail) {
            changeTrail = new LinkedHashSet<UnapprovedEntityData>();
        }
        return changeTrail.add(unapprovedEntityData);
    }

}