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
    "publisher",
    "placePublished"
})
@XmlRootElement(name = "PublicationBase")
@Entity
public abstract class PublicationBase extends StrictReferenceBase {
	
	static Logger logger = Logger.getLogger(PublicationBase.class);
	
	@XmlElement(name = "Publisher")
	private String publisher;
	
	@XmlElement(name = "PlacePublished")
	private String placePublished;

	public PublicationBase(){
		super();
	}

	
	/**
	 * Returns the string representing the name of the publisher of <i>this</i>
	 * publication. A publisher is mostly an institution or a private
	 * company which assumed the global responsibility for the publication
	 * process.
	 * 
	 * @return  the string identifying the publisher of <i>this</i>
	 * 			publication
	 */
	public String getPublisher(){
		return this.publisher;
	}
	/**
	 * @see #getPublisher()
	 */
	public void setPublisher(String publisher){
		this.publisher = publisher;
	}


	/**
	 * Returns the string representing the name of the place (mostly the city)
	 * where <i>this</i> publication has been published.
	 * 
	 * @return  the string identifying the publication place of <i>this</i>
	 * 			publication
	 */
	public String getPlacePublished(){
		return this.placePublished;
	}
	/**
	 * @see #getPlacePublished()
	 */
	public void setPlacePublished(String placePublished){
		this.placePublished = placePublished;
	}
	
	
//*********** CLONE **********************************/	


	/** 
	 * Clones <i>this</i> publication. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i>
	 * publication by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see StrictReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		PublicationBase result = (PublicationBase)super.clone();
		//no changes to: placePublished, publisher
		return result;
	}

}