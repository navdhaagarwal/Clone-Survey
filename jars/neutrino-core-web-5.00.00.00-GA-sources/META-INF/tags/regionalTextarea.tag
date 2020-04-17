<%@tag import="java.util.Map"%>
<%@tag import="com.nucleus.regional.metadata.RegionalMetaDataProcessingBean"%>
<%@tag import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="com.nucleus.regional.RegionalEnabled"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>

<%@ attribute name="regionalId" required="true"%> 
<%@ attribute name="regionalPlaceHolderKey"%> 
<%@ attribute name="regionalDisabled"%> 
<%@ attribute name="regionalMaxLength" required="true"%> 
<%@ attribute name="regionalTooltipKey"%> 
<%@ attribute name="regionalErrorPath"%> 
<%@ attribute name="regionalValidators"%>
<%@ attribute name="regionalValue" type="com.nucleus.regional.RegionalEnabled"%>
<%@ attribute name="regionalRows"%>
<%@ attribute name="regionalColSpan" required="true"%> 
<%@ attribute name="regionalTextareaBoxColSpan"%> 
<%@ attribute name="regionalTabindex"%> 
<%@ attribute name="regionalCharacter"%> 
<%@ attribute name="name"%> 
<%@ attribute name="nameAttribute"%> 
<%@ attribute name="regionalViewMode" %>
<%@ attribute name="pathPrepender" %>
<%@ attribute name="value" %>
<%@ attribute name="regionalMoneyValue" %>

<c:if test="${not empty nameAttribute}">
	<c:set var="fieldName" value="${nameAttribute}" scope="page" />
</c:if>

<c:if test="${not empty name}">
	<c:set var="fieldName" value="${name}" scope="page" />
</c:if>

<c:if test="${not empty fieldName}">

			<c:set var="regionalLabelKey_Key" value="${fieldName}_label" scope="page" />			
			<c:set var="regionalMandatory_Key" value="${fieldName}_mandatoryMode" scope="page" />			
			<c:set var="regionalViewMode_Key" value="${fieldName}_viewMode" scope="page" />			
			<c:set var="sourceEntityName_Key" value="${fieldName}_sourceEntity" scope="page" />
			<c:set var="regionalPlaceHolderKey_Key" value="${fieldName}_placeHolderKey" scope="page" />
			<c:set var="regionalToolTipKey_Key" value="${fieldName}__toolTipKey" scope="page" />
			<c:set var="regionalDisabled_Key" value="${fieldName}_disabled" scope="page" />
</c:if>


<%
		String fieldName= (String)request.getAttribute((String)jspContext.getAttribute("fieldName"));
		
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
			
			jspContext.setAttribute("regionalPlaceHolderKey",(String)request.getAttribute((String)jspContext.getAttribute("regionalPlaceHolderKey_Key")));
			jspContext.setAttribute("regionalTooltipKey",(String)request.getAttribute((String)jspContext.getAttribute("regionalToolTipKey_Key")));
			
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
				jspContext.setAttribute("regionalMoneyValue", value);
			}		
			
			if(regionalValue!=null){
				Object regionalValueData=regionalMetaDataProcessingBean.fetchRegionalValueFromRegionalPath((Object)regionalValue,regionalPath);
				
				jspContext.setAttribute("regionalMoneyValue", regionalValueData);
			}
		
			jspContext.setAttribute("fieldName", fieldName);
			
			String pathPrepender = (String) jspContext
					.getAttribute("pathPrepender");
			if (pathPrepender != null && pathPrepender != "") {	
				StringBuilder newRegionalPath=new StringBuilder();
				newRegionalPath.append(pathPrepender).append(".").append(regionalPath);
				jspContext.setAttribute("regionalPath", newRegionalPath);		
			} else {
				jspContext.setAttribute("regionalPath", regionalPath);
			}
			
		}else{
				jspContext.setAttribute("fieldName", "");
		}



%>

<c:if test="${not empty fieldName}">
	
	<c:if test="${not empty nameAttribute && not empty regionalPath}">
	
			<c:if test="${regionalMandatory eq true}">
				<neutrino:textarea id="${regionalId}" 
					path="${regionalPath}"
					labelKey="${regionalLabelKey}"
					placeHolderKey="${regionalPlaceHolderKey}" 
					viewMode="${regionalViewMode}"
					errorPath="${regionalErrorPath}" 
					colSpan="${regionalColSpan}" 
					textareaBoxColSpan="${regionalTextareaBoxColSpan}"
					tooltipKey="${regionalTooltipKey}" 
					mandatory="${regionalMandatory}"
					maxLength="${regionalMaxLength}" disabled="${regionalDisabled}"
					character="${regionalCharacter}"
					isRegional="true">
				</neutrino:textarea>
			</c:if>
			<c:if test="${regionalMandatory eq false}">
				<neutrino:textarea id="${regionalId}" 
					path="${regionalPath}"
					labelKey="${regionalLabelKey}"
					placeHolderKey="${regionalPlaceHolderKey}" 
					viewMode="${regionalViewMode}" disabled="${regionalDisabled}"
					errorPath="${regionalErrorPath}" 
					colSpan="${regionalColSpan}" 
					textareaBoxColSpan="${regionalTextareaBoxColSpan}"
					tooltipKey="${regionalTooltipKey}"
					maxLength="${regionalMaxLength}"
				    character="${regionalCharacter}"
				    isRegional="true">
				</neutrino:textarea>
			</c:if>
	</c:if>
	
	<c:if test="${not empty name && not empty regionalPath }">
	
			<c:if test="${regionalMandatory eq true}">	
				<neutrino:textarea id="${regionalId}"
					name="${regionalPath}" disabled="${regionalDisabled}"
					value="${regionalMoneyValue}" 
					colSpan="${regionalColSpan}"
					labelKey="${regionalLabelKey}" 
					viewMode="${regionalViewMode}"
					tooltipKey="${regionalTooltipKey}"
					errorPath="${regionalErrorPath}"
					placeHolderKey="${regionalPlaceHolderKey}" 
					maxLength="${regionalMaxLength}" 
					mandatory="${regionalMandatory}"
					tabindex="${regionalTabindex}"
					character="${regionalCharacter}"
					isRegional="true">
				</neutrino:textarea>				
			</c:if>
			<c:if test="${regionalMandatory eq false}">
				<neutrino:textarea id="${regionalId}"
					name="${regionalPath}" disabled="${regionalDisabled}"
					value="${regionalMoneyValue}" 
					colSpan="${regionalColSpan}"
					labelKey="${regionalLabelKey}" 
					viewMode="${regionalViewMode}"
					tooltipKey="${regionalTooltipKey}"
					errorPath="${regionalErrorPath}"
					placeHolderKey="${regionalPlaceHolderKey}" 
					maxLength="${regionalMaxLength}" 
					tabindex="${regionalTabindex}" 
					character="${regionalCharacter}"
					isRegional="true">
				</neutrino:textarea>
			</c:if>
			
	</c:if>
</c:if>
