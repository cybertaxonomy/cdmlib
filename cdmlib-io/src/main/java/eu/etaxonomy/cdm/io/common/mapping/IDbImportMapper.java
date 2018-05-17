/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 12.05.2009
 * @version 1.0
 */
public interface IDbImportMapper<STATE extends DbImportStateBase<?,?>, CDM_BASE extends CdmBase> {

	public void initialize(STATE state, Class<? extends CdmBase> destinationClass);

//	public void initialize(T state, String tableName);
	
	public CDM_BASE invoke(ResultSet rs, CDM_BASE cdmBase) throws SQLException;

}
