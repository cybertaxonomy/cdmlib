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
import java.util.Collection;
import java.util.Collections;
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
import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDtoBase;
import eu.etaxonomy.cdm.api.dto.portal.FeatureDto;
import eu.etaxonomy.cdm.api.dto.portal.IFactDto;
import eu.etaxonomy.cdm.api.dto.portal.IndividualsAssociationDto;
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
import eu.etaxonomy.cdm.format.description.CategoricalDataFormatter;
import eu.etaxonomy.cdm.format.description.QuantitativeDataFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
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
public class PortalDtoFactLoader extends PortalDtoLoaderBase {

    private LazyDtoLoader factLazyLoader = new LazyDtoLoader();

    private TaxonPageDto pageDto;

    private IGeoServiceAreaMapping areaMapping;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    public PortalDtoFactLoader(ICdmRepository repository, ICdmGenericDao dao,
            IGeoServiceAreaMapping areaMapping) {
        super(repository, dao);
        this.areaMapping = areaMapping;
    }

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
            SetMap<UUID, FactDtoBase> factsPerFeature = loadFactsPerFeature(taxon, config);
//            Map<UUID,Set<FactTmpDto>> factsPerFeature = loadFeatureMap(taxon, config.isIncludeUnpublished());


            factsPerFeature.values().stream().forEach(s->s.stream().forEach(f->handleSupplementalData(f)));

            factLazyLoader.loadAll(dao, config.getSourceTypes());

