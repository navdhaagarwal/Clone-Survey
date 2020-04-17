<%@tag import="java.util.HashMap"%>
<%@tag import="org.joda.time.DateTime"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<jsp:useBean id="selectedValueMap" class="java.util.HashMap" />
<%@ attribute name="disabled"%>
<%@ attribute name="id"%>
<%@ attribute name="path"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="phoneBoxColSpan"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="validators"%>
<%@ attribute name="name"%>
<%@ attribute name="valueExp"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="mobile" required="true"%>
<%@ attribute name="populateISOFromCountry"%>
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
            </c:forEach>
        </c:if>
    </c:forEach>
</c:if>
<c:if test="${not empty populateISOFromCountry}">
	<c:set var="populateISOFromCountry" value="${populateISOFromCountry}"
		scope="page">
	</c:set>
</c:if>
<c:if test="${empty populateISOFromCountry}">
	<c:set var="populateISOFromCountry" value="false">
	</c:set>
</c:if>

<c:if test="${not empty path}">
	<c:set var="completePath" value="${path}.">
	</c:set>
</c:if>
<c:if test="${empty path}">
	<c:if test="${not empty name}">
		<c:set var="completePath" value="${name}.">
		</c:set>
	</c:if>
</c:if>
<c:if test="${not empty mobile}">
	<c:set var="mobile" value="${mobile}">
	</c:set>
</c:if>
<c:if test="${empty mobile}">
	<c:set var="mobile" value="${false}">
	</c:set>
</c:if>
<c:if test="${not empty viewMode}">
	<c:if test="${viewMode eq true}">
		<c:set var="disabled" value="${viewMode}" scope="page" />
		<c:set var="placeHolderKey" value="" scope="page" />
		<c:set var="tooltipKey" value="" scope="page" />
	</c:if>
</c:if>
<c:if test="${not empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}"></spring:message>
	</c:set>
</c:if>
<c:set var="placeHolderStd" scope="page">
	<spring:message code="label.std"></spring:message>
</c:set>
<c:set var="placeHolderIsd" scope="page">
	<spring:message code="label.isd"></spring:message>
</c:set>
<c:set var="placeHolderNumber" scope="page">
	<spring:message code="label.number"></spring:message>
</c:set>
<c:set var="placeHolderExt" scope="page">
	<spring:message code="label.extension"></spring:message>
</c:set>
<c:set var="validatorsForExtn" scope="page">
	${validators}
</c:set>
<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
		<c:out value='${validators}' />
	</c:set>
	<c:if test="${mandatory eq true}">
		<c:set var="validators" scope="page">
			${validators} required
		</c:set>
	</c:if>
</c:if>
<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>
<c:set var="phoneBoxSpanClass" value="col-sm-10" scope="page" />
<c:if test="${not empty phoneBoxColSpan}">
	<c:set var="phoneBoxSpanClass" value="col-sm-${phoneBoxColSpan}"
		scope="page" />
</c:if>
<c:if test="${not empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}"></spring:message>
	</c:set>
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
<c:if test="${not empty  path}">
	<spring:bind path="${completePath}id">
		<c:set var="currentId" value="${status.actualValue}"></c:set>
	</spring:bind>
	<spring:bind path="${completePath}verified">
		<c:set var="currentVerified" value="${status.actualValue}"></c:set>
	</spring:bind>
	<spring:bind path="${completePath}Valid">
        <c:set var="isValid" value="${status.actualValue}"></c:set>
    </spring:bind>
	<spring:bind path="${completePath}codeExpirationTime">
		<c:set var="currentExpirationTime" value="${status.actualValue}"></c:set>
	</spring:bind>
	<spring:bind path="${completePath}verCodeDeliveryStatus">
		<c:set var="currentVerCodeDeliveryStatus" value="${status.actualValue}"></c:set>
	</spring:bind>
	<spring:bind path="${completePath}verCodeDelStatusMessage">
		<c:set var="currentVerCodeDelStatusMessage" value="${status.actualValue}"></c:set>
	</spring:bind>
	<spring:bind path="${path}">
		<c:set var="actualExpression" value="${status.expression}"></c:set>
	</spring:bind>
