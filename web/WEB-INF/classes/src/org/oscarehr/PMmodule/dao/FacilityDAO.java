package org.oscarehr.PMmodule.dao;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.oscarehr.PMmodule.model.Facility;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.quatro.util.Utility;

import oscar.util.SqlUtils;

/**
 * Data access object for retrieving, creating, and updating facilities.
 */
public class FacilityDAO extends HibernateDaoSupport {

    public Facility getFacility(Integer id) {
        return (Facility) getHibernateTemplate().get(Facility.class, id);
    }

    //@SuppressWarnings("unchecked")
    public List getFacilities() {
        String query = "from Facility f order by name";
        return getHibernateTemplate().find(query);
    }
    
    public List getFacilities(Integer shelterId) {
        String query = "from Facility f where orgId=? order by name";
        return getHibernateTemplate().find(query,shelterId);
    }

    //@SuppressWarnings("unchecked")
    public List getActiveFacilities() {
        String query = "from Facility f where active=true order by name";
        return getHibernateTemplate().find(query);
    }
     
    public List getActiveFacilities(Integer shelterId,String providerNo) {
       // String query = "from Facility f where active=true  order by name";
       /*
    	String sql = "select p.id,p.name,p.active," +
        		"p.contact_email,p.contact_name,p.contact_phone,"+
        		"p.description,p.hic,p.lastUpdateDate,p.lastUpdateUser,p.org_id,p.sector_id"+
        		"  from facility p where p.active=1 and p.id in " + Utility.getUserOrgSqlStringByFac(providerNo, shelterId);
    	Query query = getSession().createSQLQuery(sql);
    	((SQLQuery) query).addScalar("id", Hibernate.INTEGER); 
    	((SQLQuery) query).addScalar("name", Hibernate.STRING); 
    	((SQLQuery) query).addScalar("active", Hibernate.INTEGER); 
    	((SQLQuery) query).addScalar("contact_email",Hibernate.STRING);
    	((SQLQuery) query).addScalar("contact_name",Hibernate.STRING);
    	((SQLQuery) query).addScalar("contact_phone",Hibernate.STRING);
    	((SQLQuery) query).addScalar("description",Hibernate.STRING);
    	((SQLQuery) query).addScalar("hic",Hibernate.INTEGER);
    	((SQLQuery) query).addScalar("lastUpdateDate",Hibernate.DATE);
    	((SQLQuery) query).addScalar("lastUpdateUser",Hibernate.STRING);
    	((SQLQuery) query).addScalar("org_id", Hibernate.INTEGER);
    	((SQLQuery) query).addScalar("sector_id", Hibernate.INTEGER);
    	List lst=query.list();
    	*/
    	String query = "from Facility f where active=true and f.id in "+Utility.getUserOrgStringByFac(providerNo, shelterId)+" order by name";
        return getHibernateTemplate().find(query);
    }
    public void saveFacility(Facility facility) {
        getHibernateTemplate().saveOrUpdate(facility);
        getHibernateTemplate().flush();
        getHibernateTemplate().refresh(facility);
    }
    
    public List getDistinctFacilityIdsByProgramId(int programId)
    {
        // select program_id,facility_id from room;
        
        String sqlCommand="select distinct facility_id from room where room.program_id="+programId;
        return(SqlUtils.selectIntList(sqlCommand));
    }
    
    public List getDistinctProgramIdsByFacilityId(int facilityId) {
    	String sqlCommand = "select distinct program_id from room where room.facility_id=" + facilityId;
    	return(SqlUtils.selectIntList(sqlCommand));
    }
    
   
    public static  boolean facilityHasIntersection(List providersFacilityIds, List noteFacilities) {
//        for (Integer id : noteFacilities) {
        for (int i=0;i<noteFacilities.size();i++) {
        	Integer id = (Integer) noteFacilities.get(i);
            if (providersFacilityIds.contains(id)) return(true);
        }
        
        return(false);
    }

}
