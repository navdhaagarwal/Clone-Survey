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

import static net.sf.dynamicreports.report.builder.DynamicReports.cht;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import net.sf.dynamicreports.report.builder.chart.Bar3DChartBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.datatype.BigDecimalType;
import net.sf.dynamicreports.report.exception.DRException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import antlr.TokenStreamException;

import com.nucleus.core.dynamicQuery.entity.QueryContext;
import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.dynamicQuery.service.DynamicFormDataQueryService;
import com.nucleus.core.dynamicQuery.service.DynamicQueryMetadataService;
import com.nucleus.core.dynamicQuery.service.DynamicQueryTranslatorService;
import com.nucleus.core.dynamicQuery.support.DynamicQuery;
import com.nucleus.core.dynamicQuery.support.DynamicQueryWrapper;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.formsConfiguration.FieldDataType;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.core.json.util.JsonUtils;
import com.nucleus.core.money.entity.Money;
import com.nucleus.core.scheduler.service.SchedulerParam;
import com.nucleus.core.scheduler.service.SchedulerService;
import com.nucleus.core.scheduler.service.SchedulerVO;
import com.nucleus.core.web.dynamicReport.DynamicReportBuilder;
import com.nucleus.core.web.dynamicReport.DynamicReportConfig;
import com.nucleus.core.web.dynamicReport.DynamicReportJob;
import com.nucleus.core.web.dynamicReport.DynamicReportPojo;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONSerializer;

/**
 * @author Nucleus Software Exports Limited
 *
 */
@Controller
@RequestMapping(value = "/dynamicQuery")
public class DynamicQueryController extends BaseController {

    @Inject
    @Named(value = "entityDao")
    EntityDao                     entityDao;

    @Inject
    @Named(value = "dynamicQueryTranslatorService")
    DynamicQueryTranslatorService queryTranslatorService;

    @Inject
    @Named(value = "dynamicQueryMetadataService")
    DynamicQueryMetadataService   queryMetadataService;

    @Inject
    @Named(value = "dynamicReportBuilder")
    DynamicReportBuilder          dynamicReportBuilder;

    @Inject
    @Named("dynamicFormDataQueryService")
    DynamicFormDataQueryService   dynamicFormDataQueryService;

