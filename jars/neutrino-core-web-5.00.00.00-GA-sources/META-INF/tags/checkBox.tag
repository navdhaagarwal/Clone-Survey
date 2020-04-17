<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="disabled"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="path"%>
<%@ attribute name="name"%>
<%@ attribute name="value"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="validators"%>
<%@ attribute name="checked"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="labelDynamicForm"%>
<%@ attribute name="dynamicFormToolTip"%>
<%@ attribute name="pathPrepender" %>
<%@ attribute name="onClickEvent"%>
<%@ attribute name="modificationAllowed"%>

<%
	    		String name = (String) jspContext.getAttribute("name");
				String path = (String) jspContext.getAttribute("path");
				
			
				/*
		  		Temporarily Code commented -Attributes made non-mandatory
		 	 	*/
				/* if (name == null && path == null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' must be specified");
				} else
				if (name != null && path != null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' can be specified at once");
				} */
				
				String fieldName=null;
				
				if(name == null){
				 	fieldName=path;
				}else{
					fieldName=name;
				} 
				
				String mandatory=(String)request.getAttribute(fieldName+"_mandatoryMode"); 
				String viewMode=(String)request.getAttribute(fieldName+"_viewMode");
				String regionalVisibility=(String)request.getAttribute(fieldName+"_regionalVisibility");
				String labelKey=(String)request.getAttribute(fieldName+"_label");
				String tooltipKey=(String)request.getAttribute(fieldName+"_toolTipKey");	
				if(mandatory !=null && mandatory != ""){
					jspContext.setAttribute("mandatory",mandatory);					
				}
				if(viewMode !=null && viewMode != ""){
					jspContext.setAttribute("viewMode",viewMode);					
				}
				if(labelKey !=null && labelKey != ""){
					jspContext.setAttribute("labelKey",labelKey);					
				}
				if(tooltipKey!=null && tooltipKey!=""){
					jspContext.setAttribute("tooltipKey",tooltipKey);
				}
				if(regionalVisibility !=null && regionalVisibility != "" && regionalVisibility.equals("false")){
					jspContext.setAttribute("regionalVisibility",regionalVisibility);
					
				}else{
					jspContext.setAttribute("regionalVisibility","true");
				}
				String pathPrepender=(String)jspContext.getAttribute("pathPrepender");
				if(name!=null && pathPrepender!= null){	
					StringBuilder appendedName=new StringBuilder();
					appendedName.append(pathPrepender).append(".").append(name);					
					jspContext.setAttribute("name",appendedName);					
				} 
				if(path!=null && pathPrepender!= null){	
					StringBuilder appendedPath=new StringBuilder();
					appendedPath.append(pathPrepender).append(".").append(name);					
					jspContext.setAttribute("path",appendedPath);				
				}
				
%>
<c:if test="${regionalVisibility eq true}">

<c:if test="${not empty tooltipKey}">
	<c:set var="tooltipMessage" scope="page">
		<spring:message code="${tooltipKey}"></spring:message>
	</c:set>
</c:if>

<c:if test="${not empty dynamicFormToolTip}">
	<c:set var="tooltipMessage" scope="page">
		<c:out value='${dynamicFormToolTip}' />
	</c:set>
</c:if>

<c:if test="${not empty viewMode}">
	<c:if test="${viewMode eq true}">
		<c:set var="disabled" value="${viewMode}" scope="page" />
		<c:set var="tooltipKey" value="" scope="page" />
		<c:set var="validators" value="" scope="page" />
	</c:if>
</c:if>


<c:if test="${mandatory ne true}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>
<c:set var="requiredClass" value="" scope="page" />
<c:if test="${mandatory eq true}">
	<c:set var="requiredClass" value="required" scope="page" />
</c:if>
<c:if test="${readOnly eq true}">
<c:set var="readOnlyClass" value="class='readOnlyCheckBox'" scope="page" />
</c:if>

