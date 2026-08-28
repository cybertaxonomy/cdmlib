/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.cdmprintpub.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainer;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainerFormatter;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportConfigurator;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubFactDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubFactDTO.PrintPubFactKind;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubReferenceEntryDTO.PrintPubReferenceSourceType;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubSynonymDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubSynonymGroupDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.SecundumSource;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextFormatter;

/**
 * Maps CDM domain objects to Print/Publication DTOs.
 *
 * <p>
 * This mapper is responsible for:
 * </p>
 * <ul>
 * <li>accepted-taxon mapping,</li>
 * <li>synonym mapping,</li>
 * <li>type rendering,</li>
 * <li>factual-data extraction.</li>
 * </ul>
 *
 * <p>
 * Name-related bibliography collection is delegated to
 * {@link PrintPubBibliographyCollector}.
 * </p>
 */
@Component
public class PrintPubDtoMapper {

    private final PrintPubBibliographyCollector bibliographyCollector;

    public PrintPubDtoMapper(PrintPubBibliographyCollector bibliographyCollector) {

        this.bibliographyCollector = bibliographyCollector;
    }

    public PrintPubTaxonSummaryDTO mapNodeToDto(TaxonNode node, int referenceDepth, PrintPubExportState state) {

        if (node == null || node.getTaxon() == null) {
            return null;
        }

        Taxon taxon = HibernateProxyHelper.deproxy(node.getTaxon());

        PrintPubTaxonSummaryDTO dto = new PrintPubTaxonSummaryDTO();

        dto.uuid = taxon.getUuid();
        dto.relativeDepth = calculateDepth(node) - referenceDepth;

        TaxonName name = HibernateProxyHelper.deproxy(taxon.getName());

        mapAcceptedName(taxon, name, dto);

        if (name != null) {
            extractTypeData(name, dto, state.getConfig());

            bibliographyCollector.collectAcceptedNameSources(state, name);
        }

        if (state.getConfig().isDoSynonyms()) {
            extractSynonymGroups(state, taxon, dto);
        }

        if (state.getConfig().isDoFactualData()) {
            extractDescriptionData(state, taxon, dto);
        }

        extractTaxonSecReference(state, taxon, dto);

        extractIdentifiers(taxon, dto);

        return dto;
    }

    private void mapAcceptedName(Taxon taxon, TaxonName name, PrintPubTaxonSummaryDTO dto) {

        if (name != null) {
            dto.taggedNameList = name.getTaggedFullTitle();

            dto.scientificName = TaggedTextFormatter.createString(name.getTaggedName());

            dto.titleCache = name.getTitleCache();
        } else {
            dto.titleCache = taxon.getTitleCache();
        }
    }

    private void extractTaxonSecReference(PrintPubExportState state, Taxon taxon, PrintPubTaxonSummaryDTO dto) {

        if (!state.getConfig().isIncludeTaxonomicConceptReference()) {
            return;
        }

        if (taxon.getSec() == null) {
            return;
        }

        Reference reference = HibernateProxyHelper.deproxy(taxon.getSec());

        SecundumSource secSource = taxon.getSecSource();

        if (secSource == null || secSource.getType() == null || !secSource.getType().isPrimarySource()) {
            return;
        }

        state.addReference(reference, PrintPubReferenceSourceType.TAXON_SEC);

        dto.secReferenceCitation = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(reference,
                secSource.getCitationMicroReference(), null, null);
    }

