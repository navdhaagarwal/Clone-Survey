package com.nucleus.rules.service;
import javax.inject.Inject;
import javax.inject.Named;
import com.nucleus.rules.model.RulesAuditLog;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW)
@Named(value = "ruleAUditLogEventListener")
public class RuleAUditLogEventListener implements ApplicationListener<RuleAUditLogEventClass> {

    @Inject
    @Named("rulesAuditLogService")
    private RulesAuditLogService rulesAuditLogService;

    @Override

    public void onApplicationEvent(RuleAUditLogEventClass e) {
        RuleAUditLogEventClassWorker worker = (RuleAUditLogEventClassWorker) e.getEventWorker();
        List<RulesAuditLog> ruleAuditLogList=worker.getRuleAuditLogList();
        rulesAuditLogService.saveRuleAuditLogData(ruleAuditLogList);
    }

}
