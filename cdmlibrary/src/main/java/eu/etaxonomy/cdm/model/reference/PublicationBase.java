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

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:38
 */
public abstract class PublicationBase extends StrictReferenceBase {
	static Logger logger = Logger.getLogger(PublicationBase.class);

	@Description("")
	private String publisher;
	@Description("")
	private String placePublished;

	public String getPublisher(){
		return publisher;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPublisher(String newVal){
		publisher = newVal;
	}

	public String getPlacePublished(){
		return placePublished;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPlacePublished(String newVal){
		placePublished = newVal;
	}

}