<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@taglib uri="http://www.springframework.org/security/tags" prefix="security"%>	
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@tag import="com.nucleus.core.formsConfiguration.FormComponentType"%>


<%@ attribute name="id" required="true"%>
<%@ attribute name="path" required="true"%>
<%@ attribute name="value"%>
<%@ attribute name="fieldType" required="true"%>
<%@ attribute name="fieldDataType" required="true"%>
<%@ attribute name="binderName"%>
<%@ attribute name="itemLable"%>
<%@ attribute name="itemValue"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="mandatoryField" required="true"%>
<%@ attribute name="expandableField"%>
<%@ attribute name="includeSelect" required="true" %>
<%@ attribute name="dynamicFormToolTip"%>
<%@ attribute name="entityName"%>
<%@ attribute name="customeItemList" type="java.util.List"%>
<%@ attribute name="customeMessage"%>
<%@ attribute name="maxFieldLength"%>
<%@ attribute name="minFieldLength"%>
<%@ attribute name="minFieldValue"%>
<%@ attribute name="maxFieldValue"%>
<%@ attribute name="formKey" required="true"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="searchableColumns"%>
<%@ attribute name="mobile"%>
<%@ attribute name="defDate"%>
<%@ attribute name="panelType"%>
<%@ attribute name="href" %>
<%@ attribute name="functionLogic" %>
<%@ attribute name="authority"%>

<c:if test="${not empty functionLogic}">
<script>
	function ${id}_${formKey}(viewMode){
		${functionLogic}
	}
</script>
</c:if>


<c:if
	test="${( not empty fieldDataType ) and (fieldDataType eq 2 or fieldDataType eq 3)}">
	<c:set var="validators" scope="page">
			${validators} digits
		</c:set>
</c:if>

<c:if test="${mandatoryField eq true}">
	<c:set var="requiredCssClass" scope="page">
			required
		</c:set>
</c:if>

<c:set var="radioFieldType" scope="page"
	value="<%=FormComponentType.RADIO%>" />
<c:set var="checkFieldType" scope="page"
	value="<%=FormComponentType.CHECKBOX%>" />
<c:set var="dropFieldType" scope="page"
	value="<%=FormComponentType.DROP_DOWN%>" />
<c:set var="dateFieldType" scope="page"
	value="<%=FormComponentType.DATE%>" />
<c:set var="moneyFieldType" scope="page"
	value="<%=FormComponentType.MONEY%>" />
<c:set var="textAreaFieldType" scope="page"
	value="<%=FormComponentType.TEXT_AREA%>" />
<c:set var="multiSelectBoxFieldType" scope="page"
	value="<%=FormComponentType.MULTISELECTBOX%>" />
<c:set var="autoCompleteFieldType" scope="page"
	value="<%=FormComponentType.AUTOCOMPLETE%>" />
<c:set var="phoneFieldType" scope="page"
	value="<%=FormComponentType.PHONE%>" />
<c:set var="emailFieldType" scope="page"
	value="<%=FormComponentType.EMAIL%>" />
<c:set var="buttonFieldType" scope="page"
	value="<%=FormComponentType.BUTTON%>" />
<c:set var="hyperlinkFieldType" scope="page"
	value="<%=FormComponentType.HYPERLINK%>" />	

