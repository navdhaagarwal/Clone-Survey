package com.nucleus.core.genericparameter.populator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.genericparameter.dao.GenericParameterDao;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("genericParameterByTypeCachePopulator")
public class GenericParameterByTypeCachePopulator extends FWCachePopulator {

	@Inject
	@Named("genericParameterDao")
	private GenericParameterDao genericParameterDao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : GenericParameterByTypeCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		return genericParameterDao.getDTypeBasedListOfGenericParameterIds((String) key);
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		Map<String, List<Long>> dTypeBasedMapOfGenericParameterIds = genericParameterDao
				.getDTypeBasedMapOfGenericParameterIds();
		for(Map.Entry<String, List<Long>> entry : dTypeBasedMapOfGenericParameterIds.entrySet()) {
			put(entry.getKey(),entry.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Action action, Object object) {
		GenericParameter genericParameter = (GenericParameter) object;
		String dTypeName = genericParameter.getClass().getName();

		if (action.equals(Action.DELETE) && ValidatorUtils.notNull(genericParameter)) {
			List<Long> genericParameterIdList = (List<Long>) get(dTypeName);
			if (ValidatorUtils.hasElements(genericParameterIdList)) {
				genericParameterIdList.remove(genericParameter.getId());
				put(dTypeName, genericParameterIdList);
			}
		} else if ((action.equals(Action.INSERT) || action.equals(Action.UPDATE))
				&& ValidatorUtils.notNull(genericParameter)) {
			List<Long> genericParameterIdList = (List<Long>) get(dTypeName);
			if (ValidatorUtils.isNull(genericParameterIdList)) {
				genericParameterIdList = new ArrayList<>();
			}
			genericParameterIdList.add(genericParameter.getId());
			put(dTypeName, genericParameterIdList);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.GENERIC_PARAMETER_TYPE_ENTITIES;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.GENERIC_PARAMETER_CACHE_GROUP;
	}

}
