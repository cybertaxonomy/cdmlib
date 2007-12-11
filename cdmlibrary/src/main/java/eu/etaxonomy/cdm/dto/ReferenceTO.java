/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.dto;

import java.net.URI;
import java.util.Calendar;

import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;

public class ReferenceTO extends BaseTO {
	
	/**
	 * title of the publication including authors except if the ReferenceTO 
	 * is used for nomenclatural references. See also {@link INomenclaturalReference}
	 */
	private String title;
	/**
	 * year of the publication 
	 */
	private Calendar year; 
	
	//URIs like DOIs, LSIDs or Handles for this reference
	private String uri;
}
