/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * Marker types similar to dynamically defined attributes. These  content types
 * like "IS_DOUBTFUL", "COMPLETE"  or specific local flags.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:22
 */
public class MarkerType extends DefinedTermBase {
	static Logger logger = Logger.getLogger(MarkerType.class);

	public static final MarkerType IMPORTED(){
		return null;
	}

	public static final MarkerType TO_BE_CHECKED(){
		return null;
	}

	public static final MarkerType IS_DOUBTFUL(){
		return null;
	}

	public static final MarkerType COMPLETE(){
		return null;
	}

}