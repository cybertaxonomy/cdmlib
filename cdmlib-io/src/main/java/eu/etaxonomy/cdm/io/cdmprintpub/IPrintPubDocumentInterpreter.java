// $Id$
/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmprintpub;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.IPrintPubDocumentElement;

/**
 * @author veldmap97
 * @date Dec 2, 2025
 *
 */
public interface IPrintPubDocumentInterpreter {

    void visit(IPrintPubDocumentElement element);

    Object getResult();
}
