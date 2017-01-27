<%--

    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
    This software is published under the GPL GNU General Public License.
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

    This software was written for the
    Department of Family Medicine
    McMaster University
    Hamilton
    Ontario, Canada

--%>
<%@page import="java.io.StringWriter"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="oscar.OscarProperties,oscar.log.*"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%@ page import="oscar.oscarRx.data.*,java.util.*"%>
<%@ page import="org.oscarehr.common.model.PharmacyInfo" %>

<%
    String roleName$ = (String)session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed=true;
%>
<security:oscarSec roleName="<%=roleName$%>" objectName="_rx" rights="r" reverse="<%=true%>">
	<%authed=false; %>
	<%response.sendRedirect("../securityError.jsp?type=_rx");%>
</security:oscarSec>
<%
	if(!authed) {
		return;
	}
%>

<html:html locale="true">
<head>

<script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery-1.9.1.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery-ui-1.10.2.custom.min.js"></script>

<script type="text/javascript" src="<%= request.getContextPath() %>/share/javascript/Oscar.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/share/javascript/prototype.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/share/javascript/effects.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/share/lightwindow/javascript/lightwindow.js"></script>
<title><bean:message key="SelectPharmacy.title" /></title>
<html:base />

<logic:notPresent name="RxSessionBean" scope="session">
	<logic:redirect href="error.html" />
</logic:notPresent>
<logic:present name="RxSessionBean" scope="session">
	<bean:define id="bean" type="oscar.oscarRx.pageUtil.RxSessionBean"
		name="RxSessionBean" scope="session" />
	<logic:equal name="bean" property="valid" value="false">
		<logic:redirect href="error.html" />
	</logic:equal>
</logic:present>

<%
oscar.oscarRx.pageUtil.RxSessionBean bean = (oscar.oscarRx.pageUtil.RxSessionBean)pageContext.findAttribute("bean");
%>

<bean:define id="patient"
	type="oscar.oscarRx.data.RxPatientData.Patient" name="Patient" />

<link rel="stylesheet" type="text/css" href="styles.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/share/lightwindow/css/lightwindow.css" type="text/css" media="screen" />
        

<style type="text/css">

.ui-autocomplete {
	background-color: #CEF6CE;
	border: 3px outset #2EFE2E;
	width:300px;
}

.ui-menu-item:hover {
		background-color:#426FD9;
		color:#FFFFFF;
}

