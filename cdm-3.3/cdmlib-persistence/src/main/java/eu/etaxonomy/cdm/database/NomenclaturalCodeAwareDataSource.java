// $Id$
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

import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author n.hoffmann
 * @created Sep 22, 2009
 * @version 1.0
 */
public class NomenclaturalCodeAwareDataSource extends BasicDataSource {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NomenclaturalCodeAwareDataSource.class);
	
	private NomenclaturalCode nomenclaturalCode;

	public NomenclaturalCode getNomenclaturalCode() {
		return nomenclaturalCode;
	}

	public void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}
	
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
