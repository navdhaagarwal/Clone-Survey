package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventCodeType;
import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.SourceProduct;

public interface IAdHocEventLogCriteriaBusinessObject {

    List<Object> getEntitiesFromRootContextObject(String rootElement);

    List<EventCode> getEventCodesBasedOnModule(SourceProduct sourceProduct);
    
    List<EventCode> getEventCodesBasedOnModuleAndEventCodeType(SourceProduct sourceProduct,EventCodeType eventCodeType);
    
    List<BaseEntity> fetchEntitiesBasedOnBatchSize(int batchCount,int batchSize,String rootElement);
    
    int fetchTotalRecordSize(String rootContextObject);
    
    void fireEventForAllApplicableEntitiesAndLogEvent(String eventCode,SourceProduct module, String rootContextObject,BaseEntity batchObject,String requestReferenceId,Boolean generateMergedFile);
    
    String fetchNumberOfDuplicateSchedulersOfAdhocCommunication(String schedulerName,
			SourceProduct sourceProduct,Long id,String uuid);
}
