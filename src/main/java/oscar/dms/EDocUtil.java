/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */

package oscar.dms;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.oscarehr.PMmodule.caisi_integrator.CaisiIntegratorManager;
import org.oscarehr.PMmodule.caisi_integrator.IntegratorFallBackManager;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.PMmodule.model.ProgramProvider;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.caisi_integrator.ws.CachedDemographicDocument;
import org.oscarehr.casemgmt.dao.CaseManagementNoteDAO;
import org.oscarehr.casemgmt.dao.CaseManagementNoteLinkDAO;
import org.oscarehr.casemgmt.model.CaseManagementNote;
import org.oscarehr.casemgmt.model.CaseManagementNoteLink;
import org.oscarehr.common.dao.ConsultDocsDao;
import org.oscarehr.common.dao.CtlDocTypeDao;
import org.oscarehr.common.dao.CtlDocumentDao;
import org.oscarehr.common.dao.CtlDocumentMetadataDao;
import org.oscarehr.common.dao.DocumentDao;
import org.oscarehr.common.dao.DocumentDao.Module;
import org.oscarehr.common.dao.DocumentReviewDao;
import org.oscarehr.common.dao.IndivoDocsDao;
import org.oscarehr.common.dao.OscarAppointmentDao;
import org.oscarehr.common.dao.PatientLabRoutingDao;
import org.oscarehr.common.dao.ProviderInboxRoutingDao;
import org.oscarehr.common.dao.SystemPreferencesDao;
import org.oscarehr.common.dao.TicklerLinkDao;
import org.oscarehr.common.model.Appointment;
import org.oscarehr.common.model.ConsultDocs;
import org.oscarehr.common.model.CtlDocType;
import org.oscarehr.common.model.CtlDocument;
import org.oscarehr.common.model.CtlDocumentMetadata;
import org.oscarehr.common.model.CtlDocumentPK;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.Document;
import org.oscarehr.common.model.DocumentReview;
import org.oscarehr.common.model.IndivoDocs;
import org.oscarehr.common.model.PatientLabRouting;
import org.oscarehr.common.model.Provider;
import org.oscarehr.common.model.SystemPreferences;
import org.oscarehr.common.model.Tickler;
import org.oscarehr.common.model.TicklerLink;
import org.oscarehr.managers.DemographicManager;
import org.oscarehr.managers.ProgramManager2;
import org.oscarehr.managers.TicklerManager;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;

import oscar.MyDateFormat;
import oscar.OscarProperties;
import oscar.log.LogAction;
import oscar.log.LogConst;
import oscar.oscarLab.ca.all.AcknowledgementData;
import oscar.oscarMDS.data.ReportStatus;
import oscar.util.ConversionUtils;
import oscar.util.DateUtils;
import oscar.util.UtilDateUtilities;

import javax.servlet.http.HttpServletRequest;

// all SQL statements here
public final class EDocUtil {

	private static ConsultDocsDao consultDocsDao = (ConsultDocsDao) SpringUtils.getBean("consultDocsDao");
	private static DocumentDao documentDao = (DocumentDao) SpringUtils.getBean(DocumentDao.class);
	private static DocumentReviewDao documentReviewDao = SpringUtils.getBean(DocumentReviewDao.class);
	private static IndivoDocsDao indivoDocsDao = (IndivoDocsDao) SpringUtils.getBean(IndivoDocsDao.class);
	private static Logger logger = MiscUtils.getLogger();
	private static ProgramManager2 programManager2 = SpringUtils.getBean(ProgramManager2.class);
	
	
	public static final String PUBLIC = "public";
	public static final String PRIVATE = "private";
	
	public enum EDocSort {
		DATE("d.updatedatetime DESC, d.updatedatetime DESC"),
		DESCRIPTION("d.docdesc, d.updatedatetime DESC"),
		DOCTYPE("d.doctype, d.updatedatetime DESC"),
		CREATOR("d.doccreator, d.updatedatetime DESC"),
		RESPONSIBLE("d.responsible, d.updatedatetime DESC"),
		OBSERVATIONDATE("d.observationdate DESC, d.updatedatetime DESC"),
                CONTENTDATE("d.contentdatetime DESC, d.updatedatetime DESC"),
		CONTENTTYPE("d.contenttype, d.updatedatetime DESC"),
		REVIEWER("d.reviewer, d.updatedatetime DESC");
		
		private String value;
		
		private EDocSort(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
	}
	
	public static final String SORT_DATE = "d.updatedatetime DESC, d.updatedatetime DESC";
	public static final String SORT_DESCRIPTION = "d.docdesc, d.updatedatetime DESC";
	public static final String SORT_DOCTYPE = "d.doctype, d.updatedatetime DESC";
	public static final String SORT_CREATOR = "d.doccreator, d.updatedatetime DESC";
	public static final String SORT_RESPONSIBLE = "d.responsible, d.updatedatetime DESC";
	public static final String SORT_OBSERVATIONDATE = "d.observationdate DESC, d.updatedatetime DESC";
        public static final String SORT_CONTENTDATE = "d.contentdatetime DESC, d.updatedatetime DESC";
	public static final String SORT_CONTENTTYPE = "d.contenttype, d.updatedatetime DESC";
	public static final String SORT_REVIEWER = "d.reviewer, d.updatedatetime DESC";
	
	public static final boolean ATTACHED = true;
	public static final boolean UNATTACHED = false;

	public static final String DMS_DATE_FORMAT = "yyyy/MM/dd";
	public static final String REVIEW_DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
        public static final String CONTENT_DATETIME_FORMAT ="yyyy-MM-dd HH:mm:ss";

	private static ProgramManager programManager = (ProgramManager) SpringUtils.getBean("programManager");
	private static CaseManagementNoteLinkDAO caseManagementNoteLinkDao = (CaseManagementNoteLinkDAO) SpringUtils.getBean("CaseManagementNoteLinkDAO");
        private static CaseManagementNoteDAO caseManagementNoteDao = (CaseManagementNoteDAO) SpringUtils.getBean("CaseManagementNoteDAO");
        private static TicklerLinkDao ticklerLinkDao = (TicklerLinkDao) SpringUtils.getBean("ticklerLinkDao");
        private static TicklerManager ticklerManager = SpringUtils.getBean(TicklerManager.class);
	private static ProviderDao providerDao = (ProviderDao)SpringUtils.getBean("providerDao");
	private static CtlDocTypeDao ctldoctypedao = (CtlDocTypeDao) SpringUtils.getBean("ctlDocTypeDao");
	private static DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
	private static CtlDocumentDao ctlDocumentDao = (CtlDocumentDao) SpringUtils.getBean("ctlDocumentDao");
	
	public static String getProviderName(String providerNo) {
		if (providerNo == null || providerNo.length() == 0) {
			return "";
		}
		Provider p = providerDao.getProvider(providerNo);
		if (p != null) {
			return p.getLastName().toUpperCase() + ", " + p.getFirstName().toUpperCase();
		}
		return "";
	}

