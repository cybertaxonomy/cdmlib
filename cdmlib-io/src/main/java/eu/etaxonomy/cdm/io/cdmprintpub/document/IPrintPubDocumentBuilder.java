/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmprintpub.document;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;

/**
 * Interface for Print/Publication document layout builders.
 *
 * Implementations transform data stored in the export state into a structured
 * document model, independent of the final output format.
 */

public interface IPrintPubDocumentBuilder {
	void buildLayout(PrintPubExportState state);
}