package com.nucleus.contact;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.nucleus.address.Address;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * @author Nucleus Software Exports Limited
 * Information about a contact.
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class DetailedContactInfo extends BaseEntity {

    // ~ Static variables/initializers ==============================================================

    private static final long       serialVersionUID = 52461;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "contact_info_fk")
    private List<PhoneNumber>       phoneNumbers;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "contact_info_fk")
    private List<Address>           addresses;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "contact_info_fk")
    private List<EMailInfo>         emails;
	
	@Transient
    private PhoneNumber fax;

    @Embedded
    private CallPreference          callPreference;

    @Embedded
    private StmntSubsrptnPreference stmntSubsrptnPreference;

    private String                  mailingName;
    
    private String gcdId;

    private Boolean consentToCall;

    /**
     * Instantiates a new detailed contact info.
     */
    public DetailedContactInfo() {
    }

    /**
     * Instantiates a new detailed contact info.
     *
     * @param contactInfo the contact info
     */
    public DetailedContactInfo(SimpleContactInfo contactInfo) {
        if (contactInfo.getAddress() != null) {
            addAddress(contactInfo.getAddress());
        }
        if (contactInfo.getMobileNumber() != null) {
            addPhoneNumber(contactInfo.getMobileNumber());
        }
        if (contactInfo.getPhoneNumber() != null) {
            addPhoneNumber(contactInfo.getPhoneNumber());
        }
        if (contactInfo.getEmail() != null) {
            addEmail(contactInfo.getEmail());
        }
    }

    /**
     * Gets the call preference.
     *
     * @return the call preference
     */
    public CallPreference getCallPreference() {
        return callPreference;
    }

    /**
     * Sets the call preference.
     *
     * @param callPreference the new call preference
     */
    public void setCallPreference(CallPreference callPreference) {
        this.callPreference = callPreference;
    }

    /**
     * Adds the address.
     *
     * @param address the address
     */
    public void addAddress(Address address) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        addresses.add(address);
    }

    /**
     * Gets the phone numbers.
     *
     * @return the phone numbers
     */
    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * Adds the phone number.
     *
     * @param phoneNumber the phone number
     */
    public void addPhoneNumber(PhoneNumber phoneNumber) {
        if (this.phoneNumbers == null) {
            this.phoneNumbers = new ArrayList<>();
        }
        this.phoneNumbers.add(phoneNumber);
    }

    /**
     * Sets the phone numbers.
     *
     * @param phoneNumbers the new phone numbers
     */
    public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    @Override
    protected void populate(BaseEntity clonedEntity, CloneOptions cloneOptions) {
        super.populate(clonedEntity, cloneOptions);
        DetailedContactInfo contactInfo = (DetailedContactInfo) clonedEntity;
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            contactInfo.setPhoneNumbers(new ArrayList<PhoneNumber>());
            for (PhoneNumber phoneNumber : phoneNumbers) {
                contactInfo.getPhoneNumbers().add((PhoneNumber) phoneNumber.cloneYourself(cloneOptions));
            }
        }
        if (addresses != null && !addresses.isEmpty()) {
            contactInfo.setAddresses(new ArrayList<Address>());
            for (Address address : addresses) {
                contactInfo.getAddresses().add((Address) address.cloneYourself(cloneOptions));
            }
        }
        if (emails != null && !emails.isEmpty() ){
            contactInfo.setEmails(new ArrayList<EMailInfo>());
            for (EMailInfo eMailInfo : emails) {
                contactInfo.getEmails().add((EMailInfo) eMailInfo.cloneYourself(cloneOptions));
            }
        }
        if (callPreference != null) {
            CallPreference clonedCallPreference = new CallPreference();
            clonedCallPreference.setDetailedCallPrefInfoForDay(callPreference.getDetailedCallPrefInfoForDay());
            clonedCallPreference.setNumberOfCallsPerDay(callPreference.getNumberOfCallsPerDay());
            clonedCallPreference.setPrefferedLanguage(callPreference.getPrefferedLanguage());
            clonedCallPreference.setPrefferedModeOfCommunication(callPreference.getPrefferedModeOfCommunication());
            clonedCallPreference.setReceivePromotionalCalls(callPreference.getReceivePromotionalCalls());
            contactInfo.setCallPreference(clonedCallPreference);
        }
        this.setConsentToCall(((DetailedContactInfo) clonedEntity).getConsentToCall());
    }

    /**
     * Gets the addresses.
     *
     * @return the addresses
     */
    public List<Address> getAddresses() {
        return addresses;
    }

    /**
     * Sets the addresses.
     *
     * @param addresses the new addresses
     */
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    /**
     * Gets the emails.
     *
     * @return the emails
     */
    public List<EMailInfo> getEmails() {
        return emails;
    }

    /**
     * Sets the emails.
     *
     * @param emails the new emails
     */
    public void setEmails(List<EMailInfo> emails) {
        this.emails = emails;
    }

    /**
     * Adds the email.
     *
     * @param email the email
     */
    public void addEmail(EMailInfo email) {
        if (email != null) {
            if (this.emails == null) {
                this.emails = new ArrayList<>();
            }
            this.emails.add(email);
        }
    }

    public Boolean getConsentToCall() {
        return consentToCall;
    }

    public void setConsentToCall(Boolean consentToCall) {
        this.consentToCall = consentToCall;
    }

    public StmntSubsrptnPreference getStmntSubsrptnPreference() {
        return stmntSubsrptnPreference;
    }

    public void setStmntSubsrptnPreference(StmntSubsrptnPreference stmntSubsrptnPreference) {
        this.stmntSubsrptnPreference = stmntSubsrptnPreference;
    }

    public String getMailingName() {
        return mailingName;
    }

    public void setMailingName(String mailingName) {
        this.mailingName = mailingName;
    }
	
	public PhoneNumber getFax() {
        return fax;
    }

        public void setFax(PhoneNumber fax) {
        this.fax = fax;
    }

	public String getGcdId() {
		return gcdId;
	}

	public void setGcdId(String gcdId) {
		this.gcdId = gcdId;
	}

}