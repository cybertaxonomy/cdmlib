/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

import eu.etaxonomy.cdm.model.common.Partial;

public class PartialBridge implements ValueBridge<Partial, String> {

	@Override
    public String toIndexedValue(Partial value, ValueBridgeToIndexedValueContext context) {
		if(value == null || !value.hasYear()) {
		    return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(value.getYear());
		if(value.hasMonth()) {
		    stringBuilder.append(value.getMonth());
		    if(value.hasDay()) {
			    stringBuilder.append(value.getDay());
		    }
		}
		return stringBuilder.toString();
	}

    @Override
    public String parse(String value) {
        return value;
    }
}
