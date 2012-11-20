package org.auscope.portal.server.vegl;

import java.io.Serializable;

public class VGLSignature implements Serializable {
    private static final long serialVersionUID = 4558063081881913834L;

    private Integer id;
    private String user;
    private String individualName;
    private String organisationName;
    private String positionName;
    private String telephone;
    private String facsimile;
    private String deliveryPoint;
    private String city;
    private String administrativeArea;
    private String postalCode;
    private String country;
    private String onlineContactName;
    private String onlineContactDescription;
    private String onlineContactURL;
    private String keywords;
    private String constraints;

    public VGLSignature() {
    }

    public VGLSignature(Integer id, String user) {
        this.id = id;
        this.user = user;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the individualName
     */
    public String getIndividualName() {
        return individualName;
    }

    /**
     * @param individualName the individualName to set
     */
    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    /**
     * @return the organisationName
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * @param organisationName the organisationName to set
     */
    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    /**
     * @return the positionName
     */
    public String getPositionName() {
        return positionName;
    }

    /**
     * @param positionName the positionName to set
     */
    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    /**
     * @return the telephone
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * @param telephone the telephone to set
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * @return the facsimile
     */
    public String getFacsimile() {
        return facsimile;
    }

    /**
     * @param facsimile the facsimile to set
     */
    public void setFacsimile(String facsimile) {
        this.facsimile = facsimile;
    }

    /**
     * @return the deliveryPoint
     */
    public String getDeliveryPoint() {
        return deliveryPoint;
    }

    /**
     * @param deliveryPoint the deliveryPoint to set
     */
    public void setDeliveryPoint(String deliveryPoint) {
        this.deliveryPoint = deliveryPoint;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the administrativeArea
     */
    public String getAdministrativeArea() {
        return administrativeArea;
    }

    /**
     * @param administrativeArea the administrativeArea to set
     */
    public void setAdministrativeArea(String administrativeArea) {
        this.administrativeArea = administrativeArea;
    }

    /**
     * @return the postalCode
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * @param postalCode the postalCode to set
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the onlineContactName
     */
    public String getOnlineContactName() {
        return onlineContactName;
    }

    /**
     * @param onlineContactName the onlineContactName to set
     */
    public void setOnlineContactName(String onlineContactName) {
        this.onlineContactName = onlineContactName;
    }

    /**
     * @return the onlineContactDescription
     */
    public String getOnlineContactDescription() {
        return onlineContactDescription;
    }

    /**
     * @param onlineContactDescription the onlineContactDescription to set
     */
    public void setOnlineContactDescription(String onlineContactDescription) {
        this.onlineContactDescription = onlineContactDescription;
    }

    /**
     * @return the onlineContactURL
     */
    public String getOnlineContactURL() {
        return onlineContactURL;
    }

    /**
     * @param onlineContactURL the onlineContactURL to set
     */
    public void setOnlineContactURL(String onlineContactURL) {
        this.onlineContactURL = onlineContactURL;
    }

    /**
     * @return the keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * @return the constraints
     */
    public String getConstraints() {
        return constraints;
    }

    /**
     * @param constraints the constraints to set
     */
    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    /**
     * Tests two VglSignature objects for equality based on signature id and name
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VGLSignature)) {
            return false;
        }
        VGLSignature other = (VGLSignature) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }    
}