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

<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%
    String roleName2$ = (String)session.getAttribute("userrole") + "," + (String) session.getAttribute("user");
    boolean authed=true;
%>
<security:oscarSec roleName="<%=roleName2$%>" objectName="_form" rights="r" reverse="<%=true%>">
	<%authed=false; %>
	<%response.sendRedirect("../securityError.jsp?type=_form");%>
</security:oscarSec>
<%
	if(!authed) {
		return;
	}
%>

<%@page import="org.oscarehr.util.LoggedInInfo"%>
<%@page import="org.oscarehr.util.MiscUtils"%>
<%@page import="org.oscarehr.PMmodule.caisi_integrator.CaisiIntegratorManager"%>
<%@page import="org.oscarehr.util.LocaleUtils"%>    
<%@page import="org.oscarehr.common.dao.FrmLabReqPreSetDao, org.oscarehr.util.SpringUtils"%>
<%@page import="oscar.form.*, oscar.OscarProperties, java.util.Date, oscar.util.UtilDateUtilities"%>
<%@page import="org.oscarehr.util.MiscUtils,oscar.oscarClinic.ClinicData"%>
<%@page import="org.oscarehr.PMmodule.model.Program" %>
<%@page import="org.oscarehr.PMmodule.dao.ProgramDao" %>
<%@page import="org.oscarehr.PMmodule.dao.ProviderDao" %>
<%@page import="org.oscarehr.common.dao.ClinicDAO" %>
<%@page import="org.oscarehr.common.model.Clinic" %>
<%@page import="org.oscarehr.common.model.Provider" %>
<%@page import="org.oscarehr.util.SpringUtils" %>
<%@page import="java.util.List" %>
<%@page import="oscar.oscarRx.data.RxProviderData" %>
<%@page import="org.oscarehr.util.LocaleUtils"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/rewrite-tag.tld" prefix="rewrite"%>

<html:html locale="true">
<head>
<script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
<title>Laboratory Requisition</title>
<html:base />
<link rel="stylesheet" type="text/css" media="screen"
	href="labReq07Style.css">
<link rel="stylesheet" type="text/css" media="print" href="print.css">
<script src="../share/javascript/prototype.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/jquery-1.7.1.min.js"></script>
<link rel="stylesheet" type="text/css" media="all" href="../share/css/extractedFromPages.css"  />
</head>

<%
	String formClass = "LabReq10";
	String formLink = "formlabreq10.jsp";
	
	ClinicData clinic = new ClinicData();
  ProviderDao providerDao = (ProviderDao)SpringUtils.getBean("providerDao");
	List<Provider> prList = providerDao.getDoctorsWithPractionerNo();
	
	ProgramDao programDao = SpringUtils.getBean(ProgramDao.class);
	List<Program> programList = programDao.getAllActivePrograms();
	LoggedInInfo loggedInInfo=LoggedInInfo.getLoggedInInfoFromSession(request);
	
  ClinicDAO clinicDao = (ClinicDAO)SpringUtils.getBean("clinicDAO");
  Clinic clin = clinicDao.getClinic();

  String clinicName = clin.getClinicName() == null ? "" : clin.getClinicName(); 
  String clinicProvince = clin.getClinicProvince() == null ? "" : clin.getClinicProvince();
  String clinicAddress = clin.getClinicAddress() == null ? "" : clin.getClinicAddress();
  String clinicCity = clin.getClinicCity() == null ? "" : clin.getClinicCity();
  String clinicPC = clin.getClinicPostal() == null ? "" : clin.getClinicPostal();

   boolean readOnly = false;
   int demoNo = Integer.parseInt(request.getParameter("demographic_no"));
   int formId = Integer.parseInt(request.getParameter("formId"));
   String provNo = (String) session.getAttribute("user");
	String remoteFacilityIdString=request.getParameter("remoteFacilityId");
	String fromSession = request.getParameter("fromSession");
   java.util.Properties props =null;	        

   // means it's local
	if (remoteFacilityIdString==null)
	{
		FrmRecord rec = (new FrmRecordFactory()).factory(formClass);
		if(fromSession != null && fromSession.equals("true")) {
			props = (java.util.Properties)request.getSession().getAttribute("labReq10"+demoNo);	
		}
		if(props == null) {
	   		props = rec.getFormRecord(LoggedInInfo.getLoggedInInfoFromSession(request),demoNo, formId);	        
			props = ((FrmLabReq10Record) rec).getFormCustRecord(props, provNo);
		}		
	}
	else // it's remote
	{
		MiscUtils.getLogger().debug("Getting remote form : "+remoteFacilityIdString+":"+formId);
		props=FrmLabReq10Record.getRemoteRecordProperties(loggedInInfo, Integer.parseInt(remoteFacilityIdString), formId);
		FrmRecordHelp.convertBooleanToChecked(props);
	}
	
   MiscUtils.getLogger().debug("properties : "+props);
   
   OscarProperties oscarProps = OscarProperties.getInstance();

   if (request.getParameter("labType") != null){
      if (formId == 0 ){
         FrmLabReqPreSetDao preSetDao = (FrmLabReqPreSetDao) SpringUtils.getBean("frmLabReqPreSetDao");
         String labPreSet = request.getParameter("labType");
         props = preSetDao.fillPropertiesByLabType(labPreSet,props);
      }
   }
   
   if (request.getParameter("readOnly") != null){
      readOnly = true;    
   }

   String patientName = props.getProperty("patientName"," , ");
   String[] patientNames = patientName.split(",");
   
   String[] patientDOB = props.getProperty("birthDate", " / / ").split("/");
   request.removeAttribute("submit");
%>

<script type="text/javascript" language="Javascript">

var temp;
temp = "";


    function onPrintPDF() {
         
        var ret = checkAllDates();
		if (document.forms[0].letterhead.value == '') {
			alert("You must select a letterhead provider.");
			return false;
		}
        if(ret==true)
        {            
            
            //ret = confirm("Do you wish to save this form and view the print preview?");
            //popupFixedPage(650,850,'../provider/notice.htm');
            temp=document.forms[0].action;         
            document.forms[0].action = "<rewrite:reWrite jspPage="formname.do?__title=Lab+Request&__cfgfile=labReqPrintEncounterForm2010&__template=labReqForm2010"/>";
            document.forms[0].submit.value="printall"; 
            document.forms[0].target="_self";
        }
        return ret;
    }
    function onSave() {
        if (temp != "") { document.forms[0].action = temp; }
        document.forms[0].target="_self";        
        document.forms[0].submit.value="save";
        var ret = checkAllDates();
		if (document.forms[0].letterhead.value == '') {
			alert("You must select a letterhead provider.");
			return false;
		}
        return ret;
    }
    
    function onSaveExit() {
        if (temp != "") { document.forms[0].action = temp; }
        document.forms[0].target="_self";
        document.forms[0].submit.value="exit";
        var ret = checkAllDates();
		if (document.forms[0].letterhead.value == '') {
			alert("You must select a letterhead provider.");
			return false;
		}
        if(ret == true)
        {
            ret = confirm("Are you sure you wish to save and close this window?");
        }
        return ret;
    }
function popupFixedPage(vheight,vwidth,varpage) { 
  var page = "" + varpage;
  windowprop = "height="+vheight+",width="+vwidth+",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=10,screenY=0,top=0,left=0";
  var popup=window.open(page, "planner", windowprop);
}

/**
 * DHTML date validation script. Courtesy of SmartWebby.com (http://www.smartwebby.com/dhtml/)
 */
