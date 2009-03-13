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
import java.util.Collections;
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
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.name.INomenclaturalStatusDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;


@Service
@Transactional(readOnly = true)
public class TaxonServiceImpl extends IdentifiableServiceBase<TaxonBase,ITaxonDao> implements ITaxonService {
	private static final Logger logger = Logger.getLogger(TaxonServiceImpl.class);

	@Autowired
	private ITaxonNameDao nameDao;
	@Autowired
	private IOrderedTermVocabularyDao orderedVocabularyDao;
	
	public TaxonBase getTaxonByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid); 
	}

	@Transactional(readOnly = false)
	public UUID saveTaxon(TaxonBase taxon) {
		return super.saveCdmObject(taxon);
	}

	//@Transactional(readOnly = false)
	public UUID saveTaxon(TaxonBase taxon, TransactionStatus txStatus) {
		
		//return super.saveCdmObject(taxon, txStatus);
		return super.saveCdmObject(taxon);
	}
	
	
	@Transactional(readOnly = false)
	public Map<UUID, ? extends TaxonBase> saveTaxonAll(Collection<? extends TaxonBase> taxonCollection){
		return saveCdmObjectAll(taxonCollection);
	}

	@Transactional(readOnly = false)
	public UUID removeTaxon(TaxonBase taxon) {
		return super.removeCdmObject(taxon);
	}

	public List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec) {
		return dao.getTaxaByName(name, sec);
	}

	public List<TaxonBase> getAllTaxonBases(int limit, int start){
		return dao.list(limit, start);
	}

	public List<Taxon> getAllTaxa(int limit, int start){
		return dao.getAllTaxa(limit, start);
	}
	
	public List<Synonym> getAllSynonyms(int limit, int start) {
		return dao.getAllSynonyms(limit, start);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec){
		return getRootTaxa(sec, CdmFetch.FETCH_CHILDTAXA(), true);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase, boolean)
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, boolean onlyWithChildren) {
		if (cdmFetch == null){
			cdmFetch = CdmFetch.NO_FETCH();
		}
		return dao.getRootTaxa(sec, cdmFetch, onlyWithChildren, false);
	}

	
	/* (non-Javadoc)
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
			boolean withMisapplications) {
		return dao.getRootTaxa(rank, sec, null, onlyWithChildren, withMisapplications);
	}

	public List<RelationshipBase> getAllRelationships(int limit, int start){
		return dao.getAllRelationships(limit, start);
	}
	
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
	public Synonym makeTaxonSynonym(Taxon oldTaxon, Taxon newAcceptedTaxon, SynonymRelationshipType synonymType, ReferenceBase citation, String citationMicroReference) {
		if (oldTaxon == null || newAcceptedTaxon == null || oldTaxon.getName() == null){
			return null;
		}
		
		// Move oldTaxon to newTaxon
		TaxonNameBase<?,?> synonymName = oldTaxon.getName();
		if (synonymType == null){
			if (synonymName.isHomotypic(newAcceptedTaxon.getName())){
				synonymType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
			}else{
				//TODO synonymType 
				synonymType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
			}
		}
		SynonymRelationship synRel = newAcceptedTaxon.addSynonymName(synonymName, synonymType, citation, citationMicroReference);
		
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


	public void generateTitleCache() {
		generateTitleCache(true);
	}
	//TODO
	public void generateTitleCache(boolean forceProtected) {
		logger.warn("generateTitleCache not yet fully implemented!");
//		for (TaxonBase tb : taxonDao.getAllTaxa(null,null)){
//			logger.warn("Old taxon title: " + tb.getTitleCache());
//			if (forceProtected || !tb.isProtectedTitleCache() ){
//				tb.setTitleCache(tb.generateTitle(), false);
//				taxonDao.update(tb);
//				logger.warn("New title: " + tb.getTitleCache());
//			}
//		}
		
	}

	@Autowired
	protected void setDao(ITaxonDao dao) {
		this.dao = dao;
	}

	public Pager<TaxonBase> findTaxaByName(Boolean accepted, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(accepted, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);
		
		List<TaxonBase> results = new ArrayList<TaxonBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.findTaxaByName(accepted, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<TaxonBase>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<TaxonRelationship> getRelatedTaxa(Taxon taxon,	TaxonRelationshipType type, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countRelatedTaxa(taxon, type);
		
		List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getRelatedTaxa(taxon, type, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<SynonymRelationship> getSynonyms(Taxon taxon,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countSynonyms(taxon, type);
		
		List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getSynonyms(taxon, type, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<TaxonBase> searchTaxa(String queryString, Boolean accepted, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countTaxa(queryString, accepted);
		
		List<TaxonBase> results = new ArrayList<TaxonBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.searchTaxa(queryString, accepted, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<TaxonBase>(pageNumber, numberOfResults, pageSize, results);
	}

	
	public Pager<IdentifiableEntity> findTaxaAndNames(ITaxonServiceConfigurator configurator) {
		
		List<IdentifiableEntity> results = new ArrayList<IdentifiableEntity>();
		int numberOfResults = 0;

		if(configurator.isDoTaxa()) {
			int numberTaxaResults = dao.countTaxaByName(configurator.getSearchString(), true, configurator.getSec());
			if (logger.isDebugEnabled()) { logger.debug(numberTaxaResults + " taxa counted"); }
			if (numberTaxaResults > 0) {
				List<TaxonBase> taxa = 
					dao.getTaxaByName(configurator.getSearchString(), true, configurator.getSec());
				if (!results.addAll(taxa)) {
					logger.warn("Problem adding taxa to result");
				}
				numberOfResults += numberTaxaResults;
			}
		}

		if(configurator.isDoSynonyms()) {
			int numberSynonymResults = dao.countTaxaByName(configurator.getSearchString(), false, configurator.getSec());
			if (logger.isDebugEnabled()) { logger.debug(numberSynonymResults + " synonyms counted"); }
			if (numberSynonymResults > 0) {
				List<TaxonBase> synonyms = 
					dao.getTaxaByName(configurator.getSearchString(), false, configurator.getSec());
				if (!results.addAll(synonyms)) {
					logger.warn("Problem adding synonyms to result");
				}
				numberOfResults += numberSynonymResults;
			}
		}

		if (configurator.isDoNamesWithoutTaxa()) {
			int numberNameResults = nameDao.countNames(configurator.getSearchString());
			// TODO: Implement and use a count-method that counts names without taxa
			if (logger.isDebugEnabled()) { logger.debug(numberNameResults + " names counted"); }
			if (numberNameResults > 0) {
				List<TaxonNameBase<?,?>> names = 
					nameDao.searchNames(configurator.getSearchString(), null, null);
				for (TaxonNameBase<?,?> taxonName : names) {
					if (taxonName.getTaxonBases().size() == 0) {
						if (!results.add(taxonName)) {
							logger.warn("Problem adding taxon name " + taxonName.getTitleCache() + " to result");
						} else {
							numberNameResults++;
						}
					}
				}
				numberOfResults += numberNameResults;
			}
		}
		Collections.sort(results);
		return new DefaultPagerImpl<IdentifiableEntity>
			(configurator.getPageNumber(), numberOfResults, configurator.getPageSize(), results);
	}
	
}
