package org.oscarehr.PMmodule.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Service restriction
 */
public class ProgramClientRestriction implements Serializable {

    private Integer id;
    private Integer programId;
    private String programDesc;
    
    private Integer demographicNo;
    private String providerNo;
    private String providerFirstName;
    private String providerLastName;
    private String commentId;
    private String comments;
    private Date startDate;
    private Date endDate;
    private boolean enabled;
    private String earlyTerminationProvider;
    
    private Program program;
    private Demographic client;
    private Provider provider;

    public ProgramClientRestriction() {
    }

    public ProgramClientRestriction(Integer id, Integer programId, Integer demographicNo, String providerNo, String comments, Date startDate, Date endDate, boolean enabled, Program program, Demographic client) {
        this.id = id;
        this.programId = programId;
        this.demographicNo = demographicNo;
        this.providerNo = providerNo;
        this.comments = comments;
        this.startDate = startDate;
        this.endDate = endDate;
        this.enabled = enabled;
        this.program = program;
        this.client = client;
    }

    public String getProviderNo() {
        return providerNo;
    }

    public long getDaysRemaining() {
        return (this.getEndDate().getTime() - this.getStartDate().getTime()) / 1000 / 60 / 60 / 24;
    }

    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

    public Integer getDemographicNo() {
        return demographicNo;
    }

    public void setDemographicNo(Integer demographicNo) {
        this.demographicNo = demographicNo;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public Demographic getClient() {
        return client;
    }

    public void setClient(Demographic client) {
        this.client = client;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgramClientRestriction that = (ProgramClientRestriction) o;

        if (id != that.id) return false;

        return true;
    }

    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

    public String getEarlyTerminationProvider() {
        return earlyTerminationProvider;
    }

    public void setEarlyTerminationProvider(String earlyTerminationProvider) {
        this.earlyTerminationProvider = earlyTerminationProvider;
    }

	public String getProgramDesc() {
		return programDesc;
	}

	public void setProgramDesc(String programDesc) {
		this.programDesc = programDesc;
	}
	public String getProviderFormattedName() {
	    return getProviderLastName() + "," + getProviderFirstName();
	}
	public String getProviderFirstName() {
		return providerFirstName;
	}

	public void setProviderFirstName(String providerFirstName) {
		this.providerFirstName = providerFirstName;
	}

	public String getProviderLastName() {
		return providerLastName;
	}

	public void setProviderLastName(String providerLastName) {
		this.providerLastName = providerLastName;
	}

    

}
