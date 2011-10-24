package org.auscope.portal.server.cloud;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract base class representing the base state of a job that is sent to the
 * cloud for processing.
 *
 * @author Josh Vote
 *
 */
public abstract class CloudJob implements Serializable {

    private Integer id;
    private String name;
    private String description;
    private String emailAddress;
    private String user;
    private String submitDate;
    private String status;

    private String ec2AMI;
    private String ec2InstanceId;
    private String ec2Endpoint;

    protected CloudJob() {
        super();
        this.id = 0;
        this.user = "";
        this.emailAddress = "";
        this.status = "";
        this.name = "";
        this.description = "";
        this.submitDate = "";
        this.ec2InstanceId = "";
        this.ec2AMI = "";
        this.ec2Endpoint = "";
    }

    protected CloudJob(Integer id, String name, String description,
            String user, String emailAddress, String submitDate, String status,
            String ec2InstanceId, String ec2AMI, String ec2Endpoint) {
        super();
        this.id = id;
        this.user = user;
        this.emailAddress = emailAddress;
        this.status = status;
        this.name = name;
        this.description = description;
        this.submitDate = submitDate;
        this.ec2InstanceId = ec2InstanceId;
        this.ec2AMI = ec2AMI;
        this.ec2Endpoint = ec2Endpoint;
    }

    /**
     * Gets a description of this job
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the status of this job
     *
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets the user who owns this job
     *
     * @return
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user who owns this job
     *
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Sets the status of this job
     *
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets a description of this job
     *
     * @return
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets an ID for this job
     *
     * @return
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets an ID for this job
     *
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the EC2 endpoint for where this job will be run
     *
     * @return
     */
    public String getEc2Endpoint() {
        return ec2Endpoint;
    }

    /**
     * Sets the EC2 endpoint for where this job will be run
     *
     * @param ec2Endpoint
     */
    public void setEc2Endpoint(String ec2Endpoint) {
        this.ec2Endpoint = ec2Endpoint;
    }

    /**
     * Gets the ID of the Amazon Machine Instance (Virtual Machine) that runs
     * this job
     *
     * @return
     */
    public String getEc2AMI() {
        return ec2AMI;
    }

    /**
     * Sets the ID of the Amazon Machine Instance (Virtual Machine) that runs
     * this job
     *
     * @param ec2ami
     */
    public void setEc2AMI(String ec2ami) {
        ec2AMI = ec2ami;
    }

    /**
     * Gets the date when this job was submitted
     *
     * @return
     */
    public String getSubmitDate() {
        return submitDate;
    }

    /**
     * Sets the date when this job was submitted
     *
     * @param submitDate
     */
    public void setSubmitDate(String submitDate) {
        this.submitDate = submitDate;
    }

    public void setSubmitDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        this.submitDate = sdf.format(date);
    }

    /**
     * Gets the ID of the Virtual machine instance that this job started
     *
     * @return
     */
    public String getEc2InstanceId() {
        return ec2InstanceId;
    }

    /**
     * Sets the ID of the Virtual machine instance that this job started
     *
     * @param ec2InstanceId
     */
    public void setEc2InstanceId(String ec2InstanceId) {
        this.ec2InstanceId = ec2InstanceId;
    }

    /**
     * Gets the contact email address for this job
     *
     * @return
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the contact email address for this job
     *
     * @param emailAddress
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Gets the descriptive name of this job
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the descriptive name of this job
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CloudJob [description=" + description + ", ec2AMI=" + ec2AMI
                + ", ec2Endpoint=" + ec2Endpoint + ", ec2InstanceId="
                + ec2InstanceId + ", emailAddress=" + emailAddress + ", id="
                + id + ", name=" + name + ", status=" + status
                + ", submitDate=" + submitDate + ", user=" + user + "]";
    }
}
