/**
 * Copyright (C) 2020 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.ext.geo.kml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.geotools.feature.SchemaException;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermType;
import si.uom.SI;
import tec.uom.se.quantity.Quantities;

/**
 * 
 * @author Andreas Kohlbecker
 * @since Apr 21, 2020
 */
public class KMLDocumentBuilder {

	private final static Logger logger = Logger.getLogger(KMLDocumentBuilder.class);

	private Set<SpecimenOrObservationBase> occSet = new HashSet<>();
	
	private Map<FieldUnit, Set<SpecimenOrObservationBase>> fieldUnitMap = new HashMap<>();
	private Map<FieldUnit, Set<SpecimenOrObservationType>> fieldUnitRecordBases = new HashMap<>();

	private Map<String, Style> styles = new HashMap<>();

	private static final DefinedTerm KIND_OF_UNIT_UNSET = DefinedTerm.NewInstance(TermType.KindOfUnit);

	public KMLDocumentBuilder addSpecimenOrObservationBase(SpecimenOrObservationBase occurrence) {
		occSet.add(occurrence);
		return this;
	}

	public Kml build() {

		Kml kml = KmlFactory.createKml();
		List<Feature> documentFeatures = new ArrayList<>();

		for (SpecimenOrObservationBase sob : occSet) {
			mapFieldUnit(sob, null, null);
		}
		for (FieldUnit fu : fieldUnitMap.keySet()) {
			createFieldUnitPlacemarks(documentFeatures, fu);
		}

		Document doc = kml.createAndSetDocument();
		doc.getFeature().addAll(documentFeatures);
		doc.getStyleSelector().addAll(styles.values());
		return kml;
	}
	
	/**
	 * @param derivedUnit
	 * @param documentFeatures
	 * @param kindOfUnit
	 */
	/*
	private void walkDerivationPath(DerivedUnit derivedUnit, DerivedUnit unitOfInterest, List<Feature> documentFeatures,
			DefinedTerm kindOfUnit) {

		if (kindOfUnit == null) {
			if (derivedUnit.getKindOfUnit() != null) {
				kindOfUnit = derivedUnit.getKindOfUnit();
			} else {
				kindOfUnit = KIND_OF_UNIT_UNSET;
			}
		}

		Set<SpecimenOrObservationBase> originals = derivedUnit.getOriginals();
		if (originals != null) {
			for (SpecimenOrObservationBase<?> original : originals) {
				if (original instanceof FieldUnit) {
					GatheringEvent gatherEvent = ((FieldUnit) original).getGatheringEvent();
					if (gatherEvent != null && isValidPoint(gatherEvent.getExactLocation())) {
						createFieldUnitPlacemarks(documentFeatures, unitOfInterest, gatherEvent, LocationType.DERIVED_UNIT, kindOfUnit);
					}
				} else {
					walkDerivationPath((DerivedUnit) original, unitOfInterest, documentFeatures, kindOfUnit);
				}
			}
		}
	}
	*/
	
	private void mapFieldUnit(SpecimenOrObservationBase unitOfInterest, SpecimenOrObservationBase original, Set<SpecimenOrObservationType> recordBases) {
		
		if(original == null) {
			original = unitOfInterest;
		}
		if(recordBases == null) {
			recordBases = new HashSet<>();
		}
		
		if (original instanceof FieldUnit) {
			FieldUnit fu = (FieldUnit)original;
			if(!fieldUnitMap.containsKey(fu)) {
				fieldUnitMap.put(fu, new HashSet<>());
			}
			fieldUnitMap.get(fu).add(unitOfInterest);
			if(!fieldUnitRecordBases.containsKey(fu)) {
				fieldUnitRecordBases.put(fu, new HashSet<>());
			}
			fieldUnitRecordBases.get(fu).addAll(recordBases);
		} else if (original instanceof DerivedUnit) {
			Set<SpecimenOrObservationBase> originals = ((DerivedUnit)original).getOriginals();
			if (originals != null) {
				for (SpecimenOrObservationBase parentOriginal : originals) {
					mapFieldUnit(original, parentOriginal, recordBases);
				}
			}
		}
	}

