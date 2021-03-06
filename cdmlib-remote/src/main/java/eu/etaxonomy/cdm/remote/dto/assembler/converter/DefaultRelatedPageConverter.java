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
import java.util.UUID;

import com.github.dozermapper.core.ConfigurableCustomConverter;

import eu.etaxonomy.cdm.remote.dto.dc.Relation;

/**
 * Default page converter implementation that makes a gross assumption that
 * the metadata are being served on the same server as the human-readable page,
 * and that the urls map to {relatedPagePrefix}{param}/{uuid}
 *
 * @author ben.clark
 */
public class DefaultRelatedPageConverter implements ConfigurableCustomConverter  {

	private String relatedPagePrefix = "/";

	public void setRelatedPagePrefix(String relatedPagePrefix) {
		this.relatedPagePrefix = relatedPagePrefix;
	}

	private String parameter = null;

	@Override
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Override
    public Object convert(Object existingDestinationFieldValue,	Object sourceFieldValue,
            Class<?> destinationClass, Class<?> sourceClass) {
		if(sourceFieldValue == null) {
			return null;
		}
		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(relatedPagePrefix);

		stringBuffer.append(parameter + "/");
		stringBuffer.append(((UUID)sourceFieldValue).toString());

		Relation relation = new Relation();
		try {
			relation.setResource(new URI(stringBuffer.toString()));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return relation;
	}
}