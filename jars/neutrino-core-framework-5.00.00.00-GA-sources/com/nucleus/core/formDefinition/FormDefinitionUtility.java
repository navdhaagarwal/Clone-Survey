package com.nucleus.core.formDefinition;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasAnyEntry;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;


import com.nucleus.core.dynamicform.exception.InvalidDynamicFormDataException;
import com.nucleus.core.formsConfiguration.fieldcomponent.LOVFieldVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentSet;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.core.dynamicform.service.FormConfigurationConstant;
import com.nucleus.core.dynamicform.service.FormConfigurationMappingService;
import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.dynamicform.service.JsonMapConverter;
import com.nucleus.core.formsConfiguration.DynamicForm;
import com.nucleus.core.formsConfiguration.DynamicFormData;
import com.nucleus.core.formsConfiguration.DynamicFormScreenMapping;
import com.nucleus.core.formsConfiguration.DynamicFormScreenMappingDetail;
import com.nucleus.core.formsConfiguration.FieldCustomOptions;
import com.nucleus.core.formsConfiguration.FieldCustomOptionsVO;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.core.formsConfiguration.FormComponentType;
import com.nucleus.core.formsConfiguration.FormComponentVO;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.FormContainerType;
import com.nucleus.core.formsConfiguration.FormFieldVO;
import com.nucleus.core.formsConfiguration.IDynamicForm;
import com.nucleus.core.formsConfiguration.MasterDynamicForm;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.MultipleForm;
import com.nucleus.core.formsConfiguration.MultipleFormData;
import com.nucleus.core.formsConfiguration.PanelDefinition;
import com.nucleus.core.formsConfiguration.PersistentFormData;
import com.nucleus.core.formsConfiguration.SingleDynamicForm;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.formsConfiguration.UIMetaDataVo;
import com.nucleus.core.formsConfiguration.fieldcomponent.EmailInfoVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.core.money.entity.Money;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.general.util.CoreDateUtility;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserService;
import com.nucleus.core.loanproduct.ProductType;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.AbstractTransformer;
import flexjson.transformer.Transformer;

@Named("formDefinitionUtility")
public class FormDefinitionUtility {

	@Inject
	@Named("formConfigService")
	protected FormService formService;

	@Inject
	@Named("jsonMapConverter")
	private JsonMapConverter jsonMapConverter;


	@Inject
    @Named("entityDao")
    protected EntityDao         entityDao;
	
	@Inject
    @Named("userService")
    protected UserService       userService;
	
	
	   
	private static final String UNABLE_TO_CONVERT_VALUE_TO_DATE_OBJECT="Unable to convert parse value to Date";
	private JSONSerializer outgointObjectSerializer = new JSONSerializer().transform(new AbstractTransformer() {
		
		@Override
		public void transform(Object object) {
			//DO NOTHING
			getContext().writeOpenArray();
			getContext().writeCloseArray();
		}
	
	},new Class[]{PersistentBag.class,PersistentSet.class,org.hibernate.proxy.HibernateProxy.class,org.joda.time.DateTime.class
			
	})
			.include("values.*").exclude(
			new String[] { "*.class", "*.entityDisplayName", "*.displayName", "*.entityId", "*.entityLifeCycleData",
					"*.makeBusinessDate", "*.uuid", "*.persistentFormData", "*.masterLifeCycleData","*.authorizationBusinessDate",
					"*.viewProperties"});
	
	@Inject
	@Named("formConfigurationMappingService")
	protected FormConfigurationMappingService formConfigurationMappingService;

	 private static final String UNABLE_TO_CONVERT_JSON_STRING="Unable to convert multipleFormData map to JSON String";
	 private static final String UNABLE_TO_CONVERT_JSON_TO_MAP="Unable to convert JSON String to MultipleFormData map";

	 private final Transformer       money_transformer = new AbstractTransformer() {
         @Override
         public void transform(Object object) {
             Money money = (Money) object;
             getContext().write("\"" + money.toString() + "\"");
         }
     };

	public UIMetaDataVo prepareUIMetaDataVoForNonWorkFlowBased(String uri,
			Long screenId, String formName,Long sourceProductId,String viewMode,String dynamicFormData,String productType) {
	UIMetaDataVo uiMetaDataVo=null;
		if(!isEmpty(uri) && !uri.equals("null")  && notNull(screenId) && !isEmpty(formName)){
			uiMetaDataVo=prepareUIMetaDataVoBasedOnUriAndFormName(uri,screenId,formName,false,sourceProductId,viewMode,dynamicFormData,productType);
		}else if( !isEmpty(formName) && !formName.equals("null") && notNull(screenId)){
			uiMetaDataVo = prepareUIMetaDataVoBasedOnFormName(screenId,formName,sourceProductId);
		}
		else if(notNull(screenId)){
			uiMetaDataVo=prepareUIMetaDataVoBasedOnScreenId(screenId,sourceProductId,dynamicFormData,productType);
		}
		if(ValidatorUtils.notNull(uiMetaDataVo)){
			uiMetaDataVo.setPlaceHolderID(screenId);
		}
		return uiMetaDataVo;
	}

	public UIMetaDataVo prepareUIMetaDataVoForNonWorkFlowBased(String uri, EntityId entityId,
			Long screenId, String formName, Long sourceProductId, String viewMode, String dynamicFormData) {
		UIMetaDataVo uiMetaDataVo = null;
		if (!isEmpty(uri) && !uri.equals("null") && notNull(screenId) && !isEmpty(formName)) {
			uiMetaDataVo = prepareUIMetaDataVoBasedOnUriAndFormName(uri, entityId, screenId, formName, false,
					sourceProductId, viewMode, dynamicFormData);
		} else if (!isEmpty(formName) && !formName.equals("null") && notNull(screenId)) {
			uiMetaDataVo = prepareUIMetaDataVoBasedOnFormName(screenId, formName, sourceProductId);
		} else if (notNull(screenId)) {
			uiMetaDataVo = prepareUIMetaDataVoBasedOnScreenId(screenId, sourceProductId, dynamicFormData,null);
		}
		if (ValidatorUtils.notNull(uiMetaDataVo)) {
			uiMetaDataVo.setPlaceHolderID(screenId);
		}
		return uiMetaDataVo;
	}
	
	private UIMetaDataVo prepareUIMetaDataVoBasedOnFormName(Long screenId,
			String formName,Long sourceProductId) {
		UIMetaDataVo uiMetaDataVo=null;
		List<DynamicFormScreenMappingDetail> mappedDynamicFormsList = fetchDynamicFormsMappedToScreenIdAndSourceProduct(screenId,sourceProductId,null);
			for (DynamicFormScreenMappingDetail mappedDynamicForm : mappedDynamicFormsList) {
				if (notNull(mappedDynamicForm.getFormConfigurationMapping())) {
					UIMetaData uiMetaDataObj = mappedDynamicForm
							.getFormConfigurationMapping().getUiMetaData();
					if(notNull(uiMetaDataObj) && formName.equals(uiMetaDataObj.getFormName())){
						 uiMetaDataVo = mergeFormDetailsAndData(
								 uiMetaDataObj, null);
						 uiMetaDataVo.setFormViewMode(!mappedDynamicForm.getEditModeEnabled());
						 return uiMetaDataVo;
					}
				}
			}

		return null;
	}

	public boolean isDynamicFormMapped(UIMetaData uiMetaData){
		return isNonWorkflowBasedFormMappingDone(uiMetaData) || isWorkflowBasedFormFormMappingDone(uiMetaData) ;
	}
	public boolean isNonWorkflowBasedFormMappingDone(UIMetaData uiMetaData) {
		
		NamedQueryExecutor<DynamicFormScreenMappingDetail> dynamicFormMappingDtlsQuery = new NamedQueryExecutor<DynamicFormScreenMappingDetail>(
				"getDynamicFormMappingDtlByDynamicForm").addParameter("uiMetaDataId", uiMetaData.getId());
		List<DynamicFormScreenMappingDetail> dynamicFormMappingDtls = entityDao.executeQuery(dynamicFormMappingDtlsQuery);
		if(ValidatorUtils.hasElements(dynamicFormMappingDtls)){
			for(DynamicFormScreenMappingDetail dynamicFormScreenMappingDetail:dynamicFormMappingDtls){
				DynamicFormScreenMapping dynamicFormScreenMapping=entityDao.find(DynamicFormScreenMapping.class, dynamicFormScreenMappingDetail.getDynamicFormMappingId());	
				if(dynamicFormScreenMapping.isActiveFlag() && dynamicFormScreenMapping.getApprovalStatus()!=ApprovalStatus.DELETED_APPROVED_IN_HISTORY){
					return true;
				}
			}
		}
		return false;
	}

	public boolean isWorkflowBasedFormFormMappingDone(UIMetaData uiMetaData) {
	
		NamedQueryExecutor<PersistentFormData> persistentFormDataQuery = new NamedQueryExecutor<PersistentFormData>(
				"getPersistentFormDataByFormUri").addParameter("uiMetaDataUri", EntityId.getUri(uiMetaData.getId(), UIMetaData.class));
		List<PersistentFormData> persistentFormDataList = entityDao.executeQuery(persistentFormDataQuery);
		if(ValidatorUtils.hasElements(persistentFormDataList)){
			return true;
		}
		return false;
	}

