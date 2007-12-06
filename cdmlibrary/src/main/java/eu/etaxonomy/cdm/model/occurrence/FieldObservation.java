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
public class FieldObservation extends SpecimenOrObservationBase{
	static Logger logger = Logger.getLogger(FieldObservation.class);

	private String fieldNumber;
	private String fieldNotes;
	private GatheringEvent gatheringEvent;


	@Override
	@ManyToMany
	@Cascade( { CascadeType.SAVE_UPDATE })
	public GatheringEvent getGatheringEvent() {
		return this.gatheringEvent;
	}
	public void setGatheringEvent(GatheringEvent gatheringEvent) {
		this.setGatheringEvent(gatheringEvent);
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