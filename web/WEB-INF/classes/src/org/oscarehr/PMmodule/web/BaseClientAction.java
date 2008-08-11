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

package org.oscarehr.PMmodule.web;

import javax.servlet.http.HttpServletRequest;

import org.oscarehr.PMmodule.model.Demographic;
import org.oscarehr.PMmodule.service.AdmissionManager;
import org.oscarehr.PMmodule.service.ClientManager;
import org.oscarehr.casemgmt.service.CaseManagementManager;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.ibm.ws.http.HttpRequest;
import com.quatro.common.KeyConstants;
import com.quatro.service.security.SecurityManager;
import com.quatro.util.Utility;

public abstract class BaseClientAction extends BaseAction {

	protected void setScreenMode(HttpServletRequest request, String currentTab) {

		super.setMenu(request, KeyConstants.MENU_CLIENT);
		SecurityManager sec = super.getSecurityManager(request);
		//summary
		String clientId =request.getParameter("clientId");
	//	Demographic client =(Demographic)request.getAttribute("client");
		if(Utility.IsEmpty(clientId)){
			if(null!=request.getAttribute("clientId")) clientId=request.getAttribute("clientId").toString();
			else clientId=(String)request.getSession(true).getAttribute("casemgmt_DemoNo");
		}
		if(Utility.IsEmpty(clientId)||"0".equals(clientId) ||KeyConstants.FUN_CLIENT.equals(currentTab)){
			request.setAttribute(KeyConstants.TAB_CLIENT_SUMMARY, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_HEALTH, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_DISCHARGE, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_ADMISSION, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_CONSENT, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_HISTORY, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_INTAKE, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_REFER, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_RESTRICTION, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_COMPLAINT, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_CASE, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_ATTCHMENT, KeyConstants.ACCESS_NULL);
			request.setAttribute(KeyConstants.TAB_CLIENT_TASK, KeyConstants.ACCESS_NULL);			
		}
		else
		{
			if (sec.GetAccess(KeyConstants.FUN_CLIENT).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_SUMMARY, KeyConstants.ACCESS_VIEW);
				if (currentTab.equals(KeyConstants.TAB_CLIENT_SUMMARY))request.setAttribute(KeyConstants.TAB_CLIENT_SUMMARY, KeyConstants.ACCESS_CURRENT);
			} else
				request.setAttribute(KeyConstants.TAB_CLIENT_SUMMARY, KeyConstants.ACCESS_NULL);
			
			if (sec.GetAccess(KeyConstants.FUN_CLIENTHEALTHSAFETY).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_HEALTH, KeyConstants.ACCESS_VIEW);
				if (currentTab.equals(KeyConstants.TAB_CLIENT_HEALTH))request.setAttribute(KeyConstants.TAB_CLIENT_SUMMARY, KeyConstants.ACCESS_CURRENT);
			} else
				request.setAttribute(KeyConstants.TAB_CLIENT_HEALTH, KeyConstants.ACCESS_NULL);
			//discharge
			if (sec.GetAccess(KeyConstants.FUN_CLIENTDISCHARGE).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_DISCHARGE, KeyConstants.ACCESS_VIEW);
				if (currentTab.equals(KeyConstants.TAB_CLIENT_DISCHARGE))	request.setAttribute(KeyConstants.TAB_CLIENT_DISCHARGE, KeyConstants.ACCESS_CURRENT);
			}else request.setAttribute(KeyConstants.TAB_CLIENT_DISCHARGE, KeyConstants.ACCESS_NULL);
			//admission
			if (sec.GetAccess(KeyConstants.FUN_CLIENTADMISSION).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_ADMISSION, KeyConstants.ACCESS_VIEW);
				if(currentTab.equals(KeyConstants.TAB_CLIENT_ADMISSION))request.setAttribute(KeyConstants.TAB_CLIENT_ADMISSION, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_ADMISSION, KeyConstants.ACCESS_NULL);
			//consent
			if (sec.GetAccess(KeyConstants.FUN_CLIENTCONSENT).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_CONSENT, KeyConstants.ACCESS_VIEW);
				if(currentTab.equals(KeyConstants.TAB_CLIENT_CONSENT))request.setAttribute(KeyConstants.TAB_CLIENT_CONSENT, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_CONSENT, KeyConstants.ACCESS_NULL);
			//history
			if (sec.GetAccess(KeyConstants.FUN_CLIENTHISTORY).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_HISTORY, KeyConstants.ACCESS_VIEW);
				if (currentTab.equals(KeyConstants.TAB_CLIENT_HISTORY))request.setAttribute(KeyConstants.TAB_CLIENT_HISTORY, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_HISTORY, KeyConstants.ACCESS_NULL);
			//intake
			if (sec.GetAccess(KeyConstants.FUN_CLIENTINTAKE).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_INTAKE, KeyConstants.ACCESS_VIEW);
				if(currentTab.equals(KeyConstants.TAB_CLIENT_INTAKE))request.setAttribute(KeyConstants.TAB_CLIENT_INTAKE, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_INTAKE, KeyConstants.ACCESS_NULL);
			//refer
			if (sec.GetAccess(KeyConstants.FUN_CLIENTREFER).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_REFER, KeyConstants.ACCESS_VIEW);
				if(currentTab.equals(KeyConstants.TAB_CLIENT_REFER))request.setAttribute(KeyConstants.TAB_CLIENT_REFER, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_REFER, KeyConstants.ACCESS_NULL);
			//restriction
			if (sec.GetAccess(KeyConstants.FUN_CLIENTRESTRICTION).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_RESTRICTION, KeyConstants.ACCESS_VIEW);
				if(currentTab.equals(KeyConstants.TAB_CLIENT_RESTRICTION))request.setAttribute(KeyConstants.TAB_CLIENT_RESTRICTION, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_RESTRICTION, KeyConstants.ACCESS_NULL);
			//complaint
			if (sec.GetAccess(KeyConstants.FUN_CLIENTCOMPLAINT).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_COMPLAINT, KeyConstants.ACCESS_VIEW);
				if(currentTab.equals(KeyConstants.TAB_CLIENT_COMPLAINT))request.setAttribute(KeyConstants.TAB_CLIENT_COMPLAINT, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_COMPLAINT, KeyConstants.ACCESS_NULL);
			//case
			if (sec.GetAccess(KeyConstants.FUN_CLIENTCASE).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_CASE, KeyConstants.ACCESS_VIEW);
				if (currentTab.equals(KeyConstants.TAB_CLIENT_CASE))request.setAttribute(KeyConstants.TAB_CLIENT_CASE, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_CASE, KeyConstants.ACCESS_NULL);
			//attachment
			if (sec.GetAccess(KeyConstants.FUN_CLIENTDOCUMENT).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_ATTCHMENT, KeyConstants.ACCESS_VIEW);
				if (currentTab.equals(KeyConstants.TAB_CLIENT_ATTCHMENT))request.setAttribute(KeyConstants.TAB_CLIENT_ATTCHMENT, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_ATTCHMENT, KeyConstants.ACCESS_NULL);
	
			//task
			if (sec.GetAccess(KeyConstants.FUN_CLIENTTASKS).compareTo(KeyConstants.ACCESS_READ) >= 0) {
				request.setAttribute(KeyConstants.TAB_CLIENT_TASK, KeyConstants.ACCESS_VIEW);
				if (currentTab.equals(KeyConstants.TAB_CLIENT_TASK))request.setAttribute(KeyConstants.TAB_CLIENT_TASK, KeyConstants.ACCESS_CURRENT);
			}
			else request.setAttribute(KeyConstants.TAB_CLIENT_TASK, KeyConstants.ACCESS_NULL);
		}
	}
	private String getProgramIdByClient(HttpServletRequest request){
		String cId =request.getParameter("clientId");
		//Demographic client =(Demographic)request.getAttribute("client");
		if(Utility.IsEmpty(cId)){
			 if(null !=request.getAttribute("clientId"))cId=request.getAttribute("clientId").toString();
			 else cId=(String)request.getSession(true).getAttribute("casemgmt_DemoNo");
		}
		String providerNo=(String) request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO);
		String programId="";
		Integer shelterId=(Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
        if(!Utility.IsEmpty(cId)){ 
        	Integer demoInt = Integer.valueOf(cId);
        	programId = this.getClientManager().getRecentProgramId(demoInt, providerNo,shelterId).toString();        		
        }
        return programId;
	}
	public boolean isReadOnly(HttpServletRequest request, String status,String funName,Integer programId){
		boolean readOnly =false;
		if(KeyConstants.STATUS_COMPLETED.equals(status)) readOnly =true;
		else if(KeyConstants.STATUS_INACTIVE.equals(status)){
			if(KeyConstants.FUN_CLIENTINTAKE.equals(funName))readOnly =true;	
		}
		else if(KeyConstants.STATUS_SIGNED.equals(status)) readOnly =true;
		else if(KeyConstants.STATUS_DISCHARGED.equals(status)) readOnly =true;
		else if(KeyConstants.STATUS_EXPIRED.equals(status)) readOnly =true;
		else if(KeyConstants.STATUS_TERMEARLY.equals(status)) readOnly =true;
		
		else if(KeyConstants.STATUS_ACCEPTED.equals(status) || KeyConstants.STATUS_REJECTED.equals(status)){			
		//	if(KeyConstants.FUNCTION_INTAKE.equals(funName))readOnly =true;
		//	else if(KeyConstants.FUNCTION_ADMISSION.equals(funName))readOnly =true;	
			if(KeyConstants.FUN_CLIENTREFER.equals(funName))readOnly =true;	
		}
		
		else if(KeyConstants.STATUS_READONLY.equals(status)) readOnly =true;
		else if(KeyConstants.STATUS_REJECTED.equals(status)) {
			readOnly =true;
			if(KeyConstants.FUN_CLIENTREFER.equals(funName))readOnly =false;
		}
		SecurityManager sec = super.getSecurityManager(request);
		//summary
		if(programId==null) return true;
		
		String orgCd="";		
		if(programId.intValue()!=0) {
			orgCd="P" + programId.toString();
		}
		if (sec.GetAccess(funName, orgCd).compareTo(KeyConstants.ACCESS_READ) <= 0) 
			readOnly=true;
		return readOnly;
	}
	public String getAccess(HttpServletRequest request, String fucName,Integer programId){
		String orgCd="";
		if(programId.intValue()!=0) {
			orgCd="P" + programId.toString();
		}
		SecurityManager sec = super.getSecurityManager(request);
		return sec.GetAccess(fucName, orgCd);
	}
	public CaseManagementManager getCaseManagementManager() {
		return (CaseManagementManager) getAppContext().getBean(
				"caseManagementManager");
	}

	public AdmissionManager getAdmissionManager() {
		return (AdmissionManager) getAppContext().getBean("admissionManager");
	}
	public ClientManager getClientManager() {
		return (ClientManager) getAppContext().getBean("clientManager");
	}


	public ApplicationContext getAppContext() {
	return WebApplicationContextUtils.getWebApplicationContext(getServlet()
			.getServletContext());
	}	
}