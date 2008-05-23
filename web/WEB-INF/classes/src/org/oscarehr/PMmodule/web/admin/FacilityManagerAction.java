package org.oscarehr.PMmodule.web.admin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.struts.actions.DispatchAction;
import org.oscarehr.PMmodule.web.FacilityDischargedClients;
import org.springframework.beans.factory.annotation.Required;

import com.quatro.service.LookupManager;

/**
 */
public class FacilityManagerAction extends DispatchAction {
    private static final Log log = LogFactory.getLog(FacilityManagerAction.class);

    private FacilityManager facilityManager;
    private AdmissionDao admissionDao;
    private ClientDao clientDao;
    private ProgramManager programManager;
    private LookupManager lookupManager;
    private LogManager logManager;

    private static final String FORWARD_EDIT = "edit";
    private static final String FORWARD_VIEW = "view";
    private static final String FORWARD_LIST = "list";

    private static final String BEAN_FACILITIES = "facilities";
    private static final String BEAN_ASSOCIATED_PROGRAMS = "associatedPrograms";
    private static final String BEAN_ASSOCIATED_CLIENTS = "associatedClients";

    public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        return list(mapping, form, request, response);
    }

    public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        List<Facility> facilities = facilityManager.getFacilities();
        List<Facility> filteredFacilities = new ArrayList<Facility>();
        for (Facility facility : facilities) {
            if (!facility.isDisabled()) filteredFacilities.add(facility);
        }
        request.setAttribute(BEAN_FACILITIES, filteredFacilities);

        // get agency's organization list from caisi editor table
        request.setAttribute("orgList", lookupManager.LoadCodeList("OGN", true, null, null));

        // get agency's sector list from caisi editor table
        request.setAttribute("sectorList", lookupManager.LoadCodeList("SEC", true, null, null));

        return mapping.findForward(FORWARD_LIST);
    }

    public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String idStr = request.getParameter("id");
        Integer id = Integer.valueOf(idStr);
        Facility facility = facilityManager.getFacility(id);

        FacilityManagerForm facilityForm = (FacilityManagerForm) form;
        facilityForm.setFacility(facility);

        // Get facility associated client list -----------
        Map facilityClientsMap = new LinkedHashMap();
        List<FacilityDischargedClients> facilityClients = new ArrayList();

        // Get program list by facility id in table room.
        for (Program program : programManager.getPrograms(id)) {
            if (program != null) {
                // Get admission list by program id and automatic_discharge=true

                List<Admission> admissions = admissionDao.getAdmissionsByProgramId(program.getId(), new Boolean(true), new Integer(-7));
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

        request.setAttribute(BEAN_ASSOCIATED_PROGRAMS, programManager.getPrograms(id));

        request.setAttribute("id", facility.getId());

        return mapping.findForward(FORWARD_VIEW);
    }

    public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        Facility facility = facilityManager.getFacility(Integer.valueOf(id));

        FacilityManagerForm managerForm = (FacilityManagerForm) form;
        managerForm.setFacility(facility);

        request.setAttribute("id", facility.getId());
        request.setAttribute("orgId", facility.getOrgId());
        request.setAttribute("sectorId", facility.getSectorId());

        // get agency's organization list from caisi editor table
        request.setAttribute("orgList", lookupManager.LoadCodeList("OGN", true, null, null));

        // get agency's sector list from caisi editor table
        request.setAttribute("sectorList", lookupManager.LoadCodeList("SEC", true, null, null));

        return mapping.findForward(FORWARD_EDIT);
    }

    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String id = request.getParameter("id");
        Facility facility = facilityManager.getFacility(Integer.valueOf(id));
        facility.setDisabled(true);
        facilityManager.saveFacility(facility);

        return list(mapping, form, request, response);
    }

    public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        Facility facility = new Facility("", "");
        ((FacilityManagerForm) form).setFacility(facility);

        // get agency's organization list from caisi editor table
        request.setAttribute("orgList", lookupManager.LoadCodeList("OGN", true, null, null));

        // get agency's sector list from caisi editor table
        request.setAttribute("sectorList", lookupManager.LoadCodeList("SEC", true, null, null));

        return mapping.findForward(FORWARD_EDIT);
    }

    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        FacilityManagerForm mform = (FacilityManagerForm) form;
        Facility facility = mform.getFacility();

        if (request.getParameter("facility.hic") == null) facility.setHic(false);

        if (isCancelled(request)) {
            request.getSession().removeAttribute("facilityManagerForm");

            return list(mapping, form, request, response);
        }

        try {
            facilityManager.saveFacility(facility);

            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("facility.saved", facility.getName()));
            saveMessages(request, messages);

            request.setAttribute("id", facility.getId());

            logManager.log("write", "facility", facility.getId().toString(), request);

            return list(mapping, form, request, response);
        }
        catch (Exception e) {
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("duplicateKey", "The name " + facility.getName()));
            saveMessages(request, messages);

            return mapping.findForward(FORWARD_EDIT);
        }
    }

    public FacilityManager getFacilityManager() {
        return facilityManager;
    }

    @Required
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
