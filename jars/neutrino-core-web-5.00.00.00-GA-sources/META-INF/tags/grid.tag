<%@ attribute name="gridId"%>
<%@ attribute name="columnNames"%>
<%@ attribute name="jsonString"%>
<%@ attribute name="onViewClick"%>
<%@ attribute name="sortOn"%>
<%@ attribute name="sortOrder"%>
<%@ attribute name="heading"%>
<%@ attribute name="xscroll"%>
<%@ attribute name="rowNum"%>
<%@ attribute name="rowList"%>

<c:if test="${sortOn == null}">
	<c:set var="sortOn" value="1" />
</c:if>
<c:if test="${sortOrder == null}">
	<c:set var="sortOrder" value="asc" />
</c:if>
<c:choose>
	<c:when test="${xscroll == null}">
		<c:set var="sScrollX" value="100%" />
		<c:set var="bScrollCollapse" value="false" />
	</c:when>
	<c:when test="${xscroll == 'Y'}">
		<c:set var="sScrollX" value="110%" />
		<c:set var="bScrollCollapse" value="true" />
	</c:when>
	<c:otherwise>
		<c:set var="sScrollX" value="100%" />
		<c:set var="bScrollCollapse" value="false" />
	</c:otherwise>
</c:choose>

<c:if test="${xscroll == null}">
	<c:set var="xscroll" value="N" />
</c:if>

<c:if test="${rowNum == null}">
	<c:set var="rowNum" value="10" />
</c:if>
<c:if test="${rowList == null}">
	<c:set var="rowList" value="10,20,30,40" />
</c:if>


