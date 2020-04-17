<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ tag import="com.nucleus.money.MoneyServiceImpl"%>
<%@ tag
	import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ tag import="org.springframework.context.ApplicationContext"%>
<%@ tag import="com.nucleus.currency.Currency"%>
<%@ tag import="com.nucleus.core.money.entity.Money"%>
<%@ tag import="com.nucleus.core.money.utils.MoneyUtils"%>
<%@ tag import="java.util.List"%>
<%@ tag import="java.util.Locale"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="disabled"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="maxLength"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="validators"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="moneyBoxColSpan"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="label"%>
<%@ attribute name="name" %>
<%@ attribute name="value" %>
<%@ attribute name="labelDynamicForm"%>
<%@ attribute name="dynamicFormToolTip"%>
<%@ attribute name="useBaseCurrency"%>
<%@ attribute name="useSpecificCurrency"%>
<%@ attribute name="acceptNegative"%>
<%@ attribute name="pathPrepender" %>
<%@ attribute name="conditionStatement"%>
<%@ attribute name="conditionValue"%>
<%@ attribute name="maskedValue"%>
<%@ attribute name="maskedPath"%>
<%
   ApplicationContext ctx = RequestContextUtils.findWebApplicationContext(request);
   MoneyServiceImpl moneyService = (MoneyServiceImpl) ctx.getBean("moneyService");
   List<Currency> currencies=null;
   Currency defaultCur = null;
   String code = Money.getBaseCurrency().getCurrencyCode();
   if(request!=null && (request.getAttribute("currencies")==null || request.getAttribute("defaultCurForTag")==null)){
		
		currencies = moneyService.retrieveAllActiveCurrencies();
		request.setAttribute("currencies",currencies);
		for(Currency cur : currencies){
			   if(cur!=null && cur.getIsBaseCurrency()){
				   defaultCur = cur;
				   request.setAttribute("defaultCurForTag",defaultCur);	
				   break;
			   }
		}
	}else if(request!=null){
	   currencies = (List<Currency>) request.getAttribute("currencies");
	   defaultCur = (Currency) request.getAttribute("defaultCurForTag");
   }
   jspContext.setAttribute("currencies", currencies);
   if(defaultCur != null){
    String baseCurCode= defaultCur.getIsoCode();
    jspContext.setAttribute("baseCurCode",baseCurCode);
   
   }
      
   String name = (String) jspContext.getAttribute("name");
   /*
		Temporarily Code commented -Attributes made non-mandatory
	 */
   /* if (name == null) {
		throw new SystemException(
				"Attribute 'name' must be specified");
	}  */
	
   String fieldName=name;
	
   String viewMode = (String) jspContext.getAttribute("viewMode");
   String regionalVisibility=(String)request.getAttribute(fieldName+"_regionalVisibility");
   String labelKey=(String)request.getAttribute(fieldName+"_label");
   String placeHolderKey=(String)request.getAttribute(fieldName+"_placeHolderKey");
   String tooltipKey=(String)request.getAttribute(fieldName+"_tooltipKey");
   jspContext.setAttribute("viewMode",viewMode);
   
   //formatting value
   String value = (String) jspContext.getAttribute("value");
   Locale userLocale = (Locale) jspContext.getAttribute("userLocale");
   if (userLocale == null && (request != null && request.getAttribute("userLocale") == null)) {
	   userLocale = moneyService.getUserLocale();
	   request.setAttribute("userLocale", userLocale);
   } else if (request != null) {
	   userLocale = (Locale) request.getAttribute("userLocale");
   }
   value = MoneyUtils.formatMoney(value, userLocale);
   jspContext.setAttribute("formattedValue",value);
   //using specific currency
    String specificCurrencyCode = (String) jspContext.getAttribute("useSpecificCurrency");
   if(specificCurrencyCode!=null)
   {
	   java.util.Currency specificCurrency=java.util.Currency.getInstance(specificCurrencyCode);
	    if(specificCurrency!=null)
	    {
	    	 jspContext.setAttribute("specificCurCode",specificCurrency.getCurrencyCode());
	    }
   }	   
