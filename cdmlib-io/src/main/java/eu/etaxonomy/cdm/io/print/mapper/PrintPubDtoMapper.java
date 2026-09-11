/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.print.mapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainer;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainerFormatter;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.print.PrintPubExportConfigurator;
import eu.etaxonomy.cdm.io.print.PrintPubExportConfigurator.FeatureSortMode;
import eu.etaxonomy.cdm.io.print.PrintPubExportState;
import eu.etaxonomy.cdm.io.print.compare.IPrintPubFactOrderStrategy;
import eu.etaxonomy.cdm.io.print.compare.IPrintPubFeatureOrderStrategy;
import eu.etaxonomy.cdm.io.print.compare.PrintPubFactOrderStrategyResolver;
import eu.etaxonomy.cdm.io.print.compare.PrintPubFeatureKey;
import eu.etaxonomy.cdm.io.print.compare.PrintPubFeatureOrderStrategyResolver;
import eu.etaxonomy.cdm.io.print.dto.PrintPubFactDTO;
import eu.etaxonomy.cdm.io.print.dto.PrintPubFactDTO.PrintPubFactKind;
import eu.etaxonomy.cdm.io.print.dto.PrintPubNameDTO;
import eu.etaxonomy.cdm.io.print.dto.PrintPubReferenceEntryDTO.PrintPubReferenceSourceType;
import eu.etaxonomy.cdm.io.print.dto.PrintPubSynonymDTO;
import eu.etaxonomy.cdm.io.print.dto.PrintPubSynonymGroupDTO;
import eu.etaxonomy.cdm.io.print.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
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
import eu.etaxonomy.cdm.model.name.TaxonName;
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

    @Autowired
    private PrintPubFeatureOrderStrategyResolver featureOrderResolver;

    @Autowired
    private PrintPubFactOrderStrategyResolver factOrderResolver;

    private final PrintPubBibliographyCollector bibliographyCollector;

    public PrintPubDtoMapper(PrintPubBibliographyCollector bibliographyCollector) {

        this.bibliographyCollector = bibliographyCollector;
    }

    public PrintPubTaxonSummaryDTO mapNodeToDto(TaxonNode node, int referenceDepth, PrintPubExportState state) {

        if (node == null || node.getTaxon() == null) {
            return null;
        }

        Taxon taxon = node.getTaxon();

        PrintPubTaxonSummaryDTO taxonDto = new PrintPubTaxonSummaryDTO();

        taxonDto.uuid = taxon.getUuid();
        taxonDto.relativeDepth = calculateDepth(node) - referenceDepth;

        TaxonName name = HibernateProxyHelper.deproxy(taxon.getName());

        mapAcceptedName(taxon, name, taxonDto);

        if (name != null) {

            bibliographyCollector.collectAcceptedNameSources(state, name);
        }

        if (state.getConfig().isDoSynonyms()) {
            extractSynonymGroups(state, taxon, taxonDto);
        } else if (includeAnyTypes(state.getConfig())){
            //handle accepted name types
            taxonDto.homotypicSynonymGroup = new PrintPubSynonymGroupDTO();
            extractTypes(state, taxon.getName().getHomotypicalGroup(), taxonDto.homotypicSynonymGroup);
        }

        if (state.getConfig().isDoFactualData()) {
            extractDescriptionData(state, taxon, taxonDto);
            sortAndFilterFacts(state, taxonDto);
        }

        extractTaxonSecReference(state, taxon, taxonDto);

        extractIdentifiers(taxon.getName(), taxonDto.nameDTO);

        return taxonDto;
    }

    private boolean includeAnyTypes(PrintPubExportConfigurator config) {
        return config.isIncludeSpeciesTypes() || config.isIncludeSupraspecificTypes();
    }

    private void sortAndFilterFacts(PrintPubExportState state, PrintPubTaxonSummaryDTO taxonDto) {

        if (taxonDto.facts == null || taxonDto.facts.isEmpty()) {
            return;
        }

        PrintPubExportConfigurator config = state.getConfig();

        final Map<UUID, Integer> featureOrderIndex = state.getFeatureOrderIndex() == null ? Map.of()
                : state.getFeatureOrderIndex();

        boolean useFeatureTree = config.getFeatureSortMode() == FeatureSortMode.FEATURE_TREE
                && !featureOrderIndex.isEmpty();

        if (useFeatureTree) {
            taxonDto.facts.removeIf(fact -> fact == null || fact.featureUuid == null
                    || !featureOrderIndex.containsKey(fact.featureUuid));
        } else {
            taxonDto.facts.removeIf(Objects::isNull);
        }

        IPrintPubFeatureOrderStrategy featureOrderStrategy = featureOrderResolver.resolve(config.getFeatureSortMode(),
                featureOrderIndex);

        IPrintPubFactOrderStrategy factOrderStrategy = factOrderResolver.resolve(config.getFactSortMode());

        Comparator<PrintPubFeatureKey> featureComparator = featureOrderStrategy.comparator(featureOrderIndex);

        Comparator<PrintPubFactDTO> factComparator = factOrderStrategy.comparator();

        taxonDto.facts.sort((fact1, fact2) -> {

            PrintPubFeatureKey key1 = createFeatureKey(fact1);
            PrintPubFeatureKey key2 = createFeatureKey(fact2);

            int featureComparison = featureComparator.compare(key1, key2);

            if (featureComparison != 0) {
                return featureComparison;
            }

            return factComparator.compare(fact1, fact2);
        });
    }

    private PrintPubFeatureKey createFeatureKey(PrintPubFactDTO fact) {
        return new PrintPubFeatureKey(fact.featureUuid, fact.label);
    }

    private String normalizeFactLabel(String label) {
        return label == null ? null : label.replaceFirst("^<[^>]+>", "").trim();
    }

    private void mapAcceptedName(Taxon taxon, TaxonName name, PrintPubTaxonSummaryDTO taxonDto) {

        if (name != null) {
            taxonDto.nameDTO.uuid = name.getUuid();

            taxonDto.nameDTO.taggedNameList = name.getTaggedFullTitle();

            taxonDto.nameDTO.scientificName = TaggedTextFormatter.createString(name.getTaggedName());

            taxonDto.titleCache = name.getTitleCache();
        } else {
            taxonDto.titleCache = taxon.getTitleCache();
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

        // homotypic synonyms
        HomotypicalGroup acceptedGroup = taxon.getHomotypicGroup();
        List<Synonym> homotypicSynonyms = taxon.getSynonymsInGroup(acceptedGroup);

        PrintPubSynonymGroupDTO homotypicGroupDTO = new PrintPubSynonymGroupDTO();
        taxonDto.homotypicSynonymGroup = homotypicGroupDTO;

        extractSynonymGroup(state, acceptedGroup, homotypicSynonyms, homotypicGroupDTO);

        // heterotypic synonyms
        List<HomotypicalGroup> heterotypicGroups = taxon.getHeterotypicSynonymyGroups();

        for (HomotypicalGroup group : heterotypicGroups) {

            List<Synonym> groupSynonyms = taxon.getSynonymsInGroup(group);

            PrintPubSynonymGroupDTO heterotypicGroupDTO = new PrintPubSynonymGroupDTO();
            taxonDto.heterotypicSynonymGroups.add(heterotypicGroupDTO);

            extractSynonymGroup(state, group, groupSynonyms, heterotypicGroupDTO);
        }
    }

    private void extractSynonymGroup(PrintPubExportState state, HomotypicalGroup homotypicGroup,
            List<Synonym> synonyms, PrintPubSynonymGroupDTO homotypicGroupDTO) {

        //synonyms
        for (Synonym synonym : synonyms) {
            PrintPubSynonymDTO synonymDTO = createSynonymDTO(state, synonym);
            homotypicGroupDTO.synonyms.add(synonymDTO);
        }

        //types
        extractTypes(state, homotypicGroup, homotypicGroupDTO);
    }

    private void extractTypes(PrintPubExportState state, HomotypicalGroup homotypicGroup,
            PrintPubSynonymGroupDTO homotypicGroupDTO) {

        TypeDesignationGroupContainer container = new TypeDesignationGroupContainer(homotypicGroup);
        List<TaggedText> types = new TypeDesignationGroupContainerFormatter().withStartingTypeLabel(true)
                .toTaggedText(container);
        String formattedTypes = createTypeDesignationString(types);

        boolean isSupraspecific = false; //TODO
        homotypicGroupDTO.typeSpecimenString = addOptionalTypeLineBreak(formattedTypes, isSupraspecific, state.getConfig());
    }

    private PrintPubSynonymDTO createSynonymDTO(PrintPubExportState state, Synonym synonym) {

        PrintPubSynonymDTO synDto = new PrintPubSynonymDTO();

        TaxonName name = HibernateProxyHelper.deproxy(synonym.getName());

        if (name != null) {

            synDto.nameDTO.uuid = name.getUuid();
            synDto.nameDTO.taggedNameList = name.getTaggedFullTitle();

            synDto.nameDTO.scientificName = TaggedTextFormatter.createString(name.getTaggedName());

            synDto.titleCache = name.getTitleCache();

            synDto.isInvalidDesignation = name.getStatus().stream().map(NomenclaturalStatus::getType)
                    .filter(type -> type != null).anyMatch(type -> type.isInvalid());

            bibliographyCollector.collectSynonymNameSources(state, name);

        } else {
            synDto.titleCache = synonym.getTitleCache();
        }

        extractSynonymSecReference(state, synonym, synDto);

        extractIdentifiers(synonym.getName(), synDto.nameDTO);

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

    private void extractIdentifiers(TaxonName name, PrintPubNameDTO nameDto) {

        if (name == null) {
            return;
        }

        addIdentifierStrings(nameDto.wfoIds, name, IdentifierType.IDENTIFIER_NAME_WFO());
        addIdentifierStrings(nameDto.ipniIds, name, IdentifierType.IDENTIFIER_NAME_IPNI());

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
                nameDto.links.add(uri.trim());
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

    private void extractDescriptionData(PrintPubExportState state, Taxon taxon, PrintPubTaxonSummaryDTO taxonDto) {

        int factSequence = 0;
        List<String> distributions = new ArrayList<>();

        for (TaxonDescription description : taxon.getDescriptions()) {

            if (!state.getConfig().isIncludeUnpublishedFacts() && !description.isPublish()) {
                continue;
            }

            Set<IdentifiableSource> descriptionSources = description.getSources();

            for (DescriptionElementBase fact : description.getElements()) {

                fact = CdmBase.deproxy(fact);
                Feature feature = fact.getFeature();

                if (Feature.COMMON_NAME().equals(feature) && fact instanceof CommonTaxonName) {

                    addCommonName(taxonDto, (CommonTaxonName) fact);

                } else if (Feature.DISTRIBUTION().equals(feature) && fact instanceof Distribution) {

                    addDistribution(distributions, (Distribution) fact);

                } else if (fact instanceof TextData) {

                    LanguageString textLs = ((TextData) fact).getPreferredLanguageString(Language.DEFAULT());

                    if (textLs == null || StringUtils.isBlank(textLs.getText())) {
                        continue;
                    }

                    PrintPubFactDTO factDto = createTextFact((TextData)fact, feature, textLs.getText(), factSequence++);

                    addElementCitations(state, fact, factDto);

                    addDatasetCitations(state, descriptionSources, factDto);

                    taxonDto.facts.add(factDto);
                } else {
                    // TODO Handle other types of DescriptionElementBase if needed
                    continue;
                }
            }
        }
        //sort common names
        taxonDto.commonNames.sort(String.CASE_INSENSITIVE_ORDER);
        taxonDto.commonNameString = StringUtils.join(taxonDto.commonNames, ", ");

        //distributions
        distributions.sort(String.CASE_INSENSITIVE_ORDER);
        taxonDto.distributionString = StringUtils.join(distributions, ", ");

    }

    private void addCommonName(PrintPubTaxonSummaryDTO dto, CommonTaxonName commonName) {

        String value = commonName.getName();

        if (value == null || value.isBlank()) {
            return;
        }

        List<Language> languages = List.of(Language.DEFAULT());
        if (commonName.getLanguage() != null) {
            value += " [" + commonName.getLanguage().getPreferredLabel(languages) + "]";
        }

        //preliminary implementation
        dto.commonNames.add(value);
    }

    private void addDistribution(List<String> distributions, Distribution distribution) {

        if (distribution.getArea() == null) {
            return;
        }

        List<Language> languages = List.of(Language.DEFAULT());
        String area = distribution.getArea().getPreferredLabel(languages);

        if (StringUtils.isNotBlank(area)){
            distributions.add(area);
        }
    }

    private PrintPubFactDTO createTextFact(TextData element, Feature feature, String text, int sequence) {

        PrintPubFactDTO fact = new PrintPubFactDTO();

        if (feature != null) {
            fact.label = normalizeFactLabel(feature.getLabel());
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