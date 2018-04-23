/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import eu.etaxonomy.cdm.api.service.ITaxonService;

/**
 * This class is used to configure synonym deletion.
 *
 * @see  ITaxonService#deleteSynonym(eu.etaxonomy.cdm.model.taxon.Synonym)
 *
 * @author k.luther
 \* @since 09.11.2011
 *
 */
public class SynonymDeletionConfigurator extends TaxonBaseDeletionConfigurator{
	private boolean newHomotypicGroupIfNeeded = true;

	public boolean isNewHomotypicGroupIfNeeded() {
		return newHomotypicGroupIfNeeded;
	}

	public void setNewHomotypicGroupIfNeeded(boolean newHomotypicGroupIfNeeded) {
		this.newHomotypicGroupIfNeeded = newHomotypicGroupIfNeeded;
	}




}
