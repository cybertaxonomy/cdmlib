/**
* Copyright (C) 2023 EDIT
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
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * Transformer for WFO Content export.
 *
 * @author a.mueller
 * @date 2024-01-30
 */
public class WfoContentExportTransformer extends ExportTransformerBase {

    private static final long serialVersionUID = -527652844010832994L;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Override
    public String getCacheByNomStatus(NomenclaturalStatusType nomStatusType) {
        if (nomStatusType == null) {
            return null;
        //specific status
        }else if (nomStatusType.equals(NomenclaturalStatusType.CONSERVED())) {
            return "conserved";
        }else if (nomStatusType.equals(NomenclaturalStatusType.REJECTED())) {
            return "rejected";
        }else if (nomStatusType.equals(NomenclaturalStatusType.DOUBTFUL())) {
            return "doubtful";
        }else if (nomStatusType.equals(NomenclaturalStatusType.INED())) {
            return "manuscript";
        //general status
        }else if (nomStatusType.isLegitimate()) {
            return "acceptable";
        }else if (nomStatusType.isIllegitimate()) {
            return "nomen illegitimum";
        }else if (nomStatusType.isInvalid()) {
            return "not established";
        }

        return null;
    }

    @Override
    public String getCacheByRank(Rank rank) throws UndefinedTransformerMethodException {
        if (rank == null) {
            return null;
        }
        if (rank.equals(Rank.FAMILY())) {return "family";}
        else if(rank.equals(Rank.SUBFAMILY())) {return "subfamily";}
        else if(rank.equals(Rank.TRIBE())) {return "tribe";}
        else if(rank.equals(Rank.SUBTRIBE())) {return "subtribe";}
        else if(rank.equals(Rank.GENUS())) {return "genus";}
        else if(rank.equals(Rank.SUBGENUS())) {return "subgenus";}
        else if(rank.equals(Rank.SPECIES())) {return "species";}
        else if(rank.equals(Rank.SUBSPECIES())) {return "subspecies";}
        else if(rank.equals(Rank.VARIETY())) {return "variety";}
        else if(rank.equals(Rank.SUBVARIETY())) {return "subvariety";}
        else if(rank.equals(Rank.FORM())) {return "form";}
        else if(rank.equals(Rank.SUBFORM())) {return "subform";}
        else if(rank.equals(Rank.INFRASPECIFICTAXON())) {return "infraspecificName";}
        return null;
    }
}