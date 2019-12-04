/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.util.Arrays;
import java.util.List;

public enum AggregationMode {
        WithinTaxon,
        ToParent;
//        public boolean isByRank() {
//           return this==byRanks || this == byAreasAndRanks;
//        }
//        public boolean isByArea() {
//            return this==byAreas || this == byAreasAndRanks;
//         }


        public static List<AggregationMode> byAreasAndRanks(){
            return Arrays.asList(new AggregationMode[]{AggregationMode.WithinTaxon, AggregationMode.ToParent});
        }
        public static List<AggregationMode> byAreas(){
            return Arrays.asList(new AggregationMode[]{AggregationMode.WithinTaxon});
        }
        public static List<AggregationMode> byRanks(){
            return Arrays.asList(new AggregationMode[]{AggregationMode.ToParent});
        }

    }