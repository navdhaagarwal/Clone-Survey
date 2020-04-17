<%@ tag language="java" pageEncoding="ISO-8859-1"
	description="Creates a HTML table with jQuery and jQuery DataTables plugin."%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<script>
  setMessage('lbl.error','<spring:message code="lbl.error"/>');
  setMessage('msg.00002518','<spring:message code="msg.00002518"/>');
  setMessage('label.copy',"<spring:message code='label.copy' />");
  setMessage('label.csv',"<spring:message code='label.csv' />");
  setMessage('label.xls',"<spring:message code='label.xls' />");
  setMessage('label.pdf',"<spring:message code='label.pdf' />");
  setMessage('label.finnOne.product',"<spring:message code='label.finnOne.product' />");
  setMessage('label.show.hide.columns',"<spring:message code='label.show.hide.columns' />");
  setMessage('label.previous.page',"<spring:message code='label.previous.page' />");
  setMessage('label.next.page',"<spring:message code='label.next.page' />");
  setMessage('label.no.records.found',"<spring:message code='label.no.records.found' />");
  setMessage('label.processing',"<spring:message code='label.processing' />");
  setMessage('label.datatable.info',"<spring:message code='label.datatable.info' />");
  setMessage('label.search',"<spring:message code='label.search' />");
  setMessage('label.show.entries',"<spring:message code='label.show.entries' />");
  setMessage('lbl.search',"<spring:message code='lbl.search' />");
  setMessage('lms.datatable.info',"<spring:message code='lms.datatable.info' />");
</script>
<%@ attribute name="htmlTableId" required="true" type="java.lang.String"
	description=""%>
<%@ attribute name="loadDataUrl" required="true" type="java.lang.String"
	description=""%>
<%@ attribute name="defaultCheckBox" required="true"
	type="java.lang.String" description=""%>
<%@ attribute name="dateformat" type="java.lang.String" description=""%>
<%@ attribute name="footer" type="java.lang.String" description=""%>
<%@ attribute name="tools" type="java.lang.String" description=""%>
<%@ attribute name="pdfDownload" type="java.lang.String"
	description="Download data in pdf format"%>
<%@ attribute name="csvDownload" type="java.lang.String"
	description="Download data in csv format"%>
<%@ attribute name="xlsDownload" type="java.lang.String"
	description="Download data in xls format"%>
<%@ attribute name="copyDownload" type="java.lang.String"
	description="Copy table content"%>
<%@ attribute name="defaultLoad" type="java.lang.String" description=""%>
<%@ attribute name="method" type="java.lang.String" description=""%>
<%@ attribute name="serialNumber" type="java.lang.String" description=""%>
<%@ attribute name="titleName" type="java.lang.String" description=""%>
<%@ attribute name="serverSide" type="java.lang.String" description=""%>
<%@ attribute name="fnRowCallback" type="java.lang.String"
	description=""%>
<%@ attribute name="preAjaxParamCallback" type="java.lang.String" description=""%>
<%@ attribute name="fnInitComplete" type="java.lang.String"
	description=""%>
<%@ attribute name="fnFooterCallback" type="java.lang.String"
	description=""%>
<%@ attribute name="fnDrawCallback" type="java.lang.String"
	description=""%>
<%@ attribute name="radioGroup" type="java.lang.String" description=""%>
<%@ attribute name="paginate" type="java.lang.String" description=""%>
<%@ attribute name="currencyProp" type="java.lang.String" description=""%>
<%@ attribute name="blankKey" type="java.lang.String" description=""%>
<%@ attribute name="blankPopUp" type="java.lang.String" description=""%>
<%@ attribute name="focusField" type="java.lang.String"
	description="Field on which focus to be set after alert"%>
<%@ attribute name="displayLength" type="java.lang.String"
	description=""%>
<%@ attribute name="headerValue" type="java.lang.String"
	description="attribute for multiple headers"%>
<%@ attribute name="colSpan1" type="java.lang.String"
	description="column span for first header"%>
<%@ attribute name="colSpan2" type="java.lang.String"
	description="column span for second header"%>
<%@ attribute name="colSpan3" type="java.lang.String"
	description="column span for third header"%>
<%@ attribute name="colSpan4" type="java.lang.String"
	description="column span for fourth header"%>
<%@ attribute name="value1" type="java.lang.String"
	description="label for first header"%>
<%@ attribute name="value2" type="java.lang.String"
	description="label for second header"%>
<%@ attribute name="value3" type="java.lang.String"
	description="label for third header"%>
<%@ attribute name="value4" type="java.lang.String"
	description="label for fourth header"%>
<%@ attribute name="fnSuccessCallback" type="java.lang.String"
	description=""%>
<%@ attribute name="fnErrorCallback" type="java.lang.String"
	description=""%>
	

<%@ attribute name="scrollable" type="java.lang.String" description=""%>
<%@ attribute name="yscroll" type="java.lang.String" description=""%>
<%@ attribute name="search" type="java.lang.String" description=""%>
<%@ attribute name="maxCheckCount" type="java.lang.String"%>
<%@ attribute name="retainPropertyList" type="java.lang.String"%>
<%@ attribute name="defaultRadio" type="java.lang.String"%>
<%@ attribute name="fixedColumn" type="java.lang.String"%>
<%@ attribute name="leftFixedColumn" type="java.lang.String"%>
<%@ attribute name="rightFixedColumn" type="java.lang.String"%>
<%@ attribute name="retainDataTriggeringPropertyList" type="java.lang.String"%>
<%@ attribute name="removeRetainDataEventTriggeringPropertyList" type="java.lang.String"%>
<%@ attribute name="removeRetainDataCallback" type="java.lang.String"%>
<%@ attribute name="putRetainDataCallback" type="java.lang.String"%>
<%@ attribute name="ignoreForResize" type="java.lang.String"%>
<%@ attribute name="enableRowReorder" type="java.lang.String"%>
<%@ attribute name="rowReorderDataSrc" type="java.lang.String"%>
<%@ attribute name="orderCustom" type="java.lang.String"%>
<%@ attribute name="orderCustomFixed" type="java.lang.String"%>
<%@ attribute name="fnInfoCallback" type="java.lang.String"%>

<style type="text/css">
table.KeyTable tr.focus td {
	background: #FFFFE0;
}

table.KeyTable tr.focus td.focus {
	-moz-box-shadow: 0 0 3px yellow inset, 0 0 1px yellow;
	-webkit-box-shadow: 0 0 9px yellow inset, 0 0 1px yellow;
	box-shadow: 0 0 6px yellow inset, 0 0 1px yellow;
	border: 1px solid #3366FF;
}

td.right {
	text-align: right;
}

table thead th p {
  display: inline;
}

table thead th div {
  float : left;
}
#neutrino-body .dataTables_wrapper td {
	word-break: normal;
	white-space: normal;
}

.modal-body .table td {
	word-break: normal;
}

.dataTables_wrapper th:last-child, .dataTable td:last-child {
	text-align: left;
}

.dataTables_scroll {
	overflow-y: auto !important;
}

.dataTables_scrollBody {
	-webkit-overflow-scrolling: initial;
}

.dataTables_scrollHeadInner thead th {
  position : relative
}

.dataTables_scrollHeadInner thead th p {
  position : absolute
}

</style>

<c:set var="delimitor" value="|" scope="request" />
<c:set var="dataTableId" value="${htmlTableId}" scope="request" />

<c:set var="titles" scope="request" />
<c:set var="titleNames" scope="request" />
<c:set var="columnTypeList" scope="request" />
<c:set var="properties" scope="request" />
<c:set var="alignments" scope="request" />
<c:set var="amtFormatList" scope="request" />
<c:set var="footerValueList" scope="request" />
<c:set var="widthList" scope="request" />
<c:set var="sortableList" scope="request" />
<c:set var="colvisList" scope="request" />
<c:set var="headerAlignList" scope="request" />
<c:set var="imgSrcList" scope="request" />
<c:set var="onClickList" scope="request" />
<c:set var="soTypeList" scope="request" />
<c:set var="defaultVisList" scope="request" />
<c:set var="groupTypeList" scope="request" />
<c:set var="groupNameList" scope="request" />
<c:set var="selectedRadio" scope="request" />
<c:set var="neoDisplayIconList" scope="request" />
<c:set var="maxLengthList" scope="request" />
<c:set var="columnsSearchList" scope="request" />
<c:set var="wordBreakList" scope="request" />
<c:set var="lovViewModeList" scope="request" />
<c:set var="lovAlignToolTipList" scope="request" />
<c:set var="lovPreValidationMethodList" scope="request" />
<c:set var="lovValidationFailureMethodList" scope="request" />
<c:set var="lovFilterDataMethodList" scope="request" />
<c:set var="lovPostExecutionScriptList" scope="request" />
<c:set var="lovKeyList" scope="request" />
<c:set var="lovShowModalAlongWithValueList" scope="request" />
<c:set var="lovShowTextAreaInViewModeList" scope="request" />
<c:set var="lovMandatoryList" scope="request" />

<jsp:doBody />

<c:set var="columnCount" value="${0}" scope="request"/>
<c:set var="listProperty" value="${fn:split(properties, delimitor)}"
	scope="request" />
<c:set var="listColumnType"
	value="${fn:split(columnTypeList, delimitor)}" scope="request" />
<c:set var="listTitleNames" value="${fn:split(titleNames, delimitor)}"
	scope="request" />
<c:set var="listTiles" value="${fn:split(titles, delimitor)}"
	scope="request" />
<c:set var="listTxtAlign" value="${fn:split(alignments, delimitor)}"
	scope="request" />
<c:set var="listAmtFormat" value="${fn:split(amtFormatList, delimitor)}"
	scope="request" />
<c:set var="listfooterValue"
	value="${fn:split(footerValueList, delimitor)}" scope="request" />
<c:set var="listWidth" value="${fn:split(widthList, delimitor)}"
	scope="request" />
<c:set var="listsortable" value="${fn:split(sortableList, delimitor)}"
	scope="request" />
<c:set var="listColvis" value="${fn:split(colvisList, delimitor)}"
	scope="request" />
<c:set var="listHeaderAlign"
	value="${fn:split(headerAlignList, delimitor)}" scope="request" />
<c:set var="listImgSrc" value="${fn:split(imgSrcList, delimitor)}"
	scope="request" />
<c:set var="listOnClick" value="${fn:split(onClickList, delimitor)}"
	scope="request" />
<c:set var="listSType" value="${fn:split(soTypeList, delimitor)}"
	scope="request" />
<c:set var="listDefaultVis"
	value="${fn:split(defaultVisList, delimitor)}" scope="request" />
<c:set var="listGroupType" value="${fn:split(groupTypeList, delimitor)}"
	scope="request" />
<c:set var="listGroupName" value="${fn:split(groupNameList, delimitor)}"
	scope="request" />
<c:set var="defaultSelectedRadioIndex" value="${selectedRadio}"
	scope="request" />
<c:set var="listDisplayIcons"
	value="${fn:split(neoDisplayIconList, delimitor)}" scope="request" />
<c:set var="listmaxLength" value="${fn:split(maxLengthList, delimitor)}"
	scope="request" />
<c:set var="listColumnSearch"
	value="${fn:split(columnsSearchList, delimitor)}" scope="request" />
<c:set var="listGlyphName"
	value="${fn:split(glyphsNameList, delimitor)}" scope="request" />
<c:set var="listWordBreak" value="${fn:split(wordBreakList, delimitor)}"
	scope="request" />
<c:set var="listLovViewMode" value="${fn:split(lovViewModeList, delimitor)}"
	scope="request" />
<c:set var="listLovAlignToolTip" value="${fn:split(lovAlignToolTipList, delimitor)}"
	scope="request" />
<c:set var="listLovPreValidationMethod" value="${fn:split(lovPreValidationMethodList, delimitor)}"
	scope="request" />
<c:set var="listLovValidationFailureMethod" value="${fn:split(lovValidationFailureMethodList, delimitor)}"
	scope="request" />
<c:set var="listLovFilterDataMethod" value="${fn:split(lovFilterDataMethodList, delimitor)}"
	scope="request" />
<c:set var="listLovPostExecutionScript" value="${fn:split(lovPostExecutionScriptList, delimitor)}"
	scope="request" />
<c:set var="listLovKey" value="${fn:split(lovKeyList, delimitor)}"
	scope="request" />
<c:set var="listLovShowModalAlongWithValue" value="${fn:split(lovShowModalAlongWithValueList, delimitor)}"
	scope="request" />
<c:set var="listLovShowTextAreaInViewMode" value="${fn:split(lovShowTextAreaInViewModeList, delimitor)}"
	scope="request" />
