/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 05.02.2008 14:59:38
 *
 */
public class ReferencedEntityBaseTO extends BaseTO{
	
	private String citationMicroReference;
	private String originalNameString;
	private ReferenceBase citation;

}
