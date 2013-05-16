package eu.etaxonomy.cdm.io.excel.bfn;

import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.stream.StreamImportConfiguratorBase;
/**
 * 
 * @author a.oppermann
 * @date 08.05.2013
 *
 */
public class ExcelStreamImportConfigurator extends StreamImportConfiguratorBase<ExcelStreamImportState, URI> implements IImportConfigurator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExcelStreamImportConfigurator.class);

	private static final String DEFAULT_REF_TITLE = "Excel Stream Import";

	
	private static IInputTransformer defaultTransformer = null;
	
	/**
	 * @param transformer
	 */
	public ExcelStreamImportConfigurator(IInputTransformer transformer) {
		super(transformer);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	@Override
	public ExcelStreamImportState getNewState() {
		return new ExcelStreamImportState(this);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#makeIoClassList()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void makeIoClassList() {
		ioClassList = new Class[]{
				ExcelStreamImport.class
		};
	}
	
	/**
	 * Factory method.
	 * @param uri
	 * @param destination
	 * @return
	 */
	public static ExcelStreamImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new ExcelStreamImportConfigurator(uri, destination);
	}
	
	/**
	 * Constructor.
	 * @param uri
	 * @param destination
	 */
	private ExcelStreamImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		this.setSource(uri);
		this.setDestination(destination);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.in.StreamImportConfiguratorBase#getDefaultSourceReferenceTitle()
	 */
	@Override
	protected String getDefaultSourceReferenceTitle() {
		return DEFAULT_REF_TITLE;
	}

}