    private void extractSynonymGroups(PrintPubExportState state, Taxon taxon, PrintPubTaxonSummaryDTO taxonDto) {

        HomotypicalGroup acceptedGroup = taxon.getHomotypicGroup();

        //homotypic synonyms
        List<Synonym> homotypicSynonyms = taxon.getSynonymsInGroup(acceptedGroup);

        if (homotypicSynonyms != null && !homotypicSynonyms.isEmpty()) {

            PrintPubSynonymGroupDTO homotypicGroupDTO = new PrintPubSynonymGroupDTO();

            for (Synonym synonym : homotypicSynonyms) {
                PrintPubSynonymDTO synonymDTO = createSynonymDTO(state, synonym);

                if (synonymDTO != null) {
                    homotypicGroupDTO.synonyms.add(synonymDTO);
                }
            }

            taxonDto.homotypicSynonymGroup = homotypicGroupDTO;
        }

        //heterotypic synonyms
        List<HomotypicalGroup> heterotypicGroups = taxon.getHeterotypicSynonymyGroups();

        if (heterotypicGroups == null) {
            return;
        }

        for (HomotypicalGroup group : heterotypicGroups) {

            List<Synonym> groupSynonyms = taxon.getSynonymsInGroup(group);

            if (groupSynonyms == null || groupSynonyms.isEmpty()) {
                continue;
            }

            PrintPubSynonymGroupDTO heterotypicGroupDTO = new PrintPubSynonymGroupDTO();

            for (Synonym synonym : groupSynonyms) {
                PrintPubSynonymDTO synonymDTO = createSynonymDTO(state, synonym);

                if (synonymDTO != null) {
                    heterotypicGroupDTO.synonyms.add(synonymDTO);
                }
            }

            taxonDto.synonymGroups.add(heterotypicGroupDTO);
        }
    }

    private PrintPubSynonymDTO createSynonymDTO(PrintPubExportState state, Synonym synonym) {

        synonym = CdmBase.deproxy(synonym);

        if (synonym == null) {
            return null;
        }

        PrintPubSynonymDTO synDto = new PrintPubSynonymDTO();

        TaxonName name = HibernateProxyHelper.deproxy(synonym.getName());

        if (name != null) {
            synDto.taggedNameList = name.getTaggedFullTitle();

            synDto.scientificName = TaggedTextFormatter.createString(name.getTaggedName());

            synDto.titleCache = name.getTitleCache();

            synDto.isInvalidDesignation = name.getStatus().stream().map(NomenclaturalStatus::getType)
                    .filter(type -> type != null).anyMatch(type -> type.isInvalid());

            bibliographyCollector.collectSynonymNameSources(state, name);

            PrintPubTaxonSummaryDTO typeData = new PrintPubTaxonSummaryDTO();

            extractTypeData(name, typeData, state.getConfig());

            synDto.typeSpecimenString = typeData.typeSpecimenString;

            synDto.typeStatementString = typeData.typeStatementString;
        } else {
            synDto.titleCache = synonym.getTitleCache();
        }

        extractSynonymSecReference(state, synonym, synDto);

        return synDto;
    }

    private void extractSynonymSecReference(PrintPubExportState state, Synonym synonym, PrintPubSynonymDTO synDto) {

        if (!state.getConfig().isIncludeSynonymConceptReference()) {
            return;
        }

        if (synonym.getSec() == null) {
            return;
        }

        Reference reference = HibernateProxyHelper.deproxy(synonym.getSec());

        state.addReference(reference, PrintPubReferenceSourceType.SYNONYM_SEC);

        synDto.secReference = reference.getTitleCache();
    }

    private void extractIdentifiers(Taxon taxon, PrintPubTaxonSummaryDTO dto) {

        if (taxon == null) {
            return;
        }

        TaxonName name = HibernateProxyHelper.deproxy(taxon.getName());

        if (name == null) {
            return;
        }

        addIdentifierStrings(dto.wfoIds, name, IdentifierType.IDENTIFIER_NAME_WFO());
        addIdentifierStrings(dto.ipniIds, name, IdentifierType.IDENTIFIER_NAME_IPNI());

        /*
         * Do not add nomenclatural-source links directly to name.getLinks(). That
         * collection may be Hibernate-managed and represents persistent domain state.
         */
        Set<ExternalLink> allLinks = new HashSet<>();

        if (name.getLinks() != null) {
            allLinks.addAll(name.getLinks());
        }

        NomenclaturalSource nomenclaturalSource = name.getNomenclaturalSource();

        if (nomenclaturalSource != null && nomenclaturalSource.getLinks() != null) {

            allLinks.addAll(nomenclaturalSource.getLinks());
        }

        for (ExternalLink externalLink : allLinks) {

            ExternalLink link = HibernateProxyHelper.deproxy(externalLink);

            if (link == null || link.getUri() == null) {
                continue;
            }

            String uri = link.getUri().toString();

            if (uri != null && !uri.isBlank()) {
                dto.links.add(uri.trim());
            }
        }
    }

