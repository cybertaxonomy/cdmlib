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

	protected CdmIoApplicationController(Resource applicationContextResource, ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation,
			boolean omitTermLoading, IProgressMonitor progressMonitor) {
		super(applicationContextResource, dataSource, dbSchemaValidation,
				omitTermLoading, progressMonitor, null);
		
	}
	
	
	public static CdmApplicationController NewInstance() {
		return CdmIoApplicationController.NewInstance(getDefaultDatasource(), defaultDbSchemaValidation, false);
	}
	
	public static CdmApplicationController NewInstance(DbSchemaValidation dbSchemaValidation) {
		return CdmIoApplicationController.NewInstance(getDefaultDatasource(), dbSchemaValidation, false);
	}
	/**
	 * Constructor, opens an spring ApplicationContext by using the according data source and the
	 * default database schema validation type
	 * @param dataSource
	 */
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource) {
		return CdmApplicationController.NewInstance(getClasspathResource(), dataSource, defaultDbSchemaValidation, false);
	}

	
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) {
		return CdmApplicationController.NewInstance(getClasspathResource(), dataSource, dbSchemaValidation, false);
	}

	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) {
		return CdmApplicationController.NewInstance(getClasspathResource(), dataSource, dbSchemaValidation, omitTermLoading);
	}


	/**
	 * @return
	 */
	protected static ClassPathResource getClasspathResource() {
		return new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE);
	}

}
