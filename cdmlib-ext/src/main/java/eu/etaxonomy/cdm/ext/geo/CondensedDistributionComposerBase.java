// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;

/**
 * Base class for Distribution Composers
 * @author a.mueller
 * @date 02.06.2016
 *
 */
public abstract class CondensedDistributionComposerBase implements ICondensedDistributionComposer{

    protected static Map<UUID, String> statusSymbols;

    /**
     * @param status
     * @return
     */
    protected String statusSymbol(PresenceAbsenceTerm status) {
        if(status == null) {
            return "";
        }
        String symbol = statusSymbols.get(status.getUuid());
        if(symbol != null) {
            return symbol;
        }else if (status.getSymbol() != null){
            return status.getSymbol();
        }else if (status.getIdInVocabulary() != null){
            return status.getIdInVocabulary();
        }else {
            Representation r = status.getPreferredRepresentation((Language)null);
            if (r != null){
                String abbrevLabel = r.getAbbreviatedLabel();
                if (abbrevLabel != null){
                    return abbrevLabel;
                }
            }
        }

        return "n.a.";
    }

    /**
     * @param status
     * @return
     */
    private String statusSymbolEuroMedOld(PresenceAbsenceTerm status) {
        if(status == null) {
            return "";
        }
        String symbol = statusSymbols.get(status.getUuid());
        if(symbol == null) {
            symbol = "";
        }
        return symbol;
    }

}
