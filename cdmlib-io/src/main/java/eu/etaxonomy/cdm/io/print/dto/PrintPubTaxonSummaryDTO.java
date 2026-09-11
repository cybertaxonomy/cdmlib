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
import java.util.List;
import java.util.UUID;

/**
 * Primary data transfer object for rendering a taxon.
 */
public class PrintPubTaxonSummaryDTO {

    public UUID uuid;

    //should never become null
    public PrintPubNameDTO nameDTO = new PrintPubNameDTO();

    public int relativeDepth;
    public String titleCache;

    public PrintPubSynonymGroupDTO homotypicSynonymGroup;
    public List<PrintPubSynonymGroupDTO> heterotypicSynonymGroups = new ArrayList<>();

    public List<PrintPubFactDTO> facts = new ArrayList<>();

    public String commonNameString;
    public List<String> commonNames = new ArrayList<>();
    public String distributionString;
    public String secReferenceCitation;
    public String secMicroCitation;

}