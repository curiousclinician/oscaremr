<%@ include file="/taglibs.jsp"%>
<%@page import="com.quatro.common.KeyConstants" %>
<%
    String url ="/PMmodule/QuatroClientSummary.do";
%>
<c:set var="ctx" value="${pageContext.request.contextPath}"	scope="request" />
<script type="text/javascript"	src='<c:out value="${ctx}"/>/js/quatroLookup.js'></script>
<script>
	function resetClientFields() {
		var form = document.clientSearchForm2;
		if(form == null) alert ('form is null');
		if (form.elements == null) alert('elements is null');
		form.elements['criteria.demographicNo'].value='';
		form.elements['criteria.firstName'].value='';
		form.elements['criteria.lastName'].value='';
		form.elements['criteria.dob'].value='';
		// form.elements['criteria.healthCardNumber'].value='';
		// form.elements['criteria.healthCardVersion'].value='';
		// form.elements['criteria.searchOutsideDomain'].checked = true;
		// form.elements['criteria.searchUsingSoundex'].checked = true;
		// form.elements['criteria.dateFrom'].value=''; 
		// form.elements['criteria.dateTo'].value=''; 
		form.elements['criteria.bedProgramId'].selectedIndex = 0;
		form.elements['criteria.assignedToProviderNo'].selectedIndex = 0;
		form.elements['criteria.active'].selectedIndex = 0;
		form.elements['criteria.gender'].selectedIndex = 0;
	}
		function hasAnyFilter() {
		var form = document.clientSearchForm2;
		if(form == null) alert ('form is null');
		if (form.elements == null) alert('elements is null');
		if (form.elements['criteria.demographicNo'].value!='') return true;
		if (form.elements['criteria.firstName'].value !='') return true;
		if (form.elements['criteria.lastName'].value !='') return true;
		if (form.elements['criteria.dob'].value!='') return true;
		// form.elements['criteria.healthCardNumber'].value='';
		// form.elements['criteria.healthCardVersion'].value='';
		// form.elements['criteria.searchOutsideDomain'].checked = true;
		// form.elements['criteria.searchUsingSoundex'].checked = true;
		// form.elements['criteria.dateFrom'].value=''; 
		// form.elements['criteria.dateTo'].value=''; 
		if (form.elements['criteria.bedProgramId'].selectedIndex > 0) return true;
		if (form.elements['criteria.assignedToProviderNo'].selectedIndex > 0) return true;
//		if (form.elements['criteria.active'].selectedIndex > 0) return true;
//		if (form.elements['criteria.gender'].selectedIndex > 0) return true;
		alert('Please enter at least one of Client No, First Name, Last Name, DOB, Program or Assigned To to search');
		return false; 
	}	

	function submitForm(methodVal) {
		if (!hasAnyFilter()) return;
		document.forms[0].method.value = methodVal;
		document.forms[0].submit();
	}
