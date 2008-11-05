/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;


public interface ITaxonService extends IIdentifiableEntityService<TaxonBase>{
	
	/** */
	public abstract TaxonBase getTaxonByUuid(UUID uuid);

	/** save a taxon and return its UUID**/
	public abstract UUID saveTaxon(TaxonBase taxon);

	/** save a taxon and return its UUID**/
	//public abstract UUID saveTaxon(TaxonBase taxon, TransactionStatus txStatus);
	
	/** save a collection of taxa and return its UUID**/
	public abstract Map<UUID, ? extends TaxonBase> saveTaxonAll(Collection<? extends TaxonBase> taxonCollection);

	
	/** delete a taxon and return its UUID**/
	public abstract UUID removeTaxon(TaxonBase taxon);
	
	/**
	 * Computes all taxon bases.
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<Synonym> getAllSynonyms(int limit, int start);
	
	/**
	 * Computes all taxon bases.
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<Taxon> getAllTaxa(int limit, int start);
	
	/**
	 * Computes all taxon bases.
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<TaxonBase> getAllTaxonBases(int limit, int start);
	
	/**
	 * Computes all Taxon instances that do not have a taxonomic parent and has at least one child.
	 * @param sec The concept reference that the taxon belongs to
	 * @return The List<Taxon> of root taxa.
	 */
	public abstract List<Taxon> getRootTaxa(ReferenceBase sec);

	/**
	 * Computes all Taxon instances that do not have a taxonomic parent.
	 * @param sec The concept reference that the taxon belongs to
	 * 
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @return The List<Taxon> of root taxa.
	 */
	public abstract List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, boolean onlyWithChildren);
	
	/**
	 * Computes all relationships.
	 * @param limit
	 * @param start
	 * @return
	 */
    public abstract List<RelationshipBase> getAllRelationships(int limit, int start);

    /** */
	public abstract List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec);
		
	public Synonym makeTaxonSynonym (Taxon oldTaxon, Taxon newAcceptedTaxon, SynonymRelationshipType synonymType, ReferenceBase citation, String citationMicroReference);
	
}
