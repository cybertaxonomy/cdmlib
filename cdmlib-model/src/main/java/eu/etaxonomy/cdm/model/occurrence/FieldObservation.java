/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations .CascadeType;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * In situ observation of a taxon in the field. If a specimen exists, 
 * in most cases a parallel field observation object should be instantiated and the specimen then is "derived" from the field unit
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:40
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FieldObservation", propOrder = {
    "fieldNumber",
    "fieldNotes",
    "gatheringEvent"
})
@XmlRootElement(name = "FieldObservation")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
@Audited
@Configurable
public class FieldObservation extends SpecimenOrObservationBase<IIdentifiableEntityCacheStrategy<FieldObservation>> implements Cloneable{
	private static final Logger logger = Logger.getLogger(FieldObservation.class);

	@XmlElement(name = "FieldNumber")
	@Field(index=Index.TOKENIZED)
	@NullOrNotEmpty
	@Length(max = 255)
	private String fieldNumber;
	
	@XmlElement(name = "FieldNotes")
	@Field(index=Index.TOKENIZED)
	@NullOrNotEmpty
	@Length(max = 255)
	private String fieldNotes;
	
	@XmlElement(name = "GatheringEvent")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE })
    @IndexedEmbedded(depth = 2)
    @Valid
	private GatheringEvent gatheringEvent;

	/**
	 * Factory method
	 * @return
	 */
	public static FieldObservation NewInstance(){
		return new FieldObservation();
	}
	
	/**
	 * Constructor
	 */
	protected FieldObservation(){
		super();
		this.cacheStrategy = new IdentifiableEntityDefaultCacheStrategy<FieldObservation>();
	}

    @Override
	public GatheringEvent getGatheringEvent() {
    	return gatheringEvent;
	}
	
	public void setGatheringEvent(GatheringEvent gatheringEvent) {
		this.gatheringEvent = gatheringEvent;
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
	
	//*********** CLONE **********************************/	
	
	/** 
	 * Clones <i>this</i> field observation. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> field observation
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link SpecimenOrObservationBase SpecimenOrObservationBase}.
	 * 
	 * @see SpecimenOrObservationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public FieldObservation clone(){
		try{
			FieldObservation result = (FieldObservation)super.clone();		
			//no changes to: fieldNotes, fieldNumber
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
		
	}
}