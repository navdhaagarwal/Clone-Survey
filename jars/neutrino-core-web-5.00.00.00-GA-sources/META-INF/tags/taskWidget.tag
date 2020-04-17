<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ tag import="java.util.List"%>
<%@ tag import="java.util.ArrayList"%>
<%@ tag import="java.util.Set"%>
<%@ tag import="java.util.HashSet"%>
<%@ tag import="java.util.Map"%>
<%@ tag import="java.util.LinkedHashMap"%>
<%@ tag import="java.lang.String"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="name" required="true"%>
<%@ attribute name="filterableCol" required="true" type="java.util.List"%>
<%@ attribute name="taskList" required="true" type="java.util.List"%>
<%@ attribute name="accordionType" required="false"
	type="java.lang.String"%>
<c:set var="filterableColSize" scope="request" value="0">
</c:set>
<c:set var="comma" scope="request" value=",">
</c:set>
<c:forEach items="${filterableCol}" var="col" varStatus="colIndex" >
<c:if test="${col!='TAT'}">
<c:set var="filterableColSize" scope="request" value="${filterableColSize+1}"/>
</c:if>	
</c:forEach>
<script type="text/javascript">
(function(){
	
	var taskWidgetTagScriptInput = {};
	taskWidgetTagScriptInput = {
			id_taskWidget : "<c:out value='${id}' />",
			filterableColSize : "${filterableColSize}"
	}
	taskWidgetTagScript(taskWidgetTagScriptInput);
})();
</script>
<c:set var="id" scope="request" value="${id}">
</c:set>
<c:set var="filterableCol" scope="request" value="${filterableCol}">
</c:set>
<c:set var="taskList" scope="request" value="${taskList}">
</c:set>
<%
    List<Map<String, Object>> taskList = (List<Map<String, Object>>) request.getAttribute("taskList");
    List<String> filterableCol = (List<String>) request.getAttribute("filterableCol");
    Set<String> set = null;
    Map<String, ArrayList<String>> filterCols = new LinkedHashMap<String, ArrayList<String>>();
    int filterableColSize = 0;
    for (String col : filterableCol) {
        set = new HashSet<String>();
        for (Map<String, Object> map : taskList) {
            Set<String> keyset = map.keySet();
            for (String key : keyset) {

                if (key.equals(col)) {
                    set.add(String.valueOf(map.get(key)));
                }

            }
            filterCols.put(col, new ArrayList<String>(set));
        }
        if (col != "TAT") {
            filterableColSize++;
        }

    }

    String id = (String) request.getAttribute("id") + "_filterCols";
    request.setAttribute(id, filterCols);
%>
<c:set var="filterableColSize" scope="request"
	value="<%=filterableColSize%>">
