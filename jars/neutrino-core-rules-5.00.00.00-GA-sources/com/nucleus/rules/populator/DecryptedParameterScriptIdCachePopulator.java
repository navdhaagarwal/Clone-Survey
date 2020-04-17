package com.nucleus.rules.populator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.dao.ParameterDao;
import com.nucleus.rules.model.ScriptParameter;
import com.nucleus.rules.service.ParameterService;

@Named("decryptedParameterScriptIdCachePopulator")
public class DecryptedParameterScriptIdCachePopulator extends FWCachePopulator {

	@Inject
	@Named("parameterService")
	private ParameterService parameterService;

	@Inject
	@Named("parameterDao")
	private ParameterDao parameterDao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : DecryptedParameterScriptIdCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		Long id = (Long) key;
		ScriptParameter scriptParam = parameterDao.find(ScriptParameter.class, id);
		return parameterService.getDecryptedParamScriptIdMap(Arrays.asList(scriptParam)).get(id);
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		List<ScriptParameter> paramList = parameterDao.getAllParametersFromDB(ScriptParameter.class);
		if (paramList == null) {
			return;
		}
		Map<Long, String> decryptedParamScriptIdMap = parameterService.getDecryptedParamScriptIdMap(paramList);
		if (!decryptedParamScriptIdMap.isEmpty()) {
			for(Map.Entry<Long, String> entry : decryptedParamScriptIdMap.entrySet()) {
				put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.INSERT) && ValidatorUtils.notNull(object)) {
			putAll(object);
		} else if (action.equals(Action.DELETE) && ValidatorUtils.notNull(object)) {
			remove(object);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.DECRYPTED_PARAM_SCRIPT_ID;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.PARAMETER_CACHE_GROUP;
	}

}
