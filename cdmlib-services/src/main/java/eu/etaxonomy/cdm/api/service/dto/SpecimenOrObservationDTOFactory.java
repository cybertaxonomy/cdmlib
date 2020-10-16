/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * Factory for all SpecimenOrObservationBase related DTOs.
 * <p>
 * Internally this class delegates to the factory methods of the specific DTO implementations.
 *
 * @author a.kohlbecker
 * @since Oct 14, 2020
 */
public class SpecimenOrObservationDTOFactory {

    public static SpecimenOrObservationBaseDTO fromEntity(SpecimenOrObservationBase entity) {
        if(entity == null) {
            return null;
        }
        if (entity.isInstanceOf(FieldUnit.class)) {
            return FieldUnitDTO.fromEntity((FieldUnit) entity);
        } else {
            if (entity.isInstanceOf(DnaSample.class)){
                return new DNASampleDTO((DnaSample)entity); // FIXME use factory method
            } else {
                return DerivedUnitDTO.fromEntity((DerivedUnit) entity);
            }
        }
    }

    public static FieldUnitDTO fromFieldUnit(FieldUnit entity){
        if(entity == null) {
            return null;
        }
        return FieldUnitDTO.fromEntity(entity);
    }

    public static DerivedUnitDTO fromDerivedUnit(DerivedUnit entity){
        if(entity == null) {
            return null;
        }
        return DerivedUnitDTO.fromEntity(entity);
    }

}
