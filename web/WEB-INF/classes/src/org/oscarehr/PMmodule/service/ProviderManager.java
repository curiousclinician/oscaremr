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

package org.oscarehr.PMmodule.service;

import java.util.ArrayList;
import java.util.List;

import org.oscarehr.PMmodule.dao.AgencyDao;
import org.oscarehr.PMmodule.dao.OscarSecurityDAO;
import org.oscarehr.PMmodule.dao.ProgramProviderDAO;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.PMmodule.model.Agency;
import org.oscarehr.PMmodule.model.Facility;
import org.oscarehr.PMmodule.model.caisi_ProgramProvider;
import org.oscarehr.PMmodule.model.Provider;
import org.oscarehr.PMmodule.model.SecUserRole;


public class ProviderManager
{
	private ProviderDao dao;
	private AgencyDao agencyDAO;
	private ProgramProviderDAO programProviderDAO;
	private OscarSecurityDAO oscarSecurityDAO; 
	
	
	public void setProviderDao(ProviderDao dao)	{
		this.dao = dao;
	}
	
	public void setAgencyDAO(AgencyDao dao) {
		this.agencyDAO = dao;
	}
	
	public void setProgramProviderDAO(ProgramProviderDAO dao) {
		this.programProviderDAO = dao;
	}
	
	public void setOscarSecurityDAO(OscarSecurityDAO oscarSecurityDAO) {
		this.oscarSecurityDAO = oscarSecurityDAO;
	}
	
	public Provider getProvider(String providerNo)
	{
		return dao.getProvider(providerNo);
	}
	
	public String getProviderName(String providerNo)
	{
		return dao.getProviderName(providerNo);
	}
	
	public List getProviders()
	{
		return dao.getProviders();
	}
	
	public List getActiveProviders()
	{
		return dao.getActiveProviders();
	}

    public List getActiveProviders(String facilityId, String programId) {
		return dao.getActiveProviders(facilityId, programId);
    }
	
	public List search(String name) {
		return dao.search(name);
	}

    public List getProgramDomain(String providerNo) {
		return programProviderDAO.getProgramDomain(providerNo);
	}

    public List getProgramDomainByFacility(String providerNo, Integer facilityId) {
		return programProviderDAO.getProgramDomainByFacility(providerNo, facilityId);
	}

    public List getFacilitiesInProgramDomain(String providerNo) {
        return programProviderDAO.getFacilitiesInProgramDomain(providerNo);
    }
	public List getFacilityIds(String provider_no)
	{
		return dao.getFacilityIds(provider_no);
	}

    public List getAgencyDomain(String providerNo) {
		Agency localAgency =  agencyDAO.getLocalAgency();
		List agencies = new ArrayList();
		agencies.add(localAgency);
		return agencies;
	}
	
	public List getProvidersByType(String type) {
		return dao.getProvidersByType(type);
	}
	
	public List getSecUserRoles(String providerNo) {
		return oscarSecurityDAO.getUserRoles(providerNo);
	}

	
}
