/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class PointDTO {

    private Double longitude;
    private String longitudeSexagesimal;

    private Double latitude;
    private String latitudeSexagesimal;

    private Integer errorRadius;

    private String referenceSystemAbbrev;

    private String referenceSystemLabel;

    private String label;


    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getErrorRadius() {
        return errorRadius;
    }
    public void setErrorRadius(Integer errorRadius) {
        this.errorRadius = errorRadius;
    }

    public String getReferenceSystemAbbrev() {
        return referenceSystemAbbrev;
    }
    public void setReferenceSystemAbbrev(String referenceSystemAbbrev) {
        this.referenceSystemAbbrev = referenceSystemAbbrev;
    }

    public String getReferenceSystemLabel() {
        return referenceSystemLabel;
    }
    public void setReferenceSystemLabel(String referenceSystemLabel) {
        this.referenceSystemLabel = referenceSystemLabel;
    }

    public String getLongitudeSexagesimal() {
        return longitudeSexagesimal;
    }
    public void setLongitudeSexagesimal(String longitudeSexagesimal) {
        this.longitudeSexagesimal = longitudeSexagesimal;
    }

    public String getLatitudeSexagesimal() {
        return latitudeSexagesimal;
    }
    public void setLatitudeSexagesimal(String latitudeSexagesimal) {
        this.latitudeSexagesimal = latitudeSexagesimal;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
}
