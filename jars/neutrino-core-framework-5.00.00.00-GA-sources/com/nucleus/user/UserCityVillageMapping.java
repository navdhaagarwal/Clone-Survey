package com.nucleus.user;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;


@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(name = "USER_CITY_VILLAGE_MAPPING")
public class UserCityVillageMapping extends BaseEntity{
    @ManyToOne
    @JoinColumn(name="USERS")
    private User user;

    @Transient
    private String auditTrailIdentifier;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "USER_VILLAGE_JOIN_TABLE", joinColumns= {@JoinColumn(name="USER_CITY_VILLAGE_MAPPING", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name="USER_VILLAGE_MAPPING", referencedColumnName = "ID")})
    @EmbedInAuditAsValueObject
    private List<UserVillageMapping> userVillageMappings;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "USER_CITY_JOIN_TABLE", joinColumns= {@JoinColumn(name="USER_CITY_VILLAGE_MAPPING", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name="USER_CITY_MAPPING", referencedColumnName = "ID")})
    @EmbedInAuditAsValueObject
    private List<UserCityMapping> userCityMappings;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<UserVillageMapping> getUserVillageMappings() {
        return userVillageMappings;
    }

    public void setUserVillageMappings(List<UserVillageMapping> userVillageMappings) {
        this.userVillageMappings = userVillageMappings;
    }

    public List<UserCityMapping> getUserCityMappings() {
        return userCityMappings;
    }

    public void setUserCityMappings(List<UserCityMapping> userCityMappings) {
        this.userCityMappings = userCityMappings;
    }

    public String getAuditTrailIdentifier() {
        return auditTrailIdentifier;
    }

    public void setAuditTrailIdentifier(String auditTrailIdentifier) {
        this.auditTrailIdentifier = auditTrailIdentifier;
    }
}
