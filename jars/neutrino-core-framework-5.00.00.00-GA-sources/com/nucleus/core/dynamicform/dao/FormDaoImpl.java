package com.nucleus.core.dynamicform.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.formsConfiguration.ScreenId;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDaoImpl;
import com.nucleus.query.constants.QueryHint;

@Named("formDao")
public class FormDaoImpl extends EntityDaoImpl implements FormDao {

    private static final Integer DEFAULT_PAGE_SIZE = 3;

	@Override
    public List<FormConfigurationMapping> loadUniqueDynamicForms() {
        NamedQueryExecutor<FormConfigurationMapping> formDefCriteria = new NamedQueryExecutor<FormConfigurationMapping>(
                "dynamicForm.getUniqueDynamicForms");
        formDefCriteria.addParameter("panelType",5);
        return executeQuery(formDefCriteria);
    }

    @Override
    public List<FormConfigurationMapping> fetchSelectedFormConfigurations(
            List<Long> formConfigIds) {
        NamedQueryExecutor<FormConfigurationMapping> formDefCriteria = new NamedQueryExecutor<FormConfigurationMapping>(
                "dynamicForm.getSelectedDynamicForms").addParameter("formConfigIds", formConfigIds);
        return executeQuery(formDefCriteria);
    }

    @Override
    public ScreenId fetchScreenIdObjectBasedOnId(Long screenId) {
        NamedQueryExecutor<ScreenId> screenDef=new NamedQueryExecutor<ScreenId>("getScreenFromScreenId").addParameter("screenId", screenId).addQueryHint(
                QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
       return executeQueryForSingleValue(screenDef);
    }

    @Override
    public List<ScreenId> fetchPlaceHolderIdsMappedToSourceProduct(
            Long sourceProductId) {
    	

	    List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        
        NamedQueryExecutor<ScreenId> screenDef=new NamedQueryExecutor<ScreenId>("getPlaceHolderIdsMappedToSourceProduct").addParameter("sourceProductId", sourceProductId).addParameter("approvalStatusList", statusList);
        return executeQuery(screenDef);
    }

    @Override
    public List<FormConfigurationMapping> loadUniqueDynamicFormsForSourceProduct(
            Long sourceProductValue) {
    	List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.DELETED_APPROVED_IN_HISTORY);
        NamedQueryExecutor<FormConfigurationMapping> formDefCriteria = new NamedQueryExecutor<FormConfigurationMapping>(
                "dynamicForm.getUniqueDynamicFormsBasedOnSourceProduct").addParameter("sourceProductId", sourceProductValue)
        		.addParameter("approvalStatusList", statusList)
        		.addParameter("approvalStatusNotEqualTo", 5)
        		.addParameter("panelType", 5);

        return executeQuery(formDefCriteria);
    }

	@Override
	public List<Map<String, ?>> fetchPlaceHolderIdsMappedToSourceProduct(String value, Long sourceProductId, int pageNo) {
		int counter=0;
		long totalRecords=0;
		
		List<Map<String, ?>> finalResult = new ArrayList<Map<String, ?>>();
		
		List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
        StringBuilder searchString = new StringBuilder();
        searchString.append("%").append(value.toLowerCase()).append("%");
        
        NamedQueryExecutor<Map<String, ?>> screenDef=new NamedQueryExecutor<Map<String, ?>>("dynamicForm.getPlaceHolderIdsMappedToSourceProductForAutoComplete").
        		addParameter("sourceProductId", sourceProductId).addParameter("approvalStatusList", statusList).
        		addParameter("value", searchString.toString()).addParameter("value1",searchString.toString());

        List<Map<String, ?>> screenIdList = executeQuery(screenDef, pageNo * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        
         for (Map<String, ?> temp : screenIdList) {
 			finalResult.add(counter, temp);
 			counter++;
 		}
 		totalRecords = totalRecords + executeTotalRowsQuery(screenDef);

 		Map<String, Long> sizeMap = new HashMap<String, Long>();

 		sizeMap.put("size", totalRecords);
 		finalResult.add(counter, sizeMap);
 		if (finalResult != null) {
 			BaseLoggers.flowLogger.debug("size of finalResult :" + finalResult.size());
 		}

 		return finalResult;
	}

}
