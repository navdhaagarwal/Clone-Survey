package com.nucleus.web.dynamicForm.datasharing;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.core.dynamicform.service.DynamicFormUtil;
import com.nucleus.core.dynamicform.service.FormDefinitionService;
import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.formDefinition.FormDefinitionUtility;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.core.formsConfiguration.FormComponentType;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.FormContainerVO;
import com.nucleus.core.formsConfiguration.FormVO;
import com.nucleus.core.formsConfiguration.PanelDefinition;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.formsConfiguration.UIMetaDataVo;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.formDefinition.DynamicFormController;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.entity.ApprovalStatus;

@Transactional
@Controller
@RequestMapping(value = "/DynamicFormFilter")
@SessionAttributes({ "formVO", "componentList", "formVoMap","isFilter","formValidationVO","ifComponentList","whenList"})
public class DynamicFormFilterController extends DynamicFormController{



	private static final String  masterId   = "DynamicFormFilter";



	@Inject
	@Named("formConfigService")
	private FormService               formService;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;

	@Inject
	@Named("formDefinitionUtility")
	protected FormDefinitionUtility formDefinitionUtility;

	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService        makerCheckerService;

	@Inject
	@Named("formDefinitionService")
	private FormDefinitionService      formDefinitionService;

	@Inject
	@Named("entityDao")
	private EntityDao                  entityDao;


	@PreAuthorize("hasAuthority('MAKER_DYNAMICFORMFILTER')")
	@RequestMapping(value = "/create")
	public String createFilter(ModelMap map) {

		FormValidationConstants.initMappings();
		map.put("dynamicFormFilterVO", new DynamicFormFilterVO());
		SourceProduct sourceProduct=null;
		FormVO formVO = new FormVO();
		if(notNull(ProductInformationLoader.getProductCode())){
			sourceProduct=genericParameterService.findByCode(ProductInformationLoader.getProductCode(), SourceProduct.class);
		}
		if(notNull(sourceProduct) && notNull(sourceProduct.getId())){
			formVO.setSourceProduct(sourceProduct);
			formVO.setSourceProductId(sourceProduct.getId());

		}
		map.put("masterID", masterId);
		formVO.setFormuuid(UUID.randomUUID().toString());
		formVO.setIsFilter(true);
		formVO.setDynamicFormFilter(new DynamicFormFilter());
		formVO.setColumnLayout(2);
		map.put("formVO", formVO);
		map.put("formVoMap",new HashMap<Long, FormVO>());
		map.put("isFilter", true);
		return "dynamicFormFilter";
	}

	private Collection<? extends UIMetaData> getNotNull(Set<UIMetaData> allDynamicFormConfigTypeSet) {
		if(allDynamicFormConfigTypeSet!=null) {
			return allDynamicFormConfigTypeSet;
		}
		return new HashSet<>();
	}

	@RequestMapping(value = "/saveFilter")
	@PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
	public String saveDynamicFormFilter(@ModelAttribute("formVO") FormVO formVO,@ModelAttribute("isFilter") Boolean isFilter,@ModelAttribute("formVoMap") Map<Long, FormVO> formVoMap) {

		User user = getUserDetails().getUserReference();
		if (isFilter != null && isFilter) {
			ModelMetaData modelMetaData = formDefinitionService.saveModelMetaData(formVO, user);
			UIMetaData uiMetaData = formDefinitionService.saveUIMetaData(formVO, modelMetaData.getUri(), user);
			modelMetaData.setPersistenceStatus(PersistenceStatus.INACTIVE);
			uiMetaData.setPersistenceStatus(PersistenceStatus.INACTIVE);
			uiMetaData.setFormName(null);
			DynamicFormFilter dynamicFormFilter = formVO.getDynamicFormFilter();
			if (dynamicFormFilter == null) {
				dynamicFormFilter = new DynamicFormFilter();
			}
			dynamicFormFilter.setActiveFlag(formVO.isActiveFlag());
			populateDynamicFormFilterData(dynamicFormFilter, formVO, uiMetaData, formVoMap);
			makerCheckerService.masterEntityChangedByUser(dynamicFormFilter, user);
			return "redirect:/app/grid/DynamicFormFilter/DynamicFormFilter/loadColumnConfig";
		} else {
			return "redirect:/app/grid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";
		}
	}

