package com.nucleus.rules.taskAssignmentMaster;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;
import com.nucleus.master.audit.annotation.NeutrinoAuditableMaster;
import com.nucleus.rules.model.ModuleName;
import com.nucleus.rules.model.ObjectGraphTypes;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@DeletionPreValidator
@Synonym(grant="ALL")
@Table(name = "OBJECT_GRAPH_CLASS_MAPPING")
@NeutrinoAuditableMaster(identifierColumn="code")
public class ObjectGraphClassMapping extends BaseMasterEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.EAGER)
	@EmbedInAuditAsValue(displayKey="label.objectGraphMapping.objectGraph")
	private ObjectGraphTypes objectGraphType;
	
	@EmbedInAuditAsValue(displayKey="label.objectGraphMapping.package")
	private String mappedClassName;
	
	@EmbedInAuditAsValue(displayKey="label.objectGraphMapping.code")
	private String code;
	
	@EmbedInAuditAsValue(displayKey="label.objectGraphMapping.name")
	private String name;
	
	@EmbedInAuditAsValue(displayKey="label.objectGraphMapping.description")
	private String description;
	
	@EmbedInAuditAsValue(displayKey="label.objectGraphMapping.system")
	private String sourceProduct;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@EmbedInAuditAsValue(displayKey="label.objectGraphMapping.moduleName")
	private ModuleName module;
	
   @OneToOne(cascade = CascadeType.ALL)
	private ReasonsActiveInactiveMapping reasonActInactMap;

	
	public ObjectGraphTypes getObjectGraphType() {
		return objectGraphType;
	}

	public void setObjectGraphType(ObjectGraphTypes objectGraphType) {
		this.objectGraphType = objectGraphType;
	}

	public String getMappedClassName() {
		return mappedClassName;
	}

	public void setMappedClassName(String mappedClassName) {
		this.mappedClassName = mappedClassName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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

	public ModuleName getModule() {
		return module;
	}

	public void setModule(ModuleName module) {
		this.module = module;
	}
	
	public String getSourceProduct() {
		return sourceProduct;
	}

	public void setSourceProduct(String sourceProduct) {
		this.sourceProduct = sourceProduct;
	}

	public ReasonsActiveInactiveMapping getReasonActInactMap() {
		return reasonActInactMap;
	}

	public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
		this.reasonActInactMap = reasonActInactMap;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mappedClassName == null) ? 0 : mappedClassName.hashCode());
		result = prime * result
				+ ((objectGraphType == null) ? 0 : objectGraphType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectGraphClassMapping other = (ObjectGraphClassMapping) obj;
		if (mappedClassName == null) {
			if (other.mappedClassName != null)
				return false;
		} else if (!mappedClassName.equals(other.mappedClassName))
			return false;
		if (objectGraphType == null) {
			if (other.objectGraphType != null)
				return false;
		} else if (!objectGraphType.equals(other.objectGraphType))
			return false;
		return true;
	}
	
	  public String getDisplayName() {
	        return getCode();
	    }
	
	   @Override
	    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		   ObjectGraphClassMapping objectGraphMapping=(ObjectGraphClassMapping) baseEntity;
	        super.populate(objectGraphMapping, cloneOptions);
	        objectGraphMapping.setCode(code);
	        objectGraphMapping.setName(name);
	        objectGraphMapping.setDescription(description);
	        objectGraphMapping.setSourceProduct(sourceProduct);
	        objectGraphMapping.setModule(module);
	        objectGraphMapping.setObjectGraphType(objectGraphType);
	        objectGraphMapping.setMappedClassName(mappedClassName);
	        if (reasonActInactMap != null) {
	        	objectGraphMapping.setReasonActInactMap((ReasonsActiveInactiveMapping) this.reasonActInactMap.cloneYourself(cloneOptions));
	        }
	    }

	    @Override
	    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
	 	   ObjectGraphClassMapping objectGraphMapping = (ObjectGraphClassMapping) baseEntity;
	        super.populateFrom(objectGraphMapping, cloneOptions);
	        this.setCode(objectGraphMapping.getCode());
	        this.setName(objectGraphMapping.getName());
	        this.setDescription(objectGraphMapping.getDescription());
	        this.setSourceProduct(objectGraphMapping.getSourceProduct());
	        this.setModule(objectGraphMapping.getModule());
	        this.setObjectGraphType(objectGraphMapping.getObjectGraphType());
	        this.setMappedClassName(objectGraphMapping.getMappedClassName());
	        if (objectGraphMapping.getReasonActInactMap() != null) {
	            this.setReasonActInactMap((ReasonsActiveInactiveMapping) objectGraphMapping.getReasonActInactMap().cloneYourself(cloneOptions));
	        }
	    }
	
	
}
