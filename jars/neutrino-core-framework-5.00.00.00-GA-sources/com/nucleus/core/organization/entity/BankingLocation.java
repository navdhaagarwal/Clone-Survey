package com.nucleus.core.organization.entity;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.cas.parentChildDeletionHandling.DeletionPreValidator;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "COM_BANKING_LOC_MST")
@Cacheable
@AttributeOverride(name = "id", column = @Column(name = "ID"))
@Synonym(grant="SELECT,REFERENCES")
@DeletionPreValidator
public class BankingLocation extends BaseMasterEntity implements Serializable {

    private static final long serialVersionUID = -2004208469L;

    public BankingLocation() {
        super();
    }

    public BankingLocation(Long id) {
        super.setId(id);
    }

    @Column(name = "NAME", length = 100)
    private String    name;

    @Column(name = "CODE", length = 3)
    private String    code;

    @Column(name = "MICR_CODE", length = 3)
    private String    micrCode;
    
    @Column(name = "IFSC_CODE")
    private String    ifscCode;

	@Column(name = "MICR_NON_MICR")
    private Character micrNonMICR;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMicrCode() {
        return micrCode;
    }

    public void setMicrCode(String micrCode) {
        this.micrCode = micrCode;
    }

    public Character getMicrNonMICR() {
        return micrNonMICR;
    }

    public void setMicrNonMICR(Character micrNonMICR) {
        this.micrNonMICR = micrNonMICR;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public String getIfscCode() {
  		return ifscCode;
  	}

  	public void setIfscCode(String ifscCode) {
  		this.ifscCode = ifscCode;
  	}

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        BankingLocation bankingLocation = (BankingLocation) baseEntity;
        super.populate(bankingLocation, cloneOptions);
        bankingLocation.setCode(this.code);
        bankingLocation.setMicrCode(this.micrCode);
        bankingLocation.setMicrNonMICR(this.micrNonMICR);
        bankingLocation.setName(this.name);
		bankingLocation.setTenantId(this.getTenantId());
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        BankingLocation bankingLocation = (BankingLocation) baseEntity;
        super.populateFrom(bankingLocation, cloneOptions);
        setCode(bankingLocation.getCode());
        setMicrCode(bankingLocation.getMicrCode());
        setMicrNonMICR(bankingLocation.getMicrNonMICR());
        setName(bankingLocation.getName());
		setTenantId(bankingLocation.getTenantId());
    }
    
    @Override
    public String getDisplayName() {
        return getName();
    }
}
