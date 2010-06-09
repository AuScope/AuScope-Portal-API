package org.auscope.portal.server.gridjob;

import java.io.Serializable;

import net.sf.json.JSONObject;

/**
 * This class represents a url pointer to an input file to the grid (along with associated metadata)
 * @author VOT002
 *
 */
public class GeodesyGridInputFile implements Serializable {
	/**
	 * The 4 character ID of the station that generated this input file
	 */
	private String stationId;
	/**
	 * The remote URL where the actual input file is stored
	 */
	private String fileUrl;
	/**
	 * Whether this input file has been selected
	 */
	private boolean selected;
	/**
	 * The date at which this input file was generated
	 */
	private String fileDate;
	
	public GeodesyGridInputFile() {
		
	}
	
	public GeodesyGridInputFile(String stationId, String fileUrl,
			boolean selected, String fileDate) {
		this.stationId = stationId;
		this.fileUrl = fileUrl;
		this.selected = selected;
		this.fileDate = fileDate;
	}
	
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public String getFileDate() {
		return fileDate;
	}
	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}
	
	public static GeodesyGridInputFile fromJSONObject(JSONObject obj) {
		return new GeodesyGridInputFile(
				(String)obj.get("stationId"),
				(String)obj.get("fileUrl"),
				(Boolean)obj.get("selected"),
				(String)obj.get("fileDate")
				);
	}
}
