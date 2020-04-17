package com.nucleus.process;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import net.bull.javamelody.MonitoredWithSpring;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.NativeHistoricTaskInstanceQuery;
import org.activiti.image.ProcessDiagramGenerator;
//import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nucleus.cas.loan.workflow.CoreWorkflowConstants;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.persistence.jdbc.NeutrinoJdbcTemplate;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.core.workflowconfig.entity.WorkflowConfigurationType;
import com.nucleus.dao.query.NativeQueryExecutor;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MasterApprovalFlowConstants;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserService;

/**
 * BPMN Process Service class to do the process specific operations
 *
 *  @author Nucleus Software India Pvt Ltd
 */
@Named("bpmnProcessService")
public class BPMNProcessServiceImpl extends BaseServiceImpl implements BPMNProcessService {

	private static final int          	ORACLE_BATCH_SIZE = 1000;
    private static final String 		SINGLE_QUOTE = "'";
    private static final String 		COMMA = ",";
    
    @Autowired
    protected ProcessEngine                    processEngine;

    @Inject
    ProcessDiagramGenerator      processDiagramGenerator;

    @Autowired
    protected SpringProcessEngineConfiguration processEngineConfiguration;

    @Inject
    @Named("userService")
    protected UserService                      userService;

    @Inject
    @Named("genericParameterService")
    protected GenericParameterService          genericParameterService;

    @Inject
    @Named("neutrinoJdbcTemplate")
    protected NeutrinoJdbcTemplate             neutrinoJdbcTemplate;

    
    public static final int BATCHSIZE=999;
    
    
    @Override
    public String startProcess(String processDefinitionKey, Map<String, Object> parametersMap) {

        BaseLoggers.workflowLogger.debug("Starting a workflow new process with process id: {}", processDefinitionKey);

        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey(processDefinitionKey,
                parametersMap);
        return processInstance.getProcessInstanceId();
    }

    @Override
    public String startProcessByMessage(String processId, Map<String, Object> parametersMap) {

        BaseLoggers.workflowLogger.debug("Starting a workflow new process with process id: {}", processId);

        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByMessage(processId,
                parametersMap);
        return processInstance.getProcessInstanceId();
    }

    @Override
    public void completeUserTask(String taskId, Map<String, Object> parametersMap) {
        BaseLoggers.workflowLogger.debug("Completing workflow user task with id: {}", taskId);
        processEngine.getTaskService().complete(taskId, parametersMap);
    }

    @Override
    public void deleteUserTask(String processId, String deletionReason) {
        BaseLoggers.workflowLogger.debug("Deleting workflow process with id: {}", processId);
        processEngine.getRuntimeService().deleteProcessInstance(processId, deletionReason);
    }

    @Override
    public void completeUserTask(String taskId) {
        completeUserTask(taskId, null);
    }

    @Override
    public void resolveUserTask(String taskId) {
        BaseLoggers.workflowLogger.debug("Completing workflow user task with id: {}", taskId);
        processEngine.getTaskService().resolveTask(taskId);
    }
    
    @Override
    public String resolveReceiveTask(String processInstanceId, String receiveTaskId) {
    	Execution execution = processEngine.getRuntimeService().createExecutionQuery()
      		  .processInstanceId(processInstanceId)
      		  .activityId(receiveTaskId)
      		  .singleResult();
    	if(execution != null) {
    		return execution.getId();
    	}
    	return null;
    }
    
    @Override
    public void signalReceiveTask(String executionId) {
    	processEngine.getRuntimeService().signal(executionId);
    }

    @Override
    public byte[] generateWorkflowImage(String processInstanceId) {
        try {
            BpmnModel bpmnModel = processEngine.getRepositoryService().getBpmnModel(processInstanceId);
            InputStream is = processDiagramGenerator.generatePngDiagram(bpmnModel);
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new SystemException("Exception while generating image for workflow process instance id: "
                    + processInstanceId, e);
        }
    }

