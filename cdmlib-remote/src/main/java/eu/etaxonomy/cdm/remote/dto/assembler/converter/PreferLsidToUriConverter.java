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

import com.github.dozermapper.core.CustomConverter;
import com.github.dozermapper.core.MappingException;

/**
 * @author ben.clark
 * @since 2009-02-25
 */
public class PreferLsidToUriConverter implements CustomConverter {

	@Override
    public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (destination == null) {
			if(source != null) {
				try {
					return new URI((String)source);
				} catch (URISyntaxException e) {
					throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
							+ destination + " and " + source);
				}
			} else {
				return null;
			}
		} else {
		    return destination;
		}
	}
}