/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.agent.AgentBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventBase", propOrder = {
    "timeperiod",
    "actor",
    "description"
})
@XmlRootElement(name = "EventBase")
@MappedSuperclass
@Audited
public abstract class EventBase extends AnnotatableEntity implements IEvent {
	private static final long serialVersionUID = -1859035632758446593L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EventBase.class);

	@XmlElement(name = "TimePeriod")
	private TimePeriod timeperiod = TimePeriod.NewInstance();

	@XmlElement(name = "Actor")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade(CascadeType.SAVE_UPDATE)
	private AgentBase<?> actor;

	@XmlElement(name = "Description")
	@Field(index=Index.YES)
	private String description;

//******************** GETTER / SETTER *******************/	
	
	@Override
    public TimePeriod getTimeperiod() {
		return timeperiod;
	}
	@Override
    public void setTimeperiod(TimePeriod timeperiod) {
		if (timeperiod == null){
			timeperiod = TimePeriod.NewInstance();
		}
		this.timeperiod = timeperiod;
	}

	@Override
    public AgentBase getActor() {
		return actor;
	}
	@Override
    public void setActor(AgentBase actor) {
		this.actor = actor;
	}

	/**
	 * The description of this event. Implementing classes may use this field for different purposes.
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see #getDescription()
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}


//*********** CLONE **********************************/

	/**
	 * Clones <i>this</i> event base. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> event base
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link AnnotatableEntity AnnotatableEntity}.
	 *
	 * @see eu.etaxonomy.cdm.model.media.AnnotatableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		EventBase result = (EventBase)super.clone();
		//Actor  //is this needed??
		result.setActor(this.getActor());
		//time period
		result.setTimeperiod((TimePeriod)this.getTimeperiod().clone());
		//no changes to: description
		return result;
	}



}
