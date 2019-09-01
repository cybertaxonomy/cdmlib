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

/**
 * @author a.mueller
 * @since 02.03.2010
 */
public interface ICheckIgnoreMapper {

	public boolean checkIgnoreMapper(IDbImportMapper mapper, ResultSet rs) throws SQLException;

}
