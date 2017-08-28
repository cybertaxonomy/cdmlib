/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler.converter;


import java.time.ZonedDateTime;

import org.dozer.CustomConverter;
import org.dozer.MappingException;

import eu.etaxonomy.cdm.model.common.TimePeriod;

public class TimePeriodConverter implements CustomConverter {

    @Override
	public Object convert(Object existingDestinationFieldValue, Object source, Class<?> destClass, Class<?> sourceClass) {

		if (source == null) {
			return null;
		}
		if (source instanceof TimePeriod) {

			//convert from TimePeriod -> DateTime
			//FIXME .toDateTime(null) most probably not correct
			if(((TimePeriod)source).getStart() != null){
				return ((TimePeriod)source).getStart();
			} else {
				return null;
			}

		} else if (source instanceof ZonedDateTime) {

			//convert from DateTime -> TimePeriod
			//FIXME implement
			return null;

		} else {

			throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
					+ existingDestinationFieldValue + " and " + source);
		}
	}

}
