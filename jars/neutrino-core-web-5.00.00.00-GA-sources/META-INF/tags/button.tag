<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ tag body-content="empty" dynamic-attributes='tagAttrs'%>

<%@ attribute name="valueKey"%>
<%@ attribute name="id"%>
<%@ attribute name="cssClass"%>
<%@ attribute name="buttonType"%>
<%@ attribute name="onClickEvent"%>
<%@ attribute name="displayIcon"%>
<%@ attribute name="enableHide"%>
<%@ attribute name="imageButton"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="disabled"%>
<%@ attribute name="enablehtml5"%>
<%@ attribute name="href"%>

<c:if test="${not empty valueKey}">
	<c:set var="buttonValue">
		<spring:message code="${valueKey}" />
	</c:set>
</c:if>

<c:set var="tabindexAttr" ></c:set>
<c:if test="${not empty tabindex}">
	<c:set var="tabindexAttr">tabindex=${tabindex}</c:set>
</c:if>

<c:if test="${enableHide==true}">
	<c:set var="hideButton" value="hide"></c:set>
</c:if>

<c:if test="${disabled==true}">
	<c:set var="disableButton" value="disabled"></c:set>
</c:if>

<%
   String enablehtml5Value = (String) jspContext.getAttribute("enablehtml5");
   String idValue = (String) jspContext.getAttribute("id");
   if("true".equals(enablehtml5Value) && idValue==null){
     throw new IllegalArgumentException("id attribute is required.");
   }
%>

<c:choose>
	<c:when test="${enablehtml5==true}">
		<button id="<c:out value='${id}' />"
			type="<c:out value='${buttonType}' />"
			class="<c:out value='${cssClass}' /> ${hideButton}"
			onclick="${onClickEvent} "
			${tabindexAttr}
			${disableButton}>
			<i class="<c:out value='${displayIcon}'/> ${hideButton}"></i>
			${buttonValue}
		</button>
	</c:when>
	<c:otherwise>
		<c:if test="${empty imageButton}">
			<input id="<c:out value='${id}' />"
				type="<c:out value='${buttonType}' />"
				class="<c:out value='${cssClass}' /> ${hideButton}"
				value="${buttonValue}" onclick="${onClickEvent} "
				${tabindexAttr} ></input>
		</c:if>

		<c:if test="${not empty imageButton}">
			<a class="<c:out value='${cssClass}' />"
				href="<c:out value='${href}' />" id="<c:out value='${id}' />"
				${tabindexAttr}> <i
				class="<c:out value='${displayIcon}'/> ${hideButton}"></i>
				${buttonValue}
			</a>
		</c:if>
	</c:otherwise>
</c:choose>

