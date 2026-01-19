package eu.etaxonomy.cdm.io.cdmprintpub;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubContext.FactDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubContext.SynonymDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubContext.SynonymGroupDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubContext.TaxonSummaryDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubSectionHeader;
import eu.etaxonomy.cdm.model.reference.Reference;

@Component
public class PrintPubDocumentBuilder {

    public void buildLayout(PrintPubExportState state, PrintPubContext context) {
        // 1. Document Header
        state.getProcessor().add(new PrintPubSectionHeader(state.getConfig().getDocumentTitle(), 1));
        state.getProcessor().add(new PrintPubParagraphElement("Total Taxa: " + context.taxonList.size()));
        state.getProcessor().add(new PrintPubPageBreakElement());

        // 2. Main Taxon Content
        for (TaxonSummaryDTO dto : context.taxonList) {
            renderTaxon(state, dto);
        }

        // 3. Bibliography
        if (!context.referenceStore.isEmpty()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(new PrintPubSectionHeader("Bibliography", 1));
            for (Reference ref : context.getSortedBibliography()) {
                state.getProcessor().add(new PrintPubParagraphElement(ref.getTitleCache()));
            }
        }

        // 4. Index: Scientific Names
        if (state.getConfig().isGenerateScientificNameIndex()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(new PrintPubSectionHeader("Index to Scientific Names", 1));

            List<TaxonSummaryDTO> sortedTaxa = context.taxonList.stream()
                    .sorted(Comparator.comparing(t -> t.titleCache))
                    .collect(Collectors.toList());

            for (TaxonSummaryDTO dto : sortedTaxa) {
                // Simple index entry
                state.getProcessor().add(new PrintPubParagraphElement(dto.titleCache));
            }
        }

        // 5. Index: Common Names
        if (state.getConfig().isGenerateCommonNameIndex()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(new PrintPubSectionHeader("Index to Common Names", 1));

            // Extract all common names with reference to Taxon
            context.taxonList.stream()
                .flatMap(dto -> dto.commonNames.stream())
                .sorted()
                .forEach(commonName -> state.getProcessor().add(new PrintPubParagraphElement(commonName)));
        }

        // 6. Appendix: Digital Identifiers
        if (state.getConfig().isAppendIdentifierList()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(new PrintPubSectionHeader("Appendix: Digital Identifiers", 1));

            for (TaxonSummaryDTO dto : context.taxonList) {
                StringBuilder line = new StringBuilder(dto.titleCache);

                /*// Note: Real implementation needs retrieval of WFO-ID/URI from identifiers/sources
                if (state.getConfig().isIncludeWfoId()) {
                    // Placeholder logic - assuming WFO ID might be extracted in mapper in real scenario
                    // line.append(" [WFO-ID: ...]");
                }
                if (state.getConfig().isIncludeProtologueUris()) {
                    // Placeholder logic
                    // line.append(" [URI: ...]");
                }*/

                state.getProcessor().add(new PrintPubParagraphElement(line.toString()));
            }
        }
    }

    private void renderTaxon(PrintPubExportState state, TaxonSummaryDTO dto) {
        int headerLevel = Math.min(dto.relativeDepth + 2, 6);
        state.getProcessor().add(new PrintPubSectionHeader(dto.titleCache, headerLevel));

        if (dto.typeSpecimenString != null) {
            state.getProcessor().add(new PrintPubParagraphElement(dto.typeSpecimenString));
        }
        if (dto.typeStatementString != null) {
            state.getProcessor().add(new PrintPubParagraphElement("Type (verbatim): " + dto.typeStatementString));
        }

        renderSynonyms(state, dto);

        if (dto.distributionString != null) {
            state.getProcessor().add(new PrintPubLabeledTextElement("Distribution", dto.distributionString));
        }

        for (FactDTO fact : dto.facts) {
            String textContent = fact.text;
            if (fact.citation != null) {
                textContent += " [" + fact.citation + "]";
            }
            state.getProcessor().add(new PrintPubLabeledTextElement(fact.label, textContent));
        }
    }

    private void renderSynonyms(PrintPubExportState state, TaxonSummaryDTO dto) {
        if (dto.synonymGroups.isEmpty()) {
            return;
        }
        for (SynonymGroupDTO group : dto.synonymGroups) {
            String prefix = group.isHomotypic ? "≡ " : "= ";
            for (SynonymDTO syn : group.synonyms) {
                String line = prefix + syn.titleCache;
                if (syn.secReference != null) {
                    line += " sec. " + syn.secReference;
                }
                state.getProcessor().add(new PrintPubParagraphElement(line));

                if (syn.typeSpecimenString != null) {
                    state.getProcessor().add(new PrintPubParagraphElement("    " + syn.typeSpecimenString));
                }
            }
        }
    }
}