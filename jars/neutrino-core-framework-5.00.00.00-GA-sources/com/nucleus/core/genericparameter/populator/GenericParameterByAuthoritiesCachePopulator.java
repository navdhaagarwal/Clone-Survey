package com.nucleus.core.genericparameter.populator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.authority.Authority;
import com.nucleus.core.genericparameter.dao.GenericParameterDao;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;

@Named("genericParameterByAuthoritiesCachePopulator")
public class GenericParameterByAuthoritiesCachePopulator extends FWCachePopulator {

	@Inject
	@Named("genericParameterDao")
	private GenericParameterDao genericParameterDao;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : GenericParameterByAuthoritiesCachePopulator");
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		String[] keyArray = ((String) key).split(FWCacheConstants.REGEX_DELIMITER, 2);
		try {
			List<? extends GenericParameter> genericParameterList = genericParameterDao.findByAuthorities(
					Arrays.asList(keyArray[1]), (Class<? extends GenericParameter>) Class.forName(keyArray[0]));
			Set<Long> authCodeGenericParameterIdSet = new HashSet<>();
			for (GenericParameter genericParameter : genericParameterList) {
				authCodeGenericParameterIdSet.add(genericParameter.getId());
			}
			return authCodeGenericParameterIdSet;
		} catch (ClassNotFoundException e) {
			BaseLoggers.flowLogger.error(e.getMessage());
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
					prepareAuthCodeCacheOfGenericParameter(dTypeName, genericParameter);
				}
			}
		}
	}

	@Override
	public void update(Action action, Object object) {
		GenericParameter genericParameter = (GenericParameter) object;
		String dTypeName = genericParameter.getClass().getName();

		if (action.equals(Action.DELETE) && ValidatorUtils.notNull(genericParameter)) {
			removeFromGenericParameterEntitiesByAuthCodeCache(dTypeName, genericParameter);
		} else if ((action.equals(Action.INSERT) || action.equals(Action.UPDATE))
				&& ValidatorUtils.notNull(genericParameter)) {
			prepareAuthCodeCacheOfGenericParameter(dTypeName, genericParameter);
		}
	}

	@SuppressWarnings("unchecked")
	private void prepareAuthCodeCacheOfGenericParameter(String dtypeName, GenericParameter genericParameter) {
		if (ValidatorUtils.notNull(genericParameter.getAuthorities())) {
			Set<Long> authCodeGenericParameterSet = null;

			for (Authority authority : genericParameter.getAuthorities()) {
				String key = new StringBuilder(dtypeName).append(FWCacheConstants.KEY_DELIMITER)
						.append(authority.getAuthCode()).toString();
				authCodeGenericParameterSet = (Set<Long>) get(key);
				if (ValidatorUtils.isNull(authCodeGenericParameterSet)) {
					authCodeGenericParameterSet = new HashSet<>();
				}
				authCodeGenericParameterSet.add(genericParameter.getId());
				put(key, authCodeGenericParameterSet);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void removeFromGenericParameterEntitiesByAuthCodeCache(String dtypeName,
			GenericParameter genericParameter) {
		if (ValidatorUtils.notNull(genericParameter.getAuthorities())) {
			Set<Long> authCodeGenericParameterSet = null;
			for (Authority authority : genericParameter.getAuthorities()) {
				String key = new StringBuilder(dtypeName).append(FWCacheConstants.KEY_DELIMITER)
						.append(authority.getAuthCode()).toString();
				authCodeGenericParameterSet = (Set<Long>) get(key);
				if (ValidatorUtils.notNull(authCodeGenericParameterSet) && !authCodeGenericParameterSet.isEmpty()) {
					authCodeGenericParameterSet.remove(genericParameter.getId());
					put(key, authCodeGenericParameterSet);
				}
			}
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.GENERIC_PARAMETER_AUTHCODE_ENTITIES;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.GENERIC_PARAMETER_CACHE_GROUP;
	}
	
}
