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
import org.joda.time.format.DateTimeFormatterBuilder;

public class DateTimeEditor extends PropertyEditorSupport {

	private static DateTimeFormatter parser;
	private static DateTimeFormatter printer;

	static {
		parser = new DateTimeFormatterBuilder().appendPattern("dd/MM/YYYY").appendOptional(new DateTimeFormatterBuilder().appendPattern(" HH:mm:ss").toParser()).toFormatter();
		printer = new DateTimeFormatterBuilder().appendPattern("dd/MM/YYYY HH:mm:ss").toFormatter();
	}

	@Override
    public void setAsText(String text) {
		setValue(parser.parseDateTime(text));
	}

	@Override
    public String getAsText() {
		return printer.print((DateTime)getValue());
	}
}