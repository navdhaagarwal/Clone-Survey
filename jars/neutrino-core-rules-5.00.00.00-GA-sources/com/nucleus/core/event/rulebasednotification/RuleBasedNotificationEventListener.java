package com.nucleus.core.event.rulebasednotification;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationListener;

import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.notificationMaster.NotificationMasterPostCommitWorker;
import com.nucleus.notificationMaster.NotificationMasterVO;
import com.nucleus.rules.service.RuleGroupEvaluationService;

@Named("ruleBasedNotEventListener")
public class RuleBasedNotificationEventListener
  implements ApplicationListener<RuleBasedNotificationEvent>
{

  @Inject
  @Named("ruleGroupEvaluationService")
  private RuleGroupEvaluationService ruleGroupEvaluationService;

  @Inject
  @Named("notificationMasterPostCommitWorker")
  private NotificationMasterPostCommitWorker notificationMasterPostCommitWorker;

  public void onApplicationEvent(RuleBasedNotificationEvent event)
  {
    RuleBasedNotificationEventWorker worker = (RuleBasedNotificationEventWorker)event.getEventWorker();
    Boolean result = this.ruleGroupEvaluationService.executeRuleGroup(worker.getRuleGroup(), worker.getMap(), worker.getUuid
      (), worker.getName(), worker.isAuditingEnabled(),worker.isPurgingRequired());

    if ((null != result) && (result.booleanValue()))
      try
      {
        NotificationMasterVO notificationMasterVO = new NotificationMasterVO();
        prepareNotificationMasterData(notificationMasterVO, worker);
        TransactionPostCommitWorker.handlePostCommit(notificationMasterPostCommitWorker, notificationMasterVO, true);
      }
      catch (Exception exception) {
        BaseLoggers.exceptionLogger.debug("Exception occured while processing notification." + exception);
      }
  }

  private void prepareNotificationMasterData(NotificationMasterVO notificationMasterVO, RuleBasedNotificationEventWorker worker)
  {
    notificationMasterVO.setNotificationMaster(worker.getNotificationMaster());
    notificationMasterVO.setContextMap(worker.getMap());
    notificationMasterVO.setMetadata(worker.getMetadata());
  }
}