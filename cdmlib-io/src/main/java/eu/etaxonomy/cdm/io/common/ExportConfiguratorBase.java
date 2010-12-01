/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.CHECK;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.babadshanjan
 * @created 16.11.2008
 */
public abstract class ExportConfiguratorBase<DESTINATION extends Object> extends IoConfiguratorBase {

	private static final Logger logger = Logger.getLogger(ExportConfiguratorBase.class);

	private CHECK check = CHECK.EXPORT_WITHOUT_CHECK;
	
	private ICdmDataSource source;
	private DESTINATION destination;
	protected IDatabase sourceReference;
	protected Class<ICdmIO>[] ioClassList;

	
	public ExportConfiguratorBase(){
		super();
		//setDbSchemaValidation(DbSchemaValidation.UPDATE);
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
	public DESTINATION getDestination() {
		return destination;
	}
	
	/**
	 * @param source the source to set
	 */
	public void setDestination(DESTINATION destination) {
		this.destination = destination;
	}
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
//	@Override
	public IDatabase getSourceReference() {
		//TODO //needed
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		if (this.sourceReference == null){
			sourceReference = refFactory.newDatabase();
			if (getSource() != null){
				sourceReference.setTitleCache(getSource().getDatabase(), true);
			}
		}
		return sourceReference;
	}
	
	public Class<ICdmIO>[] getIoClassList(){
		return ioClassList;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getCheck()
	 */
	public CHECK getCheck() {
		return this.check;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setCheck(eu.etaxonomy.cdm.io.tcsrdf.TcsRdfImportConfigurator.CHECK)
	 */
	public void setCheck(CHECK check) {
		this.check = check;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IExportConfigurator#getDbSchemaValidation()
	 */
//	public DbSchemaValidation getDbSchemaValidation() {
//		return dbSchemaValidation;
//	}
	
//	/**
//	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
//	 * If a controller was already created before the last created controller is returned.
//	 * @return
//	 */
//	public CdmApplicationController getCdmAppController(){
//		return getCdmAppController(false);
//	}
//	
//	/**
//	 * Returns a new instance of <code>CdmApplicationController</code> created by the values of this configuration.
//	 * @return
//	 */
//	public CdmApplicationController getNewCdmAppController(){
//		return getCdmAppController(true, false);
//	}
//	
//	/**
//	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
//	 * If create new is true always a new controller is returned, else the last created controller is returned. If no controller has
//	 * been created before a new controller is returned.
//	 * @return
//	 */
//	public CdmApplicationController getCdmAppController(boolean createNew){
//		return getCdmAppController(createNew, false);
//	}
//	
//	
//	/**
//	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
//	 * If create new is true always a new controller is returned, else the last created controller is returned. If no controller has
//	 * been created before a new controller is returned.
//	 * @return
//	 */
//	public CdmApplicationController getCdmAppController(boolean createNew, boolean omitTermLoading){
//		if (cdmApp == null || createNew == true){
//			try {
//				cdmApp = CdmApplicationController.NewInstance(this.getSource(), this.getDbSchemaValidation(), omitTermLoading);
//			} catch (DataSourceNotFoundException e) {
//				logger.error("could not connect to destination database");
//				return null;
//			}catch (TermNotFoundException e) {
//				logger.error("could not find needed term in destination datasource");
//				return null;
//			}
//		}
//		return cdmApp;
//	}
	
	
	/**
	 * @return
	 */
	public boolean isValid(){
		boolean result = true;
//		if (source == null && this.getCdmAppController() == null ){
//			logger.warn("Connection to CDM could not be established");
//			result = false;
//		}
		if (destination == null){
			logger.warn("Invalid export destination");
			result = false;
		}
		
		return result;
	}
	
	public String getSourceNameString() {
		if (this.getSource() == null) {
			return null;
		} else {
			return (String)this.getSource().getName();
		}
	}
	
}