/* 
	if(mandatory !=null && mandatory != "" && mandatory.equals("true")){
		jspContext.setAttribute("mandatory",mandatory);					
	}else if(mandatory !=null && mandatory != "" && mandatory.equals("false")){
		jspContext.setAttribute("mandatory","");
	}		 */		
	if(viewMode !=null && viewMode != ""){
		jspContext.setAttribute("viewMode",viewMode);					
	}
	if(labelKey !=null && labelKey != ""){
		jspContext.setAttribute("labelKey",labelKey);					
	}
	if(placeHolderKey!=null && placeHolderKey!=""){
		jspContext.setAttribute("placeHolderKey",placeHolderKey);
	}
	if(tooltipKey!=null && tooltipKey!=""){
		jspContext.setAttribute("tooltipKey",tooltipKey);
	}
	if(regionalVisibility !=null && regionalVisibility != "" && regionalVisibility.equals("false")){
		jspContext.setAttribute("regionalVisibility",regionalVisibility);
		
	}else{
		jspContext.setAttribute("regionalVisibility","true");
	}
    
	String pathPrepender=(String)jspContext.getAttribute("pathPrepender");
	
	if(name!=null && pathPrepender!= null){	
		StringBuilder appendedName=new StringBuilder();
		appendedName.append(pathPrepender).append(".").append(name);					
		jspContext.setAttribute("name",appendedName);					
	}
%>
<c:if test="${not empty conditionStatement}">
    <c:set var = "statementList" value = "${fn:split(conditionStatement, ';')}" scope="page" />
    <c:set var = "paramList" value = "${fn:split(conditionValue, ';')}" scope="page" />
    <c:forEach var = "statement" items="${statementList}" begin="0" varStatus="i" step="1">
        <c:if test="${fn:trim(statement)}">
            <c:set var = "conditionList" value = "${fn:split(paramList[i.index], ',')}" scope="page" />
            <c:forEach var="condition" items="${conditionList}">
                <c:set var = "conditionParams" value = "${fn:split(condition, '=')}" scope="page" />
                <c:if test="${fn:trim(conditionParams[0]) eq 'mandatory'}">
                    <c:set var = "mandatory" value = "${fn:replace(fn:trim(conditionParams[1]),'false','')}" scope="page" />
                </c:if>
                <c:if test="${fn:trim(conditionParams[0]) eq 'readOnly'}">
                    <c:set var = "readOnly" value = "${fn:replace(fn:trim(conditionParams[1]),'false','')}" scope="page" />
                </c:if>
                <c:if test="${fn:trim(conditionParams[0]) eq 'maxLength'}">
                    <c:set var = "maxLength" value = "${fn:trim(conditionParams[1])}" scope="page" />
                </c:if>
            </c:forEach>
        </c:if>
    </c:forEach>
</c:if>
<c:if test="${not empty errorPath}">
    <c:set var="errorPathValue">
          <form:errors path="${errorPath}"/>
    </c:set>  
</c:if>
<c:if test="${regionalVisibility eq true}">
<c:set var="colSpanClass" value="" scope="page" />
<c:if test="${not empty colSpan}">
	<c:set var="colSpanClass" value="col-sm-${colSpan}" scope="page" />
</c:if>
<c:if test="${not empty viewMode}">
	<c:if test="${viewMode eq true}">
		<c:set var="disabled" value="${viewMode}" scope="page" />
		<c:set var="placeHolderKey" value="" scope="page" />
		<c:set var="tooltipKey" value="" scope="page" />
		<c:set var="validators" value="" scope="page" />
	</c:if>
</c:if>
<c:if
	test="${ viewMode eq true || readOnly eq true || disabled eq true}">
	<c:set var="disableSelect" value="true"></c:set>
</c:if>
<c:set var="moneyBoxSpanClass" value="col-sm-10" scope="page" />
<c:if test="${not empty moneyBoxColSpan}">
	<c:set var="moneyBoxSpanClass" value="col-sm-${moneyBoxColSpan}"
		scope="page" />
</c:if>
<c:set var="inputMaxLength" value="43" scope="page" />
<c:if test="${maxLength ge 0}">
	<c:set var="inputMaxLength" value="${maxLength}" scope="page" />
</c:if>
<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
			${validators} required
		</c:set>
</c:if>
<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>
<c:if test="${empty label}">
	<c:set var="label" value="${label}" scope="page" />
	<c:set var="labelId" value="${id}" scope="page" />
</c:if>
<c:if test="${empty acceptNegative}">
	<c:set var="acceptNegative" value="false" scope="page" />
</c:if>
<c:if test="${false eq acceptNegative}">
	<c:set var="validateAmountClass" value="validatePositiveAmount" scope="page" />
