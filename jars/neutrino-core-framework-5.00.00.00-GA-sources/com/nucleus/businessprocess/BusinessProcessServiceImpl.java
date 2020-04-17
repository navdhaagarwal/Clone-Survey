package com.nucleus.businessprocess;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
//import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.core.workflowconfig.entity.ProcessingStageType;
import com.nucleus.core.workflowconfig.entity.WorkflowConfigurationType;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.service.BaseServiceImpl;

import org.activiti.image.ProcessDiagramGenerator;

@Named("BusinessProcessService")
@MonitoredWithSpring(name = "BusinessProcess_Service_IMPL_")
public class BusinessProcessServiceImpl extends BaseServiceImpl implements BusinessProcessService {

    @Inject
    RepositoryService               repositoryService;

    @Inject
    RuntimeService                  runtimeService;

    @Inject
    ProcessEngineConfiguration      processEngineConfiguration;

    @Inject
    ProcessDiagramGenerator      processDiagramGenerator;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    private static final String     GENERIC_SUB_PROCESS = "generic_sub_process";

    private static final String     IMPLEMENTATION      = "${genericWorkflowService.startTaskProcessingStep(execution)}";

    /* (non-Javadoc) @see com.nucleus.businessProcess.BusinessProcessService#deployNewBusinessProcess(java.lang.String, java.io.InputStream) */
    @Override
    public String deployNewBusinessProcess(CommonsMultipartFile commonsMultipartFile) throws Exception {
        BpmnModel bpmnModel = getKeyFromBpXML(commonsMultipartFile.getInputStream());

        Boolean isPresent = isProcessPresent(bpmnModel.getProcesses().get(0).getId());

        if (isPresent)
            return "Process already present";
        String valid = validate(bpmnModel);
        if (!valid.isEmpty())
            return valid;

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().addInputStream(
                commonsMultipartFile.getOriginalFilename(), commonsMultipartFile.getInputStream());
        deploymentBuilder.deploy();
        return "";
    }

    @Override
    public String deployEixistingBusinessProcess(String toBeUpdatedSourcekey, CommonsMultipartFile commonsMultipartFile)
            throws Exception {
        BpmnModel bpmnModel = getKeyFromBpXML(commonsMultipartFile.getInputStream());

        Boolean isPresent = isProcessPresent(bpmnModel.getProcesses().get(0).getId());

        if (!isPresent)
            return "This is a new process. Please add this process before update";

        if (!toBeUpdatedSourcekey.equals(bpmnModel.getProcesses().get(0).getId()))
            return "Please update the correct process with id '"+toBeUpdatedSourcekey+"'";
        String valid = validate(bpmnModel);
        if (!valid.isEmpty())
            return valid;

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().addInputStream(
                commonsMultipartFile.getOriginalFilename(), commonsMultipartFile.getInputStream());
        deploymentBuilder.deploy();
        return "";
    }

    private String validate(BpmnModel bpmnModel) {

        List<CallActivity> callActivities = bpmnModel.getProcesses().get(0).findFlowElementsOfType(CallActivity.class);
        Boolean flag = false;
        for (CallActivity callActivity : callActivities) {
            if (callActivity.getCalledElement().equals(GENERIC_SUB_PROCESS)) {
                if (callActivity.getIncomingFlows().size() != 1)
                    return "'" + callActivity.getName()
                            + "' should have exactly 1 service task befor it. Please modify the same and upload again";
                else {
                    String procStageCode = callActivity.getIncomingFlows().get(0).getSourceRef();
                    FlowElement flowElement = bpmnModel.getProcesses().get(0).getFlowElement(procStageCode);
                    if (flowElement instanceof ServiceTask) {
                        ServiceTask new_name = (ServiceTask) flowElement;
                        if (!new_name.getImplementation().equals(IMPLEMENTATION)
                                || !new_name.getImplementationType().equals("expression")) {
                            return "'" + new_name.getName() + "' must implement '" + IMPLEMENTATION
                                    + "' as expression. Please modify and upload again";
                        } else {
                            String procStageName = new_name.getName();
                            String procStageDesc = new_name.getDocumentation();
                            ProcessingStageType procStageCode1 = genericParameterService.findByCode(procStageCode,
                                    ProcessingStageType.class);
                            if (procStageCode1 != null) {
                                procStageCode1.setName(procStageName);
                                procStageCode1.setDescription(procStageDesc);
                                //entityDao.update(procStageCode1); do nothing
                            } else {
                                ProcessingStageType processingStageType = new ProcessingStageType();
                                processingStageType.setCode(procStageCode);
                                processingStageType.setName(procStageName);
                                processingStageType.setDescription(procStageDesc);
                                genericParameterService.createGenericParameter(processingStageType);
                            }
                        }
                    } else {
                        return "Please add a Service Task before '" + callActivity.getName() + "'";
                    }

                    flag = true;
                }
            }
        }
        if (!flag) {
            return "Please add atleast one '" + GENERIC_SUB_PROCESS + "' in workflow and upload again";
        } else {
            return "";
        }
    }

