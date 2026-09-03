/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.print.docmodel;

import eu.etaxonomy.cdm.io.print.render.IPrintPubDocumentInterpreter;

/**
 * Document element representing a paragraph of plain text.
 *
 * Supports optional indentation and is used for free text such as type
 * statements or synonym entries.
 */

public class PrintPubParagraphElement implements IPrintPubDocumentElement {

	private final String text;
	private final boolean indented;

	public PrintPubParagraphElement(String text) {
		this(text, false);
	}

	public PrintPubParagraphElement(String text, boolean indented) {
		this.text = text;
		this.indented = indented;
	}

	public String getText() {
		return text;
	}

	public boolean isIndented() {
		return indented;
	}

	@Override
	public void accept(IPrintPubDocumentInterpreter interpreter) {
		interpreter.visit(this);
	}
}