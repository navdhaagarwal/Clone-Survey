package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.STRING_LENGTH_THREE_HUNDRED;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;

/**
 * @author mukul.kumar
 * In this master all parameters for Letters will be maintained.
 */
@Entity
@Cacheable
@DynamicInsert 
@DynamicUpdate
@Table(name = "COM_COMMN_PARAMETER_MST")
@Synonym(grant="SELECT")
public class CommunicationParameter extends BaseMasterEntity implements Cloneable {

	private static final long serialVersionUID = 1340692833619942939L;
	
	@Column(name="PARAMETER_CODE", nullable = false)
	private String parameterCode;
	
	@Column(name="PARAMETER_DESC", nullable = false)
	private String parameterDesc;
	
	@Column(name="PARAMETER_VALUE")
	private String parameterValue;
	
	@Column(name = "IS_IMAGE")
	private Boolean isImage;
	
	@Column(name = "IS_TABLE_PARAMETER")
	private Boolean isTableParameter;
	
	@Column(name="TARGET_SERVICE_NAME")
	private String beanId;
	
	@Column(name="TARGET_METHOD_NAME")
	private String methodName;
	
	@Column(name="SERVICE_INTERFACE_NAME", length = STRING_LENGTH_THREE_HUNDRED)
	private String serviceInterfaceName;
	
	@Column(name="PARAMETER_SOURCE")
	private String parameterSource;
	
	@Column(name="FIELD_TYPE")
	private String fieldType;
	
	@Column(name="FIELD_FORMAT_MASK")
	private String formatMask;
	
	@Column(name="PARAMETER_SOURCE_KEY")
	private String sourceKey;
	
    @Column(name="PARAMETER_DISPLAY_CASE ")
    private Character parameterDisplayCase;    
    
    @ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")
    private SourceProduct sourceProduct;
    
    @Column(name="IS_ADHOC_PARAMETER")
    private boolean adHocParameter;
	
    
    
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions){
        CommunicationParameter communicationParameter = (CommunicationParameter)baseEntity;
        super.populate(communicationParameter, cloneOptions);
        communicationParameter.setParameterCode(parameterCode);
        communicationParameter.setParameterDesc(parameterDesc);
        communicationParameter.setParameterValue(parameterValue);    	
        communicationParameter.setIsImage(isImage);
        communicationParameter.setIsTableParameter(isTableParameter);
        communicationParameter.setBeanId(beanId);    	
        communicationParameter.setMethodName(methodName);
        communicationParameter.setServiceInterfaceName(serviceInterfaceName);
        communicationParameter.setParameterSource(parameterSource);    	
        communicationParameter.setFieldType(fieldType);
        communicationParameter.setFormatMask(formatMask);
        communicationParameter.setSourceKey(sourceKey);
        communicationParameter.setParameterDisplayCase(parameterDisplayCase);     
        communicationParameter.setSourceProduct(sourceProduct);
        communicationParameter.setAdHocParameter(adHocParameter);        
        
      }

    
    

        @Override
        protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        	CommunicationParameter communicationParameter = (CommunicationParameter)baseEntity;
            super.populate(communicationParameter, cloneOptions);
            this.setParameterCode(communicationParameter.getParameterCode());
            this.setParameterDesc(communicationParameter.getParameterDesc());
            this.setParameterValue(communicationParameter.getParameterValue());    	
            this.setIsImage(communicationParameter.getIsImage());
            this.setIsTableParameter(communicationParameter.getIsTableParameter());
            this.setBeanId(communicationParameter.getBeanId());    	
            this.setMethodName(communicationParameter.getMethodName());
            this.setServiceInterfaceName(communicationParameter.getServiceInterfaceName());
            this.setParameterSource(communicationParameter.getParameterSource());    	
            this.setFieldType(communicationParameter.getFieldType());
            this.setFormatMask(communicationParameter.getFormatMask());
            this.setSourceKey(communicationParameter.getSourceKey());
            this.setParameterDisplayCase(communicationParameter.getParameterDisplayCase());     
            this.setSourceProduct(communicationParameter.getSourceProduct());
            this.setAdHocParameter(communicationParameter.isAdHocParameter());        
                      
        } 
    
    
    
	public String getSourceKey() {
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public String getParameterSource() {
		return parameterSource;
	}

	public void setParameterSource(String parameterSource) {
		this.parameterSource = parameterSource;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public String getFormatMask() {
		return formatMask;
	}

	public void setFormatMask(String formatMask) {
		this.formatMask = formatMask;
	}

	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getServiceInterfaceName() {
		return serviceInterfaceName;
	}

	public void setServiceInterfaceName(String serviceInterfaceName) {
		this.serviceInterfaceName = serviceInterfaceName;
	}

	public String getParameterCode() {
		return parameterCode;
	}

	public void setParameterCode(String parameterCode) {
		this.parameterCode = parameterCode;
	}

	public String getParameterDesc() {
		return parameterDesc;
	}

	public void setParameterDesc(String parameterDesc) {
		this.parameterDesc = parameterDesc;
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}

	public Boolean getIsImage() {
		return isImage;
	}

	public Boolean getIsTableParameter() {
		return isTableParameter;
	}

	public void setIsTableParameter(Boolean isTableParameter) {
		this.isTableParameter = isTableParameter;
	}

	public void setIsImage(Boolean isImage) {
		this.isImage = isImage;
	}
	
	public CommunicationParameter clone() throws CloneNotSupportedException{

		CommunicationParameter cloneCommunicationParameter=null;
	
		cloneCommunicationParameter = (CommunicationParameter)super.clone();
	return cloneCommunicationParameter;
	}

	public Character getParameterDisplayCase() {
		return parameterDisplayCase;
	}

	public void setParameterDisplayCase(Character parameterDisplayCase) {
		this.parameterDisplayCase = parameterDisplayCase;
	}  

    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

    public boolean isAdHocParameter() {
        return adHocParameter;
    }

    public void setAdHocParameter(boolean adHocParameter) {
        this.adHocParameter = adHocParameter;
    }	
	
    
}