	@RequestMapping(value = "/saveAndSendForApprovalFilter")
	@PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
	public String saveAndSendForApprovalFilter(@ModelAttribute("formVO") FormVO formVO,@ModelAttribute("isFilter") Boolean isFilter,@ModelAttribute("formVoMap") Map<Long, FormVO> formVoMap) {

		User user = getUserDetails().getUserReference();
		if (isFilter != null && isFilter) {
			ModelMetaData modelMetaData = formDefinitionService.saveModelMetaData(formVO, user);
			UIMetaData uiMetaData = formDefinitionService.saveUIMetaData(formVO, modelMetaData.getUri(), user);
			modelMetaData.setPersistenceStatus(PersistenceStatus.INACTIVE);
			uiMetaData.setPersistenceStatus(PersistenceStatus.INACTIVE);
			uiMetaData.setFormName(null);
			DynamicFormFilter dynamicFormFilter = formVO.getDynamicFormFilter();
			if (dynamicFormFilter == null) {
				dynamicFormFilter = new DynamicFormFilter();
			}
			dynamicFormFilter.setActiveFlag(formVO.isActiveFlag());
			populateDynamicFormFilterData(dynamicFormFilter, formVO, uiMetaData, formVoMap);
			makerCheckerService.saveAndSendForApproval(dynamicFormFilter, user);
			return "redirect:/app/grid/DynamicFormFilter/DynamicFormFilter/loadColumnConfig";
		} else {
			return "redirect:/app/grid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";
		}
	}

