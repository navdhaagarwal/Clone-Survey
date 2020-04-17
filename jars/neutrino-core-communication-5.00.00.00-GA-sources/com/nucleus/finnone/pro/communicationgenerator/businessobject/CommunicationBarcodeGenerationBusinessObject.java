package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.communicationgenerator.dao.CommunicationTrackingDao;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationGenerationDetailHistory;

@Named("communicationeGenerationBarcodeDetailBusinessObject")
public class CommunicationBarcodeGenerationBusinessObject
		implements ICommunicationBarcodeGenerationBusinessObject {

	@Inject
	@Named("communicationTrackingDao")
	private CommunicationTrackingDao communicationTrackingDao;

	@Override
	public CommunicationGenerationDetailHistory getCommunicationHistoryByUniqueBarcodeReferenceNumber(
			String barcodeReferenceNumber) {

		return communicationTrackingDao.getCommunicationHistoryByUniqueBarcodeReferenceNumber(barcodeReferenceNumber);
	}

}