// Declaring valid date character, minimum year and maximum year
var dtCh= "/";
var minYear=1900;
var maxYear=3100;

    function isInteger(s){
        var i;
        for (i = 0; i < s.length; i++){
            // Check that current character is number.
            var c = s.charAt(i);
            if (((c < "0") || (c > "9"))) return false;
        }
        // All characters are numbers.
        return true;
    }

    function stripCharsInBag(s, bag){
        var i;
        var returnString = "";
        // Search through string's characters one by one.
        // If character is not in bag, append to returnString.
        for (i = 0; i < s.length; i++){
            var c = s.charAt(i);
            if (bag.indexOf(c) == -1) returnString += c;
        }
        return returnString;
    }

    function daysInFebruary (year){
        // February has 29 days in any year evenly divisible by four,
        // EXCEPT for centurial years which are not also divisible by 400.
        return (((year % 4 == 0) && ( (!(year % 100 == 0)) || (year % 400 == 0))) ? 29 : 28 );
    }
    function DaysArray(n) {
        for (var i = 1; i <= n; i++) {
            this[i] = 31
            if (i==4 || i==6 || i==9 || i==11) {this[i] = 30}
            if (i==2) {this[i] = 29}
       }
       return this
    }

    function isDate(dtStr){
        var daysInMonth = DaysArray(12)
        var pos1=dtStr.indexOf(dtCh)
        var pos2=dtStr.indexOf(dtCh,pos1+1)
        var strMonth=dtStr.substring(0,pos1)
        var strDay=dtStr.substring(pos1+1,pos2)
        var strYear=dtStr.substring(pos2+1)
        strYr=strYear
        if (strDay.charAt(0)=="0" && strDay.length>1) strDay=strDay.substring(1)
        if (strMonth.charAt(0)=="0" && strMonth.length>1) strMonth=strMonth.substring(1)
        for (var i = 1; i <= 3; i++) {
            if (strYr.charAt(0)=="0" && strYr.length>1) strYr=strYr.substring(1)
        }
        month=parseInt(strMonth)
        day=parseInt(strDay)
        year=parseInt(strYr)
        if (pos1==-1 || pos2==-1){
            return "format"
        }
        if (month<1 || month>12){
            return "month"
        }
        if (day<1 || day>31 || (month==2 && day>daysInFebruary(year)) || day > daysInMonth[month]){
            return "day"
        }
        if (strYear.length != 4 || year==0 || year<minYear || year>maxYear){
            return "year"
        }
        if (dtStr.indexOf(dtCh,pos2+1)!=-1 || isInteger(stripCharsInBag(dtStr, dtCh))==false){
            return "date"
        }
    return true
    }


    function checkTypeIn(obj) {
      if(!checkTypeNum(obj.value) ) {
          alert ("You must type in a number in the field.");
        }
    }

    function valDate(dateBox)
    {
        try
        {
            var dateString = dateBox.value;
            if(dateString == "")
            {
    //            alert('dateString'+dateString);
                return true;
            }
            var dt = dateString.split('/');
            if (dt[1] == null) 
                dt = dateString.split('-');
            var y = dt[0];
            var m = dt[1];
            var d = dt[2];
            var orderString = m + '/' + d + '/' + y;
            var pass = isDate(orderString);

            if(pass!=true)
            {
                var s = dateBox.name;
                //alert('Invalid '+pass+' in field ' + s.substring(3));
                dateBox.focus();
                return false;
            }
        }
        catch (ex)
        {
            alert('Catch Invalid Date in field ' + dateBox.name);
            dateBox.focus();
            return false;
        }
        return true;
    }

    function checkAllDates()
    {
        var b = true;
        if (valDate(document.forms[0].b_dateSigned) == false) {
            alert("The 'Signature Date' field is not valid");
            b = false;
        }
        if (valDate(document.forms[0].o_specimenCollectionDate) == false) {
            alert("The 'Specimen Collection Date' field is not valid");
            b = false;
         }
        return b;

    }

    function popup(link) {
    	windowprops = "height=700, width=960,location=no,"
   			 + "scrollbars=yes, menubars=no, toolbars=no, resizable=no, top=0, left=0 titlebar=yes";
    	window.open(link, "_blank", windowprops);
	}
    
    
    var providerData = new Object(); //{};
    <%
    RxProviderData rxProviderData = new RxProviderData();
    for (Provider p : prList) {
      if( !p.getProviderType().equals("doctor") )
        continue;
      
    	if (!p.getProviderNo().equalsIgnoreCase("-1")) {
    		String prov_no = "prov_"+p.getProviderNo();
        
        String comments = p.getComments();
        String xmlSpecialtyCode = "<xml_p_specialty_code>";
        String xmlSpecialtyCode2 = "</xml_p_specialty_code>";
        String strSpecialtyCode = "00";
        if( comments != null && comments.indexOf(xmlSpecialtyCode) != -1 ) {
          strSpecialtyCode = comments.substring(comments.indexOf(xmlSpecialtyCode) + xmlSpecialtyCode.length(), comments.indexOf(xmlSpecialtyCode2));
          strSpecialtyCode = strSpecialtyCode.trim();
          if( strSpecialtyCode.equals("") ) {
            strSpecialtyCode = "00";
          }
        }
        
        String num = p.getOhipNo() == null ? "" : p.getOhipNo();
        String practitionerNo="0000-" + num + "-" + strSpecialtyCode;
        String cpso= p.getPractitionerNo()!=null?p.getPractitionerNo():"";
        oscar.oscarRx.data.RxProviderData.Provider rxp = rxProviderData.getProvider(p.getProviderNo());
    		%>
    providerData['<%=prov_no%>'] = new Object(); //{};
	providerData['<%=prov_no%>'].providerNo = "<%=p.getProviderNo()%>";
    providerData['<%=prov_no%>'].clinic_name = "<%=clinicName %>";
    providerData['<%=prov_no%>'].address = "<%=rxp.getClinicAddress() %>";
    providerData['<%=prov_no%>'].city = "<%=rxp.getClinicCity() %>";
    providerData['<%=prov_no%>'].province = "<%=rxp.getClinicProvince() %>";
    providerData['<%=prov_no%>'].postal = "<%=rxp.getClinicPostal() %>";
    providerData['<%=prov_no%>'].first_name = "<%=p.getFirstName() %>";
    providerData['<%=prov_no%>'].last_name = "<%=p.getLastName() %>";
    providerData['<%=prov_no%>'].formatted_name = "<%=p.getFormattedName() %>";
    providerData['<%=prov_no%>'].prac_no = "<%=practitionerNo %>";
    providerData['<%=prov_no%>'].cpso = "<%=cpso %>";
	providerData['<%=prov_no%>'].ohip_no = "<%=num%>";
	providerData['<%=prov_no%>'].specialty = "<%=p.getSpecialty()%>";
    <%	}
	}

	//get default clinic info
	oscar.oscarRx.data.RxProviderData.Provider rxp = rxProviderData.getProvider(null);
	%>
	providerData[''] = new Object(); //{};
	providerData[''].providerNo = "";
	providerData[''].clinic_name = "<%=clinicName%>";
	providerData[''].address = "<%=rxp.getClinicAddress() %>";
	providerData[''].city = "<%=rxp.getClinicCity() %>";
	providerData[''].province = "<%=rxp.getClinicProvince() %>";
	providerData[''].postal = "<%=rxp.getClinicPostal() %>";
	providerData[''].first_name = "";
	providerData[''].last_name = "";
	providerData[''].formatted_name = "";
	providerData[''].prac_no = "";
	providerData[''].cpso = "";
	providerData[''].ohip_no = "";
	providerData[''].specialty = "";

