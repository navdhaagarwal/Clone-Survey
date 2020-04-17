<%@tag import="com.nucleus.core.misc.util.DateUtils"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>
<%@ tag import="org.joda.time.DateTime"%>
<%@tag import="java.util.Date"%>


<%@ attribute name="id" required="true"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="creditCardTypeName"%>
<%@ attribute name="bankName"%>
<%@ attribute name="creditCardNumber"%>
<%@ attribute name="validFrom" type="org.joda.time.DateTime"%>
<%@ attribute name="validTill" type="org.joda.time.DateTime"%>
<%@ attribute name="cardHolderName"%>
<%@ attribute name="networkGateWay"%>
<%@ attribute name="cardTemplate"%>
<%@ attribute name="chipCardFlag"%>
<%@ attribute name="signatureEmbossedFlag"%>
<%@ attribute name="cardCssClass"%>
<%@ attribute name="creditCardCode"%>
<%@ attribute name="addCloseIcon" %>
<%@ attribute name="isCardMini" %>
<%@ attribute name="jpNumber" %>

<c:if test="${not empty id}">
	<c:set var="id_tag" value="${id}" scope="request" />
</c:if>

<c:if test="${not empty colSpan}">
	<c:set var="colSpanClass_tag" value="col-sm-${colSpan}" scope="request" />
</c:if>

<c:if test="${not empty creditCardTypeName}">
	<c:set var="creditCardTypeName_tag" value="${creditCardTypeName}" scope="request" />
</c:if>
<c:if test="${not empty bankName}">
	<c:set var="bankName_tag" value="${bankName}" scope="request" />
</c:if>
<c:if test="${not empty creditCardNumber}">
	<c:set var="creditCardNumber_tag" value="${creditCardNumber}" scope="request" />
</c:if>
<c:if test="${not empty validFrom}">
	<c:set var="validFrom_tag" value="${validFrom}" scope="request" />
</c:if>
<c:if test="${not empty validTill}">
	<c:set var="validTill_tag" value="${validTill}" scope="request" />
</c:if>
<c:if test="${not empty cardHolderName}">
	<c:set var="cardHolderName_tag" value="${cardHolderName}" scope="request" />
</c:if>
<c:if test="${not empty networkGateWay}">
	<c:set var="networkGateWay_tag" value="${networkGateWay}" scope="request" />
</c:if>
<c:if test="${not empty cardTemplate}">
	<c:set var="cardTemplate_tag" value="${cardTemplate}" scope="request" />
</c:if>
<c:if test="${not empty chipCardFlag}">
	<c:set var="chipCardFlag_tag" value="${chipCardFlag}" scope="request" />
</c:if>
<c:if test="${not empty signatureEmbossedFlag}">
	<c:set var="signatureEmbossedFlag_tag" value="${signatureEmbossedFlag}" scope="request" />
</c:if>

<c:if test="${not empty cardCssClass}">
	<c:set var="cardCssClass_tag" value="${cardCssClass}" scope="request" />
</c:if>
<c:if test="${not empty creditCardCode}">
	<c:set var="creditCardCode_tag" value="${creditCardCode}" scope="request" />
</c:if>
<c:if test="${not empty addCloseIcon}">
	<c:set var="addCloseIcon_tag" value="${addCloseIcon}" scope="request" />
</c:if>
<c:if test="${not empty isCardMini}">
	<c:set var="isCardMini_tag" value="${isCardMini}" scope="request" />
</c:if>
<c:if test="${not empty  jpNumber}">
	<c:set var="jpNumber_tag" value="${jpNumber}" scope="request"></c:set>
</c:if>



<c:if test="${empty bankName}">
	<c:set var="bankName_tag" value="BANK NAME" scope="request" />
</c:if>

<c:if test="${empty creditCardNumber}">
	<c:set var="creditCardNumber_tag" value="2402 XXXX XXXX XXXX" scope="request" />
</c:if>
<c:if test="${empty validFrom}">
	<%
	    DateTime startDate = DateUtils.getCurrentUTCTime();
	%>
	<c:set var="validFrom_tag" value="<%=startDate%>" scope="request" />
</c:if>

<c:if test="${empty validTill}">
	<%
	DateTime endDate = DateUtils.getCurrentUTCTime().plusYears(5);
	%>
	<c:set var="validTill_tag" value="<%=endDate%>" scope="request" />
</c:if>

<c:if test="${empty cardHolderName}">
	<c:set var="cardHolderName_tag" value="XXXXX" scope="request" />
</c:if>

<c:if test="${empty cardTemplate || cardTemplate eq ''}">
	<c:set var="cardTemplate_tag" value="default_Template" scope="request" />
</c:if>

<c:if test="${empty chipCardFlag}">
	<c:set var="chipCardFlag_tag" value="false" scope="request" />
</c:if>
<c:if test="${empty signatureEmbossedFlag}">
	<c:set var="signatureEmbossedFlag_tag" value="false" scope="request" />
</c:if>

<c:if test="${empty creditCardTypeName}">
	<c:set var="creditCardTypeName_tag" value="XXXXX" scope="request" />
</c:if>
<c:if test="${empty creditCardCode}">
	<c:set var="creditCardCode_tag" value="${creditCardTypeName}" scope="request" />
</c:if>
<c:if test="${empty addCloseIcon}">
	<c:set var="addCloseIcon_tag" value="false" scope="request" />
</c:if>
<c:if test="${empty isCardMini}">
	<c:set var="isCardMini_tag" value="false" scope="request" />
</c:if>
<c:if test="${empty jpNumber}">
	<c:set var="jpNumber_tag" value="XXXXXXXXX" scope="request"></c:set>
</c:if>
<c:if test="${empty networkGateWay}">
	<c:set var="networkGateWay_tag" value="VISA" scope="request" />
</c:if>

<jsp:include page="/WEB-INF/jsp/creditCardTemplates/${cardTemplate}.jsp"></jsp:include>


<!-- Removing these variables from Request Scope as these will be available on different jsps and different instances of Credit Card Tag
     under the same request.  -->


<c:remove var="id_tag" scope="request" />
<c:remove var="colSpan_tag" scope="request"/>
<c:remove var="creditCardTypeName_tag" scope="request"/>
<c:remove var="bankName_tag" scope="request"/>
<c:remove var="creditCardNumber_tag" scope="request"/>
<c:remove var="validFrom_tag" scope="request"/>
<c:remove var="validTill_tag" scope="request"/>
<c:remove var="cardHolderName_tag" scope="request"/>
<c:remove var="networkGateWay_tag" scope="request"/>
<c:remove var="cardTemplate_tag" scope="request"/>
<c:remove var="chipCardFlag_tag" scope="request"/>
<c:remove var="signatureEmbossedFlag_tag" scope="request"/>
<c:remove var="cardCssClass_tag" scope="request"/>
<c:remove var="creditCardCode_tag" scope="request"/>
<c:remove var="addCloseIcon_tag" scope="request"/>
<c:remove var="isCardMini_tag" scope="request"/>
<c:remove var="jpNumber_tag" scope="request"/>

 <script>
 
 function removeCard(obj) {
		obj.parent().parent().remove();
	} 
</script>


