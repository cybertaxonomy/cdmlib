/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.common.NullProgressMonitor;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;

/**
 * @author a.babadshanjan
 * @created 16.11.2008
 */
public abstract class IoConfiguratorBase implements IIoConfigurator{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IoConfiguratorBase.class);

	//im-/export uses Classification for is_taxonomically_included_in relationships
	private boolean useClassification = true;
	
//	protected Class<ICdmIO>[] ioClassList;
	private DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
	
	private CdmApplicationController cdmApp = null;
	
	private boolean doAuthors = true;
	//references
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	//names
	private boolean doTaxonNames = true;
	private boolean doTypes = true;
	
	//taxa
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;
	
	//etc

	private IProgressMonitor progressMonitor;

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IIoConfigurator#getDbSchemaValidation()
	 */
	public DbSchemaValidation getDbSchemaValidation() {
		return dbSchemaValidation;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IIoConfigurator#setDbSchemaValidation(eu.etaxonomy.cdm.database.DbSchemaValidation)
	 */
	public void setDbSchemaValidation(DbSchemaValidation dbSchemaValidation) {
		this.dbSchemaValidation = dbSchemaValidation;
	}
	
	public CdmApplicationController getCdmAppController(){
		return this.cdmApp;
	}
	
	/**
	 * @param cdmApp the cdmApp to set
	 */
	public void setCdmAppController(CdmApplicationController cdmApp) {
		this.cdmApp = cdmApp;
	}
	
	public boolean isDoAuthors() {
		return doAuthors;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoAuthors(boolean)
	 */
	public void setDoAuthors(boolean doAuthors) {
		this.doAuthors = doAuthors;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#getDoReferences()
	 */
	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoReferences(eu.etaxonomy.cdm.io.tcsrdf.TcsRdfImportConfigurator.DO_REFERENCES)
	 */
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}
	
	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoTaxonNames(boolean)
	 */
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoTypes()
	 */
	public boolean isDoTypes() {
		return doTypes;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoTypes(boolean)
	 */
	public void setDoTypes(boolean doTypes) {
		this.doTypes = doTypes;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoTaxa()
	 */
	public boolean isDoTaxa() {
		return doTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoTaxa(boolean)
	 */
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoRelTaxa()
	 */
	public boolean isDoRelTaxa() {
		return doRelTaxa;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoRelTaxa(boolean)
	 */
	public void setDoRelTaxa(boolean doRelTaxa) {
		this.doRelTaxa = doRelTaxa;
	}
	
	/**
	 * @return the useClassification
	 */
	public boolean isUseClassification() {
		return useClassification;
	}
	

	/**
	 * @param useClassification the useClassification to set
	 */
	public void setUseClassification(boolean useClassification) {
		this.useClassification = useClassification;
	}
	
	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		this.progressMonitor = monitor;
	}
	
	public IProgressMonitor getProgressMonitor(){
		return progressMonitor != null ? progressMonitor : new NullProgressMonitor();
	}
}
