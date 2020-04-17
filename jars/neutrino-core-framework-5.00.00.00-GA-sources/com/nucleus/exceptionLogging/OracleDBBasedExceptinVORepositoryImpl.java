package com.nucleus.exceptionLogging;

import java.util.*;

import javax.persistence.Query;

import com.nucleus.config.persisted.enity.Configuration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.service.BaseServiceImpl;

public class OracleDBBasedExceptinVORepositoryImpl extends BaseServiceImpl implements ExceptionVORepository {

    private static final String queryString = "truncate table  neutrino_exception_entity";

    public static final String USER_BASED_FILTER = "User";
    public static final String EXCEPTION_BASED_FILTER = "Exception";
    public static final String APPLICATION_BASED_FILTER = "Application";

    @Override
    public List<ExceptionVO> findByExceptionType(String exceptionType) {
        String[] exceptionTypeArray = exceptionType.split(",");
        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byType").addParameter("exceptionType", Arrays.asList(exceptionTypeArray));
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);
    }

    @Override
    public List<ExceptionVO> findByExceptionType(String exceptionType, String node) {
        String[] exceptionTypeArray = exceptionType.split(",");
        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byTypeWithNode").addParameter("exceptionType", Arrays.asList(exceptionTypeArray)).addParameter("node", node);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);
    }

    @Override
    public Set<String> findUniqueExceptionTypes() {
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>("Exception.uniqueType");
        return new HashSet<String>(entityDao.executeQuery(executor));
    }

    @Override
    public List<ExceptionVO> findByLoggedInUserUri(String loggedInUserUri, String node) {
        String[] loggedInUserUriArray = loggedInUserUri.split(",");
        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byLoggedInUserWithNode").addParameter("loggedInUserUri", Arrays.asList(loggedInUserUriArray))
                .addParameter("node", node);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);


        return populateExceptionVoList(exceptionList);
    }

    @Override
    public List<ExceptionVO> findByLoggedInUserUri(String loggedInUserUri) {
        String[] loggedInUserUriArray = loggedInUserUri.split(",");
        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byLoggedInUser").addParameter("loggedInUserUri", Arrays.asList(loggedInUserUriArray));
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);

    }

    @Override
    public Set<String> findUniqueUserUri() {
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>("Exception.uniqueUser");
        return new HashSet<String>(entityDao.executeQuery(executor));
    }

    @Override
    public List<ExceptionVO> findByMethodName(String methodName) {

        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byMethodName").addParameter("methodName", methodName);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);
    }

    @Override
    public List<ExceptionVO> findByClassName(String className) {

        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byClassName").addParameter("className", className);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);
    }

    @Override
    public List<ExceptionVO> findByExceptionOccuredTimestamp(String exceptionOccuredTimestamp) {

        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byTimeStamp").addParameter("exceptionOccuredTimestamp", exceptionOccuredTimestamp);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);
    }

    @Override
    public List<ExceptionVO> findByExceptionOccuredDate(String exceptionOccuredDate) {

        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byDate").addParameter("exceptionOccuredDate", exceptionOccuredDate);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);
    }

    @Override
    public List<ExceptionVO> findExceptionsByDateAndType(String exceptionOccuredTimestamp, String exceptionType) {

        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byDateAndType").addParameter("exceptionOccuredTimestamp", exceptionOccuredTimestamp)
                .addParameter("exceptionType", exceptionType);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);
    }

    @Override
    public void saveExceptionObject(ExceptionVO exceptionVO) {
        NeutrinoExceptionEntity exception = new NeutrinoExceptionEntity();
        exception.setExceptionType(exceptionVO.getExceptionType());
        exception.setClassName(exceptionVO.getClassName());
        exception.setCasTransactionId(exceptionVO.getCasTransactionId());
        exception.setFileName(exceptionVO.getFileName());
        // exception.setId(exceptionVO.getId().toString());
        exception.setLoggedInUserUri(exceptionVO.getLoggedInUserUri());
        exception.setMessage(exceptionVO.getMessage());
        exception.setMethodName(exceptionVO.getMethodName());
        exception.setCreationDate(new DateTime().millisOfDay().withMinimumValue().toDateTime(DateTimeZone.UTC));
        exception.setNode(exceptionVO.getNode());
        exception.setStackTrace(exceptionVO.getStackTrace());
        exception.setRequestURI(exceptionVO.getRequestURI());
        exception.setRequestParameters(exceptionVO.getRequestParameters());
		exception.setFunctionalParameter(exceptionVO.getFunctionalParameter());
        entityDao.persist(exception);

    }

    @Override
    public ExceptionVO getExceptionById(String exceptionVOId) {
        NeutrinoValidator.notEmpty(exceptionVOId);
        NeutrinoExceptionEntity neutrinoExceptionEntity = entityDao.find(
                NeutrinoExceptionEntity.class, Long.valueOf(exceptionVOId));
        return populateAndReturnExceptionVOFromEntity(neutrinoExceptionEntity);
    }

    private ExceptionVO populateAndReturnExceptionVOFromEntity(NeutrinoExceptionEntity neutrinoExceptionEntity) {
        ExceptionVO exception = new ExceptionVO();
        exception.setExceptionType(neutrinoExceptionEntity.getExceptionType());
        exception.setClassName(neutrinoExceptionEntity.getClassName());
        exception.setCasTransactionId(neutrinoExceptionEntity.getCasTransactionId());
        exception.setFileName(neutrinoExceptionEntity.getFileName());
        exception.setId(neutrinoExceptionEntity.getId().toString());
        exception.setLoggedInUserUri(neutrinoExceptionEntity.getLoggedInUserUri());
        exception.setMessage(neutrinoExceptionEntity.getMessage());
        exception.setMethodName(neutrinoExceptionEntity.getMethodName());
        exception.setNode(neutrinoExceptionEntity.getNode());
        exception.setStackTrace(neutrinoExceptionEntity.getStackTrace());
        exception.setRequestURI(neutrinoExceptionEntity.getRequestURI());
        exception.setFunctionalParameter(neutrinoExceptionEntity.getFunctionalParameter());
        exception.setRequestParameters(neutrinoExceptionEntity.getRequestParameters());
        exception.setExceptionOccuredDate(neutrinoExceptionEntity.getEntityLifeCycleData().getCreationTimeStamp()
                .millisOfDay().withMinimumValue().toDateTime(DateTimeZone.UTC).toString());
        exception.setExceptionOccuredTimestamp(neutrinoExceptionEntity.getEntityLifeCycleData().getCreationTimeStamp()
                .toString());
        return exception;
    }

    @Override
    public List<ExceptionVO> findExceptionsBetweenDates(DateTime startDate, DateTime endDate, String node) {
        DateTime endDateGenerated = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23,
                59, 59);
        endDate = endDateGenerated;
        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.betweenDatesWithNode").addParameter("startDate", startDate).addParameter("endDate", endDate)
                .addParameter("node", node);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);
        return populateExceptionVoList(exceptionList);
    }

    @Override
    public List<ExceptionVO> findExceptionsBetweenDates(DateTime startDate, DateTime endDate) {

        DateTime endDateGenerated = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23,
                59, 59);
        endDate = endDateGenerated;
        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.betweenDates").addParameter("startDate", startDate).addParameter("endDate", endDate);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);
        return populateExceptionVoList(exceptionList);
    }

    public List<ExceptionVO> populateExceptionVoList(List<NeutrinoExceptionEntity> exceptionList) {
        List<ExceptionVO> exceptionVoList = new ArrayList<ExceptionVO>();
        if (CollectionUtils.isNotEmpty(exceptionList)) {
            for (NeutrinoExceptionEntity neutrinoExceptionEntity : exceptionList) {
                exceptionVoList.add(populateAndReturnExceptionVOFromEntity(neutrinoExceptionEntity));
            }
        }
        return exceptionVoList;
    }

    @Override
    public boolean deleteExceptionOnBasisOfDays(int days) {
        if (days == 0) {
            return false;
        }
        DateTime fromDateTime = new DateTime().minusDays(days).millisOfDay().withMinimumValue();
        DateTime startDateTime = new DateTime().millisOfDay().withMinimumValue();

        DateTime endDateGenerated = new DateTime(startDateTime.getYear(), startDateTime.getMonthOfYear(),
                startDateTime.getDayOfMonth(), 23, 59, 59);


        String qlString = "DELETE FROM NeutrinoExceptionEntity e WHERE e.entityLifeCycleData.creationTimeStamp BETWEEN :startDate AND :endDate";
        Query qry = entityDao.getEntityManager().createQuery(qlString);
        qry.setParameter("startDate", fromDateTime);
        qry.setParameter("endDate", endDateGenerated);
        qry.executeUpdate();

        return false;
    }

    @Override
    public List<ExceptionVO> getAll() {

        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.all");
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);
    }

    @Override
    public void truncateAllExceptionsInDb() {

        StringBuilder nativeQueryStringBuilder = new StringBuilder();
        nativeQueryStringBuilder.append(String.format(queryString));
        String selectClause = nativeQueryStringBuilder.toString();
        Query nativeQuery = entityDao.getEntityManager().createNativeQuery(selectClause);
        nativeQuery.unwrap(org.hibernate.SQLQuery.class).addSynchronizedQuerySpace("");
        nativeQuery.executeUpdate();
    }

    @Override
    public void findUniqueNode(List<String> nodeList) {

        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>("Exception.uniqueNode");
        nodeList.addAll(entityDao.executeQuery(executor));
    }

    @Override
    public void getExceptionsFromNode(List<ExceptionVO> exceptionsList, String node) {
        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.byNode").addParameter("node", node);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        exceptionsList.addAll(populateExceptionVoList(exceptionList));
    }

    @Override
    public void saveIgnoredException(String ignoredExcept) {
        NamedQueryExecutor<Configuration> executor = new NamedQueryExecutor<Configuration>(
                "Exception.configurationByPropertyKey").addParameter("propertyKey", "config.exceptions.remove.log");
        List<Configuration> configuration = entityDao.executeQuery(executor);
        for (Configuration config : configuration) {
            config.setPropertyValue(ignoredExcept);
            entityDao.saveOrUpdate(config);
        }

    }

    @Override
    public void getIgnoredException(StringBuilder ignoredExcept) {
        NamedQueryExecutor<Configuration> executor = new NamedQueryExecutor<Configuration>(
                "Exception.configurationByPropertyKey").addParameter("propertyKey", "config.exceptions.remove.log");

        List<Configuration> configuration = entityDao.executeQuery(executor);
        if (configuration.get(0).getPropertyValue() != null)
            ignoredExcept.append(configuration.get(0).getPropertyValue());

    }

    @Override
    public List getUniqueExceptionData(String node, String exceptionType) {
        NamedQueryExecutor<NeutrinoExceptionEntity> executor = new NamedQueryExecutor<NeutrinoExceptionEntity>(
                "Exception.getUniqueExceptionData").addParameter("node", node)
                .addParameter("exceptionType", exceptionType);
        List<NeutrinoExceptionEntity> exceptionList = entityDao.executeQuery(executor);

        return populateExceptionVoList(exceptionList);
    }

    private Query appendQueryString(String searchParam, String searchParamValue, DateTime toDate, DateTime fromDate, String node, String casTransactionId, StringBuilder exceptionQueryBuilder, Map datatableMap) {
        Boolean isMultiple = new Boolean(false);
        String exceptionType = (String) datatableMap.get("exceptionType");

        if (StringUtils.isNotEmpty(searchParamValue) || toDate != null || fromDate != null || StringUtils.isNotEmpty(node) || StringUtils.isNotEmpty(casTransactionId) || StringUtils.isNotEmpty(exceptionType)) {
            exceptionQueryBuilder.append(" Where ");
            if (StringUtils.isNotEmpty(searchParam) && StringUtils.isNotEmpty(searchParamValue)) {
                if (searchParam.equals(EXCEPTION_BASED_FILTER)) {
                    exceptionQueryBuilder.append(" e.exceptionType in (:searchParamValue) ");
                } else if (searchParam.equals(USER_BASED_FILTER)) {
                    exceptionQueryBuilder.append(" e.loggedInUserUri in (:searchParamValue) ");
                }
                else if (searchParam.equals(APPLICATION_BASED_FILTER)) {
                    exceptionQueryBuilder.append(" e.functionalParameter in (:searchParamValue) ");
                }
                isMultiple = true;
            }

            if (StringUtils.isNotEmpty(node)) {
                if (isMultiple) {
                    exceptionQueryBuilder.append(" AND ");
                }
                exceptionQueryBuilder.append("e.node = :node ");
                isMultiple = true;
            }

            if (StringUtils.isNotEmpty(exceptionType)) {
                if (isMultiple) {
                    exceptionQueryBuilder.append(" AND ");
                }
                exceptionQueryBuilder.append(" e.exceptionType = :exceptionType ");
                isMultiple = true;
            }


            if (fromDate != null) {
                if (isMultiple) {
                    exceptionQueryBuilder.append(" AND ");
                }

                exceptionQueryBuilder.append(" e.entityLifeCycleData.creationTimeStamp BETWEEN :startDate AND :endDate ");
                isMultiple = true;
            }

            if (StringUtils.isNotEmpty(casTransactionId)) {
                if (isMultiple) {
                    exceptionQueryBuilder.append(" AND ");
                }
                exceptionQueryBuilder.append(" e.casTransactionId = :casTransactionId ");
                isMultiple = true;
            }
        }
        if (isMultiple) {
            exceptionQueryBuilder.append(" AND ");
        }
        else {
            exceptionQueryBuilder.append(" Where ");
        }

        String sortColumnName = (String) datatableMap.get("sortColumnName");
        String sortColumn = (String) datatableMap.get("sortColumnName");

        if (sortColumnName.equalsIgnoreCase("exceptionOccuredTimestamp")) {
            sortColumnName = "e.entityLifeCycleData.creationTimeStamp ";
        }
        else if (sortColumnName.equalsIgnoreCase("fileName")){
            sortColumnName = " upper(e.fileName) " + datatableMap.get("sortColumnDirection") + " NULLS LAST , upper(e.casTransactionId) asc";
        }
        else {
            sortColumnName = "upper(e." + sortColumnName + ") ";
        }

        exceptionQueryBuilder.append(" ( upper(e.casTransactionId) like :sSearch OR " +
                "upper(e.className) like :sSearch OR " +
                "upper(e.fileName) like :sSearch OR " +
                "upper(e.functionalParameter) like :sSearch OR " +
                "upper(e.exceptionType) like :sSearch OR " +
                "upper(e.methodName) like :sSearch OR " +
                "upper(e.node) like :sSearch) ");
        if(!exceptionQueryBuilder.toString().contains("Select count")) {
            exceptionQueryBuilder.append(" ORDER BY ");
            if (!sortColumn.equalsIgnoreCase("fileName")) {
                exceptionQueryBuilder.append(sortColumnName + datatableMap.get("sortColumnDirection") + " NULLS LAST");
            } else {
                exceptionQueryBuilder.append(sortColumnName);
            }
        }

        Query query = entityDao.getEntityManager().createQuery(exceptionQueryBuilder.toString());


        if (StringUtils.isNotEmpty(searchParam) && StringUtils.isNotEmpty(searchParamValue)) {
            String[] searchParamValueArray = searchParamValue.split(",");
            query.setParameter("searchParamValue", Arrays.asList(searchParamValueArray));
        }

        if (StringUtils.isNotEmpty(node)) {
            query.setParameter("node", node);
        }

        if (StringUtils.isNotEmpty(exceptionType)) {
            query.setParameter("exceptionType", exceptionType.trim());
        }

        if (fromDate != null) {
            query.setParameter("startDate", fromDate);
            /*if (toDate == null) {
                toDate = new DateTime();
            }*/
            //toDate = toDate.plusDays(1).minusMillis(1);
            query.setParameter("endDate", toDate);
        }

        if (StringUtils.isNotEmpty(casTransactionId)) {
            query.setParameter("casTransactionId", casTransactionId);
        }

        query.setParameter("sSearch", "%" + datatableMap.get("sSearch").toString().toUpperCase() + "%");
        return query;
    }


    public Long getTotalRecords(String searchParam, String searchParamValue, DateTime toDate, DateTime fromDate, String node, String casTransactionId, Map datatableMap) {

        StringBuilder exceptionQueryBuilder = new StringBuilder("Select count(*) FROM NeutrinoExceptionEntity e ");
        Query query = appendQueryString(searchParam, searchParamValue, toDate, fromDate, node, casTransactionId, exceptionQueryBuilder, datatableMap);
        Long iTotalRecords = (Long) query.getSingleResult();

        return iTotalRecords;
    }


    @Override
    public Map fetchExceptionsFromDatesAndParameterValue(Map fieldData, Map datatableMap) {

        //String searchParam, String searchParamValue, DateTime toDate, DateTime fromDate, String node, String casTransactionId



        String searchParam = (String) fieldData.get("searchParameter");
        String searchParamValue = (String) fieldData.get("searchParamValue");
        DateTime toDate = (DateTime)fieldData.get("toDate");
        DateTime fromDate = (DateTime)fieldData.get("fromDate");
        String node = (String) fieldData.get("node");
        String casTransactionId = (String) fieldData.get("casTransactionId");
        String exceptionType = (String) fieldData.get("exceptionType");

        datatableMap.put("exceptionType", exceptionType);



        StringBuilder exceptionQueryBuilder = new StringBuilder("Select e FROM NeutrinoExceptionEntity e ");
        Query query = appendQueryString(searchParam, searchParamValue, toDate, fromDate, node, casTransactionId, exceptionQueryBuilder, datatableMap);

        query.setFirstResult((Integer) datatableMap.get("iDisplayStart"));
        query.setMaxResults((Integer) datatableMap.get("iDisplayLength"));

        List exceptionList = query.getResultList();
        Long iTotalRecords = getTotalRecords(searchParam, searchParamValue, toDate, fromDate, node, casTransactionId, datatableMap);

        Map map = new HashMap();
        map.put("iTotalRecords", iTotalRecords);
        map.put("exceptionVoList", populateExceptionVoList(exceptionList));

        return map;
    }

    @Override
    public List getExceptionSummary(String node) {

        NamedQueryExecutor<Map> executor = new NamedQueryExecutor<Map>("Exception.exceptionsAndCountOfNode").addParameter("node", node);
        List<Map> mapList= entityDao.executeQuery(executor);

        return mapList;
    }

    @Override
    public List getExceptionSummaryGroup(List nodeList) {

        NamedQueryExecutor<Map> executor = new NamedQueryExecutor<Map>("Exception.exceptionCountOfNodeGroup").addParameter("nodeList", nodeList);
        List<Map> mapList= entityDao.executeQuery(executor);

        return mapList;
    }
}
