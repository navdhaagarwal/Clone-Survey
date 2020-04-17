package com.nucleus.core.dynamicform.dao;

import java.util.List;
import java.util.Map;

import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.ScreenId;
import com.nucleus.persistence.EntityDao;

public interface FormDao extends EntityDao{

    List<FormConfigurationMapping> loadUniqueDynamicForms();
    List<FormConfigurationMapping> fetchSelectedFormConfigurations(List<Long> formConfigIds);
    ScreenId fetchScreenIdObjectBasedOnId(Long screenId);
    List<ScreenId> fetchPlaceHolderIdsMappedToSourceProduct(
            Long sourceProductId);
    List<FormConfigurationMapping> loadUniqueDynamicFormsForSourceProduct(Long sourceProductValue);
    
	List<Map<String, ?>> fetchPlaceHolderIdsMappedToSourceProduct(String value, Long sourceProductId, int pageNo);
}
