<%@tag import="java.util.ArrayList"%>
<%@tag import="java.util.List"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@tag import="com.nucleus.autocomplete.AutocompleteLoadedEntitiesMap"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>
<%@ attribute name="disabled"%>
<%@ attribute name="id"%>
<%@ attribute name="path"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="itemValue"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="itemLabel"%>
<%@ attribute name="items" type="java.util.List"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="selectBoxColSpan"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="minArguments"%>
<%@ attribute name="maxArguments"%>
<%@ attribute name="multiSelectBoxSpanClass"%>
<%@ attribute name="name"%>
<%@ attribute name="value" type="java.util.Collection"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="defaultValue"%>
<%@ attribute name="enableJSOnUpdationOfList"%>
<%@ attribute name="itemDescription"%>
<%@ attribute name="enableAJAX"%>
<%@ attribute name="className"%>
<%@ attribute name="customURL"%>
<%@ attribute name="minCharToBeginSearch"%>
<%@ attribute name="pageSize"%>
<%@ attribute name="searchColumn"%>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="containsSearchEnabled"%>
<script>
$(document).ready(function(){
	$("#"+escapeSpecialCharactersInId("${id}")).change(function() {
		return handleMinSelectedOptions(escapeSpecialCharactersInId("${id}"));
	});
	$("#"+escapeSpecialCharactersInId("${id}")).on("chosen:maxselected",function(evt) {
		handleMaxSelectedOptions(escapeSpecialCharactersInId("${id}"),evt);
	});
});	
</script>
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
	
			
%>
<c:if test="${not empty className}">
			<%
				String value = (String) jspContext.getAttribute("className");
				AutocompleteLoadedEntitiesMap.addClassesToMap(value);
			%>
			
</c:if>
<c:if test="${not empty id}">
	<c:if test="${not empty minArguments}">
		<input type="hidden" id="minArguments_<c:out value='${id}' />" value="<c:out value='${minArguments}' />" />
	</c:if>
	<c:if test="${empty minArguments}">
		<input type="hidden" id="minArguments_<c:out value='${id}' />" value="1" />
	</c:if>
	<c:if test="${not empty maxArguments}">
		<input type="hidden" id="maxArguments_<c:out value='${id}' />" value="<c:out value='${maxArguments}' />" />
	</c:if>
	<c:if test="${empty maxArguments}">
		<c:set var="maxArguments" value="3"  scope="page"></c:set>
		<input type="hidden" id="maxArguments_<c:out value='${id}' />" value="3" />
	</c:if>
</c:if>

<c:set var="selectBoxColSpan" value="col-sm-10" scope="page" />
<c:if test="${not empty selectBoxColSpan}">
	<c:set var="selectBoxColSpan" value="col-sm-${selectBoxColSpan}"
		scope="page" />
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
<c:if test="${empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="label.select"></spring:message>
	</c:set>
</c:if>
<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
			required
		</c:set>
</c:if>
<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>
<c:if test="${not empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}"></spring:message>
	</c:set>
</c:if>
<c:if test="${not empty tooltipKey}">
	<c:set var="tooltipMessage" scope="page">
		<spring:message code="${tooltipKey}"></spring:message>
	</c:set>
</c:if>

<c:if test="${empty itemDescription}">
	<c:set var="itemDescription" scope="page" value="${itemLabel}" />
</c:if>
<c:set var="spanClass" value="col-sm-${colSpan}" scope="page" />
<div id="<c:out value='${id}' />-control-group"
	class="form-group input-group input-group <c:out value='${spanClass}' /> ${nonMandatoryClass}"
	style="margin-left: 0%">
	<c:if test="${not empty labelKey}">
		<label><strong><spring:message code="${labelKey}"></spring:message></strong>
			<c:if test="${not empty mandatory}">
				<span style="color: red">*</span>
			</c:if> </label>
	</c:if>
