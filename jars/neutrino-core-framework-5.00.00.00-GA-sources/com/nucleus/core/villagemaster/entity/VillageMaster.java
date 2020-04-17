package com.nucleus.core.villagemaster.entity;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.address.District;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.tehsil.entity.Tehsil;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="CAS_VILLAGE_MST",indexes={@Index(name="RAIM_PERF_45_4105",columnList="REASON_ACT_INACT_MAP")})
@Synonym(grant="ALL")
@DeletionPreValidator
public class VillageMaster extends BaseMasterEntity  {


    private String code;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    private District district;
    @ManyToOne(fetch = FetchType.LAZY)
    private Tehsil tehsil;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    private Boolean  negativeVillage;

    @ManyToOne(fetch = FetchType.LAZY)
    private VillageRiskCategory villageRiskCategory;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }
    public Tehsil getTehsil() {
        return tehsil;
    }

    public void setTehsil(Tehsil tehsil) {
        this.tehsil = tehsil;
    }

    public Boolean getNegativeVillage() {
        return negativeVillage;
    }

    public void setNegativeVillage(Boolean negativeVillage) {
        this.negativeVillage = negativeVillage;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    public VillageRiskCategory getVillageRiskCategory() {
        return villageRiskCategory;
    }

    public void setVillageRiskCategory(VillageRiskCategory villageRiskCategory) {
        this.villageRiskCategory = villageRiskCategory;
    }

    @Override
    public String toString() {
        return "VillageMaster{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", district=" + district +
                ", tehsil=" + tehsil +
                ", negativeVillage=" + negativeVillage +
                ", villageRiskCategory=" + villageRiskCategory +
                '}';
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        VillageMaster village= (VillageMaster) baseEntity;
        super.populate(village, cloneOptions);
        village.setCode(this.code);
        village.setName(this.name);
        village.setDistrict(this.district);
        village.setTehsil(this.tehsil);
        village.setNegativeVillage(this.negativeVillage);
        village.setVillageRiskCategory(this.villageRiskCategory);
        if (reasonActInactMap != null) {
            village.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }

    }
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        VillageMaster village= (VillageMaster) baseEntity;
        super.populateFrom(village, cloneOptions);
        this.setCode(village.getCode());
        this.setName(village.getName());
        this.setDistrict(village.getDistrict());
        this.setTehsil(village.getTehsil());
        this.setNegativeVillage(village.getNegativeVillage());
        this.setVillageRiskCategory(village.getVillageRiskCategory());
        if (village.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) village.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    @Override
    public String getDisplayName() {
        return name;
    }
}