<div class="row">
	<c:choose>
		<c:when test="${fieldType eq  dropFieldType}">
			<c:choose>

				<c:when test="${(not empty itemLable) and (not empty itemValue)}">

					<label> <strong><spring:message code="${labelKey}"></spring:message></strong>
						<c:if test="${mandatoryField eq true}">
							<span class="Mandatory color-red">*</span>
						</c:if>
						<div>
							<form:select id="${id}_${formKey}" path="${path}"
								data-original-title="${dynamicFormToolTip}"
								disabled="${viewMode}" cssClass="form-control ${requiredCssClass}" >
								<c:forEach items="${neutrino:binder(binderName)}" var="item">
									<form:option value="${item[itemValue]}"
										label="${item[itemLable]}"></form:option>
								</c:forEach>
							</form:select>
						</div>
				</c:when>
				<c:otherwise>
					<label> <c:choose>
							<c:when test="${(not empty customeMessage)}">
								<strong><c:out value='${customeMessage}' /></strong>
							</c:when>
							<c:otherwise>
								<strong><spring:message code="${labelKey}"></spring:message></strong>
							</c:otherwise>
						</c:choose> <c:if test="${mandatoryField eq true}">
							<span class="Mandatory color-red">*</span>
						</c:if>
					</label>

				<form:select id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
						cssClass="form-control col-sm-6 chosen_a" 
						data-original-title="${dynamicFormToolTip}" disabled="${viewMode}">
						<c:if test="${includeSelect eq true}">
						<form:option value="">
							<spring:message code="label.select" />
						</form:option>
						</c:if>
						<form:options items="${customeItemList}"
							itemLabel="customeItemLabel" itemValue="customeItemValue" />
					</form:select>
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:when test="${fieldType eq  multiSelectBoxFieldType}">
			<c:choose>
				<c:when test="${(not empty itemLable) and (not empty itemValue)}">
					<label> <c:choose>
							<c:when test="${(not empty customeMessage)}">
								<strong><c:out value='${customeMessage}' /></strong>
							</c:when>
							<c:otherwise>
								<strong><spring:message code="${labelKey}"></spring:message></strong>
							</c:otherwise>
						</c:choose> <c:if test="${mandatoryField eq true and panelType ne 3}">
							<span class="Mandatory color-red">*</span>
						</c:if>
					</label>

					<form:select id="${id}_${formKey}" path="${path}"
						multiple="multiple" data-original-title="${dynamicFormToolTip}"
						disabled="${viewMode}" cssClass="form-control ${requiredCssClass}" >
						<c:forEach items="${neutrino:binder(binderName)}" var="item">
							<form:option value="${item[itemValue]}"
								label="${item[itemLable]}"></form:option>
						</c:forEach>
					</form:select>
				</c:when>
				<c:otherwise>
					<label> <c:choose>
							<c:when test="${(not empty customeMessage)}">
								<strong><c:out value='${customeMessage}' /></strong>
							</c:when>
							<c:otherwise>
								<strong><spring:message code="${labelKey}"></spring:message></strong>
							</c:otherwise>
						</c:choose> <c:if test="${mandatoryField eq true and panelType ne 3}">
							<span class="Mandatory color-red">*</span>
						</c:if>
					</label>

					<form:select id="${id}_${formKey}" path="${path}"
						multiple="multiple" data-original-title="${dynamicFormToolTip}"
						disabled="${viewMode}" cssClass="form-control ${requiredCssClass}" >
						<form:options items="${customeItemList}" 
							itemLabel="customeItemLabel" itemValue="customeItemValue" />
					</form:select>
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${fieldType eq  radioFieldType}">

					<c:choose>
						<c:when test="${(not empty itemLable) and (not empty itemValue)}">
							<label class="radio"> 
							

							<c:if test="${mandatoryField eq true}">
								<span class="required"> <c:forEach
										items="${neutrino:binder(binderName)}" var="item">
										<form:radiobutton id="${id}_${formKey}" path="${path}"
											label="${item[itemLable]}" value="${item[itemValue]}"
											disabled="${viewMode}"  /> <strong><spring:message
										code="${labelKey}"></spring:message></strong>
									<span class="Mandatory color-red">*</span>
								
									</c:forEach>
								</span>
							</c:if>
							</label>
							<label class="radio"> 
							<c:if test="${mandatoryField eq false}">
								<c:forEach items="${neutrino:binder(binderName)}" var="item">
									<form:radiobutton id="${id}_${formKey}" path="${path}"
										label="${item[itemLable]}" value="${item[itemValue]}"
										disabled="${viewMode}"   /> <strong><spring:message
										code="${labelKey}"></spring:message></strong>
								</c:forEach>
							</c:if>
							</label>
						</c:when>
						<c:otherwise>
						
							<label class="radio uni_style">  
							<c:choose>

									<c:when test="${(not empty customeMessage)}">
										<strong><c:out value='${customeMessage}' /></strong>
									</c:when>

									<c:otherwise>
										<strong><spring:message code="${labelKey}"></spring:message></strong>
									</c:otherwise>

								</c:choose>
								<c:if test="${mandatoryField eq true}">
									<span class="Mandatory color-red">*</span>
								</c:if>
								<br>
					</label>
							<form:radiobuttons id="${id}_${formKey}" path="${path}"
								cssClass="uni_style ${requiredCssClass}"
								data-original-title="${dynamicFormToolTip}"
								items="${customeItemList}" itemLabel="customeItemLabel"
								itemValue="customeItemValue" disabled="${viewMode}"  />
							

						</c:otherwise>
					</c:choose>
				</c:when>
				<c:when test="${fieldType eq dateFieldType}">
					<label><strong><spring:message code="${labelKey}"></spring:message>
					</strong> <c:if test="${mandatoryField eq true}">
							<span class="Mandatory color-red">*</span></label>
					<span class="reuired"> <form:input path="${path}"
							cssClass="form-control inputmask date col-sm-10 ${validators} ${requiredCssClass}"
							id="${id}_${formKey}" data-original-title="${dynamicFormToolTip}"
							maxlength="${maxFieldValue}" minlength="${minFieldValue}"
							disabled="${viewMode}" ></form:input>
					</span>
					</c:if>
					<c:if test="${mandatoryField eq false}">
						<form:input path="${path}"
							cssClass="form-control inputmask date col-sm-10 ${validators} ${requiredCssClass}"
							id="${id}_${formKey}" data-original-title="${dynamicFormToolTip}"
							maxlength="${maxFieldValue}" minlength="${minFieldValue}"
							disabled="${viewMode}" ></form:input>
					</c:if>
				</c:when>
				<c:when test="${fieldType eq textAreaFieldType}">
				<c:if test="${mandatoryField eq true}">
					<label> <strong><spring:message code="${labelKey}"></spring:message></strong>
						
							<span class="Mandatory color-red">*</span></label>
							<br>
							<span class="required">
							  <c:choose>
                                      <c:when test="${expandableField eq true}">
							<form:textarea path="${path}"
									id="${id}_${formKey}" disabled="${disabled}"
									data-original-title="${dynamicFormToolTip}"
									maxlength="${maxLength}"
									cssClass="form-control ${validators} neutrino_textarea textarea_resize" ></form:textarea>
									 </c:when>
                                        <c:otherwise>
                                        <form:textarea path="${path}"
                                        			id="${id}_${formKey}" disabled="${disabled}"
                                        			data-original-title="${dynamicFormToolTip}"
                                        			maxlength="${maxLength}"
                                        			cssClass="form-control ${validators} neutrino_textarea" ></form:textarea>
                                        </c:otherwise>
                                       </c:choose>
							</span>
						</c:if> <c:if test="${mandatoryField eq false}">
						<label> <strong><spring:message code="${labelKey}"></spring:message></strong></label><br>
							  <c:choose>
                                      <c:when test="${expandableField eq true}">
							<form:textarea path="${path}" id="${id}_${formKey}"
								disabled="${disabled}"
								data-original-title="${dynamicFormToolTip}"
								maxlength="${maxLength}"
								cssClass="form-control ${validators} neutrino_textarea textarea_resize" ></form:textarea>
								 </c:when>
                                      <c:otherwise>
                                      	<form:textarea path="${path}" id="${id}_${formKey}"
                                      								disabled="${disabled}"
                                      								data-original-title="${dynamicFormToolTip}"
                                      								maxlength="${maxLength}"
                                      								cssClass="form-control ${validators} neutrino_textarea " ></form:textarea>
                                 </c:otherwise>
                                                                       </c:choose>
						</c:if>
				</c:when>
				
				<c:when test="${fieldType eq hyperlinkFieldType}">
					<c:if test="${viewMode eq true}">
						<c:if test="${not empty authority}">
							<security:authorize access="hasAnyAuthority('${authority}')">
								<div class="container" style="padding: 25px">
									<a id="${id}_${formKey}_${clonedRowStatus}"
										href="javascript:void(null)" style="cursor: default" target="_blank"><spring:message
											code="${labelKey}"></spring:message></a>

								</div>
							</security:authorize>
						</c:if>
						<c:if test="${empty authority}">
							<div class="container" style="padding: 25px">
								<a id="${id}_${formKey}_${clonedRowStatus}"
									href="javascript:void(null)" style="cursor: default" target="_blank"><spring:message
										code="${labelKey}"></spring:message></a>

							</div>
						</c:if>
					</c:if>
					<c:if test="${viewMode eq false}">
						<c:if test="${not empty authority}">
							<c:if test="${not empty href}">
								<c:if test="${ not empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="<c:out value='${href}' />"
												onclick="${id}_${formKey}${'()'}" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
								<c:if test="${empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="<c:out value='${href}' />" target="_blank"><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
							</c:if>
							<c:if test="${empty href}">
								<c:if test="${ not empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">

											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="javascript:void(null)"
												onclick="${id}_${formKey}${'()'}" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
								<c:if test="${empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">

											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="javascript:void(null)" style="cursor: default" target="_blank"><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
							</c:if>
						</c:if>
						<c:if test="${empty authority}">
							<c:if test="${not empty href}">
								<c:if test="${ not empty functionLogic}">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="<c:out value='${href}' />"
												onclick="${id}_${formKey}${'()'}" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
								</c:if>
								<c:if test="${empty functionLogic}">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="<c:out value='${href}' />" target="_blank"><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
								</c:if>
							</c:if>
							<c:if test="${empty href}">
								<c:if test="${ not empty functionLogic}">
										<div class="container" style="padding: 25px">

											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="javascript:void(null)"
												onclick="${id}_${formKey}${'()'}" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
								</c:if>
								<c:if test="${empty functionLogic}">
										<div class="container" style="padding: 25px">

											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="javascript:void(null)" style="cursor: default" target="_blank"><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
								</c:if>
							</c:if>
						</c:if>
					</c:if>
				</c:when>

				<c:when test="${fieldType eq buttonFieldType}">

						<c:if test="${not empty authority}">
							<c:if test="${empty href}">
								<c:if test="${ not empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="javascript:void(null)"
												onclick="${id}_${formKey}('${viewMode}')" class="btn btn-inverse" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
								<c:if test="${empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="javascript:void(null)"
												class="btn btn-inverse" target="_blank"><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
							</c:if>
							<c:if test="${not empty href}">
								<c:if test="${ not empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="<c:out value='${href}' />"
												onclick="${id}_${formKey}('${viewMode}')" class="btn btn-inverse" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
								<c:if test="${empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="<c:out value='${href}' />" class="btn btn-inverse" target="_blank"><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
							</c:if>
						</c:if>

						<c:if test="${empty authority}">
							<c:if test="${empty href}">
								<c:if test="${ not empty functionLogic}">
									<div class="container" style="padding: 25px">
										<a id="${id}_${formKey}_${clonedRowStatus}"
											href="javascript:void(null)"
											onclick="${id}_${formKey}('${viewMode}')" class="btn btn-inverse" ><spring:message
												code="${labelKey}"></spring:message></a>
									</div>
								</c:if>
								<c:if test="${empty functionLogic}">
									<div class="container" style="padding: 25px">
										<a id="${id}_${formKey}_${clonedRowStatus}"
											href="javascript:void(null)"
											class="btn btn-inverse" target="_blank"><spring:message
												code="${labelKey}"></spring:message></a>
									</div>
								</c:if>
							</c:if>
							<c:if test="${not empty href}">
								<c:if test="${ not empty functionLogic}">
									<div class="container" style="padding: 25px">
										<a id="${id}_${formKey}_${clonedRowStatus}"
											href="<c:out value='${href}' />"
											onclick="${id}_${formKey}('${viewMode}')" class="btn btn-inverse" ><spring:message
												code="${labelKey}"></spring:message></a>
									</div>
								</c:if>
								<c:if test="${empty functionLogic}">
									<div class="container" style="padding: 25px">
										<a id="${id}_${formKey}_${clonedRowStatus}"
											href="<c:out value='${href}' />" class="btn btn-inverse" target="_blank"><spring:message
												code="${labelKey}"></spring:message></a>
									</div>
								</c:if>
							</c:if>
						</c:if>
				</c:when>

				<c:when test="${fieldType eq checkFieldType}">
					<c:if test="${mandatoryField eq true}">
						<span class="Mandatory color-red">*</span>
						<span class="required"> <form:checkbox
								id="${id}_${formKey}" path="${path}" value="true"
								label="${labelKey}" viewMode="${viewMode}" ></form:checkbox>
						</span>

					</c:if>

					<c:if test="${mandatoryField eq false}">
						<form:checkbox id="${id}_${formKey}" path="${path}" value="true"
							label="${labelKey}" viewMode="${viewMode}" ></form:checkbox>
					</c:if>
				</c:when>
				<c:otherwise>
					<label><strong><spring:message code="${labelKey}"></spring:message>
					</strong> <c:if test="${mandatoryField eq true}">
							<span class="Mandatory color-red">*</span>
						</c:if> </label>

					<div id="<c:out value='${id}' />_<c:out value='${formKey}' />-control-group"
						class="form-group input-group input-group <c:out value='${validators}' /> ${requiredCssClass}">
						<form:input path="${path}"
							cssClass="form-control inputmask col-sm-10 ${validators} ${requiredCssClass}"
							id="${id}_${formKey}" data-original-title="${dynamicFormToolTip}"
							maxlength="${maxFieldLength}" minlength="${minFieldLength}"
							disabled="${viewMode}" ></form:input>
					</div>

				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
</div>

<c:choose>
	<c:when test="${fieldType eq  phoneFieldType}">
		<c:set var="absolutePath"
			value="${fn:substringBefore(path, '.phoneNumberVO')}" scope="page"></c:set>

	</c:when>
	<c:when test="${fieldType eq  emailFieldType}">
		<c:set var="absolutePath"
			value="${fn:substringBefore(path, '.emailInfoVO')}" scope="page"></c:set>
	</c:when>
	<c:otherwise>
		<c:set var="absolutePath"
			value="${fn:substringBefore(path, '.value')}" scope="page"></c:set>
	</c:otherwise>
</c:choose>

<input type="hidden" name="<c:out value='${absolutePath}' />.id" value="<c:out value='${id}' />" />
<input type="hidden" name="<c:out value='${absolutePath}' />.fieldDataType"
	value="<c:out value='${fieldDataType}' />" />
<input type="hidden" name="<c:out value='${absolutePath}' />.entityName"
	value="<c:out value='${entityName}' />" />
<input type="hidden" name="<c:out value='${absolutePath}' />.fieldType"
	value="<c:out value='${fieldType}' />" />