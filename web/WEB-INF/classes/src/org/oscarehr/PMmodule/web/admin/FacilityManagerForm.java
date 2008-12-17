package org.oscarehr.PMmodule.web.admin;

import org.apache.struts.action.ActionForm;
import org.oscarehr.PMmodule.model.Facility;

/**
 */
public class FacilityManagerForm extends ActionForm {
    private Facility facility;

    public Facility getFacility() {
    	if(facility == null) facility = new Facility();
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }
}
