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
    FLOAT("float"),
    TINYINTEGER("tinyint"),
    BIT("bit"),
    BIGDECIMAL("decimal"),
    ;

    private String defaultStr;

    private Datatype(String strType){
        this.defaultStr = strType;
    }

    public String format(ICdmDataSource datasource, Integer size) {
        return format(datasource, size, null);
    }

    public String format(ICdmDataSource datasource, Integer size, Integer scale) {
        String result = defaultStr;
        DatabaseTypeEnum dbType = datasource.getDatabaseType();
        //nvarchar
        if (dbType.equals(DatabaseTypeEnum.PostgreSQL)){  //TODO use PostgeSQL82 Dialect infos
            result = result.replace("nvarchar", "varchar");
            result = result.replace("float", dbType.getHibernateDialect().getTypeName(Types.FLOAT));
            result = result.replace("double", dbType.getHibernateDialect().getTypeName(Types.DOUBLE));
            result = result.replace("bit", dbType.getHibernateDialect().getTypeName(Types.BIT));
            result = result.replace("datetime", dbType.getHibernateDialect().getTypeName(Types.TIMESTAMP));
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
        }else if (this == BIGDECIMAL){
            result = result + "(" + size + "," + scale + ")";
        }
        return result;
    }
}
