/**
 * 
 */
package com.nucleus.web.masters;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.service.GenericParameterServiceImpl;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.additionaldata.constants.CustomFieldFor;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataMetaData;
import com.nucleus.finnone.pro.additionaldata.domainobject.CustomFieldDataType;
import com.nucleus.finnone.pro.additionaldata.domainobject.ServiceRequestTransactionType;
import com.nucleus.finnone.pro.additionaldata.serviceinterface.IAdditionalDataService;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationCommonService;
import com.nucleus.finnone.pro.general.util.MapToList;

import com.nucleus.lms.web.common.MessageOutput;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.master.CommonFileIOMasterGridLoad;
/**
 * @author shivani.aggarwal
 *
 */

@Transactional
public class AdditionalDataMetaDataController extends BaseController{

	  @Inject
	  @Named("makerCheckerService")
	  private MakerCheckerService makerCheckerService;
	  
	  @Inject
	  @Named("masterXMLDocumentBuilder")
	  private CommonFileIOMasterGridLoad commonFileIOMasterGridLoad;

	  @Inject
	  @Named("baseMasterService")
	  private BaseMasterService baseMasterService;
	  
	  @Inject
	  @Named("genericParameterService")
	  private GenericParameterServiceImpl genericParameterService; 
	  
	  @Inject
	  @Named("communicationCommonService")
	  private ICommunicationCommonService communicationCommonService;
	  
	  @Inject
	  @Named("additionalDataService")
	  private IAdditionalDataService additionalDataService;
	  
/*	  @Inject
	  @Named("additionalDataHelper")
	  private AdditionalDataHelper additionalDataHelper;*/
	  
	  @Autowired
	  protected MessageSource messageSource;
	  
	//  private static final String MASTERID= "AdditionalDataMetaData";
	  //private static final String ADDITIONAL_MASTER_JSP_NAME= "additionalDataMaster";
	  String masterId = "AdditionalDataMetaData";
	  String pathToRedirect;
	  String propertyValue;
	  String entityPath = "";
	  

	  public AdditionalDataMetaDataController(){
	    this.masterId = "AdditionalDataMetaData";
	    this.entityPath = ""; 
	  }

	  @ModelAttribute("currentEntityClassName")
	  public String getEntityClassName(){
	    return AdditionalDataMetaData.class.getName();
	  }
	
	  @PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	  @RequestMapping({"/create"})
	  public String createAdditionalData(ModelMap map,HttpServletRequest request){
		 
		map = setInitialDataForRenderingJSP(map);
		map.put("additionalDataMetaData", new AdditionalDataMetaData());
	    return "additionalDataMaster";
	  }
	  
	  protected <T extends GenericParameter> String getGenericParameterId(String value,Class<T> entityClass)
		{
			// Changes done in rel 1.29
		  	GenericParameter genericParameter=genericParameterService.findByCode(value, entityClass);
			return genericParameter!=null?String.valueOf(genericParameter.getId()):"";
		}
	  
	  /** 
	 	* Used to saves the Reason.
	 	* @param LMSReason:reason
	 	* @param BindingResult:result
	 	* @param boolean:createAnotherMaster
	 	* @return String
	  */

	  @PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	  @RequestMapping(value={"/save"}, method={RequestMethod.POST})
	  public String save(@Validated AdditionalDataMetaData additionalDataMetaData,BindingResult result,ModelMap map,@RequestParam("createAnotherMaster") boolean createAnotherMaster,HttpServletRequest request){
		  
		  User user = getUserDetails().getUserReference();
		  String genericParameterAmountId=getGenericParameterId(CustomFieldDataType.AMOUNT,CustomFieldDataType.class);
			if(StringUtils.isNotEmpty(genericParameterAmountId) 
					&& additionalDataMetaData.getDataType().compareTo(Long.parseLong(genericParameterAmountId))==0 ){
				additionalDataMetaData.setLength(17);
			}
		String genericParameterFloatId=getGenericParameterId(CustomFieldDataType.RATE,CustomFieldDataType.class);	
			if(StringUtils.isNotEmpty(genericParameterFloatId) 
							&& additionalDataMetaData.getDataType().compareTo(Long.parseLong(genericParameterFloatId))==0){
				additionalDataMetaData.setLength(10);
			}
		  additionalDataMetaData.setAdditionalDataTransactionType(null);
		  if(additionalDataMetaData.getMandatory() == null)
			  additionalDataMetaData.setMandatory('N');
		  if (user != null){
			  this.makerCheckerService.masterEntityChangedByUser(additionalDataMetaData, user);
		  }
		  
		  if (createAnotherMaster) {
			map = setInitialDataForRenderingJSP(map);
			map.put("additionalDataMetaData", new AdditionalDataMetaData());
			 return "additionalDataMaster";
		  }
		  map.put("masterID", this.masterId);
		  return "redirect:/app/grid/AdditionalDataMetaData/AdditionalDataMetaData/loadColumnConfig";
	  }
	  
