package eu.etaxonomy.cdm.io.cdmprintpub.dto;

import java.util.ArrayList;
import java.util.List;

public class PrintPubSynonymGroupDTO {
    public boolean isHomotypic; // True = '≡', False = '='
    public List<PrintPubSynonymDTO> synonyms = new ArrayList<>();
}