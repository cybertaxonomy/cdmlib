/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.geo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import eu.etaxonomy.cdm.ext.geo.kml.KMLDocumentBuilder;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;

/**
 * @author a.kohlbecker
 * @since 18.06.2009
 */
@Service
@Transactional(readOnly = true)
public class EditGeoService implements IEditGeoService {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private IOccurrenceDao occurrenceDao;

    @Override
    public OccurrenceServiceRequestParameterDto getOccurrenceServiceRequestParameters(
            List<SpecimenOrObservationBase> specimensOrObservations,
            Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors) {

        List<Point> fieldUnitPoints = new ArrayList<>();
        List<Point> derivedUnitPoints = new ArrayList<>();
        List<String> propertyPath = Arrays.asList(new String []{
                "derivedFrom.originals.gatheringEvent.exactLocation.referenceSystem.includes.$",
                "gatheringEvent.exactLocation.referenceSystem.includes.$"
        });

        for (SpecimenOrObservationBase<?> specimenOrObservationBase : specimensOrObservations) {
            SpecimenOrObservationBase<?> specimensOrObservation = occurrenceDao
                    .load(specimenOrObservationBase.getUuid(), propertyPath);

            if (specimensOrObservation instanceof FieldUnit) {
                GatheringEvent gatherEvent = ((FieldUnit) specimensOrObservation).getGatheringEvent();
                if (gatherEvent != null && gatherEvent.getExactLocation() != null){
                    fieldUnitPoints.add(gatherEvent.getExactLocation());
                }
            }
            if (specimensOrObservation instanceof DerivedUnit) {
                registerDerivedUnitLocations((DerivedUnit) specimensOrObservation, derivedUnitPoints);
            }
        }

        return EditGeoServiceUtilities.getOccurrenceServiceRequestParameterString(fieldUnitPoints,
                derivedUnitPoints, specimenOrObservationTypeColors);
    }

    @Override
    public Kml occurrencesToKML(
            @SuppressWarnings("rawtypes") List<SpecimenOrObservationBase> specimensOrObservations,
            Map<SpecimenOrObservationType, Color> specimenOrObservationTypeColors) {

    		KMLDocumentBuilder builder = new KMLDocumentBuilder();

    		for (SpecimenOrObservationBase<?> specimenOrObservationBase : specimensOrObservations) {
    			builder.addSpecimenOrObservationBase(occurrenceDao.load(specimenOrObservationBase.getUuid()));
    		}

    		Kml kml = builder.build();

    		return kml;
    }


    private void registerDerivedUnitLocations(DerivedUnit derivedUnit, List<Point> derivedUnitPoints) {

        @SuppressWarnings("rawtypes")
        Set<SpecimenOrObservationBase> originals = derivedUnit.getOriginals();
        for (SpecimenOrObservationBase<?> original : originals) {
            if (original instanceof FieldUnit) {
                if (((FieldUnit) original).getGatheringEvent() != null) {
                    Point point = ((FieldUnit) original).getGatheringEvent().getExactLocation();
                    if (point != null) {
                        // points with no longitude or latitude should not exist
                        // see  #4173 ([Rule] Longitude and Latitude in Point must not be null)
                        if (point.getLatitude() == null || point.getLongitude() == null){
                            continue;
                        }
                        // FIXME: remove next statement after
                        // DerivedUnitFacade or ABCD import is fixed
                        //
                        if(point.getLatitude() == 0.0 || point.getLongitude() == 0.0) {
                            continue;
                        }
                        derivedUnitPoints.add(point);
                    }
                }
            } else {
                registerDerivedUnitLocations((DerivedUnit) original, derivedUnitPoints);
            }
        }
    }

}
