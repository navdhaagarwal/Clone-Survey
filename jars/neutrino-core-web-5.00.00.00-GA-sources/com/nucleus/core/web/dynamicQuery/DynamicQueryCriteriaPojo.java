/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.web.dynamicQuery;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.nucleus.core.scheduler.service.SchedulerVO;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class DynamicQueryCriteriaPojo implements Serializable {

    private static final long serialVersionUID = -8595966259321291004L;

    private Long              queryContextId;

    private Long[]            selectItemIds;

    private String            whereClause;

    private int               maxResults;

    private Boolean           useDistinct;

    private Long              orderByTokenId;

    // fields for dynamic report generation

    private String            reportTitle;
    private Long              groupByTokenId;
    private String            exportType;
    private Long[]            sumForTokenIds;
    private Long[]            avgForTokenIds;

    private Long[]            percentageForTokenIds;
    private Long[]            countForTokenIds;

    private boolean           sumAtSummary;
    private boolean           sumAtGroup;
    private boolean           avgAtSummary;
    private boolean           avgAtGroup;
    private boolean           percentageAtSummary;
    private boolean           percentageAtGroup;
    private boolean           countAtSummary;
    private boolean           countAtGroup;

    // for charts
    private Long              chartType;
    private String            chartTitle;

    private Long              keyTokenId;
    private Long[]            seriesTokenIds;

    // fields added for dynamic form based reports
    private boolean           dynamicFormReport;
    private Long              dynamicFormId;
    private DateTime          fromDateFilter;
    private DateTime          toDateFilter;

    // for jobs
    private boolean           scheduleJob;
    private String            sendReportToEmailIds;
    private SchedulerVO       schedulerVO;

    public Long getQueryContextId() {
        return queryContextId;
    }

    public void setQueryContextId(Long queryContextId) {
        this.queryContextId = queryContextId;
    }

    public Long[] getSelectItemIds() {
        return selectItemIds;
    }

    public void setSelectItemIds(Long[] selectItemIds) {
        this.selectItemIds = selectItemIds;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public Boolean getUseDistinct() {
        return useDistinct;
    }

    public void setUseDistinct(Boolean useDistinct) {
        this.useDistinct = useDistinct;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public Long getOrderByTokenId() {
        return orderByTokenId;
    }

    public void setOrderByTokenId(Long orderByTokenId) {
        this.orderByTokenId = orderByTokenId;
    }

    public Long getGroupByTokenId() {
        return groupByTokenId;
    }

    public void setGroupByTokenId(Long groupByTokenId) {
        this.groupByTokenId = groupByTokenId;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public Long[] getSumForTokenIds() {
        return sumForTokenIds;
    }

    public void setSumForTokenIds(Long[] sumForTokenIds) {
        this.sumForTokenIds = sumForTokenIds;
    }

    public Long[] getAvgForTokenIds() {
        return avgForTokenIds;
    }

    public void setAvgForTokenIds(Long[] avgForTokenIds) {
        this.avgForTokenIds = avgForTokenIds;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public Long[] getPercentageForTokenIds() {
        return percentageForTokenIds;
    }

    public void setPercentageForTokenIds(Long[] percentageForTokenIds) {
        this.percentageForTokenIds = percentageForTokenIds;
    }

    public Long[] getCountForTokenIds() {
        return countForTokenIds;
    }

    public void setCountForTokenIds(Long[] countForTokenIds) {
        this.countForTokenIds = countForTokenIds;
    }

    public boolean isSumAtSummary() {
        return sumAtSummary;
    }

    public void setSumAtSummary(boolean sumAtSummary) {
        this.sumAtSummary = sumAtSummary;
    }

    public boolean isSumAtGroup() {
        return sumAtGroup;
    }

    public void setSumAtGroup(boolean sumAtGroup) {
        this.sumAtGroup = sumAtGroup;
    }

    public boolean isAvgAtSummary() {
        return avgAtSummary;
    }

    public void setAvgAtSummary(boolean avgAtSummary) {
        this.avgAtSummary = avgAtSummary;
    }

    public boolean isAvgAtGroup() {
        return avgAtGroup;
    }

    public void setAvgAtGroup(boolean avgAtGroup) {
        this.avgAtGroup = avgAtGroup;
    }

    public boolean isPercentageAtSummary() {
        return percentageAtSummary;
    }

    public void setPercentageAtSummary(boolean percentageAtSummary) {
        this.percentageAtSummary = percentageAtSummary;
    }

    public boolean isPercentageAtGroup() {
        return percentageAtGroup;
    }

    public void setPercentageAtGroup(boolean percentageAtGroup) {
        this.percentageAtGroup = percentageAtGroup;
    }

    public boolean isCountAtSummary() {
        return countAtSummary;
    }

    public void setCountAtSummary(boolean countAtSummary) {
        this.countAtSummary = countAtSummary;
    }

    public boolean isCountAtGroup() {
        return countAtGroup;
    }

    public void setCountAtGroup(boolean countAtGroup) {
        this.countAtGroup = countAtGroup;
    }

    public Long getChartType() {
        return chartType;
    }

    public void setChartType(Long chartType) {
        this.chartType = chartType;
    }

    public String getChartTitle() {
        return chartTitle;
    }

    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
    }

    public Long getKeyTokenId() {
        return keyTokenId;
    }

    public void setKeyTokenId(Long keyTokenId) {
        this.keyTokenId = keyTokenId;
    }

    public Long[] getSeriesTokenIds() {
        return seriesTokenIds;
    }

    public void setSeriesTokenIds(Long[] seriesTokenIds) {
        this.seriesTokenIds = seriesTokenIds;
    }

    public boolean isDynamicFormReport() {
        return dynamicFormReport;
    }

    public void setDynamicFormReport(boolean dynamicFormReport) {
        this.dynamicFormReport = dynamicFormReport;
    }

    public Long getDynamicFormId() {
        return dynamicFormId;
    }

    public void setDynamicFormId(Long dynamicFormId) {
        this.dynamicFormId = dynamicFormId;
    }

    public DateTime getFromDateFilter() {
        return fromDateFilter;
    }

    public void setFromDateFilter(DateTime fromDateFilter) {
        this.fromDateFilter = fromDateFilter;
    }

    public DateTime getToDateFilter() {
        return toDateFilter;
    }

    public void setToDateFilter(DateTime toDateFilter) {
        this.toDateFilter = toDateFilter;
    }

    public boolean isScheduleJob() {
        return scheduleJob;
    }

    public void setScheduleJob(boolean scheduleJob) {
        this.scheduleJob = scheduleJob;
    }

    public String getSendReportToEmailIds() {
        return sendReportToEmailIds;
    }

    public void setSendReportToEmailIds(String sendReportToEmailIds) {
        this.sendReportToEmailIds = sendReportToEmailIds;
    }

    public SchedulerVO getSchedulerVO() {
        return schedulerVO;
    }

    public void setSchedulerVO(SchedulerVO schedulerVO) {
        this.schedulerVO = schedulerVO;
    }

}
