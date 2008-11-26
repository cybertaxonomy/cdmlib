/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.CHECK;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.babadshanjan
 * @created 16.11.2008
 */
public abstract class ExportConfiguratorBase extends IoConfiguratorBase {

	private static final Logger logger = Logger.getLogger(ExportConfiguratorBase.class);

	private CHECK check = CHECK.CHECK_AND_EXPORT;

	private ICdmDataSource source;
	private Object destination;
	//private DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
	private CdmApplicationController cdmApp = null;
	protected ReferenceBase sourceReference;
	// TODO: Replace exportClassList by ioClassList
	protected Class<ICdmIoExport>[] exportClassList; 

	
	public ExportConfiguratorBase(){
		super();
		makeIoClassList();
	}
	
	abstract protected void makeIoClassList();
	
	public ICdmDataSource getSource() {
		return source;
	}
	
	public void setSource(ICdmDataSource source) {
		this.source = source;
	}
	
	/**
	 * @param source the source to get
	 */
	public Object getDestination() {
		return destination;
	}
	
	/**
	 * @param source the source to set
	 */
	public void setDestination(Object destination) {
		this.destination = destination;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
//	@Override
	public ReferenceBase getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = Database.NewInstance();
			sourceReference.setTitleCache("Data export");
		}
		return sourceReference;
	}
	
	public Class<ICdmIoExport>[] getIoClassList(){
		return exportClassList;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getCheck()
	 */
	public CHECK getCheck() {
		return this.check;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setCheck(eu.etaxonomy.cdm.io.tcs.TcsImportConfigurator.CHECK)
	 */
	public void setCheck(CHECK check) {
		this.check = check;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IExportConfigurator#getDbSchemaValidation()
	 */
//	public DbSchemaValidation getDbSchemaValidation() {
//		return dbSchemaValidation;
//	}
	
	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If a controller was already created before the last created controller is returned.
	 * @return
	 */
	public CdmApplicationController getCdmAppController(){
		return getCdmAppController(false);
	}
	
	/**
	 * Returns a new instance of <code>CdmApplicationController</code> created by the values of this configuration.
	 * @return
	 */
	public CdmApplicationController getNewCdmAppController(){
		return getCdmAppController(true, false);
	}
	
	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If create new is true always a new controller is returned, else the last created controller is returned. If no controller has
	 * been created before a new controller is returned.
	 * @return
	 */
	public CdmApplicationController getCdmAppController(boolean createNew){
		return getCdmAppController(createNew, false);
	}
	
	
	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If create new is true always a new controller is returned, else the last created controller is returned. If no controller has
	 * been created before a new controller is returned.
	 * @return
	 */
	public CdmApplicationController getCdmAppController(boolean createNew, boolean omitTermLoading){
		if (cdmApp == null || createNew == true){
			try {
				cdmApp = CdmApplicationController.NewInstance(this.getSource(), this.getDbSchemaValidation(), omitTermLoading);
			} catch (DataSourceNotFoundException e) {
				logger.error("could not connect to destination database");
				return null;
			}catch (TermNotFoundException e) {
				logger.error("could not find needed term in destination datasource");
				return null;
			}
		}
		return cdmApp;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#isValid()
	 */
	public boolean isValid(){
		boolean result = true;
		if (source == null){
			logger.warn("Connection to CDM could not be established");
			result = false;
		}
		if (destination == null){
			logger.warn("Invalid export destination");
			result = false;
		}
		
		return result;
	}
	
}
