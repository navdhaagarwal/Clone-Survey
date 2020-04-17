package com.nucleus.notificationMaster;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.contact.EMailInfo;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class NotificationAdditionalInformation extends BaseEntity {

    @Transient
    private static final long serialVersionUID = 1L;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EMailInfo>   email;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhoneNumber> phoneNumber;

    public List<EMailInfo> getEmail() {
        return email;
    }

    public void setEmail(List<EMailInfo> email) {
        this.email = email;
    }

    public List<PhoneNumber> getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(List<PhoneNumber> phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {

        NotificationAdditionalInformation notificationAdditionalInformation = (NotificationAdditionalInformation) baseEntity;
        super.populate(notificationAdditionalInformation, cloneOptions);

        List<EMailInfo> emailList = new ArrayList<EMailInfo>();
        if (email != null) {
            for (EMailInfo eMailInfo : email) {
                emailList.add(eMailInfo != null ? (EMailInfo) eMailInfo.cloneYourself(cloneOptions) : null);
            }
        }
        notificationAdditionalInformation.setEmail(emailList);

        List<PhoneNumber> phoneNumberList = new ArrayList<PhoneNumber>();
        if (phoneNumber != null) {
            for (PhoneNumber phoneNumber : this.phoneNumber) {
                phoneNumberList.add(phoneNumber != null ? (PhoneNumber) phoneNumber.cloneYourself(cloneOptions) : null);
            }
        }
        notificationAdditionalInformation.setPhoneNumber(phoneNumberList);

    }

}
