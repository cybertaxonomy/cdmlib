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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
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
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class DescriptionServiceImpl extends IdentifiableServiceBase<DescriptionBase,IDescriptionDao> implements IDescriptionService {
 	
	private static final Logger logger = Logger.getLogger(DescriptionServiceImpl.class);

	protected IDescriptionElementDao descriptionElementDao;
	protected IFeatureTreeDao featureTreeDao;
	protected IFeatureNodeDao featureNodeDao;
	protected IFeatureDao featureDao;
	protected ITermVocabularyDao vocabularyDao;
	protected IStatisticalMeasurementValueDao statisticalMeasurementValueDao;
	private INaturalLanguageGenerator naturalLanguageGenerator;
	
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
	
	@Autowired
	protected void setNaturalLanguageGenerator(INaturalLanguageGenerator naturalLanguageGenerator) {
		this.naturalLanguageGenerator = naturalLanguageGenerator;
	}
	
	/**
	 * 
	 */
	public DescriptionServiceImpl() {
		logger.debug("Load DescriptionService Bean");
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache()
	 */
	@Override
	public void updateTitleCache() {
		Class<DescriptionBase> clazz = DescriptionBase.class;
		super.updateTitleCache(clazz, null, null);
	}
	
	public TermVocabulary<Feature> getDefaultFeatureVocabulary(){
		String uuidFeature = "b187d555-f06f-4d65-9e53-da7c93f8eaa8";
		UUID featureUuid = UUID.fromString(uuidFeature);
		return (TermVocabulary)vocabularyDao.findByUuid(featureUuid);
	}

	@Autowired
	protected void setDao(IDescriptionDao dao) {
		this.dao = dao;
	}

	public int count(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText,Set<Feature> feature) {
		return dao.countDescriptions(type, hasImages, hasText, feature);
	}

	/**
	 * FIXME Candidate for harmonization
	 * rename -> getElements
	 */
	public Pager<DescriptionElementBase> getDescriptionElements(DescriptionBase description,
			Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

		List<DescriptionElementBase> results = listDescriptionElements(description, features, type, pageSize, pageNumber, propertyPaths);
		return new DefaultPagerImpl<DescriptionElementBase>(pageNumber, results.size(), pageSize, results);
	}

	public List<DescriptionElementBase> listDescriptionElements(DescriptionBase description,
			Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
		Integer numberOfResults = dao.countDescriptionElements(description, features, type);
		
		List<DescriptionElementBase> results = new ArrayList<DescriptionElementBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getDescriptionElements(description, features, type, pageSize, pageNumber, propertyPaths);
		}
		return results;
	}
	public Pager<Annotation> getDescriptionElementAnnotations(DescriptionElementBase annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
		Integer numberOfResults = descriptionElementDao.countAnnotations(annotatedObj, status);
		
		List<Annotation> results = new ArrayList<Annotation>();
		if(numberOfResults > 0) { // no point checking again
			results = descriptionElementDao.getAnnotations(annotatedObj, status, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<Annotation>(pageNumber, numberOfResults, pageSize, results);
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
	
	public NamedAreaTree getOrderedDistributions(Set<TaxonDescription> taxonDescriptions, Set<NamedAreaLevel> omitLevels){
		List<NamedArea> areaList = new ArrayList<NamedArea>();
		NamedAreaTree tree = new NamedAreaTree();
		//getting all the areas
		for (TaxonDescription taxonDescription : taxonDescriptions) {
			taxonDescription = (TaxonDescription) dao.load(taxonDescription.getUuid());
			Set<DescriptionElementBase> elements = taxonDescription.getElements();
			for (DescriptionElementBase element : elements) {
				if(element.isInstanceOf(Distribution.class)){
					Distribution distribution = (Distribution) element;
					areaList.add(distribution.getArea());
				}
			}
		}
		//ordering the areas
		tree.merge(areaList, omitLevels);
		tree.sortChildren();
		return tree;	
	}
	
	public DistributionTree getOrderedDistributionsB(
			Set<TaxonDescription> taxonDescriptions,
			Set<NamedAreaLevel> omitLevels){
		
		DistributionTree tree = new DistributionTree();
		List<Distribution> distList = new ArrayList<Distribution>();
		
		for (TaxonDescription taxonDescription : taxonDescriptions) {
			taxonDescription = (TaxonDescription) dao.load(taxonDescription.getUuid());
			Set<DescriptionElementBase> elements = taxonDescription.getElements();
			for (DescriptionElementBase element : elements) {
				if(element.isInstanceOf(Distribution.class)){
					Distribution distribution = (Distribution) element;
					distList.add(distribution);
				}
			}
		}
		
		//ordering the areas
		tree.merge(distList, omitLevels);
		tree.sortChildren();
		return tree;
	}

	public Pager<TaxonNameDescription> getTaxonNameDescriptions(TaxonNameBase name, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonNameDescriptions(name);
		
		List<TaxonNameDescription> results = new ArrayList<TaxonNameDescription>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getTaxonNameDescriptions(name, pageSize, pageNumber,propertyPaths); 
		}
		
		return new DefaultPagerImpl<TaxonNameDescription>(pageNumber, numberOfResults, pageSize, results);
	}

	
	public Pager<DescriptionBase> page(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDescriptions(type, hasImages, hasText, feature);
		
		List<DescriptionBase> results = new ArrayList<DescriptionBase>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.listDescriptions(type, hasImages, hasText, feature, pageSize, pageNumber,orderHints,propertyPaths); 
		}
		
		return new DefaultPagerImpl<DescriptionBase>(pageNumber, numberOfResults, pageSize, results);
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
	public Pager<DescriptionElementBase> searchElements(Class<? extends DescriptionElementBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = descriptionElementDao.count(clazz,queryString);
		
		List<DescriptionElementBase> results = new ArrayList<DescriptionElementBase>();
		if(numberOfResults > 0) { // no point checking again
			results = descriptionElementDao.search(clazz, queryString, pageSize, pageNumber, orderHints, propertyPaths); 
		}
		
		return new DefaultPagerImpl<DescriptionElementBase>(pageNumber, numberOfResults, pageSize, results);
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
	 * descriptionElementService.load
	 */
	public DescriptionElementBase loadDescriptionElement(UUID uuid,	List<String> propertyPaths) {
		return descriptionElementDao.load(uuid, propertyPaths);
	}

    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.save
     */
	@Transactional(readOnly = false)
	public UUID saveDescriptionElement(DescriptionElementBase descriptionElement) {
		return descriptionElementDao.save(descriptionElement);
	}
	
    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.save
     */
	@Transactional(readOnly = false)
	public Map<UUID, DescriptionElementBase> saveDescriptionElement(Collection<DescriptionElementBase> descriptionElements) {
		return descriptionElementDao.saveAll(descriptionElements);
	}

    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.delete
     */
	public UUID deleteDescriptionElement(DescriptionElementBase descriptionElement) {
		return descriptionElementDao.delete(descriptionElement);
	}

	public TermVocabulary<Feature> getFeatureVocabulary(UUID uuid) {
		return (TermVocabulary)vocabularyDao.findByUuid(uuid);
	}

	public List<DescriptionElementBase> getDescriptionElementsForTaxon(
			Taxon taxon, Set<Feature> features,
			Class<? extends DescriptionElementBase> type, Integer pageSize,
			Integer pageNumber, List<String> propertyPaths) {
		 return dao.getDescriptionElementForTaxon(taxon, features, type, pageSize, pageNumber, propertyPaths);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#generateNaturalLanguageDescription(eu.etaxonomy.cdm.model.description.FeatureTree, eu.etaxonomy.cdm.model.description.TaxonDescription, eu.etaxonomy.cdm.model.common.Language, java.util.List)
	 */
	@Override
	public String generateNaturalLanguageDescription(FeatureTree featureTree,
			TaxonDescription description, List<Language> preferredLanguages, String separator) {
		
		Language lang = preferredLanguages.size() > 0 ? preferredLanguages.get(0) : Language.DEFAULT();
		
		description = (TaxonDescription)load(description.getUuid());
		featureTree = featureTreeDao.load(featureTree.getUuid());
		
		StringBuilder naturalLanguageDescription = new StringBuilder();
		
		if(description.hasStructuredData()){
			
			List<TextData> textDataList = naturalLanguageGenerator.generateNaturalLanguageDescription(
					featureTree, 
					((TaxonDescription)description),
					lang);
			
			String lastCategory = null;
			String categorySeparator = ". ";
			
			String sep = separator;
			
			boolean doItBetter = true;

			for (TextData textData : textDataList.toArray(new TextData[textDataList.size()])){
				if(textData.getMultilanguageText().size() > 0){
					
					if (!textData.getFeature().equals(Feature.UNKNOWN())) {
						String featureLabel = textData.getFeature().getLabel(lang);

						if(doItBetter){
						/*
						 *  WARNING
						 *  The code lines below are desinged to handle
						 *  a special case where as the fealure label contains 
						 *  hirarchical information on the features. This code 
						 *  exist only as a base for discussion, and is not 
						 *  intendet to be used in production.
						 */
						featureLabel = StringUtils.remove(featureLabel, '>');
						
						String[] labelTokens = StringUtils.split(featureLabel, '<');
						if(labelTokens[0].equals(lastCategory) && labelTokens.length > 1){
							if(naturalLanguageDescription.length() > 0){
								naturalLanguageDescription.append(separator);
}
							naturalLanguageDescription.append(labelTokens[1]);
						} else {
							if(naturalLanguageDescription.length() > 0){
								naturalLanguageDescription.append(categorySeparator);
							}
							naturalLanguageDescription.append(StringUtils.join(labelTokens));
						}
						lastCategory = labelTokens[0];
						// end of demo code
						} else {
							if(naturalLanguageDescription.length() > 0){
								naturalLanguageDescription.append(categorySeparator);
							}
							naturalLanguageDescription.append(textData.getFeature().getLabel(lang));							
						}
					}
					String text = textData.getMultilanguageText().values().iterator().next().getText();
					naturalLanguageDescription.append(text);		
					
				}
			}
		
		}
		return naturalLanguageDescription.toString();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IDescriptionService#hasStructuredData(eu.etaxonomy.cdm.model.description.DescriptionBase)
	 */
	@Override
	public boolean hasStructuredData(DescriptionBase<?> description) {
		return load(description.getUuid()).hasStructuredData();
	}
}
