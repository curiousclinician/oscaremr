package org.oscarehr.PMmodule.model;

import java.io.Serializable;
import java.util.Calendar;

public class SdmtDetail implements Serializable {
	 private static final long serialVersionUID = 1L;
	 private Integer batchNumber;
	 private Calendar batchDate;
	 private String firstName;
	 private String lastName;
	 private Calendar dob;
	 private String benefitUnitStatus;
	 private String program;
	 private String office;
	 private Integer recordId;
	 private Calendar terminationDate;
	 private String lastBenMonth;
	 private Double totalPayment;
	 private Double basicAmount;
	 private Double housingAmount;
	 private Calendar paidDate;
	 private String address;
	 private String status;
	 private String role;
	 private Integer sdmtBenUnitId;
	 private Integer clientId;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Double getBasicAmount() {
		return basicAmount;
	}
	public void setBasicAmount(Double basicAmount) {
		this.basicAmount = basicAmount;
	}
	public Calendar getBatchDate() {
		return batchDate;
	}
	public void setBatchDate(Calendar batchDate) {
		this.batchDate = batchDate;
	}
	public Integer getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(Integer batchNumber) {
		this.batchNumber = batchNumber;
	}
	public String getBenefitUnitStatus() {
		return benefitUnitStatus;
	}
	public void setBenefitUnitStatus(String benefitUnitStatus) {
		this.benefitUnitStatus = benefitUnitStatus;
	}
	public Integer getClientId() {
		return clientId;
	}
	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}
	public Calendar getDob() {
		return dob;
	}
	public void setDob(Calendar dob) {
		this.dob = dob;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public Double getHousingAmount() {
		return housingAmount;
	}
	public void setHousingAmount(Double housingAmount) {
		this.housingAmount = housingAmount;
	}
	public String getLastBenMonth() {
		return lastBenMonth;
	}
	public void setLastBenMonth(String lastBenMonth) {
		this.lastBenMonth = lastBenMonth;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getOffice() {
		return office;
	}
	public void setOffice(String office) {
		this.office = office;
	}
	public Calendar getPaidDate() {
		return paidDate;
	}
	public void setPaidDate(Calendar paidDate) {
		this.paidDate = paidDate;
	}
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	public Integer getRecordId() {
		return recordId;
	}
	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Integer getSdmtBenUnitId() {
		return sdmtBenUnitId;
	}
	public void setSdmtBenUnitId(Integer sdmtBenUnitId) {
		this.sdmtBenUnitId = sdmtBenUnitId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Calendar getTerminationDate() {
		return terminationDate;
	}
	public void setTerminationDate(Calendar terminationDate) {
		this.terminationDate = terminationDate;
	}
	public Double getTotalPayment() {
		return totalPayment;
	}
	public void setTotalPayment(Double totalPayment) {
		this.totalPayment = totalPayment;
	}	 
}
