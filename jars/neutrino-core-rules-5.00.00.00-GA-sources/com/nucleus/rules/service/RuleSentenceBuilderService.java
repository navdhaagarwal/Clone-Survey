package com.nucleus.rules.service;

import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * This to build the Rule Sentence
 */
public interface RuleSentenceBuilderService extends BaseService {

    /**
     * 
     * To build the Compound Parameter sentence in simpler form
     * @param parameterExpression
     * @return
     */
    public String buildCompoundSentence(String parameterExpression);

    /**
     * 
     * To build the Compound Parameter sentence in simpler form
     * @param parameterExpression
     * @return
     */
    public String buildConditionSentence(String conditionExpression);

    /**
     * To build the Rule sentence in simpler form
     * @param ruleExpression
     * @return
     */
    public String buildRuleSentence(String ruleExpression);

}
