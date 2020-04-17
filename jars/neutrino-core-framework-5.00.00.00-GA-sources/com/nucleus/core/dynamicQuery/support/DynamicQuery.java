package com.nucleus.core.dynamicQuery.support;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class DynamicQuery extends BaseEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    @Column(length = 2000)
    private String            hqlMapQueryString;
    private String            reportTitle;
    private String            userSavedQuery;
    private String            selectedTokens;
    private String            selectItemIds;
    // for running report as job we need to resolve date-time parameters later
    private String            hqlQueryParametersUnresolved;
    private Long              groupByTokenId;
    private String            exportType;
    private String            sumForTokenIds;
    private String            avgForTokenIds;

    private String            percentageForTokenIds;
    private String            countForTokenIds;

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


    public String getHqlMapQueryString() {
        return hqlMapQueryString;
    }

    public void setHqlMapQueryString(String hqlMapQueryString) {
        this.hqlMapQueryString = hqlMapQueryString;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getUserSavedQuery() {
        return userSavedQuery;
    }

    public void setUserSavedQuery(String userSavedQuery) {
        this.userSavedQuery = userSavedQuery;
    }

    public String getSelectedTokens() {
        return selectedTokens;
    }

    public void setSelectedTokens(String selectedTokens) {
        this.selectedTokens = selectedTokens;
    }

    public String getHqlQueryParametersUnresolved() {
        return hqlQueryParametersUnresolved;
    }

    public void setHqlQueryParametersUnresolved(String hqlQueryParametersUnresolved) {
        this.hqlQueryParametersUnresolved = hqlQueryParametersUnresolved;
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

    public String getSumForTokenIds() {
        return sumForTokenIds;
    }

    public void setSumForTokenIds(String sumForTokenIds) {
        this.sumForTokenIds = sumForTokenIds;
    }

    public String getAvgForTokenIds() {
        return avgForTokenIds;
    }

    public void setAvgForTokenIds(String avgForTokenIds) {
        this.avgForTokenIds = avgForTokenIds;
    }

    public String getPercentageForTokenIds() {
        return percentageForTokenIds;
    }

    public void setPercentageForTokenIds(String percentageForTokenIds) {
        this.percentageForTokenIds = percentageForTokenIds;
    }

    public String getCountForTokenIds() {
        return countForTokenIds;
    }

    public void setCountForTokenIds(String countForTokenIds) {
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

    public String getSelectItemIds() {
        return selectItemIds;
    }

    public void setSelectItemIds(String selectItemIds) {
        this.selectItemIds = selectItemIds;
    }

}
