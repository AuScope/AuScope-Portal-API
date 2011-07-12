package org.auscope.portal.server.vegl;

import org.auscope.portal.server.cloud.CloudJob;

/**
 * A specialisation of a generic cloud job for the VEGL Portal
 * 
 * A VEGL job is assumed to write all output to a specific S3 location
 * @author Josh Vote
 *
 */
public class VEGLJob extends CloudJob {

	private String s3OutputAccessKey;
    private String s3OutputSecretKey;
    private String s3OutputBucket;    
    private String s3OutputBaseKey;
    private String registeredUrl;
    private Integer seriesId;
    private String fileStorageId;
	
    /**
     * Creates an unitialised VEGLJob
     */
    public VEGLJob() {
    	super();
    	
    	this.s3OutputAccessKey = "";
		this.s3OutputSecretKey = "";
		this.s3OutputBucket = "";
		this.s3OutputBaseKey = "";
		this.registeredUrl = "";
		this.seriesId = 0;
		this.fileStorageId = "";
    }
    
	/**
	 * Creates a fully initialised VEGLJob
	 * @param id ID for this job
	 * @param description Description of this job
	 * @param submitDate The date of submission for this job
	 * @param user The username of whoever is running this job
	 * @param emailAddress The contact email for whoever is running this job
	 * @param status The descriptive status of this job
	 * @param ec2InstanceId The ID of the running AMI instance (not the actual AMI ID).
	 * @param ec2Endpoint The endpoint for the elastic compute cloud
	 * @param ec2AMI The Amazon Machine Instance ID of the VM type that will run this job
	 * @param s3OutputAccessKey the access key used to connect to amazon S3 for storing output
	 * @param s3OutputSecretKey the secret key used to connect to amazon S3 for storing output 
	 * @param s3OutputBucket the S3 bucket name where output will be stored
	 * @param s3OutputBaseKey the base key path (folder name) for all S3 output
	 * @param registeredUrl Where this job has been registered for future reference
	 * @param fileStorageId The ID of this job that is used for storing input/output files
	 */
    public VEGLJob(Integer id, String name, String description, String user, String emailAddress, String submitDate,
			String status, String ec2InstanceId, String ec2Endpoint, String ec2AMI, String s3OutputAccessKey,
			String s3OutputSecretKey, String s3OutputBucket,
			String s3OutputBaseKey, String registeredUrl, Integer seriesId, String fileStorageId) {
		super(id,name, description, user, emailAddress, submitDate, status, ec2InstanceId, ec2AMI, ec2Endpoint);
		
		this.s3OutputAccessKey = s3OutputAccessKey;
		this.s3OutputSecretKey = s3OutputSecretKey;
		this.s3OutputBucket = s3OutputBucket;
		this.s3OutputBaseKey = s3OutputBaseKey;
		this.registeredUrl = registeredUrl;
		this.seriesId = seriesId;
		this.fileStorageId = fileStorageId;
	}
    
    /**
	 * Gets the access key used to connect to amazon S3 for storing output
	 * @return
	 */
	public String getS3OutputAccessKey() {
		return s3OutputAccessKey;
	}

	/**
	 * Sets the access key used to connect to amazon S3 for storing output
	 * @param s3OutputAccessKey
	 */
	public void setS3OutputAccessKey(String s3OutputAccessKey) {
		this.s3OutputAccessKey = s3OutputAccessKey;
	}

	/**
	 * Gets the secret key used to connect to amazon S3 for storing output 
	 * @return
	 */
	public String getS3OutputSecretKey() {
		return s3OutputSecretKey;
	}

	/**
	 * Sets the secret key used to connect to amazon S3 for storing output
	 * @param s3OutputSecretKey
	 */
	public void setS3OutputSecretKey(String s3OutputSecretKey) {
		this.s3OutputSecretKey = s3OutputSecretKey;
	}

	/**
	 * Gets the bucket name where output will be stored
	 * @return
	 */
	public String getS3OutputBucket() {
		return s3OutputBucket;
	}

	/**
	 * Sets the bucket name where output will be stored
	 * @param s3OutputBucket
	 */
	public void setS3OutputBucket(String s3OutputBucket) {
		this.s3OutputBucket = s3OutputBucket;
	}

	/**
	 * Gets the base key path (folder name) for all S3 output
	 * @return
	 */
	public String getS3OutputBaseKey() {
		return s3OutputBaseKey;
	}

	/**
	 * Sets the base key path (folder name) for all S3 output
	 * @param s3OutputBaseKey
	 */
	public void setS3OutputBaseKey(String s3OutputBaseKey) {
		this.s3OutputBaseKey = s3OutputBaseKey;
	}

	
	/**
	 * Gets where this job has been registered
	 * @return
	 */
	public String getRegisteredUrl() {
		return registeredUrl;
	}

	/**
	 * Sets where this job has been registered
	 * @param registeredUrl
	 */
	public void setRegisteredUrl(String registeredUrl) {
		this.registeredUrl = registeredUrl;
	}

	
	/**
	 * Gets the ID of the series this job belongs to
	 * @return
	 */
	public Integer getSeriesId() {
		return seriesId;
	}

	/**
	 * Sets the ID of the series this job belongs to
	 * @param seriesId
	 */
	public void setSeriesId(Integer seriesId) {
		this.seriesId = seriesId;
	}
	
	
	/**
	 * Gets the ID that is used for storing job input/output files
	 * @return
	 */
	public String getFileStorageId() {
		return fileStorageId;
	}

	/**
	 * Sets the ID that is used for storing job input/output files
	 * @param fileStorageId
	 */
	public void setFileStorageId(String fileStorageId) {
		this.fileStorageId = fileStorageId;
	}

	/**
	 * Prints the NON SENSITIVE information associated with this job to a string
	 */
	@Override
	public String toString() {
		return "VEGLJob [registeredUrl=" + registeredUrl + ", s3OutputBaseKey="
				+ s3OutputBaseKey + ", s3OutputBucket=" + s3OutputBucket
				+ ", seriesId=" + seriesId + ", getDescription()="
				+ getDescription() + ", getEc2AMI()=" + getEc2AMI()
				+ ", getName()=" + getName()
				+ ", getEc2Endpoint()=" + getEc2Endpoint()
				+ ", getEc2InstanceId()=" + getEc2InstanceId()
				+ ", getEmailAddress()=" + getEmailAddress() + ", getId()="
				+ getId() + ", getStatus()=" + getStatus()
				+ ", getSubmitDate()=" + getSubmitDate() + ", getUser()="
				+ getUser() + "]";
	}

	
}
