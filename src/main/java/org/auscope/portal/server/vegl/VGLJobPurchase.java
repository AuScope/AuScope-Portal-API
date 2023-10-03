package org.auscope.portal.server.vegl;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.auscope.portal.server.web.security.PortalUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Simple class that stores user job purchase information
 * @author rob508
 *
 */
@Entity
@Table(name = "job_purchases")
public class VGLJobPurchase implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable=false)
    private Date date;
    
    @Column(nullable=false)
    private Float amount;

    @Column(nullable=false)
    private Integer jobId;
    
    @Column(nullable=false)
    private String jobName;
    
    @Column(nullable=false)
    private String paymentRecord;
    
    /** The user who made the purchase */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private PortalUser parent;
    
    public VGLJobPurchase() {
        super();
    }
    
    public VGLJobPurchase(Date date, Float amount, Integer jobId, String jobName,
           String paymentRecord, PortalUser user) {
        super();
        this.date = date;
        this.amount = amount;
        this.jobId = jobId;
        this.jobName = jobName;
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
    
    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
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
    public PortalUser getParent() {
        return parent;
    }

    /**
     * User that made the purchases
     * @param parent
     */
    public void setParent(PortalUser parent) {
        this.parent = parent;
    }
   
}