<c:set var="enableAjaxFlag" value="${enableAJAX eq true}" scope="page"/>
<c:set var="disableAjaxFlag" value="${empty enableAJAX || enableAJAX eq false}" scope="page"/>
	<c:if test="${not empty name && not empty value}">
		<c:if test="${empty viewMode}">
			<c:choose>
				<c:when test="${not empty disabled}">
				<c:choose>
				<c:when test="${disableAjaxFlag eq true}">
					<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
						Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
						data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
						data-original-title="<c:out value='${tooltipMessage}' />" disabled="<c:out value='${disabled}' />">
						<c:if test="${not empty items}">
							<c:forEach items="${items}" var="item">
								<c:set var="status" scope="page" value="false" />
								<c:forEach items="${value}" var="selectedItem">
									<c:if test="${status ne 'true'}">
										<c:set var="status" scope="page" value="false" />
									</c:if>
									<c:if test="${selectedItem[itemValue] eq item[itemValue]}">
										<c:set var="status" scope="page" value="true" />
										<option value="<c:out value='${item[itemValue]}' />" selected="selected"><c:out value='${item[itemLabel]}' /></option>
										
										<c:if test="${modificationAllowed == 'false'}"> 
										<c:set var="myVar" value="${myVar}${item[itemValue]}," />
										</c:if>
									</c:if>
								</c:forEach>
								<c:if test="${status ne 'true'}">
									<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
								</c:if>
							</c:forEach>
						</c:if>
					</select>
					</c:when>
					<c:otherwise>
						<c:if test="${enableAjaxFlag eq true}">
							<neutrino:multiSelectWithAjax id="${id}" name="${name}" 
								placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
								itemLabel="${itemLabel}" customURL="${customURL}"
								minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
								value="${value}" className="${className}" disabled="${disabled}"
								mandatory="${mandatory}" multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
								maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
								searchColumn="${searchColumn}" minArguments="${minArguments}" modificationAllowed="${modificationAllowed}" 
								containsSearchEnabled="${containsSearchEnabled}"
							/>
						</c:if>
					</c:otherwise>
				</c:choose>
				</c:when>
				<c:otherwise>
				<c:choose>
				<c:when test="${disableAjaxFlag eq true}">
					<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
						Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
						data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
						data-original-title="<c:out value='${tooltipMessage}' />">
						<c:if test="${not empty items}">
							<c:forEach items="${items}" var="item">
								<c:set var="status" scope="page" value="false" />
								<c:forEach items="${value}" var="selectedItem">
									<c:if test="${status ne 'true'}">
										<c:set var="status" scope="page" value="false" />
									</c:if>
									<c:if test="${selectedItem[itemValue] eq item[itemValue]}">
										<c:set var="status" scope="page" value="true" />
										<option value="<c:out value='${item[itemValue]}' />" selected="selected"><c:out value='${item[itemLabel]}' /></option>
										<c:if test="${modificationAllowed == 'false'}"> 
										<c:set var="myVar" value="${myVar}${item[itemValue]}," />
										</c:if>
									</c:if>
								</c:forEach>
								<c:if test="${status ne 'true'}">
									<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
								</c:if>
							</c:forEach>
						</c:if> 
					</select>
					</c:when>
					<c:otherwise>
						<c:if test="${enableAjaxFlag eq true}">
							<neutrino:multiSelectWithAjax id="${id}" name="${name}"
								placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
								itemLabel="${itemLabel}" customURL="${customURL}"
								minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
								value="${value}" className="${className}" mandatory="${mandatory}" 
								multiSelectBoxSpanClass="${multiSelectBoxSpanClass}" maxArguments="${maxArguments}"
								itemValue="${itemValue}" pageSize="${pageSize}" searchColumn="${searchColumn}"
								minArguments="${minArguments}"  modificationAllowed="${modificationAllowed}" 
								containsSearchEnabled="${containsSearchEnabled}"
							/>
						</c:if>
					</c:otherwise>
				</c:choose>
				</c:otherwise>
			</c:choose>
		</c:if>
	</c:if>
	<c:if test="${not empty name && not empty value}">
		<c:if test="${not empty viewMode}">
			<c:choose>
				<c:when test="${viewMode eq true}">
				<c:choose>
				<c:when test="${disableAjaxFlag eq true}">
					<select  multiple="multiple" id="<c:out value='${id}' />" disabled="disabled"
						name="<c:out value='${name}' />"
						Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
						data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
						data-original-title="<c:out value='${tooltipMessage}' />">
						<c:if test="${not empty items}">
							<c:forEach items="${items}" var="item">
								<c:forEach items="${value}" var="selectedItem">
									<c:if test="${selectedItem[itemValue] eq item[itemValue]}">
										<option value="<c:out value='${item[itemValue]}' />" selected="selected"><c:out value='${item[itemLabel]}' /></option>
										<c:if test="${modificationAllowed == 'false'}"> 
										<c:set var="myVar" value="${myVar}${item[itemValue]}," />
										</c:if>
									</c:if>
								</c:forEach>
							</c:forEach>
						</c:if>
					</select>
					</c:when>
					<c:otherwise>
					<c:if test="${enableAjaxFlag eq true}">
						<neutrino:multiSelectWithAjax id="${id}" name="${name}" 
								placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
								itemLabel="${itemLabel}" customURL="${customURL}"
								minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
								value="${value}" className="${className}" disabled="disabled"
								mandatory="${mandatory}" multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
								maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
								searchColumn="${searchColumn}" minArguments="${minArguments}"  modificationAllowed="${modificationAllowed}"
								containsSearchEnabled="${containsSearchEnabled}"
							/>
					</c:if>
					</c:otherwise>
					</c:choose>
				</c:when>
				<c:otherwise>
					<c:choose>
						<c:when test="${not empty disabled}">
						<c:choose>
						<c:when test="${disableAjaxFlag eq true}">
							<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
								Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
								data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
								data-original-title="<c:out value='${tooltipMessage}' />" disabled="<c:out value='${disabled}' />">
								<c:if test="${not empty items}">
									<c:forEach items="${items}" var="item">
										<c:set var="status" scope="page" value="false" />
										<c:forEach items="${value}" var="selectedItem">
											<c:if test="${status ne 'true'}">
												<c:set var="status" scope="page" value="false" />
											</c:if>
											<c:if test="${selectedItem[itemValue] eq item[itemValue]}">
												<c:set var="status" scope="page" value="true" />
												<option value="<c:out value='${item[itemValue]}' />" selected="selected"><c:out value='${item[itemLabel]}' /></option>
													<c:if test="${modificationAllowed == 'false'}"> 
										<c:set var="myVar" value="${myVar}${item[itemValue]}," />
										</c:if>
											</c:if>
										</c:forEach>
										<c:if test="${status ne 'true'}">
											<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
										</c:if>
									</c:forEach>
								</c:if>
							</select>
							</c:when>
							<c:otherwise>
							<c:if test="${enableAjaxFlag eq true}">
								<neutrino:multiSelectWithAjax id="${id}" name="${name}" 
									placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
									itemLabel="${itemLabel}" customURL="${customURL}"
									minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
									value="${value}" className="${className}" disabled="${disabled}"
									mandatory="${mandatory}" multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
									maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
									searchColumn="${searchColumn}" minArguments="${minArguments}"  modificationAllowed="${modificationAllowed}" 
									containsSearchEnabled="${containsSearchEnabled}"
								/>
							</c:if>
							</c:otherwise>
						</c:choose>
						</c:when>
						<c:otherwise>
						<c:choose>
						<c:when test="${disableAjaxFlag eq true}">
							<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
								Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
								data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
								data-original-title="<c:out value='${tooltipMessage}' />">
								<c:if test="${not empty items}">
									<c:forEach items="${items}" var="item">
										<c:set var="status" scope="page" value="false" />
										<c:forEach items="${value}" var="selectedItem">
											<c:if test="${status ne 'true'}">
												<c:set var="status" scope="page" value="false" />
											</c:if>
											<c:if test="${selectedItem[itemValue] eq item[itemValue]}">
												<c:set var="status" scope="page" value="true" />
												<option value="<c:out value='${item[itemValue]}' />" selected="selected"><c:out value='${item[itemLabel]}' /></option>
												<c:if test="${modificationAllowed == 'false'}"> 
										<c:set var="myVar" value="${myVar}${item[itemValue]}," />
										</c:if>
											</c:if>
										</c:forEach>
										<c:if test="${status ne 'true'}">
											<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
										</c:if>
									</c:forEach>
								</c:if>
							</select>
							</c:when>
							<c:otherwise>
							<c:if test="${enableAjaxFlag eq true}">
								<neutrino:multiSelectWithAjax id="${id}" name="${name}" 
									placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
									itemLabel="${itemLabel}" customURL="${customURL}"
									minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
									value="${value}" className="${className}"
									mandatory="${mandatory}" multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
									maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
									searchColumn="${searchColumn}" minArguments="${minArguments}"  modificationAllowed="${modificationAllowed}" 
									containsSearchEnabled="${containsSearchEnabled}"
								/>
							</c:if>
							</c:otherwise>
						</c:choose>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
		</c:if>
	</c:if>
	<c:if test="${not empty name && empty value}">
		<c:if test="${not empty viewMode}">
			<c:choose>
				<c:when test="${viewMode eq true}">
				<c:choose>
				<c:when test="${disableAjaxFlag eq true}">
					<select  multiple="multiple" id="<c:out value='${id}' />" disabled="disabled"
						name="<c:out value='${name}' />"
						Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
						data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
						data-original-title="<c:out value='${tooltipMessage}' />">
						<c:if test="${not empty items}">
							<c:forEach items="${items}" var="item">
								<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
							</c:forEach>
						</c:if>
					</select>
					</c:when>
					<c:otherwise>
					<c:if test="${enableAjaxFlag eq true}">
						<neutrino:multiSelectWithAjax id="${id}" name="${name}" 
								placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
								itemLabel="${itemLabel}" customURL="${customURL}"
								minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
								className="${className}" disabled="disabled"
								mandatory="${mandatory}" multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
								maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
								searchColumn="${searchColumn}" minArguments="${minArguments}"  modificationAllowed="${modificationAllowed}" 
								containsSearchEnabled="${containsSearchEnabled}"
							/>
					</c:if>
					</c:otherwise>
				</c:choose>
				</c:when>
				<c:otherwise>
					<c:choose>
					<c:when test="${not empty disabled}">
					<c:choose>
					<c:when test="${disableAjaxFlag eq true}">
							<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
								Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
								data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
								data-original-title="<c:out value='${tooltipMessage}' />" disabled="<c:out value='${disabled}' />">
								<c:if test="${not empty items}">
									<c:forEach items="${items}" var="item">
										<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
									</c:forEach>
								</c:if>
							</select>
							</c:when>
							<c:otherwise>
							<c:if test="${enableAjaxFlag eq true}">
								<neutrino:multiSelectWithAjax id="${id}" name="${name}" 
									placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
									itemLabel="${itemLabel}" customURL="${customURL}"
									minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
									className="${className}" disabled="${disabled}"
									mandatory="${mandatory}" multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
									maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
									searchColumn="${searchColumn}" minArguments="${minArguments}"  modificationAllowed="${modificationAllowed}" 
									containsSearchEnabled="${containsSearchEnabled}"
								/>
							</c:if>
							</c:otherwise>
						</c:choose>
						</c:when>
						<c:otherwise>
					<c:choose>
						<c:when test="${disableAjaxFlag eq true}">
							<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
								Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
								data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
								data-original-title="<c:out value='${tooltipMessage}' />">
								<c:if test="${not empty items}">
									<c:forEach items="${items}" var="item">
										<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
									</c:forEach>
								</c:if>
							</select>
						</c:when>
						<c:otherwise>
						<c:if test="${enableAjaxFlag eq true}">
							<neutrino:multiSelectWithAjax id="${id}" name="${name}" 
								placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
								itemLabel="${itemLabel}" customURL="${customURL}"
								minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
								className="${className}" disabled="${disabled}"
								mandatory="${mandatory}" multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
								maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
								searchColumn="${searchColumn}" minArguments="${minArguments}"  modificationAllowed="${modificationAllowed}" 
								containsSearchEnabled="${containsSearchEnabled}"
							/>
						</c:if>
						</c:otherwise>
					</c:choose>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
		</c:if>
	</c:if>
	<c:if test="${not empty name && empty value}">
		<c:if test="${empty viewMode}">
			<c:choose>
				<c:when test="${not empty disabled}">
				<c:choose>
					<c:when test="${disableAjaxFlag eq true}">
					<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
						disabled="<c:out value='${disabled}' />"
						Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
						data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
						data-original-title="<c:out value='${tooltipMessage}' />">
						<c:if test="${not empty items}">
							<c:forEach items="${items}" var="item">
								<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
							</c:forEach>
						</c:if>
					</select>
					</c:when>
					<c:otherwise>
					<c:if test="${enableAjaxFlag eq true}">
						<neutrino:multiSelectWithAjax id="${id}" name="${name}" 
								placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
								itemLabel="${itemLabel}" customURL="${customURL}"
								minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
								className="${className}" disabled="${disabled}"
								mandatory="${mandatory}" multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
								maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
								searchColumn="${searchColumn}" minArguments="${minArguments}" modificationAllowed="${modificationAllowed}" 
								containsSearchEnabled="${containsSearchEnabled}"
							/>
						</c:if>
					</c:otherwise>
				</c:choose>
				</c:when>
				<c:otherwise>
				<c:choose>
				<c:when test="${disableAjaxFlag eq true}">
					<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
						Class="form-control <c:out value='${multiSelectBoxSpanClass}' />  <c:out value='${validators}' />  chosen_a"
						data-placeholder="${placeHolderMessage}" tabindex="<c:out value='${tabindex}' />"
						data-original-title="<c:out value='${tooltipMessage}' />">
						<c:if test="${not empty items}">
							<c:forEach items="${items}" var="item">
								<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
							</c:forEach>
						</c:if>
					</select>
					</c:when>
					<c:otherwise>
					<c:if test="${enableAjaxFlag eq true}">
						<neutrino:multiSelectWithAjax id="${id}" name="${name}" 
								placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
								itemLabel="${itemLabel}" customURL="${customURL}"
								minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
								className="${className}" mandatory="${mandatory}" 
								multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
								maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
								searchColumn="${searchColumn}" minArguments="${minArguments}"  modificationAllowed="${modificationAllowed}" 
								containsSearchEnabled="${containsSearchEnabled}"
							/>
						</c:if>
					</c:otherwise>
				</c:choose>
				</c:otherwise>
			</c:choose>
		</c:if>
	</c:if>
	<c:if test="${not empty path}">
		<c:choose>
			<c:when test="${not empty disabled}">
			<c:choose>
				<c:when test="${disableAjaxFlag eq true}">
					<form:select  multiple="multiple" id="${id}" path="${path}"
						cssClass="form-control ${multiSelectBoxSpanClass}  ${validators}  chosen_a"
						data-placeholder="${placeHolderMessage}" tabindex="${tabindex}"
						data-original-title="${tooltipMessage}" disabled="${disabled}">
						<c:if test="${not empty items}">
							<%-- 				<form:option value="">${placeHolderMessage}</form:option> --%>
							<%-- <form:options items="${items}" itemValue="${itemValue}"
						itemLabel="${itemLabel}" /> --%>
							<c:forEach items="${items}" var="item">
								<form:option value="${item[itemValue]}"
									label="${item[itemLabel]}"></form:option>
							</c:forEach>
						</c:if>
					</form:select>
					</c:when>
					<c:otherwise>
					<c:if test="${enableAjaxFlag eq true}">
						<neutrino:multiSelectWithAjax id="${id}" name="${path}" 
								placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
								itemLabel="${itemLabel}" customURL="${customURL}"
								minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
								className="${className}" mandatory="${mandatory}" disabled="${disabled}"
								multiSelectBoxSpanClass="${multiSelectBoxSpanClass}" value="${value}"
								maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
								searchColumn="${searchColumn}" minArguments="${minArguments}"  modificationAllowed="${modificationAllowed}" 
								containsSearchEnabled="${containsSearchEnabled}"
							/>
						</c:if>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
			<c:choose>
				<c:when test="${disableAjaxFlag eq true}">
					<form:select  multiple="multiple" id="${id}" path="${path}"
						cssClass="form-control ${multiSelectBoxSpanClass}  ${validators}  chosen_a"
						data-placeholder="${placeHolderMessage}" tabindex="${tabindex}"
						data-original-title="${tooltipMessage}">
						<c:if test="${not empty items}">
							<%-- 				<form:option value="">${placeHolderMessage}</form:option> --%>
							<%-- <form:options items="${items}" itemValue="${itemValue}"
						itemLabel="${itemLabel}" /> --%>
							<c:forEach items="${items}" var="item">
								<form:option value="${item[itemValue]}"
									label="${item[itemLabel]}"></form:option>
							</c:forEach>
						</c:if>
					</form:select>
				</c:when>
				<c:otherwise>
				<c:if test="${enableAjaxFlag eq true}">
						<neutrino:multiSelectWithAjax id="${id}" name="${path}" 
								placeHolderKey="${placeHolderKey}" tabindex="${tabindex}"
								itemLabel="${itemLabel}" customURL="${customURL}"
								minCharToBeginSearch="${minCharToBeginSearch}" tooltipKey="${tooltipMessage}"
								className="${className}" mandatory="${mandatory}" value="${value}"
								multiSelectBoxSpanClass="${multiSelectBoxSpanClass}"
								maxArguments="${maxArguments}" itemValue="${itemValue}" pageSize="${pageSize}"
								searchColumn="${searchColumn}" minArguments="${minArguments}" modificationAllowed="${modificationAllowed}" 
								containsSearchEnabled="${containsSearchEnabled}"
							/>
					</c:if>
					</c:otherwise>
			</c:choose>
			</c:otherwise>
		</c:choose>
	</c:if>
	<%-- <form:select multiple="multiple" id="${id}" path="${path}" disabled="${disabled}" 
	name="${name}" cssClass="form-control ${multiSelectBoxSpanClass}  ${validators}  chosen_a"
	placeholder="${placeHolderMessage}" tabindex="${tabindex}" data-original-title="${tooltipMessage}">
	<c:if test="${not empty items}" >
		   <form:option value="">${placeHolderMessage}</form:option>	
		   <form:options items="${items}" itemValue="${itemValue}"
			itemLabel="${itemLabel}"  selected="${defaultValue}" />
	</c:if>
</form:select> --%>
</div>
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
<script>
	var id1 = escapeSpecialCharactersInId("<c:out value='${id}' />");
	$("#"+id1).data("neutrino-chosen-options",{max_selected_options:"${maxArguments}"});
	executeOnLoad(["#" + id1]);
</script>
<%
	String fieldName = null;
	String val = (String) jspContext.getAttribute("myVar");
	
	if (name == null) {
		fieldName = path;
	} else {
		fieldName = name;
	}
	
	try {
		
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty()) {
			val = val.substring(0,val.length()-1);
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>