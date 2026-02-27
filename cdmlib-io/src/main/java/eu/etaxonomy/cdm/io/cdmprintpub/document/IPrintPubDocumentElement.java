package eu.etaxonomy.cdm.io.cdmprintpub.document;

import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

public interface IPrintPubDocumentElement {
    void accept(IPrintPubDocumentInterpreter interpreter);
}