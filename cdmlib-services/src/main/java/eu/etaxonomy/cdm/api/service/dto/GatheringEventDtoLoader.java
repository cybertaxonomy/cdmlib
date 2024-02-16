/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.api.dto.GatheringEventDTO;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

/**
 * Loader for {@link GatheringEventDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 14.02.2024
 */
public class GatheringEventDtoLoader {

    public static GatheringEventDTO fromValues(String locality, Point exactLocation, String country, Set<String> collectingAreas,
            String collectingMethod, String collector, Integer absoluteElevation, Integer absoluteElevationMax,
            String absoluteElevationText, Double distanceToGround, Double distanceToGroundMax,
            String distanceToGroundText, Double distanceToWaterSurface, Double distanceToWaterSurfaceMax,
            String distanceToWaterSurfaceText) {

        GatheringEventDTO dto = new GatheringEventDTO();
        dto.setLocality(locality);

        dto.setExactLocation(PointDtoLoader.fromEntity(exactLocation));
        dto.setCountry(country);

        dto.setCollectingAreas(collectingAreas);
        dto.setCollectingMethod(collectingMethod);
        dto.setCollector(collector);
        dto.setAbsoluteElevation(absoluteElevation);
        dto.setAbsoluteElevationMax(absoluteElevationMax);
        dto.setAbsoluteElevationText(absoluteElevationText);
        dto.setDistanceToGround(distanceToGround);
        dto.setDistanceToGroundMax(distanceToGroundMax);
        dto.setDistanceToGroundText(distanceToGroundText);
        dto.setDistanceToWaterSurface(distanceToWaterSurface);
        dto.setDistanceToWaterSurfaceMax(distanceToWaterSurfaceMax);
        dto.setDistanceToWaterSurfaceText(distanceToWaterSurfaceText);
        return dto;
    }

    public static GatheringEventDTO fromEntity(GatheringEvent gathering){
        GatheringEventDTO dto = new GatheringEventDTO();
        if (gathering.getLocality() != null){
            LanguageString locality = gathering.getLocality();
            dto.setLocality(locality.getText());
        }
        dto.setExactLocation(PointDtoLoader.fromEntity(gathering.getExactLocation()));
        if (gathering.getCountry() != null){
            //TODO i18n
            dto.setCountry(gathering.getCountry().getTitleCache());
        }
        dto.setCollectingMethod(gathering.getCollectingMethod());
        if (gathering.getCollector() != null ){
            dto.setCollector(gathering.getCollector().getTitleCache());
        }
        dto.setAbsoluteElevation(gathering.getAbsoluteElevation());
        dto.setAbsoluteElevationMax(gathering.getAbsoluteElevationMax());
        dto.setAbsoluteElevationText(gathering.getAbsoluteElevationText());
        dto.setDistanceToGround(gathering.getDistanceToGround());
        dto.setDistanceToGroundMax(gathering.getDistanceToGroundMax());
        dto.setDistanceToGroundText(gathering.getDistanceToGroundText());
        dto.setDistanceToWaterSurface(gathering.getDistanceToWaterSurface());
        dto.setDistanceToWaterSurfaceMax(gathering.getDistanceToWaterSurfaceMax());
        dto.setDistanceToWaterSurfaceText(gathering.getDistanceToWaterSurfaceText());

        //TODO why only start date
        dto.setDate(gathering.getGatheringDate());

        for (NamedArea area: gathering.getCollectingAreas()){
            String areaString = area.getLabel();
            if (dto.getCollectingAreas() == null){
                dto.setCollectingAreas(new HashSet<>());
            }
            dto.addCollectionArea(areaString);
        }

        return dto;
    }

}
