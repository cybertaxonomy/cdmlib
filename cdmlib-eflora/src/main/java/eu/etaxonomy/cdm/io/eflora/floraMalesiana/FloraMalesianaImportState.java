/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.eflora.floraMalesiana;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.eflora.EfloraImportState;

/**
 * @author a.mueller
 *
 */
public class FloraMalesianaImportState extends EfloraImportState{
	private static final Logger logger = Logger.getLogger(FloraMalesianaImportState.class);

// ******************************* CONSTRUCTOR **********************************************
	
	public FloraMalesianaImportState(FloraMalesianaImportConfigurator config) {
		super(config);
	}
	
}
