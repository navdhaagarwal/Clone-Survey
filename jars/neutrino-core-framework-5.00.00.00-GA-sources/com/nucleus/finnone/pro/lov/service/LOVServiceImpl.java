package com.nucleus.finnone.pro.lov.service;

import static com.nucleus.finnone.pro.lov.constants.LOVConstants.SEARCH_RECORD_LIST;
import static com.nucleus.finnone.pro.lov.constants.LOVConstants.SEARCH_RECORD_LIST_SIZE;
import static com.nucleus.finnone.pro.lov.constants.LOVConstants.TOTAL_RECORD_LIST_SIZE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.finnone.pro.lov.LOVSearchVO;
import com.nucleus.finnone.pro.lov.businessobject.ILOVBusinessObject;
import com.nucleus.finnone.pro.lov.serviceinterface.ILOVService;
import com.nucleus.master.BaseMasterEntity;

@Named("lovServiceImpl")
public class LOVServiceImpl implements ILOVService
{

  @Inject
  @Named("lovBusinessObject")
  private ILOVBusinessObject lovBusinessObject;

  
  @Override
  public Map<String, Object> loadLOVData(LOVSearchVO lovSearchVO)
  {
    Map<String, Object> searchRecordMap = new HashMap<String, Object>();
    List searchRecords;
    if(lovSearchVO.getEntityClass()!=null
    		&&BaseMasterEntity.class.isAssignableFrom(lovSearchVO.getEntityClass())){
    	searchRecords = lovBusinessObject.loadPaginatedMasterEntityData(lovSearchVO.getEntityClass(),lovSearchVO);	
    }else{
    	searchRecords = lovBusinessObject.loadPaginatedEntityData(lovSearchVO.getEntityClass(),lovSearchVO);
    }
    
    setRequiredParametersForLOV(searchRecords, lovSearchVO, searchRecordMap);
    
    return searchRecordMap;
  }
  
  
  @Override
  public void setRequiredParametersForLOV(List searchRecords,LOVSearchVO lovSearchVO,Map<String, Object> searchRecordMap){
		if(isNotEmpty(searchRecords)){
			
			Integer recordCount = Integer.valueOf(searchRecords.size());
			if(recordCount > lovSearchVO.getiDisplayLength()){
		    	searchRecordMap.put(TOTAL_RECORD_LIST_SIZE, lovSearchVO.getiDisplayLength()+1);
		    	searchRecords.remove(searchRecords.size() - 1);
		    }else{
		    	searchRecordMap.put(TOTAL_RECORD_LIST_SIZE, lovSearchVO.getiDisplayLength());
		    }
		    
		    searchRecordMap.put(SEARCH_RECORD_LIST_SIZE, searchRecords.size());
		}
		else
		{
			searchRecordMap.put(TOTAL_RECORD_LIST_SIZE, 0);
			searchRecordMap.put(SEARCH_RECORD_LIST_SIZE, 0);
		}
		searchRecordMap.put(SEARCH_RECORD_LIST, searchRecords);
	}

  @Override
  public <T extends Object> Map<String, Object>  setRequiredParametersForLOVWithAllData(List<T> searchRecords, LOVSearchVO lovSearchVO) {
    List<T> filteredList=null;   
    Map<String, Object> searchRecordMap = new HashMap<String, Object>();
    if(isNotEmpty(searchRecords)){
            
            Integer recordCount = Integer.valueOf(searchRecords.size());
            if(recordCount > lovSearchVO.getiDisplayLength()){
              Integer endIndex=lovSearchVO.getiDisplayStart()+lovSearchVO.getiDisplayLength();
                searchRecordMap.put(TOTAL_RECORD_LIST_SIZE, lovSearchVO.getiDisplayLength()+1);
         filteredList= searchRecords.subList(lovSearchVO.getiDisplayStart(), endIndex>recordCount?recordCount:endIndex);
         
                
            }else{
                searchRecordMap.put(TOTAL_RECORD_LIST_SIZE, lovSearchVO.getiDisplayLength());
            }
            
            searchRecordMap.put(SEARCH_RECORD_LIST_SIZE, searchRecords.size());
        }
        else
        {
            searchRecordMap.put(TOTAL_RECORD_LIST_SIZE, 0);
            searchRecordMap.put(SEARCH_RECORD_LIST_SIZE, 0);
        }
        searchRecordMap.put(SEARCH_RECORD_LIST, ValidatorUtils.notNull(filteredList)?filteredList:searchRecords);
        return searchRecordMap; 
  }
    
  
}