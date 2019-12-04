	 	
<%--

    Copyright (c) 2006-. OSCARservice, OpenSoft System. All Rights Reserved.
    This software is published under the GPL GNU General Public License.
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

--%>
<%@page import="org.oscarehr.util.LoggedInInfo"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/oscar-tag.tld" prefix="oscar"%>
<%@ page errorPage="errorpage.jsp"%>
<%@ page
	import="java.util.*,java.net.*,java.sql.*,oscar.*,oscar.util.*,oscar.appt.*"%>
<%@ page import="oscar.oscarBilling.ca.on.data.*"%>
<%@ page import="oscar.oscarBilling.ca.on.pageUtil.*"%>
<%@page import="org.oscarehr.common.model.ProviderPreference"%>
<%@page import="org.oscarehr.web.admin.ProviderPreferencesUIBean"%>
<%@page import="org.oscarehr.util.SpringUtils"%>
<%@page import="org.oscarehr.PMmodule.dao.ProviderDao"%>
<%@page import="org.oscarehr.common.dao.DxresearchDAO"%>
<%@page import="org.oscarehr.common.model.Dxresearch"%>
<%@page
	import="oscar.oscarBilling.ca.bc.decisionSupport.BillingGuidelines"%>
<%@page import="org.oscarehr.decisionSupport.model.DSConsequence"%>
<%@page import="org.oscarehr.common.model.Demographic"%>
<%@page import="org.oscarehr.common.model.CtlBillingService, org.oscarehr.common.dao.CtlBillingServiceDao"%>
<%@page import="org.oscarehr.common.model.MyGroup, org.oscarehr.common.dao.MyGroupDao"%>
<%@page import="org.oscarehr.common.model.UserProperty, org.oscarehr.common.dao.UserPropertyDAO"%>
<%@page import="org.oscarehr.common.dao.DiagnosticCodeDao, org.oscarehr.common.model.CtlDiagCode, org.oscarehr.common.model.DiagnosticCode"%>
<%@page import="org.oscarehr.common.dao.CSSStylesDAO, org.oscarehr.common.model.CssStyle" %>
<%@page import="org.oscarehr.common.dao.ClinicNbrDao, org.oscarehr.common.model.ClinicNbr"%>
<%@page import="org.oscarehr.common.dao.CtlBillingTypeDao, org.oscarehr.common.model.CtlBillingType"%>
<%@page import="org.oscarehr.common.dao.CtlBillingServicePremiumDao, org.oscarehr.common.model.CtlBillingServicePremium"%>
<%@page import="org.oscarehr.common.dao.DiagnosticCodeDao, org.oscarehr.common.model.CtlDiagCode, org.oscarehr.common.model.DiagnosticCode"%>
<%@page import="org.oscarehr.common.dao.MyGroupDao, org.oscarehr.common.model.MyGroup"%>
<%@page import="org.oscarehr.common.dao.OscarAppointmentDao, org.oscarehr.common.model.Appointment"%>
<%@page import="org.oscarehr.common.dao.ProfessionalSpecialistDao, org.oscarehr.common.model.ProfessionalSpecialist"%>
<%@page import="org.oscarehr.managers.DemographicManager,org.oscarehr.billing.CA.filters.CodeFilterManager"%>

<%
	ProfessionalSpecialistDao professionalSpecialistDao = (ProfessionalSpecialistDao) SpringUtils.getBean("professionalSpecialistDao");
    LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
	UserPropertyDAO propertyDao = SpringUtils.getBean(UserPropertyDAO.class);
	Map<String, String> provinces = ProvinceNames.getDefaultProvinces();
%>
<jsp:useBean id="providerBean" class="java.util.Properties"
	scope="session" />
