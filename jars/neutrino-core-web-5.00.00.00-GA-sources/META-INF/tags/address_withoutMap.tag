<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="approvedCountryList" required="true" rtexprvalue="true" type="java.util.List"%>
<%@ attribute name="pathPrepender"%>
<%@ attribute name="viewMode" required="true"%>
<%@ attribute name="addressID" required="true"%>
<%@ attribute name="dialogId"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="implementationType"%>
<%@ attribute name="residenceFieldEnabled"%>
<%@ attribute name="requestPath"%>
<%@ attribute name="decorateSelectTag"%>
<%@ attribute name="disableISDCode"%>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="relatedAddressTypeId"%>
<%@ attribute name="addressDataVo" required="false" %>
<%@ attribute name="columnLayout" %>


<!-- Specifies which address id to be used, i.e to obtain it from CAS side or from global customer's side.Possible values are -true, false.
If not specified the by default its value will be false. -->
<%@ attribute name="source"%>
<%@ attribute name="countryId"%>

<!-- Specifies how the address object is to be received, i.e to obtain it from Collateral ID , then on the basis of this parameter value the address object will be searched. -->
<%@ attribute name="additionalParameter"%>

<!-- Parent Code added to distinguish in xml for same country and address type -->
<%@ attribute name="filterCode"%>



<!--
'dialogId' work as master Id in case address tag is to be used inside a modal window
 -->
<!-- This tag will work only for the data that exist in Masters
Steps to work with address tag
1.Select the Type of address
2.Select the country to populate rest of the country specific fields
3.Enter the rest of the fields

 -->
 <c:if test="${not empty requestPath}">
	<c:set var="requestPathValue" value="${requestPath}" scope="page" />
</c:if>
<c:if test="${empty relatedAddressTypeId }">
	<c:set var="relatedAddressTypeId" value="addressType"></c:set>
</c:if>
<c:if test="${not empty source}">
	<c:set var="source" value="${source}" scope="page" />
</c:if>
<c:if test="${empty source}">
	<c:set var="source" value="false" scope="page" />
</c:if>
<c:if test="${empty requestPath}">
 <c:set var="requestPathValue" value="AddressTag" scope="page" />
 </c:if>
<c:if test="${empty modificationAllowed}">
<c:set var="modificationAllowed" value="true" scope="page" />
</c:if>

<c:if test="${not empty pathPrepender}">
	<c:set var="completePath" value="${pathPrepender}.">
	</c:set>
</c:if>

<c:set var="addressID" value="${addressID}"></c:set>

<c:set var="dialogId" value="${dialogId}"></c:set>

<c:if test="${empty tabindex}">
<c:set var="tabindex" value=""></c:set>
</c:if>
<c:if test="${not empty tabindex}">
<c:set var="tabindex" value="${tabindex}"></c:set>
</c:if>


<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
			 required
		</c:set>
</c:if>
<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>

<c:if test="${empty columnLayout}">
	<c:set var="countryColSpan" scope="page" value="6"/>
</c:if>

<c:if test="${not empty columnLayout}">
	<fmt:parseNumber var = "countryColSpan" integerOnly = "true"  type = "number" value = "${12/columnLayout}" />
</c:if>

