/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmprintpub;

import java.nio.charset.StandardCharsets;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * Acts as the bridge between the extraction logic and the Document Model.
 * Unlike the CSV version, this does NOT handle text formatting or buffering.
 * * @author veldmap97
 * @date Dec 2, 2025
 */
public class PrintPubExportResultProcessor {

    private PrintPubExportState state;

    public PrintPubExportResultProcessor(PrintPubExportState state) {
        this.state = state;
    }

    /**
     * Adds an element (Header, Paragraph, Table) to the document.
     * Use this for elements that don't need deduplication (like Chapter Headers).
     */
    public void add(IPrintPubDocumentElement element) {
        state.getDocumentModel().add(element);
    }

    /**
     * Adds an element ONLY if the associated CDM Object (UUID) hasn't been printed yet.
     * Analog to the old `put(table, id, record)` but for Objects.
     */
    public void add(ICdmBase cdmBase, IPrintPubDocumentElement element) {
        if (state.hasPrinted(cdmBase.getUuid())) {
            return; // We already printed this object
        }

        state.markAsPrinted(cdmBase.getUuid());
        state.getDocumentModel().add(element);
    }

    /**
     * The "Commit" phase.
     * Instead of writing byte arrays manually, we ask an Interpreter to render the model.
     */
    public void createFinalResult() {
        if (state.getDocumentModel().isEmpty()) {
            state.getResult().addWarning("Document Model is empty. No data exported.");
            return;
        }

        try {
            // 1. Select the Interpreter (Here: Markdown. Later: PDF/ODT via config)
            IPrintPubDocumentInterpreter interpreter = new PrintPubMarkdownInterpreter();

            // 2. "Play" the document structure into the interpreter
            state.getDocumentModel().render(interpreter);

            // 3. Retrieve the result (String for MD, byte[] for PDF)
            String resultOutput = (String) interpreter.getResult();

            // 4. Store in the legacy ExportResult wrapper
            state.getResult().putExportData("export.md", resultOutput.getBytes(StandardCharsets.UTF_8));
            state.getResult().setExportType(ExportType.PRINT_PUBLICATION); // Assuming you added this enum
        } catch (Exception e) {
            state.getResult().addException(e, "Error rendering document: " + e.getMessage());
        }
    }
}