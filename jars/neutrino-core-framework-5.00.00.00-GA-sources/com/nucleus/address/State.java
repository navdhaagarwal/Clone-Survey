package com.nucleus.address;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;

import org.apache.commons.collections4.CollectionUtils;
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
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
@DeletionPreValidator
@Table(indexes={@Index(name="RAIM_PERF_45_4177",columnList="REASON_ACT_INACT_MAP"),
        @Index(name="stateCode_index",columnList="stateCode"),@Index(name="stateName_index",columnList="stateName")})
public class State extends BaseMasterEntity {

    @Transient
    private static final long  serialVersionUID = 1L;

    /**
     * Attribute to hold the State code 
     */
        private String             stateCode;

    /**
     * Attribute to hold the name of the State
     */
    @Sortable
    private String             stateName;

    /**
     * Attribute to hold the code of the country in which the state is located. 
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Country            country;

    /**
     * Attribute to hold the code of the Region in which the state is located. 
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private IntraCountryRegion region;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    private Integer minimumLength;

    private Integer maximumLength;

    private Boolean pincodeType  = Boolean.FALSE;

    private String validationType;

    private String pincodeStart;
    
    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL)  
    @JoinColumn(name = "state_fk")
    private List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings;

    private Boolean unionTerritory  = Boolean.FALSE;

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    private String pincodeEnd;

    private String pincodeRange;

    public Integer getMinimumLength() {
        return minimumLength;
    }

    public void setMinimumLength(Integer minimumLength) {
        this.minimumLength = minimumLength;
    }

    public Integer getMaximumLength() {
        return maximumLength;
    }

    public void setMaximumLength(Integer maximumLength) {
        this.maximumLength = maximumLength;
    }

    public Boolean getPincodeType() {
        return pincodeType;
    }

    public void setPincodeType(Boolean pincodeType) {
        this.pincodeType = pincodeType;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public String getPincodeStart() {
        return pincodeStart;
    }

    public void setPincodeStart(String pincodeStart) {
        this.pincodeStart = pincodeStart;
    }

    public String getPincodeEnd() {
        return pincodeEnd;
    }

    public void setPincodeEnd(String pincodeEnd) {
        this.pincodeEnd = pincodeEnd;
    }

    public String getPincodeRange() {
        return pincodeRange;
    }

    public void setPincodeRange(String pincodeRange) {
        this.pincodeRange = pincodeRange;
    }

    /**
     * @return the stateCode
     */
    public String getStateCode() {
        return stateCode;
    }

    /**
     * @param stateCode the stateCode to set
     */
    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    /**
     * @return the stateName
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * @param stateName the stateName to set
     */
    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    /**
     * @return the country
     */
    public Country getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(Country country) {
        this.country = country;
    }

    public IntraCountryRegion getRegion() {
        return region;
    }

    public void setRegion(IntraCountryRegion region) {
        this.region = region;
    }


    /**
     * @return the vehicleStateRegistraionMappings
     */
    public List<VehicleStateRegistraionMapping> getVehicleStateRegistraionMappings() {
        return vehicleStateRegistraionMappings;
    }

    /**
     * @param vehicleStateRegistraionMappings the vehicleStateRegistraionMappings to set
     */
    public void setVehicleStateRegistraionMappings(List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings) {
        this.vehicleStateRegistraionMappings = vehicleStateRegistraionMappings;
    }

    public Boolean getUnionTerritory() {
        if(null ==unionTerritory){
            return Boolean.FALSE;
        }
        return unionTerritory;
    }

    public void setUnionTerritory(Boolean unionTerritory) {
        this.unionTerritory = unionTerritory;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        State state = (State) baseEntity;
        super.populate(state, cloneOptions);
        state.setStateName(stateName);
        state.setRegion(region);
        state.setStateCode(stateCode);
        state.setCountry(country);
        
        state.setMinimumLength(minimumLength);
        state.setMaximumLength(maximumLength);
        state.setPincodeType(pincodeType);
        state.setValidationType(validationType);
        state.setPincodeStart(pincodeStart);
        state.setPincodeEnd(pincodeEnd);
        state.setPincodeRange(pincodeRange);
        if (reasonActInactMap != null) {
            state.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
        List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappingList = null;
        if (CollectionUtils.isNotEmpty(this.vehicleStateRegistraionMappings)) {
            vehicleStateRegistraionMappingList = new ArrayList<VehicleStateRegistraionMapping>();
            for (VehicleStateRegistraionMapping vehicleStateRegistraionMapping : this.vehicleStateRegistraionMappings) {
                vehicleStateRegistraionMappingList.add((VehicleStateRegistraionMapping) vehicleStateRegistraionMapping.cloneYourself(cloneOptions));
            }
        }
        state.setVehicleStateRegistraionMappings(vehicleStateRegistraionMappingList);
        state.setUnionTerritory(unionTerritory);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        State state = (State) baseEntity;
        super.populateFrom(state, cloneOptions);
        this.setStateName(state.getStateName());
        this.setStateCode(state.getStateCode());
        this.setRegion(state.getRegion());
        this.setCountry(state.getCountry());
        
        this.setMinimumLength(state.getMinimumLength());
        this.setMaximumLength(state.getMaximumLength());
        this.setPincodeType(state.getPincodeType());
        this.setValidationType(state.getValidationType());
        this.setPincodeStart(state.getPincodeStart());
        this.setPincodeEnd(state.getPincodeEnd());
        this.setPincodeRange(state.getPincodeRange());
        if (state.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) state.getReasonActInactMap().cloneYourself(cloneOptions));
        }
        List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappingList = null;
        if (CollectionUtils.isNotEmpty(state.getVehicleStateRegistraionMappings())) {
            vehicleStateRegistraionMappingList = new ArrayList<VehicleStateRegistraionMapping>();
            for (VehicleStateRegistraionMapping vehicleStateRegistraionMapping : state.getVehicleStateRegistraionMappings()) {
                vehicleStateRegistraionMappingList.add((VehicleStateRegistraionMapping) vehicleStateRegistraionMapping.cloneYourself(cloneOptions));
            }
        }
        this.setVehicleStateRegistraionMappings(vehicleStateRegistraionMappingList);
        this.setUnionTerritory(state.getUnionTerritory());
    }

    @Override
    public String getDisplayName() {
        return getStateName();
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("State Master Object received to be saved ------------>");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("State Name :" + stateName);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("State Code :" + stateCode);
        stf.append(SystemPropertyUtils.getNewline());

        if (country != null) {
            stf.append("Country  :" + country.getId());

        }
        log = stf.toString();
        return log;
    }
}