/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

/**
 * @author n.hoffmann
 * @since Sep 22, 2009
 */
public class DbcpSaveDataSource extends BasicDataSource {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbcpSaveDataSource.class);

	/* FIXME This is a workaround to solve a problem with dbcp connection pooling.
	 * Remove this when dbcp connection pool gets configured correctly
	 *
	 * (non-Javadoc)
	 * @see org.apache.commons.dbcp.BasicDataSource#createDataSource()
	 */
	@Override
	protected synchronized DataSource createDataSource() throws SQLException {
		super.createDataSource();
		connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
		return dataSource;
	}
}
