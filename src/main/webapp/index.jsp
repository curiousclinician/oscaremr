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

<%@page import="org.oscarehr.common.service.AcceptableUseAgreementManager"%>
<%@page import="oscar.OscarProperties, javax.servlet.http.Cookie, oscar.oscarSecurity.CookieSecurity, oscar.login.UAgentInfo" %>
<%@ page import="java.net.URLEncoder"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/caisi-tag.tld" prefix="caisi" %>
<%@ taglib uri="/WEB-INF/oscar-tag.tld" prefix="oscar" %>
<caisi:isModuleLoad moduleName="ticklerplus"><%
    if(session.getValue("user") != null) {
        response.sendRedirect("provider/providercontrol.jsp");
    }
%></caisi:isModuleLoad><%
OscarProperties props = OscarProperties.getInstance();

// clear old cookies
Cookie prvCookie = new Cookie(CookieSecurity.providerCookie, "");
prvCookie.setPath("/");
response.addCookie(prvCookie);

String econsultUrl = props.getProperty("backendEconsultUrl");

// Initialize browser info variables
String userAgent = request.getHeader("User-Agent");
String httpAccept = request.getHeader("Accept");
UAgentInfo detector = new UAgentInfo(userAgent, httpAccept);

// This parameter exists only if the user clicks the "Full Site" link on a mobile device
if (request.getParameter("full") != null) {
    session.setAttribute("fullSite","true");
}

// If a user is accessing through a smartphone (currently only supports mobile browsers with webkit),
// and if they haven't already clicked to see the full site, then we set a property which is
// used to bring up iPhone-optimized stylesheets, and add or remove functionality in certain pages.
if (detector.detectSmartphone() && detector.detectWebkit()) {
    session.setAttribute("mobileOptimized", "true");
} else {
    session.removeAttribute("mobileOptimized");
}
Boolean isMobileOptimized = session.getAttribute("mobileOptimized") != null;

String hostPath = request.getScheme() + "://" + request.getHeader("Host") +  ":" + request.getLocalPort();
String loginUrl = hostPath + request.getContextPath();

String ssoLoginMessage = "";
if (request.getParameter("email") != null) {
	ssoLoginMessage = "Hello " + request.getParameter("email") + "<br>"
						+ "Please login with your OSCAR credentials to link your accounts.";
}
else if (request.getParameter("errorMessage") != null) {
	ssoLoginMessage = request.getParameter("errorMessage");
}

//Input field styles
String login_error="";

//Gets the request URL
StringBuffer oscarUrl = request.getRequestURL();
//Determines the initial length by subtracting the length of the servlet path from the full url's length
Integer urlLength = oscarUrl.length() - request.getServletPath().length();
//Sets the length of the URL, found by subtracting the length of the servlet path from the length of the full URL, that way it only gets up to the context path
oscarUrl.setLength(urlLength);
%>

