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

import javax.persistence.*;

/**
 * In situ observation of a taxon in the field. If a specimen exists, 
 * in most cases a parallel field observation object should be instantiated and the specimen then is "derived" from the field unit
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:40
 */
@Entity
public class FieldObservation extends SpecimenOrObservationBase{
	private static final Logger logger = Logger.getLogger(FieldObservation.class);

	private String fieldNumber;
	private String fieldNotes;
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
	}
	
	@Override
	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public GatheringEvent getGatheringEvent() {
		return this.gatheringEvent;
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

}