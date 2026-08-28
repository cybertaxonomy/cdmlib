/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cdmprintpub.documentBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportConfigurator;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportConfigurator.FeatureSortMode;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;
import eu.etaxonomy.cdm.io.cdmprintpub.compare.IPrintPubFactOrderStrategy;
import eu.etaxonomy.cdm.io.cdmprintpub.compare.IPrintPubFeatureOrderStrategy;
import eu.etaxonomy.cdm.io.cdmprintpub.compare.PrintPubFactOrderStrategyResolver;
import eu.etaxonomy.cdm.io.cdmprintpub.compare.PrintPubFeatureKey;
import eu.etaxonomy.cdm.io.cdmprintpub.compare.PrintPubFeatureOrderStrategyResolver;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubFactDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubSynonymDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubSynonymGroupDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubSectionHeaderElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubTextRunElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubTextRunElement.Run;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubTextRunElement.RunType;
import eu.etaxonomy.cdm.io.cdmprintpub.util.PrintPubNonNestedHtmlTokenConverter;
import eu.etaxonomy.cdm.io.cdmprintpub.util.PrintPubNonNestedHtmlTokenizer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * Concrete document builder for Print/Publication output.
 *
 * Renders the taxonomic hierarchy including headings, synonyms, facts, and type
 * information. Converts taxon DTOs into low-level document elements suitable
 * for subsequent rendering.
 */
@Component("printPubDocumentBuilder")
public class PrintPubDocumentBuilder implements IPrintPubDocumentBuilder {

    @Autowired
    private PrintPubFeatureOrderStrategyResolver featureOrderResolver;

    @Autowired
    private PrintPubFactOrderStrategyResolver factOrderResolver;

    private static final String SYNONYM_MARKER = UTF8.EQUALS_SIGN + " ";

    private static final String HOMOTYPIC_MARKER = UTF8.IDENTICAL_TO + " ";

    private static final String INVALID_NAME_MARKER = UTF8.EN_DASH + " ";

    private static final String ACC_SEC_MARKER = " sec. ";

    private static final String SYN_SEC_MARKER = " syn sec. ";

    protected void buildContent(PrintPubExportState state) {
        state.getProcessor().add(new PrintPubSectionHeaderElement("Taxonomic Hierarchy", 1));

        for (PrintPubTaxonSummaryDTO dto : state.getTaxa()) {
            try {
                renderTaxon(state, dto);
            } catch (Exception e) {
                state.getResult().addException(e, "An handled exception occurred while handling content.", "PrintPubDocumentBuilder.buildContent(state)", "Taxon: " + dto.titleCache);
            }
        }
    }

    private void renderTaxon(PrintPubExportState state, PrintPubTaxonSummaryDTO dto) {

        renderTaxonHeading(state, dto);

        if (dto.typeSpecimenString != null && !dto.typeSpecimenString.trim().isEmpty()) {
            state.getProcessor().add(new PrintPubParagraphElement(dto.typeSpecimenString));
        }

        if (dto.typeStatementString != null && !dto.typeStatementString.trim().isEmpty()) {
            state.getProcessor().add(new PrintPubParagraphElement(dto.typeStatementString));
        }

        if (state.getConfig().isDoSynonyms()) {
            renderSynonyms(state, dto);
        }

        renderTaxonDetails(state, dto);
    }

