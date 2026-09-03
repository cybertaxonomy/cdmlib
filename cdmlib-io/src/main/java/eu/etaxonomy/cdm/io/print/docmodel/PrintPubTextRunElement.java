/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.print.docmodel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import eu.etaxonomy.cdm.io.print.render.IPrintPubDocumentInterpreter;

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
        LINE_BREAK,
        BOLD_ITALIC,
        CDM_REFERENCE
    }

    public enum PrintPubTextRole {
        BODY,
        TAXON_NAME,
        FACT_GROUP
    }

    public static class Run {

        public final RunType type;
        public final String text;

        public final String rawMarkup;
        public final String tagName;
        public final Map<String, String> attributes;

        public Run(RunType type, String text) {
            this(type, text, null, null, null);
        }

        public Run(
                RunType type,
                String text,
                String rawMarkup,
                String tagName,
                Map<String, String> attributes) {

            this.type = Objects.requireNonNull(type, "RunType must not be null");
            this.text = Objects.requireNonNull(text, "Run text must not be null");
            this.rawMarkup = rawMarkup;
            this.tagName = tagName;
            this.attributes = attributes == null
                    ? new LinkedHashMap<String, String>()
                    : new LinkedHashMap<String, String>(attributes);
        }

        public String getAttribute(String key) {
            return attributes.get(key);
        }

        public String getCdmId() {
            return attributes.get("cdmId");
        }

        public String getIntextId() {
            return attributes.get("intextId");
        }
    }

    private final String label;
    private final List<Run> runs;
    private final PrintPubTextRole role;

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