package com.nucleus.finnone.pro.communicationgenerator.task;

import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationEventLogTaskSupervisor {

  void submitObjectsToLogEvents(String eventCode,SourceProduct sourceProduct,String rootElement,String requestReferenceId,Boolean generateMergedFile);
}
