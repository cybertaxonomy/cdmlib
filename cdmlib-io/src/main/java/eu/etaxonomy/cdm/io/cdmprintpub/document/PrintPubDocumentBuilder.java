/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cdmprintpub.document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubFactDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubSynonymDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubSynonymGroupDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.render.PrintPubTextRunElement;
import eu.etaxonomy.cdm.io.cdmprintpub.util.PrintPubNonNestedHtmlTokenConverter;
import eu.etaxonomy.cdm.io.cdmprintpub.util.PrintPubNonNestedHtmlTokenizer;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 *
 * @author veldmap97
 * @date Feb 17, 2026
 */
@Component("printPubDocumentBuilder")
public class PrintPubDocumentBuilder extends AbstractPrintPubDocumentBuilder {

    @Override
    protected void buildContent(PrintPubExportState state) {
        state.getConfig().setGenerateScientificNameIndex(false);
        state.getConfig().setGenerateCommonNameIndex(false);
        state.getConfig().setAppendIdentifierList(false);
        state.clearCollectedReferences();
        state.getProcessor().add(new PrintPubSectionHeader("Taxonomic Hierarchy", 1));

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

        state.getProcessor().add(new PrintPubTextRunElement(null, runsFromTaggedName(dto.taggedName), headerLevel));

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
                    state.getProcessor().add(new PrintPubParagraphElement("\\t " + syn.typeSpecimenString));
                }
            }
        }
    }

    private void renderTaxonDetails(PrintPubExportState state, PrintPubTaxonSummaryDTO dto) {

        if (dto.distributionString != null) {
            state.getProcessor().add(new PrintPubLabeledTextElement("Distribution", dto.distributionString));
        }

        // Group facts by *normalized* label, sorted by that label
        Map<String, List<PrintPubFactDTO>> factsByLabel = dto.facts.stream()
                .collect(Collectors.groupingBy(f -> normalizeFactLabel(f.label),
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().sorted(Comparator.comparing(f -> normalizeFactLabel(f.label)))
                                        .collect(Collectors.toList()))));

        // Process groups in sorted order of normalized labels
        factsByLabel.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {

            List<PrintPubTextRunElement.Run> combinedRuns = new ArrayList<>();
            boolean first = true;

            for (PrintPubFactDTO fact : entry.getValue()) {

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

            state.getProcessor().add(new PrintPubTextRunElement(entry.getKey(), combinedRuns));
        });
    }

    private List<PrintPubTextRunElement.Run>
    runsFromTaggedName(List<TaggedText> taggedName) {

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
                runs.add(new PrintPubTextRunElement.Run(
                        PrintPubTextRunElement.RunType.TEXT, " "));
            }
            first = false;

            if (tt.getType() == TagEnum.name) {
                runs.add(new PrintPubTextRunElement.Run(
                        PrintPubTextRunElement.RunType.ITALIC, text));
            } else {
                runs.add(new PrintPubTextRunElement.Run(
                        PrintPubTextRunElement.RunType.TEXT, text));
            }
        }
        return runs;
    }

    private boolean needsSpaceBefore(String text) {
        return !text.startsWith(",")
            && !text.startsWith(";")
            && !text.startsWith(")");
    }

    private String normalizeFactLabel(String label) {
        if (label == null) {
            return null;
        }
        // Remove leading <Category> if present
        return label.replaceFirst("^<[^>]+>", "").trim();
    }
}
