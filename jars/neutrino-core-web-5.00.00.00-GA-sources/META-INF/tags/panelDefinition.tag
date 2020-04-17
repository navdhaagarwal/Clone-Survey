<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib"
   prefix="neutrino"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@tag import="com.nucleus.core.formsConfiguration.FormComponentType"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="items" type="java.util.List" required="true"%>
<%@ attribute name="formKey" required="true"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="path"%>
<style>
   table .columnDiv{
     min-width : 200px;
   }
   .dynamicFormTableDiv{
   overflow : auto;
   }	
</style>
<jsp:doBody var="bodyContents"></jsp:doBody>
<c:set var="multiSelectBoxFieldType" scope="page"
   value="<%=FormComponentType.MULTISELECTBOX%>" />
<c:set var="phoneFieldType" scope="page"
   value="<%=FormComponentType.PHONE%>" />
<c:set var="emailFieldType" scope="page"
   value="<%=FormComponentType.EMAIL%>" />
   
<c:if test="${empty path}">
	<c:set var="pathName" scope="page" value="uiComponents"></c:set>
</c:if>
<c:if test="${not empty path}">
	<c:set var="pathName" scope="page" value="${path}.uiComponents"></c:set>
</c:if>
<c:choose>
   <c:when test="${fn:contains(bodyContents,'<')}">
      ${bodyContents}
   </c:when>
   <c:otherwise>
      <div id="${id}_${formKey}-panel-form"
         class="row auto-width">
         <div class="row">
            <c:forEach items="${items}" var="singleItem"
               varStatus="panelItemStatus" step="1" begin="0">
               <c:choose>
                  <c:when
                     test="${singleItem.panelType eq 0 or singleItem.panelType eq 2}">
                     <!-- Render Panel Start -->
                     <!-- <div class="row formSep"> -->
                     <div id="${singleItem.panelKey}" class="row <c:if test="${not empty singleItem.panelHeader}">panel panel-default</c:if>">
                     <c:if test="${not empty singleItem.panelHeader}">
                     <div class="row panel-heading">
                        <b>
                           <spring:message code="${singleItem.panelHeader}"></spring:message>
                        </b>
                     </div>
                     </c:if>
                        <c:choose>
                           <c:when test="${singleItem.panelColumnLayout == 2}">
                           <div <c:if test="${not empty singleItem.panelHeader}">class="panel-body"</c:if>>
                              <c:forEach items="${singleItem.formFieldVOList}"
                                 var="fieldItem" varStatus="fieldItemStatus" step="1"
                                 begin="0">
                                 <c:if test="${fieldItemStatus.index % 2 eq 0}">
                                    <div class="row">
                                 </c:if>
                                 <div class="col-sm-6">
                                 <c:choose>
                                 <c:when
                                    test="${fieldItem.fieldType eq  multiSelectBoxFieldType}">
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].value"></c:set>
                                 </c:when>
                                 <c:when test="${fieldItem.fieldType eq  phoneFieldType}">
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].phoneNumberVO" />
                                 </c:when>
                                 <c:when test="${fieldItem.fieldType eq  emailFieldType}">
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].emailInfoVO" />
                                 </c:when>
                                 <c:otherwise>
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].value[0]"></c:set>
                                 <c:set var="actualValue"
                                    value="${items[panelItemStatus.index].formFieldVOList[fieldItemStatus.index].value[0]}"></c:set>
                                 </c:otherwise>
                                 </c:choose>
                                 <c:if test="${offlineTemplate eq false}">
                                 <neutrino:formField id="${fieldItem.id}"
                                    path="${actualPath}" fieldType="${fieldItem.fieldType}"
                                    value="${actualValue}"
                                    item="${fieldItem.item}"
                                    binderName="${fieldItem.binderName}"
                                    labelKey="${fieldItem.fieldLabel}"
                                    itemLable="${fieldItem.itemLabel}"
                                    itemValue="${fieldItem.itemValue}"
                                    expandableField="${fieldItem.expandableField}"
                                    mandatoryField="${fieldItem.mandatoryField}"
                                    includeSelect="${fieldItem.includeSelect}"
                                    fieldDataType="${fieldItem.fieldDataType}"
                                    dynamicFormToolTip="${fieldItem.toolTipMessage}"
                                    entityName="${fieldItem.entityName}"
                                    customeItemList="${fieldItem.fieldCustomOptionsVOList}"
                                    customeMessage="${fieldItem.customeLongMessage}"
                                    minFieldLength="${fieldItem.minFieldLength}"
                                    maxFieldLength="${fieldItem.maxFieldLength}"
                                    minFieldValue="${fieldItem.minFieldValue}"
                                    maxFieldValue="${fieldItem.maxFieldValue}"
                                    lovKey="${fieldItem.lovKey}"
                                    formKey="${formKey}" viewMode="${viewMode}"
                                    searchableColumns="${fieldItem.searchableColumns}"
                                    defDate="${fieldItem.defDate}"
                                    mobile="${fieldItem.mobile}"
                                    clonedRowStatus= "${panelItemStatus.index}"
                                    parentFieldKey="${fieldItem.parentFieldKey}"
                                    urlCascadeSelect="${fieldItem.urlCascadeSelect}"
                                    href="${fieldItem.href}" 
                                    functionLogic="${fieldItem.functionLogic}"
                                    authority="${fieldItem.authority}"
                                    parentFieldId="${fieldItem.parentFieldId}"
                                    parentColumn="${fieldItem.parentColumn}"
                                    errorMessageCode="${fieldItem.errorMessageCode}"
                                    mainFormDependant="${fieldItem.mainFormDependant}"
                                    parentKey="${fieldItem.parent}"
                                    disableKey="${fieldItem.disable}"
                                    />
                                 </c:if>
                                 <c:if test="${offlineTemplate eq true}">
                                 <neutrino:templateFormField id="${fieldItem.id}"
                                    path="${actualPath}" fieldType="${fieldItem.fieldType}"
                                    value="${actualValue}"
                                    binderName="${fieldItem.binderName}"
                                    labelKey="${fieldItem.fieldLabel}"
                                    itemLable="${fieldItem.itemLabel}"
                                    itemValue="${fieldItem.itemValue}"
                                    expandableField="${fieldItem.expandableField}"
                                    mandatoryField="${fieldItem.mandatoryField}"
                                    includeSelect="${fieldItem.includeSelect}"
                                    fieldDataType="${fieldItem.fieldDataType}"
                                    dynamicFormToolTip="${fieldItem.toolTipMessage}"
                                    entityName="${fieldItem.entityName}"
                                    customeItemList="${fieldItem.fieldCustomOptionsVOList}"
                                    customeMessage="${fieldItem.customeLongMessage}"
                                    minFieldLength="${fieldItem.minFieldLength}"
                                    maxFieldLength="${fieldItem.maxFieldLength}"
                                    minFieldValue="${fieldItem.minFieldValue}"
                                    maxFieldValue="${fieldItem.maxFieldValue}"
                                    formKey="${formKey}" viewMode="${viewMode}"
                                    searchableColumns="${fieldItem.searchableColumns}"
                                    defDate="${fieldItem.defDate}"
                                    mobile="${fieldItem.mobile}"
                                    href="${fieldItem.href}" 
                                    functionLogic="${fieldItem.functionLogic}"
                                    authority="${fieldItem.authority}"
                                    />
                                 </c:if>
                                 </div>
                                 <c:if test="${fieldItemStatus.index % 2 eq 1  || (fieldItemStatus.index +1 ) eq fn:length(singleItem.formFieldVOList)}">
                                 </div>
                                 </c:if>
                              </c:forEach>
                            </div>
                           </c:when>
                           <c:when test="${singleItem.panelColumnLayout == 3}">
                           <div <c:if test="${not empty singleItem.panelHeader}">class="panel-body"</c:if>>
                              <c:forEach items="${singleItem.formFieldVOList}" var="fieldItem"
                                 varStatus="fieldItemStatus" step="1" begin="0">
                                 <c:if test="${fieldItemStatus.index % 3 eq 0}">
                                    <div class="row">
                                 </c:if>
                                 <div class="col-sm-4">
                                 <c:choose>
                                 <c:when
                                    test="${fieldItem.fieldType eq  multiSelectBoxFieldType}">
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].value"></c:set>
                                 </c:when>
                                 <c:when test="${fieldItem.fieldType eq  phoneFieldType}">
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].phoneNumberVO" />
                                 </c:when>
                                 <c:when test="${fieldItem.fieldType eq  emailFieldType}">
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].emailInfoVO" />
                                 </c:when>
                                 <c:otherwise>
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].value[0]"></c:set>
                                 <c:set var="actualValue"
                                    value="${items[panelItemStatus.index].formFieldVOList[fieldItemStatus.index].value[0]}"></c:set>
                                 </c:otherwise>
                                 </c:choose>
                                 <c:if test="${offlineTemplate eq false}">
                                 <neutrino:formField id="${fieldItem.id}"
                                    path="${actualPath}" fieldType="${fieldItem.fieldType}"
                                    value="${actualValue}" binderName="${fieldItem.binderName}"
                                    item="${fieldItem.item}"
                                    labelKey="${fieldItem.fieldLabel}"
                                    itemLable="${fieldItem.itemLabel}"
                                    itemValue="${fieldItem.itemValue}"
                                    expandableField="${fieldItem.expandableField}"
                                    mandatoryField="${fieldItem.mandatoryField}"
                                    includeSelect="${fieldItem.includeSelect}"
                                    fieldDataType="${fieldItem.fieldDataType}"
                                    dynamicFormToolTip="${fieldItem.toolTipMessage}"
                                    entityName="${fieldItem.entityName}"
                                    customeItemList="${fieldItem.fieldCustomOptionsVOList}"
                                    customeMessage="${fieldItem.customeLongMessage}"
                                    minFieldLength="${fieldItem.minFieldLength}"
                                    maxFieldLength="${fieldItem.maxFieldLength}"
                                    minFieldValue="${fieldItem.minFieldValue}"
                                    maxFieldValue="${fieldItem.maxFieldValue}"
                                    lovKey="${fieldItem.lovKey}"
                                    formKey="${formKey}"
                                    viewMode="${viewMode}"
                                    searchableColumns="${fieldItem.searchableColumns}"
                                    defDate="${fieldItem.defDate}" mobile="${fieldItem.mobile}"
                                    clonedRowStatus=  "${panelItemStatus.index}"
                                    parentFieldKey="${fieldItem.parentFieldKey}"
                                    urlCascadeSelect="${fieldItem.urlCascadeSelect}"
                                    href="${fieldItem.href}" 
                                    functionLogic="${fieldItem.functionLogic}"
                                    authority="${fieldItem.authority}"
                                    parentFieldId="${fieldItem.parentFieldId}"
                                    parentColumn="${fieldItem.parentColumn}"
                                    errorMessageCode="${fieldItem.errorMessageCode}"
                                    mainFormDependant="${fieldItem.mainFormDependant}"
                                    parentKey="${fieldItem.parent}"
                                    disableKey="${fieldItem.disable}"
                                    />
                                 </c:if>
                                 <c:if test="${offlineTemplate eq true}">
                                 <neutrino:templateFormField id="${fieldItem.id}"
                                    path="${actualPath}" fieldType="${fieldItem.fieldType}"
                                    value="${actualValue}" binderName="${fieldItem.binderName}"
                                    labelKey="${fieldItem.fieldLabel}"
                                    itemLable="${fieldItem.itemLabel}"
                                    itemValue="${fieldItem.itemValue}"
                                    expandableField="${fieldItem.expandableField}"
                                    mandatoryField="${fieldItem.mandatoryField}"
                                    includeSelect="${fieldItem.includeSelect}"
                                    fieldDataType="${fieldItem.fieldDataType}"
                                    dynamicFormToolTip="${fieldItem.toolTipMessage}"
                                    entityName="${fieldItem.entityName}"
                                    customeItemList="${fieldItem.fieldCustomOptionsVOList}"
                                    customeMessage="${fieldItem.customeLongMessage}"
                                    minFieldLength="${fieldItem.minFieldLength}"
                                    maxFieldLength="${fieldItem.maxFieldLength}"
                                    minFieldValue="${fieldItem.minFieldValue}"
                                    maxFieldValue="${fieldItem.maxFieldValue}" formKey="${formKey}"
                                    viewMode="${viewMode}"
                                    searchableColumns="${fieldItem.searchableColumns}"
                                    defDate="${fieldItem.defDate}" mobile="${fieldItem.mobile}"
                                    href="${fieldItem.href}" 
                                    functionLogic="${fieldItem.functionLogic}"
                                    authority="${fieldItem.authority}"
                                    />
                                 </c:if>
                                 </div>
                                 <c:if test="${fieldItemStatus.index % 3 eq 2 || (fieldItemStatus.index +1 ) eq fn:length(singleItem.formFieldVOList)}">
                                 </div>
                                 </c:if>
                              </c:forEach>
                           </div>
                           </c:when>
                            <c:when test="${singleItem.panelColumnLayout == 4}">
                           <div <c:if test="${not empty singleItem.panelHeader}">class="panel-body"</c:if> id="fourColumn">
                              <c:forEach items="${singleItem.formFieldVOList}"
                                 var="fieldItem" varStatus="fieldItemStatus" step="1"
                                 begin="0">
                                 <c:if test="${fieldItemStatus.index %4 eq 0}">
                                    <div class="row">
                                 </c:if>
                                 <div class="col-sm-3">
                                 <c:choose>
                                 <c:when
                                    test="${fieldItem.fieldType eq  multiSelectBoxFieldType}">
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].value"></c:set>
                                 </c:when>
                                 <c:when test="${fieldItem.fieldType eq  phoneFieldType}">
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].phoneNumberVO" />
                                 </c:when>
                                 <c:when test="${fieldItem.fieldType eq  emailFieldType}">
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].emailInfoVO" />
                                 </c:when>
                                 <c:otherwise>
                                 <c:set var="actualPath"
                                    value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].value[0]"></c:set>
                                 <c:set var="actualValue"
                                    value="${items[panelItemStatus.index].formFieldVOList[fieldItemStatus.index].value[0]}"></c:set>
                                 </c:otherwise>
                                 </c:choose>
                                 <c:if test="${offlineTemplate eq false}">
                                 <neutrino:formField id="${fieldItem.id}"
                                    path="${actualPath}" fieldType="${fieldItem.fieldType}"
                                    value="${actualValue}"
                                    item="${fieldItem.item}"
                                    binderName="${fieldItem.binderName}"
                                    labelKey="${fieldItem.fieldLabel}"
                                    itemLable="${fieldItem.itemLabel}"
                                    itemValue="${fieldItem.itemValue}"
                                    expandableField="${fieldItem.expandableField}"
                                    mandatoryField="${fieldItem.mandatoryField}"
                                    includeSelect="${fieldItem.includeSelect}"
                                    fieldDataType="${fieldItem.fieldDataType}"
                                    dynamicFormToolTip="${fieldItem.toolTipMessage}"
                                    entityName="${fieldItem.entityName}"
                                    customeItemList="${fieldItem.fieldCustomOptionsVOList}"
                                    customeMessage="${fieldItem.customeLongMessage}"
                                    minFieldLength="${fieldItem.minFieldLength}"
                                    maxFieldLength="${fieldItem.maxFieldLength}"
                                    minFieldValue="${fieldItem.minFieldValue}"
                                    maxFieldValue="${fieldItem.maxFieldValue}"
                                    lovKey="${fieldItem.lovKey}"
                                    formKey="${formKey}" viewMode="${viewMode}"
                                    searchableColumns="${fieldItem.searchableColumns}"
                                    defDate="${fieldItem.defDate}"
                                    mobile="${fieldItem.mobile}"
                                    clonedRowStatus= "${panelItemStatus.index}"
                                    parentFieldKey="${fieldItem.parentFieldKey}"
                                    urlCascadeSelect="${fieldItem.urlCascadeSelect}"
                                    href="${fieldItem.href}" 
                                    functionLogic="${fieldItem.functionLogic}"
                                    authority="${fieldItem.authority}"
                                    parentFieldId="${fieldItem.parentFieldId}"
                                    parentColumn="${fieldItem.parentColumn}"
                                    errorMessageCode="${fieldItem.errorMessageCode}"
                                    mainFormDependant="${fieldItem.mainFormDependant}"
                                    parentKey="${fieldItem.parent}"
                                    disableKey="${fieldItem.disable}"
                                    panelColumnLayout="${singleItem.panelColumnLayout}"
                                    />
                                 </c:if>
                                 <c:if test="${offlineTemplate eq true}">
                                 <neutrino:templateFormField id="${fieldItem.id}"
                                    path="${actualPath}" fieldType="${fieldItem.fieldType}"
                                    value="${actualValue}"
                                    binderName="${fieldItem.binderName}"
                                    labelKey="${fieldItem.fieldLabel}"
                                    itemLable="${fieldItem.itemLabel}"
                                    itemValue="${fieldItem.itemValue}"
                                    expandableField="${fieldItem.expandableField}"
                                    mandatoryField="${fieldItem.mandatoryField}"
                                    includeSelect="${fieldItem.includeSelect}"
                                    fieldDataType="${fieldItem.fieldDataType}"
                                    dynamicFormToolTip="${fieldItem.toolTipMessage}"
                                    entityName="${fieldItem.entityName}"
                                    customeItemList="${fieldItem.fieldCustomOptionsVOList}"
                                    customeMessage="${fieldItem.customeLongMessage}"
                                    minFieldLength="${fieldItem.minFieldLength}"
                                    maxFieldLength="${fieldItem.maxFieldLength}"
                                    minFieldValue="${fieldItem.minFieldValue}"
                                    maxFieldValue="${fieldItem.maxFieldValue}"
                                    formKey="${formKey}" viewMode="${viewMode}"
                                    searchableColumns="${fieldItem.searchableColumns}"
                                    defDate="${fieldItem.defDate}"
                                    mobile="${fieldItem.mobile}"
                                    href="${fieldItem.href}" 
                                    functionLogic="${fieldItem.functionLogic}"
                                    authority="${fieldItem.authority}"
                                    />
                                 </c:if>
                                 </div>
                                 <c:if test="${fieldItemStatus.index % 4 eq 3  || (fieldItemStatus.index +1 ) eq fn:length(singleItem.formFieldVOList)}">
                                 </div>
                                 </c:if>
                              </c:forEach>
                            </div>
                           </c:when>
                           <c:otherwise>
                           <div <c:if test="${not empty singleItem.panelHeader}">class="panel-body"</c:if>>
                           <div class="row">
                              <div class="col-sm-6">
                                 <c:forEach items="${singleItem.formFieldVOList}" var="fieldItem"
                                    varStatus="fieldItemStatus">
                                    <c:choose>
                                       <c:when test="${fieldItem.fieldType eq  multiSelectBoxFieldType}">
                                          <c:set var="actualPath"
                                             value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].value"></c:set>
                                       </c:when>
                                       <c:when test="${fieldItem.fieldType eq  phoneFieldType}">
                                          <c:set var="actualPath"
                                             value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].phoneNumberVO" />
                                       </c:when>
                                       <c:when test="${fieldItem.fieldType eq  emailFieldType}">
                                          <c:set var="actualPath"
                                             value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].emailInfoVO" />
                                       </c:when>
                                       <c:otherwise>
                                          <c:set var="actualPath"
                                             value="${pathName}[${panelItemStatus.index}].formFieldVOList[${fieldItemStatus.index}].value[0]"></c:set>
                                          <c:set var="actualValue"
                                             value="${items[panelItemStatus.index].formFieldVOList[fieldItemStatus.index].value[0]}"></c:set>
                                       </c:otherwise>
                                    </c:choose>
                                    <c:if test="${offlineTemplate eq false}">
                                       <neutrino:formField id="${fieldItem.id}" path="${actualPath}"
                                          fieldType="${fieldItem.fieldType}" value="${actualValue}"
                                          binderName="${fieldItem.binderName}"
                                          item="${fieldItem.item}"
                                          labelKey="${fieldItem.fieldLabel}"
                                          itemLable="${fieldItem.itemLabel}"
                                          itemValue="${fieldItem.itemValue}"
                                          expandableField="${fieldItem.expandableField}"
                                          mandatoryField="${fieldItem.mandatoryField}"
                                          includeSelect="${fieldItem.includeSelect}"
                                          fieldDataType="${fieldItem.fieldDataType}"
                                          dynamicFormToolTip="${fieldItem.toolTipMessage}"
                                          entityName="${fieldItem.entityName}"
                                          customeItemList="${fieldItem.fieldCustomOptionsVOList}"
                                          customeMessage="${fieldItem.customeLongMessage}"
                                          minFieldLength="${fieldItem.minFieldLength}"
                                          maxFieldLength="${fieldItem.maxFieldLength}"
                                          minFieldValue="${fieldItem.minFieldValue}"
                                          maxFieldValue="${fieldItem.maxFieldValue}"
                                          lovKey="${fieldItem.lovKey}"
                                          formKey="${formKey}"
                                          viewMode="${viewMode}"
                                          searchableColumns="${fieldItem.searchableColumns}"
                                          defDate="${fieldItem.defDate}" mobile="${fieldItem.mobile}"
                                          clonedRowStatus=  "${panelItemStatus.index}"
                                          parentFieldKey="${fieldItem.parentFieldKey}"
                                          urlCascadeSelect="${fieldItem.urlCascadeSelect}"
                                          href="${fieldItem.href}" 
                                          functionLogic="${fieldItem.functionLogic}"
                                          authority="${fieldItem.authority}"
                                          parentFieldId="${fieldItem.parentFieldId}"
                                          parentColumn="${fieldItem.parentColumn}"
                                          errorMessageCode="${fieldItem.errorMessageCode}"
                                          mainFormDependant="${fieldItem.mainFormDependant}"
                                          parentKey="${fieldItem.parent}"
                                          disableKey="${fieldItem.disable}"
                                          />
                                    </c:if>
                                    <c:if test="${offlineTemplate eq true}">
                                       <neutrino:templateFormField id="${fieldItem.id}"
                                          path="${actualPath}" fieldType="${fieldItem.fieldType}"
                                          value="${actualValue}" binderName="${fieldItem.binderName}"
                                          labelKey="${fieldItem.fieldLabel}"
                                          itemLable="${fieldItem.itemLabel}"
                                          itemValue="${fieldItem.itemValue}"
                                          expandableField="${fieldItem.expandableField}"
                                          mandatoryField="${fieldItem.mandatoryField}"
                                          includeSelect="${fieldItem.includeSelect}"
                                          fieldDataType="${fieldItem.fieldDataType}"
                                          dynamicFormToolTip="${fieldItem.toolTipMessage}"
                                          entityName="${fieldItem.entityName}"
                                          customeItemList="${fieldItem.fieldCustomOptionsVOList}"
                                          customeMessage="${fieldItem.customeLongMessage}"
                                          minFieldLength="${fieldItem.minFieldLength}"
                                          maxFieldLength="${fieldItem.maxFieldLength}"
                                          minFieldValue="${fieldItem.minFieldValue}"
                                          maxFieldValue="${fieldItem.maxFieldValue}" formKey="${formKey}"
                                          viewMode="${viewMode}"
                                          searchableColumns="${fieldItem.searchableColumns}"
                                          defDate="${fieldItem.defDate}" mobile="${fieldItem.mobile}" 
                                          href="${fieldItem.href}" 
                                          functionLogic="${fieldItem.functionLogic}"
                                          authority="${fieldItem.authority}"
                                          />
                                    </c:if>
                                 </c:forEach>
                              </div>
                             </div>
                           </div>
                           </c:otherwise>
                        </c:choose>
                         <c:if test="${singleItem.allowPanelSave eq true}">
                         <script>
                            var panelDivId = '${singleItem.panelKey}';
                            $("#"+panelDivId).css({"border":"ridge"});
                         </script>
                         <c:if test="${(empty viewMode) || (viewMode eq false)}">
                                <div class='row txt-r'>
                                    <a href="#" id="${singleItem.panelKey}saveButton" class="btn btn-primary" onclick="savePanelWise('${singleItem.panelKey}');"> <spring:message
                                            code="label.save" />
                                    </a>
                                </div>
                          </c:if>
                        </c:if>
                     </div>
                     <!-- </div> -->
                     <!-- <hr> -->
                     <!-- Render Panel End -->
                  </c:when>
                  <c:when test="${singleItem.panelType eq 3}">
                     <div class="row p-b5">
                        <b>
                           <spring:message code="${singleItem.panelHeader}"></spring:message>
                        </b>
                     </div>
                     <div class="row dynamicFormTableDiv">
                        <!-- table Start -->
                        <table
                           class="table table-striped table-bordered table-condensed KeyTable dataTable"
                           id="table_${panelItemStatus.index}_${formKey}">
                           <c:set var="numberOfTableRows" scope="page"
                              value="${fn:length(singleItem.formComponentList)}" />
                           <thead>
                              <tr>
                                 <c:forEach
                                    items="${singleItem.formComponentList[0].formFieldVOList}"
                                    var="tableItem">
                                    <td>
                                       <spring:message code="${tableItem.fieldLabel}"></spring:message>
                                       <c:if test="${tableItem.mandatoryField eq true}">
                                          <span class="Mandatory color-red">*</span>
                                       </c:if>
                                    </td>
                                 </c:forEach>
                                 <c:if test="${(empty viewMode) || (viewMode eq false)}">
                                    <td>
                                       <spring:message code="label.dynamicForm.tableaction"></spring:message>
                                    </td>
                                 </c:if>
                              </tr>
                           </thead>
                           <tbody>
                              <c:forEach items="${singleItem.formComponentList}"
                                 var="tableSingleItem" varStatus="tableSingleItemStatus">
                                 <tr>
                                    <neutrino:dynamicFormTableRow tableSingleItem="${tableSingleItem}"
                                       uiComponentsIndex="${panelItemStatus.index}"
                                       formComponentIndex="${tableSingleItemStatus.index}"
                                       viewMode="${viewMode}" fieldItem="${fieldItem}"
                                       formKey="${formKey}"
                                       tableSingleItemStatusFirst="${tableSingleItemStatus.first}"
                                       offlineTemplate="${offlineTemplate}" path="${pathName}"/>
                                 </tr>
                              </c:forEach>
                           </tbody>
                        </table>
                        <!-- table End -->
                     </div>
                  </c:when>
                  <c:when test="${singleItem.panelType eq 5}">
                     <div class="row p-b5">
                        <b>
                           <spring:message code="${singleItem.panelHeader}"></spring:message>
                        </b>
                     </div>
                     <div class="row dynamicFormTableDiv">
                        <!-- table Start -->
                        <table
                           class="table table-striped table-bordered table-condensed KeyTable dataTable"
                           id="table_${panelItemStatus.index}_${formKey}">
                           <c:set var="numberOfTableRows" scope="page"
                              value="${fn:length(singleItem.formComponentList)}" />
                           <thead>
                              <tr>
                                 <c:forEach
                                    items="${singleItem.formComponentList[0].formFieldVOList}"
                                    var="tableItem">
                                    <td>
                                       <spring:message code="${tableItem.fieldLabel}"></spring:message>
                                       <c:if test="${tableItem.mandatoryField eq true}">
                                          <span class="Mandatory color-red">*</span>
                                       </c:if>
                                    </td>
                                 </c:forEach>
                              </tr>
                           </thead>
                           <tbody>
                              <c:forEach items="${singleItem.formComponentList}"
                                 var="tableSingleItem" varStatus="tableSingleItemStatus">
                                 <tr>
                                    <neutrino:dynamicFormTableRow tableSingleItem="${tableSingleItem}"
                                       uiComponentsIndex="${panelItemStatus.index}"
                                       formComponentIndex="${tableSingleItemStatus.index}"
                                       viewMode="${viewMode}" fieldItem="${fieldItem}"
                                       formKey="${formKey}"
                                       tableSingleItemStatusFirst="${tableSingleItemStatus.first}"
                                       offlineTemplate="${offlineTemplate}"
                                       isSpecialTable="true" path="${pathName}"/>
                                 </tr>
                              </c:forEach>
                           </tbody>
                        </table>
                        <!-- table End -->
                     </div>
                  </c:when>
               </c:choose>
               <input type="hidden" name="${pathName}[${panelItemStatus.index}].type"
                  value="${singleItem.type}" />
               <input type="hidden"
                  name="${pathName}[${panelItemStatus.index}].panelType"
                  value="${singleItem.panelType}" />
               <input type="hidden"
                  name="${pathName}[${panelItemStatus.index}].panelName"
                  value="${singleItem.panelName}" />
               <input type="hidden"
                  name="${pathName}[${panelItemStatus.index}].panelKey"
                  value="${singleItem.panelKey}" />
            </c:forEach>
         </div>
      </div>
   </c:otherwise>
</c:choose>
<script>
   (function(){
   	var panelDefinitionTagScriptInput = {};
   	panelDefinitionTagScriptInput = {
   		viewMode_pd : '${viewMode}',
   		numberOfTableRows_pd: '${numberOfTableRows}',
   		formKey_pd : '${formKey}'
   	}
   	panelDefinitionTagScript(panelDefinitionTagScriptInput);
   	$("[rel=tooltip]").tooltip({
    	placement : 'right',
    	"container" : 'body',
    	"viewport": { selector: 'html', padding: 0 }
    });
   })();
</script>