	public static String getDemographicName(LoggedInInfo loggedInInfo, String demographicNo) {
		if (demographicNo == null || demographicNo.length() == 0) {
			return "";
		}
		Demographic d = demographicManager.getDemographic(loggedInInfo, demographicNo);
		if (d != null) {
			return d.getLastName().toUpperCase() + ", " + d.getFirstName().toUpperCase();
		}
		return "";
	}

	public static Provider getProvider(String providerNo) {
		if (providerNo == null || providerNo.length() == 0) {
			return null;
		}
		return providerDao.getProvider(providerNo);
	}

	public static ArrayList<String> getDoctypesByStatus(String module, String[] statuses) {
		ArrayList<String> doctypes = new ArrayList<String>();
		List<CtlDocType> result = ctldoctypedao.findByStatusAndModule(statuses, module);
		for (CtlDocType obj : result) {
			doctypes.add(obj.getDocType());
		}

		return doctypes;
	}
	
	public static ArrayList<String> getDoctypes(String module) {
		return getDoctypesByStatus(module,new String[]{ "A", "H", "I" });
	}

	public static ArrayList<String> getActiveDocTypes(String module) {
		return getDoctypesByStatus(module,new String[]{"A"});
	}

	public static String getDocStatus(String module, String doctype) {
		List<CtlDocType> result = ctldoctypedao.findByDocTypeAndModule(doctype, module);
		String status = "";
		for (CtlDocType obj : result) {
			status = obj.getStatus();
		}
		return status;
	}

	public static void addCaseMgmtNoteLink(CaseManagementNoteLink cmnl) {
		caseManagementNoteLinkDao.save(cmnl);
		logger.debug("ADD CASEMGMT NOTE LINK : Id=" + cmnl.getId());
	}

	/**
	 * @return the new documentId
	 */
	public static String addDocumentSQL(EDoc newDocument) {
		return addDocumentSQL(newDocument, null, false);
	}
	
	public static String addDocumentSQL(EDoc newDocument, String providerNo, boolean doReview) {
		Document doc = new Document();
		doc.setDoctype(newDocument.getType());
		doc.setDocClass(newDocument.getDocClass());
		doc.setDocSubClass(newDocument.getDocSubClass());
		doc.setDocdesc(newDocument.getDescription());
		doc.setDocxml(newDocument.getHtml());
		doc.setDocfilename(newDocument.getFileName());
		doc.setDoccreator(newDocument.getCreatorId());
		doc.setSource(newDocument.getSource());
		doc.setSourceFacility(newDocument.getSourceFacility());
		doc.setResponsible(newDocument.getResponsibleId());
		doc.setProgramId(newDocument.getProgramId());
		doc.setUpdatedatetime(newDocument.getDateTimeStampAsDate());
                doc.setContentdatetime(newDocument.getContentDateTime());
		doc.setStatus(newDocument.getStatus());
		doc.setContenttype(newDocument.getContentType());
		doc.setPublic1(ConversionUtils.fromIntString(newDocument.getDocPublic()));
		doc.setObservationdate(MyDateFormat.getSysDate(newDocument.getObservationDate()));
		doc.setNumberofpages(newDocument.getNumberOfPages());
		doc.setAppointmentNo(newDocument.getAppointmentNo());
		doc.setRestrictToProgram(newDocument.isRestrictToProgram());
		documentDao.persist(doc);

		Integer document_no = doc.getId();

		CtlDocumentPK cdpk = new CtlDocumentPK();
		CtlDocument cd = new CtlDocument();
		cd.setId(cdpk);
		cdpk.setModule(newDocument.getModule());
		cdpk.setDocumentNo(document_no);
		cd.getId().setModuleId(ConversionUtils.fromIntString(newDocument.getModuleId()));
		cd.setStatus(String.valueOf(newDocument.getStatus()));
		ctlDocumentDao.persist(cd);

		return document_no.toString();
	}

	//new method to let the user add a new DocumentType into the database
	public static void addDocTypeSQL(String docType, String module, String status) {
		CtlDocType ctldoctype = new CtlDocType();
		ctldoctype.setDocType(docType);
		ctldoctype.setModule(module);
		ctldoctype.setStatus(status);
		ctldoctypedao.persist(ctldoctype);
	}

	public static void changeDocTypeStatusSQL(String docType, String module, String status) {
		ctldoctypedao.changeDocType(docType, module, status);
	}

	/** new method to let the user add a new DocumentType into the database */
	public static void addDocTypeSQL(String docType, String module) {
		ctldoctypedao.addDocType(docType, module);
	}

	public static void detachDocConsult(String docNo, String consultId) {
		List<ConsultDocs> consultDocs = consultDocsDao.findByRequestIdDocNoDocType(ConversionUtils.fromIntString(consultId), ConversionUtils.fromIntString(docNo), ConsultDocs.DOCTYPE_DOC);
		for (ConsultDocs consultDoc : consultDocs) {
			consultDoc.setDeleted("Y");
			consultDocsDao.merge(consultDoc);
		}
	}

	public static void attachDocConsult(String providerNo, String docNo, String consultId) {
		ConsultDocs consultDoc = new ConsultDocs();
		consultDoc.setRequestId(ConversionUtils.fromIntString(consultId));
		consultDoc.setDocumentNo(ConversionUtils.fromIntString(docNo));
		consultDoc.setDocType(ConsultDocs.DOCTYPE_DOC);
		consultDoc.setAttachDate(new Date());
		consultDoc.setProviderNo(providerNo);
		consultDocsDao.persist(consultDoc);
	}

	public static void editDocumentSQL(EDoc newDocument, String providerNo, boolean doReview) {

		Document doc = documentDao.find(ConversionUtils.fromIntString(newDocument.getDocId()));
		if (doc != null) {
			doc.setDoctype(newDocument.getType());
			doc.setDocClass(newDocument.getDocClass());
			doc.setDocSubClass(newDocument.getDocSubClass());
			doc.setDocdesc(newDocument.getDescription());
			doc.setSource(newDocument.getSource());
			doc.setSourceFacility(newDocument.getSourceFacility());
			doc.setDocxml(newDocument.getHtml());
			doc.setResponsible(newDocument.getResponsibleId());
			doc.setPublic1(ConversionUtils.fromIntString(newDocument.getDocPublic()));
            doc.setSentDateTime(newDocument.getSentDateTime());
			if (doReview) {
				documentReviewDao.persist(new DocumentReview(doc.getId(), providerNo));
			} else {
				doc.setUpdatedatetime(newDocument.getDateTimeStampAsDate());
				doc.setObservationdate(MyDateFormat.getSysDate(newDocument.getObservationDate()));
			}
			if (newDocument.getFileName().length() > 0) {
				doc.setDocfilename(newDocument.getFileName());
				doc.setContenttype(newDocument.getContentType());
                                doc.setContentdatetime(newDocument.getContentDateTime());
                                doc.setNumberofpages(newDocument.getNumberOfPages());
			}
			documentDao.merge(doc);
		}
	}

	public static void indivoRegister(EDoc doc) {
		IndivoDocs id = new IndivoDocs();
		id.setOscarDocNo(ConversionUtils.fromIntString(doc.getDocId()));
		id.setIndivoDocIdx(doc.getIndivoIdx());
		id.setDocType("document");
		id.setDateSent(new Date());
		if (doc.isInIndivo()) {
			id.setUpdate("U");
		} else {
			id.setUpdate("I");
		}
		indivoDocsDao.persist(id);
	}

