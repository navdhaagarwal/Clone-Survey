package com.nucleus.rules.populator;

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
import com.nucleus.rules.dao.ParameterDao;
import com.nucleus.rules.model.ScriptParameter;
import com.nucleus.rules.service.ParameterService;
import com.nucleus.rules.service.ScriptParameterEvaluator;

@Named("scriptParameterEvalByParamIdCachePopulator")
public class ScriptParameterEvalByParamIdCachePopulator extends FWCachePopulator {
	
	@Inject
	@Named("parameterService")
	private ParameterService parameterService;

	@Inject
	@Named("parameterDao")
	private ParameterDao parameterDao;
	
	private Map<Long, Long> localLastUpdatedTimeStampMap = new ConcurrentHashMap<>();
	
	private Map<Long, ScriptParameterEvaluator> localScriptParameterEvaluatorMap = new ConcurrentHashMap<>();
	

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : ScriptParameterEvalByParamIdCachePopulator");
	}


	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		ScriptParameter scriptParameter = parameterDao.find(ScriptParameter.class, (Long) key);
		return getLastUpdateTimeStampFromParamter(scriptParameter);
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		//No need to build. Values will be cached lazily.
	}
	
	private Long getLastUpdateTimeStampFromParamter(ScriptParameter scriptParameter) {
		DateTime lastUpdatedTimeStamp = scriptParameter.getEntityLifeCycleData().getLastUpdatedTimeStamp();
		if (lastUpdatedTimeStamp != null) {
			return lastUpdatedTimeStamp.getMillis();
		}
		return new DateTime().getMillis();
	}
	
	private ScriptParameterEvaluator createScriptEvaluator(ScriptParameter scriptParameter) {
		try {
			if (scriptParameter != null && scriptParameter.getScriptCode() != null) {
				scriptParameter.setScriptCodeValue(parameterService.decryptString(scriptParameter.getScriptCode()));
				if (scriptParameter.getScriptCodeValue() != null
						&& !scriptParameter.getScriptCodeValue().isEmpty()) {
					return parameterService.generateScriptParameterEvaluator(scriptParameter);
				}
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.debug("Error occured in scriptParameter Map Build ::" + scriptParameter.getName()
							+ " for script param id " + scriptParameter.getId() + " ::  " + e.getMessage());
		}
		return null;
	}
	
	private void updateParameterEvaluatorMaps(Long lastUpdateTimeStamp, ScriptParameter scriptParameter, boolean isDistributedCachePutRequired) {
		ScriptParameterEvaluator evaluator = createScriptEvaluator(scriptParameter);
		if (evaluator != null) {
			localLastUpdatedTimeStampMap.put(scriptParameter.getId(), lastUpdateTimeStamp);
			localScriptParameterEvaluatorMap.put(scriptParameter.getId(), evaluator);
			if (isDistributedCachePutRequired) {
				put(scriptParameter.getId(), lastUpdateTimeStamp);			//To put time stamp in distributed cache.
			}
		}
	}
	
	private void update(Action action, ScriptParameter scriptParameter, Long lastUpdateTimeStamp, boolean isDistributedCachePutRequired) {
		if (!action.equals(Action.DELETE) && ValidatorUtils.notNull(scriptParameter)) {
			if(isDistributedCachePutRequired) {
				lastUpdateTimeStamp = getLastUpdateTimeStampFromParamter(scriptParameter);	
			}
			updateParameterEvaluatorMaps(lastUpdateTimeStamp, scriptParameter, isDistributedCachePutRequired);
		}
	}
	
	@Override
	public void update(Action action, Object object) {
		if (ValidatorUtils.notNull(object)) {
			update(action, (ScriptParameter) object, null, true);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.SCRIPTPARAMETER_EVALUATOR_BY_PARAM_ID;
	}

	
	@Override
	public Object get(Object key) {
		Long id = (Long) key;
		Long distributedTimeStamp = (Long) super.get(id);
		Long localTimeStamp = localLastUpdatedTimeStampMap.get(id);
		if (localTimeStamp == null || !distributedTimeStamp.equals(localTimeStamp)) {
			update(Action.INSERT, parameterDao.find(ScriptParameter.class, (Long) key), distributedTimeStamp, false);
		}
		return localScriptParameterEvaluatorMap.get(key);
	}

	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.MISCELLANEOUS_CACHE_GROUP;
	}

}
