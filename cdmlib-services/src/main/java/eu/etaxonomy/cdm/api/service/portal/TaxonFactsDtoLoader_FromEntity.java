/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.dto.portal.CommonNameDto;
import eu.etaxonomy.cdm.api.dto.portal.ContainerDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDtoBase;
import eu.etaxonomy.cdm.api.dto.portal.FeatureDto;
import eu.etaxonomy.cdm.api.dto.portal.IndividualsAssociationDto;
import eu.etaxonomy.cdm.api.dto.portal.MediaDto2;
import eu.etaxonomy.cdm.api.dto.portal.MessagesDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonInteractionDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionInfoConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.service.geo.DistributionInfoBuilder;
import eu.etaxonomy.cdm.api.service.geo.DistributionServiceUtilities;
import eu.etaxonomy.cdm.api.service.geo.IGeoServiceAreaMapping;
import eu.etaxonomy.cdm.api.service.l10n.LocaleContext;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.format.description.CategoricalDataFormatter;
import eu.etaxonomy.cdm.format.description.QuantitativeDataFormatter;
import eu.etaxonomy.cdm.format.description.TemporalDataFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TemporalData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

/**
 * TaxonPageDto
 * |-...
 * |-ContainerDto<FeatureDto>
 * | |-count
 * | |-orderRelevant
 * | |-lastUpdated
 * | |-items<FeatureDto>
 * |   |-1
 * |   | |-id
 * |   | |-uuid
 * |   | |-label
 * |   | |-ContainerDto<IFactDto>
 * |   | | |-...
 * |   | | |-1<DistributionInfoDto>
 * |   | | | |-clazz
 * |   | | | |-condensedDistribution
 * |   | | | |-tree<NamedAreaDto,DistributioDto>
 * |   | | | |                       |-area
 * |   | | | |                       |-status
 * |   | | | |                       |-factDtoBase(timeperiod, sources, annotations, marker, id, uuid)
 * |   | | | |-mapUriParams
 * |   | | | |-lastUpdated
 * |   | | |
 * |   | | |-2<CommonNameDto>
 * |   | | | |-clazz
 * |   | | | |-name
 * |   | | | |-transliteration
 * |   | | | |-language
 * |   | | | |-languageUuid
 * |   | | | |-area
 * |   | | | |-areaUuid
 * |   | | | |-factDtoBase...
 * |   | | |
 * |   | | |-3<FactDto>
 * |   | | | |-clazz
 * |   | | | |-typedLabel
 * |   | | | |-factDtoBase...
 * |   | | |
 * |   | | |-4<IndividualsAssociationDto>
 * |   | | | |-description
 * |   | | | |-occurrence
 * |   | | | |-occurrenceUuid
 * |   | | |
 * |   | | |-5<TaxonInteractionDto>
 * |   | | | |-description
 * |   | | | |-taxon
 * |   | | | |-taxonUuid
 * |   | | | |-...
 * |   | |
 * |   | |-ContainerDto<FeatureDto>
 * |   | | |-...
 * |   |
 * |   |-2
 * |     |-...
 * |
 * |-...
 *
 * @author muellera
 * @since 27.02.2024
 */
