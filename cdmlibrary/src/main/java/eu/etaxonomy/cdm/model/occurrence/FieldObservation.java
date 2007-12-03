/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.IEvent;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;

import java.util.*;

import javax.persistence.*;

/**
 * In situ observation of a taxon in the field. If a specimen exists, 
 * in most cases a parallel field observation object should be instantiated and the specimen then is "derived" from the field unit
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:40
 */
@Entity
public class FieldObservation extends SpecimenOrObservation implements IEvent{
	static Logger logger = Logger.getLogger(FieldObservation.class);

	//Locality name (as free text) where this occurrence happened
	private String locality;
	//Date on which this occurrence happened
	private Calendar collectingDate;
	private Point exactLocation;
	private NamedArea collectingArea;
	private String collectingMethod;
	private Agent collector;
	private String fieldNumber;
	private String fieldNotes;
	// meter above/below sea level of the surface
	private Integer absoluteElevation;
	private Integer absoluteElevationError;
	// distance in meter from the ground surface when collecting. E.g. 10m below the ground or 10m above the ground/bottom of a lake or 20m up in the canope 
	private Integer distanceToGround;
	// distance in meters to lake or sea surface. Simmilar to distanceToGround use negative integers for distance *below* the surface, ie under water 
	private Integer distanceToWaterSurface;

	public Point getExactLocation(){
		return this.exactLocation;
	}
	public void setExactLocation(Point exactLocation){
		this.exactLocation = exactLocation;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getCollector(){
		return this.collector;
	}
	public void setCollector(Agent collector){
		this.collector = collector;
	}

	@ManyToOne
	public NamedArea getCollectingArea(){
		return this.collectingArea;
	}
	public void setCollectingArea(NamedArea area){
		this.collectingArea = area;
	}

	public String getLocality(){
		return this.locality;
	}
	public void setLocality(String locality){
		this.locality = locality;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getEventDate(){
		return this.collectingDate;
	}
	public void setEventDate(Calendar eventDate){
		this.collectingDate = eventDate;
	}

	public String getFieldNumber() {
		return fieldNumber;
	}

	public void setFieldNumber(String fieldNumber) {
		this.fieldNumber = fieldNumber;
	}

	public String getFieldNotes() {
		return fieldNotes;
	}

	public void setFieldNotes(String fieldNotes) {
		this.fieldNotes = fieldNotes;
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


	public Integer getAbsoluteElevationError() {
		return absoluteElevationError;
	}
	public void setAbsoluteElevationError(Integer absoluteElevationError) {
		this.absoluteElevationError = absoluteElevationError;
	}
	public Integer getDistanceToGround() {
		return distanceToGround;
	}
	public void setDistanceToGround(Integer distanceToGround) {
		this.distanceToGround = distanceToGround;
	}
	public Integer getDistanceToWaterSurface() {
		return distanceToWaterSurface;
	}
	public void setDistanceToWaterSurface(Integer distanceToWaterSurface) {
		this.distanceToWaterSurface = distanceToWaterSurface;
	}
	
	@Transient
	public Agent getActor() {
		return this.collector;
	}
	public void setActor(Agent actor) {
		collector=actor;
	}
	@Transient
	public TimePeriod getTimeperiod() {
		return new TimePeriod(this.collectingDate);
	}
	public void setTimeperiod(TimePeriod timeperiod) {
		this.collectingDate=timeperiod.getStart();
	}
}