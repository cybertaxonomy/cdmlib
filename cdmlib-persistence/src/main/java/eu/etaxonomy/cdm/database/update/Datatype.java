/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.Types;

import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @since 24.07.2019
 */
public enum Datatype {
    INTEGER("int"),
    CLOB("clob"),
    VARCHAR("varchar"),
    DATETIME("datetime"),
    DOUBLE("double"),
    TINYINTEGER("tinyint"),
    BIT("bit")
    ;

    private String defaultStr;
    private Datatype(String strType){
        this.defaultStr = strType;
    }

    /**
     * @param datasource
     * @param size
     * @return
     */
    public String format(ICdmDataSource datasource, Integer size) {
        String result = defaultStr;
        DatabaseTypeEnum dbType = datasource.getDatabaseType();
        //nvarchar
        if (dbType.equals(DatabaseTypeEnum.PostgreSQL)){  //TODO use PostgeSQL82 Dialect infos
            result = result.replace("nvarchar", "varchar");
            result = result.replace("double", "float8");
            result = result.replace("bit", DatabaseTypeEnum.PostgreSQL.getHibernateDialect().getTypeName(Types.BIT));
            result = result.replace("datetime", DatabaseTypeEnum.PostgreSQL.getHibernateDialect().getTypeName(Types.TIMESTAMP));
            result = result.replace("tinyint", DatabaseTypeEnum.PostgreSQL.getHibernateDialect().getTypeName(Types.TINYINT));
        }
        //CLOB
        if (this == CLOB){
            //TODO use hibernate dialects
            if (dbType.equals(DatabaseTypeEnum.MySQL)){
                result = "longtext";
            }else if (dbType.equals(DatabaseTypeEnum.H2)){
                result = "CLOB";  //or NVARCHAR
            }else if (dbType.equals(DatabaseTypeEnum.PostgreSQL)){
                result = "text";
            }else if (dbType.equals(DatabaseTypeEnum.SqlServer2005)){
                result = "NVARCHAR(MAX)";
            }
        }else if (this == VARCHAR){
            result = result + "(" + size + ")";
        }
        return result;
    }
}
