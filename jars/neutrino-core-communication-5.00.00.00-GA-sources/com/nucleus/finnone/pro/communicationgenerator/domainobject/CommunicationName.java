package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.entity.CloneOptions;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.constants.CustomerInternalFlag;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterUtils;
import com.nucleus.master.marker.HistoryOptimizable;
import com.nucleus.rules.model.SourceProduct;

/**
 * @author mukul.kumar
 * It's a master for maintaining various Letters
 */
@Entity
@Cacheable
@DynamicInsert 
@DynamicUpdate
@Table(name = "COM_COMMUNICATION_HDR")
@NamedQuery(name="getCommunication",query="select communicationName from CommunicationName communicationName where communicationName.communicationCode=:communicationCode")
@Synonym(grant="SELECT,REFERENCES")
public class CommunicationName extends BaseMasterEntity implements Cloneable, HistoryOptimizable {

    private static final long serialVersionUID = 5820040200936073279L;

    public static final String IT_LETTER_CODE_PROV="PROVISIONAL_IT_CERTIFICATE_LETTER";
    public static final String IT_LETTER_CODE_FINAL="FINAL_IT_CERTIFICATE_LETTER";
    public static final String NOC_LETTER_CODE="NOC_CERTIFICATE_LETTER";
    public static final String NDC_LETTER_CODE="NDC_LETTER";
    public static final String WELCOME_LETTER_CODE="WELCOME_LETTER";
    public static final String BALANCE_CONFIRMATION_LETTER_CODE="BALANCE_CONFIRMATION";
    public static final String SIMULATION_LETTER_CODE_PAYABLE="FORECLOSURE_STATEMENT";
    public static final String SIMULATION_LETTER_CODE_RECEIVABLE="FORECLOSURE_STATEMENT";
    public static final String SWAP_LETTER="SWAP_LETTER";
    public static final String DISBURSAL_COMMERCIAL_VEHICLE_LETTER="DISBURSAL_CV_LETTER";
    public static final String BOUNCE_LETTER="BOUNCE_LETTER";
    public static final String FIXED_TO_FLOAT_LETTER="FIXED_TO_FLOAT_LETTER";
    public static final String PERMIT_RENEWAL_LETTER="RENEWAL_LETTER";
    public static final String DUPLICATE_RC_LETTER="RC_LETTER";
    public static final String DISBURSAL_HOME_LOAN_LETTER="DISBURSAL_HL_LETTER";
    public static final String DISBURSAL_EMI_INITIATION_LETTER="DISBURSAL_EMI_INITIATION_LETTER";
    public static final String EMI_BOUNCE_SMS="EMI_BOUNCE_SMS";
    public static final String AMOUNT_RECEIVED_VIA_CASH_SMS="AMOUNT_RECEIVED_VIA_CASH_SMS";
    public static final String AMOUNT_RECEIVED_VIA_CHEQUE_SMS="AMOUNT_RECEIVED_VIA_CHEQUE_SMS";
    public static final String DISBURSEMENT_INTIMATION_SMS="DISBURSEMENT_INTIMATION_SMS";
    public static final String EMI_DUE_REMINDER_SMS="EMI_DUE_REMINDER_SMS";
    public static final String PAYMENT_NOT_RECEIVED_SMS="PAYMENT_NOT_RECEIVED_SMS";
    public static final String PDC_EXHAUST_LETTER="PDC_EXHAUST_LETTER";
    public static final String LIST_OF_DOCUMENT_LETTER="LIST_OF_DOCS_HL";
    public static final String FRR_LETTER="FRR_LETTER";
    public static final String RESCHEDULING_LETTER="RESCHEDULING_LETTER";
    public static final String RTO_CONFIRMATION_LETTER_CODE="RTO_CONFIRMATION_LETTER";
    public static final String NOC_LETTER_MC="NOC_LETTER_MC";
    public static final String NOC_LETTER_EC="NOC_LETTER_EC";
    public static final String NOC_LETTER_EC_SETTLEMENT="NOC_LETTER_EC_SETTLEMENT";
    public static final String NOC_LETTER_MC_SETTLEMENT="NOC_LETTER_MC_SETTLEMENT";
    public static final String NDC_LETTER_EC="NDC_LETTER_EC";
    public static final String NDC_LETTER_MC="NDC_LETTER_MC";
    public static final String NDC_LETTER_EC_SETTLEMENT="NDC_LETTER_EC_SETTLEMENT";
    public static final String NDC_LETTER_MC_SETTLEMENT="NDC_LETTER_MC_SETTLEMENT";
    public static final String RESCH_CONVERSION_LETTER="RESCH_CONVERSION_LETTER";
    public static final String RESCH_ANCHOR_CHANGE_LETTER="RESCH_ANCHOR_CHANGE_LETTER";

    
    @OneToMany(cascade=CascadeType.ALL,mappedBy = "communication")
    private List<CommunicationTemplate> communicationTemplates;
    
    
    @Column(name="COMMUNICATION_CODE")
    private String communicationCode;

    @Column(name="COMMUNICATION_NAME")
    private String communicationName;

