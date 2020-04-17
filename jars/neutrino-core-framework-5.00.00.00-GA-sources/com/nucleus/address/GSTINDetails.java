package com.nucleus.address;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicInsert 
@DynamicUpdate

@Synonym(grant="ALL")
@Table(name="CAS_GSTIN_DTL")
public class GSTINDetails  extends BaseEntity {

    private static final long  serialVersionUID = 1L;
    private String  gstIn;
    private Boolean isDefaultGstIn;
    private String gcdId;
    public String getGstIn() {
        return gstIn;
    }
    public void setGstIn(String gstIn) {
        this.gstIn = gstIn;
    }
    public Boolean getIsDefaultGstIn() {
        return isDefaultGstIn;
    }
    public void setIsDefaultGstIn(Boolean isDefaultGstIn) {
        this.isDefaultGstIn = isDefaultGstIn;
    }
    
    
    public String getGcdId() {
        return gcdId;
    }
    public void setGcdId(String gcdId) {
        this.gcdId = gcdId;
    }
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        GSTINDetails gSTINDetails = (GSTINDetails)baseEntity;
        super.populate(gSTINDetails, cloneOptions);
        gSTINDetails.setGstIn(gstIn);
        gSTINDetails.setIsDefaultGstIn(isDefaultGstIn);
    }
}
