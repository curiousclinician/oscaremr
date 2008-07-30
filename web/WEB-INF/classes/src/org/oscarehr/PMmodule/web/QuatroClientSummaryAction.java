package org.oscarehr.PMmodule.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.oscarehr.PMmodule.model.QuatroIntake;
import org.oscarehr.PMmodule.model.Admission;
import org.oscarehr.PMmodule.model.Demographic;
import org.oscarehr.PMmodule.model.HealthSafety;
import org.oscarehr.PMmodule.model.Provider;
import org.oscarehr.PMmodule.model.QuatroIntakeHeader;
import org.oscarehr.PMmodule.model.Room;
import org.oscarehr.PMmodule.model.Bed;
import org.oscarehr.PMmodule.model.RoomDemographic;
import org.oscarehr.PMmodule.model.ClientCurrentProgram;
import org.oscarehr.PMmodule.service.AdmissionManager;
import org.oscarehr.PMmodule.service.ClientManager;
import org.oscarehr.PMmodule.service.HealthSafetyManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.PMmodule.service.RoomDemographicManager;
import org.oscarehr.PMmodule.service.RoomManager;
import org.oscarehr.PMmodule.service.BedManager;
import org.oscarehr.PMmodule.service.LogManager;

import com.quatro.common.KeyConstants;
import com.quatro.service.IntakeManager;

import org.oscarehr.PMmodule.web.admin.BedManagerForm;
import org.oscarehr.PMmodule.web.formbean.QuatroClientSummaryForm;

public class QuatroClientSummaryAction extends BaseClientAction {

   private HealthSafetyManager healthSafetyManager;
   private ClientManager clientManager;
   private ProgramManager programManager;
   private AdmissionManager admissionManager;
   private RoomDemographicManager roomDemographicManager;
   private RoomManager roomManager;
   private BedManager bedManager;
   private IntakeManager intakeManager;
   private LogManager logManager;
   public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       return edit(mapping, form, request, response);
   }

   public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       super.setScreenMode(request, KeyConstants.TAB_CLIENT_SUMMARY);
       HashMap actionParam = (HashMap) request.getAttribute("actionParam");
       if(actionParam==null){
    	  actionParam = new HashMap();
          actionParam.put("clientId", request.getParameter("clientId")); 
//        don't delete sample code below for html:link parameter       
//        actionParam.put("id", "200492"); 
//        actionParam.put("floatProperty", new Float(444.0)); 
//        actionParam.put("intProperty", new Integer(555)); 
//        actionParam.put("stringArray", new String[] { "Value 1", "Value 2", "Value 3" }); 
       }

       String demographicNo= (String)actionParam.get("clientId");

       request.getSession().setAttribute(KeyConstants.SESSION_KEY_CURRENT_MODULE, KeyConstants.MODULE_ID_CLIENT);
       
