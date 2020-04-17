/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd
Description: DAO interface for Additional Field 
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.dao;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataHistory;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataMetaData;
import com.nucleus.finnone.pro.additionaldata.domainobject.ServiceRequestTransactionType;
import com.nucleus.persistence.EntityDao;

public interface IAdditionalDataDAO extends EntityDao {
	/** 
	 *  to get Additional Field Meta Data on the basis of TRansaction Type
	 * @return 
	 * @param _transactionType 
	 */
	List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionType(String transactionType);

	List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeId(
			Long transactionTypeId);
	List<ServiceRequestTransactionType> getAdditionalDataTransactionType(Long serviceOrTxnTypeId);
	
	List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeForDisplayOrder(
			Long transactionTypeId);
	
	
	List<Map<String,String>> getMastersInfo();
	
	/**
	 * used to find Service Request Transaction Type
	 * @param serviceReqTxnTypeId
	 * @return
	 */
	ServiceRequestTransactionType findServiceRequestTransactionType(Long serviceReqTxnTypeId);
	
	List<ServiceRequestTransactionType> getTransactionTypeDetails();
	
	void createAdditionalDataHistory(AdditionalDataHistory additionalDataHistory);
	void deleteAdditionalData(AdditionalData additionalData);
	
	
}