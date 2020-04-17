<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ attribute name="labelKey" required="true"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="placeHolderKey" %>
<%@ attribute name="itemLabel" required="true"%>
<%@ attribute name="items" required="true" type="java.util.List"%>
<%@ attribute name="colSpan" required="true"%>
<%@ attribute name="listBoxColSpan"%>
<c:set var="colSpanClass" value="" scope="page" />
	<c:if test="${not empty colSpan}">
		<c:set var="colSpanClass" value="col-sm-${colSpan}" scope="page" />
	</c:if><c:set var="listBoxColSpanClass" value="" scope="page" />
	<c:if test="${not empty listBoxColSpan}">
		<c:set var="listBoxColSpanClass" value="col-sm-${listBoxColSpan}" scope="page" />
	</c:if>
	<c:if test="${empty listBoxColSpan}">
	<c:set var="listBoxColSpanClass" value="col-sm-12" scope="page" />
   </c:if>

								
 
<div id="<c:out value='${id}'/>"
						class="<c:out value='${listBoxColSpanClass}'/>  m-r10 style-fieldset style-legend">
						<fieldset>
							<legend class="m-b5">
								<h5>
									<strong><spring:message
											code="${labelKey}" /></strong>
								</h5>
							</legend>
						
								<div class="row">
								
									
										<c:forEach items="${items}"
											var="indvValue" varStatus="i">

											<div class="<c:out value='${colSpanClass}' />">${indvValue[itemLabel]}</div>
											
										</c:forEach>
									
								</div>
							
						</fieldset>
</div>