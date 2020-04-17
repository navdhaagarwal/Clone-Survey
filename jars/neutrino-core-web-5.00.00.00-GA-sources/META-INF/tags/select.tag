<%@tag import="com.nucleus.core.NeutrinoSpringAppContextUtil"%>
<%@tag import="org.apache.commons.lang3.StringUtils"%>
<%@tag import="com.nucleus.logging.BaseLoggers"%>
<%@tag import="java.lang.Exception"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@tag import="com.nucleus.core.exceptions.SystemException"%>
<%@tag import="com.nucleus.core.genericparameter.entity.GenericParameter"%>
<%@tag import="com.nucleus.core.genericparameter.service.GenericParameterService"%>
<%@tag import="java.util.List"%>
<%@tag import="java.util.ListIterator"%>
<%@tag import="com.nucleus.web.tag.TagProtectionUtil"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ attribute name="disabled"%>
<%@ attribute name="id"%>
<%@ attribute name="path"%>
<%@ attribute name="name"%>
<%@ attribute name="value"%>
<%@ attribute name="placeHolderKey"%>
<%@ attribute name="itemValue"%>
<%@ attribute name="colSpan"%>
<%@ attribute name="itemLabel"%>
<%@ attribute name="itemCode"%>
<%@ attribute name="items" type="java.util.List"%>
<%@ attribute name="itemsMap" type="java.util.Map"%>
<%@ attribute name="tooltipKey"%>
<%@ attribute name="errorPath"%>
<%@ attribute name="messageKey"%>
<%@ attribute name="helpKey"%>
<%@ attribute name="labelKey"%>
<%@ attribute name="mandatory"%>
<%@ attribute name="selectBoxColSpan"%>
<%@ attribute name="viewMode"%>
<%@ attribute name="tabindex"%>
<%@ attribute name="defaultValue"%>
<%@ attribute name="labelDynamicForm"%>
<%@ attribute name="defaultItemLabel"%>
<%@ attribute name="defaultItemValue"%>
<%@ attribute name="dynamicFormToolTip"%>
<%@ attribute name="itemDescription"%>
<%@ attribute name="pathPrepender" %>
<%@ attribute name="validators" %>
<%@ attribute name="genericParameterType" %>
<%@ attribute name="parentCode" %>
<%@ attribute name="sortBy" %>
<%@ attribute name="comparatorType" %>
<%@ attribute name="modificationAllowed"%>
<%@ attribute name="conditionStatement"%>
<%@ attribute name="conditionValue"%>
<%@ attribute name="maskedValue"%>
<%@ attribute name="maskedPath"%>
<%
	    		String name = (String) jspContext.getAttribute("name");
				String path = (String) jspContext.getAttribute("path");
				/*
		  		Temporarily Code commented -Attributes made non-mandatory
		 		 */
				/* if (name == null && path == null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' must be specified");
				} else 
				if (name != null && path != null) {
					throw new SystemException(
							"Either of attributes 'name' or 'path' can be specified at once");
				} */
				
				String fieldName=null;
				
				if(name == null){
				 	fieldName=path;
				}else{
					fieldName=name;
				} 
			try {
				 List<GenericParameter> genericParamList = null;
				if(genericParameterType != null){
				   GenericParameterService  genericParameterService=NeutrinoSpringAppContextUtil.getBeanByName("genericParameterService", GenericParameterService.class);
				  Class<GenericParameter> genParamClassName = (Class<GenericParameter>) Class.forName(genericParameterType);
	              
	                 if (parentCode != null && !(parentCode.isEmpty())) {
	                    genericParamList = genericParameterService.findChildrenByParentCode(parentCode, genParamClassName);
	                } else { 
	                    
	                	genericParamList = genericParameterService.retrieveTypes(genParamClassName);
	               }
				
				// Move others to last if present
	                	  GenericParameter genericParameterForOthers = null;
	                	  if(genericParamList !=null){
	                      ListIterator<GenericParameter> it = genericParamList.listIterator();
	                      while (it.hasNext()) {
	                          GenericParameter genericParameter = it.next();
	                          if ("others".equalsIgnoreCase(genericParameter.getCode())) {
	                              genericParameterForOthers = genericParameter;
	                              it.remove();
	                          }
	                      }
	                	  }

	                      if (genericParameterForOthers != null) {
	                          genericParamList.add(genericParameterForOthers);
	                      }
	                      genericParameterService.sortGenericParameterList(genericParamList, sortBy, comparatorType);
				   }
				  
				String regionalVisibility=(String)request.getAttribute(fieldName+"_regionalVisibility");
					String mandatory=(String)request.getAttribute(fieldName+"_mandatoryMode");
					String viewMode=(String)request.getAttribute(fieldName+"_viewMode");
					String labelKey=(String)request.getAttribute(fieldName+"_label");
					String placeHolderKey=(String)request.getAttribute(fieldName+"_placeHolderKey");
					String tooltipKey=(String)request.getAttribute(fieldName+"_toolTipKey");	
				
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
				if(genericParamList!=null){	
					jspContext.setAttribute("items",genericParamList);
					
				if(itemValue == null || itemValue == ""){
						jspContext.setAttribute("itemValue","id");
				}
				
				if(itemLabel == null || itemLabel == ""){
						jspContext.setAttribute("itemLabel","name");
				}
				}
			 } catch (Exception e) {
	                BaseLoggers.exceptionLogger.error("No Such Class Exist:" + e);
	                throw new SystemException("No Such Class Exist:" + e);
	           }	
				
				
