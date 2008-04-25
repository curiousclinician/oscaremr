package org.oscarehr.PMmodule.dao;

import java.util.List;

import org.oscarehr.PMmodule.model.Facility;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import oscar.util.SqlUtils;

/**
 * Data access object for retrieving, creating, and updating facilities.
 */
public class FacilityDAO extends HibernateDaoSupport {

    public Facility getFacility(Integer id) {
        return (Facility) getHibernateTemplate().get(Facility.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Facility> getFacilities() {
        String query = "from Facility f";
        return getHibernateTemplate().find(query);
    }

    @SuppressWarnings("unchecked")
    public List<Facility> getActiveFacilities() {
        String query = "from Facility f where disabled=false";
        return getHibernateTemplate().find(query);
    }
        
    public void saveFacility(Facility facility) {
        getHibernateTemplate().saveOrUpdate(facility);
        getHibernateTemplate().flush();
        getHibernateTemplate().refresh(facility);
    }
    
    public List<Integer> getDistinctFacilityIdsByProgramId(int programId)
    {
        // select program_id,facility_id from room;
        
        String sqlCommand="select distinct facility_id from room where room.program_id="+programId;
        return(SqlUtils.selectIntList(sqlCommand));
    }
    
    public List<Long> getDistinctProgramIdsByFacilityId(int facilityId) {
    	String sqlCommand = "select distinct program_id from room where room.facility_id=" + facilityId;
    	return(SqlUtils.selectLongList(sqlCommand));
    }
    
   
    public static  boolean facilityHasIntersection(List<Long> providersFacilityIds, List<Long> noteFacilities) {
        for (Long id : noteFacilities) {
            if (providersFacilityIds.contains(id)) return(true);
        }
        
        return(false);
    }


}
