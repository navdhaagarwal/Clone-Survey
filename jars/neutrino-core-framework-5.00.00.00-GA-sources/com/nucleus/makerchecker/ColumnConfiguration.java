/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.makerchecker;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
/**
 * @author Nucleus Software Exports Limited TODO -> amit.parashar Add
 *         documentation to class
 */
public class ColumnConfiguration {

	private String titleKey;

	private String width;

	private Boolean sortable;

	private Boolean searchable;

	private Boolean hidden;

	private String dataField;
	
	private String regionalDataField;

	private String columnType;

	private Boolean isPercentage;

	private String columnCSS;
	
	private String columnClickFunction;
	
	private Boolean isRegional=Boolean.FALSE;

	private Boolean defaultSearch;
	
	private String authority;

	private String displayName;

	private Boolean clickable;

	private Boolean expandable;

	private Boolean toolTip;

	private Integer columnOrder;

	private String filterBoxUIComponent;

	private String filterAttribute;

	private String className;

	private String searchableEntityFields;

	public String getSearchableEntityFields() {
		return searchableEntityFields;
	}

	public void setSearchableEntityFields(String searchableEntityFields) {
		this.searchableEntityFields = searchableEntityFields;
	}

	public Boolean getDefaultSearch() {
		return defaultSearch;
	}

	public void setDefaultSearch(Boolean defaultSearch) {
		this.defaultSearch = defaultSearch;
	}

	/**
	 * @return the titleKey
	 */
	public String getTitleKey() {
		return titleKey;
	}

	/**
	 * @param titleKey
	 *            the titleKey to set
	 */
	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}

	/**
	 * @return the width
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(String width) {
		this.width = width;
	}

	/**
	 * @return the sortable
	 */
	public Boolean getSortable() {
		return sortable;
	}

	/**
	 * @param sortable
	 *            the sortable to set
	 */
	public void setSortable(Boolean sortable) {
		this.sortable = sortable;
	}

	/**
	 * @return the searchable
	 */
	public Boolean getSearchable() {
		return searchable;
	}

	/**
	 * @param searchable
	 *            the searchable to set
	 */
	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}

	/**
	 * @return the hidden
	 */
	public Boolean getHidden() {
		return hidden;
	}

	/**
	 * @param hidden
	 *            the hidden to set
	 */
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return the dataField
	 */
	public String getDataField() {
		return dataField;
	}

	/**
	 * @param dataField
	 *            the dataField to set
	 */
	public void setDataField(String dataField) {
		this.dataField = dataField;
	}

	/**
	 * @return the columnType
	 */
	public String getColumnType() {
		return columnType;
	}

	/**
	 * @param columnType
	 *            the columnType to set
	 */
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	/**
	 * @return the isPercentage
	 */
	public Boolean getIsPercentage() {
		return isPercentage;
	}

	/**
	 * @param isPercentage
	 *            the isPercentage to set
	 */
	public void setIsPercentage(Boolean isPercentage) {
		this.isPercentage = isPercentage;
	}

	/**
	 * @return the columnCSS
	 */
	public String getColumnCSS() {
		return columnCSS;
	}

	/**
	 * @param columnCSS the columnCSS to set
	 */
	public void setColumnCSS(String columnCSS) {
		this.columnCSS = columnCSS;
	}

	public String getRegionalDataField() {
		
		return regionalDataField;
	}

	public void setRegionalDataField(String regionalDataField) {
		this.regionalDataField = regionalDataField;
	}
	
	
	 public Boolean getIsRegional() {
		 if(isNull(isRegional)){
			 isRegional=Boolean.FALSE;
		 }
		return isRegional;
	 }

	 public void setIsRegional(Boolean regional) {
		this.isRegional = regional;
	  }

	public String getColumnClickFunction() {
		return columnClickFunction;
	}

	public void setColumnClickFunction(String columnClickFunction) {
		this.columnClickFunction = columnClickFunction;
	}

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Boolean getClickable() {
		return clickable;
	}

	public void setClickable(Boolean clickable) {
		this.clickable = clickable;
	}

	public Boolean getExpandable() {
		return expandable;
	}

	public void setExpandable(Boolean expandable) {
		this.expandable = expandable;
	}

	public Integer getColumnOrder() {
		return columnOrder;
	}

	public void setColumnOrder(Integer columnOrder) {
		this.columnOrder = columnOrder;
	}

	public Boolean getToolTip() {
		return toolTip;
	}

	public void setToolTip(Boolean toolTip) {
		this.toolTip = toolTip;
	}

	public String getFilterBoxUIComponent() {
		return filterBoxUIComponent;
	}

	public void setFilterBoxUIComponent(String filterBoxUIComponent) {
		this.filterBoxUIComponent = filterBoxUIComponent;
	}

	public String getFilterAttribute() {
		return filterAttribute;
	}

	public void setFilterAttribute(String filterAttribute) {
		this.filterAttribute = filterAttribute;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
