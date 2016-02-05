/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 *
 * In situ observation of a taxon in the field. If a specimen exists,
 * in most cases a parallel field unit object should be instantiated and the specimen then
 * is "derived" from the field unit via derivation type "accessioning" or any other.
 *
 * @author m.doering
 * @created 08-Nov-2007 13:06:40
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FieldUnit", propOrder = {
    "fieldNumber",
    "primaryCollector",
    "fieldNotes",
    "gatheringEvent"
})
@XmlRootElement(name = "FieldUnit")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
@Audited
@Configurable
public class FieldUnit extends SpecimenOrObservationBase<IIdentifiableEntityCacheStrategy<FieldUnit>> implements Cloneable{
	private static final long serialVersionUID = -7586670941559035171L;
	private static final Logger logger = Logger.getLogger(FieldUnit.class);

	@XmlElement(name = "FieldNumber")
	@Field
    //TODO Val #3379
//	@NullOrNotEmpty
	@Column(length=255)
	private String fieldNumber;

	@XmlElement(name = "PrimaryCollector")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE,CascadeType.MERGE })
    @IndexedEmbedded(depth = 2)
    @Valid
	private Person primaryCollector;

	@XmlElement(name = "FieldNotes")
	@Field
    //TODO Val #3379
//	@NullOrNotEmpty
	@Column(length=255)
	private String fieldNotes;

	@XmlElement(name = "GatheringEvent")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE,CascadeType.MERGE })
    @IndexedEmbedded(depth = 2)
    @Valid
	private GatheringEvent gatheringEvent;

// *************** FACTORY METHOD *************************/

	/**
	 * Factory method.
	 * @return
	 */
	public static FieldUnit NewInstance(){
		return new FieldUnit();
	}

//****************************** CONSTRUCTOR **************************************/

	/**
	 * Constructor
	 */
	protected FieldUnit(){
		super(SpecimenOrObservationType.FieldUnit);
		this.cacheStrategy = new IdentifiableEntityDefaultCacheStrategy<FieldUnit>();
	}

// ************************ GETTER / SETTER *******************************************

	public GatheringEvent getGatheringEvent() {
    	return gatheringEvent;
	}

	public void setGatheringEvent(GatheringEvent gatheringEvent) {
		this.gatheringEvent = gatheringEvent;
		addGatheringEventPropertyChangeListener();
	}


	private void addGatheringEventPropertyChangeListener() {
		if (gatheringEvent != null){
			gatheringEvent.addPropertyChangeListener(getNewGatheringEventPropChangeListener());
		}
	}

	/**
	 * The collectors field number. If the collector is a team the field number
	 * is taken from the field book of the primary collector.
	 * @see #primaryCollector
	 * @return
	 */
	public String getFieldNumber() {
		return fieldNumber;
	}

	public void setFieldNumber(String fieldNumber) {
		this.fieldNumber = StringUtils.isBlank(fieldNumber)? null : fieldNumber;
	}


	/**
	 * The primary collector is the person who the field books belongs to.
	 * So the field number is also taken from him (his field book).
	 * @see #fieldNumber
	 * @param primaryCollector
	 */
	public void setPrimaryCollector(Person primaryCollector) {
		this.primaryCollector = primaryCollector;
	}

	public Person getPrimaryCollector() {
		return primaryCollector;
	}

	public String getFieldNotes() {
		return fieldNotes;
	}

	public void setFieldNotes(String fieldNotes) {
		this.fieldNotes = StringUtils.isBlank(fieldNotes)? null : fieldNotes;
	}

	// *********** Listener *****************************/

	private PropertyChangeListener getNewGatheringEventPropChangeListener() {
		PropertyChangeListener listener = new PropertyChangeListener(){

			@Override
            public void propertyChange(PropertyChangeEvent event) {
				firePropertyChange(event);
			}

		};
		return listener;
	}

	//*********** CLONE **********************************/

	/**
	 * Clones <i>this</i> field unit. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> field unit
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link SpecimenOrObservationBase SpecimenOrObservationBase}.
	 *
	 * @see SpecimenOrObservationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public FieldUnit clone(){
		try{
			FieldUnit result = (FieldUnit)super.clone();
			//no changes to: fieldNotes, fieldNumber
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}

	}

}