<script>
var oTable;
$(document).ready(function() {
	
			// Code for dynamic table,model generation
			var dtable = '<div id="messageDiv" style="position:absolute;left:0px;z-index:6;">';
			dtable += '</div>';
			dtable += '<div class="container">';
			dtable += '<div id="gridDiv">';
			dtable += '<br>';
			dtable += "<legend><c:out value='${heading}' /></legend>";
			dtable += '<table cellpadding="0" cellspacing="0" border="0" class="table table-striped table-bordered table-hover" id="<c:out value='${gridId}' />">';
			dtable += '<thead>';
			dtable += '<tr>';
			//dtable += '<th>';
			//dtable += '</th>';
			
			var dmodel = '';
			                
			var columnNames = "<c:out value='${columnNames}' />";
			var str_array = columnNames.split(',');
			for(var i = 0; i < str_array.length; i++)
			{
				str_array[i] = str_array[i].replace(/^\s*/, "").replace(/\s*$/, "");
				dtable += '<th>';
				dtable += str_array[i];
				dtable += '</th>';
				
				dmodel += '<div class="form-group">';
				dmodel += '<label class="control-label" for="name">'; 
				dmodel += str_array[i];
				dmodel += '</label>';
				dmodel += '<div class="controls">';
				dmodel += '<input type="text" class="form-control input-large" ';
				dmodel += 'name="'+ str_array[i].toLowerCase() + '" ';
				dmodel += 'id="'+ str_array[i].toLowerCase() + '">';
				dmodel += '</div>';
				dmodel += '</div>';
			}
			dtable += '</tr>';
			dtable += '</thead>';
			dtable += '<tbody>';
			dtable += '</tbody>';
			dtable += '</table>';
			dtable += '</div>';
			dtable += '<hr>';
			//dtable += '<div align="right">';
			//dtable += '<a  class="btn" id="add_Record">Add Record</a>';
			//dtable += '<a  id="delete_Record" class="btn btn-primary  btn-danger">Delete Record</a>';
			//dtable += '</div>';
			//dtable += '<hr>';
			dtable += '</div>';
			
			$('#dynamicGridDiv').append(dtable);
			$('#dynamicModelDiv').append(dmodel);
			
			function fnGetSelected( oTableLocal )
			{
				return oTableLocal.$('tr.row_selected');
			}
			
			var nCloneTh = document.createElement('th');
			//var nCloneTd = document.createElement('td');
			//nCloneTd.innerHTML = '<img src="/finnone-webapp/images/grid/details_open.png">';
			//nCloneTd.className = "center";
			nCloneTh.innerHTML = "<div align='left '>&nbsp;<input type='checkbox' id='selectAll'/> </div>";
			
			//$('#${gridId} thead tr').each( function () {
			//	this.insertBefore( nCloneTh, this.childNodes[0] );
			//} );
				
			//$('#${gridId} tbody tr').each( function () {
			//	this.insertBefore(  nCloneTd.cloneNode( true ), this.childNodes[0] );
			//} );
				
			$(document).ready(function() {

				$(".paging_bootstrap").click(function() {
					undef();
					generate_datatable_tool_tip();

				});
			});
			
			$(document).ready(function() 
			{
				$(".ColVis").click(	function(e) {
					$(".ColVis_collection.TableTools_collection").children('.ColVis_Button.TableTools_Button:first').hide();
				});
			});
			
			
    		$('#delete_Record').click( function() 
    		{
    			var anSelected = fnGetSelected( oTable );
        		if ( anSelected.length !== 0 ) 
        		{
            		oTable.fnDeleteRow( anSelected[0] );
        		}
        		
        		oTable = $("#<c:out value='${gridId}' />").dataTable();
    		});
    		

    		$.fn.dataTableExt.oApi.fnPagingInfo = function ( oSettings )
			{
				  return {
						    "iStart":         oSettings._iDisplayStart,
						    "iEnd":           oSettings.fnDisplayEnd(),
						    "iLength":        oSettings._iDisplayLength,
						    "iTotal":         oSettings.fnRecordsTotal(),
						    "iFilteredTotal": oSettings.fnRecordsDisplay(),
						    "iPage":          Math.ceil( oSettings._iDisplayStart / oSettings._iDisplayLength ),
						    "iTotalPages":    Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength )
						  };
			};

    		
			$.extend($.fn.dataTableExt.oPagination,
							{
								"bootstrap" : {
									"fnInit" : function(oSettings,nPaging, fnDraw) 
									{
													var oLang = oSettings.oLanguage.oPaginate;
													var fnClickHandler = function(e) {
														e.preventDefault();
														if (oSettings.oApi._fnPageChange(oSettings,e.data.action)) 
														{
															fnDraw(oSettings);
														}
													};

													$(nPaging).addClass('pagination').append(
																'<ul>'
																+ '<li class="prev disabled"><a href="#">&larr; '
																+ oLang.sPrevious
																+ '</a></li>'
																+ '<li class="next disabled"><a href="#">'
																+ oLang.sNext
																+ ' &rarr; </a></li>'
																+ '</ul>');
													var els = $('a', nPaging);
													$(els[0]).bind('click.DT',{action : "previous"},fnClickHandler);
													$(els[1]).bind('click.DT',{action : "next"}, fnClickHandler);
									},

									"fnUpdate" : function(oSettings, fnDraw) 
									{
										var iListLength = 5;
										var oPaging = oSettings.oInstance.fnPagingInfo();
										var an = oSettings.aanFeatures.p;
										var i, j, sClass, iStart, iEnd, iHalf = Math.floor(iListLength / 2);

										if (oPaging.iTotalPages < iListLength) 
										{
											iStart = 1;
											iEnd = oPaging.iTotalPages;
										} 
										else if (oPaging.iPage <= iHalf) 
										{
											iStart = 1;
											iEnd = iListLength;
										}
										else if (oPaging.iPage >= (oPaging.iTotalPages - iHalf)) 
										{
											iStart = oPaging.iTotalPages - iListLength + 1;
											iEnd = oPaging.iTotalPages;
										} 
										else 
										{
											iStart = oPaging.iPage - iHalf + 1;
											iEnd = iStart + iListLength - 1;
										}

										for (i = 0, iLen = an.length; i < iLen; i++) 
										{
											$('li:gt(0)', an[i]).filter(':not(:last)').remove();
											for (j = iStart; j <= iEnd; j++) 
											{
												sClass = (j == oPaging.iPage + 1) ? 'class="active"': '';
												$('<li '+sClass+'><a href="#">'+ j+ '</a></li>').insertBefore(
														$('li:last',an[i])[0]).bind(
																'click',
																function(e) {e.preventDefault();
																	oSettings._iDisplayStart = (parseInt(
																			$('a',this).text(),10) - 1)
																			* oPaging.iLength;
																	fnDraw(oSettings);
																});
											}

											// Add / remove disabled classes from the static elements
											if (oPaging.iPage === 0) {
												$('li:first', an[i])
														.addClass(
																'disabled');
											} else {
												$('li:first', an[i])
														.removeClass(
																'disabled');
											}

											if (oPaging.iPage === oPaging.iTotalPages - 1
													|| oPaging.iTotalPages === 0) {
												$('li:last', an[i])
														.addClass(
																'disabled');
											} else {
												$('li:last', an[i])
														.removeClass(
																'disabled');
											}
										}
									}
								}
							});
    		
    		
    			oTable = 	$("#<c:out value='${gridId}' />").dataTable( {
    			"aaData"	: <c:out value='${jsonString}' />.aaData,
    			"aoColumns": [
 				               { "mData": "id" },
 				               { "mData": "individualCustomer.personInfo.firstName" },
 				               { "mData": "individualCustomer.personInfo.middleName" },
 				               { "mData": "individualCustomer.personInfo.lastName" }
 				           ],
 				
				//"aaSorting"	: [[ ${sortOn}, "${sortOrder}" ]],
				//"sScrollX"	: "${sScrollX}",
			    //"bScrollCollapse": ${bScrollCollapse},
			    "oLanguage" : {	"sLengthMenu" :  "Show _MENU_"},
			    "iDisplayLength": <c:out value='${rowNum}' />,
    			"aLengthMenu": [<c:out value='${rowList}' />],
			    "bPaginate": true,
    			"oTableTools" : {"sRowSelect" : "multi","aButtons" : [ "select_all","select_none" ]},
				"sDom" : 'RlCri<"toolbar">t<"bottom" ip>',
				"oColVis" : {"aiExclude" : [ 4 ]},
				"sPaginationType" : "full_numbers",
				"sPaginationType" : "bootstrap",
				"aoColumnDefs" : [ {
				"bVisible" : true,
				"aTargets" : [ 0],
				"sWidth": "2%"
				},
				 {
					 
					"aTargets" : [ 1],
					"sWidth": "2%"
				}
				, {
					 
					"aTargets" : [ 2],
					"sWidth": "2%"
				}
				,
				 { 
					"aTargets" : [ 3],
					"sWidth": "2%"
				}]
			});
    	oTable.$('td').click( function () {
		        	    var sData = oTable.fnGetData( this );
		        	    neutrinoNavigateTo("<%=request.getContextPath()%>/app/Customer/edit/"  + sData);
		        	  } );
		$("div.toolbar")
    			
    		$("div.toolbar")
					.html(
							'<div class="btn-group" data-toggle="buttons-radio" style="float:right;position:relative;right:8%;"><button type="button" class="btn btn-xs" >Approved</button><button type="button" class="btn btn-xs">UnApproved</button><button type="button" class="btn btn-xs">Both</button> </div>');
			$("div.toolbar").css("color", "red");
		
			$('<div />').addClass('div_buttton_class').css({
				'position' : 'relative',
				'right' : '50%',
				'bottom' : '-18px',
				'float' : 'right'
			}).attr({
				'id' : 'div_buttton_id'
			}).prependTo($('#mac_filter'));
		
			//$('<div />').addClass('heading').css({
			//	'position' : 'relative',
			//	'left' : '+00%',
			//	'right' : '-60%',
			//	//'bottom' : '-3px',
			//	'float' : 'left'
			//}).attr({
			//	'id' : 'div_buttton_id1'
			//}).prependTo($('#mac_filter'));
			
			//$('<h4 />').attr({
			//	'id' : 'a'
			//}).html('Data Grid Tag ShowCase').appendTo($('#div_buttton_id1'));
			
			$('<a />').attr({
				'id' : 'approve'
			}).html('Approve').appendTo($('#div_buttton_id'));
		
			$('<a />').attr({
				'id' : 'reject'
			}).html('Reject').appendTo($('#div_buttton_id'));
		
			$('<a />').attr({
				'id' : 'send_back'
			}).html('Send Back').appendTo($('#div_buttton_id'));
		
			//$('#a').addClass('heading');
			$('#approve').addClass('btn btn-xs');
			$('#reject').addClass('btn btn-xs');
			$('#send_back').addClass('btn btn-xs');
		
			$('#approve').click(function() {
				alert('approve.');
			});
		
			$('#reject').click(function() {
				alert('reject.');
			});
			$('#send_back').click(function() {
				alert('send back.');
			});
    		
			
			generate_datatable_tool_tip();
			
			$(document).on('click',"#<c:out value='${gridId}' /> tbody td img", function () {
				
				var nTr = $(this).parents('tr')[0];
				if ( oTable.fnIsOpen(nTr) )
				{
					this.src = "/finnone-webapp/images/grid/details_open.png";
					oTable.fnClose( nTr );
				}
				else
				{
					this.src = "/finnone-webapp/images/grid/details_close.png";
					oTable.fnOpen( nTr, <c:out value='${onViewClick}' />(oTable, nTr), 'details' );
				}
				});
			
			$(document).ready(function()
			{
				$('#selectAll').change(function() {

					if ($('#selectAll').is(':checked')) {
						$("#<c:out value='${gridId}' /> .checkbox").prop("checked", true);

					} else {
						$("#<c:out value='${gridId}' /> .checkbox").prop("checked", false);
					}
				});
				
				$('#add_Record').click( function() {
				   $('#windowTitleDialog').modal('show');  
    			 } );	
				
				$("#model_submit").click(function() 
				{
					if ($("#model-form").valid()) 
					{
						fnSubmitAddRow();
						generate_datatable_tool_tip();
						$('#windowTitleDialog').modal('hide');
					} 
					else 
					{
						 $(function(){new PNotify({
		    					title: 'Record cannot been added',
		    					text: 'Please check Validation Errors',
		    					type: 'error',
		    					opacity: .8
										});
							});
					}

				});

				$("select[name=<c:out value='${gridId}' />_length]").css("width", "50");
				$("BUTTON.ColVis_Button.TableTools_Button.ColVis_MasterButton span").hide();
				$("BUTTON.ColVis_Button.TableTools_Button.ColVis_MasterButton").css(
				{
							'background-image' : 'url("/finnone-webapp/images/grid/gear_icon.png")',
							'font-weight' : 'bolder',
							'width' : '25',
							'height' : '25',
							'position':'relative',
						    'left':'280px',
						    'float':'right'
				});
				
			
			});
			}); // document ready finish

