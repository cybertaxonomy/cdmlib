package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class IsoDateTimeEditor extends PropertyEditorSupport {

	private static DateTimeFormatter iso8601Format = DateTimeFormatter.ISO_DATE_TIME;

	@Override
    public void setAsText(String text) {
		setValue(iso8601Format.parse(text));
	}

	@Override
    public String getAsText() {
		return ((ZonedDateTime)getValue()).format(iso8601Format);
	}

}
