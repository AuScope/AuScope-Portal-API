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
public class VglParameter implements Serializable, Cloneable {

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
    /** The name of this parameter*/
    private String name;
    /** The value (as a string) of this parameter*/
    private String value;
    /** The 'type' of this parameter. Can be 'number' or 'string'*/
    private String type;
    /** The job that owns this parameter*/
    private VEGLJob parent;


    /**
     * Default constructor
     */
    public VglParameter() {
        this(null, null);
    }

    /**
     * Default constructor
     */
    public VglParameter(Integer id, String name) {
        this(id, name, null, null);
    }

    /**
     * Construct a fully populated instance
     */
    public VglParameter(Integer id, String name, String value, String type) {
        this(id, name, value, type, null);
    }

    /**
     * Construct a fully populated instance
     */
    public VglParameter(Integer id, String name, String value, String type, VEGLJob parent) {
        super();
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
        this.parent = parent;
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
     * The job that owns this parameter
     * @return
     */
    public VEGLJob getParent() {
        return parent;
    }

    /**
     * The job that owns this parameter
     * @param parent
     */
    public void setParent(VEGLJob parent) {
        this.parent = parent;
    }

    /**
     * Tests two VglJobParameter objects for equality based on job id and name
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof VglParameter) {
            return this.parent.getId().equals(((VglParameter) o).parent.getId()) && this.name.equals(((VglParameter) o).name);
        }

        return false;
    }

    /**
     * Gets a hashcode based of the job id and name parameters;
     */
    @Override
    public int hashCode() {
        return name.hashCode() ^ parent.getId().hashCode();
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
