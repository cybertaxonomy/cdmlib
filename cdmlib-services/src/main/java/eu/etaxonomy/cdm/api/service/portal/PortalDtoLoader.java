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
import eu.etaxonomy.cdm.api.dto.portal.MessagesDto;
import eu.etaxonomy.cdm.api.dto.portal.OccurrenceInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto.TaxonNameDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.ConceptRelationDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.HomotypicGroupDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.KeyDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.MediaDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.MediaRepresentationDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.NameRelationDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.SpecimenDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.TaxonNodeAgentsRelDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.TaxonNodeDTO;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.filter.TaxonOccurrenceRelationType;
import eu.etaxonomy.cdm.api.service.dto.DtoUtil;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetContainer;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetFormatter;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.compare.taxon.TaxonComparator;
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
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
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
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.taxon.TaxonBaseDefaultCacheStrategy;

/**
 * Loads the portal dto from a taxon instance.
 * Maybe later also supports loading from persistence.
 *
 * @author a.mueller
 * @date 09.01.2023
 */
public class PortalDtoLoader extends PortalDtoLoaderBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private PortalDtoFactLoader factLoader;

    public PortalDtoLoader(ICdmRepository repository, ICdmGenericDao dao) {
        super(repository, dao);
        this.factLoader = new PortalDtoFactLoader(repository, dao);
    }

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
            loadBaseData(taxon, result);
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
        loadBaseData(name, nameDto);

        INameCacheStrategy formatter = name.cacheStrategy();
        formatter.setEtAlPosition(config.getEtAlPosition());

        taxonDto.setName(nameDto);
        taxonDto.setNameLabel(formatter.getTitleCache(name));
        handleRelatedNames(name, taxonDto, config);
        loadProtologues(name, taxonDto);
        taxonDto.setNameUuid(name.getUuid());
        taxonDto.setNameType(name.getNameType().toString());
        loadNameFacts(name, taxonDto, config, pageDto);
        nameDto.setTaggedName(formatter.getTaggedFullTitle(name));
    }

    private List<TaggedText> getTaggedTaxon(TaxonBase<?> taxon, TaxonPageDtoConfiguration config) {
//        List<TypedLabel> result = new ArrayList<>();
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
                loadBaseData(key, dto);
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
                loadBaseData(specimen, dto);
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
            ContainerDto<MediaDTO> container = new ContainerDto<TaxonPageDto.MediaDTO>();

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
            for (Media media : medias) {
                MediaDTO dto = new TaxonPageDto.MediaDTO();
                loadBaseData(media, dto);
                dto.setLabel(media.getTitleCache());
                ContainerDto<MediaRepresentationDTO> representations = new ContainerDto<>();
                for (MediaRepresentation rep : media.getRepresentations()) {
                    MediaRepresentationDTO repDto = new MediaRepresentationDTO();
                    loadBaseData(rep, dto);
                    repDto.setMimeType(rep.getMimeType());
                    repDto.setSuffix(rep.getSuffix());
                    if (!rep.getParts().isEmpty()) {
                        //TODO handle message if n(parts) > 1
                        MediaRepresentationPart part = rep.getParts().get(0);
                        repDto.setUri(part.getUri());
                        repDto.setClazz(part.getClass());
                        repDto.setSize(part.getSize());
                        if (part.isInstanceOf(ImageFile.class)) {
                            ImageFile image = CdmBase.deproxy(part, ImageFile.class);
                            repDto.setHeight(image.getHeight());
                            repDto.setWidth(image.getWidth());
                        }
                        //TODO AudioFile etc.
                    }
                    representations.addItem(repDto);
                }
                if (representations.getCount() > 0) {
                    dto.setRepresentations(representations);
                }
                //TODO load representation data
                container.addItem(dto);
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
                loadBaseData(node, dto);
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
                //statusNote
                dto.setStatusNote(node.preferredStatusNote(language));

                //agent relations
                Set<TaxonNodeAgentRelation> agents = node.getAgentRelations();
                if (!agents.isEmpty()) {
                    for (TaxonNodeAgentRelation rel : agents) {
                        TaxonNodeAgentsRelDTO agentDto = new TaxonNodeAgentsRelDTO();
                        loadBaseData(rel, agentDto);

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
            //        List<HomotypicalGroup> homotypicGroups = taxon.getHomotypicSynonymyGroups();

            TaxonComparator comparator = new TaxonComparator();

            TaxonName name = taxon.getName();

            //TODO depending on config add/remove accepted name

            //homotypic synonyms
            List<Synonym> homotypicSynonmys = filterPublished(taxon.getHomotypicSynonymsByHomotypicGroup(comparator));

            TaxonPageDto.HomotypicGroupDTO homotypicGroupDto = new TaxonPageDto.HomotypicGroupDTO();
            if (homotypicSynonmys != null && !homotypicSynonmys.isEmpty()) {
                loadBaseData(name.getHomotypicalGroup(), homotypicGroupDto);

                for (Synonym syn : homotypicSynonmys) {
                    loadSynonymsInGroup(homotypicGroupDto, syn, config, result);
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
                loadBaseData(taxon.getName().getHomotypicalGroup(), hgDto);
                heteroContainer.addItem(hgDto);

                List<Synonym> heteroSyns = filterPublished(taxon.getSynonymsInGroup(hg, comparator));
                for (Synonym syn : heteroSyns) {
                    loadSynonymsInGroup(hgDto, syn, config, result);
                }
                handleTypification(hg, hgDto, result, config);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            result.addMessage(MessagesDto.NewErrorInstance("Error when loading synonym data.", e));
        }
    }

    private void handleTypification(HomotypicalGroup homotypicalGroup, HomotypicGroupDTO hgDto,
            TaxonPageDto result, TaxonPageDtoConfiguration config) {

        TypeDesignationSetFormatter formatter = new TypeDesignationSetFormatter();
        Set<TypeDesignationBase<?>> desigs = homotypicalGroup.getTypeDesignations();
        try {
            TypeDesignationSetContainer manager = TypeDesignationSetContainer.NewDefaultInstance((Set)desigs);
            List<TaggedText> tags = formatter.toTaggedText(manager);
            String label = TaggedCacheHelper.createString(tags);
            hgDto.setTypes(label);
            hgDto.setTaggedTypes(tags);
//            hgDto.setTypedTypes(null);

        } catch (Exception e) {
//          e.printStackTrace();
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
                loadConceptRelation(taxRelFormatter, rel, conceptRelContainer, inverse, withoutName);
            }

            //... pro parte Synonyms
            Set<TaxonRelationship> proParteRels = taxon.getProParteAndPartialSynonymRelations();
            for (TaxonRelationship rel : proParteRels) {
                boolean inverse = true;
                boolean withoutName = false;
                loadConceptRelation(taxRelFormatter, rel, conceptRelContainer, inverse, withoutName);
            }

            //TODO MAN and pp from this taxon

            //... to-relations
            Set<TaxonRelationship> toRels = taxon.getRelationsToThisTaxon();
            toRels.removeAll(misappliedRels);
            toRels.removeAll(proParteRels);
            for (TaxonRelationship rel : toRels) {
                boolean inverse = true;
                boolean withoutName = false;
                loadConceptRelation(taxRelFormatter, rel, conceptRelContainer, inverse, withoutName);
            }

            //... from-relations
            Set<TaxonRelationship> fromRels = taxon.getRelationsFromThisTaxon();
            for (TaxonRelationship rel : fromRels) {
                boolean inverse = false;
                boolean withoutName = false;
                loadConceptRelation(taxRelFormatter, rel, conceptRelContainer, inverse, withoutName);
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
            boolean withoutName) {

        List<Language> languages = Arrays.asList(new Language[] {Language.DEFAULT()}); // TODO config.locales;
        List<TaggedText> tags = taxRelFormatter.getTaggedText(rel, inverse, languages, withoutName);
        String relLabel = TaggedCacheHelper.createString(tags);
        ConceptRelationDTO dto = new TaxonPageDto.ConceptRelationDTO();
        loadBaseData(rel, dto);
        dto.setRelSource(makeSource(rel.getSource()));
        Taxon relTaxon = inverse ? rel.getFromTaxon() : rel.getToTaxon();
        dto.setRelTaxonId(relTaxon.getId());
        dto.setRelTaxonUuid(relTaxon.getUuid());
        dto.setRelTaxonLabel(relTaxon.getTitleCache());
        dto.setSecSource(makeSource(relTaxon.getSecSource()));
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

    private void loadSynonymsInGroup(TaxonPageDto.HomotypicGroupDTO hgDto, Synonym syn,
            TaxonPageDtoConfiguration config, TaxonPageDto pageDto) {

        TaxonBaseDto synDto = new TaxonBaseDto();
        loadBaseData(syn, synDto);
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

    private void handleRelatedNames(TaxonName name, TaxonBaseDto taxonDto, TaxonPageDtoConfiguration config) {
        //exclusions TODO handle via config
        Set<UUID> excludedTypes = new HashSet<>();  //both directions
        excludedTypes.add(NameRelationshipType.uuidBasionym);
        excludedTypes.add(NameRelationshipType.uuidReplacedSynonym);
        Set<UUID> excludedFromTypes = new HashSet<>(excludedTypes);
        Set<UUID> excludedToTypes = new HashSet<>(excludedTypes);
        //TODO non-types

        //TODO config.getLocales();
        Language locale = Language.DEFAULT();

        for (NameRelationship rel : name.getRelationsFromThisName()) {
            TaxonName relatedName = rel.getToName();
            if (relatedName == null || rel.getType() == null || excludedFromTypes.contains(rel.getType().getUuid())) {
                continue;
            }
            NameRelationDTO dto = new NameRelationDTO();
            loadBaseData(rel, dto);
            //name
            dto.setNameUuid(relatedName.getUuid());
            dto.setNameLabel(relatedName.getTaggedName());
            //type
            dto.setRelTypeUuid(rel.getType().getUuid());
            Representation rep = rel.getType().getPreferredRepresentation(locale);
            dto.setRelType(rep == null ? rel.getType().toString() : rep.getLabel());
            //inverse
            dto.setInverse(false);
            //ruleConsidered
            dto.setRuleConsidered(rel.getRuleConsidered());
            taxonDto.addRelatedName(dto);
        }

        //to relations
        for (NameRelationship rel : name.getRelationsToThisName()) {
            TaxonName relatedName = rel.getFromName();
            if (relatedName == null || rel.getType() == null || excludedFromTypes.contains(rel.getType().getUuid())) {
                continue;
            }
            NameRelationDTO dto = new NameRelationDTO();
            loadBaseData(rel, dto);
            //name
            dto.setNameUuid(relatedName.getUuid());
            dto.setNameLabel(relatedName.getTaggedName());
            //type
            dto.setRelTypeUuid(rel.getType().getUuid());
            Representation rep = rel.getType().getPreferredInverseRepresentation(Arrays.asList(new Language[] {locale}));
            dto.setRelType(rep == null ? rel.getType().toString() : rep.getLabel());
            //inverse
            dto.setInverse(true);
            taxonDto.addRelatedName(dto);
        }
    }

    private void loadFacts(Taxon taxon, TaxonPageDto taxonPageDto, TaxonPageDtoConfiguration config) {
        factLoader.loadTaxonFacts(taxon, taxonPageDto, config);
    }

    private void loadNameFacts(TaxonName name, TaxonBaseDto nameDto, TaxonPageDtoConfiguration config, TaxonPageDto pageDto) {
        factLoader.loadNameFacts(name, nameDto, config, pageDto);
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

                if (isPublicSource(source)) {
        }
                    if (isPublicSource(source)) {
            }
        }
    }

    private static boolean isPublicSource(OriginalSourceBase source) {
        if (source.getType() == null) {
            return false; //should not happen
        }else {
            OriginalSourceType type = source.getType();
            //TODO 3 make source type configurable
            return type.isPrimarySource();
}