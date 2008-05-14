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

package org.oscarehr.survey.model;

import java.util.Date;

public class Survey extends BaseObject {
	
	public static final short STATUS_IN_REVIEW 	= 0;
	public static final short STATUS_TEST 		= 1;
	public static final short STATUS_LAUNCHED 	= 2;
	public static final short STATUS_CLOSED 	= 3;
	
	private Integer surveyId;
    private String description;
	private String surveyData;
    private Integer userId;
    private Integer facilityId;
    private Short status;
    private Date dateCreated;
    private Date dateLaunched;
    private Date dateClosed;
    private String dummy; //Used specifically for non-teared values of a form
    private Integer launchedInstanceId;
    private Integer version;
    
    public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	/**
     * @return Returns the dateClosed.
     */
    public Date getDateClosed() {
        return this.dateClosed;
    }
    /**
     * @param dateClosed The dateClosed to set.
     */
    public void setDateClosed(Date dateClosed) {
        this.dateClosed = dateClosed;
    }
    /**
     * @return Returns the dateCreated.
     */
    public Date getDateCreated() {
        return this.dateCreated;
    }
    /**
     * @param dateCreated The dateCreated to set.
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    /**
     * @return Returns the dateLaunched.
     */
    public Date getDateLaunched() {
        return this.dateLaunched;
    }
    /**
     * @param dateLaunched The dateLaunched to set.
     */
    public void setDateLaunched(Date dateLaunched) {
        this.dateLaunched = dateLaunched;
    }
    /**
     * @return Returns the status.
     */
    public Short getStatus() {
        return this.status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(Short status) {
        this.status = status;
    }
    /**
     * @return Returns the surveyData.
     */
    public String getSurveyData() {
        return this.surveyData;
    }
    /**
     * @param surveyData The surveyData to set.
     */
    public void setSurveyData(String surveyData) {
        this.surveyData = surveyData;
    }
    /**
     * @return Returns the surveyId.
     */
    public Integer getSurveyId() {
        return this.surveyId;
    }
    /**
     * @param surveyId The surveyId to set.
     */
    public void setSurveyId(Integer surveyId) {
        this.surveyId = surveyId;
    }
    /**
     * @return Returns the userId.
     */
    public Integer getUserId() {
        return this.userId;
    }
    /**
     * @param userId The userId to set.
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return Returns the dummy.
     */
    public String getDummy() {
        return this.dummy;
    }
    /**
     * @param dummy The dummy to set.
     */
    public void setDummy(String dummy) {
        this.dummy = dummy;
    }
	public Integer getLaunchedInstanceId() {
		return launchedInstanceId;
	}
	public void setLaunchedInstanceId(Integer launchedInstanceId) {
		this.launchedInstanceId = launchedInstanceId;
	}
    public Integer getFacilityId() {
        return facilityId;
    }
    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }
    
	
}
