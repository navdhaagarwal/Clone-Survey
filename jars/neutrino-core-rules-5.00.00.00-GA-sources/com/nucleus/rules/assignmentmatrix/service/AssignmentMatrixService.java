package com.nucleus.rules.assignmentmatrix.service;

import java.util.List;
import java.util.Map;

import com.nucleus.rules.model.EntityType;
import com.nucleus.rules.model.ObjectGraphTypes;
import com.nucleus.rules.model.assignmentMatrix.*;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;
import com.nucleus.service.BaseService;
import org.springframework.ui.ModelMap;

/**
 *
 * @author Nucleus Software Exports Limited
 * Service class to handle Assignment Master related functionality
 */

public interface AssignmentMatrixService extends BaseService {

    /**
     *
     * Method to create Rule
     * @param assignmentMaster
     * @return
     */

    public void populateAssignmentMasterFields(BaseAssignmentMaster baseAssignmentMaster);

    /**
     *
     * Method to get the binder name with its itemLabel and itemValue to display on UI
     * @param ognl
     * @return
     */

    public FormConfigEntityDataVO getBinderNameForReferenceOgnl(String ognl);

    /**
     *
     * Load Assignment Master by name
     * @param name
     * @return
     */

    public AssignmentMaster getAssignmentMatrixByName(String name);

    public List<Map<String, ?>> getAssignmentMatrixByPurpose(String[] searchColumnList, String value, int page);

    /**
     *
     * convert Infix to RPN Expression
     * @param inputTokens
     * @return
     */
    public Object[] convertInfixToRPN(String[] inputTokens);

    /**
     * Gets the entity types for task assignment.
     *
     * @param displayNameList the display name list
     * @return the entity types for task assignment
     */
    public List<EntityType> getEntityTypesForTaskAssignment(List<String> displayEntityNameList);

    /**
     *
     * Load All Assignment Matrix Actions that need to be compiled
     * @return
     */
    public List<AssignmentMatrixAction> getAssignmentActionsToCompile();

    /**
     *
     * Load Assignment Set by Assignment Matrix Action id
     * @param id
     * @return
     */
    public AssignmentSet getAssignmentSetByAction(Long id);

    /**
     *
     * Compile and create the execution script for Assignmatrx action
     * @param assignmentMatrixAction
     * @param assignmentActionFieldMetaDataList
     */
    public void compileAndSaveScript(AssignmentMatrixAction assignmentMatrixAction,
                                     List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList, String aggregateFunction);

    /**
     *
     * Compile and create the execution script for Rule Matrix action
     * @param assignmentMatrixAction
     * @param assignmentActionFieldMetaDataList
     */
    public void compileAndSaveScriptForRuleMatrix(AssignmentMatrixAction assignmentMatrixAction,
                                                  List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList);

    /**
     *
     * Load Assignment Master by id
     * @param id
     * @return
     */

    public AssignmentMaster getAssignmentMatrixById(Long id);

    /**
     *
     * Method to set the Assignment Expression
     * @param assignmentExpression
     */
    public void populateAssignmentExpProperties(AssignmentExpression assignmentExpression);

    /**
     *
     * Populate Assignment Grid object with rules
     * @param assignmentGrid
     */
    public void populateAssignmentGridProperties(AssignmentGrid assignmentGrid);

    public List<RuleMatrixMaster> getRateLimitMasters();

    public List<RuleMatrixMaster> getChargeLimitMasters();

    List<Map<String,?>> searchAutoCompleteValues(String className, String itemVal,
                                                 String[] searchColumnList, String value,
                                                 Boolean loadApprovedEntityFlag, String itemsList,
                                                 Boolean strictSearchOnitemsList, int page, String whereCondition,
                                                 Map<String, Object>paramMap);

    String prepareAutoComplete(ModelMap map, String i_label, String idCurr,
                               String content_id, int page, List<Map<String, ?>> list);
    String getDataTypeForObjectGraphType(Long id);

    String loadAssignmentGridHeaderJson(AssignmentGrid assignmentGrid);

    String populateRowData(String mode, Integer index, ModelMap map,AssignmentSet assignmentSetVO,String sourceProduct,Long moduleId);


    String loadAssignmentGridBodyJson(AssignmentGrid assignmentGrid);


    String loadAssignmentGridRowJson(AssignmentGrid assignmentGrid,int rowIndex);

    void populateAssignmentSetValues(AssignmentSet assignmentSet1,AssignmentSet assignmentSetVO);

    AssignmentSet CopyAssignmentSetValues(AssignmentSet assignmentSetVO );

    void prepareParametersForMultiSelect(AssignmentGrid assignmentGrid,Long moduleId, String sourceProduct);

    void generateOperatorsForOgnlMetaField(AssignmentSet assignmentSet);

    void deleteRowDataFromSet(AssignmentSet assignmentSetVO,long[] deleteArrayList);

    void setAssignmentSetPropertiesForGrid(ModelMap map, int assignmentGridIndex,
                                                  List<ObjectGraphTypes> objectGraphTypesActionFieldList, List<ObjectGraphTypes> objectGraphTypesParameterActionFieldList,
                                                  AssignmentSet assignmentSet );

    String loadAssignmentGridHeaderJsonTask(AssignmentGrid assignmentSetVO);

    String populateRowDataTask(String mode, Integer index, ModelMap map, AssignmentSet assignmentSetVO, Long moduleId);

}
