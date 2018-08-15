/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

/**
 * @author k.luther
 * @since 21.06.2018
 *
 */
public class GatheringEventDTO {

    private String locality;
    private Point exactLocation;
    private String country;
    private Set<String> collectingAreas;
    private String collectingMethod;
    private Integer absoluteElevation;
    private Integer absoluteElevationMax;
    private String absoluteElevationText;
    private Double distanceToGround;
    private Double distanceToGroundMax;
    private String distanceToGroundText;
    private Double distanceToWaterSurface;
    private Double distanceToWaterSurfaceMax;
    private String collector;

    /**
     * @param locality
     * @param exactLocation
     * @param country
     * @param collectingAreas
     * @param collectingMethod
     * @param absoluteElevation
     * @param absoluteElevationMax
     * @param absoluteElevationText
     * @param distanceToGround
     * @param distanceToGroundMax
     * @param distanceToGroundText
     * @param distanceToWaterSurface
     * @param distanceToWaterSurfaceMax
     * @param distanceToWaterSurfaceText
     */
    public GatheringEventDTO(String locality, Point exactLocation, String country, Set<String> collectingAreas,
            String collectingMethod, String collector, Integer absoluteElevation, Integer absoluteElevationMax,
            String absoluteElevationText, Double distanceToGround, Double distanceToGroundMax,
            String distanceToGroundText, Double distanceToWaterSurface, Double distanceToWaterSurfaceMax,
            String distanceToWaterSurfaceText) {

        this.locality = locality;
        this.exactLocation = exactLocation;
        this.country = country;
        this.collectingAreas = collectingAreas;
        this.collectingMethod = collectingMethod;
        this.setCollector(collector);
        this.absoluteElevation = absoluteElevation;
        this.absoluteElevationMax = absoluteElevationMax;
        this.absoluteElevationText = absoluteElevationText;
        this.distanceToGround = distanceToGround;
        this.distanceToGroundMax = distanceToGroundMax;
        this.distanceToGroundText = distanceToGroundText;
        this.distanceToWaterSurface = distanceToWaterSurface;
        this.distanceToWaterSurfaceMax = distanceToWaterSurfaceMax;
        this.distanceToWaterSurfaceText = distanceToWaterSurfaceText;
    }

    public static GatheringEventDTO newInstance(GatheringEvent gathering){
        GatheringEventDTO dto = new GatheringEventDTO(gathering.getLocality().getLanguageLabel(Language.DEFAULT()), gathering.getExactLocation(), gathering.getCountry().getTitleCache(),
                null,gathering.getCollectingMethod(), gathering.getCollector() != null ? gathering.getCollector().getTitleCache(): null, gathering.getAbsoluteElevation(), gathering.getAbsoluteElevationMax(), gathering.getAbsoluteElevationText(),
                gathering.getDistanceToGround(), gathering.getDistanceToGroundMax(), gathering.getDistanceToGroundText(), gathering.getDistanceToWaterSurface(), gathering.getDistanceToWaterSurfaceMax(), gathering.getDistanceToWaterSurfaceText());
        return dto;
    }


    public String getLocality() {
        return locality;
    }
    public Point getExactLocation() {
        return exactLocation;
    }
    public String getCountry() {
        return country;
    }
    public Set<String> getCollectingAreas() {
        return collectingAreas;
    }
    public String getCollectingMethod() {
        return collectingMethod;
    }
    public Integer getAbsoluteElevation() {
        return absoluteElevation;
    }
    public Integer getAbsoluteElevationMax() {
        return absoluteElevationMax;
    }
    public String getAbsoluteElevationText() {
        return absoluteElevationText;
    }
    public Double getDistanceToGround() {
        return distanceToGround;
    }
    public Double getDistanceToGroundMax() {
        return distanceToGroundMax;
    }
    public String getDistanceToGroundText() {
        return distanceToGroundText;
    }
    public Double getDistanceToWaterSurface() {
        return distanceToWaterSurface;
    }
    public Double getDistanceToWaterSurfaceMax() {
        return distanceToWaterSurfaceMax;
    }
    public String getDistanceToWaterSurfaceText() {
        return distanceToWaterSurfaceText;
    }
    public String getCollector() {
        return collector;
    }
    public void setCollector(String collector) {
        this.collector = collector;
    }
    private String distanceToWaterSurfaceText;

}
