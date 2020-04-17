<%@tag import="java.util.Arrays"%>
<%@tag import="java.util.ArrayList"%>
<%@tag import="java.util.Map"%>
<%@tag import="java.util.HashMap"%>
<%@tag import="fr.opensagres.xdocreport.document.json.JSONArray"%>
<%@tag import="org.springframework.web.util.HtmlUtils"%>
<%@tag import="org.apache.commons.beanutils.PropertyUtils"%>
<%@tag import="fr.opensagres.xdocreport.document.json.JSONObject"%>
<%@tag import="java.util.List"%>
<%@tag import="org.springframework.util.StringUtils"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ attribute name="disabled"%>
<%@ attribute name="id" required="true" %>
<%@ attribute name="path"%>
<%@ attribute name="name"%>
<%@ attribute name="value" type="java.util.List"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="itemValue"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="itemLabel"%>
<%@ attribute name="items" type="java.util.List"%>
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

<%@ attribute name="initItemLoadCount"%>
<%@ attribute name="valueMap" type="java.util.Map"%>
<%@ attribute name="itemsMap" type="java.util.Map"%>
 
<%
 
 String finalValue = null;
 %>
 
<c:if test="${empty initItemLoadCount}">
    <c:set var="itemLoadCount" value="10" scope="page" />
</c:if>
<c:if test="${not empty initItemLoadCount}">
    <c:set var="itemLoadCount" value="initItemLoadCount" scope="page" />
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

<c:if test="${not empty path}">
	<c:if test="${not empty items}">
			<spring:bind path="${path}">
	              <c:set var="selectedValue" value="${status.value}" scope="page"></c:set>
	       </spring:bind>
	</c:if>
    <c:if test="${not empty itemsMap}">
    		<spring:bind path="${path}">
	              <c:set var="selectedValueMap" value="${status.value}" scope="page"></c:set>
	       </spring:bind>
    </c:if>
</c:if>

<c:if test="${not empty value}">
       <c:set var="selectedValue" value="${value}"></c:set>
</c:if>
<c:if test="${not empty valueMap}">
       <c:set var="selectedValueMap" value="${valueMap}"></c:set>
</c:if>

