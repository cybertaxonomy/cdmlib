/**
 * Copyright (C) 2025 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cdmprintpub.document;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

public class PrintPubDocumentModel {

    private List<IPrintPubDocumentElement> elements = new ArrayList<>();

    public void add(IPrintPubDocumentElement element) {
        this.elements.add(element);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void render(IPrintPubDocumentInterpreter interpreter) {
        for (IPrintPubDocumentElement element : elements) {
            element.accept(interpreter);
        }
    }
}