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

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * A publisher is part of any reference inheriting from PublicationBase.
 * A publication can have several publishers each of them having it's own publication place. 
 * The UML association type is composition, which means that a publisher exists only as part of a publication base, not on its own. Therefore
 * all the methods of publisher are not visible but handled by publication base.
 * 
 * @author a.mueller
 * @created 23.03.2009
 * @version 1.0
 */
@Entity
@Audited
@Configurable
public class Publisher extends CdmBase implements Cloneable{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Publisher.class);
	
	
	private String publisherName;
	
	private String place;

	protected Publisher(){
		
	}
	protected Publisher(String publisherName, String place){
		this.publisherName = publisherName;
		this.place = place;
	}
	
	
	/**
	 * @return the publisher
	 */
	public String getPublisherName() {
		return publisherName;
	}

	/**
	 * @param publisher the publisher to set
	 */
	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	/**
	 * @return the place
	 */
	public String getPlace() {
		return place;
	}

	/**
	 * @param place the place to set
	 */
	public void setPlace(String place) {
		this.place = place;
	} 
	
}
