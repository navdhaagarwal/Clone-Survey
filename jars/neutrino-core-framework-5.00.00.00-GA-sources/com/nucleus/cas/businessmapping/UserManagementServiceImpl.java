/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.cas.businessmapping;

import com.nucleus.address.*;
import com.nucleus.businessmapping.entity.UserOrgBranchMapping;
import com.nucleus.businessmapping.service.UserBPMappingService;
import com.nucleus.businessmapping.service.UserManagementDao;
import com.nucleus.businessmapping.service.UserManagementServiceCore;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.config.persisted.vo.ValueType;
import com.nucleus.contact.SimpleContactInfo;
import com.nucleus.core.SelectiveMapping;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.core.organization.entity.OrganizationType;
import com.nucleus.core.organization.entity.SystemName;
import com.nucleus.core.organization.service.OrganizationService;
import com.nucleus.core.persistence.jdbc.PersistenceUtils;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.menu.MenuEntity;
import com.nucleus.menu.MenuVO;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.*;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.context.MessageSource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Nucleus Software Exports Limited
 */
@Named("userManagementService")
public class UserManagementServiceImpl extends BaseServiceImpl implements UserManagementService {

    @Inject
    @Named("userManagementDaoCore")
    private UserManagementDao               userManagementDao;

    @Inject
    @Named("userManagementServiceCore")
    private UserManagementServiceCore       userManagementServiceCore;

    @Inject
    @Named("organizationService")
    private OrganizationService             organizationService;
    
    @Inject
    @Named("baseMasterService")
    private BaseMasterService           	baseMasterService;
    
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService           	makerCheckerService;
    
    @Inject
    @Named("userService")
    private UserService                   	userService;
    
    @Inject
    @Named("configurationService")
    private ConfigurationService            configurationService;
    
    @Inject
    @Named("teamService")
    private TeamService                     teamService;
    
    @Inject
    @Named("userBPMappingService")
    private UserBPMappingService userBPMappingService;
    
    
    @Inject
    @Named("genericParameterService")
    private GenericParameterService   		genericParameterService;
	
	@Inject
    @Named("userCityVillageMappingService")
    private UserCityVillageMappingService userCityVillageMappingService;

    @Inject
    @Named("messageSource")
    private MessageSource messageSource;

    private static final int          ORACLE_BATCH_SIZE = 900;

    private static final String       QUERY_TO_FETCHING_USER_ORG_BRANCHES_PRODUCT_BY_USER_ORG_BRANCHE_ID = "SELECT ubm FROM UserOrgBranchProdMapping ubm where ubm.userOrgBranchMappingId  IN :userOrgBranchMappingIds";

    private static final String       QUERY_FOR_FETCHING_USER_BRANCHES                         = "UserManagement.getUserOrgBranchesProduct";

    private static final String       QUERY_FOR_FETCHING_USER_BRANCHES_WITHOUT_PRODUCT_MAPPING = "UserManagement.getUserOrgBranches";
    
    private static final String       QUERY_FOR_FETCHING_USER_ORG_BRANCHES_PRODUCT_BY_USER_BRANCHES = "UserManagement.getUserOrgBranchesProductByBranchIdAndUser";
    
    private static final String       QUERY_FOR_FETCHING_USER_ORG_BRANCHES_BY_BRANCHE_USER = "UserManagement.getUserOrgBranchByBranchAndUserId";
    
    private static final String       QUERY_FOR_FETCHING_USER_ORG_BRANCHES_PRODUCT_BY_USER_ORG_BRANCHE_ID = "UserManagement.getUserOrgBranchesProductByUserOrgBranchId";
    
    private static final String       QUERY_FOR_FETCHING_USER_ORG_BRANCHES_PRODUCT_BY_PRODUCT_USER_ORG_BRANCHE = "UserManagement.getUserOrgBranchesProductByLoanProductAndUserOrgBranchId";
    
    private static final String       QUERY_FOR_ORGANIZATION_BRANCH_GET_ORG_BRANCHES_WHERE_USER_IS_BRANCH_ADMIN = "Organization.getOrgBranchesWhereUserIsBranchAdmin";
    
    private static final String		  QUERY_FOR_USER_PRODUCT_SCHEME_MAPPINGS_BY_USER_ID	="UserManagement.getAllProductSchemeMappingListByUserId";
    
    private static final String       SELECTIVE_OPERATION_TYPE = "operationType";
    
    private static final String       SELECTIVE_Id = "id";
    
    private static final String       SELECTIVE_IS_ADMIN = "isAdmin";
    
    private static final String UNAPPROVED_ADDED = "unapproved_added";

	private static final String UNAPPROVED_DELETED = "unapproved_deleted";
    
	private static final String PRODUCT_CONCAT_CHAR = "P_";
    @Override
    public List<UserOrgBranchProdMapping> getUserOrgBranchProductMapping(Long userID) {
    	
    	User user = entityDao.find(User.class, userID);
    	List<UserOrgBranchProdMapping> userOrgBranchProductMappingList = new ArrayList<UserOrgBranchProdMapping>();
           /*
            * Get the original/approved user's data
            */
    	if(user.getApprovalStatus()==ApprovalStatus.APPROVED || user.getApprovalStatus()==ApprovalStatus.APPROVED_MODIFIED)
        {
    		userOrgBranchProductMappingList = getUserOrgBranchProductMappingList(userID);
        }else{
	    	User originalUser = (User) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(user.getEntityId());
	    	if(null!=originalUser){
	    		userOrgBranchProductMappingList = getUserOrgBranchProductMappingList(originalUser.getId());
	    	}
        }
    	
    	/*
    	 * Convert to map 
    	 */
    	Map<String, UserOrgBranchProdMapping> orgBranchProductMap = new HashMap<String, UserOrgBranchProdMapping>();
    	StringBuilder orgBranchProductMapKeyBuilder = new StringBuilder();
    	int start=0;
    	char tilde='~';
    	
    	for (UserOrgBranchProdMapping orgBranchProductMapping : userOrgBranchProductMappingList) {
			orgBranchProductMapKeyBuilder.delete(start, orgBranchProductMapKeyBuilder.length());
			orgBranchProductMapKeyBuilder.append(orgBranchProductMapping.getUserOrgBranchMapping().getOrganizationBranchId())
							.append(tilde)
							.append(orgBranchProductMapping.getLoanProductId());
			orgBranchProductMap.put(orgBranchProductMapKeyBuilder.toString(), orgBranchProductMapping);
		}
    	/*
    	 * Perform deletion/addition for unapproved user
    	 */
		if(!(user.getApprovalStatus()==ApprovalStatus.APPROVED || user.getApprovalStatus()==ApprovalStatus.APPROVED_MODIFIED)){	
        	List<UserOrgBranchProdMapping> updatedUserOrgBranchProductMappingList = getUserOrgBranchProductMappingList(userID);
        	if (! orgBranchProductMap.isEmpty()) {
            	updateOrgBranchProductMapForUnapprovedCases(orgBranchProductMap,updatedUserOrgBranchProductMappingList);
        	} else {        		
    	   		orgBranchProductMap = getBranchProductMap(updatedUserOrgBranchProductMappingList);
    	   	} 	
        }
		return new ArrayList<UserOrgBranchProdMapping>(orgBranchProductMap.values());
    }
    
    private void updateOrgBranchProductMapForUnapprovedCases(Map<String, UserOrgBranchProdMapping> orgBranchProductMap, List<UserOrgBranchProdMapping> updatedUserOrgBranchProductMappingList) {
    	
   	 Map<String, Map<String, UserOrgBranchProdMapping>>   addedOrDeletedBranchProdMap = getApprovedModifiedAddedOrDeletedBranchProdMap(updatedUserOrgBranchProductMappingList);
  		         		 
  		 Map<String, UserOrgBranchProdMapping> addedUnapprovedRecords = addedOrDeletedBranchProdMap.get(UNAPPROVED_ADDED);
  		 Map<String, UserOrgBranchProdMapping> deletedUnapprovedRecords = addedOrDeletedBranchProdMap.get(UNAPPROVED_DELETED);        		 
  		 updateApprovedOrgBranchProductFinalMap(orgBranchProductMap, addedUnapprovedRecords, deletedUnapprovedRecords);
   }
	private void updateApprovedOrgBranchProductFinalMap(Map<String, UserOrgBranchProdMapping> orgBranchProductMap,Map<String, UserOrgBranchProdMapping> addedUnapprovedRecords,Map<String, UserOrgBranchProdMapping> deletedUnapprovedRecords){
   	
   	if(!addedUnapprovedRecords.isEmpty()){
			 for (Map.Entry<String, UserOrgBranchProdMapping> entry : addedUnapprovedRecords.entrySet())  {
				 
				 if(!orgBranchProductMap.containsKey(entry.getKey())){
					 orgBranchProductMap.put(entry.getKey(), entry.getValue()); 
				 }
			}
		 }
		 
		 if(!deletedUnapprovedRecords.isEmpty()){
			 for (Map.Entry<String, UserOrgBranchProdMapping> entry : deletedUnapprovedRecords.entrySet())  {
				 if(orgBranchProductMap.containsKey(entry.getKey())){
					 orgBranchProductMap.remove(entry.getKey()); 
				 }
			}
		 }
		 
   }
   
   
   private Map<String, Map<String, UserOrgBranchProdMapping>> getApprovedModifiedAddedOrDeletedBranchProdMap(List<UserOrgBranchProdMapping> updatedUserOrgBranchProductMappingList){
   	
   	
   	 Map<String, Map<String, UserOrgBranchProdMapping>> addedAndRemovedUnapprovedRecordsMap = new HashMap<String, Map<String,UserOrgBranchProdMapping>>(); 
   	
   
   	
   	Map<String, UserOrgBranchProdMapping> addedOrgBranchProductMap = new HashMap<String, UserOrgBranchProdMapping>();
   	Map<String, UserOrgBranchProdMapping> deletedOrgBranchProductMap = new HashMap<String, UserOrgBranchProdMapping>();

   	MultiMap<String,UserOrgBranchProdMapping> addedUserOrgBranchProductMappingMultiValueMap = new MultiValueMap<String,UserOrgBranchProdMapping>();
   	MultiMap<String,UserOrgBranchProdMapping> deletedUserOrgBranchProductMappingMultiValueMap = new MultiValueMap<String,UserOrgBranchProdMapping>();

   	
   	List<String> addedBranchProdMappingKeyList = new ArrayList<String>();
   	List<String> deletedBranchProdMappingKeyList = new ArrayList<String>();	
   	
   	
   	
   	prepareAddedDeletedMultiValueMapForBranchProduct(updatedUserOrgBranchProductMappingList, addedUserOrgBranchProductMappingMultiValueMap, deletedUserOrgBranchProductMappingMultiValueMap, addedBranchProdMappingKeyList, deletedBranchProdMappingKeyList);
   	
   	updateAddedDeletedBranchProdMappingKeyList(addedBranchProdMappingKeyList,deletedBranchProdMappingKeyList);
   	
	    	for(int j=0;j<addedBranchProdMappingKeyList.size();j++){
	    		
	    		List<UserOrgBranchProdMapping>   userOrgBranchProdMappingList = (List<UserOrgBranchProdMapping>) addedUserOrgBranchProductMappingMultiValueMap.get(addedBranchProdMappingKeyList.get(j));
	    		if(userOrgBranchProdMappingList != null && !userOrgBranchProdMappingList.isEmpty()){
	    			addedOrgBranchProductMap.put(addedBranchProdMappingKeyList.get(j), userOrgBranchProdMappingList.get(0));
	    		}
	    	}
				
	    	for(int j=0;j<deletedBranchProdMappingKeyList.size();j++){
	    		
	    		List<UserOrgBranchProdMapping>   userOrgBranchProdMappingList = (List<UserOrgBranchProdMapping>) deletedUserOrgBranchProductMappingMultiValueMap.get(deletedBranchProdMappingKeyList.get(j));
	    		if(userOrgBranchProdMappingList != null && !userOrgBranchProdMappingList.isEmpty()){
	    			deletedOrgBranchProductMap.put(deletedBranchProdMappingKeyList.get(j), userOrgBranchProdMappingList.get(0));
	    		}
	    	}

	    	addedAndRemovedUnapprovedRecordsMap.put(UNAPPROVED_ADDED, addedOrgBranchProductMap);
	    	addedAndRemovedUnapprovedRecordsMap.put(UNAPPROVED_DELETED, deletedOrgBranchProductMap);
	    	
   		return addedAndRemovedUnapprovedRecordsMap;
   }
   private void updateAddedDeletedBranchProdMappingKeyList(
			List<String> addedBranchProdMappingKeyList,
			List<String> deletedBranchProdMappingKeyList) {
		
   	List<String> addedOrgBranchProdMappingTempList1 = new ArrayList<String>(addedBranchProdMappingKeyList); 
	    List<String> deletedOrgBranchProdMappingTempList1 = new ArrayList<String>(deletedBranchProdMappingKeyList); 

	    List<String> addedOrgBranchProdMappingTempList2 = new ArrayList<String>(addedBranchProdMappingKeyList); 
	    List<String> deletedOrgBranchProdMappingTempList2 = new ArrayList<String>(deletedBranchProdMappingKeyList); 
	    
   	for(int i=0;i<addedOrgBranchProdMappingTempList1.size();i++)	{
   		
			if(deletedOrgBranchProdMappingTempList1.contains(addedOrgBranchProdMappingTempList1.get(i))){    						
				addedBranchProdMappingKeyList.remove(addedOrgBranchProdMappingTempList1.get(i));
				deletedOrgBranchProdMappingTempList1.remove(addedOrgBranchProdMappingTempList1.get(i));
			}
		}       
   	
   	
   	for(int k=0;k<deletedOrgBranchProdMappingTempList2.size();k++)	{
   		
			if(addedOrgBranchProdMappingTempList2.contains(deletedOrgBranchProdMappingTempList2.get(k))){
					
				deletedBranchProdMappingKeyList.remove(deletedOrgBranchProdMappingTempList2.get(k));
				addedOrgBranchProdMappingTempList2.remove(deletedOrgBranchProdMappingTempList2.get(k));
			}
   	}
   	
   	
	}

	private void prepareAddedDeletedMultiValueMapForBranchProduct(List<UserOrgBranchProdMapping> userOrgBranchProductMappingList,
			  MultiMap<String,UserOrgBranchProdMapping> addedUserOrgBranchProductMappingMultiValueMap,
			  MultiMap<String,UserOrgBranchProdMapping> deletedUserOrgBranchProductMappingMultiValueMap,
			  List<String> addedBranchProdMappingKeyList,
			  List<String> deletedBranchProdMappingKeyList){
   	
   	StringBuilder orgBranchProductMapKeyBuilder = new StringBuilder();
   	int start=0;
   	char tilde='~';
   	
   	for(UserOrgBranchProdMapping userOrgBranchProductMapping : userOrgBranchProductMappingList)
   	{
   		prepareBranchProductMapKey(orgBranchProductMapKeyBuilder, start, tilde, userOrgBranchProductMapping);
			
   		if(SelectiveMapping.ADDITION_OPERATION.equalsIgnoreCase(userOrgBranchProductMapping.getOperationType()))
   		{        			
   			addedBranchProdMappingKeyList.add(orgBranchProductMapKeyBuilder.toString());
   			addedUserOrgBranchProductMappingMultiValueMap.put(orgBranchProductMapKeyBuilder.toString(), userOrgBranchProductMapping);
   		}
   		if(SelectiveMapping.DELETION_OPERATION.equalsIgnoreCase(userOrgBranchProductMapping.getOperationType()))
   		{
   			deletedBranchProdMappingKeyList.add(orgBranchProductMapKeyBuilder.toString());
   			deletedUserOrgBranchProductMappingMultiValueMap.put(orgBranchProductMapKeyBuilder.toString(), userOrgBranchProductMapping);

   		}
   	}
   	
   }
   
   private void prepareBranchProductMapKey(StringBuilder orgBranchProductMapKeyBuilder,int start,char tilde,UserOrgBranchProdMapping userOrgBranchProductMapping ){
   	orgBranchProductMapKeyBuilder.delete(start, orgBranchProductMapKeyBuilder.length());
		orgBranchProductMapKeyBuilder.append(userOrgBranchProductMapping.getUserOrgBranchMapping().getOrganizationBranchId())
						.append(tilde)
						.append(userOrgBranchProductMapping.getLoanProductId());
   }
   
   private void prepareMultiValueMapForBranchProduct(List<UserOrgBranchProdMapping> userOrgBranchProductMappingList,
   												  MultiMap<String,UserOrgBranchProdMapping> userOrgBranchProductMappingMultiValueMap,
   												  List<String> addedBranchProdMappingKeyList,
   												  List<String> deletedBranchProdMappingKeyList){
   	StringBuilder orgBranchProductMapKeyBuilder = new StringBuilder();
   	int start=0;
   	char tilde='~';
   	
   	for(UserOrgBranchProdMapping userOrgBranchProductMapping : userOrgBranchProductMappingList)
   	{
   		prepareBranchProductMapKey(orgBranchProductMapKeyBuilder, start, tilde, userOrgBranchProductMapping);
			
   		if(SelectiveMapping.ADDITION_OPERATION.equalsIgnoreCase(userOrgBranchProductMapping.getOperationType()))
   		{        			
   			addedBranchProdMappingKeyList.add(orgBranchProductMapKeyBuilder.toString());
   			userOrgBranchProductMappingMultiValueMap.put(orgBranchProductMapKeyBuilder.toString(), userOrgBranchProductMapping);
   		}
   		if(SelectiveMapping.DELETION_OPERATION.equalsIgnoreCase(userOrgBranchProductMapping.getOperationType()))
   		{
   			deletedBranchProdMappingKeyList.add(orgBranchProductMapKeyBuilder.toString());

   		}
   		
   	}
   }
   
   private void prepareFinalAddedBranchProductMappingKeyList(List<String> addedBranchProdMappingKeyList,
		   List<String> deletedBranchProdMappingKeyList){
	   List<String> addedOrgBranchProdMappingTempList = new ArrayList<String>(addedBranchProdMappingKeyList); 
	   for (int i=0; i<addedOrgBranchProdMappingTempList.size(); i++)	{
		   if (deletedBranchProdMappingKeyList.contains(addedOrgBranchProdMappingTempList.get(i))) {
			   addedBranchProdMappingKeyList.remove(addedOrgBranchProdMappingTempList.get(i));
			   deletedBranchProdMappingKeyList.remove(addedOrgBranchProdMappingTempList.get(i));
		   }       		
	   }
	   if (!addedOrgBranchProdMappingTempList.isEmpty()) {
		   addedOrgBranchProdMappingTempList.clear();
	   }
   }
   
