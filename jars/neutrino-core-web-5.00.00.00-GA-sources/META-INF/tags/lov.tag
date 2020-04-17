<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>


<%@ attribute name="prefixKey"%>
<%@ attribute name="suffixKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="disabled"%>
<%@ attribute name="readOnly"%>
<%@ attribute name="maxLength"%>
<%@ attribute name="minLength"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="path"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="alignToolTip"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="inputMaskKey"%>
<%@ attribute name="validators"%>
<%@ attribute name="colSpan" required="true"%>
<%@ attribute name="enableScript"%>
<%@ attribute name="inputBoxColSpan"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="name"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="textDirection"%>
<%@ attribute name="value"%>
<%@ attribute name="textAlign"%>
<%@ attribute name="inputCase"%>
<%@ attribute name="autoComplete"%>
<%@ attribute name="hiddenPath"%>
<%@ attribute name="lovHeaderLabel"%>
<%@ attribute name="lovKey"%>
<%@ attribute name="displayValuePath"%>
<%@ attribute name="hiddenValuePath"%>
<%@ attribute name="applyAll"%>
<%@ attribute name="allLabelKey"%>
<%@ attribute name="isAll"%>
<%@ attribute name="preValidationMethod" %>
<%@ attribute name="filterDataMethod" %>
<%@ attribute name="validationFailureMethod" %>
<%@ attribute name="postExecutionScript" %>
<%@ attribute name="showTextAreaInViewMode" %>
<%@ attribute name="valueInViewMode" %>
<%@ attribute name="inputBoxDivColSpan"%>
<%@ attribute name="showModalAlongWithValue"%>

<style type="text/css">
 a span.input-group-addon {
    border-bottom-right-radius: 4px;
    border-top-right-radius: 4px;
    border-right: 1px solid #ccc!important;
 }
</style>

<c:set var="inputCaseVar" value="" scope="page" />
<c:if test="${not empty inputCase}">
	<c:if test="${inputCase eq 'U'}">
		<c:set var="inputCaseVar" value="inputCaseUpper" scope="page" />
	</c:if>
	<c:if test="${inputCase eq 'L'}">
		<c:set var="inputCaseVar" value="inputCaseLower" scope="page" />
	</c:if>
</c:if>

<c:set var="colSpanClass" value="" scope="page" />
<c:if test="${not empty colSpan}">
	<c:set var="colSpanClass" value="col-sm-${colSpan}" scope="page" />
</c:if>

<c:set var="scriptTagFlag" value="true" scope="page" />
<c:if test="${not empty enableScript}">
	<c:set var="scriptTagFlag" value="${enableScript}" scope="page" />
</c:if>
<c:if test="${not empty viewMode}">
	<c:if test="${viewMode eq true}">
		<c:set var="disabled" value="${viewMode}" scope="page" />
		<c:set var="placeHolderKey" value="" scope="page" />
		<c:set var="tooltipKey" value="" scope="page" />
		<c:set var="validators" value="" scope="page" />
	</c:if>
</c:if>
<c:if test="${empty disabled}">
	<c:set var="disabled" value="false" scope="page" />
</c:if>

<c:set var="inputBoxSpanClass" value="col-sm-10" scope="page" />
<c:if test="${not empty inputBoxColSpan}">
	<c:set var="inputBoxSpanClass" value="col-sm-${inputBoxColSpan}"
		scope="page" />
</c:if>

<c:set var="inputBoxDivSpanClass" value="col-sm-6" scope="page" />
<c:if test="${not empty inputBoxDivColSpan}">
	<c:set var="inputBoxDivSpanClass" value="col-sm-${inputBoxDivColSpan}"
		scope="page" />
</c:if>

<c:set var="inputMaxLength" value="255" scope="page" />
<c:if test="${maxLength ge 0}">
	<c:set var="inputMaxLength" value="${maxLength}" scope="page" />
</c:if>

<c:set var="shownModalEvenWithValue" value="false" scope="page" />
<c:if test="${showModalAlongWithValue eq true}">
  <c:set var="shownModalEvenWithValue" value="true" scope="page" />
</c:if>

<c:if test="${maxLength ge 0}">
	<c:set var="inputMinLength" value="${minLength}" scope="page" />
</c:if>

<c:if test="${not empty mandatory and mandatory eq true}">
	<c:set var="validators" scope="page">
			${validators} required
		</c:set>
</c:if>

<c:if test="${empty mandatory or mandatory eq false}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>

<c:if test="${not empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}" />
	</c:set>

</c:if>

