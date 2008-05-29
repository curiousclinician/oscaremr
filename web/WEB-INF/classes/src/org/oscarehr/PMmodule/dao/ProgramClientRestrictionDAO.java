package org.oscarehr.PMmodule.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.caisi.dao.DemographicDAO;
import org.oscarehr.PMmodule.model.ProgramClientRestriction;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Iterator;

/**
 */
public class ProgramClientRestrictionDAO extends HibernateDaoSupport {
    private DemographicDAO demographicDAOT;
    private ProgramDao programDao;
    private ProviderDao providerDao;

    private static Log log = LogFactory.getLog(ProgramClientRestrictionDAO.class);

    public Collection<ProgramClientRestriction> find(int programId, int demographicNo) {

        List<ProgramClientRestriction> pcrs = getHibernateTemplate().find("from ProgramClientRestriction pcr where pcr.enabled = true and pcr.programId = ? and pcr.demographicNo = ? order by pcr.startDate", new Object[]{programId, demographicNo});
        for (ProgramClientRestriction pcr : pcrs) {
            setRelationships(pcr);
        }
        return pcrs;
    }

    public void save(ProgramClientRestriction restriction) {
        getHibernateTemplate().saveOrUpdate(restriction);
    }

    public ProgramClientRestriction find(Integer restrictionId) {
        return setRelationships((ProgramClientRestriction) getHibernateTemplate().get(ProgramClientRestriction.class, restrictionId));
    }

    public Collection<ProgramClientRestriction> findForProgram(Integer programId) {
        Collection<ProgramClientRestriction> pcrs = getHibernateTemplate().find("from ProgramClientRestriction pcr where pcr.enabled = true and pcr.programId = ? order by pcr.demographicNo", programId);
        for (ProgramClientRestriction pcr : pcrs) {
            setRelationships(pcr);
        }
        return pcrs;
    }

    public Collection<ProgramClientRestriction> findDisabledForProgram(Integer programId) {
        Collection<ProgramClientRestriction> pcrs = getHibernateTemplate().find("from ProgramClientRestriction pcr where pcr.enabled = false and pcr.programId = ? order by pcr.demographicNo", programId);
        for (ProgramClientRestriction pcr : pcrs) {
            setRelationships(pcr);
        }
        return pcrs;
    }
    
    public List findAllForClient(Integer demographicNo) {      
        List results = getHibernateTemplate().find("from ProgramClientRestriction pcr where pcr.demographicNo = ? order by pcr.endDate desc", demographicNo);       
        return results;
    }
    public Collection<ProgramClientRestriction> findForClient(Integer demographicNo) {
        Collection<ProgramClientRestriction> pcrs = getHibernateTemplate().find("from ProgramClientRestriction pcr where pcr.enabled = true and pcr.demographicNo = ? order by pcr.programId", demographicNo);
        for (ProgramClientRestriction pcr : pcrs) {
            setRelationships(pcr);
        }
        return pcrs;
    }

    public Collection<ProgramClientRestriction> findForClient(Integer demographicNo, Integer facilityId) {
        ArrayList paramList = new ArrayList();
        String sSQL="from ProgramClientRestriction pcr where pcr.enabled = true and " +
  		 "pcr.demographicNo = ? and pcr.programId in (select s.id from Program s where s.facilityId = ? or s.facilityId is null) " +
         "order by pcr.programId";
          paramList.add(Integer.valueOf(demographicNo));
          paramList.add(facilityId);
          Object params[] = paramList.toArray(new Object[paramList.size()]);
          Collection<ProgramClientRestriction> pcrs= getHibernateTemplate().find(sSQL, params);
          for (ProgramClientRestriction pcr : pcrs) {
              setRelationships(pcr);
          }
          return pcrs;
/*
    	Collection<ProgramClientRestriction> pcrs = getHibernateTemplate().find("from ProgramClientRestriction pcr where pcr.enabled = true and pcr.demographicNo = ? order by pcr.programId", demographicNo);
        for (ProgramClientRestriction pcr : pcrs) {
            setRelationships(pcr);
        }
        return pcrs;
*/        
    }

    public Collection<ProgramClientRestriction> findDisabledForClient(Integer demographicNo) {
        Collection<ProgramClientRestriction> pcrs = getHibernateTemplate().find("from ProgramClientRestriction pcr where pcr.enabled = false and pcr.demographicNo = ? order by pcr.programId", demographicNo);
        for (ProgramClientRestriction pcr : pcrs) {
            setRelationships(pcr);
        }
        return pcrs;
    }

    private ProgramClientRestriction setRelationships(ProgramClientRestriction pcr) {
        pcr.setClient(demographicDAOT.getDemographic("" + pcr.getDemographicNo()));
        pcr.setProgram(programDao.getProgram(pcr.getProgramId()));
        pcr.setProvider(providerDao.getProvider(pcr.getProviderNo()));
        
        return pcr;
    }

    @Required
    public void setDemographicDAOT(DemographicDAO demographicDAOT) {
        this.demographicDAOT = demographicDAOT;
    }

    @Required
    public void setProgramDao(ProgramDao programDao) {
        this.programDao = programDao;
    }

    @Required
    public void setProviderDao(ProviderDao providerDao) {
        this.providerDao = providerDao;
    }

}
