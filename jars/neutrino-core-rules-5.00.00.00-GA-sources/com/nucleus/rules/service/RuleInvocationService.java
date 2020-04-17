package com.nucleus.rules.service;

import java.util.List;
import java.util.Map;

import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.RuleInvocationMapping;
import com.nucleus.rules.model.RuleInvocationPoint;
import com.nucleus.service.BaseService;
import com.nucleus.user.User;

/**
 * Service to invoke the Rules mapped to an Invocation Point.
 *
 * @author Nucleus Software Exports Limited
 */
public interface RuleInvocationService extends BaseService {

    /**
     * It will load the Rules/Rule Sets mapped with the invocation point,
     * Evaluate the Rules/Rule Set and executes the actions on the basis of the Basis of the action mapped to the Rules/Rule Set.
     * It will generate the unique Transaction ID and returned the Rules execution out come and the transaction ID in the map.
     *
     * @param invocationPoint
     * @return
     * @deprecated (Since GA2.5, To Support configurable auditing)
    */
   @Deprecated
    public RuleInvocationResult invokeRule(String invocationPoint, Map<Object, Object> map);
    
    
    
    /**
     * @param invocationPoint
     * @param contextObjectMap
     * @param auditingEnabled
     * @param purgingRequired
     * @return
     */
    RuleInvocationResult invokeRule(String invocationPoint, final Map<Object, Object> contextObjectMap, boolean auditingEnabled, boolean purgingRequired);

    /**
     *
     * Get the rule set
     * @param ruleGroupExpression
     * @return
     */
    public List<Rule> getRuleSet(String ruleGroupExpression);

    /**
     * 
     * 
     * @param invocationPoint
     * @return
     */
    public RuleInvocationMapping getRuleMapping(String invocationPoint);

    /**
     * 
     * 
     * @param ruleInvocation
     * @param user
     */
    public void saveRuleInvocationMapping(RuleInvocationMapping ruleInvocation, User user);

    /**
     * it would load all the invocation point except those who are already edited
     */
    public List<RuleInvocationPoint> getAllUnEditedInvocationPoint();
}
