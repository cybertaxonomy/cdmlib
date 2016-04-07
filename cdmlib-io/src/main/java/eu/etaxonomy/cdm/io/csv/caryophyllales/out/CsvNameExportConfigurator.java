package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.name.Rank;


public class CsvNameExportConfigurator extends XmlExportConfiguratorBase<CsvNameExportState>{

    private static final long serialVersionUID = 1L;


    private ByteArrayOutputStream byteOutputStream;
	private String encoding = "UTF-8";
	private String linesTerminatedBy = "\r\n";
	private String fieldsEnclosedBy = "\"";
	private boolean hasHeaderLines = true;
	private String fieldsTerminatedBy=";";
	private boolean namesOnly = false;
	private UUID classificationUUID;

	private Rank rank = Rank.GENUS();

	protected CsvNameExportConfigurator(File destination,
			ICdmDataSource cdmSource, IExportTransformer transformer) {
		super(destination, cdmSource, transformer);
		// TODO Auto-generated constructor stub
	}

	public static CsvNameExportConfigurator NewInstance(ICdmDataSource source, File destinationFolder){

		return new CsvNameExportConfigurator(destinationFolder,source, null);

	}



	public ByteArrayOutputStream getByteOutputStream() {
		if (byteOutputStream == null){
			byteOutputStream = new ByteArrayOutputStream();
		}

		return byteOutputStream;
	}

	public void setByteOutputStream(ByteArrayOutputStream byteOutputStream) {
		this.byteOutputStream = byteOutputStream;
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

	@Override
	public CsvNameExportState getNewState() {
		return new CsvNameExportState(this);
	}
	@Override
	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				CsvNameExport.class
		};
	}

	public boolean isNamesOnly() {
		return namesOnly;
	}

	public void setNamesOnly(boolean namesOnly) {
		this.namesOnly = namesOnly;
	}

	public UUID getClassificationUUID() {
		return classificationUUID;
	}

	public void setClassificationUUID(UUID classificationUUID) {
		this.classificationUUID = classificationUUID;
	}

    /**
     * @return the rank
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(Rank rank) {
        this.rank = rank;
    }

}
