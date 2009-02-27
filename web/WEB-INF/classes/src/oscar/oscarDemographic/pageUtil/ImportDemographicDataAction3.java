/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved. *
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
 * Jason Gallagher
 *
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada   Creates a new instance of ImportDemographicDataAction
 *
 *
 * * ImportDemographicDataAction2.java
 *
 * Created on Oct 2, 2007
 */

package oscar.oscarDemographic.pageUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.jdom.Element;
import org.oscarehr.casemgmt.model.CaseManagementIssue;
import org.oscarehr.casemgmt.model.CaseManagementNote;
import org.oscarehr.casemgmt.model.CaseManagementNoteExt;
import org.oscarehr.casemgmt.model.CaseManagementNoteLink;
import org.oscarehr.casemgmt.model.Issue;
import org.oscarehr.casemgmt.service.CaseManagementManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import oscar.appt.ApptStatusData;
import oscar.dms.EDocUtil;
import oscar.oscarDemographic.data.DemographicData;
import oscar.oscarDemographic.data.DemographicExt;
import oscar.oscarDemographic.data.DemographicRelationship;
import oscar.oscarLab.ca.on.LabResultImport;
import oscar.oscarPrevention.PreventionData;
import oscar.oscarProvider.data.ProviderData;
import oscar.oscarRx.data.RxAllergyImport;
import oscar.oscarRx.data.RxPrescriptionImport;
import oscar.service.OscarSuperManager;
import oscar.util.UtilDateUtilities;

/**
 *
 * @author Ronnie Cheng
 */
public class ImportDemographicDataAction3 extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception  {
       
	String proNo = (String) request.getSession().getAttribute("user");
	String tmpDir = oscar.OscarProperties.getInstance().getProperty("TMP_DIR");
	File importLog = null;
	if (!filled(tmpDir)) {
	    throw new Exception("Temporary Import Directory not set! Check oscar.properties.");
	} else {
	    if (tmpDir.charAt(tmpDir.length()-1)!='/') tmpDir = tmpDir + '/';
	}
        ImportDemographicDataForm frm = (ImportDemographicDataForm) form; 
        FormFile imp = frm.getImportFile();
	String ifile = tmpDir + imp.getFileName();
	
