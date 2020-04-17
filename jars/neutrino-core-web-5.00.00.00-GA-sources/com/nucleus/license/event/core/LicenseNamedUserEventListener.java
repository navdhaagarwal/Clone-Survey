package com.nucleus.license.event.core;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.springframework.context.ApplicationListener;

import com.nucleus.license.audit.LicenseAuditLog;
import com.nucleus.license.dao.ILicenseClientDAO;
import com.nucleus.license.event.LicenseNamedUserEvent;
import com.nucleus.license.event.LicenseNamedUserEventWorker;
import com.nucleus.logging.BaseLoggers;

@Named("licenseNamedUserEventListener")
public class LicenseNamedUserEventListener implements ApplicationListener<LicenseNamedUserEvent> {

	@Inject
	@Named("licenseClientDAO")
	private ILicenseClientDAO licenseClientDAO;
	
    public LicenseAuditLog saveApplicationEvent(LicenseNamedUserEventWorker worker){
		 LicenseAuditLog auditLog=new LicenseAuditLog();
		 String myEncryptedText = worker.getName(); 
		 auditLog.setDescription(myEncryptedText);
		 auditLog.setEventType(worker.getEventType());
		 auditLog.setEventTime(new DateTime());
		 licenseClientDAO.saveOrUpdate(auditLog,null);
		 return auditLog;
	}
	
	@Override
	public void onApplicationEvent(LicenseNamedUserEvent event) {
		 LicenseNamedUserEventWorker worker=(LicenseNamedUserEventWorker) event.getEventWorker();
		 saveApplicationEvent(worker);
		 BaseLoggers.flowLogger.debug("LicenseNamedUserEventListener onApplicationEvent(): LicenseAuditLog saved in Database");
	}

}
