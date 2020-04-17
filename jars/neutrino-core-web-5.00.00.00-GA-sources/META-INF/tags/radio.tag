<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="disabled"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="name"%>
<%@ attribute name="path"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="validators"%>
<%@ attribute name="checked"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="value"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="selectedValue" required="true"%>
<%@ attribute name="onClickEvent"%>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="conditionStatement"%>
<%@ attribute name="conditionValue"%>
<%@ attribute name="addClass"%>

<!--  'selectedValue' attribute is made mandatory so that radio button is properly rendered in view and edit mode
		when 'name' is used instead of 'path'  -->

<div>

	<%
	    String name = (String) jspContext.getAttribute("name");
	    String path = (String) jspContext.getAttribute("path");
 
	    if (name == null && path == null) {
	        throw new SystemException("Either of attributes 'name' or 'path' must be specified");
	    } else if (name != null && path != null) {
	        throw new SystemException("Either of attributes 'name' or 'path' can be specified at once");
	    }
	%>
<c:if test="${not empty conditionStatement}">
    <c:set var = "statementList" value = "${fn:split(conditionStatement, ';')}" scope="page" />
    <c:set var = "paramList" value = "${fn:split(conditionValue, ';')}" scope="page" />
    <c:forEach var = "statement" items="${statementList}" begin="0" varStatus="i" step="1">
        <c:if test="${fn:trim(statement)}">
            <c:set var = "conditionList" value = "${fn:split(paramList[i.index], ',')}" scope="page" />
            <c:forEach var="condition" items="${conditionList}">
                <c:set var = "conditionParams" value = "${fn:split(condition, '=')}" scope="page" />
                <c:if test="${fn:trim(conditionParams[0]) eq 'mandatory'}">
                    <c:set var = "mandatory" value = "${fn:replace(fn:trim(conditionParams[1]),'false','')}" scope="page" />
                </c:if>
                <c:if test="${fn:trim(conditionParams[0]) eq 'readOnly'}">
                    <c:set var = "readOnly" value = "${fn:replace(fn:trim(conditionParams[1]),'false','')}" scope="page" />
                </c:if>
            </c:forEach>
        </c:if>
    </c:forEach>
</c:if>
	<c:if test="${not empty tooltipKey}">
		<c:set var="tooltipMessage" scope="page">
			<spring:message code="${tooltipKey}"></spring:message>
		</c:set>
	</c:if>

	<c:set var="selectedValue" value="${selectedValue}"></c:set>

	<c:if test="${not empty viewMode}">
		<c:if test="${viewMode eq true}">
			<c:set var="disabled" value="${viewMode}" scope="page" />
			<c:set var="disabled1" value="disabled" scope="page" />
			<c:set var="readonly1" value="readOnly" scope="page" />
			<c:set var="tooltipKey" value="" scope="page" />
			<c:set var="validators" value="" scope="page" />
		</c:if>
	</c:if>
	<c:set var="requiredClass" value="" scope="page" />
	<c:if test="${mandatory eq true}">
		<c:set var="requiredClass" value="required" scope="page" />
	</c:if>

	<c:if test="${not empty labelKey}">
		<c:choose>
			<c:when test="${not empty path}">
				<spring:bind path="${path}">
					<c:set var="preEvalValue" value="${status.value}"></c:set>
				</spring:bind>
				<label class="radio"> <form:radiobutton path="${path}"
						cssClass="${validators} ${addClass} uni_style ${requiredClass}" id="${id}"
						 onclick="${onClickEvent}"
						disabled="${disabled}" readonly="${readOnly}" checked="${checked}"
						value="${value}" tabindex="${tabindex}" /> <spring:message
						code="${labelKey}
						"></spring:message>
						<%--  <c:if
						test="${not empty mandatory}">
						<span class="Mandatory" style="color: red">*</span>
					</c:if> --%> 
					
					<c:if test="${not empty tooltipMessage}">
						<a rel="tooltip" href="javascript:void(0)" data-original-title="${tooltipMessage}"><i
							id="<c:out value='${id}' />-help-icon" class="glyphicon glyphicon-question-sign"></i></a>
					</c:if>
				</label>
			</c:when>
			<c:otherwise>
				<label class="radio"> <c:if test="${selectedValue eq value}">
						<input type="radio" name="<c:out value='${name}' />" onclick="${onClickEvent}" class="<c:out value='${validators} ${addClass}' /> uni_style ${requiredClass}"
							id="<c:out value='${id}' />" ${disabled1} selectedValue="<c:out value='${selectedValue}' />"
							${readonly1} checked="checked" value="<c:out value='${value}' />"
							tabindex="<c:out value='${tabindex}' />" />
					</c:if> <c:if test="${selectedValue ne value}">
						<input type="radio" name="<c:out value='${name}' />" onclick="${onClickEvent}" class="<c:out value='${validators} ${addClass}' /> uni_style ${requiredClass}"
							id="<c:out value='${id}' />" ${disabled1} selectedValue="<c:out value='${selectedValue}' />"
							${readonly1} value="<c:out value='${value}' />" tabindex="<c:out value='${tabindex}' />" />
					</c:if> <spring:message code="${labelKey}" /> 
					<%-- <c:if
						test="${not empty mandatory}">
						<span class="Mandatory" style="color: red">*</span>
					</c:if> --%>
					
					 <c:if test="${not empty tooltipMessage}">
						<a rel="tooltip" href="javascript:void(0)" data-original-title="${tooltipMessage}"><i
							id="<c:out value='${id}' />-help-icon" class="glyphicon glyphicon-question-sign"></i></a>
					</c:if>
				</label>
			</c:otherwise>
		</c:choose>

	</c:if>

	<c:if test="${not empty helpKey}">
		<span class="help-block"><spring:message code="${helpKey}" /></span>
	</c:if>

	<c:if test="${not empty errorPath}">
		<p class="text-danger">
			<form:errors path="${errorPath}" />
		</p>
	</c:if>

	<c:if test="${not empty messageKey}">
		<p class="text-info">
			<spring:message code="${messageKey}" />
		</p>
	</c:if>
</div>
<script>

	$(document).ready(function() {
		var ids = ["#<c:out value='${id}'/>"];
		executeOnLoad(ids);
	})

</script>
<%
	String val = null;
	String fieldName = null;
	if (name == null) {
		val = (String) jspContext.getAttribute("preEvalValue");
		fieldName = path;
	} else {
		val = selectedValue;
		fieldName = name;
	}

	try {
		
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty()) {
			
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>