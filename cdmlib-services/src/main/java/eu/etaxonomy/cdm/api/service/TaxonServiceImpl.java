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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.ITreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


@Service
@Transactional(readOnly = true)
public class TaxonServiceImpl extends IdentifiableServiceBase<TaxonBase,ITaxonDao> implements ITaxonService{
	private static final Logger logger = Logger.getLogger(TaxonServiceImpl.class);

	@Autowired
	private ITaxonNameDao nameDao;
	

	@Autowired
	private IOrderedTermVocabularyDao orderedVocabularyDao;
	@Autowired
	private IDescriptionDao descriptionDao;
	@Autowired
	private BeanInitializer defaultBeanInitializer;
	
	private Comparator<? super TaxonNode> taxonNodeComparator;
	@Autowired
	public void setTaxonNodeComparator(ITaxonNodeComparator<? super TaxonNode> taxonNodeComparator){
		this.taxonNodeComparator = (Comparator<? super TaxonNode>) taxonNodeComparator;
	}
	
	/**
	 * Constructor
	 */
	public TaxonServiceImpl(){
		if (logger.isDebugEnabled()) { logger.debug("Load TaxonService Bean"); }
	}

	/**
	 * FIXME Candidate for harmonization
	 * rename searchByName ? 
	 */
	public List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec) {
		return dao.getTaxaByName(name, sec);
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * list(Synonym.class, ...)
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllSynonyms(int, int)
	 */
	public List<Synonym> getAllSynonyms(int limit, int start) {
		return dao.getAllSynonyms(limit, start);
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * list(Taxon.class, ...)
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllTaxa(int, int)
	 */
	public List<Taxon> getAllTaxa(int limit, int start) {
		return dao.getAllTaxa(limit, start);
	}


	/**
	 * FIXME Candidate for harmonization
	 * merge with getRootTaxa(ReferenceBase sec, ..., ...)
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec){
		return getRootTaxa(sec, CdmFetch.FETCH_CHILDTAXA(), true);
	}

	/**
	 * FIXME Candidate for harmonization
	 * merge with getRootTaxa(ReferenceBase sec, ..., ...)
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase, boolean)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, boolean onlyWithChildren) {
		if (cdmFetch == null){
			cdmFetch = CdmFetch.NO_FETCH();
		}
		return dao.getRootTaxa(sec, cdmFetch, onlyWithChildren, false);
	}
	
	/**
 	 * FIXME Candidate for harmonization
	 * merge with getRootTaxa(ReferenceBase sec, ..., ...)
	 *  (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase, boolean, boolean)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, boolean onlyWithChildren,
			boolean withMisapplications) {
		return dao.getRootTaxa(sec, null, onlyWithChildren, withMisapplications);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.name.Rank, eu.etaxonomy.cdm.model.reference.ReferenceBase, boolean, boolean)
	 */
	public List<Taxon> getRootTaxa(Rank rank, ReferenceBase sec, boolean onlyWithChildren,
			boolean withMisapplications, List<String> propertyPaths) {
		return dao.getRootTaxa(rank, sec, null, onlyWithChildren, withMisapplications, propertyPaths);
	}

	public List<RelationshipBase> getAllRelationships(int limit, int start){
		return dao.getAllRelationships(limit, start);
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * is this the same as termService.getVocabulary(VocabularyEnum.TaxonRelationshipType) ? 
	 */
	@Deprecated
	public OrderedTermVocabulary<TaxonRelationshipType> getTaxonRelationshipTypeVocabulary() {
		
		String taxonRelTypeVocabularyId = "15db0cf7-7afc-4a86-a7d4-221c73b0c9ac";
		UUID uuid = UUID.fromString(taxonRelTypeVocabularyId);
		OrderedTermVocabulary<TaxonRelationshipType> taxonRelTypeVocabulary = 
			(OrderedTermVocabulary)orderedVocabularyDao.findByUuid(uuid);
		return taxonRelTypeVocabulary;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#makeTaxonSynonym(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Taxon)
	 */
	@Transactional(readOnly = false)
	public Synonym makeTaxonSynonym(Taxon oldTaxon, Taxon newAcceptedTaxon, SynonymRelationshipType synonymRelationshipType, ReferenceBase citation, String citationMicroReference) {
		if (oldTaxon == null || newAcceptedTaxon == null || oldTaxon.getName() == null){
			throw new IllegalArgumentException();
		}
		
		// Move oldTaxon to newTaxon
		TaxonNameBase<?,?> synonymName = oldTaxon.getName();
		if (synonymRelationshipType == null){
			if (synonymName.isHomotypic(newAcceptedTaxon.getName())){
				synonymRelationshipType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
			}else{
				//TODO synonymType 
				synonymRelationshipType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
			}
		}
		SynonymRelationship synRel = newAcceptedTaxon.addSynonymName(synonymName, synonymRelationshipType, citation, citationMicroReference);
		
		//Move Synonym Relations to new Taxon
		for(SynonymRelationship synRelation : oldTaxon.getSynonymRelations()){
			newAcceptedTaxon.addSynonym(synRelation.getSynonym(), synRelation.getType(), 
					synRelation.getCitation(), synRelation.getCitationMicroReference());
		}

		//Move Taxon RelationShips to new Taxon
		Set<TaxonRelationship> removableTaxonRels = new HashSet<TaxonRelationship>();
		for(TaxonRelationship taxonRelation : oldTaxon.getTaxonRelations()){
			//CHILDREN
			if (taxonRelation.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())){
				if (taxonRelation.getFromTaxon() == oldTaxon){
					removableTaxonRels.add(taxonRelation);
//					oldTaxon.removeTaxonRelation(taxonRelation);
				}else if(taxonRelation.getToTaxon() == oldTaxon){
					newAcceptedTaxon.addTaxonomicChild(taxonRelation.getFromTaxon(), taxonRelation.getCitation(), taxonRelation.getCitationMicroReference());
					removableTaxonRels.add(taxonRelation);
//					oldTaxon.removeTaxonRelation(taxonRelation);
				}else{
					logger.warn("Taxon is not part of its own Taxonrelationship");
				}
			}
			//MISAPPLIED NAMES
			if (taxonRelation.getType().equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
				if (taxonRelation.getFromTaxon() == oldTaxon){
					newAcceptedTaxon.addMisappliedName(taxonRelation.getToTaxon(), taxonRelation.getCitation(), taxonRelation.getCitationMicroReference());
					removableTaxonRels.add(taxonRelation);
//					oldTaxon.removeTaxonRelation(taxonRelation);
				}else if(taxonRelation.getToTaxon() == oldTaxon){
					newAcceptedTaxon.addMisappliedName(taxonRelation.getFromTaxon(), taxonRelation.getCitation(), taxonRelation.getCitationMicroReference());
					removableTaxonRels.add(taxonRelation);
//					oldTaxon.removeTaxonRelation(taxonRelation);
				}else{
					logger.warn("Taxon is not part of its own Taxonrelationship");
				}
			}
			//Concept Relationships
			//FIXME implement
//			if (taxonRelation.getType().equals(TaxonRelationshipType.MISAPPLIEDNAMEFOR())){
//				if (taxonRelation.getFromTaxon() == oldTaxon){
//					newAcceptedTaxon.addMisappliedName(taxonRelation.getToTaxon(), taxonRelation.getCitation(), taxonRelation.getCitationMicroReference());
//			        removableTaxonRels.add(taxonRelation);
//				}else if(taxonRelation.getToTaxon() == oldTaxon){
//					newAcceptedTaxon.addMisappliedName(taxonRelation.getFromTaxon(), taxonRelation.getCitation(), taxonRelation.getCitationMicroReference());
//	                removableTaxonRels.add(taxonRelation);
//				}else{
//					logger.warn("Taxon is not part of its own Taxonrelationship");
//				}
//			}
		}
		
		for(TaxonRelationship taxonRel : removableTaxonRels) {
			oldTaxon.removeTaxonRelation(taxonRel);
		}
		
		//Move Descriptions to new Taxon
		for(TaxonDescription taxDescription : oldTaxon.getDescriptions()){
			newAcceptedTaxon.addDescription(taxDescription);
		}
		//delete old Taxon
		this.dao.saveOrUpdate(newAcceptedTaxon);
//		FIXME implement
//		this.dao.delete(oldTaxon);
		
		//return
//		this.dao.flush();
		return synRel.getSynonym();
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#swapSynonymWithAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym)
	 */
	@Transactional(readOnly = false)
	public void makeSynonymAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon, SynonymRelationshipType synonymRelationshipType){
		
		// create a new synonym with the old acceptedName
		TaxonNameBase oldAcceptedTaxonName = acceptedTaxon.getName();
		
		// remove synonym from oldAcceptedTaxon
		acceptedTaxon.removeSynonym(synonym);
		
		// make synonym name the accepted taxons name
		TaxonNameBase newAcceptedTaxonName = synonym.getName();
		acceptedTaxon.setName(newAcceptedTaxonName);
		
		// add the new synonym to the acceptedTaxon
		if(synonymRelationshipType == null){
			synonymRelationshipType = SynonymRelationshipType.SYNONYM_OF();
		}
		
		acceptedTaxon.addSynonymName(oldAcceptedTaxonName, synonymRelationshipType);
	}

	public void generateTitleCache() {
		generateTitleCache(true);
	}
	//TODO
	public void generateTitleCache(boolean forceProtected) {
		logger.warn("generateTitleCache not yet fully implemented!");
	}

	@Autowired
	protected void setDao(ITaxonDao dao) {
		this.dao = dao;
	}

	public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);
		
		List<TaxonBase> results = new ArrayList<TaxonBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<TaxonBase>(pageNumber, numberOfResults, pageSize, results);
	}

	public List<TaxonRelationship> listToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
		Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);
		
		List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo); 
		}
		return results;
	}
	
	public Pager<TaxonRelationship> pageToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);
		
		List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo); 
		}
		return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
	}
	
	public List<TaxonRelationship> listFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
		Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);
		
		List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom); 
		}
		return results;
	}
	
	public Pager<TaxonRelationship> pageFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);
		
		List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom); 
		}
		return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<SynonymRelationship> getSynonyms(Taxon taxon,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countSynonyms(taxon, type);
		
		List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getSynonyms(taxon, type, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
	}
	
	public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths){
		Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
		return t.getHomotypicSynonymsByHomotypicGroup();
	}
	
	public List<List<Synonym>> getHeterotypicSynonymyGroups(Taxon taxon, List<String> propertyPaths){
		Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
		List<HomotypicalGroup> hsgl = t.getHeterotypicSynonymyGroups();
		List<List<Synonym>> heterotypicSynonymyGroups = new ArrayList<List<Synonym>>(hsgl.size());
		for(HomotypicalGroup hsg : hsgl){
			heterotypicSynonymyGroups.add(hsg.getSynonymsInGroup(t.getSec()));
		}
		return heterotypicSynonymyGroups;
	}

	public Pager<TaxonBase> search(Class<? extends TaxonBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.count(clazz,queryString);
		
		List<TaxonBase> results = new ArrayList<TaxonBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.search(clazz,queryString, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<TaxonBase>(pageNumber, numberOfResults, pageSize, results);
	}

	
	public Pager<IdentifiableEntity> findTaxaAndNames(ITaxonServiceConfigurator configurator) {
		
		List<IdentifiableEntity> results = new ArrayList<IdentifiableEntity>();
		int numberOfResults = 0; // overall number of results (as opposed to number of results per page)
		List<TaxonBase> taxa = null; 

		// Taxa and synonyms
		long numberTaxaResults = 0L;
		
		Class<? extends TaxonBase> clazz = null;
		if (configurator.isDoTaxa() && configurator.isDoSynonyms()) {
			clazz = TaxonBase.class;
		} else if(configurator.isDoTaxa()) {
			clazz = Taxon.class;
		} else if (configurator.isDoSynonyms()) {
			clazz = Synonym.class;
		}
		
		if(clazz != null){
			numberTaxaResults = 
				dao.countTaxaByName(clazz, 
					configurator.getSearchString(), configurator.getTaxonomicTree(), configurator.getMatchMode(),
					configurator.getNamedAreas());
			if(numberTaxaResults > configurator.getPageSize() * configurator.getPageNumber()){ // no point checking again if less results
				taxa = dao.getTaxaByName(clazz, 
					configurator.getSearchString(), configurator.getTaxonomicTree(), configurator.getMatchMode(),
					configurator.getNamedAreas(), configurator.getPageSize(), 
					configurator.getPageNumber(), configurator.getTaxonPropertyPath());
			}
		}

		if (logger.isDebugEnabled()) { logger.debug(numberTaxaResults + " matching taxa counted"); }
		
		if(taxa != null){
			results.addAll(taxa);
		}
		
		numberOfResults += numberTaxaResults;
		
		// Names without taxa 
		
		if (configurator.isDoNamesWithoutTaxa()) {
            int numberNameResults = 0;
            //FIXME implement search by area
			List<? extends TaxonNameBase<?,?>> names = 
				nameDao.findByName(configurator.getSearchString(), configurator.getMatchMode(), 
						configurator.getPageSize(), configurator.getPageNumber(), null, null);
			if (logger.isDebugEnabled()) { logger.debug(names.size() + " matching name(s) found"); }
			if (names.size() > 0) {
				for (TaxonNameBase<?,?> taxonName : names) {
					if (taxonName.getTaxonBases().size() == 0) {
						results.add(taxonName);
						numberNameResults++;
					}
				}
				if (logger.isDebugEnabled()) { logger.debug(numberNameResults + " matching name(s) without taxa found"); }
				numberOfResults += numberNameResults;
			}
		}
		
		// Taxa from common names
		// FIXME the matching common names also must be returned
		// FIXME implement search by area
		if (configurator.isDoTaxaByCommonNames()) {
			int numberCommonNameResults = 0;
			List<CommonTaxonName> commonTaxonNames = 
				descriptionDao.searchDescriptionByCommonName(configurator.getSearchString(), 
						configurator.getMatchMode(), configurator.getPageSize(), configurator.getPageNumber());
			if (logger.isDebugEnabled()) { logger.debug(commonTaxonNames.size() + " matching common name(s) found"); }
			if (commonTaxonNames.size() > 0) {
				for (CommonTaxonName commonTaxonName : commonTaxonNames) {
					DescriptionBase description = commonTaxonName.getInDescription();
					description = HibernateProxyHelper.deproxy(description, DescriptionBase.class);
					if (description instanceof TaxonDescription) {
						TaxonDescription taxonDescription = HibernateProxyHelper.deproxy(description, TaxonDescription.class);
						Taxon taxon = taxonDescription.getTaxon();
						taxon = HibernateProxyHelper.deproxy(taxon, Taxon.class);
						if (!results.contains(taxon) && !taxon.isMisappliedName()) {
							defaultBeanInitializer.initialize(taxon, configurator.getTaxonPropertyPath());
							results.add(taxon);
							numberCommonNameResults++;
						}
					} else {
						logger.warn("Description of " + commonTaxonName.getName() + " is not an instance of TaxonDescription");
					}
				}
				numberOfResults += numberCommonNameResults;
			} 
		}
		
		//FIXME does not work any more after model change
		logger.warn("Sort does currently not work on identifiable entities due to model changes (duplicated implementation of the Comparable interface).");
		//Collections.sort(results);
		return new DefaultPagerImpl<IdentifiableEntity>
			(configurator.getPageNumber(), numberOfResults, configurator.getPageSize(), results);
	}
	
	public List<UuidAndTitleCache<TaxonBase>> getTaxonUuidAndTitleCache(){
		return dao.getUuidAndTitleCache();
	}

	public List<MediaRepresentation> getAllMedia(Taxon taxon, int size, int height, int widthOrDuration, String[] mimeTypes){
		List<MediaRepresentation> medRep = new ArrayList<MediaRepresentation>();
		taxon = (Taxon)dao.load(taxon.getUuid());
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		for (TaxonDescription taxDesc: descriptions){
			Set<DescriptionElementBase> elements = taxDesc.getElements();
			for (DescriptionElementBase descElem: elements){
				for(Media media : descElem.getMedia()){
									
					//find the best matching representation
					medRep.add(media.findBestMatchingRepresentation(size, height, widthOrDuration, mimeTypes));
					
				}
			}
		}
		return medRep;
	}
}
