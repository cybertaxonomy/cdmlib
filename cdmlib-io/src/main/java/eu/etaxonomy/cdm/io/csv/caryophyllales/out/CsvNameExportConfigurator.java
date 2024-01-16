package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CsvIOConfigurator;
import eu.etaxonomy.cdm.io.common.XmlExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.name.Rank;


public class CsvNameExportConfigurator extends XmlExportConfiguratorBase<CsvNameExportState>{

    private static final long serialVersionUID = 412364298450440297L;

    private ByteArrayOutputStream byteOutputStream;
	private boolean namesOnly = false;
	private UUID classificationUUID;
	private boolean condensedDistribution = false;
	private boolean invalidNamesQuoted = false;
	private CsvIOConfigurator csvIOConfig = CsvIOConfigurator.NewInstance();

    private Rank rank = Rank.GENUS();

    private UUID statusTree;

	protected CsvNameExportConfigurator(File destination,
			ICdmDataSource cdmSource, IExportTransformer transformer) {
		super(destination, cdmSource, transformer);
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
		return csvIOConfig.getEncoding();
	}
	public void setEncoding(String encoding) {
		this.csvIOConfig.setEncoding(encoding);
	}

	public String getLinesTerminatedBy() {
		return csvIOConfig.getLinesTerminatedBy();
	}
	public void setLinesTerminatedBy(String linesTerminatedBy) {
		this.csvIOConfig.setLinesTerminatedBy(linesTerminatedBy);
	}

	public String getFieldsEnclosedBy() {
		return  csvIOConfig.getFieldsEnclosedBy();
	}
	public void setFieldsEnclosedBy(String fieldsEnclosedBy) {
		this.csvIOConfig.setFieldsEnclosedBy(fieldsEnclosedBy);
	}

	public boolean isHasHeaderLines() {
		return  csvIOConfig.isIncludeHeaderLines();
	}
	public void setHasHeaderLines(boolean hasHeaderLines) {
		this.csvIOConfig.setIncludeHeaderLines(hasHeaderLines);
	}

	public String getFieldsTerminatedBy() {
		return  csvIOConfig.getFieldsTerminatedBy();
	}
	public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
		this.csvIOConfig.setFieldsTerminatedBy(fieldsTerminatedBy);
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

    public Rank getRank() {
        return rank;
    }
    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public boolean isCondensedDistribution() {
        return condensedDistribution;
    }
    public void setCondensedDistribution(boolean condensedDistribution) {
        this.condensedDistribution = condensedDistribution;
    }

    public boolean isInvalidNamesQuoted() {
        return invalidNamesQuoted;
    }
    public void setInvalidNamesQuoted(boolean invalidNamesQuoted) {
        this.invalidNamesQuoted = invalidNamesQuoted;
    }

    public UUID getStatusTree() {
        return statusTree;
    }
    public void setStatusTree(UUID statusTree) {
        this.statusTree = statusTree;
    }
}
