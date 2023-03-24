/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.ServletContextAware;

import com.github.dozermapper.core.CustomConverter;
import com.github.dozermapper.core.MappingException;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;

public class IdentifierConverter implements CustomConverter, ServletContextAware{

    private static final Logger logger = LogManager.getLogger();

    private ServletContext servletContext;

	@Override
	public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (source == null) {
			return null;
		}
		if (source instanceof IdentifiableEntity) {
			IdentifiableEntity<?> identifiableEntity = (IdentifiableEntity<?>)source;
			URI uri = null;
			try {
				if(identifiableEntity.getLsid() != null && identifiableEntity.getLsid().getLsid() != null){
					uri = new URI(((LSID)source).getLsid());
				} else {
					StringBuilder stringBuilder = new StringBuilder();
					if(servletContext != null){
						stringBuilder.append(servletContext.getContextPath()).append('/');
					} else {
						//happens only during testing
						logger.warn("No ServletContext configured in Unitils");
					}
					String[] classNameTokens = StringUtils.split(source.getClass().getName(), '.');
					stringBuilder.append(classNameTokens[classNameTokens.length - 2]).append('/');
					stringBuilder.append(identifiableEntity.getUuid().toString());
					uri = new URI(stringBuilder.toString());
				}
			} catch (URISyntaxException e) {
				throwMappingException(destination, source);
			}
			if(uri != null){
				if(String.class.isAssignableFrom(destClass)){
					return uri.toString();
				} else if(URL.class.isAssignableFrom(destClass)){
					try {
						return uri.toURL();
					} catch (MalformedURLException e) {
						logger.error(e);
					}
				} else if(URI.class.isAssignableFrom(destClass)){
					return uri;
				} else {
					throwMappingException(destination, source);
				}
			}
		} else {
			throwMappingException(destination, source);
		}
		return null;
	}

	private void throwMappingException(Object destination, Object source) {
		throw new MappingException("Converter TestCustomConverter used incorrectly. Arguments passed in were:"
				+ destination + " and " + source);
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext =  servletContext;
	}
}