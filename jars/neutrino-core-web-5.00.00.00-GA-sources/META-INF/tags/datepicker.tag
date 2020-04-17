<%@tag import="com.nucleus.config.persisted.enity.Configuration"%>
<%@ tag
	import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ tag import="org.springframework.context.ApplicationContext"%>
<%@ tag import="com.nucleus.master.BaseMasterServiceImpl"%>
<%@ tag import="com.nucleus.era.Era"%>
<%@ tag import="java.util.List"%>
<%@tag import="org.joda.time.LocalDate"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ tag import="org.joda.time.DateTime"%>
<%@ tag import="java.util.Date" %>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ tag import="com.nucleus.core.misc.util.DateUtils"%>
<%@ tag import="org.joda.time.DateTime"%>
<%@ tag import="java.lang.Object"%>
<%@ attribute name="prefixKey"%>
<%@ attribute name="suffixKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="disabled"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="maxLength"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="name"%>
<%@ attribute name="path"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="inputMaskKey"%>
<%@ attribute name="validators"%>
<%@ attribute name="editable"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="pastDefaultDate"%>
<%@ attribute name="value" type="java.lang.Object"%>
<%@ attribute name="dateFormat"%>
<%@ attribute name="defDate"%>
<%@ attribute name="disablePast"%>
<%@ attribute name="disableFuture"%>
<%@ attribute name="disableDateFormat"%>
<%@ attribute name="labelDynamicForm"%>
<%@ attribute name="dynamicFormToolTip"%>
<%@ attribute name="minFieldValue"%>
<%@ attribute name="maxFieldValue"%>
<%@ attribute name="pathPrepender" %>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="maskedValue"%>
<%@ attribute name="maskedPath"%>
<%@ attribute name="openWindowAfterXYears"
	description="calendar window will be open after provided number of years(eg 1,2,5 etc.)"%>
<%@ attribute name="openWindowBeforeXYears"
	description="calendar window will be open before provided number of years(eg 1,2,5 etc.)"%>
<%@ attribute name="conditionStatement"%>
<%@ attribute name="conditionValue"%>
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
else if(dateValue instanceof Date ){
    if(dateValue != null){
        jspContext.setAttribute("dateValueToFormat", dateValue);
    }    
   }