    private BpmnModel getKeyFromBpXML(InputStream inputStream) {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        Boolean enableSafeBpmnXml = ((ProcessEngineConfigurationImpl) processEngineConfiguration).isEnableSafeBpmnXml();
        String encoding = ((ProcessEngineConfigurationImpl) processEngineConfiguration).getXmlEncoding();
        return converter.convertToBpmnModel(new InputStreamSource(inputStream), true, enableSafeBpmnXml, encoding);

    }

    private Boolean isProcessPresent(String newid) {
        List<ProcessDefinition> processDefinitions = getActiveLatestProcessDefinition();

        Boolean isPresent = false;
        for (ProcessDefinition definition : processDefinitions) {
            if (definition.getKey().equals(newid)) {
                isPresent = true;
            }
        }
        return isPresent;
    }

    /* (non-Javadoc) @see com.nucleus.businessProcess.BusinessProcessService#getLatestVersionsProcessDefinitionByKey(java.lang.String) */
    @Override
    public ProcessDefinition getLatestVersionsProcessDefinitionByKey(String key) {
        return repositoryService.createProcessDefinitionQuery().active().processDefinitionKey(key).latestVersion()
                .singleResult();
    }

    /* (non-Javadoc) @see com.nucleus.businessProcess.BusinessProcessService#getAllVersionsProcessDefinitionByKey(java.lang.String) */
    @Override
    public List<ProcessDefinition> getAllVersionsProcessDefinitionByKey(String key) {
        return repositoryService.createProcessDefinitionQuery().active().processDefinitionKey(key)
                .orderByProcessDefinitionVersion().desc().list();
    }

    /* (non-Javadoc) @see com.nucleus.businessProcess.BusinessProcessService#getActiveLatestProcessDefinition() */
    @Override
    public List<ProcessDefinition> getActiveLatestProcessDefinition() {

        return repositoryService.createProcessDefinitionQuery().latestVersion().active().list();
    }

    /* (non-Javadoc) @see com.nucleus.businessProcess.BusinessProcessService#getWorkflowImage(java.lang.String) */
    @Override
    public byte[] getWorkflowImage(String processDefinitionKey) throws IOException {
        return IOUtils.toByteArray(processDiagramGenerator.generatePngDiagram(repositoryService
                .getBpmnModel(getLatestVersionsProcessDefinitionByKey(processDefinitionKey).getId())));
    }

    /* (non-Javadoc) @see com.nucleus.businessProcess.BusinessProcessService#getWorkflowStringXML(java.lang.String) */
    @Override
    public String getWorkflowStringXML(ProcessDefinition processDefinition) throws IOException {
        // ProcessDefinition processDefinition = getLatestVersionsProcessDefinitionByKey(processDefinitionKey);
        return IOUtils.toString(repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName()));
    }

    @Override
    public List<String> getUnmappedLatestProcessDefinition() {
        List<WorkflowConfigurationType> workflowConfigurationTypes = genericParameterService
                .retrieveTypes(WorkflowConfigurationType.class);

        NeutrinoValidator.notNull(workflowConfigurationTypes, "no workflow config type defined");
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>("Config.getProcessDefinitionKeyonConfigTypes")
        		.addParameter("approvalStatusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
                .addParameter("workflowConfigTypeList", workflowConfigurationTypes);
        List<String> processKeysmapped = entityDao.executeQuery(executor);
        List<ProcessDefinition> processDefinitions = getActiveLatestProcessDefinition();
        List<String> processDefinitionKeys = new ArrayList<String>();
        for (ProcessDefinition processDefinition : processDefinitions) {
            processDefinitionKeys.add(processDefinition.getKey());
        }
        processDefinitionKeys.removeAll(processKeysmapped);
        return processDefinitionKeys;
    }

}
