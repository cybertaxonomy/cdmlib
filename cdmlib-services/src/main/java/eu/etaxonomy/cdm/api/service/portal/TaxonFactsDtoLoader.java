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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.dto.portal.CommonNameDto;
import eu.etaxonomy.cdm.api.dto.portal.ContainerDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDtoBase;
import eu.etaxonomy.cdm.api.dto.portal.FeatureDto;
import eu.etaxonomy.cdm.api.dto.portal.IndividualsAssociationDto;
import eu.etaxonomy.cdm.api.dto.portal.MediaDto2;
import eu.etaxonomy.cdm.api.dto.portal.MessagesDto;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonInteractionDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionInfoConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermDto;
import eu.etaxonomy.cdm.api.service.geo.DistributionInfoBuilder;
import eu.etaxonomy.cdm.api.service.geo.DistributionServiceUtilities;
import eu.etaxonomy.cdm.api.service.geo.IGeoServiceAreaMapping;
import eu.etaxonomy.cdm.api.service.l10n.LocaleContext;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
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
public class TaxonFactsDtoLoader extends TaxonFactsDtoLoaderBase {

    private ProxyDtoLoader factProxyLoader = new ProxyDtoLoader(this);

    private TaxonPageDto pageDto;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    public TaxonFactsDtoLoader(ICdmRepository repository, ICdmGenericDao dao,
            IGeoServiceAreaMapping areaMapping) {
        super(repository, dao, areaMapping);
    }

    @Override
    void loadTaxonFacts(Taxon taxon, TaxonPageDto taxonPageDto, TaxonPageDtoConfiguration config) {

        this.pageDto = taxonPageDto;
        try {
            //compute the features that do exist for this taxon
            //TODO should be uuidAndTitleCache e.g. for sorting in createDefaultFeatureNode
            Set<UUID> existingFeatureUuids = getExistingFeatureUuids(taxon,config.isIncludeUnpublished());

            //featureTree (filter, sort and structure according to feature tree)
            TreeNode<Feature, UUID> filteredRootNode = null;
            if (config.getFeatureTree() != null) {

                //TODO featureTree as DTO
                @SuppressWarnings({ "unchecked"})
                TermTree<Feature> featureTree = repository.getTermTreeService().find(config.getFeatureTree());
                if (featureTree != null) {
                    filteredRootNode = filterFeatureNode(featureTree.getRoot(), existingFeatureUuids);
                }
            }
            if (filteredRootNode == null) {
                //TODO dto
                filteredRootNode = createDefaultFeatureNode(taxon);
            }

            //load facts per feature
            SetMap<UUID, FactDtoBase> factsPerFeature = loadFactsPerFeature(taxon, config, taxonPageDto);

            //handle supplemental data
            factsPerFeature.values().stream().forEach(s->s.stream().forEach(f->handleSupplementalData(f)));

            //annotations + marker
            //TODO make configurable
            factProxyLoader.loadAll(dao, config.getSourceTypes(), config);

            //load final result
            if (!filteredRootNode.getChildren().isEmpty()) {
                ContainerDto<FeatureDto> features = new ContainerDto<>();
                for (TreeNode<Feature,UUID> node : filteredRootNode.getChildren()) {
                    handleFeatureNode(config, factsPerFeature, features, node, taxonPageDto);
                }
                taxonPageDto.setTaxonFacts(features);
            }
        } catch (Exception e) {
            e.printStackTrace();
            taxonPageDto.addMessage(MessagesDto.NewErrorInstance("Error when loading factual data.", e));
        }
    }


    private void handleSupplementalData(FactDtoBase f) {
        factProxyLoader.add(DescriptionElementBase.class, f);
    }