<%
			if (session.getAttribute("user") == null) {
				response.sendRedirect("../../../logout.jsp");
			}

		String invoiceID = (String)request.getAttribute("invoiceId");
		String freshbooksProvNo = (String)request.getAttribute("freshbooksProvNo");

                        oscar.OscarProperties oscarVariables = oscar.OscarProperties.getInstance();
			java.text.SimpleDateFormat standardDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");

			FreshbooksService fbs = new FreshbooksService();
			DemographicExtDao ded = SpringUtils.getBean(DemographicExtDao.class);
			CustomHealthcardTypeDao customHealthcardTypeDao = SpringUtils.getBean(CustomHealthcardTypeDao.class);

			String user_no = (String) session.getAttribute("user");
			String providerview = request.getParameter("providerview") == null ? "" : request.getParameter("providerview");
			String asstProvider_no = "", color = "", premiumFlag = "", service_form = "";
			String sql = null;
			ResultSet rs = null;
			String sql0 = null;
			ResultSet rs0 = null;
			String strToday = UtilDateUtilities.getToday("yyyy-MM-dd");
			boolean bSingleClick = oscarVariables.getProperty("onBillingSingleClick", "").equals("yes") ? true : false;
			boolean bHospitalBilling = false;
			String clinicview = bHospitalBilling ? oscarVariables.getProperty("clinic_hospital", "") : oscarVariables.getProperty("clinic_view", "");
			String clinicNo = oscarVariables.getProperty("clinic_no", "").trim();
			String visitType = bHospitalBilling ? "02" : oscarVariables.getProperty("visit_type", "");
			List<BillingServiceSchedule> billingServiceSchedule = new ArrayList<BillingServiceSchedule>();
			List<String> invoiceProviderNos = new ArrayList<String>();

			invoiceProviderNos.add(user_no);

			if (visitType.startsWith("00") || visitType.equals(""))	clinicview = "0000";
			String appt_no = request.getParameter("appointment_no");
                        String billReferenceDate;
                        if( appt_no != null && appt_no.compareTo("0") == 0 ) {
                            billReferenceDate = request.getParameter("service_date")!=null? request.getParameter("service_date"):strToday;
                        }
                        else {
                           billReferenceDate = request.getParameter("appointment_date");
                        }
			String demoname = request.getParameter("demographic_name");
			String demo_no = request.getParameter("demographic_no");
			String apptProvider_no = request.getParameter("apptProvider_no");
			String assgProvider_no = request.getParameter("assgProvider_no");
			String demoSex = request.getParameter("DemoSex");
			String m_review = request.getParameter("m_review")!=null ? request.getParameter("m_review") : "";
			String ctlBillForm = request.getParameter("billForm");
			String curBillForm = request.getParameter("curBillForm");

			String provider_no;
            if( apptProvider_no.equalsIgnoreCase("none") ) {
                     provider_no = user_no;
 			}
			else {
     			provider_no = apptProvider_no;
     			if (!user_no.equals(provider_no)) {
     			    invoiceProviderNos.add(provider_no);
				}
 			}
 			
            CodeFilterManager codeFilterManager = SpringUtils.getBean(CodeFilterManager.class);
            DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
			Demographic demo = demographicManager.getDemographic(loggedInInfo, demo_no); 

			// True - has e-mail, False - doesn't have e-mail
			Boolean demoEmail = demo.getEmail()!=null&&!demo.getEmail().trim().equals("");
			java.util.Date filterDate = ConversionUtils.fromDateString(billReferenceDate);
			String startTime = request.getParameter("start_time");
			if(startTime != null){
	   			filterDate =  ConversionUtils.fromTimestampString(billReferenceDate+" "+ startTime);
	   			if (appt_no != null && !appt_no.equals("0")) {
					billingServiceSchedule = ScheduleBillingServiceAction.load(provider_no, filterDate);
				}
				
 			}

            //check for management fee code eligibility
            StringBuilder billingRecomendations = new StringBuilder();
            try{
            	List<DSConsequence> list = BillingGuidelines.getInstance().evaluateAndGetConsequences(loggedInInfo, request.getParameter("demographic_no"), (String) request.getSession().getAttribute("user"));

            	for (DSConsequence dscon : list){
                     if (dscon.getConsequenceStrength().equals(DSConsequence.ConsequenceStrength.warning)) {
                        billingRecomendations.append(dscon.getText()).append("<br/>");
                     }
                }
            }catch(Exception e){
                    MiscUtils.getLogger().error("Error", e);
            }

            ProviderPreferenceDao preferenceDao = (ProviderPreferenceDao) SpringUtils.getBean("providerPreferenceDao");
            ProviderPreference preference = null;
            preference=ProviderPreferencesUIBean.getProviderPreferenceByProviderNo(provider_no);
            String defaultSliCode = "0000";


           	

			if (curBillForm!=null) {
			    // user picks a bill form from browser
			    ctlBillForm = curBillForm;
			} else {                    
				//check if patient's roster status determines which billing form to display (this superceeds provider preference)                    
				String rosterStatus = demo.getRosterStatus();
				
				CtlBillingServiceDao ctlBillingServiceDao = (CtlBillingServiceDao) SpringUtils.getBean("ctlBillingServiceDao");
				List<CtlBillingService> ctlBillSrvList = ctlBillingServiceDao.findByServiceTypeId(rosterStatus);
				
				if (!ctlBillSrvList.isEmpty() && !rosterStatus.isEmpty()) {
					ctlBillForm = ctlBillSrvList.get(0).getServiceType();
				}
				else {                                                        
					// check user preference to show a bill form
					ProviderPreferenceDao providerPreferenceDao=(ProviderPreferenceDao)SpringUtils.getBean("providerPreferenceDao");
					ProviderPreference providerPreference=null;
				
					//use the appointment provider's preferences first if we can
					//otherwise, use the preferences of the logged in user
					if( apptProvider_no.equalsIgnoreCase("none") ) {                                   
						providerPreference = providerPreferenceDao.find(user_no);
					} else {                                    
						providerPreference = providerPreferenceDao.find(apptProvider_no);
					}

					String defaultServiceType = "";
					if (providerPreference!=null) {
						defaultServiceType = providerPreference.getDefaultServiceType();
						if ((providerPreference.getDefaultSliCode()!=null && !providerPreference.getDefaultSliCode().isEmpty() && !providerPreference.getDefaultSliCode().equals("no"))){
							defaultSliCode = providerPreference.getDefaultSliCode();
						}
					}
					
					if (defaultServiceType != null && !defaultServiceType.isEmpty() && !defaultServiceType.equals("no")) {
						ctlBillForm = providerPreference.getDefaultServiceType();
					} else { 
							//check if there is a group preference for default billing
							MyGroupDao myGroupDao = (MyGroupDao) SpringUtils.getBean("myGroupDao"); 
							List<MyGroup> myGroups = myGroupDao.getProviderGroups(provider_no);
							String groupBillForm = "";
							for (MyGroup group : myGroups) {
								groupBillForm = group.getDefaultBillingForm();
								if (groupBillForm != null && !groupBillForm.isEmpty()) {
									ctlBillForm = groupBillForm;
									break;
								}
							}
						   
							if (ctlBillForm == null || ctlBillForm.isEmpty()) {
								// check oscar.properties to show a default bill form
								String dv = OscarProperties.getInstance().getProperty("default_view");
								if (dv!=null) ctlBillForm = dv;
							}
					}
				}
			}

			if( ctlBillForm == null ) {
				ctlBillForm = "";
			}

			GregorianCalendar now = new GregorianCalendar();
			int curYear = now.get(Calendar.YEAR);
			int curMonth = (now.get(Calendar.MONTH) + 1);
			int curDay = now.get(Calendar.DAY_OF_MONTH);
			int dob_year = 0, dob_month = 0, dob_date = 0, age = 0;

			String msg = "The default unit and @ value is 1.";
			String action = "edit";
			Properties propHist = null;
			Vector vecHist = new Vector();

			if (request.getParameter("xml_provider") != null) {
				providerview = request.getParameter("xml_provider");
				if(providerview.indexOf("|")!=-1)
					providerview = providerview.substring(0,providerview.indexOf("|"));
			}

			// get patient's detail
			String errorFlag = "";
			String warningMsg = "", errorMsg = "";
			String r_doctor = "", r_doctor_ohip = "";
			String demoFirst = "", demoLast = "", demoHIN = "", demoVer = "", demoDOB = "", demoDOBYY = "", demoDOBMM = "", demoDOBDD = "", demoHCTYPE = "";
			String family_doctor = "";
			String roster_status = "";
			String referSpet = "";
			String demoFullHIN = "";
			// last_name,first_name,dob,hin,ver,hc_type,sex,family_doctor
			JdbcBillingPageUtil tdbObj = new JdbcBillingPageUtil();
			List demoL = tdbObj.getPatientCurBillingDemographic(loggedInInfo, demo_no);

			//String sql = "select * from demographic where demographic_no=" + demo_no;
			//ResultSet rs = dbObj.searchDBRecord(sql);
			//while (rs.next()) {
				demoLast = (String)demoL.get(0); //rs.getString("last_name");
				demoFirst = (String)demoL.get(1); //rs.getString("first_name");
				demoDOB = (String)demoL.get(2);
				demoHIN = (String)demoL.get(3); //rs.getString("hin");
				demoVer = (String)demoL.get(4); //rs.getString("ver");
				demoFullHIN = demoHIN;
				if (demoVer != null && !demoVer.trim().isEmpty()) {
				    demoFullHIN +=  " " + demoVer;
				}
				demoHCTYPE = (String)demoL.get(5); //rs.getString("hc_type") == null ? "" : rs.getString("hc_type");
				demoSex = (String)demoL.get(6); //rs.getString("sex");
				family_doctor = (String)demoL.get(7);
				assgProvider_no = (String)demoL.get(8);
				roster_status = (String)demoL.get(9);

				if (demoHCTYPE.compareTo("") == 0 || demoHCTYPE == null || demoHCTYPE.length() < 2) {
					demoHCTYPE = "ON";
				} else if (provinces.containsKey(demoHCTYPE)) {
					demoHCTYPE = demoHCTYPE.substring(0, 2).toUpperCase();
				} else {
					demoHCTYPE = "OT";
				}

				if ("".equals(family_doctor)) {
					r_doctor = "N/A";
					r_doctor_ohip = "000000";
				} else {
					r_doctor = SxmlMisc.getXmlContent(family_doctor, "rd") == null ? "" : SxmlMisc
							.getXmlContent(family_doctor, "rd");
					r_doctor_ohip = SxmlMisc.getXmlContent(family_doctor, "rdohip") == null ? ""
							: SxmlMisc.getXmlContent(family_doctor, "rdohip");
					referSpet = tdbObj.getReferDocSpet(r_doctor_ohip);
				}

				if (demoHIN.equals("")) {
					warningMsg += "<br/><b><div class='myError'>Warning: The patient does not have a valid HIN. </div></b><br/>";
				}
				if (r_doctor_ohip != null && r_doctor_ohip.length() > 0 && r_doctor_ohip.length() != 6) {
					warningMsg += "<br/><div class='myError'>Warning: the referral doctor's no is wrong. </div><br/>";
				}
				if (StringUtils.isBlank(demoDOB) || demoDOB.length() != 8) {
					errorFlag = "1";
					errorMsg = errorMsg
							+ "<br/><b><div class='myError'>Error: The patient does not have a valid DOB. </div></b><br/>";
				}
			//}

			// get patient's billing history
			boolean bFirst = true;
			int maxResults = Integer.parseInt(oscarVariables.getProperty("maxBillingHistResults"));
			JdbcBillingReviewImpl hdbObj = new JdbcBillingReviewImpl();
			List aL = hdbObj.getBillingHist(demo_no, maxResults,0, null);

			Vector vecHistD = new Vector();
			if (aL != null && aL.size()>0) {
				BillingClaimHeader1Data obj = (BillingClaimHeader1Data) aL.get(0);
				BillingItemData iobj = (BillingItemData) aL.get(1);

				propHist = new Properties();

				//propHist.setProperty("billing_no", "" + rs.getInt("id"));
				propHist.setProperty("visitdate", obj.getAdmission_date() == null ? "" : obj.getAdmission_date()); // admission date
				//propHist.setProperty("billing_date", rs.getString("billing_date")); // service date
				//propHist.setProperty("update_date", rs.getString("timestamp")); // create date
				propHist.setProperty("visitType", obj.getVisittype());
				propHist.setProperty("clinic_ref_code", obj.getFacilty_num());
				propHist.setProperty("admissionDate", StringUtils.trimToEmpty(obj.getAdmission_date()));
				propHist.setProperty("visitLocation", StringUtils.trimToEmpty(obj.getFacilty_num()));
				vecHist.add(propHist);
				//propHist.setProperty("service_code", serCode);
				propHist.setProperty("diagnostic_code", iobj.getDxToBill());
				vecHistD.add(propHist);
			}

			// display the fixed billing part
			// Retrieving Provider
			Vector vecProvider = new Vector();
			Properties propT = null;
			ProviderDao dao = SpringUtils.getBean(ProviderDao.class);
			for(Provider p : dao.getProvidersWithNonEmptyCredentials()) {
				propT = new Properties();
				propT.setProperty("last_name", p.getLastName());
				propT.setProperty("first_name", p.getFirstName());
				propT.setProperty("proOHIP", p.getProviderNo() + "|" + p.getOhipNo() + "|" + p.isThirdPartyOnly());
				vecProvider.add(propT);
			}

			// set default value
			// use parameter -> history record
			ProfessionalSpecialist specialist = professionalSpecialistDao.getByReferralNo(r_doctor_ohip);
            if(specialist != null) {
            	r_doctor = specialist.getLastName() + "," + specialist.getFirstName();
            }

			String paraName = request.getParameter("dxCode");
            if(paraName==null || paraName.equals("")) {
            	// get the default diagnostic code
                if(preference!=null) {
                    paraName = preference.getDefaultDxCode();
            	}
            }
            String dxCode = getDefaultValue(paraName, vecHistD, "diagnostic_code");

			//visitType
			paraName = request.getParameter("xml_visittype");

			String xml_visittype = getDefaultValue(paraName, vecHist, "visitType");
			//xml_visittype = paraName != null && !"".equals(paraName)? paraName : "00" ;

			//Creates empty strings for the visitLocation and admissiondate
			String visitLocation = "";
			String admissionDate = "";
			//Checks if the autofill option is enabled in the properties
			if (OscarProperties.getInstance().getBooleanProperty("autofill_billing_date_and_location", "true")) {
				//Checks if the vecHist contaions the properties
				String visitLocationProperty = null;
				String admissionDateProperty = null;
				if(OscarProperties.getInstance().getBooleanProperty("useFirstLocationAndAdmission", "true")){
					List firstBill = hdbObj.getFirstBill(demo_no);
					if(firstBill != null && firstBill.size() > 0){
						BillingClaimHeader1Data obj = (BillingClaimHeader1Data) firstBill.get(0);
						BillingItemData iobj = (BillingItemData) firstBill.get(1);

						propHist.setProperty("admissionDate", StringUtils.trimToEmpty(obj.getAdmission_date()));
						propHist.setProperty("visitLocation", StringUtils.trimToEmpty(obj.getFacilty_num()));
						
						visitLocationProperty = StringUtils.trimToEmpty(obj.getFacilty_num());
						admissionDateProperty = StringUtils.trimToEmpty(obj.getAdmission_date());
					}
				} else if (preference!=null && preference.getDefaultBillingLocation()!=null && !preference.getDefaultBillingLocation().isEmpty() && !preference.getDefaultBillingLocation().equals("no")){
					visitLocationProperty = preference.getDefaultBillingLocation();
				} else if (vecHist != null && vecHist.size() > 0 && vecHist.get(0) != null) {
					visitLocationProperty = ((Properties)vecHist.get(0)).getProperty("visitLocation");
					admissionDateProperty = ((Properties)vecHist.get(0)).getProperty("admissionDate"); 
				}
				visitLocation = visitLocationProperty != null ? visitLocationProperty : "";
				admissionDate = admissionDateProperty != null ? admissionDateProperty : "";
			}

			if (!"".equals(xml_visittype)) {
				visitType = xml_visittype;
			} else {
				visitType = visitType == null ? "" : visitType;
			}

			paraName = request.getParameter("xml_location");
			String xml_location = getDefaultValue(paraName, vecHist, "clinic_ref_code");
			xml_location = paraName != null && !"".equals(paraName)? paraName : "0000";
			if (!"".equals(xml_location)) {
				clinicview = xml_location;
			} else {
				clinicview = clinicview == null ? "" : clinicview;
			}

			//Read default clinic_view from oscar.properties file
			String cv = OscarProperties.getInstance().getProperty("clinic_view");
			if (cv!=null) clinicview = cv;

			String visitdate = null;
			paraName = request.getParameter("xml_vdate");
			String xml_vdate = getDefaultValue(paraName, vecHist, "visitdate");
			xml_vdate = request.getParameter("xml_vdate")!=null? paraName : "" ;
			if (!"".equals(xml_vdate)) {
				visitdate = xml_vdate;
			} else {
				visitdate = visitdate == null ? "" : visitdate;
			}

			// get billing dx/form info
			HashMap<String, ArrayList<Properties>> billingServiceCodesMap = new HashMap<String, ArrayList<Properties>>();
            ArrayList<String> listServiceType = new ArrayList<String>();
            HashMap<String, String> titleMap = new HashMap<String, String>();
            Properties propPremium = new Properties();
            String serviceCode, serviceDesc, serviceValue, servicePercentage, serviceType, displayStyle, serviceDisp = "";
            String headerTitle1 = "", headerTitle2 = "", headerTitle3 = "";

            CSSStylesDAO cssStylesDao = (CSSStylesDAO) SpringUtils.getBean("CSSStylesDAO");
			CssStyle cssStyle;
			String styleId;

            boolean sliFlag = false;

            CtlBillingServiceDao cbsDao = SpringUtils.getBean(CtlBillingServiceDao.class);
            BillingServiceDao bDao = SpringUtils.getBean(BillingServiceDao.class);
            CtlBillingServicePremiumDao pDao = SpringUtils.getBean(CtlBillingServicePremiumDao.class);

            for(Object[] i : cbsDao.findServiceTypesByStatus("A")) {
				ArrayList<Properties> listGroup1 = new ArrayList<Properties>();
				ArrayList<Properties> listGroup2 = new ArrayList<Properties>();
				ArrayList<Properties> listGroup3 = new ArrayList<Properties>();

				String ctlcode = String.valueOf(i[1]);
				String ctlcodename = String.valueOf(i[0]);

				listServiceType.add(ctlcode);
					
				for(Object[] o : bDao.findBillingServiceAndCtlBillingServiceByMagic(ctlcode, "Group1", ConversionUtils.fromDateString(billReferenceDate))) {
					BillingService b = (BillingService) o[0];
					CtlBillingService c = (CtlBillingService) o[1];
									
					if(!codeFilterManager.isCodeValid(b.getServiceCode(), null, false, filterDate, demo)){
						continue;
					}
					
					propT = new Properties();
					propT.setProperty("serviceCode", b.getServiceCode() );
					propT.setProperty("serviceDesc", b.getDescription() == null ? "N/A" : b.getDescription());
					propT.setProperty("serviceDisp", noNull(b.getValue()));
					propT.setProperty("servicePercentage", noNull(b.getPercentage()));
					propT.setProperty("serviceType", c.getServiceType());
					propT.setProperty("serviceTypeName", c.getServiceGroupName());
					styleId = null;
					if(b != null && b.getDisplayStyle() != null) {
						styleId = "" + b.getDisplayStyle();
						cssStyle = cssStylesDao.find(b.getDisplayStyle());
						propT.setProperty("displaystyle", cssStyle.getStyle());
					}
					else {
						propT.setProperty("displaystyle", "");
					}

					propT.setProperty("serviceSLI",  "" + b.getSliFlag());
					titleMap.put("group1_".concat(ctlcode), c.getServiceGroupName());

					listGroup1.add(propT);
				}

				if (listGroup1.size() > 0) {
					List<String> serviceCodes = new ArrayList<String>();
					for (int ii = 0; ii < listGroup1.size(); ii++) {
						serviceCodes.add(listGroup1.get(ii).getProperty("serviceCode"));
					}
					
					for(CtlBillingServicePremium p : pDao.findByServceCodes(serviceCodes)) {
						propPremium.setProperty(p.getServiceCode(), "A");
					}
				}
				billingServiceCodesMap.put("group1_".concat(ctlcode),listGroup1);
				
				for(Object[] o : bDao.findBillingServiceAndCtlBillingServiceByMagic(ctlcode, "Group2", ConversionUtils.fromDateString(billReferenceDate))) {
					BillingService b = (BillingService) o[0];
					CtlBillingService c = (CtlBillingService) o[1];
									
					if(!codeFilterManager.isCodeValid(b.getServiceCode(), null, false, filterDate, demo)){
						continue;
					}
					propT = new Properties();
					
					propT.setProperty("serviceCode", b.getServiceCode() );
					propT.setProperty("serviceDesc", b.getDescription());
					propT.setProperty("serviceDisp", b.getValue());
					propT.setProperty("servicePercentage", noNull(b.getPercentage()));
					propT.setProperty("serviceType", c.getServiceType());
					propT.setProperty("serviceTypeName", c.getServiceGroupName());
					styleId = null;
					if(b != null && b.getDisplayStyle() != null) {
						styleId = "" + b.getDisplayStyle();
						cssStyle = cssStylesDao.find(b.getDisplayStyle());
						propT.setProperty("displaystyle", cssStyle.getStyle());
					}
					else {
						propT.setProperty("displaystyle", "");
					}

					propT.setProperty("serviceSLI", "" + b.getSliFlag());
					titleMap.put("group2_".concat(ctlcode), c.getServiceGroupName());

					listGroup2.add(propT);

				}

				if (listGroup2.size() > 0) {
					List<String> serviceCodes = new ArrayList<String>();
					for (int ii = 0; ii < listGroup2.size(); ii++) {
						serviceCodes.add(listGroup2.get(ii).getProperty("serviceCode"));
					}
					
					for(CtlBillingServicePremium p : pDao.findByServceCodes(serviceCodes)) {
						propPremium.setProperty(p.getServiceCode(), "A");
					}
				}
				
				billingServiceCodesMap.put("group2_".concat(ctlcode),listGroup2);
				
				for(Object[] o : bDao.findBillingServiceAndCtlBillingServiceByMagic(ctlcode, "Group3", ConversionUtils.fromDateString(billReferenceDate))) {
					BillingService b = (BillingService) o[0];
					CtlBillingService c = (CtlBillingService) o[1];

					if(!codeFilterManager.isCodeValid(b.getServiceCode(), null, false, filterDate, demo)){
						continue;
					}
					
					propT = new Properties();
					
					propT.setProperty("serviceCode", b.getServiceCode() );
					propT.setProperty("serviceDesc", b.getDescription());
					propT.setProperty("serviceDisp", b.getValue());
					propT.setProperty("servicePercentage", noNull(b.getPercentage()));
					propT.setProperty("serviceType", c.getServiceType());
					propT.setProperty("serviceTypeName", c.getServiceGroupName());
					styleId = null;
					if(b != null && b.getDisplayStyle() != null) {
						styleId = "" + b.getDisplayStyle();
						cssStyle = cssStylesDao.find(b.getDisplayStyle());
						propT.setProperty("displaystyle", cssStyle.getStyle());
					}
					else {
						propT.setProperty("displaystyle", "");
					}

					propT.setProperty("serviceSLI", "" + b.getSliFlag());
					titleMap.put("group3_".concat(ctlcode), c.getServiceGroupName());

					listGroup3.add(propT);

				}

				if (listGroup3.size() > 0) {
					List<String> serviceCodes = new ArrayList<String>();
					for (int ii = 0; ii < listGroup3.size(); ii++) {
						serviceCodes.add(listGroup3.get(ii).getProperty("serviceCode"));
					}
					
					for(CtlBillingServicePremium p : pDao.findByServceCodes(serviceCodes)) {
						propPremium.setProperty(p.getServiceCode(), "A");
					}
				}

				billingServiceCodesMap.put("group3_".concat(ctlcode),listGroup3);

            }

           CtlBillingTypeDao tDao = SpringUtils.getBean(CtlBillingTypeDao.class);
           String defaultBillType = "";
           for(CtlBillingType t : tDao.findByServiceType(ctlBillForm)){
            	defaultBillType = t.getBillType();
           }

			List<CustomHealthcardType> customHealthcardTypes = customHealthcardTypeDao.findByName(demo.getHcType());
           if ("OT".equals(demo.getHcType()) || "QC".equals(demo.getHcType()) || customHealthcardTypes.size() > 0) {
               ctlBillForm = "PRI";
               UserProperty defaultServiceTypeForHc = propertyDao.getProp(loggedInInfo.getLoggedInProviderNo(), "OT".equals(demo.getHcType()) ? UserProperty.DEFAULT_SERVICE_OTHER : UserProperty.DEFAULT_SERVICE_QUEBEC);
			   if (defaultServiceTypeForHc != null && defaultServiceTypeForHc.getValue() != null && !defaultServiceTypeForHc.getValue().isEmpty() && !defaultServiceTypeForHc.getValue().equalsIgnoreCase("no")) {
			       ctlBillForm = defaultServiceTypeForHc.getValue();
			   }
			   defaultBillType = "PAT";
		   }

			// create msg
			msg += errorMsg + warningMsg;
           
           boolean hideDxBoxes = Boolean.parseBoolean(oscarVariables.getProperty("hide_extra_dx_codes"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.oscarehr.util.MiscUtils"%>
<%@page import="org.oscarehr.util.SpringUtils"%>
<%@page import="org.oscarehr.common.model.ProviderPreference"%>
<%@ page import="org.oscarehr.common.model.*" %>
<%@ page import="org.oscarehr.common.dao.*" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="javax.swing.*" %>
<%@ page import="oscar.oscarDemographic.data.ProvinceNames" %>
<%@ page import="org.oscarehr.billing.ScheduleBillingServiceAction" %>
<html>
<head>
<title>Ontario Billing</title>
<style type="text/css">
<!--
.demo1 {
	color: #000033;
	background-color: silver;
	layer-background-color: #cccccc;
	position: absolute;
	top: 50px;
	left: 170px;
	width: 190px;
	height: 80px;
	z-index: 99;
	visibility: hidden;
}
-->
</style>
<link rel="stylesheet" type="text/css" href="billingON.css" />
<!-- calendar stylesheet -->
<link rel="stylesheet" type="text/css" media="all"
	href="../../../share/calendar/calendar.css" title="win2k-cold-1" />
<!-- main calendar program -->
<script type="text/javascript" src="../../../share/calendar/calendar.js"></script>
<!-- language for the calendar -->
<script type="text/javascript"
	src="../../../share/calendar/lang/calendar-en.js"></script>
<!-- the following script defines the Calendar.setup helper function, which makes
       adding a calendar a matter of 1 or 2 lines of code. -->
<script type="text/javascript"
	src="../../../share/calendar/calendar-setup.js"></script>

<script type="text/javascript"
	src="../../../share/javascript/prototype.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/share/javascript/Oscar.js"></script>
<script type="text/javascript"
	src="<%=request.getContextPath() %>/js/jquery.js"></script>
<script type="text/javascript" language="JavaScript">
	jQuery.noConflict();
</script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery-3.1.0.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.maskedinput.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/global.js"></script>	
<script type="application/javascript">
	var jQuery_3_1_0 = jQuery.noConflict(true);
</script>
<oscar:customInterface section="billing"/>
<script>
function changeBillType(selection){
    var isThirdPartyOnly = selection.split("|")[2];
    if(isThirdPartyOnly == 'true'){
        document.forms[0].xml_billtype.value = "PAT | Bill Patient";
        onChangePrivate();
	}
}

function gotoBillingOB() {
    if(self.location.href.lastIndexOf("?") > 0) {
	a = self.location.href.substring(self.location.href.lastIndexOf("?"));
    }
    self.location.href = "billingOB.jsp" + a ;
}

function findObj(n, d) { //v4.0
    var p,i,x;
    if(!d) d=document;
    if((p=n.indexOf("?"))>0&&parent.frames.length) {
	d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);
    }
    if(!(x=d[n])&&d.all) x=d.all[n];
    for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
    for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=findObj(n,d.layers[i].document);
    if(!x && document.getElementById) x=document.getElementById(n);
    return x;
}

function showHideLayers() { //v3.0
    var i,p,v,obj,args=showHideLayers.arguments;
    for (i=0; i<(args.length-2); i+=3) {
	if ((obj=findObj(args[i]))!=null) {
	    v=args[i+2];
	    if (obj.style) {
		obj=obj.style;
		v=(v=='show')?'visible':(v='hide')?'hidden':v;
	    }
	    obj.visibility=v;
	}
    }
}

function onNext() {
    var ret = true;
    <% if (org.oscarehr.common.IsPropertiesOn.isMultisitesEnable()) { %>
        if (!checkSiteSelect()) {
			alert("You haven't selected a clinic!");
        	return false;
        }
    <% } %>
	
    if (!checkProviderSelect()) {
		alert("You haven't selected a provider!");
		ret = false;
	} else if (!checkAllDates()) {
    	ret = false;
    }else if(!checkServicePercent()){
    	ret = false;
    	alert("Please enter a decimal number in the service code percent textbox!");
    } else if (!existServiceCode() && document.forms[0].services_checked.value<=0) {
	    ret = false;
	    alert("You haven't selected any billing item yet!");
	} else if (!checkServiceUnits()) {
    	ret = false;
    	alert("Service Units must be whole numbers");
	} else if (!checkSli()) {
		ret = false;
		alert("You have selected billing codes that require an SLI code but have not provided an SLI code.");
	}
	else if (document.forms[0].dxCode.value=="") {
	    ret = confirm("You didn't enter a diagnostic code in the Dx box. Continue?");
	    if (!ret) document.forms[0].dxCode.focus();
	}
    return ret;
}

function checkProviderSelect() {
	var providerSelectElement = document.getElementsByName('xml_provider')[0];
	if (providerSelectElement.options.length === 0 || providerSelectElement.selectedIndex == -1) {
		return false;
	}
	return true;
}
function checkSiteSelect() {
	var siteSelectElement = document.getElementsByName('site')[0];
	if (siteSelectElement.options.length === 0 || siteSelectElement.options[siteSelectElement.selectedIndex].value == 'none') {
		return false;
	}
	return true;
}

function checkServicePercent() {
	var ret = true;
	var regInt = /^-?\d+\.\d+$/;
	jQuery("input[id^='serviceAt'][value!='']").each(function() {
		var val = this.value.trim();
		if (val.length > 0 && !regInt.test(val)) {
			ret = false;
			return false;
		}
	});
	return ret;
}

function checkSli() {
	var needsSli = false;
    jQuery("input[name^=xml_]:checked").each(function() {
            needsSli = needsSli || eval(jQuery("input[name='sli_xml_" + this.name.substring(4) + "']").val());
    });
    jQuery("input[name^=serviceCode][value!='']").each(function() {
            needsSli = needsSli || eval(jQuery("input[name='sli_xml_" + this.value + "']").val());
    });
    return !needsSli || jQuery("select[name='xml_slicode']").get(0).selectedIndex != 0;
}

function checkAllDates() {
    var b = true;

    if(document.forms[0].xml_provider.value=="000000"){
		alert("Please select a provider.");
		b = false;
    }
    <% if (!OscarProperties.getInstance().getBooleanProperty("rma_enabled", "true")) { %>
    else if(document.forms[0].xml_visittype.options[2].selected && (document.forms[0].xml_vdate.value=="" || document.forms[0].xml_vdate.value=="0000-00-00")) {
		alert("Need an admission date.");
		b = false;
    }
    <% } %>
    else if(document.forms[0].xml_vdate.value.length>0) {
		b = checkServiceDate(document.forms[0].xml_vdate.value);
    } else if(document.forms[0].referralCode.value.length>0) {
		if(document.forms[0].referralCode.value.length!=6 || !isInteger(document.forms[0].referralCode.value)) {
		    alert("Wrong referral code!");
		    b = false;
		}
    }
    return b;
}

function checkServiceDate(s) {
	var calDate=new Date();
	varYear = calDate.getFullYear();
	varMonth = calDate.getMonth()+1;
	varDate = calDate.getDate();
	var str_date = s; //document.forms[0].xml_appointment_date.value;
	var yyyy = str_date.substring(0, str_date.indexOf("-"));
	var mm = str_date.substring(eval(str_date.indexOf("-")+1), str_date.lastIndexOf("-"));
	var dd = str_date.substring(eval(str_date.lastIndexOf("-")+1));
	var bWrongDate = false;
	sMsg = "";
	if(yyyy > varYear) {
		sMsg = "year";
		bWrongDate = true;
	} else if(yyyy == varYear && mm > varMonth) {
		sMsg = "month";
		bWrongDate = true;
	} else if(yyyy == varYear && mm == varMonth && dd > varDate) {
		sMsg = "date";
		bWrongDate = true;
	}
	if(bWrongDate) {
		alert("You may have a wrong Service/admission Date!" + " Wrong " + sMsg);
		return false;
	} else {
		return true;
	}
}

function existServiceCode() {
    b = false;

    if (document.forms[0].serviceCode0.value!="") b=true;
<% for (int i = 1; i < BillingDataHlp.FIELD_SERVICE_NUM; i++) { %>
    else if (document.forms[0].serviceCode<%=i%>.value!="") b=true;
<% } %>

    return b;
}

function isInteger(s){
    var i;
    for (i = 0; i < s.length; i++){
	// Check that current character is number.
	var c = s.charAt(i);
	if (((c < "0") || (c > "9"))) return false;
    }
    // All characters are numbers.
    return true;
}

function isServiceCode(s){
    // temp for 0.
    if(s.length==0) return true;
    if(s.length!=5) return false;
    if((s.charAt(0) < "A") || (s.charAt(0) > "Z")) return false;
    if((s.charAt(4) < "A") || (s.charAt(4) > "Z")) return false;

    var i;
    for (i = 1; i < s.length-1; i++){
	// Check that current character is number.
	var c = s.charAt(i);
	if (((c < "0") || (c > "9"))) return false;
    }
    return true;
}

function isChecked(s) {
    for (var i =0; i <document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.indexOf(s)==0 && document.forms[0].elements[i].name.length==14) {
            if (document.forms[0].elements[i].checked) {
				return true;
	    }
    	}
    }
    return false;
}

