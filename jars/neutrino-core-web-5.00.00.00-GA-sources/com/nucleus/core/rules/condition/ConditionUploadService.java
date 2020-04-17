package com.nucleus.core.rules.condition;

import com.nucleus.rules.model.Condition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 
 */
@Service
@Named("conditionUploadService")
public class ConditionUploadService implements IConditionUploadService {
    @Inject
    private IConditionUploadBusinessObj conditionUploadBusinessObj;

    @Override
    @Transactional
    public Condition uploadCondition(Condition condition) {
        return conditionUploadBusinessObj.uploadCondition(condition);
    }
}