//       setEditAttributes(form, request, demographicNo);
       Integer shelterId=(Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
       
       request.setAttribute("clientId", demographicNo);
       request.setAttribute("client", clientManager.getClientByDemographicNo(demographicNo));

       String providerNo = ((Provider) request.getSession().getAttribute("provider")).getProviderNo();
       
       List lst = intakeManager.getActiveQuatroIntakeHeaderListByFacility(Integer.valueOf(demographicNo), shelterId, providerNo);
       QuatroIntakeHeader obj0 = null;
       Integer programId=null;
       if(lst.size()>0) {
    	   obj0=  (QuatroIntakeHeader)lst.get(0);
    	   programId=obj0.getProgramId();
       }
      
       boolean readOnly= super.isReadOnly(request, "", KeyConstants.FUN_CLIENTHEALTHSAFETY, programId);
       request.setAttribute("isReadOnly",Boolean.valueOf(readOnly));
       
       for(int i=0;i<lst.size();i++){
    	 QuatroIntakeHeader obj = (QuatroIntakeHeader)lst.get(i);
    	 if(obj.getProgramType().equals(KeyConstants.BED_PROGRAM_TYPE)){
    		List lst2 = intakeManager.getClientFamilyByIntakeId(obj.getId().toString());
   		    request.setAttribute("family", lst2);
   		    break;
    	 }
       }
       
       // only allow bed/service programs show up.(not external program)
       List currentAdmissionList = admissionManager.getCurrentAdmissions(Integer.valueOf(demographicNo),providerNo, shelterId);
       List bedServiceList = new ArrayList();
       for (int i=0;i<currentAdmissionList.size();i++) {
          Admission admission1 = (Admission) currentAdmissionList.get(i);
          ClientCurrentProgram ccp = new ClientCurrentProgram();
          ccp.setProgramId(admission1.getProgramId());
          ccp.setProgramName(admission1.getProgramName());
          ccp.setProgramType(admission1.getProgramType());
          ccp.setAdmissionDate(admission1.getAdmissionDate());
          ccp.setDaysInProgram(admission1.getDaysInProgram());
          bedServiceList.add(ccp);
       }
       List currentServiceIntakeList = intakeManager.getActiveServiceIntakeHeaderListByFacility(Integer.valueOf(demographicNo), shelterId, providerNo);
       Date today = new Date();
       for (int i=0;i<currentServiceIntakeList.size();i++) {
         QuatroIntakeHeader qih1 = (QuatroIntakeHeader) currentServiceIntakeList.get(i);
         ClientCurrentProgram ccp = new ClientCurrentProgram();
         ccp.setProgramId(qih1.getProgramId());
         ccp.setProgramName(qih1.getProgramName());
         ccp.setProgramType(qih1.getProgramType());
         ccp.setAdmissionDate(qih1.getCreatedOn());
   	     long diff = today.getTime() - qih1.getCreatedOn().getTime().getTime();
    	 diff = diff / 1000; // seconds
         diff = diff / 60; // minutes
         diff = diff / 60; // hours
         diff = diff / 24; // days
         ccp.setDaysInProgram(new Integer((int)diff));
         bedServiceList.add(ccp);
       }

       request.setAttribute("clientCurrentPrograms", bedServiceList);

       HealthSafety healthsafety = healthSafetyManager.getHealthSafetyByDemographic(Integer.valueOf(demographicNo));
       request.setAttribute("healthsafety", healthsafety);

       request.setAttribute("referrals", clientManager.getActiveManualReferrals(demographicNo,providerNo, shelterId));
           
       // bed reservation view
       if(currentAdmissionList.size()>0){
         RoomDemographic roomDemographic = roomDemographicManager.getRoomDemographicByDemographic(Integer.valueOf(demographicNo));
		 request.setAttribute("roomDemographic", roomDemographic);
       }
       
       request.setAttribute("actionParam", actionParam);

       return mapping.findForward("edit");
   }