<c:if test="${not empty inputMaskKey}">
	<c:set var="inputMask" scope="page">
		<spring:message code="${inputMaskKey}"></spring:message>
	</c:set>
</c:if>

<c:if test="${not empty tooltipKey}">
	<c:set var="tooltipMessage" scope="page">
		<spring:message code="${tooltipKey}"></spring:message>
	</c:set>
</c:if>

<c:if test="${empty textDirection}">
	<c:set var="textDirection" value="ltr" scope="page" />
</c:if>
<c:if test="${not empty textDirection}">
	<c:set var="textDirection" value="${textDirection}" scope="page" />
</c:if>
<c:if test="${empty showTextAreaInViewMode}">
	<c:set var="showTextAreaInViewMode" value="false" scope="page" />
</c:if>
<c:if test="${not empty valueInViewMode}">
	<c:set var="valueInViewMode" value="${valueInViewMode}" scope="page" />
</c:if>

<c:set var="imgId" value="${id}_image" scope="page" />
<c:set var="imgClass" value="glyphicon glyphicon-search" scope="page" />

<c:set var="postExecutionParam" value="null" scope="page" />
<c:choose>
	<c:when test="${empty postExecutionScript}">
		<c:set var="postExecutionScript" value="null" scope="page" />
	</c:when>
	<c:otherwise>
		<c:if test="${fn:contains(postExecutionScript, '(') }" >
			<c:set var="postExecuteMethod" value="${fn:split(postExecutionScript, '(')}" />
			<c:set var="postExecutionScript" value="${postExecuteMethod[0]}" />
			<c:if test="${not empty postExecuteMethod[1] }">
				<c:set var="postExecutionParam" value="${fn:substringBefore(postExecuteMethod[1], ')' )}" />
			</c:if>
		</c:if>
	</c:otherwise>
</c:choose>

