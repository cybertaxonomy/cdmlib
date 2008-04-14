/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import org.apache.log4j.Logger;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:46
 */
@Entity
public abstract class PublicationBase extends StrictReferenceBase {
	static Logger logger = Logger.getLogger(PublicationBase.class);
	private String publisher;
	private String placePublished;

	public String getPublisher(){
		return this.publisher;
	}
	public void setPublisher(String publisher){
		this.publisher = publisher;
	}


	public String getPlacePublished(){
		return this.placePublished;
	}
	public void setPlacePublished(String placePublished){
		this.placePublished = placePublished;
	}

}