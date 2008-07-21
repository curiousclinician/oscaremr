package org.oscarehr.PMmodule.web.admin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.oscarehr.PMmodule.dao.AdmissionDao;
import org.oscarehr.PMmodule.dao.ClientDao;
import org.oscarehr.PMmodule.model.Admission;
import org.oscarehr.PMmodule.model.Demographic;
import org.oscarehr.PMmodule.model.Facility;
import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.PMmodule.service.FacilityManager;
import org.oscarehr.PMmodule.service.LogManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.PMmodule.web.BaseFacilityAction;
import org.oscarehr.PMmodule.web.FacilityDischargedClients;

import com.quatro.common.KeyConstants;
import com.quatro.model.LookupCodeValue;
import com.quatro.service.LookupManager;
import oscar.MyDateFormat;

/**
 */
public class FacilityManagerAction extends BaseFacilityAction {
    private FacilityManager facilityManager;
    private AdmissionDao admissionDao;
    private ClientDao clientDao;
    private ProgramManager programManager;
    private LookupManager lookupManager;
    private LogManager logManager;

    private static final String FORWARD_EDIT = "edit";
    private static final String FORWARD_VIEW = "view";
    private static final String FORWARD_LIST = "list";
    private static final String FORWARD_PROGRAM = "program";

    private static final String BEAN_FACILITIES = "facilities";
    private static final String BEAN_ASSOCIATED_PROGRAMS = "associatedPrograms";
    private static final String BEAN_ASSOCIATED_CLIENTS = "associatedClients";

    public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        return list(mapping, form, request, response);
    }

    public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        
    	Integer shelterId = (Integer)request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
    	List facilities =null;
    	if(shelterId.intValue()>0) facilities =	facilityManager.getFacilities(shelterId);
    	else facilities =	facilityManager.getFacilities();
     
        request.setAttribute(BEAN_FACILITIES, facilities);
        
        // get agency's organization list from caisi editor table
        request.setAttribute("orgList", lookupManager.LoadCodeList("SHL", true, null, null));

        // get agency's sector list from caisi editor table
        request.setAttribute("sectorList", lookupManager.LoadCodeList("SEC", true, null, null));
               
        //boolean readOnly = super.isReadOnly(request, KeyConstants.FUN_PMM_FACILITYLIST, shelterId);
        //request.setAttribute("isEditable", Boolean.valueOf(!readOnly));
        super.setMenu(request,KeyConstants.MENU_FACILITY);
        return mapping.findForward(FORWARD_LIST);
    }

    public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	
    	
    	
    	HashMap actionParam = (HashMap) request.getAttribute("actionParam");
        if(actionParam==null){
     	  actionParam = new HashMap();
           actionParam.put("facilityId", request.getParameter("facilityId")); 
        }
        request.setAttribute("actionParam", actionParam);
        
        
    	String idStr = request.getParameter("facilityId");
        Integer facilityId = Integer.valueOf(idStr);
        Facility facility = facilityManager.getFacility(facilityId);

        FacilityManagerForm facilityForm = (FacilityManagerForm) form;
        

