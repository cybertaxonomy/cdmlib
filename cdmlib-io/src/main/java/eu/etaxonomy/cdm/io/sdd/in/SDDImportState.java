// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd.in;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlImportState;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class SDDImportState extends XmlImportState<SDDImportConfigurator, SDDImport>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SDDImportState.class);

	public SDDImportState(SDDImportConfigurator config) {
		super(config);
	}


}
