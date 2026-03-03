/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmprintpub.document;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubContext;
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Base implementation of the document builder using the Template Method pattern.
 * Enforces a standard document structure: Header -> Body (Abstract) -> Bibliography -> Indices.
 *
 * @author veldmap97
 * @date Feb 13, 2026
 */
public abstract class AbstractPrintPubDocumentBuilder implements IPrintPubDocumentBuilder {

    /**
     * The Template Method. It is final to prevent subclasses from changing the
     * overall document flow (Header -> Content -> Biblio -> Indices).
     */
	@Override
    public final void buildLayout(PrintPubExportState state, PrintPubContext context) {
        buildHeader(state, context);

        // Hook for subclasses to implement the specific layout logic
        buildContent(state, context);

        // FIX: Check if references exist instead of calling undefined config method
        if (!context.referenceStore.isEmpty()) {
             buildBibliography(state, context);
        }

        buildIndices(state, context);

        if (state.getConfig().isAppendIdentifierList()) {
            buildAppendix(state, context);
        }
    }

    /**
     * Abstract method that subclasses must implement to render the main taxonomic content.
     */
    protected abstract void buildContent(PrintPubExportState state, PrintPubContext context);

    // --- Shared Implementations ---

    protected void buildHeader(PrintPubExportState state, PrintPubContext context) {
        state.getProcessor().add(new PrintPubSectionHeader(state.getConfig().getDocumentTitle(), 1));
        state.getProcessor().add(new PrintPubParagraphElement("Total Taxa: " + context.taxonList.size()));
        state.getProcessor().add(new PrintPubPageBreakElement());
    }

    protected void buildBibliography(PrintPubExportState state, PrintPubContext context) {
        if (context.referenceStore.isEmpty()) {
            return;
        }

        state.getProcessor().add(new PrintPubPageBreakElement());
        state.getProcessor().add(new PrintPubSectionHeader("Bibliography", 1));

        for (Reference ref : context.getSortedBibliography()) {
            state.getProcessor().add(new PrintPubParagraphElement(ref.getTitleCache()));
        }
    }

    protected void buildIndices(PrintPubExportState state, PrintPubContext context) {
        // Index: Scientific Names
        if (state.getConfig().isGenerateScientificNameIndex()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(new PrintPubSectionHeader("Index to Scientific Names", 1));

            List<PrintPubTaxonSummaryDTO> sortedTaxa = context.taxonList.stream()
                    .sorted(Comparator.comparing(t -> t.titleCache))
                    .collect(Collectors.toList());

            for (PrintPubTaxonSummaryDTO dto : sortedTaxa) {
                state.getProcessor().add(new PrintPubParagraphElement(dto.titleCache));
            }
        }

        // Index: Common Names
        if (state.getConfig().isGenerateCommonNameIndex()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(new PrintPubSectionHeader("Index to Common Names", 1));

            context.taxonList.stream()
                .flatMap(dto -> dto.commonNames.stream())
                .sorted()
                .forEach(commonName -> state.getProcessor().add(new PrintPubParagraphElement(commonName)));
        }
    }

    protected void buildAppendix(PrintPubExportState state, PrintPubContext context) {
        state.getProcessor().add(new PrintPubPageBreakElement());
        state.getProcessor().add(new PrintPubSectionHeader("Appendix: Digital Identifiers", 1));

        for (PrintPubTaxonSummaryDTO dto : context.taxonList) {
            // Basic implementation; subclasses can override if they need specific formatting
            state.getProcessor().add(new PrintPubParagraphElement(dto.titleCache + " [" + dto.uuid + "]"));
        }
    }
}