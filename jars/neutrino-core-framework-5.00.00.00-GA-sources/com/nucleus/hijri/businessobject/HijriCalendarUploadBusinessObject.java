package com.nucleus.hijri.businessobject;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.hijri.dao.IHijriGregorianUploadDAO;
import com.nucleus.hijri.domainobject.GregorianHijriCalendarMapping;
import com.nucleus.persistence.EntityDao;

@Named("hijriCalendarUploadBusinessObject")
public class HijriCalendarUploadBusinessObject implements
		IHijriCalendarUploadBusinessObject {

    @Inject
    @Named("entityDao")
    private EntityDao                entityDao;

	@Inject
	@Named("hijriGregorianUploadDAO")
	private IHijriGregorianUploadDAO hijriGregorianUploadDAO;

	

	

	@Override
	public GregorianHijriCalendarMapping uploadHijriCalendar(
			GregorianHijriCalendarMapping hijriCalendarUpload) {
		if (hijriCalendarUpload.getGregorianDate().compareTo(CoreUtility.getBusinessDate()) >=0) {
			GregorianHijriCalendarMapping hijriDataBasedOnGreg = hijriGregorianUploadDAO
					.getCalendarBasedOnGregDate(hijriCalendarUpload);
			if (checkIfGregorianAlreadyExisting(hijriDataBasedOnGreg)) {
				hijriDataBasedOnGreg.setHijriDate(hijriCalendarUpload
						.getHijriDate());
				entityDao.update(hijriDataBasedOnGreg);
			} else {
				entityDao.persist(hijriCalendarUpload);
			}
			
		}else{
			
//			List<Message> validationMessages = new ArrayList<Message>();
//			validationMessages.add(new Message(HijriConstants.INVALID_GREGORIAN_DATE,Message.MessageType.ERROR,hijriCalendarUpload.getGregorianDate().toString(),requestServicingContext.getBusinessDate().toString() ));
//			throw ExceptionBuilder.getInstance(BusinessException.class, HijriConstants.INVALID_GREGORIAN_DATE, "Gregorian Date is invalid.").setMessages(validationMessages).build();
		}
		return hijriCalendarUpload;
	}

	protected Boolean checkIfGregorianAlreadyExisting(
			GregorianHijriCalendarMapping hijriDataBasedOnGreg) {
		if (hijriDataBasedOnGreg == null) {
			return false;
		} else {
			return true;
		}
	}

}