   /**
    * Clearing map and list data to reduce the load on GC.(In case of huge values in List or map)
    */
   private void clearMultiValueMapAndLists(MultiMap<String,UserOrgBranchProdMapping> userOrgBranchProductMappingMultiValueMap,
			  List<String> addedBranchProdMappingKeyList,
			  List<String> deletedBranchProdMappingKeyList){

	   if(!addedBranchProdMappingKeyList.isEmpty()){
		   addedBranchProdMappingKeyList.clear();
	   }
	   if(!deletedBranchProdMappingKeyList.isEmpty()){
		   deletedBranchProdMappingKeyList.clear();
	   }
	   if(!userOrgBranchProductMappingMultiValueMap.isEmpty()){
		   userOrgBranchProductMappingMultiValueMap.clear();
	   }
   }
   private Map<String, UserOrgBranchProdMapping> getBranchProductMap(List<UserOrgBranchProdMapping> userOrgBranchProductMappingList){    	

	   Map<String, UserOrgBranchProdMapping> processedOrgBranchProductMap = new HashMap<String, UserOrgBranchProdMapping>();
	   MultiMap<String,UserOrgBranchProdMapping> userOrgBranchProductMappingMultiValueMap = new MultiValueMap<String,UserOrgBranchProdMapping>();
	   List<String> addedBranchProdMappingKeyList = new ArrayList<String>();
	   List<String> deletedBranchProdMappingKeyList = new ArrayList<String>();

	   prepareMultiValueMapForBranchProduct(userOrgBranchProductMappingList, userOrgBranchProductMappingMultiValueMap, addedBranchProdMappingKeyList, deletedBranchProdMappingKeyList);
	   prepareFinalAddedBranchProductMappingKeyList(addedBranchProdMappingKeyList, deletedBranchProdMappingKeyList);

	   for(int i=0;i<addedBranchProdMappingKeyList.size();i++){
		   List<UserOrgBranchProdMapping>   userOrgBranchProdMappingList = (List<UserOrgBranchProdMapping>) userOrgBranchProductMappingMultiValueMap.get(addedBranchProdMappingKeyList.get(i));
		   if(userOrgBranchProdMappingList != null && !userOrgBranchProdMappingList.isEmpty()){
			   processedOrgBranchProductMap.put(addedBranchProdMappingKeyList.get(i), userOrgBranchProdMappingList.get(0));
		   }
	   }
	   clearMultiValueMapAndLists(userOrgBranchProductMappingMultiValueMap, addedBranchProdMappingKeyList, deletedBranchProdMappingKeyList);   	
	   return processedOrgBranchProductMap;
   }

