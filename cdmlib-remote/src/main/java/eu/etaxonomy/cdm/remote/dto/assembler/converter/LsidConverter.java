package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import java.net.URI;
import java.net.URISyntaxException;

import net.sf.dozer.util.mapping.MappingException;
import net.sf.dozer.util.mapping.converters.CustomConverter;
import eu.etaxonomy.cdm.model.common.LSID;

public class LsidConverter implements CustomConverter {

	public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
		if (source == null) {
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
