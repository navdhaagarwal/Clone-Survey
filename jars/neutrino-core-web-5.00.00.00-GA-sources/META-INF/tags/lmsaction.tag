<%@ tag language="java" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ attribute name="imgSrc" required="false" type="java.lang.String" description="" %>
<%@ attribute name="onClick" required="false" type="java.lang.String" description="" %>

<c:choose>
		<c:when test="${empty imgSrcList}">
			<c:set var="imgSrcList" value="${imgSrc eq null ? '/action/image.jpg' : imgSrc}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${imgSrc eq null}">
					<c:set var="imgSrcList" value="${imgSrcList}${delimitor}/action/image.jpg" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="imgSrcList" value="${imgSrcList}${delimitor}${imgSrc}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty onClickList}">
			<c:set var="onClickList" value="${onClick eq null ? 'myAction()' : onClick}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${onClick eq null}">
					<c:set var="onClickList" value="${onClickList}${delimitor}myAction()" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="onClickList" value="${onClickList}${delimitor}${onClick}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>


