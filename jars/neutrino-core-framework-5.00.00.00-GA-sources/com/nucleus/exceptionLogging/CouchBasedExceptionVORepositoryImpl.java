/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.exceptionLogging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ektorp.BulkDeleteDocument;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentOperationResult;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
public class CouchBasedExceptionVORepositoryImpl extends CouchDbRepositorySupport<ExceptionVO> implements
        ExceptionVORepository {

    public CouchBasedExceptionVORepositoryImpl(CouchDbConnector db) {
        super(ExceptionVO.class, db);
        initStandardDesignDocument();
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findByExceptionType(java.lang.String) */
    @Override
    @View(name = "by_exceptionType", map = "function(doc) {if(doc.exceptionType) {emit(doc.exceptionType,doc.exceptionType);}}")
    public List<ExceptionVO> findByExceptionType(String exceptionType) {
        return queryView("by_exceptionType", exceptionType);
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findUniqueExceptionTypes() */
    @Override
    public Set<String> findUniqueExceptionTypes() {
        ViewQuery query1 = new ViewQuery().designDocId("_design/ExceptionVO").viewName("by_exceptionType");
        ViewResult result1 = db.queryView(query1);
        ViewResult viewResult = result1;
        Set<String> exceptionTypelist = new HashSet<String>();
        if (viewResult == null) {
            return exceptionTypelist;
        }
        for (ViewResult.Row row : viewResult.getRows()) {
            String stringValue = row.getValue();
            exceptionTypelist.add(stringValue);
        }
        return exceptionTypelist;
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findByLoggedInUserUri(java.lang.String) */
    @Override
    @View(name = "by_loggedInUserUri", map = "function(doc) {if(doc.loggedInUserUri) {emit(doc.loggedInUserUri,doc.loggedInUserUri);}}")
    public List<ExceptionVO> findByLoggedInUserUri(String loggedInUserUri) {
        return queryView("by_loggedInUserUri", loggedInUserUri);
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findUniqueUserUri() */
    @Override
    public Set<String> findUniqueUserUri() {
        ViewQuery userQuery = new ViewQuery().designDocId("_design/ExceptionVO").viewName("by_loggedInUserUri");
        ViewResult result1 = db.queryView(userQuery);
        ViewResult viewResult = result1;
        Set<String> userURIlist = new HashSet<String>();
        if (viewResult == null) {
            return userURIlist;
        }
        for (ViewResult.Row row : viewResult.getRows()) {
            String stringValue = row.getKey();
            userURIlist.add(stringValue);
        }
        return userURIlist;
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findByMethodName(java.lang.String) */
    @Override
    @View(name = "by_methodName", map = "function(doc) {if(doc.methodName) {emit(doc.methodName,doc.methodName);}}")
    public List<ExceptionVO> findByMethodName(String methodName) {
        return queryView("by_methodName", methodName);
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findByClassName(java.lang.String) */
    @Override
    @GenerateView
    public List<ExceptionVO> findByClassName(String className) {
        return queryView("by_className", className);
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findByExceptionOccuredTimestamp(java.lang.String) */
    @Override
    @GenerateView
    public List<ExceptionVO> findByExceptionOccuredTimestamp(String exceptionOccuredTimestamp) {
        return queryView("by_exceptionOccuredTimestamp", exceptionOccuredTimestamp);
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findByExceptionOccuredDate(java.lang.String) */
    @Override
    @GenerateView
    public List<ExceptionVO> findByExceptionOccuredDate(String exceptionOccuredDate) {
        return queryView("by_exceptionOccuredDate", exceptionOccuredDate);
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findExceptionsByDateAndType(java.lang.String, java.lang.String) */
    @Override
    @View(name = "by_exceptionOccuredTimestamp_and_exceptionType", map = "function(doc) {if(doc.exceptionOccuredTimestamp && doc.exceptionType) {emit([doc.exceptionOccuredTimestamp, doc.exceptionType],null);}}")
    public List<ExceptionVO> findExceptionsByDateAndType(String exceptionOccuredTimestamp, String exceptionType) {
        ComplexKey complexKey = ComplexKey.of(exceptionOccuredTimestamp, exceptionType);
        return queryView("by_exceptionOccuredTimestamp_and_exceptionType", complexKey);

    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#saveExceptionObject(com.nucleus.exceptionLogging.ExceptionVO) */
    @Override
    public void saveExceptionObject(ExceptionVO exceptionVO) {
        if (exceptionVO == null) {
            return;
        }
        db.create(exceptionVO);
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#getExceptionById(java.lang.String) */
    @Override
    public ExceptionVO getExceptionById(String exceptionVOId) {
        if (StringUtils.isBlank(exceptionVOId)) {
            return null;
        }
        return db.get(ExceptionVO.class, exceptionVOId);

    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#findExceptionsBetweenDates(org.joda.time.DateTime, org.joda.time.DateTime) */
    @Override
    public List<ExceptionVO> findExceptionsBetweenDates(DateTime startDate, DateTime endDate) {
        ViewQuery query = new ViewQuery().designDocId("_design/ExceptionVO").viewName("by_exceptionOccuredDate")
                .startKey(startDate.toDateTime(DateTimeZone.UTC).toString())
                .endKey(endDate.toDateTime(DateTimeZone.UTC).toString()).includeDocs(true);
        return db.queryView(query, ExceptionVO.class);
    }

    /* (non-Javadoc) @see com.nucleus.exceptionLogging.ExceptionVORepository#deleteExceptionOnBasisOfDays(int) */
    @Override
    public boolean deleteExceptionOnBasisOfDays(int days) {
        List<DocumentOperationResult> documentOperationResults;
        if (days == 0) {
            return false;
        }

        DateTime fromDateTime = new DateTime().minusDays(days).millisOfDay().withMinimumValue();
        DateTime startDateTime = new DateTime().millisOfDay().withMinimumValue();
        List<ExceptionVO> exceptionByDateList = findExceptionsBetweenDates(fromDateTime, startDateTime);
        List<Object> bulkDocs = new ArrayList<Object>();
        if (CollectionUtils.isEmpty(exceptionByDateList)) {
            return false;
        }
        for (ExceptionVO exceptionVO : exceptionByDateList) {
            bulkDocs.add(BulkDeleteDocument.of(exceptionVO));
        }

        documentOperationResults = db.executeBulk(bulkDocs);

        if (CollectionUtils.isNotEmpty(documentOperationResults)) {
            return true;
        }
        return false;
    }

    @Override
    public void truncateAllExceptionsInDb() {
        BaseLoggers.flowLogger.info("Method truncateAllExceptionsInDb not supported for couch db based exception repository for now");
    }

    @Override
    public List<ExceptionVO> findByExceptionType(String exceptionType, String node) {
        return null;
    }

    @Override
    public List<ExceptionVO> findByLoggedInUserUri(String loggedInUserUri, String node) {
        return null;
    }

    @Override
    public List<ExceptionVO> findExceptionsBetweenDates(DateTime startDate, DateTime endDate, String node) {
        return null;
    }
}
