package eu.etaxonomy.cdm.io.cdmprintpub.context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrintPubTaxonSummaryDTO {
    public UUID uuid;
    public String titleCache;
    public int relativeDepth;

    public String typeSpecimenString;
    public String typeStatementString;

    public List<PrintPubSynonymGroupDTO> synonymGroups = new ArrayList<>();

    public List<PrintPubFactDTO> facts = new ArrayList<>();

    public List<String> commonNames = new ArrayList<>();
    public String distributionString;
    public String secReferenceCitation;
}