<%
if (OscarProperties.getInstance().getBooleanProperty("consultation_program_letterhead_enabled", "true")) {
	if (programList != null) {
		for (Program p : programList) {
			String progNo = "prog_" + p.getId();
%>
		providerData['<%=progNo %>'] = new Object();
		providerData['<%=progNo %>'].address = "<%=(p.getAddress() != null && p.getAddress().trim().length() > 0) ? p.getAddress().trim() : ((clinic.getClinicAddress() + "  " + clinic.getClinicCity() + "   " + clinic.getClinicProvince() + "  " + clinic.getClinicPostal()).trim()) %>";
		providerData['<%=progNo %>'].city = "";
		providerData['<%=progNo %>'].province = "";
		providerData['<%=progNo %>'].postal = "";
<%
		}
	}
} %>

    
    function switchProvider(value) {
    	
    	if (value==-1) {
    		$("select[name='letterhead']").value = value;
    		$("input[name='clinicName']").val ("<%=clinic.getClinicName()%>");
    		$("input[name='clinicAddress']").val ("<%=clinic.getClinicAddress() %>");
    		$("input[name='clinicCity']").val ("<%=clinic.getClinicCity() + " " + clinic.getClinicProvince()%>");
    		$("input[name='clinicPC']").val ("<%=clinic.getClinicPostal()  %>");
            $("input[name='practitionerNo']").val("");
            $("input[name='reqProvName']").val("");

            $("#pracNo").html("&nbsp;");
    		$("#clinicName").html("<%=clinic.getClinicName()%>");
    		$("#clinicAddress").html("<%=clinic.getClinicAddress() %>");
    		$("#clinicCity").html("<%=clinic.getClinicCity() + " " + clinic.getClinicProvince()%>");
    		$("#clinicPC").html("<%=clinic.getClinicPostal()  %>");
            $("#reqProvName").html("&nbsp;");
    	} else {

			if (typeof providerData["prov_" + value] != "undefined")
                value = "prov_" + value;
                
    		$("select[name='letterhead']").value = value;
    		
    		$("input[name='clinicName']").val (providerData[value]['clinic_name']);
    		$("input[name='clinicAddress']").val (providerData[value]['address']);
    		$("input[name='clinicCity']").val (providerData[value]['city'] + providerData[value]['province']);
    		$("input[name='clinicPC']").val (providerData[value]['postal']);
        	$("input[name='practitionerNo']").val(providerData[value]['prac_no']);
            $("input[name='cpsoNo']").val(providerData[value]['cpso']);
            $("input[name='reqProvName']").val(providerData[value]['formatted_name']);
            $("#cpsoNo").html(providerData[value]['cpso']);


			if (value == "") {
				$("#pracNo").html('');
				$("input[name='practitionerNo']").val('');
			} else if (providerData[value]['ohip_no'] != "" && !providerData[value]['specialty'].toUpperCase().startsWith('NP')) {
				$("#pracNo").html(providerData[value]['prac_no']);
				$("input[name='practitionerNo']").val(providerData[value]['prac_no']);
			} else if ("<%=props.getProperty("mrp", "")%>" != "") { // If the requesting physician does not have a billing number, use MRP's
				$("#pracNo").html(providerData["prov_<%=props.getProperty("mrp", "")%>"]['prac_no']);
				$("input[name='practitionerNo']").val(providerData["prov_<%=props.getProperty("mrp", "")%>"]['prac_no']);
			} else { //else just use the requesting physician's ohip number
				$("#pracNo").html("<%=props.getProperty("practitionerNo", "")%>");
				$("input[name='practitionerNo']").val("<%=props.getProperty("practitionerNo", "")%>");
			}
            $("#clinicName").html(providerData[value]['clinic_name']);
            $("#clinicAddress").html(providerData[value]['address']);
            $("#clinicCity").html(providerData[value]['city'] + " " + providerData[value]['province']);
            $("#clinicPC").html(providerData[value]['postal']);
            $("#reqProvName").html(providerData[value]['formatted_name']);
        }
        

    	<% if (oscarProps.isPropertyActive("queens_labreq10_settings")) { %>
			var mrpProviderData = providerData['prov_<%=props.getProperty("mrp", "")%>'];
			var rules = getQueensSpecialtyRules(providerData[value], mrpProviderData);
			if (rules != null) {
				//billing number
				$("#pracNo").html(rules.billingNo);
				$("input[name='practitionerNo']").val(rules.billingNo);

				if (rules.copyToMrp) {
					$("#copy2clinician").prop('checked', true);
					$("#copyLname").val(mrpProviderData.last_name);
					$("#copyFname").val(mrpProviderData.first_name);
					$("#copyAddress").val(mrpProviderData.address);
				} else {
					$("#copy2clinician").prop('checked', false);
					$("#copyLname").val("");
					$("#copyFname").val("");
					$("#copyAddress").val("");
				}
			}
        <% } %>
    }

	/** returns object with rules defined by queens for how MRP is diplayed,
	 * what the lab req billing number should be, whether to copy to clinition
	 * should be checked, and the address to copy to
	 * @param providerData
	 * @param mrpProviderData
	 */
	function getQueensSpecialtyRules(providerData, mrpProviderData) {
		if (providerData == undefined) return null;
		if (mrpProviderData == undefined) return null;
		var specialty = (providerData['specialty'] != '' ? providerData['specialty'] : "MRP").toUpperCase();
		var provNo = providerData['providerNo'];
		var mrpProvNo = mrpProviderData['providerNo'];
		
		var rules = new Object();
		rules.displayMrp = (mrpProvNo != provNo); // By defualt displayMrp is true if the mrp is not the requesting provider
		rules.billingNo = providerData['prac_no'];
		rules.copyToMrp = (mrpProvNo != provNo);
		
		if (specialty.startsWith('MRP')) {
			rules.displayMrp = (mrpProvNo != provNo);
			rules.billingNo = providerData['prac_no'];
			rules.copyToMrp = (mrpProvNo != provNo);
		} else if (specialty.startsWith('LOCUM') || specialty.startsWith('NP')) {
			rules.displayMrp = true;
			rules.billingNo = providerData['prac_no'];
			rules.copyToMrp = true;
		} else if (specialty.startsWith('NORTH KINGSTON')) {
			rules.displayMrp = (mrpProvNo != provNo);
			rules.billingNo = mrpProviderData['prac_no'];
			rules.copyToMrp = false;
		} else if (specialty.startsWith('RESIDENT') || specialty.startsWith('PHARMACIST') 
				|| specialty.startsWith('RN') || specialty.startsWith('RPN')) {
			rules.displayMrp = true;
			rules.billingNo = mrpProviderData['prac_no'];
			rules.copyToMrp = true;
		}
    	return rules;
	}
    
    $(document).ready(function(){
    	switchProvider($("select[name='letterhead']").val());
    });
</script>

