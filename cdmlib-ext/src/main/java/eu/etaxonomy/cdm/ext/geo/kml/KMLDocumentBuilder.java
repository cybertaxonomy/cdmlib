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
import java.util.Collection;
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
			if (sob instanceof FieldUnit) {
				GatheringEvent gatherEvent = ((FieldUnit) sob).getGatheringEvent();
				if (gatherEvent != null && isValidPoint(gatherEvent.getExactLocation())) {
					createFieldUnitPlacemarks(documentFeatures, sob, gatherEvent, LocationType.FIELD_UNIT, sob.getKindOfUnit());
				}
			}
			if (sob instanceof DerivedUnit) {
				walkDerivationPath((DerivedUnit) sob, (DerivedUnit) sob, documentFeatures, null);
			}
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

	/**
	 * @param documentFeatures
	 * @param sob
	 * @param gatherEvent
	 */
	private void createFieldUnitPlacemarks(List<Feature> documentFeatures, SpecimenOrObservationBase sob,
			GatheringEvent gatherEvent, LocationType locationType, DefinedTerm kindOfUnit) {
		Placemark mapMarker = fieldUnitLocationMarker(gatherEvent.getExactLocation(),
				gatherEvent.getAbsoluteElevation(), locationType, kindOfUnit);
		documentFeatures.add(mapMarker);
		addExtendedData(mapMarker, sob, gatherEvent);
		errorRadiusPlacemark(gatherEvent.getExactLocation()).ifPresent(pm -> documentFeatures.add(pm));
	}


	/**
	 * @param exactLocation
	 * @param altitude
	 * @param locationType
	 * @param kindOfUnit
	 * @return
	 */
	private Placemark fieldUnitLocationMarker(Point exactLocation, Integer altitude, LocationType locationType,
			DefinedTerm kindOfUnit) {

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
		mapMarker.setStyleUrl(styleURL(locationType, kindOfUnit));

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
	
	private void addExtendedData(Placemark mapMarker, SpecimenOrObservationBase sob, GatheringEvent gatherEvent) {
		
		String name = "";
		if(sob.getRecordBasis() != null) {
			name = sob.getRecordBasis().name();
		} else {
			name = SpecimenOrObservationType.Unknown.name();
		}
		String titleCache = sob.getTitleCache();
		String locationString = null;
		if(gatherEvent.getExactLocation() != null) {
			locationString = gatherEvent.getExactLocation().toSexagesimalString(false,  true);
			titleCache = titleCache.replace(locationString + ", ", "");
		}
		String description = "<p class=\"title-cache\">" + titleCache 
				+ " <a class=\"specimen-link\" href=\"${specimen-link-base-url}/" + sob.getUuid() + "\">${specimen-link-text}</a>"
				+ "</p>";
		if(locationString != null) {
			// see https://www.mediawiki.org/wiki/GeoHack
			description += "<p class=\"exact-location\"><a target=\"geohack\" href=\"https://tools.wmflabs.org/geohack/en/" + 
		gatherEvent.getExactLocation().getLatitude() + ";" + gatherEvent.getExactLocation().getLongitude() +"?pagename=" + name + "\">" + locationString + "</a></p>";
		}
		// mapMarker.setName(name);
		mapMarker.setDescription(description);
		
		ExtendedData extendedData = mapMarker.createAndSetExtendedData();
		extendedData.createAndAddData(sob.getTitleCache()).setName("titleCache");
		if(mapMarker.getGeometry() != null && mapMarker.getGeometry() instanceof de.micromata.opengis.kml.v_2_2_0.Point) {
			extendedData.createAndAddData(((de.micromata.opengis.kml.v_2_2_0.Point)mapMarker.getGeometry()).getCoordinates().toString()).setName("Location");
		}
		
	}


	private String styleURL(LocationType locationType, DefinedTerm kindOfUnit) {
		String key = "";
		if (locationType != null) {
			key += locationType.name();
		}
		if (kindOfUnit != null) {
			key += kindOfUnit.getUuid();
		}
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

	public enum LocationType {
		FIELD_UNIT, DERIVED_UNIT;
	}
}
