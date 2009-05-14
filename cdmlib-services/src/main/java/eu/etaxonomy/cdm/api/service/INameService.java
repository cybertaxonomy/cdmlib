// $Id$
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

import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.INameServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface INameService extends IIdentifiableEntityService<TaxonNameBase> {

	/**
	 * FIXME candidate for harmonization?
	 * @param uuid
	 * @return
	 */
	public TaxonNameBase getTaxonNameByUuid(UUID uuid);

	/**
	 * FIXME candidate for harmonization?
	 * @param taxonName
	 * @return
	 */
	public UUID saveTaxonName(TaxonNameBase taxonName);

	/**
	 * FIXME candidate for harmonization?
	 * Saves a collection of  TaxonNames and return its UUID@param taxonCollection
	 * @return
	 */
	public Map<UUID, TaxonNameBase> saveTaxonNameAll(Collection<? extends TaxonNameBase> taxonCollection);

	public Map<UUID, TypeDesignationBase> saveTypeDesignationAll(Collection<TypeDesignationBase> typeDesignationCollection);

	public Map<UUID, ReferencedEntityBase> saveReferencedEntitiesAll(Collection<ReferencedEntityBase> referencedEntityCollection);
		
	public Map<UUID, HomotypicalGroup> saveAllHomotypicalGroups(Collection<HomotypicalGroup> homotypicalGroups);
	
	/**
	 * FIXME candidate for harmonization?
	 * @param limit
	 * @param start
	 * @return
	 */
	public List<TaxonNameBase> getAllNames(int limit, int start);

	public List<NomenclaturalStatus> getAllNomenclaturalStatus(int limit, int start);

	public List<TypeDesignationBase> getAllTypeDesignations(int limit, int start);
	
	/**
	 * @param name
	 * @return
	 */
	public List<TaxonNameBase> getNamesByName(String name);

	/**
	 * @param name
	 * @param sessionObject An object that is attached to the session before executing the query
	 * @return
	 */
	public List getNamesByName(String name, CdmBase sessionObject);
	
	// TODO: Remove getNamesByName() methods. Use findNamesByTitle() instead.
	
	// FIXME candidate for harmonization?
	public List findNamesByTitle(String title);
	
	public List findNamesByTitle(String title, CdmBase sessionObject);
	
	/**
	 * Finds taxon name(s) according to specifications in configurator
	 * 
	 * @param configurator
	 * @return
	 */
	public List<TaxonNameBase> findByTitle(IIdentifiableEntityServiceConfigurator config);

    public List<HomotypicalGroup> getAllHomotypicalGroups(int limit, int start);

    public List<RelationshipBase> getAllRelationships(int limit, int start);
    
	/**
	 * Returns all Ranks
	 * @return
	 */
	public OrderedTermVocabulary<Rank> getRankVocabulary();
	
	/**
	 * Returns all NomenclaturalStatusTypes
	 * @return
	 */
	public TermVocabulary<NomenclaturalStatusType> getStatusTypeVocabulary();
	
	/**
	 * Returns TypeDesignationStatus vocabulary
	 * @return
	 */
	public TermVocabulary<SpecimenTypeDesignationStatus> getSpecimenTypeDesignationStatusVocabulary();
		
	/**
	 * Returns TypeDesignationStatus ordered vocabulary
	 * @return
	 */
	public OrderedTermVocabulary<SpecimenTypeDesignationStatus> getSpecimenTypeDesignationVocabulary();

	/**
	 * Returns all NameRelationshipTypes
	 * @return
	 */
	public TermVocabulary<NameRelationshipType> getNameRelationshipTypeVocabulary();
	
	/**
	 * Return a List of relationships related to this name, optionally filtered 
	 * by relationship type
	 * 
	 * @param name the name
	 * @param type the relationship type (or null to return all relationships) 
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of NameRelationship instances
	 */
	public Pager<NameRelationship> getRelatedNames(TaxonNameBase name,  NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Return a List of hybrids related to this name, optionally filtered 
	 * by hybrid relationship type
	 * 
	 * @param name the name
	 * @param type the hybrid relationship type (or null to return all hybrids) 
	 * @param pageSize The maximum number of hybrid relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of HybridRelationship instances
	 */
	public Pager<HybridRelationship> getHybridNames(BotanicalName name, HybridRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Return a List of types related to this name, optionally filtered 
	 * by type designation status
	 * 
	 * @param name the name
	 * @param status the type designation status (or null to return all types) 
	 * @param pageSize The maximum number of types returned (can be null for all types)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of TypeDesignationBase instances
	 */
	public Pager<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, 
			SpecimenTypeDesignationStatus status, Integer pageSize, Integer pageNumber);
	
	public Pager<TypeDesignationBase> getTypeDesignations(TaxonNameBase name,
			SpecimenTypeDesignationStatus status, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
	
	
	/**
	 * Returns a List of TaxonNameBase instances that match the properties passed
	 * 
	 * @param uninomial
	 * @param infraGenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @param pageSize The maximum number of names returned (can be null for all names)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of TaxonNameBase instances
	 */
	public Pager<TaxonNameBase> searchNames(String uninomial, String infraGenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber);
}