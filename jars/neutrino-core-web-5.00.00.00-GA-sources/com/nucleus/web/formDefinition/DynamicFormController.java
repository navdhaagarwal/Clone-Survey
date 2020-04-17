package com.nucleus.web.formDefinition;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.IOException;
import java.sql.SQLException;
import java.text.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.autocomplete.AutocompleteVO;
import com.nucleus.cas.eligibility.service.ProductProcessor;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.dynamicform.service.*;
import com.nucleus.core.formsConfiguration.*;
import com.nucleus.core.formsConfiguration.validationcomponent.*;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.*;
import com.nucleus.web.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Hibernate;
import org.joda.time.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.nucleus.contact.EMailType;
import com.nucleus.contact.PhoneNumberType;
import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.core.formDefinition.FormDefinitionUtility;
import com.nucleus.core.formsConfiguration.fieldcomponent.EmailTypeVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberTypeVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;

@Controller
@Transactional
@RequestMapping(value = "/dynamicForm")
@SessionAttributes({ "formVO", "componentList","formVoMap","isFilter", "formValidationVO","ifComponentList","whenList","fieldMap","panelKeyList"})
public class DynamicFormController extends BaseController {

    private static final String MASTER_DYNAMIC_FORM_CONS = "_MasterDynamicForm";
	private static final String  masterID   = "FormConfig";

    @Inject
    @Named("genericParameterService")
    private GenericParameterService    genericParameterService;

    @Inject
    @Named("formDefinitionService")
    private FormDefinitionService      formDefinitionService;

    @Inject
    @Named("formConfigService")
    private FormService               formService;
    
	@Inject
 	@Named("formDefinitionUtility")
 	protected FormDefinitionUtility formDefinitionUtility;

	@Inject
    @Named("makerCheckerService")
    private MakerCheckerService        makerCheckerService;
	
	@Inject
    @Named("messageSource")
    protected MessageSource              messageSourceObj;
    
	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;
	
    @Inject
    @Named("entityDao")
    private EntityDao                  entityDao;

    @Inject
    @Named("formConfigurationMappingService")
    FormConfigurationMappingService    formConfigurationMappingService;

    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;

	@Inject
	@Named("IDynamicFormValidationService")
	IDynamicFormValidationService dynamicFormValidationService;

	@Inject
	@Named("userService")
	private UserService userService;

    @Autowired(required = false)
	private AssignmentMatrixPopulation assignmentMatrixPopulation;
    
    private static String              MOVEMENT_UP         = "UP";
    private static String              MOVEMENT_DOWN       = "DOWN";
    private static String              MOVEMENT_LEFT       = "LEFT";
    private static String              MOVEMENT_RIGHT      = "RIGHT";
    private static String              ONE_COLUMN_LAYOUT   = "oneColumn";
    private static String              TWO_COLUMN_LAYOUT   = "twoColumn";
    private static String              THREE_COLUMN_LAYOUT = "threeColumn";
    private static String              FOUR_COLUMN_LAYOUT = "fourColumn";
    private static final List<Integer> MONTHS              = new ArrayList<Integer>();
    private static final String DynamicDtypeClass = "com.nucleus.core.genericparameter.entity.DynamicGenericParameter";
	private static final List<String> TIME_STAMP_APPLIED_COMPONETS=new ArrayList<>();

    static {
        for (int i = 1 ; i <= 12 ; i++) {
            MONTHS.add(i);
        }
		TIME_STAMP_APPLIED_COMPONETS.add(FormComponentType.CHECKBOX);
		TIME_STAMP_APPLIED_COMPONETS.add(FormComponentType.DROP_DOWN);
		TIME_STAMP_APPLIED_COMPONETS.add(FormComponentType.MULTISELECTBOX);
		TIME_STAMP_APPLIED_COMPONETS.add(FormComponentType.TEXT_AREA);
		TIME_STAMP_APPLIED_COMPONETS.add(FormComponentType.TEXT_BOX);
		TIME_STAMP_APPLIED_COMPONETS.add(FormComponentType.AUTOCOMPLETE);
		TIME_STAMP_APPLIED_COMPONETS.add(FormComponentType.MONEY);
		TIME_STAMP_APPLIED_COMPONETS.add(FormComponentType.CASCADED_SELECT);
		TIME_STAMP_APPLIED_COMPONETS.add(FormComponentType.CUSTOM_CASCADED_SELECT);
    }

    /**
     *  main page of creating dynamic page
     * @param map
     * @return
     */
    @RequestMapping(value = "/mainPage")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG')")
    public String openMainPage(ModelMap map) {
		FormValidationConstants.initMappings();
        FormVO formVO = new FormVO();
        formVO.setFormuuid(UUID.randomUUID().toString());
        formVO.setFormVersion(FormConfigurationConstant.FORM_VERSION_CONTROL + 1);
        formVO.setCreateNewVersion(true);
        formVO.setAllowSaveOption(true);
        formVO.setAllowBorder(false);
        SourceProduct sourceProduct=null;
        if(notNull(ProductInformationLoader.getProductCode())){
			sourceProduct=genericParameterService.findByCode(ProductInformationLoader.getProductCode(), SourceProduct.class);
			formVO.setSourceProduct(sourceProduct);
			if(notNull(sourceProduct)) {
				formVO.setSourceProductId(sourceProduct.getId());
			}
        }
        formVO.setColumnLayout(2);
        map.put("panelKeyList",new ArrayList<>());
        map.put("ifComponentList",new HashMap());
        map.put("formVO", formVO);
        map.put("isFilter", false);
        map.put("formVoMap",new HashMap<Long, FormVO>());
		map.put("masterID", masterID);
        map.put("componentList", genericParameterService.retrieveTypes(FormComponentType.class));
        map.put("actions", Arrays.asList(FormValidationConstants.ThenActionTypes.values()));
        // map of column layout
        Map<Integer,String> colLayout = new HashMap<Integer, String>();
        colLayout.put(2, messageSource.getMessage("label.dynamicForm.twocolumnlayout", null, configurationService.getSystemLocale()));
        colLayout.put(3, messageSource.getMessage("label.dynamicForm.threecolumnlayout", null, configurationService.getSystemLocale()));
        colLayout.put(4, messageSource.getMessage("label.dynamicForm.fourcolumnlayout", null, configurationService.getSystemLocale()));
        map.put("colLayout", colLayout);
		return "dynamicMainPage";
    }

    public FormContainerVO addComponentVo(String componentCode,
            String panelCode, FormVO formVo,
            List<FormComponentType> componentList, ModelMap map) {
    	return addComponentVo(componentCode,panelCode, formVo,componentList,  map,null,true);
    }
    
    public FormContainerVO addComponentVo(String componentCode,
            String panelCode, FormVO formVo,
            List<FormComponentType> componentList, ModelMap map,Integer index,boolean isparent) {
    	FormContainerVO formContainerVO = new FormContainerVO();
        if (componentCode != null && !componentCode.isEmpty()) {
            
            // intializing default value list
            List<String> defaultValue = new ArrayList<String>();
            formContainerVO.setDefaultValue(defaultValue);
            formContainerVO.setFieldType(componentCode);
            formContainerVO.setType(FormContainerType.FIELD_TYPE_FIELD);
            if (componentCode.equals(FormComponentType.PANEL)) {
                formContainerVO.setType(FormContainerType.FIELD_TYPE_PANEL);
                List<FormContainerVO> nestedComponentVO = new ArrayList<FormContainerVO>();
                formContainerVO.setFormContainerVOList(nestedComponentVO);

            } else if (componentCode.equals(FormComponentType.TABLE)) {
                formContainerVO.setType(FormContainerType.FIELD_TYPE_TABLE);
                List<FormContainerVO> nestedComponentVO = new ArrayList<FormContainerVO>();
                formContainerVO.setFormContainerVOList(nestedComponentVO);
            } else if (componentCode.equals(FormComponentType.SPECIAL_TABLE)) {
				formContainerVO.setType(FormContainerType.FIELD_TYPE_SPECIAL_TABLE);
				List<FormContainerVO> nestedComponentVO = new ArrayList<FormContainerVO>();
				formContainerVO.setFormContainerVOList(nestedComponentVO);
			}else if (isparent && (componentCode.equals(FormComponentType.CUSTOM_CASCADED_SELECT)|| componentCode.equals(FormComponentType.CASCADED_SELECT))) {
				formContainerVO.setFirstParent(Boolean.TRUE);
			}

            List<FormContainerVO> containerVOList = formVo.getContainerVOList();
            if (containerVOList == null) {
                containerVOList = new ArrayList<FormContainerVO>();
            }

            // this section shows that request is from main Form
            if (panelCode == null || panelCode.isEmpty()) {
            	updateOrCreateVirtualPanelAndAddField(formContainerVO,containerVOList,formVo.getColumnLayout());
            	
            }
            // this section shows that request is from some other panel
            else {
                // setting partial value of display key
                formContainerVO.setComponentDisplayKey(panelCode);
                DynamicFormUtil.addComponentAtPanel(formContainerVO, panelCode, containerVOList,index);
            }

            if (componentCode.equals(FormComponentType.DROP_DOWN) || componentCode.equals(FormComponentType.RADIO)
                    || componentCode.equals(FormComponentType.CHECKBOX)
                    || componentCode.equals(FormComponentType.AUTOCOMPLETE)) {
                // getting list of binders
                map.put("binderList", formDefinitionService.getEntityNameList());
            } 
            else if (componentCode.equals(FormComponentType.CASCADED_SELECT)){
            	// getting the list of parent binders
            	map.put("binderCascadeList", formDefinitionService.getCascadeEntityData());
            }else if (componentCode.equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
            	// getting the list of parent binders
            	getCustomCascadeSelectData(formVo, map,formContainerVO);
            }else if (componentCode.equals(FormComponentType.LOV)){
				map.put("lovKeyList", formDefinitionService.getLovKeyList());
			}
            
            if(formContainerVO.getMobile() == null ){
                formContainerVO.setMobile(false);
              }           
            
            formVo.setContainerVOList(containerVOList);
            
            if(FormComponentType.AUTOCOMPLETE.equals(componentCode))
            {
            	Map<String,String> availableIdMap = new HashMap<String, String>();
            	getAvailableIds(formVo.getContainerVOList(),availableIdMap);
            	map.put("parentIdList", availableIdMap);
            }
            
           //Code to get product for dropDown and TextBox fields
            List<ProductSchemeMetaData> productSchemeList = formService.getProductSchemeMetaDataColumn(componentCode);

            map.put("productSchemeList", productSchemeList);
			List<ProductSchemeMetaData> productSchemeListForAssignmentMatrix = formService.getProductSchemeMetaDataColumnForAssignmentMatrix(componentCode);
			map.put("productSchemeListForAssignmentMatrix",productSchemeListForAssignmentMatrix);
            map.put("dataTypeList", getSupportedDataTypes(componentCode, null));
            map.put("formContainerVO", DynamicFormUtil.updateContainerVO(formContainerVO, componentCode));
            map.put("formVO", formVo);
        }
        return formContainerVO;
       
    }

	private void getAvailableIds(List<FormContainerVO> containerVoList, Map<String,String> availableIdMap)
    {
    	for(FormContainerVO formContainerVo : containerVoList)
    	{
    		if(FormContainerType.FIELD_TYPE_FIELD!=formContainerVo.getType())
    		{
    			getAvailableIds(formContainerVo.getFormContainerVOList(), availableIdMap);
    		}
    		else if(formContainerVo.getFieldKey()!=null)
    			availableIdMap.put(formContainerVo.getFieldKey(), messageSourceObj.getMessage(formContainerVo.getFieldLabel(), null, configurationService.getSystemLocale() ));
    	}
    	
    }
    
    
	private void updateOrCreateVirtualPanelAndAddField(FormContainerVO formContainerVO, List<FormContainerVO> containerVOList,Integer columnLayout) {
		

		if(formContainerVO.getType()!=FormContainerType.FIELD_TYPE_FIELD)
		{
	        formContainerVO.setComponentDisplayKey("component[" + containerVOList.size() + "]");
	        containerVOList.add(formContainerVO);
			
		}
		else
		{
        	String panelCode=updateOrCreateVirtualPanel(formContainerVO,containerVOList,columnLayout);
        	formContainerVO.setComponentDisplayKey(panelCode);
        	DynamicFormUtil.addComponentAtPanel(formContainerVO, panelCode, containerVOList);

		}
	}


	private String updateOrCreateVirtualPanel(FormContainerVO formContainerVO, List<FormContainerVO> containerVOList,Integer columnLayout) {
		
		int currentPosition=containerVOList.size();
		int prevPosition=currentPosition-1;
		FormContainerVO parentPanel=null;
		if(prevPosition>=0 && containerVOList.get(prevPosition).getType()==FormContainerType.FIELD_TYPE_VIRTUAL)
		{
			parentPanel=containerVOList.get(prevPosition);
			return parentPanel.getComponentDisplayKey();
		}
		else
		{
			parentPanel=createVirtualPanel(containerVOList.size(),columnLayout);
			containerVOList.add(parentPanel);
			return parentPanel.getComponentDisplayKey();
		}
		
	}
	
	private FormContainerVO createVirtualPanel(int position,Integer columnLayout)
	{
		FormContainerVO virtualPanel = new FormContainerVO();
		virtualPanel.setType(FormContainerType.FIELD_TYPE_VIRTUAL);
		virtualPanel.setComponentDisplayKey("component[" + position + "]");
		virtualPanel.setPanelColumnLayout(columnLayout!=null ? columnLayout : 2);
		return virtualPanel;
	}

	protected void addMultipleComponents(String selectedFields[], Long selectedFormId, FormVO formVo,
			List<FormComponentType> componentList, Map<Long, FormVO> formVoMap, ModelMap map,boolean addAllFields)    
	{
    	FormVO selectedFormVo=formVoMap.get(selectedFormId);
		List<FormContainerVO> containerRootList=selectedFormVo.getContainerVOList();
		Map<String, FormContainerVO> formContainersMap=new LinkedHashMap<>();
		Map<String, FormContainerVO> newFormKeysMap=new LinkedHashMap<>();
		List<FormContainerVO> newFormContainers=formVo.getContainerVOList();
		if(newFormContainers==null)
		{
			newFormContainers=new ArrayList<FormContainerVO>();
		}
	    for(FormContainerVO containerVO:newFormContainers)
	    {
	    	addFormContainersByKey(containerVO,newFormKeysMap);
	    }
	    for(FormContainerVO containerVO:containerRootList)
	    {
	    	addFormContainersByKey(containerVO,formContainersMap);
	    }
	    if(addAllFields && ValidatorUtils.notNull(formContainersMap.keySet()))
	    {
	    	Set<String> keySet=formContainersMap.keySet();
	    	selectedFields=keySet.toArray(new String[keySet.size()]);
	    }
	    Map<Integer, FormContainerVO> addedPanelMap=new HashMap<Integer, FormContainerVO>();
	    for(String fieldKey:selectedFields)
	    {
	    	if(newFormKeysMap.containsKey(fieldKey) )
	    	{
	    		continue;
	    	}
	    	FormContainerVO selectedField=formContainersMap.get(fieldKey);
	    	int parentPanel=DynamicFormUtil.getParentPanelFromField(selectedField.getComponentDisplayKey());
	    	FormContainerVO addedContainerVO=null;
	    	FormContainerVO panelContainerVO =null;
	    	String parentDisplayKey=null;
	    	if(parentPanel!=-1 && addedPanelMap.get(parentPanel) ==null)
	    	{
	    		
	    		
	    		panelContainerVO = containerRootList.get(DynamicFormUtil.getComponentIndexFromCode("component[" + parentPanel + "]"));
	    		String panelKey=panelContainerVO.getFieldKey();
	    		if(panelContainerVO.getType() == FormContainerType.FIELD_TYPE_VIRTUAL){
	    			panelKey=selectedFormVo.getFormName()+"_"+parentPanel;
	    		}  		
	    		if(!newFormKeysMap.containsKey(panelKey))
    	    	{
	    			FormContainerVO addedPanelContainerVO=addComponentVo(panelContainerVO.getFieldType(),null, formVo,
		                    componentList, map);
		    		populateContainerValues(selectedFormVo.getFormName(),addedPanelContainerVO,panelContainerVO);
		    		addedPanelMap.put(parentPanel, addedPanelContainerVO);
		    		newFormKeysMap.put(panelKey,addedPanelContainerVO);
    	    	}
	    		
	    	}
	    	if(parentPanel!=-1)
	    	{
	    		parentDisplayKey=addedPanelMap.get(parentPanel) != null ? addedPanelMap.get(parentPanel).getComponentDisplayKey():null;
	    	}
	    	addedContainerVO=addComponentVo(selectedField.getFieldType(),parentDisplayKey, formVo,
	                    componentList, map);
	    		populateContainerValues(selectedFormVo.getFormName(),addedContainerVO,selectedField);
	    		newFormKeysMap.put(addedContainerVO.getFieldKey(),addedContainerVO);
	    		
	    	if(selectedField.getType() == FormContainerType.FIELD_TYPE_PANEL
                    || selectedField.getType() == FormContainerType.FIELD_TYPE_TABLE || selectedField.getType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE)
	    	{
	    		addedPanelMap.put(DynamicFormUtil.getComponentIndexFromCode(selectedField.getComponentDisplayKey()), addedContainerVO);
	    	}
	    }
	    
    }
    