<%-- Variables required for angular multiselect box --%>
<c:set var="dataFrom" value="from_${id}" scope="page" />
<c:set var="dataTo" value="to_${id}" scope="page" />
<c:set var="showItemsAll" value="showItemsAll_${id}" scope="page" />
<c:set var="showItemsSelected" value="showItemsSelected_${id}" scope="page" />
<c:set var="searchAll" value="searchAll_${id}" scope="page" />
<c:set var="searchSelected" value="searchSelected_${id}" scope="page" />
<c:set var="selectItems" value="selectItems_${id}" scope="page" />
<c:set var="selectedItems" value="selectedItems_${id}" scope="page" />

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
			<c:when test="${not empty name && empty disabled}">
				<div ng-controller="myCtrl" ng-cloak class="angDiv">
					<div id="multiselectbox_${id}" multiselectbox data-from="${dataFrom}" data-to="${dataTo}">
						<div class="col-sm-5">
							<input type="text" class="form-control form-control" placeholder="Search..." ng-model="${searchAll}" style="margin-bottom:0px; "/>
							<button type="button" id="search_rightSelected" class="btn btn-block" ng-click="addItems('${dataFrom}','${dataTo}','${showItemsSelected}','${selectItems}','${searchAll}')">
								Select All<i class="right"></i>
							</button>
							<ul 
								data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />"
								ng-model="${selectItems}" scroller data-limitTo="${showItemsAll}" ng-init="${showItemsAll}= ${itemLoadCount}" ng-init="${selectItems} = ${selectItems} || []">
								<li value="{{item}}" data-position="{{$index}}" ng-repeat="item in ${dataFrom} | filter: {itemLabel:${searchAll}} | limitTo: ${showItemsAll}  track by $index" ng-click="addAllItems('${dataFrom}','${dataTo}','${showItemsSelected}',item,'${searchAll}')">{{item.itemLabel}} <i></i></li>
							</ul>
						</div>
						<div class="col-sm-2 multiItemSelect-right-arrow"></div>
						<div class="col-sm-5">
							<input type="text" class="form-control form-control" placeholder="Search..." ng-model="${searchSelected}" style="margin-bottom:0px;" />
							<button type="button" id="search_leftSelected" class="btn btn-block" ng-click="removeAddedItems('${dataTo}','${dataFrom}','${showItemsAll}','${selectedItems}','${searchSelected}')">
								<i class="left"></i> Deselect All
							</button>
							<ul 
								data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />" 
								ng-model="${selectedItems}" scroller data-limitTo="${showItemsSelected}" ng-init="${showItemsSelected}= ${itemLoadCount}" ng-init="${selectedItems} = ${selectedItems} || []">
								<li value="{{item}}" data-position="{{$index}}" ng-repeat="item in ${dataTo} | filter: {itemLabel:${searchSelected}} | limitTo: ${showItemsSelected}  track by $index"  ng-click="removeAllAddedItems('${dataTo}','${dataFrom}','${showItemsAll}',item,'${searchSelected}')">{{item.itemLabel}}</li>
							</ul>
							<input class="form-control <c:out value='${validators}' /> " name="${name}" type="text" value="{{dataToIdArray}}" style="display:none"/>
						</div>
					</div>		
				</div>
			</c:when>
			<c:when test="${not empty name && not empty disabled}">
				<div ng-controller="myCtrl" ng-cloak class="angDiv">
								<div id="multiselectbox_${id}" multiselectbox data-from="${dataFrom}" data-to="${dataTo}">
									<div class="col-sm-5">
										<input type="text" class="form-control form-control" placeholder="Search..." disabled="disabled" ng-model="${searchAll}" style="margin-bottom:0px; "/>
										<button type="button" id="search_rightSelected" class="btn btn-block" disabled="disabled" ng-click="addAllItems('${dataFrom}','${dataTo}','${showItemsSelected}','${selectItems}','${searchAll}')">
											Select All<i class="right"></i>
										</button>
										<ul  disabled="disabled"
											 data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />" size="8"
											ng-model="${selectItems}" scroller data-limitTo="${showItemsAll}" ng-init="${showItemsAll}= ${itemLoadCount}" ng-init="${selectItems} = ${selectItems} || []">
											<li value="{{item}}" data-position="{{$index}}" ng-repeat="item in ${dataFrom} | filter: {itemLabel:${searchAll}} | limitTo: ${showItemsAll}  track by $index" ng-click="addAllItems('${dataFrom}','${dataTo}','${showItemsSelected}',item,'${searchAll}')">{{item.itemLabel}} <i></i></li>
										</ul>
									</div>
									<div class="col-sm-2 multiItemSelect-right-arrow"></div>
									<div class="col-sm-5">
										<input type="text" class="form-control form-control" placeholder="Search..." disabled="disabled" ng-model="${searchSelected}" style="margin-bottom:0px;" />
										<button type="button" id="search_leftSelected" class="btn btn-block" disabled="disabled" ng-click="removeAllAddedItems('${dataTo}','${dataFrom}','${showItemsAll}','${selectedItems}','${searchSelected}')">
											<i class="left"></i> Deselect All
										</button>
										<ul  disabled="disabled"
											data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}' />" 
											size="8" ng-model="${selectedItems}" scroller data-limitTo="${showItemsSelected}" ng-init="${showItemsSelected}= ${itemLoadCount}" ng-init="${selectedItems} = ${selectedItems} || []">
											<li value="{{item}}" data-position="{{$index}}" ng-repeat="item in ${dataTo} | filter: {itemLabel:${searchSelected}} | limitTo: ${showItemsSelected}  track by $index"  ng-click="removeAllAddedItems('${dataTo}','${dataFrom}','${showItemsAll}',item,'${searchSelected}')">{{item.itemLabel}}</li>
										</ul>
										<input  class="form-control " name="${name}" type="text" value="{{dataToIdArray}}" style="display:none"/>
									</div>
								</div>		
							</div>
			</c:when>
			<c:otherwise>
				<c:choose>
					<c:when test="${empty disabled}">
							<div ng-controller="myCtrl" ng-cloak class="angDiv">
								<div id="multiselectbox_${id}" multiselectbox data-from="${dataFrom}" data-to="${dataTo}">
									<div class="col-sm-5">
										<input type="text" class="form-control form-control" placeholder="Search..." ng-model="${searchAll}" style="margin-bottom:0px; "/> 
										<button type="button" id="search_rightSelected" class="btn btn-block" ng-click="addAllItems('${dataFrom}','${dataTo}','${showItemsSelected}','${selectItems}','${searchAll}')">
											Select All<i class="right"></i>
										</button>
										<ul  
										placeholder="${placeHolderMessage}" data-original-title="${tooltipMessage}" tabindex="${tabindex}" 
										ng-model="${selectItems}" scroller data-limitTo="${showItemsAll}" ng-init="${showItemsAll}= ${itemLoadCount}" ng-init="${selectItems} = ${selectItems} || []">
											<li value="{{item}}" data-position="{{$index}}" ng-repeat="item in ${dataFrom} | filter: {itemLabel:${searchAll}} | limitTo: ${showItemsAll}  track by $index" ng-click="addItems('${dataFrom}','${dataTo}','${showItemsSelected}',item,'${searchAll}')">{{item.itemLabel}} <i></i></li>
										</ul>
									</div>
									<div class="col-sm-2 multiItemSelect-right-arrow"></div>
									<div class="col-sm-5">
										 <input type="text" class="form-control form-control" placeholder="Search..." ng-model="${searchSelected}" style="margin-bottom:0px;" />
										<button type="button" id="search_leftSelected" class="btn btn-block" ng-click="removeAllAddedItems('${dataTo}','${dataFrom}','${showItemsAll}','${selectedItems}','${searchSelected}')">
											<i class="left"></i> Deselect All
										</button>
										<ul 
										 data-original-title="${tooltipMessage}" tabindex="${tabindex}"
										ng-model="${selectedItems}" scroller data-limitTo="${showItemsSelected}" ng-init="${showItemsSelected}= ${itemLoadCount}" ng-init="${selectedItems} = ${selectedItems} || []">
											<li value="{{item}}"  data-position="{{$index}}" ng-repeat="item in ${dataTo} | filter: {itemLabel:${searchSelected}} | limitTo: ${showItemsSelected}  track by $index"  ng-click="removeAddedItems('${dataTo}','${dataFrom}','${showItemsAll}',item,'${searchSelected}')">{{item.itemLabel}}</li>
										</ul>
										<input class="form-control <c:out value='${validators}' /> " name="${path}" type="text" value="{{dataToIdArray}}" style="display:none"/>
									</div>
								</div>		
							</div>
					</c:when>
					<c:when test="${not empty disabled}">
							<div ng-controller="myCtrl" ng-cloak class="angDiv">
								<div id="multiselectbox_${id}" multiselectbox data-from="${dataFrom}" data-to="${dataTo}">
									<div class="col-sm-5">
										<input disabled="disabled" type="text" class="form-control form-control" placeholder="Search..." ng-model="${searchAll}" style="margin-bottom:0px; "/>
										<button disabled="disabled" type="button" id="search_rightSelected" class="btn btn-block" ng-click="addAllItems('${dataFrom}','${dataTo}','${showItemsSelected}','${selectItems}','${searchAll}')">
											Select All<i class="right"></i>
										</button>
										<ul disabled="disabled" 
										 data-original-title="${tooltipMessage}" tabindex="${tabindex}" 
										ng-model="${selectItems}" scroller data-limitTo="${showItemsAll}" ng-init="${showItemsAll}= ${itemLoadCount}" ng-init="${selectItems} = ${selectItems} || []">
											<li value="{{item}}" data-position="{{$index}}" ng-repeat="item in ${dataFrom} | filter: {itemLabel:${searchAll}} | limitTo: ${showItemsAll}  track by $index" ng-click="addItems('${dataFrom}','${dataTo}','${showItemsSelected}',item,'${searchAll}')">{{item.itemLabel}} <i></i></li>
										</ul>
									</div>
									<div class="col-sm-2 multiItemSelect-right-arrow"></div>
									<div class="col-sm-5">
										<input disabled="disabled" type="text" class="form-control form-control" placeholder="Search..." ng-model="${searchSelected}" style="margin-bottom:0px;" />
										<button disabled="disabled" type="button" id="search_leftSelected" class="btn btn-block" ng-click="removeAllAddedItems('${dataTo}','${dataFrom}','${showItemsAll}','${selectedItems}','${searchSelected}')">
											<i class="left"></i> Deselect All
										</button>
										<ul  disabled="disabled" 
										data-original-title="${tooltipMessage}" tabindex="${tabindex}" 
										ng-model="${selectedItems}" scroller data-limitTo="${showItemsSelected}" ng-init="${showItemsSelected}= ${itemLoadCount}" ng-init="${selectedItems} = ${selectedItems} || []">
											<li value="{{item}}" data-position="{{$index}}" ng-repeat="item in ${dataTo} | filter: {itemLabel:${searchSelected}} | limitTo: ${showItemsSelected}  track by $index"  ng-click="removeAddedItems('${dataTo}','${dataFrom}','${showItemsAll}',item,'${searchSelected}')">{{item.itemLabel}}</li>
										</ul>
										<input  class="form-control " name="${path}" type="text" value="{{dataToIdArray}}" style="display:none"/>
									</div>
								</div>		
							</div>
					</c:when>
				</c:choose>
			</c:otherwise>			
			</c:choose>

	</div>
	

	
