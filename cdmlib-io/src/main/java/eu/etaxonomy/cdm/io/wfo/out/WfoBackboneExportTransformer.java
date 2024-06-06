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

import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;

/**
 * Transformer for WFO Backbone export.
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/10446
 *
 * @author a.mueller
 * @date 2023-12-08
 */
public class WfoBackboneExportTransformer extends WfoExportTransformerBase {

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
}