/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class ZonedDateTimeEditor extends PropertyEditorSupport {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

	private static DateTimeFormatter parser;
	private static DateTimeFormatter formatter;

	static {
		parser = new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy")
		        .optionalStart()
		        .appendPattern(" HH:mm:ss")
		        .optionalEnd()
		        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
		        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		        .toFormatter()
		        .withZone(DEFAULT_ZONE);
		formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	}

	@Override
    public void setAsText(String text) {
		setValue(ZonedDateTime.parse(text, parser));
	}

	@Override
    public String getAsText() {
		return formatter.format((ZonedDateTime)getValue());
	}
}