package com.nucleus.web.formDefinition;

import com.nucleus.core.dynamicform.service.FormConfigurationConstant;
import com.nucleus.core.dynamicform.service.FormDefinitionService;
import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventExecutionResult;
import com.nucleus.core.event.service.EventExecutionService;
import com.nucleus.core.formDefinition.FormDefinitionUtility;
import com.nucleus.core.formsConfiguration.*;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.rules.service.RuleService;
import com.nucleus.rules.simulation.service.RuleSimulationService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Transactional
@Controller
@RequestMapping(value = "/FormDefinition")
@SessionAttributes("FormDefinition")
public class FormDefinitionController extends BaseDynamicFormController {


	@Inject
 	@Named("formDefinitionUtility")
 	protected FormDefinitionUtility formDefinitionUtility;
	
	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("formDefinitionService")
	private FormDefinitionService formDefinitionService;
	
    @Inject
    @Named("entityDao")
    private EntityDao                  entityDao;
    
    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;

	@Inject
	@Named(value = "eventExecutionService")
	private EventExecutionService eventExecutionService;

	@Inject
	@Named("ruleSimulationService")
	private RuleSimulationService ruleSimulationService;

	@Inject
	@Named(value = "ruleService")
	protected RuleService ruleService;

    public static final String SELECT_CLAUSE_FOR_AUTOPOPULATE="SELECT DISTINCT new Map(";

	public static final String SPECIAL_TABLE_BLOCK_EXECUTED = "Special Table block executed";

	public static final String FROM_CLAUSE_PATTERN = "FROM\\s(.*?)\\s.*";

	public static final String URI_PATTERN = "\\.(\\w+?):\\d+";

	private static final String FIV_NAME = "com.nucleus.core.loan.fi.FieldInvestigationEntry";

	private static final String COLL_NAME = "com.nucleus.core.loan.collateralInvestigation.CollateralInvestigationEntry";

	
	/*@Value(value = "#{'${core.web.config.default.source.system.code}'}")
    private String               sourceSystemCode;*/
 	
 	@RequestMapping(value={"/displayDynamicForm"},method={RequestMethod.POST})
	public ModelAndView displayDynamicForm(@RequestParam(required=false) String uri,@RequestParam(required=false) String  screenId,
				@RequestParam(required=false) String formName,@RequestParam(required=false) Boolean workFlowBased,
				@RequestParam(value = "viewMode", required = false) String viewMode,@RequestParam(required=false) String uiMetaDataVoString,
				@RequestParam(value = "dynamicFormData", required = false) String dynamicFormData,
				@RequestParam(value = "masterId", required = false) String masterId,
				@RequestParam(value = "productType", required = false) String productType, HttpSession session,
				@RequestParam(value= "sourceProductId",required = false) Long sourceProductIdVal) {

 		UIMetaDataVo uiMetaDataVo=null;
 		Long screenIdValue=null;
 		Long sourceProductId =null;
 		if(!isEmpty(screenId) && !screenId.equals("null")){
 			 screenIdValue=Long.parseLong(screenId);
 		}
 		if(isEmpty(formName))
 		{
 			formName="null";
 		}
 		
		if (notNull(sourceProductIdVal)) {
			SourceProduct sourceProduct = genericParameterService.findById(sourceProductIdVal, SourceProduct.class);
			if (notNull(sourceProduct))
				sourceProductId = sourceProduct.getId();
		}else if(notNull(ProductInformationLoader.getProductName())){
			SourceProduct sourceProduct=genericParameterService.findByCode(ProductInformationLoader.getProductName(), SourceProduct.class);
			if(notNull(sourceProduct))
	 		   sourceProductId=sourceProduct.getId();
	 		
		}
	 		
 		ModelAndView modelAndView = new ModelAndView("dynamicFormDefinition");
 		
 		//If masterId is not defined or null, continue with existing flow.
		if (StringUtils.isEmpty(masterId)) {
			if(notNull(workFlowBased) && !workFlowBased){
	 			if(notNull(uiMetaDataVoString) && StringUtils.isNotEmpty(uiMetaDataVoString) && !uiMetaDataVoString.equals("null")){
	 				uiMetaDataVo = formDefinitionUtility.prepareDynamicFormFromUiMetaData(uiMetaDataVoString,screenIdValue);
	 			}else{
	 				uiMetaDataVo=formDefinitionUtility.prepareUIMetaDataVoForNonWorkFlowBased(uri,screenIdValue,formName,sourceProductId,viewMode,dynamicFormData,productType);
	 			}
	 			modelAndView.addObject("showSaveButton", false);
	 		}else{
	 			uiMetaDataVo=formDefinitionUtility.prepareUIMetaDataVoForWorkFlowBased(uri,formName,sourceProductId,viewMode,dynamicFormData);
	 			modelAndView.addObject("showSaveButton", true);
	 			modelAndView.addObject("previewSaveButton",true);
	 		}
		} else {
			BaseLoggers.flowLogger.info("Preparing Model and View Data for Master Dynamic Form for Master ID : " + masterId);
			uiMetaDataVo = prepareModelForMasterDynamicForm(uri, formName, workFlowBased, viewMode,
					uiMetaDataVoString, dynamicFormData, masterId, screenIdValue, sourceProductId, modelAndView);
		}
 		
 		if(ValidatorUtils.isNull(uiMetaDataVo)) {
			modelAndView.addObject("isDynamicFormAttached", false);
		}
 		else {
 			modelAndView.addObject("isDynamicFormAttached", true);
 		}
		
 		modelAndView.addObject("uiMetaDataVo", ValidatorUtils.isNull(uiMetaDataVo)?new UIMetaDataVo():uiMetaDataVo);
		modelAndView.addObject("defaultValuesMapString", ValidatorUtils.isNull(uiMetaDataVo)?"null":convertToJSONString(uiMetaDataVo.getDefaultValuesMap()));
		modelAndView.addObject("screenId", screenIdValue);
		modelAndView.addObject("offlineTemplate", false);
		modelAndView.addObject("uri", uri);
		
		
		
		if (viewMode != null && !viewMode.isEmpty()) {
	            modelAndView.addObject("viewMode", viewMode);
	    }
		
		if (ValidatorUtils.notNull(uiMetaDataVo) && ValidatorUtils.notNull(uiMetaDataVo.getFormName())) {
			modelAndView.addObject("formKey", uiMetaDataVo.getFormName().replaceAll(" ", "_"));
        }
		return modelAndView;
 	}