	@PreAuthorize("hasAuthority('VIEW_DYNAMICFORMFILTER') or hasAuthority('MAKER_DYNAMICFORMFILTER') or hasAuthority('CHECKER_DYNAMICFORMFILTER')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewFilter(@PathVariable("id") Long id, ModelMap map) {

		FormValidationConstants.initMappings();
		UserInfo currentUser = getUserDetails();
		DynamicFormFilter dynamicFormFilter = baseMasterService.getMasterEntityWithActionsById(DynamicFormFilter.class, id, currentUser.getUserEntityId()
				.getUri());

		if (dynamicFormFilter.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
			DynamicFormFilter prevDynamicFormFilter = (DynamicFormFilter) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(dynamicFormFilter.getEntityId());
			map.put("prevDynamicFormFilter", prevDynamicFormFilter);
			map.put("editLink", false);
		} else if (dynamicFormFilter.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED
				|| dynamicFormFilter.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
			DynamicFormFilter prevDynamicFormFilter = (DynamicFormFilter) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(dynamicFormFilter.getEntityId());
			map.put("prevDynamicFormFilter", prevDynamicFormFilter);
			map.put("viewLink", false);
		}

		map.put("editLink", false);
		map.put("viewLink", false);
		SourceProduct sourceProduct=dynamicFormFilter.getSourceProduct();
		String formFieldsMetaData=dynamicFormFilter.getFilterFieldsJsonMap();
		List<UIMetaData> allDynamicFormConfigTypeList=null;
		Set<UIMetaData> allDynamicFormConfigTypeSet = null;
		if(notNull(sourceProduct) && notNull(sourceProduct.getId())){
			allDynamicFormConfigTypeSet = new HashSet<>(formService.getUniqueFormsBySourceProductAndPersistantStatus(sourceProduct.getId()));
		}

		Map<String, String> fieldFormMap=new HashMap<String, String>();
		List<Long> selectedFormIds=getUiMetaDataListFrom(formFieldsMetaData,fieldFormMap);
		for(Long uiMetaDataId : selectedFormIds) {
			FormConfigurationMapping formConfigurationMapping = formDefinitionService.loadFormConfigMappingByFormName(uiMetaDataId);
			if(formConfigurationMapping != null) {
				UIMetaData uiMetaData=formConfigurationMapping.getUiMetaData();
				allDynamicFormConfigTypeSet.add(uiMetaData);
				if(uiMetaData != null && !uiMetaDataId.equals(uiMetaData.getId())) {
					selectedFormIds.remove(uiMetaDataId);
					selectedFormIds.add(uiMetaData.getId());
				}
			}
		}
		allDynamicFormConfigTypeList = new ArrayList<>();
		allDynamicFormConfigTypeList.addAll(getNotNull(allDynamicFormConfigTypeSet));
		map.put("dynamicFormsList", allDynamicFormConfigTypeList);
		map.put("dynamicFormsIds", selectedFormIds);
		map.put("masterID", masterId);
		map.put("viewable", true);
		map.put("isFilter", true);
		map.put("formVoMap", new HashMap<Long, FormVO>());
		map.put("id", dynamicFormFilter.getId());
		initializeFilterData(selectedFormIds,  map);
		DynamicFormUtil.getDataForEditViewMode(dynamicFormFilter.getUiMetaData().getId(), map,true);
		updateSourceFormNames(map,dynamicFormFilter,fieldFormMap);
		FormVO formVO=(FormVO)map.get("formVO");
		formVO.setFormName(dynamicFormFilter.getName());
		formVO.setDynamicFormFilter(dynamicFormFilter);
		formVO.setIsShareable(dynamicFormFilter.getIsShareable());
		formVO.setIsFilter(true);
		formVO.setActiveFlag(dynamicFormFilter.isActiveFlag());
		formVO.setTaskId((Long) dynamicFormFilter.getViewProperties().get("taskId"));
		formVO.setId(dynamicFormFilter.getId());

		if (dynamicFormFilter.getViewProperties() != null) {
			ArrayList<String> actions = (ArrayList<String>) dynamicFormFilter.getViewProperties().get("actions");
			if (actions != null) {
				for (String act : actions) {
					String actionString = "act" + act;
					map.put(actionString.replaceAll(" ", ""), false);
				}
			}
		}

		return "dynamicFormFilter";
	}

	private void populateDynamicFormFilterData(DynamicFormFilter dynamicFormFilter, FormVO formVO,UIMetaData uiMetaData,Map<Long, FormVO> formVoMap) {
		dynamicFormFilter.setName(formVO.getFormName());
		dynamicFormFilter.setDescription(formVO.getFormDescription());
		dynamicFormFilter.setIsShareable(formVO.getIsShareable());
		dynamicFormFilter.setSourceProduct(formVO.getSourceProduct());
		dynamicFormFilter.setActiveFlag(formVO.isActiveFlag());
		dynamicFormFilter.setUiMetaData(uiMetaData);
		StringBuilder sourcesForms = new StringBuilder();
		Map<String, JSONObject> formFieldMap = new HashMap<String, JSONObject>();
		JSONObject filterFieldsJsonMap = new JSONObject();
		try {

			for (Map.Entry<Long, FormVO> entry : formVoMap.entrySet()) {
				JSONObject jsonObject = new JSONObject();
				JSONArray fields = new JSONArray();
				jsonObject.put("uiMetaDataId", entry.getKey());
				jsonObject.put("fields", fields);
				formFieldMap.put(entry.getValue().getFormName(), jsonObject);
				filterFieldsJsonMap.put(entry.getValue().getFormName(), jsonObject);
			}
			List<FormContainerVO> formContainerVoList = formVO.getContainerVOList();
			if (formContainerVoList != null) {
				for (FormContainerVO containerVO : formContainerVoList) {
					addAllFieldsFromForm(formFieldMap, containerVO);
				}
			}
		} catch (JSONException e) {
			BaseLoggers.exceptionLogger.error(e.getMessage(), e);
		}
		dynamicFormFilter.setFilterFieldsJsonMap(filterFieldsJsonMap.toString());
	}

