<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ attribute name="htmlTableId" required="true" type="java.lang.String"
    description="DOM id of the HTML table."%>
<%@ attribute name="data" required="true" rtexprvalue="true"
    type="java.util.Collection"
    description="Collection of data used to populate the table."%>
<%@ attribute name="editable" required="false" rtexprvalue="true"
    type="java.lang.Boolean"
    description="Specifies if the table is inline editable"%>
<%@ attribute name="childTable" required="false" rtexprvalue="true"
    type="java.lang.Boolean"
    description="Specifies if the table is a child table, if yes opens the form in modal window"%>
<%@ attribute name="dataObjectId" required="true"
    type="java.lang.String" rtexprvalue="false"
    description="Name of the object representing the current row."%>
<%@ attribute name="loadDataUrl" required="true" type="java.lang.String"
    description="Name of the object representing the current row."%>
<%@ attribute name="disableHtmlEncodingForloadDataUrl" required="false" type="java.lang.Boolean"
    description="By default loadDataUrl will be escaped, this can be disabled with disableHtmlEncodingForloadDataUrl" %>

<%@ attribute name="serverSide" required="false"
    type="java.lang.Boolean"
    description="Specifies if the sorting and searching is server side"%>
<%@ attribute name="hrefBoolType" required="false"
    type="java.lang.Boolean"
    description="Specifies if the first column of table should have hyperlink or not"%>
<%@ attribute name="cssClass" required="false" type="java.lang.String"
    rtexprvalue="false" description="CSS class(es) of the HTML table."%>
<%@ attribute name="style" required="false" type="java.lang.String"
    rtexprvalue="false" description="CSS style of the HTML table."%>
<%@ attribute name="htmlRowIdPrefix" required="false"
    type="java.lang.String"
    description="String which is prepended to the htmlRowIdBase attribute."%>
<%@ attribute name="htmlRowIdBase" required="false"
    type="java.lang.String"
    description="Useful if you want each row has a DOM id. This attribute is evaluated as an object's property of the data attribute."%>
<%@ attribute name="htmlRowIdSufix" required="false"
    type="java.lang.String"
    description="String which is appended to the htmlRowIdBase attribute."%>
<%@ attribute name="info" required="false" rtexprvalue="true"
    type="java.lang.Boolean"
    description="Enable or disable the table information display. This shows information about the data that is currently visible on the page, including information about filtered data if that action is being performed."%>
<%@ attribute name="paginate" required="false" rtexprvalue="true"
    type="java.lang.Boolean" description="Enable or disable pagination."%>
<%@ attribute name="lengthPaginate" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="If paginate is enabled, allows the end user to select the size of a formatted page from a select menu (sizes are 10, 25, 50 and 100)."%>
<%@ attribute name="filter" required="false" rtexprvalue="true"
    type="java.lang.Boolean"
    description="Enable or disable filtering of data."%>
<%@ attribute name="labels" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Base URL of an AJAX loaded file which contains all the labels used in tables. This attribute is the value for the sUrl DataTable parameter.Warning This attribute is evaluated with &lt;c:url&gt;"%>
<%@ attribute name="extraConf" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Base URL of an AJAX loaded DataTable configuration file which is merged with the default configuration (thanks to the JQuery $.extend function). Warning This attribute is evaluated with &lt;c:url&gt;"%>
<%@ attribute name="extraFile" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Base URL of an AJAX loaded Javascript file which is then executed (thanks to the JQuery $.getScript function).Warning This attribute is evaluated with &lt;c:url&gt;"%>
<%@ attribute name="addButton" required="false" rtexprvalue="true"
    type="java.lang.Boolean"
    description="Enable or disable an add link on the top of the table."%>
<%@ attribute name="addButtonUrl" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="If addButton is enabled, used to fill href attribute of a link. Warning This attribute is evaluated with &lt;c:url&gt;"%>
<%@ attribute name="addButtonLabel" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="If addButton is enabled, used to fill the link description."%>
<%@ attribute name="addButtonCssClass" required="false"
    rtexprvalue="true" type="java.lang.String"
    description="If addButton is enabled, appended to the CSS attribute of the link."%>
