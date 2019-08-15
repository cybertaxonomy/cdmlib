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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.dto.TaxonDistributionDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.AbstractPagerImpl;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.utility.DescriptionUtility;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptiveDataSetDao;
import eu.etaxonomy.cdm.persistence.dao.description.IStatisticalMeasurementValueDao;
import eu.etaxonomy.cdm.persistence.dao.description.ITermNodeDao;
import eu.etaxonomy.cdm.persistence.dao.description.ITermTreeDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.mueller
 * @author a.kohlbecker
 *
 * @since 24.06.2008
 *
 */
@Service
@Transactional(readOnly = true)
public class DescriptionServiceImpl
        extends IdentifiableServiceBase<DescriptionBase,IDescriptionDao>
        implements IDescriptionService {

    private static final Logger logger = Logger.getLogger(DescriptionServiceImpl.class);

    protected IDescriptionElementDao descriptionElementDao;
    protected ITermTreeDao featureTreeDao;
    protected IDescriptiveDataSetDao descriptiveDataSetDao;
    protected ITermNodeDao termNodeDao;
    protected ITermVocabularyDao vocabularyDao;
    protected IDefinedTermDao definedTermDao;
    protected IStatisticalMeasurementValueDao statisticalMeasurementValueDao;
    protected ITaxonDao taxonDao;
    protected ITaxonNameDao nameDao;
    protected IOccurrenceDao occurrenceDao;
    protected ITaxonNodeDao taxonNodeDao;
    protected IDescriptiveDataSetDao dataSetDao;

    @Autowired
    private IProgressMonitorService progressMonitorService;

    //TODO change to Interface
    private NaturalLanguageGenerator naturalLanguageGenerator;

    @Autowired
    protected void setFeatureTreeDao(ITermTreeDao featureTreeDao) {
        this.featureTreeDao = featureTreeDao;
    }

    @Autowired
    protected void setDescriptiveDataSetDao(IDescriptiveDataSetDao descriptiveDataSetDao) {
        this.descriptiveDataSetDao = descriptiveDataSetDao;
    }

    @Autowired
    protected void setTermNodeDao(ITermNodeDao featureNodeDao) {
        this.termNodeDao = featureNodeDao;
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

    @Autowired
    protected void setTaxonNodeDao(ITaxonNodeDao taxonNodeDao) {
        this.taxonNodeDao = taxonNodeDao;
    }

    @Autowired
    protected void setDataSetDao(IDescriptiveDataSetDao dataSetDao) {
        this.dataSetDao = dataSetDao;
    }

    /**
     *
     */
    public DescriptionServiceImpl() {
        logger.debug("Load DescriptionService Bean");
    }


    @Override
    @Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends DescriptionBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<DescriptionBase> cacheStrategy, IProgressMonitor monitor) {
        if (clazz == null){
            clazz = DescriptionBase.class;
        }
        return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
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
    public long count(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText,Set<Feature> feature) {
        return dao.countDescriptions(type, hasImages, hasText, feature);
    }

    @Override
    public <T extends DescriptionElementBase> Pager<T> pageDescriptionElements(DescriptionBase description, Class<? extends DescriptionBase> descriptionType,
            Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        List<T> results = listDescriptionElements(description, descriptionType, features, type, pageSize, pageNumber, propertyPaths);
        return new DefaultPagerImpl<>(pageNumber, results.size(), pageSize, results);
    }

    @Override
    @Deprecated
    public <T extends DescriptionElementBase> Pager<T> getDescriptionElements(DescriptionBase description,
            Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        return pageDescriptionElements(description, null, features, type, pageSize, pageNumber, propertyPaths);
    }



    @Override
    public <T extends DescriptionElementBase> List<T> listDescriptionElements(DescriptionBase description,
            Class<? extends DescriptionBase> descriptionType, Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber,
            List<String> propertyPaths) {

        long numberOfResults = dao.countDescriptionElements(description, descriptionType, features, type);
        List<T> results = new ArrayList<T>();
        if(AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)) {
            results = dao.getDescriptionElements(description, descriptionType, features, type, pageSize, pageNumber, propertyPaths);
        }
        return results;

    }


    @Override
    @Deprecated
    public <T extends DescriptionElementBase> List<T> listDescriptionElements(DescriptionBase description,
            Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        return listDescriptionElements(description, null, features, type, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public Pager<Annotation> getDescriptionElementAnnotations(DescriptionElementBase annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        long numberOfResults = descriptionElementDao.countAnnotations(annotatedObj, status);

        List<Annotation> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = descriptionElementDao.getAnnotations(annotatedObj, status, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
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

    @Override
    public Pager<TaxonDescription> pageTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes);

        List<TaxonDescription> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    @Override
    public List<TaxonDescription> listTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScope, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        List<TaxonDescription> results = dao.listTaxonDescriptions(taxon, scopes, geographicalScope, markerTypes, pageSize, pageNumber, propertyPaths);
        return results;
    }


    @Override
    public List<Media> listTaxonDescriptionMedia(UUID taxonUuid, boolean limitToGalleries, Set<MarkerType> markerTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths){
        return this.dao.listTaxonDescriptionMedia(taxonUuid, limitToGalleries, markerTypes, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public int countTaxonDescriptionMedia(UUID taxonUuid, boolean limitToGalleries, Set<MarkerType> markerTypes){
        return this.dao.countTaxonDescriptionMedia(taxonUuid, limitToGalleries, markerTypes);
    }

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

        return DescriptionUtility.orderDistributions(definedTermDao, omitLevels, distList, hiddenAreaMarkerTypes, null);
    }


    @Override
    public Pager<TaxonNameDescription> getTaxonNameDescriptions(TaxonName name, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        long numberOfResults = dao.countTaxonNameDescriptions(name);

        List<TaxonNameDescription> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getTaxonNameDescriptions(name, pageSize, pageNumber,propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }


    @Override
    public Pager<DescriptionBase> page(Class<? extends DescriptionBase> type, Boolean hasImages, Boolean hasText, Set<Feature> feature, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        long numberOfResults = dao.countDescriptions(type, hasImages, hasText, feature);

        @SuppressWarnings("rawtypes")
        List<DescriptionBase> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.listDescriptions(type, hasImages, hasText, feature, pageSize, pageNumber,orderHints,propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    /**
     * FIXME Candidate for harmonization
     * Rename: searchByDistribution
     */
    @Override
    public Pager<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTerm presence,	Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        long numberOfResults = dao.countDescriptionByDistribution(namedAreas, presence);

        List<TaxonDescription> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.searchDescriptionByDistribution(namedAreas, presence, pageSize, pageNumber,orderHints,propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

    /**
     * FIXME Candidate for harmonization
     * move: descriptionElementService.search
     */
    @Override
//    public Pager<T> searchElements(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
    public <S extends DescriptionElementBase> Pager<S> searchElements(Class<S> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        long numberOfResults = descriptionElementDao.count(clazz, queryString);

        List<S> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = (List<S>)descriptionElementDao.search(clazz, queryString, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
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

    @Override
    @Transactional(readOnly = false)
    public List<MergeResult<DescriptionBase>> mergeDescriptionElements(Collection<TaxonDistributionDTO> descriptionElements, boolean returnTransientEntity) {
        List<MergeResult<DescriptionBase>> mergedObjects = new ArrayList();
        List<Distribution> toDelete = new ArrayList<>();
        for(TaxonDistributionDTO obj : descriptionElements) {
            Iterator<TaxonDescription> iterator = obj.getDescriptionsWrapper().getDescriptions().iterator();
            while (iterator.hasNext()){
                TaxonDescription desc = iterator.next();
                mergedObjects.add(dao.merge(desc, returnTransientEntity));
            }


        }

        return mergedObjects;
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
        description = load(description.getId(), Arrays.asList("descriptiveDataSets"));//avoid lazy init exception

    	if (description instanceof TaxonDescription){
    		TaxonDescription taxDescription = HibernateProxyHelper.deproxy(description, TaxonDescription.class);
    		Taxon tax = taxDescription.getTaxon();
    		tax.removeDescription(taxDescription, true);
            deleteResult.addUpdatedObject(tax);
    	}
    	else if (HibernateProxyHelper.isInstanceOf(description, SpecimenDescription.class)){
    	    SpecimenDescription specimenDescription = HibernateProxyHelper.deproxy(description, SpecimenDescription.class);
    	    SpecimenOrObservationBase specimen = specimenDescription.getDescribedSpecimenOrObservation();
    	    specimen.removeDescription(specimenDescription);
    	    deleteResult.addUpdatedObject(specimen);
    	}

    	Set<DescriptiveDataSet> descriptiveDataSets = description.getDescriptiveDataSets();
    	for (Iterator<DescriptiveDataSet> iterator = descriptiveDataSets.iterator(); iterator.hasNext();) {
    	    iterator.next().removeDescription(description);
        }

    	dao.delete(description);
    	deleteResult.addDeletedObject(description);
    	deleteResult.setCdmEntity(description);


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
        return new DefaultPagerImpl<T>(pageNumber, count, pageSize, descriptionElements);
    }

    @Override
    public String generateNaturalLanguageDescription(TermTree featureTree,
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
   // @Transactional(readOnly = false)
    public UpdateResult moveDescriptionElementsToDescription(
            Collection<DescriptionElementBase> descriptionElements,
            DescriptionBase targetDescription,
            boolean isCopy,
            boolean setNameInSource) {

        UpdateResult result = new UpdateResult();
        if (descriptionElements.isEmpty() || descriptionElements.iterator().next() == null){
            result.setAbort();
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
            description = dao.load(description.getUuid());
            Taxon taxon;
            TaxonName name = null;
            if (description instanceof TaxonDescription){
                TaxonDescription taxonDescription = HibernateProxyHelper.deproxy(description, TaxonDescription.class);
                if (taxonDescription.getTaxon() != null){
                    taxon = (Taxon) taxonDao.load(taxonDescription.getTaxon().getUuid());
                    name = taxon.getName();
                }


            }
            try {
                DescriptionElementBase newElement = (DescriptionElementBase)element.clone();
                if (setNameInSource) {
                    for (DescriptionElementBase descElement: descriptionElements){
                        for (DescriptionElementSource source: element.getSources()){
                            if (source.getNameUsedInSource() == null){
                                source.setNameUsedInSource(name);
                            }
                        }
                    }
                }
                targetDescription.addElement(newElement);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException ("Clone not yet implemented for class " + element.getClass().getName(), e);
            }
            if (! isCopy){
                description.removeElement(element);
                dao.saveOrUpdate(description);
                result.addUpdatedObject(description);
//                if (description.getElements().isEmpty()){
//                   if (description instanceof TaxonDescription){
//                       TaxonDescription taxDescription = HibernateProxyHelper.deproxy(description, TaxonDescription.class);
//                       if (taxDescription.getTaxon() != null){
//                           taxDescription.getTaxon().removeDescription((TaxonDescription)description);
//                       }
//                   }
//                    dao.delete(description);
//
//                }//else{
//                    dao.saveOrUpdate(description);
//                    result.addUpdatedObject(description);
//                }
            }


        }
        dao.saveOrUpdate(targetDescription);
        result.addUpdatedObject(targetDescription);
        if (targetDescription instanceof TaxonDescription){
            result.addUpdatedObject(((TaxonDescription)targetDescription).getTaxon());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveDescriptionElementsToDescription(
            Set<UUID> descriptionElementUUIDs,
            UUID targetDescriptionUuid,
            boolean isCopy, boolean setNameInSource) {
        Set<DescriptionElementBase> descriptionElements = new HashSet<DescriptionElementBase>();
        for(UUID deUuid : descriptionElementUUIDs) {
            DescriptionElementBase element = descriptionElementDao.load(deUuid);
            if (element != null){
                descriptionElements.add(element);
            }
        }
        DescriptionBase targetDescription = dao.load(targetDescriptionUuid);

        return moveDescriptionElementsToDescription(descriptionElements, targetDescription, isCopy, setNameInSource);
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveDescriptionElementsToDescription(
            Set<UUID> descriptionElementUUIDs,
            DescriptionBase targetDescription,
            boolean isCopy, boolean setNameInSource) {
        Set<DescriptionElementBase> descriptionElements = new HashSet<DescriptionElementBase>();
        for(UUID deUuid : descriptionElementUUIDs) {
            DescriptionElementBase element = descriptionElementDao.load(deUuid);
            if (element != null){
                descriptionElements.add(element);
            }
        }
        DescriptionBase newTargetDescription;
        if (targetDescription.isPersited()){
            newTargetDescription = dao.load(targetDescription.getUuid());
        }else{
            if (targetDescription instanceof TaxonDescription){
                Taxon taxon = (Taxon)taxonDao.load(((TaxonDescription)targetDescription).getTaxon().getUuid());

                newTargetDescription = TaxonDescription.NewInstance(taxon, targetDescription.isImageGallery());

            }else if (targetDescription instanceof TaxonNameDescription){
                TaxonName name = nameDao.load(((TaxonNameDescription)targetDescription).getTaxonName().getUuid());
                newTargetDescription = TaxonNameDescription.NewInstance(name);
            }else {
                SpecimenOrObservationBase specimen = occurrenceDao.load(((SpecimenDescription)targetDescription).getDescribedSpecimenOrObservation().getUuid());
                newTargetDescription = SpecimenDescription.NewInstance(specimen);
            }

            newTargetDescription.addSources(targetDescription.getSources());
            newTargetDescription.setTitleCache(targetDescription.getTitleCache(), targetDescription.isProtectedTitleCache());

        }
        return moveDescriptionElementsToDescription(descriptionElements, newTargetDescription, isCopy, setNameInSource);
    }


    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveDescriptionElementsToDescription(
            Set<UUID> descriptionElementUUIDs,
            UUID targetTaxonUuid,
            String moveMessage,
            boolean isCopy, boolean setNameInSource) {
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

        return moveDescriptionElementsToDescription(descriptionElements, targetDescription, isCopy, setNameInSource);
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
    public UpdateResult moveTaxonDescriptions(Taxon sourceTaxon, Taxon targetTaxon, boolean setNameInSource) {
        List<TaxonDescription> descriptions = new ArrayList<>(sourceTaxon.getDescriptions());
        UpdateResult result = new UpdateResult();
        result.addUpdatedObject(sourceTaxon);
        result.addUpdatedObject(targetTaxon);
        for(TaxonDescription description : descriptions){
            targetTaxon.addDescription(prepareDescriptionForMove(description, sourceTaxon, setNameInSource));
        }
        return result;
    }

    private TaxonDescription prepareDescriptionForMove(TaxonDescription description, Taxon sourceTaxon, boolean setNameInSource){
        String moveMessage = String.format("Description moved from %s", sourceTaxon);
        if(description.isProtectedTitleCache()){
            String separator = "";
            if(!StringUtils.isBlank(description.getTitleCache())){
                separator = " - ";
            }
            description.setTitleCache(description.getTitleCache() + separator + moveMessage, true);
        }
        else{
            description.setTitleCache(moveMessage, true);
        }
        Annotation annotation = Annotation.NewInstance(moveMessage, Language.getDefaultLanguage());
        annotation.setAnnotationType(AnnotationType.TECHNICAL());
        description.addAnnotation(annotation);
        if(setNameInSource){
            for (DescriptionElementBase element: description.getElements()){
                for (DescriptionElementSource source: element.getSources()){
                    if (source.getNameUsedInSource() == null){
                        source.setNameUsedInSource(sourceTaxon.getName());
                    }
                }
            }
        }
        return description;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveTaxonDescriptions(UUID sourceTaxonUuid, UUID targetTaxonUuid, boolean setNameInSource) {
        Taxon sourceTaxon = HibernateProxyHelper.deproxy(taxonDao.load(sourceTaxonUuid), Taxon.class);
        Taxon targetTaxon = HibernateProxyHelper.deproxy(taxonDao.load(targetTaxonUuid), Taxon.class);
        return moveTaxonDescriptions(sourceTaxon, targetTaxon, setNameInSource);

    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveTaxonDescription(UUID descriptionUuid, UUID targetTaxonUuid, boolean setNameInSource){
        TaxonDescription description = HibernateProxyHelper.deproxy(dao.load(descriptionUuid), TaxonDescription.class);
        Taxon targetTaxon = HibernateProxyHelper.deproxy(taxonDao.load(targetTaxonUuid), Taxon.class);
        Taxon sourceTaxon = description.getTaxon();
        UpdateResult result = new UpdateResult();
        result.addUpdatedObject(sourceTaxon);
        result.addUpdatedObject(targetTaxon);

        targetTaxon.addDescription(prepareDescriptionForMove(description, sourceTaxon, setNameInSource));
        return result;

    }


}
