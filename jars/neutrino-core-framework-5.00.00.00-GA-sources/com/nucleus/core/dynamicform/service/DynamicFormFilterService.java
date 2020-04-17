package com.nucleus.core.dynamicform.service;

import com.nucleus.core.dynamicform.dao.FormDefinitionDao;
import com.nucleus.core.dynamicform.entities.FieldFilterMapping;
import com.nucleus.core.dynamicform.entities.ServiceFieldFilterMapping;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderFilterMapping;
import com.nucleus.core.dynamicform.exception.InvalidDynamicFormDataException;
import com.nucleus.core.dynamicform.vo.DynamicFormDataVO;
import com.nucleus.core.formDefinition.FormDefinitionUtility;
import com.nucleus.core.formsConfiguration.IDynamicForm;
import com.nucleus.core.formsConfiguration.ModelMetaData;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.ServiceIdentifierService;
import com.nucleus.ws.core.entities.ServiceFieldName;
import com.nucleus.ws.core.entities.ServiceIdentifier;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jettison.json.JSONException;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 
 * @author gajendra.jatav
 *
 */
@Named("dynamicFormFilterService")
public class DynamicFormFilterService implements IDynamicFormFilterService {

	@Inject
	@Named("formDefinitionDao")
	private FormDefinitionDao formDefinitionDao;

	@Inject
	@Named("formConfigService")
	protected FormService formService;

	@Inject
	@Named("formDefinitionUtility")
	private FormDefinitionUtility formDefinitionUtility;

	@Inject
	@Named("dynamicFormFilterHelper")
	private DynamicFormFilterHelper dynamicFormFilterHelper;

	@Inject
	@Named("dynamicFormFilterDao")
	private IDynamicFormFilterDao dynamicFormFilterDao;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("serviceIdentifierService")
	private ServiceIdentifierService serviceIdentifierService;
	
	public FormDefinitionDao getFormDefinitionDao() {
		return formDefinitionDao;
	}

	public void setFormDefinitionDao(FormDefinitionDao formDefinitionDao) {
		this.formDefinitionDao = formDefinitionDao;
	}

	public FormService getFormService() {
		return formService;
	}

	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public FormDefinitionUtility getFormDefinitionUtility() {
		return formDefinitionUtility;
	}

	public void setFormDefinitionUtility(FormDefinitionUtility formDefinitionUtility) {
		this.formDefinitionUtility = formDefinitionUtility;
	}

	public DynamicFormFilterHelper getDynamicFormFilterHelper() {
		return dynamicFormFilterHelper;
	}

	public void setDynamicFormFilterHelper(DynamicFormFilterHelper dynamicFormFilterHelper) {
		this.dynamicFormFilterHelper = dynamicFormFilterHelper;
	}

	public IDynamicFormFilterDao getDynamicFormFilterDao() {
		return dynamicFormFilterDao;
	}

	public void setDynamicFormFilterDao(IDynamicFormFilterDao dynamicFormFilterDao) {
		this.dynamicFormFilterDao = dynamicFormFilterDao;
	}

	public GenericParameterService getGenericParameterService() {
		return genericParameterService;
	}

	public void setGenericParameterService(GenericParameterService genericParameterService) {
		this.genericParameterService = genericParameterService;
	}

	@Transactional
	@Override
	public Map<String, String> getFieldWiseFilteredFormData(List<IDynamicForm> dynamicFormsList,
			String serviceIdentifierCode, String... fields) {

		Map<String, String> fieldWiseJsonData = new HashMap<String, String>();
		List<FieldFilterMapping> filterMappings=getFieldFilterMappings(serviceIdentifierCode,fields);
	    /*
	     * Collect Dynamic form data from multiple entities into a map
	     * json in form of Map against dynamic form model Name
	     */
		Map<String, Map<String, Object>> dynamicFormDataMap = formDefinitionUtility
				.prepareDynamicFormMapData(dynamicFormsList);
		
		dynamicFormFilterHelper.getFieldWiseJsonDataMap(fieldWiseJsonData, dynamicFormDataMap, filterMappings);
		return fieldWiseJsonData;
	}

