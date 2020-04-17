<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>
<%@tag import="com.nucleus.core.NeutrinoSpringAppContextUtil"%>
<%@tag import="com.nucleus.security.masking.MaskingUtility"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ attribute name="prefixKey"%>
<%@ attribute name="suffixKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="disabled"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="maxLength"%>
<%@ attribute name="minLength"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="path"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="alignToolTip"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="inputMaskKey"%>
<%@ attribute name="validators"%>
<%@ attribute name="colSpan" required="true"%>
<%@ attribute name="inputBoxColSpan"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="name"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="textDirection"%>
<%@ attribute name="value"%>
<%@ attribute name="maskedValue"%>
<%@ attribute name="maskedPath"%>
<%@ attribute name="textAlign"%>
<%@ attribute name="inputCase"%>
<%@ attribute name="imgClass"%>
<%@ attribute name="imgId"%>
<%@ attribute name="autoComplete"%>
<%@ attribute name="isRegional"%>
<%@ attribute name="pathPrepender" %>
<%@ attribute name="onKeyUpEvent"%>
<%@ attribute name="onChangeEvent"%>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="maskingPolicyCode"%> 
<%@ attribute name="conditionStatement"%>
<%@ attribute name="conditionValue"%>
 
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
				
				String mandatory=null;
				String viewMode=null;
				String labelKey=null;
				String placeHolderKey=null;
				String tooltipKey=null;
				String isRegional = (String) jspContext.getAttribute("isRegional");
				String regionalVisibility=(String)request.getAttribute(fieldName+"_regionalVisibility");
				if(isRegional == null ){
					 mandatory=(String)request.getAttribute(fieldName+"_mandatoryMode");					
					 viewMode=(String)request.getAttribute(fieldName+"_viewMode");					
					 labelKey=(String)request.getAttribute(fieldName+"_label");
					 placeHolderKey=(String)request.getAttribute(fieldName+"_placeHolderKey");					
					 tooltipKey=(String)request.getAttribute(fieldName+"_toolTipKey");						 
				}

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
				
				
				if(name!=null && maskingPolicyCode!=null){
					MaskingUtility  maskingUtility=NeutrinoSpringAppContextUtil.getBeanByName("maskingUtility", MaskingUtility.class);
					value = maskingUtility.getMaskedValue(maskingPolicyCode, value, "input");
				}
	%>

<c:if test="${not empty errorPath}">
    <c:set var="errorPathValue">
          <form:errors path="${errorPath}"/>
    </c:set>  
</c:if>
<c:if test="${empty tabindex}">
  <c:set var="tabindex" value="0" />
</c:if>
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
                <c:if test="${fn:trim(conditionParams[0]) eq 'minLength'}">
                    <c:set var = "minLength" value = "${fn:trim(conditionParams[1])}" scope="page" />
                </c:if>
            </c:forEach>
        </c:if>
    </c:forEach>
</c:if>

