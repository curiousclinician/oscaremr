<%@ include file="/taglibs.jsp" %>
<%@ page import="java.util.*, java.sql.*, oscar.*, java.text.*,java.net.*" %>
<%
  //to prepare calendar display  
  String type = request.getParameter("type");
  int year = Integer.parseInt(request.getParameter("year"));
  int month = Integer.parseInt(request.getParameter("month"));
  //int day = now.get(Calendar.DATE);
  int delta = request.getParameter("delta")==null?0:Integer.parseInt(request.getParameter("delta")); //add or minus month
  GregorianCalendar now = new GregorianCalendar(year,month-1,1);
  String openerForm = request.getParameter("openerForm");
  String openerElement = request.getParameter("openerElement");
  now.add(Calendar.MONTH, delta);
  year = now.get(Calendar.YEAR);
  month = now.get(Calendar.MONTH)+1;
%>

<html:html locale="true">
<head><title>Calendar</title>
<script language="JavaScript">
<!--
function setfocus() {
  this.focus();
}
function getFormatedDate(year1, month1,day1)
{
	// date format defined as YYYY/MM/DD
	var sM = month1;
	if (month1 < 10) sM = "0" + sM;
	var sD = day1;
	if (day1 < 10) sD = "0" + sD;
	return year1 + "/" + sM + "/" + sD;
}
function typeInDate(year1,month1,day1) {
  self.close();
  opener.document.serviceform.xml_vdate.value=getFormatedDate(year1,month1,day1);
}
function typeSrvDate(year1,month1,day1) {
  self.close();
  opener.document.serviceform.xml_appointment_date.value=getFormatedDate(year1,month1,day1);
}

function typeCaisiDate(form_name,element_name,year1,month1,day1) {
  self.close();
  var test = "opener.document." + form_name + ".elements['" + element_name +"'].value";
  var val = "'" + getFormatedDate(year1,month1,day1) + "'";
  var myexpr = test + "=" + val;
  eval(myexpr);
}
//-->
</script>
</head>
<body bgcolor="ivory" onLoad="setfocus()">
<%
  now.add(Calendar.DATE, -1); 
  oscar.DateInMonthTable aDate = new oscar.DateInMonthTable(year, month-1, 1);
  int [][] dateGrid = aDate.getMonthDateGrid();
%>
      <table BORDER="0" CELLPADDING="0" CELLSPACING="0" WIDTH="100%">
  			<tr>
        	  <td BGCOLOR="#FFD7C4" width="50%" align="center" >
			  <a href="quatroCalendarPopup.jsp?year=<%=year-1%>&month=<%=month%>&delta=0&openerForm=<%=openerForm%>&openerElement=<%=openerElement %>"><img src="../images/previous.gif" WIDTH="10" HEIGHT="9" BORDER="0" ALT="View Last Year" vspace="2">Last Year
              </a>  <b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b>
        <a href="quatroCalendarPopup.jsp?year=<%=year+1%>&month=<%=month%>&delta=0&openerForm=<%=openerForm%>&openerElement=<%=openerElement %>">Next Year<img src="../images/next.gif" WIDTH="10" HEIGHT="9" BORDER="0" ALT="View Next Month" vspace="2"></a></td>
  			</TR>
  			<tr>
        	  <td BGCOLOR="#FFD7C4" width="50%" align="center" >
			  <a href="quatroCalendarPopup.jsp?year=<%=year%>&month=<%=month%>&delta=-1&openerForm=<%=openerForm%>&openerElement=<%=openerElement %>"><img src="../images/previous.gif" WIDTH="10" HEIGHT="9" BORDER="0" ALT="View Last Month" vspace="2">Last Month
              </a>&nbsp;  <b><span CLASS=title><%=year%>-<%=month%></span></b>&nbsp;
        <a href="quatroCalendarPopup.jsp?year=<%=year%>&month=<%=month%>&delta=1&openerForm=<%=openerForm%>&openerElement=<%=openerElement %>">Next Month<img src="../images/next.gif" WIDTH="10" HEIGHT="9" BORDER="0" ALT="View Next Month" vspace="2"></a></td>
  			</TR>
		</table>
<p>
          <table width="100%" border="1" cellspacing="0" cellpadding="2"  bgcolor="silver" >
            <tr bgcolor="#FOFOFO" align="center"> 
              <td width="12.5%"><font FACE="VERDANA,ARIAL,HELVETICA" SIZE="2" color="red"><bean:message key="billing.billingCalendarPopup.msgSun"/></font></td>
              <td width="12.5%"><font FACE="VERDANA,ARIAL,HELVETICA" SIZE="2"><bean:message key="billing.billingCalendarPopup.msgMon"/></font></td>
              <td width="12.5%"><font FACE="VERDANA,ARIAL,HELVETICA" SIZE="2"><bean:message key="billing.billingCalendarPopup.msgTue"/></font></td>
              <td width="12.5%"><font FACE="VERDANA,ARIAL,HELVETICA" SIZE="2"><bean:message key="billing.billingCalendarPopup.msgWed"/></font></td>
              <td width="12.5%"><font FACE="VERDANA,ARIAL,HELVETICA" SIZE="2"><bean:message key="billing.billingCalendarPopup.msgThu"/></font></td>
              <td width="12.5%"><font FACE="VERDANA,ARIAL,HELVETICA" SIZE="2"><bean:message key="billing.billingCalendarPopup.msgFri"/></font></td>
              <td width="12.5%"><font FACE="VERDANA,ARIAL,HELVETICA" SIZE="2" color="green"><bean:message key="billing.billingCalendarPopup.msgSat"/></font></td>
            </tr>
            
            <%
              for (int i=0; i<dateGrid.length; i++) {
                out.println("<tr>");
                for (int j=0; j<7; j++) {
                  if(dateGrid[i][j]==0) out.println("<td></td>");
                  else {
                    now.add(Calendar.DATE, 1);
                 %>
                     <td align="center" bgcolor='#FBECF3'><a href="#" onClick="typeCaisiDate('<%=openerForm %>','<%=openerElement %>',<%=year%>,<%=month%>,<%= dateGrid[i][j] %>)">
                      <%= dateGrid[i][j] %> </a>
                     </td>
                 <%
                  }
                 
                }
                out.println("</tr>");
              }
            %>
            
          </table>
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr> 
              <td>&nbsp;</td>
            </tr>
            <tr> 
              <td bgcolor="#FFD7C4"> 
                <div align="center"> 
                  <input type="button" name="Cancel" value=" <bean:message key="billing.billingCalendarPopup.btnExit"/> " onClick="window.close()">
                </div>
              </td>
            </tr>
          </table>

</body>
</html:html>