	private List<FieldFilterMapping> getFieldFilterMappings(String serviceIdentifierCode,String[] fields) {

		ServiceFieldFilterMapping serviceFieldFilterMapping = dynamicFormFilterDao
				.getServiceFieldFilterMappingByServiceName(serviceIdentifierCode);

		List<FieldFilterMapping> filterMappings = new ArrayList<FieldFilterMapping>();
		if (serviceFieldFilterMapping!=null) {
			List<FieldFilterMapping> fieldFilterMappings = serviceFieldFilterMapping.getFieldFilterMappings();
			if (ValidatorUtils.hasElements(fieldFilterMappings)) {
				filterMappings.addAll(fieldFilterMappings);
				dynamicFormFilterHelper.removeExtraMappings(filterMappings, fields);
				return filterMappings;
			}

		}
		ServiceIdentifier serviceIdentifier=serviceIdentifierService.getServiceIdentifierByCode(serviceIdentifierCode);
		List<ServiceFieldName> serviceFieldNames=serviceIdentifier.getServiceFields();
		if(ValidatorUtils.hasElements(serviceFieldNames))
		{
			for(ServiceFieldName fieldName:serviceFieldNames) {
				FieldFilterMapping fieldFilterMapping=new FieldFilterMapping();
				fieldFilterMapping.setServiceFieldName(fieldName);
				fieldFilterMapping.setDynamicFormFilter(null);
				filterMappings.add(fieldFilterMapping);
			}
		}

		dynamicFormFilterHelper.removeExtraMappings(filterMappings, fields);

		return filterMappings;
	}

	@Transactional
	@Override
	public void filterDynamicFormDataAndUpdateEntities(List<IDynamicForm> dynamicFormsList,
			List<String> dynamicFormDataList, String serviceIdentifierCode, String... placeHolderCode) {
		try {

			List<DynamicFormDataVO> dynamicFormDataVOS = new ArrayList<>();
			dynamicFormDataList.forEach(s -> {
				DynamicFormDataVO sourceMap = (DynamicFormDataVO)formDefinitionUtility
						.parseJSONStringToType(s, new TypeReference<DynamicFormDataVO>() {
						});
				dynamicFormDataVOS.add(sourceMap);
			});
			validateAndUpdateAndDynamicFormData(dynamicFormsList,dynamicFormDataVOS,serviceIdentifierCode,false,placeHolderCode);
		} catch (InvalidDynamicFormDataException e) {
			BaseLoggers.flowLogger.debug("Exception while updating dynamicFormData to entity " + e);
		}
	}

	@Override
	public void validateAndUpdateDynamicFormData(List<IDynamicForm> dynamicFormsList, List<DynamicFormDataVO> dynamicFormDataList, String serviceIdentifierCode, String... placeHolderCode) {
		validateAndUpdateAndDynamicFormData(dynamicFormsList,dynamicFormDataList,serviceIdentifierCode,true,placeHolderCode);
	}


	private void validateAndUpdateAndDynamicFormData(List<IDynamicForm> dynamicFormsList, List<DynamicFormDataVO> dynamicFormDataList, String serviceIdentifierCode,boolean doValidate, String... placeHolderCode) {

		ServicePlaceholderFilterMapping servicePlaceholderFilterMapping = dynamicFormFilterDao
				.getServicePlaceholderFilterMappingByServiceName(serviceIdentifierCode);

		Map<String, List<String>> entityPlaceholderMap = new HashMap<String, List<String>>();
		Map<String, Set<String>> placeholderFilterFieldsMap = new HashMap<String, Set<String>>();
		Map<String, Map<UIMetaData, ModelMetaData>> placeholderUiMetaDataMap = new HashMap<String,  Map<UIMetaData, ModelMetaData>>();
		try {
			dynamicFormFilterHelper.prepareMetaDataForDynamicFormUpdate(serviceIdentifierCode, placeHolderCode,
					servicePlaceholderFilterMapping, entityPlaceholderMap, placeholderFilterFieldsMap,
					placeholderUiMetaDataMap);
		} catch (JSONException e1) {
			BaseLoggers.exceptionLogger.error(e1.getMessage(), e1);
		}
		if (ValidatorUtils.hasNoEntry(placeholderFilterFieldsMap)) {
			return;
		}

		Map<String, Object> mergedFormData;
		try {
			mergedFormData = dynamicFormFilterHelper.prepareMapFromData(dynamicFormDataList,doValidate);

			for (IDynamicForm dynamicForm : dynamicFormsList) {
				dynamicFormFilterHelper.updateDynamicFormObject(dynamicForm, entityPlaceholderMap,
						placeholderFilterFieldsMap,placeholderUiMetaDataMap,mergedFormData,doValidate);
			}

		} catch (JSONException e) {
			BaseLoggers.flowLogger.debug("Exception while updating dynamicFormData to entity " + e);

		}
	}

}
