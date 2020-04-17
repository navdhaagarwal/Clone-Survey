package com.nucleus.exceptionLogging;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.core.misc.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Controller
@RequestMapping(value = "/ExceptionMonitoring")
public class ExceptionMonitoringController extends BaseController {

    @Inject
    @Named("exceptionLoggingService")
    private ExceptionLoggingService exceptionLoggingService;

    @Inject
    @Named("userService")
    private UserService             userService;
    
    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/loadExceptionGridPage")
    public String loadExceptionGridPage(ModelMap map) {
        List<String> nodeList = new ArrayList<String>();
        exceptionLoggingService.getUniqueNode(nodeList);
        Map<String,String> nodes = new HashMap<>();
        for (String node:nodeList){
            nodes.put(node,node);
        }
        map.put("nodes", nodes);
        StringBuilder ignoredExceptions = new StringBuilder();
        exceptionLoggingService.getIgnoredException(ignoredExceptions);
        map.put("ignoredExceptions", ignoredExceptions);
        map.put("basedOnFilter", ExceptionLoggingConstants.BASED_ON_FILTER);
        map.put("daysPermissible", ExceptionLoggingConstants.DAYS_LIST);
        return "exceptionGridMainPage";
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/exceptionSummary")
    public String loadExceptionSummaryPage(ModelMap map) {

        List<String> nodeList = new ArrayList<String>();
        exceptionLoggingService.getUniqueNode(nodeList);
        Map<String,String> nodes = new HashMap<>();
        for (String node:nodeList){
            nodes.put(node,node);
        }
        map.put("nodes", nodes);
        return "exceptionSummary";
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/loggedExceptionSummary")
    public String loadLoggedExceptionSummaryPage() {

        return "loggedExceptionSummary";
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/loadExceptionSummaryData")
    public @ResponseBody Map loadExceptionSummaryGridPage(ModelMap map, HttpServletRequest request,
                                                          @RequestParam(value = "node", required = false) String node) {
        List list = exceptionLoggingService.getExceptionSummary(node);
        if(CollectionUtils.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                Map exceptionMap = (Map) list.get(i);
                String exceptionTypeName = (String) exceptionMap.get("exceptionType");
                try {
                    Class<?> exceptionTypeClass = Class.forName(exceptionTypeName);
                    if (exceptionTypeClass != null) {
                        exceptionMap.put("exceptionName", exceptionTypeClass.getSimpleName());
                    }
                } catch (ClassNotFoundException cnfe) {
                    BaseLoggers.bugLogger.debug("No exception class found for " + exceptionTypeName);
                    BaseLoggers.bugLogger.debug("Setting default exception class to " + exceptionTypeName);
                }
            }
        }

        Map jsonData = new HashMap();

        jsonData.put("aaData", list);

        String jsonString = new JSONSerializer()
                .exclude("*.class").include("aaData.exceptionName","aaData.exceptionType", "aaData.node", "aaData.count").exclude("*")
                .deepSerialize(jsonData);

        jsonData = (HashMap<String, Object>) new JSONDeserializer().deserialize(jsonString);

        return jsonData;
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/loadExceptionsDataGrid")
    public String loadExceptionDataGridPage() {
        return "exceptionLoggingGridPage";
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/loadExceptionsDataGridSummary")
    public String loadExceptionDataGridPageSummary(ModelMap map, HttpServletRequest request,
                                            @RequestParam(value = "node", required = false) String node,
                                            @RequestParam(value = "exceptionType", required = false) String exceptionType,
                                            @RequestParam(value = "count", required = false) String count) {
        map.put("node",node);
        map.put("exceptionType",exceptionType);
        map.put("count",count);
        map.put("summary",request.getParameter("summary"));


        return "exceptionLoggingGridPageSummary";
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/loadUniqueExceptionData")
    public @ResponseBody Map loadUniqueExceptionSummary(ModelMap map, HttpServletRequest request,
                                                        @RequestParam(value = "node", required = false) String node,
                                                        @RequestParam(value = "exceptionType", required = false) String exceptionType) {

        List list = exceptionLoggingService.getUniqueExceptionData(node,exceptionType);

        Map jsonData = new HashMap();
        jsonData.put("aaData", list);
        String jsonString = new JSONSerializer()
                .exclude("*.class")
                .include("aaData.id", "aaData.casTransactionId", "aaData.className", "aaData.fileName",
                        "aaData.exceptionType", "aaData.methodName", "aaData.exceptionOccuredTimestamp","aaData.node").exclude("*")
                .deepSerialize(jsonData);

        jsonData = (HashMap<String, Object>) new JSONDeserializer().deserialize(jsonString);

        return jsonData;
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RequestMapping(value = "/loadLoggedExceptionsData")
    public @ResponseBody
    Map loadLoggedExceptionsData(ModelMap map, HttpServletRequest request,
                                 @RequestParam(value = "fromDate", required = false) String fromDateTime,
                                 @RequestParam(value = "toDate", required = false) String toDateTime,
                                 @RequestParam(value = "searchParameter", required = false) String searchParameter,
                                 @RequestParam(value = "searchParamValue", required = false) String searchParamValue,
                                 @RequestParam(value = "node", required = false) String node,
                                 @RequestParam(value = "casTransactionId", required = false) String casTransactionId,
                                 @RequestParam(value = "iDisplayStart", required = false) Integer iDisplayStart,
                                 @RequestParam(value = "iDisplayLength", required = false) Integer iDisplayLength,
                                 @RequestParam(value = "sEcho", required = false) Integer sEcho,
                                 @RequestParam(value = "sSearch", required = false) String sSearch){

        DateTime fromDate = null;
        DateTime toDate = null;
        String fromTime = request.getParameter("fromTime");
        String toTime = request.getParameter("toTime");
        if(fromDateTime != null && StringUtils.isNotEmpty(fromDateTime)){
            if(fromTime != null && StringUtils.isNotEmpty(fromTime))
                fromDate = DateTime.parse(fromDateTime, DateTimeFormat.forPattern(getUserDateFormat()+" "+DateUtils.ALTERNATE_TIME_FORMAT));
            else
                fromDate = DateTime.parse(fromDateTime, DateTimeFormat.forPattern(getUserDateFormat()));
        }
        if(toDateTime != null && StringUtils.isNotEmpty(toDateTime)){
            if(toTime != null && StringUtils.isNotEmpty(toTime))
                toDate = DateTime.parse(toDateTime, DateTimeFormat.forPattern(getUserDateFormat()+" "+DateUtils.ALTERNATE_TIME_FORMAT)).plusMinutes(1);
            else
                toDate = DateTime.parse(toDateTime, DateTimeFormat.forPattern(getUserDateFormat())).plusDays(1).minusMillis(1);
        }
        if(fromDate != null && toDate == null){
            toDate = new DateTime();
        }
        Boolean summary = Boolean.parseBoolean(request.getParameter("summary"));
        String exceptionType = request.getParameter("exceptionType");
        int iSortCol_0 = Integer.parseInt(request.getParameter("iSortCol_0"));
        int col = (iSortCol_0== 0) ? 1 : iSortCol_0;
        String sortColumnName = request.getParameter("mDataProp_"+col);
        String sortColumnDirection = request.getParameter("sSortDir_0");
        List<ExceptionVO> exceptionsList = new ArrayList<ExceptionVO>();
        Map datatableMap = new HashMap();
        Map result = new HashMap();
        datatableMap.put("sortColumnName", sortColumnName);
        datatableMap.put("sortColumnDirection", sortColumnDirection);
        datatableMap.put("iDisplayStart", iDisplayStart);
        datatableMap.put("iDisplayLength", iDisplayLength);
        datatableMap.put("sEcho", sEcho);
        datatableMap.put("sSearch", sSearch);


        Map fieldData = new HashMap();

        if(summary){
            fieldData.put("node",node);
            fieldData.put("exceptionType",exceptionType);
            fieldData.put("summary",summary);

        }
        else {

            fieldData.put("searchParameter",searchParameter);
            fieldData.put("searchParamValue",searchParamValue);
            fieldData.put("toDate", toDate);
            fieldData.put("fromDate",fromDate);
            fieldData.put("node",node);
            fieldData.put("casTransactionId",casTransactionId);

        }


        result = exceptionLoggingService.fetchExceptionsFromDatesAndParameterValue(fieldData, datatableMap);
        exceptionsList = (List<ExceptionVO>)result.get("exceptionVoList");

        if(CollectionUtils.isNotEmpty(exceptionsList)){
            for (ExceptionVO currException : exceptionsList) {

                if(currException != null && StringUtils.isNotEmpty(currException.getClassName())){
                    String className = currException.getClassName();
                    try {
                        Class<?> actualClass = Class.forName(className);
                        if (actualClass != null) {
                            String simpleName = actualClass.getSimpleName();
                            if(currException.getFileName() != null)
                                currException.setClassName(simpleName.equals("")?currException.getFileName().split("\\.")[0]:simpleName);
                        }
                    } catch (ClassNotFoundException ex) {
                        BaseLoggers.bugLogger.debug("No class found for " + currException.getClassName());
                        BaseLoggers.bugLogger.debug("Setting default name class to " + currException.getClassName());
                    }
                }

                if(currException != null && StringUtils.isNotEmpty(currException.getExceptionType())){
                    String exceptionTypeName = currException.getExceptionType();
                    try {
                        Class<?> exceptionTypeClass = Class.forName(exceptionTypeName);
                        if (exceptionTypeClass != null) {
                            currException.setExceptionType(exceptionTypeClass.getSimpleName());
                        }
                    } catch (ClassNotFoundException cnfe) {
                        BaseLoggers.bugLogger.debug("No exception class found for " + currException.getClassName());
                        BaseLoggers.bugLogger.debug("Setting default excpetion class to " + currException.getClassName());
                    }
                }
            }
        }else{
            exceptionsList = new ArrayList<>();
        }


        Map jsonData = new HashMap();
        jsonData.put("iTotalRecords", result.get("iTotalRecords"));
        jsonData.put("iTotalDisplayRecords", result.get("iTotalRecords"));
        jsonData.put("aaData", exceptionsList);

        String jsonString = new JSONSerializer()
                .exclude("*.class")
                .include("iTotalRecords", "iTotalDisplayRecords", "aaData.id", "aaData.casTransactionId", "aaData.className", "aaData.fileName", "aaData.functionalParameter",
                        "aaData.exceptionType", "aaData.methodName", "aaData.exceptionOccuredTimestamp","aaData.node").exclude("*")
                .deepSerialize(jsonData);

        jsonData = (HashMap<String, Object>) new JSONDeserializer().deserialize(jsonString);

        return jsonData;
    }

    private List<ExceptionVO> getFilteredList(DateTime fromDate, DateTime toDate, String searchParam, String searchParamValue, String node) {
        List<ExceptionVO> exceptionsList = null;

        if ((fromDate != null  && (searchParamValue == null || searchParamValue.isEmpty()))) {
            if (toDate == null) {
                toDate = new DateTime();
            }
            List<ExceptionVO> exceptionsListFromDates = exceptionLoggingService.getExceptionsBetweenDates(fromDate, toDate, node);
            exceptionsList = exceptionsListFromDates;
        } else if (fromDate == null && toDate == null && (searchParamValue != null && !searchParamValue.isEmpty())) {
                 exceptionsList = fetchExceptionsFromSearchParameterOnly(searchParam, searchParamValue, node);
        }
        else if ((fromDate != null || toDate != null) && searchParamValue != null && !searchParamValue.isEmpty()) {
            if (toDate == null) {
                toDate = new DateTime();
            }
            exceptionsList = fetchExceptionsFromDatesAndParameterValue(searchParam, searchParamValue, toDate, fromDate, node);
        }

        return exceptionsList;
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/showExceptionTrace")
    public String showExceptionTrace(ModelMap map, @RequestParam("couchDbId") String couchDbId) {
        ExceptionVO exceptionVO = exceptionLoggingService.getExceptionById(couchDbId);

        //StackTraceElement[] stackTraceElements = exceptionVO.getStackTraceElements();
       /* if (stackTraceElements != null && stackTraceElements.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement s : exceptionVO.getStackTraceElements()) {
                builder.append(s.toString());
                builder.append(SystemPropertyUtils.getNewline());
            }
            builder.toString();
            map.put("builderStackTrace", builder);
        }*/
        if(null != exceptionVO.getStackTrace())
        {
            map.put("builderStackTrace", exceptionVO.getStackTrace());
        }

        map.put("exceptionVO", exceptionVO);
        return "exceptionTraceDetail";
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/discardExceptionsFromDays")
    public @ResponseBody
    boolean discardExceptionsFromDays(@RequestParam(value = "noOfDays") Integer noOfDays) {
        if (noOfDays != null) {
            exceptionLoggingService.deleteExceptionsByDays(noOfDays);
            return true;
        } else {
            return false;
        }
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/saveIgnoredException")
    public @ResponseBody
    void saveIgnoredException(@RequestParam(value = "ignoredExcept") String ignoredExcept) {
            exceptionLoggingService.saveIgnoredException(ignoredExcept);
    }

    @PreAuthorize("hasAuthority('AUTHORITY_EXCEPTION_MONITORING')")
    @RequestMapping(value = "/getBasedOnFilterValues/{parentCode}")
    public @ResponseBody
    String listLoans(@PathVariable String parentCode, ModelMap map) {

        List<Map<String, ?>> par = new ArrayList<Map<String, ?>>();
        Map<String, String> valueMap = new HashMap<String, String>();
        if (parentCode != null && (!parentCode.isEmpty())) {
            if (parentCode.equals(ExceptionLoggingConstants.USER_BASED_FILTER)) {
                Set<String> filterSubList = exceptionLoggingService.getUniqueUserURIList();
                for (String indvUserURI : filterSubList) {
                	if(indvUserURI != null){
                		String username = userService.getUserNameByUserUri(indvUserURI);
                        valueMap.put(indvUserURI, username);
                	}                    
                }
                par.add(valueMap);
            }
            if (parentCode.equals(ExceptionLoggingConstants.EXCEPTION_BASED_FILTER)) {
                Set<String> filterSubListOfExceptions = exceptionLoggingService.getUniqueExceptionTypeList();
                if(CollectionUtils.isNotEmpty(filterSubListOfExceptions))
                {
                for (String indvExceptionURI : filterSubListOfExceptions) {
                    try {
                        Class<?> exceptionTypeClass = Class.forName(indvExceptionURI);
                        valueMap.put(indvExceptionURI, exceptionTypeClass.getSimpleName());
                    } catch (ClassNotFoundException ex) {
                        valueMap.put(indvExceptionURI, indvExceptionURI);
                    }
                }
                }
                par.add(valueMap);
            }
        }
        JSONSerializer iSerializer = new JSONSerializer();
        return iSerializer.serialize(valueMap);

    }

    private List<ExceptionVO> fetchExceptionsFromSearchParameterOnly(String searchParam, String searchParamValue,String node) {
        if (searchParam != null && searchParamValue != null) {
            if (searchParam.equals(ExceptionLoggingConstants.EXCEPTION_BASED_FILTER)) {
                List<ExceptionVO> exceptionsListFromExceptionType = exceptionLoggingService
                        .getExceptionsByType(searchParamValue,node);
                return exceptionsListFromExceptionType;
            } else if (searchParam.equals(ExceptionLoggingConstants.USER_BASED_FILTER)) {
                List<ExceptionVO> exceptionsListFromUserTypes = exceptionLoggingService
                        .getExceptionsFromUserURI(searchParamValue,node);
                return exceptionsListFromUserTypes;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private List<ExceptionVO> fetchExceptionsFromDatesAndParameterValue(String searchParam, String searchParamValue,
            DateTime toDate, DateTime fromDate, String node) {
        List<ExceptionVO> exceptionsListFromDates = exceptionLoggingService.getExceptionsBetweenDates(fromDate, toDate,node);
        List<ExceptionVO> exceptionsListFromParameterValue = null;
        List<ExceptionVO> exceptionsList = null;

        if (searchParam != null && !searchParam.isEmpty()) {
            if (searchParam.equals(ExceptionLoggingConstants.EXCEPTION_BASED_FILTER)) {
                List<ExceptionVO> exceptionsListFromExceptionType = exceptionLoggingService
                        .getExceptionsByType(searchParamValue,node);
                exceptionsListFromParameterValue = exceptionsListFromExceptionType;
            } else if (searchParam.equals(ExceptionLoggingConstants.USER_BASED_FILTER)) {
                List<ExceptionVO> exceptionsListFromUserTypes = exceptionLoggingService
                        .getExceptionsFromUserURI(searchParamValue,node);
                exceptionsListFromParameterValue = exceptionsListFromUserTypes;
            }
        }

        List<ExceptionVO> intersectedListFromDateAndParameterValue = new ArrayList<ExceptionVO>();
        for (ExceptionVO currException : exceptionsListFromDates) {
            if (exceptionsListFromParameterValue.contains(currException)) {
                intersectedListFromDateAndParameterValue.add(currException);
            }
        }
        exceptionsList = intersectedListFromDateAndParameterValue;
        return exceptionsList;
    }

}
