package com.nucleus.web.trustedsource;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.entity.EntityId;
import com.nucleus.event.Event;
import com.nucleus.event.EventTypes;
import com.nucleus.event.GenericEventListener;
import com.nucleus.event.MakerCheckerEvent;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.persistence.EntityDao;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.security.oauth.util.TrustedSourceHelper;

public class TrustedSourceApprovalListener extends GenericEventListener {
	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;

	@Override
	public boolean canHandleEvent(Event event) {
		if (event instanceof MakerCheckerEvent && (event.getEventType() == EventTypes.MAKER_CHECKER_APPROVED
				|| event.getEventType() == EventTypes.MAKER_CHECKER_UPDATED_APPROVED)) {
			MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
			EntityId entityId = makerCheckerEvent.getOwnerEntityId();
			if (entityId.getEntityClass() != null && entityId.getEntityClass().equals(OauthClientDetails.class)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void handleEvent(Event event) {
		MakerCheckerEvent makerCheckerEvent = (MakerCheckerEvent) event;
		EntityId entityId = makerCheckerEvent.getOwnerEntityId();
		OauthClientDetails trustedSource = entityDao.get(entityId);
		boolean invalidData = false;
		if (TrustedSourceHelper.isInternalModule(trustedSource.getClientId())) {
			if (!trustedSource.getIsInternal()) {
				invalidData = true;
			}
		} else if (trustedSource.getIsInternal()) {
			invalidData = true;
		}
		if (invalidData) {
			Message message = new Message("label.internal.source.manual", MessageType.ERROR);

			throw ExceptionBuilder.getInstance(BusinessException.class).setMessage(message)
					.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
		}

	}

}
