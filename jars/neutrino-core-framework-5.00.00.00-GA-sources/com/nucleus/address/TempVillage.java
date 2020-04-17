package com.nucleus.address;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class TempVillage extends BaseEntity{

    @Transient
    private static final long serialVersionUID = 1L;

    @Column(name="village_name")
    private String name;

    public String getVillageName() {
        return name;
    }

    public void setVillageName(String villageName) {
        this.name = villageName;
    }
    

}
