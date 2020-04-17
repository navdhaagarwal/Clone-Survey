package com.nucleus.rules.populator;

import com.nucleus.finnone.pro.cache.common.*;
import com.nucleus.finnone.pro.cache.constants.*;
import com.nucleus.logging.*;
import com.nucleus.rules.service.*;
import org.apache.commons.collections.*;
import org.springframework.transaction.annotation.*;

import javax.inject.*;
import java.util.*;

@Named("ruleExpressionCNFMetaDataCache")
public class RuleExpressionCNFMetaDataCachePopulator extends FWCachePopulator {

    @Inject
    @Named("ruleExpressionCNFComparisonService")
    private RuleExpressionCNFComparisonService ruleExpressionCNFComparisonService;

    @Override
    public String getNeutrinoCacheName() {
        return FWCacheConstants.RULE_EXPRESSION_CNF_META_DATA_CACHE;
    }

    @Override
    public void init() {
        BaseLoggers.flowLogger.debug("Init Called : RuleExpressionCNFMetaDataCachePopulator");
    }

    @Override
    public Object fallback(Object key) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public void build(Long tenantId) {
        ruleExpressionCNFComparisonService.createCnfForAllRules();
    }

    @Override
    public void update(Action action, Object object) {
        if(action.equals(Action.INSERT) || action.equals(Action.UPDATE) ){
            List<Object> list = (List)object;
            if(CollectionUtils.isNotEmpty(list))
                put(list.get(0),list.get(1));
        }else if(action.equals(Action.DELETE)){
            remove(object);
        }
    }

    @Override
    public String getCacheGroupName() {
        return FWCacheConstants.RULE_EXPRESSION_GROUP_CACHE;
    }

}
