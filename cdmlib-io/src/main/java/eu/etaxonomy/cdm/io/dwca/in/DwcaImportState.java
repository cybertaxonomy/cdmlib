// $Id$
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

import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @created 23.11.2011
 */
public class DwcaImportState extends ImportStateBase<DwcaImportConfigurator, DwcaImport>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportState.class);

	boolean taxaCreated;
	
	public DwcaImportState(DwcaImportConfigurator config) {
		super(config);
	}

	/**
	 * True, if taxa have been fully created.
	 * @return
	 */
	public boolean isTaxaCreated() {
		return taxaCreated;
	}

	/**
	 * @param taxaCreated the taxaCreated to set
	 */
	public void setTaxaCreated(boolean taxaCreated) {
		this.taxaCreated = taxaCreated;
	}


}
