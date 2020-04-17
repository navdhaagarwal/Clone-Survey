package com.nucleus.exceptionLogging;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExceptionVORepository {

    public abstract List<ExceptionVO> findByExceptionType(String exceptionType);

    public abstract Set<String> findUniqueExceptionTypes();

    public abstract List<ExceptionVO> findByLoggedInUserUri(String loggedInUserUri);

    public abstract Set<String> findUniqueUserUri();

    public abstract List<ExceptionVO> findByMethodName(String methodName);

    public abstract List<ExceptionVO> findByClassName(String className);

    public abstract List<ExceptionVO> findByExceptionOccuredTimestamp(String exceptionOccuredTimestamp);

    public abstract List<ExceptionVO> findByExceptionOccuredDate(String exceptionOccuredDate);

    public abstract List<ExceptionVO> findExceptionsByDateAndType(String exceptionOccuredTimestamp, String exceptionType);

    public abstract void saveExceptionObject(ExceptionVO exceptionVO);

    public abstract ExceptionVO getExceptionById(String exceptionVOId);

    public abstract List<ExceptionVO> findExceptionsBetweenDates(DateTime startDate, DateTime endDate);

    public abstract boolean deleteExceptionOnBasisOfDays(int days);
    
    public abstract void truncateAllExceptionsInDb();

    default public void findUniqueNode(List<String> nodeList){

    }

    default public void getExceptionsFromNode(List<ExceptionVO> exceptionsList, String node){

    }

    default public void saveIgnoredException(String ignoredExcept){

    }

    default public void getIgnoredException(StringBuilder ignoredExcept){

    }

    public abstract List<ExceptionVO> findByExceptionType(String exceptionType,String node);
    public abstract List<ExceptionVO> findByLoggedInUserUri(String loggedInUserUri,String node);
    public abstract List<ExceptionVO> findExceptionsBetweenDates(DateTime startDate, DateTime endDate,String node);

    List<ExceptionVO> getAll();

    default Map fetchExceptionsFromDatesAndParameterValue(Map fieldData, Map datatableMap){
        return null;
    }

    default List getExceptionSummary(String node){
        return null;
    }
    default List getExceptionSummaryGroup(List nodeList){
        return null;
    }

    default List getUniqueExceptionData(String node, String exceptionType){
        return null;
    }
}