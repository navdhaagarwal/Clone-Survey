package com.nucleus.pushnotification.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.pushnotification.vo.PushNoticationsClient;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.user.UserInfo;
import com.nucleus.ws.core.entities.PushNotificationClientDetail;


@Named("pushNotificationClientService")
public class PushNotificationClientServiceImpl implements PushNotificationClientService {

	@Inject
	@Named("baseMasterDao")
	protected BaseMasterDao baseMasterDao;

	private PushNotificationClientDetail findActivePushNotificationClientDetailByClientId(String notificationClientId) {
		NamedQueryExecutor<PushNotificationClientDetail> executor = new NamedQueryExecutor<PushNotificationClientDetail>(
				"findNotificationClientByNotificationClientId")
						.addParameter("notificationClientId", notificationClientId)
						.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

		return baseMasterDao.executeQueryForSingleValue(executor);

	}

	@Override
	public List<PushNotificationClientDetail> findActivePushNotificationClientDetailByNotificationClientIds(List<String> notificationClientIds) {
		NamedQueryExecutor<PushNotificationClientDetail> executor = new NamedQueryExecutor<PushNotificationClientDetail>(
				"findNotificationClientByListOfNotificationClientId").addParameter("notificationClientIds", notificationClientIds)
						.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

		return baseMasterDao.executeQuery(executor);

	}
	
	@Override
	public List<PushNotificationClientDetail> findActivePushNotificationClientDetailByUserIds(List<Long> userIds) {
		NamedQueryExecutor<PushNotificationClientDetail> executor = new NamedQueryExecutor<PushNotificationClientDetail>(
				"findNotificationClientByListOfUserIds").addParameter("userIds", userIds)
						.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

		return baseMasterDao.executeQuery(executor);

	}

	@Override
	public List<PushNotificationClientDetail> findActivePushNotificationClientDetailByUserIdsAndTrustedSourceModules(List<Long> userIds, List<String> trustedSourceNames) {
		NamedQueryExecutor<PushNotificationClientDetail> executor = new NamedQueryExecutor<PushNotificationClientDetail>(
				"findNotificationClientByListOfUserIdsAndTrustedSourceModules").addParameter("userIds", userIds).addParameter("trustedSourceIds", trustedSourceNames)
				.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

		return baseMasterDao.executeQuery(executor);

	}

	@Override
	public PushNotificationClientDetail unregisterPushNotification(String notificationClientId) {

		PushNotificationClientDetail pushNotificationClientDtl = findActivePushNotificationClientDetailByClientId(
				notificationClientId);
		if (pushNotificationClientDtl != null) {
			pushNotificationClientDtl.setActiveFlag(false);
			pushNotificationClientDtl.setInactivationDate(new DateTime());
			baseMasterDao.saveOrUpdate(pushNotificationClientDtl);
		}
		return pushNotificationClientDtl;
	}

	@Override
	public void registerPushNotification(UserInfo user, String trustedSourceName,
			PushNoticationsClient pushNoticationsClient) {

		PushNotificationClientDetail pushNotificationClientDtl = findActivePushNotificationClientDetailByClientId(
				pushNoticationsClient.getNotificationClientId());

		if (pushNotificationClientDtl == null) {
			pushNotificationClientDtl = new PushNotificationClientDetail();
		}

		pushNotificationClientDtl.setInactivationDate(null);
		pushNotificationClientDtl.setTrustedSourceId(trustedSourceName);
		pushNotificationClientDtl.setUserId(user.getId());
		pushNotificationClientDtl.setUsername(user.getUsername());
		pushNotificationClientDtl.setApprovalStatus(0);
		pushNotificationClientDtl.setImeiNumber(pushNoticationsClient.getImeiNumber());
		pushNotificationClientDtl.setOperatingSystem(pushNoticationsClient.getOperatingSystem());
		pushNotificationClientDtl.setDeviceType(pushNoticationsClient.getDeviceType());
		pushNotificationClientDtl.setNotificationClientId(pushNoticationsClient.getNotificationClientId());
		baseMasterDao.saveOrUpdate(pushNotificationClientDtl);
	}

}

