<%@ include file="../taglibs.jsp"%>

<%@ page import="org.oscarehr.casemgmt.model.*"%>
<%@ page import="org.oscarehr.casemgmt.web.formbeans.*"%>
<%@ page import="org.springframework.web.context.WebApplicationContext"%>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ page import="org.caisi.service.Version"%>
<%@ page import="oscar.OscarProperties"%>

<%@ taglib uri="/WEB-INF/caisi-tag.tld" prefix="caisi"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<%
response.setHeader("Cache-Control", "no-cache");
%>
<c:set var="ctx" value="${pageContext.request.contextPath}"	scope="request" } />
<script type="text/javascript">

	function submitForm(methodValue)
	{
		document.forms[0].method.value=methodValue;
		document.forms[0].submit();
	}
	function resetClientFields() {
		var form = document.caseManagementViewForm;
		form.elements['searchProviderNo'].value='';
		form.elements['searchServiceComponent'].value='';
		form.elements['searchCaseStatus'].value='';
		form.elements['searchStartDate'].value='';
		form.elements['searchEndDate'].value='';
		form.elements['searchProviderNo'].selectedIndex = 0;
		form.elements['searchCaseStatus'].selectedIndex = 0;
		}
	function clickTab(name) {
		document.caseManagementViewForm.tab.value=name;
		document.caseManagementViewForm.submit();
	}
	function popupNotePage(varpage) {
        var page = "" + varpage;
        windowprops = "height=800,width=800,location=no,"
          + "scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=50,screenY=50,top=0,left=0";
       var popup = window.open(page, "editNote", windowprops);
       if (popup != null) {
    		if (popup.opener == null) {
      		popup.opener = self;
    		}
    		popup.focus();
  		}
    }
    
    function popupHistoryPage(varpage) {
        var page = "" + varpage;
        windowprops = "location=no,"
          + "scrollbars=yes,menubars=no,toolbars=no,resizable=yes,top=0,left=0";
        window.open(page, "", windowprops);
    }
    
	function popupUploadPage(varpage,dn) {
        var page = "" + varpage+"?demographicNo="+dn;
        windowprops = "height=500,width=500,location=no,"
          + "scrollbars=no,menubars=no,toolbars=no,resizable=yes,top=50,left=50";
         var popup=window.open(page, "", windowprops);
         popup.focus();
        
    }
        
	function delay(time){
		var str="document.getElementById('ci').src='<c:out value="${ctx}"/>/images/default_img.jpg'";
		setTimeout(str,time);
	}
    
</script>

