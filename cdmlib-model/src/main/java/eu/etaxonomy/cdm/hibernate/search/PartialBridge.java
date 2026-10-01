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
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

public class PartialBridge implements ValueBridge<Partial, String> {

	@Override
    public String toIndexedValue(Partial value, ValueBridgeToIndexedValueContext context) {
		if(value == null || !value.isSupported(DateTimeFieldType.year())) {
		    return null;
		}
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(value.get(DateTimeFieldType.year()));
		if(value.isSupported(DateTimeFieldType.monthOfYear())) {
		    stringBuilder.append(value.get(DateTimeFieldType.monthOfYear()));
		    if(value.isSupported(DateTimeFieldType.dayOfYear())) {
			    stringBuilder.append(value.get(DateTimeFieldType.dayOfYear()));
		    }
		}
		return stringBuilder.toString();
	}

    @Override
    public String parse(String value) {
        return value;
    }
}