	private void addAllFieldsFromForm(Map<String, JSONObject> formFieldMap, FormContainerVO containerVO) throws JSONException {

		String fieldKey = containerVO.getFieldKey();
		if (fieldKey != null) {

			String formName = containerVO.getSourceFormName();
			JSONObject jsonObject = formFieldMap.get(formName);
			JSONArray fields = null;
			if (jsonObject.has("fields")) {
				fields = (JSONArray) jsonObject.get("fields");
			} else {
				fields = new JSONArray();
			}
			fields.put(fieldKey);
			jsonObject.put("fields", fields);
		}
		if (containerVO.getFormContainerVOList() != null) {
			for (FormContainerVO formContainerVO : containerVO.getFormContainerVOList()) {
				addAllFieldsFromForm(formFieldMap, formContainerVO);
			}
		}
	}

	private void updateSourceFormNames(ModelMap map, DynamicFormFilter dynamicFormFilter,Map<String, String> fieldFormMap) {
		String formFieldMetaData=dynamicFormFilter.getFilterFieldsJsonMap();
		FormVO formVO=(FormVO)map.get("formVO");
		List<FormContainerVO> containerVoList=formVO.getContainerVOList();
		if(containerVoList==null)
		{
			return;
		}
		for(FormContainerVO containerVO:containerVoList)
		{
			updateSourceFormName(containerVO,fieldFormMap);

		}

	}




	private void updateSourceFormName(FormContainerVO containerVO, Map<String, String> fieldFormMap) {

		String key=containerVO.getFieldKey();
		String sourceFormName=fieldFormMap.get(key);
		if(sourceFormName!=null)
		{
			containerVO.setSourceFormName(sourceFormName);

		}
		List<FormContainerVO> containerVoList=containerVO.getFormContainerVOList();
		if(containerVoList==null)
		{
			return;
		}
		for(FormContainerVO formContainerVO:containerVoList)
		{
			updateSourceFormName(formContainerVO,fieldFormMap);

		}

	}




	@PreAuthorize("hasAuthority('VIEW_DYNAMICFORMFILTER') or hasAuthority('MAKER_DYNAMICFORMFILTER') or hasAuthority('CHECKER_DYNAMICFORMFILTER')")
	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
	public String editFilter(@PathVariable("id") Long id, ModelMap map) {
		FormValidationConstants.initMappings();
		UserInfo currentUser = getUserDetails();
		DynamicFormFilter dynamicFormFilter = baseMasterService.getMasterEntityWithActionsById(DynamicFormFilter.class, id, currentUser.getUserEntityId()
				.getUri());

		String formFieldsMetaData=dynamicFormFilter.getFilterFieldsJsonMap();
		List<UIMetaData> allDynamicFormConfigTypeList=null;
		Set<UIMetaData> allDynamicFormConfigTypeSet = null;

		SourceProduct sourceProduct = dynamicFormFilter.getSourceProduct();

		if(notNull(sourceProduct) && notNull(sourceProduct.getId())){
			allDynamicFormConfigTypeSet = new HashSet<UIMetaData>(formService.getUniqueFormsBySourceProductAndPersistantStatus(sourceProduct.getId()));
		}

		Map<String, String> fieldFormMap=new HashMap<String, String>();
		List<Long> selectedFormIds=getUiMetaDataListFrom(formFieldsMetaData,fieldFormMap);
		for(Long uiMetaDataId : selectedFormIds) {
			FormConfigurationMapping formConfigurationMapping = formDefinitionService.loadFormConfigMappingByFormName(uiMetaDataId);
			if(formConfigurationMapping != null) {
				UIMetaData uiMetaData=formConfigurationMapping.getUiMetaData();
				allDynamicFormConfigTypeSet.add(uiMetaData);
				if(uiMetaData != null && !uiMetaDataId.equals(uiMetaData.getId())) {
					selectedFormIds.remove(uiMetaDataId);
					selectedFormIds.add(uiMetaData.getId());
				}
			}
		}
		if(ApprovalStatus.CLONED == dynamicFormFilter.getApprovalStatus()) {
			map.put("cloned", true);
		} else {
			map.put("cloned", false);
		}
		allDynamicFormConfigTypeList = new ArrayList<UIMetaData>();
		allDynamicFormConfigTypeList.addAll(getNotNull(allDynamicFormConfigTypeSet));
		map.put("dynamicFormsList", allDynamicFormConfigTypeList);
		map.put("dynamicFormsIds", selectedFormIds);
		map.put("masterID", masterId);
		map.put("edit", true);
		map.put("isFilter", true);
		map.put("formVoMap", new HashMap<Long, FormVO>());
		map.put("id", dynamicFormFilter.getId());
		initializeFilterData(selectedFormIds,  map);
		DynamicFormUtil.getDataForEditViewMode(dynamicFormFilter.getUiMetaData().getId(), map,true);
		updateSourceFormNames(map,dynamicFormFilter,fieldFormMap);
		FormVO formVO=(FormVO)map.get("formVO");
		formVO.setDynamicFormFilter(dynamicFormFilter);
		formVO.setIsShareable(dynamicFormFilter.getIsShareable());
		formVO.setFormName(dynamicFormFilter.getName());
		formVO.setIsFilter(true);
		formVO.setActiveFlag(dynamicFormFilter.isActiveFlag());
		formVO.setTaskId((Long) dynamicFormFilter.getViewProperties().get("taskId"));
		formVO.setId(dynamicFormFilter.getId());
		return "dynamicFormFilter";
	}


