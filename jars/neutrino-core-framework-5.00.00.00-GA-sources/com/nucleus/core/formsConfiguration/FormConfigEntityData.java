package com.nucleus.core.formsConfiguration;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
@NamedQuery(name="fetchFormConfigEntityDataByBinderName",query = "Select formConfigEntityData FROM FormConfigEntityData formConfigEntityData WHERE formConfigEntityData.webDataBinderName = :webDataBinderName")
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class FormConfigEntityData extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String            entityName;

    private String            packageName;

    private String            itemLabel;

    private String            itemValue;

    /**
     * for setting value of binder Name as registeres in cas web data binder
     */
    private String            webDataBinderName;

    /** 
     * The comma separated columns for autocomplete tag. */
    private String            commaSeparatedColumns;

    /**
     * @return the entityName
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private FormConfigEntityData parent;
    
    /**
     *  comma separated Parent Entity Columns along with required field.
     */
    private String parentColumns;
    
    private String url;
    
    public String getEntityName() {
        return entityName;
    }

    /**
     * @param entityName the entityName to set
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @param packageName the packageName to set
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * @return the itemLabel
     */
    public String getItemLabel() {
        return itemLabel;
    }

    /**
     * @param itemLabel the itemLabel to set
     */
    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    /**
     * @return the itemValue
     */
    public String getItemValue() {
        return itemValue;
    }

    /**
     * @param itemValue the itemValue to set
     */
    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    /**
     * @return
     */
    public String getWebDataBinderName() {
        return webDataBinderName;
    }

    /**
     * @param webDataBinderName
     */
    public void setWebDataBinderName(String webDataBinderName) {
        this.webDataBinderName = webDataBinderName;
    }

    public String getCommaSeparatedColumns() {
        return commaSeparatedColumns;
    }

    public void setCommaSeparatedColumns(String commaSeparatedColumns) {
        this.commaSeparatedColumns = commaSeparatedColumns;
    }

	public FormConfigEntityData getParent() {
		return parent;
	}

	public void setParent(FormConfigEntityData parent) {
		this.parent = parent;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParentColumns() {
		return parentColumns;
	}

	public void setParentColumns(String parentColumns) {
		this.parentColumns = parentColumns;
	}
    

}
