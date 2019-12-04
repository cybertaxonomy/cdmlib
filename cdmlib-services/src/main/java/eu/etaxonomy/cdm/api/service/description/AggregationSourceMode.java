/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.IKeyTerm;

public enum AggregationSourceMode implements IKeyTerm{
    NONE("NO", "None", true, true),
    ALL("ALL", "All sources", true, true),
    ALL_SAMEVALUE("ALSV", "All sources with highest status", true, true),
    DESCRIPTION("DESC","Link to underlying description", true, true),
    TAXON("TAX","Link to child taxon", false, true);

    private String key;
    private String message;
    private boolean isWithinTaxon;
    private boolean isToParent;

    private AggregationSourceMode(String key, String message, boolean isWithinTaxon, boolean isToParent) {
        this.key = key;
        this.message = message;
        this.isWithinTaxon = isWithinTaxon;
        this.isToParent = isToParent;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getMessage(Language language) {
        //TODO i18n not yet implemented for AggregationMode
        return message;
    }

    /**
     * Returns a list of {@link AggregationSourceMode}s available for the
     * given {@link AggregationMode} and {@link AggregationType}
     */
    public static List<AggregationSourceMode> list(AggregationMode aggregationMode, AggregationType type){
        //TODO currently aggType is not yet used as all source modes are available for all aggTypes
        List<AggregationSourceMode> result = new ArrayList<>();
        for(AggregationSourceMode mode : values()){
            if (aggregationMode == AggregationMode.WithinTaxon && mode.isWithinTaxon){
                result.add(mode);
            }else if (aggregationMode == AggregationMode.ToParent && mode.isToParent){
                result.add(mode);
            }
        }
        return result;
    }
 }