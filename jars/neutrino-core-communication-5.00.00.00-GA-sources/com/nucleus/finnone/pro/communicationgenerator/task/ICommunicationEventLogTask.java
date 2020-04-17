package com.nucleus.finnone.pro.communicationgenerator.task;

import java.util.concurrent.Callable;

import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationEventLogTask extends Callable<Long>{

    void populateEventLogTask(BaseEntity batchObject,String eventCode,SourceProduct sourceProduct,String rootElement,String requestReferenceId,Boolean generateMergedFile);
}
