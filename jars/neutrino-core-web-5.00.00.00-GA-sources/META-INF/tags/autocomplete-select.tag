<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@tag import="com.nucleus.autocomplete.AutocompleteLoadedEntitiesMap"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ attribute name="inputClass"%>
<%@ attribute name="disabled"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="path"%>
<%@ attribute name="name"%>
<%@ attribute name="value"%>
<%@ attribute name="colSpan"%>
<!-- Use 'label' attribute with using 'name' attribute when itemValue
  and itemLabel attributes contain different object property, for reference see bankDetailsGridDcb.jsp
  -->
<%@ attribute name="label"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="itemValue" required="true"%>
<%@ attribute name="itemLabel" required="true"%>
<%@ attribute name="item" type="java.lang.Object"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="className" required="true"%>
<%@ attribute name="searchColList" required="true"%>
<%@ attribute name="loadApprovedEntity"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="onSelection"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="customController"%>
<%@ attribute name="showValueOnEditOrViewMode"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="items"%>
<%@ attribute name="strictSearchOnitemsList"%>
<%@ attribute name="strictMode"%>
<%@ attribute name="styleClass"%>
<%@ attribute name="maxLength"%>
<%@ attribute name="pathPrepender"%>
<%@ attribute name="additionalDataCollectionFunction"%>
<%@ attribute name="additionalDataCollectionFunction_params"%>
<%@ attribute name="inputBoxColSpan"%>
<%@ attribute name="minCharsToBeginSearch"%>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="parentId"%>
<%@ attribute name="parentCol"%>
<%@ attribute name="emptyParentError" %>
<%@ attribute name="containsSearchEnabled" %>
<%@ attribute name="conditionStatement"%>
<%@ attribute name="conditionValue"%>
<%@ attribute name="maskedValue"%>
<%@ attribute name="maskedPath"%>

<%@ attribute name="searchrowswithparentIdnull" %>

