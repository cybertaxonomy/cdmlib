package eu.etaxonomy.cdm.io.cdmprintpub.document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubContext;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubFactDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubSynonymDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubSynonymGroupDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.render.PrintPubTextRunElement;
import eu.etaxonomy.cdm.io.cdmprintpub.util.PrintPubNonNestedHtmlTokenConverter;
import eu.etaxonomy.cdm.io.cdmprintpub.util.PrintPubNonNestedHtmlTokenizer;

/**
 *
 * @author veldmap97
 * @date Feb 17, 2026
 */
@Component("printPubDocumentBuilder")
public class PrintPubDocumentBuilder extends AbstractPrintPubDocumentBuilder {

    @Override
    protected void buildContent(PrintPubExportState state, PrintPubContext context) {
        state.getConfig().setGenerateScientificNameIndex(false);
        state.getConfig().setGenerateCommonNameIndex(false);
        state.getConfig().setAppendIdentifierList(false);
        context.referenceStore.clear();
        state.getProcessor().add(new PrintPubSectionHeader("Taxonomic Hierarchy", 1));

        for (PrintPubTaxonSummaryDTO dto : context.taxonList) {
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
        state.getProcessor().add(new PrintPubSectionHeader(dto.titleCache, headerLevel));
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

    private String normalizeFactLabel(String label) {
        if (label == null) {
            return null;
        }
        // Remove leading <Category> if present
        return label.replaceFirst("^<[^>]+>", "").trim();
    }
}