<%@ attribute name="extraFilterId" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="DOM id of a HTML element you would see appear at the top of the table."%>
<%@ attribute name="extraFilterPosition" required="false"
    rtexprvalue="true" type="java.lang.String"
    description="If extraFilterId is enabled, position at the top of the table."%>
<%@ attribute name="extraFilterCssClass" required="false"
    rtexprvalue="true" type="java.lang.String"
    description="If extraFilterId is enabled, CSS class of the extra filter."%>
<%@ attribute name="autoWidth" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Enable or disable automatic column width calculation."%>
<%@ attribute name="deferRender" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Defer the creation of the table elements for each row until they are needed for a draw"%>
<%@ attribute name="jqueryUI" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Enable jQuery UI ThemeRoller support"%>
<%@ attribute name="processing" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Enable or disable the display of a 'processing' indicator when the table is being processed (e.g. a sort)."%>
<%@ attribute name="sort" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Enable or disable sorting of columns."%>
<%@ attribute name="sortClasses" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Enable or disable the addition of the classes 'sorting_1', 'sorting_2' and 'sorting_3' to the columns which are currently being sorted on."%>
<%@ attribute name="stateSave" required="false" rtexprvalue="true"
    type="java.lang.String" description="Enable or disable state saving."%>
<%@ attribute name="dom" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Specify exactly where in the DOM you want DataTables to inject the various controls it adds to the page."%>
<%@ attribute name="paginationType" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Choice between the two different built-in pagination interaction methods ('two_button' or 'full_numbers') which present different page controls to the end user."%>
<%@ attribute name="cookiePrefix" required="false" rtexprvalue="true"
    type="java.lang.String"
    description="Override the default prefix that DataTables assigns to a cookie when state saving is enabled."%>
<%@ attribute name="showPopover" required="false"
    type="java.lang.Boolean"
    description="Specifies if popover is to be displayed or not"%>
<%@ attribute name="viewMode" required="false" type="java.lang.Boolean"
    description="ViewMode Functionality in Data Table"%>
<%@ variable name-from-attribute="dataObjectId" alias="dataItem"
    variable-class="java.lang.Object" scope="NESTED"%>

<%-- ******************************************** --%>
<%-- Variables initialization --%>
<%-- ******************************************** --%>
<%-- Global ones --%>
<c:set var="delimitor" value="|" scope="request" />

<%-- Column ones --%>
<c:set var="titles" scope="request" />
<c:set var="styles" scope="request" />
<c:set var="headerStyles" scope="request" />
<c:set var="cssClasses" scope="request" />
<c:set var="headerCssClasses" scope="request" />
<c:set var="sortables" scope="request" />
<c:set var="hiddenList" scope="request" />
<c:set var="columnTypeList" scope="request" />
<c:set var="filterables" scope="request" />
<c:set var="filterTypes" scope="request" />
<c:set var="filterCssClasses" scope="request" />

<%-- Table ones --%>
<c:set var="colCounter" value="0" scope="request" />
<c:set var="dataTableSortProperty" value="[]" />
<c:set var="dataTableDisplayProperty" value="[]" />
<c:set var="customRowId"
    value="${not empty htmlRowIdPrefix or not empty htmlRowIdBase or not empty htmlRowIdSufix}" />
<c:set var="infoProperty" value="${empty info ? true : info}" />
<c:set var="paginateProperty"
    value="${empty paginate ? true : paginate}" />
<c:set var="lengthPaginateProperty"
    value="${empty lengthPaginate ? true : lengthPaginate}" />
<c:set var="filterProperty" value="${empty filter ? true : filter}" />
<c:set var="labelsProperty"
    value="${empty labels ? 'dataTables.default.french.txt' : labels}" />
<c:set var="extraConfProperty"
    value="${empty extraConf ? null : extraConf}" />
<c:set var="extraFileProperty"
    value="${empty extraFile ? null : extraFile}" />
<c:set var="addButtonProperty"
    value="${empty addButton or addButton eq false ? false : true}" />
<c:set var="addButtonUrlProperty"
    value="${empty addButtonUrl ? '' : addButtonUrl}" />
<c:set var="addButtonLabelProperty"
    value="${empty addButtonLabel ? 'Ajouter' : addButtonLabel}" />
