/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlExportState;

/**
 * @author a.mueller
 * @since 11.05.2009
 */
public class SDDExportState extends XmlExportState<SDDExportConfigurator>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SDDExportState.class);

	public SDDExportState(SDDExportConfigurator config) {
		super(config);
	}


}
