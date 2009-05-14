// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.berlinModel.out.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import eu.etaxonomy.cdm.io.berlinModel.out.DbExportState;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public interface IDbExportMapper<T extends DbExportState<?>> {

//	public void initialize(PreparedStatement stmt, IndexCounter index);

	public void initialize(PreparedStatement stmt, IndexCounter index, T state, String tableName);
	
	public boolean invoke(CdmBase cdmBase) throws SQLException;

}