<html:html locale="true">
    <head>
    <link rel="shortcut icon" href="images/Oscar.ico" />
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery-1.7.1.min.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/js/global.js"></script>
        <html:base/>
        <% if (isMobileOptimized) { %><meta name="viewport" content="initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, width=device-width"/><% } %>
        <title>
            <% if (props.getProperty("logintitle", "").equals("")) { %>
            <bean:message key="loginApplication.title"/>
            <% } else { %>
            <%= props.getProperty("logintitle", "")%>
            <% } %>
        </title>
        <!--LINK REL="StyleSheet" HREF="web.css" TYPE="text/css"-->
		<link href='https://fonts.googleapis.com/css?family=Roboto:300,400,500,600,700' rel='stylesheet' type='text/css'>

        <script language="JavaScript">
        function showHideItem(id){
            if(document.getElementById(id).style.display == 'none')
                document.getElementById(id).style.display = 'block';
            else
                document.getElementById(id).style.display = 'none';
        }
        
  <!-- hide
  function setfocus() {
    document.loginForm.username.focus();
    document.loginForm.username.select();
  }
  function popupPage(vheight,vwidth,varpage) {
    var page = "" + varpage;
    windowprops = "height="+vheight+",width="+vwidth+",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes";
    var popup=window.open(page, "gpl", windowprops);
  }
  -->
  			function addStartTime() {
            	document.getElementById("oneIdLogin").href += (Math.round(new Date().getTime() / 1000).toString());
			}


			function enhancedOrClassic(choice) {
				document.getElementById("loginType").value = choice;
			}
        </script>
        
        <style type="text/css">
            body { 
               margin: 0;
				font-family: 'Roboto', Helvetica, Arial, sans-serif;
				font-size: 16px;
				color: #333333;
				background-color: #ffffff;
            }
            
            * {
			    -webkit-box-sizing: border-box;
			    -moz-box-sizing: border-box;
			    box-sizing: border-box;
			}
            
            h1 {
                font-size: 38px;
		    	font-weight: 300;
		    }
		    
		    button, input, optgroup, select, textarea {
			    margin: 0;
			    font: inherit;
			    color: inherit;
			}
		    
		    input {
			    line-height: normal;
			}
            
            button, input, select, textarea {
			    font-family: inherit;
			    font-size: inherit;
			    line-height: inherit;
			}
			
			.heading, .loginContainer {
				text-align: center;
			}
			
			.powered {
				margin-right: auto;
				margin-left: auto;
			}
			
			.powered .details {
				text-align: right;
			    margin: 10px 20px 0 0;
			    float: left;
			    width: 35%;
			}
			
            .loginContainer {
            	padding: 30px 15px;
				margin-right: auto;
				margin-left: auto;
            }
            .auaContainer {
				margin-right: auto;
				margin-left: auto;
				text-align:center;
				z-index:3;width:70%;
            }
            
            .panel {
                margin-bottom: 20px;
			    background-color: #fff;
			    border: 1px solid transparent;
			    border-radius: 4px;
			    -webkit-box-shadow: 0 1px 1px rgba(0,0,0,.05);
			    box-shadow: 0 1px 1px rgba(0,0,0,.05);
			}
			
			.panel-body {
			    padding: 10px 40px 40px;
			}
            
            .panel-default {
           		border-color: #ddd;
            }
            
			.form-group {
			    margin-bottom: 15px;
			}
			
			label {
			    display: inline-block;
			    max-width: 100%;
			    margin-bottom: 5px;
			    font-weight: 700;
			}
			
			.form-control {
			    display: block;
			    width: 100%;
			    height: 34px;
			    padding: 6px 12px;
			    font-size: 14px;
			    line-height: 1.42857143;
			    color: #555;
			    background-color: #fff;
			    background-image: none;
			    border: 1px solid #ccc;
			    border-radius: 4px;
			    -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
			    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
			    -webkit-transition: border-color ease-in-out .15s,-webkit-box-shadow ease-in-out .15s;
			    -o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
			    transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
			}
			
			.has-error .form-control {
			    border-color: #a94442;
			    -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
			    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
			}
			
			.btn {
			    display: inline-block;
			    padding: 6px 12px;
			    margin-bottom: 0;
			    font-size: 14px;
			    font-weight: 400;
			    line-height: 1.42857143;
			    text-align: center;
			    white-space: nowrap;
			    vertical-align: middle;
			    -ms-touch-action: manipulation;
			    touch-action: manipulation;
			    cursor: pointer;
			    -webkit-user-select: none;
			    -moz-user-select: none;
			    -ms-user-select: none;
			    user-select: none;
			    background-image: none;
			    border: 1px solid transparent;
			    border-radius: 4px;
			}
			
			.btn-primary {
			    color: #fff;
			    background-color: #53b848;
			    border-color: #3f9336;
			}
			
			.btn-block {
			    display: block;
			    width: 100%;
			}
			
			button, html input[type=button], input[type=reset], input[type=submit] {
			    -webkit-appearance: button;
			    cursor: pointer;
			}
			
			.btn.active.focus, .btn.active:focus, .btn.focus, .btn:active.focus, .btn:active:focus, .btn:focus {
			    outline: thin dotted;
			    outline: 5px auto -webkit-focus-ring-color;
			    outline-offset: -2px;
			}
			
			.btn.focus, .btn:focus, .btn:hover {
			    color: #333;
			    text-decoration: none;
			}
			
			.btn.active, .btn:active {
			    background-image: none;
			    outline: 0;
			    -webkit-box-shadow: inset 0 3px 5px rgba(0,0,0,.125);
			    box-shadow: inset 0 3px 5px rgba(0,0,0,.125);
			}
			
			.btn-primary.focus, .btn-primary:focus {
			    color: #fff;
			    background-color: #3f9336;
			    border-color: #3f9336;
			}
			
			.btn-primary:hover {
			    color: #fff;
			    background-color: #3f9336;
			    border-color: #3f9336;
			}
			
			.btn-primary.active, .btn-primary:active, .open>.dropdown-toggle.btn-primary {
			    color: #fff;
			    background-color: #3f9336;
			    border-color: #3f9336;
			}
			
			.btn-primary.active, .btn-primary:active, .open>.dropdown-toggle.btn-primary {
			    background-image: none;
			}
			
			input[type=button].btn-block, input[type=reset].btn-block, input[type=submit].btn-block {
			    width: 100%;
				margin-bottom: 10px;
			}
			
			.btn.active.focus, .btn.active:focus, .btn.focus, .btn:active.focus, .btn:active:focus, .btn:focus {
			    outline: thin dotted;
			    outline: 5px auto -webkit-focus-ring-color;
			    outline-offset: -2px;
			}
			
			.btn-primary.active.focus, .btn-primary.active:focus, .btn-primary.active:hover, .btn-primary:active.focus, .btn-primary:active:focus, .btn-primary:active:hover, .open>.dropdown-toggle.btn-primary.focus, .open>.dropdown-toggle.btn-primary:focus, .open>.dropdown-toggle.btn-primary:hover {
			    color: #fff;
			    background-color: #3f9336;
			    border-color: #3f9336;
			}
            
            td.topbar{
               background-color: rgb(83, 184, 72);
            }
            td.leftbar{
                background-color:  #6C706E;
                color: white;
            }
            td.leftinput{
                background-color: #f5fffa;
            }
            td#loginText{
                width:200px;
                font-size: small;
                }
            span#buildInfo{
                float: right;
                color:#000000;
                font-size: xx-small;
                text-align: right;
                position: absolute;
                right: 0;
            }
            
			span.extrasmall{
			    font-size: small;
			    float: left;
			    margin: 10px 0 20px;
			}
            #mobileMsg { display: none; }
            
			.topbar {
				background-color: #53B848;
				color: #ffffff;
				text-align: center;
			}

			.clinic-text {
				display: inline;
				font-weight: 400;
			}
			
            @media (min-width: 768px) {
				.loginContainer, .powered {
					width: 450px;
				}
			}
			
			@media (min-width: 992px) {
				
			}
			
			@media (min-width: 1200px) {
				
			}
			
			.oneIdLogin {
				background-color: #000;
				width: 60%;
				height: 34px;
				margin: 0px auto;
			}
			
			.oneIdLogo {
				background-color: transparent;
				background: url("./images/oneId/oneIDLogo.png");
				border: none;
				display: inline-block;
				float: left;
      			vertical-align: bottom;
      			width: 70px;
      			height: 16px;
			}
			
			.oneIDText {
				display: inline-block;
				float: left;
				padding-left: 10px
			}
        </style>
        <% if (isMobileOptimized) { %>
        <!-- Small adjustments are made to the mobile stylesheet -->
        <style type="text/css">
            html { -webkit-text-size-adjust: none; }
            td.topbar{ width: 75%; }
            td.leftbar{ width: 25%; }
            span.extrasmall{ font-size: small; }
            #browserInfo, #logoImg, #buildInfo { display: none; }
            #mobileMsg { display: inline; }
        </style>
        <% } %>
    </head>
    
    <body onLoad="setfocus()" bgcolor="#ffffff">
        <div class="topbar">
			<% String headerText = OscarProperties.getInstance().getProperty("login_screen_header_text"); 
			if (headerText != null && !headerText.isEmpty()) { %>
				<h3 class="clinic-text"><%=headerText%></h3>
			<% } %>
            <span id="buildInfo">Build: <%=OscarProperties.getBuildTag()%> </span>
        </div>
        
        <div class="heading">
        	<img src="images/Logo.png" border="0" style="margin: 25px auto;">
        </div>
        <div class="loginContainer">
	        <div class="panel panel-default">
	        	<h1>OSCAR EMR Login</h1>
	        	
	        	<h4><%=ssoLoginMessage%></h4>
	        	<%String key = "loginApplication.formLabel" ;
                    if(request.getParameter("login")!=null && request.getParameter("login").equals("failed") ){
                    key = "loginApplication.formFailedLabel" ;
                    login_error="has-error";
                    }
                    %>

    			  	<div class="panel-body">
    			    	<div class="leftinput" border="0" width="100%" ng-app="indexApp" ng-controller="indexCtrl"> <!-- id="loginText" -->
    				    	<html:form action="login" >
    							<div class="form-group <%=login_error%>"> 
    	                        	<input type="text" name="username" placeholder="Enter your username" value="" size="15" maxlength="15" autocomplete="off" class="form-control" ng-model="username"/> <%-- class="<%=login_input_style %>" --%>
    	                        </div>
    	                        
    	                        <div class="form-group <%=login_error%>">               
    	                        	<input type="password" name="password" placeholder="Enter your password" value="" size="15" maxlength="32" autocomplete="off" class="form-control" ng-model="password"/>
    	                        </div>
    	                        
    	                        <div class="form-group <%=login_error%>">
    	                        	<input type="password" name="pin" placeholder="Enter your PIN" value="" size="15" maxlength="15" autocomplete="off" class="form-control" ng-model="pin"/>
    	                        	<span class="extrasmall">
    		                            <bean:message key="loginApplication.formCmt"/>
    		                        </span>
    	                        </div>
    	                        <input type="hidden" id="oneIdKey" name="nameId" value="<%=request.getParameter("nameId") != null ? request.getParameter("nameId") : ""%>"/>
    	                        <input type="hidden" id="email" name="email" value="<%=request.getParameter("email") != null ? request.getParameter("email") : ""%>"/>
								<input type="hidden" id="loginType" name="loginType" value=""/>

    	                        <input type=hidden name='propname' value='<bean:message key="loginApplication.propertyFile"/>' />
								<div id="buttonContainer">
									<% if (!Boolean.parseBoolean(OscarProperties.getInstance().getProperty("hide_oscar_classic"))) { %>
									<input class="btn btn-oscar btn-block" name="submit" type="submit" onclick="enhancedOrClassic('C');" value="<bean:message key="index.OSCARClassic"/>" />
									<% } %>
									<input class="btn btn-primary btn-block" name="submit" type="submit" onclick="enhancedOrClassic('E');" value="<bean:message key="index.KaiEnhanced"/>" />
								</div>
    	                        <% if (detector.detectSmartphone() && detector.detectWebkit()) {
    	                        	session.setAttribute("fullSite","true"); %>
    	                        	<input class="btn btn-primary btn-block" name="submit" type="submit" value="<bean:message key="index.btnSignIn"/> using <bean:message key="loginApplication.fullSite"/>" />
    	                        <% } %>
    						</html:form>
							<oscar:oscarPropertiesCheck property="enable_econsult" value="true" defaultVal="false">
    							<a href="<%=econsultUrl %>/SAML2/login?oscarReturnURL=<%=URLEncoder.encode(oscarUrl + "/ssoLogin.do", "UTF-8") + "?loginStart="%>" id="oneIdLogin" onclick="addStartTime()"><div class="btn btn-primary btn-block oneIDLogin"><span class="oneIDLogo"></span><span class="oneIdText">ONE ID Login</span></div></a>
							</oscar:oscarPropertiesCheck>
    			                        
                        <%if (AcceptableUseAgreementManager.hasAUA() && !AcceptableUseAgreementManager.auaAlwaysShow()){ %>
                        <span class="extrasmall">
                        	<bean:message key="global.aua" /> &nbsp; <a href="javascript:void(0);" onclick="showHideItem('auaText');"><bean:message key="global.showhide"/></a>
                        </span>
                        <%} %>       
			        </div>
			  	</div>
			</div>
		</div>
		<%if (AcceptableUseAgreementManager.hasAUA() || AcceptableUseAgreementManager.auaAlwaysShow()){ %>
			<div id="auaText" class="auaContainer" style="display:none;" >
				<div class="panel panel-default">
					<%=AcceptableUseAgreementManager.getAUAText()%>
				</div>
			</div>
		<%}
		if (AcceptableUseAgreementManager.auaAlwaysShow()) { %>
			<script type="text/javascript">document.getElementById('auaText').style.display = 'block';</script>
		<% } %>
		<div class="powered">
			<span class="details">
				<div>Powered</div>
				<div>by</div>
			</span>
			<img alt="KAI Innovations" src="images/logo/KAI_LOGO.png">
		</div>        
    </body>
</html:html>