</div>
<!-- /.multiSelectBox -->
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

<c:if test="${not empty items}">
<%
    
    List itemsList=(List) jspContext.getAttribute("items");

    JSONArray jsonArr=new JSONArray();
    JSONArray jsonArrSelectedValues=new JSONArray();
    String optionValue=(String) jspContext.getAttribute("itemValue");
    String optionLabel=(String) jspContext.getAttribute("itemLabel");

    
    
    
    Object valueObject =(Object) jspContext.getAttribute("selectedValue");

    List selectedIdsList = new ArrayList();
    
    if(valueObject !=null){
    	if(valueObject instanceof String){
        	String[] idArrays = null;
        	String valueString = (String) valueObject;
            if(!StringUtils.isEmpty(valueString)){
            	finalValue = valueString;
                idArrays = valueString.split(",");
                selectedIdsList = Arrays.asList(idArrays);
            }
        }else if(valueObject instanceof List){
        	selectedIdsList = (List)valueObject;
        }
    }
    
    
    

    for(Object object:itemsList)
    {
        Object value= PropertyUtils.getNestedProperty(object,optionValue );
        Object label =  PropertyUtils.getNestedProperty(object,optionLabel );
        JSONObject jsonObject=new JSONObject();
        
        String sLabel = "";
        String sValue = "";
        
        if(label != null){
        	sLabel = HtmlUtils.htmlEscape(label.toString());
        }
        if(value != null){
        	 sValue = HtmlUtils.htmlEscape(value.toString());
        }
        

        
        jsonObject.put("itemLabel", sLabel);
        jsonObject.put("itemValue", sValue);
        jsonArr.add(jsonObject);
        
        
        
        if(valueObject !=null && !selectedIdsList.isEmpty()  && selectedIdsList.contains(sValue)){
            jsonArrSelectedValues.add(jsonObject);
        }
        
    }
    jspContext.setAttribute("optionsValuesMap", jsonArrSelectedValues.toString());
   jspContext.setAttribute("optionsItemsMap", jsonArr.toString());
