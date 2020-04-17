package com.nucleus.core.datasource.logging;

import java.util.List;

import com.nucleus.logging.BaseLoggers;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

public class DataSourceQueryLoggingListener implements QueryExecutionListener {

    private static final String NON_REQUEST_QUERY = "NON_REQUEST_THREAD_QUERY";

    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
    }

    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {

        try {
            final String dataSourceName = execInfo.getDataSourceName();

            QueryInfoPerRequest count = QueryInfoPerRequestHolder.get(dataSourceName);
            if (count == null) {
                count = new QueryInfoPerRequest();
                QueryInfoPerRequestHolder.put(dataSourceName, count);
            }

            // increment db call
            count.incrementCall(); // num of db call
            if (execInfo.getThrowable() != null) {
                count.incrementFailure();
            }

            // increment elapsed time
            final long elapsedTime = execInfo.getElapsedTime();
            count.incrementElapsedTime(elapsedTime);

            count.addQueryListWithElapsedTime(queryInfoList, elapsedTime);

            // increment query count
            for (QueryInfo queryInfo : queryInfoList) {
                final String query = queryInfo.getQuery();
                final QueryType type = getQueryType(query);
                count.increment(type);

            }

            // queries are not related to any request.So there is no interceptor to log and clear context.
            // everything must be done here
            if (!QueryInfoPerRequestHolder.getHttpRequestActive()) {
                DatasourceLoggingUtils.logQueriesForRequestAndClearContext(NON_REQUEST_QUERY, false);
            }
        } catch (Exception th) {

            // don't let it to break business logic
            BaseLoggers.exceptionLogger.error("Error in logging sql queries {}", th.getMessage());

        }
    }

    private QueryType getQueryType(String query) {
        final String trimmedQuery = removeCommentAndWhiteSpace(query);
        final char firstChar = trimmedQuery.charAt(0);

        final QueryType type;
        switch (firstChar) {
            case 'S':
            case 's':
                type = QueryType.SELECT;
                break;
            case 'I':
            case 'i':
                type = QueryType.INSERT;
                break;
            case 'U':
            case 'u':
                type = QueryType.UPDATE;
                break;
            case 'D':
            case 'd':
                type = QueryType.DELETE;
                break;
            default:
                type = QueryType.OTHER;
        }
        return type;
    }

    private String removeCommentAndWhiteSpace(String query) {
        return query.replaceAll("--.*\n", "").replaceAll("\n", "").replaceAll("/\\*.*\\*/", "").trim();
    }

}