	/**
	 * Fetches all consult documents attached to specific consultation
	 */
	//Consultation Request fetch documents
	public static ArrayList<EDoc> listDocs(LoggedInInfo loggedInInfo, String demoNo, String requestId, boolean attached) {
		List<Object[]> docs = documentDao.findDocsAndConsultDocsByConsultId(ConversionUtils.fromIntString(requestId));
		List<Object[]> ctlDocs = null;
		if (!attached) {
			ctlDocs = documentDao.findCtlDocsAndDocsByModuleAndModuleId(Module.DEMOGRAPHIC, ConversionUtils.fromIntString(demoNo));
		}
		return documentProgramFiltering(loggedInInfo,listDocs(loggedInInfo, attached, docs, ctlDocs));
	}
	
	//Consultation Response fetch documents
	public static ArrayList<EDoc> listResponseDocs(LoggedInInfo loggedInInfo, String demoNo, String responseId, boolean attached) {
		List<Object[]> docs = documentDao.findDocsAndConsultResponseDocsByConsultId(ConversionUtils.fromIntString(responseId));
		List<Object[]> ctlDocs = null;
		if (!attached) {
			ctlDocs = documentDao.findCtlDocsAndDocsByModuleAndModuleId(Module.DEMOGRAPHIC, ConversionUtils.fromIntString(demoNo));
		}
		return documentProgramFiltering(loggedInInfo,listDocs(loggedInInfo, attached, docs, ctlDocs));
	}
	
	private static ArrayList<EDoc> listDocs(LoggedInInfo loggedInInfo, boolean attached, List<Object[]> docs, List<Object[]> ctlDocs) {
		ArrayList<EDoc> resultDocs = new ArrayList<EDoc>();
		ArrayList<EDoc> attachedDocs = new ArrayList<EDoc>();

		for (Object[] o : docs) {
			Document d = (Document) o[0];

			EDoc currentdoc = new EDoc();
			currentdoc.setDocId("" + d.getDocumentNo());
			currentdoc.setDescription(d.getDocdesc());
			currentdoc.setFileName(d.getDocfilename());
			currentdoc.setContentType(d.getContenttype());
			currentdoc.setCreatorId(d.getDoccreator());
			currentdoc.setSource(d.getSource());
			currentdoc.setSourceFacility(d.getSourceFacility());
			currentdoc.setResponsibleId(d.getResponsible());
			if (d.getProgramId() != null) {
				currentdoc.setProgramId(d.getProgramId());
			}
			if (d.getAppointmentNo() != null) {
				currentdoc.setAppointmentNo(d.getAppointmentNo());
			}
			currentdoc.setType(d.getDoctype());
			currentdoc.setStatus(d.getStatus());
			currentdoc.setObservationDate(d.getObservationdate());
			currentdoc.setReviewerId(d.getReviewer());
			currentdoc.setReviewDateTime(ConversionUtils.toTimestampString(d.getReviewdatetime()));
			currentdoc.setReviewDateTimeDate(d.getReviewdatetime());
            currentdoc.setContentDateTime(d.getContentdatetime());
            
            if(d.isRestrictToProgram() != null){            
            	currentdoc.setRestrictToProgram(d.isRestrictToProgram());
            }
            
			attachedDocs.add(currentdoc);
		}

		if (attached) { //listing attached documents only
			resultDocs = attachedDocs;
		}
		else { //remove attached documents from full document list
			for (Object[] o : ctlDocs) {
				Document d = (Document) o[1];

				EDoc currentdoc = new EDoc();
				currentdoc.setDocId("" + d.getDocumentNo());
				currentdoc.setDescription(d.getDocdesc());
				currentdoc.setFileName(d.getDocfilename());
				currentdoc.setContentType(d.getContenttype());
				if(d.getReportMedia() != null){ 
					currentdoc.setMediaType(d.getReportMedia()); 
				}
				currentdoc.setCreatorId(d.getDoccreator());
				currentdoc.setSource(d.getSource());
				currentdoc.setSourceFacility(d.getSourceFacility());
				currentdoc.setResponsibleId(d.getResponsible());
				if (d.getProgramId() != null) {
					currentdoc.setProgramId(d.getProgramId());
				}
				if (d.getAppointmentNo() != null) {
					currentdoc.setAppointmentNo(d.getAppointmentNo());
				}
				currentdoc.setType(d.getDoctype());
				currentdoc.setStatus(d.getStatus());
				currentdoc.setObservationDate(d.getObservationdate());
				currentdoc.setReviewerId(d.getReviewer());
				currentdoc.setReviewDateTime(ConversionUtils.toTimestampString(d.getReviewdatetime()));
				currentdoc.setReviewDateTimeDate(d.getReviewdatetime());
                                currentdoc.setContentDateTime(d.getContentdatetime());
                currentdoc.setRestrictToProgram(d.isRestrictToProgram());
                                
				if (!attachedDocs.contains(currentdoc)) resultDocs.add(currentdoc);
			}
		}

		if (OscarProperties.getInstance().getBooleanProperty("FILTER_ON_FACILITY", "true")) {
			resultDocs = documentFacilityFiltering(loggedInInfo, resultDocs);
		}

		return resultDocs;
	}
	/**
	 * End Fetches consult documents
	 */
	

	public static ArrayList<EDoc> listDocs(LoggedInInfo loggedInInfo, String module, String moduleid, String docType, String publicDoc, EDocSort sort) {
		return listDocs(loggedInInfo, module, moduleid, docType, publicDoc, sort, "active");
	}

	public static EDoc getEDocFromDocId(String docId) {
		DocumentDao dao = SpringUtils.getBean(DocumentDao.class);
		EDoc currentdoc = new EDoc();

		for (Object[] o : dao.findCtlDocsAndDocsByDocNo(ConversionUtils.fromIntString(docId))) {
			Document d = (Document) o[0];
			CtlDocument c = (CtlDocument) o[1];

			currentdoc.setModule(c.getId().getModule());
			currentdoc.setModuleId("" + c.getId().getModuleId());
			currentdoc.setDocId("" + d.getDocumentNo());
			currentdoc.setDescription(d.getDocdesc());
			currentdoc.setType(d.getDoctype());
			currentdoc.setDocClass(d.getDocClass());
			currentdoc.setDocSubClass(d.getDocSubClass());
			currentdoc.setCreatorId(d.getDoccreator());
			currentdoc.setSource(d.getSource());
			currentdoc.setSourceFacility(d.getSourceFacility());
			currentdoc.setResponsibleId(d.getResponsible());
			if (d.getProgramId() != null) {
				currentdoc.setProgramId(d.getProgramId());
			}
			if (d.getAppointmentNo() != null) {
				currentdoc.setAppointmentNo(d.getAppointmentNo());
			}
			currentdoc.setDateTimeStampAsDate(d.getUpdatedatetime());
			currentdoc.setDateTimeStamp(ConversionUtils.toTimestampString(d.getUpdatedatetime()));
			currentdoc.setFileName(d.getDocfilename());
			currentdoc.setStatus(d.getStatus());
			currentdoc.setContentType(d.getContenttype());
			if(d.getReportMedia() != null){
				currentdoc.setMediaType(d.getReportMedia());
			}
			currentdoc.setObservationDate(d.getObservationdate());
			currentdoc.setReviewerId(d.getReviewer());
			currentdoc.setReviewDateTime(ConversionUtils.toTimestampString(d.getReviewdatetime()));
			currentdoc.setReviewDateTimeDate(d.getReviewdatetime());
                        currentdoc.setContentDateTime(d.getContentdatetime());
            currentdoc.setAbnormal(d.isAbnormal());
		}

		return currentdoc;
	}

