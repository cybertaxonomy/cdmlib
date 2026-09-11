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
 * Document element representing a labeled text paragraph.
 *
 * Typically used for structured facts such as distribution or similar labeled
 * content, where the label and value are rendered together.
 */

public class PrintPubLabeledTextElement implements IPrintPubDocumentElement {
	private String label;
	private String text;

	public PrintPubLabeledTextElement(String label, String text) {
		this.label = label;
		this.text = text;
	}

	public String getLabel() {
		return label;
	}

	public String getText() {
		return text;
	}

	@Override
	public void accept(IPrintPubDocumentInterpreter interpreter) {
		interpreter.visit(this);
	}
}