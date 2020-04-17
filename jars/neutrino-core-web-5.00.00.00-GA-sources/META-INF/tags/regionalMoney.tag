<%@tag import="com.nucleus.core.money.entity.Money"%>
<%@tag import="com.nucleus.regional.metadata.service.IRegionalMetaDataService"%>
<%@ tag import="org.springframework.context.ApplicationContext"%>
<%@tag
	import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
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
<%@ attribute name="regionalValue" type="com.nucleus.regional.RegionalData" %>
<%@ attribute name="name" required="true"%>
<%@ attribute name="regionalColSpan"%>
<%@ attribute name="regionalMoneyBoxColSpan"%>		
<%@ attribute name="regionalPlaceHolderKey"%>
<%@ attribute name="regionalDisabled"%>
<%@ attribute name="regionalReadOnly"%>
<%@ attribute name="regionalTooltipKey"%>
<%@ attribute name="regionalErrorPath"%>
<%@ attribute name="regionalValidators"%>
<%@ attribute name="regionalAcceptNegative"%>
<%@ attribute name="regionalViewMode" %>
<%@ attribute name="pathPrepender" %>



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

					
		String name = (String) jspContext.getAttribute("name");
						
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
		
		RegionalData regionalData = (RegionalData)jspContext.getAttribute("regionalValue");
 		if(regionalData!=null){
 			IRegionalMetaDataService regionalMetaDataService = (IRegionalMetaDataService) applicationContext.getBean("regionalMetaDataService");
 			Map<String, Object> map = regionalMetaDataService.getRegionalDataAttributeValue(regionalData, sourceEntityName);
 			Money fieldValue = null;
 			String filedColumnName = null;
 			jspContext.setAttribute("regionalValue", fieldValue);
 			if(map!=null && map.size()>0){
 				List<Object> list = (List)map.get(fieldName);
 				if(list!=null && list.size()>0){
 					fieldValue = (Money)list.get(0);
 					filedColumnName = (String)list.get(1);
 					
 					jspContext.setAttribute("regionalValue", fieldValue);
 				}
 			} 
 		}
		
		}else{
			jspContext.setAttribute("fieldName", "");
		}
		
%>


<c:if test="${not empty fieldName}">
	
	
	<c:if test="${not empty name && not empty regionalPath }">
	
			<c:if test="${regionalMandatory eq true}">	
				<neutrino:money id="${regionalId}" mandatory="${regionalMandatory}" value="${regionalValue}"
								name="${regionalPath}" moneyBoxColSpan="${regionalMoneyBoxColSpan}" colSpan="${regionalColSpan}"
								tooltipKey="${regionalTooltipKey}"  disabled="${regionalDisabled}"
								placeHolderKey="${regionalPlaceHolderKey}"
								labelKey="${regionalLabelKey}" 
								viewMode="${regionalViewMode}"  />
		
			</c:if>
			<c:if test="${regionalMandatory eq false}">
				<neutrino:money id="${regionalId}"  value="${regionalValue}"
								name="${regionalPath}" moneyBoxColSpan="${regionalMoneyBoxColSpan}" colSpan="${regionalColSpan}"
								tooltipKey="${regionalTooltipKey}" disabled="${regionalDisabled}"
								placeHolderKey="${regionalPlaceHolderKey}"
								labelKey="${regionalLabelKey}" 
								viewMode="${regionalViewMode}"  />
			</c:if>
			
	</c:if>
</c:if>
<script type="text/javascript">
	
/* this is being used instead of document.ready as it was not working in IE*/	
$(document).ready(function(){
				
				if ('${regionalLabelKey}' != "") {
					$("#${regionalId}").bind(
							'change',
							function() {
				
								if ($("#hid_${regionalId}").val() != "") {

									var inputLen = $("#hid_${regionalId}").length;

									var moneyLabelClassVal = $(
											"#${regionalId}_moneyLabelDiv").attr(
											"class");

									if (moneyLabelClassVal != "") {
										$("#${regionalId}_moneyLabelDiv").removeClass(
												moneyLabelClassVal);

										if (inputLen > 1) {
											$("#${regionalId}_moneyLabelDiv").addClass(
													"span" + (inputLen - 1));
										} 
									}

								}
							});
				}

			

				$("#amount_${regionalId}").change(
						function() {

							if ($("#amount_${regionalId}").val() == "") {
								$("#amount_${regionalId}").val("");
								formMoneyValue("${regionalId}");
							}
							
							/* jQuery( '#amount_${id}' ).wrap( '<form id="temp_form_for_money" />' );
                            var isValid = jQuery( '#temp_form_for_money' ).valid();
                            jQuery( '#amount_${id}' ).unwrap(); */
                            
                            if ($("#amount_${regionalId}").valid()) {
								formatCurrency("${regionalId}",false,"${regionalAcceptNegative}");
							}
							
						});

				$("#listMoney_${regionalId}").focus(function() {
				}).change(
						function() {
							if ($("#amount_${regionalId}").val() == "") {
								$("#amount_${regionalId}").val("");
								formMoneyValue("${regionalId}");
							}
							if ($("#amount_${regionalId}").valid()) {
								formatCurrency("${regionalId}",false,"${regionalAcceptNegative}");
							}
						});

				

				if ( "${regionalViewMode}" != "" &&  "${regionalViewMode}" == 'true') {
					$("#listMoney_${regionalId}").attr("disabled", "disabled");

				}

			});


</script>
