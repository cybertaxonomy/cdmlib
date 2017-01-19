/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import java.net.URI;
import java.net.URISyntaxException;

import org.dozer.CustomConverter;
import org.dozer.MappingException;

import eu.etaxonomy.cdm.model.common.LSID;

public class LsidConverter implements CustomConverter {

	public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
		if (source == null || ((LSID)source).toString() == null || ((LSID)source).toString().equals("")) {
			return null;
		}
		if (source instanceof LSID) {		      
				try {
					return new URI(((LSID)source).getLsid());
				} catch (URISyntaxException e) {
					throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
							+ destination + " and " + source);
				}
		} else {
			throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
