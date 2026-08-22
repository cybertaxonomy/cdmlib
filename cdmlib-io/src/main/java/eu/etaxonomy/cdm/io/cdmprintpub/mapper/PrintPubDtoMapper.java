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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import eu.etaxonomy.cdm.model.name.Rank;
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
 * Mapper class converting CDM domain objects into Print/Publication DTOs.
 *
 * Extracts names, types, synonyms, descriptions, and references based on export
 * configuration and registers shared references in the export state.
 */
@Component
public class PrintPubDtoMapper {

    public PrintPubTaxonSummaryDTO mapNodeToDto(TaxonNode node, int referenceDepth, PrintPubExportState state) {

        if (node == null || node.getTaxon() == null) {
            return null;
        }

        Taxon taxon = HibernateProxyHelper.deproxy(node.getTaxon());

        PrintPubTaxonSummaryDTO dto = new PrintPubTaxonSummaryDTO();
        dto.uuid = taxon.getUuid();
        dto.relativeDepth = calculateDepth(node) - referenceDepth;

        TaxonName name = HibernateProxyHelper.deproxy(taxon.getName());

        if (name != null) {
            dto.taggedNameList = name.getTaggedFullTitle();
            dto.taggedScientificIndexNameList = name.getTaggedName();
            dto.titleCache = name.getTitleCache();
        } else {
            dto.titleCache = taxon.getTitleCache();
        }

        if (name != null) {
            extractTypeData(name, dto, state.getConfig());
        }

        if (state.getConfig().isDoSynonyms()) {
            extractSynonymGroups(state, taxon, dto);
        }

        if (state.getConfig().isDoFactualData()) {
            extractDescriptionData(state, taxon, dto);
        }

        if (state.getConfig().isIncludeTaxonomicConceptReference() && taxon.getSec() != null) {

            Reference ref = HibernateProxyHelper.deproxy(taxon.getSec());
            SecundumSource secSource = taxon.getSecSource();

            if (secSource != null && secSource.getType().isPrimarySource()) {

                state.addReference(ref, PrintPubReferenceSourceType.TAXON_SEC);

                dto.secReferenceCitation = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(ref,
                        secSource.getCitationMicroReference(), null, null);
            }
        }

        extractIdentifiers(state, taxon, dto);

        return dto;
    }

    private void extractSynonymGroups(PrintPubExportState state, Taxon taxon, PrintPubTaxonSummaryDTO dto) {

        HomotypicalGroup acceptedGroup = taxon.getHomotypicGroup();
        List<Synonym> homotypicSynonyms = taxon.getSynonymsInGroup(acceptedGroup);

        filterMisapplied(homotypicSynonyms, state.getConfig().isIncludeMisappliedNames());

        if (!homotypicSynonyms.isEmpty()) {
            PrintPubSynonymGroupDTO homotypicGroupDTO = new PrintPubSynonymGroupDTO();
            homotypicGroupDTO.isHomotypic = true;
            for (Synonym syn : homotypicSynonyms) {
                homotypicGroupDTO.synonyms.add(createSynonymDTO(state, syn));
            }
            dto.synonymGroups.add(homotypicGroupDTO);
        }

        List<HomotypicalGroup> heteroGroups = taxon.getHeterotypicSynonymyGroups();
        for (HomotypicalGroup group : heteroGroups) {
            List<Synonym> groupSynonyms = taxon.getSynonymsInGroup(group);

            filterMisapplied(groupSynonyms, state.getConfig().isIncludeMisappliedNames());

            if (!groupSynonyms.isEmpty()) {
                PrintPubSynonymGroupDTO heteroGroupDTO = new PrintPubSynonymGroupDTO();
                heteroGroupDTO.isHomotypic = false;
                for (Synonym syn : groupSynonyms) {
                    heteroGroupDTO.synonyms.add(createSynonymDTO(state, syn));
                }
                dto.synonymGroups.add(heteroGroupDTO);
            }
        }
    }