<c:set var="addButtonCssClassProperty"
    value="${empty addButtonCssClass ? '' : addButtonCssClass}" />
<c:set var="cssClassProperty"
    value="${empty cssClass ? 'table table-striped table-bordered table-condensed' : cssClass}" />
<c:set var="cssStyleProperty" value="${empty style ? '' : style}" />
<c:set var="extraFilterIdProperty"
    value="${empty extraFilterId ? null : extraFilterId}" />
<c:set var="extraFilterPositionProperty"
    value="${empty extraFilterPosition ? 'right' : extraFilterPosition}" />
<c:set var="extraFilterCssClassProperty"
    value="${empty extraFilterCssClass ? '' : extraFilterCssClass}" />
<c:set var="autoWidthPreProperty"
    value="${empty autoWidth ? true : autoWidth}" />
<c:set var="deferRenderPreProperty"
    value="${empty deferRender ? false : deferRender}" />
<c:set var="jqueryUIPreProperty"
    value="${empty jqueryUI ? false : jqueryUI}" />
<c:set var="processingPreProperty"
    value="${empty processing ? false : processing}" />
<c:set var="sortPreProperty" value="${empty sort ? false : sort}" />
<c:set var="sortClassesPreProperty"
    value="${empty sortClasses ? true : sortClasses}" />
<c:set var="stateSavePreProperty"
    value="${empty stateSave ? true : stateSave}" />
<c:set var="showPopoverProperty"
    value="${empty showPopover or showPopover eq false ? false : true}" />
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="viewModeProperty"
    value="${empty viewMode or viewMode eq false ? false : true}" />
<c:choose>
	<c:when test="${not empty disableHtmlEncodingForloadDataUrl && disableHtmlEncodingForloadDataUrl eq true}">
	<c:set var="loadDataUrl" value="${loadDataUrl}" scope="page" />
	</c:when>
	<c:otherwise>
		<c:set var="loadDataUrl" scope="page" >
		    <c:out value='${loadDataUrl}' />
		</c:set>
	</c:otherwise>
</c:choose>


<c:set var="paginationTypeProperty"
    value="${empty paginationType ? 'two_button' : paginationType}" />
<c:set var="cookiePrefixPreProperty"
    value="${empty cookiePrefix ? 'SpryMedia_DataTables_' : cookiePrefix}" />

<%-- Evaluation du corps du tag table --%>
<c:set var="htmlVarManagement" value="true" scope="request" />
<jsp:doBody />
<c:set var="htmlVarManagement" value="false" scope="request" />

<%-- Evaluation du corps du tag table --%>
<c:set var="bodyManagement" value="true" scope="request" />
<jsp:doBody />
<c:set var="bodyManagement" value="false" scope="request" />

<c:set var="listStyle" value="${fn:split(style, delimitor)}"
    scope="request" />
<c:set var="listHeaderStyle"
    value="${fn:split(headerStyles, delimitor)}" scope="request" />
<c:set var="listClass" value="${fn:split(cssClasses, delimitor)}"
    scope="request" />
<c:set var="listHeaderClass"
    value="${fn:split(headerCssClasses, delimitor)}" scope="request" />
<c:set var="listFilterable" value="${fn:split(filterables, delimitor)}"
    scope="request" />
<c:set var="listFilterType" value="${fn:split(filterTypes, delimitor)}"
    scope="request" />
<c:set var="listFilterCssClass"
    value="${fn:split(filterCssClasses, delimitor)}" scope="request" />

<c:set var="noOfcolumns" value="0" />
<c:choose>
    <c:when test="${not empty minCharToBeginSearch}">
        <c:set var="minCharToBeginSearch" value="${minCharToBeginSearch}"/>
    </c:when>
    <c:otherwise>
        <c:set var="minCharToBeginSearch" value="3"/>
    </c:otherwise>
