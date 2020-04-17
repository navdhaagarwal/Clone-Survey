<%@tag import="com.nucleus.web.WebDataBinderElClass"%>
<%@tag import="java.util.List"%>
<%@tag import="com.nucleus.regional.metadata.service.IRegionalMetaDataService"%>
<%@tag import="com.nucleus.regional.RegionalData"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="java.util.Map"%>
<%@tag import="com.nucleus.regional.RegionalEnabled"%>
<%@tag
	import="com.nucleus.regional.metadata.RegionalMetaDataProcessingBean"%>
<%@tag
	import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>

<%@ attribute name="regionalId"%>
<%@ attribute name="regionalUrl"%>
<%@ attribute name="regionalParentId"%>
<%@ attribute name="regionalValue" type="com.nucleus.regional.RegionalEnabled"%>	
<%@ attribute name="value" %>
<%-- <%@ attribute name="regionalItems" type="java.util.List"%>--%>
 <%@ attribute name="regionalColSpan"%>
<%@ attribute name="regionalSelectBoxColSpan"%>
<%@ attribute name="regionalTooltipKey"%>
<%@ attribute name="nameAttribute"%>
<%@ attribute name="name"%>
<%@ attribute name="regionalDisabled"%>
<%@ attribute name="regionalViewMode" %>
<%@ attribute name="pathPrepender"%>
<%@ attribute name="regionalCascadeValue" %>


<c:if test="${not empty nameAttribute}">
	<c:set var="fieldName" value="${nameAttribute}" scope="page" />
</c:if>

<c:if test="${not empty name}">
	<c:set var="fieldName" value="${name}" scope="page" />
</c:if>


 


<c:if test="${not empty fieldName}">

	<c:set var="regionalLabelKey_Key" value="${fieldName}_label" scope="page" />
	<c:set var="regionalMandatory_Key" value="${fieldName}_mandatoryMode" scope="page" />
	<c:set var="regionalViewMode_Key" value="${fieldName}_viewMode"	scope="page" />
	<c:set var="sourceEntityName_Key" value="${fieldName}_sourceEntity"	scope="page" />	
	<c:set var="regionalItemValue_key" value="${fieldName}_regionalItemValue"	scope="page" />	
	<c:set var="regionalItemLabel_key" value="${fieldName}_regionalItemLabel"	scope="page" />			
	<c:set var="regionalListValue_key" value="${fieldName}_regionalListValue"	scope="page" />	 
	<c:set var="regionalParentId_Key" value="${fieldName}_regionalParentId" scope="page"/>
	<c:set var="regionalPlaceHolderKey_Key" value="${fieldName}_placeHolderKey" scope="page" />
	<c:set var="regionalToolTipKey_Key" value="${fieldName}_toolTipKey" scope="page" />
	<c:set var="regionalDisabled_Key" value="${fieldName}_disabled" scope="page" />
</c:if>

