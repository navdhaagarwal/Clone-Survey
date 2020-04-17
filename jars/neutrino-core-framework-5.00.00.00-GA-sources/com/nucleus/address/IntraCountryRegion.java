package com.nucleus.address;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
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
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@DeletionPreValidator
@Synonym(grant="SELECT")
@Table(indexes={
    @Index(name="RAIM_PERF_45_4202",columnList="REASON_ACT_INACT_MAP"),
    @Index(name="intraRegionName_index",columnList="intraRegionName"),
	@Index(name="intraRegionCode_index",columnList="intraRegionCode")
	})
public class IntraCountryRegion extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    /**
     * Attribute to hold the code assigned to the Region
     */
    private String            intraRegionCode;

    /**
     * Attribute to hold the name of the Region
     */
    @Sortable
    private String            intraRegionName;

    @ManyToOne
    private Country           country;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    public String getIntraRegionCode() {
        return intraRegionCode;
    }

    public void setIntraRegionCode(String intraRegionCode) {
        this.intraRegionCode = intraRegionCode;
    }

    public String getIntraRegionName() {
        return intraRegionName;
    }

    public void setIntraRegionName(String intraRegionName) {
        this.intraRegionName = intraRegionName;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        IntraCountryRegion intraCountryRegion = (IntraCountryRegion) baseEntity;
        super.populate(intraCountryRegion, cloneOptions);
        intraCountryRegion.setCountry(country);
        intraCountryRegion.setIntraRegionCode(intraRegionCode);
        intraCountryRegion.setIntraRegionName(intraRegionName);
        if (reasonActInactMap != null) {
            intraCountryRegion.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        IntraCountryRegion intraCountryRegion = (IntraCountryRegion) baseEntity;
        super.populateFrom(intraCountryRegion, cloneOptions);
        this.setIntraRegionCode(intraCountryRegion.getIntraRegionCode());
        this.setIntraRegionName(intraCountryRegion.getIntraRegionName());
        this.setCountry(intraCountryRegion.getCountry());
        if (intraCountryRegion.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) intraCountryRegion.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }
    @Override
    public String getDisplayName() {
        return getIntraRegionName();
    }
}
