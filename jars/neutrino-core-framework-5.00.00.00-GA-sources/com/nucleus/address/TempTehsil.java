package com.nucleus.address;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class TempTehsil extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



}