var remote=null;
function rs(n,u,w,h,x) {
    args="width="+w+",height="+h+",resizable=yes,scrollbars=yes,status=0,top=60,left=30";
    remote=window.open(u,n,args);
    //if (remote != null) {
    //  if (remote.opener == null)
    //    remote.opener = self;
    //}
    //if (x == 1) { return remote; }
}

var awnd=null;
function referralScriptAttach(elementName) {
     var d = elementName;
     t0 = escape("document.forms[0].elements[\'"+d+"\'].value");
     //t1 = escape("");
     //alert(('searchRefDoc.jsp?param='+t0));
     awnd=rs('att',('searchRefDoc.jsp?param='+t0),600,600,1);
     awnd.focus();
}

function referralScriptAttach2(elementName, name2) {
     var d = elementName;
     t0 = escape("document.forms[0].elements[\'"+d+"\'].value");
     t1 = escape("document.forms[0].elements[\'"+name2+"\'].value");
     awnd=rs('att',('searchRefDoc.jsp?param='+t0+'&param2='+t1+'&submit=Search&keyword='+document.forms[0].elements[name2].value),600,600,1);
     awnd.focus();
}

function dxScriptAttach(name2) {
	ff = eval("document.forms[0].elements['" +name2+"']");
	f0 = ff.value;
	f1 = escape("document.forms[0].elements[\'"+name2+"\'].value");
	awnd=rs('att','billingDigSearch.jsp?name='+f0 + '&search=&name2='+f1,600,600,1);
	awnd.focus();
}

