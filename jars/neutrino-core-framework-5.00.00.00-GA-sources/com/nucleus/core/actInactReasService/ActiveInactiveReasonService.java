package com.nucleus.core.actInactReasService;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;

import java.util.List;

public interface ActiveInactiveReasonService {

    public  Boolean checkForActiveInactiveForApprovedModified(ReasonsActiveInactiveMapping reasonsActiveInactiveMapping, String masterName, String uniqueParameter, String uniqueValue);
    public List<MasterActiveInactiveReasons> getMasterReasonList(List<MasterActiveInactiveReasons> msterReasListFromUpdate, List<MasterActiveInactiveReasons> msterReasListToUpdate, List<ValidationRuleResult> dataValidationRuleResults);
    public Boolean checkForActionofReasons(ReasonsActiveInactiveMapping reasonsActiveInactiveMapping);
    public Boolean checkForGenericReasons(ReasonsActiveInactiveMapping reasonsActiveInactiveMapping);
    public Boolean checkForDuplicateReasons(ReasonsActiveInactiveMapping reasonsActiveInactiveMapping);
}