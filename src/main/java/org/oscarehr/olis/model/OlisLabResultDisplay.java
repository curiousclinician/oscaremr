package org.oscarehr.olis.model;

import org.oscarehr.common.dao.DemographicDao;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.olis.OLISUtils;
import org.oscarehr.util.SpringUtils;
import oscar.oscarLab.ca.all.parsers.OLISHL7Handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OlisLabResultDisplay {
    
    private String labUuid;
    private int labObrIndex;
    private String placerGroupNo;
    private String testRequestZbr11;
    private String patientFirstName;
    private String patientLastName;
    private String patientHcn;
    private String labName = "";
    private String testRequestName;
    private String category;
    private String requestStatus;
    private String specimenType;
    private String resultsIndicator;
    private String orderingPractitioner;
    private String orderingPractitionerFull;
    private String ccPractitioners;
    private String admittingPractitioner;
    private String attendingPractitioner;
    private String reportingFacilityName = "";
    private String performingFacilityName = "";
    private String olisLastUpdated;
    private String collectionDate;
    private String collectorsComment;
    private boolean isBlocked = false;
    private OLISRequestNomenclature nomenclature;
    private int obrSetId;
    private String sex;
    private String DOB;
    private Integer demographicNo;
    
    private List<OlisMeasurementsResultDisplay> measurements = new ArrayList<OlisMeasurementsResultDisplay>();
    
    private static SimpleDateFormat collectionDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public OlisLabResultDisplay() { }

    public String getLabUuid() {
        return labUuid;
    }
    public void setLabUuid(String labUuid) {
        this.labUuid = labUuid;
    }

    public int getLabObrIndex() {
        return labObrIndex;
    }
    public void setLabObrIndex(int labObrIndex) {
        this.labObrIndex = labObrIndex;
    }

    public String getPlacerGroupNo() {
        return placerGroupNo;
    }
    public void setPlacerGroupNo(String placerGroupNo) {
        this.placerGroupNo = placerGroupNo;
    }

    public String getTestRequestZbr11() {
        return testRequestZbr11;
    }
    public void setTestRequestZbr11(String testRequestZbr11) {
        this.testRequestZbr11 = testRequestZbr11;
    }

    public String getPatientFirstName() {
        return patientFirstName;
    }
    public void setPatientFirstName(String patientFirstName) {
        this.patientFirstName = patientFirstName;
    }

    public String getPatientLastName() {
        return patientLastName;
    }
    public void setPatientLastName(String patientLastName) {
        this.patientLastName = patientLastName;
    }

    public String getPatientHcn() {
        return patientHcn;
    }
    public void setPatientHcn(String patientHcn) {
        this.patientHcn = patientHcn;
    }

    public String getLabName() {
        return labName;
    }
    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getTestRequestName() {
        return testRequestName;
    }
    public void setTestRequestName(String testRequestName) {
        this.testRequestName = testRequestName;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getRequestStatus() {
        return requestStatus;
    }
    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getSpecimenType() {
        return specimenType;
    }
    public void setSpecimenType(String specimentType) {
        this.specimenType = specimentType;
    }

    public String getResultsIndicator() {
        return resultsIndicator;
    }
    public void setResultsIndicator(String resultsIndicator) {
        this.resultsIndicator = resultsIndicator;
    }

    public String getReportingFacilityName() {
        return reportingFacilityName;
    }
    public void setReportingFacilityName(String reportingFacilityName) {
        this.reportingFacilityName = reportingFacilityName;
    }

    public String getPerformingFacilityName() {
        return performingFacilityName;
    }
    public void setPerformingFacilityName(String performingFacilityName) {
        this.performingFacilityName = performingFacilityName;
    }

    public String getOrderingPractitioner() {
        return orderingPractitioner;
    }
    public void setOrderingPractitioner(String orderingPractitioner) {
        this.orderingPractitioner = orderingPractitioner;
    }

    public String getOrderingPractitionerFull() {
        return orderingPractitionerFull;
    }
    public void setOrderingPractitionerFull(String orderingPractitionerFull) {
        this.orderingPractitionerFull = orderingPractitionerFull;
    }

    public String getCcPractitioners() {
        return ccPractitioners;
    }
    public void setCcPractitioners(String ccPractitioners) {
        this.ccPractitioners = ccPractitioners;
    }

    public String getAdmittingPractitioner() {
        return admittingPractitioner;
    }
    public void setAdmittingPractitioner(String admittingPractitioner) {
        this.admittingPractitioner = admittingPractitioner;
    }

    public String getAttendingPractitioner() {
        return attendingPractitioner;
    }
    public void setAttendingPractitioner(String attendingPractitioner) {
        this.attendingPractitioner = attendingPractitioner;
    }

    public String getOlisLastUpdated() {
        return olisLastUpdated;
    }
    public void setOlisLastUpdated(String olisLastUpdated) {
        this.olisLastUpdated = olisLastUpdated;
    }

    public Date getCollectionDateAsDate() {
        try {
            return collectionDateTimeFormatter.parse(collectionDate);
        } catch (ParseException e) {
            return null;
        }
    }
    public String getCollectionDate() {
        return collectionDate;
    }
    public void setCollectionDate(String collectionDate) {
        this.collectionDate = collectionDate;
    }

    public String getCollectorsComment() {
        return collectorsComment;
    }
    public void setCollectorsComment(String collectorsComment) {
        this.collectorsComment = collectorsComment;
    }

    public boolean isBlocked() {
        return isBlocked;
    }
    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public OLISRequestNomenclature getNomenclature() {
        return nomenclature;
    }
    public void setNomenclature(OLISRequestNomenclature nomenclature) {
        this.nomenclature = nomenclature;
    }

    public int getObrSetId() {
        return obrSetId;
    }
    public void setObrSetId(int obrSetId) {
        this.obrSetId = obrSetId;
    }

    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDOB() {
        return DOB;
    }
    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public Integer getDemographicNo() {
        return demographicNo;
    }

    public void setDemographicNo(Integer demographicNo) {
        this.demographicNo = demographicNo;
    }

    public List<OlisMeasurementsResultDisplay> getMeasurements() {
        return measurements;
    }
    public void setMeasurements(List<OlisMeasurementsResultDisplay> measurements) {
        this.measurements = measurements;
    }

    public static List<OlisLabResultDisplay> getListFromHandler(OLISHL7Handler olisHandler, String resultUuid) {
        List<OlisLabResultDisplay> results = new ArrayList<OlisLabResultDisplay>();
        boolean isReportBlocked = olisHandler.isReportBlocked();
        // Get OLIS Request Nomenclature for lab results for adding to results
        Map<String, OLISRequestNomenclature> nomenclatureMap = olisHandler.getOlisRequestNomenclatureMap();
        
        ArrayList headers = olisHandler.getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            
            int obr = olisHandler.getMappedOBR(i);
            if (olisHandler.isChildOBR(obr)) {
                continue;
            }

            OlisLabResultDisplay labResult = new OlisLabResultDisplay();

            // Lab specific values
            labResult.setLabUuid(resultUuid);
            labResult.setLabObrIndex(obr);
            labResult.setLabName(olisHandler.getReportingFacilityNameShort());
            labResult.setPatientFirstName(olisHandler.getFirstName());
            labResult.setSex(olisHandler.getSex());
            labResult.setDOB(olisHandler.getDOB());
            labResult.setPatientLastName(olisHandler.getLastName());
            labResult.setPatientHcn(olisHandler.getHealthNum());
            labResult.setTestRequestName(olisHandler.getOBRName(obr));
            labResult.setCategory(olisHandler.getOBRCategory(obr));
            labResult.setRequestStatus(olisHandler.getTestRequestStatusMessage(olisHandler.getObrStatusFinal(obr).charAt(0), true));
            labResult.setSpecimenType(olisHandler.getOBRSpecimentType(obr));
            labResult.setResultsIndicator(olisHandler.getObrStatus(obr));
            labResult.setTestRequestZbr11(olisHandler.getZBR11(obr));
            String collectorsComments = olisHandler.getCollectorsComment(obr);
            collectorsComments = OLISUtils.Hl7EncodedRepeatableCharacter.performReplacement(collectorsComments, true);
            labResult.setCollectorsComment(collectorsComments.replaceAll("(?<=\\s)\\s", "&nbsp;"));
            labResult.setNomenclature(nomenclatureMap.get(olisHandler.getNomenclatureRequestCode(obr)));
            // Checks if information in the report is blocked, either at the report level or at the OBR level
            boolean isBlocked = isReportBlocked || olisHandler.isOBRBlocked(obr);
            labResult.setBlocked(isBlocked);
            int setId = Integer.parseInt(olisHandler.getObrSetId(obr));
            labResult.setObrSetId(setId);
            
            // Report level values
            labResult.setOrderingPractitioner(olisHandler.getShortDocName());
            labResult.setOrderingPractitionerFull(olisHandler.getDocName());
            
            List<String> ccPractitioners = new ArrayList<>();
            for (HashMap<String, String> ccMap: olisHandler.getFormattedCcDocs()) {
                ccPractitioners.add(ccMap.get("name").trim());
            }
            labResult.setCcPractitioners(String.join(";", ccPractitioners));
            
            
            labResult.setAdmittingPractitioner(olisHandler.getAdmittingProviderNameShort());
            labResult.setAttendingPractitioner(olisHandler.getAttendingProviderNameShort());
            labResult.setOlisLastUpdated(olisHandler.getLastUpdateInOLIS());
            labResult.setReportingFacilityName(olisHandler.getReportingFacilityName());
            labResult.setPerformingFacilityName(olisHandler.getPerformingFacilityName());
            
            String collectionDate = olisHandler.getCollectionDateTime(obr);
            labResult.setCollectionDate(collectionDate);
            
            labResult.setPlacerGroupNo(olisHandler.getAccessionNum());
            
            DemographicDao demographicDao = SpringUtils.getBean(DemographicDao.class);
            Demographic demographic = null;
            List <Demographic> demographics = demographicDao.getByHinAndGenderAndDobAndLastName(olisHandler.getHealthNum(), olisHandler.getSex(), olisHandler.getDOB(), olisHandler.getLastName());
            if(!demographics.isEmpty()){
                demographic = demographics.get(0);
                labResult.setDemographicNo(demographic.getDemographicNo());
            }
                  
            
            int obxCount = olisHandler.getOBXCount(obr);
            if (obxCount > 0) {
                for (int obxIndex = 0; obxIndex < obxCount; obxIndex++) {
                    // Gets the related result sortable
                    OlisLabResultSortable resultSortable = olisHandler.getObxSortable(obr, obxIndex);
                    // Gets the set id and subtracts it by one to get the proper obx location in the segment list for the obr
                    int obx = resultSortable.getSetId() - 1;
                    String resultStatus = olisHandler.getOBXResultStatus(obr, obx).trim();
                    String status = "";
                    if (!resultStatus.isEmpty()) {
                        status = olisHandler.getTestResultStatusMessage(resultStatus.charAt(0));
                    }
                    // Gets the OBX value type
                    String valueType = olisHandler.getOBXValueType(obr,obx).trim();
                    String resultValue;
                    // If the value type is a coded entry, it needs to get OBX 5.2
                    // If it isn't a coded entry, then gets OBX 5.1
                    if (valueType.equals("CE")) {
                        resultValue = olisHandler.getOBXCEName(obr, obx);
                    } else if (valueType.equals("SN")) {
                        // If the result is a Structured Number, it needs to get both sections of 5.1
                        resultValue = olisHandler.getOBXSNResult(obr, obx);
                    } else if (valueType.equals("TS") || valueType.equals("DT")) {
                        // If the value is a timestamp or date, it can use the getOBXTSResult function
                        resultValue = olisHandler.getOBXTSResult(obr, obx);
                    } else if (valueType.equals("TM")) {
                        // If the value is a Time, gets it in a formatted version
                        resultValue = olisHandler.getOBXTMResult(obr, obx);
                    } else {
                        resultValue = olisHandler.getOBXResult(obr, obx);
                    }
                    
                    OlisMeasurementsResultDisplay measurement = new OlisMeasurementsResultDisplay();
                    measurement.setMeasurementObxIndex(obx);
                    measurement.setParentLab(labResult);
                    measurement.setTestResultName(olisHandler.getOBXName(obr, obx));
                    measurement.setStatus(status);
                    measurement.setResultValue(resultValue);
                    measurement.setFlag(olisHandler.getOBXAbnormalFlag(obr, obx));
                    measurement.setReferenceRange(olisHandler.getOBXReferenceRange(obr, obx));
                    measurement.setUnits(olisHandler.getOBXUnits(obr, obx));
                    String abnormal = olisHandler.getOBXAbnormalFlag(obr, obx);
                    measurement.setAbnormal(abnormal != null && (abnormal.equals("A") || abnormal.startsWith("H") || olisHandler.isOBXAbnormal(obr, obx)));
                    measurement.setNatureOfAbnormalText(olisHandler.getNatureOfAbnormalTest(obr, obx));
                    measurement.setIsAttachment("ED".equals(olisHandler.getOBXValueType(obr, obx).trim()));
                    measurement.setBlocked(isBlocked);
                    measurement.setResultSortable(resultSortable);

                    for (int commentIndex = 0; commentIndex < olisHandler.getOBXCommentCount(obr, obx); commentIndex++) {
                        measurement.getComments().add(olisHandler.getOBXComment(obr, obx, commentIndex));
                    }

                    // Checks if the results is invalid, a status of W, and if it is, sets the flag
                    if (resultStatus.equalsIgnoreCase("W")) {
                        measurement.setInvalid(true);
                    }
                    
                    labResult.getMeasurements().add(measurement);
                }
            } else {
                int commentCount = olisHandler.getOBRCommentCount(obr);
                for (int cc = 0; cc < commentCount; cc++) {
                    String comment = olisHandler.getOBRComment(obr, cc);
                    
                    OlisMeasurementsResultDisplay result = new OlisMeasurementsResultDisplay();
                    result.setMeasurementObxIndex(0);
                    result.setParentLab(labResult);
                    result.getComments().add(comment);
                    result.setBlocked(isBlocked);
                    result.setResultSortable(new OlisLabResultSortable());
                    labResult.getMeasurements().add(result);
                }
            }
            
            results.add(labResult);
        }
        
        return results;
    }
    /**
     * The order and priority that is achieved is as follows: 
     * 1. Collection Date/Time
     * 2. Request Placer Group Number
     * 3. Request ZBR11 Sort Key
     * 4. Request Nomenclature Alternate Name 1
     * 5. Request Set Id
     */
    public static final Comparator<OlisLabResultDisplay> OLIS_LAB_RESULT_DISPLAY_COMPARATOR = new Comparator<OlisLabResultDisplay>() {
        @Override
        public int compare(OlisLabResultDisplay o1, OlisLabResultDisplay o2) {
            Date collectionDate1 = o1.getCollectionDateAsDate();
            Date collectionDate2 = o2.getCollectionDateAsDate();
            int compared = 0;
            
            // If both collection dates are not null, compares them using the Date objects compareTo
            // If Collection Date 1 is null and Collection Date 2 is not, sets compared to 1 to indicate that o1 should come later in the list than o2
            // If Collection Date 1 is not null, sets compared to -1 to indicate that o2 should come later in the list than o1
            if (collectionDate1 != null && collectionDate2 != null) {
                // Compares the collection dates, using o2 as the basis in order to order it in reverse chronological
                compared = collectionDate2.compareTo(collectionDate1);
            } else if (collectionDate1 == null && collectionDate2 != null) {
                compared = 1;
            } else if (collectionDate1 != null) {
                compared = -1;
            }
            
            // If the dates are the same (either they are both null or compareTo returned 0), continues comparing other elements to determine order
            if (compared == 0) {
                // Compares placer group numbers, continuing to compare other attributes if they are the same
                compared = OLISUtils.compareStringEmptyIsMore(o1.getPlacerGroupNo(), o2.getPlacerGroupNo());
                if (compared == 0) {
                    // Compares the ZBR11 sort key, continuing to compare other attributes if they are the same
                    compared = OLISUtils.compareStringEmptyIsMore(o1.getTestRequestZbr11(), o2.getTestRequestZbr11());
                    if (compared == 0) {
                        // Compares the nomenclature sort keys, continuing to compare other attributes if they are the same
                        compared = OLISUtils.compareStringEmptyIsMore(o1.getNomenclature().getSortKey(), o2.getNomenclature().getSortKey());
                        if (compared == 0) {
                            // Compares the alternate names stored in the nomenclature, continuing to compare other attributes if they are the same
                            compared = OLISUtils.compareStringEmptyIsMore(o1.getNomenclature().getRequestAlternateName1(), o2.getNomenclature().getRequestAlternateName1());
                            if (compared == 0) {
                                // Compares the set ids to determine order
                                compared = Integer.compare(o1.getObrSetId(), o2.getObrSetId());
                            }
                        }
                    }
                }
            }
            
            return compared;
        }
    };
}
