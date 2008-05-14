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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.oscarehr.PMmodule.model.Provider;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import oscar.OscarProperties;
import oscar.util.SqlUtils;

public class ProviderDao extends HibernateDaoSupport {
	private static Log log = LogFactory.getLog(ProviderDao.class);
	
	public boolean providerExists(String providerNo) {
		boolean exists = (((Long) getHibernateTemplate().iterate("select count(*) from Provider p where p.ProviderNo = " + providerNo).next()) == 1);
		log.debug("providerExists: " + exists);

		return exists;
	}

	public Provider getProvider(String providerNo) {
		if (providerNo == null || providerNo.length() <= 0) {
			throw new IllegalArgumentException();
		}

		Provider provider = (Provider) getHibernateTemplate().get(Provider.class, providerNo);

		if (log.isDebugEnabled()) {
			log.debug("getProvider: providerNo=" + providerNo + ",found=" + (provider != null));
		}

		return provider;
	}

	public String getProviderName(String providerNo) {
		if (providerNo == null || providerNo.length() <= 0) {
			throw new IllegalArgumentException();
		}

		Provider provider = getProvider(providerNo);
		String providerName = "";

		if (provider != null && provider.getFirstName() != null) {
			providerName = provider.getFirstName() + " ";
		}

		if (provider != null && provider.getLastName() != null) {
			providerName += provider.getLastName();
		}

		if (log.isDebugEnabled()) {
			log.debug("getProviderName: providerNo=" + providerNo + ",result=" + providerName);
		}

		return providerName;
	}

    public List<Provider> getProviders() {
    	@SuppressWarnings("unchecked")
		List<Provider> rs = getHibernateTemplate().find("FROM  Provider p ORDER BY p.LastName");

		if (log.isDebugEnabled()) {
			log.debug("getProviders: # of results=" + rs.size());
		}
		return rs;
	}

    public List<Provider> getActiveProviders(String facilityId, String programId) {
        ArrayList paramList = new ArrayList();

    	String sSQL;
    	List<Provider> rs;
    	if(programId!=null && "0".equals(programId)==false){
    	  sSQL="FROM  Provider p where p.Status='1' and p.ProviderNo in " +
               "(select c.providerNo from Secuserrole c where c.orgcd ='P' || ?) ORDER BY p.LastName";
	      paramList.add(Long.valueOf(programId));
	      Object params[] = paramList.toArray(new Object[paramList.size()]);
	      rs =  getHibernateTemplate().find(sSQL ,params);
    	}else if(facilityId!=null && "0".equals(facilityId)==false){
    	  sSQL="FROM  Provider p where p.Status='1' and p.ProviderNo in " +
                "(select c.providerNo from Secuserrole c where c.orgcd in " +
                "(select 'P' || a.id from Program a where a.facilityId=?)) ORDER BY p.LastName";
  	      paramList.add(Integer.valueOf(facilityId));
  	      Object params[] = paramList.toArray(new Object[paramList.size()]);
	      rs = getHibernateTemplate().find(sSQL ,params);
    	}else{
    	  sSQL="FROM  Provider p where p.Status='1' ORDER BY p.LastName";
    	  rs = getHibernateTemplate().find(sSQL);
    	}
//    	List<Provider> rs = getHibernateTemplate().find("FROM  Provider p ORDER BY p.LastName");

		return rs;
	}

    public List<Provider> getActiveProviders() {
    	@SuppressWarnings("unchecked")
		List<Provider> rs = getHibernateTemplate().find("FROM  Provider p where p.Status='1' ORDER BY p.LastName");

		if (log.isDebugEnabled()) {
			log.debug("getProviders: # of results=" + rs.size());
		}
		return rs;
	}
    
	public List<Provider> search(String name) {
		boolean isOracle = OscarProperties.getInstance().getDbType().equals("oracle");
		Criteria c = this.getSession().createCriteria(Provider.class);
		if (isOracle) {
			c.add(Restrictions.or(Expression.ilike("FirstName", name + "%"), Expression.ilike("LastName", name + "%")));
		}
		else
		{
			c.add(Restrictions.or(Expression.like("FirstName", name + "%"), Expression.like("LastName", name + "%")));
		}
		c.addOrder(Order.asc("ProviderNo"));

		@SuppressWarnings("unchecked")
		List<Provider> results = c.list();

		if (log.isDebugEnabled()) {
			log.debug("search: # of results=" + results.size());
		}
		return results;
	}

	public List<Provider> getProvidersByType(String type) {
		@SuppressWarnings("unchecked")
		List<Provider> results = this.getHibernateTemplate().find("from Provider p where p.ProviderType = ?", type);

		if (log.isDebugEnabled()) {
			log.debug("getProvidersByType: type=" + type + ",# of results=" + results.size());
		}

		return results;
	}
	
	public static void addProviderToFacility(String provider_no, int facilityId)
	{
	    try {
            SqlUtils.update("insert into provider_facility values ('"+provider_no+"',"+facilityId+')');
        }
        catch (RuntimeException e) {
            // chances are it's a duplicate unique entry exception so it's safe to ignore.
            // this is still unexpected because duplicate calls shouldn't be made
            log.warn("Unexpected exception occurred.", e);
        }
	}
	
	public static void removeProviderFromFacility(String provider_no, int facilityId)
	{
        SqlUtils.update("delete from provider_facility where provider_no='"+provider_no+"' and facility_id="+facilityId);
	}
	
	public List<Integer> getFacilityIds(String provider_no)
	{
//	    return(SqlUtils.selectIntList("select facility_id from secuserrole where provider_no='"+provider_no+'\''));
		String sql = "select distinct substr(codetree,18,7) as facility_id from lst_orgcd" ;
		sql += " where code in (select orgcd from secuserrole where provider_no=?)";
		sql += " and fullcode like '%F%'";

		Query query = getSession().createSQLQuery(sql);
    	((SQLQuery) query).addScalar("facility_id", Hibernate.INTEGER); 
    	query.setString(0, provider_no);

    	List<Integer> lst=query.list();
		return lst;
	}
}
