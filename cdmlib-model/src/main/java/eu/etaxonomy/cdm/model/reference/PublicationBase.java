/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.collection.PersistentSet;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;

/**
 * This (abstract) class represents all different kind of published {@link StrictReferenceBase references}
 * which constitute a physical or virtual unit. A reference is a published
 * reference if it can be consulted by the general public.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicationBase", propOrder = {
    "publishers"
})
@XmlRootElement(name = "PublicationBase")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
public abstract class PublicationBase<S extends IReferenceBaseCacheStrategy> extends StrictReferenceBase<S> {
	private static final Logger logger = Logger.getLogger(PublicationBase.class);

	@XmlElementWrapper(name = "Publishers")
	@XmlElement(name = "Publisher")
    @OneToMany (cascade = {javax.persistence.CascadeType.ALL}, fetch= FetchType.LAZY)
	@IndexColumn(name="sortIndex", base = 0)
	@JoinColumn (name = "referenceBase_id")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	private List<Publisher> publishers = new ArrayList<Publisher>();
	
	
	public PublicationBase(){
		super();
	}

	/**
	 * Returns the list of publishers representing the name of the publisher and the place. 
	 * A publisher is mostly an institution or a private company which assumed the 
	 * global responsibility for the publication process whereas the place is 
	 * mostly a city .<BR>
	 * 
	 * @return  the list of publishers of <i>this</i>
	 * 			publication
	 * @see 	#getEditor()
	 */
	public List<Publisher> getPublishers(){
		return publishers;
	}
	

	public Publisher getPublisher(int index) throws IndexOutOfBoundsException{
		try{	
//			return this.publishers.iterator().next();
			return this.publishers.get(index);
		} catch (IndexOutOfBoundsException e) {
			logger.warn("IndexOutOfBoundsException. Index is " + index + " but must be 0 <= index < " + publishers.size());
			throw e;
		}
	}
	
	public void addPublisher(String publisher, String place){
		Publisher newPublisher = new Publisher(publisher, place);
		publishers.add(newPublisher);
	}
	public void addPublisher(String publisher, String place, int index) throws IndexOutOfBoundsException{
		try {
			Publisher newPublisher = new Publisher(publisher, place);
//			publishers.add(newPublisher);
			publishers.add(index, newPublisher);
		} catch (IndexOutOfBoundsException e) {
			logger.warn("IndexOutOfBoundsException. Index is " + index + " but must be 0 <= index < " + publishers.size());
			throw e;
		}
	}
	
	public void removePublisher(Publisher publisher){
		publishers.remove(publisher);
	}
	
//*********** CLONE **********************************/	


	/** 
	 * Clones <i>this</i> publication. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * publication by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		PublicationBase result = (PublicationBase)super.clone();
		//Publisher
		result.publishers = new ArrayList<Publisher>();
		for (Publisher publisher : this.publishers ){
			Publisher newPublisher;
			try {
				newPublisher = (Publisher)publisher.clone();
			} catch (CloneNotSupportedException e) {
				//Publisher implements Cloneable therefore this should not be reached
				throw new RuntimeException("Publisher does not implement Cloneable");
			}
			result.addPublisher(newPublisher.getPublisherName(), newPublisher.getPlace());
		}
		//No changes: -
		return result;
	}

}