//        // Get facility associated client list -----------
//        Map facilityClientsMap = new LinkedHashMap();
//        List facilityClients = new ArrayList();
//
//        // Get program list by facility id in table room.
//        // for (Program program : programManager.getPrograms(id)) {
//        List programs = programManager.getPrograms(id);
//        for (int i=0;i<programs.size();i++) {
//        	Program program = (Program) programs.get(i);
//            if (program != null) {
//                // Get admission list by program id and automatic_discharge=true
//
//                List admissions = admissionDao.getAdmissionsByProgramId(program.getId(), new Boolean(true), new Integer(-7));
//                if (admissions != null) {
//                    Iterator it = admissions.iterator();
//                    while (it.hasNext()) {
//
//                        Admission admission = (Admission) it.next();
//
//                        // Get demographic list by demographic_no
//                        Demographic client = clientDao.getClientByDemographicNo(admission.getClientId());
//
//                        String name = client.getFirstName() + " " + client.getLastName();
//                        String dob = client.getYearOfBirth() + "/" + client.getMonthOfBirth() + "/" + client.getDateOfBirth();
//                        String pName = program.getName();
//                        Date dischargeDate = admission.getDischargeDate().getTime();
//                        String dDate = dischargeDate.toString();
//
//                        // today's date
//                        Calendar calendar = Calendar.getInstance();
//
//                        // today's date - days
//                        calendar.add(Calendar.DAY_OF_YEAR, -1);
//
//                        Date oneDayAgo = calendar.getTime();
//
//                        FacilityDischargedClients fdc = new FacilityDischargedClients();
//                        fdc.setName(name);
//                        fdc.setDob(dob);
//                        fdc.setProgramName(pName);
//                        fdc.setDischargeDate(dDate);
//
//                        if (dischargeDate.after(oneDayAgo)) {
//                            fdc.setInOneDay(true);
//                        }
//                        else {
//                            fdc.setInOneDay(false);
//                        }
//                        facilityClients.add(fdc);
//
//                    }
//                }
//            }
//        }
//        request.setAttribute(BEAN_ASSOCIATED_CLIENTS, facilityClients);
//
//        request.setAttribute(BEAN_ASSOCIATED_PROGRAMS, programs);

        
        // get agency's organization list from caisi editor table
        List sList = lookupManager.LoadCodeList("SHL", true, null, null);
        for(int i = 0; i <sList.size();i++){
        	LookupCodeValue lv = (LookupCodeValue)sList.get(i);
        	if(lv.getCode().equals( facility.getOrgId().toString())){
        		facility.setShelter(lv.getDescription());
        		break;
        	}
        }
        // get agency's sector list from caisi editor table
        List sList2 = lookupManager.LoadCodeList("SEC", true, null, null);
        for(int i = 0; i <sList2.size();i++){
        	LookupCodeValue lv = (LookupCodeValue)sList2.get(i);
        	if(lv.getCode().equals( facility.getSectorId().toString())){
        		facility.setSector(lv.getDescription());
        		break;
        	}
        }

        request.setAttribute("facilityId", facility.getId());
        request.setAttribute("facility", facility);
        facilityForm.setFacility(facility);
        super.setScreenMode(request, KeyConstants.TAB_FACILITY_GENERAL,facility.getId());
        return mapping.findForward(FORWARD_VIEW);
    }
    
    public ActionForward listPrograms(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	   	
    	HashMap actionParam = (HashMap) request.getAttribute("actionParam");
        if(actionParam==null){
     	  actionParam = new HashMap();
           actionParam.put("facilityId", request.getParameter("facilityId")); 
        }
        request.setAttribute("actionParam", actionParam);
        
        String providerNo = (String)(request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO));
    	String idStr = request.getParameter("facilityId");
        Integer facilityId = Integer.valueOf(idStr);
        Facility facility = facilityManager.getFacility(facilityId);

        // Get program list by facility id in table room.
        List programs = programManager.getBedProgramsInFacility(providerNo,facilityId);
        

        request.setAttribute(BEAN_ASSOCIATED_PROGRAMS, programs);
        
        
        
