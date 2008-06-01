package org.oscarehr.PMmodule.dao;

import java.io.Serializable;

import org.oscarehr.PMmodule.model.Bed;
import org.oscarehr.common.dao.AuditStrategyFactory;
import org.oscarehr.common.dao.AuditableEvent;
import org.oscarehr.common.dao.BaseAuditStrategy;

public class BedAuditStrategy extends BaseAuditStrategy {

	public void registerEvents() {
		AuditStrategyFactory.register(Bed.class, AuditableEvent.CREATE, this);
		AuditStrategyFactory.register(Bed.class, AuditableEvent.UPDATE, this);
		AuditStrategyFactory.register(Bed.class, AuditableEvent.DELETE, this);
	}

	public void auditCreate(Object entity, Serializable id, Object[] currentState, String[] propertyNames) {
		System.out.println("BedDemographicAuditStrategy.auditCreate()");
	}

	public void auditUpdate(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames) {
		System.out.println("BedDemographicAuditStrategy.auditUpdate()");
	}

	public void auditDelete(Object entity, Serializable id, Object[] state, String[] propertyNames) {
		System.out.println("BedDemographicAuditStrategy.auditDelete()");
	}

}