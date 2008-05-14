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
package org.oscarehr.PMmodule.model;

import java.io.Serializable;

/**
 * This is the object class that relates to the program_provider table.
 * Any customizations belong here.
 */
public class ProgramProvider implements Serializable {

	private static final long serialVersionUID = 1L;
    public static String PROP_PROVIDER_NO = "ProviderNo";
    public static String PROP_PROGRAM_ID = "ProgramId";
    public static String PROP_PROVIDER = "provider";
    public static String PROP_ROLE = "role";
    public static String PROP_ID = "Id";
    public static String PROP_ROLE_ID = "RoleId";
    private int hashCode = Integer.MIN_VALUE;// primary key
    private Integer id;// fields
    private Integer programId;
    private String providerNo;
    private Integer roleId;// many to one
    private org.caisi.model.Role _role;
    private Provider _provider;// collections
    private java.util.Set _teams;
    private String programName;
    private Program program;

    // constructors
	public ProgramProvider () {
		initialize();
	}

    /**
	 * Constructor for primary key
	 */
	public ProgramProvider (Integer _id) {
		this.setId(_id);
		initialize();
	}

    /**
	 * @return Returns the programName.
	 */
	public String getProgramName() {
		return programName;
	}

	/**
	 * @param programName The programName to set.
	 */
	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public Program getProgram() {
		return program;
	}

	public void setProgram(Program program) {
		this.program = program;
	}

    protected void initialize () {}

    /**
	 * Return the unique identifier of this class
* @hibernate.id
*  generator-class="native"
*  column="id"
*/
    public Integer getId () {
        return id;
    }

    /**
	 * Set the unique identifier of this class
     * @param _id the new ID
     */
    public void setId (Integer _id) {
        this.id = _id;
        this.hashCode = Integer.MIN_VALUE;
    }

    /**
	 * Return the value associated with the column: program_id
     */
    public Integer getProgramId () {
        return programId;
    }

    /**
	 * Set the value related to the column: program_id
     * @param _programId the program_id value
     */
    public void setProgramId (Integer _programId) {
        this.programId = _programId;
    }

    /**
	 * Return the value associated with the column: provider_no
     */
    public String getProviderNo () {
        return providerNo;
    }

    /**
	 * Set the value related to the column: provider_no
     * @param _providerNo the provider_no value
     */
    public void setProviderNo (String _providerNo) {
        this.providerNo = _providerNo;
    }

    /**
	 * Return the value associated with the column: role_id
     */
    public Integer getRoleId () {
        return roleId;
    }

    /**
	 * Set the value related to the column: role_id
     * @param _roleId the role_id value
     */
    public void setRoleId (Integer _roleId) {
        this.roleId = _roleId;
    }

    /**
     * @hibernate.property
*  column=role_id
     */
    public org.caisi.model.Role getRole () {
        return this._role;
    }

    /**
	 * Set the value related to the column: role_id
     * @param _role the role_id value
     */
    public void setRole (org.caisi.model.Role _role) {
        this._role = _role;
    }

    /**
     * @hibernate.property
*  column=provider_no
     */
    public Provider getProvider () {
        return this._provider;
    }

    /**
	 * Set the value related to the column: provider_no
     * @param _provider the provider_no value
     */
    public void setProvider (Provider _provider) {
        this._provider = _provider;
    }

    /**
	 * Return the value associated with the column: teams
     */
    public java.util.Set getTeams () {
        return this._teams;
    }

    /**
	 * Set the value related to the column: teams
     * @param _teams the teams value
     */
    public void setTeams (java.util.Set _teams) {
        this._teams = _teams;
    }

    public void addToTeams (Object obj) {
        if (null == this._teams) this._teams = new java.util.HashSet();
        this._teams.add(obj);
    }

    public boolean equals (Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof ProgramProvider)) return false;
        else {
            ProgramProvider mObj = (ProgramProvider) obj;
            if (null == this.getId() || null == mObj.getId()) return false;
            else return (this.getId().equals(mObj.getId()));
        }
    }

    public int hashCode () {
        if (Integer.MIN_VALUE == this.hashCode) {
            if (null == this.getId()) return super.hashCode();
            else {
                String hashStr = this.getClass().getName() + ":" + this.getId().hashCode();
                this.hashCode = hashStr.hashCode();
            }
        }
        return this.hashCode;
    }

    public String toString () {
        return super.toString();
    }
}