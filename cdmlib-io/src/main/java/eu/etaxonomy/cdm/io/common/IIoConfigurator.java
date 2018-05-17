/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.config.Configuration;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.persistence.hibernate.HibernateConfiguration;


/**
 * @author a.babadshanjan
 * @since 13.11.2008
 */
public interface IIoConfigurator extends IIoObservable{

	/**
	 * A String representation of the used source may it be a source to be imported (e.g. "BerlinModel Cichorieae Database")
	 * or a source to be exported (e.g. "CDM Cichorieae Database")
	 * @return String representing the source for the io
	 */
	public String getSourceNameString();


	/**
	 * A String representation of the destination may it be an import destination and therefore a CDM (e.g. CDM Cichorieae Database)
	 * or an export destination (e.g. CDM XML)
	 * @return
	 */
	public String getDestinationNameString();


	/**
	 * Returns the CdmApplicationController
	 * @return
	 */
	public ICdmRepository getCdmAppController();


	/**
	 * Sets the CdmApplicationController
	 * @param cdmApp the cdmApp to set
	 */
	public void setCdmAppController(ICdmRepository cdmApp);

	/**
	 * Get the way how the CDM schema is validated
	 * @see eu.etaxonomy.cdm.database.DbSchemaValidation
	 * @return
	 */
	public DbSchemaValidation getDbSchemaValidation();

	/**
	 * Get the way how the CDM schema is validated
	 * For exports values that delete the source (CREATE, CREATE_DROP) are not allowed and may throw an
	 * Exception in the further run
	 * @see eu.etaxonomy.cdm.database.DbSchemaValidation
	 * @param dbSchemaValidation
	 */
	public void setDbSchemaValidation(DbSchemaValidation dbSchemaValidation);


	/**
	 * Returns the progress monitor.
	 * @return
	 */
	public IProgressMonitor getProgressMonitor();

	/**
	 * Sets the progress monitor.
	 * @see #getProgressMonitor()
	 * @param monitor
	 */
	public void setProgressMonitor(IProgressMonitor monitor);

	/**
	 * Returns the observers for this import/export
	 * @return
	 */
	@Override
    public Set<IIoObserver> getObservers();

	/**
	 * Sets the observers for this import/export
	 * @param observers
	 */
	public void setObservers(Set<IIoObserver> observers);

	public UsernamePasswordAuthenticationToken getAuthenticationToken();

	public void setAuthenticationToken(UsernamePasswordAuthenticationToken token);

	public void setAuthentication(String login, String password);

	/**
	 * Creates the UsernamePasswordAuthenticationToken for the default admin.
	 *
	 * @see Configuration#adminLogin
	 * @see Configuration#adminPassword
	 */
	public void authenticateAsDefaultAdmin();

    /**
     * @return the hibernate configuration to use, if some values are set
     */
    public HibernateConfiguration getHibernateConfig();

}
