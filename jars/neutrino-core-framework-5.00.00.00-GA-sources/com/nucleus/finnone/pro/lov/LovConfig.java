package com.nucleus.finnone.pro.lov;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@NamedQueries({
		@NamedQuery(name="lovConfig.fetchAllLovKeys",query = "Select lovConfig.key FROM LovConfig lovConfig"),
		@NamedQuery(name="lovConfig.fetchLovConfigForLovKey",query = "Select lovConfig FROM LovConfig lovConfig where lovConfig.key = :lovKey")
})
@Table(name = "LOV_CONFIG_MST")
public class LovConfig extends BaseEntity {

	@Column(name="LOV_KEY")
	private String key;

	@Column(name="ENTITY_CLASS")
	private String entityClass;

	@Column(name="SERVICE_BEAN_NAME")
	private String serviceBeanName;

	@Column(name="SERVICE_INTERFACE_NAME")
	private String serviceInterfaceName;

	@Column(name="SERVICE_OPERATION_NAME")
	private String serviceOperationName;

	@Column(name="VISIBLE_COLUMN")
	private String visibleColumn;

	@Column(name="LOV_TITLE_LABEL")
	private String lovTitleKey;

	@Column(name="HIDDEN_COLUMN")
	private String hiddenColumn;

	@Column(name="SEARCH_INPUT_LABEL")
	private String lovSearchInputLabel;

	@Column(name="SEARCH_TOOLTIP_LABEL")
	private String lovSearchInputTooltip;

	@Column(name="SEARCH_PLACEHOLDER_LABEL")
	private String lovSearchInputPlaceHolder;

	@Column(name="VISIBLE_COL_SIMPLENAME")
	private String visibleColumnSimpleName;

	@Column(name="HIDDEN_COL_SIMPLENAME")
	private String hiddenColumnSimpleName;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="LOV_CONFIGID", referencedColumnName = "ID", insertable=false, updatable = false)
	private List<LovColumnConfig> columnNameList;
	
	public String getServiceInterfaceName() {
		return serviceInterfaceName;
	}
	public void setServiceInterfaceName(String serviceInterfaceName) {
		this.serviceInterfaceName = serviceInterfaceName;
	}
	public String getServiceOperationName() {
		return serviceOperationName;
	}
	public void setServiceOperationName(String serviceOperationName) {
		this.serviceOperationName = serviceOperationName;
	}

	public String getLovSearchInputLabel() {
		return lovSearchInputLabel;
	}
	public void setLovSearchInputLabel(String lovSearchInputLabel) {
		this.lovSearchInputLabel = lovSearchInputLabel;
	}
	public String getLovSearchInputTooltip() {
		return lovSearchInputTooltip;
	}
	public void setLovSearchInputTooltip(String lovSearchInputTooltip) {
		this.lovSearchInputTooltip = lovSearchInputTooltip;
	}
	public String getLovSearchInputPlaceHolder() {
		return lovSearchInputPlaceHolder;
	}
	public void setLovSearchInputPlaceHolder(String lovSearchInputPlaceHolder) {
		this.lovSearchInputPlaceHolder = lovSearchInputPlaceHolder;
	}
	
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getEntityClass() {
		return entityClass;
	}
	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}
	public String getServiceBeanName() {
		return serviceBeanName;
	}
	public void setServiceBeanName(String serviceBeanName) {
		this.serviceBeanName = serviceBeanName;
	}
	public List<LovColumnConfig> getColumnNameList() {
		return columnNameList;
	}
	public void setColumnNameList(List<LovColumnConfig> columnNameList) {
		this.columnNameList = columnNameList;
	}
	public String getVisibleColumn() {
		return visibleColumn;
	}
	public void setVisibleColumn(String visibleColumn) {
		this.visibleColumn = visibleColumn;
	}
	public String getHiddenColumn() {
		return hiddenColumn;
	}
	public void setHiddenColumn(String hiddenColumn) {
		this.hiddenColumn = hiddenColumn;
	}
	public String getLovTitleKey() {
		return lovTitleKey;
	}
	public void setLovTitleKey(String lovTitleKey) {
		this.lovTitleKey = lovTitleKey;
	}
	/**
	 * @return the visibleColumnSimpleName
	 */
	public String getVisibleColumnSimpleName() {
		return visibleColumnSimpleName;
	}
	/**
	 * @param visibleColumnSimpleName the visibleColumnSimpleName to set
	 */
	public void setVisibleColumnSimpleName(String visibleColumnSimpleName) {
		this.visibleColumnSimpleName = visibleColumnSimpleName;
	}
	/**
	 * @return the hiddenColumnSimpleName
	 */
	public String getHiddenColumnSimpleName() {
		return hiddenColumnSimpleName;
	}
	/**
	 * @param hiddenColumnSimpleName the hiddenColumnSimpleName to set
	 */
	public void setHiddenColumnSimpleName(String hiddenColumnSimpleName) {
		this.hiddenColumnSimpleName = hiddenColumnSimpleName;
	}
	
	
	
}
