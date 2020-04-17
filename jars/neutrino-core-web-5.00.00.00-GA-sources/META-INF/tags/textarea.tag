<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="disabled"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="maxLength" required="true"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="path"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="alignToolTip"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="validators"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="name"%>
<%@ attribute name="value"%>
<%@ attribute name="rows"%>
<%@ attribute name="resize"%>
<%@ attribute name="words"%>
<%@ attribute name="character"%>
<%@ attribute name="colSpan" required="true"%>
<%@ attribute name="textareaBoxColSpan"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="labelDynamicForm"%>
<%@ attribute name="dynamicFormToolTip"%>
<%@ attribute name="isRegional"%>
<%@ attribute name="pathPrepender" %>
<%@ attribute name="isSpellCheckEnabled"%>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="onfocusout"%>
<%@ attribute name="conditionStatement"%>
<%@ attribute name="conditionValue"%>
<%@ attribute name="maskedValue"%>
<%@ attribute name="maskedPath"%>

<%
    String name = (String) jspContext.getAttribute("name");
    String path = (String) jspContext.getAttribute("path");

    if (name == null && path == null) {
        throw new SystemException("Either of attributes 'name' or 'path' must be specified");
    } else if (name != null && path != null) {
        throw new SystemException("Either of attributes 'name' or 'path' can be specified at once");
    }
    
    String fieldName=null;
	
	if(name == null){
	 	fieldName=path;
	}else{
		fieldName=name;
	} 
	
	String mandatory=null;
	String viewMode=null;
	String labelKey=null;
	String placeHolderKey=null;
	String tooltipKey=null;
	
	String isRegional = (String) jspContext.getAttribute("isRegional");
	String regionalVisibility=(String)request.getAttribute(fieldName+"_regionalVisibility");
	
	if(isRegional == null ){
		 mandatory=(String)request.getAttribute(fieldName+"_mandatoryMode");
		 viewMode=(String)request.getAttribute(fieldName+"_viewMode");
		 labelKey=(String)request.getAttribute(fieldName+"_label");
		 placeHolderKey=(String)request.getAttribute(fieldName+"_placeHolderKey");
		 tooltipKey=(String)request.getAttribute(fieldName+"_toolTipKey");	
	}

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
<c:if test="${regionalVisibility eq true}">

<c:set var="colSpanClass" value="" scope="page" />
<c:if test="${not empty colSpan}">
	<c:set var="colSpanClass" value="col-sm-${colSpan}" scope="page" />
</c:if>

<c:set var="textareaBoxSpanClass" value="col-sm-10" scope="page" />
<c:if test="${not empty textareaBoxColSpan}">
	<c:set var="textareaBoxSpanClass" value="col-sm-${textareaBoxColSpan}"
		scope="page" />
</c:if>

<c:if test="${not empty viewMode}">
	<c:if test="${viewMode eq true}">
		<c:set var="disabled" value="${viewMode}" scope="page" />
		<c:set var="placeHolderKey" value="" scope="page" />
		<c:set var="tooltipKey" value="" scope="page" />
		<c:set var="validators" value="" scope="page" />
	</c:if>
</c:if>


<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
			${validators} required
		</c:set>
</c:if>

<c:if test="${not empty words}">
	<c:set var="words" value="txtarea_limit_words" scope="page">
	</c:set>
</c:if>
<c:if test="${not empty viewMode}">
	<c:if test="${viewMode ne true}">
		<c:if test="${not empty character}">
			<c:set var="character" value="txtarea_limit_chars" scope="page">
			</c:set>
		</c:if>
	</c:if>
</c:if>
	<c:if test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true))}">
      <c:if test="${not empty maskedValue}">
   	<c:set var="value" value="${maskedValue}" scope="page" />
   	</c:if>
   	   <c:if test="${not empty maskedValue && not empty maskedPath}">
   	<c:set var="path" value="${maskedPath}" scope="page" />
      </c:if>
   </c:if>
<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
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

<c:set var="textAreaMaxLength" value="255" scope="page" />
<c:choose>
	<c:when test="${not empty maxLength && maxLength ge 0}">
		<c:set var="textAreaMaxLength" value="${maxLength}" />
	</c:when>
</c:choose>



