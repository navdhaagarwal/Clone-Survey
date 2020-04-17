<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<%@ attribute name="entityName" required="true"%>
<%@ attribute name="entityID" required="true"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="parentStage"%>
<%@ attribute name="childStage"%>

<script type="text/javascript">
<!--
	$(document).ready(function(){
		$
		.ajax({
			type : "POST",
			url : "${pageContext.request.contextPath}/app/RuleErrors/getErrorMessages",
			data : ({
				entityName : "<c:out value='${entityName}' />",
				 entityId   : "<c:out value='${entityID}'/>",
				 parentStage   : "<c:out value='${parentStage}'/>",
				childStage   : "<c:out value='${childStage}'/>" 
			}),
			success : function(result) {
				if(result != ''){
					$("#rule-error-block-<c:out value='${id}'/>").removeClass('block-no');
					$("#rule-error-block-<c:out value='${id}'/> #error-message").html(result);
				}
			},
			error : function(jqXHR, textStatus, errorThrown) {
				$("#rule-error-block-<c:out value='${id}'/>").removeClass('block-no');
				$("#rule-error-block-<c:out value='${id}'/> #error-message").html('Problem in getting Failed rules detail');
			}
			
		});
	});
//-->

</script>

<div id="rule-error-block-<c:out value='${id}'/>" class="block-no rule-error-container">
	<div class="alert alert-danger">
		<div class="panel-group" id="accordion2">
			<div class="panel panel-primary">
				<div class="panel-heading">
					<a class="accordion-toggle" data-toggle="collapse"
						data-parent="#accordion2" href="#collapseOne_1"><u><b><spring:message
									code="rule.label.ruleerrorheader"></spring:message> </b><i
							class=" glyphicon glyphicon-chevron-down" title="Press to expend rule result"></i></u>
					</a>
				</div>
				<div id="collapseOne_1" class="panel-collapse collapse in">
					<div class="panel-body" id="collapse">
						<div class="row">
							<div class="col-sm-12 txt-l" id="error-message"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
