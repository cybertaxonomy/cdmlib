package eu.etaxonomy.cdm.io.api.application;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.persistence.hibernate.HibernateConfiguration;

public class CdmIoApplicationController extends CdmApplicationController {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmIoApplicationController.class);


	public static final String DEFAULT_APPLICATION_CONTEXT_RESOURCE = "/eu/etaxonomy/cdm/defaultIoApplicationContext.xml";


	public static CdmIoApplicationController NewInstance() throws DataSourceNotFoundException {
		return CdmIoApplicationController.NewInstance(getDefaultDatasource(), defaultDbSchemaValidation, false);
	}

	public static CdmIoApplicationController NewInstance(DbSchemaValidation dbSchemaValidation) throws DataSourceNotFoundException {
		return CdmIoApplicationController.NewInstance(getDefaultDatasource(), dbSchemaValidation, false);
	}

	/**
	 * Constructor, opens an spring ApplicationContext by using the according data source and the
	 * default database schema validation type
	 * @param dataSource
	 */
	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource) {
		return CdmIoApplicationController.NewInstance(dataSource, defaultDbSchemaValidation, false);
	}


	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) {
		return CdmIoApplicationController.NewInstance(dataSource, dbSchemaValidation, false);
	}

	public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation,
	        boolean omitTermLoading) {
		return NewInstance(dataSource, dbSchemaValidation, null, omitTermLoading);
	}

    public static CdmIoApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation,
            HibernateConfiguration hibernateConfig, boolean omitTermLoading) {
        return new CdmIoApplicationController(getClasspathResource(),dataSource, dbSchemaValidation,
                hibernateConfig, omitTermLoading, null);
    }

	/**
	 * @return
	 */
	protected static ClassPathResource getClasspathResource() {
		return new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE);
	}


	/**
	 * Constructor.
	 * @param applicationContextResource
	 * @param dataSource
	 * @param dbSchemaValidation
	 * @param omitTermLoading
	 * @param progressMonitor
	 */
	protected CdmIoApplicationController(Resource applicationContextResource, ICdmDataSource dataSource,
	        DbSchemaValidation dbSchemaValidation, HibernateConfiguration hibernateConfig,
			boolean omitTermLoading, IProgressMonitor progressMonitor) {
		super(applicationContextResource, dataSource, dbSchemaValidation, hibernateConfig,
		        omitTermLoading, progressMonitor, null);
	}

}
