/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

public interface IPublicationBase extends IReferenceBase {
	
	public String getPublisher() ;
	
	public void setPublisher(String publisher) ;
	/**
	 * @param publisher the publisher to set
	 * @param placePublished the place where the publication was published
	 */
	public void setPublisher(String publisher, String placePublished);
		

	public String getPlacePublished() ;
	
	public void setPlacePublished(String placePublished) ;
}
