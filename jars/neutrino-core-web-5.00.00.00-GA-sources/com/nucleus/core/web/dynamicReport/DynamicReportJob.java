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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.core.io.ByteArrayResource;

import com.nucleus.cfi.mail.service.MailMessageIntegrationService;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.dynamicQuery.service.DynamicFormDataQueryService;
import com.nucleus.core.dynamicQuery.service.DynamicQueryMetadataService;
import com.nucleus.core.scheduler.NeutrinoJob;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.persistence.EntityDao;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public class DynamicReportJob extends NeutrinoJob {

    public static final String  JOB_PARAM_DYNAMIC_REPORT_CONFIG = "dynamicReportConfig";

    private DynamicReportConfig dynamicReportConfig;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        BaseLoggers.flowLogger.info("Executing dynamic report job with title [{}]", dynamicReportConfig.getReportTitle());

        DynamicQueryMetadataService metadataService = NeutrinoSpringAppContextUtil.getBeanByName(
                "dynamicQueryMetadataService", DynamicQueryMetadataService.class);
        EntityDao entityDao = NeutrinoSpringAppContextUtil.getBeanByName("entityDao", EntityDao.class);

        DynamicFormDataQueryService dynamicFormDataQueryService = NeutrinoSpringAppContextUtil.getBeanByName(
                "dynamicFormDataQueryService", DynamicFormDataQueryService.class);

        if (dynamicReportConfig.isDynamicFormReport()) {

            // first of all fetch data for dynamic form
            List<Map<String, Object>> list = dynamicFormDataQueryService.getFormDataByDate(
                    dynamicReportConfig.getDynamicFormId(), dynamicReportConfig.getDynamicFormFromDateFilter(),
                    dynamicReportConfig.getDynamicFormToDateFilter(),
                    dynamicReportConfig.getSelectedTokenIdsForDynamicForm(), dynamicReportConfig.getGroupByTokenId());

            doGenerateAndSendReportByMail(list);

        } else {
            JPAQueryExecutor<Map<String, Object>> jpaQueryExecutor = new JPAQueryExecutor<Map<String, Object>>(
                    dynamicReportConfig.getHqlMapQueryString());
            // resolve date time parameters now
            if (dynamicReportConfig.getHqlQueryParametersUnresolved() != null
                    && !dynamicReportConfig.getHqlQueryParametersUnresolved().isEmpty()) {
                for (Entry<String, String> param : dynamicReportConfig.getHqlQueryParametersUnresolved().entrySet()) {
                    jpaQueryExecutor.addParameter(param.getKey(),
                            metadataService.getDateTimeValueForTokenValue(param.getValue()));
                }
            }
            doGenerateAndSendReportByMail(entityDao.executeQuery(jpaQueryExecutor));
        }

    }

    public DynamicReportConfig getDynamicReportConfig() {
        return dynamicReportConfig;
    }

    public void setDynamicReportConfig(DynamicReportConfig dynamicReportConfig) {
        this.dynamicReportConfig = dynamicReportConfig;
    }

    private void doGenerateAndSendReportByMail(List<Map<String, Object>> resultList) {

        DynamicReportBuilder dynamicReportBuilder = NeutrinoSpringAppContextUtil.getBeanByName("dynamicReportBuilder",
                DynamicReportBuilder.class);
        MailService mailService = NeutrinoSpringAppContextUtil.getBeanByName("mailService", MailService.class);

        MailMessageIntegrationService mailMessageIntegrationService = NeutrinoSpringAppContextUtil.getBeanByName(
                "mailMessageIntegrationService", MailMessageIntegrationService.class);

        if (resultList != null) {
            BaseLoggers.flowLogger.info("Found {} data records for query in dynamic report job with title [{}]",
                    resultList.size(), dynamicReportConfig.getReportTitle());
            // set result list to DynamicReportConfig
            dynamicReportConfig.setDataList(resultList);
            DynamicReportPojo dynamicReportPojo = dynamicReportBuilder.generateReport(dynamicReportConfig);
            String[] mailIdsToSendReport = StringUtils.split(dynamicReportConfig.getSendReportToEmailIds(), ",");
            byte[] resultedReport = dynamicReportPojo.getReportData();
            if (resultedReport != null) {
                BaseLoggers.flowLogger.info("Generated dynamic report {} and sending to mail ids --> [{}]",
                        dynamicReportPojo.getFileName(), dynamicReportConfig.getSendReportToEmailIds());
                MimeMessage mimeMessage = mailService
                        .createMimeMailBuilder()
                        .setTo(mailIdsToSendReport)
                        .setSubject(dynamicReportConfig.getReportTitle())
                        .setPlainTextBody(
                                "Dear User,\n\nPFA the report generated for your query.\n[".concat(
                                        dynamicReportConfig.getDynamicQueryWhereClause()).concat("]"))
                        .addAttachment(dynamicReportPojo.getFileName(),
                                new ByteArrayResource(dynamicReportPojo.getReportData())).getMimeMessage();
                try {
                    mailMessageIntegrationService.sendMailMessageToIntegrationServer(mimeMessage);
                } catch (MessagingException e) {
                    BaseLoggers.exceptionLogger.error("Error in sending generated dynamic report by E-mail", e);
                } catch (IOException e) {
                    BaseLoggers.exceptionLogger.error("Error in sending generated dynamic report by E-mail", e);
                }
            } else {
                BaseLoggers.exceptionLogger
                        .error("Error in generating dynamic report.Null returned from DynamicReportBuilder.generateReport.");
            }
        }

    }

}
