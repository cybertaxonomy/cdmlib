/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.Partial;

/**
 * @author k.luther
 * @since 21.06.2018
 */
public class GatheringEventDTO implements Serializable{

    private static final long serialVersionUID = -4381193272881277448L;

    private String locality;
    private PointDTO exactLocation;
    private String country;
    private Set<String> collectingAreas = new HashSet<>();
    private String collectingMethod;
    private Integer absoluteElevation;
    private Integer absoluteElevationMax;
    private String absoluteElevationText;
    private Double distanceToGround;
    private Double distanceToGroundMax;
    private String distanceToGroundText;
    private Double distanceToWaterSurface;
    private Double distanceToWaterSurfaceMax;
    private String distanceToWaterSurfaceText;
    private String collector;
    private Partial date;


    public String getLocality() {
        return locality;
    }
    public void setLocality(String locality) {
        this.locality = locality;
    }

    public PointDTO getExactLocation() {
        return exactLocation;
    }
    public void setExactLocation(PointDTO exactLocation) {
        this.exactLocation = exactLocation;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public Set<String> getCollectingAreas() {
        return collectingAreas;
    }
    public void addCollectionArea(String areaString) {
        this.collectingAreas.add(areaString);
    }
    public void setCollectingAreas(Set<String> collectingAreas) {
        this.collectingAreas = collectingAreas;
    }


    public String getCollectingMethod() {
        return collectingMethod;
    }
    public void setCollectingMethod(String collectingMethod) {
        this.collectingMethod = collectingMethod;
    }

    public Integer getAbsoluteElevation() {
        return absoluteElevation;
    }
    public void setAbsoluteElevation(Integer absoluteElevation) {
        this.absoluteElevation = absoluteElevation;
    }

    public Integer getAbsoluteElevationMax() {
        return absoluteElevationMax;
    }
    public void setAbsoluteElevationMax(Integer absoluteElevationMax) {
        this.absoluteElevationMax = absoluteElevationMax;
    }

    public String getAbsoluteElevationText() {
        return absoluteElevationText;
    }
    public void setAbsoluteElevationText(String absoluteElevationText) {
        this.absoluteElevationText = absoluteElevationText;
    }


    public Double getDistanceToGround() {
        return distanceToGround;
    }
    public void setDistanceToGround(Double distanceToGround) {
        this.distanceToGround = distanceToGround;
    }

    public Double getDistanceToGroundMax() {
        return distanceToGroundMax;
    }
    public void setDistanceToGroundMax(Double distanceToGroundMax) {
        this.distanceToGroundMax = distanceToGroundMax;
    }

    public String getDistanceToGroundText() {
        return distanceToGroundText;
    }
    public void setDistanceToGroundText(String distanceToGroundText) {
        this.distanceToGroundText = distanceToGroundText;
    }


    public Double getDistanceToWaterSurface() {
        return distanceToWaterSurface;
    }
    public void setDistanceToWaterSurface(Double distanceToWaterSurface) {
        this.distanceToWaterSurface = distanceToWaterSurface;
    }

    public Double getDistanceToWaterSurfaceMax() {
        return distanceToWaterSurfaceMax;
    }
    public void setDistanceToWaterSurfaceMax(Double distanceToWaterSurfaceMax) {
        this.distanceToWaterSurfaceMax = distanceToWaterSurfaceMax;
    }

    public String getDistanceToWaterSurfaceText() {
        return distanceToWaterSurfaceText;
    }
    public void setDistanceToWaterSurfaceText(String distanceToWaterSurfaceText) {
        this.distanceToWaterSurfaceText = distanceToWaterSurfaceText;
    }

    public String getCollector() {
        return collector;
    }
    public void setCollector(String collector) {
        this.collector = collector;
    }

    public Partial getDate() {
        return date;
    }
    public void setDate(Partial date) {
        this.date = date;
    }
}