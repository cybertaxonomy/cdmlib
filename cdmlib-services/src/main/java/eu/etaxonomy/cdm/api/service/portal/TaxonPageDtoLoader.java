/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.api.dto.portal.ContainerDto;
import eu.etaxonomy.cdm.api.dto.portal.MediaDto2;
import eu.etaxonomy.cdm.api.dto.portal.MessagesDto;
import eu.etaxonomy.cdm.api.dto.portal.OccurrenceInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto.TaxonNameDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.ConceptRelationDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.HomotypicGroupDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.KeyDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.NameRelationDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.NomenclaturalStatusDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.SpecimenDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.TaxonNodeAgentsRelDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.TaxonNodeDTO;
import eu.etaxonomy.cdm.api.dto.portal.config.ISourceableLoaderConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.filter.TaxonOccurrenceRelationType;
import eu.etaxonomy.cdm.api.service.dto.DtoUtil;
import eu.etaxonomy.cdm.api.service.geo.IGeoServiceAreaMapping;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainer;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainerFormatter;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.compare.taxon.HomotypicGroupTaxonComparator;
import eu.etaxonomy.cdm.format.taxon.TaxonRelationshipFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextFormatter;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.taxon.TaxonBaseDefaultCacheStrategy;

/**
 * Loads the portal dto from a taxon instance.
 * Maybe later also supports loading from persistence.
 *
 * @author a.mueller
 * @date 09.01.2023
 */
