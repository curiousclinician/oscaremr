<%@ include file="/taglibs.jsp"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="org.oscarehr.util.SpringUtils"%>
<html:html locale="true">
	<head>
		<title>Program Search</title>
		<link href="<html:rewrite page='/css/tigris.css'/>" rel="stylesheet" type="text/css" />
		<link href="<html:rewrite page='/css/displaytag.css'/>" rel="stylesheet" type="text/css" />
	</head>
	
	<script type="text/javascript">
		var gender='<%=request.getSession().getAttribute("clientGender")%>';
		var age=<%=request.getSession().getAttribute("clientAge")%>;
		
		var programMaleOnly=<%=session.getAttribute("programMaleOnly")%>;
        var programFemaleOnly=<%=session.getAttribute("programFemaleOnly")%>;
        var programTransgenderOnly=<%=session.getAttribute("programTransgenderOnly")%>;

		<%=session.getAttribute("programAgeValidationMethod")%>

		function error(msg) {
			alert(msg);
			return false;
		}

if (!Array.prototype.indexOf)
{
  Array.prototype.indexOf = function(elt /*, from*/)
  {
    var len = this.length;

    var from = Number(arguments[1]) || 0;
    from = (from < 0)
         ? Math.ceil(from)
         : Math.floor(from);
    if (from < 0)
      from += len;

    for (; from < len; from++)
    {
      if (from in this &&
          this[from] === elt)
        return from;
    }
    return -1;
  };
}


		function selectProgram(clientId,id,type) {
			var programId=Number(id);
			if (gender == 'M')
			{
				if (programFemaleOnly.indexOf(programId)>=0 ||  programTransgenderOnly.indexOf(programId)>=0)
				{
					return error("This gender not allowed in selected program.");
				}
			}
			if (gender == 'F')
			{
				if (programMaleOnly.indexOf(programId)>=0 ||  programTransgenderOnly.indexOf(programId)>=0)
				{
					return error("This gender not allowed in selected program.");
				}
			}
			if (gender == 'T')
			{
				if (programFemaleOnly.indexOf(programId)>=0 ||  programMaleOnly.indexOf(programId)>=0)
				{
					return error("This gender not allowed in selected program.");
				}
			}		
		
			if (!validAgeRangeForProgram(programId,age))
			{
				return error("This client does not meet the age range requirements for this program.");
			}
		
			opener.document.<%=request.getParameter("formName")%>.elements['<%=request.getParameter("formElementId")%>'].value=id;
			opener.document.<%=request.getParameter("formName")%>.elements['id'].value=clientId;
			
			<% if(request.getParameter("submit") != null && request.getParameter("submit").equals("true")) { %>
				opener.document.<%=request.getParameter("formName")%>.submit();
			<% } %>
			
			self.close();
		}
		
		
	</script>
	
	<body marginwidth="0" marginheight="0">
		<%@ include file="/common/messages.jsp"%>
		<div class="tabs" id="tabs">
			<table cellpadding="3" cellspacing="0" border="0">
				<tr>
					<th title="Programs">Search Results</th>
				</tr>
			</table>
		</div>
		
		<display:table class="simple" cellspacing="2" cellpadding="3" id="program" name="programs" pagesize="200" requestURI="/PMmodule/QuatroRefer.do">
			<display:setProperty name="paging.banner.placement" value="bottom" />
			<display:column sortable="true" title="Name">
				<a href="#javascript:void(0);" onclick="selectProgram('<c:out value="${id}" />','<c:out value="${program.id}" />','<c:out value="${program.type}" />');"><c:out value="${program.name}" /></a>
			</display:column>
			<display:column property="type" sortable="true" title="Type"></display:column>
			<display:column sortable="false" title="Participation">
				<c:out value="${program.numOfMembers}" />/<c:out value="${program.maxAllowed}" />&nbsp;(<c:out value="${program.queueSize}" /> waiting)
			</display:column>
			<display:column property="descr" sortable="false" title="Description"></display:column>
		</display:table>

	</body>
</html:html>