<div id="${id}-control-group" class="form-group input-group ${colSpanClass}  ${nonMandatoryClass}">

	<c:if test="${not empty labelKey}">
		<label><strong><spring:message code="${labelKey}"></spring:message></strong>
			<c:if test="${not empty mandatory and mandatory eq true}">
				<span class="color-red">*</span>
			</c:if> </label>
	</c:if>

	<c:if test="${not empty prefixKey}">
		<span class="input-group-addon"><spring:message code="${prefixKey}" /></span>
	</c:if>

	<%
	    String name = (String) jspContext.getAttribute("name");
				String path = (String) jspContext.getAttribute("path");
				String hiddenPath = (String) jspContext.getAttribute("hiddenPath");
				if (name == null && path == null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' must be specified");
				} else if (name != null && path != null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' can be specified at once");
				}
	%>

	<c:if test="${not empty name}">
    <div class="col-sm-12">
		  <c:choose>
			  <c:when test="${viewMode eq true}">
			    <c:choose>
				    <c:when test="${showTextAreaInViewMode eq true}">
				      <div class="${inputBoxDivSpanClass}">
				        <textarea name="${name}" id="${id}" style="font-size:14px;"
				        	placeholder="${placeHolderMessage}" readonly="${readOnly}" disabled="${disabled}"
				        	data-original-title="${tooltipMessage}"
				        	rows="3" data-maxlength="${inputMaxLength}"
				        	class="form-control ${validators} ${inputBoxSpanClass} txtarea_limit_words txtarea_limit_chars neutrino_textarea validTextLength "
				        	tabindex="${tabindex}">${valueInViewMode}</textarea>
				       </div>
				     </c:when>
				     <c:otherwise>
				       <div class="${inputBoxDivSpanClass}">
				         <input type="text" name="${name}"
				        	 class="form-control inputmask ${validators} ${inputBoxSpanClass}" id="${id}"
				        	 maxlength="${inputMaxLength}" minlength="${inputMinLength}" placeholder="${placeHolderMessage}"
				        	 disabled="${disabled}" readonly="${readOnly}" value="${value}"
				        	 data-mask="${inputMask}" data-original-title="${tooltipMessage}"
				        	 dir="${textDirection}" style="text-align:${textAlign};" tabindex="${tabindex}">
				       </div>
				       <div class="col-sm-1">
				         <a id="${id}_image" tabindex="-1" href="#"> <span class="input-group-addon input-sm" style="font-size: 15px;"><i class="glyphicon glyphicon-search"></i></span></a>
				      	 <span for="${id}" generated="true" class="" style=""></span>
				       </div>
				     </c:otherwise>
		  	   </c:choose>
		  	   <c:if test="${not empty applyAll && applyAll eq true}">
				     <div class="col-sm-3">
				  	   <neutrino:checkBox id="${id}_applyAllCheck" path=""  name="next" value="" labelKey="${allLabelKey}" tooltipKey="ttp.lbl.next" viewMode="true" errorPath="" />
				  	 </div>
			     </c:if>
			  </c:when>
			  <c:otherwise>
			  	<c:choose>
			  	 <c:when test="${not empty readOnly}">
			  	   <div class="${inputBoxDivSpanClass}">
			  			  <input type="text" name="${name}"
			  				  class="form-control inputmask ${validators} ${inputBoxSpanClass}" id="${id}"
			  				  maxlength="${inputMaxLength}" minlength="${inputMinLength}"
			  				  placeholder="${placeHolderMessage}" data-mask="${inputMask}"
			  				  value="${value}" readonly="${readOnly}"
			  				  data-original-title="${tooltipMessage}" dir="${textDirection}"
			  				  style="text-align:${textAlign};" tabindex="${tabindex}">
			  	   </div>
			  	 </c:when>
			  	 <c:otherwise>
			  	   <div class="${inputBoxDivSpanClass}">
			  		   <input type="text" name="${name}"
			  		     class="form-control inputmask ${validators} ${inputBoxSpanClass}" id="${id}"
			  		     maxlength="${inputMaxLength}" minlength="${inputMinLength}"
			  		     placeholder="${placeHolderMessage}" data-mask="${inputMask}"
			  		     value="${value}" data-original-title="${tooltipMessage}"
			  		     dir="${textDirection}" style="text-align:${textAlign};"
			  		     tabindex="${tabindex}">
			  	   </div>
			  	 </c:otherwise>
			   </c:choose>
			   <div class="col-sm-1">
			  	  <a id="${id}_image" href="#"> <span class="input-group-addon input-sm" style="font-size: 15px;"><i class="glyphicon glyphicon-search"></i></span></a>
			  	  <span for="${id}" generated="true" class="lovClass"></span>
			   </div>
			    <c:if test="${not empty applyAll && applyAll eq true}">
			      <div class="col-sm-3">
				    <neutrino:checkBox id="${id}_applyAllCheck" path="" name="next" value="" labelKey="${allLabelKey}" tooltipKey="ttp.lbl.next" viewMode="false" errorPath="" />
				  </div>
			    </c:if>
			  </c:otherwise>
		  </c:choose>
		</div>
	</c:if>
	<c:if test="${not empty path}">
		<form:input path="${path}"
			cssClass="form-control inputmask ${validators} ${inputCaseVar} ${inputBoxSpanClass}"
			id="${id}" maxlength="${inputMaxLength}" minlength="${inputMinLength}"
			placeholder="${placeHolderMessage}" disabled="${disabled}"
			readonly="${readOnly}" tabindex="${tabindex}"
			data-mask="${inputMask}" data-original-title="${tooltipMessage}"			dir="${textDirection}" style="text-align:${textAlign};"
			autocomplete="${autoComplete}"></form:input>
		<c:if test="${not empty imgClass}">
			<c:if test="${not empty imgId}">
				<a id="${imgId}" href="#"> <span class="input-group-addon input-sm" style="font-size: 15px;"><i
						class="${imgClass}"></i></span>
				</a>
				<span for="${id}" generated="true" class="help-block" style=""></span>
			</c:if>
		</c:if>
	</c:if>
	<input type="hidden" id="${id}_apply" value = '<spring:message code="${allLabelKey}" />' name="hiddenField2"/>
	<input type="hidden" id="${id}_postExecution" name="hiddenField3" value = "${postExecutionScript}"/>
	<c:choose>
	<c:when test="${not empty hiddenPath}">
	<form:input  cssClass="form-control " type="hidden" id="${id}_hidden" path="${hiddenPath}"/>
	</c:when>
	<c:otherwise>
	<input type="hidden" id="${id}_hidden" name="hiddenField"/>

	</c:otherwise>

	</c:choose>
	 <input type="hidden" id="${id}_hidden_lovSelectedDataObject" name="${id}_hidden_lovSelectedDataObject"/>

	<c:if test="${not empty suffixKey}">
		<span class="input-group-addon"><spring:message code="${suffixKey}" /></span>
	</c:if>

	<c:if test="${not empty helpKey}">
		<span class="help-block"><spring:message code="${helpKey}" /></span>
	</c:if>

	<c:if test="${not empty messageKey}">
		<p class="text-info">
			<spring:message code="${messageKey}" />
		</p>
	</c:if>

	<c:if test="${not empty errorPath}">
		<p class="text-danger">
			<form:errors path="${errorPath}" />
		</p>
	</c:if>


