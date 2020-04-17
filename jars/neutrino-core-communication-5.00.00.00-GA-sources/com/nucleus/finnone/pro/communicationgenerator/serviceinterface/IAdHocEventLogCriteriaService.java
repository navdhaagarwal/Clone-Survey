package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.List;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventCodeType;
import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.SourceProduct;

public interface IAdHocEventLogCriteriaService {

    List<EventCode> getEventCodesBasedOnModule(SourceProduct sourceProduct);
    
    List<EventCode> getEventCodesBasedOnModuleAndEventCodeType(SourceProduct sourceProduct,EventCodeType eventCodeType);
    @Deprecated
    void logApplicableCommunicationEventsBasedOnEventCodes(
            List<EventCode> eventCodeList, SourceProduct module);
    
    List<BaseEntity> fetchObjectsBasedOnBatchSize(int batchCount,int batchSize,String rootContextObject);
    
    int fetchTotalRecordSize(String rootElement);
    
    void logApplicableCommunicationEventBasedOnEventCodesInBatch(EventCode eventCode, SourceProduct sourceProduct,String requestReferenceId,Boolean generateMergedFile);
    
    void logApplicableCommunicationEventsBasedOnEventCodesInBatch(List<EventCode> eventCodeList, SourceProduct module,Boolean generateMergedFile);
    
    
    
    String fetchNumberOfDuplicateSchedulersOfAdhocCommunication(String schedulerName,
			SourceProduct sourceProduct,Long id,String uuid);

}
