<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="approvedCountryList" required="true" rtexprvalue="true" type="java.util.List"%>
<%@ attribute name="pathPrepender"%>
<%@ attribute name="viewMode" required="true"%>
<%@ attribute name="addressID" required="true"%>
<%@ attribute name="dialogId"%>
<%@ attribute name="enableMap" required="true"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="implementationType"%>
<%@ attribute name="residenceFieldEnabled"%>
<%@ attribute name="decorateSelectTag"%>
<%@ attribute name="disableISDCode"%>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="relatedAddressTypeId"%>
<!-- The new attribute residenceFieldEnabled has been added to govern the fact that residence status and residence type are to be shown or not -->


<!-- 
'dialogId' work as master Id in case address tag is to be used inside a modal window 
 -->
<!-- This tag will work only for the data that exist in Masters
Steps to work with address tag
1.Select the Type of address
2.Select the country to populate rest of the country specific fields
3.Enter the rest of the fields 

 -->
 
 <c:if test="${empty relatedAddressTypeId }">
	<c:set var="relatedAddressTypeId" value="addressType"></c:set>
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


<div class="row">
	<div class="${nonMandatoryClass}">
		<label><strong><spring:message code="label.country"></spring:message>
		<c:if test="${mandatory eq true}">
		<span class="color-red">*</span>
		</c:if>
		</strong></label>
		
		<input type="hidden"  name="<c:out value='${completePath}' />id" value="<c:out value='${addressID}' />"/>
		<c:if test="${addressID eq '' || addressID eq null}">
		<c:if test="${viewMode eq true}">
		 <select name="<c:out value='${completePath}' />country.id" id="text_<c:out value='${id}' />" class="form-control readonly_true chosen_a col-sm-3" data-original-title="<spring:message code="label.Country"></spring:message>" style="width: 256px;">
			<c:forEach items="${approvedCountryList}" var="iCountry">
			<%-- <c:if test="${iCountry.countryISOCode eq 'IN'}">
			<option value="${iCountry.id}" selected="selected">${iCountry.countryName}</option>
			</c:if> --%>
			<%-- <c:if test="${iCountry.countryISOCode ne 'IN'}"> --%>
			<option value="<c:out value='${iCountry.id}' />" data-code="<c:out value='${iCountry.countryISOCode}' />"><c:out value='${iCountry.countryName}' /></option>
			<%-- </c:if> --%>
			</c:forEach>
		</select>
		</c:if>
		<c:if test="${viewMode ne true}">
		 <select name="<c:out value='${completePath}' />country.id" id="text_<c:out value='${id}' />"  class="form-control ${validators} chosen_a col-sm-3 "
		 tabindex="<c:out value='${tabindex}' />" style="width: 256px;" data-original-title="<spring:message code="label.Country"></spring:message>">
		 	<option value=""><spring:message code="label.select.one"></spring:message> </option>
			<c:forEach items="${approvedCountryList}" var="iCountry">
			<%-- <c:if test="${iCountry.countryISOCode eq 'IN'}">
			<option value="${iCountry.id}" selected="selected">${iCountry.countryName}</option>
			</c:if> --%>
			<%-- <c:if test="${iCountry.countryISOCode ne 'IN'}"> --%>
			<option value="<c:out value='${iCountry.id}' />" data-code="<c:out value='${iCountry.countryISOCode}' />"><c:out value='${iCountry.countryName}' /></option>
			<%-- </c:if> --%>
			</c:forEach>
		</select>
		</c:if>
		</c:if>
		<c:if test="${addressID ne ''}">
		<c:if test="${viewMode eq true}">
		<select name="<c:out value='${completePath}' />country.id" id="text_<c:out value='${id}' />" disabled="true" style="width: 256px;"
					  class="form-control readonly_true chosen_a col-sm-3 " data-original-title="<spring:message code="label.Country"></spring:message>" >
								<option value="<c:out value='${address.country.id}' />"
									selected="selected"><c:out value='${address.country.countryName}' /></option>
				</select>
		</c:if>
			<c:if test="${viewMode ne true}">
				<select name="<c:out value='${completePath}' />country.id" id="text_<c:out value='${id}' />" tabindex="<c:out value='${tabindex}' />" style="width: 256px;"
					class="form-control ${validators} chosen_a col-sm-3 " data-original-title="<spring:message code="label.Country"></spring:message>" >
					 <option value=""><spring:message code="label.select.one"></spring:message> </option>
					<c:forEach items="${approvedCountryList}" var="iCountry">
						<c:choose>
							<c:when
								test="${address.country.countryISOCode eq iCountry.countryISOCode}">
								<option value="<c:out value='${iCountry.id}' />"
									selected="selected"><c:out value='${iCountry.countryName}' /></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${iCountry.id}' />" data-code="<c:out value='${iCountry.countryISOCode}' />" ><c:out value='${iCountry.countryName}' /></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</c:if>
		</c:if>
	


<div class="row">
	<div id="remainingFields_<c:out value='${id}' />">
		<!-- ajax pulled data here -->
	</div>
</div>
</div>
</div>
<%-- Jira Id : PDDEV-14601/Jitendra Kumar
For external JS file address-with-map-tag.js --%>
<script>
(function(){
 var address_withMapScriptInput = {};

    address_withMapScriptInput = {
			id : "<c:out value='${id}' />",
            addressId : "<c:out value='${addressID}' />",
            dialogId : "<c:out value='${dialogId}' />",
            pathPrepender : "<c:out value='${pathPrepender}' />",
            implementationType : "<c:out value='${implementationType}' />",
            residenceEnabled : "<c:out value='${residenceFieldEnabled}' />",
            htmlAddressId : "<c:out value='${htmlAddressId}' />",
            tabCount : "<c:out value='${tabindex}' />",
            decorateSelectTag : "<c:out value='${decorateSelectTag}' />",
            disableISDCode : "<c:out value='${disableISDCode}' />",
            mandatoryAttr : "<c:out value='${mandatory}' />",
            profileName :  "<c:out value='${casViewConfig[cas.client]}' />",
            defaultCountryId : "<c:out value='${applicationScope.commonConfigUtility.defaultCountryISOCode}'/>",
            enableMapValue : "<c:out value='${enableMap}' />",
      		relatedAddressTypeId:"<c:out value='${relatedAddressTypeId}' />"
    }
    address_withMapScript(address_withMapScriptInput);
    })();
</script>


