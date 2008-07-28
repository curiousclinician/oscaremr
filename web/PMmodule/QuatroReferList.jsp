<%@ include file="/taglibs.jsp" %>
<%@page import="java.util.Date"%>
<%@page import="com.quatro.common.KeyConstants"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<script type="text/javascript" src='<c:out value="${ctx}"/>/js/quatroLookup.js'></script>
<script lang="javascript">
		function submitForm(methodVal) {
			trimInputBox();
			document.forms[0].method.value = methodVal;
			document.forms[0].submit();
		}
	
		function updateQuatroRefer(clientId, rId) {
			location.href = '<html:rewrite action="/PMmodule/QuatroRefer.do"/>' + "?method=edit&rId=" + rId + "&clientId=" + clientId;
		}	
</script>

<html-el:form action="/PMmodule/QuatroRefer.do">
<input type="hidden" name="method"/>
<input type="hidden" name="clientId"/>
<table width="100%"  cellpadding="0px" cellspacing="0px">
	<tr>
		<th class="pageTitle" align="center">Client Management - Referral</th>
	</tr>
	<tr>
			<td class="simple" style="background: lavender"><%@ include file="ClientInfo.jsp" %></td>
	</tr>	
	<tr>
		<td align="left" class="buttonBar2">
		<html:link action="/Home.do"
		style="color:Navy;text-decoration:none">&nbsp;
		<img style="vertical-align: middle" border=0 src=<html:rewrite page="/images/close16.png"/> />&nbsp;Close&nbsp;&nbsp;|</html:link>
	
		<html:link action="/PMmodule/ClientSearch2.do" style="color:Navy;text-decoration:none;">
		<img style="vertical-align: middle" border=0 src=<html:rewrite page="/images/Back16.png"/> />&nbsp;Back to Client Search&nbsp;&nbsp;</html:link>
		<security:oscarSec objectName="<%=KeyConstants.FUN_PMM_CLIENTREFER %>" rights="<%=KeyConstants.ACCESS_WRITE %>">
          <c:if test="${currentIntakeProgramId>0}">
			<html:link	action="/PMmodule/QuatroRefer.do?method=edit&rId=0" paramId="clientId"  paramName="clientId" style="color:Navy;text-decoration:none;">|&nbsp;
			<img style="vertical-align: middle" border=0 src=<html:rewrite page="/images/New16.png"/> />&nbsp;New Referral&nbsp;&nbsp;
			</html:link>
          </c:if>
		</security:oscarSec>	
		</td>
	</tr>
	<tr height="18px">
			<td align="left" class="message">			
			<logic:messagesPresent
				message="true">
				<html:messages id="message" message="true" bundle="pmm">
					<c:out escapeXml="false" value="${message}" />
				</html:messages>
			</logic:messagesPresent>
			<br /></td>
		</tr>
		<tr><td><div class="tabs">
              <table cellpadding="3" cellspacing="0" border="0">
                <tr><th>Referrals</th></tr>
              </table></div>
       </td></tr>
		<% int a=1; %>
		<tr>
			<td>
				<div style="color: Black; background-color: White; border-width: 1px; border-style: Ridge;
                    height: 500px; width: 100%; overflow: auto;" id="scrollBar">
               <display:table class="simple" sort="list" cellspacing="2" cellpadding="3" id="refer" name="lstRefers" export="false" pagesize="50" requestURI="/PMmodule/QuatroRefer.do">
			    <display:setProperty name="paging.banner.placement" value="bottom" />
    			<display:setProperty name="basic.msg.empty_list" value="No Referrals found." />
			    <display:column property="programName" sortable="true" title="Program Name" />
 			    <display:column property="referralDate.time" sortable="true" title="Created On" format="{0,date,yyyy/MM/dd hh:mm:ss a}" />
			    <display:column property="providerFormattedName" sortable="true" title="Staff" />
			    <display:column property="status" sortable="true" title="Status" />
			    <display:column  title="Actions">
					<c:choose>
						<c:when test="${'M' eq refer.autoManual and (refer.status eq 'pending') }">
							<a href="javascript:updateQuatroRefer('<c:out value="${refer.clientId}" />', '<c:out value="${refer.id}" />')" >Update</a>
						</c:when>						
						<c:otherwise>
							<a href="javascript:updateQuatroRefer('<c:out value="${refer.clientId}" />', '<c:out value="${refer.id}" />')" >View</a>
						</c:otherwise>
				   </c:choose>
			    </display:column>
			   </display:table>
			    
				</div>
			</td>
		</tr>		
			
	</table>
</html-el:form>
