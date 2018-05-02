/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


/**
 * @author n.hoffmann
 * @since Sep 14, 2009
 * @version 1.0
 */
public class IllegalAncestryException extends RuntimeException {
	private static final long serialVersionUID = -3307728728821304141L;
	
	/**
	 * @param message
	 */
	public IllegalAncestryException(String message) {
		super(message);
	}
	
}
