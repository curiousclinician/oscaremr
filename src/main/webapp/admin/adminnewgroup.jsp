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

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<%@ page import="java.util.*,java.sql.*,java.util.ResourceBundle"%>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="org.oscarehr.util.SpringUtils" %>
<%@ page import="org.oscarehr.common.model.MyGroup" %>
<%@ page import="org.oscarehr.common.model.MyGroupPrimaryKey" %>
<%@ page import="org.oscarehr.common.dao.MyGroupDao" %>
<%@ page import="org.oscarehr.common.model.ProviderData"%>
<%@ page import="org.oscarehr.common.dao.ProviderDataDao"%>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%
	MyGroupDao myGroupDao = SpringUtils.getBean(MyGroupDao.class);
	ProviderDataDao providerDao = SpringUtils.getBean(ProviderDataDao.class);

    String curProvider_no = (String) session.getAttribute("user");
    String roleName$ = (String)session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
	String groupNo = request.getParameter("groupNo") != null ? URLDecoder.decode((String) request.getParameter("groupNo")) : "";

    boolean isSiteAccessPrivacy=false;
%>

<security:oscarSec objectName="_site_access_privacy" roleName="<%=roleName$%>" rights="r" reverse="false">
	<%isSiteAccessPrivacy=true; %>
</security:oscarSec>

<!DOCTYPE html>
<html:html locale="true">
<head>

<title><bean:message key="admin.adminnewgroup.title" /></title>
<script src="<%=request.getContextPath()%>/JavaScriptServlet" type="text/javascript"></script>

<script>

function setfocus() {
  this.focus();
  document.UPDATEPRE.mygroup_no.focus();
  document.UPDATEPRE.mygroup_no.select();
}

function validate() {
  group = document.UPDATEPRE.mygroup_no.value;

  if (group.length <=0 || group <= " ") {
     alert("<bean:message key="admin.adminNewGroup.msgGroupIsRequired"/>");

     return false;
  }
  else {
  	var checked=false;
  	var checkboxes = document.getElementsByName("data");
	var x=0;
		for(x=0;x<checkboxes.length;x++) {
			if(checkboxes[x].checked==true) {
				checked=true;
			}
		}
		if(checked==false) {
			alert('You must choose a provider');
			return false;
		}
		return true;
  	}
}
</script>

<link href="<%=request.getContextPath() %>/css/bootstrap.min.css" rel="stylesheet">
</head>



<body onload="setfocus();">
<%
  ResourceBundle properties = ResourceBundle.getBundle("oscarResources", request.getLocale());

  if(request.getParameter("submit")!=null && request.getParameter("submit").equals(properties.getString("admin.admindisplaymygroup.btnSubmit1")) ) { //delete the group member
    String[] param =new String[2];
    StringBuffer strbuf=new StringBuffer();
	
	String[] remProvider = request.getParameterValues("removeBtn");
	String[] groProviders= request.getParameterValues("providerNo");
	
	//Incase of html injection, we need to have backend validation to prevent unexpected data from
    //being saved into the database.
	boolean hasErrors = false;
	
	//If either list is null, or they aren't the same length, that means something went wrong
	  //and it will cause a null pointer exception further down if we don't catch it.
	if (remProvider == null || groProviders == null || remProvider.length != groProviders.length) {
		hasErrors = true;
	}
	
	//Next we need to make sure the providers that we got are actually in the groups we're editing
	//in case a false provider was snuck in. We also want to ensure that the provider has a
	//legal number
	if (!hasErrors) {
		for (String provider : groProviders) {
			MyGroup findGroup = myGroupDao.getGroup(groupNo, provider);
			if (findGroup == null ||
				StringUtils.isEmpty(provider) ||
				provider.length() > 6 ||
				!StringUtils.isNumeric(provider)) {
			
				hasErrors = true;
			}
		}
	}

	if (!hasErrors) {
		for (int i = 0; i < groProviders.length; i++) {
			if (remProvider[i].equals("true")) {
				myGroupDao.deleteGroupMember(groupNo, groProviders[i]);
			} else {
				MyGroup save = myGroupDao.getGroup(groupNo, groProviders[i]);
				save.setViewOrder(i);
				myGroupDao.merge(save);
			}
		}
	}
  }
%>

<FORM NAME="UPDATEPRE" METHOD="post" ACTION="adminsavemygroup.jsp" onsubmit="return validate();">

<%if(request.getParameter("submit")!=null && request.getParameter("submit").equals(properties.getString("admin.admindisplaymygroup.btnSubmit1")) ) { %>
<br>
    <div class="alert alert-success">
 		<strong>Success!</strong> Group has been updated!
    </div>
    
    <a href="admindisplaymygroup.jsp" class="btn btn-primary">View Group List</a>

	<a href="adminnewgroup.jsp" class="btn"><bean:message key="admin.admindisplaymygroup.btnSubmit2"/></a>
<%}else{%>    
    
<h3><bean:message key="admin.adminnewgroup.description" /></h3>


					
				 
					<input type="text" name="mygroup_no" size="10" maxlength="10" placeholder="<bean:message key="admin.adminmygroup.formGroupNo" />" title="Enter an existing or new group name." value="<%= groupNo %>">
					<small>(Max. 10 chars.)</small>
					
		<table class="table table-condensed table-hover">	
		<thead>
			<tr class="btn-inverse">
				<th></th>
				<th>
					<bean:message key="admin.admindisplaymygroup.formProviderName" />
				</th>
			</tr>
		</thead>
			
		<tbody>
<%
	// find all active providers
	int i=0;
	List<ProviderData> providerList = providerDao.findAllOrderByLastName();
	List<String> groupDocs = new ArrayList();
	
	if(!groupNo.equals("")){
		groupDocs = myGroupDao.getGroupDoctors(groupNo);
	}
   
   for(ProviderData provider : providerList) {
		if(!groupDocs.contains(provider.getId())){
		i++;
%>
			<tr class="<%=i%2==0?"":"info"%>">
				<td width="20px" ALIGN="center">
				<input type="checkbox" name="data" value="<%=i%>"> 
				<input type="hidden" name="provider_no<%=i%>" value="<%= provider.getId() %>"> 
				<input type="hidden" name="last_name<%=i%>" value='<%= provider.getLastName() %>'> 
				<input type="hidden" name="first_name<%=i%>" value='<%= provider.getFirstName() %>'>
				</td>
				
				<td><%= provider.getLastName() %>, <%= provider.getFirstName() %></td>

			</tr>
<%
		}
   }
%>
		</tbody>
		</table>


<input type="submit" name="Submit"	class="btn btn-primary" value="<bean:message key="admin.adminnewgroup.btnSubmit"/>">

<a href="admindisplaymygroup.jsp" class="btn btn-default">Cancel</a>

</FORM>

<%} %>


<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery-1.9.1.min.js"></script>

<script>
$( document ).ready(function() {
parent.parent.resizeIframe($('html').height());      

});
</script>
</body>
</html:html>
