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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.DeleteException;
import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.api.service.config.NameDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.IOrderedTermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;


/**
 * @author a.kohlbecker
 * @date 10.09.2010
 *
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TaxonServiceImpl extends IdentifiableServiceBase<TaxonBase,ITaxonDao> implements ITaxonService{
    private static final Logger logger = Logger.getLogger(TaxonServiceImpl.class);

	@Autowired
	private ITaxonNameDao nameDao;

	@Autowired
	private IOrderedTermVocabularyDao orderedVocabularyDao;

	@Autowired
	private INameService nameService;
	
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
    public List<TaxonBase> searchTaxaByName(String name, Reference sec) {
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
     * merge with getRootTaxa(Reference sec, ..., ...)
     *  (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.reference.Reference, boolean)
     */
    public List<Taxon> getRootTaxa(Reference sec, CdmFetch cdmFetch, boolean onlyWithChildren) {
        if (cdmFetch == null){
            cdmFetch = CdmFetch.NO_FETCH();
        }
        return dao.getRootTaxa(sec, cdmFetch, onlyWithChildren, false);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getRootTaxa(eu.etaxonomy.cdm.model.name.Rank, eu.etaxonomy.cdm.model.reference.Reference, boolean, boolean)
     */
    public List<Taxon> getRootTaxa(Rank rank, Reference sec, boolean onlyWithChildren,boolean withMisapplications, List<String> propertyPaths) {
        return dao.getRootTaxa(rank, sec, null, onlyWithChildren, withMisapplications, propertyPaths);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllRelationships(int, int)
     */
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

    
    
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#swapSynonymWithAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym)
	 */
	@Transactional(readOnly = false)
	public void swapSynonymAndAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon){
		
		TaxonNameBase<?,?> synonymName = synonym.getName();
		synonymName.removeTaxonBase(synonym);
		TaxonNameBase<?,?> taxonName = acceptedTaxon.getName();
		taxonName.removeTaxonBase(acceptedTaxon);
		
		synonym.setName(taxonName);
		acceptedTaxon.setName(synonymName);
		
		// the accepted taxon needs a new uuid because the concept has changed
		// FIXME this leads to an error "HibernateException: immutable natural identifier of an instance of eu.etaxonomy.cdm.model.taxon.Taxon was altered"
		//acceptedTaxon.setUuid(UUID.randomUUID());
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#changeSynonymToAcceptedTaxon(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)
	 */
	//TODO correct delete handling still needs to be implemented / checked
	@Override
	@Transactional(readOnly = false)
	public Taxon changeSynonymToAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon, boolean deleteSynonym, boolean copyCitationInfo, Reference citation, String microCitation) throws IllegalArgumentException{
		
		TaxonNameBase<?,?> acceptedName = acceptedTaxon.getName();
		TaxonNameBase<?,?> synonymName = synonym.getName();
		HomotypicalGroup synonymHomotypicGroup = synonymName.getHomotypicalGroup();
		
		//check synonym is not homotypic
		if (acceptedName.getHomotypicalGroup().equals(synonymHomotypicGroup)){
			String message = "The accepted taxon and the synonym are part of the same homotypical group and therefore can not be both accepted.";
			throw new IllegalArgumentException(message);
		}
		
		Taxon newAcceptedTaxon = Taxon.NewInstance(synonymName, acceptedTaxon.getSec());
		
		SynonymRelationshipType relTypeForGroup = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
		List<Synonym> heteroSynonyms = acceptedTaxon.getSynonymsInGroup(synonymHomotypicGroup);
		
		for (Synonym heteroSynonym : heteroSynonyms){
			if (synonym.equals(heteroSynonym)){
				acceptedTaxon.removeSynonym(heteroSynonym, false);
			}else{
				//move synonyms in same homotypic group to new accepted taxon
				heteroSynonym.replaceAcceptedTaxon(newAcceptedTaxon, relTypeForGroup, copyCitationInfo, citation, microCitation);
			}
		}

		//synonym.getName().removeTaxonBase(synonym);
		//TODO correct delete handling still needs to be implemented / checked
		if (deleteSynonym){
//			deleteSynonym(synonym, taxon, false);
			try {
				this.dao.flush();
				this.delete(synonym);
				
			} catch (Exception e) {
				logger.info("Can't delete old synonym from database");
			}
		}
		
		return newAcceptedTaxon;
	}
	
	
	public Taxon changeSynonymToRelatedTaxon(Synonym synonym, Taxon toTaxon, TaxonRelationshipType taxonRelationshipType, Reference citation, String microcitation){
		
		// Get name from synonym
		TaxonNameBase<?, ?> synonymName = synonym.getName();
		
		// remove synonym from taxon
		toTaxon.removeSynonym(synonym);
		
		// Create a taxon with synonym name
		Taxon fromTaxon = Taxon.NewInstance(synonymName, null);
		
		// Add taxon relation 
		fromTaxon.addTaxonRelation(toTaxon, taxonRelationshipType, citation, microcitation);
				
		// since we are swapping names, we have to detach the name from the synonym completely. 
		// Otherwise the synonym will still be in the list of typified names.
		synonym.getName().removeTaxonBase(synonym);
		
		return fromTaxon;
	}
	

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#changeHomotypicalGroupOfSynonym(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.name.HomotypicalGroup, eu.etaxonomy.cdm.model.taxon.Taxon, boolean, boolean)
     */
    @Transactional(readOnly = false)
    @Override
    public void changeHomotypicalGroupOfSynonym(Synonym synonym, HomotypicalGroup newHomotypicalGroup, Taxon targetTaxon,
                        boolean removeFromOtherTaxa, boolean setBasionymRelationIfApplicable){
        // Get synonym name
        TaxonNameBase synonymName = synonym.getName();
        HomotypicalGroup oldHomotypicalGroup = synonymName.getHomotypicalGroup();


        // Switch groups
        oldHomotypicalGroup.removeTypifiedName(synonymName);
        newHomotypicalGroup.addTypifiedName(synonymName);

        //remove existing basionym relationships
        synonymName.removeBasionyms();

        //add basionym relationship
        if (setBasionymRelationIfApplicable){
            Set<TaxonNameBase> basionyms = newHomotypicalGroup.getBasionyms();
            for (TaxonNameBase basionym : basionyms){
                synonymName.addBasionym(basionym);
            }
        }

        //set synonym relationship correctly
//			SynonymRelationship relToTaxon = null;
        boolean relToTargetTaxonExists = false;
        Set<SynonymRelationship> existingRelations = synonym.getSynonymRelations();
        for (SynonymRelationship rel : existingRelations){
            Taxon acceptedTaxon = rel.getAcceptedTaxon();
            boolean isTargetTaxon = acceptedTaxon != null && acceptedTaxon.equals(targetTaxon);
            HomotypicalGroup acceptedGroup = acceptedTaxon.getHomotypicGroup();
            boolean isHomotypicToTaxon = acceptedGroup.equals(newHomotypicalGroup);
            SynonymRelationshipType newRelationType = isHomotypicToTaxon? SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF() : SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
            rel.setType(newRelationType);
            //TODO handle citation and microCitation

            if (isTargetTaxon){
                relToTargetTaxonExists = true;
            }else{
                if (removeFromOtherTaxa){
                    acceptedTaxon.removeSynonym(synonym, false);
                }else{
                    //do nothing
                }
            }
        }
        if (targetTaxon != null &&  ! relToTargetTaxonExists ){
            Taxon acceptedTaxon = targetTaxon;
            HomotypicalGroup acceptedGroup = acceptedTaxon.getHomotypicGroup();
            boolean isHomotypicToTaxon = acceptedGroup.equals(newHomotypicalGroup);
            SynonymRelationshipType relType = isHomotypicToTaxon? SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF() : SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
            //TODO handle citation and microCitation
            Reference citation = null;
            String microCitation = null;
            acceptedTaxon.addSynonym(synonym, relType, citation, microCitation);
        }

    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
     */
    @Override
    public void updateTitleCache(Class<? extends TaxonBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<TaxonBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = TaxonBase.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }

    @Autowired
    protected void setDao(ITaxonDao dao) {
        this.dao = dao;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaByName(java.lang.Class, java.lang.String, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, java.lang.Integer, java.lang.Integer)
     */
    public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<TaxonBase>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber);
        }

        return new DefaultPagerImpl<TaxonBase>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listTaxaByName(java.lang.Class, java.lang.String, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, java.lang.Integer, java.lang.Integer)
     */
    public List<TaxonBase> listTaxaByName(Class<? extends TaxonBase> clazz, String uninomial,	String infragenericEpithet, String specificEpithet,	String infraspecificEpithet, Rank rank, Integer pageSize,Integer pageNumber) {
        Integer numberOfResults = dao.countTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank);

        List<TaxonBase> results = new ArrayList<TaxonBase>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.findTaxaByName(clazz, uninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, rank, pageSize, pageNumber);
        }

        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listToTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public List<TaxonRelationship> listToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#pageToTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public Pager<TaxonRelationship> pageToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedTo);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedTo);
        }
        return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listFromTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public List<TaxonRelationship> listFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#pageFromTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public Pager<TaxonRelationship> pageFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonRelationships(taxon, type, TaxonRelationship.Direction.relatedFrom);

        List<TaxonRelationship> results = new ArrayList<TaxonRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getTaxonRelationships(taxon, type, pageSize, pageNumber, orderHints, propertyPaths, TaxonRelationship.Direction.relatedFrom);
        }
        return new DefaultPagerImpl<TaxonRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getSynonyms(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public Pager<SynonymRelationship> getSynonyms(Taxon taxon,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countSynonyms(taxon, type);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonyms(taxon, type, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getSynonyms(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public Pager<SynonymRelationship> getSynonyms(Synonym synonym,	SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countSynonyms(synonym, type);
		
		List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
		if(numberOfResults > 0) { // no point checking again
		    results = dao.getSynonyms(synonym, type, pageSize, pageNumber, orderHints, propertyPaths);
		}

        return new DefaultPagerImpl<SynonymRelationship>(pageNumber, numberOfResults, pageSize, results);
    }

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getHomotypicSynonymsByHomotypicGroup(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.List)
	 */
	public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths){
		Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
		return t.getHomotypicSynonymsByHomotypicGroup();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getHeterotypicSynonymyGroups(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.List)
	 */
	public List<List<Synonym>> getHeterotypicSynonymyGroups(Taxon taxon, List<String> propertyPaths){
		Taxon t = (Taxon)dao.load(taxon.getUuid(), propertyPaths);
		List<HomotypicalGroup> homotypicalGroups = t.getHeterotypicSynonymyGroups();
		List<List<Synonym>> heterotypicSynonymyGroups = new ArrayList<List<Synonym>>(homotypicalGroups.size());
		for(HomotypicalGroup homotypicalGroup : homotypicalGroups){
			heterotypicSynonymyGroups.add(t.getSynonymsInGroup(homotypicalGroup));
		}
		return heterotypicSynonymyGroups;
	}

	public List<UuidAndTitleCache<TaxonBase>> findTaxaAndNamesForEditor(ITaxonServiceConfigurator configurator){
		
		List<UuidAndTitleCache<TaxonBase>> result = new ArrayList<UuidAndTitleCache<TaxonBase>>();
		Class<? extends TaxonBase> clazz = null;
		if ((configurator.isDoTaxa() && configurator.isDoSynonyms())) {
			clazz = TaxonBase.class;
			//propertyPath.addAll(configurator.getTaxonPropertyPath());
			//propertyPath.addAll(configurator.getSynonymPropertyPath());
		} else if(configurator.isDoTaxa()) {
			clazz = Taxon.class;
			//propertyPath = configurator.getTaxonPropertyPath();
		} else if (configurator.isDoSynonyms()) {
			clazz = Synonym.class;
			//propertyPath = configurator.getSynonymPropertyPath();
		}
		
		
		result = dao.getTaxaByNameForEditor(clazz, configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas());
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator)
	 */
	public Pager<IdentifiableEntity> findTaxaAndNames(ITaxonServiceConfigurator configurator) {
		
		List<IdentifiableEntity> results = new ArrayList<IdentifiableEntity>();
		int numberOfResults = 0; // overall number of results (as opposed to number of results per page)
		List<TaxonBase> taxa = null; 

        // Taxa and synonyms
        long numberTaxaResults = 0L;

        Class<? extends TaxonBase> clazz = null;
        List<String> propertyPath = new ArrayList<String>();
        if(configurator.getTaxonPropertyPath() != null){
            propertyPath.addAll(configurator.getTaxonPropertyPath());
        }
        if ((configurator.isDoTaxa() && configurator.isDoSynonyms())) {
            clazz = TaxonBase.class;
            //propertyPath.addAll(configurator.getTaxonPropertyPath());
            //propertyPath.addAll(configurator.getSynonymPropertyPath());
        } else if(configurator.isDoTaxa()) {
            clazz = Taxon.class;
            //propertyPath = configurator.getTaxonPropertyPath();
        } else if (configurator.isDoSynonyms()) {
            clazz = Synonym.class;
            //propertyPath = configurator.getSynonymPropertyPath();
        }

        if(clazz != null){
            if(configurator.getPageSize() != null){ // no point counting if we need all anyway
                numberTaxaResults =
                    dao.countTaxaByName(clazz,
                        configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(),
                        configurator.getNamedAreas());
            }

            if(configurator.getPageSize() == null || numberTaxaResults > configurator.getPageSize() * configurator.getPageNumber()){ // no point checking again if less results
                taxa = dao.getTaxaByName(clazz,
                    configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(),
                    configurator.getNamedAreas(), configurator.getPageSize(),
                    configurator.getPageNumber(), propertyPath);
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

            List<? extends TaxonNameBase<?,?>> names =
                nameDao.findByName(configurator.getTitleSearchStringSqlized(), configurator.getMatchMode(),
                        configurator.getPageSize(), configurator.getPageNumber(), null, configurator.getTaxonNamePropertyPath());
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

        if (configurator.isDoTaxaByCommonNames()) {
            taxa = null;
            numberTaxaResults = 0;
            if(configurator.getPageSize() != null){// no point counting if we need all anyway
                numberTaxaResults = dao.countTaxaByCommonName(configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas());
            }
            if(configurator.getPageSize() == null || numberTaxaResults > configurator.getPageSize() * configurator.getPageNumber()){
                taxa = dao.getTaxaByCommonName(configurator.getTitleSearchStringSqlized(), configurator.getClassification(), configurator.getMatchMode(), configurator.getNamedAreas(), configurator.getPageSize(), configurator.getPageNumber(), configurator.getTaxonPropertyPath());
            }
            if(taxa != null){
                results.addAll(taxa);
            }
            numberOfResults += numberTaxaResults;

        }

       return new DefaultPagerImpl<IdentifiableEntity>
            (configurator.getPageNumber(), numberOfResults, configurator.getPageSize(), results);
    }

    public List<UuidAndTitleCache<TaxonBase>> getTaxonUuidAndTitleCache(){
        return dao.getUuidAndTitleCache();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getAllMedia(eu.etaxonomy.cdm.model.taxon.Taxon, int, int, int, java.lang.String[])
     */
    public List<MediaRepresentation> getAllMedia(Taxon taxon, int size, int height, int widthOrDuration, String[] mimeTypes){
        List<MediaRepresentation> medRep = new ArrayList<MediaRepresentation>();
        taxon = (Taxon)dao.load(taxon.getUuid());
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        for (TaxonDescription taxDesc: descriptions){
            Set<DescriptionElementBase> elements = taxDesc.getElements();
            for (DescriptionElementBase descElem: elements){
                for(Media media : descElem.getMedia()){

                    //find the best matching representation
                    medRep.add(MediaUtils.findBestMatchingRepresentation(media, null, size, height, widthOrDuration, mimeTypes));

                }
            }
        }
        return medRep;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findTaxaByID(java.util.Set)
     */
    public List<TaxonBase> findTaxaByID(Set<Integer> listOfIDs) {
        return this.dao.findById(listOfIDs);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#countAllRelationships()
     */
    public int countAllRelationships() {
        return this.dao.countAllRelationships();
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#createAllInferredSynonyms(eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.model.taxon.Taxon)
     */
    public List<Synonym> createAllInferredSynonyms(Classification tree,
            Taxon taxon) {

        return this.dao.createAllInferredSynonyms(taxon, tree);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#createInferredSynonyms(eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType)
     */
    public List<Synonym> createInferredSynonyms(Classification tree, Taxon taxon, SynonymRelationshipType type) {
        return this.dao.createInferredSynonyms(taxon, tree, type);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findIdenticalTaxonNames(java.util.List)
     */
    public List<TaxonNameBase> findIdenticalTaxonNames(List<String> propertyPath) {
        return this.dao.findIdenticalTaxonNames(propertyPath);
    }


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteSynonym(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.Synonym, boolean)
	 */
	@Transactional(readOnly = false)
	@Override
	public void deleteSynonym(Synonym synonym, Taxon taxon, boolean removeNameIfPossible /*,boolean newHomotypicGroup*/) {
		if (synonym == null){
			return;
		}
		synonym = CdmBase.deproxy(dao.merge(synonym), Synonym.class);
		
		//remove synonymRelationship
		Set<Taxon> taxonSet = new HashSet<Taxon>();
		if (taxon != null){
			taxonSet.add(taxon);
		}else{
			taxonSet.addAll(synonym.getAcceptedTaxa());
		}
		for (Taxon relatedTaxon : taxonSet){
//			dao.deleteSynonymRelationships(synonym, relatedTaxon);
			relatedTaxon.removeSynonym(synonym, true);
		}
		this.saveOrUpdate(synonym);
		
		//TODO remove name from homotypical group?
		
		//remove synonym (if necessary)
		if (synonym.getSynonymRelations().isEmpty()){
			TaxonNameBase<?,?> name = synonym.getName();
			synonym.setName(null);
			dao.delete(synonym);
			
			//remove name if possible (and required)
			if (name != null && removeNameIfPossible){
				try{
					nameService.delete(name, new NameDeletionConfigurator());
				}catch (DeleteException ex){
					if (logger.isDebugEnabled())logger.debug("Name wasn't deleted as it is referenced");
				}
			}
		}
	}
	
	
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findIdenticalTaxonNameIds(java.util.List)
     */
    public List<TaxonNameBase> findIdenticalTaxonNameIds(List<String> propertyPath) {

        return this.dao.findIdenticalNamesNew(propertyPath);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getPhylumName(eu.etaxonomy.cdm.model.name.TaxonNameBase)
     */
    public String getPhylumName(TaxonNameBase name){
        return this.dao.getPhylumName(name);
    }

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#deleteSynonymRelationships(eu.etaxonomy.cdm.model.taxon.Synonym)
	 */
	public long deleteSynonymRelationships(Synonym syn) {
		return dao.deleteSynonymRelationships(syn, null);
	}


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#listSynonymRelationships(eu.etaxonomy.cdm.model.taxon.TaxonBase, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction)
     */
    public List<SynonymRelationship> listSynonymRelationships(
            TaxonBase taxonBase, SynonymRelationshipType type, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths, Direction direction) {
        Integer numberOfResults = dao.countSynonymRelationships(taxonBase, type, direction);

        List<SynonymRelationship> results = new ArrayList<SynonymRelationship>();
        if(numberOfResults > 0) { // no point checking again
            results = dao.getSynonymRelationships(taxonBase, type, pageSize, pageNumber, orderHints, propertyPaths, direction);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findBestMatchingTaxon(java.lang.String)
     */
    @Override
    public Taxon findBestMatchingTaxon(String taxonName) {
        MatchingTaxonConfigurator config = MatchingTaxonConfigurator.NewInstance();
        config.setTaxonNameTitle(taxonName);
        return findBestMatchingTaxon(config);
    }



    @Override
    public Taxon findBestMatchingTaxon(MatchingTaxonConfigurator config) {

        Taxon bestCandidate = null;
        try{
            // 1. search for acceptet taxa
            List<TaxonBase> taxonList = dao.findByNameTitleCache(Taxon.class, config.getTaxonNameTitle(), null, MatchMode.EXACT, null, 0, null, null);
            boolean bestCandidateMatchesSecUuid = false;
            boolean bestCandidateIsInClassification = false;
            int countEqualCandidates = 0;
            for(TaxonBase taxonBaseCandidate : taxonList){
                if(taxonBaseCandidate instanceof Taxon){
                    Taxon newCanditate = CdmBase.deproxy(taxonBaseCandidate, Taxon.class);
                    boolean newCandidateMatchesSecUuid = isMatchesSecUuid(newCanditate, config);
                    if (! newCandidateMatchesSecUuid && config.isOnlyMatchingSecUuid() ){
                        continue;
                    }else if(newCandidateMatchesSecUuid && ! bestCandidateMatchesSecUuid){
                        bestCandidate = newCanditate;
                        countEqualCandidates = 1;
                        bestCandidateMatchesSecUuid = true;
                        continue;
                    }

                    boolean newCandidateInClassification = isInClassification(newCanditate, config);
                    if (! newCandidateInClassification && config.isOnlyMatchingClassificationUuid()){
                        continue;
                    }else if (newCandidateInClassification && ! bestCandidateIsInClassification){
                        bestCandidate = newCanditate;
                        countEqualCandidates = 1;
                        bestCandidateIsInClassification = true;
                        continue;
                    }
                    if (bestCandidate == null){
                        bestCandidate = newCanditate;
                        countEqualCandidates = 1;
                        continue;
                    }

                }else{  //not Taxon.class
                    continue;
                }
                countEqualCandidates++;

            }
            if (bestCandidate != null){
                if(countEqualCandidates > 1){
                    logger.info(countEqualCandidates + " equally matching TaxonBases found, using first accepted Taxon: " + bestCandidate.getTitleCache());
                    return bestCandidate;
                } else {
                    logger.info("using accepted Taxon: " + bestCandidate.getTitleCache());
                    return bestCandidate;
                }
            }


            // 2. search for synonyms
            if (config.isIncludeSynonyms()){
                List<TaxonBase> synonymList = dao.findByNameTitleCache(Synonym.class, config.getTaxonNameTitle(), null, MatchMode.EXACT, null, 0, null, null);
                for(TaxonBase taxonBase : synonymList){
                    if(taxonBase instanceof Synonym){
                        Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
                        Set<Taxon> acceptetdCandidates = synonym.getAcceptedTaxa();
                        if(!acceptetdCandidates.isEmpty()){
                            bestCandidate = acceptetdCandidates.iterator().next();
                            if(acceptetdCandidates.size() == 1){
                                logger.info(acceptetdCandidates.size() + " Accepted taxa found for synonym " + taxonBase.getTitleCache() + ", using first one: " + bestCandidate.getTitleCache());
                                return bestCandidate;
                            } else {
                                logger.info("using accepted Taxon " +  bestCandidate.getTitleCache() + "for synonym " + taxonBase.getTitleCache());
                                return bestCandidate;
                            }
                            //TODO extend method: search using treeUUID, using SecUUID, first find accepted then include synonyms until a matching taxon is found
                        }
                    }
                }
            }

        } catch (Exception e){
            logger.error(e);
        }

        return bestCandidate;
    }

    private boolean isInClassification(Taxon taxon, MatchingTaxonConfigurator config) {
        UUID configClassificationUuid = config.getClassificationUuid();
        if (configClassificationUuid == null){
            return false;
        }
        for (TaxonNode node : taxon.getTaxonNodes()){
            UUID classUuid = node.getClassification().getUuid();
            if (configClassificationUuid.equals(classUuid)){
                return true;
            }
        }
        return false;
    }

    private boolean isMatchesSecUuid(Taxon taxon, MatchingTaxonConfigurator config) {
        UUID configSecUuid = config.getSecUuid();
        if (configSecUuid == null){
            return false;
        }
        UUID taxonSecUuid = (taxon.getSec() == null)? null : taxon.getSec().getUuid();
        return configSecUuid.equals(taxonSecUuid);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#findBestMatchingSynonym(java.lang.String)
     */
    @Override
    public Synonym findBestMatchingSynonym(String taxonName) {
        List<TaxonBase> synonymList = dao.findByNameTitleCache(Synonym.class, taxonName, null, MatchMode.EXACT, null, 0, null, null);
        if(! synonymList.isEmpty()){
            Synonym result = CdmBase.deproxy(synonymList.iterator().next(), Synonym.class);
            if(synonymList.size() == 1){
                logger.info(synonymList.size() + " Synonym found " + result.getTitleCache() );
                return result;
            } else {
                logger.info("Several matching synonyms found. Using first: " +  result.getTitleCache());
                return result;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#moveSynonymToAnotherTaxon(eu.etaxonomy.cdm.model.taxon.SynonymRelationship, eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)
     */
    @Override
    public Taxon moveSynonymToAnotherTaxon(SynonymRelationship synonymRelation,
            Taxon toTaxon, SynonymRelationshipType synonymRelationshipType, Reference reference, String referenceDetail) {
        Taxon fromTaxon = synonymRelation.getAcceptedTaxon();

        toTaxon.addSynonym(synonymRelation.getSynonym(), synonymRelationshipType, reference, referenceDetail);

        fromTaxon.removeSynonymRelation(synonymRelation);

        return toTaxon;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getUuidAndTitleCacheTaxon()
     */
    @Override
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheTaxon() {
        return dao.getUuidAndTitleCacheTaxon();
    }

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ITaxonService#getUuidAndTitleCacheSynonym()
	 */
	@Override
	public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheSynonym() {
		return dao.getUuidAndTitleCacheSynonym();
	}

}