	  @PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	  @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
		public String viewCustomField(@PathVariable("id") Long id, ModelMap map,HttpServletRequest request){
		  
		  UserInfo currentUser = getUserDetails();
		  AdditionalDataMetaData additionalDataMetaData = (AdditionalDataMetaData)this.baseMasterService.getMasterEntityWithActionsById(AdditionalDataMetaData.class, id, currentUser.getUserEntityId().getUri());
		  	map = setInitialDataForRenderingJSP(map);
		  	map.put("additionalDataMetaData", additionalDataMetaData);
		  	map.put("mandatoryValueFlag", additionalDataMetaData.getMandatory());
		  	map.put("additionalDataTransactionTypeHidden", additionalDataMetaData.getAdditionalDataTransactionType().getId());
			
			map.put("viewable", true);
			if (additionalDataMetaData.getViewProperties() != null) {
				@SuppressWarnings("unchecked")
				List<String> actions = (ArrayList<String>) additionalDataMetaData.getViewProperties().get("actions");
				if (actions != null) {
					if(ApprovalStatus.APPROVED == additionalDataMetaData.getApprovalStatus()){
						actions.remove("Delete");
					}
					
					if(actions.contains("Clone")){
			  			actions.remove("Clone");
	 				}
					for (String act : actions) {
						String actionString = "act" + act;
						map.put(actionString.replaceAll(" ", ""), false);
					}
				}
			}
			return "additionalDataMaster";
		}
	  
	  /**
		 * Fetches record for displaying existing order
		 * @param id
		 * @param map
		 * @return
		 */
	  @PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	  @RequestMapping(value = "/viewExistingOrder", method = RequestMethod.POST)
	  @ResponseBody
	  public Map<String,Object> viewExistingOrder(Long transactionType, ModelMap map,HttpServletRequest request){
		  Map<String,Object> responseMap=new HashMap<String,Object>();
			List<Map<String,Object>> dataTableMap=new ArrayList<Map<String,Object>>();
			List<AdditionalDataMetaData> additionalDataMetaData = additionalDataService.getAdditionalDataMetaDataByTransactionTypeForDisplayOrder(transactionType);
			Map<String, Object> customDataMap;
			if(!additionalDataMetaData.isEmpty()){
				for(AdditionalDataMetaData aDataMetaData: additionalDataMetaData)
				{  
					customDataMap=new HashMap<String,Object>();
					customDataMap.put("id",aDataMetaData.getId());
					customDataMap.put("displayOrder",aDataMetaData.getDisplayOrder());
					customDataMap.put("fieldLabel",aDataMetaData.getFieldLabel());
					customDataMap.put("fieldName",aDataMetaData.getFieldName());
					GenericParameter genericParameter=communicationCommonService.findById(aDataMetaData.getDataType(), CustomFieldDataType.class);
					if(genericParameter!=null && genericParameter.getName()!=null)
					{
							customDataMap.put("dataType",genericParameter.getName());
					}else{
						customDataMap.put("dataType","");
					}
					if(aDataMetaData.getMandatory()!=null && aDataMetaData.getMandatory().equals('Y'))
					{
						customDataMap.put("mandatory","Yes");
					}
					else
					{
						customDataMap.put("mandatory","No");
					}
				  dataTableMap.add(customDataMap);	
				}
			}
			responseMap.put("aaData",dataTableMap);
			return responseMap;
		}

	  
	  /**
		 * Fetches record for displaying mapping fields to left
		 * @param id
		 * @param map
		 * @return
		 */
	  @PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	  @RequestMapping(value = "/displayMappingFields", method = RequestMethod.POST)
	  @ResponseBody
	  public Map<String,Object> displayMappingFields(Long transactionType, ModelMap map,HttpServletRequest request){
		  Map<String,Object> responseMap=new HashMap<String,Object>();
			List<AdditionalDataMetaData> additionalDataMetaData = additionalDataService.getAdditionalDataMetaDataByTransactionTypeId(transactionType);
			Map<String, String> allMappingFields = getMappingFields();
			if(!additionalDataMetaData.isEmpty()){
					for(AdditionalDataMetaData aDataMetaData: additionalDataMetaData)
					{  
						  allMappingFields.remove(aDataMetaData.getMappingField());
					}
			}
			responseMap.put("mappingFields",allMappingFields);
			return responseMap;
		}
	  
	  
	  /**
		 * Fetches record for checking existing Field Name for Duplicacy
		 * @param id
		 * @param map
		 * @return
		 */
	  @PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	  @RequestMapping(value = "/checkExistingFieldName", method = RequestMethod.POST)
	  @ResponseBody
	  public Map<String,Object> checkExistingFieldName(Long transactionType,String fieldName,HttpServletRequest request){
		  Map<String,Object> responseMap=new HashMap<String,Object>();
			List<AdditionalDataMetaData> additionalDataMetaData = additionalDataService.getAdditionalDataMetaDataByTransactionTypeId(transactionType);
			if(!additionalDataMetaData.isEmpty()){
					for(AdditionalDataMetaData aDataMetaData: additionalDataMetaData)
					{  
						  if(fieldName.equals(aDataMetaData.getFieldName()))
						  {
							  responseMap.put("error",prepareMessageOutputs("fmsg.00001750",Message.MessageType.ERROR,request));
						  }
					}
			}
			return responseMap;
		}
	  