	@PreAuthorize("hasAuthority('VIEW_DYNAMICFORMFILTER') or hasAuthority('MAKER_DYNAMICFORMFILTER') or hasAuthority('CHECKER_DYNAMICFORMFILTER')")
	@RequestMapping(value = "/import/{id}", method = RequestMethod.POST)
	public String importDynamicForm(@PathVariable("id") Long id,String formName,@RequestParam("importFormSourceProductId") Long importFormSourceProductId,ModelMap map,@ModelAttribute("formVO") FormVO formVo) {
		FormValidationConstants.initMappings();
		UserInfo currentUser = getUserDetails();
		User user = getUserDetails().getUserReference();
		DynamicFormFilter dynamicFormFilter = baseMasterService.getMasterEntityWithActionsById(DynamicFormFilter.class, id, currentUser.getUserEntityId()
				.getUri());

		String formFieldsMetaData=dynamicFormFilter.getFilterFieldsJsonMap();
		SourceProduct sourceProduct=null;

		if(notNull(importFormSourceProductId)){
			sourceProduct=genericParameterService.findById(importFormSourceProductId,SourceProduct.class);
		}

		UIMetaData referenceUIMetaData=dynamicFormFilter.getUiMetaData();
		UIMetaData newUiMetaData=new UIMetaData();
		newUiMetaData=(UIMetaData)referenceUIMetaData.cloneYourself(CloneOptionConstants.MAKER_CHECKER_COPY_OPTION);
		ModelMetaData modelMetaData = entityDao.find(ModelMetaData.class, formVo.getModelMetaDataId());
		ModelMetaData newModelMetaData=(ModelMetaData)modelMetaData.cloneYourself(CloneOptionConstants.MAKER_CHECKER_COPY_OPTION);
		entityDao.saveOrUpdate(newModelMetaData);
		newUiMetaData.setModelUri(newModelMetaData.getUri());
		newUiMetaData.setSourceProduct(sourceProduct);
		newUiMetaData.setSourceProductId(sourceProduct.getId());
		newUiMetaData.setFormName(formName);
		newModelMetaData.setName("ModelF"+formName);
		newUiMetaData.setModelName("ModelF"+formName);
		newModelMetaData.setPersistenceStatus(PersistenceStatus.ACTIVE);
		newUiMetaData.setPersistenceStatus(PersistenceStatus.ACTIVE);
		newUiMetaData.setFormVersion("Version1");
		newUiMetaData.getEntityLifeCycleData().setCreatedByUri(user.getUri());
		entityDao.saveOrUpdate(newUiMetaData);
		formVo.setCreateNewVersion(false);
		formVo.setSourceProduct(sourceProduct);
		formVo.setSourceProductId(sourceProduct.getId());
		formVo.setId(null);
		FormConfigurationMapping formConfigurationMapping = formDefinitionService.saveFormConfigMapping(newModelMetaData, newUiMetaData, formVo,user);
		makerCheckerService.saveAndSendForApproval(formConfigurationMapping, user);
// 	    FormVO newFormVo = formDefinitionService.createVOFromRealObject(clonedUIMetaData);
		// ModelMetaData newModelMetaData = formDefinitionService.saveModelMetaData(newFormVo,user);
		// UIMetaData newUiMetaData = formDefinitionService.saveUIMetaData(newFormVo, newModelMetaData.getUri(),user);

		formVo.setDynamicFormFilter(dynamicFormFilter);
		formVo.setIsShareable(dynamicFormFilter.getIsShareable());
		formVo.setIsFilter(true);
		formVo.setActiveFlag(dynamicFormFilter.isActiveFlag());
		formVo.setTaskId((Long) dynamicFormFilter.getViewProperties().get("taskId"));
		map.put("isFilter", true);
		return "redirect:/app/grid/FormConfigurationMapping/FormConfigurationMapping/loadColumnConfig";
	}