<div class="${nonMandatoryClass}">
	<div class="row">
		<div class="col-sm-${countryColSpan}  form-group chosen-full-ctrl">
              <div class='fancy-select col-sm-6 country_dd'>
			<label><strong><spring:message code="label.country"></spring:message>
					<c:if test="${mandatory eq true}">
						<span class="color-red">*</span>
					</c:if> </strong></label> <input type="hidden" class="addressHidden" id="addressHidden_<c:out value='${id}' />"
				name="<c:out value='${completePath}' />id" value="<c:out value='${addressID}' />" />
			<c:if test="${addressID eq '' || addressID eq null}">
				<c:if test="${viewMode eq true}">
					<select name="<c:out value='${completePath}' />country.id" id="text_<c:out value='${id}' />"
						class="form-control readonly_true chosen_a col-sm-12 " data-original-title="<spring:message code="label.Country"></spring:message>">
						<option value="">
							<spring:message code="label.select.one"></spring:message>
						</option>
						<c:forEach items="${approvedCountryList}" var="iCountry">
							<%-- <c:if test="${iCountry.countryISOCode eq 'IN'}">
			<option value="${iCountry.id}" selected="selected">${iCountry.countryName}</option>
			</c:if> --%>
							<%-- <c:if test="${iCountry.countryISOCode ne 'IN'}"> --%>
							<option value="<c:out value='${iCountry.id}' />"
								data-code="<c:out value='${iCountry.countryISOCode}' />"><c:out value='${iCountry.countryName}' /></option>
							<%-- </c:if> --%>
						</c:forEach>
					</select>
				</c:if>

				<c:if test="${viewMode ne true}">
					<select name="<c:out value='${completePath}' />country.id" id="text_<c:out value='${id}' />"
						class="form-control ${validators} chosen_a col-sm-12 " tabindex="<c:out value='${tabindex}' />" data-original-title="<spring:message code="label.Country"></spring:message>">
						<option value="">
							<spring:message code="label.select.one"></spring:message>
						</option>
						<c:forEach items="${approvedCountryList}" var="iCountry">
							<c:choose>
								<c:when
										test="${address.country.countryISOCode eq iCountry.countryISOCode}">
									<option value="<c:out value='${iCountry.id}' />" data-code="<c:out value='${iCountry.countryISOCode}' />" selected="selected"><c:out value='${iCountry.countryName}' /></option>
								</c:when>
								<c:otherwise>
									<option value="<c:out value='${iCountry.id}' />"  data-code="<c:out value='${iCountry.countryISOCode}' />" ><c:out value='${iCountry.countryName}' /></option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</c:if>
			</c:if>

			<c:if test="${addressID ne ''}">
				<c:if test="${viewMode eq true}">
					<select name="<c:out value='${completePath}' />country.id" id="text_<c:out value='${id}' />" disabled="true"
						class="form-control readonly_true chosen_a col-sm-12 " data-original-title="<spring:message code="label.Country"></spring:message>">
						<option value="<c:out value='${address.country.id}' />" data-code="<c:out value='${address.country.countryISOCode}' />" selected="selected"><c:out value='${address.country.countryName}' /></option>
					</select>
				</c:if>
				<c:if test="${viewMode ne true}">
					<select name="<c:out value='${completePath}' />country.id" id="text_<c:out value='${id}' />"
						tabindex="<c:out value='${tabindex}'  />"
						class="form-control ${validators} chosen_a col-sm-12 " data-original-title="<spring:message code="label.Country"></spring:message>">
						<option value="">
							<spring:message code="label.select.one"></spring:message>
						</option>
						<c:forEach items="${approvedCountryList}" var="iCountry">
							<c:choose>
								<c:when
									test="${address.country.countryISOCode eq iCountry.countryISOCode}">
									<option value="<c:out value='${iCountry.id}' />" data-code="<c:out value='${iCountry.countryISOCode}' />" selected="selected"><c:out value='${iCountry.countryName}' /></option>
								</c:when>
								<c:otherwise>
									<option value="<c:out value='${iCountry.id}' />"  data-code="<c:out value='${iCountry.countryISOCode}' />" ><c:out value='${iCountry.countryName}' /></option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
				</c:if>
			</c:if>

		</div>
	</div>
</div>

		<div class='row m-t10' id="remainingFields_<c:out value='${id}' />">
				<!-- ajax pulled data here -->
		</div>
</div>
<%-- Jira Id : PDDEV-14602/Jitendra Kumar
For external JS file address-without-map-tag.js --%>
<script>
(function(){
    address_withoutMapScriptInput = {};
    address_withoutMapScriptInput = {
    		id : "<c:out value='${id}' />",
      		addressId : "<c:out value='${addressID}' />",
      		dialogId : "<c:out value='${dialogId}' />",
      		pathPrepender : "<c:out value='${pathPrepender}' />",
      		implementationType : "<c:out value='${implementationType}' />",
      		residenceEnabled : "<c:out value='${residenceFieldEnabled}' />",
      		htmlAddressId : "<c:out value='${htmlAddressId}' />",
      		tabCount : "<c:out value='${tabindex}' />",
      		source : "<c:out value='${source}' />",
      		additionalParameter : "<c:out value='${additionalParameter}' />",
      		decorateSelectTag : "<c:out value='${decorateSelectTag}' />",
      		disableISDCode : "<c:out value='${disableISDCode}' />",
      		mandatoryAttr : "<c:out value='${mandatory}' />",
      		filterCode : "<c:out value='${filterCode}' />",
      		context : "<c:out value='${pageContext.request.contextPath}' />",
      		requestPathValue : "<c:out value='${requestPathValue}' />",
      		profileName :  "<c:out value='${casViewConfig[cas.client]}' />",
      		defaultCountryId : "<c:out value='${applicationScope.commonConfigUtility.defaultCountryISOCode}'/>",
      		completePath :	"<c:out value='${completePath}' />",
      		viewMode : "<c:out value='${viewMode}' />",
      		relatedAddressTypeId:"<c:out value='${relatedAddressTypeId}' />",
			addressDataVo: "<c:out value='${addressDataVo}' />",
            columnLayout : "<c:out value='${columnLayout}' />"

    }
	 address_withoutMapScript(address_withoutMapScriptInput);
})();
</script>