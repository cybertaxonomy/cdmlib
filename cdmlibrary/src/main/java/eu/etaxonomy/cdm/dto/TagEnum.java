/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.dto;

/**
 * Tags for atomised taxon name strings and atomised reference citation strings.
 * Used by {@link TaggedText}.
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 13.12.2007 12:04:15
 *
 */
public enum TagEnum {
	
	name,
	authors,
	reference,
	microreference,
	year,
}
