package com.nucleus.rules.service;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.SystemEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.notificationMaster.service.NotificationMasterService;
import com.nucleus.rules.model.*;
import com.nucleus.service.BaseServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Lazy
@Named("ruleExceptionLoggingServiceImpl")
public class RuleExceptionLoggingServiceImpl extends BaseServiceImpl implements RuleExceptionLoggingService {

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @Inject
    @Named("notificationMasterService")
    protected NotificationMasterService notificationMasterService;

    private static final String COMMA =",";
    private static final String emailSubject_Rule="Exception while Evaluating Rule";
    private static final String emailSubject_RuleAction="Exception while Evaluating Rule Action";
    private static final String emailSubject_Parameter="Exception while Evaluating Parameter";

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRuleErrorLogs(RuleExceptionLoggingVO ruleExceptionLoggingVO) {

        switch(ruleExceptionLoggingVO.getExceptionOwner()) {
            case RuleConstants.RULE_EXCEPTION: {
                RuleExceptionLogging ruleErrorLogging = new RuleExceptionLogging();
                ruleErrorLogging.setTypeId(ruleExceptionLoggingVO.getRule().getId());
                ruleErrorLogging.setTypeCode(ruleExceptionLoggingVO.getRule().getCode());
                ruleErrorLogging.setException(ExceptionUtils.getFullStackTrace(ruleExceptionLoggingVO.getE()));
                ruleErrorLogging.setInvokeTime(DateUtils.getCurrentUTCTime().toString());
                udpateRuleOwner(ruleExceptionLoggingVO.getContextMap(), ruleErrorLogging);
                ruleErrorLogging.setRuleInvocationUUID((String)ruleExceptionLoggingVO.getContextMap().get("Uuid_Audit_Excep"));
                entityDao.saveOrUpdate(ruleErrorLogging);
                sendMailAsynchronously(ruleExceptionLoggingVO,emailSubject_Rule,ruleExceptionLoggingVO.getRule().getCode());
                break;
            }
            case RuleConstants.RULE_ACTION_EXCEPTION: {
                RuleActionExceptionLogging ruleErrorLogging = new RuleActionExceptionLogging();
                ruleErrorLogging.setTypeId(ruleExceptionLoggingVO.getRule().getId());
                ruleErrorLogging.setTypeCode(ruleExceptionLoggingVO.getRule().getCode());
                ruleErrorLogging.setException(ExceptionUtils.getFullStackTrace(ruleExceptionLoggingVO.getE()));
                ruleErrorLogging.setInvokeTime(DateUtils.getCurrentUTCTime().toString());
                udpateRuleOwner(ruleExceptionLoggingVO.getContextMap(), ruleErrorLogging);
                entityDao.saveOrUpdate(ruleErrorLogging);
                sendMailAsynchronously(ruleExceptionLoggingVO,emailSubject_RuleAction,ruleExceptionLoggingVO.getRule().getCode());

                break;
            }
            case RuleConstants.PARAMETER_EXCEPTION: {
                ParameterExceptionLogging ruleErrorLogging = new ParameterExceptionLogging();
                ruleErrorLogging.setTypeId(ruleExceptionLoggingVO.getParameter().getId());
                ruleErrorLogging.setTypeCode(ruleExceptionLoggingVO.getParameter().getCode());
                ruleErrorLogging.setException(ExceptionUtils.getFullStackTrace(ruleExceptionLoggingVO.getE()));
                ruleErrorLogging.setInvokeTime(DateUtils.getCurrentUTCTime().toString());
                udpateRuleOwner(ruleExceptionLoggingVO.getContextMap(), ruleErrorLogging);
                entityDao.saveOrUpdate(ruleErrorLogging);
                sendMailAsynchronously(ruleExceptionLoggingVO,emailSubject_Parameter,ruleExceptionLoggingVO.getParameter().getCode());

                break;
            }
        }

    }
    @Override
    public List<Map<String, Object>> getRuleExceptionForAudit(List<String> uuid) {
        List<Map<String,Object>> mapList=null;
        NamedQueryExecutor<Map<String, Object>> executor = new NamedQueryExecutor<Map<String, Object>>(
                "ruleExceptionUUID.byListUUID").addParameter("inclausevar", uuid);
        mapList=entityDao.executeQuery(executor);
       return mapList;
    }
    @Override
    public List getRuleExceptionTypeForAudit(String uuid) {
        List<Map<String,Object>> mapList=null;
        NamedQueryExecutor executor = new NamedQueryExecutor(
                "ruleExceptionTypeCode.byUUID").addParameter("inclausevar", uuid);
        mapList=entityDao.executeQuery(executor);

            return mapList;
    }
    public Map<String,Object> getRuleExceptionMessageForAuditByUuidCode(String uuid,String uuidCode) {
        List<Map<String,Object>> mapList=null;
        NamedQueryExecutor executor = new NamedQueryExecutor(
                "ruleExceptionUUID.byListUUIDandRuleCode").addParameter("UUID", uuid).addParameter
                ("typeCode",uuidCode);
        mapList=entityDao.executeQuery(executor);
        if (CollectionUtils.isNotEmpty(mapList)) {
            return mapList.get(0);
        }
        return null;


    }
    private void udpateRuleOwner(Map<Object, Object> objectMap, RuleEngineExceptionLogging ruleErrorLogging) {
        ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(
                SystemEntity.getSystemEntityId(), "config.system.ruleaudit.owneruri");
        if (configVo != null) {
            String ownersName = configVo.getPropertyValue();
            if (StringUtils.isNoneEmpty(ownersName)) {
                List<String> owners = new ArrayList<>();
                if(!ownersName.contains(COMMA)){
                    owners.add(ownersName);
                }else{
                    owners = Arrays.asList(ownersName.split(COMMA));
                }
                for (String o : owners) {
                    Object contextObjectOwner = objectMap.get(o);
                    if (contextObjectOwner != null) {
                        ruleErrorLogging.setInvokerUri(contextObjectOwner.toString());
                        break;
                    }
                }
            }
        }
    }

