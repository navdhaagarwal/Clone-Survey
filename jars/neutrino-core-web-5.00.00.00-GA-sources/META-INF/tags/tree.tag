<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<%@ attribute name="VO" rtexprvalue="true" type="com.nucleus.web.useradministration.TreeVO"%>

<input type="checkbox" name="<c:out value='${VO.name}' />" id="<c:out value='${VO.id}' />">   : <c:out value='${VO.name}' />
</br>
<c:forEach items="${VO.childTreeVoList}" var="singleVO" varStatus="status">
    &nbsp;
	<input type="checkbox" name="<c:out value='${singleVO.name}' />" id="<c:out value='${singleVO.id}' />">  : <c:out value='${singleVO.name}' />
	</br>
	<c:forEach items="${singleVO.childTreeVoList}" var="singletwoVO" varStatus="statustwo">
	    &nbsp;&nbsp;
		<input type="checkbox" name="<c:out value='${singletwoVO.name}'/>" id="<c:out value='${singletwoVO.id}'/>">  : <c:out value='${singletwoVO.name}'/>
		</br>
	</c:forEach>
</c:forEach>
