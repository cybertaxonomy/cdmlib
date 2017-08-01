/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.occurrences;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author pplitzner
 * @date Nov 30, 2015
 *
 */
public class SpecimenOrObservationBaseFormatter extends IdentifiableEntityFormatter {

    public SpecimenOrObservationBaseFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object) {
        super.initFormatKeys(object);
        SpecimenOrObservationBase<?> specimenOrObservationBase = (SpecimenOrObservationBase<?>)object;
        if(specimenOrObservationBase.getRecordBasis()!=null){
            formatKeyMap.put(FormatKey.RECORD_BASIS, specimenOrObservationBase.getRecordBasis().toString());
        }
        if(specimenOrObservationBase.getKindOfUnit()!=null){
            formatKeyMap.put(FormatKey.KIND_OF_UNIT, specimenOrObservationBase.getKindOfUnit().toString());
        }
    }

}