%>

<%-- <%@ attribute name="className"%>
<%@ attribute name="searchColList"%>
<%@ attribute name="staticFlag"%> --%>

<c:if test="${regionalVisibility eq true}">
<%-- <c:if test="${(not empty items) and (not empty itemLabel) and (not empty itemValue) and (fn:length(items) gt 10)}">
     	<c:set var="pagination" value="true" scope="page" />
     	<c:set var="pageSize" value="10" scope="page" />
</c:if>
 --%>
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
             <c:if test="${fn:trim(conditionParams[0]) eq 'maxLength'}">
                 <c:set var = "maxLength" value = "${fn:trim(conditionParams[1])}" scope="page" />
             </c:if>
         </c:forEach>
     </c:if>
 </c:forEach>
</c:if>
<c:if test="${not empty viewMode}">
	<c:if test="${viewMode eq true}">
		<c:set var="disabled" value="${viewMode}" scope="page" />
		<c:set var="placeHolderKey" value="" scope="page" />
		<c:set var="tooltipKey" value="" scope="page" />
	</c:if>
</c:if>

<c:choose>
	<c:when test="${alignment eq 'rtl'}">
	<c:set var="alignmentClass" value="chosen-rtl" scope="page" />

	</c:when>
	<c:when test="${alignment eq 'ltr'}">
		<c:set var="alignmentClass" value="" scope="page" />
	
	</c:when>
	<c:otherwise>
		<c:set var="alignmentClass" value="" scope="page" />
	
	</c:otherwise>
</c:choose>


<c:if test="${empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="label.select.one" />
	</c:set>
</c:if>

<c:if test="${ not empty placeHolderKey}">
	<c:set var="placeHolderMessage" scope="page">
		<spring:message code="${placeHolderKey}"></spring:message>
	</c:set>
</c:if>

<c:if test="${not empty mandatory}">
	<c:set var="validators" scope="page">
			${validators} required
		</c:set>
</c:if>

<c:if test="${empty mandatory}">
	<c:set var="nonMandatoryClass" value="nonMandatory" scope="page" />
</c:if>

<c:if test="${empty colSpan}">
	<c:set var="colSpan" value="12"	scope="page" />
</c:if>


<c:set var="selectBoxSpanClass" value="col-sm-10" scope="page" />
<c:if test="${not empty selectBoxColSpan}">
	<c:set var="selectBoxSpanClass" value="col-sm-${selectBoxColSpan}"
		scope="page" />
</c:if>

<c:if test="${not empty tooltipKey}">
	<c:set var="tooltipMessage" scope="page">
		<spring:message code="${tooltipKey}"></spring:message>
	</c:set>
</c:if>

<c:if test="${empty itemDescription}">
	<c:set var="itemDescription" scope="page" value="${itemLabel}" />
</c:if>