<html:form action="/CaseManagementView2">
	<html:hidden property="demographicNo" />
	<html:hidden property="providerNo" />
	<!--  no need for tabs 
	 
	<html:hidden property="tab" />
	<html:hidden property="hideActiveIssue" />
	-->
	<input type="hidden" name="method" value="view" />	
	<div id="pageTitle">
	<table width="100%">
		<tr>
			<th class="pageTitle" width="100%">Case Management Encounter</th>
		</tr>
		<tr>
			<td align="left" class="buttonBar">
			<html:link	action="/CaseManagementView2.do?note_view=summary"	style="color:Navy;text-decoration:none;">
				<img border=0 src=<html:rewrite page="/images/Back16.png"/> />&nbsp;Summary&nbsp;&nbsp;|
			</html:link> 
			<html:link action="/CaseManagementView2.do?note_view=detailed"	style="color:Navy;text-decoration:none;">&nbsp;Detailed&nbsp;&nbsp;|
			</html:link> 
			<html:link	action="/CaseManagementEntry2.do?method=edit&note_edit=new&from=casemgmt"	style="color:Navy;text-decoration:none;">
				 New&nbsp;Note&nbsp;&nbsp;|
			</html:link> 
			
			<a	href="javascript:window.print();" style="color:Navy;text-decoration:none;">Print</a>
			
			<a href="javascript:submitForm('close')" style="color:Navy;text-decoration:none;">&nbsp;&nbsp;|&nbsp;Close&nbsp;&nbsp;|</a>
			</td>
		</tr>
	</table>
	</div>
	<div id="clientInfo" class="axial">
	<table width="100%">
		<tr>
			<td width="65%">
			<table with="100%" class="simple" cellspacing="2" cellpadding="3">
				<tr>
					<th>Client Name</th>
					<td><c:out value="${casemgmt_demoName}" /></td>
				</tr>
				<tr>
					<th>Age</th>
					<td><c:out value="${casemgmt_demoAge}" /></td>
				</tr>
				<tr>
					<th>DOB</th>
					<td><c:out value="${casemgmt_demoDOB}" /></td>
				</tr>

				<%
				if (!OscarProperties.getInstance().isTorontoRFQ()) {
				%>

				<tr>
					<th nowrap>Primary Health Care Provider</th>
					<td><c:out value="${cpp.primaryPhysician}" /></td>
				</tr>
				<%
				}
				%>
				<tr>
					<th>Primary Counsellor/Caseworker</th>
					<td><c:out value="${cpp.primaryCounsellor}" /></td>
				</tr>
			</table>
			</td>
			<td>
				<%
					String demo = request.getParameter("demographicNo");
				%>
				<c:choose>
					<c:when test="${not empty image_filename}">
						<img style="cursor: pointer;" id="ci"	src="<c:out value="${ctx}"/>/images/default_img.jpg"
							alt="id_photo" height="100" title="Click to upload new photo."
							OnMouseOver="document.getElementById('ci').src='<c:out value="${ctx}"/>/images/<c:out value="${image_filename}"/>'"
							OnMouseOut="delay(5000)" window.status='Click to upload new photo'; 
							return	true;" onClick="popupUploadPage('<html:rewrite page="/casemgmt/uploadimage.jsp"/>',<%=demo%>);return false;" />
					</c:when>
					<c:otherwise>
						<img style="cursor: pointer;"	src="<c:out value="${ctx}"/>/images/defaultR_img.jpg"
							alt="No_Id_Photo" height="100" title="Click to upload new photo."
							OnMouseOver="window.status='Click to upload new photo';return true"
							onClick="popupUploadPage('<c:out value="${ctx}"/>/casemgmt/uploadimage.jsp',<%=demo%>);return false;" />
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
	</table>
	</div>
	<br />
	<div class="axial">
	<table border="0" cellspacing="2" cellpadding="3" width="100%">
		<tr>
			<th width="30%"><bean-el:message key="CaseSearch.dateRangeFrom"	bundle="pmm" nowrap /></th>
			<td width="20%"><quatro:datePickerTag property="searchStartDate" openerForm="caseManagementViewForm" width="120px"></quatro:datePickerTag></td>
			<th style="width:20%;" align="right" ><bean-el:message key="CaseSearch.dateRangeTo" 	bundle="pmm" /></th>
			<td><quatro:datePickerTag property="searchEndDate" width="120px" openerForm="caseManagementViewForm"></quatro:datePickerTag></td>
		</tr>

		<tr>
			<th width="30%" align="right"><bean-el:message key="CaseSearch.provider" bundle="pmm"	nowrap /></th>
			<td width="20%">
				<html:select property="searchProviderNo">
					<html:option value="">		</html:option>
					<html:options collection="providers" property="providerNo"	labelProperty="fullName" nowrap />
				</html:select>
			</td>
			<th width="20%" align="right"><bean-el:message key="CaseSearch.caseStatus" bundle="pmm"		nowrap /></th>
			<td>
				<html:select property="searchCaseStatus">
					<html:option value="">	</html:option>
					<html:options collection="caseStatusList" property="code"	labelProperty="description" nowrap />
				</html:select>
			</td>
		</tr>
		<tr>
			<th width="30%"><bean-el:message key="CaseSearch.componentsOfService"	bundle="pmm" nowrap /></th>
			<td width="20%">
				<html:select property="searchServiceComponent">
					<html:option value="">	</html:option>
					<html:options collection="issues" property="id"	labelProperty="description" />
				</html:select>
			</td>
			<td width="20%">&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td align="left" colspan="4">
				<input type="button" name="search"	value="search" onclick="submitForm('search')" />&nbsp; 
				<input	type="button" name="reset" value="reset"	onclick="resetClientFields()" />
			</td>
		</tr>
	</table>
	</div>	
	<c:if test="${not empty Notes}">
	    <!-- for sort align right purpose -->
	    <c:if test="${note_view!='detailed'}">
	    <table width="100%">
				<tr align="right">
					<td style="width: 20%">&nbsp;
					</td>
					<td style="width: 20%">&nbsp;</td>	
					<td style="width: 20%">&nbsp;</td>
					<td style="width: 25%;">&nbsp;</td>									
					<td  align="right">
					Sort
						<html:select property="note_sort" onchange="document.caseManagementViewForm.method.value='view';document.caseManagementViewForm.submit()">
							<html:option value="update_date">Date</html:option>
							<html:option value="providerName">Provider</html:option>
							<html:option value="programName">Program</html:option>
							<html:option value="roleName">Role</html:option>
						</html:select>
					</td>
				</tr>
			</table>
			</c:if>
		<c:if test="${note_view!='detailed'}">
					
			<div	style="color: Black; background-color: White; border-width: 1px; border-style: Ridge;
                    height: 350px; width: 100%; overflow: auto;">
			<table id="test" width="100%" border="0" cellspacing="2"	cellpadding="3" class="simple">
				
				<tr>
					<td style="background-color:#EEEEFF"></td>
					<th style="background-color:#EEEEFF">Date</thd>
					<th style="background-color:#EEEEFF">Provider</th>
					<th style="background-color:#EEEEFF">Status</th>
					<th style="background-color:#EEEEFF">Program</th>
					<th style="background-color:#EEEEFF">Role</th>
				</tr>

				<%
					int index = 0;
					String bgcolor = "white";
				%>				
				<c:forEach var="note" items="${Notes}">
					<%
						if (index++ % 2 != 0) {
							bgcolor = "white";
						} else {
							bgcolor = "#EEEEFF";
						}
					%>

					<tr bgcolor="<%=bgcolor %>" align="center">
						<td>
							<c:choose>
								<c:when	test="${(!note.signed) and (sessionScope.readonly=='false')}">

									<a	href="<html:rewrite action="/CaseManagementEntry2.do?method=edit&from=casemgmt"/>
										&noteId=<c:out value="${note.id}"/>&demographicNo=<c:out value="${param.demographicNo}"/>
										&providerNo=<c:out value="${param.providerNo}" />&forceNote=true" style="color:Navy;text-decoration:none;">
									<img border="0" src="<c:out value="${ctx}"/>/images/edit_white.png" title="Edit/Sign Note" /> </a>
								</c:when>
								<c:when test="${note.signed and note.provider_no eq param.providerNo and (note.locked !=true)}">
									<a	href="<html:rewrite action="/CaseManagementEntry2.do?method=edit&from=casemgmt"/>
										&noteId=<c:out value="${note.id}"/>&demographicNo=<c:out value="${param.demographicNo}"/>
										&providerNo=<c:out value="${param.providerNo}" />&forceNote=true" style="color:Navy;text-decoration:none;">
									<img border="0" src="<c:out value="${ctx}"/>/images/edit_white.png"	title="Edit Note" /> 
									</a>
								</c:when>
								<c:otherwise>
									<img border="0" style="color:Navy;text-decoration:none;" src="<c:out value="${ctx}"/>/images/transparent_icon.gif" 	title="" />
								</c:otherwise>
							</c:choose> 
							<c:choose>
								<c:when test="${note.hasHistory == true and note.locked != true}">
								<a	href="<html:rewrite action="/CaseManagementEntry2.do?method=history&from=casemgmt"/>
										&noteId=<c:out value="${note.id}"/>&demographicNo=<c:out value="${param.demographicNo}"/>
										&providerNo=<c:out value="${param.providerNo}" />"  style="color:Navy;text-decoration:none;" >
									<img border="0" src="<c:out value="${ctx}"/>/images/history.gif"
									title="Note History" /> 
								</a>
								</c:when>
								<c:otherwise>
									<img border="0" style="color:Navy;text-decoration:none;" src="<c:out value="${ctx}"/>/images/transparent_icon.gif"	title="" />
								</c:otherwise>
							</c:choose> 
							<c:choose>
								<c:when test="${note.locked}">
									<a style="color:Navy;text-decoration:none;"	href="<html:rewrite action="/CaseManagementView2.do?method=unlock" />	 &noteId=<c:out value="${note.id}"/>
										<img border="0" src="<c:out value="${ctx}"/>/images/ulock.gif"		title="Unlock"  />
									</a>
								</c:when>
							<c:otherwise>
								<img border="0" style="color:Navy;text-decoration:none;" src="<c:out value="${ctx}"/>/images/transparent_icon.gif"	title="" />
							</c:otherwise>
							</c:choose>
						</td>
						<td><fmt:formatDate pattern="yyyy-MM-dd hh:mm a" value="${note.observation_date}" /></td>
						<td><c:out value="${note.providerName}" /></td>
						<td><c:out value="${note.status}" /></td>
						<td><c:out value="${note.programName}" /></td>
						<td><c:out value="${note.roleName}" /></td>
					</tr>
				</c:forEach>				
			</table>
			</div>
		</c:if>
		<c:if test="${note_view=='detailed'}">
			<div	style="color: Black; background-color: White; border-width: 1px; border-style: Ridge;   height: 400px; width: 100%; overflow: auto;">
			<table id="test" width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="#C0C0C0">
				<%
					int index1 = 0;
					String bgcolor1 = "white";
				%>
				
				<c:forEach var="note" items="${Notes}">
					<%
						if (index1++ % 2 != 0) {
							bgcolor1 = "white";
						} else {
							bgcolor1 = "#EEEEFF";
						}
						java.util.List noteList = (java.util.List) request.getAttribute("Notes");
						String noteId = ((CaseManagementNote) noteList.get(index1 - 1)).getId().toString();
						request.setAttribute("noteId", noteId);
					%>
					<tr>
						<td>
						<table width="100%" border="0">
							<tr bgcolor="<%=bgcolor1 %>">
								<td width="7%">Provider</td>
								<td width="93%">
									<c:out	value="${note.provider.formattedName }" />
								</td>
							</tr>
							<tr bgcolor="<%=bgcolor1 %>">
								<td width="7%">Date</td>
								<td width="93%"><fmt:formatDate	pattern="yyyy-MM-dd hh:mm a" value="${note.observation_date}" /></td>
							</tr>
							<tr bgcolor="<%=bgcolor1 %>">
								<td width="7%">Status</td>
								<td width="93%"><c:out value="${note.status}" /></td>
							</tr>
							<tr bgcolor="<%=bgcolor1 %>">
								<td width="7%">Action</td>
								<td width="93%">
								<c:if test="${(!note.signed) and (sessionScope.readonly=='false')}">									
									<a href="<html:rewrite action="/CaseManagementEntry2.do?method=edit&from=casemgmt"/>
										&noteId=<c:out value="${note.id}"/>&demographicNo=<c:out value="${param.demographicNo}"/>
										&providerNo=<c:out value="${param.providerNo}" />" >							
										Edit and Sign
									</a>
									
								</c:if> 
								<c:if	test="${note.signed and note.provider_no eq param.providerNo}">									
									
									<a href="<html:rewrite action="/CaseManagementEntry2.do?method=edit&from=casemgmt"/>
										&noteId=<c:out value="${noteId}"/>&demographicNo=<c:out value="${param.demographicNo}"/>
										&providerNo=<c:out value="${param.providerNo}" />" >							
										Edit This Note
									</a>									
								</c:if> 
								<c:if test="${note.hasHistory == true}">									
										<a href="<html:rewrite action="/CaseManagementEntry2.do?method=history&from=casemgmt"/>
											&noteId=<c:out value="${noteId}"/>&demographicNo=<c:out value="${param.demographicNo}"/>
											&providerNo=<c:out value="${param.providerNo}" />" >							
											Note History
										</a>									
								</c:if> 
								<c:if test="${note.locked}">
									<c:url	value="/CaseManagementView2.do?method=unlock&noteId=${noteId}"	var="lockedURL" />
									<a href="<html:rewrite action="/CaseManagementEntry2.do?method=unlock"/>
										&noteId=<c:out value="${noteId}"/> ">Unlock
									</a>
								</c:if>
							</td>
							</tr>
							<tr bgcolor="<%=bgcolor1 %>">
								<td width="7%">Note</td>
								<td width="93%"><c:choose>
									<c:when test="${note.locked}">
										<span style="color:red"><i>Contents Hidden</i></span>
									</c:when>
									<c:otherwise>
										<pre><c:out value="${note.note }" /></pre>
									</c:otherwise>
								</c:choose></td>
							</tr>
						</table>
						</td>
					</tr>
				</c:forEach>				
			</table>
			</div>
		</c:if>
		<br />
	</c:if>
</html:form>

