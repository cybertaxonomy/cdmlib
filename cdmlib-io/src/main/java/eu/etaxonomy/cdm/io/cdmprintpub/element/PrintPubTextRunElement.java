/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cdmprintpub.element;

import java.util.List;
import java.util.Objects;

import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

/**
 * Document element representing styled inline text.
 *
 * Consists of a sequence of text runs (plain, bold, italic, line breaks)
 * and an explicit semantic role that determines how the element is rendered
 * (e.g. body text, taxon name, grouped facts).
 *
 * Rendering decisions such as font size or paragraph style are delegated
 * to document interpreters based on the {@link PrintPubTextRole}.
 */
public class PrintPubTextRunElement implements IPrintPubDocumentElement {

    public enum RunType {
        TEXT,
        BOLD,
        ITALIC,
        LINE_BREAK
    }

    public enum PrintPubTextRole {
        BODY,
        TAXON_NAME,
        FACT_GROUP
    }

    /**
     * A single inline text run with formatting.
     */
    public static class Run {

        public final RunType type;
        public final String text;

        public Run(RunType type, String text) {
            this.type = Objects.requireNonNull(type, "RunType must not be null");
            this.text = Objects.requireNonNull(text, "Run text must not be null");
        }
    }

    private final String label;              // optional label (rendered e.g. as bold prefix)
    private final List<Run> runs;             // inline content
    private final PrintPubTextRole role;      // semantic intent

    public PrintPubTextRunElement(List<Run> runs) {
        this(null, runs, PrintPubTextRole.BODY);
    }


    public PrintPubTextRunElement(String label, List<Run> runs) {
        this(label, runs, PrintPubTextRole.BODY);
    }


    public PrintPubTextRunElement(String label, List<Run> runs, PrintPubTextRole role) {
        this.label = label;
        this.runs = Objects.requireNonNull(runs, "Runs must not be null");
        this.role = role == null ? PrintPubTextRole.BODY : role;
    }

    public String getLabel() {
        return label;
    }

    public List<Run> getRuns() {
        return runs;
    }

    public PrintPubTextRole getRole() {
        return role;
    }

    @Override
    public void accept(IPrintPubDocumentInterpreter interpreter) {
        interpreter.visit(this);
    }
}
