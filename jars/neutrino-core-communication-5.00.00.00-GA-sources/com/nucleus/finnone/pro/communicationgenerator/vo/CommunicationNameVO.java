package com.nucleus.finnone.pro.communicationgenerator.vo;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.constants.CustomerInternalFlag;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.rules.model.SourceProduct;
/**
 * @author gajendra.jatav
 */


public class CommunicationNameVO {
    

    private Long id;
    
    private List<CommunicationTemplateVo> communicationTemplateVoList;
    
    private boolean activeFlag = true; 


    private String communicationCode;

    
    private String communicationName;

    
    private CommunicationType communicationType;

    private CustomerInternalFlag customerInternalFlag;

    private SourceProduct sourceProduct;

    private String location;
    
    private boolean adHocCommunication;

    private Long[] attachmentIds;
    

    private Map<String, Object> viewProperties;
    
    private String communicationReferenceNumber;
    
    private String operationType;
    
    private Long[] communicationParameters;
    
    public Long[] getCommunicationParameters() {
		return communicationParameters;
	}

	public void setCommunicationParameters(Long[] communicationParameters) {
		this.communicationParameters = communicationParameters;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public String getCommunicationReferenceNumber() {
		return communicationReferenceNumber;
	}

	public void setCommunicationReferenceNumber(String communicationReferenceNumber) {
		this.communicationReferenceNumber = communicationReferenceNumber;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getViewProperties() {
        return viewProperties;
    }

    public void setViewProperties(Map<String, Object> viewProperties) {
        this.viewProperties = viewProperties;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }    
    

    public String getCommunicationCode() {
        return communicationCode;
    }

    public void setCommunicationCode(String communicationCode) {
        this.communicationCode = communicationCode;
    }

    public String getCommunicationName() {
        return communicationName;
    }

    public void setCommunicationName(String communicationName) {
        this.communicationName = communicationName;
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

    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAdHocCommunication() {
        return adHocCommunication;
    }

    public void setAdHocCommunication(boolean adHocCommunication) {
        this.adHocCommunication = adHocCommunication;
    }

    public Long[] getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(Long[] attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public CommunicationName updateCommunicationName(CommunicationName communicationName)
    {
        
        communicationName.setId(this.id);
        communicationName.setActiveFlag(this.activeFlag);
        communicationName.setCommunicationName(this.communicationName);
        communicationName.setLocation(this.location);
        return communicationName;
    }

	public List<CommunicationTemplateVo> getCommunicationTemplateVoList() {
		return communicationTemplateVoList;
	}

	public void setCommunicationTemplateVoList(List<CommunicationTemplateVo> communicationTemplateVoList) {
		this.communicationTemplateVoList = communicationTemplateVoList;
	}
}
