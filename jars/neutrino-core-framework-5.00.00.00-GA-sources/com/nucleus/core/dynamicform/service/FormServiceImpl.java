package com.nucleus.core.dynamicform.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.formsConfiguration.fieldcomponent.LOVFieldVO;
import com.nucleus.finnone.pro.lov.LovConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.core.convert.ConversionService;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.dynamicform.dao.FormDao;
import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.core.formDefinition.FormDefinitionUtility;
import com.nucleus.core.formsConfiguration.DynamicForm;
import com.nucleus.core.formsConfiguration.DynamicFormScreenMapping;
import com.nucleus.core.formsConfiguration.FieldDataType;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.core.formsConfiguration.FormComponentType;
import com.nucleus.core.formsConfiguration.FormComponentVO;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.FormContainerType;
import com.nucleus.core.formsConfiguration.FormFieldVO;
import com.nucleus.core.formsConfiguration.MasterDynamicForm;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.MultipleForm;
import com.nucleus.core.formsConfiguration.MultipleFormData;
import com.nucleus.core.formsConfiguration.PanelDefinition;
import com.nucleus.core.formsConfiguration.PersistentFormData;
import com.nucleus.core.formsConfiguration.ProductSchemeMetaData;
import com.nucleus.core.formsConfiguration.ScreenId;
import com.nucleus.core.formsConfiguration.SingleDynamicForm;
import com.nucleus.core.formsConfiguration.SpecialTable;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.formsConfiguration.UIMetaDataVo;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.money.entity.Money;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.money.MoneyService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.service.BaseServiceImpl;

import flexjson.JSONSerializer;
import flexjson.transformer.AbstractTransformer;
import flexjson.transformer.Transformer;

/**
 * 
 * @author Nucleus Software Exports Limited Dynamic Form Service Implementation
 */

@Named(value = "formConfigService")
public class FormServiceImpl extends BaseServiceImpl implements FormService {

    public static final String      defaultDateFormat = "MM/dd/yyyy";
  
    @Inject
    @Named("formDefinitionService")
    private FormDefinitionService   formDefinitionService;

    @Inject
    @Named("formConfigurationMappingService")
    FormConfigurationMappingService formConfigurationMappingService;

    @Inject
    @Named("jsonMapConverter")
    private JsonMapConverter        jsonMapConverter;

    @Inject
    @Named("conversionService")
    private ConversionService       conversionService;

    @Inject
    @Named("moneyService")
    protected MoneyService          moneyService;
    
    @Inject
    @Named("formDefinitionUtility")
    private FormDefinitionUtility formDefinitionUtility;
    
    @Inject
    @Named("baseMasterService")
    protected BaseMasterService       baseMasterService;
    
    @Inject
    @Named("formDao")
    private FormDao formDao;
    
    @Inject
    @Named("entityDao")
    private EntityDao entityDao;
    
    @Inject
    @Named("genericParameterService")
    GenericParameterService genericParameterService;
      
    @Inject
    protected IDynamicFormPostProcessor dynamicFormPostProcessor;

    private final Transformer       money_transformer = new AbstractTransformer() {
                                                          @Override
                                                          public void transform(Object object) {
                                                              Money money = (Money) object;
                                                              getContext().write("\"" + money.toString() + "\"");
                                                          }
                                                      };

    @Override
    public Object loadTransientMap(Object object, boolean ruleExecution) {

        PersistentFormData persistentFormData = null;
        Map<String, Object> loadedMap = null;

        Class superClass = MultipleFormData.class;

        if (null != object) {

            if (superClass.isAssignableFrom(object.getClass())) {

                // For Multiple forms
                Map<String, PersistentFormData> persistentFormDataMap = ((MultipleFormData) object).getFormDataMap();

                if (null != persistentFormDataMap && persistentFormDataMap.size() > 0) {

                    for (Map.Entry<String, PersistentFormData> entry : persistentFormDataMap.entrySet()) {

                        persistentFormData = entry.getValue();
                        loadedMap = jsonMapConverter.loadFormPersistentDataMap(persistentFormData, ruleExecution);
                        persistentFormData.setFieldValuePariring(loadedMap);
                    }
                }

            } else {
            	// For single form
            	Method  method = DynamicFormUtil.getPersistentFormDataGetterOrSetterMethod(object,DynamicFormUtil.PERSISTENT_FORM_DATA_GETTER_METHOD);
            	if(method != null) {
                   try {
                	   persistentFormData = (PersistentFormData) method.invoke(object, null);
					} catch (Exception e) {
				    }
            	}
            	
                loadedMap = jsonMapConverter.loadFormPersistentDataMap(persistentFormData, ruleExecution);
                persistentFormData.setFieldValuePariring(loadedMap);
            }
        }

        return object;
    }
   
	@Override
    public Map<String, Object> loadPersistentDataMap(String uri, String formName, PersistentFormData persistentFormData) {

        Map<String, Object> dataMap = jsonMapConverter.loadFormPersistentDataMap(persistentFormData, false);

        return dataMap;
    }

    @Override
    public List<FormComponentType> getFormComponentType() {
        return entityDao.findAll(FormComponentType.class);
    }

    public List<PanelDefinition> getFormByModelName(String modelName) {
        NamedQueryExecutor<UIMetaData> formDefCriteria = new NamedQueryExecutor<UIMetaData>("dynamicForm.getFormByModelName")
                .addParameter("modelName", modelName).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        List<PanelDefinition> panelDefinitionList = new ArrayList<PanelDefinition>();

        UIMetaData createPanelDefinitionList = entityDao.executeQueryForSingleValue(formDefCriteria);

        if (null != createPanelDefinitionList) {
            panelDefinitionList = createPanelDefinitionList.getPanelDefinitionList();
        }

        return panelDefinitionList;
    }

    @Override
    public ModelMetaData getModelByModelName(String modelName) {
        NamedQueryExecutor<ModelMetaData> executor = new NamedQueryExecutor<ModelMetaData>(
                "dynamicForm.getModelMetaDataByModelName").addParameter("modelName", modelName);

        List<ModelMetaData> modelMataDatalist = entityDao.executeQuery(executor);
        ModelMetaData modelMetaData = null;
        
        if(ValidatorUtils.hasElements( modelMataDatalist)){
        		modelMetaData = modelMataDatalist.get(0);
        }
        
        return modelMetaData;
    }

