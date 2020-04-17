/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.exceptionLogging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.core.json.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.MDC;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.thread.support.MdcRetainingRunnable;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;

import static com.nucleus.logging.BaseLoggers.exceptionLogger;

/**
 * @author Nucleus Software India Pvt Ltd 
 */
@Named(value = "exceptionLoggingService")
public class ExceptionLoggingServiceImpl implements ExceptionLoggingService {

    private static final String   DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    private static final String   DEFAULT_TIME_FORMAT = "hh:mm:ss a";
    private static final String   SPACE_STRING        = " ";

    @Inject
    @Named("exceptionVORepository")
    private ExceptionVORepository exceptionVORepository;

    @Inject
    @Named("neutrinoThreadPoolExecutor")
    protected Executor            taskExecutor;

    /*
     * TODO: temporarily passing logged in user as a parameter as current
     * user being retrieved in service becomes null as couch db transaction
     * is being executed in a separate thread.
     */
    @Override
    public void saveExceptionDataInCouch(final UserInfo loggedInUser, final Exception e) {
        if (taskExecutor != null && e != null) {
            BaseLoggers.eventLogger.info("Saving data in couch db asynchronously");
            taskExecutor.execute(new MdcRetainingRunnable() {

                @Override
                public void runWithMdc() {
                    String transactionId = StringUtils.isNotBlank(MDC.get("UUID")) ? MDC.get("UUID")
                            : "CASTXN-ID_NOT_AVAILABLE";
                    if (!removeCurrentExceptionFromLoggingInCouch(loggedInUser, e)) {
                        ExceptionVO exceptionVO = new ExceptionVO();

                        setExceptionDetails(e, exceptionVO);
                        setTimeDatails(exceptionVO);
                        setServerDetails(exceptionVO);

                        exceptionVO.setCasTransactionId(transactionId);
                        if (loggedInUser != null && loggedInUser.getUserReference() != null
                                && loggedInUser.getUserReference().getEntityId() != null) {
                            exceptionVO.setLoggedInUserUri(loggedInUser.getUserReference().getEntityId().getUri());
                        }

                        exceptionVORepository.saveExceptionObject(exceptionVO);
                    } else {
                        BaseLoggers.flowLogger.debug("Escaping encountered exception's save in couch db..");
                    }
                }


            });
        }
    }

    @Override
    public void saveDebuggingDataOfException(UserInfo loggedInUser, HttpServletRequest request, Exception e) {
        if (taskExecutor != null && e != null) {
            BaseLoggers.eventLogger.info("Saving data in couch db asynchronously");
            final ExceptionVO exceptionVO = new ExceptionVO();
            setHTTPDetails(request, exceptionVO);
            taskExecutor.execute(new MdcRetainingRunnable() {

                @Override
                public void runWithMdc() {
                    String transactionId = StringUtils.isNotBlank(MDC.get("UUID")) ? MDC.get("UUID")
                            : "CASTXN-ID_NOT_AVAILABLE";
                    if (!removeCurrentExceptionFromLoggingInCouch(loggedInUser, e)) {

                        setExceptionDetails(e, exceptionVO);
                        setTimeDatails(exceptionVO);
                        setServerDetails(exceptionVO);

                        exceptionVO.setCasTransactionId(transactionId);

                        if (loggedInUser != null && loggedInUser.getUserReference() != null
                                && loggedInUser.getUserReference().getEntityId() != null) {
                            exceptionVO.setLoggedInUserUri(loggedInUser.getUserReference().getEntityId().getUri());
                        }

                        exceptionVORepository.saveExceptionObject(exceptionVO);
                    } else {
                        BaseLoggers.flowLogger.debug("Escaping encountered exception's save in couch db..");
                    }
                }
            });
        }
    }

