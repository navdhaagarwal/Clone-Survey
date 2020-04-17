<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="id" required="true"%>
<%@ attribute name="items" type="java.util.List" required="true"%>




<jsp:doBody var="bodyContents"></jsp:doBody>
<c:choose>
	<c:when test="${fn:contains(bodyContents,'<')}">
	${bodyContents}
	</c:when>
	<c:otherwise>
		<div id="<c:out value='${id}' />-search-criteria" class="row m-l5">
				<c:forEach items="${items}" var="singleItem"
					varStatus="singleItemStatus" step="1" begin="0">
					<div class="col-sm-6">
						<c:if test="${not empty singleItem.displayName}">
							<c:set var="displayName" value="${singleItem.displayName}"></c:set>
						</c:if>
						<c:if test="${empty singleItem.displayName}">
							<c:set var="displayName" value="displayName"></c:set>
						</c:if>
						<neutrino:searchAttribute id="${singleItem.id}"
							path="searchAttributeList[${singleItemStatus.index}].value"
							type="${singleItem.type}" binderName="${singleItem.binderName}"
							labelKey="label.searchcriteria.${singleItem.id}"
							display="${singleItem.display}" operator="${singleItem.operator}"
							field="${singleItem.field}" itemLable="${displayName}"
							itemValue="id" />
					</div>
				</c:forEach>
		</div>
	</c:otherwise>
</c:choose>