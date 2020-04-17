package com.nucleus.user;

import com.nucleus.address.Area;
import com.nucleus.address.Village;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.entity.BaseEntity;

import javax.persistence.*;

import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;


@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(name = "USER_VILLAGE_MAPPING")
public class UserVillageMapping extends BaseEntity{

    @ManyToOne
    @EmbedInAuditAsReference
    private VillageMaster villageMaster;

    @ManyToMany
    @JoinTable(name = "USER_VILLAGE_AREA_MAPPING", joinColumns= {@JoinColumn(name="USER_VILLAGE_MAPPING", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name="AREA", referencedColumnName = "ID")})
    @EmbedInAuditAsReference(columnToDisplay = "areaCode")
    private List<Area> villageAreaList;

    @Transient
    private String auditTrailIdentifier;
    
    public VillageMaster getVillageMaster() {
        return villageMaster;
    }

    public void setVillageMaster(VillageMaster villageMaster) {
        this.villageMaster = villageMaster;
    }

    public List<Area> getVillageAreaList() {
        return villageAreaList;
    }

    public void setVillageAreaList(List<Area> villageAreaList) {
        this.villageAreaList = villageAreaList;
    }

    public String getAuditTrailIdentifier() {
        return auditTrailIdentifier;
    }

    public void setAuditTrailIdentifier(String auditTrailIdentifier) {
        this.auditTrailIdentifier = auditTrailIdentifier;
    }
}
