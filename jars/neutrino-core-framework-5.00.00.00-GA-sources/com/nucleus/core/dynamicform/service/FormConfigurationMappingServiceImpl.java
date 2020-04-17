package com.nucleus.core.dynamicform.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.formsConfiguration.DynamicFormScreenMappingDetail;
import com.nucleus.core.formsConfiguration.FormConfigInvocMapping;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.MultipleFormData;
import com.nucleus.core.formsConfiguration.PersistentFormData;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;

@Named(value = "formConfigurationMappingService")
public class FormConfigurationMappingServiceImpl extends BaseServiceImpl implements FormConfigurationMappingService {

    @Inject
    @Named("formConfigService")
    protected FormService formService;

    @Override
    public Map<String, Object> getUiMetaData(String formName, String invocationPoint, String uri, String formVersion) {

        Map<String, Object> objectDataMap = new HashMap<String, Object>();

        PersistentFormData persistentFormData = getPersistentData(uri, formName);
        objectDataMap.put(FormConfigurationConstant.PERSISTENT_FORM_DATA, persistentFormData);

        if (null != persistentFormData && null != persistentFormData.getFormUri()) {
            UIMetaData uIMetaData = entityDao.get(EntityId.fromUri(persistentFormData.getFormUri()));
            objectDataMap.put(FormConfigurationConstant.UI_META_DATA, uIMetaData);
            return objectDataMap;
        }

        if (StringUtils.isNotEmpty(invocationPoint)) {
            NamedQueryExecutor<FormConfigurationMapping> executor = new NamedQueryExecutor<FormConfigurationMapping>(
                    "dynamicForm.getUIMetaDataByInvocationPoint").addParameter("invocationPoint", invocationPoint)
                    .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

            FormConfigurationMapping formConfigurationMapping = entityDao.executeQueryForSingleValue(executor);

            objectDataMap.put(FormConfigurationConstant.UI_META_DATA, formConfigurationMapping.getUiMetaData());

        } else if (StringUtils.isNotEmpty(formName) && StringUtils.isNotEmpty(formVersion)) {

            List<UIMetaData> uIMetaDatas = formService.getFormByNameAndVersion(formName, formVersion);

            if (null != uIMetaDatas && uIMetaDatas.size() > 0) {
                objectDataMap.put(FormConfigurationConstant.UI_META_DATA, uIMetaDatas.get(0));
                return objectDataMap;
            }

        } else if (StringUtils.isNotEmpty(formName)) {

            List<UIMetaData> uIMetaDatas = formService.getFormByName(formName);

            if (null != uIMetaDatas && uIMetaDatas.size() > 0) {
                objectDataMap.put(FormConfigurationConstant.UI_META_DATA, uIMetaDatas.get(0));
                return objectDataMap;
            }
        }

        return objectDataMap;
    }

    @Override
    public List<ModelMetaData> getModelMetaData() {
        return entityDao.findAll(ModelMetaData.class);
    }

    @Override
    public List<UIMetaData> getUIMetaDataByModel(long modelId) {

        ModelMetaData modelMetaData = entityDao.find(ModelMetaData.class, modelId);
        NamedQueryExecutor<UIMetaData> executor = new NamedQueryExecutor<UIMetaData>("dynamicForm.getFormByModelName")
                .addParameter("modelName", modelMetaData.getName()).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE,
                        Boolean.TRUE);

        List<UIMetaData> uiMetaDataList = entityDao.executeQuery(executor);

