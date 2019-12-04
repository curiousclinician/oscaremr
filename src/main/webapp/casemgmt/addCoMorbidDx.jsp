<%@ page import="oscar.oscarResearch.oscarDxResearch.bean.dxResearchBeanHandler" %>
<%@ page import="java.util.Vector" %>
<%@ page import="org.oscarehr.common.dao.DxresearchDAO" %>
<%@ page import="org.oscarehr.util.SpringUtils" %>
<%@ page import="org.oscarehr.common.model.Dxresearch" %>
<%@ page import="java.util.List" %>
<%@ page import="org.oscarehr.common.dao.Icd9Dao" %>
<%@ page import="org.oscarehr.common.model.Icd9" %><%--


    Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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

    This software was written for
    Centre for Research on Inner City Health, St. Michael's Hospital,
    Toronto, Ontario, Canada

--%>
<%@ include file="/casemgmt/taglibs.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html:html locale="true">
<head>
	<title>Add Co-morbid ICD9 Dx</title>
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-3.1.0.min.js"></script>
	<script type="text/javascript">
		function addCoMorbidity() {
			var selectedVal = $('#dx-select').val();
            var openerElement = $('#<%=request.getParameter("noteDx")%>', window.opener.document);
            
            var dxCode = selectedVal.split(';')[0];
            var dxDescription = selectedVal.split(';')[1];
            var existingDiagnosticIdElem = openerElement.find('[name=diagnostic_id]');
            existingDiagnosticIdElem.val(existingDiagnosticIdElem.val() + ';' + dxCode);
            openerElement.find('.co-morbid-link').replaceWith(' (Co-morbid: ' + dxCode + ' ' + dxDescription + ')');
            
            window.close();
        }
	</script>
	<%
		String demographicNo = request.getParameter("demographicNo");
		DxresearchDAO dxresearchDAO = SpringUtils.getBean(DxresearchDAO.class);
		Icd9Dao icd9Dao = SpringUtils.getBean(Icd9Dao.class);
		List<Dxresearch> dxCodes = dxresearchDAO.findActiveByDemographicNoAndCodeSystem(Integer.parseInt(demographicNo), "icd9");
	%>
</head>
<body style="text-align: center;">
<div style="background-color: #CCCCFF">
	<label for="dx-select">Add Co-morbid ICD9 Dx code to existing disease registry: </label>
</div>
<div>
	<select id="dx-select">
		<% for (Dxresearch dxCode : dxCodes) {
			Icd9 code = icd9Dao.findByCode(dxCode.getDxresearchCode());
			if (code != null) {
		%>
		<option value="<%=code.getCode()%>;<%=code.getDescription()%>">
			<%=code.getCode()%>: <%=code.getDescription()%>
		</option>
		<%		} 
			} %>
	</select>
	<button onclick="addCoMorbidity()">Add</button>
</div>
</body>
</html:html>