%>
</c:if>


 <c:if test="${not empty itemsMap}">
 <%! 
   public JSONArray getJSONArrayFromMap(Map<String, String> itemMap,JSONArray jsonArray) { 
	 for (Map.Entry<String, String> entry : itemMap.entrySet())
 	{
 		JSONObject jsonObject=new JSONObject();
 		
 		String sLabel = "";
 	    String sValue = "";
 	    
 		if(entry.getValue() != null){
 			sLabel = HtmlUtils.htmlEscape(String.valueOf(entry.getValue()));
 		}
 		
 		
 		if(entry.getKey() != null){
 			sValue = HtmlUtils.htmlEscape(String.valueOf(entry.getKey()));
 		}
 		jsonObject.put("itemLabel", sLabel);
         jsonObject.put("itemValue", sValue);
         jsonArray.add(jsonObject);
 	} 
	 return jsonArray;
   } 
%>

<%
    
	Map<Object, Object> itemsMapTemp=(Map) jspContext.getAttribute("itemsMap");
	Map<String, String> itemsMap = new HashMap();
	for (Map.Entry<Object, Object> entry : itemsMapTemp.entrySet())
	{
		itemsMap.put(String.valueOf(entry.getKey()), entry.getValue()==null ? null:String.valueOf(entry.getValue()));
	}


    JSONArray jsonArr=new JSONArray();
    JSONArray jsonArrSelectedValues=new JSONArray();
    
    Object valueObjectMap =(Object) jspContext.getAttribute("selectedValueMap");
    
    
    
    if(valueObjectMap != null ){
    	
    	if(valueObjectMap instanceof String){
        	String[] idArrays = null;
        	Map<String, String> selectedValuesObjectMap = new HashMap();
        	String valueString = (String) valueObjectMap;
        	
            if(!StringUtils.isEmpty(valueString)){
            	finalValue = valueString;
                idArrays = valueString.split(",");
                for(String strIds : idArrays){     
                	
                	selectedValuesObjectMap.put(strIds, itemsMap.get(strIds));
                }
            }
            getJSONArrayFromMap(selectedValuesObjectMap, jsonArrSelectedValues);
            
        }else if(valueObjectMap instanceof Map){
        	Map<Object, Object> selectedValuesObjectMapTemp=(Map) valueObjectMap;
        	Map<String, String> selectedValuesObjectMap = new HashMap();
        	for (Map.Entry<Object, Object> entry : selectedValuesObjectMapTemp.entrySet())
        	{
        		selectedValuesObjectMap.put(String.valueOf(entry.getKey()), entry.getValue()==null ? null:String.valueOf(entry.getValue()));
        	}
        	getJSONArrayFromMap(selectedValuesObjectMap, jsonArrSelectedValues);
        }    	
    }
    
    getJSONArrayFromMap(itemsMap, jsonArr);
    
    jspContext.setAttribute("optionsValuesMap", jsonArrSelectedValues);
   	jspContext.setAttribute("optionsItemsMap", jsonArr);
