package org.oscarehr.PMmodule.web;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.oscarehr.PMmodule.model.Admission;
import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.PMmodule.service.AdmissionManager;
import org.oscarehr.PMmodule.service.BedDemographicManager;
import org.oscarehr.PMmodule.service.BedManager;
import org.oscarehr.PMmodule.service.ClientManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.PMmodule.service.ProviderManager;
import org.oscarehr.PMmodule.service.RoomDemographicManager;
import org.oscarehr.PMmodule.service.RoomManager;
import org.oscarehr.PMmodule.web.formbean.ClientManagerFormBean;
import org.oscarehr.casemgmt.service.CaseManagementManager;

import com.quatro.common.KeyConstants;
import com.quatro.service.IntakeManager;
import com.quatro.service.LookupManager;

public class QuatroClientDischargeAction  extends BaseClientAction {
   private ClientManager clientManager;
   private ProviderManager providerManager;
   private ProgramManager programManager;
   private AdmissionManager admissionManager;
   private CaseManagementManager caseManagementManager;
   private BedDemographicManager bedDemographicManager;
   private RoomDemographicManager roomDemographicManager;
   private RoomManager roomManager;
   private BedManager bedManager;
   private LookupManager lookupManager;
   private IntakeManager intakeManager;
   


   public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       
       return list(mapping, form, request, response);
   }
   
   public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       setEditAttributes(form, request);
       
       super.setScreenMode(request, KeyConstants.TAB_CLIENT_DISCHARGE);
       return mapping.findForward("edit");
   }
   public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
	   DynaActionForm clientForm = (DynaActionForm) form;
	   Integer shelterId=(Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
       String providerNo = (String) request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO);
	   ActionMessages messages = new ActionMessages();
	   boolean isError = false;
	   boolean isWarning = false;
	   Admission admObj =(Admission)clientForm.get("admission");
	   admObj.setAdmissionStatus(KeyConstants.INTAKE_STATUS_DISCHARGED);
	   admObj.setDischargeDate(Calendar.getInstance());
	   admObj.setLastUpdateDate(new GregorianCalendar());
	   admObj.setProviderNo(providerNo);
	   boolean isReferral=false;
	   if(null!=admObj.getBedProgramId() && admObj.getBedProgramId().intValue()>0) {
		   isReferral =true;
		   admObj.setCommunityProgramCode(admObj.getBedProgramId().toString());
	   }

	   List lstFamily = intakeManager.getClientFamilyByIntakeId(admObj.getIntakeId().toString());
	   admissionManager.dischargeAdmission(admObj, isReferral, lstFamily);
/*	   
	   if(lstFamily!=null){
		   admissionManager.updateDischargeInfo(admObj, isReferral);
		   Iterator item = lstFamily.iterator();
			while(item.hasNext()){
				QuatroIntakeFamily qifTmp = (QuatroIntakeFamily)item.next();
				Admission admLoc =admissionManager.getAdmissionByIntakeId(qifTmp.getIntakeId());
				if(admLoc.getId().intValue()!=admObj.getId().intValue()){ 
					admLoc.setAdmissionStatus(KeyConstants.INTAKE_STATUS_DISCHARGED);
					admLoc.setDischargeDate(Calendar.getInstance());
				  	admLoc.setDischargeNotes(admObj.getDischargeNotes());	
					admLoc.setCommunityProgramCode(admObj.getCommunityProgramCode());
				  	admissionManager.updateDischargeInfo(admLoc, false);
				}
			}
			
	   }else{
		   admissionManager.updateDischargeInfo(admObj, isReferral);		  
	   }
*/
	   
       super.setScreenMode(request, KeyConstants.TAB_CLIENT_DISCHARGE);
	  	HashMap actionParam = (HashMap) request.getAttribute("actionParam");
       if(actionParam==null){
    	  actionParam = new HashMap();
          actionParam.put("clientId", request.getParameter("clientId")); 
       }
       request.setAttribute("actionParam", actionParam);

	   
	   if(!(isWarning || isError)) messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("message.save.success", request.getContextPath()));
       saveMessages(request,messages);
       
       /* discharge */      
      
       List lstCommProgram =lookupManager.LoadCodeList("CMP", true, null, null);
       request.setAttribute("lstCommProgram", lstCommProgram);
       List lstDischargeReason =lookupManager.LoadCodeList("DRN", true, null, null);
       request.setAttribute("lstDischargeReason", lstDischargeReason);
       List lstTransType =lookupManager.LoadCodeList("TPT", true, null, null);
       request.setAttribute("lstTransType", lstTransType);     
       List  lstBed=programManager.getBedPrograms(providerNo, shelterId);
       request.setAttribute("lstBedProgram",lstBed);
       request.setAttribute("admission", admObj);
       request.setAttribute("admissionId", admObj.getId());
       
       request.setAttribute("clientId", admObj.getClientId());
       request.setAttribute("client", clientManager.getClientByDemographicNo(admObj.getClientId().toString()));

       return mapping.findForward("edit");
