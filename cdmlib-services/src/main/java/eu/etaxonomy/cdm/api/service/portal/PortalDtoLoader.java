/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.awt.Color;
import java.time.LocalDateTime;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.dto.portal.AnnotatableDto;
import eu.etaxonomy.cdm.api.dto.portal.AnnotationDto;
import eu.etaxonomy.cdm.api.dto.portal.CdmBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.CommonNameDto;
import eu.etaxonomy.cdm.api.dto.portal.ContainerDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionTreeDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDtoBase;
import eu.etaxonomy.cdm.api.dto.portal.FeatureDto;
import eu.etaxonomy.cdm.api.dto.portal.IFactDto;
import eu.etaxonomy.cdm.api.dto.portal.IndividualsAssociationDto;
import eu.etaxonomy.cdm.api.dto.portal.MarkerDto;
import eu.etaxonomy.cdm.api.dto.portal.MessagesDto;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.SingleSourcedDto;
import eu.etaxonomy.cdm.api.dto.portal.SourceDto;
import eu.etaxonomy.cdm.api.dto.portal.SourcedDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonInteractionDto;
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
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionInfoConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.api.service.geo.DistributionServiceUtilities;
import eu.etaxonomy.cdm.api.service.geo.IDistributionService;
import eu.etaxonomy.cdm.api.service.l10n.LocaleContext;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetContainer;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetFormatter;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.compare.taxon.TaxonComparator;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.format.description.CategoricalDataFormatter;
import eu.etaxonomy.cdm.format.description.QuantitativeDataFormatter;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.format.taxon.TaxonRelationshipFormatter;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.common.SingleSourcedEntityBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TemporalData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.taxon.TaxonBaseDefaultCacheStrategy;

/**
 * Loads the portal dto from a taxon instance.
 * Maybe later also supports loading from persistence.
 *
 * @author a.mueller
 * @date 09.01.2023
 */
public class PortalDtoLoader {

    private static final Logger logger = LogManager.getLogger();

    private ICdmRepository repository;