</c:if>
<c:if test="${not empty valueExp and empty path}">
	<c:set var="currentIdVal" value="${valueExp}.id" />
	<c:set var="currentVerifiedVal" value="${valueExp}.verified" />
	<c:set var="isValid" value="${valueExp}.Valid" />
	<c:set var="currentExpirationTimeVal"
		value="${valueExp}.codeExpirationTime" />
	<c:set var="currentVerCodeDeliveryStat"
		value="${valueExp}.verCodeDeliveryStatus" />
	<c:set var="currentVerCodeDelStatusMsg"
		value="${valueExp}.verCodeDelStatusMessage" />
	<spring:eval expression="${valueExp}" var="phoneNumberObject"></spring:eval>
	<c:if test="${not empty phoneNumberObject}">
		<spring:eval expression="${currentIdVal}" var="currentId"></spring:eval>
		<spring:eval expression="${currentVerifiedVal}" var="currentVerified"></spring:eval>
		<spring:eval expression="${isValid}" var="isValid"></spring:eval>
		<spring:eval expression="${currentExpirationTimeVal}"
			var="currentExpirationTime"></spring:eval>
		<spring:eval expression="${currentVerCodeDeliveryStat}"
			var="currentVerCodeDeliveryStatus"></spring:eval>
		<spring:eval expression="${currentVerCodeDelStatusMsg}"
			var="currentVerCodeDelStatusMessage"></spring:eval>
	</c:if>
