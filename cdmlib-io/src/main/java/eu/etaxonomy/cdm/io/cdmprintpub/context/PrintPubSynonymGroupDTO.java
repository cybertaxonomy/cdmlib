package eu.etaxonomy.cdm.io.cdmprintpub.context;

import java.util.ArrayList;
import java.util.List;

public class PrintPubSynonymGroupDTO {
    public boolean isHomotypic; // True = '≡', False = '='
    public List<PrintPubSynonymDTO> synonyms = new ArrayList<>();
}