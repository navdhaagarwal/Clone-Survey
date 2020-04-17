/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.rules.service;

import java.util.Map;

import com.nucleus.rules.model.RuleGroup;
import com.nucleus.service.BaseService;

/**
 * @author Nucleus Software India Pvt Ltd
 */
public interface RuleGroupEvaluationService extends BaseService {

	/**
	 * Evaluates the rule group and also does the rule auditing
	 *
	 * @param ruleGroup
	 *            the rule group
	 * @param contextObjectMap
	 *            the context object map
	 * @param uuid
	 *            the uuid
	 * @param ruleInvocationPoint
	 *            the rule invocation point
	 * @return the object
	 * @deprecated (Since GA2.5, To Support configurable auditing)
    */
   @Deprecated
	public Boolean executeRuleGroup(RuleGroup ruleGroup, Map<Object, Object> contextObjectMap, String uuid,
			String ruleInvocationPoint);

	/**
	 * @param ruleGroup
	 * @param contextObjectMap
	 * @param uuid
	 * @param eventName
	 * @param auditingEnabled
	 * @param purgingRequired
	 * @return
	 */
	Boolean executeRuleGroup(RuleGroup ruleGroup, Map<Object, Object> contextObjectMap, String uuid, String eventName,
			boolean auditingEnabled, boolean purgingRequired);

}
