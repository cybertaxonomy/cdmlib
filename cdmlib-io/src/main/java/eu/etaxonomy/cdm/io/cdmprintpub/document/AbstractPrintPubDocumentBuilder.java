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
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubTaxonSummaryDTO;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Base implementation of the document builder using the Template Method pattern.
 * Enforces a standard document structure:
 * Header -> Content -> Bibliography -> Indices -> Appendix.
 *
 * @author veldmap97
 * @date Feb 13, 2026
 */
public abstract class AbstractPrintPubDocumentBuilder implements IPrintPubDocumentBuilder {

    /**
     * The Template Method.
     * Final to enforce the canonical document flow.
     */
    @Override
    public final void buildLayout(PrintPubExportState state) {

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

    /**
     * Subclasses render the main taxonomic content here.
     */
    protected abstract void buildContent(PrintPubExportState state);

    // ======================
    // Shared building blocks
    // ======================

    protected void buildHeader(PrintPubExportState state) {
        state.getProcessor().add(
                new PrintPubSectionHeader(state.getConfig().getDocumentTitle(), 1)
        );
        state.getProcessor().add(
                new PrintPubParagraphElement("Total Taxa: " + state.getTaxa().size())
        );
        state.getProcessor().add(new PrintPubPageBreakElement());
    }

    protected void buildBibliography(PrintPubExportState state) {
        List<Reference> bibliography = state.getSortedBibliography();
        if (bibliography.isEmpty()) {
            return;
        }

        state.getProcessor().add(new PrintPubPageBreakElement());
        state.getProcessor().add(new PrintPubSectionHeader("Bibliography", 1));

        for (Reference ref : bibliography) {
            state.getProcessor().add(
                    new PrintPubParagraphElement(ref.getTitleCache())
            );
        }
    }

    protected void buildIndices(PrintPubExportState state) {

        // ---- Scientific name index ----
        if (state.getConfig().isGenerateScientificNameIndex()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(
                    new PrintPubSectionHeader("Index to Scientific Names", 1)
            );

            List<PrintPubTaxonSummaryDTO> sortedTaxa =
                    state.getTaxa().stream()
                            .sorted(Comparator.comparing(t -> t.titleCache))
                            .collect(Collectors.toList());

            for (PrintPubTaxonSummaryDTO dto : sortedTaxa) {
                state.getProcessor().add(
                        new PrintPubParagraphElement(dto.titleCache)
                );
            }
        }

        // ---- Common name index ----
        if (state.getConfig().isGenerateCommonNameIndex()) {
            state.getProcessor().add(new PrintPubPageBreakElement());
            state.getProcessor().add(
                    new PrintPubSectionHeader("Index to Common Names", 1)
            );

            state.getTaxa().stream()
                    .flatMap(dto -> dto.commonNames.stream())
                    .sorted()
                    .forEach(commonName ->
                            state.getProcessor().add(
                                    new PrintPubParagraphElement(commonName)
                            )
                    );
        }
    }

    protected void buildAppendix(PrintPubExportState state) {
        state.getProcessor().add(new PrintPubPageBreakElement());
        state.getProcessor().add(
                new PrintPubSectionHeader("Appendix: Digital Identifiers", 1)
        );

        for (PrintPubTaxonSummaryDTO dto : state.getTaxa()) {
            state.getProcessor().add(
                    new PrintPubParagraphElement(dto.titleCache + " [" + dto.uuid + "]")
            );
        }
    }
}