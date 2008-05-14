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
* This software was written for 
* Centre for Research on Inner City Health, St. Michael's Hospital, 
* Toronto, Ontario, Canada 
*/

package org.caisi.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.caisi.dao.CaisiEditorDAO;
import org.caisi.model.CaisiEditor;

public class CaisiEditorManager {
    private static Log log = LogFactory.getLog(CaisiEditorManager.class);
    private CaisiEditorDAO dao;

    public void setCaisiEditorDAO(CaisiEditorDAO dao) {
        this.dao = dao;
    }

    public List getCaisiEditors() {
        return dao.getCaisiEditors();
    }

    public CaisiEditor getCaisiEditor(String CaisiEditorId) {
        CaisiEditor CaisiEditor = dao.getCaisiEditor(Integer.valueOf(CaisiEditorId));
        if (CaisiEditor == null) {
            log.warn("UserId '" + CaisiEditorId + "' not found in database.");
        }
        return CaisiEditor;
    }

    public List getActiveLabelValue(String category, String label) {
    	return dao.getActiveLabelValue(category, label);
    }
    
    public List getActiveLabelValue(String label) {
    	List results = new ArrayList();
    	List values = dao.getActiveLabelValue(label);
    	for(Iterator it = values.iterator(); it.hasNext();) {
    		CaisiEditor ce = (CaisiEditor) it.next();
    		results.add(ce.getLabelValue());
    	}
    	return results;
    }
    
    public List getActiveLabelCaisiEditor(String label) {
    	List<CaisiEditor> results = new ArrayList<CaisiEditor>();
    	List values = dao.getActiveLabelValue(label);
    	for(Iterator it = values.iterator(); it.hasNext();) {
    		CaisiEditor ce = (CaisiEditor) it.next();
    		results.add(ce);
    	}
    	return results;
    }
    
    public CaisiEditor saveCaisiEditor(CaisiEditor CaisiEditor) {
        dao.saveCaisiEditor(CaisiEditor);
        return CaisiEditor;
    }

    public void removeCaisiEditor(String CaisiEditorId) {
        dao.removeCaisiEditor(Integer.valueOf(CaisiEditorId));
    }
}
