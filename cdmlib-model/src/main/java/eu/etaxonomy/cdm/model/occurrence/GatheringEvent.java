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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;

/**
 * The event when gathering a specimen or recording a field observation only
 * @author m.doering
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GatheringEvent", propOrder = {
    "locality",
    "exactLocation",
    "collectingArea",
    "collectingMethod",
    "absoluteElevation",
    "absoluteElevationError",
    "distanceToGround",
    "distanceToWaterSurface"
})
@XmlRootElement(name = "GatheringEvent")
@Entity
public class GatheringEvent extends EventBase {
	
	static Logger logger = Logger.getLogger(GatheringEvent.class);

	//Locality name (as free text) where this occurrence happened
	@XmlElement(name = "Locality")
	private String locality;
	
	@XmlElement(name = "ExactLocation")
	private Point exactLocation;
	
	@XmlElement(name = "CollectingArea")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private NamedArea collectingArea;
	
	@XmlElement(name = "CollectingMethod")
	private String collectingMethod;
	
	// meter above/below sea level of the surface
	@XmlElement(name = "AbsoluteElevation")
	private Integer absoluteElevation;
	
	@XmlElement(name = "AbsoluteElevationError")
	private Integer absoluteElevationError;
	
	// distance in meter from the ground surface when collecting. E.g. 10m below the ground or 10m above the ground/bottom of a lake or 20m up in the canope 
	@XmlElement(name = "DistanceToGround")
	private Integer distanceToGround;
	
	// distance in meters to lake or sea surface. Similar to distanceToGround use negative integers for distance *below* the surface, ie under water 
	@XmlElement(name = "DistanceToWaterSurface")
	private Integer distanceToWaterSurface;


	/**
	 * Factory method
	 * @return
	 */
	public static GatheringEvent NewInstance(){
		return new GatheringEvent();
	}
	
	/**
	 * Constructor
	 */
	protected GatheringEvent() {
		super();
	}

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
