// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.redlist.validation;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.redlist.RoteListeDbImportConfigurator;
import eu.etaxonomy.cdm.io.redlist.RoteListeDbImportState;

/**
 * @author a.mueller
 * @created 17.02.2010
 * @version 1.0
 */
public class RoteListeDbTaxonImportValidator implements IOValidator<RoteListeDbImportState>{
	private static final Logger logger = Logger.getLogger(RoteListeDbTaxonImportValidator.class);

	public boolean validate(RoteListeDbImportState state){
		boolean result = true;
		RoteListeDbImportConfigurator config = state.getConfig();
		logger.warn("Checking for Taxa not yet implemented");
//		result &= checkParentTaxonStatus(config);
//		result &= checkAccParentTaxonStatus(config);
		return result;
	}
	

	


}