    public List<FormComponentType> getDynamicFormFieldType() {
        return entityDao.findAll(FormComponentType.class);
    }

    public List<FieldDataType> getDynamicFormFieldDataType() {
        return entityDao.findAll(FieldDataType.class);
    }

    @Override
    public void persistFormData(String uri, Map<String, Object> dataMap, UIMetaDataVo uiMetaDataVo) {

        PersistentFormData persistentFormData = null;
        Object object = entityDao.get(EntityId.fromUri(uri));

        Class superClass = MultipleFormData.class;

        if (superClass.isAssignableFrom(object.getClass())) {

            Map<String, PersistentFormData> persistentFormDataMap = ((MultipleFormData) object).getFormDataMap();

            if (null != persistentFormDataMap) {

                persistentFormData = persistentFormDataMap.get(uiMetaDataVo.getModelName().replaceAll(" ", "_"));
                persistentFormData = populateJsonMap(uiMetaDataVo.getFormUri(), persistentFormData, dataMap,
                        uiMetaDataVo.getModelUri());

                ((MultipleFormData) object).addToFormDataMap(uiMetaDataVo.getModelName().replaceAll(" ", "_"),
                        persistentFormData);
            }

        } else {
     	
        	Method  getterMethod = DynamicFormUtil.getPersistentFormDataGetterOrSetterMethod(object,DynamicFormUtil.PERSISTENT_FORM_DATA_GETTER_METHOD);
        	if(getterMethod != null) {
               try {
            	   persistentFormData = (PersistentFormData) getterMethod.invoke(object, null);
            	   persistentFormData = populateJsonMap(uiMetaDataVo.getFormUri(), persistentFormData, dataMap,
                           uiMetaDataVo.getModelUri());
				} catch (Exception e) {
			    }
        	}
        	
            

            Method  setterMethod = DynamicFormUtil.getPersistentFormDataGetterOrSetterMethod(object,DynamicFormUtil.PERSISTENT_FORM_DATA_SETTER_METHOD);
        	if(setterMethod != null) {
               try {
            	   setterMethod.invoke(object, persistentFormData);
            	   dynamicFormPostProcessor.postProcessDynamicFormData(object);
				} catch (Exception e) {
			    }
        	}
        }

        entityDao.saveOrUpdate(((BaseEntity) object));
    }

	private PersistentFormData populateJsonMap(String formUri, PersistentFormData persistentFormData,
            Map<String, Object> dataMap, String modelUri) {

        if (null == persistentFormData) {
            persistentFormData = new PersistentFormData();
        }

        persistentFormData.setFieldValuePariring(dataMap);

        ModelMetaData modelMetaData = formDefinitionService.getModelMetaDataByUri(modelUri);

        persistentFormData.setModelMetaData(modelMetaData);
        persistentFormData.setFormUri(formUri);

        Map<String, Object> jsonMap = jsonMapConverter.saveFormPersistentDataMap(persistentFormData.getFieldValuePariring(),persistentFormData.getModelMetaData().getFields());
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.transform(money_transformer, Money.class).exclude("*.class").deepSerialize(jsonMap);

        persistentFormData.setFieldValueData(json);

        return persistentFormData;
    }

    @Override
    public void saveNewObject(Entity object) {
        entityDao.persist(object);
    }

    @Override
    public BaseEntity loadReference(EntityId entityId) {
        return (BaseEntity) entityDao.find(entityId.getEntityClass(), entityId.getLocalId());
    }

    @Override
    public Object loadLOVFieldValueOrReference(LOVFieldVO lovFieldVO) {
        if(StringUtils.isNotBlank(lovFieldVO.getLovEntityClass())){
            String referencedEntityURI = lovFieldVO.getLovEntityClass()+EntityId.URI_PART_SEPARATOR+lovFieldVO.getLovHiddenValue();
            return loadReference(EntityId.fromUri(referencedEntityURI));
        }else{
            return lovFieldVO.getLovHiddenValue();
        }
    }

    @Override
    public List<UIMetaData> loadUniqueLatestForms() {
        NamedQueryExecutor<UIMetaData> formDefCriteria = new NamedQueryExecutor<UIMetaData>(
                "dynamicForm.getUniqueLatestForms").addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        return entityDao.executeQuery(formDefCriteria);
    }
    
    @Override
    public List<UIMetaData> loadUniqueLatestFormsBasedOnSourceProduct(Long sourceProductValue) {
        NamedQueryExecutor<UIMetaData> formDefCriteria = new NamedQueryExecutor<UIMetaData>(
                "dynamicForm.loadUniqueLatestFormsBasedOnSourceProduct").addParameter("sourceProductId", sourceProductValue)
                .addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        return entityDao.executeQuery(formDefCriteria);
    }

    @Override
    public List<FormConfigurationMapping> loadUniqueDynamicForms() {
        return formDao.loadUniqueDynamicForms();
    }
    
    @Override
    public List<FormConfigurationMapping> fetchSelectedFormConfigurations(List<Long> formConfigIds){
        return formDao.fetchSelectedFormConfigurations(formConfigIds);
    }
    
    @Override
    public UIMetaData getLatestFormByFormUuid(String formuuid) {

        UIMetaData uiMetaData = new UIMetaData();

        NamedQueryExecutor<UIMetaData> formDefCriteria = new NamedQueryExecutor<UIMetaData>(
                "dynamicForm.getLatestFormsByFormuuid").addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        List<UIMetaData> uiMetaDatas = entityDao.executeQuery(formDefCriteria.addParameter("formuuid", formuuid));

        if (null != uiMetaDatas && uiMetaDatas.size() > 0) {
            uiMetaData = uiMetaDatas.get(0);
        }

        return uiMetaData;
    }

    @Override
    public List<Map<String, Object>> getFormsNamesGroupByuuid() {
        NamedQueryExecutor<Map<String, Object>> formDefCriteria = new NamedQueryExecutor<Map<String, Object>>(
                "dynamicForm.getformsGroupByuuid").addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Map<String, Object>> formNames = entityDao.executeQuery(formDefCriteria);
        return formNames;
    }

