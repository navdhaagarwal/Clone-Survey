<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ attribute name="colSpan"%>
<%@ attribute name="selectBoxColSpan"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="parentId"%>
<%@ attribute name="url"%>
<%@ attribute name="path"%>
<%@ attribute name="name"%>
<%@ attribute name="value"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="itemValue"%>
<%@ attribute name="itemLabel"%>
<%@ attribute name="itemCode"%>
<%@ attribute name="items" type="java.util.List"%>
<%@ attribute name="itemsMap" type="java.util.Map"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="disabled"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="pathPrepender" %>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="conditionStatement"%>
<%@ attribute name="conditionValue"%>
<%-- <%@ attribute name="className"%>
<%@ attribute name="searchColList"%>
<%@ attribute name="staticFlag"%> --%>
 
 
 
 <%
	    		String name = (String) jspContext.getAttribute("name");
				String path = (String) jspContext.getAttribute("path");
			
				/*
		  		Temporarily Code commented -Attributes made non-mandatory
		 		 */
				/* if (name == null && path == null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' must be specified");
				} else  
				if (name != null && path != null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' can be specified at once");
				}*/
				
				String fieldName=null;
				
				if(name == null){
				 	fieldName=path;
				}else{
					fieldName=name;
				} 
				
			
				
				String regionalVisibility=(String)request.getAttribute(fieldName+"_regionalVisibility");
				String mandatory=(String)request.getAttribute(fieldName+"_mandatoryMode");
				String viewMode=(String)request.getAttribute(fieldName+"_viewMode");
				String labelKey=(String)request.getAttribute(fieldName+"_label");
				String placeHolderKey=(String)request.getAttribute(fieldName+"_placeHolderKey");
				String tooltipKey=(String)request.getAttribute(fieldName+"_toolTipKey");	
				
				if(mandatory !=null && mandatory != "" && mandatory.equals("true")){
					jspContext.setAttribute("mandatory",mandatory);					
				}else if(mandatory !=null && mandatory != "" && mandatory.equals("false")){
					jspContext.setAttribute("mandatory","");
				}		 
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
				if(path!=null && pathPrepender!= null){	
					StringBuilder appendedPath=new StringBuilder();
					appendedPath.append(pathPrepender).append(".").append(name);					
					jspContext.setAttribute("path",appendedPath);
					
					
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
                <c:if test="${fn:trim(conditionParams[0]) eq 'maxLength'}">
                    <c:set var = "maxLength" value = "${fn:trim(conditionParams[1])}" scope="page" />
                </c:if>
            </c:forEach>
        </c:if>
    </c:forEach>
</c:if>

 
 
<c:if test="${regionalVisibility eq true}">
<%-- <c:if test="${(not empty items) and (not empty itemLabel) and (not empty itemValue) and (fn:length(items) gt 10)}">
     	<c:set var="pagination" value="true" scope="page" />
     	<c:set var="pageSize" value="10" scope="page" />
</c:if>
 --%>
 <c:choose>
	<c:when test="${alignment eq 'rtl'}">
	<c:set var="alignmentClass" value="chosen-rtl" scope="page" />

	</c:when>

	<c:otherwise>
		<c:set var="alignmentClass" value="" scope="page" />
	
	</c:otherwise>
</c:choose>
 
<c:if test="${not empty value}">
	<c:set var="defaultValue" value="${value}" scope="page" />
	
</c:if>
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

<c:if test="${empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="label.select.one"></spring:message>
	</c:set>
</c:if>


<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
			${validators} required
		</c:set>
</c:if>


<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>

<c:if test="${empty colSpan}">
	<c:set var="colSpan" value="12"	scope="page" />
</c:if>


<c:set var="selectBoxSpanClass" value="col-sm-10" scope="page" />
<c:if test="${not empty selectBoxColSpan}">
	<c:set var="selectBoxSpanClass" value="col-sm-${selectBoxColSpan}"
		scope="page" />
</c:if>
<c:if test="${not empty tooltipKey}">
	<c:set var="tooltipMessage" scope="page">
		<spring:message code="${tooltipKey}"></spring:message>
	</c:set>
</c:if>
</c:if>


<c:set var="spanClass" value="col-sm-${colSpan}" scope="page" />

<div id="<c:out value='${id}'/>-control-group"
	class="cascade-select select-ctrl TagSelectBox form-group input-group input-group <c:out value='${spanClass}' /> ${nonMandatoryClass}">
	<c:if test="${not empty labelKey}">
		<label><strong><spring:message code="${labelKey}"></spring:message>
			<c:if test="${not empty mandatory}">
				<span class='color-red'>*</span>
			</c:if> </strong></label>
	</c:if>
	<c:choose>
		<c:when test="${not empty path}">
			<spring:bind path="${path}">
				<c:set var="preEvalValue" value="${status.value}"></c:set>
			</spring:bind>
			<form:select id="${id }" parentId="${parentId }" popurl="${url}"
				path="${path}" disabled="${disabled}" name="${name}"
				value="${defaultValue}"				
				cssClass="form-control ${selectBoxSpanClass}  chosen_a ${validators} cascade_tag ${alignmentClass}"
				data-placeholder="${placeHolderMessage}"
				data-original-title="${tooltipMessage}">
										<form:option value=""></form:option>
				
				<c:choose>
					<c:when test="${not empty items}">
						<c:if test="${pagination ne true}">
						<c:forEach items="${items}" var="item">
							<form:option value="${item[itemValue]}"
								data-code="${item[itemCode]}"><c:out value='${item[itemLabel]}' /></form:option>
						</c:forEach>
						</c:if>
						<c:if test="${pagination eq true}">
							<c:forEach var="i" begin="0" end="${pageSize-1}">
								<c:set var="item" value="${items[i]}"></c:set>
								<form:option value="${item[itemValue]}"
									data-code="${item[itemCode]}">
									<c:out value='${item[itemLabel]}' />
								</form:option>
							</c:forEach>
						</c:if>						
					</c:when>
					<c:when test="${not empty itemsMap}">
						<form:options items="${itemsMap}" />
					</c:when>
				</c:choose>
			</form:select>
		</c:when>
		<c:when test="${not empty name}">
			<c:if test="${not empty disabled }">
						<select id="<c:out value='${id}' />" parentId="<c:out value='${parentId }'/>" popurl="<c:out value='${url}'/>"
								value="<c:out value='${defaultValue}' />" disabled="<c:out value='${disabled}'/>" name="<c:out value='${name}'/>"
								class="form-control <c:out value='${selectBoxSpanClass}' />  chosen_a ${validators} cascade_tag ${alignmentClass}"
								data-original-title="${tooltipMessage}"
								data-placeholder="${placeHolderMessage}">
															<option value=""></option>
								

					<c:choose>
						<c:when test="${not empty items}">
							<c:if test="${pagination ne true}">
							<c:forEach items="${items}" var="item">
								<c:choose>
									<c:when test="${not empty value && item[itemValue]==value }">
										<option selected="selected" value="<c:out value='${item[itemValue]}'/>"
											data-code="<c:out value='${item[itemCode]}'/>"><c:out value='${item[itemLabel]}' /></option>
									</c:when>
									<c:otherwise>
										<option value="<c:out value='${item[itemValue]}'/>"
											data-code="<c:out value='${item[itemCode]}'/>"><c:out value='${item[itemLabel]}' /></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
							</c:if>
							<c:if test="${pagination eq true}">
								<c:forEach var="i" begin="0" end="${pageSize-1}">
									<c:set var="item" value="${items[i]}"></c:set>
									<c:choose>
										<c:when test="${not empty value && item[itemValue]==value }">
											<option selected="selected"
												value="<c:out value='${item[itemValue]}'/>"
												data-code="<c:out value='${item[itemCode]}'/>"><c:out
													value='${item[itemLabel]}' /></option>
										</c:when>
										<c:otherwise>
											<option value="<c:out value='${item[itemValue]}'/>"
												data-code="<c:out value='${item[itemCode]}'/>"><c:out
													value='${item[itemLabel]}' /></option>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</c:if>
						</c:when>
						<c:when test="${not empty itemsMap}">
							<c:forEach items="${itemsMap}" var="item">
								<option value="<c:out value='${item.key}'/>"><c:out value='${item.value}'/></option>
							</c:forEach>
						</c:when>
					</c:choose>
				</select>
			</c:if>
			<c:if test="${empty disabled }">
						<select id="<c:out value='${id}' />" parentId="<c:out value='${parentId }'/>" popurl="<c:out value='${url}'/>"
							value="<c:out value='${defaultValue}' />" name="<c:out value='${name}'/>"
							data-placeholder="${placeHolderMessage}"
							class="form-control <c:out value='${selectBoxSpanClass}' />  chosen_a ${validators} cascade_tag ${alignmentClass} "						
							data-original-title="${tooltipMessage}">
							<option value=""></option>
					<c:choose>
						<c:when test="${not empty items}">
							
							<c:if test="${pagination ne true}">
							<c:forEach items="${items}" var="item">
								<c:choose>
									<c:when test="${not empty value && item[itemValue]==value }">
										<option selected="selected" value="<c:out value='${item[itemValue]}'/>"
											data-code="<c:out value='${item[itemCode]}'/>"><c:out value='${item[itemLabel]}' /></option>
									</c:when>
									<c:otherwise>
										<option value="<c:out value='${item[itemValue]}'/>"
											data-code="<c:out value='${item[itemCode]}'/>"><c:out value='${item[itemLabel]}' /></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
							</c:if>
							<c:if test="${pagination eq true}">
								<c:forEach var="i" begin="0" end="${pageSize-1}">
									<c:set var="item" value="${items[i]}"></c:set>
									<c:choose>
										<c:when test="${not empty value && item[itemValue]==value }">
											<option selected="selected"
												value="<c:out value='${item[itemValue]}'/>"
												data-code="<c:out value='${item[itemCode]}'/>"><c:out
													value='${item[itemLabel]}' /></option>
										</c:when>
										<c:otherwise>
											<option value="<c:out value='${item[itemValue]}'/>"
												data-code="<c:out value='${item[itemCode]}'/>"><c:out
													value='${item[itemLabel]}' /></option>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</c:if>							
						</c:when>
						<c:when test="${not empty itemsMap}">
							<c:forEach items="${itemsMap}" var="item">
								<option value="<c:out value='${item.key}'/>"><c:out value='${item.value}'/></option>
							</c:forEach>
						</c:when>
					</c:choose>
				</select>
			</c:if>
		</c:when>
	</c:choose>

	<c:if test="${not empty helpKey}">
		<span class="help-block"><spring:message code="${helpKey}" /></span>
	</c:if>

	<p class="text-danger">
		<c:if test="${not empty errorPath}">
			<form:errors path="${errorPath}" />
		</c:if>
	</p>

	<c:if test="${not empty messageKey}">
		<p class="text-info">
			<spring:message code="${messageKey}" />
		</p>
	</c:if>
</div>


<!-- Client side pagination logic -->

<c:if test="${pagination eq true}">
	<c:set var="itmVal" value="'["></c:set>
	<c:set var="itmCode" value="'["></c:set>
	<c:set var="itmLabel" value="'["></c:set>
	<c:set var="firstItm" value="true"></c:set>
	<c:set var="separator" value=""></c:set>
	<c:set var="dblQt" value='"'></c:set>
	<c:if test="${not empty path}">
		<spring:bind path="${path}">
			<c:set var="selectedValue" value="${status.value}"></c:set>
		</spring:bind>
	</c:if>
	<c:if test="${not empty value}">
		<c:set var="selectedValue" value="${value}"></c:set>
	</c:if>
	<c:forEach items="${items}" var="item" varStatus="stat">
		<c:if test="${item[itemValue] eq selectedValue}">
			<c:set var="selectedValueIndex" value="${stat.index}"></c:set>
		</c:if>
		<c:set var="escapeditmVal"><c:out value='${item[itemValue]}' /></c:set>
		<c:set var="escapeditmCode"><c:out value='${item[itemCode]}' /></c:set>
		<c:set var="escapeditmLabel"><c:out value='${item[itemLabel]}' /></c:set>
		<c:set var="itmVal"
			value="${itmVal}${separator}${dblQt}${escapeditmVal}${dblQt}"></c:set>
		<c:set var="itmCode"
			value="${itmCode}${separator}${dblQt}${escapeditmCode}${dblQt}"></c:set>
		<c:set var="itmLabel"
			value="${itmLabel}${separator}${dblQt}${escapeditmLabel}${dblQt}"></c:set>
		<c:if test="${firstItm eq true}">
			<c:set var="separator" value=","></c:set>
			<c:set var="firstItm" value="false"></c:set>
		</c:if>
	</c:forEach>

	<c:set var="itmVal" value="${itmVal}]'"></c:set>
	<c:set var="itmCode" value="${itmCode}]'"></c:set>
	<c:set var="itmLabel" value="${itmLabel}]'"></c:set>

	<script>
			$("#"+escapeSpecialCharactersInId("${id}")).data("pg_itmVal",JSON.parse(${itmVal}));
			$("#"+escapeSpecialCharactersInId("${id}")).data("pg_itmCode",JSON.parse(${itmCode}));
			$("#"+escapeSpecialCharactersInId("${id}")).data("pg_itmLabel",JSON.parse(${itmLabel}));
			$("#"+escapeSpecialCharactersInId("${id}")).data("pageSize",${pageSize});
			$("#"+escapeSpecialCharactersInId("${id}")).data("idWithOutSplChar",replaceSplCharWithUndrScor("${id}"));
			$("#"+escapeSpecialCharactersInId("${id}")).on("chosen:ready",function() {			
				if("${pagination}"=="true")
				{		
					initPagination(escapeSpecialCharactersInId("${id}"),${pageSize},'${selectedValueIndex}');
				}
			});

</script>
</c:if>

<script>
	$(document).ready(function() {
		
		var idWithEscapeChars = "#"+escapeSpecialCharactersInId("<c:out value='${id}'/>");
		executeOnLoad([idWithEscapeChars]);
	});
</script>
<%
	String val = null;
	if (name == null) {
		val = (String) jspContext.getAttribute("preEvalValue");
	} else {
		val = (String) jspContext.getAttribute("value");
	}

	try {
		
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty()) {
			
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>


