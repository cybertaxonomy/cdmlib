package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import java.net.URI;
import java.net.URISyntaxException;

import net.sf.dozer.util.mapping.MappingException;
import net.sf.dozer.util.mapping.converters.CustomConverter;

public class PreferLsidToUriConverter implements CustomConverter {

	public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
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
