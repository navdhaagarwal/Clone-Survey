<%@tag import="com.nucleus.web.WebDataBinderElClass"%>
<%@tag import="java.util.List"%>
<%@tag import="com.nucleus.regional.metadata.service.IRegionalMetaDataService"%>
<%@tag import="com.nucleus.regional.RegionalData"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="java.util.Map"%>
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
<%@ attribute name="regionalValue" type="com.nucleus.regional.RegionalData"%>
 <%@ attribute name="regionalColSpan"%>
<%@ attribute name="regionalSelectBoxColSpan"%>
<%@ attribute name="regionalTooltipKey"%>
<%@ attribute name="regionalToolTip"%>
<%@ attribute name="nameAttribute"%>
<%@ attribute name="name"%>
<%@ attribute name="regionalSelectedGenericParameterId" type="com.nucleus.regional.RegionalData"%>
<%@ attribute name="regionalValidationClass"%>
<%@ attribute name="regionalDisabled" %>
<%@ attribute name="regionalViewMode" %>
<%@ attribute name="pathPrepender" %>
<%@ attribute name="list" type="java.util.List" %>

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
	<c:set var="regionalGenericParameterType_key" value="${fieldName}_regionalGenericParameterType"	scope="page" />	
	<c:set var="isGeneric_key" value="${fieldName}_isGeneric"	scope="page" />	
	<c:set var="regionalDisabled_Key" value="${fieldName}_disabled" scope="page" />
	<c:set var="regionalItemValue_key" value="${fieldName}_regionalItemValue"	scope="page" />	
	<c:set var="regionalItemLabel_key" value="${fieldName}_regionalItemLabel"	scope="page" />			
	<c:set var="regionalListValue_key" value="${fieldName}_regionalListValue"	scope="page" />	 
	<c:set var="regionalItemCode_key" value="${fieldName}_regionalItemCode"	scope="page" />	
	
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
		
		jspContext.setAttribute("regionalGenericParameterType",(String)request.getAttribute((String)jspContext.getAttribute("regionalGenericParameterType_key")));
		
		jspContext.setAttribute("isGenericalParameter",(String)request.getAttribute((String)jspContext.getAttribute("isGeneric_key")));
		
		jspContext.setAttribute("regionalItemValue",(String)request.getAttribute((String)jspContext.getAttribute("regionalItemValue_key")));
		
		jspContext.setAttribute("regionalItemLabel",(String)request.getAttribute((String)jspContext.getAttribute("regionalItemLabel_key")));	
		jspContext.setAttribute("regionalItemCode",(String)request.getAttribute((String)jspContext.getAttribute("regionalItemCode_key")));	
		
		String regionalListNameKey= (String)request.getAttribute((String)jspContext.getAttribute("regionalListValue_key"));
		List listValuesName=(List)jspContext.getAttribute("list");
		
		if(listValuesName!=null && listValuesName.size()>0 && regionalListNameKey!=null){
			throw new SystemException(
					"Either of attributes 'regionalListValue' or 'listValue' cannot be specified at once");
		}
		
		if(listValuesName!=null && listValuesName.size()>0){
			jspContext.setAttribute("regionalItemsValue",listValuesName);
		}
		
		List binderData = null;
		if(regionalListNameKey!=null && regionalListNameKey != ""){			
			binderData = (List)WebDataBinderElClass.getWebDataBinderData(regionalListNameKey);			
			jspContext.setAttribute("regionalItemsValue", binderData);
		}		
		
		
		ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext(request);
		RegionalMetaDataProcessingBean regionalMetaDataProcessingBean = (RegionalMetaDataProcessingBean) applicationContext.getBean("regionalMetaDataProcessingBean");
		Map<String,Object>  regionalDataMap =(Map<String,Object>) application.getAttribute("regionalMetaDataMapContext");
		String regionalPath=regionalMetaDataProcessingBean.getRegionalPathAttribute(name, sourceEntityName,regionalDataMap);
		
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
		
		
 		RegionalData regionalData = (RegionalData)jspContext.getAttribute("regionalSelectedGenericParameterId");
 		if(regionalData==null){
 			regionalData = (RegionalData)jspContext.getAttribute("regionalValue");
 		}
 		
		IRegionalMetaDataService regionalMetaDataService = (IRegionalMetaDataService) applicationContext.getBean("regionalMetaDataService");
		Map<String, Object> map = regionalMetaDataService.getRegionalDataAttributeValue(regionalData, sourceEntityName);
		Long fieldValue = 0L;
		String filedColumnName = null;
		
		jspContext.setAttribute("regionalSelectedValueId", fieldValue);
		if(map!=null && map.size()>0){
			List<Object> list = (List)map.get(fieldName);
			if(list!=null && list.size()>0){
				fieldValue = (Long)list.get(0);
				filedColumnName = (String)list.get(1);
				
				jspContext.setAttribute("regionalSelectedValueId", fieldValue);
			}
		} 
		
		}else{
			jspContext.setAttribute("fieldName", "");
		}
					