<div class="itemDescriptionContainer">
       <c:if test="${pagination ne true}">
				<c:forEach  varStatus="i"  items="${items}" var="item">					
						
						<input p_multi_desc="<c:out value='${item[itemDescription]}' />" p_multi_label="<c:out value='${item[itemLabel]}' />" disabled="disabled" type="hidden" class="p_desc_mutli"/> 	
				</c:forEach>
	</c:if>
	
</div>
<c:if test="${not empty dynamicFormToolTip}">
	<c:set var="tooltipMessage" scope="page">
		<c:out value='${dynamicFormToolTip}' />
	</c:set>
</c:if>
<c:set var="spanClass" value="col-sm-${colSpan}" scope="page" />

<c:if test="${not empty defaultValue}">
	<c:set var="defaultValue" value="selected" scope="page" />
</c:if>

<div id="<c:out value='${id}'></c:out>-control-group"
	class="fancy-select select-ctrl form-group input-group input-group <c:out value='${spanClass}' /> ${nonMandatoryClass} reset-m-l p-r5">
	<c:if test="${not empty labelKey}">
		<label><strong><spring:message code="${labelKey}"></spring:message>
			<c:if test="${not empty mandatory}">
				<span class='color-red'>*</span>
			</c:if> </strong></label>
	</c:if>

	<c:if test="${not empty labelDynamicForm}">
		<label><strong><c:out value='${labelDynamicForm}' /> <c:if
				test="${not empty mandatory}">
				<span class='color-red'>*</span>
			</c:if> </strong></label>
	</c:if>
	<c:choose>
		<c:when test="${not empty path}">
			<spring:bind path="${path}">
				<c:set var="preEvalValue" value="${status.value}"></c:set>
			</spring:bind>
			<form:select id="${id }" path="${path}" 
				tabindex="${tabindex}" 
				disabled="${disabled}" name="${name}" 
				cssClass="form-control ${selectBoxSpanClass}  ${validators}  tooltip chosen_a ${alignmentClass}"
				data-placeholder="${placeHolderMessage}" data-original-title="${tooltipMessage}">
			<c:if test="${not empty items}">
					<c:choose>
						<c:when
							test="${(not empty defaultItemLabel) and (not empty defaultItemValue) }">

							<form:option value="${defaultItemValue}"><c:out value='${defaultItemLabel}' /></form:option>
						</c:when>
						<c:otherwise>
							<form:option value=""></form:option>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${(not empty itemLabel) and (not empty itemValue) }">
							
						<c:if test="${pagination ne true}">
							<c:forEach items="${items}" var="item"> 
							<c:choose>
								<c:when test="${item[itemValue] == defaultValue}">
									<form:option selected="selected" value="${item[itemValue]}" data-code="${item[itemCode]}">
									<c:choose>	
													<c:when 
													test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true)) && not empty maskedValue}">
                                                         <c:out value='${maskedValue}' />
                                                    </c:when>
   	                                                 <c:otherwise>
                                                    <c:out value='${item[itemLabel]}' />
                                                    </c:otherwise>
                                                </c:choose>
									</form:option>
								
								</c:when>
								<c:otherwise>
								<form:option  value="${item[itemValue]}" data-code="${item[itemCode]}"><c:out value='${item[itemLabel]}' /></form:option>
								</c:otherwise>
							</c:choose>
							
						</c:forEach>
						</c:if>
						<c:if test="${pagination eq true}">
							<c:forEach var="i" begin="0" end="${pageSize-1}">
								<c:set var="item" value="${items[i]}"></c:set>
								<c:choose>
									<c:when test="${item[itemValue] == defaultValue}">
										<form:option selected="selected" value="${item[itemValue]}"
											data-code="${item[itemCode]}">
											<c:choose>	
													<c:when 
													test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true)) && not empty maskedValue}">
                                                         <c:out value='${maskedValue}' />
                                                    </c:when>
   	                                                 <c:otherwise>
                                                    <c:out value='${item[itemLabel]}' />
                                                    </c:otherwise>
                                                </c:choose>											
											</form:option>

									</c:when>
									<c:otherwise>
										<form:option value="${item[itemValue]}"
											data-code="${item[itemCode]}"><c:out value='${item[itemLabel]}' /></form:option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</c:if>
							</c:when>
						<c:otherwise>
								<form:options items="${items}" />
						</c:otherwise>
					</c:choose>

				</c:if>
				<c:if test="${empty items}">
					<c:if test="${not empty itemsMap}">
						<c:choose>
							<c:when
								test="${(not empty defaultItemLabel) and (not empty defaultItemValue) }">
								<form:option value="${defaultItemValue}"><c:out value='${defaultItemLabel}'/></form:option>
							</c:when>
							<c:otherwise>
								<form:option value=""></form:option>
							</c:otherwise>
						</c:choose>
						<form:options items="${itemsMap}" selected="${defaultValue}" />
					</c:if>
				</c:if>
			</form:select>
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when
					test="${not empty name && not empty disabled && disabled==true }">
					<select id="<c:out value='${id}'></c:out>" name="<c:out value='${name}' />" 
						disabled="<c:out value='${disabled}' />"
						class="form-control <c:out value='${selectBoxSpanClass}' />  ${validators}  tooltip chosen_a ${alignmentClass}"
						data-placeholder="${placeHolderMessage}"
						data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}'/>">
						<c:if test="${not empty items}">
							<c:choose>
								<c:when
									test="${(not empty defaultItemLabel) and (not empty defaultItemValue) }">

									<option value="<c:out value='${defaultItemValue}'/>"><c:out value='${defaultItemLabel}'/></option>
								</c:when>
								<c:otherwise>
									<option value=""></option>
								</c:otherwise>
							</c:choose>
								<c:if test="${pagination ne true}">
									<c:forEach items="${items}" var="item">
										<c:choose>
											<c:when
												test="${(not empty value) and (value eq item[itemValue]) }">
												<option value="<c:out value='${item[itemValue]}'/>"
													data-code="<c:out value='${item[itemCode] }'/>"
													selected="selected">
												<c:choose>	
													<c:when 
													test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true)) && not empty maskedValue}">
                                                         <c:out value='${maskedValue}' />
                                                    </c:when>
   	                                                 <c:otherwise>
                                                       	   <c:out value='${item[itemLabel]}' />
                                                    </c:otherwise>
                                                </c:choose>
													</option>
											</c:when>
											<c:otherwise>
												<option value="<c:out value='${item[itemValue]}'/>"
													data-code="<c:out value='${item[itemCode] }'/>"><c:out
														value='${item[itemLabel]}' /></option>
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</c:if>
								<c:if test="${pagination eq true}">
									<c:forEach var="i" begin="0" end="${pageSize-1}">
										<c:set var="item" value="${items[i]}"></c:set>
										<c:choose>
											<c:when
												test="${(not empty value) and (value eq item[itemValue]) }">
												<option value="<c:out value='${item[itemValue]}'/>"
													data-code="<c:out value='${item[itemCode] }'/>"
													selected="selected">
													<c:choose>	
													<c:when 
													test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true)) && not empty maskedValue}">
                                                         <c:out value='${maskedValue}' />
                                                    </c:when>
   	                                                 <c:otherwise>
                                                       	<c:out value='${item[itemLabel]}' />
                                                    </c:otherwise>
                                                </c:choose></option>
											</c:when>
											<c:otherwise>
												<option value="<c:out value='${item[itemValue]}'/>"
													data-code="<c:out value='${item[itemCode] }'/>"><c:out
														value='${item[itemLabel]}' /></option>
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</c:if>
								<c:if test="${empty itemValue and empty itemLabel}">
								<c:forEach items="${items}" var="item">
									<c:choose>
										<c:when test="${(not empty value) and (value eq item) }">
											<option value="<c:out value='${item}'/>" selected="selected">
											<c:choose>	
													<c:when 
													test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true)) && not empty maskedValue}">
                                                         <c:out value='${maskedValue}' />
                                                    </c:when>
   	                                                 <c:otherwise>
                                                       <c:out value='${item}'/>
                                                    </c:otherwise>
                                                </c:choose></option>
										</c:when>
										<c:otherwise>
											<option value="<c:out value='${item}'/>"><c:out value='${item}'/></option>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</c:if>
						</c:if>
						<c:if test="${empty items}">
							<c:if test="${not empty itemsMap}">
								<c:choose>
									<c:when
										test="${(not empty defaultItemLabel) and (not empty defaultItemValue) }">
										<option value="<c:out value='${defaultItemValue}'/>"><c:out value='${defaultItemLabel}'/></option>
									</c:when>
									<c:otherwise>
										<option value=""></option>
									</c:otherwise>
								</c:choose>
								<c:forEach items="${itemsMap}" var="item">
									<option value="<c:out value='${item.value}'/>"><c:out value='${item.key}'/></option>
								</c:forEach>
							</c:if>
						</c:if>
					</select>

				</c:when>
				<c:otherwise>
					<select id="<c:out value='${id }'/>" name="<c:out value='${name}'/>" 
						class="form-control <c:out value='${selectBoxSpanClass}' />  ${validators}  tooltip chosen_a ${alignmentClass}"
						data-placeholder="${placeHolderMessage}"
						data-original-title="${tooltipMessage}" tabindex="<c:out value='${tabindex}'/>">
						<c:if test="${not empty items}">
							<c:choose>
								<c:when
									test="${(not empty defaultItemLabel) and (not empty defaultItemValue) }">

									<option value="<c:out value='${defaultItemValue}'/>"><c:out value='${defaultItemLabel}'/> </option>
								</c:when>
								<c:otherwise>
									<option value=""></option>
								</c:otherwise>
							</c:choose>
							<c:if test="${pagination ne true}">
							<c:forEach items="${items}" var="item">
								<c:choose>
									<c:when test="${(not empty value) and (value == item[itemValue]) }">
										<option value="<c:out value='${item[itemValue]}'/>" selected="selected" data-code="<c:out value='${item[itemCode] }'/>">
											<c:choose>	
													<c:when 
													test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true)) && not empty maskedValue}">
                                                         <c:out value='${maskedValue}' />
                                                    </c:when>
   	                                                 <c:otherwise>
                                                      <c:out value='${item[itemLabel]}'/>
                                                    </c:otherwise>
                                                </c:choose>
										</option>
									</c:when>
									<c:otherwise>
										<option value="<c:out value='${item[itemValue]}'/>" data-code="<c:out value='${item[itemCode] }'/>"><c:out value='${item[itemLabel]}'/></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
                       	</c:if>
						<c:if test="${pagination eq true}">
									<c:forEach var="i" begin="0" end="${pageSize-1}">
										<c:set var="item" value="${items[i]}"></c:set>
										<c:choose>
											<c:when
												test="${(not empty value) and (value == item[itemValue]) }">
												<option value="<c:out value='${item[itemValue]}'/>"
													selected="selected"
													data-code="<c:out value='${item[itemCode] }'/>"><c:out value='${item[itemLabel]}' /></option>
											</c:when>
											<c:otherwise>
												<option value="<c:out value='${item[itemValue]}'/>"
													data-code="<c:out value='${item[itemCode] }'/>"><c:out
														value='${item[itemLabel]}' /></option>
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</c:if>
								<c:if test="${empty itemValue and empty itemLabel}">
								<c:forEach items="${items}" var="item">
									<c:choose>
										<c:when test="${(not empty value) and (value eq item) }">
											<option value="<c:out value='${item}'/>" selected="selected">
											<c:choose>	
													<c:when 
													test="${((not empty viewMode && viewMode eq true)||(not empty disabled && disabled eq true)||(not empty readOnly && readOnly eq true)) && not empty maskedValue}">
                                                         <c:out value='${maskedValue}' />
                                                    </c:when>
   	                                                 <c:otherwise>
                                                    <c:out value='${item}'/>
                                                    </c:otherwise>
                                                </c:choose>
											</option>
										</c:when>
										<c:otherwise>
											<option value="${item}">${item}</option>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</c:if>
						</c:if>
						<c:if test="${empty items}">
							<c:if test="${not empty itemsMap}">
								<c:choose>
									<c:when
										test="${(not empty defaultItemLabel) and (not empty defaultItemValue) }">
										<option value="<c:out value='${defaultItemValue}'/>"><c:out value='${defaultItemLabel}'/></option>
									</c:when>
									<c:otherwise>
										<option value=""></option>
									</c:otherwise>
								</c:choose>
								<c:forEach items="${itemsMap}" var="item">
									<option value="<c:out value='${item.value}'/>"><c:out value='${item.key}'/></option>
								</c:forEach>
							</c:if>
						</c:if>
					</select>

				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
 	<c:if test="${not empty errorPath}">
		<p class="text-danger">
			<form:errors path="${errorPath}" />
		</p>
	</c:if>
