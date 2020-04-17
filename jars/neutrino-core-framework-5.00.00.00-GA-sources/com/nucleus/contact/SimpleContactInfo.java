package com.nucleus.contact;

import javax.persistence.*;

import com.nucleus.master.audit.annotation.EmbedInAuditAsReference;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.address.Address;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValueObject;

/**
 * @author Nucleus Software Exports Limited
 * Information about a contact.
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(indexes={@Index(name="SCI_ADDRESS",columnList="ADDRESS")})
public class SimpleContactInfo extends BaseEntity {

    // ~ Static variables/initializers ==============================================================

    private static final long serialVersionUID = 52461;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject
    private PhoneNumber       phoneNumber;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject
    private PhoneNumber       mobileNumber;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @EmbedInAuditAsValueObject
    private Address           address;

    @EmbedInAuditAsValue
    private String            postOfficeBoxNumber;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EmbedInAuditAsReference(columnToDisplay = "emailAddress")
    private EMailInfo         email;

    @EmbedInAuditAsValue
    private Boolean         consentToCall;

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return the mobileNumber
     */
    public PhoneNumber getMobileNumber() {
        return mobileNumber;
    }

    /**
     * @param mobileNumber the mobileNumber to set
     */
    public void setMobileNumber(PhoneNumber mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public EMailInfo getEmail() {
        return email;
    }

    public void setEmail(EMailInfo email) {
        this.email = email;
    }

    public String getPostOfficeBoxNumber() {
        return postOfficeBoxNumber;
    }

    public void setPostOfficeBoxNumber(String postOfficeBoxNumber) {
        this.postOfficeBoxNumber = postOfficeBoxNumber;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        SimpleContactInfo simpleContactInfo = (SimpleContactInfo) baseEntity;
        super.populate(simpleContactInfo, cloneOptions);
        simpleContactInfo.setAddress(address != null ? (Address) address.cloneYourself(cloneOptions) : null);
        simpleContactInfo.setEmail(email != null ? (EMailInfo) email.cloneYourself(cloneOptions) : null);
        simpleContactInfo.setMobileNumber(mobileNumber != null ? (PhoneNumber) mobileNumber.cloneYourself(cloneOptions)
                : null);
        simpleContactInfo.setPhoneNumber(phoneNumber != null ? (PhoneNumber) phoneNumber.cloneYourself(cloneOptions) : null);
        simpleContactInfo.setPostOfficeBoxNumber(postOfficeBoxNumber);

        this.setConsentToCall(((SimpleContactInfo) baseEntity).getConsentToCall());
    }

    public Boolean getConsentToCall() {
        return consentToCall;
    }

    public void setConsentToCall(Boolean consentToCall) {
        this.consentToCall = consentToCall;
    }
}