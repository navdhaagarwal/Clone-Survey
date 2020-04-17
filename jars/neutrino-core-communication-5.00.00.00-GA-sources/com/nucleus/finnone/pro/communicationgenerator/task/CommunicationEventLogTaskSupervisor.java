package com.nucleus.finnone.pro.communicationgenerator.task;

import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.ACTIVE_TASK_COUNT;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.BATCH_SIZE_COUNT;
import static com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants.CONCURRENT_TASK_COUNT;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Hibernate;

import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.config.persisted.enity.ConfigurationGroup;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.IAdHocEventLogCriteriaBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationEventLogBatchVO;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationEventLogTaskSupervisor")
public class CommunicationEventLogTaskSupervisor implements

ICommunicationEventLogTaskSupervisor {
 @Inject
 @Named("configurationService")
 private ConfigurationService configurationService;

 @Inject
 @Named("beanAccessHelper")
 private BeanAccessHelper beanAccessHelper;

 @Inject
 @Named("adHocEventLogCriteriaBusinessObject")
 private IAdHocEventLogCriteriaBusinessObject adHocEventLogCriteriaBusinessObject;
 private Map<String, Integer> taskConfigurations = null;

 @Override
 public void submitObjectsToLogEvents(String eventCode,
   SourceProduct sourceProduct, String rootContextObject,
   String requestReferenceId, Boolean generateMergedFile) {
  if (taskConfigurations == null) {
   taskConfigurations = getTaskConfigurations();
  }

  int concurrentTaskCount = taskConfigurations.get(CONCURRENT_TASK_COUNT);
  int activeTaskCount = taskConfigurations.get(ACTIVE_TASK_COUNT);
  int batchSizeCount = taskConfigurations.get(BATCH_SIZE_COUNT);
  ExecutorService executorService = Executors
    .newFixedThreadPool(activeTaskCount);
  CompletionService<Long> completionService = new ExecutorCompletionService<Long>(
    executorService);
  List<BaseEntity> batchObjectList = new ArrayList<BaseEntity>();

  int countBatchObjects = adHocEventLogCriteriaBusinessObject
    .fetchTotalRecordSize(rootContextObject);
  int startIndex = 0;
  int processedItems = 0;
  while (processedItems < countBatchObjects
    || hasElements(batchObjectList)) {
   if (batchObjectList.size() < concurrentTaskCount) {
    batchObjectList.addAll(adHocEventLogCriteriaBusinessObject
      .fetchEntitiesBasedOnBatchSize(startIndex,
        batchSizeCount, rootContextObject));
    startIndex = startIndex + batchSizeCount;
   }
   CommunicationEventLogBatchVO communicationVO = new CommunicationEventLogBatchVO();
   communicationVO.setBatchObjectList(batchObjectList);
   communicationVO.setEventCode(eventCode);
   communicationVO.setSourceProduct(sourceProduct);
   communicationVO.setRootContextObject(rootContextObject);
   communicationVO.setCompletionService(completionService);
   communicationVO.setExecutorService(executorService);
   communicationVO.setRequestReferenceId(requestReferenceId);
   communicationVO.setGenerateMergedFile(generateMergedFile);
   processedItems = processedItems + processBatch(communicationVO);
  }
  executorService.shutdown();
 }

 protected int processBatch(CommunicationEventLogBatchVO communicationVO) {
  Long resultStatus = null;
  Long errorCount = 0L;
  Long successCount = 0L;
  int processedItems = 0;
  int concurrentTaskCount = taskConfigurations.get(CONCURRENT_TASK_COUNT);
  List<BaseEntity> batchObjectList = communicationVO.getBatchObjectList();
  CompletionService<Long> completionService = communicationVO
    .getCompletionService();
  while (batchObjectList.size() > 0) {
   List<BaseEntity> batchObjectsSubList = getNextBatchForProcessing(
     concurrentTaskCount, batchObjectList);
   processedItems = processedItems + batchObjectsSubList.size();
   // Submit tasks
   for (BaseEntity batchObject : batchObjectsSubList) {
    BaseLoggers.flowLogger.info("Adding " + batchObject.getUri()
      + " for processing");
    ICommunicationEventLogTask communicationEventLogTask = beanAccessHelper
      .getBean("communicationEventLogTask",
        ICommunicationEventLogTask.class);
    communicationEventLogTask.populateEventLogTask(batchObject,
      communicationVO.getEventCode(),
      communicationVO.getSourceProduct(),
      communicationVO.getRootContextObject(),
      communicationVO.getRequestReferenceId(),
      communicationVO.getGenerateMergedFile());
    completionService.submit(communicationEventLogTask);
   }

   // Check for tasks complition
   Iterator<BaseEntity> batchIterator = batchObjectsSubList.iterator();
   while (batchIterator.hasNext()) {
    batchIterator.next();
    try {
     resultStatus = completionService.take().get();
     if (resultStatus == 1L) {
      successCount += 1;
     } else {
      errorCount += 1;
     }

    } catch (InterruptedException ex) {
     BaseLoggers.exceptionLogger
       .error("CommunicationEventLogTaskSupervisor.submitObjectsToLogEvent - interrupted exception while waiting for task to be completed",
         ex);
     errorCount += 1;
    } catch (ExecutionException ex) {
     BaseLoggers.exceptionLogger
       .error("CommunicationEventLogTaskSupervisor.submitObjectsToLogEvent - ExecutionException while waiting for task to be completed",
         ex);
     errorCount += 1;
    }

   }

   if (batchObjectList.size() < concurrentTaskCount)
    break;

  }
  return processedItems;

 }

 protected Map<String, Integer> getTaskConfigurations() {

  Integer defaultConcurrentTaskCount = CommunicationGeneratorConstants.DEFAULT_CONCURRENT_TASK_COUNT;
  Integer defaultActiveTaskCount = CommunicationGeneratorConstants.DEFAULT_ACTIVE_TASK_COUNT;
  Integer defaultBatchSizeCount = CommunicationGeneratorConstants.DEFAULT_BATCH_SIZE_COUNT;
  Map<String, Integer> taskConfigurations = new HashMap<String, Integer>();
  taskConfigurations.put(CONCURRENT_TASK_COUNT,
    defaultConcurrentTaskCount);
  taskConfigurations.put(ACTIVE_TASK_COUNT, defaultActiveTaskCount);
  taskConfigurations.put(BATCH_SIZE_COUNT, defaultBatchSizeCount);
  ConfigurationGroup configurationGroup = configurationService
    .getConfigurationGroupFor(SystemEntity.getSystemEntityId());

  if (isNull(configurationGroup)
    || !hasElements(configurationGroup.getConfiguration())) {
   return taskConfigurations;
  }

  List<Configuration> configurations = configurationGroup
    .getConfiguration();
  Hibernate.initialize(configurations);
  for (Configuration configuration : configurations) {

   if (isNull(configuration.getPropertyKey())
     || isNull(configuration.getPropertyValue())) {

    continue;
   }
   putValuesInTaskConfigurations(CONCURRENT_TASK_COUNT,
     configuration.getPropertyKey(),
     configuration.getPropertyValue(), taskConfigurations);
   putValuesInTaskConfigurations(ACTIVE_TASK_COUNT,
     configuration.getPropertyKey(),
     configuration.getPropertyValue(), taskConfigurations);
   putValuesInTaskConfigurations(BATCH_SIZE_COUNT,
     configuration.getPropertyKey(),
     configuration.getPropertyValue(), taskConfigurations);

  }
  return taskConfigurations;

 }

 protected List<BaseEntity> getNextBatchForProcessing(
   int concurrentTaskCount, List<BaseEntity> batchObjectList) {

  int subListRecordCount = 0;
  List<BaseEntity> subBatchObjectsList = new ArrayList<BaseEntity>();

  if (hasElements(batchObjectList)) {
   Iterator<BaseEntity> iterator = batchObjectList.iterator();
   while (subListRecordCount < concurrentTaskCount
     && iterator.hasNext()) {
    BaseEntity batchObject = iterator.next();
    if (notNull(batchObject)) {
     subBatchObjectsList.add(batchObject);
     subListRecordCount++;
     iterator.remove();
    }
   }
  }
  return subBatchObjectsList;
 }

 protected void putValuesInTaskConfigurations(String providedConfigKey,
   String key, String value, Map<String, Integer> taskConfigurations) {

  if (providedConfigKey.equals(key)) {
   try {
    taskConfigurations.put(providedConfigKey,
      Integer.parseInt(value));
   } catch (NumberFormatException ne) {
    // do not do anything
   }
  }
 }

}
