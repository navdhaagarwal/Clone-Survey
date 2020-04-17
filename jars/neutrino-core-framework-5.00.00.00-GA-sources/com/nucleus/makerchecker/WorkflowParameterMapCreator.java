/**
 * @FileName: WorkflowParameterMapCreator.java
 * @Author: amit.parashar
 * @Copyright: Nucleus Software Exports Ltd
 * @Description:
 * @Program-specification-Referred:
 * @Revision:
 *            --------------------------------------------------------------------------------------------------------------
 *            --
 * @Version | @Last Revision Date | @Name | @Function/Module affected | @Modifications Done
 *          ----------------------------------------------------------------------------------------------------------------
 *          | Jun 21, 2012 | amit.parashar | |
 */

package com.nucleus.makerchecker;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nucleus.approval.ApprovalFlow;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.mail.MailService;
import com.nucleus.mail.SimpleMailMessageBuilder;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;

/**
 * @author amit.parashar
 *
 */
public class WorkflowParameterMapCreator {
    
    public static Map<String, Object> createWorkflowMap(ApprovalFlow processEntity, Long reviewerId, MailService mailService) {

        Map<String, Object> variables = new HashMap<String, Object>();
        Date dateVariable = new Date();
        SimpleMailMessageBuilder checkerNotificationMailBuilderParams = mailService.createSimpleMailBuilder();
        checkerNotificationMailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("New task has arrived").setPlainTextBody("Hi, you need to approve the new entity created");
        variables.put("checkerNotificationMailBuilder", checkerNotificationMailBuilderParams);
        
        SimpleMailMessageBuilder approvalMailBuilderParams = mailService.createSimpleMailBuilder();
        approvalMailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("Request Approved").setPlainTextBody("Hi, your request is approved");
        
        SimpleMailMessageBuilder rejectionmailBuilderParams = mailService.createSimpleMailBuilder();
        rejectionmailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("Your request is rejected").setPlainTextBody("Hi, your request is rejected");
        

        // these mail parameters are being used for sending the notification to
        // restart the process since information provided is not complete
        
        SimpleMailMessageBuilder makerNotificationMailBuilderParams = mailService.createSimpleMailBuilder();
        makerNotificationMailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("Information is not complete").setPlainTextBody("Hi, your request is rejected");
        
        variables.put("approvalMailBuilder", approvalMailBuilderParams);
        variables.put("dateVariable", dateVariable);
        variables.put("processEntity", processEntity.getUri());
        variables.put("rejectionMailBuilder", rejectionmailBuilderParams);
        variables.put("makerNotificationMailBuilder", makerNotificationMailBuilderParams);
        variables.put("actions", "Send Back,Approved,Rejected");
        variables.put("reviewerId", reviewerId);
        populateExecutionContextWithVariables(variables);
        return variables;

    }

    private static void populateExecutionContextWithVariables(Map<String, Object> variables){
        Map<String,String> approvalFlowActionsMap = (Map<String,String>)getNeutrinoExecutionContextHolder().getFromGlobalContext(MakerCheckerServiceImpl.WORKFLOW_ACTION_MAP);
        if(approvalFlowActionsMap==null) {
            approvalFlowActionsMap = new ConcurrentHashMap<String,String>();
            getNeutrinoExecutionContextHolder().addToGlobalContext(MakerCheckerServiceImpl.WORKFLOW_ACTION_MAP,approvalFlowActionsMap);
        }
        approvalFlowActionsMap.put(variables.get("processEntity").toString(),variables.get("actions").toString());
    }

    private static INeutrinoExecutionContextHolder getNeutrinoExecutionContextHolder(){
        return NeutrinoSpringAppContextUtil.getBeanByName("neutrinoExecutionContextHolder",INeutrinoExecutionContextHolder.class);
    }

    public static Map<String, Object> createWorkflowMap(String actionTaken, MailService mailService) {
        Map<String, Object> variables = new HashMap<String, Object>();
       
        
        SimpleMailMessageBuilder approvalMailBuilderParams = mailService.createSimpleMailBuilder();
        approvalMailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("Request Approved").setPlainTextBody("Hi, your request is approved");
        
        
        SimpleMailMessageBuilder rejectionmailBuilderParams = mailService.createSimpleMailBuilder();
        rejectionmailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("Your request is rejected").setPlainTextBody("Hi, your request is rejected");
        

        // these mail parameters are being used for sending the notification to
        // restart the process since information provided is not complete
        
        SimpleMailMessageBuilder makerNotificationMailBuilderParams = mailService.createSimpleMailBuilder();
        makerNotificationMailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("Information is not complete").setPlainTextBody("Hi, your request is rejected");
        
        variables.put("approvalMailBuilder", approvalMailBuilderParams);
        variables.put("rejectionMailParameters", rejectionmailBuilderParams);
        variables.put("makerNotificationMailBuilder", makerNotificationMailBuilderParams);
        variables.put("checkerDecision", actionTaken);
        return variables;

    }
    
    public static Map<String, Object> createWorkflowMap(String actionTaken, Long reviewerId, MailService mailService) {
        Map<String, Object> variables = new HashMap<String, Object>();
       
        SimpleMailMessageBuilder approvalMailBuilderParams = mailService.createSimpleMailBuilder();
        approvalMailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("Request Approved").setPlainTextBody("Hi, your request is approved");
        
        
        SimpleMailMessageBuilder rejectionmailBuilderParams = mailService.createSimpleMailBuilder();
        rejectionmailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("Your request is rejected").setPlainTextBody("Hi, your request is rejected");
        

        // these mail parameters are being used for sending the notification to
        // restart the process since information provided is not complete
        
        SimpleMailMessageBuilder makerNotificationMailBuilderParams = mailService.createSimpleMailBuilder();
        makerNotificationMailBuilderParams.setFrom("ci-pdg@nucleussoftware.com").setTo("tushar.singh@nucleussoftware.com").setCc("tushar.singh@nucleussoftware.com").setSubject("Information is not complete").setPlainTextBody("Hi, your request is rejected");
        
        variables.put("approvalMailBuilder", approvalMailBuilderParams);
        variables.put("rejectionMailBuilder", rejectionmailBuilderParams);
        variables.put("makerNotificationMailBuilder", makerNotificationMailBuilderParams);
        variables.put("checkerDecision", actionTaken);
        variables.put("reviewerId", reviewerId);
        return variables;

    }
    

}
