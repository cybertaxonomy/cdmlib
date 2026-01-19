/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmprintpub;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * Acts as the bridge between the extraction logic and the Document Model.
 * Unlike the CSV version, this does NOT handle text formatting or buffering.
 * * @author veldmap97
 *
 * @date Dec 2, 2025
 */
public class PrintPubExportResultProcessor {

    private PrintPubExportState state;

    public PrintPubExportResultProcessor(PrintPubExportState state) {
        this.state = state;
    }

    public void add(IPrintPubDocumentElement element) {
        state.getDocumentModel().add(element);
    }

    public void add(ICdmBase cdmBase, IPrintPubDocumentElement element) {
        if (state.hasPrinted(cdmBase.getUuid())) {
            return;
        }

        state.markAsPrinted(cdmBase.getUuid());
        state.getDocumentModel().add(element);
    }

    public void createFinalResult() {
        if (state.getDocumentModel().isEmpty()) {
            state.getResult().addWarning("Document Model is empty. No data exported.");
            return;
        }

        try {
            IPrintPubDocumentInterpreter interpreter = new PrintPubMarkdownInterpreter();
            state.getDocumentModel().render(interpreter);
            String resultOutput = (String) interpreter.getResult();

            byte[] data = resultOutput.getBytes(StandardCharsets.UTF_8);

            state.getResult().putExportData("export.md", data);
            state.getResult().setExportType(ExportType.PRINT_PUBLICATION);

            File destinationDir = state.getConfig().getDestination();

            if (destinationDir != null) {
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs();
                }

                File outputFile = new File(destinationDir, "export.md");

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(data);
                }
            } else {
                state.getResult().addError("No destination directory configured. File could not be written.");
            }

        } catch (Exception e) {
            state.getResult().addException(e, "Error rendering/writing document: " + e.getMessage());
        }
    }
}