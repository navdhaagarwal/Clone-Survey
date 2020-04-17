package com.nucleus.core.datasource.logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryInfoPerRequestHolder {

    private static ThreadLocal<Map<String, QueryInfoPerRequest>> queryCountMapHolder = new ThreadLocal<Map<String, QueryInfoPerRequest>>() {
                                                                                         @Override
                                                                                         protected Map<String, QueryInfoPerRequest> initialValue() {
                                                                                             return new HashMap<String, QueryInfoPerRequest>();
                                                                                         }
                                                                                     };

    private static ThreadLocal<Boolean>                          httpRequestActive   = new ThreadLocal<Boolean>() {
                                                                                         @Override
                                                                                         protected Boolean initialValue() {
                                                                                             return Boolean.FALSE;
                                                                                         }
                                                                                     };

    public static QueryInfoPerRequest get(String dataSourceName) {
        final Map<String, QueryInfoPerRequest> map = queryCountMapHolder.get();
        return map.get(dataSourceName);
    }

    public static QueryInfoPerRequest getGrandTotal() {
        final QueryInfoPerRequest totalCount = new QueryInfoPerRequest();
        final Map<String, QueryInfoPerRequest> map = queryCountMapHolder.get();
        for (QueryInfoPerRequest queryCount : map.values()) {
            totalCount.setSelect(totalCount.getSelect() + queryCount.getSelect());
            totalCount.setInsert(totalCount.getInsert() + queryCount.getInsert());
            totalCount.setUpdate(totalCount.getUpdate() + queryCount.getUpdate());
            totalCount.setDelete(totalCount.getDelete() + queryCount.getDelete());
            totalCount.setOther(totalCount.getOther() + queryCount.getOther());
            totalCount.setCall(totalCount.getCall() + queryCount.getCall());
            totalCount.setFailure(totalCount.getFailure() + queryCount.getFailure());
            totalCount.setElapsedTime(totalCount.getElapsedTime() + queryCount.getElapsedTime());
        }
        return totalCount;
    }

    public static void put(String dataSourceName, QueryInfoPerRequest count) {
        queryCountMapHolder.get().put(dataSourceName, count);
    }

    public static List<String> getDataSourceNamesAsList() {
        return new ArrayList<String>(getDataSourceNames());
    }

    public static Set<String> getDataSourceNames() {
        return queryCountMapHolder.get().keySet();
    }

    public static void clear() {
        queryCountMapHolder.get().clear();
        httpRequestActive.set(Boolean.FALSE);
    }

    public static void setHttpRequestActive() {
        httpRequestActive.set(Boolean.TRUE);
    }

    public static Boolean getHttpRequestActive() {
        return httpRequestActive.get();
    }

}
