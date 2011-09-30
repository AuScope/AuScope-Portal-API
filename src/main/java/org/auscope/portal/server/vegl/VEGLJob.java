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

	private String cloudOutputAccessKey;
    private String cloudOutputSecretKey;
    private String cloudOutputBucket;    
    private String cloudOutputBaseKey;
    private String registeredUrl;
    private Integer seriesId;
    private String fileStorageId;
    private String vmSubsetFilePath;
    private Double paddingMinEasting;
    private Double paddingMaxEasting;
    private Double paddingMinNorthing;
    private Double paddingMaxNorthing;
    private Double selectionMinEasting;
    private Double selectionMaxEasting;
    private Double selectionMinNorthing;
    private Double selectionMaxNorthing;
    private String mgaZone;
    private Integer cellX;
    private Integer cellY;
    private Integer cellZ;
    private Integer inversionDepth;
    
    /**
     * Creates an unitialised VEGLJob
     */
    public VEGLJob() {
    	super();
    	
    	this.cloudOutputAccessKey = "";
		this.cloudOutputSecretKey = "";
		this.cloudOutputBucket = "";
		this.cloudOutputBaseKey = "";
		this.registeredUrl = "";
		this.seriesId = 0;
		this.fileStorageId = "";
		this.vmSubsetFilePath = "";
		
		this.paddingMinEasting = 0.0;
		this.paddingMaxEasting = 0.0;
		this.paddingMinNorthing = 0.0;
		this.paddingMaxNorthing = 0.0;
		this.selectionMinEasting = 0.0;
		this.selectionMaxEasting = 0.0;
		this.selectionMinNorthing = 0.0;
		this.selectionMaxNorthing = 0.0;
		this.mgaZone = "";
		this.cellX = 0;
		this.cellY = 0;
	    this.cellZ = 0;
	    this.inversionDepth = 0;
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
	 * @param cloudOutputAccessKey the access key used to connect to amazon S3 for storing output
	 * @param cloudOutputSecretKey the secret key used to connect to amazon S3 for storing output 
	 * @param cloudOutputBucket the S3 bucket name where output will be stored
	 * @param cloudOutputBaseKey the base key path (folder name) for all S3 output
	 * @param registeredUrl Where this job has been registered for future reference
	 * @param fileStorageId The ID of this job that is used for storing input/output files
	 * @param vmSubsetFilePath The File path (on the VM) where the job should look for its input subset file
	 */
    public VEGLJob(Integer id, String name, String description, String user, String emailAddress, String submitDate,
			String status, String ec2InstanceId, String ec2Endpoint, String ec2AMI, String cloudOutputAccessKey,
			String cloudOutputSecretKey, String cloudOutputBucket,
			String cloudOutputBaseKey, String registeredUrl, Integer seriesId, String fileStorageId, String vmSubsetFilePath) {
		super(id,name, description, user, emailAddress, submitDate, status, ec2InstanceId, ec2AMI, ec2Endpoint);
		
		this.cloudOutputAccessKey = cloudOutputAccessKey;
		this.cloudOutputSecretKey = cloudOutputSecretKey;
		this.cloudOutputBucket = cloudOutputBucket;
		this.cloudOutputBaseKey = cloudOutputBaseKey;
		this.registeredUrl = registeredUrl;
		this.seriesId = seriesId;
		this.fileStorageId = fileStorageId;
		this.vmSubsetFilePath = vmSubsetFilePath;
	}
    
    /**
	 * Gets the access key used to connect to amazon S3 for storing output
	 * @return
	 */
	public String getCloudOutputAccessKey() {
		return cloudOutputAccessKey;
	}

	/**
	 * Sets the access key used to connect to amazon S3 for storing output
	 * @param cloudOutputAccessKey
	 */
	public void setCloudOutputAccessKey(String cloudOutputAccessKey) {
		this.cloudOutputAccessKey = cloudOutputAccessKey;
	}

	/**
	 * Gets the secret key used to connect to amazon S3 for storing output 
	 * @return
	 */
	public String getCloudOutputSecretKey() {
		return cloudOutputSecretKey;
	}

	/**
	 * Sets the secret key used to connect to amazon S3 for storing output
	 * @param cloudOutputSecretKey
	 */
	public void setCloudOutputSecretKey(String cloudOutputSecretKey) {
		this.cloudOutputSecretKey = cloudOutputSecretKey;
	}

	/**
	 * Gets the bucket name where output will be stored
	 * @return
	 */
	public String getCloudOutputBucket() {
		return cloudOutputBucket;
	}

	/**
	 * Sets the bucket name where output will be stored
	 * @param cloudOutputBucket
	 */
	public void setCloudOutputBucket(String cloudOutputBucket) {
		this.cloudOutputBucket = cloudOutputBucket;
	}

	/**
	 * Gets the base key path (folder name) for all S3 output
	 * @return
	 */
	public String getCloudOutputBaseKey() {
		return cloudOutputBaseKey;
	}

	/**
	 * Sets the base key path (folder name) for all S3 output
	 * @param cloudOutputBaseKey
	 */
	public void setCloudOutputBaseKey(String cloudOutputBaseKey) {
		this.cloudOutputBaseKey = cloudOutputBaseKey;
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
	 * Gets the minimum easting of the padded bounds
	 * @return
	 */
	public Double getPaddingMinEasting() {
		return paddingMinEasting;
	}

	/**
	 * Sets the minimum easting of the padded bounds
	 * @param paddingMinEasting
	 */
	public void setPaddingMinEasting(Double paddingMinEasting) {
		this.paddingMinEasting = paddingMinEasting;
	}

	/**
	 * Gets the maximum easting of the padded bounds
	 * @return
	 */
	public Double getPaddingMaxEasting() {
		return paddingMaxEasting;
	}

	/**
	 * Sets the maximum easting of the padded bounds
	 * @param paddingMaxEasting
	 */
	public void setPaddingMaxEasting(Double paddingMaxEasting) {
		this.paddingMaxEasting = paddingMaxEasting;
	}

	/**
	 * Gets the minimum northing of the padded bounds
	 * @return
	 */
	public Double getPaddingMinNorthing() {
		return paddingMinNorthing;
	}
	
	/**
	 * Sets the minimum northing of the padded bounds
	 * @param paddingMinNorthing
	 */
	public void setPaddingMinNorthing(Double paddingMinNorthing) {
		this.paddingMinNorthing = paddingMinNorthing;
	}

	/**
	 * Gets the maximum northing of the padded bounds
	 * @return
	 */
	public Double getPaddingMaxNorthing() {
		return paddingMaxNorthing;
	}

	/**
	 * Sets the maximum northing of the padded bounds
	 * @param paddingMaxNorthing
	 */
	public void setPaddingMaxNorthing(Double paddingMaxNorthing) {
		this.paddingMaxNorthing = paddingMaxNorthing;
	}

	/**
	 * Gets the minimum easting of the selected bounds
	 * @return
	 */
	public Double getSelectionMinEasting() {
		return selectionMinEasting;
	}

	/**
	 * Sets the minimum easting of the selected bounds
	 * @param selectionMinEasting
	 */
	public void setSelectionMinEasting(Double selectionMinEasting) {
		this.selectionMinEasting = selectionMinEasting;
	}

	/**
	 * Gets the maximum easting of the selected bounds
	 * @return
	 */
	public Double getSelectionMaxEasting() {
		return selectionMaxEasting;
	}

	/**
	 * Sets the maximum easting of the selected bounds
	 * @param selectionMaxEasting
	 */
	public void setSelectionMaxEasting(Double selectionMaxEasting) {
		this.selectionMaxEasting = selectionMaxEasting;
	}

	/**
	 * Gets the minimum northing of the selected bounds
	 * @return
	 */
	public Double getSelectionMinNorthing() {
		return selectionMinNorthing;
	}

	/**
	 * Sets the minimum northing of the selected bounds
	 * @param selectionMinNorthing
	 */
	public void setSelectionMinNorthing(Double selectionMinNorthing) {
		this.selectionMinNorthing = selectionMinNorthing;
	}

	/**
	 * Gets the maximum northing of the selected bounds
	 * @return
	 */
	public Double getSelectionMaxNorthing() {
		return selectionMaxNorthing;
	}

	/**
	 * Sets the maximum northing of the selected bounds
	 * @param selectionMaxNorthing
	 */
	public void setSelectionMaxNorthing(Double selectionMaxNorthing) {
		this.selectionMaxNorthing = selectionMaxNorthing;
	}

	/**
	 * Gets the selected MGA zone of the padded bounds
	 * @return
	 */
	public String getMgaZone() {
		return mgaZone;
	}

	/**
	 * Sets the selected MGA zone of the padded bounds
	 * @param mgaZone
	 */
	public void setMgaZone(String mgaZone) {
		this.mgaZone = mgaZone;
	}

	/**
	 * Gets the selected cell size in the X direction
	 * @return
	 */
	public Integer getCellX() {
		return cellX;
	}

	/**
	 * Sets the selected cell size in the X direction
	 * @param cellX
	 */
	public void setCellX(Integer cellX) {
		this.cellX = cellX;
	}

	/**
	 * Gets the selected cell size in the Y direction
	 * @return
	 */
	public Integer getCellY() {
		return cellY;
	}

	/**
	 * Sets the selected cell size in the Y direction
	 * @param cellY
	 */
	public void setCellY(Integer cellY) {
		this.cellY = cellY;
	}

	/**
	 * Gets the selected cell size in the Z direction
	 * @return
	 */
	public Integer getCellZ() {
		return cellZ;
	}

	/**
	 * Sets the selected cell size in the Z direction
	 * @param cellZ
	 */
	public void setCellZ(Integer cellZ) {
		this.cellZ = cellZ;
	}

	/**
	 * Gets the selected inversion depth
	 * @return
	 */
	public Integer getInversionDepth() {
		return inversionDepth;
	}

	/**
	 * Sets the selected inversion depth
	 * @param inversionDepth
	 */
	public void setInversionDepth(Integer inversionDepth) {
		this.inversionDepth = inversionDepth;
	}
	
	
	/**
	 * Gets the File path (on the VM) where the job should look for its input subset file
	 * @return
	 */
	public String getVmSubsetFilePath() {
		return vmSubsetFilePath;
	}

	/**
	 * Sets the File path (on the VM) where the job should look for its input subset file
	 * @param vmSubsetFilePath
	 */
	public void setVmSubsetFilePath(String vmSubsetFilePath) {
		this.vmSubsetFilePath = vmSubsetFilePath;
	}

	/**
	 * Prints the NON SENSITIVE information associated with this job to a string
	 */
	@Override
	public String toString() {
		return "VEGLJob [cellX=" + cellX + ", cellY=" + cellY + ", cellZ="
				+ cellZ + ", fileStorageId=" + fileStorageId
				+ ", inversionDepth=" + inversionDepth + ", mgaZone=" + mgaZone
				+ ", paddingMaxEasting=" + paddingMaxEasting
				+ ", paddingMaxNorthing=" + paddingMaxNorthing
				+ ", paddingMinEasting=" + paddingMinEasting
				+ ", paddingMinNorthing=" + paddingMinNorthing
				+ ", registeredUrl=" + registeredUrl + ", s3OutputBaseKey="
				+ cloudOutputBaseKey + ", s3OutputBucket=" + cloudOutputBucket
				+ ", selectionMaxEasting=" + selectionMaxEasting
				+ ", selectionMaxNorthing=" + selectionMaxNorthing
				+ ", selectionMinEasting=" + selectionMinEasting
				+ ", selectionMinNorthing=" + selectionMinNorthing
				+ ", seriesId=" + seriesId + ", getDescription()="
				+ getDescription() + ", getEc2AMI()=" + getEc2AMI()
				+ ", getEc2Endpoint()=" + getEc2Endpoint()
				+ ", getEc2InstanceId()=" + getEc2InstanceId()
				+ ", getEmailAddress()=" + getEmailAddress() + ", getId()="
				+ getId() + ", getName()=" + getName() + ", getStatus()="
				+ getStatus() + ", getSubmitDate()=" + getSubmitDate()
				+ ", getUser()=" + getUser() + "]";
	}	
}
