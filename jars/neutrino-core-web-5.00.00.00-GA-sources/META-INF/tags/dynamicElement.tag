<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ tag body-content="empty" dynamic-attributes='tagAttrs'%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ attribute name="id"%>
<%@ attribute name="cssClass"%>
<%@ attribute name="tagName"%>
<%@ attribute name="name"%>
<%@ attribute name="path"%>
<%@ attribute name="validator"%>
<%@ attribute name="boxColSpan"%>
<%@ attribute name="value"%>
<!-- this field is important for select field as it helps to load its options -->
<%@ attribute name="fieldName"%>
<%@ attribute name="index"%>

<c:set var="textField" value="enterable" />
<c:set var="lovField" value="lov" />
<c:set var="dateField" value="date" />
<c:set var="checkboxField" value="checkbox" />
<c:set var="masterSelectField" value="master" />

<c:if test="${not empty boxColSpan}">
	<c:set var="boxColSpanClass" value="col-sm-${boxColSpan}" scope="page" />
</c:if>

<c:if test="${not empty tagName}">
	<select id="equalityOp_<c:out value='${id}' />" class="form-control col-sm-6">
		<option value="==">==</option>
		<option value="LIKE">LIKE</option>
	</select>
	<c:choose>
		<c:when test="${fn:containsIgnoreCase(tagName,textField)}">
			<input type="text" id="<c:out value='${id}' />_field" index="<c:out value='${index}' />" value="<c:out value='${value}' />"
				class="form-control <c:out value='${validator}' /> <c:out value='${cssClass}' /> <c:out value='${boxColSpanClass}' />" />
		</c:when>
		<c:when test="${fn:containsIgnoreCase(tagName,lovField)}">
			<select id="<c:out value='${id}' />_field" index="<c:out value='${index}' />" value="<c:out value='${value}' />" name="genericParameter"
				class="form-control <c:out value='${cssClass}' /> <c:out value='${boxColSpanClass}' />">
				<option></option>
			</select>
		</c:when>
		<c:when test="${fn:containsIgnoreCase(tagName,dateField)}">
			<input type="text" id="<c:out value='${id}' />_field" index="<c:out value='${index}' />" value="<c:out value='${value}' />"
				class="form-control date <c:out value='${validator}' /> <c:out value='${cssClass}' /> <c:out value='${boxColSpanClass}' />" />
		</c:when>
		<c:when test="${fn:containsIgnoreCase(tagName,checkboxField)}">
			<%-- <input type="checkbox" id="${id}_field" class="${cssClass}" value="${value}" index="${index}"/> --%>
		</c:when>
		<c:when test="${fn:containsIgnoreCase(tagName,masterSelectField)}">
			<select id="<c:out value='${id}' />_field" index="<c:out value='${index}' />" value="<c:out value='${value}' />" name="master"
				class="form-control <c:out value='${cssClass}' /> <c:out value='${boxColSpanClass}' />">
				<option></option>
			</select>
		</c:when>
		<c:otherwise>
			Not Found
		</c:otherwise>
	</c:choose>
</c:if>
<script>
	$(function() {
		if ("<c:out value='${tagName}' />" == "<c:out value='${lovField}' />") {
			var genericType = "<c:out value='${fieldName}' />";
			$
					.ajax({
						url : "${pageContext.request.contextPath}/app/queryForReportGeneration/populateGenericParameterData",
						data : ({
							genericParameterType : genericType
						}),
						dataType : "json",
						async : false,
						success : function(items) {
							$.each(items, function(i, item) {
								$("select#<c:out value='${id}' />_field[name*='genericParameter']").append($('<option>', {
									value : item.id,
									text : item.name
								}));
							});
						},
						error : function(jqXHR, error, errorThrown) {
							alert('error');
						}
					});
		}
		/* if ('${tagName}' == '${masterSelectField}') {
			var masterField = '${fieldName}';
			$
					.ajax({
						url : "${pageContext.request.contextPath}/app/queryForReportGeneration/populateMasterData",
						data : ({
							master : masterField
						}),
						dataType : "json",
						async : false,
						success : function(items) {
							$.each(items, function(i, item) {
								$("select#${id}_field[name*='master']").append($('<option>', {
									value : item.countryISOCode,
									text : item.countryName
								}));
							});
						},
						error : function(jqXHR, error, errorThrown) {
							alert('error');
						}
					});
		} */
	});
</script>
