/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.model.location.Point;

/**
 * @author pplitzner
 * @since Apr 9, 2015
 *
 */
public class OccurrenceServiceRequestParameterDto {

    private List<Point> fieldUnitPoints;
    private List<Point> derivedUnitPoints;
    private String occurrenceQuery = null;

    /**
     * @return the fieldUnitPoints
     */
    public List<Point> getFieldUnitPoints() {
        return fieldUnitPoints;
    }
    /**
     * @return the derivedUnitPoints
     */
    public List<Point> getDerivedUnitPoints() {
        return derivedUnitPoints;
    }
    /**
     * @return the occurrenceQuery
     */
    public String getOccurrenceQuery() {
        return occurrenceQuery;
    }
    /**
     * @param occurrenceQuery the occurrenceQuery to set
     */
    public void setOccurrenceQuery(String occurrenceQuery) {
        this.occurrenceQuery = occurrenceQuery;
    }
    /**
     * @param fieldUnitPoints the fieldUnitPoints to set
     */
    public void setFieldUnitPoints(List<Point> fieldUnitPoints) {
        this.fieldUnitPoints = fieldUnitPoints;
    }
    /**
     * @param derivedUnitPoints the derivedUnitPoints to set
     */
    public void setDerivedUnitPoints(List<Point> derivedUnitPoints) {
        this.derivedUnitPoints = derivedUnitPoints;
    }
    public void addFieldUnitPoint(Point point){
        if(fieldUnitPoints==null){
            fieldUnitPoints = new ArrayList<Point>();
        }
        fieldUnitPoints.add(point);
    }
    public void addDerivedUnitPoint(Point point){
        if(derivedUnitPoints==null){
            derivedUnitPoints = new ArrayList<Point>();
        }
        derivedUnitPoints.add(point);
    }

}
