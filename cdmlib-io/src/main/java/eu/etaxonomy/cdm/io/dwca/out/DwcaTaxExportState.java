// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlExportState;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
public class DwcaTaxExportState extends XmlExportState<DwcaTaxExportConfigurator>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxExportState.class);

	public DwcaTaxExportState(DwcaTaxExportConfigurator config) {
		super(config);
	}


}
