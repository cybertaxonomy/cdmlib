/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;

/**
 * This class is used to configure taxon node deletion.
 *
 * @see ITaxonNodeService#delete(eu.etaxonomy.cdm.model.taxon.TaxonNode)
 *
 * @author a.mueller
 * @since 09.11.2011
 *
 */
public class TaxonNodeDeletionConfigurator extends NodeDeletionConfigurator {

    private static final long serialVersionUID = 1187266117499706532L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public boolean isDeleteTaxon() {
		return isDeleteElement();
	}
	public void setDeleteTaxon(boolean deleteTaxon) {
		this.deleteElement = deleteTaxon;
	}
}