	private void initializeFilterData(List<Long> selectedForms, ModelMap map)
	{
		List<UIMetaData> selectedDynamicForms=(List<UIMetaData>)map.get("selectedDynamicForms");
		Map<Long, FormVO> formVoMap=(Map<Long, FormVO>)map.get("formVoMap");
		FormVO formVo=( FormVO)map.get("formVO");
		List<Long> deSelectedForms=new ArrayList<Long>();
		if(selectedDynamicForms==null)
		{
			selectedDynamicForms=new ArrayList<UIMetaData>();
		}
		if(formVoMap==null)
		{
			formVoMap=new HashMap<Long, FormVO>();
		}
		//remove de-selected ids from formVoMap
		for(Long id: formVoMap.keySet())
		{
			if(!selectedForms.contains(id))
			{

				deSelectedForms.add(id);
			}

		}
		for(Long deSelectedFormsId : deSelectedForms){
			removeMultipleComponents(deSelectedFormsId,formVo,formVoMap);
		}
		for(Long id: selectedForms)
		{
			if(formVoMap.containsKey(id))
			{
				FormConfigurationMapping formConfigurationMapping = formDefinitionService.loadFormConfigMappingByFormName(id);
				if(formConfigurationMapping != null) {
					UIMetaData uiMetaData=formConfigurationMapping.getUiMetaData();
					DynamicFormUtil.initializeUiMetaData(uiMetaData);
					selectedDynamicForms.add(uiMetaData);
				}
				continue;
			}
			FormConfigurationMapping formConfigurationMapping = formDefinitionService.loadFormConfigMappingByFormName(id);
			if(formConfigurationMapping != null) {
				UIMetaData uiMetaData=formConfigurationMapping.getUiMetaData();
				DynamicFormUtil.initializeUiMetaData(uiMetaData);
				FormVO formVO = formDefinitionService.createVOFromRealObject(uiMetaData);
				formVoMap.put(uiMetaData.getId(), formVO);
				selectedDynamicForms.add(uiMetaData);
			}
		}
		map.put("selectedDynamicForms", selectedDynamicForms);
		map.put("formVoMap", formVoMap);
		map.put("formVO", formVo);
	}
	protected void removeMultipleComponents(Long deSelectedFormId, FormVO formVo,Map<Long, FormVO> formVoMap)
	{
		FormVO deSelectedFormVo=formVoMap.get(deSelectedFormId);
		List<FormContainerVO> deSelectedContainerRootList=deSelectedFormVo.getContainerVOList();
		List<String> deSelectedFrmCntnrKeyLst=new LinkedList<>();
		List<FormContainerVO> newFormContainers=formVo.getContainerVOList();
		List<FormContainerVO> tempFormContainers=new LinkedList<>(newFormContainers);
		for(FormContainerVO containerVO:deSelectedContainerRootList)
		{
			getFormContainersByKey(containerVO,deSelectedFrmCntnrKeyLst);
		}
		for(FormContainerVO containerVO:tempFormContainers)
		{
			removeFormContainersByKey(containerVO,deSelectedFrmCntnrKeyLst,newFormContainers);
		}
		tempFormContainers.clear();
		tempFormContainers.addAll(newFormContainers);
		for(FormContainerVO containerVO : tempFormContainers){
			removeEmptyContainerVOs(containerVO,newFormContainers);
		}

		formVo.setContainerVOList(newFormContainers);
		formVoMap.remove(deSelectedFormId);

	}
	private void getFormContainersByKey(FormContainerVO containerVO, List<String> formContainersMap) {
		if(containerVO.getFieldKey()!=null)
		{
			formContainersMap.add(containerVO.getFieldKey());
		}
		if(containerVO.getFormContainerVOList()!=null)
		{
			for(FormContainerVO childFields:containerVO.getFormContainerVOList())
			{
				getFormContainersByKey(childFields,formContainersMap);
			}
		}

	}
	private void removeFormContainersByKey(FormContainerVO containerVO, List<String> deSelectedFrmCntnrKeyLst,List<FormContainerVO> newFormContainers) {
		if(containerVO.getFieldKey()!=null && deSelectedFrmCntnrKeyLst.contains(containerVO.getFieldKey()))
		{
			newFormContainers.remove(containerVO);
			return;
		}
		if(containerVO.getFormContainerVOList()!=null)
		{
			List<FormContainerVO> childFieldContainerVOList=containerVO.getFormContainerVOList();
			List<FormContainerVO> tempFormContainers=new LinkedList<>(childFieldContainerVOList);
			for(FormContainerVO childFields:tempFormContainers)
			{
				removeFormContainersByKey(childFields,deSelectedFrmCntnrKeyLst,childFieldContainerVOList);
			}
		}

	}
	private void removeEmptyContainerVOs(FormContainerVO containerVO,List<FormContainerVO> newFormContainers) {
		if(containerVO.getFieldKey()==null && containerVO.getFormContainerVOList()!=null && containerVO.getFormContainerVOList().isEmpty()){
			newFormContainers.remove(containerVO);
			return;
		}
		if(containerVO.getFormContainerVOList()!=null)
		{	List<FormContainerVO> childFieldContainerVOList=containerVO.getFormContainerVOList();
			List<FormContainerVO> tempFormContainers=new LinkedList<>(childFieldContainerVOList);
			for(FormContainerVO childContainerVO : tempFormContainers)
				removeEmptyContainerVOs(childContainerVO,childFieldContainerVOList);
		}

	}
	private List<Long> getUiMetaDataListFrom(String formFieldsMetaData,Map<String, String> fieldFormMap) {
		List<Long> selectedFormIds=new ArrayList<Long>();
		try {
			JSONObject jsonObject=new JSONObject(formFieldsMetaData);
			Iterator<String> keys=jsonObject.keys();
			while(keys.hasNext())
			{
				String formName=keys.next();
				JSONObject formData=(JSONObject) jsonObject.get(formName);
				Long uiMetaDataId=formData.getLong(DynamicFormFilter.UIMETADATA_ID);
				if(!formData.has(DynamicFormFilter.FORM_FIELDS)){
					formData.put("fields",new JSONArray());
				}
				JSONArray fields=(JSONArray)formData.get(DynamicFormFilter.FORM_FIELDS);
				for(int i=0;i<fields.length();i++)
				{
					fieldFormMap.put(fields.getString(i), formName);
				}
				selectedFormIds.add(uiMetaDataId);
			}
		} catch (JSONException e) {
			BaseLoggers.flowLogger.debug("Error occured parsing json for dynamic form filter"+e);

		}
		return selectedFormIds;
	}




