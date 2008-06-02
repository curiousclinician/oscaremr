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

package org.oscarehr.PMmodule.dao;

import java.util.List;

import org.oscarehr.PMmodule.model.caisi_DefaultRoleAccess;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class DefaultRoleAccessDAO extends HibernateDaoSupport {

    public void deleteDefaultRoleAccess(Integer id) {
        this.getHibernateTemplate().delete(getDefaultRoleAccess(id));
    }

    public caisi_DefaultRoleAccess getDefaultRoleAccess(Integer id) {
        return (caisi_DefaultRoleAccess)this.getHibernateTemplate().get(caisi_DefaultRoleAccess.class, id);
    }

    public List getDefaultRoleAccesses() {
        return this.getHibernateTemplate().find("from DefaultRoleAccess dra ORDER BY role_id");
    }

    public void saveDefaultRoleAccess(caisi_DefaultRoleAccess dra) {
        this.getHibernateTemplate().saveOrUpdate(dra);
    }

    public caisi_DefaultRoleAccess find(Integer roleId, Integer accessTypeId) {
        List results = this.getHibernateTemplate().find("from DefaultRoleAccess dra where dra.roleId=? and dra.accessTypeId=?", new Object[] {roleId, accessTypeId});

        if (!results.isEmpty()) {
            return (caisi_DefaultRoleAccess)results.get(0);
        }
        return null;
    }

}
