/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.integration;

import java.sql.Types;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;

/**
 *
 * @deprecated can be fully replaced by org.dbunit.ext.hsqldb.HsqldbDataTypeFactory and thus shoiuld be removed (a.kohlbecker)
 */
public class HsqldbDataTypeFactory  extends DefaultDataTypeFactory {

   public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
      if (sqlType == Types.BOOLEAN) {
         return DataType.BOOLEAN;
      }

      return super.createDataType(sqlType, sqlTypeName);
    }
}