</c:if>
<c:if test="${true eq acceptNegative}">
	<c:set var="validateAmountClass" value="validateNegativeAmount" scope="page" />
</c:if>
<c:if test="${not empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}" />
	</c:set>
</c:if>
<c:if test="${not empty tooltipKey}">
	<c:set var="tooltipMessage" scope="page">
		<spring:message code="${tooltipKey}"></spring:message>
	</c:set>
</c:if>
<c:if test="${not empty dynamicFormToolTip}">
	<c:set var="tooltipMessage" scope="page">
		<c:out value='${dynamicFormToolTip}' />
	</c:set>
</c:if>
<div id="<c:out value='${id}' />-control-group"
	class="money-ctrl form-group input-group input-group reset-m <c:out value='${colSpanClass}' />  ${nonMandatoryClass}">
	<c:choose>
	<c:when test="${not empty formattedValue && fn:contains(formattedValue, '~')}">
	<c:forTokens items="${formattedValue}" delims="~" var="item" varStatus="i">
						<c:if test="${i.index==0}">
							<c:set var="selMoneyCurr" value="${item}" />
						</c:if>
						<c:if test="${i.index==1}">
							<c:set var="selMoneyVal" value="${item}" />
						</c:if>
	</c:forTokens>
	</c:when>
	<c:otherwise>
	<c:if test="${not empty formattedValue}">
		<c:set var="selMoneyCurr" value="${baseCurCode}" />
		<c:set var="selMoneyVal" value="${value}" />
		<c:set var="formattedValue" value="${selMoneyCurr}~${selMoneyVal}" />
	</c:if>
	</c:otherwise>
	</c:choose>
	<c:choose>
	<c:when test="${not empty formattedValue && not fn:contains(formattedValue, '~')}">
		<c:catch var="catchString">
	  		<c:set var="myString" value="${0 + formattedValue}" />
		</c:catch>
		<c:if test="${empty catchString}">
			<c:set var="selMoneyCurr" value="${baseCurCode}" />
			<c:set var="selMoneyVal" value="${formattedValue}" />
			<c:set var="formattedValue" value="${selMoneyCurr}~${selMoneyVal}" />
		</c:if>
		<c:if test="${not empty catchString}">
			<c:set var="selMoneyCurr" value="${formattedValue}" />
			<c:set var="selMoneyVal" value="" />
			<c:set var="formattedValue" value="${selMoneyCurr}~${selMoneyVal}" />
		</c:if>	
	</c:when>
	<c:otherwise>
		<c:if test="${empty formattedValue}">
			<c:set var="selMoneyCurr" value="${baseCurCode}" />
		</c:if>
	</c:otherwise>
	</c:choose>
	<c:if test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true))}">
      <c:if test="${not empty maskedValue}">
   	<c:set var="selMoneyVal" value="${maskedValue}" scope="page" />
   	</c:if>
   </c:if>
	<input type="hidden" id="hid_<c:out value='${id}' />_appId" name="money_appId" />
	<input type="hidden" id="hid_<c:out value='${id}' />" name="<c:out value='${name}' />" value="<c:out value='${formattedValue}' />"  acceptNegative="<c:out value='${acceptNegative}' />"/>
	<c:choose>
		<c:when test="${not empty label}">
			<div id="<c:out value='${id}' />_moneyLabelDiv" class="ro-ff">
				<div class="row">
					<div class="moneyReadOnly_div">
					<c:choose>
					<c:when test="${not empty useBaseCurrency || useBaseCurrency eq true}">
					<input type="hidden" class="<c:out value='${moneyBoxSpanClass}' />"
							id="amountCurrency_<c:out value='${id}' />" value="<c:out value='${selMoneyCurr}' />" readonly="readonly"/>
					</c:when>
					<c:otherwise>
					<p id="label_amountCurrency_<c:out value='${id}' />" ><c:out value='${selMoneyCurr}' /></p>
					<input type="hidden" class="form-control <c:out value='${moneyBoxSpanClass}' />"
							id="amountCurrency_<c:out value='${id}' />" value="<c:out value='${selMoneyCurr}' />" readonly="readonly"/>
					</c:otherwise>
					</c:choose>
					</div>
					<div class="moneyReadOnly_div">
						<p id="label_<c:out value='${id}' />" ><c:out value='${selMoneyVal}' /></p>
						<input
							class="form-control currencyField amount <c:out value='${validateAmountClass}'/> <c:out value='${validators}' /> <c:out value='${moneyBoxSpanClass}' />"
							id="<c:out value='${id}' />" maxlength="<c:out value='${inputMaxLength}' />" type="hidden"
							readonly="readonly" value="<c:out value='${selMoneyVal}' />" />
					</div>

				</div>
			</div>
		</c:when>
		<c:otherwise>
			<c:if test="${not empty labelKey}">

				<label class="control-label"><strong><spring:message
							code="${labelKey}"></spring:message></strong> <c:if
						test="${not empty mandatory}">
						<span class='color-red'>*</span>
					</c:if> </label>
			</c:if>
			<c:if test="${not empty labelDynamicForm}">
				<label><strong><c:out value='${labelDynamicForm}' /></strong> <c:if
						test="${not empty mandatory}">
						<span class='color-red'>*</span>
					</c:if> </label>
			</c:if>
			<c:choose>
				<c:when test="${not empty useBaseCurrency || useBaseCurrency eq true}">
				<span class="input-group-btn-custom">
					<select id="listMoney_<c:out value='${id}' />" class="form-control readonly_${disableSelect} hide"
						style="width: 90px" >
						<c:forEach items="${currencies}" var="rowType">
								<c:if test="${rowType.isoCode eq baseCurCode}">
									<option value="<c:out value='${rowType.id}' />" selected="selected"
										data-code="<c:out value='${rowType.isoCode}' />"><c:out value='${rowType.isoCode}' /></option>
								</c:if>
						</c:forEach>
					</select>
					<c:if test="${not empty disableSelect}">
						<input
							class="form-control currencyField amount  <c:out value='${validateAmountClass}'/> no-currency-dd <c:out value='${validators}' /> <c:out value='${moneyBoxSpanClass}' />"
							id="amount_<c:out value='${id}' />" name="amount_<c:out value='${id}' />"
							maxlength="<c:out value='${inputMaxLength}' />" type="text" value="<c:out value='${selMoneyVal}' />"
							data-mask="${inputMask}" readOnly="readOnly"
							style="text-align: right; min-width: 100px;"
							data-original-title="${tooltipMessage}"
							placeholder="${placeHolderMessage}" />
					</c:if>
					<c:if test="${empty disableSelect}">
						<input
							class="form-control currencyField amount  <c:out value='${validateAmountClass}'/> no-currency-dd <c:out value='${validators}' /> <c:out value='${moneyBoxSpanClass}' />"
							id="amount_<c:out value='${id}' />" name="amount_<c:out value='${id}' />"
							maxlength="<c:out value='${inputMaxLength}' />" type="text" value="<c:out value='${selMoneyVal}' />"
							data-mask="${inputMask}"
							style="text-align: right; min-width: 100px;"
							data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />"
							placeholder="${placeHolderMessage}" />
					</c:if>
					</span>
				</c:when>
				<c:when
					test="${not empty useSpecificCurrency && not empty specificCurCode}">
					<span class="input-group-btn-custom">
					<select id="listMoney_<c:out value='${id}' />" class="form-control readonly_${disableSelect} hide"
						style="width: 90px" >

						<c:forEach items="${currencies}" var="rowType">
								<c:if test="${rowType.isoCode eq specificCurCode}">
									<option value="<c:out value='${rowType.id}' />" selected="selected"
										data-code="<c:out value='${rowType.isoCode}' />"><c:out value='${rowType.isoCode}' /></option>
								</c:if>
						</c:forEach>
					</select>
					<c:if test="${not empty disableSelect}">
						<input
							class="form-control currencyField amount  <c:out value='${validateAmountClass}'/> no-currency-dd <c:out value='${validators}' /> <c:out value='${moneyBoxSpanClass}' />"
							id="amount_<c:out value='${id}' />" name="amount_<c:out value='${id}' />"
							maxlength="<c:out value='${inputMaxLength}' />" type="text" value="<c:out value='${selMoneyVal}' />"
							data-mask="${inputMask}" readOnly="readOnly"
							style="text-align: right; min-width: 100px;"
							data-original-title="${tooltipMessage}"
							placeholder="${placeHolderMessage}" />
					</c:if>
					<c:if test="${empty disableSelect}">
						<input
							class="form-control currencyField amount  <c:out value='${validateAmountClass}'/> no-currency-dd <c:out value='${validators}' /> <c:out value='${moneyBoxSpanClass}' />"
							id="amount_<c:out value='${id}' />" name="amount_<c:out value='${id}' />"
							maxlength="<c:out value='${inputMaxLength}' />" type="text" value="<c:out value='${selMoneyVal}' />"
							data-mask="${inputMask}"
							style="text-align: right; min-width: 100px;"
							data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />"
							placeholder="${placeHolderMessage}" />
					</c:if>
