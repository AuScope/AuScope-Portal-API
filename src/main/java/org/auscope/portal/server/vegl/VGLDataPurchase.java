package org.auscope.portal.server.vegl;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.auscope.portal.server.web.security.ANVGLUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Simple class that stores user data purchase information
 * @author rob508
 *
 */
@Entity
@Table(name = "data_purchases")
public class VGLDataPurchase implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable=false)
    private Date date;
    
    @Column(nullable=false)
    private Float amount;
    
    @Column(nullable=false)
    private String downloadUrl;
    
    @Column(nullable=false)
    private String cswRecord;
    
    @Column(nullable=false)
    private String onlineResourceType;
    
    @Column(nullable=false)
    private String url;
    
    @Column(nullable=false)
    private String localPath;
    
    @Column(nullable=false)
    private String name;
    
    @Column(nullable=false)
    private String description;
    
    @Column(nullable=false)
    private Double northBoundLatitude;
    
    @Column(nullable=false)
    private Double southBoundLatitude;
    
    @Column(nullable=false)
    private Double eastBoundLongitude;
    
    @Column(nullable=false)
    private Double westBoundLongitude;
    
    @Column(nullable=false)
    private String paymentRecord;
    
    /** The user who made the purchase */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private ANVGLUser parent;
    
    public VGLDataPurchase() {
        super();
    }
    
    public VGLDataPurchase(Date date, Float amount, String downloadUrl, String cswRecord, String onlineResourceType, String url, String localPath, String name, String description, 
            Double northBoundLatitude, Double southBoundLatitude, Double eastBoundLongitude, Double westBoundLongitude, String paymentRecord, 
            ANVGLUser user) {
        super();
        this.date = date;
        this.amount = amount;
        this.downloadUrl = downloadUrl;
        this.cswRecord = cswRecord;
        this.onlineResourceType = onlineResourceType;
        this.url = url;
        this.localPath = localPath;
        this.name = name;
        this.description = description;
        this.northBoundLatitude = northBoundLatitude;
        this.southBoundLatitude = southBoundLatitude;
        this.eastBoundLongitude = eastBoundLongitude;
        this.westBoundLongitude = westBoundLongitude;
        this.paymentRecord = paymentRecord;
        this.parent = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }
    
    public String getCswRecord() {
        return cswRecord;
    }

    public void setCswRecord(String cswRecord) {
        this.cswRecord = cswRecord;
    }
    
    public String getOnlineResourceType() {
        return onlineResourceType;
    }

    public void setOnlineResourceType(String onlineResourceType) {
        this.onlineResourceType = onlineResourceType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    public void setNorthBoundLatitude(Double northBoundLatitude) {
        this.northBoundLatitude = northBoundLatitude;
    }

    public Double getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    public void setSouthBoundLatitude(Double southBoundLatitude) {
        this.southBoundLatitude = southBoundLatitude;
    }

    public Double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    public void setEastBoundLongitude(Double eastBoundLongitude) {
        this.eastBoundLongitude = eastBoundLongitude;
    }

    public Double getWestBoundLongitude() {
        return westBoundLongitude;
    }

    public void setWestBoundLongitude(Double westBoundLongitude) {
        this.westBoundLongitude = westBoundLongitude;
    }

    public String getPaymentRecord() {
        return this.paymentRecord;
    }
    
    public void setPaymentRecord(String paymentRecord) {
        this.paymentRecord = paymentRecord;
    }

    /**
     * User that made the purchases
     * @return
     */
    public ANVGLUser getParent() {
        return parent;
    }

    /**
     * User that made the purchases
     * @param parent
     */
    public void setParent(ANVGLUser parent) {
        this.parent = parent;
    }
   
}