	@PreAuthorize("hasAuthority('MAKER_DYNAMICFORMFILTER')")
	@RequestMapping(value = "/createFilter")
	public String createFilterFromSelectedForms(Long selectedForms[], ModelMap map) {

		List<Long> selectedFormsList=new ArrayList<Long>();
		for(int i=0;i<selectedForms.length;i++)
		{
			selectedFormsList.add(selectedForms[i]);
		}
		initializeFilterData(selectedFormsList,  map);
		map.put("masterID", masterId);
		FormVO formVO =null;
		formVO=(FormVO)map.get("formVO");

		if(map.get("componentList")==null)
		{
			map.put("componentList", genericParameterService.retrieveTypes(FormComponentType.class));
		}

		map.put("isFilter", true);
		map.put("isCreateDyFilterBtnClicked", true);

		return "filterCreation";
	}
	@PreAuthorize("hasAuthority('MAKER_DYNAMICFORMFILTER')")
	@RequestMapping(value = "/displayForm")
	public String displayFormForFieldSelection(Long formId, ModelMap map,@ModelAttribute("formVO") FormVO formVo) {


		UIMetaData uiMetaData=	formService.getUiMetaDataById(formId);
		UIMetaDataVo uIMetaDataVo =formDefinitionUtility.mergeFormDetailsAndData(uiMetaData, null);
		map.put("showSaveButton", false);
		if(ValidatorUtils.isNull(uIMetaDataVo)) {
			map.put("isDynamicFormAttached", false);
		}
		else {
			map.put("isDynamicFormAttached", true);
		}
		map.put("uiMetaDataVo", ValidatorUtils.isNull(uIMetaDataVo) ? new UIMetaDataVo() : uIMetaDataVo);
		map.put("viewMode", true);
		map.put("masterID", masterId);
		map.put("offlineTemplate", false);
		map.put("formVO", formVo);
		map.put("defaultValuesMapString","null");
		if (ValidatorUtils.notNull(uIMetaDataVo) && ValidatorUtils.notNull(uIMetaDataVo.getFormName())) {
			map.put("formKey", uIMetaDataVo.getFormName().replaceAll(" ", "_"));
		}
		return "filterFormPreview";
	}

