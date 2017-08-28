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
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.dozer.CustomConverter;
import org.dozer.MappingException;


public class DateTimeConverter implements CustomConverter {

	@Override
    public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
		if (source == null) {
			return null;
		}
		Object result = null;
		if (source instanceof ZonedDateTime) {
			if(destClass.equals(ZonedDateTime.class)){
				result =  source;
			} else if(destClass.equals(XMLGregorianCalendar.class)){
				result = dataTypeFactory().newXMLGregorianCalendar( GregorianCalendar.from((ZonedDateTime)source)); //naive approach, may mot result in correct representation of partial datetime
			}
		}

		if(result == null){
			throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
		return result;
	}

	  /**
	   * Cache the DatatypeFactory because newInstance is very expensive.
	   */
	  private static DatatypeFactory dataTypeFactory;

	/**
	   * Returns a new instance of DatatypeFactory, or the cached one if previously created.
	   *
	   * @return instance of DatatypeFactory
	   */
	  private static DatatypeFactory dataTypeFactory() {
	    if (dataTypeFactory == null) {
	      try {
	        dataTypeFactory = DatatypeFactory.newInstance();
	      } catch (DatatypeConfigurationException e) {
	        throw new MappingException(e);
	      }
	    }
	    return dataTypeFactory;
	  }

}
