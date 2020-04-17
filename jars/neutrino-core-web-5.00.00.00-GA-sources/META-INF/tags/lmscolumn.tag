<%@ tag language="java" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>

<%@ attribute name="property" required="false" type="java.lang.String" description="" %>
<%@ attribute name="title" required="false" type="java.lang.String" description="" %>
<%@ attribute name="columnType" required="false" type="java.lang.String" description="" %>
<%@ attribute name="textAlign" required="false" type="java.lang.String" description="" %>
<%@ attribute name="amtFormat" required="false" type="java.lang.String" description="" %>
<%@ attribute name="footerValue" required="false" type="java.lang.String" description="" %>
<%@ attribute name="width" required="false" type="java.lang.String" description="" %>
<%@ attribute name="sortable" required="false" type="java.lang.String" description="" %>
<%@ attribute name="colvis" required="false" type="java.lang.String" description="" %>
<%@ attribute name="headerAlign" required="false" type="java.lang.String" description="" %>
<%@ attribute name="soType" required="false" type="java.lang.String" description="" %>
<%@ attribute name="titleName" required="false" type="java.lang.String" description="" %>
<%@ attribute name="defaultVis" required="false" type="java.lang.String" description="" %>
<%@ attribute name="groupType" required="false" type="java.lang.String" description="" %>
<%@ attribute name="groupName" required="false" type="java.lang.String" description="" %>
<%@ attribute name="selectedRadioIndex" required="false" type="java.lang.String" description="" %>
<%@ attribute name="neoDisplayIcon" required="false" type="java.lang.String" description="" %>
<%@ attribute name="maxLength" required="false" type="java.lang.String" description="" %>
<%@ attribute name="columnSearch" required="false" type="java.lang.String" description="" %>
<%@ attribute name="glyphName" required="false" type="java.lang.String" description="" %>
<%@ attribute name="wordBreak" required="false" type="java.lang.String" description="" %>
<%@ attribute name="onClick" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovViewMode" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovAlignToolTip" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovPreValidationMethod" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovValidationFailureMethod" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovFilterDataMethod" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovPostExecutionScript" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovKey" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovShowModalAlongWithValue" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovPrefixKey" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovSuffixKey" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovPlaceHolderKey" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovDisabled" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovReadOnly" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovMaxLength" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovMinLength" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovTooltipKey" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovMessageKey" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovHelpKey" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovInputMaskKey" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovValidators" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovColSpan" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovInputBoxColSpan" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovInputBoxDivColSpan" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovTabindex" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovTextDirection" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovTextAlign" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovInputCase" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovAutoComplete" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovHeaderLabel" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovShowTextAreaInViewMode" required="false" type="java.lang.String" description="" %>
<%@ attribute name="lovMandatory" required="false" type="java.lang.String" description="" %>

<c:choose>
		<c:when test="${empty titleNames}">
			<c:set var="titleNames" value="${titleName}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:set var="titleNames" value="${titleNames}${delimitor}${titleName}" scope="request" />
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty titles}">
			<c:set var="titles" value="${title}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:set var="titles" value="${titles}${delimitor}${title}" scope="request" />
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty properties}">
			<c:set var="properties" value="${property eq null ? 'null' : property}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${property eq null}">
					<c:set var="properties" value="${properties}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="properties" value="${properties}${delimitor}${property}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>
	
<c:choose>
		<c:when test="${empty columnTypeList}">
			<c:set var="columnTypeList" value="${columnType eq null ? 'string' : columnType}" scope="request" />
			<c:set var="columnCount" value="${1}" scope="request"/>
		</c:when>
		<c:otherwise>
			<c:choose>
			
				<c:when test="${columnType eq null}">
				
					<c:set var="columnTypeList" value="${columnTypeList}${delimitor}'string'" scope="request" />
					<c:set var="columnCount" value="${columnCount + 1}" scope="request"/>
				</c:when>
				<c:otherwise>
					<c:set var="columnTypeList" value="${columnTypeList}${delimitor}${columnType}" scope="request" />
					<c:set var="columnCount" value="${columnCount + 1}" scope="request"/>
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty alignments}">
			<c:set var="alignments" value="${textAlign eq null ? 'left' : textAlign}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${textAlign eq null}">
					<c:set var="alignments" value="${alignments}${delimitor}left" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="alignments" value="${alignments}${delimitor}${textAlign}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty amtFormatList}">
			<c:set var="amtFormatList" value="${amtFormat eq null ? 'null' : amtFormat}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${amtFormat eq null}">
					<c:set var="amtFormatList" value="${amtFormatList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="amtFormatList" value="${amtFormatList}${delimitor}${amtFormat}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty footerValueList}">
			<c:set var="footerValueList" value="${footerValue eq null ? 'null' : footerValue}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${footerValue eq null}">
					<c:set var="footerValueList" value="${footerValueList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="footerValueList" value="${footerValueList}${delimitor}${footerValue}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty widthList}">
			<c:set var="widthList" value="${width eq null ? 'null' : width}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${width eq null}">
					<c:set var="widthList" value="${widthList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="widthList" value="${widthList}${delimitor}${width}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>


