package eu.etaxonomy.cdm.io.cdmprintpub.document;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubContext;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubFactDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubSynonymDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubSynonymGroupDTO;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubTaxonSummaryDTO;

/**
 * Consolidated implementation of the document builder.
 * Can render taxa in either tree or standard view based on configuration.
*
* @author veldmap97
* @date Feb 17, 2026
*/
@Component("printPubDocumentBuilder")
public class PrintPubDocumentBuilder extends AbstractPrintPubDocumentBuilder {

    private static final String INDENT_UNIT = "    ";

    @Override
    protected void buildContent(PrintPubExportState state, PrintPubContext context) {
        boolean isTreeView = state.getConfig().isDoIndentation();

        if (isTreeView) {
            state.getConfig().setGenerateScientificNameIndex(false);
            state.getConfig().setGenerateCommonNameIndex(false);
            state.getConfig().setAppendIdentifierList(false);
            context.referenceStore.clear(); // Suppress the Bibliography
            state.getProcessor().add(new PrintPubSectionHeader("Taxonomic Hierarchy", 1));
        }

        for (PrintPubTaxonSummaryDTO dto : context.taxonList) {
            renderTaxon(state, dto, isTreeView);
        }
    }

    private void renderTaxon(PrintPubExportState state, PrintPubTaxonSummaryDTO dto, boolean isTreeView) {
        if (isTreeView) {
            renderTaxonTreeView(state, dto);
        } else {
            renderTaxonStandardView(state, dto);
        }
    }

    private void renderTaxonTreeView(PrintPubExportState state, PrintPubTaxonSummaryDTO dto) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < dto.relativeDepth; i++) {
            indent.append(INDENT_UNIT);
        }
        String indentStr = indent.toString();

        StringBuilder line = new StringBuilder();
        line.append(indentStr).append("* **").append(dto.titleCache).append("**");

        if (state.getConfig().isIncludeTaxonomicConceptReference() && dto.secReferenceCitation != null) {
            String suffix = state.incrementShortCitation(dto.secReferenceCitation);
            line.append(" sec. ").append(dto.secReferenceCitation).append(suffix);
        }

        state.getProcessor().add(new PrintPubParagraphElement(line.toString()));

        if (state.getConfig().isDoSynonyms() && !dto.synonymGroups.isEmpty()) {
            String synonymIndent = indentStr + INDENT_UNIT;

            for (PrintPubSynonymGroupDTO group : dto.synonymGroups) {
                String prefix = group.isHomotypic ? "≡ " : "= ";

                for (PrintPubSynonymDTO syn : group.synonyms) {
                    StringBuilder synLine = new StringBuilder();
                    synLine.append(synonymIndent).append("- ").append(prefix).append(syn.titleCache);

                    if (state.getConfig().isIncludeSynonymConceptReference() && syn.secReference != null) {
                        String suffix = state.incrementShortCitation(syn.secReference);
                        synLine.append(" sec. ").append(syn.secReference).append(suffix);
                    }

                    state.getProcessor().add(new PrintPubParagraphElement(synLine.toString()));
                }
            }
        }
    }

    private void renderTaxonStandardView(PrintPubExportState state, PrintPubTaxonSummaryDTO dto) {
        int headerLevel = Math.min(dto.relativeDepth + 2, 6);
        state.getProcessor().add(new PrintPubSectionHeader(dto.titleCache, headerLevel));

        if (dto.typeSpecimenString != null) {
            state.getProcessor().add(new PrintPubParagraphElement(dto.typeSpecimenString));
        }
        if (dto.typeStatementString != null) {
            state.getProcessor().add(new PrintPubParagraphElement("Type (verbatim): " + dto.typeStatementString));
        }

        renderSynonyms(state, dto, false); // No indentation for standard view

        if (dto.distributionString != null) {
            state.getProcessor().add(new PrintPubLabeledTextElement("Distribution", dto.distributionString));
        }

        for (PrintPubFactDTO fact : dto.facts) {
            String textContent = fact.text;
            if (fact.citation != null) {
                textContent += " [" + fact.citation + "]";
            }
            state.getProcessor().add(new PrintPubLabeledTextElement(fact.label, textContent));
        }
    }

    private void renderSynonyms(PrintPubExportState state, PrintPubTaxonSummaryDTO dto, boolean isTreeView) {
        if (dto.synonymGroups.isEmpty()) {
            return;
        }

        for (PrintPubSynonymGroupDTO group : dto.synonymGroups) {
            String prefix = group.isHomotypic ? "≡ " : "= ";
            for (PrintPubSynonymDTO syn : group.synonyms) {
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