%>

					
				
		
<c:if test="${not empty fieldName}">
		
	<c:choose>
		<c:when test="${not empty nameAttribute && not empty regionalPath && isGenericalParameter eq true}">
						
		
			<c:if test="${regionalMandatory eq true}">
						
						 <neutrino:neutrino-select id="${regionalId}" colSpan="${regionalColSpan}"
								genericParameterType="${regionalGenericParameterType}"
								selectBoxColSpan="${regionalSelectBoxColSpan}" viewMode="${regionalViewMode}"
								genericParameterPath="${regionalPath}"
								selectedGenericParameterId="${regionalSelectedValueId}"
								label="${regionalLabelKey}" data-code="${regionalItemCode}"
								toolTip="${regionalToolTip}" mandatory="${regionalMandatory}"
								validationClass="${regionalValidationClass}"
						></neutrino:neutrino-select> 
			</c:if>	
			<c:if test="${regionalMandatory eq false}">
						
						<neutrino:neutrino-select id="${regionalId}" colSpan="${regionalColSpan}"
								genericParameterType="${regionalGenericParameterType}"
								selectBoxColSpan="${regionalSelectBoxColSpan}" viewMode="${regionalViewMode}"
								genericParameterPath="${regionalPath}" data-code="${regionalItemCode}"
								selectedGenericParameterId="${regionalSelectedValueId}"
								label="${regionalLabelKey}"
								toolTip="${regionalToolTip}" 
								validationClass="${regionalValidationClass}"
						></neutrino:neutrino-select>
			</c:if>	
		</c:when>	
		<c:otherwise>
		
			<c:if test="${not empty nameAttribute && not empty regionalPath}">
			  <c:if test="${regionalMandatory eq true}">
					
				<neutrino:select id="${regionalId}"
							mandatory="${regionalMandatory}" path="${regionalPath}"
							labelKey="${regionalLabelKey}" disabled="${regionalDisabled}" itemCode="${regionalItemCode}"
							itemValue="${regionalItemValue}" items="${regionalItemsValue}"
							itemLabel="${regionalItemLabel}" viewMode="${regionalViewMode}"
							colSpan="${regionalColSpan}"
							selectBoxColSpan="${regionalSelectBoxColSpan}"
				></neutrino:select>	
			</c:if>	
			<c:if test="${regionalMandatory eq false}">
			
				<neutrino:select id="${regionalId}"
							path="${regionalPath}" disabled="${regionalDisabled}"
							labelKey="${regionalLabelKey}" itemCode="${regionalItemCode}"
							itemValue="${regionalItemValue}" items="${regionalItemsValue}"
							itemLabel="${regionalItemLabel}" viewMode="${regionalViewMode}"
							colSpan="${regionalColSpan}"
							selectBoxColSpan="${regionalSelectBoxColSpan}"
				></neutrino:select>	
			</c:if>	
		</c:if>
		<c:if test="${not empty name && not empty regionalPath}">
		
			<c:if test="${regionalMandatory eq true}">
					<neutrino:select id="${regionalId}" name="${regionalPath}" disabled="${regionalDisabled}"
							labelKey="${regionalLabelKey}" items="${regionalItemsValue}" itemCode="${regionalItemCode}"
							itemValue="${regionalItemValue}" itemLabel="${regionalItemLabel}"
							tooltipKey="${regionalTooltipKey}" colSpan="${regionalColSpan}" selectBoxColSpan="${regionalSelectBoxColSpan}"
							viewMode="${regionalViewMode}" mandatory="${regionalMandatory}" 
							value="${regionalSelectedValueId}" />
			</c:if>	
			<c:if test="${regionalMandatory eq false}">
				
					<neutrino:select id="${regionalId}" name="${regionalPath}" disabled="${regionalDisabled}"
							labelKey="${regionalLabelKey}" items="${regionalItemsValue}" itemCode="${regionalItemCode}"
							itemValue="${regionalItemValue}" itemLabel="${regionalItemLabel}"
							tooltipKey="${regionalTooltipKey}" colSpan="${regionalColSpan}" selectBoxColSpan="${regionalSelectBoxColSpan}"
							viewMode="${regionalViewMode}" 
							value="${regionalSelectedValueId}" />
			</c:if>	
		</c:if>
		</c:otherwise>
	</c:choose>
</c:if>

	