	public static ArrayList<EDoc> listDocsPreviewInbox(List<String> docIds) {

		ArrayList<EDoc> resultDocs = new ArrayList<EDoc>();
		for (String docId : docIds) {
			EDoc currentdoc = new EDoc();
			currentdoc = getEDocFromDocId(docId);
			resultDocs.add(currentdoc);
		}
		return resultDocs;
	}

	public static ArrayList<EDoc> listDocs(LoggedInInfo loggedInInfo, String module, String moduleid, String docType, String publicDoc, EDocSort sort, String viewstatus) {
		
		boolean includePublic = publicDoc.equals(PUBLIC);
		boolean includeDeleted = viewstatus.equals("deleted");
		boolean includeActive = viewstatus.equals("active");
		List<Object[]> documents = documentDao.findDocuments(module, moduleid, docType, includePublic, includeDeleted, includeActive, sort, null);

		ArrayList<EDoc> resultDocs = new ArrayList<EDoc>();
		for (Object[] o : documents) {
			Document d = (Document)o[1];
			EDoc currentdoc = toEDoc(d);
			resultDocs.add(currentdoc);
		}

		if (OscarProperties.getInstance().getBooleanProperty("FILTER_ON_FACILITY", "true")) {
			resultDocs = documentFacilityFiltering(loggedInInfo, resultDocs);
		}
		
		//filter by program.
		resultDocs = documentProgramFiltering(loggedInInfo, resultDocs);

		return resultDocs;
	}

	public static ArrayList<EDoc> listDocsSince(LoggedInInfo loggedInInfo, String module, String moduleid, String docType, String publicDoc, EDocSort sort, String viewstatus, Date since) {
		
		boolean includePublic = publicDoc.equals(PUBLIC);
		boolean includeDeleted = viewstatus.equals("deleted");
		boolean includeActive = viewstatus.equals("active");
		
		
		List<Object[]> documents = documentDao.findDocuments(module, moduleid, docType, includePublic, includeDeleted, includeActive, sort, since);

		ArrayList<EDoc> resultDocs = new ArrayList<EDoc>();
		for (Object[] o : documents) {
			Document d = (Document)o[1];
			EDoc currentdoc = toEDoc(d);
			resultDocs.add(currentdoc);
		}

		if (OscarProperties.getInstance().getBooleanProperty("FILTER_ON_FACILITY", "true")) {
			resultDocs = documentFacilityFiltering(loggedInInfo, resultDocs);
		}

		return resultDocs;
	}
	
	public static ArrayList<Integer> listDemographicIdsSince(Date since) {
		return (ArrayList<Integer>)documentDao.findDemographicIdsSince(since);
	}


	
	private static EDoc toEDoc(Document d) {
		EDoc currentdoc = new EDoc();
		currentdoc.setDocId(d.getId().toString());
		currentdoc.setDescription(d.getDocdesc());
		currentdoc.setFileName(d.getDocfilename());
		currentdoc.setContentType(d.getContenttype());
		if(d.getReportMedia() != null){
			currentdoc.setMediaType(d.getReportMedia()); 
		}
		currentdoc.setCreatorId(d.getDoccreator());
		currentdoc.setSource(d.getSource());
		currentdoc.setSourceFacility(d.getSourceFacility());
		currentdoc.setResponsibleId(d.getResponsible());
		currentdoc.setProgramId(d.getProgramId());
		currentdoc.setAppointmentNo(d.getAppointmentNo());
		currentdoc.setType(d.getDoctype());
		currentdoc.setStatus(d.getStatus());
		currentdoc.setObservationDate(ConversionUtils.toDateString(d.getObservationdate()));
		currentdoc.setReviews(d.getReviews());
        currentdoc.setDateTimeStamp(ConversionUtils.toDateString(d.getUpdatedatetime()));
        currentdoc.setDateTimeStampAsDate(d.getUpdatedatetime());
        currentdoc.setDocClass(d.getDocClass());
        currentdoc.setDocSubClass(d.getDocSubClass());
        currentdoc.setContentDateTime(d.getContentdatetime());
        currentdoc.setAbnormal(d.isAbnormal());
        if(d.isRestrictToProgram() != null && d.isRestrictToProgram()) {
        	currentdoc.setRestrictToProgram(true);
        }
	    return currentdoc;
    }

	public ArrayList<EDoc> getUnmatchedDocuments(String creator, String responsible, Date startDate, Date endDate, boolean unmatchedDemographics) {
		ArrayList<EDoc> list = new ArrayList<EDoc>();

		DocumentDao dao = SpringUtils.getBean(DocumentDao.class);

		for (Object[] o : dao.findCtlDocsAndDocsByModuleCreatorResponsibleAndDates(Module.DEMOGRAPHIC, creator, responsible, startDate, endDate, unmatchedDemographics)) {
			Document d = (Document) o[0];
			CtlDocument c = (CtlDocument) o[1];

			EDoc currentdoc = new EDoc();
			currentdoc.setModule(c.getId().getModule());
			currentdoc.setModuleId("" + c.getId().getModuleId());
			currentdoc.setDocId("" + d.getDocumentNo());
			currentdoc.setDescription(d.getDocdesc());
			currentdoc.setType(d.getDoctype());
			currentdoc.setCreatorId(d.getDoccreator());
			currentdoc.setSource(d.getSource());
			currentdoc.setSourceFacility(d.getSourceFacility());
			currentdoc.setResponsibleId(d.getResponsible());
			if (d.getProgramId() != null) {
				currentdoc.setProgramId(d.getProgramId());
			}
			if (d.getAppointmentNo() != null) {
				currentdoc.setAppointmentNo(d.getAppointmentNo());
			}
			currentdoc.setDateTimeStamp(ConversionUtils.toTimestampString(d.getUpdatedatetime()));
			currentdoc.setFileName(d.getDocfilename());
			currentdoc.setStatus(d.getStatus());
			currentdoc.setContentType(d.getContenttype());
			if(d.getReportMedia() != null){
				currentdoc.setMediaType(d.getReportMedia());
			}
			currentdoc.setObservationDate(d.getObservationdate());
                        currentdoc.setContentDateTime(d.getContentdatetime());

			list.add(currentdoc);
		}

		return list;
	}

	private static ArrayList<EDoc> documentFacilityFiltering(LoggedInInfo loggedInInfo, List<EDoc> eDocs) {
		ArrayList<EDoc> results = new ArrayList<EDoc>();

		for (EDoc eDoc : eDocs) {
			Integer programId = eDoc.getProgramId();
			if (programManager.hasAccessBasedOnCurrentFacility(loggedInInfo, programId)) results.add(eDoc);
		}

		return results;
	}

