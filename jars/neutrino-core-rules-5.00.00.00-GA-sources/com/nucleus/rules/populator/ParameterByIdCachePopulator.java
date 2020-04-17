package com.nucleus.rules.populator;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.dao.ParameterDao;
import com.nucleus.rules.model.Parameter;

@Named("parameterByIdCachePopulator")
public class ParameterByIdCachePopulator extends FWCachePopulator {

	@Inject
	@Named("parameterDao")
	private ParameterDao parameterDao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : ParameterByIdCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		Long id = (Long) key;
		return parameterDao.find(Parameter.class, id);
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		List<Parameter> listOfAllParameters = parameterDao.getAllParametersFromDB(null);
		if (listOfAllParameters == null) {
			return;
		}
		for (Parameter parameter : listOfAllParameters) {
			try {
				put(parameter.getId(), parameter);
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(
						" Error occured in build of Parameter by ID cache for Parameter Id:: " + parameter.getId());

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
		return FWCacheConstants.PARAMETER_CACHE_ID;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.PARAMETER_CACHE_GROUP;
	}

}
