/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.in;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 23.11.2011
 */
public class DwcaImportState extends DwcaDataImportStateBase<DwcaImportConfigurator>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportState.class);

	public DwcaImportState(DwcaImportConfigurator config) {
		super(config);
	}
}