	private UIMetaDataVo prepareModelForMasterDynamicForm(String uri, String formName, Boolean workFlowBased,
			String viewMode, String uiMetaDataVoString, String dynamicFormData, String masterId, Long screenIdValue,
			Long sourceProductId, ModelAndView modelAndView) {
		UIMetaDataVo uiMetaDataVo;
		EntityId entityId = EntityId.fromUri(uri);
		if (notNull(workFlowBased) && !workFlowBased) {
			if (notNull(uiMetaDataVoString) && StringUtils.isNotEmpty(uiMetaDataVoString)
					&& !uiMetaDataVoString.equals("null")) {
				uiMetaDataVo = formDefinitionUtility.prepareDynamicFormFromUiMetaData(uiMetaDataVoString, screenIdValue);
			} else {
				uiMetaDataVo = formDefinitionUtility.prepareUIMetaDataVoForNonWorkFlowBased(uri, entityId,
						screenIdValue, formName, sourceProductId, viewMode, dynamicFormData);
			}
			modelAndView.addObject("showSaveButton", false);
		} else {
			uiMetaDataVo = formDefinitionUtility.prepareUIMetaDataVoForWorkFlowBased(uri, entityId, formName,
					sourceProductId, viewMode, dynamicFormData);
			modelAndView.addObject("showSaveButton", true);
			modelAndView.addObject("previewSaveButton", true);
		}

		modelAndView.addObject("isMasterDynamicForm", true);
		modelAndView.addObject("masterId", masterId);
		Entity entityObj = null;
		if (StringUtils.isNotEmpty(uri)) {
			entityObj = entityDao.get(entityId);
			entityDao.detach(entityObj);
		} else {
			String entityName = masterConfigurationRegistry.getEntityClass(masterId);
			entityObj = EntityId.getNewInstance(entityName);
		}
		if (entityObj instanceof BaseMasterEntity) {
			((BaseMasterEntity) entityObj).setUiMetaDataVo(uiMetaDataVo);
		}
		modelAndView.addObject(masterId, entityObj);
		return uiMetaDataVo;
	}
 	