    private void addIdentifierStrings(List<String> target, TaxonName name, IdentifierType requestedType) {

        if (target == null || name == null || requestedType == null || name.getIdentifiers() == null) {
            return;
        }

        for (Identifier identifier : name.getIdentifiers()) {

            if (identifier == null || identifier.getType() == null) {
                continue;
            }

            if (!requestedType.equals(identifier.getType())) {
                continue;
            }

            String value = identifier.getIdentifier();

            if (value != null && !value.isBlank()) {
                target.add(value.trim());
            }
        }
    }

    private void extractTypeData(TaxonName name, PrintPubTaxonSummaryDTO taxonDto, PrintPubExportConfigurator config) {

        if (name == null || taxonDto == null || config == null) {
            return;
        }

        boolean isSupraspecific = name.isSupraSpecific();

        if (isSupraspecific && !config.isIncludeSupraspecificTypes()) {
            return;
        }

        if (!isSupraspecific && !config.isIncludeSpeciesTypes()) {
            return;
        }

        Set<TypeDesignationBase> designations = name.getTypeDesignations();

        if (designations == null || designations.isEmpty()) {
            return;
        }

        List<SpecimenTypeDesignation> specimenTypes = new ArrayList<>();

        List<TextualTypeDesignation> textualTypes = new ArrayList<>();

        for (TypeDesignationBase<?> designation : designations) {

            designation = CdmBase.deproxy(designation);

            if (designation instanceof SpecimenTypeDesignation) {
                specimenTypes.add((SpecimenTypeDesignation) designation);
            } else if (designation instanceof TextualTypeDesignation) {
                textualTypes.add((TextualTypeDesignation) designation);
            }
        }

        if (!specimenTypes.isEmpty()) {
            try {
                TypeDesignationGroupContainer container = new TypeDesignationGroupContainer(specimenTypes, name, null);

                List<TaggedText> types = new TypeDesignationGroupContainerFormatter().withStartingTypeLabel(true)
                        .toTaggedText(container);

                String formattedTypes = createTypeDesignationString(types);

                taxonDto.typeSpecimenString = addOptionalTypeLineBreak(formattedTypes, isSupraspecific, config);

            } catch (Exception exception) {
                taxonDto.typeSpecimenString = "Error retrieving type data: " + exception.getMessage();
            }
        }

        if (!textualTypes.isEmpty()) {
            String statement = textualTypes.stream().map(type -> type.getPreferredText(Language.DEFAULT()))
                    .filter(text -> text != null && !text.isBlank()).collect(Collectors.joining("; "));

            if (!statement.isBlank()) {
                taxonDto.typeStatementString = addOptionalTypeLineBreak(statement, isSupraspecific, config);
            }
        }
    }

    private String addOptionalTypeLineBreak(String value, boolean isSupraspecific, PrintPubExportConfigurator config) {

        if (value == null || value.isBlank()) {
            return value;
        }

        if (isSupraspecific && config.isStartSupraspecificTypesOnNewLine()) {
            return "\n" + value;
        }

        return value;
    }

    private String createTypeDesignationString(List<TaggedText> taggedText) {

        if (taggedText == null || taggedText.isEmpty()) {
            return null;
        }

        HTMLTagRules rules = new HTMLTagRules();

        rules.addRule(TagEnum.name, "i");

        return TaggedTextFormatter.createString(taggedText, rules);
    }

