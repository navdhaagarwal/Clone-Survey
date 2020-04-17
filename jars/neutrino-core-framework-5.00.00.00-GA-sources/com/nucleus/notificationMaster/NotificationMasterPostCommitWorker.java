package com.nucleus.notificationMaster;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.notificationMaster.service.NotificationMasterService;

@Named(value="notificationMasterPostCommitWorker")
public class NotificationMasterPostCommitWorker implements TransactionPostCommitWork{
	
    @Inject
    @Named("notificationMasterService")
    private NotificationMasterService notificationMasterService;
    
	@Override
	public void work(Object argument) {		
		
		if(argument instanceof NotificationMasterVO){
			NotificationMasterVO notificationMasterVO = (NotificationMasterVO)argument;
			notificationMasterService.sendNotification(notificationMasterVO.getNotificationMaster(), notificationMasterVO.getContextMap(), null , notificationMasterVO.getMetadata());	
		}
		
	}
}
