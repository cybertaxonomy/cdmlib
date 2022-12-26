/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * This class is used to configure taxon node deletion.
 * It is initialized with the following default settings:
 * <ul>
 *  <li> deleteSynonymRelations: <b>true</b></li>
 *  <li> deleteSynonymsIfPossible: <b>true</b></li>
 *  <li> deleteMisappliedNamesAndInvalidDesignations: <b>true</b></li>
 *  <li> deleteNameIfPossible: <b>true</b></li>
 *  <li> nameDeletionConfig: see {@link NameDeletionConfigurator}</li>
 *  <li> taxonNodeConfig: see {@link TaxonNodeDeletionConfigurator}</li>
 *  <li> deleteTaxonNodes: <b>true</b></li>
 *  <li> deleteTaxonRelationships: <b>true</b>; </li>
 *  <li> deleteDescriptions: <b>true</b></li>
 *  <li> deleteInAllClassifications: <b>true</b></li>
 * </ul>
 *
 * @see ITaxonService#deleteTaxon(eu.etaxonomy.cdm.model.taxon.Taxon)
 *
 * @author a.mueller
 * @since 09.11.2011
 */
public class TaxonDeletionConfigurator extends TaxonBaseDeletionConfigurator {

    private static final long serialVersionUID = -4932682946459109886L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private boolean deleteSynonymsIfPossible = true;

    private boolean deleteMisappliedNames = true;

    private boolean deleteConceptRelationships = false;

    //private NameDeletionConfigurator nameDeletionConfig = new NameDeletionConfigurator();

    private TaxonNodeDeletionConfigurator taxonNodeConfig = new TaxonNodeDeletionConfigurator();

    private boolean deleteTaxonNodes = true;

    private boolean deleteTaxonRelationships = true;

    private boolean deleteDescriptions = true;

    private boolean deleteInAllClassifications = false;

    private UUID classificationUuid = null;


	public boolean isDeleteInAllClassifications() {
        return deleteInAllClassifications;
    }
    public void setDeleteInAllClassifications(boolean deleteInAllClassifications) {
        this.deleteInAllClassifications = deleteInAllClassifications;
    }

    /**
     * If <code>true</code> related taxa with  {@link TaxonRelationshipType} misappliedName
     * will be removed if possible.<BR>
     * It is possible to remove a related taxon if it is not used in any other context, e.g. any
     * other @link {@link TaxonRelationship} or in another @link {@link Classification}
     * @return
     */
    public boolean isDeleteMisappliedNames() {
        return deleteMisappliedNames;
    }
    public void setDeleteMisappliedNames(
            boolean deleteMisappliedNames) {
        this.deleteMisappliedNames = deleteMisappliedNames;
    }

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
     * If <code>true</code> synonyms will be removed if possible but only if {@link #isDeleteSynonymRelations()}
     * is also <code>true</code>.
     * It is possible to remove a synonym if it is not used in any other context
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

    /**
     * @return the deleteConceptRelationships
     */
    public boolean isDeleteConceptRelationships() {
        return deleteConceptRelationships;
    }
    /**
     * @param deleteConceptRelationships the deleteConceptRelationships to set
     */
    public void setDeleteConceptRelationships(boolean deleteConceptRelationships) {
        this.deleteConceptRelationships = deleteConceptRelationships;
    }

    public UUID getClassificationUuid() {
        return classificationUuid;
    }
    public void setClassificationUuid(UUID classificationUuid) {
        this.classificationUuid = classificationUuid;
    }
}