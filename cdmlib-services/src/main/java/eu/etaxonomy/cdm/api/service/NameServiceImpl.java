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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.INameServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.common.IReferencedEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.INomenclaturalStatusDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


@Service
@Transactional(readOnly = true)
public class NameServiceImpl extends IdentifiableServiceBase<TaxonNameBase,ITaxonNameDao> implements INameService {
	static private final Logger logger = Logger.getLogger(NameServiceImpl.class);
	
	@Autowired
	protected ITermVocabularyDao vocabularyDao;
	@Autowired
	protected IOrderedTermVocabularyDao orderedVocabularyDao;
	@Autowired
	@Qualifier("refEntDao")
    protected IReferencedEntityDao<ReferencedEntityBase> referencedEntityDao;
	@Autowired
	private INomenclaturalStatusDao nomStatusDao;
	@Autowired
	private ITypeDesignationDao typeDesignationDao;
	@Autowired
	private IHomotypicalGroupDao homotypicalGroupDao;

	/**
	 * Constructor
	 */
	public NameServiceImpl(){
		if (logger.isDebugEnabled()) { logger.debug("Load NameService Bean"); }
	}

//********************* METHODS ****************************************************************//	
	
	public List getNamesByName(String name){
		return super.findCdmObjectsByTitle(name);
	}
	
	public List getNamesByName(String name, CdmBase sessionObject){
		return super.findCdmObjectsByTitle(name, sessionObject);
	}

	public List findNamesByTitle(String title){
		return super.findCdmObjectsByTitle(title);
	}
	
	public List findNamesByTitle(String title, CdmBase sessionObject){
		return super.findCdmObjectsByTitle(title, sessionObject);
	}
 	
	public List<TaxonNameBase> findByTitle(IIdentifiableEntityServiceConfigurator config) {
		return super.findByTitle(config);
	}
	
