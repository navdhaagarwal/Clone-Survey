package com.nucleus.rules.service;

import java.util.List;

import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited This class is to validate the given
 *         expression Compound parameter expression Condition Expression Rule
 *         Expression
 */

public interface ExpressionValidationService extends BaseService {

    /**
     * 
     * Function to validate Compound Parameter Expression
     * 
     * @param expression
     * @return
     */
    public List<ValidationError> validateCompoundParameterExpression(String expression, int dataType);

    /**
     * 
     * Function to validate Condition Expression
     * 
     * @param expression
     * @return
     */
    public List<ValidationError> validateConditionExpression(String expression);

    /**
     * 
     * Method to validate the Rule expression
     * 
     * @param expression
     * @return
     */
    public List<ValidationError> validateRule(String expression);

    /**
     * 
     * Validate Rule Group
     * 
     * @param expression
     * @return
     */
    public List<ValidationError> validateRuleGroup(String expression);

    /**
     * 
     * @param expression
     * @return
     */
    public List<ValidationError> validateMvelScriptParameter(String expression);

    
}
