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

import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * Primary data transfer object for rendering a taxon.
 */
public class PrintPubTaxonSummaryDTO {
    public UUID uuid;
    public List<TaggedText> taggedNameList;
    public List<TaggedText> taggedScientificIndexNameList;

    public int relativeDepth;
    public String titleCache;

    public String typeSpecimenString;
    public String typeStatementString;

    public List<PrintPubSynonymGroupDTO> synonymGroups = new ArrayList<>();

    public List<PrintPubFactDTO> facts = new ArrayList<>();

    public List<String> commonNames = new ArrayList<>();
    public String distributionString;
    public String secReferenceCitation;
    public String secMicroCitation;

    public List<String> links = new ArrayList<>();
    public List<String> wfoIds = new ArrayList<>();
    public List<String> ipniIds = new ArrayList<>();
}