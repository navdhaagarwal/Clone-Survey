package com.nucleus.core.generic.makerchecker;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;

import com.nucleus.service.BaseService;

public interface GenericMakerCheckerService extends BaseService {

    public void setCheckerVariables(DelegateExecution execution);

    public void completeApprovalProcess(DelegateExecution execution);

    public void completeRejectProcess(DelegateExecution execution);

    public void completeSendBackProcess(DelegateExecution execution);

    public void initaiteGenericFlow(Map<String, Object> processMap);

    void completeMakerCheckerTask(String taskId, Map<String, Object> variables);

}
