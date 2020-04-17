package com.nucleus.rules.populator;

import com.nucleus.finnone.pro.cache.common.*;
import com.nucleus.finnone.pro.cache.constants.*;
import com.nucleus.logging.*;
import com.nucleus.rules.service.*;
import org.apache.commons.collections.*;


import javax.inject.*;
import java.util.*;

@Named("ruleDistinctConditionCache")
public class RuleDistinctConditionCachePopulator extends FWCachePopulator {

    @Inject
    @Named("ruleExpressionCNFComparisonService")
    private RuleExpressionCNFComparisonService ruleExpressionCNFComparisonService;

    @Override
    public String getNeutrinoCacheName() {
        return FWCacheConstants.RULE_DISTINCT_CONDITION_CACHE;
    }

    @Override
    public void init() {
        BaseLoggers.flowLogger.debug("Init Called : RuleDistinctConditionCachePopulator");
    }

    @Override
    public Object fallback(Object key) {
        return null;
    }

    @Override
    public void build(Long tenantId) {
        ruleExpressionCNFComparisonService.createCnfForAllRules();
    }

    @Override
    public void update(Action action, Object object) {
        if (action.equals(Action.INSERT) || action.equals(Action.UPDATE)) {
            List<Object> list = (List) object;
            if(CollectionUtils.isNotEmpty(list))
                put(list.get(0), list.get(1));

        } else if (action.equals(Action.DELETE)) {
            List<Object> list = (List) object;
            if (CollectionUtils.isNotEmpty(list)){
                Set<Long> value = (Set<Long>) get(list.get(0));
                if (value.contains(list.get(1))) {
                    value.remove(list.get(1));
                }
                put(list.get(0), value);
            }
        }
    }

    @Override
    public String getCacheGroupName() {
        return FWCacheConstants.RULE_EXPRESSION_GROUP_CACHE;
    }
}
