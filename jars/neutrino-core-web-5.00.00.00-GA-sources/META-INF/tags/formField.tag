<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="security"%>	
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
<%@ attribute name="item" type="java.lang.Object"%>
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
<%@ attribute name="clonedRowStatus"%>
<%@ attribute name="parentFieldKey"%>
<%@ attribute name="urlCascadeSelect"%>
<%@ attribute name="href"%>
<%@ attribute name="functionLogic"%>
<%@ attribute name="authority"%>
<%@ attribute name="parentColumn"%>
<%@ attribute name="errorMessageCode"%>
<%@ attribute name="parentFieldId"%>
<%@ attribute name="mainFormDependant"%>
<%@ attribute name="specialTableColumn"%>
<%@ attribute name="parentKey"%>
<%@ attribute name="disableKey"%>
<%@ attribute name="panelColumnLayout"%>
<%@ attribute name="lovKey"%>


<c:if test="${not empty functionLogic}">
	<script>
	function fn${id}_${formKey}(viewMode){
		${functionLogic}
	}
</script>
</c:if>


<c:if
	test="${( not empty fieldDataType ) and (fieldDataType eq 3)}">
	<c:set var="validators" scope="page">
			${validators} floatingDigits
	</c:set>
</c:if>

<c:if
	test="${( not empty fieldDataType ) and (fieldDataType eq 2)}">
	<c:set var="validators" scope="page">
			${validators} Integer
	</c:set>
</c:if>

<c:if test="${mandatoryField eq true}">
	<c:set var="requiredCssClass" scope="page">
			required
		</c:set>
</c:if>

<c:if test="${disableKey eq true}">
	<c:set var="viewMode" scope="page" value="true"/>
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
<c:set var="textBoxFieldType" scope="page"
	value="<%=FormComponentType.TEXT_BOX%>" />
<c:set var="cascadeSelectFieldType" scope="page"
	value="<%=FormComponentType.CASCADED_SELECT%>" />
<c:set var="customCascadeSelectFieldType" scope="page"
	value="<%=FormComponentType.CUSTOM_CASCADED_SELECT%>" />
<c:set var="buttonFieldType" scope="page"
	value="<%=FormComponentType.BUTTON%>" />
<c:set var="lovFieldType" scope="page"
	value="<%=FormComponentType.LOV%>" />
<c:set var="hyperlinkFieldType" scope="page"
	value="<%=FormComponentType.HYPERLINK%>" />
<c:set var="currentTimeStampComponentType" scope="request"
    value="<%=FormComponentType.CURRENT_TIME_STAMP%>" />

<c:choose>
	<c:when test="${not empty panelColumnLayout && panelColumnLayout eq 4}">
		<c:set var="colSpanByLayout" value="12" scope="page"/>
		<c:set var="multiSelctColSpan" value="10" scope="page"/>
		<c:set var="inputSelectColSpan" value="10" scope="page"/>
		<c:set var="textAreaColSpan" value="10" scope="page"/>
		<c:set var="autoCompleteColSpan" value="10" scope="page"/>
		<c:set var="phoneColSpan" value="10" scope="page"/>	
		<c:set var="lovColSpan" value="10" scope="page"/>
	</c:when>
	<c:otherwise>
		<c:set var="colSpanByLayout" value="6" scope="page"/>
		<c:set var="multiSelctColSpan" value="12" scope="page"/>
		<c:set var="inputSelectColSpan" value="6" scope="page"/>
		<c:set var="textAreaColSpan" value="6" scope="page"/>
		<c:set var="autoCompleteColSpan" value="6" scope="page"/>
		<c:set var="phoneColSpan" value="12" scope="page"/>
		<c:set var="lovColSpan" value="6" scope="page"/>
	</c:otherwise>
</c:choose>

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

