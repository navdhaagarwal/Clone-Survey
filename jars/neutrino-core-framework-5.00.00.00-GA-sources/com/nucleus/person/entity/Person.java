package com.nucleus.person.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.Assert;

import com.nucleus.contact.DetailedContactInfo;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class Person extends BaseEntity {

    private static final long       serialVersionUID = 7586167971746919227L;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private DetailedContactInfo     contactInfo;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private PersonInfo              personInfo;

    @OneToMany
    @JoinColumn(name = "person_info_fk")
    private List<EducationalDetail> educationalDetails;

    public DetailedContactInfo getContactInfo() {
        return contactInfo;
    }

    public void createContactInfo() {
        Assert.isNull(contactInfo);
        contactInfo = new DetailedContactInfo();
    }

    public void setContactInfo(DetailedContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public PersonInfo getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(PersonInfo personInfo) {
        this.personInfo = personInfo;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Person person = (Person) baseEntity;
        super.populate(person, cloneOptions);
        if (contactInfo != null) {
            person.setContactInfo((DetailedContactInfo) this.contactInfo.cloneYourself(cloneOptions));
        }
        if (personInfo != null) {
            person.setPersonInfo((PersonInfo) this.personInfo.cloneYourself(cloneOptions));
        }
    }

    public List<EducationalDetail> getEducationalDetails() {
        return educationalDetails;
    }

    public void setEducationalDetails(List<EducationalDetail> educationalDetails) {
        this.educationalDetails = educationalDetails;
    }

}