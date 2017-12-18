/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;

/**
 * @author a.kohlbecker
 * @since Dec 14, 2017
 *
 */
public class DerivedUnitConverterFactory {

    private static IOccurrenceService service;

    @Autowired
    public void setIOccurrenceService(IOccurrenceService service){
        DerivedUnitConverterFactory.service = service;
    }

    public static <TARGET extends DerivedUnit> DerivedUnitConverter<TARGET> createDerivedUnitConverter(DerivedUnit derivedUnit, Class<TARGET> targetType){
        DerivedUnitConverter<TARGET> converter = new DerivedUnitConverter<TARGET>(derivedUnit, service);
        return converter;
    }


}