//      Get facility associated client list -----------
        List facilityClients = new ArrayList();

         // Get program list by facility id in table room.
                  
         for (int i=0;i<programs.size();i++) {
         	Program program = (Program) programs.get(i);
             if (program != null) {
                 // Get admission list by program id and automatic_discharge=true

                 List admissions = admissionDao.getAdmissionsByProgramId(program.getId(), new Boolean(true), new Integer(-7));
                 if (admissions != null) {
                     Iterator it = admissions.iterator();
                     while (it.hasNext()) {

                         Admission admission = (Admission) it.next();

                         // Get demographic list by demographic_no
                         Demographic client = clientDao.getClientByDemographicNo(admission.getClientId());

                         String name = client.getFirstName() + " " + client.getLastName();
                         String dob = MyDateFormat.getStandardDate(client.getDateOfBirth());
                         String pName = program.getName();
                         Date dischargeDate = admission.getDischargeDate().getTime();
                         String dDate = dischargeDate.toString();

                         // today's date
                         Calendar calendar = Calendar.getInstance();

                         // today's date - days
                         calendar.add(Calendar.DAY_OF_YEAR, -1);

                         Date oneDayAgo = calendar.getTime();

                         FacilityDischargedClients fdc = new FacilityDischargedClients();
                         fdc.setName(name);
                         fdc.setDob(dob);
                         fdc.setProgramName(pName);
                         fdc.setDischargeDate(dDate);

                         if (dischargeDate.after(oneDayAgo)) {
                             fdc.setInOneDay(true);
                         }
                         else {
                             fdc.setInOneDay(false);
                         }
                         facilityClients.add(fdc);

                     }
                 }
             }
         }
         request.setAttribute(BEAN_ASSOCIATED_CLIENTS, facilityClients);
        
        

        request.setAttribute("facilityId", facility.getId());
        
        request.setAttribute("facility", facility);
        super.setScreenMode(request, KeyConstants.TAB_FACILITY_PROGRAM,facility.getId());
        return mapping.findForward(FORWARD_PROGRAM);
    }
    /*
    public ActionForward listMessages(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	
    	super.setScreenMode(request, KeyConstants.TAB_FACILITY_MESSAGE);
    	
    	HashMap actionParam = (HashMap) request.getAttribute("actionParam");
        if(actionParam==null){
     	  actionParam = new HashMap();
           actionParam.put("facilityId", request.getParameter("facilityId")); 
        }
        request.setAttribute("actionParam", actionParam);
        
        String providerNo = (String)(request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO));
    	String idStr = request.getParameter("facilityId");
        Integer facilityId = Integer.valueOf(idStr);
        Facility facility = facilityManager.getFacility(facilityId);

       // Get facility associated client list -----------
       List facilityClients = new ArrayList();

        // Get program list by facility id in table room.
        // for (Program program : programManager.getPrograms(id)) {
        List programs = programManager.getBedProgramsInFacility(providerNo,facilityId);
        for (int i=0;i<programs.size();i++) {
        	Program program = (Program) programs.get(i);
            if (program != null) {
                // Get admission list by program id and automatic_discharge=true

                List admissions = admissionDao.getAdmissionsByProgramId(program.getId(), new Boolean(true), new Integer(-7));
                if (admissions != null) {
                    Iterator it = admissions.iterator();
                    while (it.hasNext()) {

                        Admission admission = (Admission) it.next();

                        // Get demographic list by demographic_no
                        Demographic client = clientDao.getClientByDemographicNo(admission.getClientId());

                        String name = client.getFirstName() + " " + client.getLastName();
                        String dob = client.getYearOfBirth() + "/" + client.getMonthOfBirth() + "/" + client.getDateOfBirth();
                        String pName = program.getName();
                        Date dischargeDate = admission.getDischargeDate().getTime();
                        String dDate = dischargeDate.toString();

                        // today's date
                        Calendar calendar = Calendar.getInstance();

                        // today's date - days
                        calendar.add(Calendar.DAY_OF_YEAR, -1);

                        Date oneDayAgo = calendar.getTime();

                        FacilityDischargedClients fdc = new FacilityDischargedClients();
                        fdc.setName(name);
                        fdc.setDob(dob);
                        fdc.setProgramName(pName);
                        fdc.setDischargeDate(dDate);

                        if (dischargeDate.after(oneDayAgo)) {
                            fdc.setInOneDay(true);
                        }
                        else {
                            fdc.setInOneDay(false);
                        }
                        facilityClients.add(fdc);

                    }
                }
            }
        }
        request.setAttribute(BEAN_ASSOCIATED_CLIENTS, facilityClients);

        request.setAttribute("facilityId", facility.getId());
        
        request.setAttribute("facility", facility);

        return mapping.findForward(FORWARD_MESSAGE);
    }
*/
    public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	
    	
    	
    	HashMap actionParam = (HashMap) request.getAttribute("actionParam");
        if(actionParam==null){
     	  actionParam = new HashMap();
           actionParam.put("facilityId", request.getParameter("facilityId")); 
           //actionParam.put("facilityId", request.getParameter("id"));
        }
        request.setAttribute("actionParam", actionParam);
        
    	Facility facility = null;
    	String facilityId = request.getParameter("facilityId");
        if(facilityId!=null){
        	facility = facilityManager.getFacility(Integer.valueOf(facilityId));
        }else{
        	facility = facilityManager.getFacility((Integer)request.getAttribute("facilityId"));
        }

        FacilityManagerForm managerForm = (FacilityManagerForm) form;
        managerForm.setFacility(facility);

        request.setAttribute("facilityId", facility.getId());
        request.setAttribute("orgId", facility.getOrgId());
        request.setAttribute("sectorId", facility.getSectorId());
        request.setAttribute("facility", facility);
        
        // get agency's organization list from caisi editor table
        request.setAttribute("orgList", lookupManager.LoadCodeList("SHL", true, null, null));

        // get agency's sector list from caisi editor table
        request.setAttribute("sectorList", lookupManager.LoadCodeList("SEC", true, null, null));
        super.setScreenMode(request, KeyConstants.TAB_FACILITY_EDIT,facility.getId());
        boolean readOnly = super.isReadOnly(request, KeyConstants.FUN_PMM_FACILITY_EDIT, facility.getId());
        if(readOnly)request.setAttribute("isReadOnly", Boolean.valueOf(readOnly));
        return mapping.findForward(FORWARD_EDIT);
    }

    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	ActionMessages messages = new ActionMessages();
    	
    	try{
	        String facilityId = request.getParameter("facilityId");
	        Facility facility = facilityManager.getFacility(Integer.valueOf(facilityId));
	        facility.setActive(false);
	        facilityManager.saveFacility(facility);
	        
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.remove.success", request.getContextPath()));
            saveMessages(request, messages);

    	}catch( Exception e){
    		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.remove.failed", request.getContextPath()));
            saveMessages(request, messages);
    	}

        return list(mapping, form, request, response);
    }

    public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        Facility facility = new Facility("", "");
        facility.setActive(true);
        ((FacilityManagerForm) form).setFacility(facility);

        // get agency's organization list from caisi editor table
        request.setAttribute("orgList", lookupManager.LoadCodeList("SHL", true, null, null));

        // get agency's sector list from caisi editor table
        request.setAttribute("sectorList", lookupManager.LoadCodeList("SEC", true, null, null));

        return mapping.findForward(FORWARD_EDIT);
    }

    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	
    	
    	FacilityManagerForm mform = (FacilityManagerForm) form;
        Facility facility = mform.getFacility();    	
        
        if (request.getParameter("facility.hic") == null) 
        	facility.setHic(false);
        else
        	facility.setHic(true);
        
        if (request.getParameter("facility.active") == null) 
        	facility.setActive(false);
        else
        	facility.setActive(true);
        
        if (isCancelled(request)) {
            request.getSession().removeAttribute("facilityManagerForm");

            return list(mapping, form, request, response);
        }

        try {
            facilityManager.saveFacility(facility);

            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.save.success", request.getContextPath()));
            saveMessages(request, messages);

            request.setAttribute("facilityId", facility.getId());

            logManager.log("write", "facility", facility.getId().toString(), request);

            //return list(mapping, form, request, response);
            //return mapping.findForward(FORWARD_EDIT);
        }
        catch (Exception e) {
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("duplicateKey", "The name " + facility.getName()));
            saveMessages(request, messages);

            //return mapping.findForward(FORWARD_EDIT);
        }
        request.setAttribute("facility", facility);
    	HashMap actionParam = (HashMap) request.getAttribute("actionParam");
        if(actionParam==null) actionParam = new HashMap();
        actionParam.put("facilityId", facility.getId()); 
        request.setAttribute("actionParam", actionParam);
        
        super.setScreenMode(request, KeyConstants.TAB_FACILITY_EDIT,facility.getOrgId());
        return edit(mapping, form, request, response);
    }

    public FacilityManager getFacilityManager() {
        return facilityManager;
    }

    //@Required
    public void setFacilityManager(FacilityManager facilityManager) {
        this.facilityManager = facilityManager;
    }

    public void setAdmissionDao(AdmissionDao admissionDao) {
        this.admissionDao = admissionDao;
    }

    public void setClientDao(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    public void setLookupManager(LookupManager lookupManager) {
        this.lookupManager = lookupManager;
    }

    public void setLogManager(LogManager mgr) {
        this.logManager = mgr;
    }

    public void setProgramManager(ProgramManager mgr) {
        this.programManager = mgr;
    }

}
