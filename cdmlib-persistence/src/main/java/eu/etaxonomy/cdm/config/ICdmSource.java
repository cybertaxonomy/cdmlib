/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.config;

import java.util.Map;

import eu.etaxonomy.cdm.model.metadata.CdmMetaData.MetaDataPropertyName;

/**
 * Interface which represents any CDM Source
 *
 */
public interface ICdmSource {

	/**
	 * Returns the name representation of this CDM Source
	 *
	 * @return name representation of this CDM Source
	 */
	public String getName();

	/**
	 * Sets the name representation of this CDM Source
	 *
	 * @param name
	 * @return
	 */
	public void setName(String name);

	/**
	 * Returns the server (string representation) where this CDM Source resides
	 *
	 * @return server (string representation) where this CDM Source resides
	 */
	public String getServer();

	/**
	 * Sets the server (string representation) where this CDM Source resides
	 *
	 * @param server
	 * @return
	 */
	public void setServer(String server);

	/**
	 * Returns the port on which this CDM Source is listening
	 *
	 * @return port on which this CDM Source is listening
	 */
	public int getPort();

	/**
	 * Sets the port on which this CDM Source is listening
	 *
	 * @param port
	 */
	public void setPort(int port);

	/**
	 * Returns the CDM  schema version of this CDM Source
	 *
	 * @return CDM  schema version of this CDM Source
	 * @throws CdmSourceException if any underlying error
	 */
	public String getDbSchemaVersion() throws CdmSourceException;

	/**
	 * Checks whether the underlying database is empty
	 *
	 * @return true if underlying database is empty, o/w false
	 * @throws CdmSourceException
	 */
	public boolean isDbEmpty() throws CdmSourceException;


	/**
	 * Tests, if a database connection can be established.
	 * @return true if test was successful, false otherwise
	 * @throws CdmSourceException if any underlying error
	 */
	public boolean checkConnection() throws CdmSourceException;

	/**
	 * Returns the message to display when connecting to this CDM Source
	 *
	 * @return message to display when connecting to this CDM Source
	 */
	public String getConnectionMessage();

	/**
	 * Closes any open connections to this CDM Source
	 *
	 */
	public void closeOpenConnections();

	public Map<MetaDataPropertyName, String> getMetaDataMap() throws CdmSourceException ;

}