</style>
<script type="text/javascript">
( function($) {
	$(function() {
		var demo = $("#demographicNo").val();
		$.get("<%=request.getContextPath() + "/oscarRx/managePharmacy.do?method=getPharmacyFromDemographic&demographicNo="%>"+demo,
			function( data ) {
				if(data.length > 0){
					$("#preferredList").html("");
					var json;
					var preferredPharmacyInfo;
					for( var idx = 0; idx < data.length; ++idx  ) {
						preferredPharmacyInfo = data[idx];
						json = JSON.stringify(preferredPharmacyInfo);
						
						var pharm = "<div prefOrder='"+idx+"' pharmId='"+preferredPharmacyInfo.id+"'><table><tr><td class='prefAction prefUp'> Up </td>";
						pharm += "<td rowspan='3' style='padding-left: 5px'>" + preferredPharmacyInfo.name + "<br /> ";
						pharm += preferredPharmacyInfo.address + ", " + preferredPharmacyInfo.city + " " +preferredPharmacyInfo.province + "<br /> ";
						pharm += preferredPharmacyInfo.postalCode + "<br />";
						pharm += "Main Phone: " + preferredPharmacyInfo.phone1 + "<br />";
						pharm += "Fax: " + preferredPharmacyInfo.fax + "<br />";
                        pharm += "<a href='#'  onclick='viewPharmacy(" + preferredPharmacyInfo.id  + ");'>View More</a>" + "</td>";
						pharm += "</tr><tr><td class='prefAction prefUnlink'> Unlink </td></tr><tr><td class='prefAction prefDown'>Down</td></tr></table></div>";
						
						$("#preferredList").append(pharm);
					}
					
					$(".prefUnlink").click(function(){
						  var data = "pharmacyId=" + $(this).closest("div").attr("pharmId") + "&demographicNo=" + demo;
						  $.post("<%=request.getContextPath()%>/oscarRx/managePharmacy.do?method=unlink",
							  data, function( data ) {
								if( data.id ) {	  	
									window.location.reload(false);
								}
								else {
									alert("Unable to unlink pharmacy");
								}
							}, "json");
					  });
					  
					$(".prefUp").click(function(){
						if($(this).closest("div").prev() != null){
							var $curr = $(this).closest("div");
							var $prev = $(this).closest("div").prev();
							
							var data = "pharmId=" + $curr.attr("pharmId") + "&demographicNo=" + demo + "&preferredOrder=" + (parseInt($curr.attr("prefOrder")) - 1);
							$.post("<%=request.getContextPath()%>/oscarRx/managePharmacy.do?method=setPreferred",
							  data, function( data2 ) {
									if( data2.id ) {
										data = "pharmId=" + $prev.attr("pharmId") + "&demographicNo=" + demo + "&preferredOrder=" + (parseInt($prev.attr("prefOrder")) + 1);
										$.post("<%=request.getContextPath()%>/oscarRx/managePharmacy.do?method=setPreferred",
										  data, function( data3 ) {
												if( data3.id ) {
													window.location.reload(false);
												}
										}, "json");
									}
							}, "json");
						}
					  });
					  
					$(".prefDown").click(function(){
						if($(this).closest("div").next() != null){
							var $curr = $(this).closest("div");
							var $next = $(this).closest("div").next();
							
							var data = "pharmId=" + $curr.attr("pharmId") + "&demographicNo=" + demo + "&preferredOrder=" + (parseInt($curr.attr("prefOrder")) + 1);
							$.post("<%=request.getContextPath()%>/oscarRx/managePharmacy.do?method=setPreferred",
							  data, function( data2 ) {
									if( data2.id ) {
										data = "pharmId=" + $next.attr("pharmId") + "&demographicNo=" + demo + "&preferredOrder=" + (parseInt($next.attr("prefOrder")) - 1);
										$.post("<%=request.getContextPath()%>/oscarRx/managePharmacy.do?method=setPreferred",
										  data, function( data3 ) {
												if( data3.id ) {
													window.location.reload(false);
												}
										}, "json");
									}
							}, "json");
						}
					  });
				}
		}, "json");
	 
      $("#pharmacySearch").keyup(function(){
		  $(".pharmacyItem").hide();
		  $.each($(".pharmacyName"), function( key, value ) {
			if($(value).html().indexOf($("#pharmacySearch").val()) >= 0){
				if($(value).siblings(".city").html().indexOf($("#pharmacyCitySearch").val()) >= 0){
					if($(value).siblings(".fax").html().indexOf($("#pharmacyFaxSearch").val()) >= 0){
						$(value).parent().show();
					}
				}
			}
		  });
	  });
    
	  $("#pharmacyCitySearch").keyup(function(){
		  $(".pharmacyItem").hide();
		  $.each($(".city"), function( key, value ) {
			if($(value).html().indexOf($("#pharmacyCitySearch").val()) >= 0){
				if($(value).siblings(".pharmacyName").html().indexOf($("#pharmacySearch").val()) >= 0){
					if($(value).siblings(".fax").html().indexOf($("#pharmacyFaxSearch").val()) >= 0){
						$(value).parent().show();
					}
				}
			}
		  });
	  });
    
	  $("#pharmacyFaxSearch").keyup(function(){
		  $(".pharmacyItem").hide();
		  $.each($(".fax"), function( key, value ) {
			if($(value).html().indexOf($("#pharmacyFaxSearch").val()) >= 0){
				if($(value).siblings(".pharmacyName").html().indexOf($("#pharmacySearch").val()) >= 0){
					if($(value).siblings(".city").html().indexOf($("#pharmacyCitySearch").val()) >= 0){
						$(value).parent().show();
					}
				}
			}
		  });
	  });

        $("#pharmacyPhoneSearch").keyup(function(){
            $(".pharmacyItem").hide();
            $.each($(".phone"), function( key, value ) {
                if($(value).html().indexOf($("#pharmacyPhoneSearch").val()) >= 0 || $(value).html().split("-").join("").indexOf($("#pharmacyPhoneSearch").val()) >= 0){
                    if($(value).siblings(".pharmacyName").html().indexOf($("#pharmacySearch").val()) >= 0){
                        if($(value).siblings(".city").html().indexOf($("#pharmacyCitySearch").val()) >= 0){
                            $(value).parent().show();
                        }
                    }
                }
            });
        });

      $(".pharmacyItem").click(function(){
		  var pharmId = $(this).attr("pharmId");
		  
		  $("#preferredList div").each(function(){
			  if($(this).attr("pharmId") == pharmId){
				  alert("Selected pharamacy is already selected");
				  return false;
			  }
		  });
		  
		  var data = "pharmId=" + pharmId + "&demographicNo=" + demo + "&preferredOrder=" + $("#preferredList div").length;
		  
		  $.post("<%=request.getContextPath() + "/oscarRx/managePharmacy.do?method=setPreferred"%>", data, function( data ) {
			if( data.id ) {
				window.location.reload(false);
			}
			else {
				alert("There was an error setting your preferred Pharmacy");
			}
		  },"json");
      });
  
	$(".deletePharm").click(function(){
		if( confirm("You are about to remove this pharmacy for all users. Are you sure you want to continue?")) {
			var data = "pharmacyId=" + $(this).closest("tr").attr("pharmId");
			$.post("<%=request.getContextPath()%>/oscarRx/managePharmacy.do?method=delete",
					data, function( data ) {
				if( data.success ) {  	
					window.location.reload(false);
				}
				else {
					alert("There was an error deleting the Pharmacy");
				}
			},"json");
		}
	});
  
})}) ( jQuery );

