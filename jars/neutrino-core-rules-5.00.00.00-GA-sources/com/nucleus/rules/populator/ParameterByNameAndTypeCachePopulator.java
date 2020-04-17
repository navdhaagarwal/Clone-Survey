package com.nucleus.rules.populator;

import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.KEY_DELIMITER;
import static com.nucleus.finnone.pro.cache.constants.FWCacheConstants.REGEX_DELIMITER;

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
import com.nucleus.rules.model.Parameter;

@Named("parameterByNameAndTypeCachePopulator")
public class ParameterByNameAndTypeCachePopulator extends FWCachePopulator {

	@Inject
	@Named("parameterDao")
	private ParameterDao parameterdao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : ParameterByNameAndTypeCachePopulator");
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		String[] keyArray = ((String) key).split(REGEX_DELIMITER);
		try {
			Parameter parameterEntity = parameterdao.findApprovedParameterByName(keyArray[1],
					(Class<? extends Parameter>) Class.forName(keyArray[0]));
			if (ValidatorUtils.notNull(parameterEntity)) {
				return parameterEntity.getId();
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger
					.error(" Error occured in fallback of Parameter by Name And Type Cache for Key :: " + key);
			BaseLoggers.flowLogger.error(e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		for (Map.Entry<String, List<? extends Parameter>> entry : parameterdao.getTypeBasedMapOfParameters()
				.entrySet()) {

			String parameterType = entry.getKey();
			Object entryValue = entry.getValue();

			if (ValidatorUtils.notNull(entryValue)) {
				List<Parameter> parameterList = (List<Parameter>) entryValue;
				for (Parameter parameter : parameterList) {
					try {
						put(new StringBuilder(parameterType).append(KEY_DELIMITER).append(parameter.getName())
								.toString(), parameter.getId());
					} catch (Exception e) {
						BaseLoggers.exceptionLogger.error(
								" Error occured in fallback of Parameter by Name And Type Cache for Parameter Id :: "
										+ parameter.getId());
					}
				}
			}

		}
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.UPDATE) && ValidatorUtils.notNull(object)) {
			putAll(object);
		} else if (action.equals(Action.DELETE) && ValidatorUtils.notNull(object)) {
			removeAll(object);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.PARAMETER_BY_TYPE_AND_NAME;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.PARAMETER_CACHE_GROUP;
	}

}
