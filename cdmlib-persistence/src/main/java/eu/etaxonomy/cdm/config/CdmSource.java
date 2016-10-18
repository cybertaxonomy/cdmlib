/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.config;

/**
 * Abstract class representing the base CDM Source object.
 */
public abstract class CdmSource implements ICdmSource {

	private String name;
	private String server;
	private int port;

	public static final int NULL_PORT = -1;

	public static final String DEFAULT_ENTRY = "-";

	/**
	 * Sets the CDM Source name
	 *
	 * @param name
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the CDM Source server
	 *
	 * @param server
	 */
	@Override
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * Sets the CDM Source port
	 *
	 * @param port
	 */
	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getServer() {
		return this.server;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public abstract String getDbSchemaVersion() throws CdmSourceException;

	@Override
	public abstract boolean isDbEmpty() throws CdmSourceException;

	@Override
	public abstract boolean checkConnection() throws CdmSourceException;

	@Override
	public abstract String getConnectionMessage();

	@Override
	public void closeOpenConnections() {

	}

}
