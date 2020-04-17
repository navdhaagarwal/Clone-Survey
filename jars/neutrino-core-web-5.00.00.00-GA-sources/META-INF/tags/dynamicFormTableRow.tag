<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@tag import="com.nucleus.core.formsConfiguration.FormComponentType"%>

<%@ attribute name="tableSingleItem" type = "com.nucleus.core.formsConfiguration.FormComponentVO" %>
<%@ attribute name="uiComponentsIndex"%>

<%@ attribute name="formComponentIndex"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="fieldItem" type = "com.nucleus.core.formsConfiguration.FormFieldVO"%>
<%@ attribute name="formKey"%>
<%@ attribute name="tableSingleItemStatusFirst"%>
<%@ attribute name="offlineTemplate"%>
<%@ attribute name="path"%>

<%@ attribute name="isSpecialTable"%>
<c:if test="${(empty path)}">
	<c:set var="path" value="uiComponents">
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
<c:set var="textBoxFieldType" scope="page"
	value="<%=FormComponentType.TEXT_BOX%>" />
<c:set var="cascadeSelectFieldType" scope="page"
	value="<%=FormComponentType.CASCADED_SELECT%>" />
	<c:set var="customCascadeSelectFieldType" scope="page"
	value="<%=FormComponentType.CUSTOM_CASCADED_SELECT%>" />
<c:if test="${(empty isSpecialTable)}">
	<c:set var="isSpecialTable" value="false">
	</c:set>
</c:if>



