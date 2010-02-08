// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.babadshanjan
 * @created 22.09.2009
 * @version 1.0
 */
public class CdmImportState extends ImportStateBase<CdmImportConfigurator>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmImportState.class);

	
	public CdmImportState(CdmImportConfigurator config) {
		super(config);
	}
	
}
