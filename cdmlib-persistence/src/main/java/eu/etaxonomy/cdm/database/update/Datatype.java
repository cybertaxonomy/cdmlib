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

import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;

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
    DATE("date"),
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
            DdlTypeRegistry ddlRegistry = new TypeConfiguration().getDdlTypeRegistry();
            Dialect dialect = dbType.getHibernateDialect();

            result = result.replace("float", ddlRegistry.getTypeName(Types.FLOAT, dialect));
            result = result.replace("double", ddlRegistry.getTypeName(Types.DOUBLE, dialect));
            result = result.replace("bit", ddlRegistry.getTypeName(Types.BIT, dialect));
            result = result.replace("datetime", ddlRegistry.getTypeName(Types.TIMESTAMP, dialect));
            result = result.replace("date", ddlRegistry.getTypeName(Types.DATE, dialect));

            Dialect pgDialect = DatabaseTypeEnum.PostgreSQL.getHibernateDialect();
            result = result.replace("tinyint", ddlRegistry.getTypeName(Types.TINYINT, pgDialect));
        }
        //CLOB
        if (this == CLOB){
            //TODO use hibernate dialects
            if (dbType.isMySqlMariaDB()){
                result = "longtext";
            }else if (dbType.isH2()){
                result = "CLOB";  //or NVARCHAR
            }else if (dbType.isPostgres()){
                result = "text";
            }else if (dbType.isSqlServer()){
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