<c:choose>
		<c:when test="${empty maxLengthList}">
			<c:set var="maxLengthList" value="${maxLength eq null ? 'null' : maxLength}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${maxLength eq null}">
					<c:set var="maxLengthList" value="${maxLengthList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="maxLengthList" value="${maxLengthList}${delimitor}${maxLength}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty sortableList}">
			<c:set var="sortableList" value="${sortable eq null ? false : sortable}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${sortable eq null}">
					<c:set var="sortableList" value="${sortableList}${delimitor}false" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="sortableList" value="${sortableList}${delimitor}${sortable}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty colvisList}">
			<c:set var="colvisList" value="${colvis eq null ? 'N' : colvis}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${colvis eq null}">
					<c:set var="colvisList" value="${colvisList}${delimitor}N" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="colvisList" value="${colvisList}${delimitor}${colvis}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty headerAlignList}">
			<c:set var="headerAlignList" value="${headerAlign eq null ? 'left' : headerAlign}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${headerAlign eq null}">
					<c:set var="headerAlignList" value="${headerAlignList}${delimitor}left" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="headerAlignList" value="${headerAlignList}${delimitor}${headerAlign}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>


<c:choose>
		<c:when test="${empty soTypeList}">
			<c:set var="soTypeList" value='${soType eq null ? (columnType eq "textAmount" || columnType eq "text") ? "string" : "html" : soType}' scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${soType eq null}">
					<c:set var="soTypeList" value='${soTypeList}${delimitor}${(columnType eq "textAmount" || columnType eq "text") ? "string" : "html"}' scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="soTypeList" value="${soTypeList}${delimitor}${soType}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty defaultVisList}">
			<c:set var="defaultVisList" value="${colvis eq null ? 'Y' : defaultVis}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${colvis eq null}">
					<c:set var="defaultVisList" value="${defaultVisList}${delimitor}Y" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="defaultVisList" value="${defaultVisList}${delimitor}${defaultVis}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty groupTypeList}">
			<c:set var="groupTypeList" value="${groupType eq null ? 'V' : groupType eq 'H' ? groupType : 'V'}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${groupType eq null}">
					<c:set var="groupTypeList" value="${groupTypeList}${delimitor}V" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="groupTypeList" value="${groupTypeList}${delimitor}${groupType eq 'H' ? groupType : 'V'}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty groupNameList}">
			<c:set var="groupNameList" value="${groupName eq null ? 'Group' : groupName}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${groupName eq null}">
					<c:set var="groupNameList" value="${groupNameList}${delimitor}Group" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="groupNameList" value="${groupNameList}${delimitor}${groupName}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${empty selectedRadio}">
		<c:set var="selectedRadio" value="0" scope="request" />
	</c:when>
	<c:when test="${selectedRadioIndex eq null}">
		<c:set var="selectedRadio" value="${selectedRadio}" scope="request" />
	</c:when>
	<c:otherwise>
		<c:set var="selectedRadio" value="${selectedRadioIndex}" scope="request" />
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${neoDisplayIcon eq null}">
			<c:set var="neoDisplayIconList" value="${neoDisplayIconList}${delimitor}null" scope="request" />
	</c:when>
	<c:otherwise>
		<c:choose>
			<c:when test="${empty neoDisplayIconList}">
				<c:set var="neoDisplayIconList" value="${neoDisplayIcon}" scope="request" />
			</c:when>
			<c:otherwise>
				<c:set var="neoDisplayIconList" value="${neoDisplayIconList}${delimitor}${neoDisplayIcon}" scope="request" />
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty columnsSearchList}">
			<c:set var="columnsSearchList" value="${columnSearch eq null ? 'N' : columnSearch}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${columnSearch eq null}">
					<c:set var="columnsSearchList" value="${columnsSearchList}${delimitor}N" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="columnsSearchList" value="${columnsSearchList}${delimitor}${columnSearch}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty glyphsNameList}">
			<c:set var="glyphsNameList" value="${glyphName eq null ? 'null' : glyphName}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${glyphName eq null}">
					<c:set var="glyphsNameList" value="${glyphsNameList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="glyphsNameList" value="${glyphsNameList}${delimitor}${glyphName}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>
