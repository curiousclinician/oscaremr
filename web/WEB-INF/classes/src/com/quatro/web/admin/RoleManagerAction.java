package com.quatro.web.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;
import org.oscarehr.PMmodule.service.LogManager;
import org.oscarehr.PMmodule.web.BaseAction;

import com.quatro.model.DataViews;
import com.quatro.model.LookupCodeValue;
import com.quatro.model.ReportFilterValue;
import com.quatro.model.ReportTempCriValue;
import com.quatro.model.ReportValue;
import com.quatro.model.security.Secobjprivilege;

import com.quatro.model.security.Secrole;
import com.quatro.service.LookupManager;
import com.quatro.service.security.RolesManager;

public class RoleManagerAction extends BaseAction {

	private LogManager logManager;

	private RolesManager rolesManager;

	private LookupManager lookupManager;

	public void setLookupManager(LookupManager lookupManager) {
		this.lookupManager = lookupManager;
	}

	public void setLogManager(LogManager mgr) {
		this.logManager = mgr;
	}

	public void setRolesManager(RolesManager rolesManager) {
		this.rolesManager = rolesManager;
	}

	@Override
	public ActionForward unspecified(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		return list(mapping, form, request, response);
	}

	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		List<Secrole> list = null;
		list = rolesManager.getRoles();

		request.setAttribute("roles", list);
		logManager.log("read", "full roles list", "", request);

		return mapping.findForward("list");

	}

	@SuppressWarnings("unchecked")
	public ActionForward edit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		System.out.println("=========== EDIT ========= in RoleManagerAction");

		DynaActionForm secroleForm = (DynaActionForm) form;

		String roleNo = request.getParameter("roleNo");

		if (isCancelled(request)) {
			return list(mapping, form, request, response);
		}

		if (roleNo != null) {
			Secrole secrole = rolesManager.getRole(roleNo);

			if (secrole == null) {
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"role.missing"));
				saveMessages(request, messages);

				return list(mapping, form, request, response);
			}

			secroleForm.set("roleNo", secrole.getRoleNo());
			secroleForm.set("roleName", secrole.getRoleName());
			secroleForm.set("description", secrole.getDescription());
			request.setAttribute("secroleForEdit", secrole);

		
////////
			ArrayList<Secobjprivilege> secobjprivilegeLst = new ArrayList<Secobjprivilege>();
			ArrayList funLst = new ArrayList();
			funLst = (ArrayList)rolesManager.getFunctions(secrole.getRoleName());
			
			
			
			int lineno = funLst.size();
					
			for (int i = 0; i < lineno; i++) {
								
				Secobjprivilege objNew = (Secobjprivilege) funLst.get(i);

				String accessType_code = objNew.getPrivilege();
				String accessType_description = rolesManager.getAccessDesc(accessType_code );
				String function_code = objNew.getObjectname();
				String function_description = rolesManager.getFunctionDesc(function_code );

				if (accessType_code != null)
					objNew.setPrivilege_code(accessType_code);
				if (accessType_description != null)
					objNew.setPrivilege(accessType_description);
				if (function_code != null)
					objNew.setObjectname_code(function_code);
				if (function_description != null)
					objNew.setObjectname_desc(function_description);
				else
					objNew.setObjectname_desc(function_code);

				secobjprivilegeLst.add(objNew);
	
					
			
			}
			secroleForm.set("secobjprivilegeLst", secobjprivilegeLst);
			