	private UIMetaDataVo prepareUIMetaDataVoBasedOnScreenId(Long screenId,Long sourceProductId,String dynamicFormData,String productType) {
		UIMetaDataVo uiMetaDataVo=null;
		Map<String, Object> dataMap = new HashMap<>();
		List<DynamicFormScreenMappingDetail> mappedDynamicFormsList = fetchDynamicFormsMappedToScreenIdAndSourceProduct(screenId,sourceProductId,productType);
		if (hasElements(mappedDynamicFormsList) && mappedDynamicFormsList.size()==1) {
			for (DynamicFormScreenMappingDetail mappedDynamicForm : mappedDynamicFormsList) {
				if (notNull(mappedDynamicForm.getFormConfigurationMapping())) {
					UIMetaData uiMetaDataObj = mappedDynamicForm
							.getFormConfigurationMapping().getUiMetaData();
					
					dataMap = prepareDataToBePopulatedInDynamicForm(uiMetaDataObj,null,dynamicFormData);
					
					 uiMetaDataVo = mergeFormDetailsAndData(
							 uiMetaDataObj, dataMap);
					 uiMetaDataVo.setFormViewMode(!mappedDynamicForm.getEditModeEnabled());
				}
			}
		}
		return uiMetaDataVo;
	}


	private UIMetaDataVo prepareUIMetaDataVoBasedOnUriAndFormName(String uri,
			Long screenId, String formName, Boolean workFlowBased, Long sourceProductId, String viewMode,
			String dynamiCFormData,String productType) {
		Object object = entityDao.get(EntityId.fromUri(uri));
		return prepareUIMetaDataVo(uri, screenId, formName, workFlowBased, sourceProductId, viewMode, dynamiCFormData,
				object,productType);

	}
	
	private UIMetaDataVo prepareUIMetaDataVoBasedOnUriAndFormName(String uri, EntityId entityId,
			Long screenId, String formName, Boolean workFlowBased, Long sourceProductId, String viewMode,
			String dynamiCFormData) {
		Object object = entityDao.get(entityId);
		return prepareUIMetaDataVo(uri, screenId, formName, workFlowBased, sourceProductId, viewMode, dynamiCFormData,
				object,null);
	}

	private UIMetaDataVo prepareUIMetaDataVo(String uri, Long screenId, String formName, Boolean workFlowBased,
			Long sourceProductId, String viewMode, String dynamiCFormData, Object object,String productType) {
		UIMetaDataVo uiMetaDataVo = null;
		if (object instanceof MultipleForm) {
			uiMetaDataVo = prepareUiMetaDataVoForMultipleDynamicFormBasedOnFormNameAndScreenId(object, formName,
					screenId, workFlowBased, sourceProductId, viewMode);
		} else if (object instanceof SingleDynamicForm) {
			uiMetaDataVo = prepareUiMetaDataVoForSingleDynamicForm(object, viewMode, screenId, sourceProductId,
					dynamiCFormData,productType);
		} else if (object instanceof MultipleFormData) {
			Map<String, Object> pfdPlusUiMap = formConfigurationMappingService.getUiMetaData(formName, null, uri, null);
			PersistentFormData persistentFormData = (PersistentFormData) pfdPlusUiMap
					.get(FormConfigurationConstant.PERSISTENT_FORM_DATA);
			Map<String, Object> dataMap = formService.loadPersistentDataMap(uri, formName, persistentFormData);
			List<UIMetaData> uIMetaDatas = formService.getFormByNameAndSourceProduct(formName, sourceProductId);
			if (hasElements(uIMetaDatas)) {
				uiMetaDataVo = mergeFormDetailsAndData(uIMetaDatas.get(0), dataMap);
			}
		} else if (object instanceof MasterDynamicForm) {
			uiMetaDataVo = prepareUiMetaDataVoForMasterDynamicForm(object, viewMode, screenId, sourceProductId,
					dynamiCFormData);
		}
		return uiMetaDataVo;
	}

	
    private UIMetaDataVo prepareUiMetaDataVoForMasterDynamicForm(Object object, String viewMode, Long screenId,
			Long sourceProductId, String dynamiCFormData) {
    	MasterDynamicForm masterDynamicForm=(MasterDynamicForm) object;
    	if(isNull(object)){
    		return null;
    	}
		DynamicForm dynamicFormObj=masterDynamicForm.getDynamicForm();
		if(isNull(dynamicFormObj) && !("true").equals(viewMode)
				&& notNull(screenId) && notNull(sourceProductId)){
			return prepareUIMetaDataVoBasedOnScreenId(screenId,sourceProductId,dynamiCFormData,null);
		}else if(isNull(dynamicFormObj)){
			return null;
		}
		Map<String,Object> formDataMap = dynamicFormObj.getFieldValuePariringForDisplay();
		UIMetaDataVo uiMetaDataVo = mergeFormDetailsAndData(
				dynamicFormObj.getUiMetaData(), formDataMap);
		return uiMetaDataVo;
	}

	private UIMetaDataVo prepareUiMetaDataVoForSingleDynamicForm(
				Object object,String viewMode,Long screenId,Long sourceProductId,String dynamiCFormData,String productType) {
	    	SingleDynamicForm singleDynamicFormobj=(SingleDynamicForm) object;
	    	if(isNull(object)){
	    		return null;
	    	}
			DynamicForm dynamicFormObj=singleDynamicFormobj.getDynamicForm();
			if(isNull(dynamicFormObj) && !("true").equals(viewMode)
					&& notNull(screenId) && notNull(sourceProductId)){
				return prepareUIMetaDataVoBasedOnScreenId(screenId,sourceProductId,dynamiCFormData,productType);
			}else if(isNull(dynamicFormObj)){
				return null;
			}
			Map<String,Object> formDataMap = dynamicFormObj.getFieldValuePariringForDisplay();
			UIMetaDataVo uiMetaDataVo = mergeFormDetailsAndData(
					dynamicFormObj.getUiMetaData(), formDataMap);
			return uiMetaDataVo;
		}

	private UIMetaDataVo prepareUiMetaDataVoForMultipleDynamicFormBasedOnFormNameAndScreenId(
			Object object, String formName, Long screenId,Boolean workFlowBased,Long sourceProductId,String viewMode) {
		UIMetaDataVo uiMetaDataVo = null;
    	MultipleForm multipleFormDataObj=(MultipleForm)object;
    	if(isNull(object)){
    		return null;
    	}
		DynamicForm dynamicForm=multipleFormDataObj.getDynamicForm(formName);

		if(isNull(dynamicForm) && !("true").equals(viewMode)
				&& notNull(screenId) && notNull(sourceProductId)){
			return prepareUIMetaDataVoBasedOnFormName(screenId,formName,sourceProductId);
		}else if(isNull(dynamicForm)){
			return null;
		}
		if(notNull(dynamicForm)){
			Map<String,Object> formDataMap=dynamicForm.getFieldValuePariringForDisplay();
			 uiMetaDataVo = mergeFormDetailsAndData(
					 dynamicForm.getUiMetaData(), formDataMap);
			 if(!workFlowBased){
				 uiMetaDataVo.setFormViewMode(checkIfFormIsAvailableInViewMode(formName,screenId,sourceProductId));
			 }

		}else if(notNull(screenId) && notNull(sourceProductId)){
			List<DynamicFormScreenMappingDetail> mappedDynamicFormsList = fetchDynamicFormsMappedToScreenIdAndSourceProduct(screenId,sourceProductId,null);
			if (hasElements(mappedDynamicFormsList)){
				for (DynamicFormScreenMappingDetail mappedDynamicForm : mappedDynamicFormsList) {
					if (notNull(mappedDynamicForm.getFormConfigurationMapping()) && notNull(mappedDynamicForm
								.getFormConfigurationMapping().getUiMetaData()) && notNull(mappedDynamicForm
								.getFormConfigurationMapping().getUiMetaData().getFormName())
								&& mappedDynamicForm
								.getFormConfigurationMapping().getUiMetaData().getFormName().equals(formName)) {
						UIMetaData uiMetaDataObj = mappedDynamicForm
								.getFormConfigurationMapping().getUiMetaData();
						 uiMetaDataVo = mergeFormDetailsAndData(
								 uiMetaDataObj, null);
						 uiMetaDataVo.setFormViewMode(!mappedDynamicForm.getEditModeEnabled());
					}
				}
			}
		}

		return uiMetaDataVo;

	}



