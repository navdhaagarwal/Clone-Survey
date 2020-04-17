package com.nucleus.core.dynamicform.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.formsConfiguration.*;
import com.nucleus.core.formsConfiguration.validationcomponent.DedupeMapperVO;
import com.nucleus.core.formsConfiguration.validationcomponent.DynamicFormMapperVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.CloneOptionConstants;
import com.nucleus.finnone.pro.lov.LOVConfigurationLoader;
import com.nucleus.finnone.pro.lov.LovConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.context.MessageSource;

import com.nucleus.contact.EMailType;
import com.nucleus.contact.PhoneNumberType;
import com.nucleus.core.dynamicform.dao.FormDefinitionDao;
import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.core.formDefinition.FormDefinitionUtility;
import com.nucleus.core.formsConfiguration.fieldcomponent.EmailInfoVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.EmailTypeVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberTypeVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationMetadataVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesIFMetadataVO;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationRulesThenMetadataVO;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.dao.query.NativeQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Named("formDefinitionService")
public class FormDefinitionServiceImpl  extends BaseServiceImpl  implements FormDefinitionService {
    
    @Inject
    @Named("messageSource")
    protected MessageSource messageSource;

    @Inject
    @Named("entityDao")
    EntityDao                       entityDao;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;
    
    @Inject
    @Named("formDefinitionDao")
    private FormDefinitionDao formDefinitionDao; 
    
    @Inject
 	@Named("formDefinitionUtility")
 	protected FormDefinitionUtility formDefinitionUtility;

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService        makerCheckerService;
    
    @Inject
    @Named("IDynamicFormValidationService")
    private IDynamicFormValidationService dynamicFormValidationService;
    
    @Inject
    @Named("baseMasterService")
	private BaseMasterService baseMasterService;

    @Inject
    @Named("formConfigService")
    private FormService formService;

    @Inject
    @Named("lovConfigurationLoader")
    private LOVConfigurationLoader lovConfigurationLoader;
    
    @Override
    public List<FormConfigEntityData> getEntityNameList() {
        return entityDao.findAll(FormConfigEntityData.class);
    }

    @Override
    public List<LovConfig> getLovKeyList() {

        List<String> lovKeyList = lovConfigurationLoader.getListOfConfiguredLovKeys();
        List<LovConfig> lovConfigVOList = new ArrayList<>();

        if(lovKeyList == null){
            lovKeyList = new ArrayList<>();
        }

        for(String lovKey:lovKeyList){
            LovConfig lovConfigVO = new LovConfig();
            lovConfigVO.setKey(lovKey);
            lovConfigVOList.add(lovConfigVO);
        }

        return lovConfigVOList;
    }

    @Override
    public LovConfig getLovConfigForLovKey(String lovKey) {
        return lovConfigurationLoader.getConfiguration(lovKey);
    }
    
    @Override
	public List<FormConfigEntityData> getCascadeEntityData() {
    	NamedQueryExecutor<FormConfigEntityData> formConfigDataCriteria = new NamedQueryExecutor<FormConfigEntityData>("dynamicForm.getCascadeFormConfigData");
    	List<FormConfigEntityData> formConfigEntityDataList = entityDao.executeQuery(formConfigDataCriteria);
    	if(null != formConfigEntityDataList && !formConfigEntityDataList.isEmpty()){
    		return formConfigEntityDataList;
    	}
		return null;
	}
    
    @Override
    public List<FormConfigEntityData> getChildCascadeEntityData(String entityName){
    	NamedQueryExecutor<FormConfigEntityData> childFormConfigDataCriteria = new NamedQueryExecutor<FormConfigEntityData>("dynamicForm.getCascadeChildFormConfigData").addParameter("entityName",entityName);
    	return entityDao.executeQuery(childFormConfigDataCriteria);
    }

    @Override
    public Map<String, String> getChildCascadeDropdownData(String entityName, String parentName, Long parentId) {

        if(notNull(entityName) && notNull(parentName) && notNull(parentId)) {
        	String url = entityName + "/" + parentName;
            NamedQueryExecutor<Map<String,String>> executor = new NamedQueryExecutor<Map<String,String>>("dynamicForm.getItemLabelAndItemName").addParameter("entityName",entityName).addParameter("url", url);
            Map<String,String> itemLabelAndValue = entityDao.executeQueryForSingleValue(executor);

            if (notNull(itemLabelAndValue)) {
                String itemLabel = itemLabelAndValue.get("itemLabel");
                String itemValue = itemLabelAndValue.get("itemValue");
                String queryChildData = "SELECT new Map(childEntity." + itemValue + " as itemValue, childEntity." + itemLabel + " as itemLabel)  FROM " + entityName + " childEntity WHERE childEntity." + parentName + ".id = :id AND childEntity.masterLifeCycleData.approvalStatus in (:approvalStatus)";
                JPAQueryExecutor<Map<String,Object>> executorNew = new JPAQueryExecutor<>(queryChildData);
                executorNew.addParameter("id", parentId);
                executorNew.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
                List<Map<String,Object>> cascadeDropdownDataList = entityDao.executeQuery(executorNew);
                if(CollectionUtils.isNotEmpty(cascadeDropdownDataList)){
                    Map<String, String> cascadeDropdownData = new LinkedHashMap<>();
                    for(int i=0; i<cascadeDropdownDataList.size(); i++){
                        if(notNull(cascadeDropdownDataList.get(i)) && notNull(cascadeDropdownDataList.get(i).get("itemValue")) && notNull(cascadeDropdownDataList.get(i).get("itemLabel"))) {
                            cascadeDropdownData.put(cascadeDropdownDataList.get(i).get("itemValue").toString(), cascadeDropdownDataList.get(i).get("itemLabel").toString());
                        }
                    }
                    return cascadeDropdownData;
                }
            }
        }
        return new LinkedHashMap<>();
    }
    
