/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.taxonx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 11.05.2009
 */
public class TaxonXImportState extends ImportStateBase<TaxonXImportConfigurator, CdmImportBase>{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private Reference modsReference;

	/**
	 * @return the modsReference
	 */
	public Reference getModsReference() {
		return modsReference;
	}

	/**
	 * @param modsReference the modsReference to set
	 */
	public void setModsReference(Reference modsReference) {
		this.modsReference = modsReference;
	}

	public TaxonXImportState(TaxonXImportConfigurator config) {
		super(config);
	}

//	@Override
//	public void initialize(TaxonXImportConfigurator config) {
//
//	}

}