<body style="page: doublepage; page-break-after: right">
<html:form action="/form/formname">

	<input type="hidden" name="demographic_no"
		value="<%= props.getProperty("demographic_no", "0") %>" />
	<input type="hidden" name="patientLastName"
		value="<%=patientNames[0].trim()%>" />
	<input type="hidden" name="patientFirstName"
		value="<%=patientNames[1].trim()%>" />
	<input type="hidden" name="patientBirthYear"
		value="<%=patientDOB[0].trim()%>" />
	<input type="hidden" name="patientBirthMth"
		value="<%=patientDOB[1].trim()%>" />
	<input type="hidden" name="patientBirthDay"
		value="<%=patientDOB[2].trim()%>" />
	<input type="hidden" name="ID"
		value="<%= props.getProperty("ID", "0") %>" />
	<input type="hidden" name="provider_no"
		value=<%=request.getParameter("provNo")%> />
	<input type="hidden" name="formCreated"
		value="<%= props.getProperty("formCreated", "") %>" />
	<input type="hidden" name="form_class" value="<%=formClass%>" />
	<input type="hidden" name="form_link" value="<%=formLink%>" />
	<input type="hidden" name="provNo"
		value="<%= request.getParameter("provNo") %>" />
	<input type="hidden" name="submit" value="exit" />
	<input type="hidden" name="formId" value="<%=formId%>" />
	<table class="Head" class="hidePrint">
		<tr>
			<td nowrap="true">
				<% if(!readOnly){ %> 
					<input type="submit" value="Save" onclick="javascript:return onSave();" /> 
					<input type="submit" value="Save and Exit" onclick="javascript:return onSaveExit();" />
				 <% } %>
				<input type="submit" value="Exit" onclick="javascript:return onExit();" /> 
				<input type="submit" value="Print Pdf" onclick="javascript:return onPrintPDF();" />

				<select name="letterhead" id="letterhead" onchange="switchProvider(this.value)">
				<%

					//Gets the letterhead provider number
					String providerNumber = props.getProperty("letterhead", "-1");
					//If there is no provider in the letterhead
					if(providerNumber.isEmpty() || providerNumber.equals("-1")) {
					    String loggedInProviderCpsoNumber = loggedInInfo.getLoggedInProvider().getPractitionerNo();
					    //Checks if the currently logged in provider is billable, if so then it becomes the provider number to select
						if (loggedInProviderCpsoNumber != null && !loggedInProviderCpsoNumber.equals("")) {
							providerNumber = loggedInInfo.getLoggedInProviderNo();
						} else {
						    //Else it gets the demographics MRP
							providerNumber = props.getProperty("demoProvider", "-1");
						}
						if ("".equals(providerNumber)) {
						    providerNumber = null;
						    props.setProperty("reqProvName", "");
							props.setProperty("practitionerNo", "");
							props.setProperty("cpso", "");
						}
					}
				%>
				<option value="" <%= providerNumber == null ? "selected=\"selected\"" : "" %>>
					Select Requisitioning Physician
				</option>
				<%	for (Provider p : prList) {
						if (p.getProviderNo().compareTo("-1") != 0 && (p.getFirstName() != null || p.getLastName() != null) && p.getProviderType().equals("doctor")) {
				%>
				<option value="<%=p.getProviderNo() %>" <%= p.getProviderNo().equals(providerNumber) ? "selected=\"selected\"" : "" %>>
		
					<%=p.getLastName() %>, <%=p.getFirstName()%> <%=((oscarProps.isPropertyActive("queens_labreq10_settings") && !p.getSpecialty().isEmpty()) ? "(" + p.getSpecialty() + ")" : "")%>
				</option>
				<% }} %>
				<option value="-1"><%=clinic.getClinicName() %></option>
				<%
				if (OscarProperties.getInstance().getBooleanProperty("consultation_program_letterhead_enabled", "true")) {
				for (Program p : programList) {
				%>
					<option value="prog_<%=p.getId() %>" <%=(!props.getProperty("letterhead","-1").equals("-1") && props.getProperty("letterhead","-1").equals("prog_"+p.getId()))?" selected=\"selected\" ":"" %>>
					<%=p.getName() %>
					</option>
				<% }
				}%>
				</select>

			</td>
		</tr>
	</table>

	<!-- class="TableWithBorder" -->
	<table class="outerTable" width="100%">
		<tr>
			<td width="40%" class="outerTable">



			<table width="100%" class="topTable">
				<tr>
					<td class="title" colspan="3" nowrap="nowrap">LABORATORY REQUISITION</td>
				</tr>
				<tr>
					<td colspan="3" nowrap="nowrap">Requisitioning
					Physician/Practitioner:<br>
					<input type="hidden" style="width: 100%" name="provName"
						value="<%=props.getProperty("provName", "")%>" /> <input
						type="hidden" style="width: 100%" name="reqProvName"
						value="<%=props.getProperty("reqProvName", "")%>" /><span id="reqProvName"><%=props.getProperty("reqProvName", "")%>&nbsp;</span><br>
						<span id="MRP"><%=props.getProperty("provName", "")%>&nbsp;</span><br>			
					<input type="hidden" style="width: 100%" name="clinicName" value="<%=props.getProperty("clinicName","")%>" /><span id="clinicName"><%=props.getProperty("clinicName","")%></span><br>
					<input type="hidden" style="width: 100%" name="clinicAddress" value="<%=props.getProperty("clinicAddress", "")%>" /> <span id="clinicAddress"><%=props.getProperty("clinicAddress", "")%></span><br>
					<input type="hidden" style="width: 100%" name="clinicCity" value="<%=props.getProperty("clinicCity", "")%>" /><span id="clinicCity"> <%=props.getProperty("clinicCity", "")%>,<%=props.getProperty("clinicProvince","") %></span><br>
					<input type="hidden" style="width: 100%" name="clinicPC" value="<%=props.getProperty("clinicPC", "")%>" /><span id="clinicPC"> <%=props.getProperty("clinicPC", "")%></span><br>
					<%  String lifeLabsContractId = "Report to Contract ID: " + oscarProps.getProperty("lifeLabs_contract_id");
						if (oscarProps.getProperty("lifeLabs_contract_id") != null) { %>
						<input type="hidden" style="width: 100%" name="lifeLabsContractId" value="<%=lifeLabsContractId%>" />
						<b><span id="lifeLabsContractId"><%=lifeLabsContractId%></span></b><br>
					<% } %>
					</td>
				</tr>
				<tr>
					<td class="borderGray" style="border-bottom: 0px;width: 70%"><font
						class="subHeading">Physician/Practitioner Number</font><br>
					<input type="hidden" name="practitionerNo"
						   value="<%=props.getProperty("practitionerNo", "")%>" />
					<span id="pracNo"><%=props.getProperty("practitionerNo", "")%>&nbsp;</span>
					</td>
					<td class="borderGrayTopBottom" style="border-bottom: 0px;"><font class="subHeading">CPSO No.</font><br>
						<input type="hidden" name="cpsoNo"
							   value="<%=props.getProperty("cpso", "")%>" />
						<span id="cpsoNo"><%=props.getProperty("cpso", "")%>&nbsp;</span>
					</td>

				</tr>
				<tr>
					<td class="borderBlackTopBottom"><b><font
						class="subHeading">Check one:</font></b></br>
					<font style="font-size: 10px;">
					<div style="margin-left: 10px;"><input type="checkbox"
						name="ohip" <%=props.getProperty("ohip", "")%> /><b>OHIP/Insured</b>&nbsp;
					&nbsp; <input type="checkbox" name="thirdParty"
						<%=props.getProperty("thirdParty", "")%> /><b>Third
					Party/Uninsured</b><br>
					<input type="checkbox" name="wcb" <%=props.getProperty("wcb", "")%> /><b>WCB</b><br>
					</div>
					</font></td>
					<td class="borderBlackTopBottom"></td>
				</tr>
				<tr>
					<td class="borderGrayTopBottom" style="border-top: 0px;"><font
						class="subHeading">Additional Clinical Information <i
						style="font-size: -1;">(e.g. diagnosis)</i></font><br>
					<textarea name="aci" style="width: 100%; height: 59px;"
						tabindex="1">
					<%
                    if (props.getProperty("aci") == null) {
                        if (oscarProps.getProperty("clinic_code") != null)
                           out.print(" \n" + oscarProps.getProperty("clinic_code"));
                    } else {
                        out.print(props.getProperty("aci", "").trim());
                    }
                %>
					</textarea></td>
				</tr>
				<tr>
					<td style="height: 15px; vertical-align: top;">
						<font class="subHeading">
							<input type="checkbox" id="copy2clinician" name="copy2clinician" <%=props.getProperty("copy2clinician", "")%> />
							Copy to: Clinician/Practitioner
						</font><br />
                                                <table width="100%">
                                                    <tr>
                                                        <td style="color:grey;">Last Name</td>
                                                        <td style="color:grey;">First Name</td>
                                                    </tr>
                                                    <tr>
                                                        <td><input type="text" id="copyLname" name="copyLname" value="<%=props.getProperty("copyLname", "")%>"></td>
                                                        <td><input type="text" id="copyFname" name="copyFname" value="<%=props.getProperty("copyFname", "")%>"></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="color:grey;">Address</td>
                                                        <td style="color:grey;">&nbsp;</td>
                                                    </tr>
                                                    <tr>
                                                        <td colspan="2"><textarea id="copyAddress" name="copyAddress" style="width:100%;"><%=props.getProperty("copyAddress", "")%></textarea></td>
                                                    </tr>
                                                </table>
					</td>
				</tr>
			</table>
			</td>


			<td width="70%" class="outerTable">



			<table width="100%" class="topTable">
				<tr>
					<td class="labArea" style="vertical-align: top; height: 154px;"
						colspan="3"><b><i><font class="subHeading">Laboratory
					Use Only:</font></i></b></br>
					</td>
				</tr>
				<tr>
					<td class="borderGray"
						style="border-left: 0; height: 33px; vertical-align: top; border-bottom: 1px solid black;">
					<font class="subHeading">Clinician/Practitioner's Contact
					Number for Urgent Results</font> <input type="text"
						style="margin-left: 10px; font-size: 10px; width: 200px;"
						tabindex="2" name="clinicianContactUrgent"
						value="<%=props.getProperty("clinicianContactUrgent", "")%>" />
					</td>
					<td class="labArea borderGray"
						style="border-right: 0; border-left: 0; border-bottom: 1px solid black; vertical-align: top; width: 200px;"><font
						class="subHeading">Service Date (yyyy/mm/dd)</font></td>
				</tr>
				<tr>
					<td colspan="2">
					<table width="100%">
						<tr>
							<td class="borderGrayBottomRight"><font class="subHeading">Patient's
							Name:</font><br />
							<input type="hidden" style="width: 90%" name="patientName"
								value="<%=props.getProperty("patientName", "")%>" /> <%=props.getProperty("patientName", "")%>&nbsp;
							</td>
							<td class="borderGrayBottomRight" style="width: 200px;"><font
								class="subHeading">Health Number:</font><br />
							<input type="hidden" name="healthNumber" size="10"
								value="<%=props.getProperty("healthNumber", "")%>" />
							<center><%=props.getProperty("healthNumber", "")%>&nbsp;</center>
							</td>
							<td class="borderGrayBottomRight" style="width: 20px;"><font
								class="subHeading">Version:</font><br />
							<input type="hidden" name="version" size="10"
								value="<%=props.getProperty("version", "")%>" />
							<center><%=props.getProperty("version", "")%></center>
							</td>
							<td class="borderGrayBottomRight"
								style="border-right: 0px; width: 50px;"><font
								class="subHeading">HC Type:</font><br />
							<input type="hidden" name="hcType" size="12"
								value="<%=props.getProperty("hcType", "")%>" />
							<center><%=props.getProperty("hcType", "")%>&nbsp;</center>
							</td>
						</tr>
					</table>
					<table width="100%">
						<tr>
							<td class="borderGrayBottomRight"><font class="subHeading">Patient's
							Address:</font><br />
							<input type="hidden" style="width: 90%" name="patientAddress"
								value="<%=props.getProperty("patientAddress", "")%>" /> <%=props.getProperty("patientAddress", "")%>
							</td>
							<td class="borderGrayBottomRight" style="width: 100px;"><font
								class="subHeading">City:</font><br />
							<input type="hidden" style="width: 90%" name="patientCity"
								value="<%=props.getProperty("patientCity", "")%>" />
							<center><%=props.getProperty("patientCity", "")%></center>
							</td>
							<td class="borderGrayBottomRight"
								style="width: 130px;"><font
								class="subHeading">Postal Code:</font><br />
							<input type="hidden" style="width: 90%" name="patientPC"
								value="<%=props.getProperty("patientPC", "")%>" /> <%=props.getProperty("patientPC", "")%>
							</td>
                                                        <%  
                                                            String demoChartNo = "";
                                                            if(oscarProps.getProperty("lab_req_include_chartno","false").equals("true")){
                                                                demoChartNo = LocaleUtils.getMessage(request.getLocale(), "oscarEncounter.form.labreq.patientChartNo") + ":" + props.getProperty("patientChartNo", "");
                                                            }
                                                        %>
                                                        <td class="borderGrayBottomRight"
								style="border-right: 0px; width: 130px;"><font
                                                                class="subHeading"><bean:message key="oscarEncounter.form.labreq.patientChartNo"/></font><br />
							<input type="hidden" style="width: 90%" name="patientChartNo"
								value="<%=demoChartNo%>" /> <%=props.getProperty("patientChartNo", "")%>
                                                        </td>
						</tr>
					</table>
					<table width="100%">
						<tr>
							<td class="borderGrayBottomRight" style="width: 80px;"><font
								class="subHeading">Date of Birth:</font><br />
							<input type="hidden" name="birthDate" size="10"
								value="<%=props.getProperty("birthDate", "")%>" />
							<center><%=props.getProperty("birthDate", "")%></center>
							</td>
							<td class="borderGrayBottomRight" style="width: 120px"><font
								class="subHeading">Other Provincial Registration Number:</font><br />
							<input type="text" name="oprn" tabindex="3"
								value="<%=props.getProperty("oprn", "")%>"
								style="font-size: 10px; margin-left: 10px;" /></td>
							<td class="borderGrayBottomRight" style="width: 70px;"><font
								class="subHeading">Sex:</font><br />
							<input type="hidden" name="sex" size="12"
								value="<%=props.getProperty("sex", "")%>" />
							<input type="hidden" name="male" size="12"
								value="<%=props.getProperty("male", "")%>" />
							<input type="hidden" name="female" size="12"
								value="<%=props.getProperty("female", "")%>" />
							<center><%=props.getProperty("sex", "")%></center>
							</td>
							<td class="borderGrayBottomRight"
								style="width: 80px; border-right: 0px;"><font
								class="subHeading">Phone Number:</font><br />
							<input type="hidden" name="phoneNumber" size="12"
								value="<%=props.getProperty("phoneNumber", "")%>" />
							<center><%=props.getProperty("phoneNumber", "")%></center>
							</td>
						</tr>
					</table>
				</tr>
			</table>
			</td>
		</tr>
		<tr>
			<td class="outerTable" colspan="2"><b><i>Note: Separate
			requisitions are required for cytology, histology / pathology and
			tests performed by Public Health Laboratory</i></b></td>
		</tr>
		<tr>
			<td colspan="2">
			<table class="bottomTable" width="100%">
				<tr>
					<td width="40%" class="bottomTableTd" rowspan="2">



					<table class="bottomInnerTable">
						<tr>
							<th class="checkboxTd">X</th>
							<th class="checkboxLabelTd" colspan="2">Biochemistry</th>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_glucose" <%=props.getProperty("b_glucose", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Glucose &nbsp;
							&nbsp; &nbsp; <input type="checkbox" name="b_glucose_random"
								<%=props.getProperty("b_glucose_random", "")%>> Random
							&nbsp; &nbsp; <input type="checkbox" name="b_glucose_fasting"
								<%=props.getProperty("b_glucose_fasting", "")%>> Fasting</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox" name="b_hba1c"
								<%=props.getProperty("b_hba1c", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">HbA1C</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox" name="b_tsh"
								<%=props.getProperty("b_tsh", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">TSH</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_creatinine" <%=props.getProperty("b_creatinine", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Creatinine (eGFR)</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_uricAcid" <%=props.getProperty("b_uricAcid", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Uric Acid</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_sodium" <%=props.getProperty("b_sodium", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Sodium</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_potassium" <%=props.getProperty("b_potassium", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Potassium</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_chloride" <%=props.getProperty("b_chloride", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Chloride</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox" name="b_ck"
								<%=props.getProperty("b_ck", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">CK</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox" name="b_alt"
								<%=props.getProperty("b_alt", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">ALT</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_alkPhosphatase"
								<%=props.getProperty("b_alkPhosphatase", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Alk. Phosphatase</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_bilirubin" <%=props.getProperty("b_bilirubin", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Bilirubin</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_albumin" <%=props.getProperty("b_albumin", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Albumin</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_lipidAssessment"
								<%=props.getProperty("b_lipidAssessment", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Lipid Assessment
							(includes Cholesterol, HDL-C, Triglycerides, calculated LDL-C &
							Chol/HDL-C ratio; individual lipid tests may be ordered in the
							"Other Tests" section of this form)</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_vitaminB12" <%=props.getProperty("b_vitaminB12", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Vitamin B12</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_ferritin" <%=props.getProperty("b_ferritin", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Ferritin</td>
						</tr>
						<tr>
							<td class="checkboxTd"><input type="checkbox"
								name="b_acRatioUrine"
								<%=props.getProperty("b_acRatioUrine", "")%>></td>
							<td class="checkboxLabelTd" colspan="2">Albumin/Creatinine
							Ratio, Urine</td>
						</tr>
						<tr>
							<td class="checkboxTd bottomEndSection"><input
								type="checkbox" name="b_urinalysis"
								<%=props.getProperty("b_urinalysis", "")%>></td>
							<td class="checkboxLabelTd bottomEndSection" colspan="2">Urinalysis
							(Chemical)</td>
						</tr>
						<!-----Neonatal heading -->
						<tr>
							<td class="checkboxTd subSectionHeading"><input
								type="checkbox" name="b_neonatalBilirubin"
								<%=props.getProperty("b_neonatalBilirubin", "")%>></td>
							<td class="checkboxLabelTd subSectionHeading" colspan="2">Neonatal
							Bilirubin:</td>
						</tr>
						<tr>
							<td class="checkboxTd" style="border-bottom: 0px;">&nbsp;</td>
							<td class="checkboxLabelTd" colspan="2">Child's Age: <input
								type="text"
								style="width: 28px; text-align: center; margin-left: 10px;"
								tabindex="4" name="b_childsAgeDays"
								value="<%=props.getProperty("b_childsAgeDays", "")%>">
							days <input type="text"
								style="width: 28px; text-align: center; margin-left: 10px;"
								tabindex="5" name="b_childsAgeHours"
								value="<%=props.getProperty("b_childsAgeHours", "")%>">
							hours</td>
						</tr>
						<tr>
							<td class="checkboxTd" style="border-bottom: 0px;">&nbsp;</td>
							<td class="checkboxLabelTd" colspan="2">Clinician/Practitioner's
							tel. no. <input type="text"
								style="width: 140px; margin-left: 10px;" tabindex="6"
								name="b_cliniciansTelNo"
								value="<%=props.getProperty("b_cliniciansTelNo", "")%>"></td>
						</tr>
						<tr>
							<td class="checkboxTd bottomEndSection">&nbsp;</td>
							<td class="checkboxLabelTd bottomEndSection" colspan="2">Patient's
							24 hr telephone no. <input type="text"
								style="width: 140px; margin-left: 10px;" tabindex="7"
								name="b_patientsTelNo"
								value="<%=props.getProperty("b_patientsTelNo", "")%>"></td>
						</tr>
						<!-----Theraputic Drug Monitoring -->
						<tr>
							<td class="checkboxTd subSectionHeading"><input
								type="checkbox" name="b_therapeuticDrugMonitoring"
								<%=props.getProperty("b_therapeuticDrugMonitoring", "")%>></td>
							<td class="checkboxLabelTd subSectionHeading" colspan="2">Therapeutic
							Drug Monitoring.</td>
						</tr>
						<tr>
							<td class="checkboxTd" style="border-bottom: 0px;">&nbsp;</td>
							<td class="checkboxLabelTd" colspan="2">Name of Drug #1: <input
								type="text" style="width: 160px; margin-left: 10px;"
								tabindex="8" name="b_nameDrug1"
								value="<%=props.getProperty("b_nameDrug1", "")%>"></td>
						</tr>
						<tr>
							<td class="checkboxTd" style="border-bottom: 0px;">&nbsp;</td>
							<td class="checkboxLabelTd" colspan="2">Name of Drug #2: <input
								type="text" style="width: 160px; margin-left: 10px;"
								tabindex="9" name="b_nameDrug2"
								value="<%=props.getProperty("b_nameDrug2", "")%>"></td>
						</tr>
						<tr>
							<td class="checkboxTd" style="border-bottom: 0px;">&nbsp;</td>
							<td class="checkboxLabelTd">Time Collected #1: <input
								type="text"
								style="width: 50px; margin-left: 3px; text-align: center;"
								tabindex="10" name="b_timeCollected1"
								value="<%=props.getProperty("b_timeCollected1", "")%>">hr</td>
							<td class="checkboxLabelTd2">#2:<input type="text"
								style="width: 50px; margin-left: 3px; text-align: center;"
								tabindex="11" name="b_timeCollected2"
								value="<%=props.getProperty("b_timeCollected2", "")%>">hr</td>
						</tr>
						<tr>
							<td class="checkboxTd" style="border-bottom: 0px;">&nbsp;</td>
							<td class="checkboxLabelTd">Time of Last Dose #1:<input
								type="text"
								style="width: 50px; margin-left: 3px; text-align: center;"
								tabindex="12" name="b_timeLastDose1"
								value="<%=props.getProperty("b_timeLastDose1", "")%>">hr</td>
							<td class="checkboxLabelTd2">#2:<input type="text"
								style="width: 50px; margin-left: 3px; text-align: center;"
								tabindex="13" name="b_timeLastDose2"
								value="<%=props.getProperty("b_timeLastDose2", "")%>">hr</td>
						</tr>
						<tr>
							<td class="checkboxTd bottomEndSection">&nbsp;</td>
							<td class="checkboxLabelTd bottomEndSection">Time of Next
							Dose #1:<input type="text"
								style="width: 50px; margin-left: 3px; text-align: center;"
								tabindex="14" name="b_timeNextDose1"
								value="<%=props.getProperty("b_timeNextDose1", "")%>">hr</td>
							<td class="checkboxLabelTd2 bottomEndSection">#2:<input
								type="text"
								style="width: 50px; margin-left: 3px; text-align: center;"
								tabindex="15" name="b_timeNextDose2"
								value="<%=props.getProperty("b_timeNextDose2", "")%>">hr</td>
						</tr>
						<tr>
							<td class="checkboxLabelTd" colspan="3"
								style="border-bottom: 0px;"><b><i>I hereby certify
							the tests ordered are not for registered in or out patients of a
							hospital.</i></b><br />
							<br />
							<br />
							</td>
						</tr>
						<tr>
							<td class="checkboxLabelTd" colspan="2"
								style="border-bottom: 0px;">
							_________________________________</td>
							<td class="checkboxLabelTd" style="border-bottom: 0px;"><input
								type="text" style="width: 70px;" tabindex="16"
								name="b_dateSigned"
								value="<%=props.getProperty("b_dateSigned", UtilDateUtilities.getToday("yyyy-MM-dd"))%>">
							</td>
						</tr>
						<tr>
							<td class="checkboxLabelTd" colspan="2"
								style="border-bottom: 0px;">Clinician/Practitioner
							Signature</td>
							<td class="checkboxLabelTd" style="border-bottom: 0px;">
							Date</td>
						</tr>

					</table>


					</td>
					<td width="50%" class="bottomTableTd">
					<table>
						<tr>
							<td>

							<table class="bottomInnerTable">
								<tr>
									<th class="checkboxTd">X</th>
									<th class="checkboxLabelTd">Hematology</th>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox" name="h_cbc"
										<%=props.getProperty("h_cbc", "")%>></td>
									<td class="checkboxLabelTd">CBC</td>
								</tr>
								<tr>
									<td class="checkboxTd bottomEndSection"><input
										type="checkbox" name="h_prothrombinTime"
										<%=props.getProperty("h_prothrombinTime", "")%>></td>
									<td class="checkboxLabelTd bottomEndSection">Prothrombin
									Time (INR)"</td>
								</tr>

								<tr>
									<th class="checkboxTd">&nbsp;</th>
									<th class="checkboxLabelTd">Immunology</th>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="i_pregnancyTest"
										<%=props.getProperty("i_pregnancyTest", "")%>></td>
									<td class="checkboxLabelTd">Pregnancy Test (Urine)</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="i_mononucleosisScreen"
										<%=props.getProperty("i_mononucleosisScreen", "")%>></td>
									<td class="checkboxLabelTd">Mononucleosis Screen</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="i_rubella" <%=props.getProperty("i_rubella", "")%>></td>
									<td class="checkboxLabelTd">Rubella</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="i_prenatal" <%=props.getProperty("i_prenatal", "")%>></td>
									<td class="checkboxLabelTd">Prenatal: ABO, RhD, Antibody
									Screen (titre and ident. if positive)</td>
								</tr>
								<tr>
									<td class="checkboxTd bottomEndSection"><input
										type="checkbox" name="i_repeatPrenatalAntibodies"
										<%=props.getProperty("i_repeatPrenatalAntibodies", "")%>></td>
									<td class="checkboxLabelTd bottomEndSection">Repeat
									Prenatal Antibodies</td>
								</tr>

								<tr>
									<th class="checkboxTd">&nbsp;</th>
									<th class="checkboxLabelTd">Microbiology ID &
									Sensitivities (if warranted)</th>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_cervical" <%=props.getProperty("m_cervical", "")%>></td>
									<td class="checkboxLabelTd">Cervical</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_vaginal" <%=props.getProperty("m_vaginal", "")%>></td>
									<td class="checkboxLabelTd">Vaginal</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_vaginalRectal"
										<%=props.getProperty("m_vaginalRectal", "")%>></td>
									<td class="checkboxLabelTd">Vaginal / Rectal - Group B
									Strep</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_chlamydia" <%=props.getProperty("m_chlamydia", "")%>></td>
									<td class="checkboxLabelTd">Chlamydia <i>(specify
									source):</i> <input type="text" name="m_chlamydiaSource"
										style="width: 110px;" tabindex="17"
										value="<%=props.getProperty("m_chlamydiaSource", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox" name="m_gc"
										<%=props.getProperty("m_gc", "")%>></td>
									<td class="checkboxLabelTd">GC <i>(specify source):</i> <input
										type="text" name="m_gcSource" style="width: 110px;"
										tabindex="18" value="<%=props.getProperty("m_gcSource", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_sputum" <%=props.getProperty("m_sputum", "")%>></td>
									<td class="checkboxLabelTd">Sputum</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_throat" <%=props.getProperty("m_throat", "")%>></td>
									<td class="checkboxLabelTd">Throat</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_wound" <%=props.getProperty("m_wound", "")%>></td>
									<td class="checkboxLabelTd">Wound <i>(specify source):</i>
									<input type="text" name="m_woundSource" style="width: 100px;"
										tabindex="19"
										value="<%=props.getProperty("m_woundSource", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_urine" <%=props.getProperty("m_urine", "")%>></td>
									<td class="checkboxLabelTd">Urine</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_stoolCulture"
										<%=props.getProperty("m_stoolCulture", "")%>></td>
									<td class="checkboxLabelTd">Stool Culture</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_stoolOvaParasites"
										<%=props.getProperty("m_stoolOvaParasites", "")%>></td>
									<td class="checkboxLabelTd">Stool Ova & Parasites</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_otherSwabsPus"
										<%=props.getProperty("m_otherSwabsPus", "")%>></td>
									<td class="checkboxLabelTd">Other Swabs / Pus <i>(specify
									source):</i> <input type="text" name="m_otherSwabsSource"
										style="width: 100px; margin-left: 10px;" tabindex="20"
										value="<%=props.getProperty("m_otherSwabsSource", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="m_blank" <%=props.getProperty("m_blank", "")%>></td>
									<td class="checkboxLabelTd"><input type="text"
										name="m_blankText" style="width: 93%;" tabindex="21"
										value="<%=props.getProperty("m_blankText", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxTd"><!--<input type="checkbox" name="m_fecalOccultBlood" <%=props.getProperty("m_fecalOccultBlood", "")%>>-->&nbsp;</td>
									<td class="checkboxLabelTd"><!--Fecal Occult Blood-->&nbsp;</td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"
										style="border-bottom: 0px;">Specimen Collection Time</td>
								</tr>
								<tr>
									<td class="checkboxLabelTd bottomEndSection" colspan="2"
										style="text-align: center; border-bottom: 0px;"><input
										type="text" style="width: 60; text-align: center;"
										tabindex="22" name="m_specimenCollectionTime"
										value="<%=props.getProperty("m_specimenCollectionTime", "")%>">
									hr.</td>
								</tr>

							</table>


							</td>
							<td width="50%" class="bottomTableTd">


							<table class="bottomInnerTable">
								<tr>
									<th class="checkboxTd">X</th>
									<th class="checkboxLabelTd">Viral Hepatitis <i>(check
									<b>one</b> only)</i></th>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="v_acuteHepatitis"
										<%=props.getProperty("v_acuteHepatitis", "")%>></td>
									<td class="checkboxLabelTd">Acute Hepatitis</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="v_chronicHepatitis"
										<%=props.getProperty("v_chronicHepatitis", "")%>></td>
									<td class="checkboxLabelTd">Chronic Hepatitis</td>
								</tr>
								<tr>
									<td class="checkboxTd"><input type="checkbox"
										name="v_immuneStatus"
										<%=props.getProperty("v_immuneStatus", "")%>></td>
									<td class="checkboxLabelTd" style="border-bottom: 0px;">Immune
									Status / Previous Exposure</td>
								</tr>
								<tr>
									<td class="checkboxId bottomEndSection">&nbsp;</td>
									<td class="checkboxLabelTd bottomEndSection">
									<table style="font-size: 11px; width: 80%;">
										<tr>
											<td><i>Specify:</i></td>
											<td><input type="checkbox" name="v_immune_HepatitisA"
												<%=props.getProperty("v_immune_HepatitisA", "")%>></td>
											<td>Hepatitis A</td>
										</tr>
										<tr>
											<td>&nbsp;</td>
											<td><input type="checkbox" name="v_immune_HepatitisB"
												<%=props.getProperty("v_immune_HepatitisB", "")%>></td>
											<td>Hepatitis B</td>
										</tr>
										<tr>
											<td>&nbsp;</td>
											<td><input type="checkbox" name="v_immune_HepatitisC"
												<%=props.getProperty("v_immune_HepatitisC", "")%>></td>
											<td>Hepatitis C</td>
										</tr>
										<tr>
											<td colspan="3">or order individual hepatitis tests in
											the "Other Tests" section below</td>
										</tr>
									</table>

									</td>
								</tr>
                                                                
                                                                <tr>                                                                    
                                                                    <th colspan="2" class="checkboxLabelTd">Prostate Specific Antigen (PSA)</th>
                                                                </tr>
                                                                <tr>
                                                                    <td colspan="2" class="checkboxLabelTd bottomEndSection">
                                                                         <span style="float:right; margin-right:5px;">
                                                                            <input type="checkbox" name="psa_free"
										<%=props.getProperty("psa_free", "")%>>Free PSA
                                                                        </span>
                                                                        <input type="checkbox" name="psa_total"
										<%=props.getProperty("psa_total", "")%>>Total PSA                                                                       
                                                                                <p>Specify one below:<br>
                                                                                    
                                                                                    <input type="checkbox" name="psa_uninsured"
                                                                                           <%=props.getProperty("psa_uninsured", "")%>>Screening purposes - Uninsured test<br>
                                                                                    <input type="checkbox" name="psa_insured"
                                                                                           <%=props.getProperty("psa_insured", "")%>>Meets OHIP elibility criteria - Insured test
                                                                                    
                                                                                </p>
                                                                    </td>
                                                                </tr>
                                                                <tr>                                                                    
                                                                    <th colspan="2" class="checkboxLabelTd">Vitamin D (25-Hydroxy)</th>
                                                                </tr>
                                                                <tr>
                                                                    <td colspan="2" class="checkboxLabelTd bottomEndSection">                                                                         
                                                                                    
                                                                                    <input type="checkbox" name="vitd_uninsured"
                                                                                           <%=props.getProperty("vitd_uninsured", "")%>>Uninsured - Patient responsible for payment<br>
                                                                                    <input type="checkbox" name="vitd_insured"
                                                                                           <%=props.getProperty("vitd_insured", "")%>>Insured - Meets OHIP eligibility criteria: osteopenia; osteoporosis; rickets; renal disease; malabsorption syndromes; medications affecting vitamin D metabolism
                                                                                    
                                                                                </p>
                                                                    </td>
                                                                </tr>
								<tr>
									<th class="checkboxTd">&nbsp;</th>
									<th class="checkboxLabelTd">Other Tests - <font
										style="font-weight: normal;">one test per line</font></th>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"><input type="text"
										style="width: 80%" tabindex="23" name="o_otherTests1"
										value="<%=props.getProperty("o_otherTests1", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"><input type="text"
										style="width: 80%" tabindex="24" name="o_otherTests2"
										value="<%=props.getProperty("o_otherTests2", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"><input type="text"
										style="width: 80%" tabindex="25" name="o_otherTests3"
										value="<%=props.getProperty("o_otherTests3", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"><input type="text"
										style="width: 80%" tabindex="26" name="o_otherTests4"
										value="<%=props.getProperty("o_otherTests4", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"><input type="text"
										style="width: 80%" tabindex="27" name="o_otherTests5"
										value="<%=props.getProperty("o_otherTests5", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"><input type="text"
										style="width: 80%" tabindex="28" name="o_otherTests6"
										value="<%=props.getProperty("o_otherTests6", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"><input type="text"
										style="width: 80%" tabindex="29" name="o_otherTests7"
										value="<%=props.getProperty("o_otherTests7", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"><input type="text"
										style="width: 80%" tabindex="30" name="o_otherTests8"
										value="<%=props.getProperty("o_otherTests8", "")%>"></td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"><input type="text"
										style="width: 80%" tabindex="31" name="o_otherTests9"
										value="<%=props.getProperty("o_otherTests9", "")%>"></td>
								</tr>															
								<tr>
									<td class="checkboxLabelTd" colspan="2">&nbsp;</td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2">&nbsp;</td>
								</tr>
								<tr>
									<td style="height: 0px;"></td>
								</tr>
								<tr>
									<td class="checkboxLabelTd" colspan="2"
										style="border-bottom: 0px;">Specimen Collection Date</td>
								</tr>
								<tr>
									<td class="checkboxLabelTd bottomEndSection" colspan="2"
										style="text-align: center; border-bottom: 0px;"><input
										type="text" name="o_specimenCollectionDate"
										style="width: 70; text-align: center;" tabindex="39"
										value="<%=props.getProperty("o_specimenCollectionDate", "")%>"></td>
								</tr>
								<!--<tr>-->
							</table>



							</td>
						</tr>


						<tr>
							<td colspan="2">
							<table class="bottomInnerTable">
								<tr>
									<th class="checkboxLabelTd" colspan="2">Fecal Occult Blood
									Test (FOBT) (check one only)</th>
								</tr>
								<tr>
									<td class="checkboxTd" style="vertical-align: top; width: 30%;"><input
										type="checkbox" name="fobt_nonCCC"
										<%=props.getProperty("fobt_nonCCC", "")%>><span
										class="checkboxLabelTd">&nbsp; FOBT (non CCC)</span></td>
									<td class="checkboxTd"><input type="checkbox"
										name="fobt_CCC" <%=props.getProperty("fobt_CCC", "")%>><span
										class="checkboxLabelTd">&nbsp; ColonCancerCheck FOBT
									(CCC) no other test can be ordered on this form</span></td>
								</tr>
							</table>
							</td>
						</tr>
						<tr>
							<td class="labArea"
								style="vertical-align: top; height: 150px; font-size: 11px; padding-left: 10px;"
								colspan="2"><b><i>Laboratory Use Only</i></b></td>
						</tr>
					</table>
					</td>
				</tr>
			</table>
			</td>
		</tr>
	</table>

	<table class="Head" class="hidePrint">
		<tr>
			<td nowrap="true">
			<% if(!readOnly){ %> <input type="submit" value="Save"
				onclick="javascript:return onSave();" /> <input type="submit"
				value="Save and Exit" onclick="javascript:return onSaveExit();" /> <% } %>
			<input type="submit" value="Exit"
				onclick="javascript:return onExit();" /> <input type="submit"
				value="Print Pdf" onclick="javascript:return onPrintPDF();" /></td>
		</tr>
	</table>

</html:form>
</body>
</html:html>
