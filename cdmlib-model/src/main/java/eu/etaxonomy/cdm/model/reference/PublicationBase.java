/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

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
//    "publisher",
//    "placePublished"
})
@XmlRootElement(name = "PublicationBase")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.reference.ReferenceBase")
@Audited
@Deprecated
public abstract class PublicationBase<S extends IReferenceBaseCacheStrategy> extends ReferenceBase<S> {
	private static final long serialVersionUID = -3050853696708743386L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PublicationBase.class);
//	
//	@XmlElement(name = "Publisher")
//	private String publisher;
//	
//	@XmlElement(name = "PlacePublished")
//	private String placePublished;

	
	public PublicationBase(){
		super();
	}
	

	/**
	 * @return the publisher
	 */
	public String getPublisher() {
		return publisher;
	}



	/**
	 * @param publisher the publisher to set
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}



	/**
	 * @return the placePublished
	 */
	public String getPlacePublished() {
		return placePublished;
	}



	/**
	 * @param placePublished the placePublished to set
	 */
	public void setPlacePublished(String placePublished) {
		this.placePublished = placePublished;
	}
	
	


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
		//No changes: - 
		return result;
	}
}