<c:set var="listLovMandatory" value="${fn:split(lovMandatoryList, delimitor)}"
	scope="request" />

<c:set var="key" value="URL" scope="page" />

<c:set var="yScrolls" value="false" scope="page" />
<c:if test="${not empty yscroll}">
	<c:if test="${yscroll eq true}">
		<c:set var="yScrolls" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="searchGrid" value="true" scope="page" />
<c:if test="${not empty search}">
	<c:if test="${search eq false}">
		<c:set var="searchGrid" value="false" scope="page" />
	</c:if>
</c:if>


<c:set var="tableTools" value="false" scope="page" />
<c:if test="${not empty tools}">
	<c:if test="${tools eq true}">
		<c:set var="tableTools" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="pdfButton" value="false" scope="page" />
<c:if test="${not empty pdfDownload}">
	<c:if test="${pdfDownload eq true}">
		<c:set var="pdfButton" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="csvButton" value="false" scope="page" />
<c:if test="${not empty csvDownload}">
	<c:if test="${csvDownload eq true}">
		<c:set var="csvButton" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="xlsButton" value="false" scope="page" />
<c:if test="${not empty xlsDownload}">
	<c:if test="${xlsDownload eq true}">
		<c:set var="xlsButton" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="copyButton" value="false" scope="page" />
<c:if test="${not empty copyDownload}">
	<c:if test="${copyDownload eq true}">
		<c:set var="copyButton" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="scroll" value="false" scope="page" />
<c:if test="${not empty scrollable}">
	<c:if test="${scrollable eq true}">
		<c:set var="scroll" value="true" scope="page" />
	</c:if>
</c:if>


<c:set var="currencyCode" value="currencyCode" scope="page" />
<c:if test="${not empty currencyProp}">
	<c:set var="currencyCode" value="${currencyProp}" scope="page" />
</c:if>

<c:set var="mblankKey" value="msg.00000035" scope="page" />
<c:if test="${not empty blankKey}">
	<c:set var="mblankKey" value="${blankKey}" scope="page" />
</c:if>

<c:set var="sdisplayLength" value="10" scope="page" />
<c:if test="${not empty displayLength}">
	<c:set var="sdisplayLength" value="${displayLength}" scope="page" />
</c:if>

<c:set var="mblankPopUp" value="true" scope="page" />
<c:if test="${not empty blankPopUp}">
	<c:if test="${blankPopUp eq false}">
		<c:set var="mblankPopUp" value="false" scope="page" />
	</c:if>
</c:if>

<c:if test="${not empty focusField}">
	<c:set var="mfocusField" value="${focusField}" scope="page" />
</c:if>


<c:set var="rGroup" value="false" scope="page" />
<c:if test="${not empty radioGroup}">
	<c:if test="${radioGroup eq true}">
		<c:set var="rGroup" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="loadGridUrl" value="null" scope="page" />
<c:if test="${not empty loadDataUrl}">
	<c:set var="loadGridUrl" value="${loadDataUrl}" scope="page" />
</c:if>

<c:set var="lfnRowCallback" value="" scope="page" />
<c:if test="${not empty fnRowCallback}">
	<c:set var="lfnRowCallback" value="${fnRowCallback}" scope="page" />
</c:if>

<c:set var="lpreAjaxParamCallback" value="" scope="page" />
<c:if test="${not empty preAjaxParamCallback}">
	<c:set var="lpreAjaxParamCallback" value="${preAjaxParamCallback}" scope="page" />
</c:if>



<c:set var="lfnInitComplete" value="" scope="page" />
<c:if test="${not empty fnInitComplete}">
	<c:set var="lfnInitComplete" value="${fnInitComplete}" scope="page" />
</c:if>

<c:set var="lfnFooterCallback" value="" scope="page" />
<c:if test="${not empty fnFooterCallback}">
	<c:set var="lfnFooterCallback" value="${fnFooterCallback}" scope="page" />
</c:if>

<c:set var="lfnDrawCallback" value="" scope="page" />
<c:if test="${not empty fnDrawCallback}">
	<c:set var="lfnDrawCallback" value="${fnDrawCallback}" scope="page" />
</c:if>

<c:set var="lfnSuccessCallback" value="" scope="page" />
<c:if test="${not empty fnSuccessCallback}">
	<c:set var="lfnSuccessCallback" value="${fnSuccessCallback}"
		scope="page" />
</c:if>

<c:set var="lfnErrorCallback" value="" scope="page" />
<c:if test="${not empty fnErrorCallback}">
	<c:set var="lfnErrorCallback" value="${fnErrorCallback}"
		scope="page" />
</c:if>

<c:set var="lfnInfoCallback" value="return defaultInfoCallback(this)" scope="page" />
<c:if test="${not empty fnInfoCallback}">
	<c:set var="lfnInfoCallback" value="${fnInfoCallback}"
		scope="page" />
</c:if>

<c:set var="serverSideFlag" value="false" scope="page" />
<c:if test="${not empty serverSide}">
	<c:if test="${serverSide eq true}">
		<c:set var="serverSideFlag" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="paginateFlag" value="true" scope="page" />
<c:if test="${not empty paginate}">
	<c:if test="${paginate eq false}">
		<c:set var="paginateFlag" value="false" scope="page" />
	</c:if>
</c:if>

<c:set var="serialNumberFlag" value="false" scope="page" />
<c:if test="${not empty serialNumber}">
	<c:if test="${serialNumber eq true}">
		<c:set var="serialNumberFlag" value="true" scope="page" />
	</c:if>
</c:if>


<c:set var="titleNameFlag" value="false" scope="page" />
<c:if test="${not empty titleName}">
	<c:if test="${titleName eq true}">
		<c:set var="titleNameFlag" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="methodType" value="GET" scope="page" />
<c:if test="${not empty method}">
	<c:if test="${method eq 'POST'}">
		<c:set var="methodType" value="POST" scope="page" />
	</c:if>
</c:if>

<c:set var="loadByDefault" value="true" scope="page" />
<c:if test="${not empty defaultLoad}">
	<c:if test="${defaultLoad eq false}">
		<c:set var="loadByDefault" value="false" scope="page" />
	</c:if>
</c:if>


<c:set var="footerAva" value="false" scope="page" />
<c:if test="${not empty footer}">
	<c:if test="${footer eq true}">
		<c:set var="footerAva" value="true" scope="page" />
	</c:if>
</c:if>

<c:set var="checkBoxdef" value="true" scope="page" />
<c:if test="${not empty defaultCheckBox}">
	<c:if test="${defaultCheckBox eq false}">
		<c:set var="checkBoxdef" value="false" scope="page" />
	</c:if>
</c:if>

<c:set var="headerDef" value="false" scope="page" />
<c:if test="${not empty headerValue}">
	<c:if test="${headerValue eq true}">
		<c:set var="headerDef" value="true" scope="page" />
	</c:if>
</c:if>

<c:if test="${not empty retainPropertyList}">
	<c:set var="retainDataPropertyList" value="${retainPropertyList}" scope="page" />
</c:if>

<c:if test="${not empty retainDataTriggeringPropertyList}">
  <c:set var="retainDataEventTriggeringPropertyList" value="${retainDataTriggeringPropertyList}" scope="page" />
</c:if>

<c:set var="radiodef" value="true" scope="page" />
<c:if test="${not empty defaultRadio}">
	<c:if test="${defaultRadio eq false}">
		<c:set var="radiodef" value="false" scope="page" />
	</c:if>
</c:if>

<c:set var="columnFixed" value="false" scope="page" />
<c:if test="${fixedColumn eq true }">
  <c:set var="columnFixed" value="true" scope="page" />
</c:if>

<c:set var="fixedColumnLeftSide" value="0" scope="page" />
<c:if test="${not empty leftFixedColumn}">
  <c:set var="fixedColumnLeftSide" value="${leftFixedColumn}" scope="page" />
</c:if>

<c:set var="fixedColumnRightSide" value="0" scope="page" />
<c:if test="${not empty rightFixedColumn}">
  <c:set var="fixedColumnRightSide" value="${rightFixedColumn}" scope="page" />
</c:if>
<c:if test="${not empty removeRetainDataCallback}">
  <c:set var="removeRetainDataCallback" value="${removeRetainDataCallback}" scope="page" />
</c:if>

<c:if test="${not empty putRetainDataCallback}">
  <c:set var="putRetainDataCallback" value="${putRetainDataCallback}" scope="page" />
</c:if>

<c:if test="${not empty removeRetainDataEventTriggeringPropertyList}">
  <c:set var="removeRetainDataEventTriggeringPropertyList" value="${removeRetainDataEventTriggeringPropertyList}" scope="page" />
</c:if>

<c:set var="enableToggleCheckBox">
  <spring:eval expression="@lmsTableTagProperties.getProperty('enableToggleCheckBox')"/>
</c:set>

<c:if test="${empty enableToggleCheckBox }">
  <c:set var="enableToggleCheckBox" value="false" />
</c:if>

<c:set var="ignoreForResizeClass" value="" />
<c:if test="${not empty ignoreForResize && ignoreForResize eq 'false'}">
  <c:set var="ignoreForResizeClass" value="lmsTable-resize" />
</c:if>

<c:if test="${empty enableRowReorder}" >
    <c:set var="enableRowReorder" value="false" />
</c:if>

<c:if test="${empty rowReorderDataSrc}" >
    <c:set var="rowReorderDataSrc" value="0" />
</c:if>

<c:if test="${empty orderCustom}" >
    <c:set var="orderCustom" value="" />
</c:if>

<c:if test="${empty orderCustomFixed}" >
    <c:set var="orderCustomFixed" value="" />
</c:if>

<c:choose>
	<c:when test="${fn:contains(dateformat, 'MMM')}">
		<c:set var="pluginDateFormat"
			value="${fn:replace(dateformat,'MMM','M')}" />
	</c:when>
	<c:otherwise>
		<c:set var="pluginDateFormat" value="${fn:toLowerCase(dateformat)}"></c:set>
	</c:otherwise>
</c:choose>