<c:choose>
		<c:when test="${empty wordBreakList}">
			<c:set var="wordBreakList" value="${wordBreak eq null ? 'N' : wordBreak}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${wordBreak eq null}">
					<c:set var="wordBreakList" value="${wordBreakList}${delimitor}N" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="wordBreakList" value="${wordBreakList}${delimitor}${wordBreak}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>
<c:choose>
		<c:when test="${empty onClickList}">
			<c:set var="onClickList" value="${onClick eq null ? '' : onClick}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${onClickList eq null}">
					<c:set var="onClickList" value="${onClickList}${delimitor} " scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="onClickList" value="${onClickList}${delimitor}${onClick}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>
<c:choose>
        <c:when test="${lovMaxLength eq null}">
            <c:set var="lovMaxInputLength" value="255" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="lovMaxInputLength" value="${lovMaxLength}" scope="request" />
        </c:otherwise>
</c:choose>
<c:choose>
        <c:when test="${lovMinLength eq null}">
            <c:set var="lovMinInputLength" value="" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="lovMinInputLength" value="${lovMinLength}" scope="request" />
        </c:otherwise>
</c:choose>

<c:set var="lovColumnSpan" value="12" scope="request"/>
<c:if test="${not empty lovColSpan}">
    <c:set var="lovColumnSpan" value="${lovColSpan}" scope="request" />
</c:if>


<c:set var="lovInputBoxColumnSpan" value="12" scope="request"/>
<c:if test="${not empty lovInputBoxColSpan}">
    <c:set var="lovInputBoxColumnSpan" value="${lovInputBoxColSpan}" scope="request" />
</c:if>

<c:set var="lovInputBoxDivColumnSpan" value="8" scope="request"/>
<c:if test="${lovViewMode eq true and lovShowTextAreaInViewMode eq true}">
    <c:set var="lovInputBoxDivColumnSpan" value="12" scope="request" />
</c:if>

<c:if test="${not empty lovInputBoxDivColSpan}">
    <c:set var="lovInputBoxDivColumnSpan" value="${lovInputBoxDivColSpan}" scope="request" />
</c:if>

<c:set var="isLovMandatory" value="false" scope="request"/>
<c:if test="${lovMandatory eq true}">
    <c:set var="isLovMandatory" value="true" scope="request" />
</c:if>

<c:choose>
		<c:when test="${empty lovViewModeList}">
			<c:set var="lovViewModeList" value="${lovViewMode eq null ? 'false' : lovViewMode}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${lovViewMode eq null}">
					<c:set var="lovViewModeList" value="${lovViewModeList}${delimitor}false" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="lovViewModeList" value="${lovViewModeList}${delimitor}${lovViewMode}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty lovAlignToolTipList}">
			<c:set var="lovAlignToolTipList" value="${lovAlignToolTip eq null ? 'null' : lovAlignToolTip}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${lovAlignToolTip eq null}">
					<c:set var="lovAlignToolTipList" value="${lovAlignToolTipList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="lovAlignToolTipList" value="${lovAlignToolTipList}${delimitor}${lovAlignToolTip}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>
<c:choose>
		<c:when test="${empty lovPreValidationMethodList}">
			<c:set var="lovPreValidationMethodList" value="${lovPreValidationMethod eq null ? 'null' : lovPreValidationMethod}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${lovPreValidationMethod eq null}">
					<c:set var="lovPreValidationMethodList" value="${lovPreValidationMethodList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="lovPreValidationMethodList" value="${lovPreValidationMethodList}${delimitor}${lovPreValidationMethod}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty lovValidationFailureMethodList}">
			<c:set var="lovValidationFailureMethodList" value="${lovValidationFailureMethod eq null ? 'null' : lovValidationFailureMethod}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${lovValidationFailureMethod eq null}">
					<c:set var="lovValidationFailureMethodList" value="${lovValidationFailureMethodList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="lovValidationFailureMethodList" value="${lovValidationFailureMethodList}${delimitor}${lovValidationFailureMethod}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>
