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
 * Base interface for all Print/Publication document elements.
 *
 * Document elements participate in a Visitor pattern by accepting a document
 * interpreter, allowing the same document model to be rendered into different
 * output formats.
 */

public interface IPrintPubDocumentElement {
	void accept(IPrintPubDocumentInterpreter interpreter);
}