    @ManyToOne
    @JoinColumn(name="COMMUNICATION_TYPE", referencedColumnName="ID")
    private CommunicationType communicationType;

    @ManyToOne
    @JoinColumn(name="CUSTOMER_INTERNAL_FLAG", referencedColumnName="ID")
    private CustomerInternalFlag customerInternalFlag;

    @ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")
    private SourceProduct sourceProduct;

    @Column(name="LOCATION")
    private String location;
    
    @Column(name="IS_ADHOC_COMMUNICATION")
    private boolean adHocCommunication=false;
    
    @Column(name="IS_TEMPLATE_BASED")
    private boolean templateBased=true;

    @OneToMany(cascade={CascadeType.ALL})
    @JoinColumn(name="PARENT_COMMN_ID")
    private List<CommunicationAttachment> attachments;
    
    @ManyToOne
    @JoinColumn(name="PWD_POLICY_ID")
    private AttachmentEncryptionPolicy attachmentEncryptionPolicy;
    
    
    @OneToOne
    @JoinColumn(name="PWD_DECRPT_TXT_ID")    
    private PasswordDecryptionText passwordDecryptionText;
    
    @Column(name="SENDER_EMAIL_ADDRESS")
    private String senderEmailAddress;
    
    @Column(name="COMM_REFERENCE_NUMBER", length = 100)
    private String communicationReferenceNumber;
    
    @ManyToMany
    @JoinTable(name="COM_COMMUNICATION_PARAM", joinColumns= {@JoinColumn(name="COMMUNICATION_MST_ID", referencedColumnName = "ID")},
    inverseJoinColumns = {@JoinColumn(name="PARAMETER_MST_ID", referencedColumnName = "ID")})
    private List<CommunicationParameter> communicationParameters;
    
    public PasswordDecryptionText getPasswordDecryptionText() {
		return passwordDecryptionText;
	}

	public void setPasswordDecryptionText(PasswordDecryptionText passwordDecryptionText) {
		this.passwordDecryptionText = passwordDecryptionText;
	}

	public AttachmentEncryptionPolicy getAttachmentEncryptionPolicy() {
        return attachmentEncryptionPolicy;
    }

    public void setAttachmentEncryptionPolicy(AttachmentEncryptionPolicy attachmentEncryptionPolicy) {
        this.attachmentEncryptionPolicy = attachmentEncryptionPolicy;
    }