    private void renderTaxonHeading(PrintPubExportState state, PrintPubTaxonSummaryDTO dto) {

        List<PrintPubTextRunElement.Run> runs = new ArrayList<>(runsFromTaggedNameForTitle(dto.taggedNameList));

        if (dto.secReferenceCitation != null && !dto.secReferenceCitation.isBlank()) {

            runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT,
                    ACC_SEC_MARKER + dto.secReferenceCitation));
        }

        state.getProcessor()
                .add(new PrintPubTextRunElement(null, runs, PrintPubTextRunElement.PrintPubTextRole.TAXON_NAME));
    }

    private void renderSynonyms(PrintPubExportState state, PrintPubTaxonSummaryDTO dto) {

        boolean oneLinePerHomotypicGroup = !state.getConfig().isIncludeSynonymConceptReference();
        if (dto.homotypicSynonymGroup != null) {
            boolean first = false;  //the accepted name is always the first in group for the homotypic group
            for (PrintPubSynonymDTO syn : dto.homotypicSynonymGroup.synonyms) {
                renderSingleSynonym(state, first, syn, oneLinePerHomotypicGroup);
            }
        }

        for (PrintPubSynonymGroupDTO group : dto.synonymGroups) {

            boolean firstInGroup = true;

            for (PrintPubSynonymDTO syn : group.synonyms) {

                renderSingleSynonym(state, firstInGroup, syn, oneLinePerHomotypicGroup);
                firstInGroup = false;
            }
        }
    }

    private void renderSingleSynonym(PrintPubExportState state, boolean isFirstInGroup,
            PrintPubSynonymDTO syn, boolean oneLinePerHomotypicGroup) {

        // --- choose prefix ---
        String prefix;
        if (syn.forceDashMarker) {
            prefix = INVALID_NAME_MARKER;
        } else if (isFirstInGroup) {
            prefix = SYNONYM_MARKER;
        } else {
            prefix = HOMOTYPIC_MARKER;
        }

        // --- sec. reference suffix ---
        String suffix = "";

        if (state.getConfig().isIncludeSynonymConceptReference() && syn.secReference != null) {
            String citationSuffix = state.incrementShortCitation(syn.secReference);
            suffix = SYN_SEC_MARKER + syn.secReference + citationSuffix;
        }

        // --- name rendered from TaggedText, not titleCache ---
        boolean newLine = !isFirstInGroup && !oneLinePerHomotypicGroup;
        state.getProcessor().add(new PrintPubTextRunElement(synonymRuns(syn, prefix, suffix, newLine)));

        // --- type information ---
        if (StringUtils.isNotBlank(syn.typeSpecimenString)) {
            state.getProcessor().add(new PrintPubParagraphElement(syn.typeSpecimenString));
        }
        return;
    }

    private void renderTaxonDetails(PrintPubExportState state, PrintPubTaxonSummaryDTO dto) {

        if (dto.distributionString != null) {
            state.getProcessor().add(new PrintPubLabeledTextElement("Distribution", dto.distributionString));
        }

        if (dto.facts == null || dto.facts.isEmpty()) {
            return;
        }

        // Resolve strategies based on config (FeatureSortMode / FactSortMode)
        IPrintPubFeatureOrderStrategy featureOrder = featureOrderResolver.resolve(state);
        IPrintPubFactOrderStrategy factOrder = factOrderResolver.resolve(state);

        // Group facts by featureUuid + normalized label
        Map<PrintPubFeatureKey, List<PrintPubFactDTO>> groups = dto.facts.stream().collect(
                Collectors.groupingBy(f -> new PrintPubFeatureKey(f.featureUuid, normalizeFactLabel(f.label))));

        if (state.getConfig().getFeatureSortMode() == FeatureSortMode.FEATURE_TREE) {

            Map<UUID, Integer> index = state.getFeatureOrderIndex();

            if (index != null && !index.isEmpty()) {
                groups.entrySet().removeIf(entry -> {
                    UUID uuid = entry.getKey().getFeatureUuid();
                    return uuid == null || !index.containsKey(uuid);
                });
            }
        }

        // Sort the feature keys using chosen feature ordering
        List<PrintPubFeatureKey> keys = new ArrayList<>(groups.keySet());
        Collections.sort(keys, featureOrder.comparator(state));

        // Sort facts within each group using chosen fact ordering
        Comparator<PrintPubFactDTO> factComparator = factOrder.comparator();

        for (PrintPubFeatureKey key : keys) {

            List<PrintPubFactDTO> facts = groups.get(key);
            if (facts == null || facts.isEmpty()) {
                continue;
            }

            Collections.sort(facts, factComparator);

            List<PrintPubTextRunElement.Run> combinedRuns = new ArrayList<>();
            boolean first = true;

            for (PrintPubFactDTO fact : facts) {

                if (!first) {
                    combinedRuns.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, " "));
                }
                first = false;

                combinedRuns.addAll(
                        PrintPubNonNestedHtmlTokenConverter.toRuns(PrintPubNonNestedHtmlTokenizer.tokenize(fact.text)));

                if (fact.citations != null && !fact.citations.isEmpty()) {
                    combinedRuns.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT,
                            " [" + String.join("; ", fact.citations) + "]"));
                }
            }

            String label = key.getLabel() == null ? "Facts" : key.getLabel();
            state.getProcessor().add(new PrintPubTextRunElement(label, combinedRuns));
        }
    }

    private List<PrintPubTextRunElement.Run> runsFromTaggedName(List<TaggedText> taggedName) {

        List<PrintPubTextRunElement.Run> runs = new ArrayList<>();
        boolean first = true;

        if (taggedName == null) {
            return runs;
        }

        for (TaggedText tt : taggedName) {

            String text = tt.getText();
            if (text == null || text.isEmpty()) {
                continue;
            }

            if (!first && needsSpaceBefore(text)) {
                runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, " "));
            }
            first = false;

            if (tt.getType() == TagEnum.name) {
                runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.ITALIC, text));
            } else {
                runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, text));
            }
        }
        return runs;
    }

    private List<Run> runsFromTaggedNameForTitle(List<TaggedText> taggedName) {

        List<Run> runs = new ArrayList<>();
        boolean first = true;

        for (TaggedText tt : taggedName) {

            String text = tt.getText();
            if (text == null || text.isEmpty()) {
                continue;
            }

            if (!first && needsSpaceBefore(text)) {
                runs.add(new Run(RunType.TEXT, " "));
            }
            first = false;

            RunType type;

            if (tt.getType() == TagEnum.name) {
                type = RunType.BOLD_ITALIC;
            } else {
                type = RunType.BOLD;
            }

            runs.add(new Run(type, text));
        }

        return runs;
    }

    private List<PrintPubTextRunElement.Run> synonymRuns(PrintPubSynonymDTO syn, String prefix, String suffix, boolean newLine) {

        List<PrintPubTextRunElement.Run> runs = new ArrayList<>();

        if (newLine) {
            runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.LINE_BREAK, ""));
        }else {
            runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, " "));
        }

        runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, prefix));

        List<PrintPubTextRunElement.Run> nameRuns = runsFromTaggedName(syn.taggedNameList);

        if (!nameRuns.isEmpty()) {
            runs.addAll(nameRuns);
        } else if (syn.titleCache != null && !syn.titleCache.trim().isEmpty()) {
            runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, syn.titleCache.trim()));
        }

        if (suffix != null && !suffix.isEmpty()) {
            runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, suffix));
        }

        return runs;
    }

    private boolean needsSpaceBefore(String text) {
        return !text.startsWith(",") && !text.startsWith(";") && !text.startsWith(")");
    }

    private String normalizeFactLabel(String label) {
        if (label == null) {
            return null;
        }
        // Remove leading <Category> if present
        return label.replaceFirst("^<[^>]+>", "").trim();
    }

    @Override
    public void buildLayout(PrintPubExportState state) {

        buildHeader(state);

        buildContent(state);

        if (!state.getSortedBibliography().isEmpty()) {
            buildBibliography(state);
        }

        if (state.getConfig().isGenerateScientificNameIndex()) {
            buildScientificNameIndex(state);
        }

        if (state.getConfig().isGenerateCommonNameIndex()) {
            buildCommonNameIndex(state);
        }

        if (state.getConfig().isAppendIdentifierList()) {
            buildAppendix(state);
        }
    }

    protected void buildHeader(PrintPubExportState state) {
        state.getProcessor().add(new PrintPubSectionHeaderElement(state.getConfig().getDocumentTitle(), 1));
        state.getProcessor().add(new PrintPubParagraphElement("Total Taxa: " + state.getTaxa().size()));
        state.getProcessor().add(new PrintPubPageBreakElement());
    }

    protected void buildBibliography(PrintPubExportState state) {
        List<Reference> bibliography = state.getSortedBibliography();
        if (bibliography.isEmpty()) {
            return;
        }

        state.getProcessor().add(new PrintPubPageBreakElement());
        state.getProcessor().add(new PrintPubSectionHeaderElement("Bibliography", 1));

        for (Reference ref : bibliography) {
            state.getProcessor().add(new PrintPubParagraphElement(ref.getTitleCache()));
        }
    }

    protected void buildScientificNameIndex(PrintPubExportState state) {

        state.getProcessor().add(new PrintPubPageBreakElement());
        state.getProcessor().add(
                new PrintPubSectionHeaderElement("Index to Scientific Names", 1));

        // To do, put together scientific names with back-references to origin in checklist
        state.getTaxa().stream()
                .filter(Objects::nonNull)
                .flatMap(taxon -> Stream.concat(
                        Stream.ofNullable(taxon.scientificName),
                        taxon.synonymGroups.stream()
                                .filter(group -> group != null && group.synonyms != null)
                                .flatMap(group -> group.synonyms.stream())
                                .filter(Objects::nonNull)
                                .map(synonym -> synonym.scientificName)))
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(name -> state.getProcessor().add(
                        new PrintPubTextRunElement(List.of(
                                new PrintPubTextRunElement.Run(
                                        PrintPubTextRunElement.RunType.ITALIC,
                                        name)))));
    }

    protected void buildCommonNameIndex(PrintPubExportState state) {

        state.getProcessor().add(new PrintPubPageBreakElement());
        state.getProcessor().add(new PrintPubSectionHeaderElement("Index to Common Names", 1));

        state.getTaxa().stream().filter(dto -> dto.commonNames != null).flatMap(dto -> dto.commonNames.stream())
                .filter(Objects::nonNull).sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(commonName -> state.getProcessor().add(new PrintPubParagraphElement(commonName)));
    }

    protected void buildAppendix(PrintPubExportState state) {

        PrintPubExportConfigurator conf = state.getConfig();

        if (!conf.isAppendIdentifierList()) {
            return;
        }

        state.getProcessor().add(new PrintPubPageBreakElement());
        state.getProcessor().add(new PrintPubSectionHeaderElement("Appendix: Digital Identifiers", 1));

        for (PrintPubTaxonSummaryDTO dto : state.getTaxa()) {

            if (!conf.isIncludeEmptyIds() && !hasAnySelectedIdentifier(dto, conf)) {
                continue;
            }

            List<PrintPubTextRunElement.Run> runs = new ArrayList<>();

            // --- name rendered from TaggedText ---
            List<PrintPubTextRunElement.Run> nameRuns = runsFromTaggedName(dto.taggedNameList);

            if (!nameRuns.isEmpty()) {
                runs.addAll(nameRuns);
            } else if (dto.titleCache != null && !dto.titleCache.trim().isEmpty()) {
                runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, dto.titleCache.trim()));
            }

            // --- plain identifier suffix ---
            StringBuilder suffix = new StringBuilder();

            if (conf.isIncludeWfoId()) {
                appendAppendixField(suffix, "WFO", dto.wfoIds);
            }

            if (conf.isIncludeIpniId()) {
                appendAppendixField(suffix, "IPNI", dto.ipniIds);
            }

            if (conf.isIncludeProtologueUris()) {
                appendAppendixField(suffix, "URL", dto.links);
            }

            if (suffix.length() > 0) {
                runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, "; " + suffix.toString()));
            }

            state.getProcessor().add(new PrintPubTextRunElement(runs));
        }
    }

    private boolean hasAnySelectedIdentifier(PrintPubTaxonSummaryDTO dto, PrintPubExportConfigurator conf) {

        if (dto == null) {
            return false;
        }

        if (conf.isIncludeWfoId() && hasValues(dto.wfoIds)) {
            return true;
        }

        if (conf.isIncludeIpniId() && hasValues(dto.ipniIds)) {
            return true;
        }

        if (conf.isIncludeProtologueUris() && hasValues(dto.links)) {
            return true;
        }

        return false;
    }

    private boolean hasValues(List<String> values) {

        if (values == null || values.isEmpty()) {
            return false;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void appendAppendixField(StringBuilder line, String label, List<String> values) {

        if (line.length() > 0) {
            line.append("; ");
        }

        line.append(label).append(": ").append(joinValuesOrDash(values));
    }

    private String joinValuesOrDash(List<String> values) {

        if (values == null || values.isEmpty()) {
            return "--";
        }

        StringBuilder result = new StringBuilder();

        for (String value : values) {

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(value.trim());
        }

        return result.length() == 0 ? "--" : result.toString();
    }
}
