<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="java.util.List"%>
<%@tag import="java.util.Collection"%>
<%@tag import="com.nucleus.entity.Entity"%>
<%@tag import="com.nucleus.autocomplete.TagUtils"%>
<%@tag import="java.util.ArrayList"%>
<%@tag import="java.lang.Exception"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@tag import="com.nucleus.autocomplete.AutocompleteLoadedEntitiesMap"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.nucleussoftware.com/neutrino/web/taglib" prefix="neutrino"%>

	
<%@ attribute name="id"%>
<%@ attribute name="disabled"%>
<%@ attribute name="path"%>
<%@ attribute name="name"%>
<%@ attribute name="value" type="java.util.Collection"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="className"%>
<%@ attribute name="itemLabel"%>
<%@ attribute name="itemValue"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="customURL"%>
<%@ attribute name="styleClass"%>
<%@ attribute name="minCharToBeginSearch"%>
<%@ attribute name="multiSelectBoxSpanClass"%>
<%@ attribute name="maxArguments"%>
<%@ attribute name="minArguments"%>
<%@ attribute name="pageSize"%>
<%@ attribute name="searchColumn"%>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="containsSearchEnabled"%>
<%
	if(value != null) {
		Collection<Object> preselectedItemsList = (Collection<Object>) value;
		String selectedItems = TagUtils.convertListToJsonString(preselectedItemsList, itemValue, itemLabel);
		
		jspContext.setAttribute("preSelectedItems", selectedItems);
	}
%>

<c:if test="${not empty value}">
	<input type="hidden" id="preSelectedItems_<c:out value='${id}' />" value="<c:out value='${preSelectedItems}' />" />
</c:if>

<script>
$(document).ready(function() {
	var tagId = "<c:out value='${id}' />";
	var customURL = $("#"+tagId).attr("data-customURL");
	var className = $("#"+tagId).attr("data-className");
	var itemLabel = $("#"+tagId).attr("data-itemLabel");
	var searchColumn = $("#"+tagId).attr("data-searchCol");
	var itemValue = $("#"+tagId).attr("data-itemValue");
	var preSelectedValues =  $("#preSelectedItems_"+tagId).val();
	var minimumInputLength =  $("#minInputLength_"+tagId).val();
	var pageSize =  $("#pageSize_"+tagId).val();
	var placeHolderMessage =  $("#"+tagId).attr("placeholder");
	var maxArguments = $("#maximumArguments_"+tagId).val();
	var containsSearchEnabled = $("#containsSearchEnabledID_"+tagId).val();
	var inputValue;		
	if(customURL) {
		var url = '${pageContext.request.contextPath}/app';
		customURL = url + customURL;
	} else {
		customURL = "${pageContext.request.contextPath}/app/multiselect/populate";	
	}
	
	$("#"+tagId).select2({
		  ajax: {
			url: customURL,
			type : 'POST',
		    dataType: 'json',
		    delay: 250,
		    data: function (params) {
		    	var pageNo = params.page;
		    	if(typeof pageNo == "undefined") {
		    		var pageNo = 1;
		    	}
		    	var selectedItemsList = $("#"+ tagId).val();
		    	var selectedItems;
		    	if(selectedItemsList != null && selectedItemsList.length > 0) {
		    		selectedItems = selectedItemsList.toString();
		    	}
		    	inputValue = params.term;
		    	if(typeof inputValue === "undefined") {
		    		inputValue = "%";
		    	}
		    	
		      return {
		    	inputValue: inputValue,
		        page: pageNo,
		        pageSize:pageSize,
		        itemLabel: itemLabel,
		        searchColumn: searchColumn,
		        itemValue: itemValue,
		        className: className,
		        itemToBeExcluded: selectedItems,
		        containsSearchEnabled : containsSearchEnabled
		      };
		    },
		    processResults: function (data, params) {
		      toggleShowHideSpan(tagId, false);
		      params.page = params.page || 1;
		      return {
		    	  results: $.map(data.items, function (item) {
	                    return {
	                        text: item[itemLabel],
							name: item[itemLabel],
	                        id: item[itemValue]
	                    }
	                }),
		        pagination: {
		          more: (params.page * pageSize) < data.count
		        }
		      };
		    }
		  },
		  placeholder: placeHolderMessage,
		  escapeMarkup: function (markup) { return markup; },
		  minimumInputLength: minimumInputLength,
		  templateResult: formatRepo,
		  maximumSelectionLength: maxArguments,
		  language: {
			  maximumSelected: function (limit) {
				  toggleShowHideSpan(tagId, true);
				  handleMaxSelectedOptions(escapeSpecialCharactersInId(tagId),limit);
		          return '';
		        },
		        inputTooShort: function () { toggleShowHideSpan(tagId, true);
		        					return ''; 
		        			} ,
		        noResults: function() {
		        	if(inputValue == "%") {
		        		return "No results found";
		        	} else{
		        		return 'No results match "' + inputValue + '"';
		        	}
		        }
		  }
		});

		if(preSelectedValues) {
			var preSelectedItemsJson = JSON.parse(preSelectedValues);
			var preSelectedOptions = $("#"+tagId);
			for (i=0; i<preSelectedItemsJson.length; i++) {
				var option = new Option(retrieveKeyValue(preSelectedItemsJson[i], itemLabel), preSelectedItemsJson[i][itemValue], true, true);
				preSelectedOptions.append(option).trigger('change');
				
	        }
			preSelectedOptions.trigger({
		        type: 'select2:select',
		        params: {
		            data: preSelectedItemsJson
		        }
		    });
		}
		
		function formatRepo (repo) {
		  if (repo.loading) {
		    return repo.text;
		  }
		  var markup = "<div class='select2-result-repository clearfix'></div>" +
		         "<div class='select2-result-repository__title'>" + repo.text + "</div>";
		  
		   return markup;
		}

		/* Function to retrieve value for multilevel key
		   separted by '.' from JSON object (itemJson). */
		function retrieveKeyValue(itemJson, key) { 
			if(key.lastIndexOf('.') == -1) {
		 		 return itemJson[key];
		  } else {
			   var keys = key.split('.');
			   var jsonObj = itemJson;
			   for(var i =0 ; i<keys.length; i++) {
			   	if(jsonObj.hasOwnProperty(keys[i])) {
			   		jsonObj = jsonObj[keys[i]];
			    }
			   }
			   return jsonObj;
			 }
		}
});
	
