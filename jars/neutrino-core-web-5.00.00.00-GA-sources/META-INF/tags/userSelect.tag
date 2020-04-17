<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>


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
<%@ attribute name="tabindex"%>
<%@ attribute name="defaultValue"%>
<%@ attribute name="name"%>


<%@ attribute name="multiple"
	description="can select multiple values,should be bound to a list type parameter"%>

<%@ attribute name="filterButtonClass"
	description="apply bootstrap button classes to user search filter dropdowns.(btn-primary,btn-large,btn-info and others etc.)"%>
<%@ attribute name="multiselectButtonClass"
	description="apply bootstrap button classes to multiselect dropdown.(btn-primary,btn-large,btn-info and others etc.)"%>

<%@ attribute name="filterBackgroundClass"
	description="apply background color class to filter.(give 'none' to provide transparent background.default is a class with bgcolor #FBFBFC)"%>


<spring:message code="label.userSelectTag.teamSelectAll"
	var="teamSelectAll" scope="page" text="Select All Teams" />
<spring:message code="label.userSelectTag.roleSelectAll"
	var="roleSelectAll" scope="page" text="Select All Roles" />
<spring:message code="label.userSelectTag.branchSelectAll"
	var="branchSelectAll" scope="page" text="Select All Branches" />
<spring:message code="label.userSelectTag.bPartnerSelectAll"
	var="bPartnerSelectAll" scope="page" text="Select All Partners" />
<spring:message code="label.userSelectTag.userSelectAll"
	var="userSelectAll" scope="page" text="Select All Users" />

<c:choose>
	<c:when test="${alignment eq 'rtl'}">
	<c:set var="txtFloat" value="float-r" scope="page" />
	<c:set var="txtPadding" value="p-r5" scope="page" />
	<c:set var="txtPull" value="pull-left" scope="page" />
	</c:when>
	<c:otherwise>
	<c:set var="txtFloat" value="float-l" scope="page" />
	<c:set var="txtPadding" value="p-l5" scope="page" />
	<c:set var="txtPull" value="pull-right" scope="page" />
	</c:otherwise>
</c:choose>

<c:if test="${empty filterButtonClass}">
	<c:set var="filterButtonClass" value="btn-success" scope="page" />
</c:if>


<c:if test="${empty filterBackgroundClass}">
	<c:set var="filterBackgroundClass" value="filter_bgcolor" scope="page" />
</c:if>

<c:if test="${filterBackgroundClass eq none}">
	<c:set var="filterBackgroundClass" value="" scope="page" />
</c:if>

<c:if test="${not empty viewMode}">
	<c:if test="${viewMode eq true}">
		<c:set var="disabled" value="${viewMode}" scope="page" />
		<c:set var="placeHolderKey" value="" scope="page" />
		<c:set var="tooltipKey" value="" scope="page" />
	</c:if>
</c:if>

<c:if test="${empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="label.select.one"></spring:message>
	</c:set>
</c:if>

<c:if test="${not empty placeHolderKey}">
		<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}"></spring:message>
	</c:set>
</c:if>

<c:if test="${not empty mandatory}">
	<c:set var="validators" value="required" scope="page"></c:set>
</c:if>

<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>

<c:set var="selectBoxSpanClass" value="col-sm-10" scope="page" />
<c:if test="${not empty selectBoxColSpan}">
	<c:set var="selectBoxSpanClass" value="col-sm-${selectBoxColSpan}"
		scope="page" />
</c:if>

<c:if test="${not empty tooltipKey}">
	<c:set var="tooltipMessage" scope="page">
		<spring:message code="${tooltipKey}"></spring:message>
	</c:set>
</c:if>
<c:set var="spanClass" value="col-sm-12" scope="page" />
<c:set var="filterSpanClass" value="col-sm-11" scope="page" />
<c:if test="${not empty colSpan}">
	<c:set var="spanClass" value="col-sm-${colSpan}" scope="page" />
	<c:set var="filterSpanClass" value="col-sm-${colSpan-1}" scope="page" />