<%
	String name = (String) jspContext.getAttribute("name");
	String path = (String) jspContext.getAttribute("path");
	
	String parentId = (String) jspContext.getAttribute("parentId");
	if(parentId == null){
		jspContext.setAttribute("parentId", "");
	}
	
	String parentCol = (String) jspContext.getAttribute("parentCol");
	if(parentCol == null){
		jspContext.setAttribute("parentCol", "");
	}
	
	String additionalDataCollectionFunction = (String) jspContext.getAttribute("additionalDataCollectionFunction");
	if(additionalDataCollectionFunction == null){
		jspContext.setAttribute("additionalDataCollectionFunction", "");
	}
	
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
	}*/

	String fieldName = null;

	if (name == null) {
		fieldName = path;
	} else {
		fieldName = name;
	}

	String regionalVisibility = (String) request.getAttribute(fieldName + "_regionalVisibility");
	String mandatory = (String) request.getAttribute(fieldName + "_mandatoryMode");
	String viewMode = (String) request.getAttribute(fieldName + "_viewMode");
	String labelKey = (String) request.getAttribute(fieldName + "_label");
	String placeHolderKey = (String) request.getAttribute(fieldName + "_placeHolderKey");
	String tooltipKey = (String) request.getAttribute(fieldName + "_toolTipKey");

	if (mandatory != null && mandatory != "" && mandatory.equals("true")) {
		jspContext.setAttribute("mandatory", mandatory);
	} else if (mandatory != null && mandatory != "" && mandatory.equals("false")) {
		jspContext.setAttribute("mandatory", "");
	}
	if (viewMode != null && viewMode != "") {
		jspContext.setAttribute("viewMode", viewMode);
	}
	if (labelKey != null && labelKey != "") {
		jspContext.setAttribute("labelKey", labelKey);
	}
	if (placeHolderKey != null && placeHolderKey != "") {
		jspContext.setAttribute("placeHolderKey", placeHolderKey);
	}
	if (tooltipKey != null && tooltipKey != "") {
		jspContext.setAttribute("tooltipKey", tooltipKey);
	}
	if (regionalVisibility != null && regionalVisibility != "" && regionalVisibility.equals("false")) {
		jspContext.setAttribute("regionalVisibility", regionalVisibility);

	} else {
		jspContext.setAttribute("regionalVisibility", "true");
	}
	String pathPrepender = (String) jspContext.getAttribute("pathPrepender");
	if (name != null && pathPrepender != null) {
		StringBuilder appendedName = new StringBuilder();
		appendedName.append(pathPrepender).append(".").append(name);
		jspContext.setAttribute("name", appendedName);
	}
	if (path != null && pathPrepender != null) {
		StringBuilder appendedPath = new StringBuilder();
		appendedPath.append(pathPrepender).append(".").append(name);
		jspContext.setAttribute("path", appendedPath);

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
                <c:if test="${fn:trim(conditionParams[0]) eq 'maxLength'}">
                    <c:set var = "maxLength" value = "${fn:trim(conditionParams[1])}" scope="page" />
                </c:if>
            </c:forEach>
        </c:if>
    </c:forEach>
</c:if>
<c:if test="${regionalVisibility eq true}">
	<c:if test="${ empty loadApprovedEntity}">
		<c:set var="loadApprovedEntityFlag" scope="page" value="false">

		</c:set>
	</c:if>

	<c:if test="${not empty loadApprovedEntity}">
		<c:set var="loadApprovedEntityFlag" scope="page"
			value="${loadApprovedEntity}">

		</c:set>
	</c:if>





	<c:set var="colSpanClass" value="" scope="page" />
	<c:if test="${not empty colSpan}">
		<c:set var="colSpanClass" value="col-sm-${colSpan}" scope="page" />
	</c:if>

	<c:set var="inputBoxColSpanClass" value="" scope="page" />
	<c:if test="${not empty inputBoxColSpan}">
		<c:set var="inputBoxColSpanClass" value="col-sm-${inputBoxColSpan}"
			scope="page" />
	</c:if>
	<c:if test="${empty maxLength}">
		<c:set var="maxLength" value="255" scope="page" />
	</c:if>



	<c:if test="${not empty viewMode}">
		<c:if test="${viewMode eq true}">
			<c:set var="disabled" value="${viewMode}" scope="page" />
			<c:set var="placeHolderKey" value="" scope="page" />
			<c:set var="tooltipKey" value="" scope="page" />
			<c:set var="validators" value="" scope="page" />
		</c:if>
	</c:if>
	<c:if test="${disabled eq ''}">
		<c:set var="disabled" value="false" scope="page" />
	</c:if>
	<c:if test="${not empty id}">
		<c:set var="listviewId" scope="page" value="listview_${id}">
		</c:set>
		<c:set var="contentId" scope="page" value="content_${id}">
		</c:set>
		<input type="hidden" id="contentIdVarID_<c:out value='${id}' />"
			value="content_<c:out value='${id}'/>" />
		<input type="hidden" id="idVarID_<c:out value='${id}' />"
			value="<c:out value='${id}' />" />
		<input type="hidden" id="idDataFound_<c:out value='${id}' />"
			value="0" />
		<input class="form-control " type="text" style="display: none">
	</c:if>

	<input type="hidden"
		id="strictSearchOnitemsList_<c:out value='${id}' />"
		value="<c:out value='${strictSearchOnitemsList}' />" />
	<input type="hidden" id="strictMode_<c:out value='${id}' />"
		value="<c:out value='${strictMode}' />" />

	<c:if test="${not empty value}">
		<input type="hidden" id="lovClicked_<c:out value='${id}' />"
			value="true" />
	</c:if>
	<c:if test="${empty value}">
		<input type="hidden" id="lovClicked_<c:out value='${id}' />"
			value="false" />
	</c:if>

	<c:if test="${not empty mandatory}">
		<c:set var="validators" scope="page">
			${validators} required
		</c:set>
	</c:if>

	<c:if test="${empty mandatory}">
		<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
	</c:if>

	<c:if test="${not empty searchColList}">
		<input type="hidden" id="searchColListVarID_<c:out value='${id}' />"
			value="<c:out value='${searchColList}' />" />
	</c:if>

	<c:if test="${not empty itemValue}">
		<input type="hidden" id="itemValueVarID_<c:out value='${id}' />"
			value="<c:out value='${itemValue}' />" />
	</c:if>
	<c:if test="${not empty itemLabel}">
		<input type="hidden" id="itemLabelVarID_<c:out value='${id}' />"
			value="<c:out value='${itemLabel}' />" />
	</c:if>
	<c:if test="${not empty className}">
		<input type="hidden" id="classNameVarID_<c:out value='${id}' />"
			value="<c:out value='${className}' />" />
			<%
				String value = (String) jspContext.getAttribute("className");
				AutocompleteLoadedEntitiesMap.addClassesToMap(value);
			%>
			
	</c:if>
	<c:if test="${not empty items}">
		<input type="hidden" id="items_<c:out value='${id}' />"
			value="<c:out value='${items}' />" />
	</c:if>
	<c:if test="${not empty containsSearchEnabled}">
		<input type="hidden" id="containsSearchEnabledID_<c:out value='${id}' />"
			value="<c:out value='${containsSearchEnabled}' />" />
	</c:if>
	<c:if test="${not empty searchrowswithparentIdnull}">
    		<input type="hidden" id="searchrowswithparentIdnullID_<c:out value='${id}' />"
    		value="<c:out value='${searchrowswithparentIdnull}' />" />
    </c:if>


	<c:if test="${not empty styleClass}">
		<c:set var="styleClass" value="${styleClass}" scope="page" />
	</c:if>



	<c:if test="${not empty placeHolderKey}">
		<c:set var="placeHolderMessage" scope="page">
			<spring:message code="${placeHolderKey}"></spring:message>
		</c:set>
	</c:if>

	<c:if test="${not empty tooltipKey}">
		<c:set var="tooltipMessage" scope="page">
			<spring:message code="${tooltipKey}"></spring:message>
		</c:set>
	</c:if>
	<c:if test="${empty minCharsToBeginSearch}">
		<c:set var="minCharsToBeginSearch" scope="page" value="3">
		</c:set>
	</c:if>
	<c:if
		test="${not empty minCharsToBeginSearch && minCharsToBeginSearch lt 0}">
		<c:set var="minCharsToBeginSearch" scope="page" value="3">
		</c:set>
	</c:if>

	<c:if test="${not empty item}">

		<c:set var="labelVal" scope="page" value="${item[itemLabel]}">
		</c:set>
		<c:set var="label" scope="page" value="${labelVal}"></c:set>


	</c:if>
	<c:if test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true))}">
      <c:if test="${not empty maskedValue}">
   	<c:set var="label" value="${maskedValue}" scope="page" />
   	</c:if>
   	   <c:if test="${not empty maskedValue && not empty maskedPath}">
   	<c:set var="path" value="${maskedPath}" scope="page" />
      </c:if>
   </c:if>

	<c:set var="inputId" scope="page">
