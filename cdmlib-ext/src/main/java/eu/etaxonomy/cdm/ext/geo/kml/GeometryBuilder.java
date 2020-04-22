/**
 * Copyright (C) 2020 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.ext.geo.kml;

import java.awt.geom.Point2D;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Length;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import si.uom.SI;
import tec.uom.se.quantity.Quantities;

/**
 * See :
 * 
 * https://gis.stackexchange.com/questions/311272/create-dynamic-circle-polygon-from-specific-lat-long-using-geotools
 * https://gis.stackexchange.com/questions/283183/using-geometricshapefactory-to-create-circle-with-radius-in-miles
 * 
 * https://stackoverflow.com/questions/36481651/how-do-i-create-a-circle-with-latitude-longitude-and-radius-with-geotools#36528805
 * 
 * 
 * Another great library for creating shapes is https://github.com/locationtech/spatial4j
 * 
 * @author Andreas Kohlbecker
 * @since Apr 21, 2020
 */
public class GeometryBuilder {

	private final static Logger logger = Logger.getLogger(GeometryBuilder.class);

	public enum CircleMethod {
		circle, simpleCircleSmall, simpleCircle, reprojectedCircle;
	}

	/**
	 * Fails with javax.measure.IncommensurableException: m is not compatible with
	 * deg
	 * 
	 * see
	 * https://gis.stackexchange.com/questions/283183/using-geometricshapefactory-to-create-circle-with-radius-in-miles
	 * 
	 * @param radius
	 * @param latitude
	 * @param longitude
	 * @return
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 */
	public Polygon circle(Double radius, Double latitude, Double longitude)
			throws NoSuchAuthorityCodeException, FactoryException {

		Quantity<Length> radiusMeter = Quantities.getQuantity(radius, SI.METRE);
		Unit<Length> origUnit = (Unit<Length>) DefaultGeographicCRS.WGS84.getCoordinateSystem().getAxis(0).getUnit(); //// (Unit<Length>)
																														//// ////
																														//// origCRS.getCoordinateSystem().getAxis(0).getUnit();

		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(32);
		shapeFactory.setCentre(new Coordinate(latitude, longitude));
		shapeFactory.setSize(radiusMeter.to(origUnit).getValue().doubleValue() * 2);

		Polygon circlePolygon = shapeFactory.createCircle();
		circlePolygon.setSRID(4326);

		return circlePolygon;
	}

	/**
	 * Only suitable for small radius (> 1000 m) as the circles are heavily
	 * distorted otherwise.
	 * 
	 * @param radius
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public Geometry simpleCircleSmall(Double radius, Double latitude, Double longitude) {

		double diameterInMeters = radius * 2.0d;
		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(64); // adjustable
		shapeFactory.setCentre(new Coordinate(longitude, latitude));
		// Length in meters of 1° of latitude = always 111.32 km
		shapeFactory.setWidth(diameterInMeters / 111320d);
		// Length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
		shapeFactory.setHeight(diameterInMeters / (40075000 * Math.cos(Math.toRadians(latitude)) / 360));

		Polygon circle = shapeFactory.createEllipse();
		return circle;
	}

	/**
	 * Creates perfect circles which are looking good but might be projected
	 * incorrectly for the resulting map
	 * 
	 * @param distance
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public Geometry simpleCircle(Quantity<Length> distance, Double latitude, Double longitude) {

		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		CoordinateSequence coordinateSequence = geometryFactory.getCoordinateSequenceFactory()
				.create(new Coordinate[] { new Coordinate(longitude, latitude) });
		Point point = new Point(coordinateSequence, geometryFactory);

		GeodeticCalculator calc = new GeodeticCalculator(DefaultGeographicCRS.WGS84);
		calc.setStartingGeographicPoint(longitude, latitude);
		UnitConverter converter = distance.getUnit().getConverterTo(SI.METRE);
		double d = converter.convert(distance.getValue()).doubleValue();
		calc.setDirection(0.0, d);
		Point2D p2 = calc.getDestinationGeographicPoint();
		calc.setDirection(90.0, d);
		Point2D p3 = calc.getDestinationGeographicPoint();

		double dy = p2.getY() - latitude;
		double dx = p3.getX() - longitude;
		double dist = (dy + dx) / 2.0;
		Polygon p1 = (Polygon) point.buffer(dist);
		return p1;
	}

	/**
	 * This Method should produces the best circles.
	 * 
	 * The code is based on an example published by Ian Turton on stackoverflow:
	 * 
	 * https://stackoverflow.com/questions/36481651/how-do-i-create-a-circle-with-latitude-longitude-and-radius-with-geotools#36528805
	 * 
	 * see https://gist.github.com/ianturton/973563fe5004985ba35a6e2247f7d823 and
	 * https://gitlab.com/snippets/17558
	 * 
	 * @param feature
	 * @param distance
	 * @return
	 */
	public Geometry bufferFeature(SimpleFeature feature, Double radius) {

		// replacment for Measure<Double, Length> distance:
		Quantity<Length> radiusMeter = Quantities.getQuantity(radius, SI.METRE);

		// extract the geometry
		GeometryAttribute gProp = feature.getDefaultGeometryProperty();
		CoordinateReferenceSystem origCRS = gProp.getDescriptor().getCoordinateReferenceSystem();

		Geometry geom = (Geometry) feature.getDefaultGeometry();
		Geometry pGeom = geom;
		MathTransform toTransform, fromTransform = null;

		// reproject the geometry to a local projection
		if (!(origCRS instanceof ProjectedCRS)) {

			double x = geom.getCoordinate().x;
			double y = geom.getCoordinate().y;

			String code = "AUTO:42001," + x + "," + y;
			// System.out.println(code);
			CoordinateReferenceSystem auto;
			try {
				auto = CRS.decode(code);
				toTransform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);
				fromTransform = CRS.findMathTransform(auto, DefaultGeographicCRS.WGS84);
				pGeom = JTS.transform(geom, toTransform);

			} catch (MismatchedDimensionException | TransformException | FactoryException e) {
				logger.error(e);
			}

		}

		// create a buffer around the geometry, assumes the geometry is in the same
		// units as the distance variable.
		Geometry out = pGeom.buffer(radiusMeter.getValue().doubleValue());
		Geometry retGeom = out;
		// reproject the geometry to the original projection
		if (!(origCRS instanceof ProjectedCRS)) {
			try {
				retGeom = JTS.transform(out, fromTransform);

			} catch (MismatchedDimensionException | TransformException e) {
				logger.error(e);
			}
		}

		return retGeom;
	}

	public static SimpleFeature createSimplePointFeature(Double longitude, Double latitude) throws SchemaException {
		SimpleFeatureType schema = DataUtilities.createType("", "Location", "locations:Point:srid=4326,id:Integer" // a
																													// number
																													// attribute
		);
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);

		GeometryFactory geometryFactory = new GeometryFactory();
		/* Longitude (= x coord) first ! */
		Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
		featureBuilder.add(point);
		SimpleFeature feature = featureBuilder.buildFeature(null);
		return feature;
	}

}
