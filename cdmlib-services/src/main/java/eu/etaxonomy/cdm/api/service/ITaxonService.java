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
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;


public interface ITaxonService extends IIdentifiableEntityService<TaxonBase>{
	
	/** */
	public abstract TaxonBase getTaxonByUuid(UUID uuid);

	/** save a taxon and return its UUID**/
	public abstract UUID saveTaxon(TaxonBase taxon);

	/** save a collection of  taxa and return its UUID**/
	public abstract Map<UUID, TaxonBase> saveTaxonAll(Collection<TaxonBase> taxonCollection);

	
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
	public abstract List<TaxonBase> getAllTaxa(int limit, int start);
	
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
	 * Computes all taxon relationships.
	 * @param limit
	 * @param start
	 * @return
	 */
    //public abstract List<TaxonRelationship> getAllTaxonRelationships(int limit, int start);

	/**
	 * Computes all synonym relationships.
	 * @param limit
	 * @param start
	 * @return
	 */
    //public abstract List<SynonymRelationship> getAllSynonymRelationships(int limit, int start);
    
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
