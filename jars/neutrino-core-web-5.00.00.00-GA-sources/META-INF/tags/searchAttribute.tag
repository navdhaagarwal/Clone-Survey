<%@tag
	import="com.nucleus.core.searchframework.service.SearchConfigProcessor"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>


<%@ attribute name="id" required="true"%>
<%@ attribute name="path" required="true"%>
<%@ attribute name="type" required="true"%>
<%@ attribute name="binderName"%>
<%@ attribute name="itemLable"%>
<%@ attribute name="itemValue"%>
<%@ attribute name="labelKey" required="true"%>
<%@ attribute name="display" required="true"%>
<%@ attribute name="operator" required="true"%>
<%@ attribute name="field" required="true"%>

<c:if test="${display eq 'visible'}"> 
 <div class="col-sm-12">
 <%-- <div style="display: ${display}" class="col-sm-12"> --%>
	<c:choose>
		<c:when test="${not empty binderName}">
			<c:choose>
				<c:when test="${(not empty itemLable) and (not empty itemValue) }">
				<div>
					<neutrino:select path="${path}" itemLabel="${itemLable}" id="${id}"
						itemValue="${itemValue}" items="${neutrino:binder(binderName)}"
						selectBoxColSpan="10" colSpan="12" labelKey="${labelKey}"></neutrino:select>
				</div>				
				</c:when>
				<c:when test="${not empty itemLable}">
				<div>
					<neutrino:select path="${path}" itemLabel="${itemLable}" id="${id}"
						itemValue="id" items="${neutrino:binder(binderName)}"
						selectBoxColSpan="10" colSpan="12" labelKey="${labelKey}"></neutrino:select>
				</div>		
				</c:when>
				<c:when test="${not empty itemValue}">
				<div>
					<neutrino:select path="${path}" itemLabel="displayName" id="${id}"
						itemValue="${itemValue}" items="${neutrino:binder(binderName)}"
						selectBoxColSpan="10" colSpan="12" labelKey="${labelKey}"></neutrino:select>
				</div>
				</c:when>
				
				<c:otherwise>
				<div>
					<neutrino:select path="${path}" id="${id}"
						items="${neutrino:binder(binderName)}" selectBoxColSpan="10" colSpan="12"
						labelKey="${labelKey}"></neutrino:select>
				</div>		
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${type eq  'bvalue' }">
					<label> <strong><spring:message code=""></spring:message></strong>
					</label>
					<form:radiobutton id="${id}" path="${path}" label="True" />
					<form:radiobutton id="${id}" path="${path}" label="False" />
				</c:when>
				<c:when test="${type eq 'dvalue'}">
					<neutrino:datepicker id="${id}" path="${path}" 
						editable="true" labelKey="${labelKey}" dateFormat="dd/mm/yyyy"/>
				</c:when>
				<c:otherwise>
					<label><strong><spring:message code="${labelKey}"></spring:message></strong></label>
					<div id="<c:out value='${id}' />-control-group"
						class="form-group input-group input-group nonMandatory">
						<form:input path="${path}" cssClass="form-control inputmask col-sm-10" id="${id}"></form:input>
					</div>
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
</div>
</c:if> 
<c:set var="absolutePath" value="${fn:substringBefore(path, '.')}" scope="page"></c:set>
<input type="hidden" name="<c:out value='${absolutePath}' />.id" value="<c:out value='${id}' />"/>
<input type="hidden" name="<c:out value='${absolutePath}' />.field" value="<c:out value='${field}' />"/>
<input type="hidden" name="<c:out value='${absolutePath}' />.operator" value="<c:out value='${operator}' />"/>
<input type="hidden" name="<c:out value='${absolutePath}' />.type" value="<c:out value='${type}' />"/>


