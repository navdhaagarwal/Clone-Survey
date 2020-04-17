package com.nucleus.notificationMaster;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.document.core.entity.Document;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.AttachmentEncryptionPolicy;
import com.nucleus.html.util.HtmlUtils;
import com.nucleus.letterMaster.LetterType;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="RAIM_PERF_45_4057",columnList="REASON_ACT_INACT_MAP")})
public class NotificationMaster extends BaseMasterEntity {

    @Transient
    private static final long                 serialVersionUID  = 123569L;

    @ManyToOne
    private NotificationMasterType            notificationMasterType;

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(FetchMode.SUBSELECT)
    private List<String>                      toUsers;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private NotificationAdditionalInformation notificationAdditionalInformation;
    
    @OneToMany(cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "notificationmaster_fk")
	List<NotificationAudienceDetails> notificationAudienceDetailsList;

    @OneToOne
    private Document                          attachedDocument;

    @OneToOne(cascade = CascadeType.ALL)
    private ReasonsActiveInactiveMapping reasonActInactMap;

    @Column(length=4000)
    private String                            templateText;

    private String                            mailSubject;

    private boolean                           trackDeliveryStatus;

    private boolean                           trackReadStatus;

    private String                            notificationCode;

    private String                            notificationDescription;

    @Sortable
    private String                            notificationName;
    
    @Column(length=1000)
    private String                            additionalTextBox;

    /*It tell whether the email body is In-line or Uploaded*/
    private String                            emailBodyType;

    /*This field is used to store email body file name. Its not used right now but it may be used later so right now making it transient*/
    @Transient
    private String                            emailBodyFileName;

    /*Document to store Email Body File uploaded. It stores the couchDB id for this uploaded file*/
    @OneToOne
    private Document                          emailBodyDocument;
    
    @OneToOne
    private Document                          whatsAppBodyDocument;

    public static final String                UploadedEmailBody = "UPLOADED_EMAIL_BODY";
    public static final String                InlineTextBody    = "INLINE_TEXT_BODY";

    @Column(name="ENABLE_PASSWORD")
    private boolean							  enablePassword;
    
    @Column(name="ATTACHMENT_OPTION")
    private boolean							  attachmentOption;
    
    @Column(name="ATTACHMENT_ENABLED")
    private boolean							  attachment;
	
	@ManyToOne
    private AttachmentEncryptionPolicy       attachmentEncryptionPolicy;
    
    @ManyToOne
    private LetterType letterType;
    
