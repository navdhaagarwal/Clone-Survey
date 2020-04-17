package com.nucleus.core.dynamicform.dao;

import java.util.List;
import java.util.Map;

import com.nucleus.core.formsConfiguration.FormConfigEntityData;
import com.nucleus.core.formsConfiguration.FormVO;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.persistence.EntityDao;

public interface FormDefinitionDao extends EntityDao{
    List<UIMetaData> fetchUiMetaDataBasedOnFormNameAndSourceProduct(FormVO formVo);

    public <T> List<T> genericNamedQueryExecutor( String queryName, Map<String, Object> parameters); 
    
    
    public List<UIMetaData> getAllUIMetaData();
    FormConfigEntityData getFormConfigEntityDataBasedOnBinderName(String  webDataBinderName);
    
}
