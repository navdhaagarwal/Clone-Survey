package com.nucleus.finnone.pro.communicationgenerator.task;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.IAdHocEventLogCriteriaBusinessObject;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationEventLogTask")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CommunicationEventLogTask implements ICommunicationEventLogTask {

 private BaseEntity batchObject;

 private String eventCode;

 private SourceProduct sourceProduct;

 private String rootContextObject;

 private String requestReferenceId;

 private Boolean generateMergedFile;

 @Inject
 @Named("adHocEventLogCriteriaBusinessObject")
 private IAdHocEventLogCriteriaBusinessObject adHocEventLogCriteriaBusinessObject;

 public String getEventCode() {
  return eventCode;
 }

 public void setEventCode(String eventCode) {
  this.eventCode = eventCode;
 }

 public SourceProduct getSourceProduct() {
  return sourceProduct;
 }

 public void setSourceProduct(SourceProduct sourceProduct) {
  this.sourceProduct = sourceProduct;
 }

 public BaseEntity getBatchObject() {
  return batchObject;
 }

 public void setBatchObject(BaseEntity batchObject) {
  this.batchObject = batchObject;
 }

 public String getRootContextObject() {
  return rootContextObject;
 }

 public void setRootContextObject(String rootContextObject) {
  this.rootContextObject = rootContextObject;
 }

 public String getRequestReferenceId() {
  return requestReferenceId;
 }

 public void setRequestReferenceId(String requestReferenceId) {
  this.requestReferenceId = requestReferenceId;
 }

 public Boolean getGenerateMergedFile() {
     if(ValidatorUtils.isNull(this.generateMergedFile))
     {
         this.generateMergedFile=false;
     }
  return generateMergedFile;
 }

 public void setGenerateMergedFile(Boolean generateMergedFile) {
  this.generateMergedFile = generateMergedFile;
 }

 @Override
 public Long call() throws Exception {
  adHocEventLogCriteriaBusinessObject
    .fireEventForAllApplicableEntitiesAndLogEvent(eventCode,
      sourceProduct, rootContextObject, batchObject,
      requestReferenceId, generateMergedFile);
  return 1L;
 }

 @Override
 public void populateEventLogTask(BaseEntity batchObject, String eventCode,
   SourceProduct sourceProduct, String rootContextObject,
   String requestReferenceId, Boolean generateMergedFile) {
  this.batchObject = batchObject;
  this.eventCode = eventCode;
  this.sourceProduct = sourceProduct;
  this.rootContextObject = rootContextObject;
  this.requestReferenceId = requestReferenceId;
  this.generateMergedFile = generateMergedFile;
 }

}
