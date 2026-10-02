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

import eu.etaxonomy.cdm.common.DateTimeUtil;

public class IsoDateTimeEditor extends PropertyEditorSupport {

	private static DateTimeFormatter iso8601Formatter = DateTimeUtil.DATE_TIME_FORMATTER;

	@Override
    public void setAsText(String text) {
		setValue(iso8601Formatter.parse(text));
	}

	@Override
    public String getAsText() {
		return ((ZonedDateTime)getValue()).format(iso8601Formatter);
	}
}