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
import java.util.UUID;
import java.util.stream.Collectors;

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

    protected void buildContent(PrintPubExportState state) {
        state.getProcessor().add(new PrintPubSectionHeaderElement("Taxonomic Hierarchy", 1));

        for (PrintPubTaxonSummaryDTO dto : state.getTaxa()) {
            renderTaxon(state, dto);
        }
    }

    private void renderTaxon(PrintPubExportState state, PrintPubTaxonSummaryDTO dto) {
        String indent = "   ";

        renderTaxonHeading(state, dto, indent);

        if (dto.typeSpecimenString != null && !dto.typeSpecimenString.trim().isEmpty()) {
            state.getProcessor().add(new PrintPubParagraphElement(dto.typeSpecimenString));
        }

        if (dto.typeStatementString != null && !dto.typeStatementString.trim().isEmpty()) {
            state.getProcessor().add(new PrintPubParagraphElement(dto.typeStatementString));
        }

        if (state.getConfig().isDoSynonyms()) {
            renderSynonyms(state, dto, indent);
        }

        renderTaxonDetails(state, dto);

    }

    private void renderTaxonHeading(PrintPubExportState state, PrintPubTaxonSummaryDTO dto, String indent) {
        int headerLevel = Math.min(dto.relativeDepth + 2, 6);

        state.getProcessor().add(new PrintPubTextRunElement(null, runsFromTaggedName(dto.taggedName),
                PrintPubTextRunElement.PrintPubTextRole.TAXON_NAME));
    }

    private void renderSynonyms(PrintPubExportState state, PrintPubTaxonSummaryDTO dto, String indent) {

        if (dto.synonymGroups.isEmpty()) {
            return;
        }

        for (PrintPubSynonymGroupDTO group : dto.synonymGroups) {

            boolean first = true;

            for (PrintPubSynonymDTO syn : group.synonyms) {

                StringBuilder line = new StringBuilder();

                boolean doIndent = false;

                // --- choose prefix ---
                if (syn.forceDashMarker) {
                    line.append(UTF8.EM_DASH + " ");
                } else if (first) {
                    line.append("= ");
                    first = false;
                } else {
                    // members of the same homotypic group
                    line.append("≡ ");
                    doIndent = true;
                }

                // --- name ---
                line.append(syn.titleCache);

                // --- sec. reference ---
                if (state.getConfig().isIncludeSynonymConceptReference() && syn.secReference != null) {

                    String suffix = state.incrementShortCitation(syn.secReference);
                    line.append(" sec. ").append(syn.secReference).append(suffix);
                }

                state.getProcessor().add(new PrintPubParagraphElement(line.toString(), doIndent));

                // --- type information ---
                if (syn.typeSpecimenString != null && !syn.typeSpecimenString.trim().isEmpty()) {
                    state.getProcessor().add(new PrintPubParagraphElement(syn.typeSpecimenString));
                }
            }
        }
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

        buildIndices(state);

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

    protected void buildIndices(PrintPubExportState state) {

        // ---- Scientific name index ----
        if (state.getConfig().isGenerateScientificNameIndex()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(new PrintPubSectionHeaderElement("Index to Scientific Names", 1));

            List<PrintPubTaxonSummaryDTO> sortedTaxa = state.getTaxa().stream()
                    .sorted(Comparator.comparing(t -> t.titleCache)).collect(Collectors.toList());

            for (PrintPubTaxonSummaryDTO dto : sortedTaxa) {
                state.getProcessor().add(new PrintPubParagraphElement(dto.titleCache));
            }
        }

        // ---- Common name index ----
        if (state.getConfig().isGenerateCommonNameIndex()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(new PrintPubSectionHeaderElement("Index to Common Names", 1));

            state.getTaxa().stream().flatMap(dto -> dto.commonNames.stream()).sorted()
                    .forEach(commonName -> state.getProcessor().add(new PrintPubParagraphElement(commonName)));
        }
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

            StringBuilder line = new StringBuilder();

            if (dto.titleCache != null && !dto.titleCache.trim().isEmpty()) {
                line.append(dto.titleCache.trim());
            }

            if (conf.isIncludeWfoId()) {
                appendAppendixField(line, "WFO", dto.wfoIds);
            }

            if (conf.isIncludeIpniId()) {
                appendAppendixField(line, "IPNI", dto.ipniIds);
            }

            if (conf.isIncludeProtologueUris()) {
                appendAppendixField(line, "URL", dto.links);
            }

            state.getProcessor().add(new PrintPubParagraphElement(line.toString(), true));
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