    /**
     * adding new component in root as well as in panel
     * @param componentCode
     * @param panelCode
     * @param formVo
     * @param componentList
     * @param map
     * @return
     */
    @RequestMapping(value = "/addComponent", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    public String addComponent(@RequestParam("componentCode") String componentCode,
            @RequestParam(value = "panelCode", required = false) String panelCode, @ModelAttribute("formVO") FormVO formVo,
            @ModelAttribute("componentList") List<FormComponentType> componentList, ModelMap map) {

    	addComponentVo(componentCode,panelCode, formVo,
                componentList, map);
        return "componentDetailPage";
    }
    
    
    
    
    
    
    
    @RequestMapping(value = "/addMultipleComponents", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    public String addMultipleComponentsForSelectedForm(String selectedFields[],Long selectedFormId,@ModelAttribute("formVO") FormVO formVo,
            @ModelAttribute("componentList") List<FormComponentType> componentList, @ModelAttribute("formVoMap") Map<Long, FormVO> formVoMap,ModelMap map) {
    	
    	addMultipleComponents(selectedFields,selectedFormId, formVo, componentList, formVoMap, map,false);	
        return "componentDetailPage";
    }


    
    
    @RequestMapping(value = "/addAllComponents", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    public String addAllFieldsForSelectedForms(@ModelAttribute("formVO") FormVO formVo,
            @ModelAttribute("componentList") List<FormComponentType> componentList, @ModelAttribute("formVoMap") Map<Long, FormVO> formVoMap,ModelMap map) {
    	
    	//addMultipleComponents(selectedFields,selectedFormId,panelCode, formVo, componentList, formVoMap, map);
    	if(ValidatorUtils.hasNoEntry(formVoMap))
    	{
    		return "componentDetailPage";
    	}
    	for(Map.Entry<Long, FormVO> entry:formVoMap.entrySet())
    	{
    		addMultipleComponents(null,entry.getKey(), formVo, componentList, formVoMap, map,true);
    	}
        return "componentDetailPage";
    }


    
    
    

    private void addFormContainersByKey(FormContainerVO containerVO, Map<String, FormContainerVO> formContainersMap) {
    	if(containerVO.getFieldKey()!=null)
    	{
    		formContainersMap.put(containerVO.getFieldKey(), containerVO);
    	}
    	if(containerVO.getFormContainerVOList()!=null)
    	{
    		for(FormContainerVO childFields:containerVO.getFormContainerVOList())
    		{
    			addFormContainersByKey(childFields,formContainersMap);
    		}
    	}
		
	}


	private void populateContainerValues(String formName, FormContainerVO addedPanelContainerVO, FormContainerVO panelContainerVO) {
		
    	addedPanelContainerVO.setFieldKey(panelContainerVO.getFieldKey());
    	addedPanelContainerVO.setSourceFormName(formName);
    	addedPanelContainerVO.setFieldLabel(panelContainerVO.getFieldLabel());
    	addedPanelContainerVO.setDefaultValue(panelContainerVO.getDefaultValue());
    	addedPanelContainerVO.setEntityName(panelContainerVO.getEntityName());
    	addedPanelContainerVO.setBinderName(panelContainerVO.getBinderName());
    	addedPanelContainerVO.setMandatoryField(panelContainerVO.isMandatoryField());
		addedPanelContainerVO.setExpandableField(panelContainerVO.getExpandableField());
    	addedPanelContainerVO.setFieldDataType(panelContainerVO.getFieldDataType());
    	addedPanelContainerVO.setToolTipMessage(panelContainerVO.getToolTipMessage());
    	addedPanelContainerVO.setFieldValidationErrorMessage(panelContainerVO.getFieldValidationErrorMessage());
    	addedPanelContainerVO.setPanelName(panelContainerVO.getPanelName());
    	addedPanelContainerVO.setPanelHeader(panelContainerVO.getPanelHeader());
    	addedPanelContainerVO.setDescription(panelContainerVO.getDescription());
    	addedPanelContainerVO.setItemValue(panelContainerVO.getItemValue());
    	addedPanelContainerVO.setItemLabel(panelContainerVO.getItemLabel());
    	addedPanelContainerVO.setPanelColumnLayout(panelContainerVO.getPanelColumnLayout());
    	addedPanelContainerVO.setFieldCustomOptionsList(panelContainerVO.getFieldCustomOptionsList());
    	addedPanelContainerVO.setCustomeLongMessage(panelContainerVO.getCustomeLongMessage());
    	addedPanelContainerVO.setModelName(panelContainerVO.getModelName());
    	addedPanelContainerVO.setModelId(panelContainerVO.getModelId());
    	addedPanelContainerVO.setMaxFieldLength(panelContainerVO.getMaxFieldLength());
    	addedPanelContainerVO.setMinFieldLength(panelContainerVO.getMinFieldLength());
    	addedPanelContainerVO.setMaxFieldValue(panelContainerVO.getMaxFieldValue());
    	addedPanelContainerVO.setMinFieldValue(panelContainerVO.getMinFieldValue());
    	addedPanelContainerVO.setFieldValue(panelContainerVO.getFieldValue());
    	if(panelContainerVO.getAutoCompleteColumnsHolder()!=null)
    	{
    		List<String > autoCompleteColumnsHolder=panelContainerVO.getAutoCompleteColumnsHolder();
    		addedPanelContainerVO.setAutoCompleteColumnsHolder(autoCompleteColumnsHolder.toString());
    	}
    	
    	addedPanelContainerVO.setPhoneNumberVO(panelContainerVO.getPhoneNumberVO());
    	addedPanelContainerVO.setMobile(panelContainerVO.getMobile());
    	addedPanelContainerVO.setEmailInfoVO(panelContainerVO.getEmailInfoVO());
    	addedPanelContainerVO.setDefaultMonth(panelContainerVO.getDefaultMonth());
    	addedPanelContainerVO.setDefaultYear(panelContainerVO.getDefaultYear());
    	addedPanelContainerVO.setIncludeSelect(panelContainerVO.isIncludeSelect());
    	/*added for hyperlink type*/
		addedPanelContainerVO.setHref(panelContainerVO.getHref());
		addedPanelContainerVO.setFunctionLogic(panelContainerVO.getFunctionLogic());
		addedPanelContainerVO.setAuthority(panelContainerVO.getAuthority());
		
		/*
		 * added for autocomplete types
		 */
		addedPanelContainerVO.setErrorMessageCode(panelContainerVO.getErrorMessageCode());
		addedPanelContainerVO.setParentColumn(panelContainerVO.getParentColumn());
		addedPanelContainerVO.setParentFieldId(panelContainerVO.getParentFieldId());
		addedPanelContainerVO.setMainFormDependant(panelContainerVO.isMainFormDependant());

		addedPanelContainerVO.setLovKey(panelContainerVO.getLovKey());
		addedPanelContainerVO.setLovFieldVO(panelContainerVO.getLovFieldVO());

	}


	/**
     *  refreshing preview page
     * @param formVo
     * @param map
     * @return
     */
    @RequestMapping(value = "/refreshPreviewSection")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG')")
    public String refreshPreviewSection(@RequestParam(value = "viewMode", required = false) String viewMode,@ModelAttribute("formVO") FormVO formVo, ModelMap map) {
        map.put("formVO", formVo);
        if(StringUtils.isEmpty(viewMode)){
        	viewMode = "false";
        }
        map.put("viewable", viewMode);
        return "previewSection";
    }

    /**
     *  offlineTemplate
     * @param formVo
     * @param map
     * @return
     */
    @RequestMapping(value = "/getOfflineTemplate")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG')")
    public String getOfflineTemplate(@ModelAttribute("formVO") FormVO formVo, ModelMap map) {
        map.put("formVO", formVo);
        return "dynamicOfflineTemplate";
    }

    /**
     *  updating component details
     * @param containerVO
     * @param formVo
     * @param map
     * @return
     */

    @RequestMapping(value = "/updateComponent", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @ResponseBody
    public String updateComponent(@ModelAttribute("formContainerVO") FormContainerVO containerVO,
            @ModelAttribute("formVO") FormVO formVo, ModelMap map) {
        String fieldKey = containerVO.getFieldKey();
                
        if (null != fieldKey) {
            fieldKey = fieldKey.trim().replace(" ", "_");
            containerVO.setFieldKey(fieldKey);
        }
        // removing deleted custom value
        if (CollectionUtils.isNotEmpty(containerVO.getFieldCustomOptionsList())) {
            for (Iterator iterator = containerVO.getFieldCustomOptionsList().iterator() ; iterator.hasNext() ;) {
                FieldCustomOptionsVO fieldCustomOptionsVO = (FieldCustomOptionsVO) iterator.next();
                if (fieldCustomOptionsVO.getCustomeItemLabel().isEmpty()
                        || fieldCustomOptionsVO.getCustomeItemValue().isEmpty()) {
                    iterator.remove();
                }
            }
        }
        // updating panel component in case of panel detail update.
        if (containerVO.getFieldType().equals(FormComponentType.PANEL)
                || containerVO.getFieldType().equals(FormComponentType.TABLE) || containerVO.getFieldType().equals(FormComponentType.SPECIAL_TABLE)) {
            int index = DynamicFormUtil.getComponentIndexFromCode(containerVO.getComponentDisplayKey());
            if (index != -1 && formVo.getContainerVOList().get(index).getFormContainerVOList() != null
                    && !formVo.getContainerVOList().get(index).getFormContainerVOList().isEmpty()) {
                containerVO.setFormContainerVOList(formVo.getContainerVOList().get(index).getFormContainerVOList());
            }
		}
        if (containerVO.getFieldType().equals(FormComponentType.PHONE)) {
            PhoneNumberVO phoneNumberVO = containerVO.getPhoneNumberVO();
            if (containerVO != null && phoneNumberVO != null && phoneNumberVO.getNumberTypeVO() != null) {
                if (phoneNumberVO.getNumberTypeVO().getCode().equals(PhoneNumberTypeVO.MOBILE_NUMBER)) {
                    containerVO.setMobile(true);
                } else {
                    containerVO.setMobile(false);
                }
            }
        }
        else if(containerVO.getFieldType().equals(FormComponentType.CASCADED_SELECT) || containerVO.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
			List<Integer> nestedPanelPosition = DynamicFormUtil
					.getPanelComponentIndex(containerVO.getComponentDisplayKey());
			List<FormContainerVO> rootContainerList = formVo.getContainerVOList();
			List<FormContainerVO> nestedList = rootContainerList.get(nestedPanelPosition.get(0))
					.getFormContainerVOList();
			FormContainerVO formContVo = nestedList.get(nestedPanelPosition.get(1).intValue());
			if("GenericParameter".equalsIgnoreCase(formContVo.getBinderName())){
				formContVo.setBinderName(containerVO.getBinderName());
			}
			if (containerVO.getCurrentChildEntityName() != null && !containerVO.getCurrentChildEntityName().isEmpty()
					&& (containerVO.getPreviousChildEntityName().isEmpty()
							|| containerVO.getPreviousChildEntityName() == null)) {
        		// creating child VO and appending
        		createChildContainerVo(formVo, containerVO, map);
        		containerVO.setPreviousChildEntityName(containerVO.getCurrentChildEntityName());
        	}
			else if ((containerVO.getCurrentChildEntityName() != null && !containerVO.getCurrentChildEntityName().isEmpty()
					 && containerVO.getPreviousChildEntityName()!=null	&& !containerVO.getPreviousChildEntityName().isEmpty() 
					 && !(containerVO.getCurrentChildEntityName().equals(containerVO.getPreviousChildEntityName())))
								||
					  (containerVO.getBinderName()!=null && !containerVO.getBinderName().isEmpty()
						&& formContVo.getBinderName()!=null && !formContVo.getBinderName().isEmpty()
						&& !(formContVo.getBinderName().equals(containerVO.getBinderName())))) {
        		
        			//Removing all child of updated component
        		//	String deleteContainerEntityName = containerVO.getPreviousChildEntityName();
        		//	List<Integer> panelPosition = DynamicFormUtil.getPanelComponentIndex(containerVO.getComponentDisplayKey());
        		//	List<FormContainerVO> listVo = formVo.getContainerVOList().get(panelPosition.get(0)).getFormContainerVOList();
					/*for (int i = 0 ; i < listVo.size(); i++) {
						if (CollectionUtils.isNotEmpty(nestedList) 
								&& listVo.get(i).getFieldType().equals(FormComponentType.CASCADED_SELECT)) {
								// Added By Shikhar
								for (Iterator iterator2 = nestedList.iterator(); iterator2.hasNext();) {
									FormContainerVO formContainerVO = (FormContainerVO) iterator2.next();
									if (formContainerVO != null && formContainerVO.getFieldType() != null
											&& formContainerVO.getFieldType().equals(FormComponentType.CASCADED_SELECT)
											&& formContainerVO.getParentFieldKey() != null
											&& formContainerVO.getParentFieldKey().equals(temp.getFieldKey())) {
										temp = formContainerVO;
										iterator2.remove();
										break;
										}
									}
							//	break;
						}
					}*/
					FormContainerVO temp = containerVO;
        			Boolean breakCondition = true;
        			Integer count = 0;
        			while(breakCondition || count <= nestedList.size()){
        				count++;
        				for (Iterator iterator2 = nestedList.iterator(); iterator2.hasNext();) {
							FormContainerVO formContainerVO = (FormContainerVO) iterator2.next();
							if (formContainerVO != null && formContainerVO.getFieldType() != null
									&& (formContainerVO.getFieldType().equals(FormComponentType.CASCADED_SELECT) ||formContainerVO.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT))
									&& formContainerVO.getParentFieldKey() != null
									&& formContainerVO.getParentFieldKey().equals(temp.getFieldKey())) {
								temp = formContainerVO;
								iterator2.remove();
								breakCondition = true;
								break;
								}else{
									breakCondition = false;
								}
							}
        			}
					// udpating component ids after deltion
					 for (int j = 0; j < nestedList.size(); j++) {
							if(nestedPanelPosition != null) {
								nestedList.get(j).setComponentDisplayKey("component[" + nestedPanelPosition.get(0) + "][" + j + "]");
							}
							if(nestedList.get(j).getFieldKey().equals(containerVO.getFieldKey())){
								containerVO.setComponentDisplayKey("component[" + nestedPanelPosition.get(0) + "][" + j + "]");
							}
                     }
        			//Create new Child 
					if(containerVO.getCurrentChildEntityName() != null && !containerVO.getCurrentChildEntityName().isEmpty()){
						createChildContainerVo(formVo, containerVO, map);
            			containerVO.setPreviousChildEntityName(containerVO.getCurrentChildEntityName());
					}
        	}
    
        }

        DynamicFormUtil.updateComponentAtPanel(formVo.getContainerVOList(), containerVO,
        DynamicFormUtil.getPanelComponentIndex(containerVO.getComponentDisplayKey()));
        map.put("binderList", formDefinitionService.getEntityNameList());
        map.put("dataTypeList", getSupportedDataTypes(containerVO.getFieldType(), containerVO.getBinderName()));

        if(containerVO.getFieldType().equals(FormComponentType.SPECIAL_TABLE)){
			updateSpecialComponent(containerVO, formVo, map);
		}
		//Code to get product for dropDown and TextBox fields
		List<ProductSchemeMetaData> productSchemeList = formService.getProductSchemeMetaDataColumn(containerVO.getFieldType());

		map.put("productSchemeList", productSchemeList);
		List<ProductSchemeMetaData> productSchemeListForAssignmentMatrix = formService.getProductSchemeMetaDataColumnForAssignmentMatrix(containerVO.getFieldType());
		map.put("productSchemeListForAssignmentMatrix",productSchemeListForAssignmentMatrix);
		map.put("formVO", formVo);
        return "success";
    }

	private boolean checkForPanelExclusionInRule(FormContainerVO containerVO, FormVO formVo) {
		List<Integer> arrayList = DynamicFormUtil.getPanelComponentIndex(containerVO.getComponentDisplayKey());
		if(CollectionUtils.isNotEmpty(arrayList) && arrayList.size()==2){
			int type = formVo.getContainerVOList().get(arrayList.get(0)).getType();
			if(type == FormContainerType.FIELD_TYPE_PANEL && containerVO.isMandatoryField()){
				String panelKey = formVo.getContainerVOList().get(arrayList.get(0)).getFieldKey();
				if(CollectionUtils.isNotEmpty(formVo.getValidationsVO())){
					for(FormValidationMetadataVO formValidationMetadataVO : formVo.getValidationsVO()){
						if(CollectionUtils.isNotEmpty(formValidationMetadataVO.getThenActions())){
							for(FormValidationRulesThenMetadataVO formValidationRulesThenMetadataVO : formValidationMetadataVO.getThenActions()){
								if(formValidationRulesThenMetadataVO.getTargetFieldKey()!=null && formValidationRulesThenMetadataVO.getTargetFieldKey().equalsIgnoreCase(panelKey))
								{
									return true;
								}
							}
						}
					}
				}
			}
		}
    	return false;
	}

	private void updateSpecialComponent(@ModelAttribute("formContainerVO") FormContainerVO containerVO, @ModelAttribute("formVO") FormVO formVo, ModelMap map) {
    	if(CollectionUtils.isNotEmpty(formVo.getContainerVOList())){
			formVo.getContainerVOList().forEach(cv->{
				if(cv.getFieldKey()!=null && cv.getFieldKey().equalsIgnoreCase(containerVO.getFieldKey()) && containerVO.getFieldType().equals(FormComponentType.SPECIAL_TABLE)){
					boolean flag=false;
					if(CollectionUtils.isNotEmpty(cv.getFormContainerVOList())){
						Iterator<FormContainerVO> iterator = cv.getFormContainerVOList().iterator();
						while (iterator.hasNext()){
                            FormContainerVO formContainerVO = iterator.next();
                            if(StringUtils.isNotBlank(formContainerVO.getSpecialTable())){
                                iterator.remove();
                                flag=true;
                            }
                        }
					}
					if(flag){
						for(int i=0;i<cv.getFormContainerVOList().size();i++){
							List<Integer> indexInsidePanelList = DynamicFormUtil.getPanelComponentIndex(cv.getFormContainerVOList().get(i)
									.getComponentDisplayKey());
							cv.getFormContainerVOList().get(i).setComponentDisplayKey("component[" + indexInsidePanelList.get(0) + "][" + i + "]");
						}
					}
				}
			});
		}

		String key = containerVO.getSpecialTable();
		if(StringUtils.isNotBlank(key) && containerVO.getSpecialColumns().length>0){
			SpecialTable specialTable = formService.getSpecialTable(key);
			String primaryValue = specialTable.getPrimaryValue();
			ArrayList<String> list = new ArrayList<>(Arrays.asList(containerVO.getSpecialColumns()));
			list.add(primaryValue);
			String [] newArray = list.toArray(new String[list.size()]);
			containerVO.setSpecialColumns(newArray);

			for(int i=0;i<containerVO.getSpecialColumns().length;i++){
					addComponent(FormComponentType.TEXT_BOX,containerVO.getComponentDisplayKey(),formVo,null,map);
			}
			if(CollectionUtils.isNotEmpty(formVo.getContainerVOList())){
				formVo.getContainerVOList().forEach(cv->{
					if(cv.getComponentDisplayKey().equalsIgnoreCase(containerVO.getComponentDisplayKey())){
						int index= 0;
						if(CollectionUtils.isNotEmpty(cv.getFormContainerVOList())){
							for(FormContainerVO dd: cv.getFormContainerVOList()){
								StringBuilder stringBuilder = new StringBuilder();
								if(StringUtils.isBlank(dd.getFieldKey())){
									stringBuilder.append(containerVO.getFieldKey()+"_"+containerVO.getSpecialColumns()[index]);
									index++;
									dd.setSpecialTable(containerVO.getSpecialTable());
									dd.setFieldKey(stringBuilder.toString());
									dd.setFieldDataType(FieldDataType.DATA_TYPE_TEXT);
									dd.setFieldLabel(containerVO.getPanelHeader());
								}
							}
						}
					}
				});
			}
		}
	}

	// Method to create child form container Vo
    private FormContainerVO createChildContainerVo(FormVO formVo,FormContainerVO containerVO,ModelMap map){
    	String panelCode = null;
    	FormContainerVO childVo = null;
		for(FormContainerVO containerVo : formVo.getContainerVOList()){
			if(containerVo != null && containerVo.getFormContainerVOList() != null){
				for(FormContainerVO childContainerVo : containerVo.getFormContainerVOList()){
					if(childContainerVo.getComponentDisplayKey().equals(containerVO.getComponentDisplayKey())){
						panelCode  = containerVo.getComponentDisplayKey();
						break;
					}
				}
			}
		}
		List<Integer> panelPosition = DynamicFormUtil.getPanelComponentIndex(containerVO.getComponentDisplayKey());
		if(panelCode != null){
			if(containerVO.getFieldType().equalsIgnoreCase(FormComponentType.CUSTOM_CASCADED_SELECT)){
				childVo = addComponentVo(FormComponentType.CUSTOM_CASCADED_SELECT.toString(), panelCode, formVo, null, map,panelPosition.get(1),false);
			}else{
				childVo = addComponentVo(FormComponentType.CASCADED_SELECT.toString(), panelCode, formVo, null, map,panelPosition.get(1),false);
			}
		}else{
			if(containerVO.getFieldType().equalsIgnoreCase(FormComponentType.CUSTOM_CASCADED_SELECT)){
			childVo = addComponentVo(FormComponentType.CUSTOM_CASCADED_SELECT.toString(), null, formVo, null, map,panelPosition.get(1),false);
			}else{
				childVo = addComponentVo(FormComponentType.CASCADED_SELECT.toString(), null, formVo, null, map,panelPosition.get(1),false);
			}
		}
		
		FormConfigEntityData configEntityData;
		if(containerVO.getFieldType().equalsIgnoreCase(FormComponentType.CUSTOM_CASCADED_SELECT)){
			configEntityData = formDefinitionService.getFormConfigData(containerVO.getCurrentChildEntityName());
		}else{
			configEntityData = formDefinitionService.getFormConfigDataByParentChild(containerVO.getCurrentChildEntityName(),containerVO.getBinderName());
		}
		childVo.setBinderName(configEntityData.getWebDataBinderName());
		childVo.setEntityName(configEntityData.getEntityName());
		childVo.setParentFieldKey(containerVO.getFieldKey());
		childVo.setUrlCascadeSelect(containerVO.getUrlCascadeSelect());
		childVo.setParentId(configEntityData.getParent()!=null ? configEntityData.getParent().getId() : null);
		childVo.setItemValue(configEntityData.getItemValue());
		childVo.setItemLabel(configEntityData.getItemLabel());
		return childVo;
    }

    @RequestMapping(value = "/validateKey")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG')")
    @ResponseBody
    public String validateFieldKey(@ModelAttribute("formContainerVO") FormContainerVO containerVO,
            @ModelAttribute("formVO") FormVO formVo, ModelMap map) {
        
        if (StringUtils.isNotEmpty(containerVO.getPreviousKey()) && StringUtils.isNotEmpty(containerVO.getFieldKey())
                && !containerVO.getFieldKey().equals(containerVO.getPreviousKey()) && formVo.getDedupeKeySet() != null
                && formVo.getDedupeKeySet().size() > 0 && formVo.getDedupeKeySet().contains(containerVO.getPreviousKey())) {
            return "dedupeKeyMapped";
        }
		boolean isPanelLogicExist = checkForPanelExclusionInRule(containerVO,formVo);
		if(isPanelLogicExist){
			return "true";
		}
        boolean idFieldKeyValid = validateFormFieldKey(formVo, containerVO);
        if (idFieldKeyValid) {
        	
        	List<String> list= formDefinitionService.getUiMetaDataForFieldKey(formVo.getFormName(),containerVO.getFieldKey(),formVo.getSourceProductId());
        	
        	if(containerVO!=null && containerVO.getFieldKey()!=null && ValidatorUtils.hasElements(list))
        	{
        		return list.toString();
        	}
            return "valid";
        } else {
            return "invalid";
        }
    }
    
   

    /**
     * 
     * Method to validate form fields keys
     * @param formVo
     * @return
     */

    private boolean validateFormFieldKey(FormVO formVo, FormContainerVO containerVO) {
        String fieldKey = null;
        List<FormContainerVO> containerVOList = formVo.getContainerVOList();
        fieldKey = containerVO.getFieldKey();

        if (null != fieldKey) {
            fieldKey = fieldKey.trim().replace(" ", "_");
            containerVO.setFieldKey(fieldKey);
        }

        if (null != containerVOList && !containerVOList.isEmpty()) {
            for (FormContainerVO formContainerVO : containerVOList) {

                if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_FIELD) {
                    if (null != formContainerVO.getFieldKey() && formContainerVO.getFieldKey().equals(fieldKey)
                            && !(formContainerVO.getComponentDisplayKey().equals(containerVO.getComponentDisplayKey()))) {
                        return false;
                    }

                } else if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_PANEL
                		 || formContainerVO.getType() == FormContainerType.FIELD_TYPE_TABLE
                         || formContainerVO.getType() == FormContainerType.FIELD_TYPE_VIRTUAL) {

                    if (null != formContainerVO.getFieldKey() && formContainerVO.getFieldKey().equals(fieldKey)
                            && !(formContainerVO.getComponentDisplayKey().equals(containerVO.getComponentDisplayKey()))) {
                        return false;
                    }

                    if (formContainerVO.getFormContainerVOList() != null
                            && !formContainerVO.getFormContainerVOList().isEmpty()) {
                        for (FormContainerVO formContainerVO2 : formContainerVO.getFormContainerVOList()) {
                            if (null != formContainerVO2.getFieldKey()
                                    && formContainerVO2.getFieldKey().equals(fieldKey)
                                    && !(formContainerVO2.getComponentDisplayKey().equals(containerVO
                                            .getComponentDisplayKey()))) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     *  for viewing detail of component with component display key = componentid
     * @param componentId
     * @param formVO
     * @param componentList
     * @param map
     * @return
     */
    @RequestMapping(value = "/viewComponent/{componentId}")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG')")
    public String viewComponent(@RequestParam(value = "viewMode", required = false) String viewMode,
    		@PathVariable("componentId") String componentId, @ModelAttribute("formVO") FormVO formVO,
            @ModelAttribute("componentList") List<FormComponentType> componentList,@ModelAttribute("isFilter") Boolean isFilter, ModelMap map) {
        if (componentId != null && !componentId.isEmpty()) {
            List<Integer> nestedpathLocation = DynamicFormUtil.getPanelComponentIndex(componentId);
            FormContainerVO formContainerVO = null;
            if (nestedpathLocation != null) {
                // root component
                if (nestedpathLocation.size() == 1 && formVO.getContainerVOList().size() > nestedpathLocation.get(0)) {
                    formContainerVO = formVO.getContainerVOList().get(nestedpathLocation.get(0));
                }// panel component
                else if (nestedpathLocation.size() == 2) {
                    FormContainerVO parentContainerVO = formVO.getContainerVOList().get(nestedpathLocation.get(0));
                    formContainerVO = parentContainerVO.getFormContainerVOList().get(nestedpathLocation.get(1));
					//Code to get product for dropDown and TextBox fields
					if(parentContainerVO!=null && (parentContainerVO.getFieldType() == null || (parentContainerVO.getFieldType() != null && (parentContainerVO.getFieldType().equalsIgnoreCase(FormComponentType.PANEL))))){
						List<ProductSchemeMetaData> productSchemeList = formService.getProductSchemeMetaDataColumn(formContainerVO.getFieldType());
						map.put("productSchemeList", productSchemeList);
						List<ProductSchemeMetaData> productSchemeListForAssignmentMatrix = formService.getProductSchemeMetaDataColumnForAssignmentMatrix(formContainerVO.getFieldType());
						map.put("productSchemeListForAssignmentMatrix",productSchemeListForAssignmentMatrix);
					}
                }
            }
            if (null != formContainerVO) {
                map.put("binderList", formDefinitionService.getEntityNameList());
                if (null != formContainerVO.getFieldType()) {
                    map.put("dataTypeList",
                            getSupportedDataTypes(formContainerVO.getFieldType(), formContainerVO.getBinderName()));
                }
				if(StringUtils.isNotEmpty(formContainerVO.getFieldType()) && formContainerVO.getFieldType().equalsIgnoreCase(FormComponentType.CURRENT_TIME_STAMP)){
                	List<FormContainerVO> formContainerVOS=formVO.getContainerVOList();

					List<String> fieldsKeyList=new ArrayList<>();
					if(CollectionUtils.isNotEmpty(formContainerVOS)){
						for(FormContainerVO formContainer:formContainerVOS){

							List<FormContainerVO> formFieldsKeyList=formContainer.getFormContainerVOList().stream().filter(value -> value.getFieldKey() != null &&
									!FormComponentType.CURRENT_TIME_STAMP.equalsIgnoreCase(value.getFieldType()) &&
									TIME_STAMP_APPLIED_COMPONETS.contains(value.getFieldType())).collect(Collectors.toList());
							List<String> fieldKeyList=formFieldsKeyList.stream().map(FormContainerVO::getFieldKey).collect(Collectors.toList());
							fieldsKeyList.addAll(fieldKeyList);
						}
					}
					map.put("fieldsKeyList",fieldsKeyList);
				}
                if(StringUtils.isNotEmpty(formContainerVO.getFieldType()) && formContainerVO.getFieldType().equalsIgnoreCase(FormComponentType.SPECIAL_TABLE) && StringUtils.isNotBlank(formContainerVO.getSpecialTable())){
                    String key = formContainerVO.getSpecialTable();
                    SpecialTable specialTable = formService.getSpecialTable(key);
					String primaryColumn = specialTable!=null?specialTable.getPrimaryValue():null;
                    try{
                        List<SpecialColumnVo> specialColumnVos = getSpecialColumnVos(specialTable, primaryColumn);
                        map.put("specialColumns",specialColumnVos);

						map.put("primaryColumn",primaryColumn);
						map.put("description",specialTable!=null?specialTable.getDescription():null);
                    }catch (SQLException e){
                        BaseLoggers.flowLogger.info(e.getMessage());
                    }
                }
                if(StringUtils.isNotEmpty(formContainerVO.getFieldType()) && formContainerVO.getFieldType().equalsIgnoreCase(FormComponentType.SPECIAL_TABLE)){
					List<SpecialColumnVo> partyRoles = new ArrayList<>();
					for(String applicantType : FormConfigurationConstant.dynamicFormPartyRoleMap.keySet()) {
						SpecialColumnVo obj1 = new SpecialColumnVo();
						obj1.setValue(applicantType);
						partyRoles.add(obj1);
					}
					if(formContainerVO.getPartyRoles()==null){
						String [] arr = {FormConfigurationConstant.PRIMARY_APPLICANT,FormConfigurationConstant.CO_APPLICANT,FormConfigurationConstant.GUARANTOR};
						formContainerVO.setPartyRoles(arr);
					}
					map.put("partyRoles",partyRoles);

				}

               if(formContainerVO.getFieldType()!=null && (formContainerVO.getFieldType().equals(FormComponentType.CASCADED_SELECT) || formContainerVO.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT))){
                	//getting the list of parent binders
                	map.put("binderCascadeList", formDefinitionService.getCascadeEntityData());
                	if(formContainerVO.getEntityName() != null){
                		FormConfigEntityData configEntityData = formDefinitionService.getFormConfigData(formContainerVO
                				.getEntityName(), formContainerVO.getParentId());
                		formContainerVO.setItemLabel(configEntityData.getItemLabel());
                		formContainerVO.setItemValue(configEntityData.getItemValue());
						formContainerVO.setUrlCascadeSelect(configEntityData.getUrl());
                		//getting the list of binder Names
						if("GenericParameter".equalsIgnoreCase(formContainerVO.getEntityName())){
		                	map.put("binderNameList", getGenericParameterList());
		                }else {
                		map.put("binderNameList", configEntityData.getWebDataBinderName().split(","));
		                }
                		formContainerVO.setPreviousChildEntityName(formContainerVO.getCurrentChildEntityName());
                		if(formContainerVO.getParentFieldKey() != null){
                			map.put("binderChildEntityList", Arrays.asList(formDefinitionService.getFormConfigData(formContainerVO.getEntityName(),formContainerVO.getParentId())));
                		}
                		/*if(formContainerVO.getParentFieldKey() == null || formContainerVO.getParentFieldKey().isEmpty()){
                			//getting the list of child binders for selected parent
                			if(formContainerVO.getPreviousChildEntityName() == null){
                				//required for edit mode
                				formContainerVO.setPreviousChildEntityName(formContainerVO.getCurrentChildEntityName());
                			}
                			map.put("childBinderCascadeList", formDefinitionService.getChildCascadeEntityData(formContainerVO.getEntityName()));
                		}else if(formContainerVO.getParentFieldKey() != null){
                			//getting the single object in a list for child (for display only) 
                			map.put("binderChildEntityList", Arrays.asList(formDefinitionService.getFormConfigData(formContainerVO.getEntityName())));
                			map.put("childBinderCascadeList", formDefinitionService.getChildCascadeEntityData(formContainerVO.getEntityName()));
                		}*/
            			map.put("childBinderCascadeList", formDefinitionService.getChildCascadeEntityData(formContainerVO.getEntityName()));

                	}
                }
                // attaching binder name if entity name is present as required in edit mode
                if(!(formContainerVO.getFieldType()!=null && (formContainerVO.getFieldType().equals(FormComponentType.CASCADED_SELECT) || formContainerVO.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)))){
                	if (formContainerVO.getEntityName() != null) {
                		FormConfigEntityData configEntityData = formDefinitionService.getFormConfigData(formContainerVO
                				.getEntityName(), formContainerVO.getParentId());
                		formContainerVO.setItemLabel(configEntityData.getItemLabel());
                		formContainerVO.setItemValue(configEntityData.getItemValue());
						formContainerVO.setUrlCascadeSelect(configEntityData.getUrl());
						if("GenericParameter".equalsIgnoreCase(formContainerVO.getEntityName())){
							map.put("binderNameList", getGenericParameterList());
		                }else {
                		map.put("binderNameList", configEntityData.getWebDataBinderName().split(","));
		                }
                		if (formContainerVO.getFieldType().equals(FormComponentType.AUTOCOMPLETE)
                				&& StringUtils.isNotBlank(configEntityData.getCommaSeparatedColumns())) {
                			// Bind Column List
                			List<String> columnList = new ArrayList<String>(Arrays.asList(configEntityData
                					.getCommaSeparatedColumns().split(",")));
                			map.put("columnList", DynamicFormUtil.createBindToColumnList(columnList));
                			if(formContainerVO.getFieldType().equals(FormComponentType.AUTOCOMPLETE) && StringUtils.isNotBlank(configEntityData.getParentColumns()))
                            {
                            	map.put("parentColumnList", getParentColumnList(configEntityData.getParentColumns()));
                            }
                		}
                	}
                }

                if (formContainerVO.getEntityName() == null
                        && FormConfigurationConstant.CUSTOM_BINDER.equals(formContainerVO.getBinderName())) {
                    FormConfigEntityData configEntityData = formDefinitionService
                            .getFormConfigData(FormConfigurationConstant.CUSTOM_BINDER);
                    formContainerVO.setEntityName(configEntityData.getEntityName());
                    if("GenericParameter".equalsIgnoreCase(formContainerVO.getEntityName())){
                    	map.put("binderNameList", getGenericParameterList());
	                }else {
                    map.put("binderNameList", configEntityData.getWebDataBinderName().split(","));
	                }

                }

                if(FormComponentType.AUTOCOMPLETE.equals(formContainerVO.getFieldType()))
                {
                	Map<String,String> availableIdMap = new HashMap<>();
                	getAvailableIds(formVO.getContainerVOList(),availableIdMap);
                	map.put("parentIdList", availableIdMap);
                	
                }
                
                if (FormComponentType.PHONE.equals(formContainerVO.getFieldType())) {
                    map.put("phoneNumberTypeList", getNumberTypeList());
                }

				if (FormComponentType.EMAIL.equals(formContainerVO.getFieldType())) {
					map.put("emailTypeList", getEmailTypeList());
				}
				if (FormComponentType.LOV.equals(formContainerVO.getFieldType())) {
					map.put("lovKeyList", formDefinitionService.getLovKeyList());
				}
                if (FormComponentType.CUSTOM_CASCADED_SELECT.equals(formContainerVO.getFieldType())) {
                	getCustomCascadeSelectData(formVO,map,formContainerVO);
                }
                if(ValidatorUtils.isNull(formContainerVO.getMobile())){
                    formContainerVO.setMobile(false);
                  } 

                map.put("formContainerVO", formContainerVO);
                List<String> selectColumnValues = formContainerVO.getAutoCompleteColumnsHolder();
                if(notNull(selectColumnValues)){
                	map.put("selectedColumnValues", DynamicFormUtil.createBindToColumnList(selectColumnValues));
                }
                map.put("componentList", componentList);
                map.put("months", MONTHS);
                

            } else {
                map.put("formContainerVO", new FormContainerVO());
            }
            if(isFilter==true)
            {
            	viewMode="true";
            }
            if(StringUtils.isEmpty(viewMode)){
            	viewMode ="false";
            }
            map.put("viewable", viewMode);
        }
        return "componentDetailPage";
    }


    @RequestMapping(value = "/updateList")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    public String updateListComponent(@ModelAttribute("formContainerVO") FormContainerVO containerVO,
            @ModelAttribute("formVO") FormVO formVO, @ModelAttribute("componentList") List<FormComponentType> componentList,
            ModelMap map) {
        if (StringUtils.isNotBlank(containerVO.getComponentDisplayKey())) {
            boolean flagForAutoComplete = containerVO.getFieldType().equals(FormComponentType.AUTOCOMPLETE);
            if ((containerVO.getFieldType().equals(FormComponentType.DROP_DOWN)
                    || containerVO.getFieldType().equals(FormComponentType.RADIO)
                    || containerVO.getFieldType().equals(FormComponentType.CASCADED_SELECT)
                    || containerVO.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)
                    || (containerVO.getFieldType().equals(FormComponentType.MULTISELECTBOX)) || flagForAutoComplete)
                    && StringUtils.isNotBlank(containerVO.getEntityName())) {
                FormConfigEntityData configEntityData = formDefinitionService.getFormConfigData(containerVO.getEntityName());
                containerVO.setItemLabel(configEntityData.getItemLabel());
                containerVO.setItemValue(configEntityData.getItemValue());
                if (flagForAutoComplete && StringUtils.isNotBlank(configEntityData.getCommaSeparatedColumns())) {
                    // Bind Column List for autocomplete tag
                    List<String> columnList = new ArrayList<String>(Arrays.asList(configEntityData
                            .getCommaSeparatedColumns().split(",")));
                    map.put("columnList", DynamicFormUtil.createBindToColumnList(columnList));
                    Map<String,String> availableIdMap = new HashMap<>();
                    getAvailableIds(formVO.getContainerVOList(),availableIdMap);
                    map.put("parentIdList", availableIdMap);
                    
                } else if("GenericParameter".equalsIgnoreCase(containerVO.getEntityName())){
                	map.put("binderNameList", getGenericParameterList());
                } else {
                    map.put("binderNameList", configEntityData.getWebDataBinderName().split(","));
                }
                if(flagForAutoComplete && StringUtils.isNotBlank(configEntityData.getParentColumns()))
                {
                	map.put("parentColumnList", getParentColumnList(configEntityData.getParentColumns()));
                }
                containerVO.setBinderName(null);
            }
            if(containerVO.getFieldType().equals(FormComponentType.CASCADED_SELECT) || containerVO.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
            	 map.put("binderCascadeList", formDefinitionService.getCascadeEntityData());
            	 map.put("childBinderCascadeList", formDefinitionService.getChildCascadeEntityData(containerVO.getEntityName()));
            }
            map.put("binderList", formDefinitionService.getEntityNameList());
            map.put("dataTypeList", getSupportedDataTypes(containerVO.getFieldType(), containerVO.getBinderName()));
            map.put("formContainerVO", containerVO);
            map.put("componentList", componentList);
            if (FormComponentType.CUSTOM_CASCADED_SELECT.equals(containerVO.getFieldType())) {
            	getCustomCascadeSelectData(formVO,map,containerVO);
            }
			//Code to get product for dropDown and TextBox fields
			List<ProductSchemeMetaData> productSchemeList = formService.getProductSchemeMetaDataColumn(containerVO.getFieldType());

			map.put("productSchemeList", productSchemeList);
			List<ProductSchemeMetaData> productSchemeListForAssignmentMatrix = formService.getProductSchemeMetaDataColumnForAssignmentMatrix(containerVO.getFieldType());
			map.put("productSchemeListForAssignmentMatrix",productSchemeListForAssignmentMatrix);
        }
        return "componentDetailPage";
    }

    private Map<String,String> getParentColumnList(String parentColumns)
    {
    	Map<String,String> parentColumnList = new HashMap<>();
    	
    	for(String var : parentColumns.split(","))
    	{
    		parentColumnList.put(var,var.replace(".", " "));
    	}
    	
    	return parentColumnList;
    }
    

    @RequestMapping(value = "/updateBinderName")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    public String updateBinderNameintoComponent(@ModelAttribute("formContainerVO") FormContainerVO containerVO,
            @ModelAttribute("formVO") FormVO formVO, @ModelAttribute("componentList") List<FormComponentType> componentList,
            ModelMap map) {
        if (containerVO.getBinderName() != null && !containerVO.getBinderName().isEmpty()) {
            if (containerVO.getBinderName().equals(FormConfigurationConstant.CUSTOM_BINDER)) {
                List<FieldCustomOptionsVO> fieldCustomOptionsList = new ArrayList<FieldCustomOptionsVO>();
                FieldCustomOptionsVO customOptionsVO = new FieldCustomOptionsVO();
                fieldCustomOptionsList.add(customOptionsVO);
                containerVO.setFieldCustomOptionsList(fieldCustomOptionsList);
            } else {
                containerVO.setFieldCustomOptionsList(null);
            }
        }
        FormConfigEntityData configEntityData = formDefinitionService.getFormConfigData(containerVO.getEntityName());
        if("GenericParameter".equalsIgnoreCase(containerVO.getEntityName())){
        	map.put("binderNameList", getGenericParameterList());
        }else{
        map.put("binderNameList", configEntityData.getWebDataBinderName().split(","));
        }
        map.put("binderList", formDefinitionService.getEntityNameList());
        if(containerVO.getFieldType().equals(FormComponentType.CASCADED_SELECT) || containerVO.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
       	 map.put("childBinderCascadeList", formDefinitionService.getChildCascadeEntityData(containerVO.getEntityName()));
       	 map.put("binderCascadeList", formDefinitionService.getCascadeEntityData());
       }
        if(FormComponentType.AUTOCOMPLETE.equals(containerVO.getFieldType()) && StringUtils.isNotBlank(configEntityData.getParentColumns()))
        {
        	map.put("parentColumnList", getParentColumnList(configEntityData.getParentColumns()));
        }
        map.put("dataTypeList", getSupportedDataTypes(containerVO.getFieldType(), containerVO.getBinderName()));
        map.put("formContainerVO", containerVO);
        map.put("componentList", componentList);
        if (FormComponentType.CUSTOM_CASCADED_SELECT.equals(containerVO.getFieldType())) {
        	getCustomCascadeSelectData(formVO,map,containerVO);
        }
       //Code to get product for dropDown and TextBox fields
        List<ProductSchemeMetaData> productSchemeList = formService.getProductSchemeMetaDataColumn(containerVO.getFieldType());

        map.put("productSchemeList", productSchemeList);
		List<ProductSchemeMetaData> productSchemeListForAssignmentMatrix = formService.getProductSchemeMetaDataColumnForAssignmentMatrix(containerVO.getFieldType());
		map.put("productSchemeListForAssignmentMatrix",productSchemeListForAssignmentMatrix);

        return "componentDetailPage";
    }

    /**
     *  updating form name and description into session object
     * @param formVO
     * @param map
     * @return
     */
    @RequestMapping(value = "/updateFormDetail")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @ResponseBody
    public String updateFormVO(@ModelAttribute("formVO") FormVO formVO, ModelMap map) {
        if(notNull(formVO.getFormName()) 
                && notNull(formVO.getSourceProduct())){
        	String formStatus=validateFormNameAndSourceProduct(formVO);
			if ("false".equalsIgnoreCase(formStatus)
					|| DynamicFormNameStatus.DUPLICATE.toString().equalsIgnoreCase(formStatus)) {
				return "invalid";
			}
        	if(DynamicFormNameStatus.DELETED_MAPPED.toString().equalsIgnoreCase(formStatus) ){
        		return formStatus;
        	}
        }
        map.put("formVO", formVO);
        //updating layout in case of change in layout
        if(CollectionUtils.isNotEmpty(formVO.getContainerVOList())){
        	formVO.getContainerVOList().forEach((panel)->{
        		if(panel.getType() == FormContainerType.FIELD_TYPE_VIRTUAL){
        			panel.setPanelColumnLayout(formVO.getColumnLayout());
        		}
        	});
        }
        return "valid";
    }
    
	@RequestMapping(value = "/validateFormDetail/{formname}")
	@PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
	@ResponseBody
	public String validateFormVO(@ModelAttribute("formVO") FormVO formVO,
			@PathVariable("formname") String formname,@RequestParam("importFormSourceProductId") Long importFormSourceProductId, ModelMap map) {

		FormVO formnameVO = new FormVO();
		formnameVO.setFormName(formname);

		SourceProduct sourceProduct=null;

		if(notNull(importFormSourceProductId)){
			sourceProduct=genericParameterService.findById(importFormSourceProductId,SourceProduct.class);
		}

		if(notNull(sourceProduct)){
			formnameVO.setSourceProduct(sourceProduct);
		}


		if (notNull(formVO.getFormName()) && notNull(formVO.getSourceProduct())
				&& validateFormNameAndSource(formnameVO)) {

			return "invalid";
		}
		map.put("formVO", formVO);
		return "valid";
	}

	private boolean validateFormNameAndSource(FormVO formVO) {
		return formDefinitionService.isDuplicateForm(formVO);
	}
    
    private String validateFormNameAndSourceProduct(FormVO formVO) {
    	if(formVO.getIsFilter()!=null && formVO.getIsFilter()==true)
    	{
    		return Boolean.toString(formDefinitionService.validateFilterNameAndSourceProduct(formVO));
    	}
        return formDefinitionService.validateFormNameAndSourceProduct(formVO).toString();
    }

    /**
     * final save for all content of dynamic form
     * @param formVO
     * @return
     */
    @RequestMapping(value = "/saveAll")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    public String saveAll(@ModelAttribute("formVO") FormVO formVO,@ModelAttribute("isFilter") Boolean isFilter,@ModelAttribute("formVoMap") Map<Long, FormVO> formVoMap) {

		User user = getUserDetails().getUserReference();  
    	if(isFilter!=null && isFilter==true)
    	{	
    		ModelMetaData modelMetaData = formDefinitionService.saveModelMetaData(formVO,user);
            UIMetaData uiMetaData = formDefinitionService.saveUIMetaData(formVO, modelMetaData.getUri(),user);
            modelMetaData.setPersistenceStatus(PersistenceStatus.INACTIVE);
            uiMetaData.setPersistenceStatus(PersistenceStatus.INACTIVE);
            uiMetaData.setFormName(null);
    		DynamicFormFilter dynamicFormFilter=formVO.getDynamicFormFilter();
    		if(dynamicFormFilter==null)
    		{
    			dynamicFormFilter=new DynamicFormFilter();
    		}
    		
    		populateDynamicFormFilterData(dynamicFormFilter,formVO,uiMetaData,formVoMap);
    		makerCheckerService.saveAndSendForApproval(dynamicFormFilter, user);
    		return "redirect:/app/grid/DynamicFormFilter/DynamicFormFilter/loadColumnConfig";
    	}
    	else
    	{
            ModelMetaData modelMetaData = formDefinitionService.saveModelMetaData(formVO,user);
            UIMetaData uiMetaData = formDefinitionService.saveUIMetaData(formVO, modelMetaData.getUri(),user);
			FormConfigurationMapping formConfigurationMapping = formDefinitionService.saveFormConfigMapping(modelMetaData, uiMetaData, formVO,user);
			makerCheckerService.saveAndSendForApproval(formConfigurationMapping, user);
            return "redirect:/app/grid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";

    	}
    }
	@RequestMapping(value = "/save")
	@PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
	public String saveDynamicForm(@ModelAttribute("formVO") FormVO formVO,@ModelAttribute("ifComponentList") Map<String,FieldDefinition> componentList) {
		User user = getUserDetails().getUserReference();
		FormConfigurationMapping formConfigurationMapping = prepareDataForSave(formVO, user,componentList);
		makerCheckerService.masterEntityChangedByUser(formConfigurationMapping, user);
		return "redirect:/app/grid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";
	}

	@RequestMapping(value = "/saveAndSendForApproval")
	@PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
	public String saveAndSendForApproval(@ModelAttribute("formVO") FormVO formVO,@ModelAttribute("ifComponentList") Map<String,FieldDefinition> componentList) {
		User user = getUserDetails().getUserReference();
		FormConfigurationMapping formConfigurationMapping = prepareDataForSave(formVO, user,componentList);
		makerCheckerService.saveAndSendForApproval(formConfigurationMapping, user);
		return "redirect:/app/grid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";
	}

	private FormConfigurationMapping prepareDataForSave(FormVO formVO,User user,Map<String,FieldDefinition> componentList) {
		convertValidationVoIfsRightExpression(formVO,componentList);
		ModelMetaData modelMetaData = formDefinitionService.saveModelMetaData(formVO, user);
		UIMetaData uiMetaData = formDefinitionService.saveUIMetaData(formVO, modelMetaData.getUri(), user);
		return formDefinitionService.saveFormConfigMapping(modelMetaData, uiMetaData, formVO, user);
	}

    private void populateDynamicFormFilterData(DynamicFormFilter dynamicFormFilter, FormVO formVO,UIMetaData uiMetaData,Map<Long, FormVO> formVoMap) {
    	dynamicFormFilter.setName(formVO.getFormName());
    	dynamicFormFilter.setDescription(formVO.getFormDescription());
    	dynamicFormFilter.setIsShareable(formVO.getIsShareable());
    	dynamicFormFilter.setSourceProduct(formVO.getSourceProduct());
    	dynamicFormFilter.setUiMetaData(uiMetaData);
		dynamicFormFilter.setActiveFlag(formVO.isActiveFlag());
    	StringBuilder sourcesForms=new StringBuilder();
    	Map<String, JSONObject> formFieldMap=new HashMap<String, JSONObject>();
    	JSONObject filterFieldsJsonMap=new JSONObject();
    	try {
    		
	    	for(Map.Entry<Long, FormVO> entry:formVoMap.entrySet())
	    	{
	    			JSONObject jsonObject=new JSONObject();
	    			JSONArray fields=new JSONArray();
	    			jsonObject.put("uiMetaDataId", entry.getKey());
					jsonObject.put("fields",fields);
					formFieldMap.put(entry.getValue().getFormName(), jsonObject);
					filterFieldsJsonMap.put(entry.getValue().getFormName(),jsonObject);
	    	}
	    	List<FormContainerVO> formContainerVoList=formVO.getContainerVOList();
	    	if(formContainerVoList!=null)
	    	{
	    		for(FormContainerVO containerVO:formContainerVoList)
	    		{
	    			addAllFieldsFromForm(formFieldMap,containerVO);
	    		}
	    	}
	    	
    	
    	} catch (JSONException e) {
			BaseLoggers.exceptionLogger.error(e.getMessage(), e);
		}
    	
    	dynamicFormFilter.setFilterFieldsJsonMap(filterFieldsJsonMap.toString());
    	
		
	}


	private void addAllFieldsFromForm(Map<String, JSONObject> formFieldMap, FormContainerVO containerVO) throws JSONException {
		
		String fieldKey=containerVO.getFieldKey();
		if(fieldKey!=null)
		{
			
			String formName=containerVO.getSourceFormName();
			JSONObject jsonObject=formFieldMap.get(formName);
			JSONArray fields=null;
			if(jsonObject.has("fields"))
			{
				fields=(JSONArray)jsonObject.get("fields");
			}
			else
			{
				fields=new JSONArray();
			}
			fields.put(fieldKey);
			jsonObject.put("fields",fields);
		}
		if(containerVO.getFormContainerVOList()!=null)
		{
			for(FormContainerVO formContainerVO:containerVO.getFormContainerVOList())
			{
				addAllFieldsFromForm(formFieldMap,formContainerVO);
			}
		}
		
	}


	/**
     * validating form VO before saving
     * @param formVO
     * @return
     */
    @RequestMapping(value = "/validateBeforeSave")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG')")
    @ResponseBody
    public String validateFormVOBeforeSave(@ModelAttribute("formVO") FormVO formVO, @ModelAttribute("isFilter") Boolean isFilter, @RequestParam("cloned") Boolean cloned) {
		if (isFilter && formVO != null && formVO.getContainerVOList() != null) {
			for (FormContainerVO formContainerVO : formVO.getContainerVOList()) {
				if (formContainerVO.getFormContainerVOList() != null) {
					for (FormContainerVO formContainer : formContainerVO.getFormContainerVOList()) {
						if(formContainer.getSourceFormName() != null) {
							List<UIMetaData> forms = formService.getFormByName(formContainer.getSourceFormName());
							if (forms != null && forms.size() == 0) {
								return "Error: Dynamic Form Not Available: " + formContainer.getSourceFormName();
							}
						} else {
							return "Error: Component not available having field key: " + formContainer.getFieldKey();
						}
					}
				}
			}
		}
		if(cloned && notNull(formVO) && notNull(formVO.getFormName()) && notNull(formVO.getSourceProduct())){
			String formStatus = validateFormNameAndSourceProduct(formVO);
			if ("false".equalsIgnoreCase(formStatus) || DynamicFormNameStatus.DUPLICATE.toString().equalsIgnoreCase(formStatus)) {
				return "formNameInvalid";
			}
		}
		return validateFormVO(formVO).get("validationResposne");
	}

    /**
     * deleting componet or panel 
     * @param formVO
     * @param componentCode
     * @return
     */
    @RequestMapping(value = "/deleteComponent/{componentCode}")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @ResponseBody
    public String deleteComponent(@ModelAttribute("formVO") FormVO formVO, @PathVariable("componentCode") String componentCode) {
        if (componentCode != null && !componentCode.isEmpty()) {            
            DynamicFormUtil.deleteDedupeMapping(formVO);
        	DynamicFormUtil.updateComponentAtPanel(formVO.getContainerVOList(), null, DynamicFormUtil.getPanelComponentIndex(componentCode));
        }
        return "success";
    }
    
   
    @RequestMapping(value = "/deleteComponentDedupeCheck/{componentCode}")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @ResponseBody
    public String deleteComponentDedupeCheck(@ModelAttribute("formVO") FormVO formVO,
            @PathVariable("componentCode") String componentCode) {
        if (componentCode != null && !componentCode.isEmpty() && formVO!=null) {
            boolean response = DynamicFormUtil.checkIfDedupeConfiguredDeleted(formVO,
                    DynamicFormUtil.getPanelComponentIndex(componentCode));
            if (!response) {
                return "failure";
            }
        }
        return "success";
    }
    
   

    /**
     * 
     * Method to delete dynamic form
     * @param ids
     * @param map
     * @return
     */
    @RequestMapping(value = "/delete/{id}")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    public String deleteDynamicForm(@PathVariable("id") String ids, ModelMap map) {

        String[] idsToDelete = ids.split(",");
        Long id = null;

        for (int i = 0 ; i < idsToDelete.length ; i++) {
            id = Long.parseLong(idsToDelete[i]);
            if (null != id) {
                formDefinitionService.deleteDynamicForm(id);
            }
        }

        return "redirect:/app/grid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";
    }

    /**
     * 
     * Edit Mode
     * @param id
     * @param map
     * @return
     */
    @RequestMapping(value = "/edit/{id}")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    public String editDynamicFormFields(@PathVariable("id") Long id, ModelMap map) {
    	FormValidationConstants.initMappings();
		FormConfigurationMapping formConfigurationMapping = entityDao.find(FormConfigurationMapping.class, id);
		DynamicFormUtil.getDataForEditViewMode(formConfigurationMapping.getUiMetaData().getId(), map,false);
		FormVO formVO=(FormVO)map.get("formVO");
		formVO.setActiveFlag(formConfigurationMapping.isActiveFlag());
		formVO.setTaskId((Long) formConfigurationMapping.getViewProperties().get("taskId"));
		formVO.setId(formConfigurationMapping.getId());
        if(ApprovalStatus.CLONED == formConfigurationMapping.getApprovalStatus()) {
			map.put("cloned", true);
		} else {
			map.put("cloned", false);
		}
        map.put("edit", true);
        map.put("isFilter", false);
		map.put("masterID", masterID);
        map.put("formVoMap",new HashMap<Long, FormVO>());
        Map<Integer,String> colLayout = new HashMap<Integer, String>();
        updateDefaultColumnLayout(map, formVO, colLayout);
        return "dynamicMainPage";
    }

	private void updateDefaultColumnLayout(ModelMap map, FormVO formVO, Map<Integer, String> colLayout) {
		colLayout.put(2, messageSource.getMessage("label.dynamicForm.twocolumnlayout", null, configurationService.getSystemLocale()));
        colLayout.put(3, messageSource.getMessage("label.dynamicForm.threecolumnlayout", null, configurationService.getSystemLocale()));
        colLayout.put(4, messageSource.getMessage("label.dynamicForm.fourcolumnlayout", null, configurationService.getSystemLocale()));
        map.put("colLayout", colLayout);
        if(CollectionUtils.isNotEmpty(formVO.getContainerVOList())){
        	Optional<FormContainerVO> containerVO = formVO.getContainerVOList().stream().filter((p)->{if(p.getType() == FormContainerType.FIELD_TYPE_VIRTUAL) return true;
        							return false;}).findAny();
        	if(containerVO.isPresent()){
        		formVO.setColumnLayout(containerVO.get().getPanelColumnLayout());
        	}
        }
	}

    /**
     * 
     * View Mode
     * @param id
     * @param map
     * @return
     */

    @RequestMapping(value = "/view/{id}")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG')")
    public String viewDynamicFormFields(@PathVariable("id") Long id, ModelMap map) {
		UserInfo currentUser = getUserDetails();
    	FormValidationConstants.initMappings();
		FormConfigurationMapping formConfigurationMapping = baseMasterService.getMasterEntityWithActionsById(FormConfigurationMapping.class, id,currentUser.getUserEntityId().getUri());
    	DynamicFormUtil.getDataForEditViewMode(formConfigurationMapping.getUiMetaData().getId(), map,false);
		FormVO formVO=(FormVO)map.get("formVO");
		formVO.setActiveFlag(formConfigurationMapping.isActiveFlag());
		formVO.setTaskId((Long) formConfigurationMapping.getViewProperties().get("taskId"));
		formVO.setId(formConfigurationMapping.getId());
        map.put("viewable", true);
        map.put("isFilter", false);
		map.put("masterID", masterID);
        map.put("formVoMap",new HashMap<Long, FormVO>());
		if (formConfigurationMapping.getViewProperties() != null) {
			ArrayList<String> actions = (ArrayList<String>) formConfigurationMapping.getViewProperties().get("actions");
			if (actions != null) {
				for (String act : actions) {
					String actionString = "act" + act;
					map.put(actionString.replaceAll(" ", ""), false);
				}
			}
		}
		Map<Integer,String> colLayout = new HashMap<Integer, String>();
        updateDefaultColumnLayout(map, formVO, colLayout);
        return "dynamicMainPage";
    }

    /**
     * to download template for form
     * @param map
	 * @param formName
	 * @return
     */
    @RequestMapping(value = "/getTemplate/{formName}")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG') or hasAuthority('CHECKER_FORM_CONFIG') or hasAuthority('VIEW_FORM_CONFIG')")
    public ModelAndView getTemplateForFormConfiguration(@PathVariable("formName") String formName, ModelMap map) {
        UIMetaData uiMetaData = formConfigurationMappingService.getUiMetaDataTemplate(formName, null);
        // itializing meta data
        if (uiMetaData.getPanelDefinitionList() != null) {
            Hibernate.initialize(uiMetaData.getPanelDefinitionList());
            for (PanelDefinition panelDefinition : uiMetaData.getPanelDefinitionList()) {
                Hibernate.initialize(panelDefinition.getFieldDefinitionList());
            }
        }
        Map<String,Object> dataMap = new HashMap<String,Object>();

		dataMap.put("uiMetaData", uiMetaData);
		dataMap.put("formName", formName);
        
        return new ModelAndView("ExcelFormConfigurationTemplate","dataMap",dataMap);

    }

    @RequestMapping(value = "/moveComponent")
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @ResponseBody
    public String moveComponent(@RequestParam("componentId") String componentId, @RequestParam("direction") String direction,
            @RequestParam("layout") String layout, @ModelAttribute("formVO") FormVO formVO) {
        if (componentId != null && !componentId.isEmpty() && direction != null && !direction.isEmpty()) {
            List<Integer> indexList = DynamicFormUtil.getPanelComponentIndex(componentId);
            List<FormContainerVO> containerVOList = null;
            int sourceIndex = 0;
            if (indexList.size() == 1) {
                containerVOList = formVO.getContainerVOList();
                sourceIndex = indexList.get(0);
            } else if (indexList.size() == 2) {
                containerVOList = formVO.getContainerVOList().get(indexList.get(0)).getFormContainerVOList();
                sourceIndex = indexList.get(1);
            }
            if (layout.equals(ONE_COLUMN_LAYOUT)) {
                if (direction.equals(MOVEMENT_DOWN)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex + 1);
                } else if (direction.equals(MOVEMENT_UP)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex - 1);
                }
            } else if (layout.equals(TWO_COLUMN_LAYOUT)) {
                if (direction.equals(MOVEMENT_DOWN)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex + 2);
                } else if (direction.equals(MOVEMENT_LEFT)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex - 1);
                } else if (direction.equals(MOVEMENT_RIGHT)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex + 1);
                } else if (direction.equals(MOVEMENT_UP)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex - 2);
                }
            } else if (layout.equals(THREE_COLUMN_LAYOUT)) {
                if (direction.equals(MOVEMENT_DOWN)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex + 3);
                } else if (direction.equals(MOVEMENT_LEFT)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex - 1);
                } else if (direction.equals(MOVEMENT_RIGHT)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex + 1);
                } else if (direction.equals(MOVEMENT_UP)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex - 3);
                }
            }else if (layout.equals(FOUR_COLUMN_LAYOUT)) {
                if (direction.equals(MOVEMENT_DOWN)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex + 4);
                } else if (direction.equals(MOVEMENT_LEFT)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex - 1);
                } else if (direction.equals(MOVEMENT_RIGHT)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex + 1);
                } else if (direction.equals(MOVEMENT_UP)) {
                	DynamicFormUtil.swapFormContainerInList(containerVOList, sourceIndex, sourceIndex - 4);
                }
            }
        }
        return "success";
    }

   
	
	 @RequestMapping(value = "/addLogic", method = RequestMethod.POST)
	public @ResponseBody void addLogic(@RequestBody String json, ModelMap map)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		FormValidationMetadataVO formValidationMetadataVO = mapper.readValue(json, FormValidationMetadataVO.class);
		map.put("formValidationVO", formValidationMetadataVO);
	}
    
    @RequestMapping(value = "/getOperators", method = RequestMethod.POST)
    public @ResponseBody String getOperators(String componentCode, ModelMap map) throws JsonGenerationException, JsonMappingException, IOException {
    	ObjectMapper mapper = new ObjectMapper();
    	return mapper.writeValueAsString(FormValidationDataTypeOperatorMap.getOperatorsByDataType(componentCode));
    }
    
    @RequestMapping(value="/addNewRule", method = RequestMethod.POST)
    public String addNewRule(@ModelAttribute("formVO") FormVO formVo, ModelMap map,@ModelAttribute("ifComponentList") Map<String,FieldDefinition> components){
    	FormValidationMetadataVO validation = new FormValidationMetadataVO();
    	List<FormValidationMetadataVO> validations = formVo.getValidationsVO();
    	if(validations == null){
    		validations = new ArrayList<>();
    		formVo.setValidationsVO(validations);
    	}
    	validations.add(validation);
    	map.put("formVO", formVo);
    	map.put("validationsVO", validations);
    	map.put("ruleBlockIndex",validations.size()-1);
    	//map.put("componentList", components);
    	return "dynamicFormRuleSingleRuleBlockPage";
    }

    @RequestMapping(value="/deleteRuleBlock", method = RequestMethod.POST)
    public @ResponseBody String deleteRuleBlock(@RequestParam("index") Integer index,@ModelAttribute("formVO") FormVO formVo, ModelMap map){
    	if(formVo.getValidationsVO().size() >= index){
    		formVo.getValidationsVO().set(index, null);
    	}
    	return "success";
    }
    
    
    @RequestMapping(value="/addNewIfSection", method = RequestMethod.POST)
    public String addNewIfSection(@ModelAttribute("formVO") FormVO formVo, ModelMap map,@RequestParam("index") Integer index){
    	FormValidationRulesIFMetadataVO newIfSection = new FormValidationRulesIFMetadataVO();
    	List<FormValidationRulesIFMetadataVO> ifs = formVo.getValidationsVO().get(index).getIfConditions();
    	if(ifs == null){
    		ifs = new ArrayList<>();
    		formVo.getValidationsVO().get(index).setIfConditions(ifs);
    	}
    	for (FormValidationRulesIFMetadataVO formValidationRulesIFMetadataVO : ifs) {
			if(formValidationRulesIFMetadataVO !=null){
				map.put("includeAndOR", true);
				break;
			}
		}
    	ifs.add(newIfSection);
    	map.put("formVO", formVo);
    	map.put("validationsVO", formVo.getValidationsVO());
    	map.put("ruleBlockIndex",index);
    	map.put("ruleBlockifSectionIndex",ifs.size()-1);
    	return "dynamicFormRuleSingleIfBlockPage";
    }
    
    
    @RequestMapping(value="/deleteIfSection" ,method = RequestMethod.POST)
    public @ResponseBody String deleteifSectionBlock(@RequestParam("RuleBlockindex") Integer ruleBlockIndex,@RequestParam("ifSectionIndex") Integer ifBlock,
    		@ModelAttribute("formVO") FormVO formVo, ModelMap map){
    	if(formVo.getValidationsVO().size() >= ruleBlockIndex){
    		FormValidationMetadataVO validation = formVo.getValidationsVO().get(ruleBlockIndex);
    		if(validation.getIfConditions().size() >= ifBlock){
    			validation.getIfConditions().set(ifBlock, null);
    		}
    		int avaliableIfs = 0;
    		for(FormValidationRulesIFMetadataVO vo : validation.getIfConditions()){
    			if(vo !=null){
    				avaliableIfs++;
    			}
    			if(avaliableIfs > 1){
    				return "successAndDisable";
    			}
    		}
    	}
    	
    	return "successAndEnable";
    }
    
    
    @RequestMapping(value="/addNewThenSection", method = RequestMethod.POST)
    public String addNewThenSection(@ModelAttribute("formVO") FormVO formVo, ModelMap map,@RequestParam("index") Integer index){
    	FormValidationRulesThenMetadataVO newThenSection = new FormValidationRulesThenMetadataVO();
    	List<FormValidationRulesThenMetadataVO> thens = formVo.getValidationsVO().get(index).getThenActions();
    	if(thens == null){
    		thens = new ArrayList<>();
    		formVo.getValidationsVO().get(index).setThenActions(thens);
    	}
    	thens.add(newThenSection);
    	map.put("formVO", formVo);
    	map.put("validationsVO", formVo.getValidationsVO());
    	map.put("ruleBlockIndex",index);
    	map.put("ruleBlockThenSectionIndex",thens.size()-1);
    	return "dynamicFormRuleSingleThenBlockPage";
    }
    
    
    @RequestMapping(value="/deleteThenSection", method = RequestMethod.POST)
    public @ResponseBody String deleteThenSectionBlock(@RequestParam("RuleBlockindex") Integer ruleBlockIndex,@RequestParam("ifSectionIndex") Integer thenBlock,
    		@ModelAttribute("formVO") FormVO formVo, ModelMap map){
    	if(formVo.getValidationsVO().size() >= ruleBlockIndex){
    		FormValidationMetadataVO validation = formVo.getValidationsVO().get(ruleBlockIndex);
    		if(validation.getThenActions().size() >= thenBlock){
    			validation.getThenActions().set(thenBlock, null);
    		}
    	}
    	return "success";
    }
    
    @RequestMapping(value="/getOperatorsByFieldType", method = RequestMethod.POST)
    public String getOperatorAndFieldByFieldType(@RequestParam("selectedField") String selectedField,@ModelAttribute("ifComponentList") Map<String,FieldDefinition> componentList,
    		@ModelAttribute("formVO") FormVO formVo, @RequestParam("ruleBlockIndex") Long ruleBlockIndex, @RequestParam("ruleIfIndex") Long ruleIfIndex, ModelMap map){
    	FieldDefinition field = componentList.get(selectedField);
    	Map<String,String> operatorsString = new HashMap<>();
    	Map<String,String> fieldsString = new HashMap<>();
    	DynamicFormUtil.populateOperatorsAndFieldsByFieldType(componentList, field, operatorsString, fieldsString);
		map.put("field",field);

    	//populateRadioOptionsAsKeyValuePair(field,map);
		JSONSerializer serializer = new JSONSerializer();
		map.put("supportedOperatorsExpression",serializer.serialize(operatorsString));
		map.put("supportedFieldsExpression", serializer.serialize(fieldsString));
		map.put("ruleBlockIndex",ruleBlockIndex);
		map.put("ruleIfIndex",ruleIfIndex);
    	return "dynamicFormConstantField";
    }


    @RequestMapping(value="/getThenActionsByFieldType", method = RequestMethod.POST)
    public @ResponseBody String getThenActionsByFieldType(@RequestParam("selectedField") String selectedField,@ModelAttribute("ifComponentList") Map<String,FieldDefinition> componentList, 
    		@ModelAttribute("formVO") FormVO formVo, ModelMap map){
    	FieldDefinition field = componentList.get(selectedField);
    	Map<String,Map<String,String>> result = new HashMap<>();
    	Map<String,String> operatorsString = new HashMap<>();
    	Map<String,String> fieldsString = new HashMap<>();
    	DynamicFormUtil.populateOperatorsAndFieldListForThen(componentList, field, operatorsString, fieldsString);
    	result.put("supportedThenActionsExpression",operatorsString);
    	result.put("supportedThenFieldsExpression", fieldsString);
    	return new JSONSerializer().serialize(result);
    }
    
   
    @RequestMapping(value = "/saveAllRules",method = RequestMethod.POST)
    public @ResponseBody String SaveLogics(@ModelAttribute("formVO") FormVO formVo) {
    	List<FormValidationMetadataVO> validations = formVo.getValidationsVO();
    	String validationResults = DynamicFormUtil.validateValidationsRules(validations);
    	if(!DynamicFormUtil.isNullOREmpty(validationResults)){
    		return "Invalid : Reason : "+validationResults;
    	}
        return "valid";
    }
    
    
    
    @RequestMapping(value="/viewLogics",method = RequestMethod.POST)
    public String viewLogics(@ModelAttribute("formVO") FormVO formVo, ModelMap map
    		,@ModelAttribute("ifComponentList") Map<String,FieldDefinition> componentList,@RequestParam("viewLogics") String viewMode){
    	if(viewMode!=null && !viewMode.isEmpty()){
    		map.put("viewable",true);
    	}else{
    		map.put("viewable",false);
    	}
    	map.put("formVO", formVo);
    	DynamicFormUtil.populateValidationRelatedData(formVo, map, componentList);
    	return "dynamicFormRulePage";
    }

	@RequestMapping(value="/mapProductProcessor",method = RequestMethod.POST)
	public String mapProductProcessor(@ModelAttribute("formVO") FormVO formVo, ModelMap map
			,@RequestParam("viewLogics") String viewMode){
		if(viewMode!=null && !viewMode.isEmpty()){
			map.put("viewable",true);
		}else{
			map.put("viewable",false);
		}
		List<FormContainerVO> formContainerVOList = formVo.getContainerVOList();
		Map<String, String> fieldMap = null;
		if(CollectionUtils.isNotEmpty(formContainerVOList)) {
			fieldMap = getFieldData(formContainerVOList);
		}
		DynamicFormMapperVO dynamicFormMapperVO = new DynamicFormMapperVO();
		map.put("formVO", formVo);
		map.put("dynamicFormMapperVO", dynamicFormMapperVO);
		map.put("fieldMap", fieldMap);
		return "dynamicFormProcessorMapping";
	}
	
    @RequestMapping(value = "/mapDedupeConfiguration", method = RequestMethod.POST)
    public String mapDedupeConfiguration(@ModelAttribute("formVO") FormVO formVo, ModelMap map,
            @RequestParam("viewLogics") String viewMode) {
        if (viewMode != null && !viewMode.isEmpty()) {
            map.put("viewable", true);
        } else {
            map.put("viewable", false);
        }
        List<FormContainerVO> formContainerVOList = formVo.getContainerVOList();
        Map<String, String> fieldMap = null;
        if (CollectionUtils.isNotEmpty(formContainerVOList)) {
            fieldMap = getDedupeFieldData(formContainerVOList);
        }
        DedupeMapperVO dedupeMapperVO = formVo.getDedupeMapperVO();
        if (dedupeMapperVO == null) {
            dedupeMapperVO = new DedupeMapperVO();
        }
        map.put("formVO", formVo);
        map.put("dedupeMapperVO", dedupeMapperVO);
        map.put("dedupeFieldMap", fieldMap);
        return "dynamicFormDedupeMapping";
    }
	
	@RequestMapping(value="/fetchDataByProcessorId",method = RequestMethod.POST)
	public String fetchDataByProcessorId(@ModelAttribute("formVO") FormVO formVo,
										 @ModelAttribute("fieldMap") Map<String, String> fieldMap, ModelMap map,
										 @RequestParam("productProcessor") Long productProcessorId,
										 @RequestParam("viewMode") String viewMode){
		if(StringUtils.isNotEmpty(viewMode) && viewMode.equals("true")){
			map.put("viewable",true);
		}else{
			map.put("viewable",false);
		}
		DynamicFormMapperVO dynamicFormMapperVO = prepareDynamicFormVO(productProcessorId, formVo, fieldMap);
		map.put("dynamicFormMapperVO", dynamicFormMapperVO);
		if(dynamicFormMapperVO.getAllFieldsMap() != null && dynamicFormMapperVO.getAllFieldsMap().size() > 0) {
			map.put("message", "success");
		} else {
			map.put("message", "failure");
		}
		map.put("formVO", formVo);
		return "dynamicFormProcessorMapping";
	}

	@RequestMapping(value="/saveProcessorMapping",method = RequestMethod.POST)
	public @ResponseBody String saveProcessorMapping(DynamicFormMapperVO dynamicFormMapperVO,
									   @ModelAttribute("formVO") FormVO formVo, ModelMap map){

		List<DynamicFormMapperVO> dynamicFormMapperVOList = formVo.getDynamicFormMapperVOList();
		setCurrentTimeStampWithRelatedField(formVo,dynamicFormMapperVO);
		if(CollectionUtils.isNotEmpty(dynamicFormMapperVOList)) {
			Iterator<DynamicFormMapperVO> iterator = dynamicFormMapperVOList.iterator();
			while(iterator.hasNext()) {
				DynamicFormMapperVO tempFormMapper = iterator.next();
                if (notNull(dynamicFormMapperVO.getProductProcessor()) && tempFormMapper.getProductProcessor().getId().equals(dynamicFormMapperVO.getProductProcessor().getId())) {
                    iterator.remove();
                }
			}
			dynamicFormMapperVOList.add(dynamicFormMapperVO);
			formVo.setDynamicFormMapperVOList(dynamicFormMapperVOList);
		} else {
			dynamicFormMapperVOList = new ArrayList<>();
			dynamicFormMapperVOList.add(dynamicFormMapperVO);
			formVo.setDynamicFormMapperVOList(dynamicFormMapperVOList);
		}
		map.put("formVO", formVo);
		return "success";
	}
	
    @RequestMapping(value = "/saveDedupeMapping", method = RequestMethod.POST)
    public @ResponseBody String saveProcessorMapping(DedupeMapperVO dedupeMapperVO, @ModelAttribute("formVO") FormVO formVo,
            ModelMap map) {

        if (StringUtils.isEmpty(dedupeMapperVO.getPathField1()) ^ dedupeMapperVO.getScoreField1() == null) {
            return "mapBoth";
        }
        if (StringUtils.isEmpty(dedupeMapperVO.getPathField2()) ^ dedupeMapperVO.getScoreField2() == null) {
            return "mapBoth";
        }
        if (StringUtils.isEmpty(dedupeMapperVO.getPathField3()) ^ dedupeMapperVO.getScoreField3() == null) {
            return "mapBoth";
        }
        if (StringUtils.isEmpty(dedupeMapperVO.getPathField4()) ^ dedupeMapperVO.getScoreField4() == null) {
            return "mapBoth";
        }
        if (StringUtils.isEmpty(dedupeMapperVO.getPathField5()) ^ dedupeMapperVO.getScoreField5() == null) {
            return "mapBoth";
        }
        if (StringUtils.isEmpty(dedupeMapperVO.getPathField6()) ^ dedupeMapperVO.getScoreField6() == null) {
            return "mapBoth";
        }

        Map<String, String> duplicacyMap = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(dedupeMapperVO.getPathField1())
                && duplicacyMap.put(dedupeMapperVO.getPathField1(), dedupeMapperVO.getPathField1()) != null) {
            return "duplicate";
        }
        if (StringUtils.isNotEmpty(dedupeMapperVO.getPathField2())
                && duplicacyMap.put(dedupeMapperVO.getPathField2(), dedupeMapperVO.getPathField2()) != null) {
            return "duplicate";
        }
        if (StringUtils.isNotEmpty(dedupeMapperVO.getPathField3())
                && duplicacyMap.put(dedupeMapperVO.getPathField3(), dedupeMapperVO.getPathField3()) != null) {
            return "duplicate";
        }
        if (StringUtils.isNotEmpty(dedupeMapperVO.getPathField4())
                && duplicacyMap.put(dedupeMapperVO.getPathField4(), dedupeMapperVO.getPathField4()) != null) {
            return "duplicate";
        }
        if (StringUtils.isNotEmpty(dedupeMapperVO.getPathField5())
                && duplicacyMap.put(dedupeMapperVO.getPathField5(), dedupeMapperVO.getPathField5()) != null) {
            return "duplicate";
        }
        if (StringUtils.isNotEmpty(dedupeMapperVO.getPathField6())
                && duplicacyMap.put(dedupeMapperVO.getPathField6(), dedupeMapperVO.getPathField6()) != null) {
            return "duplicate";
        }

        Set<String> dedupeKeySet = new HashSet<String>();
        if (dedupeMapperVO != null) {
            DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField1(), dedupeKeySet);
            DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField2(), dedupeKeySet);
            DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField3(), dedupeKeySet);
            DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField4(), dedupeKeySet);
            DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField5(), dedupeKeySet);
            DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField6(), dedupeKeySet);
        }
        formVo.setDedupeKeySet(dedupeKeySet);
        formVo.setDedupeMapperVO(dedupeMapperVO);
        map.put("formVO", formVo);
        return "success";
    }
  
   
	private DynamicFormMapperVO prepareDynamicFormVO(Long productProcessorId, FormVO formVo, Map<String, String> fieldMap) {
		ProductProcessor productProcessor = genericParameterService.findById(productProcessorId, ProductProcessor.class);
		DynamicFormMapperVO dynamicFormMapperVO = new DynamicFormMapperVO();
		int flag = 0;

		if(CollectionUtils.isNotEmpty(formVo.getDynamicFormMapperVOList())) {
			for(DynamicFormMapperVO dynamicFormMapperVO1 : formVo.getDynamicFormMapperVOList()) {
				if(notNull(dynamicFormMapperVO1.getProductProcessor().getId()) && dynamicFormMapperVO1.getProductProcessor().getId().equals(productProcessorId)) {
                    dynamicFormMapperVO.setProductProcessor(productProcessor);
                    dynamicFormMapperVO.setAllFieldsMap(fieldMap);
                    dynamicFormMapperVO.setSelectedFieldsMap(prepareMapOfSelectedFields(dynamicFormMapperVO1.getSelectedFields()));
                    flag = 1;
                    break;
				}
			}
		}
		if(flag == 0){
			dynamicFormMapperVO.setProductProcessor(productProcessor);
			dynamicFormMapperVO.setAllFieldsMap(fieldMap);
			dynamicFormMapperVO.setSelectedFieldsMap(null);
		}
		return dynamicFormMapperVO;
	}

	private Map<String,String> prepareMapOfSelectedFields(String selectedFields) {
		Map<String,String> fieldMap = new HashMap<>();
		if(StringUtils.isNotEmpty(selectedFields)) {
            String[] fields = selectedFields.split(",", -1);
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i].trim();
                fieldMap.put(field, field);
            }
        }
		return fieldMap;
	}
		
    private Map<String, String> getDedupeFieldData(List<FormContainerVO> formContainerVOList) {
        Map<String, String> fieldMap = new HashMap<>();
        for (FormContainerVO formContainerVO : formContainerVOList) {
            if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_VIRTUAL
                    || formContainerVO.getType() == FormContainerType.FIELD_TYPE_PANEL) {
                for (FormContainerVO f : formContainerVO.getFormContainerVOList()) {    
                    if(StringUtils.isNotEmpty(formContainerVO.getFieldKey())){
                        String fieldKey=formContainerVO.getFieldKey()+"."+f.getFieldKey();
                        updateDedupeMapIfValid(f.getFieldType(), fieldKey,f.getFieldKey(), fieldMap);
                    }else{
                        updateDedupeMapIfValid(f.getFieldType(), f.getFieldKey(),f.getFieldKey(), fieldMap);
                    }                 
                }
            } else if ((formContainerVO.getType() == FormContainerType.FIELD_TYPE_TABLE)
                    || (formContainerVO.getType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE)) {
                continue;
            } else if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_FIELD) {
                updateDedupeMapIfValid(formContainerVO.getFieldType(), formContainerVO.getFieldKey(),formContainerVO.getFieldKey(), fieldMap);
            }
        }

        return fieldMap;
    }

    private boolean updateDedupeMapIfValid(String fieldType, String fieldKey,String fieldValue, Map<String, String> fieldMap) {
        boolean valid = (fieldType.equals(FormComponentType.TEXT_AREA)
               || fieldType.equals(FormComponentType.TEXT_BOX)
               || fieldType.equals(FormComponentType.AUTOCOMPLETE)
               || fieldType.equals(FormComponentType.CASCADED_SELECT)
               || fieldType.equals(FormComponentType.CUSTOM_CASCADED_SELECT)
               || fieldType.equals(FormComponentType.DROP_DOWN));
        if (valid) {
            fieldMap.put(fieldKey, fieldValue);
        }
        return valid;
    }

	private Map<String, String> getFieldData(List<FormContainerVO> formContainerVOList){
		Map<String, String> fieldMap = new HashMap<>();
		for(FormContainerVO formContainerVO : formContainerVOList) {
			if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_VIRTUAL || formContainerVO.getType() == FormContainerType.FIELD_TYPE_PANEL) {
				for (FormContainerVO f : formContainerVO.getFormContainerVOList()) {
					updateMapIfValid(f.isMandatoryField(), f.getFieldType(), f.getFieldKey(), fieldMap);
				}
			} else if ((formContainerVO.getType() == FormContainerType.FIELD_TYPE_TABLE) || (formContainerVO.getType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE)) {
				Optional<FormContainerVO> optional = formContainerVO.getFormContainerVOList().stream().
						filter(fieldType ->fieldType.isMandatoryField() && !fieldType.equals(FormComponentType.CASCADED_SELECT) && !fieldType.equals(FormComponentType.CUSTOM_CASCADED_SELECT) && !fieldType.equals(FormComponentType.AUTOCOMPLETE) && !fieldType.equals(FormComponentType.CURRENT_TIME_STAMP)).
						findFirst();
				if(!optional.isPresent()){
					fieldMap.put(formContainerVO.getFieldKey(), formContainerVO.getFieldKey());
				}
			} else if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_FIELD) {
				updateMapIfValid(formContainerVO.isMandatoryField(), formContainerVO.getFieldType(), formContainerVO.getFieldKey(), fieldMap);
			}
		}

		return fieldMap;
	}

	private boolean updateMapIfValid(boolean mandatory, String fieldType, String fieldKey, Map<String, String> fieldMap){
		boolean valid =  (!mandatory  && !fieldType.equals(FormComponentType.CASCADED_SELECT) && !fieldType.equals(FormComponentType.CUSTOM_CASCADED_SELECT) && !fieldType.equals(FormComponentType.AUTOCOMPLETE) && !fieldType.equals(FormComponentType.CURRENT_TIME_STAMP));
		if(valid) {
			fieldMap.put(fieldKey, fieldKey);
		}
		return valid;
	}

	/**
     * 
     * get supported data types list
     * @param suuportedDataTypes
     * @return
     */

    private List<FieldDataType> deleteUnSupportedDataTypes(int[] suuportedDataTypes) {

        List<FieldDataType> fieldDataTypesList = formDefinitionService.getDynamicFormFieldDataType();
        List<FieldDataType> supportedFieldDataTypesList = new ArrayList<FieldDataType>();

        for (FieldDataType fieldDataType : fieldDataTypesList) {
            for (int i = 0 ; i < suuportedDataTypes.length ; i++) {
                if (suuportedDataTypes[i] == Integer.parseInt(fieldDataType.getCode())) {
                    supportedFieldDataTypesList.add(fieldDataType);
                }
            }
        }

        return supportedFieldDataTypesList;

    }

    
    private Map<String, String>  validateFormVO(FormVO formVO) {
    	Map<String, String> validationMessagesMap = new HashMap<>();
    	validationMessagesMap.put("validationResposne", "valid");
        if (formVO == null || ValidatorUtils.hasNoElements(formVO.getContainerVOList()) || hasOnlyVirtualPanelWithNoComponents(formVO) ) {
            validationMessagesMap.put("validationResposne", "emptyContainerVoList");
        } else {
            if (ValidatorUtils.hasElements(formVO.getContainerVOList())) {
                validateFormContainer(formVO, validationMessagesMap);
            }
        }
        return validationMessagesMap;
    }

    private void validateFormContainer(FormVO formVO, Map<String, String> validationMessagesMap) {
    	for (FormContainerVO formContainerVO : formVO.getContainerVOList()) {                	
        	if (isPanelOrTableContainer(formContainerVO.getFieldType())
                    || formContainerVO.getType() == FormContainerType.FIELD_TYPE_VIRTUAL
                    || formContainerVO.getType() == FormContainerType.FIELD_TYPE_FIELD) {
            	if (isPanelOrTableContainer(formContainerVO.getFieldType()) && 
            			(formContainerVO.getFieldKey() == null || formContainerVO.getFieldKey().isEmpty())) {
            		validationMessagesMap.put("validationResposne", "invalid");
            		break;
            	}
                validateInnerFormContainers(formContainerVO, validationMessagesMap);
            } else {														// invalid field type
            	validationMessagesMap.put("validationResposne", "invalid");
            }                	
        }
	}

	private boolean isPanelOrTableContainer(String fieldType) {
		return fieldType != null && (fieldType.equals(FormComponentType.PANEL) || fieldType.equals(FormComponentType.TABLE) || fieldType.equals(FormComponentType.SPECIAL_TABLE));
	}

	private void validateInnerFormContainers(FormContainerVO formContainerVO, Map<String, String> validationMessagesMap) {
    	List<FormContainerVO> innerFormContainerVOList = formContainerVO.getFormContainerVOList();
    	// it is a panel or table
        if (innerFormContainerVOList != null
                && !innerFormContainerVOList.isEmpty()) {
            for (FormContainerVO innerFormContainerVO : innerFormContainerVOList) {

				if ( (innerFormContainerVO.getFieldKey() == null || innerFormContainerVO.getFieldKey().isEmpty())){

					validationMessagesMap.put("validationResposne", "invalid");

				}else if(!FormComponentType.CURRENT_TIME_STAMP.equalsIgnoreCase(innerFormContainerVO.getFieldType()) && innerFormContainerVO.getFieldDataType() == null){

						validationMessagesMap.put("validationResposne", "invalid");

                }
            }
        } else if (formContainerVO.getFieldDataType() == null) {	// it is a single field
            	validationMessagesMap.put("validationResposne", "invalid");
        }
	}

	/**
     * Gets the number type list.
     *
     * @return the number type list
     */
    private List<PhoneNumberTypeVO> getNumberTypeList() {
        List<PhoneNumberTypeVO> numberTypeVOList = new ArrayList<PhoneNumberTypeVO>();

        PhoneNumberTypeVO numberTypeVO = null;
        List<PhoneNumberType> numberTypeList = genericParameterService.retrieveTypes(PhoneNumberType.class);
        if (CollectionUtils.isNotEmpty(numberTypeList)) {
            for (PhoneNumberType numberType : numberTypeList) {
                numberTypeVO = new PhoneNumberTypeVO();
                numberTypeVO.setCode(numberType.getCode());
                numberTypeVO.setName(numberType.getName());
                numberTypeVO.setDescription(numberType.getDescription());
                numberTypeVOList.add(numberTypeVO);
            }
        }

        return numberTypeVOList;
    }

    /**
     * Gets the email type list.
     *
     * @return the email type list
     */
    private List<EmailTypeVO> getEmailTypeList() {
        List<EmailTypeVO> emailTypeVOList = new ArrayList<EmailTypeVO>();
        List<EMailType> emailTypeList = null;

        EmailTypeVO emailTypeVO = null;
        emailTypeList = genericParameterService.retrieveTypes(EMailType.class);
        if (CollectionUtils.isNotEmpty(emailTypeList)) {
            for (EMailType eMailType : emailTypeList) {
                emailTypeVO = new EmailTypeVO();
                emailTypeVO.setCode(eMailType.getCode());
                emailTypeVO.setName(eMailType.getName());
                emailTypeVO.setDescription(eMailType.getDescription());
                emailTypeVOList.add(emailTypeVO);
            }
        }

        return emailTypeVOList;
    }
	
    
    private List<FieldDataType> getSupportedDataTypes(String componentCode, String binderName) {

        if (componentCode.equals(FormComponentType.DROP_DOWN) || componentCode.equals(FormComponentType.RADIO)
                || componentCode.equals(FormComponentType.MULTISELECTBOX)
                || componentCode.equals(FormComponentType.AUTOCOMPLETE)
                || componentCode.equals(FormComponentType.CASCADED_SELECT)
                || componentCode.equals(FormComponentType.CUSTOM_CASCADED_SELECT))
            {

            if (null != binderName && binderName.equals(FormConfigurationConstant.CUSTOM_BINDER)) {
                return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_INTEGER,
                        FieldDataType.DATA_TYPE_NUMBER, FieldDataType.DATA_TYPE_TEXT });

            } else {
                return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_TEXT_REFERENCE });
            }

        } else if (componentCode.equals(FormComponentType.PHONE)) {
            return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_PHONE });

        } else if (componentCode.equals(FormComponentType.EMAIL)) {
            return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_EMAIL });

        } else if (componentCode.equals(FormComponentType.CHECKBOX)) {
            return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_TEXT_BOOLEAN });

        } else if (componentCode.equals(FormComponentType.DATE)) {
            return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_DATE });

        } else if (componentCode.equals(FormComponentType.MONEY)) {
            return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_MONEY });

        } else if (componentCode.equals(FormComponentType.TEXT_BOX)) {
            return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_INTEGER, FieldDataType.DATA_TYPE_NUMBER,
                    FieldDataType.DATA_TYPE_TEXT });

        } else if (componentCode.equals(FormComponentType.TEXT_AREA)) {
            return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_TEXT });
            
        } else if (componentCode.equals(FormComponentType.BUTTON)) {
			return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_TEXT });
		
		} else if (componentCode.equals(FormComponentType.HYPERLINK)) {
			return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_TEXT });
		
		} else if (componentCode.equals(FormComponentType.LOV)) {
			return deleteUnSupportedDataTypes(new int[] { FieldDataType.DATA_TYPE_LOV });

		}

        return Collections.emptyList();
    }
    
    private boolean hasOnlyVirtualPanelWithNoComponents(FormVO formVo )
    {
    	return formVo.getContainerVOList().size() == 1 && formVo.getContainerVOList().get(0).getType() == FormContainerType.FIELD_TYPE_VIRTUAL &&  ValidatorUtils.hasNoElements(formVo.getContainerVOList().get(0).getFormContainerVOList());  
    	
    }

    @RequestMapping(value="/getSpecialColumn",method = RequestMethod.POST)
    public String getSpecialColumByKey(@RequestParam("specialKey") String specialKey,ModelMap map,@ModelAttribute FormContainerVO formContainerVO){
    	if(StringUtils.isNotBlank(specialKey)){
			SpecialTable specialTable = formService.getSpecialTable(specialKey);
			String primaryColumn = specialTable!=null?specialTable.getPrimaryValue():null;
			try {
                List<SpecialColumnVo> specialColumnVos = getSpecialColumnVos(specialTable,primaryColumn);
				map.put("specialColumns",specialColumnVos);

				map.put("primaryColumn",primaryColumn);
                map.put("description",specialTable!=null?specialTable.getDescription():null);
				map.put("formContainerVO",formContainerVO);
			} catch (SQLException e) {
                BaseLoggers.flowLogger.info(e.getMessage());
			}
		}
    	return "specialColumn";
	}

    private List<SpecialColumnVo> getSpecialColumnVos(SpecialTable specialTable, String primaryColumn) throws SQLException {
        long length = specialTable.getSelectClause().length();
        String str = specialTable.getSelectClause().getSubString(1,(int)length);
		String[] arr = str.split(",");
		List<SpecialColumnVo> list = new ArrayList<>();
		for(String a : arr){
			SpecialColumnVo specialColumnVo = new SpecialColumnVo();
			int indexOfAs = a.indexOf(" as ");
			String value = a.substring(indexOfAs+4);
			if(!value.equalsIgnoreCase(primaryColumn)){
				specialColumnVo.setValue(value);
				list.add(specialColumnVo);
			}
		}
        return list;
    }
    
	/**
	 *
	 * Gets Screen Id from Screen Code.
	 * 
	 * @param masterId
	 * @return
	 */
	@RequestMapping(value = "/getMasterDynamicFormData/{masterId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getDynamicFormScreenIdForScreenCode(
			@PathVariable("masterId") String masterId, @RequestParam("viewOrEditMode") boolean viewOrEditMode) {

		String screenCode = masterId + MASTER_DYNAMIC_FORM_CONS;

		BaseLoggers.flowLogger.info("Getting Dynamic Form Screen Id from Master Id: " + masterId);
		BaseLoggers.flowLogger.info("Screen Code: " + screenCode);
		Long screenId = formService.getScreenIdbyScreenCode(screenCode);
		Map<String, Object> map = new HashMap<>();
		map.put("screenId", screenId);
		map.put("screenCode", screenCode);
		if (screenId != null && viewOrEditMode) {
			BaseLoggers.flowLogger.info("Getting Entity Name for the Given Master ID.");
			BaseLoggers.flowLogger.info("Master ID: " + masterId);
			map.put("entityName", masterConfigurationRegistry.getEntityClass(masterId));
		}
		return map;
	}
	
	private List<String> getGenericParameterList(){
		List<String>  genericParameterTypes= genericParameterService.findAllGenericParameterTypes();
    	List<String> genericParameterTypesList = new ArrayList<String>();
    	for (String genericParameterType:genericParameterTypes){
    		try {
    			if(DynamicDtypeClass.equalsIgnoreCase(genericParameterType)){
    				List<String> dynamicgenericTypes = genericParameterService.findAllDynamicGenericParameter();
    				for(String dynamicgenericType:dynamicgenericTypes){
    					genericParameterTypesList.add(dynamicgenericType);
    				}
    			}
				Class genericParameterEntityClass = Class.forName(genericParameterType);
				genericParameterTypesList.add(genericParameterEntityClass.getSimpleName());
			} catch (ClassNotFoundException e) {
				BaseLoggers.flowLogger.info("Error While Dynamic Generic Parameter: " + e);
			}
    		
    	}
    	return genericParameterTypesList;
	}

	private void setCurrentTimeStampWithRelatedField(FormVO formVo,DynamicFormMapperVO dynamicFormMapperVO){
		List<FormContainerVO> formContainerVOS=formVo.getContainerVOList();
		String selectedFields=dynamicFormMapperVO.getSelectedFields();
		if(dynamicFormMapperVO!=null && selectedFields!=null) {
			String[] selectedFieldsArray = selectedFields.split(",");
			List<String> selectedFieldsList = Arrays.asList(selectedFieldsArray);
			List<String> fieldsKeyList = new ArrayList<>(selectedFieldsList);
			List<FormContainerVO> allCurrentTimeStampFields = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(formContainerVOS)) {
				for (FormContainerVO formContainer : formContainerVOS) {
					List<FormContainerVO> formFieldsKeyList = formContainer.getFormContainerVOList().stream().filter(value -> 	       		FormComponentType.CURRENT_TIME_STAMP.equalsIgnoreCase(value.getFieldType())).collect(Collectors.toList());
					allCurrentTimeStampFields.addAll(formFieldsKeyList);
				}
				for (FormContainerVO formContainerVO : allCurrentTimeStampFields) {
					if (formContainerVO.getAssociatedFieldKey() != null && fieldsKeyList.contains(formContainerVO.getAssociatedFieldKey())) {
                        selectedFields=selectedFields.concat(",").concat(formContainerVO.getFieldKey());
						dynamicFormMapperVO.setSelectedFields(selectedFields);
					}
				}
			}
		}
	}
	public void getCustomCascadeSelectData(FormVO formVO,ModelMap map,FormContainerVO formContainerVO){
    	List<String> parentBinder = new ArrayList<>();
    	FormContainerVO ParentformContainerVO=null;
    	if(null != formVO.getContainerVOList() && !formVO.getContainerVOList().isEmpty()){
	    	for(int i=0;i<formVO.getContainerVOList().size();i++){
	    		if(null != formVO.getContainerVOList().get(i).getFormContainerVOList() && !formVO.getContainerVOList().get(i).getFormContainerVOList().isEmpty()){
	    			for(int j=0;j<formVO.getContainerVOList().get(i).getFormContainerVOList().size();j++){
	    				FormContainerVO  formContainerVO2 =formVO.getContainerVOList().get(i).getFormContainerVOList().get(j); 
	    				if(null != formContainerVO.getParent() && formContainerVO.getParent().equalsIgnoreCase(formContainerVO2.getFieldKey())){
	    					ParentformContainerVO = formContainerVO2;
	    					if(null !=formContainerVO.getFirstParent() && formContainerVO.getFirstParent() && null == formContainerVO2.getParent() && !FormComponentType.CUSTOM_CASCADED_SELECT.equalsIgnoreCase(formContainerVO2.getFieldType())){
	    						formContainerVO2.setParent("externalParent");
	    					}
	    				}
	    				if((null != formContainerVO.getParentFieldKey() && !"".equalsIgnoreCase(formContainerVO.getParentFieldKey())) && FormComponentType.CUSTOM_CASCADED_SELECT.equalsIgnoreCase(formContainerVO.getFieldType()) && formContainerVO.getParentFieldKey().equalsIgnoreCase(formContainerVO2.getFieldKey())){
	    					parentBinder.add(formContainerVO2.getFieldKey());
	            		}else if((null == formContainerVO.getParentFieldKey() || "".equalsIgnoreCase(formContainerVO.getParentFieldKey())) && FormComponentType.DROP_DOWN.equals(formContainerVO2.getFieldType()) && !FormConfigurationConstant.CUSTOM_BINDER.equalsIgnoreCase(formContainerVO2.getBinderName())){
	                		parentBinder.add(formContainerVO2.getFieldKey());
	                	}
	    			}
	    			
	    		}
	    	}
    	}
        map.put("cascadeParentBinder", parentBinder);
        List<FormConfigEntityData>  formConfigEntityData=formDefinitionService.getEntityNameList();
        List<FormConfigEntityData>  parentConfigEntityData=new ArrayList<>();
        List<FormConfigEntityData>  childConfigEntityData=new ArrayList<>();
        for(int i=0;i<formConfigEntityData.size();i++){
        	if(!"Custom Binder".equalsIgnoreCase(formConfigEntityData.get(i).getEntityName())){
        		parentConfigEntityData.add(formConfigEntityData.get(i));
            	childConfigEntityData.add(formConfigEntityData.get(i));
        	}
        	
        }
        map.put("customCascadeBinderList", parentConfigEntityData);
        map.put("childcustomCascadeBinderList", childConfigEntityData);
        if(formContainerVO.getParentFieldKey() != null){
			map.put("binderChildEntityList", Arrays.asList(formDefinitionService.getFormConfigData(formContainerVO.getEntityName(),formContainerVO.getParentId())));
		}
        if(null!= formContainerVO.getParent() && !"".equalsIgnoreCase(formContainerVO.getParent()) && null!= formContainerVO.getBinderName() && !"".equalsIgnoreCase(formContainerVO.getBinderName())){
    		map.put("parentItemLabel", ParentformContainerVO.getItemLabel());
    		map.put("parentItemValue", ParentformContainerVO.getItemValue());
    		map.put("parentBinder", ParentformContainerVO.getBinderName());
   	
    		map.put("childItemLabel", formContainerVO.getItemLabel());
    		map.put("childItemValue", formContainerVO.getItemValue());
    		map.put("childBinder", formContainerVO.getBinderName());
	    	if(null != formContainerVO.getParentChildForms() && !formContainerVO.getParentChildForms().isEmpty()){
	        	for(int i=0;i<formContainerVO.getParentChildForms().size();i++){
	        		ParentChildForm parentChildForm =  formContainerVO.getParentChildForms().get(i);
	        		if(null != parentChildForm.getParentIds() && parentChildForm.getParentIds().length >0 && null != parentChildForm.getChildIds() && parentChildForm.getChildIds().length >0){
	        			formContainerVO.getParentChildForms().get(i).setSelectdparentIds(DynamicFormUtil.createBindToColumnList(parentChildForm.getParentIds(),ParentformContainerVO.getItemLabel(),ParentformContainerVO.getItemValue()));
	        			formContainerVO.getParentChildForms().get(i).setSelectdchildIds(DynamicFormUtil.createBindToColumnList(parentChildForm.getChildIds(),formContainerVO.getItemLabel(),formContainerVO.getItemValue()));
	        		}else{
	        			formContainerVO.getParentChildForms().remove(i);
	        		}
	        		
	        	}
	        }
        }
    
    }
	@RequestMapping(value="/getChildRow/{index}",method = RequestMethod.GET)
    public String getChildRow(@PathVariable("index") int index,ModelMap map,@ModelAttribute("formContainerVO") FormContainerVO containerVO,
            @ModelAttribute("formVO") FormVO formVO,@ModelAttribute("componentList") List<FormComponentType> componentList){
    	getCustomCascadeSelectData(formVO,map,containerVO);
    	int index1  = index+1;
    	map.put("index", index1);
    	map.put("parentChildForms["+index1+"]", new ParentChildForm());
    	map.put("viewable", false);
    	return "addNewChildMapping";
    }

	@RequestMapping(value = "/populateAssignmentMatrix")
	@ResponseBody
	public AutocompleteVO populateAssignmentMatrixForDynamicForm(ModelMap map, @RequestParam String value, @RequestParam String itemVal,
																	 @RequestParam String searchCol, @RequestParam String className, @RequestParam Boolean loadApprovedEntityFlag,
																	 @RequestParam String i_label, @RequestParam String idCurr, @RequestParam String content_id,
																	 @RequestParam int page, @RequestParam(required = false) String itemsList,
																	 @RequestParam(required = false) Boolean strictSearchOnitemsList, HttpServletRequest req) {

		String[] searchColumnList = searchCol.split(" ");
		AutocompleteVO autocompleteVO = new AutocompleteVO();
		List<Map<String, ?>> list = new ArrayList<>();
		if(assignmentMatrixPopulation!=null) {
			list = assignmentMatrixPopulation.searchOnAssignmetMaster(itemVal, searchColumnList, value);

			if (list.size() > 0) {
				// map.put("size", list.size());
				//map.put("page", page);
				autocompleteVO.setS(list.size());
				autocompleteVO.setP(page);
				// if remainder is 1 when size of list is divided by 3
				if (list.size() / 3 == page && list.size() % 3 == 1)
					list = list.subList(3 * page, 3 * page + 1);

					// if remainder is 2 when size of list is divided by 3
				else if (list.size() / 3 == page && list.size() % 3 == 2)
					list = list.subList(3 * page, 3 * page + 2);

				else
					list = list.subList(3 * page, 3 * page + 3);
			}


			if (i_label != null && i_label.contains(".")) {
				i_label = i_label.replace(".", "");
			}
			int i;
			String[] sclHeading = new String[searchColumnList.length];
			for (i = 0; i < searchColumnList.length; i++) {
				searchColumnList[i] = searchColumnList[i].replace(".", "");
				sclHeading[i] = messageSource.getMessage("label.autoComplete." + searchColumnList[i], null, Locale.getDefault());
			}
			autocompleteVO.setColh(sclHeading);
		}
		autocompleteVO.setD(list);
		autocompleteVO.setIc(idCurr);
		autocompleteVO.setIl(i_label);
		autocompleteVO.setCi(content_id);
		autocompleteVO.setIv(itemVal);
		autocompleteVO.setScl(searchColumnList);

		return  autocompleteVO;
	}

	@RequestMapping(value="/getUserPreferenceTime",method = RequestMethod.GET)
	@ResponseBody
	public String getUserPreferenceTime(){
		return dynamicFormValidationService.getUserPreferenceTime();
	}



	private void convertValidationVoIfsRightExpression(FormVO formVO, Map<String, FieldDefinition> componentList) {
		if (org.apache.commons.collections.CollectionUtils.isNotEmpty(formVO.getValidationsVO())) {
			for (FormValidationMetadataVO formValidationMetadataVO : formVO.getValidationsVO()) {
				if (formValidationMetadataVO != null && org.apache.commons.collections.CollectionUtils
						.isNotEmpty(formValidationMetadataVO.getIfConditions())) {
					for (FormValidationRulesIFMetadataVO ifMetadataVO : formValidationMetadataVO.getIfConditions()) {
						if (ifMetadataVO != null && ifMetadataVO.getLeftOperandFieldKey() != null) {
							FieldDefinition fieldDefinition = componentList
									.get(ifMetadataVO.getLeftOperandFieldKey().getExpression());
							if (fieldDefinition != null && ifMetadataVO.getRightOperandFieldKey() != null
									&& ifMetadataVO.getRightOperandFieldKey().getExpression() != null) {
								ifMetadataVO.getRightOperandFieldKey()
										.setExpression(convertIfRightExpression(fieldDefinition.getFieldType(),
												ifMetadataVO.getRightOperandFieldKey().getExpression()));
							}
						}
					}

				}
			}
		}
	}

	private String convertIfRightExpression(String fieldType,String rightExpression)  {
		String convertedString = rightExpression;
		if(fieldType.equals(FormComponentType.DATE)){
			try {
				DateTime dateTime = userService.parseDateTime(rightExpression);
				convertedString = String.valueOf(dateTime.getMillis());
			} catch (ParseException e) {
				BaseLoggers.flowLogger.error("Exception", e);
			}
		}



		return convertedString;
	}

}
