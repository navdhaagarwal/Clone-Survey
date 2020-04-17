package com.nucleus.finnone.pro.communicationgenerator.vo;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;

import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.SourceProduct;

public class CommunicationEventLogBatchVO {

    private List<BaseEntity> batchObjectList;
    private String eventCode;
    private SourceProduct sourceProduct;
    private String rootContextObject;
    private CompletionService<Long> completionService;
    private ExecutorService executorService;
    private String requestReferenceId;
    private Boolean generateMergedFile;

    public List<BaseEntity> getBatchObjectList() {
        return batchObjectList;
    }

    public void setBatchObjectList(List<BaseEntity> batchObjectList) {
        this.batchObjectList = batchObjectList;
    }

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

    public String getRootContextObject() {
        return rootContextObject;
    }

    public void setRootContextObject(String rootContextObject) {
        this.rootContextObject = rootContextObject;
    }

    public CompletionService<Long> getCompletionService() {
        return completionService;
    }

    public void setCompletionService(CompletionService<Long> completionService) {
        this.completionService = completionService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public String getRequestReferenceId() {
        return requestReferenceId;
    }

    public void setRequestReferenceId(String requestReferenceId) {
        this.requestReferenceId = requestReferenceId;
    }

    public Boolean getGenerateMergedFile() {
        return generateMergedFile;
    }

    public void setGenerateMergedFile(Boolean generateMergedFile) {
        this.generateMergedFile = generateMergedFile;
    }

}