</div>
<c:if test="${not empty helpKey}">
	<span class="help-block"><spring:message code="${helpKey}" /></span>
</c:if>
<c:if test="${not empty messageKey}">
	<p class="text-info">
		<spring:message code="${messageKey}" />
	</p>
</c:if>

</c:if>
<!-- select. tag -->
<!-- script moved at the bottom, also it was causing issue in layout -->
<!-- Client side pagination logic -->

<c:if test="${pagination eq true}">
	<c:set var="itmVal" value="'["></c:set>
	<c:set var="itmCode" value="'["></c:set>
	<c:set var="itmLabel" value="'["></c:set>
	<c:set var="firstItm" value="true"></c:set>
	<c:set var="separator" value=""></c:set>
	<c:set var="dblQt" value='"'></c:set>
	<c:if test="${not empty path}">
		<spring:bind path="${path}">
			<c:set var="selectedValue" value="${status.value}"></c:set>
		</spring:bind>
	</c:if>
	<c:if test="${not empty value}">
		<c:set var="selectedValue" value="${value}"></c:set>
	</c:if>
	<c:forEach items="${items}" var="item" varStatus="stat">
		<c:if test="${item[itemValue] eq selectedValue}">
			<c:set var="selectedValueIndex" value="${stat.index}"></c:set>
		</c:if>
		<c:set var="escapeditmVal"><c:out value='${item[itemValue]}' /></c:set>
		<c:set var="escapeditmCode"><c:out value='${item[itemCode]}' /></c:set>
		<c:set var="escapeditmLabel"><c:out value='${item[itemLabel]}' /></c:set>
		<c:set var="itmVal"
			value="${itmVal}${separator}${dblQt}${escapeditmVal}${dblQt}"></c:set>
		<c:set var="itmCode"
			value="${itmCode}${separator}${dblQt}${escapeditmCode}${dblQt}"></c:set>
		<c:set var="itmLabel"
			value="${itmLabel}${separator}${dblQt}${escapeditmLabel}${dblQt}"></c:set>
		<c:if test="${firstItm eq true}">
			<c:set var="separator" value=","></c:set>
			<c:set var="firstItm" value="false"></c:set>
		</c:if>
	</c:forEach>

	<c:set var="itmVal" value="${itmVal}]'"></c:set>
	<c:set var="itmCode" value="${itmCode}]'"></c:set>
	<c:set var="itmLabel" value="${itmLabel}]'"></c:set>

	<script>
			$("#"+escapeSpecialCharactersInId("${id}")).data("pg_itmVal",JSON.parse(${itmVal}));
			$("#"+escapeSpecialCharactersInId("${id}")).data("pg_itmCode",JSON.parse(${itmCode}));
			$("#"+escapeSpecialCharactersInId("${id}")).data("pg_itmLabel",JSON.parse(${itmLabel}));
			$("#"+escapeSpecialCharactersInId("${id}")).data("pageSize",${pageSize});
			$("#"+escapeSpecialCharactersInId("${id}")).data("idWithOutSplChar",replaceSplCharWithUndrScor("${id}"));
			$("#"+escapeSpecialCharactersInId("${id}")).on("chosen:ready",function() {			
				if("${pagination}"=="true")
				{		
					initPagination(escapeSpecialCharactersInId("${id}"),${pageSize},'${selectedValueIndex}');
				}
			});

</script>
</c:if>

<script>

/* function refDescSelect(selectOb) { 		
	selectOb.next().find(".chosen-results").find(".active-result").each(function(){					
	$(this).attr("title",selectOb.parent().prev(".itemDescriptionContainer").find("input[p_multi_label^='" + $(this).text() + "']").attr("p_multi_desc"));
}
)}; */

	$(document).ready(function() {

		$('p.text-danger').css("font-size", "15px");
		$('.chosen-results').attr('tabindex',-1);
		
		var idWithEscapeChars = "#"+escapeSpecialCharactersInId("<c:out value='${id}'/>");
		executeOnLoad([idWithEscapeChars]);
	});
	
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