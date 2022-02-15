/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.io.application;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.api.application.CdmIoApplicationController;
import eu.etaxonomy.cdm.persistence.hibernate.HibernateConfiguration;

//for now we need this controller only within test therefore it is only in test modul
public class CdmRemoteApplicationController extends CdmIoApplicationController {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmRemoteApplicationController.class);


	public static final String DEFAULT_APPLICATION_CONTEXT_RESOURCE = "/eu/etaxonomy/cdm/applicationRemoteContext.xml";


	public static CdmRemoteApplicationController NewRemoteInstance() {
		return CdmRemoteApplicationController.NewRemoteInstance(null, null);
	}

	public static CdmRemoteApplicationController NewRemoteInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) {
	    try {
            dataSource = dataSource == null? getDefaultDatasource() : dataSource;
        } catch (DataSourceNotFoundException e) {
            throw new RuntimeException(e);
        }
	    dbSchemaValidation = dbSchemaValidation == null? defaultDbSchemaValidation : dbSchemaValidation;
	    return CdmRemoteApplicationController.NewRemoteInstance(dataSource, dbSchemaValidation, null, false);
	}

    public static CdmRemoteApplicationController NewRemoteInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation,
            HibernateConfiguration hibernateConfig, boolean omitTermLoading) {
        return new CdmRemoteApplicationController(getClasspathResource(),dataSource, dbSchemaValidation,
                hibernateConfig, omitTermLoading, null);
    }

// *************************************** CONSTRUCTOR ***********************************************************/

	protected CdmRemoteApplicationController(Resource applicationContextResource, ICdmDataSource dataSource,
	        DbSchemaValidation dbSchemaValidation, HibernateConfiguration hibernateConfig,
			boolean omitTermLoading, IProgressMonitor progressMonitor) {
		super(applicationContextResource, dataSource, dbSchemaValidation, hibernateConfig,
		        omitTermLoading, progressMonitor);
	}


    protected static ClassPathResource getClasspathResource() {
        return new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE);
    }
}