<c:forEach items="${tableSingleItem.formFieldVOList}"
						var="singleField" varStatus="singleFieldStatus">
	<td>
		<!-- start --> <c:choose>
			<c:when test="${singleField.fieldType eq  'DropDown'}">
				<c:if test="${singleField.mandatoryField eq true}">
					<c:set var="validators" scope="page">
												required
										</c:set>
				</c:if>
									<c:if test="${empty singleField.mandatoryField || singleField.mandatoryField eq false}">
					<c:remove var="validators" />
				</c:if>
				<div class="form-group columnDiv">
					<form:select id="${singleField.id}"
						path="${path}[${uiComponentsIndex}].formComponentList[${formComponentIndex}].formFieldVOList[${singleFieldStatus.index}].value[0]"
						cssClass="form-control ${validators} chosen_a col-sm-12"
						data-original-title="${singleField.toolTipMessage}"
						disabled="${viewMode}">
						<c:choose>
							<c:when
								test="${(not empty singleField.itemLabel) and (not empty singleField.itemValue) }">
								<c:if test="${singleField.includeSelect eq true}">
									<form:option value="">
										<spring:message code="label.select" />
									</form:option>
								</c:if>
								<c:forEach items="${neutrino:binder(singleField.binderName)}"
									var="item">
									<form:option value="${item[singleField.itemValue]}"
										disabled="${viewMode}" label="${item[singleField.itemLabel]}"></form:option>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<c:if test="${singleField.includeSelect eq true}">
									<form:option value="">
										<spring:message code="label.select" />
									</form:option>
								</c:if>
								<form:options items="${singleField.fieldCustomOptionsVOList}"
									itemLabel="customeItemLabel" itemValue="customeItemValue"
									disabled="${viewMode}" />
							</c:otherwise>
						</c:choose>
					</form:select>
				</div>
				<input type="hidden"
					name="${path}[${uiComponentsIndex}].formComponentList[${formComponentIndex}].formFieldVOList[${singleFieldStatus.index}].id"
					value="${singleField.id}" />

				<input type="hidden"
					name="${path}[${uiComponentsIndex}].formComponentList[${formComponentIndex}].formFieldVOList[${singleFieldStatus.index}].fieldDataType"
					value="${singleField.fieldDataType}" />

				<input type="hidden"
					name="${path}[${uiComponentsIndex}].formComponentList[${formComponentIndex}].formFieldVOList[${singleFieldStatus.index}].entityName"
					value="${singleField.entityName}" />

				<input type="hidden"
					name="${path}[${uiComponentsIndex}].formComponentList[${formComponentIndex}].formFieldVOList[${singleFieldStatus.index}].fieldType"
					value="${singleField.fieldType}" />

			</c:when>

			<c:otherwise>
				<c:choose>
										<c:when
											test="${singleField.fieldType eq  multiSelectBoxFieldType}">
						<c:set var="actualPath"
							value="${path}[${uiComponentsIndex}].formComponentList[${formComponentIndex}].formFieldVOList[${singleFieldStatus.index}].value"></c:set>
					</c:when>
					<%-- <c:when
																	test="${singleField.fieldType eq  phoneFieldType}">
																	<c:set var="actualPath"
																		value="uiComponents[${uiComponentsIndex}].formComponentList[${formComponentIndex}].formFieldVOList[${singleFieldStatus.index}].phoneNumberVO" />
																	<c:set var="actualValue"
																		value="${items[uiComponentsIndex].formFieldVOList[fieldItemStatus.index].value[0]}"></c:set>
																</c:when> --%>
					<c:when test="${singleField.fieldType eq  emailFieldType}">
						<c:set var="actualPath"
							value="${path}[${uiComponentsIndex}].formComponentList[${formComponentIndex}].formFieldVOList[${singleFieldStatus.index}].emailInfoVO" />
						<c:set var="actualValue"
							value="${items[uiComponentsIndex].formFieldVOList[fieldItemStatus.index].value[0]}"></c:set>
					</c:when>
					<c:otherwise>
						<c:set var="actualPath"
							value="${path}[${uiComponentsIndex}].formComponentList[${formComponentIndex}].formFieldVOList[${singleFieldStatus.index}].value[0]"></c:set>
											<c:set var="actualValue"
												value="${ singleField.value[0]}"></c:set>

					</c:otherwise>
				</c:choose>
				<c:if test="${offlineTemplate eq false}">


                    <neutrino:formField id="${singleField.id}"
                        path="${actualPath}" value="${actualValue}"

                        fieldType="${singleField.fieldType}"
                        binderName="${singleField.binderName}"
                        itemLable="${singleField.itemLabel}"
                        itemValue="${singleField.itemValue}"
                        mandatoryField="${singleField.mandatoryField}"
                        expandableField="${singleField.expandableField}"
                        includeSelect="${fieldItem.includeSelect}"
                        fieldDataType="${singleField.fieldDataType}"
                        dynamicFormToolTip="${singleField.toolTipMessage}"
                        entityName="${singleField.entityName}"
                        customeItemList="${singleField.fieldCustomOptionsVOList}"
                        customeMessage="${singleField.customeLongMessage}"
                        minFieldLength="${singleField.minFieldLength}"
                        maxFieldLength="${singleField.maxFieldLength}"
                        minFieldValue="${singleField.minFieldValue}"
                        maxFieldValue="${singleField.maxFieldValue}"
                        lovKey="${singleField.lovKey}"
                        formKey="${formKey}" viewMode="${viewMode}"
                        defDate="${singleField.defDate}"
                        searchableColumns="${singleField.searchableColumns}"
                        panelType="3"
                        clonedRowStatus="${formComponentIndex}"
                        parentFieldKey="${fieldItem.parentFieldKey}"
                        urlCascadeSelect="${fieldItem.urlCascadeSelect}"
                        specialTableColumn="${singleField.specialTable}"
                        />
				</c:if>
				<c:if test="${offlineTemplate eq true}">
										<neutrino:templateFormField id="${singleField.id}"
											path="${actualPath}" value="${actualValue}"
											fieldType="${singleField.fieldType}"
											binderName="${singleField.binderName}"
											itemLable="${singleField.itemLabel}"
											itemValue="${singleField.itemValue}"
											mandatoryField="${singleField.mandatoryField}"
											expandableField="${singleField.expandableField}"
											includeSelect="${fieldItem.includeSelect}"
											fieldDataType="${singleField.fieldDataType}"
											dynamicFormToolTip="${singleField.toolTipMessage}"
											entityName="${singleField.entityName}"
											customeItemList="${singleField.fieldCustomOptionsVOList}"
											customeMessage="${singleField.customeLongMessage}"
											minFieldLength="${singleField.minFieldLength}"
											maxFieldLength="${singleField.maxFieldLength}"
											minFieldValue="${singleField.minFieldValue}"
											maxFieldValue="${singleField.maxFieldValue}"
											formKey="${formKey}" viewMode="${viewMode}"
											defDate="${singleField.defDate}"
											searchableColumns="${singleField.searchableColumns}"
											panelType="3"/>
									</c:if>
			</c:otherwise>
		</c:choose> <!-- end -->
	</td>
</c:forEach>
<c:if test="${((empty viewMode) || (viewMode eq false)) && isSpecialTable eq false}">
						<td>
							<c:choose>
			<c:when test="${tableSingleItemStatusFirst}">
				<input class="btn btn-primary" type="button"
					id="addNewRowButton_${uiComponentsIndex}" value='+'
					onClick="addNewRow('${uiComponentsIndex}','${formKey}')" />
			</c:when>
			<c:otherwise>
				<input class='btn btn-primary' type='button'
					id='deleteRowButton_Customer Data' value='-'
										onclick="deleteNewRow(this)">
			</c:otherwise>
		</c:choose></td>
</c:if>