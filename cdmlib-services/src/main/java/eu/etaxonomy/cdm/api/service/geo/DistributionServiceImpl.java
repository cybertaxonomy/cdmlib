/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto.InfoPart;
import eu.etaxonomy.cdm.api.dto.portal.IDistributionTree;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionInfoConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionOrder;
import eu.etaxonomy.cdm.api.service.portal.PortalDtoLoader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistribution;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermTreeDao;
import eu.etaxonomy.cdm.persistence.dao.term.ITermVocabularyDao;

/**
 * @author a.mueller
 * @date 08.02.2023
 */
@Service
@Transactional(readOnly = true)
public class DistributionServiceImpl implements IDistributionService {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private IDescriptionDao dao;

    @Autowired
    private IDefinedTermDao termDao;

    @Autowired
    private ITermTreeDao termTreeDao;

    @Autowired
    private ITermVocabularyDao vocabDao;

    @Autowired
    private IGeoServiceAreaMapping areaMapping;

    @Override
    public DistributionInfoDto composeDistributionInfoFor(DistributionInfoConfiguration config, UUID taxonUUID,
            boolean neverUseFallbackAreaAsParent,
            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> languages, List<String> propertyPaths){

        Set<UUID> featureUuids = config.getFeatures();
        Set<Feature> features = null;
        if (!CdmUtils.isNullSafeEmpty(featureUuids)) {
            features = termDao.list(featureUuids, null, null, null, null)
                    .stream().filter(t->t.isInstanceOf(Feature.class)).map(t->(Feature)t).collect(Collectors.toSet());
        }

        if (propertyPaths == null){
            propertyPaths = Arrays.asList(new String []{});
        }
        // Adding default initStrategies to improve the performance of this method
        // adding 'status' and 'area' has a good positive effect:
        // filterDistributions() only takes 21% of the total method time (before it was 46%)
        // at the same time the cost of the getDescriptionElementForTaxon is not increased at all!
        //
        // adding 'markers.markerType' is not improving the performance since it only
        // moved the load from the filter method to the getDescriptionElementForTaxon()
        // method.
        // overall improvement by this means is by 42% (from 77,711 ms to 44,868 ms)
        ArrayList<String> initStrategy = new ArrayList<>(propertyPaths);
        if(!initStrategy.contains("status")) {
            initStrategy.add("status");
        }
        if(!initStrategy.contains("area")) {
            initStrategy.add("area");
        }
        if(!initStrategy.contains("markers.markerType")) {
            initStrategy.add("markers.markerType");
        }

        List<Distribution> distributions = dao.getDescriptionElementForTaxon(
                taxonUUID, features, Distribution.class, config.isIncludeUnpublished(), null, null, initStrategy);

        return composeDistributionInfoFor(config, distributions, neverUseFallbackAreaAsParent, presenceAbsenceTermColors, languages);
    }

