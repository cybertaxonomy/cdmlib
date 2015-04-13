package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.ByteArrayOutputStream;
import java.io.File;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoExport;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoExportState;

public class CsvNameExportConfigurator extends XmlExportConfiguratorBase<CsvNameExportState>{
	
	protected CsvNameExportConfigurator(File destination,
			ICdmDataSource cdmSource, IExportTransformer transformer) {
		super(destination, cdmSource, transformer);
		// TODO Auto-generated constructor stub
	}
	
	public static CsvNameExportConfigurator NewInstance(ICdmDataSource source, File destinationFolder){
		
		return new CsvNameExportConfigurator(destinationFolder,source, null);
		
	}
	
	private ByteArrayOutputStream byteOutputStream;
	
	public ByteArrayOutputStream getByteOutputStream() {
		return byteOutputStream;
	}

	public void setByteOutputStream(ByteArrayOutputStream byteOutputStream) {
		this.byteOutputStream = byteOutputStream;
	}

	private String encoding = "UTF-8";
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
	private String linesTerminatedBy = "\r\n";
	private String fieldsEnclosedBy = "\"";
	private boolean hasHeaderLines = true;
	private String fieldsTerminatedBy=";";
	@Override
	public CsvNameExportState getNewState() {
		return new CsvNameExportState(this);
	}
	@Override
	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				CsvNameExportBase.class
		};
	}

}
