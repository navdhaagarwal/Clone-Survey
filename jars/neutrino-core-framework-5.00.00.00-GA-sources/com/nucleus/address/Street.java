package com.nucleus.address;


import com.nucleus.activeInactiveReason.*;
import com.nucleus.core.annotations.*;
import com.nucleus.entity.*;
import com.nucleus.master.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")

public class Street extends BaseMasterEntity {

	@Column(length = 8)
    private String streetCode;
    private String streetName;
    private String abbreviation;

    @ManyToOne(fetch = FetchType.LAZY)
    private City city;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    public String getStreetCode() {
        return streetCode;
    }

    public void setStreetCode(String streetCode) {
        this.streetCode = streetCode;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Street street = (Street) baseEntity;
        super.populate(street, cloneOptions);
        street.setStreetCode(streetCode);
        street.setStreetName(streetName);
        street.setAbbreviation(abbreviation);
        street.setCity(city);
        if (reasonActInactMap != null) {
            street.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Street street = (Street) baseEntity;
        super.populateFrom(street, cloneOptions);
        this.setStreetCode(street.getStreetCode());
        this.setStreetName(street.getStreetName());
        this.setAbbreviation(street.getAbbreviation());
        this.setCity(street.getCity());
        if (street.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) street.getReasonActInactMap().cloneYourself(cloneOptions));
        }

    }

    @Override
    public String getDisplayName() {
        return streetName;
    }

}
