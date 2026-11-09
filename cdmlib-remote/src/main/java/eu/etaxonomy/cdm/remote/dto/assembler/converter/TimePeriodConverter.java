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

import org.joda.time.Partial;

import com.github.dozermapper.core.CustomConverter;
import com.github.dozermapper.core.MappingException;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.TimePeriodBase;

public class TimePeriodConverter implements CustomConverter {

    @Override
	public Object convert(Object existingDestinationFieldValue, Object source, Class<?> destClass, Class<?> sourceClass) {

		if (source == null) {
			return null;
		}
		// datePublished is VerbatimTimePeriod (TimePeriodBase), not only TimePeriod
		if (source instanceof TimePeriodBase) {

			//convert from TimePeriodBase -> ZonedDateTime
			//FIXME null as default most probably not correct
		    Partial start = ((TimePeriodBase)source).getStart();
			if(start != null){
			    return TimePeriod.zonedDateTimeFromPartial(start, null);
			} else {
				return null;
			}

		} else if (source instanceof ZonedDateTime) {

			//convert from ZonedDateTime -> TimePeriod
			//FIXME implement
			return null;
		} else {

			throw new MappingException("TimePeriodConverter used incorrectly. Arguments passed in were:"
					+ existingDestinationFieldValue + " and " + source);
		}
	}
}