/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd
Description: DAO for Additional Field Implementation
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataHistory;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataMetaData;
import com.nucleus.finnone.pro.additionaldata.domainobject.ServiceRequestTransactionType;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDaoImpl;
@Named("additionalDataDAO")
public class AdditionalDataDAO extends EntityDaoImpl implements IAdditionalDataDAO{
	/*@Inject
	@Named("requestContext")
	private IRequestServicingContext requestServicingContext; 	
	*/
	public static final String APPROVAL_STATUS = "approvalStatus";
	public static final String TRANSACTION_TYPE_ID = "transactionTypeId";
	/** 
	 *  to get Additional Field Meta Data on the basis of TRansaction Type
	 * @return 
	 * @param _transactionType 
	 */
	@Override
	public List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionType(	String transactionType) {
		List<AdditionalDataMetaData> additionalDataMetaDataList = null;
		Query query=getEntityManager().createNamedQuery("getAdditionalDataMetaDataByTransactionType");
		//query.setParameter("tenantId", requestServicingContext.getTenant().getId());
		query.setParameter("transactionType", transactionType);
		List<Integer>  statusList = new ArrayList<Integer> ();
	    statusList.add(Integer.valueOf(0));
	    statusList.add(Integer.valueOf(3));
	    statusList.add(Integer.valueOf(4));
	    statusList.add(Integer.valueOf(6));
		query.setParameter(APPROVAL_STATUS, statusList);
		additionalDataMetaDataList = (List<AdditionalDataMetaData>)query.getResultList();
		
		return additionalDataMetaDataList;
	}
	@Override
	public List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeId(Long transactionTypeId) {
		List<AdditionalDataMetaData> additionalDataMetaDataList = null;
		Query query=getEntityManager().createNamedQuery("getAdditionalDataMetaDataByTransactionTypeID");
		//query.setParameter("tenantId", requestServicingContext.getTenant().getId());
		query.setParameter(TRANSACTION_TYPE_ID, transactionTypeId);
		List<Integer>  statusList = new ArrayList<Integer> ();
	    statusList.add(Integer.valueOf(0));
	    statusList.add(Integer.valueOf(2));
	    statusList.add(Integer.valueOf(3));
	    statusList.add(Integer.valueOf(4));
	    statusList.add(Integer.valueOf(5));
	    statusList.add(Integer.valueOf(6));
	    statusList.add(Integer.valueOf(7));
	    statusList.add(Integer.valueOf(8));
		query.setParameter(APPROVAL_STATUS, statusList);
		additionalDataMetaDataList = (List<AdditionalDataMetaData>)query.getResultList();
		return additionalDataMetaDataList;
	}
	
	
	@Override
	public List<AdditionalDataMetaData> getAdditionalDataMetaDataByTransactionTypeForDisplayOrder(Long transactionTypeId) {
		List<AdditionalDataMetaData> additionalDataMetaDataList = null;
		Query query=getEntityManager().createNamedQuery("getAdditionalDataByTransactionTypeForDisplayOrder");
		//query.setParameter("tenantId", requestServicingContext.getTenant().getId());
		query.setParameter(TRANSACTION_TYPE_ID, transactionTypeId);
		List<Integer>  statusList = new ArrayList<Integer> ();
		statusList.add(Integer.valueOf(0));
	    statusList.add(Integer.valueOf(2));
	    statusList.add(Integer.valueOf(3));
	    statusList.add(Integer.valueOf(4));
	    statusList.add(Integer.valueOf(5));
	    statusList.add(Integer.valueOf(6));
	    statusList.add(Integer.valueOf(7));
	    statusList.add(Integer.valueOf(8));
		query.setParameter(APPROVAL_STATUS, statusList);
		additionalDataMetaDataList = (List<AdditionalDataMetaData>)query.getResultList();
		return additionalDataMetaDataList;
	}
	
	
	public List<ServiceRequestTransactionType> getAdditionalDataTransactionType(Long serviceOrTxnTypeId) {
		List<ServiceRequestTransactionType> serviceRequestTransactionTypes = null;
		Query query=getEntityManager().createNamedQuery("getAdditionalDataTransactionType");
		//query.setParameter("tenantId", requestServicingContext.getTenant().getId());
		query.setParameter(TRANSACTION_TYPE_ID, serviceOrTxnTypeId);
		serviceRequestTransactionTypes = (List<ServiceRequestTransactionType>)query.getResultList();
		return serviceRequestTransactionTypes;
	}
	
	  public List<Map<String,String>> getMastersInfo()
	  {
		  List<Map<String,String>> allmastersList = new ArrayList<Map<String,String>>();
	      Set allEntityTypes = getEntityManager().getMetamodel().getEntities();
	      Iterator itr = allEntityTypes.iterator();
	      
	      Map<String,String> listOfTypeMap = null;
		  while(itr.hasNext()) 
	      { 
	    	  EntityType et = (EntityType)itr.next();
	    	  if (BaseMasterEntity.class.isAssignableFrom(et.getJavaType()) || GenericParameter.class.isAssignableFrom(et.getJavaType()))
	    	  {
	    		  listOfTypeMap = new HashMap<String,String>();
	    		  listOfTypeMap.put("id",et.getJavaType().getName());
	    		  listOfTypeMap.put("name", et.getJavaType().getSimpleName());
				  allmastersList.add(listOfTypeMap);
	    	  }
	      }
	    return allmastersList;
	  }
	  
	  /**
		 * used to find Service Request Transaction Type
		 * @param serviceReqTxnTypeId
		 * @return
		 */
	  public ServiceRequestTransactionType findServiceRequestTransactionType(Long serviceReqTxnTypeId) {
		  return getEntityManager().find(ServiceRequestTransactionType.class, serviceReqTxnTypeId);
	  }
	  
	  @Override
	  public List<ServiceRequestTransactionType> getTransactionTypeDetails(){
			
			Query transactionTypeQuery = getEntityManager().createNamedQuery("getTransactionTypeDetailsTest");
		//	transactionTypeQuery.setParameter(TRANSACTION_TYPE_ID, serviceOrTxnTypeId);
			List<ServiceRequestTransactionType> transactionTypes = transactionTypeQuery.getResultList();
			if(transactionTypes !=null && transactionTypes.size()>0){
				return transactionTypes;
			}
			return null;
		}
	/*@Override
	public List<ServiceRequestTransactionType> getTransactionTypeDetails() {
		// TODO Auto-generated method stub
		return null;
	}*/
	  
	  public void createAdditionalDataHistory(AdditionalDataHistory additionalDataHistory){
		  persist(additionalDataHistory);
	  }
	  
	  public void deleteAdditionalData(AdditionalData additionalData){
		  delete(additionalData);
	  }
	  
}