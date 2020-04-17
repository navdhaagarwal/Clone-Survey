package com.nucleus.core.rules.rulesMaster;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;


@Service
@Named("ruleUploadService")
public class RuleUploadService implements IRuleUploadService{

    @Inject
    private IRuleUploadBusinessObj ruleUploadBusinessObj;

    @Override
    @Transactional
    public RuleVO uploadRule(RuleVO ruleVO) {
        return ruleUploadBusinessObj.uploadRule(ruleVO);
    }
}
