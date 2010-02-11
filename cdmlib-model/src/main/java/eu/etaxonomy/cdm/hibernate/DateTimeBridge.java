/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.hibernate;

import org.hibernate.search.bridge.StringBridge;
import org.joda.time.DateTime;

public class DateTimeBridge implements StringBridge {

	public String objectToString(Object object) {
		if(object != null) {
			DateTime dateTime = ((DateTime)object);
			return dateTime.toString();
		}
		return null;
	}

}