public class TaxonPageDtoLoader extends TaxonPageDtoLoaderBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private TaxonFactsDtoLoaderBase factLoader;

    //
    private TaxonFactsDtoLoaderBase nameFactLoader;

    public TaxonPageDtoLoader(ICdmRepository repository, ICdmGenericDao dao, IGeoServiceAreaMapping areaMapping,
            TaxonPageDtoConfiguration config) {

        super(repository, dao);
        if (!config.isUseDtoLoading() || /* FIXME #10622 */ !config.getExcludedFactDatasetMarkerTypes().isEmpty()) {
            this.factLoader = new TaxonFactsDtoLoader_FromEntity(repository, dao, areaMapping);
            this.nameFactLoader = this.factLoader;
        }else {
            this.factLoader = new TaxonFactsDtoLoader(repository, dao, areaMapping);
            //TODO implement DTO loading for name facts
            this.nameFactLoader = new TaxonFactsDtoLoader_FromEntity(repository, dao, areaMapping);
        }
    }

    //TODO can we handle the area mapping better?
    public TaxonPageDto load(Taxon taxon, TaxonPageDtoConfiguration config) {
        TaxonPageDto result = new TaxonPageDto();

        loadAcceptedTaxon(taxon, config, result);

        loadTaxonNodes(taxon, result, config);
        loadSynonyms(taxon, result, config);
        loadConceptRelations(taxon, result, config);
        loadFacts(taxon, result, config);
        loadMedia(taxon, result, config);
        loadSpecimens(taxon, result, config);
        loadKeys(taxon, result, config);

        return result;
    }

    private void loadAcceptedTaxon(Taxon taxon, TaxonPageDtoConfiguration config, TaxonPageDto result) {
        try {
            TaxonName name = taxon.getName();

            //load 1:1
            //TODO supplementalData for name
            loadBaseData(config, taxon, result);
            result.setLastUpdated(getLastUpdated(null, taxon));
            result.setLabel(CdmUtils.Nz(taxon.getTitleCache()));
//          result.setTypedTaxonLabel(getTypedTaxonLabel(taxon, config));
            result.setTaggedLabel(getTaggedTaxon(taxon, config));
            if (name != null) {
                handleName(config, result, name, result);
            }
            if (taxon.getSec() != null) {
                result.setSecTitleCache(taxon.getSec().getTitleCache());
            }
        } catch (Exception e) {
            //e.printStackTrace();
            result.addMessage(MessagesDto.NewErrorInstance("Error when loading accepted name data.", e));
        }
    }

    private void handleName(TaxonPageDtoConfiguration config, TaxonBaseDto taxonDto, TaxonName name, TaxonPageDto pageDto) {
        TaxonNameDto nameDto = taxonDto.new TaxonNameDto();
        taxonDto.setName(nameDto);

        //we load base data into the taxonDto to avoid sending the nameDto over the wire
        //sources and annotations are handled in the same DTO
//        loadBaseData(config, name, nameDto);
        loadAnnotatable(config, name, taxonDto);
        loadSources(config, name, taxonDto);
        loadIdentifiable(config, name, taxonDto);
        taxonDto.setNameUuid(name.getUuid());

        //formatting
        INameCacheStrategy formatter = name.cacheStrategy();
        formatter.setEtAlPosition(config.getEtAlPosition());

        taxonDto.setNameLabel(formatter.getTitleCache(name));
        handleRelatedNames(name, taxonDto, config);
        handleNomenclaturalStatus(name, taxonDto, config);
        loadProtologues(name, taxonDto);
        taxonDto.setNameUuid(name.getUuid());
        taxonDto.setNameType(name.getNameType().toString());
        loadNameFacts(name, taxonDto, config, pageDto);
        nameDto.setTaggedName(formatter.getTaggedFullTitle(name));
        nameDto.setInvalid(name.isInvalid());
        nameDto.setHasRegistration(getPublicRegistrations(name));
    }

    private boolean getPublicRegistrations(TaxonName name) {
        return name.getRegistrations().stream()
                .filter(r->r.isPublished()).findAny().isPresent();
    }

    private List<TaggedText> getTaggedTaxon(TaxonBase<?> taxon, TaxonPageDtoConfiguration config) {
        TaxonBaseDefaultCacheStrategy<TaxonBase<?>> formatter = new TaxonBaseDefaultCacheStrategy<>();
        List<TaggedText> tags = formatter.getTaggedTitle(taxon);
        return tags;
    }

    private void loadKeys(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
        if (!config.isWithKeys()){
            return;
        }
        try {
            ContainerDto<KeyDTO> container = new ContainerDto<>();

            //TODO other key types, but type must not be null, otherwise NPE
            Pager<PolytomousKey> keys = repository.getIdentificationKeyService().findKeysConvering(taxon, PolytomousKey.class, null, null, null);
            for (PolytomousKey key : keys.getRecords()) {
                KeyDTO dto = new KeyDTO();
                loadBaseData(config, key, dto);
                dto.setLabel(key.getTitleCache());
                dto.setKeyClass(key.getClass().getSimpleName());
                container.addItem(dto);
            }
            if (container.getCount() > 0) {
                result.setKeys(container);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            result.addMessage(MessagesDto.NewErrorInstance("Error when loading identification key data.", e));
        }
    }

    private void loadSpecimens(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
        if (!config.isWithSpecimens()){
            return;
        }
        loadRootSpecimens(taxon, result, config);

        //once fully switching to newSpecimens the tansient annotation on the getter should be removed
        //in the TaxonPageDto
        boolean newSpecimensImplemented = false;
        if (newSpecimensImplemented) {
            loadNewRootSpecimens(taxon, result, config);
        }
    }

    /**
     * Not really implemented yet
     */
    private void loadNewRootSpecimens(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
        //TODO load specimen from multiple places

        //TODO use filter
        EnumSet<TaxonOccurrenceRelationType> specimenFilter = config.getSpecimenAssociationFilter();

        //TODO maybe use OccurrenceService.listRootUnitDTOsByAssociatedTaxon
        //     or OccurrenceService.listRootUnitsByAssociatedTaxon()

        try {
            ContainerDto<SpecimenDTO> container = new ContainerDto<>();

            List<SpecimenOrObservationBase<?>> specimens = new ArrayList<>();
            for (TaxonDescription taxonDescription : taxon.getDescriptions()) {
                if (taxonDescription.isImageGallery()) {
                    continue;
                }
                for (DescriptionElementBase el : taxonDescription.getElements()) {
                    if (el.isInstanceOf(IndividualsAssociation.class)) {
                        IndividualsAssociation indAss = CdmBase.deproxy(el, IndividualsAssociation.class);
                        SpecimenOrObservationBase<?> specimen = indAss.getAssociatedSpecimenOrObservation();
                        specimens.add(specimen);
                    }
                }
            }
            List<SpecimenOrObservationBase<?>> typeSpecimens = loadTypeSpecimen(taxon.getName(), config);
            specimens.addAll(typeSpecimens);
            for (TaxonName syn : taxon.getSynonymNames()) {
                typeSpecimens = loadTypeSpecimen(syn, config);
                specimens.addAll(typeSpecimens);
            }

            //TODO maybe still need to check full derivate path #4484, #8424, #9559
            for (SpecimenOrObservationBase<?> specimen : filterPublished(specimens)) {
                SpecimenDTO dto = new SpecimenDTO();
                loadBaseData(config, specimen, dto);
                dto.setLabel(specimen.getTitleCache());
                container.addItem(dto);
            }
            if (container.getCount() > 0 ) {
                result.setSpecimens(container);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            result.addMessage(MessagesDto.NewErrorInstance("Error when loading specimen data.", e));
        }
    }

    /**
     * Loads specimens the "old" way.
     */
    private void loadRootSpecimens(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
        List<SpecimenOrObservationBaseDTO> rootSpecimens = this.repository.getOccurrenceService().listRootUnitDTOsByAssociatedTaxon(
                taxon.getUuid(), null, config.isIncludeUnpublished(),
                config.getSpecimenAssociationFilter(), null);

        if (result.getOccurrenceInfo() == null) {
            OccurrenceInfoDto oid = new OccurrenceInfoDto();
            result.setOccurrenceInfo(oid);
        }
        result.getOccurrenceInfo().setRootSpecimens(rootSpecimens);
    }

    private List<SpecimenOrObservationBase<?>> loadTypeSpecimen(TaxonName name, TaxonPageDtoConfiguration config) {
        List<SpecimenOrObservationBase<?>> result = new ArrayList<>();
        for (SpecimenTypeDesignation desig: name.getSpecimenTypeDesignations()){
            DerivedUnit specimen = desig.getTypeSpecimen();
            if (specimen != null) {
                result.add(specimen);
            }
        }
        return result;
    }

    private void loadMedia(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
        if (!config.isWithMedia()){
            return;
        }
        try {

            List<Media> medias = new ArrayList<>();
            for (TaxonDescription taxonDescription : taxon.getDescriptions()) {
                if (!taxonDescription.isImageGallery()) {
                    continue;
                }

                List<Media> newMedia = taxonDescription.getElements().stream()
                    .filter(el->el.isInstanceOf(TextData.class))
                    .map(el->CdmBase.deproxy(el, TextData.class))
                    .filter(td->true)
                    .flatMap(td->td.getMedia().stream())
                    .collect(Collectors.toList())
                    ;
                medias.addAll(newMedia);
            }
            //TODO collect media from elsewhere

            ContainerDto<MediaDto2> container = new ContainerDto<>();
            for (Media media : medias) {
                handleSingleMedia(config, container, media);
            }
            if (container.getCount() > 0) {
                result.setMedia(container);
            }

        } catch (Exception e) {
            //e.printStackTrace();
            result.addMessage(MessagesDto.NewErrorInstance("Error when loading media data.", e));
        }
    }

    private void loadTaxonNodes(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
        if (!config.isWithTaxonNodes()){
            return;
        }
        try {
            ContainerDto<TaxonNodeDTO> container = new ContainerDto<TaxonPageDto.TaxonNodeDTO>();
            for (TaxonNode node : taxon.getTaxonNodes()) {
                TaxonNodeDTO dto = new TaxonNodeDTO();
                loadBaseData(config, node, dto);
                //classification
                Classification classification = node.getClassification();
                if (classification != null) {
                    dto.setClassificationUuid(node.getClassification().getUuid());
                    dto.setClassificationLabel(classification.getName().getText());
                }
                //TODO lang/locale
                Language language = Language.DEFAULT();

                //status
                TaxonNodeStatus status = node.getStatus();
                if (status != null) {
                    dto.setStatus(status.getLabel(language));
                }
                //placementNote
                dto.setPlacementNote(node.preferredPlacementNote(language));

                //agent relations
                Set<TaxonNodeAgentRelation> agents = node.getAgentRelations();
                if (!agents.isEmpty()) {
                    for (TaxonNodeAgentRelation rel : agents) {
                        TaxonNodeAgentsRelDTO agentDto = new TaxonNodeAgentsRelDTO();
                        loadBaseData(config, rel, agentDto);

                        //TODO laod
                        if (rel.getAgent() != null) {
                            agentDto.setAgent(rel.getAgent().getFullTitle());
                            agentDto.setAgentUuid(rel.getAgent().getUuid());
                            //TODO compute preferred external link
                            agentDto.setAgentLink(null);
                        }
                        if (rel.getType() != null) {
                            agentDto.setType(rel.getType().getTitleCache());
                            agentDto.setTypeUuid(rel.getType().getUuid());
                        }
                        dto.addAgent(agentDto);
                    }
                }
                container.addItem(dto);
            }
            if (container.getCount() > 0) {
                result.setTaxonNodes(container);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            result.addMessage(MessagesDto.NewErrorInstance("Error when loading taxon node data.", e));
        }
    }

    private void loadSynonyms(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {

        if (!config.isWithSynonyms()) {
            return;
        }

        try {
            HomotypicGroupTaxonComparator comparator = new HomotypicGroupTaxonComparator(taxon);

            TaxonName name = taxon.getName();

            //TODO depending on config add/remove accepted name

            //homotypic synonyms
            List<Synonym> homotypicSynonmys = filterPublished(taxon.getHomotypicSynonymsByHomotypicGroup(comparator));

            TaxonPageDto.HomotypicGroupDTO homotypicGroupDto = new TaxonPageDto.HomotypicGroupDTO();
            if (homotypicSynonmys != null && !homotypicSynonmys.isEmpty()) {
                loadBaseData(config, name.getHomotypicalGroup(), homotypicGroupDto);

                for (Synonym syn : homotypicSynonmys) {
                    loadSynonymInGroup(homotypicGroupDto, syn, config, result);
                }
            }
            if (name != null) {
                handleTypification(name.getHomotypicalGroup(), homotypicGroupDto, result, config);
            }
            result.setHomotypicSynonyms(homotypicGroupDto);

            //heterotypic synonyms
            List<HomotypicalGroup> heteroGroups = taxon.getHeterotypicSynonymyGroups();
            if (heteroGroups.isEmpty()) {
                return;
            }
            ContainerDto<HomotypicGroupDTO> heteroContainer = new ContainerDto<>();
            result.setHeterotypicSynonymGroups(heteroContainer);

            for (HomotypicalGroup hg : heteroGroups) {
                TaxonPageDto.HomotypicGroupDTO hgDto = new TaxonPageDto.HomotypicGroupDTO();
                loadBaseData(config, taxon.getName().getHomotypicalGroup(), hgDto);
                heteroContainer.addItem(hgDto);

                List<Synonym> heteroSyns = filterPublished(taxon.getSynonymsInGroup(hg, comparator));
                for (Synonym syn : heteroSyns) {
                    loadSynonymInGroup(hgDto, syn, config, result);
                }
                handleTypification(hg, hgDto, result, config);
            }
        } catch (Exception e) {
            result.addMessage(MessagesDto.NewErrorInstance("Error when loading synonym data.", e));
        }
    }

    private void handleTypification(HomotypicalGroup homotypicalGroup, HomotypicGroupDTO hgDto,
            TaxonPageDto result, TaxonPageDtoConfiguration config) {

        TypeDesignationGroupContainerFormatter formatter = new TypeDesignationGroupContainerFormatter();
        formatter.withAccessionNoType(config.isWithAccessionType());  //remove once this becomes the default
        Set<TypeDesignationBase<?>> designations = homotypicalGroup.getTypeDesignations();
        try {
            TypeDesignationGroupContainer manager = TypeDesignationGroupContainer.NewDefaultInstance((Set)designations);
            List<TaggedText> tags = formatter.toTaggedText(manager);
            String label = TaggedTextFormatter.createString(tags);
            hgDto.setTypes(label);
            hgDto.setTaggedTypes(tags);
        } catch (Exception e) {
            result.addMessage(MessagesDto.NewErrorInstance("Error when creating type designation information", e));
        }
    }

    private void loadConceptRelations(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
        if (!config.isWithTaxonRelationships()){
            return;
        }
        try {
            //concept relations
            ContainerDto<ConceptRelationDTO> conceptRelContainer = new ContainerDto<>();
            TaxonRelationshipFormatter taxRelFormatter = TaxonRelationshipFormatter.INSTANCE();

            //... MAN
            Set<TaxonRelationship> misappliedRels = taxon.getMisappliedNameRelations();
            for (TaxonRelationship rel : misappliedRels) {
                boolean inverse = true;
                boolean withoutName = false;
                loadConceptRelation(taxRelFormatter, rel, conceptRelContainer, inverse, withoutName, config);
            }

            //... pro parte Synonyms
            Set<TaxonRelationship> proParteRels = taxon.getProParteAndPartialSynonymRelations();
            for (TaxonRelationship rel : proParteRels) {
                boolean inverse = true;
                boolean withoutName = false;
                loadConceptRelation(taxRelFormatter, rel, conceptRelContainer, inverse, withoutName, config);
            }

            //TODO MAN and pp from this taxon

            //... to-relations
            Set<TaxonRelationship> toRels = taxon.getRelationsToThisTaxon();
            toRels.removeAll(misappliedRels);
            toRels.removeAll(proParteRels);
            for (TaxonRelationship rel : toRels) {
                boolean inverse = true;
                boolean withoutName = false;
                loadConceptRelation(taxRelFormatter, rel, conceptRelContainer, inverse, withoutName, config);
            }

            //... from-relations
            Set<TaxonRelationship> fromRels = taxon.getRelationsFromThisTaxon();
            for (TaxonRelationship rel : fromRels) {
                boolean inverse = false;
                boolean withoutName = false;
                loadConceptRelation(taxRelFormatter, rel, conceptRelContainer, inverse, withoutName, config);
            }

            if (conceptRelContainer.getCount() > 0) {
                result.setConceptRelations(conceptRelContainer);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            result.addMessage(MessagesDto.NewErrorInstance("Error when loading concept relation data.", e));
        }
    }

    private void loadConceptRelation(TaxonRelationshipFormatter taxRelFormatter, TaxonRelationship rel, ContainerDto<ConceptRelationDTO> conceptRelContainer, boolean inverse,
            boolean withoutName, ISourceableLoaderConfiguration config) {

        List<Language> languages = Arrays.asList(new Language[] {Language.DEFAULT()}); // TODO config.locales;
        List<TaggedText> tags = taxRelFormatter.getTaggedText(rel, inverse, languages, withoutName);
        String relLabel = TaggedTextFormatter.createString(tags);
        ConceptRelationDTO dto = new TaxonPageDto.ConceptRelationDTO();
        loadBaseData(config, rel, dto);
        dto.setRelSource(makeSource(config, rel.getSource()));
        Taxon relTaxon = inverse ? rel.getFromTaxon() : rel.getToTaxon();
        loadAnnotatable(config, relTaxon, dto);
        dto.setRelTaxonId(relTaxon.getId());
        dto.setRelTaxonUuid(relTaxon.getUuid());
        dto.setRelTaxonLabel(relTaxon.getTitleCache());
        dto.setSecSource(makeSource(config, relTaxon.getSecSource()));
        dto.setLabel(relLabel);
        dto.setTaggedLabel(tags);
        dto.setNameUuid(relTaxon.getName() != null ? relTaxon.getName().getUuid() : null);

        if (rel.getType() != null) {
            dto.setRelTypeUuid(rel.getType().getUuid());
        }
        for (TaxonNode node : relTaxon.getTaxonNodes()) {
            Classification classification = node.getClassification();
            if (classification != null) {
                dto.addClassificationUuids(classification.getUuid());
            }
        }
        conceptRelContainer.addItem(dto);
    }

    private void loadSynonymInGroup(TaxonPageDto.HomotypicGroupDTO hgDto, Synonym syn,
            TaxonPageDtoConfiguration config, TaxonPageDto pageDto) {

        TaxonBaseDto synDto = new TaxonBaseDto();
        loadBaseData(config, syn, synDto);
        synDto.setLabel(syn.getTitleCache());
        synDto.setTaggedLabel(getTaggedTaxon(syn, config));

        if (syn.getName() != null) {
            handleName(config, synDto, syn.getName(), pageDto);
        }

        //TODO
        hgDto.addSynonym(synDto);
    }

    private void loadProtologues(TaxonName name, TaxonBaseDto taxonBaseDto) {
        //TODO maybe also load reference DOI/URL if no source external link exists
        NomenclaturalSource nomSource = name.getNomenclaturalSource();
        if (nomSource != null) {
            Set<ExternalLink> links = nomSource.getLinks();
            for (ExternalLink link : links) {
                if (link.getUri() != null) {
                    taxonBaseDto.addProtologue(link.getUri());
                }
            }
        }
    }

    //needed to show rule considered and codeEdition (with source)
    private void handleNomenclaturalStatus(TaxonName name, TaxonBaseDto taxonDto, TaxonPageDtoConfiguration config) {
        //TODO config.getLocales();
        Language locale = Language.DEFAULT();

        for (NomenclaturalStatus status : name.getStatus()) {
            NomenclaturalStatusDTO dto = new NomenclaturalStatusDTO();
            loadBaseData(config, status, dto);
            //type
            if (status.getType() != null) {
                dto.setStatusTypeUuid(status.getType().getUuid());
                Representation rep = status.getType().getPreferredRepresentation(locale);
                dto.setStatusType(rep == null ? status.getType().toString() : rep.getLabel());
            }
            //ruleConsidered
            dto.setRuleConsidered(status.getRuleConsidered());
            //TODO i18n
            if (status.getCodeEdition() != null) {
                dto.setCodeEdition(status.getCodeEdition().getLabel(/*locale*/));
                dto.setCodeEditionSource(makeSource(config, status.getCodeEditionSource()));
            }
            taxonDto.addNomenclaturalStatus(dto);
        }
    }

    private void handleRelatedNames(TaxonName name, TaxonBaseDto taxonDto, TaxonPageDtoConfiguration config) {
        //exclusions TODO handle via config
        Set<UUID> excludedTypes = new HashSet<>();  //both directions
        excludedTypes.add(NameRelationshipType.uuidBasionym);
        excludedTypes.add(NameRelationshipType.uuidReplacedSynonym);
        Set<UUID> excludedFromTypes = new HashSet<>(excludedTypes);
        Set<UUID> excludedToTypes = new HashSet<>(excludedTypes);

        Set<UUID> includedFromTypes = null;
        if (!CdmUtils.isNullSafeEmpty(config.getDirectNameRelTyes())) {
            includedFromTypes = new HashSet<>(config.getDirectNameRelTyes());
        }
        Set<UUID> includedToTypes = null;
        if (!CdmUtils.isNullSafeEmpty(config.getInverseNameRelTyes())) {
            includedToTypes = new HashSet<>(config.getDirectNameRelTyes());
        }
        //TODO non-types

        //TODO config.getLocales();
        Language locale = Language.DEFAULT();

        for (NameRelationship rel : name.getRelationsFromThisName()) {
            TaxonName relatedName = rel.getToName();
            Representation rep = rel.getType().getPreferredRepresentation(locale);
            handleRelatedName(taxonDto, config, rel, relatedName, rep, excludedFromTypes, includedFromTypes, false);
        }

        //to relations
        for (NameRelationship rel : name.getRelationsToThisName()) {
            TaxonName relatedName = rel.getFromName();
            Representation rep = rel.getType().getPreferredInverseRepresentation(Arrays.asList(new Language[] {locale}));
            handleRelatedName(taxonDto, config, rel, relatedName, rep, excludedToTypes, includedToTypes, true);
        }

        //order
        ContainerDto<NameRelationDTO> relatedNames = taxonDto.getRelatedNames();
        if (relatedNames != null) {
            List<NameRelationDTO> items = relatedNames.getItems();
            Collections.sort(items, relatedNamesComparator);

        }
    }

    private static Comparator<NameRelationDTO> relatedNamesComparator = new Comparator<NameRelationDTO>() {

        @Override
        public int compare(NameRelationDTO o1, NameRelationDTO o2) {
            int c = CdmUtils.nullSafeCompareTo(o1.getYear(), o2.getYear(), true);
            if (c != 0) {
                return c;
            }
            String label1 = TaggedTextFormatter.createString(o1.getNameLabel());
            String label2 = TaggedTextFormatter.createString(o1.getNameLabel());
            c = CdmUtils.nullSafeCompareTo(label1, label2, true);
            if (c != 0) {
                return c;
            }
            return CdmUtils.nullSafeCompareTo(o1.getUuid(), o2.getUuid());
        }
    };


    private void handleRelatedName(TaxonBaseDto taxonDto, TaxonPageDtoConfiguration config,
            NameRelationship rel, TaxonName relatedName, Representation rep,
            Set<UUID> excludedTypes, Set<UUID> includedTypes, boolean inverse) {

        if (relatedName == null
                || rel.getType() == null
                || excludedTypes.contains(rel.getType().getUuid())
                || (includedTypes != null && !includedTypes.contains(rel.getType().getUuid()))) {
            return ;
        }
        NameRelationDTO dto = new NameRelationDTO();
        loadBaseData(config, rel, dto);
        //name
        dto.setNameUuid(relatedName.getUuid());
        dto.setNameLabel(relatedName.getTaggedName());
        //... annotations
        loadAnnotatable(config, relatedName, dto);
        //type
        dto.setRelTypeUuid(rel.getType().getUuid());
        dto.setRelType(rep == null ? rel.getType().toString() : rep.getLabel());
        //inverse
        dto.setInverse(inverse);
        //ruleConsidered
        dto.setRuleConsidered(rel.getRuleConsidered());
        //TODO i18n
        if (rel.getCodeEdition() != null) {
            dto.setCodeEdition(rel.getCodeEdition().getLabel(/*locale*/));
            dto.setCodeEditionSource(makeSource(config, rel.getCodeEditionSource()));
        }
        //year
        dto.setYear(relatedName.getReferenceYear());
        taxonDto.addRelatedName(dto);
        return;
    }

    private void loadFacts(Taxon taxon, TaxonPageDto taxonPageDto, TaxonPageDtoConfiguration config) {
        if (config.isWithFacts()) {
            factLoader.loadTaxonFacts(taxon, taxonPageDto, config);
        }
    }

    private void loadNameFacts(TaxonName name, TaxonBaseDto nameDto, TaxonPageDtoConfiguration config, TaxonPageDto pageDto) {
        nameFactLoader.loadNameFacts(name, nameDto, config, pageDto);
    }

    /**
     * Compares an existing last date and the last date of an entity
     * and returns the resulting last date.
     */
    private LocalDateTime getLastUpdated(LocalDateTime existingLastDate, VersionableEntity dateToAddEntity) {

        DateTime dateToAdd = dateToAddEntity.getUpdated() != null ? dateToAddEntity.getUpdated() : dateToAddEntity.getCreated();

        LocalDateTime javaLocalDateTimeOfEntity = DtoUtil.fromDateTime(dateToAdd);

       if (existingLastDate == null) {
           return javaLocalDateTimeOfEntity;
       }else if (javaLocalDateTimeOfEntity == null || javaLocalDateTimeOfEntity.compareTo(existingLastDate) < 0)  {
           return existingLastDate;
       }else {
           return javaLocalDateTimeOfEntity;
       }
    }
}