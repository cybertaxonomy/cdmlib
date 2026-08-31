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

/**
 * Data transfer object grouping related taxonomic synonyms.
 */
public class PrintPubSynonymGroupDTO {

    public List<PrintPubSynonymDTO> synonyms = new ArrayList<>();
}