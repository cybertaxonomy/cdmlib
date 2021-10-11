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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.IKeyTerm;

public enum AggregationSourceMode implements IKeyTerm{
    NONE("NO", "None",           true , true, false, true, true ),
    ALL("ALL", "All sources",    true , true, true , true, false), //for now we do not support structured descriptions, may change in future
    ALL_SAMEVALUE("ALSV", "All sources with highest status",
                                 true , true, true , true, false), //does not make sense for struc. descriptions
    DESCRIPTION("DESC","Link to underlying description",
                                 true , true, false, true, true ),  //probably not really supported for distributions yet, at least not for "within taxon"
    TAXON("TAX","Link to child taxon",
                                 false, true, false, true, true );

    final private String key;
    final private String label;
    final private boolean supportsWithinTaxon;
    final private boolean supportsToParent;
    final private boolean supportsOriginalSourceType;
    final private EnumSet<AggregationType> supportedAggregationTypes;

    private AggregationSourceMode(String key, String message,
            boolean supportsWithinTaxon, boolean supportsToParent,
            boolean supportsOriginalSourceType,
            boolean supportsDistribution, boolean supportsStructuredDescription) {

        this.key = key;
        this.label = message;
        this.supportsWithinTaxon = supportsWithinTaxon;
        this.supportsToParent = supportsToParent;
        this.supportsOriginalSourceType = supportsOriginalSourceType;
        Set<AggregationType> aggTypes = new HashSet<>();
        if (supportsDistribution){
            aggTypes.add(AggregationType.Distribution);
        }
        if (supportsStructuredDescription){
            aggTypes.add(AggregationType.StructuredDescription);
        }
        this.supportedAggregationTypes = EnumSet.copyOf(aggTypes);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getLabel(Language language) {
        //TODO i18n not yet implemented for AggregationMode
        return label;
    }

    public boolean isSupportsWithinTaxon() {
        return supportsWithinTaxon;
    }

    public boolean isSupportsToParent() {
        return supportsToParent;
    }

    public boolean isSupportsOriginalSourceType(){
        return supportsOriginalSourceType;
    }

    public boolean isNone(){
        return this == NONE;
    }

    public boolean isTaxon(){
        return this == TAXON;
    }

    /**
     * Returns a list of {@link AggregationSourceMode}s available for the
     * given {@link AggregationMode} and {@link AggregationType}
     */
    public static List<AggregationSourceMode> list(AggregationMode aggregationMode, AggregationType type){
        List<AggregationSourceMode> result = new ArrayList<>();
        for(AggregationSourceMode mode : values()){
            if (type != null && !mode.supportedAggregationTypes.contains(type)){
                continue;
            }
            if (aggregationMode == AggregationMode.WithinTaxon && mode.supportsWithinTaxon){
                result.add(mode);
            }else if (aggregationMode == AggregationMode.ToParent && mode.supportsToParent){
                result.add(mode);
            }
        }
        return result;
    }
 }