	@RequestMapping(value={"/addDynamicFormTableRow"},method={RequestMethod.POST})
	public ModelAndView addDynamicFormTableRow(
			@RequestParam(required = false) Integer tableIndex,
			@RequestParam(required = false) Integer formComponentIndex,
			@RequestParam(value = "formVersion", required = false) String formVersion,
			@RequestParam(required = false) String uiMetadataUri,
			@RequestParam(required = false) String masterId
			){
		
		UIMetaData uiMetaData=entityDao.find(UIMetaData.class, EntityId.fromUri(uiMetadataUri).getLocalId());
		ModelAndView modelAndView = new ModelAndView("dynamicFormTableRowDefinition");
		UIMetaDataVo uiMetaDataVo = mergeFormDetailsAndData(uiMetaData, new HashMap<String,Object>());
		List<FormComponentVO> uiComponents = uiMetaDataVo.getUiComponents();
		FormComponentVO tableElement = uiComponents.get(tableIndex);
		FormComponentVO firstTableElement = tableElement.getFormComponentList().get(0);
		
		if(StringUtils.isNotEmpty(masterId)) {
			String entityName = masterConfigurationRegistry.getEntityClass(masterId);
			Entity entityObj = EntityId.getNewInstance(entityName);
			((BaseMasterEntity) entityObj).setUiMetaDataVo(uiMetaDataVo);
			modelAndView.addObject("masterId", masterId);
			modelAndView.addObject(masterId, entityObj);
			modelAndView.addObject("isMasterDynamicForm", true);
		} else {
			modelAndView.addObject("isMasterDynamicForm", false);
		}
		modelAndView.addObject("tableSingleItem", firstTableElement);
		modelAndView.addObject("uiComponentsIndex", tableIndex);
		modelAndView.addObject("formComponentIndex", formComponentIndex);
		modelAndView.addObject("viewMode", false);
		modelAndView.addObject("formKey", uiMetaDataVo.getFormName().replaceAll(" ", "_"));
		modelAndView.addObject("tableSingleItemStatusFirst", false);
		modelAndView.addObject("offlineTemplate", false);
		modelAndView.addObject("uiMetaDataVo", uiMetaDataVo);
		
		
		return modelAndView;
	}
	
	
	@RequestMapping(value={"/displayDynamicFormNames"},method={RequestMethod.POST})
	public ModelAndView fetchDynamicFormsMapped(@RequestParam(required=false) String uri,@RequestParam(required=false) Long  screenId,@RequestParam(value = "productType", required = false) String productType,
 HttpSession session) {
		
		List<Map<String,String>> multipleDynamicFormDetails=null;
		Long sourceProductId=null;
		if(notNull(ProductInformationLoader.getProductName())){
		   SourceProduct sourceProduct=genericParameterService.findByCode(ProductInformationLoader.getProductName(), SourceProduct.class);
		   if(notNull(sourceProduct))
		   sourceProductId=sourceProduct.getId();
	        }
		
		if(StringUtils.isNotEmpty(uri) && !uri.equals("null") && notNull(screenId)){
			multipleDynamicFormDetails=formDefinitionUtility.fetchDynamicFormNamesMappedToEnityByUri(uri,screenId);
		}
		
		if((StringUtils.isEmpty(uri) || uri.equals("null")) && notNull(screenId) && ValidatorUtils.hasNoElements(multipleDynamicFormDetails) ){
			multipleDynamicFormDetails=formDefinitionUtility.fetchDynamicFormNamesMappedToScreenId(screenId,sourceProductId,productType);
		}
		
		ModelAndView modelAndView = new ModelAndView("multipleDynamicFormsInfo");
	
		modelAndView.addObject("screenId", screenId);
		modelAndView.addObject("multipleDynamicFormNames",convertToJSONString(multipleDynamicFormDetails));
		
		return modelAndView;
	}

	protected String convertToJSONString(Object object) {

		String jsonString = null;
		ObjectMapper mapper = getJacksonObjectMapper();
		try {
			jsonString = mapper.writeValueAsString(object);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Exception:" + e.getMessage(), e);
		}
		return jsonString;
	}
	
	   protected ObjectMapper getJacksonObjectMapper() {
			ObjectMapper mapper = new ObjectMapper();
			/*String dateFormat = GeneralUtility.getDisplayDateFormat(requestContext);
			mapper.setDateFormat(new SimpleDateFormat(dateFormat));*/
			return mapper;
		}
	