<div id="<c:out value='${id}' />-control-group"
	class="tagTextArea form-group <c:out value='${colSpanClass}' /> ${nonMandatoryClass}">

	<c:if test="${not empty labelKey}">
		<label><strong><spring:message code="${labelKey}"></spring:message>
			<c:if test="${not empty mandatory}">
				<span style="color: red">*</span>
			</c:if></strong> </label>
	</c:if>

	<c:if test="${not empty labelDynamicForm}">
		<label><strong><c:out value='${labelDynamicForm}' /> <c:if
				test="${not empty mandatory}">
				<span class='color-red'>*</span>
			</c:if> </strong></label>
	</c:if>


	<c:if test="${not empty path}">
		<spring:bind path="${path}">
				<c:set var="preEvalValue" value="${status.value}"></c:set>
		</spring:bind>
		<c:if test="${resize eq 'vertical'}">
		<form:textarea path="${path}" id="${id}"
			placeholder="${placeHolderMessage}" disabled="${disabled}"
			readonly="${readOnly}" data-original-title="${tooltipMessage}"
			maxlength="${textAreaMaxLength}" rows="${rows}"  spellcheck="${isSpellCheckEnabled}"
			cssClass="form-control ${validators} ${textareaBoxSpanClass} ${words} ${character} neutrino_textarea textarea_resize"
			tabindex="${tabindex}"></form:textarea>
			</c:if>
            		<c:if test="${resize ne 'vertical'}">
            		<form:textarea path="${path}" id="${id}"
            			placeholder="${placeHolderMessage}" disabled="${disabled}"
            			readonly="${readOnly}" data-original-title="${tooltipMessage}"
            			maxlength="${textAreaMaxLength}" rows="${rows}"  spellcheck="${isSpellCheckEnabled}"
            			cssClass="form-control ${validators} ${textareaBoxSpanClass} ${words} ${character} neutrino_textarea"
            			tabindex="${tabindex}"></form:textarea>
            		</c:if>
		<div id="instDesc_counter0_<c:out value='${id}' />" class="text_count_div">
			<span id="instDesc_count1_<c:out value='${id}' />"
				class="text_descr label label-info text_count"> <c:out value='${textAreaMaxLength}' />
			</span> <i><spring:message code="label.textarea.character" /></i>
		</div>
	</c:if>

	<c:if test="${not empty name}">
		<c:choose>
			<c:when test="${viewMode eq true}">
			   <c:if test="${resize eq 'vertical'}">
				<textarea name="<c:out value='${name}' />" id="<c:out value='${id}' />"
					placeholder="${placeHolderMessage}" readonly="<c:out value='${readOnly}' />"
					data-original-title="${tooltipMessage}" maxlength="<c:out value='${textAreaMaxLength}' />"
					rows="<c:out value='${rows}' />" spellcheck="${isSpellCheckEnabled}"
					class="form-control <c:out value='${validators}' /> <c:out value='${textareaBoxSpanClass}' /> <c:out value='${words}' /> <c:out value='${character}' /> neutrino_textarea textarea_resize"
					tabindex="<c:out value='${tabindex}' />" onfocusout="<c:out value='${onfocusout}' />" ><c:out value='${value}'/></textarea>
					</c:if>
					<c:if test="${resize ne 'vertical'}">
                                    <textarea name="<c:out value='${name}' />" id="<c:out value='${id}' />"
                                        placeholder="${placeHolderMessage}" readonly="<c:out value='${readOnly}' />"
                                        data-original-title="${tooltipMessage}" maxlength="<c:out value='${textAreaMaxLength}' />"
                                        rows="<c:out value='${rows}' />" spellcheck="${isSpellCheckEnabled}"
                                        class="form-control <c:out value='${validators}' /> <c:out value='${textareaBoxSpanClass}' /> <c:out value='${words}' /> <c:out value='${character}' /> neutrino_textarea "
                                        tabindex="<c:out value='${tabindex}' />" onfocusout="<c:out value='${onfocusout}' />" ><c:out value='${value}'/></textarea>
                                        </c:if>
				<div id="instDesc_counter0_<c:out value='${id}' />" class="text_count_div">
					<span id="instDesc_count1_<c:out value='${id}' />"
						class="text_descr label label-info text_count">
						<c:out value='${textAreaMaxLength}' /> </span> <i><spring:message
							code="label.textarea.character" /></i>
				</div>

			</c:when>
			<c:otherwise>
			      <c:if test="${resize eq 'vertical'}">
					<c:choose>
						<c:when test="${not empty readOnly && readOnly eq true}">
                            <textarea name="<c:out value='${name}' />" id="<c:out value='${id}' />"
                                placeholder="${placeHolderMessage}" readonly="true"
                                data-original-title="${tooltipMessage}" maxlength="<c:out value='${textAreaMaxLength}' />"
                                rows="<c:out value='${rows}' />" spellcheck="${isSpellCheckEnabled}"
                                class="form-control <c:out value='${validators}' /> <c:out value='${textareaBoxSpanClass}' /> <c:out value='${words}' /> <c:out value='${character}' /> neutrino_textarea textarea_resize"
                                tabindex="<c:out value='${tabindex}' />" onfocusout="<c:out value='${onfocusout}' />" ><c:out value='${value}' /></textarea>
						</c:when>
						<c:when test="${not empty disabled && disabled eq true}">
                            <textarea name="<c:out value='${name}' />" id="<c:out value='${id}' />"
                                placeholder="${placeHolderMessage}" disabled="disabled"
                                data-original-title="${tooltipMessage}" maxlength="<c:out value='${textAreaMaxLength}' />"
                                rows="<c:out value='${rows}' />" spellcheck="${isSpellCheckEnabled}"
                                class="form-control <c:out value='${validators}' /> <c:out value='${textareaBoxSpanClass}' /> <c:out value='${words}' /> <c:out value='${character}' /> neutrino_textarea textarea_resize"
                                tabindex="<c:out value='${tabindex}' />" onfocusout="<c:out value='${onfocusout}' />" ><c:out value='${value}' /></textarea>
						</c:when>
						<c:otherwise>
                            <textarea name="<c:out value='${name}' />" id="<c:out value='${id}' />"
                                placeholder="${placeHolderMessage}"
                                data-original-title="${tooltipMessage}" maxlength="<c:out value='${textAreaMaxLength}' />"
                                rows="<c:out value='${rows}' />" spellcheck="${isSpellCheckEnabled}"
                                class="form-control <c:out value='${validators}' /> <c:out value='${textareaBoxSpanClass}' /> <c:out value='${words}' /> <c:out value='${character}' /> neutrino_textarea textarea_resize"
                                tabindex="<c:out value='${tabindex}' />" onfocusout="<c:out value='${onfocusout}' />" ><c:out value='${value}' /></textarea>
						</c:otherwise>
					</c:choose>
					</c:if>
					<c:if test="${resize ne 'vertical'}">
                     <c:choose>
                     						<c:when test="${not empty readOnly && readOnly eq true}">
                                                 <textarea name="<c:out value='${name}' />" id="<c:out value='${id}' />"
                                                     placeholder="${placeHolderMessage}" readonly="true"
                                                     data-original-title="${tooltipMessage}" maxlength="<c:out value='${textAreaMaxLength}' />"
                                                     rows="<c:out value='${rows}' />" spellcheck="${isSpellCheckEnabled}"
                                                     class="form-control <c:out value='${validators}' /> <c:out value='${textareaBoxSpanClass}' /> <c:out value='${words}' /> <c:out value='${character}' /> neutrino_textarea"
                                                     tabindex="<c:out value='${tabindex}' />" onfocusout="<c:out value='${onfocusout}' />" ><c:out value='${value}' /></textarea>
                     						</c:when>
                     						<c:when test="${not empty disabled && disabled eq true}">
                                                 <textarea name="<c:out value='${name}' />" id="<c:out value='${id}' />"
                                                     placeholder="${placeHolderMessage}" disabled="disabled"
                                                     data-original-title="${tooltipMessage}" maxlength="<c:out value='${textAreaMaxLength}' />"
                                                     rows="<c:out value='${rows}' />" spellcheck="${isSpellCheckEnabled}"
                                                     class="form-control <c:out value='${validators}' /> <c:out value='${textareaBoxSpanClass}' /> <c:out value='${words}' /> <c:out value='${character}' /> neutrino_textarea"
                                                     tabindex="<c:out value='${tabindex}' />" onfocusout="<c:out value='${onfocusout}' />" ><c:out value='${value}' /></textarea>
                     						</c:when>
                     						<c:otherwise>
                                                 <textarea name="<c:out value='${name}' />" id="<c:out value='${id}' />"
                                                     placeholder="${placeHolderMessage}"
                                                     data-original-title="${tooltipMessage}" maxlength="<c:out value='${textAreaMaxLength}' />"
                                                     rows="<c:out value='${rows}' />" spellcheck="${isSpellCheckEnabled}"
                                                     class="form-control <c:out value='${validators}' /> <c:out value='${textareaBoxSpanClass}' /> <c:out value='${words}' /> <c:out value='${character}' /> neutrino_textarea"
                                                     tabindex="<c:out value='${tabindex}' />" onfocusout="<c:out value='${onfocusout}' />" ><c:out value='${value}' /></textarea>
                     						</c:otherwise>
                     					</c:choose>
					</c:if>

				<div id="instDesc_counter0_<c:out value='${id}' />" class="text_count_div">
					<span id="instDesc_count1_<c:out value='${id}' />"
						class="text_descr label label-info text_count">
						<c:out value='${textAreaMaxLength}' /> </span> <i><spring:message
							code="label.textarea.character" /></i>
				</div>
			</c:otherwise>
		</c:choose>
	</c:if>

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
</div>
<script>
	applyTooltip('${id}', '${alignToolTip}');

	</script>
</c:if>

<script>
	$(document).ready(function() {
		var ids = ["#<c:out value='${id}'/>"];
		executeOnLoad(ids);
		var disabledFlag="<c:out value='${disabled}'/>";
		var readOnlyFlag="<c:out value='${readOnly}'/>";
		var hideDescCountFlag=false;
		if(disabledFlag.toLowerCase() == "true" || readOnlyFlag.toLowerCase() == "true" || readOnlyFlag.toLowerCase() =="readonly" ){
			hideDescCountFlag=true;
		}
		var objId="<c:out value='${id}'/>";
		hideUnhideDescCount(objId,hideDescCountFlag);
		addEventsForNonPrintingCharactersCheck(objId);
	  setTimeout(function(){
		$("textarea[id^='${id}']").data("initial-height",$("textarea[id^='${id}']").height());
		$("textarea[id^='${id}']").focusout(function(e){
            if($(this).css("resize") == 'vertical'){
            	
                var initialHeight = $(this).data("initial-height");
                if(initialHeight!=undefined){
                    $(this).height(initialHeight);
                    $(this).css({minHeight:'60px'});
                }
              }
        });
        },500);
	})
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