<%
		String fieldName = (String)request.getAttribute((String)jspContext.getAttribute("fieldName"));
		
		if( fieldName !=null && fieldName != ""){

		String nameAttribute = (String) jspContext.getAttribute("nameAttribute");
		
		String name = (String) jspContext.getAttribute("name");
		
		if (name == null && nameAttribute == null) {
			throw new SystemException(
					"Either of attributes 'name' or 'path' must be specified");
				} else if (name != null && nameAttribute != null) {
			throw new SystemException(
					"Either of attributes 'name' or 'path' can be specified at once");
		}
		
		if(name == null){
			name=nameAttribute;
		}
		
		String viewModeVal=(String)jspContext.getAttribute("regionalViewMode");
		if(viewModeVal==null || viewModeVal==""){
			jspContext.setAttribute("regionalViewMode",(String)request.getAttribute((String)jspContext.getAttribute("regionalViewMode_Key")));
		}
		String disabledVal=(String)jspContext.getAttribute("regionalDisabled");
		if(disabledVal==null || disabledVal==" "){
			jspContext.setAttribute("regionalDisabled",(String)request.getAttribute((String)jspContext.getAttribute("regionalDisabled_Key")));
		}
		String sourceEntityName= (String)request.getAttribute((String)jspContext.getAttribute("sourceEntityName_Key"));
		
		jspContext.setAttribute("regionalLabelKey",(String)request.getAttribute((String)jspContext.getAttribute("regionalLabelKey_Key")));			
		jspContext.setAttribute("regionalMandatory",(String)request.getAttribute((String)jspContext.getAttribute("regionalMandatory_Key")));
		
		jspContext.setAttribute("regionalItemValue",(String)request.getAttribute((String)jspContext.getAttribute("regionalItemValue_key")));
		jspContext.setAttribute("regionalItemLabel",(String)request.getAttribute((String)jspContext.getAttribute("regionalItemLabel_key")));	
		jspContext.setAttribute("regionalParentId", (String)request.getAttribute((String)jspContext.getAttribute("regionalParentId_Key")));
		String regionalListNameKey= (String)request.getAttribute((String)jspContext.getAttribute("regionalListValue_key"));
		jspContext.setAttribute("regionalPlaceHolderKey",(String)request.getAttribute((String)jspContext.getAttribute("regionalPlaceHolderKey_Key")));
		jspContext.setAttribute("regionalTooltipKey",(String)request.getAttribute((String)jspContext.getAttribute("regionalToolTipKey_Key")));
		
		List binderData = null;
		if(regionalListNameKey!=null && regionalListNameKey != ""){
			binderData = (List)WebDataBinderElClass.getWebDataBinderData(regionalListNameKey);			
		}		
		jspContext.setAttribute("regionalItemsValue", binderData);
		
		ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext(request);
		RegionalMetaDataProcessingBean regionalMetaDataProcessingBean = (RegionalMetaDataProcessingBean) applicationContext.getBean("regionalMetaDataProcessingBean");
		Map<String,Object>  regionalDataMap =(Map<String,Object>) application.getAttribute("regionalMetaDataMapContext");
		String regionalPath=regionalMetaDataProcessingBean.getRegionalPathAttribute(name, sourceEntityName,regionalDataMap);	
		
		Object regionalValue=(RegionalEnabled)jspContext.getAttribute("regionalValue");
		Object value=(Object)jspContext.getAttribute("value");			
		if(value!=null && regionalValue!=null)
		{
			throw new SystemException(
					"Either of attributes 'regionalValue' or 'value' can be specified at once");
		}
		if(value!=null)
		{
			jspContext.setAttribute("regionalCascadeValue", value);
		}		
		
		if(regionalValue!=null){
			Object regionalValueData=regionalMetaDataProcessingBean.fetchRegionalValueFromRegionalPath((Object)regionalValue,regionalPath);
			
			jspContext.setAttribute("regionalCascadeValue", regionalValueData);
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
		jspContext.setAttribute("fieldName", fieldName);
		
		}else{
			jspContext.setAttribute("fieldName", "");
		}
					
%>

					
				
		
<c:if test="${not empty fieldName}">
	
	<c:if test="${not empty nameAttribute && not empty regionalPath}">
	
			<c:if test="${regionalMandatory eq true}">
			
			<neutrino:cascadeSelect id="${regionalId}" parentId="${regionalParentId}" disabled="${regionalDisabled}"
						url="${regionalUrl}" placeHolderKey="${regionalPlaceHolderKey}" tooltipKey="${regionalTooltipKey}"
						path="${regionalPath}" viewMode="${regionalViewMode}" selectBoxColSpan="${regionalSelectBoxColSpan}"
						colSpan="${regionalColSpan}" items="${regionalItemsValue}" 
				   		itemValue="${regionalItemValue}" itemLabel="${regionalItemLabel}" 
				   		labelKey="${regionalLabelKey}" mandatory="${regionalMandatory}"
				   		tooltipKey="${regionalTooltipKey}" /> 
					 
			</c:if>
			<c:if test="${regionalMandatory eq false}">
				<neutrino:cascadeSelect id="${regionalId}" parentId="${regionalParentId}" disabled="${regionalDisabled}"
						url="${regionalUrl}" placeHolderKey="${regionalPlaceHolderKey}" tooltipKey="${regionalTooltipKey}"
						path="${regionalPath}" viewMode="${regionalViewMode}" selectBoxColSpan="${regionalSelectBoxColSpan}"
						colSpan="${regionalColSpan}" items="${regionalItemsValue}" 
				   		itemValue="${regionalItemValue}" itemLabel="${regionalItemLabel}" 
				   		labelKey="${regionalLabelKey}"
				   		tooltipKey="${regionalTooltipKey}" /> 
			</c:if>
	</c:if>
	
	<c:if test="${not empty name && not empty regionalPath }">
	
			<c:if test="${regionalMandatory eq true}">	
				<neutrino:cascadeSelect id="${regionalId}" parentId="${regionalParentId}"
						url="${regionalUrl}" placeHolderKey="${regionalPlaceHolderKey}" tooltipKey="${regionalTooltipKey}"
						name="${regionalPath}" viewMode="${regionalViewMode}" selectBoxColSpan="${regionalSelectBoxColSpan}"
						colSpan="${regionalColSpan}" items="${regionalItemsValue}" value="${regionalCascadeValue}"
				   		itemValue="${regionalItemValue}" itemLabel="${regionalItemLabel}"  disabled="${regionalDisabled}"
				   		labelKey="${regionalLabelKey}" mandatory="${regionalMandatory}"
				   		tooltipKey="${regionalTooltipKey}" /> 
			</c:if>
			<c:if test="${regionalMandatory eq false}">
				<neutrino:cascadeSelect id="${regionalId}" parentId="${regionalParentId}"
						url="${regionalUrl}"
						name="${regionalPath}" viewMode="${regionalViewMode}" selectBoxColSpan="${regionalSelectBoxColSpan}"
						colSpan="${regionalColSpan}" items="${regionalItemsValue}" value="${regionalCascadeValue}"
				   		itemValue="${regionalItemValue}" itemLabel="${regionalItemLabel}" disabled="${regionalDisabled}"
				   		labelKey="${regionalLabelKey}"
				   		tooltipKey="${regionalTooltipKey}" /> 
			</c:if>
			
	</c:if>
</c:if>
		