package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventCodeType;
import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.SourceProduct;

public interface IAdHocEventLogCriteriaDAO {

    List<Object> getEntitiesFromRootContextObject(String rootElement);

    List<EventCode> getEventCodesBasedOnModule(SourceProduct sourceProduct);
    
    List<EventCode> getEventCodesBasedOnModuleAndEventCodeType(SourceProduct sourceProduct,EventCodeType eventCodeType);
    
    List<BaseEntity> fetchEntitiesBasedOnBatchSize(int batchCount,int batchSize,String rootContextObject);
    
    int fetchTotalRecordSize(String rootContextObject);
    
    String fetchNumberOfDuplicateSchedulersOfAdhocCommunication(String schedulerName,
			SourceProduct sourceProduct,Long id,String uuid);

}