    /**
     * 
     * creates Dynamic Form Data for Demo
     * 
     * @param map
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/displayForm")
    public String createDynamicFormData(ModelMap map,
            @RequestParam(value = "formKey", required = false) String invocationPoint,
            @RequestParam(value = "uri", required = false) String uri,
            @RequestParam(value = "formName", required = false) String formName,
            @RequestParam(value = "packageName", required = false) String packageName,
            @RequestParam(value = "viewMode", required = false) String viewMode,
            @RequestParam(value = "formVersion", required = false) String formVersion,
            @RequestParam(value = "dynamicFormData", required = false) String dynamicFormData) {

        Map<String, Object> pfdPlusUiMap = formConfigurationMappingService.getUiMetaData(formName, invocationPoint, uri,
                formVersion);
        UIMetaData uiMetaData = (UIMetaData) pfdPlusUiMap.get(FormConfigurationConstant.UI_META_DATA);

        PersistentFormData persistentFormData = (PersistentFormData) pfdPlusUiMap
                .get(FormConfigurationConstant.PERSISTENT_FORM_DATA);

        Map<String, Object> dataMap = formService.loadPersistentDataMap(uri, formName, persistentFormData);
		//Calling method to auto populated the data in dynamic form when dataMap is empty
		if(MapUtils.isEmpty(dataMap)){
			dataMap = formDefinitionUtility.prepareDataToBePopulatedInDynamicForm(uiMetaData,uri,dynamicFormData);
		}
        Map<String,List<Map<String,Object>>> newData = new HashMap<>();
		boolean dataMapEmpty=true;
        if(MapUtils.isNotEmpty(dataMap)){
			dataMapEmpty=false;
		}
        try{
        	if(StringUtils.isNotBlank(uri)){
				Long id = EntityId.fromUri(uri).getLocalId();
				String entityName  = getEntityName(uri,URI_PATTERN);
				if(StringUtils.isNotBlank(entityName) && id!=null && notNull(uiMetaData) && CollectionUtils.isNotEmpty(uiMetaData.getPanelDefinitionList())){
					for(PanelDefinition pd : uiMetaData.getPanelDefinitionList()){
						if(pd.getPanelType()==FormContainerType.FIELD_TYPE_SPECIAL_TABLE){
							List<Map<String,Object>> newList = new ArrayList<>();
							long fromClasueLength = pd.getSpecialTable().getFromClause().length();
							String fromClause = pd.getSpecialTable().getFromClause().getSubString(1,(int)fromClasueLength);
							long selectClasueLength = pd.getSpecialTable().getSelectClause().length();
							String selectClause = pd.getSpecialTable().getSelectClause().getSubString(1,(int)selectClasueLength);
 							List<Integer> partyRoles = new ArrayList<>();

							if(StringUtils.isNotEmpty(pd.getSpecialTablePartyRoles())) {
								String[] partyRolesString = pd.getSpecialTablePartyRoles().split(",");

								for (int i = 0; i < partyRolesString.length; i++) {
									Integer partyRole = FormConfigurationConstant.dynamicFormPartyRoleMap.get(partyRolesString[i]);
									partyRoles.add(partyRole);
								}
							}else{
								partyRoles.addAll(Arrays.asList(0,1,2));
							}

							String query_with_join_and_where = SELECT_CLAUSE_FOR_AUTOPOPULATE+selectClause+")"+fromClause;
							JPAQueryExecutor<Map<String, String>> jpaExecutorForApplicationsSearch = new JPAQueryExecutor<Map<String, String>>(query_with_join_and_where);
							jpaExecutorForApplicationsSearch.addParameter("id",id);
							jpaExecutorForApplicationsSearch.addParameter("partyRoles",partyRoles);
							String entityForFromClause = getEntityName(fromClause,FROM_CLAUSE_PATTERN);
                            List<Map<String, String>> result =null;
							if(StringUtils.isNotBlank(entityForFromClause) && entityForFromClause.equalsIgnoreCase(entityName)){
								result =entityDao.executeQuery(jpaExecutorForApplicationsSearch);

								prepareCustomerData(dataMap, dataMapEmpty, pd, newList, result);

								if(CollectionUtils.isNotEmpty(newList)){
									newData.put(pd.getPanelKey(),newList);
								}
							}
							/*if(!dataMapEmpty && newList.size()==0){
								dataMap.put(pd.getPanelKey(),newData.get(pd.getPanelKey()));
							}*/
						}
					}
					if(dataMapEmpty && MapUtils.isNotEmpty(newData)){
						dataMap.putAll(newData);
					}
				}
			}
		}catch (Exception e){
			BaseLoggers.flowLogger.info(e.getMessage());
		}finally {
			BaseLoggers.flowLogger.info(SPECIAL_TABLE_BLOCK_EXECUTED);
		}

        UIMetaDataVo uiMetaDataVo = mergeFormDetailsAndData(uiMetaData, dataMap);

        if (viewMode != null && !viewMode.isEmpty()) {
            map.put("viewMode", viewMode);
        }

        map.put("uiMetaDataVo", uiMetaDataVo);
        map.put("uri", uri);
        map.put("offlineTemplate", false);
        if (null != uiMetaDataVo.getFormName()) {
            map.put("formKey", uiMetaDataVo.getFormName().replaceAll(" ", "_"));
        }

        map.put("packageName", packageName);
        return "genericFormDefinition";

    }

	private void prepareCustomerData(Map<String, Object> dataMap, boolean dataMapEmpty, PanelDefinition pd, List<Map<String, Object>> newList, List<Map<String, String>> result) {
		if(CollectionUtils.isNotEmpty(pd.getFieldDefinitionList()) && CollectionUtils.isNotEmpty(result)){
			int i = result.size();
			for(int j=0; j<i;j++){
				Map<String,Object> keyData = new HashMap<>();
				for(FieldDefinition fd : pd.getFieldDefinitionList()){
					Hibernate.initialize(fd.getSpecialTable());
					if(fd.getSpecialTable()!=null){
						keyData.put(fd.getFieldKey(),getValueByFieldKey(fd.getFieldKey(),result,j));
					}
				}
				String primaryValue = pd.getPanelKey()+"_"+pd.getSpecialTable().getPrimaryValue();
				if(!dataMapEmpty){
					Object obj = dataMap.get(pd.getPanelKey());
					if(notNull(obj) && obj instanceof List){
						List<Map> list = (List<Map>) obj;
						boolean customerNotInDataMap=false;
						for (Map map1 : list) {
							if(StringUtils.equalsIgnoreCase((String)keyData.get(primaryValue),(String) map1.get(primaryValue))){
								customerNotInDataMap=true;
								Set<String> specialKeys =  keyData.keySet();
								specialKeys.forEach(s -> {
									map1.put(s,keyData.get(s));
								});
							}
						}
						if(!customerNotInDataMap){
							list.add(keyData);
						}
					}else{
						newList.add(keyData);
						dataMap.put(pd.getPanelKey(),newList);
					}
				}else{
					newList.add(keyData);
				}
			}
		}else if(CollectionUtils.isNotEmpty(pd.getFieldDefinitionList()) && CollectionUtils.isEmpty(result)) {
			Object obj = dataMap.get(pd.getPanelKey());
			if (notNull(obj) && obj instanceof List) {
				List<Map> list = (List<Map>) obj;

				for (Map map1 : list) {
					Set<String> specialKeys = map1.keySet();
					specialKeys.forEach(s -> {
						map1.put(s, "");
					});
					break;
				}

			}
		}
	}

	private String getEntityName(String value,String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find())
        {
            return matcher.group(1);
        }else{
            return null;
        }
    }

    private String getValueByFieldKey(String fieldKey,List<Map<String, String>> result,int j){
		String str = fieldKey;
		String indexOfUnderScore[] = str.split("_");
		String resultKey = indexOfUnderScore[indexOfUnderScore.length-1];
        Map<String, String> map = result.get(j);
        String value;
        if(resultKey.equals("partyRole")){
        	String partyRoleString = String.valueOf(map.get(resultKey));
        	Integer partyRole = Integer.valueOf(partyRoleString);

        	if(partyRole.equals(0)) {
				value = FormConfigurationConstant.PRIMARY_APPLICANT;
			}else if(partyRole.equals(1)){
        		value=FormConfigurationConstant.CO_APPLICANT;
			}else{
        		value=FormConfigurationConstant.GUARANTOR;
			}
		}else {
			value = (String) map.get(resultKey);
		}
        if(StringUtils.isNotBlank(value)){
            return value;
        }else if(value == null){
        	return StringUtils.SPACE;
		}
    	return null;
	}

    /**
     * 
     * creates Dynamic Form Data for Demo
     * 
     * @param map
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/displayFormWithTemplate")
    public String createDynamicFormDataTemplate(ModelMap map,
            @RequestParam(value = "formKey", required = false) String formKey,
            @RequestParam(value = "uri", required = false) String uri,
            @RequestParam(value = "formName", required = false) String formName,
            @RequestParam(value = "packageName", required = false) String packageName,
            @RequestParam(value = "viewMode", required = false) String viewMode,
            @RequestParam(value = "formVersion", required = false) String formVersion) {

        Map<String, Object> pfdPlusUiMap = formConfigurationMappingService
                .getUiMetaData(formName, formKey, uri, formVersion);
        UIMetaData uiMetaData = (UIMetaData) pfdPlusUiMap.get(FormConfigurationConstant.UI_META_DATA);

        PersistentFormData persistentFormData = (PersistentFormData) pfdPlusUiMap
                .get(FormConfigurationConstant.PERSISTENT_FORM_DATA);

        Map<String, Object> dataMap = formService.loadPersistentDataMap(uri, formName, persistentFormData);

        UIMetaDataVo uiMetaDataVo = mergeFormDetailsAndData(uiMetaData, dataMap);

        if (viewMode != null && !viewMode.isEmpty()) {
            map.put("viewMode", viewMode);
        }
        map.put("uiMetaDataVo", uiMetaDataVo);
        map.put("uri", uri);
        map.put("offlineTemplate", false);
        if (null != uiMetaDataVo.getFormName()) {
            map.put("formKey", uiMetaDataVo.getFormName().replaceAll(" ", "_"));
        }

        map.put("packageName", packageName);
        return "genericFormDefinitionWthHlfmTemp";

    }

	@RequestMapping(value={"/saveDynamicPanelFormData"},method={RequestMethod.POST})
    public @ResponseBody String saveDynamicPanelFormData(UIMetaDataVo uiMetaDataVo,
										   @RequestParam(value="panelId")String panelId,
										   @RequestParam(value="uri",required=true)String uri){
		if(!isEmpty(panelId) && uiMetaDataVo!=null && !isEmpty(uri) && !uri.equals("null")){
			if(CollectionUtils.isNotEmpty(uiMetaDataVo.getUiComponents())){
				for(int i = 0; i< uiMetaDataVo.getUiComponents().size() ; i++){
					FormComponentVO formComponentVO = uiMetaDataVo.getUiComponents().get(i);
					if(!formComponentVO.getPanelKey().equalsIgnoreCase(panelId)){
						uiMetaDataVo.getUiComponents().remove(i);
						i--;
					}
				}
			}
			formService.savePanelData(formService.getJsonMapToSave(uiMetaDataVo),uri,uiMetaDataVo,panelId);
			return "true";
		}
		return "false";
	}

    @RequestMapping(value={"/saveDynamicFormData"},method={RequestMethod.POST})
    public @ResponseBody String saveDynamicFormData(UIMetaDataVo uiMetaDataVo,@RequestParam(value="uri",required=true)String uri){
    	
    	if(!isEmpty(uri) && !uri.equals("null")){
    		formService.saveDynamicFormData(uri,uiMetaDataVo);
    	}
    	
    	 return "success";
    }
    
    /**
     * 
     * saves Dynamic Form Data for
     * 
     * @param uiMetaDataVo
     * @param taskId
     */
    @RequestMapping(value = "/saveFormData")
    public @ResponseBody
    String saveDynamicFormData(UIMetaDataVo uiMetaDataVo, @RequestParam(value = "uri", required = false) String uri,
							   @RequestParam(value = "packageName", required = false) String packageName) {
    	String eventCode = EventCode.DYNAMIC_EVENT;
    	if(StringUtils.isNotEmpty(uri)){
    		if(uri.contains(FIV_NAME))
    			eventCode = EventCode.DYNAMIC_EVENT_FIV;
    		else  if(uri.contains(COLL_NAME))
    			eventCode = EventCode.DYNAMIC_EVENT_COLL;
		}
        return saveDynamicFormDataWithEvent(uiMetaDataVo,uri,packageName,false,eventCode);

    }

	/**
	 *
	 * Gets dropdown values for child in cascade select
	 * @param entityName
	 * @param parentName
	 * @param parentId
	 * @return
	 */
    @RequestMapping(value="/getCascadeDropdownData/{entityName}/{parentName}/{id}")
	public @ResponseBody Map<String, String> getCascadeDropdownData(@PathVariable("entityName") String entityName,
																	@PathVariable("parentName") String parentName,
																	@PathVariable("id") Long parentId){

		BaseLoggers.flowLogger.info("Getting cascade drop down data");
		BaseLoggers.flowLogger.info("Entity Name: " + entityName + " Parent Name: " + parentName + " Parent Id: " + parentId.toString());
		return formDefinitionService.getChildCascadeDropdownData(entityName, parentName, parentId);
	}
    @RequestMapping(value="/getCascadeDropdownData/{dynamicForm}/{entityName}/{binderName}/{parentName}/{id}")
   	public @ResponseBody Map<String, String> getCustomCascadeDropdownData(@PathVariable("entityName") String entityName,
   																	@PathVariable("dynamicForm") String dynamicForm,
   																	@PathVariable("parentName") String parentName,
   																	@PathVariable("binderName") String binderName,
   																	@PathVariable("id") Long parentId){

   		BaseLoggers.flowLogger.info("Getting cascade drop down data");
   		BaseLoggers.flowLogger.info("Entity Name: " + entityName + " Parent Name: " + parentName + " Parent Id: " + parentId.toString());
   		return formDefinitionService.getChildCascadeDropdownData(dynamicForm,entityName, binderName,parentName, parentId);
   	}


	public String saveDynamicFormDataWithEvent(UIMetaDataVo uiMetaDataVo, String uri, String packageName, Boolean isEventExecuted,String eventCode) {
		Class clazz = null;
		Entity object = null;
		Boolean ruleResult = null;
		Map<Object, Object> contextMap = new HashMap<>();

		if (null != packageName && !packageName.isEmpty()) {

			try {
				clazz = Class.forName(packageName);
				if (null != clazz) {
					object = (Entity) clazz.newInstance();
				}
			} catch (ClassNotFoundException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage());
			} catch (InstantiationException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage());
			} catch (IllegalAccessException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage());
			}
		}

		String formName = uiMetaDataVo.getFormName().replaceAll(" ","_");
		try {
			if(StringUtils.isNotEmpty(uri) && !isEventExecuted){
				String pName = uri.split(":")[0];
				Class baseClass =  Class.forName(pName);
				EntityId entityId = EntityId.fromUri(uri);
				Long id=null;
				if(entityId!=null) {
					id=entityId.getLocalId();
				}

				BaseEntity baseEntity = (BaseEntity) ruleSimulationService.find(id, baseClass);
		        contextMap = ruleSimulationService.populateContextObject(baseEntity, baseClass);
				contextMap.put("contextObject"+StringUtils.capitalize(formName)+"DataMap",formService.getJsonMapToSave(uiMetaDataVo));
				EventExecutionResult executionResult = eventExecutionService.fireEventExecution(formName.toUpperCase()+ eventCode, contextMap, null);
				if(executionResult!=null)
					ruleResult=executionResult.getValidationTask();
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Exception occured while dynamicForm Event trigger : ",e);
		}
		if(ruleResult==null || ruleResult) {
			if (null != object) {
				formService.saveNewObject(object);
				uri = object.getUri();
			}

			if (null != uri && !uri.isEmpty()) {
				formService.persistFormData(uri, formService.getJsonMapToSave(uiMetaDataVo), uiMetaDataVo);
			}
			return "success";
		}else{
			return getRuleErrorMessage(formName.toUpperCase()+eventCode,contextMap);
		}

	}

	private String getRuleErrorMessage(String eventCode,Map<Object,Object> contextMap){
		String ruleErrorMessage = "";
		List<String> errorMessageList = ruleService.getValidationRuleErrorMessages(
				eventCode, contextMap);
		if (CollectionUtils.isNotEmpty(errorMessageList)) {

			for (String errorMessage : errorMessageList) {
				if (StringUtils.isNotEmpty(errorMessage) && !errorMessage.equalsIgnoreCase("null")) {
					ruleErrorMessage = ruleErrorMessage.concat(errorMessage).concat("\n");
				}
				else{
					ruleErrorMessage = ruleErrorMessage.replace("null","Rule fail but no Error message is associated with rule.").concat("\n");
				}
			}
		}
		return ruleErrorMessage;
	}
}
