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
* Toronto, Ontario, Canada  - UPDATED: Quatro Group 2008/2009
 */

package org.caisi.service;

import java.util.ArrayList;
import java.util.List;

//import org.caisi.dao.CustomFilterDAO;
import org.caisi.dao.TicklerDAO;
import org.caisi.model.CustomFilter;
import org.caisi.model.Tickler;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.casemgmt.service.CaseManagementManager;

/**
 */
public class TicklerManager {

    private TicklerDAO ticklerDAO = null;
//    private CustomFilterDAO customFilterDAO = null;
    
    private ProgramManager programManager = null;
    private CaseManagementManager caseManagementManager = null;
    
    public void setProgramManager(ProgramManager programManager) {
        this.programManager = programManager;
    }

    public void setCaseManagementManager(CaseManagementManager caseManagementManager) {
        this.caseManagementManager = caseManagementManager;
    }
    
    public void setTicklerDAO(TicklerDAO ticklerDAO) {
        this.ticklerDAO = ticklerDAO;
    }
/*
    public void setCustomFilterDAO(CustomFilterDAO customFilterDAO) {
        this.customFilterDAO = customFilterDAO;
    }
*/    
    
    public void addTickler(Tickler tickler) {
        ticklerDAO.saveTickler(tickler);
    }
/*
    public List getTicklers() {
        return ticklerDAO.getTicklers();
    }
*/
    public List getTicklers(CustomFilter filter, Integer shelterId, String providerNo) {
        List results = ticklerDAO.getTicklers(filter, shelterId, providerNo);   
        return(results);
    }

/*    
    public List getTicklers(CustomFilter filter, Integer currentFacilityId,String providerNo,String programId) {
        List results = ticklerDAO.getTicklers(filter, currentFacilityId, providerNo,programId);   
        return(results);
    }
*/
    
    public List getTicklersByClientId(Integer facilityId, String providerNo, Integer clientId) {
        List results = ticklerDAO.getTicklersByClientId(facilityId, providerNo,clientId);   
        return(results);
    }
    
    private List ticklerFacilityFiltering(Integer currentFacilityId, List ticklers) {
        ArrayList results = new ArrayList();

//        for (Tickler tickler : ticklers) {
        for (int i=0;i<ticklers.size();i++) {
        	Tickler tickler = (Tickler)ticklers.get(i); 
            Integer programId = tickler.getProgram_id();
            
            if (programManager.hasAccessBasedOnFacility(currentFacilityId, programId)) {            	
            	results.add(tickler);
            }        
        }

        return results;
    }
    
    public int getActiveTicklerCount(String providerNo) {
        return ticklerDAO.getActiveTicklerCount(providerNo);
    }

    public int getNumTicklers(CustomFilter filter) {
        return ticklerDAO.getNumTicklers(filter);
    }

    public Tickler getTickler(String tickler_no) {
        Integer id = Integer.valueOf(tickler_no);
        return ticklerDAO.getTickler(id);
    }

    public void addComment(String tickler_no, String provider, String message, String status) {
        Integer id = Integer.valueOf(tickler_no);
        ticklerDAO.addComment(id, provider, message, status);
    }

    public void reassign(String tickler_no, String provider, String task_assigned_to) {
        Integer id = Integer.valueOf(tickler_no);
        ticklerDAO.reassign(id, provider, task_assigned_to);
    }

    public void deleteTickler(String tickler_no, String provider) {
        ticklerDAO.deleteTickler(Integer.valueOf(tickler_no), provider);
    }

    public void completeTickler(String tickler_no, String provider) {
        ticklerDAO.completeTickler(Integer.valueOf(tickler_no), provider);
    }

    public void activateTickler(String tickler_no, String provider) {
        ticklerDAO.activateTickler(Integer.valueOf(tickler_no), provider);
    }
/*
    public List getCustomFilters() {
        return customFilterDAO.getCustomFilters();
    }

    public List getCustomFilters(String provider_no) {
        return customFilterDAO.getCustomFilters(provider_no);
    }

    public List getCustomFilterWithShortCut(String providerNo) {
        return customFilterDAO.getCustomFilterWithShortCut(providerNo);
    }

    public CustomFilter getCustomFilter(String name) {
        return customFilterDAO.getCustomFilter(name);
    }

    public CustomFilter getCustomFilter(String name, String providerNo) {
        return customFilterDAO.getCustomFilter(name, providerNo);
    }

    public CustomFilter getCustomFilterById(Integer id) {
        return customFilterDAO.getCustomFilterById(id);
    }

    public void saveCustomFilter(CustomFilter filter) {
        customFilterDAO.saveCustomFilter(filter);
    }

    public void deleteCustomFilter(String name) {
        customFilterDAO.deleteCustomFilter(name);
    }

    public void deleteCustomFilterById(Integer id) {
        customFilterDAO.deleteCustomFilterById(id);
    }
*/    
}
