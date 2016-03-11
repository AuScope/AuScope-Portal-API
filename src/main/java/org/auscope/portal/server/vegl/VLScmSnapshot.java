package org.auscope.portal.server.vegl;

import java.io.Serializable;

public class VLScmSnapshot implements Serializable {

    private static final long serialVersionUID = -6638880820028925202L;

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
