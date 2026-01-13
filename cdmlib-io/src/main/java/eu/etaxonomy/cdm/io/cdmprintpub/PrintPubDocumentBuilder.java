package eu.etaxonomy.cdm.io.cdmprintpub;

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
        // Document Header
        state.getProcessor().add(new PrintPubSectionHeader(state.getConfig().getDocumentTitle(), 1));
        state.getProcessor().add(new PrintPubParagraphElement("Total Taxa: " + context.taxonList.size()));
        state.getProcessor().add(new PrintPubPageBreakElement());

        // Taxon Content
        for (TaxonSummaryDTO dto : context.taxonList) {
            renderTaxon(state, dto);
        }

        // Bibliography
        if (!context.referenceStore.isEmpty()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(new PrintPubSectionHeader("Bibliography", 1));
            for (Reference ref : context.getSortedBibliography()) {
                state.getProcessor().add(new PrintPubParagraphElement(ref.getTitleCache()));
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

        // === REFACTORED: Use Semantic Element (No Markdown here!) ===
        if (dto.distributionString != null) {
            state.getProcessor().add(new PrintPubLabeledTextElement("Distribution", dto.distributionString));
        }

        // === REFACTORED: Use Semantic Element (No Markdown here!) ===
        for (FactDTO fact : dto.facts) {
            String textContent = fact.text;

            // We append the citation to the text content, but formatting the LABEL is the Interpreter's job.
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