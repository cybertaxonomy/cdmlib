/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

/**
 * @author k.luther
 * @since 21.06.2018
 *
 */
public class GatheringEventDTO {

    private String locality;
    private Point exactLocation;
    private String country;
    private Set<TermDto> collectingAreas;
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
    public GatheringEventDTO(String locality, Point exactLocation, String country, Set<TermDto> collectingAreas,
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

    /**
     *
     */
    public GatheringEventDTO() {

    }

    public static GatheringEventDTO newInstance(GatheringEvent gathering){
        GatheringEventDTO dto = new GatheringEventDTO();
        if (gathering.getLocality() != null){
            dto.locality = gathering.getLocality().getLanguageLabel(Language.DEFAULT());
            }
        if (gathering.getExactLocation() != null){
            dto.exactLocation = gathering.getExactLocation();
        }
        if (gathering.getCountry() != null){
            dto.country =  gathering.getCountry().getTitleCache();
        }
        if (gathering.getCollectingMethod() != null){
            dto.collectingMethod = gathering.getCollectingMethod();
        }
        if (gathering.getCollector() != null ){
            dto.collector = gathering.getCollector().getTitleCache();
        }
        if (gathering.getAbsoluteElevation() != null){
            dto.absoluteElevation = gathering.getAbsoluteElevation();
        }
        if (gathering.getAbsoluteElevationMax() != null){
            dto.absoluteElevationMax = gathering.getAbsoluteElevationMax();
        }
        if (gathering.getAbsoluteElevationText() != null){
            dto.absoluteElevationText = gathering.getAbsoluteElevationText();
        }
        if (gathering.getDistanceToGround() != null){
            dto.distanceToGround = gathering.getDistanceToGround();
        }
        if (gathering.getDistanceToGroundMax() != null){
            dto.distanceToGroundMax = gathering.getDistanceToGroundMax();
        }
        if (gathering.getDistanceToGroundText() != null){
            dto.distanceToGroundText = gathering.getDistanceToGroundText();
        }
        if (gathering.getDistanceToWaterSurface() != null){
            dto.distanceToWaterSurface= gathering.getDistanceToWaterSurface();
        }
        if (gathering.getDistanceToWaterSurfaceMax() != null){
            dto.distanceToWaterSurfaceMax= gathering.getDistanceToWaterSurfaceMax();
        }
        if (gathering.getDistanceToWaterSurfaceText() != null){
            dto.distanceToWaterSurfaceText= gathering.getDistanceToWaterSurfaceText();
        }

        for (NamedArea area: gathering.getCollectingAreas()){
            TermDto areaDto = TermDto.fromNamedArea(area);
            if (dto.getCollectingAreas() == null){
                dto.collectingAreas = new HashSet<>();
            }
            dto.collectingAreas.add(areaDto);
        }

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
    public Set<TermDto> getCollectingAreas() {
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