/////////	
		}
		return mapping.findForward("edit");

	}

	public ActionForward saveNew(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		ActionMessages messages = new ActionMessages();
        boolean isError = false;
        boolean isWarning = false;
        
		DynaActionForm secroleForm = (DynaActionForm) form;

		Secrole secrole = new Secrole();
		secrole.setRoleNo((Integer) secroleForm.get("roleNo"));
		String roleName = (String) secroleForm.get("roleName");
		secrole.setRoleName(roleName);
		secrole.setDescription((String) secroleForm.get("description"));

		// check rolename, should be unique
		Secrole existRole = rolesManager.getRoleByRolename(roleName);

		if (existRole == null) {
			rolesManager.save(secrole);

			secroleForm.set("roleNo", secrole.getRoleNo());

			LookupCodeValue functions = new LookupCodeValue();
			secroleForm.set("functions", functions);

			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.role.created",
           			request.getContextPath(), roleName));
			saveMessages(request,messages);
			
			return addFunction(mapping, form, request, response);//mapping.findForward("functions");
		} else {
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.role.exist",
           			request.getContextPath(), roleName));
			saveMessages(request,messages);
			
			return mapping.findForward("edit");

		}

	}

	public ActionForward saveChange(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		System.out
				.println("=========== SAVE Change ========= in RoleManagerAction");
		ActionMessages messages = new ActionMessages();
		DynaActionForm secroleForm = (DynaActionForm) form;

		Secrole secrole = new Secrole();
		secrole.setRoleNo((Integer) secroleForm.get("roleNo"));
		String roleName = (String) secroleForm.get("roleName");
		secrole.setRoleName(roleName);
		secrole.setDescription((String) secroleForm.get("description"));
		try{
			rolesManager.save(secrole);
			saveFunctions(mapping, form, request, response);
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.save.success",
			request.getContextPath()));
			saveMessages(request,messages);			
		}catch(Exception e){
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.save.failed",
			request.getContextPath()));
			saveMessages(request,messages);				
		}
		secroleForm.set("roleNo", secrole.getRoleNo());

		LookupCodeValue functions = new LookupCodeValue();
		secroleForm.set("functions", functions);

		return edit(mapping, form, request, response);

	}

	public ActionForward preNew(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		System.out.println("=========== preNew ========= in RoleManagerAction");

		return mapping.findForward("edit");

	}

	public ActionForward addFunction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		System.out
				.println("=========== addFunction ========= in RoleManagerAction");
		DynaActionForm secroleForm = (DynaActionForm) form;
		ChangeFunLstTable(2, secroleForm, request);

		return mapping.findForward("functions");

	}
	public ActionForward addFunctionInEdit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		System.out
				.println("=========== addFunction ========= in RoleManagerAction");
		DynaActionForm secroleForm = (DynaActionForm) form;
		ChangeFunLstTable(2, secroleForm, request);
		request.setAttribute("secroleForEdit", "flag");
		return mapping.findForward("edit");

	}

	public ActionForward removeFunction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		System.out
				.println("=========== removeFunction ========= in RoleManagerAction");
		DynaActionForm secroleForm = (DynaActionForm) form;
		ChangeFunLstTable(1, secroleForm, request);

		return mapping.findForward("functions");

	}
	public ActionForward removeFunctionInEdit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		System.out
				.println("=========== removeFunction ========= in RoleManagerAction");
		DynaActionForm secroleForm = (DynaActionForm) form;
		ChangeFunLstTable(1, secroleForm, request);
		request.setAttribute("secroleForEdit", "flag");
		
		return mapping.findForward("edit");

	}

	public void ChangeFunLstTable(int operationType, DynaActionForm myForm,
			HttpServletRequest request) {
		
		ActionMessages messages = new ActionMessages();
		
		ArrayList<Secobjprivilege> secobjprivilegeLst = new ArrayList<Secobjprivilege>();

		

		Map map = request.getParameterMap();
		String[] arr_lineno = (String[]) map.get("lineno");
		int lineno = 0;
		if (arr_lineno != null)
			lineno = arr_lineno.length;

		switch (operationType) {
		case 1: // remove
			for (int i = 0; i < lineno; i++) {
				String[] isChecked = (String[]) map.get("p" + i);

				if (isChecked == null) {
					Secobjprivilege objNew = new Secobjprivilege();
					
					String[] accessType_code = (String[]) map
							.get("accessTypes_code" + i);
					String[] accessType_description = (String[]) map
							.get("accessTypes_description" + i);
					String[] function_code = (String[]) map.get("function_code"
							+ i);
					String[] function_description = (String[]) map
							.get("function_description" + i);

					if (accessType_code != null)
						objNew.setPrivilege_code(accessType_code[0]);
					if (accessType_description != null)
						objNew.setPrivilege(accessType_description[0]);
					if (function_code != null)
						objNew.setObjectname_code(function_code[0]);
					if (function_description != null)
						objNew.setObjectname_desc(function_description[0]);

					secobjprivilegeLst.add(objNew);

				}

			}
			break;
		case 2: // add
			for (int i = 0; i < lineno; i++) {
				Secobjprivilege objNew = new Secobjprivilege();

				String[] accessType_code = (String[]) map
						.get("accessTypes_code" + i);
				String[] accessType_description = (String[]) map
						.get("accessTypes_description" + i);
				String[] function_code = (String[]) map
						.get("function_code" + i);
				String[] function_description = (String[]) map
						.get("function_description" + i);

				if (accessType_code != null)
					objNew.setPrivilege_code(accessType_code[0]);
				if (accessType_description != null)
					objNew.setPrivilege(accessType_description[0]);
				if (function_code != null)
					objNew.setObjectname_code(function_code[0]);
				if (function_description != null)
					objNew.setObjectname_desc(function_description[0]);

				secobjprivilegeLst.add(objNew);
			}
			Secobjprivilege objNew2 = new Secobjprivilege();
			secobjprivilegeLst.add(objNew2);
			break;

		}
		myForm.set("secobjprivilegeLst", secobjprivilegeLst);
//		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("message.role.function.add",
//       			request.getContextPath()));
//		saveMessages(request,messages);
		
		

	}

	public ActionForward saveFunction(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		System.out
				.println("=========== saveFunction ========= in RoleManagerAction");
		saveFunctions( mapping, form, request, response);
		
		return list(mapping, form, request, response);

	}
	public void saveFunctions(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		DynaActionForm secroleForm = (DynaActionForm) form;

		String roleName = (String) secroleForm.get("roleName");
		String providerNo = (String) request.getSession().getAttribute("user");
		
		Map map = request.getParameterMap();
		String[] arr_lineno = (String[]) map.get("lineno");
		int lineno = 0;
		if (arr_lineno != null)
			lineno = arr_lineno.length;
		ArrayList listForSave = new ArrayList();
		for (int i = 0; i < lineno; i++) {
			String[] function_code = (String[]) map.get("function_code" + i);
			if (function_code != null && function_code[0].length() > 0) {
				Secobjprivilege objNew = new Secobjprivilege();
				objNew.setObjectname(function_code[0]);
				objNew.setRoleusergroup(roleName);

				String[] accessType_code = (String[]) map
						.get("accessTypes_code" + i);
				if (accessType_code != null)
					objNew.setPrivilege(accessType_code[0]);
				
				objNew.setProviderNo(providerNo);
				objNew.setPriority(new Integer("0"));

				//rolesManager.saveFunction(objNew);
				listForSave.add(objNew);
			}
		}
		rolesManager.saveFunctions(listForSave, roleName);

		
	}

}