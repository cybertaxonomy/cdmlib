/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy;


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
	
	/**
	 *  A taxon name including genus name, epithet etc.
	 */
	name,
	/**
	 *  A name rank abbreviation, e.g. subsp.
	 */
	rank,
	/**
	 * Non-atomised addition to a taxon name not ruled by a nomenclatural code
	 */
	appendedPhrase,
	/**
	 * The authors of a reference, also used in taxon names 
	 */
	authors,
	/**
	 * a reference
	 */
	reference,
	/**
	 * Volume, page number etc. of a reference
	 */
	microreference,
	/**
	 * publication year of a reference
	 */
	year,
}