        ArrayList warnings = new ArrayList();
	try {
	    InputStream is = imp.getInputStream();
	    OutputStream os = new FileOutputStream(ifile);
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len=is.read(buf)) > 0) os.write(buf,0,len);
	    is.close();
	    os.close();
	    
	    Vector logResult = new Vector();
	    if (ifile.substring(ifile.length()-3).equals("zip")) {
		ZipInputStream in = new ZipInputStream(new FileInputStream(ifile));
		boolean noXML = true;
		ZipEntry entry = in.getNextEntry();
		
		while (entry!=null) {
		    String ofile = tmpDir + entry.getName();
		    if (ofile.substring(ofile.length()-3).equals("xml")) {
			noXML = false;
			OutputStream out = new FileOutputStream(ofile);
			while ((len=in.read(buf)) > 0) out.write(buf,0,len);
			out.close();
			String[] logR = importXML(ofile, proNo, warnings, request);
			if (logR!=null) logResult.add(logR);
		    }
		    entry = in.getNextEntry();
		}
		if (noXML) {
		    cleanFile(ifile);
		    throw new Exception ("Error! No XML file in zip");
		} else {
		    if (logResult.size()>0) importLog = makeImportLog(logResult, tmpDir);
		}
		in.close();
		if (!cleanFile(ifile)) throw new Exception ("Error! Cannot delete import file!");

	    } else if (ifile.substring(ifile.length()-3).equals("xml")) {
		String[] logR = importXML(ifile, proNo, warnings, request);
		if (logR!=null) {
		    logResult.add(logR);
		    if (logResult.size()>0) importLog = makeImportLog(logResult, tmpDir);
		}
		
	    } else {
		cleanFile(ifile);
		throw new Exception ("Error! Import file must be XML or ZIP");
	    }

	} catch (Exception e) {
	    warnings.add("Error processing file: " + imp.getFileName());
	    e.printStackTrace();
	}
        request.setAttribute("warnings",warnings);
	if (importLog!=null) request.setAttribute("importlog",importLog.getPath());
        
        System.out.println("warnings size "+warnings.size());
        for( int i = 0; i < warnings.size(); i++ ){
           String str = (String) warnings.get(i);
           System.out.println(str);
        }
        return mapping.findForward("success");
    }
    
    String[] importXML(String xmlFile, String proNum, ArrayList errWarnings, HttpServletRequest req) {
	String demoNo="", dataGood="Yes", summaryGood="Yes", otherGood="Yes", errorImport="";
	DemographicData.DemographicAddResult demoRes = null;
	try {
	    File xmlF = new File(xmlFile);

	    cds.OmdCdsDocument omdCdsDoc = cds.OmdCdsDocument.Factory.parse(xmlF);
	    cds.OmdCdsDocument.OmdCds omdCds = omdCdsDoc.getOmdCds();
	    cds.PatientRecordDocument.PatientRecord patientRec = omdCds.getPatientRecord();
	    
	    //DEMOGRAPHICS
	    cds.DemographicsDocument.Demographics demo = patientRec.getDemographics();
	    cdsDt.PersonNameStandard.LegalName legalName = demo.getNames().getLegalName();
	    String lastName="", firstName="";
	    if (legalName!=null) {
		if (legalName.getLastName()!=null) lastName = filledOrEmpty(legalName.getLastName().getPart());
		if (legalName.getFirstName()!=null) firstName = filledOrEmpty(legalName.getFirstName().getPart());
	    } else {
		dataGood = "No";
		appendLine(errorImport,"No Legal Name");
	    }
	    String title = demo.getNames().getNamePrefix()!=null ? demo.getNames().getNamePrefix().toString() : "";
	    String sex = demo.getGender()!=null ? demo.getGender().toString() : "";
	    if (!filled(sex)) {
		dataGood = "No";
		appendLine(errorImport,"No Gender");
	    }
	    String birthDate = getDateFullPartial(demo.getDateOfBirth());
	    if (!filled(birthDate)) {
		birthDate = "0001-01-01";
		dataGood = "No";
		appendLine(errorImport,"No Date Of Birth");
	    }
	    String roster_status = demo.getEnrollmentStatus()!=null ? demo.getEnrollmentStatus().toString() : "";
	    if	    (roster_status.equals("1")) roster_status = "RO";
	    else if (roster_status.equals("0")) roster_status = "NR";
	    else {
		dataGood = "No";
		appendLine(errorImport,"No Enrollment Status");
	    }
	    String patient_status = demo.getPersonStatusCode()!=null ? demo.getPersonStatusCode().toString() : "";
	    if	    (patient_status.equals("A")) patient_status = "AC";
	    else if (patient_status.equals("I")) patient_status = "IN";
	    else if (patient_status.equals("D")) patient_status = "DE";
	    else if (patient_status.equals("O")) patient_status = "OTHER";
	    else {
		dataGood = "No";
		appendLine(errorImport,"No Person Status Code");
	    }
	    String date_joined = getDateFullPartial(demo.getEnrollmentDate());
	    String end_date = getDateFullPartial(demo.getEnrollmentTerminationDate());
	    String sin = filledOrEmpty(demo.getSIN());
	    String dNote = "";
	    appendLine(dNote, "SIN: ", sin);
	    
	    String chart_no = filledOrEmpty(demo.getChartNumber());
	    String preferred_lang = filledOrEmpty(demo.getPreferredSpokenLanguage());
	    preferred_lang = preferred_lang.equals("ENG") ? "English" : preferred_lang;
	    preferred_lang = preferred_lang.equals("FRE") ? "French" : preferred_lang;
	    String uvID = filledOrEmpty(demo.getUniqueVendorIdSequence());
	    if (filled(uvID)) {
		if (!filled(chart_no)) {
		    chart_no = uvID;
		    appendLine(errorImport,"Note: Unique Vendor Id imported as [chart_no]");
		} else {
		    appendLine(dNote, "Unique Vendor ID: ", uvID);
		}
	    }
	    String versionCode="", hin="", hc_type="", eff_date="";
	    cdsDt.HealthCard healthCard = demo.getHealthCard();
	    if (healthCard!=null) {
		hin = filledOrEmpty(healthCard.getNumber());
		if (hin.equals("")) {
		    dataGood = "No";
		    appendLine(errorImport,"No health card number!");
		}
		hc_type = healthCard.getProvinceCode()!=null ? healthCard.getProvinceCode().toString() : "";
		if (hc_type.equals("")) {
		    dataGood = "No";
		    appendLine(errorImport,"No Province Code for health card!");
		}
		versionCode = filledOrEmpty(healthCard.getVersion());
		eff_date = getCalDate(healthCard.getExpirydate());
		if (eff_date.equals("")) {
		    String errMsg = "Error! Invalid health card expiry date!";
		    appendLine(errorImport, errMsg);
		    errWarnings.add(errMsg);
		}
	    }
	    String address="", city="", province="", postalCode="";
	    if (demo.getAddressArray().length>0) {
		cdsDt.Address addr = demo.getAddressArray(0);	//only get 1st address, other ignored
		if (addr!=null) {
		    if (filled(addr.getFormatted())) {
			address = addr.getFormatted();
		    } else {
			cdsDt.AddressStructured addrStr = addr.getStructured();
			if (addrStr!=null) {
			    address = filledOrEmpty(addrStr.getLine1()) + filledOrEmpty(addrStr.getLine2()) + filledOrEmpty(addrStr.getLine3());
			    city = filledOrEmpty(addrStr.getCity());
			    province = filledOrEmpty(addrStr.getCountrySubdivisionCode());
			    cdsDt.PostalZipCode postalZip = addrStr.getPostalZipCode();
			    if (postalZip!=null) postalCode = filledOrEmpty(postalZip.getPostalCode());
			}
		    }	    
		}
	    }
	    cdsDt.PhoneNumber[] pn = demo.getPhoneNumberArray();
	    String workPhone="", workExt="", homePhone="", homeExt="", cellPhone="", ext="", patientPhone="";
	    for (int i=0; i<pn.length; i++) {
		String phone = pn[i].getPhoneNumber();
		if (!filled(phone)) {
		    if (filled(pn[i].getNumber())) {
			String areaCode = filled(pn[i].getAreaCode()) ? "("+pn[i].getAreaCode()+")" : "";
			phone = areaCode + pn[i].getNumber();
		    }
		}
		if (filled(phone)) {
		    if (filled(pn[i].getExtension())) ext = pn[i].getExtension();
		    else if (filled(pn[i].getExchange())) ext = pn[i].getExchange();
		    
		    if (pn[i].getPhoneNumberType()==cdsDt.PhoneNumberType.W) {
			workPhone = phone;
			workExt   = ext;		    
		    } else if (pn[i].getPhoneNumberType()==cdsDt.PhoneNumberType.R) {
			homePhone = phone;
			homeExt   = ext;
		    } else if (pn[i].getPhoneNumberType()==cdsDt.PhoneNumberType.C) {
			cellPhone = phone;
		    }
		}
	    }
	    if (demo.getPreferredPhone()!=null) {
		if (demo.getPreferredPhone()==cdsDt.PhoneNumberType.R) {
		    if (filled(homePhone)) patientPhone = homePhone+" "+homeExt;
		}
		if (demo.getPreferredPhone()==cdsDt.PhoneNumberType.W) {
		    if (filled(workPhone)) patientPhone = workPhone+" "+workExt;
		}
		if (demo.getPreferredPhone()==cdsDt.PhoneNumberType.C) {
		    if (filled(cellPhone)) patientPhone = cellPhone;
		}
	    } else {
		if      (filled(homePhone)) patientPhone = homePhone+" "+homeExt;
		else if (filled(workPhone)) patientPhone = workPhone+" "+workExt;
		else if (filled(cellPhone)) patientPhone = cellPhone;
	    }
	    String email = filledOrEmpty(demo.getEmail());
	    
	    String primaryPhysician = "";
	    if (demo.getPrimaryPhysician()!=null) {
		String[] personName = getPersonName(demo.getPrimaryPhysician().getName());
		String personOHIP = demo.getPrimaryPhysician().getOHIPPhysicianId();
		primaryPhysician = writeProviderData(personName, personOHIP);
	    } else {
		String errMsg = "Error! No Primary Physician!";
		appendLine(errorImport, errMsg);
		errWarnings.add(errMsg);
	    }
	    
	    Date bDate = UtilDateUtilities.StringToDate(birthDate,"yyyy-MM-dd");
	    String year_of_birth = UtilDateUtilities.DateToString(bDate,"yyyy");
	    String month_of_birth = UtilDateUtilities.DateToString(bDate,"MM");
	    String date_of_birth = UtilDateUtilities.DateToString(bDate,"dd");
	    
	    DemographicData dd = new DemographicData();
	    DemographicExt dExt = new DemographicExt();
	    demoRes = dd.addDemographic(title,lastName, firstName, address, city, province, postalCode, homePhone, workPhone,
					year_of_birth, month_of_birth, date_of_birth, hin, versionCode, roster_status, patient_status,
					date_joined, chart_no, preferred_lang, primaryPhysician, sex, end_date, eff_date,
					""/*pcn_indicator*/, hc_type, ""/*hc_renew_date*/, ""/*family_doctor*/, email, ""/*pin*/, 
					""/*alias*/, ""/*previousAddress*/, ""/*children*/, ""/*sourceOfIncome*/, ""/*citizenship*/, sin);
	    demoNo = demoRes.getId();
	    if (demoNo!=null)
	    {
		if (filled(dNote)) dd.addDemographiccust(demoNo, dNote);
		
		if (!workExt.equals("")) dExt.addKey(primaryPhysician, demoNo, "wPhoneExt", workExt);
		if (!homeExt.equals("")) dExt.addKey(primaryPhysician, demoNo, "hPhoneExt", homeExt);
		if (!cellPhone.equals("")) dExt.addKey(primaryPhysician, demoNo, "demo_cell", cellPhone);

		cds.DemographicsDocument.Demographics.Contact[] contt = demo.getContactArray();
		for (int i=0; i<contt.length; i++) {
		    String[] contactName = getPersonName(contt[i].getName());
		    String cFirstName = contactName[0];
		    String cLastName  = contactName[1];
		    String cEmail = filledOrEmpty(contt[i].getEmailAddress());

		    pn = contt[i].getPhoneNumberArray();
		    workPhone=""; workExt=""; homePhone=""; homeExt=""; cellPhone=""; ext="";
		    for (int j=0; j<pn.length; j++) {
			String phone = pn[j].getPhoneNumber();
			if (phone==null) {
			    if (pn[j].getNumber()!=null) {
				if (pn[j].getAreaCode()!=null) phone = pn[j].getAreaCode()+"-"+pn[j].getNumber();
				else phone = pn[j].getNumber();
			    }
			}
			if (phone!=null) {
			    if (pn[j].getExtension()!=null) ext = pn[j].getExtension();
			    else if (pn[j].getExchange()!=null) ext = pn[j].getExchange();
			    
			    if (pn[j].getPhoneNumberType()==cdsDt.PhoneNumberType.W) {
				workPhone = phone;
				workExt   = ext;		    
			    } else if (pn[j].getPhoneNumberType()==cdsDt.PhoneNumberType.R) {
				homePhone = phone;
				homeExt   = ext;
			    } else if (pn[j].getPhoneNumberType()==cdsDt.PhoneNumberType.C) {
				cellPhone = phone;
			    }
			}
		    }
		    
		    String cPurpose = contt[i].getContactPurpose()!=null ? contt[i].getContactPurpose().toString() : "";
		    boolean sdm = false;
		    boolean emc = false;
		    String rel = "Other";
		    if (cPurpose.equals("EC")) emc = true;
		    else if (cPurpose.equals("NK")) sdm = true;
		    else if (cPurpose.equals("AS")) rel = "Administrative Staff";
		    else if (cPurpose.equals("CG")) rel = "Care Giver";
		    else if (cPurpose.equals("PA")) rel = "Power of Attorney";
		    else if (cPurpose.equals("IN")) rel = "Insurance";
		    else if (cPurpose.equals("GT")) rel = "Guarantor";

		    String cDemoNo = dd.getDemoNoByNamePhoneEmail(cFirstName, cLastName, homePhone, workPhone, cEmail);
		    if (cDemoNo.equals("")) {   //add new demographic
			demoRes = dd.addDemographic("", cLastName, cFirstName, "", "", "", "", homePhone, workPhone, "0001", "01", "01", "", "",
						"", "", "", "", "", "", "F", "", "", "", "", "", "", cEmail, "", "", "", "", "", "", "");
			cDemoNo = demoRes.getId();
			if (!workExt.equals("")) dExt.addKey("", cDemoNo, "wPhoneExt", workExt);
			if (!homeExt.equals("")) dExt.addKey("", cDemoNo, "hPhoneExt", homeExt);
			if (!cellPhone.equals("")) dExt.addKey("", cDemoNo, "demo_cell", cellPhone);
		    }
		    DemographicRelationship demoRel = new DemographicRelationship();
		    if (!cDemoNo.equals("")) {
		        demoRel.addDemographicRelationship(demoNo, cDemoNo, rel, sdm, emc, ""/*notes*/, primaryPhysician, null);
		    }
		}
		
		HttpSession se = req.getSession();
		WebApplicationContext  ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(se.getServletContext());
		CaseManagementManager cmm = (CaseManagementManager) ctx.getBean("caseManagementManager");
		
		//Prepare cmNote
		CaseManagementNote cmNote = new CaseManagementNote();
		CaseManagementIssue cmIssu = new CaseManagementIssue();
		cmNote.setUpdate_date(new Date());
		cmNote.setObservation_date(new Date());
		cmNote.setDemographic_no(demoNo);
		cmNote.setProviderNo(primaryPhysician);
		cmNote.setSigning_provider_no(primaryPhysician);
		cmNote.setSigned(true);
		cmNote.setHistory("");
		cmNote.setProgram_no("10015");  //dummy program no for program "OSCAR"
		cmNote.setReporter_caisi_role("1");  //caisi_role for "doctor"
		cmNote.setReporter_program_team("0");
		cmIssu.setDemographic_no(demoNo);
		Set<CaseManagementIssue> sCmIssu = new TreeSet<CaseManagementIssue>();
		sCmIssu.add(cmIssu);
		cmNote.setIssues(sCmIssu);
		
		//PERSONAL HISTORY
		Issue isu = cmm.getIssueInfoByCode("SocHistory");
		cmIssu.setIssue_id(isu.getId());
		cmIssu.setType(isu.getRole());
		cds.PersonalHistoryDocument.PersonalHistory[] pHist = patientRec.getPersonalHistoryArray();
		for (int i=0; i<pHist.length; i++) {
		    String socialHist = "";
		    if (filled(pHist[i].getCategorySummaryLine())) {
			socialHist = pHist[i].getCategorySummaryLine().trim();
		    } else {
			summaryGood = "No";
			appendLine(errorImport,"No Summary for Personal History ("+(i+1)+")");
		    }		    
		    appendLine(socialHist, getResidual(pHist[i].getResidualInfo()));
		    if (filled(socialHist)) {
			cmNote.setNote(socialHist);
			cmm.saveCaseIssue(cmIssu);
			cmm.saveNoteSimple(cmNote);
		    }
		}
		//FAMILY HISTORY
		isu = cmm.getIssueInfoByCode("FamHistory");
		cmIssu.setIssue_id(isu.getId());
		cmIssu.setType(isu.getRole());
		cds.FamilyHistoryDocument.FamilyHistory[] fHist = patientRec.getFamilyHistoryArray();
		for (int i=0; i<fHist.length; i++) {
		    String familyHist = "";
		    appendLine(familyHist, fHist[i].getDiagnosisProblemDescription());
		    appendLine(familyHist, getCode(fHist[i].getDiagnosisCode(),"Diagnosis"));
		    if (fHist[i].getAgeAtOnset()!=null) appendLine(familyHist,"Age at Onset: ",fHist[i].getAgeAtOnset().toString());
		    if (filled(fHist[i].getCategorySummaryLine())) {
			familyHist = "Summary: " + fHist[i].getCategorySummaryLine().trim();
		    } else {
			summaryGood = "No";
			appendLine(errorImport,"No Summary for Family History ("+(i+1)+")");
		    }
		    appendLine(familyHist, getResidual(fHist[i].getResidualInfo()));
		    if (filled(familyHist)) {
			cmNote.setNote(familyHist);
			cmm.saveCaseIssue(cmIssu);
			cmm.saveNoteSimple(cmNote);
		    }
		    cmNote.setNote(fHist[i].getNotes());
		    saveLinkNote(cmNote, cmm);
		    
		    CaseManagementNoteExt cme = new CaseManagementNoteExt();
		    cme.setNoteId(cmNote.getId());
		    if (fHist[i].getStartDate()!=null) {
			cme.setKeyVal(cme.STARTDATE);
			cme.setDateValue(fHist[i].getStartDate().getDateTime().getTime());
			cmm.saveNoteExt(cme);
		    }
		    if (filled(fHist[i].getRelationship())) {
			cme.setKeyVal(cme.RELATIONSHIP);
			cme.setValue(fHist[i].getRelationship());
			cmm.saveNoteExt(cme);
		    }
		    if (filled(fHist[i].getTreatment())) {
			cme.setKeyVal(cme.TREATMENT);
			cme.setValue(fHist[i].getTreatment());
			cmm.saveNoteExt(cme);
		    }
		}
		//PAST HEALTH
		isu = cmm.getIssueInfoByCode("MedHistory");
		cmIssu.setIssue_id(isu.getId());
		cmIssu.setType(isu.getRole());
		cds.PastHealthDocument.PastHealth[] pHealth = patientRec.getPastHealthArray();
		for (int i=0; i< pHealth.length; i++) {
		    String medicalHist = "";
		    appendLine(medicalHist, pHealth[i].getPastHealthProblemDescriptionOrProcedures());
		    appendLine(medicalHist, getCode(pHealth[i].getDiagnosisOrProcedureCode(),"Diagnosis/Procedure"));
		    if (pHealth[i].getMedicalSurgicalFlag()!=null) appendLine(medicalHist,"Medical Surgical Flag: ",pHealth[i].getMedicalSurgicalFlag().toString());
		    appendLine(medicalHist,"Resolved ? ",pHealth[i].getResolvedIndicator());
		    if (filled(pHealth[i].getCategorySummaryLine())) {
			medicalHist = pHealth[i].getCategorySummaryLine().trim();
		    } else {
			summaryGood = "No";
			appendLine(errorImport,"No Summary for Past Health ("+(i+1)+")");
		    }
		    appendLine(medicalHist, getResidual(pHealth[i].getResidualInfo()));
		    if (filled(medicalHist)) {
			cmNote.setNote(medicalHist);
			cmm.saveCaseIssue(cmIssu);
			cmm.saveNoteSimple(cmNote);
		    }
		    cmNote.setNote(pHealth[i].getNotes());
		    saveLinkNote(cmNote, cmm);
		    
		    if (pHealth[i].getResolvedDate()!=null) {
			CaseManagementNoteExt cme = new CaseManagementNoteExt();
			cme.setNoteId(cmNote.getId());
			cme.setKeyVal(cme.RESOLUTIONDATE);
			cme.setDateValue(pHealth[i].getResolvedDate().getDateTime().getTime());
			cmm.saveNoteExt(cme);
		    }
		}
		//PROBLEM LIST
		isu = cmm.getIssueInfoByCode("Concerns");
		cmIssu.setIssue_id(isu.getId());
		cmIssu.setType(isu.getRole());
		cds.ProblemListDocument.ProblemList[] probList = patientRec.getProblemListArray();
		for (int i=0; i<probList.length; i++) {
		    String ongConcerns = "";
		    appendLine(ongConcerns, probList[i].getProblemDescription());
		    appendLine(ongConcerns, getCode(probList[i].getDiagnosisCode(),"Diagnosis"));
		    if (filled(probList[i].getCategorySummaryLine())) {
			ongConcerns = probList[i].getCategorySummaryLine().trim();
		    } else {
			summaryGood = "No";
			appendLine(errorImport,"No Summary for Problem List ("+(i+1)+")");
		    }
		    appendLine(ongConcerns, getResidual(probList[i].getResidualInfo()));
		    if (filled(ongConcerns)) {
			cmNote.setNote(ongConcerns);
			cmm.saveCaseIssue(cmIssu);
			cmm.saveNoteSimple(cmNote);
		    }
		    cmNote.setNote(probList[i].getNotes());
		    saveLinkNote(cmNote, cmm);
		    
		    CaseManagementNoteExt cme = new CaseManagementNoteExt();
		    cme.setNoteId(cmNote.getId());
		    if (probList[i].getOnsetDate()!=null) {
			cme.setKeyVal(cme.STARTDATE);
			cme.setDateValue(probList[i].getOnsetDate().getDateTime().getTime());
			cmm.saveNoteExt(cme);
		    } else {
			dataGood = "No";
			appendLine(errorImport,"No Onset Date for Problem List ("+(i+1)+")");
		    }
		    if (probList[i].getResolutionDate()!=null) {
			cme.setKeyVal(cme.RESOLUTIONDATE);
			cme.setDateValue(probList[i].getResolutionDate().getDateTime().getTime());
			cmm.saveNoteExt(cme);
		    }
		    if (filled(probList[i].getProblemStatus())) {
			cme.setKeyVal(cme.PROBLEMSTATUS);
			cme.setValue(probList[i].getProblemStatus());
			cmm.saveNoteExt(cme);
		    }
		    if (probList[i].getDiagnosisCode()!=null) {
			if (probList[i].getDiagnosisCode().getCodingSystem().equalsIgnoreCase("icd9")) {
			    cme.setKeyVal(cme.DIAGNOSIS);
			    cme.setValue(probList[i].getDiagnosisCode().getValue());
			    if (filled(cme.getValue())) cmm.saveNoteExt(cme);
			}
		    }
		}
		//RISK FACTORS
		isu = cmm.getIssueInfoByCode("Reminders");
		cmIssu.setIssue_id(isu.getId());
		cmIssu.setType(isu.getRole());
		cds.RiskFactorsDocument.RiskFactors[] rFactors = patientRec.getRiskFactorsArray();
		for (int i=0; i<rFactors.length; i++) {
		    String reminders = "";
		    appendLine(reminders,"Risk Factor: ",rFactors[i].getRiskFactor());
		    if (rFactors[i].getAgeOfOnset()!=null) appendLine(reminders,"Age of Onset: ",rFactors[i].getAgeOfOnset().toString());
		    if (filled(rFactors[i].getCategorySummaryLine())) {
			reminders = rFactors[i].getCategorySummaryLine().trim();
		    } else {
			summaryGood = "No";
			appendLine(errorImport,"No Summary for Risk Factors ("+(i+1)+")");
		    }
		    appendLine(reminders, getResidual(rFactors[i].getResidualInfo()));
		    if (filled(reminders)) {
			cmNote.setNote(reminders);
			cmm.saveCaseIssue(cmIssu);
			cmm.saveNoteSimple(cmNote);
		    }
		    cmNote.setNote(rFactors[i].getNotes());
		    saveLinkNote(cmNote, cmm);
		    
		    CaseManagementNoteExt cme = new CaseManagementNoteExt();
		    cme.setNoteId(cmNote.getId());
		    if (rFactors[i].getStartDate()!=null) {
			cme.setKeyVal(cme.STARTDATE);
			cme.setDateValue(rFactors[i].getStartDate().getDateTime().getTime());
			cmm.saveNoteExt(cme);
		    }
		    if (rFactors[i].getEndDate()!=null) {
			cme.setKeyVal(cme.RESOLUTIONDATE);
			cme.setDateValue(rFactors[i].getEndDate().getDateTime().getTime());
			cmm.saveNoteExt(cme);
		    }
		    if (filled(rFactors[i].getExposureDetails())) {
			cme.setKeyVal(cme.EXPOSUREDETAIL);
			cme.setValue(rFactors[i].getExposureDetails());
			cmm.saveNoteExt(cme);
		    }
		}
		//CLINICAL NOTES
		cmNote.setIssues(null);
		cds.ClinicalNotesDocument.ClinicalNotes[] cNotes = patientRec.getClinicalNotesArray();
		for (int i=0; i<cNotes.length; i++) {
		    if (cNotes[i].getEventDateTime()==null) cmNote.setObservation_date(new Date());
		    else cmNote.setObservation_date(cNotes[i].getEventDateTime().getDateTime().getTime());
		    if (cNotes[i].getEnteredDateTime()==null) cmNote.setUpdate_date(new Date());
		    else cmNote.setUpdate_date(cNotes[i].getEnteredDateTime().getDateTime().getTime());
		    
		    String encounter = cNotes[i].getMyClinicalNotesContent();
		    appendLine(encounter,"Note Type: ",cNotes[i].getNoteType());
		    if (cNotes[i].getPrincipalAuthor()!=null) {
			cds.ClinicalNotesDocument.ClinicalNotes.PrincipalAuthor cpAuthor = cNotes[i].getPrincipalAuthor();
			String[] personName = getPersonName(cpAuthor.getName());
			cmNote.setProviderNo(writeProviderData(personName, cpAuthor.getOHIPPhysicianId()));
			appendLine(encounter, "Principal Author Function: ", cNotes[i].getPrincipalAuthorFunction());
		    }
		    if (cNotes[i].getSigningOHIPPhysicianId()!=null) {
			String[] signPhysicianName = {"",""};
			String signPhysicianOHIP = cNotes[i].getSigningOHIPPhysicianId();
			cmNote.setSigning_provider_no(writeProviderData(signPhysicianName, signPhysicianOHIP));
		    }
		    appendLine(encounter,"Signed DateTime: ",getDateFullPartial(cNotes[i].getSignedDateTime()));
		    appendLine(encounter,"Signing Physician OHIP Id: ",cNotes[i].getSigningOHIPPhysicianId());
		    if (filled(encounter)) {
			cmNote.setNote(encounter);
			cmm.saveNoteSimple(cmNote);
		    }
		}
		
		//ALLERGIES & ADVERSE REACTIONS
		cds.AllergiesAndAdverseReactionsDocument.AllergiesAndAdverseReactions[] aaReactArray = patientRec.getAllergiesAndAdverseReactionsArray();
		for (int i=0; i<aaReactArray.length; i++) {
		    String description="", drugrefId="", reaction="", severity="", entryDate="", startDate="", typeCode="";
		    String aSummary = "";
		    if (filled(aaReactArray[i].getCategorySummaryLine())) {
			aSummary = aaReactArray[i].getCategorySummaryLine().trim();
		    } else {
			summaryGood = "No";
			appendLine(errorImport,"No Summary for Allergies & Adverse Reactions ("+(i+1)+")");
		    }
		    
		    reaction = filledOrEmpty(aaReactArray[i].getReaction());
		    if (filled(aSummary)) {
			appendLine(reaction, "Summary: ", aSummary);
			appendLine(errorImport,"Note: Allergies Summary imported in [reaction] ("+(i+1)+")");
		    }
		    description = filledOrEmpty(aaReactArray[i].getOffendingAgentDescription());
		    drugrefId   = filledOrEmpty(aaReactArray[i].getCode().getValue());
		    entryDate   = getDateFullPartial(aaReactArray[i].getRecordedDate());
		    startDate   = getDateFullPartial(aaReactArray[i].getStartDate());
		    appendLine(reaction,"Known Allergies: ",getYN(aaReactArray[i].getKnownAllergies()));
		    appendLine(reaction,"Offending Agent Description: ",aaReactArray[i].getOffendingAgentDescription());
		    appendLine(reaction, getCode(aaReactArray[i].getCode(),"Offending Agent Code"));
		    if (aaReactArray[i].getReactionType()!=null) appendLine(reaction,"Reaction Type: ",aaReactArray[i].getReactionType().toString());
		    if (aaReactArray[i].getHealthcarePractitionerType()!=null) appendLine(reaction,"Healthcare Practitioner Type: ",aaReactArray[i].getHealthcarePractitionerType().toString());
		    appendLine(reaction, getResidual(aaReactArray[i].getResidualInfo()));
		    
		    if (aaReactArray[i].getPropertyOfOffendingAgent()!=null) {
			if (aaReactArray[i].getPropertyOfOffendingAgent()==cdsDt.PropertyOfOffendingAgent.DR) typeCode="13"; //drug
			else if (aaReactArray[i].getPropertyOfOffendingAgent()==cdsDt.PropertyOfOffendingAgent.ND) typeCode="1"; //non-drug
			else if (aaReactArray[i].getPropertyOfOffendingAgent()==cdsDt.PropertyOfOffendingAgent.UK) typeCode="2"; //unknown
		    }
		    if (aaReactArray[i].getSeverity()!=null) {
			if (aaReactArray[i].getSeverity()==cdsDt.AdverseReactionSeverity.MI) severity="1"; //mild
			else if (aaReactArray[i].getSeverity()==cdsDt.AdverseReactionSeverity.MO) severity="2"; //moderate
			else if (aaReactArray[i].getSeverity()==cdsDt.AdverseReactionSeverity.LT) severity="3"; //severe
			else if (aaReactArray[i].getSeverity()==cdsDt.AdverseReactionSeverity.NO) {
			    severity="1";
			    appendLine(errorImport,"Note: Allergies Severity [No Reaction] imported as [Mild] ("+(i+1)+")");
			}
		    }
		    Long allergyId = new RxAllergyImport().Save(demoNo, entryDate, description, typeCode, reaction, startDate, severity, drugrefId);
		    
		    cmNote.setNote(aaReactArray[i].getNotes());
		    saveLinkNote(cmNote, CaseManagementNoteLink.ALLERGIES, allergyId, cmm);

		}
		
		
		//MEDICATIONS & TREATMENTS
		cds.MedicationsAndTreatmentsDocument.MedicationsAndTreatments[] medArray = patientRec.getMedicationsAndTreatmentsArray();
		for (int i=0; i<medArray.length; i++) {
		    String rxDate="", endDate="", BN="", regionalId="", frequencyCode="", duration="1";
		    String quantity="", special="", route="", drugForm="", createDate="", dosage="", unit="", lastRefillDate="";
		    boolean longTerm=false, pastMed=false;
		    int takemin=0, takemax=0, repeat=0, patientCompliance=0;

		    String mSummary = "";
		    if (filled(medArray[i].getCategorySummaryLine())) {
			mSummary = medArray[i].getCategorySummaryLine().trim();
		    } else {
			summaryGood = "No";
			appendLine(errorImport,"No Summary for Medications & Treatments ("+(i+1)+")");
		    }
		    createDate	   = getDateFullPartial(medArray[i].getPrescriptionWrittenDate());
		    rxDate	   = getDateFullPartial(medArray[i].getStartDate());
		    endDate	   = getDateFullPartial(medArray[i].getEndDate());
		    lastRefillDate = getDateFullPartial(medArray[i].getLastRefillDate());
		    regionalId	   = filledOrEmpty(medArray[i].getDrugIdentificationNumber());
		    frequencyCode  = filledOrEmpty(medArray[i].getFrequency());
		    quantity	   = filledOrEmpty(medArray[i].getQuantity());
		    duration	   = filledOrEmpty(medArray[i].getDuration());
		    route	   = filledOrEmpty(medArray[i].getRoute());
		    drugForm	   = filledOrEmpty(medArray[i].getForm());
		    longTerm	   = getYN(medArray[i].getLongTermMedication())=="Y";
		    pastMed	   = getYN(medArray[i].getPastMedications())=="Y";
		    
		    String pc = getYN(medArray[i].getPatientCompliance());
		    if (pc=="Y") patientCompliance = 1;
		    else if (pc=="N") patientCompliance = -1;
		    else patientCompliance = 0;
		    
		    if (duration.trim().equals("1 year")) duration = "365"; //coping with scenario in CMS 2.0
		    
		    if (filled(medArray[i].getNumberOfRefills())) {
			repeat = Integer.parseInt(medArray[i].getNumberOfRefills());
		    }
		    
		    if (filled(medArray[i].getDrugName())) {
			BN = medArray[i].getDrugName();
			special = medArray[i].getDrugName();
		    } else {
			dataGood = "No";
			appendLine(errorImport,"No Drug Name in Medications & Treatments ("+(i+1)+")");
		    }
		    
		    if (filled(mSummary)) {
			appendLine(special, "Summary: ", mSummary);
			appendLine(errorImport,"Note: Medications Summary imported in [special] ("+(i+1)+")");
		    }
		    appendLine(special, "DIN: ", regionalId);
		    appendLine(special, "Prescription Instructions: ", medArray[i].getPrescriptionInstructions());
		    appendLine(special, "Dosage: ", medArray[i].getDosage());
		    appendLine(special, "Quantity: ", medArray[i].getQuantity());
		    appendLine(special, "Notes: ", medArray[i].getNotes());
		    appendLine(special, getResidual(medArray[i].getResidualInfo()));
		    
		    String dose = medArray[i].getDosage();
		    int sep1 = dose.indexOf("-");
		    int sep2 = dose.indexOf(" ");
		    int sep3 = dose.indexOf(" ", sep2+1);
		    if (sep2>sep1 && sep3>sep2) {
			if (sep1>0) {
			    takemin = Integer.parseInt(dose.substring(0,sep1));
			    takemax = Integer.parseInt(dose.substring(sep1+1,sep2));
			} else {
			    takemin = Integer.parseInt(dose.substring(0,sep2));
			    takemax = takemin;
			}
			unit = dose.substring(sep2+1,sep3);
		    }
		    if (medArray[i].getStrength()!=null) {
			dosage = filledOrEmpty(medArray[i].getStrength().getAmount())+" "+filledOrEmpty(medArray[i].getStrength().getUnitOfMeasure());
			appendLine(special, "Strength: ", dosage);
		    }
		    String prescribedBy = "";
		    if (medArray[i].getPrescribedBy()!=null) {
			String[] personName = getPersonName(medArray[i].getPrescribedBy().getName());
			String personOHIP = medArray[i].getPrescribedBy().getOHIPPhysicianId();
			prescribedBy = writeProviderData(personName, personOHIP);
		    }
		    
		    RxPrescriptionImport rpi = new RxPrescriptionImport(prescribedBy,demoNo,rxDate,endDate,BN,regionalId,frequencyCode,duration,quantity,
						repeat,lastRefillDate,special,route,drugForm,createDate,dosage,takemin,takemax,unit,longTerm,pastMed,patientCompliance,(i+1));
		    rpi.Save();
		    
		    cmNote.setNote(medArray[i].getNotes());
		    saveLinkNote(cmNote, CaseManagementNoteLink.DRUGS, rpi.getImportedDrugId(), cmm);
		}


		//IMMUNIZATIONS
		cds.ImmunizationsDocument.Immunizations[] immuArray = patientRec.getImmunizationsArray();
		for (int i=0; i<immuArray.length; i++) {
		    String preventionDate="", providerName="", preventionType="", refused="0";
		    ArrayList preventionExt = new ArrayList();
		   
		    if (filled(immuArray[i].getImmunizationName())) {
			preventionType = immuArray[i].getImmunizationName();
		    } else {
			dataGood = "No";
			appendLine(errorImport,"No Immunization Name ("+(i+1)+")");
		    }
		    preventionDate = getDateFullPartial(immuArray[i].getDate());
		    refused = getYN(immuArray[i].getRefusedFlag()).equals("Y") ? "1" : "0";
		    
		    String comments="", iSummary="";
		    if (immuArray[i].getCategorySummaryLine()!=null) {
			iSummary = immuArray[i].getCategorySummaryLine().trim();
		    } else {
			summaryGood = "No";
			appendLine(errorImport,"No Summary for Immunizations ("+(i+1)+")");
		    }
		    appendLine(comments, immuArray[i].getNotes());
		    if (filled(iSummary)) {
			appendLine(comments, "Summary", iSummary);
			appendLine(errorImport,"Note: Immunization Summary imported in [comments] ("+(i+1)+")");
		    }
		    appendLine(comments, getCode(immuArray[i].getImmunizationCode(),"Immunization Code"));
//		    appendLine(comments, "Dose: ", immuArray[i].getDose());
		    appendLine(comments, "Instructions: ", immuArray[i].getInstructions());
		    appendLine(comments, getResidual(immuArray[i].getResidualInfo()));
		    if (filled(comments)) {
			Hashtable ht = new Hashtable();
			ht.put("comments", comments);
			preventionExt.add(ht);
		    }
		    if (filled(immuArray[i].getManufacturer())) {
			Hashtable ht = new Hashtable();
			ht.put("manufacture", immuArray[i].getManufacturer());
			preventionExt.add(ht);
		    }
		    if (filled(immuArray[i].getLotNumber())) {
			Hashtable ht = new Hashtable();
			ht.put("lot", immuArray[i].getLotNumber());
			preventionExt.add(ht);
		    }
		    if (filled(immuArray[i].getRoute())) {
			Hashtable ht = new Hashtable();
			ht.put("route", immuArray[i].getRoute());
			preventionExt.add(ht);
		    }
		    if (filled(immuArray[i].getSite())) {
			Hashtable ht = new Hashtable();
			ht.put("location", immuArray[i].getSite());
			preventionExt.add(ht);
		    }
		    if (filled(immuArray[i].getDose())) {
			Hashtable ht = new Hashtable();
			ht.put("dose", immuArray[i].getDose());
			preventionExt.add(ht);
		    }
		    PreventionData prevD = new PreventionData();
		    prevD.insertPreventionData(primaryPhysician, demoNo, preventionDate, primaryPhysician, providerName, preventionType, refused, "", "", preventionExt);
		}

		//LABORATORY RESULTS
		cds.LaboratoryResultsDocument.LaboratoryResults[] labResultArray = patientRec.getLaboratoryResultsArray();
		String ppId="";
		for (int i=0; i<labResultArray.length; i++) {
		    String testName="", abn="", minimum="", maximum="", result="", unit="", description="", location="", accession_num="", coll_date="";
		    testName = filledOrEmpty(labResultArray[i].getTestName());
		    if (labResultArray[i].getResultNormalAbnormalFlag()!=null) {
			cdsDt.ResultNormalAbnormalFlag.Enum flag = labResultArray[i].getResultNormalAbnormalFlag();
			if (flag.equals(cdsDt.ResultNormalAbnormalFlag.Y)) abn = "Y";
			if (flag.equals(cdsDt.ResultNormalAbnormalFlag.N)) abn = "N";
		    } else {
			dataGood = "No";
			appendLine(errorImport,"No Normal/Abnormal Flag for Laboratory Results ("+(i+1)+")");
		    }
		    if (labResultArray[i].getReferenceRange()!=null) {
			cds.LaboratoryResultsDocument.LaboratoryResults.ReferenceRange ref = labResultArray[i].getReferenceRange();
			maximum = filledOrEmpty(ref.getHighLimit());
			minimum = filledOrEmpty(ref.getLowLimit());
			if (!filled(maximum) && !filled(minimum)) minimum = filledOrEmpty(ref.getReferenceRangeText());
		    }
		    if (labResultArray[i].getResult()!=null) {
			result = filledOrEmpty(labResultArray[i].getResult().getValue());
			unit = filledOrEmpty(labResultArray[i].getResult().getUnitOfMeasure());
		    }   
		    description   = filledOrEmpty(labResultArray[i].getNotesFromLab());
		    location	  = filledOrEmpty(labResultArray[i].getLaboratoryName());
		    accession_num = filledOrEmpty(labResultArray[i].getAccessionNumber());
		    
		    coll_date = getDateFullPartial(labResultArray[i].getCollectionDateTime());
		    if (filled(coll_date)) {
			coll_date = UtilDateUtilities.DateToString(UtilDateUtilities.StringToDate(coll_date,"yyyy-MM-dd"),"dd-MMM-yyyy");
		    } else {
			dataGood = "No";
			String errorMsg = "No Collection DateTime for Laboratory Results";
			appendLine(errorImport, errorMsg);
			errWarnings.add(errorMsg);
		    }
		    LabResultImport lri = new LabResultImport();
		    String print_date = UtilDateUtilities.DateToString(UtilDateUtilities.StringToDate(coll_date),"yyyyMMdd");
		    String RIId = lri.saveLabRI(location, print_date, "00:00:00");
		    ppId = lri.saveLabPPInfo(RIId, accession_num, firstName, lastName, sex, hin, birthDate, patientPhone, coll_date);
		    Long lrId = lri.SaveLabTR(testName, abn, minimum, maximum, result, unit, description, location, ppId);
                    String labEverything = getLabDline(labResultArray[i]);
                    if (filled(labEverything)){
                       lri.SaveLabDesc(labEverything,ppId);
                    }
		    lri.savePatientLR(demoNo, ppId);
		    
		    cmNote.setNote(labResultArray[i].getPhysiciansNotes());
		    saveLinkNote(cmNote, CaseManagementNoteLink.LABTEST, lrId, cmm);
		}
		
		
		//APPOINTMENTS
		cds.AppointmentsDocument.Appointments[] appArray = patientRec.getAppointmentsArray();
		Date appointmentDate = null;
		String name="", notes="", reason="", status="", startTime="", endTime="", providerNo="";

		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(
				req.getSession().getServletContext());
		OscarSuperManager oscarSuperManager = (OscarSuperManager)webApplicationContext.getBean("oscarSuperManager");

		for (int i=0; i<appArray.length; i++) {
		    name = lastName + "," + firstName;
		    
		    String apptDateStr = getDateFullPartial(appArray[i].getAppointmentDate());
		    if (filled(apptDateStr)) {
			appointmentDate = UtilDateUtilities.StringToDate(apptDateStr);
		    } else {
			dataGood = "No";
			appendLine(errorImport,"No Appointment Date ("+(i+1)+")");
		    }
		    if (appArray[i].getAppointmentTime()!=null) {
			startTime = getCalTime(appArray[i].getAppointmentTime());
			if (appArray[i].getDuration()!=null) {
			    Date d_startTime = appArray[i].getAppointmentTime().getTime();
			    Date d_endTime = new Date();
			    d_endTime.setTime(d_startTime.getTime() + appArray[i].getDuration().longValue()*60000);
			    endTime = UtilDateUtilities.DateToString(d_endTime,"HH:mm:ss");
			} else {
			    Date d_startTime = appArray[i].getAppointmentTime().getTime();
			    Date d_endTime = new Date();
			    d_endTime.setTime(d_startTime.getTime() + 15*60000);
			    endTime = UtilDateUtilities.DateToString(d_endTime,"HH:mm:ss");
			}
		    } else {
			dataGood = "No";
			appendLine(errorImport,"No Appointment Time ("+(i+1)+")");
		    }
		    if (filled(appArray[i].getAppointmentNotes())) {
			notes = appArray[i].getAppointmentNotes();
		    } else {
			dataGood = "No";
			appendLine(errorImport,"No Appointment Notes ("+(i+1)+")");
		    }
		    if (appArray[i].getAppointmentStatus()!=null) {
			ApptStatusData asd = new ApptStatusData();
			String[] allStatus = asd.getAllStatus();
			String[] allTitle = asd.getAllTitle();
			status = allStatus[0];
			for (int j=1; j<allStatus.length; j++) {
			    String msg = getResources(req).getMessage(allTitle[j]);
			    if (appArray[i].getAppointmentStatus().trim().equalsIgnoreCase(msg)) {
				status = allStatus[j];
				break;
			    }
			}
		    }
		    reason = filledOrEmpty(appArray[i].getAppointmentPurpose());
		    if (appArray[i].getProvider()!=null) {
			String[] personName = getPersonName(appArray[i].getProvider().getName());
			String personOHIP = appArray[i].getProvider().getOHIPPhysicianId();
			providerNo = writeProviderData(personName, personOHIP);
		    }
		    oscarSuperManager.update("appointmentDao", "import_appt", new Object [] {providerNo,
		    		appointmentDate, startTime, endTime, name, demoNo, notes, reason, status});
		}
		
		//REPORTS RECEIVED
		cds.ReportsReceivedDocument.ReportsReceived[] repR = patientRec.getReportsReceivedArray();
		for (int i=0; i<repR.length; i++) {
		    cdsDt.ReportContent repCt = repR[i].getContent();
		    if (repCt!=null) {
			byte[] b = null;
			if (repCt.getMedia()!=null) b = repCt.getMedia();
			else if (repCt.getTextContent()!=null) b = repCt.getTextContent().getBytes();
			if (b==null) {
			    otherGood = "No";
			    appendLine(errorImport,"No report file in xml ("+(i+1)+")");
			} else {
			    String docFileName = "ImportReport-"+UtilDateUtilities.getToday("yyyy-MM-dd.HH.mm.ss")+"-"+i;
			    String docType=null, contentType=null, observationDate=null, updateDateTime=null, docCreator=null;
			    
			    if (filled(repR[i].getFileExtensionAndVersion())) {
				contentType = repR[i].getFileExtensionAndVersion();
				docFileName += getFileExt(contentType);
			    } else {
				dataGood = "No";
				appendLine(errorImport,"No File Extension & Version for Reports ("+(i+1)+")");
			    }
			    String docDesc = docFileName;
			    String docDir = oscar.OscarProperties.getInstance().getProperty("DOCUMENT_DIR");
			    if (!filled(docDir)) {
				throw new Exception("Document Directory not set! Check oscar.properties.");
			    } else {
				if (docDir.charAt(docDir.length()-1)!='/') docDir = docDir + '/';
			    }
			    FileOutputStream f = new FileOutputStream(docDir+docFileName);
			    f.write(b);
			    f.close();

			    observationDate = getDateFullPartial(repR[i].getEventDateTime());
			    updateDateTime = getDateFullPartial(repR[i].getReceivedDateTime());
			    if (repR[i].getClass1()!=null) {
				if (repR[i].getClass1().equals(cdsDt.ReportClass.DIAGNOSTIC_IMAGING_REPORT)) docType = "radiology";
				else if (repR[i].getClass1().equals(cdsDt.ReportClass.DIAGNOSTIC_TEST_REPORT)) docType = "pathology";
				else if (repR[i].getClass1().equals(cdsDt.ReportClass.CONSULTANT_REPORT)) docType = "consult";
				else docType = "others";
			    } else {
				dataGood = "No";
				appendLine(errorImport,"No Class for Reports ("+(i+1)+")");
			    }
			    if (repR[i].getAuthorPhysician()!=null) {
				String[] personName = getPersonName(repR[i].getAuthorPhysician());
				
				ProviderData provData = new ProviderData();
				ArrayList pList = provData.getProviderList();
				boolean providerFound = false;
				for (int j=0; j<pList.size(); j++) {
				    Hashtable pHash = (Hashtable) pList.get(j);
				    if (personName[0].equalsIgnoreCase((String)pHash.get("firstName")) &&
					personName[1].equalsIgnoreCase((String)pHash.get("lastName"))) {
					docCreator = (String)pHash.get("providerNo");
					providerFound = true;
					j = pList.size();
				    }
				}
				if (!providerFound) {
				    docCreator = provData.getNewExtProviderNo();
				    provData.addProvider(docCreator, personName[0], personName[1], "");
				}
			    }
			    EDocUtil.addDocument(demoNo,docFileName,docDesc,docType,contentType,observationDate,updateDateTime,docCreator);
			}
		    }
		}
		
		//AUDIT INFORMATION
		String fileTime = UtilDateUtilities.getToday("yyyy-MM-dd.HH.mm.ss");
		String auditInfoSummary = "importAuditInfoSummary-"+fileTime;
		String docDir = oscar.OscarProperties.getInstance().getProperty("DOCUMENT_DIR");
		if (!filled(docDir)) {
		    throw new Exception("Document Directory not set! Check oscar.properties.");
		} else {
		    if (docDir.charAt(docDir.length()-1)!='/') docDir = docDir + '/';
		}
		
		/**********************
		 * Read data from xml *
		 **********************/
		cds.AuditInformationDocument.AuditInformation[] audInf = patientRec.getAuditInformationArray();
		for (int i=0; i<audInf.length; i++) {
		    boolean multi = (audInf.length>1);
		    String sAudInfo = audInf[i].getCategorySummaryLine();
		    if (!filled(sAudInfo)) {
			summaryGood = "No";
			appendLine(errorImport,"No Summary for Audit Information ("+(i+1)+")");
		    } else {
			/***** Write summary *****/
			FileWriter fw = new FileWriter(docDir+auditInfoSummary);
			fw.write(sAudInfo);
			fw.close();
		    }
		    
		    if (audInf[i].getFormat().equals(cdsDt.AuditFormat.TEXT)) {
			cdsDt.ResidualInformation audRes = audInf[i].getResidualInfo();
			if (audRes!=null) {
			    cdsDt.ResidualInformation.DataElement[] audEArr = audRes.getDataElementArray();
			    for (cdsDt.ResidualInformation.DataElement audE : audEArr) {
				appendLine(sAudInfo, "Name: ", audE.getName());
				appendLine(sAudInfo, "Data Type: ", audE.getDataType());
				if (audE.getDescription()!=null) appendLine(sAudInfo, "Description: ", audE.getDescription());
				appendLine(sAudInfo, "Content: ", audE.getContent());
				appendLine(sAudInfo, "", "----------------------------------------");
			    }
			}
			if (audInf[i].getContent()!=null) {
			    appendLine(sAudInfo, "", audInf[i].getContent().getTextContent());
			}
			
		    } else if (audInf[i].getFormat().equals(cdsDt.AuditFormat.FILE)) {
			String contentType = getFileExt(audInf[i].getFileExtensionAndVersion());
			if (audInf[i].getContent()!=null) {
			    String auditInfoFile = "importAuditInfo-"+fileTime;
			    auditInfoFile = multi ? auditInfoFile+"("+i+")" : auditInfoFile;
			    auditInfoFile += contentType;
			    FileOutputStream f = new FileOutputStream(docDir+auditInfoFile);
			    f.write(audInf[i].getContent().getMedia());
			    f.close();
			}
		    }
		}
		
		/***** Write to database *****
		ArrayList<Log> lglist = new ArrayList<Log>();
		
		for (Log lg : lglist) {
		    Timestamp dt = Timestamp.valueOf(lg.getDateTime().toString());
		    LogAction.addFullLog(dt, lg.getProviderNo(), lg.getAction(), lg.getContent(), lg.getContentId(), lg.getIp());
		}
		***** Write to database *****/
	    }
	    errWarnings.addAll(demoRes.getWarningsCollection());
	    if (!cleanFile(xmlFile)) throw new Exception ("Error! Cannot delete XML file!");
	    
	} catch (Exception e) {
	    errWarnings.addAll(demoRes.getWarningsCollection());
	    e.printStackTrace();
	}
	if (demoNo.equals("")) {
	    return null;
	} else {
	    String[] d = {demoNo, dataGood, summaryGood, otherGood, errorImport};
	    return d;
	}
    }
    
    File makeImportLog(Vector demo, String tDir) throws IOException {
	String[][] keyword = new String[3][5];
	keyword[0][0] = "PatientID";
	keyword[0][1] = "Discrete Data";
	keyword[0][2] = "Summary LIne";
	keyword[0][3] = "Other Import";
	keyword[0][4] = "Errors";
	keyword[1][0] = "";
	keyword[1][1] = "Elements Import";
	keyword[1][2] = "Import";
	keyword[1][3] = "Categories";
	keyword[1][4] = "";
	keyword[2][0] = "";
	keyword[2][1] = "Successful";
	keyword[2][2] = "Successful";
	keyword[2][3] = "";
	keyword[2][4] = "";
	File importLog = new File(tDir, "ImportEvent-"+UtilDateUtilities.getToday("yyyy-MM-dd.HH.mm.ss")+".log");
	BufferedWriter out = new BufferedWriter(new FileWriter(importLog));
	int[] colWidth = new int[5];
	colWidth[0] = keyword[0][0].length()+2;
	colWidth[1] = keyword[1][1].length()+2;
	colWidth[2] = keyword[0][2].length()+2;
	colWidth[3] = keyword[0][3].length()+2;
	colWidth[4] = 66;
	int tableWidth = colWidth[0]+colWidth[1]+colWidth[2]+colWidth[3]+colWidth[4]+1;
	out.newLine();
	out.write(fillUp("",'-',tableWidth));
	out.newLine();
	for (int i=0; i<keyword.length; i++) {
	    for (int j=0; j<keyword[i].length; j++) {
		out.write(fillUp("|" + keyword[i][j],' ',colWidth[j]));
	    }
	    out.write("|");
	    out.newLine();
	}
	out.write(fillUp("",'-',tableWidth));
	out.newLine();
	
	for (int i=0; i<demo.size(); i++) {
	    String[] info = (String[]) demo.get(i);
	    for (int j=0; j<info.length; j++) {
		if (j==info.length-1) {
		    String[] text = info[j].split("\n");
		    for (int k=0; k<text.length; k++) {
			text[k] = fillUp("|"+text[k],' ',colWidth[j]);
		    }
		    out.write(text[0] + "|");
		    for (int k=1; k<text.length; k++) {
			out.newLine();
			out.write(fillUp("|",' ',colWidth[0]));
			out.write(fillUp("|",' ',colWidth[1]));
			out.write(fillUp("|",' ',colWidth[2]));
			out.write(fillUp("|",' ',colWidth[3]));
			out.write(text[k] + "|");
		    }
		} else {
		    out.write(fillUp("|" + info[j],' ',colWidth[j]));
		}
	    }
	    out.newLine();
	    out.write(fillUp("",'-',tableWidth));
	    out.newLine();
	}
	out.close();
	return importLog;
    }

    boolean cleanFile(String filename) {
	File f = new File(filename);
	return f.delete();
    }
    
    String fillUp(String filled, char c, int size) {
	if (size>=filled.length()) {
	    int fill = size-filled.length();
	    for (int i=0; i<fill; i++) filled += c;
	}
	return filled;
    }
    
    String getCalDate(Calendar c) {
	if (c==null) return "";
	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
	return f.format(c.getTime());
    }
    String getCalTime(Calendar c) {
	if (c==null) return "";
	SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
	return f.format(c.getTime());
    }
    String getCalDateTime(Calendar c) {
	if (c==null) return "";
	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	return f.format(c.getTime());
    }
    
    String getDateFullPartial(cdsDt.DateFullOrPartial dfp) {
	if (dfp==null) return "";
	
	if (dfp.getDateTime()!=null) return getCalDateTime(dfp.getDateTime());
	else if (dfp.getFullDate()!=null) return getCalDate(dfp.getFullDate());
	else if (dfp.getYearMonth()!=null) return getCalDate(dfp.getYearMonth());
	else if (dfp.getYearOnly()!=null) return getCalDate(dfp.getYearOnly());
	else return "";
    }
    
    String[] getPersonName(cdsDt.PersonNameSimple person) {
	String[] name = new String[2];
	if (person==null) {
	    name[0] = "";
	    name[1] = "";
	} else {	
	    name[0] = filledOrEmpty(person.getFirstName());
	    name[1] = filledOrEmpty(person.getLastName());
	}
	return name;
    }
    
    String[] getPersonName(cdsDt.PersonNameSimpleWithMiddleName person) {
	String[] name = new String[2];
	if (person==null) {
	    name[0] = "";
	    name[1] = "";
	} else {	
	    name[0] = filledOrEmpty(person.getFirstName());
	    name[1] = filledOrEmpty(person.getLastName());
	}
	return name;
    }
    
    String writeProviderData(String[] name, String ohip) throws SQLException {
	String providerNo = "";
	ProviderData pd = new ProviderData();
	if (filled(ohip)) providerNo = filledOrEmpty(pd.getProviderNoByOhip(ohip));
	if (!filled(providerNo)) {   //this is a new provider
	    providerNo = pd.getNewExtProviderNo();
	    pd.addProvider(providerNo, name[0], name[1], filledOrEmpty(ohip));
	}
	return providerNo;
    }
    
    String getCode(cdsDt.Code dCode, String dTitle) {
	if (dCode==null) return "";
	
	String ret = filled(dTitle) ? dTitle+" -" : "";
	appendLine(ret, "Coding System: ", dCode.getCodingSystem());
	appendLine(ret, "Value: ",         dCode.getValue());
	appendLine(ret, "Description: ",   dCode.getDescription());
	
	return ret;
    }
    
    String getYN(cdsDt.YnIndicator yn) {
	if (yn==null) return "";
	String ret = "N";
	if (yn.getYnIndicatorsimple()==cdsDt.YnIndicatorsimple.Y || yn.getYnIndicatorsimple()==cdsDt.YnIndicatorsimple.Y_2) {
	    ret = "Y";
	} else if (yn.getBoolean()) {
	    ret = "Y";
	}
	return ret;
    }
    
    String getYN(cdsDt.YnIndicatorAndBlank yn) {
	if (yn==null) return "";
	String ret = "N";
	if (yn.getYnIndicatorsimple()==cdsDt.YnIndicatorsimple.Y || yn.getYnIndicatorsimple()==cdsDt.YnIndicatorsimple.Y_2) {
	    ret = "Y";
	} else if (yn.getBoolean()) {
	    ret = "Y";
	} else if (yn.getBlank()!=null) {
	    ret = "";
	}
	return ret;
    }
    
    String getResidual(cdsDt.ResidualInformation resInfo) {
	String ret = "";
	if (resInfo==null) return ret;
	
	cdsDt.ResidualInformation.DataElement[] resData = resInfo.getDataElementArray();
	for (int i=0; i<resData.length; i++) {
	    if (filled(resData[i].getName())) {
		appendLine(ret, "Data Name: ",   resData[i].getName());
		appendLine(ret, "Description: ", resData[i].getDescription());
		appendLine(ret, "Data Type: ",   resData[i].getDataType());
		appendLine(ret, "Content: ",     resData[i].getContent());
	    }
	}
	return ret;
    }
    
    boolean filled(String s) {
	return (s!=null && s.trim().length()>0);
    }
    
    String filledOrEmpty(String nullOrTextlessStr) {
	if (!filled(nullOrTextlessStr)) {
	    nullOrTextlessStr = "";
	}
	return nullOrTextlessStr;
    }
    
    void appendLine(String baseStr, String addStr) {
	appendLine(baseStr, "", addStr);
    }
    
    void appendLine(String baseStr, String label, String addStr) {
	if (filled(baseStr)) {
	    baseStr += filled(addStr) ? "\n"+label+addStr : "";
	} else {
	    baseStr = filledOrEmpty(label+addStr);
	}
    }
    
    String getInternalString(Element e){
       String ret = "";
       if (e !=null){
          ret = e.getTextTrim();
       }
       return ret;
    }
    
    void appendIfNotNull(StringBuffer s, String name, String object){
        if (object != null){
            s.append(name+": "+object+"\n");
        }
    }

    String getLabDline( cds.LaboratoryResultsDocument.LaboratoryResults labRes){
        StringBuffer s = new StringBuffer();
        appendIfNotNull(s,"LaboratoryName",labRes.getLaboratoryName());
        appendIfNotNull(s,"TestNameReportedByLab", labRes.getTestNameReportedByLab());
        appendIfNotNull(s,"LabTestCode",labRes.getLabTestCode()); 
	appendIfNotNull(s,"TestName", labRes.getTestName());
	appendIfNotNull(s,"AccessionNumber",labRes.getAccessionNumber());
        
        if (labRes.getResult ()!=null) {
            appendIfNotNull(s,"Value",labRes.getResult().getValue());
            appendIfNotNull(s,"UnitOfMeasure",labRes.getResult().getUnitOfMeasure());
	}   
        if (labRes.getReferenceRange()!=null) {
            cds.LaboratoryResultsDocument.LaboratoryResults.ReferenceRange ref = labRes.getReferenceRange();
            appendIfNotNull(s,"LowLimit",ref.getLowLimit());
            appendIfNotNull(s,"HighLimit",ref.getHighLimit());
            appendIfNotNull(s,"ReferenceRangeText", ref.getReferenceRangeText());                                             
	}
        
//<xs:element name="LabRequisitionDateTime" type="cdsd:dateTimeYYYYMMDDHHMM" minOccurs="0"/>
        appendIfNotNull(s,"LabRequisitionDateTime",getDateFullPartial(labRes.getLabRequisitionDateTime ()));     
//<xs:element name="CollectionDateTime" type="cdsd:dateTimeYYYYMMDDHHMM"/>
        appendIfNotNull(s,"CollectionDateTime",getDateFullPartial( labRes.getCollectionDateTime()));     
//<xs:element name="DateTimeResultReceivedByCMS" type="cdsd:dateTimeYYYYMMDDHHMM" minOccurs="0"/>
        appendIfNotNull(s,"DateTimeResultReceivedByCMS",getDateFullPartial(labRes.getDateTimeResultReceivedByCMS()));     
//<xs:element name="DateTimeResultReviewed" type="cdsd:dateTimeYYYYMMDDHHMM" minOccurs="0"/>
        appendIfNotNull(s,"DateTimeResultReviewed",getDateFullPartial(labRes.getDateTimeResultReviewed()));     
        
        if (labRes.getResultReviewer() != null){ //ResultReviewer){
//<xs:element name="ResultReviewer" type="cdsd:ohipBillingNumber" minOccurs="0"/>
	if (labRes.getResultReviewer().getName()!=null) {
        appendIfNotNull(s,"Reviewer First Name:", labRes.getResultReviewer().getName().getFirstName());
        appendIfNotNull(s,"Reviewer Last Name:",labRes.getResultReviewer().getName().getLastName());
	}
        appendIfNotNull(s,"OHIP ID :", labRes.getResultReviewer().getOHIPPhysicianId());
        }
//<xs:element name="ResultNormalAbnormalFlag">xs:restriction base="xs:token"> xs:enumeration value="Y"/><xs:enumeration value="N"/<xs:enumeration value="U"/>
        appendIfNotNull(s,"ResultNormalAbnormalFlag",""+labRes.getResultNormalAbnormalFlag());     
//<xs:element name="TestResultsInformationreportedbytheLaboratory" type="cdsd:text32K"/>
        appendIfNotNull(s,"TestResultsInformationreportedbytheLaboratory",labRes.getTestResultsInformationReportedByTheLab());     
//<xs:element name="NotesFromLab" type="cdsd:text32K" minOccurs="0"/>
        appendIfNotNull(s,"NotesFromLab",labRes.getNotesFromLab());     
//<xs:element name="PhysiciansNotes" type="cdsd:text32K" minOccurs="0"/>
        appendIfNotNull(s,"PhysiciansNotes",labRes.getPhysiciansNotes());     
        
        /*
         <xs:element name="LaboratoryName" <xs:restriction base="cdsd:text"xs:maxLength value="120"/>
<xs:element name="TestNameReportedByLab" minOccurs="0">xs:restriction base="xs:token"xs:maxLength value="120"/>
<xs:element name="LabTestCode" minOccurs="0"xs:restriction base="xs:token"xs:maxLength value="50"/>
<xs:element name="TestName" minOccurs="0"xs:restriction base="cdsd:text"xs:maxLength value="120"/>
<xs:element name="AccessionNumber" minOccurs="0"xs:restriction base="cdsd:text"xs:maxLength value="120"/>
<xs:element name="Result" minOccurs="0"
<xs:all>
<xs:element name="Value" type="cdsd:labResultValue"/>
<xs:element name="UnitOfMeasure" type="xs:token"/>
<xs:element name="ReferenceRange" minOccurs="0"
<xs:element name="LowLimit" type="cdsd:labResultValue"/>
<xs:element name="HighLimit" type="cdsd:labResultValue"/>
<xs:element name="ReferenceRangeText">xs:restriction base="cdsd:text"> xs:maxLength value="1024"/>
<xs:element name="LabRequisitionDateTime" type="cdsd:dateTimeYYYYMMDDHHMM" minOccurs="0"/>
<xs:element name="CollectionDateTime" type="cdsd:dateTimeYYYYMMDDHHMM"/>
<xs:element name="DateTimeResultReceivedByCMS" type="cdsd:dateTimeYYYYMMDDHHMM" minOccurs="0"/>
<xs:element name="DateTimeResultReviewed" type="cdsd:dateTimeYYYYMMDDHHMM" minOccurs="0"/>
<xs:element name="ResultReviewer" type="cdsd:ohipBillingNumber" minOccurs="0"/>
<xs:element name="ResultNormalAbnormalFlag">xs:restriction base="xs:token"> xs:enumeration value="Y"/><xs:enumeration value="N"/<xs:enumeration value="U"/>
<xs:element name="TestResultsInformationreportedbytheLaboratory" type="cdsd:text32K"/>
<xs:element name="NotesFromLab" type="cdsd:text32K" minOccurs="0"/>
<xs:element name="PhysiciansNotes" type="cdsd:text32K" minOccurs="0"/>
         */
	return s.toString();
    }
    
    String getFileExt(String mimeType) {
	String ret = "";
	if (!filled(mimeType)) return ret;
	
	String type_ext = "application/envoy=evy|application/fractals=fif|application/futuresplash=spl|application/hta=hta|application/internet-property-stream=acx|application/mac-binhex40=hqx|application/msword=doc|application/msword=dot|application/octet-stream=bin|application/octet-stream=class|application/octet-stream=dms|application/octet-stream=exe|application/octet-stream=lha|application/octet-stream=lzh|application/oda=oda|application/olescript=axs|application/pdf=pdf|application/pics-rules=prf|application/pkcs10=p10|application/pkix-crl=crl|application/postscript=ai|application/postscript=eps|application/postscript=ps|application/rtf=rtf|application/set-payment-initiation=setpay|application/set-registration-initiation=setreg|application/vnd.ms-excel=xla|application/vnd.ms-excel=xlc|application/vnd.ms-excel=xlm|application/vnd.ms-excel=xls|application/vnd.ms-excel=xlt|application/vnd.ms-excel=xlw|application/vnd.ms-outlook=msg|application/vnd.ms-pkicertstore=sst|application/vnd.ms-pkiseccat=cat|application/vnd.ms-pkistl=stl|application/vnd.ms-powerpoint=pot|application/vnd.ms-powerpoint=pps|application/vnd.ms-powerpoint=ppt|application/vnd.ms-project=mpp|application/vnd.ms-works=wcm|application/vnd.ms-works=wdb|application/vnd.ms-works=wks|application/vnd.ms-works=wps|application/winhlp=hlp|application/x-bcpio=bcpio|application/x-cdf=cdf|application/x-compress=z|application/x-compressed=tgz|application/x-cpio=cpio|application/x-csh=csh|application/x-director=dcr|application/x-director=dir|application/x-director=dxr|application/x-dvi=dvi|application/x-gtar=gtar|application/x-gzip=gz|application/x-hdf=hdf|application/x-internet-signup=ins|application/x-internet-signup=isp|application/x-iphone=iii|application/x-javascript=js|application/x-latex=latex|application/x-msaccess=mdb|application/x-mscardfile=crd|application/x-msclip=clp|application/x-msdownload=dll|application/x-msmediaview=m13|application/x-msmediaview=m14|application/x-msmediaview=mvb|application/x-msmetafile=wmf|application/x-msmoney=mny|application/x-mspublisher=pub|application/x-msschedule=scd|application/x-msterminal=trm|application/x-mswrite=wri|application/x-netcdf=cdf|application/x-netcdf=nc|application/x-perfmon=pma|application/x-perfmon=pmc|application/x-perfmon=pml|application/x-perfmon=pmr|application/x-perfmon=pmw|application/x-pkcs12=p12|application/x-pkcs12=pfx|application/x-pkcs7-certificates=p7b|application/x-pkcs7-certificates=spc|application/x-pkcs7-certreqresp=p7r|application/x-pkcs7-mime=p7c|application/x-pkcs7-mime=p7m|application/x-pkcs7-signature=p7s|application/x-sh=sh|application/x-shar=shar|application/x-shockwave-flash=swf|application/x-stuffit=sit|application/x-sv4cpio=sv4cpio|application/x-sv4crc=sv4crc|application/x-tar=tar|application/x-tcl=tcl|application/x-tex=tex|application/x-texinfo=texi|application/x-texinfo=texinfo|application/x-troff=roff|application/x-troff=t|application/x-troff=tr|application/x-troff-man=man|application/x-troff-me=me|application/x-troff-ms=ms|application/x-ustar=ustar|application/x-wais-source=src|application/x-x509-ca-cert=cer|application/x-x509-ca-cert=crt|application/x-x509-ca-cert=der|application/ynd.ms-pkipko=pko|application/zip=zip|audio/basic=au|audio/basic=snd|audio/mid=mid|audio/mid=rmi|audio/mpeg=mp3|audio/x-aiff=aif|audio/x-aiff=aifc|audio/x-aiff=aiff|audio/x-mpegurl=m3u|audio/x-pn-realaudio=ra|audio/x-pn-realaudio=ram|audio/x-wav=wav|image/bmp=bmp|image/cis-cod=cod|image/gif=gif|image/ief=ief|image/jpeg=jpe|image/jpeg=jpeg|image/jpeg=jpg|image/pipeg=jfif|image/svg+xml=svg|image/tiff=tif|image/tiff=tiff|image/x-cmu-raster=ras|image/x-cmx=cmx|image/x-icon=ico|image/x-portable-anymap=pnm|image/x-portable-bitmap=pbm|image/x-portable-graymap=pgm|image/x-portable-pixmap=ppm|image/x-rgb=rgb|image/x-xbitmap=xbm|image/x-xpixmap=xpm|image/x-xwindowdump=xwd|message/rfc822=mht|message/rfc822=mhtml|message/rfc822=nws|text/css=css|text/h323=323|text/html=htm|text/html=html|text/html=stm|text/iuls=uls|text/plain=bas|text/plain=c|text/plain=h|text/plain=txt|text/richtext=rtx|text/scriptlet=sct|text/tab-separated-values=tsv|text/webviewhtml=htt|text/x-component=htc|text/x-setext=etx|text/x-vcard=vcf|video/mpeg=mp2|video/mpeg=mpa|video/mpeg=mpe|video/mpeg=mpeg|video/mpeg=mpg|video/mpeg=mpv2|video/quicktime=mov|video/quicktime=qt|video/x-la-asf=lsf|video/x-la-asf=lsx|video/x-ms-asf=asf|video/x-ms-asf=asr|video/x-ms-asf=asx|video/x-msvideo=avi|video/x-sgi-movie=movie|x-world/x-vrml=flr|x-world/x-vrml=vrml|x-world/x-vrml=wrl|x-world/x-vrml=wrz|x-world/x-vrml=xaf|x-world/x-vrml=xof|";
	mimeType = mimeType.toLowerCase();
	type_ext = type_ext.toLowerCase();
	int pos = type_ext.indexOf(mimeType);
	if (pos>-1) {
	    pos = pos + mimeType.length() + 1;
	    int end_pos = type_ext.indexOf('|', pos);
	    ret = "." + type_ext.substring(pos, end_pos);
	}
	return ret;
    }
    
    void saveLinkNote(CaseManagementNote cmn, CaseManagementManager cmm) {
	saveLinkNote(cmn, CaseManagementNoteLink.CASEMGMTNOTE, cmn.getId(), cmm);
    }
    
    void saveLinkNote(CaseManagementNote cmn, Integer tableName, Long tableId, CaseManagementManager cmm) {
	if (filled(cmn.getNote())) {
	    cmm.saveNoteSimple(cmn);    //new note id created
	    
	    CaseManagementNoteLink cml = new CaseManagementNoteLink();
	    cml.setTableName(tableName);
	    cml.setTableId(tableId);
	    cml.setNoteId(cmn.getId()); //new note id
	    cmm.saveNoteLink(cml);
	}
    }
    
    public ImportDemographicDataAction3() {
   }
   
}
