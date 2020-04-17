package com.nucleus.address;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Cacheable
@Entity
@Synonym(grant="ALL")
@DynamicUpdate
@DynamicInsert
public class VehicleStateRegistraionMapping extends BaseEntity {

    @Transient
    private static final long  serialVersionUID = 15644L;
    
    private String stateRTOCode;

    /**
     * @return the stateRTOCode
     */
    public String getStateRTOCode() {
        return stateRTOCode;
    }

    /**
     * @param stateRTOCode the stateRTOCode to set
     */
    public void setStateRTOCode(String stateRTOCode) {
        this.stateRTOCode = stateRTOCode;
    }
    
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        VehicleStateRegistraionMapping vehicleStateRegistraionMapping = (VehicleStateRegistraionMapping) baseEntity;
        super.populate(vehicleStateRegistraionMapping, cloneOptions);
        vehicleStateRegistraionMapping.setStateRTOCode(stateRTOCode);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        VehicleStateRegistraionMapping vehicleStateRegistraionMapping = (VehicleStateRegistraionMapping) baseEntity;
        super.populateFrom(vehicleStateRegistraionMapping, cloneOptions);
        this.setStateRTOCode(vehicleStateRegistraionMapping.getStateRTOCode());

    }
}