<div id="<c:out value='${htmlTableId}'/>_div">
    	<table id="<c:out value='${htmlTableId}'/>" class="table table-bordered table-striped lmsTable ${ignoreForResizeClass}" style="width: 100%">
		<thead class="gridHeading">
			<c:if test="${headerDef == true}">
				<c:if test="${not empty colSpan1}">
					<th colspan="<c:out value='${colSpan1}'/>"
						style="text-align: center"><spring:message code="${value1}" /></th>
				</c:if>
				<c:if test="${not empty colSpan2}">
					<th colspan="<c:out value='${colSpan2}'/>"
						style="text-align: center"><spring:message code="${value2}" /></th>
				</c:if>
				<c:if test="${not empty colSpan3}">
					<th colspan="<c:out value='${colSpan3}'/>"
						style="text-align: center"><spring:message code="${value3}" /></th>
				</c:if>
				<c:if test="${not empty colSpan4}">
					<th colspan="<c:out value='${colSpan4}'/>"
						style="text-align: center"><spring:message code="${value4}" /></th>
				</c:if>
			</c:if>

			<tr>
				<c:set var="neoIcons" value="${listDisplayIcons}" />
				<c:choose>
					<c:when test="${checkBoxdef == true}">
						<th style="vertical-align: middle; text-align: center;" class="noExport"><c:if test="${enableToggleCheckBox eq true}"><label class="toggleSwitch"></c:if> <input
							id="<c:out value='${htmlTableId}'/>_check_all" type="checkbox" <c:if test="${not empty maxCheckCount}"> maxCheckCount="<c:out value='${maxCheckCount}' />" </c:if> <c:if test="${maxCheckCount eq '0'}"> disabled="true" </c:if> />
							<c:if test="${enableToggleCheckBox eq true}"><span id="<c:out value='${htmlTableId}'/>_check_all_span" class="slider round"></span></label></c:if>
							<span class="block-no"><spring:message code="lbl.10000162" /></span></th>
						<c:set var="it" value="1" />
						<c:choose>
							<c:when test="${titleNameFlag==true}">
								<c:forEach items="${listTitleNames}" var="titleName"
									varStatus="i">
									<c:set var="whiteSpaceNoWrap" value=""/>
                                    <c:if test="${listColumnType[it] == 'lov'}">
                                        <c:set var="whiteSpaceNoWrap" value=";white-space: nowrap;"/>
                                    </c:if>
									<c:if test="${neoIcons[it] ne null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										${titleName}<i class="<c:out value='${neoIcons[it]}'/> "></i>
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div style="visibility:hidden" class="pull-right"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:if test="${neoIcons[it] eq null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										${titleName}
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div style="visibility:hidden" class="pull-right"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:set var="it" value="${it + 1}" />
								</c:forEach>
							</c:when>
							<c:otherwise>
								<c:forEach items="${listTiles}" var="title" varStatus="i">
								    <c:set var="whiteSpaceNoWrap" value=""/>
                                    <c:if test="${listColumnType[it] == 'lov'}">
                                        <c:set var="whiteSpaceNoWrap" value=";white-space: nowrap;"/>
                                    </c:if>
									<c:if test="${neoIcons[it] ne null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										<spring:message code="${title}" /> <i class="<c:out value='${neoIcons[it]}'/> "></i>
                    <c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div style="visibility:hidden" class="pull-right"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:if test="${neoIcons[it] eq null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										<spring:message code="${title}" />
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div style="visibility:hidden" class="pull-right"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:set var="it" value="${it + 1}" />
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</c:when>
					<c:when test="${serialNumberFlag == true}">
						<th><spring:message code="lbl.sNo" /></th>
						<c:set var="it" value="1" />
						<c:choose>
							<c:when test="${titleNameFlag==true}">
								<c:forEach items="${listTitleNames}" var="titleName"
									varStatus="i">
									<c:set var="whiteSpaceNoWrap" value=""/>
									<c:if test="${listColumnType[it] == 'lov'}">
                                        <c:set var="whiteSpaceNoWrap" value=";white-space: nowrap;"/>
                                    </c:if>
									<c:if test="${neoIcons[it] ne null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										${titleName}<i class="<c:out value='${neoIcons[it]}'/> "></i>
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div style="visibility:hidden" class="pull-right"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:if test="${neoIcons[it] eq null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										${titleName}
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div class="pull-right" style="visibility:hidden"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:set var="it" value="${it + 1}" />
								</c:forEach>
							</c:when>
							<c:otherwise>
								<c:forEach items="${listTiles}" var="title" varStatus="i">
								    <c:set var="whiteSpaceNoWrap" value=""/>
								    <c:if test="${listColumnType[it] == 'lov'}">
                                        <c:set var="whiteSpaceNoWrap" value=";white-space: nowrap;"/>
                                    </c:if>
									<c:if test="${neoIcons[it] ne null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										<spring:message code="${title}" /> <i class="<c:out value='${neoIcons[it]}'/> "></i>
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div class="pull-right" style="visibility:hidden"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:if test="${neoIcons[it] eq null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										<spring:message code="${title}" />
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div class="pull-right" style="visibility:hidden"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:set var="it" value="${it + 1}" />
								</c:forEach>
							</c:otherwise>
						</c:choose>

					</c:when>
					<c:otherwise>
						<c:set var="it" value="0" />
						<c:choose>
							<c:when test="${titleNameFlag==true}">
								<c:forEach items="${listTitleNames}" var="titleName"
									varStatus="i">
									<c:set var="whiteSpaceNoWrap" value=""/>
									<c:if test="${listColumnType[it] == 'lov'}">
									    <c:set var="whiteSpaceNoWrap" value=";white-space: nowrap;"/>
									</c:if>
									<c:if test="${neoIcons[it] ne null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										</c:if>
										${titleName}
										<div class="pull-right" style="visibility:hidden"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										<i class="<c:out value='${neoIcons[it]}'/> "></i>
										</th>
									</c:if>
									<c:if test="${neoIcons[it] eq null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										${titleName}
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div class="pull-right" style="visibility:hidden"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:set var="it" value="${it + 1}" />
								</c:forEach>
							</c:when>
							<c:otherwise>
								<c:forEach items="${listTiles}" var="title" varStatus="i">
								    <c:set var="whiteSpaceNoWrap" value=""/>
								    <c:if test="${listColumnType[it] == 'lov'}">
								        <c:set var="whiteSpaceNoWrap" value=";white-space: nowrap;"/>
                                    </c:if>
									<c:if test="${neoIcons[it] ne null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}" >
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										<spring:message code="${title}" /> <i class="<c:out value='${neoIcons[it]}'/> "></i>
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div class="pull-right" style="visibility:hidden"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:if test="${neoIcons[it] eq null}">
										<th style="text-align: ${listHeaderAlign[it]}${whiteSpaceNoWrap}">
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  <p>
										</c:if>
										<spring:message code="${title}" />
										<c:if test="${listColumnSearch[it] eq 'Y'}">
										  </p>
										  <div class="pull-right" style="visibility:hidden"><a href="javascript:void(0)" data-toggle="popover">  <span class="glyphicon glyphicon-filter"></span></a></div>
										</c:if>
										</th>
									</c:if>
									<c:set var="it" value="${it + 1}" />
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</c:otherwise>
				</c:choose>
			</tr>
		</thead>
		<tbody>
		</tbody>
		<c:if test="${footerAva == true}">
			<tfoot style="background-color: darkgray; border: 0">
				<tr>
					<c:set var="it" value="0" />
					<c:forEach items="${listColumnType}" var="listColumnType">
						<c:choose>
							<c:when test="${listfooterValue[it] == 'sum'}">
								<c:if test="${listColumnType == 'textAmount'}">
									<th style="text-align: right; padding: 3px 10px 3px 10px;"><span
										id="<c:out value='${htmlTableId}'/>_f${it}"></span></th>
								</c:if>
								<c:if test="${listColumnType == 'amount'}">
									<th style="text-align: right; padding: 3px 16px 3px 10px;"><span
										id="<c:out value='${htmlTableId}'/>_f${it}"></span></th>
								</c:if>
							</c:when>
							<c:when test="${listfooterValue[it] == 'null'}">
								<th><span id="<c:out value='${htmlTableId}'/>_f${it}"></span></th>
							</c:when>
							<c:otherwise>
								<th><span id="<c:out value='${htmlTableId}'/>_f${it}">${listfooterValue[it]}</span></th>
							</c:otherwise>
						</c:choose>
						<c:set var="it" value="${it + 1}" />
					</c:forEach>
				</tr>
			</tfoot>
		</c:if>
	</table>
