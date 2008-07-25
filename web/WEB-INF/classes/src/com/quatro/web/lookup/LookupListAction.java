package com.quatro.web.lookup;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.quatro.model.LookupCodeValue;
import com.quatro.model.LookupTableDefValue;
import com.quatro.service.LookupManager;
import com.quatro.util.Utility;

public class LookupListAction extends DispatchAction {
    private LookupManager lookupManager=null;
    
	public void setLookupManager(LookupManager lookupManager) {
		this.lookupManager = lookupManager;
	}

    public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		return list(mapping,form,request,response);
	}
	
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String tableId=request.getParameter("tableId");
        String parentCode =request.getParameter("parentCode");
        request.setAttribute("parentCode",parentCode);
        String grandParentCode =request.getParameter("grandParentCode");
        LookupTableDefValue tableDef = lookupManager.GetLookupTableDef(tableId);
		List lst = lookupManager.LoadCodeList(tableId, true,parentCode, null, null);
		LookupListForm qform = (LookupListForm) form;
		qform.setLookups(lst);
		qform.setTableDef(tableDef);
		return mapping.findForward("list");
	}
	
	public ActionForward search(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		LookupListForm qform = (LookupListForm) form;
        String tableId=request.getParameter("tableId");
        String parentCode =request.getParameter("parentCode");
        	if(Utility.IsEmpty(parentCode)) parentCode=qform.getParentCode();
		List lst = lookupManager.LoadCodeList(tableId, true,parentCode, null, qform.getKeywordName());
		 LookupTableDefValue tableDef = lookupManager.GetLookupTableDef(tableId);
		qform.setLookups(lst);
		qform.setTableDef(tableDef);
//		qform.setOpenerFormName(request.getParameter("openerFormName"));
//		qform.setOpenerCodeElementName(request.getParameter("openerCodeElementName"));
//		qform.setOpenerDescElementName(request.getParameter("openerDescElementName"));
		return mapping.findForward("list");
	}
	
}