</c:if>




<c:set var="isMultiple" value="${(multiple==true)?'true':'false'}"
	scope="page" />
<c:set var="pluginElementClass"
	value="${(multiple==true)?'boot-multi_a':'chosen_userTag_a'}"
	scope="page" />

<c:set var="pluginControlGroupClass"
	value="${(multiple==true)?'boot-multi_top':'fancy-select select-ctrl'}"
	scope="page" />


<div class="userSelectTag_container">
	<div class="row">
		<div id="<c:out value='${id}' />-control-group"
			class="${pluginControlGroupClass} form-group <c:out value='${spanClass}' /> ${nonMandatoryClass} reset-m-l p-r5">
			<c:if test="${not empty labelKey}">
				<label><strong><spring:message code="${labelKey}"></spring:message></strong>
					<c:if test="${not empty mandatory}">
						<span class='color-red'>*</span>
					</c:if> </label>
			</c:if>

			<c:if test="${not empty path}">
				<form:select id="${id}" path="${path}" disabled="${disabled}"
					data-bm-select-all-text="${userSelectAll}"
					data-bm-btn-class="${multiselectButtonClass}"
					cssClass="form-control ${selectBoxSpanClass}  ${validators}  tooltip ${pluginElementClass} userSelectElement"
					data-placeholder="${placeHolderMessage}"
					data-original-title="${tooltipMessage}" multiple="${isMultiple}">
					<c:if test="${multiple ne true}">
						<form:option value=""></form:option>
					</c:if>
					<c:if test="${not empty items}">
						<form:options items="${items}" itemLabel="${itemLabel}"
							itemValue="${itemValue}"></form:options>
					</c:if>
				</form:select>
			</c:if>

			<c:if test="${empty path}">
				<select id="<c:out value='${id}' />" name="<c:out value='${name}' />"
					data-bm-select-all-text="${userSelectAll}"
					data-bm-btn-class="form-control <c:out value='${multiselectButtonClass}' />" data-placeholder="${placeHolderMessage}"
					class="form-control <c:out value='${selectBoxSpanClass}' />  <c:out value='${validators}' />  tooltip ${pluginElementClass} userSelectElement"
					data-original-title="${tooltipMessage}" multiple="${isMultiple}">
					<c:if test="${multiple ne true}">
						<option value=""></option>
					</c:if>
					<c:if test="${not empty items}">
						<c:forEach items="${items}" var="item">
							<option value="<c:out value='${item.id}' />"><c:out value='${item.username}' /></option>

						</c:forEach>

					</c:if>
				</select>
			</c:if>

			<c:if test="${viewMode ne true}">
				<div class="${txtFloat} ${txtPadding} m-t5">
					<span class="criteria_search_icon"
						onclick="showHideCriteria(this);" id="<c:out value='${id}' />filter-icon"> <i
						class="glyphicon glyphicon-filter"></i>
					</span>					
				</div>
			</c:if>
				<div class="${txtFloat} ${txtPadding} m-t5">
					<span class="selected_user_icon" multiUserSelectElementId="${id}"
						onclick="showSelectedUsers(this);" data-toggle="tooltip" data-placement="right" title="<spring:message code='label.show.selected.users' javaScriptEscape='true' />" id="<c:out value='${id}' />users-icon"> <i
						class=" glyphicon glyphicon-user"></i>
					</span>					
				</div>
		</div>
	</div>


	<div
		class="row userSelectTag_filter hide userSelectTag_filter_form">
		<div
			class="<c:out value='${filterSpanClass}' /> search_criteria_container <c:out value='${filterBackgroundClass}' />">
			<div class="border-line">
				<button class="btn btn-sm btn-info btn-xs  filterForm_submit"
					type="button">
					<spring:message code="label.userSelectTag.filterUsers" />
				</button>

				<button class="btn btn-xs ${txtPull} filterForm_reset"
					type="button">
					<spring:message code="label.userSelectTag.reset" />
				</button>
			</div>
			<div class="row">
				<div class="col-sm-4">
					<label class="control-label" for="inputEmail"><small><strong><spring:message
									code="label.userSelectTag.team" /></strong></small></label>
				</div>
				<div class="col-sm-8 uscri_businessPartner teams">
					<select class="form-control boot_multiselect" name="teamIds" multiple="multiple"
						data-bm-select-all-text="<c:out value='${teamSelectAll}' />" data-bm-width="120%"
						data-bm-height="auto"
						data-bm-btn-class="form-control <c:out value='${filterButtonClass}' /> btn-xs">
						<%-- <c:forEach items="${neutrino:binder('allTeams')}" var="team">
							<option value="<c:out value='${team.id}' />"><c:out value='${team.name}' /></option>
						</c:forEach> --%>

					</select>
				</div>
			</div>

			<div class="row">
				<div class="col-sm-4">
					<label class="control-label" for="inputEmail"><small><strong><spring:message
									code="label.userSelectTag.branch" /></strong></small></label>
				</div>
				<div class="col-sm-8 uscri_businessPartner orgBranch">
					<select class="form-control boot_multiselect" name="orgBranchIds"
						multiple="multiple" data-bm-select-all-text="${branchSelectAll}"
						data-bm-width="120%" data-bm-height="auto" data-bm-disable-filter="true" data-bm-include-select-all-text="false"
						data-bm-btn-class="form-control <c:out value='${filterButtonClass}' /> btn-xs">
						<%-- <c:forEach items="${neutrino:binder('organizationBranchList')}"
							var="branch">
							<option value="${branch.id}">${branch.name}</option>
						</c:forEach> --%>


					</select>
				</div>
			</div>

			<div class="row">
				<div class="col-sm-4">
					<label class="control-label" for="inputEmail"><small><strong><spring:message
									code="label.userSelectTag.role" /></strong></small></label>
				</div>
				<div class="col-sm-8 uscri_businessPartner role">
					<select class="form-control boot_multiselect" name="roleIds" multiple="multiple"
						data-bm-select-all-text="${roleSelectAll}" data-bm-width="120%"
						data-bm-height="auto"
						data-bm-btn-class="form-control <c:out value='${filterButtonClass}' /> btn-xs">
						<%-- <c:forEach items="${neutrino:binder('roleList')}" var="role">
							<option value="<c:out value='${role.id}' />"><c:out value='${role.name}' /></option>
						</c:forEach> --%>

					</select>
				</div>
			</div>

			<div class="row">
				<div class="col-sm-4">
					<label class="control-label"><small><strong><spring:message
									code="label.userSelectTag.businessPartner" /> </strong></small></label>
				</div>
				<div class="col-sm-8 uscri_businessPartner bPartner">
					<select class="form-control boot_multiselect" name="businessPartnerIds"
						multiple="multiple" data-bm-select-all-text="${bPartnerSelectAll}" data-bm-include-select-all-text="false"
						data-bm-width="120%" data-bm-height="auto" data-bm-disable-filter="true"
						data-bm-btn-class="form-control <c:out value='${filterButtonClass}' /> btn-xs">
						<%-- <c:forEach items="${neutrino:binder('businessPartnerList')}"
							var="bp">
							<option value="${bp.id}">${bp.name}</option>
						</c:forEach> --%>

					</select>
				</div>
			</div>
		</div>

	</div>
	<div class=" row userSelect_selectedUsers hide">
	
		
	
	</div>
	

	<c:if test="${not empty errorPath}">
		<p class="text-danger">
			<form:errors path="${errorPath}" />
		</p>
	</c:if>

	<c:if test="${not empty helpKey}">
		<span class="help-block"><spring:message code="${helpKey}" /></span>
	</c:if>
	<c:if test="${not empty messageKey}">
		<p class="text-info">
			<spring:message code="${messageKey}" />
		</p>
	</c:if>
	<br/>
</div>

<script>
	$(document)
			.ready(
					function() {

						executeOnLoad([ 'div.userSelectTag_container .boot-multi_a, .boot_multiselect, .searchable-form' ]);

					});
</script>