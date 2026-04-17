package eu.etaxonomy.cdm.io.cdmprintpub.document;

import java.util.List;

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

    private static final String INDENT_UNIT = "    ";

    private enum RenderMode {
        TREE, STANDARD
    }

    @Override
    protected void buildContent(PrintPubExportState state, PrintPubContext context) {

        RenderMode mode = state.getConfig().isDoIndentation() ? RenderMode.TREE : RenderMode.STANDARD;

        if (mode == RenderMode.TREE) {
            state.getConfig().setGenerateScientificNameIndex(false);
            state.getConfig().setGenerateCommonNameIndex(false);
            state.getConfig().setAppendIdentifierList(false);
            context.referenceStore.clear();
            state.getProcessor().add(new PrintPubSectionHeader("Taxonomic Hierarchy", 1));
        }

        for (PrintPubTaxonSummaryDTO dto : context.taxonList) {
            renderTaxon(state, dto, mode);
        }
    }

    private void renderTaxon(PrintPubExportState state, PrintPubTaxonSummaryDTO dto, RenderMode mode) {
        String indent = (mode == RenderMode.TREE)
                ? indent(dto.relativeDepth)
                : "";

        renderTaxonHeading(state, dto, mode, indent);

        if (state.getConfig().isDoSynonyms()) {
            renderSynonyms(state, dto, mode, indent);
        }

        if (mode == RenderMode.STANDARD) {
            renderTaxonDetails(state, dto);
        }
    }

    private void renderTaxonHeading(PrintPubExportState state, PrintPubTaxonSummaryDTO dto, RenderMode mode,
            String indent) {

        if (mode == RenderMode.TREE) {

            StringBuilder line = new StringBuilder();
            line.append(indent).append("* **").append(dto.titleCache).append("**");

            if (state.getConfig().isIncludeTaxonomicConceptReference() && dto.secReferenceCitation != null) {

                String suffix = state.incrementShortCitation(dto.secReferenceCitation);
                line.append(" sec. ").append(dto.secReferenceCitation).append(suffix);
            }

            state.getProcessor().add(new PrintPubParagraphElement(line.toString()));

        } else {

            int headerLevel = Math.min(dto.relativeDepth + 2, 6);
            state.getProcessor().add(new PrintPubSectionHeader(dto.titleCache, headerLevel));
        }
    }

    private void renderSynonyms(PrintPubExportState state, PrintPubTaxonSummaryDTO dto, RenderMode mode,
            String indent) {

        if (dto.synonymGroups.isEmpty()) {
            return;
        }

        String baseIndent = (mode == RenderMode.TREE) ? indent + INDENT_UNIT : "";

        for (PrintPubSynonymGroupDTO group : dto.synonymGroups) {
            String prefix = group.isHomotypic ? "≡ " : "= ";

            for (PrintPubSynonymDTO syn : group.synonyms) {

                StringBuilder line = new StringBuilder();

                if (mode == RenderMode.TREE) {
                    line.append(baseIndent).append("- ");
                }

                line.append(prefix).append(syn.titleCache);

                if (state.getConfig().isIncludeSynonymConceptReference() && syn.secReference != null) {

                    String suffix = state.incrementShortCitation(syn.secReference);
                    line.append(" sec. ").append(syn.secReference).append(suffix);
                }

                state.getProcessor().add(new PrintPubParagraphElement(line.toString()));

                if (mode == RenderMode.STANDARD && syn.typeSpecimenString != null) {

                    state.getProcessor().add(new PrintPubParagraphElement(INDENT_UNIT + syn.typeSpecimenString));
                }
            }
        }
    }

    private void renderTaxonDetails(PrintPubExportState state, PrintPubTaxonSummaryDTO dto) {

        if (dto.typeSpecimenString != null) {
            state.getProcessor().add(new PrintPubLabeledTextElement("Types", dto.typeSpecimenString));
        }

        if (dto.typeStatementString != null) {
            state.getProcessor().add(new PrintPubLabeledTextElement("Types", dto.typeStatementString));
        }

        if (dto.distributionString != null) {
            state.getProcessor().add(new PrintPubLabeledTextElement("Distribution", dto.distributionString));
        }

        for (PrintPubFactDTO fact : dto.facts) {

            List<PrintPubTextRunElement.Run> runs = PrintPubNonNestedHtmlTokenConverter
                    .toRuns(PrintPubNonNestedHtmlTokenizer.tokenize(fact.text));

            if (fact.citation != null) {
                runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT,
                        " [" + fact.citation + "]"));
            }

            state.getProcessor().add(new PrintPubTextRunElement(fact.label, runs));
        }
    }

    private static String indent(int depth) {
        StringBuilder sb = new StringBuilder(depth * INDENT_UNIT.length());
        for (int i = 0; i < depth; i++) {
            sb.append(INDENT_UNIT);
        }
        return sb.toString();
    }

}
