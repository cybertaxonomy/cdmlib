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
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubSectionHeaderElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubTextRunElement;
import eu.etaxonomy.cdm.io.cdmprintpub.util.PrintPubNonNestedHtmlTokenConverter;
import eu.etaxonomy.cdm.io.cdmprintpub.util.PrintPubNonNestedHtmlTokenizer;
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
public class PrintPubDocumentBuilder extends AbstractPrintPubDocumentBuilder {

    @Autowired
    private PrintPubFeatureOrderStrategyResolver featureOrderResolver;

    @Autowired
    private PrintPubFactOrderStrategyResolver factOrderResolver;

    @Override
    protected void buildContent(PrintPubExportState state) {
        state.clearCollectedReferences();
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
                if (first) {
                    // group header
                    if (syn.forceDashMarker) {
                        line.append("- ");
                    } else if (group.isHomotypic) {
                        line.append("≡ ");
                    } else {
                        line.append("= ");
                    }
                    first = false;
                } else {
                    // members of the same homotypic group
                    line.append("- ");
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

                if (fact.citation != null) {
                    combinedRuns.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT,
                            " [" + fact.citation + "]"));
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
}
