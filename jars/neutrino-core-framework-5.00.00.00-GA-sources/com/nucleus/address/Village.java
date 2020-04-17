package com.nucleus.address;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="ALL")
public class Village extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    /**
     * Attribute to hold the code assigned to the Village 
     */
    private String            villageCode;

    /**
     * Attribute to hold the name of the Village 
     */
    @Sortable
    private String            villageName;

    /**
     * Attribute to hold the code of the district in which the village is located
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private District          district;

    /**
     * @return the villageCode
     */
    public String getVillageCode() {
        return villageCode;
    }

    /**
     * @param villageCode the villageCode to set
     */
    public void setVillageCode(String villageCode) {
        this.villageCode = villageCode;
    }

    /**
     * @return the villageName
     */
    public String getVillageName() {
        return villageName;
    }

    /**
     * @param villageName the villageName to set
     */
    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    /**
     * @return the district
     */
    public District getDistrict() {
        return district;
    }

    /**
     * @param district the district to set
     */
    public void setDistrict(District district) {
        this.district = district;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Village village = (Village) baseEntity;
        super.populate(village, cloneOptions);
        village.setVillageName(villageName);
        village.setVillageCode(villageCode);
        village.setDistrict(district);
    }
    // @Override
    // public String toString() {
    // return "Village [villageCode=" + villageCode + ", villageName=" + villageName + ", district=" + district + "]";
    // }
}