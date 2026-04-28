/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cdmprintpub.dto;

import java.util.List;

import eu.etaxonomy.cdm.strategy.cache.TaggedText;

public class PrintPubSynonymDTO {
    public String titleCache;
    public List<TaggedText> taggedName;
    public String secReference;
    public String typeSpecimenString;
    public String typeStatementString;

    public boolean forceDashMarker;
}