	  @PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	  @RequestMapping(value = "/listAdditionalDataTransactionType", method = RequestMethod.POST)
		@ResponseBody
		public Map<String, Object> listAdditionalDataTransactionType(Long serviceOrTxnTypeId,HttpServletRequest request) {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			try {
				List<ServiceRequestTransactionType> serviceRequestTransactionTypes = additionalDataService.getAdditionalDataTransactionType(serviceOrTxnTypeId);
				
				List<Map<String,Object>> serviceRequestTransactionTypeList = new ArrayList<Map<String,Object>>();
				Map<String,Object> serviceRequestTransactionTypeMap = null;
				for(ServiceRequestTransactionType serviceRequestTransactionType:serviceRequestTransactionTypes)
				{
					serviceRequestTransactionTypeMap = new HashMap<String,Object>();
					serviceRequestTransactionTypeMap.put("id", serviceRequestTransactionType.getId());
					serviceRequestTransactionTypeMap.put("name", serviceRequestTransactionType.getServiceRequestTransactionTypeName());
					serviceRequestTransactionTypeList.add(serviceRequestTransactionTypeMap);
				}
				
				resultMap.put("additionDataTransactionTypeList", serviceRequestTransactionTypeList);
			} catch (Exception ex) {
				BaseLoggers.exceptionLogger.error("Exception: " + ex.getMessage(),
						ex);
			}
			return resultMap;
		}
	  
	    
		private List<Map<String,String>> getListOfMaster() 
		{
	    	List<Map<String,String>> listOfTypeList = additionalDataService.getMastersInfo();
			List<Map<String,String>> listTypeList = new ArrayList<Map<String,String>>();
			for(Map<String,String> map : listOfTypeList)
			{
				if(!"GenericParameter".equals(map.get("name")) && !"LMSReason".equals(map.get("name")))
					listTypeList.add(map);
			}
			sortMastersList(listTypeList);
			return listTypeList;
		}
	    
		@PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	    @RequestMapping(value = "/listOfMasterFields", method = RequestMethod.POST)
		@ResponseBody
		public Map<String, Object> listOfMasterFields(String className,HttpServletRequest request) 
		{
	    	Map<String, Object> resultMap = new HashMap<String, Object>();
	    	List<Map<String, String>> fieldList = new ArrayList<Map<String, String>>(); 
	    	Map<String,String> fieldMap = null;
	    	
	    	try
	    	{
	    		Class masterClass = Class.forName(className);
	    		Class baseClass = Class.forName("com.nucleus.master.BaseMasterEntity");
	    		if(GenericParameter.class.isAssignableFrom(masterClass))
				   baseClass = Class.forName("com.nucleus.entity.BaseEntity");
				
				for(PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(masterClass,baseClass).getPropertyDescriptors()){
		    	    if(propertyDescriptor.getPropertyType().equals(String.class))
		    	    {
		    	    	  fieldMap = new HashMap<String, String>();
			    		  String fieldName = Character.toUpperCase(propertyDescriptor.getName().charAt(0)) + propertyDescriptor.getName().substring(1);
			    		  fieldMap.put("fieldId",propertyDescriptor.getName());
			    		  fieldMap.put("fieldName", fieldName);
			    		  fieldList.add(fieldMap);
		    	    }
		    	}
	    	}
	    	catch(Exception e)
	    	{
	    		//TODO handle catch
	    	}
	    	sortMastersFieldList(fieldList);
	    	resultMap.put("fieldList", fieldList);
			return resultMap;
		}
	  
