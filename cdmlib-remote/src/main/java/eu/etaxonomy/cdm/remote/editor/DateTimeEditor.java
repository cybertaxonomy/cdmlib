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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.joda.time.format.DateTimeFormatterBuilder;

@Deprecated  //we do not use joda.time.DateTime anymore and therefore
//this will be removed in near future
public class DateTimeEditor extends PropertyEditorSupport {

	private static org.joda.time.format.DateTimeFormatter parser;
	private static DateTimeFormatter formatter;

	static {
		parser = new DateTimeFormatterBuilder().appendPattern("dd/MM/YYYY")
		        .appendOptional(new DateTimeFormatterBuilder().appendPattern(" HH:mm:ss").toParser())
		        .toFormatter();
		formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	}

	@Deprecated
    @Override
    public void setAsText(String text) {
		setValue(parser.parseDateTime(text));
	}

	@Deprecated
    @Override
    public String getAsText() {
		return formatter.format((ZonedDateTime)getValue());
	}
}