#Text_<c:out value='${id}' />
	</c:set>


	<div id="<c:out value='${contentId}' />"
		class="form-group autoC-ctrl-grp <c:out value='${colSpanClass}'/> <c:out value='${validators}' /> ${nonMandatoryClass}">

		<c:if test="${not empty labelKey}">
			<label><strong><spring:message code="${labelKey}"></spring:message></strong>
				<c:if test="${not empty mandatory}">
					<span class='fcr'>*</span>
				</c:if></label>
		</c:if>
		<c:choose>
			<c:when test="${not empty name}">
				<c:if test="${not empty  disabled}">
					<input value="${label!=null && label!=''?label:value}"
						data-showValueOnEditOrViewMode="<c:out value='${showValueOnEditOrViewMode}' />"
						data-value="<c:out value='${value}' />"
						data-className="<c:out value='${className}' />"
						class="form-control light-blue medium auto-complete-input <c:out value='${inputBoxColSpanClass}'/> <c:out value='${validators}' /> ${nonMandatoryClass} <c:out value='${styleClass}' />  <c:out value='${inputClass}' />"
						id="Text_<c:out value='${id}' />" autocomplete="off" type="text"
						data-itemValue="<c:out value='${itemValue}' />"
						data-itemLabel="<c:out value='${itemLabel}' />"
						tabindex="<c:out value='${tabindex}' />"
						oninput="loadData_${id}(this,this.value,this.id,${loadApprovedEntityFlag},0,${minCharsToBeginSearch},false)"
						placeholder="${placeHolderMessage}"
						data-custom-controller="<c:out value='${customController}' />"
						data-original-title="${tooltipMessage}" name=""
						disabled="<c:out value='${disabled}' />"
						readonly="<c:out value='${readOnly}' />"
						maxlength="<c:out value='${maxLength}' />" />
				</c:if>
				<c:if test="${empty disabled}">
					<input value="${label!=null && label!=''?label:value}"
						data-showValueOnEditOrViewMode="<c:out value='${showValueOnEditOrViewMode}' />"
						data-value="<c:out value='${value}' />"
						data-className="<c:out value='${className}' />"
						class="form-control <c:out value='${inputBoxColSpanClass}'/> <c:out value='${validators}' /> ${nonMandatoryClass} <c:out value='${styleClass}' /> <c:out value='${inputClass}' /> auto-complete-input"
						data-itemLabel="<c:out value='${itemLabel}' />"
						id="Text_<c:out value='${id}' />" autocomplete="off" type="text"
						tabindex="<c:out value='${tabindex}' />"
						data-itemValue="<c:out value='${itemValue}' />"
						oninput="loadData_${id}(this,this.value,this.id,${loadApprovedEntityFlag},0,${minCharsToBeginSearch},false)"
						placeholder="${placeHolderMessage}"
						data-custom-controller="<c:out value='${customController}' />"
						data-original-title="${tooltipMessage}" name=""
						maxlength="<c:out value='${maxLength}' />"
						onchange="validateNullandSetHiddenValueToEmpty('<c:out value='${id}' />')" />
				</c:if>
				<input id="<c:out value='${id}' />" name="<c:out value='${name}' />"
					value="<c:out value='${value}' />" type="hidden" />
			</c:when>
			<c:otherwise>
				<form:input
					data-showValueOnEditOrViewMode="${showValueOnEditOrViewMode}"
					data-value="${value}" data-className="${className}"
					class="form-control light-blue medium ${inputBoxColSpanClass} ${validators} ${nonMandatoryClass} ${styleClass} auto-complete-input  ${inputClass}"
					data-itemLabel="${itemLabel}" id="Text_${id}" autocomplete="off"
					type="text" tabindex="${tabindex}" data-itemValue="${itemValue}"
					oninput="loadData_${id}(this,this.value,this.id,${loadApprovedEntityFlag},0,${minCharsToBeginSearch},false)"
					placeholder="${placeHolderMessage}" value="${label!=null && label!=''?label:value}"
					data-custom-controller="${customController}"
					data-original-title="${tooltipMessage}" path=""
					disabled="${disabled}" readonly="${readOnly}"
					maxlength="${maxLength}"
					onchange="validateNullandSetHiddenValueToEmpty('${id}')" />
				<form:input cssClass="form-control " id="${id}" path="${path}"
					type="hidden" />
			</c:otherwise>
		</c:choose>
		<div id="auto-container"
			class="form-group  auto-container-height <c:out value='${validators}' /> ${nonMandatoryClass}"></div>
	</div>

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
	<c:if test="${not empty onSelection}">
		<input type="hidden" id="onSelectEvent_<c:out value='${id}' />"
			value="${onSelection}" />
	</c:if>
