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
package com.nucleus.user;

import javax.persistence.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.contact.SimpleContactInfo;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;
import com.nucleus.person.entity.GenderType;
import com.nucleus.person.entity.SalutationType;
import com.nucleus.user.cache.UserProfileEntityListener;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
@Cacheable
@EntityListeners(UserProfileEntityListener.class)
@Table(indexes={@Index(name="USER_PROFILE_UK_IDX1",columnList="ASSOCIATED_USER",unique=true),
		@Index(name="UP_FIRST_NAME_INDX",columnList="firstName",unique=false),
        @Index(name="UP_MIDDLE_NAME_INDX",columnList="middleName",unique=false),
        @Index(name="UP_LAST_NAME_INDX",columnList="lastName",unique=false),
        @Index(name="UP_FOURTH_NAME_INDX",columnList="fourthName",unique=false),
        @Index(name="UP_FULL_NAME_INDX",columnList="fullName",unique=false),
        @Index(name="UP_ALIAS_NAME_INDX",columnList="aliasName",unique=false),
        @Index(name="UP_SIM_CON_INF_INDX",columnList="SIMPLE_CONTACT_INFO")
})
public class UserProfile extends BaseEntity {

    private static final long serialVersionUID = 5566766873331294534L;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)

    @EmbedInAuditAsValueObject
    private SimpleContactInfo simpleContactInfo;

    @OneToOne(fetch = FetchType.LAZY)
    User                      associatedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    User                      escalationUser;

    @EmbedInAuditAsValue
    private String            firstName;

    @EmbedInAuditAsValue
    private String            middleName;

    @EmbedInAuditAsValue
    private String            lastName;

    @EmbedInAuditAsValue
    private String            fourthName;

    @EmbedInAuditAsValue
    private String            fullName;

    @EmbedInAuditAsValue
    private String            aliasName;

    @ManyToOne
    @EmbedInAuditAsReference
    private SalutationType    salutation;

    @ManyToOne
    @EmbedInAuditAsReference
    private GenderType        gender;

    @EmbedInAuditAsValue
    private String            mothersMaidenName;

    @EmbedInAuditAsValue
    private String            MobileImeiNumber;

    @Embedded
    @EmbedInAuditAsValueObject
    private IPAddressRange    addressRange;

    @ManyToOne
    @EmbedInAuditAsReference
    private AccessType userAccessType;

    private String            photoUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference
    private UserDepartment	userDepartment;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference
    private UserClassification	userClassification;
    
   
	@ManyToOne(fetch = FetchType.LAZY)
    @EmbedInAuditAsReference
    private UserCategory	userCategory;

    @Transient
    private String auditTrailIdentifier;
    
      
	public UserDepartment getUserDepartment() {
		return userDepartment;
	}

	public void setUserDepartment(UserDepartment userDepartment) {
		this.userDepartment = userDepartment;
	}
	
	 public UserClassification getUserClassification() {
			return userClassification;
		}

		public void setUserClassification(UserClassification userClassification) {
			this.userClassification = userClassification;
		}

		public UserCategory getUserCategory() {
			return userCategory;
		}

		public void setUserCategory(UserCategory userCategory) {
			this.userCategory = userCategory;
		}
		
		
		
		
	public SimpleContactInfo getSimpleContactInfo() {
        return simpleContactInfo;
    }

    public void setSimpleContactInfo(SimpleContactInfo simpleContactInfo) {
        this.simpleContactInfo = simpleContactInfo;
    }

    public User getAssociatedUser() {
        return associatedUser;
    }

    public void setAssociatedUser(User associatedUser) {
        this.associatedUser = associatedUser;
    }

    public AccessType getUserAccessType() {
        return userAccessType;
    }

    public void setUserAccessType(AccessType userAccessType) {
        this.userAccessType = userAccessType;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public User getEscalationUser() {
        return escalationUser;
    }

    public void setEscalationUser(User escalationUser) {
        this.escalationUser = escalationUser;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
    	String name = null;
    	
    	if(fullName != null){
    		name = fullName.trim();
    	}
    	
    	this.fullName = name;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public SalutationType getSalutation() {
        return salutation;
    }

    public void setSalutation(SalutationType salutation) {
        this.salutation = salutation;
    }

    public GenderType getGender() {
        return gender;
    }

    public void setGender(GenderType gender) {
        this.gender = gender;
    }

    public String getMothersMaidenName() {
        return mothersMaidenName;
    }

    public void setMothersMaidenName(String mothersMaidenName) {
        this.mothersMaidenName = mothersMaidenName;
    }

    public String getMobileImeiNumber() {
        return MobileImeiNumber;
    }

    public void setMobileImeiNumber(String mobileImeiNumber) {
        MobileImeiNumber = mobileImeiNumber;
    }

    public IPAddressRange getAddressRange() {
        return addressRange;
    }

    public void setAddressRange(IPAddressRange addressRange) {
        this.addressRange = addressRange;
    }

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("FirstName:" + firstName);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("LastName:" + lastName);
        stf.append(SystemPropertyUtils.getNewline());
        if (associatedUser != null) {
            stf.append("UserName:" + associatedUser.getUsername());
            stf.append(SystemPropertyUtils.getNewline());
            stf.append("Password:" + associatedUser.getPassword());
            stf.append(SystemPropertyUtils.getNewline());
            stf.append("Password Hint Question:" + associatedUser.getPasswordHintQuestion());
            stf.append(SystemPropertyUtils.getNewline());
            stf.append("Password Hint Answer:" + associatedUser.getPasswordHintAnswer());
            stf.append(SystemPropertyUtils.getNewline());
            if (associatedUser.getDeviationLevel() != null) {
                stf.append("Deviation Level:" + associatedUser.getDeviationLevel().getId());
            }

        }
        log = stf.toString();
        return log;
    }

    @Override
    public void loadLazyFields()
    {
    	if(getSimpleContactInfo()!=null)
    	{
    		getSimpleContactInfo().loadLazyFields();
    	}
    	if(getAssociatedUser()!=null)
    	{
    		getAssociatedUser().loadLazyFields();
    	}
    	if(getEscalationUser()!=null)
    	{
    		getEscalationUser().loadLazyFields();
    	}
    	if(getGender()!=null)
    	{
    		getGender().loadLazyFields();
    	}
    }

    public String getAuditTrailIdentifier() {
        return auditTrailIdentifier;
    }

    public void setAuditTrailIdentifier(String auditTrailIdentifier) {
        this.auditTrailIdentifier = auditTrailIdentifier;
    }
}
