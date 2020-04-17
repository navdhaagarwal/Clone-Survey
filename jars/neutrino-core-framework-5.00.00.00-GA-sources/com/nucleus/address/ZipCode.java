/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.address;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;

import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.entity.masking.MaskingEntityListener;
import com.nucleus.master.BaseMasterEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@DeletionPreValidator
@Synonym(grant="ALL")
@Table(indexes = {@Index(name="RAIM_PERF_45_4018",columnList="REASON_ACT_INACT_MAP"),
        @Index(name="zipCode_index",columnList="zipCode")})
public class ZipCode extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    /**
     * Attribute to hold the zipCode assigned to the area
     */
    private String            zipCode;
    /**
     * Attribute to hold the placeName assigned
     */
    @Sortable
    private String            placeName;

    /**
     * Attribute to hold the CountryCode of the country to which ZipCode is assigned
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Country           country;

    /**
     * Attribute to hold the StateCode of the state to which ZipCode is assigned
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private State             state;

    /**
     * Attribute to hold the CityCode of the city to which ZipCode is assigned
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private City              city;

    /**
     * Attribute to hold the VillageCode of the city to which ZipCode is assigned
     */

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ZIP_CODE_VILLAGE", joinColumns= {@JoinColumn(name="ZIPCODE_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name="VILLAGE_MST_ID", referencedColumnName = "ID")})
    private List<VillageMaster> village;

    private transient Long[] villageIds;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;
    /**

     /**
     * Attribute to hold the negativeArea
     */
    private boolean           negativeArea;

    /**
     * Attribute to hold the telephoneExchange
     */
    private String            telephoneExchange;

    /**
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * @param zipCode the zipCode to set
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * @return the placeName
     */
    public String getPlaceName() {
        return placeName;
    }

    /**
     * @param placeName the placeName to set
     */
    public void setPlaceName(String placeName) {
        this.placeName = placeName;
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

    /**
     * @return the city
     */
    public City getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(City city) {
        this.city = city;
    }

    public List<VillageMaster> getVillage() {
        return village;
    }

    public void setVillage(List<VillageMaster> village) {
        this.village = village;
    }

    public Long[] getVillageIds() {
        return villageIds;
    }

    public void setVillageIds(Long[] villageIds) {
        this.villageIds = villageIds;
    }

    /**
     * @return the negativeArea
     */
    public boolean isNegativeArea() {
        return negativeArea;
    }

    /**
     * @param negativeArea the negativeArea to set
     */
    public void setNegativeArea(boolean negativeArea) {
        this.negativeArea = negativeArea;
    }

    /**
     * @return the telephoneExchange
     */
    public String getTelephoneExchange() {
        return telephoneExchange;
    }

    /**
     * @param telephoneExchange the telephoneExchange to set
     */
    public void setTelephoneExchange(String telephoneExchange) {
        this.telephoneExchange = telephoneExchange;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ZipCode zipcode = (ZipCode) baseEntity;
        super.populate(zipcode, cloneOptions);
        zipcode.setZipCode(zipCode);
        zipcode.setPlaceName(placeName);
        zipcode.setCity(city);
        if(village != null) {
            zipcode.setVillage(new ArrayList<VillageMaster>(this.village));
        }
        zipcode.setCountry(country);
        zipcode.setNegativeArea(negativeArea);
        zipcode.setState(state);
        zipcode.setTelephoneExchange(telephoneExchange);
        if (reasonActInactMap != null) {
            zipcode.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ZipCode zipcode = (ZipCode) baseEntity;
        super.populateFrom(zipcode, cloneOptions);
        this.setZipCode(zipcode.getZipCode());
        this.setPlaceName(zipcode.getPlaceName());
        this.setCity(zipcode.getCity());
        if(zipcode.getVillage() != null) {
            this.setVillage(zipcode.getVillage());
        }
        this.setCountry(zipcode.getCountry());
        this.setNegativeArea(zipcode.isNegativeArea());
        this.setState(zipcode.getState());
        this.setTelephoneExchange(zipcode.getTelephoneExchange());
        if (zipcode.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) zipcode.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    /*@Override
    public String toString() {
        return "ZipCode [zipCode=" + zipCode + ", placeName=" + placeName + ", city=" + city + ", state=" + state
                + ", country=" + country + ", negativeArea=" + negativeArea + ", telephoneExchange=" + telephoneExchange
                + "]";
    }*/

    @Override
    public String getDisplayName() {
        return getZipCode();
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("ZipCode Master Object received to be saved ------------>");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Zip Code : " + zipCode);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Place Name : " + placeName);
        stf.append(SystemPropertyUtils.getNewline());
        if (city != null) {
            stf.append("City : " + city.getId());
        }
        stf.append(SystemPropertyUtils.getNewline());
        if (state != null) {
            stf.append("State : " + state.getId());
        }
        stf.append(SystemPropertyUtils.getNewline());
        if (country != null) {
            stf.append("Country : " + country.getId());
        }
        log = stf.toString();
        return log;
    }

}
