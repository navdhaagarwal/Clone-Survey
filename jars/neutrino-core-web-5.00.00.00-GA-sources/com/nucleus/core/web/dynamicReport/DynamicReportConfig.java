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
package com.nucleus.core.web.dynamicReport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.dynamicQuery.entity.QueryTokenValue;
import com.nucleus.core.formsConfiguration.FieldDataType;

/**
 * @author Nucleus Software Exports Limited
 */
public class DynamicReportConfig implements Serializable {

    private static final long     serialVersionUID = 8113684796114882298L;

    // used internally
    private Map<Long, QueryToken> selectedTokens;
    private Map<Long, Object[]>   selectedTokensDynamicForm;
    private boolean               dynamicFormReport;

    // for hql based reports
    public DynamicReportConfig(List<Map<String, Object>> dataList, Map<Long, QueryToken> selectedTokens) {
        super();
        this.dataList = dataList;
        this.selectedTokens = selectedTokens;
    }

    // for dynamic form based reports
    public DynamicReportConfig(List<Map<String, Object>> dataList, Map<Long, Object[]> selectedTokensDynamicForm,
            boolean dynamicFormReport) {
        super();
        this.dataList = dataList;
        this.selectedTokensDynamicForm = selectedTokensDynamicForm;
        this.dynamicFormReport = dynamicFormReport;
    }

    // for dynamic reports job scheduling--->HQL
    public DynamicReportConfig(String hqlMapQueryString, Map<String, String> hqlQueryParametersUnresolved,
            Map<Long, QueryToken> selectedTokens) {
        super();
        this.hqlMapQueryString = hqlMapQueryString;
        this.hqlQueryParametersUnresolved = hqlQueryParametersUnresolved;
        this.selectedTokens = selectedTokens;
    }

    // for dynamic reports job scheduling--->Dynamic forms
    public DynamicReportConfig(Long dynamicFormId, DateTime dynamicFormFromDateFilter, DateTime dynamicFormToDateFilter,
            Map<Long, Object[]> selectedTokensDynamicForm, Long groupByTokenId) {
        super();
        this.dynamicFormId = dynamicFormId;
        this.dynamicFormFromDateFilter = dynamicFormFromDateFilter;
        this.dynamicFormToDateFilter = dynamicFormToDateFilter;
        this.selectedTokensDynamicForm = selectedTokensDynamicForm;
        this.dynamicFormReport = true;
        this.groupByTokenId = groupByTokenId;
    }

    private String                    reportTitle;
    private List<Map<String, Object>> dataList;

    private String                    groupByTokenName;
    private String                    exportType;

    private List<String>              sumForTokenNames             = new ArrayList<String>();
    private List<String>              avgForTokenNames             = new ArrayList<String>();
    private List<String>              percentageForTokenNames      = new ArrayList<String>();
    private List<String>              countForTokenNames           = new ArrayList<String>();

    private boolean                   sumAtSummary;
    private boolean                   sumAtGroup;
    private boolean                   avgAtSummary;
    private boolean                   avgAtGroup;
    private boolean                   percentageAtSummary;
    private boolean                   percentageAtGroup;
    private boolean                   countAtSummary;
    private boolean                   countAtGroup;

    // for charts
    private Long                      chartType;
    private String                    chartTitle;

    private String                    chartKeyTokenName;

    private List<String>              chartSeriesTokenNames        = new ArrayList<String>();

    // to be used for no result message
    private String                    dynamicQueryWhereClause;

    // to be used for job scheduling(will be used to get result list and send report to provided emailIds)
    private String                    sendReportToEmailIds;
    private String                    hqlMapQueryString;
    private Map<String, String>       hqlQueryParametersUnresolved = new HashMap<String, String>();

    // fields added for dynamic form based reports
    private Long                      dynamicFormId;
    private DateTime                  dynamicFormFromDateFilter;
    private DateTime                  dynamicFormToDateFilter;
    private Long                      groupByTokenId;

