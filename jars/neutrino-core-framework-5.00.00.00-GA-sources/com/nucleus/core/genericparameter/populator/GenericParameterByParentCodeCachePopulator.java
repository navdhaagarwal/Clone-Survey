package com.nucleus.core.genericparameter.populator;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.genericparameter.dao.GenericParameterDao;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("genericParameterByParentCodeCachePopulator")
public class GenericParameterByParentCodeCachePopulator extends FWCachePopulator {

	@Inject
	@Named("genericParameterDao")
	private GenericParameterDao genericParameterDao;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : GenericParameterByParentCodeCachePopulator");
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		String[] keyArray = ((String) key).split(FWCacheConstants.REGEX_DELIMITER,2);
		try {
			List<? extends GenericParameter> genericParameterList = genericParameterDao.findChildrenByParentCode(
					keyArray[1], (Class<? extends GenericParameter>) Class.forName(keyArray[0]));
			Set<Long> parentCodeBasedGenericParameterIdSet = new HashSet<>();
			for (GenericParameter genericParameter : genericParameterList) {
				parentCodeBasedGenericParameterIdSet.add(genericParameter.getId());
			}
			return parentCodeBasedGenericParameterIdSet;
		} catch (ClassNotFoundException e) {
			BaseLoggers.flowLogger.error(e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void build(Long tenantId) {
		Map<String,Set<Long>> localMap = new HashMap<>();
		for (Map.Entry<String, List<? extends GenericParameter>> entry : genericParameterDao
				.getDTypeBasedMapOfGenericParameter().entrySet()) {
			String dTypeName = entry.getKey();
			Object genericParameterObjList = entry.getValue();
			if (ValidatorUtils.notNull(genericParameterObjList)) {
				List<GenericParameter> genericParameterList = (List<GenericParameter>) genericParameterObjList;
				for (GenericParameter genericParameter : genericParameterList) {
					prepareParentCodeCacheOfGenericParameter(dTypeName, genericParameter,localMap);
				}
			}
		}
		putAll(localMap);
	}

	@Override
	public void update(Action action, Object object) {
		GenericParameter genericParameter = (GenericParameter) object;
		String dTypeName = genericParameter.getClass().getName();

		if (action.equals(Action.DELETE) && ValidatorUtils.notNull(genericParameter)) {
			removeFromGenericParameterEntitiesByParentCodeCache(dTypeName, genericParameter);
		} else if ((action.equals(Action.INSERT) || action.equals(Action.UPDATE))
				&& ValidatorUtils.notNull(genericParameter)) {
			prepareParentCodeCacheOfGenericParameter(dTypeName, genericParameter,null);
		}
	}

	@SuppressWarnings("unchecked")
	private void prepareParentCodeCacheOfGenericParameter(String dTypeName, GenericParameter genericParameter, Map<String,Set<Long>> localMap) {
		if (ValidatorUtils.notNull(genericParameter.getParentCode())
				&& genericParameter.getPersistenceStatus().equals(PersistenceStatus.ACTIVE)
				&& genericParameter.isActiveFlag() && !genericParameter.getParentCode().isEmpty()) {
			String key = new StringBuilder(dTypeName).append(FWCacheConstants.KEY_DELIMITER)
					.append(genericParameter.getParentCode()).toString();
			Set<Long> parentCodeBasedGenericParameterIdSet = (Set<Long>) (localMap == null ? get(key) : localMap.get(key));
			if (ValidatorUtils.isNull(parentCodeBasedGenericParameterIdSet)) {
				parentCodeBasedGenericParameterIdSet = new HashSet<>();
			}
			parentCodeBasedGenericParameterIdSet.add(genericParameter.getId());
			if (localMap == null) {
				put(key, parentCodeBasedGenericParameterIdSet);
			} else {
				localMap.put(key, parentCodeBasedGenericParameterIdSet);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void removeFromGenericParameterEntitiesByParentCodeCache(String dTypeName,
			GenericParameter oldGenericParameter) {
		if (ValidatorUtils.notNull(oldGenericParameter.getParentCode())) {
			String key = new StringBuilder(dTypeName).append(FWCacheConstants.KEY_DELIMITER)
					.append(oldGenericParameter.getParentCode()).toString();
			Set<Long> parentCodeBasedGenericParameterIdSet = (Set<Long>) get(key);
			if (ValidatorUtils.notNull(parentCodeBasedGenericParameterIdSet)
					&& !parentCodeBasedGenericParameterIdSet.isEmpty()) {
				parentCodeBasedGenericParameterIdSet.remove(oldGenericParameter.getId());
				put(key, parentCodeBasedGenericParameterIdSet);
			}
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.GENERIC_PARAMETER_PARENTCODE_ENTITIES;
	}
	
	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.GENERIC_PARAMETER_CACHE_GROUP;
	}

}
