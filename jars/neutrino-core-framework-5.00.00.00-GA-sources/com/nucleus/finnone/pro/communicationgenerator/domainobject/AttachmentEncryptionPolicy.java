package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.SourceProduct;
/**
 * 
 * @author gajendra.jatav
 *
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="COM_COMM_ATT_ENCPT_POLICY_MST")
@Cacheable
@Synonym(grant="ALL")
public class AttachmentEncryptionPolicy extends BaseEntity{

    private static final long serialVersionUID = 1L;
    
    public static final String PSWD_DECRPT_PLACEHOLDER="PASSWORD_DECRYPTION_TEXT";
    
    public static final String DEFAULT_PASS_PROVIDER="neutrinoCommPasswordProvider";
    
    /**
     * communicationParamCodeList is list of comma separated communication codes 
     */
    private String passwordExpression;
    
    private String name;
    
    private String description;
    
    
    private String code;
    
    @OneToOne
    private PasswordDecryptionText       passwordDecryptionText;
    
    @ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")
	private SourceProduct sysName;
	    
	    
	public  SourceProduct getSysName() {
	          return sysName;
	   }

	public void setSysName(SourceProduct sysName) {
		  this.sysName = sysName;
	   }

    
    public PasswordDecryptionText getPasswordDecryptionText() {
		return passwordDecryptionText;
	}

	public void setPasswordDecryptionText(
			PasswordDecryptionText passwordDecryptionText) {
		this.passwordDecryptionText = passwordDecryptionText;
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

	private String passwordProviderBean;
    
    
    public String getPasswordExpression() {
        return passwordExpression;
    }

    public void setPasswordExpression(String passwordExpression) {
        this.passwordExpression = passwordExpression;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPasswordProviderBean() {
        if(passwordProviderBean==null)
        {
            return DEFAULT_PASS_PROVIDER;
        }
        return passwordProviderBean;
    }

    public void setPasswordProviderBean(String passwordProviderBean) {
        this.passwordProviderBean = passwordProviderBean;
    }
    
    
    
}
