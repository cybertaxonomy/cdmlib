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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.Length;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.cache.name.CacheUpdate;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * The event when gathering a specimen or recording a field observation only
 * @author m.doering
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GatheringEvent", propOrder = {
    "locality",
    "exactLocation",
    "country",
    "collectingAreas",
    "collectingMethod",
    "absoluteElevation",
    "absoluteElevationError",
    "distanceToGround",
    "distanceToWaterSurface"
})
@XmlRootElement(name = "GatheringEvent")
@Entity
@Audited
@Indexed
public class GatheringEvent extends EventBase implements Cloneable{
	private static final long serialVersionUID = 7980806082366532180L;
	private static final Logger logger = Logger.getLogger(GatheringEvent.class);

	@XmlElement(name = "Locality")
	@OneToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.ALL,CascadeType.DELETE_ORPHAN})
	@IndexedEmbedded
	private LanguageString locality;
	
	@XmlElement(name = "ExactLocation")
	private Point exactLocation;
	
	
	@XmlElement(name = "Country")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private NamedArea country;
	
    @XmlElementWrapper(name = "CollectingAreas")
	@XmlElement(name = "CollectingArea")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@NotNull
	// further collecting areas. Should not include country
	private Set<NamedArea> collectingAreas = new HashSet<NamedArea>();
	
	@XmlElement(name = "CollectingMethod")
	@Field(index=Index.TOKENIZED)
	@NullOrNotEmpty
	@Length(max = 255)
	private String collectingMethod;
	
	// meter above/below sea level of the surface
	@XmlElement(name = "AbsoluteElevation")
	@Field(index=Index.UN_TOKENIZED)
	private Integer absoluteElevation;
	
	@XmlElement(name = "AbsoluteElevationError")
	@Field(index=Index.UN_TOKENIZED)
	private Integer absoluteElevationError;
	
	// distance in meter from the ground surface when collecting. E.g. 10m below the ground or 10m above the ground/bottom of a lake or 20m up in the canope 
	@XmlElement(name = "DistanceToGround")
	@Field(index=Index.UN_TOKENIZED)
	private Integer distanceToGround;
	
	// distance in meters to lake or sea surface. Similar to distanceToGround use negative integers for distance *below* the surface, ie under water 
	@XmlElement(name = "DistanceToWaterSurface")
	@Field(index=Index.UN_TOKENIZED)
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

	

	public NamedArea getCountry() {
		return country;
	}

	public void setCountry(NamedArea country) {
		this.country = country;
	}

	/**
	 * Further collecting areas. Should not include #getCountry()
	 * @return
	 */
	public Set<NamedArea> getCollectingAreas(){
		if(collectingAreas == null) {
			this.collectingAreas = new HashSet<NamedArea>();
		}
		return this.collectingAreas;
	}

	
	 /**
	  * Further collecting areas. Should not include #getCountry()
	  * @param area
	 */
	public void addCollectingArea(NamedArea area){
		if (this.collectingAreas == null)
			this.collectingAreas = getNewNamedAreaSet();
		this.collectingAreas.add(area);
	}
	
	public void removeCollectingArea(NamedArea area){
		//TODO to be implemented?
		logger.warn("not yet fully implemented?");
		this.collectingAreas.remove(area);
	}

	public LanguageString getLocality(){
		return this.locality;
	}
	
	public void setLocality(LanguageString locality){
		this.locality = locality;
	}

	/**
	 * EventBase managed attributes
	 * @return
	 */

	@Transient
	public Partial getGatheringDate(){
		return this.getTimeperiod().getStart();
	}
	
	public void setGatheringDate(Partial gatheringDate){
		this.setTimeperiod(TimePeriod.NewInstance(gatheringDate));
	}	

	public void setGatheringDate(Calendar gatheringDate){
		this.setTimeperiod(TimePeriod.NewInstance(gatheringDate));
	}
	
	@Transient
	public AgentBase getCollector(){
		return this.getActor();
	}
	
	public void setCollector(AgentBase collector){
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
	
//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> gathering event. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> gathering event
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link DerivedUnitBase DerivedUnitBase}.
	 * 
	 * @see DerivedUnitBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public GatheringEvent clone(){
		try{
			GatheringEvent result = (GatheringEvent)super.clone();
			//locality
			LanguageString langString = LanguageString.NewInstance(this.locality.getText(), this.locality.getLanguage());
			result.setLocality(langString);
			//exact location
			result.setExactLocation(this.exactLocation.clone());
			//namedAreas
			result.collectingAreas = new HashSet<NamedArea>();
			for(NamedArea collectingArea : this.collectingAreas) {
				result.addCollectingArea(collectingArea);
			}
			
			//no changes to: distanceToWaterSurface, distanceToGround, collectingMethod, absoluteElevationError, absoluteElevation
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

	private static Set<NamedArea> getNewNamedAreaSet(){
		return new HashSet<NamedArea>();
	}
}
