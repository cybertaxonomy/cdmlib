/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.Set;

public class ReferenceTO extends BaseTO{
	
	/**
	 * 	URIs like DOIs, LSIDs or Handles for this reference
	 */
	private String uri;
	/**
	 * 
	 */
	private String authorship;
	
	private String citation;
	/**
	 * Details of the nomenclatural reference (protologue). These are mostly (implicitly) pages but can also be figures or
	 * tables or any other element of a publication. {only if a nomenclatural reference exists}
	 */
	private String microReference;
	/**
	 * year of the publication 
	 */
	private String year;
	
	private Set<IdentifiedString> media_uri;
}
