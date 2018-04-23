/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 \* @since 15.06.2017
 *
 */
public class IndexRenamer extends SchemaUpdaterStepBase {
    private static final Logger logger = Logger.getLogger(IndexRenamer.class);

    private String tableName;

    private String oldIndexName;

    private String newIndexName;

    private String columnName;

    private Integer length;

// ********************** FACTORY ****************************************/

    public static final IndexRenamer NewStringInstance(String tableName, String oldIndexName,
            String newIndexName, String columnName, Integer length){
        return new IndexRenamer(tableName, oldIndexName,
                newIndexName, columnName, length == null ? 255 : length);
    }

    public static final IndexRenamer NewIntegerInstance(String tableName, String oldIndexName,
            String newIndexName, String columnName){
        return new IndexRenamer(tableName, oldIndexName, newIndexName, columnName, null);
    }
    /**
     * @param stepName
     */
    protected IndexRenamer(String tableName, String oldIndexName,
            String newIndexName, String columnName, Integer length) {
        super("Rename index " + oldIndexName + " to " + newIndexName);
        this.tableName = tableName;
        this.oldIndexName = oldIndexName;
        this.newIndexName = newIndexName;
        this.columnName = columnName;
        this.length = length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {
        try {
            String[] updateQuery = getCreateQuery(datasource, caseType, tableName, oldIndexName, newIndexName, columnName, length);
            try {
                datasource.executeUpdate(updateQuery[0]);
            } catch (Exception e) {
                if (updateQuery.length > 1){
                    datasource.executeUpdate(updateQuery[1]);
                }else{
                    throw e;
                }
            }
            return;
        } catch (Exception e) {
            String message = "Index ("+tableName +"."+oldIndexName+") could not be renamed "
                    + "to ("+newIndexName+") or drop/add was not possible.\n"
                    + "Please ask your admin to rename manually.";
            logger.warn(message);
            result.addWarning(message, this, "invoke");
            return;
        }

    }

    private String[] getCreateQuery(ICdmDataSource datasource, CaseType caseType, String tableName, String oldIndexName, String newIndexName, String columnName, Integer length) {
        DatabaseTypeEnum type = datasource.getDatabaseType();
//      String indexName = "_UniqueKey";
        String[] updateQueries;
        if (type.equals(DatabaseTypeEnum.MySQL)){
            //https://stackoverflow.com/questions/1463363/how-do-i-rename-an-index-in-mysql
            //in future: https://dev.mysql.com/worklog/task/?id=6555
            String format = "ALTER TABLE @@%s@@ DROP INDEX %s, ADD INDEX %s (%s%s)";
            updateQueries = new String[]{String.format(format, tableName, oldIndexName, newIndexName, columnName, length!= null ? "("+length+")": "")};
        }else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL)){
            //http://www.h2database.com/html/grammar.html#alter_index_rename
            //https://www.postgresql.org/docs/9.4/static/sql-alterindex.html (maybe IF EXISTS does not work prior to 9.x)
            String format = "ALTER INDEX %s %s RENAME TO %s";
            updateQueries = new String[]{String.format(format, " IF EXISTS ", oldIndexName, newIndexName),
                    String.format(format, "", oldIndexName, newIndexName)};

        }else if (type.equals(DatabaseTypeEnum.SqlServer2005)){
            //TODO Untested !!!!
            //https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-rename-transact-sql
            //https://www.mssqltips.com/sqlservertip/2709/script-to-rename-constraints-and-indexes-to-conform-to-a-sql-server-naming-convention/
            //https://stackoverflow.com/questions/40865886/rename-sql-server-index-in-ms-sql-server
            String format = "EXEC sp_rename N'%s.%s', N'%s', N'INDEX'";
            updateQueries = new String[]{String.format(format, tableName, oldIndexName, newIndexName)};
        }else{
            throw new IllegalArgumentException("Datasource type not supported yet: " + type.getName());
        }
//      updateQuery = updateQuery.replace("@indexName", indexName);
        for (int i = 0; i < updateQueries.length ; i++ ){
            updateQueries[i] = caseType.replaceTableNames(updateQueries[i]);
        }
        return updateQueries;
}

}
