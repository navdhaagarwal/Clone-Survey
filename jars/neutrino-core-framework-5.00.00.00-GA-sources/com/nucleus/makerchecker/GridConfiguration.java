package com.nucleus.makerchecker;

import java.util.List;

public class GridConfiguration {
	
    private String                    entityClass;
    private String                    jspName;
    private Boolean                   processingType;
    private Boolean                   hrefBool;
    private String                    recordUrl;
    private MasterEntityConfiguration masterEntityConfiguration;
    private List<ColumnConfiguration> columnConfigurationList;
    private List<ActionConfiguration> actionConfigurationList;
    private List<AccordianHeaders>    accordianHeadersList;
    private List<String>              initializingPath;
    private String                    entityNameMRKey;
    private String                    springBeanName;
    private String                    key;
    private boolean                   removeCommonActions;
    private Boolean                   isPercentage;
    private String                    defaultSortable;
    private String                    sortDirection;
    private boolean                   disableMasterCreation;
    private Integer                   minCharToBeginSearch;
    private String					  attributeNameInParentClass;
    private boolean					  containsSearchEnabled;
    private String                    defaultSortingField;
    private boolean                   descendingSortable;
    private ApplicationDataTableConfiguration       applicationDataTableConfiguration;
    private List<AccordianList>       accordianList;
    private boolean                   isUploadAvailable;
    private boolean                   isFormatDownloadAvailable;
    
    public boolean getDisableMasterCreation() {
        return disableMasterCreation;
    }

    public void setDisableMasterCreation(boolean disableMasterCreation) {
        this.disableMasterCreation = disableMasterCreation;
    }

    public String getJspName() {
        return jspName;
    }

    public void setJspName(String jspName) {
        this.jspName = jspName;
    }

    public String getRecordUrl() {
        return recordUrl;
    }

    public void setRecordUrl(String recordUrl) {
        this.recordUrl = recordUrl;
    }

    public MasterEntityConfiguration getMasterEntityConfiguration() {
        return masterEntityConfiguration;
    }

    public void setMasterEntityConfiguration(MasterEntityConfiguration masterEntityConfiguration) {
        this.masterEntityConfiguration = masterEntityConfiguration;
    }

    public String getSpringBeanName() {
        return springBeanName;
    }

    public void setSpringBeanName(String springBeanName) {
        this.springBeanName = springBeanName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<ColumnConfiguration> getColumnConfigurationList() {
        return columnConfigurationList;
    }

    public void setColumnConfigurationList(List<ColumnConfiguration> columnConfigurationList) {
        this.columnConfigurationList = columnConfigurationList;
    }

    public List<ActionConfiguration> getActionConfigurationList() {
        return actionConfigurationList;
    }

    public void setActionConfigurationList(List<ActionConfiguration> actionConfigurationList) {
        this.actionConfigurationList = actionConfigurationList;
    }

    public List<String> getInitializingPath() {
        return initializingPath;
    }

    public void setInitializingPath(List<String> initializingPath) {
        this.initializingPath = initializingPath;
    }

    public String getEntityNameMRKey() {
        return entityNameMRKey;
    }

    public void setEntityNameMRKey(String entityNameMRKey) {
        this.entityNameMRKey = entityNameMRKey;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }

    public Boolean getProcessingType() {
        return processingType;
    }

    public void setProcessingType(Boolean processingType) {
        this.processingType = processingType;
    }

    /**
     * @return the hrefBool
     */
    public Boolean getHrefBool() {
        return hrefBool;
    }

    /**
     * @param hrefBool the hrefBool to set
     */
    public void setHrefBool(Boolean hrefBool) {
        this.hrefBool = hrefBool;
    }

    public boolean isRemoveCommonActions() {
        return removeCommonActions;
    }

    public void setRemoveCommonActions(boolean removeCommonActions) {
        this.removeCommonActions = removeCommonActions;
    }

    public Boolean getIsPercentage() {
        return isPercentage;
    }

    public void setIsPercentage(Boolean isPercentage) {
        this.isPercentage = isPercentage;
    }

    /**
     * @return the sortDirection
     */
    public String getSortDirection() {
        return sortDirection;
    }

    /**
     * @param sortDirection the sortDirection to set
     */
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    /**
     * @return the defaultSortable
     */
    public String getDefaultSortable() {
        return defaultSortable;
    }

    /**
     * @param defaultSortable the defaultSortable to set
     */
    public void setDefaultSortable(String defaultSortable) {
        this.defaultSortable = defaultSortable;
    }

	public Integer getMinCharToBeginSearch() {
		return minCharToBeginSearch;
	}

	public void setMinCharToBeginSearch(Integer minCharToBeginSearch) {
		this.minCharToBeginSearch = minCharToBeginSearch;
	}

	public String getAttributeNameInParentClass() {
		return attributeNameInParentClass;
	}

	public void setAttributeNameInParentClass(String attributeNameInParentClass) {
		this.attributeNameInParentClass = attributeNameInParentClass;
	}

	public boolean isContainsSearchEnabled() {
		return containsSearchEnabled;
	}

	public void setContainsSearchEnabled(boolean containsSearchEnabled) {
		this.containsSearchEnabled = containsSearchEnabled;
	}

    public String getDefaultSortingField() {
        return defaultSortingField;
    }

    public void setDefaultSortingField(String defaultSortingField) {
        this.defaultSortingField = defaultSortingField;
    }

    public boolean isDescendingSortable() {
        return descendingSortable;
    }

    public void setDescendingSortable(boolean descendingSortable) {
        this.descendingSortable = descendingSortable;
    }

    public ApplicationDataTableConfiguration getApplicationDataTableConfiguration() {
        return applicationDataTableConfiguration;
    }

    public void setApplicationDataTableConfiguration(ApplicationDataTableConfiguration applicationDataTableConfiguration) {
        this.applicationDataTableConfiguration = applicationDataTableConfiguration;
    }

    public List<AccordianHeaders> getAccordianHeadersList() {
        return accordianHeadersList;
    }

    public void setAccordianHeadersList(List<AccordianHeaders> accordianHeadersList) {
        this.accordianHeadersList = accordianHeadersList;
    }

    public List<AccordianList> getAccordianList() {
        return accordianList;
    }

    public void setAccordianList(List<AccordianList> accordianList) {
        this.accordianList = accordianList;
    }

	public boolean isUploadAvailable() {
		return isUploadAvailable;
	}

	public void setUploadAvailable(boolean isUploadAvailable) {
		this.isUploadAvailable = isUploadAvailable;
	}
	public boolean isFormatDownloadAvailable() {
		return isFormatDownloadAvailable;
	}

	public void setFormatDownloadAvailable(boolean isFormatDownloadAvailable) {
		this.isFormatDownloadAvailable = isFormatDownloadAvailable;
	}	
	
}