    private void extractIdentifiers(PrintPubExportState state, Taxon taxon, PrintPubTaxonSummaryDTO dto) {

        if (taxon == null) {
            return;
        }

        TaxonName name = HibernateProxyHelper.deproxy(taxon.getName());

        if (name == null) {
            return;
        }

        addIdentifierStrings(dto.wfoIds, name.getIdentifierStrings(IdentifierType.IDENTIFIER_NAME_WFO()));
        addIdentifierStrings(dto.ipniIds, name.getIdentifierStrings(IdentifierType.IDENTIFIER_NAME_IPNI()));

        Set<ExternalLink> allLinks = name.getLinks();

        NomenclaturalSource nameSource = name.getNomenclaturalSource();

        if (nameSource != null) {
            Set<ExternalLink> nomenclaturalSourceLinks = nameSource.getLinks();
            allLinks.addAll(nomenclaturalSourceLinks);
        }

        if (allLinks != null) {
            for (ExternalLink link : allLinks) {

                link = HibernateProxyHelper.deproxy(link);

                if (link == null || link.getUri() == null) {
                    continue;
                }

                String uri = link.getUri().toString();

                if (uri != null && !uri.trim().isEmpty()) {
                    dto.links.add(uri.trim());
                }
            }
        }
    }

    private void addIdentifierStrings(List<String> target, Set<String> values) {

        if (target == null || values == null || values.isEmpty()) {
            return;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                target.add(value.trim());
            }
        }
    }

    private void filterMisapplied(List<Synonym> synonyms, boolean includeMisapplied) {
        if (includeMisapplied) {
            return;
        }
        synonyms.removeIf(syn -> syn.getType() == null);
    }

    private PrintPubSynonymDTO createSynonymDTO(PrintPubExportState state, Synonym syn) {

        syn = CdmBase.deproxy(syn);
        PrintPubSynonymDTO synDTO = new PrintPubSynonymDTO();

        TaxonName synName = HibernateProxyHelper.deproxy(syn.getName());

        if (synName != null) {
            synDTO.taggedNameList = synName.getTaggedFullTitle();
            synDTO.titleCache = synName.getTitleCache();
        } else {
            synDTO.titleCache = syn.getTitleCache();
        }

        if (synName != null) {
            synDTO.forceDashMarker = synName.getStatus().stream().map(NomenclaturalStatus::getType)
                    .filter(statusType -> statusType != null).anyMatch(statusType -> statusType.isInvalid());
        }

        if (state.getConfig().isIncludeSynonymConceptReference() && syn.getSec() != null) {
            Reference ref = HibernateProxyHelper.deproxy(syn.getSec());
            state.addReference(ref, PrintPubReferenceSourceType.SYNONYM_SEC);
            synDTO.secReference = ref.getTitleCache();
        }

        if (synName != null) {
            PrintPubTaxonSummaryDTO tmp = new PrintPubTaxonSummaryDTO();
            extractTypeData(synName, tmp, state.getConfig());
            synDTO.typeSpecimenString = tmp.typeSpecimenString;
            synDTO.typeStatementString = tmp.typeStatementString;
        }

        return synDTO;
    }

    private void extractTypeData(TaxonName name, PrintPubTaxonSummaryDTO dto, PrintPubExportConfigurator config) {

        Rank rank = name.getRank();
        boolean isSupraspecific = (rank != null && rank.compareTo(Rank.SPECIES()) > 0);

        if (isSupraspecific && !config.isIncludeSupraspecificTypes()) {
            return;
        }
        if (!isSupraspecific && !config.isIncludeSpeciesTypes()) {
            return;
        }

        Set<TypeDesignationBase> designations = name.getTypeDesignations();
        List<SpecimenTypeDesignation> specimenTypes = new ArrayList<>();
        List<TextualTypeDesignation> textualTypes = new ArrayList<>();

        for (TypeDesignationBase<?> design : designations) {
            if (design instanceof SpecimenTypeDesignation) {
                specimenTypes.add((SpecimenTypeDesignation) design);
            } else if (design instanceof TextualTypeDesignation) {
                textualTypes.add((TextualTypeDesignation) design);
            }
        }

        if (!specimenTypes.isEmpty()) {
            try {
                TypeDesignationGroupContainer container = new TypeDesignationGroupContainer(specimenTypes, name, null);
                List<TaggedText> types = new TypeDesignationGroupContainerFormatter().withStartingTypeLabel(true)
                        .toTaggedText(container);
                String formattedTypesString = createTypeDesignationString(types);

                if (isSupraspecific && config.isStartSupraspecificTypesOnNewLine()) {
                    dto.typeSpecimenString = "\n" + formattedTypesString;
                } else {
                    dto.typeSpecimenString = formattedTypesString;
                }

            } catch (Exception e) {
                dto.typeSpecimenString = "Error retrieving type data: " + e.getMessage();
            }
        }

        if (!textualTypes.isEmpty()) {
            String statement = textualTypes.stream().map(t -> t.getPreferredText(Language.DEFAULT()))
                    .collect(Collectors.joining("; "));

            if (isSupraspecific && config.isStartSupraspecificTypesOnNewLine()) {
                dto.typeStatementString = "\n" + statement;
            } else {
                dto.typeStatementString = statement;
            }
        }
    }

    private String createTypeDesignationString(List<TaggedText> list) {
        HTMLTagRules rules = new HTMLTagRules();
        rules.addRule(TagEnum.name, "i");
        String typeDesignations = TaggedTextFormatter.createString(list, rules);
        return typeDesignations;
    }

    private void extractDescriptionData(PrintPubExportState state, Taxon taxon, PrintPubTaxonSummaryDTO dto) {
        int factSequence = 0;

        for (TaxonDescription desc : taxon.getDescriptions()) {

            if (!state.getConfig().isIncludeUnpublishedFacts() && !desc.isPublish()) {
                continue;
            }

            Set<IdentifiableSource> descSources = desc.getSources();

            for (DescriptionElementBase element : desc.getElements()) {

                element = CdmBase.deproxy(element);
                Feature feature = element.getFeature();

                // ---- Common names ----
                if (Feature.COMMON_NAME().equals(feature) && element instanceof CommonTaxonName) {

                    CommonTaxonName ctn = (CommonTaxonName) element;
                    dto.commonNames.add(ctn.getName()
                            + (ctn.getLanguage() != null ? " [" + ctn.getLanguage().getLabel() + "]" : ""));
                    continue;
                }

                // ---- Distribution ----
                if (Feature.DISTRIBUTION().equals(feature) && element instanceof Distribution) {

                    Distribution d = (Distribution) element;
                    if (d.getArea() != null) {
                        dto.distributionString = (dto.distributionString == null) ? d.getArea().getLabel()
                                : dto.distributionString + ", " + d.getArea().getLabel();
                    }
                    continue;
                }

                // ---- Text facts ----
                if (element instanceof TextData) {

                    String text = ((TextData) element).getText(Language.DEFAULT());
                    if (text == null) {
                        continue;
                    }

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
                    fact.sequence = factSequence++;

                    for (DescriptionElementSource source : element.getSources()) {
                        if (source.getCitation() != null && !source.getType().isPrimarySource()) {
                            Reference ref = HibernateProxyHelper.deproxy(source.getCitation());
                            state.addReference(ref, PrintPubReferenceSourceType.TAXON_FACT_SOURCE);
                            String shortCit = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(ref, null);
                            fact.citations.add(shortCit);
                        }
                    }

                    for (IdentifiableSource identifiableSource : descSources) {
                        if (identifiableSource == null || identifiableSource.getCitation() == null
                                || !identifiableSource.getType().isPrimarySource()) {
                            continue;
                        }

                        Reference ref = HibernateProxyHelper.deproxy(identifiableSource.getCitation());

                        // Add enum first, see PrintPubReferenceEntryDTO section.
                        state.addReference(ref, PrintPubReferenceSourceType.FACT_DATASET_SOURCE);

                        String shortCit = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS.format(ref, null);
                        fact.citations.add(shortCit);
                    }

                    dto.facts.add(fact);
                }
            }
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