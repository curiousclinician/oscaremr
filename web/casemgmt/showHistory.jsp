<!--/*
 * 
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved. *
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
 * This software was written for the 
 * Department of Family Medicine 
 * McMaster Unviersity 
 * Hamilton 
 * Ontario, Canada 
 */
 -->
 <%@ include file="/casemgmt/taglibs.jsp" %>
 
 <html>
     <head>
	     <link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon" />
         <title>Note History</title>
     </head>    
     <body>
             <h3 style="text-align:center;">Note Revision History for</h3>
             <h3 style="text-align:center;"><nested:write name="demoName"/></h3>
             <nested:iterate id="note" name="history">
                 <div style="width:99%; background-color:#EFEFEF; font-size:12px; border-left: thin groove #000000; border-bottom: thin groove #000000; border-right: thin groove #000000;">
                     <pre><nested:write name="note" property="note" /></pre>
                     <div style="color:#0000FF;">
                         Documentation Date: <nested:write name="note" property="observation_date" format="dd-MMM-yyyy H:mm" /><br>                         
                         <nested:equal name="note" property="signed" value="true"> 
                             Signed by 
                             <nested:write name="note" property="signing_provider_no"/>:
                         </nested:equal>
                         <nested:notEqual name="note" property="signed" value="true"> 
                             Saved by 
                             <nested:write name="note" property="provider.formattedName" />:
                         </nested:notEqual>           
                         <nested:write name="note" property="update_date" format="dd-MMM-yyyy H:mm" />
                     </div>
                 </div>
             </nested:iterate>                            
     </body>     
 </html>