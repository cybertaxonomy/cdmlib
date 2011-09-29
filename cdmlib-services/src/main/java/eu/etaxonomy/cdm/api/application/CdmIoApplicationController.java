package eu.etaxonomy.cdm.api.application;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public class CdmIoApplicationController extends CdmApplicationController {
	private static final Logger logger = Logger.getLogger(CdmIoApplicationController.class);


	public static final String DEFAULT_APPLICATION_CONTEXT_RESOURCE = "/eu/etaxonomy/cdm/defaultIoApplicationContext.xml";

	protected CdmIoApplicationController(Resource applicationContextResource,
			ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation,
			boolean omitTermLoading, IProgressMonitor progressMonitor) {
		super(applicationContextResource, dataSource, dbSchemaValidation,
				omitTermLoading, progressMonitor);
		
	}
	
	/**
	 * Constructor, opens an spring ApplicationContext by using the according data source and the
	 * default database schema validation type
	 * @param dataSource
	 */
	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource) {
		return CdmIoApplicationController.NewInstance(new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE), dataSource, defaultDbSchemaValidation, false);
	}
	
	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) {
		return CdmIoApplicationController.NewInstance(new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE), dataSource, dbSchemaValidation, false);
	}

	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) {
		return CdmIoApplicationController.NewInstance(new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE), dataSource, dbSchemaValidation, omitTermLoading);
	}
	
	public static CdmIoApplicationController NewInstance(Resource applicationContextResource, ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) {
		return CdmIoApplicationController.NewInstance(applicationContextResource, dataSource, dbSchemaValidation, omitTermLoading, null);
	}
	
	public static CdmIoApplicationController NewInstance(Resource applicationContextResource, ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading, IProgressMonitor progressMonitor) {
		return new CdmIoApplicationController(applicationContextResource, dataSource, dbSchemaValidation, omitTermLoading, progressMonitor);
	}

}
