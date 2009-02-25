package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import net.sf.dozer.util.mapping.MappingException;
import net.sf.dozer.util.mapping.converters.CustomConverter;

import org.joda.time.DateTime;

public class DateTimeConverter implements CustomConverter {

	public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
		if (source == null) {
			return null;
		}
		if (source instanceof DateTime) {		      
			return new DateTime(((DateTime)source)); 
		} else {
			throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
