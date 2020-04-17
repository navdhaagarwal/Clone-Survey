/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd
Description: Entity for Additional Field Transaction History Data
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.domainobject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;

@Entity 
@DynamicInsert 
@DynamicUpdate
@Table(name = "TRANS_ADDL_FIELD_HST")
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS) 
@Synonym(grant="ALL")
public class AdditionalDataHistory extends AdditionalDataBasic {
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Column(name = "ADDL_FIELD_TXN_ID", nullable = false, columnDefinition = "Numeric(16)")
	private Long historyId;
	
	
	public Long getHistoryId() {
		return historyId;
	}

	public void setHistoryId(Long historyId) {
		this.historyId = historyId;
	}

	
	public AdditionalDataHistory() {
	}
	public AdditionalDataHistory(AdditionalDataBasic additionalData) {
		super(additionalData);
		this.historyId = additionalData.getId();

	}
}