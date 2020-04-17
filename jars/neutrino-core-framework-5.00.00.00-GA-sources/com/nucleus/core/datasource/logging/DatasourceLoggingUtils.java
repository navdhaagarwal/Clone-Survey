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
package com.nucleus.core.datasource.logging;

import java.util.Collections;
import java.util.List;

import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.SLF4JLogLevel;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nucleus Software Exports Limited
 */
public class DatasourceLoggingUtils {

    private static Logger                  COUNT_LOGGER             = LoggerFactory
                                                                            .getLogger("com.nucleus.sql.count.logger");
    private static Logger                  QUERY_LOGGER             = LoggerFactory
                                                                            .getLogger("com.nucleus.sql.query.logger");
    private static Logger                  COUNT_CSV_LOGGER         = LoggerFactory
                                                                            .getLogger("com.nucleus.sql.count.csv.logger");

    private static Logger                  QUERY_LOGGER_NON_REQUEST = LoggerFactory
                                                                            .getLogger("com.nucleus.sql.query.nonRequestThread.logger");

    private static SLF4JLogLevel           logLevel                 = SLF4JLogLevel.DEBUG;

    private static final String            MESSAGE                  = "Datasource:{} Time(Millis):{} Num:{} Query:{}";

    private static final String            DATE_FORMAT              = "dd-MMM-yyyy hh:mm:ss.SSS a";

    private static final DateTimeFormatter DATE_TIME_FORMATTER      = DateTimeFormat.forPattern(DATE_FORMAT);

    static {
        COUNT_CSV_LOGGER.debug(getCSVHeader());
    }

    public static void logQueriesForRequestAndClearContext(String requestUri, boolean isHttpRequestActive) {

        String requestId = RandomStringUtils.randomAlphanumeric(20);

        final List<String> dsNames = QueryInfoPerRequestHolder.getDataSourceNamesAsList();
        if (dsNames != null && !dsNames.isEmpty()) {
            Collections.sort(dsNames);
            for (String dsName : dsNames) {
                final QueryInfoPerRequest count = QueryInfoPerRequestHolder.get(dsName);
                String message = getLogMessage(dsName, count);
                if (requestUri != null) {
                    message = "[".concat(requestUri).concat("] ").concat(message);
                }
                if (isHttpRequestActive) {
                    // log counts as plain text
                    // COUNT_LOGGER.debug(message);
                    // log counts as csv for reporting purpose
                    COUNT_CSV_LOGGER.debug(getLogMessageAsCSV(dsName, count, requestUri, requestId));
                } else {
                    QUERY_LOGGER_NON_REQUEST.debug(message);
                    // optionally log counts as csv for reporting purpose
                }
                int totalLogs = count.getQueryInfoLists().size();
                if (isHttpRequestActive) {
                    QUERY_LOGGER.debug("------------------------------" + getDateTime() + "----------------------------");
                    QUERY_LOGGER.debug("Queries For RequestURI [{}] and Request ID [{}] Total Queries:[{}]         ",
                            requestUri, requestId, count.getTotalNumOfQuery());
                    QUERY_LOGGER
                            .debug("---------------------------------------xxxxxxxxx-------------------------------------");
                }

                for (int i = 0 ; i < totalLogs ; i++) {
                    logQueriesForRequest(dsName, count.getQueryInfoLists().get(i), count.getElapsedTimePerQuery().get(i),
                            isHttpRequestActive);
                }

            }
        }

        QueryInfoPerRequestHolder.clear();

    }

    public static String getLogMessage(String datasourceName, QueryInfoPerRequest count) {
        final StringBuilder sb = new StringBuilder();
        sb.append("DataSource:");
        sb.append(datasourceName);
        sb.append(" ");

        sb.append("ElapsedTime(Millis):");
        sb.append(count.getElapsedTime());
        sb.append(" ");

        sb.append("Call:");
        sb.append(count.getCall());
        sb.append(" ");

        sb.append("Query:");
        sb.append(count.getTotalNumOfQuery());

        sb.append(" (");
        sb.append("Select:");
        sb.append(count.getSelect());
        sb.append(" ");

        sb.append("Insert:");
        sb.append(count.getInsert());
        sb.append(" ");

        sb.append("Update:");
        sb.append(count.getUpdate());
        sb.append(" ");

        sb.append("Delete:");
        sb.append(count.getDelete());
        sb.append(" ");

        sb.append("Other:");
        sb.append(count.getOther());
        sb.append(")");

        return sb.toString();
    }

