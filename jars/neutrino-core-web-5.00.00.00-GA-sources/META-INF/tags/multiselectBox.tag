<%@tag import="java.util.List"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@ attribute name="disabled"%>
<%@ attribute name="id"%>
<%@ attribute name="path"%>
<%@ attribute name="name"%>
<%@ attribute name="value" type="java.util.List"%>
<%@ attribute name="valueMap" type="java.util.Map"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="itemValue"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="itemLabel"%>
<%@ attribute name="items" type="java.util.List"%>
<%@ attribute name="itemsMap" type="java.util.Map"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="selectBoxColSpan"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="labelDynamicForm"%>
<%@ attribute name="dynamicFormToolTip"%>
<%@ attribute name="itemDescription"%>
<%@ attribute name="modificationAllowed"%>
<c:if test="${not empty viewMode}">
	<c:if test="${viewMode eq true}">
		<c:set var="disabled" value="${viewMode}" scope="page" />
		<c:set var="placeHolderKey" value="" scope="page" />
		<c:set var="tooltipKey" value="" scope="page" />
	</c:if>
</c:if>
<c:if test="${not empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}"></spring:message>
	</c:set>
</c:if>
<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>
<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
			required
		</c:set>
</c:if>
<c:set var="selectBoxSpanClass" value="col-sm-10" scope="page" />
<c:if test="${not empty selectBoxColSpan}">
	<c:set var="selectBoxSpanClass" value="col-sm-${selectBoxColSpan}"
		scope="page" />
</c:if>
<c:if test="${not empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}"></spring:message>
	</c:set>
</c:if>
<c:if test="${not empty tooltipKey}">
	<c:set var="tooltipMessage" scope="page">
		<spring:message code="${tooltipKey}"></spring:message>
	</c:set>
</c:if>
<c:set var="spanClass" value="col-sm-${colSpan}" scope="page" />
<c:if test="${not empty labelDynamicForm}">
	<label><strong><c:out value='${labelDynamicForm}' /></strong> <c:if
			test="${not empty mandatory}">
			<span class='color-red'>*</span>
		</c:if> </label>
</c:if>
<c:if test="${not empty dynamicFormToolTip}">
	<c:set var="tooltipMessage" scope="page">
		<c:out value='${dynamicFormToolTip}' />
	</c:set>
</c:if>

<c:if test="${empty itemDescription}">
	<c:set var="itemDescription" scope="page" value="${itemLabel}" />