	  /**
		 * Fetches record for checking existing Field Label for Duplicacy
		 * @param id
		 * @param map
		 * @return 
		 */
		@PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	  @RequestMapping(value = "/checkExistingFieldLabel", method = RequestMethod.POST)
	  @ResponseBody
	  public Map<String,Object> checkExistingFieldLabel(Long transactionType,String fieldLabel,@RequestParam(required=false) String fieldLabelId,HttpServletRequest request){
		  Map<String,Object> responseMap=new HashMap<String,Object>();
			List<AdditionalDataMetaData> additionalDataMetaData = additionalDataService.getAdditionalDataMetaDataByTransactionTypeId(transactionType);
			if(!additionalDataMetaData.isEmpty()){
					for(AdditionalDataMetaData aDataMetaData: additionalDataMetaData)
					{  
						  if(aDataMetaData.getFieldLabel().equalsIgnoreCase(fieldLabel))
						  {
							  String[] messageArguments = {fieldLabel,aDataMetaData.getId().toString()};
							  responseMap.put("errorInFieldLabel",prepareMessageOutputs("fmsg.00001785",Message.MessageType.ERROR,request,messageArguments));
							  
						  }
						  if(fieldLabelId!=null && "".equals(fieldLabelId) && fieldLabelId.equalsIgnoreCase(aDataMetaData.getFieldLabelId()))
						  {
							  String[] messageArguments = {fieldLabelId,aDataMetaData.getId().toString()};
							  responseMap.put("errorInFieldLabelId",prepareMessageOutputs("fmsg.00001786",Message.MessageType.ERROR,request,messageArguments));
						  }
					}
			}
			return responseMap;
		}
	  
	  
	  /**
		 * Fetches record for edit
		 * @param id
		 * @param map
		 * @return
		 */
		@PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
		@RequestMapping({"/edit/{id}"})
		public String editCustomField(@PathVariable("id") Long id, ModelMap map,HttpServletRequest request){
			
			 UserInfo currentUser = getUserDetails();
			  AdditionalDataMetaData additionalDataMetaData = (AdditionalDataMetaData)this.baseMasterService.getMasterEntityWithActionsById(AdditionalDataMetaData.class, id, currentUser.getUserEntityId().getUri());
			  
			  if (additionalDataMetaData.getApprovalStatus() == ApprovalStatus.APPROVED || 
						additionalDataMetaData.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED)
				{
					map.put("approvedEdit",true);
				}
			  
			  	map = setInitialDataForRenderingJSP(map);
			  	map.put("additionalDataMetaData", additionalDataMetaData);
			  	map.put("additionalDataTransactionTypeHidden", additionalDataMetaData.getAdditionalDataTransactionType().getId());
			  	map.put("mandatoryValueFlag", additionalDataMetaData.getMandatory());
			  	map.put("edit", true);
				if (additionalDataMetaData.getViewProperties() != null) {
					@SuppressWarnings("unchecked")
					List<String> actions = (ArrayList<String>) additionalDataMetaData
							.getViewProperties().get("actions");
					if (actions != null) {
						for (String act : actions) {
							String actionString = "act" + act;
							map.put(actionString.replaceAll(" ", ""), false);
						}
					}
				}
		   return "additionalDataMaster";
		}

	  
	  /** 
	 	* saveAndSendForApproval is used to save the Reason and send the saved record for approval.
	 	* @param LMSReason:reason
	 	* @param BindingResult:result
	 	* @param boolean:createAnotherMaster
	 	* @return String
	  */
		@PreAuthorize("hasAnyAuthority('MNU_CUSTOMFIELDS_MASTER')")
	  @RequestMapping(value={"/saveAndSendForApproval"}, method={RequestMethod.POST})
	  public String saveAndSendForApproval(@Validated AdditionalDataMetaData additionalDataMetaData, BindingResult result, ModelMap map,
			  @RequestParam("createAnotherMaster") boolean createAnotherMaster,HttpServletRequest request){
		  
			User user = getUserDetails().getUserReference();
			 String genericParameterAmountId=getGenericParameterId(CustomFieldDataType.AMOUNT,CustomFieldDataType.class);
				if(StringUtils.isNotEmpty(genericParameterAmountId) 
						&& additionalDataMetaData.getDataType().compareTo(Long.parseLong(genericParameterAmountId))==0 ){
					additionalDataMetaData.setLength(17);
				}
			String genericParameterFloatId=getGenericParameterId(CustomFieldDataType.RATE,CustomFieldDataType.class);	
				if(StringUtils.isNotEmpty(genericParameterFloatId) 
								&& additionalDataMetaData.getDataType().compareTo(Long.parseLong(genericParameterFloatId))==0){
					additionalDataMetaData.setLength(10);
				}
			additionalDataMetaData.setAdditionalDataTransactionType(null);
			if(additionalDataMetaData.getMandatory() == null)
				  additionalDataMetaData.setMandatory('N');
		    if (user != null) {
		        makerCheckerService.saveAndSendForApproval(additionalDataMetaData, user);
		    }
		
		    if (createAnotherMaster) {
				map = setInitialDataForRenderingJSP(map);
				map.put("additionalDataMetaData", new AdditionalDataMetaData());
				 return "additionalDataMaster";
			}
		    
		    return "redirect:/app/grid/AdditionalDataMetaData/AdditionalDataMetaData/loadColumnConfig";
	  }
	  
