/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.config;

import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * Abstract class representing the base CDM Source object.
 * 
 *
 */
public abstract class CdmSource implements ICdmSource {
	
	private String name;
	private String server;
	private int port;
	private NomenclaturalCode nomenclaturalCode;
	
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

	/**
	 * Sets the CDM Source {@link NomenclaturalCode}
	 * 
	 * @param nomenclaturalCode
	 */
	@Override
	public void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.config.ICdmSource#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.config.ICdmSource#getServer()
	 */
	@Override
	public String getServer() {
		return this.server;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.config.ICdmSource#getPort()
	 */
	@Override
	public int getPort() {
		return this.port;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.config.ICdmSource#getNomenclaturalCode()
	 */
	@Override
	public NomenclaturalCode getNomenclaturalCode() {
		return this.nomenclaturalCode;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.config.ICdmSource#getDbSchemaVersion()
	 */
	@Override
	public abstract String getDbSchemaVersion() throws CdmSourceException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.config.ICdmSource#isDbEmpty()
	 */
	@Override
	public abstract boolean isDbEmpty() throws CdmSourceException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.config.ICdmSource#checkConnection()
	 */
	@Override
	public abstract boolean checkConnection() throws CdmSourceException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.config.ICdmSource#getConnectionMessage()
	 */
	@Override
	public abstract String getConnectionMessage();
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.config.ICdmSource#closeOpenConnections()
	 */
	@Override
	public void closeOpenConnections() {
		
	}		

}
