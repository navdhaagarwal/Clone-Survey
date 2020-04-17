<%@tag import="com.nucleus.regional.metadata.service.IRegionalMetaDataService"%>
<%@ tag import="org.springframework.context.ApplicationContext"%>
<%@tag
	import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@tag
	import="com.nucleus.regional.metadata.RegionalMetaDataProcessingBean"%>
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
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
	
<%@ attribute name="regionalId" required="true"%>
<%@ attribute name="regionalValueKey"%>
<%@ attribute name="regionalButtonType"%>
<%@ attribute name="regionalOnClickEvent"%>
<%@ attribute name="regionalCssClass"%>
<%@ attribute name="regionalEnableHide"%>
<%@ attribute name="name"%>		

<c:if test="${not empty name}">
	<c:set var="fieldName" value="${name}" scope="page" />
</c:if>

<c:if test="${not empty fieldName}">

			<c:set var="regionalLabelKey_Key" value="${fieldName}_label" scope="page" />				
			<c:set var="regionalViewMode_Key" value="${fieldName}_viewMode" scope="page" />			
			
			
</c:if>

<%
		String fieldName= (String)jspContext.getAttribute("fieldName");
		
		
		if( fieldName !=null && fieldName != ""){

			
			String name = (String) jspContext.getAttribute("name");
			
			
			if (name == null) {
			throw new SystemException(
					"Attribute 'name' must be specified");
			}		
		
			jspContext.setAttribute("regionalLabelKey",(String)request.getAttribute((String)jspContext.getAttribute("regionalLabelKey_Key")));		
			jspContext.setAttribute("regionalViewMode",(String)request.getAttribute((String)jspContext.getAttribute("regionalViewMode_Key")));
			
			
			jspContext.setAttribute("fieldName", fieldName);

		}else{
			jspContext.setAttribute("fieldName", "");
		}
		
%>


<c:if test="${not empty fieldName}">
	
	<c:if test="${not empty name && not empty regionalLabelKey}">
				<neutrino:button valueKey="${regionalLabelKey}" buttonType="${regionalButtonType}"
						cssClass="${regionalCssClass}" id="${regionalId}"
						onClickEvent="${regionalOnClickEvent}" enableHide="${regionalEnableHide}" />
	</c:if>
	
</c:if>
