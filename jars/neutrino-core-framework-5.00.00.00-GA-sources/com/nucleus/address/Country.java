package com.nucleus.address;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
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
@EntityListeners(CountryEntityListener.class)
@Table(indexes = {@Index(name="RAIM_PERF_45_4382",columnList="REASON_ACT_INACT_MAP"),
        @Index(name="countryISOCode_index",columnList="countryISOCode")})
public class Country extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    /**
     * Attribute to hold the ISO code of the Country
     */
    private String            countryISOCode;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;


    /**
     * Attribute to hold name of the country to be captured
     */
    @Sortable
    private String            countryName;

    /**
     * Attribute to hold the code of the country, according to the International Organization for Standardization (ISO)
     */
    private String            countryISDCode;

    /**
     * Attribute to hold the nationality
     */
    private String            nationality;

    /**
     * Attribute to mark a country as Negative
     */
    private boolean           negativeCountry;

    /**
     * Attribute to hold group of country for GCC, Others and Stateless respectively.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private CountryGroup      countryGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    private GeoRegion         region;

    /**This Field maintains numeric country code as maintained by ICICI , which is 
     * sent to PRIME sub system. 
     */
    private String            numCountryISOCode;

    /**
     * Attribute to hold the defaultFlag
     */
    private Boolean defaultFlag = Boolean.FALSE;

    /**
     * @return the countryISOCode
     */
    public String getCountryISOCode() {
        return countryISOCode;
    }

    /**
     * @param CountryISOCode the CountryISOCode to set
     */
    public void setCountryISOCode(String countryISOCode) {
        this.countryISOCode = countryISOCode;
    }

    /**
     * @return the countryName
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * @param CountryName the CountryName to set
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    /**
     * @return the countryISDCode
     */
    public String getCountryISDCode() {
        return countryISDCode;
    }

    /**
     * @param CountryISDCode the CountryISDCode to set
     */
    public void setCountryISDCode(String countryISDCode) {
        this.countryISDCode = countryISDCode;
    }

    /**
     * @return the nationality
     */
    public String getNationality() {
        return nationality;
    }

    /**
     * @param Nationality the Nationality to set
     */
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    /**
     * @return the negativeCountry
     */
    public boolean isNegativeCountry() {
        return negativeCountry;
    }

    /**
     * @param NegativeCountry the NegativeCountry to set
     */
    public void setNegativeCountry(boolean negativeCountry) {
        this.negativeCountry = negativeCountry;
    }

    /**
     * @return the countryGroup
     */
    public CountryGroup getCountryGroup() {
        return countryGroup;
    }

    /**
     * @param CountryGroup the CountryGroup to set
     */
    public void setCountryGroup(CountryGroup countryGroup) {
        this.countryGroup = countryGroup;
    }

    /**
     * @return the region
     */
    public GeoRegion getRegion() {
        return region;
    }

    /**
     * @param region the region to set
     */
    public void setRegion(GeoRegion region) {
        this.region = region;
    }

    /**
     * @return the defaultFlag
     */
    public Boolean getDefaultFlag() { return defaultFlag; }

    /**
     * @param DefaultFlag the DefaultFlag to set
     */
    public void setDefaultFlag(Boolean defaultFlag) { this.defaultFlag = defaultFlag; }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Country country = (Country) baseEntity;
        super.populate(country, cloneOptions);
        country.setCountryName(countryName);
        country.setCountryGroup(countryGroup);
        country.setCountryISDCode(countryISDCode);
        country.setNationality(nationality);
        country.setNegativeCountry(negativeCountry);
        country.setRegion(region);
        country.setCountryISOCode(countryISOCode);
        country.setNumCountryISOCode(numCountryISOCode);
        country.setDefaultFlag(defaultFlag);
        if (reasonActInactMap != null) {
            country.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Country country = (Country) baseEntity;
        super.populateFrom(country, cloneOptions);
        this.setCountryName(country.getCountryName());
        this.setCountryGroup(country.getCountryGroup());
        this.setCountryISDCode(country.getCountryISDCode());
        this.setNationality(country.getNationality());
        this.setNegativeCountry(country.isNegativeCountry());
        this.setRegion(country.getRegion());
        this.setCountryISOCode(country.getCountryISOCode());
        this.setNumCountryISOCode(country.getNumCountryISOCode());
        this.setDefaultFlag(country.getDefaultFlag());
        if (country.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) country.getReasonActInactMap().cloneYourself(cloneOptions));
        }

    }

    @Override
    public String getDisplayName() {
        return countryName;
    }

    public String getLogInfo() {
        String log = null;
        StringBuilder stf = new StringBuilder();
        stf.append("Country Master Object received to be saved ------------> ");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Country Name : " + countryName);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Country ISD Code :" + countryISDCode);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Country ISO Code :" + countryISOCode);
        log = stf.toString();
        return log;
    }

    public String getNumCountryISOCode() {
        return numCountryISOCode;
    }

    public void setNumCountryISOCode(String numCountryISOCode) {
        this.numCountryISOCode = numCountryISOCode;
    }

}