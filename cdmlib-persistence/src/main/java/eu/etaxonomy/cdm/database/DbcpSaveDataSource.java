/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author n.hoffmann
 * @since Sep 22, 2009
 */
public class DbcpSaveDataSource extends BasicDataSource {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

    @Override
    public java.util.logging.Logger getParentLogger() {
        return java.util.logging.Logger.getLogger("eu.etaxonomy.cdm");
        //or throw
        //throw new SQLFeatureNotSupportedException("Log4j is used. JUL is not supported.");
    }
}