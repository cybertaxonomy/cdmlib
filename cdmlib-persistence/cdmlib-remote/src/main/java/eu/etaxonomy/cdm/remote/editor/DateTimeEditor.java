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
	
	public void setAsText(String text) {
		setValue(parser.parseDateTime(text));
	}
	
	public String getAsText() {
		return printer.print((DateTime)getValue());
	}

}
