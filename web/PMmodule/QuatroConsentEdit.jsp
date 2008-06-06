<%@ include file="/taglibs.jsp"%>
<%@page import="java.util.Date"%>

<c:set var="ctx" value="${pageContext.request.contextPath}"
	scope="request" />
<script type="text/javascript"
	src='<c:out value="${ctx}"/>/js/quatroLookup.js'></script>

<html-el:form action="/PMmodule/QuatroConsent.do">
	<input type="hidden" name="method" />
	<html-el:hidden property="consentValue.demographicNo"  />	
	<html-el:hidden property="consentValue.id" />
	<input type="hidden" name="clientId"/>	
<script lang="javascript">
	function printForm(recordId){
    	//To PDF 
    	// var url = appPath + "/Print/PrintView.aspx?id=" + reportId + ":" + recordId + ":p";

    	var url='<c:out value="${ctx}" />/PMmodule/PrintView.do?rId=' +recordId+'&moduleName=consent';
		top.childWin = window.open(url,"_blank","resizable=yes,scrollbars=yes,status=yes,width=650,height=400,top=50, left=50");
		top.childWin.focus();
	}
	function submitForm(methodVal) {
		document.forms[0].method.value = methodVal;
		document.forms[0].submit();
	}
	
	function validateStateLength(){
	    var fieldValue = document.consentDetailForm.elements["consentValue.statePurpose"].value;
		if (fieldValue.length>512)
		{
			alert("State Purpose input length is over 512!!!");
			return false;
		}else{
		 	return true;
		}	
	}
	function validateNotesLength(){
	    var fieldValue = document.consentDetailForm.elements["consentValue.notes"].value;
		if (fieldValue.length>512)
		{
			return false;
		}else{
		 	return true;
		}	
	}
	function validateSave(){
	
		var str1="State Purpose input length is over 512!!!" ;
		var str2="Notes input length is over 512!!!" ;
		
		if (!validateStateLength()){
			alert(str1); return false;
		}
		if (!validateNotesLength()){
			alert(str2); return false;
		}
			return true;
	}
	
	function signSignature(){
	   var rId=document.consentDetailForm.elements['consentValue.id'].value;
	   var url='<c:out value="${ctx}" />/PMmodule/ClientManager/signature.jsp?' +
	     "rid=" +rId +"&moduleName=consent";
	
	   win = window.open(url,"_blank","toolbar=yes,menubar= yes,resizable=yes,scrollbars=yes,status=yes,width=600,height=400");
	   win.focus();
	}
	function viewSignature(){
	  var rId=document.consentDetailForm.elements['consentValue.id'].value;
	   var url='<c:out value="${ctx}" />/topazGetImage.do?' +
	     "rid=" +rId +"&moduleName=consent";
	
	   win = window.open(url,"_blank","toolbar=yes,menubar= yes,resizable=yes,scrollbars=yes,status=yes,width=400,height=200");
	   win.focus();
	}
</script>


	<table width="100%" height="100%" cellpadding="0px" cellspacing="0px">
		<tr>
			<th class="pageTitle" align="center">Client Management - Consent</th>
			
		</tr>
		<tr>
			<td class="simple" style="background: lavender"><%@ include file="ClientInfo.jsp" %></td>
		</tr>
		<tr>
			<td align="left" class="buttonBar2">			
				<a href='javaScript:submitForm("update");' onclick="return validateSave();"
				style="color:Navy;text-decoration:none;">
				<img border=0 src=<html:rewrite page="/images/Save16.png"/> />&nbsp;Save&nbsp;&nbsp;</a>
				<logic:greaterThan name="consentDetailForm" property="consentValue.id" value="0">
		  			<a href="javascript:signSignature();" style="color:Navy;text-decoration:none;">
		 		 		<img border=0 src=<html:rewrite page="/images/notepad.gif"/> />&nbsp;Sign&nbsp;&nbsp;</a>|
		  			<a href="javascript:viewSignature();" style="color:Navy;text-decoration:none;">
		  				<img border=0 src=<html:rewrite page="/images/search16.gif"/> />&nbsp;Preview Signature&nbsp;&nbsp;</a>|
		  			<a href="javaScript:printForm('<bean:write name="consentDetailForm" property="consentValue.id" />');"        
         				style="color:Navy;text-decoration:none;">
						<img border=0 src=<html:rewrite page="/images/Print16x16.gif"/> />&nbsp;Print&nbsp;&nbsp;</a>	
		        </logic:greaterThan>
		        
		        <html:link
						action="/PMmodule/QuatroConsent.do?method=list" name="actionParam"
						style="color:Navy;text-decoration:none;">
						<img border=0 src=<html:rewrite page="/images/Back16.png"/> />&nbsp;Close&nbsp;&nbsp;</html:link>
				
			</td>
		</tr>
		<tr height="18px">
			<td align="left" class="message">		
				<logic:messagesPresent	message="true">
					<html:messages id="message" message="true" bundle="pmm">
						<c:out escapeXml="false" value="${message}" />
					</html:messages>
				</logic:messagesPresent>
				<br />
			</td>
		</tr>
		<tr>
			<td height="100%">
			<div style="color: Black; background-color: White; border-width: 1px; border-style: Ridge;
                    height: 100%; width: 95%; overflow: auto;">
