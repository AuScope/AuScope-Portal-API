package org.auscope.portal.server.vegl;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
//@Table()   No table currently - legacy
public class VLScmSnapshot implements Serializable {

    private static final long serialVersionUID = -6638880820028925202L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String scmEntryId;
    private String computeVmId;
    private String computeServiceId;

    protected VLScmSnapshot() {
    }

    public VLScmSnapshot(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    protected void setId(Integer id) {
        this.id = id;
    }

    public String getScmEntryId() {
        return scmEntryId;
    }

    public void setScmEntryId(String scmEntryId) {
        this.scmEntryId = scmEntryId;
    }

    public String getComputeVmId() {
        return computeVmId;
    }

    public void setComputeVmId(String computeVmId) {
        this.computeVmId = computeVmId;
    }

    public String getComputeServiceId() {
        return computeServiceId;
    }

    public void setComputeServiceId(String computeServiceId) {
        this.computeServiceId = computeServiceId;
    }
}
