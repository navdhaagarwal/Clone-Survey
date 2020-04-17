<%@ tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="com.nucleus.regional.metadata.RegionalMetaDataProcessingBean"%>
<%@tag import="com.nucleus.regional.metadata.RegionalMetaData"%>
<%@tag import="java.util.List"%>
<%@tag import="com.nucleus.regional.RegionalData"%>
<%@tag import="javax.sound.midi.SysexMessage"%>
<%@tag import="com.nucleus.regional.RegionalEnabled"%>
<%@tag import="java.util.Map"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>

<%@ attribute name="regionalDateFormat"%>
<%@ attribute name="regionalTabindex"%>
<%@ attribute name="regionalId" required="true"%>
<%@ attribute name="regionalPastDefaultDate"%>
<%@ attribute name="regionalDisabled"%>
<%@ attribute name="regionalDefDate"%>
<%@ attribute name="regionalMaxLength"%>
<%@ attribute name="regionalValidators"%>
<%@ attribute name="regionalColSpan"%>
<%@ attribute name="regionalTooltipMessage"%>
<%@ attribute name="regionalPlaceHolderKey"%>
<%@ attribute name="regionalErrorPath"%>
<%@ attribute name="regionalTooltipKey"%>
<%@ attribute name="nameAttribute" required="true"%>
<%@ attribute name="calendarType" required="true"%>
<%@ attribute name="disableRegionalDateFormat"%>
<%@ attribute name="regionalViewMode" %>

<c:if test="${not empty nameAttribute}">
	<c:set var="fieldName" value="${nameAttribute}" scope="page" />
</c:if>

<c:if test="${not empty fieldName}">

			<c:set var="regionalLabelKey_Key" value="${fieldName}_label" scope="page" />			
			<c:set var="regionalMandatory_Key" value="${fieldName}_mandatoryMode" scope="page" />			
			<c:set var="regionalViewMode_Key" value="${fieldName}_viewMode" scope="page" />			
			<c:set var="sourceEntityName_Key" value="${fieldName}_sourceEntity" scope="page" />
			<c:set var="regionalDisabled_Key" value="${fieldName}_disabled" scope="page" />
</c:if>

<c:if test="${not empty calendarType}">
		<c:set var="calendarType" value="${calendarType}"/> 
</c:if>

<c:if test="${empty disableRegionalDateFormat}">
		<c:set var="disableRegionalDateFormat" value="false" scope="page" />
	</c:if>

<%-- <c:if test="${empty regionalDateFormat}"> --%>
	<c:set var="regionalDateFormat"
		value="dd/mm/yyyy"></c:set>
<%-- </c:if> --%>



<c:set var="spanClass" value="col-sm-${regionalColSpan}" scope="page" />


	
	<%				
				
				String fieldName= (String)request.getAttribute((String)jspContext.getAttribute("fieldName"));
				
				if(fieldName != null && fieldName != ""){
				
					String sourceEntityName= (String)request.getAttribute((String)jspContext.getAttribute("sourceEntityName_Key"));
					jspContext.setAttribute("regionalLabelKey",(String)request.getAttribute((String)jspContext.getAttribute("regionalLabelKey_Key")));		
					jspContext.setAttribute("regionalMandatory",(String)request.getAttribute((String)jspContext.getAttribute("regionalMandatory_Key")));
					
					String viewModeVal = (String) jspContext.getAttribute("regionalViewMode");
					if (viewModeVal == null || viewModeVal == " ") {
						jspContext.setAttribute("regionalViewMode",(String)request.getAttribute((String)jspContext.getAttribute("regionalViewMode_Key")));
					}

					String disabledVal = (String) jspContext.getAttribute("regionalDisabled");
					if (disabledVal == null || disabledVal == " ") {
						jspContext.setAttribute("regionalDisabled",(String) request.getAttribute((String) jspContext.getAttribute("regionalDisabled_Key")));
					}
					
			
					ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext(request);
					RegionalMetaDataProcessingBean regionalMetaDataProcessingBean = (RegionalMetaDataProcessingBean) applicationContext.getBean("regionalMetaDataProcessingBean");
					Map<String,Object>  regionalDataMap =(Map<String,Object>) application.getAttribute("regionalMetaDataMapContext");
					
					
					String regionalPath=regionalMetaDataProcessingBean.getRegionalPathAttribute(fieldName, sourceEntityName,regionalDataMap);
			
					jspContext.setAttribute("regionalPath", regionalPath);	
					jspContext.setAttribute("fieldName", fieldName);
					
				}else{
					jspContext.setAttribute("fieldName", "");
				}
				
	%>
	
<c:if test="${regionalMandatory eq true}">
	<c:set var="validators" scope="page">
			${regionalValidators} required
		</c:set>
</c:if>

<c:if test="${not empty regionalViewMode}">
		<c:if test="${regionalViewMode eq true}">
			<c:set var="regionalDisabled" value="${regionalViewMode}" scope="page" />
		</c:if>
</c:if>

<c:if test="${regionalDisabled eq ''}">
		<c:set var="regionalDisabled" value="false" scope="page" />
	</c:if>

<c:if test="${regionalMandatory eq false}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>

<c:if test="${not empty fieldName && not empty regionalPath && not empty calendarType}">
	<div id="<c:out value='${regionalId}' />-control-group"
		class="form-group <c:out value='${spanClass}' />  ${nonMandatoryClass} ">
		<c:if test="${not empty regionalLabelKey}">
			<label><strong><spring:message code="${regionalLabelKey}"></spring:message></strong>
			<c:if test="${regionalMandatory eq true}">
					<span class='color-red'>*</span>
				</c:if></label>
		</c:if>
		<div class="input-group input-group" data-date-format='${regionalDateFormat}' id="datepicker_${regionalId}" 
					data-block-calander='${regionalDisabled}' style="margin-bottom:0px">			
			
				<div class="col-sm-8" style="width: 118px;">
					<form:input 
					cssClass="form-control ${spanClass} ${validators} ${nonMandatoryClass} validateRegionalDateFormat  float-l"
					id="${regionalId}" type="text" tabindex="${regionalTabindex}"
					path="${regionalPath}" pastDefaultDate="${regionalPastDefaultDate}"
					disabled="${regionalDisabled}" defDate="${regionalDefDate}" maxlength="${regionalMaxLength}"
					cssStyle="width: 91px;"   />
				</div>		    
	    </div>	
	     <c:if test="${disableRegionalDateFormat eq false}">
					<div class="help-block"><c:out value='${regionalDateFormat}'/></div>
		</c:if>		
	 </div>
 <script>
initRegionalDatePickerTag('${regionalId}','${regionalId}_span','${regionalTooltipMessage}','${calendarType}','${regionalDateFormat}');
</script>
 
</c:if>