    @Inject
    @Named("schedulerService")
    SchedulerService              schedulerService;

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/showPage")
    public String displayMainPage(ModelMap map) {
        // for icici
        List<QueryContext> contexts = entityDao.findAll(QueryContext.class);
        List<QueryContext> contextsToDisplay = new ArrayList<QueryContext>();
        for (QueryContext queryContext : contexts) {
            QueryContext context = new QueryContext();
            context.setId(queryContext.getId());
            if (queryContext.getQueryCode().equalsIgnoreCase("LOAN_APPLICATION")) {
                context.setQueryCode("CREDIT_APPLICATION");
            } else {
                context.setQueryCode(queryContext.getQueryCode());
            }
            contextsToDisplay.add(context);
        }

        map.put("queryContextList", contextsToDisplay);
        map.put("selectItemList", new HashMap<String, Object>());
        map.put("maxResultsList", Arrays.asList(1, 5, 10, 15, 20, 50, 100));// not used currently
        map.put("exportTypeList", dynamicReportBuilder.getReportExportTypes());
        map.put("chartTypeList", dynamicReportBuilder.getChartTypes());

        DynamicQueryCriteriaPojo criteriaPojo = new DynamicQueryCriteriaPojo();
        map.put("queryCriteriaPojo", criteriaPojo);

        map.put("dynamicForms", queryMetadataService.getAllDynamicFormIdNameVersionMap());

        // for job scheduling
        map.put("hourOfDayList", SchedulerParam.JOB_HOURS_OF_DAY);
        map.put("dateOfMonthList", SchedulerParam.JOB_DATEOFMONTH);
        map.put("jobFrequencyList", SchedulerParam.JOB_FREQUENCY);
        map.put("jobStatusList", SchedulerParam.JOB_STATUS);
        map.put("jobDaysOfWeekList", SchedulerParam.JOB_DAYSOFWEEK);

        criteriaPojo.setSchedulerVO(new SchedulerVO());
        criteriaPojo.getSchedulerVO().setJobGroup(SchedulerParam.DYNAMIC_REPORT_JOB_GROUP);

        if (getUserDetails() != null && getUserDetails().getUserReference() != null
                && getUserDetails().getUserReference().getMailId() != null) {
            criteriaPojo.setSendReportToEmailIds(getUserDetails().getUserReference().getMailId());
        }

        return "queryBuilderMainPage";
    }

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/contentAssist/{queryContextId}")
    @ResponseBody
    public Set<String> contentAssist(ModelMap map, @RequestParam("queryText") String queryText,
            @PathVariable("queryContextId") Long queryContextId, @RequestParam("cursorPosition") int cursorPosition,
            @RequestParam("queryTerm") String queryTerm) throws TokenStreamException {

        String part = queryMetadataService.getBaseQuery();
        queryText = part.concat(queryText==null?"":queryText);
        List<String> proposals = queryMetadataService.getAutocompleteProposals(queryText, queryContextId, queryTerm);
        return proposals != null ? new HashSet<String>(proposals) : new HashSet<String>();
    }

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/showHQL")
    @ResponseBody
    public DynamicQueryWrapper showHQL(ModelMap map, DynamicQueryCriteriaPojo queryCriteriaPojo) throws TokenStreamException {

        DynamicQueryWrapper queryWrapper = processQuery(queryCriteriaPojo);
        return queryWrapper;
    }

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/showResults")
    @ResponseBody
    public String showResults(ModelMap map, DynamicQueryCriteriaPojo queryCriteriaPojo) throws TokenStreamException {

        DynamicQueryWrapper queryWrapper = processQuery(queryCriteriaPojo);
        if (queryWrapper != null) {
            List<Map<String, Object>> list = entityDao.executeQuery(queryWrapper.getMapQueryExecuterWithAllParameterAdded(),
                    0, queryCriteriaPojo.getMaxResults());
            if (list != null) {
                return JsonUtils.serializeWithLazyInitialization(list);
            }
        }
        return "";
    }

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/getSelectionList/{contextId}/{dynamicFormReport}")
    @ResponseBody
    public List<Map<Long, String>> getSelectionList(ModelMap map, @PathVariable("contextId") Long contextId,
            @PathVariable("dynamicFormReport") boolean dynamicFormReport) throws TokenStreamException {

        final Map<Long, String> allTokenMap = new HashMap<Long, String>();
        final Map<Long, String> numericTokenMap = new HashMap<Long, String>();

        if (dynamicFormReport) {
            List<FieldDefinition> fieldDefinitions = queryMetadataService.getDynamicFormFieldsByFormId(contextId);

            if (fieldDefinitions != null && !fieldDefinitions.isEmpty()) {

                for (FieldDefinition fieldDefinition : fieldDefinitions) {
                    // all tokens can be used for grouping/display
                    allTokenMap.put(fieldDefinition.getId(), fieldDefinition.getFieldKey());

                    // only token which are of numeric type are added here for sum/avg etc
                    if (fieldDefinition.getFieldDataType() == FieldDataType.DATA_TYPE_INTEGER
                            || fieldDefinition.getFieldDataType() == FieldDataType.DATA_TYPE_NUMBER
                            || fieldDefinition.getFieldDataType() == FieldDataType.DATA_TYPE_MONEY) {
                        numericTokenMap.put(fieldDefinition.getId(), fieldDefinition.getFieldKey());
                    }

                }
            }

        } else {
            List<QueryToken> queryTokens = queryMetadataService.getAllTokensWithContextIdAndType(contextId,
                    Arrays.asList(QueryToken.SELECT_TYPE, QueryToken.BOTH));
            if (queryTokens != null && !queryTokens.isEmpty()) {

                for (QueryToken queryToken : queryTokens) {
                    if (queryToken.isToken()) {

                        // all tokens can be used for grouping/display
                        allTokenMap.put(queryToken.getId(), queryToken.getTokenName());

                        // only token which are of numeric type are added here for sum/avg etc
                        if ((queryToken.getValueActualType().equalsIgnoreCase(QueryToken.NUMBER)
                                || queryToken.getValueActualType().equalsIgnoreCase(QueryToken.FLOAT) || queryToken
                                .getValueActualType().equalsIgnoreCase(QueryToken.MONEY))
                                && queryToken.getValueDisplayType().equals(queryToken.getValueActualType())) {
                            numericTokenMap.put(queryToken.getId(), queryToken.getTokenName());
                        }
                    }

                }
            }
        }
        return new ArrayList<Map<Long, String>>() {
            private static final long serialVersionUID = 1L;
            {
                add(allTokenMap);
                add(numericTokenMap);
            }
        };
    }

