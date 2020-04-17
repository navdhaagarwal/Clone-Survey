package com.nucleus.finnone.pro.communicationgenerator.util;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import java.util.Date;
import javax.inject.Named;
import org.joda.time.DateTime;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationEventLoggerHelper")
public class CommunicationEventLoggerHelper {
	
	public CommunicationRequestDetail prepareCommunicationRequestDetailObject(
			String subjectURI,
			String applicablePrimaryURI,
			String communicationEventCode,
			CommunicationTemplate communicationTemplate,
			String phoneNumbers,
			String emailAddress,
			String ccAddress,
			String bccAddress,
			AdditionalData additionalData,
			SourceProduct module,
			String subjectReferenceNumber,
			String subjectReferenceType,
			DateTime eventLogTimeStamp,
			Date referenceDate) {
		CommunicationRequestDetail communicationRequestDetail = new CommunicationRequestDetail();
		communicationRequestDetail.setStatus('I');
		communicationRequestDetail.setCommunicationCode(communicationTemplate.getCommunication().getCommunicationCode());
		communicationRequestDetail.setCommunicationTemplate(communicationTemplate);
		communicationRequestDetail.setRetriedAttemptsDone(0);
		communicationRequestDetail.setAdditionalData(additionalData);
		communicationRequestDetail.setCommunicationEventCode(communicationEventCode);
		communicationRequestDetail.setCommunicationTemplateId(communicationTemplate.getId());
		communicationRequestDetail.setSubjectURI(subjectURI);
		communicationRequestDetail.setSubjectId(EntityId.fromUri(subjectURI).getLocalId());
		communicationRequestDetail.setSubjectReferenceNumber(subjectReferenceNumber);
		communicationRequestDetail.setSubjectReferenceType(subjectReferenceType);
		communicationRequestDetail.setPhoneNumber(phoneNumbers);
		communicationRequestDetail.setPrimaryEmailAddress(emailAddress);
		communicationRequestDetail.setCcEmailAddress(ccAddress);
		communicationRequestDetail.setBccEmailAddress(bccAddress);
		communicationRequestDetail.setCommunicationTemplateCode(communicationTemplate.getCommunicationTemplateCode());
		communicationRequestDetail.setApplicablePrimaryEntityURI(applicablePrimaryURI);
		communicationRequestDetail.setEventLogTimeStamp(eventLogTimeStamp);
		communicationRequestDetail.setReferenceDate(referenceDate);
		communicationRequestDetail.setSourceProduct(module);
		if (notNull(applicablePrimaryURI)) {
			communicationRequestDetail.setApplicablePrimaryEntityId(EntityId.fromUri(applicablePrimaryURI).getLocalId());
		}
		if (notNull(additionalData)) {
			communicationRequestDetail.setAdditionalFieldTxnId(additionalData.getId());
		}
		return communicationRequestDetail;
	}
}