	/*
	 * 1) is the patient in my program domain
	 */
	private static ArrayList<EDoc> documentProgramFiltering(LoggedInInfo loggedInInfo, List<EDoc> eDocs) {		
		ArrayList<EDoc> results = new ArrayList<EDoc>();

		List<ProgramProvider> ppList = programManager2.getProgramDomain(loggedInInfo, loggedInInfo.getLoggedInProviderNo());
			
		for (EDoc eDoc : eDocs) {
			
			if(!eDoc.isRestrictToProgram() || eDoc.getProgramId() == null || eDoc.getProgramId().intValue() == -1) {
				results.add(eDoc);
				continue;
			}
			
			for(ProgramProvider pp:ppList){
				if(pp.getProgramId().intValue() == eDoc.getProgramId().intValue()) {
					results.add(eDoc);
					continue;
				}
			}
		}

		return results;
	}

	public static ArrayList<EDoc> listDemoDocs(LoggedInInfo loggedInInfo, String moduleid) {
		DocumentDao dao = SpringUtils.getBean(DocumentDao.class);
		ArrayList<EDoc> resultDocs = new ArrayList<EDoc>();
		for (Object[] o : dao.findConstultDocsDocsAndProvidersByModule(Module.DEMOGRAPHIC, ConversionUtils.fromIntString(moduleid))) {
			Document d = (Document) o[0];

			EDoc currentdoc = new EDoc();
			currentdoc.setDocId("" + d.getDocumentNo());
			currentdoc.setType(d.getDoctype());
			currentdoc.setFileName(d.getDocfilename());
			currentdoc.setCreatorId(d.getDoccreator());
			currentdoc.setSource(d.getSource());
			currentdoc.setSourceFacility(d.getSourceFacility());
			currentdoc.setResponsibleId(d.getResponsible());
			currentdoc.setDocClass(d.getDocClass());
			currentdoc.setDocSubClass(d.getDocSubClass());
			currentdoc.setDescription(d.getDocdesc());
			if (d.getProgramId() != null) {
				currentdoc.setProgramId(d.getProgramId());
			}
			if (d.getAppointmentNo() != null) {
				currentdoc.setAppointmentNo(d.getAppointmentNo());
			}
			currentdoc.setDateTimeStamp(ConversionUtils.toTimestampString(d.getUpdatedatetime()));
			currentdoc.setContentType(d.getContenttype());
			if(d.getReportMedia() != null){
				currentdoc.setMediaType(d.getReportMedia());
			}
			currentdoc.setObservationDate(d.getObservationdate());
			currentdoc.setReviews(d.getReviews());
			currentdoc.setContentDateTime(d.getContentdatetime());
			currentdoc.setSentDateTime(d.getSentDateTime());
			resultDocs.add(currentdoc);
		}

		if (OscarProperties.getInstance().getBooleanProperty("FILTER_ON_FACILITY", "true")) {
			resultDocs = documentFacilityFiltering(loggedInInfo, resultDocs);
		}

		return resultDocs;
	}

	public static List<String> listModules() {
		CtlDocTypeDao dao = SpringUtils.getBean(CtlDocTypeDao.class);
		return dao.findModules();
	}

	public static EDoc getDoc(String documentNo) {
		
		DocumentDao dao = SpringUtils.getBean(DocumentDao.class);
		IndivoDocsDao iDao = SpringUtils.getBean(IndivoDocsDao.class);

		EDoc currentdoc = new EDoc();

		for (Object[] o : dao.findCtlDocsAndDocsByDocNo(ConversionUtils.fromIntString(documentNo))) {
			Document d = (Document) o[0];
			CtlDocument c = (CtlDocument) o[1];

			currentdoc.setModule("" + c.getId().getModule());
			currentdoc.setModuleId("" + c.getId().getModuleId());
			currentdoc.setDocId("" + d.getDocumentNo());
			currentdoc.setDescription(d.getDocdesc());
			currentdoc.setType(d.getDoctype());
			currentdoc.setDocClass(d.getDocClass());
			currentdoc.setDocSubClass(d.getDocSubClass());
			currentdoc.setCreatorId(d.getDoccreator());
			currentdoc.setResponsibleId(d.getResponsible());
			currentdoc.setSource(d.getSource());
			currentdoc.setSourceFacility(d.getSourceFacility());
			currentdoc.setDateTimeStampAsDate(d.getUpdatedatetime());
			currentdoc.setDateTimeStamp(ConversionUtils.toTimestampString(d.getUpdatedatetime()));
			currentdoc.setFileName(d.getDocfilename());
			currentdoc.setDocPublic("" + d.getPublic1());
			currentdoc.setObservationDate(d.getObservationdate());
			currentdoc.setReviews(d.getReviews());
			currentdoc.setHtml(d.getDocxml());
			currentdoc.setStatus(d.getStatus());
			currentdoc.setContentType(d.getContenttype());
			if(d.getReportMedia() != null){
				currentdoc.setMediaType(d.getReportMedia());
			}
			currentdoc.setNumberOfPages(d.getNumberofpages());
            currentdoc.setContentDateTime(d.getContentdatetime());
            currentdoc.setSentDateTime(d.getSentDateTime());
            currentdoc.setAbnormal(d.isAbnormal());
            
            if(d.isRestrictToProgram() != null){
            	currentdoc.setRestrictToProgram(d.isRestrictToProgram());
            }
            
			IndivoDocs id = iDao.findByOscarDocNo(d.getDocumentNo(), "document");
			if (id != null) {
				currentdoc.setIndivoIdx(id.getIndivoDocIdx());
				if (currentdoc.getIndivoIdx().length() > 0) {
					currentdoc.registerIndivo();
				}
			}
			
		}

		return currentdoc;
	}

	public String getDocumentName(String id) {
		Document d = documentDao.find(ConversionUtils.fromIntString(id));
		if (d != null) {
			return d.getDocfilename();
		}
		return null;
	}

	public static void undeleteDocument(String documentNo) {
		CtlDocument cd = ctlDocumentDao.getCtrlDocument(ConversionUtils.fromIntString(documentNo));
		String status = "";
		if (cd != null) {
			status = cd.getStatus();
		}

		Document d = documentDao.find(ConversionUtils.fromIntString(documentNo));
		if (d != null) {
			d.setStatus(status.toCharArray()[0]);
			d.setUpdatedatetime(MyDateFormat.getSysDate(getDmsDateTime()));
			documentDao.merge(d);
		}
	}

	public static void deleteDocument(String documentNo) {
		Document d = documentDao.find(ConversionUtils.fromIntString(documentNo));
		if (d != null) {
			d.setStatus('D');
			d.setUpdatedatetime(MyDateFormat.getSysDate(getDmsDateTime()));
			documentDao.merge(d);
		}
	}