    @Override
    public byte[] generateWorkflowImageByTask(String taskId) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");
        Task task = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
        return generateWorkflowImage(task.getProcessInstanceId());
    }

    @Override
    public String getCandidateGroupForTask(String taskId) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");
        List<IdentityLink> identityLinks = processEngine.getTaskService().getIdentityLinksForTask(taskId);
        Iterator<IdentityLink> itr = identityLinks.iterator();
        while (itr.hasNext()) {
            IdentityLink idLink = itr.next();

            if (idLink.getGroupId() != null) {
                return idLink.getGroupId();
            }
        }
        return null;
    }

    /**
     * Gets the task by task id.
     *
     * @param taskId the task id
     * @return the task by task id
     */
    @Override
    @MonitoredWithSpring(name = "BPMN_TASK_FOR_TASK_ID")
    public Task getTaskByTaskId(String taskId, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");

        TaskQuery query = processEngine.getTaskService().createTaskQuery();
        query.taskId(taskId);

        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return query.singleResult();

    }

    @Override
    @MonitoredWithSpring(name = "BPMN_TASK_FOR_TASK_ID")
    public HistoricTaskInstance getHistoricTaskByTaskId(String taskId, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");

        HistoricTaskInstanceQuery query = processEngine.getHistoryService().createHistoricTaskInstanceQuery();
        query.taskId(taskId);

        if (variableMap != null) {
            query = addHistoricVariables(query, variableMap);
        }
        return query.singleResult();

    }

    @Override
    public Task getTaskByProcessIdAndTaskDefinitionKey(String processInstanceId, String taskDefinitionKey) {
        NeutrinoValidator.notEmpty(processInstanceId, "Process Instance Id couldn't be null or empty");
        NeutrinoValidator.notEmpty(taskDefinitionKey, "Task Definition Key couldn't be null or empty");
        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId)
                .taskDefinitionKey(taskDefinitionKey).singleResult();
        return task;
    }

    /*@Override
    public HistoricProcessInstance getSuperProcessInstanceByProcessId(String processInstanceId) {
        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (historicProcessInstance.getSuperProcessInstanceId() != null) {
            return getSuperProcessInstanceByProcessId(historicProcessInstance.getSuperProcessInstanceId());
        } else {
            return historicProcessInstance;
        }
    }*/

    @Override
    public List<Task> getAdhocTasks() {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskDefinitionKey("adhocTask").list();
        return tasks;
    }

    @Override
    public List<Task> getAllTasks(Map<String, Object> variableMap) {
        TaskQuery query = processEngine.getTaskService().createTaskQuery();
        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return execute(query);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Task> getAllTasksByNativeQuery(Map<String, Object> variableMap) {

        StringBuilder stageList = null;
        Long applicationId = null;
        StringBuilder nativeQueryString = new StringBuilder();
        if (variableMap != null) {
            List<String> stages = (List<String>) variableMap.get(CoreWorkflowConstants.STAGE_NAME);
            if (CollectionUtils.isNotEmpty(stages)) {
                stageList = new StringBuilder();
                for (int i = 0 ; i < stages.size() ; i++) {
                    stageList.append("'").append(stages.get(i)).append("'");
                    if (i != stages.size() - 1) {
                        stageList.append(",");
                    }
                }
            } else {
                return Collections.emptyList();
            }
            applicationId = (Long) variableMap.get(CoreWorkflowConstants.APPLICATION_ID);
        }
        nativeQueryString.append("SELECT DISTINCT ART.* FROM ACT_RU_TASK ART, ACT_RU_VARIABLE ARV ");
        if (applicationId != null) {
            nativeQueryString.append(", ACT_RU_VARIABLE ARV1 ");
        }

        nativeQueryString.append("WHERE ARV.PROC_INST_ID_ = ART.PROC_INST_ID_ ");

        if (applicationId != null) {
            nativeQueryString.append("AND ARV1.PROC_INST_ID_ = ART.PROC_INST_ID_ ");
        }

        nativeQueryString.append("AND ARV.NAME_ = 'stageName' AND ARV.TEXT_ IN (" + stageList + ")");

        if (applicationId != null) {
            nativeQueryString.append("AND ( ARV1.NAME_ = 'applicationId' AND ARV1.TEXT_ = '" + applicationId + "')");
        }

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(nativeQueryString.toString());
        List<Task> tasks = query.list();
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks;
    }

    @Override
    public List<HistoricTaskInstance> getAllHistoricTasks(Map<String, Object> variableMap, Boolean finishedTask) {
        List<HistoricTaskInstance> historicTaskInstances = null;
        HistoricTaskInstanceQuery query = processEngine.getHistoryService().createHistoricTaskInstanceQuery();
        if (variableMap != null) {
            query = addHistoricVariables(query, variableMap);
        }
        if (finishedTask == null) {
            historicTaskInstances = executeHistoricTaskQuery(query);
        } else if (finishedTask) {
            historicTaskInstances = executeHistoricTaskQuery(query.finished());
        } else {
            historicTaskInstances = executeHistoricTaskQuery(query.unfinished());
        }
        return historicTaskInstances;
    }

    @Override
    public List<HistoricTaskInstance> getAllHistoricTasksforUser(Map<String, Object> variableMap, Boolean finishedTask,
            String userURI) {
        List<HistoricTaskInstance> historicTaskInstances = null;
        HistoricTaskInstanceQuery query = processEngine.getHistoryService().createHistoricTaskInstanceQuery()
                .taskAssignee(userURI);
        if (variableMap != null) {
            query = addHistoricVariables(query, variableMap);
        }
        if (finishedTask == null) {
            historicTaskInstances = executeHistoricTaskQuery(query);
        } else if (finishedTask) {
            historicTaskInstances = executeHistoricTaskQuery(query.finished());
        } else {
            historicTaskInstances = executeHistoricTaskQuery(query.unfinished());
        }
        return historicTaskInstances;
    }

    @Override
    public HistoricVariableInstance getAllHistoricTasksVariables(String processInstanceId, String variableName) {
        NeutrinoValidator.notEmpty(processInstanceId, "ProcessInstanceId couldn't be null or empty");
        BaseLoggers.flowLogger.debug("ProcessInstanceId " + processInstanceId + " variableName " + variableName);
        HistoricVariableInstance variableInstance = processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId).variableName(variableName).singleResult();
        return variableInstance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BigDecimal> getHistoricTasksVariables(List<String> processInstanceId, String variableName) {
        NeutrinoValidator.notEmpty(processInstanceId, "ProcessInstanceId couldn't be null or empty");
        BaseLoggers.flowLogger.debug("ProcessInstanceId " + processInstanceId + " variableName " + variableName);
        List<BigDecimal> resultantObject = new ArrayList<BigDecimal>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("variableName", variableName);
        List<Map<String, Object>> PROCESS_INST_TO_STAGE_NAME = neutrinoJdbcTemplate.queryForListWithSingleInClause(
                "QUERY_FOR_HISTORIC_TASK_VARIABLES", "processInstIds", processInstanceId, params);
        if(PROCESS_INST_TO_STAGE_NAME !=null && !PROCESS_INST_TO_STAGE_NAME.isEmpty()){
        	 for (Map<String, Object> map : PROCESS_INST_TO_STAGE_NAME) {
             	for (Map.Entry<String, Object> entry : map.entrySet()) {
                     Object value = entry.getValue();
                     BigDecimal res = new BigDecimal(value.toString());
                     resultantObject.add(res);
                 }
             }
        }
        return resultantObject;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object[]> getApplicationTasksVariables(List<BigDecimal> variableValue) {
        
    	int size=variableValue.size();
    	
    	List<Object[]> queryResult=new ArrayList<Object[]>();
		String selectClause = "SELECT AHV1.LONG_, AHT.ID_ , AHV.TEXT_, AHT.END_TIME_  FROM ACT_HI_TASKINST AHT, ACT_HI_VARINST AHV, ACT_HI_VARINST AHV1 "
				+ "WHERE AHT.PROC_INST_ID_ = AHV.PROC_INST_ID_ AND AHT.PROC_INST_ID_ = AHV1.PROC_INST_ID_ AND AHV1.NAME_ = :ahv1.name"
				+ " AND AHV1.LONG_ IN :variableValue AND AHV.NAME_ = :ahv.name AND AHT.END_TIME_ IS NULL";
    	
    	for(int startIndex=0,endIndex=0;startIndex<size;startIndex=startIndex+BATCHSIZE)
    	{
    		endIndex=startIndex+BATCHSIZE;
    		if(endIndex>size)
    		{
    			endIndex=size;
    		}

    		Query nativeQuery = entityDao.getEntityManager().createNativeQuery(selectClause)
    				.setParameter("variableValue", variableValue.subList(startIndex, endIndex))
    				.setParameter("ahv1.name", "applicationId")
    				.setParameter("ahv.name", "stageName");
    		nativeQuery.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
    		List<Object[]> resultantObject  = nativeQuery.getResultList();
    		queryResult.addAll(resultantObject);
    	}
 
        return queryResult;
    }

    /*This method is used to get all completed stages */
    @Override
    public List<String> getAllHistoricTasksVariables(Map<String, Object> variableMap) {
        HistoricTaskInstanceQuery query = processEngine.getHistoryService().createHistoricTaskInstanceQuery();
        if (variableMap != null) {
            query = addHistoricVariables(query, variableMap);
        }
        List<HistoricTaskInstance> taskInstanceList = query.orderByHistoricTaskInstanceEndTime().asc().list();
        List<String> historicVariables = null;
        if (CollectionUtils.isNotEmpty(taskInstanceList)) {
            historicVariables = new ArrayList<String>();
            for (HistoricTaskInstance taskInstance : taskInstanceList) {
                if (taskInstance.getEndTime() == null) {
                    continue;
                } else {
                    HistoricVariableInstance variableInstance = getAllHistoricTasksVariables(
                            taskInstance.getProcessInstanceId(), "stageName");
                    historicVariables.add((String) variableInstance.getValue());
                }
            }
        }
        return historicVariables;
    }
    
    /*@Override
    public List<String> getAllHistoricProcessVariables(Map<String, Object> variableMap) {
    	HistoricProcessInstanceQuery query = processEngine.getHistoryService().createHistoricProcessInstanceQuery();
    	if (variableMap != null) {
            query = addHistoricVariables(query, variableMap);
        }
        List<HistoricProcessInstance> processInstanceList = query.orderByProcessInstanceId().asc().list();
        List<String> historicVariables = null;
        if (CollectionUtils.isNotEmpty(processInstanceList)) {
            historicVariables = new ArrayList<String>();
            for (HistoricProcessInstance processInstance : processInstanceList) {
            	if (processInstance.getEndTime() != null) {
                    continue;
                } else {
                    HistoricVariableInstance variableInstance = getAllHistoricTasksVariables(
                    		processInstance.getId(), "stageName");
                    historicVariables.add((String) variableInstance.getValue());
                }
            }
        }
        return historicVariables;
    }*/

	@Override
    public List<HistoricTaskInstance> getAllFinishedHistoricTasks(Map<String, Object> variableMap) {
        List<HistoricTaskInstance> taskInstanceList = null;

        String stageName = (String) variableMap.get(CoreWorkflowConstants.STAGE_NAME);
        Long applicationId = (Long) variableMap.get(CoreWorkflowConstants.APPLICATION_ID);
        String userAction = (String) variableMap.get(CoreWorkflowConstants.USER_ACTION);

        StringBuilder nativeQuery = new StringBuilder();
        nativeQuery
                .append("SELECT DISTINCT RES.* FROM ACT_HI_TASKINST RES, ACT_HI_VARINST AHV1, ACT_HI_VARINST AHV2, ACT_HI_VARINST AHV3 ")
                .append("WHERE RES.PROC_INST_ID_ = AHV1.PROC_INST_ID_  AND RES.PROC_INST_ID_ = AHV2.PROC_INST_ID_ AND RES.PROC_INST_ID_ = AHV3.PROC_INST_ID_ ")
                .append("AND RES.END_TIME_ IS NOT NULL AND AHV1.NAME_= #{AHV1.NAME} AND AHV1.TEXT_ = #{stageName}")
                .append("AND AHV2.NAME_= #{AHV2.NAME} AND AHV2.LONG_ = #{applicationId}")
                .append("AND AHV3.NAME_= #{AHV3.NAME} AND AHV3.TEXT_ = #{userAction}")
                .append("ORDER BY RES.END_TIME_ ASC");

        NativeHistoricTaskInstanceQuery query = processEngine.getHistoryService().createNativeHistoricTaskInstanceQuery()
                .sql(nativeQuery.toString());
        query.parameter("stageName", stageName);
        query.parameter("applicationId", applicationId);
        query.parameter("userAction", userAction);
        query.parameter("AHV1.NAME", "stageName"); 
        query.parameter("AHV2.NAME", "applicationId"); 
        query.parameter("AHV3.NAME", "userAction");
        

        taskInstanceList = query.list();

        if (CollectionUtils.isEmpty(taskInstanceList)) {
            taskInstanceList = getAllHistoricTasks(variableMap, false);
        }
        return taskInstanceList;
    }

    private HistoricTaskInstanceQuery addHistoricVariables(HistoricTaskInstanceQuery query, Map<String, Object> variableMap) {
        for (Map.Entry<String, Object> entry : variableMap.entrySet()) {
            query.processVariableValueEquals(entry.getKey(), entry.getValue());
        }
        return query;
    }
    
    /*private HistoricProcessInstanceQuery addHistoricVariables(HistoricProcessInstanceQuery query, Map<String, Object> variableMap) {
    	for (Map.Entry<String, Object> entry : variableMap.entrySet()) {
            query.variableValueEquals(entry.getKey(), entry.getValue());
        }
        return query;
	}*/
    
    private List<HistoricTaskInstance> executeHistoricTaskQuery(HistoricTaskInstanceQuery taskQuery) {
        List<HistoricTaskInstance> tasks = taskQuery.list();
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks;
    }

    @Override
    public List<Task> getUnassignedAdhocTasksForTeam(List<String> teamUris) {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskDefinitionKey("adhocTask")
                .taskCandidateGroupIn(teamUris).list();
        return tasks;
    }

    @Override
    public List<Task> getAssignedAdhocTasksForTeam(String teamUri) {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskDefinitionKey("adhocTask")
                .taskVariableValueEquals("assignedToTeam", teamUri).list();
        return tasks;
    }

    @Override
    public List<Task> getTasksForLoanApplication(Long appId) {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery()
                .processVariableValueEquals("applicationId", appId).list();
        return tasks;
    }

    @Override
    public List<Task> getAdhocTasksForAssignee(String assigneeUri) {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskDefinitionKey("adhocTask")
                .taskAssignee(assigneeUri).list();
        return tasks;
    }

    @Override
    public List<Task> getAssignedAdhocTasksForOwner(String ownerUri, String teamUri) {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskDefinitionKey("adhocTask")
                .taskVariableValueEquals("assignedToTeam", teamUri).taskOwner(ownerUri).list();
        return tasks;
    }

    @Override
    public List<Task> getAllAdhocTasksForOwner(String ownerUri) {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskDefinitionKey("adhocTask")
                .taskOwner(ownerUri).list();
        return tasks;
    }

    @Override
    public void addComment(String taskId, String processInstanceId, String message) {
        processEngine.getTaskService().addComment(taskId, processInstanceId, message);
    }

    @Override
    public List<Comment> getCommentsForTask(String taskId) {
        return processEngine.getTaskService().getTaskComments(taskId);
    }

    @Override
    public Object getTaskVariable(String taskId, String variableName) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");
        return processEngine.getTaskService().getVariableLocal(taskId, variableName);
    }
    
    @Override
    public Object getProcessVariableByTask(String taskId, String variableName) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");
        return processEngine.getTaskService().getVariable(taskId, variableName);
    }

    @Override
    public Map<String, Object> getVariables(String taskId) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");
        return processEngine.getTaskService().getVariables(taskId);
    }

    @Override
    public void setTaskVariable(String taskId, String variableName, Object value) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");
        processEngine.getTaskService().setVariableLocal(taskId, variableName, value);
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_PROCESS_VARS_FOR_TASK")
    public Object getProcessVariable(String processInstanceId, String variableName) {
        NeutrinoValidator.notEmpty(processInstanceId, "ProcessInstanceId couldn't be null or empty");
        BaseLoggers.flowLogger.debug("ProcessInstanceId " + processInstanceId + " variableName " + variableName);
        return processEngine.getRuntimeService().getVariable(processInstanceId, variableName);
    }

    @Override
    public void setProcessVariable(String processInstanceId, String variableName, Object value) {
        NeutrinoValidator.notEmpty(processInstanceId, "ProcessInstanceId couldn't be null or empty");
        processEngine.getRuntimeService().setVariable(processInstanceId, variableName, value);
    }

    @Override
    public String getGroupIdFromIdentityLinksForTask(String taskId) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");
        return processEngine.getTaskService().getIdentityLinksForTask(taskId).iterator().next().getGroupId();
    }

    @Override
    public List<Task> getUnassignedTasks(String processDefinitionKey, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        TaskQuery taskQuery = processEngine.getTaskService().createTaskQuery().includeProcessVariables()
                .processDefinitionKey(processDefinitionKey).taskUnassigned();
        if (variableMap != null) {
            taskQuery = addVariables(taskQuery, variableMap);
        }
        return execute(taskQuery);

    }

    @Override
    public List<Task> getTasksForVariables(String processDefinitionKey, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        TaskQuery query = processEngine.getTaskService().createTaskQuery().processDefinitionKey(processDefinitionKey);
        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return execute(query);
    }

    @Override
    public List<Task> getPaginatedTasksForVariables(String processDefinitionKey, Map<String, Object> variableMap,
            Integer displayStart, Integer displayRecords) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        String selectClause = createNativeQuery(processDefinitionKey, variableMap, false, null, true);
        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        List<Task> tasks;
        if (displayStart == null || displayRecords == null) {
            tasks = query.list();
        } else {
            tasks = query.listPage(displayStart, displayRecords);
        }
        if (tasks == null) {
            return Collections.emptyList();
        }

        return tasks;
    }

    @Override
    public List<Task> getPaginatedTasksForStage(Map<String, Object> variableMap, Integer displayStart, Integer displayRecords) {

        StringBuilder queryClause = new StringBuilder();

        String teamUris = (String) variableMap.get("teamUris");
        String stageName = (String) variableMap.get(CoreWorkflowConstants.STAGE_NAME);

        queryClause
                .append("SELECT DISTINCT ART.* FROM ACT_RU_TASK ART, ACT_RU_VARIABLE ARV2, ACT_RU_IDENTITYLINK ARI WHERE ART.PROC_INST_ID_ = ARV2.PROC_INST_ID_ AND ARI.GROUP_ID_ IN (")
                .append(teamUris).append(") AND ARV2.NAME_ = 'stageName' AND ARV2.TEXT_ = '").append(stageName).append("'");

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(queryClause.toString());
        List<Task> tasks;
        if (displayStart == null || displayRecords == null) {
            tasks = query.list();
        } else {
            tasks = query.listPage(displayStart, displayRecords);
        }
        if (tasks == null) {
            return Collections.emptyList();
        }

        return tasks;
    }

    @Override
    public Long getPaginatedTasksCountForStage(Map<String, Object> variableMap) {

        StringBuilder queryClause = new StringBuilder();

        String teamUris = (String) variableMap.get("teamUris");
        String stageName = (String) variableMap.get(CoreWorkflowConstants.STAGE_NAME);
        queryClause
                .append("SELECT COUNT(DISTINCT ART.ID_) FROM ACT_RU_TASK ART, ACT_RU_VARIABLE ARV2, ACT_RU_IDENTITYLINK ARI WHERE ART.PROC_INST_ID_ = ARV2.PROC_INST_ID_ AND ARI.GROUP_ID_ IN (")
                .append(teamUris).append(") AND ARV2.NAME_ = 'stageName' AND ARV2.TEXT_ = '").append(stageName).append("'");

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(queryClause.toString());

        return query.count();
    }

    @Override
    public List<Task> getAppTasksForVariables(String processDefinitionKey, Map<String, Object> variableMap,
            Integer displayStart, Integer displayRecords) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");

        String selectClause = createNativeQuery(processDefinitionKey, variableMap, false, null, false);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        List<Task> tasks;
        if (displayStart == null || displayRecords == null) {
            tasks = query.list();
        } else {
            tasks = query.listPage(displayStart, displayRecords);
        }
        if (tasks == null) {
            return Collections.emptyList();
        }

        return tasks;
    }

    @Override
    public Long getLeadTaskCount(String processDefinitionKey, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        String selectClause = createNativeQuery(processDefinitionKey, variableMap, true, null, true);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        return query.count();
    }

    @Override
    public Long getAppTaskCount(String processDefinitionKey, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");

        String selectClause = createNativeQuery(processDefinitionKey, variableMap, true, null, false);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);

        return query.count();
    }

    @SuppressWarnings("unchecked")
    private String createNativeQuery(String processDefinitionKey, Map<String, Object> variableMap, boolean isCount,
            String assignee, boolean searchInParameter) {
        StringBuilder stageList = null;
        StringBuilder workflowConfigList = null;
        StringBuilder teamIdList = null;
        StringBuilder nativeQueryString = new StringBuilder();
        if (variableMap != null) {
            List<String> stages = (List<String>) variableMap.get(CoreWorkflowConstants.STAGE_NAME);
            if (CollectionUtils.isNotEmpty(stages)) {
                stageList = new StringBuilder();
                for (int i = 0 ; i < stages.size() ; i++) {
                    stageList.append("'").append(stages.get(i)).append("'");
                    if (i != stages.size() - 1) {
                        stageList.append(",");
                    }
                }
            }

            List<String> workflowConfigs = (List<String>) variableMap.get(CoreWorkflowConstants.WORKFLOW_CONFIGURATION_TYPE);
            if (CollectionUtils.isNotEmpty(workflowConfigs)) {
                workflowConfigList = new StringBuilder();
                for (int i = 0 ; i < workflowConfigs.size() ; i++) {
                    workflowConfigList.append("'").append(workflowConfigs.get(i)).append("'");
                    if (i != workflowConfigs.size() - 1) {
                        workflowConfigList.append(",");
                    }
                }
            }

            List<Long> teamIds = (List<Long>) variableMap.get(CoreWorkflowConstants.ASSIGNED_TO_TEAM);
            if (CollectionUtils.isNotEmpty(teamIds)) {
                teamIdList = new StringBuilder();
                for (int i = 0 ; i < teamIds.size() ; i++) {
                    teamIdList.append("'").append(teamIds.get(i)).append("'");
                    if (i != teamIds.size() - 1) {
                        teamIdList.append(",");
                    }
                }
            }
        }
        String searchParameter = null;
        if (searchInParameter) {// This block executes when we use IN keyword for Stage and workflow configuration type list.
            searchParameter = "IN (";
        } else {// This block executes when we use NOT IN keyword for Stage and workflow configuration type list.
            searchParameter = "NOT IN (";
        }

        if (isCount) {// This block executes when we have to fetch only task count
            nativeQueryString.append("SELECT COUNT(DISTINCT ART.ID_) FROM ACT_RU_TASK ART");
        } else {// This block executes when we have to fetch the task
            nativeQueryString.append("SELECT DISTINCT ART.* FROM ACT_RU_TASK ART");
        }

        if (stageList != null && workflowConfigList != null && teamIdList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV1, ACT_RU_VARIABLE ARV2, ACT_RU_VARIABLE ARV3, ACT_RE_PROCDEF ARP "
                            + "WHERE ART.PROC_INST_ID_ = ARV1.PROC_INST_ID_ "
                            + "AND ART.PROC_INST_ID_ = ARV2.PROC_INST_ID_ " + "AND ART.PROC_INST_ID_ = ARV3.PROC_INST_ID_ "
                            + "AND ARP.ID_ = ART.PROC_DEF_ID_ AND ARP.KEY_ = '").append(processDefinitionKey).append("'");
            if (assignee != null) {
                nativeQueryString.append(" AND ART.ASSIGNEE_ = '").append(assignee).append("'");
            }
            nativeQueryString.append(" AND (ARV1.NAME_ = 'assignedToTeam' AND ARV1.LONG_ IN (").append(teamIdList)
                    .append("))").append(" AND (ARV2.NAME_ = 'workflowConfigurationType' AND ARV2.TEXT_ ")
                    .append(searchParameter).append(workflowConfigList).append("))")
                    .append(" AND (ARV3.NAME_ = 'stageName' AND ARV3.TEXT_ ").append(searchParameter).append(stageList)
                    .append("))");
        } else if (stageList != null && workflowConfigList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV2, ACT_RU_VARIABLE ARV3, ACT_RE_PROCDEF ARP "
                            + "WHERE ART.PROC_INST_ID_ = ARV2.PROC_INST_ID_ "
                            + "AND ART.PROC_INST_ID_ = ARV3.PROC_INST_ID_ "
                            + "AND ARP.ID_ = ART.PROC_DEF_ID_ AND ARP.KEY_ = '").append(processDefinitionKey).append("'");
            if (assignee != null) {
                nativeQueryString.append(" AND ART.ASSIGNEE_ = '").append(assignee).append("'");
            }
            nativeQueryString.append(" AND (ARV2.NAME_ = 'workflowConfigurationType' AND ARV2.TEXT_ ")
                    .append(searchParameter).append(workflowConfigList).append("))")
                    .append(" AND (ARV3.NAME_ = 'stageName' AND ARV3.TEXT_ ").append(searchParameter).append(stageList)
                    .append("))");
        } else if (stageList != null && teamIdList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV1, ACT_RU_VARIABLE ARV3, ACT_RE_PROCDEF ARP "
                            + "WHERE ART.PROC_INST_ID_ = ARV1.PROC_INST_ID_ "
                            + "AND ART.PROC_INST_ID_ = ARV3.PROC_INST_ID_ "
                            + "AND ARP.ID_ = ART.PROC_DEF_ID_ AND ARP.KEY_ = '").append(processDefinitionKey).append("'");
            if (assignee != null) {
                nativeQueryString.append(" AND ART.ASSIGNEE_ = '").append(assignee).append("'");
            }
            nativeQueryString.append(" AND (ARV1.NAME_ = 'assignedToTeam' AND ARV1.LONG_ IN (").append(teamIdList)
                    .append("))").append(" AND (ARV3.NAME_ = 'stageName' AND ARV3.TEXT_ ").append(searchParameter)
                    .append(stageList).append("))");
        } else if (workflowConfigList != null && teamIdList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV1, ACT_RU_VARIABLE ARV2, ACT_RE_PROCDEF ARP "
                            + "WHERE ART.PROC_INST_ID_ = ARV1.PROC_INST_ID_ "
                            + "AND ART.PROC_INST_ID_ = ARV2.PROC_INST_ID_ "
                            + "AND ARP.ID_ = ART.PROC_DEF_ID_ AND ARP.KEY_ = '").append(processDefinitionKey).append("'");
            if (assignee != null) {
                nativeQueryString.append(" AND ART.ASSIGNEE_ = '").append(assignee).append("'");
            }
            nativeQueryString.append(" AND (ARV1.NAME_ = 'assignedToTeam' AND ARV1.LONG_ IN (").append(teamIdList)
                    .append("))").append(" AND (ARV2.NAME_ = 'workflowConfigurationType' AND ARV2.TEXT_ ")
                    .append(searchParameter).append(workflowConfigList).append("))");
        } else if (teamIdList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV1, ACT_RE_PROCDEF ARP " + "WHERE ART.PROC_INST_ID_ = ARV1.PROC_INST_ID_ "
                            + "AND ARP.ID_ = ART.PROC_DEF_ID_ AND ARP.KEY_ = '").append(processDefinitionKey).append("'");
            if (assignee != null) {
                nativeQueryString.append(" AND ART.ASSIGNEE_ = '").append(assignee).append("'");
            }
            nativeQueryString.append(" AND (ARV1.NAME_ = 'assignedToTeam' AND ARV1.LONG_ IN (").append(teamIdList)
                    .append("))");
        } else if (workflowConfigList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV2, ACT_RE_PROCDEF ARP " + "WHERE ART.PROC_INST_ID_ = ARV2.PROC_INST_ID_ "
                            + "AND ARP.ID_ = ART.PROC_DEF_ID_ AND ARP.KEY_ = '").append(processDefinitionKey).append("'");
            if (assignee != null) {
                nativeQueryString.append(" AND ART.ASSIGNEE_ = '").append(assignee).append("'");
            }
            nativeQueryString.append(" AND (ARV2.NAME_ = 'workflowConfigurationType' AND ARV2.TEXT_ ")
                    .append(searchParameter).append(workflowConfigList).append("))");
        } else if (stageList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV3, ACT_RE_PROCDEF ARP " + "WHERE ART.PROC_INST_ID_ = ARV3.PROC_INST_ID_ "
                            + "AND ARP.ID_ = ART.PROC_DEF_ID_ AND ARP.KEY_ = '").append(processDefinitionKey).append("'");
            if (assignee != null) {
                nativeQueryString.append(" AND ART.ASSIGNEE_ = '").append(assignee).append("'");
            }
            nativeQueryString.append(" AND (ARV3.NAME_ = 'stageName' AND ARV3.TEXT_ ").append(searchParameter)
                    .append(stageList).append("))");
        } else {
            nativeQueryString.append(", ACT_RE_PROCDEF ARP WHERE ARP.ID_ = ART.PROC_DEF_ID_ AND ARP.KEY_ = '")
                    .append(processDefinitionKey).append("'");
            if (assignee != null) {
                nativeQueryString.append(" AND ART.ASSIGNEE_ = '").append(assignee).append("'");
            }
        }

        return nativeQueryString.toString();
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_TASKS_FOR_CANDIDATE_GROUP")
    public List<Task> getTasksForCandidateGroups(String processDefinitionKey, List<String> teamUris,
            Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        NeutrinoValidator.notEmpty(teamUris, "TeamUris couldn't be null or empty");
        TaskQuery taskQuery = processEngine.getTaskService().createTaskQuery().processDefinitionKey(processDefinitionKey)
                .taskCandidateGroupIn(teamUris);
        if (variableMap != null) {
            taskQuery = addVariables(taskQuery, variableMap);
        }
        return execute(taskQuery);

    }

    @Override
    @MonitoredWithSpring(name = "BPMN_PAG_TASKS_FOR_CANDIDATE_GROUP")
    public List<Task> getPaginatedTasksForCandidateGroups(String processDefinitionKey, List<String> teamUris,
            Map<String, Object> variableMap, Integer displayStart, Integer displayRecords) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        NeutrinoValidator.notEmpty(teamUris, "TeamUris couldn't be null or empty");
        String selectClause = createNativeQueryForTeamUris(processDefinitionKey, teamUris, variableMap, false, true);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        List<Task> tasks;
        if (displayStart == null || displayRecords == null) {
            tasks = query.list();
        } else {
            tasks = query.listPage(displayStart, displayRecords);
        }
        if (tasks == null) {
            return Collections.emptyList();
        }

        return tasks;

    }

    @Override
    @MonitoredWithSpring(name = "BPMN_PAG_TASKS_FOR_CANDIDATE_GROUP")
    public List<Task> getPaginatedTasksForCandidateGroups(List<String> teamUris, Map<String, Object> variableMap,
            Integer displayStart, Integer displayRecords) {
        NeutrinoValidator.notEmpty(teamUris, "TeamUris couldn't be null or empty");
        TaskQuery taskQuery = processEngine.getTaskService().createTaskQuery().taskCandidateGroupIn(teamUris);
        if (variableMap != null) {
            taskQuery = addVariables(taskQuery, variableMap);
        }
        return execute(taskQuery, displayStart, displayRecords);
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_PAG_TASKS_FOR_CANDIDATE_GROUP")
    public Long getTasksForCandidateGroupsCount(List<String> teamUris, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(teamUris, "TeamUris couldn't be null or empty");
        TaskQuery taskQuery = processEngine.getTaskService().createTaskQuery().taskCandidateGroupIn(teamUris);
        if (variableMap != null) {
            taskQuery = addVariables(taskQuery, variableMap);
        }
        return taskQuery.count();
    }

    @Override
    public Long getLeadPoolTaskCount(String processDefinitionKey, List<String> teamUris, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        NeutrinoValidator.notEmpty(teamUris, "TeamUris couldn't be null or empty");
        String selectClause = createNativeQueryForTeamUris(processDefinitionKey, teamUris, variableMap, true, true);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        return query.count();

    }

    @Override
    public Long getAppPoolTaskCount(String processDefinitionKey, List<String> teamUris, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        NeutrinoValidator.notEmpty(teamUris, "TeamUris couldn't be null or empty");

        String selectClause = null;
        if("disbursal_initiation_process".equalsIgnoreCase(processDefinitionKey)){
            selectClause = createNativeQueryForTeamUris(processDefinitionKey, teamUris, variableMap, true, true);
        }else{
            selectClause = createNativeQueryForTeamUris(processDefinitionKey, teamUris, variableMap, true, false);
        }
        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        return query.count();
    }

    @SuppressWarnings("unchecked")
    private String createNativeQueryForTeamUris(String processDefinitionKey, List<String> teamUris,
            Map<String, Object> variableMap, boolean isCount, boolean searchInParameter) {

        StringBuilder stageList = null;
        StringBuilder workflowConfigList = null;
        StringBuilder teamUriList = null;
        StringBuilder nativeQueryString = new StringBuilder();
        Boolean assigneeNull = null;
        if (variableMap != null) {
            List<String> stages = (List<String>) variableMap.get(CoreWorkflowConstants.STAGE_NAME);
            if (CollectionUtils.isNotEmpty(stages)) {
                stageList = new StringBuilder();
                for (int i = 0 ; i < stages.size() ; i++) {
                    stageList.append("'").append(stages.get(i)).append("'");
                    if (i != stages.size() - 1) {
                        stageList.append(",");
                    }
                }
            }

            List<String> workflowConfigs = (List<String>) variableMap.get(CoreWorkflowConstants.WORKFLOW_CONFIGURATION_TYPE);
            if (CollectionUtils.isNotEmpty(workflowConfigs)) {
                workflowConfigList = new StringBuilder();
                for (int i = 0 ; i < workflowConfigs.size() ; i++) {
                    workflowConfigList.append("'").append(workflowConfigs.get(i)).append("'");
                    if (i != workflowConfigs.size() - 1) {
                        workflowConfigList.append(",");
                    }
                }
            }

            teamUriList = new StringBuilder();
            for (int i = 0 ; i < teamUris.size() ; i++) {
                teamUriList.append("'").append(teamUris.get(i)).append("'");
                if (i != teamUris.size() - 1) {
                    teamUriList.append(",");
                }
            }

            assigneeNull = (Boolean) variableMap.get("assigneeNull");

        }

        String searchParameter = null;
        if (searchInParameter) {
            searchParameter = "IN (";
        } else {
            searchParameter = "NOT IN (";
        }

        String nullAssigneeString = null;
        if (assigneeNull == null) {
            nullAssigneeString = "AND ART.ASSIGNEE_ IS NULL ";
        }

        if (isCount) {
            nativeQueryString.append("SELECT COUNT(DISTINCT ART.ID_) FROM ACT_RU_TASK ART");
        } else {
            nativeQueryString.append("SELECT DISTINCT ART.* FROM ACT_RU_TASK ART");
        }

        if (stageList != null && workflowConfigList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV1, ACT_RU_VARIABLE ARV2, ACT_RE_PROCDEF ARP, ACT_RU_IDENTITYLINK ARI "
                            + "WHERE ART.PROC_INST_ID_ = ARV1.PROC_INST_ID_ "
                            + "AND ART.PROC_INST_ID_ = ARV2.PROC_INST_ID_ " + "AND ART.ID_ = ARI.TASK_ID_ "
                            + "AND ART.PROC_DEF_ID_ = ARP.ID_ AND ARP.KEY_ = '").append(processDefinitionKey).append("' ");

            if (assigneeNull == null) {
                nativeQueryString.append(nullAssigneeString);
            }

            nativeQueryString.append("AND ARI.TYPE_ = 'candidate' AND ARI.GROUP_ID_ IN (").append(teamUriList)
                    .append(") AND ARV1.NAME_ = 'workflowConfigurationType' AND ARV1.TEXT_ ").append(searchParameter)
                    .append(workflowConfigList).append(") AND ARV2.NAME_ = 'stageName' AND ARV2.TEXT_ ")
                    .append(searchParameter).append(stageList).append(")");
        } else if (workflowConfigList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV1, ACT_RE_PROCDEF ARP, ACT_RU_IDENTITYLINK ARI "
                            + "WHERE ART.PROC_INST_ID_ = ARV1.PROC_INST_ID_ " + "AND ART.ID_ = ARI.TASK_ID_ "
                            + "AND ART.PROC_DEF_ID_ = ARP.ID_ " + "AND ARP.KEY_ = '").append(processDefinitionKey)
                    .append("' ");

            if (assigneeNull == null) {
                nativeQueryString.append(nullAssigneeString);
            }

            nativeQueryString.append("AND ARI.TYPE_ = 'candidate' AND ARI.GROUP_ID_ IN (").append(teamUriList)
                    .append(") AND ARV1.NAME_ = 'workflowConfigurationType' AND ARV1.TEXT_ ").append(searchParameter)
                    .append(workflowConfigList).append(")");
        } else if (stageList != null) {
            nativeQueryString
                    .append(", ACT_RU_VARIABLE ARV2, ACT_RE_PROCDEF ARP, ACT_RU_IDENTITYLINK ARI "
                            + "WHERE ART.PROC_INST_ID_ = ARV2.PROC_INST_ID_ " + "AND ART.ID_ = ARI.TASK_ID_ "
                            + "AND ART.PROC_DEF_ID_ = ARP.ID_ " + "AND ARP.KEY_ = '").append(processDefinitionKey)
                    .append("' ");

            if (assigneeNull == null) {
                nativeQueryString.append(nullAssigneeString);
            }

            nativeQueryString.append("AND ARI.TYPE_ = 'candidate' AND ARI.GROUP_ID_ IN (").append(teamUriList)
                    .append(") AND ARV2.NAME_ = 'stageName' AND ARV2.TEXT_ ").append(searchParameter).append(stageList)
                    .append(")");
        } else {
            nativeQueryString
                    .append(", ACT_RE_PROCDEF ARP, ACT_RU_IDENTITYLINK ARI " + "WHERE ART.ID_ = ARI.TASK_ID_ "
                            + "AND ART.PROC_DEF_ID_ = ARP.ID_ " + "AND ARP.KEY_ = '").append(processDefinitionKey)
                    .append("' ");

            if (assigneeNull == null) {
                nativeQueryString.append(nullAssigneeString);
            }

            nativeQueryString.append("AND ARI.TYPE_ = 'candidate' AND ARI.GROUP_ID_ IN (").append(teamUriList).append(")");
        }

        return nativeQueryString.toString();
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_APP_TASKS_FOR_CANDIDATE_GROUP")
    public List<Task> getAppTasksForCandidateGroups(String processDefinitionKey, List<String> teamUris,
            Map<String, Object> variableMap, Integer displayStart, Integer displayRecords) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        NeutrinoValidator.notEmpty(teamUris, "TeamUris couldn't be null or empty");

        String selectClause = createNativeQueryForTeamUris(processDefinitionKey, teamUris, variableMap, false, false);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        List<Task> tasks;
        if (displayStart == null || displayRecords == null) {
            tasks = query.list();
        } else {
            tasks = query.listPage(displayStart, displayRecords);
        }
        if (tasks == null) {
            return Collections.emptyList();
        }

        return tasks;
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_TASKS_FOR_PROCESS_DEF")
    public List<Task> getAllTasksForProcessDef(String userUri, String processDefinitionKey, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        TaskQuery taskQuery = null;
        if (StringUtils.isEmpty(userUri)) {
            taskQuery = processEngine.getTaskService().createTaskQuery().processDefinitionKey(processDefinitionKey);
        } else {
            taskQuery = processEngine.getTaskService().createTaskQuery().processDefinitionKey(processDefinitionKey)
                    .taskAssignee(userUri);
        }
        if (variableMap != null) {
            taskQuery = addVariables(taskQuery, variableMap);
        }
        return execute(taskQuery);

    }

    @Override
    @MonitoredWithSpring(name = "BPMN_TASKS_FOR_CANDIDATE_GROUP_LIST")
    public List<Task> getTasksForCandidateGroupList(List<String> teamUris, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(teamUris, "TeamUris couldn't be null or empty");
        TaskQuery taskQuery = processEngine.getTaskService().createTaskQuery().taskCandidateGroupIn(teamUris);
        if (variableMap != null) {
            taskQuery = addVariables(taskQuery, variableMap);
        }
        return execute(taskQuery);

    }

    @Override
    public List<Task> getAssignedTasksForProcessDefinitionKey(String processDefinitionKey, String assignee,
            Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        String selectClause = createNativeQuery(processDefinitionKey, variableMap, false, assignee, false);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        List<Task> tasks = query.list();
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks;

    }

    @Override
    public Long getLeadAssigneeTaskCount(String processDefinitionKey, String assignee, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        String selectClause = createNativeQuery(processDefinitionKey, variableMap, true, assignee, true);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        return query.count();
    }

    @Override
    public Long getAppAssigneeTaskCount(String processDefinitionKey, String assignee, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        String selectClause = createNativeQuery(processDefinitionKey, variableMap, true, assignee, false);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        return query.count();
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_ASSIGNED_TASKS")
    public List<Task> getAssignedTasks(String processDefinitionKey, String assignee, Map<String, Object> variableMap,
            Integer displayStart, Integer displayLength) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        //NeutrinoValidator.notEmpty(assignee, "Assignee couldn't be null or empty");
        String selectClause = createNativeQuery(processDefinitionKey, variableMap, false, assignee, true);
        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        List<Task> tasks;
        if (displayStart == null || displayLength == null) {
            tasks = query.list();
        } else {
            tasks = query.listPage(displayStart, displayLength);
        }
        if (tasks == null) {
            return Collections.emptyList();
        }

        return tasks;
    }


    @Override
    @MonitoredWithSpring(name = "BPMN_ASSIGNED_TASKS")
    public List<Task> getAssignedTasks(String assignee, Map<String, Object> variableMap, Integer displayStart,
            Integer displayLength) {
        NeutrinoValidator.notEmpty(assignee, "Assignee couldn't be null or empty");
        TaskQuery query = processEngine.getTaskService().createTaskQuery().taskAssignee(assignee);

        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return execute(query, displayStart, displayLength);
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_ASSIGNED_TASKS")
    public List<Task> getAssignedTasks(String assignee, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(assignee, "Assignee couldn't be null or empty");
        TaskQuery query = processEngine.getTaskService().createTaskQuery().taskAssignee(assignee);

        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return execute(query);
    }

    @Override
    public Long getAssignedTaskCount(String processDefinitionKey, String assignee, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        NeutrinoValidator.notEmpty(assignee, "Assignee couldn't be null or empty");
        TaskQuery query = processEngine.getTaskService().createTaskQuery().taskAssignee(assignee)
                .processDefinitionKey(processDefinitionKey);

        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return query.count();
    }

    private List<Task> execute(TaskQuery taskQuery, Integer displayStart, Integer displayLength) {
        List<Task> tasks;
        if (displayStart == null || displayLength == null) {
            tasks = taskQuery.list();
        } else {
            tasks = taskQuery.listPage(displayStart, displayLength);
        }
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks;
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_ASSIGNED_TASKS_FOR_APP")
    public List<Task> getAssignedTasksForApplication(String processDefinitionKey, String assignee,
            Map<String, Object> variableMap, Integer displayStart, Integer displayLength) {
        NeutrinoValidator.notEmpty(processDefinitionKey, "ProcessDefinitionKey couldn't be null or empty");
        NeutrinoValidator.notEmpty(assignee, "Assignee couldn't be null or empty");
        String selectClause = createNativeQuery(processDefinitionKey, variableMap, false, assignee, false);

        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);

        List<Task> tasks;
        if (displayStart == null || displayLength == null) {
            tasks = query.list();
        } else {
            tasks = query.listPage(displayStart, displayLength);
        }
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks;

    }

    @Override
    public void setAssignee(String taskId, String userId) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");
        processEngine.getTaskService().setAssignee(taskId, userId);
    }

    private List<Task> execute(TaskQuery taskQuery) {
        List<Task> tasks = taskQuery.list();
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks;
    }

    private TaskQuery addVariables(TaskQuery query, Map<String, Object> variableMap) {
        for (Map.Entry<String, Object> entry : variableMap.entrySet()) {
            query.processVariableValueEquals(entry.getKey(), entry.getValue());
        }
        return query;
    }

    @Override
    public Task getTaskByProcessId(String processInstanceId) {
        NeutrinoValidator.notEmpty(processInstanceId, "Process Instance Id couldn't be null or empty");
        // It is assumed for now that start event will bind to only one task..
        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        return task;
    }

    @Override
    public ProcessInstance getSubProcessInstanceByProcessId(String processInstanceId) {
        ProcessInstance processInstance = processEngine.getRuntimeService().createProcessInstanceQuery()
                .superProcessInstanceId(processInstanceId).singleResult();
        /*HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().superProcessInstanceId(processInstanceId).singleResult();
        return historicProcessInstance;*/
        return processInstance;
    }

    @Override
    public List<Task> getTaskListByProcessId(String processInstanceId) {
        NeutrinoValidator.notEmpty(processInstanceId, "Process Instance Id couldn't be null or empty");
        List<Task> task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        return task;
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_ASSIGNED_TASKS_FOR_USER")
    public List<Task> getAssignedTasksForUser(String assignee, Map<String, Object> variableMap) {
        NeutrinoValidator.notEmpty(assignee, "Assignee couldn't be null or empty");
        BaseLoggers.flowLogger.debug("getAssignedTasks" + assignee);
        TaskQuery query = processEngine.getTaskService().createTaskQuery().taskAssignee(assignee);

        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return execute(query);
    }

    @SuppressWarnings("unchecked")
    @Override
    @MonitoredWithSpring(name = "BPMN_ASSIGNED_TASKS_FOR_USER_DUE_DATE")
    public List<Task> getAssignedTasksForUserAndDueDate(String assignee, Map<String, Object> variableMap, Date expiryDate,
            Integer totalDisplayRecords) {
        NeutrinoValidator.notEmpty(assignee, "Assignee couldn't be null or empty");
        BaseLoggers.flowLogger.debug("getAssignedTasks" + assignee);
        List<String> assignedToTeam = (List<String>) variableMap.get(CoreWorkflowConstants.ASSIGNED_TO_TEAM);

        String nativeTaskQuery = getNativeTaskQuery(assignedToTeam);      
        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery()
        							.sql(nativeTaskQuery)
        								.parameter("assignee", assignee)
        								   .parameter("expiryDate", expiryDate);
        List<Task> tasks;
        if (totalDisplayRecords == null) {
            tasks = query.list();
        } else {
            tasks = query.listPage(0, totalDisplayRecords);
        }
        if (tasks == null) {
            return Collections.emptyList();
        }
        return tasks;
    }

    private String getNativeTaskQuery(List<String> assignedToTeam) {
        StringBuilder assignedTeamUris = getCommaSeparatedAssignedTeamUris(assignedToTeam);

    	String nativeTaskQuery = "select distinct RES.* from ACT_RU_TASK RES inner join ACT_RU_IDENTITYLINK I on I.TASK_ID_ = RES.ID_"
                +" WHERE  RES.ASSIGNEE_ = #{assignee}  and RES.DUE_DATE_ < #{expiryDate}   and RES.DUE_DATE_ is not null and I.TYPE_ = 'candidate'"
                +" and (  I.GROUP_ID_ IN ("+assignedTeamUris.toString()+")) order by RES.DUE_DATE_ asc ";
    	
    	return nativeTaskQuery;
	}

	private StringBuilder getCommaSeparatedAssignedTeamUris(List<String> assignedToTeam) {
    	StringBuilder assignedTeamUris;
    	
    	if(assignedToTeam != null && ! assignedToTeam.isEmpty()){
    		assignedTeamUris  = new StringBuilder();    		
    		for (String team : assignedToTeam) {
            	assignedTeamUris.append("'").append(team).append("'").append(",");
            }
            assignedTeamUris.deleteCharAt(assignedTeamUris.length()-1);
    	}else{
    		assignedTeamUris  = new StringBuilder("\'\'");
    	}
        
    	return assignedTeamUris;
	}
	
    @Override
    public void signalEventReceived(String signalName, String processInstanceId, Map<String, Object> processVars) {
        Execution execution = processEngine.getRuntimeService().createExecutionQuery().signalEventSubscriptionName("unhold")
                .processInstanceId(processInstanceId).singleResult();
        if (execution != null) {
            processEngine.getRuntimeService().signalEventReceived("unhold", execution.getId(), processVars);
        }
    }

    @Override
    public List<Execution> getExecutionsForAssigneeUri(String userUri) {
        NeutrinoValidator.notEmpty(userUri, "UserUri couldn't be null or empty");
        List<Execution> executions = processEngine
                .getRuntimeService()
                .createExecutionQuery()
                .variableValueEquals("previousAssignee", userUri)
                .variableValueEquals(
                        "workflowConfigurationType",
                        genericParameterService.findByCode(WorkflowConfigurationType.LEAD_WORKFLOW_CONFIG,
                                WorkflowConfigurationType.class).getUri()).list();
        return executions;
    }

    @Override
    public List<Execution> getApplicationExecutionsForAssigneeUri(String userUri) {
        NeutrinoValidator.notEmpty(userUri, "UserUri couldn't be null or empty");
        List<Execution> executions = processEngine
                .getRuntimeService()
                .createExecutionQuery()
                .variableValueEquals("previousAssignee", userUri)
                .variableValueEquals("userAction", "hold")
                .variableValueNotEquals(
                        "workflowConfigurationType",
                        genericParameterService.findByCode(WorkflowConfigurationType.LEAD_WORKFLOW_CONFIG,
                                WorkflowConfigurationType.class).getUri()).list();
        return executions;
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_SENT_APPROVAL_TASKS")
    public List<Task> getSendForApprovalTasks(String taskAssignee) {
        NeutrinoValidator.notEmpty(taskAssignee, "taskAssignee couldn't be null or empty");
        List<Task> tasks = processEngine.getTaskService().createTaskQuery()
                .taskDefinitionKey(MasterApprovalFlowConstants.CHECKER_APPROVAL_TASK_WF_ID).taskAssignee(taskAssignee)
                .list();

        return tasks;
    }

    @Override
    public List<Long> getApplicationsBasedOnStage(String stageName) {
        List<Task> tasks = processEngine.getTaskService().createTaskQuery()
                .processVariableValueEquals("stageName", stageName).list();
        List<Long> applicationIds = new ArrayList<Long>();
        for (Task task : tasks) {
            // Map<String, Object> processVariables = task.getProcessVariables();
            applicationIds.add((Long) getProcessVariable(task.getProcessInstanceId(), "applicationId"));
        }
        return applicationIds;
    }

    @Override
    public void changeAssignedTeam(String taskId, String oldTeamUri, String newTeamUri) {
        processEngine.getTaskService().deleteCandidateGroup(taskId, oldTeamUri);
        processEngine.getTaskService().addCandidateGroup(taskId, newTeamUri);

    }

    @Override
    public void changeCurrentProcessingVariable(String executionId, String variableName, Object value) {
        processEngine.getRuntimeService().setVariable(executionId, variableName, value);
    }

    @Override
    public List<String> getTeamUriByTaskId(String taskId) {
        List<String> teamList = new ArrayList<String>();
        List<IdentityLink> list = processEngine.getTaskService().getIdentityLinksForTask(taskId);
        for (IdentityLink iLink : list) {
            if (iLink.getType().equals(IdentityLinkType.CANDIDATE)) {
                teamList.add(iLink.getGroupId());
            }
        }
        return teamList;

    }

    @Override
    public List<Task> getAllLeadTasksWithinTimeSpan(Map<String, Object> variableMap, Date startDate, Date endDate,
            Integer displayStart, Integer displayRecords) {
        TaskQuery query = null;

        if (null != startDate && null != endDate) {
            query = processEngine.getTaskService().createTaskQuery().taskCreatedBefore(endDate).taskCreatedAfter(startDate);
        } else if (null != startDate) {
            query = processEngine.getTaskService().createTaskQuery().taskCreatedAfter(startDate);
        } else if (null != endDate) {
            query = processEngine.getTaskService().createTaskQuery().taskCreatedBefore(endDate);
        } else {
            query = processEngine.getTaskService().createTaskQuery();
        }
        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return execute(query, displayStart, displayRecords);
    }

    @Override
    public Long getAllLeadTasksWithinTimeSpanCount(Map<String, Object> variableMap, Date startDate, Date endDate) {
        TaskQuery query = null;

        if (null != startDate && null != endDate) {
            query = processEngine.getTaskService().createTaskQuery().taskCreatedBefore(endDate).taskCreatedAfter(startDate);
        } else if (null != startDate) {
            query = processEngine.getTaskService().createTaskQuery().taskCreatedAfter(startDate);
        } else if (null != endDate) {
            query = processEngine.getTaskService().createTaskQuery().taskCreatedBefore(endDate);
        } else {
            query = processEngine.getTaskService().createTaskQuery();
        }

        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return query.count();
    }

    @Override
    public List<WorkflowTaskVO> getTaskStatusReport(List<String> userUriList, Date startDate, Date endDate) {
        HistoricTaskInstanceQuery query = null;
        List<WorkflowTaskVO> workflowtaskVoList = new ArrayList<WorkflowTaskVO>();

        for (String userUri : userUriList) {
            int taskcompleteOnTime = 0;
            int taskCompletedBeforeTime = 0;
            int taskCompletedAfterTime = 0;
            int taskNotCompleteAfterTime = 0;
            int taskNotCompleteBeforeTime = 0;
            int taskNotCompleteOnTime = 0;
            if (null != startDate && null != endDate) {
                query = processEngine.getHistoryService().createHistoricTaskInstanceQuery().taskAssignee(userUri)
                        .taskCreatedBefore(endDate).taskCreatedAfter(startDate);
            } else if (null != startDate) {
                query = processEngine.getHistoryService().createHistoricTaskInstanceQuery().taskAssignee(userUri)
                        .taskCreatedAfter(startDate);
            } else if (null != endDate) {
                query = processEngine.getHistoryService().createHistoricTaskInstanceQuery().taskAssignee(userUri)
                        .taskCreatedBefore(endDate);
            } else {
                query = processEngine.getHistoryService().createHistoricTaskInstanceQuery().taskAssignee(userUri);
            }

            List<HistoricTaskInstance> historicTaskInstanceList = query.list();
            for (HistoricTaskInstance historicTaskInstance : historicTaskInstanceList) {
                Date dueDate = historicTaskInstance.getDueDate();
                Date endTime = historicTaskInstance.getEndTime();
                Calendar calendar = Calendar.getInstance();
                int dateStatus = 0;
                if (endTime != null) {
                    dateStatus = dueDate.compareTo(endTime);
                    if (dateStatus < 0) {
                        taskCompletedAfterTime++;
                    } else if (dateStatus > 0) {
                        taskCompletedBeforeTime++;
                    } else {
                        taskcompleteOnTime++;
                    }
                } else {
                    Date currentDate = calendar.getTime();
                    dateStatus = dueDate.compareTo(currentDate);
                    if (dateStatus < 0) {
                        taskNotCompleteAfterTime++;
                    } else if (dateStatus > 0) {
                        taskNotCompleteBeforeTime++;
                    } else {
                        taskNotCompleteOnTime++;
                    }
                }
            }
            WorkflowTaskVO workflowTaskVO = new WorkflowTaskVO();
            workflowTaskVO.setAssigneeName(userService.getUserNameByUserUri(userUri));
            workflowTaskVO.setTaskCompletedAfterTime(taskCompletedAfterTime);
            workflowTaskVO.setTaskCompletedBeforeTime(taskCompletedBeforeTime);
            workflowTaskVO.setTaskcompleteOnTime(taskcompleteOnTime);
            workflowTaskVO.setTaskNotCompleteAfterTime(taskNotCompleteAfterTime);
            workflowTaskVO.setTaskNotCompleteBeforeTime(taskNotCompleteBeforeTime);
            workflowTaskVO.setTaskNotCompleteOnTime(taskNotCompleteOnTime);

            workflowtaskVoList.add(workflowTaskVO);
        }

        return workflowtaskVoList;
    }

    @Override
    public String getSuperProcessInstanceId(String processInstanceId) {
        return processEngine.getRuntimeService().createProcessInstanceQuery().subProcessInstanceId(processInstanceId)
                .singleResult().getProcessDefinitionId();
    }

    @Override
    public ProcessInstance getSuperProcessInstance(String processInstanceId) {
        return processEngine.getRuntimeService().createProcessInstanceQuery().subProcessInstanceId(processInstanceId)
                .singleResult();
    }

    @Override
    public ProcessInstance getProcessInstanceId(String processInstanceId) {
        return processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId)
                .includeProcessVariables().singleResult();
    }

    @Override
    public Task getTaskIncludingProcessVariables(String taskId) {
        return processEngine.getTaskService().createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_PAG_TASKS_FOR_CANDIDATE_GROUP")
    public List<ProcessInstance> getPaginatedProcessInstance(Map<String, Object> variableMap) {
        ProcessInstanceQuery processInstanceQuery = processEngine.getRuntimeService().createProcessInstanceQuery();
        for (Map.Entry<String, Object> entry : variableMap.entrySet()) {
            processInstanceQuery.variableValueEquals(entry.getKey(), entry.getValue());
        }
        return processInstanceQuery.list();
    }

    @Override
    public List<Task> getAllTasksIncludeVariables(Map<String, Object> variableMap) {
        TaskQuery query = processEngine.getTaskService().createTaskQuery().includeProcessVariables();
        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return execute(query);
    }

    @Override
    public List<Task> getAllUnassignedTasks(Map<String, Object> variableMap) {
        TaskQuery query = processEngine.getTaskService().createTaskQuery().taskUnassigned();
        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return execute(query);
    }

    @Override
    @MonitoredWithSpring(name = "BPMN_ASSIGNED_TASKS_FOR_USER")
    public List<Task> getAssignedTasksForUser(String assignee, Map<String, Object> variableMap, int displayStart,
            int displayLength) {
        NeutrinoValidator.notEmpty(assignee, "Assignee couldn't be null or empty");
        BaseLoggers.flowLogger.debug("getAssignedTasks" + assignee);
        TaskQuery query = processEngine.getTaskService().createTaskQuery().taskAssignee(assignee);

        if (variableMap != null) {
            query = addVariables(query, variableMap);
        }
        return execute(query, displayStart, displayLength);
    }

    @Override
    public List<HistoricTaskInstance> getAllNativeHistoricTasksforUser(Map<String, Object> variableMap,
            Boolean finishedTask, Boolean searchTask, Boolean isPaginated, int displayStart, int displayLength) {
        List<HistoricTaskInstance> historicTaskInstances = null;
        String selectClause = createHistoricTaskNativeQuery(finishedTask, variableMap, searchTask);
        if (StringUtils.isEmpty(selectClause)) {
            return Collections.emptyList();
        }
        NativeHistoricTaskInstanceQuery query = processEngine.getHistoryService().createNativeHistoricTaskInstanceQuery()
                .sql(selectClause);
        if (isPaginated) {
            historicTaskInstances = query.listPage(displayStart, displayLength);
        } else {

            historicTaskInstances = query.list();
        }
        if (CollectionUtils.isEmpty(historicTaskInstances)) {
            return Collections.emptyList();
        }

        return historicTaskInstances;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getAllProcInstanceforUser(Map<String, Object> variableMap, Boolean finishedTask, Boolean searchTask,
            Boolean isPaginated, int displayStart, int displayLength) {
        StringBuilder queryClause = new StringBuilder();
        List<String> userList = null;
        String isFinished = null;
        String taskType = null;
        String searchOn = null;
        if (variableMap != null) {
            searchOn = (String) variableMap.get("searchOn");
            taskType = (String) variableMap.get(searchOn);
            if (StringUtils.isEmpty(taskType) || StringUtils.isEmpty(searchOn)) {
                return Collections.emptyList();
            }
            userList = (List<String>) variableMap.get(CoreWorkflowConstants.USER_LIST);
            if (finishedTask != null && finishedTask) {
                isFinished = " AND RES.END_TIME_ IS NOT NULL";
            } else if (finishedTask != null && !finishedTask) {
                isFinished = " AND RES.END_TIME_ IS NULL";
            }
        }

        if (CollectionUtils.isNotEmpty(userList)) {
            queryClause.append("SELECT DISTINCT RES.PROC_INST_ID_ FROM ACT_HI_TASKINST RES, ACT_HI_VARINST AHV ");

            if (searchTask != null && searchTask) {
                queryClause.append(", ACT_HI_VARINST AHV1 ");
            }

            queryClause.append("WHERE RES.PROC_INST_ID_ = AHV.PROC_INST_ID_ ");
            if (searchTask != null && searchTask) {
                queryClause.append("AND RES.PROC_INST_ID_ = AHV1.PROC_INST_ID_ ");
            }
            queryClause.append("AND RES.ASSIGNEE_ IN :userList");

            if (StringUtils.isNotEmpty(isFinished)) {
                queryClause.append(isFinished);
            }
            queryClause.append(" AND AHV.NAME_ = :searchOn  AND AHV.TEXT_ = :taskType");
        } else {
            return Collections.emptyList();
        }
        String selectClause = queryClause.toString();
        Query nativeQuery = entityDao.getEntityManager().createNativeQuery(selectClause)
                .setParameter("userList", userList).setParameter("searchOn", searchOn).setParameter("taskType", taskType);
        nativeQuery.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
        List<String> resultantObject = nativeQuery.getResultList();

        return resultantObject;
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<String> getProcInstanceforRCUUser(Map<String, Object> variableMap, Boolean finishedTask, Boolean searchTask,
            Boolean isPaginated, int displayStart, int displayLength) {
        StringBuilder queryClause = new StringBuilder();
        List<String> userList = null;
        String isFinished = null;
        List<Long> applicationIds = null;
        if (variableMap != null) {
        	applicationIds = (List<Long>) variableMap.get("applicationIds");
            userList = (List<String>) variableMap.get(CoreWorkflowConstants.USER_LIST);
            if (finishedTask != null && finishedTask) {
                isFinished = " AND RES.END_TIME_ IS NOT NULL";
            } else if (finishedTask != null && !finishedTask) {
                isFinished = " AND RES.END_TIME_ IS NULL";
            }
        }
        if (CollectionUtils.isNotEmpty(userList)) {
        	  queryClause.append("SELECT DISTINCT(RES.PROC_INST_ID_) FROM ACT_HI_TASKINST RES, ACT_HI_VARINST AHV ");
              queryClause.append("WHERE RES.PROC_INST_ID_ = AHV.PROC_INST_ID_ ");
              queryClause.append("AND RES.ASSIGNEE_ IN :userList");
            if (StringUtils.isNotEmpty(isFinished)) {
                queryClause.append(isFinished);
            }
            queryClause.append(" AND AHV.LONG_ IN :appList").append(" AND AHV.NAME_ = :nameType ");
        } else {
            return Collections.emptyList();
        }
        List<String> resultantObject = null;
        if(CollectionUtils.isNotEmpty(userList) && CollectionUtils.isNotEmpty(applicationIds)){
        	String selectClause = queryClause.toString();
        	Query nativeQuery = entityDao.getEntityManager().createNativeQuery(selectClause).setParameter("userList", userList).setParameter("appList", applicationIds).setParameter("nameType", "applicationId");
        	nativeQuery.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
        	resultantObject = nativeQuery.getResultList();
        }
        return resultantObject;
    }

    private String createHistoricTaskNativeQuery(Boolean finishedTask, Map<String, Object> variableMap, Boolean searchTask) {
    	Map<Integer,StringBuilder> userListMap = null;
        String isFinished = null;
        String taskType = null;
        String searchOn = null;
        StringBuilder nativeQueryString = new StringBuilder();
        if (variableMap != null) {
            searchOn = (String) variableMap.get("searchOn");
            taskType = (String) variableMap.get(searchOn);
            if (StringUtils.isEmpty(taskType) || StringUtils.isEmpty(searchOn)) {
                return null;
            }
            List<User> users = (List<User>) variableMap.get(CoreWorkflowConstants.USER_LIST);
            if (CollectionUtils.isNotEmpty(users)) {
            	userListMap = prepareInClauseBatchesForDynamicQuery(users);
            }
            if (finishedTask != null && finishedTask) {
                isFinished = "AND RES.END_TIME_ IS NOT NULL";
            } else if (finishedTask != null && !finishedTask) {
                isFinished = "AND RES.END_TIME_ IS NULL";
            }
        }
        if (! userListMap.isEmpty()) {
            nativeQueryString.append("SELECT DISTINCT RES.* FROM ACT_HI_TASKINST RES, ACT_HI_VARINST AHV ");

            if (searchTask != null && searchTask) {
                nativeQueryString.append(", ACT_HI_VARINST AHV1 ");
            }

            nativeQueryString.append("WHERE RES.PROC_INST_ID_ = AHV.PROC_INST_ID_ ");
            if (searchTask != null && searchTask) {
                nativeQueryString.append("AND RES.PROC_INST_ID_ = AHV1.PROC_INST_ID_ ");
            }
            
            nativeQueryString.append("AND ( ");

            for(int i=0;i<userListMap.size();i++){
            	nativeQueryString.append("RES.ASSIGNEE_ IN (").append(userListMap.get(i).toString()).append(") ");
            	if(i != userListMap.size()-1){
                	nativeQueryString.append(" OR ");
            	}
            }
            
            nativeQueryString.append(") ");
            
            if (StringUtils.isNotEmpty(isFinished)) {
                nativeQueryString.append(isFinished);
            }
            nativeQueryString.append(" AND AHV.NAME_ = '" + searchOn + "' AND AHV.TEXT_ = '" + taskType + "'");
        } else {
            return null;
        }
        if (searchTask != null && searchTask) {
            Long applicationId = (Long) variableMap.get(CoreWorkflowConstants.APPLICATION_ID);
            nativeQueryString.append(" AND AHV1.NAME_ = 'applicationId' AND AHV1.LONG_ = '" + applicationId + "'");
        }

        return nativeQueryString.toString();
    }

    private Map<Integer,StringBuilder> prepareInClauseBatchesForDynamicQuery(List<User> userList){
    	
    	Map<Integer,StringBuilder>  userListMap = new HashMap<Integer, StringBuilder>();
    	
        int fromIndex = 0;
        int batchCount = 0;
        int toIndex = userList.size() > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : userList.size();
     
        if(userList.size() <= ORACLE_BATCH_SIZE){
            
        	StringBuilder userUriList = new StringBuilder();
        	prepareUserUriList(userList,userUriList,userList.size());
        	userListMap.put(batchCount, userUriList);        	

        }else{
		        while (toIndex <= userList.size() && fromIndex < toIndex) {
		        	List<User>  userSubList = new ArrayList<User>(userList.subList(fromIndex, toIndex));
		            StringBuilder userUriList = new StringBuilder();
		                 	
		                fromIndex = toIndex;
		                int difference = userList.size() - toIndex;
		                if(difference <= 0){
		                	break;
		                }
		                int batchSize = difference > ORACLE_BATCH_SIZE ? ORACLE_BATCH_SIZE : difference;
		                toIndex = toIndex + batchSize;
		                
		                if (!userSubList.isEmpty()) {
		                	prepareUserUriList(userSubList,userUriList,batchSize);
		                }
		           	
			           	userListMap.put(batchCount, userUriList);
			           	batchCount++;
			    }
        }
        
		return userListMap;
    	
    }

	private void prepareUserUriList(List<User>  userSubList,StringBuilder userUriList,int toIndex) {
	
		for (int i = 0 ; i < toIndex ; i++) {
			userUriList.append(SINGLE_QUOTE).append(userSubList.get(i).getUri()).append(SINGLE_QUOTE);
	        if (i < toIndex - 1) {
	       	 userUriList.append(COMMA);
	        }			
		}
	}
    
    @Override
    public Long fetchLoginComparison(String selectClause, Map<String, Object> parameters) {
        NativeTaskQuery query = processEngine.getTaskService().createNativeTaskQuery().sql(selectClause);
        query.parameter("startDate", parameters.get("startDate"));
        query.parameter("endDate", parameters.get("endDate"));
        return query.count();
    }

    // performance change utility methods
    @Override
    public Map<String, String> getTextVariablesByProcInstIds(List<String> procInstIds, String variableName) {

        if(procInstIds==null || procInstIds.isEmpty())
        {
            return new HashMap<String,String>();
        }

        Map<String, String> PROCESS_INST_TO_TEXT_VAR_MAP = new HashMap<String, String>(procInstIds.size());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("variableName", variableName);
        List<Map<String, Object>> PROCESS_INST_TO_STAGE_NAME = neutrinoJdbcTemplate.queryForListWithSingleInClause(
                "ACT_VAR_TEXT_VALUE_BY_PROCESS_INSTANCE_IDs", "processInstIds", procInstIds, params);
        for (Map<String, Object> map : PROCESS_INST_TO_STAGE_NAME) {

            PROCESS_INST_TO_TEXT_VAR_MAP.put((String) map.get("processInstId"), (String) map.get("textValue"));
        }
        return PROCESS_INST_TO_TEXT_VAR_MAP;
    }

    @Override
    public Map<String, Long> getLongVariablesByProcInstIds(List<String> procInstIds, String variableName) {

        if(procInstIds==null || procInstIds.isEmpty())
        {
            return new HashMap<String,Long>();
        }

        Map<String, Long> PROCESS_INST_TO_LONG_VAR_MAP = new HashMap<String, Long>(procInstIds.size());
        Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("variableName", variableName);
        List<Map<String, Object>> PROCESS_INST_TO_APPID = neutrinoJdbcTemplate.queryForListWithSingleInClause(
                "ACT_VAR_LONG_VALUE_BY_PROCESS_INSTANCE_IDs", "processInstIds", procInstIds, params2);
        for (Map<String, Object> map : PROCESS_INST_TO_APPID) {
            Number number = (Number) map.get("longValue");
            PROCESS_INST_TO_LONG_VAR_MAP.put((String) map.get("processInstId"), number != null ? number.longValue() : null);
        }
        return PROCESS_INST_TO_LONG_VAR_MAP;
    }

    @Override
    public Map<String, Long> getAppIdsByProcessInstIds(Collection<Task> taskList) {

        if(taskList==null || taskList.isEmpty())
        {
            return new HashMap<String,Long>();
        }

        List<String> processInstIds = new ArrayList<String>();
        for (Task userTask : taskList) {
            if (userTask != null) {
                processInstIds.add(userTask.getProcessInstanceId());
            }
        }
        return getLongVariablesByProcInstIds(processInstIds, CoreWorkflowConstants.APPLICATION_ID);
    }

    @Override
    public Map<String, String> getStageNamesByProcessInstIds(Collection<Task> taskList) {

        if(taskList==null || taskList.isEmpty())
        {
            return new HashMap<String,String>();
        }

        List<String> processInstIds = new ArrayList<String>();
        for (Task userTask : taskList) {
            if (userTask != null) {
                processInstIds.add(userTask.getProcessInstanceId());
            }
        }

        return getTextVariablesByProcInstIds(processInstIds, CoreWorkflowConstants.STAGE_NAME);
    }

    @Override
    public Map<String, List<String>> getTeamUriListsByTaskId(Collection<Task> taskList) {

        if(taskList==null || taskList.isEmpty())
        {
            return new HashMap<String, List<String>>();
        }

        List<String> taskIds = new ArrayList<String>();


        for (Task userTask : taskList) {
            if (userTask != null) {
                taskIds.add(userTask.getId());
            }
        }

        Map<String, List<String>> IDENTITY_LINK_GROUP_IDs_BY_TASK_ID = new HashMap<String, List<String>>(taskIds.size());

        Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("identityLinkType", IdentityLinkType.CANDIDATE);
        List<Map<String, Object>> PROCESS_INST_TO_APPID = neutrinoJdbcTemplate.queryForListWithSingleInClause(
                "IDENTITY_LINK_GROUP_IDs_BY_TASK_IDs", "taskIds", taskIds, params2);
        for (Map<String, Object> map : PROCESS_INST_TO_APPID) {
            String taskId = (String) map.get("taskId");
            List<String> IDENTITY_LINK_GROUP_IDs = IDENTITY_LINK_GROUP_IDs_BY_TASK_ID.get(taskId);
            if (IDENTITY_LINK_GROUP_IDs == null) {
                IDENTITY_LINK_GROUP_IDs = new ArrayList<String>();
                IDENTITY_LINK_GROUP_IDs_BY_TASK_ID.put(taskId, IDENTITY_LINK_GROUP_IDs);
            }
            IDENTITY_LINK_GROUP_IDs.add((String) map.get("groupId"));

        }
        return IDENTITY_LINK_GROUP_IDs_BY_TASK_ID;

    }

    @Override
    public Map<String, Object> getVariablesForProcessInstanceId(String pid) {
    	Map<String, Object> map=processEngine.getRuntimeService().getVariables(pid);
    	return map;
    }
    @Override
	public Long getTasksCountForLoanApplication(Long appId) {
        return processEngine.getTaskService().createTaskQuery().processVariableValueEquals("applicationId", appId).count();
    }
    @Override
    public Task getTaskByTaskIdWithProcessVariable(String taskId) {
        NeutrinoValidator.notEmpty(taskId, "TaskId couldn't be null or empty");

        TaskQuery query = processEngine.getTaskService().createTaskQuery().includeProcessVariables();
        query.taskId(taskId);

        return query.singleResult();
    }

    @Override
    public List<HistoricTaskInstance> getAllNativeHistoricTasksForIds(List<String> taskIds) {
        StringBuilder userList = new StringBuilder();

        if (CollectionUtils.isNotEmpty(taskIds)) {
            for (int i = 0 ; i < taskIds.size() ; i++) {
                userList.append("'").append(taskIds.get(i)).append("'");
                if (i != taskIds.size() - 1) {
                    userList.append(",");
                }
            }
        }

        StringBuffer nativeQueryString = new StringBuffer();
        nativeQueryString.append("SELECT DISTINCT RES.* FROM ACT_HI_TASKINST RES WHERE RES.ID_ IN  (").append(userList)
                .append(")");

        if (StringUtils.isEmpty(nativeQueryString)) {
            return Collections.emptyList();
        }
        NativeHistoricTaskInstanceQuery query = processEngine.getHistoryService().createNativeHistoricTaskInstanceQuery()
                .sql(nativeQueryString.toString());

        return query.list();
    }

    @Override
    public Map<String, List<String>> getTeamUriByTaskId(Collection<String> taskIds) {

        Map<String, List<String>> finalResult = new HashMap<String, List<String>>();

        StringBuffer sb = new StringBuffer(
                "Select TASK_ID_, GROUP_ID_, TYPE_ from ACT_RU_IDENTITYLINK where TASK_ID_ IN (:taskIds)");

        NativeQueryExecutor<Object[]> nativeQueryExecutor = new NativeQueryExecutor<Object[]>(sb.toString());
        nativeQueryExecutor.addParameter("taskIds", taskIds);

        List<Object[]> results = entityDao.executeQuery(nativeQueryExecutor);

        for (Object[] result : results) {

            String taskId = String.valueOf(result[0]);
            String groupId = String.valueOf(result[1]);
            String type = String.valueOf(result[2]);

            if (type.equals(IdentityLinkType.CANDIDATE)) {

                if (finalResult.get(taskId) == null) {
                    finalResult.put(taskId, new ArrayList<String>());
                }

                finalResult.get(taskId).add(groupId);
            }
        }
        return finalResult;
    }

    @Override
    public Map<String, Map<String, Object>> getTaskByVariable(Collection<String> taskIds, Collection<String> variableNames) {


        StringBuffer stringBuffer = new StringBuffer("");

        int i = 0;
        for (String taskId : taskIds) {
        	i++;

        	if (i == taskIds.size()) {
        		stringBuffer.append("'").append(taskId).append("'");
        	} else {
        		stringBuffer.append("'").append(taskId).append("'").append(",");
        	}
        }

        StringBuffer stringBufferStage = new StringBuffer("");

        i = 0;
        for (String variableName : variableNames) {
        	i++;

        	if (i == variableNames.size()) {
        		stringBufferStage.append("'").append(variableName).append("'");
        	} else {
        		stringBufferStage.append("'").append(variableName).append("'").append(",");
        	}
        }


        String query = "SELECT RES.ID_, VAR.NAME_ as VAR_NAME_, VAR.TEXT_ as VAR_TEXT_ from ACT_RU_TASK RES "
                + "left outer join ACT_RU_VARIABLE VAR ON RES.PROC_INST_ID_ = VAR.EXECUTION_ID_ and VAR.TASK_ID_ is null "
                + "WHERE RES.ID_ IN (" + stringBuffer.toString() + ") AND VAR.NAME_ IN (" + stringBufferStage.toString() + ")";

        NativeQueryExecutor<Object[]> nativeQueryExecutor = new NativeQueryExecutor<Object[]>(query);

        List<Object[]> results = entityDao.executeQuery(nativeQueryExecutor);

        Map<String, Map<String, Object>> variableMap = new HashMap<String, Map<String, Object>>();

        for (Object[] result : results) {

            String taskId = String.valueOf(result[0]);
            String varName = String.valueOf(result[1]);
            Object varValue = result[2];

            if (variableMap.get(taskId) == null) {
                variableMap.put(taskId, new HashMap<String, Object>());
            }

            variableMap.get(taskId).put(varName, varValue);
        }
        return variableMap;
    }

    @Override
    public List<Task> getTaskByTaskId(Collection<String> taskIds) {
        NeutrinoValidator.notEmpty(taskIds, "TaskId couldn't be null or empty");
        StringBuilder sb = new StringBuilder("SELECT * FROM ACT_RU_TASK WHERE ID_ IN (");
        StringBuilder stringBuilder = new StringBuilder("");
        List<Task> tasksList=new ArrayList<Task>();    
        List<List<String>> partitionedTaskIdsList= ListUtils.partition(new ArrayList<String>(taskIds),BATCHSIZE);
        for(List<String> taskIdList:partitionedTaskIdsList){
        	stringBuilder.delete(0, stringBuilder.length());
        	stringBuilder.append(sb);
        	int i = 0;
        	for (String taskId : taskIdList) {
            	i++;

            	if (i == taskIdList.size()) {
            		stringBuilder.append("'").append(taskId).append("'");
            	} else {
            		stringBuilder.append("'").append(taskId).append("'").append(",");
            	}
            }
        	stringBuilder.append(")");
            NativeTaskQuery nativeTaskQuery = processEngine.getTaskService().createNativeTaskQuery().sql(stringBuilder.toString());
            tasksList.addAll(nativeTaskQuery.list());
        }
        
        return tasksList;
    }
    
    @Override
    public List<Task> getTaskListByTaskName(String taskName) {
        NeutrinoValidator.notEmpty(taskName, "Task Name couldn't be null or empty");
        List<Task> task = processEngine.getTaskService().createTaskQuery().taskName(taskName).list();
        return task;
    }

    public static <E> void validateAndAddTaskstoList(List<E> list, Collection<? extends E> c) {
        if (c != null) {
            list.addAll(c);
        }
    }
}