</c:choose>
<!--  dialog form form for child table -->
<div id="dialog-form-<c:out value='${childId}' />" class=" modal  fade">
    <div class="modal-header">
        <a class="close" onClick="closeChildDialog ('${childId}');" href="#">&times;</a>
        <a href="javascript:void(0);" onclick="minimizationModal(this);" class="minimizedLink"><i class="glyphicon glyphicon-resize-small"></i></a>
        <h3 class="txt-l">
            <span id="viewChild" class="block-no"> <spring:message
                    code="label.view" />&nbsp; <spring:message
                    code="label.${masterId}" text=" " />
            </span> <span id="editChild" class="block-no"> <spring:message
                    code="label.edit" />&nbsp; <spring:message
                    code="label.${masterId}" text=" " />
            </span> <span id="createChild"> <spring:message 
                    code="New" />&nbsp;<spring:message code="label.${masterId}" text=" " />
            </span>
        </h3>
        <div>

            <div class="row">
                <div class="col-sm-12">
                    <div class="row">
                        <div id="showHideRequiredFields" class="col-sm-7">
                            <p class="field switch">
                                <label for="radio1" id="allFieldsRadio" class="cb-enable selected"
                                    onclick="showChildModalAllFields()"><span><spring:message
                                            code="label.scheme.allFields" /></span>&nbsp|&nbsp </label> <label
                                    for="radio2" class="cb-disable" id="reqFieldsRadio"
                                    onclick="showChildModalMandatoryFields()"> <span><spring:message
                                            code="label.scheme.required" /> <span class="color-red">*</span>
                                </span>
                                </label>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div id="childModal<c:out value='${childId}' />" class="modal-body min-hgt250"></div>
    <div class="modal-footer clearfix">
        <span id="create_another_div_<c:out value='${childId}' />"> <input
            type="checkbox" id="create_another_<c:out value='${childId}' />" value="true" /> <spring:message code="label.createAnother" />
        </span> <a href="#" class="btn btn-primary"
            id="childModalWindowDoneButton<c:out value='${childId}' />" onclick="saveToSession()">
            <spring:message code="label.done" />
        </a> <a class="btn secondary" onClick="closeChildDialog('${childId}');"
            href="#"><spring:message code="label.cancel" /></a>
    </div>

</div>
<!-- end of dialog form -->

<!-- start of delete confirmation dialog form -->
<div
    class="modal  fade dialog-modal-window deleteRecord_<c:out value='${masterId}' />"
    id="deleteRecord_<c:out value='${masterId}' />">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal"
            aria-hidden="true" tabindex=3 onclick="closeConfirmDialog('${masterId}');">&times;</button>
        <a href="javascript:void(0);" onclick="minimizationModal(this);" class="minimizedLink"><i class="glyphicon glyphicon-resize-small"></i></a>
        <h3>
            <spring:message code="label.customer.remove.confirm"></spring:message>
        </h3>
    </div>
    <div class="modal-body">
        <p>
            <span class="fs-3"><spring:message
                    code="label.record.delete.confirmation"></spring:message></span>
        </p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-primary" tabindex=1 id="deleteButton_<c:out value='${masterId}' />"
            onclick="deleteTableRecord('${masterId}','${parentId}');"><spring:message
                code="label.customer.remove.confirm">
            </spring:message> </button> <button type="button" class="btn btn-primary"  id="deleteRecordButton_<c:out value='${masterId}' />"
            onclick="deleteRecord('${masterId}','${parentId}');"><spring:message
                code="label.customer.remove.confirm">
            </spring:message> </button> <a class="btn tableTag_deleteRecordCloseButton" id="deleteRecordCloseButton_<c:out value='${masterId}' />" href="#" tabindex=2 onclick="closeConfirmDialog('${masterId}');"><spring:message
                code="label.close"></spring:message> </a>
    </div>
</div>

<!-- end of delete confirmation dialog form -->

<div class="m-b10"></div>
<div class="row" id="commonButton_<c:out value='${masterId}' />" hidden="true">
    <c:forEach items="${actionConfiguration}" var="actionConfig"
        varStatus="s">
        <c:set var="actionId"
            value="${fn:replace(actionConfig.action,' ', '')}"></c:set>
        <c:if test="${!(actionId eq 'Edit') && !(actionId eq 'UnlockReason')}">
            <button class="btn btn-primary tableTag_<c:out value='${actionId}' />_BtnClass" type="button"
                id="<c:out value='${actionId}' /><c:out value='${masterId}' />Bttn" onclick="bttnActions(this)">
                <spring:message code="label.${actionId}" />
            </button>
        </c:if>
    </c:forEach>
