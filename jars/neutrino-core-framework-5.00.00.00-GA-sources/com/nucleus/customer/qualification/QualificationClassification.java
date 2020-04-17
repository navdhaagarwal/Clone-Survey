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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class QualificationClassification extends BaseMasterEntity {

    private static final long                 serialVersionUID = 3103769037146394706L;

    private String                            code;

    private String                            name;

    @ManyToOne
    private QualificationType                 qualificationType;

    @OneToMany
    @JoinColumn(name = "specialization_fk")
    private List<QualificationSpecialization> qualificationSpecializations;

    public QualificationType getQualificationType() {
        return qualificationType;
    }

    public void setQualificationType(QualificationType qualificationType) {
        this.qualificationType = qualificationType;
    }

    public List<QualificationSpecialization> getQualificationSpecializations() {
        return qualificationSpecializations;
    }

    public void setQualificationSpecializations(List<QualificationSpecialization> qualificationSpecializations) {
        this.qualificationSpecializations = qualificationSpecializations;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        QualificationClassification qualificationClassification = (QualificationClassification) baseEntity;
        super.populate(qualificationClassification, cloneOptions);
        qualificationClassification.setCode(code);
        qualificationClassification.setName(name);
        qualificationClassification.setQualificationType(qualificationType);

        List<QualificationSpecialization> qualificationSpecializationList = new ArrayList<QualificationSpecialization>();

        if (qualificationSpecializations != null) {

            for (QualificationSpecialization qualificationSpecialization : qualificationSpecializations) {
                qualificationSpecializationList
                        .add((qualificationSpecialization != null) ? (QualificationSpecialization) qualificationSpecialization
                                .cloneYourself(cloneOptions) : null);
            }
        }
        qualificationClassification.setQualificationSpecializations(qualificationSpecializationList);
    }

}
