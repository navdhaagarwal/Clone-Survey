package com.nucleus.tehsil.entity;

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


@Entity
@DynamicUpdate
@DynamicInsert
@DeletionPreValidator
@Table(name="CAS_Tehsil_mst",indexes={@Index(name="RAIM_PERF_45_4116",columnList="REASON_ACT_INACT_MAP")})
@Synonym(grant="ALL")
public class Tehsil  extends BaseMasterEntity  {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String code;


    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private District district;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;



    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

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

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    public String toString() {
        return "Tehsil [code=" + code + ", name=" + name + ", district="
                + district +  "]";
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Tehsil tehsil= (Tehsil) baseEntity;
        super.populate(tehsil, cloneOptions);
        tehsil.setCode(this.code);
        tehsil.setName(this.name);
        tehsil.setDistrict(this.district);
        if (reasonActInactMap != null) {
            tehsil.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }

    }
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Tehsil tehsil= (Tehsil) baseEntity;
        super.populateFrom(tehsil, cloneOptions);
        this.setCode(tehsil.getCode());
        this.setName(tehsil.getName());
        this.setDistrict(tehsil.getDistrict());
        if (tehsil.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) tehsil.getReasonActInactMap().cloneYourself(cloneOptions));
        }

    }

    @Override
    public String getDisplayName() {
        return name;
    }


}
