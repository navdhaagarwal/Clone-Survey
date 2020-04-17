package com.nucleus.person.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.nucleus.houseHold.HouseholdType;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.accountType.AccountTypeForCKYC;
import com.nucleus.address.Country;
import com.nucleus.core.annotations.Synonym;
import io.swagger.annotations.ApiModelProperty;
import com.nucleus.core.formsConfiguration.DynamicForm;
import com.nucleus.core.formsConfiguration.SingleDynamicForm;
import com.nucleus.customer.CustomerCategory;
import com.nucleus.customer.CustomerConstitution;
import com.nucleus.customer.CustomerSegmentType;
import com.nucleus.demographics.RelationshipType;
import com.nucleus.demographics.ReligionType;
import com.nucleus.demographics.ResidentType;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.internetchannel.AccomodationType;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.regional.RegionalData;
import com.nucleus.regional.RegionalEnabled;
/**
 * Information about a contact.
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
@Table(indexes = {@Index (name = "pf_first_name_code_idx", columnList = "firstNameCode"), 
        @Index (name = "pf_middle_name_code_idx", columnList = "middleNameCode"),
        @Index(name="pf_last_name_code_idx",columnList="lastNameCode"),
        @Index(name="pf_full_name_code_idx",columnList="fullNameCode"),
        @Index(name="pf_date_of_birth_idx",columnList="dateOfBirth"),
        @Index (name="pf_first_name_idx", columnList = "firstName"), 
        @Index (name="pf_middle_name_idx", columnList = "middleName"),
        @Index(name="pf_last_name_idx",columnList="lastName"),
        @Index(name="pf_full_name_idx",columnList="fullName")
        })
public class PersonInfo extends BaseEntity implements RegionalEnabled,SingleDynamicForm {

    // ~ Static variables/initializers ==============================================================

    @Transient
    private static final long    serialVersionUID = 52461;

    @Transient
    private static DoubleMetaphone metaphone = new DoubleMetaphone();

    private String               firstName;

    private String               firstNameCode;

    private String               middleName;

    private String               middleNameCode;

    private String               lastName;

    private String               lastNameCode;

    private String               fourthName;

    @Column(length=1000)
    @EmbedInAuditAsValue(displayKey="label.contact.person")
    private String               fullName;

    private String               fullNameCode;

    private String               aliasName;

    @ManyToOne(fetch=FetchType.LAZY)
    private SalutationType       salutation;

    @ManyToOne(fetch=FetchType.LAZY)
    private GenderType           gender;

    private String               mothersMaidenName;

    private String               photoUrl;

    private String               photoDatastoreReferenceKey;

    private String               fatherName;

    private String               motherName;

    @ManyToOne(fetch=FetchType.LAZY)
    private ReligionType         religion;

    @ManyToOne(fetch=FetchType.LAZY)
    private CustomerCategory     customerCategory;

    @ManyToOne(fetch=FetchType.LAZY)
    private CustomerConstitution customerConstitution;

    @ManyToOne(fetch=FetchType.LAZY)
    private MaritalStatusType    maritalStatus;

    @ManyToOne(fetch=FetchType.LAZY)
    private RelationshipType relationshipType;

    @Column
    @Temporal(TemporalType.DATE)
    private Date	             dateOfBirth;

    private String               placeOfBirth;

    private String               isMinor;

    private String               guardianName;

    @ManyToOne(fetch=FetchType.LAZY)
    private ResidentType         residentType;

    @ManyToOne(fetch=FetchType.LAZY)
    private AccomodationType     accomodationType;

    @ManyToOne(fetch=FetchType.LAZY)
    private Country              country;

    @ManyToOne(fetch=FetchType.LAZY)
    private CustomerSegmentType  customerSegmentType;

    /** stores the age of person in months. This field would be persisted/updated at certain stage of workflow.**/
    private String               age;

    private Integer              noOfDependents;

    @ManyToOne(fetch=FetchType.LAZY)
    private PersonWithDisability       personWithDisability;

    @Embedded
    @ApiModelProperty(hidden=true)
    private DynamicForm dynamicForm;

    @ManyToOne(fetch=FetchType.LAZY)
    private SalutationType  maidenSalutation;

    private String maidenFirstName;

    private String maidenMiddleName;

    private String maidenLastName;

    @ManyToOne(fetch=FetchType.LAZY)
    private AccountTypeForCKYC    accountTypeForCKYC;

    private String               signatureUrl;

    private String bankEmpId;
    private String bankEmpName;

    @ManyToOne(fetch=FetchType.LAZY)
    private HouseholdType householdType;


    public String getSignatureUrl() {
        return signatureUrl;
    }

    public void setSignatureUrl(String signatureUrl) {
        this.signatureUrl = signatureUrl;
    }

    public AccountTypeForCKYC getAccountTypeForCKYC() {
        return accountTypeForCKYC;
    }

    public void setAccountTypeForCKYC(AccountTypeForCKYC accountTypeForCKYC) {
        this.accountTypeForCKYC = accountTypeForCKYC;
    }


    public SalutationType getMaidenSalutation() {
        return maidenSalutation;
    }

    public void setMaidenSalutation(SalutationType maidenSalutation) {
        this.maidenSalutation = maidenSalutation;
    }

    public String getMaidenFirstName() {
        return maidenFirstName;
    }

    public void setMaidenFirstName(String maidenFirstName) {
        this.maidenFirstName = maidenFirstName;
    }

    public String getMaidenMiddleName() {
        return maidenMiddleName;
    }

    public void setMaidenMiddleName(String maidenMiddleName) {
        this.maidenMiddleName = maidenMiddleName;
    }

    public String getMaidenLastName() {
        return maidenLastName;
    }

    public void setMaidenLastName(String maidenLastName) {
        this.maidenLastName = maidenLastName;
    }



    public DynamicForm getDynamicForm() {
        return dynamicForm;
    }

    public void setDynamicForm(DynamicForm dynamicForm) {
        this.dynamicForm = dynamicForm;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {

        this.firstName = firstName;
        firstNameCode = encodeName(firstName);
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
        middleNameCode= encodeName(middleName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        lastNameCode = encodeName(lastName);
    }

    public GenderType getGender() {
        return gender;
    }

    public void setGender(GenderType gender) {
        this.gender = gender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoDatastoreReferenceKey() {
        return photoDatastoreReferenceKey;
    }

    public void setPhotoDatastoreReferenceKey(String photoDatastoreReferenceKey) {
        this.photoDatastoreReferenceKey = photoDatastoreReferenceKey;
    }

    public SalutationType getSalutation() {
        return salutation;
    }

    public void setSalutation(SalutationType salutation) {
        this.salutation = salutation;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public ReligionType getReligion() {
        return religion;
    }

    public void setReligion(ReligionType religion) {
        this.religion = religion;
    }

    public MaritalStatusType getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatusType maritalStatus) {
        this.maritalStatus = maritalStatus;
    }


    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getFourthName() {
        return fourthName;
    }

    public void setFourthName(String fourthName) {
        this.fourthName = fourthName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        fullNameCode = encodeName(fullName);
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getMothersMaidenName() {
        return mothersMaidenName;
    }

    public void setMothersMaidenName(String mothersMaidenName) {
        this.mothersMaidenName = mothersMaidenName;
    }

    public String getIsMinor() {
        return isMinor;
    }

    public void setIsMinor(String isMinor) {
        this.isMinor = isMinor;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public ResidentType getResidentType() {
        return residentType;
    }

    public Country getCountry() {
        return country;
    }

    public void setResidentType(ResidentType residentType) {
        this.residentType = residentType;
    }

    /**
     * @return the accomodationType
     */
    public AccomodationType getAccomodationType() {
        return accomodationType;
    }

    /**
     * @param accomodationType the accomodationType to set
     */
    public void setAccomodationType(AccomodationType accomodationType) {
        this.accomodationType = accomodationType;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    /**
     * @return the customerSegmentType
     */
    public CustomerSegmentType getCustomerSegmentType() {
        return customerSegmentType;
    }

    /**
     * @param customerSegmentType the customerSegmentType to set
     */
    public void setCustomerSegmentType(CustomerSegmentType customerSegmentType) {
        this.customerSegmentType = customerSegmentType;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public CustomerCategory getCustomerCategory() {
        return customerCategory;
    }

    public void setCustomerCategory(CustomerCategory customerCategory) {
        this.customerCategory = customerCategory;
    }

    public CustomerConstitution getCustomerConstitution() {
        return customerConstitution;
    }

    public void setCustomerConstitution(CustomerConstitution customerConstitution) {
        this.customerConstitution = customerConstitution;
    }

    public Integer getNoOfDependents() {
        return noOfDependents;
    }

    public void setNoOfDependents(Integer noOfDependents) {
        this.noOfDependents = noOfDependents;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getBankEmpId() {
        return bankEmpId;
    }
    public void setBankEmpId(String bankEmpId) {
        this.bankEmpId = bankEmpId;
    }

    public String getBankEmpName() {
        return bankEmpName;
    }
    public void setBankEmpName(String bankEmpName) {
        this.bankEmpName = bankEmpName;
    }

    /**
     * populate method
     */
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        PersonInfo personInfo = (PersonInfo) baseEntity;
        super.populate(personInfo, cloneOptions);
        personInfo.setAccomodationType(accomodationType);
        personInfo.setAge(age);
        personInfo.setAliasName(aliasName);
        personInfo.setCountry(country);
        personInfo.setCustomerCategory(customerCategory);
        personInfo.setCustomerConstitution(customerConstitution);
        personInfo.setDateOfBirth(dateOfBirth);
        personInfo.setFatherName(fatherName);
        personInfo.setFirstName(firstName);
        personInfo.setFourthName(fourthName);
        personInfo.setFullName(fullName);
        personInfo.setGender(gender);
        personInfo.setGuardianName(guardianName);
        personInfo.setIsMinor(isMinor);
        personInfo.setLastName(lastName);
        personInfo.setMaritalStatus(maritalStatus);
        personInfo.setMiddleName(middleName);
        personInfo.setMotherName(motherName);
        personInfo.setMothersMaidenName(mothersMaidenName);
        personInfo.setNoOfDependents(noOfDependents);
        personInfo.setPhotoDatastoreReferenceKey(photoDatastoreReferenceKey);
        personInfo.setPhotoUrl(photoUrl);
        personInfo.setPlaceOfBirth(placeOfBirth);
        personInfo.setReligion(religion);
        personInfo.setResidentType(residentType);
        personInfo.setSalutation(salutation);
        personInfo.setCustomerSegmentType(customerSegmentType);
        personInfo.setRegionalData(regionalData);
        personInfo.setPersonWithDisability(personWithDisability);
        personInfo.setMaidenSalutation(maidenSalutation);
        personInfo.setMaidenFirstName(maidenFirstName);
        personInfo.setMaidenLastName(maidenLastName);
        personInfo.setMaidenLastName(maidenLastName);
        personInfo.setAccountTypeForCKYC(accountTypeForCKYC);
        personInfo.setRelationshipType(relationshipType);
        personInfo.setBankEmpId(bankEmpId);
        personInfo.setBankEmpName(bankEmpName);
        personInfo.setHouseholdType(householdType);
    }

    @Embedded
    private RegionalData regionalData;

    @Override
    public RegionalData getRegionalData() {
        if(regionalData == null)
            regionalData = new RegionalData();
        return regionalData;
    }

    @Override
    public void setRegionalData(RegionalData regionalData) {
        this.regionalData=regionalData;
    }

    public PersonWithDisability getPersonWithDisability() {
        return personWithDisability;
    }

    public void setPersonWithDisability(PersonWithDisability personWithDisability) {
        this.personWithDisability = personWithDisability;
    }

    private String encodeName(String name) {
        return metaphone.encode(name);
    }

    public HouseholdType getHouseholdType() {
        return householdType;
    }

    public void setHouseholdType(HouseholdType householdType) {
        this.householdType = householdType;
    }
}