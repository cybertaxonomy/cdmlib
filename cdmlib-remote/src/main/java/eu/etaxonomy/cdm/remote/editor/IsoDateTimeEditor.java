/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class IsoDateTimeEditor extends PropertyEditorSupport {

	private static DateTimeFormatter iso8601Format = ISODateTimeFormat.dateTime();

	@Override
    public void setAsText(String text) {
		setValue(iso8601Format.parseDateTime(text));
	}

	@Override
    public String getAsText() {
		return iso8601Format.print((DateTime)getValue());
	}
}