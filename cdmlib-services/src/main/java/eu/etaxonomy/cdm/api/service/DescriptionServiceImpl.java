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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.utility.DescriptionUtility;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureNodeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;
import eu.etaxonomy.cdm.persistence.dao.description.IStatisticalMeasurementValueDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.mueller
 * @created 24.06.2008
 * @version 1.0
 */
/**
 * @author a.kohlbecker
 * @date Dec 5, 2013
 *
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
    protected IDefinedTermDao definedTermDao;
    protected IStatisticalMeasurementValueDao statisticalMeasurementValueDao;
    protected ITaxonDao taxonDao;

    //TODO change to Interface
    private NaturalLanguageGenerator naturalLanguageGenerator;

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
    protected void setDefinedTermDao(IDefinedTermDao definedTermDao) {
        this.definedTermDao = definedTermDao;
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
    protected void setNaturalLanguageGenerator(NaturalLanguageGenerator naturalLanguageGenerator) {
        this.naturalLanguageGenerator = naturalLanguageGenerator;
    }

    @Autowired
    protected void setTaxonDao(ITaxonDao taxonDao) {
        this.taxonDao = taxonDao;
    }

    /**
     *
     */
    public DescriptionServiceImpl() {
        logger.debug("Load DescriptionService Bean");
    }



    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache(java.lang.Integer, eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends DescriptionBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<DescriptionBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = DescriptionBase.class;
        }
        super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
    }


    @Override
    public TermVocabulary<Feature> getDefaultFeatureVocabulary(){
        String uuidFeature = "b187d555-f06f-4d65-9e53-da7c93f8eaa8";
        UUID featureUuid = UUID.fromString(uuidFeature);
        return vocabularyDao.findByUuid(featureUuid);
    }

    @Override
    @Autowired
    protected void setDao(IDescriptionDao dao) {
        this.dao = dao;
    }

    @Override
    public int count(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText,Set<Feature> feature) {
        return dao.countDescriptions(type, hasImages, hasText, feature);
    }

    @Override
    public Pager<DescriptionElementBase> pageDescriptionElements(DescriptionBase description, Class<? extends DescriptionBase> descriptionType,
            Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        List<DescriptionElementBase> results = listDescriptionElements(description, descriptionType, features, type, pageSize, pageNumber, propertyPaths);
        return new DefaultPagerImpl<DescriptionElementBase>(pageNumber, results.size(), pageSize, results);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IDescriptionService#getDescriptionElements(eu.etaxonomy.cdm.model.description.DescriptionBase, java.util.Set, java.lang.Class, java.lang.Integer, java.lang.Integer, java.util.List)
     */
    @Override
    @Deprecated
    public Pager<DescriptionElementBase> getDescriptionElements(DescriptionBase description,
            Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        return pageDescriptionElements(description, null, features, type, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public List<DescriptionElementBase> listDescriptionElements(DescriptionBase description, Class<? extends DescriptionBase> descriptionType,
            Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        Integer numberOfResults = dao.countDescriptionElements(description, descriptionType, features, type);
        List<DescriptionElementBase> results = new ArrayList<DescriptionElementBase>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults.longValue(), pageNumber, pageSize)) {
            results = dao.getDescriptionElements(description, descriptionType, features, type, pageSize, pageNumber, propertyPaths);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IDescriptionService#listDescriptionElements(eu.etaxonomy.cdm.model.description.DescriptionBase, java.util.Set, java.lang.Class, java.lang.Integer, java.lang.Integer, java.util.List)
     */
    @Override
    @Deprecated
    public List<DescriptionElementBase> listDescriptionElements(DescriptionBase description,
            Set<Feature> features, Class<? extends DescriptionElementBase> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        return listDescriptionElements(description, null, features, type, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public Pager<Annotation> getDescriptionElementAnnotations(DescriptionElementBase annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        Integer numberOfResults = descriptionElementDao.countAnnotations(annotatedObj, status);

        List<Annotation> results = new ArrayList<Annotation>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = descriptionElementDao.getAnnotations(annotatedObj, status, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<Annotation>(pageNumber, numberOfResults, pageSize, results);
    }



    @Override
    public Pager<Media> getMedia(DescriptionElementBase descriptionElement,	Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = descriptionElementDao.countMedia(descriptionElement);

        List<Media> results = new ArrayList<Media>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = descriptionElementDao.getMedia(descriptionElement, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<Media>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public Pager<TaxonDescription> pageTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Set<MarkerType> markerTypes = null;
        return pageTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public List<TaxonDescription> listTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Set<MarkerType> markerTypes = null;
        return listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IDescriptionService#pageMarkedTaxonDescriptions(eu.etaxonomy.cdm.model.taxon.Taxon, java.util.Set, java.util.Set, java.util.Set, java.lang.Integer, java.lang.Integer, java.util.List)
     */
    @Override
    public Pager<TaxonDescription> pageTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes);

        List<TaxonDescription> results = new ArrayList<TaxonDescription>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<TaxonDescription>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<TaxonDescription> listTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        List<TaxonDescription> results = dao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
        return results;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IDescriptionService#listTaxonDescriptionMedia(UUID, boolean, Set, Integer, Integer, List)
     */
    @Override
    public List<Media> listTaxonDescriptionMedia(UUID taxonUuid, boolean limitToGalleries, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths){
        return this.dao.listTaxonDescriptionMedia(taxonUuid, limitToGalleries, markerTypes, pageSize, pageNumber, propertyPaths);
    }

    /*
     * @see IDescriptionService#countTaxonDescriptionMedia(UUID, boolean, Set)
     */
    @Override
    public int countTaxonDescriptionMedia(UUID taxonUuid, boolean limitToGalleries, Set<MarkerType> markerTypes){
        return this.dao.countTaxonDescriptionMedia(taxonUuid, limitToGalleries, markerTypes);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IDescriptionService#getOrderedDistributions(java.util.Set, boolean, boolean, java.util.Set, java.util.List)
     */
    @Override
    @Deprecated
    public DistributionTree getOrderedDistributions(
            Set<TaxonDescription> taxonDescriptions,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hiddenAreaMarkerTypes,
            Set<NamedAreaLevel> omitLevels, List<String> propertyPaths){

        List<Distribution> distList = new ArrayList<Distribution>();

        List<UUID> uuids = new ArrayList<UUID>();
        for (TaxonDescription taxonDescription : taxonDescriptions) {
            if (! taxonDescription.isImageGallery()){    //image galleries should not have descriptions, but better filter fully on DTYPE of description element
                uuids.add(taxonDescription.getUuid());
            }
        }

        List<DescriptionBase> desclist = dao.list(uuids, null, null, null, propertyPaths);
        for (DescriptionBase desc : desclist) {
            if (desc.isInstanceOf(TaxonDescription.class)){
                Set<DescriptionElementBase> elements = desc.getElements();
                for (DescriptionElementBase element : elements) {
                        if (element.isInstanceOf(Distribution.class)) {
                            Distribution distribution = (Distribution) element;
                            if(distribution.getArea() != null){
                                distList.add(distribution);
                            }
                        }
                }
            }
        }

        //old
//        for (TaxonDescription taxonDescription : taxonDescriptions) {
//            if (logger.isDebugEnabled()){ logger.debug("load taxon description " + taxonDescription.getUuid());}
//        	//TODO why not loading all description via .list ? This may improve performance
//            taxonDescription = (TaxonDescription) dao.load(taxonDescription.getUuid(), propertyPaths);
//            Set<DescriptionElementBase> elements = taxonDescription.getElements();
//            for (DescriptionElementBase element : elements) {
//                    if (element.isInstanceOf(Distribution.class)) {
//                        Distribution distribution = (Distribution) element;
//                        if(distribution.getArea() != null){
//                            distList.add(distribution);
//                        }
//                    }
//            }
//        }

        if (logger.isDebugEnabled()){logger.debug("filter tree for " + distList.size() + " distributions ...");}

        // filter distributions
        Collection<Distribution> filteredDistributions = DescriptionUtility.filterDistributions(distList, hiddenAreaMarkerTypes, true, statusOrderPreference, false);
        distList.clear();
        distList.addAll(filteredDistributions);

        return DescriptionUtility.orderDistributions(definedTermDao, omitLevels, distList, hiddenAreaMarkerTypes);
    }


    @Override
    public Pager<TaxonNameDescription> getTaxonNameDescriptions(TaxonNameBase name, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        Integer numberOfResults = dao.countTaxonNameDescriptions(name);

        List<TaxonNameDescription> results = new ArrayList<TaxonNameDescription>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getTaxonNameDescriptions(name, pageSize, pageNumber,propertyPaths);
        }

        return new DefaultPagerImpl<TaxonNameDescription>(pageNumber, numberOfResults, pageSize, results);
    }


    @Override
    public Pager<DescriptionBase> page(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDescriptions(type, hasImages, hasText, feature);

        List<DescriptionBase> results = new ArrayList<DescriptionBase>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.listDescriptions(type, hasImages, hasText, feature, pageSize, pageNumber,orderHints,propertyPaths);
        }

        return new DefaultPagerImpl<DescriptionBase>(pageNumber, numberOfResults, pageSize, results);
    }

    /**
     * FIXME Candidate for harmonization
     * Rename: searchByDistribution
     */
    @Override
    public Pager<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTerm presence,	Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = dao.countDescriptionByDistribution(namedAreas, presence);

        List<TaxonDescription> results = new ArrayList<TaxonDescription>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.searchDescriptionByDistribution(namedAreas, presence, pageSize, pageNumber,orderHints,propertyPaths);
        }

        return new DefaultPagerImpl<TaxonDescription>(pageNumber, numberOfResults, pageSize, results);
    }

    /**
     * FIXME Candidate for harmonization
     * move: descriptionElementService.search
     */
    @Override
    public Pager<DescriptionElementBase> searchElements(Class<? extends DescriptionElementBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Integer numberOfResults = descriptionElementDao.count(clazz, queryString);

        List<DescriptionElementBase> results = new ArrayList<DescriptionElementBase>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = descriptionElementDao.search(clazz, queryString, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<DescriptionElementBase>(pageNumber, numberOfResults, pageSize, results);
    }

    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.find
     */
    @Override
    public DescriptionElementBase getDescriptionElementByUuid(UUID uuid) {
        return descriptionElementDao.findByUuid(uuid);
    }

    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.load
     */
    @Override
    public DescriptionElementBase loadDescriptionElement(UUID uuid,	List<String> propertyPaths) {
        return descriptionElementDao.load(uuid, propertyPaths);
    }

    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.save
     */
    @Override
    @Transactional(readOnly = false)
    public UUID saveDescriptionElement(DescriptionElementBase descriptionElement) {
        return descriptionElementDao.save(descriptionElement).getUuid();
    }

    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.save
     */
    @Override
    @Transactional(readOnly = false)
    public Map<UUID, DescriptionElementBase> saveDescriptionElement(Collection<DescriptionElementBase> descriptionElements) {
        return descriptionElementDao.saveAll(descriptionElements);
    }

    /**
     * FIXME Candidate for harmonization
     * descriptionElementService.delete
     */
    @Override
    public UUID deleteDescriptionElement(DescriptionElementBase descriptionElement) {
        return descriptionElementDao.delete(descriptionElement);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IDescriptionService#deleteDescriptionElement(java.util.UUID)
     */
    @Override
    public UUID deleteDescriptionElement(UUID descriptionElementUuid) {
        return deleteDescriptionElement(descriptionElementDao.load(descriptionElementUuid));
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteDescription(DescriptionBase description) {
        DeleteResult deleteResult = new DeleteResult();

    	if (description instanceof TaxonDescription){
    		TaxonDescription taxDescription = HibernateProxyHelper.deproxy(description, TaxonDescription.class);
    		Taxon tax = taxDescription.getTaxon();
    		tax.removeDescription(taxDescription, true);
    		dao.delete(description);

            deleteResult.addUpdatedObject(tax);
            deleteResult.setCdmEntity(tax);
    	}


        return deleteResult;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IDescriptionService#deleteDescription(java.util.UUID)
     */
    @Override
    @Transactional(readOnly = false)
    public DeleteResult deleteDescription(UUID descriptionUuid) {
        return deleteDescription(dao.load(descriptionUuid));
    }


    @Override
    public TermVocabulary<Feature> getFeatureVocabulary(UUID uuid) {
        return vocabularyDao.findByUuid(uuid);
    }

    @Override
    @Deprecated
    public <T extends DescriptionElementBase> List<T> getDescriptionElementsForTaxon(
            Taxon taxon, Set<Feature> features,
            Class<T> type, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths) {
        return listDescriptionElementsForTaxon(taxon, features, type, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public <T extends DescriptionElementBase> List<T> listDescriptionElementsForTaxon(
            Taxon taxon, Set<Feature> features,
            Class<T> type, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths) {
        return dao.getDescriptionElementForTaxon(taxon.getUuid(), features, type, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public <T extends DescriptionElementBase> Pager<T> pageDescriptionElementsForTaxon(
            Taxon taxon, Set<Feature> features,
            Class<T> type, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths) {
        if (logger.isDebugEnabled()){logger.debug(" get count ...");}
        Long count = dao.countDescriptionElementForTaxon(taxon.getUuid(), features, type);
        List<T> descriptionElements;
        if(AbstractPagerImpl.hasResultsInRange(count, pageNumber, pageSize)){ // no point checking again
            if (logger.isDebugEnabled()){logger.debug(" get list ...");}
            descriptionElements = listDescriptionElementsForTaxon(taxon, features, type, pageSize, pageNumber, propertyPaths);
        } else {
            descriptionElements = new ArrayList<T>(0);
        }
        if (logger.isDebugEnabled()){logger.debug(" service - DONE ...");}
        return new DefaultPagerImpl<T>(pageNumber, count.intValue(), pageSize, descriptionElements);
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

        MarkerType useMarkerType = (MarkerType) definedTermDao.load(UUID.fromString("2e6e42d9-e92a-41f4-899b-03c0ac64f039"));
        boolean isUseDescription = false;
        if(!description.getMarkers().isEmpty()) {
            for (Marker marker: description.getMarkers()) {
                MarkerType markerType = marker.getMarkerType();
                if (markerType.equals(useMarkerType)) {
                    isUseDescription = true;
                }

            }
        }

        if(description.hasStructuredData() && !isUseDescription){


            String lastCategory = null;
            String categorySeparator = ". ";

            List<TextData> textDataList;
            TextData naturalLanguageDescriptionText = null;

            boolean useMicroFormatQuantitativeDescriptionBuilder = false;

            if(useMicroFormatQuantitativeDescriptionBuilder){

                MicroFormatQuantitativeDescriptionBuilder micro = new MicroFormatQuantitativeDescriptionBuilder();
                naturalLanguageGenerator.setQuantitativeDescriptionBuilder(micro);
                naturalLanguageDescriptionText = naturalLanguageGenerator.generateSingleTextData(featureTree, (description), lang);

            } else {

                naturalLanguageDescriptionText = naturalLanguageGenerator.generateSingleTextData(
                        featureTree,
                        (description),
                        lang);
            }

            return naturalLanguageDescriptionText.getText(lang);

//
//			boolean doItBetter = false;
//
//			for (TextData textData : textDataList.toArray(new TextData[textDataList.size()])){
//				if(textData.getMultilanguageText().size() > 0){
//
//					if (!textData.getFeature().equals(Feature.UNKNOWN())) {
//						String featureLabel = textData.getFeature().getLabel(lang);
//
//						if(doItBetter){
//							/*
//							 *  WARNING
//							 *  The code lines below are desinged to handle
//							 *  a special case where as the feature label contains
//							 *  hierarchical information on the features. This code
//							 *  exist only as a base for discussion, and is not
//							 *  intendet to be used in production.
//							 */
//							featureLabel = StringUtils.remove(featureLabel, '>');
//
//							String[] labelTokens = StringUtils.split(featureLabel, '<');
//							if(labelTokens[0].equals(lastCategory) && labelTokens.length > 1){
//								if(naturalLanguageDescription.length() > 0){
//									naturalLanguageDescription.append(separator);
//								}
//								naturalLanguageDescription.append(labelTokens[1]);
//							} else {
//								if(naturalLanguageDescription.length() > 0){
//									naturalLanguageDescription.append(categorySeparator);
//								}
//								naturalLanguageDescription.append(StringUtils.join(labelTokens));
//							}
//							lastCategory = labelTokens[0];
//							// end of demo code
//						} else {
//							if(naturalLanguageDescription.length() > 0){
//								naturalLanguageDescription.append(separator);
//							}
//							naturalLanguageDescription.append(textData.getFeature().getLabel(lang));
//						}
//					} else {
//						if(naturalLanguageDescription.length() > 0){
//							naturalLanguageDescription.append(separator);
//						}
//					}
//					String text = textData.getMultilanguageText().values().iterator().next().getText();
//					naturalLanguageDescription.append(text);
//
//				}
//			}

        }
        else if (isUseDescription) {
            //AT: Left Blank in case we need to generate a Natural language text string.
        }
        return naturalLanguageDescription.toString();
    }


    @Override
    public boolean hasStructuredData(DescriptionBase<?> description) {
        return load(description.getUuid()).hasStructuredData();
    }


    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveDescriptionElementsToDescription(
            Collection<DescriptionElementBase> descriptionElements,
            DescriptionBase targetDescription,
            boolean isCopy) {

        UpdateResult result = new UpdateResult();
        if (descriptionElements.isEmpty() ){
            return result;
        }

        if (! isCopy && descriptionElements == descriptionElements.iterator().next().getInDescription().getElements()){
            //if the descriptionElements collection is the elements set of a description, put it in a separate set before to avoid concurrent modification exceptions
            descriptionElements = new HashSet<DescriptionElementBase>(descriptionElements);
//			descriptionElementsTmp.addAll(descriptionElements);
//			descriptionElements = descriptionElementsTmp;
        }
        for (DescriptionElementBase element : descriptionElements){
            DescriptionBase<?> description = element.getInDescription();
            try {
                DescriptionElementBase newElement = (DescriptionElementBase)element.clone();
                targetDescription.addElement(newElement);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException ("Clone not yet implemented for class " + element.getClass().getName(), e);
            }
            if (! isCopy){
                description.removeElement(element);
                dao.saveOrUpdate(description);
            }
            result.addUpdatedObject(description);

        }
        dao.saveOrUpdate(targetDescription);
        result.addUpdatedObject(targetDescription);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveDescriptionElementsToDescription(
            Set<UUID> descriptionElementUUIDs,
            UUID targetDescriptionUuid,
            boolean isCopy) {
        Set<DescriptionElementBase> descriptionElements = new HashSet<DescriptionElementBase>();
        for(UUID deUuid : descriptionElementUUIDs) {
            descriptionElements.add(descriptionElementDao.load(deUuid));
        }
        DescriptionBase targetDescription = dao.load(targetDescriptionUuid);

        return moveDescriptionElementsToDescription(descriptionElements, targetDescription, isCopy);
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveDescriptionElementsToDescription(
            Set<UUID> descriptionElementUUIDs,
            UUID targetTaxonUuid,
            String moveMessage,
            boolean isCopy) {
        Taxon targetTaxon = CdmBase.deproxy(taxonDao.load(targetTaxonUuid), Taxon.class);
        DescriptionBase targetDescription = TaxonDescription.NewInstance(targetTaxon);
        targetDescription.setTitleCache(moveMessage, true);
        Annotation annotation = Annotation.NewInstance(moveMessage, Language.getDefaultLanguage());
        annotation.setAnnotationType(AnnotationType.TECHNICAL());
        targetDescription.addAnnotation(annotation);

        targetDescription = dao.save(targetDescription);
        Set<DescriptionElementBase> descriptionElements = new HashSet<DescriptionElementBase>();
        for(UUID deUuid : descriptionElementUUIDs) {
            descriptionElements.add(descriptionElementDao.load(deUuid));
        }

        return moveDescriptionElementsToDescription(descriptionElements, targetDescription, isCopy);
    }

    @Override
    public Pager<TermDto> pageNamedAreasInUse(boolean includeAllParents, Integer pageSize,
            Integer pageNumber){
        List<TermDto> results = dao.listNamedAreasInUse(includeAllParents, null, null);
        int startIndex= pageNumber * pageSize;
        int toIndex = Math.min(startIndex + pageSize, results.size());
        List<TermDto> page = results.subList(startIndex, toIndex);
        return new DefaultPagerImpl<TermDto>(pageNumber, results.size(), pageSize, page);
    }


    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveTaxonDescriptions(Taxon sourceTaxon, Taxon targetTaxon) {
        List<TaxonDescription> descriptions = new ArrayList(sourceTaxon.getDescriptions());
        UpdateResult result = new UpdateResult();
        result.addUpdatedObject(sourceTaxon);
        result.addUpdatedObject(targetTaxon);
        for(TaxonDescription description : descriptions){

            String moveMessage = String.format("Description moved from %s", sourceTaxon);
            if(description.isProtectedTitleCache()){
                String separator = "";
                if(!StringUtils.isBlank(description.getTitleCache())){
                    separator = " - ";
                }
                description.setTitleCache(description.getTitleCache() + separator + moveMessage, true);
            }
            Annotation annotation = Annotation.NewInstance(moveMessage, Language.getDefaultLanguage());
            annotation.setAnnotationType(AnnotationType.TECHNICAL());
            description.addAnnotation(annotation);
            targetTaxon.addDescription(description);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveTaxonDescriptions(UUID sourceTaxonUuid, UUID targetTaxonUuid) {
        Taxon sourceTaxon = HibernateProxyHelper.deproxy(taxonDao.load(sourceTaxonUuid), Taxon.class);
        Taxon targetTaxon = HibernateProxyHelper.deproxy(taxonDao.load(targetTaxonUuid), Taxon.class);
        return moveTaxonDescriptions(sourceTaxon, targetTaxon);

    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveTaxonDescription(UUID descriptionUuid, UUID targetTaxonUuid){
        UpdateResult result = new UpdateResult();
        TaxonDescription description = HibernateProxyHelper.deproxy(dao.load(descriptionUuid), TaxonDescription.class);

        Taxon sourceTaxon = description.getTaxon();
        String moveMessage = String.format("Description moved from %s", sourceTaxon);
        if(description.isProtectedTitleCache()){
            String separator = "";
            if(!StringUtils.isBlank(description.getTitleCache())){
                separator = " - ";
            }
            description.setTitleCache(description.getTitleCache() + separator + moveMessage, true);
        }
        Annotation annotation = Annotation.NewInstance(moveMessage, Language.getDefaultLanguage());
        annotation.setAnnotationType(AnnotationType.TECHNICAL());
        description.addAnnotation(annotation);
        Taxon targetTaxon = HibernateProxyHelper.deproxy(taxonDao.load(targetTaxonUuid), Taxon.class);
        targetTaxon.addDescription(description);
        result.addUpdatedObject(targetTaxon);
        result.addUpdatedObject(sourceTaxon);
        dao.merge(description);
        return result;

    }


}
