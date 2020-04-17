package com.nucleus.activeInactiveReason;

import com.nucleus.core.annotations.Synonym;
import io.swagger.annotations.ApiModelProperty;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Synonym(grant="ALL")
@Table(name="REAS_MAP_ACTIN")
public class ReasonsActiveInactiveMapping extends BaseEntity {

    @EmbedInAuditAsValue(displayKey="label.status.activeFlag")
    private String typeOfAction;

    @Transient
    @ApiModelProperty(hidden=true)
    private String[] reasonDeletedId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "REAS_ACT_INA_FK")
    @EmbedInAuditAsValueObject
    private List<MasterActiveInactiveReasons> masterActiveInactiveReasons;

    public List<MasterActiveInactiveReasons> getMasterActiveInactiveReasons() {
        return masterActiveInactiveReasons;
    }

    public void setMasterActiveInactiveReasons(List<MasterActiveInactiveReasons> masterActiveInactiveReasons) {
        this.masterActiveInactiveReasons = masterActiveInactiveReasons;
    }


    public String getTypeOfAction() {
        return typeOfAction;
    }

    public void setTypeOfAction(String typeOfAction) {
        this.typeOfAction = typeOfAction;
    }

    public String[] getReasonDeletedId() {
        return reasonDeletedId;
    }

    public void setReasonDeletedId(String[] reasonDeletedId) {
        this.reasonDeletedId = reasonDeletedId;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping= (ReasonsActiveInactiveMapping) baseEntity;
        super.populate(reasonsActiveInactiveMapping, cloneOptions);
        reasonsActiveInactiveMapping.setTypeOfAction(this.typeOfAction);
        if (null != masterActiveInactiveReasons && !masterActiveInactiveReasons.isEmpty()) {
            List<MasterActiveInactiveReasons> masterActiveInactiveReasonsList = new ArrayList<MasterActiveInactiveReasons>();
            for (MasterActiveInactiveReasons mstreason : this.masterActiveInactiveReasons) {
                if (mstreason != null) {
                    masterActiveInactiveReasonsList.add((MasterActiveInactiveReasons) mstreason.cloneYourself(cloneOptions));
                }
            }
            reasonsActiveInactiveMapping.setMasterActiveInactiveReasons(masterActiveInactiveReasonsList);
        }
    }


    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ReasonsActiveInactiveMapping reasonsActiveInactiveMapping= (ReasonsActiveInactiveMapping) baseEntity;
        super.populateFrom(reasonsActiveInactiveMapping, cloneOptions);
        this.setTypeOfAction(reasonsActiveInactiveMapping.getTypeOfAction());
        if (null != reasonsActiveInactiveMapping.getMasterActiveInactiveReasons()) {
            this.getMasterActiveInactiveReasons().clear();
            for (MasterActiveInactiveReasons mst : reasonsActiveInactiveMapping.getMasterActiveInactiveReasons()) {
                if (mst != null) {
                    this.getMasterActiveInactiveReasons().add((MasterActiveInactiveReasons) mst.cloneYourself(cloneOptions));
                }
            }
        }

    }
}