</div>

		<div id="${id}_lovModal" class="modal  fade container  txt-l"	style="display: none">
			<div id="lov_modal_header" class="modal-header">
				<div class="row">
					<div class="pull-left" class="col-sm-6">
				<h2>
					  <spring:message code="${lovHeaderLabel}" />
				</h2>
					</div>
					<div class="pull-right">
			     	     <a id="${id}_lovCloseButton" class="close" onClick="hideLov(true, ${postExecutionScript}, ${postExecutionParam} );" href="#">X</a>
				 	</div>
				</div>
			</div>
				<div id="lov_modal_body" class="modal-body">
					<div class="row" >

						<div class="col-sm-12">
							<div id="${id}_lovBody" class="pull-left col-sm-12" ></div>
						</div>
					</div>
				</div>
	<div id="lov_modal_footer" class="pull-right modal-footer clearfix" >
       <neutrino:button id="${id}_OK" enablehtml5="true"
                     buttonType="button" cssClass="btn btn-primary"
                     onClickEvent="checkandhideLov(${postExecutionScript}, ${postExecutionParam})" valueKey="lbl.ok"
                     displayIcon="glyphicon glyphicon-ok  glyphicon glyphicon-large" />
      <neutrino:button id="${id}_Cancel" enablehtml5="true"
                     buttonType="button" cssClass="btn secondary"
                     onClickEvent="hideLov(true, ${postExecutionScript}, ${postExecutionParam})" valueKey="lbl.cancel"
                     displayIcon="glyphicon glyphicon-remove glyphicon glyphicon-large" />

	</div>
</div>
<c:if test="${scriptTagFlag == 'true'}" >
<script>
	var alignDir = '${alignToolTip}';
	jQuery(document).ready(
			function() {
				var vw = '${viewMode}';
		 		if(vw == 'true') {
					jQuery("#${id}"+"_image").children().css('pointer-events','none');
                    jQuery("#${id}"+"_image").css('pointer-events','none');
				}

		 		applyTooltip('${id}', '${alignToolTip}');

				$(document).off('shown','${id}-control-group .tooltip').on('shown','${id}-control-group .tooltip',function(){
					executeOnLiveSelected('${id}-control-group');
				});
				jQuery('p.text-danger').css("font-size","15px");
	});

	applyClickOnLOVSearchImage('${id}',
								<c:choose><c:when test="${not empty preValidationMethod}">${preValidationMethod}</c:when><c:otherwise>null</c:otherwise></c:choose>,
								<c:choose><c:when test="${not empty validationFailureMethod}">${validationFailureMethod}</c:when><c:otherwise>null</c:otherwise></c:choose>,
								<c:choose><c:when test="${not empty filterDataMethod}">${filterDataMethod}</c:when><c:otherwise>null</c:otherwise></c:choose>,
								<c:choose><c:when test="${not empty postExecutionScript}">'${postExecutionScript}'</c:when><c:otherwise>null</c:otherwise></c:choose>,
								'${lovKey}',
								'${id}_hidden',
                '${shownModalEvenWithValue}');

  applyChangeOnLOVSearchImage('${id}',
								<c:choose><c:when test="${not empty preValidationMethod}">${preValidationMethod}</c:when><c:otherwise>null</c:otherwise></c:choose>,
								<c:choose><c:when test="${not empty validationFailureMethod}">${validationFailureMethod}</c:when><c:otherwise>null</c:otherwise></c:choose>,
								<c:choose><c:when test="${not empty filterDataMethod}">${filterDataMethod}</c:when><c:otherwise>null</c:otherwise></c:choose>,
								<c:choose><c:when test="${not empty postExecutionScript}">'${postExecutionScript}'</c:when><c:otherwise>null</c:otherwise></c:choose>,
								'${lovKey}',
								'${id}_hidden');

	setDisplayValue('${displayValuePath}','${id}');
	setHiddenValue('${hiddenValuePath}','${id}_hidden');
	callForApplyAllCheck('${isAll}','${id}','${id}_hidden','<spring:message code="${allLabelKey}" />');

	function executeOnLiveSelected(currentElementId) {
		var toolTipDivElement = jQuery('#'+currentElementId).find('.tooltip');
		if(typeof jQuery(toolTipDivElement)[0]!='undefined'){
			var toolTipLeftMargin = parseInt(jQuery(toolTipDivElement)[0].style.left)+30;
			jQuery(toolTipDivElement).css("left",toolTipLeftMargin+'px');
			jQuery(toolTipDivElement)[0].style.left = toolTipLeftMargin+'px';
		}
	}
</script>
</c:if>