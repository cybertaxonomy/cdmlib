package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class IsoDateTimeEditor extends PropertyEditorSupport {

	private static DateTimeFormatter iso8601Format = ISODateTimeFormat.dateTime();
	
	public void setAsText(String text) {
		setValue(iso8601Format.parseDateTime(text));
	}
	
	public String getAsText() {
		return iso8601Format.print((DateTime)getValue());
	}

}
