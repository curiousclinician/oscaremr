/*
 *  Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved. *
 *  This software is published under the GPL GNU General Public License.
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version. *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details. * * You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. *
 *
 *  Jason Gallagher
 *
 *  This software was written for the
 *  Department of Family Medicine
 *  McMaster University
 *  Hamilton
 *  Ontario, Canada   
 *
 * MeasurementTemplateFlowSheetConfig.java
 *
 * Created on January 28, 2006, 10:45 PM
 *
 */

package oscar.oscarEncounter.oscarMeasurements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.InitializingBean;

import oscar.oscarEncounter.oscarMeasurements.bean.EctMeasurementTypeBeanHandler;
import oscar.oscarEncounter.oscarMeasurements.data.ImportMeasurementTypes;

/**
 * @author jay
 */
public class MeasurementTemplateFlowSheetConfig implements InitializingBean {

    private static Log log = LogFactory.getLog(MeasurementTemplateFlowSheetConfig.class);

    private List flowSheets;
    
    ArrayList dxTriggers = new ArrayList();
    Hashtable dxTrigHash = new Hashtable();
    Hashtable flowsheetDisplayNames = new Hashtable();
    ArrayList universalFlowSheets = new ArrayList();

    static MeasurementTemplateFlowSheetConfig measurementTemplateFlowSheetConfig;

    Hashtable flowsheets = null;

    public void afterPropertiesSet() throws Exception {
        measurementTemplateFlowSheetConfig = this;
    }

    /**
     * Creates a new instance of MeasurementTemplateFlowSheetConfig
     */
    private MeasurementTemplateFlowSheetConfig() {
    }


