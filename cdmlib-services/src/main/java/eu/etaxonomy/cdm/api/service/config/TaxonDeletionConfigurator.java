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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

/**
 * This class is used to configure taxon node deletion.
 * 
 * @see ITaxonNodeService#delete(eu.etaxonomy.cdm.model.taxon.TaxonNode)
 * 
 * @author a.mueller
 * @date 09.11.2011
 *
 */
public class TaxonDeletionConfigurator extends TaxonBaseDeletionConfigurator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonDeletionConfigurator.class);

	private boolean deleteSynonymRelations = true;
	
	private boolean deleteSynonymsIfPossible = true;
	
	
	
	private TaxonNodeDeletionConfigurator taxonNodeConfig = new TaxonNodeDeletionConfigurator();

	
	private boolean deleteTaxonNodes = true;
	
	private boolean deleteTaxonRelationships = true; 
	
	private boolean deleteDescriptions = true;
	

	


	
	/**
	 * The configurator for node deletion. Only evaluated if {@link #isDeleteNode()}
	 * is <code>true</code>.
	 * @see TaxonNodeDeletionConfigurator
	 * @see #isDeleteNode()
	 * @see #isDeleteSynonymsIfPossible()
	 * @return
	 */
	
	public TaxonNodeDeletionConfigurator getTaxonNodeConfig() {
		return taxonNodeConfig;
	}

	public void setTaxonNodeConfig(TaxonNodeDeletionConfigurator taxonNodeConfig) {
		this.taxonNodeConfig = taxonNodeConfig;
	}


	/**
	 * 
	 * If <code>true</code> all {@link SynonymRelationship relations to synonyms} will be 
	 * removed.
	 * Synonyms itself will be removed depending on {@link #deleteSynonymsIfPossible}.
	 * @return
	 */
	public boolean isDeleteSynonymRelations() {
		return deleteSynonymRelations;
	}

	public void setDeleteSynonymRelations(boolean deleteSynonymRelations) {
		this.deleteSynonymRelations = deleteSynonymRelations;
	}

	
	/**
	 * If <code>true</code> synonyms will be removed if possible but only if {@link #isDeleteSynonymRelations()}
	 * is also <code>true</code>.
	 * It is possible to remove a synonym if it is not used in any other context, e.g. any 
	 * other @link {@link SynonymRelationship}
	 * @return
	 */
	public boolean isDeleteSynonymsIfPossible() {
		return deleteSynonymsIfPossible;
	}

	public void setDeleteSynonymsIfPossible(boolean deleteSynonymsIfPossible) {
		this.deleteSynonymsIfPossible = deleteSynonymsIfPossible;
	}



	/**
	 * If <code>true</code> all {@link TaxonNode taxon nodes} this taxon belongs to
	 * are deleted. If <code>false</code> an exception is thrown if this taxon belongs
	 * to a taxon node.
	 * @return
	 */
	public boolean isDeleteTaxonNodes() {
		return deleteTaxonNodes;
	}
	
	public void setDeleteTaxonNodes(boolean deleteTaxonNodes) {
		this.deleteTaxonNodes = deleteTaxonNodes;
	}

	/**
	 * If <code>true</code> all {@link TaxonRelationship taxon relationships} linked to 
	 * the taxon are removed.
	 */
	 // TODO how to handle missapllied names
	public boolean isDeleteTaxonRelationships() {
		return deleteTaxonRelationships;
	}
	
	public void setDeleteTaxonRelationships(boolean deleteTaxonRelationships) {
		this.deleteTaxonRelationships = deleteTaxonRelationships;
	}

	/**
	 * If <code>true</code> all {@link TaxonDescription taxon descriptions} linked to 
	 * the taxon are deleted.
	 */
	public boolean isDeleteDescriptions() {
		return deleteDescriptions;
	}

	public void setDeleteDescriptions(boolean deleteDescriptions) {
		this.deleteDescriptions = deleteDescriptions;
	}

	
	
	
}
