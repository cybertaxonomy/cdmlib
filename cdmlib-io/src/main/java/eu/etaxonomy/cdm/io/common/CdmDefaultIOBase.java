/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 */

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * This is an exporter that invokes the application aware defaultExport when
 * invoked itself
 * 
 * @author a.babadshanjan
 * @created 17.11.2008
 */
public class CdmDefaultIOBase<T extends IIoConfigurator> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(CdmDefaultIOBase.class);

	protected ICdmApplicationConfiguration cdmApp = null;

	/**
	 * Creates a new {@link CdmApplicationController} if it does not exist yet
	 * or if createNew is <ocde>true</code>
	 * 
	 * @param config
	 * @param destination
	 * @param omitTermLoading
	 * @param createNew
	 * @return
	 */
	protected boolean startApplicationController(IIoConfigurator config,
			ICdmDataSource cdmSource, boolean omitTermLoading, boolean createNew) {
		if (config.getCdmAppController() != null) {
			this.cdmApp = config.getCdmAppController();
		}
		DbSchemaValidation schemaValidation = config.getDbSchemaValidation();
		if (this instanceof CdmDefaultExport) {
			if (schemaValidation.equals(DbSchemaValidation.CREATE)
					|| schemaValidation.equals(DbSchemaValidation.CREATE_DROP)) {
				throw new IllegalArgumentException(
						"The export may not run with DbSchemaValidation.CREATE or DbSchemaValidation.CREATE_DROP as this value deletes the source database");
			}
		}

		if (createNew == true || cdmApp == null) {
			cdmApp = CdmApplicationController.NewInstance(cdmSource,
					schemaValidation, omitTermLoading);
			if (cdmApp != null) {
				return true;
			} else {
				return false;
			}
		}
		return true;

	}

	/**
	 * Returns the {@link CdmApplicationController}. This is null if invoke()
	 * has not been called yet and if the controller has not been set manually
	 * by setCdmApp() yet.
	 * 
	 * @return the cdmApp
	 */
	public ICdmApplicationConfiguration getCdmAppController() {
		return this.cdmApp;
	}

	/**
	 * @param cdmApp
	 *            the cdmApp to set
	 */
	public void setCdmAppController(ICdmApplicationConfiguration cdmApp) {
		this.cdmApp = cdmApp;
	}

}
