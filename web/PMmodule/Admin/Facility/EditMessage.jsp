
<%@ include file="/taglibs.jsp"%>
<%
String s = "debug";
%>
<%@ page import="org.oscarehr.PMmodule.model.Facility"%>
<bean:define id="facility" name="facilityManagerForm"
	property="facility" />

<html:form action="/FacilityMessage.do">
	<input type="hidden" name="method" value="save" />
	<html:hidden property="facility_message.id" />
	<input type="hidden" name="facility_message.facilityId" value='<c:out value="${facility.id}" />' />

	<table cellpadding="0" cellspacing="0" border="0" width="100%"
		height="100%">

		<!-- Title -->
		<tr>
			<th class="pageTitle" align="center">Facility Management -
			Messages</th>
		</tr>

		<!-- body start -->
		<tr>
			<td height="100%">

			<table width="100%" cellpadding="0px" cellspacing="0px" height="100%"
				border="0">
				<!-- submenu -->
				<tr>
					<td align="left" class="buttonBar2">
					<html:link action="/Home.do"
					style="color:Navy;text-decoration:none">&nbsp;
					<img style="vertical-align: middle" border=0 src=<html:rewrite page="/images/close16.png"/> />&nbsp;Close&nbsp;&nbsp;|</html:link>
					<html:link
						action="/FacilityMessage.do?method=list" name="actionParam"
						style="color:Navy;text-decoration:none;">
						<img style="vertical-align: middle" border="0" src="<html:rewrite page="/images/Back16.png"/>" />&nbsp;Back to Messages&nbsp;&nbsp;!</html:link>
					<c:if test="${!isReadOnly }">
						<html:link href="javascript:submitForm();" 	style="color:Navy;text-decoration:none;" onclick="javascript: setNoConfirm();">
						<img style="vertical-align: middle" border="0" src="<html:rewrite page="/images/Save16.png"/>" />&nbsp;Save&nbsp;&nbsp;</html:link>
					</c:if>	
					</td>
				</tr>

				<!-- messages -->
				<tr>
					<td align="left" class="message"><logic:messagesPresent
						message="true">
						<br />
						<html:messages id="message" message="true" bundle="pmm">
							<c:out escapeXml="false" value="${message}" />
						</html:messages>
						<br />
					</logic:messagesPresent></td>
				</tr>

				<tr>
					<td height="100%">
					<div
						style="color: Black; background-color: White; border-width: 1px; border-style: Ridge;
				                    height: 100%; width: 100%; overflow: auto;" id="scrollBar">





					<br />
					<div class="tabs" id="tabs">
					<table cellpadding="3" cellspacing="0" border="0">
						<tr>
							<th title="Facility Messages">Messages</th>
						</tr>
					</table>
					</div>

					<br />

					<table width="100%" border="1" cellspacing="2" cellpadding="3" >
						<tr class="b">
							<td width="150px">Expiry Day:&nbsp;</td>
							<td><quatro:datePickerTag property="facility_message.expiry_day" 
									width="150px" openerForm="facilityMessageForm" />
							</td>
							
						</tr>
						<tr class="b">
							<td>Expiry Time:&nbsp;</td>
							<td>Hour: <html:select
								property="facility_message.expiry_hour">
								<%
								for (int x = 0; x < 24; x++) {
								%>
								<html:option value="<%=String.valueOf(x) %>">
									<%=x%>
								</html:option>
								<%
								}
								%>
							</html:select> &nbsp;&nbsp; Minute: <html:select
								property="facility_message.expiry_minute">
								<%
								for (int x = 0; x < 60; x++) {
								%>
								<html:option value="<%=String.valueOf(x) %>">
									<%=x%>
								</html:option>
								<%
								}
								%>
							</html:select></td>
							
						</tr>
						<tr class="b">
							<td>Message Type:</td>
							<td>
								<html-el:select property="facility_message.type" tabindex="8">
									<c:forEach var="type" items="${msgTypepList}">
										<html-el:option value="${type.code}">
											<c:out value="${type.description}" />
										</html-el:option>
									</c:forEach>
								</html-el:select>
							</td>
						</tr>
					
						<tr class="b">
							<td>Message:&nbsp;</td>
							<td colspan="2">
								<html-el:textarea property="facility_message.message" rows="10" cols="50" 
									onkeyup="javascript:txtAreaLenChecker(this, 4000);"/>
							</td>
						</tr>

						

						
					</table>






					</div>
					</td>
				</tr>
			</table>



			</td>
		</tr>
		<!-- body end -->
	</table>
	<%@ include file="/common/readonly.jsp" %>
</html:form>
<script type="text/javascript">
<!--
	function submitForm(){
		document.forms[0].submit();
	}
	
	function txtAreaLenChecker(obj, maxLen) {
	   //counting each end of line as two characters
	   
		var v = obj.value;
		var len = v.length;
		
		if(len > maxLen){
			alert("Length of this field can not exceed " + maxLen + " characters.");
			obj.value = v.substr(0, maxLen);
	   }
	
	}
	
//-->
</script>
