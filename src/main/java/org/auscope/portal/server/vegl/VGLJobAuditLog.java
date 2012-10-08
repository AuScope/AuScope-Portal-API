package org.auscope.portal.server.vegl;

import java.io.Serializable;
import java.util.Date;

/**
 * A simple POJO class that stores a job life cycle transition
 * information for auditing purposes.
 *
 * @author Richard Goh
 */
public class VGLJobAuditLog implements Serializable {

    /**
     * Generated on 2012-09-24
     */
    private static final long serialVersionUID = -1762982566490775865L;

    /** The primary key for this parameter*/
    private Integer id;
    /** The id of the job that owns this parameter*/
    private Integer jobId;
    /** The descriptive status of the job before its status change */
    private String fromStatus;
    /** The descriptive status of the job after its status change  */
    private String toStatus;
    /** The date & time when this job changed its status*/
    private Date transitionDate;
    /** An optional job transition audit log */
    private String message;


    /**
     * Default constructor.
     */
    public VGLJobAuditLog() {
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the jobId
     */
    public Integer getJobId() {
        return jobId;
    }

    /**
     * @param jobId the jobId to set
     */
    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    /**
     * @return the fromStatus
     */
    public String getFromStatus() {
        return fromStatus;
    }

    /**
     * @param fromStatus the fromStatus to set
     */
    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    /**
     * @return the toStatus
     */
    public String getToStatus() {
        return toStatus;
    }

    /**
     * @param toStatus the toStatus to set
     */
    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    /**
     * @return the transitionDate
     */
    public Date getTransitionDate() {
        return transitionDate;
    }

    /**
     * @param transitionDate the transitionDate to set
     */
    public void setTransitionDate(Date transitionDate) {
        this.transitionDate = transitionDate;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
}