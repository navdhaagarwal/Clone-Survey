/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd
Description: Service interface for Additional Field
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.serviceinterface;

import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataMetaData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataTemp;
import com.nucleus.finnone.pro.additionaldata.domainobject.ServiceRequestTransactionType;

public interface IAdditionalDataService {
	/** 
	 *  to get Additional Field Meta Data on the basis of TRansaction Type
	 * @return 
	 * @param _transactionType 
	 */
	List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionType(String transactionType);
	
	/** 
	 *  to get Additional Field Meta Data on the basis of TRansaction Type ID
	 * @return 
	 * @param _transactionTypeId 
	 */
	List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeId(Long transactionTypeId);

	/** 
	 *  to get Additional Fieldvalues on the basis of Primary Key
	 * @return 
	 * @param additionalDataId 
	 */
	AdditionalData getAdditionalData(Long additionalDataId);
	
	/** 
	 *  to get Additional Field values from temp table on the basis of Primary Key
	 * @return 
	 * @param tempAdditionalDataId 
	 */
	AdditionalDataTemp getTempAdditionalData(Long tempAdditionalDataId);
	
	List<ServiceRequestTransactionType> getAdditionalDataTransactionType(Long serviceOrTxnTypeId);
	
	List<Map<String,String>> getMastersInfo();
	
	/** 
	 *  to get Additional Field Meta Data on the basis of TRansaction Type ID
	 * @return 
	 * @param _transactionTypeId 
	 */
	List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeForDisplayOrder(Long transactionTypeId);

	/**
	 * used to find Service Request Transaction Type
	 * @param serviceReqTxnTypeId
	 * @return
	 */
	ServiceRequestTransactionType findServiceRequestTransactionType(Long serviceReqTxnTypeId);
	List<ServiceRequestTransactionType> getTransactionTypeDetails();

	void deleteAdditionalDataAndMoveToHistory(AdditionalData additionalData);
}