    private void sendMailAsynchronously(RuleExceptionLoggingVO ruleExceptionLoggingVO , String emailSubject,String entityCode){

        List<String> emailIdsList = new ArrayList<>();
        ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(
                SystemEntity.getSystemEntityId(), "rule.exception.emailRecipients.list");
        if(configVo!=null){
            String emailIds = configVo.getPropertyValue();
            if(StringUtils.isNotEmpty(emailIds)){
                if(!emailIds.contains(COMMA)){
                    emailIdsList.add(emailIds);
                }else{
                    emailIdsList = Arrays.asList(emailIds.split(COMMA));
                }
            }
         String uri=null;
         if(ruleExceptionLoggingVO.getContextMap().containsKey("contextObjectLoanApplicationUri")){
             uri=(String)ruleExceptionLoggingVO.getContextMap().get("contextObjectLoanApplicationUri");
         }
            try {
                String ipAddress = InetAddress.getLocalHost().getHostAddress();
                String emailBody = "Code : "+entityCode+COMMA+"\n"+"Loan Application : "+uri+COMMA;
                if(StringUtils.isNotEmpty(ipAddress))
                    emailBody=emailBody + "\n"+"Ip Address : "+ipAddress+COMMA;
                emailBody =  emailBody+"\n"+"Exception : "+ExceptionUtils.getFullStackTrace(ruleExceptionLoggingVO.getE());
                notificationMasterService.constructEmailAndSend(emailIdsList,ruleExceptionLoggingVO.getContextMap(),emailSubject,emailBody,null);
            } catch (IOException | MessagingException e) {
                BaseLoggers.exceptionLogger.error("Exception occured while sending mail for rule Exception Logging : ",e);
            }
        }
    }

}
