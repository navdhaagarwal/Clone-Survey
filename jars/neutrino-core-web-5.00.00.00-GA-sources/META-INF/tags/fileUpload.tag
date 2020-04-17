<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
	prefix="neutrino"%>
<%@ attribute name="disabled"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="remarkMandatory"%>
<%@ attribute name="uploadEvent" required="true"%>
<c:if test="${(empty remarkMandatory) or (remarkMandatory != 'true')}">
	<c:set var="remarkMandatory" scope="page" value="false"/>
</c:if>
<c:if test="${(empty disabled) or (disabled != 'true')}">
	<c:set var="disabled" scope="page" value="false">
	</c:set>
</c:if>
<form id="form_${id}" class="drag_drop_remarks_form">
<span id="dragDrop_${id}" class="hidden"><spring:message
		code="label.drag.drop.files.here" /></span>
				<c:choose>
						<c:when test="${alignment eq 'rtl'}">
							<div class="row m-t5" id='browsediv_${id}'>
						</c:when>
						<c:otherwise>
							<div class="row" id='browsediv_${id}'>
						</c:otherwise>
				</c:choose>
	<div id="dragandrophandler_${id}"
		class="col-sm-5 m-b10 txt-c dragandrophandler hgt90">
		<spring:message code="label.drag.drop.files.here" />
	</div>
	<div class="col-sm-2 hgt90 p-t20 p-l20">
		<neutrino:button id='browse_${id}' valueKey="label.browse"
			buttonType="button" cssClass="btn btn-primary"
			onClickEvent="$('#commonsMultipartFile_${id}').click();" />
	</div>
	<div class='col-sm-5 hgt90'>
				<c:choose>
						<c:when test="${alignment eq 'rtl'}">
							<div class='hgt90 left-border col-sm-8'>
						</c:when>
						<c:otherwise>
							<div class='hgt90 right-border col-sm-8'>
						</c:otherwise>
				</c:choose>

			<c:choose>
				<c:when test="${(empty remarkMandatory) or (remarkMandatory != 'true')}">
					<neutrino:textarea id="remark_${id}"
						name="remark_${id}" colSpan="12" maxLength="200"
						labelKey="label.remarks" onfocusout="onFocusOutListener()" />
				</c:when>
				<c:otherwise>
					<neutrino:textarea id="remark_${id}" mandatory="true"
						name="remark_${id}" colSpan="12" maxLength="200"
						labelKey="label.remarks" onfocusout="onFocusOutListener()" />
				</c:otherwise>
			</c:choose>
			<!-- textarea here -->
		</div>
		<div class='col-sm-4 hgt90 p-t20 m-t20 txt-c'>
			<neutrino:button id='upload_${id}' valueKey="label.upload"
				buttonType="button" cssClass="btn btn-primary"
				onClickEvent="onClickEventWapper()" />
		</div>
	</div>
</div>
<input id="commonsMultipartFile_${id}" name="commonsMultipartFile"
	style='display: none;' type="file" />
<input type="hidden" name="dbId_${id}" id="dbId_${id}" />
</form>
<script>
var fileUploadTagScriptInput = {};
(function(){
	fileUploadTagScriptInput = {
			 id_fileUpload : "${id}",
			 disabled_fileUpload : "${disabled}",
		     remarkMandatory:"${remarkMandatory}",
			 uploadEvent:"${uploadEvent}"		     
	}
	//fileUploadTagScript(fileUploadTagScriptInput);
	

})();
var fileUploadTagScript_Instance = 	new fileUploadTagScript(fileUploadTagScriptInput);
var initialiseDropBox2 = fileUploadTagScript_Instance.initialiseDropBox2;
var onClickEventWapper = fileUploadTagScript_Instance.onClickEventWapper;
var onFocusOutListener = fileUploadTagScript_Instance.onFocusOutListener;

</script>

