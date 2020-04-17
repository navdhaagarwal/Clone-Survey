<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@tag import="com.nucleus.contact.EMailInfo"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://www.springframework.org/security/tags"
	prefix="security"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<script type="text/javascript">
	
	function VerifyMail(id) {
		$("#email-message-box-" + id).addClass('block-no');
		$("#email-message-box-" + id).html('');
		var emailAddress = $("#" + id).val();
		if (emailAddress == '') {
			alert("Email Address is blank");
			return;
		}
		var filter = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
		if (!filter.test(emailAddress)) {
			alert('Please provide a valid email address');
			return false;
		}
		$.ajax({
			type : "POST",
			url : getContextPath() + "/app/emailApproval/verifyEmailAddress",
			data : ({
				emailAddress : emailAddress
			}),
			success : function(result) {
				if (result == 'success') {
					sendVerificationMail(id,emailAddress);					
				} else {
					alert('Please provide a valid domain name');
				}
			}
		});
	}
	function sendVerificationMail(id,emailAddress){
		$
		.ajax({
			type : "POST",
			url : getContextPath() + "/app/emailApproval/sendVerificationMail",
			data : ({
				emailAddress : emailAddress
			}),
			success : function(result) {
				if (result == 'success') {
					$("#email-message-box-" + id).removeClass(
							'block-no');
					$("#email-message-box-" + id)
							.html(
									'A verification email is being sent to your Email Id');
				} else {
					$("#email-message-box-" + id).removeClass(
							'block-no');
					$("#email-message-box-" + id).html(
							'Problem in Verifying Email Id');
				}
			},
			error : function(jqXHR, textStatus, errorThrown) {
				$("#email-message-box-" + id).removeClass('block-no');
				$("#email-message-box-" + id).html(
						'Problem in Verifying Email Id');
			}

		});
	}
</script>

<%@ attribute name="disabled"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="maxLength"%>
<%@ attribute name="path"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="emailBoxColSpan"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="name"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="validationRequired"%>
<%@ attribute name="validationAuthorityRole"%>
<%@ attribute name="verifyField"%>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="conditionStatement"%>
<%@ attribute name="conditionValue"%>
<%@ attribute name="maskedValue"%>
<%@ attribute name="maskedPath"%>
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
<c:if test="${not empty path}">
	<c:set var="emailAddress" value="${path}.emailAddress" scope="page">
	</c:set>
	<c:set var="persistedId" value="${path}.id" scope="page"></c:set>
	<c:set var="verifyStatus" value="${path}.verified" scope="page"></c:set>
</c:if>


<c:if test="${empty path}">
	<c:if test="${not empty name}">
		<c:set var="emailAddress" value="${name}.emailAddress" scope="page">
		</c:set>
		<c:set var="persistedId" value="${name}.id" scope="page"></c:set>
		<c:set var="verifyStatus" value="${path}.verified" scope="page"></c:set>
	</c:if>
</c:if>
<c:if test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true))}">
      <c:if test="${not empty maskedValue}">
   	<c:set var="value" value="${maskedValue}" scope="page" />
   	</c:if>
   	   <c:if test="${not empty maskedValue && not empty maskedPath}">
   <c:set var="emailAddress" value="${path}.transientMaskingMap['emailAddress']" scope="page">
	</c:set>
      </c:if>
   </c:if>
<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
			${validators} required
	</c:set>
</c:if>
<c:set var="inputMaxLength" value="255" scope="page" />
<c:if test="${maxLength ge 0}">
	<c:set var="inputMaxLength" value="${maxLength}" scope="page" />
</c:if>
<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>

<c:set var="emailBoxpanClass" value="col-sm-10" scope="page" />
<c:if test="${not empty emailBoxColSpan}">
	<c:set var="emailBoxpanClass" value="col-sm-${emailBoxColSpan}"
		scope="page" />
</c:if>

<c:if test="${not empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}"></spring:message>
	</c:set>
</c:if>
<c:if test="${not empty validationRequired}">
	<c:set var="validationRequired" value="true" scope="page"></c:set>
</c:if>
<c:if test="${not empty validationAuthorityRole}">
	<c:set var="role" value="${validationAuthorityRole}" scope="page"></c:set>
</c:if>
<%
    String name = (String) jspContext.getAttribute("name");
    String path = (String) jspContext.getAttribute("path");

    if (name == null && path == null) {
        throw new SystemException("Either of attributes 'name' or 'path' must be specified");
    } else if (name != null && path != null) {
        throw new SystemException("Either of attributes 'name' or 'path' can be specified at once");
    }
%>

<c:set var="spanClass" value="col-sm-${colSpan}" scope="page" />
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

