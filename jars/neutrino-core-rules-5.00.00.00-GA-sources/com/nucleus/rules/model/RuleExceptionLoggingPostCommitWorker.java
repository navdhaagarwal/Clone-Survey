package com.nucleus.rules.model;

import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.rules.service.RuleExceptionLoggingService;
import com.nucleus.rules.service.RuleService;
import org.apache.commons.collections.MapUtils;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;

@Named("ruleExceptionLoggingPostCommitWorker")
public class RuleExceptionLoggingPostCommitWorker implements TransactionPostCommitWork {

    @Inject
    @Named("ruleService")
    RuleService ruleService;

    @Lazy
    @Inject
    @Named("ruleExceptionLoggingServiceImpl")
    private RuleExceptionLoggingService ruleExceptionLoggingService;

    @Override
    public void work(Object argument) {
        if(argument instanceof RuleExceptionLoggingVO){
            RuleExceptionLoggingVO ruleExceptionLoggingVO = (RuleExceptionLoggingVO) argument;
            if(ruleExceptionLoggingVO.getRule()!=null && MapUtils.isNotEmpty(ruleExceptionLoggingVO.getContextMap()) && ruleExceptionLoggingVO.getE()!=null){
                ruleExceptionLoggingService.saveRuleErrorLogs(ruleExceptionLoggingVO);
            }
        }
    }
}