function scScriptAttach(nameF) {
	f0 = escape(nameF.value);
	f1 = escape("document.forms[0].elements[\'"+nameF.name + "\'].value");
	awnd=rs('att','billingCodeSearch.jsp?name='+f0 + '&search=&name1=&name2=&nameF='+f1,600,600,1);
	awnd.focus();
}

function onDblClickServiceCode(item) {
	if(document.forms[0].serviceCode0.value=="") {
		document.forms[0].serviceCode0.value = item.id.substring(3);
	}
<% for(int i=1; i<BillingDataHlp.FIELD_SERVICE_NUM; ++i) { %>
	else if(document.forms[0].serviceCode<%=i%>.value=="") {
		document.forms[0].serviceCode<%=i%>.value = item.id.substring(3);
	}
<% } %>
}

function onClickServiceCode(item) {
	if(document.forms[0].serviceCode0.value=="") {
		document.forms[0].serviceCode0.value = item.id.substring(4);
	}
<% for(int i=1; i<BillingDataHlp.FIELD_SERVICE_NUM; ++i) { %>
	else if(document.forms[0].serviceCode<%=i%>.value=="") {
		document.forms[0].serviceCode<%=i%>.value = item.id.substring(4);
	}
<% } %>
}

function upCaseCtrl(ctrl) {
	var n = document.forms[0].xml_billtype.selectedIndex;
	var val = document.forms[0].xml_billtype[n].value;
	if(val.substring(0,3) == "ODP" || val.substring(0,3) == "WCB" || val.substring(0,3) == "BON") ctrl.value = ctrl.value.toUpperCase();
}

function changeTypeLocation(dropdown){

}
function changeCut(dropdown) {
	var str = dropdown.options[dropdown.selectedIndex].value;
	var temp = new Array();
	temp =  str.split(" ");

	var codes = new Array();
	codes = temp[0].split('\|');
	var visitCode = temp[1].split('\|')[0].replace('[', '');
	var locationCode = temp[1].split('\|')[1].replace(']', '');
	//alert(temp);
	var tlen = codes.length;
	//alert(tlen);
	document.forms[0].dxCode.value="";
	document.forms[0].dxCode1.value="";
	document.forms[0].dxCode2.value="";
	var n = 0;
	for(var i=0; i<<%=BillingDataHlp.FIELD_SERVICE_NUM %>; ++i) {
		ocode = eval("document.forms[0].serviceCode" + i);
		ounit = eval("document.forms[0].serviceUnit" + i);
		operc = eval("document.forms[0].serviceAt" + i);
		//alert(i+":"+n+"|"+temp[n]);
		ocode.value	= "";
		ounit.value	= "";
		operc.value	= "";
		if(i<tlen && codes[n].length==5) {
			ocode.value	= codes[n];
			ounit.value	= codes[n+1];
			operc.value	= codes[n+2];
			n=n+3;
		} else if(i<tlen && codes[n].length==3) {
			if(document.forms[0].dxCode.value=="") {
				document.forms[0].dxCode.value=codes[n];
			} else if(document.forms[0].dxCode1.value=="") {
				document.forms[0].dxCode1.value=codes[n];
			} else if(document.forms[0].dxCode2.value=="") {
				document.forms[0].dxCode2.value=codes[n];
			}
			n=n+1;
		}
	}
	if (document.forms[0].dxCode.value=="" && document.forms[0].dxCode1.value=="" && document.forms[0].dxCode2.value=="") {
		document.forms[0].dxCode.value='<%=request.getParameter("dxCode")!=null?request.getParameter("dxCode"):dxCode%>';
		document.forms[0].dxCode1.value='<%=request.getParameter("dxCode1")!=null?request.getParameter("dxCode1"):""%>';
		document.forms[0].dxCode2.value='<%=request.getParameter("dxCode2")!=null?request.getParameter("dxCode2"):""%>';
		}

	if (visitCode.trim().length>0){
        jQuery('select[name=xml_visittype] option').each(function(){
            if (this.value.startsWith(visitCode) ) {
                this.selected = true;
            }
        });
	}
    if (locationCode.trim().length>0){
        jQuery('select[name=xml_location] option').each(function(){
            if (this.value.startsWith(locationCode) ) {
                this.selected = true;
            }
        });
    }
}

function popupPage(vheight,vwidth,varpage) { //open a new popup window
    var page = "" + varpage;
    windowprops = "height="+vheight+",width="+vwidth+",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0";
    var popup=window.open(page, "billingfavourite", windowprops);
    if (popup != null) {
	if (popup.opener == null) popup.opener = self;
	popup.focus();
    }
}

function onClickRefDoc() {
    if (!document.forms[0].rfcheck.checked) {
	document.forms[0].referralCode.value="";
	document.forms[0].referralDocName.value="";
	document.forms[0].referralSpet.value="";
    } else {
	document.forms[0].referralCode.value="<%=r_doctor_ohip%>";
	document.forms[0].referralDocName.value="<%=r_doctor%>";
	document.forms[0].referralSpet.value="<%=referSpet%>";
    }
}
function validateEmail(email)
{
    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}

function checkServiceUnit(serviceUnitField){
	let thirdParty = (document.forms[0].xml_billtype.value.substring(0, 3) === "PAT");
	let fieldName = serviceUnitField.name;
	
	if (!thirdParty && serviceUnitField != null && !validateInteger(serviceUnitField.value)) {
		document.getElementById(fieldName + "_error").show();
		serviceUnitField.select();
	} else {
		document.getElementById(fieldName + "_error").hide();
	}
}

function checkServiceUnits() {
	let allValid = true;
	let thirdParty = (document.forms[0].xml_billtype.value.substring(0, 3) === "PAT");
	if (!thirdParty) {
		for (let i = 0; i < 10; i++) {
			let serviceUnit = jQuery("#serviceUnit" + i);
			if (serviceUnit != null && !validateInteger(serviceUnit.val())) {
				allValid = false;
				break;
			}
		}
	}
	
	return allValid;
}

function validateInteger(number) {
	var valid = true;

	if (number != null && number.trim() !== "") {
		valid = Number.isInteger(Number(number));
	}

	return valid;
}

function onChangePrivate() {
    var n = document.forms[0].xml_billtype.selectedIndex;
    var val = document.forms[0].xml_billtype[n].value;
    var n2 = document.forms[0].xml_provider.selectedIndex;
    var providerSelect = document.getElementsByName('xml_provider')[0];
    var selectedProviderStr = '&providerview=<%=providerview%>';
    if (providerSelect.options.length !== 0) {
		selectedProviderStr = '&providerview=' + providerSelect.options[providerSelect.selectedIndex].value.split("|")[0];
	}
    if (val.substring(0, 3) == "PAT" || val.substring(0, 3) == "OCF" || val.substring(0, 3) == "ODS" || val.substring(0, 3) == "CPP" || val.substring(0, 3) == "STD") {
        self.location.href = "billingON.jsp?curBillForm=<%="PRI"%>&hotclick=<%=URLEncoder.encode("","UTF-8")%>&appointment_no=<%=request.getParameter("appointment_no")%>&demographic_name=<%=URLEncoder.encode(demoname,"UTF-8")%>&demographic_no=<%=request.getParameter("demographic_no")%>&xml_billtype="+val.substring(0,3)+"&apptProvider_no=<%=request.getParameter("apptProvider_no")%>"+selectedProviderStr+"&appointment_date=<%=request.getParameter("appointment_date")%>&status=<%=request.getParameter("status")%>&start_time=<%=request.getParameter("start_time")%>&bNewForm=1";
    }
    else if (val.substring(0, 3) == "BON") {
        self.location.href = "billingON.jsp?curBillForm=<%=oscarVariables.getProperty("primary_care_incentive", "").trim()%>&hotclick=<%=URLEncoder.encode("","UTF-8")%>&appointment_no=<%=request.getParameter("appointment_no")%>&demographic_name=<%=URLEncoder.encode(demoname,"UTF-8")%>&demographic_no=<%=request.getParameter("demographic_no")%>&xml_billtype="+val.substring(0,3)+"&apptProvider_no=<%=request.getParameter("apptProvider_no")%>&providerview="+selectedProvider+"&appointment_date=<%=request.getParameter("appointment_date")%>&status=<%=request.getParameter("status")%>&start_time=<%=request.getParameter("start_time")%>&bNewForm=1";    }
    else if (val.substring(0, 3) == "K3P")
    {
        if (document.forms[0].xml_provider[n2].value == "000000")
        {
            alert("You must select a provider before proceeding!");
            document.forms[0].xml_billtype.selectedIndex = 0;
        }
        else
        {
            var hasEmail = '<%=demoEmail%>';
            var provNo = document.forms[0].xml_provider[n2].value;
            var apptNo = '<%=request.getParameter("appointment_no")%>';
            var serviceDate = '<%=request.getParameter("appointment_date")%>';

            if (hasEmail=='true')
            {
                var email = '<%=demo.getEmail()%>';
                var demoNo = '<%=demo.getDemographicNo()%>';
                var data = "submit=validate&email="+email+"&provNo="+provNo+"&demoNo="+demoNo+"&isNewEmail=false&apptNo=" + apptNo + "&serviceDate=" + serviceDate;
                var url = "<%=request.getContextPath()%>/FreshbooksAction.do";

                new Ajax.Request(url,{method:'post',parameters:data, onSuccess:function(transport)
				{
				    window.location.href=transport.responseText;
                },
                    onFailure:function()
					{
                        alert("Something went wrong! Please contact Kai Support.\n\n Error: Failed to write to Freshbooks with demographic E-mail. Reference: BO.JSPL-1018\nU");
                    }});
            }
            else
            {
                var emailOption = prompt("No E-mail on file for <%=demo.getDisplayName().replaceAll("\"", "\\\\\"")%>!\nEnter an E-mail for <%=demo.getDisplayName().replaceAll("\"", "\\\\\"")%>, or leave blank and click OK to continue", "");

                if (emailOption!==null) // If they didn't click cancel
                {
                    if(emailOption.toString().trim().length > 0) // If the demo has no e-mail but they enter one
                    {
                        while (!validateEmail(emailOption)) // If they have a valid e-mail, use that for the invoice
                        { // If the email is not valid, ...
							if (emailOption==null) {break;}
							if (emailOption=='') {break;}
                            alert("The e-mail you entered is not valid! Please try again.");
                            document.forms[0].xml_billtype.selectedIndex = 0;

                            emailOption = prompt("No E-mail on file for <%=demo.getDisplayName().replaceAll("\"", "\\\\\"")%>!\nEnter an E-mail for <%=demo.getDisplayName().replaceAll("\"", "\\\\\"")%>, or leave blank and click OK to continue", "");
                        }
                        if (emailOption!=null || emailOption == '')
						{
                            var email = emailOption;
                            var demoNo = '<%=demo.getDemographicNo()%>';
                            var data = "submit=validate&email="+email+"&provNo="+provNo+"&demoNo="+demoNo+"&isNewEmail=true&apptNo=" + apptNo + "&serviceDate=" + serviceDate;
                            var url = "<%=request.getContextPath()%>/FreshbooksAction.do";

                            new Ajax.Request(url,{method:'post',parameters:data, onSuccess:function(transport)
                            {
                                window.location.href=transport.responseText;
                            },
                                onFailure:function()
                                {
                                    alert("Something went wrong! Please contact Support.\n\n ERR: Failed to write to Freshbooks with prompted E-mail. Code: BO.JSPL-1051");
                                }});
						}
                    }
                    else
                    {
                        var demoNo = '<%=demo.getDemographicNo()%>';
                        var data = "submit=validate&provNo="+provNo+"&demoNo="+demoNo+"&isNewEmail=false&apptNo=" + apptNo + "&serviceDate=" + serviceDate;
                        var url = "<%=request.getContextPath()%>/FreshbooksAction.do";

                        new Ajax.Request(url,{method:'post',parameters:data, onSuccess:function(transport)
                        {
                            window.location.href=transport.responseText;
                        },
                            onFailure:function()
                            {
                                alert("Something went wrong! Please contact Support.\n\n ERR: Failed to write to Freshbooks with no E-mail. Code: BO.JSPL-1068");
                            }});

                    }
                }
                else // If the user clicked cancel, set the billing type select box back to the top and cancel the creation
                {
                    document.forms[0].xml_billtype.selectedIndex = 0;
                }
            }
        }
    }
    else {
<% if(ctlBillForm.equals("PRI") ) {%>
        self.location.href = "billingON.jsp?curBillForm=<%=oscarVariables.getProperty("default_view", "").trim()%>&hotclick=<%=URLEncoder.encode("","UTF-8")%>&appointment_no=<%=request.getParameter("appointment_no")%>&demographic_name=<%=URLEncoder.encode(demoname,"UTF-8")%>&demographic_no=<%=request.getParameter("demographic_no")%>&xml_billtype="+val.substring(0,3)+"&apptProvider_no=<%=request.getParameter("apptProvider_no")%>&providerview=<%=request.getParameter("apptProvider_no")%>&appointment_date=<%=request.getParameter("appointment_date")%>&status=<%=request.getParameter("status")%>&start_time=<%=request.getParameter("start_time")%>&bNewForm=1";
<% } %>
  	}
}