	public UIMetaDataVo mergeFormDetailsAndData(UIMetaData uiMetaData, Map<String, Object> dataMap) {

        UIMetaDataVo uiMetaDataVo = new UIMetaDataVo();
        List<FormComponentVO> formComponentVOList = null;
        List<FormFieldVO> formFieldVOList = null;
        Map<String,List<String>> defaultValuesMap = new HashMap<String, List<String>>();

        List<Map<String, Object>> actualTableMapData = null;

        Map<String, Object> fieldValueMap = null;

        uiMetaDataVo.setFormName(uiMetaData.getFormName());
        uiMetaDataVo.setFormTitle(uiMetaData.getFormTitle());
        uiMetaDataVo.setModelName(uiMetaData.getModelName());
        uiMetaDataVo.setFormHeader(uiMetaData.getFormHeader());
        uiMetaDataVo.setAllowSaveOption(uiMetaData.getAllowSaveOption());
        uiMetaDataVo.setAllowBorder(uiMetaData.getAllowBorder());
        uiMetaDataVo.setFormuuid(uiMetaData.getFormuuid());
        uiMetaDataVo.setFormVersion(uiMetaData.getFormVersion());
        uiMetaDataVo.setFormUri(uiMetaData.getUri());
        uiMetaDataVo.setModelUri(uiMetaData.getModelUri());
        uiMetaDataVo.setValidationJS(uiMetaData.getFormValidationRulesInJS());
        Hibernate.initialize(uiMetaData.getPanelDefinitionList());
        if (null != uiMetaData.getPanelDefinitionList()) {

            formComponentVOList = new ArrayList<FormComponentVO>();

            // Loop through the panels
            for (PanelDefinition panelDefinition : uiMetaData.getPanelDefinitionList()) {

                fieldValueMap = dataMap;

                if (null != panelDefinition) {

                    if ((panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_PANEL) && hasAnyEntry(dataMap)) {
                        fieldValueMap = (Map<String, Object>) dataMap.get(panelDefinition.getPanelKey());
                    }

                    if ((panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_TABLE || panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE) &&  hasAnyEntry(dataMap)) {
                        actualTableMapData = (List<Map<String, Object>>) dataMap.get(panelDefinition.getPanelKey());
                    }

                    FormComponentVO formComponentVO = new FormComponentVO();

                    formComponentVO.setPanelName(panelDefinition.getPanelName());
                    formComponentVO.setPanelHeader(panelDefinition.getPanelHeader());
                    formComponentVO.setAccordian(panelDefinition.isAccordian());
                    formComponentVO.setDisplayBorder(panelDefinition.isDisplayBorder());
                    formComponentVO.setPanelColumnLayout(panelDefinition.getPanelColumnLayout());
                    formComponentVO.setPanelType(panelDefinition.getPanelType());
                    formComponentVO.setPanelKey(panelDefinition.getPanelKey());
					formComponentVO.setSpecialTable(panelDefinition.getSpecialTable()!=null?panelDefinition.getSpecialTable().getKeyy():null);
					formComponentVO.setAllowPanelSave(panelDefinition.getAllowPanelSave());
                    // Loop through the fields inside panel

                    Hibernate.initialize(panelDefinition.getFieldDefinitionList());
                    if (null != panelDefinition.getFieldDefinitionList()) {

                        formFieldVOList = new ArrayList<FormFieldVO>();
                        if (formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_PANEL
                                || formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_VIRTUAL) {

                            for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {

                                Hibernate.initialize(fieldDefinition.getValue());

                                FormFieldVO formFieldVO = new FormFieldVO();
                                formFieldVO.setId(fieldDefinition.getFieldKey());
                                formFieldVO.setFieldType(fieldDefinition.getFieldType());
                                formFieldVO.setFieldDataType(fieldDefinition.getFieldDataType());
                                if(null != fieldDefinition.getParent() && !"".equals(fieldDefinition.getParent())){
                                	formFieldVO.setParent(fieldDefinition.getParent());
                                }
                                formFieldVO.setDisable(fieldDefinition.getDisable());
                                // to generate default Date

                                setDateFieldProperties(fieldDefinition, formFieldVO);

								// If Actual panel and virtual panel
								setFieldValuesForNonTable(formFieldVOList, fieldDefinition, formFieldVO);

                                if (null != fieldValueMap && fieldValueMap.size() > 0) {
                                    setFieldValue(fieldValueMap, fieldDefinition, formFieldVO);
                                } else {
                                    formFieldVO.setValue(fieldDefinition.getValue());
                                }

                                prepareDefaultValuesMapForForm(formFieldVO,uiMetaData.getFormName(),defaultValuesMap);

                                formComponentVO.setFormFieldVOList(formFieldVOList);

                            }
                        } else if ((formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_TABLE) || (formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE)) {
                            // value in configuration map for table
                            if (actualTableMapData != null) {
                                List<FormComponentVO> formComponentVOs = new ArrayList<FormComponentVO>();
                                for (Map<String, Object> singleTableRowMap : actualTableMapData) {
                                    FormComponentVO tableComponentVO = new FormComponentVO();
                                    List<FormFieldVO> fieldVOs = new ArrayList<FormFieldVO>();
                                    for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {

                                        Hibernate.initialize(fieldDefinition.getValue());
										Hibernate.initialize(fieldDefinition.getSpecialTable());
                                        FormFieldVO formFieldVO = new FormFieldVO();
                                        formFieldVO.setId(fieldDefinition.getFieldKey());
                                        formFieldVO.setFieldType(fieldDefinition.getFieldType());
                                        formFieldVO.setFieldDataType(fieldDefinition.getFieldDataType());
                                        formFieldVO.setSpecialTable(fieldDefinition.getSpecialTable()!=null?fieldDefinition.getSpecialTable().getKeyy():null);
                                        // setting filed value
                                        // If not Table - add properties - Run in all case
                                        setFieldValueForTable(fieldDefinition, formFieldVO);
                                        // setting value from map

                                        setFieldValue(singleTableRowMap, fieldDefinition, formFieldVO);
                                        prepareDefaultValuesMapForForm(formFieldVO,uiMetaData.getFormName(),defaultValuesMap);
                                        fieldVOs.add(formFieldVO);
                                    }
                                    tableComponentVO.setFormFieldVOList(fieldVOs);
                                    formComponentVOs.add(tableComponentVO);
                                }
                                formComponentVO.setFormComponentList(formComponentVOs);
                            }
                            // no value in configuration map for table
                            else {
                                List<FormComponentVO> formComponentVOs = new ArrayList<FormComponentVO>();
                                FormComponentVO tableComponentVO = new FormComponentVO();
                                List<FormFieldVO> fieldVOs = new ArrayList<FormFieldVO>();
                                for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {

                                    Hibernate.initialize(fieldDefinition.getValue());
									Hibernate.initialize(fieldDefinition.getSpecialTable());
                                    FormFieldVO formFieldVO = new FormFieldVO();
                                    formFieldVO.setId(fieldDefinition.getFieldKey());
                                    formFieldVO.setFieldType(fieldDefinition.getFieldType());
                                    formFieldVO.setFieldDataType(fieldDefinition.getFieldDataType());
									formFieldVO.setSpecialTable(fieldDefinition.getSpecialTable()!=null?fieldDefinition.getSpecialTable().getKeyy():null);
                                    // setting filed value
                                    // If not Table - add properties - Run in all case
                                    setFieldValueForTable(fieldDefinition, formFieldVO);
                                    // setting default value
                                    formFieldVO.setValue(fieldDefinition.getValue());
                                    prepareDefaultValuesMapForForm(formFieldVO,uiMetaData.getFormName(),defaultValuesMap);
                                    fieldVOs.add(formFieldVO);
                                }
                                tableComponentVO.setFormFieldVOList(fieldVOs);
                                formComponentVOs.add(tableComponentVO);
                                formComponentVO.setFormComponentList(formComponentVOs);
                            }

                        }

                    }
                    formComponentVOList.add(formComponentVO);
                }
            }
            uiMetaDataVo.setUiComponents(formComponentVOList);
            uiMetaDataVo.setDefaultValuesMap(defaultValuesMap);
        }
        return uiMetaDataVo;
    }


    private void setDateFieldProperties(FieldDefinition fieldDefinition, FormFieldVO formFieldVO) {
        if (fieldDefinition.getFieldType().equals(FormComponentType.DATE)) {
            if (ValidatorUtils.hasNoElements(fieldDefinition.getValue())
                    || "".equals(fieldDefinition.getValue().get(0)) 
                    || ValidatorUtils.isNull(fieldDefinition.getValue().get(0))) {
            	String defaultYear = null;
            	String defaultMonth = null;
            	if(fieldDefinition.getDefaultYear() != null){
            		defaultYear = fieldDefinition.getDefaultYear().toString();
            	}else{
            		Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
            		defaultYear = currentYear.toString();
            	}
            	
            	if(fieldDefinition.getDefaultMonth() != null && fieldDefinition.getDefaultMonth().toString().length()==1){
            		defaultMonth = "0"+fieldDefinition.getDefaultMonth().toString();
            	}else if(fieldDefinition.getDefaultMonth() != null){
            		defaultMonth=fieldDefinition.getDefaultMonth().toString();
            	}else{
            		defaultMonth="01";
            	}
            	
            	if(ValidatorUtils.notNull(fieldDefinition.getDefaultYear()) || ValidatorUtils.notNull(fieldDefinition.getDefaultMonth()))
            	formFieldVO.setDefDate(setDefDateAsPerUserFormat(defaultMonth+"/01/"+defaultYear));
            }

            else {
                formFieldVO.setDefDate(fieldDefinition.getValue().get(0));
            }

        }
    }

    private void setFieldValue(Map<String, Object> fieldValueMap, FieldDefinition fieldDefinition, FormFieldVO formFieldVO) {
        if (null != fieldValueMap && fieldValueMap.containsKey(fieldDefinition.getFieldKey())) {

            List<String> newValues = new ArrayList<>();
            List<Object> oldValuesList = null;

            Object object = fieldValueMap.get(fieldDefinition.getFieldKey());

            if (object instanceof java.util.List) {

                oldValuesList = (List<Object>) fieldValueMap.get(fieldDefinition.getFieldKey());

            } else {

                oldValuesList = new ArrayList<>();
                oldValuesList.add(object);
            }

           if (fieldDefinition.getFieldType().equals(FormComponentType.PHONE)) {
            
        	   setFieldValueForPhone(oldValuesList,formFieldVO,fieldDefinition);
            } else if (fieldDefinition.getFieldType().equals(FormComponentType.EMAIL)) {
             
            	setFieldValueForEmail(oldValuesList,formFieldVO);
            } else if (fieldDefinition.getFieldType().equals(FormComponentType.LOV)){



			   if (CollectionUtils.isNotEmpty(oldValuesList)) {
				   formFieldVO.setLovFieldVO((LOVFieldVO) oldValuesList.get(0));
			   } else {
				   formFieldVO.setLovFieldVO(new LOVFieldVO());
			   }
            } else  if (null != oldValuesList) {
                
                    for (Object obj : oldValuesList) {
                        newValues.add(obj.toString());
                    }
                    formFieldVO.setValue(newValues);
                    
				if (fieldDefinition.getFieldType().equals(FormComponentType.AUTOCOMPLETE)
						&& fieldDefinition.getEntityName() != null && !fieldDefinition.getEntityName().isEmpty()
						&& object != null && !object.toString().isEmpty()) {
					Object bindObject = entityDao
							.get(EntityId.fromUri(fieldDefinition.getEntityName() + ":" + object.toString()));
					formFieldVO.setItem(bindObject);
				}
            }

        }else{
        	formFieldVO.setValue(fieldDefinition.getValue());
        }
    }

    private void createLovFieldVO(){

	}