    //TODO merge with loadFacts, it is almost the same, see //DIFFERENT
    @Override
    void loadNameFacts(TaxonName name, TaxonBaseDto nameDto, TaxonPageDtoConfiguration config, TaxonPageDto pageDto) {

        try {
            //compute the features that do exist for this taxon
            Collection<UUID> existingFeatureUuids = getExistingFeatureUuids(name, config.isIncludeUnpublished());

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
            SetMap<UUID,FactDtoBase> featureMap = loadFactsPerFeature(name, config, pageDto);

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


    /**
     * Computes the (unsorted) set of features for  which facts exist
     * for the given taxon.
     * @param includeUnpublished
     */
    private Set<UUID> getExistingFeatureUuids(@SuppressWarnings("rawtypes") IDescribable describable, boolean includeUnpublished) {

        @SuppressWarnings("rawtypes")
        Class<? extends IDescribable> clazz = describable.getClass();
        UUID entityUuid = describable.getUuid();

        String describedAttr = clazz.equals(Taxon.class) ? "taxon" : clazz.equals(TaxonName.class) ? "name" : "describedSpecimenOrObservation";

        String hql = "SELECT DISTINCT deb.feature.uuid "
                + " FROM DescriptionElementBase deb "
                + " WHERE deb.inDescription." + describedAttr +".uuid = '"+entityUuid  +"'"
                + " AND deb.inDescription.imageGallery = false ";
        if (!includeUnpublished) {
            hql += " AND deb.inDescription.publish = true ";
        }
        List<UUID> result = dao.getHqlResult(hql, UUID.class);

//        boolean includeImageGallery = false;
        return new HashSet<>(result);

//
//        Map<UUID, Feature> result = new HashMap<>();
//        for (DescriptionBase<?> description : filterPublished(describable.getDescriptions())) {
//            if (description.isImageGallery()) {
//                continue;
//            }
//            for (DescriptionElementBase deb : description.getElements()) {
//                Feature feature = deb.getFeature();
//                if (feature != null) {  //null should not happen
//                    result.put(feature.getUuid(), feature);
//                }
//            }
//        }
//        return result.keySet();
    }

    private void handleDistributions(TaxonPageDtoConfiguration config, FeatureDto featureDto,
            List<DistributionDto> distributions, TaxonPageDto pageDto) {

        if (distributions.isEmpty()) {
            return;
        }

        //configs
        DistributionInfoConfiguration distributionConfig = config.getDistributionInfoConfiguration(featureDto.getUuid());
        CondensedDistributionConfiguration condensedConfig = distributionConfig.getCondensedDistributionConfiguration();

        String statusColorsString = distributionConfig.getStatusColorsString();

        //fallbackArea markers include markers for fully hidden areas and fallback areas.
        //The later are hidden markers on areas that have non-hidden subareas (#4408)
        Set<MarkerType> fallbackAreaMarkerTypes = distributionConfig.getFallbackAreaMarkerTypes();
        if(!CdmUtils.isNullSafeEmpty(fallbackAreaMarkerTypes)){
            condensedConfig.fallbackAreaMarkers = fallbackAreaMarkerTypes.stream().map(mt->mt.getUuid()).collect(Collectors.toSet());
        }

        Map<UUID,Color> distributionStatusColors;
        try {
            distributionStatusColors = DistributionServiceUtilities.buildStatusColorMap(
                    statusColorsString, repository.getTermService(), repository.getVocabularyService());
        } catch (JsonProcessingException e) {
            pageDto.addMessage(MessagesDto.NewErrorInstance("JsonProcessingException when reading distribution status colors", e));
            //TODO is null allowed?
            distributionStatusColors = null;
        }

        DistributionInfoDto dto = new DistributionInfoBuilder(LocaleContext.getLanguages(), repository.getCommonService())
                .buildFromDto(distributionConfig,
                    distributions,
                    null, null,   //TODO areaTree and statusTree ??
                    distributionStatusColors, areaMapping);

        //should not be necessary anymore as distribution data is now loaded directly in DistributionServiceImpl
        //by calling TaxonPageDtoLoader.loadBaseData() directly
//        if (distributionConfig.isUseTreeDto() && dto.getTree() != null) {
//            DistributionTreeDto tree = (DistributionTreeDto)dto.getTree();
//            TreeNode<Set<DistributionDto>, NamedAreaDto> root = tree.getRootElement();
//            //fill uuid->distribution map
//            Map<UUID,Distribution> distributionMap = new HashMap<>();
//            distributions.stream().forEach(d->distributionMap.put(d.getUuid(), d));
//            handleDistributionDtoNode(distributionMap, root);
//        }

        featureDto.addFact(dto);
    }

    /**
     * Recursive call to a feature tree's feature node in order to creates a tree structure
     * ordered in the same way as the according feature tree but only containing features
     * that do really exist for the given taxon. If only a child node is required the parent
     * node/feature is also considered to be required.<BR>
     */
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

    private FactDtoBase allFactTypesDto2factDtoBase(AllFactTypesDto aftd) {

        //TODO i18n
        List<Language> languages = new ArrayList<>();

        FactDtoBase dto;
        //TODO label
        if (aftd.type == Distribution.class) {
            NamedAreaDto areaDto = TermDtoLoader.INSTANCE().fromEntity(aftd.area);
            //factProxyLoader.add(NamedArea.class, areaDto)
            TermDto statusDto = TermDtoLoader.INSTANCE().fromEntity(aftd.status);
            //OLD: factProxyLoader.add(PresenceAbsenceTerm.class, statusDto)
            DistributionDto distDto = new DistributionDto(aftd.uuid, aftd.id, areaDto, statusDto);
            dto = distDto;
        } else if (aftd.type == TextData.class) {
            FactDto td = new FactDto();
            if (StringUtils.isNotBlank(aftd.text)) {
                td.addTypedLabel(new TypedLabel(aftd.text));
            }
            dto = td;
        }else if (aftd.type == CommonTaxonName.class) {
            CommonNameDto ctn = new CommonNameDto();
            ctn.setArea(aftd.area.getPreferredLabel(languages));
            ctn.setLanguage(aftd.language.getPreferredLabel(languages));
            ctn.setName(aftd.name);
            ctn.setTransliteration(aftd.transliteration);
            dto = ctn;
        } else if (aftd.type == IndividualsAssociation.class) {
            IndividualsAssociationDto ia = new IndividualsAssociationDto();
            if (aftd.associatedOccurrence != null) {
                ia.setOccurrence(aftd.associatedOccurrence.getTitleCache());
                ia.setOccurrenceUuid(aftd.associatedOccurrence.getUuid());
            }
            ia.setDescritpion(aftd.description);
            dto = ia;
        } else if (aftd.type == TaxonInteraction.class) {
            TaxonInteractionDto ti = new TaxonInteractionDto();
            if (aftd.interactingTaxon != null) {
                ti.setTaxon(aftd.interactingTaxon.getTaggedTitle());
                ti.setTaxonUuid(aftd.interactingTaxon.getUuid());
            }
            ti.setDescritpion(aftd.description);
            dto = ti;
        }else if (aftd.type == CategoricalData.class) {
            FactDto fd = new FactDto();
            //TODO text
            dto = fd;
        }else if (aftd.type == QuantitativeData.class) {
            FactDto fd = new FactDto();
            //TODO text
            dto = fd;
        }else if (aftd.type == TemporalData.class) {
            FactDto fd = new FactDto();
            if (aftd.period != null) {
                fd.addTypedLabel(new TypedLabel(aftd.period.toString()));
            }
            dto = fd;
        }else {
            pageDto.addMessage(MessagesDto.NewWarnInstance("DescriptionElement type not yet handled: " + aftd.type.getSimpleName()));
            return null;
        }

        dto.setTimeperiod(aftd.timePeriod == null ? null : aftd.timePeriod.toString());
        dto.setId(aftd.id);
        dto.setSortIndex(aftd.sortIndex);
        return dto;
    }

    private class AllFactTypesDto{

        UUID featureUuid;
        Class type;
        UUID uuid;
        int id;
        NamedArea area;
        Language language;
        String name;
        TimePeriod timePeriod;
        Integer sortIndex;
        PresenceAbsenceTerm status;
        String transliteration;
        String text;
        String description;
        ExtendedTimePeriod period;
        SpecimenOrObservationBase<?> associatedOccurrence;  //for now we load the cdm instance as it does not appear often in facts (instead it it loaded separately in specimen part)
        Taxon interactingTaxon; //for now we load the cdm instance as it does not appear often

        public AllFactTypesDto(Map<String,Object> map) {
            this.featureUuid = (UUID)map.get("featureUuid");
            this.type = (Class)map.get("type");
            this.uuid = (UUID)map.get("uuid");
            this.id = (int)map.get("id");
            this.area = (NamedArea) map.get("area");
            this.status = (PresenceAbsenceTerm) map.get("status");
            this.language = (Language)map.get("language");
            this.associatedOccurrence = (SpecimenOrObservationBase<?>)map.get("associatedOccurrence");
            this.interactingTaxon = (Taxon)map.get("interactingTaxon");
            this.name = (String) map.get("name");
            this.timePeriod = (TimePeriod) map.get("timePeriod");
            this.period = (ExtendedTimePeriod)map.get("extendedPeriod");
            this.sortIndex = (Integer)map.get("sortIndex");
            this.transliteration = (String)map.get("transliteration");
            LanguageString textDataTextI18n = (LanguageString)map.get("textDataText");
            //TODO why is this already a language string and not a multi-language string
            //FIXME probably we need to deduplicate if >1 language representation exists!!
            this.text = textDataTextI18n == null ? null : textDataTextI18n.getText();
            LanguageString descriptionI18n = (LanguageString)map.get("description");
           //FIXME probably we need to deduplicate if >1 language representation exists!!
            this.description = descriptionI18n == null ? null : descriptionI18n.getText();
        }
    }

    private SetMap<UUID, FactDtoBase> loadFactsPerFeature(IDescribable<?> describable,
            TaxonPageDtoConfiguration config, TaxonPageDto taxonPageDto) {

        @SuppressWarnings("rawtypes")
        Class<? extends IDescribable> clazz = Taxon.class;
        UUID entityUuid = describable.getUuid();

        String describedAttr = clazz.equals(Taxon.class) ? "taxon" : clazz.equals(TaxonName.class) ? "name" : "describedSpecimenOrObservation";
        String hql = "SELECT new map(deb.feature.uuid as featureUuid, type(deb) as type "
                   +    " ,deb.uuid as uuid, deb.id as id "
                   +    " ,a as area, st as status, l as language"
                   +    " ,deb.name as name "
                   +    " ,deb.timeperiod as timePeriod, deb.period as extendedPeriod "
                   +    " ,deb.sortIndex as sortIndex"
                   +    " ,deb.transliteration as transliteration "
                   +    " ,mlt as textDataText "
                   +    " ,occ as associatedOccurrence "
                   +    " ,taxon2 as interactingTaxon "
//                   +    " ,descr as description2 "
                   +    ")"
                   + " FROM DescriptionElementBase deb "
                   + "     LEFT OUTER JOIN deb.status st "
                   + "     LEFT OUTER JOIN deb.area a "
                   + "     LEFT OUTER JOIN deb.language l "
                   + "     LEFT OUTER JOIN deb.multilanguageText mlt "
                   + "     LEFT OUTER JOIN deb.associatedSpecimenOrObservation occ "
                   + "     LEFT OUTER JOIN deb.taxon2 taxon2 "
                   //FIXME for some reason this throws an exception as DescriptionElementBase.description is not recognized
//                   + "     LEFT OUTER JOIN deb.description descr "
                   + " WHERE deb.inDescription." + describedAttr +".uuid = '" + entityUuid +"'"
                   + " AND deb.inDescription.imageGallery = false ";
        if (!config.isIncludeUnpublished()) {
            hql += " AND deb.inDescription.publish = true ";
        }

        try {
            SetMap<UUID, FactDtoBase> result = new SetMap<>();

            List<Map<String,Object>> factMaps = dao.getHqlMapResult(hql, Object.class);

            for (Map<String,Object> factMap : factMaps) {
                AllFactTypesDto fact = new AllFactTypesDto(factMap);
                UUID featureUuid = fact.featureUuid;
                FactDtoBase dto = allFactTypesDto2factDtoBase(fact);
                if (dto != null) {
                    result.putItem(featureUuid, dto);
                }
            }

            return result;

        } catch (Exception e) {
//            e.printStackTrace();
            taxonPageDto.addMessage(MessagesDto.NewErrorInstance("Error while loading facts per feature", e));
            return new SetMap<>();
        }
    }

    private void handleFeatureNode(TaxonPageDtoConfiguration config,
            SetMap<UUID,FactDtoBase> featureMap, ContainerDto<FeatureDto> features,
            TreeNode<Feature, UUID> node, TaxonPageDto pageDto) {

        Feature feature = node.getData();
        UUID featureUuid = node.getNodeId();
        Set<FactDtoBase> facts = featureMap.get(featureUuid);
        FeatureDto featureDto;
        if(facts == null){   //can this happen?
            if (node.getChildren().isEmpty()) {
                return;
            }else {
                //TODO locale und früher
                featureDto = new FeatureDto(feature.getUuid(), feature.getId(), feature.getLabel());
            }
        }else {
            //TODO locale und früher
            String featureLabel = facts.size() > 1 ?  feature.getPluralLabel() : feature.getLabel();
            featureDto = new FeatureDto(feature.getUuid(), feature.getId(), featureLabel);

            List<DistributionDto> distributions = new ArrayList<>();

            //
            for (FactDtoBase fact : facts){
                if (fact instanceof DistributionDto) {
                    distributions.add((DistributionDto)fact);
                }else {
                    featureDto.addFact(fact);
                }
            }

            handleDistributions(config, featureDto, distributions, pageDto);
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

    void loadProxyFacts(Set<FactDtoBase> factDtos){

        @SuppressWarnings("rawtypes")
        Class baseClass = DescriptionElementBase.class;

        Set<Integer> baseIds = factDtos.stream().map(d->d.getId()).collect(Collectors.toSet());

        SetMap<Integer,FactDtoBase> id2factInstancesMap = new SetMap<>(); //it is a set because there might be multiple instances for the same object
        factDtos.stream().forEach(dto->id2factInstancesMap.putItem(dto.getId(), dto));

        String hql = "SELECT new map(bc.id as baseId, m.id as mediaId) "
                + " FROM "+baseClass.getSimpleName()+" bc JOIN bc.media m "
                + " WHERE bc.id IN :baseIds";

        Map<String,Object> params = new HashMap<>();
        params.put("baseIds", baseIds);

        try {
            List<Map<String, Integer>> fact2mediaMapping = dao.getHqlMapResult(hql, params, Integer.class);

            fact2mediaMapping.stream().forEach(e->{
                Integer mediaId = e.get("mediaId");
                MediaDto2 mediaDto2 = new MediaDto2();
                mediaDto2.setId(mediaId);
                Integer baseId = e.get("baseId");
                id2factInstancesMap.get(baseId).stream().forEach(sdd->sdd.addMedia(mediaDto2));
                factProxyLoader.add(Media.class, mediaDto2);
            });
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("Exception while loading sources for sourced entities", e);
        }
    }
}