package org.oscarehr.PMmodule.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.oscarehr.PMmodule.model.Demographic;
import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.PMmodule.model.ProgramClientRestriction;
import org.oscarehr.PMmodule.model.QuatroIntake;
import org.oscarehr.PMmodule.model.QuatroIntakeDB;
import org.oscarehr.PMmodule.model.QuatroIntakeFamily;
import org.oscarehr.PMmodule.service.ClientManager;
import org.oscarehr.PMmodule.service.ClientRestrictionManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.PMmodule.web.formbean.ClientSearchFormBean;
import org.oscarehr.PMmodule.web.formbean.QuatroClientFamilyIntakeForm;

import oscar.MyDateFormat;

import com.quatro.common.KeyConstants;
import com.quatro.model.LookupCodeValue;
import com.quatro.service.IntakeManager;
import com.quatro.service.LookupManager;

public class QuatroFamilyIntakeAction extends BaseClientAction {

   private IntakeManager intakeManager;
   private LookupManager lookupManager;
   private ClientManager clientManager;
   private ProgramManager programManager;
   private ClientRestrictionManager clientRestrictionManager;
   
   public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       return edit(mapping, form, request, response);
   }

   public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       QuatroClientFamilyIntakeForm clientForm = (QuatroClientFamilyIntakeForm)form; 
       
       String intakeId = (String)clientForm.getIntakeId();
       Integer intakeFamilyHeadId = intakeManager.getIntakeFamilyHeadId(intakeId);
       request.setAttribute("intakeHeadId", intakeFamilyHeadId);
       
       if(clientForm.getIntakeStatus()==null){
         QuatroIntakeDB headIntakeDB = intakeManager.getQuatroIntakeDBByIntakeId(Integer.valueOf(intakeId));
         clientForm.setIntakeStatus(headIntakeDB.getIntakeStatus());
       }  
       
       HashMap actionParam = (HashMap) request.getAttribute("actionParam");
       if(actionParam==null){
    	  actionParam = new HashMap();
          actionParam.put("headclientId", request.getParameter("headclientId")); 
          actionParam.put("clientId", request.getParameter("clientId")); 
          actionParam.put("intakeId", request.getParameter("intakeId")); 
       }
       request.setAttribute("actionParam", actionParam);
       
       String demographicNo = (String)actionParam.get("headclientId");
       request.setAttribute("headclientId", demographicNo);
       request.setAttribute("clientId", (String)actionParam.get("clientId"));
       request.setAttribute("client", clientManager.getClientByDemographicNo((String)actionParam.get("clientId")));
       
       if(demographicNo.equals((String)actionParam.get("clientId")))
           request.setAttribute("isReadOnly", Boolean.FALSE);
       else if(!(intakeFamilyHeadId.toString().equals(intakeId)))
      	 request.setAttribute("isReadOnly", Boolean.TRUE);
       else
         request.setAttribute("isReadOnly", Boolean.FALSE);
       
       List genders = lookupManager.LoadCodeList("GEN",true, null, null);
       LookupCodeValue obj2= new LookupCodeValue();
       obj2.setCode("");
       obj2.setDescription("");
       genders.add(0,obj2);
       clientForm.setGenders(genders);

       List relationships = lookupManager.LoadCodeList("FRA",true, null, null);
       relationships.add(0,obj2);
       clientForm.setRelationships(relationships);
       
	   Demographic familyHead = intakeManager.getClientByDemographicNo(demographicNo);
	   for(int i=0;i<genders.size();i++){
           LookupCodeValue obj= (LookupCodeValue)genders.get(i);
           if(obj.getCode().equals(familyHead.getSex())){
        	 familyHead.setSexDesc(obj.getDescription());
		     break;
           }  
	   }
	   
       List dependent = intakeManager.getClientFamilyByIntakeId(intakeId);
       if(dependent==null) dependent = new ArrayList(); 
       for(int i=0;i<dependent.size();i++){
     	  QuatroIntakeFamily obj= (QuatroIntakeFamily)dependent.get(i);
     	  obj.setNewClientChecked("N");
     	  obj.setDuplicateClient("N");
     	  obj.setServiceRestriction("N");
     	  obj.setStatusMsg("#");
       }
       for(int i=0;i<dependent.size();i++){
    	  QuatroIntakeFamily obj= (QuatroIntakeFamily)dependent.get(i);
    	  if(obj.getIntakeHeadId().equals(obj.getIntakeId())){
    		 dependent.remove(obj);
    		 break;
    	  }
       }

       clientForm.setFamilyHead(familyHead);
       clientForm.setDob(MyDateFormat.getStandardDate(familyHead.getDateOfBirth()));
       clientForm.setDependents(dependent);
       clientForm.setDependentsSize(dependent.size());
       
       super.setScreenMode(request, KeyConstants.TAB_CLIENT_INTAKE);
       return mapping.findForward("edit");
   }

   public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       QuatroClientFamilyIntakeForm clientForm = (QuatroClientFamilyIntakeForm)form; 
       
       String intakeId = (String)clientForm.getIntakeId();
       Integer intakeFamilyHeadId = intakeManager.getIntakeFamilyHeadId(intakeId);
       request.setAttribute("intakeHeadId", intakeFamilyHeadId);

       HashMap actionParam = (HashMap) request.getAttribute("actionParam");
       if(actionParam==null){
    	  actionParam = new HashMap();
          actionParam.put("headclientId", request.getParameter("headclientId")); 
          actionParam.put("clientId", request.getParameter("clientId")); 
          actionParam.put("intakeId", request.getParameter("intakeId")); 
       }
       request.setAttribute("actionParam", actionParam);
       
       String demographicNo= (String)actionParam.get("headclientId");
       request.setAttribute("headclientId", demographicNo);
       request.setAttribute("clientId", (String)actionParam.get("clientId"));
       request.setAttribute("client", clientManager.getClientByDemographicNo((String)actionParam.get("clientId")));

       if(demographicNo.equals((String)actionParam.get("clientId")))
           request.setAttribute("isReadOnly", Boolean.FALSE);
       else if(!(intakeFamilyHeadId.toString().equals(intakeId)))
      	 request.setAttribute("isReadOnly", Boolean.TRUE);
       else
       	 request.setAttribute("isReadOnly", Boolean.FALSE);

       List genders = lookupManager.LoadCodeList("GEN",true, null, null);
       LookupCodeValue obj3= new LookupCodeValue();
       obj3.setCode("");
       obj3.setDescription("");
       genders.add(0,obj3);
       clientForm.setGenders(genders);

       List relationships = lookupManager.LoadCodeList("FRA",true, null, null);
       relationships.add(0,obj3);
       clientForm.setRelationships(relationships);

	   Demographic familyHead = clientForm.getFamilyHead();
	   for(int i=0;i<genders.size();i++){
           LookupCodeValue obj= (LookupCodeValue)genders.get(i);
           if(obj.getCode().equals(familyHead.getSex())){
        	 familyHead.setSexDesc(obj.getDescription());
		     break;
           }  
	   }
       clientForm.setFamilyHead(familyHead);
       
       ArrayList dependents = new ArrayList();
	   int dependentsSize=clientForm.getDependentsSize();

	   for(int i=0;i<dependentsSize;i++){
	      QuatroIntakeFamily obj = new QuatroIntakeFamily();	
		  obj.setClientId(Integer.valueOf(request.getParameter("dependent[" + i +"].clientId")));
		  obj.setIntakeHeadId(Integer.valueOf(request.getParameter("intakeHeadId")));
		  obj.setIntakeId(Integer.valueOf(request.getParameter("dependent[" + i +"].intakeId")));
		  obj.setLastName(request.getParameter("dependent[" + i +"].lastName"));
		  obj.setFirstName(request.getParameter("dependent[" + i +"].firstName"));
		  obj.setDob(request.getParameter("dependent[" + i +"].dob"));
		  obj.setSex(request.getParameter("dependent[" + i +"].sex"));
		  obj.setAlias(request.getParameter("dependent[" + i +"].alias"));
		  obj.setRelationship(request.getParameter("dependent[" + i +"].relationship"));
		  obj.setSelect(request.getParameter("dependent[" + i +"].select"));
		  obj.setNewClientChecked(request.getParameter("dependent[" + i +"].newClientChecked"));
		  obj.setDuplicateClient(request.getParameter("dependent[" + i +"].duplicateClient"));
		  obj.setServiceRestriction(request.getParameter("dependent[" + i +"].serviceRestriction"));
		  obj.setEffDate(request.getParameter("dependent[" + i +"].effDate"));
		  obj.setJoinFamilyDateTxt(request.getParameter("dependent[" + i +"].joinFamilyDateTxt"));
		  if(obj.getClientId().intValue()>0){
		     obj.setStatusMsg("#");
		  }else{
			obj.setStatusMsg("");
		  }
		  dependents.add(obj);
	    }
       
        QuatroIntakeFamily obj2 = new QuatroIntakeFamily();
        obj2.setClientId(new Integer(0));
		obj2.setIntakeHeadId(Integer.valueOf(request.getParameter("intakeHeadId")));
        obj2.setIntakeId(new Integer(0));
        String currentDateTxt = MyDateFormat.getSysDateString(new Date());
    	obj2.setDuplicateClient("N");  
		obj2.setNewClientChecked("N");
		obj2.setServiceRestriction("N");
		obj2.setStatusMsg("");
		obj2.setEffDate(currentDateTxt);
		obj2.setJoinFamilyDateTxt(currentDateTxt);
        dependents.add(obj2);
        clientForm.setDependents(dependents);
        clientForm.setDependentsSize(dependents.size());
       
        super.setScreenMode(request, KeyConstants.TAB_CLIENT_INTAKE);
       return mapping.findForward("edit");
   }

   public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       QuatroClientFamilyIntakeForm clientForm = (QuatroClientFamilyIntakeForm)form; 
       
       String intakeId = (String)clientForm.getIntakeId();
       Integer intakeFamilyHeadId = intakeManager.getIntakeFamilyHeadId(intakeId);
       request.setAttribute("intakeHeadId", intakeFamilyHeadId);

       HashMap actionParam = (HashMap) request.getAttribute("actionParam");
       if(actionParam==null){
    	  actionParam = new HashMap();
          actionParam.put("headclientId", request.getParameter("headclientId")); 
          actionParam.put("clientId", request.getParameter("clientId")); 
          actionParam.put("intakeId", request.getParameter("intakeId")); 
       }
       request.setAttribute("actionParam", actionParam);
       
       String demographicNo= (String)actionParam.get("headclientId");
       request.setAttribute("headclientId", demographicNo);
       request.setAttribute("clientId", (String)actionParam.get("clientId"));
       request.setAttribute("client", clientManager.getClientByDemographicNo((String)actionParam.get("clientId")));

       if(demographicNo.equals((String)actionParam.get("clientId")))
           request.setAttribute("isReadOnly", Boolean.FALSE);
       else if(!(intakeFamilyHeadId.toString().equals(intakeId)))
      	 request.setAttribute("isReadOnly", Boolean.TRUE);
       else
       	 request.setAttribute("isReadOnly", Boolean.FALSE);
       
       List genders = lookupManager.LoadCodeList("GEN",true, null, null);
       LookupCodeValue obj2= new LookupCodeValue();
       obj2.setCode("");
       obj2.setDescription("");
       genders.add(0,obj2);
       clientForm.setGenders(genders);

       List relationships = lookupManager.LoadCodeList("FRA",true, null, null);
       relationships.add(0,obj2);
       clientForm.setRelationships(relationships);
       
	   Demographic familyHead = clientForm.getFamilyHead();
	   for(int i=0;i<genders.size();i++){
           LookupCodeValue obj= (LookupCodeValue)genders.get(i);
           if(obj.getCode().equals(familyHead.getSex())){
        	 familyHead.setSexDesc(obj.getDescription());
		     break;
           }  
	   }
       clientForm.setFamilyHead(familyHead);

       ArrayList dependents = new ArrayList();
	   int dependentsSize=clientForm.getDependentsSize();

	   for(int i=0;i<dependentsSize;i++){
	      if(request.getParameter("dependent[" + i +"].select")==null){
		    QuatroIntakeFamily obj = new QuatroIntakeFamily();	
		    obj.setClientId(Integer.valueOf(request.getParameter("dependent[" + i +"].clientId")));
			obj.setIntakeHeadId(Integer.valueOf(request.getParameter("intakeHeadId")));
		    obj.setIntakeId(Integer.valueOf(request.getParameter("dependent[" + i +"].intakeId")));
		    obj.setLastName(request.getParameter("dependent[" + i +"].lastName"));
		    obj.setFirstName(request.getParameter("dependent[" + i +"].firstName"));
		    obj.setDob(request.getParameter("dependent[" + i +"].dob"));
		    obj.setSex(request.getParameter("dependent[" + i +"].sex"));
		    obj.setAlias(request.getParameter("dependent[" + i +"].alias"));
		    obj.setRelationship(request.getParameter("dependent[" + i +"].relationship"));
		    obj.setIntakeId(Integer.valueOf(request.getParameter("dependent[" + i +"].intakeId")));
		    obj.setSelect(request.getParameter("dependent[" + i +"].select"));
			obj.setNewClientChecked(request.getParameter("dependent[" + i +"].newClientChecked"));
			obj.setDuplicateClient(request.getParameter("dependent[" + i +"].duplicateClient"));
			obj.setServiceRestriction(request.getParameter("dependent[" + i +"].serviceRestriction"));
			obj.setEffDate(request.getParameter("dependent[" + i +"].effDate"));
			obj.setJoinFamilyDateTxt(request.getParameter("dependent[" + i +"].joinFamilyDateTxt"));
		    if(obj.getClientId().intValue()>0){
			  obj.setStatusMsg("#");
		    }else{
			  obj.setStatusMsg("");
		    }
		    dependents.add(obj);
	      }
	    }

       clientForm.setDependents(dependents);
       clientForm.setDependentsSize(dependents.size());
	   
       super.setScreenMode(request, KeyConstants.TAB_CLIENT_INTAKE);
       return mapping.findForward("edit");
   }

   public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
       ActionMessages messages = new ActionMessages();
       boolean isError = false;
       boolean isWarning = false;

       QuatroClientFamilyIntakeForm clientForm = (QuatroClientFamilyIntakeForm)form; 

       String intakeId = (String)clientForm.getIntakeId();
       Integer intakeFamilyHeadId = intakeManager.getIntakeFamilyHeadId(intakeId);
       request.setAttribute("intakeHeadId", intakeFamilyHeadId);

	   HashMap actionParam = (HashMap) request.getAttribute("actionParam");
       if(actionParam==null){
    	  actionParam = new HashMap();
          actionParam.put("headclientId", request.getParameter("headclientId")); 
          actionParam.put("clientId", request.getParameter("clientId")); 
          actionParam.put("intakeId", intakeId); 
       }
       request.setAttribute("actionParam", actionParam);

       String demographicNo= (String)actionParam.get("headclientId");
       request.setAttribute("headclientId", demographicNo);
       request.setAttribute("clientId", (String)actionParam.get("clientId"));
       request.setAttribute("client", clientManager.getClientByDemographicNo((String)actionParam.get("clientId")));
       
       List genders = lookupManager.LoadCodeList("GEN",true, null, null);
       LookupCodeValue obj4= new LookupCodeValue();
       obj4.setCode("");
       obj4.setDescription("");
       genders.add(0,obj4);
       clientForm.setGenders(genders);

       List relationships = lookupManager.LoadCodeList("FRA",true, null, null);
       relationships.add(0,obj4);
       clientForm.setRelationships(relationships);
       
	   Demographic familyHead = intakeManager.getClientByDemographicNo(demographicNo);
	   for(int i=0;i<genders.size();i++){
           LookupCodeValue obj= (LookupCodeValue)genders.get(i);
           if(obj.getCode().equals(familyHead.getSex())){
        	 familyHead.setSexDesc(obj.getDescription());
		     break;
           }  
	   }
       clientForm.setFamilyHead(familyHead);
	   
       ArrayList dependents = new ArrayList();
	   int dependentsSize=clientForm.getDependentsSize();
       
	   boolean bDupliDemographicNoApproved=true;
	   String newClientConfirmed= request.getParameter("newClientConfirmed");
	   
       StringBuffer sb = new StringBuffer();
       for(int i=0;i<dependentsSize;i++){
		  QuatroIntakeFamily obj = new QuatroIntakeFamily();	
		  obj.setClientId(Integer.valueOf(request.getParameter("dependent[" + i +"].clientId")));
		  obj.setIntakeHeadId(Integer.valueOf(request.getParameter("intakeHeadId")));
          obj.setIntakeId(Integer.valueOf(request.getParameter("dependent[" + i +"].intakeId")));
		  if(obj.getIntakeId().intValue()>0){
			sb.append("," + obj.getIntakeId().toString());
		  }
          obj.setLastName(request.getParameter("dependent[" + i +"].lastName"));
		  obj.setFirstName(request.getParameter("dependent[" + i +"].firstName"));
		  obj.setDob(request.getParameter("dependent[" + i +"].dob"));
		  obj.setSex(request.getParameter("dependent[" + i +"].sex"));
		  obj.setAlias(request.getParameter("dependent[" + i +"].alias"));
		  obj.setRelationship(request.getParameter("dependent[" + i +"].relationship"));
		  obj.setIntakeId(Integer.valueOf(request.getParameter("dependent[" + i +"].intakeId")));
		  obj.setSelect(request.getParameter("dependent[" + i +"].select"));
		  obj.setNewClientChecked(request.getParameter("dependent[" + i +"].newClientChecked"));
		  if(newClientConfirmed==null || newClientConfirmed.equals("N")){
			  obj.setDuplicateClient(request.getParameter("dependent[" + i +"].duplicateClient"));
		  }else{
			  obj.setDuplicateClient("N");
		  }
		  obj.setServiceRestriction("N");  //don't check service restriction until no duplicate client.
		  obj.setEffDate(request.getParameter("dependent[" + i +"].effDate"));
		  obj.setJoinFamilyDateTxt(request.getParameter("dependent[" + i +"].joinFamilyDateTxt"));

		  if(obj.getClientId().intValue()>0){
			obj.setStatusMsg("#");
		  }else{
			obj.setStatusMsg("");
		  }

		  
		  //check duplicate client for intakeId==0 && clientId==0 
		  if(obj.getIntakeId().intValue()==0 && obj.getClientId().intValue()==0){
			if(newClientConfirmed == null || newClientConfirmed.equals("N")){
		       ClientSearchFormBean criteria = new ClientSearchFormBean();
	           criteria.setLastName(obj.getLastName());
	           criteria.setFirstName(obj.getFirstName());
	           criteria.setDob(obj.getDob());
	           criteria.setGender(obj.getSex());
		       List lst = clientManager.search(criteria, false,true);
	           if(lst.size()>0){
	             obj.setDuplicateClient("Y");
		         obj.setNewClientChecked("N");
				 obj.setStatusMsg("?");
	             bDupliDemographicNoApproved=false;
	           }else{
		         obj.setDuplicateClient("N");
		         obj.setNewClientChecked("Y");
		         obj.setStatusMsg("");
	           }
			}
		  }
		  
	      dependents.add(obj);
	   }
	   
       if(!bDupliDemographicNoApproved){
		 messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("error.intake.family.duplicated_client",
          			request.getContextPath()));
	     saveMessages(request,messages);
         isError = true;
         clientForm.setDependents(dependents);
         clientForm.setDependentsSize(dependents.size());
         request.setAttribute("bDupliDemographicNoApproved", "false");
         return mapping.findForward("edit");
       }else{
         request.setAttribute("bDupliDemographicNoApproved", "true");
       }

       Integer headIntakeId = Integer.valueOf(clientForm.getIntakeId());
       
       //check if family members existing in other families.
       if(KeyConstants.INTAKE_STATUS_ACTIVE.equals(clientForm.getIntakeStatus())){
  	     //intakeFamily is null before a family created
    	 List intakeFamily = intakeManager.getClientFamilyByIntakeId(headIntakeId.toString());
         for(int i=0;i<dependentsSize;i++){
           QuatroIntakeFamily obj3 = (QuatroIntakeFamily)dependents.get(i);
           int j=-1;
           if(intakeFamily!=null){
             for(j=0;j<intakeFamily.size();j++){
               //obj5: current family member in DB
        	   QuatroIntakeFamily obj5 = (QuatroIntakeFamily)intakeFamily.get(j);
               if(obj3.getClientId().equals(obj5.getClientId())) break;
             }
           }
         
           //new added dependent (existing client)
           if(intakeFamily==null || (j==intakeFamily.size() && obj3.getClientId().intValue()>0)){
             //only check intake_status=active/admitted
             List activeIntakeIds = intakeManager.getActiveIntakeIds(obj3.getClientId());
             for(j=0;j<activeIntakeIds.size();j++){
               Integer intakeHeadId_exist =intakeManager.getIntakeFamilyHeadId(((Integer)activeIntakeIds.get(j)).toString());
               if(intakeHeadId_exist.intValue()>0 && !intakeHeadId_exist.equals(headIntakeId)){
          		  messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("error.intake.family.existing_in_other_family",
               			request.getContextPath(), obj3.getLastName() + "," + obj3.getFirstName()));
                  isError = true;
               }
             }
           }
         }
         if(isError){
	       saveMessages(request,messages);
           clientForm.setDependents(dependents);
           clientForm.setDependentsSize(dependents.size());
           return mapping.findForward("edit");
         }
       }       
       
       String providerNo = (String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO);
 	   QuatroIntake headIntake = intakeManager.getQuatroIntake(headIntakeId);
	   Program program = programManager.getProgram(headIntake.getProgramId());
       for(int i=0;i<dependentsSize;i++){
         QuatroIntakeFamily obj3 = (QuatroIntakeFamily)dependents.get(i);

   		 //check gender conflict and age conflict
         Demographic client;
         if(obj3.getClientId().intValue()==0){
           client = new Demographic();
         }else{
           client = clientManager.getClientByDemographicNo(obj3.getClientId().toString());
         }
         client.setDateOfBirth(MyDateFormat.getCalendar(obj3.getDob()));
         client.setSex(obj3.getSex());
		 if(clientRestrictionManager.checkGenderConflict(program, client)){
         	 messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("warning.intake.gender_conflict", request.getContextPath()));
             isWarning = true;
		 }
		 if(clientRestrictionManager.checkAgeConflict(program, client)){
         	  messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("warning.intake.age_conflict", request.getContextPath()));
             isWarning = true;
		 }
    	   
  	     //check service restriction
    	 obj3.setServiceRestriction("N");
         if(obj3.getClientId().intValue()>0){
           ProgramClientRestriction restrInPlace = clientRestrictionManager.checkClientRestriction(
        		 headIntake.getProgramId(), obj3.getClientId(), new Date());
           if (restrInPlace != null) {
     	     obj3.setServiceRestriction("Y");
        	 messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("warning.intake.service_restriction",
             			request.getContextPath(), headIntake.getProgramName()));
       		 isWarning = true;
           }
         }
       }
       
       QuatroIntakeFamily intakeFamilyHead = new QuatroIntakeFamily();
       intakeFamilyHead.setIntakeHeadId(headIntake.getId());
       intakeFamilyHead.setIntakeId(headIntake.getId());
       intakeFamilyHead.setMemberStatus(KeyConstants.INTAKE_STATUS_ACTIVE);
       intakeFamilyHead.setRelationship(KeyConstants.FAMILY_HEAD_CODE);
       intakeFamilyHead.setJoinFamilyDate(headIntake.getCreatedOn());
       intakeFamilyHead.setLastUpdateUser(providerNo);
       intakeFamilyHead.setLastUpdateDate(new GregorianCalendar());
       intakeManager.saveQuatroIntakeFamilyHead(intakeFamilyHead);

       //delete removed family memeber from family intake.
       if(sb.length()>0){
         intakeManager.removeInactiveIntakeFamilyMember(sb.substring(1), headIntake.getId());
       }else{
         intakeManager.removeInactiveIntakeFamilyMember("", headIntake.getId());
       }

       for(int i=0;i<dependentsSize;i++){
       	 Demographic client = new Demographic();
         QuatroIntakeFamily intakeFamily = (QuatroIntakeFamily)dependents.get(i);
         client.setDemographicNo(intakeFamily.getClientId());
         client.setLastName(intakeFamily.getLastName());
         client.setFirstName(intakeFamily.getFirstName());

		 client.setDateOfBirth(MyDateFormat.getCalendar(intakeFamily.getDob()));
         
         client.setSex(intakeFamily.getSex());
         client.setAlias(intakeFamily.getAlias());
         client.setProviderNo(providerNo);
         client.setLastUpdateDate(new GregorianCalendar());
         client.setEffDate(MyDateFormat.getCalendar(intakeFamily.getEffDate()).getTime());
         
     	 //check if this client has any existing active intake with same program before create new intake
         QuatroIntakeDB newClient_intakeDBExist = null;
     	 if(intakeFamily.getIntakeId().intValue()==0){
     		newClient_intakeDBExist = intakeManager.findQuatroIntakeDB(intakeFamily.getClientId(), headIntake.getProgramId());
      	    if(newClient_intakeDBExist!=null) intakeFamily.setIntakeId(newClient_intakeDBExist.getId());
     	 }
  		 
  		 QuatroIntake intake = new QuatroIntake();
     	 intake.setClientId(client.getDemographicNo());
     	 intake.setId(intakeFamily.getIntakeId());
     	 intake.setProgramId(headIntake.getProgramId());
     	 //copy head intake to new client intake
    
     	 if(intake.getId().intValue()==0){
     	 //  intake.setStaffId(providerNo);
     	   intake.setCreatedOn(Calendar.getInstance());
       	   intake.setIntakeStatus(KeyConstants.INTAKE_STATUS_ACTIVE);

       	   intake.setReferredBy(headIntake.getReferredBy());
       	   intake.setContactName(headIntake.getContactName());
       	   intake.setContactNumber(headIntake.getContactNumber());
       	   intake.setContactEmail(headIntake.getContactEmail());

       	   intake.setLanguage(headIntake.getLanguage());
       	   intake.setAboriginal(headIntake.getAboriginal());
       	   intake.setAboriginalOther(headIntake.getAboriginalOther());
       	   intake.setVAW(headIntake.getVAW());
       	   intake.setCurSleepArrangement(headIntake.getCurSleepArrangement());
       	   intake.setOriginalCountry(headIntake.getOriginalCountry());
     	 }else{
           intake.setIntakeStatus(headIntake.getIntakeStatus());
     	 }
     	 intake.setStaffId(providerNo);
     	 intake.setLastUpdateDate(new GregorianCalendar());

     	 intakeFamily.setJoinFamilyDate(MyDateFormat.getCalendar(intakeFamily.getJoinFamilyDateTxt()));
     	 intakeFamily.setMemberStatus(KeyConstants.INTAKE_STATUS_ACTIVE);
     	 intakeFamily.setIntakeHeadId(headIntake.getId());
     	 intakeFamily.setLastUpdateUser(providerNo);
     	 intakeFamily.setLastUpdateDate(new GregorianCalendar());
     	 ArrayList lst2 = intakeManager.saveQuatroIntakeFamily(client, Integer.valueOf(intakeFamilyHeadId), intake, newClient_intakeDBExist, intakeFamily);
     	 intakeFamily.setIntakeId((Integer)lst2.get(1));
     	 intakeFamily.setClientId((Integer)lst2.get(2));
  	   }
   
  	   if(!(isWarning || isError)){
  		  messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("message.save.success", request.getContextPath()));
	   }else if(isWarning){
		  messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("warning.intake.saved_with_warning"));
	   }
       saveMessages(request,messages);
	   
       super.setScreenMode(request, KeyConstants.TAB_CLIENT_INTAKE);
	
       //in case added same client in multiple family member lines,
       //the save method saved family member correctly, 
       //but need to remove duplicated family member line(s) by reading every family memeber from DB again.  
       intakeFamilyHeadId = intakeManager.getIntakeFamilyHeadId(intakeId);
       request.setAttribute("intakeHeadId", intakeFamilyHeadId);

       if(demographicNo.equals((String)actionParam.get("clientId")))
           request.setAttribute("isReadOnly", Boolean.FALSE);
       else if(!(intakeFamilyHeadId.toString().equals(intakeId)))
      	 request.setAttribute("isReadOnly", Boolean.TRUE);
       else
       	 request.setAttribute("isReadOnly", Boolean.FALSE);
       
       List dependentsDB = intakeManager.getClientFamilyByIntakeId(intakeFamilyHeadId.toString());
       if(dependentsDB==null) dependentsDB = new ArrayList(); 
       for(int i=0;i<dependentsDB.size();i++){
     	  QuatroIntakeFamily obj= (QuatroIntakeFamily)dependentsDB.get(i);
     	  obj.setNewClientChecked("N");
     	  obj.setDuplicateClient("N");
     	  obj.setServiceRestriction("N");
     	  obj.setStatusMsg("#");
       }
       for(int i=0;i<dependentsDB.size();i++){
    	  QuatroIntakeFamily obj= (QuatroIntakeFamily)dependentsDB.get(i);
    	  if(obj.getIntakeHeadId().equals(obj.getIntakeId())){
    		  dependentsDB.remove(obj);
    		 break;
    	  }
       }
       clientForm.setDependents(dependentsDB);
       clientForm.setDependentsSize(dependentsDB.size());

       return mapping.findForward("edit");
   }

   
   public void setIntakeManager(IntakeManager intakeManager) {
	 this.intakeManager = intakeManager;
   }

   public void setLookupManager(LookupManager lookupManager) {
	 this.lookupManager = lookupManager;
   }

   public void setClientManager(ClientManager clientManager) {
	 this.clientManager = clientManager;
   }

   public void setClientRestrictionManager(ClientRestrictionManager clientRestrictionManager) {
	 this.clientRestrictionManager = clientRestrictionManager;
   }

   public void setProgramManager(ProgramManager programManager) {
	 this.programManager = programManager;
   }
   
}
