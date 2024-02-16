/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.compare;

import java.util.Comparator;

import eu.etaxonomy.cdm.api.dto.DerivedUnitDTO;
import eu.etaxonomy.cdm.api.dto.FieldUnitDTO;
import eu.etaxonomy.cdm.api.dto.SpecimenOrObservationBaseDTO;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class OccurrenceDtoComparator implements Comparator<SpecimenOrObservationBaseDTO<?>> {

    private static final OccurrenceDtoComparator instance = new OccurrenceDtoComparator();

    public static final OccurrenceDtoComparator INSTANCE() {
        return instance;
    }

    @Override
    public int compare(SpecimenOrObservationBaseDTO<?> o1, SpecimenOrObservationBaseDTO<?> o2) {
        if(o1 instanceof FieldUnitDTO && o2 instanceof FieldUnitDTO) {
            FieldUnitDTO fu1 = (FieldUnitDTO)o1;
            FieldUnitDTO fu2 = (FieldUnitDTO)o2;
            //TODO if we want null values and values with missing year first we should set this to true
            boolean nullFirst = false;
            return fu1.compareByTimePeriod(fu2, nullFirst);
        }
        if(o1 instanceof DerivedUnitDTO && o2 instanceof DerivedUnitDTO) {
            SpecimenOrObservationBaseDTO<?> du1 = o1;
            SpecimenOrObservationBaseDTO<?> du2 = o2;
            return du1.compareTo(du2);
         }
        if(o1 instanceof FieldUnitDTO && o2 instanceof DerivedUnitDTO) {
            return -1;
        } else {
            return 1;
        }
    }


}
