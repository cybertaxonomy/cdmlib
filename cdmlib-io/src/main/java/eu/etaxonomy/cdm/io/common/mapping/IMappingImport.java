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

import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 * @created 23.02.2010
 * @version 1.0
 */
public interface IMappingImport<T extends CdmBase, STATE extends ImportStateBase> {
	
	
	public T createObject(ResultSet rs, STATE state) throws SQLException;
}