    private Boolean customerCommunication;

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }

    public boolean isEnablePassword() {
		return enablePassword;
	}

	public void setEnablePassword(boolean enablepassword) {
		this.enablePassword = enablepassword;
	}

	public boolean isAttachmentOption() {
		return attachmentOption;
	}

	public void setAttachmentOption(boolean attachmentOption) {
		this.attachmentOption = attachmentOption;
	}

	public boolean isAttachment() {
		return attachment;
	}

	public void setAttachment(boolean attachment) {
		this.attachment = attachment;
	}

	public AttachmentEncryptionPolicy getAttachmentEncryptionPolicy() {
		return attachmentEncryptionPolicy;
	}

	public void setAttachmentEncryptionPolicy(
			AttachmentEncryptionPolicy attachmentEncryptionPolicy) {
		this.attachmentEncryptionPolicy = attachmentEncryptionPolicy;
	}

	public LetterType getLetterType() {
		return letterType;
	}

	public void setLetterType(LetterType letterType) {
		this.letterType = letterType;
	}

	public String getNotificationCode() {
        return notificationCode;
    }

    public void setNotificationCode(String notificationCode) {
        this.notificationCode = notificationCode;
    }

    public String getNotificationDescription() {
        return notificationDescription;
    }

    public void setNotificationDescription(String notificationDescription) {
        this.notificationDescription = notificationDescription;
    }

    public String getNotificationName() {
        return notificationName;
    }

    public void setNotificationName(String notificationName) {
        this.notificationName = notificationName;
    }

    public boolean isTrackDeliveryStatus() {
        return trackDeliveryStatus;
    }

    public void setTrackDeliveryStatus(boolean trackDeliveryStatus) {
        this.trackDeliveryStatus = trackDeliveryStatus;
    }

    public boolean isTrackReadStatus() {
        return trackReadStatus;
    }

    public void setTrackReadStatus(boolean trackReadStatus) {
        this.trackReadStatus = trackReadStatus;
    }

    public NotificationMasterType getNotificationMasterType() {
        return notificationMasterType;
    }

    public void setNotificationMasterType(NotificationMasterType notificationType) {
        this.notificationMasterType = notificationType;
    }

    public List<String> getToUsers() {
        return toUsers;
    }

    public void setToUsers(List<String> toUsers) {
        this.toUsers = toUsers;
    }

    public NotificationAdditionalInformation getNotificationAdditionalInformation() {
        return notificationAdditionalInformation;
    }

    public void setNotificationAdditionalInformation(NotificationAdditionalInformation notificationAdditionalInformation) {
        this.notificationAdditionalInformation = notificationAdditionalInformation;
    }

    public Document getAttachedDocument() {
        return attachedDocument;
    }

    public void setAttachedDocument(Document attachedDocument) {
        this.attachedDocument = attachedDocument;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getAdditionalTextBox() {
        return additionalTextBox;
    }

    public void setAdditionalTextBox(String additionalTextBox) {
        this.additionalTextBox = additionalTextBox;
    }

    public String getTemplateText() {
        return templateText;
    }

    public void setTemplateText(String templateText) {
        this.templateText = templateText;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {

        NotificationMaster notificationMaster = (NotificationMaster) baseEntity;
        super.populate(notificationMaster, cloneOptions);
        notificationMaster.setNotificationMasterType(notificationMasterType);
        notificationMaster
                .setNotificationAdditionalInformation((notificationAdditionalInformation != null) ? (NotificationAdditionalInformation) notificationAdditionalInformation
                        .cloneYourself(cloneOptions) : null);

        List<String> toUsersList = new ArrayList<>();
        if (toUsers != null) {
            for (String toUser : toUsers) {
                toUsersList.add(toUser != null ? toUser : null);
            }
        }
        notificationMaster.setToUsers(toUsersList);
        notificationMaster.setAttachedDocument(attachedDocument);
        notificationMaster.setTemplateText(templateText);
        notificationMaster.setTrackDeliveryStatus(trackDeliveryStatus);
        notificationMaster.setTrackReadStatus(trackReadStatus);
        notificationMaster.setNotificationCode(notificationCode);
        notificationMaster.setNotificationName(notificationName);
        notificationMaster.setNotificationDescription(notificationDescription);
        notificationMaster.setMailSubject(mailSubject);
        notificationMaster.setAdditionalTextBox(additionalTextBox);
        notificationMaster.setEmailBodyType(emailBodyType);
        notificationMaster.setCustomerCommunication(customerCommunication);
        notificationMaster.setEmailBodyFileName(emailBodyFileName);
        notificationMaster.setEmailBodyDocument(emailBodyDocument);
        notificationMaster.setWhatsAppBodyDocument(whatsAppBodyDocument);
        notificationMaster.setEnablePassword(enablePassword);
        notificationMaster.setAttachmentOption(attachmentOption);
        notificationMaster.setAttachment(attachment);
        notificationMaster.setLetterType(letterType);
        notificationMaster.setAttachmentEncryptionPolicy(attachmentEncryptionPolicy);
        List<NotificationAudienceDetails> audienceDetailsList = new ArrayList<NotificationAudienceDetails>();
        if (notificationAudienceDetailsList != null) {
            for (NotificationAudienceDetails audDetails : notificationAudienceDetailsList) {
            	audienceDetailsList.add(audDetails);
            }
        }
       
        
        
        
        notificationMaster.setNotificationAudienceDetailsList(audienceDetailsList);
        if (reasonActInactMap != null) {
            notificationMaster.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
        }
    }

    public String getEmailBodyFileName() {
        return emailBodyFileName;
    }

    public void setEmailBodyFileName(String emailBodyFileName) {
        this.emailBodyFileName = emailBodyFileName;
    }

    public List<NotificationAudienceDetails> getNotificationAudienceDetailsList() {
		return notificationAudienceDetailsList;
	}

	public void setNotificationAudienceDetailsList(
			List<NotificationAudienceDetails> notificationAudienceDetailsList) {
		this.notificationAudienceDetailsList = notificationAudienceDetailsList;
	}

	@Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		List<NotificationAudienceDetails> notificationAudienceDetails = new ArrayList<>();
        NotificationMaster notificationMaster = (NotificationMaster) baseEntity;
        super.populateFrom(notificationMaster, cloneOptions);
        this.setNotificationMasterType(notificationMaster.getNotificationMasterType());
        this.setNotificationAdditionalInformation(notificationMaster.getNotificationAdditionalInformation());
    
        this.setAttachedDocument(notificationMaster.getAttachedDocument());
        this.setTemplateText(notificationMaster.getTemplateText());
        this.setTrackDeliveryStatus(notificationMaster.isTrackDeliveryStatus());
        this.setTrackReadStatus(notificationMaster.isTrackReadStatus());
        this.setCustomerCommunication(notificationMaster.getCustomerCommunication());
        this.setNotificationCode(notificationMaster.getNotificationCode());
        this.setNotificationName(notificationMaster.getNotificationName());
        this.setNotificationDescription(notificationMaster.getNotificationDescription());
        this.setMailSubject(notificationMaster.getMailSubject());
        this.setAdditionalTextBox(notificationMaster.getAdditionalTextBox());
        this.setEmailBodyType(notificationMaster.getEmailBodyType());
        this.setEmailBodyFileName(notificationMaster.getEmailBodyFileName());
        this.setEmailBodyDocument(notificationMaster.getEmailBodyDocument());
        this.setWhatsAppBodyDocument(notificationMaster.getWhatsAppBodyDocument());
		if (notificationMaster.getNotificationAudienceDetailsList() != null) {
			for (NotificationAudienceDetails audienceDetail : notificationMaster.getNotificationAudienceDetailsList()) {
				notificationAudienceDetails.add(audienceDetail);
			}
		}
		
		
		
	       
		if (notificationMaster.getToUsers() != null) {
			 List<String> toUsersList = new ArrayList<>();
			for (String user : notificationMaster.getToUsers()) {
				 toUsersList.add(user);
			}
			this.setToUsers(toUsersList);
		}
        this.setNotificationAudienceDetailsList(notificationAudienceDetails);
        this.setLetterType(notificationMaster.getLetterType());
        this.setEnablePassword(notificationMaster.isEnablePassword());
        this.setAttachmentOption(notificationMaster.isAttachmentOption());
        this.setAttachment(notificationMaster.isAttachment());
        this.setAttachmentEncryptionPolicy(notificationMaster.getAttachmentEncryptionPolicy());
        if (notificationMaster.getReasonActInactMap() != null) {
            this.setReasonActInactMap((ReasonsActiveInactiveMapping) notificationMaster.getReasonActInactMap().cloneYourself(cloneOptions));
        }
    }

   

	public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("Notification Name: " + notificationName);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Notification Code: " + notificationCode);
        stf.append(SystemPropertyUtils.getNewline());
        if (notificationMasterType != null) {
            stf.append("Notification Type: " + notificationMasterType.getId());

        }
        log = stf.toString();
        return log;
    }

    public String getEmailBodyType() {
        return emailBodyType;
    }

    public void setEmailBodyType(String emailBodyType) {
        this.emailBodyType = emailBodyType;
    }

    public Document getEmailBodyDocument() {
        return emailBodyDocument;
    }

    public void setEmailBodyDocument(Document emailBodyDocument) {
        this.emailBodyDocument = emailBodyDocument;
    }
    
    @Override
    public String getDisplayName() {
        return notificationName;
    }
    
    public String getNormalTextFromTemplateText() {
        return HtmlUtils.getNormalTextFromHtmlText(templateText);
    }


    public void setCustomerCommunication(Boolean customerCommunication) {
        this.customerCommunication = customerCommunication;
    }

    public Boolean getCustomerCommunication() {
        return customerCommunication;
    }

	public Document getWhatsAppBodyDocument() {
		return whatsAppBodyDocument;
	}

	public void setWhatsAppBodyDocument(Document whatsAppBodyDocument) {
		this.whatsAppBodyDocument = whatsAppBodyDocument;
	}
}
