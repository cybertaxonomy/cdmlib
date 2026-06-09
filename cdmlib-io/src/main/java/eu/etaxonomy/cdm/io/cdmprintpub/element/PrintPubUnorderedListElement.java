/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.cdmprintpub.element;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

/**
 * Document element representing an unordered list.
 *
 * Preserves list semantics independently of the output format, allowing
 * interpreters to render list items appropriately.
 */

public class PrintPubUnorderedListElement implements IPrintPubDocumentElement {
	private List<String> items = new ArrayList<>();

	public void addItem(String item) {
		this.items.add(item);
	}

	public List<String> getItems() {
		return items;
	}

	@Override
	public void accept(IPrintPubDocumentInterpreter interpreter) {
		interpreter.visit(this);
	}
}