/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cdmprintpub.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrintPubFactDTO {

    public enum PrintPubFactKind {
        TEXT_DATA,
        COMMON_NAME,
        DISTRIBUTION,
        OTHER
    }

    public UUID featureUuid;
    public String label;
    public String text;
    public List<String> citations = new ArrayList<>();

    public Integer sortIndex;
    public Integer elementId;
    public PrintPubFactKind kind;

    public int sequence;
}