</span>
				</c:when>
				<c:otherwise>
				<span class="input-group-btn-custom">
					<select id="listMoney_<c:out value='${id}' />" class="form-control readonly_${disableSelect}"
						style="width: 90px" >

						<%-- 				<form:options items="${currencies}" itemLabel="currencyName" itemValue="id" /> --%>
						<c:forEach items="${currencies}" var="rowType">
							<c:choose>
								<c:when test="${selMoneyCurr eq rowType.isoCode}">
									<option value="<c:out value='${rowType.id}' />" selected="selected"
										data-code="<c:out value='${rowType.isoCode}' />"><c:out value='${rowType.isoCode}' /></option>
								</c:when>
								<c:otherwise>
									<option value="<c:out value='${rowType.id}' />" data-code="<c:out value='${rowType.isoCode}' />"><c:out value='${rowType.isoCode}' /></option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
					<c:if test="${not empty disableSelect}">
						<input
							class="form-control currencyField amount  <c:out value='${validateAmountClass}'/> <c:out value='${validators}' /> <c:out value='${moneyBoxSpanClass}' />"
							id="amount_<c:out value='${id}' />" name="amount_<c:out value='${id}' />"
							maxlength="<c:out value='${inputMaxLength}' />" type="text" value="<c:out value='${selMoneyVal}' />"
							data-mask="${inputMask}" readOnly="readOnly"
							style="text-align: right; min-width: 100px;"
							data-original-title="${tooltipMessage}"
							placeholder="${placeHolderMessage}" />
					</c:if>
					<c:if test="${empty disableSelect}">
						<input
							class="form-control currencyField amount  <c:out value='${validateAmountClass}'/> <c:out value='${validators}' /> <c:out value='${moneyBoxSpanClass}' />"
							id="amount_<c:out value='${id}' />" name="amount_<c:out value='${id}' />"
							maxlength="<c:out value='${inputMaxLength}' />" type="text" value="<c:out value='${selMoneyVal}' />"
							data-mask="${inputMask}"
							style="text-align: right; min-width: 100px;"
							data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />"
							placeholder="${placeHolderMessage}" />
					</c:if>

