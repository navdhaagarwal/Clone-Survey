/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd.
Description: Business Object for Addition Field implementation
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.businessobject;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.additionaldata.dao.IAdditionalDataDAO;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataHistory;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataMetaData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataTemp;
import com.nucleus.finnone.pro.additionaldata.domainobject.ServiceRequestTransactionType;

@Named("additionalDataBusinessObject")
public class AdditionalDataBusinessObject implements IAdditionalDataBusinessObject{
	
	@Inject
	@Named("additionalDataDAO")
	private IAdditionalDataDAO additionalDataDAO;
	
	/** 
	 *  to get Additional Field Meta Data on the basis of TRansaction Type
	 * @return 
	 * @param _transactionType 
	 */
	@Override
	public List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionType(String transactionType) {
		return additionalDataDAO.getAdditionalDataMetaDataByTransactionType(transactionType);
	}
	
	@Override
	public List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeId(Long transactionTypeId) {
		return additionalDataDAO.getAdditionalDataMetaDataByTransactionTypeId(transactionTypeId);
	}
	/** 
	 *  to get Additional Fieldvalues on the basis of Primary Key
	 * @return 
	 * @param additionalDataId 
	 */
	@Override
	public AdditionalData getAdditionalData(Long additionalDataId) {
		return additionalDataDAO.find(AdditionalData.class, additionalDataId);
		
	}
	
	/** 
	 *  to get Additional Field values from temp table on the basis of Primary Key
	 * @return 
	 * @param tempAdditionalDataId 
	 */
	@Override
	public AdditionalDataTemp getTempAdditionalData(Long tempAdditionalDataId) {
		return additionalDataDAO.find(AdditionalDataTemp.class, tempAdditionalDataId);
	}
	
	@Override
	public List<ServiceRequestTransactionType> getAdditionalDataTransactionType(Long serviceOrTxnTypeId) {
		return additionalDataDAO.getAdditionalDataTransactionType(serviceOrTxnTypeId);
	}
	
	@Override
	public List<Map<String,String>> getMastersInfo()
	{
		return additionalDataDAO.getMastersInfo();
	}
	
	@Override
	public List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeForDisplayOrder(Long transactionTypeId) {
		return additionalDataDAO.getAdditionalDataMetaDataByTransactionTypeForDisplayOrder(transactionTypeId);
	}
	
	/**
	 * used to find Service Request Transaction Type
	 * @param serviceReqTxnTypeId
	 * @return
	 */
	@Override
	public ServiceRequestTransactionType findServiceRequestTransactionType(Long serviceReqTxnTypeId) {
	  return additionalDataDAO.findServiceRequestTransactionType(serviceReqTxnTypeId);
	}

	@Override
	public List<ServiceRequestTransactionType> getTransactionTypeDetails() {
		return additionalDataDAO.getTransactionTypeDetails();
	}	
	
	@Override
	public void deleteAdditionalDataAndMoveToHistory(AdditionalData additionalData){
		prepareDataAndMoveToHistory(additionalData);
		deleteAdditionalData(additionalData);
	}
	
	public void deleteAdditionalData(AdditionalData additionalData){
		additionalDataDAO.deleteAdditionalData(additionalData);
	}
	
	public void prepareDataAndMoveToHistory(AdditionalData additionalData){
		AdditionalDataHistory additionalDataHistory=new AdditionalDataHistory(additionalData);
		additionalDataDAO.createAdditionalDataHistory(additionalDataHistory);
	}
	
}