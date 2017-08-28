/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.hibernate.search;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

import org.hibernate.search.bridge.StringBridge;

public class PartialBridge implements StringBridge {

	@Override
    public String objectToString(Object object) {
		if(object != null) {
			LocalDate partial = ((LocalDate)object);
			StringBuilder stringBuilder = new StringBuilder();
			if(partial.isSupported(ChronoField.YEAR)) {
			    stringBuilder.append(partial.get(ChronoField.YEAR));

			    if(partial.isSupported(ChronoField.MONTH_OF_YEAR)) {
				    stringBuilder.append(partial.get(ChronoField.MONTH_OF_YEAR));

				    if(partial.isSupported(ChronoField.DAY_OF_MONTH)) {
					    stringBuilder.append(partial.get(ChronoField.DAY_OF_MONTH));
 				    }
			    }
		        return stringBuilder.toString();
			}
		}
		return null;
	}

}
