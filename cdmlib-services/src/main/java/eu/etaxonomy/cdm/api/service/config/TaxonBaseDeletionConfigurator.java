// $Id$
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
 * This class is used to configure taxonbase deletion.
 *
 * @see ITaxonService#deleteTaxon(eu.etaxonomy.cdm.model.taxon.Taxon) and ITaxonService#deleteSynonym(eu.etaxonomy.cdm.model.taxon.Synonym)
 *
 * @author k.luther
 * @date 09.11.2011
 *
 */
public class TaxonBaseDeletionConfigurator extends DeleteConfiguratorBase{

	private boolean deleteNameIfPossible = true;
	private boolean deleteSynonymRelations = true;

	private NameDeletionConfigurator nameDeletionConfig = new NameDeletionConfigurator();

	/**
	 * If true the taxons name will be deleted if this is possible.
	 * It is possible if the name is not linked in a way that it can not be deleted.
	 * This depends also on the {@link NameDeletionConfigurator}
	 * @see #getNameDeletionConfig()
	 * @return
	 */
	public boolean isDeleteNameIfPossible() {
		return deleteNameIfPossible;
	}

	public void setDeleteNameIfPossible(boolean deleteNameIfPossible) {
		this.deleteNameIfPossible = deleteNameIfPossible;
	}

	/**
	 * The configurator for name deletion. Only evaluated if {@link #isDeleteNameIfPossible()}
	 * is <code>true</code>.
	 * @see NameDeletionConfigurator
	 * @see #isDeleteNameIfPossible()
	 * @see #isDeleteSynonymsIfPossible()
	 * @return
	 */
	public NameDeletionConfigurator getNameDeletionConfig() {
		return nameDeletionConfig;
	}

	public void setNameDeletionConfig(NameDeletionConfigurator nameDeletionConfig) {
		this.nameDeletionConfig = nameDeletionConfig;
	}

	/**
    *
    * If <code>true</code> all {@link SynonymRelationship relations from taxon to synonyms} will be
    * removed.
    * Synonyms itself will be removed depending on {@link #deleteSynonymsIfPossible}.
    * @return
    */
    public boolean isDeleteSynonymRelations() {
        return deleteSynonymRelations;
    }

    /**
     * @param deleteSynonymRelations the deleteSynonymRelations to set
     */
    public void setDeleteSynonymRelations(boolean deleteSynonymRelations) {
        this.deleteSynonymRelations = deleteSynonymRelations;
    }
}
