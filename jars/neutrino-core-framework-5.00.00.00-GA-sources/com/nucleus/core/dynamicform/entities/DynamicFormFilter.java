package com.nucleus.core.dynamicform.entities;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;
/*
 * @author gajendra.jatav
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="DYNAMIC_FORM_FILTER_MST")
@Cacheable
@Synonym(grant="SELECT")
@DeletionPreValidator
public class DynamicFormFilter extends BaseMasterEntity{

	private static final long serialVersionUID = 1L;

	private String name;
	
	
	private String description;
	
	/**
	 * Captures mata information of mapped dynamic forms in below format
	 * { formName1 :{ fields: [],uiMetaDataId:idOfuiMetaData },formName2:{ fields:[],uiMetaDataId:idOfuiMetaData}}
	 * 
	 */
	@Column(length = 4000)
	private String filterFieldsJsonMap;
	
	private Boolean isShareable;
	
	@OneToOne
	@JoinColumn(name="UI_META_DATA_ID")
	private UIMetaData uiMetaData;

    @ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")	
	private SourceProduct sourceProduct;  
    
    public static final String FORM_FIELDS="fields";
    
    
    public static final String PANEL_KEY_LIST="$__panelKeyList";
    
    
    public static final String TABLE_KEY_LIST="$__tableKeyList";
    
    
    public static final String UIMETADATA_ID="uiMetaDataId";

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DynamicFormFilter dynamicFormFilter=(DynamicFormFilter)baseEntity;
        super.populate(dynamicFormFilter, cloneOptions);
        dynamicFormFilter.setName(name);
        dynamicFormFilter.setDescription(description);
        dynamicFormFilter.setFilterFieldsJsonMap(filterFieldsJsonMap);
        dynamicFormFilter.setIsShareable(isShareable);
        dynamicFormFilter.setUiMetaData(uiMetaData);
        dynamicFormFilter.setSourceProduct(sourceProduct);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	 DynamicFormFilter dynamicFormFilter=(DynamicFormFilter)baseEntity;
    	 super.populateFrom(dynamicFormFilter, cloneOptions);
         this.setName(dynamicFormFilter.getName());
         this.setDescription(dynamicFormFilter.getDescription());
         this.setFilterFieldsJsonMap(dynamicFormFilter.getFilterFieldsJsonMap());
         this.setIsShareable(dynamicFormFilter.getIsShareable());
         this.setUiMetaData(dynamicFormFilter.getUiMetaData());
         this.setSourceProduct(dynamicFormFilter.getSourceProduct());
    }
    
	public Boolean getIsShareable() {
		if(isShareable==null)
			return false;
		
		return isShareable;
	}


	public void setIsShareable(Boolean isShareable) {
		this.isShareable = isShareable;
	}

	public SourceProduct getSourceProduct() {
		return sourceProduct;
	}

	public void setSourceProduct(SourceProduct sourceProduct) {
		this.sourceProduct = sourceProduct;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFilterFieldsJsonMap() {
		return filterFieldsJsonMap;
	}

	public void setFilterFieldsJsonMap(String filterFieldsJsonMap) {
		this.filterFieldsJsonMap = filterFieldsJsonMap;
	}

	public UIMetaData getUiMetaData() {
		return uiMetaData;
	}

	public void setUiMetaData(UIMetaData uiMetaData) {
		this.uiMetaData = uiMetaData;
	}
	
	@Override
    public String getDisplayName() {
        return name;
    }

	
	
	
}
