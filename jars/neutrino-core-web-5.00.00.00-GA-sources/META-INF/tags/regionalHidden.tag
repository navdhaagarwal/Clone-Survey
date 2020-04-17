<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@ tag import="org.springframework.context.ApplicationContext"%>
<%@ tag import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ tag import="org.springframework.context.ApplicationContext"%>
<%@ tag	import="com.nucleus.regional.metadata.RegionalMetaDataProcessingBean"%>
<%@ tag import="com.nucleus.regional.metadata.RegionalMetaData"%>
<%@tag import="com.nucleus.regional.RegionalEnabled"%>
<%@ tag import="java.util.Map"%>
<%@ attribute name="regionalId" %>
<%@ attribute name="nameAttribute"%>	
<%@ attribute name="name"%>	
<%@ attribute name="value" %>
<%@ attribute name="regionalValue" type="com.nucleus.regional.RegionalEnabled"%>	
<%@ attribute name="pathPrepender"%>
<%@ attribute name="regionalPath"%>
<%@ attribute name="regionalHiddenValue" %>
<c:if test="${not empty nameAttribute}">
	<c:set var="fieldName" value="${nameAttribute}" scope="page" />
</c:if>

<c:if test="${not empty name}">
	<c:set var="fieldName" value="${name}" scope="page" />
</c:if>

<c:if test="${not empty fieldName}">
			<c:set var="regionalViewMode_Key" value="${fieldName}_viewMode" scope="page" />			
			<c:set var="sourceEntityName_Key" value="${fieldName}_sourceEntity" scope="page" />			
</c:if>
<%
		String fieldName= (String)request.getAttribute((String)jspContext.getAttribute("fieldName"));
		
		if( fieldName !=null && fieldName != ""){
			
		String nameAttribute = (String) jspContext
				.getAttribute("nameAttribute");
		String name = (String) jspContext.getAttribute("name");

		if (name == null && nameAttribute == null) {
			throw new SystemException(
					"Either of attributes 'name' or 'path' must be specified");
		} else if (name != null && nameAttribute != null) {
			throw new SystemException(
					"Either of attributes 'name' or 'path' can be specified at once");
		}

		if (name == null) {
			name = nameAttribute;
		}

		String viewModeVal = (String) jspContext
				.getAttribute("regionalViewMode");
		if (viewModeVal == null || viewModeVal == " ") {
			jspContext.setAttribute("regionalViewMode",
					(String) request.getAttribute((String) jspContext
							.getAttribute("regionalViewMode_Key")));
		}
		String sourceEntityName = (String) request
				.getAttribute((String) jspContext
						.getAttribute("sourceEntityName_Key"));
		
		ApplicationContext applicationContext = RequestContextUtils
				.findWebApplicationContext(request);
		RegionalMetaDataProcessingBean regionalMetaDataProcessingBean = (RegionalMetaDataProcessingBean) applicationContext
				.getBean("regionalMetaDataProcessingBean");
		Map<String, Object> regionalDataMap = (Map<String, Object>) application
				.getAttribute("regionalMetaDataMapContext");
		String regionalPath = regionalMetaDataProcessingBean
				.getRegionalPathAttribute(name, sourceEntityName,
						regionalDataMap);
		
		jspContext.setAttribute("fieldName", fieldName);
		Object regionalValue=(RegionalEnabled)jspContext.getAttribute("regionalValue");
		Object value=(Object)jspContext.getAttribute("value");			
		if(value!=null && regionalValue!=null)
		{
			throw new SystemException(
					"Either of attributes 'regionalValue' or 'value' can be specified at once");
		}
		if(value!=null)
		{
			jspContext.setAttribute("regionalHiddenValue", value);
		}		
		
		if(regionalValue!=null){
			Object regionalValueData=regionalMetaDataProcessingBean.fetchRegionalValueFromRegionalPath((Object)regionalValue,regionalPath);
			
			jspContext.setAttribute("regionalHiddenValue", regionalValueData);
		}
		
		String pathPrepender = (String) jspContext
				.getAttribute("pathPrepender");
		
		if (pathPrepender != null && pathPrepender != "") {	
			
			StringBuilder newRegionalPath=new StringBuilder();
			newRegionalPath.append(pathPrepender).append(".").append(regionalPath);
			
			jspContext.setAttribute("regionalPath", newRegionalPath);		
		} else {
			
			jspContext.setAttribute("regionalPath", regionalPath);
		}

	} else {
		jspContext.setAttribute("fieldName", "");
	}
%>



<c:if test="${not empty nameAttribute}">
	<c:set var="fieldName" value="${nameAttribute}" scope="page" />
</c:if>

<c:if test="${not empty name}">
	<c:set var="fieldName" value="${name}" scope="page" />
</c:if>


<c:if test="${not empty fieldName}">
	
	<c:if test="${not empty name && not empty regionalPath }">	
		<neutrino:hidden name="${regionalPath}" id="${regionalId}" value="${regionalHiddenValue}"></neutrino:hidden>
	</c:if>
	
	
	<c:if test="${not empty nameAttribute && not empty regionalPath}">
		<neutrino:hidden path="${regionalPath}" id="${regionalId}" ></neutrino:hidden>
	</c:if>
	
	
</c:if>