    public List<Map<String, Object>> getDataList() {
        return dataList;
    }

    private Map<Long, QueryToken> getSelectedTokens() {
        return selectedTokens;
    }

    public String getGroupByTokenName() {
        return groupByTokenName;
    }

    public String getExportType() {
        return exportType;
    }

    public List<String> getSumForTokenNames() {
        return sumForTokenNames;
    }

    public List<String> getAvgForTokenNames() {
        return avgForTokenNames;
    }

    public void setGroupByTokenName(Long tokenId) {
        if (tokenId != null) {
            this.groupByTokenName = getTokenNameById(tokenId);
        }
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public void setSumForTokenNames(Long[] sumForTokenIds) {

        if (sumForTokenIds != null) {
            int len = sumForTokenIds.length;
            for (int i = 0 ; i < len ; i++) {
                sumForTokenNames.add(getTokenNameById(sumForTokenIds[i]));
            }
        }
    }

    public void setAvgForTokenNames(Long[] avgForTokenIds) {

        if (avgForTokenIds != null) {
            int len = avgForTokenIds.length;
            for (int i = 0 ; i < len ; i++) {
                avgForTokenNames.add(getTokenNameById(avgForTokenIds[i]));
            }
        }
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public boolean isCurrencyType(String tokenName) {

        if (dynamicFormReport) {
            for (Object[] qt : selectedTokensDynamicForm.values()) {
                if (tokenName.equalsIgnoreCase((String) qt[1])) {
                    return (((Integer) qt[2]) == FieldDataType.DATA_TYPE_MONEY);
                }
            }
        } else {

            for (QueryToken qt : selectedTokens.values()) {
                if (qt.getTokenName().equals(tokenName)) {
                    return qt.getValueActualType().equalsIgnoreCase(QueryToken.MONEY);
                }
            }
        }
        return false;
    }

    public String getDynamicQueryWhereClause() {
        return dynamicQueryWhereClause;
    }

    public void setDynamicQueryWhereClause(String dynamicQueryWhereClause) {
        this.dynamicQueryWhereClause = dynamicQueryWhereClause;
    }

    public List<String> getPercentageForTokenNames() {
        return percentageForTokenNames;
    }

    public List<String> getCountForTokenNames() {
        return countForTokenNames;
    }

    public boolean isSumAtSummary() {
        return sumAtSummary;
    }

    public boolean isSumAtGroup() {
        return sumAtGroup;
    }

    public boolean isAvgAtSummary() {
        return avgAtSummary;
    }

    public boolean isAvgAtGroup() {
        return avgAtGroup;
    }

    public boolean isPercentageAtSummary() {
        return percentageAtSummary;
    }

    public boolean isPercentageAtGroup() {
        return percentageAtGroup;
    }

    public boolean isCountAtSummary() {
        return countAtSummary;
    }

    public boolean isCountAtGroup() {
        return countAtGroup;
    }

    public Long getChartType() {
        return chartType;
    }

    public String getChartTitle() {
        return chartTitle;
    }

    public String getChartKeyTokenName() {
        return chartKeyTokenName;
    }

    public List<String> getChartSeriesTokenNames() {
        return chartSeriesTokenNames;
    }

    public void setDataList(List<Map<String, Object>> dataList) {
        this.dataList = dataList;
    }

    public void setGroupByTokenName(String groupByTokenName) {
        this.groupByTokenName = groupByTokenName;
    }

    public void setSumForTokenNames(List<String> sumForTokenNames) {
        this.sumForTokenNames = sumForTokenNames;
    }

    public void setAvgForTokenNames(List<String> avgForTokenNames) {
        this.avgForTokenNames = avgForTokenNames;
    }

    public void setPercentageForTokenNames(Long[] percentageForTokenIds) {
        if (percentageForTokenIds != null) {
            int len = percentageForTokenIds.length;
            for (int i = 0 ; i < len ; i++) {
                percentageForTokenNames.add(getTokenNameById(percentageForTokenIds[i]));
            }
        }
    }

    public void setCountForTokenNames(Long[] countForTokenIds) {
        if (countForTokenIds != null) {
            int len = countForTokenIds.length;
            for (int i = 0 ; i < len ; i++) {
                countForTokenNames.add(getTokenNameById(countForTokenIds[i]));
            }
        }
    }

    public void setSumAtSummary(boolean sumAtSummary) {
        this.sumAtSummary = sumAtSummary;
    }

    public void setSumAtGroup(boolean sumAtGroup) {
        this.sumAtGroup = sumAtGroup;
    }

    public void setAvgAtSummary(boolean avgAtSummary) {
        this.avgAtSummary = avgAtSummary;
    }

    public void setAvgAtGroup(boolean avgAtGroup) {
        this.avgAtGroup = avgAtGroup;
    }

    public void setPercentageAtSummary(boolean percentageAtSummary) {
        this.percentageAtSummary = percentageAtSummary;
    }

    public void setPercentageAtGroup(boolean percentageAtGroup) {
        this.percentageAtGroup = percentageAtGroup;
    }

    public void setCountAtSummary(boolean countAtSummary) {
        this.countAtSummary = countAtSummary;
    }

    public void setCountAtGroup(boolean countAtGroup) {
        this.countAtGroup = countAtGroup;
    }

    public void setChartType(Long chartType) {
        this.chartType = chartType;
    }

    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
    }

    public void setChartKeyTokenName(Long chartKeyTokenId) {
        if (chartKeyTokenId != null) {
            this.chartKeyTokenName = getTokenNameById(chartKeyTokenId);
        }
    }

    public void setChartSeriesTokenNames(Long[] chartSeriesTokenIds) {

        if (chartSeriesTokenIds != null) {
            int len = chartSeriesTokenIds.length;
            for (int i = 0 ; i < len ; i++) {
                chartSeriesTokenNames.add(getTokenNameById(chartSeriesTokenIds[i]));
            }
        }
    }

    private String getTokenNameById(Long id) {

        if (dynamicFormReport) {
            return (String) selectedTokensDynamicForm.get(id)[1];
        } else {
            return getSelectedTokens().get(id).getTokenName();
        }

    }

    public String getSendReportToEmailIds() {
        return sendReportToEmailIds;
    }

    public void setSendReportToEmailIds(String sendReportToEmailIds) {
        this.sendReportToEmailIds = sendReportToEmailIds;
    }

    public String getHqlMapQueryString() {
        return hqlMapQueryString;
    }

    public Map<String, String> getHqlQueryParametersUnresolved() {
        return hqlQueryParametersUnresolved;
    }

    public Long getDynamicFormId() {
        return dynamicFormId;
    }

    public DateTime getDynamicFormFromDateFilter() {
        return dynamicFormFromDateFilter;
    }

    public DateTime getDynamicFormToDateFilter() {
        return dynamicFormToDateFilter;
    }

    public boolean isDynamicFormReport() {
        return dynamicFormReport;
    }

    public Long getGroupByTokenId() {
        return groupByTokenId;
    }

    // for job scheduling
    public Set<Long> getSelectedTokenIdsForDynamicForm() {

        if (selectedTokensDynamicForm != null) {
            return new HashSet<Long>(selectedTokensDynamicForm.keySet());
        } else {
            return new HashSet<Long>();
        }

    }

    public List<QueryTokenValue> getTokenValuesIfAnyForSelectedQueryToken(String queryTokenName) {

        List<QueryTokenValue> tokenValues = new ArrayList<QueryTokenValue>();

        if (dynamicFormReport) {
            // for dynamic reports--NOOP
        } else {
            for (QueryToken qt : selectedTokens.values()) {
                if (qt.getTokenName().equals(queryTokenName) && qt.getQueryTokenValues() != null) {
                    tokenValues.addAll(qt.getQueryTokenValues());
                    break;
                }
            }
        }
        return tokenValues;
    }
}
