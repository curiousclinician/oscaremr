<!-- 
/*
* 
* Copyright (c) 2001-2002. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved. *
* This software is published under the GPL GNU General Public License. 
* This program is free software; you can redistribute it and/or 
* modify it under the terms of the GNU General Public License 
* as published by the Free Software Foundation; either version 2 
* of the License, or (at your option) any later version. * 
* This program is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
* GNU General Public License for more details. * * You should have received a copy of the GNU General Public License 
* along with this program; if not, write to the Free Software 
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. * 
* 
* <OSCAR TEAM>
* 
* This software was written for 
* Centre for Research on Inner City Health, St. Michael's Hospital, 
* Toronto, Ontario, Canada 
*/
 -->
<%@ include file="/taglibs.jsp"%>
<table border="1" cellspacing="0" cellpadding="0" width="100%">
	<tr>
		<td align="left" width="200px" rowspan="3">
		<%if (oscar.OscarProperties.getInstance().isTorontoRFQ() && !oscar.OscarProperties.getInstance().getBooleanProperty("USE_CAISI_LOGO", "true")){%>
 	        <img src="<html:rewrite page="/images/QuatroShelterLogo.gif"  />" alt="QuatroShelter" border="0"/>
 	    <%} else {%>
	        <img src="<html:rewrite page="/images/caisi_1.jpg" />" alt="Caisi" id="caisilogo"  border="0"/>
	    <%}%>
        </td>
		<th rowspan="3"> &nbsp;<c:out value="${sessionScope.pageTitle}"/> </th>
        <td width="300px">
        <table width="100%" style="text-align: right"><tr>
        <td style="text-align: right"><html:link action="/Home.do">Home</html:link></td>
        <td style="text-align: right">
               <a target="_blank" href='<%=request.getContextPath()%>/help/index.html'>Help</a>
        </td>
        <td style="text-align: right">
               <a href='<%=request.getContextPath()%>/logout.jsp'>Logout</a>
        </td>
        </tr></table>
        </td>
	</tr>
	<tr>
        <td width="300px" style="text-align: right">User: <c:out value="${sessionScope.provider.formattedName}" /></td>
	</tr>
	<tr>
        <td width="300px" style="text-align: right">Shelter: <c:out value="${sessionScope.currentFacility.name}"/></td>
    </tr>
</table>
<!--</div>-->
