/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;

/**
 * Reference systems for coordinates also according to OGC (Open Geographical
 * Consosrtium)
 * The list should be extensible at runtime through configuration. This needs to
 * be investigated.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:40
 */
public class ReferenceSystem extends DefinedTermBase {
	static Logger logger = Logger.getLogger(ReferenceSystem.class);

	public static final ReferenceSystem WGS84(){
		return null;
	}

}