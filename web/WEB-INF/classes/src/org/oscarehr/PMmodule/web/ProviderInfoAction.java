/*
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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.oscarehr.PMmodule.dao.FacilityDAO;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.PMmodule.model.Facility;
import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.PMmodule.model.ProgramProvider;
import org.oscarehr.PMmodule.service.FacilityManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.PMmodule.service.ProviderManager;
import org.apache.struts.actions.DispatchAction;

import com.quatro.common.KeyConstants;

public class ProviderInfoAction extends DispatchAction {

    private FacilityDAO facilityDAO=null;
    private ProgramManager programManager;
    private ProviderManager providerManager;
    
    public void setFacilityDAO(FacilityDAO facilityDAO) {
        this.facilityDAO = facilityDAO;
    }

    public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        return view(mapping, form, request, response);
    }

    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        return view(mapping, form, request, response);
    }

    public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String providerNo = null;
        providerNo = (String)request.getSession().getAttribute("user");
        if(providerNo == null || "".equals(providerNo) ) {
            providerNo = (String)request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO);
        }

        request.setAttribute("provider", providerManager.getProvider(providerNo));
        request.setAttribute("agencyDomain", providerManager.getAgencyDomain(providerNo));

        List<ProgramProvider> programDomain = new ArrayList<ProgramProvider>();

        int facilityId1=0;
        Facility facility = (Facility)request.getSession().getAttribute("currentFacility");
        if(facility!=null) facilityId1=facility.getId();
        
        for (ProgramProvider programProvider : providerManager.getProgramDomainByFacility(providerNo, new Integer(facilityId1))) {
            Program program = programManager.getProgram(programProvider.getProgramId());

            if (program.getProgramStatus().equals("active")) {
                programProvider.setProgram(program);
                programDomain.add(programProvider);
            }
        }
        
/*
        for (ProgramProvider programProvider : providerManager.getProgramDomain(providerNo)) {
            Program program = programManager.getProgram(programProvider.getProgramId());

            if (program.getProgramStatus().equals("active")) {
                programProvider.setProgram(program);
                programDomain.add(programProvider);
            }
        }
       
*/        
        List<Integer> facilityIds = providerManager.getFacilityIds(providerNo);
        ArrayList<Facility> facilities=new ArrayList<Facility>();
        for (Integer facilityId : facilityIds){
            facilities.add(facilityDAO.getFacility(facilityId));
        }
        
        
        request.setAttribute("programDomain", programDomain);
        request.setAttribute("facilityDomain", facilities);

        return mapping.findForward("view");
    }

    public void setProgramManager(ProgramManager mgr) {
    	this.programManager = mgr;
    }

    public void setProviderManager(ProviderManager mgr) {
    	this.providerManager = mgr;
    }

}