</span>
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
	<c:if test="${not empty helpKey}">
		<span class="help-block"><spring:message code="${helpKey}" /></span>
	</c:if>
	<c:if test="${not empty errorPathValue}">
			<span for="amount_<c:out value='${id}'/>" generated="true" class="help-block" style=""><form:errors path="${errorPath}"/></span>
			<script>
			populateServerSideError('amount_<c:out value='${id}'/>');
			</script>
		</c:if>	

	<c:if test="${not empty messageKey}">
		<p class="text-info">
			<spring:message code="${messageKey}" />
		</p>
	</c:if>
</div>
<script>
(function(){
	var moneyTagScriptInput = {};
	moneyTagScriptInput = {
		hid_appId_mt : "#hid_<c:out value='${id}' />_appId",
		hId_mt : "#hid_<c:out value='${id}' />",
		moneyLabelDiv_mt : "#<c:out value='${id}' />_moneyLabelDiv",
		amount_mt : "#amount_<c:out value='${id}' />",
		listMoney_mt : "#listMoney_<c:out value='${id}' />",
		label_mt  : "<c:out value='${label}' />",
		id_mt : "#<c:out value='${id}' />",
		viewMode_mt : "<c:out value='${viewMode}' />",
		readonly_mt : "<c:out value='${readOnly}' />",
		formatCurrencyInput_mt : "<c:out value='${id}' />",
		formatCurrencyInput_acceptNegative_mt : "<c:out value='${acceptNegative}' />",
		applyTooltipInput_mt : "amount_<c:out value='${id}' />"	 ,
		alignToolTip:"<c:out value='${alignToolTip}' />"
	}
	 
	 moneyTagScript(moneyTagScriptInput);
})();
</script>
</c:if>