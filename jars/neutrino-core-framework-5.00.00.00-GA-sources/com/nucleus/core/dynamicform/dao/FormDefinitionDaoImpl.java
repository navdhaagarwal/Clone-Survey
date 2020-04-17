package com.nucleus.core.dynamicform.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.nucleus.entity.ApprovalStatus;
import com.nucleus.core.formsConfiguration.FormConfigEntityData;
import com.nucleus.core.formsConfiguration.FormVO;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.persistence.EntityDaoImpl;

@Named("formDefinitionDao")
public class FormDefinitionDaoImpl extends EntityDaoImpl implements FormDefinitionDao{

    @Override
    public List<UIMetaData> fetchUiMetaDataBasedOnFormNameAndSourceProduct(
            FormVO formVo) {
        NamedQueryExecutor<UIMetaData> formConfigDataCriteria = new NamedQueryExecutor<UIMetaData>(
                "dynamicForm.fetchFormBasedOnFormNameAndSourceProduct").addParameter("formName", formVo.getFormName()).addParameter("sourceProductId", formVo.getSourceProduct().getId());
       return executeQuery(formConfigDataCriteria);
    }
    
    
	@Override
	public <T> List<T> genericNamedQueryExecutor(String queryName, Map<String, Object> parameters)
	{
		 NamedQueryExecutor<T> namedQuery = new NamedQueryExecutor<T>(queryName);
		 if(parameters!=null)
		 {
			 for(Map.Entry<String, Object> entry:parameters.entrySet())
			 {
				 namedQuery.addParameter(entry.getKey(), entry.getValue());
			 }
		 }
	        
	     return executeQuery(namedQuery);
	}
	
	@Override
	public List<UIMetaData> getAllUIMetaData() {
    	
    	Map<String, Object> parameters=new HashMap<String, Object>();
    	parameters.put("persistenceStatus",PersistenceStatus.INACTIVE );
    	parameters.put("approvalStatus",ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return genericNamedQueryExecutor("dynamicForm.getAllApprovedUiMetaData", parameters);
    }


	@Override
	public FormConfigEntityData getFormConfigEntityDataBasedOnBinderName(String webDataBinderName) {
		 NamedQueryExecutor<FormConfigEntityData> formConfigDataQuery = new NamedQueryExecutor<FormConfigEntityData>(
	                "fetchFormConfigEntityDataByBinderName").addParameter("webDataBinderName",webDataBinderName);
		 return (FormConfigEntityData) executeQueryForSingleValue(formConfigDataQuery);
	}


	

}