</div>
<script type="text/javascript">
var table_${htmlTableId}={
	scrollable:	${scroll},
	verticalScrollable:${yScrolls}
};
var datatableWarnAlert = new LMSPopUpHelper('OK');
var datatableErrorAlert = new LMSPopUpHelper('OK', 'ERROR');
var ${htmlTableId}_numberRoundingAndFormatHelper = null;
var rowCounter = 0;
var buttonCommon;
jQuery(document).ready(function(){
	${htmlTableId}_numberRoundingAndFormatHelper = new NumberRoundingAndFormatHelper(amountFormatWithoutPrecision,pageContextPath,groupingSeperator,decimalSeperator,currencyFormatInfo);
	eventOnLoadForDate();
 	jQuery('input').on('keydown',function(e){
 		if(e.keyCode==13){
			if(jQuery(this).hasClass('loanaccountnumber')){
             				openLoanSearchWindow(this);
             			}
 		}
	});

  jQuery(document).off('keyup','#${htmlTableId}_wrapper div.dataTables_filter input').on('keyup','#${htmlTableId}_wrapper div.dataTables_filter input', function(e){
		filter${htmlTableId}DataBasedOnInput(this,e);
	});

  jQuery(document).off('mousedown',".tableToolButton").on('mousedown',".tableToolButton",function(){
    rowCounter = 0;
  });

  jQuery(document).on("click",".dt-button",function(){
    oTable_${htmlTableId}.draw();
  });

  jQuery(document).on("mouseover",get${htmlTableId}PopoverSelector(),function(){
    jQuery(this).find('div').css('visibility','visible');
  });

  jQuery(document).on("mouseout",get${htmlTableId}PopoverSelector(),function(){
    var popoverElement = jQuery(this).find('a');
    if(jQuery('#'+popoverElement.attr('aria-describedby')).length==0 && ValidatorUtils.isEmpty(popoverElement.prop("filterValue"))){
      jQuery(this).find('div').css('visibility','hidden');
    }
  });

  var $popover = jQuery(get${htmlTableId}PopoverSelector() + " div a").each(function(){
    jQuery(this).popover({
      html      : true,
      content   : "<input type='text' id='${htmlTableId}_filterSpecificColumn'>",
      title     : getMessage("lbl.search"),
      placement : "bottom",
      container : false,
      trigger   : 'click'
    }).data('bs.popover').tip().addClass('specificColumnFilterPopover');
  })

  jQuery(document).on("click", function (e) {
    var $target = jQuery(e.target),
        clickExecutedOnPopover = jQuery(e.target).closest('a').is('[data-toggle=popover]'),
        isPopoverOnClickedEventVisible = jQuery(e.target).closest('.specificColumnFilterPopover').length > 0

    if (!clickExecutedOnPopover && !isPopoverOnClickedEventVisible){
      $popover.popover('hide');
      if(ValidatorUtils.isNotEmpty(jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').data('bs.popover'))){
        jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').data('bs.popover').inState.click=false;
      }
      if(ValidatorUtils.isEmpty(jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').prop("filterValue"))){
        jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').closest('div').css('visibility','hidden');
      }
    }
  });

  jQuery("[data-toggle='popover']").on('click',function(e){
    e.stopPropagation();
  });

  jQuery("[data-toggle='popover']").on('shown.bs.popover', function(){
    jQuery('.specificColumnFilterPopover').find('input').val(jQuery(this).prop("filterValue"));
    jQuery('.specificColumnFilterPopover').find('input').focus();
  });

  jQuery("[data-toggle='popover']").on('show.bs.popover', function(){
    if(jQuery('.specificColumnFilterPopover').length>0){
      jQuery('.specificColumnFilterPopover').each(function(){
        jQuery('a[aria-describedby="'+jQuery(this).attr('id')+'"]').each(function(){
          if(ValidatorUtils.isEmpty(jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').prop("filterValue"))){
            jQuery(this).closest('div').css('visibility','hidden');
          }
        });
      });
      jQuery('.specificColumnFilterPopover').popover('hide');
      if(ValidatorUtils.isNotEmpty(jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').data('bs.popover'))){
        jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').data('bs.popover').inState.click=false;
      }
    }
  });

  jQuery(document).on('keyup change','#${htmlTableId}_filterSpecificColumn', function (e) {
    var filterColumnLink = jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]');
    var colIdx = filterColumnLink.closest('th').index();
  	oTable_${htmlTableId}.column(colIdx).search(this.value).draw();
    filterColumnLink.prop("filterValue",this.value);
    if(e.keyCode==13){
      $popover.popover('hide');
      if(ValidatorUtils.isNotEmpty(jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').data('bs.popover'))){
        jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').data('bs.popover').inState.click=false;
      }
      if(ValidatorUtils.isEmpty(this.value)){
        jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').closest('div').css('visibility','hidden');
        jQuery('a[aria-describedby="'+jQuery('.specificColumnFilterPopover').attr('id')+'"]').prop('aria-describedby',"");
      }
    }
  });

  jQuery(document).on("page.dt","#${htmlTableId}",function(){
	  if(<c:out value="${radiodef}"/>) {
	  	${htmlTableId}_triggerDefaultSelectedRadioClickEvent(oTable_${htmlTableId});
	  }
	  setTimeout(function(){
	    if(${serverSideFlag} && ValidatorUtils.isNotEmpty(jQuery("#filter_${htmlTableId}").val()) && jQuery("#filter_${htmlTableId}").val().length>=2){
        filter${htmlTableId}DataBasedOnInput(jQuery("#filter_${htmlTableId}"),null);
      }
	  },0);
  });

  jQuery(document).on("length.dt","#${htmlTableId}",function(){
	  if(<c:out value="${radiodef}"/>) {
	  	${htmlTableId}_triggerDefaultSelectedRadioClickEvent(oTable_${htmlTableId});
	  }
	  setTimeout(function(){
	    if(ValidatorUtils.isNotEmpty(jQuery("#filter_${htmlTableId}").val()) && jQuery("#filter_${htmlTableId}").val().length>=2){
        filter${htmlTableId}DataBasedOnInput(jQuery("#filter_${htmlTableId}"),null);
      }
	  },0);
  });

  jQuery(document).off('click',"#${htmlTableId}_wrapper .sorting,#${htmlTableId}_wrapper .sorting_asc,#${htmlTableId}_wrapper .sorting_desc").on('click',"#${htmlTableId}_wrapper .sorting,#${htmlTableId}_wrapper .sorting_asc,#${htmlTableId}_wrapper .sorting_desc",function(){
    if(<c:out value="${serverSideFlag}"/>) {
      var oSettings = jQuery("#<c:out value='${htmlTableId}'/>").dataTable().fnSettings();
	  oSettings.oFeatures.bServerSide=false;
    }
    setTimeout(function(){
  	    if(ValidatorUtils.isNotEmpty(jQuery("#filter_${htmlTableId}").val()) && jQuery("#filter_${htmlTableId}").val().length>=2){
          filter${htmlTableId}DataBasedOnInput(jQuery("#filter_${htmlTableId}"),null);
        }
  	  },0);
  });

});

function get${htmlTableId}PopoverSelector(){
  var selectorValue = '#${htmlTableId}_wrapper th';
  <c:if test='${scroll eq true || yScrolls eq true}'>
    selectorValue = '#${htmlTableId}_wrapper .dataTables_scrollHeadInner th';
  </c:if>
  return selectorValue;
}

function format_date(date) {

	var date = date.replace(" ", "");

	if (date.indexOf('.') > 0) {
		/*date a, format dd.mn.(yyyy) ; (year is optional)*/
		var eu_date = date.split('.');
	} else {
		/*date a, format dd/mn/(yyyy) ; (year is optional)*/
		var eu_date = date.split('/');
	}

	return eu_date;
}

function defaultInfoCallback(oTable) {
	return getMessage("lms.datatable.info",[oTable.api().rows({ page: 'current' }).toArray()[0].length]);
}

jQuery.fn.dataTable.ext.order['dom-checkbox'] = function  ( settings, col )
{
	return this.api().column( col, {order:'index'} ).nodes().map( function ( td, i ) {
		return jQuery('input', td).prop('checked') ? '1' : '0';
	} );
}

jQuery.fn.dataTable.ext.order['lms_date-asc']  = function(a,b) {
    var ukDatea = format_date(a);
    var ukDateb = format_date(b);

    var x = (ukDatea[2] + ukDatea[0] + ukDatea[1]) * 1;
    var y = (ukDateb[2] + ukDateb[0] + ukDateb[1]) * 1;
    if (isNaN(x) || x < y) { return -1; }
    if (isNaN(y) || x > y) { return 1; }
    return ((x < y) ? -1 : ((x > y) ?  1 : 0));
};

jQuery.fn.dataTable.ext.order['lms_date-desc'] = function(a,b) {
	var ukDatea = format_date(a);
  var ukDateb = format_date(b);

  var x = (ukDatea[2] + ukDatea[0] + ukDatea[1]) * 1;
  var y = (ukDateb[2] + ukDateb[0] + ukDateb[1]) * 1;
  if (isNaN(y) || x < y) { return 1; }
  if (isNaN(x) || x > y) { return -1; }
  return ((x < y) ? 1 : ((x > y) ?  -1 : 0));
};

jQuery.fn.dataTable.Api.register( 'page.jumpToData()', function ( data, column ) {
  var pos = this.column(column, {order:'current'}).data().indexOf( data );

  if ( pos >= 0 ) {
      var page = Math.floor( pos / this.page.info().length );
      this.page( page ).draw( false );
  }

  return this;
});

$.fn.dataTable.Api.register('row().show()', function() {
  var page_info = this.table().page.info();
  // Get row index
  var new_row_index = this.index();
  // Row position
  var row_position = this.table().rows()[0].indexOf( new_row_index );
  // Already on right page ?
  if( row_position >= page_info.start && row_position < page_info.end ) {
      // Return row object
      return this;
  }
  // Find page number
  var page_to_display = Math.floor( row_position / this.table().page.len() );
  // Go to that page
  this.table().page( page_to_display );
  // Return row object
  return this;
});

jQuery.fn.dataTable.ext.errMode = 'alert';

var oTable_${htmlTableId};
var ${htmlTableId}_lms_url;
var ${htmlTableId}_lms_data;
var ${htmlTableId}_retainPropertyList = '<c:out value="${retainPropertyList}" escapeXml="false"></c:out>';
var ${htmlTableId}_retainDataEventTriggeringPropertyList = '<c:out value="${retainDataEventTriggeringPropertyList}" escapeXml="false"></c:out>';
var ${htmlTableId}_removeRetainDataEventTriggeringPropertyList = '<c:out value="${removeRetainDataEventTriggeringPropertyList}" escapeXml="false"></c:out>';
var ${htmlTableId}_removeRetainDataCallback = '<c:out value="${removeRetainDataCallback}" escapeXml="false"></c:out>';
var ${htmlTableId}_putRetainDataCallback = '<c:out value="${putRetainDataCallback}" escapeXml="false"></c:out>';

var ${htmlTableId}_lov_column_html = new Map();
var ${htmlTableId}_retained_data = new Map();
var ${htmlTableId}_selected_radio_retained_data = new Map();
var ${htmlTableId}_empty_search_flag = false;

function call${htmlTableId}Datatable(urlOrdata, key) {
	if(key == 'URL') {
		${htmlTableId}_lms_url  = urlOrdata;
		${htmlTableId}_lms_data = null;
	}
	else if(key == 'DATA') {
		${htmlTableId}_lms_url  = null;
		${htmlTableId}_lms_data = urlOrdata;
	}
	oTable_${htmlTableId} = $('#${htmlTableId}').dataTable({

	<c:if test="${enableRowReorder eq true}" >
        rowReorder: {
                    update: true,
                    dataSrc: '${rowReorderDataSrc}'
                },
    </c:if>

 		<c:set var="ColvisFlag" value="N" />
		<c:forEach items="${listColvis}" var="listColvis">
			<c:if test="${listColvis == 'Y'}">
				<c:set var="ColvisFlag" value="Y" />
			</c:if>
		</c:forEach>

		<c:set var="bSortFlag" value="false" />
		<c:forEach items="${listsortable}" var="listsortable">
			<c:if test="${listsortable == true}">
				<c:set var="bSortFlag" value="true" />
			</c:if>
		</c:forEach>
	"order": [${orderCustom}],
	"orderFixed": [${orderCustomFixed}],
	"sPaginationType": "simple_numbers",
    "tabIndex": 0,
	"dom": '<"row gridHeader"<c:if test="${searchGrid == true}"><"col-sm-6"f></c:if><c:if test="${searchGrid == false}"><"col-sm-6"></c:if><"col-sm-6"l<"#colvis">>r>t<"row gridFooter"<"col-sm-6"i><"col-sm-6 text-right"<c:if test="${tableTools == true}">B</c:if>p>>',
	"buttons": [
     <c:if test="${copyButton == true}">
	   {
       "extend"       : "copy",
       "name"         : "copy",
       "text"         : getMessage('label.copy'),
       "className"    : "tableToolButton",
       "exportOptions": {
         "columns"    : "thead th:not(.noExport)",
         "format"     : {
           		            body: function ( data, row, column, node ) {
                          <c:if test="${serialNumberFlag == true}">
      	       	            if(column==0){
      	                      rowCounter++;
      	                      data = rowCounter;
      	       	            }
                          </c:if>
           			          return getValueFromHtml(data);
           	}
         }
       }
	   },
	   </c:if>
	   <c:if test="${csvButton == true}">
	   {
	      "extend"       : "csv",
	      "name"         : "csv",
	      "text"         : getMessage('label.csv'),
	      "className"    : "tableToolButton",
		    "filename"     : "*",
		    "title"        : getMessage('label.finnOne.product'),
		    "extension"    : ".csv",
        "exportOptions": {
          "columns"    : "thead th:not(.noExport)",
          "format"     : {
            		            body: function ( data, row, column, node ) {
                           <c:if test="${serialNumberFlag == true}">
   	           	            if(column==0){
   	                          rowCounter++;
   	                          data = rowCounter;
   	           	            }
                           </c:if>
            			          return getValueFromHtml(data);
            	             }
                         }
        }
	   },
	   </c:if>
	   <c:if test="${xlsButton == true}">
	   {
	      "extend"       : "excel",
	      "name"         : "excel",
	      "text"         : getMessage('label.xls'),
	      "className"    : "tableToolButton",
	      "filename"     : "*",
	      "title"        : getMessage('label.finnOne.product'),
	      "extension"    : ".xls",
        "exportOptions": {
          "columns"    : "thead th:not(.noExport)",
          "format"     : {
            		            body: function ( data, row, column, node ) {
                           <c:if test="${serialNumberFlag == true}">
   	           	            if(column==0){
   	                          rowCounter++;
   	                          data = rowCounter;
   	           	            }
                           </c:if>
            			          return getValueFromHtml(data);
            	}
          }
        }
	   },
	   </c:if>
	   <c:if test="${pdfButton == true}">
	   {
	      "extend"       : "pdf",
	      "name"         : "pdf",
	      "text"         : getMessage('label.pdf'),
	      "className"    : "tableToolButton",
		    "filename"     : "*",
		    "extension"    : ".pdf",
		    "title"        : getMessage('label.finnOne.product'),
        "exportOptions": {
          "columns"    : "thead th:not(.noExport)",
          "format"     : {
            		            body: function ( data, row, column, node ) {
                           <c:if test="${serialNumberFlag == true}">
   	           	            if(column==0){
   	                          rowCounter++;
   	                          data = rowCounter;
   	           	            }
                           </c:if>
            			          return getValueFromHtml(data);
            	}
          }
        }
	   }
	   </c:if>
	],
	"language": {
  	  "paginate": {
	          "previous": getMessage('label.previous.page'),
	          "next":     getMessage('label.next.page')
	        },
         	  "zeroRecords": getMessage('label.no.records.found'),
	          "processing": getMessage('label.processing'),
	          "info": getMessage('label.datatable.info'),
	          "infoFiltered": "",
	          "search": getMessage('label.search'),
	          "lengthMenu": getMessage('label.show.entries')
      },
	<c:if test="${yScrolls == true}">
		<c:set var="paginateFlag" value="false" scope="page" />
	</c:if>
  <c:if test="${yScrolls ne true}">
	  "paging":${paginateFlag},
	</c:if>
    "searching": ${searchGrid},
    "responsive": true,
    "autoWidth": true,
    "pageLength": ${sdisplayLength},
    "destroy": true,
    "info": true,
    "ordering": ${bSortFlag},
    <c:if test="${yScrolls == true}">
		  "scrollY": "200px",
		  "scrollCollapse": true,
      "paging": false,
	  </c:if>
    <c:choose>
    	<c:when test="${scroll == true or columnFixed == true}">
    		"scrollX": true,
    		"scrollCollapse": true,
        "scroller":       true,
    	</c:when>
    	<c:otherwise>
    		"autoWidth": false,
    	</c:otherwise>
    </c:choose>
    <c:if test="${columnFixed == true}">
      "fixedColumns": {
        "leftColumns": ${fixedColumnLeftSide},
        "rightColumns": ${fixedColumnRightSide}
      },
    </c:if>
    "data": ${htmlTableId}_lms_data,
    "processing": true,
    "serverSide": ${serverSideFlag},
    "ajax": function (data, callback, settings  ) {

          data["CSRFToken"]=getCsrfTokenValue();
          ${lpreAjaxParamCallback};
    	    if(${htmlTableId}_lms_url!=null){
	    		  settings.jqXHR = $.ajax( {
			        "dataType": 'json',
			        "type": "<c:out value='${methodType}'/>",
			        "url": ${htmlTableId}_lms_url,
			        "data": data,
			        "async" : false,
			        "success": function(data){
						if(data.error != undefined){
							displayAjaxMessages(data,"<c:out value='${htmlTableId}_errorDiv'/>");
							callback({"aaData":[],"recordsTotal":settings._iRecordsTotal,"recordsFiltered":settings._iRecordsDisplay});
              ${lfnErrorCallback};
						} else if(data.aaData != undefined) {
			        		if(data.aaData == '') {
								    callback(data);
								    <c:if test="${mblankPopUp == true}">
								      //In case of server side by default pop-up should not be displayed so loadPopUp will be passed in aaData
								    	if(typeof data.loadPopUp!='undefined'){
								    		if(data.loadPopUp=="true" && ${htmlTableId}_empty_search_flag){
								    			datatableWarnAlert.showLmsAlert('<spring:message code="${mblankKey}" />','<spring:message code="lbl.info" />',null,"${mfocusField}");
								    		}
								    	}else if(${htmlTableId}_empty_search_flag){
								    		datatableWarnAlert.showLmsAlert('<spring:message code="${mblankKey}" />','<spring:message code="lbl.info" />',null,"${mfocusField}");
								    	}
								    </c:if>
								    updateFooterValuesForEmptyData();
							    } else {
								    var validatedData = validateDataForMissingProperties(settings,data.aaData);
								    if(validatedData != null)	{
									    callback(data);
								    } else {
									    callback({"aaData":[],"recordsTotal":settings._iRecordsTotal,"recordsFiltered":settings._iRecordsDisplay});
								    }
							    }
							    ${lfnSuccessCallback};
						    }
			        },
			        "error": function(xhr,status,err){
			        	if(err == 10) {
				          window.location.href = '${pageContext.request.contextPath}/lms/common/sessionException';
	         			}	else {
							    $("#<c:out value='${htmlTableId}'/>_errorDiv").html("An Error Occured in Datatable Operation.Please Try Again");
				        	$("#<c:out value='${htmlTableId}'/>_errorDiv").show();
				        	callback({"aaData":[],"recordsTotal":settings._iRecordsTotal,"recordsFiltered":settings._iRecordsDisplay});
				        }
			        }
      			});
    	    }else{
    	    //if data is passed blank then handling has been done
    	    	if(${htmlTableId}_lms_data==null && key == 'DATA'){
    	    		callback({"aaData":[],"recordsTotal":settings._iRecordsTotal,"recordsFiltered":settings._iRecordsDisplay});
    	    		<c:if test="${mblankPopUp == true && serverSideFlag == false}">
						    datatableWarnAlert.showLmsAlert('<spring:message code="${mblankKey}" />','<spring:message code="lbl.info" />',null,"${mfocusField}");
					    </c:if>
					    updateFooterValuesForEmptyData();
    	    	} else{
    	    		callback({"aaData":[],"recordsTotal":settings._iRecordsTotal,"recordsFiltered":settings._iRecordsDisplay});
    	    	}
    	    }
		},
	  "columns" :[
					<c:set var="it" value="0" />
					<c:forEach items="${listProperty}" var="listProperty" varStatus ="i">
						<c:if test="${it != 0}">
						,
						</c:if>
						{"data" :"${listProperty}",
						 "name" :"${listProperty}"
						}
						<c:set var="it" value="${it + 1}" />
					</c:forEach>
		     ],
					"columnDefs":[
									<c:set var="it" value="0" />
									<c:forEach items="${listColumnType}" var="listColumnType" varStatus ="j">
									<c:if test="${it != 0}">
									,
									</c:if>
									<c:set var="width" value="${listWidth[it]}" />
									<c:set var="maxLength" value="${listmaxLength[it]}" />
									<c:set var="align" value="${listTxtAlign[it]}" />
									<c:set var="wordBreak" value="${listWordBreak[it]}" />
									<c:choose>
										<c:when test="${listColumnType == 'hidden'}">
												{"targets":[${it}], "visible": false, "searchable": false,
													"createdCell": function (nTd, sData, oData, iRow, iCol) {
												         jQuery(nTd).attr('columnType', '${listColumnType}');
												     }
												}
										</c:when>
										<c:when test="${listColumnType == 'textAmount'}">
												{"targets":[${it}], "className": "right",
													<c:if test="${width != 'null'}">
													"width":"${width}",
													</c:if>
													"orderable":${listsortable[it]},
													<c:if test="${listDefaultVis[it] == 'Y'}">
													"visible":true
													</c:if>
													<c:if test="${listDefaultVis[it] == 'N'}">
													"visible":false
													</c:if>
													,
													"createdCell": function (nTd, sData, oData, iRow, iCol) {
												         jQuery(nTd).attr('columnType', '${listColumnType}');
												     }
												}
										</c:when>
										<c:when test="${listColumnType == 'rate'}">
												{"targets":[${it}], "className": "right",
													<c:if test="${width != 'null'}">
													"width":"${width}",
													</c:if>"orderable":${listsortable[it]},
													<c:if test="${listDefaultVis[it] == 'Y'}">
													"visible":true
													</c:if>
													<c:if test="${listDefaultVis[it] == 'N'}">
													"visible":false
													</c:if>
													,
													"createdCell": function (nTd, sData, oData, iRow, iCol) {
												         jQuery(nTd).attr('columnType', '${listColumnType}');
												     }
												}
										</c:when>
										<c:when test="${listColumnType != 'text'}">
								        		{"targets":[${it}],<c:if test="${width != 'null'}">
													"width":"${width}",
													</c:if>"orderable":${listsortable[it]},
													<c:if test="${listDefaultVis[it] == 'Y'}">
													"visible":true,
													</c:if>
													<c:if test="${listDefaultVis[it] == 'N'}">
													"visible":false,
													</c:if>
													<c:if test="${listColumnType == 'link'}">
													"orderDataType": "dom-a",
													</c:if>
													<c:if test="${listColumnType == 'checkBox' || listColumnType == 'dataCheckBox' || listColumnType == 'toggle' || listColumnType == 'dataToggle'}">
													"orderDataType": "dom-checkbox",
													</c:if>
													"type": '${listSType[it]}',"render" : function(data, type, row, meta){
												          var id;
								                  var htmlText=row.id;
								                  var obj = $(htmlText);
								                  var radioGroupIndex = '${listGroupType[it]}'=='H'?meta.row : meta.col;

								                  var getId=$(obj).attr("id");
								                  if(getId == null || getId=='undefined')
								                  {
								                      id=row.id;
								                  }
								                  else
								                  {
								                      id=$(obj).val();
								                  }

								        		var htmlCode="";
								        		var tableId="<c:out value='${htmlTableId}'/>";
								        		var valueChk = getValueFromHtml(data);
								        		var radioDef = "<c:out value='${radioDef}'/>";
								        		var dateFormatLength = dateFormatSessionScope.length;
								        		<c:choose>
												<c:when test="${listColumnType == 'lov'}">
													var lovDataMap = {};
													var lovPropertyData = row.${listProperty[it]};
                                                    var newLovId = tableId + 'lov' + '${listLovKey[it]}' + '${it}' + id;
                                                    var defaultLovId = tableId + "_lov_" + "<c:out value='${it + 1}'/>";
                                                    if (${htmlTableId}_lov_column_html.has("lovDiv${it+1}")) {
                                                        htmlCode = ${htmlTableId}_lov_column_html.get("lovDiv${it+1}");
                                                    }
                                                    else {
                                                        htmlCode = jQuery("#"+tableId+"_lov_div_"+"<c:out value='${it + 1}'/>").html();
                                                    	${htmlTableId}_lov_column_html.set("lovDiv${it+1}", htmlCode);
                                                    }
                                                    htmlCode = replaceIdOfLOVInHtml(htmlCode, defaultLovId, newLovId);
                                                    jQuery("#"+tableId+"_lov_div_"+"<c:out value='${it + 1}'/>").html(htmlCode);
													lovDataMap.preValidationMethod = <c:choose><c:when test="${listLovPreValidationMethod[it] ne 'null'}">${listLovPreValidationMethod[it]}</c:when><c:otherwise>null</c:otherwise></c:choose>;
													lovDataMap.validationFailureMethod = <c:choose><c:when test="${listLovValidationFailureMethod[it] ne 'null'}">${listLovValidationFailureMethod[it]}</c:when><c:otherwise>null</c:otherwise></c:choose>;
													lovDataMap.filterDataMethod = <c:choose><c:when test="${listLovFilterDataMethod[it] ne 'null'}">${listLovFilterDataMethod[it]}</c:when><c:otherwise>null</c:otherwise></c:choose>;
													lovDataMap.postExecutionScript = <c:choose><c:when test="${listLovPostExecutionScript[it] ne 'null'}">'${listLovPostExecutionScript[it]}'</c:when><c:otherwise>null</c:otherwise></c:choose>;
													lovDataMap.lovKey = '${listLovKey[it]}';
													lovDataMap.alignToolTip = <c:choose><c:when test="${listLovAlignToolTip[it] ne 'null'}">'${listLovAlignToolTip[it]}'</c:when><c:otherwise>''</c:otherwise></c:choose>;
													lovDataMap.shownModalEvenWithValue = '${listLovShowModalAlongWithValue[it]}';
													lovDataMap.viewMode = '${listLovViewMode[it]}';
													lovDataMap.required = '${listLovMandatory[it]}';
													lovDataMap.showTextAreaInViewMode = '${listLovShowTextAreaInViewMode[it]}';
                                                    bindEventsOnLovInTable(newLovId, lovDataMap, lovPropertyData);
													htmlCode = jQuery("#"+tableId+"_lov_div_"+"<c:out value='${it + 1}'/>").html();
													if (jQuery("#"+tableId+"_lov_div_"+"<c:out value='${it + 1}'/>").length > 0)
                                                        jQuery("#"+tableId+"_lov_div_"+"<c:out value='${it + 1}'/>").html("");
												</c:when>
												<c:when test="${listColumnType == 'action'}">
													<c:set var="st" value="0" />
													htmlCode = "";
													<c:forEach items="${listImgSrc}" var="listImgSrc" varStatus ="i" >
													htmlCode += '<a onclick="${listOnClick[st]}" style="margin-right: 5px;" href="#" id="action'+${st}+'"><img src="${cdnUrl}/images/${listImgSrc}"/></a>';
													<c:set var="st" value="${st + 1}" />
													</c:forEach>
												</c:when>
												<c:when test="${listColumnType == 'link'}">
													htmlCode = data!==null?'<a href="#" id="link'+${it}+id+'" style="float: ${align};" >'+data+'</a>':'';
												</c:when>
												<c:when test="${listColumnType == 'textBox'}">
													<c:choose>
													  <c:when test="${maxLength eq 'null'}">
													    htmlCode = '<input type="text" id="input'+${it}+id+'" style="float: ${align}; text-align: ${align};" value="'+data+'" class="col-sm-12" >';
													  </c:when>
													  <c:otherwise>
													    htmlCode = '<input type="text" id="input'+${it}+id+'" style="float: ${align}; text-align: ${align};" value="'+data+'" class="col-sm-12" maxLength="${maxLength}" >';
													  </c:otherwise>
													</c:choose>
												</c:when>
												<c:when test="${listColumnType == 'number'}">
													<c:choose>
													  <c:when test="${maxLength eq 'null'}">
													    htmlCode = '<input type="number" id="input'+${it}+id+'" style="float: ${align}; text-align: ${align};" value="'+data+'" class="col-sm-12" >';
													  </c:when>
													  <c:otherwise>
													    htmlCode = '<input type="number" id="input'+${it}+id+'" style="float: ${align}; text-align: ${align};" value="'+data+'" class="col-sm-12" maxLength="${maxLength}" >';
													  </c:otherwise>
													</c:choose>
												</c:when>
												<c:when test="${listColumnType == 'radio'}">
						var removeRetainDataCallback  = ValidatorUtils.isNotEmpty('${removeRetainDataCallback}')?"<c:out value='${removeRetainDataCallback}'/>":null;
						var putRetainDataCallbackVar= ValidatorUtils.isNotEmpty('${putRetainDataCallback}')?"<c:out value='${putRetainDataCallback}'/>":null;

                         var radioSelectedFunction = "selectThisRowRadio(this,oTable_${htmlTableId},"+tableId+",<c:out value='${radiodef}'/>,"+putRetainDataCallbackVar+","+removeRetainDataCallback+")";
                         if(ValidatorUtils.isNotEmpty(${htmlTableId}_retainDataEventTriggeringPropertyList) && ${htmlTableId}_removeRetainDataEventTriggeringPropertyList.indexOf('${listProperty[it]}')!=-1 &&ValidatorUtils.isNotEmpty('${removeRetainDataCallback}')){
                           radioSelectedFunction = "uncheckSelectedRowRadio(this,oTable_${htmlTableId},"+tableId+",<c:out value='${radiodef}'/>,<c:out value='${removeRetainDataCallback}'/>)";
                         }

                         if(ValidatorUtils.isNotEmpty(${htmlTableId}_retainDataEventTriggeringPropertyList) && ${htmlTableId}_retainDataEventTriggeringPropertyList.indexOf('${listProperty[it]}')==-1){
                           radioSelectedFunction = "uncheckSelectedRowRadio(this,oTable_${htmlTableId},"+tableId+",<c:out value='${radiodef}'/>)";
                         }
												 htmlCode = '<input type="radio" name="radio${listGroupType[it]}${listGroupName[it]}'+radioGroupIndex+'" id="radio'+${it}+id+'" onclick="'+radioSelectedFunction+'" value="'+data+'">';
												</c:when>
												<c:when test="${listColumnType == 'deleteRow'}">
												htmlCode='<a onclick="deleteRow(this,oTable_${htmlTableId},' + '\'' +tableId + '\'' + ');" style="align : center; vertical-align: middle;" href="#" id="deleteRow'+${it}+id+'"><img src="${cdnUrl}/images/Delete.png" style="margin:0px"/></a>'
											    </c:when>
												<c:when test="${listColumnType == 'checkBox'}">
													htmlCode = '<c:if test="${enableToggleCheckBox eq true}"><label id="check_label'+${it}+id+'" class="toggleSwitch"></c:if><input type="checkbox" class="chk1" style="float: ${align}; vertical-align: middle;" id="check'+${it}+id+'"  value="'+getValueFromHtml(data)+'" onclick="selectThisRow(this,oTable_${htmlTableId},' + '\'' +tableId + '\'' + ');" name="<c:out value='${htmlTableId}'/>_chk"><c:if test="${enableToggleCheckBox eq true}"><span id="check_span'+${it}+id+'" class="slider round"></span></label></c:if>';
												</c:when>
												<c:when test="${listColumnType == 'toggle'}">
												    htmlCode = '<div id="toggle_div'+${it}+id+'" style="align:${align};text-align: ${align};"><label id="toggle_label'+${it}+id+'" class="toggleSwitch"><input type="checkbox" class="chk1" style="float: ${align}; vertical-align: middle;" id="check'+${it}+id+'"  value="'+getValueFromHtml(data)+'" onclick="selectThisRow(this,oTable_${htmlTableId},' + '\'' +tableId + '\'' + ');" name="<c:out value='${htmlTableId}'/>_chk"><span id="toggle_span'+${it}+id+'" class="slider round"></span></label>';
												</c:when>
                                                <c:when test="${listColumnType == 'dataToggle'}">
													if(valueChk == 'Y')
														htmlCode = '<div id="dataToggle_div'+${it}+id+'" style="align:${align};text-align: ${align};"><label id="toggle_label'+${it}+id+'" class="toggleSwitch"><input type="checkbox" class="chk1" style="float: ${align}; vertical-align: middle;" id="check'+${it}+id+'"  value="'+getValueFromHtml(data)+'" onclick="selectThisRow(this,oTable_${htmlTableId},' + '\'' +tableId + '\'' + ');" name="<c:out value='${htmlTableId}'/>_chk" checked><span id="toggle_span'+${it}+id+'" class="slider round"></span></label>';
													else
														htmlCode = '<div id="dataToggle_div'+${it}+id+'" style="align:${align};text-align: ${align};"><label id="toggle_label'+${it}+id+'" class="toggleSwitch"><input type="checkbox" class="chk1" style="float: ${align}; vertical-align: middle;" id="check'+${it}+id+'"  value="'+getValueFromHtml(data)+'" onclick="selectThisRow(this,oTable_${htmlTableId},' + '\'' +tableId + '\'' + ');" name="<c:out value='${htmlTableId}'/>_chk"><span id="toggle_span'+${it}+id+'" class="slider round"></span></label>';
                                                </c:when>
												<c:when test="${listColumnType == 'glyph'}">
												   htmlCode = '<div id="glyph_div'+${it}+id+'" width="100%"  style="align:${align};text-align: ${align};"><a href="#" id="glyph'+${it}+id+'" style="font-size: 16px; float: ${align};" ><span id="glyph_span'+${it}+id+'" class="${listGlyphName[it]}"></span></a></div>';
											    </c:when>
												<c:when test="${listColumnType == 'dataCheckBox'}">
													if(valueChk == 'Y')
														htmlCode = '<c:if test="${enableToggleCheckBox eq true}"><label class="toggleSwitch"></c:if><input type="checkbox" style="align : center; vertical-align: middle;" id="checkNew'+${it}+id+'"  value="'+getValueFromHtml(data)+'" onclick="" name="<c:out value='${htmlTableId}'/>_checkNew" checked/><c:if test="${enableToggleCheckBox eq true}"><span id="check_span'+${it}+id+'" class="slider round"></span></label></c:if>';
													else
														htmlCode = '<c:if test="${enableToggleCheckBox eq true}"><label class="toggleSwitch"></c:if><input type="checkbox" style="align : center; vertical-align: middle;" id="checkNew'+${it}+id+'"  value="'+getValueFromHtml(data)+'" onclick="" name="<c:out value='${htmlTableId}'/>_checkNew"/><c:if test="${enableToggleCheckBox eq true}"><span id="check_span'+${it}+id+'" class="slider round"></span></label></c:if>';
								    			</c:when>
								    			<c:when test="${listColumnType == 'datepicker'}">
												htmlCode ='<div class="input-group date datepicker_div" id="datepicker_'+${it}+id+'" data-date-format="<c:out value='${pluginDateFormat}'/>" data-real-format="<c:out value='${dateformat}'/>" data-disable-past="" data-disable-future="" data-past-date="" data-open-window-before="" data-open-window-after="" data-block-calander=""><span id="'+${it}+id+'_span" class="input-group-addon float-r"><i class="TagdateIcon" style="cursor: not-allowed;"></i></span><input type="text" class="validateDateFormat" value="'+data+'" id="dp'+${it}+id+'" maxLength="'+dateFormatLength+'"></div>';
												</c:when>
												<c:when test="${listColumnType == 'datepicker1'}">
									      		htmlCode ='<input type="text" class=" datepicker" value="'+data+'" id="dp'+${it}+id+'" maxLength="'+dateFormatLength+'">';
												</c:when>
												<c:when test="${listColumnType == 'amount' && listDefaultVis[it] == 'Y'}">
												htmlCode = '<input type="text" id="amount'+${it}+id+'" value="'+getValueFromHtml(data)+'" style="text-align:right;size: 100%;float: ${align};" class=" lmsamount"/>';
													if(row != null && row != 'undefined'){
														var currencyCode=getValueFromHtml(row.currencyCode);
													}
													if(currencyCode == null || currencyCode=='undefined' || currencyCode == ''){
														currencyCode=tenantCurrencyISOCode
													}
													${htmlTableId}_numberRoundingAndFormatHelper.addFieldFormatInfo("amount"+${it}+id,currencyCode);
												</c:when>
												<c:when test="${listColumnType == 'select'}">
													htmlCode = '<select  class="form-control "  id="select'+${it}+id+'">';
													if(data != null && data[0]!=null && data[0]!="" && typeof data[0] != "undefined"){
														if(typeof data[0].spanClass !="undefined"){
															htmlCode = '<select  id="select'+${it}+id+'" class="form-control span'+data[0].spanClass+'">';
														}
													}
													$.each(data, function(index, value) {
													    if(value.sSelected){
													        htmlCode += '<option value="'+value.sId+'" selected>'+value.sValue+'</option>';
													    } else{
													        htmlCode += '<option value="'+value.sId+'">'+value.sValue+'</option>';
													    }
													});
													htmlCode +='</select>';
												</c:when>
												</c:choose>
												return htmlCode;
											},
											"createdCell": function (nTd, sData, oData, iRow, iCol) {
										         jQuery(nTd).attr('columnType', '${listColumnType}');
										         <c:if test="${listColumnType == 'checkBox'}">
										         	jQuery(nTd).css('text-align', 'center');
												</c:if>
												<c:if test="${listColumnType == 'datepicker'}">
                                                	jQuery(nTd).addClass('tagDatePicker');
                                                </c:if>
                                                <c:if test="${listColumnType == 'lov'}">
                                                    jQuery(nTd).css('white-space', 'nowrap');
                                                </c:if>
										     }
											}
										</c:when>
										<c:otherwise>
												{"targets":[${it}], "className": "${align}","type": '${listSType[it]}',<c:if test="${width != 'null'}">
													"width":"${width}",
													</c:if>"orderable":${listsortable[it]},
													<c:if test="${listDefaultVis[it] == 'Y'}">
													"visible":true
													</c:if>
													<c:if test="${listDefaultVis[it] == 'N'}">
													"visible":false
													</c:if>
													,
													"createdCell": function (nTd, sData, oData, iRow, iCol) {
												         jQuery(nTd).attr('columnType', '${listColumnType}');
														<c:if test="${wordBreak eq 'Y'}">
															jQuery(nTd).css('word-break','break-all');
														</c:if>
												     }
												}
									    </c:otherwise>
									</c:choose>
										<c:set var="it" value="${it + 1}" />

									</c:forEach>
			        ],
			         "rowCallback": function( nRow, aData, iDisplayIndex) {
			     		var obj = $(aData.id);
					    var oSettings = this.fnSettings();
			        	if((aData.id == null || aData.id=="") && aData.id!=0) {
			        		aData.id = randomInteger();
			        	}
     					var hrefLink=$(obj).attr("href");
			        	<c:if test="${serialNumberFlag == true && serverSideFlag == false}">
			        	    jQuery("td:first", nRow).html(nRow._DT_RowIndex +1);
			        	</c:if>
					    <c:if test="${serialNumberFlag == true && serverSideFlag == true}">
			                jQuery("td:first", nRow).html(oSettings._iDisplayStart+nRow._DT_RowIndex +1);
			        	</c:if>
			        	/*Code for setting Id to Row and Cell of Table*/
						nRow.id="tr_datatable"+getValueFromHtml(aData.id);
						var childNodes = nRow.childNodes;
						var currencyCode=getValueFromHtml(aData.${currencyCode});
						if(currencyCode == null || currencyCode=='undefined' || currencyCode == ''){
							currencyCode=tenantCurrencyISOCode
						}
						<c:set var="it" value="0" />
		        		<c:forEach items="${listColumnType}" var="listColumnType">
			        		<c:if test="${footerAva == true}">
				        		<c:if test="${listfooterValue[it] == 'sum'}">
									<c:if test="${(listColumnType == 'textAmount' ||listColumnType == 'amount') && listDefaultVis[it] == 'Y'}">
			        					var dataValue=isNaN(aData.${listProperty[it]})?removeFormatting(getValueFromHtml(aData.${listProperty[it]}))
						        				 :(""+aData.${listProperty[it]});
			        					sumCellAmount("<c:out value='${htmlTableId}'/>_f${it}",dataValue,iDisplayIndex);
			        					${htmlTableId}_numberRoundingAndFormatHelper.addFieldFormatInfo('<c:out value="${htmlTableId}"/>_f${it}',currencyCode,${htmlTableId}_numberRoundingAndFormatHelper.FIELD_TYPE_HTML);
			        				</c:if>
			        			</c:if>
	     					</c:if>
	     					formatCells(aData, childNodes, '${htmlTableId}', currencyCode, '${it}',
	     							${htmlTableId}_numberRoundingAndFormatHelper,hrefLink,'${listDefaultVis[it]}');
	     				 	<c:set var="it" value="${it + 1}" />
					</c:forEach>
						/* Code for setting Id to Row and Cell of Table*/
						${lfnRowCallback}
			       },
					"initComplete"  : function (settings, json) {
						  $('#<c:out value="${htmlTableId}"/>_filter input').addClass('search-query');
				      $('#<c:out value="${htmlTableId}"/>_length label').css('line-height','27px');
				      $('#<c:out value="${htmlTableId}"/>_length select').attr('id',"<c:out value='${htmlTableId}'/>_selectId");
							$('#${htmlTableId}_check_all').off('click').on('click',function(){
				        if(this.checked){
				          checkAllOnVisiblePage("<c:out value='${htmlTableId}'/>");
				        }else{
				          unCheckAllOnVisiblePage("<c:out value='${htmlTableId}'/>");
				        }
				      });
					    <c:if test="${footerAva == true}">
		   	       	<c:set var="it" value="0" />
		   					<c:forEach items="${listColumnType}" var="listColumnType">
		   					  <c:if test="${listfooterValue[it] == 'sum'}">
		   						  <c:choose>
												<c:when test="${listColumnType == 'amount'}">
														var totalSum  = new BigDecimal("0");
														$('#${htmlTableId} tbody tr').each( function()
														{
											    			var nTds = $('td', this);
											    			var abc = $('input', $(nTds[${it}])).val();
															if(abc==null || abc == 'undefined'){
											    			}else{
											    				totalSum = totalSum.add( new BigDecimal(checkNullValueAndReplaceItWithZero(removeFormatting(abc))));
											    			}
											    		});
														$('#<c:out value="${htmlTableId}"/>_f${it}').text(totalSum);
											   </c:when>
											</c:choose>
		   							</c:if>
		   							<c:set var="it" value="${it + 1}" />
		   					</c:forEach>
				    </c:if>
				    ${lfnInitComplete}
                    oTable_${htmlTableId} = this.api();
                                    ${htmlTableId}_numberRoundingAndFormatHelper.populate();
				    setTimeout(function(){
                    	   adjust${htmlTableId}ColumnSizing(oTable_${htmlTableId});
                    	 },400);

				   },
				   "footerCallback": function (nFoot, aaData, iStart, iEnd, aiDisplay ) {
					   ${lfnFooterCallback}
				   },
				   "infoCallback" : function(settings, start, end, max, total, pre){
				        ${lfnInfoCallback};
				   },
				    "drawCallback": function( oSettings ) {
				      oTable_${htmlTableId} = this.api();
				      if(oSettings.bSorted && <c:out value="${serverSideFlag}"/>) {
				        oSettings.oFeatures.bServerSide=true;
                        if(ValidatorUtils.isNotEmpty(oTable_${htmlTableId})) {
                          oTable_${htmlTableId}.context[0].oApi._fnProcessingDisplay(oSettings,false);
				        }
				      }
				    	$('#<c:out value="${htmlTableId}"/>_check_all').prop('checked',false);
				    	eventOnLoadForDate();
				    	${htmlTableId}_triggerRetainedDataSelection(oTable_${htmlTableId});
				    	checkUncheckCheckAll('<c:out value="${htmlTableId}"/>');
				    	removeIdFromCloneElementIn${htmlTableId}DataTableScrollBody();
				    	${htmlTableId}_numberRoundingAndFormatHelper.populate();
				        var firstRadioId = ${htmlTableId}_getFirstRadioId(oTable_${htmlTableId});
				        if(ValidatorUtils.isNotEmpty(firstRadioId) && <c:out value="${radiodef}"/>){
				            var radioName = jQuery("#"+firstRadioId).attr('name');
				            var radioGroupType = radioName.substring(5,6);
				                 if(radioGroupType!='H' && ValidatorUtils.isNotEmpty(firstRadioId) && ValidatorUtils.isNotEmpty(${htmlTableId}_selectedRadioIdOnLoad)){
				                     jQuery("#"+firstRadioId).prop("checked",true);
				                  }
				        }
				        jQuery('#${htmlTableId}_paginate .pagination').find('li.disabled,.active').find('a').each(function(){
				          jQuery(this).removeAttr("href");
				        });
				    	${lfnDrawCallback}
                        ${htmlTableId}disablePrevNextLinkIfApplicable();

				    }
	}).api();

  <c:set var="colVisIncluded" value="false" />
  var colvis = new $.fn.dataTable.Buttons( oTable_${htmlTableId}, {
      buttons: [
        {
     	   	  "extend"         : "colvis",
     	   	  "name"           : "colvis",
     	   	  "text"           : getMessage('label.show.hide.columns'),
     	   	  "postfixButtons" : [ 'colvisRestore' ],
     	   	  "columns" : [
     	  	     				<c:set var="it" value="0" />
     	  	            <c:set var="firstIndexIncluded" value="false" />
     	  	     				<c:forEach items="${listColumnType}" var="listColumnType">
     	  	     					<c:if test="${listColumnType != 'hidden' && listColvis[it] != 'N'}">
     	  	                <c:if test="${!((checkBoxdef == true || serialNumberFlag == true) && it==0)}">
     	  	                  <c:if test="${firstIndexIncluded == true}">,</c:if>${it}
     	  	                  <c:set var="firstIndexIncluded" value="true" />
     	  	                </c:if>
     	  	     					</c:if>
     	  	     					<c:set var="it" value="${it + 1}" />
     	  	     				</c:forEach>
     	  	     				]
     	   }
      ]
  });

  <c:if test="${tableTools == true && colVisIncluded==false && ColvisFlag == 'Y'}">
    colvis.container().appendTo('#${htmlTableId}_wrapper #colvis');
    jQuery('#${htmlTableId}_wrapper #colvis').attr("style","padding:0px 6px;").addClass("pull-right");
    <c:set var="colVisIncluded" value="true" />
    jQuery("#${htmlTableId}_wrapper .dataTables_length").addClass("pull-right").css({
      'position':'absolute',
      'right':'155px'
    });
  </c:if>
  oTable_${htmlTableId}.button('csv:name').nodes().attr('id','${htmlTableId}_csv');
  oTable_${htmlTableId}.button('copy:name').nodes().attr('id','${htmlTableId}_copy');
  oTable_${htmlTableId}.button('pdf:name').nodes().attr('id','${htmlTableId}_pdf');
  oTable_${htmlTableId}.button('excel:name').nodes().attr('id','${htmlTableId}_excel');
  oTable_${htmlTableId}.button('colvis:name').nodes().attr('id','${htmlTableId}_colvis');
  $('#${htmlTableId}_wrapper input[type="search"]').prop("id","filter_${htmlTableId}");

}


function updateFooterValuesForEmptyData(){
	<c:if test="${footerAva == true}">
		<c:set var="it" value="0" />
		<c:forEach items="${listColumnType}" var="listColumnType">
				<c:if test="${listfooterValue[it] == 'sum'}">
					$('#<c:out value="${htmlTableId}"/>_f${it}').text('');
				</c:if>
				<c:set var="it" value="${it + 1}" />
		</c:forEach>
</c:if>
}

function ${htmlTableId}_triggerRetainedDataSelection(oTable) {
	if(${htmlTableId}_retained_data.size>0) {
		var tableId_keyPropertyList = parseJqueryJSON(${htmlTableId}_retainPropertyList).keyPropertyList;
		jQuery.each(jQuery('tr td input[type=checkbox], tr td input[type=radio]'), function(key, value) {
			var rowIndex = jQuery('#'+value.id).closest('tr').index();
      var colIndex = jQuery('#'+value.id).closest('td').index();
			var rowData = oTable_${htmlTableId}.row(rowIndex).renderedData().toArray()[0];
      var elementName = jQuery('#'+value.id).attr('name');
      var radioGroupType = jQuery('#'+value.id).prop("type")=="radio"?elementName.substring(5,6):"";
      var radioKeyIndex = "H"==radioGroupType?rowIndex:colIndex;
			var selectedRadioRetainDataKey = radioGroupType+radioKeyIndex+"_"+value.id;
			var uniqueKey = "";
			if(ValidatorUtils.isNotEmpty(${htmlTableId}_retainPropertyList)){
			  jQuery.each(parseJqueryJSON(${htmlTableId}_retainPropertyList).keyPropertyList, function(key, value) {
				  uniqueKey = uniqueKey + getValueFromHtml(rowData[value]);
	  	  });
			}
			if((jQuery('#'+value.id).prop("type")=="checkbox" && ${htmlTableId}_retained_data.has(uniqueKey)) ||
         (jQuery('#'+value.id).prop("type")=="radio" && ${htmlTableId}_selected_radio_retained_data.has(selectedRadioRetainDataKey))) {
				jQuery('#'+value.id).prop('checked', true);
			}
		});
	}
}

function load${htmlTableId}DataTable(loadUrl){
	${htmlTableId}_empty_search_flag = true;
	$("#<c:out value='${htmlTableId}'/>_errorDiv").hide();
	if(ValidatorUtils.isNotEmpty(loadUrl))
	{
		loadUrl = "${pageContext.request.contextPath}"+loadUrl;
	}
	else
	{
		loadUrl = null;
	}
	if(${htmlTableId}_numberRoundingAndFormatHelper == null)
	{
		${htmlTableId}_numberRoundingAndFormatHelper = new NumberRoundingAndFormatHelper(amountFormatWithoutPrecision,getContextPath(),groupingSeperator,decimalSeperator,currencyFormatInfo);
	}
	call${htmlTableId}Datatable(loadUrl,'URL');

	 if(<c:out value="${radiodef}"/>) {
		 ${htmlTableId}_triggerDefaultSelectedRadioClickEvent(oTable_${htmlTableId});
	 }
	 ${htmlTableId}_empty_search_flag = false;
}
function load${htmlTableId}DataTableAaData(aaData){
	var oSettings = oTable_${htmlTableId}.settings();
	aaData = validateDataForMissingProperties(oSettings,aaData);

	$("#<c:out value='${htmlTableId}'/>_errorDiv").hide();
	if(aaData==""){
		<c:if test="${mblankPopUp == true}">
			datatableWarnAlert.showLmsAlert('<spring:message code="${mblankKey}" />','<spring:message code="lbl.info" />',null,"${mfocusField}");
		</c:if>
	}
	else{
		if(${htmlTableId}_numberRoundingAndFormatHelper == null)
		{
			${htmlTableId}_numberRoundingAndFormatHelper = new NumberRoundingAndFormatHelper(amountFormatWithoutPrecision,pageContextPath,groupingSeperator,decimalSeperator,currencyFormatInfo);
		}
		call${htmlTableId}Datatable(aaData,'DATA');
	}

	if(<c:out value="${radiodef}"/>) {
		${htmlTableId}_triggerDefaultSelectedRadioClickEvent(oTable_${htmlTableId});
	}
}

jQuery.fn.dataTable.Api.register( 'rows().renderedData()', function () {
  var api = this;
  var mentionedColumnDetails = api.settings().init().columns;
  var actualRowsData = api.rows().data();
  var columnDetails = [];
  var columnindex = 0;
  for(key in actualRowsData[0]){
    columnDetails.push({"data":key, "index":columnindex});
    columnindex++;
  }
  
  return actualRowsData.length==0?actualRowsData: this.iterator( 'row', function ( context, index ) {
	  var rowDataObject = {};
      var node = this.row(index).node();
      if(ValidatorUtils.isNotEmpty(node)){
    	var rowData = this.row(index).data();
        var renderedDataArray = this.cells(node,"").render("display");
        var nTds = jQuery(">td",node);
        var cellIndexesFetched = [];
        jQuery(nTds).each(function(cellIndex,cellObject){
          var cellIndexDetails = api.cell(cellObject).index();
          var columnsObject = mentionedColumnDetails[cellIndexDetails["column"]];
          var renderedDataValue = jQuery.type(renderedDataArray[cellIndexDetails["column"]]) =='number' || (jQuery.type(renderedDataArray[cellIndexDetails["column"]]) == 'string' && renderedDataArray[cellIndexDetails["column"]].indexOf('type="checkbox"')== -1) ?
                                    renderedDataArray[cellIndexDetails["column"]] :
                                    jQuery(renderedDataArray[cellIndexDetails["column"]]).prop('nodeName')=='LABEL' ?
                                        jQuery(jQuery(renderedDataArray[cellIndexDetails["column"]]).find('input'))[0].outerHTML :
                                        renderedDataArray[cellIndexDetails["column"]];
          rowDataObject[columnsObject["data"]] = renderedDataValue;
          cellIndexesFetched.push(cellIndexDetails["column"]);
        });

        var fetchedColumnDetails = [];
        for(key in rowDataObject){
          fetchedColumnDetails.push(key);
        }

        jQuery(columnDetails).each(function(columnIndex,columnObject){
          if(fetchedColumnDetails.indexOf(columnObject["data"])==-1){
            rowDataObject[columnObject["data"]] = rowData[columnObject["data"]];
          }
        });
      }
      return rowDataObject;
  } );
});

function validateForNestedProperties(property,aoData){
  var nestedPropertyDoesNotExist = false;
  var nestedPropertyArray = property.split(".");
  var missingNestedProperties=[];
  if(nestedPropertyArray.length>1){
    for(var index=1;index<nestedPropertyArray.length;index++){
      if(!aoData[nestedPropertyArray[0]].hasOwnProperty(nestedPropertyArray[index])){
        if(nestedPropertyArray[index].indexOf('.')!=-1){
          validateForNestedProperties(nestedPropertyArray[index],aoData);
        }else if(missingNestedProperties.indexOf(nestedPropertyArray[index]) == -1){
          missingNestedProperties.push(nestedPropertyArray[index]);
    			nestedPropertyDoesNotExist = true;
        }
    	}
    }
  }
  return nestedPropertyDoesNotExist;
}

function validateDataForMissingProperties(oSettings,aaData)
{
	var allPropertiesAvailable = true;
	var missingProperties =[];
	if(aaData != null) {
		for(var index=0 ; index<aaData.length ; index++ ) {
	    if(oSettings!=null && typeof(oSettings)!='undefined'){
	      var columns = typeof oSettings.init!="undefined" ? oSettings.init().columns : oSettings.aoColumns;
			  $.map(columns, function(node,columnIndex) {
			    if(!aaData[index].hasOwnProperty(node["data"])) {
			      var propertyDoesNotExist = node["data"].indexOf('.')!=-1?validateForNestedProperties(node["data"],aaData[index]):true;
			    	if(propertyDoesNotExist && missingProperties.indexOf(node["data"]) == -1){
			    	  missingProperties.push(node["data"]);
			    	  allPropertiesAvailable = false;
			    	}
			    }
			  });
	    }
		}
	}

	if(!allPropertiesAvailable) {
		var missingPropertyString = '';
		for (var argIndex = 0; argIndex < missingProperties.length; argIndex++) {
			missingPropertyString = missingPropertyString + missingProperties[argIndex] +', ';
		}
		message_arguments =[];
		message_arguments[0] = missingPropertyString.substring(0, missingPropertyString.length-2);
		datatableErrorAlert.showLmsAlert(getMessage('msg.00002518',message_arguments),getMessage('lbl.error'));
		aaData = null;
	}
	return aaData;
}


function sumCellAmount(htmlTableId,dataValue,iDisplayIndex){
	if(iDisplayIndex == 0){
	  	$('#'+htmlTableId).text(0);
	}
	if(ValidatorUtils.isEmpty(dataValue)){
		dataValue = 0;
	}
	if(jQuery.isNumeric(dataValue)){
	  if(typeof dataValue!='undefined' && dataValue!=null){
	  	dataValue = removeFormatting(dataValue);
	  }
	  dataValue = new BigDecimal(checkNullValueAndReplaceItWithZero(dataValue));
	  var aa = $('#'+htmlTableId).text();
	  if(aa !='' && aa!=null && typeof aa !='undefined'){
	  	aa = removeFormatting(aa);
	  	var bb = new BigDecimal(checkNullValueAndReplaceItWithZero(aa));
	  	dataValue = dataValue.add(bb);
	  }
	  $('#'+htmlTableId).text(dataValue);
	}
}

function replaceIdOfLOVInHtml(htmlCode, currentLovId, newLovId) {
	var htmlCodeEle = jQuery(htmlCode);
	var childrenWithOldId = jQuery(htmlCode).find("[id^="+currentLovId+"]").toArray();
	for (var  i =0; i<childrenWithOldId.length; i++) {
		var domId = jQuery(childrenWithOldId[i]).attr("id");
		if (typeof htmlCodeEle.find("#"+domId).attr("name") !='undefined' && htmlCodeEle.find("#"+domId).filter("[name^="+currentLovId+"]").length > 0) {
			var name = jQuery(childrenWithOldId[i]).attr("name");
			name = name.replace(currentLovId, newLovId);
			htmlCodeEle.find("#"+domId).attr("name", name);
		}
		var newdomId = domId.replace(currentLovId, newLovId);
		htmlCodeEle.find("#"+domId).attr("id", newdomId);
	}
	if (htmlCodeEle.find("span").filter("[for^="+currentLovId+"]") > 0) {
        var spanFor = htmlCodeEle.find("span").filter("[for^="+currentLovId+"]").attr("for");
    	htmlCodeEle.find("span").filter("[for^="+currentLovId+"]").attr("for", spanFor.replace(currentLovId, newLovId));
    }
	var result = "";
	for (var  i =0;i<htmlCodeEle.length;i++) {
		if(typeof htmlCodeEle[i].outerHTML != 'undefined') {
			htmlCodeEle[i].id = htmlCodeEle[i].id != "" ? htmlCodeEle[i].id.replace(currentLovId, newLovId) : htmlCodeEle[i].id;
			result += htmlCodeEle[i].outerHTML;
		}
	}
	return result;
}

function bindEventsOnLovInTable(newId, lovDataMap, lovPropertyData) {

	lovPropertyData = (typeof lovPropertyData == 'undefined' || lovPropertyData == null ) ? {} : lovPropertyData;
	if(lovDataMap.viewMode == 'true') {
		jQuery("#"+newId+"_image").children().css('pointer-events','none');
		jQuery("#"+newId+"_image").css('pointer-events','none');
	}
	if(lovDataMap.required == 'true') {
	    jQuery("#"+newId+"-control-group").removeClass("nonMandatory");
	    jQuery("#"+newId).addClass("required");
	}
	applyTooltip(newId, lovDataMap.alignToolTip);
	$(document).off('shown','#'+newId+'-control-group .tooltip').on('shown', '#'+newId+'-control-group .tooltip',function(){
		var currentElementId = newId+'-control-group';
		var toolTipDivElement = jQuery('#'+currentElementId).find('.tooltip');
		if(typeof jQuery(toolTipDivElement)[0]!='undefined'){
			var toolTipLeftMargin = parseInt(jQuery(toolTipDivElement)[0].style.left)+30;
			jQuery(toolTipDivElement).css("left",toolTipLeftMargin+'px');
			jQuery(toolTipDivElement)[0].style.left = toolTipLeftMargin+'px';
		}
	});
	jQuery('p.text-danger').css("font-size","15px");
	applyClickOnLOVSearchImage(newId, lovDataMap.preValidationMethod, lovDataMap.validationFailureMethod, lovDataMap.filterDataMethod,
	                            lovDataMap.postExecutionScript, lovDataMap.lovKey, newId+'_hidden', lovDataMap.shownModalEvenWithValue);

    applyChangeOnLOVSearchImage(newId, lovDataMap.preValidationMethod, lovDataMap.validationFailureMethod, lovDataMap.filterDataMethod,
                                lovDataMap.postExecutionScript, lovDataMap.lovKey, newId+'_hidden');

    if (typeof lovPropertyData.hiddenValue != 'undefined'  && typeof lovPropertyData.displayValue != 'undefined') {
        if (lovPropertyData.displayValue != null && lovPropertyData.displayValue != "") {
            if (lovDataMap.viewMode == 'true' && lovDataMap.showTextAreaInViewMode == 'true')
                jQuery("#" + newId).html(lovPropertyData.displayValue);
            else
                jQuery("#" + newId).attr("value", lovPropertyData.displayValue);
        }
        setHiddenValue(lovPropertyData.hiddenValue, newId+'_hidden');
    }
}

function formatCells(aData, childNodes, htmlTableId, currencyCode, it, numberRoundingAndFormatHelper,hrefLink,defaultVisibility){
	if(childNodes[it]!="undefined" && childNodes[it]!= null){
		if(aData.lmscolor != null && aData.lmscolor != "")
			$(childNodes[it]).css('background-color', aData.lmscolor);
		var columnType = jQuery(childNodes[it]).attr('columnType');
		if('hidden'!=columnType){
		 	if(hrefLink == null || hrefLink == 'undefined'){
				childNodes[it].id='td_datatable_'+htmlTableId+it+''+getValueFromHtml(aData.id);
			}
			else{
				childNodes[it].id='td_datatable_'+htmlTableId+it+''+getValueFromHtmlLink(aData.id);
			}
			
			if('textAmount'===columnType){
				numberRoundingAndFormatHelper.addFieldFormatInfo(childNodes[it].id,currencyCode,numberRoundingAndFormatHelper.FIELD_TYPE_HTML);
			}else if('rate'===columnType){
				numberRoundingAndFormatHelper.addFieldFormatInfo(childNodes[it].id,null,numberRoundingAndFormatHelper.FIELD_TYPE_HTML,numberRoundingAndFormatHelper.NUMBER_INTEREST_RATE_TYPE);
			}else if('amount'===columnType){
				numberRoundingAndFormatHelper.addFieldFormatInfo("amount"+it+childNodes[it].id,currencyCode);
			}else{
				numberRoundingAndFormatHelper.removeFieldFormatInfo(childNodes[it].id);
			}
		}
	}
}

function filter${htmlTableId}DataBasedOnInput(currentObject,event){
	var item = jQuery(currentObject);
	var oSettings = jQuery("#<c:out value='${htmlTableId}'/>").dataTable().fnSettings();
	var serverSideOriginal = oSettings.oFeatures.bServerSide;
	var iDisplayStart = oSettings._iDisplayStart;
	var iDisplayLength = oSettings._iDisplayLength;
	var rowReadObject = oSettings._rowReadObject;
	oSettings.oFeatures.bServerSide=false;
    if (jQuery(item).val().length >= 2 || (ValidatorUtils.isNotEmpty(jQuery(item).attr("oldValue")) && jQuery(item).attr("oldValue").length>2)) {
      searchTerm = jQuery(item).val();
      if(ValidatorUtils.isEmpty(searchTerm)){
        oSettings.oFeatures.bServerSide=serverSideOriginal;
      }
      jQuery(item).attr("oldValue",searchTerm);
      oTable_${htmlTableId}.search(searchTerm).draw();
    }else if([8,46].indexOf(event.keyCode)!=-1){
      oTable_${htmlTableId}.search("").draw();
    }
    oSettings.oFeatures.bServerSide=serverSideOriginal;
    oSettings._iDisplayStart = iDisplayStart;
    oSettings._iDisplayLength = iDisplayLength;
    oSettings._rowReadObject = rowReadObject;
    if(serverSideOriginal && jQuery(item).val().length <2 && [8,46].indexOf(event.keyCode)!=-1){
    	oSettings._iRecordsDisplay = oSettings.fnRecordsDisplay();
        oSettings.aoDrawCallback.filter(function(object,index){
          if(object["sName"]=="pagination"){
        	object["fn"].call(this,oSettings);
          }
          return true;
        });
    }
}

$("#<c:out value='${htmlTableId}'/>_div").hide();
<c:if test="${loadByDefault == true}">
	load${htmlTableId}DataTable('${loadGridUrl}');
	$("#<c:out value='${htmlTableId}'/>_div").show();
	clearTableIfNoData(oTable_${htmlTableId});
</c:if>

function adjust${htmlTableId}ColumnSizing(datatable){
  if(typeof datatable!='undefined'){
       datatable.columns.adjust();
  }
}

var ${htmlTableId}_selectedRadioIdOnLoad = "";
function ${htmlTableId}_triggerDefaultSelectedRadioClickEvent(datatable){
	if(ValidatorUtils.isNotEmpty(datatable) && (${htmlTableId}_lms_data!=null || ${htmlTableId}_lms_url!=null)){
		var radioToBeSelectedForVerticalGrouping = true;
    	jQuery('#${htmlTableId} tbody tr td').each(function(){
    		if(jQuery(this).attr('columnType')=='radio' && typeof datatable.cell(this) !='undefined'){
				var cellPosition = datatable.cell(this).index();
				if(ValidatorUtils.isNotEmpty(cellPosition) && cellPosition.column=="${defaultSelectedRadioIndex}"){
					var id=jQuery(jQuery(this).html()).attr('id');
					var radioName = jQuery(jQuery(this).html()).attr('name');
					var radioGroupType = radioName.substring(5,6);
					if(radioGroupType=='H' || radioToBeSelectedForVerticalGrouping){
                      ${htmlTableId}_selectedRadioIdOnLoad = id;
					  jQuery('#'+id).prop('checked',true);
					  radioToBeSelectedForVerticalGrouping = false
					  jQuery('#'+id).click();
					}
				}
    		}
		});
	}
}

function ${htmlTableId}_getFirstRadioId(datatable){
  var firstRadioId = "";
  if(ValidatorUtils.isNotEmpty(datatable) && (${htmlTableId}_lms_data!=null || ${htmlTableId}_lms_url!=null)){
    		var radioToBeSelectedForVerticalGrouping = true;
        	jQuery('#${htmlTableId} tbody tr td').each(function(){
        		if(jQuery(this).attr('columnType')=='radio' && typeof datatable.cell(this) !='undefined' && ValidatorUtils.isEmpty(firstRadioId)){
    				var cellPosition = datatable.cell(this).index();
    				if(ValidatorUtils.isNotEmpty(cellPosition) && cellPosition.column=="${defaultSelectedRadioIndex}"){
    					var id=jQuery(jQuery(this).html()).attr('id');
    					var radioName = jQuery(jQuery(this).html()).attr('name');
    					var radioGroupType = radioName.substring(5,6);
    					if(radioGroupType=='H' || radioToBeSelectedForVerticalGrouping){
                          firstRadioId = id;
    					}
    				}
        		}
    		});
    	}
  return firstRadioId;
}

function eventOnLoadForDate(){
		var elementId='div[id^=datepicker_]';
		var listOnLoadId=[elementId];
		executeOnLoad(listOnLoadId);
		executeOnDomNodeInserted(listOnLoadId);
}

function removeIdFromCloneElementIn${htmlTableId}DataTableScrollBody(){
		 jQuery("#${htmlTableId}_wrapper .dataTables_scrollBody tfoot tr th").find('input,span,div,a').each(function(){
		   if(typeof jQuery(this).attr('id')!='undefined'){
		     jQuery(this).removeAttr('id').attr('tabindex',-1);
		   }
		 });
		 
		 jQuery("#${htmlTableId}_wrapper .dataTables_scrollBody thead tr th").find('input,span,div,a').each(function(){
		   if(typeof jQuery(this).attr('id')!='undefined'){
		     jQuery(this).removeAttr('id').prop('tabindex',-1);
		   }
     });
} 

function ${htmlTableId}disablePrevNextLinkIfApplicable(){
  jQuery('#${htmlTableId}_wrapper .paginate_button.disabled').find('a').each(function(){
    jQuery(this).prop('tabindex',-1);
  });
  jQuery('#${htmlTableId}_wrapper .paginate_button').filter(':not(".disabled")').find('a').each(function(){
    jQuery(this).prop('href','javascript:void(0)');
  });
}

jQuery(document).on('scroll',function(){
  if(jQuery('#${htmlTableId}_wrapper').is(":visible")){
    removeIdFromCloneElementIn${htmlTableId}DataTableScrollBody();
    ${htmlTableId}disablePrevNextLinkIfApplicable();
  }
}); 


</script>