    // ~~===============================for reporting

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/generateReport")
    public @ResponseBody
    HttpEntity<byte[]> generateDynamicReport(ModelMap map, DynamicQueryCriteriaPojo queryCriteriaPojo) throws IOException,
            DRException {

        removeNullFromArray(queryCriteriaPojo);
        // first of all create HQL from dynamic neutrino query
        DynamicQueryWrapper queryWrapper = processQuery(queryCriteriaPojo);

        BaseLoggers.flowLogger.info("Processed dynamic report query and generated hql --> [{}]",
                queryWrapper.getHqlQueryString());

        DynamicReportConfig dynamicReportConfig = null;
        if (queryWrapper != null) {
            List<Map<String, Object>> list = entityDao.executeQuery(queryWrapper.getMapQueryExecuterWithAllParameterAdded());
            if (list != null) {
                BaseLoggers.flowLogger.info("Found {} results for dynamic report query.Now generating report {} file.",
                        list.size(), queryCriteriaPojo.getReportTitle());
                // create DynamicReportConfig
                dynamicReportConfig = new DynamicReportConfig(list, queryWrapper.getSelectedTokens());
                populateCommonFieldsInDynamicReportConfig(queryCriteriaPojo, dynamicReportConfig);

            }
        }

        DynamicReportPojo dynamicReportPojo = dynamicReportBuilder.generateReport(dynamicReportConfig);

        MediaType mediaType = MediaType.parseMediaType(dynamicReportPojo.getMediaType());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(mediaType);
        responseHeaders.setContentDispositionFormData("attachment", dynamicReportPojo.getFileName());
        HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(dynamicReportPojo.getReportData(), responseHeaders);
        return fileEntity;
    }

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/scheduleReportJob")
    public @ResponseBody
    String scheduleDynamicReportJob(ModelMap map, DynamicQueryCriteriaPojo queryCriteriaPojo) throws IOException,
            DRException {
        removeNullFromArray(queryCriteriaPojo);
        // first of all create HQL from dynamic neutrino query
        DynamicQueryWrapper queryWrapper = processQuery(queryCriteriaPojo);

        BaseLoggers.flowLogger.info("Processed dynamic report job and generated hql --> [{}]",
                queryWrapper.getHqlQueryString());

        DynamicReportConfig dynamicReportConfig = null;
        if (queryWrapper != null) {

            // test generated query before scheduling job so errors are detected early.
            List<Map<String, Object>> list = entityDao.executeQuery(queryWrapper.getMapQueryExecuterWithAllParameterAdded(),
                    0, 1);
            // create DynamicReportConfig
            dynamicReportConfig = new DynamicReportConfig(queryWrapper.getHqlMapQueryString(),
                    queryWrapper.getHqlQueryParametersUnresolved(), queryWrapper.getSelectedTokens());
            populateCommonFieldsInDynamicReportConfig(queryCriteriaPojo, dynamicReportConfig);

            // add a job now
            Map<String, Object> jobContextMap = new HashMap<String, Object>();
            jobContextMap.put(DynamicReportJob.JOB_PARAM_DYNAMIC_REPORT_CONFIG, dynamicReportConfig);
            try {
                BaseLoggers.flowLogger.info(
                        "Validated and tested HQL for dynamic report {} job.Now creating a Job to schedule.",
                        queryCriteriaPojo.getReportTitle());
                schedulerService.addJob(queryCriteriaPojo.getSchedulerVO(), DynamicReportJob.class, jobContextMap);
            } catch (SchedulerException e) {
                throw new SystemException("Error in scheduling dynamic report job", e);
            } catch (ParseException e) {
                throw new SystemException("Error in scheduling dynamic report job", e);
            }
        }

        return "done";
    }

