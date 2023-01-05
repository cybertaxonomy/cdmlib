/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @since 11.05.2009
 */
public class JaxbImportState extends ImportStateBase<JaxbImportConfigurator, CdmImportBase<JaxbImportConfigurator, JaxbImportState>>{

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	public JaxbImportState(JaxbImportConfigurator config) {
		super(config);
	}
}