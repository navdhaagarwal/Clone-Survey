package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STRING_LENGTH_FOUR_THOUSAND;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;


/**
 * @author mukul.kumar
 * @Maps Letter with templates
 */
@Entity 
@Cacheable
@DynamicInsert 
@DynamicUpdate
@Table(name = "COM_COMMUNICATION_TEMPLATE_DTL")
@Synonym(grant="SELECT")
public class CommunicationTemplate extends BaseMasterEntity implements Cloneable {

	
	private static final long serialVersionUID = -7633334884671873060L;
	

	@Transient
	private Long communicationMasterId;
	
	@ManyToOne(fetch = FetchType.LAZY, optional=false)
	@JoinColumn(name = "COMMUNICATION_MST_ID", referencedColumnName = "ID")
	private CommunicationName communication;
	
	@Column(name="COMMN_TEMPLATE_CODE", nullable = false)
	private String communicationTemplateCode;
	
	@Column(name="COMMN_TEMPLATE_NAME",  nullable = false)
	private String communicationTemplateName;
	
	@Column(name="COMMN_TEMPLATE_FILE")
	private String communicationTemplateFile;
	
	@Column(name="TEMPLATE_TEXT", length = STRING_LENGTH_FOUR_THOUSAND)
	private String templateText;
	
	@Column(name="COMMN_SUBJECT")
	private String subject;

    @ManyToOne
    @JoinColumn(name="PWD_POLICY_ID")
    private AttachmentEncryptionPolicy attachmentEncryptionPolicy;
    
    @OneToOne
    @JoinColumn(name="PWD_DECRPT_TXT_ID")    
    private PasswordDecryptionText passwordDecryptionText;
    
    @Column(name="TEMPLATE_REFERENCE_NUMBER", length = 100)
    private String templateReferenceNumber;
    
    private String uploadedDocumentId;
    
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

	public String getCommunicationTemplateCode() {
		return communicationTemplateCode;
	}

	public void setCommunicationTemplateCode(String communicationTemplateCode) {
		this.communicationTemplateCode = communicationTemplateCode;
	}

	public String getCommunicationTemplateName() {
		return communicationTemplateName;
	}

	public void setCommunicationTemplateName(String communicationTemplateName) {
		this.communicationTemplateName = communicationTemplateName;
	}

	public String getCommunicationTemplateFile() {
		return communicationTemplateFile;
	}

	public void setCommunicationTemplateFile(String communicationTemplateFile) {
		this.communicationTemplateFile = communicationTemplateFile;
	}

	public String getTemplateText() {
		return templateText;
	}

	public void setTemplateText(String templateText) {
		this.templateText = templateText;
	}

	public Long getCommunicationMasterId() {
		return this.communication.getId();
	}

	public void setCommunicationMasterId(Long communicationMasterId) {
		this.communicationMasterId = communicationMasterId;
	}

	public CommunicationName getCommunication() {
		return communication;
	}

	public void setCommunication(CommunicationName communication) {
		this.communication = communication;
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getTemplateReferenceNumber() {
		return templateReferenceNumber;
	}

	public void setTemplateReferenceNumber(String templateReferenceNumber) {
		this.templateReferenceNumber = templateReferenceNumber;
	}

	public CommunicationTemplate clone() throws CloneNotSupportedException{
		CommunicationTemplate cloneCommunicationTemplate=null;
	
		cloneCommunicationTemplate = (CommunicationTemplate)super.clone();
	
	return cloneCommunicationTemplate;
	}

	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions){
		CommunicationTemplate communicationTemplate = (CommunicationTemplate)baseEntity;
        super.populate(communicationTemplate, cloneOptions);
        communicationTemplate.setCommunicationMasterId(this.getCommunication().getId());
        communicationTemplate.setCommunication(this.getCommunication());
        communicationTemplate.setCommunicationTemplateCode(this.getCommunicationTemplateCode());
        communicationTemplate.setCommunicationTemplateName(this.getCommunicationTemplateName());
        communicationTemplate.setCommunicationTemplateFile(this.getCommunicationTemplateFile());
        communicationTemplate.setTemplateText(this.getTemplateText());
        communicationTemplate.setSubject(this.getSubject());
        communicationTemplate.setTemplateReferenceNumber(templateReferenceNumber);
        communicationTemplate.setUploadedDocumentId(this.getUploadedDocumentId());
        
        Boolean hydrateObject=cloneOptions.getCloneOptionAsBoolean((CloneOptionConstants.HYDRATE_OBJECT));
        if(hydrateObject)
        {
        	if(this.getCommunication()!=null)
        	{
            	communicationTemplate.setCommunication((CommunicationName) this.getCommunication().cloneYourself(cloneOptions));        
        		
        	}
        	if(this.getAttachmentEncryptionPolicy()!=null)
        	{
        		communicationTemplate.setAttachmentEncryptionPolicy(
        				(AttachmentEncryptionPolicy) this.getAttachmentEncryptionPolicy().cloneYourself(cloneOptions));
        		
        	}
        	if(this.getPasswordDecryptionText()!=null)
        	{
        		communicationTemplate.setPasswordDecryptionText(
        				(PasswordDecryptionText) this.getPasswordDecryptionText().cloneYourself(cloneOptions));
        		
        	}
                                 	
        }
        
	    
      }

	
	

	    @Override
	    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
	    	CommunicationTemplate communicationTemplate = (CommunicationTemplate)baseEntity;
	        super.populateFrom(communicationTemplate, cloneOptions);
	        this.setCommunicationMasterId(communicationTemplate.getCommunicationMasterId());
	        this.setCommunicationTemplateCode(communicationTemplate.getCommunicationTemplateCode());
	        this.setCommunicationTemplateName(communicationTemplate.getCommunicationTemplateName());
	        this.setCommunicationTemplateFile(communicationTemplate.getCommunicationTemplateFile());
	        this.setTemplateText(communicationTemplate.getTemplateText());
	        this.setSubject(communicationTemplate.getSubject());
	        this.setUploadedDocumentId(communicationTemplate.getUploadedDocumentId());
	        this.setTemplateReferenceNumber(communicationTemplate.getTemplateReferenceNumber());
	        Boolean hydrateObject=cloneOptions.getCloneOptionAsBoolean((CloneOptionConstants.HYDRATE_OBJECT));
	        if(hydrateObject)
	        {
	        	if(communicationTemplate.getCommunication()!=null)
	        	{
			        this.setCommunication((CommunicationName) communicationTemplate.getCommunication().cloneYourself(cloneOptions));
	        		
	        	}
	        	if(communicationTemplate.getAttachmentEncryptionPolicy()!=null)
	        	{
					this.setAttachmentEncryptionPolicy((AttachmentEncryptionPolicy) communicationTemplate
							.getAttachmentEncryptionPolicy().cloneYourself(cloneOptions));
	        		
	        	}
	        	if(communicationTemplate.getPasswordDecryptionText()!=null)
	        	{
					this.setPasswordDecryptionText((PasswordDecryptionText) communicationTemplate
							.getPasswordDecryptionText().cloneYourself(cloneOptions));
	        		
	        	}
	                                 	
	        }
	        
	        
	    }

	    @Override
        public String getDisplayName() {
            return getCommunicationTemplateName();
        }

		public String getUploadedDocumentId() {
			return uploadedDocumentId;
		}

		public void setUploadedDocumentId(String uploadedDocumentId) {
			this.uploadedDocumentId = uploadedDocumentId;
		}
}
