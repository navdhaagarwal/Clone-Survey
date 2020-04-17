package com.nucleus.user;

import com.nucleus.address.Area;
import com.nucleus.address.City;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

import javax.persistence.*;

import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;


@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(name = "USER_CITY_MAPPING")
public class UserCityMapping extends BaseEntity{

    @ManyToOne
    @EmbedInAuditAsReference(columnToDisplay = "cityCode")
    private City city;

    @ManyToMany
    @JoinTable(name = "USER_CITY_AREA_MAPPING", joinColumns= {@JoinColumn(name="USER_CITY_MAPPING", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name="AREA", referencedColumnName = "ID")})
    @EmbedInAuditAsReference(columnToDisplay = "areaCode")
    private List<Area> cityAreaList;

    @Transient
    private String auditTrailIdentifier;
    
    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public List<Area> getCityAreaList() {
        return cityAreaList;
    }

    public void setCityAreaList(List<Area> cityAreaList) {
        this.cityAreaList = cityAreaList;
    }

    public String getAuditTrailIdentifier() {
        return auditTrailIdentifier;
    }

    public void setAuditTrailIdentifier(String auditTrailIdentifier) {
        this.auditTrailIdentifier = auditTrailIdentifier;
    }
}