    private void setFieldValueForEmail(List<Object> oldValuesList, FormFieldVO formFieldVO) {
    	    			if (CollectionUtils.isNotEmpty(oldValuesList)) {
    	    	            formFieldVO.setEmailInfoVO((EmailInfoVO) oldValuesList.get(0));
    	    	        } else {
    	    	           formFieldVO.setEmailInfoVO(new EmailInfoVO());
    	           }
    	   			
    	    		}
    	   private void setFieldValueForPhone(List<Object> oldValuesList, FormFieldVO formFieldVO,FieldDefinition fieldDefinition) {
    	    		  if ( !oldValuesList.isEmpty()) {
    	    	              formFieldVO.setPhoneNumberVO((PhoneNumberVO) oldValuesList.get(0));
    	    	          } else {
    	    	        	  formFieldVO.setPhoneNumberVO(new PhoneNumberVO());
    	     	         }
    	    	          formFieldVO.setMobile(fieldDefinition.getMobile());
    	    			
    	    		}
    private void setFieldValuesForNonTable(List<FormFieldVO> formFieldVOList, FieldDefinition fieldDefinition,
            FormFieldVO formFieldVO) {
        setFieldValueForTable(fieldDefinition, formFieldVO);

        formFieldVOList.add(formFieldVO);
    }

    private void setFieldValueForTable(FieldDefinition fieldDefinition, FormFieldVO formFieldVO) {
        formFieldVO.setItemLabel(fieldDefinition.getItemLabel());
        formFieldVO.setBinderName(fieldDefinition.getBinderName());
        formFieldVO.setFieldType(fieldDefinition.getFieldType());
        formFieldVO.setMandatoryField(fieldDefinition.isMandatoryField());
		formFieldVO.setExpandableField(fieldDefinition.getExpandableField());
        //     formFieldVO.setHideField(fieldDefinition.isHideField());
        formFieldVO.setIncludeSelect(fieldDefinition.isIncludeSelect());
        formFieldVO.setItemValue(fieldDefinition.getItemValue());
        formFieldVO.setFieldSequence(fieldDefinition.getFieldSequence());
        formFieldVO.setFieldLabel(fieldDefinition.getFieldLabel());
        formFieldVO.setToolTipMessage(fieldDefinition.getToolTipMessage());
        formFieldVO.setEntityName(fieldDefinition.getEntityName());

        formFieldVO.setMinFieldLength(fieldDefinition.getMinFieldLength());
        formFieldVO.setMaxFieldLength(fieldDefinition.getMaxFieldLength());
        formFieldVO.setMinFieldValue(fieldDefinition.getMinFieldValue());
        formFieldVO.setMaxFieldValue(fieldDefinition.getMaxFieldValue());
        // for button and href items
        formFieldVO.setHref(fieldDefinition.getHref());
        formFieldVO.setFunctionLogic(fieldDefinition.getFunctionLogic());
        formFieldVO.setAuthority(fieldDefinition.getAuthority());
        
        // for date field tag

        setDateFieldProperties(fieldDefinition, formFieldVO);

        // For Phone Tag Field
        formFieldVO.setMobile(fieldDefinition.getMobile());

        //For LOV Tag Field
		formFieldVO.setLovKey(fieldDefinition.getLovKey());

        // For AutoComplete Tag Field
        String autoCompleteColumnsHolder = fieldDefinition.getAutoCompleteColumnsHolder();
        if (StringUtils.isNotBlank(autoCompleteColumnsHolder)) {
            String[] columns = autoCompleteColumnsHolder.split(",");
            StringBuilder stringBuilder = new StringBuilder();
            for (String column : columns) {
                stringBuilder.append(column);
                stringBuilder.append(" ");
            }
            formFieldVO.setSearchableColumns(stringBuilder.toString());
        }
        
        // added for autocomplete dependant type
        
        formFieldVO.setErrorMessageCode(fieldDefinition.getErrorMessageCode());
        formFieldVO.setParentColumn(fieldDefinition.getParentColumn());
        formFieldVO.setParentFieldId(fieldDefinition.getParentFieldId());
        formFieldVO.setMainFormDependant(fieldDefinition.isMainFormDependant());

        formFieldVO.setCustomeLongMessage(fieldDefinition.getCustomeLongMessage());

        if (fieldDefinition.getFieldCustomOptionsList() != null && fieldDefinition.getFieldCustomOptionsList().size() > 0) {
            List<FieldCustomOptionsVO> customOptionsList = new ArrayList<FieldCustomOptionsVO>();
            for (FieldCustomOptions fieldCustomOptions : fieldDefinition.getFieldCustomOptionsList()) {
                FieldCustomOptionsVO customOptions = new FieldCustomOptionsVO();
                customOptions.setCustomeItemLabel(fieldCustomOptions.getCustomeItemLabel());
                customOptions.setCustomeItemValue(fieldCustomOptions.getCustomeItemValue());
                customOptionsList.add(customOptions);
            }
            formFieldVO.setFieldCustomOptionsVOList(customOptionsList);
        }
        
        if(fieldDefinition.getFieldType().equals(FormComponentType.CASCADED_SELECT) || fieldDefinition.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
        	if(fieldDefinition.getParentFieldKey() != null)
        		formFieldVO.setParentFieldKey(fieldDefinition.getParentFieldKey());
        	if(fieldDefinition.getActiveChildEntityName() != null)
        		formFieldVO.setCurrentChildEntityName(fieldDefinition.getActiveChildEntityName());
            if(fieldDefinition.getUrlCascadeSelect() != null)
                formFieldVO.setUrlCascadeSelect(fieldDefinition.getUrlCascadeSelect());
        }
    }

    public List<DynamicFormScreenMappingDetail> fetchDynamicFormsMappedToScreenIdAndSourceProduct(Long screenId,Long sourceProductId,String productType) {
		if(productType != null) {
			List<DynamicFormScreenMappingDetail> mappedDynamicFormsListDetail = null;
			Long productTypeValue = null;
			NamedQueryExecutor<ProductType> query = new NamedQueryExecutor<>(
					"getProductTypeByShortNameOrCode");
			query.addParameter("masterId", productType);
			query.addParameter("code", productType);
			ProductType productTypeObj = entityDao.executeQueryForSingleValue(query);
			 if (productTypeObj != null){
				productTypeValue = productTypeObj.getId();
			  mappedDynamicFormsListDetail = formConfigurationMappingService.fetchDynamicFormsMappedToScreenIdAndSourceproductId(screenId, sourceProductId, productTypeValue);
		}
			if(CollectionUtils.isEmpty(mappedDynamicFormsListDetail)){
				return formConfigurationMappingService.fetchDynamicFormsMappedToScreenIdAndSourceproductId(screenId,sourceProductId);
			}
			else{
				return mappedDynamicFormsListDetail;
			}
		}
		else{
			return formConfigurationMappingService.fetchDynamicFormsMappedToScreenIdAndSourceproductId(screenId,sourceProductId);
		}

	}

	public List<DynamicFormScreenMappingDetail> fetchAllDynamicFormsMappedToScreenIdAndSourceProduct(Long screenId,Long sourceProductId) {

			return formConfigurationMappingService.fetchAllDynamicFormsMappedToScreenIdAndSourceproduct(screenId,sourceProductId);


	}


    private Boolean checkIfFormIsAvailableInViewMode(String formName,
			Long screenId,Long sourceProductId) {
    	Boolean isViewModeEnabled=Boolean.FALSE;
    	List<DynamicFormScreenMappingDetail> mappedDynamicFormsList = fetchDynamicFormsMappedToScreenIdAndSourceProduct(screenId,sourceProductId,null);
    	if(hasElements(mappedDynamicFormsList)){
    		for(DynamicFormScreenMappingDetail dynScreenMapping:mappedDynamicFormsList){
    				if(notNull(dynScreenMapping.getFormConfigurationMapping())
    						&& notNull(dynScreenMapping.getFormConfigurationMapping().getUiMetaData())
    						&& notNull(dynScreenMapping.getFormConfigurationMapping().getUiMetaData().getFormName())
    						&& notNull(dynScreenMapping.getFormConfigurationMapping().getUiMetaData().getFormName().equals(formName))){
    					isViewModeEnabled=!dynScreenMapping.getEditModeEnabled();
    					break;
    				}
    		}
    	}
    	return isViewModeEnabled;
    }

	public UIMetaDataVo prepareUIMetaDataVoForWorkFlowBased(String uri,String formName,Long sourceProductId,String viewMode,String dynamiCFormData) {
		UIMetaDataVo uiMetaDataVo=null;
			if(!isEmpty(uri) && !uri.equals("null") && !isEmpty(formName)){
				uiMetaDataVo=prepareUIMetaDataVoBasedOnUriAndFormName(uri,null,formName,true,sourceProductId,viewMode,dynamiCFormData,null);
			}
			if(isNull(uiMetaDataVo)){
				uiMetaDataVo=prepareUIMetaDataVoBasedOnFormName(formName,sourceProductId);
			}
			return uiMetaDataVo;

	}

	public UIMetaDataVo prepareUIMetaDataVoForWorkFlowBased(String uri, EntityId entityId, String formName,
			Long sourceProductId, String viewMode, String dynamiCFormData) {
		UIMetaDataVo uiMetaDataVo = null;
		if (!isEmpty(uri) && !uri.equals("null") && !isEmpty(formName)) {
			uiMetaDataVo = prepareUIMetaDataVoBasedOnUriAndFormName(uri, entityId, null, formName, true,
					sourceProductId, viewMode, dynamiCFormData);
		}
		if (isNull(uiMetaDataVo)) {
			uiMetaDataVo = prepareUIMetaDataVoBasedOnFormName(formName, sourceProductId);
		}
		return uiMetaDataVo;

	}
	
	private UIMetaDataVo prepareUIMetaDataVoBasedOnFormName(String formName,Long sourceProductId) {

		UIMetaDataVo uiMetaDataVo=null;
	      List<UIMetaData> uIMetaDatas = formService.getFormByNameAndSourceProduct(formName,sourceProductId);
		if (hasElements(uIMetaDatas)) {
			uiMetaDataVo = mergeFormDetailsAndData(
					uIMetaDatas.get(0), null);
		}
		return uiMetaDataVo;

	}