    @Override
    public DistributionInfoDto composeDistributionInfoFor(DistributionInfoConfiguration config, List<Distribution> distributions,
            boolean neverUseFallbackAreaAsParent, Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> languages){

        EnumSet<DistributionInfoDto.InfoPart> parts = config.getInfoParts();
        boolean subAreaPreference = config.isPreferSubareas();
        boolean statusOrderPreference = config.isStatusOrderPreference();
        Set<MarkerType> fallbackAreaMarkerTypes = config.getFallbackAreaMarkerTypeList();
        Set<MarkerType> alternativeRootAreaMarkerTypes = config.getAlternativeRootAreaMarkerTypes();
        Set<NamedAreaLevel> omitLevels = config.getOmitLevels();
        CondensedDistributionConfiguration condensedDistConfig = config.getCondensedDistributionConfiguration();
        DistributionOrder distributionOrder = config.getDistributionOrder();

        final boolean PREFER_AGGREGATED = true;
        final boolean PREFER_SUBAREA = true;

        DistributionInfoDto dto = new DistributionInfoDto();

        if(omitLevels == null) {
            @SuppressWarnings("unchecked")  //2 lines to allow unchecked annotation
            Set<NamedAreaLevel> emptySet = Collections.EMPTY_SET;
            omitLevels = emptySet;
        }

        //area tree
        TermTree<NamedArea> areaTree = getPersistentAreaTree(distributions, config);
        if (areaTree == null) {
            //TODO better use areaTree created within filterDistributions(...) but how to get it easily?
            areaTree = DistributionServiceUtilities.getAreaTree(distributions, fallbackAreaMarkerTypes);
        }

        //TODO unify to use only the node map
        SetMap<NamedArea, NamedArea> parentAreaMap = areaTree.getParentMap();
        SetMap<NamedArea, TermNode<NamedArea>> parentAreaNodeMap = areaTree.getParentNodeMap();
        SetMap<NamedArea, TermNode<NamedArea>> area2TermNodesMap = areaTree.getTermNodesMap();

        //status tree
        TermTree<PresenceAbsenceTerm> statusTree = getPersistentStatusTree(config);

        // for all later applications apply the rules statusOrderPreference, hideHiddenArea
        // and statusTree(statusFilter) to all distributions, but KEEP fallback area distributions
        boolean keepFallBackOnlyIfNoSubareaDataExists = false;
        Set<Distribution> filteredDistributions = DistributionServiceUtilities.filterDistributions(distributions,
                areaTree, statusTree, fallbackAreaMarkerTypes, !PREFER_AGGREGATED, statusOrderPreference, !PREFER_SUBAREA,
                keepFallBackOnlyIfNoSubareaDataExists);

        if(parts.contains(InfoPart.elements)) {
            dto.setElements(filteredDistributions);
        }

        if(parts.contains(InfoPart.tree)) {
            IDistributionTree tree;
            if (config.isUseTreeDto()) {

                //transform distributions to distribution DTOs
                Set<DistributionDto> filteredDtoDistributions = new HashSet<>();
                for (Distribution distribution : filteredDistributions) {
                    DistributionDto distDto = new DistributionDto(distribution, parentAreaMap);
                    PortalDtoLoader.loadBaseData(distribution, distDto);
                    distDto.setTimeperiod(distribution.getTimeperiod() == null ? null : distribution.getTimeperiod().toString());
                    filteredDtoDistributions.add(distDto);
                }

                boolean useSecondMethod = false;
                tree = DistributionServiceUtilities.buildOrderedTreeDto(omitLevels,
                        filteredDtoDistributions, parentAreaMap, areaTree, fallbackAreaMarkerTypes,
                        alternativeRootAreaMarkerTypes, neverUseFallbackAreaAsParent,
                        distributionOrder, termDao, useSecondMethod);
            }else {
                //version with model entities as used in direct webservice (not taxon page DTO)
                //TODO this is probably not in use anymore
                tree = DistributionServiceUtilities.buildOrderedTree(omitLevels,
                        filteredDistributions, parentAreaMap, fallbackAreaMarkerTypes,
                        alternativeRootAreaMarkerTypes, neverUseFallbackAreaAsParent,
                        distributionOrder, termDao);
            }
            dto.setTree(tree);
        }

        if(parts.contains(InfoPart.condensedDistribution)) {
            CondensedDistribution condensedDistribution = DistributionServiceUtilities.getCondensedDistribution(
                    filteredDistributions, area2TermNodesMap, condensedDistConfig, languages);
            dto.setCondensedDistribution(condensedDistribution);
        }

        if (parts.contains(InfoPart.mapUriParams)) {
            boolean IGNORE_STATUS_ORDER_PREF = false;
            Set<MarkerType> fallbackAreaMarkerType = null;
            // only apply the subAreaPreference rule for the maps
            keepFallBackOnlyIfNoSubareaDataExists = true;
            //this filters again, but this time with subarea preference rule and fallback area removal
            Set<Distribution> filteredMapDistributions = DistributionServiceUtilities.filterDistributions(
                    filteredDistributions, areaTree, statusTree, fallbackAreaMarkerType, !PREFER_AGGREGATED,
                    IGNORE_STATUS_ORDER_PREF, subAreaPreference, keepFallBackOnlyIfNoSubareaDataExists);

            String mapUri = DistributionServiceUtilities.getDistributionServiceRequestParameterString(
                    filteredMapDistributions,
                    areaMapping,
                    presenceAbsenceTermColors,
                    null, languages);
            dto.setMapUriParams(mapUri);
        }

        return dto;
    }

    private TermTree<NamedArea> getPersistentAreaTree(List<Distribution> distributions, DistributionInfoConfiguration config) {
        UUID areaTreeUuid = config.getAreaTree();
        if (areaTreeUuid == null) {
            return null;
        }
        //TODO property path
        String[] propertyPath = new String[] {};
        @SuppressWarnings("unchecked")
        TermTree<NamedArea> areaTree = termTreeDao.load(areaTreeUuid, Arrays.asList(propertyPath));
        return areaTree;
    }

    private TermTree<PresenceAbsenceTerm> getPersistentStatusTree(DistributionInfoConfiguration config) {
        UUID statusTreeUuid = config.getStatusTree();
        if (statusTreeUuid == null) {
            return null;
        }
        //TODO property path
        String[] propertyPath = new String[] {};
        @SuppressWarnings("unchecked")
        TermTree<PresenceAbsenceTerm> statusTree = termTreeDao.load(statusTreeUuid, Arrays.asList(propertyPath));
        return statusTree;
    }