       public static void refileDocument(String documentNo, String queueId) throws Exception {

            String sourceDocDir = oscar.OscarProperties.getInstance().getProperty("DOCUMENT_DIR");
            Document d = documentDao.find(ConversionUtils.fromIntString(documentNo));
            File sourceFile = new File(sourceDocDir, d.getDocfilename());

            String destFileName = sourceFile.getName();
            if (destFileName.length() > 18) {
                destFileName = destFileName.substring(14, destFileName.length());
            }

            String destPath = IncomingDocUtil.getIncomingDocumentFilePath(queueId, "Refile");
            File destFile = new File(destPath, "R" + destFileName);

            try {
                if (destFile.exists()) {
                    throw new IOException("Cannot refile document #"+documentNo+ " "+d.getDocdesc()+". Destination File " + destFile.getAbsolutePath() + " already exists");
                } else {
                    FileUtils.copyFile(sourceFile, destFile);
                    EDocUtil.deleteDocument(documentNo);
                }
            } catch (IOException e) {
                logger.error("Error", e);
                throw new Exception(e);
            }
        }

	public static String getDmsDateTime() {
		String nowDateR = UtilDateUtilities.DateToString(new Date(), "yyyy/MM/dd");
		String nowTimeR = UtilDateUtilities.DateToString(new Date(), "HH:mm:ss");
		String dateTimeStamp = nowDateR + " " + nowTimeR;
		return dateTimeStamp;
	}

	public static Date getDmsDateTimeAsDate() {
		return new Date();
	}

	public static int addDocument(String demoNo, String docFileName, String docDesc, String docType, String docClass, String docSubClass, String contentType, String contentDateTime, String observationDate, String updateDateTime, String docCreator, String responsible) {
		return addDocument(demoNo, docFileName, docDesc, docType, docClass, docSubClass, contentType, contentDateTime, observationDate, updateDateTime, docCreator, responsible, null, null);
	}

	public static int addDocument(String demoNo, String docFileName, String docDesc, String docType, String docClass, String docSubClass, String contentType, String contentDateTime, String observationDate, String updateDateTime, String docCreator, String responsible, List<DocumentReview> reviews) {
		return addDocument(demoNo, docFileName, docDesc, docType, docClass, docSubClass, contentType, contentDateTime, null,  observationDate, updateDateTime, docCreator, responsible, reviews, null, null);
	}

	public static int addDocument(String demoNo, String docFileName, String docDesc, String docType, String docClass, String docSubClass, String contentType, String contentDateTime, String observationDate, String updateDateTime, String docCreator, String responsible, List<DocumentReview> reviews, String source) {
		return addDocument(demoNo, docFileName, docDesc, docType, docClass, docSubClass, contentType, contentDateTime, null, observationDate, updateDateTime, docCreator, responsible, reviews, source, null);
	}

	public static int addDocument(String demoNo, String docFileName, String docDesc, String docType, String docClass, String docSubClass, String contentType, String contentDateTime, String sentDateTime, String observationDate, String updateDateTime, String docCreator, String responsible, List<DocumentReview> reviews, String source, String sourceFacility) {
		return addDocument(demoNo, docFileName, docDesc, docType, docClass, docSubClass, contentType, contentDateTime, null, null, observationDate, updateDateTime, docCreator, responsible, reviews, source, null);
	}


		public static int addDocument(String demoNo, String docFileName, String docDesc, String docType, String docClass, String docSubClass, String contentType, String contentDateTime, String mediaType, String sentDateTime, String observationDate, String updateDateTime, String docCreator, String responsible, List<DocumentReview> reviews, String source, String sourceFacility) {

		Document doc = new Document();
		doc.setDoctype(docType);
		doc.setDocClass(docClass);
		doc.setDocSubClass(docSubClass);
		doc.setDocdesc(docDesc);
		doc.setDocfilename(docFileName);
		doc.setDoccreator(docCreator);
		doc.setResponsible(responsible);
		doc.setUpdatedatetime(MyDateFormat.getSysDate(updateDateTime));
		doc.setStatus('A');
		doc.setContenttype(contentType);
		doc.setPublic1(0);
		doc.setObservationdate(MyDateFormat.getSysDate(observationDate));
		doc.setContentdatetime(MyDateFormat.getSysDate(contentDateTime));
		doc.setReportMedia(mediaType);
		doc.setSentDateTime(MyDateFormat.getSysDate(sentDateTime));
		doc.setSource(source);
		doc.setSourceFacility(sourceFacility);
		doc.setNumberofpages(1);
		documentDao.persist(doc);

		int key = 0;
		if (doc.getDocumentNo() > 0) {
			CtlDocumentPK cdpk = new CtlDocumentPK();
			CtlDocument cd = new CtlDocument();
			cd.setId(cdpk);
			cdpk.setModule("demographic");
			cdpk.setDocumentNo(doc.getDocumentNo());
			cd.getId().setModuleId(ConversionUtils.fromIntString(demoNo));
			cd.setStatus(String.valueOf('A'));
			ctlDocumentDao.persist(cd);
			key = cd.getId().getDocumentNo();
			
			if (reviews != null && !reviews.isEmpty()) {
				for (DocumentReview review : reviews) {
					review.setDocumentNo(doc.getDocumentNo());
					documentReviewDao.persist(review);
				}
			}
		}
		return key;
	}

	// private static String getLastDocumentNo() {
	public static String getLastDocumentNo() {
		DocumentDao dao = SpringUtils.getBean(DocumentDao.class);
		return "" + dao.findMaxDocNo();
	}

	public static String getLastDocumentDesc() {
		String docNumber = EDocUtil.getLastDocumentNo();
		DocumentDao dao = SpringUtils.getBean(DocumentDao.class);
		Document d = dao.find(ConversionUtils.fromIntString(docNumber));
		if (d != null) {
			return d.getDocdesc();
		}
		return null;
	}

	public static byte[] getFile(String fpath) {
		byte[] fdata = null;
		FileInputStream fis = null;
		try {
			// first we get length of file and allocate mem for file
			File file = new File(fpath);
			long length = file.length();
			fdata = new byte[(int) length];

			// now we read file into array buffer
			fis = new FileInputStream(file);
			fis.read(fdata);

		} catch (NullPointerException ex) {
			logger.error("Error", ex);
		} catch (FileNotFoundException ex) {
			logger.error("Error", ex);
		} catch (IOException ex) {
			logger.error("Error", ex);
		} finally {
			try {
				if (fis != null) fis.close();
			} catch (IOException e) {
				logger.error("error", e);
			}
		}

		return fdata;
	}

	// add for inbox manager

	public static boolean getDocReviewFlag(String docId) {
		boolean flag = false;
		return flag;
	}

	public static boolean getDocUrgentFlag(String docId) {
		boolean flag = false;
		return flag;
	}

	// get noteId from tableId
	public static Long getNoteIdFromDocId(Long docId) {
		Long noteId = 0L;
		CaseManagementNoteLink cmnLink = caseManagementNoteLinkDao.getLastLinkByTableId(CaseManagementNoteLink.DOCUMENT, docId);
		if (cmnLink != null) noteId = cmnLink.getNoteId();
		return noteId;
	}