	@Transactional(readOnly = true)
	public List<Map<String,String>> fetchDynamicFormNamesMappedToScreenId(Long screenId,Long sourceProductId,String productType){
		List<Map<String,String>> multipleDynamicFormDetails=new ArrayList<Map<String,String>>();
		List<DynamicFormScreenMappingDetail> mappedDynamicFormsList = fetchDynamicFormsMappedToScreenIdAndSourceProduct(screenId,sourceProductId,productType);
		 if(hasElements(mappedDynamicFormsList)){
				for(DynamicFormScreenMappingDetail dyn:mappedDynamicFormsList){
					DynamicFormScreenMapping dynamicFormScreenMapping = entityDao.find(DynamicFormScreenMapping.class,
							dyn.getDynamicFormMappingId());
					if (dynamicFormScreenMapping != null && !dynamicFormScreenMapping.isActiveFlag()
							&& dynamicFormScreenMapping.getApprovalStatus() == ApprovalStatus.DELETED_APPROVED_IN_HISTORY) {
						continue;
					}
					Map<String,String> dynamicFormDetailMap = new HashMap<String, String>();
					dynamicFormDetailMap.put("formName", dyn.getFormConfigurationMapping().getUiMetaData().getFormName());
					dynamicFormDetailMap.put("isViewModeEnabled", dyn.getEditModeEnabled().equals(true)?"false":"true");
					multipleDynamicFormDetails.add(dynamicFormDetailMap);
				}
			}
		 return multipleDynamicFormDetails;
	}
	
	@Transactional(readOnly = true)
	public List<Map<String,String>> fetchDynamicFormNamesMappedToScreenId(Long screenId,Long sourceProductId){
		List<Map<String,String>> multipleDynamicFormDetails=new ArrayList<Map<String,String>>();
		List<DynamicFormScreenMappingDetail> mappedDynamicFormsList = fetchAllDynamicFormsMappedToScreenIdAndSourceProduct(screenId,sourceProductId);
		 if(hasElements(mappedDynamicFormsList)){
				for(DynamicFormScreenMappingDetail dyn:mappedDynamicFormsList){
					DynamicFormScreenMapping dynamicFormScreenMapping = entityDao.find(DynamicFormScreenMapping.class,
							dyn.getDynamicFormMappingId());
					if (dynamicFormScreenMapping != null && !dynamicFormScreenMapping.isActiveFlag()
							&& dynamicFormScreenMapping.getApprovalStatus() == ApprovalStatus.DELETED_APPROVED_IN_HISTORY) {
						continue;
					}
					Map<String,String> dynamicFormDetailMap = new HashMap<String, String>();
					dynamicFormDetailMap.put("formName", dyn.getFormConfigurationMapping().getUiMetaData().getFormName());
					dynamicFormDetailMap.put("isViewModeEnabled", dyn.getEditModeEnabled().equals(true)?"false":"true");
					multipleDynamicFormDetails.add(dynamicFormDetailMap);
				}
			}
		 return multipleDynamicFormDetails;
	}

	@Transactional(readOnly = true)
	public List<Map<String,String>> fetchDynamicFormNamesMappedToEnityByUri(String uri,Long placheHolderId){
		List<Map<String,String>> multipleDynamicFormDetails=new ArrayList<Map<String,String>>();
		Object entity = entityDao.get(EntityId.fromUri(uri));
		List<DynamicForm> dynamicFormsAttachedToEntity = new ArrayList<DynamicForm>();
		if(entity instanceof MultipleForm){
			MultipleForm multiDynamicFormEntity = (MultipleForm)entity;
			if(ValidatorUtils.notNull(multiDynamicFormEntity)){
				dynamicFormsAttachedToEntity.addAll(multiDynamicFormEntity.getAllDynamicForms());
			}
		}else  if(entity instanceof SingleDynamicForm){
			SingleDynamicForm singleDynamicFormEntity = (SingleDynamicForm)entity;
			if(ValidatorUtils.notNull(singleDynamicFormEntity)){
				dynamicFormsAttachedToEntity.add(singleDynamicFormEntity.getDynamicForm());
			}			
		}

		for(DynamicForm dynamicForm : dynamicFormsAttachedToEntity){
			if(ValidatorUtils.isNull(dynamicForm) 
					|| ValidatorUtils.isNull(dynamicForm.getModelMetaDataId()) 
					|| ValidatorUtils.isNull(dynamicForm.getUiMetaDataId())){
				continue;
			}else if(placheHolderId.equals(dynamicForm.getPlaceholderId())){
				Map<String,String> dynamicFormDetailMap = new HashMap<String, String>();
				dynamicFormDetailMap.put("formName", dynamicForm.getUiMetaData().getFormName());
				dynamicFormDetailMap.put("isViewModeEnabled", "false");
				multipleDynamicFormDetails.add(dynamicFormDetailMap);
			}
		}
		 return multipleDynamicFormDetails;
	}

    public Map<Long, FormConfigurationMapping> prepareConfigIdMapForSelectedForms(
            DynamicFormScreenMapping dynamicFormScreenMapping) {
        Map<Long,FormConfigurationMapping> formConfigIdSelectedMap=new HashMap<Long, FormConfigurationMapping>();
        for(DynamicFormScreenMappingDetail dynamicFormDtl:dynamicFormScreenMapping.getDynamicFormScreenDtlList()){
            if(notNull(dynamicFormDtl.getFormConfigValue())){
                formConfigIdSelectedMap.put(dynamicFormDtl.getFormConfigValue(), dynamicFormDtl.getFormConfigurationMapping());
            }
        }
        return formConfigIdSelectedMap;
    }

    public String getDynamicFormJSONRepresentation(MultipleFormData mulFormDataObj) throws SystemException {
        Map<String, PersistentFormData> modelNameAndPersistentFormDataMap=mulFormDataObj.getFormDataMap();
        Map<String,Map<String,Object>> modelNameDynamicFormDataMap=getDynamicFormDataObjectRepresentationForPersistentFormData(modelNameAndPersistentFormDataMap);
        return prepareJSONStringFromObject(modelNameDynamicFormDataMap);
    }
   /* public String getDynamicFormJSONRepresentation(SingleDynamicForm singleDynamicForm) throws SystemException {
    	    	String jsonString=null;
    	    	Map<String,Map<String,Object>> modelNameDynamicFormDataMap=new HashMap<String, Map<String,Object>>();
    	    	DynamicForm dynamicFormObj=singleDynamicForm.getDynamicForm();
    	    	dynamicFormObj.getUiMetaData().getModelName();
    	    	if(notNull(dynamicFormObj)){
    	    		jsonString=dynamicFormObj.getDataJsonString();
    	    	}
    	    	modelNameDynamicFormDataMap.put(dynamicFormObj.getUiMetaData().getModelName(), DynamicForm.transFormJSONAsMapAsperMetaData(jsonString,dynamicFormObj.getModelMetaData() ,true));
    	    	dynamicFormObj.getDataJsonString();
            
    	        
    	        return prepareJSONStringFromObject(modelNameDynamicFormDataMap);
    }*/
    
    /*
     * Transform dynamicObject form to a map <modelName : Map created from dynamic form json>
     */
	public Map<String, Map<String, Object>> getDynamicFormDataObjectRepresentation(
			Map<String, DynamicForm> formNameDynamicFormDataMap) {

		Map<String, Map<String, Object>> modelNameDynamicFormDataMap = new HashMap<String, Map<String, Object>>();
		if (isNotEmpty(formNameDynamicFormDataMap)) {
			for (Map.Entry<String, DynamicForm> entry : formNameDynamicFormDataMap.entrySet()) {
				DynamicForm dynamicFormObj = entry.getValue();
				ModelMetaData modelMetaDataObj = dynamicFormObj.getModelMetaData();
				if (notNull(modelMetaDataObj)) {
					modelNameDynamicFormDataMap.put(modelMetaDataObj.getName().replaceAll(" ", "_"),
							DynamicForm.transFormJSONAsMapAsperMetaData(dynamicFormObj.getDataJsonString(),
									modelMetaDataObj, true));
				}
			}
		}
		return modelNameDynamicFormDataMap;
	}
	
	
	public Map<String, Map<String, Object>> getDynamicFormDataObjectRepresentationForPersistentFormData(
			Map<String, PersistentFormData> modelNameAndPersistentFormDataMap) {

	        Map<String,Map<String,Object>> modelNameDynamicFormDataMap=new HashMap<String, Map<String,Object>>();
	        if(isNotEmpty(modelNameAndPersistentFormDataMap)){
	                for (Map.Entry<String, PersistentFormData> entry : modelNameAndPersistentFormDataMap.entrySet()) {
	                    String modelName = entry.getKey();
	                    PersistentFormData persistentFormValue = entry.getValue();
	                    modelNameDynamicFormDataMap.put(modelName, DynamicForm.transFormJSONAsMapAsperMetaData(persistentFormValue.getFieldValueData(),persistentFormValue.getModelMetaData() ,true));
	                }
	        }

	        return modelNameDynamicFormDataMap;
	}
	
	
	
    
    public String getDynamicFormJSONRepresentation(MultipleForm multipleFormObj){
    	    	Map<String,DynamicForm> formNameDynamicFormDataMap=multipleFormObj.getAllDynamicFormAsMap();
    	        Map<String,Map<String,Object>> modelNameDynamicFormDataMap=getDynamicFormDataObjectRepresentation(formNameDynamicFormDataMap);
    	     return prepareJSONStringFromObject(modelNameDynamicFormDataMap);
    	    	 
    } 
    public Map<String, String> getDynamicFormJSONRepresentation(List<IDynamicForm> dynamicFormsList,String serviceIdentifierCode,String... fields){
    	
    	
    	
    	Map<String, String> fieldDataMap=new HashMap<String, String>();
    	for(String field:fields)
    	{
    		String jsonData=getJsonForServiceField(dynamicFormsList, serviceIdentifierCode,  field);
    		fieldDataMap.put(field, jsonData);
    	}
    	return fieldDataMap;
    }   
    