    @Override
    public List<UserOrgBranchMapping> getUserOrgBranchMapping(Long userID) {
        NamedQueryExecutor<UserOrgBranchMapping> executorUserOrgBranchMappings = new NamedQueryExecutor<UserOrgBranchMapping>(
                QUERY_FOR_FETCHING_USER_BRANCHES_WITHOUT_PRODUCT_MAPPING).addParameter("userID", userID);

        return entityDao.executeQuery(executorUserOrgBranchMappings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void saveBranchesAndProductsToUser(User user, Map<String, List<String>> productBranchMap,
            List<Long> originalOrgBranchList, List<Long> adminOfBranches, List<Long> selectedBranchesList, Long defaultBranch) {
        NeutrinoValidator.notNull(productBranchMap);
        List<String> newProductSet = new ArrayList<String>(productBranchMap.keySet());
        NeutrinoValidator.notNull(newProductSet);

        // Retrieve all the existing userOrgBranchProd Mappings
        List<UserOrgBranchProdMapping> existingUserOrgBranchProdMappings = getUserOrgBranchProductMapping(user.getId());
        List<UserOrgBranchMapping> existingUserOrgBranchList = new ArrayList<UserOrgBranchMapping>();

        // Remove the existing userOrgBranchProd Mappings
        for (UserOrgBranchProdMapping orgBranchProdMapping : existingUserOrgBranchProdMappings) {
            Long linkedOrgBranch = null;
            if (orgBranchProdMapping != null && orgBranchProdMapping.getUserOrgBranchMapping() != null
                    && orgBranchProdMapping.getUserOrgBranchMapping().getOrganizationBranch() != null) {
                linkedOrgBranch = orgBranchProdMapping.getUserOrgBranchMapping().getOrganizationBranch().getId();

            }
            if (linkedOrgBranch != null && CollectionUtils.isNotEmpty(originalOrgBranchList)
                    && originalOrgBranchList.contains(linkedOrgBranch)) {
                existingUserOrgBranchList.add(orgBranchProdMapping.getUserOrgBranchMapping());
                entityDao.delete(orgBranchProdMapping);
            }

        }

        // Remove the existing userOrgBranch Mappings corresponding to the
        // mapped products
        for (UserOrgBranchMapping ubm : existingUserOrgBranchList) {
            userManagementDao.delete(ubm);
        }

        // Remove the existing userOrgbranches in case there is no UserOrgBranchProductMapping is
        // available
        existingUserOrgBranchList = getUserOrgBranchMapping(user.getId());
        for (UserOrgBranchMapping ubm : existingUserOrgBranchList) {
            Long linkedOrgBranch = null;
            if (ubm != null && ubm.getOrganizationBranch() != null) {
                linkedOrgBranch = ubm.getOrganizationBranch().getId();
            }
            if (linkedOrgBranch != null && CollectionUtils.isNotEmpty(originalOrgBranchList)
                    && originalOrgBranchList.contains(linkedOrgBranch)) {
                userManagementDao.delete(ubm);
            }
        }

        /* Get all the branch ids which are mapped to at least one product */
        Set<Long> productMappedBranchIds = new HashSet<Long>();
        for (String newProduct : newProductSet) {
            if (!"_1".equals(newProduct)) {
                for (String branchId : productBranchMap.get(newProduct)) {
                    productMappedBranchIds.add(Long.valueOf(branchId));
                }
            }
        }

        /*Get all the branch ids which are not mapped to any of the product */
        List<Long> branchIdsWithNoProductsMapped = null;
        if (selectedBranchesList != null && productMappedBranchIds != null) {
            branchIdsWithNoProductsMapped = (List<Long>) CollectionUtils.subtract(selectedBranchesList,
                    productMappedBranchIds);
        }
        entityDao.flush();
        List<String> alreadyMappedBranches = new ArrayList<String>();
        
        for (String newProduct : newProductSet) {
            if (!"_1".equals(newProduct)) {

                List<UserOrgBranchMapping> listOfUBMapping = new ArrayList<UserOrgBranchMapping>();
                for (String branchId : productBranchMap.get(newProduct)) {
                    if (!(alreadyMappedBranches.contains(branchId))) {
                        UserOrgBranchMapping userOrgBrnchMapping = new UserOrgBranchMapping();
                        OrganizationBranch branch = entityDao.find(OrganizationBranch.class, Long.parseLong(branchId));
                        userOrgBrnchMapping.setAssociatedUser(user);
                        userOrgBrnchMapping.setOrganizationBranch(branch);
                        userOrgBrnchMapping.setApprovalStatus(ApprovalStatus.APPROVED);
                        if (defaultBranch != null && branchId.equals(defaultBranch.toString())) {
                                userOrgBrnchMapping.setPrimaryBranch(true);
                        }

                        /*Set branch admin */
                        if (adminOfBranches != null && adminOfBranches.contains(Long.parseLong(branchId))) {
                            userOrgBrnchMapping.setBranchAdmin(true);
                        }

                        userManagementDao.persist(userOrgBrnchMapping);
                        listOfUBMapping.add(userOrgBrnchMapping);
                        alreadyMappedBranches.add(branchId);
                    } else {
                        List<UserOrgBranchMapping> existingUBM = getUserOrgBranchMappingByBranchAndUserID(
                                Long.parseLong(branchId), user.getId());
                        if (existingUBM != null && !existingUBM.isEmpty()) {
                            if (defaultBranch != null && branchId.equals(defaultBranch.toString())) {
                                    existingUBM.get(0).setPrimaryBranch(true);
                            }
                            /*Set branch admin */
                            if (adminOfBranches != null && adminOfBranches.contains(Long.parseLong(branchId))) {
                                existingUBM.get(0).setBranchAdmin(true);
                            }
                            listOfUBMapping.add(existingUBM.get(0));

                        }

                    }

                }

                for (UserOrgBranchMapping uobm : listOfUBMapping) {
                    UserOrgBranchProdMapping uobpm = new UserOrgBranchProdMapping();
                  //TODO make LoanProduct plugable
                    //                    LoanProduct prod = entityDao.find(LoanProduct.class, Long.parseLong(newProduct));
//                    uobpm.setLoanProduct(prod);
                    uobpm.setUserOrgBranchMapping(uobm);
                    entityDao.persist(uobpm);
                }

            }
        }

        /*Save User Branch Mapping for all the branches which doesn't have any product mapping */
        if (branchIdsWithNoProductsMapped != null && !branchIdsWithNoProductsMapped.isEmpty()) {
            for (Long branchId : branchIdsWithNoProductsMapped) {
                UserOrgBranchMapping userOrgBrnchMapping = new UserOrgBranchMapping();
                OrganizationBranch branch = entityDao.find(OrganizationBranch.class, branchId);
                userOrgBrnchMapping.setAssociatedUser(user);
                userOrgBrnchMapping.setOrganizationBranch(branch);
                userOrgBrnchMapping.setApprovalStatus(ApprovalStatus.APPROVED);
                if (defaultBranch != null && branchId.equals(defaultBranch)) {
                        userOrgBrnchMapping.setPrimaryBranch(true);
                }

                /*Set branch admin */
                if (adminOfBranches != null && adminOfBranches.contains(branchId)) {
                    userOrgBrnchMapping.setBranchAdmin(true);
                }

                userManagementDao.persist(userOrgBrnchMapping);

            }
        }
    }

    @Override
    public List<UserOrgBranchMapping> getUserOrgBranchMappingByBranchAndUserID(Long branchID, Long userID) {
        NamedQueryExecutor<UserOrgBranchMapping> executorUserOrgBranchMappings = new NamedQueryExecutor<UserOrgBranchMapping>(
        		QUERY_FOR_FETCHING_USER_ORG_BRANCHES_BY_BRANCHE_USER).addParameter("userID", userID)
                .addParameter("branchID", branchID).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        List<UserOrgBranchMapping> userOrgBranchMappingList = entityDao.executeQuery(executorUserOrgBranchMappings);
        if(ValidatorUtils.notNull(userOrgBranchMappingList))
        {
        	 return userOrgBranchMappingList;
        }
        return new ArrayList<UserOrgBranchMapping>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void saveBranchesToUser(User user, List<Long> selectedBranchesList, List<Long> originalOrgBranchList,
            List<Long> adminOfBranches, List<Long> originalAdminBranches1, Long defaultBranch) {
        NeutrinoValidator.notNull(user, "User cannot be null");
        NeutrinoValidator.notNull(selectedBranchesList, "Selected BranchList cannot be null");
        NeutrinoValidator.notNull(originalOrgBranchList, "Original Branch list cannot be null");

        /*If none of the branches is selected and current user didn't had any prior mappings*/
        if (originalOrgBranchList.isEmpty() && selectedBranchesList.isEmpty()) {
            return;
        }

        /* List of OrganizationBranch ids that are unmapped for the current user*/
        List<Long> orgBranchMappingToRemovedList = (List<Long>) CollectionUtils.subtract(originalOrgBranchList,
                selectedBranchesList);

        /* List of new OrganizationBranch ids that are mapped to current user.*/
        List<Long> orgBranchMappingToAddList = (List<Long>) CollectionUtils.subtract(selectedBranchesList,
                originalOrgBranchList);

        /*Removing User-Branch-product and User-Branch Mapping for current user */
        if (!orgBranchMappingToRemovedList.isEmpty()) {

            /* List of UserOrgBranchProductMappings that are to be deleted. */

            NamedQueryExecutor<UserOrgBranchProdMapping> executor = new NamedQueryExecutor<UserOrgBranchProdMapping>(
                    "UserManagement.getUserOrgBranchesProductByBranchId");
            executor.addParameter("userId", user.getId());
            executor.addParameter("orgBranchMappingIds", orgBranchMappingToRemovedList);
            List<UserOrgBranchProdMapping> orgBranchProdMappingsToBeRemoved = entityDao.executeQuery(executor);

            if (orgBranchProdMappingsToBeRemoved != null && !orgBranchProdMappingsToBeRemoved.isEmpty()) {
                Set<UserOrgBranchMapping> orgBranchMappingsToBeRemoved = new HashSet<UserOrgBranchMapping>();
                for (UserOrgBranchProdMapping orgBranchProdMapping : orgBranchProdMappingsToBeRemoved) {
                    orgBranchMappingsToBeRemoved.add(orgBranchProdMapping.getUserOrgBranchMapping());
                    entityDao.delete(orgBranchProdMapping);
                }
            }

            /*Delete the remaining UserOrgBranchMappings which were not mapped to Products but now unmapped from user*/
            List<UserOrgBranchMapping> userOrgBranchMappings = userManagementServiceCore
                    .getUserOrgBranchMappingsForBranches(orgBranchMappingToRemovedList, user.getId());
            if (userOrgBranchMappings != null && !userOrgBranchMappings.isEmpty()) {
                for (UserOrgBranchMapping userOrgBranchMapping : userOrgBranchMappings) {
                    entityDao.delete(userOrgBranchMapping);
                }
            }

        }

        entityDao.flush();

        /*It may happen that user unchecks a branch but its "branch admin mapping" is still there. So removing all such admin branches */
        if (adminOfBranches != null && !adminOfBranches.isEmpty() && selectedBranchesList != null) {
            adminOfBranches = (List<Long>) CollectionUtils.intersection(adminOfBranches, selectedBranchesList);
        }

        /*Get Primary UserOrgBranchMapping */
        UserOrgBranchMapping defaultOrgBranchMapping = userManagementServiceCore.getUserBranchMappingForPrimaryBranch(user
                .getId());

        /*Adding New UserOrgBranchMapping */
        if (orgBranchMappingToAddList != null && !orgBranchMappingToAddList.isEmpty()) {
            for (Long branchId : orgBranchMappingToAddList) {
                UserOrgBranchMapping userOrgBrnchMapping = new UserOrgBranchMapping();
                OrganizationBranch branch = entityDao.find(OrganizationBranch.class, branchId);
                userOrgBrnchMapping.setAssociatedUser(user);
                userOrgBrnchMapping.setOrganizationBranch(branch);
                userOrgBrnchMapping.setApprovalStatus(ApprovalStatus.APPROVED);
                userOrgBrnchMapping.setActiveFlag(true);
                if (defaultBranch != null && branchId.equals(defaultBranch)) {
                        userOrgBrnchMapping.setPrimaryBranch(true);

                        /*Change default OrgBranch user. So that User will have only one primary branch  */
                        if (defaultOrgBranchMapping != null) {
                            defaultOrgBranchMapping.setPrimaryBranch(false);
                            entityDao.update(defaultOrgBranchMapping);
                        }
                }

                /*Set branch admin */
                if (adminOfBranches != null && adminOfBranches.contains(branchId)) {
                    userOrgBrnchMapping.setBranchAdmin(true);
                }
                userManagementDao.persist(userOrgBrnchMapping);
            }
        }

        /*Code for setting user's primary branch if primary branch is chosen from existing branches.*/
        if (orgBranchMappingToAddList != null && defaultBranch != null && orgBranchMappingToAddList.contains(defaultBranch)) {
            // Do nothing for user's primary branch
        } else if (defaultBranch != null) {/*Update default branch for the case when no new branch is mapped*/

            if (defaultOrgBranchMapping == null) {

                updateDefaultOrgBranchMapping(defaultBranch, user.getId());

                /*If Branch which we are going to make primary is already his primary branch*/
            } else if (!defaultBranch.equals(defaultOrgBranchMapping.getOrganizationBranch().getId())) {

                updateDefaultOrgBranchMapping(defaultBranch, user.getId());

                /*Change previous mapping as well*/
                defaultOrgBranchMapping.setPrimaryBranch(false);
                entityDao.update(defaultOrgBranchMapping);
            }
        }

        List<OrganizationBranch> originalAdminBranchList = organizationService.getBranchesWhereUserIsBranchAdmin(user
                .getId());

        List<Long> originalAdminBranches = new ArrayList<Long>();
        if (originalAdminBranchList != null && !originalAdminBranchList.isEmpty()) {
            for (OrganizationBranch organizationBranch : originalAdminBranchList) {
                originalAdminBranches.add(organizationBranch.getId());
            }
        }

        /* Do this if current user is made branch admin for any of the existing branch */
        if (adminOfBranches != null) {

            /* List of OrganizationBranch ids for which current user is made admin*/
            List<Long> addAdminBranchList = (List<Long>) CollectionUtils.subtract(adminOfBranches, originalAdminBranches);

            /*Get all branches where user is made admin but that branch is already mapped to the user. */
            addAdminBranchList = (List<Long>) CollectionUtils.subtract(addAdminBranchList, orgBranchMappingToAddList);

            if (!addAdminBranchList.isEmpty()) {
                List<UserOrgBranchMapping> orgBranchMappings = userManagementServiceCore
                        .getUserOrgBranchMappingsForBranches(addAdminBranchList, user.getId());
                if (orgBranchMappings != null && !orgBranchMappings.isEmpty()) {
                    for (UserOrgBranchMapping orgBranchMapping : orgBranchMappings) {
                        orgBranchMapping.setBranchAdmin(true);
                        entityDao.update(orgBranchMapping);
                    }
                }
            }

            /* List of OrganizationBranch ids for which current user was admin but not now. Need to update this now*/
            List<Long> removeAdminBranchList = (List<Long>) CollectionUtils.subtract(originalAdminBranches, adminOfBranches);

            /*Get all branches where user is not admin but are still mapped to the user. */
            removeAdminBranchList = (List<Long>) CollectionUtils.subtract(removeAdminBranchList,
                    orgBranchMappingToRemovedList);

            if (!removeAdminBranchList.isEmpty()) {
                List<UserOrgBranchMapping> orgBranchMappings = userManagementServiceCore
                        .getUserOrgBranchMappingsForBranches(removeAdminBranchList, user.getId());
                if (orgBranchMappings != null && !orgBranchMappings.isEmpty()) {
                    for (UserOrgBranchMapping orgBranchMapping : orgBranchMappings) {
                        orgBranchMapping.setBranchAdmin(false);
                        entityDao.update(orgBranchMapping);
                    }
                }
            }

        }

    }

    private void updateDefaultOrgBranchMapping(Long defaultBranch, Long userId) {
        List<UserOrgBranchMapping> newDefaultUOBMList = getUserOrgBranchMappingByBranchAndUserID(defaultBranch, userId);

        if (newDefaultUOBMList != null && !newDefaultUOBMList.isEmpty()) {
            UserOrgBranchMapping newDefaultMapping = newDefaultUOBMList.get(0);
            newDefaultMapping.setPrimaryBranch(true);
            entityDao.update(newDefaultMapping);
        }
    }

    @Override
    public Long getUserOrganizationPrimaryBranchIdFromUserId(Long userId) {
        NeutrinoValidator.notNull(userId, "User Id Cannot be null");
        Long defaultBranchId = null;
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("User.primaryOrganizationBranchIdbyUserId")
                .addParameter("userId", userId);
        List<Long> defaultBranchList = entityDao.executeQuery(executor);
        if (CollectionUtils.isNotEmpty(defaultBranchList)) {
            defaultBranchId = defaultBranchList.get(0);
        }
        return defaultBranchId;

    }
    
   @Override
	public void saveBranchesToUser(User user,
			List<Map<String, Object>> changeBranchList) {/*

		NeutrinoValidator.notNull(user, "User cannot be null");

		
		 * If none of the branches is selected and current user didn't had any
		 * prior mappings
		 
		if (changeBranchList.size() == 0) {
			return;
		}

		 Adding New UserOrgBranchMapping 
		for (Map<String, Object> changedBranch : changeBranchList) {
			UserOrgBranchMapping userOrgBrnchMapping = new UserOrgBranchMapping();
			OrganizationBranch branch = entityDao.find(
					OrganizationBranch.class, (Long) changedBranch.get("id"));
			userOrgBrnchMapping.setAssociatedUser(user);
			userOrgBrnchMapping.setOrganizationBranch(branch);
			userOrgBrnchMapping
					.setApprovalStatus(ApprovalStatus.APPROVED_MODIFIED);
			userOrgBrnchMapping.setActiveFlag(true);
			userOrgBrnchMapping.setBranchAdmin((Boolean) changedBranch
					.get(SELECTIVE_IS_ADMIN));
			userOrgBrnchMapping.setPrimaryBranch((Boolean) changedBranch
					.get("defaultBranch"));

			if (SelectiveMapping.ADDITION_OPERATION.equals(changedBranch
					.get("operationType"))) {
				userOrgBrnchMapping
						.setOpeartionType(SelectiveMapping.ADDITION_OPERATION);
				userManagementDao.persist(userOrgBrnchMapping);
			} else if (SelectiveMapping.DELETION_OPERATION.equals(changedBranch
					.get("operationType"))) {

				NamedQueryExecutor<UserOrgBranchMapping> executor = new NamedQueryExecutor<UserOrgBranchMapping>(
						QUERY_FOR_FETCHING_USER_ORG_BRANCHES_BY_USER_BRANCHES);
				executor.addParameter("userId", user.getId());
				executor.addParameter("branchId", changedBranch.get("id"));
				List<UserOrgBranchMapping> persistedUserOrgBranchMapList = entityDao
						.executeQuery(executor);
				if (null != persistedUserOrgBranchMapList
						&& persistedUserOrgBranchMapList.size() > 0) {
					userOrgBrnchMapping
							.setBranchAdmin(persistedUserOrgBranchMapList
									.get(0).isBranchAdmin());
					userOrgBrnchMapping
							.setPrimaryBranch(persistedUserOrgBranchMapList
									.get(0).isPrimaryBranch());
					userOrgBrnchMapping
							.setOpeartionType(SelectiveMapping.DELETION_OPERATION);
					userManagementDao.persist(userOrgBrnchMapping);
				}
			} else {
				userOrgBrnchMapping
						.setOpeartionType(SelectiveMapping.MODIFICATION_OPERATION);
				userManagementDao.persist(userOrgBrnchMapping);
			}

		}

	*/}
   
   
   
   
   //TODO should update
   @SuppressWarnings("unchecked")
   
   public void saveBranchesAndProductsToUser(User user, Map<String, List<String>> productBranchMap,
			List<Long> originalOrgBranchList, List<Long> adminOfBranches,
			List<Long> selectedBranchesList, Long defaultBranch, String aa) {

		NeutrinoValidator.notNull(productBranchMap);
		List<String> newProductSet = new ArrayList<String>(
				productBranchMap.keySet());
		NeutrinoValidator.notNull(newProductSet);

		// Retrieve all the existing userOrgBranchProd Mappings
		List<UserOrgBranchProdMapping> existingUserOrgBranchProdMappings = getUserOrgBranchProductMapping(user
				.getId());
		List<UserOrgBranchMapping> existingUserOrgBranchList = new ArrayList<UserOrgBranchMapping>();

		// Remove the existing userOrgBranchProd Mappings
		for (UserOrgBranchProdMapping orgBranchProdMapping : existingUserOrgBranchProdMappings) {
			Long linkedOrgBranch = null;
			if (orgBranchProdMapping != null
					&& orgBranchProdMapping.getUserOrgBranchMapping() != null
					&& orgBranchProdMapping.getUserOrgBranchMapping()
							.getOrganizationBranch() != null) {
				linkedOrgBranch = orgBranchProdMapping
						.getUserOrgBranchMapping().getOrganizationBranch()
						.getId();

			}
			if (linkedOrgBranch != null
					&& CollectionUtils.isNotEmpty(originalOrgBranchList)
					&& originalOrgBranchList.contains(linkedOrgBranch)) {
				existingUserOrgBranchList.add(orgBranchProdMapping
						.getUserOrgBranchMapping());
				entityDao.delete(orgBranchProdMapping);
			}

		}

		// Remove the existing userOrgBranch Mappings corresponding to the
		// mapped products
		for (UserOrgBranchMapping ubm : existingUserOrgBranchList) {
			userManagementDao.delete(ubm);
		}

		// Remove the existing userOrgbranches in case there is no
		// UserOrgBranchProductMapping is
		// available
		existingUserOrgBranchList = getUserOrgBranchMapping(user.getId());
		for (UserOrgBranchMapping ubm : existingUserOrgBranchList) {
			Long linkedOrgBranch = null;
			if (ubm != null && ubm.getOrganizationBranch() != null) {
				linkedOrgBranch = ubm.getOrganizationBranch().getId();
			}
			if (linkedOrgBranch != null
					&& CollectionUtils.isNotEmpty(originalOrgBranchList)
					&& originalOrgBranchList.contains(linkedOrgBranch)) {
				userManagementDao.delete(ubm);
			}
		}

		/* Get all the branch ids which are mapped to at least one product */
		Set<Long> productMappedBranchIds = new HashSet<Long>();
		for (String newProduct : newProductSet) {
			if (!"_1".equals(newProduct)) {
				for (String branchId : productBranchMap.get(newProduct)) {
					productMappedBranchIds.add(Long.valueOf(branchId));
				}
			}
		}

		/* Get all the branch ids which are not mapped to any of the product */
		List<Long> branchIdsWithNoProductsMapped = null;
		if (selectedBranchesList != null && productMappedBranchIds != null) {
			branchIdsWithNoProductsMapped = (List<Long>) CollectionUtils
					.subtract(selectedBranchesList, productMappedBranchIds);
		}
		entityDao.flush();
		List<String> alreadyMappedBranches = new ArrayList<String>();
		for (String newProduct : newProductSet) {
			if (!"_1".equals(newProduct)) {

				List<UserOrgBranchMapping> listOfUBMapping = new ArrayList<UserOrgBranchMapping>();
				for (String branchId : productBranchMap.get(newProduct)) {
					if (!(alreadyMappedBranches.contains(branchId))) {
						UserOrgBranchMapping userOrgBrnchMapping = new UserOrgBranchMapping();
						OrganizationBranch branch = entityDao.find(
								OrganizationBranch.class,
								Long.parseLong(branchId));
						userOrgBrnchMapping.setAssociatedUser(user);
						userOrgBrnchMapping.setOrganizationBranch(branch);
						userOrgBrnchMapping
								.setApprovalStatus(ApprovalStatus.APPROVED);
						if (defaultBranch != null && branchId.equals(defaultBranch.toString())) {
								userOrgBrnchMapping.setPrimaryBranch(true);
						}

						/* Set branch admin */
						if (adminOfBranches != null
								&& adminOfBranches.contains(Long
										.parseLong(branchId))) {
							userOrgBrnchMapping.setBranchAdmin(true);
						}

						userManagementDao.persist(userOrgBrnchMapping);
						listOfUBMapping.add(userOrgBrnchMapping);
						alreadyMappedBranches.add(branchId);
					} else {
						List<UserOrgBranchMapping> existingUBM = getUserOrgBranchMappingByBranchAndUserID(
								Long.parseLong(branchId), user.getId());
						if (existingUBM != null && !existingUBM.isEmpty()) {
							if (defaultBranch != null && branchId.equals(defaultBranch.toString())) {
									existingUBM.get(0).setPrimaryBranch(true);
							}
							/* Set branch admin */
							if (adminOfBranches != null
									&& adminOfBranches.contains(Long
											.parseLong(branchId))) {
								existingUBM.get(0).setBranchAdmin(true);
							}
							listOfUBMapping.add(existingUBM.get(0));

						}

					}

				}

				for (UserOrgBranchMapping uobm : listOfUBMapping) {
					UserOrgBranchProdMapping uobpm = new UserOrgBranchProdMapping();
					//TODO make LoanProduct plugable
					//					LoanProduct prod = entityDao.find(LoanProduct.class,
//							Long.parseLong(newProduct));
//					uobpm.setLoanProduct(prod);
					uobpm.setUserOrgBranchMapping(uobm);
					entityDao.persist(uobpm);
				}

			}
		}

		/*
		 * Save User Branch Mapping for all the branches which doesn't have any
		 * product mapping
		 */
		if (branchIdsWithNoProductsMapped != null
				&& !branchIdsWithNoProductsMapped.isEmpty()) {
			for (Long branchId : branchIdsWithNoProductsMapped) {
				UserOrgBranchMapping userOrgBrnchMapping = new UserOrgBranchMapping();
				OrganizationBranch branch = entityDao.find(
						OrganizationBranch.class, branchId);
				userOrgBrnchMapping.setAssociatedUser(user);
				userOrgBrnchMapping.setOrganizationBranch(branch);
				userOrgBrnchMapping.setApprovalStatus(ApprovalStatus.APPROVED);
				if (defaultBranch != null && branchId.equals(defaultBranch)) {
						userOrgBrnchMapping.setPrimaryBranch(true);
				}

				/* Set branch admin */
				if (adminOfBranches != null
						&& adminOfBranches.contains(branchId)) {
					userOrgBrnchMapping.setBranchAdmin(true);
				}

				userManagementDao.persist(userOrgBrnchMapping);

			}
		}
	}
   
	/**
	 * This method is used to save user and user related data.
	 * 
	 * @param userVo
	 */
	@Override
	public User updateUserAtMakerStage(UserVO userVO) {

		User user = baseMasterService.getMasterEntityById(User.class, userVO.getFormUser().getId());
		Integer approvalStatusBeforeUpdate = user.getMasterLifeCycleData().getApprovalStatus();

		/**
		 * Initiate maker/checker workflow
		 */
		
		User lastApprovedUserRecord = (User) baseMasterService
				.getLastApprovedEntityByUnapprovedEntityId(userVO.getFormUser().getEntityId());
		
		User updatedUser = (User) makerCheckerService
				.masterEntityChangedByUser(userVO.getFormUser(),getCurrentUser().getUserReference());

		updatedUser = updateUserInformation(userVO, user, updatedUser, lastApprovedUserRecord, approvalStatusBeforeUpdate);

		return updatedUser;
	}
	
	
	
	@Override
	public User updateUserAtMakerStageSendForApproval(UserVO userVO) {

		User user = baseMasterService.getMasterEntityById(User.class, userVO.getFormUser().getId());
		Integer approvalStatusBeforeUpdate = user.getMasterLifeCycleData().getApprovalStatus();
		
		/**
		 * Initiate checker operation
		 */
		
		User lastApprovedUserRecord = (User) baseMasterService
				.getLastApprovedEntityByUnapprovedEntityId(userVO.getFormUser().getEntityId());

		User updatedUser = (User) makerCheckerService
				.saveAndSendForApproval(userVO.getFormUser(), getCurrentUser().getUserReference());

		user = updateUserInformation(userVO, user, updatedUser, lastApprovedUserRecord, approvalStatusBeforeUpdate);
		
		return user;
	}
	
	
	@Override
	public User updateUserAtMakerStageSendForApproval(User changedUser, User fromUser) {
		
		User user = baseMasterService.getMasterEntityById(User.class, fromUser.getId());
		Integer approvalStatusBeforeUpdate = user.getMasterLifeCycleData().getApprovalStatus();
		
		/**
		 * Initiate checker operation
		 */
		
		User lastApprovedUserRecord = (User) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(fromUser.getEntityId());

		User updatedUser = (User) makerCheckerService.saveAndSendForApproval(changedUser, getCurrentUser().getUserReference());
	
		user = updateUserInformation(fromUser, updatedUser, lastApprovedUserRecord, approvalStatusBeforeUpdate);
		
		return user;
	}


	protected User updateUserInformation(UserVO userVO, User user, User updatedUser, User lastApprovedUserRecord,Integer approvalStatusBeforeUpdate)
	{
		User originalUserRecord = null;
		/*
		 * If the supplied user was approved - find the unapproved entity else
		 * since it was unapproved - find by id itself
		 */
		boolean primaryBranchToBeUpdatedForUpdatedUser = Boolean.FALSE;
		if (ValidatorUtils.notNull(approvalStatusBeforeUpdate)
				&& (approvalStatusBeforeUpdate == ApprovalStatus.APPROVED)) {
			originalUserRecord = user;
		} else if (ValidatorUtils.notNull(approvalStatusBeforeUpdate)
				&& (approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_ADDED || approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_MODIFIED)) {
			originalUserRecord = lastApprovedUserRecord;
			primaryBranchToBeUpdatedForUpdatedUser = Boolean.TRUE;
		}

		/*
		 * 1. Add or update user profile
		 */
		userVO.getFormUserProfile().setAssociatedUser(updatedUser);
		updateUserProfileAtMakerStage(userVO.getFormUserProfile(), updatedUser,
				originalUserRecord, approvalStatusBeforeUpdate);

		/*
		 * 2. Add or update Roles mapped to user
		 */
		
		boolean userIsNonLogin = !userVO.getFormUser().isLoginEnabled();
		
		if (userVO.getRoleMappings() != null
				&& (userVO.getRoleMappings().length > 0 || userIsNonLogin) && updatedUser != null) {
			userService.saveRolesForUser(updatedUser, userVO.getRoleMappings());
		}

		/*
		 * 3, 4 & 5. Add or update branches, branch admin & User-Branch-Products
		 * mapped to user
		 */
     updateBranchesAndProductsMappingAtMakerStageModified(
				userVO.getUpdatedUserOrgBranchMappings(),
             userVO.getUpdatedUserOrgBranchProductMappingsModified(),
				userVO.getDefaultBranch(), updatedUser, originalUserRecord,
				approvalStatusBeforeUpdate, primaryBranchToBeUpdatedForUpdatedUser);

		/*
		 * 7. Add or update preferences mapped to user
		 */
		updateUserPreferencesAtMakerStage(userVO.getUpdatedUserPreferences(),
				updatedUser, userVO.getMyFavs());

		/*
		 * 8. Add or update teams mapped to user
		 */
		if (userVO.getTeamMappings() != null
				&& (userVO.getTeamMappings().length > 0 || userIsNonLogin) && updatedUser != null) {
			updateTeamsMappingAtMakerStage(updatedUser, userVO.getTeamMappings());
		}
		
		/*
		 * 9. Add or update business partner and team mapped to user
		 */		
		if (userVO.getMappedBPId() != null || userVO.getFormUser().getTeamLead()) {
			updateBusinessPartnerMappingAndTeamMappingAtMakerStage(updatedUser,userVO.getMappedBPId(),userVO.getFormUser().getTeamLead());
		}
		
		/*
		 * 10. Add or update city village mapping
		 */

        updateCityVillageMappingAtMakerStage(userVO.getCityVillageMapping(),updatedUser,originalUserRecord,approvalStatusBeforeUpdate);
     updateUserUrlMappingAtMakerStage(userVO.getUserDefaultUrlMappingVOList(),updatedUser,originalUserRecord,approvalStatusBeforeUpdate,userVO.getDeletedUserUrlMappings());

		return updatedUser;

	}

	private User updateUserInformation(User existingUser, User updatedUser, User lastApprovedUserRecord,Integer approvalStatusBeforeUpdate)
	{
		User originalUserRecord = null;
		/*
		 * If the supplied user was approved - find the unapproved entity else
		 * since it was unapproved - find by id itself
		 */
		if (ValidatorUtils.notNull(approvalStatusBeforeUpdate)
				&& (approvalStatusBeforeUpdate == ApprovalStatus.APPROVED)) {
			originalUserRecord = existingUser;
		} else if (ValidatorUtils.notNull(approvalStatusBeforeUpdate)
				&& (approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_ADDED || approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_MODIFIED)) {
			originalUserRecord = lastApprovedUserRecord;
		}
		
		/*
		 * 1. Add or update user profile
		 */
		UserProfile formUserProfile = userService.getUserProfile(originalUserRecord);
		formUserProfile.setAssociatedUser(updatedUser);
		updateUserProfileAtMakerStage(formUserProfile, updatedUser,originalUserRecord, approvalStatusBeforeUpdate);

		/*
		 * 2. Add or update Roles mapped to user
		 */
		
		boolean userIsNonLogin = !originalUserRecord.isLoginEnabled();
		
		if (originalUserRecord.getUserRoles() != null
				&& (originalUserRecord.getUserRoles().size() > 0 || userIsNonLogin) 
				&& updatedUser != null) {
			Long[] rolesIds = originalUserRecord.getUserRoles().stream().map(Role::getId).toArray(Long[]::new);
			userService.saveRolesForUser(updatedUser, rolesIds);
		}

		/*
		 * 7. Add or update preferences mapped to user
		 */
	     Map<String, ConfigurationVO> configurationMap = configurationService
	             .getFinalUserModifiableConfigurationForEntity(existingUser.getEntityId());
	
	     // If there is no user preference, it means that this is a new user
	     if (configurationMap.size() == 0) {
	         // Add an entry in configuration group for this new user
	         configurationService.populateConfigurationForNewEntity(SystemEntity.getSystemEntityId(), existingUser.getEntityId());
	         configurationMap = configurationService.getFinalUserModifiableConfigurationForEntity(existingUser.getEntityId());
	     }
		updateUserPreferencesAtMakerStage(new ArrayList<ConfigurationVO>(configurationMap.values()),updatedUser, null);

		/*
		 * 8. Add or update teams mapped to user
		 */
		List<Long> teamIds = teamService.getTeamIdAssociatedToUserByUserId(originalUserRecord.getId());
		if (teamIds != null
				&& (teamIds.size() > 0 || userIsNonLogin) && updatedUser != null) {
			updateTeamsMappingAtMakerStage(updatedUser, teamIds.toArray(new Long[teamIds.size()]));
		}
		
		/*
		 * 9. Add or update business partner and team mapped to user
		 */
		Long associatedBPId = userBPMappingService.getAssociatedBPIdByUserId(originalUserRecord.getId());
		if (associatedBPId != null || originalUserRecord.getTeamLead()) {
			updateBusinessPartnerMappingAndTeamMappingAtMakerStage(updatedUser,associatedBPId,originalUserRecord.getTeamLead());
		}
		
		/*
		 * 10. Add or update city village mapping
		 */
		UserCityVillageMapping saveduserCityVillageMapping  = userCityVillageMappingService.getCityVillageMappingByUserId(originalUserRecord.getId());
		
		if(saveduserCityVillageMapping != null) {
			
			List<UserCityMapping> userCityMappingList = new ArrayList<UserCityMapping>();
			List<UserVillageMapping> userVillageMappingList = new ArrayList<UserVillageMapping>();
			
			for(UserCityMapping ucm : saveduserCityVillageMapping.getUserCityMappings()) {
				if(ucm != null) {
					UserCityMapping userCityMapping = new UserCityMapping();
		            userCityMapping.setCity(ucm.getCity());
		            
		            List<Area> areaList = new ArrayList<>();
					for (Area areaKey : ucm.getCityAreaList()) {
						if(areaKey != null && areaKey.getUri() != null) {
							areaList.add(baseMasterService.getEntityByEntityId(EntityId.fromUri(areaKey.getUri())));
						}
					}
					userCityMapping.setCityAreaList(areaList);
		            userCityMappingList.add(userCityMapping);
				}
			}
			
			for(UserVillageMapping uvm : saveduserCityVillageMapping.getUserVillageMappings()) {	            
				if(uvm != null) {
	            	UserVillageMapping userVillageMapping = new UserVillageMapping();
		            userVillageMapping.setVillageMaster(uvm.getVillageMaster());
		            
		            List<Area> areaList = new ArrayList<>();
					for (Area areaKey : uvm.getVillageAreaList()) {
						if(areaKey != null && areaKey.getUri() != null) {
							areaList.add(baseMasterService.getEntityByEntityId(EntityId.fromUri(areaKey.getUri())));
						}
					}
		            userVillageMapping.setVillageAreaList(areaList);
		            userVillageMappingList.add(userVillageMapping);
	            }
			}
			
			UserCityVillageMapping userCityVillageMapping = new UserCityVillageMapping();
	        userCityVillageMapping.setUserCityMappings(userCityMappingList);
	        userCityVillageMapping.setUserVillageMappings(userVillageMappingList);
	        
	        updateCityVillageMappingAtMakerStage(userCityVillageMapping,updatedUser,originalUserRecord,approvalStatusBeforeUpdate);
		}
        
        List<UserDefaultUrlMapping> userDefaultUrlMappingList = getAllUrlMappingsOfUser(originalUserRecord.getId());
        
        if(!CollectionUtils.isEmpty(userDefaultUrlMappingList)) {
            List<UserDefaultUrlMappingVO> userDefaultUrlMappingVOList = userUrlMappingListToVO(userDefaultUrlMappingList);
            
            updateUserUrlMappingAtMakerStage(userDefaultUrlMappingVOList,updatedUser,originalUserRecord,approvalStatusBeforeUpdate,null);
        }
		return updatedUser;

	}


	/**
	 * 1. Add or update user profile
	 * 
	 * @param suppliedUserProfile
	 * @param userToBeUpdated
	 * @param approvalStatusBeforeUpdate
	 */
	protected void updateUserProfileAtMakerStage(
			UserProfile suppliedUserProfile, User userToBeUpdated, User originalUser,
			int approvalStatusBeforeUpdate) {
		if (ValidatorUtils.notNull(approvalStatusBeforeUpdate)
				&& (approvalStatusBeforeUpdate == ApprovalStatus.APPROVED)) {
			UserProfile userProfile = userService.getUserProfile(originalUser);
            if(suppliedUserProfile.getSimpleContactInfo()!=null && suppliedUserProfile.getSimpleContactInfo().getAddress()!=null && !isAddressNeeded(suppliedUserProfile.getSimpleContactInfo().getAddress())){
                suppliedUserProfile.getSimpleContactInfo().setAddress(null);
            }
			suppliedUserProfile = userManagementServiceCore.prepareUserProfileFromExistingUserProfile(userProfile, suppliedUserProfile);
			suppliedUserProfile.setAssociatedUser(userToBeUpdated);
			userService.saveNewUserProfile(suppliedUserProfile);
		} else if (ValidatorUtils.notNull(approvalStatusBeforeUpdate)
				&& (approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_ADDED || approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_MODIFIED)) {
			UserProfile userProfile = userService
					.getUserProfile(userToBeUpdated);
            if(suppliedUserProfile.getSimpleContactInfo()!=null && suppliedUserProfile.getSimpleContactInfo().getAddress()!=null && !isAddressNeeded(suppliedUserProfile.getSimpleContactInfo().getAddress())){
                suppliedUserProfile.getSimpleContactInfo().setAddress(null);
            }
			userProfile = userManagementServiceCore.copyUserProfile(
					userProfile, suppliedUserProfile);
			userService.saveUserProfile(userProfile);
		}
	}

	private Boolean isAddressNeeded(Address address){
        if((address.getAddressLine1()==null || address.getAddressLine1().isEmpty()) && (address.getAddressLine2()==null || address.getAddressLine2().isEmpty()) && (address.getAddressLine3()==null ||
                address.getAddressLine3().isEmpty()) && (address.getCity()==null || address.getCity().getCityCode()==null || address.getCity().getCityCode().isEmpty()) && address.getPoBox()==null &&
                (address.getState()==null || address.getState().getStateCode()==null || address.getState().getStateCode().isEmpty()) && (address.getRegion()==null || address.getRegion().getIntraRegionCode()==null
        || address.getRegion().getIntraRegionCode().isEmpty()) && (address.getDistrict()==null || address.getDistrict().getDistrictCode()==null || address.getDistrict().getDistrictCode().isEmpty())
                && (address.getTaluka()==null ||address.getTaluka().isEmpty()) && (address.getVillage()==null || address.getVillage().isEmpty())){
            return false;
        }
         return true;
    }

	/**
	 * 3, 4 & 5. Add or update branches, branch admin & User-Branch-Products
	 * mapped to user
	 * 
	 * @param updatedUserOrgBranchMappings
	 * @param updatedUserOrgBranchProductMappings
	 * @param defaultBranch
	 * @param updatedUser
	 * @param originalUser
	 * @param approvalStatusBeforeUpdate
	 */
	protected void updateBranchesAndProductsMappingAtMakerStage(
			List<Map<String, Object>> updatedUserOrgBranchMappings,
			Map<Long, List<Map<Long, String>>> updatedUserOrgBranchProductMappings,
			Long defaultBranch, User updatedUser,User originalUser,
			Integer approvalStatusBeforeUpdate, boolean primaryBranchToBeUpdatedForUpdatedUser) {

		NeutrinoValidator.notNull(updatedUser, "User cannot be null");

		/*
		 * If none of the branches and products are selected and current user
		 * didn't had any prior mappings
		 */
		if (updatedUserOrgBranchMappings.isEmpty()
				&& updatedUserOrgBranchProductMappings.isEmpty()) {
			return;
		}

		// insert new branches mapped to user - including branch admin

		/* Adding New UserOrgBranchMapping */
		Map<Long, UserOrgBranchMapping> userOrgBranchMap = new HashMap<Long, UserOrgBranchMapping>();
		Map<Long, List<UserOrgBranchProdMapping>> userOrgBranchProdMap = new HashMap<Long, List<UserOrgBranchProdMapping>>();
		
		 
		processUserOrgMappings(updatedUserOrgBranchMappings,userOrgBranchMap,
				userOrgBranchProdMap, updatedUser,originalUser, defaultBranch, approvalStatusBeforeUpdate);
		
		/* Adding New UserOrgBranchProdMapping */
		processUserOrgProdMappings(updatedUserOrgBranchProductMappings, userOrgBranchMap,
				userOrgBranchProdMap, updatedUser, originalUser, defaultBranch, approvalStatusBeforeUpdate);
		
		/* checking change of default branch and if changed than added old default branch entry as modified */
		if(ValidatorUtils.notNull(originalUser)){
			updateExistingPrimaryBranch(userOrgBranchMap, defaultBranch, originalUser, updatedUser);
		}
		
		if(primaryBranchToBeUpdatedForUpdatedUser){
			updateExistingPrimaryBranchForUpdatedUser(userOrgBranchMap, defaultBranch, updatedUser);
		}
		
		updateUserOrgBranchAndProdMappings(userOrgBranchMap ,userOrgBranchProdMap);
	}

    /**
     * 3, 4 & 5. Add or update branches, branch admin & User-Branch-Products
     * mapped to user
     *
     * @param updatedUserOrgBranchMappings
     * @param updatedUserOrgBranchProductMappings
     * @param defaultBranch
     * @param updatedUser
     * @param originalUser
     * @param approvalStatusBeforeUpdate
     */
    protected void updateBranchesAndProductsMappingAtMakerStageModified(
            List<Map<String, Object>> updatedUserOrgBranchMappings,
            Map<String,String> updatedUserOrgBranchProductMappings,
            Long defaultBranch, User updatedUser,User originalUser,
            Integer approvalStatusBeforeUpdate, boolean primaryBranchToBeUpdatedForUpdatedUser) {

        NeutrinoValidator.notNull(updatedUser, "User cannot be null");

        /*
         * If none of the branches and products are selected and current user
         * didn't had any prior mappings
         */
        if (updatedUserOrgBranchMappings.isEmpty()
                && updatedUserOrgBranchProductMappings.isEmpty()) {
            return;
        }

        // insert new branches mapped to user - including branch admin

        /* Adding New UserOrgBranchMapping */
        Map<Long, UserOrgBranchMapping> userOrgBranchMap = new HashMap<Long, UserOrgBranchMapping>();
        Map<Long, List<UserOrgBranchProdMapping>> userOrgBranchProdMap = new HashMap<Long, List<UserOrgBranchProdMapping>>();


        processUserOrgMappings(updatedUserOrgBranchMappings,userOrgBranchMap,
                userOrgBranchProdMap, updatedUser,originalUser, defaultBranch, approvalStatusBeforeUpdate);

        /* Adding New UserOrgBranchProdMapping */
        processUserOrgProdMappingsModified(updatedUserOrgBranchProductMappings, userOrgBranchMap,
                userOrgBranchProdMap, updatedUser, originalUser, defaultBranch, approvalStatusBeforeUpdate);

        /* checking change of default branch between ORIGINAL USER's STATE and the default branch passed in VO.
        If changed then add ORIGINAL USER's entry as modfied and with primary branch as FALSE.
        Or If changed then update ORIGINAL USER's entry with primary branch as FALSE.*/
        if(ValidatorUtils.notNull(originalUser)){
            updateExistingPrimaryBranch(userOrgBranchMap, defaultBranch, originalUser, updatedUser);
        }

        /* checking change of default branch between EDITED USER's existing STATE and the default branch passed in VO.
        If changed then update earlier default branch entry with primary branch as FALSE*/
        if(primaryBranchToBeUpdatedForUpdatedUser){
            updateExistingPrimaryBranchForUpdatedUser(userOrgBranchMap, defaultBranch, updatedUser);
        }

        /*Persisting/Updating UserOrgBranchMappings and UserOrgBranchProdMapping to DB*/
        updateUserOrgBranchAndProdMappings(userOrgBranchMap ,userOrgBranchProdMap);
    }


    /**
	 * 7. Add or update preferences mapped to user
	 *
	 * @param updatedUserPreferences
	 * @param updatedUser
	 * @param approvalStatusBeforeUpdate
	 */
	// TODO enable aftear completion of checker
	protected void updateUserPreferencesAtMakerStage(
			List<ConfigurationVO> updatedUserPreferences, User updatedUser , List<String> myFavs) {
        List<ConfigurationVO> configVOList = new ArrayList<ConfigurationVO>();
        Map<String, ConfigurationVO> userPreferences = configurationService
                .getFinalUserModifiableConfigurationForEntity(updatedUser.getEntityId());
        String temp = null;
        if (updatedUserPreferences != null && !updatedUserPreferences.isEmpty()) {
            for (ConfigurationVO configVO : updatedUserPreferences) {
                ConfigurationVO orgConfig = userPreferences.get(configVO.getPropertyKey());
                if(orgConfig!=null){
                orgConfig.setOverride(configVO.isOverride());
                if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.NORMAL_TEXT.toString())
                        && (myFavs != null)
                        && ("config.notifications.myFavorites".equalsIgnoreCase(orgConfig.getPropertyKey()))) {

                    temp = convertListToString(myFavs);
                    orgConfig.setText(temp);
                }

                else if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.NORMAL_TEXT.toString())
                        && configVO.getText() != null) {
                    orgConfig.setText(configVO.getText());
                }

                else if ((orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DATE.toString()) || orgConfig
                        .getValueType().toString().equalsIgnoreCase(ValueType.TIME.toString()))
                        && (configVO.getDate() != null)) {
                    orgConfig.setDate(configVO.getDate());
                } else if ((orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DATE_RANGE.toString()) || orgConfig
                        .getValueType().toString().equalsIgnoreCase(ValueType.TIME_RANGE.toString()))
                        && (configVO.getFromDate() != null && configVO.getToDate() != null)) {
                    orgConfig.setFromDate(configVO.getFromDate());
                    orgConfig.setToDate(configVO.getToDate());
                } else if ((orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DAY_OF_WEEK.toString()))
                        && (configVO.getDay() != null)) {
                    orgConfig.setDay(configVO.getDay());
                } else if ((orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.DAYS_OF_WEEK_RANGE.toString()))
                        && (configVO.getFromDay() != null && configVO.getToDay() != null)) {
                    orgConfig.setFromDay(configVO.getFromDay());
                    orgConfig.setToDay(configVO.getToDay());
                } else if (orgConfig.getValueType().toString().equalsIgnoreCase(ValueType.BOOLEAN_VALUE.toString())) {
                    orgConfig.setConfigurable(configVO.isConfigurable());
                }

                configVOList.add(orgConfig);
                }
            }
            configurationService.syncConfiguration(updatedUser.getEntityId(), configVOList);
        }
    }
	
	/**
	 * 
	 * 8. Add or update teams mapped to user
	 * 
	 * @param teamIds
	 * @param updatedUser
	 */
	protected void updateTeamsMappingAtMakerStage(User updatedUser, Long[] teamIds) {
		saveTeamsForUser(updatedUser,teamIds);
	}
	
	/**
	 * 
	 * 9. Add or update business partner mapped to user
	 * 
	 * @param updatedUser
	 * @param mappedBPId
	 * @param isUserTeamLead
	 */
	public void updateBusinessPartnerMappingAndTeamMappingAtMakerStage(User updatedUser, Long mappedBPId, Boolean isUserTeamLead) {

		//Add or update business partner mapped to user
		if (null != mappedBPId) {
			userService.mapUserToBusinessPartner(mappedBPId, updatedUser);
		}
        
        //create or update team 
        if (isUserTeamLead) {
            teamService.checkIfTeamExistOrNot(updatedUser);
        }
	}
	
	/**
	 * @param userId
	 * @param organizationBranchId
	 * @return persistedUserOrgBranchMapList
	 */
	protected List<UserOrgBranchMapping> findOrgBranchMappingsByUserAndBranches(Long userId, Long organizationBranchId)
	{
		NamedQueryExecutor<UserOrgBranchMapping> executor = new NamedQueryExecutor<UserOrgBranchMapping>(
				QUERY_FOR_FETCHING_USER_ORG_BRANCHES_BY_BRANCHE_USER);
		executor.addParameter("userID", userId);
		executor.addParameter("branchID", organizationBranchId);
		List<UserOrgBranchMapping> userBranchMappings = entityDao.executeQuery(executor);
		if(null != userBranchMappings){
			return userBranchMappings;
		}
		return new ArrayList<UserOrgBranchMapping>();
	}
	
	/**
	 * @param changedBranch
	 * @param updatedUser
     * @param originalUser
     * @param defaultBranch
	 * @return userOrgBrnchMapping
     * This method will return UserOrgBranchMapping for the combination of Updated User's ID & Changed Branch's ID
     * As per the changedBranch map's inputs
	 */
	protected UserOrgBranchMapping getOrCreateNewUserOrgBranchMappingObject(Map<String, Object> changedBranch,
                                                                            User updatedUser, User originalUser, Long defaultBranch) {
        Long branchID = Long.parseLong((String) changedBranch.get(SELECTIVE_Id));
        Long userID = updatedUser.getId();
        String operationType = (String) changedBranch.get(SELECTIVE_OPERATION_TYPE);
        /*Get UserOrgBranchMapping from DB for the combination of updated user ID and branch ID - If Exists.
        * This is applicable for the scenario, when an already edited user's UserOrgBranchMapping is re-edited again
        * before sending to approval.
        * Otherwise, new UserOrgBranchMapping is created if not found in DB*/
        List<UserOrgBranchMapping> userOrgBranchMappings = getUserOrgBranchMappingByBranchAndUserID(branchID, userID);
        UserOrgBranchMapping userOrgBranchMapping = null;
        if (userOrgBranchMappings.size() > 0 && ValidatorUtils.notNull(userOrgBranchMappings.get(0))) {
            /*Only in the case when an already edited user's UserOrgBranchMapping is re-edited again*/
            userOrgBranchMapping = userOrgBranchMappings.get(0);
            /*In this case of re-editing, OPERATION TYPE sent from VO need not to be set as it is.
            * Please refer the comments within the method updateOperationTypeForAlreadyUpdatedUser
            * for understanding all the OPERATION TYPES.*/
            updateOperationTypeForAlreadyUpdatedUser(userOrgBranchMapping, operationType, originalUser, branchID);
        } else {
            /*In the case when an UserOrgBranchMapping is edited for the first time*/
            userOrgBranchMapping = new UserOrgBranchMapping();
            /*OPERATION TYPE to set is same as Operation Type passsed in VO*/
            userOrgBranchMapping.setOperationType(operationType);
        }
        /*Checking whether Default Branch's ID sent from VO is same as Branch ID of the current Changed Branch*/
        if (ValidatorUtils.notNull(defaultBranch)
                && defaultBranch.equals(branchID)) {
            userOrgBranchMapping.setPrimaryBranch(Boolean.TRUE);
        } else {
            userOrgBranchMapping.setPrimaryBranch(Boolean.FALSE);
        }
        userOrgBranchMapping.setAssociatedUser(updatedUser);
        userOrgBranchMapping.setOrganizationBranchId(branchID);
        userOrgBranchMapping.setActiveFlag(true);
        userOrgBranchMapping.setBranchAdmin((Boolean) changedBranch.get(SELECTIVE_IS_ADMIN));

        return userOrgBranchMapping;
    }

    /**
     *
     * @param userOrgBranchMapping
     * @param operationType
     * @param originalUser
     * @param branchID
     * This is only applicable for userOrgBranchMapping which is for already EDITED USER (Approval Status 7 or 8).
     * This method sets the appropriate OPERATION TYPE to the userOrgBranchMapping object passed as param.
     * The OPERATION TYPE to be set in userOrgBranchMapping need not to be same as operationType passed as param. This happens when editing an already edited user (Approval Status 7 or 8).
     * Please refer the following GRID to understand the possible OPERATION TYPE transitions.
     *
         * APPROVED OPERATION TYPE  -   EDITED USER OPERATION TYPE  -   PASSED OPERATION TYPE   -   OPERATION TYPE TO SET
         *          NA (WON'T COME) -               A               -               A           -               A
         *          NA              -               A               -               M           -               A
         *          NA              -               A               -               D           -               D
         *          NA (WON'T COME) -               M               -               A           -               M
         *          NA              -               M               -               M           -               M
         *          NA              -               M               -               D           -               D
         *          NA (WON'T COME) -               D               -               D           -               D
         *          D               -               D               -               A           -               A
         *          D  (WON'T COME) -               D               -               M           -               A
         *          A               -               D               -               A           -               M
         *          A  (WON'T COME) -               D               -               M           -               M
         *          M               -               D               -               A           -               M
         *          M  (WON'T COME) -               D               -               M           -               M
     *
     */
    private void updateOperationTypeForAlreadyUpdatedUser(UserOrgBranchMapping userOrgBranchMapping, String operationType, User originalUser, Long branchID) {
        if ((userOrgBranchMapping.getOperationType().equals(SelectiveMapping.ADDITION_OPERATION) ||
                userOrgBranchMapping.getOperationType().equals(SelectiveMapping.MODIFICATION_OPERATION))
                && operationType.equals(SelectiveMapping.DELETION_OPERATION)) {
            userOrgBranchMapping.setOperationType(SelectiveMapping.DELETION_OPERATION);
        } else if (userOrgBranchMapping.getOperationType().equals(SelectiveMapping.DELETION_OPERATION) &&
                !operationType.equals(SelectiveMapping.DELETION_OPERATION)) {
            if (originalUser != null) {
                List<UserOrgBranchMapping> originalUserOrgBranchMappings = getUserOrgBranchMappingByBranchAndUserID(branchID, originalUser.getId());
                UserOrgBranchMapping originalUserOrgBranchMapping = (originalUserOrgBranchMappings.size() > 0 ? originalUserOrgBranchMappings.get(0) : null);
                if (originalUserOrgBranchMapping == null) {
                    userOrgBranchMapping.setOperationType(SelectiveMapping.ADDITION_OPERATION);
                } else {
                    userOrgBranchMapping.setOperationType(SelectiveMapping.MODIFICATION_OPERATION);
                }
            } else {
                userOrgBranchMapping.setOperationType(SelectiveMapping.ADDITION_OPERATION);
            }
        }
    }

	
	/**
	 * @param changedBranch
	 * @param updatedUser
	 * @return userOrgBrnchMapping
	 */
	protected UserOrgBranchMapping copyUserOrgBranchMappingToNewObject(Long branchId, User updatedUser,User originalUser){

        UserOrgBranchMapping userOrgBranchMapping = null;
		List<UserOrgBranchMapping> originalOrgBranchMappings = findOrgBranchMappingsByUserAndBranches(originalUser.getId(), branchId);
        if(!originalOrgBranchMappings.isEmpty())
		{
            UserOrgBranchMapping originalOrgBranchMapping = originalOrgBranchMappings.get(0);
            List<UserOrgBranchMapping> userOrgBranchMappings = getUserOrgBranchMappingByBranchAndUserID(branchId, updatedUser.getId());

            if (userOrgBranchMappings.size() > 0 && ValidatorUtils.notNull(userOrgBranchMappings.get(0))) {
                userOrgBranchMapping = userOrgBranchMappings.get(0);
            } else {
                userOrgBranchMapping = new UserOrgBranchMapping();
                userOrgBranchMapping
                        .setOperationType(SelectiveMapping.MODIFICATION_OPERATION);
            }

            userOrgBranchMapping.setAssociatedUser(updatedUser);
            userOrgBranchMapping.setOrganizationBranchId(originalOrgBranchMapping.getOrganizationBranchId());
            userOrgBranchMapping.setActiveFlag(true);
            userOrgBranchMapping.setBranchAdmin(originalOrgBranchMapping.isBranchAdmin());
            userOrgBranchMapping.setPrimaryBranch(originalOrgBranchMapping.isPrimaryBranch());
			
		}
		return userOrgBranchMapping;
	}
	
	
	/**
	 * @param existingUserOrgBranchProdMapping
	 * @return
	 */
	protected UserOrgBranchProdMapping createNewUserOrgBranchProductMappingObject(UserOrgBranchProdMapping existingUserOrgBranchProdMapping){
		
		UserOrgBranchProdMapping userOrgBrnchProdMapping = new UserOrgBranchProdMapping();
		userOrgBrnchProdMapping.setActiveFlag(existingUserOrgBranchProdMapping.isActiveFlag());
		//TODO make LoanProduct plugable
		//		userOrgBrnchProdMapping.setLoanProduct(existingUserOrgBranchProdMapping.getLoanProduct());
		userOrgBrnchProdMapping.setLoanProductId(existingUserOrgBranchProdMapping.getLoanProductId());
		userOrgBrnchProdMapping.setTenantId(existingUserOrgBranchProdMapping.getTenantId());
		userOrgBrnchProdMapping.setUserOrgBranchMappingId(existingUserOrgBranchProdMapping.getUserOrgBranchMappingId());
		return userOrgBrnchProdMapping;
	}
	
	