            //load final result
            if (!filteredRootNode.getChildren().isEmpty()) {
                ContainerDto<FeatureDto> features = new ContainerDto<>();
                for (TreeNode<Feature,UUID> node : filteredRootNode.getChildren()) {
                    handleFeatureNode(config, factsPerFeature, features, node, taxonPageDto);
                }
                taxonPageDto.setTaxonFacts(features);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            taxonPageDto.addMessage(MessagesDto.NewErrorInstance("Error when loading factual data.", e));
        }
    }


    private void handleSupplementalData(FactDtoBase f) {
        factLazyLoader.add(DescriptionElementBase.class, f);
        return;
    }

    //TODO merge with loadFacts, it is almost the same, see //DIFFERENT
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
            SetMap<UUID,FactDtoBase> featureMap = loadFactsPerFeature(name, config);

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
    private Set<UUID> getExistingFeatureUuids(IDescribable describable, boolean includeUnpublished) {

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
        //by calling PortalDtoLoader.loadBaseData() directly
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

    //should not be necessary anymore as distribution data is now loaded directly in DIstributionServiceImpl
    //by calling PortalDtoLoader.loadBaseData() directly
//    private static void handleDistributionDtoNode(Map<UUID, Distribution> map,
//            TreeNode<Set<DistributionDto>, NamedAreaDto> root) {
//       if (root.getData() != null) {
//           root.getData().stream().forEach(dto->{
//               Distribution distr  = map.get(dto.getUuid());
//               loadBaseData(distr, dto);
//               dto.setTimeperiod(distr.getTimeperiod() == null ? null : distr.getTimeperiod().toString());
//           });
//       }
//
//       //handle children
//       if (root.getChildren() != null) {
//           root.getChildren().stream().forEach(c->handleDistributionDtoNode(map, c));
//       }
//    }

    private FactDtoBase handleFactDto(FeatureDto featureDto, AllFactTypesDto fact, TaxonPageDto pageDto) {
        //TODO locale
        Language localeLang = null;

        FactDtoBase result;
        if (fact.type == TextData.class) {
            TextData td = CdmBase.deproxy(fact, TextData.class);
            LanguageString ls = td.getPreferredLanguageString(localeLang);
            String text = ls == null ? "" : CdmUtils.Nz(ls.getText());

            FactDto factDto = new FactDto();
            featureDto.addFact(factDto);
            //TODO do we really need type information for textdata here?
            TypedLabel typedLabel = new TypedLabel(text);
            typedLabel.setClassAndId(td);
            factDto.getTypedLabel().add(typedLabel);
            loadBaseData(td, factDto);
            //TODO
            result = factDto;
        }else if (fact.type == CommonTaxonName.class) {
            CommonTaxonName ctn = CdmBase.deproxy(fact, CommonTaxonName.class);
            CommonNameDto dto = new CommonNameDto();
            featureDto.addFact(dto);

            Language lang = ctn.getLanguage();
            if (lang != null) {
                String langLabel = getTermLabel(lang, localeLang);
                dto.setLanguage(langLabel);
                dto.setLanguageUuid(lang.getUuid());
            }else {
                //TODO
                dto.setLanguage("-");
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
            loadBaseData(ctn, dto);
            //TODO sort all common names (not urgent as this is done by portal code)
            result = dto;
        } else if (fact.type == IndividualsAssociation.class) {
            IndividualsAssociation ia = CdmBase.deproxy(fact, IndividualsAssociation.class);
            IndividualsAssociationDto dto = new IndividualsAssociationDto ();

            LanguageString description = MultilanguageTextHelper.getPreferredLanguageString(ia.getDescription(), Arrays.asList(localeLang));
            if (description != null) {
                dto.setDescritpion(description.getText());
            }
            SpecimenOrObservationBase<?> specimen = ia.getAssociatedSpecimenOrObservation();
            if (specimen != null) {
                //TODO what to use here??
                dto.setOccurrence(specimen.getTitleCache());
                dto.setOccurrenceUuid(specimen.getUuid());
            }

            featureDto.addFact(dto);
            loadBaseData(ia, dto);
            result = dto;
        } else if (fact.type == TaxonInteraction.class) {
            TaxonInteraction ti = CdmBase.deproxy(fact, TaxonInteraction.class);
            TaxonInteractionDto dto = new TaxonInteractionDto ();

            LanguageString description = MultilanguageTextHelper.getPreferredLanguageString(
                    ti.getDescription(), Arrays.asList(localeLang));
            if (description != null) {
                dto.setDescritpion(description.getText());
            }
            Taxon taxon = ti.getTaxon2();
            if (taxon != null) {
                //TODO what to use here??
                dto.setTaxon(taxon.cacheStrategy().getTaggedTitle(taxon));
                dto.setTaxonUuid(taxon.getUuid());
            }
            featureDto.addFact(dto);
            loadBaseData(ti, dto);
            result = dto;
        }else if (fact.type == CategoricalData.class) {
            CategoricalData cd = CdmBase.deproxy(fact, CategoricalData.class);
            FactDto factDto = new FactDto();
            featureDto.addFact(factDto);
            //TODO do we really need type information for textdata here?
            String label = CategoricalDataFormatter.NewInstance(null).format(cd, localeLang);
            TypedLabel typedLabel = new TypedLabel(label);
            typedLabel.setClassAndId(cd);
            factDto.getTypedLabel().add(typedLabel);
            //TODO
            loadBaseData(cd, factDto);
            result = factDto;
        }else if (fact.type == QuantitativeData.class) {
            QuantitativeData qd = CdmBase.deproxy(fact, QuantitativeData.class);
            FactDto factDto = new FactDto();
            featureDto.addFact(factDto);
            //TODO do we really need type information for textdata here?
            String label = QuantitativeDataFormatter.NewInstance(null).format(qd, localeLang);
            TypedLabel typedLabel = new TypedLabel(label);
            typedLabel.setClassAndId(qd);
            factDto.getTypedLabel().add(typedLabel);
            //TODO
            loadBaseData(qd, factDto);
            result = factDto;
        }else if (fact.type == TemporalData.class) {
            TemporalData td = CdmBase.deproxy(fact, TemporalData.class);
            FactDto factDto = new FactDto();
            featureDto.addFact(factDto);
            //TODO do we really need type information for textdata here?
            String label = td.toString();
            TypedLabel typedLabel = new TypedLabel(label);
            typedLabel.setClassAndId(td);
            factDto.getTypedLabel().add(typedLabel);
            //TODO
            loadBaseData(td, factDto);
            result = factDto;
        }else {
            pageDto.addMessage(MessagesDto.NewWarnInstance("DescriptionElement type not yet handled: " + fact.getClass().getSimpleName()));
            return null;
        }
        result.setTimeperiod(fact.timePeriod == null ? null : fact.timePeriod.toString());
        result.setSortIndex(fact.sortIndex);
        return result;
    }

//    private FactDtoBase handleFact(FeatureDto featureDto, FactDtoBase fact, TaxonPageDto pageDto) {
//        //TODO locale
//        Language localeLang = null;
//
//        FactDtoBase result;
//        if (fact.type == TextData.class) {
//            Map<Language, LanguageString> i18n = fact.multilanguageText;
//            LanguageString ls = MultilanguageTextHelper.getPreferredLanguageString(i18n, LocaleContext.getLanguages());
//            String text = ls == null ? "" : CdmUtils.Nz(ls.getText());
//
//            FactDto factDto = new FactDto();
//            featureDto.addFact(factDto);
//            //TODO do we really need type information for textdata here?
//            TypedLabel typedLabel = new TypedLabel(text);
//            typedLabel.setCdmClass(fact.type);
//            typedLabel.setUuid(fact.uuid);
//            factDto.getTypedLabel().add(typedLabel);
//            //FIXME TODO
//            loadBaseData(fact.toInstance(), factDto);
//            //TODO
//            result = factDto;
//        }else if (fact.type == CommonTaxonName.class) {
//            CommonTaxonName ctn = CdmBase.deproxy(fact, CommonTaxonName.class);
//            CommonNameDto dto = new CommonNameDto();
//            featureDto.addFact(dto);
//
//            Language lang = ctn.getLanguage();
//            if (lang != null) {
//                String langLabel = getTermLabel(lang, localeLang);
//                dto.setLanguage(langLabel);
//                dto.setLanguageUuid(lang.getUuid());
//            }else {
//                //TODO
//                dto.setLanguage("-");
//            }
//            //area
//            NamedArea area = ctn.getArea();
//            if (area != null) {
//                String areaLabel = getTermLabel(area, localeLang);
//                dto.setArea(areaLabel);
//                dto.setAreaUUID(area.getUuid());
//            }
//            dto.setName(ctn.getName());
//            dto.setTransliteration(ctn.getTransliteration());
//            loadBaseData(ctn, dto);
//            //TODO sort all common names (not urgent as this is done by portal code)
//            result = dto;
//        } else if (fact.type == IndividualsAssociation.class) {
//            IndividualsAssociation ia = CdmBase.deproxy(fact, IndividualsAssociation.class);
//            IndividualsAssociationDto dto = new IndividualsAssociationDto ();
//
//            LanguageString description = MultilanguageTextHelper.getPreferredLanguageString(ia.getDescription(), Arrays.asList(localeLang));
//            if (description != null) {
//                dto.setDescritpion(description.getText());
//            }
//            SpecimenOrObservationBase<?> specimen = ia.getAssociatedSpecimenOrObservation();
//            if (specimen != null) {
//                //TODO what to use here??
//                dto.setOccurrence(specimen.getTitleCache());
//                dto.setOccurrenceUuid(specimen.getUuid());
//            }
//
//            featureDto.addFact(dto);
//            loadBaseData(ia, dto);
//            result = dto;
//        } else if (fact.type == TaxonInteraction.class) {
//            TaxonInteraction ti = CdmBase.deproxy(fact, TaxonInteraction.class);
//            TaxonInteractionDto dto = new TaxonInteractionDto ();
//
//            LanguageString description = MultilanguageTextHelper.getPreferredLanguageString(
//                    ti.getDescription(), Arrays.asList(localeLang));
//            if (description != null) {
//                dto.setDescritpion(description.getText());
//            }
//            Taxon taxon = ti.getTaxon2();
//            if (taxon != null) {
//                //TODO what to use here??
//                dto.setTaxon(taxon.cacheStrategy().getTaggedTitle(taxon));
//                dto.setTaxonUuid(taxon.getUuid());
//            }
//            featureDto.addFact(dto);
//            loadBaseData(ti, dto);
//            result = dto;
//        }else if (fact.type == CategoricalData.class) {
//            CategoricalData cd = CdmBase.deproxy(fact, CategoricalData.class);
//            FactDto factDto = new FactDto();
//            featureDto.addFact(factDto);
//            //TODO do we really need type information for textdata here?
//            String label = CategoricalDataFormatter.NewInstance(null).format(cd, localeLang);
//            TypedLabel typedLabel = new TypedLabel(label);
//            typedLabel.setClassAndId(cd);
//            factDto.getTypedLabel().add(typedLabel);
//            //TODO
//            loadBaseData(cd, factDto);
//            result = factDto;
//        }else if (fact.type == QuantitativeData.class) {
//            QuantitativeData qd = CdmBase.deproxy(fact, QuantitativeData.class);
//            FactDto factDto = new FactDto();
//            featureDto.addFact(factDto);
//            //TODO do we really need type information for textdata here?
//            String label = QuantitativeDataFormatter.NewInstance(null).format(qd, localeLang);
//            TypedLabel typedLabel = new TypedLabel(label);
//            typedLabel.setClassAndId(qd);
//            factDto.getTypedLabel().add(typedLabel);
//            //TODO
//            loadBaseData(qd, factDto);
//            result = factDto;
//        }else if (fact.type == TemporalData.class) {
//            TemporalData td = CdmBase.deproxy(fact, TemporalData.class);
//            FactDto factDto = new FactDto();
//            featureDto.addFact(factDto);
//            //TODO do we really need type information for textdata here?
//            String label = td.toString();
//            TypedLabel typedLabel = new TypedLabel(label);
//            typedLabel.setClassAndId(td);
//            factDto.getTypedLabel().add(typedLabel);
//            //TODO
//            loadBaseData(td, factDto);
//            result = factDto;
//        }else {
//            pageDto.addMessage(MessagesDto.NewWarnInstance("DescriptionElement type not yet handled: " + fact.getClass().getSimpleName()));
//            return null;
//        }
//        result.setTimeperiod(fact.getTimeperiod() == null ? null : fact.getTimeperiod().toString());
//        return result;
//    }

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

    public class AllFactTypesDto{

        DescriptionElementBase toInstance() {
            DescriptionElementBase deb;
            if (type == Distribution.class) {
                Distribution td = Distribution.NewInstance(area, status);
                deb = td;
            } else if (type == TextData.class) {
                TextData td = TextData.NewInstance();
                deb = td;
            }else if (type == CommonTaxonName.class) {
                Language lang = null;
                CommonTaxonName ctn = CommonTaxonName.NewInstance(name, lang);
                deb = ctn;
            } else if (type == IndividualsAssociation.class) {
                IndividualsAssociation ia = IndividualsAssociation.NewInstance();
                deb = ia;
            } else if (type == TaxonInteraction.class) {
                TaxonInteraction ti = TaxonInteraction.NewInstance();
                deb = ti;
            }else if (type == CategoricalData.class) {
                CategoricalData cd = CategoricalData.NewInstance();
                deb = cd;
            }else if (type == QuantitativeData.class) {
                QuantitativeData qd = QuantitativeData.NewInstance();
                deb = qd;
            }else if (type == TemporalData.class) {
                TemporalData td = TemporalData.NewInstance();
                deb = td;
            }else {
                //TODO logging
                return null;
            }
            deb.setTimeperiod(timePeriod);
            deb.setSortIndex(sortIndex);
            return deb;
        }

        public FactDtoBase toDto() {

            FactDtoBase dto;
            //TODO label
            if (type == Distribution.class) {
                DistributionDto dd = new DistributionDto(null, this.id, null, null);
                NamedAreaDto areaDto = new NamedAreaDto(null, id, null);
                dd.setArea(factLazyLoader.add(NamedArea.class, areaDto));
                TermDto statusDto = new TermDto(null, id, null);
                dd.setStatus(factLazyLoader.add(PresenceAbsenceTerm.class, statusDto));
                dto = dd;
            } else if (type == TextData.class) {
                FactDto td = new FactDto();
                td.addTypedLabel(new TypedLabel("Test text"));
                //TODO text
                dto = td;
            }else if (type == CommonTaxonName.class) {
                CommonNameDto ctn = new CommonNameDto();
//                NamedAreaDto areaDto = new NamedAreaDto(null, id, null);
//                ctn.setArea(factLazyLoader.add(NamedArea.class, areaDto));
                ctn.setArea("Testarea");  //TODO  //String
                ctn.setLanguage("Testlanguage");  //String //TODO
                ctn.setName(name);
                ctn.setTransliteration(transliteration);
                dto = ctn;
            } else if (type == IndividualsAssociation.class) {
                IndividualsAssociationDto ia = new IndividualsAssociationDto();
                ia.setOccurrence(null);  //TODO
                ia.setDescritpion(null); //TODO xxx
                dto = ia;
            } else if (type == TaxonInteraction.class) {
                TaxonInteractionDto ti = new TaxonInteractionDto();
                ti.setTaxon(null);  //TODO
                ti.setDescritpion(null);  //TODO xxx
                dto = ti;
            }else if (type == CategoricalData.class) {
                FactDto fd = new FactDto();
                //TODO text
                dto = fd;
            }else if (type == QuantitativeData.class) {
                FactDto fd = new FactDto();
                //TODO text
                dto = fd;
            }else if (type == TemporalData.class) {
                FactDto fd = new FactDto();
                //TODO text
                dto = fd;
            }else {
                pageDto.addMessage(MessagesDto.NewWarnInstance("DescriptionElement type not yet handled: " + this.type.getSimpleName()));
                return null;
            }
            dto.setTimeperiod(this.getTimeperiod() == null ? null : this.getTimeperiod().toString());
            dto.setId(this.id);
            dto.setSortIndex(this.sortIndex);
            return dto;
        }

        public Object getTimeperiod() {
            return deb != null? deb.getTimeperiod(): timePeriod;
        }

        DescriptionElementBase deb;
        UUID featureUuid;
        Class type;
        UUID uuid;
        int id;
        NamedArea area;
        String name;
        TimePeriod timePeriod;
        Integer sortIndex;
        PresenceAbsenceTerm status;
        String transliteration;

        public AllFactTypesDto(Map<String,Object> map) {
            this.featureUuid = (UUID)map.get("featureUuid");
            this.type = (Class) map.get("type");
            this.uuid = (UUID)map.get("uuid");
            this.id = (int)map.get("id");
            this.area = (NamedArea) map.get("area");
            this.name = (String) map.get("name");
            this.timePeriod = (TimePeriod) map.get("timePeriod");
            this.sortIndex = (Integer)map.get("sortIndex");
            this.status = (PresenceAbsenceTerm) map.get("status");
            this.transliteration = (String)map.get("transliteration");
//            this.multilanguageText = (Map<Language, LanguageString>)map.get("i18nText");
        }
        public AllFactTypesDto(DescriptionElementBase deb) {
            this.deb = deb;
            this.type = CdmBase.deproxy(deb).getClass();
        }

    }

    private SetMap<UUID, FactDtoBase> loadFactsPerFeature(IDescribable<?> describable,
            TaxonPageDtoConfiguration config) {

        boolean withImageGallery = false;
//        List<Map<String, Object>> dto = this.repository.getDescriptionElementService().getFactDto(
//                Taxon.class, describable.getUuid(), includeUnpublished, withImageGallery);

        Class<? extends IDescribable> clazz = Taxon.class;
        UUID entityUuid = describable.getUuid();

        String describedAttr = clazz.equals(Taxon.class) ? "taxon" : clazz.equals(TaxonName.class) ? "name" : "describedSpecimenOrObservation";
        String hql = "SELECT new map(deb.feature.uuid as featureUuid, type(deb) as type "
                   +    " ,deb.uuid as uuid, deb.id as id "
                   +    " ,deb.area as area, deb.name as name "
                   +    " ,deb.timeperiod as timePeriod, deb.sortIndex as sortIndex"
                   +    " ,deb.status as status "
                   +    " ,deb.transliteration as transliteration "
//                   +    " ,deb.multilanguageText as i18nText"
                   +    ")"
                   + " FROM DescriptionElementBase deb "
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
                FactDtoBase dto = fact.toDto();
                if (dto != null) {
                    result.putItem(featureUuid, dto);
                }
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
//                TODO logging
            return new SetMap<>();
        }


    }

    private SetMap<UUID,AllFactTypesDto> loadFeature2FactMap(IDescribable<?> describable, boolean includeUnpublished) {

        SetMap<UUID,AllFactTypesDto> featureMap = new SetMap<>();

        //... load facts
        for (DescriptionBase<?> description : filterPublished(describable.getDescriptions())) {
            if (description.isImageGallery()) {
                continue;
            }
            for (DescriptionElementBase deb : description.getElements()) {
                Feature feature = deb.getFeature();
                if (featureMap.get(feature.getUuid()) == null) {
                    featureMap.put(feature.getUuid(), new HashSet<>());
                }
                featureMap.get(feature.getUuid()).add(new AllFactTypesDto(deb));
            }
        }
        return featureMap;
    }

    private void handleFeatureNode(TaxonPageDtoConfiguration config,
            SetMap<UUID,FactDtoBase> featureMap, ContainerDto<FeatureDto> features,
            TreeNode<Feature, UUID> node, TaxonPageDto pageDto) {

        Feature feature = node.getData();
        UUID featureUuid = node.getNodeId();
        Set<FactDtoBase> facts = featureMap.get(featureUuid);
        FeatureDto featureDto;
        if(facts == null){
            if (node.getChildren().isEmpty()) {
                return;
            }else {
                //TODO locale und früher
                featureDto = new FeatureDto(feature.getUuid(), feature.getId(), feature.getLabel());
            }
        }else {
            //TODO locale und früher
            featureDto = new FeatureDto(feature.getUuid(), feature.getId(), feature.getLabel());

            List<DistributionDto> distributions = new ArrayList<>();

            //
            for (FactDtoBase fact : facts){
                if (fact instanceof DistributionDto) {
                    distributions.add((DistributionDto)fact);
                }else {
                    //TODO how to handle CommonNames, do we also want to have a data structure
                    //with Language|
//                             -- Area|
//                                    --name
                    // a bit like for distribution??

                    //normal facts (usually 1 per feature or at least not hierarchically ordered)
                    featureDto.addFact(fact);
                }

            }

            handleDistributions(config, featureDto, distributions, pageDto);
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

    //TODO not really used yet, only for distinguishing fact classes,
    //needs discussion if needed and how to implement.
    //we could also move compareTo methods to DTO classes but with this
    //remove from having only data in the DTO, no logic
    private void orderFacts(FeatureDto featureDto) {
        List<IFactDto> list = featureDto.getFacts().getItems();
        Collections.sort(list, (f1,f2)->{
            if (!f1.getClass().equals(f2.getClass())) {
                return f1.getClass().getSimpleName().compareTo(f2.getClass().getSimpleName());
            }else {
                if (f1 instanceof FactDto) {
                   FactDto fact1 = (FactDto)f1;
                   FactDto fact2 = (FactDto)f2;

//                   return fact1.getTypedLabel().toString().compareTo(fact2.getTypedLabel().toString());
                   return 0; //FIXME;
                } else if (f1 instanceof CommonNameDto) {
                    int result = 0;
                    CommonNameDto fact1 = (CommonNameDto)f1;
                    CommonNameDto fact2 = (CommonNameDto)f2;
                    return 0;  //FIXME
                }
            }
            return 0;  //FIXME
        });
    }
}