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
import org.apache.struts.util.LabelValueBean;
import org.oscarehr.PMmodule.model.Demographic;
import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.PMmodule.model.ProgramClientRestriction;
import org.oscarehr.PMmodule.model.ProgramQueue;
import org.oscarehr.PMmodule.model.QuatroIntake;
import org.oscarehr.PMmodule.service.ClientManager;
import org.oscarehr.PMmodule.service.ClientRestrictionManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.PMmodule.service.ProgramQueueManager;
import org.oscarehr.PMmodule.web.formbean.ClientSearchFormBean;
import org.oscarehr.PMmodule.web.formbean.QuatroIntakeEditForm;

import oscar.MyDateFormat;

import com.quatro.common.KeyConstants;
import com.quatro.model.LookupCodeValue;
import com.quatro.service.IntakeManager;
import com.quatro.service.LookupManager;

public class QuatroIntakeEditAction extends BaseClientAction {
    private ClientManager clientManager;
    private LookupManager lookupManager;
    private IntakeManager intakeManager;
    private ProgramManager programManager;
    private ClientRestrictionManager clientRestrictionManager;
    private ProgramQueueManager programQueueManager;
	
    public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	return update(mapping,form,request,response);
	}

    //for new client
    public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	QuatroIntakeEditForm qform = (QuatroIntakeEditForm) form;

    	String clientId = qform.getClientId();
        Integer intakeId = Integer.valueOf(qform.getIntakeId());
        HashMap actionParam = (HashMap) request.getAttribute("actionParam");
        if(actionParam==null){
     	  actionParam = new HashMap();
           actionParam.put("clientId", clientId); 
           actionParam.put("intakeId", intakeId.toString()); 
        }
        request.setAttribute("actionParam", actionParam);

        Demographic client;
        if(Integer.parseInt(clientId)>0){
		  client= clientManager.getClientByDemographicNo(clientId);
		  qform.setDob(MyDateFormat.getStandardDate(client.getDateOfBirth()));
        }else{
          client= new Demographic();
  		  qform.setDob("");
        }
		qform.setClient(client);
        
		com.quatro.web.intake.OptionList optionValues = intakeManager.LoadOptionsList();
  		qform.setOptionList(optionValues);

        Integer shelterId= (Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
        ArrayList lst= (ArrayList)programManager.getProgramIds(
        		shelterId,(String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO));
        ArrayList lst2 = new ArrayList();
        ArrayList lst3 = new ArrayList();
        for(int i=0;i<lst.size();i++){
           Object[] obj = (Object[])lst.get(i);
           lst2.add(new LabelValueBean((String)obj[1], ((Integer)obj[0]).toString()));
           lst3.add(new LabelValueBean((String)obj[2], ((Integer)obj[0]).toString()));
        }
        qform.setProgramList(lst2);
        qform.setProgramTypeList(lst3);

        QuatroIntake obj;
        if(intakeId.intValue()!=0){
            obj=intakeManager.getQuatroIntake(intakeId);
        }else{
        	obj= new QuatroIntake();
        	obj.setCreatedOn(Calendar.getInstance());
            obj.setId(new Integer(0));
            obj.setClientId(Integer.valueOf(qform.getClientId()));
            obj.setIntakeStatus(KeyConstants.INTAKE_STATUS_ACTIVE);
    		obj.setStaffId((String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO));
    		obj.setYouth(KeyConstants.CONSTANT_NO);
    		obj.setVAW(KeyConstants.CONSTANT_NO);
        }

        obj.setCurrentProgramId(obj.getProgramId());
		qform.setIntake(obj);

		request.setAttribute("intakeHeadId", new Integer(0));  //intakeHeadId: for intake stauts='discharged' or 'rejected' to view family details.

        LookupCodeValue language;
        LookupCodeValue originalCountry;
        if(intakeId.intValue()!=0){
          if(obj.getLanguage()!=null){
        	language = lookupManager.GetLookupCode("LNG", obj.getLanguage());
          }else{
          	language = new LookupCodeValue();
          }
          if(obj.getOriginalCountry()!=null){
            originalCountry = lookupManager.GetLookupCode("CNT", obj.getOriginalCountry());
          }else{
        	originalCountry = new LookupCodeValue();
          }
        }else{
            language = new LookupCodeValue();
            originalCountry = new LookupCodeValue();
        }
        
        qform.setLanguage(language);
        qform.setOriginalCountry(originalCountry);

        request.setAttribute("newClientFlag", "true");
        request.setAttribute("isReadOnly", Boolean.FALSE);
        super.setScreenMode(request, KeyConstants.TAB_CLIENT_INTAKE);
		return mapping.findForward("edit");
    }
    
    //for existing client
    public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	QuatroIntakeEditForm qform = (QuatroIntakeEditForm) form;

    	String clientId = qform.getClientId();
        Integer intakeId = Integer.valueOf(qform.getIntakeId());
        HashMap actionParam = (HashMap) request.getAttribute("actionParam");
        if(actionParam==null){
     	  actionParam = new HashMap();
          actionParam.put("clientId", clientId);
          actionParam.put("intakeId", intakeId.toString()); 
        }
        request.setAttribute("actionParam", actionParam);
        Integer intakeHeadId = intakeManager.getIntakeFamilyHeadId(intakeId.toString());
        if(intakeHeadId.intValue()!=0){
          Integer intakeHeadClientId = intakeManager.getQuatroIntakeDBByIntakeId(intakeHeadId).getClientId();
          request.setAttribute("clientId", intakeHeadClientId); 
          request.setAttribute("intakeHeadId", intakeHeadId);  //intakeHeadId: for intake stauts='discharged' or 'rejected' to view family details.
        }else{
          request.setAttribute("clientId", clientId); 
          request.setAttribute("intakeHeadId", new Integer(0)); //intakeHeadId: for intake stauts='discharged' or 'rejected' to view family details. 
        }

        Demographic client;
        if(Integer.parseInt(clientId)>0){
		  client= clientManager.getClientByDemographicNo(clientId);
		  qform.setDob(MyDateFormat.getStandardDate(client.getDateOfBirth()));
        }else{
          client= new Demographic();
  		  qform.setDob("");
        } 	
		qform.setClient(client);
		request.setAttribute("client", client);
		com.quatro.web.intake.OptionList optionValues = intakeManager.LoadOptionsList();
  		qform.setOptionList(optionValues);

        QuatroIntake intake;
        if(intakeId.intValue()!=0){
        	intake=intakeManager.getQuatroIntake(intakeId);
        	if(KeyConstants.PROGRAM_TYPE_Service.equals(intake.getProgramType())){
        	   if(intake.getEndDate().before(Calendar.getInstance())) intake.setIntakeStatus(KeyConstants.STATUS_INACTIVE);	
        	}
        	boolean readOnly=super.isReadOnly(request,intake.getIntakeStatus(), KeyConstants.FUN_PMM_CLIENTINTAKE,intake.getProgramId());
            request.setAttribute("isReadOnly", Boolean.valueOf(readOnly));
        }else{
        	intake= new QuatroIntake();
        	intake.setCreatedOn(Calendar.getInstance());
        	intake.setId(new Integer(0));
        	intake.setClientId(Integer.valueOf(qform.getClientId()));
        	intake.setIntakeStatus(KeyConstants.INTAKE_STATUS_ACTIVE);
        	intake.setStaffId((String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO));
        	intake.setYouth(KeyConstants.CONSTANT_NO);
        	intake.setVAW(KeyConstants.CONSTANT_NO);
            request.setAttribute("isReadOnly", Boolean.FALSE);
        }

        Integer shelterId= (Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
        ArrayList lst= (ArrayList)programManager.getProgramIds( 
        		shelterId,(String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO));
        ArrayList lst2 = new ArrayList();
        ArrayList lst3 = new ArrayList();
        for(int i=0;i<lst.size();i++){
           Object[] obj = (Object[])lst.get(i);
           //don't allow existing intake change program type
           //if program type is wrong, discharge/reject this intake (for bed program), then create a new intake with correct program type.
           if(intakeId.intValue()>0){
             if(intake.getProgramType().equals((String)obj[2])){	   
               lst2.add(new LabelValueBean((String)obj[1], ((Integer)obj[0]).toString()));
               lst3.add(new LabelValueBean((String)obj[2], ((Integer)obj[0]).toString()));
             }  
           }else{
             lst2.add(new LabelValueBean((String)obj[1], ((Integer)obj[0]).toString()));
             lst3.add(new LabelValueBean((String)obj[2], ((Integer)obj[0]).toString()));
           }  
        }
        qform.setProgramList(lst2);
        qform.setProgramTypeList(lst3);
        
        intake.setCurrentProgramId(intake.getProgramId());
		qform.setIntake(intake);

        request.setAttribute("programId", intake.getProgramId()); 
        ProgramQueue queue=programQueueManager.getProgramQueuesByIntakeId(intake.getId());
        if(queue!=null) request.setAttribute("queueId", queue.getId());
		
        LookupCodeValue language = null;
        LookupCodeValue originalCountry = null;
        if(intakeId.intValue()!=0){
        	language = lookupManager.GetLookupCode("LNG", intake.getLanguage());
            originalCountry = lookupManager.GetLookupCode("CNT", intake.getOriginalCountry());
        }
        if (language == null) language = new LookupCodeValue();
        if (originalCountry == null) originalCountry = new LookupCodeValue();
        
        qform.setLanguage(language);
        qform.setOriginalCountry(originalCountry);
        
        request.setAttribute("PROGRAM_TYPE_Bed", KeyConstants.PROGRAM_TYPE_Bed);
        super.setScreenMode(request, KeyConstants.TAB_CLIENT_INTAKE);       
        return mapping.findForward("edit");
	}

    //for existing client
    public ActionForward manualreferral(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	QuatroIntakeEditForm qform = (QuatroIntakeEditForm) form;

    	String clientId = qform.getClientId();
        Integer queueId = Integer.valueOf(request.getParameter("queueId"));
        Integer programId = Integer.valueOf(request.getParameter("programId"));
        
        ProgramQueue queue = programQueueManager.getProgramQueue(queueId.toString());
        Integer referralId = queue.getReferralId();
        
        HashMap actionParam = new HashMap();
        actionParam.put("clientId", clientId);
        actionParam.put("intakeId", "0"); 
        request.setAttribute("actionParam", actionParam);

        request.setAttribute("clientId", clientId); 
        request.setAttribute("fromManualReferral", "Y");

        Demographic client;
	    client= clientManager.getClientByDemographicNo(clientId);
		qform.setDob(MyDateFormat.getStandardDate(client.getDateOfBirth()));
		qform.setClient(client);
		request.setAttribute("client", client);

		com.quatro.web.intake.OptionList optionValues = intakeManager.LoadOptionsList();
  		qform.setOptionList(optionValues);

        QuatroIntake intake;

        intake= new QuatroIntake();
        intake.setCreatedOn(Calendar.getInstance());
        intake.setId(new Integer(0));
        intake.setClientId(Integer.valueOf(qform.getClientId()));
        intake.setIntakeStatus(KeyConstants.INTAKE_STATUS_ACTIVE);
        intake.setStaffId((String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO));
        intake.setYouth(KeyConstants.CONSTANT_NO);
        intake.setVAW(KeyConstants.CONSTANT_NO);
        intake.setProgramId(programId);

        Integer shelterId= (Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
        ArrayList lst= (ArrayList)programManager.getProgramIds( 
        		shelterId,(String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO));
        ArrayList lst2 = new ArrayList();
        ArrayList lst3 = new ArrayList();
        for(int i=0;i<lst.size();i++){
           Object[] obj = (Object[])lst.get(i);
           lst2.add(new LabelValueBean((String)obj[1], ((Integer)obj[0]).toString()));
           lst3.add(new LabelValueBean((String)obj[2], ((Integer)obj[0]).toString()));
        }
        qform.setProgramList(lst2);
        qform.setProgramTypeList(lst3);
        
        intake.setCurrentProgramId(new Integer(0));
		qform.setIntake(intake);
		
        LookupCodeValue language = null;
        LookupCodeValue originalCountry = null;
        if (language == null) language = new LookupCodeValue();
        if (originalCountry == null) originalCountry = new LookupCodeValue();
        
        qform.setLanguage(language);
        qform.setOriginalCountry(originalCountry);
        
        request.setAttribute("PROGRAM_TYPE_Bed", KeyConstants.PROGRAM_TYPE_Bed);
        super.setScreenMode(request, KeyConstants.TAB_CLIENT_INTAKE);
        return mapping.findForward("edit");
	}

    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages messages = new ActionMessages();
        boolean isError = false;
        boolean isWarning = false;
    	QuatroIntakeEditForm qform = (QuatroIntakeEditForm) form;

    	String clientId = qform.getClientId();
        
    	Demographic client= qform.getClient();
    	QuatroIntake intake= qform.getIntake();
    	String providerNo =(String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO);
    	//check for new client duplication
    	if(intake.getClientId().intValue()==0 && request.getParameter("newClientChecked").equals("N")){
    	   ClientSearchFormBean criteria = new ClientSearchFormBean();
    	   criteria.setActive("");
    	   criteria.setAssignedToProviderNo("");
    	   criteria.setLastName(request.getParameter("client.lastName"));
    	   criteria.setFirstName(request.getParameter("client.firstName"));
    	   criteria.setDob(request.getParameter("dob"));
    	   criteria.setGender(request.getParameter("client.sex"));
    	   List lst = clientManager.search(criteria, false,true);
 		   if(lst.size()>0){
    	     messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("error.intake.duplicated_client",
          			request.getContextPath()));
             isError = true;
             saveMessages(request,messages);
             request.setAttribute("newClientFlag", "true");
       	     HashMap actionParam2 = new HashMap();
    	     actionParam2.put("clientId", intake.getClientId()); 
             actionParam2.put("intakeId", intake.getId().toString()); 
             request.setAttribute("actionParam", actionParam2);
		     return mapping.findForward("edit");
 		   }  
		}
    	client.setDemographicNo(intake.getClientId());
		client.setDateOfBirth(MyDateFormat.getCalendar(qform.getDob()));
		client.setProviderNo(providerNo);
		client.setLastUpdateDate(Calendar.getInstance());
		if(qform.getClient().getEffDateTxt().equals("")){
		  client.setEffDate(new Date());
		}else{
		  client.setEffDate(MyDateFormat.getSysDate(qform.getClient().getEffDateTxt()));
		}

		/* intake */
    	if (null != intake.getEndDateTxt()) {
    		intake.setEndDate(MyDateFormat.getCalendar(intake.getEndDateTxt()));
    	}
    	intake.setLastUpdateDate(Calendar.getInstance());
		//get program type
    	ArrayList lst= (ArrayList)qform.getProgramTypeList();
		for(int i=0;i<lst.size();i++){
			LabelValueBean obj2= (LabelValueBean)lst.get(i);
			if(Integer.valueOf(obj2.getValue()).equals(intake.getProgramId())){
			  intake.setProgramType(obj2.getLabel());
			  break;
			}
		}

	  if(!"Y".equals(request.getParameter(KeyConstants.CONFIRMATION_CHECKBOX_NAME)) &&
		KeyConstants.INTAKE_STATUS_ACTIVE.equals(intake.getIntakeStatus()) &&
		(intake.getId().intValue()==0 || 
		(intake.getId().intValue()>0 && !intake.getCurrentProgramId().equals(intake.getProgramId())))){

	    //check gender conflict and age conflict
	    if(intake.getProgramType().equals(KeyConstants.BED_PROGRAM_TYPE)){
		   Program program = programManager.getProgram(intake.getProgramId());
		   if(clientRestrictionManager.checkGenderConflict(program, client)){
          	  messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("warning.intake.gender_conflict", request.getContextPath()));
              isWarning = true;
		   }
		   if(clientRestrictionManager.checkAgeConflict(program, client)){
          	  messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("warning.intake.age_conflict", request.getContextPath()));
              isWarning = true;
		   }
		}
		
		//check service restriction
		if(!intake.getProgramId().equals(intake.getCurrentProgramId())){
          ProgramClientRestriction restrInPlace = clientRestrictionManager.checkClientRestriction(
        		intake.getProgramId(), intake.getClientId(), new Date());
          if (restrInPlace != null && request.getParameter("skipError")==null) {
        	List programs = qform.getProgramList();  
        	for(int i=0;i<programs.size();i++) {
        		LabelValueBean obj3 = (LabelValueBean) programs.get(i);
            	if(obj3.getValue().equals(intake.getProgramId().toString())){
          		  if(intake.getProgramType().equals(KeyConstants.BED_PROGRAM_TYPE)){
               		messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("warning.intake.service_restriction",
               			request.getContextPath(), obj3.getLabel()));
               		isWarning = true;
                    break;
          		  }else{
          			messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("error.intake.service_restriction",
              			request.getContextPath(), obj3.getLabel()));
              		isError = true;
                    break;
          		  }
            	}
            }
            
    		if(isError){
                saveMessages(request,messages);
            	HashMap actionParam = new HashMap();
            	actionParam.put("clientId", client.getDemographicNo()); 
                actionParam.put("intakeId", intake.getId().toString()); 
                Integer intakeHeadId = intakeManager.getIntakeFamilyHeadId(intake.getId().toString());
                if(intakeHeadId.intValue()!=0){
                  Integer intakeHeadClientId = intakeManager.getQuatroIntakeDBByIntakeId(intakeHeadId).getClientId();
                  request.setAttribute("clientId", intakeHeadClientId); 
                }else{
                  request.setAttribute("clientId", client.getDemographicNo()); 
                }
                request.setAttribute("actionParam", actionParam);
                request.setAttribute("client", client);
            	intake.setClientId(client.getDemographicNo());
    			
                return mapping.findForward("edit");
    		}
          }
		}
		if(isWarning){
  			messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("warning.intake.overwrite_conflict",
          			request.getContextPath()));
            saveMessages(request,messages);

            saveMessages(request,messages);
        	HashMap actionParam = new HashMap();
        	actionParam.put("clientId", client.getDemographicNo()); 
            actionParam.put("intakeId", intake.getId().toString()); 
            Integer intakeHeadId = intakeManager.getIntakeFamilyHeadId(intake.getId().toString());
            if(intakeHeadId.intValue()!=0){
              Integer intakeHeadClientId = intakeManager.getQuatroIntakeDBByIntakeId(intakeHeadId).getClientId();
              request.setAttribute("clientId", intakeHeadClientId); 
            }else{
              request.setAttribute("clientId", client.getDemographicNo()); 
            }
            request.setAttribute("actionParam", actionParam);
            request.setAttribute("client", client);
        	intake.setClientId(client.getDemographicNo());

        	return mapping.findForward("edit");
		}
	  }
	  
		clientManager.saveClient(client);

    	HashMap actionParam = new HashMap();
    	actionParam.put("clientId", client.getDemographicNo()); 
        actionParam.put("intakeId", intake.getId().toString()); 
        Integer intakeHeadId = intakeManager.getIntakeFamilyHeadId(intake.getId().toString());
        if(intakeHeadId.intValue()!=0){
          Integer intakeHeadClientId = intakeManager.getQuatroIntakeDBByIntakeId(intakeHeadId).getClientId();
          request.setAttribute("clientId", intakeHeadClientId); 
        }else{
          request.setAttribute("clientId", client.getDemographicNo()); 
        }
        request.setAttribute("actionParam", actionParam);
        request.setAttribute("client", client);
    	intake.setClientId(client.getDemographicNo());
	  
	  
		if(intake.getCreatedOnTxt().equals("")==false){
			intake.setCreatedOn(MyDateFormat.getCalendarwithTime(intake.getCreatedOnTxt()));
		}else{
	  	  Calendar cal= Calendar.getInstance();
	  	  intake.setCreatedOn(cal);
	  	  intake.setCreatedOnTxt(MyDateFormat.getStandardDateTime(cal));
		}
 
		if(intake.getId().intValue()==0) intake.setIntakeStatus(KeyConstants.INTAKE_STATUS_ACTIVE);		
		intake.setLanguage(request.getParameter("language_code"));
		intake.setOriginalCountry(request.getParameter("originalCountry_code"));
         
		intake.setPregnant(request.getParameter("intake.pregnant"));
		intake.setDisclosedAbuse(request.getParameter("intake.disclosedAbuse"));
		intake.setObservedAbuse(request.getParameter("intake.observedAbuse"));
		intake.setDisclosedMentalIssue(request.getParameter("intake.disclosedMentalIssue"));
		intake.setPoorHygiene(request.getParameter("intake.poorHygiene"));
		intake.setObservedMentalIssue(request.getParameter("intake.observedMentalIssue"));
		intake.setDisclosedAlcoholAbuse(request.getParameter("intake.disclosedAlcoholAbuse"));
		intake.setObservedAlcoholAbuse(request.getParameter("intake.observedAlcoholAbuse"));
		intake.setBirthCertificateYN(request.getParameter("intake.birthCertificateYN"));
		intake.setSINYN(request.getParameter("intake.SINYN"));
		intake.setHealthCardNoYN(request.getParameter("intake.healthCardNoYN"));
		intake.setDriverLicenseNoYN(request.getParameter("intake.driverLicenseNoYN"));
		intake.setCitizenCardNoYN(request.getParameter("intake.citizenCardNoYN"));
		intake.setNativeReserveNoYN(request.getParameter("intake.nativeReserveNoYN"));
		intake.setVeteranNoYN(request.getParameter("intake.veteranNoYN"));
		intake.setRecordLandingYN(request.getParameter("intake.recordLandingYN"));
		intake.setLibraryCardYN(request.getParameter("intake.libraryCardYN"));
		intake.setStaffId(providerNo);
		intake.setLastUpdateDate(new GregorianCalendar());
        LookupCodeValue language = new LookupCodeValue();
        language.setCode(request.getParameter("language_code"));
        language.setDescription(request.getParameter("language_description"));
        LookupCodeValue originalCountry = new LookupCodeValue();
        originalCountry.setCode(request.getParameter("originalCountry_code"));
        originalCountry.setDescription(request.getParameter("originalCountry_description"));
		qform.setLanguage(language);        
		qform.setOriginalCountry(originalCountry);
		
        Integer shelterId= (Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
        
		if(intake.getId().intValue()==0 && intakeManager.checkExistBedIntakeByFacility(intake.getClientId(), intake.getProgramId()).size()>0){
  			messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("error.intake.duplicate_bedprogram_intake",
          			request.getContextPath()));
        	isError = true;
		}else{
			Integer fromManualReferralId = null;
			if(!request.getParameter("fromManualReferralId").equals("")) fromManualReferralId = new Integer(request.getParameter("fromManualReferralId"));
			ArrayList lst2 = intakeManager.saveQuatroIntake(intake, intakeHeadId, intakeHeadId.intValue()>0, fromManualReferralId);
			Integer intakeId = (Integer)lst2.get(0);
			Integer queueId = new Integer(0);
			if(lst2.get(2)!=null) queueId = (Integer)lst2.get(2);
			intake.setId(intakeId);
			intake.setCurrentProgramId(intake.getProgramId());
			qform.setIntake(intake);
	        request.setAttribute("programId", intake.getProgramId()); 
	        request.setAttribute("queueId", queueId); 

		}
		
		if(!(isWarning || isError)){
			messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("message.save.success", request.getContextPath()));
		}else if(isWarning){
			messages.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("warning.intake.saved_with_warning"));
		}
        saveMessages(request,messages);

        request.setAttribute("intakeHeadId", request.getParameter("intakeHeadId")); 

        request.setAttribute("PROGRAM_TYPE_Bed", KeyConstants.PROGRAM_TYPE_Bed);
        super.setScreenMode(request, KeyConstants.TAB_CLIENT_INTAKE);
        return mapping.findForward("edit");
	}
	
	public ClientManager getClientManager() {
		return clientManager;
	}

	public void setClientManager(ClientManager clientManager) {
		this.clientManager = clientManager;
	}

	public LookupManager getLookupManager() {
		return lookupManager;
	}

	public void setLookupManager(LookupManager lookupManager) {
		this.lookupManager = lookupManager;
	}

	public IntakeManager getIntakeManager() {
		return intakeManager;
	}

	public void setIntakeManager(IntakeManager intakeManager) {
		this.intakeManager = intakeManager;
	}

	public void setProgramManager(ProgramManager programManager) {
		this.programManager = programManager;
	}

	public void setClientRestrictionManager(
			ClientRestrictionManager clientRestrictionManager) {
		this.clientRestrictionManager = clientRestrictionManager;
	}

	public void setProgramQueueManager(ProgramQueueManager programQueueManager) {
		this.programQueueManager = programQueueManager;
	}


}
