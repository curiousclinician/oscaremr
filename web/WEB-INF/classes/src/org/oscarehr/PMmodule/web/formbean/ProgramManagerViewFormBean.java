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

package org.oscarehr.PMmodule.web.formbean;

import org.apache.struts.action.ActionForm;
import org.oscarehr.PMmodule.model.Admission;
import org.oscarehr.PMmodule.model.Bed;
import org.oscarehr.PMmodule.model.ProgramClientRestriction;

public class ProgramManagerViewFormBean extends ActionForm {

	private static final long serialVersionUID = 1L;

	//public static final String[] tabs = { "General", "Staff", "Function User", "Clients", "Queue", "Access", "Bed Check" , "Client Status", "Service Restrictions", "Incidents"};
	public static final String[] tabs = { "General", "Queue", "Staff", "Clients", "Incidents", "Service Restrictions"};
	private String tab;
	private String clientId;
	private String queueId;
	private Bed[] reservedBeds;
	private String remoteReferralId=null;
	private String switchBed1;
	private String switchBed2;

	private String radioRejectionReason;
    private ProgramClientRestriction serviceRestriction;

    private IncidentForm incidentForm;
    private StaffForm staffForm = new StaffForm();
    private ClientForm clientForm;
       

	public IncidentForm getIncidentForm() {
		return incidentForm;
	}

	public void setIncidentForm(IncidentForm incidentForm) {
		this.incidentForm = incidentForm;
	}

	public String getRadioRejectionReason() {
		return radioRejectionReason;
	}

	public void setRadioRejectionReason(String radioRejectionReason) {
		this.radioRejectionReason = radioRejectionReason;
	}

    public String getRemoteReferralId() {
        return remoteReferralId;
    }

    public void setRemoteReferralId(String remoteReferralId) {
        this.remoteReferralId = remoteReferralId;
    }

    /**
	 * @return Returns the tab.
	 */
	public String getTab() {
		return tab;
	}
	
	/**
	 * @param tab
	 *            The tab to set.
	 */
	public void setTab(String tab) {
		this.tab = tab;
	}

	/**
	 * @return Returns the clientId.
	 */
	public String getClientId() {
		return clientId;
	}
	/**
	 * @param clientId
	 *            The clientId to set.
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public Bed[] getReservedBeds() {
    	return reservedBeds;
    }

	public void setReservedBeds(Bed[] reservedBeds) {
    	this.reservedBeds = reservedBeds;
    }

    public ProgramClientRestriction getServiceRestriction() {
        return serviceRestriction;
    }

    public void setServiceRestriction(ProgramClientRestriction serviceRestriction) {
        this.serviceRestriction = serviceRestriction;
    }

	public String getSwitchBed1() {
		return switchBed1;
	}

	public void setSwitchBed1(String switchBed1) {
		this.switchBed1 = switchBed1;
	}

	public String getSwitchBed2() {
		return switchBed2;
	}

	public void setSwitchBed2(String switchBed2) {
		this.switchBed2 = switchBed2;
	}

	public StaffForm getStaffForm() {
		return staffForm;
	}

	public void setStaffForm(StaffForm staffForm) {
		this.staffForm = staffForm;
	}

	public ClientForm getClientForm() {
		return clientForm;
	}

	public void setClientForm(ClientForm clientForm) {
		this.clientForm = clientForm;
	}
}