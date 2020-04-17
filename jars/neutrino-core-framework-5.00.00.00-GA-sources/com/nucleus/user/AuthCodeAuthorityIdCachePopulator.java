package com.nucleus.user;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.authority.Authority;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;

@Named("authCodeAuthorityIdCachePopulator")
public class AuthCodeAuthorityIdCachePopulator extends FWCachePopulator {

	@Inject
	@Named(value = "userService")
	private UserService userService;

	@Inject
	@Named(value = "entityDao")
	private EntityDao entityDao;

	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : AuthCodeAuthorityIdCachePopulator");
	}

	@Override
	@Transactional(readOnly = true)
	public Object fallback(Object key) {
		List<Authority> authorityList = userService.getAuthorityByCodeFromDb((String) key);
		if (ValidatorUtils.hasElements(authorityList)) {
			return authorityList.get(0).getId();
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public void build(Long tenantId) {
		List<Authority> authorityList = entityDao.findAll(Authority.class);
		if (ValidatorUtils.hasElements(authorityList)) {
			for (Authority authority : authorityList) {
				put(authority.getAuthCode(), authority.getId());
			}
		}
	}

	@Override
	public void update(Action action, Object object) {
		Authority authority = (Authority) object;
		if (action.equals(Action.UPDATE) && ValidatorUtils.notNull(authority)) {
			put(authority.getAuthCode(), authority.getId());
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.AUTHCODE_AUTHORITY_ID_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.USER_CACHE_GROUP;
	}

}