/**
 * @param product
 * @param operationType
 * @return
 */
protected UserOrgBranchProdMapping createNewUserOrgBranchProductMappingObject(Long product, String operationType ){
		
		UserOrgBranchProdMapping userOrgBrnchProdMapping = new UserOrgBranchProdMapping();
		userOrgBrnchProdMapping.setOperationType(operationType);
		userOrgBrnchProdMapping.setLoanProductId(product);
		return userOrgBrnchProdMapping;
	}
	
	/**
	 * @param changedBranch
	 * @param updatedUser
	 */
	protected void updateExistingPrimaryBranch(Map<Long, UserOrgBranchMapping> userOrgBranchMap, Long defaultBranch,  User originalUser, User updatedUser)
 {
		List<OrganizationBranch> userPrimaryBranches = userManagementServiceCore
				.getUserPrimaryBranch(originalUser.getId());
		UserOrgBranchMapping orgBranchMapping;
		if (null != userPrimaryBranches && !userPrimaryBranches.isEmpty()) {
            OrganizationBranch organizationBranch = userPrimaryBranches.get(0);
            /*Comparing DEFAULT BRANCH ID sent from VO with DEFAULT BRANCH Set in the ORIGINAL LAST APPROVED USER*/
            if (ValidatorUtils.notNull(defaultBranch) && !defaultBranch.equals(organizationBranch.getId())) {
                if (null != userOrgBranchMap.get(organizationBranch.getId())) {
                    orgBranchMapping = userOrgBranchMap
                            .get(organizationBranch.getId());
                } else {
                    orgBranchMapping = copyUserOrgBranchMappingToNewObject(
                            organizationBranch.getId(), updatedUser, originalUser);
                }
                orgBranchMapping.setPrimaryBranch(Boolean.FALSE);
                userOrgBranchMap.put(organizationBranch.getId(), orgBranchMapping);
            }
        }

	}
		
	/**
	 * 
	 * @param userOrgBranchMap
	 * @param defaultBranch
	 * @param originalUser
	 * @param updatedUser
	 */
	protected void updateExistingPrimaryBranchForUpdatedUser(
			Map<Long, UserOrgBranchMapping> userOrgBranchMap,
			Long defaultBranch, User updatedUser) {
		List<OrganizationBranch> userPrimaryBranches = userManagementServiceCore.getUserPrimaryBranch(updatedUser.getId());
		if (ValidatorUtils.notNull(defaultBranch) && null != userPrimaryBranches && !userPrimaryBranches.isEmpty()) {
		    /*This loop will execute at max twice*/
		    for(OrganizationBranch organizationBranch : userPrimaryBranches){
                /*Comparing DEFAULT BRANCH ID sent from VO with DEFAULT BRANCH Set in the EDITED USER*/
                if (!defaultBranch.equals(organizationBranch.getId())) {
                    updatePrimaryBranchForUpdatedUser(userOrgBranchMap, organizationBranch.getId(), updatedUser.getId());
                }
            }
		}
	}
	
	private void updatePrimaryBranchForUpdatedUser(
			Map<Long, UserOrgBranchMapping> userOrgBranchMap,
			Long organizationBranchId, Long updatedUserId) {
        UserOrgBranchMapping userOrgBranchMapping = null;
        if (null != userOrgBranchMap.get(organizationBranchId)) {
            userOrgBranchMapping = userOrgBranchMap.get(organizationBranchId);
            userOrgBranchMapping.setPrimaryBranch(Boolean.FALSE);
        } else {
            List<UserOrgBranchMapping> updatedOrgBranchMappings = findOrgBranchMappingsByUserAndBranches(updatedUserId, organizationBranchId);
            if (!updatedOrgBranchMappings.isEmpty()) {
                userOrgBranchMapping = updatedOrgBranchMappings.get(0);
                userOrgBranchMapping.setPrimaryBranch(Boolean.FALSE);
            }
        }
        userOrgBranchMap.put(organizationBranchId, userOrgBranchMapping);
    }
		
	/**
	 * @param userId
	 * @param organizationBranchId
	 * @return
	 */
	protected List<UserOrgBranchProdMapping> findOrgBranchProdMappingsByUserAndBranches(Long userId, Long organizationBranchId)
	{
		NamedQueryExecutor<UserOrgBranchProdMapping> executor = new NamedQueryExecutor<UserOrgBranchProdMapping>(
				QUERY_FOR_FETCHING_USER_ORG_BRANCHES_PRODUCT_BY_USER_BRANCHES);
		executor.addParameter("userId", userId);
		executor.addParameter("orgBranchMappingId", organizationBranchId);
		List<UserOrgBranchProdMapping> branchProdMappings = entityDao.executeQuery(executor);
		if(null != branchProdMappings){
			return branchProdMappings;
		}
		
		return new ArrayList<UserOrgBranchProdMapping>();
	}	
   
	/**
	 * updating userOrgBranch and userOrgBranchProduct info on userUpdate
	 * @param userOrgBranchMap
	 * @param userOrgBranchProdMap
	 */
	protected void updateUserOrgBranchAndProdMappings(Map<Long, UserOrgBranchMapping> userOrgBranchMap, Map<Long, List<UserOrgBranchProdMapping>> userOrgBranchProdMap)
    {
		for (Entry<Long, UserOrgBranchMapping> branchId : userOrgBranchMap
				.entrySet()) {
			UserOrgBranchMapping branchMapping = userOrgBranchMap.get(branchId
					.getKey());
            userManagementDao.saveOrUpdate(branchMapping);
			List<UserOrgBranchProdMapping> branchProdMappings = userOrgBranchProdMap
					.get(branchId.getKey());
			if (null != branchProdMappings) {
				for (UserOrgBranchProdMapping branchProdMapping : branchProdMappings) {
					branchProdMapping.setUserOrgBranchMappingId(branchMapping.getId());
					userManagementDao.saveOrUpdate(branchProdMapping);
				}
			}

		}
	}
	
	
	/**
	 * @param updatedUserOrgBranchMappings
	 * @param userOrgBranchMap
	 * @param userOrgBranchProdMap
	 * @param updatedUser
	 * @param originalUser
	 * @param defaultBranch
	 * @param approvalStatusBeforeUpdate
	 */
	protected void processUserOrgMappings(List<Map<String, Object>> updatedUserOrgBranchMappings, Map<Long, UserOrgBranchMapping> userOrgBranchMap,
			Map<Long, List<UserOrgBranchProdMapping>> userOrgBranchProdMap, User updatedUser,User originalUser, Long defaultBranch, Integer approvalStatusBeforeUpdate)
	{
	
		for (Map<String, Object> changedBranch : updatedUserOrgBranchMappings) {

			UserOrgBranchMapping userOrgBranchMapping = getOrCreateNewUserOrgBranchMappingObject(
					changedBranch, updatedUser, originalUser, defaultBranch);

			userOrgBranchMap.put(Long.parseLong((String)changedBranch.get(SELECTIVE_Id)),
					userOrgBranchMapping);

			if (SelectiveMapping.DELETION_OPERATION.equals(changedBranch
					.get(SELECTIVE_OPERATION_TYPE))) {
				
					Long userIdToFetchPreviouslyApprovedList=null;
					
					if (null != originalUser){
						userIdToFetchPreviouslyApprovedList=originalUser.getId();
					}
					Long userIdToFetchUnapprovedList=updatedUser.getId();
					
					
					List<UserOrgBranchProdMapping> toBeDelUserOrgProdMappings = new ArrayList<UserOrgBranchProdMapping>();
					
					
					if(userIdToFetchPreviouslyApprovedList!=null){
						List<UserOrgBranchProdMapping> lastApprovedUserOrgProdMappings =findOrgBranchProdMappingsByUserAndBranches(userIdToFetchPreviouslyApprovedList, Long.parseLong((String) changedBranch.get(SELECTIVE_Id)));
						for (UserOrgBranchProdMapping lastApprovedUserOrgProdMapping : lastApprovedUserOrgProdMappings) {
							UserOrgBranchProdMapping newUserOrgProdMapping = createNewUserOrgBranchProductMappingObject(lastApprovedUserOrgProdMapping);
							newUserOrgProdMapping
									.setOperationType(SelectiveMapping.DELETION_OPERATION);
							toBeDelUserOrgProdMappings.add(newUserOrgProdMapping);
						}
					}
					userOrgBranchProdMap.put(
							Long.parseLong((String)changedBranch.get(SELECTIVE_Id)),
							toBeDelUserOrgProdMappings);
					
					//Deleting existing record in selectiveUserOrgBranchMapping 
					List<UserOrgBranchProdMapping> lastUpdatedUserOrgProdMappings = findOrgBranchProdMappingsByUserAndBranches(userIdToFetchUnapprovedList, Long.parseLong((String) changedBranch.get(SELECTIVE_Id)));
					if(!lastUpdatedUserOrgProdMappings.isEmpty())
					{
					for (UserOrgBranchProdMapping  lastUpdatedUserOrgProdMapping: lastUpdatedUserOrgProdMappings) {
						userManagementDao.delete(lastUpdatedUserOrgProdMapping);
					}
					userManagementDao.delete(lastUpdatedUserOrgProdMappings.get(0).getUserOrgBranchMapping());
					}
				
				}
			}
	}
	
	
	/**
	 * @param updatedUserOrgBranchProductMappings
	 * @param userOrgBranchMap
	 * @param userOrgBranchProdMap
	 * @param updatedUser
	 * @param originalUser
	 * @param defaultBranch
	 * @param approvalStatusBeforeUpdate
	 */
	protected void processUserOrgProdMappings(Map<Long, List<Map<Long, String>>> updatedUserOrgBranchProductMappings, Map<Long, UserOrgBranchMapping> userOrgBranchMap,
			Map<Long, List<UserOrgBranchProdMapping>> userOrgBranchProdMap, User updatedUser,User originalUser, Long defaultBranch, Integer approvalStatusBeforeUpdate)
	{
		UserInfo currentLoggedInUser = getCurrentUser();
        List<Long> currentLoggedInUserAccessibleBranchesIds =  null ;
        		
		if(currentLoggedInUser != null && currentLoggedInUser.getSysName() != null){
			Map<OrganizationBranch, Long> accessibleBranches = userManagementServiceCore.getOrgBranchesWithChildCountUnderCurrentUserByOrganizationType(currentLoggedInUser.getId(),currentLoggedInUser.getSysName().getCode(),OrganizationType.ORGANIZATION_TYPE_BRANCH);
			if(!accessibleBranches.isEmpty()){
				currentLoggedInUserAccessibleBranchesIds = new ArrayList<Long>();
				for(OrganizationBranch organizationBranch:accessibleBranches.keySet()){
					currentLoggedInUserAccessibleBranchesIds.add(organizationBranch.getId());
				}
			}
			
		}
		for( Entry<Long, List<Map<Long, String>>> productMappingEntry: updatedUserOrgBranchProductMappings.entrySet())
        {   
              for(Map<Long,String> branchMap: updatedUserOrgBranchProductMappings.get(productMappingEntry.getKey()))
              {   
          	  	Long branchId = null;
            		boolean eligibleToDeleteOrgBranchProdMapping = true;

            	     for(Entry<Long, String> branchEntry : branchMap.entrySet())
                     {
                    	 branchId = branchEntry.getKey();
                    	 if(SelectiveMapping.DELETION_OPERATION.equals((String)branchEntry.getValue())){
            	    		 eligibleToDeleteOrgBranchProdMapping = isCurrentLoggedInUserEligibleToDeleteOrgBranchProdMapping(currentLoggedInUserAccessibleBranchesIds,branchId);
            	    	 }
        	    	 	 
                    	 if(eligibleToDeleteOrgBranchProdMapping){
                    		 UserOrgBranchProdMapping userOrgBrnchProdMapping = createNewUserOrgBranchProductMappingObject((Long)productMappingEntry.getKey(),
                        			 (String)branchEntry.getValue());
                    		 if(userOrgBranchProdMap.get(branchId)==null){
	                    		 userOrgBranchProdMap.put(branchId,new ArrayList<UserOrgBranchProdMapping>());
	                    	 }
	                    	 userOrgBranchProdMap.get(branchId).add(userOrgBrnchProdMapping);
                    	 }
                    	 
                     }
                     
                     if (null == userOrgBranchMap.get(branchId) && eligibleToDeleteOrgBranchProdMapping)
                     {
                    	 if(originalUser == null) {
                    		 originalUser = updatedUser;
                    	 }
                    	 UserOrgBranchMapping userOrgBrnchMapping = copyUserOrgBranchMappingToNewObject(branchId, updatedUser, originalUser);
                    	 if(userOrgBrnchMapping != null ){
                    		 userOrgBrnchMapping.setOperationType(SelectiveMapping.MODIFICATION_OPERATION);
                        	 userOrgBranchMap.put(branchId, userOrgBrnchMapping);
                    	 }                      	 
                     }
              }
        }
	}




    /**
     * @param updatedUserOrgBranchProductMappings
     * @param userOrgBranchMap
     * @param userOrgBranchProdMap
     * @param updatedUser
     * @param originalUser
     * @param defaultBranch
     * @param approvalStatusBeforeUpdate
     */
    protected void processUserOrgProdMappingsModified(Map<String,String> updatedUserOrgBranchProductMappings, Map<Long, UserOrgBranchMapping> userOrgBranchMap,
                                                      Map<Long, List<UserOrgBranchProdMapping>> userOrgBranchProdMap, User updatedUser,User originalUser, Long defaultBranch, Integer approvalStatusBeforeUpdate)
    {
        UserInfo currentLoggedInUser = getCurrentUser();
        List<Long> accessibleBranches=null;
        if(currentLoggedInUser != null && currentLoggedInUser.getSysName() != null){
            accessibleBranches = userManagementServiceCore.getOrgBranchesWithCurrentUserByOrganizationType(currentLoggedInUser.getId(),OrganizationType.ORGANIZATION_TYPE_BRANCH);
        }
        Long branchId=null;
        Long productId=null;
        String operationType="";
        boolean eligibleToDeleteOrgBranchProdMapping = true;
        for( Entry<String,String> productMappingEntry: updatedUserOrgBranchProductMappings.entrySet()) {
        	eligibleToDeleteOrgBranchProdMapping = true;
            branchId = Long.parseLong(productMappingEntry.getKey().split("-")[1]);
            productId = Long.parseLong(productMappingEntry.getKey().split("-")[0]);
            operationType =productMappingEntry.getValue();
            if (SelectiveMapping.DELETION_OPERATION.equals(operationType)) {
                eligibleToDeleteOrgBranchProdMapping = isCurrentLoggedInUserEligibleToDeleteOrgBranchProdMapping(accessibleBranches, branchId);
            }
            if (eligibleToDeleteOrgBranchProdMapping) {
                UserOrgBranchProdMapping userOrgBrnchProdMapping = createNewUserOrgBranchProductMappingObject(productId,
                        operationType);
                if (userOrgBranchProdMap.get(branchId) == null) {
                    userOrgBranchProdMap.put(branchId, new ArrayList<UserOrgBranchProdMapping>());
                }
                userOrgBranchProdMap.get(branchId).add(userOrgBrnchProdMapping);
            }


            if (null == userOrgBranchMap.get(branchId) && eligibleToDeleteOrgBranchProdMapping) {
                if (originalUser == null) {
                    originalUser = updatedUser;
                }
                UserOrgBranchMapping userOrgBrnchMapping = copyUserOrgBranchMappingToNewObject(branchId, updatedUser, originalUser);
                if (userOrgBrnchMapping != null) {
                    userOrgBrnchMapping.setOperationType(SelectiveMapping.MODIFICATION_OPERATION);
                    userOrgBranchMap.put(branchId, userOrgBrnchMapping);
                }
            }
        }

    }



    private boolean isCurrentLoggedInUserEligibleToDeleteOrgBranchProdMapping(List<Long> currentLoggedInUserAccessibleBranchesIds, Long branchId) {
		
		if(currentLoggedInUserAccessibleBranchesIds != null && ! currentLoggedInUserAccessibleBranchesIds.isEmpty() 
				&& ! currentLoggedInUserAccessibleBranchesIds.contains(branchId)){
			return false;
		}

		return true;
	}
	/* (non-Javadoc)
	 * @see com.nucleus.cas.businessmapping.UserManagementService#findOrgBranchProdMappingsByUserOrgBranche(java.lang.Long)
	 */
	@Override
	public List<UserOrgBranchProdMapping> findOrgBranchProdMappingsByUserOrgBranche(Long userOrganizationBranchId)
	{
		NamedQueryExecutor<UserOrgBranchProdMapping> executor = new NamedQueryExecutor<UserOrgBranchProdMapping>(
				QUERY_FOR_FETCHING_USER_ORG_BRANCHES_PRODUCT_BY_USER_ORG_BRANCHE_ID);
		executor.addParameter("userOrgBranchMappingId", userOrganizationBranchId);
		List<UserOrgBranchProdMapping> branchProdMappings = entityDao.executeQuery(executor);
		if(null != branchProdMappings){
			return branchProdMappings;
		}
		
		return new ArrayList<UserOrgBranchProdMapping>();
	}

    @Override
    public List<UserOrgBranchProdMapping> findAllOrgBranchProdMappingsByUserOrgBranche(List<Long> userOrganizationBranchId)
    {

        List<UserOrgBranchProdMapping> branchProdMappings = executeSingleInClauseHQLQuery(QUERY_TO_FETCHING_USER_ORG_BRANCHES_PRODUCT_BY_USER_ORG_BRANCHE_ID,"userOrgBranchMappingIds",userOrganizationBranchId);
        if(null != branchProdMappings){
            return branchProdMappings;
        }

        return new ArrayList<UserOrgBranchProdMapping>();
    }


    @Override
	public List<UserOrgBranchProdMapping> findOrgBranchProdMappingsByProductAndAndBranchMapping(Long loanProductId, Long userOrganizationBranchId)
	{
		NamedQueryExecutor<UserOrgBranchProdMapping> executor = new NamedQueryExecutor<UserOrgBranchProdMapping>(
				QUERY_FOR_FETCHING_USER_ORG_BRANCHES_PRODUCT_BY_PRODUCT_USER_ORG_BRANCHE);
		executor.addParameter("loanProductId", loanProductId);
		executor.addParameter("userOrgBranchMappingId", userOrganizationBranchId);
		List<UserOrgBranchProdMapping> branchProdMappings = entityDao.executeQuery(executor);
		if(null != branchProdMappings){
			return branchProdMappings;
		}
		
		return new ArrayList<UserOrgBranchProdMapping>();
	}
	
	/**
	 * 
	 * @param userID
	 * @return
	 */
	 private List<UserOrgBranchProdMapping> getUserOrgBranchProductMappingList(Long userID) {
		 
		NamedQueryExecutor<UserOrgBranchProdMapping> executorUserOrgBranchMappings = new NamedQueryExecutor<UserOrgBranchProdMapping>(
	                QUERY_FOR_FETCHING_USER_BRANCHES).addParameter("userID", userID);
	    List<UserOrgBranchProdMapping> userOrgBranchMappingList = entityDao.executeQuery(executorUserOrgBranchMappings);
		if (null != userOrgBranchMappingList) {
			return userOrgBranchMappingList;
		}
		return new ArrayList<UserOrgBranchProdMapping>();
	 }
	 
	 /**
	  * 
	  * @param myList
	  * @return
	  */
	 private String convertListToString(List<String> myList) {
	        StringBuilder newString = new StringBuilder();
	        for (Iterator<String> it = myList.iterator() ; it.hasNext() ;) {
	            newString.append(it.next());
	            if (it.hasNext()) {
	                newString.append(",");
	            }
	        }
	        return newString.toString();
	    }
	 
	 /**
	  * 
	  * @param teamIds
	  * @param updatedUser
	  */
	 @SuppressWarnings("unchecked")
	 @Override
	 public void saveTeamsForUser(User updatedUser, Long[] teamIds) {
		 
			// for saving teams mapped to user
			if (teamIds != null && teamIds.length > 0) {
				for (int i = 0; i < teamIds.length; i++) {
					Team team = teamService.getTeamByTeamId(teamIds[i]);
					Set<User> userSet = team.getUsers();
					if (!userSet.contains(updatedUser)) {
						userSet.add(updatedUser);
						team.setUsers(userSet);
					}
					teamService.saveTeamsForUser(team);
				}

			}

			// find teams associated with current user,added to update team of user
			List<Long> persistedTeamIds = teamService.getTeamIdAssociatedToUserByUserId(updatedUser.getId());
			if (teamIds != null && teamIds.length > 0) {
				Long[] unpersistedTeamArraysIds = teamIds;
				List<Long> unpersistedTeamIdsList = Arrays.asList(unpersistedTeamArraysIds);
				if (CollectionUtils.isNotEmpty(unpersistedTeamIdsList) && CollectionUtils.isNotEmpty(persistedTeamIds)) {
					List<Long> newlyAddedTeamIds = (List<Long>) CollectionUtils.disjunction(persistedTeamIds, unpersistedTeamIdsList);
					removeOldUsersFromTeam(newlyAddedTeamIds, updatedUser);
				}
			}
			if(!(updatedUser.isLoginEnabled() || updatedUser.isBusinessPartner())){
				removeOldUsersFromTeam(persistedTeamIds, updatedUser);
			}
	 
	 
	 }
	 
	 
	 
	 private void removeOldUsersFromTeam(List<Long> teamIds, User user){
			if (CollectionUtils.isNotEmpty(teamIds)){
				for (Long singleTeamId : teamIds) {
					Team team = teamService.getTeamByTeamId(singleTeamId);
					Set<User> userSet = team.getUsers();
					if (CollectionUtils.isNotEmpty(userSet)) {
						userSet.remove(user);
						team.setUsers(userSet);
					}
					teamService.saveTeamsForUser(team);
				}
			}
		 
		 
		 
	 }
	 
	 
	 @Override
	 public List<RecordComparatorVO> getUserAuditLog(Long originalUserId, Long changedUserId) {
	    	
	        NeutrinoValidator.notNull(originalUserId, "User Audit log cannot be null");
	        NeutrinoValidator.notNull(changedUserId, "user Id cannot be null");
	        User latestUserAuditLog = entityDao.find(User.class, changedUserId);
	        User previousUserAuditLog = entityDao.find(User.class, originalUserId);
	        List<RecordComparatorVO> recordComparatorList = new ArrayList<RecordComparatorVO>();
	        if (previousUserAuditLog != null) {
	            recordComparatorList = getDifferencesDoneInUser(latestUserAuditLog, previousUserAuditLog);
	        }
	        return recordComparatorList;
	 }
	 
	 @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	 private List<RecordComparatorVO> getDifferencesDoneInUser(User latestUserAuditLog,
	            User previousUserAuditLog) {
	        String latestModifiedString = null;
	        String previousModifiedString = null;
	        UserModificationAuditVO latestModificationAuditVO = null;
	        UserModificationAuditVO previousModificationAuditVO = null;
	        if (latestUserAuditLog != null) {
	            latestModifiedString = populateUserAuditDetailsForLatestUser(latestUserAuditLog,previousUserAuditLog);
	        }
	        if (previousUserAuditLog != null) {
	            previousModifiedString = populateUserAuditDetailsForPreviousUser(previousUserAuditLog);
	        }
	        JSONDeserializer<UserModificationAuditVO> iSerializer = new JSONDeserializer();
	        if (latestModifiedString != null) {
	            latestModificationAuditVO = iSerializer.deserialize(latestModifiedString);
	        }
	        if (previousModifiedString != null) {
	            previousModificationAuditVO = iSerializer.deserialize(previousModifiedString);
	        }
	        return compareInformationForAuditing(latestModificationAuditVO,
	                previousModificationAuditVO);
	 }
	 
	 private List<RecordComparatorVO> compareInformationForAuditing(final UserModificationAuditVO latestModifiesAuditVO,
	            final UserModificationAuditVO previousModificationAuditVO) {
	        final List<RecordComparatorVO> recordComparatorList = new ArrayList<RecordComparatorVO>();
	        ReflectionUtils.doWithFields(UserModificationAuditVO.class, new FieldCallback() {

	            @SuppressWarnings("unchecked")
	            @Override
	            public void doWith(Field field) throws IllegalAccessException {
	                RecordComparatorVO singleRecordComparator = new RecordComparatorVO();

	                ReflectionUtils.makeAccessible(field);
	                String fieldName = field.getName();
	                // if values are equal then no need to log it
	                if (field.getType().equals(String.class)) {
	                    String latestFieldValue = (String) ReflectionUtils.getField(field, latestModifiesAuditVO);
	                    String previousFieldValue = (String) ReflectionUtils.getField(field, previousModificationAuditVO);
	                    if (StringUtils.isEmpty(latestFieldValue) && StringUtils.isEmpty(previousFieldValue)) {
	                        return;
	                    } else if ((latestFieldValue != null && !latestFieldValue.equals(previousFieldValue))
	                            || (previousFieldValue != null && !previousFieldValue.equals(latestFieldValue))) {
	                        singleRecordComparator.setFieldName(fieldName);
	                        singleRecordComparator.setOldValue(previousFieldValue);
	                        singleRecordComparator.setNewValue(latestFieldValue);
	                        recordComparatorList.add(singleRecordComparator);
	                    }

	                } else if (field.getType().equals(Long.class)) {
	                    Long latestFieldValue = (Long) ReflectionUtils.getField(field, latestModifiesAuditVO);
	                    Long previousFieldValue = (Long) ReflectionUtils.getField(field, previousModificationAuditVO);
	                    if ((latestFieldValue != null && !latestFieldValue.equals(previousFieldValue))
	                            || (previousFieldValue != null && !previousFieldValue.equals(latestFieldValue))) {
	                        singleRecordComparator.setFieldName(fieldName);
	                        if (previousFieldValue != null) {
	                            singleRecordComparator.setOldValue(previousFieldValue.toString());
	                        }
	                        singleRecordComparator.setNewValue(latestFieldValue.toString());
	                        recordComparatorList.add(singleRecordComparator);
	                    }
	                }

	                else if (field.getType().equals(Boolean.class)) {
	                    Boolean latestFieldValue = (Boolean) ReflectionUtils.getField(field, latestModifiesAuditVO);
	                    Boolean previousFieldValue = (Boolean) ReflectionUtils.getField(field, previousModificationAuditVO);
	                    if ((latestFieldValue != null && !latestFieldValue.equals(previousFieldValue))
	                            || (previousFieldValue != null && !previousFieldValue.equals(latestFieldValue))) {
	                        singleRecordComparator.setFieldName(fieldName);
	                        if (previousFieldValue != null) {
	                            singleRecordComparator.setOldValue(previousFieldValue.toString());
	                        }
	                        singleRecordComparator.setNewValue(latestFieldValue.toString());
	                        recordComparatorList.add(singleRecordComparator);
	                    }
	                }

	                else if (field.getType().equals(Character.class)) {
	                    Character latestFieldValue = (Character) ReflectionUtils.getField(field, latestModifiesAuditVO);
	                    Character previousFieldValue = (Character) ReflectionUtils.getField(field, previousModificationAuditVO);
	                    if ((latestFieldValue != null && !latestFieldValue.equals(previousFieldValue))
	                            || (previousFieldValue != null && !previousFieldValue.equals(latestFieldValue))) {
	                        singleRecordComparator.setFieldName(fieldName);
	                        if (previousFieldValue != null) {
	                            singleRecordComparator.setOldValue(previousFieldValue.toString());
	                        }
	                        singleRecordComparator.setNewValue(latestFieldValue.toString());
	                        recordComparatorList.add(singleRecordComparator);
	                    }
	                }

	                else if (field.getType().equals(BigDecimal.class)) {
	                    BigDecimal latestFieldValue = (BigDecimal) ReflectionUtils.getField(field, latestModifiesAuditVO);
	                    BigDecimal previousFieldValue = (BigDecimal) ReflectionUtils
	                            .getField(field, previousModificationAuditVO);
	                    if ((latestFieldValue != null && !latestFieldValue.equals(previousFieldValue))
	                            || (previousFieldValue != null && !previousFieldValue.equals(latestFieldValue))) {
	                        singleRecordComparator.setFieldName(fieldName);
	                        if (previousFieldValue != null) {
	                            singleRecordComparator.setOldValue(previousFieldValue.toString());
	                        }
	                        singleRecordComparator.setNewValue(latestFieldValue.toString());
	                        recordComparatorList.add(singleRecordComparator);
	                    }
	                }

	                else if (field.getType().equals(Integer.class)) {
	                    Integer latestFieldValue = (Integer) ReflectionUtils.getField(field, latestModifiesAuditVO);
	                    Integer previousFieldValue = (Integer) ReflectionUtils.getField(field, previousModificationAuditVO);
	                    if ((latestFieldValue != null && !latestFieldValue.equals(previousFieldValue))
	                            || (previousFieldValue != null && !previousFieldValue.equals(latestFieldValue))) {
	                        singleRecordComparator.setFieldName(fieldName);
	                        if (previousFieldValue != null) {
	                            singleRecordComparator.setOldValue(previousFieldValue.toString());
	                        }
	                        singleRecordComparator.setNewValue(latestFieldValue.toString());
	                        recordComparatorList.add(singleRecordComparator);
	                    }
	                }

	                else if (field.getType().isAssignableFrom(ArrayList.class)) {
	                    List<String> latestFieldValue = (List<String>) ReflectionUtils.getField(field, latestModifiesAuditVO);
	                    List<String> previousFieldValue = (List<String>) ReflectionUtils.getField(field,
	                            previousModificationAuditVO);
	                    if (CollectionUtils.isNotEmpty(latestFieldValue) && CollectionUtils.isNotEmpty(previousFieldValue)) {
	                    	List<String> commonList = (List<String>) CollectionUtils.intersection(latestFieldValue,
	                                previousFieldValue);
	                        if (latestFieldValue.size() != previousFieldValue.size()) {
	                            singleRecordComparator.setFieldName(fieldName);
	                            singleRecordComparator.setOldValue(StringUtils.join(previousFieldValue, ","));
	                            singleRecordComparator.setNewValue(StringUtils.join(latestFieldValue, ","));
	                            previousFieldValue.removeAll(commonList);
	                            latestFieldValue.removeAll(commonList);
								singleRecordComparator.setDeletedValue(StringUtils.join(previousFieldValue, ","));
	                            singleRecordComparator.setAddedValue(StringUtils.join(latestFieldValue, ","));
	                            recordComparatorList.add(singleRecordComparator);
	                        } else {
	                            if (CollectionUtils.isNotEmpty(commonList)) {
	                                if (commonList.size() == latestFieldValue.size()) {

	                                } else {
	                                    singleRecordComparator.setFieldName(fieldName);
	                                    singleRecordComparator.setOldValue(StringUtils.join(previousFieldValue, ","));
	                                    singleRecordComparator.setNewValue(StringUtils.join(latestFieldValue, ","));
	                                    previousFieldValue.removeAll(commonList);
	                                    latestFieldValue.removeAll(commonList);
	                                    singleRecordComparator.setDeletedValue(StringUtils.join(previousFieldValue, ","));
	                                    singleRecordComparator.setAddedValue(StringUtils.join(latestFieldValue, ","));
	                                    recordComparatorList.add(singleRecordComparator);
	                                }
	                            }
	                            else{
	                            	singleRecordComparator.setFieldName(fieldName);
	                                singleRecordComparator.setOldValue(StringUtils.join(previousFieldValue, ","));
	                                singleRecordComparator.setNewValue(StringUtils.join(latestFieldValue, ","));
	                                previousFieldValue.removeAll(commonList);
	                                latestFieldValue.removeAll(commonList);
	                                singleRecordComparator.setDeletedValue(StringUtils.join(previousFieldValue, ","));
	                                singleRecordComparator.setAddedValue(StringUtils.join(latestFieldValue, ","));
	                                recordComparatorList.add(singleRecordComparator);
	                            }
	                        }
	                    } else if (CollectionUtils.isNotEmpty(latestFieldValue) && CollectionUtils.isEmpty(previousFieldValue)) {
	                        singleRecordComparator.setFieldName(fieldName);
	                        singleRecordComparator.setOldValue(null);
	                        singleRecordComparator.setNewValue(StringUtils.join(latestFieldValue, ","));
	                        singleRecordComparator.setDeletedValue(null);
	                        singleRecordComparator.setAddedValue(StringUtils.join(latestFieldValue, ","));
	                        recordComparatorList.add(singleRecordComparator);
	                    } else if (CollectionUtils.isEmpty(latestFieldValue) && CollectionUtils.isNotEmpty(previousFieldValue)) {
	                        singleRecordComparator.setFieldName(fieldName);
	                        singleRecordComparator.setOldValue(StringUtils.join(previousFieldValue, ","));
	                        singleRecordComparator.setNewValue(null);
	                        singleRecordComparator.setDeletedValue(StringUtils.join(previousFieldValue, ","));
	                        singleRecordComparator.setAddedValue(null);
	                        recordComparatorList.add(singleRecordComparator);
	                    }
	                }

	            }
	        });
	        return recordComparatorList;

	 }
	 
	 protected String populateUserAuditDetailsForLatestUser(User latestUserAuditLog, User previousUserAuditLog) {
	        UserModificationAuditVO userAuditVO = new UserModificationAuditVO();
	        /*populate Personal Information of user */
	        userAuditVO = populatePersonalInformationForAudit(latestUserAuditLog, userAuditVO);
	        /*populate Address & Communication of User */
	        userAuditVO = populateAddressAndCommunicationDetailsForAudit(latestUserAuditLog, userAuditVO);
	        /*populate Role & Mobility Information of User */
	        userAuditVO = populateRoleTeamAndMobilityInformationForAudit(latestUserAuditLog, userAuditVO);
	        /*populate Branches & Branch Admin & Teams of User */
	        userAuditVO = populateBranchesAndProductForLatestUser(latestUserAuditLog, previousUserAuditLog, userAuditVO);
	        JSONSerializer iSerializer = new JSONSerializer();

	        return iSerializer.deepSerialize(userAuditVO);
	 }
	 
	 protected String populateUserAuditDetailsForPreviousUser(User previousUserAuditLog) {
	        UserModificationAuditVO userAuditVO = new UserModificationAuditVO();
	        /*populate Personal Information of user */
	        userAuditVO = populatePersonalInformationForAudit(previousUserAuditLog, userAuditVO);
	        /*populate Address & Communication of User */
	        userAuditVO = populateAddressAndCommunicationDetailsForAudit(previousUserAuditLog, userAuditVO);
	        /*populate Role & Mobility Information of User */
	        userAuditVO = populateRoleTeamAndMobilityInformationForAudit(previousUserAuditLog, userAuditVO);
	        /*populate Branches & Branch Admin & Teams of User */
	        userAuditVO = populateBranchesAndProductForPreviousUser(previousUserAuditLog, userAuditVO);
	        JSONSerializer iSerializer = new JSONSerializer();

	        return iSerializer.deepSerialize(userAuditVO);
	 }
	 
	 protected UserModificationAuditVO populatePersonalInformationForAudit(User userAuditLog,
	    		UserModificationAuditVO userAuditVO) {
	    	
	            UserProfile userProfile = userService.getUserProfile(userAuditLog);
	            User associatedUser = userAuditLog;
	            if (userProfile != null) {
	                userAuditVO.setFirstName(userProfile.getFirstName());
	                userAuditVO.setMiddleName(userProfile.getMiddleName());
	                userAuditVO.setLastName(userProfile.getLastName());
	                userAuditVO.setFourthName(userProfile.getFourthName());
	                userAuditVO.setAliasName(userProfile.getAliasName());
	                
	                if (userProfile.getAddressRange() != null) {
	                    userAuditVO.setIpAddress(userProfile.getAddressRange().getIpaddress());
	                    userAuditVO.setFromIpAddress(userProfile.getAddressRange().getFromIpAddress());
	                    userAuditVO.setToIpAddress(userProfile.getAddressRange().getToIpAddress());
	                }
	            }
	            if (associatedUser != null) {
	                userAuditVO.setUserName(associatedUser.getUsername());
	                userAuditVO.setPasswordExpiration(associatedUser.getPasswordExpiresInDays());
	                userAuditVO.setEmailId(associatedUser.getMailId());
	                userAuditVO.setIsBusinessPartner(associatedUser.isBusinessPartner());
	                userAuditVO.setIsSupervisor(associatedUser.isSupervisor());
	                userAuditVO.setIsSuperAdmin(associatedUser.isSuperAdmin());
	                userAuditVO.setIsRelationshipOfficer(associatedUser.isRelationshipOfficer());
	                if (associatedUser.getDeviationLevel() != null && associatedUser.getDeviationLevel().getId() != null) {
	                    DeviationLevel deviationLevel = entityDao.find(DeviationLevel.class, associatedUser.getDeviationLevel()
	                            .getId());
	                    if (deviationLevel != null) {
	                        userAuditVO.setDeviationLevel(deviationLevel.getName());
	                    }
	                }
	                if (associatedUser.getSanctionedLimit() != null) {
	                    userAuditVO.setSanctionedAmount(associatedUser.getSanctionedLimit().getBaseAmount().getValue());
	                }
	                userAuditVO.setIsTeamLead(associatedUser.getTeamLead());
	                if (associatedUser.getSysName() != null && associatedUser.getSysName().getId() != null) {
	                    SystemName systemName = genericParameterService.findById(associatedUser.getSysName().getId(),
	                            SystemName.class);
	                    if (systemName != null) {
	                        userAuditVO.setModuleName(systemName.getCode());
	                    }
	                }
	                if (associatedUser.getAccessToAllBranches() != null
	                        && "Y".equalsIgnoreCase(associatedUser.getAccessToAllBranches().toString())) {
	                    userAuditVO.setAccessToAllBranches(associatedUser.getAccessToAllBranches());
	                } else {
	                    userAuditVO.setAccessToAllBranches('N');
	                }
	                if (associatedUser.getAccessToAllProducts() != null
	                        && "Y".equalsIgnoreCase(associatedUser.getAccessToAllProducts().toString())) {
	                    userAuditVO.setAccessToAllProducts(associatedUser.getAccessToAllProducts());
	                } else {
	                    userAuditVO.setAccessToAllProducts('N');
	                }

	            }
	        return userAuditVO;
	 }
	 
	 protected UserModificationAuditVO populateAddressAndCommunicationDetailsForAudit(User userAuditLog,
	            UserModificationAuditVO userAuditVO) {
	    	
	    	    UserProfile userProfile = userService.getUserProfile(userAuditLog);
	            if (userProfile != null) {
	                SimpleContactInfo simpleContactInfo = userProfile.getSimpleContactInfo();
	                if (simpleContactInfo != null) {
	                    Address address = simpleContactInfo.getAddress();
	                    if (address != null) {
	                        if (address.getCountry() != null && address.getCountry().getId() != null) {
	                            Country country = entityDao.find(Country.class, address.getCountry().getId());
	                            if (country != null) {
	                                userAuditVO.setCountry(country.getCountryISOCode());
	                            }
	                        }
	                        userAuditVO.setFlatNumber(address.getAddressLine1());
	                        userAuditVO.setAddressLine2(address.getAddressLine2());
	                        userAuditVO.setAddressLine3(address.getAddressLine3());
	                        if (address.getRegion() != null && address.getRegion().getId() != null) {
	                            IntraCountryRegion intraCountryRegion = entityDao.find(IntraCountryRegion.class, address
	                                    .getRegion().getId());
	                            if (intraCountryRegion != null) {
	                                userAuditVO.setRegion(intraCountryRegion.getIntraRegionCode());
	                            }
	                        }
	                        if (address.getState() != null && address.getState().getId() != null) {
	                            State state = entityDao.find(State.class, address.getState().getId());
	                            if (state != null) {
	                                userAuditVO.setState(state.getStateCode());
	                            }
	                        }
	                        if (address.getCity() != null && address.getCity().getId() != null) {
	                            City city = entityDao.find(City.class, address.getCity().getId());
	                            if (city != null) {
	                                userAuditVO.setCity(city.getCityCode());
	                            }
	                        }
	                        if (address.getDistrict() != null && address.getDistrict().getId() != null) {
	                            District district = entityDao.find(District.class, address.getDistrict().getId());
	                            if (district != null) {
	                                userAuditVO.setDistrict(district.getDistrictCode());
	                            }
	                        }
	                        if (address.getArea() != null && address.getArea().getId() != null) {
	                            Area area = entityDao.find(Area.class, address.getArea().getId());
	                            if (area != null) {
	                                userAuditVO.setArea(area.getAreaCode());
	                            }
	                        }
	                        userAuditVO.setTaluka(address.getTaluka());
	                        userAuditVO.setVillage(address.getVillage());
	                        if (address.getZipcode() != null) {
	                            userAuditVO.setPincode(address.getZipcode().getId());
	                        }
	                    }
	                    /*communication Details */
	                    if (simpleContactInfo.getPhoneNumber() != null) {
	                        String stdCode = simpleContactInfo.getPhoneNumber().getStdCode();
	                        String phoneNumber = simpleContactInfo.getPhoneNumber().getPhoneNumber();
	                        String extensionNumber = simpleContactInfo.getPhoneNumber().getExtension();
	                        String phoneNumberLandline = "";
	                        if (phoneNumber != null && stdCode != null) {
	                            phoneNumberLandline = stdCode.concat(phoneNumber);
	                        }
	                        if (phoneNumber != null && extensionNumber != null) {
	                            phoneNumberLandline = phoneNumberLandline.concat(extensionNumber);
	                        }
	                        // phoneNumberLandline = stdCode.concat(phoneNumber.concat(extensionNumber));
	                        userAuditVO.setPhoneNumber(phoneNumberLandline);
	                    }
	                    if (simpleContactInfo.getMobileNumber() != null) {
	                        userAuditVO.setMobileNumber(simpleContactInfo.getMobileNumber().getPhoneNumber());
	                    }
	                    if (simpleContactInfo.getEmail() != null) {
	                        userAuditVO.setComminicationEmail(simpleContactInfo.getEmail().getEmailAddress());
	                    }
	                }
	            }
	        return userAuditVO;
	 }
	 
	 protected UserModificationAuditVO populateRoleTeamAndMobilityInformationForAudit(User userAuditLog,
	            UserModificationAuditVO userAuditVO) {
	    	
	        List<String> roleName = userService.getRoleNamesFromUserId(userAuditLog.getId());
	        if (roleName != null) {
	            userAuditVO.setRoleNames(roleName);
	        }
	        
	        List<String> teamName = userService.getTeamNameFromUserId(userAuditLog.getId());
	        if (CollectionUtils.isNotEmpty(teamName)) {
	            userAuditVO.setTeamNames(teamName);
	        }
	        
	        UserMobilityInfo mobilityInfo = userService.getUserMobilityInfo(userAuditLog.getId());
	        if (mobilityInfo != null) {
	            userAuditVO.setMobilityEnabled(mobilityInfo.getIsMobileEnabled());
	            userAuditVO.setChallengeEnabled(mobilityInfo.getIsChallengeEnabled());
	            userAuditVO.setChallenge(mobilityInfo.getChallenge());
	        }
	        
	        return userAuditVO;
	 }
	 
	 protected UserModificationAuditVO populateBranchesAndProductForLatestUser(User latestUserAuditLog, User previousUserAuditLog,
	            UserModificationAuditVO userAuditVO) {
	    	
	                List<String> branchCode = getBranchCodeForLatestUser(latestUserAuditLog, previousUserAuditLog);
	                if (CollectionUtils.isNotEmpty(branchCode)) {
	                    userAuditVO.setBranchCode(branchCode);
	                }
	                
	                List<String> productCode = getProductCodeForLatestUser(latestUserAuditLog, previousUserAuditLog);
	                if (CollectionUtils.isNotEmpty(productCode)) {
	                    userAuditVO.setProductCode(productCode);
	                }
	                
	                String defaultBranch = userService.getDefaultBranchCodeFromUserId(latestUserAuditLog.getId());
					if (defaultBranch != null) {
						userAuditVO.setDefaultBranch(defaultBranch);
					} else {
						defaultBranch = userService.getDefaultBranchCodeFromUserId(previousUserAuditLog.getId());
						userAuditVO.setDefaultBranch(defaultBranch);
					}
	                userAuditVO.setDefaultBranch(defaultBranch);
	                
	                List<String> branchAdminCode = getBranchAdminCodeForLatestUser(latestUserAuditLog, previousUserAuditLog);
	                if (CollectionUtils.isNotEmpty(branchAdminCode)) {
	                    userAuditVO.setBranchAdminCode(branchAdminCode);
	                }
	                
	                userAuditVO.setAssociatedBusinessPartner(userBPMappingService
	                        .getBusinessPartnerNameByUserId(latestUserAuditLog.getId()));
	                
	        return userAuditVO;
	 }
	 
	 protected UserModificationAuditVO populateBranchesAndProductForPreviousUser(User previousUserAuditLog,
	            UserModificationAuditVO userAuditVO) {
	    	
	                List<String> branchCode = getBranchCodeForPreviousUser(previousUserAuditLog);
	                if (CollectionUtils.isNotEmpty(branchCode)) {
	                    userAuditVO.setBranchCode(branchCode);
	                }
	                
	                List<String> productCode = getProductCodeForPreviousUser(previousUserAuditLog);
	                if (CollectionUtils.isNotEmpty(productCode)) {
	                    userAuditVO.setProductCode(productCode);
	                }
	                
	                userAuditVO.setDefaultBranch(userService.getDefaultBranchCodeFromUserId(previousUserAuditLog.getId()));
	                
	                List<String> branchAdminCode = getBranchAdminCodeForPreviousUser(previousUserAuditLog);
	                if (CollectionUtils.isNotEmpty(branchAdminCode)) {
	                    userAuditVO.setBranchAdminCode(branchAdminCode);
	                }
	                
	                userAuditVO.setAssociatedBusinessPartner(userBPMappingService
	                        .getBusinessPartnerNameByUserId(previousUserAuditLog.getId()));
	                
	        return userAuditVO;
	 }
	 
	 protected List<String> getBranchCodeForLatestUser(User latestUserAuditLog, User previousUserAuditLog) {
	    	
	    	List<String> branchCodeList = new ArrayList<String>();
	    	String latestUserSysName = SystemName.SOURCE_PRODUCT_TYPE_CAS;
	    	String previousUserSysName = SystemName.SOURCE_PRODUCT_TYPE_CAS;
	    	
	        if (latestUserAuditLog.getSysName() != null && latestUserAuditLog.getSysName().getCode() != null)
	        	latestUserSysName = latestUserAuditLog.getSysName().getCode();
	        if (previousUserAuditLog.getSysName() != null && previousUserAuditLog.getSysName().getCode() != null)
	        	previousUserSysName = previousUserAuditLog.getSysName().getCode();
	        
	        Map<Long, OrganizationBranch> userBranchesMap = userManagementServiceCore.getUserOrgBranchMappings(previousUserAuditLog.getId(), previousUserSysName);
	    	List<UserOrgBranchMapping> userOrgBranchMappingList = userManagementServiceCore.getUserOrgBranchMappingList(latestUserAuditLog.getId());
	    	for(UserOrgBranchMapping userOrgBranchMapping : userOrgBranchMappingList)
	    	{
	    		if(SelectiveMapping.DELETION_OPERATION.equalsIgnoreCase(userOrgBranchMapping.getOperationType()))
	    		{
	    			userBranchesMap.remove(userOrgBranchMapping.getOrganizationBranchId());
	    		}else if(SelectiveMapping.ADDITION_OPERATION.equalsIgnoreCase(userOrgBranchMapping.getOperationType()))
	    		{
	    			 if (userOrgBranchMapping.isIncludesSubBranches()) {
	    				 userBranchesMap.put(userOrgBranchMapping.getOrganizationBranchId(), userOrgBranchMapping.getOrganizationBranch());
	    	             List<OrganizationBranch> organizationBranchList = organizationService.getAllChildBranches(
	    	                        userOrgBranchMapping.getOrganizationBranch().getId(), latestUserSysName);
	    	                for(OrganizationBranch organizationBranch : organizationBranchList)
	    	                {
	    	                	userBranchesMap.put(organizationBranch.getId(), organizationBranch);
	    	                }
	    	            } else {
	    	            	userBranchesMap.put(userOrgBranchMapping.getOrganizationBranchId(), userOrgBranchMapping.getOrganizationBranch());
	    	            }
	    		}
	    	}
	    	
	    	for(Entry<Long, OrganizationBranch> entry : userBranchesMap.entrySet())
	        {
	    		branchCodeList.add(entry.getValue().getBranchCode());
	        }
	    	
	    	return branchCodeList;
	 }
	 
	 protected List<String> getBranchCodeForPreviousUser(User previousUserAuditLog) {
	    	
	    	List<String> branchCodeList = new ArrayList<String>();
	    	String previousUserSysName = SystemName.SOURCE_PRODUCT_TYPE_CAS;
	    	
	        if (previousUserAuditLog.getSysName() != null && previousUserAuditLog.getSysName().getCode() != null)
	        	previousUserSysName = previousUserAuditLog.getSysName().getCode();
	        
	        Map<Long, OrganizationBranch> userBranchesMap = userManagementServiceCore.getUserOrgBranchMappings(previousUserAuditLog.getId(), previousUserSysName);
	    	
	    	for(Entry<Long, OrganizationBranch> entry : userBranchesMap.entrySet())
	        {
	    		branchCodeList.add(entry.getValue().getBranchCode());
	        }
	    	
	    	return branchCodeList;
	 }
	 
	 protected List<String> getBranchAdminCodeForLatestUser(User latestUserAuditLog, User previousUserAuditLog ) {
	        
	    	List<String> branchAdminCodeList = new ArrayList<String>();
	        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
	                QUERY_FOR_ORGANIZATION_BRANCH_GET_ORG_BRANCHES_WHERE_USER_IS_BRANCH_ADMIN);

	        //For previousUserAuditLog
	        Map<Long, OrganizationBranch> userBranchAdminMap = new HashMap<Long, OrganizationBranch>();
	        executor.addParameter("userId", previousUserAuditLog.getId())
	        		.addParameter("isBranchAdmin", true);
	        
	        List<OrganizationBranch> finalUsersBranchesWhereUserIsAdmin =entityDao.executeQuery(executor);	
			for (OrganizationBranch organizationBranch : finalUsersBranchesWhereUserIsAdmin) {
				userBranchAdminMap.put(organizationBranch.getId(), organizationBranch);
			}
	        	
	    	/*
	    	 * Find user-org-mapping for latestUserAuditLog
	    	 */
			List<UserOrgBranchMapping> unApprovedUsersBranches = userManagementServiceCore
					.getUserOrgBranchMapping(latestUserAuditLog.getId());
			for (UserOrgBranchMapping userOrgBranchMapping : unApprovedUsersBranches) {
				if (userOrgBranchMapping.isBranchAdmin()) {
					userBranchAdminMap.put(
							userOrgBranchMapping.getOrganizationBranchId(),
							userOrgBranchMapping.getOrganizationBranch());
				} else {
					userBranchAdminMap.remove(userOrgBranchMapping.getOrganizationBranchId());
				}

			}	 
	    	finalUsersBranchesWhereUserIsAdmin=new ArrayList<OrganizationBranch>(userBranchAdminMap.values());

	    	//get the branchAdminCodeList
	    	for(OrganizationBranch organizationBranch : finalUsersBranchesWhereUserIsAdmin)
	        {
	    		 branchAdminCodeList.add(organizationBranch.getBranchCode());
	        }
	    	
	    	return branchAdminCodeList;
	 }
	 
	 protected List<String> getBranchAdminCodeForPreviousUser(User previousUserAuditLog) {
	        
	    	List<String> branchAdminCodeList = new ArrayList<String>();
	        NamedQueryExecutor<OrganizationBranch> executor = new NamedQueryExecutor<OrganizationBranch>(
	                QUERY_FOR_ORGANIZATION_BRANCH_GET_ORG_BRANCHES_WHERE_USER_IS_BRANCH_ADMIN);

	        //For previousUserAuditLog
	        Map<Long, OrganizationBranch> userBranchAdminMap = new HashMap<Long, OrganizationBranch>();
	        executor.addParameter("userId", previousUserAuditLog.getId())
	        		.addParameter("isBranchAdmin", true);
	        List<OrganizationBranch> finalUsersBranchesWhereUserIsAdmin =entityDao.executeQuery(executor);	
			for (OrganizationBranch organizationBranch : finalUsersBranchesWhereUserIsAdmin) {
				userBranchAdminMap.put(organizationBranch.getId(), organizationBranch);
			}	 
	    	finalUsersBranchesWhereUserIsAdmin=new ArrayList<OrganizationBranch>(userBranchAdminMap.values());

	    	//get the branchAdminCodeList
	    	for(OrganizationBranch organizationBranch : finalUsersBranchesWhereUserIsAdmin)
	        {
	    		 branchAdminCodeList.add(organizationBranch.getBranchCode());
	        }
	    	
	    	return branchAdminCodeList;
	 }
	 
	 protected List<String> getProductCodeForLatestUser(User latestUserAuditLog, User previousUserAuditLog ) {
	        
		    List<String> productCodeCodeList = new ArrayList<String>();
	        /*
	         * Get the original/approved/previousUserAuditLog user's data
	        */
		    List<UserOrgBranchProdMapping> userOrgBranchProductMappingList = getUserOrgBranchProductMappingList(previousUserAuditLog.getId());
	    	/*
	    	 * Convert to map 
	    	*/
	    	Map<String, UserOrgBranchProdMapping> orgBranchProductMap = new HashMap<String, UserOrgBranchProdMapping>();
	    	StringBuilder orgBranchProductMapKeyBuilder = new StringBuilder();
	    	int start=0;
	    	char tilde='~';
			for(UserOrgBranchProdMapping orgBranchProductMapping : userOrgBranchProductMappingList){
				orgBranchProductMapKeyBuilder.delete(start, orgBranchProductMapKeyBuilder.length());
				orgBranchProductMapKeyBuilder.append(orgBranchProductMapping.getUserOrgBranchMapping().getOrganizationBranchId())
								.append(tilde)
								.append(orgBranchProductMapping.getLoanProductId());
				orgBranchProductMap.put(orgBranchProductMapKeyBuilder.toString(), orgBranchProductMapping);
			}
	    	/*
	    	 * Perform deletion/addition for unapproved user
	    	 */
			List<UserOrgBranchProdMapping> updatedUserOrgBranchProductMappingList = getUserOrgBranchProductMappingList(latestUserAuditLog
					.getId());
	     	for(UserOrgBranchProdMapping updateduserOrgBranchProductMapping : updatedUserOrgBranchProductMappingList)
	     	{
	     		orgBranchProductMapKeyBuilder.delete(start, orgBranchProductMapKeyBuilder.length());
	 			orgBranchProductMapKeyBuilder.append(updateduserOrgBranchProductMapping.getUserOrgBranchMapping().getOrganizationBranchId())
	 							.append(tilde)
	 							.append(updateduserOrgBranchProductMapping.getLoanProductId());
	 			
					if (SelectiveMapping.DELETION_OPERATION.equalsIgnoreCase(updateduserOrgBranchProductMapping.getOperationType())) {
						orgBranchProductMap.remove(orgBranchProductMapKeyBuilder.toString());
					} else if (SelectiveMapping.ADDITION_OPERATION.equalsIgnoreCase(updateduserOrgBranchProductMapping.getOperationType())) {
						orgBranchProductMap.put(orgBranchProductMapKeyBuilder.toString(),updateduserOrgBranchProductMapping);
					}
	     	}
	    	
	    	for(UserOrgBranchProdMapping prodMapping: orgBranchProductMap.values()){
	    		//TODO make LoanProduct plugable
	    		//productCodeCodeList.add(prodMapping.getLoanProduct().getProductCode());
	    	}
	    
	    	return productCodeCodeList;
	 }
	    
	 protected List<String> getProductCodeForPreviousUser(User previousUserAuditLog) {
	        
	    	List<String> productCodeCodeList = new ArrayList<String>();
	        /*
	         * Get the original/approved/previousUserAuditLog user's data
	        */
	    	List<UserOrgBranchProdMapping> userOrgBranchProductMappingList = getUserOrgBranchProductMappingList(previousUserAuditLog.getId());
	    	/*
	    	 * Convert to map 
	    	*/
	    	Map<String, UserOrgBranchProdMapping> orgBranchProductMap = new HashMap<String, UserOrgBranchProdMapping>();
	    	StringBuilder orgBranchProductMapKeyBuilder = new StringBuilder();
	    	int start=0;
	    	char tilde='~';
			for(UserOrgBranchProdMapping orgBranchProductMapping : userOrgBranchProductMappingList){
				orgBranchProductMapKeyBuilder.delete(start, orgBranchProductMapKeyBuilder.length());
				orgBranchProductMapKeyBuilder.append(orgBranchProductMapping.getUserOrgBranchMapping().getOrganizationBranchId())
								.append(tilde)
								.append(orgBranchProductMapping.getLoanProductId());
				orgBranchProductMap.put(orgBranchProductMapKeyBuilder.toString(), orgBranchProductMapping);
			}
			
	    	for(UserOrgBranchProdMapping prodMapping: orgBranchProductMap.values()){
	    		//TODO make LoanProduct plugable
	    		//productCodeCodeList.add(prodMapping.getLoanProduct().getProductCode());
	    	}
	    	
	    	return productCodeCodeList;
	 }
	 
	@SuppressWarnings("unchecked")
	@Override
	 public void updateUserProductSchemeMappings(String schemeListId,User updatedUser){
		 Map<String, List<String>> productSchemeMap = new HashMap<String, List<String>>();
		 		
		 @SuppressWarnings("rawtypes")
		JSONDeserializer deserializer = new JSONDeserializer();
	     productSchemeMap = (Map<String, List<String>>) deserializer.deserialize(schemeListId);
	     List<String> newProductSet = new ArrayList<String>(productSchemeMap.keySet());

		 List<UserOrgBranchProdSchemeMapping> existingSchemeProduct = getUserProductSchemeList(updatedUser.getId());
		 
		 for (UserOrgBranchProdSchemeMapping userProductScheme : existingSchemeProduct) {
				entityDao.delete(userProductScheme);
		 }
		 
		 if( !newProductSet.isEmpty()){
			 for(String product : newProductSet){
				 if(!product.equalsIgnoreCase("_1")){
					 List<String> schemeList;
					 schemeList = productSchemeMap.get(product);
					 for (String scheme : schemeList) {
						product = product.replace(PRODUCT_CONCAT_CHAR, "");
						UserOrgBranchProdSchemeMapping userProductScheme = new UserOrgBranchProdSchemeMapping();
						userProductScheme.setProductId(Long.parseLong(product));
						userProductScheme.setUserId(updatedUser.getId());
						userProductScheme.setSchemeId(Long.parseLong(scheme));
						entityDao.persist(userProductScheme);
					 }
				 }
			 }
		}
		 
	 }
	 
	@Override
	 public List<UserOrgBranchProdSchemeMapping> getUserProductSchemeList(Long userId){
			NamedQueryExecutor<UserOrgBranchProdSchemeMapping> executor = new NamedQueryExecutor<UserOrgBranchProdSchemeMapping>(
					QUERY_FOR_USER_PRODUCT_SCHEME_MAPPINGS_BY_USER_ID).addParameter("userId",userId);
			 List<UserOrgBranchProdSchemeMapping> list = entityDao.executeQuery(executor);

			 return list;
	 }
	 
	 /*
    * 10. Add or update city village mapping
    */
	 protected void updateCityVillageMappingAtMakerStage(UserCityVillageMapping  userCityVillageMapping,User userToBeUpdated, User originalUser, int approvalStatusBeforeUpdate){

         if (ValidatorUtils.notNull(approvalStatusBeforeUpdate)
                 && (approvalStatusBeforeUpdate == ApprovalStatus.APPROVED)) {

  
             userCityVillageMapping.setUser(userToBeUpdated);


             entityDao.persist(userCityVillageMapping);

         } else if (ValidatorUtils.notNull(approvalStatusBeforeUpdate)
                 && (approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_ADDED || approvalStatusBeforeUpdate == ApprovalStatus.UNAPPROVED_MODIFIED)) {

             UserCityVillageMapping prevCityVillageMapping= userCityVillageMappingService.getCityVillageMappingByUserId(userToBeUpdated.getId());


             if (prevCityVillageMapping != null) {
                 Hibernate.initialize(prevCityVillageMapping.getUserCityMappings());
                 Hibernate.initialize(prevCityVillageMapping.getUserVillageMappings());

             }
             prevCityVillageMapping = userCityVillageMappingService.deletePrevMapping(prevCityVillageMapping,userCityVillageMapping);


             prevCityVillageMapping.setUser(userToBeUpdated);
             if (prevCityVillageMapping.getId() == null) {
                 entityDao.persist(prevCityVillageMapping);
             } else {
                 entityDao.update(prevCityVillageMapping);
             }


         }

     }

    @Override
    public void updateUserUrlMappingAtMakerStage(
            List<UserDefaultUrlMappingVO> userDefaultUrlMappingVOList, User userToBeUpdated, User originalUser,
            int approvalStatusBeforeUpdate, List<Long> deletedMappings) {
        User currUser=userToBeUpdated;
        if(userDefaultUrlMappingVOList!=null && CollectionUtils.isNotEmpty(userDefaultUrlMappingVOList)){
            for(UserDefaultUrlMappingVO uiVo:userDefaultUrlMappingVOList){
                if(uiVo.getSourceProduct()!=null && uiVo.getSourceProduct().getId()!=null) {
                    UserDefaultUrlMapping newMapping = new UserDefaultUrlMapping();
                    newMapping.setMappedUser(currUser);
                    newMapping.setSourceProduct(genericParameterService.findById(uiVo.getSourceProduct().getId(), SourceProduct.class));
                    newMapping.setMenuEntity(entityDao.find(MenuEntity.class, uiVo.getMenuVO().getId()));
                    entityDao.saveOrUpdate(newMapping);
                }
            }
        }

    }


    @Override
    public void removeAllUrlMappingsOfUser(Long userId){
        if(userId==null){
            return;
        }
        List<UserDefaultUrlMapping> mappingList=getAllUrlMappingsOfUser(userId);
        for(UserDefaultUrlMapping mapping:mappingList){
            entityDao.delete(mapping);
        }
    }

    @Override
    public void deleteSelectedUrlMappingsOfUser(Long userId,List<Long> deletedUrlMappingIds){
        if(CollectionUtils.isEmpty(deletedUrlMappingIds)){
            return;
        }
        for(Long mappingId:deletedUrlMappingIds){
            UserDefaultUrlMapping mapping=entityDao.find(UserDefaultUrlMapping.class,mappingId);
            if(mapping!=null){
                entityDao.delete(mapping);
            }
        }

    }

    @Override
    public List<UserDefaultUrlMapping> getAllUrlMappingsOfUser(Long userId){
        NamedQueryExecutor<UserDefaultUrlMapping> executor= new NamedQueryExecutor<UserDefaultUrlMapping>("User.getAllUrlMappingsOfUser").addParameter("userId",userId);
        return entityDao.executeQuery(executor);
    }

    @Override
    public List<MenuVO> menuListToVoForUserMapping(List<MenuEntity> menuEntityList){
        List<MenuVO> menuVOList=new ArrayList<>();
        if(CollectionUtils.isEmpty(menuEntityList)){
            return menuVOList;
        }
        MenuVO vo=null;
        for(MenuEntity entity:menuEntityList){
            if(entity!=null){
                menuVOList.add(menuToVoForUserMapping(entity));
            }

        }
        return menuVOList;
    }

    @Override
    public MenuVO menuToVoForUserMapping(MenuEntity entity){
        if(entity==null){
            return null;
        }
        MenuVO vo=new MenuVO();
        vo.setId(entity.getId());
        vo.setMenuName(entity.getMenuName());
        vo.setMenuCode(entity.getMenuCode());
        vo.setMenuDisplayName(messageSource.getMessage(entity.getMenuName(), null, userService.getUserLocale()));
        return vo;
    }

    @Override
    public List<UserDefaultUrlMappingVO> userUrlMappingListToVO(List<UserDefaultUrlMapping> defaultMappingList){
        List<UserDefaultUrlMappingVO> voList=new ArrayList<>();
        if(CollectionUtils.isEmpty(defaultMappingList)){
            return voList;
        }
        for(UserDefaultUrlMapping defaultMapping:defaultMappingList){
            if(defaultMapping!=null){
                voList.add(defaultUrlMappingToVo(defaultMapping));
            }
        }
        return voList;
    }

    public UserDefaultUrlMappingVO defaultUrlMappingToVo(UserDefaultUrlMapping defaultUrlMapping){
        if(defaultUrlMapping==null){
            return null;
        }
        UserDefaultUrlMappingVO urlMappingVo=new UserDefaultUrlMappingVO();
        urlMappingVo.setId(defaultUrlMapping.getId());
        urlMappingVo.setSourceProduct(defaultUrlMapping.getSourceProduct());
        urlMappingVo.setMenuVO(menuToVoForUserMapping(defaultUrlMapping.getMenuEntity()));
        return urlMappingVo;
    }

    @Override
    public Long getTargetUrlMappingCount(Long userId){
        NamedQueryExecutor<Long> executorUserOrgBranchMappings = new NamedQueryExecutor<Long>(
                "User.countMappedDefaultUrlById").addParameter("userId", userId);
        return entityDao.executeQueryForSingleValue(executorUserOrgBranchMappings);
    }

    public List<UserOrgBranchProdMapping> executeSingleInClauseHQLQuery(String hqlQuery, String inParamName, Collection<?> values) {

        // first of all remove nulls
        if (values != null) {
            values = ListUtils.removeAll(values, Collections.singletonList(null));
        }
        List<UserOrgBranchProdMapping> completeResultList = new ArrayList<UserOrgBranchProdMapping>();
        if (values != null && !values.isEmpty()) {

            List idList = new ArrayList(values);
            int fromIndex = 0;
            int toIndex = idList.size() > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : idList.size();
            while (toIndex <= idList.size() && fromIndex < toIndex) {
                List idSubList = new ArrayList(idList.subList(fromIndex, toIndex));
                PersistenceUtils.resizeListWithAutoFill(idSubList);
                if (!idSubList.isEmpty()) {
                    JPAQueryExecutor<UserOrgBranchProdMapping> executor = new JPAQueryExecutor<UserOrgBranchProdMapping>(hqlQuery);
                    executor.addParameter(inParamName, idSubList);

                    // add other params
                    /*if (otherParams != null && !otherParams.isEmpty()) {
                        for (Map.Entry<String, ?> param : otherParams.entrySet()) {
                            executor.addParameter(param.getKey(), param.getValue());
                        }
                    }
                    if (cacheable != null && cacheable == true) {
                        executor.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
                    }*/
                    List<UserOrgBranchProdMapping> resultListForBatch = entityDao.executeQuery(executor);
                    if (resultListForBatch != null) {
                        completeResultList.addAll(resultListForBatch);
                    }
                    fromIndex = toIndex;
                    int difference = idList.size() - toIndex;
                    if (difference <= 0) {
                        break;
                    }
                    int batchSize = difference > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : difference;
                    toIndex = toIndex + batchSize;
                }
            }
        }
        return completeResultList;
    }


}
