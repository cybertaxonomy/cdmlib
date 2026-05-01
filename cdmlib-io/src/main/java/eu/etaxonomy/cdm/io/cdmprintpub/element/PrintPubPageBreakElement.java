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
 * Document element representing a logical page break.
 *
 * Contains no textual content and is interpreted as a page or section break
 * depending on the output format.
 */

public class PrintPubPageBreakElement implements IPrintPubDocumentElement {
	@Override
	public void accept(IPrintPubDocumentInterpreter interpreter) {
		interpreter.visit(this);
	}
}