</c:if>

<%-- Jira Id : PDDEV-14603/Jitendra Kumar
For external JS file autocomplete-select-tag --%>
<script>
	var autoCompleteTagScriptInput_<c:out value='${id}' /> = {};
	 autoCompleteTagScriptInput = {
					 id : "<c:out value='${id}' />",
					 parentId : "<c:out value='${parentId}'/>",
					 parentCol : "<c:out value='${parentCol}'/>",
  		    		 labelVal : "<c:out value='${label}'/>",
  		    		 inputId : "<c:out value='${inputId}'/>",
  		    		 loadApprovedEntityFlag : "<c:out value='${loadApprovedEntityFlag}'/>",
  		    		 currentEntityClassName : "<c:out value='${currentEntityClassName}'/>",
  		    		 additionalDataCollectionFunction : "<c:out value='${additionalDataCollectionFunction}' />",
  		    		 additionalDataCollectionFunction_params : "<c:out value='${additionalDataCollectionFunction_params}' />",
  		    		 emptyParentErrorMsg : "<spring:message code='${emptyParentError}'></spring:message>",
  		    		 minCharsToBeginSearch : "<c:out value='${minCharsToBeginSearch}'/>",
  	};
	 autoCompleteTagScript(autoCompleteTagScriptInput);
	 var loadData_<c:out value='${id}' /> = loadData.bind(autoCompleteTagScriptInput);
	 var hideAutoCompleteMenu = hideAutoCompleteMenu.bind(autoCompleteTagScriptInput);
	 var paginate_func = paginate_func.bind(autoCompleteTagScriptInput);
	 var paginate_func_textBox = paginate_func_textBox.bind(autoCompleteTagScriptInput);
	 var myFunc = myFunc.bind(autoCompleteTagScriptInput);
	
</script>
<%
	String val = (String) jspContext.getAttribute("value");

	try {

		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val != null
				&& !val.isEmpty()) {

			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>