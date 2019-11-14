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
 * Interface defining the the given IO-class should check each mapper if it should be ignored
 * on the given record.
 *
 * @author a.mueller
 * @since 02.03.2010
 */
public interface ICheckIgnoreMapper {

	/**
	 * Decides if the mapper should be ignored for the current record of the import
	 * result set.
	 * @param mapper the mapper to be decided on
	 * @param rs the import result set containing the current record
	 * @return result, wether to ignore
	 * @throws SQLException
	 */
	public boolean checkIgnoreMapper(IDbImportMapper mapper, ResultSet rs) throws SQLException;

}