</c:set>
<c:set var="nameToDisplay" value="${fn:replace(name, '_', ' ')}" />
<div id="<c:out value='${id}' />_cardBoardAId" class="row card-board clearfix">
	<div id="<c:out value='${id}' />_cardBoardAIdChild1" class="col-sm-3 resp-card-label">
		<div class="panel-group p-r5 " id="<c:out value='${id}' />_accordion2">
			<div class="panel panel-primary">
				<div
					class=" panel-heading black-bg color-white row p-t5 p-b5">

					<div class="col-sm-2 crd-spacing p-l5">${fn:length(taskList)}</div>
					<div class="col-sm-8 crd-spacing"><c:out value='${nameToDisplay}' /></div>
					<div class="col-sm-2 crd-spacing p-r5">
						<a class="float-r card-accordion-toggle accordion-toggle color-white no-txt-dec collapsed"
							data-toggle="collapse" data-parent="#<c:out value='${id}' />_accordion2"
							href="#<c:out value='${id}' />_collapseOne"> <i class="glyphicon glyphicon-filter "></i>
						</a>
					</div>
				</div>
				<div id="<c:out value='${id}' />_collapseOne" class="panel-collapse collapse ">
					<div id="<c:out value='${id}' />_leftFilters"
						class="card-accordion-inner panel-body">
						<c:forEach
							items='<%=request.getAttribute((request.getAttribute("id") + "_filterCols"))%>'
							var="filterColEntry" varStatus="filterColsIndex">
							<c:set var="filterKey"
								value="${fn:replace(filterColEntry.key, 
                                '_', ' ')}" />
							<c:choose>
								<c:when test="${filterColEntry.key== 'TAT'}">

									<div class="row m-b5">
										<label> <strong><c:out value='${filterKey}' />: </strong>
										</label>
										<div class="color tool">
											<c:forEach items="${filterColEntry.value}" var="filterColVal"
												varStatus="filterColValIndex">

												<a id="<c:out value='${id}' />_filterTAT" href='javascript:void(0)'
													onclick="javascript:filterTAT('${filterColVal}',this.id,'${filterableColSize}');"
													class="${filterColVal}"></a>

											</c:forEach>
										</div>
									</div>
								</c:when>
								<c:when test="${filterColEntry.key == 'Application_No'}">
									<div class="row m-b5">
										<div class="col-sm-12">
											<label> <strong><c:out value='${filterKey}' />: </strong>
											</label> <select id="<c:out value='${id}' />_filterId${filterColsIndex.index}"
												class="form-control chosen_a sortDataInSelect"
												name="stagesName${filterColsIndex.index}" tabindex="-1"
												style="display: inherit;">
												<c:forEach items="${filterColEntry.value}" var="stageList">
													<option value="<c:out value='${stageList}' />"><c:out value='${stageList}' /></option>
												</c:forEach>
											</select>
										</div>
									</div>
								</c:when>
								<c:otherwise>
									<div class="row m-b5">
										<div class="col-sm-12">
											<label> <strong><c:out value='${filterKey}' />: </strong>
											</label> <select id="<c:out value='${id}' />_filterId${filterColsIndex.index}"
												class="form-control chosen_a " name="stagesName${filterColsIndex.index}"
												tabindex="-1" style="display: inherit;">
												<option value="Select">Select</option>
												<c:forEach items="${filterColEntry.value}" var="stageList">
													<option value="<c:out value='${stageList}' />"><c:out value='${stageList}' /></option>
												</c:forEach>
											</select>
										</div>
									</div>
								</c:otherwise>
							</c:choose>
						</c:forEach>
						<div class="f-11">
							<a id="<c:out value='${id}' />_clearfilterId" href='javascript:void(0)'
								onclick="javascript:clearfilter(this.id,'${filterableColSize}');">Clear
								Filter</a>
						</div>

					</div>
				</div>
			</div>
		</div>

	</div>
	<%
	    List<Map<String, Object>> tasks = (List<Map<String, Object>>) request.getAttribute("taskList");
	    int i = 0;
	%>
	<div id="<c:out value='${id}' />_cardBoardAIdChild2" class="col-sm-9 container">
		<c:forEach items="${taskList}" var="map" varStatus="listIndex">
			<div id="<c:out value='${id}' />_cardBoardAId<c:out value='${listIndex.count}' />" class="child"
				<c:choose>
			<c:when test="${listIndex.count>='7'}">
		 style="display: none;">
			</c:when>
			<c:otherwise>
				>
			</c:otherwise>
				</c:choose>
				<div id="<c:out value='${id}' /></div>_cardOuterAId<c:out value='${listIndex.count}' />"
				class="card-outer
			<%
			String tat = String.valueOf(tasks.get(i).get("TAT"));
			if(tat==null)
			{
			%>" >
				<%
				    } else if (tat.equals("yellow")) {
				%>
				card-yellow">
				<%
				    } else if (tat.equals("green")) {
				%>
				card-green">
				<%
				    } else if (tat.equals("orange")) {
				%>
				card-orange">
				<%
				    } else if (tat.equals("red")) {
				%>
				card-red">
				<%
				    }
				    else
				    {
				    	%>
				    	" >
				    	<%
				    	
				    }
				        
				%><c:set var="leadUrl" value="${casViewConfig['cas.lead.create']}"></c:set>
				<div class="card-body">
					<c:forEach items="${map}" var="entry" varStatus="mapIndex">
					<c:set var="entryKey"
								value="${fn:replace(entry.key, 
                                '_', ' ')}" />
                <c:if test="${mapIndex.index==0}">
                <c:if test='<%="1".equals(String.valueOf(tasks.get(i).get("Loan_Stage")))%>'>
                <a href='${pageContext.request.contextPath}/app<c:out value='${leadUrl}' />/view/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>'><div><strong>Lead No:</strong> <%=String.valueOf(tasks.get(i).get("Application_No"))%></div>
				</a>
				</c:if>
				<c:if
					test='<%="2".equals(String.valueOf(tasks.get(i).get("Loan_Stage")))%>'>
					
				<c:if test=	'<%="FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
				
				<a
								href='${pageContext.request.contextPath}/app/FieldInvestigation/FieldInvestigationVerification/createFiForm?taskId=<%=String.valueOf(tasks.get(i).get("Task_ID"))%>&fromDashboard="true"'><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				
				</c:if>

				<c:if
					test='<%="MAL".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/LoanApplication/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET');"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				
				</c:if>
                <c:if
					test='<%="JLG".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>

					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/jointLiabilityGroup/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>

				</c:if>
                <c:if
					test='<%="SHG".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>

					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/selfHelpGroup/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>

				</c:if>
                <c:if
					test='<%="KCC".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>

					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/kisanCreditCard/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>

				</c:if>
                <c:if
					test='<%="AGRL".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>

					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/AgricultureLoan/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>

				</c:if>
                <c:if
					test='<%="EDU".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>

					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/EducationLoan/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>

				</c:if>
                <c:if
					test='<%="FE".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>

					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/FarmEquipment/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>

				</c:if>
				<c:if
					test='<%="CEQ".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>

					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/CommercialEquipment/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>

				</c:if>
				<c:if
					test='<%="ML".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/MortgageLoan/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				
				</c:if>
				
				<c:if
					test='<%="CC".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/CreditCardDetails/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				
				</c:if>
				<c:if
					test='<%="PL".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/PersonalLoan/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				
				</c:if>
				<c:if
					test='<%="LAP".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/LAP/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				
				</c:if>
				<c:if
					test='<%="CL".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/ConsumerDurable/editApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				
				</c:if>
				<c:if
					test='<%="CV".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/CommercialVehicle/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				
				</c:if>
				<c:if
					test='<%="MHL".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/MicroHousingLoan/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				</c:if>
				<c:if
					test='<%="OMNI".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/OmniLoan/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				</c:if>
				<c:if
					test='<%="GL".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/GoldLoan/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				</c:if>
				<c:if
					test='<%="FAS".equals(String.valueOf(tasks.get(i).get("Product_Name"))) && !"FIV".equals(String.valueOf(tasks.get(i).get("Stage")))%>'>
					
					<a
								href='javascript:void(0);' onclick="openApplicationForDashboard('${pageContext.request.contextPath}/app/FinanceAgainstSecurity/createApp/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>','GET')"><div>
									<strong>Application No:</strong>
									<%=String.valueOf(tasks.get(i).get("Application_No"))%></div> </a>
				</c:if>
				</c:if>
				<c:if test='<%=tasks.get(i).get("Loan_Stage") == null%>'>
					<%-- <a
						href='${pageContext.request.contextPath}/app/adhoc/view/<%=String.valueOf(tasks.get(i).get("Task_ID"))%>'><div>
							<strong>Title:</strong>
							<%=String.valueOf(tasks.get(i).get("Title"))%></div> </a> --%>
					<c:choose>
						<c:when test='<%=tasks.get(i).get("Master") == null%>'>
							<a
								href='<%=String.valueOf(tasks.get(i).get("NEW_URL"))%><%=String.valueOf(tasks.get(i).get("Task_ID"))%>'><div>
									<strong><%=String.valueOf(tasks.get(i).get("TITLE_KEY"))%>:</strong>
									<%=String.valueOf(tasks.get(i).get("Title"))%></div> </a>
						</c:when>
						<c:otherwise>
							<c:choose>
								<c:when
									test='<%=tasks.get(i).get("Display_Name").equals("New Master Data")%>'>
									<a
										href='${pageContext.request.contextPath}/app/<%=String.valueOf(tasks.get(i).get("Master"))%>/view/<%=String.valueOf(tasks.get(i).get("Master_Id"))%>'><div>
											<strong><%=String.valueOf(tasks.get(i).get("Display_Name"))%></strong>
										</div> </a>
								</c:when>
								<c:otherwise>
									<a
										href='${pageContext.request.contextPath}/app/<%=String.valueOf(tasks.get(i).get("Master"))%>/view/<%=String.valueOf(tasks.get(i).get("Master_Id"))%>'><div>
											<strong>Name: </strong><%=String.valueOf(tasks.get(i).get("Display_Name"))%>
										</div> </a>

								</c:otherwise>
							</c:choose>
						</c:otherwise>
					</c:choose>
				</c:if>
				</c:if>
				<c:if
					test="${entryKey!= 'TAT' && entryKey!= 'NEW URL' && entryKey!= 'TITLE KEY' && entryKey!= 'Title' && entryKey!= 'Task ID' && entryKey!= 'Application No' && entryKey!= 'Loan Stage' && entryKey!= 'Master Id' && entryKey!= 'Display Name' && entryKey!='Product Name' }">
					<div>
						<strong><c:out value='${entryKey}' />:</strong>
						<c:choose>
							<c:when test="${entry.value != null && entry.value != 'null'}">
						${entry.value}
						</c:when>
							<c:otherwise>
						Not Available
						</c:otherwise>
						</c:choose>
					</div>

				</c:if>
		</c:forEach>
	</div>
</div>
</div>
<c:set var="listIndexCount" scope="request" value="${listIndex.count}">
</c:set>
<%
    i++;
%>
</c:forEach>
</div>
<c:if test="${listIndexCount>'6'}">
	<div class="col-sm-3"></div>
	<a id="<c:out value='${id}' />_cardBoardAIdChild2_viewAll" href='javascript:void(0)'
		onclick="javascript:viewAll('#${id}_cardBoardAIdChild2');">More</a>
	<a id="<c:out value='${id}' />_cardBoardAIdChild2_collapseView"  href='javascript:void(0)'
		onclick="javascript:collapseView('#${id}_cardBoardAIdChild2');"
		style="display: none;">Less</a>
	<div class="row"></div>
</c:if>
</div>