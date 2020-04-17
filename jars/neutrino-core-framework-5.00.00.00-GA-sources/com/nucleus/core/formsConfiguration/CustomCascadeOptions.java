package com.nucleus.core.formsConfiguration;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class CustomCascadeOptions extends BaseEntity{

	private static final long serialVersionUID = 1L;
	
	private String parentIdList;
	private String childIdList;
	
	
	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		CustomCascadeOptions parentChildField=(CustomCascadeOptions)baseEntity;
        super.populate(parentChildField, cloneOptions);
        parentChildField.setChildIdList(childIdList);
        parentChildField.setParentIdList(parentIdList);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	CustomCascadeOptions parentChildField=(CustomCascadeOptions)baseEntity;
    	 super.populateFrom(parentChildField, cloneOptions);
    	 this.setChildIdList(parentChildField.getChildIdList());
    	 this.setParentIdList(parentChildField.getParentIdList());
    }

	public String getParentIdList() {
		return parentIdList;
	}

	public void setParentIdList(String parentIdList) {
		this.parentIdList = parentIdList;
	}

	public String getChildIdList() {
		return childIdList;
	}

	public void setChildIdList(String childIdList) {
		this.childIdList = childIdList;
	}
	
	
	

}
