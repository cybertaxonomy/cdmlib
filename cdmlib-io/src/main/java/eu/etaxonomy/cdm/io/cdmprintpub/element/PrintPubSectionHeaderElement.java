/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmprintpub.element;

import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

/**
 * Document element representing a hierarchical section heading.
 *
 * Contains a title and a heading level which is mapped to format-specific
 * heading styles by document interpreters.
 */

public class PrintPubSectionHeaderElement implements IPrintPubDocumentElement {
	private String title;
	private int level;

	public PrintPubSectionHeaderElement(String title, int level) {
		this.title = title;
		this.level = level;
	}

	public String getTitle() {
		return title;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public void accept(IPrintPubDocumentInterpreter interpreter) {
		interpreter.visit(this);
	}
}