function generate_datatable_tool_tip() 
{
	$("#<c:out value='${gridId}' /> tbody tr").unbind('click').click(function() {
			if ($(this).hasClass('row_selected')) {
				$(this).removeClass('row_selected');
			} 
			else {
				oTable.$('tr.row_selected').removeClass('row_selected');
				$(this).addClass('row_selected');
			}
	})


	$("#<c:out value='${gridId}' /> tbody tr.odd, #<c:out value='${gridId}' /> tbody tr.even").unbind('mouseover').mouseover(function() {
							var t = document.getElementById("messageDiv");
							t.style.position = "absolute";
							var a = $(this).offset();
							t.style.left = a.left - 70 + "px";
							t.style.top = a.top + "px";
							t.innerHTML = "<img src='/finnone-webapp/images/grid/Request_Approval.png'><img src='/finnone-webapp/images/grid/Send_Back_For_Review.png'><img src='/finnone-webapp/images/grid/Edit.png' onclick=menuclicked()>";
		
							//a.style.backgroundColor = "blue";
		
	});

}

function menuclicked()
{
	
	
	  // var rowIndex = ${gridId}.fnGetPosition( $(this).closest('tr');
    //   alert(importTable.fnGetData(rowIndex));
 //alert (oTableLocal.$('tr.row_selected'));
	//alert("operational menu clicked");
	//var grid = $("#list");
	//var row_id = rowObject.id;
	//win
		//	+ cellvalue + "</a>";

}

