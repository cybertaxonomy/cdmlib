package eu.etaxonomy.cdm.api.application;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public class CdmIoApplicationController extends CdmApplicationController {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmIoApplicationController.class);


	public static final String DEFAULT_APPLICATION_CONTEXT_RESOURCE = "/eu/etaxonomy/cdm/defaultIoApplicationContext.xml";

	protected CdmIoApplicationController(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation,
			boolean omitTermLoading, IProgressMonitor progressMonitor) {
		super(getClasspathResource(), dataSource, dbSchemaValidation,
				omitTermLoading, progressMonitor, null);
		
	}
	
	
	

	public static CdmIoApplicationController NewInstance() {
		return CdmIoApplicationController.NewInstance(getDefaultDatasource(), defaultDbSchemaValidation, false);
	}
	
	public static CdmIoApplicationController NewInstance(DbSchemaValidation dbSchemaValidation) {
		return CdmIoApplicationController.NewInstance(getDefaultDatasource(), dbSchemaValidation, false);
	}
	/**
	 * Constructor, opens an spring ApplicationContext by using the according data source and the
	 * default database schema validation type
	 * @param dataSource
	 */
	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource) {
		return new CdmIoApplicationController(dataSource, defaultDbSchemaValidation, false, null);
	}

	
	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) {
		return new CdmIoApplicationController(dataSource, dbSchemaValidation, false, null);
	}

	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) {
		return new CdmIoApplicationController(dataSource, dbSchemaValidation, omitTermLoading, null);
	}
	
	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading, IProgressMonitor progressMonitor){
		return new CdmIoApplicationController(dataSource, dbSchemaValidation, omitTermLoading, progressMonitor);
	}
	

	/**
	 * @return
	 */
	protected static ClassPathResource getClasspathResource() {
		return new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE);
	}

}
