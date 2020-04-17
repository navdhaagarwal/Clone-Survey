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
@Cacheable
@DeletionPreValidator
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="SELECT,REFERENCES")
@Table(indexes={@Index(name="RAIM_PERF_45_4264",columnList="REASON_ACT_INACT_MAP"),
        @Index(name="cityCode_index",columnList="cityCode"), @Index(name="cityName_index",columnList="cityName")})
public class City extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    /**
     * Attribute to hold the code of the City
     */
    private String            cityCode;

    /**
     * Attribute to hold the name of the City
     */
    @Sortable
    private String            cityName;

    /**
     * Attribute to hold the code of the District in which the city is located
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private District             district;

    /**
     * Attribute to hold the code of the State in which the city is located
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private State             state;

    /**
     * Attribute to hold the code of the Country in which the city is located
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private Country           country;

    /**
     * Attribute to hold the STD code
     */
    private String            stdCode;

    /**
     * Attribute to hold the City MICR Code

     */
    private String            cityMICRCode;
    /**
     * Attribute to hold the Location Type

     */
    @ManyToOne
    private LocationType      locationType;
    /**
     * Attribute to hold the CityCategorization

     */
    @ManyToOne
    private CityType          cityCategorization;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    /**
     * Attribute to mark city Sensitive
     */
    private boolean           highRiskArea;

    // Fields as requested by ICICI. ICICI requested these fields must be maintained.
    private String            cityRegion;
    private String            amxCityCode;
    private String            ordinal;
    private String            vstsCityCode;
    private String            iataCityCode;
    private String            cityCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    private CityRiskCategory cityRiskCategory;

    /**
     * @return the cityCode
     */
    public String getCityCode() {
        return cityCode;
    }

    /**
     * @param cityCode the cityCode to set
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * @return the cityName
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * @param cityName the cityName to set
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
	 * @return the district
	 */
	public District getDistrict() {
		return district;
	}

	/**
	 * @param district the district to set
	 */
	public void setDistrict(District district) {
		this.district = district;
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
     * @return the country
     */
    public Country getCountry() {
        return country;
    }

    /**
     * @return the stdCode
     */
    public String getStdCode() {
        return stdCode;
    }

    /**
     * @param stdCode the stdCode to set
     */
    public void setStdCode(String stdCode) {
        this.stdCode = stdCode;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(Country country) {
        this.country = country;
    }

    /**
     * @return the cityMICRCode
     */
    public String getCityMICRCode() {
        return cityMICRCode;
    }

    /**
     * @param cityMICRCode the cityMICRCode to set
     */
    public void setCityMICRCode(String cityMICRCode) {
        this.cityMICRCode = cityMICRCode;
    }

    /**
     * @return the Location Type
     */
    public LocationType getLocationType() {
        return locationType;
    }

    /**
     * @param Location Type the Location Type to set
     */
    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    /**
     * @return the CityCategorization
     */
    public CityType getCityCategorization() {
        return cityCategorization;
    }

    /**
     * @param CityType the CityCategorization to set
     */
    public void setCityCategorization(CityType cityCategorization) {
        this.cityCategorization = cityCategorization;
    }

    /**
     * @return the highRiskArea
     */
    public boolean isHighRiskArea() {
        return highRiskArea;
    }

    /**
     * @param highRiskArea the highRiskArea to set
     */
    public void setHighRiskArea(boolean highRiskArea) {
        this.highRiskArea = highRiskArea;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    public CityRiskCategory getCityRiskCategory() {
        return cityRiskCategory;
    }

    public void setCityRiskCategory(CityRiskCategory cityRiskCategory) {
        this.cityRiskCategory = cityRiskCategory;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        City city = (City) baseEntity;
        super.populate(city, cloneOptions);
        city.setCityName(cityName);
        city.setCityCode(cityCode);
        city.setCityMICRCode(cityMICRCode);
        city.setHighRiskArea(highRiskArea);
        city.setDistrict(district);
        city.setState(state);
        city.setCountry(country);
        city.setStdCode(stdCode);
        city.setLocationType(locationType);
        city.setCityCategorization(cityCategorization);
        city.setCityRiskCategory(cityRiskCategory);
        if (reasonActInactMap != null) {
            city.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        City city = (City) baseEntity;
        super.populateFrom(city, cloneOptions);
        this.setCityName(city.getCityName());
        this.setCityCode(city.getCityCode());
        this.setCityMICRCode(city.getCityMICRCode());
        this.setHighRiskArea(city.isHighRiskArea());
        this.setDistrict(city.getDistrict());
        this.setState(city.getState());
        this.setCountry(city.getCountry());
        this.setStdCode(city.getStdCode());
        this.setLocationType(city.getLocationType());
        this.setCityCategorization(city.getCityCategorization());
        this.setCityRiskCategory(city.getCityRiskCategory());
        if (city.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) city.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    /* (non-Javadoc) @see java.lang.Object#toString() */
    @Override
    public String toString() {
        return "City [cityCode=" + cityCode + ", cityName=" + cityName + ", district=" + district + ", state=" + state + ", country=" + country
                + ", stdCode=" + stdCode + ", cityMICRCode=" + cityMICRCode + ", highRiskArea=" + highRiskArea +  ", cityRiskCategory=" + cityRiskCategory +"]";
    }

    @Override
    public String getDisplayName() {
        return getCityName();
    }

    public String getLogInfo() {
        String log = null;
        StringBuilder stf = new StringBuilder();
        stf.append("City master object received to be saved ----->");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("City Code :" + cityCode);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("City Name : " + cityName);
        stf.append(SystemPropertyUtils.getNewline());
        if (state != null) {
            stf.append("State :" + state.getId());
        }
        if (district != null) {
        	stf.append(SystemPropertyUtils.getNewline());
            stf.append("District :" + district.getId());
        }
        stf.append(SystemPropertyUtils.getNewline());
        if (country != null) {
            stf.append("Country :" + country.getId());
        }
        if(cityRiskCategory!=null){
            stf.append("CityRiskCategory:" + cityRiskCategory.getId());
        }
        log = stf.toString();
        return log;
    }

    public String getCityRegion() {
        return cityRegion;
    }

    public void setCityRegion(String cityRegion) {
        this.cityRegion = cityRegion;
    }

    public String getAmxCityCode() {
        return amxCityCode;
    }

    public void setAmxCityCode(String amxCityCode) {
        this.amxCityCode = amxCityCode;
    }

    public String getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(String ordinal) {
        this.ordinal = ordinal;
    }

    public String getVstsCityCode() {
        return vstsCityCode;
    }

    public void setVstsCityCode(String vstsCityCode) {
        this.vstsCityCode = vstsCityCode;
    }

    public String getIataCityCode() {
        return iataCityCode;
    }

    public void setIataCityCode(String iataCityCode) {
        this.iataCityCode = iataCityCode;
    }

    public String getCityCategory() {
        return cityCategory;
    }

    public void setCityCategory(String cityCategory) {
        this.cityCategory = cityCategory;
    }

}
