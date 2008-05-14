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

package org.oscarehr.PMmodule.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oscarehr.PMmodule.dao.ClientReferralDAO;
import org.oscarehr.PMmodule.dao.ProgramQueueDao;
import org.oscarehr.PMmodule.model.ClientReferral;
import org.oscarehr.PMmodule.model.ProgramQueue;
import org.oscarehr.PMmodule.service.ProgramQueueManager;

public class ProgramQueueManagerImpl implements ProgramQueueManager
{
	private static Log log = LogFactory.getLog(ProgramQueueManagerImpl.class);
	private ProgramQueueDao dao;
	private ClientReferralDAO referralDAO;
	
	
	public void setProgramQueueDao(ProgramQueueDao dao)	{
		this.dao = dao;
	}
	
	public void setClientReferralDAO(ClientReferralDAO dao)	{
		this.referralDAO = dao;
	}
	
	public ProgramQueue getProgramQueue(String queueId)
	{
		ProgramQueue pq = dao.getProgramQueue(Integer.valueOf(queueId));
		if(pq == null)
		{
//			log.warn("queueId '" + queueId + "' not found in database.");			
		}
		return pq;
	}
	
	public List getProgramQueuesByProgramId(Integer programId) {
		return dao.getProgramQueuesByProgramId(programId);
	}

	public void saveProgramQueue(ProgramQueue programQueue)	{
		dao.saveProgramQueue(programQueue);
	}
	
	public List getActiveProgramQueuesByProgramId(Integer programId) {
		return dao.getActiveProgramQueuesByProgramId(programId);
	}
	
	public ProgramQueue getActiveProgramQueue(String programId, String demographicNo) {
		return dao.getActiveProgramQueue(Integer.valueOf(programId), Long.valueOf(demographicNo));
	}
	
	public void rejectQueue(String programId, String clientId,String notes, String rejectionReason) {
		ProgramQueue queue = getActiveProgramQueue(programId,clientId);
		if(queue==null) {
			return;
		}
		ClientReferral referral = this.referralDAO.getClientReferral(queue.getReferralId());
		if(referral != null) {
			referral.setStatus(ClientReferral.STATUS_REJECTED);
			referral.setCompletionDate(new Date());
			referral.setCompletionNotes(notes);			
			referral.setRadioRejectionReason(rejectionReason);
			this.referralDAO.saveClientReferral(referral);
		}
		queue.setStatus(ProgramQueue.STATUS_REJECTED);
		
		this.saveProgramQueue(queue);
	}
}
