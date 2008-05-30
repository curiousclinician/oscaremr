package org.oscarehr.PMmodule.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import oscar.MyDateFormat;

public class Admission implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private Integer clientId;
	private boolean familyMember;  //for family admission discharge jsp page
	private Integer programId;
	private String providerNo;
	private java.util.Calendar admissionDate;
	private String admissionDateTxt;
	private String admissionStatus;
	private String dischargeNotes;
	private Calendar dischargeDate;
	private boolean automaticDischarge=false;

	private Integer intakeId;
	private Integer facilityId;
	private String programName;
	private String programType;

	private String communityProgramCode;
	private Integer bedProgramId;
	private String bedProgramDesc;
	private String communityProgramDesc;
	private String transportationType;
	private String transportationTypeDesc;

	private String dischargeReason;
	
	private String residentStatus;
	private String primaryWorker;
	private String lockerNo;
	private String noOfBags;
	private String nextKinName;
	private String nextKinRelationship;
	private String nextKinTelephone;
	private String nextKinNumber;
	private String nextKinStreet;
	private String nextKinCity;
	private String nextKinProvince;
	private String nextKinPostal;
	private Calendar ovPassStartDate;
	private String ovPassStartDateTxt;
	private Calendar ovPassEndDate;
	private String ovPassEndDateTxt;
    private String notSignReason;
    private String admissionNotes;
    
    private String bedName;
    private Integer intakeHeadId;
    
    public String getBedName() {
		return bedName;
	}

	public void setBedName(String bedName) {
		this.bedName = bedName;
	}

	public String getAdmissionDateStr() {
		return MyDateFormat.getStandardDate(admissionDate);
	}

    public String getAdmissionDateTxt() {
		return admissionDateTxt;
	}
    public void setAdmissionDateTxt(String admissionDateTxt) {
    	this.admissionDateTxt=admissionDateTxt;
    }
    public java.util.Calendar getAdmissionDate() {
		return admissionDate;
	}
	public void setAdmissionDate(java.util.Calendar admissionDate) {
		this.admissionDate = admissionDate;
	}
	public String getAdmissionStatus() {
		return admissionStatus;
	}
	public void setAdmissionStatus(String admissionStatus) {
		this.admissionStatus = admissionStatus;
	}
	public Integer getClientId() {
		return clientId;
	}
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}
	public Integer getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(Integer facilityId) {
		this.facilityId = facilityId;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getIntakeId() {
		return intakeId;
	}
	public void setIntakeId(Integer intakeId) {
		this.intakeId = intakeId;
	}
	public String getLockerNo() {
		return lockerNo;
	}
	public void setLockerNo(String lockerNo) {
		this.lockerNo = lockerNo;
	}
	public String getNextKinCity() {
		return nextKinCity;
	}
	public void setNextKinCity(String nextKinCity) {
		this.nextKinCity = nextKinCity;
	}
	public String getNextKinName() {
		return nextKinName;
	}
	public void setNextKinName(String nextKinName) {
		this.nextKinName = nextKinName;
	}
	public String getNextKinNumber() {
		return nextKinNumber;
	}
	public void setNextKinNumber(String nextKinNumber) {
		this.nextKinNumber = nextKinNumber;
	}
	public String getNextKinPostal() {
		return nextKinPostal;
	}
	public void setNextKinPostal(String nextKinPostal) {
		this.nextKinPostal = nextKinPostal;
	}
	public String getNextKinProvince() {
		return nextKinProvince;
	}
	public void setNextKinProvince(String nextKinProvince) {
		this.nextKinProvince = nextKinProvince;
	}
	public String getNextKinRelationship() {
		return nextKinRelationship;
	}
	public void setNextKinRelationship(String nextKinRelationship) {
		this.nextKinRelationship = nextKinRelationship;
	}
	public String getNextKinStreet() {
		return nextKinStreet;
	}
	public void setNextKinStreet(String nextKinStreet) {
		this.nextKinStreet = nextKinStreet;
	}
	public String getNextKinTelephone() {
		return nextKinTelephone;
	}
	public void setNextKinTelephone(String nextKinTelephone) {
		this.nextKinTelephone = nextKinTelephone;
	}
	public String getNoOfBags() {
		return noOfBags;
	}
	public void setNoOfBags(String noOfBags) {
		this.noOfBags = noOfBags;
	}
	public String getNotSignReason() {
		return notSignReason;
	}
	public void setNotSignReason(String notSignReason) {
		this.notSignReason = notSignReason;
	}
	public Calendar getOvPassEndDate() {
		return ovPassEndDate;
	}
	public void setOvPassEndDate(Calendar ovPassEndDate) {
		this.ovPassEndDate = ovPassEndDate;
	}
	public Calendar getOvPassStartDate() {
		return ovPassStartDate;
	}
	public void setOvPassStartDate(Calendar ovPassStartDate) {
		this.ovPassStartDate = ovPassStartDate;
	}
	public String getPrimaryWorker() {
		return primaryWorker;
	}
	public void setPrimaryWorker(String primaryWorker) {
		this.primaryWorker = primaryWorker;
	}
	public Integer getProgramId() {
		return programId;
	}
	public void setProgramId(Integer programId) {
		this.programId = programId;
	}
	public String getProgramName() {
		return programName;
	}
	public void setProgramName(String programName) {
		this.programName = programName;
	}
	public String getProgramType() {
		return programType;
	}
	public void setProgramType(String programType) {
		this.programType = programType;
	}
	public String getProviderNo() {
		return providerNo;
	}
	public void setProviderNo(String providerNo) {
		this.providerNo = providerNo;
	}
	public String getResidentStatus() {
		return residentStatus;
	}
	public void setResidentStatus(String residentStatus) {
		this.residentStatus = residentStatus;
	}
	public Calendar getDischargeDate() {
		return dischargeDate;
	}
	public void setDischargeDate(Calendar dischargeDate) {
		this.dischargeDate = dischargeDate;
	}
	public String getDischargeNotes() {
		return dischargeNotes;
	}
	public void setDischargeNotes(String dischargeNotes) {
		this.dischargeNotes = dischargeNotes;
	}
	public String getOvPassEndDateTxt() {
		return ovPassEndDateTxt;
	}
	public void setOvPassEndDateTxt(String ovPassEndDateTxt) {
		this.ovPassEndDateTxt = ovPassEndDateTxt;
	}
	public String getOvPassStartDateTxt() {
		return ovPassStartDateTxt;
	}
	public void setOvPassStartDateTxt(String ovPassStartDateTxt) {
		this.ovPassStartDateTxt = ovPassStartDateTxt;
	}

	public boolean isAutomaticDischarge() {
		return automaticDischarge;
	}

	public void setAutomaticDischarge(boolean automaticDischarge) {
		this.automaticDischarge = automaticDischarge;
	}

	public String getCommunityProgramCode() {
		return communityProgramCode;
	}

	public void setCommunityProgramCode(String communityProgramCode) {
		this.communityProgramCode = communityProgramCode;
	}

	public String getBedProgramDesc() {
		return bedProgramDesc;
	}

	public void setBedProgramDesc(String bedProgramDesc) {
		this.bedProgramDesc = bedProgramDesc;
	}

	public Integer getBedProgramId() {
		return bedProgramId;
	}

	public void setBedProgramId(Integer bedProgramId) {
		this.bedProgramId = bedProgramId;
	}

	public String getCommunityProgramDesc() {
		return communityProgramDesc;
	}

	public void setCommunityProgramDesc(String communityProgramDesc) {
		this.communityProgramDesc = communityProgramDesc;
	}

	public String getTransportationType() {
		return transportationType;
	}

	public void setTransportationType(String transportationType) {
		this.transportationType = transportationType;
	}

	public String getTransportationTypeDesc() {
		return transportationTypeDesc;
	}

	public void setTransportationTypeDesc(String transportationTypeDesc) {
		this.transportationTypeDesc = transportationTypeDesc;
	}

	public String getDischargeReason() {
		return dischargeReason;
	}

	public void setDischargeReason(String dischargeReason) {
		this.dischargeReason = dischargeReason;
	}

	public Object clone() {
      try {
          return super.clone();
      } catch (CloneNotSupportedException e) {
          throw new Error("This should not occur since we implement Cloneable");
      }
    }

	public String getAdmissionNotes() {
		return admissionNotes;
	}

	public void setAdmissionNotes(String admissionNotes) {
		this.admissionNotes = admissionNotes;
	}
		
	public boolean isFamilyMember() {
		return familyMember;
	}

	public void setFamilyMember(boolean familyMember) {
		this.familyMember = familyMember;
	}

	public Integer getIntakeHeadId() {
		return intakeHeadId;
	}

	public void setIntakeHeadId(Integer intakeHeadId) {
		this.intakeHeadId = intakeHeadId;
	}
	
}
