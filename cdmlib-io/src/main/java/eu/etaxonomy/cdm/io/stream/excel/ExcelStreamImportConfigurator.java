package eu.etaxonomy.cdm.io.stream.excel;

import java.io.InputStream;
import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.dwca.in.DwcaDataImportConfiguratorBase;
import eu.etaxonomy.cdm.io.excel.stream.ExcelStreamImportState;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
/**
 *
 * @author a.oppermann
 * @since 08.05.2013
 *
 */
public class ExcelStreamImportConfigurator extends DwcaDataImportConfiguratorBase<ExcelStreamImportState> implements IImportConfigurator {

    private static final long serialVersionUID = 5093164389086186710L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExcelStreamImportConfigurator.class);

	private static final String DEFAULT_REF_TITLE = "Excel Stream Import";

	private static IInputTransformer defaultTransformer = null;

	private InputStream stream = null;


	/**
	 * Factory method.
	 * @param uri
	 * @param destination
	 * @return
	 */
	public static ExcelStreamImportConfigurator NewInstance(URI uri, ICdmDataSource destination, NomenclaturalCode nomenclaturalCode, DbSchemaValidation dbSchemaValidation){
		return new ExcelStreamImportConfigurator(uri, destination, nomenclaturalCode, dbSchemaValidation);
	}

	/**
	 * Constructor.
	 * @param uri
	 * @param destination
	 */
	private ExcelStreamImportConfigurator(URI uri, ICdmDataSource destination, NomenclaturalCode nomenclaturalCode, DbSchemaValidation dbSchemaValidation) {
		super(uri, destination, defaultTransformer);
		setDbSchemaValidation(dbSchemaValidation);
		setNomenclaturalCode(nomenclaturalCode);
	}

	/**
     * Constructor.
     * @param uri
     * @param destination
     */
    private ExcelStreamImportConfigurator(InputStream stream, ICdmDataSource destination, NomenclaturalCode nomenclaturalCode, DbSchemaValidation dbSchemaValidation) {
        super(null, destination, defaultTransformer);
        setDbSchemaValidation(dbSchemaValidation);
        setNomenclaturalCode(nomenclaturalCode);
        this.stream = stream;
    }

	@Override
	public ExcelStreamImportState getNewState() {
		return new ExcelStreamImportState(this);
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void makeIoClassList() {
		ioClassList = new Class[]{
				ExcelStreamImport.class
		};
	}

	@Override
	protected String getDefaultSourceReferenceTitle() {
		return DEFAULT_REF_TITLE;
	}

}