	  private Map<String,String> getMappingFields()
	  {
		  Map<String,String> mappingFieldMap = new LinkedHashMap<String,String>();
		  mappingFieldMap.put("additionalField1","customField1");
		  mappingFieldMap.put("additionalField2","customField2");
		  mappingFieldMap.put("additionalField3","customField3");
		  mappingFieldMap.put("additionalField4","customField4");
		  mappingFieldMap.put("additionalField5","customField5");
		  mappingFieldMap.put("additionalField6","customField6");
		  mappingFieldMap.put("additionalField7","customField7");
		  mappingFieldMap.put("additionalField8","customField8");
		  mappingFieldMap.put("additionalField9","customField9");
		  mappingFieldMap.put("additionalField10","customField10");
		  mappingFieldMap.put("additionalField11","customField11");
		  mappingFieldMap.put("additionalField12","customField12");
		  mappingFieldMap.put("additionalField13","customField13");
		  mappingFieldMap.put("additionalField14","customField14");
		  mappingFieldMap.put("additionalField15","customField15");
		  mappingFieldMap.put("additionalField16","customField16");
		  mappingFieldMap.put("additionalField17","customField17");
		  mappingFieldMap.put("additionalField18","customField18");
		  mappingFieldMap.put("additionalField19","customField19");
		  mappingFieldMap.put("additionalField20","customField20");
		  mappingFieldMap.put("additionalField21","customField21");
		  mappingFieldMap.put("additionalField22","customField22");
		  mappingFieldMap.put("additionalField23","customField23");
		  mappingFieldMap.put("additionalField24","customField24");
		  mappingFieldMap.put("additionalField25","customField25");
		  return mappingFieldMap;
	  }
	  
	  private Map<String,String> getListTypes()
	  {
		  Map<String,String> listType = new LinkedHashMap<String,String>();
		  listType.put("G","Generic Parameter");
		  listType.put("M","Master");
		  return listType;
	  }
	  
	  private List<ServiceRequestTransactionType> getTransactionTypes() {
		  List<ServiceRequestTransactionType> transactionTypes = additionalDataService.getTransactionTypeDetails();
		  if(notNull(transactionTypes)){
			  sortTransactionTypes(transactionTypes);
		  }
		  return transactionTypes;
	  }
	  
	  /** 
	   * sortTransactionTypes is used to sort  the Transaction Types according to their descriptions.
	   * @param List<TransactionType>:transactionTypes
	   * @return List<TransactionType>
	   */ 
	  private void sortTransactionTypes(List<ServiceRequestTransactionType> transactionTypes) {		
		  
			Comparator<ServiceRequestTransactionType> comparator = new Comparator<ServiceRequestTransactionType>() {
			    public int compare(ServiceRequestTransactionType c1, ServiceRequestTransactionType c2) {
			        return c1.getServiceRequestTransactionTypeDescription().compareTo(c2.getServiceRequestTransactionTypeDescription());
			    }
			};
			Collections.sort(transactionTypes, comparator);
	  } 
	  
	  private void sortMastersList(List<Map<String,String>> mastersList) {		
		  
			Comparator<Map<String,String>> comparator = new Comparator<Map<String,String>>() {
			    public int compare(Map<String,String> map1, Map<String,String> map2) {
			        return map1.get("name").compareTo(map2.get("name"));
			    }
			};
			Collections.sort(mastersList, comparator);
	  }
	  
