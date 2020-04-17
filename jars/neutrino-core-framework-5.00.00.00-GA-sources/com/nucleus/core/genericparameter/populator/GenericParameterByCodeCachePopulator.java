package com.nucleus.core.genericparameter.populator;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.genericparameter.dao.GenericParameterDao;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("genericParameterByCodeCachePopulator")
public class GenericParameterByCodeCachePopulator extends FWCachePopulator {

	@Inject
	@Named("genericParameterDao")
	private GenericParameterDao genericParameterDao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : GenericParameterByCodeCachePopulator");
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		String[] keyArray = ((String) key).split(FWCacheConstants.REGEX_DELIMITER, 2);
		try {
			GenericParameter genericParameterEntity = genericParameterDao.findByCode(keyArray[1],
					(Class<? extends GenericParameter>) Class.forName(keyArray[0]));
			if (ValidatorUtils.notNull(genericParameterEntity)) {
				return genericParameterEntity.getId();
			}
		} catch (ClassNotFoundException e) {
			BaseLoggers.flowLogger.error(e.getMessage());
			throw new SystemException(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		for (Map.Entry<String, List<? extends GenericParameter>> entry : genericParameterDao
				.getDTypeBasedMapOfGenericParameter().entrySet()) {
			String dTypeName = entry.getKey();
			Object genericParameterObjList = entry.getValue();

			if (ValidatorUtils.notNull(genericParameterObjList)) {
				List<GenericParameter> genericParameterList = (List<GenericParameter>) genericParameterObjList;
				for (GenericParameter genericParameter : genericParameterList) {
					put(new StringBuilder(dTypeName).append(FWCacheConstants.KEY_DELIMITER)
							.append(genericParameter.getCode()).toString(), genericParameter.getId());
				}
			}
		}
	}

	@Override
	public void update(Action action, Object object) {
		GenericParameter genericParameter = (GenericParameter) object;
		String dTypeName = genericParameter.getClass().getName();

		if (action.equals(Action.DELETE) && ValidatorUtils.notNull(genericParameter)
				&& ValidatorUtils.notNull(genericParameter.getCode())) {
			remove(new StringBuilder(dTypeName).append(FWCacheConstants.KEY_DELIMITER)
					.append(genericParameter.getCode()).toString());
		} else if ((action.equals(Action.INSERT) || action.equals(Action.UPDATE))
				&& ValidatorUtils.notNull(genericParameter) && ValidatorUtils.notNull(genericParameter.getCode())) {
			put(new StringBuilder(dTypeName).append(FWCacheConstants.KEY_DELIMITER).append(genericParameter.getCode())
					.toString(), genericParameter.getId());
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.GENERIC_PARAMETER_CODE_ENTITY;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.GENERIC_PARAMETER_CACHE_GROUP;
	}

}