function showHideBox(layerName, iState) { // 1 visible, 0 hidden
    if(document.layers)	{   //NN4+
	document.layers[layerName].visibility = iState ? "show" : "hide";
    } else if(document.getElementById) {  //gecko(NN6) + IE 5+
        var obj = document.getElementById(layerName);
        obj.style.visibility = iState ? "visible" : "hidden";
    } else if(document.all) {	// IE 4
        document.all[layerName].style.visibility = iState ? "visible" : "hidden";
    }
}

function onHistory(type) {
    <%
        DemographicDao demographicDao = (DemographicDao)SpringUtils.getBean("demographicDao");
        Demographic demographic = demographicDao.getDemographic(demo_no);
    %>
    
    var dd = document.forms[0].day.value;
    //alert(dd);
	
	if (type == 'day')
	{
        popupPage(800,640,"billingONHistorySpec.jsp?demographic_no=<%=demo_no%>&demo_name=<%=URLEncoder.encode(demoname,"UTF-8")%>&orderby=appointment_date&day=" + dd); 
	}
	else
	{
        popupPage(800,640,'billinghistory.jsp?demographic_no=<%=demographic.getDemographicNo()%>&last_name=<%=URLEncoder.encode(demographic.getLastName())%>&first_name=<%=URLEncoder.encode(demographic.getFirstName())%>&orderby=appointment_date&displaymode=appt_history&dboperation=appt_history&limit1=0&limit2=10');
	}
	
    
}

function prepareBack() {
    document.forms[0].services_checked.value = "<%=request.getParameter("services_checked")%>";
    if (document.forms[0].services_checked.value=="null") document.forms[0].services_checked.value = 0;
    document.forms[0].url_back.value = location.href;

    showBillFormDiv ("group1_", "<%=ctlBillForm%>");
    showBillFormDiv ("group2_", "<%=ctlBillForm%>");
    showBillFormDiv ("group3_", "<%=ctlBillForm%>");
    showBillFormDiv ("dxCodeSearchDiv_", "<%=ctlBillForm%>");

}

function showBillFormDiv (group, selectedForm) {
    var selectedFormDivGroupId = group + selectedForm;
    var thisDiv = document.getElementById(selectedFormDivGroupId);
    if (thisDiv)
    {
    if (thisDiv.style.display == "none") {
            thisDiv.style.display = "block";
    }
    }
    else {
    //alert("Error: Could not locate div with id: " + selectedFormDivGroupId);
    	//do nothing
    }
}

function hideBillFormDiv (group, selectedForm) {
    var selectedFormDivGroupId = group + selectedForm;
    var thisDiv = document.getElementById(selectedFormDivGroupId);
    if (thisDiv)
    {
    if (thisDiv.style.display == "block") {
            thisDiv.style.display = "none";
    }
    }
    else {
    //alert("Error: Could not locate div with id: " + selectedFormDivGroupId);
        //do nothing
    }
}

function refreshServicesChecked(chkd) {
    var name_id = chkd.name;
	if (chkd.checked) {
            document.forms[0].services_checked.value++;

            //check other checkbox with same name
            for(i=0;i<document.forms[0][name_id].length; i++) {
                    document.forms[0][name_id][i].checked = true;
            }
	} else {
            document.forms[0].services_checked.value--;

            //uncheck other checkbox with same name
            for(i=0;i<document.forms[0][name_id].length; i++) {
                    document.forms[0][name_id][i].checked = false;
          	}
	}
}


function callChangeCodeDesc() {
    setTimeout("changeCodeDesc();",10);
}

function changeCodeDesc() {
    var url  = "billingON_dx_desc.jsp";
    var pars = "diagnostic_code=" + document.forms[0].dxCode.value;

    var descAjax = new Ajax.Updater("code_desc",url, {method: "get", parameters: pars});
}

function setBillingPreferences(){
    document.forms[0].xml_slicode.value = '<%=defaultSliCode%>';
}

//this function will show the content within the <div> tag of billing codes
function toggleDiv(selectedBillForm, selectedBillFormName,billType,visitType, location)
{
        document.getElementById("billForm").value=selectedBillForm;
        document.getElementById("billFormName").value=selectedBillFormName;

        if(billType != ''){
        	for (var i=0;i<document.forms[0].xml_billtype.options.length;i++) {
        	    if (document.forms[0].xml_billtype.options[i].value.substring(0,3) == billType){
        	    	document.forms[0].xml_billtype.options[i].selected = true;
        		}
        	}
        }

        if(visitType!=''){
            for (var i=0;i<document.forms[0].xml_visittype.options.length;i++) {
                if (document.forms[0].xml_visittype.options[i].value.substring(0,2) == visitType){
                    document.forms[0].xml_visittype.options[i].selected = true;
                }
            }
		}

		if(location!='')
		{
            for (var i=0;i<document.forms[0].xml_location.options.length;i++) {
                if (document.forms[0].xml_location.options[i].value.substring(0,4) == location){
                    document.forms[0].xml_location.options[i].selected = true;
                }
            }
		}

        //dx search
        showBillFormDiv("dxCodeSearchDiv_",selectedBillForm);

        //var selectedForm = selectedBillForm.options[selectedBillForm.selectedIndex].value;
        showBillFormDiv ("group1_", selectedBillForm);
        showBillFormDiv ("group2_", selectedBillForm);
        showBillFormDiv ("group3_", selectedBillForm);

        //hide other billing codes whose forms are not selected
<%
        for( int j=0; j< listServiceType.size(); j++) {
                String st = listServiceType.get(j);
 %>
        if(selectedBillForm!="<%=st%>") {

                hideBillFormDiv ("group1_", "<%=st%>");
                hideBillFormDiv ("group2_", "<%=st%>");
                hideBillFormDiv ("group3_", "<%=st%>");

                hideBillFormDiv ("dxCodeSearchDiv_", "<%=st%>");
        }

        <% }  %>
}





//-->
</script>
</head>

<body onload="prepareBack();changeCodeDesc();setBillingPreferences();" topmargin="0">
	<div id="Instrdiv" class="demo1">

		<table bgcolor='#007FFF' width='99%'>
			<tr>
				<th align='right'><a href=#
					onclick="showHideBox('Instrdiv',0); return false;"><font
						color="red">X</font></a></th>
			</tr>
			<tr>
				<th><a href=#
					onclick="showHideBox('Instrdiv',0); return false;"><font
						color="#66FF66">Double clicking any code below will move up
						to specialist billing.</font><br>&nbsp;</a></th>
			</tr>

		</table>
	</div>
	<div id="Layer1"
		style="position: absolute; left: 360px; top: 165px; width: 410px; height: 210px; z-index: 1; background-color: #FFCC00; layer-background-color: #FFCC00; border: 1px none #000000; visibility: hidden">
		<table width="98%" border="0" cellspacing="0" cellpadding="0"
			align=center>
			<tr bgcolor="#393764">
				<td width="96%" height="7" bgcolor="#FFCC00"><font size="-2"
					face="Geneva, Arial, Helvetica, san-serif" color="#000000">
					<b>Billing Form</b></font></td>
				<td width="3%" bgcolor="#FFCC00" height="7"><b><a href="#"
						onclick="showHideLayers('Layer1','','hide');return false;">X</a></b></td>
			</tr>
			<%
		String ctlcode = "", ctlcodename = "", currentFormName = "";
		int ctlCount = 0;
		CtlBillingServiceDao ctlBillingServiceDao = SpringUtils.getBean(CtlBillingServiceDao.class);
		CtlBillingTypeDao  ctlBillingtypeDao = SpringUtils.getBean(CtlBillingTypeDao.class);
		for(Object[] bs : ctlBillingServiceDao.findServiceTypesByStatus("A")) {
			ctlcode = String.valueOf(bs[1]);
			ctlcodename = String.valueOf(bs[0]);
			ctlCount++;
			if (ctlcode.equals(ctlBillForm)) {
			    currentFormName = ctlcodename;
			}
			String billType = "";
			String formVisitType = "";
			String formLocation = "";
			
			for(CtlBillingType bt : ctlBillingtypeDao.findByServiceType(ctlcode)) {
				billType = bt.getBillType();
				formVisitType = bt.getVisitType()!=null?bt.getVisitType():"";
				formLocation = bt.getLocation()!=null?bt.getLocation():"";
	        }
%>
			<tr bgcolor=<%=ctlCount%2==0 ? "#FFFFFF" : "#EEEEFF"%>>
				<td colspan="2"><b><font size="-1" color="#7A388D">
						<a href="#"
							onclick="toggleDiv('<%=ctlcode%>', '<%=ctlcodename %>','<%=billType%>', '<%=formVisitType%>', '<%=formLocation%>');showHideLayers('Layer1','','hide');"><%=ctlcodename%></a>
					</font></b></td>
			</tr>
			<%}%>
		</table>
	</div>

	<div id="Layer2"
		style="position: absolute; left: 1px; top: 26px; width: 435px; height: 680px; z-index: 2; background-color: #FFCC00; layer-background-color: #FFCC00; border: 1px none #000000; visibility: hidden">
		<table width="98%" border="0" cellspacing="0" cellpadding="0"
			align=center>
			<tr>
				<td width="10%"><b><font size="-2">DxCode</font></b></td>
				<td width="85%"><b><font size="-2">Description</font></b></td>
				<td><a href="#" onclick="showHideLayers('Layer2','','hide');return false">
					<font face="verdana">X</font></a>
				</td>
			</tr>
		</table>
		<%
    for( int j=0; j< listServiceType.size(); j++) {
        String st = listServiceType.get(j);

        String ctldiagcode = "", ctldiagcodename = "";
        ctlCount = 0;
%>

		<div id="dxCodeSearchDiv_<%=st%>" style="display: none;">

			<%
			DiagnosticCodeDao dcDao = SpringUtils.getBean(DiagnosticCodeDao.class);
		
		for(Object[] o : dcDao.findDiagnosictsAndCtlDiagCodesByServiceType(st)) {
			DiagnosticCode d = (DiagnosticCode) o[0];
			CtlDiagCode c = (CtlDiagCode) o[1];
			
				ctldiagcode = d.getDiagnosticCode();
				ctldiagcodename = d.getDescription();
				ctlCount++;
%>
			<table width="98%" border="0" cellspacing="0" cellpadding="1" align=center>
				<tr bgcolor=<%=ctlCount%2==0 ? "#FFFFFF" : "#EEEEFF"%>>
					<td width="10%">
							<a href="#" onclick="document.forms[0].dxCode.value='<%=ctldiagcode%>';showHideLayers('Layer2','','hide');changeCodeDesc();return false;">
								<font face="verdana" size="-2"><%=ctldiagcode%></font>
							</a>
					</td>
					<td colspan="2">
						<a href="#" onclick="document.forms[0].dxCode.value='<%=ctldiagcode%>';showHideLayers('Layer2','','hide');changeCodeDesc();return false;">
							<font face="verdana" size="-2"><%=ctldiagcodename.length() < 56 ? ctldiagcodename : ctldiagcodename.substring(0, 55)%></font>
						</a>
					</td>
				</tr>
			</table>
			<%} %>
		</div>
		<%} %>

	</div>

	<form method="post" id="titlesearch" name="titlesearch"
		action="billingONReview.jsp" onsubmit="return onNext();">
		<%
