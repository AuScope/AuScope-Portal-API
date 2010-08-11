package org.auscope.portal.server.gridjob;

/**
 * Simple class that holds info required when registering jobs to Geonetwork.
 * @author Abdi Jama
 *
 */
public class GeodesyRecordInfo {
	

	private String creatorName;
    private String creatorEmail;
    private String creatorIdp;
    private String recordName;
    private String recordDateTime;
    private String recordDesc;
    private String keyWord;
    private String location;
    private String recordUrl;
    private double westBoundLongitude = 112.907;
    private double eastBoundLongitude = 158.96;
    private double southBoundLatitude = -54.7539;
    private double northBoundLatitude = -10.1357;
    private String supplementalInformation;

    public GeodesyRecordInfo(String creatorName, String creatorEmail,
			String creatorIdp, String recordName, String recordDateTime,
			String recordDesc, String keyWord, String location,
			String recordUrl, double westBoundLongitude,
			double eastBoundLongitude1, double southBoundLatitude,
			double northBoundLatitude, String supplementalInformation) {
		super();
		this.creatorName = creatorName;
		this.creatorEmail = creatorEmail;
		this.creatorIdp = creatorIdp;
		this.recordName = recordName;
		this.recordDateTime = recordDateTime;
		this.recordDesc = recordDesc;
		this.keyWord = keyWord;
		this.location = location;
		this.recordUrl = recordUrl;
		this.westBoundLongitude = westBoundLongitude;
		this.eastBoundLongitude = eastBoundLongitude1;
		this.southBoundLatitude = southBoundLatitude;
		this.northBoundLatitude = northBoundLatitude;
		this.supplementalInformation = supplementalInformation;
	}
    
	/**
	 * @return the creatorName
	 */
	public String getCreatorName() {
		return creatorName;
	}
	/**
	 * @param creatorName the creatorName to set
	 */
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	/**
	 * @return the creatorEmail
	 */
	public String getCreatorEmail() {
		return creatorEmail;
	}
	/**
	 * @param creatorEmail the creatorEmail to set
	 */
	public void setCreatorEmail(String creatorEmail) {
		this.creatorEmail = creatorEmail;
	}
	/**
	 * @return the creatorIdp
	 */
	public String getCreatorIdp() {
		return creatorIdp;
	}
	/**
	 * @param creatorIdp the creatorIdp to set
	 */
	public void setCreatorIdp(String creatorIdp) {
		this.creatorIdp = creatorIdp;
	}
	/**
	 * @return the recordName
	 */
	public String getRecordName() {
		return recordName;
	}
	/**
	 * @param recordName the recordName to set
	 */
	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}
	/**
	 * @return the recordDateTime
	 */
	public String getRecordDateTime() {
		return recordDateTime;
	}
	/**
	 * @param recordDateTime the recordDateTime to set
	 */
	public void setRecordDateTime(String recordDateTime) {
		this.recordDateTime = recordDateTime;
	}
	/**
	 * @return the recordDesc
	 */
	public String getRecordDesc() {
		return recordDesc;
	}
	/**
	 * @param recordDesc the recordDesc to set
	 */
	public void setRecordDesc(String recordDesc) {
		this.recordDesc = recordDesc;
	}
	/**
	 * @return the keyWord
	 */
	public String getKeyWord() {
		return keyWord;
	}
	/**
	 * @param keyWord the keyWord to set
	 */
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	/**
	 * @return the recordUrl
	 */
	public String getRecordUrl() {
		return recordUrl;
	}
	/**
	 * @param recordUrl the recordUrl to set
	 */
	public void setRecordUrl(String recordUrl) {
		this.recordUrl = recordUrl;
	}
	/**
	 * @return the westBoundLongitude
	 */
	public double getWestBoundLongitude() {
		return westBoundLongitude;
	}
	/**
	 * @param westBoundLongitude the westBoundLongitude to set
	 */
	public void setWestBoundLongitude(double westBoundLongitude) {
		this.westBoundLongitude = westBoundLongitude;
	}
	/**
	 * @return the eastBoundLongitude1
	 */
	public double getEastBoundLongitude() {
		return eastBoundLongitude;
	}
	/**
	 * @param eastBoundLongitude1 the eastBoundLongitude1 to set
	 */
	public void setEastBoundLongitude1(double eastBoundLongitude) {
		this.eastBoundLongitude = eastBoundLongitude;
	}
	/**
	 * @return the southBoundLatitude
	 */
	public double getSouthBoundLatitude() {
		return southBoundLatitude;
	}
	/**
	 * @param southBoundLatitude the southBoundLatitude to set
	 */
	public void setSouthBoundLatitude(double southBoundLatitude) {
		this.southBoundLatitude = southBoundLatitude;
	}
	/**
	 * @return the northBoundLatitude
	 */
	public double getNorthBoundLatitude() {
		return northBoundLatitude;
	}
	/**
	 * @param northBoundLatitude the northBoundLatitude to set
	 */
	public void setNorthBoundLatitude(double northBoundLatitude) {
		this.northBoundLatitude = northBoundLatitude;
	}
	/**
	 * @return the supplementalInformation
	 */
	public String getSupplementalInformation() {
		return supplementalInformation;
	}
	/**
	 * @param supplementalInformation the supplementalInformation to set
	 */
	public void setSupplementalInformation(String supplementalInformation) {
		this.supplementalInformation = supplementalInformation;
	}
}