	/**
	 * @param documentFeatures
	 * @param fieldUnit
	 * @param gatherEvent
	 */
	private void createFieldUnitPlacemarks(List<Feature> documentFeatures, FieldUnit fieldUnit) {
		
		GatheringEvent gatherEvent = fieldUnit.getGatheringEvent();
		if (gatherEvent != null && isValidPoint(gatherEvent.getExactLocation())) {
			Placemark mapMarker = fieldUnitLocationMarker(gatherEvent.getExactLocation(),
					gatherEvent.getAbsoluteElevation(), fieldUnitRecordBases.get(fieldUnit));
			documentFeatures.add(mapMarker);
			addExtendedData(mapMarker, fieldUnit, gatherEvent);
			errorRadiusPlacemark(gatherEvent.getExactLocation()).ifPresent(pm -> documentFeatures.add(pm));
		}
	}


	/**
	 * @param exactLocation
	 * @param altitude
	 * @param locationType
	 * @param recordBases
	 * @return
	 */
	private Placemark fieldUnitLocationMarker(Point exactLocation, Integer altitude, Set<SpecimenOrObservationType> recordBases) {

		Placemark mapMarker = KmlFactory.createPlacemark();
		de.micromata.opengis.kml.v_2_2_0.Point point = KmlFactory.createPoint();
		point.setAltitudeMode(AltitudeMode.ABSOLUTE);
		if (altitude != null) {
			point.setCoordinates(Arrays.asList(KmlFactory.createCoordinate(exactLocation.getLongitude(),
					exactLocation.getLatitude(), altitude.doubleValue())));
		} else {
			point.setCoordinates(Arrays
					.asList(KmlFactory.createCoordinate(exactLocation.getLongitude(), exactLocation.getLatitude())));
		}
		mapMarker.setGeometry(point);
		mapMarker.setStyleUrl(styleURL(recordBases));

		return mapMarker;
	}
	
	private Optional<Placemark> errorRadiusPlacemark(Point exactLocation) {

		// exactLocation.setErrorRadius(25 * 1000); // METER // uncomment for debugging
		
		Placemark errorRadiusCicle = null;
		if (exactLocation.getErrorRadius() != null && exactLocation.getErrorRadius() > 0) {
			errorRadiusCicle = KmlFactory.createPlacemark();
			LinearRing cirle = createKMLCircle(exactLocation.getLongitude(), exactLocation.getLatitude(),
					exactLocation.getErrorRadius());
			Polygon polygon = errorRadiusCicle.createAndSetPolygon();
			polygon.createAndSetOuterBoundaryIs().setLinearRing(cirle);
			polygon.setExtrude(true);
			errorRadiusCicle.setStyleUrl(errorRadiusStyleURL());
		}

		return Optional.ofNullable(errorRadiusCicle);
	}
	
	
	/**
	 * @param longitude
	 * @param latitude
	 * @param errorRadiusMeter
	 * @return
	 */
	private LinearRing createKMLCircle(Double longitude, Double latitude, Integer errorRadiusMeter) {

		GeometryBuilder.CircleMethod method = GeometryBuilder.CircleMethod.reprojectedCircle;
		LinearRing lineString = KmlFactory.createLinearRing();
		lineString.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);