	public TaxonNameBase<?,?> getTaxonNameByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid);
	}

	@Transactional(readOnly = false)
	public UUID saveTaxonName(TaxonNameBase taxonName) {
		return super.saveCdmObject(taxonName);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, TaxonNameBase> saveTaxonNameAll(Collection<? extends TaxonNameBase> taxonNameCollection){
		return saveCdmObjectAll(taxonNameCollection);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, HomotypicalGroup> saveAllHomotypicalGroups(Collection<HomotypicalGroup> homotypicalGroups){
		return homotypicalGroupDao.saveAll(homotypicalGroups);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, TypeDesignationBase> saveTypeDesignationAll(Collection<TypeDesignationBase> typeDesignationCollection){
		return typeDesignationDao.saveAll(typeDesignationCollection);
	}

	@Transactional(readOnly = false)
	public Map<UUID, ReferencedEntityBase> saveReferencedEntitiesAll(Collection<ReferencedEntityBase> referencedEntityCollection){
		return referencedEntityDao.saveAll(referencedEntityCollection);
	}
	
	@Transactional(readOnly = false)
	public UUID removeTaxonName(TaxonNameBase taxonName) {
		return super.removeCdmObject(taxonName);
	}

	public List<TaxonNameBase> getAllNames(int limit, int start){
		return dao.list(limit, start);
	}

	public List<NomenclaturalStatus> getAllNomenclaturalStatus(int limit, int start){
		return nomStatusDao.list(limit, start);
	}
	 
	public List<TypeDesignationBase> getAllTypeDesignations(int limit, int start){
		return typeDesignationDao.getAllTypeDesignations(limit, start);
	}
 	
	public List<HomotypicalGroup> getAllHomotypicalGroups(int limit, int start){
		return homotypicalGroupDao.list(limit, start);
	}
	
	public List<RelationshipBase> getAllRelationships(int limit, int start){
		return dao.getAllRelationships(limit, start);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getRankVocabulary()
	 */
	public OrderedTermVocabulary<Rank> getRankVocabulary() {
		String uuidString = "ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b";
		UUID uuid = UUID.fromString(uuidString);
		OrderedTermVocabulary<Rank> rankVocabulary = 
			(OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
		return rankVocabulary;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getNameRelationshipTypeVocabulary()
	 */
	public TermVocabulary<NameRelationshipType> getNameRelationshipTypeVocabulary() {
		String uuidString = "6878cb82-c1a4-4613-b012-7e73b413c8cd";
		UUID uuid = UUID.fromString(uuidString);
		TermVocabulary<NameRelationshipType> nameRelTypeVocabulary = 
			(TermVocabulary)vocabularyDao.findByUuid(uuid);
		return nameRelTypeVocabulary;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getStatusTypeVocabulary()
	 */
	public TermVocabulary<NomenclaturalStatusType> getStatusTypeVocabulary() {
		String uuidString = "bb28cdca-2f8a-4f11-9c21-517e9ae87f1f";
		UUID uuid = UUID.fromString(uuidString);
		TermVocabulary<NomenclaturalStatusType> nomStatusTypeVocabulary = 
			(TermVocabulary)vocabularyDao.findByUuid(uuid);
		return nomStatusTypeVocabulary;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getTypeDesignationStatusVocabulary()
	 */
	public TermVocabulary<SpecimenTypeDesignationStatus> getSpecimenTypeDesignationStatusVocabulary() {
		String uuidString = "ab177bd7-d3c8-4e58-a388-226fff6ba3c2";
		UUID uuid = UUID.fromString(uuidString);
		TermVocabulary<SpecimenTypeDesignationStatus> typeDesigStatusVocabulary = 
			(TermVocabulary)vocabularyDao.findByUuid(uuid);
		return typeDesigStatusVocabulary;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getTypeDesignationStatusVocabulary()
	 */
	public OrderedTermVocabulary<SpecimenTypeDesignationStatus> getSpecimenTypeDesignationVocabulary() {
		String uuidString = "ab177bd7-d3c8-4e58-a388-226fff6ba3c2";
		UUID uuid = UUID.fromString(uuidString);
		OrderedTermVocabulary<SpecimenTypeDesignationStatus> typeDesignationVocabulary = 
			(OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
		return typeDesignationVocabulary;
	}

	public void generateTitleCache() {
		logger.warn("Not yet implemented");
		// TODO Auto-generated method stub
	}

	@Autowired
	protected void setDao(ITaxonNameDao dao) {
		this.dao = dao;
	}

	public Pager<HybridRelationship> getHybridNames(BotanicalName name,	HybridRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countHybridNames(name, type);
		
		List<HybridRelationship> results = new ArrayList<HybridRelationship>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getHybridNames(name, type, pageSize, pageNumber,orderHints,propertyPaths); 
		}
		
		return new DefaultPagerImpl<HybridRelationship>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<NameRelationship> getRelatedNames(TaxonNameBase name,NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countRelatedNames(name, type);
		
		List<NameRelationship> results = new ArrayList<NameRelationship>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getRelatedNames(name, type, pageSize, pageNumber,orderHints,propertyPaths); 
		}
		
		return new DefaultPagerImpl<NameRelationship>(pageNumber, numberOfResults, pageSize, results);
	}

	
	public Pager<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, SpecimenTypeDesignationStatus status, 
			Integer pageSize, Integer pageNumber) {	
		return getTypeDesignations(name, status, pageSize, pageNumber, null);
	}
	
	public Pager<TypeDesignationBase> getTypeDesignations(TaxonNameBase name, SpecimenTypeDesignationStatus status, 
				Integer pageSize, Integer pageNumber, List<String> propertyPaths){
        Integer numberOfResults = dao.countTypeDesignations(name, status);
		
		List<TypeDesignationBase> results = new ArrayList<TypeDesignationBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getTypeDesignations(name, status, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<TypeDesignationBase>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<TaxonNameBase> searchNames(String uninomial,String infraGenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber) {
        Integer numberOfResults = dao.countNames(uninomial, infraGenericEpithet, specificEpithet, infraspecificEpithet, rank);
		
		List<TaxonNameBase> results = new ArrayList<TaxonNameBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.searchNames(uninomial, infraGenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<TaxonNameBase>(pageNumber, numberOfResults, pageSize, results);
	}

}