    public PortalDtoLoader(ICdmRepository repository) {
        this.repository = repository;
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
            result.setNameLabel(name != null? name.getTitleCache() : "");
            result.setLabel(CdmUtils.Nz(taxon.getTitleCache()));
//        result.setTypedTaxonLabel(getTypedTaxonLabel(taxon, config));
            result.setTaggedLabel(getTaggedTaxon(taxon, config));
            handleRelatedNames(taxon.getName(), result, config);
        } catch (Exception e) {
            //e.printStackTrace();
            result.addMessage(MessagesDto.NewErrorInstance("Error when loading accepted name data.", e));
        }
    }

    private List<TaggedText> getTaggedTaxon(TaxonBase<?> taxon, TaxonPageDtoConfiguration config) {
//        List<TypedLabel> result = new ArrayList<>();
        TaxonBaseDefaultCacheStrategy<TaxonBase<?>> formatter = new TaxonBaseDefaultCacheStrategy<>();
        List<TaggedText> tags = formatter.getTaggedTitle(taxon);
        return tags;
    }

    private void loadKeys(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
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
        //TODO load specimen from multiple places

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

            for (SpecimenOrObservationBase<?> specimen : specimens) {
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
                Map<Language, LanguageString> statusNote = node.getStatusNote();
                if (statusNote != null) {
                    //TODO handle fallback lang
                    LanguageString statusNoteStr = statusNote.get(language);
                    if (statusNoteStr == null && statusNote.size() > 0) {
                        statusNoteStr = statusNote.entrySet().iterator().next().getValue();
                    }
                    if (statusNoteStr != null) {
                        dto.setStatusNote(statusNoteStr.getText());
                    }
                }
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

        try {
            //        List<HomotypicalGroup> homotypicGroups = taxon.getHomotypicSynonymyGroups();

            TaxonComparator comparator = new TaxonComparator();

            TaxonName name = taxon.getName();

            //TODO depending on config add/remove accepted name

            //TODO check publish flag

            //homotypic synonyms
            List<Synonym> homotypicSynonmys = taxon.getHomotypicSynonymsByHomotypicGroup(comparator);
            TaxonPageDto.HomotypicGroupDTO homotypicGroupDto = new TaxonPageDto.HomotypicGroupDTO();
            if (homotypicSynonmys != null && !homotypicSynonmys.isEmpty()) {
                loadBaseData(name.getHomotypicalGroup(), homotypicGroupDto);

                for (Synonym syn : homotypicSynonmys) {
                    loadSynonymsInGroup(homotypicGroupDto, syn, config);
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

                List<Synonym> heteroSyns = taxon.getSynonymsInGroup(hg, comparator);
                for (Synonym syn : heteroSyns) {
                    loadSynonymsInGroup(hgDto, syn, config);
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

        boolean withCitation = true;
        boolean withStartingTypeLabel = true;
        boolean withNameIfAvailable = false;
        TypeDesignationSetFormatter formatter = new TypeDesignationSetFormatter(
                withCitation, withStartingTypeLabel, withNameIfAvailable);
        Set<TypeDesignationBase<?>> desigs = homotypicalGroup.getTypeDesignations();
        try {
            TypeDesignationSetContainer manager = TypeDesignationSetContainer.NewDefaultInstance((Set)desigs);
            List<TaggedText> tags = formatter.toTaggedText(manager);
            String label = TaggedCacheHelper.createString(tags);
            hgDto.setTypes(label);
            hgDto.setTaggedTypes(tags);
//            hgDto.setTypedTypes(null);

        } catch (TypeDesignationSetException e) {
            result.addMessage(MessagesDto.NewErrorInstance("Error when creating type designation information", e));
        }
    }

    private void loadConceptRelations(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {

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
        Taxon relTaxon = inverse ? rel.getFromTaxon() : rel.getToTaxon();
        dto.setRelTaxonId(relTaxon.getId());
        dto.setRelTaxonUuid(relTaxon.getUuid());
        dto.setRelTaxonLabel(relTaxon.getTitleCache());
        dto.setLabel(relLabel);
        conceptRelContainer.addItem(dto);
    }

    private void loadSynonymsInGroup(TaxonPageDto.HomotypicGroupDTO hgDto, Synonym syn, TaxonPageDtoConfiguration config) {
        TaxonBaseDto synDto = new TaxonBaseDto();
        loadBaseData(syn, synDto);
        synDto.setLabel(syn.getTitleCache());
        synDto.setTaggedLabel(getTaggedTaxon(syn, config));
        if (syn.getName() != null) {
            synDto.setNameLabel(syn.getName().getTitleCache());
            handleRelatedNames(syn.getName(), synDto, config);
        }

        //TODO
        hgDto.addSynonym(synDto);
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

        try {
            //compute the features that do exist for this taxon
            Map<UUID, Feature> existingFeatureUuids = getExistingFeatureUuids(taxon);

            //filter, sort and structure according to feature tree
            TreeNode<Feature, UUID> filteredRootNode;
            if (config.getFeatureTree() != null) {

                //TODO class cast
                TermTree<Feature> featureTree = repository.getTermTreeService().find(config.getFeatureTree());
                filteredRootNode = filterFeatureNode(featureTree.getRoot(), existingFeatureUuids.keySet());
            } else {
                filteredRootNode = createDefaultFeatureNode(taxon);
            }

            //load facts per feature
            Map<UUID,Set<DescriptionElementBase>> featureMap = loadFeatureMap(taxon);

            //load final result
            if (!filteredRootNode.getChildren().isEmpty()) {
                ContainerDto<FeatureDto> features = new ContainerDto<>();
                for (TreeNode<Feature,UUID> node : filteredRootNode.getChildren()) {
                    handleFeatureNode(taxon, config, featureMap, features, node);
                }
                taxonPageDto.setFactualData(features);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            taxonPageDto.addMessage(MessagesDto.NewErrorInstance("Error when loading factual data.", e));
        }
    }

    private void handleFeatureNode(Taxon taxon, TaxonPageDtoConfiguration config,
            Map<UUID, Set<DescriptionElementBase>> featureMap, ContainerDto<FeatureDto> features,
            TreeNode<Feature, UUID> node) {

        Feature feature = node.getData();
        //TODO locale
        FeatureDto featureDto = new FeatureDto(feature.getUuid(), feature.getId(), feature.getLabel());
        features.addItem(featureDto);

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
                handleFact(featureDto, fact);
            }
        }

        handleDistributions(config, featureDto, taxon, distributions);
        //TODO really needed?
        orderFacts(featureDto);

        //children
        ContainerDto<FeatureDto> childFeatures = new ContainerDto<>();
        for (TreeNode<Feature,UUID> child : node.getChildren()) {
            handleFeatureNode(taxon, config, featureMap, childFeatures, child);
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

    private Map<UUID, Set<DescriptionElementBase>> loadFeatureMap(Taxon taxon) {
        Map<UUID, Set<DescriptionElementBase>> featureMap = new HashMap<>();

        //... load facts
        for (TaxonDescription taxonDescription : taxon.getDescriptions()) {
            if (taxonDescription.isImageGallery()) {
                continue;
            }
            for (DescriptionElementBase deb : taxonDescription.getElements()) {
                Feature feature = deb.getFeature();
                if (featureMap.get(feature.getUuid()) == null) {
                    featureMap.put(feature.getUuid(), new HashSet<>());
                }
                featureMap.get(feature.getUuid()).add(deb);
            }
        }
        return featureMap;
    }

    private TreeNode<Feature, UUID> createDefaultFeatureNode(Taxon taxon) {
        TreeNode<Feature, UUID> root = new TreeNode<>();
        Set<Feature> requiredFeatures = new HashSet<>();

        for (TaxonDescription taxonDescription : taxon.getDescriptions()) {
            if (taxonDescription.isImageGallery()) {
                continue;
            }
            for (DescriptionElementBase deb : taxonDescription.getElements()) {
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

    /**
     * Computes the (unsorted) set of features for  which facts exist
     * for the given taxon.
     */
    private Map<UUID, Feature> getExistingFeatureUuids(Taxon taxon) {
        Map<UUID, Feature> result = new HashMap<>();
        for (TaxonDescription taxonDescription : taxon.getDescriptions()) {
            if (taxonDescription.isImageGallery()) {
                continue;
            }
            for (DescriptionElementBase deb : taxonDescription.getElements()) {
                Feature feature = deb.getFeature();
                if (feature != null) {  //null should not happen
                    result.put(feature.getUuid(), feature);
                }
            }
        }
        return result;
    }

    private void handleDistributions(TaxonPageDtoConfiguration config, FeatureDto featureDto,
            Taxon taxon, List<Distribution> distributions) {

        if (distributions.isEmpty()) {
            return;
        }
        IDistributionService distributionService = repository.getDistributionService();

        //configs
        DistributionInfoConfiguration distributionConfig = config.getDistributionInfoConfiguration();
        CondensedDistributionConfiguration condensedConfig = distributionConfig.getCondensedDistrConfig();

        String statusColorsString = distributionConfig.getStatusColorsString();


        //copied from DescriptionListController

        boolean ignoreDistributionStatusUndefined = true;  //workaround until #9500 is fully implemented
        distributionConfig.setIgnoreDistributionStatusUndefined(ignoreDistributionStatusUndefined);
        boolean fallbackAsParent = true;  //may become a service parameter in future

        DistributionInfoDto dto;

        //hiddenArea markers include markers for fully hidden areas and fallback areas. The later
        //are hidden markers on areas that have non-hidden subareas (#4408)
        Set<MarkerType> hiddenAreaMarkerTypes = distributionConfig.getHiddenAreaMarkerTypeList();
        if(hiddenAreaMarkerTypes != null && !hiddenAreaMarkerTypes.isEmpty()){
            condensedConfig.hiddenAndFallbackAreaMarkers = hiddenAreaMarkerTypes.stream().map(mt->mt.getUuid()).collect(Collectors.toSet());
        }

        List<String> initStrategy = null;

        Map<PresenceAbsenceTerm, Color> distributionStatusColors;
        try {
            distributionStatusColors = DistributionServiceUtilities.buildStatusColorMap(
                    statusColorsString, repository.getTermService(), repository.getVocabularyService());
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException when reading distribution status colors");
            //TODO is null allowed?
            distributionStatusColors = null;
        }

        dto = distributionService.composeDistributionInfoFor(distributionConfig, taxon.getUuid(),
                fallbackAsParent,
                distributionStatusColors, LocaleContext.getLanguages(),
                initStrategy);

        if (distributionConfig.isUseTreeDto() && dto.getTree() != null) {
            DistributionTreeDto tree = (DistributionTreeDto)dto.getTree();
            TreeNode<Set<DistributionDto>, NamedAreaDto> root = tree.getRootElement();
            //fill uuid->distribution map
            Map<UUID,Distribution> distributionMap = new HashMap<>();
            distributions.stream().forEach(d->distributionMap.put(d.getUuid(), d));
            handleDistributionDtoNode(distributionMap, root);
        }

        featureDto.addFact(dto);
    }

    private void handleDistributionDtoNode(Map<UUID, Distribution> map,
            TreeNode<Set<DistributionDto>, NamedAreaDto> root) {
       if (root.getData() != null) {
           root.getData().stream().forEach(d->{
               Distribution distr  = map.get(d.getUuid());
               loadBaseData(distr, d);
               d.setTimeperiod(distr.getTimeperiod() == null ? null : distr.getTimeperiod().toString());

           });
       }
       //handle children
       if (root.getChildren() != null) {
           root.getChildren().stream().forEach(c->handleDistributionDtoNode(map, c));
       }
    }

    private FactDtoBase handleFact(FeatureDto featureDto, DescriptionElementBase fact) {
        //TODO locale
        Language localeLang = null;

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
            loadBaseData(td, factDto);
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
            loadBaseData(ctn, dto);
            //TODO sort all common names
            result = dto;
        } else if (fact.isInstanceOf(IndividualsAssociation.class)) {
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
        } else if (fact.isInstanceOf(TaxonInteraction.class)) {
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
            loadBaseData(cd, factDto);
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
            loadBaseData(qd, factDto);
            result = factDto;
        }else if (fact.isInstanceOf(TemporalData.class)) {
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
//            TODO
            logger.warn("DescriptionElement type not yet handled: " + fact.getClass().getSimpleName());
            return null;
        }
        result.setTimeperiod(fact.getTimeperiod() == null ? null : fact.getTimeperiod().toString());
        return result;
    }

    private String getTermLabel(TermBase term, Language localeLang) {
        if (term == null) {
            return null;
        }
        Representation rep = term.getPreferredRepresentation(localeLang);
        String label = rep == null ? null : rep.getLabel();
        label = label == null ? term.getLabel() : label;
        return label;
    }

    /**
     * Compares an existing last date and the last date of an entity
     * and returns the resulting last date.
     */
    private LocalDateTime getLastUpdated(LocalDateTime existingLastDate, VersionableEntity dateToAddEntity) {

        DateTime dateToAdd = dateToAddEntity.getUpdated() != null ? dateToAddEntity.getUpdated() : dateToAddEntity.getCreated();

        LocalDateTime javaLocalDateTimeOfEntity = dateToAdd == null ? null:
                LocalDateTime.of(dateToAdd.getYear(), dateToAdd.getMonthOfYear(),
                        dateToAdd.getDayOfMonth(), dateToAdd.getHourOfDay(),
                        dateToAdd.getMinuteOfHour(), dateToAdd.getSecondOfMinute());

       if (existingLastDate == null) {
           return javaLocalDateTimeOfEntity;
       }else if (javaLocalDateTimeOfEntity == null || javaLocalDateTimeOfEntity.compareTo(existingLastDate) < 0)  {
           return existingLastDate;
       }else {
           return javaLocalDateTimeOfEntity;
       }
    }

    private void loadBaseData(CdmBase cdmBase, CdmBaseDto dto) {
        dto.setId(cdmBase.getId());
        dto.setUuid(cdmBase.getUuid());

        loadAnnotatable(cdmBase, dto);
        loadSources(cdmBase, dto);
        //loadIdentifiable(cdmBase, dto);
    }

    private void loadSources(CdmBase cdmBase, CdmBaseDto dto) {
        if (dto instanceof SingleSourcedDto && cdmBase.isInstanceOf(SingleSourcedEntityBase.class)) {
            //TODO other sourced
            SingleSourcedEntityBase sourced = CdmBase.deproxy(cdmBase, SingleSourcedEntityBase.class);
            SingleSourcedDto sourcedDto = (SingleSourcedDto)dto;
            NamedSource source = sourced.getSource();
            SourceDto sourceDto = new SourceDto();
            loadSource(source, sourceDto);
            sourcedDto.setSource(sourceDto);
        } else if (dto instanceof SourcedDto && cdmBase instanceof ISourceable) {
            @SuppressWarnings("unchecked")
            ISourceable<OriginalSourceBase> sourced = (ISourceable<OriginalSourceBase>)cdmBase;
            SourcedDto sourcedDto = (SourcedDto)dto;
            for (OriginalSourceBase source : sourced.getSources()) {
                SourceDto sourceDto = new SourceDto();
                loadSource(source, sourceDto);
                sourcedDto.addSource(sourceDto);
            }
        }
    }

    private void loadSource(OriginalSourceBase source, SourceDto sourceDto) {

        source = CdmBase.deproxy(source);
        //base data
        loadBaseData(source, sourceDto);

        ICdmBase linkedObject = source.getCitation();
        if (linkedObject == null) {
            //cdmsource
            linkedObject = source.getCdmSource();
        }

        //citation uuid & doi
        if (source.getCitation()!= null) {
            sourceDto.setDoi(source.getCitation().getDoiString());
        }

        //label
        //TODO this probably does not use specimen or cdmSource if necessary,
        //     also long citation is still preliminary
        String label = OriginalSourceFormatter.INSTANCE_LONG_CITATION.format(source);
        TypedLabel typedLabel = new TypedLabel(source, label);
        sourceDto.addLabel(typedLabel);
        sourceDto.setType(source.getType() != null ? source.getType().toString() : null);

        if (source.isInstanceOf(NamedSourceBase.class)) {
            NamedSourceBase ns = CdmBase.deproxy(source, NamedSourceBase.class);

            //nameUsedInSource
            TaxonName name =  ns.getNameUsedInSource();
            if (name != null) {
                List<TaggedText> taggedName = name.cacheStrategy().getTaggedTitle(name);
                //TODO nom status?
                sourceDto.setNameInSource(taggedName);
                sourceDto.setNameInSourceUuid(name.getUuid());
            }

            //specimen uuid
            if (source.isInstanceOf(DescriptionElementSource.class)) {
                DescriptionElementSource des = CdmBase.deproxy(source, DescriptionElementSource.class);
                if (linkedObject == null) {
                    linkedObject = des.getSpecimen();
                }
            }
        }

        sourceDto.setLinkedUuid(getUuid(linkedObject));
        String linkedObjectStr = linkedObject == null ? null : CdmBase.deproxy(linkedObject).getClass().getSimpleName();
        sourceDto.setLinkedClass(linkedObjectStr);
    }

    private UUID getUuid(ICdmBase cdmBase) {
        return cdmBase == null ? null : cdmBase.getUuid();
    }

    private void loadAnnotatable(CdmBase cdmBase, CdmBaseDto dto) {
        if (dto instanceof AnnotatableDto && cdmBase.isInstanceOf(AnnotatableEntity.class)) {
            AnnotatableEntity annotatable = CdmBase.deproxy(cdmBase, AnnotatableEntity.class);
            AnnotatableDto annotatableDto = (AnnotatableDto)dto;
            //annotation
            for (Annotation annotation : annotatable.getAnnotations()) {
                if (annotation.getAnnotationType() != null
                        //TODO annotation type filter
                        && annotation.getAnnotationType().getUuid().equals(AnnotationType.uuidEditorial)
                        && StringUtils.isNotBlank(annotation.getText())) {

                    AnnotationDto annotationDto = new AnnotationDto();
                    annotatableDto.addAnnotation(annotationDto);
                    //TODO id needed? but need to adapt dto and container then
                    loadBaseData(annotation, annotationDto);
                    annotationDto.setText(annotation.getText());
                    UUID uuidAnnotationType = annotation.getAnnotationType() == null ? null :annotation.getAnnotationType().getUuid();
                    annotationDto.setTypeUuid(uuidAnnotationType);
                    //language etc. currently not yet used
                }
            }

            //marker
            for (Marker marker : annotatable.getMarkers()) {
                if (marker.getMarkerType() != null
                        //TODO markertype filter
//                        && marker.getMarkerType().getUuid().equals(AnnotationType.uuidEditorial)
                           ){

                    MarkerDto markerDto = new MarkerDto();
                    annotatableDto.addMarker(markerDto);
                    //TODO id needed? but need to adapt dto and container then
                    loadBaseData(marker, markerDto);
                    if (marker.getMarkerType() != null) {
                        markerDto.setTypeUuid(marker.getMarkerType().getUuid());
                        //TODO locale
                        markerDto.setType(marker.getMarkerType().getTitleCache());
                    }
                    markerDto.setValue(marker.getValue());
                }
            }
        }
    }
}