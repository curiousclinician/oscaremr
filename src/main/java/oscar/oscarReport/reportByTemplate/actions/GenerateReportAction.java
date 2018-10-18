/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. 
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
//This action generates the report after the user filled in all the params



package oscar.oscarReport.reportByTemplate.actions;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.oscarehr.util.SpringUtils;

import com.quatro.dao.security.SecobjprivilegeDao;
import com.quatro.model.security.Secobjprivilege;

import oscar.oscarReport.reportByTemplate.ReportFactory;
import oscar.oscarReport.reportByTemplate.Reporter;

/**
 * Created on December 21, 2006, 10:47 AM
 * @author apavel (Paul)
 */
public class GenerateReportAction extends Action {
	
	static SecobjprivilegeDao secobjprivilegeDao = SpringUtils.getBean(SecobjprivilegeDao.class);
	
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {
       
    	String roleName$ = (String)request.getSession().getAttribute("userrole") + "," + (String) request.getSession().getAttribute("user");
    	if(!com.quatro.service.security.SecurityManager.hasPrivilege("_admin", roleName$)  && !com.quatro.service.security.SecurityManager.hasPrivilege("_report", roleName$)) {
    		throw new SecurityException("Insufficient Privileges");
    	}
    	
    	////
    	String templateId = request.getParameter("templateId");	
    	checkSecurity(templateId,roleName$);
    	
        Reporter reporter = ReportFactory.getReporter(request.getParameter("type"));
        
        if( reporter.generateReport(request)) {
            return mapping.findForward("success");
        }
                
        return mapping.findForward("fail");
        
        
    }
    
    
    public static void checkSecurity(String templateId, String roles) {
    	//does the user have access to run this report?
    	String securityObjectName = "_rbt.execute$" + templateId;
    	List<Secobjprivilege> entries = secobjprivilegeDao.getByObjectNameAndRoles(securityObjectName,Arrays.asList(roles.split("\\s*,\\s*")));
    	
    	if(!entries.isEmpty()) {
    		boolean deny=false;
        	for(Secobjprivilege entry: entries) {
        		if(entry.getPrivilege_code().indexOf("r") != -1 || entry.getPrivilege_code().indexOf("w") != -1 || entry.getPrivilege_code().indexOf("x") != -1) {
        			deny=false;
        			return;
        		}
        		
        		if(entry.getPrivilege_code().equals("o")) {
        			deny=true;
        			continue;
        		}
        	}
        	
        	if(deny) {
        		throw new SecurityException("Insufficient Privileges");
        	}
    	}

    }
    
}
