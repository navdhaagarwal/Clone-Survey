package com.nucleus.core.event.notification;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationListener;

import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.notificationMaster.NotificationMasterPostCommitWorker;
import com.nucleus.notificationMaster.NotificationMasterVO;

@Named("notificationListener")
public class NotificationListener
  implements ApplicationListener<NotificationEvent>
{

  @Inject
  @Named("notificationMasterPostCommitWorker")
  private NotificationMasterPostCommitWorker notificationMasterPostCommitWorker;

  public void onApplicationEvent(NotificationEvent e)
  {
    NotificationEventWorker worker = (NotificationEventWorker)e.getEventWorker();

    NotificationMasterVO notificationMasterVO = new NotificationMasterVO();
    prepareNotificationMasterData(notificationMasterVO, worker);
    TransactionPostCommitWorker.handlePostCommit(notificationMasterPostCommitWorker, notificationMasterVO, true);
  }

  private void prepareNotificationMasterData(NotificationMasterVO notificationMasterVO, NotificationEventWorker worker)
  {
    notificationMasterVO.setNotificationMaster(worker.getNotificationMaster());
    notificationMasterVO.setContextMap(worker.getMap());
    notificationMasterVO.setMetadata(worker.getMetadata());
  }
}