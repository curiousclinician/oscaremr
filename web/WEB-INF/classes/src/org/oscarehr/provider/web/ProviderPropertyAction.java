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
 * ProviderPropertyAction.java
 *
 * Created on December 20, 2007, 11:44 AM
 *
 *
 *
 */

package org.oscarehr.provider.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.actions.DispatchAction;
import org.oscarehr.common.dao.UserPropertyDAO;
import org.oscarehr.common.model.UserProperty;

/**
 *
 * @author rjonasz
 */
public class ProviderPropertyAction extends DispatchAction {

    private UserPropertyDAO userPropertyDAO;
    
    public void setUserPropertyDAO(UserPropertyDAO dao) {
        this.userPropertyDAO = dao;
        
    }
    
    public ActionForward unspecified(ActionMapping actionmapping,
                               ActionForm actionform,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        
        return view(actionmapping, actionform, request, response);
    }
    
    
    public ActionForward view(ActionMapping actionmapping,
                               ActionForm actionform,
                               HttpServletRequest request,
                               HttpServletResponse response) {

         DynaActionForm frm = (DynaActionForm)actionform;
         String provider = request.getParameter("provider_no");
         UserProperty prop = this.userPropertyDAO.getProp(provider, UserProperty.STALE_NOTEDATE);
         
         if( prop == null ) {             
             prop = new UserProperty();
             prop.setProvider_no(provider);
             prop.setName(UserProperty.STALE_NOTEDATE);
         }
         
         frm.set("dateProperty", prop);
         
         return actionmapping.findForward("success");
     }

    
    public ActionForward save(ActionMapping actionmapping,
                               ActionForm actionform,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        
         DynaActionForm frm = (DynaActionForm)actionform;
         UserProperty prop = (UserProperty)frm.get("dateProperty");         
         
         this.userPropertyDAO.saveProp(prop);
         
         request.setAttribute("status", "success");
         
         return actionmapping.findForward("success");
     }
    
    
    /////
    
    public ActionForward viewMyDrugrefId(ActionMapping actionmapping,
                               ActionForm actionform,
                               HttpServletRequest request,
                               HttpServletResponse response) {
                               
         DynaActionForm frm = (DynaActionForm)actionform;
         String provider = (String) request.getSession(true).getAttribute("user");
         //System.out.println("provider # "+provider);
         UserProperty prop = this.userPropertyDAO.getProp(provider, UserProperty.MYDRUGREF_ID);
         //String propertyToSet = "";
         //if( prop != null ) {             
         //   propertyToSet = prop.getValue();
         //    System.out.println("prop was not null "+prop.getValue());
         //}else{
         //    prop = new UserProperty();
         //    System.out.println("PROP WAS NULL");
         //}
         
         //request.setAttribute("propert",propertyToSet);
         request.setAttribute("dateProperty",prop);
         
         
         request.setAttribute("providertitle","provider.setmyDrugrefId.title"); //=Set myDrugref ID
         request.setAttribute("providermsgPrefs","provider.setmyDrugrefId.msgPrefs"); //=Preferences"); //
         request.setAttribute("providermsgProvider","provider.setmyDrugrefId.msgProvider"); //=myDrugref ID
         request.setAttribute("providermsgEdit","provider.setmyDrugrefId.msgEdit"); //=Enter your desired login for myDrugref
         request.setAttribute("providerbtnSubmit","provider.setmyDrugrefId.btnSubmit"); //=Save
         request.setAttribute("providermsgSuccess","provider.setmyDrugrefId.msgSuccess"); //=myDrugref Id saved
         request.setAttribute("method","saveMyDrugrefId");
         
         frm.set("dateProperty", prop);
         return actionmapping.findForward("gen");
     }

    
    public ActionForward saveMyDrugrefId(ActionMapping actionmapping,
                               ActionForm actionform,
                               HttpServletRequest request,
                               HttpServletResponse response) {
         String provider = (String) request.getSession(true).getAttribute("user");
         //System.out.println("provider # "+provider);
         DynaActionForm frm = (DynaActionForm)actionform;
         UserProperty  UdrugrefId = (UserProperty)frm.get("dateProperty");         
         String drugrefId = "";

         if (UdrugrefId != null){
             drugrefId = UdrugrefId.getValue();
         }   
         
         UserProperty prop = this.userPropertyDAO.getProp(provider, UserProperty.MYDRUGREF_ID);
         
         if (prop ==null){
             prop = new UserProperty();
             prop.setName(UserProperty.MYDRUGREF_ID);
             prop.setProvider_no(provider);
         }
         prop.setValue(drugrefId);
         
         this.userPropertyDAO.saveProp(prop);
         
         request.setAttribute("status", "success");
         request.setAttribute("dateProperty",prop);
         request.setAttribute("providertitle","provider.setmyDrugrefId.title"); //=Set myDrugref ID
         request.setAttribute("providermsgPrefs","provider.setmyDrugrefId.msgPrefs"); //=Preferences"); //
         request.setAttribute("providermsgProvider","provider.setmyDrugrefId.msgProvider"); //=myDrugref ID
         request.setAttribute("providermsgEdit","provider.setmyDrugrefId.msgEdit"); //=Enter your desired login for myDrugref
         request.setAttribute("providerbtnSubmit","provider.setmyDrugrefId.btnSubmit"); //=Save
         request.setAttribute("providermsgSuccess","provider.setmyDrugrefId.msgSuccess"); //=myDrugref Id saved
         request.setAttribute("method","saveMyDrugrefId");
         return actionmapping.findForward("gen");
     }
    /////
    
    
    
    
    /**
     * Creates a new instance of ProviderPropertyAction
     */
    public ProviderPropertyAction() {
    }
    
}