	  private void sortMastersFieldList(List<Map<String,String>> mastersList) {		
		  
			Comparator<Map<String,String>> comparator = new Comparator<Map<String,String>>() {
			    public int compare(Map<String,String> map1, Map<String,String> map2) {
			        return map1.get("fieldName").compareTo(map2.get("fieldName"));
			    }
			};
			Collections.sort(mastersList, comparator);
	  }
	  
	  /**
		 * Method to get Generic Parameter Details
		 * @param entityClass
		 * @return
		 */
		private <T extends GenericParameter> List<T> getGenericParameterDetails(Class<T> entityClass,String code) {
			List<T> genericParameterList = null;
			if(code==null){
				genericParameterList = genericParameterService.retrieveTypes(entityClass);
			}
			if(genericParameterList!=null && !genericParameterList.isEmpty())
			{
				Collections.sort(genericParameterList,CoreUtility.GENERIC_PARAMETER_COMPARATAOR_BY_VALUE_ASC);
			}
			return genericParameterList;
		}
	  
	  private ModelMap setInitialDataForRenderingJSP(ModelMap map)
	  {
		  
		  map.put("masterID", this.masterId);
		  map.put("transactionTypes", getTransactionTypes());
		  map.put("mastersList", getListOfMaster());
		  map.put("srOrTxnType", getGenericParameterDetails(CustomFieldFor.class,null));
		  map.put("dataTypes", getGenericParameterDetails(CustomFieldDataType.class,null));
		  map.put("mappingFields", MapToList.convertToList(getMappingFields()));
		  map.put("listTypes", MapToList.convertToList(getListTypes()));
		  map.put("genericParameterText", getGenericParameterId(CustomFieldDataType.TEXT,CustomFieldDataType.class));
		  map.put("genericParameterAmount", getGenericParameterId(CustomFieldDataType.AMOUNT,CustomFieldDataType.class));
		  map.put("genericParameterDate", getGenericParameterId(CustomFieldDataType.DATE,CustomFieldDataType.class));
		  map.put("genericParameterInteger", getGenericParameterId(CustomFieldDataType.INTEGER,CustomFieldDataType.class)); 
		  map.put("genericParameterNumber", getGenericParameterId(CustomFieldDataType.RATE,CustomFieldDataType.class));
		  map.put("genericParameterList", getGenericParameterId(CustomFieldDataType.LIST,CustomFieldDataType.class));
		  return map;
	  }
	  
	  
	  
		/**
		 * Helper method to create list of output based on single i18n key
		 * @param i18nCode
		 * @param messageType
		 * @param request
		 * @param accountNo
		 * @return
		 */
		protected MessageOutput prepareMessageOutputs(String i18nCode,Message.MessageType messageType,HttpServletRequest request, String... accountNo){
			List<Message> messageList=new ArrayList<Message>();
			Message msg = new Message(i18nCode,messageType,accountNo);
			messageList.add(msg);
			return getWebMessageList(messageList,request).get(0);
		}
	  
		@RequestMapping(value = "/getMessageValue", method = RequestMethod.GET)
		@ResponseBody
		public Map<String,String> checkExistingFieldLabel(String key,HttpServletRequest request)
		{
			  Map<String,String> responseMap=new HashMap<String,String>();
		    	try{
		    		String i18Value = messageSource.getMessage(key,new Object[0],RequestContextUtils.getLocale(request));
		    		responseMap.put("key",key);
		    		responseMap.put("value",i18Value);
		    	}
		    	catch(Exception e)
		    	{
		    		//TODO 
		    	}
			  return responseMap;
		}
		
		 public List<MessageOutput> getWebMessageList(List<Message> messageList,HttpServletRequest request)
		    {
		    	List<MessageOutput> webMessageList = new ArrayList<MessageOutput>();
		    	try{
		    	
		    	MessageOutput messageOutput = null;
		    	for (Message message : messageList)
		    	{
		    		String i18Value = messageSource.getMessage(message.getI18nCode(),message.getMessageArguments(),message.getI18nCode(),RequestContextUtils.getLocale(request));
		    		i18Value=message.getI18nCode()+" : "+i18Value;
		    		messageOutput = new MessageOutput(message,i18Value);
		    		webMessageList.add(messageOutput);
		        }
		    	}
		    	catch(Exception e)
		    	{
		    		//TODO 
		    	}
		    	return webMessageList;
			}
		    
	  
}
