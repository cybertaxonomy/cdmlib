package eu.etaxonomy.cdm.io.common;

import java.io.Serializable;

public class CsvIOConfigurator implements Serializable{

    private static final long serialVersionUID = -8456333170716346247L;

    private String encoding = "UTF-8";
	private String linesTerminatedBy = "\r\n";
	private String fieldsEnclosedBy = "\"";
	private boolean hasHeaderLines = true;
	private String fieldsTerminatedBy=";";

	public static CsvIOConfigurator NewInstance(){

		return new CsvIOConfigurator();

	}

	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getLinesTerminatedBy() {
		return linesTerminatedBy;
	}
	public void setLinesTerminatedBy(String linesTerminatedBy) {
		this.linesTerminatedBy = linesTerminatedBy;
	}
	public String getFieldsEnclosedBy() {
		return fieldsEnclosedBy;
	}
	public void setFieldsEnclosedBy(String fieldsEnclosedBy) {
		this.fieldsEnclosedBy = fieldsEnclosedBy;
	}
	public boolean isHasHeaderLines() {
		return hasHeaderLines;
	}
	public void setHasHeaderLines(boolean hasHeaderLines) {
		this.hasHeaderLines = hasHeaderLines;
	}
	public String getFieldsTerminatedBy() {
		return fieldsTerminatedBy;
	}
	public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
		this.fieldsTerminatedBy = fieldsTerminatedBy;
	}



}
