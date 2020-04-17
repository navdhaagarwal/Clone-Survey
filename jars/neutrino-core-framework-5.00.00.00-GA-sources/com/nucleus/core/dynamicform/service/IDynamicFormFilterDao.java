package com.nucleus.core.dynamicform.service;

import java.util.List;

import com.nucleus.core.dynamicform.entities.ServiceFieldFilterMapping;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderFilterMapping;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.ScreenId;

/**
 * 
 * @author gajendra.jatav
 *
 */
public interface IDynamicFormFilterDao {

	public ServiceFieldFilterMapping getServiceFieldFilterMappingByServiceName(String serviceIdentifierCode);
	
	public ServicePlaceholderFilterMapping getServicePlaceholderFilterMappingByServiceName(String serviceIdentifierCode);
	
	
	public List<ScreenId> getServicePlaceholderMappingByServiceCode(String serviceIdentifierCode);
	
	
    public List<FormConfigurationMapping> fetchDynamicFormsMappedToScreenIdByCode(String screenCode,Long sourceProductId);
}
