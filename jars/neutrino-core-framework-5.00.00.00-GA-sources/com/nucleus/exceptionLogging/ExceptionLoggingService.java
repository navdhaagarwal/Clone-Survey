/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.exceptionLogging;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.nucleus.user.UserInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Nucleus Software India Pvt Ltd 
 * The Interface ExceptionLoggingService.
 */
public interface ExceptionLoggingService {

    /**
     * Save exception data in couch.
     *
     * @param e the e
     * @return the string
     */
    public void saveExceptionDataInCouch(UserInfo loggedInUser, Exception e);

    /**
     * Save exception data in couch.
     *
     * @param e the e
     * @return the string
     */
    public void saveDebuggingDataOfException(UserInfo loggedInUser, HttpServletRequest request, Exception e);

    /**
     * Gets the exceptions by type.
     *
     * @param exceptionType the exception type
     * @return the exceptions by type
     */
    public List<ExceptionVO> getExceptionsByType(String exceptionType, String node);

    /**
     * Gets the exceptions by date.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return the exceptions by date
     */
    public List<ExceptionVO> getExceptionsBetweenDates(DateTime startDate, DateTime endDate, String node);

    /**
     * Gets the exceptions by date and type.
     * This can also be done using above two methods - find the result by running both the above 
     * queries and then take the intersection of their results.
     *
     * @param exceptionOccuredTimestamp the exception occured timestamp
     * @param exceptionType the exception type
     * @return the exceptions by date and type
     */
    public List<ExceptionVO> getExceptionsByDateAndType(DateTime exceptionOccuredTimestamp, String exceptionType);

    /**
     * Gets the all exception vos.
     *
     * @return the all exception vos
     */
    public List<ExceptionVO> getAllExceptionVOs();

    /**
     * Gets the exception by id.
     *
     * @param id the id
     * @return the exception by id
     */
    public ExceptionVO getExceptionById(String id);

    /**
     * Gets the unique exception type list.
     *
     * @return the unique exception type list
     */
    public Set<String> getUniqueExceptionTypeList();

    /**
     * Delete exceptions by days.
     *
     * @param days the days
     * @return the list
     */
    public boolean deleteExceptionsByDays(int days);

    /**
     * Gets the exceptions from user uri.
     *
     * @param userURI the user uri
     * @return the exceptions from user uri
     */
    public List<ExceptionVO> getExceptionsFromUserURI(String userURI,String node);

    /**
     * Gets the unique user uri list.
     *
     * @return the unique user uri list
     */
    public Set<String> getUniqueUserURIList();

    public void getUniqueNode(List<String> nodeList);

    public void getExceptionsFromNode(List<ExceptionVO> exceptionsList, String node);

    public void saveIgnoredException(String ignoredExcept);

    public void getIgnoredException(StringBuilder ignoredExcept);

    Map fetchExceptionsFromDatesAndParameterValue(Map fieldData, Map datatableMap);

    List getExceptionSummary(String node);
    List getExceptionSummaryGroup(List nodeList);

    List getUniqueExceptionData(String node, String exceptionType);
}