</script>
<html:form	action="/PMmodule/ClientSearch2.do">
	<input type="hidden" name="method"  />
	<table width="100%" height="100%" cellpadding="1px" cellspacing="1px">
		<tr>
			<th class="pageTitle">Search <c:out value="${moduleName}"/></th>
		</tr>
		<tr>
		<td class="buttonBar2" align="left">
			<html:link action="/Home.do" style="color:Navy;text-decoration:none;">&nbsp;
				<img style="vertical-align: middle" border="0" src="<html:rewrite page="/images/close16.png"/>" />&nbsp;Close&nbsp;|
			</html:link>
			<security:oscarSec objectName="<%=KeyConstants.FUN_PMM_CLIENTSEARCH %>" rights="<%=KeyConstants.ACCESS_WRITE %>">
				<html:link	action="/PMmodule/QuatroIntakeEdit.do?method=create&intakeId=0&clientId=0"	style="color:Navy;text-decoration:none;">&nbsp;
					<img style="vertical-align: middle" border=0 src=<html:rewrite page="/images/New16.png"/> height="16px" width="16px"/>&nbsp;New Client&nbsp;|</html:link>
			</security:oscarSec>	
			<a href="javascript:submitForm('search')" style="color:Navy;text-decoration:none;">&nbsp;
			<img style="vertical-align: middle" border=0 src=<html:rewrite page="/images/search16.gif"/> height="16px" width="16px"/>&nbsp;Search&nbsp;|</a>
			<a style="color:Navy;text-decoration:none;" href="javascript:resetClientFields();">&nbsp;
			<img style="vertical-align: middle" border=0 src=<html:rewrite page="/images/searchreset.gif" /> height="16px" width="16px"/>&nbsp;Reset&nbsp;</a>		
		</td>
		</tr>
	<tr>
		 <td>	
	<div id="projecthome" class="app" style="background-image: /images/tablebg.gif">
		<div >
			<table cellspacing="1" cellpadding="4" width="100%" bgcolor="#E8E8E8">
				<tr>
					<th width="20%" align="right"><bean-el:message key="ClientSearch.clientNo" bundle="pmm"/></th>
					<th align="left" width="80%"><html:text property="criteria.demographicNo" size="15" /></th>
				</tr>
				<tr>
					<th width="20%" align="right"><bean-el:message key="ClientSearch.firstName"  bundle="pmm"/></th>
					<th align="left" width="80%"><html:text property="criteria.firstName" size="15" /></th>
				</tr>
		
				<tr>
					<th width="20%" align="right"><bean-el:message key="ClientSearch.lastName"  bundle="pmm"/>
					</th>
					<th align="left" width="80%"><html:text property="criteria.lastName" size="15" /></th>
				</tr>
				
				<tr>
					<th width="20%" align="right"><bean-el:message key="ClientSearch.dateOfBirth"  bundle="pmm"/> <br>
					(yyyy/mm/dd)</th>
					<th align="left" width="80%">
					<quatro:datePickerTag property="criteria.dob" openerForm="clientSearchForm2" width="180px"></quatro:datePickerTag>
					</th>
				</tr>
				<tr>
					<th width="20%" align="right"> <bean-el:message key="ClientSearch.active" bundle="pmm"/></th>
					<th align="left"> <html:select property="criteria.active">
							<html:option value="">Any</html:option>
							<html:option value="1">Yes</html:option>
							<html:option value="0">No</html:option>
						</html:select>
					</th>
				</tr>
				<tr>
					<th width="20%" align="right"><bean-el:message key="ClientSearch.gender" bundle="pmm"/></th>
					<th align="left" >
						<html-el:select property="criteria.gender">
							<html-el:option value="">Any</html-el:option>
							<c:forEach var="gen" items="${genders}">
								<html-el:option value="${gen.code}"><c:out value="${gen.description}"/></html-el:option>
							</c:forEach>
						</html-el:select>
					</th>
				</tr>

				<tr>
					<th width="20%" align="right"><bean-el:message key="ClientSearch.assignedTo"  bundle="pmm"/> </th>
			          <th align="left" >
			            <html:select property="criteria.assignedToProviderNo">
			                <html:option value="">
			                </html:option>
			              	<html:options collection="allProviders" property="providerNo" labelProperty="fullName" />
			            </html:select>
			          </th>
				</tr>
				<tr>
					<th width="20%" align="right"><bean-el:message key="ClientSearch.program"  bundle="pmm"/> </th>
			          <th align="left" >
			            <html:select property="criteria.bedProgramId">
			                <html:option value="">
			                </html:option>
			                <html:option value="MyP">My Programs
			                </html:option>
			              	<html:options collection="allBedPrograms" property="id" labelProperty="name" />
			            </html:select>
			          </th>
				</tr>
 				
			</table>
		</div>
	</div>  
	</td>
	</tr>
	<c:if test="${requestScope.clients != null}">
            <tr height="100%">
                <td>
                    <div style="color: Black; background-color: White; border-style: ridge; border-width: 1px;
                        width: 100%; height: 100%; overflow: auto">

            <display:table class="simple" cellspacing="2" cellpadding="3" id="client" name="clients" export="false" pagesize="100"  
            requestURI="/PMmodule/ClientSearch2.do" defaultsort="2">
			<display:setProperty name="paging.banner.placement" value="bottom" />
			<display:setProperty name="basic.msg.empty_list" value="No clients found." />
			<display:column sortable="true" title=" First Name" sortProperty="firstName" sortName="client">
                 <a href="<html:rewrite action="<%=url%>"/>?clientId=<c:out value="${client.currentRecord}"/>"><c:out value="${client.firstName}" /></a>
			</display:column>
			<display:column sortable="true" title=" Last Name" sortProperty="lastName" sortName="client">
                 <a href="<html:rewrite action="<%=url%>"/>?clientId=<c:out value="${client.currentRecord}"/>"><c:out value="${client.lastName}" /></a>
			</display:column>
			<display:column sortable="true" title="Gender" sortName="client" sortProperty="sexDesc" >
				<c:out value="${client.sexDesc}" />
			</display:column>                        

			<display:column sortable="true" title="Date of Birth">
				<c:out value="${client.dob}" />
			</display:column>

			<display:column sortable="true" title="Active">
				<logic:equal value="0" property="activeCount" name="client">No</logic:equal>
				<logic:notEqual value="0" property="activeCount" name="client">Yes</logic:notEqual>
			</display:column>                        
			<display:column sortable="true" title="H&S Alert">
				<logic:equal value="0" property="hsAlertCount" name="client">No</logic:equal>
				<logic:notEqual value="0" property="hsAlertCount" name="client">Yes</logic:notEqual>
			</display:column>                         
			<display:column sortable="true" title="Client No" sortProperty="demographicNo" sortName="client">
                 <a href="<html:rewrite action="<%=url%>"/>?clientId=<c:out value="${client.currentRecord}"/>"><c:out value="${client.demographicNo}" /></a>
            </display:column>
		</display:table>

		</div>
		</td>
		</tr>
		</table>       
       </c:if>
</html:form>
        
           
        

   
            