    // ~~===============================for reporting-->Dynamic forms
    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/scheduleReportForDynaForm")
    public @ResponseBody
    String scheduleReportForDynamicForm(ModelMap map, DynamicQueryCriteriaPojo queryCriteriaPojo) throws IOException,
            DRException {
        removeNullFromArray(queryCriteriaPojo);
        Set<Long> projectionFields = getProjectionFields(queryCriteriaPojo);
        // create DynamicReportConfig
        DynamicReportConfig dynamicReportConfig = new DynamicReportConfig(queryCriteriaPojo.getDynamicFormId(),
                queryCriteriaPojo.getFromDateFilter(), queryCriteriaPojo.getToDateFilter(),
                queryMetadataService.getDynamicFormFieldsByIds(projectionFields), queryCriteriaPojo.getGroupByTokenId());
        // for dynamic reports we filter only on dates for now
        queryCriteriaPojo.setWhereClause(String.format("From date %s to date %s", queryCriteriaPojo.getFromDateFilter(),
                queryCriteriaPojo.getToDateFilter()));
        populateCommonFieldsInDynamicReportConfig(queryCriteriaPojo, dynamicReportConfig);

        // add a job now
        Map<String, Object> jobContextMap = new HashMap<String, Object>();
        jobContextMap.put(DynamicReportJob.JOB_PARAM_DYNAMIC_REPORT_CONFIG, dynamicReportConfig);
        try {
            BaseLoggers.flowLogger.info("Creating a Job for dynamic form based report with data from {} to {}.",
                    queryCriteriaPojo.getFromDateFilter(), queryCriteriaPojo.getToDateFilter());
            schedulerService.addJob(queryCriteriaPojo.getSchedulerVO(), DynamicReportJob.class, jobContextMap);
        } catch (SchedulerException e) {
            throw new SystemException("Error in scheduling dynamic report job", e);
        } catch (ParseException e) {
            throw new SystemException("Error in scheduling dynamic report job", e);
        }

        return "done";
    }

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/generateReportForDynaForm")
    public @ResponseBody
    HttpEntity<byte[]> generateDynamicReportForDynamicForm(ModelMap map, DynamicQueryCriteriaPojo queryCriteriaPojo)
            throws IOException, DRException {
        removeNullFromArray(queryCriteriaPojo);
        Set<Long> projectionFields = getProjectionFields(queryCriteriaPojo);

        // first of all fetch data for dynamic form
        List<Map<String, Object>> list = dynamicFormDataQueryService.getFormDataByDate(queryCriteriaPojo.getDynamicFormId(),
                queryCriteriaPojo.getFromDateFilter(), queryCriteriaPojo.getToDateFilter(), projectionFields,
                queryCriteriaPojo.getGroupByTokenId());

        BaseLoggers.flowLogger.info(
                "Found {} results for dynamic form based report and from {} to {}.Now generating report file.", list.size(),
                queryCriteriaPojo.getFromDateFilter(), queryCriteriaPojo.getToDateFilter());

        DynamicReportConfig dynamicReportConfig = null;
        // create DynamicReportConfig
        dynamicReportConfig = new DynamicReportConfig(list,
                queryMetadataService.getDynamicFormFieldsByIds(projectionFields), true);
        // for dynamic reports we filter only on dates for now
        queryCriteriaPojo.setWhereClause(String.format("From date %s to date %s", queryCriteriaPojo.getFromDateFilter(),
                queryCriteriaPojo.getToDateFilter()));
        populateCommonFieldsInDynamicReportConfig(queryCriteriaPojo, dynamicReportConfig);

        DynamicReportPojo dynamicReportPojo = dynamicReportBuilder.generateReport(dynamicReportConfig);

        MediaType mediaType = MediaType.parseMediaType(dynamicReportPojo.getMediaType());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(mediaType);
        responseHeaders.setContentDispositionFormData("attachment", dynamicReportPojo.getFileName());
        HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(dynamicReportPojo.getReportData(), responseHeaders);
        return fileEntity;
    }