    public List<CommunicationAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<CommunicationAttachment> attachments) {
        this.attachments = attachments;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCommunicationCode() {
        return communicationCode;
    }

    public void setCommunicationCode(String communicationCode) {
        this.communicationCode = communicationCode;
    }
    
    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    public void setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
    }

    public String getCommunicationName() {
        return communicationName;
    }

    public void setCommunicationName(String communicationName) {
        this.communicationName = communicationName;
    }

    public List<CommunicationParameter> getCommunicationParameters() {
        return communicationParameters;
    }

    public void setCommunicationParameters(
            List<CommunicationParameter> communicationParameters) {
        this.communicationParameters = communicationParameters;
    }

    public CommunicationType getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(CommunicationType communicationType) {
        this.communicationType = communicationType;
    }

    public CustomerInternalFlag getCustomerInternalFlag() {
        return customerInternalFlag;
    }

    public void setCustomerInternalFlag(CustomerInternalFlag customerInternalFlag) {
        this.customerInternalFlag = customerInternalFlag;
    }

	public String getCommunicationReferenceNumber() {
		return communicationReferenceNumber;
	}

	public void setCommunicationReferenceNumber(String communicationReferenceNumber) {
		this.communicationReferenceNumber = communicationReferenceNumber;
	}

	public CommunicationName clone() throws CloneNotSupportedException{

        CommunicationName cloneCommunicationName=null;
    
        cloneCommunicationName = (CommunicationName)super.clone();
    
    return cloneCommunicationName;
    }

    public List<CommunicationTemplate> getCommunicationTemplates() {
        return communicationTemplates;
    }

    public void setCommunicationTemplates(
            List<CommunicationTemplate> communicationTemplates) {
        this.communicationTemplates = communicationTemplates;
    }
    
    
    
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions){
        CommunicationName communication = (CommunicationName)baseEntity;
        super.populate(communication, cloneOptions);
        communication.setCommunicationCode(this.getCommunicationCode());
        communication.setCommunicationName(this.getCommunicationName());
        communication.setCommunicationType(this.getCommunicationType());
        communication.setCustomerInternalFlag(this.getCustomerInternalFlag());
        communication.setLocation(this.getLocation());
        communication.setAdHocCommunication(this.isAdHocCommunication());
        communication.setSourceProduct(this.getSourceProduct());
		communication.setSenderEmailAddress(senderEmailAddress);
		communication.setCommunicationReferenceNumber(communicationReferenceNumber);
        Boolean hydrateObject=cloneOptions.getCloneOptionAsBoolean((CloneOptionConstants.HYDRATE_OBJECT));
        if (hydrateObject) {
        	if (this.attachmentEncryptionPolicy != null) {
        		communication.setAttachmentEncryptionPolicy(
        				(AttachmentEncryptionPolicy) this.attachmentEncryptionPolicy.cloneYourself(cloneOptions));
        	}
        	if (this.passwordDecryptionText != null) {
        		communication.setPasswordDecryptionText(
        				(PasswordDecryptionText) this.passwordDecryptionText.cloneYourself(cloneOptions));
        	}
        }
        if (hasElements(communicationParameters)) {
            List<CommunicationParameter> parametsrs = new ArrayList<>();
            for (CommunicationParameter attachment : communicationParameters) {
            	if(attachment != null) {
            		parametsrs.add(attachment);
            	}
            }
            communication.setCommunicationParameters(parametsrs);
        }  
        if (hasElements(attachments)) {
            List<CommunicationAttachment> clonedAttachments = new ArrayList<>();
            for (CommunicationAttachment attachment : attachments) {
                clonedAttachments.add((CommunicationAttachment) attachment.cloneYourself(cloneOptions));
            }
            communication.setAttachments(clonedAttachments);
        }
        
        if(communicationTemplates != null && ! communicationTemplates.isEmpty()) {
        	communication.setCommunicationTemplates(new ArrayList<CommunicationTemplate>());
        	for(CommunicationTemplate communicationTemplate : communicationTemplates) {
        		CommunicationTemplate communicationTemplateCloned =  (CommunicationTemplate) communicationTemplate.cloneYourself(cloneOptions);
        		communicationTemplateCloned.setCommunication(communication);
        		communication.getCommunicationTemplates().add(communicationTemplateCloned);
        	}
        }
      }

    
    

        @Override
        protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
            CommunicationName communication = (CommunicationName)baseEntity;
            super.populateFrom(communication, cloneOptions);
            this.setCommunicationCode(communication.getCommunicationCode());
            this.setCommunicationName(communication.getCommunicationName());
            this.setCommunicationType(communication.getCommunicationType());
            this.setCustomerInternalFlag(communication.getCustomerInternalFlag());
            this.setLocation(communication.getLocation());
            this.setAdHocCommunication(communication.isAdHocCommunication());
            this.setSourceProduct(communication.getSourceProduct());
            this.setSenderEmailAddress(senderEmailAddress);
            this.setCommunicationReferenceNumber(communication.getCommunicationReferenceNumber());
            Boolean hydrateObject=cloneOptions.getCloneOptionAsBoolean((CloneOptionConstants.HYDRATE_OBJECT));
            if (hydrateObject) {
            	if (communication.getAttachmentEncryptionPolicy() != null) {
            		this.setAttachmentEncryptionPolicy(
          				(AttachmentEncryptionPolicy) communication.getAttachmentEncryptionPolicy().cloneYourself(cloneOptions));
            	}
            	if (communication.getPasswordDecryptionText() != null) {
					this.setPasswordDecryptionText(
							(PasswordDecryptionText) communication.getPasswordDecryptionText().cloneYourself(cloneOptions));
            	}
            }
            if (hasElements(communication.getCommunicationParameters())) {
            	this.setCommunicationParameters(new ArrayList<CommunicationParameter>());
                for (CommunicationParameter parameter : communication.getCommunicationParameters()) {
                    this.getCommunicationParameters().add(parameter);
                }

            }
            if (hasElements(communication.getAttachments())) {
            	if(this.getAttachments() != null) {
            		this.getAttachments().clear();
            	}else {
            		this.setAttachments(new ArrayList<CommunicationAttachment>());
            	}
                for (CommunicationAttachment attachment : communication.getAttachments()) {
                    this.getAttachments().add((CommunicationAttachment) attachment.cloneYourself(cloneOptions));
                }
            }
            if(ValidatorUtils.hasNoElements(this.getCommunicationTemplates())){
            	this.setCommunicationTemplates(new ArrayList<CommunicationTemplate>());
            }
            if(ValidatorUtils.hasElements(communication.getCommunicationTemplates())) {
            	BaseMasterUtils.mergeModificationsToOrigionalEntity(this.getCommunicationTemplates(), communication.getCommunicationTemplates() , cloneOptions);
            	
            }
            if(this.getCommunicationTemplates() != null && ! this.getCommunicationTemplates().isEmpty()) {
            	for(CommunicationTemplate communicationTemplate : this.getCommunicationTemplates()) {
            		communicationTemplate.setCommunication(this);
            	}
            }
        }        

        public SourceProduct getSourceProduct() {
            return sourceProduct;
        }

        public void setSourceProduct(SourceProduct sourceProduct) {
            this.sourceProduct = sourceProduct;
        }

        public boolean isAdHocCommunication() {
            return adHocCommunication;
        }

        public void setAdHocCommunication(boolean adHocCommunication) {
            this.adHocCommunication = adHocCommunication;
        }
        
		public boolean isTemplateBased() {
			return templateBased;
		}

		public void setTemplateBased(boolean templateBased) {
			this.templateBased = templateBased;
		}
		
        @Override
        public String getDisplayName() {
            return getCommunicationName();
        }
}