public class TaxonFactsDtoLoader_FromEntity extends TaxonFactsDtoLoaderBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    public TaxonFactsDtoLoader_FromEntity(ICdmRepository repository, ICdmGenericDao dao,
            IGeoServiceAreaMapping areaMapping) {
        super(repository, dao, areaMapping);
    }

    @Override
    void loadTaxonFacts(Taxon taxon, TaxonPageDto taxonPageDto, TaxonPageDtoConfiguration config) {
        try {
            //compute the features that do exist for this taxon
            Map<UUID, Feature> existingFeatureUuids = getExistingFeatureUuids(taxon, config);

            //filter, sort and structure according to feature tree
            TreeNode<Feature, UUID> filteredRootNode = null;
            if (config.getFeatureTree() != null) {

                @SuppressWarnings({ "unchecked"})
                TermTree<Feature> featureTree = repository.getTermTreeService().find(config.getFeatureTree());
                if (featureTree != null) {
                    filteredRootNode = filterFeatureNode(featureTree.getRoot(), existingFeatureUuids.keySet());
                }
            }
            if (filteredRootNode == null) {
                filteredRootNode = createDefaultFeatureNode(taxon);
            }

            //load facts per feature
            Map<UUID,Set<DescriptionElementBase>> featureMap = loadFeatureMap(taxon, config);

            //load final result
            if (filteredRootNode != null && !filteredRootNode.getChildren().isEmpty()) {
                ContainerDto<FeatureDto> features = new ContainerDto<>();
                for (TreeNode<Feature,UUID> node : filteredRootNode.getChildren()) {
                    handleFeatureNode(config, featureMap, features, node, taxonPageDto);
                }
                taxonPageDto.setTaxonFacts(features);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            taxonPageDto.addMessage(MessagesDto.NewErrorInstance("Error when loading factual data.", e));
        }
    }

    /**
     * Computes the (unsorted) set of features for  which facts exist
     * for the given taxon.
     */
    private <D extends DescriptionBase<?>> Map<UUID, Feature> getExistingFeatureUuids(IDescribable<D> describable, TaxonPageDtoConfiguration config) {

        Map<UUID, Feature> result = new HashMap<>();
        Set<D> descriptions = describable.getDescriptions();
        for (DescriptionBase<?> description : filterFactDataset(descriptions, config)) {
            if (description.isImageGallery()) {
                continue;
            }
            for (DescriptionElementBase deb : description.getElements()) {
                Feature feature = deb.getFeature();
                if (feature != null) {  //null should not happen
                    result.put(feature.getUuid(), feature);
                }
            }
        }
        return result;
    }

    private <D extends DescriptionBase<?>> Set<D> filterFactDataset(Set<D> descriptions, TaxonPageDtoConfiguration config) {
        Set<D> publicFactSets = filterPublished(descriptions);
        return excludeMarked(publicFactSets, config.getExcludedFactDatasetMarkerTypes());
    }

    private <D extends DescriptionBase<?>>  Map<UUID, Set<DescriptionElementBase>> loadFeatureMap(
            IDescribable<D> describable, TaxonPageDtoConfiguration config) {

        Map<UUID, Set<DescriptionElementBase>> featureMap = new HashMap<>();

        //... load facts
        for (D description : filterFactDataset(describable.getDescriptions(), config)) {
            if (description.isImageGallery()) {
                continue;
            }
            for (DescriptionElementBase deb : description.getElements()) {
                Feature feature = deb.getFeature();
                if (featureMap.get(feature.getUuid()) == null) {
                    featureMap.put(feature.getUuid(), new HashSet<>());
                }
                featureMap.get(feature.getUuid()).add(deb);
            }
        }
        return featureMap;
    }

    private void handleFeatureNode(TaxonPageDtoConfiguration config,
            Map<UUID, Set<DescriptionElementBase>> featureMap, ContainerDto<FeatureDto> features,
            TreeNode<Feature, UUID> node, TaxonPageDto pageDto) {

        List<Language> languages = config.getLanguages();
        Feature feature = node.getData();
        FeatureDto featureDto;
        if(!featureMap.containsKey(feature.getUuid())){
            if (node.getChildren().isEmpty()){
                return;
            }else {
                String label = feature.getPreferredLabel(languages);
                featureDto = new FeatureDto(feature.getUuid(), feature.getId(), label);
            }
        }else {
            Set<DescriptionElementBase> facts = featureMap.get(feature.getUuid());
            String featureLabel = facts.size() > 1 ?  feature.getPreferredPluralLabel(languages) : feature.getPreferredLabel(languages);
            featureDto = new FeatureDto(feature.getUuid(), feature.getId(), featureLabel);
            List<Distribution> distributions = new ArrayList<>();

            //
            for (DescriptionElementBase fact : featureMap.get(feature.getUuid())){
                if (fact.isInstanceOf(Distribution.class)) {
                    distributions.add(CdmBase.deproxy(fact, Distribution.class));
                }else {
                    //TODO how to handle CommonNames, do we also want to have a data structure
                    //with Language|
//                             -- Area|
//                                    --name
                    // a bit like for distribution??
                    handleFact(featureDto, fact, pageDto, config);
                }
            }

            handleDistributions_old(config, featureDto, distributions, pageDto);
            //TODO really needed?
            orderFacts(featureDto);
        }
        features.addItem(featureDto);

        //children
        ContainerDto<FeatureDto> childFeatures = new ContainerDto<>();
        for (TreeNode<Feature,UUID> child : node.getChildren()) {
            handleFeatureNode(config, featureMap, childFeatures, child, pageDto);
        }
        if (childFeatures.getCount() > 0) {
            featureDto.setSubFeatures(childFeatures);
        }
    }

    private FactDtoBase handleFact(FeatureDto featureDto, DescriptionElementBase fact,
            TaxonPageDto pageDto, TaxonPageDtoConfiguration config) {

        //TODO locale
        Language localeLang = config.getLanguage();

        FactDtoBase result;
        if (fact.isInstanceOf(TextData.class)) {
            TextData td = CdmBase.deproxy(fact, TextData.class);
            LanguageString ls = td.getPreferredLanguageString(localeLang);
            String text = ls == null ? "" : CdmUtils.Nz(ls.getText());

            FactDto factDto = new FactDto();
            featureDto.addFact(factDto);
            //TODO do we really need type information for textdata here?
            TypedLabel typedLabel = new TypedLabel(text);
            typedLabel.setClassAndId(td);
            factDto.getTypedLabel().add(typedLabel);
            loadBaseData(config, td, factDto);
            //TODO
            result = factDto;
        }else if (fact.isInstanceOf(CommonTaxonName.class)) {
            CommonTaxonName ctn = CdmBase.deproxy(fact, CommonTaxonName.class);
            CommonNameDto dto = new CommonNameDto();
            featureDto.addFact(dto);

            Language lang = ctn.getLanguage();
            if (lang != null) {
                String langLabel = getTermLabel(lang, localeLang);
                dto.setLanguage(langLabel);
                dto.setLanguageUuid(lang.getUuid());
            }else {
                dto.setLanguage(""); //there is an old portal test that expects empty string here
            }
            //area
            NamedArea area = ctn.getArea();
            if (area != null) {
                String areaLabel = getTermLabel(area, localeLang);
                dto.setArea(areaLabel);
                dto.setAreaUUID(area.getUuid());
            }
            dto.setName(ctn.getName());
            dto.setTransliteration(ctn.getTransliteration());
            loadBaseData(config, ctn, dto);
            //TODO sort all common names (not urgent as this is done by portal code)
            result = dto;
        } else if (fact.isInstanceOf(IndividualsAssociation.class)) {
            IndividualsAssociation ia = CdmBase.deproxy(fact, IndividualsAssociation.class);
            IndividualsAssociationDto dto = new IndividualsAssociationDto ();

            LanguageString description = MultilanguageTextHelper.getPreferredLanguageString(ia.getDescription(), Arrays.asList(localeLang));
            if (description != null) {
                dto.setDescription(description.getText());
            }
            SpecimenOrObservationBase<?> specimen = ia.getAssociatedSpecimenOrObservation();
            if (specimen != null) {
                //TODO what to use here??
                dto.setOccurrence(specimen.getTitleCache());
                dto.setOccurrenceUuid(specimen.getUuid());
            }

            featureDto.addFact(dto);
            loadBaseData(config, ia, dto);
            result = dto;
        } else if (fact.isInstanceOf(TaxonInteraction.class)) {
            TaxonInteraction ti = CdmBase.deproxy(fact, TaxonInteraction.class);
            TaxonInteractionDto dto = new TaxonInteractionDto ();

            LanguageString description = MultilanguageTextHelper.getPreferredLanguageString(
                    ti.getDescription(), Arrays.asList(localeLang));
            if (description != null) {
                dto.setDescription(description.getText());
            }
            Taxon taxon = ti.getTaxon2();
            if (taxon != null) {
                //TODO what to use here??
                dto.setTaxon(taxon.cacheStrategy().getTaggedTitle(taxon));
                dto.setTaxonUuid(taxon.getUuid());
            }
            featureDto.addFact(dto);
            loadBaseData(config, ti, dto);
            result = dto;
        }else if (fact.isInstanceOf(CategoricalData.class)) {
            CategoricalData cd = CdmBase.deproxy(fact, CategoricalData.class);
            FactDto factDto = new FactDto();
            featureDto.addFact(factDto);
            //TODO do we really need type information for textdata here?
            String label = CategoricalDataFormatter.NewInstance(null).format(cd, localeLang);
            TypedLabel typedLabel = new TypedLabel(label);
            typedLabel.setClassAndId(cd);
            factDto.getTypedLabel().add(typedLabel);
            //TODO
            loadBaseData(config, cd, factDto);
            result = factDto;
        }else if (fact.isInstanceOf(QuantitativeData.class)) {
            QuantitativeData qd = CdmBase.deproxy(fact, QuantitativeData.class);
            FactDto factDto = new FactDto();
            featureDto.addFact(factDto);
            //TODO do we really need type information for textdata here?
            String label = QuantitativeDataFormatter.NewInstance(null).format(qd, localeLang);
            TypedLabel typedLabel = new TypedLabel(label);
            typedLabel.setClassAndId(qd);
            factDto.getTypedLabel().add(typedLabel);
            //TODO
            loadBaseData(config, qd, factDto);
            result = factDto;
        }else if (fact.isInstanceOf(TemporalData.class)) {
            TemporalData td = CdmBase.deproxy(fact, TemporalData.class);
            FactDto factDto = new FactDto();
            featureDto.addFact(factDto);
            //TODO do we really need type information for textdata here?
            String label = TemporalDataFormatter.NewInstance().format(td, localeLang);
            TypedLabel typedLabel = new TypedLabel(label);
            typedLabel.setClassAndId(td);
            factDto.getTypedLabel().add(typedLabel);
            //TODO
            loadBaseData(config, td, factDto);
            result = factDto;
        }else {
            pageDto.addMessage(MessagesDto.NewWarnInstance("DescriptionElement type not yet handled: " + fact.getClass().getSimpleName()));
            return null;
        }
        result.setTimeperiod(fact.getTimeperiod() == null ? null : fact.getTimeperiod().toString());
        result.setSortIndex(fact.getSortIndex());
        loadMedia(fact, result, pageDto, config);
        return result;
    }

    private void handleDistributions_old(TaxonPageDtoConfiguration config, FeatureDto featureDto,
            List<Distribution> distributions, TaxonPageDto pageDto) {

        if (distributions.isEmpty()) {
            return;
        }

        //configs
        DistributionInfoConfiguration distributionConfig = config.getDistributionInfoConfiguration(featureDto.getUuid());
        CondensedDistributionConfiguration condensedConfig = distributionConfig.getCondensedDistributionConfiguration();

        String statusColorsString = distributionConfig.getStatusColorsString();


        //copied from DescriptionListController

        boolean neverUseFallbackAreaAsParent = true;  //may become a service parameter in future

        //fallbackArea markers include markers for fully hidden areas and fallback areas.
        //The later are hidden markers on areas that have non-hidden subareas (#4408)
        Set<MarkerType> fallbackAreaMarkerTypes = distributionConfig.getFallbackAreaMarkerTypes();
        if(!CdmUtils.isNullSafeEmpty(fallbackAreaMarkerTypes)){
            condensedConfig.fallbackAreaMarkers = fallbackAreaMarkerTypes.stream().map(mt->mt.getUuid()).collect(Collectors.toSet());
        }

        Map<UUID, Color> distributionStatusColors;
        try {
            distributionStatusColors = DistributionServiceUtilities.buildStatusColorMap(
                    statusColorsString);
        } catch (JsonProcessingException e) {
            pageDto.addMessage(MessagesDto.NewErrorInstance("JsonProcessingException when reading distribution status colors", e));
            //TODO is null allowed?
            distributionStatusColors = null;
        }

        UUID areaTreeUuid = distributionConfig.getAreaTree();
        @SuppressWarnings("unchecked")
        TermTree<NamedArea> areaTree = repository.getTermTreeService().find(areaTreeUuid);
        UUID statusTreeUuid = distributionConfig.getStatusTree();
        @SuppressWarnings("unchecked")
        TermTree<PresenceAbsenceTerm> statusTree = repository.getTermTreeService().find(statusTreeUuid);

        DistributionInfoDto dto = new DistributionInfoBuilder(LocaleContext.getLanguages(), repository.getCommonService())
            .build(distributionConfig, distributions, areaTree, statusTree, distributionStatusColors,
                    areaMapping);

        featureDto.addFact(dto);
    }

    /**
     * Recursive call to a feature tree's feature node in order to create a tree structure
     * ordered in the same way as the according feature tree but only containing features
     * that do really exist for the given taxon and ancestors of such features.<BR>
     */
    //TODO 1 does this also work with old load?
    private TreeNode<Feature, UUID> filterFeatureNode(TermNode<Feature> featureNode,
            Set<UUID> existingFeatureUuids) {

        //first filter children
        List<TreeNode<Feature, UUID>> requiredChildNodes = new ArrayList<>();
        for (TermNode<Feature> childNode : featureNode.getChildNodes()) {
            TreeNode<Feature, UUID> child = filterFeatureNode(childNode, existingFeatureUuids);
            if (child != null) {
                requiredChildNodes.add(child);
            }
        }

        //if any child is required or this node is required ....
        if (!requiredChildNodes.isEmpty() ||
                featureNode.getTerm() != null && existingFeatureUuids.contains(featureNode.getTerm().getUuid())) {

            TreeNode<Feature,UUID> result = new TreeNode<>();
            //add this nodes data
            Feature feature = featureNode.getTerm() == null ? null : featureNode.getTerm();
            if (feature != null) {
                result.setNodeId(feature.getUuid());
                result.setData(feature);
            }
            //add child data
            requiredChildNodes.stream().forEachOrdered(c->result.addChild(c));
            return result;
        }else {
            return null;
        }
    }

    private TreeNode<Feature, UUID> createDefaultFeatureNode(IDescribable<?> describable) {
        TreeNode<Feature, UUID> root = new TreeNode<>();
        Set<Feature> requiredFeatures = new HashSet<>();

        for (DescriptionBase<?> description : describable.getDescriptions()) {
            if (description.isImageGallery()) {
                continue;
            }
            for (DescriptionElementBase deb : description.getElements()) {
                Feature feature = deb.getFeature();
                if (feature != null) {  //null should not happen
                    requiredFeatures.add(feature);
                }
            }
        }
        List<Feature> sortedChildren = new ArrayList<>(requiredFeatures);
        Collections.sort(sortedChildren, (f1,f2) -> f1.getTitleCache().compareTo(f2.getTitleCache()));
        sortedChildren.stream().forEachOrdered(f->root.addChild(new TreeNode<>(f.getUuid(), f)));
        return root;
    }



    //TODO merge with loadFacts, it is almost the same, see //DIFFERENT
    @Override
    void loadNameFacts(TaxonName name, TaxonBaseDto nameDto, TaxonPageDtoConfiguration config, TaxonPageDto pageDto) {

        try {
            //compute the features that do exist for this taxon
            Map<UUID, Feature> existingFeatureUuids = getExistingFeatureUuids(name, config);

            //filter, sort and structure according to feature tree
            TreeNode<Feature, UUID> filteredRootNode;
            //DIFFERENT
//            if (config.getFeatureTree() != null) {
//
//                @SuppressWarnings({ "unchecked"})
//                TermTree<Feature> featureTree = repository.getTermTreeService().find(config.getFeatureTree());
//                filteredRootNode = filterFeatureNode(featureTree.getRoot(), existingFeatureUuids.keySet());
//            } else {
                filteredRootNode = createDefaultFeatureNode(name);
//            }  //DIFFERENT END

            //load facts per feature
            Map<UUID,Set<DescriptionElementBase>> featureMap = loadFeatureMap(name, config);

            //load final result
            if (!filteredRootNode.getChildren().isEmpty()) {
                ContainerDto<FeatureDto> features = new ContainerDto<>();
                for (TreeNode<Feature,UUID> node : filteredRootNode.getChildren()) {
                    handleFeatureNode(config, featureMap, features, node, pageDto);
                }
                //DIFFERENT
                nameDto.setNameFacts(features);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            //DIFFERENT
            pageDto.addMessage(MessagesDto.NewErrorInstance("Error when loading factual data.", e));
        }
    }

    private void loadMedia(DescriptionElementBase fact,
            FactDtoBase factDto, TaxonPageDto pageDto, TaxonPageDtoConfiguration config) {
        try {
            List<Media> medias = new ArrayList<>();
            medias.addAll(fact.getMedia());
            ContainerDto<MediaDto2> container = new ContainerDto<>();

            for (Media media : medias) {
                handleSingleMedia(config, container, media);
            }

            if (container.getCount() > 0) {
                factDto.setMedia(container);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            pageDto.addMessage(MessagesDto.NewErrorInstance("Error when loading media data for facts.", e));
        }
    }
}