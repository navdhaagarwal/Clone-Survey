package com.nucleus.finnone.pro.lov;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(name = "LOV_COLUMN_CONFIG")
public class LovColumnConfig  extends BaseEntity {

	@Column(name="COL_TITLE_LABEL")
	private String titleKey;

	@Column(name="COL_WIDTH")
	private String width;

	@Column(name="IS_SORTABLE")
	private Boolean sortable;

	@Column(name="SORT_ORDER")
	private Integer sortOrder;

	@Column(name="IS_SEARCHABLE")
	private Boolean searchable;

	@Column(name="IS_HIDDEN")
	private Boolean hidden;

	@Column(name="DATA_FIELD")
	private String dataField;

	@Column(name="DATA_FIELD_SIMPLENAME")
	private String dataFieldSimpleName;

	@Column(name="COLUMN_TYPE")
	private String columnType;

	@Column(name="IS_PERCENTAGE")
	private Boolean isPercentage;


	@Column(name="LOV_CONFIGID")
	private Long                lovConfigId;

	public Boolean getPercentage() {
		return isPercentage;
	}

	public void setPercentage(Boolean percentage) {
		isPercentage = percentage;
	}

	public Long getLovConfigId() {
		return lovConfigId;
	}

	public void setLovConfigId(Long lovConfigId) {
		this.lovConfigId = lovConfigId;
	}

	public String getTitleKey() {
		return titleKey;
	}
	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public Boolean getSortable() {
		return sortable;
	}
	public void setSortable(Boolean sortable) {
		this.sortable = sortable;
	}
	public Boolean getSearchable() {
		return searchable;
	}
	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}
	public Boolean getHidden() {
		return hidden;
	}
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	public String getDataField() {
		return dataField;
	}
	public void setDataField(String dataField) {
		this.dataField = dataField;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public Boolean getIsPercentage() {
		return isPercentage;
	}
	public void setIsPercentage(Boolean isPercentage) {
		this.isPercentage = isPercentage;
	}
	/**
	 * @return the dataFieldSimpleName
	 */
	public String getDataFieldSimpleName() {
		return dataFieldSimpleName;
	}
	/**
	 * @param dataFieldSimpleName the dataFieldSimpleName to set
	 */
	public void setDataFieldSimpleName(String dataFieldSimpleName) {
		this.dataFieldSimpleName = dataFieldSimpleName;
	}
	public Integer getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
	
}
