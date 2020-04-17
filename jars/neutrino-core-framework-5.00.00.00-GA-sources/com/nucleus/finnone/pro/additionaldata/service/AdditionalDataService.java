/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd
Description: Service for Additional Field 
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.service;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.additionaldata.businessobject.IAdditionalDataBusinessObject;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataMetaData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataTemp;
import com.nucleus.finnone.pro.additionaldata.domainobject.ServiceRequestTransactionType;
import com.nucleus.finnone.pro.additionaldata.serviceinterface.IAdditionalDataService;

@Named("additionalDataService")
public class AdditionalDataService implements IAdditionalDataService{
	
	@Inject
	@Named("additionalDataBusinessObject")
	private IAdditionalDataBusinessObject additionalDataBusinessObject;
	/** 
	 *  to get Additional Field Meta Data on the basis of TRansaction Type
	 * @return 
	 * @param _transactionType 
	 */
	
	@Transactional(readOnly=true)
	public List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionType(String transactionType) {
		List<AdditionalDataMetaData> additionalDataMetaDataList = additionalDataBusinessObject.getAdditionalDataMetaDataByTransactionType(transactionType);
		for(AdditionalDataMetaData additionalDataMetaData: additionalDataMetaDataList)
		{
			additionalDataMetaData.getCustomFieldDataTypeObject().getCode();
		}
		return additionalDataMetaDataList;
	}
	
	@Transactional(readOnly=true)
	public List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeId(Long transactionTypeId) {
		List<AdditionalDataMetaData> additionalDataMetaDataList = additionalDataBusinessObject.getAdditionalDataMetaDataByTransactionTypeId(transactionTypeId);
		for(AdditionalDataMetaData additionalDataMetaData: additionalDataMetaDataList)
		{
			additionalDataMetaData.getCustomFieldDataTypeObject().getCode();
		}
		return additionalDataMetaDataList;
	}

	/** 
	 *  to get Additional Fieldvalues on the basis of Primary Key
	 * @return 
	 * @param additionalDataId 
	 */
	public AdditionalData getAdditionalData(Long additionalDataId) {
		return additionalDataBusinessObject.getAdditionalData(additionalDataId);
	}

	/** 
	 *  to get Additional Field values from temp table on the basis of Primary Key
	 * @return 
	 * @param tempAdditionalDataId 
	 */
	public AdditionalDataTemp getTempAdditionalData(Long tempAdditionalDataId) {
		return additionalDataBusinessObject.getTempAdditionalData(tempAdditionalDataId);
	}
	
	@Transactional(readOnly=true)
	public List<ServiceRequestTransactionType> getAdditionalDataTransactionType(Long serviceOrTxnTypeId) {
		return additionalDataBusinessObject.getAdditionalDataTransactionType(serviceOrTxnTypeId);
	}
	
	  public List<Map<String,String>> getMastersInfo()
	  {
		  return additionalDataBusinessObject.getMastersInfo();
	  }	 
	  
	  @Transactional(readOnly=true)
		public List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeForDisplayOrder(Long transactionTypeId) {
			List<AdditionalDataMetaData> additionalDataMetaDataList = additionalDataBusinessObject.getAdditionalDataMetaDataByTransactionTypeForDisplayOrder(transactionTypeId);
			for(AdditionalDataMetaData additionalDataMetaData: additionalDataMetaDataList)
			{
				additionalDataMetaData.getCustomFieldDataTypeObject().getCode();
			}
			return additionalDataMetaDataList;
		}
	  
	  	/**
		 * used to find Service Request Transaction Type
		 * @param serviceReqTxnTypeId
		 * @return
		 */
	  @Transactional(readOnly=true)
		public ServiceRequestTransactionType findServiceRequestTransactionType(Long serviceReqTxnTypeId) {
		  return additionalDataBusinessObject.findServiceRequestTransactionType(serviceReqTxnTypeId);
		}

		@Override
		public List<ServiceRequestTransactionType> getTransactionTypeDetails() {
			return additionalDataBusinessObject.getTransactionTypeDetails();
		}
	  
		@Transactional(readOnly=true)
		public void deleteAdditionalDataAndMoveToHistory(AdditionalData additionalData) {
		  additionalDataBusinessObject.deleteAdditionalDataAndMoveToHistory(additionalData);
		}
}