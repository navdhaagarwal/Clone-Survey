package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Cacheable
@Table(name="COM_COMM_PWD_DECPTN_MST")
@Synonym(grant="ALL")
public class PasswordDecryptionText extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String code;
	
	private String text;
	
    @ManyToOne
    @JoinColumn(name="SOURCE_PRODUCT_ID")
	private SourceProduct sysName;
	    
	    
	public  SourceProduct getSysName() {
	          return sysName;
	   }

	public void setSysName(SourceProduct sysName) {
		  this.sysName = sysName;
	   }

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	
}