    static public MeasurementTemplateFlowSheetConfig getInstance() {
        if (measurementTemplateFlowSheetConfig.flowsheets == null) {
            try {
                measurementTemplateFlowSheetConfig.loadFlowsheets();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return measurementTemplateFlowSheetConfig;
    }

    /**
     * Takes a list of Dx codes in, compares those dx codes to the dx triggers for each flowsheet and
     * then returns the appopriate flowsheet names in a ArrayList (Should this be an String array instead)?
     * Possible problems:
     * How to handle multiple coding systems?
     * How to query in an effiecent way
     * How to handle when codes have multiple flowsheets
     */
    public ArrayList getFlowsheetsFromDxCodes(Vector coll) {
        ArrayList alist = new ArrayList();

        //should i search run thru the list of possible flowsheets?
        //or should i run thru the list of dx codes for the patient?
        log.debug("Triggers size " + dxTriggers.size());
        for (int i = 0; i < dxTriggers.size(); i++) {
            String dx = (String) dxTriggers.get(i);
            log.debug("Checking dx " + dx);
            if (coll.contains(dx) && !alist.contains(dx)) {
                log.debug("coll contains " + dx);
                ArrayList flowsheets = (ArrayList) getFlowsheetForDxCode(dx);
                log.debug("Size of flowsheets for " + dx + " is " + flowsheets.size());
                for (int j = 0; j < flowsheets.size(); j++) {
                    String flowsheet = (String) flowsheets.get(j);
                    if (!alist.contains(flowsheet)) {
                        log.debug("adding flowsheet " + flowsheet);
                        alist.add(flowsheet);
                    }
                }
            }
        }
        log.debug("alist size " + alist.size());
        return alist;
    }

    public ArrayList getUniveralFlowsheets() {
        return universalFlowSheets;
    }

    public Hashtable getDxTrigHash() {
        return dxTrigHash;
    }

    public String getDisplayName(String name) {
        return (String) flowsheetDisplayNames.get(name);
    }


    void loadFlowsheets() throws FileNotFoundException {

        flowsheets = new Hashtable();
        EctMeasurementTypeBeanHandler mType = new EctMeasurementTypeBeanHandler();
        //TODO: Will change this when there are more flowsheets

//        for (File flowSheet : flowSheets) {
        for(int i =0; i<flowSheets.size(); i++) {
        	File flowSheet = (File) flowSheets.get(i);
            InputStream is = new FileInputStream(flowSheet);
            MeasurementFlowSheet d = createflowsheet(mType, is);
            flowsheets.put(d.getName(), d);
            if (d.isUniversal())
                universalFlowSheets.add(d.getName());
            else {
                String[] dxTrig = d.getDxTriggers();
                addTriggers(dxTrig, d.getName());
            }

            flowsheetDisplayNames.put(d.getName(), d.getDisplayName());
        }
    }

    public ArrayList getFlowsheetForDxCode(String code) {
        return (ArrayList) dxTrigHash.get(code);
    }

    private void addTriggers(String[] dxTrig, String name) {
        if (dxTrig != null) {
//            for (String aDxTrig : dxTrig) {
        	for(int i=0;  i<dxTrig.length; i++) {
        		String aDxTrig = dxTrig[i];
                if (!dxTriggers.contains(aDxTrig)) {
                    dxTriggers.add(aDxTrig);
                }
                if (dxTrigHash.containsKey(aDxTrig)) {
                    ArrayList l = (ArrayList) dxTrigHash.get(aDxTrig);
                    if (!l.contains(name)) {
                        l.add(name);
                    }
                } else {
                    ArrayList l = new ArrayList();
                    l.add(name);
                    dxTrigHash.put(aDxTrig, l);
                }
            }
        }
    }


    private MeasurementFlowSheet createflowsheet(final EctMeasurementTypeBeanHandler mType, InputStream is) {
        MeasurementFlowSheet d = new MeasurementFlowSheet();

        try {
            SAXBuilder parser = new SAXBuilder();
            Document doc = parser.build(is);
            Element root = doc.getRootElement();

            //MAKE SURE ALL MEASUREMENTS HAVE BEEN INITIALIZED
            ImportMeasurementTypes importMeasurementTypes = new ImportMeasurementTypes();
            importMeasurementTypes.importMeasurements(root);

            List indi = root.getChildren("indicator"); // key="LOW" colour="blue">
            for (int i = 0; i < indi.size(); i++) {
                Element e = (Element) indi.get(i);
                d.AddIndicator(e.getAttributeValue("key"), e.getAttributeValue("colour"));
            }
            List items = root.getChildren("item");
//            for (Element e : items) {
            for(int i=0; i<items.size();i++){
            	Element e = (Element) items.get(i);
                List attr = e.getAttributes();
                Hashtable h = new Hashtable();
                //for (Attribute att : attr) {
                for(int j=0; j<attr.size(); j++) {
                	Attribute att = (Attribute) attr.get(j);
                    h.put(att.getName(), att.getValue());
                    //System.out.print(att.getName()+" "+att.getValue() );
                }

                if (h.get("measurement_type") != null) {
                    log.debug("ADDING " + h.get("measurement_type"));
                    d.addMeasurement("" + h.get("measurement_type"));
                    d.addMeasurementInfo("" + h.get("measurement_type"), mType.getMeasurementType("" + h.get("measurement_type")));
                    d.addMeasurementFlowSheetInfo("" + h.get("measurement_type"), h);
                } else {
                    d.addMeasurement("" + h.get("prevention_type"));
                    d.addMeasurementFlowSheetInfo("" + h.get("prevention_type"), h);
                }
                //prevList.add(h);
                //prevHash.put(h.get("name"), h);
            }
            if (root.getAttribute("name") != null) {
                d.setName(root.getAttribute("name").getValue());
            }
            if (root.getAttribute("display_name") != null) {
                d.setDisplayName(root.getAttribute("display_name").getValue());
            }
            
            if (root.getAttribute("top_HTML") != null) {
                d.setTopHTMLFileName(root.getAttribute("top_HTML").getValue());
            }           
            
            if (root.getAttribute("ds_rules") != null) {
                d.loadRuleBase(root.getAttribute("ds_rules").getValue());
            }
            if (root.getAttribute("dxcode_triggers") != null) {
                d.parseDxTriggers(root.getAttribute("dxcode_triggers").getValue());
            }

            if (root.getAttribute("warning_colour") != null) {
                d.setWarningColour(root.getAttribute("warning_colour").getValue());
            }
            if (root.getAttribute("recommendation_colour") != null) {
                d.setRecommendationColour(root.getAttribute("recommendation_colour").getValue());
            }
            if (root.getAttribute("is_universal") != null) {
                d.setUniversal("true".equals(root.getAttribute("is_universal").getValue()));
            }
            if (root.getAttribute("is_medical") != null) {
                d.setMedical("true".equals(root.getAttribute("is_medical").getValue()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return d;
    }

    public MeasurementFlowSheet getFlowSheet(String flowsheetName) {
        return (MeasurementFlowSheet) flowsheets.get(flowsheetName);
    }

    public List getFlowSheets() {
        return flowSheets;
    }

    public void setFlowSheets(List flowSheets) {
        this.flowSheets = flowSheets;
    }
}

//        d.addMeasurement("REBG");
//        d.addMeasurementInfo("REBG",mType.getMeasurementType("REBG"));
//        d.addMeasurement("A1C");
//        d.addMeasurementInfo("A1C",mType.getMeasurementType("A1C"));
//        d.addMeasurement("DMED");
//        d.addMeasurementInfo("DMED",mType.getMeasurementType("DMED"));
//        d.addMeasurement("BP");
//        d.addMeasurementInfo("BP",mType.getMeasurementType("BP"));
//        d.addMeasurement("BMED");
//        d.addMeasurementInfo("BMED",mType.getMeasurementType("BMED"));
//        d.addMeasurement("WT");
//        d.addMeasurementInfo("WT",mType.getMeasurementType("WT"));
////        d.addMeasurement("DIET");
////        d.addMeasurementInfo("DIET",mType.getMeasurementType("DIET"));
////        d.addMeasurement("EXE");
////        d.addMeasurementInfo("EXE",mType.getMeasurementType("EXE"));
//        
//        d.addMeasurement("DIER");
//        d.addMeasurementInfo("DIER",mType.getMeasurementType("DIER"));
//        
//        d.addMeasurement("NOSK");
//        d.addMeasurementInfo("NOSK",mType.getMeasurementType("NOSK"));
//        d.addMeasurement("VMED");
//        d.addMeasurementInfo("VMED",mType.getMeasurementType("VMED"));
//        d.addMeasurement("LDL");
//        d.addMeasurementInfo("LDL",mType.getMeasurementType("LDL"));
//        d.addMeasurement("TCHD");
//        d.addMeasurementInfo("TCHD",mType.getMeasurementType("TCHD"));
//        d.addMeasurement("LMED");
//        d.addMeasurementInfo("LMED",mType.getMeasurementType("LMED"));
//        d.addMeasurement("FGLC");
//        d.addMeasurementInfo("FGLC",mType.getMeasurementType("FGLC"));
//        d.addMeasurement("EYEE");
//        d.addMeasurementInfo("EYEE",mType.getMeasurementType("EYEE"));
//        d.addMeasurement("ACR");
//        d.addMeasurementInfo("ACR",mType.getMeasurementType("ACR"));
//        d.addMeasurement("EGFR");
//        d.addMeasurementInfo("EGFR",mType.getMeasurementType("EGFR"));
//
//        d.addMeasurement("FOTE");
//        d.addMeasurementInfo("FOTE",mType.getMeasurementType("FOTE"));
//        d.addMeasurement("FEET");
//        d.addMeasurementInfo("FEET",mType.getMeasurementType("FEET"));
//
//        d.addMeasurement("SEXF");
//        d.addMeasurementInfo("SEXF",mType.getMeasurementType("SEXF"));
//        d.addMeasurement("DMME");
//        d.addMeasurementInfo("DMME",mType.getMeasurementType("DMME"));
//        d.addMeasurement("FLUS");
//        d.addMeasurementInfo("FLUS",mType.getMeasurementType("FLUS")); 
//        d.addMeasurement("PNEU");
//        d.addMeasurementInfo("PNEU",mType.getMeasurementType("PNEU"));
