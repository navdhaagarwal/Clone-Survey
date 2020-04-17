/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd
Description: Entity for Additional Field Transaction Data
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.domainobject;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.finnone.pro.additionaldata.constants.AdditionalDataConstants;
import com.nucleus.logging.BaseLoggers;

@Entity(name="AdditionalData")
@DynamicInsert 
@DynamicUpdate
@Table(name = "TRANS_ADDL_FIELD_DTL")
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS) 
@Synonym(grant="ALL")
public class AdditionalData extends AdditionalDataBasic{
	
	@Transient
	private static final long serialVersionUID = 1L;
		public AdditionalData() {
		}
		
		public AdditionalData(AdditionalDataBasic additionalData) {
			super(additionalData);
		}
	
		
     public boolean isEmpty(){
    	 boolean emptyField = true;
    	 String fieldName="";
    	 String fieldVlaue="";
    	 try {
			for(int i=1; i<=AdditionalDataConstants.NUMBER_OF_ADDL_FIELDS;i++){
				 fieldName = "additionalField"+i;
				 fieldVlaue = (String)PropertyUtils.getProperty(this, fieldName);
				 if(fieldVlaue!=null && !fieldVlaue.equals("")){
					 emptyField = false;
					 break;
				 }
			 }
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("isEmpty",e);
		/*	Message validationMessage = new Message(AdditionalDataConstants.ADDL_DATA_GETVAL_EXCEPTION,Message.MessageType.ERROR,fieldName);
			throw ExceptionBuilder.getInstance(BusinessException.class, AdditionalDataConstants.ADDL_DATA_GETVAL_EXCEPTION, "Error in getting value of Additional Field").setMessage(validationMessage).build();*/
		} 
    	   return emptyField;
    	 
     }
}