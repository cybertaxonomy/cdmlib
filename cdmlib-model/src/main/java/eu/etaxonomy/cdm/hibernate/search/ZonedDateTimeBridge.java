/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.hibernate.search;

import java.time.ZonedDateTime;

import org.hibernate.search.bridge.StringBridge;

public class ZonedDateTimeBridge implements StringBridge {

	@Override
    public String objectToString(Object object) {
		if(object != null) {
			ZonedDateTime dateTime = ((ZonedDateTime)object);
			return dateTime.toString();
		}
		return null;
	}

}
