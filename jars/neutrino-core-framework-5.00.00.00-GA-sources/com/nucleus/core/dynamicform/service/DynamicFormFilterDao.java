package com.nucleus.core.dynamicform.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.dynamicform.dao.FormDefinitionDao;
import com.nucleus.core.dynamicform.entities.ServiceFieldFilterMapping;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderFilterMapping;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderMapping;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.ScreenId;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;

/**
 * 
 * @author gajendra.jatav
 *
 */
@Named("dynamicFormFilterDao")
public class DynamicFormFilterDao implements IDynamicFormFilterDao{



    @Inject
    @Named("formDefinitionDao")
    private FormDefinitionDao formDefinitionDao; 
	
	@Override
	public ServiceFieldFilterMapping getServiceFieldFilterMappingByServiceName(String serviceIdentifierCode) {
	
		Map<String, Object> parameters= getCommonQueryParameters();
		parameters.put("serviceCode", serviceIdentifierCode);
		List<ServiceFieldFilterMapping> serviceFieldFilterMapping=formDefinitionDao.genericNamedQueryExecutor("dynamicFormFilter.getAllServiceFieldFilterMappingByServiceCode",parameters);
		if(ValidatorUtils.hasElements(serviceFieldFilterMapping))
		{
			return serviceFieldFilterMapping.get(0);
		}
		return null;
	}

	@Override
	public ServicePlaceholderFilterMapping getServicePlaceholderFilterMappingByServiceName(
			String serviceIdentifierCode) {
		
		Map<String, Object> parameters= getCommonQueryParameters();
		parameters.put("serviceCode", serviceIdentifierCode);
		List<ServicePlaceholderFilterMapping> servicePlaceholderFilterMappings=formDefinitionDao.genericNamedQueryExecutor("dynamicFormFilter.getAllServicePlaceholderFilterMappingByServiceCode",parameters);
		if(ValidatorUtils.hasElements(servicePlaceholderFilterMappings))
		{
			return servicePlaceholderFilterMappings.get(0);
		}
		return null;
	}
	
	private Map<String, Object> getCommonQueryParameters()
	{
		List<Integer> approvalList = new ArrayList<Integer>();
		approvalList.add(ApprovalStatus.APPROVED);
		approvalList.add(ApprovalStatus.APPROVED_MODIFIED);
		approvalList.add(ApprovalStatus.APPROVED_DELETED);
		approvalList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		Map<String, Object> parameters=new HashMap<String, Object>();
		parameters.put("approvalStatus", approvalList);
		parameters.put("sourceProductCode", ProductInformationLoader.getProductCode());
		return parameters;
	}

	@Override
	public List<ScreenId> getServicePlaceholderMappingByServiceCode(String serviceIdentifierCode) {
		
		Map<String, Object> parameters=new HashMap<String, Object>();
		parameters.put("sourceProductCode", ProductInformationLoader.getProductCode());
		parameters.put("serviceCode", serviceIdentifierCode);
		List<ServicePlaceholderMapping> servicePlaceholderMappings=formDefinitionDao.genericNamedQueryExecutor("getServicePlaceholderMappingByServiceCode",parameters);
		List<ScreenId> screenIds=null;
		if(ValidatorUtils.hasElements(servicePlaceholderMappings))
		{
			screenIds=new ArrayList<ScreenId>();
			for(ServicePlaceholderMapping mapping:servicePlaceholderMappings)
			{
				screenIds.add(mapping.getScreenId());
			}
		}
		return screenIds;
	}

	@Override
	public List<FormConfigurationMapping> fetchDynamicFormsMappedToScreenIdByCode(String screenCode,
			Long sourceProductId) {
		
		Map<String, Object> parameters= getCommonQueryParameters();
		parameters.put("screenCode", screenCode);
		List<FormConfigurationMapping> formConfigurationMappings=formDefinitionDao.genericNamedQueryExecutor("fetchDynamicFormsMappedToScreenIdByCode",parameters);
		
		return formConfigurationMappings;
	}
}