	// get tableId from noteId when table_name is document
	public static Long getTableIdFromNoteId(Long noteId) {
		Long tableId = 0L;
		CaseManagementNoteLink cmnLink = caseManagementNoteLinkDao.getLastLinkByNote(noteId);
		if (cmnLink != null && cmnLink.getTableName().equals(CaseManagementNoteLink.DOCUMENT)) {
			tableId = cmnLink.getTableId();
		}
		return tableId;
	}

	// get document from its note
	public static EDoc getDocFromNote(Long noteId) {
		EDoc doc = new EDoc();
		Long docIdL = getTableIdFromNoteId(noteId);
		if (docIdL > 0L) {
			Integer docId = docIdL.intValue();

			DocumentDao dao = SpringUtils.getBean(DocumentDao.class);
			Document d = dao.find(docId);
			if (d != null) {
				doc.setDocId("" + d.getDocumentNo());
				doc.setFileName(d.getDocfilename());
				doc.setStatus(d.getStatus());
			}

		}
		return doc;
	}

	public static ArrayList<EDoc> getRemoteDocuments(LoggedInInfo loggedInInfo, Integer demographicId) {
		ArrayList<EDoc> results = new ArrayList<EDoc>();

		try {

			List<CachedDemographicDocument> remoteDocuments = null;
			try {
				if (!CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
					CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility());
					remoteDocuments = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility()).getLinkedCachedDemographicDocuments(demographicId);
				}
			} catch (Exception e) {
				MiscUtils.getLogger().error("Unexpected error.", e);
				CaisiIntegratorManager.checkForConnectionError(loggedInInfo.getSession(),e);
			}

			if (CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
				MiscUtils.getLogger().debug("getting fall back documents for " + demographicId);
				remoteDocuments = IntegratorFallBackManager.getRemoteDocuments(loggedInInfo,demographicId);
			}

			for (CachedDemographicDocument remoteDocument : remoteDocuments) {
				results.add(toEDoc(remoteDocument));
			}
		} catch (Exception e) {
			logger.error("Error retriving integrated documents.", e);
		}

		logger.debug("retreived remote documents, document count=" + results.size());

