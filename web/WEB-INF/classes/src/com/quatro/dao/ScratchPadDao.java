/*******************************************************************************
 * Copyright (c) 2008, 2009 Quatro Group Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which accompanies this distribution, and is available at
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Contributors:
 *     <Quatro Group Software Systems inc.>  <OSCAR Team>
 *******************************************************************************/
package com.quatro.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ScratchPadDao extends HibernateDaoSupport {

	public boolean isScratchFilled(String providerNo) {
        ArrayList paramList = new ArrayList();
		String sSQL="SELECT s.scratch_text FROM ScratchPad s WHERE s.provider_no = ? order by s.id";		
        paramList.add(providerNo);
        Object params[] = paramList.toArray(new Object[paramList.size()]);
        List lst = getHibernateTemplate().find(sSQL ,params);

		if (lst.size()>0){
		  String obj= (String)lst.get(0);
		  return (obj.trim().length()>0);
		}
		else{
		  return false; 
		}
		
	}
	
/*
	public CaisiRole getRoleByProviderNo(String provider_no) {
		return (CaisiRole)this.getHibernateTemplate().find("from CaisiRole cr where cr.provider_no = ?",new Object[] {provider_no}).get(0);
	}

	public List getRolesByRole(String role) {
		return this.getHibernateTemplate().find("from CaisiRole cr where cr.role = ?",new Object[] {role});
	}
*/
	
/*	
    public List<CaisiRole> getRoles() {
		return this.getHibernateTemplate().find("from Role");
	}
*/
	
}
