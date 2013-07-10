// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.out.CdmDbExportMapping;

/**
 * @author e.-m.lee
 * @date 24.02.2010
 *
 */
public class PesiExportMapping extends CdmDbExportMapping<PesiExportState,PesiExportConfigurator, PesiTransformer> {
	private static final Logger logger = Logger.getLogger(PesiExportMapping.class);
	
	public PesiExportMapping(String tableName) {
		super(tableName);
	}

	public static Logger getLogger() {
		return logger;
	}
	
	
}
