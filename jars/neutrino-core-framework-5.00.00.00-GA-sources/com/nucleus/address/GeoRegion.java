package com.nucleus.address;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@DeletionPreValidator
@Synonym(grant="SELECT")
@Table(indexes={@Index(name="RAIM_PERF_45_4245",columnList="REASON_ACT_INACT_MAP")})
public class GeoRegion extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    /**
     * Attribute to hold the code assigned to the Region 
     */
    private String            regionCode;

    /**
     * Attribute to hold the  name of the Region 
     */
    @Sortable
    private String            regionName;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    /**
     * @return the regionCode
     */
    public String getRegionCode() {
        return regionCode;
    }

    /**
     * @param regionCode the regionCode to set
     */
    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    /**
     * @return the regionName
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * @param regionName the regionName to set
     */
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        GeoRegion region = (GeoRegion) baseEntity;
        super.populate(region, cloneOptions);
        region.setRegionName(regionName);
        region.setRegionCode(regionCode);
        if (reasonActInactMap != null) {
            region.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        GeoRegion region = (GeoRegion) baseEntity;
        super.populateFrom(region, cloneOptions);
        this.setRegionName(region.getRegionName());
        this.setRegionCode(region.getRegionCode());
        if (region.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) region.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    @Override
    public String toString() {
        return "Region [regionCode=" + regionCode + ", regionName=" + regionName + "]";
    }

    @Override
    public String getDisplayName() {
        return getRegionName();
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("Region Master Object received to be saved ------------>");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Region Name :" + regionName);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Region Code :" + regionCode);
        log = stf.toString();
        return log;
    }
}
