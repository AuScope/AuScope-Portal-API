package org.auscope.portal.server.vegl;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Simple class that stores information about a job series consisting of
 * one or more jobs.
 * 
 * Developed from the original GeodesySeries
 *
 * @author Cihan Altinay
 * @Author Josh Vote
 */
@Entity
@Table(name = "series")
public class VEGLSeries implements Serializable {
	
	private static final long serialVersionUID = -4483263063748119882L;
	
	/** A unique identifier for this series */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /** The user owning this series */
	//@OneToMany(mappedBy = "seriesId", fetch=FetchType.EAGER, orphanRemoval = true)
    private String user;
    /** A short name for this series */
    private String name;
    /** A description of this series */
    private String description;

    /**
     * Default constructor.
     */
    public VEGLSeries() {
        user = name = description = "";
    }

    /**
     * Returns the unique identifier of this series.
     *
     * @return The unique ID of this series.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this series.
     *
     * @param id The new ID for this series.
     */
    protected void setId(Integer id) {
        assert (id != null);
        this.id = id;
    }

    /**
     * Returns the description of this series.
     *
     * @return The description of this series.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this series.
     *
     * @param description The description of this series.
     */
    public void setDescription(String description) {
        assert (description != null);
        this.description = description;
    }

    /**
     * Returns the user owning this series.
     *
     * @return The user owning this series.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user owning this series.
     *
     * @param user The user owning this series.
     */
    public void setUser(String user) {
        assert (user != null);
        this.user = user;
    }

    /**
     * Returns the name of this series.
     *
     * @return The name of this series.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this series.
     *
     * @param name The name of this series.
     */
    public void setName(String name) {
        assert (name != null);
        this.name = name;
    }
    
    /**
     * Returns a String representing the state of this <code>GeodesySeries</code>
     * object.
     *
     * @return A summary of the values of this object's fields
     */
    @Override
    public String toString() {
        return super.toString() +
               ",id=" + id +
               ",user=\"" + user + "\"" +
               ",name=\"" + name + "\"" +
               ",description=\"" + description + "\"";
    }
}