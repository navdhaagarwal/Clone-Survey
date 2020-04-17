<%@tag import="java.math.BigDecimal"%>
<%@tag import="com.nucleus.core.money.entity.Money"%>
<%@tag import="java.util.Locale"%>
<%@ tag import="com.nucleus.core.money.utils.MoneyUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>

<%@ attribute name="value" type="java.lang.Object" required="true"%>
<%@ attribute name="formatLocale" type="java.util.Locale"%>
<%@ attribute name="formatCurrencyCode"%>

<c:if test="${empty formatLocale}">
	<c:set var="userLocale" value="${neutrino:binder('currentUserLocale')}"
		scope="request"></c:set>
</c:if>
<c:if test="${not empty formatCurrencyCode}">
	<c:set var="currencyCodeToUse" value="${formatCurrencyCode}"
		scope="request"></c:set>
</c:if>

<%
    Locale userLocale = (Locale) request.getAttribute("userLocale");
    request.setAttribute("moneyBaseCurrencyCode", Money.getBaseCurrency().getCurrencyCode());
    if (value != null && userLocale != null) {
        if (Money.class.isAssignableFrom(value.getClass())) {
            request.setAttribute("formattedMoney", MoneyUtils.formatMoneyByLocale((Money) value, userLocale));

        } else if (BigDecimal.class.isAssignableFrom(value.getClass())) {
            request.setAttribute("formattedMoney",
                    MoneyUtils.formatMoneyByLocale(((BigDecimal) value).toPlainString(), userLocale));
        } else if (String.class.isAssignableFrom(value.getClass())) {
            request.setAttribute("formattedMoney", MoneyUtils.formatMoneyByLocale(((String) value), userLocale));
        }
    }
%>
<c:choose>
	<c:when test="${not empty formatCurrencyCode}">
		<span><b><c:out value='${formatCurrencyCode}' />&nbsp;&nbsp;<c:out value='${value.nonBaseAmount.value}' /></b></span>
	</c:when>
	<c:otherwise>
		<span><b><c:out value='${formattedMoney}' /></b></span>
	</c:otherwise>

</c:choose>