		org.locationtech.jts.geom.Geometry polygonGeom = null;
		GeometryBuilder gb = new GeometryBuilder();
		try {
			switch (method) {
			case simpleCircle:
				// this is the best method so far !!!!
				polygonGeom = gb.simpleCircle(Quantities.getQuantity(errorRadiusMeter.doubleValue(), SI.METRE),
						latitude, longitude);
				break;
			case simpleCircleSmall:
				// Only suitable for small radius (> 1000 m) as the circles are heavily distorted
				// otherwise.
				polygonGeom = gb.simpleCircleSmall(errorRadiusMeter.doubleValue(), latitude, longitude);
				break;
			case circle:
				// fails
				polygonGeom = gb.circle(errorRadiusMeter.doubleValue(), latitude, longitude);
				break;
			case reprojectedCircle:
				// incomplete
				SimpleFeature pointFeature = gb.createSimplePointFeature(longitude, latitude);
				polygonGeom = gb.bufferFeature(pointFeature, errorRadiusMeter.doubleValue());
				break;
			}
			for (Coordinate coordinate : polygonGeom.getCoordinates()) {
				lineString.addToCoordinates(coordinate.getX(), coordinate.getY());
			}
		} catch (FactoryException e) {
			logger.error("Polygon creation for error radius failed", e);
		} catch (SchemaException e) {
			logger.error("SimplePointFeature creation failed", e);
		}
		return lineString;
	}
	
	private void addExtendedData(Placemark mapMarker, FieldUnit fieldUnit, GatheringEvent gatherEvent) {
		
		String name = fieldUnit.toString();
		String titleCache = fieldUnit.getTitleCache();
		String locationString = null;
		if(gatherEvent.getExactLocation() != null) {
			locationString = gatherEvent.getExactLocation().toSexagesimalString(false,  true);
			titleCache = titleCache.replace(locationString + ", ", "");
		}
		String description = "<p class=\"title-cache\">" + titleCache;
		if(locationString != null) {
			// see https://www.mediawiki.org/wiki/GeoHack
			description += "<br/><a class=\"exact-location\" target=\"geohack\" href=\"https://tools.wmflabs.org/geohack/en/" + 
					gatherEvent.getExactLocation().getLatitude() + ";" + gatherEvent.getExactLocation().getLongitude() +"?pagename=" + name + "\">" + locationString + "</a>";
		}
		description += "</p>";
		description += "<figure><figcaption>Specimens and observations:</figcaption><ul>";
		for(SpecimenOrObservationBase sob : fieldUnitMap.get(fieldUnit)) {
			SpecimenOrObservationType type = sob.getRecordBasis() != null ? sob.getRecordBasis() : SpecimenOrObservationType.Unknown;
			description += "<li><a class=\"occurrence-link occurrence-link-" + sob.getUuid() + " \" href=\"${occurrence-link-base-url}/" + sob.getUuid() + "\">" + type.name() + "</a></li>";
		}	
		description += "</ul></figure>";
		// mapMarker.setName(name);
		mapMarker.setDescription(description);
		
		ExtendedData extendedData = mapMarker.createAndSetExtendedData();
		extendedData.createAndAddData(fieldUnit.getTitleCache()).setName("titleCache");
		if(mapMarker.getGeometry() != null && mapMarker.getGeometry() instanceof de.micromata.opengis.kml.v_2_2_0.Point) {
			extendedData.createAndAddData(((de.micromata.opengis.kml.v_2_2_0.Point)mapMarker.getGeometry()).getCoordinates().toString()).setName("Location");
		}
		
	}


	private String styleURL(Set<SpecimenOrObservationType> recordBases) {
		String key = "DEFAULT";
		// TODO determine key and style on base of the recordBases
		if (!styles.containsKey(key)) {
			Style style = KmlFactory.createStyle().withIconStyle(MapMarkerIcons.red_blank.asIconStyle());
			style.setId(key);
			styles.put(key, style);
		}
		return "#" + key;
	}

	private String errorRadiusStyleURL() {
		String key = "ERROR_RADIUS";
		if (!styles.containsKey(key)) {
			Style style = KmlFactory.createStyle();
			PolyStyle polyStyle = style.createAndSetPolyStyle();
			polyStyle.setColor("100000ee"); // aabbggrr, where aa=alpha (00 to ff); bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
			polyStyle.setFill(true);
			polyStyle.setOutline(true);
			style.setId(key);
			LineStyle lineStyle = style.createAndSetLineStyle();
			// lineStyle.setColor("ff880088"); // aabbggrr, where aa=alpha (00 to ff);
			// bb=blue (00 to ff); gg=green (00 to ff); rr=red (00 to ff).
			lineStyle.setWidth(1);
			styles.put(key, style);
		}
		return "#" + key;
	}


	// TODO use also for .EditGeoService.registerDerivedUnitLocations(DerivedUnit
	// derivedUnit, List<Point> derivedUnitPoints) !!!!!!!!!
	public boolean isValidPoint(Point point) {

		if (point == null) {
			return false;
		}
		// points with no longitude or latitude should not exist
		// see #4173 ([Rule] Longitude and Latitude in Point must not be null)
		if (point.getLatitude() == null || point.getLongitude() == null) {
			return false;
		}
		// FIXME: remove next statement after
		// DerivedUnitFacade or ABCD import is fixed
		//
		if (point.getLatitude() == 0.0 || point.getLongitude() == 0.0) {
			return false;
		}
		return true;
	}
}