%>
</c:if>
 
<script>

    $("#multiselectbox_"+"${id}").data("optionsItemsMap",${optionsItemsMap});
    $("#multiselectbox_"+"${id}").data("optionsValuesMap",${optionsValuesMap});
</script>

<!-- JS CODE -->
<script>
	function refDescMultiselectBOX(selectOb) {

		selectOb.next().find(".ms-selectable, .ms-selection").find(".ms-list")
				.find("li").each(
						function() {
							$(this).attr(
									"title",
									selectOb.parent().prev(
											".itemDescriptionContainer").find(
											"input[p_multi_label^='"
													+ $(this).text() + "']")
											.attr("p_multi_desc"));
						})
	}

	$(document).ready(function() {

		var i = "<c:out value='${tabindex}' />";
		jQuery('.ms-container input').attr("tabindex", i);
		var id = "<c:out value='${id}' />";
		$(".ms-list").click(function() {
			refDescMultiselectBOX($("#" + id));
		});

		setTimeout(function() {
			refDescMultiselectBOX($("#" + id))
		}, 1000);
		
		
		if ($('.ms-selection').find('li').length == $('.ms-selectable').find('li').length) {
			$('.ms-selectable').find('.ms-list').css('max-height','none');
			var maxHeight = $('.ms-selection').find('.ms-list').css('max-height');
			$('.ms-selectable').find('.ms-list').css('max-height',maxHeight);
		}
		
		var ids = ["#<c:out value='${id}'/>"];
		executeOnLoad(ids);

	});
	
	

    var executed = false;
	var initAngApp = (function() {
	    return function() {
	        if (!executed) {
	            executed = true;
	        	angular.element(document).ready(function() {
	        	    angular.bootstrap($(".multiselectBox-container"), ['myApp']);
	        	});
	        }
	    };
	})();

	initAngApp();
	

</script>
<%
	String fieldName = null;
	
	if (name == null) {
		fieldName = path;
	} else {
		fieldName = name;
	}
	try {
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && finalValue!=null && !finalValue.isEmpty()) {
			
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, finalValue);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in multiItemSelect tag :" + e.getMessage());
	}
%>