</div>
<table id="<c:out value='${htmlTableId}' />" class="${cssClassProperty} tblOverFlowClass"
    data-masterId="<c:out value='${masterId}' />" data-parentId="<c:out value='${parentId}' />"
    style="${cssStyleProperty}">
    <thead>
        <tr>
            <th class = "export-exclude visibility-exclude"><input id="selectAll_<c:out value='${masterId}' />" type="checkbox" autocomplete="off"/> <span class="block-no"><spring:message code="label.selectall" /></span></th>
            <c:forTokens items="${titles}" delims="${delimitor}" var="title"
                varStatus="s">
                <c:set var="columnHeaderStyle"
                    value="${listHeaderStyle[s.count - 1]}" />
                <c:set var="columnHeaderClass"
                    value="${listHeaderClass[s.count - 1]}" />
                <c:choose>
                    <c:when
                        test="${not empty columnHeaderStyle and not empty columnHeaderClass}">
                        <th class="<c:out value='${columnHeaderClass}' />" style="<c:out value='${columnHeaderStyle}' />"><p><spring:message
                                code="${title}" /><p></th>
                    </c:when>
                    <c:when
                        test="${not empty columnHeaderStyle and empty columnHeaderClass}">
                        <th style="<c:out value='${columnHeaderStyle}' />"><p><spring:message
                                code="${title}" /></p></th>
                    </c:when>
                    <c:when
                        test="${empty columnHeaderStyle and not empty columnHeaderClass}">
                        <th class="<c:out value='${columnHeaderClass}' />"><p><spring:message
                                code="${title}" /></p></th>
                    </c:when>
                    <c:otherwise>
                        <th><p><spring:message code="${title}" /></p></th>
                    </c:otherwise>
                </c:choose>
                <c:set var="noOfcolumns" value="${s.count}" />
            </c:forTokens>
        </tr>
    </thead>
    <tbody>
        <%-- <c:set var="bodyManagement" value="true" scope="request" /> --%>
        <c:set var="rowIdList" />
        <%-- Iteration sur les LIGNES / Iteration on LINE --%>
        <c:forEach var="dataItem" items="${data}" varStatus="status">
            <c:set var="rowIdList"
                value="${rowIdList}${htmlRowIdPrefix}${dataItem[htmlRowIdBase]}${htmlRowIdSufix}${delimitor}" />
            <c:set var="properties" value="" scope="request" />
            <c:set var="contents" value="" scope="request" />
            <%-- <jsp:doBody /> --%>
            <c:set var="listProperty" value="${fn:split(properties, delimitor)}"
                scope="request" />
            <c:set var="listColumnType"
                value="${fn:split(columnTypeList, delimitor)}" scope="request" />
            <tr id="${dataItem['id']}">
                <td><input id="selectThis_<c:out value='${masterId}' />"
                    class="selectThis_<c:out value='${masterId}' />" name="selectThis_<c:out value='${masterId}' />"
                    type="checkbox" /></td>
                <%-- Iteration sur les COLONNES / Iteration on COLUMNS --%>
                <c:forTokens var="body" items="${contents}" delims="${delimitor}"
                    varStatus="s">
                    <c:set var="propertyName" value="${listProperty[s.count - 1]}"
                        scope="request" />
                    <c:set var="columnContent" value="${listContent[status.count - 1]}" />

                    <td></td>
                </c:forTokens>
            </tr>
        </c:forEach>
        <%-- <c:set var="bodyManagement" value="false" scope="request" /> --%>
    </tbody>
    <c:if test="${fn:contains(filterables, 'true')}">
        <tfoot>
            <tr>
                <c:forTokens var="filter" items="${filterables}"
                    delims="${delimitor}" varStatus="s">
                    <c:set var="columnFilterType"
                        value="${listFilterType[s.count - 1]}" />
                    <c:set var="columnFilterCssClass"
                        value="${listFilterCssClass[s.count - 1]}" />
                    <c:choose>
                        <c:when test="${filter and columnFilterType eq 'select'}">
                            <th><select class="form-control <c:out value='${columnFilterCssClass}' />"></select></th>
                        </c:when>
                        <c:when test="${filter and columnFilterType eq 'input'}">
                            <th><input type="text" value=""
                                class="form-control search-query <c:out value='${columnFilterCssClass}' />"></th>
                        </c:when>
                        <c:otherwise>
                            <th></th>
                        </c:otherwise>
                    </c:choose>
                </c:forTokens>
            </tr>
        </tfoot>
    </c:if>
