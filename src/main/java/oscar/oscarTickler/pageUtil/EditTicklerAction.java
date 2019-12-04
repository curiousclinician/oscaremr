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

package oscar.oscarTickler.pageUtil;

import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.validator.DynaValidatorForm;
import org.oscarehr.casemgmt.model.CaseManagementNote;
import org.oscarehr.casemgmt.service.CaseManagementManager;
import org.oscarehr.common.dao.SecRoleDao;
import org.oscarehr.common.dao.TicklerTextSuggestDao;
import org.oscarehr.common.model.Tickler;
import org.oscarehr.common.model.SecRole;
import org.oscarehr.common.model.TicklerComment;
import org.oscarehr.common.model.TicklerTextSuggest;
import org.oscarehr.common.model.TicklerUpdate;
import org.oscarehr.managers.SecurityInfoManager;
import org.oscarehr.managers.TicklerManager;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import oscar.OscarProperties;
import oscar.oscarEncounter.data.EctProgram;
import oscar.util.DateUtils;

public class EditTicklerAction extends DispatchAction{
    
    private static final Logger logger=MiscUtils.getLogger();
    private TicklerManager ticklerManager = SpringUtils.getBean(TicklerManager.class);
	private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    
    public ActionForward editTickler(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response){
		LoggedInInfo loggedInInfo=LoggedInInfo.getLoggedInInfoFromSession(request);
		
		if (!securityInfoManager.hasPrivilege(loggedInInfo, "_tickler", "u", null)) {
			throw new RuntimeException("missing required security object (_tickler)");
		}

        HttpSession session = request.getSession();
		
		String providerNo=loggedInInfo.getLoggedInProviderNo();
		
        ActionMessages errors = this.getErrors(request);
        DynaValidatorForm editForm = (DynaValidatorForm)form;
        
        String ticklerNoStr = request.getParameter("ticklerNo");
        Integer ticklerNo = Integer.parseInt(ticklerNoStr);
                
        String status = request.getParameter("status"); 
        String priority = request.getParameter("priority");
        String assignedTo = request.getParameter("assignedToProviders");
        String serviceDate = request.getParameter("xml_appointment_date");
        String programStr = request.getParameter("program_assigned_to");
        boolean writeToEncounter = request.getParameter("updateTickler")!=null && request.getParameter("updateTickler").contains("Write to Encounter");

        if (status == null || priority == null || serviceDate == null || assignedTo == null
                || "none".equals(programStr)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("tickler.ticklerEdit.arg.error"));
            saveErrors(request, errors);
            return mapping.findForward("failure");
        }

        Integer programId = null;
        if (programStr != null) {
            programId = Integer.parseInt(programStr);
        }
        Tickler t = ticklerManager.getTickler(loggedInInfo,ticklerNo);
        
