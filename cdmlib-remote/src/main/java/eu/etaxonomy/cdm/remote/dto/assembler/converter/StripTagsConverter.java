/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.dozer.CustomConverter;
import org.dozer.MappingException;


public class StripTagsConverter implements CustomConverter {

	public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (source == null) {
			return null;
		}
		if (source instanceof String) {		      
			Reader reader = new RemoveHTMLReader(new StringReader((String)source));
			StringBuilder stringBuilder = new StringBuilder();
			int charValue;
			try {
				while ((charValue = reader.read()) != -1) {
				    stringBuilder.append((char)charValue);
				}
			} catch (IOException e) {return e.toString();}
			return stringBuilder.toString();
		} else {
			throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