<div class="row columnDiv">
	<c:choose>
		<c:when test="${fieldType eq  dropFieldType}">
			<c:choose>

				<c:when test="${(not empty itemLable) and (not empty itemValue)}">
					<c:if test="${not empty parentKey}">
						<c:if test="${mandatoryField eq true}">
							<neutrino:cascadeSelect id="${id}_${formKey}_${clonedRowStatus}"
								tooltipKey="${dynamicFormToolTip}" path="${path}"
								mandatory="true" labelKey="${labelKey}" viewMode="${viewMode}"
								colSpan="${colSpanByLayout}" selectBoxColSpan="${colSpanByLayout}" items="${neutrino:binder(binderName)}"
								itemValue="${itemValue}" itemLabel="${itemLable}" parentId="${parentKey}"/>
						</c:if>
						<c:if test="${mandatoryField eq false}">
							<neutrino:cascadeSelect id="${id}_${formKey}_${clonedRowStatus}"
								tooltipKey="${dynamicFormToolTip}" path="${path}"
								labelKey="${labelKey}" viewMode="${viewMode}"
								colSpan="${colSpanByLayout}" selectBoxColSpan="${colSpanByLayout}" items="${neutrino:binder(binderName)}"
								itemValue="${itemValue}" itemLabel="${itemLable}" parentId="${parentKey}"/>
						</c:if>
					</c:if>
					<c:if test="${empty parentKey}">
						<c:if test="${mandatoryField eq true}">
							<div>
								<neutrino:select path="${path}" itemLabel="${itemLable}"
									id="${id}_${formKey}_${clonedRowStatus}" itemValue="${itemValue}"
									items="${neutrino:binder(binderName)}" selectBoxColSpan="${colSpanByLayout}"
									labelKey="${labelKey}" mandatory="true"
									dynamicFormToolTip="${dynamicFormToolTip}"
									viewMode="${viewMode}" ></neutrino:select>
							</div>
						</c:if>
	
						<c:if test="${mandatoryField eq false}">
							<div>
								<neutrino:select path="${path}" itemLabel="${itemLable}"
									id="${id}_${formKey}_${clonedRowStatus}" itemValue="${itemValue}"
									items="${neutrino:binder(binderName)}" selectBoxColSpan="${colSpanByLayout}"
									labelKey="${labelKey}"
									dynamicFormToolTip="${dynamicFormToolTip}"
									viewMode="${viewMode}"  ></neutrino:select>
							</div>
						</c:if>
					</c:if>
				</c:when>

				<c:otherwise>
				    <c:if test="${mandatoryField eq true}">
					  <div>
					</c:if>
				    <c:if test="${mandatoryField eq false}">
					  <div class="nonMandatory">
					</c:if>
					<label>
					 	<c:choose>
							<c:when test="${(not empty customeMessage)}">
								<strong><c:out value='${customeMessage}' /></strong>
							</c:when>
							<c:otherwise>
								<strong><spring:message code="${labelKey}"></spring:message></strong>
							</c:otherwise>
						</c:choose>
						<c:if
							test="${mandatoryField eq true}">
							<span class="Mandatory color-red">*</span>
						</c:if> </label>
						<div class="form-group fancy-select">
						<c:if test="${mandatoryField eq true}">
							<form:select id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
								cssClass="form-control col-sm-6 chosen_a ${requiredCssClass}"
								data-original-title="${dynamicFormToolTip}" disabled="${viewMode}">
								<c:if test="${includeSelect eq true}">
									<form:option value="">
										<spring:message code="label.select" />
									</form:option>
								</c:if>
								<form:options items="${customeItemList}"
									itemLabel="customeItemLabel" itemValue="customeItemValue" />
								</form:select>
						</c:if>

						<c:if test="${mandatoryField eq false}">
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
						</c:if>
						</div>
						</div>
				</c:otherwise>

			</c:choose>
		</c:when>
		<c:when test="${fieldType eq  autoCompleteFieldType}">

			<c:if test="${mandatoryField eq true}">
				<div>
					<c:if test="${mainFormDependant eq false}">
						<c:set var="parentFieldId" value="${parentFieldId}_${formKey}_${clonedRowStatus}"></c:set>
					</c:if>
					<neutrino:autocomplete className="${entityName}" path="${path}"
						mandatory="${mandatoryField}" viewMode="${viewMode}" tooltipKey="${dynamicFormToolTip}"
						value="${value}" labelKey="${labelKey}" strictMode="true"
						showValueOnEditOrViewMode="true" loadApprovedEntity="true"
						searchColList="${searchableColumns}" colSpan="${autoCompleteColSpan}" inputBoxColSpan="12" itemLabel="${itemLable}"
						id="${id}_${formKey}_${clonedRowStatus}" itemValue="${itemValue}" parentCol="${parentColumn}" 
						parentId="${parentFieldId}" emptyParentError="${errorMessageCode}" item="${item}"/>
				</div>
			</c:if>

			<c:if test="${mandatoryField eq false}">
				<div>
					<c:if test="${mainFormDependant eq false}">
						<c:set var="parentFieldId" value="${parentFieldId}_${formKey}_${clonedRowStatus}"></c:set>
					</c:if>
					<neutrino:autocomplete className="${entityName}" path="${path}" tooltipKey="${dynamicFormToolTip}"
						viewMode="${viewMode}" value="${value}" labelKey="${labelKey}" strictMode="true"
						showValueOnEditOrViewMode="true" colSpan="${autoCompleteColSpan}" inputBoxColSpan="12" loadApprovedEntity="true"
						searchColList="${searchableColumns}" itemLabel="${itemLable}"
						id="${id}_${formKey}_${clonedRowStatus}" itemValue="${itemValue}" parentCol="${parentColumn}" 
						parentId="${parentFieldId}" emptyParentError="${errorMessageCode}" item="${item}"/>
				</div>
			</c:if>

		</c:when>

		<c:when test="${fieldType eq  phoneFieldType}">

			<c:if test="${mandatoryField eq true}">
				<div>
					<neutrino:phone id="${id}_${formKey}_${clonedRowStatus}" labelKey="${labelKey}"
						mandatory="${mandatoryField}" viewMode="${viewMode}"
						path="${path}" phoneBoxColSpan="${phoneColSpan}" colSpan="${phoneColSpan}" mobile="${mobile}"  />
				</div>
			</c:if>

			<c:if test="${mandatoryField eq false}">
				<div>
					<neutrino:phone id="${id}_${formKey}_${clonedRowStatus}" labelKey="${labelKey}"
						viewMode="${viewMode}" path="${path}" phoneBoxColSpan="${phoneColSpan}"
						colSpan="${phoneColSpan}" mobile="${mobile}"  />
				</div>
			</c:if>
		</c:when>

		<c:when test="${fieldType eq emailFieldType }">

			<c:if test="${mandatoryField eq true}">
				<div>
					<neutrino:email mandatory="${mandatoryField}" path="${path}"
						id="${id}_${formKey}_${clonedRowStatus}" labelKey="${labelKey}" validationRequired="true"
						viewMode="${viewMode}" colSpan="${colSpanByLayout}" emailBoxColSpan="12"
						placeHolderKey="${labelKey}" verifyField="${path}.verified"  />
				</div>
			</c:if>

			<c:if test="${mandatoryField eq false}">
				<div>
					<neutrino:email path="${path}" id="${id}_${formKey}_${clonedRowStatus}" validationRequired="true"
						labelKey="${labelKey}" viewMode="${viewMode}" colSpan="${colSpanByLayout}"
						emailBoxColSpan="12" placeHolderKey="${labelKey}" verifyField="${path}.verified" />
				</div>
			</c:if>

		</c:when>

		<c:when test="${fieldType eq  multiSelectBoxFieldType}">
		    <c:if test="${mandatoryField eq true}">
		       <div class="form-group">
	    	</c:if>
		   <c:if test="${mandatoryField eq false}">
			   <div class="form-group nonMandatory">
		   </c:if>
			<c:choose>
				<c:when test="${(not empty itemLable) and (not empty itemValue)}">

					<label>
						<c:choose>
							<c:when test="${(not empty customeMessage)}">
								<strong><c:out value='${customeMessage}' /></strong>
							</c:when>
							<c:otherwise>
								<strong><spring:message code="${labelKey}"></spring:message></strong>
							</c:otherwise>
						</c:choose>
						<c:if
							test="${mandatoryField eq true and panelType ne 3}">
							<span class="Mandatory color-red">*</span>
						</c:if> </label>

							<neutrino:multiselect id="${id}_${formKey}_${clonedRowStatus}" path="${path}" 
												colSpan="${multiSelctColSpan}" tooltipKey="${dynamicFormToolTip}" 
												items="${neutrino:binder(binderName)}"
												itemValue="${itemValue}" itemLabel="${itemLable}" enableJSOnUpdationOfList="true" viewMode="${viewMode}"
												multiSelectBoxSpanClass="col-sm-${colSpanByLayout}"/>

							<%-- <form:select id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
								multiple="multiple" data-original-title="${dynamicFormToolTip}"
								disabled="${viewMode}" cssClass="form-control ${requiredCssClass}" >
								<c:forEach items="${neutrino:binder(binderName)}" var="item">
									<form:option value="${item[itemValue]}"
										label="${item[itemLable]}"></form:option>
								</c:forEach>
							</form:select> --%>

				</c:when>

				<c:otherwise>
					<label>
						<c:choose>
							<c:when test="${(not empty customeMessage)}">
								<strong><c:out value='${customeMessage}' /></strong>
							</c:when>
							<c:otherwise>
								<strong><spring:message code="${labelKey}"></spring:message></strong>
							</c:otherwise>
						</c:choose>
						<c:if
							test="${mandatoryField eq true and panelType ne 3}">
							<span class="Mandatory color-red">*</span>
						</c:if> </label>

							<form:select id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
								multiple="multiple" data-original-title="${dynamicFormToolTip}"
								disabled="${viewMode}" cssClass="form-control ${requiredCssClass} chosen_a" >
								<form:options items="${customeItemList}"
									itemLabel="customeItemLabel" itemValue="customeItemValue" />
							</form:select>

				</c:otherwise>

			</c:choose>
			</div>
		</c:when>

		<c:when test="${fieldType eq 'CascadedSelect' or fieldType eq 'CustomCascadedSelect'}">
			<c:choose>
				<c:when test="${not empty parentFieldKey}">
					<c:if test="${mandatoryField eq true}">
						<neutrino:cascadeSelect id="${id}_${formKey}_${clonedRowStatus}"
							parentId="${parentFieldKey}_${formKey}_${clonedRowStatus}" url="${pageContext.request.contextPath}/app/FormDefinition/getCascadeDropdownData/${urlCascadeSelect}"
							tooltipKey="${dynamicFormToolTip}" path="${path}"
							mandatory="true" labelKey="${labelKey}" viewMode="${viewMode}"
							colSpan="${colSpanByLayout}" selectBoxColSpan="${colSpanByLayout}" items="${neutrino:binder(binderName)}"
							itemValue="${itemValue}" itemLabel="${itemLable}" />
					</c:if>
					<c:if test="${mandatoryField eq false}">
						<neutrino:cascadeSelect id="${id}_${formKey}_${clonedRowStatus}"
							parentId="${parentFieldKey}_${formKey}_${clonedRowStatus}" url="${pageContext.request.contextPath}/app/FormDefinition/getCascadeDropdownData/${urlCascadeSelect}"
							tooltipKey="${dynamicFormToolTip}" path="${path}"
							labelKey="${labelKey}" viewMode="${viewMode}"
							colSpan="${colSpanByLayout}" selectBoxColSpan="${colSpanByLayout}" items="${neutrino:binder(binderName)}"
							itemValue="${itemValue}" itemLabel="${itemLable}" />
					</c:if>
				</c:when>
				<c:otherwise>
					<c:if test="${mandatoryField eq true}">
						<neutrino:cascadeSelect id="${id}_${formKey}_${clonedRowStatus}"
							tooltipKey="${dynamicFormToolTip}" path="${path}"
							mandatory="true" labelKey="${labelKey}" viewMode="${viewMode}"
							colSpan="${colSpanByLayout}" selectBoxColSpan="${colSpanByLayout}" items="${neutrino:binder(binderName)}"
							itemValue="${itemValue}" itemLabel="${itemLable}" />
					</c:if>
					<c:if test="${mandatoryField eq false}">
						<neutrino:cascadeSelect id="${id}_${formKey}_${clonedRowStatus}"
							tooltipKey="${dynamicFormToolTip}" path="${path}"
							labelKey="${labelKey}" viewMode="${viewMode}"
							colSpan="${colSpanByLayout}" selectBoxColSpan="${colSpanByLayout}" items="${neutrino:binder(binderName)}"
							itemValue="${itemValue}" itemLabel="${itemLable}" />
					</c:if>
				</c:otherwise>
			</c:choose>

		</c:when>

		<c:otherwise>
			<c:choose>

				<c:when test="${fieldType eq  radioFieldType}">
				    <c:if test="${mandatoryField eq true}">
					  <div class="form-group">
					</c:if>
				    <c:if test="${mandatoryField eq false}">
					  <div class="form-group nonMandatory">
					</c:if>
					<c:choose>
						<c:when test="${(not empty itemLable) and (not empty itemValue)}">
							<label> <strong><spring:message
										code="${labelKey}"></spring:message></strong> <c:if
									test="${mandatoryField eq true}">
									<span class="Mandatory color-red">*</span>
								</c:if>
							</label>

							<c:if test="${mandatoryField eq true}">
								<span class="required"> <c:forEach
										items="${neutrino:binder(binderName)}" var="item">
										<neutrino:radio id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
											labelKey="${item[itemLable]}" value="${item[itemValue]}"
											selectedValue="" disabled="${viewMode}"
											tooltipKey="${dynamicFormToolTip}" mandatory="true" />

									</c:forEach>
								</span>
							</c:if>
							<c:if test="${mandatoryField eq false}">
								<c:forEach items="${neutrino:binder(binderName)}" var="item">
									<neutrino:radio id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
										labelKey="${item[itemLable]}" value="${item[itemValue]}"
										selectedValue="" disabled="${viewMode}"
										tooltipKey="${dynamicFormToolTip}"  />
								</c:forEach>
							</c:if>
						</c:when>

						<c:otherwise>
							<label class="radio uni_style"> <c:choose>

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

							<form:radiobuttons id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
								cssClass="uni_style ${requiredCssClass} ${mandatoryField eq true ?'required' :''}" data-original-title="${dynamicFormToolTip}"
								items="${customeItemList}" itemLabel="customeItemLabel"
								itemValue="customeItemValue" disabled="${viewMode}"  />

						</c:otherwise>
					</c:choose>

					</div>

				</c:when>

				<c:when test="${fieldType eq dateFieldType}">
					<c:if test="${mandatoryField eq true}">
						<neutrino:datepicker id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
							editable="true" labelKey="${labelKey}" colSpan="${colSpanByLayout}"
							mandatory="true" dynamicFormToolTip="${dynamicFormToolTip}"
							minFieldValue="${minFieldValue}" maxFieldValue="${maxFieldValue}"
							viewMode="${viewMode}" defDate="${defDate}" />
					</c:if>

					<c:if test="${mandatoryField eq false}">
						<neutrino:datepicker id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
							editable="true" labelKey="${labelKey}" colSpan="${colSpanByLayout}"
							dynamicFormToolTip="${dynamicFormToolTip}"
							minFieldValue="${minFieldValue}" maxFieldValue="${maxFieldValue}"
							viewMode="${viewMode}" defDate="${defDate}"  />
					</c:if>
					<input type="hidden" id="minFieldValueHidden_${id}_${formKey}_${clonedRowStatus}" value="${minFieldValue}"/>
					<input type="hidden" id="maxFieldValueHidden_${id}_${formKey}_${clonedRowStatus}" value="${maxFieldValue}"/>
					<input type="hidden" id="defDateHidden_${id}_${formKey}_${clonedRowStatus}" value="${defDate}"/>
					<input type="hidden" id="dateFormatHidden_${id}_${formKey}_${clonedRowStatus}" value="${neutrino:binder('currentUserDateFormat')}"/>
				</c:when>

				<c:when test="${fieldType eq textAreaFieldType}">
					<c:if test="${mandatoryField eq true}">
                          <c:choose>
                            <c:when test="${expandableField eq true}">
						<neutrino:textarea id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
							colSpan="${textAreaColSpan}" labelKey="${labelKey}" maxLength="${not empty maxFieldLength?maxFieldLength:255}" textareaBoxColSpan="12"
							character="true" mandatory="true" rows="4" resize="vertical"
							dynamicFormToolTip="${dynamicFormToolTip}" viewMode="${viewMode}" >
						</neutrino:textarea>
                         </c:when>
                         <c:otherwise>
                         <neutrino:textarea id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
                         							colSpan="${textAreaColSpan}" labelKey="${labelKey}" maxLength="${not empty maxFieldLength?maxFieldLength:255}" textareaBoxColSpan="12"
                         							character="true" mandatory="true" rows="4" resize="none"
                         							dynamicFormToolTip="${dynamicFormToolTip}" viewMode="${viewMode}" >
                         						</neutrino:textarea>
                          </c:otherwise>
                           </c:choose>
					</c:if>

					<c:if test="${mandatoryField eq false}">
					    <c:choose>
                             <c:when test="${expandableField eq true}">
						<neutrino:textarea id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
							textareaBoxColSpan="12" colSpan="${textAreaColSpan}" labelKey="${labelKey}" maxLength="${not empty maxFieldLength?maxFieldLength:255}"
							character="true" dynamicFormToolTip="${dynamicFormToolTip}" rows="4" resize="vertical"
							viewMode="${viewMode}" >
						</neutrino:textarea>
						 </c:when>
                            <c:otherwise>
                            <neutrino:textarea id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
                            							textareaBoxColSpan="12" colSpan="${textAreaColSpan}" labelKey="${labelKey}" maxLength="${not empty maxFieldLength?maxFieldLength:255}"
                            							character="true" dynamicFormToolTip="${dynamicFormToolTip}" rows="4" resize="none"
                            							viewMode="${viewMode}" >
                            						</neutrino:textarea>
                          </c:otherwise>
                           </c:choose>

					</c:if>
				</c:when>

				<c:when test="${fieldType eq checkFieldType}">
					<c:if test="${mandatoryField eq true}">

						<neutrino:checkBox id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
							value="true" labelKey="${labelKey}" mandatory="true"
							dynamicFormToolTip="${dynamicFormToolTip}" viewMode="${viewMode}" ></neutrino:checkBox>

					</c:if>

					<c:if test="${mandatoryField eq false}">
						<neutrino:checkBox id="${id}_${formKey}_${clonedRowStatus}" path="${path}"
							value="true" labelKey="${labelKey}"
							dynamicFormToolTip="${dynamicFormToolTip}" viewMode="${viewMode}" ></neutrino:checkBox>
					</c:if>
				</c:when>

				<c:when test="${fieldType eq moneyFieldType}">

					<c:if test="${mandatoryField eq true}">
						<neutrino:money id="${id}_${formKey}_${clonedRowStatus}" name="${path}"
							value="${value}" moneyBoxColSpan="7" colSpan="${colSpanByLayout}"
							labelKey="${labelKey}" validators="amount" mandatory="true"
							dynamicFormToolTip="${dynamicFormToolTip}"
							maxLength="${maxFieldLength}" viewMode="${viewMode}" />


					</c:if>

					<c:if test="${mandatoryField eq false}">
						<neutrino:money id="${id}_${formKey}_${clonedRowStatus}" name="${path}"
							value="${value}" moneyBoxColSpan="7" colSpan="${colSpanByLayout}"
							labelKey="${labelKey}" validators="amount"
							dynamicFormToolTip="${dynamicFormToolTip}"
							maxLength="${maxFieldLength}" viewMode="${viewMode}"  />

					</c:if>


				</c:when>

				<c:when test="${fieldType eq textBoxFieldType}">

                  <c:choose>
                    <c:when test="${fn:length(specialTableColumn) > 0}">
                        <neutrino:input path="${path}" colSpan="${inputSelectColSpan}" id="${id}_${formKey}_${clonedRowStatus}"
                            tooltipKey="${dynamicFormToolTip}" maxLength="${maxFieldLength}"
                            minLength="${minFieldLength}" readOnly="true"
                            validators="${validators}" mandatory="true"
                            labelKey="${labelKey}" ></neutrino:input>
                    </c:when>
                    <c:otherwise>
                    <c:if test="${mandatoryField eq true}">
                        <neutrino:input path="${path}" colSpan="${inputSelectColSpan}" id="${id}_${formKey}_${clonedRowStatus}"
                                    tooltipKey="${dynamicFormToolTip}" maxLength="${maxFieldLength}"
                                    minLength="${minFieldLength}" viewMode="${viewMode}"
                                    validators="${validators}" mandatory="true"
                                    labelKey="${labelKey }" ></neutrino:input>

                        </c:if>

                        <c:if test="${mandatoryField eq false}">
                            <neutrino:input path="${path}" colSpan="${inputSelectColSpan}" inputBoxColSpan="12" id="${id}_${formKey}_${clonedRowStatus}"
                                    tooltipKey="${dynamicFormToolTip}" maxLength="${maxFieldLength}"
                                    minLength="${minFieldLength}" viewMode="${viewMode}"
                                    validators="${validators}" labelKey="${labelKey }" ></neutrino:input>

                        </c:if>
                    </c:otherwise>
                  </c:choose>

				</c:when>
				
				<c:when test="${fieldType eq hyperlinkFieldType}">
					<c:if test="${viewMode eq true}">
						<c:if test="${not empty authority}">
							<security:authorize access="hasAnyAuthority('${authority}')">
								<div class="container" style="padding: 25px">
									<a id="${id}_${formKey}_${clonedRowStatus}" rel="tooltip" data-original-title="${dynamicFormToolTip}"
										href="javascript:void(null)" style="cursor: default" target="_blank"><spring:message
											code="${labelKey}"></spring:message></a>

								</div>
							</security:authorize>
						</c:if>
						<c:if test="${empty authority}">
							<div class="container" style="padding: 25px">
								<a id="${id}_${formKey}_${clonedRowStatus}" rel="tooltip" data-original-title="${dynamicFormToolTip}"
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
												href="<c:out value='${href}' />" rel="tooltip" data-original-title="${dynamicFormToolTip}"
												onclick="fn${id}_${formKey}${'()'}" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
								<c:if test="${empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}" rel="tooltip" data-original-title="${dynamicFormToolTip}"
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
												href="javascript:void(null)" rel="tooltip" data-original-title="${dynamicFormToolTip}"
												onclick="fn${id}_${formKey}${'()'}" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
								<c:if test="${empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">

											<a id="${id}_${formKey}_${clonedRowStatus}" rel="tooltip" data-original-title="${dynamicFormToolTip}"
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
												href="<c:out value='${href}' />" rel="tooltip" data-original-title="${dynamicFormToolTip}"
												onclick="fn${id}_${formKey}${'()'}" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
								</c:if>
								<c:if test="${empty functionLogic}">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}" rel="tooltip" data-original-title="${dynamicFormToolTip}"
												href="<c:out value='${href}' />" target="_blank"><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
								</c:if>
							</c:if>
							<c:if test="${empty href}">
								<c:if test="${ not empty functionLogic}">
										<div class="container" style="padding: 25px">

											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="javascript:void(null)" rel="tooltip" data-original-title="${dynamicFormToolTip}"
												onclick="fn${id}_${formKey}${'()'}" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
								</c:if>
								<c:if test="${empty functionLogic}">
										<div class="container" style="padding: 25px">

											<a id="${id}_${formKey}_${clonedRowStatus}" rel="tooltip" data-original-title="${dynamicFormToolTip}"
												href="javascript:void(null)" style="cursor: default" target="_blank"><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
								</c:if>
							</c:if>
						</c:if>
					</c:if>
				</c:when>
				<c:when test="${fieldType eq currentTimeStampComponentType}">
                     <c:if test="${mandatoryField eq true}">
                    <neutrino:input path="${path}" colSpan="${inputSelectColSpan}" id="${id}_${formKey}_${clonedRowStatus}"
                                tooltipKey="${dynamicFormToolTip}" maxLength="${maxFieldLength}"
                                minLength="${minFieldLength}" viewMode="${viewMode}"
                                validators="${validators}" mandatory="true"
                                labelKey="${labelKey }" readOnly="true" ></neutrino:input>
                     </c:if>
                    <c:if test="${mandatoryField eq false}">
                    <neutrino:input path="${path}" colSpan="${inputSelectColSpan}" id="${id}_${formKey}_${clonedRowStatus}"
                                tooltipKey="${dynamicFormToolTip}" maxLength="${maxFieldLength}"
                                minLength="${minFieldLength}" viewMode="${viewMode}"
                                validators="${validators}"
                                labelKey="${labelKey }" readOnly="true" ></neutrino:input>
                     </c:if>
                </c:when>
				<c:when test="${fieldType eq buttonFieldType}">
					
						<c:if test="${not empty authority}">
							<c:if test="${empty href}">
								<c:if test="${ not empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="javascript:void(null)" rel="tooltip" data-original-title="${dynamicFormToolTip}"
												onclick="fn${id}_${formKey}('${viewMode}')" class="btn btn-inverse" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
								<c:if test="${empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}"
												href="javascript:void(null)" rel="tooltip" data-original-title="${dynamicFormToolTip}"
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
												href="<c:out value='${href}' />" rel="tooltip" data-original-title="${dynamicFormToolTip}"
												onclick="fn${id}_${formKey}('${viewMode}')" class="btn btn-inverse" ><spring:message
													code="${labelKey}"></spring:message></a>
										</div>
									</security:authorize>
								</c:if>
								<c:if test="${empty functionLogic}">
									<security:authorize access="hasAnyAuthority('${authority}')">
										<div class="container" style="padding: 25px">
											<a id="${id}_${formKey}_${clonedRowStatus}" rel="tooltip" data-original-title="${dynamicFormToolTip}"
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
											href="javascript:void(null)" rel="tooltip" data-original-title="${dynamicFormToolTip}"
											onclick="fn${id}_${formKey}('${viewMode}')" class="btn btn-inverse" ><spring:message
												code="${labelKey}"></spring:message></a>
									</div>
								</c:if>
								<c:if test="${empty functionLogic}">
									<div class="container" style="padding: 25px">
										<a id="${id}_${formKey}_${clonedRowStatus}"
											href="javascript:void(null)" rel="tooltip" data-original-title="${dynamicFormToolTip}"
											class="btn btn-inverse" target="_blank"><spring:message
												code="${labelKey}"></spring:message></a>
									</div>
								</c:if>
							</c:if>
							<c:if test="${not empty href}">
								<c:if test="${ not empty functionLogic}">
									<div class="container" style="padding: 25px">
										<a id="${id}_${formKey}_${clonedRowStatus}"
											href="<c:out value='${href}' />" rel="tooltip" data-original-title="${dynamicFormToolTip}"
											onclick="fn${id}_${formKey}('${viewMode}')" class="btn btn-inverse" ><spring:message
												code="${labelKey}"></spring:message></a>
									</div>
								</c:if>
								<c:if test="${empty functionLogic}">
									<div class="container" style="padding: 25px">
										<a id="${id}_${formKey}_${clonedRowStatus}" rel="tooltip" data-original-title="${dynamicFormToolTip}"
											href="<c:out value='${href}' />" class="btn btn-inverse" target="_blank"><spring:message
												code="${labelKey}"></spring:message></a>
									</div>
								</c:if>
							</c:if>
						</c:if>
				</c:when>

				<c:when test="${fieldType eq  lovFieldType}">

					<c:if test="${mandatoryField eq true}">
						<div>

							<neutrino:lov id="${id}_${formKey}_${clonedRowStatus}"
								lovKey="${lovKey}" path="${absolutePath}.lovFieldVO.lovDisplayValue" hiddenPath="${absolutePath}.lovFieldVO.lovHiddenValue"
								mandatory="${mandatoryField}" viewMode="${viewMode}" value="${value}" 
								labelKey="${labelKey}" placeHolderKey="${labelKey}" tooltipKey="${dynamicFormToolTip}"
								colSpan="${lovColSpan}" inputBoxColSpan="${lovColSpan}" />

						</div>
					</c:if>

					<c:if test="${mandatoryField eq false}">
						<div>
							<neutrino:lov id="${id}_${formKey}_${clonedRowStatus}"
								lovKey="${lovKey}" path="${absolutePath}.lovFieldVO.lovDisplayValue" hiddenPath="${absolutePath}.lovFieldVO.lovHiddenValue"
								viewMode="${viewMode}" value="${value}"
								labelKey="${labelKey}" placeHolderKey="${labelKey}" tooltipKey="${dynamicFormToolTip}"
								colSpan="${lovColSpan}" inputBoxColSpan="${lovColSpan}" />
						</div>
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
								cssClass="form-control inputmask col-sm-12 ${validators} ${requiredCssClass}"
								id="${id}_${formKey}_${clonedRowStatus}"
								data-original-title="${dynamicFormToolTip}"
								maxlength="${maxFieldLength}" minlength="${minFieldLength}"
								disabled="${viewMode}" ></form:input>
						</div>
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
</div>


<input type="hidden" name="<c:out value='${absolutePath}' />.itemLabel"
	value="<c:out value='${itemLable}' />" />
<input type="hidden" name="<c:out value='${absolutePath}' />.id" value="<c:out value='${id}' />" />
<input type="hidden" name="<c:out value='${absolutePath}' />.fieldDataType"
	value="<c:out value='${fieldDataType}' />" />
<input type="hidden" name="<c:out value='${absolutePath}' />.entityName"
	value="<c:out value='${entityName}' />" />
<input type="hidden" name="<c:out value='${absolutePath}' />.fieldType"
	value="<c:out value='${fieldType}' />" />
<input type="hidden" name="<c:out value='${absolutePath}' />.parentFieldKey"
	value="<c:out value='${parentFieldKey}' />" />
<input type="hidden" name="<c:out value='${absolutePath}' />.lovKey"
	value="<c:out value='${lovKey}' />" />
<script>
(function(){
	if($("#${id}_${formKey}_${clonedRowStatus}.chosen_a").length>0){
		if($("#${id}_${formKey}_${clonedRowStatus}.chosen_a.chosen-done").length==0){
			executeOnLoad(["#${id}_${formKey}_${clonedRowStatus}"]);
}}
})();
</script>	