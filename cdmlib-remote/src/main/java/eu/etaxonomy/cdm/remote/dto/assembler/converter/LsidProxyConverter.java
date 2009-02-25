package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import net.sf.dozer.util.mapping.MappingException;
import net.sf.dozer.util.mapping.converters.CustomConverter;
import eu.etaxonomy.cdm.model.common.LSID;

public class LsidProxyConverter implements CustomConverter {

	private String lsidProxyServiceUrl;
	
	public void setLsidProxyServiceUrl(String lsidProxyServiceUrl) {
		this.lsidProxyServiceUrl = lsidProxyServiceUrl;
	}

	public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
		if (source == null) {
			return null;
		}
		String dest = null;
		if (source instanceof LSID) {		      
			dest = this.lsidProxyServiceUrl + ((LSID)source).getLsid();
			return dest;
		} else {
			throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