    public static String getLogMessageAsCSV(String datasourceName, QueryInfoPerRequest count, String requestUri,
            String requestId) {
        final StringBuilder sb = new StringBuilder();

        // sb.append("RequestUri:");
        sb.append(requestUri);
        sb.append(",");

        // sb.append("RequestID:");
        sb.append(requestId);
        sb.append(",");

        // sb.append("ElapsedTime(Millis):");
        sb.append(count.getElapsedTime());
        sb.append(",");

        // sb.append("Call:");
        sb.append(count.getCall());
        sb.append(",");

        // sb.append("Query:");
        sb.append(count.getTotalNumOfQuery());
        sb.append(",");

        // sb.append("Select:");
        sb.append(count.getSelect());
        sb.append(",");

        // sb.append("Insert:");
        sb.append(count.getInsert());
        sb.append(",");

        // sb.append("Update:");
        sb.append(count.getUpdate());
        sb.append(",");

        // sb.append("Delete:");
        sb.append(count.getDelete());
        sb.append(",");

        // sb.append("Other:");
        sb.append(count.getOther());
        sb.append(",");
        sb.append(getDateTime());
        sb.append(",");
        // sb.append("DataSource:");
        sb.append(datasourceName);

        return sb.toString();
    }

    private static void logQueriesForRequest(String dsName, List<QueryInfo> queryInfoList, long elapsedTime,
            boolean isHttpRequestActive) {

        final int numOfQuery = queryInfoList.size();

        final StringBuilder sb = new StringBuilder();
        for (QueryInfo queryInfo : queryInfoList) {
            sb.append("{");
            final String query = queryInfo.getQuery();
            final List args = queryInfo.getQueryArgs();

            sb.append("[");
            sb.append(query);
            sb.append("][");

            for (Object arg : args) {
                sb.append(arg);
                sb.append(',');
            }

            // chop if last char is ','
            chopIfEndWith(sb, ',');

            sb.append("]");
            sb.append("} ");
        }
        final String queries = sb.toString();

        writeLog(isHttpRequestActive ? QUERY_LOGGER : QUERY_LOGGER_NON_REQUEST, logLevel, MESSAGE, new Object[] { dsName,
                elapsedTime, numOfQuery, queries });

    }

    private static void writeLog(Logger logger, SLF4JLogLevel logLevel, String message, Object[] argArray) {
        switch (logLevel) {
            case DEBUG:
                logger.debug(message, argArray);
                break;
            case ERROR:
                logger.error(message, argArray);
                break;
            case INFO:
                logger.info(message, argArray);
                break;
            case TRACE:
                logger.trace(message, argArray);
                break;
            case WARN:
                logger.warn(message, argArray);
                break;
        }
    }

    private static void chopIfEndWith(StringBuilder sb, char c) {
        final int lastCharIndex = sb.length() - 1;
        if (sb.charAt(lastCharIndex) == c) {
            sb.deleteCharAt(lastCharIndex);
        }
    }

    public static String getCSVHeader() {
        final StringBuilder sb = new StringBuilder();
        // sb.append("Date,");
        sb.append("RequestUri,");
        sb.append("Request ID,");
        sb.append("ElapsedTime(Millis),");
        sb.append("DB Calls,");
        sb.append("Total Queries,");
        sb.append("Select Queries,");
        sb.append("Insert Queries,");
        sb.append("Update Queries,");
        sb.append("Delete Queries,");
        sb.append("Other Queries,");
        sb.append("Timestamp,");
        sb.append("DataSource,");
        return sb.toString();
    }

    private static String getDateTime() {
        return DATE_TIME_FORMATTER.print(DateTime.now());
    }

}
