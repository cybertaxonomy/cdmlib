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
import java.util.Arrays;
import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.IKeyTerm;

public enum AggregationMode implements IKeyTerm{
        WithinTaxon("INTAX", "Within taxon", true, true),
        ToParent("TOPAR", "From children to this taxon", true, true);
//        public boolean isByRank() {
//           return this==byRanks || this == byAreasAndRanks;
//        }
//        public boolean isByArea() {
//            return this==byAreas || this == byAreasAndRanks;
//         }

    private String key;
    private String label;
    private boolean supportsDistribution;
    private boolean supportsDescriptiveData;

    private AggregationMode(String key, String message,
            boolean supportsDistribution, boolean supportsDescriptiveData) {

        this.key = key;
        this.label = message;
        this.supportsDistribution = supportsDistribution;
        this.supportsDescriptiveData = supportsDescriptiveData;
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

    public boolean isSupportsDistribution() {
        return supportsDistribution;
    }

    public boolean isSupportsDescriptiveData() {
        return supportsDescriptiveData;
    }

    public static List<AggregationMode> list(AggregationType type){
        List<AggregationMode> result = new ArrayList<>();
        for(AggregationMode mode : values()){
            if (type == AggregationType.Distribution && mode.supportsDistribution){
                result.add(mode);
            }else if (type == AggregationType.StructuredDescription && mode.supportsDescriptiveData){
                result.add(mode);
            }
        }
        return result;
    }

    public static List<AggregationMode> byWithinTaxonAndToParent(){
        return Arrays.asList(new AggregationMode[]{AggregationMode.WithinTaxon, AggregationMode.ToParent});
    }
    public static List<AggregationMode> byWithinTaxon(){
        return Arrays.asList(new AggregationMode[]{AggregationMode.WithinTaxon});
    }
    public static List<AggregationMode> byToParent(){
        return Arrays.asList(new AggregationMode[]{AggregationMode.ToParent});
    }

}