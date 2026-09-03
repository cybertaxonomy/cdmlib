/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.print.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.etaxonomy.cdm.model.reference.Reference;

public class PrintPubReferenceEntryDTO {

    public enum PrintPubReferenceSourceType {
        TAXON_SEC, SYNONYM_SEC, TAXON_FACT_SOURCE, FACT_DATASET_SOURCE, ACCEPTED_NAME_RELATIONSHIP_SOURCE, SYNONYM_NAME_RELATIONSHIP_SOURCE, ACCEPTED_NAME_TYPE_DESIGNATION_SOURCE, SYNONYM_NAME_TYPE_DESIGNATION_SOURCE, ACCEPTED_NAME_TYPE_DESIGNATION_OTHER_SOURCE, SYNONYM_NAME_TYPE_DESIGNATION_OTHER_SOURCE
    }

    private final Reference reference;
    private final List<PrintPubReferenceSourceType> sourceTypes = new ArrayList<PrintPubReferenceSourceType>();

    public PrintPubReferenceEntryDTO(Reference reference) {
        this.reference = reference;
    }

    public Reference getReference() {
        return reference;
    }

    public List<PrintPubReferenceSourceType> getSourceTypes() {
        return Collections.unmodifiableList(sourceTypes);
    }

    public void addSourceType(PrintPubReferenceSourceType sourceType) {
        if (sourceType != null) {
            sourceTypes.add(sourceType);
        }
    }

    public boolean hasSourceType(PrintPubReferenceSourceType sourceType) {
        return sourceTypes.contains(sourceType);
    }
}