    private void extractDescriptionData(PrintPubExportState state, Taxon taxon, PrintPubTaxonSummaryDTO dto) {

        int factSequence = 0;

        for (TaxonDescription description : taxon.getDescriptions()) {

            if (!state.getConfig().isIncludeUnpublishedFacts() && !description.isPublish()) {
                continue;
            }

            Set<IdentifiableSource> descriptionSources = description.getSources();

            for (DescriptionElementBase baseElement : description.getElements()) {

                DescriptionElementBase element = CdmBase.deproxy(baseElement);

                if (element == null) {
                    continue;
                }

                Feature feature = element.getFeature();

                if (Feature.COMMON_NAME().equals(feature) && element instanceof CommonTaxonName) {

                    addCommonName(dto, (CommonTaxonName) element);

                    continue;
                }

                if (Feature.DISTRIBUTION().equals(feature) && element instanceof Distribution) {

                    addDistribution(dto, (Distribution) element);

                    continue;
                }

                if (!(element instanceof TextData)) {
                    continue;
                }

                String text = ((TextData) element).getText(Language.DEFAULT());

                if (text == null) {
                    continue;
                }

                PrintPubFactDTO fact = createTextFact(element, feature, text, factSequence++);

                addElementCitations(state, element, fact);

                addDatasetCitations(state, descriptionSources, fact);

                dto.facts.add(fact);
            }
        }
    }

    private void addCommonName(PrintPubTaxonSummaryDTO dto, CommonTaxonName commonName) {

        String value = commonName.getName();

        if (value == null || value.isBlank()) {
            return;
        }

        if (commonName.getLanguage() != null) {
            value += " [" + commonName.getLanguage().getLabel() + "]";
        }

        dto.commonNames.add(value);
    }

    private void addDistribution(PrintPubTaxonSummaryDTO dto, Distribution distribution) {

        if (distribution.getArea() == null) {
            return;
        }

        String area = distribution.getArea().getLabel();

        if (area == null || area.isBlank()) {
            return;
        }

        if (dto.distributionString == null || dto.distributionString.isBlank()) {

            dto.distributionString = area;
        } else {
            dto.distributionString += ", " + area;
        }
    }

    private PrintPubFactDTO createTextFact(DescriptionElementBase element, Feature feature, String text, int sequence) {

        PrintPubFactDTO fact = new PrintPubFactDTO();

        if (feature != null) {
            fact.label = feature.getLabel();

            fact.featureUuid = feature.getUuid();
        } else {
            fact.label = "Fact";
            fact.featureUuid = null;
        }

        fact.text = text;
        fact.kind = PrintPubFactKind.TEXT_DATA;
        fact.sortIndex = element.getSortIndex();
        fact.elementId = element.getId();
        fact.sequence = sequence;

        return fact;
    }

    private void addElementCitations(PrintPubExportState state, DescriptionElementBase element, PrintPubFactDTO fact) {

        if (element.getSources() == null) {
            return;
        }

        for (DescriptionElementSource source : element.getSources()) {

            if (source == null || source.getCitation() == null || source.getType() == null
                    || source.getType().isPrimarySource()) {
                continue;
            }

            Reference reference = HibernateProxyHelper.deproxy(source.getCitation());

            state.addReference(reference, PrintPubReferenceSourceType.TAXON_FACT_SOURCE);

            fact.citations.add(OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(reference, null));
        }
    }

    private void addDatasetCitations(PrintPubExportState state, Set<IdentifiableSource> descriptionSources,
            PrintPubFactDTO fact) {

        if (descriptionSources == null) {
            return;
        }

        for (IdentifiableSource source : descriptionSources) {

            if (source == null || source.getCitation() == null || source.getType() == null
                    || !source.getType().isPrimarySource()) {
                continue;
            }

            Reference reference = HibernateProxyHelper.deproxy(source.getCitation());

            state.addReference(reference, PrintPubReferenceSourceType.FACT_DATASET_SOURCE);

            fact.citations.add(OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(reference, null));
        }
    }

    public int calculateDepth(TaxonNode node) {

        String treeIndex = node.treeIndex();

        if (treeIndex != null && !treeIndex.isEmpty()) {

            String[] segments = treeIndex.split("#");

            int depth = 0;

            for (String segment : segments) {
                if (!segment.isEmpty()) {
                    depth++;
                }
            }

            return depth;
        }

        int depth = 1;

        TaxonNode parent = node.getParent();

        while (parent != null) {
            depth++;
            parent = parent.getParent();
        }

        return depth;
    }
}