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

package org.oscarehr.casemgmt.dao;

import java.util.Date;
import java.util.List;
import org.oscarehr.casemgmt.model.CaseManagementCPP;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class CaseManagementCPPDAO extends HibernateDaoSupport {

    public CaseManagementCPP getCPP(String demographic_no) {
        List results = this.getHibernateTemplate().find("from CaseManagementCPP cpp where cpp.demographic_no = ? order by update_date desc", new Object[] {demographic_no});
        return (results.size() != 0)?(CaseManagementCPP)results.get(0):null;
    }

    public void saveCPP(CaseManagementCPP cpp) {                        
        CaseManagementCPP tempcpp = new CaseManagementCPP();
        
        String fhist = cpp.getFamilyHistory() == null?"":cpp.getFamilyHistory();
        String ongoing = cpp.getOngoingConcerns() == null?"":cpp.getOngoingConcerns();
        String shist = cpp.getSocialHistory() == null?"":cpp.getSocialHistory();
        String rem = cpp.getReminders() == null?"":cpp.getReminders();
        String mhist = cpp.getMedicalHistory() == null?"":cpp.getMedicalHistory();
        String pm = cpp.getPastMedications() == null?"":cpp.getPastMedications();
        String ofnum = cpp.getOtherFileNumber() == null?"":cpp.getOtherFileNumber();
        String ossystem = cpp.getOtherSupportSystems() == null?"":cpp.getOtherSupportSystems();
        
        tempcpp.setDemographic_no(cpp.getDemographic_no());
        tempcpp.setFamilyHistory(fhist);
        tempcpp.setMedicalHistory(mhist);
        tempcpp.setOngoingConcerns(ongoing);
        tempcpp.setReminders(rem);
        tempcpp.setSocialHistory(shist);
        tempcpp.setUpdate_date(new Date());
        tempcpp.setPrimaryPhysician(cpp.getPrimaryPhysician());
        tempcpp.setProvider_no(cpp.getProvider_no());
        tempcpp.setPrimaryCounsellor(cpp.getPrimaryCounsellor());
        tempcpp.setOtherFileNumber(ofnum);
        tempcpp.setOtherSupportSystems(ossystem);
        tempcpp.setPastMedications(pm);         
        this.getHibernateTemplate().save(tempcpp);
        
    }

}