        return uiMetaDataList;
    }

    @Override
    public List<FormConfigInvocMapping> getFormMappingPoints() {
        return entityDao.findAll(FormConfigInvocMapping.class);
    }

    public PersistentFormData getPersistentData(String uri, String formName) {

        PersistentFormData persistentFormData = null;

        if (null != uri) {
            Object object = entityDao.get(EntityId.fromUri(uri));

            Class superClass = MultipleFormData.class;

            if (superClass.isAssignableFrom(object.getClass())) {
                Map<String, PersistentFormData> persistentFormDataMap = ((MultipleFormData) object).getFormDataMap();
                if (null != persistentFormDataMap && persistentFormDataMap.size() > 0) {
                    String modelName = getModelByFormName(formName);
                    persistentFormData = persistentFormDataMap.get(modelName.replaceAll(" ", "_"));
                }

            } else {
            	Method  method = DynamicFormUtil.getPersistentFormDataGetterOrSetterMethod(object,DynamicFormUtil.PERSISTENT_FORM_DATA_GETTER_METHOD);
            	if(method != null) {
                   try {
                	   persistentFormData = (PersistentFormData) method.invoke(object, null);
					} catch (Exception e) {
				    }
            	}
            }
        }

        return persistentFormData;
    }

    @Override
    public String getModelByFormName(String formName) {

        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>("dynamicForm.getModelMetaDataByFormName")
                .addParameter("formName", formName).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        List<String> modelNames = entityDao.executeQuery(executor);

        if (null != modelNames && modelNames.size() > 0) {
            return modelNames.get(0);
        }

        return "";

    }

    @Override
    public UIMetaData getUiMetaDataTemplate(String formName, String invocationPoint) {

        if (null != invocationPoint && !"".equals(invocationPoint)) {
            NamedQueryExecutor<FormConfigurationMapping> executor = new NamedQueryExecutor<FormConfigurationMapping>(
                    "dynamicForm.getUIMetaDataByInvocationPoint").addParameter("invocationPoint", invocationPoint);

            FormConfigurationMapping formConfigurationMapping = entityDao.executeQueryForSingleValue(executor);

            return formConfigurationMapping.getUiMetaData();

        } else if (null != formName && !"".equals(formName)) {

            NamedQueryExecutor<UIMetaData> formConfigDataCriteria = new NamedQueryExecutor<UIMetaData>(
                    "dynamicForm.validateFormName").addParameter("formName", formName);
            List<UIMetaData> uIMetaDatas = entityDao.executeQuery(formConfigDataCriteria);

            if (null != uIMetaDatas && uIMetaDatas.size() > 0) {
                return uIMetaDatas.get(0);
            }
        }

        return null;
    }


    @Override
    public List<DynamicFormScreenMappingDetail> fetchDynamicFormsMappedToScreenIdAndSourceproductId(
            Long screenId,Long sourceProductId,Long productType) {

        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);


        NamedQueryExecutor<DynamicFormScreenMappingDetail> executor = new NamedQueryExecutor<DynamicFormScreenMappingDetail>("getDynamicFormsMappedToScreenIdAndProductType")
                .addParameter("screenId", screenId).addParameter("sourceProductId", sourceProductId).addParameter("productType", "%"+productType+"%").addParameter("approvalStatusList", statusList);

        return entityDao.executeQuery(executor);
    }

    @Override
    public List<DynamicFormScreenMappingDetail> fetchDynamicFormsMappedToScreenIdAndSourceproductId(
            Long screenId,Long sourceProductId) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        
		 NamedQueryExecutor<DynamicFormScreenMappingDetail> executor = new NamedQueryExecutor<DynamicFormScreenMappingDetail>("getDynamicFormsMappedToScreenId")
                .addParameter("screenId", screenId).addParameter("sourceProductId", sourceProductId).addParameter("approvalStatusList", statusList);
        return entityDao.executeQuery(executor);
    }

    @Override
    public List<DynamicFormScreenMappingDetail> fetchAllDynamicFormsMappedToScreenIdAndSourceproduct(
            Long screenId,Long sourceProductId) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        NamedQueryExecutor<DynamicFormScreenMappingDetail> executor = new NamedQueryExecutor<DynamicFormScreenMappingDetail>("getAllDynamicFormsMappedToScreenId")
                .addParameter("screenId", screenId).addParameter("sourceProductId", sourceProductId).addParameter("approvalStatusList", statusList);
        return entityDao.executeQuery(executor);
    }



}