<div class="${nonMandatoryClass} form-group input-group checkbox-container">
<c:choose>
	<c:when test="${not empty labelKey && not empty path}">
		<spring:bind path="${path}">
				<c:set var="preEvalValue" value="${status.value}"></c:set>
		</spring:bind>
		<label ${readOnlyClass}> <form:checkbox path="${path}" onclick="${onClickEvent}" 
				cssClass="${validators} uni_style ${requiredClass}" disabled="${disabled}" id="${id}"
				readonly="${readOnly}" tabindex="${tabindex}" value="${value}" /><strong><spring:message
					code="${labelKey}" text="${labelKey}"></spring:message></strong> 
			<c:if test="${mandatory eq true}">
				<span class="Mandatory" style="color: red">*</span>
			</c:if> 
			<c:if test="${not empty tooltipMessage}">
				<a rel="tooltip" href="javascript:void(0)" data-original-title="${tooltipMessage}"><i
					id="<c:out value='${id}' />-help-icon" class="glyphicon glyphicon-info-sign"></i></a>
			</c:if>
		</label>
	</c:when>

	<c:when
		test="${not empty labelKey && not empty name && not empty disabled}">
		<label ${readOnlyClass}> <input type="checkbox" name="<c:out value='${name}'  />" onclick="${onClickEvent}" autocomplete="off"
			class="<c:out value='${validators}' /> uni_style ${requiredClass}" disabled="<c:out value='${disabled}' />" id="<c:out value='${id}'/>"
			readonly="${readOnly}" tabindex="<c:out value='${tabindex}' />" value="<c:out value='${value}' />" /><strong><spring:message
					code="${labelKey}" text="${labelKey}"></spring:message></strong> 
			<c:if test="${mandatory eq true}">
				<span class="Mandatory" style="color: red">*</span>
			</c:if>
			<c:if test="${not empty tooltipMessage}">
				<a rel="tooltip" href="javascript:void(0)" data-original-title="${tooltipMessage}"><i
					id="<c:out value='${id}' />-help-icon" class="glyphicon glyphicon-info-sign"></i></a>
			</c:if>
		</label>
	</c:when>

	<c:when test="${not empty labelKey && not empty name && empty disabled}">
		<label ${readOnlyClass}> <input type="checkbox" name="<c:out value='${name}' />" onclick="${onClickEvent}" autocomplete="off"
			class="<c:out value='${validators}' /> uni_style ${requiredClass}" id="<c:out value='${id}' />" 
			tabindex="<c:out value='${tabindex}' />" value="<c:out value='${value}' />" /><strong><spring:message
					code="${labelKey}" text="${labelKey}"></spring:message></strong>
			<c:if test="${mandatory eq true}">
				<span class="Mandatory" style="color: red">*</span>
			</c:if> 
			<c:if test="${not empty tooltipMessage}">
				<a rel="tooltip" href="javascript:void(0)" data-original-title="${tooltipMessage}"><i
					id="<c:out value='${id}' />-help-icon" class="glyphicon glyphicon-info-sign"></i></a>
			</c:if>
		</label>
	</c:when>

	<c:otherwise>
		<label ${readOnlyClass}> <form:checkbox path="${path}" autocomplete="off"
				cssClass="${validators} uni_style ${requiredClass}" disabled="${disabled}" id="${id}"
				readonly="${readOnly}" tabindex="${tabindex}" value="${value}" onclick="${onClickEvent}" /><strong>${labelDynamicForm}</strong>
			<c:if test="${mandatory eq true}">
				<span class="Mandatory" style="color: red">*</span>
			</c:if> 
			<c:if test="${not empty tooltipMessage}">
				<a rel="tooltip" href="javascript:void(0)" data-original-title="${tooltipMessage}"><i
					id="<c:out value='${id}' />-help-icon" class="glyphicon glyphicon-info-sign"></i></a>
			</c:if>
		</label>
	</c:otherwise>
</c:choose>

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
</c:if>



<script>


	$(document).ready(function() {
		var ids = ["#<c:out value='${id}'/>"];
		executeOnLoad(ids);
	})
	
	

</script>
<%
	String val = (String) jspContext.getAttribute("value");
	String path_val = (String) jspContext.getAttribute("preEvalValue");
	
	try {
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty() && val.equals(path_val)) {
			
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>