    @Override
    public Map<String, String> getChildCascadeDropdownData(String dynamicForm,String entityName,String binderName, String fieldKey, Long parentId) {

        if(notNull(entityName) && notNull(fieldKey) && notNull(parentId)) {
            NamedQueryExecutor<Map<String,String>> executor = new NamedQueryExecutor<Map<String,String>>("dynamicForm.getFieldConfigFromFormNameFieldNameAndEntityType")
            		.addParameter("formName",dynamicForm)
            		.addParameter("fieldKey",fieldKey)
            		.addParameter("entityName", "%"+entityName+"%")
            		.addParameter("binderName", binderName)
            		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_IN_APPROVED_APPROVED_MODIFIED);
            FieldDefinition fieldDefinition = (FieldDefinition) entityDao.executeQueryForSingleValue(executor);
            if(null != fieldDefinition){
            	String whereList="";
            	if(null != fieldDefinition.getCustomCascadeOptions() && !fieldDefinition.getCustomCascadeOptions().isEmpty()){
            		for(int i=0;i<fieldDefinition.getCustomCascadeOptions().size();i++){
            			CustomCascadeOptions cascadeOptions = fieldDefinition.getCustomCascadeOptions().get(i);
            			if(null !=cascadeOptions.getParentIdList() && !"".equals(cascadeOptions.getParentIdList()) && cascadeOptions.getParentIdList().contains(String.valueOf(parentId))){
            				if(null != cascadeOptions.getChildIdList() && !"".equals(cascadeOptions.getChildIdList()))
            					whereList = cascadeOptions.getChildIdList().replace("[", "").replace("]", "");
            			}
            		}
            	}
            	
                String itemLabel = fieldDefinition.getItemLabel();
                String itemValue = fieldDefinition.getItemValue();
                List<String> dynamicgenericTypes = genericParameterService.findAllDynamicGenericParameter();                
                String queryChildData = "SELECT new Map(childEntity." + itemValue + " as itemValue, childEntity." + itemLabel + " as itemLabel)  FROM " ;
                if("".equals(whereList)){
                	if("GenericParameter".equalsIgnoreCase(entityName)){
                		if(null != dynamicgenericTypes  && dynamicgenericTypes.contains(binderName)){
                    		queryChildData = queryChildData + entityName +" childEntity where childEntity.dynamicParameterName='"+binderName+"'";	
                        }else{
                        	queryChildData = queryChildData + binderName +" childEntity";
                        }
                    }else{
                    	queryChildData = queryChildData + entityName +" childEntity";
                    }
                	
                }else{
                	queryChildData = queryChildData + entityName +" childEntity where childEntity."+itemValue+" in ("+whereList+")";
                }
                //String queryChildData = "SELECT new Map(childEntity." + itemValue + " as itemValue, childEntity." + itemLabel + " as itemLabel)  FROM " + entityName + " childEntity WHERE childEntity." + parentName + ".id = :id AND childEntity.masterLifeCycleData.approvalStatus in (:approvalStatus)";
                
                JPAQueryExecutor<Map<String,Object>> executorNew = new JPAQueryExecutor<>(queryChildData);
                List<Map<String,Object>> cascadeDropdownDataList = entityDao.executeQuery(executorNew);
                if(CollectionUtils.isNotEmpty(cascadeDropdownDataList)){
                    Map<String, String> cascadeDropdownData = new LinkedHashMap<>();
                    for(int i=0; i<cascadeDropdownDataList.size(); i++){
                        if(notNull(cascadeDropdownDataList.get(i)) && notNull(cascadeDropdownDataList.get(i).get("itemValue")) && notNull(cascadeDropdownDataList.get(i).get("itemLabel"))) {
                            cascadeDropdownData.put(cascadeDropdownDataList.get(i).get("itemValue").toString(), cascadeDropdownDataList.get(i).get("itemLabel").toString());
                        }
                    }
                    return cascadeDropdownData;
                }
            }
        }
        return null;
    }

    @Override
    public List<FieldDataType> getDynamicFormFieldDataType() {
        return entityDao.findAll(FieldDataType.class);
    }

    @Override
    public FormConfigEntityData getFormConfigData(String entityName) {
        NamedQueryExecutor<FormConfigEntityData> formConfigDataCriteria = new NamedQueryExecutor<FormConfigEntityData>(
                "dynamicForm.getFormConfigData").addParameter("entityName", entityName);
        List<FormConfigEntityData> formConfigDataList = entityDao.executeQuery(formConfigDataCriteria);

        if (null != formConfigDataList && !formConfigDataList.isEmpty()) {
            return formConfigDataList.get(0);
        }
        return null;
    }
    @Override
    public FormConfigEntityData getFormConfigData(String entityName, Long parentId) {
    	if(parentId == null){
    		return getFormConfigData(entityName);
    	}
    	
        NamedQueryExecutor<FormConfigEntityData> formConfigDataCriteria = new NamedQueryExecutor<FormConfigEntityData>(
                "dynamicForm.getFormConfigData1").addParameter("entityName", entityName).addParameter("parent", parentId);
        List<FormConfigEntityData> formConfigDataList = entityDao.executeQuery(formConfigDataCriteria);

        if (null != formConfigDataList && !formConfigDataList.isEmpty()) {
            return formConfigDataList.get(0);
        }
        return null;
    }
    
    @Override
    public FormConfigEntityData getFormConfigDataByParentChild(String childEntityName, String parentEntityName) {
    	String query = "Select fcdChild.id from FORM_CONFIG_ENTITY_DATA fcdChild  , FORM_CONFIG_ENTITY_DATA fcdParent WHERE fcdParent.id = fcdChild.parent"
    			+ " and fcdParent.web_data_binder_name = :parentEntity and fcdChild.entity_Name = :childEntity";
    	
    	NativeQueryExecutor<Long> formNativQueryExecutor = new NativeQueryExecutor<Long>(query);
    	formNativQueryExecutor.addParameter("parentEntity", parentEntityName);
    	formNativQueryExecutor.addParameter("childEntity", childEntityName);
    	
    	
       /* NamedQueryExecutor<FormConfigEntityData> formConfigDataCriteria = new NamedQueryExecutor<FormConfigEntityData>(
                "dynamicForm.getCascadeChildFormConfigDataSelfJoin").addParameter("parentEntityName", parentEntityName)
        		.addParameter("childEntityName", childEntityName);*/
        List<Long> ids = entityDao.executeQuery(formNativQueryExecutor);
        
        if (null != ids && !ids.isEmpty()) {
            return entityDao.find(FormConfigEntityData.class, Long.parseLong(String.valueOf(ids.get(0))));
        }
        return null;
    }


    @Override
    public ModelMetaData saveModelMetaData(FormVO formVO,User user) {
        if (null != formVO) {
            FormConfigurationMapping formConfigurationMapping = null;
            if(formVO.getId() != null) {
                formConfigurationMapping = entityDao.find(FormConfigurationMapping.class, formVO.getId());
            } else if(formVO.getUiMetaDataId() != null) {
                formConfigurationMapping = loadFormConfigMapping(formVO.getUiMetaDataId());
            }
            ModelMetaData modelMetaData = null;
            List<FieldMetaData> fieldMetaDatas = null;
            ModelMetaData modelMetaDataExist;
            if (formVO.getModelMetaDataId() != null) {
                modelMetaDataExist = entityDao.find(ModelMetaData.class, formVO.getModelMetaDataId());
            } else {
                modelMetaDataExist = new ModelMetaData();
            }

            if ((null != formVO.getCreateNewVersion() && formVO.getCreateNewVersion())
                    || (formConfigurationMapping != null
                    && ApprovalStatus.UNAPPROVED_ADDED != formConfigurationMapping.getApprovalStatus()
                    && ApprovalStatus.UNAPPROVED_MODIFIED != formConfigurationMapping.getApprovalStatus())) {

                modelMetaData = new ModelMetaData();
                fieldMetaDatas = new ArrayList<FieldMetaData>();

            } else {

                if (formVO.getModelMetaDataId() != null) {
                    modelMetaData = modelMetaDataExist;
                    if (modelMetaData != null && modelMetaData.getFields() != null) {
                        modelMetaData.getFields().clear();
                        fieldMetaDatas = modelMetaData.getFields();
                    } else {
                        fieldMetaDatas = new ArrayList<FieldMetaData>();
                    }
                } else {
                    modelMetaData = new ModelMetaData();
                    fieldMetaDatas = new ArrayList<FieldMetaData>();
                }
            }

            modelMetaData.setName(FormConfigurationConstant.MODEL_NAME + formVO.getFormName());
            modelMetaData.setDescription(FormConfigurationConstant.MODEL_DESCRIPTION + formVO.getFormName());

            for (FormContainerVO formContainerVO : formVO.getContainerVOList()) {

                if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_FIELD) {

                    FieldMetaData fieldMetaData = new FieldMetaData();
                    fieldMetaData.setDataType(formContainerVO.getFieldDataType());
                    fieldMetaData.setName(formContainerVO.getFieldLabel());
                    fieldMetaData.setFieldKey(formContainerVO.getFieldKey());
                    fieldMetaDatas.add(fieldMetaData);

                } else if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_PANEL
                        || formContainerVO.getType() == FormContainerType.FIELD_TYPE_TABLE
                        || formContainerVO.getType() == FormContainerType.FIELD_TYPE_VIRTUAL
                        || formContainerVO.getType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE) {

                    for (FormContainerVO formContainerVO1 : formContainerVO.getFormContainerVOList()) {

                        FieldMetaData fieldMetaData1 = new FieldMetaData();
                        fieldMetaData1.setDataType(formContainerVO1.getFieldDataType()!=null ? formContainerVO1.getFieldDataType():FieldDataType.DATA_TYPE_TEXT);
                        fieldMetaData1.setName(formContainerVO1.getFieldLabel());
                        fieldMetaData1.setFieldKey(formContainerVO1.getFieldKey());

                        if ((formContainerVO.getType() == FormContainerType.FIELD_TYPE_TABLE) || (formContainerVO.getType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE)) {
                            fieldMetaData1.setMultiValued(true);
                        }

                        fieldMetaDatas.add(fieldMetaData1);
                    }

                }
            }

            modelMetaData.setModeluuid(formVO.getFormuuid());
            modelMetaData.setModelVersion(formVO.getFormVersion());

            modelMetaData.setFields(fieldMetaDatas);
            modelMetaData.setApprovalStatus(ApprovalStatus.APPROVED);

            modelMetaData = (ModelMetaData)makerCheckerService.updateBaseEntityLifeCycleData(modelMetaData, user);

            entityDao.saveOrUpdate(modelMetaData);
            return modelMetaData;
        }

        return null;
    }

    @Override
    public UIMetaData saveUIMetaData(FormVO formVO, String modelUri,User user) {
        boolean prevComponentIsPanel = false;
        Boolean fieldProcessingDone = true;
        FormConfigurationMapping formConfigurationMapping = null;
        if(formVO.getId() != null) {
            formConfigurationMapping = entityDao.find(FormConfigurationMapping.class, formVO.getId());
        } else if(formVO.getUiMetaDataId() != null) {
            formConfigurationMapping = loadFormConfigMapping(formVO.getUiMetaDataId());
        }
        UIMetaData uiMetaData = null;
        UIMetaData uiMetaDataExist;
        if (formVO.getModelMetaDataId() != null) {
            uiMetaDataExist = entityDao.find(UIMetaData.class, formVO.getUiMetaDataId());
        } else {
            uiMetaDataExist = new UIMetaData();
        }
        List<PanelDefinition> panelDefinitionList = null;

        if ((null != formVO.getCreateNewVersion() && formVO.getCreateNewVersion())|| (formConfigurationMapping != null
                && ApprovalStatus.UNAPPROVED_ADDED != formConfigurationMapping.getApprovalStatus()
                && ApprovalStatus.UNAPPROVED_MODIFIED != formConfigurationMapping.getApprovalStatus())) {

            uiMetaData = new UIMetaData();
            panelDefinitionList = new ArrayList<PanelDefinition>();

        } else {

            if (formVO.getUiMetaDataId() != null) {
                uiMetaData = uiMetaDataExist;
                if (uiMetaDataExist != null && uiMetaData.getPanelDefinitionList() != null) {
                    uiMetaData.getPanelDefinitionList().clear();
                    panelDefinitionList = uiMetaData.getPanelDefinitionList();
                } else {
                    panelDefinitionList = new ArrayList<PanelDefinition>();
                }
            } else {
                uiMetaData = new UIMetaData();
                panelDefinitionList = new ArrayList<PanelDefinition>();
            }

        }

        PanelDefinition panelDefinition = new PanelDefinition();
        List<FieldDefinition> fieldDefinitionList = new ArrayList<FieldDefinition>();

        FormConfigEntityData formConfigEntityData = null;
        String packageName = null;

        uiMetaData.setFormHeader(formVO.getFormHeader());
        uiMetaData.setFormName(formVO.getFormName());
        uiMetaData.setFormTitle(formVO.getFormTitle());
        uiMetaData.setModelName(FormConfigurationConstant.MODEL_NAME + formVO.getFormName());
        uiMetaData.setModelUri(modelUri);
        uiMetaData.setFormDescription(formVO.getFormDescription());
        uiMetaData.setAllowSaveOption(formVO.getAllowSaveOption());
        uiMetaData.setAllowBorder(formVO.getAllowBorder());
        uiMetaData.setFormuuid(formVO.getFormuuid());
        uiMetaData.setFormVersion(formVO.getFormVersion());
        uiMetaData.setCreateNewVersion(formVO.getCreateNewVersion());
        if(formVO.getValidationsVO() !=null){
        	// removing deleted member
        	for (Iterator iterator = formVO.getValidationsVO().iterator(); iterator.hasNext();) {
				FormValidationMetadataVO validationVO = (FormValidationMetadataVO) iterator.next();
				if(validationVO == null){
					iterator.remove();
				}else{

					if(validationVO.getIfConditions() !=null){
						for (Iterator iterator2 = validationVO.getIfConditions().iterator(); iterator2.hasNext();) {
							FormValidationRulesIFMetadataVO fieldDefinition = (FormValidationRulesIFMetadataVO) iterator2.next();
							if(fieldDefinition == null){
								iterator2.remove();
							}else if(fieldDefinition.getRightOperandFieldKey() !=null && fieldDefinition.getRightOperandFieldKey().getExpressionType() !=null
									&& fieldDefinition.getRightOperandFieldKey().getExpressionType().equals(FormValidationConstants.IfOperandExpressionType.CONSTANT_VALUE.toString())
									&& fieldDefinition.getRightOperandFieldKey().getExpression() !=null){
								fieldDefinition.getRightOperandFieldKey().setExpression(StringEscapeUtils.escapeHtml4(fieldDefinition.getRightOperandFieldKey().getExpression()));
							}
						}
					}
					if(validationVO.getThenActions() !=null){
						for (Iterator iterator2 = validationVO.getThenActions().iterator(); iterator2.hasNext();) {
							FormValidationRulesThenMetadataVO fieldDefinition = (FormValidationRulesThenMetadataVO) iterator2.next();
							if(fieldDefinition == null){
								iterator2.remove();
							}else if(fieldDefinition.getTypeOfAction()!=null && fieldDefinition.getTypeOfAction().equals(FormValidationConstants.ThenActionTypes.SHOW_MESSAGE.toString())
									&& fieldDefinition.getAction().getExpression()!=null){
								fieldDefinition.getAction().setExpression(StringEscapeUtils.escapeHtml4(fieldDefinition.getAction().getExpression()));
							}
						}
					}
				}
			}
        	uiMetaData.setFormValidationsRulesInJSON(new JSONSerializer().deepSerialize(formVO.getValidationsVO()));
        	try {
				uiMetaData.setFormValidationRulesInJS(dynamicFormValidationService.createJavaScriptForValidation(formVO, formVO.getValidationsVO()));
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error("Error in creating Validation Rules in JS",e);	
			}
        }
        if(notNull(formVO.getDynamicFormMapperVOList())) {
            List<DynamicFormMapper> dynamicFormMapperList = new ArrayList<>();
            for(DynamicFormMapperVO dynamicFormMapperVO : formVO.getDynamicFormMapperVOList()) {
                DynamicFormMapper dynamicFormMapper = new DynamicFormMapper();
                dynamicFormMapper.setProductProcessor(dynamicFormMapperVO.getProductProcessor());
                dynamicFormMapper.setFormFields(dynamicFormMapperVO.getSelectedFields());
                dynamicFormMapperList.add(dynamicFormMapper);
            }
            if(null == uiMetaData.getDynamicFormMapperList()){
            	uiMetaData.setDynamicFormMapperList(new ArrayList<>());
            }
            uiMetaData.getDynamicFormMapperList().clear();
            uiMetaData.getDynamicFormMapperList().addAll(dynamicFormMapperList);
        }
        if (notNull(formVO.getDedupeMapperVO())) {
            DynamicCollDedupeConfig dynamicCollDedupeConfig = uiMetaData.getDynamicCollDedupeConfig();
            if (dynamicCollDedupeConfig == null) {
                dynamicCollDedupeConfig = new DynamicCollDedupeConfig();
            }
            
            dynamicCollDedupeConfig.setPathField1(formVO.getDedupeMapperVO().getPathField1());
            dynamicCollDedupeConfig.setPathField2(formVO.getDedupeMapperVO().getPathField2());
            dynamicCollDedupeConfig.setPathField3(formVO.getDedupeMapperVO().getPathField3());
            dynamicCollDedupeConfig.setPathField4(formVO.getDedupeMapperVO().getPathField4());
            dynamicCollDedupeConfig.setPathField5(formVO.getDedupeMapperVO().getPathField5());
            dynamicCollDedupeConfig.setPathField6(formVO.getDedupeMapperVO().getPathField6());
            dynamicCollDedupeConfig.setScoreField1(formVO.getDedupeMapperVO().getScoreField1());
            dynamicCollDedupeConfig.setScoreField2(formVO.getDedupeMapperVO().getScoreField2());
            dynamicCollDedupeConfig.setScoreField3(formVO.getDedupeMapperVO().getScoreField3());
            dynamicCollDedupeConfig.setScoreField4(formVO.getDedupeMapperVO().getScoreField4());
            dynamicCollDedupeConfig.setScoreField5(formVO.getDedupeMapperVO().getScoreField5());
            dynamicCollDedupeConfig.setScoreField6(formVO.getDedupeMapperVO().getScoreField6());
            uiMetaData.setDynamicCollDedupeConfig(dynamicCollDedupeConfig);

        }
        
        if(notNull(formVO.getSourceProduct())){
            uiMetaData.setSourceProductId(formVO.getSourceProduct().getId());
        }
        try {
            String previousJSData=uiMetaData.getFormValidationRulesInJS();
            String newJSData=dynamicFormValidationService.createJavaScriptForTimeStamp(formVO);
            if(StringUtils.isNotEmpty(previousJSData) && CollectionUtils.isNotEmpty(formVO.getValidationsVO())){
                StringBuilder jsDataAppender=new StringBuilder(previousJSData);
                jsDataAppender.append("\n");
                jsDataAppender.append(newJSData);
                uiMetaData.setFormValidationRulesInJS(jsDataAppender.toString());
            }else {
                uiMetaData.setFormValidationRulesInJS(newJSData);
            }

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in creating Validation Rules in JS",e);
        }
        for (FormContainerVO formContainerVO : formVO.getContainerVOList()) {

            if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_FIELD) {

                if (prevComponentIsPanel) {
                    panelDefinition = new PanelDefinition();
                    fieldDefinitionList = new ArrayList<FieldDefinition>();
                }

                FieldDefinition fieldDefinition = new FieldDefinition();
                if(null != formContainerVO.getParent()){
                	fieldDefinition.setParent(formContainerVO.getParent());
            	}
                if(null != formContainerVO.getDisable()){
                	fieldDefinition.setDisable(formContainerVO.getDisable());
            	}else{
            		fieldDefinition.setDisable(Boolean.FALSE);
            	}
                

                fieldDefinition.setDescription(formContainerVO.getDescription());
                fieldDefinition.setBinderName(formContainerVO.getBinderName());
                
                if (formContainerVO.getBinderName() != null
                        && !(formContainerVO.getBinderName().equals(FormConfigurationConstant.CUSTOM_BINDER))) {
                    fieldDefinition.setItemLabel(formContainerVO.getItemLabel());
                    fieldDefinition.setItemValue(formContainerVO.getItemValue());
                }
                
                int id=getIdFromComponentKey(formContainerVO.getComponentDisplayKey());
                fieldDefinition.setFieldSequence(id);
                fieldDefinition.setFieldKey(formContainerVO.getFieldKey());
                fieldDefinition.setFieldLabel(formContainerVO.getFieldLabel());
                fieldDefinition.setFieldType(formContainerVO.getFieldType());
                fieldDefinition.setMandatoryField(formContainerVO.isMandatoryField());
                fieldDefinition.setExpandableField(formContainerVO.getExpandableField());
              //  fieldDefinition.setHideField(formContainerVO.isHideField());
                fieldDefinition.setIncludeSelect(formContainerVO.isIncludeSelect());
                fieldDefinition.setValue(formContainerVO.getDefaultValue());
                fieldDefinition.setToolTipMessage(formContainerVO.getToolTipMessage());
                fieldDefinition.setDefaultMonth(formContainerVO.getDefaultMonth());
                fieldDefinition.setDefaultYear(formContainerVO.getDefaultYear());
                fieldDefinition.setMinFieldLength(formContainerVO.getMinFieldLength());
                fieldDefinition.setMaxFieldLength(formContainerVO.getMaxFieldLength());
                fieldDefinition.setMinFieldValue(formContainerVO.getMinFieldValue());
                fieldDefinition.setMaxFieldValue(formContainerVO.getMaxFieldValue());
                fieldDefinition.setAssociatedFieldKey(formContainerVO.getAssociatedFieldKey());
                fieldDefinition.setLovKey(formContainerVO.getLovKey());
                /**
                 * added for hyperlink type
                */
                fieldDefinition.setHref(formContainerVO.getHref());
                fieldDefinition.setFunctionLogic(formContainerVO.getFunctionLogic());
                fieldDefinition.setAuthority(formContainerVO.getAuthority());
                
                /**
                 *  added for dependant autocomplete
                 */
                fieldDefinition.setParentColumn(formContainerVO.getParentColumn());
                fieldDefinition.setErrorMessageCode(formContainerVO.getErrorMessageCode());
                fieldDefinition.setParentFieldId(formContainerVO.getParentFieldId());
                fieldDefinition.setMainFormDependant(formContainerVO.isMainFormDependant());
                
                if (null != formContainerVO.getEntityName()) {
                    formConfigEntityData = getFormConfigData(formContainerVO.getEntityName());
                    if (null != formConfigEntityData) {
                        packageName = formConfigEntityData.getPackageName();
                    }

                    fieldDefinition.setEntityName(packageName);
                }
                if (formContainerVO.getFieldType().equals(FormComponentType.AUTOCOMPLETE)) {
                    fieldDefinition.setItemLabel(formConfigEntityData.getItemLabel());
                    fieldDefinition.setItemValue(formConfigEntityData.getItemValue());
                    List<String> autoCompleteColumnsHolder = formContainerVO.getAutoCompleteColumnsHolder();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String column : autoCompleteColumnsHolder) {
                        stringBuilder.append(column);
                        stringBuilder.append(",");
                    }
                    fieldDefinition.setAutoCompleteColumnsHolder(stringBuilder.toString());
                }
                if (formContainerVO.getFieldType().equals(FormComponentType.PHONE) &&
                    formContainerVO.getPhoneNumberVO().getNumberTypeVO() != null
                            && formContainerVO.getPhoneNumberVO().getNumberTypeVO().getCode()
                                    .equals(PhoneNumberTypeVO.MOBILE_NUMBER)) {
                        fieldDefinition.setMobile(true);
                }else{
                	fieldDefinition.setMobile(false);
                }
                if (formContainerVO.getFieldType().equals(FormComponentType.EMAIL) &&
                    formContainerVO.getEmailInfoVO() != null
                            && formContainerVO.getEmailInfoVO().getEmailTypeVO() != null) {
                        fieldDefinition.setEmailTypeCode(formContainerVO.getEmailInfoVO().getEmailTypeVO().getCode());
                }
                if(formContainerVO.getFieldType().equals(FormComponentType.CASCADED_SELECT) || formContainerVO.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
                	if(formContainerVO.getParentFieldKey() != null && (!formContainerVO.getParentFieldKey().isEmpty()))
                		fieldDefinition.setParentFieldKey(formContainerVO.getParentFieldKey());
                	if(formContainerVO.getCurrentChildEntityName() != null)
                		fieldDefinition.setActiveChildEntityName(formContainerVO.getCurrentChildEntityName());
                    if(formContainerVO.getUrlCascadeSelect() != null)
                        fieldDefinition.setUrlCascadeSelect(formContainerVO.getUrlCascadeSelect());
                    if(null !=formContainerVO.getFirstParent() && formContainerVO.getFirstParent()){
                    	fieldDefinition.setFirstParent(formContainerVO.getFirstParent());
                    }
                    if(formContainerVO.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT) && formContainerVO.getParent()!=null){
                    	fieldDefinition.setParent(formContainerVO.getParent());
                 	   if(null != formContainerVO.getParentChildForms() && !formContainerVO.getParentChildForms().isEmpty()){
                 		   List<CustomCascadeOptions> childFileds = new ArrayList<>();
                 		  formContainerVO.getParentChildForms().forEach(parentChildForm ->{
                 			   CustomCascadeOptions fileds = new CustomCascadeOptions();
                 			   fileds.setParentIdList(Arrays.toString(parentChildForm.getParentIds()));
                 			   fileds.setChildIdList(Arrays.toString(parentChildForm.getChildIds()));
                 			   childFileds.add(fileds);
                 		   });
                 		   if(!childFileds.isEmpty()){
                 			  fieldDefinition.setCustomCascadeOptions(childFileds);
                 		   }
                 	   }
                 	  fieldDefinition.setUrlCascadeSelect(formVO.getFormName()+"/"+formContainerVO.getEntityName()+"/"+formContainerVO.getBinderName()+"/"+formContainerVO.getFieldKey());
               		  fieldDefinition.setParentFieldKey(formContainerVO.getParent());   
                    }
                }

                if(formContainerVO.getFieldType().equals(FormComponentType.CURRENT_TIME_STAMP)){
                    fieldDefinition.setFieldDataType(FieldDataType.DATA_TYPE_TEXT);
                }else {
                    fieldDefinition.setFieldDataType(formContainerVO.getFieldDataType());
                }

                fieldDefinition.setCustomeLongMessage(formContainerVO.getCustomeLongMessage());

                if (formContainerVO.getFieldCustomOptionsList() != null
                        && !formContainerVO.getFieldCustomOptionsList().isEmpty()) {

                    List<FieldCustomOptions> customOptionsList = new ArrayList<FieldCustomOptions>();

                    for (FieldCustomOptionsVO fieldCustomOptionsVO : formContainerVO.getFieldCustomOptionsList()) {
                        FieldCustomOptions customOptions = new FieldCustomOptions();
                        customOptions.setCustomeItemLabel(fieldCustomOptionsVO.getCustomeItemLabel());
                        customOptions.setCustomeItemValue(fieldCustomOptionsVO.getCustomeItemValue());
                        customOptionsList.add(customOptions);
                    }
                    fieldDefinition.setFieldCustomOptionsList(customOptionsList);
                }

                fieldDefinitionList.add(fieldDefinition);

                fieldProcessingDone = false;

                prevComponentIsPanel = false;

            } else if (formContainerVO.getType() == FormContainerType.FIELD_TYPE_PANEL
                    || formContainerVO.getType() == FormContainerType.FIELD_TYPE_TABLE || formContainerVO.getType() == FormContainerType.FIELD_TYPE_VIRTUAL
                    || formContainerVO.getType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE) {

                if (!fieldProcessingDone) {
                    updateFieldProcessing(panelDefinitionList, panelDefinition, fieldDefinitionList);
                }

                prevComponentIsPanel = true;

                panelDefinition = new PanelDefinition();
                fieldDefinitionList = new ArrayList<FieldDefinition>();

                for (FormContainerVO formContainerVO1 : formContainerVO.getFormContainerVOList()) {

                    FieldDefinition fieldDefinition1 = new FieldDefinition();
                    if(null != formContainerVO1.getParent()){
                    	fieldDefinition1.setParent(formContainerVO1.getParent());
                	}
                    fieldDefinition1.setBinderName(formContainerVO1.getBinderName());

                    if (formContainerVO1.getBinderName() != null
                            && !(formContainerVO1.getBinderName().equals(FormConfigurationConstant.CUSTOM_BINDER))) {
                        fieldDefinition1.setItemLabel(formContainerVO1.getItemLabel());
                        fieldDefinition1.setItemValue(formContainerVO1.getItemValue());
                    }
                    int id=getIdFromComponentKey(formContainerVO1.getComponentDisplayKey());
                    fieldDefinition1.setFieldSequence(id);
                    fieldDefinition1.setFieldKey(formContainerVO1.getFieldKey());
                    fieldDefinition1.setFieldLabel(formContainerVO1.getFieldLabel());
                    fieldDefinition1.setFieldType(formContainerVO1.getFieldType());
                    fieldDefinition1.setMandatoryField(formContainerVO1.isMandatoryField());
                    fieldDefinition1.setExpandableField(formContainerVO1.getExpandableField());
                //    fieldDefinition1.setHideField(formContainerVO1.isHideField());
                    fieldDefinition1.setIncludeSelect(formContainerVO1.isIncludeSelect());
                    fieldDefinition1.setValue(formContainerVO1.getDefaultValue());
                    fieldDefinition1.setToolTipMessage(formContainerVO1.getToolTipMessage());
                    fieldDefinition1.setDefaultMonth(formContainerVO1.getDefaultMonth());
                    fieldDefinition1.setDefaultYear(formContainerVO1.getDefaultYear());
                    fieldDefinition1.setMinFieldLength(formContainerVO1.getMinFieldLength());
                    fieldDefinition1.setMaxFieldLength(formContainerVO1.getMaxFieldLength());
                    fieldDefinition1.setMinFieldValue(formContainerVO1.getMinFieldValue());
                    fieldDefinition1.setMaxFieldValue(formContainerVO1.getMaxFieldValue());
                    fieldDefinition1.setDescription(formContainerVO1.getDescription());
                    fieldDefinition1.setAssociatedFieldKey(formContainerVO1.getAssociatedFieldKey());
                    fieldDefinition1.setLovKey(formContainerVO1.getLovKey());
                    /**
                     * added for hyperlink type
                    */
                    fieldDefinition1.setHref(formContainerVO1.getHref());
                    fieldDefinition1.setFunctionLogic(formContainerVO1.getFunctionLogic());
                    fieldDefinition1.setAuthority(formContainerVO1.getAuthority());
                    
                    /**
                     *  added for dependant autocomplete
                     */
                    fieldDefinition1.setParentColumn(formContainerVO1.getParentColumn());
                    fieldDefinition1.setErrorMessageCode(formContainerVO1.getErrorMessageCode());
                    fieldDefinition1.setParentFieldId(formContainerVO1.getParentFieldId());
                    fieldDefinition1.setMainFormDependant(formContainerVO1.isMainFormDependant());
                    
                    if (null != formContainerVO1.getEntityName()) {
                        formConfigEntityData = getFormConfigData(formContainerVO1.getEntityName());
                        if (null != formConfigEntityData) {
                            packageName = formConfigEntityData.getPackageName();
                        }

                        fieldDefinition1.setEntityName(packageName);
                    }
                    if (formContainerVO1.getFieldType().equals(FormComponentType.AUTOCOMPLETE)) {
                        fieldDefinition1.setItemLabel(formConfigEntityData.getItemLabel());
                        fieldDefinition1.setItemValue(formConfigEntityData.getItemValue());
                        List<String> autoCompleteColumnsHolder = formContainerVO1.getAutoCompleteColumnsHolder();
                        StringBuilder stringBuilder = new StringBuilder();
                        if (null != autoCompleteColumnsHolder && !autoCompleteColumnsHolder.isEmpty()) {
                            for (String column : autoCompleteColumnsHolder) {
                                stringBuilder.append(column);
                                stringBuilder.append(",");
                            }
                        }
                        fieldDefinition1.setAutoCompleteColumnsHolder(stringBuilder.toString());
                    }
                    if (formContainerVO1.getFieldType().equals(FormComponentType.PHONE) &&
                        formContainerVO1.getPhoneNumberVO().getNumberTypeVO() != null
                                && formContainerVO1.getPhoneNumberVO().getNumberTypeVO().getCode()
                                        .equals(PhoneNumberTypeVO.MOBILE_NUMBER)) {
                            fieldDefinition1.setMobile(true);
                    }
                    if (formContainerVO1.getFieldType().equals(FormComponentType.EMAIL) &&
                        formContainerVO1.getEmailInfoVO() != null
                                && formContainerVO1.getEmailInfoVO().getEmailTypeVO() != null) {
                            fieldDefinition1.setEmailTypeCode(formContainerVO1.getEmailInfoVO().getEmailTypeVO().getCode());
                    }
                   if(formContainerVO1.getFieldType().equals(FormComponentType.CASCADED_SELECT) || formContainerVO1.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
                    	if(formContainerVO1.getParentFieldKey() != null && (!formContainerVO1.getParentFieldKey().isEmpty()))
                    		fieldDefinition1.setParentFieldKey(formContainerVO1.getParentFieldKey());
                    	if(formContainerVO1.getCurrentChildEntityName() != null)
                    		fieldDefinition1.setActiveChildEntityName(formContainerVO1.getCurrentChildEntityName());
                       if(formContainerVO1.getUrlCascadeSelect() != null)
                           fieldDefinition1.setUrlCascadeSelect(formContainerVO1.getUrlCascadeSelect());
                       if(null != formContainerVO1.getFirstParent() && formContainerVO1.getFirstParent()){
                    	   fieldDefinition1.setFirstParent(formContainerVO1.getFirstParent());
                       }
                       if(formContainerVO1.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT) && formContainerVO1.getParent()!=null){
                    	   fieldDefinition1.setParent(formContainerVO1.getParent());
                    	   if(null != formContainerVO1.getParentChildForms() && !formContainerVO1.getParentChildForms().isEmpty()){
                    		   List<CustomCascadeOptions> childFileds = new ArrayList<>();
                    		   formContainerVO1.getParentChildForms().forEach(parentChildForm ->{
                    			   CustomCascadeOptions fileds = new CustomCascadeOptions();
                    			   fileds.setParentIdList(Arrays.toString(parentChildForm.getParentIds()));
                    			   fileds.setChildIdList(Arrays.toString(parentChildForm.getChildIds()));
                    			   childFileds.add(fileds);
                    		   });
                    		   if(!childFileds.isEmpty()){
                    			   fieldDefinition1.setCustomCascadeOptions(childFileds);
                    		   }
                    	   }
                    	   fieldDefinition1.setUrlCascadeSelect(formVO.getFormName()+"/"+formContainerVO1.getEntityName()+"/"+formContainerVO1.getBinderName()+"/"+formContainerVO1.getFieldKey());
                   		   fieldDefinition1.setParentFieldKey(formContainerVO1.getParent());   
                    	   
                       }
                       
                    }
                   if(null != formContainerVO1.getDisable() && Boolean.TRUE.equals(formContainerVO1.getDisable())){
               		fieldDefinition1.setDisable(formContainerVO1.getDisable());
               		}else{
               		fieldDefinition1.setDisable(Boolean.FALSE);
               		}
                    if(formContainerVO1.getFieldType().equals(FormComponentType.DROP_DOWN)) {
                    	 if (StringUtils.isNotBlank(formContainerVO1.getProductScheme())) {
                            String key = formContainerVO1.getProductScheme();
                            if (key != null) {
                                ProductSchemeMetaData productScheme = formService.getProductSchemeMetaData(key,formContainerVO1.getFieldType());
                                fieldDefinition1.setProductSchemeMetaData(productScheme);
                            } else {
                                fieldDefinition1.setProductSchemeMetaData(null);
                            }
                        }

                    }

                    if (formContainerVO1.getFieldType().equals(FormComponentType.TEXT_BOX)) {
                        String keyy = formContainerVO1.getSpecialTable();
                        if(keyy!=null){
                            SpecialTable specialTable = formService.getSpecialTable(keyy);
                            fieldDefinition1.setSpecialTable(specialTable);
                        }else{
                            fieldDefinition1.setSpecialTable(null);
                        }
                        if (StringUtils.isNotBlank(formContainerVO1.getProductScheme())) {
                            String key = formContainerVO1.getProductScheme();
                            if(key!=null){
                                ProductSchemeMetaData productScheme = formService.getProductSchemeMetaData(key,formContainerVO1.getFieldType());
                                fieldDefinition1.setProductSchemeMetaData(productScheme);
                            }else{
                                fieldDefinition1.setProductSchemeMetaData(null);
                            }
                        }
                        if(formContainerVO1.getPopulateAssignmentResult()!=null && formContainerVO1.getPopulateAssignmentResult()) {
                            fieldDefinition1.setPopulateAssignmentResult(formContainerVO1.getPopulateAssignmentResult());
                            if (formContainerVO1.getAssignmentName() != null && !formContainerVO1.getAssignmentName().isEmpty()) {
                                fieldDefinition1.setAssignmentMasterCode(formContainerVO1.getAssignmentName());
                            }
                        }
                    }
                    if(formContainerVO1.getFieldType().equals(FormComponentType.CURRENT_TIME_STAMP)){
                        fieldDefinition1.setFieldDataType(FieldDataType.DATA_TYPE_TEXT);
                    }else {
                        fieldDefinition1.setFieldDataType(formContainerVO1.getFieldDataType());
                    }
                    fieldDefinition1.setCustomeLongMessage(formContainerVO1.getCustomeLongMessage());
                    if (formContainerVO1.getFieldCustomOptionsList() != null
                            && !formContainerVO1.getFieldCustomOptionsList().isEmpty()) {
                        List<FieldCustomOptions> customOptionsList = new ArrayList<FieldCustomOptions>();
                        for (FieldCustomOptionsVO fieldCustomOptionsVO : formContainerVO1.getFieldCustomOptionsList()) {
                            FieldCustomOptions customOptions = new FieldCustomOptions();
                            customOptions.setCustomeItemLabel(fieldCustomOptionsVO.getCustomeItemLabel());
                            customOptions.setCustomeItemValue(fieldCustomOptionsVO.getCustomeItemValue());
                            customOptionsList.add(customOptions);
                        }
                        fieldDefinition1.setFieldCustomOptionsList(customOptionsList);
                    }
                    fieldDefinitionList.add(fieldDefinition1);
                }
                panelDefinition.setPanelHeader(formContainerVO.getPanelHeader());
                panelDefinition.setPanelName(formContainerVO.getPanelHeader());
                panelDefinition.setFieldDefinitionList(fieldDefinitionList);
                panelDefinition.setPanelColumnLayout(formContainerVO.getPanelColumnLayout());
                panelDefinition.setPanelKey(formContainerVO.getFieldKey());
                panelDefinition.setPanelType(formContainerVO.getType());
                panelDefinitionList.add(panelDefinition);
                if(formContainerVO.getType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE){
                    String keyy = formContainerVO.getSpecialTable();
                    if(keyy!=null){
                        SpecialTable specialTable = formService.getSpecialTable(keyy);
                        panelDefinition.setSpecialTable(specialTable);
                        if(formContainerVO.getSpecialColumns().length>0){
                            StringBuilder stringBuilder = new StringBuilder();
                            for(String a:formContainerVO.getSpecialColumns()){
                                if(stringBuilder.length()>0){
                                    stringBuilder.append(",");
                                }
                                stringBuilder.append(a);
                            }
                            panelDefinition.setSavedSpColumn(stringBuilder.toString());

                        }
                        if(formContainerVO.getPartyRoles().length>0) {

                            StringBuilder stringBuilder = new StringBuilder();
                            for(String a:formContainerVO.getPartyRoles()){
                                if(stringBuilder.length()>0){
                                    stringBuilder.append(",");
                                }
                                stringBuilder.append(a);
                            }
                            panelDefinition.setSpecialTablePartyRoles(stringBuilder.toString());

                        }
                    }
                }else{
                    panelDefinition.setSpecialTable(null);
                    panelDefinition.setSavedSpColumn(null);
                }
                if(formContainerVO.getType() == FormContainerType.FIELD_TYPE_PANEL){
                    panelDefinition.setAllowPanelSave(formContainerVO.getAllowPanelSave());
                }
            }
        }

        if (!fieldProcessingDone) {
            updateFieldProcessing(panelDefinitionList, panelDefinition, fieldDefinitionList);
        }

        uiMetaData.setPanelDefinitionList(panelDefinitionList);
        uiMetaData.setApprovalStatus(ApprovalStatus.APPROVED);

        uiMetaData = (UIMetaData)makerCheckerService.updateBaseEntityLifeCycleData(uiMetaData, user);
        
        entityDao.saveOrUpdate(uiMetaData);


        return uiMetaData;
    }

    private int getIdFromComponentKey(String componentDisplayKey) {
    		
    	int y=0;
		componentDisplayKey=componentDisplayKey.replaceAll("[^0-9]+"," ");
		StringTokenizer strTok=new StringTokenizer(componentDisplayKey," ");
		if(strTok.hasMoreTokens())
		{
			if(strTok.countTokens()==1){
				y=Integer.parseInt(strTok.nextToken());
			}else{
				strTok.nextToken();
			}
		}
		if(strTok.hasMoreTokens())
		{
			y=Integer.parseInt(strTok.nextToken());
		}
		
		return y;
    
	}

	private void updateFieldProcessing(List<PanelDefinition> panelDefinitionList, PanelDefinition panelDefinition,
            List<FieldDefinition> fieldDefinitionList) {

        panelDefinition.setPanelHeader("");
        panelDefinition.setPanelName("");
        panelDefinition.setFieldDefinitionList(fieldDefinitionList);
        panelDefinition.setPanelType(FormContainerType.FIELD_TYPE_VIRTUAL);
        panelDefinition.setPanelColumnLayout(2);

        panelDefinitionList.add(panelDefinition);
    }

    @Override
    public FormConfigurationMapping saveFormConfigMapping(ModelMetaData modelMetaData, UIMetaData uiMetaData, FormVO formVO,User user) {
        FormConfigurationMapping formConfigurationMapping = null;
        FormConfigurationMapping newFormConfigurationMapping = new FormConfigurationMapping();

        if (!formVO.getCreateNewVersion()) {
            if (formVO.getId() != null) {
                formConfigurationMapping = entityDao.find(FormConfigurationMapping.class, formVO.getId());
            } else if (ValidatorUtils.notNull(formVO.getUiMetaDataId())) {
                formConfigurationMapping = loadFormConfigMapping(formVO.getUiMetaDataId());
            }
        }

        if (formConfigurationMapping != null) {
            newFormConfigurationMapping.setSourceProduct(formConfigurationMapping.getSourceProduct());
            newFormConfigurationMapping.setId(formConfigurationMapping.getId());
        }

        newFormConfigurationMapping.setModelMetaData(modelMetaData);
        newFormConfigurationMapping.setUiMetaData(uiMetaData);
        newFormConfigurationMapping.setInvocationPoint(formVO.getInvocationPoint());
        newFormConfigurationMapping.setActiveFlag(formVO.isActiveFlag());
        if(notNull(formVO.getSourceProduct())){
            newFormConfigurationMapping.setSourceProductId(formVO.getSourceProduct().getId());
        }
        return newFormConfigurationMapping;

    }

    /**
     * 
     * Method to save the new form definition against the invocation point
     * @param invocationPoint
     * @return
     */
    private FormConfigurationMapping getFormConfigByPoint(String invocationPoint) {
        FormConfigurationMapping formConfigurationMapping = null;

        NamedQueryExecutor<FormConfigurationMapping> formConfigDataCriteria = new NamedQueryExecutor<FormConfigurationMapping>(
                "dynamicForm.getUIMetaDataByInvocationPoint").addParameter("invocationPoint", invocationPoint);
        List<FormConfigurationMapping> formConfigurationMappingList = entityDao.executeQuery(formConfigDataCriteria);

        if (null != formConfigurationMappingList && !formConfigurationMappingList.isEmpty()) {
            formConfigurationMapping = formConfigurationMappingList.get(0);
        }
        return formConfigurationMapping;
    }

    @Override
    public DynamicFormNameStatus validateFormNameAndSourceProduct(FormVO formVo) {
        List<UIMetaData> uIMetaDatas = formDefinitionDao.fetchUiMetaDataBasedOnFormNameAndSourceProduct(formVo);
        
        if (hasElements(uIMetaDatas)) {
            for (UIMetaData uiMetaData : uIMetaDatas) {
                FormConfigurationMapping formConfigurationMapping;
                if(formVo.getId() != null) {
                    formConfigurationMapping = entityDao.find(FormConfigurationMapping.class, formVo.getId());
                } else {
                    formConfigurationMapping = loadFormConfigMapping(uiMetaData.getId());
                }
            	if(uiMetaData.getApprovalStatus()==ApprovalStatus.DELETED_APPROVED_IN_HISTORY){
            		return isFormMapped(uiMetaData)?DynamicFormNameStatus.DELETED_MAPPED:DynamicFormNameStatus.AVAILABLE;
            	}
                if (formConfigurationMapping != null && formConfigurationMapping.getApprovalStatus() != ApprovalStatus.CLONED
                        && uiMetaData.getFormuuid().equals(formVo.getFormuuid())) {
                    return DynamicFormNameStatus.AVAILABLE_FOR_UPDATE;
                } else {
                    return DynamicFormNameStatus.DUPLICATE;
                }
            }
        }
        return DynamicFormNameStatus.AVAILABLE;
    }
    
    private boolean isFormMapped(UIMetaData uiMetaData) {
		return formDefinitionUtility.isDynamicFormMapped(uiMetaData);
	}

    @Override
    public Boolean isDuplicateForm(FormVO formVo) {
        List<UIMetaData> uIMetaDatas = formDefinitionDao.fetchUiMetaDataBasedOnFormNameAndSourceProduct(formVo);
        if (hasElements(uIMetaDatas)) {
            for (UIMetaData uiMetaData : uIMetaDatas) {
                if ( uiMetaData.getFormName().equals(formVo.getFormName())) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
    

    @Override
    public void deleteDynamicForm(Long id) {
    	
    	UIMetaData uiMetaData = entityDao.find(UIMetaData.class, id);
    	 if (uiMetaData != null) {
    		 uiMetaData.setApprovalStatus(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
             entityDao.saveOrUpdate(uiMetaData);
         }
    }

    @Override
    public FormVO createVOFromRealObject(UIMetaData uiMetaData) {
        FormVO formVO = new FormVO();
        List<FormContainerVO> rootVOList = new ArrayList<FormContainerVO>();

        ModelMetaData modelMetaData = getModelMetaDataByUri(uiMetaData.getModelUri());
        // setting id into VO for Update operation
        formVO.setUiMetaDataId(uiMetaData.getId());
        formVO.setModelMetaDataId(modelMetaData.getId());

        if (null != uiMetaData) {
            formVO.setFormName(uiMetaData.getFormName());
            formVO.setFormHeader(uiMetaData.getFormHeader());
            formVO.setFormTitle(uiMetaData.getFormTitle());
            formVO.setFormDescription(uiMetaData.getFormDescription());
            formVO.setAllowSaveOption(uiMetaData.getAllowSaveOption());
            formVO.setAllowBorder(uiMetaData.getAllowBorder());
            formVO.setFormuuid(uiMetaData.getFormuuid());
            formVO.setFormVersion(uiMetaData.getFormVersion());
            formVO.setModelUri(uiMetaData.getModelUri());
            formVO.setSourceProduct(uiMetaData.getSourceProduct());
            formVO.setSourceProductId(uiMetaData.getSourceProductId());
            if(uiMetaData.getFormValidationsRulesInJSON()!=null && !uiMetaData.getFormValidationsRulesInJSON().isEmpty()){
            	formVO.setValidationsVO((List<FormValidationMetadataVO>) new JSONDeserializer().deserialize(uiMetaData.getFormValidationsRulesInJSON()));
            }
            
            if(uiMetaData.getDynamicCollDedupeConfig()!=null){
                DedupeMapperVO dedupeMapperVO=new DedupeMapperVO();
                dedupeMapperVO.setPathField1(uiMetaData.getDynamicCollDedupeConfig().getPathField1());
                dedupeMapperVO.setPathField2(uiMetaData.getDynamicCollDedupeConfig().getPathField2());
                dedupeMapperVO.setPathField3(uiMetaData.getDynamicCollDedupeConfig().getPathField3());
                dedupeMapperVO.setPathField4(uiMetaData.getDynamicCollDedupeConfig().getPathField4());
                dedupeMapperVO.setPathField5(uiMetaData.getDynamicCollDedupeConfig().getPathField5());
                dedupeMapperVO.setPathField6(uiMetaData.getDynamicCollDedupeConfig().getPathField6());                
                dedupeMapperVO.setScoreField1(uiMetaData.getDynamicCollDedupeConfig().getScoreField1());
                dedupeMapperVO.setScoreField2(uiMetaData.getDynamicCollDedupeConfig().getScoreField2());
                dedupeMapperVO.setScoreField3(uiMetaData.getDynamicCollDedupeConfig().getScoreField3());
                dedupeMapperVO.setScoreField4(uiMetaData.getDynamicCollDedupeConfig().getScoreField4());
                dedupeMapperVO.setScoreField5(uiMetaData.getDynamicCollDedupeConfig().getScoreField5());
                dedupeMapperVO.setScoreField6(uiMetaData.getDynamicCollDedupeConfig().getScoreField6());
                formVO.setDedupeMapperVO(dedupeMapperVO);
                
                Set<String> dedupeKeySet = new HashSet<String>();
                if (dedupeMapperVO != null) {
                    DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField1(), dedupeKeySet);
                    DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField2(), dedupeKeySet);
                    DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField3(), dedupeKeySet);
                    DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField4(), dedupeKeySet);
                    DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField5(), dedupeKeySet);
                    DynamicFormUtil.updateDedupe(dedupeMapperVO.getPathField6(), dedupeKeySet);
                }
                formVO.setDedupeKeySet(dedupeKeySet);
            }
            
            if(CollectionUtils.isNotEmpty(uiMetaData.getDynamicFormMapperList())) {
                List<DynamicFormMapperVO> dynamicFormMapperVOList = new ArrayList<>();
                for(DynamicFormMapper dynamicFormMapper : uiMetaData.getDynamicFormMapperList()) {
                    DynamicFormMapperVO dynamicFormMapperVO = new DynamicFormMapperVO();
                    dynamicFormMapperVO.setProductProcessor(dynamicFormMapper.getProductProcessor());
                    dynamicFormMapperVO.setSelectedFields(dynamicFormMapper.getFormFields());
                    dynamicFormMapperVOList.add(dynamicFormMapperVO);
                }
                formVO.setDynamicFormMapperVOList(dynamicFormMapperVOList);
            }
            FormConfigurationMapping configurationMapping = loadFormConfigMapping(uiMetaData.getId());

            Hibernate.initialize(modelMetaData.getFields());

            if (null != configurationMapping) {
                formVO.setInvocationPoint(configurationMapping.getInvocationPoint());
                formVO.setActiveFlag(configurationMapping.isActiveFlag());
                formVO.setTaskId((Long) configurationMapping.getViewProperties().get("taskId"));
                formVO.setId(configurationMapping.getId());
            }

            for (PanelDefinition panelDefinition : uiMetaData.getPanelDefinitionList()) {
                List<FormContainerVO> nestedVOList = new ArrayList<FormContainerVO>();

                for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {

                    // Create Field Vo objects and add to Field Container VO
                    FormContainerVO formContainerVO = new FormContainerVO();
                    if(fieldDefinition.getProductSchemeMetaData() !=null){
                        Hibernate.initialize(fieldDefinition.getProductSchemeMetaData());
                    }

                    formContainerVO.setProductScheme(fieldDefinition.getProductSchemeMetaData()!=null?fieldDefinition.getProductSchemeMetaData().getKeyy():null);
                    formContainerVO.setAssignmentName(fieldDefinition.getAssignmentMasterCode());
                    formContainerVO.setPopulateAssignmentResult(fieldDefinition.getPopulateAssignmentResult());
                    Hibernate.initialize(fieldDefinition.getSpecialTable());
                    formContainerVO.setSpecialTable(fieldDefinition.getSpecialTable()!=null?fieldDefinition.getSpecialTable().getKeyy():null);
                    formContainerVO.setBinderName(fieldDefinition.getBinderName());
                    formContainerVO.setDefaultValue(fieldDefinition.getValue());
                    formContainerVO.setFieldDataType(getDataType(modelMetaData.getFields(), fieldDefinition.getFieldKey()));
                    formContainerVO.setFieldKey(fieldDefinition.getFieldKey());
                    formContainerVO.setFieldLabel(fieldDefinition.getFieldLabel());
                    formContainerVO.setFieldType(fieldDefinition.getFieldType());
                    formContainerVO.setItemLabel(fieldDefinition.getItemLabel());
                    formContainerVO.setItemValue(fieldDefinition.getItemValue());
                    formContainerVO.setMandatoryField(fieldDefinition.isMandatoryField());
                    formContainerVO.setExpandableField(fieldDefinition.getExpandableField());
                //    formContainerVO.setHideField(fieldDefinition.isHideField());
                    formContainerVO.setIncludeSelect(fieldDefinition.isIncludeSelect());
                    formContainerVO.setToolTipMessage(fieldDefinition.getToolTipMessage());
                    formContainerVO.setDefaultMonth(fieldDefinition.getDefaultMonth());
                    formContainerVO.setDefaultYear(fieldDefinition.getDefaultYear());
                    formContainerVO.setType(FormContainerType.FIELD_TYPE_FIELD);
                    formContainerVO.setDescription(fieldDefinition.getDescription());
                    formContainerVO.setAutoCompleteColumnsHolder(fieldDefinition.getAutoCompleteColumnsHolder());
                    if (fieldDefinition.getFieldType().equals(FormComponentType.PHONE)) {
                        PhoneNumberVO numberVO = new PhoneNumberVO();
                        PhoneNumberTypeVO numberTypeVO = new PhoneNumberTypeVO();
                        PhoneNumberType numberType = null;
                        if (fieldDefinition.getMobile() != null && fieldDefinition.getMobile()) {
                            numberType = genericParameterService.findByCode(PhoneNumberTypeVO.MOBILE_NUMBER,
                                    PhoneNumberType.class);
                        } else {
                            numberType = genericParameterService.findByCode(PhoneNumberTypeVO.LANDLINE_NUMBER,
                                    PhoneNumberType.class);
                        }

                        if (numberType != null) {
                            numberTypeVO.setCode(numberType.getCode());
                            numberTypeVO.setName(numberType.getName());
                            numberTypeVO.setDescription(numberType.getDescription());
                        }
                        numberVO.setNumberTypeVO(numberTypeVO);
                        formContainerVO.setPhoneNumberVO(numberVO);
                        formContainerVO.setMobile(fieldDefinition.getMobile());
                    }
                    if (fieldDefinition.getFieldType().equals(FormComponentType.EMAIL)) {
                        EmailInfoVO emailInfoVO = new EmailInfoVO();
                        EmailTypeVO emailTypeVO = new EmailTypeVO();
                        EMailType eMailType = null;
                        if (StringUtils.isNotEmpty(fieldDefinition.getEmailTypeCode())) {
                            eMailType = genericParameterService.findByCode(fieldDefinition.getEmailTypeCode(),
                                    EMailType.class);
                            if (eMailType != null) {
                                emailTypeVO.setCode(eMailType.getCode());
                                emailTypeVO.setName(eMailType.getName());
                            }
                        }
                        emailInfoVO.setEmailTypeVO(emailTypeVO);
                        formContainerVO.setEmailInfoVO(emailInfoVO);
                    }
                    if(fieldDefinition.getFieldType().equals(FormComponentType.CASCADED_SELECT)|| fieldDefinition.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
                    	if(fieldDefinition.getParentFieldKey() != null)
                    		formContainerVO.setParentFieldKey(fieldDefinition.getParentFieldKey());
                    	if(fieldDefinition.getActiveChildEntityName() != null)
                    		formContainerVO.setCurrentChildEntityName(fieldDefinition.getActiveChildEntityName());
                        if(fieldDefinition.getUrlCascadeSelect() != null)
                            formContainerVO.setUrlCascadeSelect(fieldDefinition.getUrlCascadeSelect());
                        if(null != fieldDefinition.getParent() && null != fieldDefinition.getCustomCascadeOptions()){
                        	List<ParentChildForm> childForms = new ArrayList<>();
                        	fieldDefinition.getCustomCascadeOptions().forEach(customCascadeOptions ->{
                        		ParentChildForm childForm = new ParentChildForm();
                        		childForm.setChildIds(customCascadeOptions.getChildIdList().replace("[", "").replace("]", "").split(", "));
                        		childForm.setParentIds(customCascadeOptions.getParentIdList().replace("[", "").replace("]", "").split(", "));
                        		childForms.add(childForm);
                        	});
                        	formContainerVO.setParentChildForms(childForms);
                        }
                        formContainerVO.setFirstParent(fieldDefinition.getFirstParent());
                    }
                    formContainerVO.setMinFieldLength(fieldDefinition.getMinFieldLength());
                    formContainerVO.setMaxFieldLength(fieldDefinition.getMaxFieldLength());
                    formContainerVO.setMinFieldValue(fieldDefinition.getMinFieldValue());
                    formContainerVO.setMaxFieldValue(fieldDefinition.getMaxFieldValue());
                    formContainerVO.setParent(fieldDefinition.getParent());
                    formContainerVO.setDisable(fieldDefinition.getDisable());
                    formContainerVO.setAssociatedFieldKey(fieldDefinition.getAssociatedFieldKey());
                    formContainerVO.setLovKey(fieldDefinition.getLovKey());
                    
                    /**
                     * added for hyperlink types
                    */
                    formContainerVO.setHref(fieldDefinition.getHref());
                    formContainerVO.setFunctionLogic(fieldDefinition.getFunctionLogic());
                    formContainerVO.setAuthority(fieldDefinition.getAuthority());
                    
                    /**
                     *  added for dependant autocomplete
                     */
                    formContainerVO.setParentColumn(fieldDefinition.getParentColumn());
                    formContainerVO.setErrorMessageCode(fieldDefinition.getErrorMessageCode());
                    formContainerVO.setParentFieldId(fieldDefinition.getParentFieldId());
                    formContainerVO.setMainFormDependant(fieldDefinition.isMainFormDependant());
                    
                    
                    if (null != fieldDefinition.getEntityName() && fieldDefinition.getEntityName().contains(".")) {
                        formContainerVO.setEntityName(fieldDefinition.getEntityName().substring(
                                fieldDefinition.getEntityName().lastIndexOf(".") + 1));
                    }

                    formContainerVO.setComponentDisplayKey("component[" + rootVOList.size() + "][" + nestedVOList.size()
                            + "]");
                    formContainerVO.setCustomeLongMessage(fieldDefinition.getCustomeLongMessage());

                    if (fieldDefinition.getFieldCustomOptionsList() != null
                            && !fieldDefinition.getFieldCustomOptionsList().isEmpty()) {
                        List<FieldCustomOptionsVO> customOptionsList = new ArrayList<FieldCustomOptionsVO>();

                        for (FieldCustomOptions fieldCustomOptions : fieldDefinition.getFieldCustomOptionsList()) {
                            FieldCustomOptionsVO customOptions = new FieldCustomOptionsVO();
                            customOptions.setCustomeItemLabel(fieldCustomOptions.getCustomeItemLabel());
                            customOptions.setCustomeItemValue(fieldCustomOptions.getCustomeItemValue());
                            customOptionsList.add(customOptions);
                        }

                        formContainerVO.setFieldCustomOptionsList(customOptionsList);
                    }
                    nestedVOList.add(formContainerVO);

                }

                // Add Form COntainer to Panel Form Container VO
                FormContainerVO formContainerPanelVO = new FormContainerVO();
                formContainerPanelVO.setPanelColumnLayout(panelDefinition.getPanelColumnLayout());
                formContainerPanelVO.setPanelHeader(panelDefinition.getPanelHeader());
                formContainerPanelVO.setPanelName(panelDefinition.getPanelName());
                formContainerPanelVO.setFieldKey(panelDefinition.getPanelKey());
                Hibernate.initialize(panelDefinition.getSpecialTable());
                formContainerPanelVO.setSpecialTable(panelDefinition.getSpecialTable()!=null?panelDefinition.getSpecialTable().getKeyy():null);
                if(StringUtils.isNotBlank(panelDefinition.getSavedSpColumn())){
                    String[] arr = panelDefinition.getSavedSpColumn().split(",");
                    formContainerPanelVO.setSpecialColumns(arr);
                }else{
                    formContainerPanelVO.setSpecialColumns(null);
                }
                if(StringUtils.isNotBlank(panelDefinition.getSpecialTablePartyRoles())){
                    String[] arr = panelDefinition.getSpecialTablePartyRoles().split(",");
                    formContainerPanelVO.setPartyRoles(arr);
                }
                if(panelDefinition.getProductSchemeMetaData() !=null){
                    Hibernate.initialize(panelDefinition.getProductSchemeMetaData());
                }
                formContainerPanelVO.setProductScheme(panelDefinition.getProductSchemeMetaData()!=null?panelDefinition.getProductSchemeMetaData().getKeyy():null);

                if (panelDefinition.getPanelColumnLayout() == 0) {
                    formContainerPanelVO.setPanelColumnLayout(2);
                }

                formContainerPanelVO.setType(panelDefinition.getPanelType());

                if (formContainerPanelVO.getType() == FormContainerType.FIELD_TYPE_PANEL) {
                    formContainerPanelVO.setFieldType(FormComponentType.PANEL);
                    formContainerPanelVO.setAllowPanelSave(panelDefinition.getAllowPanelSave());
                } else if (formContainerPanelVO.getType() == FormContainerType.FIELD_TYPE_TABLE) {
                    formContainerPanelVO.setFieldType(FormComponentType.TABLE);
                }else if (formContainerPanelVO.getType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE) {
                    formContainerPanelVO.setFieldType(FormComponentType.SPECIAL_TABLE);
                }

                formContainerPanelVO.setFormContainerVOList(nestedVOList);
                formContainerPanelVO.setComponentDisplayKey("component[" + rootVOList.size() + "]");

                rootVOList.add(formContainerPanelVO);
            }

            formVO.setContainerVOList(rootVOList);
        }

        return formVO;
    }

    @Override
    public FormConfigurationMapping loadFormConfigMapping(Long id) {
        NamedQueryExecutor<FormConfigurationMapping> formConfigDataCriteria = new NamedQueryExecutor<FormConfigurationMapping>(
                "dynamicForm.loadFormConfigByUiMetaData").addParameter("id", id);
        List<FormConfigurationMapping> formConfigurationMappingList = entityDao.executeQuery(formConfigDataCriteria);

        if (null != formConfigurationMappingList && !formConfigurationMappingList.isEmpty()) {
            return formConfigurationMappingList.get(0);
        }

        return null;
    }

    @Override
    public FormConfigurationMapping loadFormConfigMappingByFormName(Long id) {
        UIMetaData uiMetaData = entityDao.find(UIMetaData.class, id);
        NamedQueryExecutor<FormConfigurationMapping> formConfigDataCriteria = new NamedQueryExecutor<FormConfigurationMapping>(
                "dynamicForm.loadFormConfigByUiMetaDataForFilter").addParameter("formName", uiMetaData.getFormName());
        List<FormConfigurationMapping> formConfigurationMappingList = entityDao.executeQuery(formConfigDataCriteria);

        if (null != formConfigurationMappingList && !formConfigurationMappingList.isEmpty()) {
            return formConfigurationMappingList.get(0);
        }

        return null;
    }

    private int getDataType(List<FieldMetaData> fields, String fieldKey) {

        for (FieldMetaData fieldMetaData : fields) {
            if (fieldMetaData.getFieldKey().equals(fieldKey)) {
                return fieldMetaData.getDataType();
            }
        }
        return 0;
    }

    @Override
    public ModelMetaData getModelMetaDataByUri(String modelUri) {
        return entityDao.get(EntityId.fromUri(modelUri));
    }

    @Override
    public FormConfigEntityData getFormConfigDataByPackageName(String packageName) {
        NamedQueryExecutor<FormConfigEntityData> formConfigDataCriteria = new NamedQueryExecutor<FormConfigEntityData>(
                "dynamicForm.getFormConfigDataByPackageName").addParameter("packageName", packageName).addQueryHint(
                QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<FormConfigEntityData> formConfigDataList = entityDao.executeQuery(formConfigDataCriteria);

        if (null != formConfigDataList && !formConfigDataList.isEmpty()) {
            return formConfigDataList.get(0);
        }
        return null;
    }
    
    @Override
    public boolean validateFilterNameAndSourceProduct(FormVO formVo)
    {
    	Map<String, Object> parameters=new HashMap<String, Object>();
    	parameters.put("filterName", formVo.getFormName());
    	parameters.put("sourceProductId", formVo.getSourceProductId());
    	parameters.put("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
    	List<DynamicFormFilter> existingFilters=formDefinitionDao.genericNamedQueryExecutor("dynamicForm.fetchDynamicFormFilterBasedOnFormNameAndSourceProduct", parameters);
    	 if (hasElements(existingFilters)) {
             for (DynamicFormFilter dynamicFormFilter : existingFilters) {
                 if (dynamicFormFilter.getUuid().equals(formVo.getDynamicFormFilter().getUuid())) {
                     return true;
                 } else {
                     return false;
                 }
             }
         }
    	return true;
    }

	@Override
	public List<String> getUiMetaDataForFieldKey(String formName,String fieldKey,Long sourceProductId) {
		Map<String, Object> parameters=new HashMap<String, Object>();
    	parameters.put("fieldKey",fieldKey);
    	parameters.put("sourceProductId", sourceProductId);
    	parameters.put("persistenceStatus", PersistenceStatus.INACTIVE);
    	parameters.put("formName", formName);
    	List<String> formWithFieldKey=formDefinitionDao.genericNamedQueryExecutor("findDuplicateKeysForUiMetaData", parameters);
    	
		return formWithFieldKey;
	}   

    @Override
    public String findLabelForKey(Long uimetaDataId, String fieldKey) {
        String labelValue = null;
        if (uimetaDataId != null && StringUtils.isNotEmpty(fieldKey)) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("uimetaDataId", uimetaDataId);
            parameters.put("fieldKey", fieldKey);
            List<String> labelKeyList = formDefinitionDao.genericNamedQueryExecutor("dynamicForm.findLabelForKey",
                    parameters);
            if (CollectionUtils.isNotEmpty(labelKeyList)) {
                String labelKey = labelKeyList.get(0);
                labelValue = messageSource.getMessage(labelKey, null, getUserLocale());
            }
        }
        return labelValue;
    }

	public FormConfigEntityData getFormConfigEntityDataBasedOnBinderName(String  webDataBinderName){
		return formDefinitionDao.getFormConfigEntityDataBasedOnBinderName(webDataBinderName);
	}

}
