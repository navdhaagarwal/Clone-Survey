package com.nucleus.address;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="SELECT,REFERENCES")
@Table(indexes={
        @Index(name="RAIM_PERF_45_4241",columnList="REASON_ACT_INACT_MAP"),
        @Index(name="areaName_index",columnList="areaName"),
		@Index(name="areaCode_index",columnList="areaCode")
		})
public class Area extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    /**
     * Attribute to hold the code of the area to be captured
     */
    private String            areaCode;

    /**
     * Attribute to hold the description of the area to be captured
     */
    @Sortable
    private String            areaName;

    /**
     * Attribute to hold the area category
     */
    @ManyToOne
    private AreaType          areaCategorization;

    /**
     * Attribute to hold whether the area captured is negative or not
     */
    private boolean           negativeArea;

    /**
     * Attribute to hold the city to which Area is covered
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private City              city;

    /**
     * Attribute to hold the village to which Area is covered
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private VillageMaster       village;

    /**
     * Attribute to hold the Zipcode to which Area is covered
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private ZipCode           zipcode;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    /**
     * @return the areaCode
     */
    public String getAreaCode() {
        return areaCode;
    }

    /**
     * @param areaCode the areaCode to set
     */
    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    /**
     * @return the areaName
     */
    public String getAreaName() {
        return areaName;
    }

    /**
     * @param areaName the areaName to set
     */
    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    /**
     * @return the areaCategorization
     */
    public AreaType getAreaCategorization() {
        return areaCategorization;
    }

    /**
     * @param areaCategorization the areaCategorization to set
     */
    public void setAreaCategorization(AreaType areaCategorization) {
        this.areaCategorization = areaCategorization;
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

    /**
     * @return the village
     */
    public VillageMaster getVillage() {
        return village;
    }

    /**
     * @param village the village to set
     */
    public void setVillage(VillageMaster village) {
        this.village = village;
    }

    /**
     * @return the zipcode
     */
    public ZipCode getZipcode() {
        return zipcode;
    }

    /**
     * @param zipcode the zipcode to set
     */
    public void setZipcode(ZipCode zipcode) {
        this.zipcode = zipcode;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Area area = (Area) baseEntity;
        super.populate(area, cloneOptions);
        area.setAreaName(areaName);
        area.setAreaCode(areaCode);
        area.setCity(city);
        area.setVillage(village);
        area.setZipcode(zipcode);
        area.setNegativeArea(negativeArea);
        area.setAreaCategorization(areaCategorization);
        if (reasonActInactMap != null) {
            area.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Area area = (Area) baseEntity;
        super.populateFrom(area, cloneOptions);
        this.setAreaName(area.getAreaName());
        this.setAreaCode(area.getAreaCode());
        this.setCity(area.getCity());
        this.setVillage(area.getVillage());
        this.setZipcode(area.getZipcode());
        this.setNegativeArea(area.isNegativeArea());
        this.setAreaCategorization(area.getAreaCategorization());
        if (area.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) area.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

    @Override
    public String getDisplayName() {
        return getAreaCode();
    }

    public String getLogInfo() {
        String log = null;
        StringBuilder stf = new StringBuilder();
        stf.append("Area Master Object received to be saved ------------> ");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Area Code :" + areaCode);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Area Name :" + areaName);
        stf.append(SystemPropertyUtils.getNewline());
        if (areaCategorization != null) {
            stf.append("Area Categorisation :" + areaCategorization.getId());
        }
        stf.append(SystemPropertyUtils.getNewline());
        if (city != null) {
            stf.append("City :" + city.getId());
        }
        stf.append(SystemPropertyUtils.getNewline());
        if (village != null) {
            stf.append("Village :" + village.getId());
        }
        stf.append(SystemPropertyUtils.getNewline());
        if (zipcode != null) {
            stf.append("Zipcode :" + zipcode.getId());
        }

        log = stf.toString();
        return log;
    }
}
