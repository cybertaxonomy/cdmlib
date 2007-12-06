/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;

@Entity
public class GatheringEvent extends DerivationEvent<FieldObservation,PhysicalUnit> {
	static Logger logger = Logger.getLogger(GatheringEvent.class);

	//Locality name (as free text) where this occurrence happened
	private String locality;
	private Point exactLocation;
	private NamedArea collectingArea;
	private String collectingMethod;
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

	/**
	 * EventBase managed attributes
	 * @return
	 */
	@Transient
	public Calendar getGatheringDate(){
		return this.getTimeperiod().getStart();
	}
	public void setGatheringDate(Calendar gatheringDate){
		this.getTimeperiod().setStart(gatheringDate);
	}	

	@Transient
	public Agent getCollector(){
		return this.getActor();
	}
	public void setCollector(Agent collector){
		this.setActor(collector);
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
	
}