		return (results);
	}

	private static EDoc toEDoc(CachedDemographicDocument remoteDocument) {
		EDoc eDoc = new EDoc();

		eDoc.setRemoteFacilityId(remoteDocument.getFacilityIntegerPk().getIntegratorFacilityId());

		eDoc.setAppointmentNo(remoteDocument.getAppointmentNo());
		eDoc.setContentType(remoteDocument.getContentType());
		eDoc.setCreatorId(remoteDocument.getDocCreator());
		eDoc.setDateTimeStamp(DateUtils.formatDate(remoteDocument.getUpdateDateTime(), null));
		eDoc.setDateTimeStampAsDate(DateUtils.toDate(remoteDocument.getUpdateDateTime()));
		eDoc.setDescription(remoteDocument.getDescription());
		eDoc.setDocId(remoteDocument.getFacilityIntegerPk().getCaisiItemId().toString());
		eDoc.setDocPublic("" + remoteDocument.getPublic1());
		eDoc.setFileName(remoteDocument.getDocFilename());
		eDoc.setHtml(remoteDocument.getDocXml());
		eDoc.setModule("demographic");
		eDoc.setModuleId("" + remoteDocument.getCaisiDemographicId());
		eDoc.setNumberOfPages(remoteDocument.getNumberOfPages());
		eDoc.setObservationDate(DateUtils.toDate(remoteDocument.getObservationDate()));
		eDoc.setProgramId(remoteDocument.getProgramId());
		eDoc.setResponsibleId(remoteDocument.getResponsible());
		eDoc.setReviewDateTimeDate(DateUtils.toDate(remoteDocument.getReviewDateTime()));
		eDoc.setReviewDateTime(DateUtils.formatDate(remoteDocument.getReviewDateTime(), null));
		eDoc.setReviewerId(remoteDocument.getReviewer());
		eDoc.setSource(remoteDocument.getSource());
		eDoc.setStatus(remoteDocument.getStatus() != null && remoteDocument.getStatus().length() > 0 ? remoteDocument.getStatus().charAt(0) : ' ');
		eDoc.setType(remoteDocument.getContentType());

		return (eDoc);
	}

	public static void subtractOnePage(String docId) {
		Document doc = documentDao.find(ConversionUtils.fromIntString(docId));
		doc.setNumberofpages(doc.getNumberofpages() - 1);

		documentDao.merge(doc);
	}
        
	public static String getHtmlTicklers(LoggedInInfo loggedInInfo,String docId ) {
                                      
            Long table_id=Long.valueOf(docId);
            List<TicklerLink> linkList = ticklerLinkDao.getLinkByTableId("DOC",table_id );
            String HtmlTickler="";
            Integer ticklerNo;
        
            if (linkList != null){
                for(TicklerLink tl : linkList){
                    ticklerNo = tl.getTicklerNo();
                    Tickler t = ticklerManager.getTickler(loggedInInfo,ticklerNo.intValue());
                      if( org.oscarehr.common.IsPropertiesOn.isTicklerPlusEnable() ) {  
                        HtmlTickler+="<br><a href='#' onclick=\"window.open('../Tickler.do?method=view&id="+ticklerNo.toString()+"','viewtickler"+ticklerNo.toString()+"','height=700,width=600,location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0');\" >"+t.getMessage()+"</a>";
                     } else
                     {
                        HtmlTickler+="<br>"+t.getMessage();
                     }
                }
            }
            return HtmlTickler;
        }
                   
        public static String getHtmlAcknowledgement(Locale locale,String docId ) {
  
            ArrayList<ReportStatus> ackList = AcknowledgementData.getAcknowledgements( "DOC",docId);
            String HtmlAcknowledgement="";
            String comment="";
            ResourceBundle props = ResourceBundle.getBundle("oscarResources", locale);
            for (int i=0; i < ackList.size(); i++) {
                    ReportStatus report = ackList.get(i);
                    HtmlAcknowledgement+=report.getProviderName()+": ";
                    String ackStatus = report.getStatus();
                    if(ackStatus.equals("A")){
                        ackStatus= props.getString("dms.documentBrowser.msgAcknowledgedOn");
                    }else if(ackStatus.equals("F")){
                        ackStatus= props.getString("dms.documentBrowser.msgFileButNotAcknowledgedOn");
                    }else{
                        ackStatus= props.getString("dms.documentBrowser.msgNotAcknowledgeSince");
                    }
                                                                        
                    HtmlAcknowledgement+=ackStatus;
                    HtmlAcknowledgement+=" "+report.getTimestamp()+ " ";
                                                                        
                    comment=report.getComment();
                    if(comment!=null) {
                        HtmlAcknowledgement+= comment; }
                    HtmlAcknowledgement+="<br>";
                                                                   
            }
            
            return HtmlAcknowledgement;
        }
        
        public static String getHtmlAnnotation(String docId) {
    
            Long tableId = 0L;
            String note="";
                
            if (docId!=null && docId.trim().length()>0)  {
                tableId = Long.valueOf(docId);
            }
            
            CaseManagementNoteLink cmnLink = caseManagementNoteLinkDao.getLastLinkByTableId(CaseManagementNoteLink.DOCUMENT, tableId);
            CaseManagementNote p_cmn = null;
            if (cmnLink!=null) {
                p_cmn = caseManagementNoteDao.getNote(cmnLink.getNoteId());
                //get the most recent previous note from uuid.
                p_cmn=caseManagementNoteDao.getMostRecentNote(p_cmn.getUuid());
            }
    
            //if get provider no is -1 , it's a document note.
            if (p_cmn!=null && p_cmn.getProviderNo().equals("-1") ) { p_cmn=null;}  //don't use document note as annotation.
    
            if(p_cmn!=null) {
                note=p_cmn.getNote();
            }
            return note;
        }
     
        /**
		 * Reads content of the specified file with.
		 * 
		 * @param fileName
		 * 		Name of the file to use for saving the content
		 * @param content
		 * 		Content to be saved into the file
		 * @return
		 * 		Returns the content of the file
		 * @throws IOException
		 * 		IOException is thrown in case file can not be read  
		 */
        public static byte[] readContent(String fileName) throws IOException {
    		InputStream is = null;
    		try {
    			is = new BufferedInputStream(new FileInputStream(new File(fileName)));
    			return IOUtils.toByteArray(is);
    		} finally {
    			try {
	                is.close();
                } catch (IOException e) {
                	logger.error("Unable to close output stream", e);
                }
    		}
        }
        
		/**
		 * Saves content to the OSCAR document directory as a file with the specified name.
		 * File with the same name will be overwritten.
		 * 
		 * @param fileName
		 * 		Name of the file to use for saving the content
		 * @param content
		 * 		Content to be saved into the file
		 * @throws IOException
		 * 		IOException is thrown in case of any save errors  
		 */
        public static void writeDocContent(String fileName, byte[] content) throws IOException {
        	String docDir = OscarProperties.getInstance().getProperty("DOCUMENT_DIR");
        	File file = new File(docDir, fileName);
        	writeContent(file.getAbsolutePath(), content);        	
        }
        
        /**
         * Resolves file name for the specified OSCAR file into the absolute path on the file system.
         * 
         * @param fileName
         * 		OSCAR file name.
         * @return
         * 		Returns the absolute path on the file system.
         */
        public static String resovePath(String fileName) {
        	String docDir = OscarProperties.getInstance().getProperty("DOCUMENT_DIR");
        	File file = new File(docDir, fileName);
        	return file.getAbsolutePath();
        }
        
        public static void review(Integer documentNo, String function) {
 
		}
        
        public static void writeContent(String fileName, byte[] content) throws IOException {
        	OutputStream os = null;
    		try {
    			File file = new File(fileName);
    			if (!file.exists()) {
    				file.createNewFile();
    			}
    			os = new BufferedOutputStream(new FileOutputStream(file));
    			os.write(content);
    			os.flush();
    		} finally {
    			if (os != null) {
    				try {
    					os.close();
    				} catch (IOException e) {
    					logger.error("Unable to close output stream", e);
    				}
    			}
    		}
        }

	/**
	 * Saves the provided RTL file to the demographic and routes it to the provider
	 * The RTL will appear in the patient's echart under documents
	 *
	 * @param file PDF to that will be added to the patient
	 * @param provider Provider that the new document record will be routed to
	 * @param demographic Demographic that the document will be added to
	 * @param request Request to retrieve information for logging from
	 */
	public static void saveRtlToPatient(File file, Provider provider, Demographic demographic, Integer appointmentNo, HttpServletRequest request) {
		SystemPreferencesDao systemPreferencesDao = SpringUtils.getBean(SystemPreferencesDao.class);
		int numberOfPages = 0;
		String user = "";
		if (provider != null) {
			user = provider.getProviderNo();
		}
		LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
		SystemPreferences documentTypePreference = systemPreferencesDao.findPreferenceByName("rtl_template_document_type");
		String documentType = "";
		if (documentTypePreference != null && documentTypePreference.getValue() != null) {
			documentType = documentTypePreference.getValue();
		}
		
		String fileName = file.getName();
		fileName = fileName.substring(0, fileName.indexOf("-")) + ".pdf";
		// Creates source and destination paths to move the file
		Path source = Paths.get(file.getPath());
		Path destination = Paths.get(oscar.OscarProperties.getInstance().getProperty("DOCUMENT_DIR") + "/" + fileName);
		String description = user + " RTL " + fileName.substring(fileName.indexOf(".") + 1, fileName.lastIndexOf("."));
		
		try {
			// Copies the file to the destination
			Files.copy(source, destination);
			// Gets the number of pages
			PdfReader reader = new PdfReader(destination.toString());
			numberOfPages = reader.getNumberOfPages();
			reader.close();
		} catch (IOException e) {
			logger.error("An error occurred when trying to copy the letter pdf to the document directory and counting the pages.");
		}

		Date observationDate = null;
		// If an appointment is linked to the eform, uses the appointment date as the observation date
		if (appointmentNo != null) {
			OscarAppointmentDao appointmentDao = SpringUtils.getBean(OscarAppointmentDao.class);
			Appointment appointment = appointmentDao.find(appointmentNo);
			if (appointment != null) {
				observationDate = appointment.getAppointmentDate();
			}
		}
		
		if (observationDate == null){
			observationDate = new Date();
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		
		EDoc newDoc = new EDoc(description, documentType, fileName, "", loggedInInfo.getLoggedInProviderNo(), user, "", 'A', sdf.format(observationDate), "",
				"", "demographic", demographic.getDemographicNo().toString(), numberOfPages);
		// Sets the appointment number
		newDoc.setAppointmentNo(appointmentNo);
		// Sets the fileName again so that it doesn't have the timestamp in front of it
		newDoc.setFileName(fileName);
		newDoc.setDocPublic("0");
		newDoc.setContentType("application/pdf");

		// Saves the document
		String doc_no = addDocumentSQL(newDoc);
		
		try {
			CtlDocumentMetadata documentMetadata = new CtlDocumentMetadata(Integer.parseInt(doc_no), appointmentNo);

			CtlDocumentMetadataDao ctlDocumentMetadataDao = SpringUtils.getBean(CtlDocumentMetadataDao.class);
			ctlDocumentMetadataDao.persist(documentMetadata);
			
		} catch (NumberFormatException e) {
			logger.error("Could not save document to CtlDocumentMetadata, document id is " + doc_no);
		}
		
		// Logs the creation of the RTL
		LogAction.addLog((String) request.getSession().getAttribute("user"), LogConst.ADD, LogConst.CON_DOCUMENT, doc_no, request.getRemoteAddr());

		// Adds a patient routing for the document
		PatientLabRoutingDao patientLabRoutingDao = SpringUtils.getBean(PatientLabRoutingDao.class);
		PatientLabRouting patientLabRouting = new PatientLabRouting(Integer.parseInt(doc_no), "DOC", demographic.getDemographicNo());
		patientLabRoutingDao.persist(patientLabRouting);

		// Adds the document to the provider's inbox
		if (provider != null) {
			ProviderInboxRoutingDao providerInboxRoutingDao = SpringUtils.getBean(ProviderInboxRoutingDao.class);
			providerInboxRoutingDao.addToProviderInbox(provider.getProviderNo(), Integer.parseInt(doc_no), "DOC");
		}
	}

	}