//       return edit(mapping, form, request, response);
   }
   public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       setListAttributes(form, request);
       super.setScreenMode(request, KeyConstants.TAB_CLIENT_DISCHARGE);
       return mapping.findForward("list");
   }
   private void setEditAttributes(ActionForm form, HttpServletRequest request) {
       DynaActionForm clientForm = (DynaActionForm) form;

       HashMap actionParam = (HashMap) request.getAttribute("actionParam");
       Integer aId = new Integer(request.getParameter("admissionId"));
       
       if(actionParam==null){
    	  actionParam = new HashMap();
          actionParam.put("clientId", request.getParameter("clientId")); 
       }
       request.setAttribute("actionParam", actionParam);
       String demographicNo= (String)actionParam.get("clientId");
       
       ClientManagerFormBean tabBean = (ClientManagerFormBean) clientForm.get("view");

       Integer shelterId=(Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
       String providerNo=(String) request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO);
       request.setAttribute("clientId", demographicNo);
       request.setAttribute("client", clientManager.getClientByDemographicNo(demographicNo));
       Admission admsObj =admissionManager.getAdmission(aId);
      
       clientForm.set("admission", admsObj);       
       /* discharge */
       List lstCommProgram =lookupManager.LoadCodeList("CMP", true, null, null);
       request.setAttribute("lstCommProgram", lstCommProgram);
       List lstDischargeReason =lookupManager.LoadCodeList("DRN", true, null, null);
       request.setAttribute("lstDischargeReason", lstDischargeReason);
       List lstTransType =lookupManager.LoadCodeList("TPT", true, null, null);
       request.setAttribute("lstTransType", lstTransType);     
       List  lstBed=programManager.getBedPrograms(providerNo,shelterId);
       request.setAttribute("lstBedProgram",lstBed);
       request.setAttribute("admission", admsObj);
       request.setAttribute("admissionId", admsObj.getId());
   }

   private void setListAttributes(ActionForm form, HttpServletRequest request) {
	   DynaActionForm clientForm = (DynaActionForm) form;

       HashMap actionParam = (HashMap) request.getAttribute("actionParam");
       if(actionParam==null){
    	  actionParam = new HashMap();
          actionParam.put("clientId", request.getParameter("clientId")); 
       }
       request.setAttribute("actionParam", actionParam);
       String demographicNo= (String)actionParam.get("clientId");
       
       Integer shelterId=(Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
       
       request.setAttribute("clientId", demographicNo);
       request.setAttribute("client", clientManager.getClientByDemographicNo(demographicNo));

       String providerNo = (String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO);
           
       List lstDischarge = admissionManager.getAdmissions(Integer.valueOf(demographicNo), providerNo,shelterId);
       request.setAttribute("quatroDischarge", lstDischarge);
       for(int i=0;i<lstDischarge.size();i++){
    	 Admission admission = (Admission)lstDischarge.get(i);
	     admission.setFamilyMember(false);	 
    	 if(admission.getAdmissionStatus().equals(KeyConstants.INTAKE_STATUS_ADMITTED)){
    	   Integer intakeFamilyHeadId = intakeManager.getIntakeFamilyHeadId(admission.getIntakeId().toString());
    	   if(intakeFamilyHeadId.intValue()!=0){  //family
    		 if(!intakeFamilyHeadId.equals(admission.getIntakeId())) admission.setFamilyMember(true);
    	   }
    	 }
       }
   }
   
   public void setIntakeManager(IntakeManager intakeManager){
	   this.intakeManager =intakeManager;
   }
   public void setAdmissionManager(AdmissionManager admissionManager) {
	 this.admissionManager = admissionManager;
   }

   public void setBedDemographicManager(BedDemographicManager bedDemographicManager) {
	 this.bedDemographicManager = bedDemographicManager;
   }

   public void setCaseManagementManager(CaseManagementManager caseManagementManager) {
	 this.caseManagementManager = caseManagementManager;
   }

   public void setClientManager(ClientManager clientManager) {
	 this.clientManager = clientManager;
   }

   public void setProgramManager(ProgramManager programManager) {
	 this.programManager = programManager;
   }

   public void setProviderManager(ProviderManager providerManager) {
	 this.providerManager = providerManager;
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

   public void setLookupManager(LookupManager lookupManager) {
	 this.lookupManager = lookupManager;
   }

   
}
