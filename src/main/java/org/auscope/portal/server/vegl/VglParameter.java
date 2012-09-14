package org.auscope.portal.server.vegl;

import java.io.Serializable;



/**
 * A Job Parameter is a single 'typed' (very loosely) key/value pairing.
 *
 * A typical Job will have one or more parameter values created as the job is constructed. The parameter
 * set is made available to any job scripts that get run
 *
 * @author Josh Vote
 *
 */
public class VglParameter implements Serializable {

    private static final long serialVersionUID = -7474027234400180238L;

    /**
     * The different types of parameter types
     * @author Josh Vote
     */
    public enum ParameterType {
        string,
        number
    }

    /** The primary key for this parameter*/
    private Integer id;
    /** The id of the job that owns this parameter*/
    private Integer jobId;
    /** The name of this parameter*/
    private String name;
    /** The value (as a string) of this parameter*/
    private String value;
    /** The 'type' of this parameter. Can be 'number' or 'string'*/
    private String type;


    /**
     * Default constructor
     */
    public VglParameter() {
        this(null, null, null);
    }

    /**
     * Default constructor
     */
    public VglParameter(Integer id, Integer jobId, String name) {
        this(id, jobId, name, null, null);
    }

    /**
     * Construct a fully populated instance
     */
    public VglParameter(Integer id, Integer jobId, String name, String value, String type) {
        super();
        this.id = id;
        this.jobId = jobId;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    /**
     * The primary key for this parameter
     * @return
     */
    public Integer getId() {
        return id;
    }

    /**
     * The primary key for this parameter
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * The id of the job that owns this parameter
     * @return
     */
    public Integer getJobId() {
        return jobId;
    }

    /**
     * The id of the job that owns this parameter
     * @param jobId
     */
    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    /**
     * The name of this parameter
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The name of this parameter
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value (as a string) of this parameter
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * The value (as a string) of this parameter
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The 'type' of this parameter. Can be 'number' or 'string'
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * The 'type' of this parameter. Can be 'number' or 'string'
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Tests two VglJobParameter objects for equality based on job id and name
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof VglParameter) {
            return this.jobId.equals(((VglParameter) o).jobId) && this.name.equals(((VglParameter) o).name);
        }

        return false;
    }

    /**
     * Gets a hashcode based of the job id and name parameters;
     */
    @Override
    public int hashCode() {
        return name.hashCode() ^ jobId.hashCode();
    }
}