</c:if>
<div class="phone_tag_top">
	<div id="<c:out value='${id}'/>-control-group"
		class="reset-m-l form-group phone-ctrl-group input-group input-group <c:out value='${spanClass}' /> ${nonMandatoryClass} <c:out value='${phoneBoxSpanClass}' />">
		<c:if test="${not empty labelKey}">
			<label><strong><spring:message code="${labelKey}"></spring:message></strong>
				<c:if test="${not empty mandatory && mandatory eq true}">
					<span class='color-red'>*</span>
				</c:if> </label>
		</c:if>
		<%-- <div class="${phoneBoxSpanClass}" class="reset-m"> --%>
		<c:if test="${mobile eq true}">
			<c:if test="${not empty viewMode}">
				<c:if test="${viewMode eq true}">
					<select id="<c:out value='${id}'/>select"
						class="form-control chosen_a select_country"
						disabled="<c:out value='${disabled}'/>" style="width: 80px;"
						tabindex="<c:out value='${tabindex}'/>"></select>
				</c:if>
			</c:if>
			<c:if test="${not empty viewMode}">
				<c:if test="${viewMode eq false}">
					<select id="<c:out value='${id}'/>select"
						class="form-control chosen_a select_country" style="width: 80px;"
						tabindex="<c:out value='${tabindex}'/>"></select>
				</c:if>
			</c:if>
			<c:if test="${empty viewMode}">
				<select id="<c:out value='${id}'/>select"
					class="form-control chosen_a select_country" style="width: 80px;"
					tabindex="<c:out value='${tabindex}'/>"></select>
			</c:if>
		</c:if>
		<c:if test="${mobile eq false}">
			<c:if test="${not empty viewMode}">
				<c:if test="${viewMode eq true}">
					<select id="<c:out value='${id}'/>_select"
						class="form-control chosen_a select_country"
						disabled="<c:out value='${disabled}'/>" style="width: 80px;"
						tabindex="<c:out value='${tabindex}'/>"></select>
				</c:if>
			</c:if>
			<c:if test="${not empty viewMode}">
				<c:if test="${viewMode eq false}">
					<select id="<c:out value='${id}'/>_select"
						class="form-control chosen_a select_country" style="width: 80px;"
						tabindex="<c:out value='${tabindex}'/>"></select>
				</c:if>
			</c:if>
			<c:if test="${empty viewMode}">
				<select id="<c:out value='${id}'/>_select"
					class="form-control chosen_a select_country" style="width: 80px;"
					tabindex="<c:out value='${tabindex}'/>"></select>
			</c:if>
		</c:if>
		<c:if test="${not empty valueExp}">
			<c:set var="countryCodeVal" value="${valueExp}.countryCode" />
			<c:set var="isdCodeVal" value="${valueExp}.isdCode" />
			<c:set var="stdCodeVal" value="${valueExp}.stdCode" />
			<c:set var="phoneNumberVal" value="${valueExp}.phoneNumber" />
			<c:set var="maskedphoneNumberVal" value="${valueExp}.transientMaskingMap['phoneNumber']" />
			<c:set var="extensionVal" value="${valueExp}.extension" />
			<spring:eval expression="${valueExp}" var="valueexpression"></spring:eval>
			<c:if test="${not empty valueexpression}">
				<spring:eval expression="${countryCodeVal}" var="countryCodeValue"></spring:eval>
				<spring:eval expression="${isdCodeVal}" var="isdCodeValue"></spring:eval>
				<spring:eval expression="${stdCodeVal}" var="stdCodeValue"></spring:eval>
				<spring:eval expression="${phoneNumberVal}" var="phoneNumberValue"></spring:eval>
				<spring:eval expression="${maskedphoneNumberVal}" var="maskedphoneNumberValue"></spring:eval>
				<c:if test="${not empty maskedphoneNumberValue}">
				<c:set var="maskedValue" value="${maskedphoneNumberValue}" />
				</c:if>
				<spring:eval expression="${extensionVal}" var="extensionValue"></spring:eval>
			</c:if>
		</c:if>
		<c:if test="${mobile eq true}">
			<div class="mobile-phn-container">

				<c:if test="${not empty name}">
					<c:if test="${not empty viewMode}">
						<c:if test="${viewMode eq false}">
							<input type="hidden"
								name="<c:out value='${completePath}' />countryCode"
								value="${countryCodeValue}"
								id="<c:out value='${id}'/>_countryCode" />

							<input type="hidden"
								name="<c:out value='${completePath}' />numberType.id"
								id="isMobileNumber_<c:out value='${id}'/>" />

							<input type="text"
								name="<c:out value='${completePath}' />isdCode"
								class="form-control col-sm-2 phoneValid" value="${isdCodeValue}"
								id="<c:out value='${id}'/>_isdCode"
								placeholder="${placeHolderIsd}" maxlength="4" tabindex="-1"
								readonly="readonly">
							<span> <input type="text"
								name="<c:out value='${completePath}' />phoneNumber"
								value="${phoneNumberValue}"
								class="form-control mask_phone phn_tag phn_tag_mobile <c:out value='${validators}'/> col-sm-6 digits"
								id="<c:out value='${id}'/>_phoneNumber"
								placeholder="${placeHolderMessage}" maxlength="13"
								tabindex="<c:out value='${tabindex}'/>" autocomplete="off">
							</span>
							<input type="hidden"
                                name="<c:out value='${completePath}' />Valid"
                                id="${id}_isValid" />
							<p class="error_div"></p>
							<input type="hidden" id="<c:out value='${id}'/>" />
							<c:set target="${selectedValueMap}"
								property="<c:out value='${completePath}' />countryCode"
								value="${countryCodeValue}" />
							<c:set target="${selectedValueMap}"
								property="<c:out value='${completePath}' />isdCode" value="${isdCodeValue}" />
							<c:set target="${selectedValueMap}"
								property="<c:out value='${completePath}' />phoneNumber"
								value="${phoneNumberValue}" />
							</c:if>
					</c:if>
				</c:if>

				<c:if test="${not empty name}">
					<c:if test="${not empty viewMode}">
						<c:if test="${viewMode eq true}">
							<input type="hidden"
								name="<c:out value='${completePath}' />countryCode"
								value="${countryCodeValue}"
								id="<c:out value='${id}'/>_countryCode" />
							<input type="hidden"
								name="<c:out value='${completePath}' />numberType.id"
								id="isMobileNumber_<c:out value='${id}'/>" />

							<input type="text"
								name="<c:out value='${completePath}' />isdCode"
								class="form-control col-sm-2 phoneValid" value="${isdCodeValue}"
								id="<c:out value='${id}'/>_isdCode"
								placeholder="${placeHolderIsd}" tabindex="-1"
								style="pointer-events: none" readonly="readOnly" maxlength="4">
							<span> <c:choose>
							<c:when test="${not empty maskedValue}"><input type="text"
								name="<c:out value='${completePath}' />transientMaskingMap['phoneNumber']"
								value="${maskedValue}"
								class="form-control mask_phone <c:out value='${validators}'/> col-sm-6 phn_tag phn_tag_mobile digits"
								id="<c:out value='${id}'/>_phoneNumber"
								placeholder="${placeHolderMessage}"
								tabindex="<c:out value='${tabindex}'/>"
								style="pointer-events: none"
								readonly="<c:out value='${readOnly}'/>" maxlength="13"
								autocomplete="off"></c:when>
						<c:otherwise>
						<input type="text"
								name="<c:out value='${completePath}' />phoneNumber"
								value="${phoneNumberValue}"
								class="form-control mask_phone <c:out value='${validators}'/> col-sm-6 phn_tag phn_tag_mobile digits"
								id="<c:out value='${id}'/>_phoneNumber"
								placeholder="${placeHolderMessage}"
								tabindex="<c:out value='${tabindex}'/>"
								style="pointer-events: none"
								readonly="<c:out value='${readOnly}'/>" maxlength="13"
								autocomplete="off">
						</c:otherwise>		
						</c:choose>		
							</span>
							<input type="hidden" id="<c:out value='${id}'/>" />
							<c:set target="${selectedValueMap}"
								property="<c:out value='${completePath}' />countryCode"
								value="${countryCodeValue}" />
							<c:set target="${selectedValueMap}"
								property="<c:out value='${completePath}' />isdCode" value="${isdCodeValue}" />
							<c:set target="${selectedValueMap}"
								property="<c:out value='${completePath}' />phoneNumber"
								value="${phoneNumberValue}" />
							</c:if>
					</c:if>
				</c:if>
				<c:if test="${not empty path}">
					<spring:bind path="${completePath}isdCode">
						<c:set var="isd_code" value="${status.actualValue}"></c:set>
					</spring:bind>
					<spring:bind path="${completePath}phoneNumber">
						<c:set var="phone_number" value="${status.actualValue}"></c:set>
					</spring:bind>
					<spring:bind path="${completePath}countryCode">
						<c:set var="country_code" value="${status.actualValue}"></c:set>
					</spring:bind>
					<spring:bind path="${completePath}Valid">
                        <c:set var="isValid" value="${status.actualValue}"></c:set>
                    </spring:bind>

					<form:hidden path="${completePath}countryCode"
						id="${id}_countryCode" />

					<input type="hidden" name="${actualExpression}.numberType.id"
						id="isMobileNumber_<c:out value='${id}'/>" />
					<form:input id="${id}_isdCode"
						cssClass="form-control col-sm-2 phoneValid"
						path="${completePath}isdCode" tabindex="-1" disabled="${disabled}"
						name="${completePath}isdCode" maxlength="4"
						placeholder="${placeHolderIsd}" readonly="readonly" />
					<span class="phonenumber_flexbox"> <c:choose>
							<c:when test="${not empty maskedValue && not empty maskedPath && (viewMode eq true||disabled eq true||readOnly eq true)}"><form:input id="${id}_phoneNumber"
							cssClass="form-control mask_phone phn_tag phn_tag_mobile phn_tag_mobile_check no-border-radius ${validators} col-sm-6 digits"
							path="${completePath}transientMaskingMap['phoneNumber']" tabindex="${tabindex}"
							autocomplete="off" disabled="${disabled}"
							name="${completePath}phoneNumber" maxlength="13"
							placeholder="${placeHolderMessage}" />
							</c:when>
							<c:otherwise>
							<form:input id="${id}_phoneNumber"
							cssClass="form-control mask_phone phn_tag phn_tag_mobile phn_tag_mobile_check no-border-radius ${validators} col-sm-6 digits"
							path="${completePath}phoneNumber" tabindex="${tabindex}"
							autocomplete="off" disabled="${disabled}"
							name="${completePath}phoneNumber" maxlength="13"
							placeholder="${placeHolderMessage}" /> 
							</c:otherwise>
						</c:choose>	
						 <c:if
							test="${not empty currentId && viewMode ne true}">
							<span class="ver_icons input-group-addon"><i
								class="${currentVerified ? 'glyphicon glyphicon-ok' : 'glyphicon glyphicon-check'} verifyPhone" tabindex=0></i>
							</span>
               		</c:if>
					</span>
     				<form:hidden name="<c:out value='${completePath}' />isValid"
                            id="${id}_isValid"
							path="${completePath}Valid"/>
					<p class="error_div"></p>
					<input type="hidden" id="<c:out value='${id}'/>" />
					<c:set target="${selectedValueMap}"
						property="${completePath}countryCode" value="${country_code}" />
					<c:set target="${selectedValueMap}"
						property="${completePath}isdCode" value="${isd_code}" />
					<c:set target="${selectedValueMap}"
						property="${completePath}phoneNumber" value="${phone_number}" />
					</c:if>
			</div>
		</c:if>
		<c:if test="${mobile eq false}">
			<c:if test="${not empty path}">
				<spring:bind path="${completePath}isdCode">
					<c:set var="isd_code" value="${status.actualValue}"></c:set>
				</spring:bind>
				<spring:bind path="${completePath}phoneNumber">
					<c:set var="phone_number" value="${status.actualValue}"></c:set>
				</spring:bind>
				<spring:bind path="${completePath}countryCode">
					<c:set var="country_code" value="${status.actualValue}"></c:set>
				</spring:bind>
				<spring:bind path="${completePath}extension">
					<c:set var="ext_ension" value="${status.actualValue}"></c:set>
				</spring:bind>
				<spring:bind path="${completePath}stdCode">
					<c:set var="std_code" value="${status.actualValue}"></c:set>
				</spring:bind>
				<form:hidden path="${completePath}countryCode"
					id="countryCode_${id}" />

				<input type="hidden" name="${actualExpression}.numberType.id"
					id="isLandlineNumber_<c:out value='${id}'/>" />
				<form:input id="isdCode_${id}" type="text"
					cssClass="form-control col-sm-2 ph-isd-code"
					path="${completePath}isdCode" tabindex="${tabindex}"
					disabled="${disabled}" placeholder="${placeHolderIsd}"
					maxlength="4" readonly="readonly" />
				<span> <form:input id="stdCode_${id}" type="text"
						cssClass="form-control ${validators} col-sm-2 ph-std-code phn_tag digits"
						path="${completePath}stdCode" placeholder="${placeHolderStd}"
						tabindex="${tabindex}" disabled="${disabled}" maxlength="4"
						readonly="${readOnly}" />
				</span>
				<span>  <c:choose>
							<c:when test="${not empty maskedValue && not empty maskedPath && (viewMode eq true||disabled eq true||readOnly eq true)}"><form:input id="phoneNumber_${id}" type="text"
						cssClass="form-control ${validators} ph-one-numbers col-sm-3 phn_tag digits"
						path="${completePath}transientMaskingMap['phoneNumber']"
						placeholder="${placeHolderNumber}" tabindex="${tabindex}"
						disabled="${disabled}" maxlength="8" readonly="${readOnly}" />
						</c:when>
						<c:otherwise>
						<form:input id="phoneNumber_${id}" type="text"
						cssClass="form-control ${validators} ph-one-numbers col-sm-3 phn_tag digits"
						path="${completePath}phoneNumber"
						placeholder="${placeHolderNumber}" tabindex="${tabindex}"
						disabled="${disabled}" maxlength="8" readonly="${readOnly}" />
						</c:otherwise>
				</c:choose>		
				</span>
				<span> <form:input id="extension_${id}" type="text"
						cssClass="form-control ${validatorsForExtn} col-sm-3 ph-ext phn_tag digits"
						path="${completePath}extension" placeholder="${placeHolderExt}"
						tabindex="${tabindex}" disabled="${disabled}" maxlength="6"
						readonly="${readOnly}" />
				</span>
				<c:set target="${selectedValueMap}"
					property="${completePath}countryCode" value="${country_code}" />
				<c:set target="${selectedValueMap}"
					property="${completePath}isdCode" value="${isd_code}" />
				<c:set target="${selectedValueMap}"
					property="${completePath}phoneNumber" value="${phone_number}" />
				<c:set target="${selectedValueMap}"
					property="${completePath}stdCode_" value="${std_code}" />
				<c:set target="${selectedValueMap}"
					property="${completePath}extension" value="${ext_ension}" />
				<p class="error_div"></p>
			</c:if>
			<c:if test="${not empty name}">
				<c:if test="${not empty viewMode}">
					<c:if test="${viewMode eq true}">
						<input type="hidden"
							name="<c:out value='${completePath}' />countryCode"
							value="${countryCodeValue}"
							id="countryCode_<c:out value='${id}'/>" />
						<input type="hidden"
							name="<c:out value='${completePath}' />numberType.id"
							id="isLandlineNumber_<c:out value='${id}'/>" />
						<input id="isdCode_<c:out value='${id}'/>"
							class="form-control col-sm-2 ph-isd-code" type="text"
							name="<c:out value='${completePath}' />isdCode phn_tag"
							tabindex="-1" value="${isdCodeValue}"
							placeholder="${placeHolderIsd}" maxlength="4"
							style="pointer-events: none" readonly="readOnly" />
						<span> <input id="stdCode_<c:out value='${id}'/>"
							type="text"
							class="form-control <c:out value='${validators}'/> col-sm-2 phn_tag ph-std-code digits"
							name="<c:out value='${completePath}' />stdCode"
							value="${stdCodeValue}" placeholder="${placeHolderStd}"
							tabindex="<c:out value='${tabindex}'/>" maxlength="4"
							style="pointer-events: none"
							readonly="<c:out value='${readOnly}'/>" />
						</span>
						<span> <c:choose>
							<c:when test="${not empty maskedValue}"><input id="phoneNumber_<c:out value='${id}'/>"
							type="text" value="${maskedValue}"
							class="form-control <c:out value='${validators}'/> col-sm-3 ph-one-numbers phn_tag digits"
							name="<c:out value='${completePath}' />transientMaskingMap['phoneNumber']"
							placeholder="${placeHolderNumber}"
							tabindex="<c:out value='${tabindex}'/>" maxlength="8"
							style="pointer-events: none"
							readonly="<c:out value='${readOnly}'/>" />
							</c:when>
							<c:otherwise>
							<input id="phoneNumber_<c:out value='${id}'/>"
							type="text" value="${phoneNumberValue}"
							class="form-control <c:out value='${validators}'/> col-sm-3 ph-one-numbers phn_tag digits"
							name="<c:out value='${completePath}' />phoneNumber"
							placeholder="${placeHolderNumber}"
							tabindex="<c:out value='${tabindex}'/>" maxlength="8"
							style="pointer-events: none"
							readonly="<c:out value='${readOnly}'/>" />
							</c:otherwise>
							</c:choose>
						</span>
						<span> <input id="extension_<c:out value='${id}'/>"
							type="text"
							class="form-control ${validatorsForExtn} col-sm-3 ph-ext phn_tag digits"
							name="<c:out value='${completePath}' />extension"
							value="${extensionValue}" placeholder="${placeHolderExt}"
							tabindex="<c:out value='${tabindex}'/>" maxlength="6"
							style="pointer-events: none"
							readonly="<c:out value='${readOnly}'/>" />
						</span>
						<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />countryCode" value="${countryCodeValue}" />
						<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />isdCode" value="${isdCodeValue}" />
						<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />phoneNumber" value="${phoneNumberValue}" />
						<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />stdCode" value="${stdCodeValue}" />
						<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />extension" value="${extensionValue}" />
					</c:if>
				</c:if>
			</c:if>
			<c:if test="${not empty name}">
				<c:if test="${viewMode eq false}">
					<input type="hidden"
						name="<c:out value='${completePath}' />countryCode"
						value="${countryCodeValue}"
						id="countryCode_<c:out value='${id}'/>" />

					<input type="hidden"
						name="<c:out value='${completePath}' />numberType.id"
						id="isLandlineNumber_<c:out value='${id}'/>" />

					<input id="isdCode_<c:out value='${id}'/>"
						class="form-control col-sm-2 ph-isd-code" type="text"
						value="${isdCodeValue}"
						name="<c:out value='${completePath}' />isdCode" tabindex="-1"
						placeholder="${placeHolderIsd}" maxlength="4" readonly="readonly" />

					<span> <input id="stdCode_<c:out value='${id}'/>"
						class="form-control <c:out value='${validators}'/> col-sm-2 phn_tag ph-std-code digits"
						type="text" name="<c:out value='${completePath}' />stdCode"
						placeholder="${placeHolderStd}" value="${stdCodeValue}"
						tabindex="<c:out value='${tabindex}'/>" maxlength="4" />
					</span>
					<span> <input id="phoneNumber_<c:out value='${id}'/>"
						type="text"
						class="form-control <c:out value='${validators}'/> col-sm-3 ph-one-numbers phn_tag digits"
						name="<c:out value='${completePath}' />phoneNumber"
						value="${phoneNumberValue}" placeholder="${placeHolderNumber}"
						tabindex="<c:out value='${tabindex}'/>" maxlength="8" />
					</span>
					<span> <input id="extension_<c:out value='${id}'/>"
						type="text"
						class="form-control ${validatorsForExtn} col-sm-3 ph-ext phn_tag digits"
						name="<c:out value='${completePath}' />extension"
						value="${extensionValue}" placeholder="${placeHolderExt}"
						tabindex="<c:out value='${tabindex}'/>" maxlength="6" />
					</span>
					<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />countryCode" value="${countryCodeValue}" />
						<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />isdCode" value="${isdCodeValue}" />
						<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />phoneNumber" value="${phoneNumberValue}" />
						<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />stdCode" value="${stdCodeValue}" />
						<c:set target="${selectedValueMap}"
							property="<c:out value='${completePath}' />extension" value="${extensionValue}" />
					<p class="error_div"></p>
				</c:if>
			</c:if>
		</c:if>
	</div>
