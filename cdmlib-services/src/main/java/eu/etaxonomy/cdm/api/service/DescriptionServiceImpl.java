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
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IStatisticalMeasurementValueDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 * @created 24.06.2008
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class DescriptionServiceImpl extends IdentifiableServiceBase<DescriptionBase,IDescriptionDao> implements IDescriptionService {
 	
	private static final Logger logger = Logger.getLogger(DescriptionServiceImpl.class);

	protected IDescriptionElementDao descriptionElementDao;
	protected IFeatureTreeDao featureTreeDao;
	protected IFeatureNodeDao featureNodeDao;
	protected IFeatureDao featureDao;
	protected ITermVocabularyDao vocabularyDao;
	protected IStatisticalMeasurementValueDao statisticalMeasurementValueDao;
	
	@Autowired
	protected void setFeatureTreeDao(IFeatureTreeDao featureTreeDao) {
		this.featureTreeDao = featureTreeDao;
	}
	
	@Autowired
	protected void setFeatureNodeDao(IFeatureNodeDao featureNodeDao) {
		this.featureNodeDao = featureNodeDao;
	}
	
	@Autowired
	protected void setFeatureDao(IFeatureDao featureDao) {
		this.featureDao = featureDao;
	}
	
	@Autowired
	protected void setVocabularyDao(ITermVocabularyDao vocabularyDao) {
		this.vocabularyDao = vocabularyDao;
	}
	
	@Autowired
	protected void statisticalMeasurementValueDao(IStatisticalMeasurementValueDao statisticalMeasurementValueDao) {
		this.statisticalMeasurementValueDao = statisticalMeasurementValueDao;
	}
	
	@Autowired
	protected void setDescriptionElementDao(IDescriptionElementDao descriptionElementDao) {
		this.descriptionElementDao = descriptionElementDao;
	}
	
	/**
	 * 
	 */
	public DescriptionServiceImpl() {
		logger.debug("Load DescriptionService Bean");
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#getDescriptionBaseByUuid(java.util.UUID)
	 * FIXME Candidate for harmonization
	 * find
	 */
	public DescriptionBase getDescriptionBaseByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#saveDescription(eu.etaxonomy.cdm.model.description.DescriptionBase)
	 * FIXME Candidate for harmonization
	 * save
	 */
	@Transactional(readOnly = false)
	public UUID saveDescription(DescriptionBase description) {
		return super.saveCdmObject(description);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#saveDescription(eu.etaxonomy.cdm.model.description.DescriptionBase)
	 * FIXME Candidate for harmonization
	 * Given that statistcal measurement values are wholy owned by their parent QuantitativeData, do we need this method? could we not 
	 * save / update statistical measurement values as part of descriptionElementService.update or descriptionElementService.save?
	 */
	@Transactional(readOnly = false)
	public UUID saveStatisticalMeasurementValue(StatisticalMeasurementValue statisticalMeasurementValue) {
		return statisticalMeasurementValueDao.saveOrUpdate(statisticalMeasurementValue);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#generateTitleCache()
	 */
	public void generateTitleCache() {
		logger.warn("generateTitleCache not yet implemented");
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#saveFeatureTree(eu.etaxonomy.cdm.model.description.FeatureTree)
	 * FIXME Candidate for harmonization
	 * featureTreeService.save
	 */
	@Transactional(readOnly = false)
	public UUID saveFeatureTree(FeatureTree tree) {
		return featureTreeDao.saveOrUpdate(tree);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#saveFeatureDataAll(java.util.Collection)
	 * FIXME Candidate for harmonization 
	 * remove
	 * featureTreeService.save(FeatureTree f) and this cascades to child nodes or does this not work?
	 * CDM Xml now only has a list of FeatureTrees, and calls saveFeatureTreeAll(featureTrees);
	 */
	@Transactional(readOnly = false)
	public void saveFeatureDataAll(Collection<VersionableEntity> featureData) {

		List<FeatureTree> trees = new ArrayList<FeatureTree>();
		List<FeatureNode> nodes = new ArrayList<FeatureNode>();
		
		for ( VersionableEntity featureItem : featureData) {
			if (featureItem instanceof FeatureTree) {
				trees.add((FeatureTree)featureItem);
			} else if (featureItem instanceof FeatureNode) {
				nodes.add((FeatureNode)featureItem);
			} else {
				logger.error("Entry of wrong type: " + featureItem.toString());
			}
		}
		
		if (trees.size() > 0) { saveFeatureTreeAll(trees); }
		if (nodes.size() > 0) { saveFeatureNodeAll(nodes); }
	}
	
	/**
	 * FIXME Candidate for harmonization 
	 * featureTreeService.save(Set<FeatureTree> featureTrees)
	 */
	@Transactional(readOnly = false)
	public Map<UUID, FeatureTree> saveFeatureTreeAll(Collection<FeatureTree> trees) {
		return featureTreeDao.saveAll(trees);
	}

	/**
	 * FIXME Candidate for harmonization
	 * is this needed? 
	 */
	@Transactional(readOnly = false)
	public Map<UUID, FeatureNode> saveFeatureNodeAll(Collection<FeatureNode> trees) {
		return featureNodeDao.saveAll(trees);
	}

	/**
	 * FIXME Candidate for harmonization
	 * vocabularyService.find(UUID uuid)
	 */
	public TermVocabulary<Feature> getFeatureVocabulary(UUID uuid){
		TermVocabulary<Feature> featureVocabulary;
		try {
			featureVocabulary = (TermVocabulary)vocabularyDao.findByUuid(uuid);
		} catch (ClassCastException e) {
			return null;
		}
		return featureVocabulary;
	}

//	public TermVocabulary<Feature> getFeatureVocabulary(){
//		TermVocabulary<Feature> featureVocabulary;
//		try {
//			featureVocabulary = (TermVocabulary)vocabularyDao.findByUuid(uuid);
//		} catch (ClassCastException e) {
//			return null;
//		}
//		return featureVocabulary;
//	}

	public TermVocabulary<Feature> getDefaultFeatureVocabulary(){
		String uuidFeature = "b187d555-f06f-4d65-9e53-da7c93f8eaa8";
		UUID featureUuid = UUID.fromString(uuidFeature);
		return getFeatureVocabulary(featureUuid);
	}

	/**
	 * FIXME Candidate for harmonization
	 * featureTreeService.list()
	 * @return
	 */
	public List<FeatureTree> getFeatureTreesAll() {
		return featureTreeDao.list();
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * is this needed?
	 */
	public List<FeatureNode> getFeatureNodesAll() {
		return featureNodeDao.list();
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * termService.list(Feature.class, null,null,...)
	 */
	public List<Feature> getFeaturesAll() {
		return featureDao.list();
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * featureTreeService.list
	 */
	public List<FeatureTree> getFeatureTreesAll(List<String> propertyPaths){
		return featureTreeDao.list(null, null, null, propertyPaths);
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * is this needed?
	 */
	public List<FeatureNode> getFeatureNodesAll(List<String> propertyPaths){
		return featureNodeDao.list(null, null, null, propertyPaths);
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * termService.list
	 */
	public List<Feature> getFeaturesAll(List<String> propertyPaths){
		return featureDao.list(null, null, null, propertyPaths);
	}

	@Autowired
	protected void setDao(IDescriptionDao dao) {
		this.dao = dao;
	}

	/**
	 * FIXME Candidate for harmonization
	 * rename -> count
	 */
	public <TYPE extends DescriptionBase> int countDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText,Set<Feature> feature) {
		return dao.countDescriptions(type, hasImages, hasText, feature);
	}

	/**
	 * FIXME Candidate for harmonization
	 * rename -> getElements
	 */
	public <TYPE extends DescriptionElementBase> Pager<TYPE> getDescriptionElements(DescriptionBase description,
			Set<Feature> features, Class<TYPE> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		Integer numberOfResults = dao.countDescriptionElements(description, features, type);

		List<TYPE> results = new ArrayList<TYPE>();
		if (numberOfResults > 0) { // no point checking again
			results = dao.getDescriptionElements(description, features, type, pageSize, pageNumber, propertyPaths);
		}

		return new DefaultPagerImpl<TYPE>(pageNumber, numberOfResults, pageSize, results);
	}
	

	public Pager<Media> getMedia(DescriptionElementBase descriptionElement,	Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = descriptionElementDao.countMedia(descriptionElement);
		
		List<Media> results = new ArrayList<Media>();
		if(numberOfResults > 0) { // no point checking again
			results = descriptionElementDao.getMedia(descriptionElement, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<TaxonDescription> getTaxonDescriptions(Taxon taxon, Set<Scope> scopes, Set<NamedArea> geographicalScope, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonDescriptions(taxon, scopes, geographicalScope);
		
		List<TaxonDescription> results = new ArrayList<TaxonDescription>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getTaxonDescriptions(taxon, scopes, geographicalScope, pageSize, pageNumber, propertyPaths); 
		}
		
		return new DefaultPagerImpl<TaxonDescription>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<TaxonNameDescription> getTaxonNameDescriptions(TaxonNameBase name, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonNameDescriptions(name);
		
		List<TaxonNameDescription> results = new ArrayList<TaxonNameDescription>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getTaxonNameDescriptions(name, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<TaxonNameDescription>(pageNumber, numberOfResults, pageSize, results);
	}

	/**
	 * FIXME Candidate for harmonization
	 * rename list
	 */
	public <TYPE extends DescriptionBase> Pager<TYPE> listDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDescriptions(type, hasImages, hasText, feature);
		
		List<TYPE> results = new ArrayList<TYPE>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.listDescriptions(type, hasImages, hasText, feature, pageSize, pageNumber,orderHints,propertyPaths); 
		}
		
		return new DefaultPagerImpl<TYPE>(pageNumber, numberOfResults, pageSize, results);
	}

	/**
	 * FIXME Candidate for harmonization
	 * Rename: searchByDistribution
	 */
	public Pager<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase presence,	Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDescriptionByDistribution(namedAreas, presence);
		
		List<TaxonDescription> results = new ArrayList<TaxonDescription>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.searchDescriptionByDistribution(namedAreas, presence, pageSize, pageNumber,orderHints,propertyPaths); 
		}
		
		return new DefaultPagerImpl<TaxonDescription>(pageNumber, numberOfResults, pageSize, results);
	}

	/**
	 * FIXME Candidate for harmonization
	 * move: descriptionElementService.search
	 */
	public Pager<DescriptionElementBase> search(Class<? extends DescriptionElementBase> clazz, String queryString, Integer pageSize,	Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = descriptionElementDao.count(clazz,queryString);
		
		List<DescriptionElementBase> results = new ArrayList<DescriptionElementBase>();
		if(numberOfResults > 0) { // no point checking again
			results = descriptionElementDao.search(clazz, queryString, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<DescriptionElementBase>(pageNumber, numberOfResults, pageSize, results);
	}

	/**
	 * FIXME Candidate for harmonization
	 * featureTreeService.find
	 */
	public FeatureTree getFeatureTreeByUuid(UUID uuid) {
		return featureTreeDao.findByUuid(uuid);
	}

	/**
	 * FIXME Candidate for harmonization
	 * descriptionElementService.find
	 */
	public DescriptionElementBase getDescriptionElementByUuid(UUID uuid) {
		return descriptionElementDao.findByUuid(uuid);
	}	

	/**
	 * FIXME Candidate for harmonization
	 * is this needed? yes, I think it is, because there is no way to recursively
	 * initialize feature trees and all of their children otherwise
	 */
	public FeatureNode loadFeatureNode(UUID uuid, List<String> propertyPaths) {
		return featureNodeDao.load(uuid, propertyPaths);
	}

	/**
	 * FIXME Candidate for harmonization
	 * featureTreeService.load
	 */
	public FeatureTree loadFeatureTree(UUID uuid, List<String> propertyPaths) {
		return featureTreeDao.load(uuid, propertyPaths);
	}
	
	/**
	 * FIXME Candidate for harmonization
	 * descriptionElementService.load
	 */
	public DescriptionElementBase loadDescriptionElement(UUID uuid,	List<String> propertyPaths) {
		return descriptionElementDao.load(uuid, propertyPaths);
	}

    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.save
     */
	public UUID saveDescriptionElement(DescriptionElementBase descriptionElement) {
		return descriptionElementDao.save(descriptionElement);
	}

    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.delete
     */
	public UUID deleteDescriptionElement(DescriptionElementBase descriptionElement) {
		return descriptionElementDao.delete(descriptionElement);
	}
}
