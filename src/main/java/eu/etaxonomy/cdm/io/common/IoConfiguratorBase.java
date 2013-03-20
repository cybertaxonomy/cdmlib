/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;

/**
 * Base class for all import/export configurators.
 * @author a.babadshanjan
 * @created 16.11.2008
 */
public abstract class IoConfiguratorBase extends ObservableBase implements IIoConfigurator, IIoObservable{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IoConfiguratorBase.class);

	//im-/export uses Classification for is_taxonomically_included_in relationships
	private boolean useClassification = true;
	
//	protected Class<ICdmIO>[] ioClassList;
	private DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
	
	protected CdmApplicationController cdmApp = null;
	
	//authentification token
	UsernamePasswordAuthenticationToken authenticationToken;
	
	private Set<IIoObserver> observers = new HashSet<IIoObserver>();
	
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

	@Override
	public UsernamePasswordAuthenticationToken getAuthenticationToken() {
		return this.authenticationToken;
	}

	@Override
	public void setAuthenticationToken(UsernamePasswordAuthenticationToken authenticationToken) {
		this.authenticationToken = authenticationToken;
		
	}

	@Override
	public void setAuthentication(String login, String password) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(login, password);
		this.authenticationToken = token;
	}
	
	
	
}