</table>

<%-- Evaluation du corps du tag table --%>
<c:set var="sortManagement" value="true" scope="request" />
<jsp:doBody />
<c:set var="sortManagement" value="false" scope="request" />
<c:set var="dataTableSortProperty"
    value="[{'bSortable' : false}," />
<c:set var="sortableColCounter" value="0" />


<c:forTokens var="sortable" items="${sortables}" delims="${delimitor}"
    varStatus="s">
    <c:forTokens var="hidden" items="${hiddenList}" delims="${delimitor}"
        begin="${s.index}" end="${s.index}">
        <c:set var="dataTableSortProperty"
            value="${dataTableSortProperty}{'bSortable': ${sortable}, 'bVisible': ${!hidden} }," />
        <c:set var="it" value="${it + 1}" />

        <c:set var="sortableColCounter" value="${sortableColCounter + 1}" />
    </c:forTokens>
</c:forTokens>

<c:set var="dataTableSortProperty" value="${dataTableSortProperty}]" />

<style type="text/css">
td.tbl-right {
    text-align: right;
}
td.tbl-left {
    text-align: left;
}
</style>
<%-- Jira Id : PDDEV-14610/Jitendra Kumar
For external JS file table-tag.js --%>
<script>
    var actionConfigurations ={};
    var actionConfiguration={};
    var dataTableRecords ={};
    var dataTableRecord ={};
    var actionUrls ={};
    tableTagScriptInput = {};
    <c:forEach items="${actionConfiguration}" var="actionConfig" varStatus ="i"> 
    	actionConfiguration['actionUrl']='${actionConfig.actionUrl}';
    	actionConfiguration['action']= '${actionConfig.action}';
    	actionConfiguration['imagePath']= '${actionConfig.imagePath}';
    	actionConfiguration['titleKey']= '${actionConfig.titleKey}';
    	actionConfiguration['titleMessage'] = '<spring:message code="${actionConfig.titleKey}" text="${actionConfig.action}"/>';
    	actionUrls['${actionConfig.action}']='${actionConfig.actionUrl}';
    	actionConfigurations[${i.index}] = jQuery.extend(true, {}, actionConfiguration);
    </c:forEach>
    <c:forEach items="${dataTableRecords}" var="columnConfig" varStatus="i">
    	dataTableRecord['titleKey'] = '${columnConfig.titleKey}';
	    dataTableRecord['width'] = '${columnConfig.width}';
	    dataTableRecord['sortable'] = '${columnConfig.sortable}';
	    dataTableRecord['searchable'] = '${columnConfig.searchable}';
	    dataTableRecord['hidden'] =  '${columnConfig.hidden}'
	    dataTableRecord['dataField'] = '${columnConfig.dataField}';
	    dataTableRecord['columnCSS'] = '${columnConfig.columnCSS}'
	    dataTableRecord['columnClickFunction'] =  '${columnConfig.columnClickFunction}'	
	    dataTableRecord['isRegional'] =	 '${columnConfig.isRegional}'
	    dataTableRecord['columnType'] = '${columnConfig.columnType}';
	    dataTableRecord['regionalDataField'] = '${columnConfig.regionalDataField}';
	    dataTableRecord['isPercentage'] = '${columnConfig.isPercentage}';
	    dataTableRecords[${i.index}] = jQuery.extend(true, {}, dataTableRecord);
    </c:forEach>
	
       tableTagScriptInput = {
    		actionConfigurations: actionConfigurations,
    		dataTableRecords : dataTableRecords,
    		childTable:"<c:out value='${childTable}' />",
			htmlTableIdtBody:"#<c:out value='${htmlTableId}' /> tbody",
			tableRef:"oTable_<c:out value='${htmlTableId}' />",
			tableId:"#<c:out value='${htmlTableId}' />",
			masterId:"<c:out value='${masterId}' />",
			recordURL:"<c:out value='${recordURL}' />",
			viewModeProperty : "<c:out value='${viewModeProperty}' />",
			childId : "<c:out value='${childId}' />",
			htmlTableId : "<c:out value='${htmlTableId}' />",
			id : "<c:out value='${id}' />",
			extraConfProperty : "<c:url value='${extraConfProperty}' />",
			bServerSide:<c:out value='${serverSide}' />,
			bSort: <c:out value='${bSort}' />,
			bFilter: <c:out value='${bFilter}'/>,
			bInfo:<c:out value='${bInfo}'/>,
			bLengthChange: <c:out value='${bLengthChange}'/>,
			bJQueryUI: <c:out value='${bJQueryUI}'/>,
			loadDataUrl: "${loadDataUrl}",
			oTable_htmlTableId: "oTable_<c:out value='${htmlTableId}' />",
			columnTypeList:	"${columnTypeList}",
			delimitor: "${delimitor}",
			childAction_htmlTableId : "childAction_<c:out value='${htmlTableId}' />",
			oTable_htmlTableId : "oTable_<c:out value='${htmlTableId}' />",
			oTable_htmlTableId_Params :	'oTable_<c:out value='${htmlTableId}' />Params',
			htmlTableId_rowIdArray: "<c:out value='${htmlTableId}' />rowIdArray",
			table_dynamicVariables : {},
			parentId : "${parentId}",
			hrefBoolType : "${hrefBoolType}",
			extraFilterIdProperty: "${extraFilterIdProperty}",
			extraFilterCssClassProperty:"${extraFilterCssClassProperty}",
			extraFilterPositionProperty : "${extraFilterPositionProperty}",
			labelsProperty:"<c:url value='${labelsProperty}' />",
			autoWidth:"${autoWidth}",
			autoWidthProperty: ${autoWidthPreProperty},
			deferRender:"${deferRender}",
			deferRenderProperty: ${deferRenderPreProperty},
			jqueryUI:"${jqueryUI}",
			jqueryUIProperty: ${jqueryUIPreProperty},
			processing:"${processing}",
			processingProperty: ${processingPreProperty},
			sort:"${sort}",
			sortProperty: ${sortPreProperty},
			sortClasses:"${sortClasses}",
			sortClassesProperty: ${sortClassesPreProperty},
			stateSave: "${stateSave}",
			stateSaveProperty:"${stateSavePreProperty}",
			cookiePrefix: "${cookiePrefix}",
			cookiePrefixProperty:"${cookiePrefixPreProperty}",
			title: "<spring:message code="label.master.${htmlTableId}" />",
			editable: "${editable}",
			dataTableSortProperty : ${dataTableSortProperty},
			showPopoverProperty : "${showPopoverProperty}",
			viewModeProperty : "${viewModeProperty}",
			minCharToBeginSearch : "<c:out value='${minCharToBeginSearch}'/>",
			extraFileProperty :	"<c:url value='${extraFileProperty}' />",
			filterables : "${filterables}",
			customRowId : "${customRowId}",
			lbl_Previous : "<spring:message code="label.previous.page" />",
			lbl_Nxt :    "<spring:message code="label.next.page" />",
			lbl_ZeroRecords : "<spring:message code="label.no.records.found" />",
	        lbl_Processing : "<spring:message code="label.processing" />",
	        lbl_Info : "<spring:message code="label.datatable.info" />",
	 		lbl_Search : "<spring:message code="label.search" />",
	        lbl_LengthMenu : "<spring:message code="label.show.entries" />",
	        lbl_copy : "<spring:message code="label.copy" />",
	        lbl_csv : "<spring:message code="label.csv" />",
	        lbl_xls : "<spring:message code="label.xls" />",
	        lbl_pdf : "<spring:message code="label.pdf" />",
	        lbl_showHideCols : "<spring:message code="label.show.hide.columns" />", 
	        delimitor: "${delimitor}",
	        rowIdList: "<c:out value='${rowIdList}' />",
	        actionUrls: actionUrls,
	        oLanguage : "<c:url value='${labelsProperty}' />" 
		    }
</script>
<script src="${cdnUrl}/static-resources/neutrino/neutrino-common/js/tags/table-tag.js" ></script>

