package com.nucleus.authority;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.SourceProduct;

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="ALL")
public class Authority extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // ==========================================================================
    // Instance Attributes and Methods
    // ==========================================================================
    private String            name;

    
    private String            description;
    

    
    @ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID", insertable=false, updatable=false)
    private SourceProduct sysName;

    @Column(name="SOURCE_PRODUCT_ID")
    private Long sourceProductId;

    public Long getSourceProductId() {
        return sourceProductId;
    }

    public void setSourceProductId(Long sourceProductId) {
        this.sourceProductId = sourceProductId;
    }
    
    
    public  SourceProduct getSysName() {
	 return sysName;
     }

    public void setSysName(SourceProduct sysName) {
	this.sysName = sysName;
    }

	@Column(unique = true)
    private String            authCode;
    
    /*
     * 0 or null is lowest degree of access
     */
    
    @Column(name="DEGREE_OF_ACCESS")
    private Integer degreeOfAccess;
    
    
    public int getDegreeOfAccess() {
		return degreeOfAccess;
	}

	public void setDegreeOfAccess(Integer degreeOfAccess) {
		this.degreeOfAccess = degreeOfAccess;
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

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getAuthority() {
        return authCode;
    }

    
    @Override
    public void loadLazyFields()
    {
    	super.loadLazyFields();
    	if(getSysName()!=null)
    	{
    		getSysName().loadLazyFields();
    	}
    }

}
