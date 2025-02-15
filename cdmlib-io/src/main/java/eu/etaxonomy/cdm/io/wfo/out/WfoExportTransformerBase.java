/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.out;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.common.mapping.out.ExportTransformerBase;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author muellera
 * @since 20.03.2024
 */
public class WfoExportTransformerBase extends ExportTransformerBase {

    private static final long serialVersionUID = 5030896109219651988L;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Override
    public String getCacheByRank(Rank rank) throws UndefinedTransformerMethodException {
        if (rank == null) {
            return null;
        }
        //see https://list.worldfloraonline.org/ranks.php
        if (rank.equals(Rank.FAMILY())) {return "family";}
        else if(rank.equals(Rank.SUBFAMILY())) {return "subfamily";}
        else if(rank.equals(Rank.SUPERTRIBE())) {return "supertribe";}
        else if(rank.equals(Rank.TRIBE())) {return "tribe";}
        else if(rank.equals(Rank.SUBTRIBE())) {return "subtribe";}
        else if(rank.equals(Rank.GENUS())) {return "genus";}
        else if(rank.equals(Rank.SUBGENUS())) {return "subgenus";}
        else if(rank.equals(Rank.SECTION_BOTANY())) {return "section";}
        else if(rank.equals(Rank.SUBSECTION_BOTANY())) {return "subsection";}
        else if(rank.equals(Rank.SERIES())) {return "series";}
        else if(rank.equals(Rank.SUBSERIES())) {return "subseries";}
        else if(rank.equals(Rank.SPECIES())) {return "species";}
        else if(rank.equals(Rank.SUBSPECIES())) {return "subspecies";}
        else if(rank.equals(Rank.PROLES())) {return "prole";}
        else if(rank.equals(Rank.VARIETY())) {return "variety";}
        else if(rank.equals(Rank.SUBVARIETY())) {return "subvariety";}
        else if(rank.equals(Rank.FORM())) {return "form";}
        else if(rank.equals(Rank.SUBFORM())) {return "subform";}
        else if(rank.equals(Rank.LUSUS())) {return "lusus";}
        //ranks higher than family
        else if (rank.equals(Rank.KINGDOM())) {return "kingdom";}
        else if (rank.equals(Rank.SUBKINGDOM())) {return "subkingdom";}
        else if (rank.equals(Rank.PHYLUM())) {return "phylum";}
        else if (rank.equals(Rank.CLASS())) {return "class";}
        else if (rank.equals(Rank.SUBCLASS())) {return "subclass";}
        else if (rank.equals(Rank.SUPERORDER())) {return "superorder";}
        else if (rank.equals(Rank.ORDER())) {return "order";}
        else if (rank.equals(Rank.SUBORDER())) {return "suborder";}
        //ranks from https://about.worldfloraonline.org/images/uploads/documents/WFOGuidelinesForTaxonomicBackboneContributorsV._2.06.pdf
        else if(rank.equals(Rank.INFRASPECIFICTAXON())) {return "infraspecificName";}
        //... and similar
        else if(rank.equals(Rank.INFRAGENERICTAXON())) {return "infragenericName";}
        //others
        else if(rank.equals(Rank.SUBPROLES())) {return "subproles";}
        else if(rank.equals(Rank.GREX_INFRASPEC())) {return "grex";}
        else if(rank.equals(Rank.RACE())) {return "race";}
        else if(rank.equals(Rank.SUBLUSUS())) {return "sublusus";}
        else {
            return null;
        }
    }
}