    @Override
    public String getFormNameByuuid(String formuuid) {
        NamedQueryExecutor<String> formDefCriteria = new NamedQueryExecutor<String>("dynamicForm.getFormNameByuuid");
        formDefCriteria.addParameter("formuuid", formuuid).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<String> formNames = entityDao.executeQuery(formDefCriteria);
        if (ValidatorUtils.hasElements(formNames)) {
            return formNames.get(0);
        }
        return null;
    }

    @Override
    public List<UIMetaData> getFormByName(String formName) {
        NamedQueryExecutor<UIMetaData> formConfigDataCriteria = new NamedQueryExecutor<UIMetaData>(
                "dynamicForm.validateFormName").addParameter("formName", formName).addQueryHint(
                QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(formConfigDataCriteria);
    }

    @Override
    public List<UIMetaData> getFormByNameAndVersion(String formName, String formVersion) {
        NamedQueryExecutor<UIMetaData> formConfigDataCriteria = new NamedQueryExecutor<UIMetaData>(
                "dynamicForm.getFormByNameVersion").addParameter("formName", formName)
                .addParameter("formVersion", formVersion).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(formConfigDataCriteria);
    }

    @Override
    public List<UIMetaDataVo> getUIMetaDataVoList(String jsonString) {
        List<UIMetaDataVo> uiMetaDataVoList = new ArrayList<UIMetaDataVo>();
        try {
            String decodedString = URLDecoder.decode(jsonString, "UTF-8");
            JSONArray jArray = new JSONArray(decodedString);
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
            if (jArray != null) {
                for (int i = 0 ; i < jArray.length() ; i++) {
                    JSONObject jsonObject = (JSONObject) jArray.get(i);
                    Map<String, Object> map = mapper.readValue(jsonObject.toString(),
                            new TypeReference<HashMap<String, Object>>() {
                            });
                    mapList.add(map);
                }
            }

            for (Map<String, Object> t : mapList) {
                Iterator<Entry<String, Object>> iter = t.entrySet().iterator();
                UIMetaDataVo uiMetaDataVo = new UIMetaDataVo();
                BeanWrapper beanWrapper = new BeanWrapperImpl(uiMetaDataVo);
                beanWrapper.setAutoGrowNestedPaths(true);
                beanWrapper.setConversionService(conversionService);

                while (iter.hasNext()) {
                    Entry<String, Object> entry = iter.next();
                    String propertyName = entry.getKey();
                    Object propertyValue = entry.getValue();

                    try{
                        beanWrapper.setPropertyValue(propertyName, propertyValue);
                    }catch (InvalidPropertyException invalidPropertyException){
                    }
                }
                uiMetaDataVoList.add(uiMetaDataVo);
            }
        } catch (Exception exception) {
            return null;
        }
        return uiMetaDataVoList;
    }

    @Override
    public Map<String, Object> getJsonMapToSave(UIMetaDataVo uiMetaDataVo) {

        Map<String, Object> dataMap = new HashMap<>();
     

        if (null != uiMetaDataVo && null != uiMetaDataVo.getUiComponents()) {

            List<FormComponentVO> formComponentVOList = uiMetaDataVo.getUiComponents();

            // Iterate over Panel Definition VO's list
            for (FormComponentVO formComponentVO : formComponentVOList) {

                if (null != formComponentVO) {

                   if (formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_PANEL) {
                	   updateJsonMapforPanel(formComponentVO,dataMap);
                        // Actual Panel
                	  

                    } else if (formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_VIRTUAL) {
                    	   updateJsonMapforVirtualField(formComponentVO,dataMap);
                       

                    } else if ((formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_TABLE)||(formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE)) {
                    	   updateJsonMapforTable(formComponentVO,dataMap);
                       
                    }
                }

            }
        }
        return dataMap;

    }

    private void updateJsonMapforVirtualField(FormComponentVO formComponentVO, Map<String, Object> dataMap) {
    	if (CollectionUtils.isNotEmpty(formComponentVO.getFormFieldVOList())) {
    		for (FormFieldVO formFieldVO : formComponentVO.getFormFieldVOList()) {
            // Set the specific values in map that can be serialized
    			dataMap.put(formFieldVO.getId(), createSpecificObjectVal(formFieldVO));
        	}
    	}
    		
	}

	private void updateJsonMapforTable(FormComponentVO formComponentVO, Map<String, Object> dataMap) {
		 List<Map<String, Object>> tablePanelDataMapList = new ArrayList<>();
	        Map<String, Object> tablePanelDataMap = null;
		 List<FormComponentVO> formComponentVOTableList = formComponentVO.getFormComponentList();
   
if(CollectionUtils.isNotEmpty(formComponentVOTableList)){

    for (FormComponentVO formComponentTableVO : formComponentVOTableList) {

        if(CollectionUtils.isNotEmpty(formComponentTableVO.getFormFieldVOList())){
            tablePanelDataMap = new HashMap<>();

            for (FormFieldVO formFieldTableVO : formComponentTableVO.getFormFieldVOList()) {

                tablePanelDataMap.put(formFieldTableVO.getId(), createSpecificObjectVal(formFieldTableVO));
            }

            tablePanelDataMapList.add(tablePanelDataMap);
            dataMap.put(formComponentVO.getPanelKey(), tablePanelDataMapList);
        }

    }
}

	}

	private void updateJsonMapforPanel(FormComponentVO formComponentVO, Map<String, Object> dataMap) {
    	 Map<String, Object>  actualPanelMapData = new HashMap<>();
          if(CollectionUtils.isNotEmpty(formComponentVO.getFormFieldVOList())){
              for (FormFieldVO formFieldVO : formComponentVO.getFormFieldVOList()) {
                  actualPanelMapData.put(formFieldVO.getId(), createSpecificObjectVal(formFieldVO));
              }
          }
          // IF actual panel, add the map against panel name
          dataMap.put(formComponentVO.getPanelKey(), actualPanelMapData);
		
	}

	/**
     * 
     * Method to get the proper object values based on data type
     * These values should be set so that they can be serialized
     * @param formFieldVO
     * @return
     */
    private Object createSpecificObjectVal(FormFieldVO formFieldVO) {

        List<Object> newValues = new ArrayList<>();

        List<String> oldValuesList = formFieldVO.getValue();

        String entityName = formFieldVO.getEntityName();

        Object obj = null;
        int dataType = formFieldVO.getFieldDataType();

        if (null != oldValuesList) {
            for (String value : oldValuesList) {

                if (dataType == FieldDataType.DATA_TYPE_TEXT_BOOLEAN) {
                   
                	obj= 	createBooleanObjectVal(value);
                } else if (dataType == FieldDataType.DATA_TYPE_DATE) {

                   
                	obj= 	createDateObjectVal(value);
                } else if (dataType == FieldDataType.DATA_TYPE_TEXT) {
                    obj = value;

                } else if (dataType == FieldDataType.DATA_TYPE_NUMBER || dataType == FieldDataType.DATA_TYPE_INTEGER) {
                   
                	obj= 	createNumObjectVal(value);
                } else if (dataType == FieldDataType.DATA_TYPE_MONEY) {
                   
                    obj= 	createMoneyObjectVal(value);
                } else if (dataType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
                   
                	obj= 	createReferenceObjectVal(value,entityName);
                }

                newValues.add(obj);
            }
        }

        if(formFieldVO.getFieldType().equals(FormComponentType.LOV)){
            LovConfig lovConfig = formDefinitionService.getLovConfigForLovKey(formFieldVO.getLovKey());
            if(lovConfig!=null){
                LOVFieldVO lovFieldVO = formFieldVO.getLovFieldVO();
                if("id".equals(lovConfig.getHiddenColumn())){
                    lovFieldVO.setLovEntityClass(lovConfig.getEntityClass());
                }
                return lovFieldVO;
            }else{
                return formFieldVO.getLovFieldVO();
            }
        }
        if (formFieldVO.getFieldType().equals(FormComponentType.MULTISELECTBOX)) {
            return newValues;
        }
        if (formFieldVO.getFieldType().equals(FormComponentType.PHONE)) {
            return formFieldVO.getPhoneNumberVO();
        }
        if (formFieldVO.getFieldType().equals(FormComponentType.EMAIL)) {
            return formFieldVO.getEmailInfoVO();
        }

        return obj;
    }
    
    private Object createBooleanObjectVal(String value) {
    	  Object obj=null;
    	 if (null != value && !"".equals(value)) {
             obj = Boolean.parseBoolean(value);
         }
		return obj;
	}

	private Object createDateObjectVal(String value) {
    	 String defaultSYSDateFormat = getUserPreferredDateFormat();
    	  Object obj=null;
         DateFormat formatter = new SimpleDateFormat(defaultSYSDateFormat);
         Date date = null;

         if (null != value && !"".equals(value)) {

             try {
                 date = formatter.parse(value);
             } catch (ParseException e) {
            		BaseLoggers.exceptionLogger.error("Exception:" + e.getMessage(), e);         
             }

             if (date != null) {
                 DateTime calendar = new DateTime(date);
                 obj = calendar;
             }

         }
		return obj;
	}

	private Object createNumObjectVal(String value) {
		  Object obj=null;
    	 if (null != value && !"".equals(value)) {
             obj = new BigDecimal(value);
         }
		return obj;
	}

	private Object createMoneyObjectVal(String value) {
		  Object obj=null;
    	 if (null != value && !"".equals(value)) {
             obj = moneyService.parseMoney(value, null);
         }
		return obj;
	}

	private Object createReferenceObjectVal(String value, String entityName) {
		  Object obj=null;
    	 if (null != entityName && !"".equals(entityName) && (null != value && !"".equals(value))) {
             obj = loadReference(EntityId.fromUri(entityName + ":" + Long.valueOf(value)));
         }
		return obj;
	}

	@Override
    public Map<String, Object> loadDynamicFormDataMap(DynamicForm dynamicFormObj) {

        return jsonMapConverter.loadDynamicFormDataMap(dynamicFormObj, false);
    }
    
        @Override
        public UIMetaData getFormByModelUri(String modelUri){
        	NamedQueryExecutor<UIMetaData> formDef=new NamedQueryExecutor<UIMetaData>("dynamicForm.getFormByModelUri").addParameter("modelUri", modelUri).addQueryHint(
                    QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        	UIMetaData uiMetaData=entityDao.executeQueryForSingleValue(formDef);
        	return uiMetaData;
    }
    	@Transactional
    	@Override
        public void saveOrUpdateDynamicFormInObject(UIMetaDataVo uiMetaDataVo, Object object){
     	   ModelMetaData modelMetaDataObj=fetchModelMetaDataFromUiMetaDataVO(uiMetaDataVo);
     	   if(ValidatorUtils.isNull(modelMetaDataObj)){
     		   return;
     	   }else if(ValidatorUtils.isNull(uiMetaDataVo)){
     		   return;
     	   }
     		UIMetaData uiMetaDataObj=fetchUiMetaDataFromVO(modelMetaDataObj);
     		Map<String, Object> dynamicFormDataMap=getJsonMapToSave(uiMetaDataVo);
     		if (object instanceof SingleDynamicForm) {
     			saveOrUpdateDynamicFormDataForSingleDynamicForm(dynamicFormDataMap,modelMetaDataObj,uiMetaDataObj,object,uiMetaDataVo.getPlaceHolderID());
     		}else if(object instanceof MultipleForm){
     			saveOrUpdateDynamicFormDataInMultipleForm(dynamicFormDataMap,modelMetaDataObj , uiMetaDataObj,object,uiMetaDataVo.getPlaceHolderID() );
     		} else if(object instanceof MasterDynamicForm){
     			saveOrUpdateDynamicFormDataInMasterForm(dynamicFormDataMap,modelMetaDataObj , uiMetaDataObj,object,uiMetaDataVo.getPlaceHolderID() );
     		}
        }


		private void saveOrUpdateDynamicFormDataInMasterForm(Map<String, Object> dynamicFormDataMap,
			ModelMetaData modelMetaDataObj, UIMetaData uiMetaDataObj, Object object, Long placeHolderID) {
		MasterDynamicForm masterDynamicForm = (MasterDynamicForm) object;
		DynamicForm dynamicFormobj = null;
		if (ValidatorUtils.isNull(masterDynamicForm)) {
			return;
		}
		if (notNull(masterDynamicForm.getDynamicForm())) {
			dynamicFormobj = masterDynamicForm.getDynamicForm();
			if (notNull(dynamicFormobj.getUiMetaData())
                    && notNull(uiMetaDataObj) && dynamicFormobj.getUiMetaData().getModelUri().equals(uiMetaDataObj.getModelUri())) {
				Map<String, Object> jsonMap = jsonMapConverter.saveDynamicFormDataMap(dynamicFormDataMap,
						modelMetaDataObj);
				JSONSerializer serializer = new JSONSerializer();
				String json = serializer.transform(money_transformer, Money.class).exclude("*.class")
						.deepSerialize(jsonMap);
				dynamicFormobj.setDataJsonString(json);

			}

		} else {
			dynamicFormobj = new DynamicForm();
			Map<String, Object> jsonMap = jsonMapConverter.saveDynamicFormDataMap(dynamicFormDataMap, modelMetaDataObj);
			JSONSerializer serializer = new JSONSerializer();
			String json = serializer.transform(money_transformer, Money.class).exclude("*.class")
					.deepSerialize(jsonMap);
			dynamicFormobj.setDataJsonString(json);
			dynamicFormobj.setModelMetaDataId(modelMetaDataObj.getId());
			dynamicFormobj.setUiMetaDataId(uiMetaDataObj.getId());
			dynamicFormobj.setPlaceholderId(placeHolderID);
			masterDynamicForm.setDynamicForm(dynamicFormobj);
		}

	}

		@Override
		public void saveDynamicFormData(String uri, UIMetaDataVo uiMetaDataVo) {
			Object object = entityDao.get(EntityId.fromUri(uri));
			saveOrUpdateDynamicFormInObject(uiMetaDataVo, object);
			entityDao.saveOrUpdate(((BaseEntity) object));			
		}

		
		@Override
		public void saveOrUpdateDynamicFormDataForSingleDynamicForm(
				Map<String, Object> dynamicFormDataMap,
				ModelMetaData modelMetaDataObj, UIMetaData uiMetaDataObj,
				Object object,Long screenId) {
			SingleDynamicForm singleDynamicForm = (SingleDynamicForm) object;
			DynamicForm dynamicFormobj=null;
			if(ValidatorUtils.isNull(singleDynamicForm)){
				return;
			}
			if( notNull(singleDynamicForm.getDynamicForm())){
				dynamicFormobj=singleDynamicForm.getDynamicForm();
				if(notNull(dynamicFormobj.getUiMetaData()) && uiMetaDataObj !=  null && dynamicFormobj.getUiMetaData().getModelUri().equals(uiMetaDataObj.getModelUri())){
					Map<String, Object> jsonMap = jsonMapConverter
							.saveDynamicFormDataMap(dynamicFormDataMap,modelMetaDataObj);
					JSONSerializer serializer = new JSONSerializer();
					String json = serializer.transform(money_transformer, Money.class)
							.exclude("*.class").deepSerialize(jsonMap);
					dynamicFormobj.setDataJsonString(json);
				
				}
				
			}else{
				dynamicFormobj = new DynamicForm();
				Map<String, Object> jsonMap = jsonMapConverter
						.saveDynamicFormDataMap(dynamicFormDataMap,modelMetaDataObj);
				JSONSerializer serializer = new JSONSerializer();
				String json = serializer.transform(money_transformer, Money.class)
						.exclude("*.class").deepSerialize(jsonMap);
				dynamicFormobj.setDataJsonString(json);
				dynamicFormobj.setModelMetaDataId(modelMetaDataObj.getId());
				if(uiMetaDataObj != null) {
                    dynamicFormobj.setUiMetaDataId(uiMetaDataObj.getId());
                }
				dynamicFormobj.setPlaceholderId(screenId);
				singleDynamicForm.setDynamicForm(dynamicFormobj);
			}
			
		}
		
		public void saveOrUpdateDynamicFormDataInMultipleForm(
				Map<String, Object> dynamicFormDataMap,ModelMetaData modelMetaDataObj, UIMetaData uiMetaDataObj,Object object,Long screenId
				) {
			MultipleForm multipleFormObj=(MultipleForm)object;
			DynamicForm	dynamicFormObj=null;
			
			if(ValidatorUtils.isNull(multipleFormObj)) {
				return;
			}
			
			if(notNull(uiMetaDataObj) 
					&& notNull(multipleFormObj.getDynamicForm(uiMetaDataObj.getFormName()))
					&& screenId.equals(multipleFormObj.getDynamicForm(uiMetaDataObj.getFormName()).getPlaceholderId())){
				dynamicFormObj = multipleFormObj.getDynamicForm(uiMetaDataObj.getFormName());
				UIMetaData uiMetaData = dynamicFormObj.getUiMetaData();
				if(notNull(dynamicFormObj) && notNull(uiMetaData) && (uiMetaData.getModelUri().equals(uiMetaDataObj.getModelUri())))
				{
					
					Map<String, Object> jsonMap = jsonMapConverter
							.saveDynamicFormDataMap(dynamicFormDataMap,modelMetaDataObj);
					JSONSerializer serializer = new JSONSerializer();
					String json = serializer.transform(money_transformer, Money.class)
							.exclude("*.class").deepSerialize(jsonMap);
					dynamicFormObj.setDataJsonString(json);
				}
			}else{
				dynamicFormObj = new DynamicForm();			
				Map<String, Object> jsonMap = jsonMapConverter
						.saveDynamicFormDataMap(dynamicFormDataMap,modelMetaDataObj);
				JSONSerializer serializer = new JSONSerializer();
				String json = serializer.transform(money_transformer, Money.class)
						.exclude("*.class").deepSerialize(jsonMap);
				dynamicFormObj.setDataJsonString(json);
				dynamicFormObj.setModelMetaDataId(modelMetaDataObj.getId());
				dynamicFormObj.setUiMetaDataId(uiMetaDataObj.getId());
				dynamicFormObj.setPlaceholderId(screenId);
				multipleFormObj.addMultiDynamicForm(dynamicFormObj);
				}
			
			
			
		}
		
		private ModelMetaData fetchModelMetaDataFromUiMetaDataVO(
				UIMetaDataVo uiMetaDataVo) {
			ModelMetaData modelMetaDataObj=null;
			if(notNull(uiMetaDataVo) && isNotEmpty(uiMetaDataVo.getModelUri())){
				Object obj = entityDao.get(EntityId.fromUri(uiMetaDataVo.getModelUri()));
				if(obj instanceof ModelMetaData){
					modelMetaDataObj=(ModelMetaData)obj;
				}
	    	}
			return modelMetaDataObj;
		}
		
		private UIMetaData fetchUiMetaDataFromVO(ModelMetaData modelMetaDataObj) {
			UIMetaData uiMetaDataObj=null;
			if(notNull(modelMetaDataObj) && isNotEmpty(modelMetaDataObj.getUri())){
				uiMetaDataObj=getAllFormsByModelUri(modelMetaDataObj.getUri());
	    	}
			return uiMetaDataObj;
		}

        private UIMetaData getAllFormsByModelUri(String modelUri) {
            NamedQueryExecutor<UIMetaData> formDef = new NamedQueryExecutor<UIMetaData>("dynamicForm.getAllFormsByModelUri")
                    .addParameter("modelUri", modelUri).addQueryHint(
                            QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            return entityDao.executeQuery(formDef).get(0);
        }

		@Override
		public Boolean checkIfScreenIdIsSingleDynamicFormEnabled(Long screenId) {
			 Boolean singleDynamicFormEnabled=Boolean.FALSE; 
			 ScreenId screenIdObject= formDao.fetchScreenIdObjectBasedOnId(screenId);
        	if(notNull(screenIdObject)){
        		singleDynamicFormEnabled=screenIdObject.getSingleDynamicFormEnabled();
        	}
        	return singleDynamicFormEnabled;
		}
		
		@Override
		@Transactional
		public DynamicFormScreenMapping fetchDynamicFormScreenMappingById(Long dynamicFormScreenMappingId){
			
			 return  entityDao.find(DynamicFormScreenMapping.class, dynamicFormScreenMappingId);
			         
		}

        @Override
        public List<ScreenId> fetchPlaceHolderIdsMappedToSourceProduct(
                Long sourceProductId) {
           return formDao.fetchPlaceHolderIdsMappedToSourceProduct(sourceProductId);
        }

      
        @Override
        @Transactional
        public List<FormConfigurationMapping> loadUniqueDynamicFormsForSourceProduct(
                Long sourceProductValue) {
            List<FormConfigurationMapping> formConfigList=formDao.loadUniqueDynamicFormsForSourceProduct(sourceProductValue);
            return formConfigList;
        }
        
       @Override
      public  ScreenId fetchScreenIdBasedOnId(Long screenIdValue){
          return  entityDao.find(ScreenId.class,screenIdValue);
        }
       
       @Override
       public SourceProduct fetchSourceProductBasedOnId(Long sourceProductValue){
        return genericParameterService.findById(sourceProductValue, SourceProduct.class);
       }

    @Override
    public List<UIMetaData> getFormByNameAndSourceProduct(String formName,
            Long sourceProductValue) {

        NamedQueryExecutor<UIMetaData> formConfigDataCriteria = new NamedQueryExecutor<UIMetaData>(
                "dynamicForm.fetchFormBasedOnFormNameAndSourceProduct").addParameter("formName", formName).addParameter("sourceProductId", sourceProductValue);
               
        return entityDao.executeQuery(formConfigDataCriteria);
    
    }

    @Override
    public Long getScreenIdbyScreenCode(String screenCode) {
        NamedQueryExecutor<Long> screenId = new NamedQueryExecutor<Long>(
                "dynamicForm.getScreenIdbyScreenCode").addParameter("screenCode", screenCode);
              return entityDao.executeQueryForSingleValue(screenId);

            }

    @Transactional
	@Override
	public UIMetaData getUiMetaDataById(Long id) {
	
		return entityDao.find(UIMetaData.class, id);
	}
    
    @Transactional
	@Override    
    public DynamicFormFilter getFilterByServiceIdentifier(Long id)
    {
    	return entityDao.find(DynamicFormFilter.class, id);
    }
    
    @Override
    public  List<UIMetaData> getUniqueFormsBySourceProductAndPersistantStatus(Long sourceProductValue)
    {
    	 NamedQueryExecutor<UIMetaData> formDefCriteria = new NamedQueryExecutor<UIMetaData>(
                 "dynamicForm.getUniqueFormsBasedOnSourceProductAndPersistantStatus")
    			 .addParameter("sourceProductId", sourceProductValue)
    			 .addParameter("persistenceStatus", PersistenceStatus.INACTIVE)
    			 .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

         return entityDao.executeQuery(formDefCriteria);
    }
    
    @Override
    public DynamicForm prepareUIMetaDataVoFromUiMetaDataJSON(UIMetaDataVo uiMetaDataVo){
    	
       ModelMetaData modelMetaDataObj=fetchModelMetaDataFromUiMetaDataVO(uiMetaDataVo);
   	   if(ValidatorUtils.isNull(modelMetaDataObj)){
   		   return  null;
   	   }else if(ValidatorUtils.isNull(uiMetaDataVo)){
   		   return  null;
   	   }
   		UIMetaData uiMetaDataObj=fetchUiMetaDataFromVO(modelMetaDataObj);
   		Map<String, Object> dynamicFormDataMap=getJsonMapToSave(uiMetaDataVo);
   		DynamicForm dynamicFormobj=null;
   		dynamicFormobj = new DynamicForm();
		Map<String, Object> jsonMap = jsonMapConverter
				.saveDynamicFormDataMap(dynamicFormDataMap,modelMetaDataObj);
		JSONSerializer serializer = new JSONSerializer();
		String json = serializer.transform(money_transformer, Money.class)
				.exclude("*.class").deepSerialize(jsonMap);
		dynamicFormobj.setDataJsonString(json);
		dynamicFormobj.setModelMetaDataId(modelMetaDataObj.getId());
		dynamicFormobj.setUiMetaDataId(uiMetaDataObj.getId());
		dynamicFormobj.setPlaceholderId(uiMetaDataVo.getPlaceHolderID());
		
		return dynamicFormobj;
   		
    }

	@Override
	public List<Map<String, ?>> fetchPlaceHolderListMappedToSourceProduct(String value,
			Long sourceProductId, int pageNo) {
		return formDao.fetchPlaceHolderIdsMappedToSourceProduct(value, sourceProductId, pageNo);

	}

    @Override
    public List<SpecialTable> getSpecialTableMetaData() {
        NamedQueryExecutor<SpecialTable> specialTableNamedQueryExecutor = (new NamedQueryExecutor("SpecialTable.getAllMetaData"));
        List<SpecialTable> specialTables = this.entityDao.executeQuery(specialTableNamedQueryExecutor);
        return specialTables;
    }

    @Override
    public SpecialTable getSpecialTable(String key) {
        NamedQueryExecutor<SpecialTable> specialTableNamedQueryExecutor = (new NamedQueryExecutor("SpecialTable.getSpecialTable")).addParameter("key",key);
        return (SpecialTable) entityDao.executeQueryForSingleValue(specialTableNamedQueryExecutor);
    }

    @Override
    public ProductSchemeMetaData getProductSchemeMetaData(String key,String formComponentType) {
        NamedQueryExecutor<ProductSchemeMetaData> productSchemeMetaDataQueryExecutor = (new NamedQueryExecutor("ProductScheme.getProductScheme"));
        productSchemeMetaDataQueryExecutor.addParameter("key",key).addParameter("formComponentType",formComponentType);
        return (ProductSchemeMetaData) entityDao.executeQueryForSingleValue(productSchemeMetaDataQueryExecutor);
    }

    @Override
    public List<ProductSchemeMetaData> getProductSchemeMetaDataColumn(String formComponentType) {
        NamedQueryExecutor<ProductSchemeMetaData> productSchemeMetaDataQueryExecutor = (new NamedQueryExecutor("ProductScheme.getProductSchemeMetaData"));
        productSchemeMetaDataQueryExecutor.addParameter("formComponentType",formComponentType);
        return entityDao.executeQuery(productSchemeMetaDataQueryExecutor);
    }

    @Override
    public List<ProductSchemeMetaData> getProductSchemeMetaDataColumnForAssignmentMatrix(String formComponentType) {
        NamedQueryExecutor<ProductSchemeMetaData> productSchemeMetaDataQueryExecutor = (new NamedQueryExecutor("ProductScheme.getProductSchemeMetaDataForAssignmentMatrix"));
        productSchemeMetaDataQueryExecutor.addParameter("formComponentType",formComponentType);
        return entityDao.executeQuery(productSchemeMetaDataQueryExecutor);
    }

    @Override
    public List<Object> getDataToBePopulatedForApplication(ProductSchemeMetaData productSchemeMetaData, String  dynamicFormData, String formComponentType) {
        return getDataToBePopulatedForApplication(productSchemeMetaData,dynamicFormData,formComponentType,null);
    }

    @Override
    public List<Object> getDataToBePopulatedForApplication(ProductSchemeMetaData productSchemeMetaData, String  dynamicFormData, String formComponentType, String assignmentMasterCode) {
        JPAQueryExecutor<Object> executor = new JPAQueryExecutor<>(productSchemeMetaData.getHql());
        String subLoanId = null;
        String customerId = null;
        String applicationId = null;
        String matrixId = null;
        String matrixName="matrixName";
        if(StringUtils.isNotEmpty(assignmentMasterCode)){
            matrixId= assignmentMasterCode;
        }
        try {

            if(dynamicFormData!=null){

                JSONObject objects = new JSONObject(dynamicFormData);
                Iterator itr = objects.keys();

                while(itr.hasNext()){
                    String subLoanIdKey = "subLoanID";
                    String customerKey = "customerId";
                    if(StringUtils.isNotEmpty(objects.getString(subLoanIdKey))){subLoanId = objects.getString(subLoanIdKey);}
                    if(StringUtils.isNotEmpty(objects.getString(customerKey))){customerId= objects.getString(customerKey);}
                    if(StringUtils.isNotEmpty(objects.getString("appID"))){ applicationId = objects.getString("appID"); }
                    String arr[] = productSchemeMetaData.getHql().split("=");
                    ArrayList<String> arrayList = new ArrayList();
                    for(String a : arr){
                        String result = getQueryParameter(a,".*?:(\\w+).*");
                        if(StringUtils.isNotBlank(result)){
                            arrayList.add(result);
                        }
                    }
                    if(CollectionUtils.isNotEmpty(arrayList)){
                        for(String al : arrayList){
                            if(al.equalsIgnoreCase("subLoanId")){
                                if(subLoanId!=null){
                                    executor.addParameter("subLoanId",Long.parseLong(subLoanId));
                                }else{
                                    executor.addNullParameter("subLoanId");
                                }
                            }
                            if(al.equalsIgnoreCase("applicationId")){
                                if(applicationId!=null){
                                    executor.addParameter("applicationId",Long.parseLong(applicationId));
                                }else {
                                    executor.addNullParameter("applicationId");
                                }
                            }
                            if(al.equalsIgnoreCase(customerKey)){
                                if(customerId!=null){
                                    executor.addParameter(customerKey,Long.parseLong(customerId));
                                }else{
                                    executor.addNullParameter(customerKey);
                                }
                            }
                            if(al.equalsIgnoreCase(matrixName)){
                                if(matrixId!=null){
                                    executor.addParameter(matrixName,matrixId);
                                }else {
                                    executor.addNullParameter(matrixName);
                                }
                            }
                        }
                    }
                    break;
                }

                return entityDao.executeQuery(executor);
            }
        } catch (JSONException e) {
            BaseLoggers.exceptionLogger.error(e.getMessage(),e);
        }
        return null;
    }

    @Override
    public void savePanelData(Map<String, Object> dataMap, String uri, UIMetaDataVo uiMetaDataVo, String panelId) {
        PersistentFormData persistentFormData = null;
        Object object = entityDao.get(EntityId.fromUri(uri));
        Class superClass = MultipleFormData.class;
        if (superClass.isAssignableFrom(object.getClass())) {
            Map<String, PersistentFormData> persistentFormDataMap = ((MultipleFormData) object).getFormDataMap();
            if (null != persistentFormDataMap) {
                persistentFormData = persistentFormDataMap.get(uiMetaDataVo.getModelName().replaceAll(" ", "_"));
                if(persistentFormData!=null && persistentFormData.getFieldValueData()!=null){
                    Map<String, Object> newDataMap = jsonMapConverter.saveFormPersistentDataMap(dataMap,persistentFormData.getModelMetaData().getFields());
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> map;
                    try{
                        map = mapper.readValue(persistentFormData.getFieldValueData(), new TypeReference<Map<String, Object>>(){});
                        for(Map.Entry m : map.entrySet()){
                            newDataMap.putIfAbsent((String)m.getKey(),m.getValue());
                        }
                        persistentFormData.setFieldValueData(mapper.writeValueAsString(newDataMap));
                    }
                    catch(IOException e){
                        BaseLoggers.webLogger.info("Error while updating dynamic form : "+e.getLocalizedMessage());
                    }
                }else{
                    persistentFormData = populateJsonMap(uiMetaDataVo.getFormUri(), persistentFormData, dataMap,uiMetaDataVo.getModelUri());
                }
                ((MultipleFormData) object).addToFormDataMap(uiMetaDataVo.getModelName().replaceAll(" ", "_"),
                        persistentFormData);
            }
        }
    }

    private String getQueryParameter(String value,String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find())
        {
            return matcher.group(1);
        }else{
            return null;
        }
    }
       
    public Map<String, Object> getJsonMapByUIMetadata(UIMetaData uiMetaData,
			Map<String, Object> dynamicFieldValueMap) {
		Map<String, Object> jsonDataMap = new HashMap<>();

		if (null != uiMetaData && null != dynamicFieldValueMap) {
			List<PanelDefinition> panelDefinitions = uiMetaData.getPanelDefinitionList();

			// Iterate over Panel Definition list
			for (PanelDefinition panelDefinition : panelDefinitions) {

				if (null != panelDefinition) {

					if (panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_PANEL) {
						updateJsonMapforFieldTypePanel(panelDefinition, jsonDataMap, dynamicFieldValueMap);
					} else if (panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_VIRTUAL) {
						updateJsonMapforVirtualFieldType(panelDefinition, jsonDataMap, dynamicFieldValueMap);
					} else if ((panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_TABLE)
							|| (panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE)) {
						updateJsonMapforTableType(panelDefinition, jsonDataMap, dynamicFieldValueMap);
					}
				}

			}
		}

		return jsonDataMap;
	}
	
	private void updateJsonMapforTableType(PanelDefinition panelDefinition, Map<String, Object> jsonDataMap,
			Map<String, Object> dynamicFieldValueMap) {

		List<Map<String, Object>> tablePanelDataMapList = new ArrayList<>();
		Map<String, Object> tablePanelDataMap = null;
		List<FieldDefinition> fieldDefinitions = panelDefinition.getFieldDefinitionList();

		if (CollectionUtils.isNotEmpty(fieldDefinitions)) {
			tablePanelDataMap = new HashMap<>();

			for (FieldDefinition fieldDefinition : fieldDefinitions) {
				tablePanelDataMap.put(fieldDefinition.getFieldKey(),
						createSpecificObjectValByFieldDefinition(fieldDefinition, dynamicFieldValueMap));
			}
			tablePanelDataMapList.add(tablePanelDataMap);
			jsonDataMap.put(panelDefinition.getPanelKey(), tablePanelDataMapList);
		}
	}

	private void updateJsonMapforFieldTypePanel(PanelDefinition panelDefinition, Map<String, Object> jsonDataMap,
			Map<String, Object> dynamicFieldValueMap) {
		Map<String, Object> actualPanelMapData = new HashMap<>();
		if (CollectionUtils.isNotEmpty(panelDefinition.getFieldDefinitionList())) {
			for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {
				actualPanelMapData.put(fieldDefinition.getFieldKey(),
						createSpecificObjectValByFieldDefinition(fieldDefinition, dynamicFieldValueMap));
			}
		}
		jsonDataMap.put(panelDefinition.getPanelKey(), actualPanelMapData);
	}
	
	
	@SuppressWarnings("unchecked")
	private Object createSpecificObjectValByFieldDefinition(FieldDefinition fieldDefinition,
			Map<String, Object> dynamicFieldValueMap) {

		List<Object> newValues = new ArrayList<>();

		String entityName = fieldDefinition.getEntityName();

		Object obj = null;
		int dataType = fieldDefinition.getFieldDataType();

		List<Object> oldValuesList = (List<Object>) dynamicFieldValueMap.get(fieldDefinition.getFieldKey());

		if (null != oldValuesList) {
			for (Object value : oldValuesList) {

				if (dataType == FieldDataType.DATA_TYPE_TEXT_BOOLEAN) {
					obj = createBooleanObjectVal(String.valueOf(value));
				} else if (dataType == FieldDataType.DATA_TYPE_DATE) {
					obj = createDateObjectVal(String.valueOf(value));
				} else if (dataType == FieldDataType.DATA_TYPE_TEXT) {
					obj = value;
				} else if (dataType == FieldDataType.DATA_TYPE_NUMBER || dataType == FieldDataType.DATA_TYPE_INTEGER) {
					obj = createNumObjectVal(String.valueOf(value));
				} else if (dataType == FieldDataType.DATA_TYPE_MONEY) {
					obj = createMoneyObjectVal(String.valueOf(value));
				} else if (dataType == FieldDataType.DATA_TYPE_TEXT_REFERENCE) {
					obj = createReferenceObjectVal(String.valueOf(value), entityName);
				} else if (dataType == FieldDataType.DATA_TYPE_PHONE) {
					obj = value;
				}

				newValues.add(obj);
			}
		}

		if (fieldDefinition.getFieldType().equals(FormComponentType.MULTISELECTBOX)) {
			return newValues;
		}

		if (fieldDefinition.getFieldType().equals(FormComponentType.PHONE)) {
			return newValues != null && newValues.size() >= 0 ? newValues.get(0) : new PhoneNumberVO();
		}

		return obj;
	}
	
	private void updateJsonMapforVirtualFieldType(PanelDefinition panelDefinition, Map<String, Object> jsonDataMap,
			Map<String, Object> dynamicFieldValueMap) {
		if (CollectionUtils.isNotEmpty(panelDefinition.getFieldDefinitionList())) {
			for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {
				jsonDataMap.put(fieldDefinition.getFieldKey(),
						createSpecificObjectValByFieldDefinition(fieldDefinition, dynamicFieldValueMap));
			}
		}
	}  
	
}
