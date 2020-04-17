package com.nucleus.rules.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.ScriptRule;

@Named("scriptRuleEvaluatorCachePopulator")
public class ScriptRuleEvaluatorCachePopulator extends FWCachePopulator {

	@Inject
	@Named("ruleCacheService")
	private RuleCacheService ruleCacheService;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	private Map<Long, Long> localLastUpdatedTimeStampMap = new ConcurrentHashMap<>();
	
	private Map<Long, ScriptRuleEvaluator> localScriptRuleEvaluatorMap = new ConcurrentHashMap<>();
	
	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : ScriptRuleEvaluatorCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		ScriptRule scriptRule = entityDao.find(ScriptRule.class, (Long) key);
		return getLastUpdateTimeStampFromRule(scriptRule);
	}

	/**
	 * Earlier we were using below code to build cache on start up.
	 * Now we are doing it lazily. so no need of doing it.
	 *  
	 * List<ScriptRule> scriptRules = ruleCacheService.getAllScriptRulesFromDB();
	 * 
	 */
	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		//Lazy building of evaluator cache.
	}
	
	private Long getLastUpdateTimeStampFromRule(ScriptRule scriptRule) {
		DateTime lastUpdatedTimeStamp = scriptRule.getEntityLifeCycleData().getLastUpdatedTimeStamp();
		if (lastUpdatedTimeStamp != null) {
			return lastUpdatedTimeStamp.getMillis();
		}
		return new DateTime().getMillis();
	}
	
	private void updateRuleEvaluatorMaps(Long lastUpdateTimeStamp, ScriptRule scriptRule, boolean isDistributedCachePutRequired) {
		ScriptRuleEvaluator evaluator = createScriptRuleEvaluator(scriptRule);
		if (evaluator != null) {
			localLastUpdatedTimeStampMap.put(scriptRule.getId(), lastUpdateTimeStamp);
			localScriptRuleEvaluatorMap.put(scriptRule.getId(), evaluator);
			if (isDistributedCachePutRequired) {
				put(scriptRule.getId(), lastUpdateTimeStamp); //To put time stamp in distributed cache.
			}
		}
	}
	
	private void update(Action action, ScriptRule scriptRule, Long lastUpdateTimeStamp, boolean isDistributedCachePutRequired) {
		if (!action.equals(Action.DELETE) && ValidatorUtils.notNull(scriptRule)) {
			if(isDistributedCachePutRequired) {
				lastUpdateTimeStamp = getLastUpdateTimeStampFromRule(scriptRule);	
			}
			updateRuleEvaluatorMaps(lastUpdateTimeStamp, scriptRule, isDistributedCachePutRequired);
		}
	}

	@Override
	public void update(Action action, Object object) {
		if (ValidatorUtils.notNull(object)) {
			update(action, (ScriptRule)object, null, true);
		}
		BaseLoggers.flowLogger.debug("Update Called : ScriptRuleEvaluatorCachePopulator.");
	}
	
	private ScriptRuleEvaluator createScriptRuleEvaluator(ScriptRule scriptRule) {
		if (scriptRule != null && scriptRule.getId() != null) {
			try {
				if (scriptRule.getScriptCode() != null && scriptRule.getScriptCodeType() == 1) {
					scriptRule.setScriptCodeValue(ruleCacheService.decryptString(scriptRule.getScriptCode()));
					return ruleCacheService.generateScriptRuleEvaluator(scriptRule);
				}
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(
						" Error in updating ScriptRuleEvaluatorMapForCache. Cache Build should continue. Script Rule Name::"
								+ scriptRule.getName() + " script rule id " + scriptRule.getId() + " ::  " + e, e);
			}
		}
		return null;
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.SCRIPTRULE_EVALUATOR_BY_SCRIPTRULE_ID;
	}
	
	@Override
	public Object get(Object key) {
		Long id = (Long) key;
		Long distributedTimeStamp = (Long) super.get(id);
		Long localTimeStamp = localLastUpdatedTimeStampMap.get(id);
		if (localTimeStamp == null || !distributedTimeStamp.equals(localTimeStamp)) {
			update(Action.INSERT, entityDao.find(ScriptRule.class, (Long) key), distributedTimeStamp, false);
		}
		return localScriptRuleEvaluatorMap.get(key);
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.MISCELLANEOUS_CACHE_GROUP;
	}

}