else if (dateValue instanceof String){
    if(dateValue != null){
        jspContext.setAttribute("dateValueToFormat", new Date(Long.parseLong((String)dateValue))  );

    }
}
else{
    if(dateValue != null ){
        throw new IllegalArgumentException();
    }    
   
}
%>
<%
	    		String name = (String) jspContext.getAttribute("name");
				String path = (String) jspContext.getAttribute("path");

				if (name == null && path == null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' must be specified");
				} else if (name != null && path != null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' can be specified at once");
				}
				
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
				String placeHolderKey=(String)request.getAttribute(fieldName+"_placeHolderKey");
				String tooltipKey=(String)request.getAttribute(fieldName+"_toolTipKey");	
				
				if(mandatory !=null && mandatory != "" && mandatory.equals("true")){
					jspContext.setAttribute("mandatory",mandatory);					
				}else if(mandatory !=null && mandatory != "" && mandatory.equals("false")){
					jspContext.setAttribute("mandatory","");
				}				
				if(viewMode !=null && viewMode != ""){
					jspContext.setAttribute("viewMode",viewMode);					
				}
				if(labelKey !=null && labelKey != ""){
					jspContext.setAttribute("labelKey",labelKey);					
				}
				if(placeHolderKey!=null && placeHolderKey!=""){
					jspContext.setAttribute("placeHolderKey",placeHolderKey);
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
                <c:if test="${fn:trim(conditionParams[0]) eq 'maxLength'}">
                    <c:set var = "maxLength" value = "${fn:trim(conditionParams[1])}" scope="page" />
                </c:if>
            </c:forEach>
        </c:if>
    </c:forEach>
</c:if>
<c:set var="errorPathValue">
	<form:errors path="${errorPath}"/>
</c:set>	

<c:if test="${regionalVisibility eq true}">

	<c:set var="userLocale" scope="session"
		value="${sessionScope.sessionUser.userPreferences['config.user.locale'].text}" />
	<c:set var="japLocale" scope="session"
		value="<%=Configuration.JAP_LOCALE%>" />
	<c:if test="${not empty userLocale && userLocale eq japLocale}">
		<c:set var="japanese" value="true" scope="page" />
	</c:if>
	
	<c:set var="minimumYear" scope="page"
		value="${applicationScope.commonConfigUtility.defaultDateMinimumYear}" />
		
	<c:set var="spanClass" value="col-sm-${colSpan}" scope="page" />
	
	
	<c:if test="${not empty pastDefaultDate}">
		<c:set var="pastDefaultDate" value="${pastDefaultDate}" scope="page" />
	</c:if>
	<c:if test="${not empty openWindowAfterXYears}">
		<c:set var="openWindowAfterXYears" value="${openWindowAfterXYears}"
			scope="page" />
	</c:if>
	<c:if test="${not empty openWindowBeforeXYears}">
		<c:set var="openWindowBeforeXYears" value="${openWindowBeforeXYears}"
			scope="page" />
	</c:if>
	
	<c:if test="${not empty defDate}">
		<c:set var="defDate" value="${defDate}" scope="page" />
	</c:if>

	<c:if test="${empty defDate}">
		<c:set var="defDate" value="${applicationScope.commonConfigUtility.systemDateInUserPreferredFormat}"  scope="page" />
	</c:if>
		
	<c:if test="${not empty mandatory}">
		<c:set var="validators" scope="page">
				${validators} required
			</c:set>
	</c:if>
	
	<c:if test="${empty mandatory}">
		<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
	</c:if>
	
	<c:if test="${empty dateFormat}">
		<c:set var="dateFormat"
			value="${neutrino:binder('currentUserDateFormat')}"></c:set>
	</c:if>
	
	<c:choose>
		<c:when test="${fn:contains(dateFormat, 'MMM')}">
			<c:set var="pluginDateFormat"
				value="${fn:replace(dateFormat,'MMM','M')}" />
		</c:when>
		<c:otherwise>
			<c:set var="pluginDateFormat" value="${fn:toLowerCase(dateFormat)}"></c:set>
		</c:otherwise>
	</c:choose>
	
	<c:if test="${not empty value }">
	<fmt:setLocale value = "en_US" scope="session"  />
		<fmt:formatDate type="date" value="${dateValueToFormat}"
			pattern="${dateFormat}" var="displayValue" />
	
	
	</c:if>
	
	<c:if test="${empty disableDateFormat}">
		<c:set var="disableDateFormat" value="false" scope="page" />
	</c:if>
	
	<c:if test="${not empty viewMode}">
		<c:if test="${viewMode eq true}">
			<c:set var="disabled" value="${viewMode}" scope="page" />
			<c:set var="editable" value="false" scope="page" />
			<c:set var="placeHolderKey" value="" scope="page" />
			<c:set var="tooltipKey" value="" scope="page" />
			<c:set var="validators" value="" scope="page" />
		</c:if>
	</c:if>
	
	<c:if test="${disabled eq ''}">
		<c:set var="disabled" value="false" scope="page" />
	</c:if>
	
	
	<c:if test="${not empty tooltipKey}">
		<c:set var="tooltipMessage" scope="page">
			<spring:message code="${tooltipKey}"></spring:message>
		</c:set>
	</c:if>
	
	<c:if test="${not empty dynamicFormToolTip}">
		<c:set var="tooltipMessage" scope="page">
			<c:out value='${dynamicFormToolTip}'/>
		</c:set>
	</c:if>
	<!-- Max Length check on the basis of dateFormat -- PDDEV-20447. -->
	<c:if test="${not empty dateFormat}">
		<c:set var="maxLength" value="${fn:length(dateFormat)}" scope="page" />
	</c:if>
	
	<div id="<c:out value='${id}'/>-control-group"
		class="tagDatePicker form-group <c:out value='${spanClass}' />  ${nonMandatoryClass} date-picker">
		<c:if test="${not empty labelKey}">
			<label><strong><spring:message code="${labelKey}"></spring:message>
				<c:if test="${not empty mandatory}">
					<span class='color-red'>*</span>
				</c:if></strong></label>
		</c:if>
	
		<c:if test="${not empty labelDynamicForm}">
			<label><strong><c:out value='${labelDynamicForm}'/> <c:if
					test="${not empty mandatory}">
					<span class='color-red'>*</span>
				</c:if> </strong></label>
		</c:if>
	
		<%-- <%
		    String name = (String) jspContext.getAttribute("name");
						String path = (String) jspContext.getAttribute("path");
						//declaration
	
						if (name == null && path == null) {
							throw new SystemException(
									"Either of attributes 'name' or 'path' must be specified");
						} else if (name != null && path != null) {
							throw new SystemException(
									"Either of attributes 'name' or 'path' can be specified at once");
						}
		%> --%>
		<%
		
    		String japanese = (String) jspContext.getAttribute("japanese");
			if(japanese!=null && "true".equals(japanese))
			{
     		   ApplicationContext ctx = RequestContextUtils.findWebApplicationContext(request);
			   BaseMasterServiceImpl baseMasterService = (BaseMasterServiceImpl) ctx.getBean("baseMasterService");
			   List<Era> kings = baseMasterService.getAllApprovedAndActiveEntities(Era.class);
			   jspContext.setAttribute("kings", kings);
			}
		%>
<c:if test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true))}">
      <c:if test="${not empty maskedValue}">
   	<c:set var="value" value="${maskedValue}" scope="page" />
   	<c:set var="displayValue" value="${maskedValue}" scope="page" />
   	</c:if>
   	   <c:if test="${not empty maskedValue && not empty maskedPath}">
   	<c:set var="path" value="${maskedPath}" scope="page" />
      </c:if>
 </c:if>	
	
		<c:if test="${not empty path}">
	
			<c:if test="${empty editable || editable == false }">
				<div class="input-group input-group date datepicker_div" id="datepicker_<c:out value='${id}'/>"
					data-date-format="${pluginDateFormat}"
					data-minimum-year="<c:out value='${minimumYear}' />" data-real-format="<c:out value='${dateFormat}'/>"
					data-disable-past="<c:out value='${disablePast}'/>"
					data-disable-future="<c:out value='${disableFuture}'/>"
					data-past-date="<c:out value='${pastDefaultDate}'/>"
					data-open-window-after="<c:out value='${openWindowAfterXYears}' />"
					data-open-window-before="<c:out value='${openWindowBeforeXYears}' />"
					data-block-calander="<c:out value='${disabled}'/>">
					
					<c:choose>
						<c:when test="${userLocale eq japLocale}">
							<div class='datepicker-jp clearfix'>
							<span class="float-l"> <select  class="form-control " id="king_<c:out value='${id}'/>"
									style="width: 90px" disabled="disabled">
										<option value="">
											<spring:message code="label.select"></spring:message>
										</option>
										<c:forEach items="${kings}" var="rowType">
											<option value="<c:out value='${rowType.eraSymbol}'/>"><c:out value='${rowType.eraName}'/></option>
										</c:forEach>
								</select>
								</span>
								<span class="float-l"> <span id="<c:out value='${id}'/>_span"
									class="input-group-addon date-tag-cal float-r no-border-radius" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>
									<c:choose>
                                        <c:when test="${empty tabindex}">
                                            <input class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l"
                                                disabled="<c:out value='${disabled}'/>" id="<c:out value='${id}'/>" type="text" value="${displayValue}"
                                                readOnly="readOnly" maxlength="<c:out value='${maxLength}'></c:out>" style="width: 91px;" />
                                        </c:when>
                                        <c:otherwise>
                                        <input class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l"
                                            disabled="<c:out value='${disabled}'/>" id="<c:out value='${id}'/>" type="text" value="${displayValue}"
                                            tabindex="<c:out value='${tabindex}'/>" readOnly="readOnly" maxlength="<c:out value='${maxLength}'></c:out>" style="width: 91px;" />
                                        </c:otherwise>
								    </c:choose>
							</span>
							<span class="float-l p-l5" >
									<span class='block'>
										<input type="radio" id="jap_<c:out value='${id}'/>" name="languageChosen_<c:out value='${id}'/>"
											class="reset-m dateLang" checked="<c:out value='${japanese}' />" value="JAP" />
									</span>
									<span class='block'>
										<input type="radio" id="eng_<c:out value='${id}'/>" name="languageChosen_<c:out value='${id}'/>"
											class="reset-m dateLang" value="ENG" />
									</span>
								</span>
							<form:hidden path="${path}" id="hid_${id}" />
							</div>
						</c:when>
						<c:otherwise>
							<div class="col-sm-12" >
								<span id="<c:out value='${id}'/>_span" class="input-group-addon float-r col-sm-2" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>
									<c:choose>
                                        <c:when test="${empty tabindex}">
                                            <form:input
                                                cssClass="form-control col-sm-10 ${validators} ${nonMandatoryClass}  validateDateFormat validateMinimumYear float-l"
                                                id="${id}" type="text" readOnly="readOnly" value="${displayValue}" path="${path}"
                                                pastDefaultDate="<c:out value='${pastDefaultDate}'/>"
                                                defDate="${defDate}" maxlength="${maxLength}"
                                                 />
                                        </c:when>
                                        <c:otherwise>
                                            <form:input
                                                cssClass="form-control col-sm-10 ${validators} ${nonMandatoryClass}  validateDateFormat validateMinimumYear float-l"
                                                id="${id}" type="text" tabindex="${tabindex}" readOnly="readOnly" value="${displayValue}" path="${path}"
                                                pastDefaultDate="<c:out value='${pastDefaultDate}'/>"
                                                defDate="${defDate}" maxlength="${maxLength}"
                                                 />
                                        </c:otherwise>
                                    </c:choose>
							</div>
						</c:otherwise>
					</c:choose>
				</div>
	
				<c:if test="${disableDateFormat eq false}">
					<div class="help-block"><c:out value='${dateFormat}'/></div>
				</c:if>
	
				<div class="help-block" id="minMAxHelpblock_<c:out value='${id}'/>">
					<c:if test="${not empty minFieldValue}">
						<c:out value='${minFieldValue}'/><b><</b>
					</c:if>
	
					<c:if
						test="${(not empty minFieldValue) and (not empty maxFieldValue)}">
						<b>TO</b>
					</c:if>
	
					<c:if test="${not empty maxFieldValue}">
						<b>></b><c:out value='${maxFieldValue}'/>
					</c:if>
				</div>
	
			</c:if>
		</c:if>
	
		<c:if test="${not empty path}">
	
			<c:if test="${editable == true  &&  empty dateFormat}">
				<div class="input-group input-group date datepicker_div"
					id="datepicker_<c:out value='${id}'/>">
					<c:choose>
						<c:when test="${userLocale eq japLocale}">
						<div class='datepicker-jp clearfix'>
							<span class="float-l"> <select  class="form-control " id="king_<c:out value='${id}'/>"
									style="width: 90px" disabled="disabled">
										<option value="">
											<spring:message code="label.select"></spring:message>
										</option>
										<c:forEach items="${kings}" var="rowType">
											<option value="<c:out value='${rowType.eraSymbol}'/>"><c:out value='${rowType.eraName}'/></option>
										</c:forEach>
								</select>
								</span>
								<span class="float-l"> <span id="<c:out value='${id}'/>_span"
									class="input-group-addon date-tag-cal float-r no-border-radius" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>

								<c:choose>
                                    <c:when test="${empty tabindex}">
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l" id="<c:out value='${id}' />"
                                            type="text" value="${displayValue}"
                                            disabled="<c:out value='${disabled}'/>" maxlength="<c:out value='${maxLength}'/>"
                                            style="width: 91px;" />
                                    </c:when>
                                    <c:otherwise>
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l" id="<c:out value='${id}' />"
                                            type="text" tabindex="<c:out value='${tabindex}'/>" value="${displayValue}"
                                            disabled="<c:out value='${disabled}'/>" maxlength="<c:out value='${maxLength}'/>"
                                            style="width: 91px;" />
                                    </c:otherwise>
                                </c:choose>
							</span>
							<span class="float-l p-l5">
									<span class='block'>
										<input type="radio" id="jap_<c:out value='${id}' />" name="languageChosen_${id}"
											class="reset-m dateLang" checked="<c:out value='${japanese}' />" value="JAP" />
									</span>
									<span class='block'>
										<input type="radio" id="eng_<c:out value='${id}' />" name="languageChosen_<c:out value='${id}' />"
											class="reset-m dateLang" value="ENG" />
									</span>
								</span>
							<form:hidden path="${path}" id="hid_${id}" />
							</div>
						</c:when>
						<c:otherwise>
							<div class="col-sm-12" >
				
								<span id="<c:out value='${id}' />_span" class="input-group-addon float-r col-sm-2" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>
								<c:choose>
                                    <c:when test="${empty tabindex}">
                                        <form:input
                                            cssClass="form-control col-sm-10 ${validators} ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="${id}" type="text" value="${displayValue}" path="${path}"
                                            pastDefaultDate="<c:out value='${pastDefaultDate}'/>" disabled="${disabled}"
                                            defDate="${defDate}" maxlength="${maxLength}"
                                            />
                                    </c:when>
                                    <c:otherwise>
                                        <form:input
                                            cssClass="form-control col-sm-10 ${validators} ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="${id}" type="text" tabindex="${tabindex}" value="${displayValue}" path="${path}"
                                            pastDefaultDate="<c:out value='${pastDefaultDate}'/>" disabled="${disabled}"
                                            defDate="${defDate}" maxlength="${maxLength}"
                                            />
                                    </c:otherwise>
                                </c:choose>
							</div>
						</c:otherwise>
					</c:choose>
	
				</div>
	
				<c:if test="${disableDateFormat eq false}">
					<div class="help-block"><c:out value='${dateFormat}'/></div>
				</c:if>
	
				<div class="help-block" id="minMAxHelpblock_<c:out value='${id}'/>">
					<c:if test="${not empty minFieldValue}">
						<c:out value='${minFieldValue}'/><b><</b>
					</c:if>
	
					<c:if
						test="${(not empty minFieldValue) and (not empty maxFieldValue)}">
						<b>TO</b>
					</c:if>
	
					<c:if test="${not empty maxFieldValue}">
						<b>></b><c:out value='${maxFieldValue}'/>
					</c:if>
				</div>
	
			</c:if>
		</c:if>
	
		<c:if test="${not empty path}">
	
			<c:if test="${editable == true &&  not empty dateFormat}">
				<div class="input-group input-group date datepicker_div"
					id="datepicker_<c:out value='${id}' />" data-date-format="${pluginDateFormat}"
					data-minimum-year="<c:out value='${minimumYear}' />" data-real-format="<c:out value='${dateFormat}'/>"
					data-disable-past="<c:out value='${disablePast}'/>"
					data-disable-future="<c:out value='${disableFuture}'/>"
					data-past-date="<c:out value='${pastDefaultDate}'/>"
					data-open-window-before="<c:out value='${openWindowBeforeXYears}' />"
					data-open-window-after="<c:out value='${openWindowAfterXYears}' />"
					data-block-calander="<c:out value='${disabled}'/>">
					<c:choose>
						<c:when test="${userLocale eq japLocale}">
						<div class='datepicker-jp clearfix'>
							<span class="float-l"> <select  class="form-control " id="king_<c:out value='${id}' />"
									style="width: 90px" disabled="disabled">
										<option value="">
											<spring:message code="label.select"></spring:message>
										</option>
										<c:forEach items="${kings}" var="rowType">
											<option value="<c:out value='${rowType.eraSymbol}'/>"><c:out value='${rowType.eraName}'/></option>
										</c:forEach>
								</select>
								</span>
								<span class="float-l"> <span id="<c:out value='${id}' />_span"
									class="input-group-addon date-tag-cal float-r no-border-radius" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>

								<c:choose>
                                    <c:when test="${empty tabindex}">
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l" id="<c:out value='${id}' />"
                                            type="text" value="${displayValue}"
                                            maxlength="<c:out value='${maxLength}'/>" style="width: 91px;" />
                                    </c:when>
                                    <c:otherwise>
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l" id="<c:out value='${id}' />"
                                            type="text" tabindex="<c:out value='${tabindex}'/>" value="${displayValue}"
                                            maxlength="<c:out value='${maxLength}'/>" style="width: 91px;" />
                                    </c:otherwise>
                                </c:choose>
							</span>
							<span class="float-l p-l5">
									<span class='block'>
										<input type="radio" id="jap_<c:out value='${id}' />" name="languageChosen_<c:out value='${id}' />"
											class="reset-m dateLang" checked="<c:out value='${japanese}' />" value="JAP" />
									</span>
									<span class='block'>
										<input type="radio" id="eng_<c:out value='${id}' />" name="languageChosen_<c:out value='${id}' />"
											class="reset-m dateLang" value="ENG" />
									</span>
								</span>
							<form:hidden path="${path}" id="hid_${id}" />
						</div>
						</c:when>
						<c:otherwise>
							<div class="col-sm-12" >
				
								<span id="<c:out value='${id}' />_span" class="input-group-addon float-r col-sm-2" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>

				                <c:choose>
                                    <c:when test="${empty tabindex}">
                                        <form:input
                                            cssClass="form-control col-sm-10 ${validators} ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="${id}" type="text" value="${displayValue}" path="${path}"
                                            pastDefaultDate="${pastDefaultDate}" disabled="${disabled}"
                                            defDate="${defDate}" maxlength="${maxLength}"
                                             />
                                    </c:when>
                                    <c:otherwise>
                                        <form:input
                                            cssClass="form-control col-sm-10 ${validators} ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="${id}" type="text" tabindex="${tabindex}"
                                            value="${displayValue}" path="${path}"
                                            pastDefaultDate="${pastDefaultDate}" disabled="${disabled}"
                                            defDate="${defDate}" maxlength="${maxLength}"
                                             />
                                    </c:otherwise>
                                </c:choose>


							</div>
						</c:otherwise>
					</c:choose>
				</div>
				<c:if test="${disableDateFormat eq false}">
					<div class="help-block"><c:out value='${dateFormat}'/></div>
				</c:if>
				<div class="help-block" id="minMAxHelpblock_<c:out value='${id}'/>">
	
					<c:if test="${not empty minFieldValue}">
						<c:out value='${minFieldValue}'/><b><</b>
					</c:if>
	
					<c:if
						test="${(not empty minFieldValue) and (not empty maxFieldValue)}">
						<b>TO</b>
					</c:if>
	
					<c:if test="${not empty maxFieldValue}">
						<b>></b><c:out value='${maxFieldValue}'/>
					</c:if>
				</div>
	
			</c:if>
		</c:if>
	
	
		<c:if test="${not empty name}">
			<c:if test="${empty editable || editable == false }">
				<div class="input-group input-group  date datepicker_div" id="datepicker_<c:out value='${id}'/>"
					data-minimum-year="<c:out value='${minimumYear}' />"
					data-date-format="${pluginDateFormat}"
					data-real-format="<c:out value='${dateFormat}'/>" data-disable-past="<c:out value='${disablePast}'/>"
					data-disable-future="<c:out value='${disableFuture}'/>"
					data-past-date="<c:out value='${pastDefaultDate}'/>"
					data-block-calander="<c:out value='${disabled}'/>"
					data-open-window-before="<c:out value='${openWindowBeforeXYears}' />"
					data-open-window-after="<c:out value='${openWindowAfterXYears}' />">
					<c:choose>
						<c:when test="${userLocale eq japLocale}">
							<div class='datepicker-jp clearfix'>
							<span class="float-l"> <select  class="form-control " id="king_<c:out value='${id}' />"
									style="width: 90px" disabled="disabled">
										<option value="">
											<spring:message code="label.select"></spring:message>
										</option>
										<c:forEach items="${kings}" var="rowType">
											<option value="<c:out value='${rowType.eraSymbol}'/>"><c:out value='${rowType.eraName}'/></option>
										</c:forEach>
								</select>
								</span>
								<span class="float-l"> <span id="<c:out value='${id}' />_span"
									class="input-group-addon date-tag-cal float-r no-border-radius" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>

								<c:choose>
                                    <c:when test="${empty tabindex}">
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l"
                                            disabled="<c:out value='${disabled}'/>" id="<c:out value='${id}' />" type="text" value="${displayValue}"
                                            maxlength="<c:out value='${maxLength}'/>" readOnly="readOnly" style="width: 91px;" />
                                    </c:when>
                                    <c:otherwise>
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l"
                                            disabled="<c:out value='${disabled}'/>" id="<c:out value='${id}' />" type="text"
                                            tabindex="<c:out value='${tabindex}'/>" value="${displayValue}"
                                            maxlength="<c:out value='${maxLength}'/>" readOnly="readOnly" style="width: 91px;" />
                                    </c:otherwise>
                                </c:choose>

							</span>
							<span class="float-l p-l5">
									<span class='block'>
										<input type="radio" id="jap_<c:out value='${id}' />" name="languageChosen_<c:out value='${id}' />"
											class="reset-m dateLang" checked="<c:out value='${japanese}' />" value="JAP" />
									</span>
									<span class='block'>
										<input type="radio" id="eng_<c:out value='${id}' />" name="languageChosen_<c:out value='${id}' />"
											class="reset-m dateLang" value="ENG" />
									</span>
								</span>
							<input type="hidden" class="float-r" name="<c:out value='${name}'/>" id="hid_<c:out value='${id}' />"
								value="${displayValue}" />
								</div>
						</c:when>
						<c:otherwise>
							<div class="col-sm-12" >

								<span id="<c:out value='${id}' />_span" class="input-group-addon float-r col-sm-2" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>
                                <c:choose>
                                    <c:when test="${empty tabindex}">
                                        <input
                                            class="form-control col-sm-10 /> <c:out value='${validators}'/> ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="<c:out value='${id}' />" type="text" readOnly="readOnly" value="${displayValue}" name="<c:out value='${name}'/>" defDate="${defDate}"
                                            value="<c:out value='${pastDefaultDate}'/>" maxlength="<c:out value='${maxLength}'/>"
                                             />
                                    </c:when>
                                    <c:otherwise>
                                        <input
                                            class="form-control col-sm-10 /> <c:out value='${validators}'/> ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="<c:out value='${id}' />" type="text" tabindex="<c:out value='${tabindex}'/>"
                                            readOnly="readOnly" value="${displayValue}" name="<c:out value='${name}'/>" defDate="${defDate}"
                                            value="<c:out value='${pastDefaultDate}'/>" maxlength="<c:out value='${maxLength}'/>"
                                             />
                                    </c:otherwise>
                                </c:choose>

							</div>
						</c:otherwise>
					</c:choose>
				</div>
	
				<div class="help-block" id="minMAxHelpblock_<c:out value='${id}'/>">
					<c:if test="${not empty minFieldValue}">
						<c:out value='${minFieldValue}'/><b><</b>
					</c:if>
	
					<c:if
						test="${(not empty minFieldValue) and (not empty maxFieldValue)}">
						<b>TO</b>
					</c:if>
	
					<c:if test="${not empty maxFieldValue}">
						<b>></b><c:out value='${maxFieldValue}'/>
					</c:if>
				</div>
	
			</c:if>
		</c:if>
	
		<c:if test="${not empty name}">
			<c:if test="${editable == true  &&  empty dateFormat}">
				<div class="input-group input-group date datepicker_div"
					id="datepicker_<c:out value='${id}' />">
						<c:choose>
							<c:when test="${userLocale eq japLocale}">
							<div class='datepicker-jp clearfix'>
								<span class="float-l"> <select  class="form-control " id="king_<c:out value='${id}' />"
									style="width: 90px" disabled="disabled">
										<option value="">
											<spring:message code="label.select"></spring:message>
										</option>
										<c:forEach items="${kings}" var="rowType">
											<option value="<c:out value='${rowType.eraSymbol}'/>"><c:out value='${rowType.eraName}'/></option>
										</c:forEach>
								</select>
								</span>
								<span class="float-l"> <span id="<c:out value='${id}' />_span"
									class="input-group-addon date-tag-cal float-r no-border-radius" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>

								<c:choose>
                                    <c:when test="${empty tabindex}">
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l" id="<c:out value='${id}' />"
                                            type="text" value="${displayValue}"
                                            disabled="<c:out value='${disabled}'/>" maxlength="<c:out value='${maxLength}'/>"
                                            style="width: 91px;" />
                                    </c:when>
                                    <c:otherwise>
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l" id="<c:out value='${id}' />"
                                            type="text" tabindex="<c:out value='${tabindex}'/>" value="${displayValue}"
                                            disabled="<c:out value='${disabled}'/>" maxlength="<c:out value='${maxLength}'/>"
                                            style="width: 91px;" />
                                    </c:otherwise>
                                </c:choose>

								</span>
								<span class="float-l p-l5">
									<span class='block'>
										<input type="radio" id="jap_<c:out value='${id}' />" name="languageChosen_<c:out value='${id}' />"
											class="reset-m dateLang" checked="<c:out value='${japanese}' />" value="JAP" />
									</span>
									<span class='block'>
										<input type="radio" id="eng_<c:out value='${id}' />" name="languageChosen_<c:out value='${id}' />"
											class="reset-m dateLang" value="ENG" />
									</span>
								</span>
								<input type="hidden" class="float-r" name="<c:out value='${name}'/>"
									id="hid_<c:out value='${id}' />" value="${displayValue}" />
								</div>
							</c:when>
							<c:otherwise>
							<div class="col-sm-12">

								<span id="<c:out value='${id}' />_span" class="input-group-addon float-r col-sm-2" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>

								<c:choose>
                                    <c:when test="${empty tabindex}">
                                        <input
                                            class="form-control col-sm-10 <c:out value='${validators}'/> ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="<c:out value='${id}' />" type="text" defDate="${defDate}"
                                            value="${displayValue}" name="<c:out value='${name}'/>" disabled="<c:out value='${disabled}'/>"
                                            maxlength="<c:out value='${maxLength}'/>" />
                                    </c:when>
                                    <c:otherwise>
                                        <input
                                            class="form-control col-sm-10 <c:out value='${validators}'/> ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="<c:out value='${id}' />" type="text" tabindex="<c:out value='${tabindex}'/>" defDate="${defDate}"
                                            value="${displayValue}" name="<c:out value='${name}'/>" disabled="<c:out value='${disabled}'/>"
                                            maxlength="<c:out value='${maxLength}'/>" />
                                    </c:otherwise>
                                </c:choose>


							</div>
							</c:otherwise>
						</c:choose>
				</div>
	
				<c:if test="${disableDateFormat eq false}">
					<div class="help-block"><c:out value='${dateFormat}'/></div>
				</c:if>
	
				<div class="help-block" id="minMAxHelpblock_<c:out value='${id}'/>">
					<c:if test="${not empty minFieldValue}">
						<c:out value='${minFieldValue}'/><b></b>
					</c:if>
	
					<c:if
						test="${(not empty minFieldValue) and (not empty maxFieldValue)}">
						<b>TO</b>
					</c:if>
	
					<c:if test="${not empty maxFieldValue}">
						<b>></b><c:out value='${maxFieldValue}'/>
					</c:if>
				</div>
	
			</c:if>
		</c:if>
	
		<c:if test="${not empty name}">
			<c:if test="${editable == true &&  not empty dateFormat}">
				<div
					class="input-group input-group date datepicker_div reset-m-l"
					id="datepicker_<c:out value='${id}' />" data-date-format="${pluginDateFormat}"
					data-minimum-year="<c:out value='${minimumYear}' />" data-real-format="<c:out value='${dateFormat}'/>"
					data-disable-past="<c:out value='${disablePast}'/>"
					data-disable-future="<c:out value='${disableFuture}'/>"
					data-past-date="<c:out value='${pastDefaultDate}'/>"
					data-block-calander="<c:out value='${disabled}'/>"
					data-open-window-before="<c:out value='${openWindowBeforeXYears}' />"
					data-open-window-after="<c:out value='${openWindowAfterXYears}' />">
						<c:choose>
							<c:when test="${userLocale eq japLocale}">
							<div class='datepicker-jp clearfix'>
								<span class="float-l"> <select  class="form-control " id="king_<c:out value='${id}' />"
									style="width: 90px" disabled="disabled">
										<option value="">
											<spring:message code="label.select"></spring:message>
										</option>
										<c:forEach items="${kings}" var="rowType">
											<option value="<c:out value='${rowType.eraSymbol}'/>"><c:out value='${rowType.eraName}'/></option>
										</c:forEach>
								</select>
								</span>
								<span class="float-l"> <span id="<c:out value='${id}' />_span"
									class="input-group-addon date-tag-cal float-r no-border-radius" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>

								<c:choose>
                                    <c:when test="${empty tabindex}">
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l" id="<c:out value='${id}' />"
                                            type="text" value="${displayValue}"
                                            maxlength="<c:out value='${maxLength}'/>" style="width: 103px;" />
                                    </c:when>
                                    <c:otherwise>
                                        <input
                                            class="form-control <c:out value='${spanClass}' /> ${nonMandatoryClass} float-l" id="<c:out value='${id}' />"
                                            type="text" tabindex="<c:out value='${tabindex}'/>" value="${displayValue}"
                                            maxlength="<c:out value='${maxLength}'/>" style="width: 103px;" />
                                    </c:otherwise>
                                </c:choose>



								</span>
								<span class="float-l p-l5">
									<span class='block'>
										<input type="radio" id="jap_<c:out value='${id}' />" name="languageChosen_<c:out value='${id}' />"
											class="reset-m dateLang" checked="<c:out value='${japanese}' />" value="JAP" />
									</span>
									<span class='block'>
										<input type="radio" id="eng_<c:out value='${id}' />" name="languageChosen_<c:out value='${id}' />"
											class="reset-m dateLang" value="ENG" />
									</span>
								</span>
								<input type="hidden" class="float-r" name="<c:out value='${name}'/>"
									id="hid_<c:out value='${id}' />" value="${displayValue}" />
								
								</div> <!-- /.datepicker-jp -->
							</c:when>
							<c:otherwise>
							<div class="col-sm-12" >
		

								<span id="<c:out value='${id}' />_span" class="input-group-addon float-r col-sm-2" rel="tooltip"
									title="${tooltipMessage}"><i class="TagdateIcon"></i></span>
								<c:choose>
                                    <c:when test="${empty tabindex}">
                                        <input
                                            class="form-control col-sm-10 <c:out value='${validators}'/> ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="<c:out value='${id}' />" type="text" name="<c:out value='${name}'/>"
                                            value="${displayValue}" maxlength="<c:out value='${maxLength}'/>"  defDate="${defDate}"
                                             />
                                    </c:when>
                                    <c:otherwise>
                                        <input
                                            class="form-control col-sm-10 <c:out value='${validators}'/> ${nonMandatoryClass} validateDateFormat validateMinimumYear float-l"
                                            id="<c:out value='${id}' />" type="text" tabindex="<c:out value='${tabindex}'/>" name="<c:out value='${name}'/>"
                                            value="${displayValue}" maxlength="<c:out value='${maxLength}'/>"  defDate="${defDate}"
                                             />
                                    </c:otherwise>
                                </c:choose>
							</div>
							</c:otherwise>
						</c:choose>
					</div>
	
				<c:if test="${disableDateFormat eq false}">
					<div class="help-block"><c:out value='${dateFormat}'/></div>
				</c:if>
	
				<div class="help-block" id="minMAxHelpblock_<c:out value='${id}'/>">
					<c:if test="${not empty minFieldValue}">
						<c:out value='${minFieldValue}'/><b><</b>
					</c:if>
	
					<c:if
						test="${(not empty minFieldValue) and (not empty maxFieldValue)}">
						<b>TO</b>
					</c:if>
	
					<c:if test="${not empty maxFieldValue}">
						<b>></b><c:out value='${maxFieldValue}'/>
					</c:if>
				</div>
	
			</c:if>
		</c:if>
	
	
	</div>
	
	<c:if test="${not empty helpKey}">
		<span class="help-block"><spring:message code="${helpKey}" /></span>
	</c:if>
	
	<c:if test="${not empty errorPathValue}">
	
		<script>
		populateServerSideError('${id}');
		var errorMsg = '<form:errors path="${errorPath}"/>';
		$( "#minMAxHelpblock_${id}" ).addClass("${id}_class");
		var errorMsgSpan = '<span for="<c:out value='${id}'/>" generated="true" class="help-block" style="display:block">'+errorMsg+'</span>' ;
		$( ".${id}_class" ).after(errorMsgSpan);
		</script>
						
	</c:if>	
	
	<c:if test="${not empty messageKey}">
		<p class="text-info">
			<spring:message code="${messageKey}" />
		</p>
	</c:if>

	
	<script>
	$(document).ready(function() {
		var tagId = "<c:out value='${id}' />";
		var editableFlag = "<c:out value='${editable}'/>";
		var disableFlag = "<c:out value='${disabled}'/>";
		var viewMode = "<c:out value='${viewMode}'/>";

		if(editableFlag == 'false') {
			$('#'+tagId).attr("readOnly", "readOnly");
		}
		
		if(viewMode == 'false' && disableFlag == 'true') {
			$('#'+tagId).attr("disabled", "disabled");
		}
		initDatePickerTag('${id}','${defDate}',"${minFieldValue}","${maxFieldValue}","${dateFormat}" , '${viewMode}');
		bindOnChangeEvent('${id}','${pluginDateFormat}','${defDate}');
	});

	
	</script>
</c:if>
<%
	String val = (String) jspContext.getAttribute("displayValue");
	

	try {
		
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty()) {
			
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>