String checkFlag = request.getParameter("checkFlag");
if(checkFlag == null) checkFlag = "0";
%>
		<input type="hidden" name="checkFlag" id="checkFlag"
			value="<%=checkFlag %>" />

		<table border="0" cellspacing="0" cellpadding="0" class="myDarkGreen"
			width="100%">
			<tr>
				<td><b><font color="#FFFFFF">Ontario Billing</font></b></td>
				<td align="right"><oscar:help keywords="1.4 Billing"
						key="app.top1" style="color: #FFFFFF" /> <font color="#FFFFFF">
					| </font> <a href=#
					onclick="popupPage(460,680,'billingONfavourite.jsp?apptProvider_no=<%=apptProvider_no%>&visitType='+document.forms[0].xml_visittype.value.split('|')[0]+'&location='+document.forms[0].xml_location.value.split('|')[0]); return false;">
						<font color="#FFFFFF">Edit</font>
				</a> <select name="cutlist" id="cutlist" onchange="changeCut(this)">
						<option selected="selected" value="">- SUPER CODES -</option>
						<% //
		    List sL = tdbObj.getBillingFavouriteList();
		    for (int i = 0; i < sL.size(); i = i + 2) {
				String favouriteName = String.valueOf(sL.get(i));
				List<String> visitTypeLocation = tdbObj.getBillingFavouriteTypeAndLocation(favouriteName);		%>
						<option value="<%=(String) sL.get(i+1)%> [<%=visitTypeLocation.get(0)!=null?visitTypeLocation.get(0):""%>|<%=visitTypeLocation.get(1)!=null?visitTypeLocation.get(1):""%>]"><%=favouriteName%></option>
						<% } %>
				</select></td>
				<td align="right" width="10%" nowrap><input type="submit"
					name="submit" value="Next" style="width: 120px;" /> <input
					type="button" name="button" value="Exit" style="width: 120px;"
					onclick="self.close();" /> &nbsp;</td>
			</tr>
		</table>

		<table border="0" cellpadding="0" cellspacing="2" width="100%">
			<tr>
				<td>
					<table border="0" cellspacing="0" cellpadding="0" width="100%"
						class="myYellow">
						<tr>
							<td nowrap bgcolor="#FFCC99" width="10%" align="center">
								<b>&nbsp;<oscar:nameage demographicNo="<%=demo_no%>" /> <%=roster_status%></b> 
								<%if (appt_no.compareTo("0") == 0) {%>
									<img src="../../../images/cal.gif" id="service_date_cal" />
									<input type="text" id="service_date" name="service_date" readonly size="10"
										   value="<%=request.getParameter("service_date")!=null? request.getParameter("service_date"):strToday%>"/>
									<script type="application/javascript">createStandardDatepicker(jQuery_3_1_0('#service_date'), "service_date_cal");</script>
								<%} else {%> 
									<input type="text" name="service_date" readonly size="10" maxlength="10" style="width: 80px;"
										   value="<%=standardDateFormat.format(standardDateFormat.parse(request.getParameter("appointment_date")))%>"/> 
								<%}%>
								<br/>
								<a href="#" onclick="popup(500, 500, '/CardSwipe/?hc=<%=demoFullHIN.replace("null", "")%>&providerNo=<%=StringUtils.trimToEmpty(loggedInInfo.getLoggedInProviderNo())%>', 'Card Swipe'); return false;" style="float:left; font-weight: bold; padding-right: 5px;"><%=demoFullHIN%></a>
							</td>
							<%
                                                              String warningStyle = "";
                                                              if (billingRecomendations.length() > 0) {
                                                                  warningStyle="border:solid 3px red;padding-left:10px;line-height:150%;font-family:Arial;";
                                                              }
                                                        %>
							<td
								style="<%=warningStyle%> color: red; background-color: #FFFFFF; font-size: 18px; font-weight: bold;"><%=billingRecomendations.toString()%></td>
							<td align="center"><font color="black"><%=msg%></font></td>
						</tr>
					</table>

					<table border="1" cellspacing="0" cellpadding="0" width="100%"
						bordercolorlight="#99A005" bordercolordark="#FFFFFF"
						bgcolor="#FFFFFF">
						<tr>
							<td width="46%">
								<table border="1" cellspacing="2" cellpadding="0" width="100%"
									bordercolorlight="#99A005" bordercolordark="#FFFFFF"
									class="myIvory">
									<tr>
										<td colspan="2" class="myPink">Specialist billing
											&nbsp;&nbsp;&nbsp;&nbsp; <a href=#
											title="Double click shaded fields for drop down or calculation"
											onClick="showHideBox('Instrdiv',1);return false;"> <font
												color='red'>Instruction</font>
										</a>
										</td>
										<td valign="top" rowspan="2">
											<table border="0" cellspacing="0" cellpadding="0"
												width="100%">
												<tr>
													<td width="15%">&nbsp;</td>
													<td nowrap><font size="-2">
														<div id="code_desc"></div>
													</font></td>
												</tr>
												<tr>
													<td><a href="#"
														onclick="showHideLayers('Layer2','','show','Layer1','','hide'); return false;">Dx</a>
													</td>
													<td><input type="text" name="dxCode" size="5"
														maxlength="5" ondblClick="dxScriptAttach('dxCode')"
														onchange="changeCodeDesc();"
														value="<%=request.getParameter("dxCode")!=null?request.getParameter("dxCode"):dxCode%>" />
														<a href=# onclick="dxScriptAttach('dxCode');">Search</a>
													</td>
												</tr>
												
												<tr <%= hideDxBoxes ? "hidden=\"hidden\"" : "" %>>
													<td>dx1</td>
													<td><input type="text" name="dxCode1" size="5"
														maxlength="5" ondblClick="dxScriptAttach('dxCode1')"
														value="<%=request.getParameter("dxCode1")!=null?request.getParameter("dxCode1"):""%>" />
														<a href=# onclick="dxScriptAttach('dxCode1')">Search</a></td>
												</tr>
												<tr <%= hideDxBoxes ? "hidden=\"hidden\"" : "" %>>
													<td>dx2</td>
													<td><input type="text" name="dxCode2" size="5"
														maxlength="5" ondblClick="dxScriptAttach('dxCode2')"
														value="<%=request.getParameter("dxCode2")!=null?request.getParameter("dxCode2"):""%>" />
														<a href=# onclick="dxScriptAttach('dxCode2')">Search</a></td>
												</tr>
												
 											</table>
 											<p>&nbsp;</p>
 											<a href="javascript:referralScriptAttach2('referralCode','referralDocName')">
 												Refer. Doctor #
											</a>
			<%
			String checkRefBox = "";
			String refName = "";
			String refNo = "";
			if (request.getParameter("rfcheck") != null) {
				checkRefBox = request.getParameter("rfcheck");
				refName = request.getParameter("referralDocName");
				refNo = request.getParameter("referralCode");
				referSpet = request.getParameter("referralSpet");
			} else if (oscarVariables.getProperty("billingRefBoxDefault", "").equals("checked")) {
				checkRefBox = "checked";
				refName = r_doctor;
				refNo = r_doctor_ohip;
			}


			%> <input type="checkbox" name="rfcheck" value="checked"
											<%=checkRefBox%> onclick="onClickRefDoc()" /><br /> <input
											type="text" name="referralCode" size="5" maxlength="6"
											value="<%=refNo%>">&nbsp; <input type="text"
											name="referralSpet" size="2" maxlength="2"
											value="<%=referSpet==null?"":referSpet%>"><br /> <input
											type="text" name="referralDocName" size="22" maxlength="30"
											value="<%=refName%>">
										</td>
									</tr>
									<tr>
										<td nowrap width="33%" align="center" class="myPink"><b>Code
												&nbsp; Time &nbsp;%</b><br /> 
											<% 
												for (int i = 0; i < BillingDataHlp.FIELD_SERVICE_NUM / 2; i++) {
												    String code = StringUtils.trimToEmpty(request.getParameter("serviceCode"+i));
												    if (code.isEmpty() && (billingServiceSchedule.size() - 1) >= i) {
												       code = billingServiceSchedule.get(i).getServiceCode();
													}
											%>
											<span class="myErrorText" id="serviceUnit<%=i%>_error" style="display: none;">Service Unit must be whole numbers only.<br/></span>
											<input type="text" name="serviceCode<%=i%>" size="4"
											maxlength="15"
											value="<%=code%>"
											onDblClick="scScriptAttach(this)" onBlur="upCaseCtrl(this)" />x
											<input type="text" id="serviceUnit<%=i%>" name="serviceUnit<%=i%>" size="2"
											maxlength="4" style="width: 20px;"
											value="<%=request.getParameter("serviceUnit"+i)!=null?request.getParameter("serviceUnit"+i):""%>" onblur="checkServiceUnit(this)" />@
											<input type="text" name="serviceAt<%=i%>" size="3"
											maxlength="4" style="width: 30px"
											value="<%=request.getParameter("serviceAt"+i)!=null?request.getParameter("serviceAt"+i):""%>" /><br />
											<%	    } %></td>
										<td nowrap width="33%" align="center" class="myPink"><b>Code
												&nbsp; Time &nbsp;%</b><br /> 
											<%	    
												for (int i = BillingDataHlp.FIELD_SERVICE_NUM / 2; i < BillingDataHlp.FIELD_SERVICE_NUM; i++) {
													String code = StringUtils.trimToEmpty(request.getParameter("serviceCode"+i));
													if (code.isEmpty() && (billingServiceSchedule.size() - 1) >= i) {
														code = billingServiceSchedule.get(i).getServiceCode();
													}
											%>
											<input type="text" name="serviceCode<%=i%>" size="4"
											maxlength="15"
											value="<%=code%>"
											onDblClick="scScriptAttach(this)" onBlur="upCaseCtrl(this)" />x
											<input type="text" id="serviceUnit<%=i%>" name="serviceUnit<%=i%>" size="2"
											maxlength="2" style="width: 20px;"
											value="<%=request.getParameter("serviceUnit"+i)!=null?request.getParameter("serviceUnit"+i):""%>" />@
											<input type="text" name="serviceAt<%=i%>" size="3"
											maxlength="4" style="width: 30px"
											value="<%=request.getParameter("serviceAt"+i)!=null?request.getParameter("serviceAt"+i):""%>" /><br />
											<%	    } %></td>
									</tr>
								</table>
							</td>
							<td valign="top">
								<table border="1" cellspacing="2" cellpadding="0" width="100%"
									bordercolorlight="#99A005" bordercolordark="#FFFFFF"
									class="myGreen">
									<tr>
										<td nowrap width="30%" align="center"><b>Billing
												Physician</b></td>
										<td width="20%">
											<% if (org.oscarehr.common.IsPropertiesOn.isMultisitesEnable())
{ // multisite start ==========================================
        	SiteDao siteDao = (SiteDao)SpringUtils.getBean("siteDao");
          	List<Site> sites = siteDao.getActiveSitesByProviderNos(invoiceProviderNos);

          	if (sites != null && !sites.isEmpty()) {

      %> <script>
var _providers = [];
<%	for (int i=0; i<sites.size(); i++) { 
	Set<Provider> siteProviders = sites.get(i).getProviders();
	List<Provider>  siteProvidersList = new ArrayList<Provider> (siteProviders);
     Collections.sort(siteProvidersList,(new Provider()).ComparatorName());%>
	_providers["<%= sites.get(i).getName() %>"]="<% Iterator<Provider> iter = siteProvidersList.iterator();
	while (iter.hasNext()) {
		Provider p=iter.next();
		if ("1".equals(p.getStatus()) && StringUtils.isNotBlank(p.getOhipNo())) {
	%><option value='<%= p.getProviderNo() %>|<%= p.getOhipNo() %>' ><%=p.getLastName()%>, <%=p.getFirstName()%></option><%}}%>";
<%}%>
function changeSite(sel) {
	sel.form.xml_provider.innerHTML=sel.value=="none"?"":_providers[sel.value];
	sel.style.backgroundColor=sel.options[sel.selectedIndex].style.backgroundColor;
}
      </script> <select id="site" name="site" onchange="changeSite(this)"
											style="width: 140px">
												<option value="none" style="background-color: white">---select
													clinic---</option>
												<%
													String selectedSite = request.getParameter("site");
												      	String xmlp = null;
												      	if (selectedSite==null) {
												      		OscarAppointmentDao apptDao = SpringUtils.getBean(OscarAppointmentDao.class);
												      		for(Object[] obj : apptDao.findAppointmentAndProviderByAppointmentNo(ConversionUtils.fromIntString(appt_no))) {
												      			Appointment a = (Appointment) obj[0]; 
												      			Provider p = (Provider) obj[1];
												      			
												      			selectedSite = a.getLocation();
												      			xmlp = a.getProviderNo() + "|" + p.getOhipNo();
												      		}
												      	}
												      	for (int i=0; i<sites.size(); i++) {
												%>
												<option value="<%=sites.get(i).getName()%>"
													style="background-color:<%=sites.get(i).getBgColor()%>"
													<%=sites.get(i).getName().toString().equals(selectedSite)?"selected":""%>><%=sites.get(i).getName()%></option>
												<%
													}
												%>
										</select> <select id="xml_provider" name="xml_provider"
											style="width: 140px"></select> <script>
     	changeSite(document.getElementById("site"));
      	document.getElementById("xml_provider").value='<%=request.getParameter("xml_provider")==null?xmlp:request.getParameter("xml_provider")%>';
      	</script> <%
		}
		else {
%>
		<select name="xml_provider" onchange="changeBillType(this.value);">
				<%
					List<Provider> providersNoSite = dao.getAllBillableProvidersWithNoSite();
					for (Provider provider : providersNoSite) {
						String selected = "";
						if (apptProvider_no.equals(provider.getProviderNo()) && request.getParameter("fromEncounter") != null)
						{
							selected = "selected";
						}
						else if (providerview.equalsIgnoreCase(provider.getProviderNo()) && request.getParameter("fromEncounter") == null)
						{
							selected = "selected";
						}

				%>
			<option value="<%=provider.getOhipNo()%>" <%=selected%>>
				<b><%=provider.getLastName()%>, <%=provider.getFirstName()%></b>
			</option>
				<%
					}
				%>
		</select>
<%		}
 	// multisite end ==========================================
 } else {
 %> <select name="xml_provider" onchange="changeBillType(this.value);">
												<%
													String[] tmp;
												            if (vecProvider.size() == 1) {
														propT = (Properties) vecProvider.get(0);
												                tmp = propT.getProperty("proOHIP","").split("\\|");
												%>
												<option value="<%=propT.getProperty("proOHIP")%>"
													<%=providerview.equals(tmp[0].trim())?"selected":""%>>
													<b><%=propT.getProperty("last_name")%>, <%=propT.getProperty("first_name")%></b>
												</option>
												<%
													} else {
												%>
												<option value="000000"
													<%=providerview.equals("000000")?"selected":""%>>
													<b>Select Provider</b>
												</option>
												<%
													for (int i = 0; i < vecProvider.size(); i++) {
														    propT = (Properties) vecProvider.get(i);
															String info = propT.getProperty("proOHIP");
															String prov = info.substring(0, info.indexOf("|"));
															String selected = "";
															if (apptProvider_no.equals(prov) && request.getParameter("fromEncounter") != null)
															{
																selected = "selected";
															}
															else if (providerview.equalsIgnoreCase(prov) && request.getParameter("fromEncounter") == null)
															{
																selected = "selected";
															}
												
												%>
												<option value="<%=propT.getProperty("proOHIP")%>"
													<%=selected%>>
													<b><%=propT.getProperty("last_name")%>, <%=propT.getProperty("first_name")%></b>
												</option>
												<%
													}
													    }
												%>
										</select> <%
 	}
 %>

										</td>
										<td nowrap width="30%" align="center"><b>Assig. Phys.</b></td>
										<td width="20%"><%=providerBean.getProperty(assgProvider_no, "").length() > 15
					    ? providerBean.getProperty(assgProvider_no, "").substring(0, 15)
					    : providerBean.getProperty(assgProvider_no, "")%></td>
									</tr>
									<tr>
										<td width="30%"><b> <%
 	if (OscarProperties.getInstance().getBooleanProperty("rma_enabled", "true")) {
 %>
												Clinic Nbr <%
 	} else {
 %> <bean:message
													key="billing.billingCorrection.formVisitType" /> <%
 	}
 %>
										</b></td>
										<td width="20%"><select name="xml_visittype">
												<%
													if (OscarProperties.getInstance().getBooleanProperty("rma_enabled", "true")) {
												%>
												<%
													ClinicNbrDao cnDao = (ClinicNbrDao) SpringUtils.getBean("clinicNbrDao");
																ArrayList<ClinicNbr> nbrs = cnDao.findAll();
													            ProviderDao providerDao = (ProviderDao) SpringUtils.getBean("providerDao");
													            String providerSearch = apptProvider_no.equalsIgnoreCase("none") ? user_no : apptProvider_no;
													            Provider p = providerDao.getProvider(providerSearch);
													            String providerNbr = SxmlMisc.getXmlContent(p.getComments(),"xml_p_nbr");
													                    for (ClinicNbr clinic : nbrs) {
																	String valueString = String.format("%s | %s", clinic.getNbrValue(), clinic.getNbrString());
												%>
												<option value="<%=valueString%>"
													<%=providerNbr.startsWith(clinic.getNbrValue())?"selected":""%>><%=valueString%></option>
												<%
													}
												%>
												<%
													} else {
												%>
												<option value="00| Clinic Visit"
													<%=visitType.startsWith("00")?"selected":""%>>00 |
													Clinic Visit</option>
												<option value="01| Outpatient Visit"
													<%=visitType.startsWith("01")?"selected":""%>>01 |
													Outpatient Visit</option>
												<option value="02| Hospital Visit"
													<%=visitType.startsWith("02")?"selected":""%>>02 |
													Hospital Visit</option>
												<option value="03| ER"
													<%=visitType.startsWith("03")?"selected":""%>>03 |
													ER</option>
												<option value="04| Nursing Home"
													<%=visitType.startsWith("04")?"selected":""%>>04 |
													Nursing Home</option>
												<option value="05| Home Visit"
													<%=visitType.startsWith("05")?"selected":""%>>05 |
													Home Visit</option>
												<%
													}
												%>
										</select></td>
										<td width="30%"><b>Billing Type</b></td>
										<td width="20%">
											<%
												String srtBillType = request.getParameter("xml_billtype")!=null ? request.getParameter("xml_billtype") : defaultBillType;
											%> <select name="xml_billtype" onchange="onChangePrivate();">
												<option value="ODP | Bill OHIP"
													<%=srtBillType.startsWith("ODP")?"selected" : ""%>>Bill
													OHIP</option>
												<option value="WCB | Worker's Compensation Board"
													<%=srtBillType.startsWith("WCB")?"selected" : ""%>>WSIB</option>
												<option value="NOT | Do Not Bill"
													<%=srtBillType.startsWith("NOT")?"selected" : ""%>>Do
													Not Bill</option>
												<option value="IFH | Interm Federal Health"
													<%=srtBillType.startsWith("IFH")?"selected" : ""%>>IFH</option>
												<option value="K3P | Kai 3rd Party"
													<%=srtBillType.startsWith("K3P")?"selected" : ""%>>Kai 3rd
													Party</option>
												<option value="PAT | Bill Patient"
													<%=srtBillType.startsWith("PAT")?"selected" : ""%>>3rd
													Party</option>
												<option value="OCF | "
													<%=srtBillType.startsWith("OCF")?"selected" : ""%>>
													-OCF</option>
												<option value="ODS | "
													<%=srtBillType.startsWith("ODS")?"selected" : ""%>>
													-ODSP</option>
												<option value="CPP | Canada Pension Plan"
													<%=srtBillType.startsWith("CPP")?"selected" : ""%>>
													-CPP</option>
												<option
													value="STD | Short Term Disability / Long Term Disability"
													<%=srtBillType.startsWith("STD")?"selected" : ""%>>-STD/LTD</option>
												<option value="BON | Bonus Codes"
													<%=srtBillType.startsWith("BON")?"selected" : ""%>>Bonus
													Codes</option>
										</select>
										</td>
									</tr>
									<tr>
										<td><b>Site Number</b></td>
										<td colspan="3"><select name="xml_location">
												<%
													//
													    String billLocationNo="", billLocation="";
													    List lLocation = tdbObj.getFacilty_num();
													    for (int i = 0; i < lLocation.size(); i = i + 2) {
														billLocationNo = (String) lLocation.get(i);
														billLocation = (String) lLocation.get(i + 1);
														String strLocation = request.getParameter("xml_location") != null ? request.getParameter("xml_location") : clinicview;
														//Checks if the visit location is null, if it is, then give it the value of strLocation so that the xml_location or clinicview is selected instead of "Not Applicable"
														visitLocation = !visitLocation.equals("") ? visitLocation : strLocation;
												%>
												<option value="<%=billLocationNo + "|" + billLocation%>"
													<%=billLocationNo.equals(visitLocation) ? "selected" : ""%>>
													<%=billLocationNo + " | " + billLocation%>
												</option>
												<%
													}
												%>
										</select> Manual: <input type="checkbox" name="m_review" value="Y"
											<%=m_review.equals("Y")?"checked":""%>></td>
									</tr>
									<tr>
										<td><b><bean:message
													key="oscar.billing.CA.ON.billingON.OB.SLIcode" /></b></td>
										<td colspan="3"><select name="xml_slicode">
												<option value="0000">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.NA" />
												</option>
											<% if (!"0000".equals(clinicNo)){ %>
												<option value="<%=clinicNo%>">
													<%=clinicNo + " | "%><bean:message
															key="oscar.billing.CA.ON.billingON.OB.SLIcode.Clinic" />
												</option>
											<% } %>
												<option value="HDS">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.HDS" />
												</option>
												<option value="HED">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.HED" />
												</option>
												<option value="HIP">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.HIP" />
												</option>
												<option value="HOP">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.HOP" />
												</option>
												<option value="HRP">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.HRP" />
												</option>
												<option value="IHF">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.IHF" />
												</option>
												<option value="OFF">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.OFF" />
												</option>
												<option value="OTN">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.OTN" />
												</option>
												<option value="PDF">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.PDF" />
												</option>
												<option value="RTF">
													<bean:message
														key="oscar.billing.CA.ON.billingON.OB.SLIcode.RTF" />
												</option>
										</select></td>
									</tr>
									<tr>
										<td><b>Admission Date</b></td>
										<td>
											<%
												String admDate = "";
											          String inPatient = oscarVariables.getProperty("inPatient");
											          try{
											             if(inPatient != null && inPatient.trim().equalsIgnoreCase("YES")){
													oscar.oscarDemographic.data.DemographicData demoData = new oscar.oscarDemographic.data.DemographicData();
													admDate = demoData.getDemographicDateJoined(loggedInInfo, demo_no);
												     }
											          }catch(Exception inPatientEx){
											        	  MiscUtils.getLogger().error("Error", inPatientEx);
												     admDate = "";
											          }

												  if (visitType.startsWith("02")) admDate = visitdate;
											%>
											<img src="../../../images/cal.gif" id="xml_vdate_cal">
											<input type="text" name="xml_vdate" id="xml_vdate" size='10' maxlength='10'
												value="<%=request.getParameter("xml_vdate") != null ? request.getParameter("xml_vdate") : (!admissionDate.equals("") ? admissionDate : admDate)%>"> 
											<script type="application/javascript">createStandardDatepicker(jQuery_3_1_0('#xml_vdate'), "xml_vdate_cal");</script>
										</td>
										<td colspan="2"><a href="#"
											onclick="showHideLayers('Layer1','','show');return false;">
												Billing form</a>: <input type="text" name="billFormName"
											id="billFormName" size="30" readonly
											value="<%=currentFormName.length() < 40 ? currentFormName : currentFormName.substring(0, 40)%>" />
											<input type="hidden" name="billForm" id="billForm" size="30"
											maxlength="30" readonly value="<%=ctlBillForm%>" /></td>
									</tr>
									<%
										if (!org.oscarehr.common.IsPropertiesOn.isMultisitesEnable()) {
									    OscarProperties props = OscarProperties.getInstance();
									    boolean bMoreAddr = props.getProperty("scheduleSiteID", "").equals("") ? false : true;
									    if(bMoreAddr) {
										BillingSiteIdPrep sitePrep = new BillingSiteIdPrep();
										String [] siteList = sitePrep.getSiteList();
										String strServDate = request.getParameter("appointment_date")!=null? request.getParameter("appointment_date"):strToday;
										String thisSite = (new JdbcApptImpl()).getLocationFromSchedule(strServDate, apptProvider_no);
										String suggestSite = sitePrep.getSuggestSite(siteList, thisSite, strServDate, apptProvider_no );
									%>
									<tr>
										<td align="right">Site</td>
										<td colspan="3"><select name="siteId">
												<%
													for(int i=0; i<siteList.length; i++) {
												%>
												<option value="<%=siteList[i]%>"
													<%=suggestSite.equals(siteList[i])?"selected":""%>>
													<b><%=siteList[i]%></b>
												</option>
												<%
													}
												%>
										</select></td>
									</tr>
									<%
										}
									}
									%>

								</table>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td>
					<table width="100%" border="0" cellspacing="0" cellpadding="0"
						height="137">
						<tr>
							<td valign="top" width="33%">
								<%
									for( int j=0; j< listServiceType.size(); j++) {
								        ArrayList<Properties> vecCodeCol1 = new ArrayList<Properties>();
								        vecCodeCol1 = billingServiceCodesMap.get("group1_".concat(listServiceType.get(j)));
								        headerTitle1 = titleMap.get("group1_".concat(listServiceType.get(j)));
								%>
								<div id="group1_<%=listServiceType.get(j)%>"
									style="display: none;">
									<table width="100%" border="1" cellspacing="0" cellpadding="1"
										height="0" bordercolorlight="#99A005"
										bordercolordark="#FFFFFF">
										<tr class="myYellow">
											<th width="10%" nowrap><div class="smallFont"><%=headerTitle1%></div></th>
											<th width="70%"><div class="smallFont">Description</div></th>
											<th><div class="smallFont">Fee</div></th>
										</tr>


										<%
											for (int i = 0; i < vecCodeCol1.size(); i++) {
												propT = vecCodeCol1.get(i);
												serviceCode = propT.getProperty("serviceCode");
												serviceDesc = propT.getProperty("serviceDesc");
												serviceDisp = propT.getProperty("serviceDisp");
												servicePercentage = propT.getProperty("servicePercentage");
												serviceType = propT.getProperty("serviceType");
												displayStyle = propT.getProperty("displaystyle");
												sliFlag = Boolean.parseBoolean(propT.getProperty("serviceSLI"));

												if (propPremium.getProperty(serviceCode) != null) premiumFlag = "A";
												else premiumFlag = "";

												String bgcolor = i % 2 == 0 ? "bgcolor='#FFFFFF'" : "class='myGreen'";
												if (request.getParameter("xml_" + serviceCode) != null) bgcolor = "bgcolor='#66FF66'";
										%>
										<tr <%=bgcolor%>>
											<td align="left" style="<%=displayStyle%>" nowrap><input
												type="checkbox" id="xml_<%=serviceCode%>"
												name="xml_<%=serviceCode%>" value="checked"
												onclick="refreshServicesChecked(this);"
												<%=request.getParameter("xml_"+serviceCode)!=null?request.getParameter("xml_"+serviceCode):""%>
												<%=bSingleClick? "onClick='onClickServiceCode(this)'" :""%> />
												<span id="sc<%=(""+i).substring(0,1)+serviceCode%>"
												ondblclick="onDblClickServiceCode(this)"><%=serviceCode%></span>

											</td>
											<td
												title="<%=serviceDesc%>"
												<%=displayStyle.equals("")? "class=\"smallFont\"": "style=\"" + displayStyle + "\""%>>
												<div><%=serviceDesc%>
													<!--<input type="hidden" name="desc_xml_<%=serviceCode%>" value="<%=serviceDesc%>" />-->
												</div>
											</td>
											<td align="right">
												<div class="smallFont"><%=serviceDisp%></div> <input
												type="hidden" name="sli_xml_<%=serviceCode%>"
												value="<%=sliFlag%>" /> <!--<input type="hidden" name="price_xml_<%=serviceCode%>" value="<%=serviceDisp%>" />
				    <input type="hidden" name="perc_xml_<%=serviceCode%>" value="<%=servicePercentage%>" />-->
											</td>
										</tr>
										<%
											}
										%>
									</table>
								</div> <%
 	}
 %>

							</td>
							<td width="33%" valign="top">
								<%
									for( int j=0; j< listServiceType.size(); j++) {

								                ArrayList<Properties> vecCodeCol2 = new ArrayList<Properties>();
								                vecCodeCol2 = billingServiceCodesMap.get("group2_".concat(listServiceType.get(j)));
								                headerTitle2 = titleMap.get("group2_".concat(listServiceType.get(j)));
								%>
								<div id="group2_<%=listServiceType.get(j)%>"
									style="display: none;">


									<table width="100%" border="1" cellspacing="0" cellpadding="1"
										height="0" bordercolorlight="#99A005"
										bordercolordark="#FFFFFF">
										<tr class="myYellow">
											<th width="10%" nowrap>
												<div class="smallFont"><%=headerTitle2%></div>
											</th>
											<th width="70%"><div class="smallFont">Description</div></th>
											<th><div class="smallFont">Fee</div></th>
										</tr>

										<%
											for (int i = 0; i < vecCodeCol2.size(); i++) {
												propT = vecCodeCol2.get(i);
												serviceCode = propT.getProperty("serviceCode");
												serviceDesc = propT.getProperty("serviceDesc");
												serviceDisp = propT.getProperty("serviceDisp");
												servicePercentage = propT.getProperty("servicePercentage");
												serviceType = propT.getProperty("serviceType");
												displayStyle = propT.getProperty("displaystyle");
												sliFlag = Boolean.parseBoolean(propT.getProperty("serviceSLI"));

												if (propPremium.getProperty(serviceCode) != null) premiumFlag = "A";
												else premiumFlag = "";

												String bgcolor = i % 2 == 0 ? "bgcolor='#FFFFFF'" : "class='myGreen'";
												if (request.getParameter("xml_" + serviceCode) != null) bgcolor = "bgcolor='#66FF66'";
										%>
										<tr <%=bgcolor%>>
											<td align="left" style="<%=displayStyle%>" nowrap><input
												type="checkbox" id="xml_<%=serviceCode%>"
												name="xml_<%=serviceCode%>" value="checked"
												onclick="refreshServicesChecked(this);"
												<%=request.getParameter("xml_"+serviceCode)!=null?request.getParameter("xml_"+serviceCode):""%>
												<%=bSingleClick? "onClick='onClickServiceCode(this)'" :""%> />
												<span id="sc<%=(""+i).substring(0,1)+serviceCode%>"
												onDblClick="onDblClickServiceCode(this)"><%=serviceCode%></span>

											</td>
											<td title="<%=serviceDesc%>"
												<%=displayStyle.equals("")? "class=\"smallFont\"": "style=\"" + displayStyle + "\""%>>
												<div>
													<%=serviceDesc%>
												</div> <!--<input type="hidden" name="desc_xml_<%=serviceCode%>" value="<%=serviceDesc%>" />-->
											</td>
											<td align="right">
												<div class="smallFont"><%=serviceDisp%></div> <input
												type="hidden" name="sli_xml_<%=serviceCode%>"
												value="<%=sliFlag%>" /> <!--<input type="hidden" name="price_xml_<%=serviceCode%>" value="<%=serviceDisp%>" />
					<input type="hidden" name="perc_xml_<%=serviceCode%>" value="<%=servicePercentage%>" />-->
											</td>
										</tr>
										<%
											}
										%>
									</table>
								</div> <%
 	}
 %>

							</td>
							<td width="33%" valign="top">
								<%
									for( int j=0; j< listServiceType.size(); j++) {

								                ArrayList<Properties> vecCodeCol3 = new ArrayList<Properties>();
								                vecCodeCol3 = billingServiceCodesMap.get("group3_".concat(listServiceType.get(j)));
								                headerTitle3 = titleMap.get("group3_".concat(listServiceType.get(j)));
								%>
								<div id="group3_<%=listServiceType.get(j)%>"
									style="display: none;">



									<table width="100%" border="1" cellspacing="0" cellpadding="1"
										height="0" bordercolorlight="#99A005"
										bordercolordark="#FFFFFF">
										<tr class="myYellow">
											<th width="10%" nowrap><div class="smallFont"><%=headerTitle3%></div></th>
											<th width="70%"><div class="smallFont">Description</div></th>
											<th><div class="smallFont">Fee</div></th>
										</tr>

										<%
											for (int i = 0; i < vecCodeCol3.size(); i++) {
												propT = vecCodeCol3.get(i);
												serviceCode = propT.getProperty("serviceCode");
												serviceDesc = propT.getProperty("serviceDesc");
												serviceDisp = propT.getProperty("serviceDisp");
												servicePercentage = propT.getProperty("servicePercentage");
												serviceType = propT.getProperty("serviceType");
												displayStyle = propT.getProperty("displaystyle");
												sliFlag = Boolean.parseBoolean(propT.getProperty("serviceSLI"));

												if (propPremium.getProperty(serviceCode) != null) premiumFlag = "A";
												else premiumFlag = "";

												String bgcolor = i % 2 == 0 ? "bgcolor='#FFFFFF'" : "class='myGreen'";
												if (request.getParameter("xml_" + serviceCode) != null) bgcolor = "bgcolor='#66FF66'";
										%>
										<tr <%=bgcolor%>>
											<td align="left" style="<%=displayStyle%>" nowrap><input
												type="checkbox" id="xml_<%=serviceCode%>"
												name="xml_<%=serviceCode%>" value="checked"
												onclick="refreshServicesChecked(this);"
												<%=request.getParameter("xml_"+serviceCode)!=null?request.getParameter("xml_"+serviceCode):""%>
												<%=bSingleClick? "onClick='onClickServiceCode(this)'" :""%> />
												<span id="sc<%=(""+i).substring(0,1)+serviceCode%>"
												onDblClick="onDblClickServiceCode(this)"><%=serviceCode%></span>
											</td>
											<td title="<%=serviceDesc%>"
												<%=displayStyle.equals("")? "class=\"smallFont\"": "style=\"" + displayStyle + "\""%>>
												<div>
													<%=serviceDesc%>
													<!--<input type="hidden" name="desc_xml_<%=serviceCode%>" value="<%=serviceDesc%>" />-->
												</div>
											</td>
											<td align="right">
												<div class="smallFont"><%=serviceDisp%></div> <input
												type="hidden" name="sli_xml_<%=serviceCode%>"
												value="<%=sliFlag%>" /> <!--<input type="hidden" name="price_xml_<%=serviceCode%>" value="<%=serviceDisp%>" />
					<input type="hidden" name="perc_xml_<%=serviceCode%>" value="<%=servicePercentage%>" />-->
											</td>
										</tr>
										<%
											}
										%>
									</table>
								</div> <%
 	}
 %>

							</td>
						</tr>
					</table>
				</td>
			</tr>

			<input type="hidden" name="clinic_no" value="<%=clinicNo%>" />
			<input type="hidden" name="demographic_no" value="<%=demo_no%>" />
			<input type="hidden" name="appointment_no" value="<%=appt_no%>" />

			<input type="hidden" name="ohip_version" value="V03G" />
			<input type="hidden" name="hin" value="<%=demoHIN%>" />
			<input type="hidden" name="ver" value="<%=demoVer%>" />
			<input type="hidden" name="hc_type" value="<%=demoHCTYPE%>" />
			<input type="hidden" name="sex" value="<%=demoSex%>" />

			<input type="hidden" name="start_time"
				value="<%=request.getParameter("start_time")%>" />

			<input type="hidden" name="demographic_dob" value="<%=demoDOB%>" />

			<input type="hidden" name="apptProvider_no"
				value="<%=request.getParameter("apptProvider_no")%>" />
			<input type="hidden" name="asstProvider_no"
				value="<%=request.getParameter("asstProvider_no")%>" />

			<input type="hidden" name="demographic_name" value="<%=demoname%>" />
			<input type="hidden" name="providerview" value="<%=providerview%>" />
			<input type="hidden" name="appointment_date"
				value="<%=request.getParameter("appointment_date")%>" />
			<input type="hidden" name="assgProvider_no"
				value="<%=assgProvider_no%>" />
			<input type="hidden" name="billForm" value="<%=ctlBillForm%>" />
			<input type="hidden" name="curBillForm" value="<%=ctlBillForm%>" />
			<input type="hidden" name="services_checked">
			<input type="hidden" name="url_back">
			<input type="hidden" name="billNo_old" id="billNo_old"
				value="<%=request.getParameter("billNo_old")%>" />
			<input type="hidden" name="billStatus_old" id="billStatus_old"
				value="<%=request.getParameter("billStatus_old")%>" />

		</table>

		<table border="0" cellpadding="0" cellspacing="2" width="100%"
			class="myIvory">
			<tr class="myYellow">
				<td><%=demoname%> - <b>Billing History</b> <% if(maxResults > -1){ %>(last <%=maxResults%> records)<% } %></td>
				<td width="20%" align="right">Last <input type="text"
					name="day" value="365" size="3" /> days <input type="button"
					name="buttonDay" value="Go" onClick="onHistory('day'); return false;" />
				</td>
			</tr>
			<tr class="myYellow">
				<td></td>
				<td align="right">
					View Fully History&nbsp; <input type="button" name="buttonAll" value="Go" onClick="onHistory('all'); return false;"/>
				</td>
			</tr>
		</table>
	</form>

	<table border="0" cellpadding="0" cellspacing="2" width="100%"
		class="myIvory">
		<tr>
			<td>
				<table border="1" cellspacing="0" cellpadding="0"
					bordercolorlight="#99A005" bordercolordark="#FFFFFF" width="100%">
					<tr class="myYellow" align="center">
						<th nowrap>Serial No.</th>
						<th nowrap>Billing Date</th>
						<th nowrap>Appt/Adm Date</th>
						<th nowrap>Service Code</th>
						<th nowrap>Dx</th>
						<th>Create Date</th>
					</tr>
					<%
						// new billing records
						if(aL != null && aL.size() > 0){
							for (int i = 0; i < aL.size(); i = i + 2) {
								BillingClaimHeader1Data obj = (BillingClaimHeader1Data) aL.get(i);
								BillingItemData iobj = (BillingItemData) aL.get(i + 1);
					%>
					<tr <%=i%4==0? "class=\"myGreen\"":""%> align="center">
						<td class="smallFont"><%=obj.getId()%></td>
						<td class="smallFont"><%=obj.getBilling_date()%></td>
						<td class="smallFont"><%=iobj.getService_date()%></td>
						<td class="smallFont"><%=iobj.getService_code()%></td>
						<td class="smallFont"><%=iobj.getDxToBill()%></td>
						<td class="smallFont"><%=obj.getUpdate_datetime().substring(0, 10)%></td>
					</tr>
					<%
							}
						}
					%>
				</table>
			</td>
		</tr>
	</table>


	<%!String getDefaultValue(String paraName, Vector vec, String propName) {
		String ret = "";
		if (paraName != null && !"".equals(paraName)) {
			ret = paraName;
		} else if (vec != null && vec.size() > 0 && vec.get(0) != null) {
			ret = ((Properties) vec.get(0)).getProperty(propName, "");
		}
		return ret;
	}

	String noNull(String str) {
		if (str != null) {
			return str;
		}
		return "";
	}%>

</body>
</html>