<!--  start of page content -->

				<table width="100%" class="edit">
					<tr>
						<td>						
							<table cellpadding="3" cellspacing="0" border="0">
								<tr>
									<th>Consent</th>
								</tr>
							</table>						
						</td>
					</tr>
					<tr><td><hr></td></tr>
					<tr><td>
						<table cellspacing="2" cellpadding="3">
							<tr>
								<td rowspan="2" style="width: 50%">
									<font size="4"><b>Client Consent to the Collection of<br />
										Information from External Agencies
									</b></font>
								</td>
								<td style="width: 30%">Shelter Name<br />Shelter Mailing Address</td>
								<td style="width: 20%">Tel:<br /> Fax:</td>
							</tr>
						</table>
					</td></tr>
					<tr><td><br></td></tr>					
					<tr>
						<td>
							<table class="simple" cellspacing="2" cellpadding="3">							
								<tr>
									<td colspan="2">I	
									<c:out value="${client.firstName}" /> &nbsp;<c:out value="${client.lastName}" />&nbsp;
									  consent to the release and exchange of the following information:
									</td>
								</tr>
								<tr>
									<td colspan="2" width="90%" ><html:textarea  rows="3" property="consentValue.notes" cols="105"  /> </td>
								</tr>
							</table>
							<table class="simple" cellspacing="2" cellpadding="3">
								<tr><td colspan="2">Between an authorized representative of &nbsp;<c:out value="${facilityDesc}"></c:out>&nbsp; and the agency/organization named below:</td></tr>
								<tr>
									<td style="width:20%">Program:</td>
									<td>
										<html:select property="consentValue.programId">			                
				              			<html:options collection="programs" property="code" labelProperty="description" />
				              			</html:select>
									</td>
								</tr>
								<tr><td style="width:20%">Agency Name:</td><td ><html-el:text style="width: 60%" property="consentValue.agencyName" maxlength="60" /></td></tr>
								<tr><td style="width:20%">Contact Person Name:</td><td><html-el:text style="width: 60%" property="consentValue.contactName" maxlength="60" /></td></tr>
								<tr><td style="width:20%">Contact Person Title:</td><td><html-el:text style="width: 60%" property="consentValue.contactTitle" maxlength="30" /></td></tr>
								<tr><td style="width:20%">Contact Person Phone:</td><td><html-el:text style="width: 60%" property="consentValue.contactPhone" maxlength="25" /></td></tr>							
								<tr><td colspan="2">For the following Stated purpose(s):</td></tr>
								<tr><td colspan="2"><html-el:textarea property="consentValue.statePurpose" cols="105"  rows="3" /></td></tr>
						  </table>
						 <table  class="simple" cellspacing="2" cellpadding="3">
								<tr>
									<td colspan="2">
										I fully understand the purpose of this consent and give it voluntarily.  The consent is valid for the period from 
									</td>
								</tr>
								<tr>
									<td colspan="2">
										<table>
											<tr>
												<td style="width:  50%">
													 Today's Date <jsp:useBean id="now" class="java.util.Date" /><fmt:formatDate pattern="yyyy/MM/dd hh:mm a" value="${now}" />
												</td>
												<td style="width: 15%"> To</td>
												<td style="width:35%"> <quatro:datePickerTag property="consentValue.endDateStr" openerForm="consentDetailForm" width="150px"></quatro:datePickerTag></td>
											</tr>
										</table>
									</td>
								</tr>
								<tr><td style="width:40%" >Dated this Today's Date</td><td>							
								<fmt:formatDate pattern="yyyy/MM/dd" value="${now}" /></td></tr>
						</table>								
						<table width="100%" align="right" class="simple">
									<tr><td width="75%"></td><td align="right" width="25%">
										<u><bean:write name="consentDetailForm" property="consentValue.providerFormattedName" /></u>
									</td></tr>
									<tr><td width="75%"></td><td align="right" width="25%" >	Witness Name</td></tr>
						
														
							<tr><td colspan="2">
								<b>Notice with Regard to the Collection of Personal Information:</b><br />
								Personal information is collected under the legal authority of the City of Toronto Act, 1997, 
								Municipal Act, 2001, Chapter 169, Article VII, By-law 112-2005 and Ontario Works Act, 1997, 
								for the purposes of  administering Government of Ontario social assistance programs, 
								providing shelter services and sharing information between shelter providers when 
								specific consent is obtained.  Questions about this collection can be directed to the 
								Administrative Supervisor, Shelter, Support and Housing Administration Division, 
								Telephone no. 416-392-8741, Metro Hall, 55 John St. 6th Floor, Toronto, Ontario M5V 3C6.
								</td></tr>
						</table>					
					</td>
					</tr>					
				</table>

<!--  end of page content -->
			
			</div>
			</td>
		</tr>
	</table>
</html-el:form>