</script>
<c:if test="${not empty className}">
			<%
				String value = (String) jspContext.getAttribute("className");
				AutocompleteLoadedEntitiesMap.addClassesToMap(value);
			%>
</c:if>
<c:if test="${not empty value}">
	<c:forEach items="${value}" var="item">
		<c:if test="${modificationAllowed == 'false'}"> 
		<c:set var="myVar" value="${myVar}${item[itemValue]}," />
		</c:if>
	</c:forEach>
</c:if>
<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
			required
		</c:set>
</c:if>

<c:if test="${not empty placeHolderKey}">
	<c:set var="placeHolder" scope="page">
		<spring:message code="${placeHolderKey}"></spring:message>
	</c:set>
</c:if>
<c:if test="${empty placeHolderKey}">
	<c:set var="placeHolder" value="Select Some Options" scope="page">
	</c:set>
</c:if>

<c:if test="${not empty minCharToBeginSearch}">
	<c:set var="minInputLength" scope="page" value="${minCharToBeginSearch}" />
</c:if>
<c:if test="${empty minCharToBeginSearch}">
	<c:set var="minInputLength" scope="page" value="0"/>
</c:if>

<c:if test="${not empty minInputLength}">
	<input type="hidden" id="minInputLength_<c:out value='${id}' />" value="<c:out value='${minInputLength}' />" />
</c:if>

<c:if test="${empty maxArguments}">
		<c:set var="maxArgumentsVar" value="3"  scope="page"></c:set>
</c:if>
<c:if test="${not empty maxArguments}">
		<c:set var="maxArgumentsVar" value="${maxArguments}" scope="page"></c:set>
</c:if>
<c:if test="${not empty maxArgumentsVar}">
	<input type="hidden" id="maximumArguments_<c:out value='${id}' />" value="<c:out value='${maxArgumentsVar}' />" />
</c:if>

<c:if test="${empty pageSize}">
		<c:set var="pageSizeVar" value="20"  scope="page"></c:set>
</c:if>
<c:if test="${not empty pageSize}">
		<c:set var="pageSizeVar" value="${pageSize}" scope="page"></c:set>
</c:if>
<c:if test="${not empty pageSizeVar}">
	<input type="hidden" id="pageSize_<c:out value='${id}' />" value="<c:out value='${pageSizeVar}' />" />
</c:if>
<c:if test="${not empty containsSearchEnabled}">
	<input type="hidden" id="containsSearchEnabledID_<c:out value='${id}' />" value="<c:out value='${containsSearchEnabled}' />" />
</c:if>

<div style="font-size: 13px;">
	<c:choose>
		<c:when test="${not empty disabled}">
			<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
					class="form-control <c:out value='${multiSelectBoxSpanClass}' /> <c:out value='${validators}' />" 
					placeholder="<c:out value='${placeHolder}' />"
					data-customURL="<c:out value='${customURL}' />" data-className="<c:out value='${className}' />" 
					data-itemLabel="<c:out value='${itemLabel}' />" data-original-title="<c:out value='${tooltipKey}' />"
					disabled="<c:out value='${disabled}' />" tabindex="<c:out value='${tabindex}' />"
					data-itemValue="<c:out value='${itemValue}' />" data-searchCol="<c:out value='${searchColumn}' />"
			></select>
		</c:when>
		<c:otherwise>
			<select  multiple="multiple" id="<c:out value='${id}' />" name="<c:out value='${name}' />"
					class="form-control <c:out value='${multiSelectBoxSpanClass}' /> <c:out value='${validators}' />"
					placeholder="<c:out value='${placeHolder}' />"
					data-customURL="<c:out value='${customURL}' />" data-className="<c:out value='${className}' />" 
					data-itemLabel="<c:out value='${itemLabel}' />" data-original-title="<c:out value='${tooltipKey}' />"
					tabindex="<c:out value='${tabindex}' />" data-itemValue="<c:out value='${itemValue}' />"
					data-searchCol="<c:out value='${searchColumn}' />"
			></select>
		</c:otherwise>
	</c:choose>

</div>
<%
	
	String fieldName = null;
	String val = (String) jspContext.getAttribute("myVar");
	
	if (name == null) {
		fieldName = path;
	} else {
		fieldName = name;
	}
	
	try {
		
		if (modificationAllowed != null && modificationAllowed.toLowerCase().equals("false") && val!=null && !val.isEmpty()) {
			val = val.substring(0,val.length()-1);
			TagProtectionUtil.addProtectedFieldToRequest(request, fieldName, val);
		}

	} catch (Exception e) {
		System.err.println("***** **** **** Exception in tag UTIL :" + e.getMessage());
	}
%>
