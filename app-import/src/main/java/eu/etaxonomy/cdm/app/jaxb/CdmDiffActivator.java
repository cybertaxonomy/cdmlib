/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.app.jaxb;

import java.io.File;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.babadshanjan
 * @created 19.09.2008
 */
public class CdmDiffActivator {

	private static final Logger logger = Logger.getLogger(CdmDiffActivator.class);

	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;

	static final ICdmDataSource cdmSourceOne = CdmDestinations.cdm_test_jaxb();
	static final ICdmDataSource cdmSourceTwo = CdmDestinations.cdm_test_jaxb2();

}