        if (t == null) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("tickler.ticklerEdit.arg.error"));
            saveErrors(request,errors);
            return mapping.findForward("failure");
        }
        
        Date now = new Date();
        
        boolean emailFailed = false;
        boolean isComment = false;        

        boolean enabledTicklerEmail = Boolean.parseBoolean(OscarProperties.getInstance().getProperty("tickler_email_enabled"));
        if (enabledTicklerEmail) {
            boolean  emailDemographic = (Boolean) editForm.get("emailDemographic");

            if (emailDemographic) {

               
                try {
                	ticklerManager.sendNotification(loggedInInfo,t);     

                    //add tickler comment noting patient was emailed
                    TicklerComment tc = new TicklerComment();
                    ResourceBundle prop = ResourceBundle.getBundle("oscarResources",request.getLocale());
                    String emailedMsg = "";
                    try {
                        emailedMsg = prop.getString("tickler.ticklerEdit.emailedDemographic");
                    }
                    catch (Exception e) {
                        emailedMsg = "Emailed Demographic";
                    }
                    tc.setMessage(emailedMsg);
                    tc.setTicklerNo(ticklerNo);
                    tc.setUpdateDate(now);
                    tc.setProviderNo(providerNo);

                    t.getComments().add(tc);
                    isComment = true;

                }catch (EmailException e) {
                    logger.error("Tickler Email cannot be sent",e);   
                    emailFailed = true;                           
                }catch (IOException e) {
                    logger.error("Tickler Email Template probably can't be found",e);  
                    emailFailed = true;
                }
                catch (Exception e) {
                    logger.error("Unexpected error. Check your tickler email, and general email configuration",e);  
                    emailFailed = true;
                }                                   
            }
        }
        
        String newMessage = request.getParameter("newMessage");
        
        /*
         * Create a new TicklerComment
         */
        if (newMessage != null && !newMessage.isEmpty()) {

            TicklerComment tc = new TicklerComment();
            tc.setMessage(newMessage);
            tc.setTicklerNo(ticklerNo);
            tc.setUpdateDate(now);
            tc.setProviderNo(providerNo);

            t.getComments().add(tc);
            isComment = true;
        }

        /*
         * Create a new TicklerUpdate                      
         */
        //back fill the original state of the tickler so we don't lose it  
        TicklerUpdate tuOriginal = new TicklerUpdate();

        if (t.getUpdates().isEmpty()) {                                                              
            tuOriginal.setTicklerNo(t.getId());
            tuOriginal.setProviderNo(t.getCreator());
            tuOriginal.setUpdateDate(t.getUpdateDate());

            tuOriginal.setStatus(t.getStatus());
            tuOriginal.setPriority(t.getPriority().toString());
            tuOriginal.setAssignedTo(t.getTaskAssignedTo());                                
            tuOriginal.setServiceDate(t.getServiceDate()); 
            
            t.getUpdates().add(tuOriginal);
        }

        TicklerUpdate tu = new TicklerUpdate();
        tu.setTicklerNo(t.getId());
        tu.setUpdateDate(now);            
        tu.setProviderNo(providerNo);

        boolean isUpdate = false;                        

        if (!status.equals(t.getStatus())){
            tu.setStatusAsChar(status.charAt(0));
            t.setStatusAsChar(status.charAt(0));
            isUpdate = true;            
        }

        if (!priority.equals(t.getPriority())) {
            tu.setPriority(priority);
            t.setPriorityAsString(priority);
            isUpdate = true;
        }


        if (!assignedTo.equals(t.getTaskAssignedTo())){
            tu.setAssignedTo(assignedTo);
            t.setTaskAssignedTo(assignedTo);
            isUpdate = true;
        }
        
        if (programId != null && !programId.equals(t.getProgramId())){
            t.setProgramId(programId);
            isUpdate = true;
        }

        if (!serviceDate.equals(t.getServiceDate())){
           try {
               Date serviceDateAsDate = DateUtils.parseDate(serviceDate, request.getLocale());
               if (serviceDateAsDate != null) {
                   tu.setServiceDate(serviceDateAsDate);
                   t.setServiceDate(serviceDateAsDate);
                   isUpdate = true;
               } else {
                   logger.error("Service date is NULL, it will not be updated.");
               }
           }
           catch (java.text.ParseException e) {
               logger.error("Service Date cannot be parsed:",e);
               return mapping.findForward("error"); 
           }                
        }

        if (isUpdate){            
            t.getUpdates().add(tu);
        }

        if (isComment || isUpdate) {            
            ticklerManager.updateTickler(loggedInInfo,t);
        }

        if (writeToEncounter){
            // Create new note
            CaseManagementManager caseManagementMgr = (CaseManagementManager) SpringUtils.getBean("caseManagementManager");
            CaseManagementNote note = new CaseManagementNote();
            String noteBody = "["+t.getUpdateDate()+" .: Tickler]\n\n";
            noteBody += t.getMessage() + "\n\n";
            for (TicklerComment comment : t.getComments()) {
                noteBody += comment.getMessage() + "\n\n";
            }
            
            Date today = new Date();
            note.setObservation_date(today);
            note.setUpdate_date(today);
            note.setCreate_date(today);
            note.setDemographic_no(String.valueOf(t.getDemographicNo()));
            note.setProvider(loggedInInfo.getLoggedInProvider());
            note.setProviderNo(loggedInInfo.getLoggedInProviderNo());
            note.setSigned(false);
            note.setSigning_provider_no("");
            note.setProgram_no(new EctProgram(session).getProgram(session.getAttribute("user").toString()));
            note.setNote(noteBody);

            SecRoleDao secRoleDao = (SecRoleDao) SpringUtils.getBean("secRoleDao");
            SecRole doctorRole = secRoleDao.findByName("doctor");
            note.setReporter_caisi_role(doctorRole.getId().toString());

            note.setReporter_program_team("0");
            note.setPassword(null);
            note.setLocked(false);
            note.setHistory(noteBody);

            caseManagementMgr.saveNoteSimple(note);
        }
                
        if (emailFailed) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("tickler.ticklerEdit.emailFailed.error"));
            saveErrors(request,errors);
            return mapping.findForward("failure");
        } 
            
        return mapping.findForward("close");
        
    }
    
     public ActionForward updateTextSuggest(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response){
         
		LoggedInInfo loggedInInfo=LoggedInInfo.getLoggedInInfoFromSession(request);
		String providerNo=loggedInInfo.getLoggedInProviderNo();

		ActionMessages errors = this.getErrors(request);
         DynaValidatorForm tsForm = (DynaValidatorForm)form;
         
         String[] activeText = (String[])tsForm.get("activeText");  
         if(activeText == null){
             errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("tickler.ticklerEdit.arg.error"));
             saveErrors(request,errors);
             return mapping.findForward("failure");
         }
         
         String[] inactiveText = (String[])tsForm.get("inactiveText");   
         if(inactiveText == null){
             errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("tickler.ticklerEdit.arg.error"));
             saveErrors(request,errors);
             return mapping.findForward("failure");
         }
         
         TicklerTextSuggestDao ticklerTextSuggestDao = (TicklerTextSuggestDao) SpringUtils.getBean("ticklerTextSuggestDao");
         
         
         for (String activeTextStr : activeText) {
             Integer textSuggestId = null;
             try {
                 textSuggestId = Integer.parseInt(activeTextStr);
             } catch (NumberFormatException e) {
                 //probably a new text suggestion then
             }
             
             TicklerTextSuggest ts = null;
             if (textSuggestId != null) {
                 ts = ticklerTextSuggestDao.find(textSuggestId);           
             }
             if (ts == null) {
                 ts = new TicklerTextSuggest();
                 ts.setActive(true);
                 ts.setCreateDate(new Date());
                 ts.setCreator(providerNo);
                 ts.setSuggestedText(activeTextStr);
                 ticklerTextSuggestDao.persist(ts);
             } else {
                 ts.setActive(true);        
                 ticklerTextSuggestDao.merge(ts);
             }            
         }
         
         
         for (String inactiveTextStr : inactiveText) {
             Integer textSuggestId = null;
             try {
                 textSuggestId = Integer.parseInt(inactiveTextStr);
             } catch (NumberFormatException e) {
                 //probably a new text suggestion then
             }
             
             TicklerTextSuggest ts = null;
             if (textSuggestId != null) {
                 ts = ticklerTextSuggestDao.find(textSuggestId);           
             }
             if (ts == null) {
                 ts = new TicklerTextSuggest();
                 ts.setActive(false);
                 ts.setCreateDate(new Date());
                 ts.setCreator(providerNo);
                 ts.setSuggestedText(inactiveTextStr); 
                 ticklerTextSuggestDao.persist(ts);
             } else {
                 ts.setActive(false);
                 ticklerTextSuggestDao.merge(ts);
             }
             
         }

         return mapping.findForward("close");
     }
}