<div id="<c:out value='${id}' />-control-group"
	class="form-group email-ctrl-group input-group <c:out value='${spanClass}' /> ${nonMandatoryClass} reset-m-l">
	<c:if test="${not empty labelKey}">
		<label><strong><spring:message code="${labelKey}"></spring:message></strong>
			<c:if test="${not empty mandatory}">
				<span class='color-red'>*</span>
			</c:if> </label>
	</c:if>
	<%-- <div class="${emailBoxpanClass} reset-m"> --%>
	<c:choose>
		<c:when test="${not empty path}">
			<spring:bind path="${path}">
				<c:set var="preEvalValue" value="${status.value}"></c:set>
			</spring:bind>
			<c:choose>
				<c:when test="${viewMode eq true}">
					<form:input path="${emailAddress}"
						cssClass="form-control inputmask ${validators} ${emailBoxpanClass} email validEmail"
						id="${id}" placeholder="${placeHolderMessage}"
						disabled="${disabled}" tabindex="${tabindex}"
						data-original-title="${tooltipMessage}" readonly="${readOnly}"
						maxlength="${inputMaxLength}"></form:input>
				</c:when>
				<c:otherwise>
					<form:input path="${emailAddress}"
						cssClass="form-control inputmask ${validators} ${emailBoxpanClass} email validEmail"
						id="${id}" placeholder="${placeHolderMessage}"
						tabindex="${tabindex}" data-original-title="${tooltipMessage}"
						readonly="${readOnly}" maxlength="${inputMaxLength}"></form:input>
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:when test="${not empty name}">
			<c:choose>
				<c:when test="${viewMode eq true}">
					<input type="text" name="<c:out value='${emailAddress}' />"
						class="form-control inputmask <c:out value='${validators}' /> <c:out value='${emailBoxpanClass}' /> email validEmail"
						id="<c:out value='${id}' />" placeholder="${placeHolderMessage}"
						disabled="<c:out value='${disabled}' />" value="<c:out value='${value}' />"
						data-original-title="<c:out value='${tooltipMessage}' />" tabindex="<c:out value='${tabindex}' />"
						maxlength="<c:out value='${inputMaxLength}' />">
				</c:when>
				<c:otherwise>
					<input type="text" name="<c:out value='${emailAddress}' />"
						class="form-control inputmask <c:out value='${validators}' /> <c:out value='${emailBoxpanClass}' /> email validEmail"
						id="<c:out value='${id}' />" placeholder="${placeHolderMessage}" value="<c:out value='${value}' />"
						data-original-title="<c:out value='${tooltipMessage}' />" tabindex="<c:out value='${tabindex}' />"
						readonly="<c:out value='${readOnly}' />" maxlength="<c:out value='${inputMaxLength}' />">
				</c:otherwise>
			</c:choose>
		</c:when>
	</c:choose>
	<c:if test="${validationRequired eq true && not empty persistedId}">
				<c:choose>
					<c:when test="${verifyField eq true}">
						<a href="#bottom" id="verified"><img
						alt="Verified"
						src="${pageContext.request.contextPath}/images/check_verified.png"></a>
					</c:when>
					<c:otherwise>
						<c:choose>
						<c:when test="${not empty role}">
							<security:authorize access="hasAnyAuthority('${role}')">
								<c:choose>
									<c:when test="${viewMode eq true}">
										<button id="verifybutton-<c:out value='${id}' />" class="btn" type="button" disabled="disabled">
											<spring:message code='label.emailtag.verifyemailid'></spring:message>
										</button>
									</c:when>
									<c:otherwise>
										<button id="verifybutton-<c:out value='${id}' />" class="btn" type="button"
											onClick="VerifyMail('<c:out value='${id}' />');">
											<spring:message code='label.emailtag.verifyemailid'></spring:message>
										</button>
									</c:otherwise>
								</c:choose>
							</security:authorize>
						</c:when>
						<c:otherwise>
							<c:choose>
								<c:when test="${viewMode eq true}">
									<button id="verifybutton-<c:out value='${id}' />" class="btn" type="button" disabled="disabled">
										<spring:message code='label.emailtag.verifyemailid'></spring:message>
									</button>
								</c:when>
								<c:otherwise>
									<button id="verifybutton-<c:out value='${id}' />" class="btn" type="button" onClick="VerifyMail('<c:out value='${id}' />');">
										<spring:message code='label.emailtag.verifyemailid'></spring:message>
									</button>
								</c:otherwise>
							</c:choose>
						</c:otherwise>
					</c:choose>
					</c:otherwise>
				</c:choose>
		<div id="email-message-box-<c:out value='${id}' />" class="block-no f-12"></div>
		<div id="serverValidationDiv-<c:out value='${id}' />" class="hide form-group">
			<span class="help-block"><p></p></span>
		</div>
	</c:if>
	</div>
<!-- </div> -->
<%
	String val = null;
	String fieldName = null;
	if (name == null) {
		EMailInfo eMailInfo = (EMailInfo) jspContext.getAttribute("preEvalValue");
		if(eMailInfo!=null){
			val = eMailInfo.getEmailAddress();
		}
		fieldName = path;
	} else {
		val = (String) jspContext.getAttribute("value");
		fieldName = name;
	}

	try {
		
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty()) {
			
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>