/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.bci;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.model.occurrence.Collection;


/**
* Interface for queying the Biodiversity Collection Index  via webservices ({@link http://www.biodiversitycollectionsindex.org}). 
* @author a.mueller
* @since Aug 18, 2010
* @version 1.0
 */
public interface IBciServiceWrapper {


	/**
	 * UUID for the reference representing the IPNI database:<BR/>
	 * 8b6d750f-c7e0-4180-afbf-aa4c50148813
	 */
	public static final UUID uuidBci = UUID.fromString("34aa4989-eb5e-4eef-9c9b-080f055ac15c");

	public static final String LOOKUP_CODE_REST = "http://www.biocol.org/rest/lookup/code/";

	
	/**
	 * Returns a list of collections collection code.
	 *  
	 * @param code
	 * @param appConfig
	 * @return
	 */
	public List<Collection> getCollectionsByCode(String code, ICdmRepository appConfig);
	
	public URL getServiceUrl(String url);

}
