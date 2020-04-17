package com.nucleus.core.generic.makerchecker;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.context.MessageSource;

import com.nucleus.authority.Authority;
import com.nucleus.authority.AuthorityCodes;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.process.BPMNProcessService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserService;

@Named("genericMakerCheckerService")
public class GenericMakerCheckerServiceImpl extends BaseServiceImpl implements GenericMakerCheckerService {

    public static final String GENERIC_MAKER_CHECKER_FLOW = "GenericMakerCheckerProcess";
    
    public static final String GENERIC_MAKER_PROCESS_DEFINITION = "generic_maker";

    @Inject
    @Named("messageSource")
    protected MessageSource    messageSource;

    @Inject
    @Named(value = "userService")
    private UserService        userService;

    @Inject
    @Named("bpmnProcessService")
    private BPMNProcessService bpmnProcessService;

    @Override
    public void setCheckerVariables(DelegateExecution execution) {
        String loggerMessageValue = messageSource.getMessage("label.generic.check.variable.value", null, null,
                getUserLocale());
        BaseLoggers.workflowLogger.info(loggerMessageValue);
        String entityUri = (String) execution.getVariable("entityUri");

        if (entityUri == null) {
            String errorMessageValue = messageSource.getMessage("label.generic.entity.null", null, null, getUserLocale());
            throw new InvalidDataException(errorMessageValue);
        }
        EntityId entityId = EntityId.fromUri(entityUri);
        entityId.getClass();

        String simpleClassName = getSimpleClassName(entityId.getEntityClass());

        String assigneeAuthorityOrUserUri = AuthorityCodes.CHECKER + "_" + simpleClassName.toUpperCase();
        Authority baseAuthority = userService.getAuthorityByCode(assigneeAuthorityOrUserUri);
        if (baseAuthority != null) {
            assigneeAuthorityOrUserUri = baseAuthority.getUri();
        } else {
            String authErrorMessageValue = messageSource.getMessage("label.authority.null", null, null, getUserLocale());
            throw new InvalidDataException(authErrorMessageValue);
        }
        execution.setVariable("assignee", assigneeAuthorityOrUserUri);

    }

    @Override
    public void completeApprovalProcess(DelegateExecution execution) {
        String approvalMessageValue = messageSource.getMessage("label.approval.process.completion.value", null, null,
                getUserLocale());
        BaseLoggers.workflowLogger.info(approvalMessageValue);
    }

    @Override
    public void completeRejectProcess(DelegateExecution execution) {
        String rejectMessageValue = messageSource.getMessage("label.reject.process.completion.value", null, null,
                getUserLocale());
        BaseLoggers.workflowLogger.info(rejectMessageValue);
    }

    @Override
    public void completeSendBackProcess(DelegateExecution execution) {
        String sendBackMessageValue = messageSource.getMessage("label.sendback.process.completion.value", null, null,
                getUserLocale());
        BaseLoggers.workflowLogger.info(sendBackMessageValue);
    }

    @SuppressWarnings("rawtypes")
    private String getSimpleClassName(Class clazz) {
        Class superClass = clazz.getSuperclass();

        if (superClass.getSimpleName().equalsIgnoreCase("BaseEntity")) {
            return clazz.getSimpleName();

        } else {
            return getSimpleClassName(superClass);
        }

    }

    @Override
    public void initaiteGenericFlow(Map<String, Object> processMap) {
        bpmnProcessService.startProcess(GENERIC_MAKER_CHECKER_FLOW, processMap);
    }
    
    @Override
    public void completeMakerCheckerTask(String taskId, Map<String, Object> variables) {
        NeutrinoValidator.notNull(taskId, "Task Id can't be null while completing the task.");
        bpmnProcessService.completeUserTask(taskId, variables);
    }

}