	@PreAuthorize("hasAuthority('MAKER_DYNAMICFORMFILTER')")
	@RequestMapping(value = "/loadDynamicFormsBySourceProduct")
	public String loadDynamicFormsBySourceProduct(@RequestParam("sourceProductId") Long sourceProductId,
												  @RequestParam("viewMode") boolean viewMode,ModelMap map) {

		List<UIMetaData> allDynamicFormConfigTypeList=null;
		Set<UIMetaData> allDynamicFormConfigTypeSet = null;
		SourceProduct sourceProduct=null;

		if(notNull(sourceProductId)){
			sourceProduct=genericParameterService.findById(sourceProductId,SourceProduct.class);
		}

		if(notNull(sourceProduct) && notNull(sourceProduct.getId())){
			allDynamicFormConfigTypeSet = new HashSet<UIMetaData>(formService.getUniqueFormsBySourceProductAndPersistantStatus(sourceProduct.getId()));
		}

		allDynamicFormConfigTypeList = new ArrayList<UIMetaData>();
		allDynamicFormConfigTypeList.addAll(getNotNull(allDynamicFormConfigTypeSet));
		map.put("dynamicFormsList", allDynamicFormConfigTypeList);
		map.put("viewable", viewMode);

		return "dynamicFormsView";
	}




}