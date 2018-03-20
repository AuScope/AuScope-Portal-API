package org.auscope.portal.server.web.service.scm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Entry {
    private String id;
    private String name;
    private String description;
    private Date createdAt;

    // Flag to indicate we've been instantiated with a URI reference only, so we
    // need to load our details from the server if properties are missing.
    @JsonIgnore private boolean isReferenceOnly = false;

    final Log logger = LogFactory.getLog(getClass());

    @JsonCreator
    public Entry(String id) {
        this();
        this.id = id;
        isReferenceOnly = true;
    }

    public Entry() {}

    @JsonProperty("@id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUri() { return id; }
    public void setUri(String uri) { this.id = uri; }

    @JsonProperty("created_at")
    public Date getCreatedAt() {
        if (isReferenceOnly) {
            loadMissingProperties();
        }
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // N.B. While only some Entries will have dependencies, we'll include them
    // here to save having to create an abstract base class for "entries that
    // can have dependencies".
    private List<Dependency> dependencies;

    /**
     * Return the list of dependencies for this Entry.
     *
     * @return List<Dependency> dependencies for this Entry.
     */
    public List<Dependency> getDependencies() {
        if (isReferenceOnly) {
            loadMissingProperties();
        }

        return this.dependencies;
    }

    // TODO: Map dependencies to 'dependencies' field in the json once it's
    // migrated in the SSSC.

    /**
     * Set the list of dependencies for this Entry.
     *
     * @param dependencies List<Dependency> new dependencies for this Entry.
     */
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;

        if (dependencies == null) {
            this.dependencies = new ArrayList<Dependency>();
        }
    }

    // Fallback to the old style "deps" property name for backwards compatibility
    public List<Dependency> getDeps() {
        return this.getDependencies();
    }
    
    public void setDeps(List<Dependency> dependencies) {
        this.setDependencies(dependencies);
    }

    /**
     * @return the name
     */
    public String getName() {
        if (isReferenceOnly) {
            loadMissingProperties();
        }
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        if (isReferenceOnly) {
            loadMissingProperties();
        }
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Resolve our URI and use resulting description to fill in missing property values.
     *
     */
    public void loadMissingProperties() {
        Entry description = ScmLoaderFactory.getInstance().loadEntry(id, getClass());

        try {
            copyMissingProperties(description);
            isReferenceOnly = false;
        }
        catch (PortalServiceException ex) {
            logger.error("Failed to load missing properties.", ex);
        }
    }

    /**
     * Copy missing property values from entry.
     *
     * @param entry Entry to copy values from
     */
    public void copyMissingProperties(Entry entry) throws PortalServiceException {
        if (!entry.getClass().isInstance(this)) {
            throw new PortalServiceException("Incompatible type passed to %s.copyMissingProperties(%s)"
                                             .format(getClass().getName(),
                                                     entry == null ? null : entry.getClass().getName()));
        }

        if (name == null) {
            setName(entry.getName());
        }

        if (description == null) {
            setDescription(entry.getDescription());
        }

        if (createdAt == null) {
            setCreatedAt(entry.getCreatedAt());
        }

        if (dependencies == null) {
            setDependencies(entry.getDependencies());
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;

        if (that == null || this.getClass() != that.getClass()) return false;

        return this.id.equals(((Entry)that).getId());
    }
}