    private void setHTTPDetails(HttpServletRequest request, ExceptionVO exceptionVO) {
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        filterMapOfRedundantEntries(requestParameterMap);
        exceptionVO.setRequestParameters(JsonUtils.serializeWithoutLazyInitialization(requestParameterMap));
        exceptionVO.setRequestURI(request.getRequestURI());
        exceptionVO.setFunctionalParameter(request.getHeader("applicationId"));
    }

    private void filterMapOfRedundantEntries(Map<String,String[]> requestParameterMap) {
        if(requestParameterMap!=null) {
            requestParameterMap.remove("_hkstd");
            requestParameterMap.remove("_");
        }
    }

    protected void setServerDetails(ExceptionVO exceptionVO) {
        try {
            exceptionVO.setNode(InetAddress.getLocalHost() != null ? InetAddress.getLocalHost().getHostName() : null);
        } catch (UnknownHostException e1) {
            exceptionLogger.error("Exception :",e1);
        }
    }

    protected void setTimeDatails(ExceptionVO exceptionVO) {
        DateTime dateTime = DateUtils.getCurrentUTCTime();
        String exceptionOccuredTimestamp = dateTime.toString();

        exceptionVO.setExceptionOccuredDate(new DateTime().millisOfDay().withMinimumValue()
                .toDateTime(DateTimeZone.UTC).toString());
        exceptionVO.setExceptionOccuredTimestamp(exceptionOccuredTimestamp);
    }

    @Override
    public List<ExceptionVO> getAllExceptionVOs() {
    	return exceptionVORepository.getAll();
    }

