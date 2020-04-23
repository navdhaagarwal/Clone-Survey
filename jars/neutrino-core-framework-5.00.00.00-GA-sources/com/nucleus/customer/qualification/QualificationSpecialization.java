/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.customer.qualification;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
// Mentioning explicit table name to keep table name under 30 characters
@Table(name = "QUALIFICATION_SPL", indexes={@Index(name="specialization_fk_index",columnList="specialization_fk")})
@Synonym(grant="ALL")
public class QualificationSpecialization extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    private String            specializationCode;

    @Sortable(index = 1)
    private String            specializationName;

    private String            specializationDescription;

    public String getSpecializationCode() {
        return specializationCode;
    }

    public void setSpecializationCode(String specializationCode) {
        this.specializationCode = specializationCode;
    }

    public String getSpecializationName() {
        return specializationName;
    }

    public void setSpecializationName(String specializationName) {
        this.specializationName = specializationName;
    }

    public String getSpecializationDescription() {
        return specializationDescription;
    }

    public void setSpecializationDescription(String specializationDescription) {
        this.specializationDescription = specializationDescription;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QualificationSpecialization qualificationSpecialization = (QualificationSpecialization) baseEntity;
        super.populate(qualificationSpecialization, cloneOptions);
        qualificationSpecialization.setSpecializationCode(specializationCode);
        qualificationSpecialization.setSpecializationDescription(specializationDescription);
        qualificationSpecialization.setSpecializationName(specializationName);
    }

}