<c:choose>
		<c:when test="${empty lovFilterDataMethodList}">
			<c:set var="lovFilterDataMethodList" value="${lovFilterDataMethod eq null ? 'null' : lovFilterDataMethod}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${lovFilterDataMethod eq null}">
					<c:set var="lovFilterDataMethodList" value="${lovFilterDataMethodList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="lovFilterDataMethodList" value="${lovFilterDataMethodList}${delimitor}${lovFilterDataMethod}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>
<c:choose>
		<c:when test="${empty lovPostExecutionScriptList}">
			<c:set var="lovPostExecutionScriptList" value="${lovPostExecutionScript eq null ? 'null' : lovPostExecutionScript}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${lovPostExecutionScript eq null}">
					<c:set var="lovPostExecutionScriptList" value="${lovPostExecutionScriptList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="lovPostExecutionScriptList" value="${lovPostExecutionScriptList}${delimitor}${lovPostExecutionScript}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty lovKeyList}">
			<c:set var="lovKeyList" value="${lovKey eq null ? 'null' : lovKey}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${lovKey eq null}">
					<c:set var="lovKeyList" value="${lovKeyList}${delimitor}null" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="lovKeyList" value="${lovKeyList}${delimitor}${lovKey}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty lovShowModalAlongWithValueList}">
			<c:set var="lovShowModalAlongWithValueList" value="${lovShowModalAlongWithValue eq null ? 'false' : lovShowModalAlongWithValue}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${lovShowModalAlongWithValue eq null}">
					<c:set var="lovShowModalAlongWithValueList" value="${lovShowModalAlongWithValueList}${delimitor}false" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="lovShowModalAlongWithValueList" value="${lovShowModalAlongWithValueList}${delimitor}${lovShowModalAlongWithValue}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty lovShowTextAreaInViewModeList}">
			<c:set var="lovShowTextAreaInViewModeList" value="${lovShowTextAreaInViewMode eq null ? 'false' : lovShowTextAreaInViewMode}" scope="request" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${lovShowTextAreaInViewMode eq null}">
					<c:set var="lovShowTextAreaInViewModeList" value="${lovShowTextAreaInViewModeList}${delimitor}false" scope="request" />
				</c:when>
				<c:otherwise>
					<c:set var="lovShowTextAreaInViewModeList" value="${lovShowTextAreaInViewModeList}${delimitor}${lovShowTextAreaInViewMode}" scope="request" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
</c:choose>

<c:choose>
		<c:when test="${empty lovMandatoryList}">
			<c:set var="lovMandatoryList" value="${isLovMandatory}" scope="request" />
		</c:when>
		<c:otherwise>
		    <c:set var="lovMandatoryList" value="${lovMandatoryList}${delimitor}${isLovMandatory}" scope="request" />
		</c:otherwise>
</c:choose>


<c:choose>
	<c:when test="${columnType  == 'lov'}">
	    <c:set var="tableId" value="${dataTableId}" scope="page"/>
		<div id = "<c:out value='${tableId}'/>_lov_div_<c:out value='${columnCount}'/>" style="display: none;">
		    <c:set var="lovId" value="${tableId}_lov_${columnCount}" scope="page"/>
		    <neutrino:lov id="${lovId}" name="${lovId}" enableScript="false" viewMode="${lovViewMode}" preValidationMethod="${lovPreValidationMethod}" inputBoxColSpan="${lovInputBoxColumnSpan}"
		        colSpan="${lovColumnSpan}" inputBoxDivColSpan="${lovInputBoxDivColumnSpan}" tooltipKey="${lovTooltipKey}" lovKey="${lovKey}" validationFailureMethod="${lovValidationFailureMethod}"
		        lovHeaderLabel="${lovHeaderLabel}" filterDataMethod="${lovFilterDataMethod}" alignToolTip="${lovAlignToolTip}" postExecutionScript="${lovPostExecutionScript}"
		        showModalAlongWithValue="${lovShowModalAlongWithValue}" prefixKey="${lovPrefixKey}" suffixKey="${lovSuffixKey}" placeHolderKey="${lovPlaceHolderKey}" disabled="${lovDisabled}"
		        readOnly="${lovReadOnly}" maxLength="${lovMaxInputLength}" minLength="${lovMinInputLength}" messageKey="${lovMessageKey}" helpKey="${lovHelpKey}"
		        inputMaskKey="${lovInputMaskKey}" validators="${lovValidators}" tabindex="${lovTabindex}" textDirection="${lovTextDirection}" textAlign="${lovTextAlign}" inputCase="${lovInputCase}"
		        autoComplete="${lovAutoComplete}" showTextAreaInViewMode="${lovShowTextAreaInViewMode}"/>
		</div>
	</c:when>
</c:choose>