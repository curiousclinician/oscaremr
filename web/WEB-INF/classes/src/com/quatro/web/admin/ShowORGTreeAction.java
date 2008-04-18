package com.quatro.web.admin;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.navigator.menu.MenuComponent;
import net.sf.navigator.menu.MenuRepository;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;

import com.quatro.model.LookupCodeValue;
import com.quatro.model.LookupTableDefValue;
import com.quatro.service.LookupManager;

public class ShowORGTreeAction extends DispatchAction {
	private LookupManager lookupManager = null;

	public void setLookupManager(LookupManager lookupManager) {
		this.lookupManager = lookupManager;
	}

	public ActionForward unspecified(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		return tree(mapping, form, request, response);
	}

	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		String tableId = request.getParameter("tableId");
		
		LookupTableDefValue tableDef = lookupManager.GetLookupTableDef(tableId);

		List lst = lookupManager.LoadCodeList(tableId, true, null, null);
		MenuRepository repository = setMenu(lst, request);

		DynaActionForm qform = (DynaActionForm) form;
		qform.set("tableDef", tableDef);
		qform.set("tree", repository);

		return mapping.findForward("tree");
	}

	private MenuRepository setMenu(List lst, HttpServletRequest request) {
		MenuRepository repository = new MenuRepository();
		MenuRepository defaultRepository = (MenuRepository) getServlet()
				.getServletContext().getAttribute(
						MenuRepository.MENU_REPOSITORY_KEY);
		repository.setDisplayers(defaultRepository.getDisplayers());

		for (int i = 0; i < lst.size(); i++) {
			MenuComponent mc = new MenuComponent();
			LookupCodeValue obj = (LookupCodeValue) lst.get(i);
			String name = obj.getCodeTree();
			mc.setName(name);
			String parent = null;
			if (name.length() > 8)
				parent = name.substring(0, name.length() - 8);
			if (parent != null) {
				MenuComponent parentMenu = repository.getMenu(parent);
				if (parentMenu == null) {
					parentMenu = new MenuComponent();
					parentMenu.setName(parent);
					repository.addMenu(parentMenu);
				}

				mc.setParent(parentMenu);
			}
			String title = obj.getDescription();
			mc.setTitle(title);
/*
			String location = "javascript:selectMe('" + obj.getCode() + "','"
					+ oscar.Misc.getStringJs(obj.getDescription()) + "','"
					+ request.getParameter("openerForm") + "','"
					+ request.getParameter("codeName") + "','"
					+ oscar.Misc.getStringJs(request.getParameter("descName"))
					+ "');";
*/
			String location = "";
			mc.setLocation(location);

				
			repository.addMenu(mc);
		}

		return repository;
	}
}
