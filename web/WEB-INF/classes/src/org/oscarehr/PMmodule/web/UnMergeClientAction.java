package org.oscarehr.PMmodule.web;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.oscarehr.PMmodule.model.ClientMerge;

import org.oscarehr.PMmodule.service.ClientManager;
import org.oscarehr.PMmodule.service.MergeClientManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.PMmodule.service.ProviderManager;

import org.oscarehr.PMmodule.web.formbean.ClientSearchFormBean;



import com.quatro.common.KeyConstants;
import com.quatro.service.LookupManager;
import com.quatro.util.Utility;

public class UnMergeClientAction extends BaseClientAction {
	
	private ClientManager clientManager;

	private ProviderManager providerManager;

	private ProgramManager programManager;	
	private LookupManager lookupManager;
	private MergeClientManager mergeClientManager;
	public ActionForward unspecified(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {	
		//return search(mapping, form, request, response);
		setLookupLists(request);
		return mergedSearch(mapping, form, request, response);
	}
	public ActionForward mergedSearch(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		DynaActionForm searchForm = (DynaActionForm) form;
		ClientSearchFormBean formBean = (ClientSearchFormBean) searchForm.get("criteria");
		/* do the search */
		request.setAttribute("mergeAction", KeyConstants.CLIENT_MODE_UNMERGE);
		request.setAttribute("clients", mergeClientManager.searchMerged(formBean));
		request.setAttribute("method", "mergedSearch");
		setLookupLists(request);

		return mapping.findForward("view");
	}
	private void setLookupLists(HttpServletRequest request) {
		Integer shelterId = (Integer) request.getSession().getAttribute(KeyConstants.SESSION_KEY_SHELTERID);
		String providerNo = (String) request.getSession().getAttribute(KeyConstants.SESSION_KEY_PROVIDERNO);
		List allBedPrograms = programManager.getBedPrograms(providerNo, shelterId);

		request.setAttribute("allBedPrograms", allBedPrograms);

		request.setAttribute("allBedPrograms", allBedPrograms);
		List allProviders = providerManager.getActiveProviders(providerNo,shelterId);
		request.setAttribute("allProviders", allProviders);
		request.setAttribute("genders", lookupManager.LoadCodeList("GEN", true,	null, null));
		request.setAttribute("moduleName", " - Client Management");		
	}
	public void setClientManager(ClientManager clientManager) {
		this.clientManager = clientManager;
	}
	public void setLookupManager(LookupManager lookupManager) {
		this.lookupManager = lookupManager;
	}
	public void setProgramManager(ProgramManager programManager) {
		this.programManager = programManager;
	}
	public void setProviderManager(ProviderManager providerManager) {
		this.providerManager = providerManager;
	}
	public void setMergeClientManager(MergeClientManager mergeClientManager) {
		this.mergeClientManager = mergeClientManager;
	}
}
