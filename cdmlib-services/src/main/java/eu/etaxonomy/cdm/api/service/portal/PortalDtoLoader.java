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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.api.dto.portal.CdmBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.ContainerDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDto;
import eu.etaxonomy.cdm.api.dto.portal.FeatureDto;
import eu.etaxonomy.cdm.api.dto.portal.MessagesDto;
import eu.etaxonomy.cdm.api.dto.portal.MessagesDto.MessageType;
import eu.etaxonomy.cdm.api.dto.portal.TaxonBaseDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.ConceptRelationDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.HomotypicGroupDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.KeyDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.MediaDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.MediaRepresentationDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.SpecimenDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.TaxonNodeAgentsRelDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.TaxonNodeDTO;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetContainer;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetFormatter;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.compare.taxon.TaxonComparator;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.format.taxon.TaxonRelationshipFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
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

    public TaxonPageDto load(Taxon taxon, TaxonPageDtoConfiguration config) {
        TaxonPageDto result = new TaxonPageDto();

        TaxonName name = taxon.getName();

        //load 1:1
        loadBaseData(taxon, result);
        result.setLastUpdated(getLastUpdated(null, taxon));
        result.setNameLabel(name != null? name.getTitleCache() : "");
        result.setTaxonLabel(CdmUtils.Nz(taxon.getTitleCache()));
//        result.setTypedTaxonLabel(getTypedTaxonLabel(taxon, config));
        result.setTaggedTaxon(getTaggedTaxon(taxon, config));

        loadTaxonNodes(taxon, result, config);
        loadSynonyms(taxon, result, config);
        loadConceptRelations(taxon, result, config);
        loadFacts(taxon, result, config);
        loadMedia(taxon, result, config);
        loadSpecimens(taxon, result, config);
        loadKeys(taxon, result, config);

        return result;
    }

    private List<TaggedText> getTaggedTaxon(TaxonBase<?> taxon, TaxonPageDtoConfiguration config) {
//        List<TypedLabel> result = new ArrayList<>();
        TaxonBaseDefaultCacheStrategy<TaxonBase<?>> formatter = new TaxonBaseDefaultCacheStrategy<>();
        List<TaggedText> tags = formatter.getTaggedTitle(taxon);
        return tags;
    }

    private void loadKeys(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
        ContainerDto<KeyDTO> container =new ContainerDto<>();
        //TODO

        if (container.getCount() > 0) {
            result.setKeys(container);
        }
    }

    private void loadSpecimens(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
        //TODO load specimen from multiple places

        ContainerDto<SpecimenDTO> container = new ContainerDto<TaxonPageDto.SpecimenDTO>();

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

    }

    private void loadTaxonNodes(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
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

    }


    private void loadSynonyms(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {
//        List<HomotypicalGroup> homotypicGroups = taxon.getHomotypicSynonymyGroups();

        TaxonComparator comparator = new TaxonComparator();

        TaxonName name = taxon.getName();

        //TODO depending on config add/remove accepted name

        //homotypic synonyms
        List<Synonym> homotypicSynonmys = taxon.getHomotypicSynonymsByHomotypicGroup(comparator);
        TaxonPageDto.HomotypicGroupDTO homotypicGroupDto = new TaxonPageDto.HomotypicGroupDTO();
        if (!homotypicSynonmys.isEmpty()) {
            loadBaseData(name.getHomotypicalGroup(), homotypicGroupDto);

            for (Synonym syn : homotypicSynonmys) {
                loadSynonymsInGroup(homotypicGroupDto, syn, config);
            }
        }
        //TODO NPE
        handleTypification(name.getHomotypicalGroup(), homotypicGroupDto, result, config);
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
            hgDto.setTypedTypes(null);

        } catch (TypeDesignationSetException e) {
            result.addMessage(new MessagesDto(MessageType.ERROR, "Error when creating type designation information"));
        }
    }

    private void loadConceptRelations(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {

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
        synDto.setNameLabel(syn.getName().getTitleCache());
        synDto.setTaxonLabel(syn.getTitleCache());
        synDto.setTaggedTaxon(getTaggedTaxon(syn, config));
        //TODO
        hgDto.addSynonym(synDto);
    }

    private void loadFacts(Taxon taxon, TaxonPageDto result, TaxonPageDtoConfiguration config) {

       //TODO load feature tree
       Map<Feature,Set<DescriptionElementBase>> featureMap = new HashMap<>();

       //load facts
       for (TaxonDescription taxonDescription : taxon.getDescriptions()) {
           if (taxonDescription.isImageGallery()) {
               continue;
           }
           for (DescriptionElementBase deb : taxonDescription.getElements()) {
               Feature feature = deb.getFeature();
               if (featureMap.get(feature) == null) {
                   featureMap.put(feature, new HashSet<>());
               }
               featureMap.get(feature).add(deb);
           }
       }

       //TODO sort
        if (!featureMap.isEmpty()) {
            ContainerDto<FeatureDto> features = new ContainerDto<>();
            result.setFactualData(features);
            for (Feature feature : featureMap.keySet()) {
                FeatureDto featureDto = new FeatureDto();
                featureDto.setId(feature.getId());
                featureDto.setUuid(feature.getUuid());
                //TODO locale
                featureDto.setLabel(feature.getTitleCache());
                features.addItem(featureDto);

                //
                for (DescriptionElementBase fact : featureMap.get(feature)){
                    handleFact(featureDto, fact);
                }
            }
        }
   }

    private void handleFact(FeatureDto featureDto, DescriptionElementBase fact) {
        if (fact.isInstanceOf(TextData.class)) {
            TextData td = CdmBase.deproxy(fact, TextData.class);
            //TODO locale
            Language lang = null;
            LanguageString ls = td.getPreferredLanguageString(lang);
            String text = ls == null ? "" : CdmUtils.Nz(ls.getText());

            FactDto factDto = new FactDto();
            featureDto.getFacts().add(factDto);
            //TODO do we really need type information for textdata here?
            TypedLabel typedLabel = new TypedLabel(text);
            typedLabel.setClassAndId(td);
            factDto.getTypedLabel().add(typedLabel);
        }else {
//            TODO
        }

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
    }

}