    // =====================

    private void removeNullFromArray(DynamicQueryCriteriaPojo queryCriteriaPojo) {
        queryCriteriaPojo.setSelectItemIds(ArrayUtils.removeElement(queryCriteriaPojo.getSelectItemIds(), null));
        queryCriteriaPojo.setSumForTokenIds(ArrayUtils.removeElement(queryCriteriaPojo.getSumForTokenIds(), null));
        queryCriteriaPojo.setAvgForTokenIds(ArrayUtils.removeElement(queryCriteriaPojo.getAvgForTokenIds(), null));
        queryCriteriaPojo.setPercentageForTokenIds(ArrayUtils.removeElement(queryCriteriaPojo.getPercentageForTokenIds(),
                null));
        queryCriteriaPojo.setCountForTokenIds(ArrayUtils.removeElement(queryCriteriaPojo.getCountForTokenIds(), null));
    }

    private DynamicQueryWrapper processQuery(DynamicQueryCriteriaPojo queryCriteriaPojo) {

        if (StringUtils.isNoneBlank(queryCriteriaPojo.getWhereClause())) {
            QueryContext queryContext = entityDao.find(QueryContext.class, queryCriteriaPojo.getQueryContextId());

            Set<Long> consolidatedIds = new HashSet<Long>();
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getSelectItemIds());
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getSumForTokenIds());
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getAvgForTokenIds());
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getPercentageForTokenIds());
            Collections.addAll(consolidatedIds, queryCriteriaPojo.getCountForTokenIds());
            // if grouping is not required
            if (queryCriteriaPojo.getGroupByTokenId() != null) {
                Collections.addAll(consolidatedIds, (queryCriteriaPojo.getGroupByTokenId()));
            }

            // for chart config
            /* if (queryCriteriaPojo.getKeyTokenId() != null) {
                 Collections.addAll(consolidatedIds, (queryCriteriaPojo.getKeyTokenId()));
             }
             Collections.addAll(consolidatedIds, (queryCriteriaPojo.getSeriesTokenIds()));*/

            return queryTranslatorService.processQuery(queryCriteriaPojo.getWhereClause(), queryContext,
                    new ArrayList<Long>(consolidatedIds), false);
        }
        return null;

    }

    private void populateCommonFieldsInDynamicReportConfig(DynamicQueryCriteriaPojo queryCriteriaPojo,
            DynamicReportConfig dynamicReportConfig) {
        dynamicReportConfig.setReportTitle(queryCriteriaPojo.getReportTitle());
        dynamicReportConfig.setExportType(queryCriteriaPojo.getExportType());
        dynamicReportConfig.setGroupByTokenName(queryCriteriaPojo.getGroupByTokenId());
        dynamicReportConfig.setSumForTokenNames(queryCriteriaPojo.getSumForTokenIds());
        dynamicReportConfig.setAvgForTokenNames(queryCriteriaPojo.getAvgForTokenIds());
        dynamicReportConfig.setPercentageForTokenNames(queryCriteriaPojo.getPercentageForTokenIds());
        dynamicReportConfig.setCountForTokenNames(queryCriteriaPojo.getCountForTokenIds());
        dynamicReportConfig.setSumAtGroup(queryCriteriaPojo.isSumAtGroup());
        dynamicReportConfig.setSumAtSummary(queryCriteriaPojo.isSumAtSummary());
        dynamicReportConfig.setAvgAtGroup(queryCriteriaPojo.isAvgAtGroup());
        dynamicReportConfig.setAvgAtSummary(queryCriteriaPojo.isAvgAtSummary());
        dynamicReportConfig.setPercentageAtGroup(queryCriteriaPojo.isPercentageAtGroup());
        dynamicReportConfig.setPercentageAtSummary(queryCriteriaPojo.isPercentageAtSummary());
        dynamicReportConfig.setCountAtGroup(queryCriteriaPojo.isCountAtGroup());
        dynamicReportConfig.setCountAtSummary(queryCriteriaPojo.isCountAtSummary());

        dynamicReportConfig.setChartTitle(queryCriteriaPojo.getChartTitle());
        dynamicReportConfig.setChartKeyTokenName(queryCriteriaPojo.getKeyTokenId());
        dynamicReportConfig.setChartType(queryCriteriaPojo.getChartType());
        dynamicReportConfig.setChartSeriesTokenNames(queryCriteriaPojo.getSeriesTokenIds());

        dynamicReportConfig.setDynamicQueryWhereClause(queryCriteriaPojo.getWhereClause());

        // for job scheduling
        dynamicReportConfig.setSendReportToEmailIds(queryCriteriaPojo.getSendReportToEmailIds());
    }

    private Set<Long> getProjectionFields(DynamicQueryCriteriaPojo queryCriteriaPojo) {

        Set<Long> consolidatedIds = new HashSet<Long>();
        Collections.addAll(consolidatedIds, (ArrayUtils.removeElement(queryCriteriaPojo.getSelectItemIds(), null)));
        Collections.addAll(consolidatedIds, (ArrayUtils.removeElement(queryCriteriaPojo.getSumForTokenIds(), null)));
        Collections.addAll(consolidatedIds, (ArrayUtils.removeElement(queryCriteriaPojo.getAvgForTokenIds(), null)));
        Collections.addAll(consolidatedIds, (ArrayUtils.removeElement(queryCriteriaPojo.getPercentageForTokenIds(), null)));
        Collections.addAll(consolidatedIds, (ArrayUtils.removeElement(queryCriteriaPojo.getCountForTokenIds(), null)));
        // if grouping is not required
        if (queryCriteriaPojo.getGroupByTokenId() != null) {
            Collections.addAll(consolidatedIds, (queryCriteriaPojo.getGroupByTokenId()));
        }

        // for chart config
        /* if (queryCriteriaPojo.getKeyTokenId() != null) {
             Collections.addAll(consolidatedIds, (queryCriteriaPojo.getKeyTokenId()));
         }
         Collections.addAll(consolidatedIds, (queryCriteriaPojo.getSeriesTokenIds()));*/
        return consolidatedIds;

    }

    private Bar3DChartBuilder buildChart() {

        TextColumnBuilder<BigDecimal> bigDecimalCol = col.column("Loan_Amount", "Loan_Amount", new CurrencyType());

        Bar3DChartBuilder itemChart2 = cht.bar3DChart().setTitle("Sample Chart Report")
                .setCategory(col.column("Branch", "Branch", type.stringType())).setUseSeriesAsCategory(true)
                .addSerie(cht.serie(bigDecimalCol), cht.serie(bigDecimalCol))
                .setCategoryAxisFormat(cht.axisFormat().setLabel("Branch"));

        return itemChart2;

    }

    private static class CurrencyType extends BigDecimalType {
        private static final long serialVersionUID = 1L;

        @Override
        public String getPattern() {
            return Money.getBaseCurrency().getCurrencyCode().concat(" #,##0.00");
        }
    }
    
    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/saveQuery")
    public @ResponseBody
    String displayGraph(ModelMap map, DynamicQueryCriteriaPojo queryCriteriaPojo) throws TokenStreamException {

        removeNullFromArray(queryCriteriaPojo);
        // first of all create HQL from dynamic neutrino query
        DynamicQueryWrapper queryWrapper = processQuery(queryCriteriaPojo);

        BaseLoggers.flowLogger.info("Processed dynamic report job and generated hql --> [{}]",
                queryWrapper.getHqlQueryString());

        if (queryWrapper != null) {

            // test generated query before scheduling job so errors are detected early.
            List<Map<String, Object>> list = entityDao.executeQuery(queryWrapper.getMapQueryExecuterWithAllParameterAdded(),
                    0, 1);

        }

        DynamicQuery saveQuery = populateDynamicQuery(new DynamicQuery(), queryWrapper, queryCriteriaPojo);
        entityDao.persist(saveQuery);
        return saveQuery.getHqlMapQueryString();
    }

    private DynamicQuery populateDynamicQuery(DynamicQuery saveQuery, DynamicQueryWrapper queryWrapper,
            DynamicQueryCriteriaPojo queryCriteriaPojo) {

        JSONSerializer iSerializer = new JSONSerializer();
        saveQuery.setHqlMapQueryString(queryWrapper.getHqlMapQueryString());
        saveQuery.setUserSavedQuery(queryCriteriaPojo.getWhereClause());
        saveQuery.setReportTitle(queryCriteriaPojo.getReportTitle());
        Set<Long> selectedTokenSet = queryWrapper.getSelectedTokens().keySet();
        saveQuery.setSelectedTokens(JsonUtils.serializeWithLazyInitialization(StringUtils.join(selectedTokenSet, ",")));
        saveQuery.setAvgAtGroup(queryCriteriaPojo.isAvgAtGroup());
        saveQuery.setAvgAtSummary(queryCriteriaPojo.isAvgAtSummary());
        saveQuery.setAvgForTokenIds(StringUtils.join(queryCriteriaPojo.getAvgForTokenIds(), ","));
        saveQuery.setChartType(queryCriteriaPojo.getChartType());
        saveQuery.setCountAtGroup(queryCriteriaPojo.isCountAtGroup());
        saveQuery.setCountAtSummary(queryCriteriaPojo.isCountAtSummary());
        saveQuery.setCountForTokenIds(StringUtils.join(queryCriteriaPojo.getCountForTokenIds(), ","));
        saveQuery.setExportType(queryCriteriaPojo.getExportType());
        saveQuery.setGroupByTokenId(queryCriteriaPojo.getGroupByTokenId());
        String unresolvedParameter = iSerializer.exclude("*.class").deepSerialize(
                queryWrapper.getHqlQueryParametersUnresolved());
        saveQuery.setHqlQueryParametersUnresolved(unresolvedParameter);
        saveQuery.setPercentageAtGroup(queryCriteriaPojo.isPercentageAtGroup());
        saveQuery.setPercentageAtSummary(queryCriteriaPojo.isPercentageAtSummary());
        saveQuery.setPercentageForTokenIds(StringUtils.join(queryCriteriaPojo.getPercentageForTokenIds(), ","));
        saveQuery.setSelectItemIds(StringUtils.join(queryCriteriaPojo.getSelectItemIds(), ","));
        saveQuery.setSumAtGroup(queryCriteriaPojo.isSumAtGroup());
        saveQuery.setSumAtSummary(queryCriteriaPojo.isSumAtSummary());
        saveQuery.setSumForTokenIds(StringUtils.join(queryCriteriaPojo.getSumForTokenIds(), ","));

        return saveQuery;

    }
}
