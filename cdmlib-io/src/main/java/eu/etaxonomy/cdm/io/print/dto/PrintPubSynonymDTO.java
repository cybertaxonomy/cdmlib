/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.print.dto;

/**
 * Data transfer object representing a single taxonomic synonym.
 */
public class PrintPubSynonymDTO {

    //should never become null
    public PrintPubNameDTO nameDTO = new PrintPubNameDTO();

    public String secReference;

    public String titleCache;

    public boolean isInvalidDesignation;
}