    /*
     * Collect Dynamic form data from multiple entities into a map
     */
    public Map<String, Map<String, Object>> prepareDynamicFormMapData(List<IDynamicForm> dynamicFormsList)
    {
    	
    	Map<String, Map<String, Object>> dynamicFormDataMap=new HashMap<String, Map<String,Object>>();
    	for(IDynamicForm dynamicForm:dynamicFormsList)
    	{
    		dynamicFormDataMap.putAll(getJsonDataForDynamicForm(dynamicForm));
    
    	}    	
    	return dynamicFormDataMap;
    }
    
    
    public String getJsonForServiceField(List<IDynamicForm> dynamicFormsList,String serviceIdentifierCode,String  field)
    {
    	String jsonData=null;
    	if(dynamicFormsList==null)
    	{
    		return null;
    	}
    	//Map<String, JSONObject> dynamicFormJsonMap=new HashMap<String, JSONObject>();
    	Map<String, Map<String, Object>> dynamicFormDataMap=new HashMap<String, Map<String,Object>>();
    	for(IDynamicForm dynamicForm:dynamicFormsList)
    	{
    		dynamicFormDataMap.putAll(getJsonDataForDynamicForm(dynamicForm));
    		//createMapFromJsonData(dynamicFormJsonMap,jsonString);
    	}
    	
    	//jsonData=filterJsonDataAsPerFilter(dynamicFormDataMap,"",filterId);
    	return jsonData;
    }
    private void createMapFromJsonData(Map<String, JSONObject> dynamicFormJsonMap,String jsonString) {
		
    	try {
			JSONObject jsonObject=new JSONObject(jsonString);
			Iterator<String> keys=jsonObject.keys();
			while(keys.hasNext())
			{
				String key=keys.next();
				JSONObject formData=(JSONObject)jsonObject.get(key);
				dynamicFormJsonMap.put(key, formData);
				
			}
		
		} catch (JSONException e) {
			BaseLoggers.exceptionLogger.error(e.getMessage(), e);
		}
		
	}

    /*
     * Transform dynamicObject form to a map <modelName : Map created from dynamic form json>
     */
	private Map<String, Map<String, Object>> getJsonDataForDynamicForm(IDynamicForm dynamicForm) {
		
    	Map<String, Map<String, Object>> dynamicFormDataMap=new HashMap<String, Map<String,Object>>();
    	Map<String, DynamicForm> dynamicFormMap=null;
    	if(dynamicForm instanceof MultipleForm)
    	{
    		dynamicFormMap=((MultipleForm)dynamicForm).getAllDynamicFormAsMap();
    		return getDynamicFormDataObjectRepresentation(dynamicFormMap);
    	}
    	else if(dynamicForm instanceof SingleDynamicForm)
    	{
    		DynamicForm dynamicFormData=((SingleDynamicForm)dynamicForm).getDynamicForm();
    		dynamicFormMap=new HashMap<String, DynamicForm>();
    		if(dynamicFormData!=null)
    		{
    			dynamicFormMap.put(dynamicFormData.getModelMetaData().getName(), dynamicFormData);
    		}
    		return getDynamicFormDataObjectRepresentation(dynamicFormMap);
    		
    	}
    	else if(dynamicForm instanceof MultipleFormData)
    	{
    		Map<String, PersistentFormData> modelNameAndPersistentFormDataMap=((MultipleFormData)dynamicForm).getFormDataMap();
    		return getDynamicFormDataObjectRepresentationForPersistentFormData(
    				modelNameAndPersistentFormDataMap);
    	}
    	
    	return dynamicFormDataMap;
    
	}

	

	

	public Map<String,Map<String,Object>> convertDynamicFormRepresentation(String jsonString) throws SystemException{
        return parseJSONStringToPersistentFormDataMap(jsonString);

    }

    public void updateDynamicForms(String dynamicFormJsonString,MultipleFormData multipleFormData){
        Map<String,Map<String,Object>> modelNameDynamicFormDataMap=convertDynamicFormRepresentation(dynamicFormJsonString);
        if(isNotEmpty(modelNameDynamicFormDataMap)){
            for (Map.Entry<String,Map<String,Object>> entry : modelNameDynamicFormDataMap.entrySet()) {
                String modelName = entry.getKey();
                Map<String,Object> dynamicFormValue = entry.getValue();
                updateMultipleFormDataObj(modelName,dynamicFormValue,multipleFormData);
            }
        }
    }
    public void updateDynamicForms(String dynamicFormJsonString,MultipleForm multipleFormObj){
    	       Map<String,Map<String,Object>> modelNameDynamicFormDataMap=convertDynamicFormRepresentation(dynamicFormJsonString);
    	        if(isNotEmpty(modelNameDynamicFormDataMap)){
    	            for (Map.Entry<String,Map<String,Object>> entry : modelNameDynamicFormDataMap.entrySet()) {
    	                String modelName = entry.getKey();
    	                Map<String,Object> dynamicFormValue = entry.getValue(); 
    	                updateMultipleFormObj(modelName,dynamicFormValue,multipleFormObj);                
    	            }
    	        }
    }
    
    public void updateDynamicForms(String dynamicFormJsonString,SingleDynamicForm singleDynamicForm){
	       Map<String,Map<String,Object>> modelNameDynamicFormDataMap=convertDynamicFormRepresentation(dynamicFormJsonString);
	        if(isNotEmpty(modelNameDynamicFormDataMap)){
	            for (Map.Entry<String,Map<String,Object>> entry : modelNameDynamicFormDataMap.entrySet()) {
	                String modelName = entry.getKey();
	                Map<String,Object> dynamicFormValue = entry.getValue(); 
	                updateSingleDynamicFormObj(modelName,dynamicFormValue,singleDynamicForm);                
	            }
	        }
}
    
    private void updateSingleDynamicFormObj(String modelName,
            Map<String, Object> dynamicFormValue,
           SingleDynamicForm singleDynamicForm) {
    	DynamicForm dynamicFormObj=new DynamicForm();
    	   ModelMetaData modelMetaData = formService.getModelByModelName(modelName);
    	   if(notNull(modelMetaData)){
    		   dynamicFormObj.setModelMetaDataId(modelMetaData.getId());
    		   UIMetaData uiMetaDataObj=formService.getFormByModelUri(modelMetaData.getUri());
               if(notNull(uiMetaDataObj)){
            	   dynamicFormObj.setUiMetaDataId(uiMetaDataObj.getId());
               }               
               //dynamicFormObj.setDataMapWithActualType(dynamicFormValue);
               Map<String, Object> jsonMap = jsonMapConverter.saveDynamicFormDataMap(dynamicFormValue, modelMetaData);
               JSONSerializer serializer = new JSONSerializer();
               String json = serializer.transform(money_transformer, Money.class).exclude("*.class").deepSerialize(jsonMap);
               dynamicFormObj.setDataJsonString(json);
               singleDynamicForm.setDynamicForm(dynamicFormObj);
    	   }
    	   
}
    
    private void updateMultipleFormObj(String modelName,
    		            Map<String, Object> dynamicFormValue,
    		           MultipleForm multipleFormObj) {
    		    	DynamicForm dynamicFormObj=new DynamicForm();
    		    	   ModelMetaData modelMetaData = formService.getModelByModelName(modelName);
    		    	   if(notNull(modelMetaData)){
    		    		   dynamicFormObj.setModelMetaDataId(modelMetaData.getId());
    		    		   UIMetaData uiMetaDataObj=formService.getFormByModelUri(modelMetaData.getUri());
    		               if(notNull(uiMetaDataObj)){
    		            	   dynamicFormObj.setUiMetaDataId(uiMetaDataObj.getId());
    		               }               
    		               //dynamicFormObj.setDataMapWithActualType(dynamicFormValue);
    		               Map<String, Object> jsonMap = jsonMapConverter.saveDynamicFormDataMap(dynamicFormValue, modelMetaData);
    		               JSONSerializer serializer = new JSONSerializer();
    		               String json = serializer.transform(money_transformer, Money.class).exclude("*.class").deepSerialize(jsonMap);
    		               dynamicFormObj.setDataJsonString(json);
    		               multipleFormObj.addMultiDynamicForm(dynamicFormObj);
    		    	   }
    		    	   
    }
    
    private void updateMultipleFormDataObj(String modelName,
            Map<String, Object> dynamicFormValue,
            MultipleFormData multipleFormData) {
        PersistentFormData persistentFormData=new PersistentFormData();
        persistentFormData.setFieldValuePariring(dynamicFormValue);
        ModelMetaData modelMetaData = formService.getModelByModelName(modelName);
        persistentFormData.setModelMetaData(modelMetaData);
        UIMetaData uiMetaDataObj=formService.getFormByModelUri(modelMetaData.getUri());
         if(notNull(uiMetaDataObj)){
             persistentFormData.setFormUri(uiMetaDataObj.getUri());
         }

        Map<String, Object> jsonMap = jsonMapConverter.prepareDynamicFormDataJsonMap(persistentFormData);
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.transform(money_transformer, Money.class).exclude("*.class").deepSerialize(jsonMap);
        persistentFormData.setFieldValueData(json);
        multipleFormData.addToFormDataMap(modelName, persistentFormData);

    }

