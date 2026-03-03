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
import eu.etaxonomy.cdm.io.cdmprintpub.context.PrintPubContext;

/**
 * Interface for building the layout of a Print/Publication document.
 *
 * @author veldmap97
 * @date Feb 13, 2026
 */
public interface IPrintPubDocumentBuilder {
    void buildLayout(PrintPubExportState state, PrintPubContext context);
}