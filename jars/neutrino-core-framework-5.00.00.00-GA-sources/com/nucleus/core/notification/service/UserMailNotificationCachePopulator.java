package com.nucleus.core.notification.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.notification.UserMailNotification;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;

@Named("userMailNotificationCachePopulator")
public class UserMailNotificationCachePopulator extends FWCachePopulator {

	@Inject
	@Named(value = "entityDao")
	private EntityDao entityDao;
	
	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : UserMailNotificationCachePopulator");
	}

	@Override
	public Object fallback(Object key) {
		String[] keyArray = ((String) key).split(FWCacheConstants.CACHE_IDENTIFER_DELIMITER);
		String userUri = keyArray[0];
		String messageStatus = keyArray[1];
		NamedQueryExecutor<Long> executor = new NamedQueryExecutor<>("UserMailNotification.countUserMailInboxByStatus");
		executor.addParameter("userEntityId", userUri).addParameter("queryStatus", messageStatus);
		NeutrinoValidator.notNull(executor, "Some error with data");
		return entityDao.executeQueryForSingleValue(executor);
	}

	@Override
	public void build(Long tenantId) {
		BaseLoggers.flowLogger.debug("Build Called : UserMailNotificationCachePopulator");
	}

	@Override
	public void update(Action action, Object object) {
		if (action.equals(Action.DELETE)) {
			reduceCountInCache((UserMailNotification) object);
		} else if (action.equals(Action.UPDATE)) {
			increaseCountInCache((UserMailNotification) object);
		}
	}
	
	private void reduceCountInCache(UserMailNotification oldUserMailNotification) {
		String toUserUri = oldUserMailNotification.getToUserUri();
		String oldMsgStatus = oldUserMailNotification.getMsgStatus();
		if (ValidatorUtils.notNull(toUserUri) && ValidatorUtils.notNull(oldMsgStatus)) {
			String key = new StringBuilder(toUserUri).append(FWCacheConstants.CACHE_IDENTIFER_DELIMITER)
					.append(oldMsgStatus).toString();
			Long count = (Long) get(key);

			if (count > 0) {
				count--;
				put(key, count);
			}

		}
	}

	
	private void increaseCountInCache(UserMailNotification userMailNotification) {
		String toUserUri = userMailNotification.getToUserUri();
		String msgStatus = userMailNotification.getMsgStatus();
		if (ValidatorUtils.notNull(toUserUri) && ValidatorUtils.notNull(msgStatus)) {
			String key = new StringBuilder(toUserUri).append(FWCacheConstants.CACHE_IDENTIFER_DELIMITER)
					.append(msgStatus).toString();
			Long count = (Long) get(key);
			count++;
			put(key, count);
		}
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.USER_MAIL_NOTIFICATION_COUNT_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.MISCELLANEOUS_CACHE_GROUP;
	}

}