</div>

<%-- Phone Number Verification Code --%>
<%
	boolean codeExpired = false;
	if (jspContext.getAttribute("currentExpirationTime") != null) {
		DateTime currentExpirationTime = (DateTime) (jspContext.getAttribute("currentExpirationTime"));

		codeExpired = currentExpirationTime.isBeforeNow();
	}
	jspContext.setAttribute("codeExpired", codeExpired);
%>
<%
	//Code has written to handle the condition in which phone tag id contains special symbols like '[' ']'.
	//These spaecial symbol violates the naming convention of javascript methods.

	String idForModification = (String) jspContext.getAttribute("id");
	String modifiedId = null;
	if (idForModification != null) {
		modifiedId = idForModification.replaceAll("[\\[,\\]\\-]", "_");
	}
	jspContext.setAttribute("modifiedId", modifiedId);
%>

<c:if test="${not empty currentId and not currentVerified}">
	<div class="mob_verify_div hide row">

		<input type="hidden" class="phone_num_id"
			id="phone_num_id<c:out value='${id}'/>"
			value="<c:out value='${currentId}' />" />
		<spring:message var="sendMessage" code="label.phone.tag.sendCode"
			text="Send Code"></spring:message>
		<div class="row sendVerCodeButton m-b15">
			<div class="col-sm-12">
				<c:if test="${not empty currentExpirationTime}">
					<div class="ver_code_delivery_status" data-toggle="tooltip"
						data-placement="top"
						title="<c:out value='${currentVerCodeDelStatusMessage}'/>">
						<small><spring:message
								code="label.phone.tag.verCodeDeliveryStatus"
								text="Delivery Status : " /> <c:if
								test="${not empty currentVerCodeDeliveryStatus}">
								<c:out value="${currentVerCodeDeliveryStatus}" />

							</c:if> </small>
						<c:if test="${empty currentVerCodeDeliveryStatus}">
							<small><spring:message
									code="label.phone.tag.pendingDeliveryStatus" text="Pending" /></small>
						</c:if>
					</div>					
				</c:if>
				<c:if test="${not empty currentExpirationTime and not codeExpired}">
					<small><spring:message code="label.phone.tag.codeSent" />
						<neutrino:dateFormat value="${currentExpirationTime}" /></small>
					<spring:message var="sendMessage" code="label.phone.tag.resendCode"
						text="Resend Code"></spring:message>
				</c:if>
				<c:if test="${not empty currentExpirationTime and codeExpired}">
					<small class="text-danger"><spring:message
							code="label.phone.tag.codeExpired" /> <neutrino:dateFormat
							value="${currentExpirationTime}" /></small>
					<spring:message var="sendMessage" code="label.phone.tag.resendCode"
						text="Resend Code"></spring:message>
				</c:if>
				<a class="btn btn-xs btn-success send_ver_code_btn">${sendMessage}</a>
			</div>
		</div>
		<div
			class="row enter_code_div ${not empty currentExpirationTime and not codeExpired ? 'noop' : 'hide'}">
			<div class="col-sm-12">
				<label class="col-sm-3"><small><strong><spring:message
								code="label.phone.tag.enterCode" /></strong></small></label>
				<div class="input-group col-sm-6">
					<input class="form-control verification_code input-mini reset-p"
						id="appendedInputButton" type="text">
					<button class="btn btn-xs btn-success submit_ver_code_btn"
						type="button">
						<i class="glyphicon glyphicon-ok "></i>
						<spring:message code="label.phone.tag.verify" />
					</button>
				</div>
			</div>
		</div>
	</div>
