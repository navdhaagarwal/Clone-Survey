package com.nucleus.activeInactiveReason;

import com.nucleus.core.annotations.Synonym;
import io.swagger.annotations.ApiModelProperty;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;

import javax.persistence.*;


@Entity
@Synonym(grant="ALL")
@Table(name="MST_ACTIN_REAS")
public class MasterActiveInactiveReasons extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(displayKey="label.Reason.Active")
    private ReasonActive reasonActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference(displayKey="label.Reason.InActive")
    private ReasonInActive reasonInactive;

    @EmbedInAuditAsValue(displayKey="label.Description")
    private String description;

    @Transient
    @ApiModelProperty(hidden=true)
    private String activeDescription;

    @Transient
    @ApiModelProperty(hidden=true)
    private String inacitveDescription;

    public ReasonActive getReasonActive() {
        return reasonActive;
    }

    public void setReasonActive(ReasonActive reasonActive) {
        this.reasonActive = reasonActive;
    }

    public ReasonInActive getReasonInactive() {
        return reasonInactive;
    }

    public void setReasonInactive(ReasonInActive reasonInactive) {
        this.reasonInactive = reasonInactive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActiveDescription() {
        return activeDescription;
    }

    public void setActiveDescription(String activeDescription) {
        this.activeDescription = activeDescription;
    }

    public String getInacitveDescription() {
        return inacitveDescription;
    }

    public void setInacitveDescription(String inacitveDescription) {
        this.inacitveDescription = inacitveDescription;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        MasterActiveInactiveReasons masterActiveInactiveReasons= (MasterActiveInactiveReasons) baseEntity;
        super.populate(masterActiveInactiveReasons, cloneOptions);
        masterActiveInactiveReasons.setReasonActive(this.reasonActive);
        masterActiveInactiveReasons.setReasonInactive(this.reasonInactive);
        masterActiveInactiveReasons.setDescription(this.description);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        MasterActiveInactiveReasons masterActiveInactiveReasons= (MasterActiveInactiveReasons) baseEntity;
        super.populateFrom(masterActiveInactiveReasons, cloneOptions);
        this.setReasonActive(masterActiveInactiveReasons.getReasonActive());
        this.setReasonInactive(masterActiveInactiveReasons.getReasonInactive());
        this.setDescription(masterActiveInactiveReasons.getDescription());
        }
}
