package com.nucleus.core.formsConfiguration;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.core.formsConfiguration.validationcomponent.DedupeMapperVO;
import com.nucleus.core.formsConfiguration.validationcomponent.DynamicFormMapperVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationMetadataVO;
import com.nucleus.rules.model.SourceProduct;

public class FormVO implements Serializable {

    private static final long     serialVersionUID = 2954847182847973322L;

    private Long                  id;

    private String                formName;

    private String                formHeader;

    private String                formTitle;

    private String                formDescription;

    private List<FormContainerVO> containerVOList;

    private String                invocationPoint;

    private Long                  modelMetaDataId;

    private Long                  uiMetaDataId;

    private Boolean               allowSaveOption;

    private Boolean               allowBorder;

    private String                formuuid;

    private String                formVersion;

    private Boolean               createNewVersion;

    private String                modelUri;
    
    private SourceProduct    sourceProduct;
    
    private Long                  sourceProductId;
    
    private Boolean isShareable;
    
    private Boolean isFilter;
    
    private DynamicFormFilter dynamicFormFilter;
     
    private List<FormValidationMetadataVO> validationsVO;

    private List<DynamicFormMapperVO> dynamicFormMapperVOList;
    
    private DedupeMapperVO dedupeMapperVO;
    
    private Set<String> dedupeKeySet;
    
    private Set<String> dedupeSetToBeDeleted;

    private Long taskId;
    
    private Integer columnLayout;

    public List<DynamicFormMapperVO> getDynamicFormMapperVOList() {
        return dynamicFormMapperVOList;
    }

    public void setDynamicFormMapperVOList(List<DynamicFormMapperVO> dynamicFormMapperVOList) {
        this.dynamicFormMapperVOList = dynamicFormMapperVOList;
    }

    public DynamicFormFilter getDynamicFormFilter() {
		return dynamicFormFilter;
	}

	public void setDynamicFormFilter(DynamicFormFilter dynamicFormFilter) {
		this.dynamicFormFilter = dynamicFormFilter;
	}

	public Boolean getIsShareable() {
		return isShareable;
	}

	public void setIsShareable(Boolean isShareable) {
		this.isShareable = isShareable;
	}

	public Boolean getIsFilter() {
		return isFilter;
	}

	public void setIsFilter(Boolean isFilter) {
		this.isFilter = isFilter;
	}

	private boolean activeFlag = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the allowBorder
     */
    public Boolean getAllowBorder() {
        return allowBorder;
    }

    /**
     * @param allowBorder the allowBorder to set
     */
    public void setAllowBorder(Boolean allowBorder) {
        this.allowBorder = allowBorder;
    }

    /**
     * @return the formuuid
     */
    public String getFormuuid() {
        return formuuid;
    }

    /**
     * @param formuuid the formuuid to set
     */
    public void setFormuuid(String formuuid) {
        this.formuuid = formuuid;
    }

    /**
     * @return the formVersion
     */
    public String getFormVersion() {
        return formVersion;
    }

    /**
     * @param formVersion the formVersion to set
     */
    public void setFormVersion(String formVersion) {
        this.formVersion = formVersion;
    }

    /**
     * @return the formName
     */
    public String getFormName() {
        return formName;
    }

    /**
     * @param formName the formName to set
     */
    public void setFormName(String formName) {
        this.formName = formName;
    }

    /**
     * @return the formHeader
     */
    public String getFormHeader() {
        return formHeader;
    }

    /**
     * @param formHeader the formHeader to set
     */
    public void setFormHeader(String formHeader) {
        this.formHeader = formHeader;
    }

    /**
     * @return the formDescription
     */
    public String getFormDescription() {
        return formDescription;
    }

    /**
     * @param formDescription the formDescription to set
     */
    public void setFormDescription(String formDescription) {
        this.formDescription = formDescription;
    }

    /**
     * @return the containerVOList
     */
    public List<FormContainerVO> getContainerVOList() {
        return containerVOList;
    }

    /**
     * @param containerVOList the containerVOList to set
     */
    public void setContainerVOList(List<FormContainerVO> containerVOList) {
        this.containerVOList = containerVOList;
    }

    /**
     * @return the invocationPoint
     */
    public String getInvocationPoint() {
        return invocationPoint;
    }

    /**
     * @param invocationPoint the invocationPoint to set
     */
    public void setInvocationPoint(String invocationPoint) {
        this.invocationPoint = invocationPoint;
    }

    public Long getModelMetaDataId() {
        return modelMetaDataId;
    }

    public void setModelMetaDataId(Long modelMetaDataId) {
        this.modelMetaDataId = modelMetaDataId;
    }

    public Long getUiMetaDataId() {
        return uiMetaDataId;
    }

    public void setUiMetaDataId(Long uiMetaDataId) {
        this.uiMetaDataId = uiMetaDataId;
    }

    public Boolean getAllowSaveOption() {
        return allowSaveOption;
    }

    public void setAllowSaveOption(Boolean allowSaveOption) {
        this.allowSaveOption = allowSaveOption;
    }

    /**
     * @return the createNewVersion
     */
    public Boolean getCreateNewVersion() {
        return createNewVersion;
    }

    /**
     * @param createNewVersion the createNewVersion to set
     */
    public void setCreateNewVersion(Boolean createNewVersion) {
        this.createNewVersion = createNewVersion;
    }

    /**
     * @return the modelUri
     */
    public String getModelUri() {
        return modelUri;
    }

    /**
     * @param modelUri the modelUri to set
     */
    public void setModelUri(String modelUri) {
        this.modelUri = modelUri;
    }

    /**
     * @return the formTitle
     */
    public String getFormTitle() {
        return formTitle;
    }

    /**
     * @param formTitle the formTitle to set
     */
    public void setFormTitle(String formTitle) {
        this.formTitle = formTitle;
    }
    
    public Long getSourceProductId() {
        return sourceProductId;
    }

    public void setSourceProductId(Long sourceProductId) {
        this.sourceProductId = sourceProductId;
    }

    public SourceProduct getSourceProduct() {
        return sourceProduct;
    }

    public void setSourceProduct(SourceProduct sourceProduct) {
        this.sourceProduct = sourceProduct;
    }

	public List<FormValidationMetadataVO> getValidationsVO() {
		return validationsVO;
	}

	public void setValidationsVO(List<FormValidationMetadataVO> validationsVO) {
		this.validationsVO = validationsVO;
	}

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public boolean isActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public DedupeMapperVO getDedupeMapperVO() {
        return dedupeMapperVO;
    }

    public void setDedupeMapperVO(DedupeMapperVO dedupeMapperVO) {
        this.dedupeMapperVO = dedupeMapperVO;
    }

    public Set<String> getDedupeKeySet() {
        return dedupeKeySet;
    }

    public void setDedupeKeySet(Set<String> dedupeKeySet) {
        this.dedupeKeySet = dedupeKeySet;
    }

    public Set<String> getDedupeSetToBeDeleted() {       
        return dedupeSetToBeDeleted;
    }

    public void setDedupeSetToBeDeleted(Set<String> dedupeSetToBeDeleted) {
        this.dedupeSetToBeDeleted = dedupeSetToBeDeleted;
    }

	public Integer getColumnLayout() {
		return columnLayout;
	}

	public void setColumnLayout(Integer columnLayout) {
		this.columnLayout = columnLayout;
	}

    
    
}
