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
 * Consists of a sequence of text runs with formatting such as plain text, bold,
 * italic, or line breaks, and can be used in headings or paragraphs.
 */

public class PrintPubTextRunElement implements IPrintPubDocumentElement {

	public enum RunType {
		TEXT, BOLD, ITALIC, LINE_BREAK
	}

	public static class Run {
		public final RunType type;
		public final String text;

		public Run(RunType type, String text) {
			this.type = Objects.requireNonNull(type, "RunType must not be null");
			this.text = Objects.requireNonNull(text, "Run text must not be null");
		}
	}

	private final String label;
	private final List<Run> runs;
	private final Integer headingLevel; // null = paragraph

	public PrintPubTextRunElement(String label, List<Run> runs) {
		this(label, runs, null);
	}

	public PrintPubTextRunElement(String label, List<Run> runs, Integer headingLevel) {
		this.label = label;
		this.runs = Objects.requireNonNull(runs, "Runs must not be null");
		this.headingLevel = headingLevel;
	}

	public String getLabel() {
		return label;
	}

	public List<Run> getRuns() {
		return runs;
	}

	public Integer getHeadingLevel() {
		return headingLevel;
	}

	public boolean isHeading() {
		return headingLevel != null;
	}

	@Override
	public void accept(IPrintPubDocumentInterpreter interpreter) {
		interpreter.visit(this);
	}
}