    @Override
    public CondensedDistribution getCondensedDistribution(Set<Distribution> distributions,
            TermTree<NamedArea> areaTree,
            TermTree<PresenceAbsenceTerm> statusTree,
            boolean statusOrderPreference,
            Set<MarkerType> fallbackAreaMarkerTypes,
            CondensedDistributionConfiguration config,
            List<Language> langs) {

        //TODO exclude "undefined" status as long as status tree is not yet
        areaTree = areaTree == null ? DistributionServiceUtilities.getAreaTree(distributions, fallbackAreaMarkerTypes) : areaTree;
        SetMap<NamedArea,TermNode<NamedArea>> parentNodeMap = areaTree.getParentNodeMap();
        Collection<Distribution> filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, null, statusTree, fallbackAreaMarkerTypes, false, statusOrderPreference, false, false);
        CondensedDistribution condensedDistribution = DistributionServiceUtilities.getCondensedDistribution(
                filteredDistributions,
                parentNodeMap,
                config,
                langs);
        return condensedDistribution;
    }

    @Override
    public void setMapping(NamedArea area, GeoServiceArea geoServiceArea) {
        areaMapping.set(area, geoServiceArea);
    }

    @Override
    public String getDistributionServiceRequestParameterString(List<TaxonDescription> taxonDescriptions,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> langs,
            boolean includeUnpublished) {

        Set<Feature> features = new HashSet<>();
        features.add(Feature.DISTRIBUTION()); //for now only this one
        Set<Distribution> distributions = getDistributionsOf(taxonDescriptions, features, includeUnpublished);

        String uriParams = getDistributionServiceRequestParameterString(distributions,
                subAreaPreference,
                statusOrderPreference,
                hideMarkedAreas,
                presenceAbsenceTermColors,
                langs);

        return uriParams;
    }

    private Set<Distribution> getDistributionsOf(List<TaxonDescription> taxonDescriptions, Set<Feature> features, boolean includeUnpublished) {
        Set<Distribution> result = new HashSet<>();

        for (TaxonDescription taxonDescription : taxonDescriptions) {
            List<Distribution> distributions;
            if (taxonDescription.getId() > 0){
                distributions = dao.getDescriptionElements(taxonDescription,
                        null, features, Distribution.class, includeUnpublished, null, null, null);
            }else{
                distributions = new ArrayList<>();
                for (DescriptionElementBase deb : taxonDescription.getElements()){
                    if (deb.isInstanceOf(Distribution.class)){
                        if (features == null || features.isEmpty()
                                || features.contains(deb.getFeature())) {
                            distributions.add(CdmBase.deproxy(deb, Distribution.class));
                        }
                    }
                }
            }
            result.addAll(distributions);
        }
        return result;
    }

    @Override
    public String getDistributionServiceRequestParameterString(
            Set<Distribution> distributions,
            boolean subAreaPreference,
            boolean statusOrderPreference,
            Set<MarkerType> hideMarkedAreas,
            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors,
            List<Language> langs) {

        TermTree<NamedArea> areaTree = null;
        TermTree<PresenceAbsenceTerm> statusTree = null;
        boolean keepFallbackOnlyIfNoSubareaDataExists = true;
        Collection<Distribution> filteredDistributions = DistributionServiceUtilities.filterDistributions(
                distributions, areaTree, statusTree,
                hideMarkedAreas, false, statusOrderPreference,
                subAreaPreference, keepFallbackOnlyIfNoSubareaDataExists);

        String uriParams = DistributionServiceUtilities.getDistributionServiceRequestParameterString(
                filteredDistributions,
                areaMapping,
                presenceAbsenceTermColors,
                null, langs);
        return uriParams;
    }

    @Override
    @Transactional(readOnly=false)
    public Map<NamedArea, String> mapShapeFileToNamedAreas(Reader csvReader,
            List<String> idSearchFields, String wmsLayerName, UUID areaVocabularyUuid,
            Set<UUID> namedAreaUuids) throws IOException {

        Set<NamedArea> areas = new HashSet<>();

        if(areaVocabularyUuid != null){
            @SuppressWarnings("unchecked")
            TermVocabulary<NamedArea> areaVocabulary = vocabDao.load(areaVocabularyUuid);
            if(areaVocabulary == null){
                throw new EntityNotFoundException("No Vocabulary found for uuid " + areaVocabularyUuid);
            }
            areas.addAll(areaVocabulary.getTerms());
        }
        if(namedAreaUuids != null && !namedAreaUuids.isEmpty()){
            for(DefinedTermBase<?> dtb : termDao.list(namedAreaUuids, null, null, null, null)){
                areas.add((NamedArea)CdmBase.deproxy(dtb));
            }
        }

        ShpAttributesToNamedAreaMapper mapper = new ShpAttributesToNamedAreaMapper(areas, areaMapping);
        Map<NamedArea, String> resultMap = mapper.readCsv(csvReader, idSearchFields, wmsLayerName);
        termDao.saveOrUpdateAll((Collection)areas);
        return resultMap;
    }
}