    private Map<String,Map<String,Object>> parseJSONStringToPersistentFormDataMap(String jsonString){

        /*
         * Convert json string for persistentFormData Map
         */
        Map<String,Map<String,Object>> dynamicFormDataMap = new HashMap<String,Map<String,Object>>();
        ObjectMapper mapper = new ObjectMapper();
        if (StringUtils.isNotBlank(jsonString)) {
                try {
                    dynamicFormDataMap = mapper.readValue(jsonString,
                            new TypeReference< Map<String,Map<String,Object>>>() {
                            });
                } catch (JsonParseException e) {
                    throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_CONVERT_JSON_TO_MAP,UNABLE_TO_CONVERT_JSON_TO_MAP).setOriginalException(e)
                    .setMessage(UNABLE_TO_CONVERT_JSON_TO_MAP).build();
                } catch (JsonMappingException e) {
                    throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_CONVERT_JSON_TO_MAP,UNABLE_TO_CONVERT_JSON_TO_MAP).setOriginalException(e)
                    .setMessage(UNABLE_TO_CONVERT_JSON_TO_MAP).build();
                } catch (IOException e) {
                    throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_CONVERT_JSON_TO_MAP,UNABLE_TO_CONVERT_JSON_TO_MAP).setOriginalException(e)
                    .setMessage(UNABLE_TO_CONVERT_JSON_TO_MAP).build();
                }

        }
        return dynamicFormDataMap;
    }
    
    
    public  Object parseJSONStringToType(String jsonString, TypeReference typeReference){

        /*
         * Convert json string for persistentFormData Map
         */
        if(StringUtils.isBlank(jsonString))
        {
        	return null;
        }
        
        	ObjectMapper mapper = new ObjectMapper();
                try {
                    return (Object)mapper.readValue(jsonString,typeReference);
                } catch (JsonParseException e) {
                    throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_CONVERT_JSON_TO_MAP,UNABLE_TO_CONVERT_JSON_TO_MAP).setOriginalException(e)
                    .setMessage(UNABLE_TO_CONVERT_JSON_TO_MAP).build();
                } catch (JsonMappingException e) {
                    throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_CONVERT_JSON_TO_MAP,UNABLE_TO_CONVERT_JSON_TO_MAP).setOriginalException(e)
                    .setMessage(UNABLE_TO_CONVERT_JSON_TO_MAP).build();
                } catch (IOException e) {
                    throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_CONVERT_JSON_TO_MAP,UNABLE_TO_CONVERT_JSON_TO_MAP).setOriginalException(e)
                    .setMessage(UNABLE_TO_CONVERT_JSON_TO_MAP).build();
                }

   }

    protected String prepareJSONStringFromObject(Object object){
        String jsOnString = null;
        ObjectMapper mapper = getJacksonObjectMapper();
        try {
            jsOnString = mapper.writeValueAsString(object);
        } catch (JsonGenerationException ex) {
            throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_CONVERT_JSON_STRING,UNABLE_TO_CONVERT_JSON_STRING).setOriginalException(ex)
            .setMessage(UNABLE_TO_CONVERT_JSON_STRING).build();
        } catch (JsonMappingException ex) {
            throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_CONVERT_JSON_STRING,UNABLE_TO_CONVERT_JSON_STRING).setOriginalException(ex)
            .setMessage(UNABLE_TO_CONVERT_JSON_STRING).build();
        } catch (IOException ex) {
            throw ExceptionBuilder.getInstance(SystemException.class,UNABLE_TO_CONVERT_JSON_STRING,UNABLE_TO_CONVERT_JSON_STRING).setOriginalException(ex)
            .setMessage(UNABLE_TO_CONVERT_JSON_STRING).build();
        }
        return jsOnString;
      }

    /**
     * This method performs Shallow serialization of object to json 
     * @param object
     * @return
     */
    protected String parseObjectToJsonShallow(Object object){

    	return outgointObjectSerializer.deepSerialize(object);            
     }


    
    private ObjectMapper getJacksonObjectMapper() {
        return  new ObjectMapper();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public DynamicFormData deserializeMultipleDynamicFormDataAsPlaceholderMap(String dynamicFormDataString){

    	
    	Map<String,List<UIMetaDataVo>> dynamicForms = new HashMap<String, List<UIMetaDataVo>>();
		if(StringUtils.isNotEmpty(dynamicFormDataString)){
			Map<String, UIMetaDataVo> formDataMap = (HashMap<String,UIMetaDataVo>) new JSONDeserializer().deserialize(dynamicFormDataString);
			
			for(Entry<String, UIMetaDataVo> formDataMapForPlaceholder : formDataMap.entrySet()){
				
				ObjectMapper mapper = new ObjectMapper();
				String formDataAsJsonString=null;
				try {
					formDataAsJsonString = mapper.writeValueAsString(formDataMapForPlaceholder.getValue());
				} catch (Exception e) {
					BaseLoggers.exceptionLogger.error("Exception:" + e.getMessage(), e);
				}
				JSONDeserializer<UIMetaDataVo> iSerializer = new JSONDeserializer<UIMetaDataVo>();
				UIMetaDataVo formdata = iSerializer.deserialize(formDataAsJsonString,UIMetaDataVo.class);
				
				String placeHolderString = formDataMapForPlaceholder.getKey().split("#")[0];
				
				if(ValidatorUtils.isNull(dynamicForms.get(placeHolderString))){
					List<UIMetaDataVo> listOfFormsForPlaceholder = new ArrayList<UIMetaDataVo>();
					listOfFormsForPlaceholder.add(formdata);
					dynamicForms.put(placeHolderString, listOfFormsForPlaceholder);
				}else{
					dynamicForms.get(placeHolderString).add(formdata);
				}	   
			}
		}
		
		return new DynamicFormData(dynamicForms);	
    	
    }
    
    public UIMetaDataVo deserializeSingleDynamicFormData(String dynamicFormDataString){
    	
    	List<UIMetaDataVo> dynamicForms=deserializeMultipleDynamicFormDataAsList(dynamicFormDataString);
    	if(ValidatorUtils.hasElements(dynamicForms))
    	{
    		return dynamicForms.get(0);
    	}
    	return null;
    	
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List<UIMetaDataVo> deserializeMultipleDynamicFormDataAsList(String dynamicFormDataString){
    	
    	List<UIMetaDataVo> dynamicForms = new ArrayList<UIMetaDataVo>();
		if(StringUtils.isNotEmpty(dynamicFormDataString)){
			Map<String, UIMetaDataVo> formDataMap = (HashMap<String,UIMetaDataVo>) new JSONDeserializer().deserialize(dynamicFormDataString);
			
			for(Entry<String, UIMetaDataVo> formDataMapForPlaceholder : formDataMap.entrySet()){
				
				ObjectMapper mapper = new ObjectMapper();
				String formDataAsJsonString=null;
				try {
					formDataAsJsonString = mapper.writeValueAsString(formDataMapForPlaceholder.getValue());
				} catch (Exception e) {
					BaseLoggers.exceptionLogger.error("Exception:" + e.getMessage(), e);
				}
				JSONDeserializer<UIMetaDataVo> iSerializer = new JSONDeserializer<UIMetaDataVo>();
				UIMetaDataVo formdata = iSerializer.deserialize(formDataAsJsonString,UIMetaDataVo.class);
				
				dynamicForms.add(formdata)	   ;
			}
		}
		
		return dynamicForms;
    	
    }
    private String filterJsonDataAsPerFilter(Map<String, JSONObject> dynamicFormJsonMap , String serviceIdentifier,Long filterId) {
		DynamicFormFilter dynamicFormFilter=formService.getFilterByServiceIdentifier(filterId);
		String filterDefinitionStr=dynamicFormFilter.getFilterFieldsJsonMap();
		String filterDataJson=null;
		//put underscors in place of space
		try {
			JSONObject filterDefinition=new JSONObject(filterDefinitionStr);
			Iterator<String> keys=filterDefinition.keys();
			JSONObject filterData=new JSONObject();
			while(keys.hasNext())
			{
				String formName=keys.next();
				JSONObject capturedFormData=dynamicFormJsonMap.get("Model"+formName);
				JSONObject formMetaObj=(JSONObject)filterDefinition.get(formName);
				if(capturedFormData==null)
				{
					continue;
				}
				//populateFilterDataForForm(formName,filterData,formMetaObj,capturedFormData);
			}
			filterDataJson=filterData.toString();
			
			
		} catch (JSONException e) {

		}
		
		return filterDataJson;
	}
	public String getFilteredJsonData(String serviceFieldName,Map<String, Map<String, Object>> dynamicFormDataMap,
			DynamicFormFilter dynamicFormFilter,Map<String, JSONObject> filterDefCache) throws JSONException {

		JSONObject filterDefinition=getFilterDefinition(dynamicFormFilter,filterDefCache);
		Map<String, Object> filterData=new HashMap<String, Object>();
		for(Map.Entry<String, Map<String, Object>> entry:dynamicFormDataMap.entrySet())
		{
			String formModelName=entry.getKey();
			JSONObject formMetaObj=null;
			String formName=formModelName.replace("Model", "");
			if(filterDefinition!=null )
			{
				formMetaObj=(JSONObject)filterDefinition.get(formModelName.replace("Model", ""));
			}
			Map<String, Object> capturedFormData=dynamicFormDataMap.get(formModelName);
			if(capturedFormData==null)
			{
				continue;
			}
			populateFilterDataForForm(formName,filterData,formMetaObj,capturedFormData);
		}
		Map<String, Object> filteredDataMap=new HashMap<String, Object>();
		filteredDataMap.put(serviceFieldName, filterData);
	return parseObjectToJsonShallow(filteredDataMap);
}
	
	//formMetaObj will be null in case of filter not attached to field
private void populateFilterDataForForm(String formName,Map<String, Object> filterData, JSONObject formMetaObj, Map<String, Object> capturedFormData) throws JSONException {
	
	Map<String, String> fieldKeyMap=null;
	if(formMetaObj!=null)
	{
		JSONArray formFields=(JSONArray)formMetaObj.get(DynamicFormFilter.FORM_FIELDS);
		if(formFields==null)
		{
			return;
		}
		fieldKeyMap=new HashMap<String, String>();
		for(int i=0;i<formFields.length();i++)
		{
			String field=formFields.getString(i);
			fieldKeyMap.put(field,field);
		}
	}
	filterAndAddData(formName,filterData,fieldKeyMap,capturedFormData);
}

//fieldKeyMap will be null if filter Not attached
private void filterAndAddData(String formName,Map<String, Object> filterData, Map<String, String> fieldKeyMap,  Map<String, Object> capturedFormData) throws JSONException {
	
	Set<String>  mappedFields=null;
	Map<String, String> formKeysMap;
	/*
	 * Filter is not attached so capture all fields data
	 */
	if(fieldKeyMap==null)
	{
		mappedFields=capturedFormData.keySet();
		formKeysMap=new HashMap<>();
		capturedFormData.forEach((mapKey,mapValue)->{
			formKeysMap.put(mapKey, mapKey);
		});
	}
	else
	{
		mappedFields=fieldKeyMap.keySet();
		formKeysMap=fieldKeyMap;
	}
	for(String key:mappedFields)
	{
		if(capturedFormData.containsKey(key))
		{
			Object fieldData=capturedFormData.get(key);
			addFieldData(formName,key,fieldData,filterData,formKeysMap);
			
		}
		
	}
	
}

	private void addFieldData(String formName, String key, Object fieldData, Map<String, Object> filterData,
			Map<String, String> fieldKeyMap) throws JSONException {
	
	/* when element type is Panel */
	if(fieldData instanceof Map)
	{
		 List<String> panelKeys=(List<String>) filterData.get(DynamicFormFilter.PANEL_KEY_LIST);
		 if(panelKeys==null)
		 {
			 panelKeys=new ArrayList<String>();
			 filterData.put(DynamicFormFilter.PANEL_KEY_LIST, panelKeys);
		 }
		 filterData.put(key, filterAndAddMapFields(formName,   fieldData, filterData, fieldKeyMap));
		 panelKeys.add(key);
		return;
	}
	if(fieldData instanceof List)
	{
		List<Object> fieldObjectList=(List<Object>)fieldData;
		List<Object> objectList=new ArrayList<Object>();
		boolean isTable=false;
		for(Object object:fieldObjectList)
		{
			 /* When element type is Table */
			 if(object instanceof Map)
			 {
				  
				  object=filterAndAddMapFields(formName,   object, filterData, fieldKeyMap);
				  isTable=true;
			 }
			 
			objectList.add(object);
		}
		 
		 if(isTable)
		 {
			 List<String> tableKeys=(List<String>) filterData.get(DynamicFormFilter.TABLE_KEY_LIST);
			 if(tableKeys==null)
			 {
				 tableKeys=new ArrayList<String>();
				 filterData.put(DynamicFormFilter.TABLE_KEY_LIST, tableKeys);
			 }
			 tableKeys.add(key);
		 }
		filterData.put(key, objectList);
		return;
	}
	filterData.put(key, fieldData);
			
}

	private Object filterAndAddMapFields(String formName, Object fieldData, Map<String, Object> filterData,
			Map<String, String> fieldKeyMap) throws JSONException {

	Map<String, Object> fieldDataJson=(Map<String, Object>) fieldData;
	Map<String, Object> jsonFieldMap=new HashMap<String, Object>();
	for(Map.Entry<String, Object> entry:fieldDataJson.entrySet())
	{
		String fKey=entry.getKey();
		
		if(fieldKeyMap.containsKey(fKey))
		{
			jsonFieldMap.put(fKey, fieldDataJson.get(fKey));
		}
	}
	return jsonFieldMap;
}
	private JSONObject getFilterDefinition(DynamicFormFilter dynamicFormFilter, Map<String, JSONObject> filterDefCache) throws JSONException {
		
		if(dynamicFormFilter==null)
		{
			return null;
		}
		if(filterDefCache.containsKey(dynamicFormFilter.getName()))
		{
			return filterDefCache.get(dynamicFormFilter.getName());
		}

		String filterDefinitionStr=dynamicFormFilter.getFilterFieldsJsonMap();
		String filterDataJson=null;
		JSONObject filterDefinition=new JSONObject(filterDefinitionStr);
		filterDefCache.put(dynamicFormFilter.getName(), filterDefinition);
		return filterDefinition;
	}
	
	public void prepareDefaultValuesMapForForm(FormFieldVO formFieldVO, String formName, Map<String,List<String>> defaultValuesMap){
		defaultValuesMap.put(formFieldVO.getId()+"_"+formFieldVO.getFieldType(), formFieldVO.getValue());
	}

	public void mergeDataAndUpdateDynamicForm(DynamicForm dynamicFormObj, String placeholder, UIMetaData uiMetaData, ModelMetaData modelMetaData,
			Set<String> allowedKeys, Map<String, Object> mergedFormData,boolean doValidate) {
		
 		   dynamicFormObj.setModelMetaDataId(modelMetaData.getId());
           dynamicFormObj.setUiMetaDataId(uiMetaData.getId());
           String jsonDataStr=dynamicFormObj.getDataJsonString();
           Map<String, Object> jsonMap= (Map<String, Object>) parseJSONStringToType(jsonDataStr,new TypeReference< Map<String,Object>>() {
           });
           if(jsonMap==null)
           {
        	   jsonMap=new HashMap<String, Object>();
        	   
           }
           jsonMapConverter.mergeDynamicFormDataMap(jsonMap, mergedFormData,uiMetaData, modelMetaData, allowedKeys,doValidate);
           JSONSerializer serializer = new JSONSerializer();
           String json = serializer.transform(money_transformer, Money.class).exclude("*.class").deepSerialize(jsonMap);
           dynamicFormObj.setDataJsonString(json);
           
		
	}
	
	private String setDefDateAsPerUserFormat(String dt){
		String dateString=null;
		Date date=null;
		try {
			date = CoreDateUtility.getDateFromString(dt, "MM/dd/yyyy");
		} catch (Exception e) {
			throw ExceptionBuilder.getInstance(BaseException.class,UNABLE_TO_CONVERT_VALUE_TO_DATE_OBJECT,UNABLE_TO_CONVERT_VALUE_TO_DATE_OBJECT).setOriginalException(e)
            .setMessage(UNABLE_TO_CONVERT_VALUE_TO_DATE_OBJECT).build();
		}
		if(ValidatorUtils.notNull(date)){
			String dateFormat = userService.getUserPreferredDateFormat();
			dateString = CoreDateUtility.formatDateAsString(date, dateFormat);
		}
		return dateString;
	}
	
	public UIMetaDataVo prepareDynamicFormFromUiMetaData(String uiMetaDataVoString,Long screenId) {
		DynamicForm dynamicFormObj = formService.prepareUIMetaDataVoFromUiMetaDataJSON(deserializeSingleDynamicFormData(uiMetaDataVoString));
		if(ValidatorUtils.notNull(dynamicFormObj)){
			Map<String,Object> formDataMap = dynamicFormObj.getFieldValuePariringForDisplay();
			UIMetaDataVo uiMetaDataVo = mergeFormDetailsAndData(
					dynamicFormObj.getUiMetaData(), formDataMap);
			if(ValidatorUtils.notNull(uiMetaDataVo)){
				uiMetaDataVo.setPlaceHolderID(screenId);
			}
			
			return uiMetaDataVo;
		}
		return null;
	}
	
	/**This Method is to prepare the data to populate in dynamic form,
	 * This method will be only called when dataMap is null

	 * @param uiMetaData
	 * @param uri
	 * @param dynamicFormData
	 * @return
	 */
    public Map<String,Object> prepareDataToBePopulatedInDynamicForm(UIMetaData uiMetaData,String uri,String dynamicFormData){

    	List<Object> dataToPopulate;
        Map<String,Object> dataMap = new HashMap<>();
        
		Hibernate.initialize(uiMetaData.getPanelDefinitionList());
		if (null != uiMetaData.getPanelDefinitionList()) {
			for (PanelDefinition panelDefinition : uiMetaData.getPanelDefinitionList()) {
				if (null != panelDefinition && !(panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE || panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_TABLE)) {
					Hibernate.initialize(panelDefinition.getFieldDefinitionList());
					Map<String,Object> fieldMap = new HashMap<>();
					if (null != panelDefinition.getFieldDefinitionList()) {
						for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {
							if (fieldDefinition != null && (fieldDefinition.getFieldType().equalsIgnoreCase(FormComponentType.DROP_DOWN) ||
									fieldDefinition.getFieldType().equalsIgnoreCase(FormComponentType.TEXT_BOX)) && fieldDefinition.getProductSchemeMetaData() != null){
								Hibernate.initialize(fieldDefinition.getProductSchemeMetaData());
								if(fieldDefinition.getPopulateAssignmentResult()!=null && fieldDefinition.getPopulateAssignmentResult() && StringUtils.isNotEmpty(fieldDefinition.getAssignmentMasterCode())){
									dataToPopulate = formService.getDataToBePopulatedForApplication(fieldDefinition.getProductSchemeMetaData(),dynamicFormData,fieldDefinition.getFieldType(),fieldDefinition.getAssignmentMasterCode());
								}else {
									dataToPopulate = formService.getDataToBePopulatedForApplication(fieldDefinition.getProductSchemeMetaData(), dynamicFormData, fieldDefinition.getFieldType());
								}
								if(CollectionUtils.isNotEmpty(dataToPopulate)){
									if(panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_PANEL){
										fieldMap.put(fieldDefinition.getFieldKey(),dataToPopulate.get(0)!=null?dataToPopulate.get(0):StringUtils.SPACE);
									}else{
										dataMap.put(fieldDefinition.getFieldKey(),dataToPopulate.get(0)!=null?dataToPopulate.get(0):StringUtils.SPACE);
									}
								}
							}
						}
						if(fieldMap.size()>0){
							dataMap.put(panelDefinition.getPanelKey(),fieldMap);
						}
					}
				}
			}
		}
		return dataMap;
    }
	
}
