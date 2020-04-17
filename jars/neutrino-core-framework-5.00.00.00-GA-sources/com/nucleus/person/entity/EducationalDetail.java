package com.nucleus.person.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.customer.qualification.EducationScoreType;
import com.nucleus.customer.qualification.QualificationClassification;
import com.nucleus.customer.qualification.QualificationSpecialization;
import com.nucleus.customer.qualification.QualificationType;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
@Table(indexes={@Index(name="person_info_fk_index",columnList="person_info_fk")})
public class EducationalDetail extends BaseEntity {

    @Transient
    private static final long           serialVersionUID = 52461;

    @ManyToOne(fetch=FetchType.LAZY)
    private QualificationType           qualificationType;

    @ManyToOne(fetch=FetchType.LAZY)
    private QualificationClassification qualificationClassification;

    @ManyToOne(fetch=FetchType.LAZY)
    private QualificationSpecialization qualificationSpecialization;

    private String                      university;

    private String                      institute;

    private Integer                     yearOfPassing;

    private String                      score;

    @ManyToOne(fetch=FetchType.LAZY)
    private EducationScoreType          scoreType;

    private Boolean                     highestDegree;
    
    private String gcdId;
    
    

    public String getGcdId() {
		return gcdId;
	}

	public void setGcdId(String gcdId) {
		this.gcdId = gcdId;
	}

	public Boolean getHighestDegree() {
        return highestDegree;
    }

    public void setHighestDegree(Boolean highestDegree) {
        this.highestDegree = highestDegree;
    }

    public QualificationType getQualificationType() {
        return qualificationType;
    }

    /**
     * qualification which the applicant has already passed.
     */
    public void setQualificationType(QualificationType qualificationType) {
        this.qualificationType = qualificationType;
    }

    /**
     * @return the qualificationClassification
     */
    public QualificationClassification getQualificationClassification() {
        return qualificationClassification;
    }

    /**
     * @return the qualificationSpecialization
     */
    public QualificationSpecialization getQualificationSpecialization() {
        return qualificationSpecialization;
    }

    /**
     * @param qualificationClassification the qualificationClassification to set
     */
    public void setQualificationClassification(QualificationClassification qualificationClassification) {
        this.qualificationClassification = qualificationClassification;
    }

    /**
     * @param qualificationSpecialization the qualificationSpecialization to set
     */
    public void setQualificationSpecialization(QualificationSpecialization qualificationSpecialization) {
        this.qualificationSpecialization = qualificationSpecialization;
    }

    public String getUniversity() {
        return university;
    }

    /**
     * To capture the name of the University or Board from which student has completed that qualification course.
     */
    public void setUniversity(String university) {
        this.university = university;
    }

    public String getInstitute() {
        return institute;
    }

    /**
     * To capture the name of the Institute from which student has completed that qualification course
     */
    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public Integer getYearOfPassing() {
        return yearOfPassing;
    }

    /**
     * To capture the Year in which course was completed by the applicant
     */
    public void setYearOfPassing(Integer yearOfPassing) {
        this.yearOfPassing = yearOfPassing;
    }

    public String getScore() {
        return score;
    }

    /**
     * To capture the grade/ percentage marks secured by the applicant
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     * @return the scoreType
     */
    public EducationScoreType getScoreType() {
        return scoreType;
    }

    /**
     * @param scoreType the scoreType to set
     */
    public void setScoreType(EducationScoreType scoreType) {
        this.scoreType = scoreType;
    }

    @Override
    public String getDisplayName() {
        return getQualificationType().getName();
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        EducationalDetail educationalDetail = (EducationalDetail) baseEntity;
        super.populate(educationalDetail, cloneOptions);
        educationalDetail.setInstitute(institute);
        educationalDetail.setQualificationClassification(qualificationClassification);
        educationalDetail.setQualificationSpecialization(qualificationSpecialization);
        educationalDetail.setQualificationType(qualificationType);
        educationalDetail.setHighestDegree(highestDegree);
        educationalDetail.setScore(score);
        educationalDetail.setScoreType(scoreType);
        educationalDetail.setUniversity(university);
        educationalDetail.setYearOfPassing(yearOfPassing);
    }

}
