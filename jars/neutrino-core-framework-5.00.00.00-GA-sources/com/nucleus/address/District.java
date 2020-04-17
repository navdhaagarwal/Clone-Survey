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
@Synonym(grant="SELECT,REFERENCES")
@Table(indexes={
    @Index(name="RAIM_PERF_45_4203",columnList="REASON_ACT_INACT_MAP"),
	@Index(name="districtName_index",columnList="districtName"),
	@Index(name="districtCode_index",columnList="districtCode")
	})
public class District extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    /**
     * Attribute to hold the code of the District
     */
    private String            districtCode;

    /**
     * Attribute to hold the description of the District
     */
    @Sortable
    private String            districtName;

    /**
     * Attribute to hold the abbreviation of the District
     */
    private String            districtAbbreviation;

    @ManyToOne(fetch = FetchType.LAZY)
    private State             state;


    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;
    /**
     * @return the districtCode
     */
    public String getDistrictCode() {
        return districtCode;
    }

    /**
     * @param districtCode the districtCode to set
     */
    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    /**
     * @return the districtDescription
     */
    public String getDistrictName() {
        return districtName;
    }

    /**
     * @param districtDescription the districtDescription to set
     */
    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    /**
     * @return the districtAbbreviation
     */
    public String getDistrictAbbreviation() {
        return districtAbbreviation;
    }

    /**
     * @param districtAbbreviation the districtAbbreviation to set
     */
    public void setDistrictAbbreviation(String districtAbbreviation) {
        this.districtAbbreviation = districtAbbreviation;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(State state) {
        this.state = state;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        District district = (District) baseEntity;
        super.populate(district, cloneOptions);
        district.setDistrictName(districtName);
        district.setDistrictCode(districtCode);
        district.setDistrictAbbreviation(districtAbbreviation);
        district.setState(state);
        if (reasonActInactMap != null) {
            district.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        District district = (District) baseEntity;
        super.populateFrom(district, cloneOptions);
        this.setDistrictName(district.getDistrictName());
        this.setDistrictCode(district.getDistrictCode());
        this.setDistrictAbbreviation(district.getDistrictAbbreviation());
        this.setState(district.getState());
        if (district.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) district.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    @Override
    public String getDisplayName() {
        return getDistrictName();
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("District Master Object received to be saved ------------> ");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("District Code : " + districtCode);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("District Name : " + districtName);
        stf.append(SystemPropertyUtils.getNewline());

        if (state != null) {
            stf.append("State : " + state.getId());

        }
        log = stf.toString();
        return log;
    }
}