    @Override
    public ExceptionVO getExceptionById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return exceptionVORepository.getExceptionById(id);
    }

    @Override
    public boolean deleteExceptionsByDays(int days) {
        if (days == 0) {
            return false;
        }
        return exceptionVORepository.deleteExceptionOnBasisOfDays(days);
    }

    @Override
    public Set<String> getUniqueExceptionTypeList() {
    	return exceptionVORepository.findUniqueExceptionTypes();
    }

    @Override
    public List<ExceptionVO> getExceptionsBetweenDates(DateTime startDate, DateTime endDate, String node) {
        List<ExceptionVO> exceptionByDateList = new ArrayList<ExceptionVO>();
        if (startDate != null && endDate != null) {
            if(node == null || node.isEmpty()){
                exceptionByDateList = exceptionVORepository.findExceptionsBetweenDates(startDate, endDate);
            }
            else{
                exceptionByDateList = exceptionVORepository.findExceptionsBetweenDates(startDate, endDate,node);
            }
        }

        return exceptionByDateList;

    }

    @Override
    public List<ExceptionVO> getExceptionsByType(String exceptionType, String node) {
        List<ExceptionVO> exceptionByType = new ArrayList<ExceptionVO>();
        if (StringUtils.isEmpty(exceptionType)) {
            return exceptionByType;
        }
        if(node == null || node.isEmpty()){
            exceptionByType = exceptionVORepository.findByExceptionType(exceptionType);
        }
        else{
            exceptionByType = exceptionVORepository.findByExceptionType(exceptionType,node);
        }
        return exceptionByType;

    }

    @Override
    public List<ExceptionVO> getExceptionsByDateAndType(DateTime exceptionOccuredTimestamp, String exceptionType) {
        List<ExceptionVO> exceptionsByDateAndType = new ArrayList<ExceptionVO>();
        if (StringUtils.isEmpty(exceptionType) && exceptionOccuredTimestamp == null) {
            return exceptionsByDateAndType;
        }

        String exceptionDate = DateUtils.getFormattedDate(exceptionOccuredTimestamp, DEFAULT_DATE_FORMAT + SPACE_STRING
                + DEFAULT_TIME_FORMAT);
        exceptionsByDateAndType = exceptionVORepository.findExceptionsByDateAndType(exceptionDate, exceptionType);
        return exceptionsByDateAndType;

    }

    @Override
    public List<ExceptionVO> getExceptionsFromUserURI(String userURI,String node) {
        List<ExceptionVO> exceptionByUserURI = new ArrayList<ExceptionVO>();
        if (StringUtils.isEmpty(userURI)) {
            return exceptionByUserURI;
        }
        if(node == null || node.isEmpty())
            exceptionByUserURI = exceptionVORepository.findByLoggedInUserUri(userURI);
        else
            exceptionByUserURI = exceptionVORepository.findByLoggedInUserUri(userURI,node);
        return exceptionByUserURI;
    }

    @Override
    public Set<String> getUniqueUserURIList() {
    	return exceptionVORepository.findUniqueUserUri();
    }

    @Override
    public void getUniqueNode(List<String> nodeList) {
        exceptionVORepository.findUniqueNode(nodeList);
    }

    @Override
    public void getExceptionsFromNode(List<ExceptionVO> exceptionsList, String node) {
        exceptionVORepository.getExceptionsFromNode(exceptionsList, node);
    }

    @Override
    public void saveIgnoredException(String ignoredExcept) {
        exceptionVORepository.saveIgnoredException(ignoredExcept);
    }

    @Override
    public void getIgnoredException(StringBuilder ignoredExcept) {
        exceptionVORepository.getIgnoredException(ignoredExcept);
    }

    protected void setExceptionDetails(Exception e, ExceptionVO exceptionVO) {

        exceptionVO.setStackTrace(ExceptionUtils.getFullStackTrace(e));
        exceptionVO.setExceptionType(e.getClass().getName());

        StackTraceElement[] stackTraceElement = e.getStackTrace();

        String fileName = "";
        String methodName = "";
        String className = "";

        if (stackTraceElement != null && stackTraceElement.length > 0) {
            fileName = stackTraceElement[0].getFileName();
            methodName = stackTraceElement[0].getMethodName();
            className = stackTraceElement[0].getClassName();
        }

        exceptionVO.setFileName(fileName);
        methodName = StringUtils.isEmpty(methodName)?methodName:methodName.replaceAll("[<>_]","");
        exceptionVO.setMethodName(methodName);
        exceptionVO.setClassName(className);

    }

    @Override
    public List getUniqueExceptionData(String node, String exceptionType) {
        return exceptionVORepository.getUniqueExceptionData(node, exceptionType);
    }

    @Override
    public List getExceptionSummary(String node) {
        return exceptionVORepository.getExceptionSummary(node);
    }

    @Override
    public List getExceptionSummaryGroup(List nodeList) {
        return exceptionVORepository.getExceptionSummaryGroup(nodeList);
    }

    @Override
    public Map fetchExceptionsFromDatesAndParameterValue(Map fieldData, Map datatableMap) {
        return exceptionVORepository.fetchExceptionsFromDatesAndParameterValue(fieldData, datatableMap);
    }

    private boolean removeCurrentExceptionFromLoggingInCouch(UserInfo loggedInUser, Exception ex) {
        String currentExceptionType = ex.getClass().getSimpleName();
        boolean removeCurrentExceptionFromLogging = false;

        if (loggedInUser == null || loggedInUser.getUserPreferences() == null) {
            return removeCurrentExceptionFromLogging;
        }

        ConfigurationVO confVO = loggedInUser.getUserPreferences().get("config.exceptions.remove.log");

        if (confVO == null || confVO.getText() == null || StringUtils.isEmpty(confVO.getText())) {
            return removeCurrentExceptionFromLogging;
        }

        String exceptionsToBeExcludedFromLog = confVO.getText();
        String[] exceptionsToBeExcluded = exceptionsToBeExcludedFromLog.split(",");
        if (exceptionsToBeExcluded.length > 0) {
            for (String indvException : exceptionsToBeExcluded) {
                indvException = indvException.trim();
                if (indvException.equalsIgnoreCase(currentExceptionType)) {
                    removeCurrentExceptionFromLogging = true;
                    break;
                }
            }
        }

        return removeCurrentExceptionFromLogging;
    }

}