function addPharmacy(){
	myLightWindow.activateWindow({
		href: "<%= request.getContextPath() %>/oscarRx/ManagePharmacy2.jsp?type=Add",
		width: 400,
		height: 500
	});
}

function editPharmacy(id){
	myLightWindow.activateWindow({
		href: "<%= request.getContextPath() %>/oscarRx/ManagePharmacy2.jsp?type=Edit&ID=" + id,
		width: 400,
		height: 500
	});
}

function viewPharmacy(id){
    myLightWindow.activateWindow({
        href: "<%= request.getContextPath() %>/oscarRx/ViewPharmacy.jsp?type=View&ID=" + id,
        width: 400,
        height: 500
    });
}


function returnToRx(){
	var rx_enhance = <%=OscarProperties.getInstance().getProperty("rx_enhance")%>;

	if(rx_enhance){
	    opener.window.refresh();
	    window.close();
	} else {
        window.location.href="SearchDrug3.jsp";
	}
}

</script>
</head>
<body topmargin="0" leftmargin="0" vlink="#0000FF">
<form id="pharmacyForm">
<input type="hidden" id="demographicNo" name="demographicNo" value="<%=bean.getDemographicNo()%>"/>
<table border="0" cellpadding="0" cellspacing="0"
	style="border-collapse: collapse" bordercolor="#111111" width="100%"
	id="AutoNumber1" height="100%">
	<%@ include file="TopLinks.jsp"%><!-- Row One included here-->
	<tr>
		<td width="100%" style="border-left: 2px solid #A9A9A9;" height="100%"
			valign="top" colspan="2">
		<table cellpadding="0" cellspacing="2"
			style="border-collapse: collapse" bordercolor="#111111" width="100%"
			height="100%">
			<tr>
				<td width="0%" valign="top" colspan="2">
				<div class="DivCCBreadCrumbs"><a href="SearchDrug3.jsp"> <bean:message
					key="SearchDrug.title" /></a> >  <bean:message key="SelectPharmacy.title" /></div>
				</td>
			</tr>
			<!----Start new rows here-->
			<tr>
				<td colspan="2">
				<div class="DivContentTitle"><b><bean:message
					key="SearchDrug.nameText" /></b> <jsp:getProperty name="patient"
					property="surname" />, <jsp:getProperty name="patient"
					property="firstName" />

					<input type=button class="ControlPushButton ReturnToRx" onclick="returnToRx();" value="Return to RX" />
				</div>
				<br />
				&nbsp; <bean:message key="SelectPharmacy.instructions" /></td>
			</tr>			
			<tr>
				<th width="33%" class="DivContentSectionHead">
					Preferred Pharmacies
				</th>
				<th class="DivContentSectionHead">
					Search Pharmacy&nbsp;&nbsp;<input type="text" id="pharmacySearch"/>&nbsp;&nbsp;
					City&nbsp;&nbsp;<input type="text" id="pharmacyCitySearch" style="width: 75px"/> &nbsp;&nbsp;
					Phone&nbsp;&nbsp;<input type="text" id="pharmacyPhoneSearch" style="width: 75px"/> &nbsp;&nbsp;
					Fax&nbsp;&nbsp;<input type="text" id="pharmacyFaxSearch" style="width: 75px"/> &nbsp;&nbsp;
					<a href="#" onclick="addPharmacy();"><bean:message key="SelectPharmacy.addLink" /></a>
				</th>
			</tr>			
			<tr>
				<td id="preferredList">
					<div>
							No pharmacies selected
					</div>
				</td>
				<td>
					<% RxPharmacyData pharmacy = new RxPharmacyData();
                         List< org.oscarehr.common.model.PharmacyInfo> pharList = pharmacy.getAllPharmacies();
                       %>
					<div style="width:100%; height:360px; overflow:auto;">
					<table id="pharmacyList" style="width:100%;">
						<tr>
							<th><bean:message key="SelectPharmacy.table.pharmacyName" /></th>
							<th><bean:message key="SelectPharmacy.table.address" /></th>
							<th><bean:message key="SelectPharmacy.table.city" /></th>
							<th><bean:message key="SelectPharmacy.table.phone" /></th>
							<th><bean:message key="SelectPharmacy.table.fax" /></th>
							<th>&nbsp;</th>
							<th>&nbsp;</th>
						</tr>
						<% for (int i = 0 ; i < pharList.size(); i++){
								   org.oscarehr.common.model.PharmacyInfo ph = pharList.get(i);
								%>
						<tr class="pharmacyItem" pharmId="<%=ph.getId()%>">
							<td class="pharmacyName" ><%=ph.getName()%></td>
							<td class="address" ><%=ph.getAddress()%></td>
							<td class="city" ><%=ph.getCity()%></td>
							<td class="phone" ><%=ph.getPhone1()%></td>
							<td class="fax" ><%=ph.getFax()%></td>
							<td onclick='event.stopPropagation();return false;'><a href="#"  onclick="editPharmacy(<%=ph.getId()%>);"><bean:message
								key="SelectPharmacy.editLink" /></a></td>
							<td onclick='event.stopPropagation();return false;'><a href="#" class="deletePharm"><bean:message
								key="SelectPharmacy.deleteLink" /></a></td>
						</tr>
						<% } %>
					</table>
					</div>
				</td>
			</tr>
			<!----End new rows here-->
			<tr height="100%">
				<td colspan="2"></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td height="0%"
			style="border-bottom: 2px solid #A9A9A9; border-top: 2px solid #A9A9A9;"></td>
		<td height="0%"
			style="border-bottom: 2px solid #A9A9A9; border-top: 2px solid #A9A9A9;"></td>
	</tr>
	<tr>
		<td width="100%" height="0%" colspan="2">&nbsp;</td>
	</tr>
	<tr>
		<td width="100%" height="0%" style="padding: 5" bgcolor="#DCDCDC"
			colspan="2"></td>
	</tr>
</table>
</form>
</body>

</html:html>