function fnSubmitAddRow()
{
	var addJsFunc = '';
	addJsFunc += '$(\'#${gridId}\').dataTable().fnAddData( [';
	addJsFunc += '\'<input type="checkbox" class="checkbox"/>\',';
	addJsFunc += '\'<img src="/finnone-webapp/images/grid/details_open.png">\',';
	var columnNames = "<c:out value='${columnNames}' />";
	var str_array = columnNames.split(',');
	for(var i = 0; i < str_array.length; i++)
	{
		str_array[i] = str_array[i].replace(/^\s*/, "").replace(/\s*$/, "");
		addJsFunc += '$(\'#' + str_array[i].toLowerCase()+ '\').val(),';
	}
	addJsFunc += ' ] );';
	
	eval(addJsFunc);
	
	$(function(){new PNotify(
						{	title: 'Record have been added',
							text: 'View in the Table Below',
							type: 'success',
							opacity: .8
						});
	});
};

function closeDialog () {
$('#windowTitleDialog').modal('hide'); 
}; 

function okClicked () {
closeDialog ();
};

</script>

<style type="text/css">

/* CSS for repositioning button on datatables*/
.ColVis {
	position: relative;
	float: right;
	right: 30%;
	top: 15px;
}

.table-striped tbody tr.row_selected td {
	background-color: gray;
}
</style>

<script type="text/javascript">
  function undef()
  {
    document.getElementById("messageDiv").innerHTML="";
  }
  function don(a,b)
  {
    a.style.backgroundColor=b;
  }
</script>

<script>
				
</script>

<!--  Body Part Stats from here -->
<div id="dynamicGridDiv"></div>

<div class="divDemoBody">
	<div id="windowTitleDialog" class="modal  fade">
		<div class="modal-header">
			<A class="close" onClick="closeDialog ();" href="#">&times;</A>
			<a href="javascript:void(0);" onclick="minimizationModal(this);" class="minimizedLink"><i class="glyphicon glyphicon-resize-small"></i></a>
			<H3>Add New Record</H3>
		</div>
		<div class="modal-body">
			<form action id="model-form" class="form-horizontal">
				<fieldset id="address_field">
					<legend>
						Model Form <small>(will not submit any information)</small>
					</legend>
					<div class="row">
						<div id="dynamicModelDiv" class="col-sm-6"></div>
					</div>

					<div class="form-actions">
						<a id="model_submit" href="#" class="btn btn-primary btn-lg">
							Submit</a>
						<button type="reset" class="btn">Reset</button>
					</div>
				</fieldset>
			</form>
		</div>
		<div class="modal-footer">
			<A class="btn secondary" onClick="closeDialog ();" href="#">Close</A>
		</div>
	</div>
</div>

<div id="dynamicModelJsDiv">
	<script type="text/javascript"></script>
</div>