</c:if>
<div class="multiselectBox-container">
	
		<div id="<c:out value='${id}' />-control-group"
		class="form-group ${spanClass} ${nonMandatoryClass}">
		<c:if test="${not empty labelKey}">
			<label><strong><spring:message code="${labelKey}"></spring:message></strong>
				<c:if test="${not empty mandatory}">
					<span class="color-red">*</span>
				</c:if> </label>
		</c:if>		
	<c:choose>		
	  <c:when test="${not empty items }">
		<c:choose>
			<c:when test="${not empty name  && empty disabled}">
				<select id="<c:out value='${id}' />" name="<c:out value='${name}' />" multiple="multiple"
					class="form-control ${selectBoxSpanClass} <c:out value='${validators}' /> searchable-form"
					data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />">
					<c:forEach varStatus="i" items="${items}" var="item">
						<c:choose>
							<c:when test="${not empty value}">
						<%-- 		<c:forEach varStatus="k" items="${value}" var="selectedItem">
									<c:choose>
										<c:when test="${selectedItem[itemValue] == item[itemValue]}">
											<option value="${item[itemValue]}" selected="selected">${item[itemLabel]}</option>
										</c:when>
										<c:otherwise>
											<option value="${item[itemValue]}">${item[itemLabel]}</option>
										</c:otherwise>
									</c:choose>
								</c:forEach>
 --%>
										<c:choose>
											<c:when test="${fn:contains(value, item[itemValue])}">
											<option value="<c:out value='${item[itemValue]}' />" selected="selected"><c:out value='${item[itemLabel]}' /></option>
											<c:if test="${modificationAllowed == 'false'}"> 
											<c:set var="myVar" value="${myVar}${item[itemValue]}," />
											</c:if>
											</c:when>
											<c:otherwise>
												<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
											</c:otherwise>
										</c:choose>
									</c:when>
							<c:otherwise>

								<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>

				</select>
			</c:when>
			<c:when test="${not empty name  && not empty disabled}">
				<select id="<c:out value='${id}' />" name="<c:out value='${name}' />" multiple="multiple"
					disabled="disabled"
					class="form-control ${selectBoxSpanClass} <c:out value='${validators}' /> searchable-form"
					data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />">
					<c:forEach varStatus="i" items="${items}" var="item">
						<c:choose>
							<c:when test="${not empty value}">
								<%-- <c:forEach varStatus="k" items="${value}" var="selectedItem">
									<c:choose>
										<c:when test="${selectedItem[itemValue] == item[itemValue]}">
											<option value="${item[itemValue]}" selected="selected">${item[itemLabel]}</option>
										</c:when>
										<c:otherwise>
											<option value="${item[itemValue]}">${item[itemLabel]}</option>
										</c:otherwise>
									</c:choose>
								</c:forEach> --%>
										<c:choose>
											<c:when test="${fn:contains(value, item[itemValue])}">
												<option value="<c:out value='${item[itemValue]}' />" selected="selected"><c:out value='${item[itemLabel]}' /></option>
												<c:if test="${modificationAllowed == 'false'}"> 
												<c:set var="myVar" value="${myVar}${item[itemValue]}," />
												</c:if>
											</c:when>
											<c:otherwise>
												<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
											</c:otherwise>
										</c:choose>
									</c:when>
							<c:otherwise>

								<option value="<c:out value='${item[itemValue]}' />"><c:out value='${item[itemLabel]}' /></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</c:when>
			<c:otherwise>
				<form:select id="${id}" path="${path}" multiple="multiple"
					disabled="${disabled}" name="${name}"
					cssClass="form-control ${selectBoxSpanClass} ${validators} searchable-form"
					placeholder="${placeHolderMessage}"
					data-original-title="${tooltipMessage}" tabindex="${tabindex}">
					<c:if test="${not empty items}">
						<%-- <form:option value="">${placeHolderMessage}</form:option> --%>
						<c:forEach items="${items}" var="item">
							<form:option value="${item[itemValue]}" label="${item[itemLabel]}" ></form:option>
						</c:forEach>
					</c:if>
				</form:select>
			</c:otherwise>
			</c:choose>
		  </c:when>			
		  <c:when test="${not empty itemsMap }">
			<c:choose>
			 <c:when test="${not empty name  && empty disabled}">
				<select id="<c:out value='${id}' />" name="<c:out value='${name}' />" multiple="multiple"
					class="form-control ${selectBoxSpanClass} <c:out value='${validators}' /> searchable-form"
					data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />">
					<c:forEach varStatus="i" items="${itemsMap}" var="item">
						<c:choose>
							<c:when test="${not empty valueMap}">
                                <c:choose>
                                    <c:when test="${fn:contains(valueMap, item.key)}">
                                        <option value="<c:out value='${item.key}' />" selected="selected"><c:out value='${item.value}' /></option>
                                    	<c:if test="${modificationAllowed == 'false'}"> 
                                    	<c:set var="myVar" value="${myVar}${item.key}," />
                                    	</c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <option value="<c:out value='${item.key}' />"><c:out value='${item.value}' /></option>
                                    </c:otherwise>
                                </c:choose>
							</c:when>
                            <c:otherwise>
								<option value="<c:out value='${item.key}' />"><c:out value='${item.value}' /></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</c:when>
			<c:when test="${not empty name  && not empty disabled}">
				<select id="<c:out value='${id}' />" name="<c:out value='${name}' />" multiple="multiple"
					disabled="disabled"
					class="form-control ${selectBoxSpanClass} <c:out value='${validators}' /> searchable-form"
					data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />">
					<c:forEach varStatus="i" items="${itemsMap}" var="item">
						<c:choose>
							<c:when test="${not empty valueMap}">
                                <c:choose>
                                    <c:when test="${fn:contains(valueMap, item.key)}">
                                        <option value="<c:out value='${item.key}' />" selected="selected"><c:out value='${item.value}' /></option>
                                   		<c:if test="${modificationAllowed == 'false'}"> 
                                   		<c:set var="myVar" value="${myVar}${item.key}," />
                                   		</c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <option value="<c:out value='${item.key}' />"><c:out value='${item.value}' /></option>
                                    </c:otherwise>
                                </c:choose>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${item.key}' />"><c:out value='${item.value}' /></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</c:when>
			<c:otherwise>
				<form:select id="${id}" path="${path}" multiple="multiple"
					disabled="${disabled}" name="${name}"
					cssClass="form-control ${selectBoxSpanClass} ${validators} searchable-form"
					placeholder="${placeHolderMessage}"
					data-original-title="${tooltipMessage}" tabindex="${tabindex}">
					<c:if test="${not empty itemsMap}">
						<%-- <form:option value="">${placeHolderMessage}</form:option> --%>
						<c:forEach items="${itemsMap}" var="item">
							<form:option value="${item.key}" label="${item.value}" ></form:option>
						</c:forEach>
					</c:if>
				</form:select>
			</c:otherwise>
		   </c:choose>
		  </c:when>
		   <c:otherwise>
				<select id="<c:out value='${id}' />" name="<c:out value='${name}' />" multiple="multiple"
        class="form-control ${selectBoxSpanClass} <c:out value='${validators}' /> searchable-form"
        data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}'/>">
				<option value="No data is available to select" disabled>No data is available to select</option>
      	</select>
		  </c:otherwise>
	      <%-- <c:when test="${empty items }">
		  <select id="id" name="name" multiple="multiple"
					class="form-control ${selectBoxSpanClass} ${validators} searchable-form"
					data-original-title="${tooltipMessage}" tabindex="${tabindex}">
		   </c:when> --%>
		</c:choose>
	</div>
	
</div>
<%-- /.multiSelectBox --%>
<c:if test="${not empty helpKey}">
	<span class="help-block"><spring:message code="${helpKey}" /></span>
</c:if>
<c:if test="${not empty errorPath}">
	<p class="text-danger">
		<form:errors path="${errorPath}" />
	</p>
</c:if>
<c:if test="${not empty messageKey}">
	<p class="text-info">
		<spring:message code="${messageKey}" />
	</p>
</c:if>
<%-- JS CODE --%>
<script>

(function(){
	var multiSelectBoxTagScriptInput = {};
	multiSelectBoxTagScriptInput = {
			 tabindex_msb : "<c:out value='${tabindex}' />",
			 id_msb : "<c:out value='${id}' />",
			 idsExecuteOnLoad_msb : ["#<c:out value='${id}'/>"] 
	}
	 
	multiSelectBoxTagScript(multiSelectBoxTagScriptInput);
})();

</script>
<%
	
	String fieldName = null;
	String val = (String) jspContext.getAttribute("myVar");
		
	if (name == null) {
		fieldName = path;
	} else {
		fieldName = name;
	}
	
	try {
		
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty()) {
			
			val = val.substring(0,val.length()-1);
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>