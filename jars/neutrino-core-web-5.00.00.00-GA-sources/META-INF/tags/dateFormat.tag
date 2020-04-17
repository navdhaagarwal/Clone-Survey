<%@tag import="org.joda.time.LocalDate"%>
<%@ tag import="org.joda.time.DateTime" %>
<%@ tag import="java.util.Date" %>
<%@ tag import="java.lang.Object"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>

<%@ attribute name="value" type="java.lang.Object" required="true"%>
<%@ attribute name="datePattern"%>
<%@ attribute name="timePattern"%>
<%@ attribute name="type" %>


<%
Object dateValue = (Object) jspContext.getAttribute("value");
if((dateValue instanceof DateTime)){
    if(dateValue != null){
        jspContext.setAttribute("dateValueToFormat",((DateTime)dateValue).toDate());
    }    
   }
else if(dateValue instanceof LocalDate ){
    if(dateValue != null){
        jspContext.setAttribute("dateValueToFormat",((LocalDate)dateValue).toDate());
    }    
   }
else if(dateValue instanceof Date ) {
    if(dateValue != null){
        jspContext.setAttribute("dateValueToFormat", dateValue);
    }    
   }
else{
    if(dateValue != null){
        throw new IllegalArgumentException();
    }    
   
}

%> 
<c:if test="${empty datePattern}">
<c:set var="dateFormat" value="${neutrino:binder('currentUserDateFormat')}"></c:set>
</c:if>
<c:if test="${not empty datePattern}">
<c:set var="dateFormat" value="${datePattern}"></c:set>
</c:if>

<c:if test="${empty timePattern}">
<c:set var="timeFormat" value="hh:mm:ss a"></c:set>
</c:if>
<c:if test="${not empty timePattern}">
<c:set var="timeFormat" value="${timePattern}"></c:set>
</c:if>

<c:if test="${empty type}">
<c:set var="type" value="both"></c:set>
</c:if>

<fmt:setLocale value = "en_US" scope="session"  />

<c:if test="${type eq 'date'}">
<fmt:formatDate type="date" value="${dateValueToFormat}" pattern="${dateFormat}" />
</c:if>
<c:if test="${type eq 'time'}">
<fmt:formatDate type="time" value="${dateValueToFormat}" pattern="${timeFormat}" />
</c:if>

<c:if test="${type eq 'both'}">
<fmt:formatDate type="date" value="${dateValueToFormat}" pattern="${dateFormat}" />
<fmt:formatDate type="time" value="${dateValueToFormat}" pattern="${timeFormat}"/>
</c:if>