<c:if test="${regionalVisibility eq true}">
	
	<c:set var="inputCaseVar" value="" scope="page" />
	<c:if test="${not empty inputCase}">
		<c:if test="${inputCase eq 'U'}">
			<c:set var="inputCaseVar" value="inputCaseUpper" scope="page" />
		</c:if>
		<c:if test="${inputCase eq 'L'}">
			<c:set var="inputCaseVar" value="inputCaseLower" scope="page" />
		</c:if>
	</c:if>
	
	<c:set var="colSpanClass" value="" scope="page" />
	<c:if test="${not empty colSpan}">
		<c:set var="colSpanClass" value="col-sm-${colSpan}" scope="page" />
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
	<c:if test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true))}">
      <c:if test="${not empty maskedValue}">
   	<c:set var="value" value="${maskedValue}" scope="page" />
   	</c:if>
   	   <c:if test="${not empty maskedValue && not empty maskedPath}">
   	<c:set var="path" value="${maskedPath}" scope="page" />
      </c:if>
   </c:if>
	<c:set var="inputBoxSpanClass" value="col-sm-10" scope="page" />
	<c:if test="${not empty inputBoxColSpan}">
		<c:set var="inputBoxSpanClass" value="col-sm-${inputBoxColSpan}"
			scope="page" />
	</c:if>
	
	
	<c:set var="inputMaxLength" value="255" scope="page" />
	<c:if test="${maxLength ge 0}">
		<c:set var="inputMaxLength" value="${maxLength}" scope="page" />
	</c:if>
	
	
	<c:if test="${maxLength ge 0}">
		<c:set var="inputMinLength" value="${minLength}" scope="page" />
	</c:if>
	
	<c:if test="${not empty mandatory}">
		<c:set var="validators" scope="page">
				${validators} required
			</c:set>
	</c:if>
	
	<c:if test="${empty mandatory}">
		<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
	</c:if>
	
	<c:if test="${not empty placeHolderKey}">
		<c:set var="placeHolderMessage" scope="page">
			<spring:message code="${placeHolderKey}" />
		</c:set>
	
	</c:if>
	
	<c:if test="${not empty inputMaskKey}">
		<c:set var="inputMask" scope="page">
			<spring:message code="${inputMaskKey}"></spring:message>
		</c:set>
	</c:if>
	
	<c:if test="${not empty tooltipKey}">
		<c:set var="tooltipMessage" scope="page">
			<spring:message code="${tooltipKey}"></spring:message>
		</c:set>
	</c:if>
	
	<c:if test="${empty textDirection}">
		<c:set var="textDirection" value="ltr" scope="page" />
	</c:if>
	<c:if test="${not empty textDirection}">
		<c:set var="textDirection" value="${textDirection}" scope="page" />
	</c:if>
	
	<div id="<c:out value='${id}'/>-control-group"
		class="tagInputText form-group input-group input-group <c:out value='${colSpanClass}' />  ${nonMandatoryClass}">
	
		<c:if test="${not empty labelKey}">
			<label><strong><spring:message code="${labelKey}"></spring:message>
				<c:if test="${not empty mandatory}">
					<span class="color-red">*</span>
				</c:if> </strong></label>
		</c:if>
	
		<c:if test="${not empty prefixKey}">
			<span class="input-group-addon"><spring:message code="${prefixKey}" /></span>
		</c:if>
	
	<%-- 	<%
		    		String name = (String) jspContext.getAttribute("name");
					String path = (String) jspContext.getAttribute("path");
	
					if (name == null && path == null) {
						throw new SystemException(
								"Either of attributes 'name' or 'path' must be specified");
					} else if (name != null && path != null) {
						throw new SystemException(
								"Either of attributes 'name' or 'path' can be specified at once");
					}
		%> --%>
	
		<c:if test="${not empty name}">
			<c:choose>
				<c:when test="${viewMode eq true}">
					<input type="text" name="<c:out value='${name}'/>"
						class="form-control inputmask <c:out value='${validators}'/> <c:out value='${inputBoxSpanClass}' />" id="<c:out value='${id}'/>" onkeyup="${onKeyUpEvent}" onchange="${onChangeEvent}"
						maxlength="<c:out value='${inputMaxLength}' />" minlength="<c:out value='${inputMinLength}' />" placeholder="${placeHolderMessage}"
						disabled="<c:out value='${disabled}' />" readonly="<cout value='${readOnly}'/>" value="<c:out value='${value}'/>"
						data-mask="${inputMask}" data-original-title="${tooltipMessage}"
						dir="<c:out value='${textDirection}' />" style="text-align:<c:out value='${textAlign}'/>;" tabindex="-1">
					<c:if test="${not empty imgClass}">
						<c:if test="${not empty imgId}">
							<a id="<c:out value='${imgId}'/>" href="#" tabindex="-1" style="pointer-events: none;"> <span class="input-group-addon"><i
									class="<c:out value='${imgClass}'/>"></i></span>
							</a>
							<span for="<c:out value='${id}'/>" generated="true" class="help-block" style=""></span>
						</c:if>
					</c:if>
				</c:when>
				<c:otherwise>
					<c:choose>
						<c:when test="${not empty readOnly && readOnly ne true}">
							<input type="text" name="<c:out value='${name}'/>"
								class="form-control inputmask <c:out value='${validators}'/> <c:out value='${inputBoxSpanClass}' />" id="<c:out value='${id}'/>"
								maxlength="<c:out value='${inputMaxLength}' />" minlength="<c:out value='${inputMinLength}' />"
								placeholder="${placeHolderMessage}" data-mask="${inputMask}" onkeyup="${onKeyUpEvent}" onchange="${onChangeEvent}"
								value="<c:out value='${value}'/>" readonly="<c:out value='${readOnly}'/>"
								data-original-title="${tooltipMessage}" dir="<c:out value='${textDirection}' />"
								style="text-align:<c:out value='${textAlign}'/>;" tabindex="<c:out value='${tabindex}'/>">
								<c:if test="${not empty imgClass}">
									<c:if test="${not empty imgId}">
										<a id="<c:out value='${imgId}'/>" href="#" tabindex="-1" style="pointer-events: none;"> <span class="input-group-addon"><i
												class="<c:out value='${imgClass}'/>"></i></span>
										</a>
										<span for="<c:out value='${id}'/>" generated="true" class="help-block" style=""></span>
									</c:if>
								</c:if>
						</c:when>
						<c:when test="${not empty readOnly && readOnly eq true}">
							<input type="text" name="<c:out value='${name}'/>"
								class="form-control inputmask <c:out value='${validators}'/> <c:out value='${inputBoxSpanClass}' />" id="<c:out value='${id}'/>"
								maxlength="<c:out value='${inputMaxLength}' />" minlength="<c:out value='${inputMinLength}' />"
								placeholder="${placeHolderMessage}" data-mask="${inputMask}" onkeyup="${onKeyUpEvent}" onchange="${onChangeEvent}"
								value="<c:out value='${value}'/>" readonly="<c:out value='${readOnly}'/>"
								data-original-title="${tooltipMessage}" dir="<c:out value='${textDirection}' />"
								style="text-align:<c:out value='${textAlign}'/>;" tabindex="-1">
								<c:if test="${not empty imgClass}">
									<c:if test="${not empty imgId}">
										<a id="<c:out value='${imgId}'/>" href="#" tabindex="-1" style="pointer-events: none;"> <span class="input-group-addon"><i
												class="<c:out value='${imgClass}'/>"></i></span>
										</a>
										<span for="<c:out value='${id}'/>" generated="true" class="help-block" style=""></span>
									</c:if>
								</c:if>
						</c:when>
						<c:otherwise>
							<input type="text" name="<c:out value='${name}'/>"
								class="form-control inputmask <c:out value='${validators}'/> <c:out value='${inputBoxSpanClass}' />" id="<c:out value='${id}'/>"
								maxlength="<c:out value='${inputMaxLength}' />" minlength="<c:out value='${inputMinLength}' />"
								placeholder="${placeHolderMessage}" data-mask="${inputMask}" onkeyup="${onKeyUpEvent}" onchange="${onChangeEvent}"
								value="${value}" data-original-title="${tooltipMessage}"
								dir="<c:out value='${textDirection}' />" style="text-align:<c:out value='${textAlign}'/>;"
								tabindex="<c:out value='${tabindex}'/>">
								<c:if test="${not empty imgClass}">
									<c:if test="${not empty imgId}">
										<a id="<c:out value='${imgId}'/>" href="#"> <span class="input-group-addon"><i
												class="<c:out value='${imgClass}'/>"></i></span>
										</a>
										<span for="<c:out value='${id}'/>" generated="true" class="help-block" style=""></span>
									</c:if>
								</c:if>
						</c:otherwise>
					</c:choose>
	
					
				</c:otherwise>
			</c:choose>
		</c:if>
		<c:if test="${not empty path}">
			<spring:bind path="${path}" >
				<c:set var="preEvalValue" value="${status.value}"></c:set>
			</spring:bind>
			<c:choose>
				<c:when test="${viewMode eq true}">
					<neutrino:maskedinput path="${path}" maskingPolicyCode="${maskingPolicyCode}"
				cssClass="form-control inputmask ${validators} ${inputCaseVar} ${inputBoxSpanClass}"
				id="${id}" maxlength="${inputMaxLength}" minlength="${inputMinLength}"
				placeholder="${placeHolderMessage}" disabled="${disabled}" 
				readonly="${readOnly}" tabindex="-1" onkeyup="${onKeyUpEvent}" onchange="${onChangeEvent}"
				data-mask="${inputMask}" data-original-title="${tooltipMessage}"
				dir="${textDirection}" style="text-align:${textAlign};"
				autocomplete="${autoComplete}"></neutrino:maskedinput>
				<c:if test="${not empty imgClass}">
					<c:if test="${not empty imgId}">
						<a id="<c:out value='${imgId}'/>" href="#" tabindex="-1" style="pointer-events: none;"> <span class="input-group-addon"><i
								class="<c:out value='${imgClass}'/>"></i></span>
						</a>
						<span for="<c:out value='${id}'/>" generated="true" class="help-block" style=""></span>
					</c:if>
				</c:if>
				</c:when>
				
				<c:otherwise>
					<c:choose>
						<c:when test="${readOnly eq true}">
							<neutrino:maskedinput path="${path}"  maskingPolicyCode="${maskingPolicyCode}"
							cssClass="form-control inputmask ${validators} ${inputCaseVar} ${inputBoxSpanClass}"
							id="${id}" maxlength="${inputMaxLength}" minlength="${inputMinLength}"
							placeholder="${placeHolderMessage}" disabled="${disabled}" 
							readonly="${readOnly}" tabindex="-1" onkeyup="${onKeyUpEvent}" onchange="${onChangeEvent}"
							data-mask="${inputMask}" data-original-title="${tooltipMessage}"
							dir="${textDirection}" style="text-align:${textAlign};"
							autocomplete="${autoComplete}"></neutrino:maskedinput>
							<c:if test="${not empty imgClass}">
								<c:if test="${not empty imgId}">
									<a id="<c:out value='${imgId}'/>" href="#" tabindex="-1" style="pointer-events: none;"> <span class="input-group-addon"><i
											class="<c:out value='${imgClass}'/>"></i></span>
									</a>
									<span for="<c:out value='${id}'/>" generated="true" class="help-block" style=""></span>
								</c:if>
							</c:if>
						</c:when>
	
						<c:otherwise>
							<form:input path="${path}" 
							cssClass="form-control inputmask ${validators} ${inputCaseVar} ${inputBoxSpanClass}"
							id="${id}" maxlength="${inputMaxLength}" minlength="${inputMinLength}"
							placeholder="${placeHolderMessage}" disabled="${disabled}" 
							readonly="${readOnly}" tabindex="${tabindex}" onkeyup="${onKeyUpEvent}" onchange="${onChangeEvent}"
							data-mask="${inputMask}" data-original-title="${tooltipMessage}"
							dir="${textDirection}" style="text-align:${textAlign};"
							autocomplete="${autoComplete}"></form:input>
							<c:if test="${not empty imgClass}">
								<c:if test="${not empty imgId}">
									<a id="<c:out value='${imgId}'/>" href="#"> <span class="input-group-addon"><i
											class="<c:out value='${imgClass}'/>"></i></span>
									</a>
									<span for="<c:out value='${id}'/>" generated="true" class="help-block" style=""></span>
								</c:if>
							</c:if>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
			
		</c:if>
	
		<c:if test="${not empty suffixKey}">
			<span class="input-group-addon"><spring:message code="${suffixKey}" /></span>
		</c:if>
	
		<c:if test="${not empty helpKey}">
			<span class="help-block"><spring:message code="${helpKey}" /></span>
		</c:if>
	
		<c:if test="${not empty messageKey}">
			<p class="text-info">
				<spring:message code="${messageKey}" />
			</p>
		</c:if>
	
		<c:if test="${not empty errorPathValue}">
			<span for="<c:out value='${id}'/>" generated="true" class="help-block" style=""><form:errors path="${errorPath}"/></span>
			<script>
			populateServerSideError('${id}');
			</script>
		</c:if>	
		
	</div>
	<script>
	applyTooltip('${id}', '${alignToolTip}');
	initializeInputTag('${id}', "${imgClass}", "${imgId}");
	</script>
</c:if>
<%
	String val = null;
	if (name == null) {
		val = (String) jspContext.getAttribute("preEvalValue");
	} else {
		val = (String) jspContext.getAttribute("value");
	}

	try {
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty()) {
			
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in input tag :" + e.getMessage());
	}
%>