</c:if>
<%-- </div> --%>
<c:if test="${not empty helpKey}">
	<span class="help-block"><spring:message code="${helpKey}" /></span>
</c:if>
<c:if test="${not empty errorPath}">
	<p class="text-danger">
		<form:errors path="${errorPath}" />
	</p>
</c:if>
<c:if test="${not empty messageKey}">
	<p class="text-info">
		<spring:message code="${messageKey}" />
	</p>
</c:if>
<%-- script at the bottom --%>
<c:set var="phoneNumber_label" scope="page">
	<spring:message code="label.phoneNumber.phoneTag"></spring:message>
</c:set>
<c:set var="numberValid" scope="page">
	<spring:message code="label.number.valid"></spring:message>
</c:set>

<c:set var="country_label" scope="page">
	<spring:message code="label.country.phoneTag"></spring:message>
</c:set>
<script>
var phoneTagInput_<c:out value='${id}'/>  = (function(){
	var phoneTagScriptInput = {};
	phoneTagScriptInput = {

		idVal_phoneTag          :      "<c:out value='${id}'/>",
		isdCode_Id_phoneTag     :      "#isdCode_<c:out value='${id}'/>",
		id_isdCode_phoneTag     :      "#<c:out value='${id}'/>_isdCode",
		id_phoneTag             :      "#<c:out value='${id}'/>",
		phoneNumberId_phoneTag    :    "#<c:out value='${id}'/>_phoneNumber",
		idSelect_phoneTag       :      "#<c:out value='${id}'/>select",
		isLandlineNumberId_phoneTag :  "#isLandlineNumber_<c:out value='${id}'/>",
		isMobileNumberId_phoneTag  :   "#isMobileNumber_<c:out value='${id}'/>",
		idcountryCode_phoneTag :       "#<c:out value='${id}'/>_countryCode",
		idOptionSelected_phoneTag :    "#<c:out value='${id}'/>select option:selected",
		countryCodeId_phoneTag :       "#countryCode_<c:out value='${id}'/>",
		idSelectTag_phoneTag :         "#<c:out value='${id}'/>_select",
		applicationScope_phoneTag : "<c:out value='${applicationScope.commonConfigUtility.defaultCountryISOCode}'/>",
		idOptionSelectedWithUnderScore_phoneTag : "#<c:out value='${id}'/>_select option:selected",
		mobile_phoneTag :                  "${mobile}",
		modifieddynamic_phoneTag :	"${modifiedId}",
		populateDefaultValuesForPhone_phoneTag : "populateDefaultValuesForPhone_${modifiedId}",
		fPopulateDefaultValuesForPhone_phoneTag: {},
		flagForPhoneOutsideAddressTag_phoneTag : "${applicationScope.commonConfigUtility.flagForPhoneOutsideAddressTag}",
		countryCodeFromCountryMaster_phoneTag : ${applicationScope.commonConfigUtility.countryCodeFromCountryMaster},
		countryCodeAlpha2Alpha3Map_phoneTag : ${applicationScope.commonConfigUtility.countryCodeAlpha2Alpha3Map},
		phoneTagInitializerData_phoneTag : ${applicationScope.commonConfigUtility.phoneTagInitializerData},
		phoneTagData_phoneTag : ${applicationScope.commonConfigUtility.phoneTagData},
		populateISOFromCountry_phoneTag : "${populateISOFromCountry}",
		phoneNumber_label_phoneTag : "${phoneNumber_label}",
		numberValid_phoneTag : "${numberValid}",
		country_phoneTag : '${country_label}',
		isValid_phoneTag : "#<c:out value='${id}'/>_isValid"
		}
	phoneTagScript(phoneTagScriptInput);
	return phoneTagScriptInput;
})();
var forcePopulateValuesForPhone = forcePopulateValuesForPhone.bind(phoneTagInput_<c:out value='${id}'/>);
</script>
<%
	HashMap<String, String> selectedItemMap = (HashMap<String, String>)jspContext.getAttribute("selectedValueMap");
	
	try {
		
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && selectedItemMap!=null && !selectedItemMap.isEmpty()) {
			
			TagProtectionUtil.addProtectedFieldToRequest(request, selectedItemMap);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>