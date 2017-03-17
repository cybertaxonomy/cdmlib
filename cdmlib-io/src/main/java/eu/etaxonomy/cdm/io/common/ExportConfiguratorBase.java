/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.babadshanjan
 * @created 16.11.2008
 */
public abstract class ExportConfiguratorBase<DESTINATION extends Object, STATE extends ExportStateBase, TRANSFORM extends IExportTransformer> extends IoConfiguratorBase implements IExportConfigurator<STATE, TRANSFORM>{

	private static final Logger logger = Logger.getLogger(ExportConfiguratorBase.class);

	private CHECK check = CHECK.EXPORT_WITHOUT_CHECK;

	private TARGET target = TARGET.FILE;

	private final boolean startTransaction = true;

	private ICdmDataSource source;
	private DESTINATION destination;
	protected IDatabase sourceReference;
	protected Class<ICdmIO>[] ioClassList;

	protected ExportResultType resultType;
	/**
	 * The transformer class to be used for Input
	 */
	private TRANSFORM transformer;

	public ExportConfiguratorBase(TRANSFORM transformer){
		super();
		//setDbSchemaValidation(DbSchemaValidation.UPDATE);
		makeIoClassList();
		this.setTransformer(transformer);
	}

	abstract protected void makeIoClassList();


	@Override
    public TRANSFORM getTransformer() {
		return transformer;
	}

	@Override
    public void setTransformer(TRANSFORM transformer) {
		this.transformer = transformer;
	}


	@Override
    public ICdmDataSource getSource() {
		return source;
	}

	@Override
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
		if (this.sourceReference == null){
			sourceReference = ReferenceFactory.newDatabase();
			if (getSource() != null){
				sourceReference.setTitleCache(getSource().getDatabase(), true);
			}
		}
		return sourceReference;
	}

	@Override
    public Class<ICdmIO>[] getIoClassList(){
		return ioClassList;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getCheck()
	 */
	@Override
    public CHECK getCheck() {
		return this.check;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setCheck(eu.etaxonomy.cdm.io.tcsrdf.TcsRdfImportConfigurator.CHECK)
	 */
	public void setCheck(CHECK check) {
		this.check = check;
	}

	@Override
	public TARGET getTarget() {
	    return this.target;
	}

	@Override
	public void setTarget(TARGET target) {
	    this.target = target;
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

	/**
	 * Returns a new instance of <code>CdmApplicationController</code> created by the values of this configuration.
	 * @return
	 */
	public ICdmRepository getNewCdmAppController(){
		return getCdmAppController(true, false);
	}

	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If create new is true always a new controller is returned, else the last created controller is returned. If no controller has
	 * been created before a new controller is returned.
	 * @return
	 */
	public ICdmRepository getCdmAppController(boolean createNew){
		return getCdmAppController(createNew, false);
	}


	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If create new is true always a new controller is returned, else the last created controller is returned. If no controller has
	 * been created before a new controller is returned.
	 * @return
	 */
	public ICdmRepository getCdmAppController(boolean createNew, boolean omitTermLoading){
		if (cdmApp == null || createNew == true){
			cdmApp = CdmApplicationController.NewInstance(this.getSource(), this.getDbSchemaValidation(), omitTermLoading);
		}
		return cdmApp;
	}


	/**
	 * @return
	 */
	@Override
    public boolean isValid(){
		boolean result = true;
//		if (source == null && this.getCdmAppController() == null ){
//			logger.warn("Connection to CDM could not be established");
//			result = false;
//		}
		if (destination == null && getTarget() != TARGET.EXPORT_DATA){
			logger.warn("Invalid export destination");
			result = false;
		}

		return result;
	}

	@Override
    public String getSourceNameString() {
		if (this.getSource() == null) {
			return null;
		} else {
			return this.getSource().getName();
		}
	}

	@Override
	public ExportResultType getResultType(){return resultType;}


}