/*
   private void setEditAttributes(ActionForm form, HttpServletRequest request, String demographicNo) {
       
       Integer shelterId=(Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
       
       request.setAttribute("clientId", demographicNo);
       request.setAttribute("client", clientManager.getClientByDemographicNo(demographicNo));

       String providerNo = ((Provider) request.getSession().getAttribute("provider")).getProviderNo();
       
       List lst = intakeManager.getActiveQuatroIntakeHeaderListByFacility(Integer.valueOf(demographicNo), shelterId, providerNo);
       QuatroIntakeHeader obj0 = null;
       Integer programId=null;
       if(lst.size()>0) {
    	   obj0=  (QuatroIntakeHeader)lst.get(0);
    	   programId=obj0.getProgramId();
       }
      
       boolean readOnly= super.isReadOnly(request, "", KeyConstants.FUN_PMM_CLIENTHEALTHSAFETY, programId);
       request.setAttribute("isReadOnly",Boolean.valueOf(readOnly));
       
       for(int i=0;i<lst.size();i++){
    	 QuatroIntakeHeader obj = (QuatroIntakeHeader)lst.get(i);
    	 if(obj.getProgramType().equals(KeyConstants.BED_PROGRAM_TYPE)){
    		List lst2 = intakeManager.getClientFamilyByIntakeId(obj.getId().toString());
   		    request.setAttribute("family", lst2);
   		    break;
    	 }
       }
       
       // only allow bed/service programs show up.(not external program)
       List currentAdmissionList = admissionManager.getCurrentAdmissions(Integer.valueOf(demographicNo),providerNo, shelterId);
       List bedServiceList = new ArrayList();
       for (int i=0;i<currentAdmissionList.size();i++) {
          Admission admission1 = (Admission) currentAdmissionList.get(i);
          ClientCurrentProgram ccp = new ClientCurrentProgram();
          ccp.setProgramId(admission1.getProgramId());
          ccp.setProgramName(admission1.getProgramName());
          ccp.setProgramType(admission1.getProgramType());
          ccp.setAdmissionDate(admission1.getAdmissionDate());
          ccp.setDaysInProgram(admission1.getDaysInProgram());
          bedServiceList.add(ccp);
       }
       List currentServiceIntakeList = intakeManager.getActiveServiceIntakeHeaderListByFacility(Integer.valueOf(demographicNo), shelterId, providerNo);
       Date today = new Date();
       for (int i=0;i<currentServiceIntakeList.size();i++) {
         QuatroIntakeHeader qih1 = (QuatroIntakeHeader) currentServiceIntakeList.get(i);
         ClientCurrentProgram ccp = new ClientCurrentProgram();
         ccp.setProgramId(qih1.getProgramId());
         ccp.setProgramName(qih1.getProgramName());
         ccp.setProgramType(qih1.getProgramType());
         ccp.setAdmissionDate(qih1.getCreatedOn());
   	     long diff = today.getTime() - qih1.getCreatedOn().getTime().getTime();
    	 diff = diff / 1000; // seconds
         diff = diff / 60; // minutes
         diff = diff / 60; // hours
         diff = diff / 24; // days
         ccp.setDaysInProgram(new Integer((int)diff));
         bedServiceList.add(ccp);
       }

       request.setAttribute("clientCurrentPrograms", bedServiceList);

       HealthSafety healthsafety = healthSafetyManager.getHealthSafetyByDemographic(Integer.valueOf(demographicNo));
       request.setAttribute("healthsafety", healthsafety);

       request.setAttribute("referrals", clientManager.getActiveReferrals(demographicNo,providerNo, shelterId));
           
       // bed reservation view
       if(currentAdmissionList.size()>0){
         RoomDemographic roomDemographic = roomDemographicManager.getRoomDemographicByDemographic(Integer.valueOf(demographicNo));
		 request.setAttribute("roomDemographic", roomDemographic);
       }
   }
*/
   
   public void setHealthSafetyManager(HealthSafetyManager healthSafetyManager) {
	 this.healthSafetyManager = healthSafetyManager;
   }


   public void setAdmissionManager(AdmissionManager admissionManager) {
	 this.admissionManager = admissionManager;
   }

   public void setClientManager(ClientManager clientManager) {
	 this.clientManager = clientManager;
   }

   public void setProgramManager(ProgramManager programManager) {
	 this.programManager = programManager;
   }

   public void setRoomDemographicManager(RoomDemographicManager roomDemographicManager) {
	 this.roomDemographicManager = roomDemographicManager;
   }

   public void setRoomManager(RoomManager roomManager) {
 	 this.roomManager = roomManager;
   }

   public void setBedManager(BedManager bedManager) {
	 	 this.bedManager = bedManager;
	   }
   public void setIntakeManager(IntakeManager intakeManager) {
	 this.intakeManager = intakeManager;
   }

   public void setLogManager(LogManager logManager) {
	 this.logManager = logManager;
   }

}
