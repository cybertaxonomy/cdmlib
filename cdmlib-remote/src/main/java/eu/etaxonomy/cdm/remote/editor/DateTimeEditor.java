package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DateTimeEditor extends PropertyEditorSupport {

	private static DateTimeFormatter parser;
	private static DateTimeFormatter printer;

	static {
		parser = new DateTimeFormatterBuilder().appendPattern("dd/MM/YYYY").appendOptional(new DateTimeFormatterBuilder().appendPattern(" HH:mm:ss").toFormatter()).toFormatter();
		printer = new DateTimeFormatterBuilder().appendPattern("dd/MM/YYYY HH:mm:ss").toFormatter();
	}

	@Override
    public void setAsText(String text) {
		setValue(parser.parse(text));
	}

	@Override
    public String getAsText() {
		return ((ZonedDateTime)getValue()).format(printer);
	}

}
