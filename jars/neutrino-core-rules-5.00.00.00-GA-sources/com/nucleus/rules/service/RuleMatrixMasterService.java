package com.nucleus.rules.service;


import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixRowData;
import com.nucleus.rules.model.assignmentMatrix.AssignmentSet;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;
import com.nucleus.rules.model.ruleMatrixMaster.pojo.AssignmentSetVO;
import com.nucleus.rules.model.ruleMatrixMaster.pojo.RuleMatrixRowData;
import com.nucleus.rules.model.ruleMatrixMaster.pojo.RuleMatrixTableData;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import java.util.*;

public interface RuleMatrixMasterService {

    AssignmentSet convertToJsonMap(AssignmentSet assignmentSet);
    void processAssignmentSet(AssignmentSet assignmentSet);
    List<Map<String, Object>> getParametersBasedOnOperators(String operand,String operator,Long moduleId,String sourceProduct);
    List<Map<String,?>> searchAutoCompleteValues(String className, String itemVal,
                                                 String[] searchColumnList, String value,
                                                 Boolean loadApprovedEntityFlag, String itemsList,
                                                 Boolean strictSearchOnitemsList, int page, String whereCondition,
                                                 Map<String, Object>paramMap);

    String prepareAutoComplete(ModelMap map, String i_label, String idCurr,
                               String content_id, int page, List<Map<String, ?>> list);
    String getDataTypeForObjectGraphType(Long id);

    void populateMatrix(RuleMatrixTableData ruleMatrixTableData, RuleMatrixMaster ruleMatrixMaster, ModelMap map,Boolean editViewModeOfAssignment);

    String loadAssignmentGridHeaderJson(RuleMatrixTableData ruleMatrixTableData);

    String populateRowData(RuleMatrixTableData ruleMatrixTableData, String mode, Integer index, ModelMap map);

    List<Parameter> getParametersBasedOnDataTypeModule(Long moduleId, String sourceProduct, Integer dataType);

    String loadAssignmentGridBodyJson(RuleMatrixTableData ruleMatrixTableData);

    void addRowData(RuleMatrixTableData ruleMatrixTableData, RuleMatrixRowData ruleMatrixRowData, ModelMap map);

    String loadAssignmentGridRowJson(RuleMatrixRowData ruleMatrixRowData);

    String saveRuleMatrixMaster(RuleMatrixMaster ruleMatrixMaster, BindingResult result,
                                ModelMap map, boolean createAnotherMaster, List<RuleMatrixTableData> ruleMatrixTableData, boolean isSaveOnly, User user);
    String newRuleMatrixMaster(RuleMatrixMaster ruleMatrixMaster, List<RuleMatrixTableData> ruleMatrixTableData, ModelMap map);

    void addAssignmentSetToSession(RuleMatrixTableData ruleMatrixTableData,ModelMap map,String assignmentSetName,Integer assignmentPriority,Long assignmentSetruleId,Boolean executeAll,Boolean defaultSet, Date effectiveFrom, Date effectiveTill, Integer bufferDays);

    String deleteRowData(RuleMatrixTableData ruleMatrixTableData, int index);

    String openRuleMatrixMaster(Long id, ModelMap map, UserInfo currentUser, boolean isViewOnly);
}
