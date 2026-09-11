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
 * Document element representing a reference to a text mark and its page number.
 *
 * @author muellera
 * @since 11.09.2026
 */
public class PrintPubPageReferenceElement implements IPrintPubDocumentElement {

    private final String refMarkId;

    public PrintPubPageReferenceElement(String refMarkId) {
        this.refMarkId = refMarkId;
    }

    public String getRefMarkId() {
        return refMarkId;
    }

    @Override
    public void accept(IPrintPubDocumentInterpreter interpreter) {
        interpreter.visit(this);
    }
}
